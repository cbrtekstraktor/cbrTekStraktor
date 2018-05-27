package thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import cbrTekStraktorModel.cmcProcSettings;
import logger.logLiason;


public class ConcurrencyController {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	
    private static int WAIT_FOR_LOCK_PERIOD   = 500;
	public enum SEMAPHORE_TYPE  {  AUTHOR , FOLLOWER }
	private static Semaphore sema = null;
	private String ErrorMsg=null;

	
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
    public ConcurrencyController(cmcProcSettings iM, logLiason ilog ,  SEMAPHORE_TYPE tp , Semaphore se)
    //------------------------------------------------------------
    {
    	xMSet = iM;
		logger = ilog;
		if( tp == SEMAPHORE_TYPE.AUTHOR ) {
			if( se != null ) do_log( 5, "Semaphore must not be provided - a mutex will be created");
			sema = new Semaphore(1);   // create MUTEX ie. semaphore is on or of 
		}
		if( tp == SEMAPHORE_TYPE.FOLLOWER ) {
			if( se == null ) do_error("Semaphore must be provided");
			else sema = se;
		}
    }
    
	//----------------------------------------------------------------
    public Semaphore getSemaphore()
	//----------------------------------------------------------------
    {
		return sema;
    }
	
	//----------------------------------------------------------------
    public boolean getLock()
    //----------------------------------------------------------------
    {
    	    try {
    	    	//logit( 5 , "Sema permits " + sema.availablePermits() );
    		    long nano = System.nanoTime();
			    boolean aquired = sema.tryAcquire( WAIT_FOR_LOCK_PERIOD , TimeUnit.MILLISECONDS);
                if ( aquired == false ) {
            	  do_error("Could not obtain a lock after [" + ((System.nanoTime() - nano) / 1000L) + "] micro seconds");
            	  return false;
                }
                return true;
			}
			catch(Exception e ) {
				do_error("Exception when trying to get a lock [" + e.getMessage() + "]");
				return false;
			}
    }
    
    //----------------------------------------------------------------
    public boolean unLock()
    //----------------------------------------------------------------
    {
    	if( sema == null ) return false;
    	sema.release();
    	return true;
    }
}
