package cbrTekStraktorModel;

import logger.logLiason;
import generalImagePurpose.gpFetchByteImageData;
import generalImagePurpose.gpImageMetadataGrabber;
import imageProcessing.cmcTimingInfo;
import dao.cmcTimingDAO;

public class cmcProcTimingInfo {

	private boolean DEBUG = false;
	
	cmcProcSettings xMSet=null;
	logLiason logger = null;
	private cmcTimingDAO dao = null;
	private runTimeMonitor moni = null;
	
	
	class normalizedTiming
	{
		float width=-1f;
		float whratio=-1f;
		float filesize=-1f;
		float filetype=-1f;
		float density=-1f;
		float colorscheme=-1f;
		float monomethod=-1f;
		float numconcomp=-1f;
		float numpara=-1f;
        //		
		float totalImageTime=-1f;
		float preprocessTime=-1f;
		float binarizeTime=-1f;
		//
		long oriwidth=-1L;
		long oriheight=-1L;
		long orisize=-1L;
		long oriTotImageTime=-1L;
		long oriRemainder=-1L;
		//
		double distance=-1;
		double relDistance=-1;
	}
	private normalizedTiming[] arTim=null;

	minmaxinterval wi_mx = null;
	minmaxinterval ra_mx = null;
	minmaxinterval si_mx = null;
	minmaxinterval co_mx = null;
	minmaxinterval pa_mx = null;
	minmaxinterval de_mx = null;
	minmaxinterval im_mx = null;
	minmaxinterval pr_mx = null;
	minmaxinterval bi_mx = null;
	
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	class minmaxinterval {
		float min;
		float max;
		float interval;
		minmaxinterval(float[] ar)
		{
			min=-1f;
			max=min;
			for(int i=0;i<ar.length;i++)
			{
				if( i == 0 ) {	min = ar[i]; max = ar[i];	continue; }
				if( min > ar[i] ) min = ar[i];
				if( max < ar[i] ) max = ar[i];
			}
			interval = max - min;
		}
		float getMax() { return max; }
		float getMin() { return min; }
		float getInterval() { return interval; }
	}
	
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
	public cmcProcTimingInfo(cmcProcSettings is, logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
		initialize();
	}
	
	//-----------------------------------------------------------------------
	private float getNormalizedFileType(String suffix)
	//-----------------------------------------------------------------------
	{
		// assume JPEG is most popular - put in the middle of distri
		if( suffix.compareToIgnoreCase("GIF")==0) return 1/4f;
		if( suffix.compareToIgnoreCase("JPG")==0) return (float)2/4;
		if( suffix.compareToIgnoreCase("JPEG")==0) return (float)2/4;
		if( suffix.compareToIgnoreCase("PNG")==0) return (float)3/4;
		do_error("Unsupported file type [" + suffix + "]");
		return -100f;
	}
	//-----------------------------------------------------------------------
	private float getNormalizedColourSchema(String col)
	//-----------------------------------------------------------------------
	{
		// distribution :  mono - color - gray 
		if( col.indexOf("MONO")>=0) return (float)1/4f;
		if( col.indexOf("GRAY")>=0) return (float)2/4;
		if( col.indexOf("COL")>=0) return (float)3/4;
		do_error("Unsupported color schema [" + col + "]");
		return -100f;
	}
	//-----------------------------------------------------------------------
	private float getNormalizedMonoMethod(String mono)
	//-----------------------------------------------------------------------
	{
		// distribution :  iterative - otsu - bleached - niblak - sauvola 
		if( mono.indexOf("ITER")>=0) return (float)1/7;
		if( mono.indexOf("OTSU")>=0) return (float)2/7;
		if( mono.indexOf("BLEA")>=0) return (float)3/7;
		if( mono.indexOf("NIBL")>=0) return (float)4/7;
		if( mono.indexOf("SAUV")>=0) return (float)5/7;
		do_error("Unsupported monochromization method [" + mono + "]");
		return (float)6/7f;
	}
	
	//-----------------------------------------------------------------------
	private boolean isValidLogging(cmcTimingInfo x)
	//-----------------------------------------------------------------------
	{
		if( x.getUID() == null ) return false;
		if( x.getHeigth() <= 0 ) return false;
		if( x.getWidth() <= 0) return false;
		if( x.getBWDensity() <= 0 ) return false;
		if( x.getFileSize() <= 0 ) return false;
		if( x.getLoadTimeImage() <= 0 ) return false;
		if (x.getLoadTimeBinarize() <= 0 ) return false;
		//
        if( x.getColourScheme() == null ) return false;
		if (x.getImageType() == null) return false;
		if (x.getBinarizeMethod() == null) return false;
		//
		if( x.getColourScheme().trim().length() <= 0 ) return false;
		if( x.getImageType().trim().length() <= 0 ) return false;
		if( x.getBinarizeMethod().trim().length() <= 0 ) return false;
		//
		return true;
	}
	
