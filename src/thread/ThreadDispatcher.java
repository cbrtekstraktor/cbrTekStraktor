package thread;

import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcSettings;
import generalpurpose.gpInterrupt;
import logger.logLiason;
import monitor.cmcMonitorController;
import tensorflow.ThreadMonitorDTO;
import tensorflow.cmcVRParagraph;
import tensorflow.ThreadMonitorDTO.INFOTRANSFERSTATUS;
import tensorflow.ThreadMonitorDTO.MONITORSTATUS;
import thread.ConcurrencyController.SEMAPHORE_TYPE;

public class ThreadDispatcher {

	
	public enum REQUESTED_CLASS { CLASSIFY_PARAGRAPH , UNKNOWN }

	private long MAX_DURATION = 10 * 60 * 1000L;   // 10 minutes
	private int MAX_NUM_THREADS = 5;
	private int MAX_THREAD_ERRORS = MAX_NUM_THREADS;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	cmcMonitorController moni=null;
	gpInterrupt irq=null;
	
	private String ErrorMsg=null;
	private int loglevel=9;

	
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
    	ErrorMsg=sIn + "\n";
    	do_log(0,sIn);
    }
    
    //------------------------------------------------------------
    public String getErrorMessage()
    //------------------------------------------------------------
    {
      return ErrorMsg;	
    }

    //------------------------------------------------------------
    public ThreadDispatcher(cmcProcSettings iM, logLiason ilog , cmcMonitorController imoni)
    //------------------------------------------------------------
    {
    	xMSet = iM;
		logger = ilog;
		loglevel = xMSet.getLogLevel();
		MAX_THREAD_ERRORS = MAX_NUM_THREADS = xMSet.getMaxThreads();
		moni = imoni;
		irq = new gpInterrupt(xMSet.getInterruptFileName());
    }

    //----------------------------------------------------------------
    public boolean performDispatch(ArrayList<ThreadMonitorDTO> list , REQUESTED_CLASS  requestedClass )
    //----------------------------------------------------------------
    {
    	ConcurrencyController locker = new ConcurrencyController(xMSet , logger , SEMAPHORE_TYPE.AUTHOR , null);
		for(int fieldIndex=0;fieldIndex<list.size();fieldIndex++)
		{
			ThreadMonitorDTO moni = list.get(fieldIndex);
			moni.setStatus(MONITORSTATUS.QUEUED);
		}
		long blockstarted = System.currentTimeMillis();
		long kicker = System.currentTimeMillis();
		boolean graceful=false;
		// MAY 2018
		boolean requestToDisplayProgress=false;
		int errorCount=0;
		boolean interrupted=false;
		//
		while ( (System.currentTimeMillis() - blockstarted) < MAX_DURATION)
		{
			requestToDisplayProgress=false;
			// Get a lock
			if( locker.getLock() == false ) break;
			//
		    boolean threadStarted=false;    	
			int activeThreads=0;
			int firstQueueIndex=-1;
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) && (firstQueueIndex<0) ) firstQueueIndex = i;
				if( (list.get(i).getStatus() == MONITORSTATUS.BUSY) || (list.get(i).getStatus() == MONITORSTATUS.STARTED) ) activeThreads++;
			}
			//
			// look for items where information is to be transferred - needs to happen before QUIT to get all stati correct
			for(int i=0;i<list.size();i++)
			{
			  if( (list.get(i).getStatus() != MONITORSTATUS.COMPLETED) && (list.get(i).getStatus() != MONITORSTATUS.FAILED) ) continue;
			  if( list.get(i).getTxstat() != INFOTRANSFERSTATUS.READY ) continue;
			  //
			  list.get(i).setTxstat( INFOTRANSFERSTATUS.TRANSFERRED );
			  // report back to monitor
    		  ReportBackToMonitor(list.get(i) );
    		  requestToDisplayProgress=true;
        	}
			
			// Quit ?
			if( (firstQueueIndex <  0) && (activeThreads==0) ) { // nothing to process and nothing active
				graceful=true;
				locker.unLock();
				break;
			}
			//
			if( (activeThreads < MAX_NUM_THREADS) && (firstQueueIndex >= 0)) {  // Start a thread
				list.get(firstQueueIndex).setStatus(MONITORSTATUS.STARTED);
				list.get(firstQueueIndex).setStarttime(System.currentTimeMillis());
				// Start the requested thread
				switch( requestedClass )
				{
				 case CLASSIFY_PARAGRAPH : {
					cmcVRTensorFlowScriptThread th = new cmcVRTensorFlowScriptThread( xMSet , logger , list.get(firstQueueIndex) , locker.getSemaphore() );
					th.start();				
					threadStarted=true;
					// 
					xMSet.setStartTimeOnScanList( list.get(firstQueueIndex).getImageFileName() );
					requestToDisplayProgress=true;
					//
					break;
				 }
				 default  : {
					do_error ("Unsupported Class requested for thread [" + requestedClass + "]");
					locker.unLock();
					return false;
				 }
				}
				
			}
			// debug
			/*
			int threadsToComplete=0;
			int nQueued=0;
			int nStarted=0;
			int nBusy=0;
			int nCompleted=0;
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) )  { threadsToComplete++; nQueued++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.STARTED) ) { threadsToComplete++; nStarted++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.BUSY) )    { threadsToComplete++; nBusy++; }
				if( (list.get(i).getStatus() == MONITORSTATUS.COMPLETED) ) {nCompleted++; }
			}
			*/
			
			// see if there are very long lasting threads
			errorCount=0;
			for(int i=0;i<list.size();i++)
			{
				if( (list.get(i).getStatus() == MONITORSTATUS.QUEUED) ) continue;
				if( (list.get(i).getStatus() == MONITORSTATUS.COMPLETED) ) continue;
				if( (list.get(i).getStatus() == MONITORSTATUS.FAILED) ) { errorCount++; continue; }
				//
				long runtime = System.currentTimeMillis() - list.get(i).getStarttime();
				if( ((runtime / 1000L) % 10L) == 9 ) {  // kicks in every 9 seconds
				 do_log(loglevel , "[Thread=" + i + "] -> [" + list.get(i).getStatus() + "] [RunTime=" + (runtime / 1000L) + " seconds] " + runtime );
				}
			}
			//
			locker.unLock();
			//
			if( errorCount >= MAX_THREAD_ERRORS ) break; 			
			//
			if( irq.gotInterrupt() ) { interrupted=true; break; }
			//
			if( requestToDisplayProgress ) {
				 if (moni != null) moni.syncMonitor();
			}
			//
			if( threadStarted ) continue;  // no sleep if you just started a thread
			// sleep
			try {
			 Thread.sleep(200);
			}
			catch(Exception e ) { do_error("Got exception " + xMSet.xU.LogStackTrace(e)); }
		}
		// clean
		for(int i=0;i<list.size();i++)
		{
			long runtime = list.get(i).getEndtime() - list.get(i).getStarttime();
			do_log(loglevel , "[File=" + list.get(i).getImageFileName() + "] [" + list.get(i).getStatus() + "] [RunTime=" + runtime + " milliSeconds]"   );
		}
		if( !graceful ) {
			if( interrupted ) do_error("Got an interrupt");
			else
			if( errorCount >= MAX_THREAD_ERRORS) do_error("Too many errors reached [" + MAX_THREAD_ERRORS + "]");
			else do_error("Thread monitor timed out");
			return false;
		}
		//
		int errors=0;
		for(int i=0;i<list.size();i++)
		{
			if( (list.get(i).getStatus() != MONITORSTATUS.COMPLETED) ) errors++;
		}
		if (moni != null) moni.syncMonitor();
	    return (errors ==0 ) ? true : false;
    }
    
    // ---------------------------------------------------------------------------------
  	private void  ReportBackToMonitor( ThreadMonitorDTO resp )
    // ---------------------------------------------------------------------------------
  	{
  		try {
  		  if ( resp == null ) return;
 		  performFeedback( resp );
		  updateMonitor( moni , resp , resp.getImageFileName() );
		  //xMSet.setStartTimeOnScanList( resp.getImageFileName() , resp.getStarttime() );
		  xMSet.setEndTimeOnScanList( resp.getImageFileName() , resp.getEndtime() );
		  moni.syncMonitorEnd( null);
  		}
  		catch(Exception e) {
  			do_error("Error on ReportBackToMonitor");
  		}
  	}
  	
  	// TODO - is identical to code in tensorscript
    // ---------------------------------------------------------------------------------
  	private void performFeedback( ThreadMonitorDTO resp )
    // ---------------------------------------------------------------------------------
  	{
  	    if( resp == null ) return;
 	    cmcVRParagraph obj = (cmcVRParagraph)xMSet.getObjectFromScanList(resp.getImageFileName());
 	    if( obj != null ) {
 	      obj.setNewTipe(resp.getTipeDeterminedViaTensor());
 	      double d=-1;
 	      switch( resp.getTipeDeterminedViaTensor() )
 	      {
 	      case TEXTPARAGRAPH: { d = resp.getTensorValidPercentage(); break; } 
 	      case PARAGRAPH: { d = resp.getTensorInvalidPercentage(); break; }
 	      default : break;
 	      }
 	      obj.setConfidence(d);
 	      xMSet.setObjectOnScanList( resp.getImageFileName() , obj);
 	    }
  	}
  	
    // ---------------------------------------------------------------------------------
  	private void updateMonitor(cmcMonitorController moni , ThreadMonitorDTO resp , String ImageFileName)
  	// ---------------------------------------------------------------------------------
  	{
  		 boolean ok = false;
  		 String threadErrorMsg="Unknown";
  		 if( resp != null ) { ok = resp.getExitStatus(); threadErrorMsg = resp.getErrorMsg(); }
  		 if( ok == false ) {
 			   moni.syncMonitorComment( ImageFileName ,threadErrorMsg );
 	 		   moni.syncMonitorEnd( ImageFileName );
 	 		   return;
 	 	  }
 		  else {
 			String comm = "" + resp.getTipeDeterminedViaTensor() + " [Valid=" + resp.getTensorValidPercentage() + "] [InValid=" + resp.getTensorInvalidPercentage() + "]";
 		    moni.syncMonitorComment( ImageFileName , comm );
 		    moni.syncMonitorEnd( ImageFileName );
 		  }   
  	}
    
}
