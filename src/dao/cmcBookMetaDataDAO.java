package dao;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;
import generalpurpose.gpPrintStream;
import generalpurpose.gpUnZipFileList;

public class cmcBookMetaDataDAO {
	
	cmcProcSettings xMSet=null;
    cmcProcEnums cenum = null;
    logLiason logger=null;	
   
    private String FMetaDataFNaam=null;
    private boolean isEmpty=true;   // FALSE => metadatahas been read
    //
	private String CMXUID;
	private String UID;  // copied from the cPage
	private String FolderName;
	private String FileName;
	private String isbn;
	private String SeriesName;
	private int    SeriesSequence;
	private String BookName;
	private int    PageNumber;
	private String WriterName;
	private String PencillerName;
	private String ColourerName;
	private String Language;
	private String Comment;
	private String Metrics;
	private cmcProcEnums.ColourSchema ColourTipe;
	private cmcProcEnums.ColourSchema ColourTipeDetected;
	private cmcProcEnums.ProximityTolerance ProximityTipe;
	private cmcProcEnums.BinarizeClassificationMethod BinarizeClassificationTipe;
	private cmcProcEnums.ClusterClassificationMethod ClusterClassificationTipe;
    private cmcProcEnums.OCR_CURATION curation;
    private cmcProcEnums.CroppingType cropping;
    private int currentDPI= -1;
    private boolean hasbeenmodified=false;
    private String prevCreatedLine=null;
    
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
	public cmcBookMetaDataDAO(cmcProcSettings iM , String imgName, comicPage cpi , logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = iM;
		logger = ilog;
		cenum = new cmcProcEnums(xMSet);
		initializeVars();
		if( imgName != null) {
			initialize(imgName,cpi);
		    readMetaDataFile(null);
	    }
	}

	//-----------------------------------------------------------------------
	private void initializeVars()
	//-----------------------------------------------------------------------
	{
		isEmpty=true;
		FMetaDataFNaam = null;
		CMXUID="";
		isbn="";
		FileName="";
		SeriesName="";
		SeriesSequence=0;
		BookName="";
		PageNumber=0;
		WriterName="";
		PencillerName="";
		ColourerName="";
		Language=xMSet.getPreferredLanguageLong();
		Comment="";
		FolderName = "";
		FileName = "";
		Metrics="";
		ColourTipe = cmcProcEnums.ColourSchema.UNKNOWN;
		ColourTipeDetected = cmcProcEnums.ColourSchema.UNKNOWN;
		ProximityTipe = xMSet.getDefaultProxTol();
		BinarizeClassificationTipe = xMSet.getDefaultBinarizeClassificationMethod();
		ClusterClassificationTipe  = xMSet.getDefaultClusterClassificationMethod();
		curation = cmcProcEnums.OCR_CURATION.USE_IMAGE_DPI;
		cropping = cmcProcEnums.CroppingType.CROP_IMAGE;
		hasbeenmodified=false;
		prevCreatedLine=null;
	}
	
	//-----------------------------------------------------------------------
	public void calculateAndSetUIDs(String imgName,comicPage cpi)
	//-----------------------------------------------------------------------
	{
		CMXUID = xMSet.getCleansedShortFileName(imgName,"cmcBookMetadata.initialize");
		if( cpi != null ) UID=cpi.getUID();
	}

