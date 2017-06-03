package imageProcessing;

import java.util.ArrayList;

import logger.logLiason;


public class cmcMonochromeImageDetector {

	logLiason logger=null;
	
	private double[] tripleHisto = null;
	private double[] smoothHisto = null;
	
	private double mean    = -1;
	private double quart1  = -1;
	private double quart2  = -1;
	private double quart3  = -1;
	private double minimum = -1;
	private double maximum = -1;
	
	private final int UP   = 1;
	private final int DOWN = 2;
	
	private int PEAK_BASE_WIDTH = 15;
	private double slopeThreshold=-1;
	
	class peak
	{
		int     MinX=-1;
		int     MaxX=-1;
		int     PeakX=-1;
		double  leftPeakHeigth=-1;
		double  rightPeakHeigth=-1;
		int     leftPeakWidth=-1;
		int     rightPeakWidth=-1;
		boolean leftHeigthIsValid=false;
		boolean rightHeigthIsValid=false;
		double  halfWidthSlopeLeft;
		double  halfWidthSlopeRight;
		boolean leftSlopeIsValid=false;
		boolean rightSlopeIsValid=false;
		int     leftMidPeakWidthIdx;
		int     rightMidPeakWidthIdx;
		boolean leftPeakIsValid=false;
		boolean rightPeakIsValid=false;
		boolean peakIsValid=false;
		peak(int sx , int ex , int tx)
		{
			MinX = sx;
			MaxX = ex;
			PeakX = tx;
			leftPeakWidth  = PeakX - MinX;
			rightPeakWidth = MaxX - PeakX;
			leftHeigthIsValid=false;
			rightHeigthIsValid=false;
			halfWidthSlopeLeft=-1;
			halfWidthSlopeRight=-1;
			leftPeakIsValid=false;
			rightPeakIsValid=false;
			peakIsValid=false;
		}
	}
	ArrayList<peak> peakList = null;
	
	class monochromeVector {
		int numberOfPeaks=-1;
		int numberOfValidPeaks=-1;
		double peakCoverage=-1;
		boolean isGrayScale=false;
		boolean isMonochrome=false;
	}
	private monochromeVector result=null;
	
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
	
	// GrayLst is a NORMALIZED grayscale histogram 
	// NORMALISED ==>   number of entries per bin() / count of all entries OR SUM(all bins) = 1
	//=================================================================
	public cmcMonochromeImageDetector(double[] grayLst , boolean isGrayScaleIn , logLiason ilog)
	//=================================================================
	{
		logger=ilog;
		try {
		result = new monochromeVector();
		result.isGrayScale = isGrayScaleIn;
	    tripleHisto = new double[grayLst.length*3];
	    smoothHisto = new double[tripleHisto.length];
	    mirrorHistoLeftRight(grayLst);
	    smoothGraph();
	    runStatistics();    
	    curveWalk();
	    processPeaks();
	    setClassification();
	    shoReport();
		}
		catch(Exception e) {
			do_error("(cmcMonochromeImageDetector) System error" + e.getMessage() );
		}
	    //dump();
	}
	
	// mirror histo to the left and to the right
	//=================================================================
	private void mirrorHistoLeftRight(double[] lst)
	//=================================================================
	{
		for(int i=0;i<lst.length;i++)
		{
			tripleHisto[lst.length+i] = lst[i];
			
		}
		for(int i=0;i<(lst.length-1);i++)
		{
			tripleHisto[lst.length-i-1] = lst[i+1];
			tripleHisto[(2*lst.length)+i] = tripleHisto[(2*lst.length)-i-2];
		}
	}
	
    // kind of a runnign average smoother
	//=================================================================
	private void smoothGraph()
	//=================================================================
	{
		    int range = 3;
	        int ondergrens=0;
	        int bovengrens=0;
	        for(int i=0;i<tripleHisto.length;i++)
	        {
	        	ondergrens = i - range; if ( ondergrens < 0 ) ondergrens = 0;
	        	bovengrens = i + range; if ( bovengrens >= tripleHisto.length  ) bovengrens = tripleHisto.length - 1;
	        	double smooth = 0;
	        	double gewicht=0;
	        	for(int j=ondergrens;j<=bovengrens;j++) {
	        		int multi = 1 + range - Math.abs(i - j);
	        		gewicht += multi;
	        		smooth += tripleHisto[j] * (double)multi;
	        	}
	        	smoothHisto[i] = smooth / gewicht;
	        }
	}
	
