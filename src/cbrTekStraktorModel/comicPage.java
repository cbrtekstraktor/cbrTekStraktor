package cbrTekStraktorModel;
import java.awt.Point;

import logger.logLiason;
import imageProcessing.cmcConnectedComponentHistogram;
import imageProcessing.cmcMonochromeImageDetector;
import imageProcessing.cmcProcColorHistogram;
import imageProcessing.cmcProcFindMargins;
import generalImagePurpose.gpFetchByteImageData;
import generalImagePurpose.gpImageMetadataGrabber;
import generalImagePurpose.gpIntArrayFileIO;
import dao.cmcStatDAO;


public class comicPage {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private cmcStopWatch watch = null;
	public cmcProcColorHistogram oriHstgrm=null;
	public cmcMonochromeImageDetector monoHstgrm=null;
	public cmcConnectedComponentHistogram cclHstgrm=null;
	
	String longFName="";
	public comicImage cmcImg;
	int UncroppedWidth;
	int UncroppedHeigth;
	int PayLoadX;
	int PayLoadY;
	int PayLoadWidth;
	int PayLoadHeigth;
	boolean grainyBackground=false;
	boolean blackBorder=false;
	boolean isGrayscale=false;
	boolean isMonochrome=false;
	String UID="";
	
	Point PayLoadTopLeft;
	
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
	public void show()
	//-----------------------------------------------------------------------
	{
		String sLijn = "comicPage info\n" +
	     "  File                   : " + longFName + "\n" +
	     "  Uncropped Width/Height : " + UncroppedWidth + " " + UncroppedHeigth + "\n" +
	     "  PayLoad                : (" + PayLoadX + "," + PayLoadY + ")   (" + PayLoadWidth + "," + PayLoadHeigth + ")\n" +
		 "  PayLoadTopLeft         : (" + PayLoadTopLeft.x + "," + PayLoadTopLeft.y + ")\n" +
	     "  Widht/Height DPI       : (" + this.getPhysicalWidthDPI() + "," + this.getPhysicalHeigthDPI() + ")";
		
	   
		if( this.oriHstgrm == null ) sLijn = sLijn + "\n Histogram has NOT been created";
		do_log(1,sLijn);
	}
	
	//-----------------------------------------------------------------------
	public comicPage(cmcProcSettings is,logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
		cmcImg = new comicImage(logger);
		doe_init("");
	}

	//-----------------------------------------------------------------------
	private void doe_init(String FN)
	//-----------------------------------------------------------------------
	{
		longFName = FN;
		xMSet.setOrigImageLongFileName(longFName);
		oriHstgrm=null;
		cclHstgrm=null;
		UncroppedWidth = UncroppedHeigth = PayLoadX = PayLoadY = PayLoadWidth = PayLoadHeigth = -1;
		grainyBackground=false;
		blackBorder=false;
		isMonochrome=false;
		isGrayscale=false;
		PayLoadTopLeft = new Point(0,0);
		UID = xMSet.mkJavaUIDNew(longFName);
	}
	
	//-----------------------------------------------------------------------
	public void prepareLoadImage(String FN)
	//-----------------------------------------------------------------------
	{
		doe_init(FN);
		//temp_frame = frame;
	}
	
	//-----------------------------------------------------------------------
	public void maakHistogrammen()
	//-----------------------------------------------------------------------
	{
		oriHstgrm = new cmcProcColorHistogram();
		oriHstgrm.makeHistogram(cmcImg.pixels);
		isGrayscale = oriHstgrm.isGrayscale();
		// monochrome
		monoHstgrm = new cmcMonochromeImageDetector(oriHstgrm.getHistoGray() , isGrayscale , logger);
		isMonochrome = monoHstgrm.isMonochrome();
	}

	//-----------------------------------------------------------------------
	public long getLoadDurationInNano()
	//-----------------------------------------------------------------------
	{
		return watch.getDurationNanoSec();
	}
	
