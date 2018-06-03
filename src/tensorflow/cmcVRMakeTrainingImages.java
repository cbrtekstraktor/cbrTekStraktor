package tensorflow;

import generalImagePurpose.cmcImageRoutines;
import generalImagePurpose.gpLoadImageInBuffer;

import java.util.ArrayList;
import java.util.Random;

import logger.logLiason;
import monitor.cmcMonitorController;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcGraphPageDAO;
import drawing.cmcGraphPageObject;

public class cmcVRMakeTrainingImages {

	enum OPERATING_MODE {  UNKNOWN , MAKE_TRAINING_SET , EXTRACT_ALL_PARAGRAPHS }
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private static int MIN_ZIP_FILES = 10;
	private static int VR_WIDTH = 299;   // size of the Tensorflow image
	private static int VR_HEIGTH = 299;
	private static String Page_UID=null;
	private static int FileCounter=0;
	private static int FileAttempts=0;
	private static OPERATING_MODE operating_mode = OPERATING_MODE.UNKNOWN;
	
	ArrayList<cmcVRArchive> moniList=null;
	
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
	public cmcVRMakeTrainingImages(cmcProcSettings is,logLiason ilog)
	//------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
		moniList=null;
	}

	
	public ArrayList<cmcVRArchive> getList()
	{
		return moniList;
	}
	
	//------------------------------------------------------------
	private boolean toss()
	//------------------------------------------------------------
	{
		  Random rn = new Random();
		  int[] ar_eyes = new int[3];
		  for(int i=0;i<ar_eyes.length;i++) ar_eyes[i]=-1;
		  int valid=0;
		  for(int k=0;k<10000;k++)
		  {
			  int eyes = rn.nextInt(6)+1;
			  for(int j=0;j<ar_eyes.length;j++)
		      {
			   if( ar_eyes[j] == eyes ) break;
			   if( ar_eyes[j] == -1 ) { ar_eyes[j] = eyes; valid++; break;} 
		      }
		      if( valid >= 3 ) break;
		  }
		  //
		  int tno = (int)((System.currentTimeMillis() % 97L) % 6L) + 1;
		  boolean match=false;
		  for(int i=0;i<ar_eyes.length;i++)
		  {
			  if( tno == ar_eyes[i] ) { match=true; break; }
		  }
		  //String sl= "[Target=" + tno + "] [" + match + "] ";
	      //for(int i=0;i<ar_eyes.length;i++) sl += "[" + ar_eyes[i] + "]";
	      //do_log( 9 , sl );
	      return match;
	}

	//------------------------------------------------------------
	public boolean make_single_set(String ShortArchiveFileName)
	//------------------------------------------------------------
	{
		 operating_mode = OPERATING_MODE.EXTRACT_ALL_PARAGRAPHS;
		 FileCounter=0;
		 FileAttempts=0;
		 moniList=null;
		 long startt = System.currentTimeMillis();
		 //
		 if( ShortArchiveFileName == null ) {
			 do_error( "ArchiveFileName is null");
			 return false;
		 }
		 String LongArchiveFileName = xMSet.getArchiveDir() + xMSet.xU.ctSlash + ShortArchiveFileName;
		 if( xMSet.xU.IsBestand(LongArchiveFileName) == false ) {
			 do_error("Cannot locate Archive file [" + LongArchiveFileName + "]");
			 return false;
		 }
		 moniList = new ArrayList<cmcVRArchive>();
		 cmcVRArchive x = new cmcVRArchive(ShortArchiveFileName);
		 moniList.add(x);
		 boolean ib = make_set_from_zip( x );
		 //do_error( "OK 2" + ib );
		 do_log(9,"[" + FileCounter + "] images created in [" + ((System.currentTimeMillis() - startt) / 1000L) + "] seconds [Stat=" + ib + "]");
		 return ib;
	}

	// use when run without the bulk load  monitor
	//------------------------------------------------------------
	public boolean make_complete_set()
	//------------------------------------------------------------
	{
		operating_mode = OPERATING_MODE.MAKE_TRAINING_SET;
		FileCounter=0;
		FileAttempts=0;
		moniList=null;
		long startt = System.currentTimeMillis();
		ArrayList<String> alist = xMSet.xU.GetFilesInDir( xMSet.getArchiveDir() , null );
		if( alist == null ) {
			do_error("Cannot read files in [" + xMSet.getArchiveDir() + "]");
			return false;
		}
		moniList = new ArrayList<cmcVRArchive>();
		for(int i=0; i<alist.size(); i++)
		{
			if( alist.get(i).toUpperCase().trim().endsWith(".ZIP") ) {
				cmcVRArchive x = new cmcVRArchive(alist.get(i));
				moniList.add( x );
			}
		}
	
		if( moniList.size() < MIN_ZIP_FILES) {
			do_error("There are at least [" + MIN_ZIP_FILES + "] files needed to create a trainingset");
			moniList=null; alist=null;
			return true;
		}
		//
		boolean ok=true;
		for(int i=0; i<moniList.size(); i++)
		{
			ok = make_set_from_zip( moniList.get(i) );
			//do_error( "OK 1" + ok );
			moniList.get(i).setStopt ( System.currentTimeMillis() );
			if( !ok ) break;
		}
		moniList=null;
		alist=null;
		//
		long stopt = System.currentTimeMillis();
		int perc = (int)((double)FileCounter * (double)100 / (double)FileAttempts);
		do_log(9,"Training set of [" + FileCounter + "] images created out of [" + FileAttempts + "] attemps in [" + ((stopt - startt) / 1000L) + "] seconds [Hits=" + perc + "%]");
	    return ok;	
	}
	
	private void sleep(long lo)
 	{
 		try {
 		 Thread.sleep(lo);
 		}
 		catch(Exception e ) { return;}
 	}

	//------------------------------------------------------------
	public boolean make_training_set_via_monitor()
	//------------------------------------------------------------
	{
		boolean ok=true;
		operating_mode = OPERATING_MODE.MAKE_TRAINING_SET;
		FileCounter=0;
		FileAttempts=0;
		moniList=null;
		moniList = new ArrayList<cmcVRArchive>();
		long startt = System.currentTimeMillis();
		//
		cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
		moni.startMonitor(xMSet.getExportFileName());
     	int maxiter = xMSet.getmoma().getScanListSize();
   	    for(int iter=0;iter<maxiter;iter++)
		{
		  String ShortArchiveName = xMSet.getmoma().popScanListItem();  // also sets the starttime
		  if( ShortArchiveName == null ) continue;
		  cmcVRArchive x = new cmcVRArchive( ShortArchiveName );
		  moniList.add( x );
          //
		  ok = make_set_from_zip( moniList.get(moniList.size()-1) );
		  //do_error( "OK 3" + ok );
		  moniList.get(moniList.size()-1).setStopt(System.currentTimeMillis());
		  if( ok == false ) break;
		  int nfi= moniList.get(moniList.size()-1).getplist() == null ? 0 : moniList.get(moniList.size()-1).getplist().size();
		  //
		  moni.syncMonitor();
 		  moni.syncMonitorComment(ShortArchiveName, "[" +nfi + "] Images created");
 		  moni.syncMonitorEnd(ShortArchiveName);
		}
		moni.requestDelayedClose();
		moniList=null;
		long stopt = System.currentTimeMillis();
		int perc = (int)((double)FileCounter * (double)100 / (double)FileAttempts);
		do_log(9,"Training set of [" + FileCounter + "] images created out of [" + FileAttempts + "] attemps in [" + ((stopt - startt) / 1000L) + "] seconds [Hits=" + perc + "%]");
		return ok;
	}
	
	//------------------------------------------------------------
	private boolean make_set_from_zip(cmcVRArchive moni )
	//------------------------------------------------------------
	{
		String LongFileName = xMSet.getArchiveDir() + xMSet.xU.ctSlash + moni.getShortArchiveFileName();
		//
		cmcGraphPageDAO dao = new cmcGraphPageDAO( xMSet , LongFileName , logger );
		cmcGraphPageObject[] ar_pabo = dao.readXML();
        if( ar_pabo == null ) {
        	do_error("Could not load archive file [" + LongFileName + "]");
        	return false;
        }
        // UID
        Page_UID = dao.getUID();
        // load the image
        String OriginalImageName = dao.getOrigFileDir() + xMSet.xU.ctSlash + dao.getOrigFileName();
        if( xMSet.xU.IsBestand( OriginalImageName ) == false ) {
        	do_error("Could not find image [" + OriginalImageName + "]");
        	return false;
        }
        gpLoadImageInBuffer lo = new gpLoadImageInBuffer( xMSet.xU , logger );
		int[] pagepicture = lo.loadBestandInBuffer(OriginalImageName);  
        // checks
		int realWidth = lo.getBreedte();
		int realHeigth = lo.getHoogte();
		if( realWidth != dao.getImageWidth() ) {
			do_error("There is a discrepancy between WIDTH on metadata and image characteristics [" + dao.getImageWidth() + "] [" + realWidth + "]");
			return false;
		}
		if( realHeigth != dao.getImageHeigth() ) {
			do_error("There is a discrepancy between HEIGTH on metadata and image characteristics [" + dao.getImageHeigth() + "] [" + realHeigth + "]");
			return false;
		}
		do_log( 9 , "Loaded image [" + OriginalImageName + "]");
	    // extract the PARAGRAPHs
        int k=0;
        for(int i=0;i<ar_pabo.length;i++)
        {
        	if( ar_pabo[i].removed ) continue;
			if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) || (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) {
			
		        if( operating_mode == OPERATING_MODE.MAKE_TRAINING_SET )
				{
        		  if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) ) {
        			if (k <= 5) continue;  // write 5 texts for 1 object paragraph
        			k=0;
        		  }
        		  else k++;
  			      FileAttempts++;
        		  if( !toss() ) continue;
				}
		        //
				else
				if( operating_mode == OPERATING_MODE.EXTRACT_ALL_PARAGRAPHS )
				{
				   FileAttempts++;
				}
				else
				{
				  do_error("Invalid operating mode [" + operating_mode + "]");
				  return false;
				}
		        //
		        cmcVRParagraph pmoni = new cmcVRParagraph(ar_pabo[i].UID);
		        pmoni.setTipe(ar_pabo[i].tipe);
		        moni.getplist().add(pmoni);
				//	
        	    String FName = doExtractAndDump( pagepicture , realWidth , ar_pabo[i] , dao.getPayLoadX() , dao.getPayLoadY() );
        	    if( FName == null ) return false;
        	    moni.getplist().get(moni.getplist().size() - 1).setLongImageFileName(FName);
        	    //do_error( ""+ moni.plist.get(moni.plist.size() - 1).UID + " " + moni.plist.get(moni.plist.size() - 1).LongImageFileName );
        	}
        }
        //
        lo=null;
        pagepicture=null;
        dao = null;
        ar_pabo=null;
		//
		return true;
	}
	
	//------------------------------------------------------------
	private String doExtractAndDump(int[] pic , int imgWidth , cmcGraphPageObject pabo , int payloadx , int payloady )
	//------------------------------------------------------------
	{
	  	//do_error( "[" + pabo.BundelIdx + " " + pabo.ClusterIdx + " " + pabo.tipe );
	  	int paraWidth  = pabo.MaxX - pabo.MinX;
	  	int paraHeigth = pabo.MaxY - pabo.MinY;
	  	int MinX = pabo.MinX;
	  	int MinY = pabo.MinY;
	  	// crop the paragraph so that it fits in the VR image
	  	if( paraWidth > VR_WIDTH ) {
	  		MinX = (paraWidth - VR_WIDTH) / 2;
	  		paraWidth = VR_WIDTH;
	  	}
	  	if( paraHeigth > VR_HEIGTH ) {
	  		MinY = (paraHeigth - VR_HEIGTH) / 2;
	  		paraHeigth = VR_HEIGTH;
	  	}
	  	//
	  	int[] extract = new int[ paraWidth * paraHeigth];
	  	int[] curated = null;
	  	//
	  	try {
	  	  int z=0;
	  	  for(int i=0;i<paraHeigth;i++)
	  	  {
	  	    int y = (MinY + payloady + i) * imgWidth;
	  	    for(int j=0;j<paraWidth;j++)
	  	    {
	  		  int k = y + MinX + payloadx + j;
	  		  extract[ z ] = pic[ k ];
	  		  z++;
	  	    }
	  	  }
	  	  
	  	  // resize to 299x299
	  	  int xmargin = (paraWidth < VR_WIDTH  ) ? ( VR_WIDTH - paraWidth ) / 2  : 0;
	  	  int ymargin = (paraHeigth < VR_HEIGTH) ? ( VR_HEIGTH - paraHeigth ) / 2 : 0;
	  	  curated = new int[ VR_WIDTH * VR_HEIGTH ];
	  	  for(int i=0;i<curated.length;i++) curated[i] = 0x00ffffff;
	  	  z=0;
	  	  for(int i=0;i<paraHeigth;i++)
	  	  {
	  		  int y = i * paraWidth;
	  		  z = ((ymargin + i) * VR_WIDTH ) + xmargin; 
	  		  for(int j=0;j<paraWidth;j++)
	  		  {
	  			  int k = y + j;
	  			  curated[ z ] = extract[k];
	  			  z++;
	  		  }
	  	  }
	  	  
	  	}
	  	catch( Exception e) {
	  		do_error("doExtractAndDump error [" + e.getMessage() + "]" );
	  		return null;
	  	}
	    //
	  	String DestDir = xMSet.getCorpusDir() + xMSet.xU.ctSlash + "Images";
	  	String prefix = pabo.tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH ? "PTxt_" : "PObj_";
	  	String sPUID = xMSet.xU.Remplaceer( Page_UID.substring(0,9) , "-" , "").trim().toLowerCase();
		String OutputFileName = DestDir + xMSet.xU.ctSlash + prefix + sPUID + "_" + pabo.UID + ".jpg";
	    if( xMSet.xU.IsBestand( OutputFileName ) ) {
	    	if( xMSet.xU.VerwijderBestand( OutputFileName ) == false ) {
	    		do_error("Cannot remove [" + OutputFileName + "]");
	    		return null;
	    	}
	    }
	  	// write image
	  	cmcImageRoutines irout = new cmcImageRoutines(logger);
	    irout.writePixelsToFile( curated , VR_WIDTH , VR_HEIGTH , OutputFileName , cmcImageRoutines.ImageType.RGB );
	  	//    
	    irout=null;
		extract=null;
		curated=null;
		if( xMSet.xU.IsBestand( OutputFileName ) == false ) {
			do_error("Image file could not be created [" + OutputFileName + "]");
			return null;
		}    
		FileCounter++;
		do_log( 9 , "Created [" + OutputFileName + "]");
		return OutputFileName;
	}
	
}
