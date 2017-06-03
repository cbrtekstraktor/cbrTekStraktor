package cbrTekStraktorModel;
import generalImagePurpose.cmcImageRoutines;
import generalImagePurpose.gpFetchByteImageData;
import generalImagePurpose.gpImageMetadataGrabber;
import imageProcessing.cmcProcColorHistogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import logger.logLiason;



public class comicImage {
  
    public cmcProcColorHistogram hstgrm=null;
	private cmcStopWatch watch=null;
	private cmcImageRoutines iRout=null;
	   
    logLiason logger=null;
   
    String FName;
  
    public Image img=null;
    public int[] pixels;
    public int[] workPixels;
    private int  width;
    private int  heigth;
    private int physicalHeigthDPI=-1;
    private int physicalWidthDPI=-1;
   
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
   comicImage(logLiason ilog)
   //---------------------------------------------------------------------------------
   {
	   logger=ilog;
	   FName=null;
	   width = -1;
	   heigth = -1;
	   physicalHeigthDPI=-1;
	   physicalWidthDPI=-1;
	   hstgrm = new cmcProcColorHistogram();
	   iRout = new cmcImageRoutines(logger);
   }
   
   //---------------------------------------------------------------------------------
   public void clearImg()
   //---------------------------------------------------------------------------------
   {  // creates an empty image with identical dimensions as previous image
	 if( img == null ) return;
	 img=null;
	 pixels = new int[ width * heigth ];
	 for(int i=0;i<pixels.length;i++) pixels[i] = 0xfffffff8;
	 img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(width,heigth,pixels,0,width));
   }
   
   //---------------------------------------------------------------------------------
   public void loadImg(String iFN)
   //---------------------------------------------------------------------------------
   {
	    watch = new cmcStopWatch( this.getClass().getName() );
	    FName = iFN;
	    img=null;
	    try {
	        // Create a URL for the image location
		    URL url = new URL("file:///" + iFN);
	        img = ImageIO.read(url);
	        //System.out.println("Height: " + img.getHeight(null));
	    }
	    catch(Exception e )
	    {
		   do_error("reading imagefile [" + iFN + "] " + e.getMessage());
	    }
	    //
		initialiseerImageAttributes();
		//
		//getMetadata(iFN);
		//
		hstgrm.makeHistogram(pixels);
		//
		watch.stopChrono();
   }
   
   //---------------------------------------------------------------------------------
   private void initialiseerImageAttributes()
   //---------------------------------------------------------------------------------
   {    // opgepast bij aanpassen wordt door crop gebruikt
	    //
	    width = img.getWidth(null);
	    heigth = img.getHeight(null);
	    //
	    pixels=null;
        workPixels=null;
        pixels = new int[ width * heigth ];
        workPixels = new int[ width * heigth ];
	    // laad de pixels
        PixelGrabber pg = new PixelGrabber( img , 0 , 0 , width , heigth , pixels , 0 , width); 
        try { 
         pg.grabPixels(); 
        } 
        catch( Exception e) { 
                 do_error("Fout bij grabPixel " + e.getMessage()); 
                 return; 
        } 
   }
   
   //---------------------------------------------------------------------------------
   public void moveWorkPixelsToImage()
   //---------------------------------------------------------------------------------
   {
	   img=null;  // GC hint
	   System.arraycopy( workPixels , 0 , pixels , 0 , width*heigth);
	   img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource( width , heigth , pixels , 0 , width));
	   hstgrm.makeHistogram(pixels);
   }
   
   //---------------------------------------------------------------------------------
   public void crop(int cropx , int cropy , int cropwidth , int cropheigth )
   //---------------------------------------------------------------------------------
   {
	   //System.out.println("CROPPING x=" + cropx + " y=" + cropy + " cw=" + cropwidth + " ch=" + cropheigth + " w=" + width + " h=" + heigth);
		
	   // checks 
	   String sErr="";
	   if( (cropx < 0) || (cropx >= width) ) sErr= "X coordinate error ";
	   if( (cropy < 0) || (cropy >= heigth) ) sErr += "Y coordinate error ";
	   if( (cropwidth < 0) || ((cropwidth + cropx) > width) ) sErr += "Crop WIDTH error ";
	   if( (cropheigth < 0) || ((cropheigth + cropy) > heigth) ) sErr += "Crop HEIGTH error";
	   if( sErr.length() != 0 ) {
		   do_error("CROP -> " + sErr);
		   return;
	   }
	   //
	   int idx = 0;
	   int tgt=0;
	   for(int y=0;y<cropheigth;y++ )
	   {
		   idx = ((y + cropy)*width) + cropx;
		   for(int x=0;x<cropwidth;x++)
		   {
			   workPixels[tgt] = pixels[idx];
			   idx++;
			   tgt++;
		   }
	   }
	   // maak een image uit de workbuffer
	   width = cropwidth;
	   heigth = cropheigth;
	   img = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource( width , heigth , workPixels , 0 , width));
	   // herzet alles
	   initialiseerImageAttributes();
	   hstgrm.makeHistogram(pixels);
   }
   
   //---------------------------------------------------------------------------------
   public void writeToFile(String FNaam)
   //---------------------------------------------------------------------------------
   {
	   writePixelsToJPG( pixels , width , heigth , FNaam);
   }
   //---------------------------------------------------------------------------------
   public void writePixelsToJPG(int[] dump , int iwidth , int iheigth , String FNaam)
   //---------------------------------------------------------------------------------
   {
	   iRout.writePixelsToFile( dump , iwidth , iheigth , FNaam , cmcImageRoutines.ImageType.RGB );
   }
   
   //---------------------------------------------------------------------------------
   //---------------------------------------------------------------------------------
   public int getWidth()
   {
	   return width;
   }
   public int getHeigth()
   {
	   return heigth;
   }
   
   public int getPixelValueAtXY(int x , int y)
   {
	    int i = x + (y * width);
	    if( (i<0) || (i>=(width*heigth))) return -1;
	    return pixels[i];
   }
 
   private int getRGBLum(int p,char tipe)
   {
	   //if( p == -1 ) return -1;
	   int r = 0xff & ( p >> 16);
	   if( tipe == 'R') return r;
       int g = 0xff & ( p >> 8); 
       if( tipe == 'G') return g;
       int b = 0xff & p;
       if( tipe == 'B') return b;
       int lum = (int) Math.round(0.2126*r + 0.7152*g + 0.0722*b);
       return lum;
   }
   public int getPixelRedValueAtXY(int x , int y)
   {
	   return getRGBLum(getPixelValueAtXY(x,y),'R');
   }
   public int getPixelGreenValueAtXY(int x , int y)
   {
	   return getRGBLum(getPixelValueAtXY(x,y),'G');
   }
   public int getPixelBlueValueAtXY(int x , int y)
   {
	   return getRGBLum(getPixelValueAtXY(x,y),'B');
   }
   public int getPixelGrayValueAtXY(int x , int y)
   {
	   return getRGBLum(getPixelValueAtXY(x,y),'L');
   }
   public long getLoadDurationInNano()
   {
		return watch.getDurationNanoSec();
   }
   
   
   // set via comic pae - because the metadata is read on comic page
   public void setPhysicalHeigthDPI(int i)
   {
	   physicalHeigthDPI=i;
   }
   public void setPhysicalWidthDPI(int i)
   {
	   physicalWidthDPI=i;
   }
   
   public int getPhysicalHeigthDPI()
   {
	return   physicalHeigthDPI;
   }
   public int getPhysicalWidthDPI()
   {
	return   physicalWidthDPI;
   }
   public double getWidthInInch()
   {
	   return( physicalWidthDPI > 0 ) ? (double)width / (double)physicalWidthDPI : -1;
   }
   public double getHeigthInInch()
   {
	   return( physicalHeigthDPI > 0 ) ? (double)heigth / (double)physicalHeigthDPI : -1;
   }
   
   
}
