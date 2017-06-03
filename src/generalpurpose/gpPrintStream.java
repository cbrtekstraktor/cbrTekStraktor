package generalpurpose;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class gpPrintStream {

	 PrintStream writer=null;
	 boolean verbose=false;
	 long  startt=0L;
	 String enCoding="";
	 //
	 //---------------------------------------------------------------------------------
	 public gpPrintStream(String FNaam, String cp)
	 //---------------------------------------------------------------------------------
	 {
		     enCoding = "ISO-8859-1";
		     
		     if( cp.compareToIgnoreCase("UTF-8") == 0 ) enCoding = "UTF-8";
		     else
		     if( cp.compareToIgnoreCase("UTF8") == 0 ) enCoding = "UTF-8";
			 else	 
		     if( cp.compareToIgnoreCase("UTF-16") == 0 ) enCoding = "UTF-16";
		     else
		     if( cp.compareToIgnoreCase("UTF16") == 0 ) enCoding = "UTF-16";
			 else
		     if( cp.compareToIgnoreCase("UTF-32") == 0 ) enCoding = "UTF-32";
		     else
		     if( cp.compareToIgnoreCase("UTF32") == 0 ) enCoding = "UTF-32";
			 else
		     if( cp.compareToIgnoreCase("ASCII") == 0 ) enCoding = "ASCII";
		     else
		     if( cp.compareToIgnoreCase("LATIN1") == 0 ) enCoding = "ISO-8859-1";
		     else
		   	 if( cp.compareToIgnoreCase("ISO-8859-1") == 0 ) enCoding = "ISO-8859-1";
			 else
		     enCoding = null;
		     
		     if( enCoding != null ) {
		      //System.out.println("WRITER in " + enCoding); 	 
			  writer = OpenWriteFile(FNaam,enCoding);
		     }
		     else {
			  System.out.println("gpPrintStream - Internal error - Unknown Code Page [" + enCoding + "]");
		     }
	 }
	 //
	 //---------------------------------------------------------------------------------
	 boolean isActive()
	 //---------------------------------------------------------------------------------
	 {
		 if( writer == null ) return false;
		 return true;
	 }
	 //
	 //---------------------------------------------------------------------------------
	 void logit(String sL)
	 //---------------------------------------------------------------------------------
	 {
		 System.out.println(sL);
	 }
	 //
	 //---------------------------------------------------------------------------------
	 private PrintStream OpenWriteFile(String FNaam,String enCoding)
	 //---------------------------------------------------------------------------------
	 {
		    startt = System.nanoTime();
	    	try {
	    	   writer = new PrintStream(FNaam,enCoding);
		     return writer;
	    	}
	    	catch (Exception e )
	    	{
	    		logit("Could not open [" + FNaam + "] for writing" + e.getMessage());
	    		return null;
	    	}
	  }
	 //
	 //---------------------------------------------------------------------------------
	 public void close()
	 //---------------------------------------------------------------------------------
	 {
	    	try {
	    	   if( writer != null ) writer.close();
	    	   writer = null;
	     	   return;
	     	}
	     	catch (Exception e )
	     	{
	     		logit("Could not close file writer" + e.getMessage());
	     		return;
	     	}
	 }
	 //
	 //---------------------------------------------------------------------------------
	 public void print(String sLijn)
	 //---------------------------------------------------------------------------------
	 {
		    if( writer == null ) {
		    	//System.out.println(sLijn);
		    	return;
		    }
	    	try {
	    	   writer.print(sLijn);
	    	   //if( verbose ) System.out.println(sLijn);
	     	   return;
	     	}
	     	catch (Exception e )
	     	{
	     		logit("Could not write to file " + e.getMessage());
	     		return;
	     	}
	 }
	 //
	 //---------------------------------------------------------------------------------
	 public void println(String sLijn)
	 //---------------------------------------------------------------------------------
	 {
		    if( writer == null ) {
		    	System.out.println(sLijn);
		    	return;
		    }
	    	try {
	    	   writer.println(sLijn);
	    	   if( verbose ) System.out.println(sLijn);
	     	   return;
	     	}
	     	catch (Exception e )
	     	{
	     		logit("Could not write to file " + e.getMessage());
	     		return;
	     	}
	 }
	 public void setVerbose(boolean ib)
	 {
		 verbose = ib;
	 }
	 

	 public void do_standard_header(String sLn1)
	 {
		 println("-- ");
		 println("-- " + sLn1);
		 println("-- " + new SimpleDateFormat("yy-MM-dd H:mm:ss").format(new Date(System.currentTimeMillis())) );
		 println("-- Encoding " + enCoding);
	 }
	 
	 public void do_standard_tail(String sIn)
	 {
		 if ( writer == null ) return;
		 println("-- ");
		 println("-- " + new SimpleDateFormat("yy-MM-dd H:mm:ss").format(new Date(System.currentTimeMillis())) );
		 String sLine = "-- " + sIn + "elapsed : " + ((System.nanoTime()-startt) / 1000000L) + " msec" ;
		 println(sLine);
	 }
}
