package textProcessing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import logger.logLiason;
import monitor.cmcMonitorController;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcTextDAO;

public class cmcTextImport {
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private int MAX_UIDSTACK = 1000;
	
	class ImportLine
	{
		String text=null;
		boolean isDefined=false;
		ImportLine()
		{
			text="";
			isDefined=false;
		}
	}
	class ImportParagraph
	{
		long UID;
		ImportLine[] translatedText;
		String[] languageList;
		ImportParagraph(long il,int nlanguages)
		{
			UID=il;
			translatedText = new ImportLine[nlanguages];
			languageList = new String[nlanguages];
			for(int i=0;i<translatedText.length;i++)
			{
				translatedText[i] = new ImportLine();
				languageList[i] = "UNKNOWN";
			}
		}
	}
	ImportParagraph[] imported = null;
	
	long[] uidStack = null;
	String[] languageStack = null;
	String DAOOriginalLanguage=null;
	String[] DAOLangList = null;
	private int nbrOfChanges=0;
	
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
 	public cmcTextImport(cmcProcSettings iM, logLiason ilog)
    // ---------------------------------------------------------------------------------
 	{
 		xMSet = iM;
		logger = ilog;
 	}
 	
 	// ---------------------------------------------------------------------------------
 	public boolean importAllText()
    // ---------------------------------------------------------------------------------
 	{
 		boolean isOK = true;
 		if( xMSet.xU.IsBestand( xMSet.getExportFileName() ) == false )  {
 			do_error("Cannot locate import file [" + xMSet.getExportFileName() + "]");
 			return false;
 		}
 		
 		cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
		moni.startMonitor(xMSet.getExportFileName());
     	int maxiter = xMSet.getmoma().getScanListSize();
		for(int iter=0;iter<maxiter;iter++)
		{
		  String sCMXUID = xMSet.getmoma().popScanListItem();  // also sets the starttime
 		  if( sCMXUID == null ) break;
 		  moni.syncMonitor();
 		  if( importTextFromArchive(sCMXUID) == false ) { isOK=false; break; }
 		  moni.syncMonitorComment(sCMXUID, "Number of modifications = " + nbrOfChanges);
 		  moni.syncMonitorEnd(sCMXUID);
		}
		moni.requestDelayedClose();
		xMSet.purgeDirByName(xMSet.getTempDir(),false);
		return isOK;
 	}
 	
