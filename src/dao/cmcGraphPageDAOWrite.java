package dao;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import logger.logLiason;
import generalpurpose.gpAppendStream;
import generalpurpose.gpPrintStream;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphPageObject;

public class cmcGraphPageDAOWrite {

	cmcProcSettings xMSet=null;
    logLiason logger=null;
    
	private gpAppendStream app = null;
	private cmcGraphPageObject[] ar_pabo=null;
	
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

	//------------------------------------------------------------
	public cmcGraphPageDAOWrite( cmcProcSettings is , logLiason ilog)
	//------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
	}
	
	//------------------------------------------------------------
	private cmcProcEnums.EditChangeType getParagraphEditChangeType(long iUID)
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_pabo.length ; i++)
		{
			if( ar_pabo[i].UID != iUID) continue;
			if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) &&(ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
			return ar_pabo[i].changetipe;
		}
		return cmcProcEnums.EditChangeType.NONE;
	}
	
	//------------------------------------------------------------
	private int countNumberOfElements(int bundleidx)
	//------------------------------------------------------------
	{
		int nbr=0;
		for(int i=0;i<ar_pabo.length ; i++)
		{
			if( ar_pabo[i].BundelIdx != bundleidx) continue;
			if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) &&(ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
			nbr++;
		}
		return nbr;
	}
	
	//------------------------------------------------------------
	private int countNumberOfLetterElements(int bundleidx)
	//------------------------------------------------------------
	{
		int nbr=0;
		for(int i=0;i<ar_pabo.length ; i++)
		{
			if( ar_pabo[i].BundelIdx != bundleidx) continue;
			//if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) && (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
			if( ar_pabo[i].tipe != cmcProcEnums.PageObjectType.LETTER) continue;
			nbr++;
		}
		return nbr;
	}
	
	//------------------------------------------------------------
	private void doNewParagraphs()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_pabo.length ; i++)
		{
			if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) &&(ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
			if( ar_pabo[i].changetipe != cmcProcEnums.EditChangeType.CREATE ) continue;
			
			boolean isLetter = (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? true : false;
			
			app.AppendIt("<paragraph>" );
			app.AppendIt("<paragraphUID>" + ar_pabo[i].UID + "</paragraphUID>" );
			app.AppendIt("<Bundle>" + ar_pabo[i].BundelIdx + "</Bundle>" );
			app.AppendIt("<NumberOfElements>" + countNumberOfElements(ar_pabo[i].BundelIdx) + "</NumberOfElements>" );
			app.AppendIt("<MinX>" + ar_pabo[i].MinX + "</MinX>" );
			app.AppendIt("<MinY>" + ar_pabo[i].MinY + "</MinY>" );
			app.AppendIt("<Width>" + (ar_pabo[i].MaxX - ar_pabo[i].MinX + 1) + "</Width>" );
			app.AppendIt("<Heigth>" + (ar_pabo[i].MaxY - ar_pabo[i].MinY + 1) + "</Heigth>" );
			app.AppendIt("<IsLetterParagraph>" + isLetter + "</IsLetterParagraph>" );
			app.AppendIt("<NumberOfLetters>" + countNumberOfLetterElements(ar_pabo[i].BundelIdx) + "</NumberOfLetters>" );
			app.AppendIt("<paragraphRemoved>" + ar_pabo[i].removed + "</paragraphRemoved>" );
			app.AppendIt("<paragraphChangeType>" + (""+ar_pabo[i].changetipe).toLowerCase() + "</paragraphChangeType>" );
			app.AppendIt("<paragraphChangeDate>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</paragraphChangeDate>" );
			app.AppendIt("</paragraph>" );
			
		}
	}
	
	//------------------------------------------------------------
	public boolean updateArchiveFile(cmcGraphPageObject[] iar)
	//------------------------------------------------------------
	{
		ar_pabo = iar;
		// check whether the <file>_stat.xml is in the Cache dir
		String FNaam = xMSet.getCacheDir() + xMSet.xU.ctSlash + "GraphEditor.xml";
		if( xMSet.xU.IsBestand( FNaam ) == false ) {
			do_error("Cannot locate the work XML file [" + FNaam + "]");
			return false;
		}
		// Create a target XML file
		String FTgt = xMSet.getCacheDir() + xMSet.xU.ctSlash + "New_GraphEditor.xml";
		gpPrintStream ps = new gpPrintStream(FTgt,"UTF8");
		ps.close();
		ps = null;
		// link the attach file to it  
		app = new gpAppendStream(FTgt,xMSet.getCodePageString());
		
		
		// start copying and updating
		try {
			File inFile  = new File(FNaam);  // File to read from.
	       	//BufferedReader reader = new BufferedReader(new FileReader(inFile));
	  	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	long curUID=-1L;
	       	boolean inpara=false;
	       	boolean indump=false;
	       	cmcProcEnums.EditChangeType currChange = cmcProcEnums.EditChangeType.NONE;
	       	while ((sLijn=reader.readLine()) != null) {
	       		
	       		// Add processHist
	       		if( sLijn.toLowerCase().indexOf("</processhistory>") >= 0 ) {
	       			app.AppendIt("<ProcessTimeStamp>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</ProcessTimeStamp>");
	       			app.AppendIt(sLijn);
		       		continue;	
	       		}
	       		if( sLijn.toLowerCase().indexOf("<paragraph>") >= 0 ) { inpara=true; currChange = cmcProcEnums.EditChangeType.NONE; }
	       		if( sLijn.toLowerCase().indexOf("</paragraph>") >= 0 ) { inpara=false; curUID=-1L; }
	       		if( sLijn.toLowerCase().indexOf("</paragraphs>") >= 0 ) doNewParagraphs();  // end
	       		//
	       		if( inpara ) {
	       			String sVal=null;
	       			sVal = xMSet.xU.extractXMLValue(sLijn,"paragraphUID"); 
	       			if ( sVal != null ) { 
	       				curUID = xMSet.xU.NaarLong(sVal);
	       				currChange = getParagraphEditChangeType(curUID);
	      
	       			}
	       			if( currChange !=  cmcProcEnums.EditChangeType.NONE ) {
	       				//
	       				if( sLijn.toLowerCase().indexOf("<isletterparagraph>") >= 0 ) {
	       					if( (currChange == cmcProcEnums.EditChangeType.TO_TEXT) || (currChange == cmcProcEnums.EditChangeType.TO_NO_TEXT) ) {
	       						boolean istext = (currChange == cmcProcEnums.EditChangeType.TO_TEXT) ? true : false;
	       						app.AppendIt("<IsLetterParagraph>" + istext + "</IsLetterParagraph>" ); 
	       						continue;
	       					}
	       				}
	       				//
	       				if( sLijn.toLowerCase().indexOf("<paragraphremoved>") >= 0 ) continue;
	       				if( sLijn.toLowerCase().indexOf("<paragraphchangedate>") >= 0 ) continue;
	       				if( sLijn.toLowerCase().indexOf("<paragraphchangetype>") >= 0 ) {
	       				 	do_log(1,"Updating paragraph UID[" + curUID + "] to [" + currChange + "]");
	       					boolean removed = (currChange ==  cmcProcEnums.EditChangeType.REMOVE) ? true : false;
	       					app.AppendIt("<paragraphRemoved>" + removed + "</paragraphRemoved>" ); 
	       					app.AppendIt("<paragraphChangeType>" + (""+currChange).toLowerCase() + "</paragraphChangeType>" );
	       					app.AppendIt("<paragraphChangeDate>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</paragraphChangeDate>" );
	       					continue;
	       				}
	       			}
	       		}
	       		//
	       		if( sLijn.toLowerCase().indexOf("<graphicaleditorarea>") >= 0 ) { indump=true; }
	       		if( sLijn.toLowerCase().indexOf("</graphicaleditorarea>") >= 0 ) { 
	       			/*
	       			cmcComponentDump ccd = new cmcComponentDump(xMSet);
	       			ccd.dumpComponents(ar_pabo);
	       			*/
	       			cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
	       			boolean ib = dao.dumpComponents(ar_pabo,app);
	       			if( ib == false ) {
	       				System.err.println("could not edit");
	       			}
	       			//
	       			indump=false;
	       			continue;
	       		}
	       		if( indump ) continue;
	       		//
	       		//
	       		app.AppendIt(sLijn);
	       		
	       	}
	       	reader.close();
		}
		catch(Exception e) {
			do_error("Error reading [" + FNaam + "] " + xMSet.xU.LogStackTrace(e));
			return false;
		}
		app.CloseAppendFile();
		return true;
	}
	
}
