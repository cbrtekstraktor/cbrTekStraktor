package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import logger.logLiason;
import generalpurpose.gpAppendStream;
import textProcessing.cmcTextObject;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;


public class cmcTextDAO {
	
	cmcProcSettings xMSet=null;
    cmcProcEnums cenum = null;
	logLiason logger=null;
	
    private String DEFAULTLANG = "English";
    private String OriginalLanguage=DEFAULTLANG;
    private String[] langList=null;
    private String currImageFileName=null;
    private String currImagePath=null;
    private String currImageCMXUID=null;
    private String currImageUID=null;

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

	//-----------------------------------------------------------------------
	public cmcTextDAO(cmcProcSettings iM , logLiason ilog)
	//-----------------------------------------------------------------------
	{
        xMSet = iM;
        logger = ilog;
        cenum = new cmcProcEnums(xMSet);
    }

	//------------------------------------------------------------
	private String Escape(String sin)
	//------------------------------------------------------------
	{
		return "<![CDATA[" + sin.trim() + "]]>";
	}

	//------------------------------------------------------------
	private String UnEscape(String sin)
	//------------------------------------------------------------
	{
	  if( sin.startsWith("<![CDATA[" ) == false ) return sin;
	  try {
	   String sRes = sin.substring("<![CDATA[".length(),sin.length() - "]]>".length()  );
	   return sRes;
	  }
	  catch( Exception e) {
		return "Cannot unescape " + sin;  
	  }
	}
	