	//-----------------------------------------------------------------------
	public void calculateAndSetPageNumber(String imgName)
	//-----------------------------------------------------------------------
	{
		try {  // read the last sequence of numerics on a filename
		  if( imgName == null ) return;
		  String FName = xMSet.xU.getFolderOrFileName(imgName);
		  // remove suffix
		  int idx = FName.lastIndexOf(".");
		  if( idx < 0 ) return;
		  FName = FName.substring(0,idx).toUpperCase();
          //do_error( "---> " + FName );
		  char[] cbuf = FName.toCharArray();
		  idx = -1;
		  for(int i=cbuf.length-1;i>0;i--) 
		  {	
		    if( (cbuf[i]>='0') && (cbuf[i]<='9') ) {
                //do_error( "--> " + cbuf[i] + " " + idx );
		    	idx = i;
		    	continue;
		    }
		    if( idx < 0 ) continue; // keep  on going until a numer
		    break; 	
		  }
		  if( idx < 0 ) return;
		  // keep numbers starting from idx
		  String sNum = "";
		  for(int i=idx;i<cbuf.length;i++)
		  {
			  if( (cbuf[i]>='0') && (cbuf[i]<='9') ) sNum += cbuf[i];
		  }
          //do_error("->" + sNum);	
          idx = xMSet.xU.NaarInt(sNum);
          if( idx < 0 ) return;
          do_log(5,"Pagenumber guessed to be [" + idx + "]");
          PageNumber = idx;
		}
		catch(Exception e) {
			return;
		}
	}

	//-----------------------------------------------------------------------
	private void initialize(String imgName,comicPage cpi)
	//-----------------------------------------------------------------------
	{
		if( imgName == null ) return;
		//
		if( xMSet.xU.IsBestand( imgName ) == false ) return;
		FolderName = xMSet.xU.GetParent(imgName);
		FileName = xMSet.xU.GetFileName(imgName);
		FMetaDataFNaam = xMSet.getMetaDataFileName();
		//
		calculateAndSetUIDs(imgName , cpi);
		calculateAndSetPageNumber(imgName);
		//
		if( cpi == null ) return;
		if( cpi.getIsMonoChrome() ) ColourTipe = cmcProcEnums.ColourSchema.MONOCHROME; 
		else {
			if( cpi.getIsGrayScale() ) ColourTipe = cmcProcEnums.ColourSchema.GRAYSCALE;
			                 else ColourTipe = cmcProcEnums.ColourSchema.COLOUR;
		}
		ColourTipeDetected = ColourTipe;
		//
		currentDPI = cpi.getPhysicalWidthDPI();
		Metrics  = "[Pixels=" + cpi.getUncroppedWidth() + " x " + cpi.getUncroppedHeigth() + "]";
		Metrics += ( currentDPI > 0 ) ? " [DPI=" + currentDPI + "]" : " [DPI=Unknown]";
		Metrics += (cpi != null) ? " [Size=" + xMSet.xU.dotter(cpi.getFileSize()) + " Bytes]": "";
	}
	