	//=================================================================
	private void runStatistics()
	//=================================================================
	{
		// dupliceer midden
		int aantal = smoothHisto.length / 3;
		double[] sort = new double[aantal];
		for(int i=0; i<aantal ; i++)
		{
			sort[i] = smoothHisto[ aantal + i];
		}
		// sorteer
		boolean swap = false;
		for(int i=0;i<aantal;i++)
		{
			swap = false;
			for(int k=0;k<(aantal-1);k++)
			{
				if( sort[k] > sort[k+1] ) {
					double dd = sort[k];
					sort[k] = sort[k+1];
					sort[k+1] = dd;
					swap=true;
				}
			}
			if( swap == false ) break;
		}
		//
		maximum = -1;
		minimum = 640000;
		for(int i=0;i<aantal;i++)
		{
		  mean += sort[i];
		  if( (i >= (aantal/4)) && (quart1==-1) ) quart1 = sort[i];
		  if( (i >= (aantal/2)) && (quart2==-1) ) quart2 = sort[i];
		  if( (i >= ((aantal*3)/4)) && (quart3==-1) ) quart3 = sort[i];
		  if( maximum < sort[i] ) maximum = sort[i];
		  if( minimum > sort[i] ) minimum = sort[i];
		}
		mean = mean / (double)aantal;
		//
	}

	// walk along he curve of the smoothed histogram
	//=================================================================
	private void curveWalk()
	//=================================================================
	{
		peakList = new ArrayList<peak>();
		int trend = DOWN;
		int direc = DOWN;
		int peakStartX  = -1;
		int peakStopX   = -1;
		int peakPeakX   = -1;
		int peakCounter = 0;
		int upCounter=0;
		int downCounter=0;
		int flatCounter=0;
		int flatX=-1;
		for(int i=1;i<smoothHisto.length;i++)
		{
			if( smoothHisto[i-1] == smoothHisto[i] ) {
				if( upCounter == 0 ) peakStartX = i;  // still in valley so wait until curve rises
				flatCounter++;
				if( flatCounter == 1 ) flatX = i - 1;  // start of top or valley
				continue;
			}
			else flatCounter=0;
			if( smoothHisto[i-1] < smoothHisto[i] ) {
				direc = UP;
				upCounter++;
			}
			else {
				direc = DOWN;
				downCounter++;
			}
			if( trend == direc ) continue;
			// slope switch from up to down : this is a peak
			if( trend == UP ) {
				trend = DOWN;
				if( flatCounter >= 2) {  // find mid of top
					int w = ((i -1) - flatX) / 2;
					peakPeakX = flatX + w;
				}
				else peakPeakX = i - 1;  // vorige
				continue;
			}
			// this is a valley
			peakStopX = i - 1; // vorige
			//
			peak x = new peak( peakStartX , peakStopX , peakPeakX);
			// reset
			trend = UP;
			peakStartX = i;
			peakStopX  = -1;
			peakPeakX  = -1;
			upCounter=0;
			downCounter=0;
			flatCounter=0;
			flatX=-1;
			// if first peak detected ignore ; left mirror
			peakCounter++;
			if( peakCounter <= 1 ) continue;
			if( x.MaxX < (smoothHisto.length / 3) ) continue;     // in Left mirrored part
			if( x.MinX > ((smoothHisto.length * 2) / 3) ) break;  // in right mirrored part
			//
			peakList.add(x);
		}
	}
	
