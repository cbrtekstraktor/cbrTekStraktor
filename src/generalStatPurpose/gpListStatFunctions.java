package generalStatPurpose;

import logger.logLiason;

public class gpListStatFunctions {
	
	logLiason logger=null;
	
	private int[] set=null;
	private int aantal=-1;
	private int mean=-1;
	private int maximum=-1;
	private int minimum=-1;
	private int median=-1;
	private double meanVariance=-1;
	private double standardDeviation=-1;
	private int quartile1=-1;
	private int quartile3=-1;
	private int iqr=-1;

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
	private void reset()
	//------------------------------------------------------------
	{
		   // init
		   mean = -1;
		   median = -1;
		   meanVariance=-1;
		   maximum=-1;
		   minimum=-1;
		   quartile1=-1;
		   quartile3=-1;
		   standardDeviation=-1;
		   iqr=-1;
	}

	//------------------------------------------------------------
	public gpListStatFunctions(int[] is , logLiason ilog)
	//------------------------------------------------------------
	{
	   logger=ilog;
	   reset();
	   try {
	   //
	   // kopieren
	   aantal = is.length;	
	   if( aantal <= 0 ) return;
	   set = new int[aantal];
       for(int i=0;i<aantal;i++) {
		   set[i] = is[i];
		   if( i == 0 ) {
			   minimum = set[i];
			   maximum = set[i];
		   }
		   else {
			   if( minimum > set[i] ) minimum = set[i];
			   if( maximum < set[i] ) maximum = set[i];
		   }
	   }
	   // sorteren
	   boolean swap=false;
	   for(int i=0;i<aantal;i++)
	   {
		   swap=false;
		   for(int j=0;j<(aantal-1);j++)
		   {
			   if( set[j] > set[j+1] ) {
				   int k = set[j];
				   set[j] = set[j+1];
				   set[j+1] = k;
				   swap=true;
			   }
		   }
		   if( swap == false ) break;
	   }
	   //
	   // Rekenkundig gemiddelde
	   double sum=0;
	   for(int i=0;i<aantal;i++)
	   {
		   sum += (double)set[i];
	   }
	   mean = (int)Math.round( sum / (double)aantal );
	   //
	   // median
	   if( (!isEven( aantal)) || (aantal < 2) ) {  // Onven neem gewoon de middelste
			int midi = aantal / 2;  // + 1 doch we tellen van zero
			median  = set[midi];
		}
		else {  // even, neem de waarde van DIV 2 en tal daarbij  de helft van her verschil van de waarden van DIV2 en 1+DIV2 
			int midi = aantal / 2;
			int wdelta = (int)Math.round(((double)(set[ midi] - set[ midi - 1 ]) / 2));
			median = set[ midi -1 ] + wdelta;
		}
	    //
	    // gemiddelde afwijking
	    double bmean =  sum / (double)aantal;
		double sq = 0;
		double dd=0;
		for(int i=0;i<aantal;i++)
		{
			dd = (double)set[i] - bmean;
			sq += (dd * dd);
		}
		if( aantal > 1 ) meanVariance = sq / ((double)aantal - 1); else meanVariance = sq;
		standardDeviation = Math.sqrt( meanVariance );
	   }
	   catch(Exception e) {
		     reset();
		     do_error("oops " + e.getMessage() );
			 return;	   
	    }
	   
		//
		// quartilen
		// Q1 =  ( N + 1 ) / 4 
		// Q3 = (3N + 3 -) / 4
		int quart1idx = ((int)Math.round(((double)aantal + 1)/4) + 1 );
		int quart3idx = ((int)Math.round( ((3*(double)aantal + 3))/4) + 1 );
		// boundaries
		if( quart1idx >= aantal ) quart1idx = aantal - 1 ;
		if( quart1idx < 0 ) quart1idx = 0;
		if( quart3idx >= aantal ) quart3idx = aantal - 1 ;
		if( quart3idx < 0 ) quart3idx=0;
		//
		try {
				 quartile1 = set[quart1idx];
				 quartile3 = set[quart3idx];
				 iqr = quartile3 - quartile1;
		}
		catch(Exception e) {
			        reset();
					do_error("oops " + e.getMessage() );
					do_error("Q1IDX=" + quart1idx + " Q3IDX=" + quart3idx + "   Aantal=" + aantal + " " + aantal);
					return;
		}
	}
	//
	//------------------------------------------------------------
	private boolean isEven(int i)
	//------------------------------------------------------------
	{
			double rest = i % 2;
			boolean even = (rest == 0 ? true : false);
			return even;
	}

	public int getMean()
	{
		return mean;
	}
	public int getMaximum()
	{
		return maximum;
	}
	public int getMinimum()
	{
		return minimum;
	}
	public int getMedian()
	{
		return median;
	}
	public double getMeanVariance()
	{
		return meanVariance;
	}
	public int getQuartile1()
	{
		return quartile1;
	}
	public int getQuartile3()
	{
		return quartile3;
	}
	public int getIQR()
	{
		return iqr;
	}
	public double getStdDev()
	{
		return standardDeviation;
	}
}
