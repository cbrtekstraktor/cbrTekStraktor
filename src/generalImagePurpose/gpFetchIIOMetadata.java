package generalImagePurpose;
import org.w3c.dom.*;

import cbrTekStraktorModel.cmcProcSettings;

import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;

import logger.logLiason;

public class gpFetchIIOMetadata {
	
	// DTD in appendix of this code
	
	/*  These are the nodes and attributes we are looking for to fetch JPG/PNG metadata
	 * 
JPG
[javax_imageio_jpeg_image_1.0][markerSequence][sof]numLines=808
[javax_imageio_jpeg_image_1.0][markerSequence][sof]samplesPerLine=1280
[javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]resUnits=1
[javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]Xdensity=300
[javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]Ydensity=300

PNG
[javax_imageio_png_1.0][IHDR]width=1103
[javax_imageio_png_1.0][IHDR]height=624
[javax_imageio_png_1.0][pHYs]pixelsPerUnitXAxis=3779
[javax_imageio_png_1.0][pHYs]pixelsPerUnitYAxis=3779
[javax_imageio_png_1.0][pHYs]unitSpecifier=meter

Generic (however there is a BUG both JPG and PNG implementation
[javax_imageio_1.0][Dimension][HorizontalPixelSize]value=0.26462027
[javax_imageio_1.0][Dimension][VerticalPixelSize]value=0.26462027

	 */

	
    enum IMAGE_TYPE { JPG, PNG , UNSUPPORTED }

	cmcProcSettings xMSet=null;
    logLiason logger = null;
    //
    private boolean verbose = true;
    private IMAGE_TYPE imaTipe = IMAGE_TYPE.UNSUPPORTED;
    private boolean formatHasBeenValidated = false;
    private double VerticalPixelSize=-1;     // The height of a pixel is millimeters
    private double HorizontalPixelSize=-1;   //The width of a pixel, in millimeters
    private int resUnits=-1;
    private int xDensity=-1;
    private int yDensity=-1;
    private int pixelsPerUnitXAxis=-1;
    private int pixelsPerUnitYAxis=-1;
    private String unitSpecifier=null;
    
    //
	private int width=-1;
	private int heigth =-1;
	private int physicalWidthDPI=-1;
	private int physicalHeigthDPI=-1;
	
	
	// just a handy element to store the current attribute and its decendence
	class iiomElement
	{
		int level = 0;
		int prevLevel=0;
		String[] arNodeName = new String[100];
		String Attribute=null;
		Object Value;
		//
		iiomElement()
		{
			for(int i=0;i<arNodeName.length;i++) arNodeName[i] = null;
			Attribute=null;
			level=0;
			prevLevel=0;
            arNodeName[level] = "root";			
		}
		void addNodeName(String sIn, int reqlevel)
		{
			level = reqlevel;
			arNodeName[level] = sIn;
			if( prevLevel > level ) {
				arNodeName[prevLevel] = null;
			}
			prevLevel=level;
			Attribute=null;  // if node then blank the attribute
			Value=null;
			//
			sho(false);
		}
		void setAttribute(String sIn,Object io)
		{
			Attribute=sIn;
			Value = io;
			//
			checkIfNeeded();
			//
			sho(false);
		}
		void sho(boolean force)
		{
		 if( (!verbose) && (force==false) ) return;
		 String sDisp="";
		 for(int i=1;i<arNodeName.length;i++)   // 0 = root so ignore
		 {
			 if( arNodeName[i] == null ) break;
			 sDisp += "[" + arNodeName[i] + "]";
		 }
		 if( Attribute != null ) {
			 sDisp += Attribute + "=" + Value;
		 }
		 do_log(1, sDisp);
		}
	}
	//
	iiomElement currElement=null;
	
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
	
    //------------------------------------------------------------
	public gpFetchIIOMetadata(cmcProcSettings im,logLiason ilog , boolean iverbose)
    //------------------------------------------------------------
	{
		verbose = iverbose;
		xMSet=im;
		logger=ilog;
		currElement = new iiomElement();
	}
	//
	public int getWidth() { return width; }
	public int getHeigth() { return heigth; }
	public int getPhysicalWidthDPI() { return physicalWidthDPI; }
	public int getPhysicalHeigthDPI() { return physicalHeigthDPI; }
	//
	
