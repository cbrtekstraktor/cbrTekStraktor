package cbrTekStraktorModel;

import java.awt.Point;

import generalImagePurpose.cmcBulkImageRoutines;
import generalImagePurpose.cmcImageRoutines;
import generalImagePurpose.gpIntArrayFileIO;
import generalpurpose.gpInterrupt;
import imageProcessing.cmcProcConnectedComponent;
import imageProcessing.cmcProcConnectedComponentPostProcessor;
import imageProcessing.cmcProcDetermineThreshold;
import imageProcessing.cmcTextBundleReport;
import imageProcessing.cmcTimingInfo;
import dao.cmcArchiveDAO;
import dao.cmcBookMetaDataDAO;
import dao.cmcStatDAO;
import dao.cmcTimingDAO;
import drawing.cmcGraphController;

import javax.swing.SwingWorker;

import ocr.cmcOCRController;
import tensorflow.cmcVRPublishResults;
import tensorflow.cmcVRRunTensorFlow;
import tensorflow.cmcVRMakeTrainingImages;
import textProcessing.cmcTextExport;
import textProcessing.cmcTextImport;
import logger.logLiason;


public class cmcProcController extends SwingWorker<Integer, Integer> {
	// return type is Integer , type voor de Process is string

	comicPage cPage=null;
	cmcProcSemaphore cSema=null;
	cmcProcSettings xMSet=null;
	cmcGraphController gCon=null;
	cmcBulkImageRoutines ibulk=null;
	cmcImageRoutines irout = null;
	cmcOCRController ocrcon = null;
	logLiason logger = null;
	gpInterrupt irq = null;
	
	//
	//  Zou uit statistiek moeten komen
	int MarginMinForCleanUp = 175;   // Indien de grayscale margin meer witter is dan dit, dan wit maken
	int MarginMeanForCleanUp = 200;  // indien het gemiddelde van de grayscale van de border minder dan dit is ; is er wellicht geen border
	// 
	private int optimalThreshold=0x80;
	private int optimalKMeansK=4;
	private cmcProcEnums.BinarizeClassificationMethod currentBinarizeTipe = cmcProcEnums.BinarizeClassificationMethod.OTSU;
	private cmcProcSemaphore.TaskType taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING;
	
	// timerstats - NANOSEC
	private cmcStopWatch owo = null;
    private cmcTimingInfo timo=null;

    
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
	private void resetTimerStats()
	//-----------------------------------------------------------------------
	{
		owo=null;
		owo = new cmcStopWatch("controller");
		timo=null;
		timo=new cmcTimingInfo();
	}
	
	
	//-----------------------------------------------------------------------
	public cmcProcController(comicPage cpin , cmcProcSemaphore is , cmcProcSettings st , cmcGraphController gi , logLiason ilog)
	//-----------------------------------------------------------------------
	{
	 cPage = cpin;	
	 cSema= is;
	 xMSet = st;
	 gCon=gi;
	 logger=ilog;
	 ibulk = new cmcBulkImageRoutines(xMSet,logger);
	 irout = new cmcImageRoutines(logger);
	 irq = new gpInterrupt(xMSet.getInterruptFileName());
	}
	