	//-----------------------------------------------------------------------
	private void initialize()
	//-----------------------------------------------------------------------
	{
		dao = new cmcTimingDAO(xMSet,logger);
		cmcTimingInfo[] ar=null;
		ar = dao.grabAllTimingInfo();
		if( ar == null ) return;
	    //	
		int aantal=0;
		for(int i=0;i<ar.length;i++)  {
			if( isValidLogging(ar[i]) == false ) continue;
			aantal++;
		}
		// laden
		arTim = new normalizedTiming[aantal];
		for(int i=0;i<aantal;i++) arTim[i] = new normalizedTiming();
		int idx=-1;
		for(int i=0;i<ar.length;i++)  {
			if( isValidLogging(ar[i]) == false ) continue;
			idx++;
			//
		  	arTim[idx].width    = ar[i].getWidth();
			arTim[idx].oriwidth = (long)arTim[idx].width;
			arTim[idx].oriheight = ar[i].getHeigth();
			arTim[idx].whratio  = (float)ar[i].getWidth() / (float)ar[i].getHeigth();
			arTim[idx].filesize = ar[i].getFileSize();
			arTim[idx].orisize  = (long)arTim[idx].filesize;
			// JPEG is middle entry
			arTim[idx].filetype = getNormalizedFileType( ar[i].getImageType());
			// colour is middle
			arTim[idx].colorscheme = getNormalizedColourSchema(ar[i].getColourScheme());
			arTim[idx].monomethod = getNormalizedMonoMethod(ar[i].getBinarizeMethod());
			arTim[idx].numconcomp = ar[i].getNbrOfConnectedComponents();
			arTim[idx].numpara = ar[i].getNbrOfParagraphs();
			arTim[idx].density = (float)ar[i].getBWDensity();
			//
			arTim[idx].totalImageTime = (float)(ar[i].getLoadTimeImage() + ar[i].getLoadTimePage());
			arTim[idx].preprocessTime = (float)ar[i].getLoadTimePreprocess();
			arTim[idx].binarizeTime   = (float)ar[i].getLoadTimeBinarize();
			//
			arTim[idx].oriTotImageTime = ar[i].getLoadTimeImage() + ar[i].getLoadTimePage();
			arTim[idx].oriRemainder = ar[i].getLoadTimeEndToEnd() - arTim[idx].oriTotImageTime;
		}
		// flatten
		long tenths = 100000000L;
		for(int i=0;i<arTim.length;i++) 
		{
			arTim[idx].totalImageTime = (arTim[idx].totalImageTime / tenths) * tenths;
			arTim[idx].preprocessTime = (arTim[idx].preprocessTime / tenths) * tenths;
			arTim[idx].binarizeTime = (arTim[idx].binarizeTime / tenths) * tenths;
		}
		//
		float[] ar_wif = new float[aantal];
		float[] ar_raf = new float[aantal];
		float[] ar_sif = new float[aantal];
		float[] ar_cof = new float[aantal];
		float[] ar_paf = new float[aantal];
		float[] ar_def = new float[aantal];
		float[] ar_imf = new float[aantal];
		float[] ar_prf = new float[aantal];
		float[] ar_bif = new float[aantal];
	    //
		for(int i=0;i<aantal;i++)
		{
			ar_wif[i] = arTim[i].width;
			ar_raf[i] = arTim[i].whratio;
			ar_sif[i] = arTim[i].filesize;
			ar_cof[i] = arTim[i].numconcomp;
			ar_paf[i] = arTim[i].numpara;
			ar_def[i] = arTim[i].density;
			ar_imf[i] = arTim[i].totalImageTime;
			ar_prf[i] = arTim[i].preprocessTime;
			ar_bif[i] = arTim[i].binarizeTime;
		}
		wi_mx = new minmaxinterval(ar_wif);
		ra_mx = new minmaxinterval(ar_raf);
		si_mx = new minmaxinterval(ar_sif);
		co_mx = new minmaxinterval(ar_cof);
		pa_mx = new minmaxinterval(ar_paf);
		de_mx = new minmaxinterval(ar_def);
		im_mx = new minmaxinterval(ar_imf);
		pr_mx = new minmaxinterval(ar_prf);
		bi_mx = new minmaxinterval(ar_bif);
		
		//
		{
		 String ss = " distribution - MinValues equals MaxValue - Probably not enough samples";
		 if( wi_mx.getInterval() <= 0f ) { arTim=null; do_error("Width"+ss); return; }
		 if( ra_mx.getInterval() <= 0f ) { arTim=null; do_error("Ratio"+ss); return; }
		 if( si_mx.getInterval() <= 0f ) { arTim=null; do_error("Size"+ss); return; }
		 if( co_mx.getInterval() <= 0f ) { arTim=null; do_error("Connected Component"+ss); return; }
		 if( pa_mx.getInterval() <= 0f ) { arTim=null; do_error("Paragraph"+ss); return;  }
		 if( de_mx.getInterval() <= 0f ) { arTim=null; do_error("Density"+ss);  return; }
		 if( im_mx.getInterval() <= 0f ) { arTim=null; do_error("ImageTime"+ss);  return; }
		 if( pr_mx.getInterval() <= 0f ) { arTim=null; do_error("PreprocessTime"+ss);  return; }
		 if( bi_mx.getInterval() <= 0f ) { arTim=null; do_error("BinarizeTime"+ss);  return; }
		}
	    // Normalize
		// Caution : filetype - monomethod and colour schem zijn al genormaliseerd
		for(int i=0;i<aantal;i++)
		{
			arTim[i].width          = ( arTim[i].width          - wi_mx.getMin() ) / wi_mx.getInterval();
			arTim[i].whratio        = ( arTim[i].whratio        - ra_mx.getMin() ) / ra_mx.getInterval();
			arTim[i].filesize       = ( arTim[i].filesize       - si_mx.getMin() ) / si_mx.getInterval();
			arTim[i].numconcomp     = ( arTim[i].numconcomp     - co_mx.getMin() ) / co_mx.getInterval();
			arTim[i].numpara        = ( arTim[i].numpara        - pa_mx.getMin() ) / pa_mx.getInterval();
			arTim[i].density        = ( arTim[i].density        - de_mx.getMin() ) / de_mx.getInterval();
			arTim[i].totalImageTime = ( arTim[i].totalImageTime - im_mx.getMin() ) / im_mx.getInterval();
			arTim[i].preprocessTime = ( arTim[i].preprocessTime - pr_mx.getMin() ) / pr_mx.getInterval();
			arTim[i].binarizeTime   = ( arTim[i].binarizeTime   - bi_mx.getMin() ) / bi_mx.getInterval();
		}
		//
		ar=null;
		//
		show();
	}

