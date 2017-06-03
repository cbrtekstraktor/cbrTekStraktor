package generalImagePurpose;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.net.URL;

import javax.imageio.ImageIO;

import generalpurpose.gpUtils;
import logger.logLiason;

public class gpLoadImageInBuffer {
	
	gpUtils xU = null;
	logLiason logger=null;
	
	private int[] srcpixels=null;
	private int imgBreedte=-1;
	private int imgHoogte=-1;
	

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
	//-----------------------------------------------------------------------
	public gpLoadImageInBuffer(gpUtils iu,logLiason ilog)
	//-----------------------------------------------------------------------
	{
	   xU=iu;	
	   logger=ilog;
	}
	
	//---------------------------------------------------------------------------------
    public int getHoogte()
    //---------------------------------------------------------------------------------
    {
		return imgHoogte;
	}
    //---------------------------------------------------------------------------------
    public int getBreedte()
	//---------------------------------------------------------------------------------
    {
		return imgBreedte;
	}
	//-----------------------------------------------------------------------
	public int[] loadBestandInBuffer(String fin)
	//-----------------------------------------------------------------------
	{
	   if( xU.IsBestand(fin)==false) {
			do_error("Cannot locate input image file [" + fin + "]");
			return null;
	   }
	   Image img=null;
	   // Blocking all met de awt.toolkit lukt dit soms niet
	   try {
	        // Create a URL for the image location
		    URL url = new URL("file:///" + fin);
	        img = ImageIO.read(url);
	        //System.out.println("Height: " + img.getHeight(null));
	    }
	    catch(Exception e )
	    {
		   do_error("reading imagefile [" + fin + "] " + e.getMessage());
		   return null;
	    }
	   //  grab pixels
  	   imgBreedte = img.getWidth(null);
	   imgHoogte  = img.getHeight(null);
	   //
	   if( (imgBreedte < 0) || (imgHoogte <0) ) {
		   do_error("Height or width cannot be determined of [" + fin + "]");
		   return null;
	   }
	   srcpixels = new int[ imgHoogte * imgBreedte];
	   try {
		   PixelGrabber pg = new PixelGrabber( img , 0 , 0 , imgBreedte , imgHoogte , srcpixels , 0 , imgBreedte);
		   pg.grabPixels();
	   }
	   catch (Exception e ) {
		   do_error("Pixelgrabber on [" + fin + "] " + e.getMessage());
		   return null;
	   }
	   img=null;
	   return srcpixels;
	}



}