	//------------------------------------------------------------
    private void resetVars()
	//------------------------------------------------------------
    {
		width=-1;
        heigth =-1;
        physicalWidthDPI=-1;
        physicalHeigthDPI=-1;
        formatHasBeenValidated=false;
        imaTipe = IMAGE_TYPE.UNSUPPORTED;
        HorizontalPixelSize=-1;
        VerticalPixelSize=-1;
        resUnits=-1;
        xDensity=-1;
        yDensity=-1;
        pixelsPerUnitXAxis=-1;
        pixelsPerUnitYAxis=-1;
        unitSpecifier=null;
    }
	
    //------------------------------------------------------------
    public boolean parseMetadataTree( String fileName ) 
    //------------------------------------------------------------
    {
        //do_log(1,"Fetching IIOMetadata [" + fileName + "]");
    	//
        resetVars();
    	//
        String suf = xMSet.xU.GetSuffix(fileName);
        if( suf == null ) suf = "XYZ";
        suf = suf.trim().toUpperCase();
        if( suf.compareToIgnoreCase("PNG") == 0 ) imaTipe = IMAGE_TYPE.PNG;
        else
        if( suf.compareToIgnoreCase("JPG") == 0 ) imaTipe = IMAGE_TYPE.JPG;
        else
        if( suf.compareToIgnoreCase("JPEG")== 0 ) imaTipe = IMAGE_TYPE.PNG;
        else imaTipe = IMAGE_TYPE.UNSUPPORTED;
        if( imaTipe == IMAGE_TYPE.UNSUPPORTED ) {
        	do_error("File [" + fileName +"] is an unsupported image type");
        	return false;
        }
        //
        try {
            File file = new File( fileName );
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            //
            if (readers.hasNext()) {
                // pick the first available ImageReader
                ImageReader reader = readers.next();
                // attach source to the reader
                reader.setInput(iis, true);
                // read metadata of first image
                IIOMetadata metadata = reader.getImageMetadata(0);
                //
                String[] names = metadata.getMetadataFormatNames();
                int length = names.length;
                for (int i = 0; i < length; i++) {
                    //System.out.println( "Format name: " + names[ i ] );
                    displayMetadata(metadata.getAsTree(names[i]));
                }
            }
        }
        catch (Exception e) { // do not return there is a know bug on the IIOMetadata tree reader
            do_error("Reading image IIOMetadata [" + e.getMessage() + "]");
            // check correct error mezssage
        }
        //
        boolean ib = calculateVars();
        return ib;
    }

    //------------------------------------------------------------
    private boolean calculateVars()
    //------------------------------------------------------------
    {
    	boolean isOK=true;
    	//
    	if( imaTipe == IMAGE_TYPE.JPG ) {
    		physicalWidthDPI  = xDensity;
    		physicalHeigthDPI = yDensity;
    		// try via PixelSize if not set via xDesnity
    		if( (physicalWidthDPI <= 0) && (HorizontalPixelSize>0) && (VerticalPixelSize>0)) {
    			physicalWidthDPI =  (int)((double)25.4 / HorizontalPixelSize);
    			physicalHeigthDPI =  (int)((double)25.4 / VerticalPixelSize);
    			//do_error("Calc " + physicalWidthDPI );
    		}
    	}
    	//
    	else if( imaTipe == IMAGE_TYPE.PNG ) {
    		if( (physicalWidthDPI <= 0) && (HorizontalPixelSize>0) && (VerticalPixelSize>0)) {
    			physicalWidthDPI  =  (int)((double)25.4 / HorizontalPixelSize);
    			physicalHeigthDPI =  (int)((double)25.4 / VerticalPixelSize);
    			//do_error("Calc " + physicalWidthDPI );
    		}

        }
    	/*
    	// post process - cappen
    	if( ((physicalWidthDPI < MIN_DPI_THRESHOLD) && (physicalWidthDPI >= 0)) || 
    		((physicalHeigthDPI< MIN_DPI_THRESHOLD) && (physicalHeigthDPI>= 0)) ) {
    		do_log(1,"Physical{Width,Heith}DPI [" + physicalWidthDPI + "," + physicalHeigthDPI + "] is out of bound [Max=" + MIN_DPI_THRESHOLD + "] and has been set to invalid");
    		physicalWidthDPI  =  -1;
			physicalHeigthDPI =  -1;
		}
		*/
        if( verbose ) do_log(1,getReport()); 	
    	//
    	return isOK;
    }
    