	//-----------------------------------------------------------------------
	private void calc_rel_distance(double fi)
	//-----------------------------------------------------------------------
	{
	  double min = 0;
	  double max = 0;
	  for(int i=0;i<arTim.length;i++)
	  {
	    if( i == 0 ) {
	    	min = max = arTim[i].distance;
	    	continue;
	    }
	    if( min > arTim[i].distance ) min = arTim[i].distance;
	    if( max < arTim[i].distance ) max = arTim[i].distance;
	  }
	  double interval = max - min;
	  if( interval >= 0 ) {
	    for(int i=0;i<arTim.length;i++)
	    {
		  arTim[i].relDistance = (( arTim[i].distance - min ) / interval) * 100;
	    }
	  }
	}

	//-----------------------------------------------------------------------
	private int minimalDistanceIdx()
	//-----------------------------------------------------------------------
	{
		   double min = 1000000;
		   int idx=-1;
		   for(int i=0;i<arTim.length;i++)
		   {
		    if( min >= arTim[i].distance ) {
			   min = arTim[i].distance;
			   idx=i;
		    }
		   }
		   return idx;
	}
	
	//-----------------------------------------------------------------------
	public long estimateImageLoadTimeNanoSeconds(String longFileName)
	//-----------------------------------------------------------------------
	{
	   moni=null;
	   if( arTim == null ) return -1L;
	   if( arTim.length <= 0) return -2L;
       if( longFileName == null ) return -3L;			
	   if( xMSet.xU.IsBestand( longFileName ) == false ) {
		   System.err.println("Cannot locate [" + longFileName + "]");
		   return -1L;
	   }
	   long ifileSize = xMSet.xU.getFileSize(longFileName);
	   String suffix = xMSet.xU.GetSuffix(longFileName).trim().toUpperCase();
	   //
	   /*
	   gpFetchByteImageData imgInfo = new gpFetchByteImageData();
	   imgInfo.cmcProcGetImageInfo(longFileName);
	   do_log(1,"Image Info -> " + imgInfo.getINFO());
	   int iwidth = imgInfo.getWidth();
	   int iheigth = imgInfo.getHeight();
	   */
	   gpImageMetadataGrabber meta = new gpImageMetadataGrabber(xMSet,logger);
	   meta.grabImageMetadata(longFileName);
	   int iwidth = meta.getWidth();
	   int iheigth = meta.getHeigth();
	   if( iheigth <= 0f ) return -39L;
       //	  
	   if( wi_mx == null ) return -40L;
	   if( ra_mx == null ) return -41L;
	   if( si_mx == null ) return -42L;
	   if( co_mx == null ) return -43L;
	   if( pa_mx == null ) return -44L;
	   if( de_mx == null ) return -45L;
	    
	   // Normalize
	   normalizedTiming x = new normalizedTiming();
	   x.width    = ((float)iwidth - wi_mx.getMin() ) /wi_mx.getInterval(); 
	   x.whratio  = (((float)iwidth / (float)iheigth) - ra_mx.getMin() ) / ra_mx.getInterval(); 
	   x.filesize = ((float)ifileSize - si_mx.getMin() ) /si_mx.getInterval(); 
	   x.filetype = getNormalizedFileType(suffix);
	   
	   // Euclidian distance :  width - ratio - size - filesuffix 
	   for(int i=0;i<arTim.length;i++)
	   {
		 float p1 = x.filesize - arTim[i].filesize;
		 float p2 = x.width - arTim[i].width;
		 float p3 = x.whratio- arTim[i].whratio;
		 float p4 = x.filetype - arTim[i].filetype;
		 arTim[i].distance = Math.sqrt( (double)((p1*p1) + (p2*p2) + (p3*p3) + (p4*p4)));
	   }
	   int idx = minimalDistanceIdx();
	   if( idx < 0 ) return -8L;
	   
	   // make a monitor entry
	   moni = new runTimeMonitor(x.width,x.whratio,x.filesize,x.filetype,arTim[idx].oriTotImageTime);
	   // report
	   lineReport("IMA",idx,x);
       //      
	   return arTim[idx].oriTotImageTime;
	}

	
	// include the color and imageloadtimeinfo
	//-----------------------------------------------------------------------
	public long estimatePreprocessTime(long actualImageLoadTime , String color)
	//-----------------------------------------------------------------
	{
		//System.err.println("-->" + actualImageLoadTime + " " + color );
		if( moni == null ) return -1L;
		moni.actualImageTime = actualImageLoadTime;
		if( arTim == null ) return -1L;
		if( arTim.length <= 0) return -2L;
		if( im_mx == null ) return -40L;
		if( im_mx.getInterval() <=0f ) return -50L;
	
		// Normalize
		normalizedTiming x = new normalizedTiming();
		x.width         = moni.normaWidth; 
		x.whratio       = moni.normaRatio; 
		x.filesize      = moni.normaSize; 
		x.filetype      = moni.normaSuffix;
        x.totalImageTime= ((float)(actualImageLoadTime) - im_mx.getMin()) / im_mx.getInterval();
		x.colorscheme   = getNormalizedColourSchema(color);

		// Euclidian distance :  width - ratio - size - filesuffix 
		for(int i=0;i<arTim.length;i++)
	    {
			 float p1 = moni.normaSize - arTim[i].filesize;
			 float p2 = moni.normaWidth - arTim[i].width;
			 float p3 = moni.normaRatio - arTim[i].whratio;
			 float p4 = moni.normaSuffix - arTim[i].filetype;
			 float p5 = x.totalImageTime - arTim[i].totalImageTime;
			 float p6 = x.colorscheme - arTim[i].colorscheme;
			 arTim[i].distance = Math.sqrt( (double)((p1*p1) + (p2*p2) + (p3*p3) + (p4*p4) + (p5*p5) + (p6*p6)));
		}
		int idx = minimalDistanceIdx();
		if( idx < 0 ) return -8L;
		
		moni.estimatedTimeBeforePreprocess = arTim[idx].oriTotImageTime + arTim[idx].oriRemainder;
	    moni.normaColor = x.colorscheme;
	    moni.normaImageTime = x.totalImageTime;
        //report
        lineReport("PRE",idx,x);
        //
		return moni.estimatedTimeBeforePreprocess;
	}
	
