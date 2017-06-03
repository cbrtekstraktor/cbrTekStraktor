package generalpurpose;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class gpLogger {
	
	private String LogFileName = "null";
	private int LogLevel;
	private PrintStream LogFile=null;
	private boolean LogFileIsOpen = false;
	private ArrayList<String> errorLst = null;
	private String LoggingDateFormat="ddMMM HH:mm:ss.SSS";
	private int MaxLogLijnen = 5000;
	private boolean verbose=true;
	
	private String getTS()
	{
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat ft = new SimpleDateFormat (LoggingDateFormat);
	    return ft.format(date).trim().toUpperCase();
	}
	
	//
	//---------------------------------------------------------------------------------
	public gpLogger(int iL , String FNm )
	//---------------------------------------------------------------------------------
	{
		LogLevel = iL;
		LogFileName = FNm;
		//LoggingDateFormaat=iDF;
		errorLst = new ArrayList<String>();
		OpenLogs();
	}
	//
	//---------------------------------------------------------------------------------
	public void setVerbose(boolean ib)
	//---------------------------------------------------------------------------------
	{
		verbose=ib;
	}
	//
	//---------------------------------------------------------------------------------
	public void setLogLevel(int i)
	//---------------------------------------------------------------------------------
	{
		LogLevel = i;
	}
	//
	//---------------------------------------------------------------------------------
	public void Logit(int level , String sIn)
	//---------------------------------------------------------------------------------
	{
		if( level > LogLevel ) return;;
		String sLijn = getTS() + " " + sIn;
		if( LogFileIsOpen ) LogFile.println(sLijn);
		if( verbose ) {
		  if( level != 0 ) System.out.println(sLijn); else System.err.println(sLijn);
		}
		if( level == 0 ) {
			String s = sIn;
			errorLst.add(s);
		}
	}
	//
	//---------------------------------------------------------------------------------
	void OpenLogs()
	//---------------------------------------------------------------------------------
	{
		try{
			LogFile=new PrintStream(new FileOutputStream(LogFileName,true));
			LogFileIsOpen=true;
			Logit(1,"============ Logger started =================");
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
	//
	//---------------------------------------------------------------------------------
	public void CloseLogs()
	//---------------------------------------------------------------------------------
	{
		if( LogFileIsOpen == false ) return;
		Logit(1,"============ Logger stopped =================");
		if( LogFile != null ) LogFile.close();
		System.out.println("Closed logfiles [" + LogFileName + "]" );
		PruneLog();
	}
	//
	//---------------------------------------------------------------------------------
	public String getLastError()
	//---------------------------------------------------------------------------------
	{
	  int idx = errorLst.size();
	  if( idx == 0 ) return "";
	  return errorLst.get(idx-1);
	}
	//
	//---------------------------------------------------------------------------------
	public String getErrorList()
	//---------------------------------------------------------------------------------
	{
	  String sOut="";
	  for(int i=0;i<errorLst.size();i++) sOut = sOut + "\n" + errorLst.get(i);
	  return sOut;
	}
	//
	//---------------------------------------------------------------------------------
	private void PruneLog()
	//---------------------------------------------------------------------------------
	{
		int loglijnen=0;
		String sLijn = null;
		String LogFileNameTemp = LogFileName + ".new";
        // counter
		try {
			  File inFile  = new File(this.LogFileName);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  while ((sLijn=reader.readLine()) != null) { loglijnen++; }
	          reader.close();
	        }
		catch (Exception e) {
				System.out.println("Error reading file [" + LogFileName + "]");
				return;
		}
		
		int skip = loglijnen - MaxLogLijnen;
		
		if( skip < 0 ) return;
		//
		loglijnen =0;
		try {
			  File inFile  = new File(this.LogFileName);  // File to read from.
	       	  BufferedReader reader = new BufferedReader(new FileReader(inFile));
	       	  LogFile=new PrintStream(new FileOutputStream(LogFileNameTemp,true));
	       	  LogFile.println(getTS() + "       ======= LOG TRUNCATED =======");
	       	  LogFile.println(getTS() + "       Lines truncated " + skip);
	       	  while ((sLijn=reader.readLine()) != null) { 
	       		  loglijnen++;
	       		  if( loglijnen < skip ) continue;
	       		  LogFile.println(sLijn);
	       	  }
	       	  LogFile.println(getTS() + "       ======= LOG TRUNCATED =======");
	       	  LogFile.println(getTS() + "       Lines truncated " + skip);
	       	  LogFile.close();
	          reader.close();
	        }
		catch (Exception e) {
				System.out.println("Error reading file [" + LogFileName + "]");
				return;
		}
		//
		// delete LOG
		File FObj = new File(LogFileName);
        if ( FObj.isFile() != true ) {
        	return;
        }
        if ( FObj.getAbsolutePath().length() < 10 ) {
        	return;  // domme veiligheid
        }
        FObj.delete();
        // rename new naar log
        File oldFile = new File(LogFileNameTemp); 
        //Now invoke the renameTo() method on the reference, oldFile in this case
        oldFile.renameTo(new File(LogFileName));
		System.out.println("Log [" + LogFileName + "] has been truncated to [" + MaxLogLijnen +"] lines by deleting [" + skip + "] lines");
	}

}