    //------------------------------------------------------------
    public String getReport()
    //------------------------------------------------------------
    {
    	String s = " parse results\n"+
    	    	"               GEN.Type [" + imaTipe + "]\n" +
    	     	"              GEN.Width [" + width + "]\n" +
    	     	"             GEN.Heigth [" + heigth + "]\n" +
    	     	"GEN.HorizontalPixelSize [" + HorizontalPixelSize + "]\n" +
    	        "  GEN.VerticalPixelSize [" + VerticalPixelSize + "]\n" +
    	        "           JPG.xDensity [" + xDensity + "]\n" +
    	        "           JPG.yDensity [" + yDensity + "]\n" +
    	        "           JPG.resUnits [" + resUnits + "]\n" +
    	        " PNG.PixelsPerUnitXAxis [" + pixelsPerUnitXAxis + "]\n" +
    	        " PNG.PixelsPerUnitYAxis [" + pixelsPerUnitYAxis + "]\n" +
    	        "      PNG.UnitSpecifier [" + unitSpecifier + "]\n" +
    	        "  CALC.PhysicalWidthDPI [" + physicalWidthDPI + "]\n" +
    	     	" CALC.PhysicalHeigthDPI [" + physicalHeigthDPI + "]";
    	        //
        return s;	    	
    }
    
    //------------------------------------------------------------
    void displayMetadata(Node root) 
    //------------------------------------------------------------
    {
        displayMetadata(root, 1);
    }

