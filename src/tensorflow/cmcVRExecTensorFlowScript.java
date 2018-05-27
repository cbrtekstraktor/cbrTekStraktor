package tensorflow;

import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import generalpurpose.gpExecBatch;
import generalpurpose.gpPrintStream;
import logger.logLiason;

public class cmcVRExecTensorFlowScript {
	
	private static boolean SIMULATOR = false;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private String PythonHome = null;
	private static String CLASSIFY_PYTHON_SCRIPT = "classifyParagraph.py";
	
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

    // ---------------------------------------------------------------------------------
 	public cmcVRExecTensorFlowScript(cmcProcSettings iM, logLiason ilog)
    // ---------------------------------------------------------------------------------
 	{
 		xMSet = iM;
		logger = ilog;
		PythonHome = xMSet.getPythonHomeDir();
		resp = new ThreadMonitorDTO();
 	}

    // ---------------------------------------------------------------------------------
 	public ThreadMonitorDTO getTensorRespons()
    // ---------------------------------------------------------------------------------
 	{
 		return resp;
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private boolean createBatchFile(int threadnumber)
    // ---------------------------------------------------------------------------------
 	{
 	  switch( xMSet.getMyOS() )
 	  {
 	  case MSDOS : return createMSDOSBatchFile(threadnumber);
 	  case LINUX : return createLINUXBatchFile(threadnumber);
 	  default : { do_error("Operating system [" + xMSet.getMyOS() + "] not supported"); return false; }
 	  }
 	}
 	
 	// ---------------------------------------------------------------------------------
 	private String getTensorBatchName()
 	// ---------------------------------------------------------------------------------
 	{
 		 return resp.getCommandFileName();
 	}
 	
    // ---------------------------------------------------------------------------------
 	private boolean createMSDOSBatchFile(int threadnumber)
    // ---------------------------------------------------------------------------------
 	{
       String ShortCmd = (threadnumber >= 0 ) ? "RunTensorFlow" + threadnumber + ".cmd" : "RunTensorFlow.cmd";
       resp.setCommandFileName (xMSet.getTensorDir() + xMSet.xU.ctSlash +  ShortCmd);
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
 	   //
 	   script.println("@ECHO OFF" );
 	   script.println("REM Generated by " + xMSet.getApplicDesc() );
	   script.println("REM Generated on " + xMSet.xU.prntStandardDateTime(System.currentTimeMillis()).toUpperCase() );
 	   //
	   script.println("SET PYTHON_HOME=\"" + PythonHome + "\"" );
	   script.println("PATH=%PYTHON_HOME%;%PYTHON_HOME%\\Scripts;%PATH%" );
	   //
	   script.println("set TNSRDIR=\"" + TensorFolder + "\"");
	   script.println("set PYTHPROG=\"" + TensorFolder + "\"");
 	   script.println("SET IMAGEFILENAME=\"" + resp.getImageFileName() + "\"");
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
 	   script.println(" ");   // You need an extra blank line - the streamgobbler class always misses the last line
 	   //
 	   script.close();
 	   return true;	
 	}

    // ---------------------------------------------------------------------------------
 	private boolean createLINUXBatchFile(int threadnumber)
    // ---------------------------------------------------------------------------------
 	{
 	   do_error("TODO Linux shell script still needs to be created");	
 	   return false;
 	}

    // ---------------------------------------------------------------------------------
 	public boolean execTensorFlowVR(String ImageFileNameIn , int threadnumber)
    // ---------------------------------------------------------------------------------
 	{
 		if( resp == null ) {
 			do_error("System error : Response class has not been initiated");
 			return false;
 		}
 		resp.setExitStatus(false );
 		resp.setImageFileName( ImageFileNameIn );
 		resp.setCommandFileName(null);
 	    resp.setTipeDeterminedViaTensor(cmcProcEnums.PageObjectType.UNKNOWN);
 	    resp.setTensorValidPercentage(-1);
 		resp.setTensorInvalidPercentage(-1);
 		//
 	 	if( createBatchFile(threadnumber) == false ) return false;
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
        resp.setExitStatus(true);
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
 	    resp.setTensorValidPercentage(ValidPerc);
 	    resp.setTensorInvalidPercentage(InValidPerc);
	    if( ValidPerc > InValidPerc ) {
 	      resp.setTipeDeterminedViaTensor(cmcProcEnums.PageObjectType.TEXTPARAGRAPH);
 	    }
 	    else {
 	 	  resp.setTipeDeterminedViaTensor(cmcProcEnums.PageObjectType.PARAGRAPH);
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
