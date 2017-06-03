package generalpurpose;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import logger.logLiason;


public class gpUnZipFileList {
 
  logLiason logger = null;
  private boolean isOK = true;
  
  //------------------------------------------------------------
  private void do_log(int logLevel , String sIn)
  //------------------------------------------------------------
  {
     if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
     else 
     if (logLevel == 0 ) System.err.println(sIn);
     else System.out.println(sIn);
  }
  private void do_error(String sIn)
  {
	  do_log(0,sIn);
	  
  }
  public gpUnZipFileList(String FZipName , String TargetDir , String pattern , logLiason ilog)
  {
	  logger=ilog;
	  unzipFileIntoDirectory(FZipName , TargetDir , pattern );
  }
  
  public boolean UnzippedCorrectly()
  {
	  return isOK;
  }
  
  private void unzipFileIntoDirectory(String zipFile, String sDir , String pattern) 
  {
	if( pattern != null ) {
		if (pattern.trim().length() <= 0) pattern=null;
	}
	byte[] buffer = new byte[1024];
	File folder = new File(sDir);
	if ( folder.exists() == false ) {
	     do_error("Cannot locate directory [" + sDir + "]");
	     isOK=false;
	     return;
	}
	if ( folder.isDirectory() == false ) {
		do_error("[" + sDir + "] is not a directory");
		isOK=false;
	    return;	
	}
	//
	try
	{
    	//get the zip file content
    	ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
    	while(ze!=null){
    	    String fileName = ze.getName();
    	    File newFile = new File(sDir + File.separator + fileName);
            //new File(newFile.getParent()).mkdirs();
            if( pattern != null ) {
    	    	if ( fileName.toUpperCase().contains(pattern.toUpperCase()) == false ) {
    	    		ze = zis.getNextEntry();
    	    		continue;
    	    	}
    	    }
            if( pattern != null )  do_log(9,"Extracting -> " + fileName);
            FileOutputStream fos = new FileOutputStream(newFile);             
            int len;
            while ((len = zis.read(buffer)) > 0) {
       		   fos.write(buffer, 0, len);
            }
            fos.close();   
            ze = zis.getNextEntry();
    	}
        zis.closeEntry();
    	zis.close();
    }
	catch(Exception e){
	   do_error("Cannot unzip " + e.getMessage());
       isOK=false;
    }
     
	
  }

}