	//------------------------------------------------------------
	private void dump_textobj_array(gpAppendStream app , cmcTextObject[] ar)
	//------------------------------------------------------------
	{
		app.AppendIt("<!-- NumberOfParagraphs : includes Text Paragraphs and Non-text Paragraphs -->" );
		app.AppendIt("<NumberOfParagraphs>"+ ar.length + "</NumberOfParagraphs>");
		for(int i=0;i<ar.length;i++)
		{
			String sDat = ar[i].changeDate <= 0L ? "" : (""+xMSet.xU.prntDateTime(ar[i].changeDate,"yyyyMMddHHmmss")).trim();
			//
			app.AppendIt("<TextBundle>");
			app.AppendIt("<TextBundleIdx>" + ar[i].BundelIdx + "</TextBundleIdx>" );
			app.AppendIt("<TextBundleUID>" + ar[i].UID + "</TextBundleUID>" );
			app.AppendIt("<TextConfidence>" + (""+ar[i].confidence).toLowerCase() + "</TextConfidence>" );
			app.AppendIt("<TextBundleRemoved>" + ar[i].removed + "</TextBundleRemoved>" );
			app.AppendIt("<TextBundleChangeDate>" + sDat + "</TextBundleChangeDate>" );
			app.AppendIt("<TextOCR>"+ Escape(ar[i].TextOCR.trim()) + "</TextOCR>");
			app.AppendIt("<TextFrom>"+ Escape(ar[i].TextFrom.trim()) + "</TextFrom>");
			for(int j=0;j<ar[i].TextTranslated.length;j++)
			{
			String sLang = xMSet.getLanguageCode(j,false);
			if( sLang == null ) continue;
			sLang = xMSet.xU.Capitalize(sLang.trim());
			if( sLang.compareToIgnoreCase("UNKNOWN")==0) continue;
			if( sLang.compareToIgnoreCase(OriginalLanguage) == 0 ) ar[i].TextTranslated[j] = ar[i].TextFrom;
			//
			if( ar[i].TextTranslated[j] == null ) continue;
			if( (ar[i].TextTranslated[j].trim().length() <= 0) && (sLang.trim().compareToIgnoreCase(OriginalLanguage)!=0) ) continue;
			//
			app.AppendIt("<TranslatedText_" + sLang + ">"+ Escape(ar[i].TextTranslated[j].trim()) + "</TranslatedText_" + sLang + ">");
			}
			app.AppendIt("</TextBundle>");
		}
	}
	//-----------------------------------------------------------------------
	public boolean createEmptyXMLFile(cmcTextObject[] ar_text , comicPage cpi , String Language)
	//-----------------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLLangFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == true ) {
			xMSet.xU.VerwijderBestand(XmlFLong);
		}
		if( xMSet.xU.IsBestand(XmlFLong) == true ) {
			do_error("Cannot remove [" + XmlFLong + "]");
			return false;
		}
		//
		setOriginalLanguage(Language);
		if ( !initLanguages() ) return false;
		//
		gpAppendStream aps = new gpAppendStream(XmlFLong,xMSet.getCodePageString());
		//
		//aps.AppendIt("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		aps.AppendIt(xMSet.getXMLEncodingHeaderLine());
		aps.AppendIt("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		aps.AppendIt("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		aps.AppendIt("<ComicPageText>");
		//
		aps.AppendIt("<ProcessHistory>");
	  	aps.AppendIt("<ProcessTimeStamp>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</ProcessTimeStamp>");
    	aps.AppendIt("</ProcessHistory>");
	    //
    	aps.AppendIt("<Languages>");
    	aps.AppendIt("<OriginalLanguage>" + xMSet.xU.Capitalize(OriginalLanguage) + "</OriginalLanguage>");
    	
    	if( ar_text.length > 0 ) {
    	 for(int j=0;j<ar_text[0].TextTranslated.length;j++)
		 {
    	 String sLang = xMSet.getLanguageCode(j,false);
    	 if( sLang == null ) continue;
    	 sLang = xMSet.xU.Capitalize(sLang).trim();
    	 if( sLang.trim().compareToIgnoreCase("UNKNOWN")==0) continue;
		 aps.AppendIt("<TranslationLanguage" + String.format("%02d", (j+1)) + ">"+ sLang + "</TranslationLanguage" + String.format("%02d", (j+1)) + ">");
		 }
    	}
    	
	    aps.AppendIt("</Languages>");
    	//
		aps.AppendIt("<ComicPageInfo>");
		aps.AppendIt("<File>");
		aps.AppendIt("<FileName>" + xMSet.xU.GetFileName(cpi.getFName()) + "</FileName>");
		aps.AppendIt("<FilePath>" + xMSet.xU.GetParent(cpi.getFName()) + "</FilePath>");
		aps.AppendIt("<FileSize>" + xMSet.xU.getFileSize(cpi.getFName())+ "</FileSize>");
		aps.AppendIt("</File>");
		aps.AppendIt("<Image>");
		aps.AppendIt("<CMXUID>" + cpi.getCMXUID() + "</CMXUID>" );
		aps.AppendIt("<UID>" + cpi.getUID() + "</UID>" );
		aps.AppendIt("</Image>");
		aps.AppendIt("</ComicPageInfo>");
		aps.AppendIt("<PageText>");
		//
		dump_textobj_array(aps,ar_text);
		//
		aps.AppendIt("</PageText>");
		aps.AppendIt("</ComicPageText>");
		aps.CloseAppendFile();
		//
		return true;
	}

	//-----------------------------------------------------------------------
	public cmcTextObject[] readTextObjects()
	//-----------------------------------------------------------------------
	{
	 	String XmlFLong = xMSet.getLanguageXML();  // $ROOT/Cache/TextEditor.xml
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "]");
			return null;
		}
		return readTextObjectsFromFile(XmlFLong);
	}
	
	//-----------------------------------------------------------------------
	public cmcTextObject[] readTextObjectsFromFile(String FName)
	//-----------------------------------------------------------------------
	{
		if( xMSet.xU.IsBestand(FName) == false ) {
			do_error("Cannot locate [" + FName + "]");
			return null;
		}
		String XmlFLong = FName;
		//
		if ( !initLanguages() ) return null;
		// What is the idx of textfrom
		int tix = 0;
		//
		currImageFileName=null;
		currImagePath=null;
		currImageUID=null;
		currImageCMXUID=null;
	  	cmcTextObject[] ar_tmp = null;
	    try {
			File inFile  = new File(XmlFLong);  // File to read from.
	       	//BufferedReader reader = new BufferedReader(new FileReader(inFile));
	  	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	int aantal=0;
	       	String sLijn=null;
          	while ((sLijn=reader.readLine()) != null) {
		      if( sLijn.indexOf("<TextBundle>") >= 0 ) aantal++;
		      
		      // global variables
		      String sVal=null;
		      if( sLijn.indexOf("<FileName>") >= 0 ) {
				sVal = xMSet.xU.extractXMLValue(sLijn,"FileName");
				currImageFileName = sVal.trim();
				continue;	 
			  }
		      if( sLijn.indexOf("<FilePath>") >= 0 ) {
				sVal = xMSet.xU.extractXMLValue(sLijn,"FilePath");
				currImagePath=sVal.trim();
				continue;	 
			  }
		      if( sLijn.indexOf("<CMXUID>") >= 0 ) {
					sVal = xMSet.xU.extractXMLValue(sLijn,"CMXUID");
					currImageCMXUID=sVal.trim();
					continue;	 
			  }
		      if( sLijn.indexOf("<UID>") >= 0 ) {
					sVal = xMSet.xU.extractXMLValue(sLijn,"UID");
					currImageUID=sVal.trim();
					continue;	 
			  }
		      
		   	}
	       	reader.close();
	       	// 2nd pass
	       	int teller=0;
	       	String sVal=null;
	       	ar_tmp = new cmcTextObject[aantal];
	       	reader = new BufferedReader(new FileReader(inFile));
	       	while ((sLijn=reader.readLine()) != null) {
	      	       		
	       	 if( sLijn.indexOf("<OriginalLanguage>") >= 0 ) {
						sVal = xMSet.xU.extractXMLValue(sLijn,"OriginalLanguage"); 
						setOriginalLanguage(sVal.trim().toUpperCase());
						initLanguages();
						tix = getOriginalLanguageIndex();
					    continue;	 
		   	 }
		   	 // textbundels	
	       	 if( sLijn.indexOf("<TextBundle>") >= 0 ) {
	       		cmcTextObject x = new cmcTextObject();
	       		x.BundelIdx = -1;
	       		x.UID = -1L;
	       		x.removed=false;
	    		ar_tmp[teller] = x;
	            teller++; 		
	       	 }
	       	 if( sLijn.indexOf("<TextBundleIdx>") >= 0 ) {
				sVal = xMSet.xU.extractXMLValue(sLijn,"TextBundleIdx"); 
				ar_tmp[teller-1].BundelIdx = xMSet.xU.NaarInt(sVal);
	       	    continue;	 
		   	 }
	       	 if( sLijn.indexOf("<TextBundleUID>") >= 0 ) {
				sVal = xMSet.xU.extractXMLValue(sLijn,"TextBundleUID"); 
				ar_tmp[teller-1].UID = xMSet.xU.NaarLong(sVal);
		        continue;	 
		   	 }
	       	 if( sLijn.indexOf("<TextBundleRemoved>") >= 0 ) {
	       		sVal = xMSet.xU.extractXMLValue(sLijn,"TextBundleRemoved");
	       		if( sVal.compareToIgnoreCase("TRUE")==0) ar_tmp[teller-1].removed=true; else ar_tmp[teller-1].removed=false;
	       	    continue;	 
		   	 }
	       	 if( sLijn.indexOf("<TextBundleChangeDate>") >= 0 ) {
	       		sVal = xMSet.xU.extractXMLValue(sLijn,"TextBundleChangeDate"); 
				ar_tmp[teller-1].changeDate = xMSet.xU.NaarLong(sVal);
	       	    continue;	 
		   	 }
	       	 if( sLijn.indexOf("<TextOCR>") >= 0 ) {
	       		sVal = xMSet.xU.extractXMLValue(sLijn,"TextOCR"); 
				ar_tmp[teller-1].TextOCR = UnEscape(sVal.trim());
	       	    continue;	 
		   	 }
	       	 if( sLijn.indexOf("<TextFrom>") >= 0 ) {
		       	sVal = xMSet.xU.extractXMLValue(sLijn,"TextFrom"); 
				ar_tmp[teller-1].TextFrom = UnEscape(sVal.trim());
		   	    continue;	 
		   	 }
		   	 if( sLijn.indexOf("<TranslatedText") >= 0 ) {
	       		extractTransText( sLijn , ar_tmp[teller-1]); 
	       	    continue;
	       	 }
		   	 
		   	 if( sLijn.indexOf("<TextConfidence>") >= 0 ) {
		   		   	sVal = xMSet.xU.extractXMLValue(sLijn,"TextConfidence");
			     	ar_tmp[teller-1].confidence = cenum.getTextConfidence(sVal.trim().toUpperCase());
					if( ar_tmp[teller-1].confidence == null ) ar_tmp[teller-1].confidence = cmcProcEnums.TextConfidence.UNKNOWN;
			   	    continue;	 
		   	 }
			
	       	}
	       	reader.close();
	       	if( teller != aantal ) {
	       		do_error("System error - count of TextBundle");
	       		ar_tmp=null;
	       	}
		}
		catch(Exception e) {
			do_error("Error reading [" + XmlFLong +"] [Message=" + e.getMessage() + "]");
			return null;
		}
		//
	    do_log(9,"Applying OriginalLanguage [" + OriginalLanguage + "] [Idx=" + tix + "]");
	    // debug
	    /*
	    for(int i=0;i<ar_tmp.length;i++)
	    {
	    	 logit(9,"TextFrom -> " + ar_tmp[i].TextFrom );
	    	 for(int j=0;j<ar_tmp[i].TextTranslated.length;j++)
	    	 {
	    		 if( j >= langList.length ) continue;
	    	     logit(9,"         " + langList[j] + " " + ar_tmp[i].TextTranslated[j]);	 
	    	 }
	    }
	    */
	    //
		return ar_tmp;
	}
	
	//-----------------------------------------------------------------------
	public boolean flushChangesToXML( cmcTextObject[] ar , String language)
	//-----------------------------------------------------------------------
	{
		//
		setOriginalLanguage(language);
		if ( !initLanguages() ) return false;
		// Move textfile to prev
	 	String XmlFLong = xMSet.getLanguageXML();  // $ROOT/Cache/TextEditor.xml
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "]");
			return false;
		}
		// merge the texteditor.xml with changes into new_texteditor.xml
		String FNew = xMSet.getLanguageNewXML(); // $ROOT/Cache/New_TextEditor.xml
		do_log(5,"New editor file -> " + FNew );
		try {
			File inFile  = new File(XmlFLong);  // File to read from.
	       	//BufferedReader reader = new BufferedReader(new FileReader(inFile));
	  	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	// Create target file - remove if it exists already (we areusing append)
	       	if( xMSet.xU.IsBestand(FNew) ) {
	       		xMSet.xU.VerwijderBestand(FNew);
	       		if( xMSet.xU.IsBestand(FNew) ) {
	       			do_error("Cannot remove [" + FNew + "] - existing ");
	       			reader.close();
	       			return false;
	       		}   		
	       	}
	    	gpAppendStream app = new gpAppendStream(FNew,xMSet.getCodePageString());
	       	//
	       	String sLijn=null;
          	while ((sLijn=reader.readLine()) != null) {
		        if( sLijn.trim().length() <= 0 ) continue;
		        // read until <PageText> and then just rewrite the entire ar_text
          		if( sLijn.indexOf("<PageText>") >= 0 ) {
         			app.AppendIt("<PageText>");
         	        dump_textobj_array(app,ar);
          			app.AppendIt("</PageText>");
          			app.AppendIt("</ComicPageText>");
          			break;
          		}
          		if( sLijn.indexOf("<!-- Start") >= 0 ) {
          			app.AppendIt(sLijn);
            		app.AppendIt("<!-- Last update : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
            		continue;
          		}	
                if( sLijn.indexOf("</ProcessHistory>") >= 0 ) {
          			app.AppendIt("<ProcessTimeStamp>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</ProcessTimeStamp>");
          			app.AppendIt(sLijn);
          			continue;
                }
        	    app.AppendIt(sLijn);
           	}
	       	reader.close();
	       	//
	       	app.CloseAppendFile();
		}
		catch(Exception e ) {
			do_error("Cannot read [" + XmlFLong + "]");
			return false;
		}
		//
		return true;
	}
	
	//-----------------------------------------------------------------------
	private boolean initLanguages()
	//-----------------------------------------------------------------------
	{
		String[] lijst = xMSet.getLanguageList();
		if( lijst.length <= 0 ) {
			do_error("Error initializing languages");
			return false;
		}
		//
		boolean found=false;
		for(int i=0;i<lijst.length;i++)
		{
			if( OriginalLanguage.trim().compareToIgnoreCase(lijst[i]) == 0) { found = true; break; }
		}
		if( !found ) {
		  do_error("Original language [" + OriginalLanguage + "] is not part of the supported languages. Adding it");
		  int aantal = (found == true) ? lijst.length : lijst.length + 1;
		  langList = new String[aantal];
		  for(int i=0;i<lijst.length;i++)
		  {
			String ss = lijst[i];
			langList[i] = ss;
		  }
		  if( ! found ) {
			String ss = OriginalLanguage;
			langList[langList.length-1] = ss;
		  }
	      xMSet.setLanguageList(langList);
	      langList = null;
	    }
	    langList = xMSet.getLanguageList();
	    cmcTextObject x = new cmcTextObject();
	    if( langList.length > x.TextTranslated.length ) {
	      do_error("There are more languages than cmcTextObject.TextTranslated can hold.");	
	    }
	    //
	    return true;
	}
	
	//-----------------------------------------------------------------------
	private void extractTransText( String sIn , cmcTextObject x )
	//-----------------------------------------------------------------------
	{
		// <Translatedtext_{language}>  etc
		String ups = sIn.toUpperCase().trim();
		String sTag = "";
		int idx = -1;
		for(int i=0;i<langList.length;i++)
		{
			sTag = "TranslatedText_" + langList[i].trim();
			if( ups.indexOf( ("<" + sTag.trim() + ">").toUpperCase()) < 0 ) continue;
			idx = i;
			break;
		}
		if( (idx < 0) || (idx >= x.TextTranslated.length) ) {
			do_error("Cannot determine language in [" + sIn + "]");
			return;
		}
  		String sVal = xMSet.xU.extractXMLValue(sIn,sTag); 
		if( sVal == null ) return;
		x.TextTranslated[idx] = UnEscape(sVal);
	}
	
	//-----------------------------------------------------------------------
	private int getOriginalLanguageIndex()
	//-----------------------------------------------------------------------
	{
		int tix = xMSet.xU.getIdxFromList( langList , OriginalLanguage );
		cmcTextObject xy = new cmcTextObject();
		if( (tix < 0) || (tix > xy.TextTranslated.length) )  {
			do_error("Cannot determine index of OriginalLanguage [" + OriginalLanguage + "]");
			tix = 0;  // default it
		}
		xy = null;
   	    return tix;
	}
	
	//-----------------------------------------------------------------------
	private void setOriginalLanguage(String sIn)
	//-----------------------------------------------------------------------
	{
		String s2 = sIn;
		if( s2 == null ) s2 = DEFAULTLANG;
		s2 = s2.trim().toUpperCase();
		if( s2.length() <= 0 ) s2 = DEFAULTLANG;
		OriginalLanguage = s2;
	}
	
	//-----------------------------------------------------------------------
	public String getOriginalLanguageCode()
	//-----------------------------------------------------------------------
	{
		return OriginalLanguage;
	}
	
	public String getUID()
	{
		return currImageUID;
	}
	public String getCMXUID()
	{
		return currImageCMXUID;
	}
	public String getImageFileName()
	{
		return currImageFileName;
	}
	public String getImagePath()
	{
		return currImagePath;
	}
	public String[] getLanguagList()
	{
		return langList;
	}
}
