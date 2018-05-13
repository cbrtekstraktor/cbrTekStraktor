package tensorflow;

import logger.logLiason;
import monitor.cmcMonitorController;
import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import generalpurpose.gpExecBatch;
import generalpurpose.gpPrintStream;

public class cmcVRRunTensorFlow {

	private static boolean SIMULATOR = false;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private String PythonHome = null;
	private String PythonExe  = null;
	private String ErrorMsg   = null;
	
	private static String CLASSIFY_PYTHON_SCRIPT = "classifyParagraph.py";
	private cmcProcEnums.PageObjectType TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.UNKNOWN;
	private double TensorValidPercentage=0;
	private double TensorInvalidPercentage=0;
	
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
 	  cmcMonitorController moni = new cmcMonitorController(xMSet,logger);
	  moni.startMonitor(null);
	  boolean ok=true;
	  try {
		long prevtime = System.currentTimeMillis();
		int maxiter = xMSet.getScanListSize();
		for(int iter=0;iter<maxiter;iter++)
		{
		  String ImageFileName = xMSet.popScanListItem();  // also sets the starttime
		  moni.syncMonitor();
 		  if( execTensorFlowVR( ImageFileName ) == false ) {
 			   ok=false;
 			   moni.syncMonitorComment( ImageFileName , getErrorMessage());
 	 		   moni.syncMonitorEnd( ImageFileName );
 	 		   break;
 	 	  }
 		  else {
 			String comm = "" + TipeDeterminedViaTensor + " [Valid=" + TensorValidPercentage + "] [" + TensorInvalidPercentage + "]";
 		    moni.syncMonitorComment( ImageFileName , comm );
 		    moni.syncMonitorEnd( ImageFileName );
 		    // feedback
 		    cmcVRParagraph obj = (cmcVRParagraph)xMSet.getObjectFromScanList(ImageFileName);
		    if( obj != null ) {
		      obj.setNewTipe(TipeDeterminedViaTensor);
		      double d=-1;
		      switch( TipeDeterminedViaTensor )
		      {
		      case TEXTPARAGRAPH: { d = TensorValidPercentage; break; } 
		      case PARAGRAPH: { d = TensorInvalidPercentage; break; }
		      default : break;
		      }
		      obj.setConfidence(d);
		      xMSet.setObjectOnScanList( ImageFileName , obj);
		    }
 		  }
 		  // Ease on NIO
 		  long lo = 500L - (System.currentTimeMillis() - prevtime);
 		  if( lo > 0) sleep( lo+100L );
 		  prevtime = System.currentTimeMillis();
 //if( iter == 2 ) break;		  
		}
	  }
	  catch(Exception e ) {
	 		do_log(1,"Excpetion occurred [" +  e.getMessage() + "]" );
	 		return false;
	  }
	  finally {
		  ok=doHousekeeping();
	  }
	  moni.requestDelayedClose();
 	  return ok;
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
 	    String GraphName = TensorFolder + xMSet.xU.ctSlash + "retrained_graph.pb";
 	    if( xMSet.xU.IsBestand( GraphName ) == false ) {
			do_error( "Could not find TensorFlow InceptionV3 [" + GraphName + "] Graph\n in TensorFlow folder [" + TensorFolder + "]");
			return false;
		}
 	    String LabelName = TensorFolder + xMSet.xU.ctSlash + "retrained_labels.txt";
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
 		do_error( "Create PYTHON " + ScriptName );
 		return true;
 	}
 	
    // ---------------------------------------------------------------------------------
 	private boolean createBatchFile( String ImageFileName )
    // ---------------------------------------------------------------------------------
 	{
 	  switch( xMSet.getMyOS() )
 	  {
 	  case MSDOS : return createMSDOSBatchFile(ImageFileName);
 	  case LINUX : return createLINUXBatchFile(ImageFileName);
 	  default : { do_error("Operating system not supported"); return false; }
 	  }
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private String getTensorBatchName()
 	// ---------------------------------------------------------------------------------
 	{
 		 return xMSet.getTensorDir() + xMSet.xU.ctSlash + "RunTensorFlow.cmd";
 	}
 	
    // ---------------------------------------------------------------------------------
 	private boolean createMSDOSBatchFile( String ImageFileName )
    // ---------------------------------------------------------------------------------
 	{
 	   String BatchName = getTensorBatchName();
 	   if( xMSet.xU.IsBestand(BatchName) ) {
 		   xMSet.xU.VerwijderBestand( BatchName );
 		   if( xMSet.xU.IsBestand(BatchName) ) {
 			  do_error("Cannot remove batch file [" + BatchName + "]");
 			  return false;
 		   }
 	   }
 	   String TensorFolder = xMSet.getTensorDir();
 	   gpPrintStream script = new gpPrintStream( BatchName , "ASCII");
 	   
 	   script.println("@ECHO OFF" );
 	   script.println("REM Generated by " + xMSet.getApplicDesc() );
	   script.println("REM Generated on " + xMSet.xU.prntStandardDateTime(System.currentTimeMillis()).toUpperCase() );
 	   //
	   script.println("SET PYTHON_HOME=\"" + PythonHome + "\"" );
	   script.println("PATH=%PYTHON_HOME%;%PYTHON_HOME%\\Scripts;%PATH%" );
	   //
	   script.println("set TNSRDIR=\"" + TensorFolder + "\"");
	   script.println("set PYTHPROG=\"" + TensorFolder + "\"");
 	   script.println("SET IMAGEFILENAME=\"" + ImageFileName + "\"");
 	   //
 	   script.println("CD %TNSRDIR%");
 	   if( SIMULATOR ) {
 		 script.println("ECHO --[CBRTEKSTRAKTOR START]--");
 		 script.println("ECHO validbubble (score = 0." + System.currentTimeMillis() % 4389 + " )--");
 		 script.println("ECHO invalidbubble ( score = 0." + System.currentTimeMillis() % 7823 + " )--");
 		 script.println("ECHO --[CBRTEKSTRAKTOR END]--");
 	   }
 	   else {
 		 script.println("python %PYTHPROG%\\" + CLASSIFY_PYTHON_SCRIPT + " %IMAGEFILENAME%");
 	   }
 	   script.println(" ");   // You need an extra blank line - streamgobbler always misses the last line
 	   //
 	   script.close();
 	   return true;	
 	}

    // ---------------------------------------------------------------------------------
 	private boolean createLINUXBatchFile( String ImageFileName )
    // ---------------------------------------------------------------------------------
 	{
 	   do_error("TODO Linux shell script still needs to be created");	
 	   return false;
 	}

    // ---------------------------------------------------------------------------------
 	private boolean execTensorFlowVR(String ImageFileName)
    // ---------------------------------------------------------------------------------
 	{
 	    TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.UNKNOWN;
 	    TensorValidPercentage=-1;
 		TensorInvalidPercentage=-1;
 		//
 	 	if( createBatchFile( ImageFileName) == false ) return false;
 		//
 		gpExecBatch exec = new gpExecBatch( getTensorBatchName() , logger );
 		int exitlevel = exec.getExitLevel();
    	int stdoutCounter = exec.getSTDOUT() == null ? 0 : exec.getSTDOUT().size();
    	int stderrCounter = exec.getSTDERR() == null ? 0 : exec.getSTDERR().size();
    	//
    	do_log(1,"[Exit=" + exitlevel  + "] [#stdout=" + stdoutCounter + "] [#stderr=" + stderrCounter + "]");
    	if( exitlevel != 0 ) {
    	  String sLog = "";
    	  ArrayList<String> lret = null;
    	  lret = exec.getSTDOUT();
    	  int MAXLEN=150;
    	  if( lret != null ) {
    	   for(int i=0;i<lret.size();i++) {
    		   String sLine = lret.get(i);
    		   if( sLine == null ) continue;
    		   do_log(1,"STDOUT -> " + sLine);
    		   if( sLine.length() > MAXLEN ) sLine = sLine.substring(0,MAXLEN);
    		   sLog += "\n" + sLine; 
    	   }
    	  }
    	  lret = exec.getSTDERR();
    	  if( lret != null ) {
    		  for(int i=0;i<lret.size();i++) {
       		   String sLine = lret.get(i);
       		   if( sLine == null ) continue;
       		   do_log(1,"STDERR -> " + sLine);
       		   if( sLine.length() > MAXLEN ) sLine = sLine.substring(0,MAXLEN);
       		   sLog += "\n" + sLine; 
       	   }
    	  }
    	  do_error("Error when executing [" + getTensorBatchName() + "] [" + exitlevel + "]" + sLog);
    	  return false;
        }
    	// Parse the results
        if( ParseResults( exec.getSTDOUT() ) == false ) return false;
        //
    
  		return true;
 	}
 	
 	//---------------------------------------------------------------------------------
 	public String keepNumbers ( String sIn )
 	//---------------------------------------------------------------------------------
 	{   
 			    char[] sBuf = sIn.toCharArray();
 			    String sRet="";		
 				for(int i=0;i<sBuf.length;i++) 
 				{	
 					if( ((sBuf[i]>='0')&&(sBuf[i]<='9')) ) sRet += sBuf[i]; 
 				}		
 				return sRet;
 	}
 	
   
 	//---------------------------------------------------------------------------------
 	private boolean ParseResults(ArrayList<String> lret)
 	//---------------------------------------------------------------------------------
 	{
 	    if( lret == null ) {
 	    	do_error("ParseResult - NULL input");
 	    	return false;
 	    }
 	    int oklevel=0;
 	    String sLog="";
 	    double ValidPerc=-2;
 	    double InValidPerc=-1;
 	    for(int i=0;i<lret.size();i++)
 	    {
 	    	String sLine = lret.get(i);
 	    	if( sLine == null ) continue;
 	    	sLine = sLine.trim();
 	    	if( sLine.length() < 1 ) continue;
 	    	sLog += sLine;
 	        if( sLine.toUpperCase().indexOf("--[CBRTEKSTRAKTOR START]--") >= 0 ) {
 	        	oklevel=1;
 	        	continue;
 	        }
 	        if( oklevel == 0 ) continue;
 	        // Caution check INVALID first and then VALID
 	        if( (sLine.toUpperCase().indexOf("INVALIDBUBBLE") >= 0) && (sLine.toUpperCase().indexOf("SCORE") >= 0) ) {
	        	double d = extractScore(sLine);
	        	if( d < 0 ) continue;
	        	oklevel++;
	        	InValidPerc=d;
	        	continue;
	        }    
 	        if( (sLine.toUpperCase().indexOf("VALIDBUBBLE") >= 0) && (sLine.toUpperCase().indexOf("SCORE") >= 0) ) {
 	        	double d = extractScore(sLine);
 	        	if( d < 0 ) continue;
 	        	oklevel++;
 	        	ValidPerc=d;
 	        	continue;
 	        }
 	        if( (sLine.toUpperCase().indexOf("--[CBRTEKSTRAKTOR END]--") >= 0) && (oklevel== 3) ) {
	        	oklevel=4;
	        }
 	    }
 	    if( oklevel != 4 ) {
 	    	do_error("Could not interpret the output correcly [" + oklevel + "] [" + sLog + "]");
 	    	return false;
 	    }
 	    if( ValidPerc < 0 ) {
 	    	do_error("Could not fetch VALIDBUBBLE percentage [" + sLog + "]");
 	    	return false;
 	    }
 	    if( InValidPerc < 0 ) {
	    	do_error("Could not fetch INVALIDBUBBLE percentage [" + sLog + "]");
	    	return false;
	    }
 	    //
 	    TensorValidPercentage=ValidPerc;
 	    TensorInvalidPercentage=InValidPerc;
	    if( ValidPerc > InValidPerc ) {
 	      TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.TEXTPARAGRAPH;
 	    }
 	    else {
 	 	  TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.PARAGRAPH;
 	    }
 	    return true;
 	}
 	
 	
    // ('%s (score = %.5f)' 
 	//---------------------------------------------------------------------------------
 	private double extractScore(String sLine)
 	//---------------------------------------------------------------------------------
 	{
 		try {
 		  int idx = sLine.indexOf("=");
 		  int pdx = sLine.indexOf(")");
 		  String sNum = sLine.substring( idx+1 , pdx ).trim();
 		  //do_log(1,sNum);
 		  return xMSet.xU.NaarDouble(sNum);
 		}
 		catch(Exception e ) {
 			return (double)-1;
 		}
 	}
 	
 	
}
