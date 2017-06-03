package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import logger.logLiason;
import imageProcessing.cmcTimingInfo;
import generalpurpose.gpAppendStream;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;
import cbrTekStraktorModel.runTimeMonitor;


public class cmcTimingDAO {

	cmcProcSettings xMSet=null;
	gpAppendStream aps = null;
    logLiason logger=null;
    
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
	public cmcTimingDAO(cmcProcSettings is,logLiason ilog)
	//------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
	}
	//-----------------------------------------------------------------------
	public boolean writeTimingStatistics(cmcTimingInfo tim)
	//-----------------------------------------------------------------------
	{
		    if( tim == null ) {
		    	do_error("Empty timing class");
		    	return false;
		    }
			String TimeFile = xMSet. getTimingFile();
			boolean newFile = !xMSet.xU.IsBestand(TimeFile);
			//
			aps = new gpAppendStream(TimeFile,xMSet.getCodePageString());
			//
			if( newFile ) {
			 aps.AppendIt("UID|Width|Heigth|FileSize|ColourScheme|FileType|ConnectedComponents|Paragraphs|BWDensity|ImageLoadTime|PageLoadTime|Preprocess|BinarizeTime|CoCoTime|LetterTime|ParagraphTime|OverheadTime");
			}
			//
			String sLijn = tim.getUID() + "|" + 
			               tim.getWidth() + "|" +
			               tim.getHeigth() + "|" +
					       tim.getFileSize() + "|" +
                           tim.getColourScheme() + "|" +
			               tim.getImageType() + "|" +
                           tim.getBinarizeMethod() + "|" +
					       tim.getNbrOfConnectedComponents() + "|" +
			               tim.getNbrOfParagraphs() + "|" +
					       tim.getBWDensity() + "|" +
			               tim.getLoadTimeImage() + "|" +
					       tim.getLoadTimePage() + "|" +
			               tim.getLoadTimePreprocess() + "|" +
			               tim.getLoadTimeBinarize() + "|" +
					       tim.getLoadTimeConnectedComponents() + "|" +
			               tim.getLoadTimeLetters() + "|" +
			               tim.getLoadTimeParagraphs() + "|" +
					       tim.getLoadTimeOverhead() + "|" +
					       (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase();
			aps.AppendIt(sLijn);
			aps.CloseAppendFile();
			aps=null;
			return true;
	}
	
	//-----------------------------------------------------------------------
	public cmcTimingInfo[] grabAllTimingInfo()
	//-----------------------------------------------------------------------
	{
		String TimeFile = xMSet. getTimingFile();
		if( xMSet.xU.IsBestand(TimeFile) == false ) {
			do_error("Cannot locate timing file [" + TimeFile +"]");
			return null;
		}
		cmcTimingInfo[] ar = new cmcTimingInfo[1000];
		for(int i=0;i<ar.length;i++)
		{
			ar[i] = new cmcTimingInfo();
			ar[i].setUID(null);
		}
		try {
			File inFile  = new File(TimeFile);  // File to read from.
	       	//BufferedReader reader = new BufferedReader(new FileReader(inFile));
	  	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
	       	//
	       	String sLijn=null;
	       	int lijnteller=0;
	       	int idx=-1;
	       	while ((sLijn=reader.readLine()) != null) {
	       		lijnteller++;
	       		if(lijnteller<=1) continue;
	       		sLijn = sLijn.trim() + "|";
	       		//
	       		//System.out.println( "->" + sLijn );
	       		String UID             = xMSet.xU.GetVeld(sLijn, 1, '|');
	       		int width              = xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn, 2, '|'));
	       		int heigth             = xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn, 3, '|'));
	       	    long filesize          = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 4, '|'));
	       	    String ColourScheme    = xMSet.xU.GetVeld(sLijn, 5 , '|').trim();
	       	    String FileType        = xMSet.xU.GetVeld(sLijn, 6 , '|').trim();
	       		String BinarizeMethod  = xMSet.xU.GetVeld(sLijn, 7 , '|').trim();
	       		int coconum            = xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn, 8, '|'));
	       		int paranum            = xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn, 9, '|'));
	       		double dens            = xMSet.xU.NaarDouble(xMSet.xU.GetVeld(sLijn, 10, '|'));
	       		long ImageLoadTime     = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 11, '|'));
	       		long PageLoadTime      = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 12, '|'));
	       		long PreprocLoadTime   = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 13, '|'));
	       		long BinarizeLoadTime  = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 14, '|'));
	       		long CoCoLoadTime      = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 15, '|'));
	       		long LetterLoadTime    = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 16, '|'));
	       		long ParagraphLoadTime = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 17, '|'));
	       		long OverheadLoadTime  = xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn, 18, '|'));
	       		String tim             = xMSet.xU.GetVeld(sLijn, 19, '|');
	       		//
	       		long e2e = ImageLoadTime + PageLoadTime + PreprocLoadTime + BinarizeLoadTime + CoCoLoadTime + LetterLoadTime + ParagraphLoadTime + OverheadLoadTime;
	       		//
	       		if( tim.indexOf("-")<0 ) {
	       			do_error("Incorrect formated line " + ColourScheme + " " + tim + " " + sLijn);
	       			continue;
	       		}
	       		//
	       		idx++;
	       		if( idx >= ar.length ) idx=0;
	       		//
	       		ar[idx].setUID( UID );
	       		ar[idx].setWidth( width);
	       		ar[idx].setHeigth( heigth);
	       		ar[idx].setFileSize( filesize);
	       		ar[idx].setImageType(FileType);
	       		ar[idx].setColourScheme( ColourScheme);
	       		ar[idx].setBinarizeMethod(BinarizeMethod);
	       		ar[idx].setNbrOfConnectedComponents(coconum);
	       		ar[idx].setNbrOfParagraphs(paranum);
	       		ar[idx].setBWDensity(dens);
	       		ar[idx].setImageTime(ImageLoadTime);
	       		ar[idx].setPageTime(PageLoadTime);
	       		ar[idx].setPreprocessTime(PreprocLoadTime);
	       		ar[idx].setBinarizeTime(BinarizeLoadTime);
	       		ar[idx].setConnectedComponentTime(CoCoLoadTime);
	       		ar[idx].setLetterTime(LetterLoadTime);
	       		ar[idx].setParagraphTime(ParagraphLoadTime);
	       		ar[idx].setEndToEndTime(e2e);
                //
	       	}
	       	reader.close();
	    }
	    catch ( Exception e ) {
	    	do_error("Error reading timing info file [" + TimeFile + "]" + xMSet.xU.LogStackTrace(e));
	    	return null;
	    }
	  	return ar;
	}
	
	private String dd(long l)
	{
	  return String.format("%6d",l);	
	}
	
	public boolean dumpRunTimeEstimationAccurarcy(runTimeMonitor moni)
	{
		if( moni == null ) return false;
	    //
		long e1 = moni.getEstimatedImageTime() / 1000000L;
		long e2 = moni.getEstimatedE2BeforePreprocessTime()/ 1000000L;
		long e3 = moni.getEstimatedE2BeforeBinarizeTime()/ 1000000L;
		long e4 = moni.getEstimatedE2BeforeCoCoTime()/ 1000000L;
		//
		long a1 = moni.getActualImageTime()/ 1000000L;
		long a2 = moni.getActualEndToEndTime()/ 1000000L;  // E2E
		long a3 = moni.getActualEndToEndTime()/ 1000000L;
		long a4 = moni.getActualEndToEndTime()/ 1000000L;
		//
		float r1 = a1 <= 0 ? 0 : (float)(e1 - a1) / (float)a1;
		float r2 = a2 <= 0 ? 0 : (float)(e2 - a2) / (float)a2;
		float r3 = a3 <= 0 ? 0 : (float)(e3 - a3) / (float)a3;
		float r4 = a1 <= 0 ? 0 : (float)(e4 - a4) / (float)a4;
		//
		//long p1 = (long)Math.abs( (double)(r1 * 100) );
		//long p2 = (long)Math.abs( (double)(r2 * 100) );
		//long p3 = (long)Math.abs( (double)(r3 * 100) );
		//long p4 = (long)Math.abs( (double)(r4 * 100) );
		long p1 = (long)(r1 * 100);
		long p2 = (long)(r2 * 100);
		long p3 = (long)(r3 * 100);
		long p4 = (long)(r4 * 100);
				
		// milliseconds
		String ss = " (" + dd(e1) + "ms, " + dd(a1) + "ms, " + dd(p1) + "%)" + 
				    " (" + dd(e2) + "ms, " + dd(a2) + "ms, " + dd(p2) + "%)" +
				    " (" + dd(e3) + "ms, " + dd(a3) + "ms, " + dd(p3) + "%)" +
				    " (" + dd(e4) + "ms, " + dd(a4) + "ms, " + dd(p4) + "%)";
        do_log(1,"Estimate Accurary [" + dd(p4) + "%]");
		
		String TimeFile = xMSet.getTimingAccuracyFile();
		boolean newFile = !xMSet.xU.IsBestand(TimeFile);
		//
		aps = new gpAppendStream(TimeFile,xMSet.getCodePageString());
		//
		if( newFile ) {
		 aps.AppendIt("TimeStamp (EstimatedImage,ActualImage,Ratio) (EstimatedBeforePreprocess,ActualbeforPreprocess,Ratio) (EstimatedBeforeBinarize,ActualBeforeBinarize,Ratio) (EstimatedBeforeCoCo,ActualBeforeCoCo,Ratio)");
		}
		aps.AppendIt( (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase()+ " " + ss);
		aps.CloseAppendFile();
		aps=null;
		return true;
	}
}
