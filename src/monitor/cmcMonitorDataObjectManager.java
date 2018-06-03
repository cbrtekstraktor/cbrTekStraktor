package monitor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;
import tensorflow.cmcVRArchive;
import tensorflow.cmcVRMakeTrainingImages;
import tensorflow.cmcVRParagraph;

public class cmcMonitorDataObjectManager {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
   
	private ArrayList<cmcMonitorItem> scanList = null;
	private ArrayList<cmcMonitorItem> scanListBackup = null;

	private String scanFolder = null;
	private long creatime=0L;
	private boolean activeBulkProcess=false;
	
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
    public cmcMonitorDataObjectManager(cmcProcSettings is, logLiason ilog)
	//---------------------------------------------------------------------------------
    {
    	xMSet = is;
   	   logger = ilog;
    }
    
	//---------------------------------------------------------------------------------
	public void createScanList(String sDir)
	//---------------------------------------------------------------------------------
	{
		creatime=System.currentTimeMillis();
		scanFolder=sDir;
		scanList=null;
		scanList = new ArrayList<cmcMonitorItem>();
		ArrayList<String> list = xMSet.xU.GetFilesInDirRecursive(scanFolder,null);
		for(int i=0;i<list.size();i++)
		{
			String sF = list.get(i);
			if( xMSet.xU.isGrafisch(sF) == false  ) continue;
		    cmcMonitorItem x = new cmcMonitorItem((long)i);
		    x.setFileName(sF);
		    scanList.add(x);
		}
	}
	 
	//---------------------------------------------------------------------------------
	public void createOCRScanList(String sDir)
	//---------------------------------------------------------------------------------
	{
		    creatime=System.currentTimeMillis();
		    // create list of images
		    createScanList(sDir);
		    if( scanList == null ) return;
		    if( scanList.size() < 1 ) return;
		    // Look for a matching ZIP archive
		    String BCKOrigImageLongFileName = xMSet.getOrigImageLongFileName();
		    for(int i=0;i<scanList.size();i++)
		    {
		    	scanList.get(i).setComment("REMOVE");
		    	xMSet.setOrigImageLongFileName(scanList.get(i).getFileName());
		    	String FArchive = xMSet.getReportZipName();
		        if( FArchive == null ) continue;
		        if( xMSet.xU.IsBestand(FArchive) ) {
		            scanList.get(i).setComment( xMSet.xU.getFolderOrFileName(scanList.get(i).getFileName()) );
		            scanList.get(i).setFileName( FArchive );
		        }
		    }
		    xMSet.setOrigImageLongFileName(BCKOrigImageLongFileName);
		    //  remove images from list without matching zip
		    int aant = scanList.size();
		    for(int k=0;k<aant;k++)
		    {
		    	for(int i=0;i<scanList.size();i++)
		    	{
		    		if( scanList.get(i).getComment().compareToIgnoreCase("REMOVE") == 0 ) {
		    			scanList.remove(i);
		    			break;
		    		}
		    	}
		    }
		    //
    }
	