	// OCR and GraphEdit read the metadata from Cache dir  ; extact and image from Archive
	//-----------------------------------------------------------------------
	public void readMetaDataFile(String overrule)
	//-----------------------------------------------------------------------
	{
		if( overrule != null ) {   // overrule reads file from cache
			//do_log(5,"Reading metadata from [" + overrule + "]" );
			if( xMSet.xU.IsBestand(overrule) == false ) {
				do_error("Read : Zmetadata file name is [" + overrule + "]");
			    return;			
			}
			FMetaDataFNaam = overrule;
		}
		if( FMetaDataFNaam == null ) {
			if( overrule == null ) {
			do_error("Read : Empty metadata file name");
			return;
			}
		}
		// Zoek de metafile en extraheer die desnoods
		if( xMSet.xU.IsBestand(FMetaDataFNaam) == false ) {
			// Look in ZIP file
			String FZipName = xMSet.getReportZipName();
			if( xMSet.xU.IsBestand( FZipName ) == false ) {
			  do_error("Cannot find matadatafile [" + FMetaDataFNaam + "] nor archive file [" + FZipName + "]");
			  return;
			}
			// unzip
			gpUnZipFileList uzip = new gpUnZipFileList( FZipName , xMSet.getTempDir() , "zMetaData_" , logger);
			uzip=null;
			int idx = FMetaDataFNaam.lastIndexOf("zMetaData_");
			String FNaamKort = FMetaDataFNaam.substring(idx);
			String FTempName = xMSet.getTempDir() + xMSet.xU.ctSlash + FNaamKort;
			if( xMSet.xU.IsBestand( FTempName ) == false ) {
				 do_error("Could not extract configurationfile [" + FMetaDataFNaam + "] from archive file [" + FZipName + "] in [" + FTempName + "]");
				 return;
			}
			// moven
			try  {
			xMSet.xU.copyFile( FTempName , FMetaDataFNaam );
			}
			catch(Exception e) {
				do_error("Could not move [" +  FTempName + "] to [" + FMetaDataFNaam + "]");
				return;
			}
			xMSet.xU.VerwijderBestand( FTempName );
		}
		//
		do_log(5,"Reading metadata from [" + FMetaDataFNaam + "]" );
		//		
		String sXML = xMSet.xU.ReadContentFromFile(FMetaDataFNaam,1000,xMSet.getCodePageString());
		int aantal = xMSet.xU.TelDelims(sXML,'\n');
		for(int i=0;i<aantal;i++)
		{
			String sLijn = xMSet.xU.GetVeld(sXML,(i+1),'\n');
			if( sLijn == null ) continue;
			//
			if( sLijn.trim().startsWith("<!-- Created")) {
				prevCreatedLine=sLijn;
			}
			//
			String sVal = "";
			sVal = xMSet.xU.extractXMLValue(sLijn,"CMXUID"); if ( sVal != null ) { CMXUID = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"UID"); if ( sVal != null ) { UID = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"ISBN"); if ( sVal != null ) { isbn = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"Series"); if ( sVal != null ) { SeriesName = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"SeriesSequence"); if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( k > 0 ) SeriesSequence = k;	}
			sVal = xMSet.xU.extractXMLValue(sLijn,"BookName"); if ( sVal != null ) { BookName = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"PageNumber"); if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( k > 0 ) PageNumber = k;	}
			sVal = xMSet.xU.extractXMLValue(sLijn,"WriterName"); if ( sVal != null ) { WriterName = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"PencillerName"); if ( sVal != null ) { PencillerName = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"ColourerName"); if ( sVal != null ) { ColourerName = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"Language"); if ( sVal != null ) { if( xMSet.isValidLanguage(sVal) ) Language=sVal.trim().toUpperCase(); };
			sVal = xMSet.xU.extractXMLValue(sLijn,"Comment"); if ( sVal != null ) { Comment = sVal; }
			sVal = xMSet.xU.extractXMLValue(sLijn,"ProximityTolerance"); if ( sVal != null ) { cmcProcEnums.ProximityTolerance x = cenum.getProximityTipe(sVal);  if ( x != null ) ProximityTipe = x;}
			sVal = xMSet.xU.extractXMLValue(sLijn,"BinarizeClassificationMethod"); if ( sVal != null ) { cmcProcEnums.BinarizeClassificationMethod x = cenum.getBinarizeClassificationTipe(sVal);  if ( x != null ) BinarizeClassificationTipe = x;}
			sVal = xMSet.xU.extractXMLValue(sLijn,"ClusterClassificationMethod"); if ( sVal != null ) { cmcProcEnums.ClusterClassificationMethod x = cenum.getClusterClassificationTipe(sVal);  if ( x != null ) ClusterClassificationTipe = x;}
			sVal = xMSet.xU.extractXMLValue(sLijn,"ColourSchema"); if ( sVal != null ) { cmcProcEnums.ColourSchema x = cenum.getColourSchema(sVal);  if ( x != null ) ColourTipe = x;}
			sVal = xMSet.xU.extractXMLValue(sLijn,"TesseractCuration"); if ( sVal != null ) { cmcProcEnums.OCR_CURATION x = cenum.getOCRCuration(sVal);  if ( x != null ) curation = x;}
			sVal = xMSet.xU.extractXMLValue(sLijn,"CropImage"); if ( sVal != null ) { cmcProcEnums.CroppingType x = cenum.getCroppingType(sVal);  if ( x != null ) cropping = x;}
			
