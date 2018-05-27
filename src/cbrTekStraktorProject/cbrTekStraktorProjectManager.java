package cbrTekStraktorProject;
import java.awt.Font;
import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;

public class cbrTekStraktorProjectManager {
	
	
	cmcProcSettings xMSet=null;
	logLiason logger = null;
	//cmcFolderInitialization ina=null;

	private cmcProjectWrapper currProj = null;
	private boolean isOK = false;
	private boolean projectExplicitelyDefined = false;
	private String fileSeparator = System.getProperty("file.separator").trim();
	private String ShortConfigName = "cbrTekstraktorProjectConfig.txt";
	
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
    public cbrTekStraktorProjectManager(cmcProcSettings is, logLiason ilog)
    //------------------------------------------------------------
    {
		xMSet = is;
		logger = ilog;
	}
    //------------------------------------------------------------
    public String getInitializationErrors()
    //------------------------------------------------------------
    {
    	//if( ina == null ) return "System Error";
    	return currProj.getInitializationErrors();
    }
    //------------------------------------------------------------
    public String getProjectConfigFileName()  
    //------------------------------------------------------------
    { 
    	return getSuperDir() + fileSeparator + ShortConfigName;
    }
    //------------------------------------------------------------
    public boolean fixit()
    //------------------------------------------------------------
    {
    	//return ina.fixit(projectExplicitelyDefined);
    	return currProj.fixit(projectExplicitelyDefined);
    }
    
	//---------------------------------------------------------------------------------
    public boolean launchProject(String[] args)
	//---------------------------------------------------------------------------------
    {
    	isOK = true;
        //
        String projdir = locateRootFolderAtStartup(args);
        if( projdir == null ) {
        	do_error("Major problem - cannot determine the project root folder");
        	isOK = false;
        }
        // cmcProject MUST be initialized even when isOK == false + cmcProject initializes the folders
        currProj = new cmcProjectWrapper(xMSet,logger,projdir);
    	boolean ib = currProj.getProjectStatus();
    	if( ib == false ) isOK = false;
        //
    	return isOK;
    }
    
    //
    public cmcProjectWrapper getCurrentProject() { return currProj; }
    //
	public String getSuperDir()       { return currProj.getSuperDir(); }
	public String getRootDir()        { return currProj.getRootDir(); }
	public String getOutputDir()      { return currProj.getOutputDir(); }
	public String getStatDir()        { return currProj.getStatDir(); }
	public String getTempDir()        { return currProj.getTempDir(); }
	public String getReportHTMLDir()  { return currProj.getReportHTMLDir(); }
	public String getReportImageDir() { return currProj.getReportImageDir(); }
	public String getCorpusDir()      { return currProj.getCorpusDir(); }
    public String getCorpusStatDir()  { return currProj.getCorpusStatDir(); }
    public String getCorpusHTMLDir()  { return currProj.getCorpusHTMLDir(); }
    public String getCorpusImageDir() { return currProj.getCorpusImageDir(); }
    public String getCacheDir()       { return currProj.getCacheDir(); }
    public String getArchiveDir()     { return currProj.getArchiveDir(); }
    public String getOCRDir()         { return currProj.getOCRDir(); }
    public String getTensorDir()      { return currProj.getTensorDir(); }
    //
    public String getDateformat()        { return currProj.getDateformat(); }
    public int getMeanCharacterCount()   { return currProj.getMeanCharacterCount(); }
    public String getTesseractDir()      { return currProj.getTesseractDir(); }
    public String getPyhtonHomeDir()     { return currProj.getPythonHomeDir(); }
    
    public Font getPreferredFont()       { return currProj.getPreferredFont(); }
    public String getPreferredFontName() { return currProj.getPreferredFontName();}
    public int getPreferredFontSize()    { return currProj.getPreferredFontSize(); }
    public int getLogLevel()             { return currProj.getLogLevel(); }
    public int getMaxThreads()           { return currProj.getMaxThreads(); }
    public int getHorizontalVerticalVarianceThreshold() { return currProj.getHorizontalVerticalVarianceThreshold(); }
    public cmcProcEnums.ENCODING getEncoding()          { return currProj.getEncoding(); }
    public cmcProcEnums.BROWSER getBrowser() { return currProj.getBrowser(); }
    //
    public String getProjectDescription() { return currProj.getProjectDescription(); }
	public String getProjectName()        { return currProj.getProjectName(); }
	public String getPreferredLanguageLong()  { return currProj.getPreferredLanguageLong(); }
	public cmcProcEnums.BackdropType getBackDropType() { return currProj.getBackDropType(); }
	
