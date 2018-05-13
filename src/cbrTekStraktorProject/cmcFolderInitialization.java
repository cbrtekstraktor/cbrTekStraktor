package cbrTekStraktorProject;


import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcMakeCSSFile;
import dao.cmcProjectDAO;
import logger.logLiason;

public class cmcFolderInitialization {
	
	
	cmcProcSettings xMSet=null;
	logLiason logger = null;
	private String InitError = "";
	private boolean isOK=true;
	
	private String sSuperDir=null;
	private String sRootDir = null;
	private String sOutputDir=null;
	private String sStatDir=null;
	private String sTempDir=null;
	private String sReportHTMLDir=null;
	private String sReportImageDir=null;
	private String sCorpusDir=null;
    private String sCorpusStatDir=null;
    private String sCorpusHTMLDir=null;
    private String sCorpusImageDir=null;
    private String sCacheDir=null;
    private String sArchiveDir=null;
    private String sOCRDir=null;
    private String sTensorDir=null;
    
    
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
    //---------------------------------------------------------------------------------
  	private void ErrIt(String sIn)
  	//---------------------------------------------------------------------------------
  	{
  	  do_error("Error :" + sIn);
  	  InitError += xMSet.xU.ctEOL + sIn;
  	}
  	public String getErrors()
  	{
  		return InitError;
  	}
    //---------------------------------------------------------------------------------
	public cmcFolderInitialization(cmcProcSettings is, logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
	}
	
	//
	//---------------------------------------------------------------------------------
	//---------------------------------------------------------------------------------
	public String getSuperDir()       { return sSuperDir; }
	public String getRootDir()        { return sRootDir; }
	public String getOutputDir()      { return sOutputDir; }
	public String getStatDir()        { return sStatDir; }
	public String getTempDir()        { return sTempDir; }
	public String getReportHTMLDir()  { return sReportHTMLDir; }
	public String getReportImageDir() { return sReportImageDir; }
	public String getCorpusDir()      { return sCorpusDir; }
    public String getCorpusStatDir()  { return sCorpusStatDir; }
    public String getCorpusHTMLDir()  { return sCorpusHTMLDir; }
    public String getCorpusImageDir() { return sCorpusImageDir; }
    public String getCacheDir()       { return sCacheDir; }
    public String getArchiveDir()     { return sArchiveDir; }
    public String getOCRDir()         { return sOCRDir; }
    public String getTensorDir()      { return sTensorDir; }
    //	
	    