	// determine the heights and the slopes of the left and right hand side of the peak
	// compare against threshold values
	//=================================================================
	private void processPeaks()
	//=================================================================
	{
		double MAXHORZ = (double)( smoothHisto.length / 3);    // You need this to NORMALIZE  the horizontal axis
		double validHeigthLimit = quart2 + ((quart3 - quart1) * 3) / 2;  //  mean + 3/2 IQR
		slopeThreshold =  ((double)PEAK_BASE_WIDTH / (double)MAXHORZ) / ( maximum - minimum );
	    //
		for(int i=0;i<peakList.size();i++)
		{
			double hStart = smoothHisto[ peakList.get(i).MinX ];
			double hEnd   = smoothHisto[ peakList.get(i).MaxX ];
			double hTop   = smoothHisto[ peakList.get(i).PeakX ];
			//
			peakList.get(i).leftPeakHeigth  = hTop - hStart;
			peakList.get(i).rightPeakHeigth = hTop - hEnd;
			if( peakList.get(i).leftPeakHeigth < validHeigthLimit ) peakList.get(i).leftHeigthIsValid = false; else peakList.get(i).leftHeigthIsValid = true;
			if( peakList.get(i).rightPeakHeigth < validHeigthLimit ) peakList.get(i).rightHeigthIsValid = false; else peakList.get(i).rightHeigthIsValid = true;
			//
			// left peak slope for half left width
			int midLeft = peakList.get(i).leftPeakWidth / 2; if( midLeft < 1 ) midLeft = 1; 
			peakList.get(i).leftMidPeakWidthIdx = peakList.get(i).PeakX - midLeft;
			double leftFx = smoothHisto[ peakList.get(i).leftMidPeakWidthIdx ];
			double leftH = (hTop - leftFx); // / ( maximum - minimum );
			peakList.get(i).halfWidthSlopeLeft = ((double)midLeft / (double)MAXHORZ ) / (double)leftH;
			//
			// right peak slope for half right width
			int midRight = peakList.get(i).rightPeakWidth / 2; if (midRight < 1 ) midRight = 1;
			peakList.get(i).rightMidPeakWidthIdx = peakList.get(i).PeakX + midRight;
			double rightFx = smoothHisto[ peakList.get(i).rightMidPeakWidthIdx ];
			double rightH = (hTop - rightFx); // / ( maximum - minimum );;
			peakList.get(i).halfWidthSlopeRight =  ((double)midRight / (double)MAXHORZ)  / (double)rightH;
			//
            //
			if( peakList.get(i).halfWidthSlopeLeft <= slopeThreshold ) peakList.get(i).leftSlopeIsValid = true; else peakList.get(i).leftSlopeIsValid = false;
			if( peakList.get(i).halfWidthSlopeRight <= slopeThreshold ) peakList.get(i).rightSlopeIsValid = true; else peakList.get(i).rightSlopeIsValid = false;
			//
			if( peakList.get(i).leftHeigthIsValid && peakList.get(i).leftSlopeIsValid ) peakList.get(i).leftPeakIsValid = true; else peakList.get(i).leftPeakIsValid = false;
			if( peakList.get(i).rightHeigthIsValid && peakList.get(i).rightSlopeIsValid ) peakList.get(i).rightPeakIsValid = true; else peakList.get(i).rightPeakIsValid = false;
			if( peakList.get(i).rightPeakIsValid || peakList.get(i).leftPeakIsValid ) peakList.get(i).peakIsValid = true; else peakList.get(i).peakIsValid = false;
			
			
			// ONLY when in err, so never ..
			if( (leftH < 0) || (rightH < 0) ) {
			do_error("Min Tg= " + slopeThreshold + " Min" + minimum + "Max");
			do_error("ERROR ===========================================" + maximum + " " + minimum);
			do_error("LEFT  H=" + leftH  + " B=" +  midLeft   + " Tg=" + peakList.get(i).halfWidthSlopeLeft + " " + peakList.get(i).leftPeakIsValid);
			do_error("RIGHT H=" + rightH + " B=" +  midRight  + " Tg=" + peakList.get(i).halfWidthSlopeRight + " " + peakList.get(i).rightPeakIsValid);
						if( leftH < 0 ) {
							do_error("ERROR -> lefH" + leftH);
							do_error("   LftX=" + peakList.get(i).MinX +                 " LeftHoogte=" + (hStart*100000));
							do_error("   MidX=" + peakList.get(i).leftMidPeakWidthIdx  + " Mid Hoogte=" + (leftFx*100000));
							do_error("   TopX=" + peakList.get(i).PeakX +                " Top Hoogte=" +   (hTop*100000));
							do_error("   MidH=" + ((hTop - leftFx) * 100000));
							do_error("   Denom=" + (((hTop - leftFx) - minimum)*100000) );
							do_error( "  deler=" + (maximum - minimum) );
							do_error("   Normal=" + (hTop - leftFx ) / ( maximum - minimum ) );
						}
			}			
		}
		
		
	}
	
