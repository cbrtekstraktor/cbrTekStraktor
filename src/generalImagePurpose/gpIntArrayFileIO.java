package generalImagePurpose;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import logger.logLiason;


public class gpIntArrayFileIO {
  
	logLiason logger=null;
	
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
	 //---------------------------------------------------------------------------------
	 public gpIntArrayFileIO(logLiason ilog)
	 //---------------------------------------------------------------------------------
	 {
		 logger = ilog;
	 }
		
	 //---------------------------------------------------------------------------------
	 public void writeIntArrayToFile(String FNaam,int[] pixels )
	 //---------------------------------------------------------------------------------
	 {
		 try
	        {
			 DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FNaam)));
			 for(int i=0;i<pixels.length;i++)
			 {
				 out.writeInt(pixels[i]);
			 }
			 out.close();
	        }
	        catch (Exception e)
	        {
	        	do_error("Error writing int array to [" + FNaam + "] " + e.getMessage());
	        }
	 }

	 //---------------------------------------------------------------------------------
	 public int[] readIntArrayFromFile(String FNaam )
	 //---------------------------------------------------------------------------------
	 {
		 try
	        {
			    File file = new File(FNaam);
			    long len = file.length() / 4L;
			    int[] temp = new int[(int)len];
			    DataInputStream  in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			    for(int i=0;i<temp.length;i++)
			    {
			    	temp[i] = in.readInt();
			    }
			    in.close();
			    return temp;
			}
	        catch (Exception e)
	        {
	        	do_error("Error reading int array from [" + FNaam + "] " + e.getMessage());
	        	return null;
	        } 
	 }
	 	 
}