    //---------------------------------------------------------------------------------
	public boolean checkFoldersAndFiles(String rootIn , boolean fixit , boolean projectExplicitelyDefined)
	//---------------------------------------------------------------------------------
	{
	    isOK=true;
		if( rootIn == null ) {
			System.err.println("Oops - system error - checkFoldersAndFiles - null rootdir" );
			return false;
		}
		
		sRootDir = rootIn;
		sSuperDir = xMSet.xU.getParentFolderName(sRootDir);
		//
		// Check dirs
		if( xMSet.xU.IsDir(sSuperDir) == false ) 
	    {
			if( (fixit==true) && (projectExplicitelyDefined==false) ) {   // not if the project is defined - too much risk in creating a folder in wrong place
			 createFolder( sSuperDir );	
			}
			else {
    	     ErrIt("Folder [" + sSuperDir + "] cannot be accessed");
    	     isOK = false;
			}
	    }
		//
		if( xMSet.xU.IsDir(sRootDir) == false ) 
	    {
			if( fixit ) {
			 createFolder( sRootDir );	
			}
			else {
    	     ErrIt("Folder [" + sRootDir + "] cannot be accessed");
    	     isOK = false;
			}
	    }
		//
	    sOutputDir = sRootDir + xMSet.xU.ctSlash + "Output";
	    if( xMSet.xU.IsDir(sOutputDir) == false ) 
	    {
	    	if( fixit ) {
	    	 createFolder(sOutputDir);	
	    	}
	    	else {
	    	 ErrIt("Output folder [" + sOutputDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sStatDir = sOutputDir + xMSet.xU.ctSlash + "Stats";
	    if( xMSet.xU.IsDir(sStatDir) == false ) 
	    {
	    	if( fixit ) {
	    	 createFolder(sStatDir);	
	    	}
	    	else {
	    	 ErrIt("Statistics folder [" + sStatDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sArchiveDir = sOutputDir + xMSet.xU.ctSlash + "Archive";
	    if( xMSet.xU.IsDir(sArchiveDir) == false ) 
	    {
	    	if( fixit ) {
	    	 createFolder( sArchiveDir );	
	    	}
	    	else {
	    	 ErrIt("Archive folder [" + sArchiveDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sTempDir = sRootDir + xMSet.xU.ctSlash + "Temp";
	    if( xMSet.xU.IsDir(sTempDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder(sTempDir);
	    	}
	    	else {
	    	 ErrIt("Statistics folder [" + sTempDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sReportHTMLDir =  sOutputDir + xMSet.xU.ctSlash + "Html";   		
	    if( xMSet.xU.IsDir(sReportHTMLDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder( sReportHTMLDir );
	    	}
	    	else {
	    	ErrIt("HTML folder [" + sReportHTMLDir + "] cannot be accessed");
	    	isOK=false;
	    	}
	    }
	    //
	    sReportImageDir =  sOutputDir + xMSet.xU.ctSlash + "Images";   		
	    if( xMSet.xU.IsDir(sReportImageDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder(sReportImageDir);
	    	}
	    	else {
	    	 ErrIt("Report Image folder [" + sReportImageDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sCorpusDir =  sRootDir + xMSet.xU.ctSlash + "Corpus";   		
	    if( xMSet.xU.IsDir(sCorpusDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder( sCorpusDir );
	    	}
	    	else {
	    	 ErrIt("Corpus folder [" + sCorpusDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sCorpusStatDir =  sCorpusDir + xMSet.xU.ctSlash + "Stats";   		
	    if( xMSet.xU.IsDir(sCorpusStatDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder( sCorpusStatDir );
	    	}
	    	else {
	    	 ErrIt("Corpus Statistics folder [" + sCorpusStatDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sCorpusHTMLDir =  sCorpusDir + xMSet.xU.ctSlash + "Html";   		
	    if( xMSet.xU.IsDir(sCorpusHTMLDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder( sCorpusHTMLDir );
	    	}
	    	else {
	    	 ErrIt("Corpus HTML folder [" + sCorpusHTMLDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sCorpusImageDir =  sCorpusDir + xMSet.xU.ctSlash + "Images";   		
	    if( xMSet.xU.IsDir(sCorpusImageDir) == false ) 
	    {
	    	if( fixit ) {
	    	 createFolder( sCorpusImageDir );	
	    	}
	    	else {
	    	 ErrIt("Corpus Images folder [" + sCorpusImageDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sCacheDir =  sRootDir + xMSet.xU.ctSlash + "Cache";   		
	    if( xMSet.xU.IsDir(sCacheDir) == false ) 
	    {
	    	if( fixit ) {
	    	  createFolder( sCacheDir );	
	    	}
	    	else {
	    	 ErrIt("Cache folder [" + sCacheDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    //
	    sOCRDir = sRootDir + xMSet.xU.ctSlash + "Ocr";
	    if( xMSet.xU.IsDir(sOCRDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder(sOCRDir);
	    	}
	    	else {
	    	 ErrIt("OCR folder [" + sOCRDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
        //
	    //
	    sTensorDir = sRootDir + xMSet.xU.ctSlash + "Tensorflow";
	    if( xMSet.xU.IsDir(sTensorDir) == false ) 
	    {
	    	if( fixit ) {
	    		createFolder(sTensorDir);
	    	}
	    	else {
	    	 ErrIt("TensorFlow folder [" + sTensorDir + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
	    
	    // Files
	    // StyleSheet - indien niet bestaand maken
	    String FNaam = sReportHTMLDir + xMSet.xU.ctSlash + "cbrTekStraktorCSS.txt";
	    if ( xMSet.xU.IsBestand(FNaam) == false ) {
	        cmcMakeCSSFile ccss = new cmcMakeCSSFile(xMSet , FNaam);
	     }
	    if ( xMSet.xU.IsBestand(FNaam) == false ) {
	    	ErrIt("CSS file [" + FNaam + "] cannot be accessed");
	    	isOK=false;
	    }
	    //
	    FNaam = sRootDir + xMSet.xU.ctSlash + "cbrTekStraktor.xml";
	    if ( xMSet.xU.IsBestand(FNaam) == false ) {
	        // make one
	    	if( xMSet.xU.IsDir(sRootDir) ) {
	    	  //cmcMakeConfigFile cccc = new cmcMakeConfigFile( xMSet , FNaam );
	    	  boolean ib = makeEmptyConfigFile( sRootDir );
	    	}
	    	if ( xMSet.xU.IsBestand(FNaam) == false ) {
	    	 ErrIt("Config file [" + FNaam + "] cannot be accessed");
	    	 isOK=false;
	    	}
	    }
        //		
		return isOK;
	}
	
	//---------------------------------------------------------------------------------
	public boolean fixit(boolean projectExplicitelyDefined)
	//---------------------------------------------------------------------------------
	{
	  boolean ib = checkFoldersAndFiles(sRootDir, true , projectExplicitelyDefined);
	  return ib;
	}
	
	//---------------------------------------------------------------------------------
	private boolean createFolder(String FolderName )
	//---------------------------------------------------------------------------------
	{
		// safety
		if( FolderName == null ) return false;
		if( FolderName.trim().length() < 10 ) {
			do_error("Foldername [" + FolderName + "] is too short and will not be created");
			return false;
		}
		//
	    do_log( 1, "Creating folder [" + FolderName.trim() + "]");
	    boolean ib = xMSet.xU.CreateDirectory(FolderName);
	    //
	    if( (xMSet.xU.IsDir( FolderName.trim() ) == false) || (ib==false) ) {
	    	do_error( "Could not create folder [" + FolderName + "]");
	    	return false;
	    }
		return true;
	}
	
	//---------------------------------------------------------------------------------
	private boolean makeEmptyConfigFile( String ProjectFolder )
	//---------------------------------------------------------------------------------
	{
		// create a project with default values
		cmcProjectCore proj = new cmcProjectCore(ProjectFolder);
		// set the name and desc
		proj.setProjectName( xMSet.xU.RemplaceerIgnoreCase(xMSet.xU.getFolderOrFileName(ProjectFolder),".xml","") );
		proj.setDescription( "Created by " + xMSet.getApplicDesc() );
		//
		cmcProjectDAO dao = new cmcProjectDAO(xMSet,logger);
		boolean ib = dao.writeConfig(proj);
		dao = null;
		proj=null;
		if( ib == false ) {
			do_error("Could not write intial project XML in [" + ProjectFolder + "]");
			return false;
		}
		return true;
	}
	
	
}