	//=================================================================
	private void setClassificationMethodOne()
	//=================================================================
	{
		result.numberOfValidPeaks=0;
		result.isMonochrome=false;
		int range = smoothHisto.length / 3;
		for(int i=0;i<peakList.size();i++)
		{
			if( (peakList.get(i).PeakX < range) || (peakList.get(i).PeakX >= (range*2)) ) continue;
			if( peakList.get(i).peakIsValid ) result.numberOfValidPeaks++;
		}
	}
	//=================================================================
	private void setClassificationMethodTwo()
	//=================================================================
	{
		// find 2 highest peaks + integral + coverage larger than 95%
		int maxPeakIdx=-1;
		double maxx=-1;
		for(int i=0;i<peakList.size();i++)
		{
			double h = Math.max(peakList.get(i).leftPeakHeigth,peakList.get(i).rightPeakHeigth);
			if( h > maxx ) { maxx = h; maxPeakIdx = i; }
		}
		int secondMaxPeakIdx=-1;
		maxx=-1;
		for(int i=0;i<peakList.size();i++)
		{
			if( i == maxPeakIdx ) continue;
			double h = Math.max(peakList.get(i).leftPeakHeigth,peakList.get(i).rightPeakHeigth);
			if( h > maxx ) { maxx = h; secondMaxPeakIdx = i; }
		}
		// veiligheid
		if( secondMaxPeakIdx < 0 ) {
			secondMaxPeakIdx = maxPeakIdx;
			do_log(1,"cmcMonochromeImageDetector -> there is no second peak. This is an image with a single colour");
		}
		// integraal
		int range = smoothHisto.length / 3;
		int min1 = peakList.get(maxPeakIdx).MinX;
		int max1 = peakList.get(maxPeakIdx).MaxX;
		int min2 = peakList.get(secondMaxPeakIdx).MinX;
		int max2 = peakList.get(secondMaxPeakIdx).MaxX;
		//
		result.peakCoverage=0;
		for(int i=0; i<range ; i++)
		{
			int idx = i + range;
			if( ((idx>=min1) && (idx<=max1)) || ((idx>=min2) && (idx<=max2)) ) {
				result.peakCoverage += tripleHisto[idx];
			}
		}
	}
	//=================================================================
	private void setClassification()
	//=================================================================
	{
		result.numberOfPeaks = peakList.size();
		setClassificationMethodOne();
		setClassificationMethodTwo();
		double coverageThreshold = (double)0.85;
		if(  result.isGrayScale ) coverageThreshold = (double)0.60;
		if( (result.numberOfValidPeaks<=2) && (result.peakCoverage>=coverageThreshold) ) result.isMonochrome=true;
	}
	//=================================================================
	private boolean isPeak(int idx)
	//=================================================================
	{
		for(int i=0;i<peakList.size();i++)
		{
			if( idx == peakList.get(i).PeakX ) return true;
		}
		return false;
	}
	//=================================================================
	private boolean isValidPeak(int idx)
	//=================================================================
	{
		for(int i=0;i<peakList.size();i++)
		{
			if( (idx == peakList.get(i).PeakX ) && ( peakList.get(i).peakIsValid ) ) return true;
		}
		return false;
	}
	
	//=================================================================
	public double[] getSmoothHisto() // middelste
	//=================================================================
	{
		double[] ar = new double[smoothHisto.length/3];
		for(int i=0;i<ar.length;i++) {
			int idx = i + ar.length;
			ar[i] = smoothHisto[idx];
			if ( isValidPeak(idx) ) { ar[i] += 2; continue; }
			if ( isPeak(idx) ) ar[i] += 1;
		}
		return ar;
	}
	//=================================================================
	public boolean isMonochrome()
	//=================================================================
	{
			return result.isMonochrome;
	}
	//=================================================================
	public int getNumberOfPeaks()
	//=================================================================
	{
		return result.numberOfPeaks;
	}
	//=================================================================
	public int getNumberOfValidPeaks()
	//=================================================================
	{
		return result.numberOfValidPeaks;
	}
	//=================================================================
	public double getPeakCoverage()
	//=================================================================
	{
		return result.peakCoverage;
	}
	