	//-----------------------------------------------------------------------
	public void loadComicPage()
	//-----------------------------------------------------------------------
	{
		//
		cmcImg.loadImg(longFName);
		// load metadata
		this.getMetadata(longFName);
		//
		// after image has been loaded
		watch = new cmcStopWatch( this.getClass().getName() );
		UncroppedWidth = getImageWidth();
		UncroppedHeigth = getImageHeigth();
		// Dump the pixel matrix van de originele image
		gpIntArrayFileIO fout = new gpIntArrayFileIO(logger);
		fout.writeIntArrayToFile( xMSet.getOriginalImagePixelDumpFileName() , cmcImg.pixels );
		//
		setPayLoadInfo(); // preliminair
		//
		// bij het eerste keer inlezen van de image het histogram bewaren
		if( oriHstgrm == null ) maakHistogrammen();
		//
		show();
		//
		watch.stopChrono();
	}
	
	 // Bit convoluted - due to the fact that the metadaroutine rely on xMSet
	 //-----------------------------------------------------------------------
	 private void getMetadata(String FileName)
	 //-----------------------------------------------------------------------
	 {
		   int xDPI=-1;
		   int yDPI=-1;
		   try {
		     gpImageMetadataGrabber meta = new gpImageMetadataGrabber(xMSet,logger);
			 meta.grabImageMetadata(FileName);
			 xDPI = meta.getPhysicalHeigthDPI();
		     yDPI = meta.getPhysicalWidthDPI();
		   }
		   catch( Exception e ) {
			   xDPI = -1;
			   xDPI = -1;
		   }
		   //do_log(9,"[" + FileName + "] DPI(Widgt,Height) (" + physicalWidthDPI + "," + physicalWidthDPI + ")");
	       if( (xDPI<=0) || (yDPI<=0) ) {
	    	   do_error("[" + FileName + "] Cannot determine widht/height DPI");
	       }
	       cmcImg.setPhysicalWidthDPI(xDPI);
	       cmcImg.setPhysicalHeigthDPI(yDPI);
	 }
	
	//-----------------------------------------------------------------------
	public void setGrainyBackground()
	//-----------------------------------------------------------------------
	{
		grainyBackground=true;
	}
	
	//-----------------------------------------------------------------------
	public void setBlackBorder()
	//-----------------------------------------------------------------------
	{
		blackBorder=true;
	}
	
	//-----------------------------------------------------------------------
	public void setPayLoadInfo()
	//-----------------------------------------------------------------------
	{
		cmcProcFindMargins pmarges = new cmcProcFindMargins( getImageWidth() , getImageHeigth() , cmcImg.pixels );
		PayLoadX = pmarges.getPayLoadPoint().x;
		PayLoadY = pmarges.getPayLoadPoint().y;
		PayLoadWidth = pmarges.getPayLoadDimension().width;
		PayLoadHeigth = pmarges.getPayLoadDimension().height;
	}

	//-----------------------------------------------------------------------
	public void updatePayLoadTopLeft()
	//-----------------------------------------------------------------------
	{
		PayLoadTopLeft.x += PayLoadX;
		PayLoadTopLeft.y += PayLoadY;
	}
	
	//-----------------------------------------------------------------------
	public void undoPayload()
	//-----------------------------------------------------------------------
	{
		cmcImg.loadImg(xMSet.getOrigImageLongFileName());
		//
		PayLoadX       = 0;
		PayLoadY       = 0;
		PayLoadTopLeft = new Point(0,0);
		PayLoadWidth   = UncroppedWidth;
		PayLoadHeigth  = UncroppedHeigth;
		//
		cmcImg.crop( 0 , 0 , UncroppedWidth , UncroppedHeigth );
	}
	
	//-----------------------------------------------------------------------
	public void dumpStats()
	//-----------------------------------------------------------------------
	{
		cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
		dao.writePageStats(this);
		dao.dump_histogram(oriHstgrm,true);
		dao.dump_histogram(cmcImg.hstgrm,false);
	}

