package imageProcessing;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

public class cmcProcFindMargins {
	private int imgWidth=-1;
	private int imgHeigth=-1;
	
	private int MAXWHITE = 50;    // maximum percentage zwarte pixels in de marge
	private int MinX = -1;
	private int MinY = -1;
	private int MaxX = -1;
	private int MaxY = -1;
	
	//-----------------------------------------------------------------------
	public cmcProcFindMargins(int iw, int ih , int[] pixels)
	//-----------------------------------------------------------------------
	{
		imgWidth = iw; 
        imgHeigth = ih; 
        
        MinY = findTopY(pixels);
        MaxY = findBottomY(pixels);
        MinX = findLeftX(pixels);
        MaxX = findRightX(pixels);
        // post proc
        if( ((MaxX - MinX) >= imgWidth) || (MinX < 0)  ) {
        	MinX = 0;
        	MaxX = imgWidth - 1;
        }
        if( ((MaxY - MinY) >= imgHeigth) || (MinY < 0) ) {
        	MinY = 0;
        	MaxY = imgHeigth - 1;
        }
    }
	
	/*
	//-----------------------------------------------------------------------
	public void sho()
	//-----------------------------------------------------------------------
	{
	     System.out.println( "("+MinX+","+MinY+") " + MaxX + " " + MaxY);
	     System.out.println( "" + getPayLoadPoint().x + " " + getPayLoadPoint().y + " " + getPayLoadDimension().width + " " + getPayLoadDimension().height );
	}
	*/
	//-----------------------------------------------------------------------
	public Point getPayLoadPoint()
	//-----------------------------------------------------------------------
	{
		Point x = new Point(MinX,MinY);
		return x;
	}
	//-----------------------------------------------------------------------
	public Dimension getPayLoadDimension()
	//-----------------------------------------------------------------------
	{
		Dimension x = new Dimension(MaxX - MinX + 1 , MaxY - MinY + 1);
		return x;
	}
	
	//-----------------------------------------------------------------------
	private int findTopY(int[] pixels)
	//-----------------------------------------------------------------------
	{
		return findVertical(pixels,true);
	}
	
	//-----------------------------------------------------------------------
	private int findBottomY(int[] pixels)
	//-----------------------------------------------------------------------
	{
		return findVertical(pixels,false);
	}
	
	//-----------------------------------------------------------------------
	private int findLeftX(int[] pixels)
	//-----------------------------------------------------------------------
	{
		return findHorizontal(pixels,true);
	}
	
	//-----------------------------------------------------------------------
	private int findRightX(int[] pixels)
	//-----------------------------------------------------------------------
	{
		return findHorizontal(pixels,false);
	}
	
	//-----------------------------------------------------------------------
	private int findVertical(int[] pixels,boolean top)
	//-----------------------------------------------------------------------
	{ // scroll a line down and count the white pixels
	  int teller=0;
	  int loc=-1;
	  int tolerantie = (imgWidth / 100 ) * MAXWHITE;
	  int hit=-1;
	  for(int y=0;y<imgHeigth;y++)
	  {
		  if( top ) loc = y * imgWidth; else loc = (imgHeigth - y - 1 ) * imgWidth;
		  teller=0;
		  for(int x=0;x<imgWidth;x++)
		  {
			  if( (pixels[ loc + x] & 0x00ffffff) != 0x00ffffff ) teller++;
			  if( teller > tolerantie ) { hit=y; break; }
		  }
		  if( hit >= 0) break;
	  }
	  if( top == false ) {
		  if ( hit <= 0 ) return imgHeigth - 1;
		  return  imgHeigth - hit - 1;
	  }
	  else {
		  if( hit <= 0 ) return 0;
		  return hit - 1;
	  }
	}
	
	//-----------------------------------------------------------------------
	private int findHorizontal(int[] pixels, boolean left)
	//-----------------------------------------------------------------------
	{
		int loc = -1;
		int teller=0;
		int tolerantie = (imgHeigth / 100 ) * MAXWHITE;
		int hit=-1;
		for(int x=0;x<imgWidth;x++)
		{
			if( left ) loc = x; else loc = imgWidth - x - 1;
			teller=0;
			for(int y=0;y<imgHeigth;y++)
			{
				if( (pixels[ loc + (imgWidth * y)] & 0x00ffffff) != 0x00ffffff ) teller++;
				if( teller > tolerantie ) { hit=x; break; }
			}
			if( hit >= 0 ) break;
		}
		if( left ) {
			if( hit <= 0) return 0;
			return hit -1;
		}
		else {
			if( hit <= 0) return imgWidth - 1;
			return imgWidth - hit - 1;
		}
	}

}