	//---------------------------------------------------------------------------------
	private void Usage()
	//---------------------------------------------------------------------------------
	{
			do_error(xMSet.getApplicationName() + " " + xMSet.getApplicDesc());
			do_error("Usage : " + xMSet.getApplicationName() + " -D WorkFolder");
	}
		
	//---------------------------------------------------------------------------------
  	private String locateRootFolderAtStartup(String[] args)
  	//---------------------------------------------------------------------------------
  	{
		isOK=true;
		String sSuperDir=null;
		String sRootDir = null;
		
		// if folder is specified it must exist
		 if( args.length == 2) {
		    if( args[0].compareToIgnoreCase("-D") != 0) {
	    	 Usage();
	    	 return null;
	        }
		    else {
		    	projectExplicitelyDefined = true;
		    	sSuperDir = xMSet.xU.getParentFolderName(args[1]);
		    	sRootDir = args[1];
		    }
		 }
		 else if( args.length > 2) {
			Usage();
			return null;
		 }	
		 else {  // Unix => $HOME/cbrTekStraktor   Windows  c:\temp\cbrTekStraktor
		    if( xMSet.getMyOS() == cmcProcEnums.OS_TYPE.LINUX ) sSuperDir = System.getProperty("user.home").trim() + "/" + xMSet.getApplicationName();
	        	                                else sSuperDir = "c:\\temp\\" + xMSet.getApplicationName();
	        // check rootdir - read lastproject - look for recent folder
	        if( xMSet.xU.IsDir(sSuperDir) == true ) {
	        	 // is there a RecentProjectFile
	        	 sRootDir = readLastProject(sSuperDir);
	        	 // lastproject does noet succeed - look for most recent folder
	        	 if( sRootDir == null ) {
	                 sRootDir = guessLastProject(sSuperDir);
	                 if( sRootDir != null ){
	                	 if( xMSet.xU.IsDir(sRootDir)==false) sRootDir=null;
	                 }
	        	 }
	        }
	        // RootDir could not be set
	        if( sRootDir == null ) {
	        	if( xMSet.getMyOS() == cmcProcEnums.OS_TYPE.LINUX ) sRootDir = sSuperDir + "/Tutorial"; else sRootDir = sSuperDir + "\\Tutorial";
	        }
	     	do_log(1,"RecentProject [" + sRootDir + "]");
		 }
		 //
         return sRootDir;  		
  	}
    
    //---------------------------------------------------------------------------------
  	private String readLastProject(String sSuperDir)
  	//---------------------------------------------------------------------------------
  	{
  		String ConfigName = sSuperDir + fileSeparator + ShortConfigName;
  		if( xMSet.xU.IsBestand(  ConfigName ) == false ) return null;
  		//
  		String sText = xMSet.xU.ReadContentFromFile( ConfigName  , 1000 , "ASCII" );
  		int aantal = xMSet.xU.TelDelims( sText , '\n' );
  		for(int i=0;i<=aantal;i++)
  		{
  			String sLijn = xMSet.xU.GetVeld( sText , (i+1) , '\n' );
  			if( sLijn == null ) continue;
  			sLijn = sLijn.trim();
  			if( sLijn.length() < 1 ) continue;
  			if( sLijn.toUpperCase().startsWith("RECENTPROJECT=") == false ) continue;
  			try {
  				String PotentialRootDir = sSuperDir + fileSeparator + sLijn.substring( "RECENTPROJECT=".length() );
  				if( xMSet.xU.IsDir(PotentialRootDir) ) return PotentialRootDir;
  				do_error("Project defined in config does not exist [" + PotentialRootDir +"] ignoring it");
  			}
  			catch( Exception e ) { return null; }
  		}
  		return null;
  	}
  	
  	//---------------------------------------------------------------------------------
  	private String guessLastProject(String sSuperDir)
  	//---------------------------------------------------------------------------------
  	{
  	   ArrayList<String>list = xMSet.xU.GetDirsInDir(sSuperDir);
  	   String guess=null;
  	   for(int i=0;i<list.size();i++)
  	   {
  		   // Check whether there is a config
  		   String Check = sSuperDir + fileSeparator + this.ShortConfigName;
  		   if( xMSet.xU.IsBestand(Check) == false ) continue;
  		   if( i == 0 )  guess = list.get(i);
  	   }
  	   if( guess == null )
	   do_error("Could not find a valid application folder in [" + sSuperDir + "]");
  	   else
  	   do_log(1,"Recent project is guessed to be [" + guess + "]");
  	   return guess;
  	}

  	//---------------------------------------------------------------------------------
  	public boolean switchProject(String iProjectFolderName)
  	//---------------------------------------------------------------------------------
  	{
  	  currProj = null;
  	  currProj = new cmcProjectWrapper(xMSet,logger,iProjectFolderName);
  	  return currProj.getProjectStatus();
  	}
}
