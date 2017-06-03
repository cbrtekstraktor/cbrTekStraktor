package imageProcessing;

import java.io.PrintWriter;
import java.io.StringWriter;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcSettings;


public class cmcProcConnectedComponent {

	logLiason logger=null;
	
    private int K64 = 1024*64;
	private int MAXMERGE = K64*4;
	private int merger[][] = new int[MAXMERGE][2];
    private int curlabel=0; 
    private int imgWidth=-1;
    private int imgHeigth=-1;
   
    
    private int optimalCharacterCount = 0;
    private int optimalKMeansK=3;
    private int preferredKMeansK=-1;
        
    int OUTLIER_TOOLARGE = -10;
    int OUTLIER_TOOSMALL = -20;
    
    cmcConnectedComponentBoundary eco[] = null;
    cmcProcParameters cParam=null;
    
    class optimum
    {
    	boolean isValid;
    	int kmeansk;
    	int characterCount;
    	int distance;
    	optimum(int k , int d , int aantal)
    	{
    		isValid=false;
    		distance=d;
    		kmeansk=k;
    		characterCount=aantal;
    	}
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
    
	//------------------------------------------------------------
	public cmcProcConnectedComponent(cmcProcSettings im , logLiason ilog)
	//------------------------------------------------------------
	{
		logger=ilog;
		preferredKMeansK=-1;
		cParam = new cmcProcParameters(im);
	}

	//------------------------------------------------------------
	public cmcConnectedComponentBoundary[] getCCBoundaryArray()
	//------------------------------------------------------------
	{
		return eco;
	}
	
	//------------------------------------------------------------
	private boolean checkAvailableHeapSpace(int required)
	//------------------------------------------------------------
	{
        //  http://javarevisited.blogspot.com/2012/01/find-max-free-total-memory-in-java.html#ixzz3LmegnEVg
		long freeMemory = Runtime.getRuntime().freeMemory();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long maxMemory = Runtime.getRuntime().maxMemory();
        return true;
	}
    
	//------------------------------------------------------------
	public boolean doit(int iw , int ih , int[] pixels , int[] workPixels , boolean trialRun , int iPref)
	//------------------------------------------------------------
	{
		preferredKMeansK=iPref;
		imgWidth = iw; 
        imgHeigth = ih;
        //
        doConnectedComponent ( pixels , workPixels );   // maakt de Connected Component Label matrix
        //
        doGatherStats(workPixels);  // Clustering van de connected components
        //
        if( trialRun ) return true;
        //
        postProcess();
        return true;
	}
	
	
	//------------------------------------------------------------
	public int getComponentCount()
	//------------------------------------------------------------
	{
		return curlabel;
	}