    //------------------------------------------------------------
    void displayMetadata(Node node, int level) 
    //------------------------------------------------------------
    {
        currElement.addNodeName(node.getNodeName(), level);
        //        
        NamedNodeMap map = node.getAttributes();
        if (map != null) {
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                currElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {           // no children, so close element and return
            return;
        }

        // children, so close current tag
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

    }
    
    //------------------------------------------------------------
    private void checkIfNeeded()
    //------------------------------------------------------------
     {
       if( currElement.Attribute == null ) return;
       if( currElement.Value == null ) return;
       // has format been validated
       if( !formatHasBeenValidated ) {
    	   if( currElement.arNodeName[1].compareToIgnoreCase("javax_imageio_jpeg_image_1.0") == 0 ) {
    		   formatHasBeenValidated=true;
    		   if( imaTipe != IMAGE_TYPE.JPG ) {
    			   do_error("This is JPG file however suffix is not JPG");
    		   }
    		   else { if (verbose ) do_log(9,"Format assessed corectly to be " + imaTipe); }
    	   }
    	   else
    	   if( currElement.arNodeName[1].compareToIgnoreCase("javax_imageio_png_1.0") == 0 ) {
        	   formatHasBeenValidated=true;
        	   if( imaTipe != IMAGE_TYPE.PNG ) {
    			   do_error("This is PNG file however suffix is not PNG");
    		   }
    		   else { if (verbose ) do_log(9,"Format assessed corectly to be " + imaTipe); }
           }
       }
       
       // Generic
       // [javax_imageio_1.0][Dimension][HorizontalPixelSize]value=0.26462027
       // [javax_imageio_1.0][Dimension][VerticalPixelSize]value=0.26462027
       if( currElement.arNodeName[1].compareToIgnoreCase("javax_imageio_1.0") == 0 ) {
         if ( currElement.arNodeName[2].compareToIgnoreCase("Dimension") == 0 ) {
        	 if ( currElement.arNodeName[3].compareToIgnoreCase("HorizontalPixelSize") == 0 ) {
        		 //do_log(1,"??" + currElement.Attribute) ; currElement.sho(true);
        		 if ( currElement.Attribute.compareToIgnoreCase("value") == 0 ) {
        		  double dd = xMSet.xU.NaarDouble(""+currElement.Value);
            	  if (dd > 0 ) HorizontalPixelSize = dd;
        		 }
             }
        	 else
        	 if ( currElement.arNodeName[3].compareToIgnoreCase("VerticalPixelSize") == 0 ) {
        		 if ( currElement.Attribute.compareToIgnoreCase("value") == 0 ) {
        	       double dd = xMSet.xU.NaarDouble(""+currElement.Value);
            	   if (dd > 0 ) VerticalPixelSize = dd;
        		 }
              }
         }
       }
       else  // JPG
       // [javax_imageio_jpeg_image_1.0][markerSequence][sof]numLines=808
       // [javax_imageio_jpeg_image_1.0][markerSequence][sof]samplesPerLine=1280
       // [javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]resUnits=1
       // [javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]Xdensity=300
       // [javax_imageio_jpeg_image_1.0][JPEGvariety][app0JFIF]Ydensity=300
       if( currElement.arNodeName[1].compareToIgnoreCase("javax_imageio_jpeg_image_1.0") == 0 ) {
    	     if ( currElement.arNodeName[2].compareToIgnoreCase("markerSequence") == 0 ) {
    	    	 if ( currElement.arNodeName[3].compareToIgnoreCase("sof") == 0 ) {
    	    		 if ( currElement.Attribute.compareToIgnoreCase("numLines") == 0 ) {
    	                   int li = xMSet.xU.NaarInt(""+currElement.Value);
    	            	   if (li > 0 ) heigth = li;
    	        	 }
    	    		 else
    	    		 if ( currElement.Attribute.compareToIgnoreCase("samplesPerLine") == 0 ) {
      	                   int li = xMSet.xU.NaarInt(""+currElement.Value);
      	            	   if (li > 0 ) width = li;
      	        	 }
    	    	 }    	 
    	     }
    	     else
   	    	 if ( currElement.arNodeName[2].compareToIgnoreCase("JPEGvariety") == 0 ) {
   	    	     if ( currElement.arNodeName[3].compareToIgnoreCase("app0JFIF") == 0 ) {
   	    	    	if ( currElement.Attribute.compareToIgnoreCase("resUnits") == 0 ) {
 	                   int li = xMSet.xU.NaarInt(""+currElement.Value);
 	            	   if (li > 0 ) resUnits = li;
 	        	    }
   	    	    	if ( currElement.Attribute.compareToIgnoreCase("Xdensity") == 0 ) {
  	                   int li = xMSet.xU.NaarInt(""+currElement.Value);
  	            	   if (li > 0 ) xDensity = li;
  	        	    }
   	    	    	if ( currElement.Attribute.compareToIgnoreCase("Ydensity") == 0 ) {
  	                   int li = xMSet.xU.NaarInt(""+currElement.Value);
  	            	   if (li > 0 ) yDensity = li;
  	        	    }
   	    	     }	
       	     }
       }
       else  // PNG
   	   //[javax_imageio_png_1.0][IHDR]width=1103
   	   //[javax_imageio_png_1.0][IHDR]height=624
   	   //[javax_imageio_png_1.0][pHYs]pixelsPerUnitXAxis=3779
   	   //[javax_imageio_png_1.0][pHYs]pixelsPerUnitYAxis=3779
   	   //[javax_imageio_png_1.0][pHYs]unitSpecifier=meter
       if( currElement.arNodeName[1].compareToIgnoreCase("javax_imageio_png_1.0") == 0 ) {
   	     if ( currElement.arNodeName[2].compareToIgnoreCase("IHDR") == 0 ) {
   	    	if ( currElement.Attribute.compareToIgnoreCase("width") == 0 ) {
   	    	  int li = xMSet.xU.NaarInt(""+currElement.Value);
         	   if (li > 0 ) width = li;
   	    	}
   	    	else
   	    	if ( currElement.Attribute.compareToIgnoreCase("height") == 0 ) {
     	    	  int li = xMSet.xU.NaarInt(""+currElement.Value);
           	      if (li > 0 ) heigth = li;
     	    }
   	     }
         //
   	     else
   	     if ( currElement.arNodeName[2].compareToIgnoreCase("pHYs") == 0 ) {
   	    	if ( currElement.Attribute.compareToIgnoreCase("pixelsPerUnitXAxis") == 0 ) {
     	    	  int li = xMSet.xU.NaarInt(""+currElement.Value);
           	      if (li > 0 ) pixelsPerUnitXAxis = li;
     	    }
   	    	else
   	    	if ( currElement.Attribute.compareToIgnoreCase("pixelsPerUnitYAxis") == 0 ) {
   	    	  int li = xMSet.xU.NaarInt(""+currElement.Value);
         	      if (li > 0 ) pixelsPerUnitYAxis = li;
   	        }
   	    	else
   	    	if ( currElement.Attribute.compareToIgnoreCase("unitSpecifier") == 0 ) {
   	    	   unitSpecifier = (""+currElement.Value).trim();
            }
   	     }
     	    
       }
    	   
    }
    
    
}


/*
 * 



PNG

Generic (however there is a BUG both JPG and PNG implementation


Standard (Plug-in Neutral) Metadata Format Specification

The plug-in neutral "javax_imageio_1.0" format consists of a root node named "javax_imageio_1.0" which has child nodes "chroma", "compression", "dimension", "document", "text", "tile", and "transparency". The format is described by the following DTD:

<!DOCTYPE "javax_imageio_1.0" [

  <!ELEMENT "javax_imageio_1.0" (Chroma?, Compression?, Data?, Dimension?, 
    Document?, Text?, Transparency?)>

    <!ELEMENT "Chroma" (ColorSpaceType?, NumChannels?, Gamma?, 
      BlackIsZero?, Palette?, BackgroundIndex?, BackgroundColor?)>
      <!-- Chroma (color) information --> 

      <!ELEMENT "ColorSpaceType" EMPTY>
        <!-- The raw color space of the image --> 
        <!ATTLIST "ColorSpaceType" "name" ("XYZ" | "Lab" | "Luv" | 
          "YCbCr" | "Yxy" | "YCCK" | "PhotoYCC" | "RGB" | "GRAY" | "HSV" | 
          "HLS" | "CMYK" | "CMY" | "2CLR" | "3CLR" | "4CLR" | "5CLR" | 
          "6CLR" | "7CLR" | "8CLR" | "9CLR" | "ACLR" | "BCLR" | "CCLR" | 
          "DCLR" | "ECLR" | "FCLR") #REQUIRED>

      <!ELEMENT "NumChannels" EMPTY>
        <!-- The number of channels in the raw image, including alpha --> 
        <!ATTLIST "NumChannels" "value" #CDATA #REQUIRED>
          <!-- Data type: List of Integer -->

      <!ELEMENT "Gamma" EMPTY>
        <!-- The image gamma --> 
        <!ATTLIST "Gamma" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "BlackIsZero" EMPTY>
        <!-- True if smaller values represent darker shades --> 
        <!ATTLIST "BlackIsZero" "value" ("TRUE" | "FALSE") "TRUE">

      <!ELEMENT "Palette" (PaletteEntry)*>
        <!-- Palette-color information --> 

        <!ELEMENT "PaletteEntry" EMPTY>
          <!-- A palette entry --> 
          <!ATTLIST "PaletteEntry" "index" #CDATA #REQUIRED>
            <!-- The index of the palette entry --> 
            <!-- Data type: Integer -->
          <!ATTLIST "PaletteEntry" "red" #CDATA #REQUIRED>
            <!-- The red value for the palette entry --> 
            <!-- Data type: Integer -->
          <!ATTLIST "PaletteEntry" "green" #CDATA #REQUIRED>
            <!-- The green value for the palette entry --> 
            <!-- Data type: Integer -->
          <!ATTLIST "PaletteEntry" "blue" #CDATA #REQUIRED>
            <!-- The blue value for the palette entry --> 
            <!-- Data type: Integer -->
          <!ATTLIST "PaletteEntry" "alpha" #CDATA "255">
            <!-- The alpha value for the palette entry --> 
            <!-- Data type: Integer -->

      <!ELEMENT "BackgroundIndex" EMPTY>
        <!-- A palette index to be used as a background --> 
        <!ATTLIST "BackgroundIndex" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "BackgroundColor" EMPTY>
        <!-- An RGB triple to be used as a background --> 
        <!ATTLIST "BackgroundColor" "red" #CDATA #REQUIRED>
          <!-- The red background value --> 
          <!-- Data type: Integer -->
        <!ATTLIST "BackgroundColor" "green" #CDATA #REQUIRED>
          <!-- The green background value --> 
          <!-- Data type: Integer -->
        <!ATTLIST "BackgroundColor" "blue" #CDATA #REQUIRED>
          <!-- The blue background value --> 
          <!-- Data type: Integer -->

    <!ELEMENT "Compression" (CompressionTypeName?, Lossless?, 
      NumProgressiveScans?, BitRate?)>
      <!-- Compression information --> 

      <!ELEMENT "CompressionTypeName" EMPTY>
        <!-- The name of the compression scheme in use --> 
        <!ATTLIST "CompressionTypeName" "value" #CDATA #REQUIRED>
          <!-- Data type: String -->

      <!ELEMENT "Lossless" EMPTY>
        <!-- True if the compression scheme is lossless --> 
        <!ATTLIST "Lossless" "value" ("TRUE" | "FALSE") "TRUE">

      <!ELEMENT "NumProgressiveScans" EMPTY>
        <!-- The number of progressive scans used in the image encoding --> 
        <!ATTLIST "NumProgressiveScans" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "BitRate" EMPTY>
        <!-- The estimated bit rate of the compression scheme --> 
        <!ATTLIST "BitRate" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

    <!ELEMENT "Data" (PlanarConfiguration?, SampleFormat?, BitsPerSample?, 
      SignificantBitsPerSample?, SampleMSB?)>
      <!-- Information on the image layout --> 

      <!ELEMENT "PlanarConfiguration" EMPTY>
        <!-- The organization of image samples in the stream --> 
        <!ATTLIST "PlanarConfiguration" "value" ("PixelInterleaved" | 
          "PlaneInterleaved" | "LineInterleaved" | "TileInterleaved")
           #REQUIRED>

      <!ELEMENT "SampleFormat" EMPTY>
        <!-- The numeric format of image samples --> 
        <!ATTLIST "SampleFormat" "value" ("SignedIntegral" | 
          "UnsignedIntegral" | "Real" | "Index") #REQUIRED>

      <!ELEMENT "BitsPerSample" EMPTY>
        <!-- The number of bits per sample --> 
        <!ATTLIST "BitsPerSample" "value" #CDATA #REQUIRED>
          <!-- A list of integers, one per channel --> 
          <!-- Data type: List of Integer -->
          <!-- Min length: 1 -->

      <!ELEMENT "SignificantBitsPerSample" EMPTY>
        <!-- The number of significant bits per sample --> 
        <!ATTLIST "SignificantBitsPerSample" "value" #CDATA #REQUIRED>
          <!-- A list of integers, one per channel --> 
          <!-- Data type: List of Integer -->
          <!-- Min length: 1 -->

      <!ELEMENT "SampleMSB" EMPTY>
        <!-- The position of the most significant bit of each sample --> 
        <!ATTLIST "SampleMSB" "value" #CDATA #REQUIRED>
          <!-- A list of integers, one per channel --> 
          <!-- Data type: List of Integer -->
          <!-- Min length: 1 -->

    <!ELEMENT "Dimension" (PixelAspectRatio?, ImageOrientation?, 
      HorizontalPixelSize?, VerticalPixelSize?, 
      HorizontalPhysicalPixelSpacing?, VerticalPhysicalPixelSpacing?, 
      HorizontalPosition?, VerticalPosition?, HorizontalPixelOffset?, 
      VerticalPixelOffset?, HorizontalScreenSize?, VerticalScreenSize?)>
      <!-- Dimension information --> 

      <!ELEMENT "PixelAspectRatio" EMPTY>
        <!-- The width of a pixel divided by its height --> 
        <!ATTLIST "PixelAspectRatio" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "ImageOrientation" EMPTY>
        <!-- The desired orientation of the image in terms of flips and 
             counter-clockwise rotations --> 
        <!ATTLIST "ImageOrientation" "value" ("Normal" | "Rotate90" | 
          "Rotate180" | "Rotate270" | "FlipH" | "FlipV" | 
          "FlipHRotate90" | "FlipVRotate90") #REQUIRED>

      <!ELEMENT "HorizontalPixelSize" EMPTY>
        <!-- The width of a pixel, in millimeters, as it should be rendered 
             on media --> 
        <!ATTLIST "HorizontalPixelSize" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "VerticalPixelSize" EMPTY>
        <!-- The height of a pixel, in millimeters, as it should be 
             rendered on media --> 
        <!ATTLIST "VerticalPixelSize" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "HorizontalPhysicalPixelSpacing" EMPTY>
        <!-- The horizontal distance in the subject of the image, in 
             millimeters, represented by one pixel at the center of the 
             image --> 
        <!ATTLIST "HorizontalPhysicalPixelSpacing" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "VerticalPhysicalPixelSpacing" EMPTY>
        <!-- The vertical distance in the subject of the image, in 
             millimeters, represented by one pixel at the center of the 
             image --> 
        <!ATTLIST "VerticalPhysicalPixelSpacing" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "HorizontalPosition" EMPTY>
        <!-- The horizontal position, in millimeters, where the image 
             should be rendered on media --> 
        <!ATTLIST "HorizontalPosition" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "VerticalPosition" EMPTY>
        <!-- The vertical position, in millimeters, where the image should 
             be rendered on media --> 
        <!ATTLIST "VerticalPosition" "value" #CDATA #REQUIRED>
          <!-- Data type: Float -->

      <!ELEMENT "HorizontalPixelOffset" EMPTY>
        <!-- The horizonal position, in pixels, where the image should be 
             rendered onto a raster display --> 
        <!ATTLIST "HorizontalPixelOffset" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "VerticalPixelOffset" EMPTY>
        <!-- The vertical position, in pixels, where the image should be 
             rendered onto a raster display --> 
        <!ATTLIST "VerticalPixelOffset" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "HorizontalScreenSize" EMPTY>
        <!-- The width, in pixels, of the raster display into which the 
             image should be rendered --> 
        <!ATTLIST "HorizontalScreenSize" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "VerticalScreenSize" EMPTY>
        <!-- The height, in pixels, of the raster display into which the 
             image should be rendered --> 
        <!ATTLIST "VerticalScreenSize" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

    <!ELEMENT "Document" (FormatVersion?, SubimageInterpretation?, 
      ImageCreationTime?, ImageModificationTime?)>
      <!-- Document information --> 

      <!ELEMENT "FormatVersion" EMPTY>
        <!-- The version of the format used by the stream --> 
        <!ATTLIST "FormatVersion" "value" #CDATA #REQUIRED>
          <!-- Data type: String -->

      <!ELEMENT "SubimageInterpretation" EMPTY>
        <!-- The interpretation of this image in relation to the other 
             images stored in the same stream --> 
        <!ATTLIST "SubimageInterpretation" "value" ("Standalone" | 
          "SinglePage" | "FullResolution" | "ReducedResolution" | 
          "PyramidLayer" | "Preview" | "VolumeSlice" | "ObjectView" | 
          "Panorama" | "AnimationFrame" | "TransparencyMask" | 
          "CompositingLayer" | "SpectralSlice" | "Unknown") #REQUIRED>

      <!ELEMENT "ImageCreationTime" EMPTY>
        <!-- The time of image creation --> 
        <!ATTLIST "ImageCreationTime" "year" #CDATA #REQUIRED>
          <!-- The full year (e.g., 1967, not 67) --> 
          <!-- Data type: Integer -->
        <!ATTLIST "ImageCreationTime" "month" #CDATA #REQUIRED>
          <!-- The month, with January = 1 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 1 (inclusive) -->
          <!-- Max value: 12 (inclusive) -->
        <!ATTLIST "ImageCreationTime" "day" #CDATA #REQUIRED>
          <!-- The day of the month --> 
          <!-- Data type: Integer -->
          <!-- Min value: 1 (inclusive) -->
          <!-- Max value: 31 (inclusive) -->
        <!ATTLIST "ImageCreationTime" "hour" #CDATA "0">
          <!-- The hour from 0 to 23 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 23 (inclusive) -->
        <!ATTLIST "ImageCreationTime" "minute" #CDATA "0">
          <!-- The minute from 0 to 59 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 59 (inclusive) -->
        <!ATTLIST "ImageCreationTime" "second" #CDATA "0">
          <!-- The second from 0 to 60 (60 = leap second) --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 60 (inclusive) -->

      <!ELEMENT "ImageModificationTime" EMPTY>
        <!-- The time of the last image modification --> 
        <!ATTLIST "ImageModificationTime" "year" #CDATA #REQUIRED>
          <!-- The full year (e.g., 1967, not 67) --> 
          <!-- Data type: Integer -->
        <!ATTLIST "ImageModificationTime" "month" #CDATA #REQUIRED>
          <!-- The month, with January = 1 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 1 (inclusive) -->
          <!-- Max value: 12 (inclusive) -->
        <!ATTLIST "ImageModificationTime" "day" #CDATA #REQUIRED>
          <!-- The day of the month --> 
          <!-- Data type: Integer -->
          <!-- Min value: 1 (inclusive) -->
          <!-- Max value: 31 (inclusive) -->
        <!ATTLIST "ImageModificationTime" "hour" #CDATA "0">
          <!-- The hour from 0 to 23 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 23 (inclusive) -->
        <!ATTLIST "ImageModificationTime" "minute" #CDATA "0">
          <!-- The minute from 0 to 59 --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 59 (inclusive) -->
        <!ATTLIST "ImageModificationTime" "second" #CDATA "0">
          <!-- The second from 0 to 60 (60 = leap second) --> 
          <!-- Data type: Integer -->
          <!-- Min value: 0 (inclusive) -->
          <!-- Max value: 60 (inclusive) -->

    <!ELEMENT "Text" (TextEntry)*>
      <!-- Text information --> 

      <!ELEMENT "TextEntry" EMPTY>
        <!-- A text entry --> 
        <!ATTLIST "TextEntry" "keyword" #CDATA #IMPLIED>
          <!-- A keyword associated with the text entry --> 
          <!-- Data type: String -->
        <!ATTLIST "TextEntry" "value" #CDATA #REQUIRED>
          <!-- the text entry --> 
          <!-- Data type: String -->
        <!ATTLIST "TextEntry" "language" #CDATA #IMPLIED>
          <!-- The language of the text --> 
          <!-- Data type: String -->
        <!ATTLIST "TextEntry" "encoding" #CDATA #IMPLIED>
          <!-- The encoding of the text --> 
          <!-- Data type: String -->
        <!ATTLIST "TextEntry" "compression" ("none" | "lzw" | "zip" | 
          "bzip" | "other") "none">
          <!-- The method used to compress the text --> 

    <!ELEMENT "Transparency" (Alpha?, TransparentIndex?, 
      TransparentColor?, TileTransparencies?, TileOpacities?)>
      <!-- Transparency information --> 

      <!ELEMENT "Alpha" EMPTY>
        <!-- The type of alpha information contained in the image --> 
        <!ATTLIST "Alpha" "value" ("none" | "premultiplied" | 
          "nonpremultiplied") "none">

      <!ELEMENT "TransparentIndex" EMPTY>
        <!-- A palette index to be treated as transparent --> 
        <!ATTLIST "TransparentIndex" "value" #CDATA #REQUIRED>
          <!-- Data type: Integer -->

      <!ELEMENT "TransparentColor" EMPTY>
        <!-- An RGB color to be treated as transparent --> 
        <!ATTLIST "TransparentColor" "value" #CDATA #REQUIRED>
          <!-- Data type: List of Integer -->

      <!ELEMENT "TileTransparencies" (TransparentTile)*>
        <!-- A list of completely transparent tiles --> 

        <!ELEMENT "TransparentTile" EMPTY>
          <!-- The index of a completely transparent tile --> 
          <!ATTLIST "TransparentTile" "x" #CDATA #REQUIRED>
            <!-- The tile's X index --> 
            <!-- Data type: Integer -->
          <!ATTLIST "TransparentTile" "y" #CDATA #REQUIRED>
            <!-- The tile's Y index --> 
            <!-- Data type: Integer -->

      <!ELEMENT "TileOpacities" (OpaqueTile)*>
        <!-- A list of completely opaque tiles --> 

        <!ELEMENT "OpaqueTile" EMPTY>
          <!-- The index of a completely opaque tile --> 
          <!ATTLIST "OpaqueTile" "x" #CDATA #REQUIRED>
            <!-- The tile's X index --> 
            <!-- Data type: Integer -->
          <!ATTLIST "OpaqueTile" "y" #CDATA #REQUIRED>
            <!-- The tile's Y index --> 
            <!-- Data type: Integer -->
]>


 */
