package textProcessing;

import generalpurpose.gpPrintStream;
import logger.logLiason;
import monitor.cmcMonitorController;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcTextDAO;

public class cmcTextExport {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	String dividerline=""; 
	gpPrintStream print =null;
	int dumpCounter=0;
    
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
    
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }
    
    // ---------------------------------------------------------------------------------
 	public cmcTextExport(cmcProcSettings iM, logLiason ilog)
    // ---------------------------------------------------------------------------------
 	{
 		xMSet = iM;
		logger = ilog;
		dividerline = ""; for(int i=0;i<100;i++) dividerline += "_";
		print = null;
 	}
 	
    // ---------------------------------------------------------------------------------
 	public boolean exportAllText()
    // ---------------------------------------------------------------------------------
 	{
 	  try {
 		boolean isOK = true;
 		cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
		moni.startMonitor(xMSet.getScanFolder());
		
		// Just extract all the LANG files
		cmcArchiveDAO xao = new cmcArchiveDAO( xMSet , logger);
		xao.extractAllLangFiles(xMSet.getTempDir());
		xao=null;
		//
		openReport();
		// Loop through the selected archives and see whether there is one in temp
		int maxiter = xMSet.getmoma().getScanListSize();
		for(int iter=0;iter<maxiter;iter++)
		{
		  String ArchiveFileName = xMSet.getmoma().popScanListItem();  // also sets the starttime
 		  if( ArchiveFileName == null ) break;
 		  moni.syncMonitor();
 		  if( extractTextFromArchive(ArchiveFileName) == false ) { isOK=false; break; }
 		  moni.syncMonitorComment(ArchiveFileName , "Lines exported = " + dumpCounter ); dumpCounter=0;
 		  moni.syncMonitorEnd(ArchiveFileName);
		}
		moni.requestDelayedClose();
		xMSet.purgeDirByName(xMSet.getTempDir(),false);
		closeReport();
		return isOK;
 	  }
 	  catch(Exception e ) {
 			do_error("Error exporting text" + e.getMessage() );
 			e.printStackTrace();
 			return false;
 	  }
 	}

    // ---------------------------------------------------------------------------------
 	private void openReport()
    // ---------------------------------------------------------------------------------
 	{
 	   print = null;
 	   String FName = xMSet.getTextReportName();
 	   print = new gpPrintStream( FName , xMSet.getCodePageString() );
 	   //
 	   dump("<!-- Application    : " + xMSet.getApplicDesc().trim() + " -->");
       dump("<!-- Start          : " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase().trim() + " -->");
       dump("<!-- Folder         : " + xMSet.getScanFolder().trim() + " -->");
       dump("<!-- Encoding       : " + xMSet.getCodePageString().trim() + " -->");
       dump("<!-- Format Version : " + cmcProcConstants.ExportFormatVersion + " -->");
       dump("");
       //
 	}
    // ---------------------------------------------------------------------------------
 	private void closeReport()
    // ---------------------------------------------------------------------------------
 	{
 		if( print != null ) {
 			print.do_standard_tail("");
 			print.close();
 		}
 		print = null;
 	}
    // ---------------------------------------------------------------------------------
 	private void dump(String s )
    // ---------------------------------------------------------------------------------
 	{
 		dumpCounter++;
 		if( print != null ) print.println(s);
 	}

    // ---------------------------------------------------------------------------------
 	private boolean extractTextFromArchive(String FName)
    // ---------------------------------------------------------------------------------
 	{
 		String ShortZip = xMSet.xU.getFolderOrFileName(FName);
 		String ShortLang = xMSet.xU.RemplaceerIgnoreCase(ShortZip , "_set.zip" , "_lang.xml");
 		String LangFileName = xMSet.getTempDir() + xMSet.xU.ctSlash + ShortLang.trim();
 		if( xMSet.xU.IsBestand( LangFileName ) == false ) {
 			do_error("Could not extract _Lang.XML from archive [" + FName + "] to [" + LangFileName + "]");
 			return false;
 		}
		do_log(5,"Extracting text [" + LangFileName + "]");
		cmcTextDAO tao = new cmcTextDAO( xMSet , logger);
		cmcTextObject[] objs = tao.readTextObjectsFromFile( LangFileName );
		if( objs == null ) {
			tao = null;
			return false;
		}
		//
		String OriginalLanguageCode = tao.getOriginalLanguageCode() == null ? "???": tao.getOriginalLanguageCode().trim();
		// Header
		dump(dividerline);
		dump("<!-- UID Start         [" + tao.getUID() + "] -->");
		//dump("<!-- ArchiveFileName   [" + ShortZip + "] -->");
		dump("<!-- Image             [" + tao.getImageFileName() + "] -->");
		dump("<!-- CMXUID            [" + tao.getCMXUID() + "] -->");
		dump("<!-- Artefact Language [" + OriginalLanguageCode + "] -->");
		//
		// number of languages
		int totLang = objs[0].TextTranslated.length;
		for(int la=-1;la<totLang;la++)
		{
			String LangCode = (la==-1) ? OriginalLanguageCode : xMSet.getLanguageCode( la , false );
			if( LangCode == null ) LangCode = "N/A";
		    if( (LangCode.compareToIgnoreCase(OriginalLanguageCode) == 0) && (la != -1) ) continue;
		    //
		    int counter=0;
		    for(int i=0;i<objs.length;i++)
		    {
			 cmcTextObject x = objs[i];
			 if( x == null ) continue;
			 if( x.confidence != cmcProcEnums.TextConfidence.TEXT ) continue;
			 if( x.removed ) continue;
			 String sPhrase = null;
			 if( la == -1 ) sPhrase = x.TextFrom;
			           else sPhrase = x.TextTranslated[la];
			 if( sPhrase == null ) continue;
			 if( sPhrase.trim().length() < 1) continue;
			 //
			 counter++;
			 if( counter == 1) {
				 dump("");
				 dump("<!-- Language [" + LangCode + "] -->");
			 }
		     dump("$" + (""+x.UID).trim() + ": " + sPhrase);
		    }
		    if( counter != 0 ) dump(" ");
		 }
		 dump("<!-- UID Stop           [" + tao.getUID() + "] -->");
		 //dump(dividerline);
		 //
 		 tao=null;
 		 return true;
 	}
}
