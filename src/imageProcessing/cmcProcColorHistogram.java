package imageProcessing;

import generalStatPurpose.gpFrequencyDistributionStatFunctions;


public class cmcProcColorHistogram {

	private int MAXHISTO=256;
	//
	private double[] dhistoGray=null;
	private double[] dhistoRed=null;
	private double[] dhistoGreen=null;
	private double[] dhistoBlue=null;
	//
	private int uGray=0;   // mean 
	private int uRed=0;
	private int uGreen=0;
	private int uBlue=0;
	//
	private int medianGray=0;  // mediaan
	private int medianRed=0;  
	private int medianGreen=0; 
	private int medianBlue=0; 
	//
	private double varianceGray=0;
	private double varianceRed=0;
	private double varianceGreen=0;
	private double varianceBlue=0;
    //
	private double stdDevGray=0;
	private double stdDevRed=0;
	private double stdDevGreen=0;
	private double stdDevBlue=0;
	//
	private int minGray=0;   
	private int minRed=0;
	private int minGreen=0;
	private int minBlue=0;
	//
	private int maxGray=0;   
	private int maxRed=0;
	private int maxGreen=0;
	private int maxBlue=0;
	//
	private int quart1Gray=0;
	private int quart1Red=0;
	private int quart1Green=0;
	private int quart1Blue=0;
	//
	private int quart3Gray=0;
	private int quart3Red=0;
	private int quart3Green=0;
	private int quart3Blue=0;
	//
	private double scale=0;
	//
	private boolean isGrayScale=false;
	private boolean isBlackWhite=false;
	
	private gpFrequencyDistributionStatFunctions pstat=null;
	