 	//------------------------------------------------------------
    private boolean importTextFromArchive(String sCMXUID)
 	//------------------------------------------------------------
    {
 		nbrOfChanges=0;
 		imported=null;
 		DAOLangList=null;
 		DAOOriginalLanguage=null;
 		uidStack = new long[MAX_UIDSTACK];
 		for(int i=0;i<uidStack.length;i++) uidStack[i]=-1L;
 		languageStack = new String[150];
 		for(int i=0;i<languageStack.length;i++) languageStack[i]=null;
 		
 		// read the import file and extract the data for this CMXUID
 		do_log(5,"Processing [" + sCMXUID + "]");
 		
 		// First pass
 		// determine Original Language
 		// determine number of UIDs
 		// determine start en stop line
 		BufferedReader reader = null;
 		int startLine=-1;
 		int endLine=-1;
 		String originalLanguage=null;
 		try {
			  reader = new BufferedReader(new InputStreamReader(new FileInputStream(xMSet.getExportFileName()), xMSet.getCodePageString()));
	       	  //
			  boolean inBlock=false;
			  boolean found=false;
	       	  int lineCounter=-1;
	       	  String sLijn=null;
        	  while ((sLijn=reader.readLine()) != null) {
        		 lineCounter++;
		         if( sLijn.trim().length() < 1) continue;
		         String sComp = xMSet.xU.keepLettersAndNumbers(sLijn).trim();
		          
		         // look for <!-- UID Start en <!-- UID Stop
		         if( (sLijn.trim().startsWith("<!-- ")) && (sLijn.trim().endsWith(" -->")) ) {
		           if( sComp.toUpperCase().startsWith("UIDSTART")) { inBlock=true; found=false; continue; }
		           if( sComp.toUpperCase().startsWith("UIDSTOP")) { inBlock=false; found = false; continue; }
		         }
		         //  
		         if( inBlock == false ) continue;
		         // look for the correct CMXUID
		         if( (sLijn.trim().startsWith("<!-- ")) && (sLijn.trim().endsWith(" -->")) ) {
		        	 if( sComp.toUpperCase().startsWith("CMXUID")) {
		        		 int l1 = sLijn.indexOf("[");
        		    	 int l2 = sLijn.indexOf("]");
        		    	 if( (l1<0) || (l2<0) || (l2<=l1) ) continue;
        		    	 String sTest = sLijn.substring(l1+1,l2);
		        		 if( sTest.compareTo(sCMXUID) == 0) {
		        			 //do_error("GOT [" + sTest + "]");
		        			 startLine=lineCounter;
		        			 found=true;
		        			 continue;
		        		 }
		        	 }
			     }    
		         // 
		         if( found == false ) continue;
		         endLine = lineCounter;
		         // 
		         long luid = getLineUID(sLijn);
		         if(  luid >= 10000L ) { // UIDs are big numbers
		        	 pushUID( luid );
		        	 continue;
		         }
		         //
		         if( (sLijn.trim().startsWith("<!-- ")) && (sLijn.trim().endsWith(" -->")) ) {
		        	 if( sComp.toUpperCase().startsWith("ARTEFACTLANGUAGE") ) {
		        		 originalLanguage=xMSet.xU.RemplaceerIgnoreCase(sComp, "ARTEFACTLANGUAGE", "").trim();
		        	 }
		        	 if( sComp.toUpperCase().startsWith("LANGUAGE") ) {
		        		 pushLanguageStack( xMSet.xU.RemplaceerIgnoreCase(sComp, "LANGUAGE", "").trim() );
		        	 }
		         }       
		         //do_log(5, sCMXUID + " " + sLijn );
        	  }
 		}
 		catch( Exception e ) {
 			do_error("Error reading [" + xMSet.getExportFileName() + "]");
 			return false;
 		}
 		finally {
 			try {
 				reader.close();
 			}
 			catch(Exception e) {
 				do_error("Cannot close [" + xMSet.getExportFileName() + "] " + e.getMessage());
 				return false;
 			}
 		}
		// check the results           
 		if( (startLine<0) || (endLine<0) || (endLine<startLine)) {
 			do_error("Could not locate CMXUID [" + sCMXUID + "] in file [" + xMSet.getExportFileName() + "]");
 			return false;
 		}
 		// count the number of different UIDs
 		int nuid = getUidStackDepth();
 		if (nuid == 0 ) {
 			do_log(5,"No UIDs found - Nothing to do");
 			return true;
 		}
 		// originallanguage
 		if( originalLanguage == null ) {
 			do_error("Could not find Artefact language ");
 			return false;
 		}
 		// languages
 		int nlang = getLanguageStackDepth();
 		if( nlang == 0 ) {
 			do_log(5,"No languages found - nothing to do");
 			return true;
 		}
 		// check the languages
 		if( checkLanguageStack() == false ) return false;
 		//
 		do_log( 5 , "[CMXUID="+sCMXUID + "] [S=" + startLine + "] [E=" + endLine + "] [Nuid=" + nuid + "] [ArteLang=" + originalLanguage + "] [NLangs=" + nlang + "]");
		
 		
 		// init memory structures
 		imported = new ImportParagraph[nuid];
        for(int i=0;i<nuid;i++)
        {
          imported[i] = new ImportParagraph( uidStack[i] , nlang);
          for( int j=0;j<nlang;j++) imported[i].languageList[j] = languageStack[j];
        }
        
        
        // re read and insert
    	try {
			  reader = new BufferedReader(new InputStreamReader(new FileInputStream(xMSet.getExportFileName()), xMSet.getCodePageString()));
	       	  //
			  int lineCounter=-1;
	       	  String sLijn=null;
	       	  String currLang="??";
	       	  int langIdx=-1;
      	      while ((sLijn=reader.readLine()) != null) {
      		     lineCounter++;
      		     if( lineCounter < startLine) continue;
      		     if( lineCounter > endLine ) break;
		         if( sLijn.trim().length() < 1) continue;
		         String sComp = xMSet.xU.keepLettersAndNumbers(sLijn).trim();
			     //
		         if( (sLijn.trim().startsWith("<!-- ")) && (sLijn.trim().endsWith(" -->")) ) {
		        	 if( sComp.toUpperCase().startsWith("LANGUAGE") ) {
		        		 currLang =  xMSet.xU.RemplaceerIgnoreCase(sComp, "LANGUAGE", "").trim();
		        		 langIdx = getLangIdx( currLang );
		        		 continue;
		        	 }
		         }
		         if( langIdx < 0 ) continue;
		         
		         // UID
		         long luid = getLineUID(sLijn);
		         if(  luid >= 10000L ) { // UIDs are big numbers
		        	 
		        	 int idx = sLijn.indexOf(": ");
		        	 if( idx < 0 ) continue;
		        	 String sCut = sLijn.substring(idx+1).trim();
		             int uidIdx = getUIDIdx( luid );
	                 if( uidIdx < 0 ) {
	                	 do_error("Could not find UID IDx for [" + luid + "]");
	                	 continue;
	                 }
	                 
		        	 //do_log(5,currLang + " " + langIdx + " " + luid + " " + uidIdx + " ["  + sCut + "]");
		        	 // set it
		        	 try {
		        	   imported[uidIdx].translatedText[langIdx].text = sCut.trim();
		        	   imported[uidIdx].translatedText[langIdx].isDefined = true;
		        	 }
		        	 catch(Exception e ) {
		        		 do_error("Should never happen - got an OOB " + e.getMessage());
		        		 return false;
		        	 }
			     }
		         
		         
      	      }
      	}
      	catch( Exception e ) {
 			do_error("Error reading [" + xMSet.getExportFileName() + "]");
 			return false;
 		}
 		finally {
 			try {
 				reader.close();
 			}
 			catch(Exception e) {
 				do_error("Cannot close [" + xMSet.getExportFileName() + "] " + e.getMessage());
 				return false;
 			}
 		}
      	
    	//
    	//runImportReport();
    	
        // load the textObject via DAO    	
    	String XMLLangFile = unzipLanguageXML(sCMXUID);
    	if( XMLLangFile == null ) return false;
    
    	// read the data via textdao
    	cmcTextDAO tao = new cmcTextDAO( xMSet , logger);
    	cmcTextObject[] objs = tao.readTextObjectsFromFile(XMLLangFile);
        DAOOriginalLanguage = tao.getOriginalLanguageCode();
        DAOLangList = tao.getLanguagList();
        
    	// compare imported met objs
    	compareAndFix( imported , objs );
  	    do_log(5,"Number of changes detected " + nbrOfChanges);	
    	
    	// write back to ZIP
    	if( nbrOfChanges != 0) {
    		 runChangeReport(objs);
    		 
    		 // copy the TEMP/lang.xml to CACHE/texteditor.xml
    		 // So you cna reuse the exsiting update text
    		 String TargetFile = xMSet.getLanguageXML();
    		 if( xMSet.xU.IsBestand( TargetFile ) ) {
    			 xMSet.xU.VerwijderBestand(TargetFile);
    			 if( xMSet.xU.IsBestand( TargetFile ) ) {
    				 do_error("Cannot remove [" + TargetFile + "]");
    				 tao=null;
    				 return false;
    			 }			 
    		 }
    		 // move
    		 try {
    			 xMSet.xU.copyFile( XMLLangFile , TargetFile );
    		 }
    		 catch(Exception e ) {
    			 do_error("Cannot move [" + XMLLangFile + "] to [" + TargetFile + "]");
    			 tao=null;
				 return false;
    		 }
    		 if( xMSet.xU.IsBestand( TargetFile ) == false ) {
    			 do_error("Cannot move [" + XMLLangFile + "] to [" + TargetFile + "]");
    			 tao=null;
                 return false;
    		 }	    
    		 // upsert
    		 boolean ib = tao.flushChangesToXML( objs , DAOOriginalLanguage );
    		 do_log(5 , "Flush changes [" + ib + "] from [" + TargetFile + "]");
    		 // rezip
    		 if( ib ) {  // recreate the ZIP file
     			cmcArchiveDAO archo = new cmcArchiveDAO( xMSet , logger );
     			String ZipFileName = xMSet.getArchiveDir() + xMSet.xU.ctSlash + sCMXUID + "_set.zip";
     	 		ib = archo.reZipAllFiles(ZipFileName);
     			archo=null;
     		}		 
    	}
    	// remove
    	
    	//
    	tao=null;
    	return true;
 	}
 	
