package cbrTekStraktorModel;

import java.awt.Font;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import cbrTekStraktorProject.cbrTekStraktorProjectManager;
import cbrTekStraktorProject.cmcProjectWrapper;
import logger.logLiason;
import monitor.cmcMonitorDataObjectManager;
import generalpurpose.gpLanguage;
import generalpurpose.gpPrintStream;
import generalpurpose.gpUtils;


public class cmcProcSettings {
	
	public boolean TENSOR_SIMULATOR = false;
	
	logLiason logger = null;
	cbrTekStraktorProjectManager projman = null;
	gpLanguage langObj=null;
	cmcMonitorDataObjectManager moma = null;
	
	private String OrigImageLongFileName=null;
	
    private int optimalThreshold=-1;
    private String preferredImageFormat = "png";   // do not change PNG is lossless 
    private String monochromeDetectionSatus="";
    private long startedAt = System.currentTimeMillis();
    private long startTmeNano = System.nanoTime();
	private long longCounter=0L;
	 
    public Rectangle mainframe = new Rectangle(50,50,1000,800);
    public Rectangle quickframe = new Rectangle(50,50,1000,400);
    public Rectangle monitframe = new Rectangle(100,100,800,400);
    
    private String[] languageLijst = null;
    
	public gpUtils xU=null;
	private boolean isActive=false;
	private boolean dialogCompletedOk=false;
	private boolean requestProjectSwap=false;
	private boolean metadatahasbeenmodified=false;
	//
	private cmcProcEnums.ProximityTolerance           defaultProxTol                      = cmcProcEnums.ProximityTolerance.TIGHT;
    private cmcProcEnums.BinarizeClassificationMethod binarizeDefaultClassificationMethod = cmcProcEnums.BinarizeClassificationMethod.SLOW_SAUVOLA;
    private cmcProcEnums.ClusterClassificationMethod  clusterDefaultClassificationMethod  = cmcProcEnums.ClusterClassificationMethod.AUTOMATIC;
    //
	private cmcProcEnums.ProximityTolerance           ProxTol                      = defaultProxTol;
    private cmcProcEnums.BinarizeClassificationMethod binarizeClassificationMethod = binarizeDefaultClassificationMethod;
    private cmcProcEnums.ClusterClassificationMethod  clusterClassificationMethod  = clusterDefaultClassificationMethod;
    private cmcProcEnums.ColourSchema ColourTipe = cmcProcEnums.ColourSchema.UNKNOWN;
    private cmcProcEnums.QUICKEDITOPTIONS quickEditTipe = cmcProcEnums.QUICKEDITOPTIONS.TEXT_AREAS;
	//
	private String InitError = "";
	
	private String[] arStack = new String[5];
	
	private int QuickEditRequestedRow = -1;
	private String exportFileName = null;
	private String recentImageDir = null;
	private String recentSaveDir = null;
	private String recentArchiveDir = null;
	private String currentArchiveFileName=null;
	private boolean useMonoChromeInDialogs=false;
	private String OCRSummaryResult = null;
    private boolean performTensorFlowPostProcess = false;
    
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
	
    public void setLogger(logLiason ilog)
    {
    	logger = ilog;
    }
	//---------------------------------------------------------------------------------
	public cmcProcSettings(String[] args )
	//---------------------------------------------------------------------------------
	{
		logger = null;
		xU = new gpUtils();
		projman = new cbrTekStraktorProjectManager(this,logger);
		langObj = new gpLanguage();
		moma = new cmcMonitorDataObjectManager(this,logger);
		isActive = initialize(args);
 	}
	
