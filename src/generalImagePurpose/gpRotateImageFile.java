package generalImagePurpose;

import generalpurpose.gpUtils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import logger.logLiason;

public class gpRotateImageFile {

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
	public gpRotateImageFile(gpUtils iu,logLiason ilog)
	//-----------------------------------------------------------------------
	{
	   xU=iu;	
	   logger=ilog;
	}
	
	//-----------------------------------------------------------------------
	public boolean rotate90DegreesClockWize(String fin, String fout)
	//-----------------------------------------------------------------------
	{
		// if file is not there return
		if( xU.IsBestand(fin) == false ) {
			do_log(1,"Cannot locate file [" + fin + " ]");
			return false;
		}
	    //doerr("Rotating [" + fin + "] to [" + fout + "]");
		if( loadBestandInBuffer(fin) == false ) return false;
		do_log(9,"Loaded [" + fin + "] for 90degree rotation");
		// rotate
		// X2 = y en Y2 = x
		int tgtpixels[] = new int [ imgBreedte * imgHoogte];
		int p=0;
		int k=-1;
	    for(int y=0;y<imgHoogte;y++)
	    {
	    	for(int x=0;x<imgBreedte;x++)
	    	{
	    		p = srcpixels[ x + (imgBreedte*y)];
	    		//k = y + ( x * imgHoogte); //  X2 = Breedte2 - y en Y2 = x en Breedte2 = hoogte oorspronkelijk
	    		k =  ((x + 1) * imgHoogte) - y - 1;
	    		tgtpixels[ k ] = p;
	    	}
	    }
	    // verwijder target image bestand
	    if( xU.IsBestand( fout ) == true )  {
	    	xU.VerwijderBestand( fout );
	    	if( xU.IsBestand( fout ) == true ) {
	    		do_error("Cannot remove targetfile [" + fout + "]");
	    		return false;
	    	}
	    }
	    // wegschrijven image bestand - hoogte en breedte zijn nu gewisseld
	    boolean ib = writePixelsToJPG( tgtpixels , imgHoogte , imgBreedte , fout);
	    return ib;
	}
	
	//-----------------------------------------------------------------------
	private boolean loadBestandInBuffer(String fin)
	//-----------------------------------------------------------------------
	{
		srcpixels=null;
		gpLoadImageInBuffer lo = new gpLoadImageInBuffer(xU,logger);
		srcpixels = lo.loadBestandInBuffer(fin);
		if( srcpixels == null ) return false;
		imgBreedte= lo.getBreedte();
		imgHoogte = lo.getHoogte();
		return true;
		
		/*
	   if( xU.IsBestand(fin)==false) {
			do_error("Cannot locate input image file [" + fin + "]");
			return false;
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
		   return false;
	    }
	   //  grab pixels
  	   imgBreedte = img.getWidth(null);
	   imgHoogte  = img.getHeight(null);
	   //
	   if( (imgBreedte < 0) || (imgHoogte <0) ) {
		   do_error("Height or width cannot be determined of [" + fin + "]");
		   return false;
	   }
	   srcpixels = new int[ imgHoogte * imgBreedte];
	   try {
		   PixelGrabber pg = new PixelGrabber( img , 0 , 0 , imgBreedte , imgHoogte , srcpixels , 0 , imgBreedte);
		   pg.grabPixels();
	   }
	   catch (Exception e ) {
		   do_error("Pixelgrabber on [" + fin + "] " + e.getMessage());
		   return false;
	   }
	   img=null;
	   return true;
	   */
	}
	
	//---------------------------------------------------------------------------------
    private boolean writePixelsToJPG(int[] dump , int iwidth , int iheigth , String FNaam)
    //---------------------------------------------------------------------------------
	{
		    BufferedImage newImage = new BufferedImage( iwidth, iheigth , BufferedImage.TYPE_INT_RGB);    // ARGB levert oranje glow op
		    newImage.setRGB(0,0,iwidth,iheigth,dump,0,iwidth);
			try {
	           ImageIO.write(newImage, "jpg", new File(FNaam));
			}
			catch(Exception e) {
				do_error("writing file" + e.getMessage() );
				return false;
			}
			newImage=null;
			return true;
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

}
