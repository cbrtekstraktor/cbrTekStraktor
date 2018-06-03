package thread;

import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;
import monitor.cmcMonitorController;
import tensorflow.ThreadMonitorDTO;
import tensorflow.cmcVRParagraph;

public class ThreadCommonCode {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	
	ThreadMonitorDTO resp = null;
	
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
    	if( resp != null ) resp.setErrorMsg(sIn + "\n");
    	do_log(0,sIn);
    }
    
    //------------------------------------------------------------
    public String getErrorMessage()
    //------------------------------------------------------------
    {
      return resp == null ? "System Error : RESP class not instantiated" : resp.getErrorMsg();	
    }

    public ThreadCommonCode(cmcProcSettings iM, logLiason ilog)
    {
    	xMSet = iM;
		logger = ilog;
    }
    
    // ---------------------------------------------------------------------------------
  	public void performFeedback( ThreadMonitorDTO resp )
    // ---------------------------------------------------------------------------------
  	{
  	    if( resp == null ) return;
 	    cmcVRParagraph obj = (cmcVRParagraph)xMSet.getmoma().getObjectFromScanList(resp.getImageFileName());
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
 	      xMSet.getmoma().setObjectOnScanList( resp.getImageFileName() , obj);
 	    }
  	}
   	
    // ---------------------------------------------------------------------------------
  	public void updateMonitor(cmcMonitorController moni , ThreadMonitorDTO resp , String ImageFileName)
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
