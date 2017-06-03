package generalStatPurpose;

public class gpFrequencyDistributionStatFunctions {

	 //-----------------------------------------------------------------------
	 public gpFrequencyDistributionStatFunctions()
	 //-----------------------------------------------------------------------
	 {
		 
	 }
	 //-----------------------------------------------------------------------	 
	 public double getMeanDouble(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int  n=0;
		 double fx=0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 fx += (double)dist[i] * (double)i;   // Is alleen correct indien f(i) = i
		 }
		 return Math.round(fx / (double)n);
	 }
	 //-----------------------------------------------------------------------	 
	 public int getMean(int[] dist)
	 //-----------------------------------------------------------------------
	 {
			 return (int)getMeanDouble(dist);
	 }
	 // 0 = geen variantie   --->   sterk verschillend 
	 // VAR =  SUM [ f(x) * ( x - mean ) ^ 2  ] /  n   :  n = aantal waarden
	 // VAR =  SUM [ f(x) * ( x - mean ) ^ 2  ] /  ( n - 1 ) => dit is Sample mean ; dit wordt hier gebruikt
	 //-----------------------------------------------------------------------
	 public double getVarianceDouble(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 double mean = getMeanDouble(dist);
		 int  n=0;
		 double fx=0;
		 double sq=0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 sq = ((double)i - mean);
			 fx += (sq * sq) * (double)dist[i];    
		 }
		 return fx / ((double)n - 1);
	 }
	 // Gemiddelde afwijking    SUM [ f(x) . ABS(x - mean) ] / N
	 //-----------------------------------------------------------------------
	 public double getGemiddeldeAfwijking(int[] dist)
	 //-----------------------------------------------------------------------
	 {
			 double mean = getMeanDouble(dist);
			 int  n=0;
			 double fx=0;
			 double sq=0;
			 for(int i=0;i<dist.length;i++)
			 {
				 n += dist[i];
				 sq = Math.abs(((double)i - mean));
				 fx += sq * (double)dist[i];    
			 }
			 return fx / (double)n;
	 }
	 //-----------------------------------------------------------------------
	 public int getMedian(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
		 }
		 int half = (int)Math.round((double)n / 2);
		 n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 if( n >= half ) return i;
		 }
		 return -1;
	 }
	 //-----------------------------------------------------------------------
	 public int getQuartile1(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
		 }
		 int q1 = (int)Math.round((double)n / 4);
		 n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 if( n >= q1 ) return i;
		 }
		 return -1;
	 }
	 //-----------------------------------------------------------------------
	 public int getQuartile3(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
		 }
		 int q1 = (int)Math.round(((double)n * 3 ) / 4);
		 n =0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 if( n >= q1 ) return i;
		 }
		 return -1;
	 }
	 //-----------------------------------------------------------------------
	 public int getMin(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 for(int i=0;i<dist.length;i++)
		 {
			 if( dist[i] > 0 ) return i;
		 } 
		 return dist.length;
	 }
	 //-----------------------------------------------------------------------
	 public int getMax(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 for(int i=dist.length-1;i>=0;i--)
		 {
			 if( dist[i] > 0 ) return i;
		 } 
		 return 0;
	 }
	 //-----------------------------------------------------------------------
	 public int getMode(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int mode = 0;
		 for(int i=0;i<dist.length;i++)
		 {
			 if( dist[i] > mode ) mode = dist[i];
		 }
		 return mode;
	 }
	 //-----------------------------------------------------------------------
	 public double getAbsoluteStandardDeviation(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int med = this.getMedian(dist);
		 double fx=0;
		 int n=0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 fx += (double)dist[i] * (  ((double)i - (double)med)  );
		 }
		 fx = fx / (double)n;
		 return fx;
	 }
	 //-----------------------------------------------------------------------
	 public double getStdDev(int[] dist)
	 //-----------------------------------------------------------------------
	 {
		 int mean = this.getMean(dist);
		 int  n=0;
		 double fx=0;
		 for(int i=0;i<dist.length;i++)
		 {
			 n += dist[i];
			 fx += (double)dist[i] * (  ((double)i - (double)mean) * ((double)i - (double)mean)   );
		 }
		 fx = fx / (double)n;
		 fx = Math.sqrt(fx);
		 return fx;
	 }
}