    //------------------------------------------------------------
    private String unzipLanguageXML(String sCMXUID)
 	//------------------------------------------------------------
    {
 		String ZipFileName = xMSet.getArchiveDir() + xMSet.xU.ctSlash + sCMXUID + "_set.zip";
 		if( xMSet.xU.IsBestand(ZipFileName) == false ) {
 			do_error("Cannot locate archive file [" + ZipFileName + "]");
 			return null;
 		}
 		String requestFileName = sCMXUID + "_lang.xml";
 		cmcArchiveDAO xao = new cmcArchiveDAO(xMSet,logger);
 	 	xao.unzip_SingleFile(ZipFileName, requestFileName);
 	 	xao = null;
 	    String LangXML = xMSet.getTempDir() + xMSet.xU.ctSlash + sCMXUID + "_lang.xml"; 		
 	    if( xMSet.xU.IsBestand(LangXML) == false ) {
			do_error("Cannot locate XML file [" + LangXML + "]");
			return null;
		}
 	 	return LangXML;
 	}
 	
    //------------------------------------------------------------
    private void pushUID(long uid)
 	//------------------------------------------------------------
 	{
 		for(int i=0;i<uidStack.length;i++)
 		{
 	       if( uidStack[i] == uid ) return;
 	    }
 		for(int i=0;i<uidStack.length;i++)
 		{
 	       if( uidStack[i] != -1L ) continue;
 	       uidStack[i] = uid;
 	       return;
 	    }
 		do_error("Too many UIDs");
 	}
 
