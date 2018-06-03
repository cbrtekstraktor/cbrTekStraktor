package monitor;


import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;

public class cmcMonitorController {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
    private cmcMonitorDialog monitorHandle=null;
	
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
 	public cmcMonitorController(cmcProcSettings is, logLiason ilog)
 	// ---------------------------------------------------------------------------------
 	{
 	   xMSet = is;
 	   logger = ilog;
 	}
 	
	//-----------------------------------------------------------------------
	public void startMonitor(String FolderName)
	//-----------------------------------------------------------------------
	{
		monitorHandle = new cmcMonitorDialog( null , xMSet , logger , FolderName );
		syncMonitor();
	}
	//-----------------------------------------------------------------------
	public void syncMonitorEnd(String BulkFileName)
	//-----------------------------------------------------------------------
	{
		if( BulkFileName == null ) return;
		if( xMSet.getmoma().setEndTimeOnScanList(BulkFileName) ) syncMonitor();
	}
	//-----------------------------------------------------------------------
	public void syncMonitorComment(String BulkFileName,String Comment)
	//-----------------------------------------------------------------------
	{
			if( (BulkFileName == null) || (Comment == null)  ) return;
			if( xMSet.getmoma().setCommentOnScanList(BulkFileName , Comment) ) syncMonitor();
	}
	//-----------------------------------------------------------------------
	public void syncMonitor()
	//-----------------------------------------------------------------------
	{
		ArrayList<cmcMonitorItem> pl = xMSet.getmoma().getMonitorList();
	    try {
		  if( monitorHandle == null ) return;
		  if( monitorHandle.hasBeenClosed() ) return;
		  for(int i= 0;i<pl.size();i++) monitorHandle.upsertMonitorItemLine(pl.get(i));
		  monitorHandle.endTransmissionAndGetFinal();
		}
		catch(Exception e ) {
			do_error("Cannot upsert to monitor [" + e.getMessage() );
		}
	}
	//-----------------------------------------------------------------------
    public void closeMonitor()
	//-----------------------------------------------------------------------
    {
    	try {
  		  if( monitorHandle == null ) return;
  		  monitorHandle.forceClose();
  		}
  		catch(Exception e ) {
  			do_error("Cannot close monitor [" + e.getMessage() );
  		}
    }
	//-----------------------------------------------------------------------
    public void requestDelayedClose()
	//-----------------------------------------------------------------------
    {
    	try {
    		  if( monitorHandle == null ) return;
    		  monitorHandle.requestDelayedClose();
    		}
    		catch(Exception e ) {
    			do_error("Cannot request to close monitor [" + e.getMessage() );
    		}
    }
}
