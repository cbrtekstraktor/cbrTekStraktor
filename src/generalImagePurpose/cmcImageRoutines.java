package generalImagePurpose;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Element;

//import com.sun.imageio.plugins.jpeg.*;


import logger.logLiason;


public class cmcImageRoutines {

	logLiason logger = null;
	
	public enum ImageType { RGB , ARGB , UNKNOWN }
	
	private static List<String> ImageSuffixTypes = Arrays.asList( ImageIO.getWriterFileSuffixes() );
	
	private int resizeWidth=0;
	private int resizeHeigth=0;

	
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
	public cmcImageRoutines(logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		logger=ilog;
	}
	//---------------------------------------------------------------------------------
	private String isImageTypeSupported(String FNaam)
	//---------------------------------------------------------------------------------
	{
	    int offset = FNaam.lastIndexOf( "." );
	    if (offset < 0 ) {
	    	do_error("(cmcImageRoutines) No suffix on Imagefile [" + FNaam + "]");
	    	return null;
	    }
		String ImageSuffix = FNaam.substring(offset + 1).trim().toLowerCase();
		if (ImageSuffixTypes.contains(ImageSuffix) == false ) {
	    	do_error("(cmcImageRoutines) Unsupported suffix [" + ImageSuffix + "] on Imagefile [" + FNaam + "]");
	    	return null;
		}
        return ImageSuffix;
	}
	//---------------------------------------------------------------------------------
	public void writePixelsToFile(int[] dump , int iwidth , int iheigth , String FNaam , cmcImageRoutines.ImageType Tipe)
	//---------------------------------------------------------------------------------
	{
		writePixelsToFileDPI(dump , iwidth , iheigth , FNaam , Tipe , -1 );
	}
	
	//---------------------------------------------------------------------------------
	public void writePixelsToFileDPI(int[] dump , int iwidth , int iheigth , String FNaam , cmcImageRoutines.ImageType Tipe  , int dpi)
	//---------------------------------------------------------------------------------
	{
		    // test whether suffix is supported
		    String ImageSuffix = isImageTypeSupported(FNaam);
		    if( ImageSuffix == null ) return;
		    //
			BufferedImage newImage = null;
			switch ( Tipe )
			{
			case ARGB : { newImage = new BufferedImage( iwidth, iheigth , BufferedImage.TYPE_INT_ARGB); break; }
			case  RGB : { newImage = new BufferedImage( iwidth, iheigth , BufferedImage.TYPE_INT_RGB); break; }
			default   : { do_error("Unsupported Image type [" + Tipe + "]"); return; }
			}
			newImage.setRGB(0,0,iwidth,iheigth,dump,0,iwidth);
		    //	 DPI not set then standard routine
			//if( (dpi <= 0) || (ImageSuffix.compareToIgnoreCase("png")!=0) ) {
			if( dpi <= 0) {
			  try {
	           ImageIO.write(newImage, ImageSuffix , new File(FNaam));
			  }
			  catch(Exception e) {
				do_error("(cmcImageRoutines) Writing file [" + FNaam + "] " + e.getMessage() );
			  }
			}
			else {
				writeBufferedImageDPI( newImage , ImageSuffix , FNaam , dpi );
			}
			newImage=null;
	 }
	
	//---------------------------------------------------------------------------------
	private void writeBufferedImageDPI(BufferedImage bimg , String ImageSuffix , String FName , int dpi)
	//---------------------------------------------------------------------------------
	{
		String formatName = ImageSuffix.toLowerCase().trim();
		IIOMetadata metadata = null;
		ImageWriter writer=null;
		ImageWriteParam writeParam = null;
        //
		if( formatName.compareToIgnoreCase("png") ==  0 ) {
		    //	
			for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			       writer = iw.next();
			       writeParam = writer.getDefaultWriteParam();
			       ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			       metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			       if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
			          continue;
			       }
			       break;
			}
            if( metadata == null ) {
            	do_error("Could not fetch IIOM metadata");
            	return;
            }
            double dotsPerMilli = Math.round((double)dpi / (double)25.4);
            //
            IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
            horiz.setAttribute("value", Double.toString(dotsPerMilli));
            //
            IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
            vert.setAttribute("value", Double.toString(dotsPerMilli));

            IIOMetadataNode dim = new IIOMetadataNode("Dimension");
            dim.appendChild(horiz);
            dim.appendChild(vert);

            IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
            root.appendChild(dim);
            try {
              metadata.mergeTree("javax_imageio_1.0", root);
            }
            catch(Exception e ) {
            	do_error("Error when mergeTree " + e.getMessage());
            	return;
            }
		}
		else 
		if( (formatName.compareToIgnoreCase("jpg")== 0) || (formatName.compareToIgnoreCase("jpeg")== 0) ) {
			//
			writer = ImageIO.getImageWritersBySuffix(formatName).next();
	        writeParam = writer.getDefaultWriteParam();
	        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
	        metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            //			
            Element tree = (Element) metadata.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));
            jfif.setAttribute("resUnits", "1");
            try {
              metadata.setFromTree("javax_imageio_jpeg_image_1.0", tree);
            }
            catch(Exception e ) {
            	do_error("Error when mergeTree " + e.getMessage());
            	return;
            }
			//do_error("System error - writeBufferedImageDPI - JPEG disabled");
		}	
		else {
			do_error("System error - writeBufferedImageDPI unsupported suffix [" + ImageSuffix + "]");
		}
		
        // write file        
        try {
        	FileOutputStream os = new FileOutputStream(FName);
            ImageOutputStream stream = ImageIO.createImageOutputStream(os);
            writer.setOutput(stream);
            writer.write(metadata, new IIOImage(bimg, null, metadata), writeParam);
        } 
        catch( Exception e ) {
        	do_error("Writing file [" + FName + "] " + e.getMessage() );
        }
		return;
	}
	
	//---------------------------------------------------------------------------------
	public int grayScale(int p)
	//---------------------------------------------------------------------------------
	{
		 int r = 0xff & ( p >> 16); 
         int g = 0xff & ( p >> 8); 
         int b = 0xff & p; 
         return (int)Math.round(0.2126*r + 0.7152*g + 0.0722*b); 
	}
	
	//---------------------------------------------------------------------------------
	public int bleach(int p)
	//---------------------------------------------------------------------------------
	{
		     int r = 0xff & (p >> 16); 
	         int g = 0xff & (p >> 8);
	         int b = 0xff & (p);
	         float[] hsb = Color.RGBtoHSB(r, g, b, null);
	         float hue = hsb[0]; 
	         float saturation = hsb[1];
	         float brightness = hsb[2];
	         hsb=null;
	         // helder en lichtere kleur   
	         brightness *= 1.4f;  if( brightness > 1f) brightness =1f;
	         saturation *= 0.1f;  //if( saturation > 1f) saturation =1f;
	         //
	         return Color.HSBtoRGB(hue,saturation,brightness);
	}
	//---------------------------------------------------------------------------------
	public int hardbleach(int p)
	//---------------------------------------------------------------------------------
	{
			     int r = 0xff & (p >> 16); 
		         int g = 0xff & (p >> 8);
		         int b = 0xff & (p);
		         float[] hsb = Color.RGBtoHSB(r, g, b, null);
		         float hue = hsb[0]; 
		         float saturation = hsb[1];
		         float brightness = hsb[2];
		         hsb=null;
		         // helder en lichtere kleur   
		         brightness *= 1.8f;  if( brightness > 1f) brightness =1f;
		         saturation *= 0.05f;  //if( saturation > 1f) saturation =1f;
		         //
		         return Color.HSBtoRGB(hue,saturation,brightness);
	}
	

	//---------------------------------------------------------------------------------
	public int blackbleach(int p)
	//---------------------------------------------------------------------------------
	{
			     int r = 0xff & (p >> 16); 
		         int g = 0xff & (p >> 8);
		         int b = 0xff & (p);
		         float[] hsb = Color.RGBtoHSB(r, g, b, null);
		         float hue = hsb[0]; 
		         float saturation = hsb[1];
		         float brightness = hsb[2];
		         hsb=null;
		         // haal alle kleur eruit   
		         //brightness *= 1.0f;  if( brightness > 1f) brightness =1f;
		         saturation *= 0.001f;  
		         hue = 0f;
		         //
		         return Color.HSBtoRGB(hue,saturation,brightness);
	}

	// Get binary treshold using Otsu's method  ==  ALLEEN GOED BIJ mooie spreiding van grijswaarden
	// http://www.labbookpages.co.uk/software/imgProc/otsuThreshold.html
	//-----------------------------------------------------------------------
	public int otsuTreshold(int[] pixels) 
	//-----------------------------------------------------------------------
	{
		   	        int lum=0; 
			        int[] histData = new int[256];
			        for (int i=0 ; i<256 ; i++)  histData[i] = 0;
			    	int total = pixels.length;
	                for(int i=0;i<total;i++) 
	                { 
	                 int p = pixels[i]; 
	                 lum = 0xff & ( p >> 16);    // RGB hebben in een grayscale toch alle dezelfde waarde 
	                 histData[ lum ] = histData[ lum ] + 1;
	                } 
			
			      	float sum = 0;
			    	for (int t=0 ; t<256 ; t++) sum += t * histData[t];
			    	float sumB = 0;
			    	int wB = 0;
			    	int wF = 0;

			    	float varMax = 0;
			    	int threshold = 0;

			    	for (int t=0 ; t<256 ; t++) {
			    	   wB += histData[t];               // Weight Background
			    	   if (wB == 0) continue;

			    	   wF = total - wB;                 // Weight Foreground
			    	   if (wF == 0) break;

			    	   sumB += (float) (t * histData[t]);

			    	   float mB = sumB / wB;            // Mean Background
			    	   float mF = (sum - sumB) / wF;    // Mean Foreground

			    	   // Calculate Between Class Variance
			    	   float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

			    	   // Check if new maximum found
			    	   if (varBetween > varMax) {
			    	      varMax = varBetween;
			    	      threshold = t;
			    	   }
			    	}
			        if( threshold == 0 ) {  
			        	do_error("OTSU did not find a threshold - Threshold will be overruled by 127");
			        	threshold = 127;
			        }
			        do_log(1," ==> OTSU " + threshold);
			        return threshold;
			 
	     }
	
	//-----------------------------------------------------------------------
	public int[] resizeImage(int[] dump, int iwidth , int iheigth , double scale , cmcImageRoutines.ImageType tipe)
	//-----------------------------------------------------------------------
	{
		resizeWidth=0;
		resizeHeigth=0;
		BufferedImage originalImage = null;
		switch ( tipe )
		{
		case ARGB : { originalImage = new BufferedImage( iwidth, iheigth , BufferedImage.TYPE_INT_ARGB); break; }
		case  RGB : { originalImage = new BufferedImage( iwidth, iheigth , BufferedImage.TYPE_INT_RGB); break; }
		default   : { do_error("Unsupported Image type [" + tipe + "]"); return null; }
		}
		originalImage.setRGB(0,0,iwidth,iheigth,dump,0,iwidth);
        //
	    resizeWidth  = (int)((double)scale * (double)iwidth);
	    resizeHeigth = (resizeWidth * iheigth) / iwidth;
	    int tipe2 = (tipe == cmcImageRoutines.ImageType.RGB ) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	    //
	    BufferedImage resizedImage = new BufferedImage(resizeWidth, resizeHeigth, tipe2);
	    Graphics2D g = resizedImage.createGraphics();
	    g.drawImage(originalImage, 0, 0, resizeWidth, resizeHeigth, null);
	    g.dispose();
	    // improve quality
	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        //
	    // extract pixels from bufferedimage
	    int[] pixels = new int[ resizeWidth * resizeHeigth ];
        PixelGrabber pg = new PixelGrabber( resizedImage , 0 , 0 , resizeWidth , resizeHeigth , pixels , 0 , resizeWidth); 
        try { 
          pg.grabPixels(); 
        } 
        catch( Exception e) { 
           do_error("Error at grabPixel " + e.getMessage());
           return null;
        }
	    //return resizedImage;
        return pixels;
	}

	//-----------------------------------------------------------------------
	public int getResizeWidth()
	//-----------------------------------------------------------------------
	{
       return resizeWidth;		
	}
	
	//-----------------------------------------------------------------------
	public int getResizeHeigth()
	//-----------------------------------------------------------------------
	{
		return resizeHeigth;
	}
	
	// if needed 
	//-----------------------------------------------------------------------
	public void writeBufferedImage(BufferedImage img , String FName)
	//-----------------------------------------------------------------------
	{
		try {
		  ImageIO.write(img, "png", new File(FName));
		}
		catch(Exception e ) {
			do_error("Writing image [" + FName + "] " + e.getMessage());
		}
	}
}
