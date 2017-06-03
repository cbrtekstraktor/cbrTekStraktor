/*
	  * 
	  * Original code on http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4 (Michael C. Daconta)
	  * 
	 */

package generalpurpose;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import logger.logLiason;

public class gpStreamGobbler extends Thread {

	    Process MyProcess;
	    private InputStream inputStream;
	    private String streamType;
	    private boolean displayStreamOutput;
	    ArrayList<String> StreamList; 
	    logLiason logger = null;
		//
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
	    
	    
	    /**
	     * Constructor.
	     * 
	     * @param inputStream the InputStream to be consumed
	     * @param streamType the stream type (should be OUTPUT or ERROR)
	     * @param displayStreamOutput whether or not to display the output of the stream being consumed
	     */
	    gpStreamGobbler(
	    		logLiason iL,
	    		      final InputStream inputStream,
	                  final String streamType,
	                  final boolean displayStreamOutput,
	                  final ArrayList<String> lijst, Process p)
	    {
	    	logger = iL;
	        this.inputStream = inputStream;
	        this.streamType = streamType;
	        this.displayStreamOutput = displayStreamOutput;
	        this.StreamList=lijst;
	        this.MyProcess=p;
	    }
	    
	    
	    /**
	     * Consumes the output from the input stream and displays the lines consumed if configured to do so.
	     */
	    @Override
	    public void run()
	    {
	        try
	        {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String line = null;
	            while ((line = bufferedReader.readLine()) != null)
	            {
	                if (displayStreamOutput)
	                {
	                    do_log(3,streamType + ">" + line);
	                    if( (line.toUpperCase().indexOf("BUILD SUCCESSFUL") >= 0) || (line.toUpperCase().indexOf("BUILD FAILED") >= 0) ) { 
	                    	do_log(3,"Gotcha! Proceeding to kill this process"); 
	                    	MyProcess.destroy();   // Forceer een einde van het process.  Ant wacht op een ENTER
	                    } 	
	                }
	                this.StreamList.add(line);
	            }
	        }
	        catch (IOException ex)
	        {
	            do_log(3,"Failed to successfully consume and display the input stream of type " + streamType + ".");
	            ex.printStackTrace();
	        }
	    }

	}