	// connected component label algo
	// ik heb de pass 2 (ttz. equiv) geintegreerd in de 1ste pass door te kijken of er meer dan & equivalent labels zijn
	// indien dit het geval is wordt onmiddellijk de resterende hoogste labels gesubstitueerd vanaf het moment dat
	// die andere label voor het eerst optrad.
	//------------------------------------------------------------
	private void doConnectedComponent(int pixels[] , int workPixels[] )
	//------------------------------------------------------------
	{
        int start[] = new int[MAXMERGE];
      
        int equi[] = new int[8];  // werkbuffertje 
        int k = -1;   // current index 
        int z = 0;    // index van de randpixel 
        // 
        for(int i=0;i<(imgWidth*imgHeigth);i++) workPixels[i]=0; 
        curlabel=0; 
        for(int i=0;i<MAXMERGE;i++) merger[i][0] = -1;    
        //
        // 
        for(int y=0;y<imgHeigth;y++)   // hoogte 
        { 
                //if( (y % 10) == 0 ) System.out.println("" + (ih-y) + " ");
                for(int x=0;x<imgWidth;x++)  // breedte 
                { 
                        k = x + (y * imgWidth);  // current 
                        // Achtergrond 
                        if( (pixels[k] & 0x00ffffff) == 0x00ffffff )  continue;  // wit = xx.FF.FF.FF 
                        // loop doorheen de omliggende en recupeer de laatste label 
                        for(int pp=0;pp<8;pp++) 
                        { 
                                equi[pp]=-1; 
                                switch ( pp ) 
                                { 
                                case 0 :  { if( x != 0) z = k - imgWidth - 1; else z = -1; break; }              // NoordWest 
                                case 1 :  { z = k - imgWidth; break; }                                           // Noord 
                                case 2 :  { if( x != (imgWidth-1) ) z = k - imgWidth + 1; else z = -1; break; }  // NoordOost 
                                case 3 :  { if( x != 0) z = k - 1; else z = -1; break; }                         // West 
                                case 4 :  { if( x != (imgWidth-1) ) z = k + 1; else z = -1; break; }             // Oost 
                                case 5 :  { if( x != 0) z = k + imgWidth - 1; else z = -1; break; }              // ZuidWest 
                                case 6 :  { z = k + imgWidth; break; }                                           // Zuid 
                                case 7 :  { if( x != (imgWidth-1) ) z = k + imgWidth + 1; else z = -1; break; }  // ZuidOost 
                                default :  { do_error("Fout bij cc"); break; } 
                                } 
                                if (  (z < 0) || ( z >=  (imgWidth*imgHeigth)) )  continue;   // out of bounds 
                                // 
                                if ( pixels[z] != pixels [k] ) continue;   //  randpixel verschilt in intensiteit 
                                // een match 
                                equi[pp] = workPixels[z];  // neem de label over 
                        } 
                        
                        // zoek de laagste waarde op equi, dat wordt nu de label van de huidige 
                        int label = -1; 
                        for(int pp=0;pp<8;pp++) { 
                                if( equi[pp] <= 0 ) continue;   // coco wordt met 0 geinit 
                                if( label == -1 ) label = equi[pp]; 
                                if( equi[pp] < label ) label = equi[pp];   
                        } 
                        if( label == -1) { 
                                label = curlabel;
                                start[curlabel] = k;
                                curlabel++; 
                                if( curlabel >= start.length ) {
                                	do_error("cmcProcConnectedComponent) MAXMERGE level reached - Applications will probable fail - increase level");
                                	curlabel--;
                                }
                        }
                        // zijn er equiv?  Indien ja vervang door te starten vanaf positie van die equi
                        else {
                                   for(int pp=0;pp<8;pp++) {
                                                   if( (equi[pp] > 0) && (equi[pp] != label)) {
                                                                  // vervang
                                                                  for( int uu=start[equi[pp]];uu<k;uu++) {
                                                                                  if( workPixels[uu] <= 0) continue;
                                                                                  if( workPixels[uu] == equi[pp] ) workPixels[uu] = label;
                                                                  }
                                                   }
                                   }
                        }
                        // de de connected component label
                        workPixels[k]=label;
                } 
        } 
        //
        int ccc=0;
        for(int i=0;i<workPixels.length;i++) if( workPixels[i] > 0 ) ccc++;
        if( ccc == 0 ) {
        	do_error("!! Strange no connected components found");
        }
        
	}
	
