package imageProcessing;

import logger.logLiason;
import generalImagePurpose.cmcBulkImageRoutines;
import generalImagePurpose.cmcImageRoutines;
import generalImagePurpose.gpIntArrayFileIO;
import cbrTekStraktorModel.cmcProcSettings;

public class cmcProcDetermineThreshold {
	
	cmcProcSettings xMSet=null;
	cmcImageRoutines irout=null;
	cmcBulkImageRoutines ibulk=null;
    logLiason logger=null;
    
	private int ONDERGRENS = 80;
	private int BOVENGRENS = 180;
	private int STEP = 5;
	private int optimalThreshold=-1;

	String FPIXELDUMPNAME = null;
	
	
	class optimum
	{
		int threshold;
		int componentCount;
		int characterCount;
		int KMeansK;
		boolean isValid;
		optimum(int t)
		{
			threshold=t;
			isValid=false;
			KMeansK = 4;
		}
	}
	optimum[] ar_opt = new optimum[256];
	private boolean isOTSU=false;

	
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
	
	public boolean didWeSettleForOtsu()
	{
		return isOTSU;
	}
	//-----------------------------------------------------------------------
	public cmcProcDetermineThreshold(int iw ,  int ih , int[] pixels  , int[] workPixels , cmcProcSettings is  , logLiason ilog)
	//-----------------------------------------------------------------------
	{
	  //
	  xMSet = is;
	  logger=ilog;
	  irout = new cmcImageRoutines(logger);
	  ibulk = new cmcBulkImageRoutines(xMSet,logger);
	  FPIXELDUMPNAME = xMSet.getTempDumpFileName();
	  
	  // bereken de OTSU waarde
	  int otsu = irout.otsuTreshold(pixels);
	  optimalThreshold=otsu;
	  
	  // zet de onder en bovengrens
	  ONDERGRENS = otsu - 20;
	  BOVENGRENS = otsu + 40;
	  
      // loop doorheen aan aantal thresholds en bepaal minste aantal connected comps
	  viaMethodOne(iw,ih,pixels,workPixels,otsu);  
	  
	  // indien geen optimum neem dan den OTSU
	  if( optimalThreshold < 0 ) {
		do_log(1,"Optimal threshold coincides with upper or lower boundary. OTSU will be used");
	    optimalThreshold = otsu;
	  }
	  else {
		  isOTSU=false;
	  }
	  do_log(1,"OPTIMAL threshold=" + getOptimalThresholdValue() + "  KMEANS-K=" + getOptimalKMeansKValue());
	}
	
	//-----------------------------------------------------------------------
	public int getOptimalThresholdValue()
	//-----------------------------------------------------------------------
	{
		return optimalThreshold;
	}
	
	//-----------------------------------------------------------------------
	public int getOptimalKMeansKValue()
	//-----------------------------------------------------------------------
	{
		return ar_opt[optimalThreshold].KMeansK;
	}
	
