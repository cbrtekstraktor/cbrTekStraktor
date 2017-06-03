package imageProcessing;

import generalStatPurpose.gpFrequencyDistributionStatFunctions;
import generalpurpose.gpAppendStream;

public class cmcConnectedComponentHistogram {

	 private int refHeigth=0;
	 private int maxBuckets=100;
	 private int[]  histo = new int[maxBuckets+1];
	 private int uCCL=0;   // gemiddelde
	 private int medianCCL=0;  // mediaan
	 private double stdDevCCL=0;
	 private int max=0;
	 private int min=0;
	 private gpFrequencyDistributionStatFunctions pstat=null;
	 private String CodePage=null;
	 
	// we maken een histogram van de hoogten van het histogram; als frequentie breedte nemen we 1 percent
	//-----------------------------------------------------------------------
	public cmcConnectedComponentHistogram(cmcConnectedComponentBoundary[] ccb_ar , int payloadh , String cp)
	//-----------------------------------------------------------------------
	{
	  for(int i=0;i<histo.length;i++) histo[i]=0;   // aantal + 1
	  pstat = new gpFrequencyDistributionStatFunctions();
	  refHeigth = payloadh;
	  CodePage=cp;
	  make_frequency_distribution(ccb_ar);
	}
	//-----------------------------------------------------------------------
	private void make_frequency_distribution(cmcConnectedComponentBoundary[] ccb_ar)
	//-----------------------------------------------------------------------
	{
	  for(int i=0;i<ccb_ar.length;i++)
	  {
		int hoogte = ccb_ar[i].MaxY - ccb_ar[i].MinY + 1;
		int idx = (int)(Math.round(((double)hoogte / (double)refHeigth) * maxBuckets));
		histo[idx]++;
	  }
	  // mean 
	  uCCL  = pstat.getMean(histo);
      // median
      medianCCL = pstat.getMedian(histo);
      // StandardDev
      stdDevCCL = pstat.getStdDev(histo);
      //
      max = pstat.getMax(histo);
      //
      min = pstat.getMin(histo);
	}
	
	//-----------------------------------------------------------------------
	public int[] getHisto()
	//-----------------------------------------------------------------------
	{
		return histo;
	}
	//-----------------------------------------------------------------------
	public int getMean()
	//-----------------------------------------------------------------------
	{
		return uCCL;
	}
	//-----------------------------------------------------------------------
	public int getMedian()
	//-----------------------------------------------------------------------
	{
		return medianCCL;
	}
	//-----------------------------------------------------------------------
	public double getStdDev()
	//-----------------------------------------------------------------------
	{
		return stdDevCCL;
	}
	//-----------------------------------------------------------------------
	public void dumpHisto(String FNaam)
	//-----------------------------------------------------------------------
	{
		
		gpAppendStream aps = new gpAppendStream(FNaam,CodePage);
		aps.AppendIt("<ConnectedComponentFrequencyDistribution>");
		aps.AppendIt("<!-- Connected Component HEIGTH Frequency Distribution distributed over [" + histo.length + "] buckets -->");
		//
		aps.AppendIt("<NormalizationHeigth>" + refHeigth + "</NormalizationHeigth>");
		aps.AppendIt("<Bins>" + histo.length + "</Bins>");
		aps.AppendIt("<Mean>" + uCCL + "</Mean>");
		aps.AppendIt("<Median>" + medianCCL + "</Median>");
		aps.AppendIt("<StandardDeviation>" + stdDevCCL + "</StandardDeviation>");
		aps.AppendIt("<Max>" + max + "</Max>");
		aps.AppendIt("<Min>" + min + "</Min>");
		aps.AppendIt("<Mode>" + pstat.getMode(histo) + "</Mode>" );
		//
		String sLijn = "";
		for(int i=0;i<histo.length;i++)
		{
			if( i== 0) sLijn = "<Frequency>" + histo[i];
			else sLijn = sLijn + "," + histo[i];
		}
		sLijn = sLijn + "</Frequency>";
		aps.AppendIt(sLijn);
        //
		aps.AppendIt("</ConnectedComponentFrequencyDistribution>");
		//
        aps.CloseAppendFile();
        
	}
}
