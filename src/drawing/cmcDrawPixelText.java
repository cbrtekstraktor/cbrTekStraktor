package drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
//import java.util.HashMap;
//import java.util.Map;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcSettings;

public class cmcDrawPixelText {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private FontMetrics metrics;
    private Color clr;
    //private Map<Character, Image> images;
	private int txtBreedte=-1;
	private int txtHoogte=-1;
	
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
    
	public cmcDrawPixelText(cmcProcSettings is,logLiason ilog)
	{
		xMSet = is;
		logger=ilog;
	}

	/*
	 * graphics.setFont(new Font("monospaced", Font.PLAIN, 24));
characterGenerator = new CharacterImageGenerator(graphics.getFontMetrics(), Color.WHITE);
	 */
	
	/*
	public void huppel (Font f, Color col) {
		BufferedImage bi = new BufferedImage( 100 , 100 , BufferedImage.TYPE_INT_RGB);
		//Graphics2D ig = bi.createGraphics();
		Graphics g = bi.getGraphics();
	    g.setFont(f);    
		metrics = g.getFontMetrics();
        clr = col;
        images = new HashMap<Character, Image>();
    }


    public Image getImage(char c) {
        if(images.containsKey(c))
            return images.get(c);

        Rectangle2D bounds = new TextLayout(Character.toString(c), metrics.getFont(), metrics.getFontRenderContext()).getOutline(null).getBounds();
        if(bounds.getWidth() == 0 || bounds.getHeight() == 0) {
            images.put(c, null);
            return null;
        }
        Image image = new BufferedImage((int)bounds.getWidth(), (int)bounds.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = image.getGraphics();
        g.setColor(C
        g.setColor(clr);
        g.setFont(metrics.getFont());
        g.drawString(Character.toString(c), 0, (int)(bounds.getHeight() - bounds.getMaxY()));

        images.put(c, image);
        return image;
    }
	*/

    //---------------------------------------------------------------------------------
	private void setFont (Font f, Color col) 
    //---------------------------------------------------------------------------------
	{
		
		BufferedImage bi = new BufferedImage( 100 , 100 , BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
	    g.setFont(f);    
		metrics = g.getFontMetrics();
        clr = col;
    }

    //---------------------------------------------------------------------------------
    private int[] getPixelText(String sIn)
    //---------------------------------------------------------------------------------
    {
    	Rectangle2D bounds = new TextLayout( sIn, metrics.getFont(), metrics.getFontRenderContext()).getOutline(null).getBounds();
        if(bounds.getWidth() == 0 || bounds.getHeight() == 0) {
            return null;
        }
        Image img = new BufferedImage((int)bounds.getWidth()+10, (int)bounds.getHeight(), BufferedImage.TYPE_INT_RGB);
        txtBreedte = img.getWidth(null);
 	    txtHoogte  = img.getHeight(null);
 	    //
        Graphics g = img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,txtBreedte,txtHoogte);
        g.setColor(clr);
        g.setFont(metrics.getFont());
        g.drawString(sIn, 0, (int)(bounds.getHeight() - bounds.getMaxY()));
   	    //
 	    if( (txtBreedte < 0) || (txtHoogte <0) ) {
 		   do_error("Height or width cannot be determined of string [" + sIn + "]");
 		   return null;
 	    }
 	    int[] srcpixels = new int[ txtHoogte * txtBreedte];
 	    try {
 		   PixelGrabber pg = new PixelGrabber( img , 0 , 0 , txtBreedte , txtHoogte , srcpixels , 0 , txtBreedte);
 		   pg.grabPixels();
 	    }
 	    catch (Exception e ) {
 		   do_error("Pixelgrabber on [" + sIn + "] " + e.getMessage());
 		   return null;
 	    }
 	    img=null;
 	    /*
        int rgb = clr.getRGB() & 0x00ffffff;
 	    for(int i=0;i<srcpixels.length;i++)
 	    {
 	    	if( (srcpixels[i] & 0x00ffffff) != rgb ) srcpixels[i] = 0xffffffff;
 	    }
 	    */
        return srcpixels;
    }
    
    //---------------------------------------------------------------------------------
  	private void pasteSnippet( int x, int y , int canvasbreedte , int[] snippet , int[] canvas)
  	//---------------------------------------------------------------------------------
  	{
  		//
  		for(int i=0;i<txtHoogte;i++)
  		{
  			for(int j=0;j<txtBreedte;j++)
  			{
  				int p = ((y + i)*canvasbreedte) + x + j;
  				int q = snippet[(txtBreedte*i)+j];
  				if( p < canvas.length ) canvas[p] = q;
  			}
  		}
  		snippet=null;
  	}

    //---------------------------------------------------------------------------------
	public void drawPixelText(Font f , Color col , int x , int y , int[]pixelcanvas , int canvasWidth , String sIn)
    //---------------------------------------------------------------------------------
	{
		txtBreedte=-1;
		txtHoogte=-1;
	    setFont(f,col);
	    int[] txtpixels = getPixelText(sIn);
	    if( txtpixels == null )  return;
	    pasteSnippet( x , y , canvasWidth , txtpixels , pixelcanvas );    
	    txtpixels=null;
	    return;
	}

}