	//-----------------------------------------------------------------------
	public long estimateBinarizeTime(long actualPreprocessTime)
	//-----------------------------------------------------------------
	{
		if( moni == null ) return -1L;
		moni.actualPreprocessTime = actualPreprocessTime;
		if( arTim == null ) return -1L;
		if( arTim.length <= 0) return -2L;
		if( pr_mx == null ) return -40L;
		if( pr_mx.getInterval() <=0f ) return -50L;
		// Normalize
		normalizedTiming x = new normalizedTiming();
		x.width          = moni.normaWidth; 
		x.whratio        = moni.normaRatio; 
		x.filesize       = moni.normaSize; 
		x.filetype       = moni.normaSuffix;
        x.totalImageTime = moni.normaImageTime;
		x.colorscheme    = moni.normaColor;
        x.preprocessTime = ((float)(actualPreprocessTime) - pr_mx.getMin()) / pr_mx.getInterval();
	
		// Euclidian distance :  width - ratio - size - filesuffix 
		for(int i=0;i<arTim.length;i++)
	    {
			 float p1 = moni.normaSize - arTim[i].filesize;
			 float p2 = moni.normaWidth - arTim[i].width;
			 float p3 = moni.normaRatio - arTim[i].whratio;
			 float p4 = moni.normaSuffix - arTim[i].filetype;
			 float p5 = moni.normaImageTime- arTim[i].totalImageTime;
			 float p6 = moni.normaColor - arTim[i].colorscheme;
			 float p7 = x.preprocessTime - arTim[i].preprocessTime;
			 arTim[i].distance = Math.sqrt( (double)((p1*p1) + (p2*p2) + (p3*p3) + (p4*p4) + (p5*p5) + (p6*p6) + (p7*p7)));
		}
		int idx = minimalDistanceIdx();
		if( idx < 0 ) return -8L;
	    //
		moni.estimatedTimeBeforeBinarize = arTim[idx].oriTotImageTime + arTim[idx].oriRemainder;
		moni.normaPreprocessTime = x.preprocessTime;
        //report
        lineReport("BIN",idx,x);
	    return moni.estimatedTimeBeforeBinarize;
	}
	
