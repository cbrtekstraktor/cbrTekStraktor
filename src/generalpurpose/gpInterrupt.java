package generalpurpose;

import java.io.File;
import java.io.PrintStream;

public class gpInterrupt {
	
	String InterruptFileName = null;

	// Simple semaphore mechanism based on File presence
	public gpInterrupt(String FileNameIn)
	{
	 InterruptFileName = FileNameIn;	
	}
	
	public boolean resetInterrupt()
	{
		if( InterruptFileName == null ) return false;
		File FObj = new File(InterruptFileName);
        if ( FObj.isFile() != true ) {
        	//logit( 0 ,"ERROR '" + sIn + ") -> file not found");
        	return true;  // already removed
        }
        if ( FObj.getAbsolutePath().length() < 10 ) {
        	System.err.println(InterruptFileName + "-> Length too small. File will not be deleted");
        	return false;  // blunt safety
        }
        FObj.delete();
        File XObj = new File(InterruptFileName);
        if ( XObj.isFile() == true ) {
        	System.err.println("ERROR" + InterruptFileName+ " -> could not be deleted");
        	return false;
        }
        return true;
	}
	
	public boolean requestInterrupt()
	{
		if( gotInterrupt() ) return true; // file already there
		if( InterruptFileName == null ) return false;
    	try {
    		 PrintStream   writer = new PrintStream(InterruptFileName,"ASCII");
		     writer.println("IRQ"+System.currentTimeMillis());
		     writer.close();
		     return true;
	    	}
	    	catch (Exception e )
	    	{
	    		System.err.println("Could set interruptfile [" + InterruptFileName + "] for writing" + e.getMessage());
	    		return false;
	    	}
	}

	public boolean gotInterrupt()
	{
		if( InterruptFileName == null ) return false;
		try {
		 File fObj = new File(InterruptFileName);
		 if ( fObj.exists() == true )
		 {
			if ( fObj.isFile() == true ) {
//System.err.println("Found STOP file [" +  InterruptFileName + "]");
				return true;
			}
		 } 
		 return false;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}
	
	
}