    //------------------------------------------------------------
    private int getUidStackDepth()
 	//------------------------------------------------------------
  	{
 		int ncount = 0;
 		for(int i=0;i<uidStack.length;i++)
 		{
 	       if( uidStack[i] != -1L ) ncount++;
 	    }
 		return ncount;
 	}

    //------------------------------------------------------------
    private int getUIDIdx(long il)
 	//------------------------------------------------------------
    {
 		if( il < 0L ) return -1;
 		for(int i=0;i<uidStack.length;i++)
 		{
 	       if( uidStack[i] == il) return i;
 	    }
 		return -1;
 	}

    //------------------------------------------------------------
    private void pushLanguageStack(String sin)
 	//------------------------------------------------------------
    {
 		if( sin == null ) return;
 		for(int i=0;i<languageStack.length;i++)
 		{
 			if( languageStack[i] == null ) break;
 			if( languageStack[i].compareTo(sin) == 0 ) return;
 		}
 		for(int i=0;i<languageStack.length;i++)
 		{
 			if( languageStack[i] == null ) {
 				languageStack[i] = sin.trim().toUpperCase();
 				return;
 			}
 		}
 		do_error("too many languages");
 	}
 	
    //------------------------------------------------------------
    private int getLanguageStackDepth()
 	//------------------------------------------------------------
    {
 		int count=0;
 		for(int i=0;i<languageStack.length;i++)
 		{
 			if( languageStack[i] == null ) continue;
 			count++;
 		}
 		return count;
 	}
 	
    //------------------------------------------------------------
    private boolean checkLanguageStack()
 	//------------------------------------------------------------
    {
 		boolean isok=true;
 		String[] list = xMSet.getLanguageList();
 		for(int i=0;i<languageStack.length;i++)
 		{
 			if( languageStack[i] == null ) continue;
 			boolean found = false;
 			for( int k=0;k<list.length;k++)
 			{
 				if( list[k].compareToIgnoreCase( languageStack[i] ) == 0) {
 					found = true;
 					break;
 				}
 			}
 			if( found == false ) {
 				do_error("Unsupported language [" + languageStack[i]  + "]");
 				isok=false;
 			}
 			//do_log(5, " -> " + languageStack[i] + " " + found);
 		}
 		return isok;
 	}

    //------------------------------------------------------------
    private int getLangIdx(String sLang)
 	//------------------------------------------------------------
    {
 		if( sLang == null ) return -1;
 		for(int i=0;i<languageStack.length;i++)
 		{
 			if( languageStack[i] == null ) continue;
 			if( sLang.compareToIgnoreCase( languageStack[i] ) == 0 ) return i;
 		}
 		return -1;
 	}
 	
    //------------------------------------------------------------
    private long getLineUID(String sIn)
 	//------------------------------------------------------------
    {
 		if( sIn == null ) return -1L;
 		String sRet = sIn.trim();
 		int l1 = sRet.indexOf("$");
 		int l2 = sRet.indexOf(": ");
 		if( (l1<0) || (l2<0) || (l2<l1) ) return -1L;
 		try {
 			String sl = sRet.substring(l1+1,l2);
 			long ll = xMSet.xU.NaarLong(sl);
 			//do_log( 5 , "-->" + sl + " " + ll + " " + sIn);
 			return ll;
 		}
 		catch(Exception e ) { return -1L; }
 	}
 	
