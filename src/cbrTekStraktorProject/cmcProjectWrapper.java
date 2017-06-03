package cbrTekStraktorProject;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcProjectDAO;
import logger.logLiason;

public class cmcProjectWrapper {

	cmcProcSettings xMSet=null;
	logLiason logger = null;
	cmcFolderInitialization ina=null;
	cmcProjectCore core=null;
	
	private boolean isOK = true;
	//
	private long size = 0L;
	private int nbrOfFiles = 0;
	private int nbrOfArchives = 0;
	private long firstAccessed = 0L;
	private long lastAccessed = 0L;
	   
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
    //
	//------------------------------------------------------------
	public cmcProjectWrapper(cmcProcSettings is, logLiason ilog , String projdir)
	//------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
		core=null;
		isOK = openProject(projdir);
	}
	
	//------------------------------------------------------------
	public boolean getProjectStatus()	
	//------------------------------------------------------------
	{
		return isOK;
	}
	//------------------------------------------------------------
    public String getInitializationErrors()
	//------------------------------------------------------------
    {
    	if( ina == null ) return "System Error";
    	return ina.getErrors();
    }
	//------------------------------------------------------------
	public boolean fixit( boolean projectExplicitelyDefined)
	//------------------------------------------------------------
	{
		if( ina == null ) {
			do_error("Strange cmcInitialize is not instantiated - System error in cmcProject");
			return false;
		}
	    return ina.fixit(projectExplicitelyDefined);
	}
	//------------------------------------------------------------
	public boolean openProject(String projdir)	
	//------------------------------------------------------------
	{
		core=null;
		if( projdir == null ) {
			do_error("Oops - system error in cmcProjectWrapper.openProject- null ProjectFolderName" ); 
			isOK = false;
		}
		// read the information on the folders
		ina  = new cmcFolderInitialization(xMSet,logger);
        boolean ib = ina.checkFoldersAndFiles( projdir , false , false); // 1st false = do not fix  ignore 2nd false
        if( ib == false ) isOK=false;
        // read the config regardless whether the folder structure is correct or not
        cmcProjectDAO dao = new cmcProjectDAO(xMSet,logger);
        core = dao.readProjectXMLConfig(projdir);
        if( core == null ) {  // create a bogus coreproject
        	core = new cmcProjectCore("c:\\temp\\error");
        	isOK = false;
        	do_error("Could not read project config - usign a default one");
        }
    //do_error("ProjectWrapper -> creatime=" + this.getProjectCreationTime() + " " + core.getCreated());
        //
        return isOK;
    }

	//------------------------------------------------------------
	//------------------------------------------------------------
	public String getSuperDir()       { return ina.getSuperDir(); }
	public String getRootDir()        { return ina.getRootDir(); }
	public String getOutputDir()      { return ina.getOutputDir(); }
	public String getStatDir()        { return ina.getStatDir(); }
	public String getTempDir()        { return ina.getTempDir(); }
	public String getReportHTMLDir()  { return ina.getReportHTMLDir(); }
	public String getReportImageDir() { return ina.getReportImageDir(); }
	public String getCorpusDir()      { return ina.getCorpusDir(); }
    public String getCorpusStatDir()  { return ina.getCorpusStatDir(); }
    public String getCorpusHTMLDir()  { return ina.getCorpusHTMLDir(); }
    public String getCorpusImageDir() { return ina.getCorpusImageDir(); }
    public String getCacheDir()       { return ina.getCacheDir(); }
    public String getArchiveDir()     { return ina.getArchiveDir(); }
    public String getOCRDir()         { return ina.getOCRDir(); }
    //
    //
	public void setDateformat(String s)        { core.setDateformat(s); }
    public void setMeanCharacterCount(int i)   { core.setMeanCharacterCount(i); }
    public void setTesseractDir(String s)      { core.setTesseractDir(s); }
    public void setPreferredFontName(String s) { core.setPreferredFontName(s) ; }
    public void setPreferredFontSize(int i)    { core.setPreferredFontSize(i) ; }
    public void setPreferredFont(Font x)       { core.setPreferredFont(x) ; }
    public void setLogLevel(int i)             { core.setLogLevel(i); }
    public void setDescription(String s)       { core.setDescription(s); }
    public void setProjectName(String s)       { core.setProjectName(s);}    
	public void setHorizontalVerticalVarianceThreshold(int i) { core.setHorizontalVerticalVarianceThreshold(i); }
    public void setEncoding(cmcProcEnums.ENCODING cp)         { core.setEncoding(cp); }
    public void setBrowser(cmcProcEnums.BROWSER br) { core.setBrowser(br); }
    
	//------------------------------------------------------------
	//------------------------------------------------------------
    public cmcProjectCore getCore()       { return core; }
    public String getDateformat()         { return core.getDateformat(); }
    public int getMeanCharacterCount()    { return core.getMeanCharacterCount(); }
    public String getTesseractDir()       { return core.getTesseractDir(); }
    public Font getPreferredFont()        { return core.getPreferredFont(); }
    public String getPreferredFontName()  { return core.getPreferredFontName(); }
    public int getPreferredFontSize()     { return core.getPreferredFontSize(); }
    public int getLogLevel()              { return core.getLogLevel(); }
    public String getProjectDescription() { return core.getProjectDescription(); }
    public String getProjectName()        { return core.getProjectName(); }
    public cmcProcEnums.ENCODING getEncoding() { return core.getEncoding(); }
    public int getHorizontalVerticalVarianceThreshold() { return core.getHorizontalVerticalVarianceThreshold(); }
    public long getProjectCreationTime()                { return core.getCreated(); }
    public long getProjectModificationTime()            { return core.getUpdated(); }
    public String getPreferredLanguageLong()                { return core.getPreferredLanguageLong(); }
    public cmcProcEnums.BackdropType getBackDropType() { return core.getBackDropType(); }
    public cmcProcEnums.BROWSER getBrowser() { return core.getBrowser(); }
    
    //---  internal
    public long getSize() { return this.size; }
    public int getNumberOfFiles() { return this.nbrOfFiles; }
    public int getNumberOfArchives() { return this.nbrOfArchives; }
    public long getLastAccessed() { return this.lastAccessed; }
    public long getFirstAccessed() { return core.getCreated(); }
    //
   
    
    //---------------------------------------------------------------------------------
    public void fetchFileStats()
    //---------------------------------------------------------------------------------
    {
    	size = 0L;
		nbrOfFiles = 0;
		firstAccessed = 0L;
		lastAccessed = 0L;
    	doFileStats(getRootDir());
    	String pp = xMSet.xU.prntDateTime(lastAccessed,"yyyMMddHHmmss");
		lastAccessed = xMSet.xU.NaarLong(pp);
		do_log(5, "[NbrOfFiles=" + nbrOfFiles + "] [Size=" + size + "] [LastAccessed=" + lastAccessed +"]");
    }
    
    //---------------------------------------------------------------------------------
	private void doFileStats(String FolderName)
	//---------------------------------------------------------------------------------
	{
	
		File  dirObj = new File( FolderName  );
		{
			if ((dirObj.exists() == true)  ) {
				if (dirObj.isDirectory() == true) {
					File [] fileList = dirObj.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						// Afdalen
						if (fileList[i].isDirectory()) {
							doFileStats( fileList[i].getAbsolutePath());
						}
						if (fileList[i].isFile()) {
							    nbrOfFiles++;
							    if( fileList[i].getName().toUpperCase().trim().endsWith(".ZIP") ) nbrOfArchives++;
							    size += fileList[i].length();
							    if( lastAccessed < fileList[i].lastModified() ) lastAccessed = fileList[i].lastModified();	
						}
					}
				}
			}
		}	
		
	}
  	
}