			//
		}
		isEmpty=false;
	}
	
	//-----------------------------------------------------------------------
	public void setMetadataFileName(String s )
	//-----------------------------------------------------------------------
	{
		FMetaDataFNaam = s;
	}
	
	//-----------------------------------------------------------------------
	public void writeMetaData()
	//-----------------------------------------------------------------------
	{
		if( FMetaDataFNaam == null ) {
			do_error("WRITE : Empty metadata file name");
			return;
		}
		//
		gpPrintStream pout = new gpPrintStream( FMetaDataFNaam , xMSet.getCodePageString());
		//
		//pout.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		pout.println(xMSet.getXMLEncodingHeaderLine());
		pout.println("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		if( prevCreatedLine == null )
		pout.println("<!-- Created     : " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		else
		pout.println(prevCreatedLine);	
		//pout.println("<!-- " + FMetaDataFNaam + " -->");
		if( hasbeenmodified )
		pout.println("<!-- Changed     : " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		pout.println("<PageMetadata>");
		//
		pout.println("<FolderName>" + FolderName+ "</FolderName>");
		pout.println("<FileName>" + FileName + "</FileName>");
		pout.println("<UID>" + UID + "</UID>");
		pout.println("<CMXUID>" + CMXUID + "</CMXUID>");
		pout.println("<ISBN>" + isbn + "</ISBN>");
		pout.println("<Series>" + SeriesName + "</Series>");
		pout.println("<SeriesSequence>" + SeriesSequence + "</SeriesSequence>");
		pout.println("<BookName>" + BookName + "</BookName>");
		pout.println("<PageNumber>" + PageNumber + "</PageNumber>");
		pout.println("<WriterName>" + WriterName + "</WriterName>");
		pout.println("<PencillerName>" + PencillerName + "</PencillerName>");
		pout.println("<ColourerName>" + ColourerName+ "</ColourerName>");
		pout.println("<Language>" + Language + "</Language>");
		pout.println("<Comment>" + Comment + "</Comment>");
		pout.println("<ColourSchema>" + (""+ColourTipe).toLowerCase() + "</ColourSchema>");
		pout.println("<ProximityTolerance>" + ProximityTipe + "</ProximityTolerance>");
		pout.println("<BinarizeClassificationMethod>" + BinarizeClassificationTipe + "</BinarizeClassificationMethod>");
		pout.println("<ClusterClassificationMethod>" + ClusterClassificationTipe + "</ClusterClassificationMethod>");
		pout.println("<TesseractCuration>" + curation + "</TesseractCuration>");
		pout.println("<CropImage>" + cropping + "</CropImage>");
		
		//
		pout.println("</PageMetadata>");
		pout.close();
		
		// 2de deel  false positives, true positives, false negatives en true negatives
		String sFirst = "true";
		if( ColourTipe != ColourTipeDetected ) sFirst = "false";
		String sSecond = "positive";
		if( ColourTipeDetected != cmcProcEnums.ColourSchema.MONOCHROME ) sSecond = "negative"; 
		String sQual = sFirst + "-" + sSecond;
		do_error( "===>" + sQual);
		// update
		xMSet.setMonochromedetectionStatus(sQual);
        xMSet.setColourSchema(ColourTipe);
        
        do_log(5,"Metadata written to [" + FMetaDataFNaam + "]");
    }
	
	
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	// getters en setters
	public boolean isEmpty()
	{
		return isEmpty;
	}
	public String getCMXUID()
	{
		return CMXUID;
	}
	public String getUID()
	{
		return UID;
	}
	public String getISBN()
	{
		return isbn;
	}
	public String getFileName()
	{
		return FileName;
	}
	public String getFolderName()
	{
		return FolderName;
	}
	public String getSeriesName()
	{
		return SeriesName;
	}
	public int getSeriesSequence()
	{
		return SeriesSequence;
	}
	public String getBookName()
	{
		return BookName;
	}
	public int getPageNumber()
	{
		return PageNumber;
	}
	public String getWriterName()
	{
		return WriterName;
	}
	public String getPencillerName()
	{
		return PencillerName;
	}
	public String getColourerName()
	{
		return ColourerName;
	}
	public String getLanguage()
	{
		return Language;
	}
	public String getComment()
	{
		return Comment;
	}
	public String getProximityTypeString()
	{
		return ProximityTipe.toString();
	}
	public String getBinarizeClassificationTypeString()
	{
		return BinarizeClassificationTipe.toString();
	}
	public cmcProcEnums.BinarizeClassificationMethod getBinarizeClassificationTypeRaw()
	{
		return BinarizeClassificationTipe;
	}
	public String getClusterClassificationTypeString()
	{
		return ClusterClassificationTipe.toString();
	}
	public cmcProcEnums.ClusterClassificationMethod getClusterClassificationTypeRaw()
	{
		return ClusterClassificationTipe;
	}
	public String getColourSchema()
	{
		return ColourTipe.toString();
	}
	public cmcProcEnums.OCR_CURATION getOCRCuration()
	{
		return curation;
	}
	public cmcProcEnums.CroppingType getCroppingType()
	{
		return cropping;
	}

	public boolean getHasBeenModified()
	{
		return hasbeenmodified;
	}
	//
	
	public void setCMXUID(String s)
	{
		if( getCMXUID().compareTo(s) != 0 ) hasbeenmodified=true;
		CMXUID=s;
	}
	public void setISBN(String s)
	{
		if( getISBN().compareTo(s) != 0 ) hasbeenmodified=true;
		isbn=s;
	}
	public void setSeriesName(String s)
	{
		if( getSeriesName().compareTo(s) != 0 ) hasbeenmodified=true;
		SeriesName=s;
	}
	public void setSeriesSequence(String s)
	{
		int i = xMSet.xU.NaarInt(s);
		if( i < 0 ) i = 0;
		if( getSeriesSequence() != i ) hasbeenmodified=true;
		SeriesSequence=i;
	}
	public void setBookName(String s)
	{
		if( getBookName().compareTo(s) != 0 ) hasbeenmodified=true;
		BookName=s;
	}
	public void setPageNumber(String s)
	{
		int i = xMSet.xU.NaarInt(s);
		if( i < 0 ) i = 0;
		if( getPageNumber() != i ) hasbeenmodified=true;
		PageNumber=i;
	}
	public void setWriterName(String s)
	{
		if( getWriterName().compareTo(s) != 0 ) hasbeenmodified=true;
		WriterName=s;
	}
	public void setPencillerName(String s)
	{
		if( getPencillerName().compareTo(s) != 0 ) hasbeenmodified=true;
		PencillerName=s;
	}
	public void setColourerName(String s)
	{
		if( getColourerName().compareTo(s) != 0 ) hasbeenmodified=true;
		ColourerName=s;
	}
	public void setLanguage(String s)
	{
		if( xMSet.isValidLanguage(s) ) {
			if( getLanguage().compareToIgnoreCase(s) != 0 ) hasbeenmodified=true;
			Language=s.trim().toUpperCase();
		}
	}
	public void setComment(String s)
	{
		if( getComment().compareTo(s) != 0 ) hasbeenmodified=true;
		Comment=s;
	}
	
	//-----------------------------------------------------------------------
	public void setProximityType(String s)
	//-----------------------------------------------------------------------
	{
        cmcProcEnums.ProximityTolerance x = cenum.getProximityTipe(s);
        if( x == null ) {
        	do_error("System error : cannot find " + s + " in ProximityTolerance");
        	return;
        }
        if( ProximityTipe != x ) hasbeenmodified=true;
        ProximityTipe = x; 
	}
	
	//-----------------------------------------------------------------------
	public void setBinarizeClassificationType(String s)
	//-----------------------------------------------------------------------
	{
		    cmcProcEnums.BinarizeClassificationMethod x = cenum.getBinarizeClassificationTipe(s);
	        if( x == null ) {
	        	do_error("System error : cannot find " + s + " in BinarizeClassificationMethod");
	        	return;
	        }
	        if( BinarizeClassificationTipe != x ) hasbeenmodified=true;
			BinarizeClassificationTipe = x; 
	}
	
	//-----------------------------------------------------------------------
	public void setClusterClassificationType(String s)
	//-----------------------------------------------------------------------
	{
		    cmcProcEnums.ClusterClassificationMethod x = cenum.getClusterClassificationTipe(s);
	        if( x == null ) {
	        	do_error("System error : cannot find " + s + " in ClusterClassificationMethod");
	        	return;
	        }
	        if( ClusterClassificationTipe != x ) hasbeenmodified = true;
	    	ClusterClassificationTipe = x;
	}
	
	//-----------------------------------------------------------------------
	public void setColourSchema(String s)
	//-----------------------------------------------------------------------
	{
		    cmcProcEnums.ColourSchema x = cenum.getColourSchema(s);
	        if( x == null ) {
	        	do_error("System error : cannot find " + s + " in Colourschema");
	        	return;
	        }
	        if( ColourTipe != x ) hasbeenmodified=true;
	        ColourTipe = x;
	}

	//-----------------------------------------------------------------------
	public void setOCRCuration(String s)
	//-----------------------------------------------------------------------
	{
		    cmcProcEnums.OCR_CURATION x = cenum.getOCRCuration(s);
	        if( x == null ) {
	        	do_error("System error : cannot find " + s + " in OCR Curation");
	        	return;
	        }
	        if( curation != x ) hasbeenmodified=true;
	    	curation = x;
	}

	//-----------------------------------------------------------------------
	public void setCroppingType(String s)
	//-----------------------------------------------------------------------
	{
			    cmcProcEnums.CroppingType x = cenum.getCroppingType(s);
		        if( x == null ) {
		        	do_error("System error : cannot find " + s + " in CroppingType");
		        	return;
		        }
		        if( cropping != x ) hasbeenmodified=true;
		    	cropping = x;
	}
	//-----------------------------------------------------------------------
	public String getMetrics()
	//-----------------------------------------------------------------------
	{
		return Metrics;
	}
	
	//-----------------------------------------------------------------------
	public boolean gotValidDPI()
	//-----------------------------------------------------------------------
	{
		return (currentDPI > 40) ? true : false;
	}
	
	//-----------------------------------------------------------------------
	public boolean flushChangesToArchive()
	//-----------------------------------------------------------------------
	{
		// chec presence of file and ZIP file
		// this must be in the archive folder
		String ZipFileName = xMSet.getReportZipName();
		do_log(5,"Checking presence of metadatafile [" + FMetaDataFNaam + "] and Archive [" + ZipFileName + "]");
        //
		if( xMSet.xU.IsBestand(FMetaDataFNaam) == false ) {
			do_error("Cannot locate [" + FMetaDataFNaam + "]");
			return false;
		}
		// the ZIP might be missing if this is the first time the image is selected
		if( xMSet.xU.IsBestand(ZipFileName) == false ) {
			do_log(1,"Cannot locate Archive [" + FMetaDataFNaam + "]. Rezip cannot be performed");
			return true;
		}
		// instantiate the archice dao and set trigger file
		cmcArchiveDAO arc = new cmcArchiveDAO(xMSet,logger);
		arc.setMetaDataSourceFileName(FMetaDataFNaam);
		boolean ib =arc.reZipAllFiles(ZipFileName);
		arc=null;
		//
		return ib;
	}
}