	//---------------------------------------------------------------------------------
	public boolean isActive()
	//---------------------------------------------------------------------------------
	{
		return isActive;
	}
	//---------------------------------------------------------------------------------
	private void ErrIt(String sIn)
	//---------------------------------------------------------------------------------
	{
	  do_error("Error :" + sIn);
	  InitError += xU.ctEOL + sIn;
	}
	//---------------------------------------------------------------------------------
	public String getApplicDesc()
	//---------------------------------------------------------------------------------
	{
		return cmcProcConstants.Application + " " + cmcProcConstants.Version + " (" + cmcProcConstants.Build + ")";
	}
	//---------------------------------------------------------------------------------
	public String getApplicationName()
	//---------------------------------------------------------------------------------
	{
		return cmcProcConstants.Application;
	}
	//---------------------------------------------------------------------------------
	public void setOptimalThreshold(int t)
	//---------------------------------------------------------------------------------
	{
		optimalThreshold=t;
	}
	//---------------------------------------------------------------------------------
	public int getOptimalThreshold()
	//---------------------------------------------------------------------------------
	{
		return optimalThreshold;
	}
	//---------------------------------------------------------------------------------
	public int getLogLevel()
	//---------------------------------------------------------------------------------
	{
		return projman.getLogLevel();
	}
	//---------------------------------------------------------------------------------
	public int getMaxThreads()
	//---------------------------------------------------------------------------------
	{
		return projman.getMaxThreads();
	}
	//---------------------------------------------------------------------------------
	public String getPreferredFontName()
	//---------------------------------------------------------------------------------
	{
		return projman.getPreferredFontName();
	}
	//---------------------------------------------------------------------------------
	public int getPreferredFontSize()
	//---------------------------------------------------------------------------------
	{
		return projman.getPreferredFontSize();
	}
	//---------------------------------------------------------------------------------
	public Font getPreferredFont()
	//---------------------------------------------------------------------------------
	{
		return projman.getPreferredFont();
	}
	//---------------------------------------------------------------------------------
	public String getDateFormat()
	//---------------------------------------------------------------------------------
	{
		return projman.getDateformat();
	}
	//---------------------------------------------------------------------------------
	public boolean initialize(String[] args)
	//---------------------------------------------------------------------------------
	{
		boolean isOK=true;
		//
		isOK = projman.launchProject(args);
		if( isOK == false ) {
			
		}
		if( isOK == false ) {
			ErrIt( projman.getInitializationErrors() );
			return false;
		}
		//
	    recentImageDir   = projman.getRootDir();
	    recentArchiveDir = projman.getRootDir();
	    recentSaveDir    = projman.getRootDir();
	     //
	    maakLanguages();
	    //
	    getProperties();
	    //
		return isOK;
	}

	//-----------------------------------------------------------------------
	public boolean fixFoldersAndFiles()
	//-----------------------------------------------------------------------
	{
		return projman.fixit();
	}
	