	//-----------------------------------------------------------------------
	private void viaMethodOne(int iw , int ih , int[] srcPixels , int[] workPixels , int otsu)
	//-----------------------------------------------------------------------
	{
		
		  for(int i=0;i<256;i++) ar_opt[i] = new optimum(i);
		  
		  
		  // enables to write/read the pixels to file
		  gpIntArrayFileIO swapper = new gpIntArrayFileIO(logger);
		  swapper.writeIntArrayToFile(FPIXELDUMPNAME, srcPixels);
		  
		  // loop doorheen een aantal thresholds
	      for(int i=0;i<=256;i++)
		  {
			int threshold = i * STEP;
			if( i == 256 ) threshold = otsu;
			
			if( ((threshold < ONDERGRENS) || (threshold > BOVENGRENS)) && (threshold != otsu)  ) continue;
			
			// breng de grayscale terug in memory
			srcPixels = null; // for GC
			srcPixels = swapper.readIntArrayFromFile(FPIXELDUMPNAME);
			
			// grayscale naar zwartwit
			workPixels = null;
			workPixels = ibulk.binarize(srcPixels, threshold);
			
			// workPixels bevat nu de zwartwit image, gebruik srcPixel array daarom als werkbuffer bij de CC
			do_log(1,"Testing threshold[" + threshold + "] " );
			cmcProcConnectedComponent cc = new cmcProcConnectedComponent(xMSet,logger);
	        if( cc.doit( iw , ih , workPixels , srcPixels , true , -1) == false ) break;
	        ar_opt[ threshold ].isValid = true;
			ar_opt[ threshold ].componentCount = cc.getComponentCount();
			ar_opt[ threshold ].characterCount = cc.getOptimalCharacterCount();
			ar_opt[ threshold ].KMeansK = cc.getOptimalKMeansK();
			
			//System.out.println("THRESHOLD [" + threshold + "] " + ar_opt[threshold].characterCount );
		  }
		  //RESTORE
	      srcPixels = null; // for GC
		  srcPixels = swapper.readIntArrayFromFile(FPIXELDUMPNAME);
	
		  do_log(1,"DEBUG optimal=" + optimalThreshold + "    otsu=" + otsu);
		
		  // Zoek het minimaal aantal componenten (anders teveel noise)
		  // en gebruik de means met max componenten
		  int idx1=-1;
		  int idx2=-1;
		  int minimum=0;
		  int idx=-1;
		  /*
		  for(int i=0;i<256;i++)
		  {
			  if( ar_opt[i].isValid == false) continue;
			  
			  if( idx == -1 ) {
				  minimum = ar_opt[i].componentCount;
				  idx=i;
			  }
			  if( (ar_opt[i].componentCount < minimum) && (ar_opt[i].characterCount > 0)  ) {
				  minimum = ar_opt[i].componentCount;
				  idx = i;
			  }
		  }
		  */
		  idx1=idx;
		  optimalThreshold=idx;
		  //
		  if( idx > 0 ) {
		   do_log(1,"Minimal number of components occurs for threshold [" + idx + "] and k = [" + ar_opt[idx].KMeansK + "]");
		  }
		  // minste aantal componenten komt overeen met een onder of bovengrens, kies dan gewoon de threshold met maximaal aantal karakters 
		  if( (optimalThreshold <= ONDERGRENS) || (optimalThreshold>=BOVENGRENS) ) {
			  do_log(1,"Threshold selected [" + idx + "] is an upper or lower boundary, so we will switch to MAX CHARS");
			  int maximum=0;
			  idx=-1;
			  for(int i=0;i<256;i++)
			  {
				  if( ar_opt[i].isValid == false) continue;
				  if( idx == -1 ) {
					  maximum = ar_opt[i].characterCount;
					  idx=i;
				  }
				  if( ar_opt[i].characterCount >= maximum) {   // neem laatste, dus meer naar de witte kand
					  maximum = ar_opt[i].characterCount;
					  idx = i;
				  }
			  }
			  idx2=idx;
			  optimalThreshold = idx;
			  do_log(1,"Maximal number of chars occurs for threshold [" + idx + "] and k = [" + ar_opt[idx].KMeansK + "]");
		  }
		  //
		  if( (optimalThreshold <= ONDERGRENS) || (optimalThreshold>=BOVENGRENS) ) {
			  do_log(1,"Threshold selected [" + idx + "] is an upper or lower boundary, so we will switch to OTSU [" + otsu +"]");
			  if( (ar_opt[otsu].isValid == false) || (ar_opt[otsu].characterCount<0) ) {
				  do_log(1,"OTSU has no optimum. So picking an arbitrary value");
				  if( idx1 > 0) {
					  optimalThreshold = idx;
				  }
				  else {
					  if( idx2 > 0) optimalThreshold=idx2; else optimalThreshold=127;
				  }
			  }
			  else optimalThreshold=otsu;
		  }
		 
		  do_log(1,"DetermineThredshold =>" + optimalThreshold);
		 
	}
	
	
	
		
}
