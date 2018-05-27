package tensorflow;

import logger.logLiason;
import monitor.cmcMonitorController;
import monitor.cmcMonitorItem;
import thread.ThreadDispatcher;
import thread.ThreadDispatcher.REQUESTED_CLASS;

import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcSettings;


public class cmcVRRunTensorFlow {

	private boolean MULTI_THREADED= true;
	
	
		
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private String PythonHome = null;
	private String PythonExe  = null;
	private String ErrorMsg   = null;
	
	private static String CLASSIFY_PYTHON_SCRIPT = "classifyParagraph.py";
	private static String RETRAINED_MODEL        = "comics_graph.pb";
	private static String RETRAINED_LABELS       = "comics_labels.txt";
	
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
    	ErrorMsg = sIn + "\n";
    	do_log(0,sIn);
    }
    
    //------------------------------------------------------------
    public String getErrorMessage()
    //------------------------------------------------------------
    {
      return ErrorMsg;	
    }
    
    // ---------------------------------------------------------------------------------
 	public cmcVRRunTensorFlow(cmcProcSettings iM, logLiason ilog)
    // ---------------------------------------------------------------------------------
 	{
 		xMSet = iM;
		logger = ilog;
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private void sleep(long lo)
 	// ---------------------------------------------------------------------------------
 	{
 		try {
 		 Thread.sleep(lo);
 		}
 		catch(Exception e ) { return;}
 	}

    // ---------------------------------------------------------------------------------
 	public boolean performVisualRecognition()
    // ---------------------------------------------------------------------------------
 	{
 	  //
 	  if( checkTensorFlowConfiguration() == false ) return false;
 	  //
 	  if( checkPythonScript() == false ) return false;
 	  //
 	  int maxiter = xMSet.getScanListSize();
 	  if( maxiter < 1 ) {
 		  do_error("There is nothing to process");
 		  return true;
 	  }
 	  //
 	  cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
	  moni.startMonitor(xMSet.getCurrentArchiveFileName());
	 
	  boolean ok=true;
	  try {
		  
		  //  NO THREAD VERSION
		  if( MULTI_THREADED == false ) {
			  long prevtime = System.currentTimeMillis();
			  for(int iter=0;iter<maxiter;iter++)
			  {
				  String ImageFileName = xMSet.popScanListItem();  // also sets the starttime
				  if( performTensorOnParagraph( moni , ImageFileName ) == false ) {
					  ok=false;
					  break;
				  }
				  // Ease on NIO
				  long lo = 500L - (System.currentTimeMillis() - prevtime);
				  if( lo > 0) sleep( lo+100L );
				  prevtime = System.currentTimeMillis();
 //if( iter == 2 ) { do_error("DEBUG BREAK"); break; }  
			  }
		  	}
		  
		    // THREAD
		    else {
		    	  ArrayList<ThreadMonitorDTO> threadlist = new ArrayList<ThreadMonitorDTO>();
		    	  ArrayList<cmcMonitorItem> monilist = xMSet.getMonitorList();
		    	  for(int iter=0;iter<monilist.size();iter++)
				  {
					cmcMonitorItem item = monilist.get(iter);
					String ImageFileName = item.getFileName(); 
	                //				
					ThreadMonitorDTO dto = new ThreadMonitorDTO();
					dto.setFieldIndex( iter );
					dto.setImageFileName(ImageFileName);
					//
					threadlist.add(dto);
				  }
		    	  //
		    	  ThreadDispatcher disp = new ThreadDispatcher( xMSet , logger , moni);
		    	  ok = disp.performDispatch( threadlist ,  REQUESTED_CLASS.CLASSIFY_PARAGRAPH );
		    	  disp=null;
		    	  // housekeeping
		    	  for(int i=0;i<threadlist.size();i++)
		    	  {
		    		String cmdfile = threadlist.get(i).getCommandFileName();
		    		if(  cmdfile == null ) continue;
		    		if( xMSet.xU.IsBestand( cmdfile ) ) {
		    		  if( xMSet.xU.VerwijderBestand( cmdfile ) == false )  {
		    			  do_error("Cannot remove cmd file [" + cmdfile + "]");
		    		  }
		    		}
		    	  }
		    }
		    
		  
	  }
	  catch(Exception e ) {
	 		do_log(1,"Exception occurred [" +  e.getMessage() + "]" );
	 		return false;
	  }
	  finally {
		  boolean ib = doHousekeeping();
		  if( ib == false ) ok=false;
	  }
	  moni.requestDelayedClose();
 	  return ok;
 	}
 	
	// ---------------------------------------------------------------------------------
 	private boolean performTensorOnParagraph(cmcMonitorController moni , String ImageFileName )
	// ---------------------------------------------------------------------------------
 	{
 		  ThreadMonitorDTO resp = null;
 	      moni.syncMonitor();
 		  cmcVRExecTensorFlowScript exc = new cmcVRExecTensorFlowScript( xMSet , logger );
 		  boolean ok = exc.execTensorFlowVR(ImageFileName , -1 );
 		  resp = exc.getTensorRespons();
 		  ok = (resp == null) ? false : resp.getExitStatus();
 		  updateMonitor(moni , resp , ImageFileName );
 		  performFeedback(resp);
 		  exc=null;
 		  return ok;
 	}
 	
 	
 	// TODO the same code is used in threaddispatcher 	
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
 	
    // ---------------------------------------------------------------------------------
 	private boolean doHousekeeping()
    // ---------------------------------------------------------------------------------
 	{
 		boolean ok=true;
 		int maxiter = xMSet.getScanListSize();
		for(int iter=0;iter<maxiter;iter++)
		{
		  cmcVRParagraph obj = (cmcVRParagraph)xMSet.getObjectFromScanList(iter);
		  if( obj != null ) {
			 String LongImageFileName = obj.getLongImageFileName();
			 if( xMSet.xU.IsBestand(LongImageFileName) == false ) continue;
			 if( xMSet.xU.VerwijderBestand(LongImageFileName) == false )  {
			   do_error("Could not remove [" + LongImageFileName + "]");
			   ok=false;
			 }
		  }
		}
 		return ok;
 	}
 	
    // ---------------------------------------------------------------------------------
 	private boolean checkTensorFlowConfiguration()
 	// ---------------------------------------------------------------------------------
 	{
 		// load settings
 		PythonHome = xMSet.getPythonHomeDir();
 		if( PythonHome == null ) {
 			do_error("Empty Python Folder");
 			return false;
 		}
 		//
 		if( xMSet.xU.IsDir( PythonHome ) == false ) {
 			do_error("PYTHON HOME Folder cannot be accessed [" + PythonHome + "]");
 			return false;
 		}
 		PythonExe = null;
 		switch( xMSet.getMyOS() )
 		{
 		case MSDOS : { PythonExe = "python.exe"; break; }
 		case LINUX : { PythonExe = "python"; break; }
 		default : { do_error("Unsupported Operating System"); return false; }
 		}
 		String PythonExeAbsolutePath = PythonHome + xMSet.xU.ctSlash + PythonExe;
 		if( xMSet.xU.IsBestand( PythonExeAbsolutePath ) == false ) {
 			do_error( "Could not find python executable [" + PythonExe + "] in PYTHON_HOME [" + PythonHome + "]");
 			return false;
 		}
 	
 	    // check Tensorflow dir 
 	    String TensorFolder = xMSet.getTensorDir();
 	    if( xMSet.xU.IsDir( TensorFolder ) == false ) {
 	    	do_error( "Cannot access TensorFlow folder  [" + TensorFolder + "]");
 			return false;
 	    }
 		// check presence of Tensorflow files retrained_graph
 	    String GraphName = TensorFolder + xMSet.xU.ctSlash + RETRAINED_MODEL;
 	    if( xMSet.xU.IsBestand( GraphName ) == false ) {
			do_error( "Could not find TensorFlow InceptionV3 [" + GraphName + "] Graph\n in TensorFlow folder [" + TensorFolder + "]");
			return false;
		}
 	    String LabelName = TensorFolder + xMSet.xU.ctSlash + RETRAINED_LABELS;
	    if( xMSet.xU.IsBestand( LabelName ) == false ) {
			do_error( "Could not find TensorFlow InceptionV3 [" + LabelName + "] Label definitions\n in TensorFlow folder [" + TensorFolder + "]");
			return false;
		}
 	    //
 		return true;
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private boolean checkPythonScript()
 	// ---------------------------------------------------------------------------------
 	{
 	   String PythonScript = xMSet.getTensorDir() + xMSet.xU.ctSlash + CLASSIFY_PYTHON_SCRIPT;
 	   if( xMSet.xU.IsBestand( PythonScript ) == false ) return createPythonScript(PythonScript);
 	   // check version
 	   String slines = xMSet.xU.ReadContentFromFile( PythonScript, 1000 , "ASCII");
 	   if( slines.indexOf( xMSet.getApplicDesc() ) >= 0 ) return true;
 	   do_log( 1 , "Python script is out of date" );
 	   return createPythonScript(PythonScript);
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private boolean createPythonScript( String ScriptName )
 	// ---------------------------------------------------------------------------------
 	{
 		do_error( "Create PYTHON script [" + ScriptName + "]");
 		return true;
 	}
 	
 
 	
}