	//-----------------------------------------------------------------------
	public String getShortImageInfo()
	//-----------------------------------------------------------------------
	{
		return " [" + xMSet.xU.GetFileName(xMSet.getOrigImageLongFileName()) + "]" + 
	           " [" + UncroppedWidth + "x" + UncroppedHeigth + "]" +
	           " [" + (xMSet.xU.prntStandardDateTime(xMSet.xU.getModificationTime(xMSet.getOrigImageLongFileName()))).toLowerCase() + "]";
	}
	
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	public int getImageWidth()
	{
		   return cmcImg.getWidth();
	}
	public int getImageHeigth()
	{
		   return cmcImg.getHeigth();
	}
	public double[] getHistoGray()
	{
		return cmcImg.hstgrm.getHistoGray();
	}
	public double[] getHistoRed()
	{
		return cmcImg.hstgrm.getHistoRed();
	}
	public double[] getHistoBlue()
	{
		return cmcImg.hstgrm.getHistoBlue();
	}
	public double[] getHistoGreen()
	{
		return cmcImg.hstgrm.getHistoGreen();
	}
	public int getMeanGray()
	{
		return cmcImg.hstgrm.getMeanGray();
	}
	public int getMeanRed()
	{
		return cmcImg.hstgrm.getMeanRed();
	}
	public int getMeanGreen()
	{
		return cmcImg.hstgrm.getMeanGreen();
	}
	public int getMeanBlue()
	{
		return cmcImg.hstgrm.getMeanBlue();
	}
	public int getMedianGray()
	{
		return cmcImg.hstgrm.getMedianGray();
	}
	public int getMedianRed()
	{
		return cmcImg.hstgrm.getMedianRed();
	}
	public int getMedianGreen()
	{
		return cmcImg.hstgrm.getMedianGreen();
	}
	public int getMedianBlue()
	{
		return cmcImg.hstgrm.getMedianBlue();
	}
	public double getStdDevGray()
	{
		return cmcImg.hstgrm.getStdDevGray();
	}
	public double getStdDevRed()
	{
		return cmcImg.hstgrm.getStdDevRed();
	}
	public double getStdDevGreen()
	{
		return cmcImg.hstgrm.getStdDevGreen();
	}
	public double getStdDevBlue()
	{
		return cmcImg.hstgrm.getStdDevBlue();
	}
	public int getNumberOfPeaks()
	{
		return monoHstgrm.getNumberOfPeaks();
	}
	public int getNumberOfValidPeaks()
	{
		return monoHstgrm.getNumberOfValidPeaks();
	}
	public double getPeakCoverage()
	{
		return monoHstgrm.getPeakCoverage();
	}
	public boolean getIsMonoChrome()
	{
		return this.isMonochrome;
	}
	public boolean getIsGrayScale()
	{
		return this.isGrayscale;
	}
	public boolean getHasBlackBorder()
	{
		return this.blackBorder;
	}
	public boolean getHasgrainyBackGround()
	{
		return this.grainyBackground;
	}
	public int getUncroppedWidth()
	{
		return UncroppedWidth;
	}
	public int getUncroppedHeigth()
	{
		return UncroppedHeigth;
	}
	public int getPayloadWidth()
	{
		return PayLoadWidth;
	}
	public int getPayloadHeigth()
	{
		return PayLoadHeigth;
	}
	
	public Point getPayLoadTopLeft()
	{
		return PayLoadTopLeft;		
	}
	public String getCMXUID()
	{
		return xMSet.getCleansedShortFileName(xMSet.getOrigImageLongFileName(),"xMSet.getCMXUID");
	}
	public String getUID()
	{
		return UID;
	}
	public String getFName()
	{
		return longFName;
	}
	public String getImageFileType()
	{
		return xMSet.xU.GetSuffix(longFName).trim().toUpperCase();
	}
	public long getFileSize()
	{
		return xMSet.xU.getFileSize(longFName);
	}
	public int getPhysicalWidthDPI()
	{
	   return cmcImg.getPhysicalWidthDPI();
	}
	public int getPhysicalHeigthDPI()
	{
	   return cmcImg.getPhysicalHeigthDPI();
	}
}