	// maakt de Connect Component Boundary Matirx
	// exclude van een aantal componnenten
	// K-MEANS clustering, indien k=4    0=NOISE 1=LETTER 2=BUBBLE 3=FRAME, indien K=5 0=NOISE    4=FRAME, daartussen onbekend
	// kijken of een cluster is met ongeveer 500 elementen
	//------------------------------------------------------------
	private void doGatherStats(int[] workPixels)
	//------------------------------------------------------------
	{
	    eco = new cmcConnectedComponentBoundary[curlabel];
        for( int y=0;y<curlabel;y++) eco[y] = new cmcConnectedComponentBoundary();
      
        int curco=-1;
        int k=0;
        for(int y=0;y<imgHeigth;y++)
        {
                   for(int x=0;x<imgWidth;x++)
                   {
                                    k = x + (y * imgWidth);  // current 
                                    if ( workPixels[k] <= 0 ) continue;
                                    curco = workPixels[k];
                                    eco[ curco ].counter++;
                                    if( eco[ curco ].MinX == -1 ) {
                                    	            eco[ curco ].isValid = true;
                                                    eco[ curco ].MinX = x;
                                                    eco[ curco ].MaxX = x;
                                                    eco[ curco ].MinY = y;
                                                    eco[ curco ].MaxY = y;
                                                    eco[ curco ].connectedcomponentlabel = curco;
                                                    continue;
                                    }
                                    if( eco[curco].MaxX < x) eco[curco].MaxX=x;
                                    if( eco[curco].MinX > x) eco[curco].MinX=x;
                                    if( eco[curco].MaxY < y) eco[curco].MaxY=y;
                                    if( eco[curco].MinY > y) eco[curco].MinY=y;
                   }
        }
        // verwerken
        int hh=0;
        int idx=-1;
        int hoogte=-1;
        int breedte=-1;
        for(int i=0;i<curlabel;i++)
        {
        	if( eco[i].counter <= 0) {  // invalideer dit is een label zonder onderliggende
        		eco[i].isValid = false;
        		continue;
        	}
        	hoogte  = eco[i].MaxY - eco[i].MinY + 1;
        	breedte = eco[i].MaxX - eco[i].MinX + 1;
        	eco[i].relBreedte =  (double)breedte / (double)imgWidth;
        	eco[i].relHoogte =  (double)hoogte / (double)imgHeigth;
        	
        	//System.out.println("" + i + "," + eco[i].counter + "," + eco[i].MinX + "," + eco[i].MinY + "," + (eco[i].MaxX - eco[i].MinX) + ","  + (eco[i].MaxY - eco[i].MinY) + "," + eco[i].relBreedte + "," + eco[i].relHoogte);
        	
        	if( hoogte > hh) {
        		hh = hoogte;
        		idx=i;
        	}
        }

        // EXCLUDE section I
        // componenten met een hoogte >= 50% eruit gooien, dergelijke hoogtes trekken de K-Means scheef
        for(int i=0;i<curlabel;i++)
        {
        	if( eco[i].isValid == false) continue;
        	if( eco[i].relHoogte >= (double)0.50 ) {
        		eco[i].isValid = false;
        		eco[i].cluster = OUTLIER_TOOLARGE;
        	   do_log(1,"Excluding TOO LARGE [" + i +"] CNTR="+ eco[i].counter + " (" + eco[i].MinX + "," + eco[i].MinY + ") B=" + (eco[i].MaxX - eco[i].MinX) + " H="  + (eco[i].MaxY - eco[i].MinY) + " ->" + eco[i].relHoogte + "%");
        	}
        }
        // EXCLUDE section II 
        // pixel < 6 eruit want minimale hogte om een tekst te kunnen lezen - zie artikel
        for(int i=0;i<curlabel;i++)
        {
        	if( eco[i].isValid == false ) continue;
        	if( (eco[i].MaxY - eco[i].MinY + 1) < 6 ) {
        	   eco[i].isValid = false;  
        	   eco[i].cluster = OUTLIER_TOOSMALL;
        	   //System.out.println("Excluding TOO SMALL [" + i +"] CNTR="+ eco[i].counter + " (" + eco[i].MinX + "," + eco[i].MinY + ") B=" + (eco[i].MaxX - eco[i].MinX) + " H="  + (eco[i].MaxY - eco[i].MinY) + " ->" + eco[i].relHoogte + "%");
        	}
        }
       
        
        // K-MEANS Clustering
        // Kijken of er een cluster is met ongeveer 500 elementen
        optimalCharacterCount=-1;
        for(int trial=3;trial<6;trial++)
        {
        	
        	// DEBUG
        	//if( trial != 4 ) continue;
        	
        	if( preferredKMeansK != -1 ) {  // op die wijze kiezen
        		trial = preferredKMeansK;
        	}
        	// KMEANS op eco
            // Maak een set van relevante data
            int kmAantal=0;
            for(int i=0;i<curlabel;i++)
            {
            	if( eco[i].isValid == false) continue;
            	kmAantal++;
            }
            int[] kmset = new int[kmAantal];
            kmAantal=0;
            for(int i=0;i<eco.length;i++)
            {
            	if( eco[i].isValid == false) continue;
            	hoogte  = eco[i].MaxY - eco[i].MinY + 1;
            	kmset[kmAantal] = hoogte;
            	kmAantal++;
            }
            cmcProcKMeans km = new cmcProcKMeans();
            km.populateSingleDimensionSet( trial , kmset);
            km.doit();
            //
            kmAantal=0;
            for(int i=0;i<eco.length;i++)
            {
            	if( eco[i].isValid == false) {
            		eco[i].cluster = -1;
            		continue;
            	}
            	eco[i].cluster = km.getClusterViaIdx(kmAantal);
            	kmAantal++;
            }
            //
            int[] kcounts = new int[trial];
            for(int i=0;i<trial;i++)
            {
            	kcounts[i] =km.getNumberOfElementsPerCentroidViaIdx(i);
            }
            //
            // Results
            do_log(1,"K-MEANS [K=" + trial + "]  -> ");
            for(int i=0;i<trial;i++) do_log( 1 , "[" + i + "=" + kcounts[i] +"] ");
            
            
            // Gebruik de volgende RULE om de optimalen te bepalen voor een LETTER
            //  aantal elementen in de clsuter moet > 100  en  < 1000
            //  aantal elementen in de cluster die het dichtst bij 500 komt   (HYPOTHESE : gemiddelde aantal letters op een blad)
            optimum[] ar_opt = new optimum[trial];
            for(int i=0;i<trial;i++)
            {
               int dist = Math.abs( cParam.MEAN_CHAR_COUNT - kcounts[i] ); //if( dist < 0 ) dist = 0 - dist;
               ar_opt[i] = new optimum(trial,dist,kcounts[i]);
               if( (kcounts[i] > cParam.MAX_CHAR_COUNT) || (kcounts[i] < cParam.MIN_CHAR_COUNT) )  { ar_opt[i].isValid = false; continue; }
               ar_opt[i].isValid = true;
            }
            idx=-1;
            int mini=K64;
            for(int i=1;i<trial;i++)   // laat de 0 over dat is de NOISE cluster 
            {
            	if( ar_opt[i].isValid == false ) continue;
            	if( mini > ar_opt[i].distance ) {
            		idx=i;
            		mini = ar_opt[i].distance;
            	}
            }
            if( idx < 0 ) {
            	 do_log(1,"No optimum found for k=" + trial);
            	 if( preferredKMeansK != -1 ) break;  // Indien geen trial eruit
            	continue;
            }
            
            do_log(1,"OPTIMAL " + idx + " " + ar_opt[idx].characterCount);
            // Optimalen bijhouden
            if( optimalCharacterCount < 0 ) {
            	optimalCharacterCount = ar_opt[idx].characterCount;
            	optimalKMeansK=trial;
            }
            if( optimalCharacterCount < ar_opt[idx].characterCount ) {
            	optimalCharacterCount = ar_opt[idx].characterCount;
            	optimalKMeansK=trial;
            }
             
            if( preferredKMeansK != -1 ) break;
            do_error("VERSIE - PREMPTEN van de analyse");  // Waarom steeds een break ??
            break;  // ? waarom steeds een break   
        }
        //System.out.println("GLOBAL OPTIMAL k-means K=" + optimalKMeansK + " chars=" + optimalCharacterCount );
        
	}
	