	//-----------------------------------------------------------------------
	public long estimateCoCoTime(long actualBinarizeTime, double actualDensity)
	//-----------------------------------------------------------------
	{
		if( moni == null ) return -1L;
		moni.actualBinarizeTime = actualBinarizeTime;
		moni.actualDensity = (float)actualDensity;
		if( arTim == null ) return -1L;
		if( arTim.length <= 0) return -2L;
		if( bi_mx == null ) return -40L;
		if( bi_mx.getInterval() <=0f ) return -50L;
		if( de_mx == null ) return -60L;
		if( de_mx.getInterval() <=0f ) return -70L;
		// Normalize
		normalizedTiming x = new normalizedTiming();
		x.width          = moni.normaWidth; 
		x.whratio        = moni.normaRatio; 
		x.filesize       = moni.normaSize; 
		x.filetype       = moni.normaSuffix;
        x.totalImageTime = moni.normaImageTime;
		x.colorscheme    = moni.normaColor;
		x.preprocessTime = moni.normaPreprocessTime;
		x.binarizeTime   =((float)(actualBinarizeTime) - bi_mx.getMin()) / bi_mx.getInterval();
		x.density        =((float)actualDensity - de_mx.getInterval()) / de_mx.getInterval();
		
		for(int i=0;i<arTim.length;i++)
	    {
			 float p1 = moni.normaSize - arTim[i].filesize;
			 float p2 = moni.normaWidth - arTim[i].width;
			 float p3 = moni.normaRatio - arTim[i].whratio;
			 float p4 = moni.normaSuffix - arTim[i].filetype;
			 float p5 = moni.normaImageTime- arTim[i].totalImageTime;
			 float p6 = moni.normaColor - arTim[i].colorscheme;
			 float p7 = moni.normaPreprocessTime - arTim[i].preprocessTime;
			 float p8 = x.binarizeTime - arTim[i].binarizeTime;
			 //float p9 = x.density - arTim[i].density;   -- does not seem to work
			 float p9=0;
			 arTim[i].distance = Math.sqrt( (double)((p1*p1) + (p2*p2) + (p3*p3) + (p4*p4) + (p5*p5) + (p6*p6) + (p7*p7) + (p8*p8) + (p9*p9)));
		}
		int idx = minimalDistanceIdx();
		if( idx < 0 ) return -8L;
	    //
		moni.estimatedTimeBeforeCoCo = arTim[idx].oriTotImageTime + arTim[idx].oriRemainder;
		moni.normaBinarizeTime = x.binarizeTime;
		moni.normaDensity = x.density;
        //report
        lineReport("CCO",idx,x);
		return moni.estimatedTimeBeforeCoCo;
	}