	//-----------------------------------------------------------------------
	public String getInitError()
	//-----------------------------------------------------------------------
	{
		return InitError.trim();
	}
	//-----------------------------------------------------------------------
	public void setDialogCompleteState(boolean b)
	//-----------------------------------------------------------------------
	{
		this.dialogCompletedOk = b;
	}
	//-----------------------------------------------------------------------
	public boolean getDialogCompleteStatus()
	//-----------------------------------------------------------------------
	{
		return this.dialogCompletedOk;
	}
	//-----------------------------------------------------------------------
	public void setMonochromedetectionStatus(String s)
	//-----------------------------------------------------------------------
	{
		monochromeDetectionSatus = s;
	}
	//-----------------------------------------------------------------------
	public String getMonochromedetectionStatus()
	//-----------------------------------------------------------------------
	{
		return monochromeDetectionSatus;
	}
	//-----------------------------------------------------------------------
	public void setColourSchema(cmcProcEnums.ColourSchema i)
	//-----------------------------------------------------------------------
	{
		ColourTipe = i;
		//do_log(9,"xMSet -> [ColourTipe=" + i + "]");
	}
	//-----------------------------------------------------------------------
	public cmcProcEnums.ColourSchema getColourSchema()
	//-----------------------------------------------------------------------
	{
		return ColourTipe;
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ProximityTolerance getDefaultProxTol()
	//---------------------------------------------------------------------------------
	{
			return defaultProxTol;
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ProximityTolerance getProxTol()
	//---------------------------------------------------------------------------------
	{
		return ProxTol;
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ClusterClassificationMethod getDefaultClusterClassificationMethod()
	//---------------------------------------------------------------------------------
	{
			return clusterDefaultClassificationMethod;
	}
	
	public cmcMonitorDataObjectManager getmoma()
	{
		return moma;
	}
	public void setTensorFlowPostProcessIndicator(boolean ib)
	{
		this.performTensorFlowPostProcess = ib;
	}
	public boolean getTensorFlowPostProcessIndicator()
	{
		return performTensorFlowPostProcess;
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.BinarizeClassificationMethod getDefaultBinarizeClassificationMethod()
	//---------------------------------------------------------------------------------
	{
			return binarizeDefaultClassificationMethod;
	}
	//---------------------------------------------------------------------------------
	public boolean isValidLanguage(String s)
	//---------------------------------------------------------------------------------
	{
	  	for(int i=0;i<languageLijst.length;i++) 
	  	{
	  		if( s.trim().compareToIgnoreCase( languageLijst[i].trim() ) == 0 ) return true;
	  	}
	  	return false;
	}
	
	public boolean hasMetadataBeenModified()
	{
		return metadatahasbeenmodified;
	}
	public void setMetadataHasBeenModified(boolean ib)
	{
		metadatahasbeenmodified=ib;
	}
	// Directories
	//---------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------
	public String getRootDir()
	{
		return projman.getRootDir();
	}
	public String getCorpusDir()
	{
		return projman.getCorpusDir();
	}
	public String getTempDir()
	{
		return projman.getTempDir();
	}
	public String getStatDir()
	{
		return projman.getStatDir();
	}
	public String getOCRDir()
	{
		return projman.getOCRDir();
	}
	public String getArchiveDir()
	{
		return projman.getArchiveDir();
	}
	public String getReportImageDir()
	{
		return projman.getReportImageDir();
	}
	public String getReportHTMLDir()
	{
		return projman.getReportHTMLDir();
	}
	public String getCacheDir()
	{
		return projman.getCacheDir();
	}
	public String getProjectDescription()
	{
		return projman.getProjectDescription();
	}
	public String getProjectName()
	{
		return projman.getProjectName();
	}
	public cmcProjectWrapper getCurrentProjectWrapper()
	{
		return projman.getCurrentProject();
	}
	public boolean switchProject(String ProjectFolderName)
	{
		return projman.switchProject(ProjectFolderName);
	}
	public cmcProcEnums.BROWSER getBrowser()
	{
		return projman.getBrowser();
	}
	public String getTextReportName()
	{
		return projman.getOutputDir() + xU.ctSlash + "TextReport.txt";
	}
	public String getExportFileName()
	{
		return exportFileName;
	}
	// getters/setters FileNames
	public void setExportFileName(String s)
	{
		exportFileName=s;
	}
	//---------------------------------------------------------------------------------
	public void setOrigImageLongFileName(String sF)
	//---------------------------------------------------------------------------------
	{
		OrigImageLongFileName = sF;
	}
	public String getOrigImageLongFileName()
	{
		return OrigImageLongFileName;
	}
	public String getOverallStatFileName()
	{
		String sDir = this.getCorpusDir();
		return sDir + xU.ctSlash + "Stats" + xU.ctSlash + "AllStat.txt";
	}
	public String getPropertiesFile()
	{
		String sDir = getRootDir();
		return sDir + xU.ctSlash + "properties.txt";
	}
	
	public String getOriginalImagePixelDumpFileName()
	{
		return getTempDir() + xU.ctSlash + "OrignalImagePixel.dmp";
	}
	//
	public String getCroppedImagePixelDumpFileName()
	{
		return getTempDir() + xU.ctSlash + "CroppedImagePixel.dmp";
	}
	//
	public String getConnectedComponentDumpFileName()
	{
		return getTempDir() + xU.ctSlash + "ConnCompLabelDump.dmp";
	}
	public String getAlternativeTempImageDir()
	{
	  return getRootDir() + xU.ctSlash + "Output" + xU.ctSlash + "Images";
	}
	//
	public String getTempDumpFileName()
	{
		return getTempDir() + xU.ctSlash + "TempDump.dmp";
	}
	public String getBoxDiagramName()
	{
		return getTempDir() + xU.ctSlash + "boxDiagram." + getPreferredImageSuffix();
	}
	public String getHistoScreenShotDumpNameUncropped()
	{
		return getTempDir() + xU.ctSlash + "histoUnCropped." + getPreferredImageSuffix();
	}
	public String getHistoScreenShotDumpNameUncropped90()
	{
		return getTempDir() + xU.ctSlash + "histoUnCropped90." + getPreferredImageSuffix();
	}
	public String getHistoScreenShotDumpNameGrayScale()
	{
		return getTempDir() + xU.ctSlash + "histoGrayScale." + getPreferredImageSuffix();
	}
	public String getHistoScreenShotDumpNameGrayScale90()
	{
		return getTempDir() + xU.ctSlash + "histoGrayScale90." + getPreferredImageSuffix();
	}
	public String getTempJPGName()
	{
		return getTempDir() + xU.ctSlash + "work." + getPreferredImageSuffix();
	}
	//
	public String getCCLOutputJPGName()
	{
		return getTempDir() + xU.ctSlash +  "CCL_Out." + getPreferredImageSuffix();
	}
	public String getBinarizedOutputJPGName()
	{
		return getTempDir() + xU.ctSlash +  "Binarized_Out." + getPreferredImageSuffix();
	}
	public String getBinarizedOutputImageNameUncropped()
	{
		return getTempDir() + xU.ctSlash +  "Binarized_Out_Uncropped." + getPreferredImageSuffix();
	}
	public String getTimingFile()
	{
		String sDir = getRootDir();
		return sDir + xU.ctSlash + "Corpus" + xU.ctSlash + "Stats" + xU.ctSlash + "TimingInputStats.txt";
	}
	public String getTimingAccuracyFile()
	{
		String sDir = getRootDir();
		return sDir + xU.ctSlash + "Corpus" + xU.ctSlash + "Stats" + xU.ctSlash + "TimingAccuracyStats.txt";
	}
	public String getLogFileName()
	{
		return getRootDir().trim() + xU.ctSlash + "cbrTekStraktorLogFile.txt";
	}
	public String getErrFileName()
	{
		return getRootDir().trim() + xU.ctSlash + "cbrTekStraktorErrFile.txt";
	}
	public String getOCROutputImageFileName()
	{
		return getOCRDir() + xU.ctSlash + "OCROutput." + getPreferredImageSuffix();
	}
	public String getOCRResultFile()
	{
		return getOCRDir() + xU.ctSlash + "OCRTextResult.txt";
	}

	//---------------------------------------------------------------------------------
	private String cleanseFileNaam(String sIn)
	//---------------------------------------------------------------------------------
	{
		String sTemp = "";
	    char[] SChar = sIn.toCharArray();
	    for(int ii=0;ii<SChar.length;ii++) 
		{	
			if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ||
				 ((SChar[ii] >= 'A') && (SChar[ii] <= 'Z')) ||
				 ((SChar[ii] >= 'a') && (SChar[ii] <= 'z')) ||
				 SChar[ii] == '-' ||
				 SChar[ii] == '_'
				) sTemp = sTemp + SChar[ii];
		}		
		return sTemp;
	}
	// Extracts the file name from absolute path and replaces non alphanumericals
	public String getCleansedShortFileName(String longFileName,String caller)
	{
		try {
		String sShortFileName = xU.GetFileName(longFileName);
		String sSuffix = xU.GetSuffix(sShortFileName);
		String sNew = xU.RemplaceerIgnoreCase(sShortFileName , "." + sSuffix , "" );
	    sNew = cleanseFileNaam(sNew);
	    return sNew.toLowerCase().trim();
		}
		catch(Exception e) {
			do_error("function - getCleansedShortFileName() [Caller=" + caller + "] [LongFileName=" + longFileName + "] [Msg=" + e.getMessage() + "]" );
		}
		return  null;
	}
	public String getXMLStatFileName()
	{
		String sDir = getStatDir();
		String sNew = getCleansedShortFileName(OrigImageLongFileName,"xMSet.getXMLStatFileName()") + "_stat.xml";
		return sDir + xU.ctSlash + sNew;
	}
	public String getXMLLangFileName()
	{
		String sDir = getStatDir();
		String sNew = getCleansedShortFileName(OrigImageLongFileName,"xMSet.getXMLLangFileName()") + "_lang.xml";
		return sDir + xU.ctSlash + sNew;
	}
	public String getTextOutputJPGName(int i)
	{
		String sDir = getReportImageDir();
		String sNew = getCleansedShortFileName(OrigImageLongFileName,"xMSet.getTextOutputJPGName") + "_txt_" + String.format("%03d", i) + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getReportZipName()
	{
		 String sDir = getArchiveDir();
		 String sNew = getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportZipName") + "_set.zip";
		 return sDir + xU.ctSlash + sNew;
	}
	public String getReportHTMLFileName()
	{
		String sDir = getReportHTMLDir();
		String sNew = getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportHTMLFileName") + ".html";
		return sDir + xU.ctSlash + sNew;
	}
	public String getLetterOutputJPGName()
	{
		String sDir = getTempDir();
		String sNew = "zCharacts_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getLetterOutputJPGName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getReportHistoColorName()
	{
		String sDir = getReportImageDir();
		String sNew = "zColrHist_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportHistoColorName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getReportHistoGrayName()
	{
		String sDir = getReportImageDir();
		String sNew = "zGrayHist_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportHistoGrayName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getReportBoxDiagramName()
	{
		String sDir = getReportImageDir();
		String sNew = "zBoxDiagr_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportColorBoxName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getReportClusterDiagramName()
	{
		String sDir = getReportImageDir();
		String sNew = "zClusters_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportClusterDiageamName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getMetaDataFileName()
	{
		String sDir = getArchiveDir();
		String sNew = "zMetaData_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getMetaDataFileName") + ".xml";
		return sDir + xU.ctSlash + sNew;
	}
	public String getGraphZMetaXML()
	{
	  return getCacheDir() + xU.ctSlash + "zMetaData_GraphEditor.xml";	
	}
	public String getReportPeakDiagramName()
	{
		String sDir = getReportImageDir();
		String sNew = "zPeakDiag_" + getCleansedShortFileName(OrigImageLongFileName,"xMSet.getReportPeakDiagramName") + "." + getPreferredImageSuffix();
		return sDir + xU.ctSlash + sNew;
	}
	public String getGraphXML()
	{
	  //logit(0,"GraphXML" + getCacheDir() + xU.ctSlash + "GraphEditor.xml" );
	  return getCacheDir() + xU.ctSlash + "GraphEditor.xml";	
	}
	public String getLanguageXML()
	{
	  return getCacheDir() + xU.ctSlash + "TextEditor.xml";	
	}
	public String getLanguageNewXML()
	{
	  return getCacheDir() + xU.ctSlash + "New_TextEditor.xml";	
	}
	
	public String getTesseractDir()
	{
	  return projman.getTesseractDir();	
	}
	public String getTesseractBatchFileName()
	{
		return getOCRDir() + xU.ctSlash + "TesseractRun.cmd";
	}
	public String getTesseractConfigurationFileName()
	{
		return getOCRDir() + xU.ctSlash + "TesseractConfig.txt";
	}
	public String getTesseractLogFileName()
	{
		return getOCRDir() + xU.ctSlash + "TesseractLog.txt";
	}
	public String getTesseractOptionFileName()
	{
		return getOCRDir() + xU.ctSlash + "TesseractOptionRepository.xml";
	}
	public String getEstafetteFileName()
	{
		return getArchiveDir() + xU.ctSlash + "estafette.xml";
	}
	public String getInterruptFileName()
	{
		return getArchiveDir() + xU.ctSlash + "cbrTekStraktor_LCK.txt";
	}
	//---------------------------------------------------------------------------------
	public String getSystemInfo()
	//---------------------------------------------------------------------------------
	{
		Properties prop = System.getProperties();
        Set<String> a = prop.stringPropertyNames();
        Iterator<String> keys = a.iterator();
        while (keys.hasNext())
        {
            String key = keys.next();
            String value = System.getProperty(key);
            do_log(9,key + "=" + value);
        }
        //
		return 
		    ""+ System.getProperty("os.name")
		+ " " + System.getProperty("sun.arch.data.model") + " bit"
		+ " [VM:" + System.getProperty("java.vm.version") + "]"
		+ " [Java:" + System.getProperty("java.version") + "]" 
		+ " " + System.getProperty("user.language").toUpperCase()  ;
		
	}
	
	//---------------------------------------------------------------------------------
	public String getPreferredImageSuffix()
	//---------------------------------------------------------------------------------
	{
		return preferredImageFormat.toLowerCase().trim();
	}
	//---------------------------------------------------------------------------------
	public int getHorizontalVerticalVarianceThreshold()
	//---------------------------------------------------------------------------------
	{
		return projman.getHorizontalVerticalVarianceThreshold();
	}
	//---------------------------------------------------------------------------------
	public int getMeanCharacterCount()
	//---------------------------------------------------------------------------------
	{
		return projman.getMeanCharacterCount();
	}
	//---------------------------------------------------------------------------------
	public void writePropertiesFile(Rectangle main , String[] ar)
	//---------------------------------------------------------------------------------
	{
	    if( this.isActive == false ) return;
		gpPrintStream ps = new gpPrintStream(getPropertiesFile(),"ASCII");
		ps.println("=============================================================");
		ps.println( getApplicDesc() );
		ps.println("Started=" + xU.prntStandardDateTime(startedAt) );
		ps.println("Stopped=" + xU.prntStandardDateTime(System.currentTimeMillis()));
		ps.println("=============================================================");
		ps.println("mainlocation=" + (int)main.getX() + "," + (int)main.getY() );
		ps.println("mainsize=" + (int)main.getWidth() + "," + (int)main.getHeight());
		ps.println("quicklocation=" + (int)quickframe.getX() + "," + (int)quickframe.getY() );
		ps.println("quicksize=" + (int)quickframe.getWidth() + "," + (int)quickframe.getHeight());
		ps.println("monitorlocation=" + (int)monitframe.getX() + "," + (int)monitframe.getY() );
		ps.println("monitorsize=" + (int)monitframe.getWidth() + "," + (int)monitframe.getHeight());
	    //
		for(int i=0;i<ar.length;i++)
		{
			ps.println("file=" + ar[i] );
		}
		ps.println("=============================================================");
		ps.println("RecentArchiveDir=" + recentArchiveDir );
		ps.println("RecentSaveDir=" + recentSaveDir );
		ps.println("RecentImageDir=" + recentImageDir );
		ps.println("=============================================================");
		ps.close();
		//
		flushProjectConfig(false);
	}
	//---------------------------------------------------------------------------------
	private void getProperties()
	//---------------------------------------------------------------------------------
	{
		if( xU.IsBestand( getPropertiesFile() ) == false ) return;
		String stext = xU.ReadContentFromFile( getPropertiesFile() , 1000 , "ASCII");
		int aantal = xU.TelDelims(stext,'\n');
		String srect[] = {null,null,null,null};
		int k=-1;
		for(int i=0;i<(aantal+1);i++)
		{
			String sLijn = xU.GetVeld(stext,i+1,'\n').trim();
			if( sLijn.indexOf("location=") >= 0) {
			    sLijn = xU.GetVeld(sLijn,2,'=');
			    srect[0] = xU.GetVeld(sLijn,1,',').trim();
			    srect[1] = xU.GetVeld(sLijn,2,',').trim();
			}
			if( sLijn.indexOf("size=") > 0) {
			    String ptwo = xU.GetVeld(sLijn,2,'=');
			    srect[2] = xU.GetVeld(ptwo,1,',').trim();
			    srect[3] = xU.GetVeld(ptwo,2,',').trim();
			    Rectangle rx = parserectangle(srect,sLijn);
			    if( rx != null ) {
			       //do_error( sLijn );
			    	if( sLijn.startsWith("main") ) mainframe = rx;
			    	if( sLijn.startsWith("quick") ) quickframe = rx;
			    	if( sLijn.startsWith("monitor") ) monitframe = rx;
			    }
			    srect[0]=srect[1]=srect[2]=srect[3]=null;
			}
			//
			if( sLijn.startsWith("file") ) {
				sLijn = xU.GetVeld(sLijn,2,'=');
				k++;
				if( k >= arStack.length ) continue;
				arStack[k] = sLijn;
			}
			if( sLijn.startsWith("RecentArchiveDir") ) {
			    sLijn = xU.GetVeld(sLijn,2,'=').trim();
			    if( xU.IsDir(sLijn) ) {
			    	recentArchiveDir=sLijn;
			    }
			}
			if( sLijn.startsWith("RecentImageDir") ) {
			    sLijn = xU.GetVeld(sLijn,2,'=').trim();
			    if( xU.IsDir(sLijn) ) {
			    	recentImageDir=sLijn;
			    }
			}
			if( sLijn.startsWith("RecentSaveDir") ) {
			    sLijn = xU.GetVeld(sLijn,2,'=').trim();
			    if( xU.IsDir(sLijn) ) {
			    	recentSaveDir=sLijn;
			    }
			}
		}
	}

	//------------------------------------------------------------
	private Rectangle parserectangle(String[] sbounds , String tipe)
	//------------------------------------------------------------
	{
		try {
		 int ix = xU.NaarInt(sbounds[0]);
		 int iy = xU.NaarInt(sbounds[1]);
		 int iw = xU.NaarInt(sbounds[2]);
		 int ih = xU.NaarInt(sbounds[3]);
		 if( ix < 0 ) return null;
		 if( iy < 0 ) return null;
		 if( iw < 100 ) return null;
		 if (ih < 100 ) return null;
         return new Rectangle( ix , iy , iw , ih );
		}
		catch(Exception e ) {
			do_error("Cannot parse rectangle dimension in properties file");
			return null; 
		}
	}
	
	//------------------------------------------------------------
	public long mkNumericalUID()
	//------------------------------------------------------------
	{
			longCounter = longCounter + 1L;
			return startTmeNano + longCounter;
	}
	
	//---------------------------------------------------------------------------------
    String InsertDashes ( String sIn )
	//---------------------------------------------------------------------------------
	{   
		   char[] cbuf = sIn.toCharArray();
		   String Ret="";
		   for(int ii=0;ii<cbuf.length;ii++) 
		   {	
				Ret += cbuf[ii];
				if( ((ii % 4)==3) && ( ii!=(cbuf.length-1)) ) Ret += "-";
		   }		
		   return Ret;
	}
	
	//------------------------------------------------------------
	public String mkJavaUID()
	//------------------------------------------------------------
	{
		String sUID = xU.makeMD5Hex((""+UUID.randomUUID()).toUpperCase());
		//return (""+UUID.randomUUID()).toUpperCase();
		return InsertDashes(sUID);
	}

	//------------------------------------------------------------
	public String mkJavaUIDNew(String longFileName)
	//------------------------------------------------------------
	{
		if( longFileName == null ) return mkJavaUID();
		if( longFileName.length() <=0 ) return mkJavaUID();
	    //	
		String part1 = xU.keepLettersAndNumbers(getCleansedShortFileName(longFileName,"xMSet.mkJavaUIDNew").trim());
	    String part2 = (""+xU.getFileSize(longFileName)).trim();
		String part3 = (""+xU.getModificationTime(longFileName)).trim();
		String sUID = xU.makeMD5Hex((part1+"-"+part2+"-"+part3).trim().toUpperCase());
        //System.out.println("->" +(part1+"-"+part2+"-"+part3).trim().toUpperCase() + " -> " + sUID);
		return InsertDashes(sUID);
	}

	//------------------------------------------------------------
	public boolean purgeDirByName(String sDir,boolean verbose)
	//------------------------------------------------------------
	{
		return purgeDirByNameButKeep( sDir , null , verbose);
	}
	
	//------------------------------------------------------------
	public boolean purgeDirByNameButKeep(String sDir,String KeepFName , boolean verbose)
	//------------------------------------------------------------
	{
		// Safety
		if( sDir == null ) {
			do_error("Folder name is empty or null - contents will not be purged");
			return false;
		}
		if (sDir.length() < 10 ) {
			do_error("Folder name to small - contents will not be purged");
			return false;
		}
		ArrayList<String> flistdel = xU.GetFilesInDir( sDir , null );
	    for(int i=0;i<flistdel.size();i++)
	    {
	        	String FNaam = sDir + xU.ctSlash + flistdel.get(i);
	        	if( xU.IsBestand( FNaam ) == false ) continue;
	        	if( KeepFName != null ) {
	        		if( FNaam.compareTo( KeepFName  ) == 0 ) continue;
	        	}
	        	if( verbose ) do_log(1,"Removing file [" + FNaam + "]");
	        	xU.VerwijderBestand( FNaam );
	    }
	    return true;
	}
		
	//------------------------------------------------------------
	public String getFileStack(int i)
	//------------------------------------------------------------
	{
		if( i < 0 ) return null;
		if (i >= arStack.length) return null;
		return arStack[i];
	}
	//------------------------------------------------------------
	public cmcProcEnums.QUICKEDITOPTIONS getQuickEditOption()
	//------------------------------------------------------------
	{
		return quickEditTipe;
	}
	//------------------------------------------------------------
	public void setQuickEditOption(cmcProcEnums.QUICKEDITOPTIONS inOption)
	//------------------------------------------------------------
	{
		quickEditTipe = inOption;
	}
	
	public void setQuickEditRequestedRow(int currentrow)
	{
		QuickEditRequestedRow=currentrow;
	}
	public int getQuickEditRequestedRow()
	{
		return QuickEditRequestedRow;
	}
	
	
	public void setRecentImageDir(String sDir)
	{
		if( xU.IsDir(sDir) ) recentImageDir=sDir;
	}
	public void setRecentArchiveDir(String sDir)
	{
		if( xU.IsDir(sDir) ) recentArchiveDir=sDir;
	}
	public void setRecentSaveDir(String sDir)
	{
		if( xU.IsDir(sDir) ) recentSaveDir=sDir;
	}
	public String getRecentImageDir()
	{
		if( xU.IsDir(recentImageDir) ) return recentImageDir; else return getRootDir();
	}
	public String getRecentSaveDir()
	{
		if( xU.IsDir(recentSaveDir) ) return recentSaveDir; else return getRootDir();
	}
	public String getRecentArchiveDir()
	{
		if( xU.IsDir(recentArchiveDir) ) return recentArchiveDir; else return getRootDir();
	}
	public String getSuperDir()
	{
		return projman.getSuperDir();
	}
	public boolean getuseMonoChromeInDialogs()
	{
		return useMonoChromeInDialogs;
	}
	public void setUseMonoChromeInDialogs(boolean ib)
	{
		useMonoChromeInDialogs=ib;
	}

	//---------------------------------------------------------------------------------
	public String getCodePageString()
	//---------------------------------------------------------------------------------
	{
		return xU.RemplaceerIgnoreCase((""+projman.getEncoding()).trim(), "_" , "-");
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.ENCODING getEncoding()
	//---------------------------------------------------------------------------------
	{
		return projman.getEncoding();
	}

	//---------------------------------------------------------------------------------
	public String getXMLEncodingHeaderLine()
	//---------------------------------------------------------------------------------
	{
		return "<?xml version=\"1.0\" encoding=\"" + getCodePageString().trim().toUpperCase() + "\"?>";
	}
	//---------------------------------------------------------------------------------
	public void setCurrentArchiveFileName(String fn)
	//---------------------------------------------------------------------------------
	{
		currentArchiveFileName = (fn == null) ? null : fn.trim();
	}
	//---------------------------------------------------------------------------------
	public String getCurrentArchiveFileName()
	//---------------------------------------------------------------------------------
	{
		return currentArchiveFileName;
	}
	
	//---------------------------------------------------------------------------------
	public void flushProjectConfig(boolean overrule)
	//---------------------------------------------------------------------------------
	{
		if( (this.isActive == false) && ( overrule == false) ) return;
		String ParentFolder  = xU.getParentFolderName( getRootDir() );
		String FolderName  = xU.getFolderOrFileName( getRootDir() );
		gpPrintStream ps = new gpPrintStream(projman.getProjectConfigFileName(),"ASCII");
		ps.println("=============================================================");
		ps.println( getApplicDesc() );
		ps.println("Started=" + xU.prntStandardDateTime(startedAt) );
		ps.println("Stopped=" + xU.prntStandardDateTime(System.currentTimeMillis()));
		ps.println("=============================================================");
		ps.println("EntryFolder=" + ParentFolder);
		ps.println("RecentProject=" + FolderName);
		ps.close();
	}
	
	//---------------------------------------------------------------------------------
	public cmcProcEnums.OS_TYPE getMyOS()
	//---------------------------------------------------------------------------------
	{
	    return ( System.getProperty("file.separator").trim().startsWith("/") == true) ? cmcProcEnums.OS_TYPE.LINUX : cmcProcEnums.OS_TYPE.MSDOS;
	}
	
	public void requestProjectSwap(boolean ib)
	{
		requestProjectSwap = ib;
	}
	public boolean getRequestProjectSwap()
	{
		return requestProjectSwap;
	}
		
	//---------------------------------------------------------------------------------
	private void maakLanguages()
	//---------------------------------------------------------------------------------
	{
		String[] ll = langObj.makeLanguageList();   // sorted on code
		languageLijst = xU.sortStringArray(ll);
	}
	//---------------------------------------------------------------------------------
	public String[] getLanguageList()
	//---------------------------------------------------------------------------------
	{
  	return languageLijst;
	}
	//---------------------------------------------------------------------------------
	public void setLanguageList(String[] ll)
	//---------------------------------------------------------------------------------
	{
	  languageLijst = new String[ll.length];	
	  for(int i=0;i<languageLijst.length;i++) languageLijst[i] = xU.Capitalize( languageLijst[i] ).trim();	
  	  languageLijst = xU.sortStringArray(ll);
	}
	//---------------------------------------------------------------------------------
	public String getLanguageCode(int idx , boolean verbose)
	//---------------------------------------------------------------------------------
	{
		try {
		  return getLanguageList()[idx].trim().toUpperCase();
		}
		catch(Exception e ) {
			if( verbose) do_error("Unknown language Index [" + idx + "]");
			return "UNKNOWN"; }
	}

	//---------------------------------------------------------------------------------
	public String getTesseractLanguage(String cmxLanguage) 
	//---------------------------------------------------------------------------------
	{
		String code = null;
		try {
         code = langObj.getTesseractLanguageCode(cmxLanguage);
 		 if( code == null ) {
			do_error("Currently unsupported language [" + cmxLanguage + "] - you will need to review method gettesseractLanguage");
			code= "eng";
	     }
        }
		catch( Exception e ) {
	       code = "eng";			  
		}
	    return code;
	}
    //---------------------------------------------------------------------------------
	public String getPreferredLanguageLong()
	//---------------------------------------------------------------------------------
	{
		return projman.getPreferredLanguageLong();
	}
	//---------------------------------------------------------------------------------
	public cmcProcEnums.BackdropType getBackDropType()
	//---------------------------------------------------------------------------------
	{
		return projman.getBackDropType();
	}
	//---------------------------------------------------------------------------------
	public String getPythonHomeDir()
	//---------------------------------------------------------------------------------
	{
		return projman.getPyhtonHomeDir();
	}
	//---------------------------------------------------------------------------------
	public String getTensorDir()
	//---------------------------------------------------------------------------------
	{
		return projman.getTensorDir();
	}
	//---------------------------------------------------------------------------------
	public String getScanFolder()
	//---------------------------------------------------------------------------------
	{
		return moma.getScanFolder();
	}
	//---------------------------------------------------------------------------------
	public void setOCRSummaryResult(String s)
	//---------------------------------------------------------------------------------
	{
		OCRSummaryResult = s;
	}
	//---------------------------------------------------------------------------------
	public String getOCRSummaryResult()
	//---------------------------------------------------------------------------------
	{
		//do_error( OCRSummaryResult );
		return OCRSummaryResult == null ? "" : OCRSummaryResult.trim();
	}
	//---------------------------------------------------------------------------------
	public String getTensorSummaryResult()
	//---------------------------------------------------------------------------------
	{
		return getOCRSummaryResult();
	}
	//---------------------------------------------------------------------------------
	public void setQuickFrameBounds(int x , int y , int w , int h)
	//---------------------------------------------------------------------------------
	{
		quickframe.setBounds(x, y, w, h);
	}
	//---------------------------------------------------------------------------------
	public void setMonitorFrameBounds(int x , int y , int w , int h)
	//---------------------------------------------------------------------------------
	{
		monitframe.setBounds(x, y, w, h);
	}
	
}