	//=================================================================
	private void shoReport()
	//=================================================================
	{
	  /*	
	  logit("Mean=" + mean + " Q1=" + quart1 + "  Median=" + quart2 + "  Q3=" + quart3 +  "  Mn=" + minimum + " Mx=" + maximum);
	  logit("Slope threshold =  " + (int)Math.toDegrees(Math.atan(slopeThreshold)) + " degrees");
	  for(int i=0;i<peakList.size();i++)
	  {
		  double ld = Math.toDegrees(Math.atan(peakList.get(i).halfWidthSlopeLeft));
		  double rd = Math.toDegrees(Math.atan(peakList.get(i).halfWidthSlopeRight));
		  //
		  logit("=====================");
		  logit(" XCoord      : Left=" + peakList.get(i).MinX + " Top=" + peakList.get(i).PeakX + " Right=" + peakList.get(i).MaxX);
		  logit(" HEIGTH      : Left=" + peakList.get(i).leftPeakHeigth + " Right=" + peakList.get(i).rightPeakHeigth + "\n" + 
				" WIDTH       : Left=" + peakList.get(i).leftPeakWidth + " Right=" + peakList.get(i).rightPeakWidth + "\n" +
				" MidHeigth   : Left=" + smoothHisto[peakList.get(i).leftMidPeakWidthIdx] + " Right=" + smoothHisto[peakList.get(i).rightMidPeakWidthIdx] + "\n" +
				" MidLSlope   : left=" + (int)ld + " Right=" + (int)rd + " degrees\n" +
				" ValidHeigth : Left=" + peakList.get(i).leftHeigthIsValid + "  Right=" + peakList.get(i).rightHeigthIsValid + "\n" +
				" ValidSlope  : Left=" + peakList.get(i).leftSlopeIsValid + "  Right=" + peakList.get(i).rightSlopeIsValid  
					         );
	  }
	  */
      do_log(1,"RESULT [nPeaks="+result.numberOfPeaks +"] [nValidPeaks=" + result.numberOfValidPeaks + "] [Coverage=" + String.format("%5.2f",(result.peakCoverage*100)) + "%] [Grayscale=" + result.isGrayScale +"] [monochrome=" + result.isMonochrome + "]");
	}
	
	

	//=================================================================
	//=================================================================
	//  ONLY FOR DEVELOPMENT PURPOSES
    // this routine creates a text file which can be loaded in Excel to view the histogram
	
	
	/*
	private boolean isValidPeakSlope(int idx)
	{
		for(int i=0;i<peakList.size();i++)
		{
			if( (idx == peakList.get(i).PeakX ) && ( peakList.get(i).leftSlopeIsValid || peakList.get(i).rightSlopeIsValid ) ) return true;
		}
		return false;
	}
	
	
	private boolean isHalfWidth(int idx)
	{
		for(int i=0;i<peakList.size();i++)
		{
			if( idx == peakList.get(i).leftMidPeakWidthIdx  )  return true;
			if( idx == peakList.get(i).rightMidPeakWidthIdx  )  return true;
		}
		return false;
	}
	private boolean isValidPeakHeight(int idx)
	{
		for(int i=0;i<peakList.size();i++)
		{
			if( (idx == peakList.get(i).PeakX ) && ( peakList.get(i).leftHeigthIsValid || peakList.get(i).rightHeigthIsValid ) ) return true;
		}
		return false;
	}
	private void dump()
	{
        gpPrintStream p = new gpPrintStream("c:\\temp\\junk2.txt", "ASCII");
        for(int i=0;i<(tripleHisto.length/3)+10;i++)
        {
        	int idx = (tripleHisto.length / 3) + i - 5;
        	double dd=0;
        	if ( isHalfWidth(idx) ) {
        		dd= 0 - (maximum / 25);
        	}
        	if ( isPeak (idx)  ) {
        		 dd = 0 - (maximum / 10);
        		 if ( isValidPeakHeight (idx)  ) dd = 0 - (maximum / 5);
        		 if ( isValidPeakSlope (idx)  ) dd = 0- (maximum/5);
        		 if ( isValidPeak (idx)  ) dd = 0 - maximum;
         		
        	}
         	p.println("" + idx + "|" + String.format("%10.5f",tripleHisto[idx]) + "|" + String.format("%10.5f",smoothHisto[idx]) + "|" + String.format("%10.5f",dd));
        }
        p.close();
	}
	*/
	
}