	@Override
	protected Integer doInBackground() throws Exception
    {
        do_log(1,"SwingWorker started");
       
		while (!this.isCancelled()) {
	    	switch ( taskTipe )
            {
            case DO_LOAD_IMAGE : {
            	resetTimerStats();
            	doe_load_image(); 
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case DO_FAST_LOAD_IMAGE : { 
            	resetTimerStats();
            	doe_load_image(); 
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case DO_DIALOG : {   // je hoeft niets te doen, gewoon het type op de semaphore zetten 
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case DO_PREPROCESS : { 
            	if( doe_preprocessor() == true ) { 
            	  dump_page_stats();
            	  cSema.setSemaphore(taskTipe); 
            	}
            	else {
            	  cSema.setSemaphore(cmcProcSemaphore.TaskType.SHOUT); // skips the rest
            	}
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING;
            	break; }
            case DO_GRAYSCALE : { 
            	// if FAST the grayscale is to be performed after the Binarize, so skip
            	if( currentBinarizeTipe != cmcProcEnums.BinarizeClassificationMethod.FAST_BLEACHED ) doe_grijs(); 
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;}
            case DO_BINARIZE : { 
            	if( doe_binarize() )  cSema.setSemaphore(taskTipe);
             	                 else cSema.setSemaphore(cmcProcSemaphore.TaskType.SHOUT); // skips the rest
               taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;}
            case DO_CONNECTEDCOMPONENT : { 
            	if( doe_processImage() ) cSema.setSemaphore(taskTipe);
                                    else cSema.setSemaphore(cmcProcSemaphore.TaskType.SHOUT); // skips the rest
             	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;}
            case PROCESS_GRAYSCALE : {
            	process_grayscale();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_SOBELGRAYSCALE : {
            	process_SobelOnGrayScale();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_SOBELCOLOR : {
            	process_SobelOnColor();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_GRADIENT_NARROW : {
            	process_gradient_narrow();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_GRADIENT_WIDE : {
            	process_gradient_wide();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_BLACKWHITE : {
            	process_blackwhite();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_NIBLAK : {
            	process_niblak(true);
            	//process_blue();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_SAUVOLA : {
            	process_niblak(false);
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_BLEACH : {
            	process_bleach();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_BLUEPRINT : {
            	process_niblak(false);
                process_blue();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_MAINFRAME : {
            	process_niblak(false);
                process_green();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_CONVOLUTION_SHARPEN : {
                process_convolution(generalImagePurpose.cmcProcConvolution.kernelType.SHARPEN);
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_CONVOLUTION_GLOW : {
                process_convolution(generalImagePurpose.cmcProcConvolution.kernelType.BLUR);
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_CONVOLUTION_EDGE : {
                process_convolution(generalImagePurpose.cmcProcConvolution.kernelType.EDGE);
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_CONVOLUTION_GAUSSIAN : {
                process_convolution(generalImagePurpose.cmcProcConvolution.kernelType.GAUSSIAN33);
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_EQUAL : {
            	process_histogramEqualization();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_INVERT : {
            	process_invert();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break;
            }
            case PROCESS_STATS : { 
            	process_stats();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case DO_EDITOR_IMAGE : {  // triggers after the drawing option dialog has been submitted
            	process_editor_image();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case DO_FLUSH_CHANGES : {  // triggers if there are changes to be written out
            	process_editor_changes();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
            	break; }
            case PREPARE_OCR : {
            	if( prepare_OCR()) cSema.setSemaphore(taskTipe); 
            	else cSema.setSemaphore(cmcProcSemaphore.TaskType.SHOUT); // skips the rest
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
               break; }
            case SHOW_OCR_FILE : {
            	resetTimerStats();
            	doe_load_image();    // first part displays the image
             	cSema.setSemaphore(taskTipe);
                taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
              	break; }
            case EDIT_OCR_FILE : {
            	resetTimerStats();
             	cSema.setSemaphore(taskTipe);
                taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
              	break; }
            case RUN_TESSERACT : {
            	if( run_tesseract() ) {
            		cSema.setSemaphore(taskTipe);
            	}
            	else  {
            		cSema.setSemaphore(cmcProcSemaphore.TaskType.SHOUT); // skips the rest
            	}
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
                break; }
            case EXTRACT_ALL_TEXT : {
            	extract_all_text();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
                break;
                }
            case IMPORT_ALL_TEXT : {
            	import_all_text();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
                break;
                }
            case TENSORFLOW_MAKE_TRAINING_SET : {
            	tensorflow_make_training_set();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
                break;
                }
            case TENSORFLOW_MAKE_SINGLE_SET : {
            	tensorflow_make_single_set();
            	cSema.setSemaphore(taskTipe); 
            	taskTipe = cmcProcSemaphore.TaskType.DO_NOTHING; 
                break;
                }
            case DO_NOTHING : break;
            default : { do_error("Unknown semaphore state [" + taskTipe  + "]"); break; }
            }
            //
            Thread.sleep(100);
        }        
	    
        return 0;
    }
	
	@Override
	protected void done()
	{
		 //cSema.setSemaphore(1);
		 if (!this.isCancelled()) {
	            // ?
	        }
		 cSema.setSemaphore(cmcProcSemaphore.TaskType.OVERTHECLIFF); 
		 do_error(" OOPS! The swingWorker stopped. You better close the application");
	}
	
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	 
	 
	//-----------------------------------------------------------------------
	 public boolean prepareForTask(cmcProcSemaphore.TaskType itt)
	//-----------------------------------------------------------------------
	 {
		 taskTipe=itt;
		 return true;
	 }
	 
	//-----------------------------------------------------------------------
	private void doe_load_image()
	//-----------------------------------------------------------------------
	{
		   cPage.loadComicPage();
		   if( timo != null ) {
		    timo.setImageTime(cPage.cmcImg.getLoadDurationInNano());
		    timo.setPageTime(cPage.getLoadDurationInNano());
		    do_log(5,"Load image [Effective leadtime= " + (timo.getLoadTimeImage() + timo.getLoadTimePage())/1000000L + "]");
		   } 
	}
	
	//-----------------------------------------------------------------------
	private void doe_grijs()
	//-----------------------------------------------------------------------
	{
		 cPage.cmcImg.workPixels=null;
		 cPage.cmcImg.workPixels=ibulk.doGrayscale(cPage.cmcImg.pixels);
	     cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private boolean doe_binarize()
	//-----------------------------------------------------------------------
	{
		cmcStopWatch ww = new cmcStopWatch("Binarize");
		boolean otsu=true;  // almost always OTSU
		boolean niblak=true;
		switch( currentBinarizeTipe )
		{
		case ITERATIVE : { // iterative approach
						try {
						 cmcProcDetermineThreshold dth = new cmcProcDetermineThreshold(cPage.getImageWidth(),cPage.getImageHeigth(),cPage.cmcImg.pixels,cPage.cmcImg.workPixels,xMSet,logger);
						 optimalThreshold = dth.getOptimalThresholdValue();
						 optimalKMeansK = dth.getOptimalKMeansKValue();
						 otsu=dth.didWeSettleForOtsu();
						}
						catch(Exception e) {
							do_error("EXCEPTION Oops - determine optimal threshold failure " + e.getMessage());
							optimalThreshold = irout.otsuTreshold( cPage.cmcImg.pixels );
						}
						break;	}
		case FAST_BLEACHED   : {  // experimental :  black Bleach en daarna drempel erop
							cPage.cmcImg.workPixels = null;
							cPage.cmcImg.workPixels = ibulk.bleachImageToBlack( cPage.cmcImg.pixels );
							cPage.cmcImg.moveWorkPixelsToImage();
							//
							optimalThreshold = irout.otsuTreshold( cPage.cmcImg.pixels );
							optimalKMeansK=5;
							break;
						 }
		case SLOW_SAUVOLA : niblak = false; // spill over naar niblak 
		case SLOW_NIBLAK  : {  // experimental :  niblak 
							  cPage.cmcImg.workPixels = null;
							  cPage.cmcImg.workPixels = ibulk.doNiblak( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() , niblak);
							  cPage.cmcImg.moveWorkPixelsToImage();
							  //
							  optimalThreshold = irout.otsuTreshold( cPage.cmcImg.pixels );
							  // ??
							  optimalThreshold = 128;
							  optimalKMeansK=5;
							  break;
		 }
		case OTSU      : ;  // followed by performing an OTSU threshold       
		default 	   : { 	optimalThreshold = irout.otsuTreshold( cPage.cmcImg.pixels );	break; }
		}
		//
		if( irq.gotInterrupt() ) return false;
		//
		xMSet.setOptimalThreshold(optimalThreshold);
		//
		cPage.cmcImg.workPixels=null;
		cPage.cmcImg.workPixels = ibulk.binarize( cPage.cmcImg.pixels , optimalThreshold);
		cPage.cmcImg.moveWorkPixelsToImage();
		// flush binarized image to file
		dumpCroppedBinarizedFile();
	    // also flush the uncropped version of the binarized File
        dumpUnCroppedBinarizedFile();
        cPage.cmcImg.moveWorkPixelsToImage();  // ensure that the image holds again the workpixels
	    //
		ww.stopChrono();
		timo.setBinarizeTime(ww.getDurationNanoSec());
		timo.setBWDensity(ibulk.getBWDensity());
		//
		do_log(5,"BINARIZE : [tipe=" + currentBinarizeTipe + "] [Threshold=" + optimalThreshold + "] [OTSU=" + otsu + "] [KMeansK=" + optimalKMeansK + "]");
		return true;
    }
		
    //-----------------------------------------------------------------------
	private void dumpCroppedBinarizedFile()
    //-----------------------------------------------------------------------
	{
		cPage.cmcImg.writeToFile(xMSet.getBinarizedOutputJPGName());
		do_log(5,"Binarized image written to [" + xMSet.getBinarizedOutputJPGName() + "]");
	}

    //-----------------------------------------------------------------------
	private void dumpUnCroppedBinarizedFile()
    //-----------------------------------------------------------------------
	{
		int uncroppedWidth = cPage.getUncroppedWidth();
		int uncroppedHeigth = cPage.getUncroppedHeigth();
		int croppedWidth = cPage.getImageWidth();
		int croppedHeigth = cPage.getImageHeigth();
		Point p = cPage.getPayLoadTopLeft();
		int payloadX = p.x;
		int payloadY = p.y;
		//
		int[] work = new int[uncroppedWidth * uncroppedHeigth];
		for(int i=0;i<work.length;i++) work[i] = cmcProcConstants.GROEN;
		//
		int[] pixels = cPage.cmcImg.pixels;
		if( pixels.length != (croppedHeigth * croppedWidth)) {
			do_error("System error - size of uncropped binarized image does not match");
		}
		if( uncroppedWidth < croppedWidth ) {
			do_error("System error - uncropped width is smaller then the cropped width");
		}
		if( uncroppedHeigth < croppedHeigth ) {
			do_error("System error - uncropped heigth is smaller then the cropped width");
		}
		// copy the Cropped pixel matrix into the larger uncropped work area
		for(int i=0;i<croppedHeigth;i++)
		{
		    int sourceIdx = (i * croppedWidth);
			for(int j=0;j<croppedWidth;j++)
			{
		      work[ (uncroppedWidth * (i + payloadY)) + j + payloadX] = pixels[sourceIdx+j];		
			}
		}
		//
		irout.writePixelsToFile(work, uncroppedWidth, uncroppedHeigth,xMSet.getBinarizedOutputImageNameUncropped(),cmcImageRoutines.ImageType.RGB);
		// debug
//do_error("DEBUG in postProcessor - dumping the Uncropped binarized");
//irout.writePixelsToFile(work, uncroppedWidth, uncroppedHeigth,"c:\\temp\\cbrtekstraktor\\junkuncrop.png",cmcImageRoutines.ImageType.RGB);

		//
		work=null;
		do_log(5,"Binarized UNCROPPED image written to [" + xMSet.getBinarizedOutputImageNameUncropped() + "]");
	}
	
    //-----------------------------------------------------------------------
	private boolean doe_processImage()
	//-----------------------------------------------------------------------
	{
		    cmcStopWatch wcc = new cmcStopWatch("ConnectedComponent");
		    if( irq.gotInterrupt() ) return false;
		    //
			cmcProcConnectedComponent cc = new cmcProcConnectedComponent(xMSet,logger);
			boolean ok = cc.doit( cPage.getImageWidth() , cPage.getImageHeigth() , cPage.cmcImg.pixels , cPage.cmcImg.workPixels , false , optimalKMeansK);
			wcc.stopChrono();
			timo.setConnectedComponentTime(wcc.getDurationNanoSec());
			timo.setNbrOfConnectedComponents(cc.getComponentCount());
			//
			if( irq.gotInterrupt() ) return false;
			cmcProcConnectedComponentPostProcessor cp = new cmcProcConnectedComponentPostProcessor(cc.getCCBoundaryArray(),cPage,xMSet,logger);
			timo.setParagraphTime(cp.getLoadTimeParagraphs()); 
			timo.setLetterTime(cp.getLoadTimeLetters());
			timo.setNbrOfParagraphs(cp.getNbrOfParagraphs());
			// maak HTMLreport
			if( irq.gotInterrupt() ) return false;
			cmcTextBundleReport tr = new cmcTextBundleReport( xMSet , cp.getTextBundle() , cPage , logger , xMSet.getCodePageString() );
			// create image
			cPage.cmcImg.moveWorkPixelsToImage();
			cPage.cmcImg.writeToFile( xMSet.getCCLOutputJPGName() );
			//
			// Dump the stat
			owo.stopChrono();
			timo.setEndToEndTime(owo.getDurationNanoSec());
			compileTimingInfo();
			// Close statfile
			cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
			dao.dumpTimeInfo(timo);
			dao.putStopGap();
			// Dump statistics
			cmcTimingDAO dpt = new cmcTimingDAO(xMSet,logger);
			dpt.writeTimingStatistics(timo);
			// Cleanup
			ZipAllFiles(true);
			// Finally remove the Stat file
			xMSet.xU.VerwijderBestand(xMSet.getXMLStatFileName());
			return true;
	}
	
	
	//-----------------------------------------------------------------------
	private boolean doe_preprocessor()
	//-----------------------------------------------------------------------
	{
		 //
		 cmcStopWatch ww = new cmcStopWatch("preprocess");
		 if( irq.gotInterrupt() ) return false;
         // when running bulk mode there is no metadatafile - see if there is an Estafette file
		 String FMetaData = xMSet.getMetaDataFileName();
		 if( xMSet.xU.IsBestand(FMetaData) == false ) {
		  String EstafetteFileName = xMSet.getEstafetteFileName();
		  if( xMSet.xU.IsBestand(EstafetteFileName) == false ) {
		    	do_error("Could not locate estafette file [" + EstafetteFileName + "]");
		    	return false;
		  }
		  // copy the estafette file over the metadatafile;
	      boolean ib = kloonEstafetteFile();
          if( ib == false ) return false;
		 }
		 //
		 if( irq.gotInterrupt() ) return false;
		 // read zMetadata
		 cmcBookMetaDataDAO cmeta = new cmcBookMetaDataDAO( xMSet , xMSet.getOrigImageLongFileName() , null , logger);
		 currentBinarizeTipe = cmeta.getBinarizeClassificationTypeRaw();
		 
		 // Crop or do not crop
		 if( cmeta.getCroppingType() == cmcProcEnums.CroppingType.DO_NOT_CROP_IMAGE ) {
			 // attempt to whiten the margins
		     CleanUpMargin();
			 cPage.undoPayload();   // set the crop coordinates and sizes to original values
			 cPage.show();
		 }
		 else {
		  // repeat until image size no longer changes
	      int start_breedte = cPage.getImageWidth();
		  for(int i=0;i<5;i++)
		  {
		   int ori_breedte = cPage.getImageWidth();
		   int ori_hoogte = cPage.getImageHeigth();
		   
		   // indien marge niet volledig wit probeer die wit te krijgen
	       CleanUpMargin();	
	       
	       // bepaal de marges
	       cPage.setPayLoadInfo();
	       cPage.updatePayLoadTopLeft();
	       cPage.show();
		  	   
	       // Crop de payload
	       cPage.cmcImg.crop( cPage.PayLoadX , cPage.PayLoadY , cPage.PayLoadWidth , cPage.PayLoadHeigth );
	       if( (ori_breedte == cPage.getImageWidth()) && (ori_hoogte == cPage.getImageHeigth())) break;
		  }
	      // sometimes too much has been cropped, so undo
		  double ratio = (double)cPage.getImageWidth() / (double)start_breedte;
		  if( ratio < (double)0.75 ) {
			 do_log(1,"Crop will be undone " + ratio );
			 cPage.undoPayload();
			 cPage.show();
		  }
		  if( irq.gotInterrupt() ) return false;
		 }
		 
		 // Dump cropped image to file
		 gpIntArrayFileIO iio = new gpIntArrayFileIO(logger);
		 iio.writeIntArrayToFile(xMSet.getCroppedImagePixelDumpFileName(),cPage.cmcImg.pixels);
		 // 
		 ww.stopChrono();
		 timo.setPreprocessTime(ww.getDurationNanoSec());
		 return true;
	}
	
	//-----------------------------------------------------------------------
	private void CleanUpMargin()
	//-----------------------------------------------------------------------
	{
		 // Look for a small spectrum histogram on the borders on the white side
		 int marginMean = ibulk.getMarginMeanColor( cPage.getImageWidth() , cPage.getImageHeigth() , cPage.cmcImg.pixels);
		 //
		 if( marginMean >= 255 ) return;  // volledig wit
		 // Black Borders
		 if( marginMean < 50 ) {
			 do_error("Looks like a comic with all black border.");
			 do_error("Currently not supported - just see what happens when processign like a white border");
			 //return;
		 }
		 // grainy background - yellowish paper - etc
		 int marginMin = ibulk.getMarginMinColor( cPage.getImageWidth() , cPage.getImageHeigth() , cPage.cmcImg.pixels);
		 if( marginMin >= MarginMinForCleanUp ) {
			 cPage.setGrainyBackground();
			 cPage.cmcImg.workPixels = null;
		     cPage.cmcImg.workPixels = ibulk.binarizeGrayBackGround(marginMin,cPage.cmcImg.pixels);
		     cPage.cmcImg.moveWorkPixelsToImage();
		 }
		 else {
			 do_log(1,"There is probably no border on this page");
		 }
	}

	//-----------------------------------------------------------------------
	private void invertImage()
	//-----------------------------------------------------------------------
	{
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.invertImage( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private void dump_page_stats()
	//-----------------------------------------------------------------------
	{
		cPage.dumpStats();
	}
	
	//-----------------------------------------------------------------------
	public void ZipAllFiles(boolean compress)
	//-----------------------------------------------------------------------
	{
	  cmcArchiveDAO archo = new cmcArchiveDAO(xMSet,logger);
	  archo.ZipAllFiles(compress);
	}
	
	//-----------------------------------------------------------------------
	private void process_grayscale()
	//-----------------------------------------------------------------------
	{
		doe_load_image();
		doe_grijs();
	}

	//-----------------------------------------------------------------------
	private void process_bleach()
	//-----------------------------------------------------------------------
	{
		doe_load_image();
		doe_softbleach();
	}

	//-----------------------------------------------------------------------
	private void process_blackwhite()
	//-----------------------------------------------------------------------
	{
		doe_load_image();
		doe_grijs();
		doe_binarize();
	}

	//-----------------------------------------------------------------------
	private void process_niblak(boolean ib)
	//-----------------------------------------------------------------------
	{
		doe_load_image();
		doe_niblak_sauvola(ib);
	}

	//-----------------------------------------------------------------------
	private void process_invert()
	//-----------------------------------------------------------------------
	{
		doe_load_image();
		invertImage();
	}
	
	//-----------------------------------------------------------------------
	private void process_stats()
	//-----------------------------------------------------------------------
	{
	    cmcProcReadCorpus corpus = new cmcProcReadCorpus(xMSet,logger);
	}
	
	//-----------------------------------------------------------------------
	private void doe_softbleach()
	//-----------------------------------------------------------------------
	{
		
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.bleachSoftImage( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
		
		
		// DEBUG voor FAST
		/*
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.bleachImageToBlack( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
		//
		//doe_grijs();
		//
		cPage.cmcImg.workPixels=null;
		cPage.cmcImg.workPixels = ibulk.binarize( cPage.cmcImg.pixels , (0xff & 220));
		cPage.cmcImg.moveWorkPixelsToImage();
		*/
		
	}
	
	//-----------------------------------------------------------------------
	private void doe_hardbleach()
	//-----------------------------------------------------------------------
	{
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.bleachHardImage( cPage.cmcImg.pixels );
			cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private void process_histogramEqualization()
	//-----------------------------------------------------------------------
	{
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.doHistogramEqualization( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
	}

	//-----------------------------------------------------------------------
	private void doe_niblak_sauvola(boolean niblak)
	//-----------------------------------------------------------------------
	{
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.doNiblak( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() , niblak);
			cPage.cmcImg.moveWorkPixelsToImage();
	}
		
	//-----------------------------------------------------------------------
	private void process_editor_image()
	//-----------------------------------------------------------------------
	{
		//gCon.clearStack();
		boolean ib = gCon.initialiseerPixels();
		if( ib == false ) {
			do_error("could not initalize pixels in gCon");
			return;
		}
		gCon.makeOverlay();
	}
	
	//-----------------------------------------------------------------------
	private void process_editor_changes()
	//-----------------------------------------------------------------------
	{
			gCon.flushChanges();
			gCon.purgeVariables();
	}
	
	//-----------------------------------------------------------------------
	private void process_blue()
	//-----------------------------------------------------------------------
	{
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.doBlue( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private void process_green()
	//-----------------------------------------------------------------------
	{
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.doMainframe( cPage.cmcImg.pixels );
		cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private void process_convolution(generalImagePurpose.cmcProcConvolution.kernelType kernelTipe)
	//-----------------------------------------------------------------------
	{
		cPage.cmcImg.workPixels = null;
		cPage.cmcImg.workPixels = ibulk.doConvolution( cPage.cmcImg.pixels , kernelTipe , cPage.cmcImg.getWidth() , cPage.cmcImg.getHeigth());
		cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	
	//-----------------------------------------------------------------------
	private void compileTimingInfo()
	//-----------------------------------------------------------------------
	{
		try {
		 timo.setUID( cPage.getUID() );
		 timo.setFileSize( cPage.getFileSize() );
		 timo.setWidth( cPage.getUncroppedWidth() );
		 timo.setHeigth( cPage.getUncroppedHeigth() );
		 timo.setImageType( cPage.getImageFileType() );
		 String tipe = "COLOR";
		 if( cPage.getIsMonoChrome() ) tipe = "MONOCHROME";
		 if( cPage.getIsGrayScale() ) tipe = "GRAYSCALE";
		 timo.setColourScheme( tipe );
		 timo.setBinarizeMethod(""+currentBinarizeTipe);
		}
		catch(Exception e ) {
			do_error("Error on compileTimingInfo");
		}
	}

	//-----------------------------------------------------------------------
	public long getActualImageLoadTime()
	//-----------------------------------------------------------------------
	{
	   if( timo == null )  return 0L;
	   return timo.getLoadTimeImage() + timo.getLoadTimePage();
	}
	//-----------------------------------------------------------------------
	public String getPageColourScheme()
	//-----------------------------------------------------------------------
	{
	  if ( cPage.getIsGrayScale() ) return "GRAYSCALE";
	  if ( cPage.getIsMonoChrome() ) return "MONOCHROME";
	  return "COLOUR";
	}
	//-----------------------------------------------------------------------
	public long getActualPreprocessTime()
	//-----------------------------------------------------------------------
	{
	   if( timo == null )  return 0L;
	   return timo.getLoadTimePreprocess();
	}
	//-----------------------------------------------------------------------
	public long getActualBinarizeTime()
	//-----------------------------------------------------------------------
	{
	   if( timo == null )  return 0L;
	   return timo.getLoadTimeBinarize();
	}
	//-----------------------------------------------------------------------
	public double getBWDensity()
	//-----------------------------------------------------------------------
	{
	   if( timo == null )  return 0;
	   return timo.getBWDensity();
	}
	//-----------------------------------------------------------------------
	public long getActualEndToEndTime()
	//-----------------------------------------------------------------------
	{
	   if( timo == null )  return 0L;
	   return timo.getLoadTimeEndToEnd();
	}

	//-----------------------------------------------------------------------
	private boolean prepare_OCR()
	//-----------------------------------------------------------------------
	{
		if( ocrcon != null ) ocrcon = null;
		ocrcon = new cmcOCRController(xMSet,logger);
		boolean ib = ocrcon.prepareOCRImage();
		if( ib == false ) {
			do_error("Cannot create the OCR Image");
			return false;
		}
		do_log(5,"The OCR Image has been created");
		return true;
	}
	//-----------------------------------------------------------------------
	private boolean run_tesseract()
	//-----------------------------------------------------------------------
	{
		boolean ib = ocrcon.run_tesseract( cmcOCRController.TESSERACTCOMMAND.DO_OCR );
		if( ib ) {
			ib = ocrcon.mergeOCRText();
		}
		ocrcon = null;
		return ib;
	}
	//-----------------------------------------------------------------------
	private void process_SobelOnGrayScale()
	//-----------------------------------------------------------------------
	{
		    do_log(9,"Sobel Gray");
		    // create grayscale
	 	    process_grayscale();
            //
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.sobelOnGrayScale( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() );
			cPage.cmcImg.moveWorkPixelsToImage();
	}
	//-----------------------------------------------------------------------
	private void process_SobelOnColor()
	//-----------------------------------------------------------------------
	{
		    do_log(9,"Sobel Color");
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.colorsobel( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() );
			cPage.cmcImg.moveWorkPixelsToImage();
	}
	//-----------------------------------------------------------------------
	private void process_gradient_wide()
	//-----------------------------------------------------------------------
	{
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.gradientwide( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() );
			cPage.cmcImg.moveWorkPixelsToImage();
	}
	//-----------------------------------------------------------------------
	private void process_gradient_narrow()
	//-----------------------------------------------------------------------
	{
			cPage.cmcImg.workPixels = null;
			cPage.cmcImg.workPixels = ibulk.gradientnarrow( cPage.cmcImg.pixels , cPage.cmcImg.getWidth() );
			cPage.cmcImg.moveWorkPixelsToImage();
	}
	
	//-----------------------------------------------------------------------
	private boolean kloonEstafetteFile()
	//-----------------------------------------------------------------------
	{
		String imageFileName =xMSet.getOrigImageLongFileName();
		if( xMSet.xU.IsBestand(imageFileName) == false ) {
			do_error("kloonEstafette - Cannot locate image file [" + imageFileName + "]");
			return false;
		}
		String estafetteFileName =xMSet.getEstafetteFileName();
		if( xMSet.xU.IsBestand(estafetteFileName) == false ) {
			do_error("kloonEstafette - Cannot locate image file [" + estafetteFileName + "]");
			return false;
		}
		String metadataFileName =xMSet.getMetaDataFileName();
		if( xMSet.xU.IsBestand(metadataFileName) == true ) {
			do_error("kloonEstafette - there already is a metadatafilename [" + metadataFileName + "]");
			return false;
		}
		// copy
		 try {
			 xMSet.xU.copyFile( estafetteFileName , metadataFileName );
		 }
		 catch (Exception e) {
			do_log(0,"Cannot Copy [" + estafetteFileName + "] -> [" + metadataFileName + "]" + e.getMessage());
			return false;
		 }
		 // check
		 if( xMSet.xU.IsBestand(metadataFileName) == false ) {
			do_error("kloonEstafette - could not copy [" + metadataFileName + "]");
			return false;
		 }
		 do_log(1,"A core metadata file has been cloned [" + metadataFileName + "]");
         // update some of the fields - via DAO
		 cmcBookMetaDataDAO cmeta = new cmcBookMetaDataDAO( xMSet , imageFileName , cPage , logger);
	     if( cmeta.isEmpty() == true ) {
	    	 do_error("Could not fetch metadata from [" + metadataFileName + "]");
	    	 return false;
	     }
	     // metadata has now been set - update specific attributes
		 cmeta.calculateAndSetUIDs(imageFileName , cPage );
	     cmeta.calculateAndSetPageNumber(imageFileName);
		 //    
	     String sComm = cmeta.getComment();
	     if( sComm == null ) sComm = "";
	     if( sComm.trim().length() == 0 ) sComm = "Cloned from [" + estafetteFileName + "]";
         cmeta.setComment( sComm );
         //
         cmeta.writeMetaData();
		 cmeta = null;
		 //
		 return true;
	}

	//-----------------------------------------------------------------------
	private void extract_all_text()
	//-----------------------------------------------------------------------
	{
		cmcTextExport teex = new cmcTextExport(xMSet,logger);
		teex.exportAllText();
		teex=null;
	}

	//-----------------------------------------------------------------------
	private void import_all_text()
	//-----------------------------------------------------------------------
	{
		cmcTextImport teim = new cmcTextImport(xMSet,logger);
		teim.importAllText();
		teim=null;
	}
	
	//-----------------------------------------------------------------------
	private void tensorflow_make_training_set()
	//-----------------------------------------------------------------------
	{
	 	cmcVRMakeTrainingImages ms = new cmcVRMakeTrainingImages( xMSet , logger );
    	ms.make_training_set_via_monitor();
    	ms = null;
    }

	//-----------------------------------------------------------------------
	private void tensorflow_make_single_set()
	//-----------------------------------------------------------------------
	{
		// check whether it is only required to extract the files
		if( xMSet.getTensorFlowPostProcessIndicator() == false ) return;
		// run tensorflow visual recognition
		xMSet.setOCRSummaryResult(null);
		cmcVRRunTensorFlow vr = new cmcVRRunTensorFlow(xMSet,logger);
		boolean ib = vr.performVisualRecognition();
		// feedback the results into the archive files
		if( ib == true ) {
			cmcVRPublishResults pr = new cmcVRPublishResults(xMSet,logger);
			ib = pr.process_results();
			pr=null;
		}
		if( ib == false ) xMSet.setOCRSummaryResult(vr.getErrorMessage());
		vr=null;
	}

}