	//-----------------------------------------------------------------------
	public cmcProcColorHistogram()
	//-----------------------------------------------------------------------
	{
	    dhistoGray = new double[MAXHISTO];
	    dhistoRed = new double[MAXHISTO];
	    dhistoGreen = new double[MAXHISTO];
	    dhistoBlue = new double[MAXHISTO];
	    for(int i=0;i<MAXHISTO;i++) {
	    	dhistoGray[i]=dhistoGreen[i]=dhistoRed[i]=dhistoBlue[i]=0;
	    }
	    pstat = new gpFrequencyDistributionStatFunctions();
	}
	//-----------------------------------------------------------------------
	public void makeHistogram(int[] pixels)
	//-----------------------------------------------------------------------
	{
        int lum=0;
        int[] histoGray = new int[256];
        int[] histoRed = new int[256];
        int[] histoGreen = new int[256];
        int[] histoBlue = new int[256];
        for(int i=0;i<256;i++)
        {
        	histoGray[i]=histoBlue[i]=histoRed[i]=histoGreen[i]=0;
        }
        for(int i=0;i<pixels.length;i++) 
        { 
                int p = pixels[i]; 
                int r = 0xff & ( p >> 16); 
                int g = 0xff & ( p >> 8); 
                int b = 0xff & p; 
                lum = (int) Math.round(0.2126*r + 0.7152*g + 0.0722*b);
                //
                histoGray[ lum ] = histoGray [ lum ] + 1;
                histoRed[ r ] = histoRed [ r ] + 1;
                histoGreen[ g ] = histoGreen [ g ] + 1;
                histoBlue[ b ] = histoBlue[ b ] + 1;
        }
        uGray  = pstat.getMean(histoGray);
        uRed   = pstat.getMean(histoRed);
        uGreen = pstat.getMean(histoGreen);
        uBlue  = pstat.getMean(histoBlue);
        //System.out.println("Mean gRGB ->" + uGray + " " + uRed + " " + uGreen + " " + uBlue );
      
        // Variance
        varianceGray = pstat.getVarianceDouble(histoGray);
        varianceRed = pstat.getVarianceDouble(histoRed);
        varianceGreen = pstat.getVarianceDouble(histoGreen);
        varianceBlue = pstat.getVarianceDouble(histoBlue);
        // Median
        medianGray = pstat.getMedian(histoGray);
        medianRed = pstat.getMedian(histoRed);
        medianGreen = pstat.getMedian(histoGreen);
        medianBlue = pstat.getMedian(histoBlue);
        //standard deviation
        stdDevGray = pstat.getStdDev(histoGray);
        stdDevRed = pstat.getStdDev(histoRed);
        stdDevGreen = pstat.getStdDev(histoGreen);
        stdDevBlue = pstat.getStdDev(histoBlue);
        // minima
        minGray = pstat.getMin(histoGray);
        minRed = pstat.getMin(histoRed);
        minGreen = pstat.getMin(histoGreen);
        minBlue = pstat.getMin(histoBlue);
        // maxima
        maxGray = pstat.getMax(histoGray);
        maxRed = pstat.getMax(histoRed);
        maxGreen = pstat.getMax(histoGreen);
        maxBlue = pstat.getMax(histoBlue);
        // first quartile
        quart1Gray  = pstat.getQuartile1(histoGray);
        quart1Red   = pstat.getQuartile1(histoRed);
        quart1Green = pstat.getQuartile1(histoGreen);
        quart1Blue  = pstat.getQuartile1(histoBlue);
        // 3rd quartile
        quart3Gray  = pstat.getQuartile3(histoGray);
        quart3Red   = pstat.getQuartile3(histoRed);
        quart3Green = pstat.getQuartile3(histoGreen);
        quart3Blue  = pstat.getQuartile3(histoBlue);		
        
        // Schaalfoctor BxH doorrekenen
        // dit dus een probabiliteits histogram; waarschijnljkheid op een luminantie binnen het spectrum
        scale = (double)pixels.length;
        for(int i=0;i<MAXHISTO;i++)
        {
        	 dhistoGray[i]  = (double)histoGray[i] / scale;
        	 dhistoRed[i]   = (double)histoRed[i] / scale;
        	 dhistoGreen[i] = (double)histoGreen[i] / scale;
        	 dhistoBlue[i]  = (double)histoBlue[i] / scale;
        }
       
        // bepalen of GrayScale
        // ZwartWit : excludeer de meest witte waarden en tel de meest zwarte waarden ; indien zeer veel zwart dan zwartwit
        int AantalGelijkeKleurWaarden = 0 ;
        double ZwartKans = 0;
        double WitKans = 0;
        double RestKans = 0;
        int range=30;
        for(int i=0;i<MAXHISTO;i++)
        {
        	if(  (dhistoRed[i] ==  dhistoGreen[i]) && (  dhistoRed[i] ==  dhistoBlue[i]) ) AantalGelijkeKleurWaarden++;
        	if( i < range ) {
        		ZwartKans +=  dhistoGray[i];
        	}
        	else {
        	 if ( i > (MAXHISTO - range) ) WitKans += dhistoGray[i];
        	                         else RestKans += dhistoGray[i];
        	}
        }
        if( AantalGelijkeKleurWaarden == MAXHISTO ) isGrayScale=true;
        //System.err.println( "GrayScale:" + isGrayScale + " Z=" + ZwartKans + " W=" + WitKans + " R=" + RestKans );
        
    }
	//-----------------------------------------------------------------------
	public boolean isGrayscale()
	//-----------------------------------------------------------------------
	{
		return isGrayScale;
	}
	//-----------------------------------------------------------------------
	public double getScale()
	//-----------------------------------------------------------------------
	{
		return scale;
	}
	//-----------------------------------------------------------------------
	//-----------------------------------------------------------------------
	public double[] getHistoGray()
	{
		return dhistoGray;
	}
	public double[] getHistoRed()
	{
		return dhistoRed;
	}
	public double[] getHistoBlue()
	{
		return dhistoBlue;
	}
	public double[] getHistoGreen()
	{
		return dhistoGreen;
	}
	public int getMeanGray()
	{
		return uGray;
	}
	public int getMeanRed()
	{
		return uRed;
	}
	public int getMeanGreen()
	{
		return uGreen;
	}
	public int getMeanBlue()
	{
		return uBlue;
	}
	public int getMedianGray()
	{
		return medianGray;
	}
	public int getMedianRed()
	{
		return medianRed;
	}
	public int getMedianGreen()
	{
		return medianGreen;
	}
	public int getMedianBlue()
	{
		return medianBlue;
	}
	public double getStdDevGray()
	{
		return stdDevGray;
	}
	public double getStdDevRed()
	{
		return stdDevRed;
	}
	public double getStdDevGreen()
	{
		return stdDevGreen;
	}
	public double getStdDevBlue()
	{
		return stdDevBlue;
	}
	
	public int getMaxGray()
	{
		return maxGray;
	}
	public int getMaxRed()
	{
		return maxRed;
	}
	public int getMaxGreen()
	{
		return maxGreen;
	}
	public int getMaxBlue()
	{
		return maxBlue;
	}
	
	public int getMinGray()
	{
		return minGray;
	}
	public int getMinRed()
	{
		return minRed;
	}
	public int getMinGreen()
	{
		return minGreen;
	}
	public int getMinBlue()
	{
		return minBlue;
	}
	
	
	public int getQuart1Gray()
	{
		return quart1Gray;
	}
	public int getQuart1Red()
	{
		return quart1Red;
	}
	public int getQuart1Green()
	{
		return quart1Green;
	}
	public int getQuart1Blue()
	{
		return quart1Blue;
	}
	
	public int getQuart3Gray()
	{
		return quart3Gray;
	}
	public int getQuart3Red()
	{
		return quart3Red;
	}
	public int getQuart3Green()
	{
		return quart3Green;
	}
	public int getQuart3Blue()
	{
		return quart3Blue;
	}
	
	public double getVarianceGray()
	{
		return varianceGray;
	}
	public double getVarianceRed()
	{
		return varianceRed;
	}
	public double getVarianceGreen()
	{
		return varianceGreen;
	}
	public double getVarianceBlue()
	{
		return varianceBlue;
	}
}