	//---------------------------------------------------------------------------------
	public void createImportScanList(String FName)
	//---------------------------------------------------------------------------------
	{
		    creatime=System.currentTimeMillis();
			scanFolder= xMSet.xU.getParentFolderName(FName);
			xMSet.setExportFileName(null);
			scanList=null;
			scanList = new ArrayList<cmcMonitorItem>();
			boolean validformat=false;
			try {
			  BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FName), xMSet.getCodePageString()));
	       	  //
	       	  int aantal=0;
	       	  String sLijn=null;
          	  while ((sLijn=reader.readLine()) != null) {
		           if( sLijn.trim().length() < 1) continue;
          		   //
          		   if( sLijn.trim().startsWith("<!--") ) {
          			 String sComp = xMSet.xU.keepLettersAndNumbers(sLijn).trim();
          		     if( sComp.toUpperCase().startsWith("FORMATVERSION") ) {
          			     sComp = xMSet.xU.keepNumbers(sLijn).trim();
          			     long vl = xMSet.xU.NaarLong(sComp);
          			     if( vl < cmcProcConstants.ExportFormatVersion ) {
          			    	 do_error("Export Format Version is obsolete [" + vl + "] requires [" + cmcProcConstants.ExportFormatVersion + "]");
          			     }
          			     else { validformat = true; xMSet.setExportFileName(FName); continue; }
          		     }
                     //          		 
          		     if( validformat == false ) continue;
            		 // extract CMXUID
          		     if( (sComp.startsWith("CMXUID")) && ( sComp.length() > ("CMXUID".length()+2) ) ) {
          		    	   //String sFile = sComp.substring("CMXUID".length());
          		    	   int l1 = sLijn.indexOf("[");
          		    	   int l2 = sLijn.indexOf("]");
          		    	   if( (l1<0) || (l2<0) || (l2<=l1) ) continue;
          		    	   String sFile = sLijn.substring(l1+1,l2);
          		    	   aantal++;
        				   cmcMonitorItem x = new cmcMonitorItem((long)aantal);
     			           x.setFileName(sFile);
     			           scanList.add(x); 	 
          		     }
          		   }
           	  }
          	  reader.close();
			}
			catch(Exception e ) {
		       do_error("Could not read [" + FName + "] " + e.getMessage() );		
			}
	}

	//---------------------------------------------------------------------------------
	public boolean createTensorTrainingSetList()
	//---------------------------------------------------------------------------------
	{
		creatime=System.currentTimeMillis();
		scanList=null;
		ArrayList<String> alist = xMSet.xU.GetFilesInDir( xMSet.getArchiveDir() , null );
		if( alist == null ) {
			do_error("Cannot read files in [" + xMSet.getArchiveDir() + "]");
			return false;
		}
		scanList = new ArrayList<cmcMonitorItem>();
		for(int i=0; i<alist.size(); i++)
		{
			if( alist.get(i).toUpperCase().trim().endsWith(".ZIP") ) {
      			cmcMonitorItem y = new cmcMonitorItem( (long)i );
    	    	y.setFileName( alist.get(i) );
    	    	scanList.add( y );
			}
		}
        return true;
	}
	
	//---------------------------------------------------------------------------------
	public boolean createTensorParagraphCheckList(String LongFName)
	//---------------------------------------------------------------------------------
	{
		    creatime=System.currentTimeMillis();
			scanList=null;
			String ShortFileName = xMSet.xU.GetFileName(LongFName);
			scanList = new ArrayList<cmcMonitorItem>();
		 	cmcMonitorItem x = new cmcMonitorItem(System.currentTimeMillis());
	    	x.setFileName( ShortFileName );
	    	x.setStarttime(System.currentTimeMillis());
	    	x.setProcessed(true);
	      	x.setComment("Busy");
	    	scanList.add(x);
	        //
			cmcVRMakeTrainingImages ms = new cmcVRMakeTrainingImages( xMSet , logger );
	    	boolean ib = ms.make_single_set( ShortFileName );
	    	if( ib ) {
	    	  ArrayList<cmcVRArchive> mlist = ms.getList();
	    	  scanList.get(0).setEndtime(System.currentTimeMillis());
	      	  scanList.get(0).setProcessed(true);
	      	  scanList.get(0).setComment("Failed");
		      if( mlist == null ) return false;
	    	  if( mlist.size() == 0 ) return false;
	    	  ArrayList<cmcVRParagraph> plist = mlist.get(0).getplist();
	    	  int ncnt = ( plist == null ) ? 0 : plist.size();
	      	  scanList.get(0).setComment("[" + ncnt + "] images extracted in [" + ((System.currentTimeMillis()-scanList.get(0).getStarttime())/1000L) + "] seconds");
              // do we also need to process the images extracted ?
	      	  if( xMSet.getTensorFlowPostProcessIndicator() ) {
	      		  scanList = null;   // 
	      		  scanList = new ArrayList<cmcMonitorItem>();
	      		  for(int i=0;i<plist.size();i++)
	      		  {
	      			cmcMonitorItem y = new cmcMonitorItem( plist.get(i).getUID() );
	    	    	y.setFileName( plist.get(i).getLongImageFileName() );
	    	    	y.setObject( plist.get(i) );
	    	    	scanList.add( y );
	    	    }
	      	  }
	    	}
	        ms=null;
	        return ib;
	}

	//---------------------------------------------------------------------------------
	public String getStatusTensorFlowSingleSet()
	//---------------------------------------------------------------------------------
	{
	     if( scanList == null) return "Could not start extraction";
	     if( scanList.size() < 1) return "Could not extract images";
	     return "[" + scanList.get(0).getFileName() + "] " + scanList.get(0).getComment();
	}
	
	//---------------------------------------------------------------------------------
	public String getScanFolder()
	//---------------------------------------------------------------------------------
	{
		return this.scanFolder;
	}
	
	//---------------------------------------------------------------------------------
	public int getScanListSize()
	//---------------------------------------------------------------------------------
	{
		if( scanList == null ) return 0;
		return scanList.size();
	}
	
	//---------------------------------------------------------------------------------
	public String popScanListItem()
	//---------------------------------------------------------------------------------
	{
		if( scanList == null ) return null;
		if( scanList.size() == 0 ) return null;
		for(int i=0;i<scanList.size();i++)
		{
//do_error( scanList.get(i).getFileName() + " " + scanList.get(i).getStarttime() );
			if ( scanList.get(i).getProcessed() == true ) continue;
			scanList.get(i).setProcessed(true);
			scanList.get(i).setStarttime(System.currentTimeMillis());
			return scanList.get(i).getFileName();
		}
	    estimateCompletionTime(false);	
		return null;
	}
	
	//---------------------------------------------------------------------------------
	private boolean setStartEndTimeOnScanList(String BulkFileName , int tipe , long tt)
	//---------------------------------------------------------------------------------
	{
		    long tset = ( tt <= 0L ) ? System.currentTimeMillis() : tt; 
			if( scanList == null ) return false;
			if( scanList.size() == 0 ) return false;
			for(int i=0;i<scanList.size();i++)
			{ 
				String FName =scanList.get(i).getFileName();
				if( FName == null ) continue;
				if( FName.compareToIgnoreCase(BulkFileName) == 0 ) {
					if( tipe == 0 ) scanList.get(i).setStarttime(tset);
					if( tipe == 1 ) scanList.get(i).setEndtime(tset);
					estimateCompletionTime(false);
					return true;
				}
			}
			return false;
	}
	
	//---------------------------------------------------------------------------------
	public boolean setStartTimeOnScanList(String BulkFileName)
	//---------------------------------------------------------------------------------
	{
		return setStartEndTimeOnScanList( BulkFileName , 0 , -1L);
	}

	//---------------------------------------------------------------------------------
	public boolean setEndTimeOnScanList(String BulkFileName)
	//---------------------------------------------------------------------------------
	{
		return setStartEndTimeOnScanList( BulkFileName , 1 , -1L);
	}
	//---------------------------------------------------------------------------------
	public boolean setStartTimeOnScanList(String BulkFileName , long tt)
	//---------------------------------------------------------------------------------
	{
		return setStartEndTimeOnScanList( BulkFileName , 0 , tt);
	}
	//---------------------------------------------------------------------------------
	public boolean setEndTimeOnScanList(String BulkFileName , long tt)
	//---------------------------------------------------------------------------------
	{
		return setStartEndTimeOnScanList( BulkFileName , 1 , tt);
	}
	//
	//---------------------------------------------------------------------------------
	public boolean setCommentOnScanList(String BulkFileName , String Comment)
	//---------------------------------------------------------------------------------
	{
			if( scanList == null ) return false;
			if( scanList.size() == 0 ) return false;
			for(int i=0;i<scanList.size();i++)
			{ 
				String FName =scanList.get(i).getFileName();
				if( FName == null ) continue;
				if( FName.compareToIgnoreCase(BulkFileName) == 0 ) {
					scanList.get(i).setComment(Comment);
					return true;
				}
			}
			return false;
	}
	//
	//---------------------------------------------------------------------------------
	public Object getObjectFromScanList(String BulkFileName )
	//---------------------------------------------------------------------------------
	{
			if( scanList == null ) return null;
			if( scanList.size() == 0 ) return null;
			for(int i=0;i<scanList.size();i++)
			{ 
				String FName =scanList.get(i).getFileName();
				if( FName == null ) continue;
				if( FName.compareToIgnoreCase(BulkFileName) == 0 ) {
					return scanList.get(i).getObject();
				}
			}
			return null;
	}
	//
	//---------------------------------------------------------------------------------
	public Object getObjectFromScanList(int idx )
	//---------------------------------------------------------------------------------
	{
			if( scanList == null ) return null;
			if( scanList.size() <= idx ) return null;
			return scanList.get(idx).getObject();
	}
	//
	//---------------------------------------------------------------------------------
	public boolean setObjectOnScanList(String BulkFileName , Object obj)
	//---------------------------------------------------------------------------------
	{
			if( scanList == null ) return false;
			if( scanList.size() == 0 ) return false;
			for(int i=0;i<scanList.size();i++)
			{ 
				String FName =scanList.get(i).getFileName();
				if( FName == null ) continue;
				if( FName.compareToIgnoreCase(BulkFileName) == 0 ) {
					scanList.get(i).setObject(obj);
		//do_error( "SET => " + obj.toString() );
					return true;
				}
			}
			return false;
	}

	//---------------------------------------------------------------------------------
	public ArrayList<cmcMonitorItem> getMonitorList()
	//---------------------------------------------------------------------------------
	{
	   return scanList;
	}

	//---------------------------------------------------------------------------------
	public String estimateCompletionTime(boolean extrapolateNow)
	//---------------------------------------------------------------------------------
	{
		activeBulkProcess=false;
		try {
		  long minStartTime=-1L;
		  long maxEndTime=-1L;
		  int ncompleted=0;
		  for(int i=0;i<scanList.size();i++)
		  {
			if( scanList.get(i).getStarttime() <= 0L ) continue;
			if( scanList.get(i).getEndtime() >  0L ) ncompleted++;
			if( (scanList.get(i).getStarttime() < minStartTime) || (minStartTime < 0L) ) minStartTime = scanList.get(i).getStarttime();
			if( scanList.get(i).getEndtime() > maxEndTime ) maxEndTime = scanList.get(i).getEndtime();
		  }
		  activeBulkProcess = (ncompleted == scanList.size() ) ? false : true; 
		  long elapsed = 0L;
		  double speed = (double)0.0001;    // 1 item per 10 seconds  ->  1 / 10000 msec
		  if( (maxEndTime > minStartTime) && (minStartTime > 0L) && (ncompleted > 0) ) {
			elapsed = maxEndTime - minStartTime;
			speed = (double)ncompleted / (double)elapsed;
		  }
		  double total  = (double)scanList.size() / speed;
		  if( extrapolateNow ) {
			  if( minStartTime < 1L ) minStartTime = creatime;
			  elapsed = System.currentTimeMillis() - minStartTime;
		  }
		  double remaining = total - (double)elapsed;
		  double progressPerc =  (double)elapsed / total;
		  //do_log( 9 , "[Elapsed=" + elapsed + "] [Remaining=" + (int)remaining + "] [Progress=" + progressPerc + "]" );
		  return "" + (int)(progressPerc * 100) + "%";  //   [" + secoDisplay(estimated) + "]";
		}
		catch(Exception e ) { return "?"; }
	}


	//---------------------------------------------------------------------------------
	public boolean isThereABulkProcessRunning()
	//---------------------------------------------------------------------------------
	{
		if( this.scanList == null ) return false;
		if( activeBulkProcess ) estimateCompletionTime(false);   // just make sure to have latest state
		return activeBulkProcess;
	}
    
	
	public void putMonitorOnStack()
	{
		scanListBackup = new ArrayList<cmcMonitorItem>();
		if( scanList == null ) return;
		for(int i=0;i<scanList.size();i++)
		{
			scanListBackup.add( scanList.get(i) );
		}
		scanList=null;
	}
	
	public void popMonitorFromStack()
	{
		scanList = new ArrayList<cmcMonitorItem>();
		if( scanListBackup == null ) return;
		for(int i=0;i<scanListBackup.size();i++)
		{
			scanList.add( scanListBackup.get(i) );
		}
		scanListBackup=null;
    }

}
