package generalImagePurpose;

import logger.logLiason;

public class cmcProcConvolution {

	logLiason logger=null;
	
	public enum kernelType { NOP , BLUR , SHARPEN , GAUSSIAN33 , EDGE };
	
	
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
	public cmcProcConvolution(logLiason ilog)
	//------------------------------------------------------------
	{
	  logger=ilog;
	}

	//------------------------------------------------------------
	public void convolve( int[] src , int[] tgt , kernelType t , int width , int heigth )
	//------------------------------------------------------------
	{
		switch ( t )
		{
		case NOP : { break; }
		case BLUR : { doGlow(src,tgt,width,heigth); break; }
		case SHARPEN : { doSharpen(src,tgt,width,heigth); break; }
		case EDGE : { doEdge(src,tgt,width,heigth); break; }
		case GAUSSIAN33 : { doGaussian(src,tgt,width,heigth); break; }
		default : { do_error("Unknown kernel type"); return; }
		}
	}

	//-----------------------------------------------------------------------
	private void doEdge(int[] inPixels, int[] outPixels, int width, int height)
	//-----------------------------------------------------------------------
	{
		boxConvolution gf = new boxConvolution(logger);  // bigger radius => more blur
		gf.setSharpenFilter();
		if( gf.getIsOk() ) {
		 try {
		   gf.convolveAndTranspose(inPixels, outPixels, width, height , false , 0);
		 }
		 catch(Exception e) {
			do_error("doEdge " + e.getMessage());
		 }
         //System.out.println("Done");
		}
		else {
			do_error("boxConvlution did not initialize ok");
		}

	}
		
	//-----------------------------------------------------------------------
	private void doGaussian(int[] inPixels, int[] outPixels, int width, int height)
	//-----------------------------------------------------------------------
	{
		boxConvolution gf = new boxConvolution(logger);  // bigger radius => more blur
		gf.setGaussianFilter(7f);
		if( gf.getIsOk() ) {
		 try {
         gf.convolveAndTranspose(inPixels, outPixels, width, height , false , 0);
		 }
		 catch(Exception e) {
			do_error("doGaussian " + e.getMessage());
		 }
         //System.out.println("Done");
		}
		else {
			do_error("doGaussian did not initialize ok");
		}
	}
	

	// first blur and then substract - see article by jim huxtable
	//-----------------------------------------------------------------------
	private void doSharpen(int[] inPixels, int[] outPixels, int width, int height)
	//-----------------------------------------------------------------------
	{
		boxConvolution gf = new boxConvolution(logger);  // bigger radius => more blur
		gf.setGaussianFilter(7f);
		if( gf.getIsOk() ) {
		 try {
            gf.convolveAndTranspose(inPixels, outPixels, width, height , false , 0);
            add_substract( inPixels , outPixels , width , height , false);
		 }
		 catch(Exception e) {
			do_error("doSharpen " + e.getMessage());
		 }
         //System.out.println("Done");
		}
		else {
			do_error("doSharpen did not initialize ok");
		}

	}
	
	//-----------------------------------------------------------------------
	private void doGlow(int[] inPixels, int[] outPixels, int width, int height)
	//-----------------------------------------------------------------------
	{
		boxConvolution gf = new boxConvolution(logger);  // bigger radius => more blur
		gf.setGaussianFilter(7f);
		if( gf.getIsOk() ) {
		 try {
            gf.convolveAndTranspose(inPixels, outPixels, width, height , false , 0);
            add_substract( inPixels , outPixels , width , height , true);
		 }
		 catch(Exception e) {
			do_error("doGlow " + e.getMessage());
		 }
         //System.out.println("Done");
		}
		else {
			do_error("doGlow did not initialize ok");
		}

	}
	
	//-----------------------------------------------------------------------
	private void add_substract(int[]src , int[]tgt , int width , int height , boolean doAdd)
	//-----------------------------------------------------------------------
	{
		int[] temp = new int[src.length];
		for(int i=0;i<src.length;i++) temp[i] = tgt[i];
		

		// to compensate on the alpha channel
		float amount = 0.5f;
		int threshold = 1;
		float a = 4*amount;

		if( doAdd )
		{
			int index = 0;
			for ( int y = 0; y < height; y++ ) {
				for ( int x = 0; x < width; x++ ) {
					int rgb1 = src[index];
					int r1 = (rgb1 >> 16) & 0xff;
					int g1 = (rgb1 >> 8) & 0xff;
					int b1 = rgb1 & 0xff;

					int rgb2 = tgt[index];
					int r2 = (rgb2 >> 16) & 0xff;
					int g2 = (rgb2 >> 8) & 0xff;
					int b2 = rgb2 & 0xff;

					if ( Math.abs( r1 +  r2 ) >= threshold )
						r1 = PixelUtils.clamp( (int)((a+1) * (r1-r2) + r2) );
					if ( Math.abs( g1 +  g2 ) >= threshold )
						g1 = PixelUtils.clamp( (int)((a+1) * (g1-g2) + g2) );
					if ( Math.abs( b1 +  b2 ) >= threshold )
						b1 = PixelUtils.clamp( (int)((a+1) * (b1-b2) + b2) );

					tgt[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
					index++;
				}
			}
				
		}
		else {
		int index = 0;
		for ( int y = 0; y < height; y++ ) {
			for ( int x = 0; x < width; x++ ) {
				int rgb1 = src[index];
				int r1 = (rgb1 >> 16) & 0xff;
				int g1 = (rgb1 >> 8) & 0xff;
				int b1 = rgb1 & 0xff;

				int rgb2 = tgt[index];
				int r2 = (rgb2 >> 16) & 0xff;
				int g2 = (rgb2 >> 8) & 0xff;
				int b2 = rgb2 & 0xff;

				if ( Math.abs( r1 -  r2 ) >= threshold )
					r1 = PixelUtils.clamp( (int)((a+1) * (r1-r2) + r2) );
				if ( Math.abs( g1 -  g2 ) >= threshold )
					g1 = PixelUtils.clamp( (int)((a+1) * (g1-g2) + g2) );
				if ( Math.abs( b1 -  b2 ) >= threshold )
					b1 = PixelUtils.clamp( (int)((a+1) * (b1-b2) + b2) );

				tgt[index] = (rgb1 & 0xff000000) | (r1 << 16) | (g1 << 8) | b1;
				index++;
			}
		}
		}

	}
	
	}