	// include the color and imageloadtimeinfo
	//-----------------------------------------------------------------------
	public void setEndToEnd(long actualEndToEnd)
	//-----------------------------------------------------------------
	{
		if( moni == null ) { // omdat de inputstat file ontbreekt - geen moni
			System.err.println("Accurary stats will not be saved - next time to you run the applciation accurracy will be saved");
			return;
		}
		moni.actualEndToEndTime = actualEndToEnd;
		dao.dumpRunTimeEstimationAccurarcy(moni);
	}

	//-----------------------------------------------------------------------
	private String ff(float f)
	//-----------------------------------------------------------------------
	{
		return String.format("%.5f", f);
	}
	
	//-----------------------------------------------------------------------
	private String shoTime(normalizedTiming x)
	//-----------------------------------------------------------------------
	{
		String ss=""; 
		ss += "  [wi=" + ff(x.width);
		ss += "] [ra=" + ff(x.whratio);
		ss += "] [fi=" + ff(x.filesize);
		ss += "] [ti=" + ff(x.filetype);
		ss += "] [co=" + ff(x.colorscheme);
		ss += "] [im=" + ff(x.totalImageTime);
		ss += "] [pr=" + ff(x.preprocessTime);
		ss += "] [bi=" + ff(x.binarizeTime);
		ss += "] [de=" + ff(x.density);
		ss += "] [mo=" + ff(x.monomethod);
		ss += "] - [Dist=" + ff((float)x.distance);
		ss += "] [%Dist=" + String.format("%4d",(int)x.relDistance) + "%]";
		return ss;
	}
	
	//-----------------------------------------------------------------------
	private void show()
	//-----------------------------------------------------------------------
	{
		for(int i=0;i<arTim.length;i++)
		{
	      do_log(5,String.format("%4d", i) + " " + shoTime(arTim[i]) );	
		}
	}

	//-----------------------------------------------------------------------
	private void lineReport(String tag , int idx , normalizedTiming x)
	//-----------------------------------------------------------------------
	{
		  if( !DEBUG ) return;
		  do_log(1," ---[" + tag.toUpperCase() + "]---");
		  do_log(1, "     " + shoTime(x) );
		  do_log(1, String.format("%4d", idx) +" " + shoTime(arTim[idx]) );
		  do_log(1,"---"); 	
		  calc_rel_distance( arTim[idx].distance );
		  show(); 
	}

	//------------------------------------------------------------
	private String secoDisplay(long ms)
	//------------------------------------------------------------
	{
		   long sec = ms / 1000L;
		   long rest = (ms % 1000L) / 100;  // 10ste
		   return "" + sec + ":" + rest;
	}
	
	//------------------------------------------------------------
	public String getRunTiming()
	//------------------------------------------------------------
	{
		if( moni == null )  return "?";
		long preamble=0L;
		long remainder = 0L;
		if( moni.estimatedTimeBeforePreprocess == -1L ) {
			preamble = moni.estimatedImageTime;
			remainder=0L;
		}
		else
		if( moni.estimatedTimeBeforeBinarize == -1L ) {
			preamble=0L;
			remainder=moni.estimatedTimeBeforePreprocess;
		}
		else
		if( moni.estimatedTimeBeforeCoCo == -1L ) {
			preamble=0L;
			remainder=moni.estimatedTimeBeforeBinarize;
		}
		else {
			preamble=0L;
			remainder =  moni.estimatedTimeBeforeCoCo;
		}
		long estimated = (preamble + remainder) / 1000000L; //nano -> milli
		long elapsed =  System.currentTimeMillis() - moni.runStartTime;
		long togo = estimated - elapsed;
		//
	    if( togo < 0 ) return secoDisplay(elapsed);
        float perc = ((float)elapsed / (float)estimated) * 100f;
	    int iperc = (int)perc;
	    return "" + iperc + "%   [" + secoDisplay(estimated) + "]";
	}
	

}