	//------------------------------------------------------------
	private void postProcess()
	//------------------------------------------------------------
	{
		try {
		 int aantal=0;
		 int oriaant=eco.length;
		 int maxcluster=0;
		 // de grote kader terug inb rengen
		 for(int i=0;i<eco.length;i++)
         {
			 if( eco[i].isValid == false ) {
				 if( eco[i].cluster == OUTLIER_TOOLARGE ) {
				  eco[i].isValid = true;
				  do_log(1,"Re-applying [" + i +"] CNTR="+ eco[i].counter + " (" + eco[i].MinX + "," + eco[i].MinY + ") B=" + (eco[i].MaxX - eco[i].MinX) + " H="  + (eco[i].MaxY - eco[i].MinY) + " ->" + eco[i].relHoogte + "%");
				 }
				 else {
				   eco[i].cluster = -1; 
				   continue;
				 } 
			 }
			 aantal++;
			 if( eco[i].cluster > maxcluster ) maxcluster = eco[i].cluster;
         }
		 //
		 if( oriaant == aantal ) return;
		 //
		 cmcConnectedComponentBoundary compressed[] = new cmcConnectedComponentBoundary[aantal];
		 // 
		 int com=0;
		 for(int i=0;i<eco.length;i++)
         {
           	    if( eco[i].isValid == false ) continue;
           	    if( eco[i].cluster == OUTLIER_TOOLARGE ) eco[i].cluster = maxcluster;
                compressed[com] = new cmcConnectedComponentBoundary();
                compressed[com].isValid = eco[i].isValid;
                compressed[com].cluster = eco[i].cluster;
                compressed[com].originalCluster = eco[i].cluster;
                compressed[com].counter = eco[i].counter;
                compressed[com].MaxX = eco[i].MaxX;
                compressed[com].MaxY = eco[i].MaxY;
                compressed[com].MinX = eco[i].MinX;
                compressed[com].MinY = eco[i].MinY;
                compressed[com].relBreedte = eco[i].relBreedte;
                compressed[com].relHoogte = eco[i].relHoogte;
                compressed[com].connectedcomponentlabel = eco[i].connectedcomponentlabel;
                com++;
         }
		 eco = null;
		 eco = compressed;
		 do_log(1,"Compressed " + oriaant + " -> " + eco.length );
	  }
		catch( Exception e) {
			do_error(LogStackTrace(e));
		}
	}
	
	//------------------------------------------------------------
	public int getOptimalCharacterCount()
	//------------------------------------------------------------
	{
		return optimalCharacterCount;
	}
	
	//------------------------------------------------------------
	public int getOptimalKMeansK()
	//------------------------------------------------------------
	{
		return optimalKMeansK;
	}
	
	//
	//---------------------------------------------------------------------------------
	private String LogStackTrace(Exception e)
	//---------------------------------------------------------------------------------
	{
			      try {
			        StringWriter sw = new StringWriter();
			        PrintWriter pw = new PrintWriter(sw);
			        e.printStackTrace(pw);
			        return sw.toString();
			      }
			      catch(Exception e2) {
			    	e.printStackTrace();
			        return "";
			      }
	} 

}