    //------------------------------------------------------------
    private int getImportParagraphViaUID(long uid)
 	//------------------------------------------------------------
   {
 		for(int i=0;i<imported.length;i++)
    	{
    		ImportParagraph x = imported[i];
    		if( x == null ) continue;
    		if( x.UID == uid ) return i;
    	}
 		return -1;
 	}
 	
    //------------------------------------------------------------
    private int getDAOLangIdx( String slang)
 	//------------------------------------------------------------
 	{
 		int idx=-1;
 		for(int i=0;i<DAOLangList.length;i++)
 		{
 			if( DAOLangList[i].compareToIgnoreCase(slang) == 0) return i;
 		}
 		return idx;
 	}

    //------------------------------------------------------------
    private void runImportReport()
 	//------------------------------------------------------------
    {
 	    // run a report
    	for(int i=0;i<imported.length;i++)
    	{
    		ImportParagraph x = imported[i];
    		if( x == null ) continue;
    		for(int j=0;j<x.translatedText.length;j++)
    		{
    			ImportLine y = x.translatedText[j];
    			if( y == null ) continue;
    			if( y.isDefined == false ) continue;
    			do_log(5,"[L=" + j + "] [" + y.isDefined + "] [" + y.text + "]" );
    		}
    	}
 	}
 	
 	
    //------------------------------------------------------------
    private boolean compareAndFix(ImportParagraph[] impList , cmcTextObject[] daoList  )
 	//------------------------------------------------------------
    {
    	do_log(5,"Comparing Imported [" + impList.length + "] with DAO extracted [" + daoList.length + "] text objects from"); 		
 		
    	for(int i=0;i<daoList.length;i++)
    	{
    	   cmcTextObject x = daoList[i];
    	   if( x == null ) continue;
    	   if( x.removed == true ) continue;
    	   int idx = getImportParagraphViaUID( x.UID );
    	   if( idx < 0 ) continue;
    	   compareTextObjects( impList[idx] , x );
    	}
    	return true;
 	}
 
    //------------------------------------------------------------
    private void runChangeReport( cmcTextObject[] daoList  )
	//------------------------------------------------------------
    {
    	for(int i=0;i<daoList.length;i++)
    	{
    	   cmcTextObject x = daoList[i];
    	   if( x == null ) continue;
    	   if( x.hasChanged == false ) continue;
    	   for(int k=0;k<x.TextTranslated.length;k++)
    	   {
    		   if( x.TextTranslated[k] == null ) continue;
    		   if( x.TextTranslated[k].trim().length() < 1) continue;
    		   do_log(5,x.TextTranslated[k]);
    	   }
    	}
 	}
 
    //------------------------------------------------------------
    private boolean compareTextObjects( ImportParagraph imp , cmcTextObject txt  )
 	//------------------------------------------------------------
   {
 		if( imp.UID != txt.UID ) return false;
 		
 		// loop through the imp object and find its txt equivalent
 		// loop around the languages
 		for(int l=0;l<imp.languageList.length;l++)  // langlist = {ENGLISH, FRENCH, }
 		{
 			String ImpLanguage = languageStack[l];
 			boolean updateTextFrom = ( ImpLanguage.compareToIgnoreCase(DAOOriginalLanguage) == 0 ) ? true : false;
 			int daoLangIdx = getDAOLangIdx( ImpLanguage );
 			for(int k=0;k<imp.translatedText.length;k++)  // transtect = { english text, french text, etc }
 			{
 				if( l != k ) continue;
 				if( imp.translatedText[k].isDefined == false ) continue;
 				
 				String ImportText = imp.translatedText[k].text;
 				String DAOText = txt.TextTranslated[ daoLangIdx];
 				
 				//do_log(5 , "" + imp.UID + " " + ImpLanguage + " " + daoLangIdx + " (" + ImportText + ") = (" + DAOText + ")");
 				if( ImportText.compareTo(DAOText) == 0 ) continue;
 				nbrOfChanges++;
 				//
 				do_log(5 , "Change " + imp.UID + " " + ImpLanguage + " " + daoLangIdx + " (" + ImportText + ") = (" + DAOText + ")");
 				//
 				txt.TextTranslated[ daoLangIdx] = ImportText;
 				if( updateTextFrom ) txt.TextFrom = ImportText;
 				txt.changeDate = System.currentTimeMillis();
 				txt.hasChanged = true;
 			}
 		}
 		return true;
 	}
    
    
}
