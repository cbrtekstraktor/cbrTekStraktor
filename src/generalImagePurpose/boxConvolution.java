package generalImagePurpose;

import java.awt.image.*;

import logger.logLiason;

/**
 * based on an article and source code by Jerry Huxtable
 */
public class boxConvolution 
{
	protected Kernel kernel;
	logLiason logger=null;
	
	// TODO :  detecteer een kruis matrix en voer alleen die multiplicaties uit
	
	//public static int ZERO_EDGES = 0;
	//public static int CLAMP_EDGES = 1;
	//public static int WRAP_EDGES = 2;

	private boolean isOK=true;
	
	int[] slider = null;

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
    private void debug(String sIn)
	//------------------------------------------------------------
    {
    	do_log(1,sIn);
    }
    
	//-----------------------------------------------------------------------
	public boxConvolution(logLiason ilog) 
	//-----------------------------------------------------------------------
	{
		logger=ilog;
		isOK=true;
	}

	//-----------------------------------------------------------------------
	public void setGaussianFilter(float radius)
	//-----------------------------------------------------------------------
	{
		kernel = makeKernel(radius);
		int cols2 = kernel.getWidth() / 2;
		int rows2 =	kernel.getHeight() / 2;
		// kernel moet oneven breedte hebben
		if( (cols2 * 2) == kernel.getWidth() ) {
			do_error("Only uneven width is supported");
			isOK=false;
		}
		//
		if( (rows2 * 2) == kernel.getHeight() ) {
			do_error("Only uneven heigth is supported");
			isOK=false;
       }
	}
	
	//-----------------------------------------------------------------------
	public void setSharpenFilter()
	//-----------------------------------------------------------------------
	{
		float[] matrix = { 0 , -1f , 0 ,    -1f , 5f , -1f ,   0  , -1f , 0 };
		float tot=0f;
		for(int i=0;i<matrix.length;i++) tot+= matrix[i];
		for(int i=0;i<matrix.length;i++) matrix[i] = matrix[i] / tot;
		kernel = new Kernel(3, 3, matrix);
	}
	
	//-----------------------------------------------------------------------
	public boolean getIsOk()
	//-----------------------------------------------------------------------
	{
		return isOK;
	}

	//-----------------------------------------------------------------------
	private void getSlider(int px , int py , int halvekernelbreedte , int halvekernelhoogte , int imageWidth , int imageHeight , int[] inPixels)
	//-----------------------------------------------------------------------
	{
		int j=-1;
		int yy=0;
		int xx=0;
		for(int y=(py - halvekernelhoogte);y<=(py + halvekernelhoogte);y++)
		{
			yy = y;
			if( yy<0) yy=0;
			if( yy>=imageHeight ) yy = imageHeight-1;
			for(int x=(px - halvekernelbreedte);x<=(px + halvekernelbreedte);x++)
			{
				xx=x;
				if( xx<0 ) xx = 0;
				if( xx>=imageWidth ) xx = imageWidth-1;
				j++;
				slider[j] = inPixels[ (yy*imageWidth) + xx ];
			}
		}
	}
    
	//-----------------------------------------------------------------------
	public void convolveAndTranspose(int[] inPixels, int[] outPixels, int width, int height, boolean alpha, int edgeAction) 
	//-----------------------------------------------------------------------
	{	
		float[] matrix = kernel.getKernelData( null );
		int cols2 = kernel.getWidth() / 2;
		int rows2 = kernel.getHeight() / 2;
		slider = new int[kernel.getWidth() * kernel.getHeight()];
		
		int curidx=-1;
		for(int y=0;y<height;y++)
		{
			for(int x=0;x<width;x++)
			{
				curidx++;
	            getSlider(x,y,cols2,rows2,width,height,inPixels);
	            //
	            float r = 0, g = 0, b = 0, a = 0;
			    for(int i=0;i<matrix.length;i++)
	            {
			    	int rgb = slider[i];
					a += matrix[i] * ((rgb >> 24) & 0xff);
					r += matrix[i] * ((rgb >> 16) & 0xff);
					g += matrix[i] * ((rgb >> 8) & 0xff);
					b += matrix[i] * (rgb & 0xff);
		        }
				int ia = alpha ? PixelUtils.clamp((int)(a+0.5)) : 0xff;
				int ir = PixelUtils.clamp((int)(r+0.5));
				int ig = PixelUtils.clamp((int)(g+0.5));
				int ib = PixelUtils.clamp((int)(b+0.5));
				//
				outPixels[ curidx ] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
			}
		}
		
	}

	/**
	 * Make a Gaussian blur kernel.
	 * KB - originally a 1D kernel nut transformed it into a 2D kernel by mirrorring the contents
	 */
	//-----------------------------------------------------------------------
	public Kernel makeKernel(float radius) 
	//-----------------------------------------------------------------------
		{	
		int r = (int)Math.ceil(radius);
		int rows = r*2+1;
		float[] matrix = new float[rows];
		float sigma = radius/3;
		float sigma22 = 2*sigma*sigma;
		float sigmaPi2 = 2*ImageMath.PI*sigma;
		float sqrtSigmaPi2 = (float)Math.sqrt(sigmaPi2);
		float radius2 = radius*radius;
		float total = 0;
		int index = 0;
		for (int row = -r; row <= r; row++) {
			float distance = row*row;
			if (distance > radius2)
				matrix[index] = 0;
			else
				matrix[index] = (float)Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
			total += matrix[index];
			index++;
		}
		for (int i = 0; i < rows; i++)
			matrix[i] /= total;
		//return new Kernel(rows, 1, matrix);
		
		
		// KB - make 2D
		float[] mat2 = new float[rows*rows];
		for(int i=0;i<mat2.length; i++) mat2[i]=0;
		for (int i = 0; i < rows; i++)
		{
			mat2[ (rows/2) + (i*rows) ] += matrix[i]/2;   //vertical
			mat2[ ((rows/2)*rows) + i ] += matrix[i]/2;   // horizontal /2 because TOTAL will double
		}
		// debug
		/*
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<rows;j++) debug(" " + mat2[ (i*rows) + j]);
			debug("");
		}
		*/
		//
		return new Kernel(rows, rows, mat2);
			
	}

	
}
