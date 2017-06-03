package imageProcessing;


import java.util.ArrayList;

import logger.logLiason;
import textProcessing.cmcTextDump;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.cmcStopWatch;
import cbrTekStraktorModel.comicPage;
import dao.cmcBookMetaDataDAO;
import dao.cmcStatDAO;
import drawing.cmcGraphPageObject;
import generalImagePurpose.gpIntArrayFileIO;
import generalImagePurpose.gpLoadImageInBuffer;
import generalStatPurpose.gpFrequencyDistributionStatFunctions;
import generalStatPurpose.gpListStatFunctions;
import generalpurpose.gpInterrupt;


public class cmcProcConnectedComponentPostProcessor {
	
			
	cmcProcSettings xMSet=null;
	cmcConnectedComponentHistogram cchist = null;
	cmcProcParameters cParam=null;
	cmcBookMetaDataDAO cmeta = null;
	cmcConnectedComponentBoundary[] ccb_ar = null;
	cmcConnectedTextComponentBundel[] tcc_ar=null;
	comicPage cPage=null;
	logLiason logger=null;
	gpInterrupt irq = null;
	
    //	een helperclass voor multi respons functies
	class multret {
		double density;
		double horizontalVariance;
		double verticalVariance;
		boolean isLetter;
		void reset()
		{
			density=-1;
			horizontalVariance=-1;
			verticalVariance=-1;
			isLetter=false;
		}
	}
	//
	ArrayList<ConnectedComponentCluster> cococulst = null;
	private int MAX_PROXIMITY = 1024*4;
	private int[] ar_proximity = new int[MAX_PROXIMITY];
	private int[] ClassificationGuess = new int[2];
    private int LETTER=-1;
	private int FRAME=-1;
	private cmcProcEnums.ProximityTolerance proxtol = null;

	private long LoadTimeLetters=-1;
	private long LoadTimeParagraphs=-1;
	private boolean isOK = true;
	
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
  	private void TRACE(String s)
  	//------------------------------------------------------------
  	{
       	//do_error(xMSet.xU.prntStandardDateTime(System.currentTimeMillis()) + " " + s);	
  	}
  	
	//------------------------------------------------------------
	public cmcConnectedTextComponentBundel[] getTextBundle()
	//------------------------------------------------------------
	{
		return tcc_ar;
	}
	
	//------------------------------------------------------------
	private void Rapporteer(String sIn)
	//------------------------------------------------------------
	{
		cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
		dao.AppendSingleLine(sIn);
		do_log(5,sIn);
	}
	
	//------------------------------------------------------------
	public boolean getOK()
	//------------------------------------------------------------
	{
		return isOK;
	}
	
	//------------------------------------------------------------
	public cmcProcConnectedComponentPostProcessor(cmcConnectedComponentBoundary[] ia , comicPage cp , cmcProcSettings is , logLiason ilog)
	//------------------------------------------------------------
	{
		isOK = true;
		xMSet = is;
		logger=ilog;
		irq = new gpInterrupt(xMSet.getInterruptFileName());
		ccb_ar=ia;
		cPage = cp;
		isOK = mainBody();
	}

	//------------------------------------------------------------
	private boolean mainBody()
	//------------------------------------------------------------
	{
		if( irq.gotInterrupt() )  return false;
		
		proxtol = xMSet.getProxTol();
		do_log(5,"Proximity Tolerance [" + proxtol + "]");
		//
		LoadTimeLetters=-1;
		LoadTimeParagraphs=-1;
		cmcStopWatch w1 = new cmcStopWatch("Letters");
		//
		for(int i=0;i<ClassificationGuess.length;i++) ClassificationGuess[i]=-1;
		//
		cmeta = new cmcBookMetaDataDAO( xMSet , xMSet.getOrigImageLongFileName() , null , logger);
		if( cmeta != null ) {
		 do_log(5,"Loaded cmeta -> "  + cmeta.getClusterClassificationTypeString());
		}
		//
		cParam = new cmcProcParameters(xMSet);
		//
		if( irq.gotInterrupt() )  return false;
		//
		// Backup : de workpixel array bevat de image met op iedere pixel de connected component label (ipv. de luminantie)
		gpIntArrayFileIO  iio = new gpIntArrayFileIO(logger);
		iio.writeIntArrayToFile( xMSet.getConnectedComponentDumpFileName() , cPage.cmcImg.workPixels );
	    //
		// histogram  - ccb_ar is de compressed versie van de connectec components, ttz. hoogte 0 zijn eruit
		cPage.cclHstgrm = new cmcConnectedComponentHistogram(ccb_ar , cPage.getPayloadHeigth() , xMSet.getCodePageString());
		cPage.cclHstgrm.dumpHisto(xMSet.getXMLStatFileName());
		//
		if( irq.gotInterrupt() )  return false;
		// zoek naar mogelijke letters in de connected components
		scanForLettersInConnectedComponentList();
		zoekNaarLetterCluster();
		zoekClassificationIDs();
		//
		if( irq.gotInterrupt() )  return false;
		//
		raporteerOriginalConComClusters(false);
		//
		// zoek naar potentiele letters
		zoekEldersNaarLetters();
		undoIsLetterOvercompensatie();    // oorspronkeljke LETTER doch niet potentieelLetter -> kijk of ze omgeven zijn
		w1.stopChrono();
		LoadTimeLetters=w1.getDurationNanoSec();
		cmcStopWatch w2 = new cmcStopWatch("paragraphs");
	    //
		if( irq.gotInterrupt() )  return false;
		// verwijder de frames omsloten door een andere frame
		growFramesAndBubbles();
		//
		// letters die bij elkaar horen clusteren
		collateLetterBoxes(false);
		//
		// doe de vertikale en horizontale wit histogrammen
		identificeerTekstParagrafen(false);
		//
		// niet tekstparagrafen met veel letters -> tekst
		overruleGraph();
		//
		if( irq.gotInterrupt() )  return false;
		// Re-assess the FRAMES
		reAssessFrames();
		// Rapporteer op de clusters (naar einde verplaatst om de moves te tonen)
		dumpControlePlaatjes(cPage.cmcImg.workPixels);
		raporteerOriginalConComClusters(true);
	    paragraphReport();
	    //
	    if( irq.gotInterrupt() )  return false;
	    // Maak de plaatjes
		genereerOutput(cPage.cmcImg.workPixels);
		// Graphical Editor Dump
		dumpComponents();
		w2.stopChrono();
		LoadTimeParagraphs=w2.getDurationNanoSec();
		//
		finalWarning();
		//
		//Rapporteer("</ComicPage>");
		//app.CloseAppendFile();
        return true;
	}
	
	//------------------------------------------------------------
	private void finalWarning()
	//------------------------------------------------------------
	{
		cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
		dao.finalWarning(ClassificationGuess, LETTER);
	}
	
	//------------------------------------------------------------
	private void zoekNaarLetterCluster()
	//------------------------------------------------------------
	{
	  TRACE("zoeknaarletters");
	  // Stap A
	  // we bepalen van iedere cluster een aantal explanatory variables : die genormaliseerd zijn
	  // Number Of Elements  DIV totaal aantal elementen
	  // Quartile1, Quartile3, Median en Mean van de hoogte van de elementen  DIV Payloadhoogte
	  // Number of paragraphs  DIV 	totaal aantal paragraphs
	  // Number of letters in de paragraphs DIV totaal aantal letters
	  // Q1,Q3, Median en Mean van de hoogte van de letters in de paragraphs DIV PayLoadHoogte
	  // Stap B : zoek naar LETTER cluster
	  // Indien geen corpus => de regels uit zoekClassification
	  // Indien een corpus => cosine similarity
	  
	  // opbouwen van de cluster lijst
	  int MAXLABELS=100;
	  int[] labels = new int[MAXLABELS];
      for(int i=0;i<MAXLABELS;i++) labels[i]=0;
      for(int i=0;i<ccb_ar.length;i++)
      {
      	if( ccb_ar[i].isValid == false) continue;
      	labels[ccb_ar[i].cluster]++;
      }
      cococulst = new ArrayList<ConnectedComponentCluster>();
  	  for(int i=0;i<labels.length;i++)
      {
    	  if( labels[i] <= 0 ) continue;
    	  ConnectedComponentCluster x = new ConnectedComponentCluster(i);
    	  x.aantalElementen = labels[i];
    	  cococulst.add(x);
      }
  	  // check
  	  if( cococulst.size() <= 0 ) {
  		  do_error("NO CLUSTERS FOUND in [" + ccb_ar.length + "] cocos");
  	  }
      labels=null;
      // bepaal aantal paragrafen en letters
      int sav=LETTER;
      boolean prevPotentialLetter[] = new boolean[ccb_ar.length];
      for(int i=0;i<ccb_ar.length;i++) prevPotentialLetter[i]=ccb_ar[i].isPotentialLetter;
      //
      for(int i=0;i<cococulst.size();i++)
      {
    	  LETTER=cococulst.get(i).clusterIdx;
    	  // 
    	  undoIsLetterOvercompensatie();   // letters in nabijheid van andere worden letters
    	  //
    	  collateLetterBoxes(true);
    	  cococulst.get(i).aantalParagrafen = tcc_ar.length;
    	  identificeerTekstParagrafen(true);
    	  cococulst.get(i).aantalTekstParagrafen = telTekstParagrafen();
    	  cococulst.get(i).aantalLetters = getNbrOfLettersInCluster(cococulst.get(i).clusterIdx);
    	  // Stat
    	  StatContainer ster = gatherLETTERStatistics();
    	  cococulst.get(i).MedianWidth      = ster.medCharWidth;
    	  cococulst.get(i).MedianHeight     = ster.medCharHeigth;
    	  cococulst.get(i).Quartile1Height  = ster.quartile1Heigth;
    	  cococulst.get(i).Quartile3Height  = ster.quartile3Heigth;
    	  cococulst.get(i).iqrHeigth        = ster.iqrHeigth;
    	  cococulst.get(i).Quartile1Width   = ster.quartile1Width;
    	  cococulst.get(i).Quartile3Width   = ster.quartile3Width;
    	  cococulst.get(i).iqrWidth         = ster.iqrWidth;
    	  cococulst.get(i).meanCharHeigth   = ster.meanCharHeigth;
     	  cococulst.get(i).meanCharWidth    = ster.meanCharWidth;
     	  cococulst.get(i).MinWidth         = ster.glbMinw;
    	  cococulst.get(i).MaxWidth         = ster.glbMaxw;
    	  cococulst.get(i).MinHeigth        = ster.glbMinh;
    	  cococulst.get(i).MaxHeigth        = ster.glbMaxh;
    	  //
    	  cococulst.get(i).normalizedMedianHeigth = (double)cococulst.get(i).MedianHeight / (double)cPage.getUncroppedHeigth();
    	  cococulst.get(i).normalizedMedianWidth  = (double)cococulst.get(i).MedianWidth / (double)cPage.getUncroppedWidth();
    	  //
    	  if( cococulst.get(i).aantalParagrafen > 0 ) {
    		  cococulst.get(i).tekstparagraafdensiteit =  (double)cococulst.get(i).aantalTekstParagrafen /  (double)cococulst.get(i).aantalParagrafen;
    	  }
    	  if( cococulst.get(i).aantalElementen > 0 ) {
    		  cococulst.get(i).letterdensiteit = (double)cococulst.get(i).aantalLetters /  (double)cococulst.get(i).aantalElementen;
    	  }
    	  //
    	  // reset (.. pas op einde bij voorkeur
    	  for(int j=0;j<ccb_ar.length;j++) {
    		  ccb_ar[j].bundel = -1;
    		  ccb_ar[j].isPotentialLetter = prevPotentialLetter[j];
    	  }
    	  
    	  
      }
      LETTER=sav;
      //
      
      // loggen
      for(int i=0;i<cococulst.size();i++)
      {
    	  do_log(5,"==> [Cluster=" + cococulst.get(i).clusterIdx + 
    			  "] [#elements=" + cococulst.get(i).aantalElementen + 
    			  "] [#paragraphs=" + cococulst.get(i).aantalParagrafen +
    			  "] [#tekstparagraphs=" + cococulst.get(i).aantalTekstParagrafen +
    			  "] [#Letters=" + cococulst.get(i).aantalLetters +
    			  "] [#LetterDensity=" +  prperc(cococulst.get(i).letterdensiteit) + 
    			  "] [#TextParagraphDensity=" +  prperc(cococulst.get(i).tekstparagraafdensiteit) +
    			  "] [Height Quartile1=" +  cococulst.get(i).Quartile1Height +
    			  "] [Height Median=" +  cococulst.get(i).MedianHeight +
    			  "] [Height Quartile3=" +  cococulst.get(i).Quartile3Height +
    			  "] [Width Median=" +  cococulst.get(i).MedianWidth +
    			  "] [Heigth ratio=" +  prperc(cococulst.get(i).normalizedMedianHeigth) +
    			  "]" );
      }
      
	}

	//------------------------------------------------------------
	private String prperc(double dd)
	//------------------------------------------------------------
	{
		double ret = dd * 100;
		return String.format("%5.2f", ret) + "%";
	}
	
	//------------------------------------------------------------
	private void zoekClassificationIDs()
	//------------------------------------------------------------
	{
		TRACE("zoekClassificationID");
		zoekClassificationIDMethodeEen();
		ClassificationGuess[0]=LETTER;
		//
		zoekClassificationIDMethodeTwee();
		ClassificationGuess[1]=LETTER;
		//
		do_log(5,"==========================================================================================");
		do_log(5,"Text cluster [MethodOne=" + ClassificationGuess[0] + "] [MethodTwo=" + ClassificationGuess[1] + "]" ) ;
		do_log(5,"==========================================================================================");
		// TODO - overrule met een commandline parameter
	    if( cmeta != null )
	    {
	    	int advised = -1;
	    	switch( cmeta.getClusterClassificationTypeRaw() )
	    	{
	    	case AUTOMATIC : break;
	    	case Cluster1 : { advised = 0; break; }
	    	case Cluster2 : { advised = 1; break; }
	    	case Cluster3 : { advised = 2; break; }
	    	case Cluster4 : { advised = 3; break; }
	    	case Cluster5 : { advised = 4; break; }
	    	default : break;
	    	}
	    	if( (advised != -1) && (advised != LETTER) ) {
	    		LETTER = advised;
	    		do_log(5,"==========================================================================================");
	    		do_log(1,"==> Text Cluster Index overruled to [" + LETTER + "]");
	    		do_log(5,"==========================================================================================");
	    	}
	    }
		
	    cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
	    dao.dumpClusterClassification(ClassificationGuess , LETTER , FRAME);

	    // finally, zet de tipes
	    fixTipesOnCoCoCuLst();
	}

	//------------------------------------------------------------
	private void fixTipesOnCoCoCuLst()
	//------------------------------------------------------------
	{
		for(int i=0;i<cococulst.size();i++)
		{
			if( cococulst.get(i).clusterIdx == LETTER ) cococulst.get(i).tipe = cmcProcEnums.ParagraphType.LETTER;
			if( cococulst.get(i).clusterIdx == FRAME ) cococulst.get(i).tipe = cmcProcEnums.ParagraphType.FRAME;
		}
	}
	
	//------------------------------------------------------------
	private void zoekClassificationIDMethodeTwee()
	//------------------------------------------------------------
	{
		TRACE("zoekClassificationIDMethodeTwee");
		// Neem gewoon de cluster met hoogste letterparagraafdensiteit
		double max=0;
		int minElem=640000;
		int max_idx=-1;
		int min_idx=-1;
		for(int i=0;i<cococulst.size();i++)
		{
			if( cococulst.get(i).tekstparagraafdensiteit > max ) {
				max_idx=i;
				max = cococulst.get(i).tekstparagraafdensiteit;
			}
			if( cococulst.get(i).aantalElementen < minElem) {
				min_idx=i;
				minElem = cococulst.get(i).aantalElementen;
			}
		}
		LETTER = max_idx;
		FRAME = min_idx;
		// het zou kunnen dat de tekstparagraafdensiteit 0 is, neem dan de letterdensiteit
		if( LETTER < 0 ) {
			max=0;
			max_idx=-1;
			for(int i=0;i<cococulst.size();i++)
			{
				if( cococulst.get(i).letterdensiteit > max ) {
					max_idx=i;
					max = cococulst.get(i).letterdensiteit;
				}
			}	
		}
		LETTER = max_idx;
		if( LETTER < 0 ) {
	     do_error("METHOD 2 : no letters or letterparagrpahs found" );
		}
		else {
		 do_log(5,"METHOD 2 : Classification indices :  FRAME=" + FRAME  + "  TEXT=" + LETTER );
		}
	}
	
	//------------------------------------------------------------
	private int getMeanClusterHeigth(int idx)
	//------------------------------------------------------------
	{
			double sum=0;
			double aant=0;
			for(int i=0;i<ccb_ar.length;i++)
			{
				if( ccb_ar[i].cluster != idx) continue;
				sum += ccb_ar[i].MaxY - ccb_ar[i].MinY + 1;
				aant += 1;
			}
			if( aant < 1) return -1;
			return (int)(sum / aant);
	}
	
	//------------------------------------------------------------
	private void zoekClassificationIDMethodeEen()
	//------------------------------------------------------------
	{
		 TRACE("zoekClassificationIDMethodeEen");
		 // zoek optimum LETTER - het aantal elementen ligt het dichtst bij 500 - dit bepaalt de LETTER index
		 for(int i=0;i<cococulst.size();i++)
		 {
				int dist = Math.abs(cParam.MEAN_CHAR_COUNT - cococulst.get(i).aantalElementen);
	        	int gemiddeldeHoogte = getMeanClusterHeigth(cococulst.get(i).clusterIdx);
	        	if( gemiddeldeHoogte > (cParam.ARBITRAIR_HOOGTE_ONDER / 2) ) dist += 3000;  // EXPERIMENTEEL
	        	cococulst.get(i).distance = dist;
		 }
		 TRACE("zoekClassificationIDMethodeEen-01 [coculst#=" + cococulst.size() + "]");
		 // Minimale distance
		 int min_dist=6400000;
		 int min_dist_idx=-1;
		 int min_elem=640000;
		 int min_elem_idx=-1;
		 for(int j=0;j<cococulst.size();j++)
		 {
			 if( cococulst.get(j).distance < min_dist ) {
					 min_dist = cococulst.get(j).distance;
					 min_dist_idx = j;
			 }
			 if( cococulst.get(j).aantalElementen < min_elem ) {
				 min_elem = cococulst.get(j).aantalElementen;
				 min_elem_idx = j;
		 }
		 }
		 TRACE("zoekClassificationIDMethodeEen-02");
		 // 2nd runner up
		 min_dist=640000;
		 int sec_min_dist_idx=-1;
		 for(int j=0;j<cococulst.size();j++)
		 {
				 if( j == min_dist_idx ) continue;
				 if( cococulst.get(j).distance < min_dist ) {
					 min_dist = cococulst.get(j).distance;
					 sec_min_dist_idx = j;
				 }
		 }
		 if( (sec_min_dist_idx < 0) || (sec_min_dist_idx >= cococulst.size() ) ) {
			 do_error("zoekClassificationIDMethodeEen - Second Idx not found");
			 sec_min_dist_idx = min_dist_idx;
		 }
		 TRACE("zoekClassificationIDMethodeEen-03");
		 LETTER = min_dist_idx;
		 FRAME = min_elem_idx;
		 Rapporteer("Classification indices :  FRAME=" + FRAME + "  TEXT=" + LETTER + "  2ndLETTER=" + sec_min_dist_idx );
		 //
		 int onderlingVerschil = Math.abs( cococulst.get(LETTER).aantalElementen - cococulst.get(sec_min_dist_idx).aantalElementen );
	     boolean doSwap = ( ( (onderlingVerschil < (cParam.CHAR_COUNT_TOLERANCE) ) && 
	        		          (cococulst.get(sec_min_dist_idx).aantalElementen     < cParam.MAX_CHAR_COUNT) && 
	        		          (cococulst.get(sec_min_dist_idx).aantalElementen     > cParam.MIN_CHAR_COUNT)  
	        		         ) ? true : false);
	     //
	     do_log(5,"?? onderlingverschil=" + onderlingVerschil + "<" + cParam.CHAR_COUNT_TOLERANCE + " && " + cococulst.get(sec_min_dist_idx).aantalElementen  + "<" + cParam.MAX_CHAR_COUNT + " && " + cococulst.get(sec_min_dist_idx).aantalElementen + ">" + cParam.MIN_CHAR_COUNT);
	     do_log(5,"Swappen? OnderlingVerschil=" + onderlingVerschil + "  drempelverschil1=" + cococulst.get(LETTER).distance + " drempelverschil2=" + cococulst.get(sec_min_dist_idx).distance + " ==> " + doSwap);
	     TRACE("zoekClassificationIDMethodeEen-06");
	     if( doSwap ) {
	    	    // ga voor minste aantal tekstparagrafen ?? raar
	        	boolean doSwap2 = ( cococulst.get(min_dist_idx).aantalTekstParagrafen > cococulst.get(sec_min_dist_idx).aantalTekstParagrafen ? true : false);
	            do_log(5,"Paragrafen [LETTER=" + cococulst.get(min_dist_idx).aantalTekstParagrafen + "] [2nd=" + cococulst.get(sec_min_dist_idx).aantalTekstParagrafen + "] ==> swap=" + doSwap2);
	            if( doSwap2 ) {
	                LETTER = sec_min_dist_idx;
	            	do_log(5,"Swapped -> Classification indices :  FRAME=" + FRAME + "  TEXT="+LETTER);
	            }
	     }
	}
	
	//------------------------------------------------------------
	private int telTekstParagrafen()
	//------------------------------------------------------------
	{
	   int teller=0;
	   for(int i=0;i<tcc_ar.length;i++) {
		   if( (tcc_ar[i].isValid == true) && (tcc_ar[i].isLetterParagraph == true) ) teller++;
	   }
	   return teller;
	}

	//------------------------------------------------------------
	private int getElementCount(int idx)
	//------------------------------------------------------------
	{
		int teller=0;
		for (int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].cluster == idx ) teller++;
		}
		return teller;
	}

	//------------------------------------------------------------
	private int getInvalids(int idx)
	//------------------------------------------------------------
	{
		int teller=0;
		for (int i=0;i<ccb_ar.length;i++)
		{
			if( (ccb_ar[i].isValid == false) && (ccb_ar[i].cluster == idx)) teller++;
		}
		return teller;
	}

	//------------------------------------------------------------
	private int getOriginals(int idx)
	//------------------------------------------------------------
	{
		int teller=0;
		for (int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].originalCluster == idx ) teller++;
		}
		return teller;
	}

	//------------------------------------------------------------
	private int getNbrOfLettersInCluster(int clusteridx)
	//------------------------------------------------------------
	{
		int teller=0;
		for (int i=0;i<ccb_ar.length;i++)
		{
			if( (ccb_ar[i].isPotentialLetter == true) && (ccb_ar[i].cluster == clusteridx)) teller++;
		}
		return teller;
	}
	
	//------------------------------------------------------------
	private int getNbrOfLettersViaBundel(int bundelIdx)
	//------------------------------------------------------------
	{
			int teller=0;
			for(int i=0;i<ccb_ar.length;i++)
			{
				if( (ccb_ar[i].bundel == bundelIdx) && (ccb_ar[i].isPotentialLetter == true) ) teller++;
			}
			return teller;
	}
	
	//------------------------------------------------------------
	private void raporteerOriginalConComClusters(boolean finaal)
	//------------------------------------------------------------
	{
	   TRACE("raporteerOriginalConComClusters");
	   // Extra - calculated fields
	   int saveLETTER = LETTER;
	   for(int i=0;i<cococulst.size();i++)
        {
	    	LETTER = cococulst.get(i).clusterIdx;
        	cococulst.get(i).sco= gatherLETTERStatistics();
            //
		    cococulst.get(i).ElementCount = getElementCount(cococulst.get(i).clusterIdx);
		    if( cococulst.get(i).tipe == cmcProcEnums.ParagraphType.LETTER ) cococulst.get(i).NbrOfTextParagraphs = telTekstParagrafen();
		                                                                else cococulst.get(i).NbrOfTextParagraphs = 0;
		    cococulst.get(i).NbrOfLettersInCluster=getNbrOfLettersInCluster(cococulst.get(i).clusterIdx);
		    cococulst.get(i).NbrOfOriginals=getOriginals(cococulst.get(i).clusterIdx);
		    cococulst.get(i).NbrOfInvalids=getInvalids(cococulst.get(i).clusterIdx);
	    }
	 	LETTER = saveLETTER;
	    cmcStatDAO dao = new cmcStatDAO( xMSet , logger );
	    dao.raporteerOriginalConComClusters(cococulst, finaal,cPage);
	    // 	
	}
	
	//------------------------------------------------------------
	private StatContainer gatherLETTERStatistics()
	//------------------------------------------------------------
	{
		TRACE("gatherLETTERStatistics");
		StatContainer x = new StatContainer();
		//
		int aantal=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false) continue;
			if( ccb_ar[i].cluster != LETTER ) continue;
			aantal++;
		}
		//
		int[] arHoogte  = new int[aantal];
		int[] arBreedte = new int[aantal];
		aantal=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false) continue;
			if( ccb_ar[i].cluster != LETTER ) continue;
			arBreedte[aantal] = ccb_ar[i].MaxX - ccb_ar[i].MinX + 1;
			arHoogte[aantal]  = ccb_ar[i].MaxY - ccb_ar[i].MinY + 1;
			aantal++;
		}
		//
		gpListStatFunctions bStat = new gpListStatFunctions(arBreedte,logger);
		x.meanCharWidth  = bStat.getMean();
		x.medCharWidth   = bStat.getMedian();
		x.glbMinw        = bStat.getMinimum();
		x.glbMaxw        = bStat.getMaximum();
		x.quartile1Width = bStat.getQuartile1();
		x.quartile3Width = bStat.getQuartile3();
		x.iqrWidth       = bStat.getIQR();
		bStat=null;
		//
		gpListStatFunctions hStat = new gpListStatFunctions(arHoogte,logger);
		x.meanCharHeigth  = hStat.getMean();
		x.medCharHeigth   = hStat.getMedian();
		x.glbMinh         = hStat.getMinimum();
		x.glbMaxh         = hStat.getMaximum();
		x.quartile1Heigth = hStat.getQuartile1();
		x.quartile3Heigth = hStat.getQuartile3();
		x.iqrHeigth       = hStat.getIQR();
		hStat=null;
		//
		//logit(5,"[Cluster:" + LETTER + "]  WIDTH  [mean=" + x.meanCharWidth + ",median=" + x.medCharWidth + "]   HEIGTH [mean=" + x.meanCharHeigth + ",median=" + x.medCharHeigth + "] +"
		//				+ "[Q1=" + x.quartile1 + "] [Q3=" + x.quartile3 + "] [IQR=" + x.iqr + "]");
		//
		return x;
	}
	
	
	//------------------------------------------------------------
	private void fetchComponentsInProximity(int idx , int medCharHeigth)
	//------------------------------------------------------------
	{
		TRACE("fetchComponentsInProximity");
		double x1breedte = (double)(ccb_ar[idx].MaxX - ccb_ar[idx].MinX + 1);
		double y1hoogte  = (double)(ccb_ar[idx].MaxY - ccb_ar[idx].MinY + 1);
		double x1center  = (double)ccb_ar[idx].MinX + (x1breedte / 2);
		double y1center  = (double)ccb_ar[idx].MinY + (y1hoogte / 2);
		//
		for(int i=0;i<ar_proximity.length;i++) ar_proximity[i]=-1;
	    int cntr=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false) continue;
			if( ccb_ar[i].cluster != LETTER ) continue;
			// Nieuw
			if( ccb_ar[i].isPotentialLetter == false ) continue;
			if( idx == i ) continue;
			//
		    double x2breedte = (double)(ccb_ar[i].MaxX - ccb_ar[i].MinX + 1);
		    double y2hoogte  = (double)(ccb_ar[i].MaxY - ccb_ar[i].MinY + 1);
			double x2center  =  (double)ccb_ar[i].MinX + ( x2breedte / 2);
			double y2center  =  (double)ccb_ar[i].MinY + ( y2hoogte / 2);
			//
			// RULE (Rigaud : Automatic text localisation) : the horizontal inter-letter distance should be smaller than maximum height of the 2 letters
			// RULE (Rigaud : Robust frame extraction) : text region closer than 2 times the median text heigth
			// OUDE REGEL ==>  : kleiner dan max van de hoogtes van de letters of 3/2 mediaan breedte
			// Nieuwe regel :  < MAX(h1,h2) and < 2 * MedianHeigth
			// RULE (this) : verticale gap is kleiner dan 150% van de mediaan hoogte
			double xdist = Math.abs(Math.abs(Math.round(x1center - x2center))) - (( x1breedte + x2breedte ) / 2) ;
			double ydist = Math.abs(Math.abs(Math.round(y1center - y2center))) - (( y1hoogte + y2hoogte ) / 2);
			switch ( proxtol )
			{
			case TIGHT : {
				if( xdist > Math.max(y1hoogte, y2hoogte ) * 1.00 ) continue;
				if( ydist > (medCharHeigth * 1.00) ) continue;
				break;
			}
			case LENIENT : {
				if( (xdist > Math.max(y1hoogte, y2hoogte )) && (xdist > ((double)medCharHeigth * 3)/2) ) continue; 
				if( ydist > ((medCharHeigth * 3) / 2) ) continue;
				break;
			}
			case WIDE : {
				if( (xdist > Math.max(y1hoogte, y2hoogte )) && (xdist > ((double)medCharHeigth * 2)) ) continue;
				if( ydist > (medCharHeigth * 2) ) continue;
				break;
			}
			case ULTRA_WIDE : {
				if( (xdist > Math.max(y1hoogte, y2hoogte )) && (xdist > ((double)medCharHeigth * 4)) ) continue;
				if( ydist > (medCharHeigth * 4) ) continue;
				break;
			}
			}
			// Add to proximity list
			ar_proximity[cntr] = i;
			cntr++;
			if( cntr >= ar_proximity.length ) {
				do_error("Too many components in proximity");
				break;
			}
		}
		
	}
	
	// Dit is een connected components alogritme op de letterboxes 
	// doel is om de letterboxes die bij elkaar horen in een bubble te combineren
	//------------------------------------------------------------
	private void collateLetterBoxes(boolean trial)
	//------------------------------------------------------------
	{
		TRACE("collateLetterBoxes");
		//
		StatContainer stc = gatherLETTERStatistics();
		//
		int STARTBUNDEL=100;
		int curbundel=STARTBUNDEL;
		int processed=0;
		// Conn.Comp.Logic
		for(int i=0;i<ccb_ar.length;i++)
        {
			ccb_ar[i].bundel = -1; // reset
			if( ccb_ar[i].isValid == false) continue;
			if( ccb_ar[i].cluster != LETTER ) continue;
			// NIEUW
			if( ccb_ar[i].isPotentialLetter == false ) continue;
			
			//
			processed++;
			// maak lijst van de componenten in de buurt
			fetchComponentsInProximity( i , stc.medCharHeigth );
			// zoek de minimale label in de reeks
			int bundel=-1;
			for(int j=0;j<ar_proximity.length;j++)
			{
				if( ar_proximity[j] < 0 ) break;
				if( ccb_ar[ar_proximity[j]].bundel < 0 ) continue;
				if( bundel < 0 ) bundel = ccb_ar[ar_proximity[j]].bundel;
		        if( ccb_ar[ar_proximity[j]].bundel < bundel ) bundel = ccb_ar[ar_proximity[j]].bundel;
			}
			// indien geen bundel in de buurt
			if( bundel == -1 ) {
				bundel=curbundel;
				curbundel++;
			}
			else {
				int repl_bundel=-1;
				// vervang alle bundels in de buurt door de minimale
				for(int j=0;j<ar_proximity.length;j++)
				{
					if( ar_proximity[j] < 0 ) break;
					repl_bundel = ccb_ar[ar_proximity[j]].bundel;
					if( (repl_bundel < 0) || ( repl_bundel == bundel)) continue;
					/*
					System.out.println("Merging bundel " + repl_bundel + " with " + bundel );
					for(int uu=0;uu<i;uu++) {
					   if( ccb_ar[uu].bundel < 0 ) continue;	
					   System.out.print(" (" + ccb_ar[uu].bundel + ")" );	
					}
					System.out.println("");
					*/
					for(int uu=0;uu<i;uu++)
					{
						if( ccb_ar[uu].bundel == repl_bundel ) ccb_ar[uu].bundel = bundel;
					}
				}
			}
			// zet de bundel
			ccb_ar[i].bundel=bundel;
        }
		
		// einde van de connected component; maak nu een lijst van bundels
	    int aantal_bundels = curbundel - STARTBUNDEL + 1;
	    int minx=-1;
	    int miny=-1;
	    int maxx=-1;
	    int maxy=-1;
	    cmcConnectedTextComponentBundel[] temp = new cmcConnectedTextComponentBundel[aantal_bundels];
	    int effectief=0;
	    int nbrSingleElem=0;
	    for(int i=0;i<aantal_bundels;i++)
	    {
	    	int idx = i + STARTBUNDEL;
	    	temp[i] = new cmcConnectedTextComponentBundel(idx,xMSet.mkNumericalUID());
	    	temp[i].isValid = false;
	    	minx=miny=maxx=maxy=-1;
	    	int elementen=0;
	    	for(int j=0;j<ccb_ar.length;j++)
	    	{
	    		if( ccb_ar[j].bundel != idx ) continue;
	    		if( minx == - 1) {
	    			minx=ccb_ar[j].MinX;
	    			miny=ccb_ar[j].MinY;
	    			maxx=ccb_ar[j].MaxX;
	    			maxy=ccb_ar[j].MaxY;
	    			elementen++;
	    			continue;
	    		}
	    		if( minx > ccb_ar[j].MinX ) minx=ccb_ar[j].MinX;
	    		if( miny > ccb_ar[j].MinY ) miny=ccb_ar[j].MinY;
	    		if( maxx < ccb_ar[j].MaxX ) maxx=ccb_ar[j].MaxX;
	    		if( maxy < ccb_ar[j].MaxY ) maxy=ccb_ar[j].MaxY;
	    		elementen++;
	    	}
	    	//
	    	if( elementen > 1 ) {
	    	  effectief++;
	    	  temp[i].isValid = true;
	    	  temp[i].bundel = idx;
	    	  temp[i].MinX = minx;
	    	  temp[i].MinY = miny;
	    	  temp[i].MaxX = maxx;
	    	  temp[i].MaxY = maxy;
	    	  temp[i].counter = elementen;
	    	  //System.out.println("BUNDLE [" + idx + "] ELEMs[" + elementen + "] " + minx + " " + miny + " " + maxx + " " + maxy);
	    	}
	    	else {
	    	  if( elementen == 1 ) nbrSingleElem++;	
	    	}
	    }
	    String sLijn = "Number of Single Element Paragraphs [" + nbrSingleElem + "] in [" + processed + "]";
	    if( !trial ) Rapporteer(sLijn); else do_log(5,sLijn);
	    
	    //
	    // compressen en creatie van de resultaat array TCC_AR
	    tcc_ar=null;
	    tcc_ar = new cmcConnectedTextComponentBundel[effectief];
	    int cntr=-1;
	    for(int i=0;i<aantal_bundels;i++)
	    {
		   if( temp[i].counter <= 0 ) continue;
		   cntr++;
		   tcc_ar[cntr] = new cmcConnectedTextComponentBundel( temp[i].bundel , xMSet.mkNumericalUID());
		   tcc_ar[cntr].isValid = true;
		   tcc_ar[cntr].counter = temp[i].counter;
		   tcc_ar[cntr].MinX = temp[i].MinX;
		   tcc_ar[cntr].MinY = temp[i].MinY;
		   tcc_ar[cntr].MaxX = temp[i].MaxX;
		   tcc_ar[cntr].MaxY = temp[i].MaxY;
//System.out.println("BUNDLE [" + tcc_ar[cntr].bundel + "] ELEMs[" + tcc_ar[cntr].counter + "] " + tcc_ar[cntr].MinX + " " + tcc_ar[cntr].MinY + " " + tcc_ar[cntr].MaxX + " " + tcc_ar[cntr].MaxY);
	    }
	    temp=null; // relase voor GC
	    //
	    //
	    sLijn= "Collate : [" + processed + "] elements in Text cluster reduced to [" + tcc_ar.length + "] Text paragaphs";
	    do_log(5,sLijn);
	    if( !trial ) Rapporteer(sLijn);
	    //
	    stc=null;
	}
	
	//------------------------------------------------------------
	private void paragraphReport()
	//------------------------------------------------------------
	{
		cmcStatDAO dao = new cmcStatDAO( xMSet , logger );
		dao.paragraphReport(tcc_ar);
	}
	
	
	// doel is om de bubbels en frames die binnen een andere bubble of frame steken te verwijderen en alleen de omvattende te houden
	//------------------------------------------------------------
	private void growFramesAndBubbles()
	//------------------------------------------------------------
	{
		TRACE("growFramesAndBubbles");
		int processed=0;
		int invalidated=0;
		for(int i=0;i<ccb_ar.length;i++)
        {
        	if( ccb_ar[i].isValid == false) continue;
        	if( ccb_ar[i].cluster == LETTER ) continue;
        	processed++;
        	// kijk of er frames die deze frame omvat indien ja dan maak de ingeslien frame HIDDENFRAME
        	for(int j=0;j<ccb_ar.length;j++)
        	{
        		if( i == j ) continue;
        		if( ccb_ar[j].isValid == false) continue;
        		if( ccb_ar[j].cluster == LETTER) continue;
        		if( (ccb_ar[j].MinX >= ccb_ar[i].MinX) && (ccb_ar[j].MaxX <= ccb_ar[i].MaxX) && (ccb_ar[j].MinY >= ccb_ar[i].MinY) && (ccb_ar[j].MaxY <= ccb_ar[i].MaxY)){
        			//System.out.println("(" + ccb_ar[i].MinX + "," + ccb_ar[i].MinY + ") (" + ccb_ar[i].MaxX + "," + ccb_ar[i].MaxY + ")  encapsulates " + 
        		    //			           "(" + ccb_ar[j].MinX + "," + ccb_ar[j].MinY + ") (" + ccb_ar[j].MaxX + "," + ccb_ar[j].MaxY + ")" );
        			//ccb_ar[j].cluster = 0 - ccb_ar[j].cluster;
        			ccb_ar[j].isValid = false;
        			invalidated++;
        		}
        	}
        }	
		Rapporteer("Found [" + invalidated + "] encapsulated bubbles/frames in a total of [" + processed + "]");
	}
	
	
	//------------------------------------------------------------
	private void genereerOutput(int[] workPixels)
	//------------------------------------------------------------
	{
		TRACE("genereerOutput");
		// maak een witte canvas
		for(int i=0;i<(cPage.getImageWidth() * cPage.getImageHeigth());i++)
		{
			workPixels[i] = cmcProcConstants.WIT;
		}
		int kleur = cmcProcConstants.ORANJE;
		for(int i=0;i<ccb_ar.length;i++)
        {
        	if( ccb_ar[i].isValid == false) continue;
        	if( ccb_ar[i].cluster  != LETTER ){
        	    kleur = (ccb_ar[i].cluster == FRAME ) ? cmcProcConstants.BLAUW : cmcProcConstants.ORANJE;
        		do_rectangle( workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1, kleur);
        	}
        }
		
		// herinlezen van de oorspronkelijke file
		gpIntArrayFileIO fin = new gpIntArrayFileIO(logger);
		cPage.cmcImg.pixels = null;
		cPage.cmcImg.pixels = fin.readIntArrayFromFile(xMSet.getOriginalImagePixelDumpFileName());
		
		
		// inlezen van secties
		for(int i=0;i<tcc_ar.length;i++)
		{
			merge_rectangle( cPage.cmcImg.pixels , workPixels , tcc_ar[i].MinX , tcc_ar[i].MinY , tcc_ar[i].MaxX - tcc_ar[i].MinX + 1 , tcc_ar[i].MaxY - tcc_ar[i].MinY + 1 );
        }
		// tonen van de individuele letters
		/*
		for(int i=0;i<ccb_ar.length;i++)
        {
        	if( ccb_ar[i].isValid == false) continue;
        	if( ccb_ar[i].cluster  == LETTER ){
        		merge_rectangle( cPage.cmcImg.pixels , workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1 );
        	}
        }
        */
		//
		kleur = cmcProcConstants.ROOD;
		for(int i=0;i<tcc_ar.length;i++)
		{
			if( tcc_ar[i].isLetterParagraph ) kleur = cmcProcConstants.GROEN; else kleur = cmcProcConstants.ROOD;
			do_rectangle( workPixels , tcc_ar[i].MinX , tcc_ar[i].MinY , tcc_ar[i].MaxX - tcc_ar[i].MinX + 1 , tcc_ar[i].MaxY - tcc_ar[i].MinY + 1, kleur);
        }
		//
		// naar JPG wegschrijven
		cPage.cmcImg.moveWorkPixelsToImage();
		cPage.cmcImg.writeToFile( xMSet.getLetterOutputJPGName() );
	}
	
	
	//------------------------------------------------------------
	private void merge_rectangle( int[] src , int[] tgt , int topx , int topy , int width , int heigth)
	//------------------------------------------------------------
	{
		int src_idx=-1;
		int tgt_idx=-1;
		int x=-1;
		int y=-1;
		
		//System.out.println( "tx=" + topx + "ty=" + topy + " w=" + width + " h=" + heigth + "marge-x=" + cPage.getPayLoadTopLeft().x + " marge-y=" + cPage.getPayLoadTopLeft().y);
		try {
		int margeHoogte  = cPage.getPayLoadTopLeft().y;
		int margeBreedte = cPage.getPayLoadTopLeft().x;
		//
		for( y=0;y<heigth;y++)
		{
			src_idx = ((topy + y + margeHoogte ) * cPage.getUncroppedWidth()) + topx + margeBreedte;
			tgt_idx = ((topy + y) * cPage.getPayloadWidth() ) + topx;
			for( x=0;x<width;x++)
			{
				tgt[ tgt_idx ] = src[ src_idx];
				tgt_idx++;
				src_idx++;
				if( src_idx >= src.length ) {
					do_error("ERROR SRC OOB");
				    break;
				}
				if( tgt_idx >= tgt.length ) {
					do_error("ERROR TT OOB");
				    break;
				}
			}
		}
		}
		catch( Exception e) {
			do_log(0,"Oops [" + e.getMessage() + "]");
			do_log(0,"Uncrop " + cPage.getUncroppedWidth() + " x " + cPage.getUncroppedHeigth() + " = " + (cPage.getUncroppedWidth() * cPage.getUncroppedHeigth())  + " " + src.length);
			do_log(0,"PayLoad " + cPage.getPayloadWidth() + " x " + cPage.getPayloadHeigth() + " = " + (cPage.getPayloadWidth() * cPage.getPayloadHeigth())  + " " + tgt.length);
			do_log(0,"topx=" + topx + " Uncropbreedte=" + cPage.getUncroppedWidth() +  " payloadbreedte=" + cPage.getPayloadWidth() );
			do_log(0,"topy=" + topy + " UncropHoogte=" + cPage.getUncroppedHeigth() +  " payloadHoogte=" + cPage.getPayloadHeigth() );
			do_log(0,"w=" + width + " h=" + heigth + "marge-x=" + cPage.getPayLoadTopLeft().x + " marge-y=" + cPage.getPayLoadTopLeft().y);
			do_log(0,"x=" + x + " y=" + y + " src_idx=" + src_idx + " tgt_idx="+tgt_idx + "  src_max=" + src.length + " tg_max=" + tgt.length + "  " + e.getMessage() );
		}
	}
		
	//------------------------------------------------------------
	private void do_rectangle(int[] p , int x , int y , int breedte , int hoogte , int kleur)
	//------------------------------------------------------------
	{
		  try {	
			for(int j =0;j<breedte-1;j++)
	    	{
	    		p[ x + (y * cPage.getImageWidth()) + j] = kleur;
	    		p[ x + ((y+hoogte-1) * cPage.getImageWidth()) + j] = kleur;
	    	}
	    	for(int j =0;j<hoogte-1;j++)
	    	{
	    		p[ x + ((y + j) * cPage.getImageWidth()) ] = kleur;
	    		p[ x + breedte - 1 + ((y + j) * cPage.getImageWidth()) ] = kleur;
	    	}
		  }
		  catch(Exception e) {
			 do_error("do_rectangle(" + x + "," + y + "," + breedte + "," + hoogte + ") is out of bounds B/H (" + cPage.getImageWidth() + "," + cPage.getImageHeigth() + ")" + " " + e.getMessage());
		  }
	}
	

	//------------------------------------------------------------
	private void identificeerTekstParagrafen(boolean trial)
	//------------------------------------------------------------
	{
		TRACE("identificeerTekstParagrafen");
		//
		multret min = new multret();
		// stat function
		gpFrequencyDistributionStatFunctions stat = new gpFrequencyDistributionStatFunctions();
		int optimalThreshold = xMSet.getOptimalThreshold() & 0xff;
        //logit(5,"OptimalThreshold="+optimalThreshold);
		//
		// herinlezen van de oorspronkelijke file 
		gpIntArrayFileIO fin = new gpIntArrayFileIO(logger);
		cPage.cmcImg.pixels = null;
		cPage.cmcImg.pixels = fin.readIntArrayFromFile(xMSet.getOriginalImagePixelDumpFileName());
		//
		int picBreedte = cPage.getUncroppedWidth();
		int picHoogte  = cPage.getUncroppedHeigth();
		int picAantal  = picBreedte * picHoogte;
		if( cPage.cmcImg.pixels.length != picAantal )  {
			do_log( 0 , "systeem fout : uncropped buffer grootte ");
		}
		//
		// loop doorheen de paragrafen
		// extraheer het zwartwitplaatje
		// maak een distributie van horizontale en verticale witte pixels
		int margeHoogte  = cPage.getPayLoadTopLeft().y;
		int margeBreedte = cPage.getPayLoadTopLeft().x;
		for(int k=0 ; k<tcc_ar.length ; k++)
		{
		    int topx = tcc_ar[k].MinX;
		    int topy = tcc_ar[k].MinY;
			int breedte = tcc_ar[k].MaxX - tcc_ar[k].MinX + 1;
			int hoogte  = tcc_ar[k].MaxY - tcc_ar[k].MinY + 1;
			int aantal_pixels = breedte * hoogte;
			// move grayscale picture in een werkbuffer
			int[] work = new int[aantal_pixels];
		    for(int y=0;y<hoogte;y++)
			{
				int src_idx = ((topy + y + margeHoogte ) * cPage.getUncroppedWidth()) + topx + margeBreedte;
				int tgt_idx = y * breedte;
				for(int x=0;x<breedte;x++)
				{
					work[ tgt_idx ] = cPage.cmcImg.pixels[ src_idx];
					tgt_idx++;
					src_idx++;
				}
			}
		    // Kleur Image wegschrijven
		    String FNaam = xMSet.getTextOutputJPGName(k);
		    if( trial == false ) cPage.cmcImg.writePixelsToJPG( work , breedte , hoogte , FNaam );
	       
		    // Zoek de contour en zet alles buiten de contour op wit
		    cleanseImage( work , topx , topy , breedte , tcc_ar[k].bundel );
		    
		    // Wegschrijven
		    String ZNaam = xMSet.getTextOutputJPGName(((LETTER+1)*100)+k);
	        //if( trial == false ) cPage.cmcImg.writePixelsToJPG( work , breedte , hoogte , ZNaam );
		    	        
	        // grijs maken
	        for(int i=0; i<aantal_pixels;i++)
			{
				int p = work[i]; 
	            int r = 0xff & ( p >> 16); 
	            int g = 0xff & ( p >> 8); 
	            int b = 0xff & p; 
	            int lum = (int) Math.round(0.2126*r + 0.7152*g + 0.0722*b);               
	            work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum;
	        }
	        //
		    // maak grijs histo
	        int[] grijshisto = new int[256];
	        for(int i=0;i<256;i++) grijshisto[i]=0;
	        for(int i=0;i<aantal_pixels;i++)
	        {   // work[i] is  TRA + R + G + B  waarbij R=G=B = grijs, neem dus gewoon de laatste 8 bist
	        	int grijs = work[i] & 0xff; 
	        	grijshisto[grijs] +=1;
	        }
	        // Grijs stats
	        tcc_ar[k].mean   = stat.getMean(grijshisto);
	        tcc_ar[k].stdev  = stat.getStdDev(grijshisto);
	        tcc_ar[k].vari   = stat.getGemiddeldeAfwijking(grijshisto);
	        tcc_ar[k].median = stat.getMedian(grijshisto);
	        
	        // Image naar ZwartWit zetten
	        int lum=0;
	        int dens=0;
	        for(int i=0;i<aantal_pixels;i++)
	        {   // work[i] is  TRA + R + G + B  waarbij R=G=B = grijs, neem dus gewoon de laatste 8 bist
	        	if( (work[i] & 0xff) < optimalThreshold) { lum = 0;	dens++; }  // zwart
	        	                        else { lum = 0xff; 	} // wit
	        	work[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum;
	        }
	        tcc_ar[k].density = (double)dens / (double)aantal_pixels;
	 
	        // kijk of dit een paragraaf met letters is
	        min = isEenParagraafMetLetters( work , breedte , min);
	        if( tcc_ar[k].density != min.density ) {
	        	do_log(0,"System error - density differs");
	        }
	        tcc_ar[k].horizontalVariance = min.horizontalVariance;
	        tcc_ar[k].verticalVariance   = min.verticalVariance;
	        tcc_ar[k].isLetterParagraph  = min.isLetter;
	        tcc_ar[k].letterCount        = this.getNbrOfLettersViaBundel( tcc_ar[k].bundel );
	        //
			work=null;
		}
		min = null;
		//
	}

	
	// bepaal gemiddeld aantal lettes per text en zet de graphs die meer letters hebben dan avg op text
	// TODO naar quartile1
	//------------------------------------------------------------
	private void overruleGraph()
	//------------------------------------------------------------
	{
		TRACE("OverruleGraph");
		double sum = 0;
		double aantal = 0;
		for(int i=0;i<tcc_ar.length;i++)
		{
			if( (tcc_ar[i].isLetterParagraph == false) || (tcc_ar[i].isValid == false) ) continue;
			sum += tcc_ar[i].letterCount;
			aantal += 1;
		}
		if( aantal == 0 ) return;
		int avg = (int)(sum / aantal);
		//
		do_log( 5, "Average number of characters [" + avg + "]");
		for(int k=0;k<tcc_ar.length;k++)
		{
			if( (tcc_ar[k].letterCount > avg) && (tcc_ar[k].isLetterParagraph==false) ) {
	        	do_log( 5 , "Overruled [" + k + "] : reason : letter count [" + tcc_ar[k].letterCount + "] exceeds threshold [" + avg  + "]");
	        	tcc_ar[k].isLetterParagraph = true;
	        }
		}
	}
	
	//   TODO    :  WAT INDIEN DENSITEIT = 0 ?
	//------------------------------------------------------------
	private multret isEenParagraafMetLetters( int[] work , int breedte , multret min)
	//------------------------------------------------------------
	{
		TRACE("isEenParagraafMetLetters");
		min.reset();
		//
		int aantal_pixels = work.length;
		int hoogte = aantal_pixels / breedte;
		// vereist wel ZwartWit
		// Aantal WITTE pixels vertikaal
		int[] wit_vertikaal = new int[breedte];
		double dens=0;
		for(int i=0;i<breedte;i++)
		{
			wit_vertikaal[i] = 0;
			for(int j=0;j<hoogte;j++)
			{
				int k = i + (j *breedte);
				if( (work[k] & 0xff) == 0xff ) wit_vertikaal[i] +=1; else dens += 1;  // densiteit ook maar
			}
		}
		dens = dens / (double)aantal_pixels;
		// Aantal WITTE pixes horizontaal
		int[] wit_horizontaal = new int[hoogte];
		for(int i=0; i<hoogte;i++)
		{
			wit_horizontaal[i]=0;
			for(int j=0;j<breedte;j++)
			{
			   int k = (i * breedte) + j;
			   if( (work[k] & 0xff) == 0xff ) wit_horizontaal[i] +=1;
			}
		}
		//  varianties 
		gpListStatFunctions vStat = new gpListStatFunctions(wit_vertikaal,logger);
		min.verticalVariance = vStat.getMeanVariance();
		vStat=null;
		gpListStatFunctions hStat = new gpListStatFunctions(wit_horizontaal,logger);
		min.horizontalVariance = hStat.getMeanVariance();
		hStat = null;
	    //	
		min.density = dens;
		//
		double ratio = min.horizontalVariance / min.verticalVariance;
        if( ratio > cParam.HorizontalVerticalVarianceThreshold ) min.isLetter= true; 
                                                            else min.isLetter = false;
        //logit(5,"==>" + work.length + " " + breedte + " " + dens + " " +ratio +" " + glbVariHorz + " " + glbVariVert + " " +glbPotentialLetter);
        return min;
    }
	
	

	//------------------------------------------------------------
	private boolean kaderOmvatLetter(int x , int y , int breedte , int hoogte)
	//------------------------------------------------------------
	{
		// zoek gewoon voor overlap
		int x1 = x;
		int x2 = x + breedte - 1;
		int y1 = y;
		int y2 = y + hoogte - 1; 
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( (ccb_ar[i].isValid == false) || (ccb_ar[i].isPotentialLetter == false) ) continue;
			if( (ccb_ar[i].MaxX < x1) || (ccb_ar[i].MinX > x2 ) ) continue;
			if( (ccb_ar[i].MaxY < y1) || (ccb_ar[i].MinY > y2 ) ) continue;
			return true;
		}
		return false;
	}
	
	//------------------------------------------------------------
	private void dumpControlePlaatjes(int[] workPixels)
	//------------------------------------------------------------
	{
		TRACE("dumpControlePlaatjes");
		        // herinlezen van de oorspronkelijke file
				gpIntArrayFileIO fin = new gpIntArrayFileIO(logger);
				cPage.cmcImg.pixels = null;
				cPage.cmcImg.pixels = fin.readIntArrayFromFile(xMSet.getOriginalImagePixelDumpFileName());
				
				// maak een witte canvas
				for(int i=0;i<( cPage.getImageWidth() * cPage.getImageHeigth() );i++)
				{
					workPixels[i] = cmcProcConstants.WIT;
				}
				// zet de frame
				int teller=0;
				for(int i=0;i<ccb_ar.length;i++)
				{
					if( ccb_ar[i].isValid == false ) continue;
					if( ccb_ar[i].cluster != FRAME ) continue;
					teller++;
					//merge_rectangle( cPage.cmcImg.pixels , workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1 );
					do_rectangle( workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1, cmcProcConstants.ORANJE);
		        }
				//
				// GROEN = niet letters die letter werden
				// BLAUW = oorspronkelijke letters
				// ROOD = oorspronkelijk letter, doch op die geen letter zijn
				teller=0;
				for(int i=0;i<ccb_ar.length;i++)
				{
					if( ccb_ar[i].isValid == false ) continue;
					if( ccb_ar[i].cluster != LETTER ) continue;
					teller++;
					if( ccb_ar[i].isPotentialLetter ) {
					 if( ccb_ar[i].originalCluster != LETTER )
					 do_rectangle( workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1, cmcProcConstants.GROEN);
					 else
					 do_rectangle( workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1, cmcProcConstants.BLAUW);
					}
					else
					do_rectangle( workPixels , ccb_ar[i].MinX , ccb_ar[i].MinY , ccb_ar[i].MaxX - ccb_ar[i].MinX + 1 , ccb_ar[i].MaxY - ccb_ar[i].MinY + 1, cmcProcConstants.ROOD);
			    }
				// de paragrafen
				for(int i=0;i<tcc_ar.length ; i++)
				{
				 do_rectangle( workPixels , tcc_ar[i].MinX , tcc_ar[i].MinY , tcc_ar[i].MaxX - tcc_ar[i].MinX + 1 , tcc_ar[i].MaxY - tcc_ar[i].MinY + 1, cmcProcConstants.GRIJS);
				}
				// dumpen
				String FNaam = xMSet.getReportClusterDiagramName();
			    cPage.cmcImg.moveWorkPixelsToImage();
			    cPage.cmcImg.writeToFile( FNaam );
			    do_log(5,"Dumped [" + teller + "] cluster report letter to [" + FNaam  + "]");
			    
	}
	
	
	// zoekt de contouren van de ccb_ar componenten die letters zijn en die binnen de grenzen van een paragraaf vallen
	// daarna vanaf de rand tot de contour de achtergrond in het WIT zetten
	// de routine werkt goed om letters te bepalen met IsLetter
	// doch het plaatje dat gemaakt wordt is niet echt bruikbaar om te displayen
	//------------------------------------------------------------
	private void cleanseImage( int[] work , int topx , int topy , int breedte , int bundel )
	//------------------------------------------------------------
	{
		TRACE("CleanseImage");
		int kleur = cmcProcConstants.WIT;
		int hoogte = work.length / breedte;
		// bekijk elke letter; kijk of hij in grenzen van work valt en zoek dan de afstand uiterst links naar 1ste object en zet dat dan wit; vervolgs afsand uiterst rechts naar meest rechtse compo
		// Horizontaal cleansen
		for(int y=0;y<hoogte;y++)
		{
		 int MinX = topx + breedte - 1;   // = MinX + (MaxX - MinX + 1) - 1  => MaxX
		 int MaxX = topx;
		 for(int i=0;i<ccb_ar.length;i++)
		 {
			//if( (ccb_ar[i].isValid == false) || (ccb_ar[i].isPotentialLetter == false) ) continue;
			if( ccb_ar[i].bundel != bundel ) continue;
			if( (ccb_ar[i].MinY <= (topy + y)) && (ccb_ar[i].MaxY >= (topy + y))  ) {
		        if( ccb_ar[i].MinX < MinX ) MinX = ccb_ar[i].MinX;
		        if( ccb_ar[i].MaxX > MaxX ) MaxX = ccb_ar[i].MaxX; 
		    }
		 }
		 MinX -= topx;
		 MaxX -= topx;
		 //
		 if( MinX < 0 ) MinX =0;
		 if( MinX > breedte ) MinX = breedte;
		 if( MaxX < 0 ) MaxX =0;
		 if( MaxX > breedte ) MaxX = breedte;
		 //
		 for(int i=0;i<MinX;i++)
		 {
			 int k = i + (y*breedte);
			 if( k >= work.length ) continue;
			 work[ k ] = kleur;
		 }
		 for(int i=(MaxX+1);i<breedte;i++)
		 {
			 int k = i + (y*breedte);
			 if( k >= work.length ) continue;
			 work[ k ] = kleur;
		 }
		}
		
		// vertikaal
		for(int x=0;x<breedte;x++)
		{
			int MinY = topy + hoogte - 1;   //  MinY + MaxY - MinY + 1 - 1 ==> MaxY
			int MaxY = topy;
	        for(int i=0;i<ccb_ar.length;i++)
	        {
	        	//if( (ccb_ar[i].isValid == false) || (ccb_ar[i].isPotentialLetter == false) ) continue;
	        	if( ccb_ar[i].bundel != bundel ) continue;
				if( (ccb_ar[i].MinX <= (topx + x)) && (ccb_ar[i].MaxX >= (topx + x))) {
					if( ccb_ar[i].MinY < MinY ) MinY = ccb_ar[i].MinY;
					if( ccb_ar[i].MaxY > MaxY ) MaxY = ccb_ar[i].MaxY;
				}
		    }
	        MinY -= topy;
	        MaxY -= topy;
	        //
	        if( MinY <= 0) MinY = 1;
	        if( MinY > hoogte) MinY = hoogte;
	        if( MaxY > hoogte ) MaxY = hoogte;
	        if( MaxY < 0 ) MaxY = 0;
	        //
	        for(int i=0;i<MinY;i++)
	        {
	        	int k = (breedte*i) + x;
	        	if( k >= work.length ) continue;
				work[ k ] = kleur;
	        }
	        for(int i=(MaxY+1);i<hoogte;i++)
	        {
	        	int k = (breedte*i) + x;
	        	if( k >= work.length ) continue;
	        	work[ k ] = kleur;
	        }
		}
		
	}
	
	// Routine bij opstart
	// loop doorheen alle connected components en zoek naar letters
	//------------------------------------------------------------
	private void scanForLettersInConnectedComponentList()
	//------------------------------------------------------------
	{
		TRACE("scanForLettersInConnectedComponentList");
		long startt=System.currentTimeMillis();
		
		// bepaal de hoogte vork
		int valids=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false ) continue;
			if( (ccb_ar[i].MaxY - ccb_ar[i].MinY + 1) < cParam.ARBITRAIR_HOOGTE_ONDER ) continue; // < 6
			valids++;
		}
		int arhoogte[] = new int[valids];
		valids=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false ) continue;
			if( (ccb_ar[i].MaxY - ccb_ar[i].MinY + 1) < cParam.ARBITRAIR_HOOGTE_ONDER ) continue;
			arhoogte[valids] = ccb_ar[i].MaxY - ccb_ar[i].MinY + 1;
			valids++;
		}
		gpListStatFunctions hStat = new gpListStatFunctions(arhoogte,logger);
		int q1 = hStat.getQuartile1();
		int q3 = hStat.getQuartile3();
		int iqr = hStat.getIQR();
		arhoogte=null;
		int range = 0;
		switch( proxtol )
		{
		case TIGHT   : { range = (iqr * 3) / 2; break; }
		case LENIENT : { range = iqr * 2; break; }
		case WIDE    : { range = (iqr * 5) / 2; break; }
		case ULTRA_WIDE : { range = (iqr * 3); break; }
		default   : { range = (iqr * 3) / 2; break; }
		}
	    int ondergrens = q1 - range;
	    int bovengrens = q3 + range;
		if( ondergrens < cParam.ARBITRAIR_HOOGTE_ONDER ) ondergrens = cParam.ARBITRAIR_HOOGTE_ONDER;
		if( bovengrens > cParam.ARBITRAIR_HOOGTE_BOVEN ) bovengrens = cParam.ARBITRAIR_HOOGTE_BOVEN;
		
		
		//
		//transformeerOrigineelNaarZwartWit();
		reloadBinarizedImageInMemory();
		
		//
		// loop doorheen de de componenten die binnen de hoogte vork vallen
		int aantal=0;
		for(int k=0;k<ccb_ar.length;k++)
		{
			ccb_ar[k].breedteConform    = false;
			ccb_ar[k].hoogteConform     = false;
			ccb_ar[k].isPotentialLetter = false;
			ccb_ar[k].density           = -1;
			ccb_ar[k].hasBeenScannedForLetter = false;
			if( ccb_ar[k].isValid == false ) continue;
			int hoogte  = ccb_ar[k].MaxY - ccb_ar[k].MinY + 1;
			// HOOGTE
			if( (hoogte<ondergrens) || (hoogte>bovengrens) ) continue;
			ccb_ar[k].hoogteConform = true;
			int breedte = ccb_ar[k].MaxX - ccb_ar[k].MinX + 1;
			// BREEDTE
			// breedte moet tussen halve en anderhalve hoogte
			if( (breedte < (hoogte / 2)) || (breedte > ((3 * hoogte) / 2) ) ) continue;
			ccb_ar[k].breedteConform = true;
			ccb_ar[k].hasBeenScannedForLetter = true;
			aantal++;
		}
		// isLetter vereist de Median Breedte van een letter
		int[] breedte = new int[aantal];
		int scanned=0;
		for(int k=0;k<ccb_ar.length;k++)
		{
			if( ccb_ar[k].hasBeenScannedForLetter == false ) continue;
			breedte[scanned] = ccb_ar[k].MaxX - ccb_ar[k].MinX + 1;
			scanned++;
		}
		//
		gpListStatFunctions aStat = new gpListStatFunctions(breedte,logger);
		int medianBreedteGuess = aStat.getMedian();
		breedte=null;
		
		//
		// TODO => medianbreedte uit de stats halen
		//
		// zoek nu letters
		int found=0;
		multret mret = new multret();
		for(int k=0;k<ccb_ar.length;k++)
		{
			if( ccb_ar[k].hasBeenScannedForLetter == false ) continue;
			mret = evalueerConnectedComponentForLetter( k , medianBreedteGuess , mret);
			ccb_ar[k].isPotentialLetter = mret.isLetter;
			ccb_ar[k].density = mret.density;
			if( ccb_ar[k].isPotentialLetter ) found++;
		}
		mret = null;
		// rapporteer
		do_log(5,"Scanning for letters -> " + (System.currentTimeMillis() - startt) + " msec");
		do_log(5,"Character heigth [Lowerlimit=" + ondergrens + "] [Upperlimit=" + bovengrens + "]");
		do_log(5,"Total=" + ccb_ar.length + " [scanned=" + scanned + "] [potentialletters=" + found + "] [Guestimate MedianWidth=" + medianBreedteGuess +"]");
	}

	
	//------------------------------------------------------------
	private multret evalueerConnectedComponentForLetter( int idx , int medianBreedteGuess , multret mret)
	//------------------------------------------------------------
	{
		TRACE("evalueerConnectedComponentForLetter");
		mret.reset();
		int topx          = ccb_ar[idx].MinX;
	    int topy          = ccb_ar[idx].MinY;
		int breedte       = ccb_ar[idx].MaxX - ccb_ar[idx].MinX + 1;
		int hoogte        = ccb_ar[idx].MaxY - ccb_ar[idx].MinY + 1;
		int aantal_pixels = breedte * hoogte;
		//
		int margeHoogte   = cPage.getPayLoadTopLeft().y;
		int margeBreedte  = cPage.getPayLoadTopLeft().x;
		//
		// move zwartwit picture in een werkbuffer
		int[] work = new int[aantal_pixels];
		double dens=0;
	    for(int y=0;y<hoogte;y++)
		{
			int src_idx = ((topy + y + margeHoogte ) * cPage.getUncroppedWidth()) + topx + margeBreedte;
			int tgt_idx = y * breedte;
			for(int x=0;x<breedte;x++)
			{
				work[ tgt_idx ] = cPage.cmcImg.pixels[ src_idx ];
				if( (work [ tgt_idx ] & 0xff ) == 0x00 ) dens += 1;  // tel zwarte pixels
				tgt_idx++;
				src_idx++;
			}
		}
	    mret.density = dens / (double)aantal_pixels ;  // densiteit van de zwarte pixels
	    //
        // Test de image voor letter
        mret.isLetter = isEenLetterBIS( work , breedte , medianBreedteGuess );
        work=null;
		//
        return mret;
	}
	
	//
	// Zhongliang Fu
	// tel het aantal volle lijnen, ttz. opeenvolging van zwarte pixels
	// werkt uitsluitend met individuele letters; of indien je weet hoeveel letters er zijn
	//------------------------------------------------------------
	private boolean isEenLetterBIS(int[] work , int breedte , int medianBreedteGuess )
	//------------------------------------------------------------
	{
		boolean Ret=false;
		int aantal_pixels = work.length;
		int hoogte = aantal_pixels / breedte;
        int AantalLetters = (int)(Math.round(((double)breedte / (double)medianBreedteGuess)));
		if( AantalLetters < 1  ) AantalLetters = 1;
		
        // vertikaal aantal continue lijnen => maximaal 3 voor een letter  A B C D E F G
		// horizontaal aantal continue lijnen => maximaal 2 voor een letter A B C D 
        
        // aantal zwarte opeenvolgende pixels in vertikale richting
        int[] vert_volle_lijn = new int[breedte];
        for(int i=0;i<breedte;i++)
        {
        	vert_volle_lijn[i]=0;
        	boolean prev_zwart=false;
        	boolean zwart=false;
        	for(int j=0;j<hoogte;j++)
        	{
        		int k = i + (j * breedte);
        		if( (work[k] & 0xff) == 0xff ) zwart = false; else zwart = true;
        		if( (zwart == true) && (prev_zwart == false ) ) vert_volle_lijn[i] += 1;
        		prev_zwart = zwart;
        	}
        }
        //
        // aantal opeenvolgnde zwarte pixels in horizontale richting
        int[] horz_volle_lijn = new int[hoogte];
        for(int i=0;i<hoogte;i++)
        {
        	horz_volle_lijn[i]=0;
        	boolean prev_zwart=false;
        	boolean zwart=false;
        	for(int j=0; j<breedte;j++)
        	{
        		int k = j + (i * breedte);
        		if( (work[k] & 0xff) == 0xff ) zwart = false; else zwart = true;
        		if( (zwart == true) && (prev_zwart == false ) ) horz_volle_lijn[i] += 1;
        		prev_zwart = zwart;
        	}
        }
        //
        // er moeten minstens 5 goede vertikale en 5 goede opeenvolgende horizontale entries zijn
        // vertikaal : minstens 1 en maximaal 3
        int vscore=0;
        boolean vOk=false;
        for(int i=0;i<vert_volle_lijn.length;i++)
        {
        	if( (vert_volle_lijn[i] >= 1) && (vert_volle_lijn[i] <= 3) ) {
        		vscore++;
        		if( vscore >= 5 ) { vOk = true; break; }
        		continue;
        	}
        	else vscore=0;
        }
        // horizontaal : ( minstens 1 en maximaal 2 ) * aantal letters
        int hscore=0;
        boolean hOk=false;
        for(int i=0;i<horz_volle_lijn.length;i++)
        {
        	if( (horz_volle_lijn[i] >= AantalLetters) && ( horz_volle_lijn[i] <= (AantalLetters * 2)) ) {
        		hscore++;
        		if( hscore >= 5 ) { hOk=true; break; }
        		continue;
        	}
        	else hscore=0;
        }
        //
        if( (hOk == true)  && ( vOk == true) ) Ret = true; 
                                          else Ret = false;
        
        //logit(5,"[" + breedte + " x " + hoogte + "] [Aantalletter=" + AantalLetters + "] [Letter=" + glbPotentialLetter + "]");
        return Ret;
	}
	
	/*
	 * Bereken de inter quartile range op de letters
	 * bekijk dan alles dat tussen de (Q1 - 1.5 IQR) en (Q3 + 1.5 IQR)  -  ik gebruik 2*IQR
	 * Q1 en Q3, resp eerste en derde quartile en IQR : Q3 - Q1
	 * dit is een manier om outliers te vinden
	 */
	//------------------------------------------------------------
	private void zoekEldersNaarLetters()
	//------------------------------------------------------------
	{
		TRACE("zoekEldersNaarLetters");
		
		// imperative that LETTER is within bounds
		boolean showstopper=false;
		if( cococulst == null ) showstopper = true;
		else {
		 if( (LETTER<0) || (LETTER >= this.cococulst.size()) ) showstopper=true;
		}
		if( showstopper ) do_error("System error - ZoekEldersNaarLetters has no valid LETTER value");
		
		// bepaal de hoogte vork (componenten die mogelijk een letter zijn)
		// behoud nu alleen die connected components die in de vork vallen
		StatContainer stco = gatherLETTERStatistics();
		int HoogteRange=0;
		int BreedteRange=0;
		switch( proxtol )
		{
		case TIGHT   : { HoogteRange = (stco.iqrHeigth * 3) / 2; BreedteRange = (stco.iqrWidth * 3 ) / 2 ; break; }
		case LENIENT : { HoogteRange = stco.iqrHeigth * 2; BreedteRange = stco.iqrWidth * 2; break; }
		case WIDE    : { HoogteRange = (stco.iqrHeigth * 5) / 2; BreedteRange = (stco.iqrWidth * 5 ) / 2 ; break; }
		default      : { HoogteRange = stco.iqrHeigth * 2; BreedteRange = stco.iqrWidth * 2 ; break; }
		}
		int ondergrensHoogte  = stco.quartile1Heigth - HoogteRange;
		int bovengrensHoogte  = stco.quartile3Heigth + HoogteRange;
		int ondergrensBreedte = stco.quartile1Width - BreedteRange;
		int bovengrensBreedte = stco.quartile1Width + BreedteRange;
		int medianBreedte     = stco.medCharWidth;
		if( ondergrensHoogte < cParam.ARBITRAIR_HOOGTE_ONDER ) ondergrensHoogte = cParam.ARBITRAIR_HOOGTE_ONDER;
		stco = null;
		//
		// LETTER cluster is gekend. We draaien isLetter opnieuw voor die componenten die in de vork vallen en voor alle LETTER componten
		//
		//transformeerOrigineelNaarZwartWit();
		reloadBinarizedImageInMemory();
		
		//
		int removed=0;
		int addedNonLetter=0;
		int realloc=0;
		int addedLetter=0;
		multret mret = new multret();
		for(int k=0;k<ccb_ar.length;k++)
		{
			if( ccb_ar[k].isValid == false ) continue;
			//
			int hoogte  = ccb_ar[k].MaxY - ccb_ar[k].MinY + 1;
			if( (hoogte >= ondergrensHoogte) && (hoogte <= bovengrensHoogte) ) ccb_ar[k].hoogteConform = true; else ccb_ar[k].hoogteConform = false;
			int breedte  = ccb_ar[k].MaxX - ccb_ar[k].MinX + 1;
			if( (breedte >= ondergrensBreedte) && (breedte <= bovengrensBreedte) ) ccb_ar[k].breedteConform = true; else ccb_ar[k].breedteConform = false;
			//
	    	if( ccb_ar[k].cluster != LETTER ) {
	    	  // gooi de letters uit de niet LETTER cluster die niet in de hoogte vork vallen
			  if( ccb_ar[k].hoogteConform == false ) {
				if( ccb_ar[k].isPotentialLetter == true ) {
				  ccb_ar[k].isPotentialLetter = false;
				  removed++;
				}
				continue;
			  }
			  // evalueer of dit toch geen letter is
			  if( ccb_ar[k].isPotentialLetter == false ) {
				  mret = evalueerConnectedComponentForLetter( k , medianBreedte , mret );
				  ccb_ar[k].isPotentialLetter = mret.isLetter;
				  ccb_ar[k].density = mret.density;
				  if( ccb_ar[k].isPotentialLetter ) addedNonLetter++;
			  }
			  // overrule de niet LETTER cluster
	          if( ccb_ar[k].isPotentialLetter == true  ) {
	        	ccb_ar[k].originalCluster = ccb_ar[k].cluster;
	        	ccb_ar[k].cluster = LETTER;
	        	realloc++;
	          }
	    	}
	    	else {  // LETTER
	    		if( ccb_ar[k].isPotentialLetter == false ) {
	    			mret = evalueerConnectedComponentForLetter( k , medianBreedte , mret );
	    			ccb_ar[k].isPotentialLetter = mret.isLetter;
					ccb_ar[k].density = mret.density;
					if( ccb_ar[k].isPotentialLetter ) addedLetter++;
	    		}
	    	}
	 	}
	    //
		do_log(1,"Look for letters [LowerHeightLimit=" + ondergrensHoogte + "] [UpperHeightLimit=" + bovengrensHoogte + "] [Removed=" + removed + "] [Reallocated=" + realloc + "]");
		do_log(1,"Look for letters [AddedLetter=" + addedLetter + "] [Added Non Letter=" + addedNonLetter + "]");
		
	}
	
	    // isLetter is zeer selectief, bvb. i, apostrofen en letters met veel vertikale strepen
		// er zijn dus nogal wat valse valse
		// De componenten die origineel LETTER zijn maar die isPotentialLetter False zijn 
		// Kijk of die omgeven zijn door een LETTER potentiaal en indien ja terug op letter zetten
		//------------------------------------------------------------
		private void undoIsLetterOvercompensatie()
		//------------------------------------------------------------
		{
			TRACE("undoIsLetterOvercompensatie");
			int restored=0;
			int assessed=0;
			int multiplicator=1;
			switch( proxtol )
			{
			case TIGHT   : { multiplicator = 3; break; }
			case LENIENT : { multiplicator = 4; break; }
			case WIDE    : { multiplicator = 5; break; }
			default : { multiplicator = 3; break; }
			}
			//
			for(int i=0;i<ccb_ar.length;i++)
			{
				if( ccb_ar[i].isValid == false ) continue;
				if( ccb_ar[i].cluster != LETTER ) continue;
				if( ccb_ar[i].isPotentialLetter == true ) continue;
				if( ccb_ar[i].originalCluster != LETTER ) continue;
				if( ccb_ar[i].hoogteConform == false ) continue;
				assessed++;
				// RIGAUD systeem :  links, rechts, boven of onder een letter is een letter
				//     X
				//   . X .
				//     X
			    int breedte = ccb_ar[i].MaxX - ccb_ar[i].MinX + 1;
				int hoogte  = ccb_ar[i].MaxY - ccb_ar[i].MinY + 1;
			    int kaderx  = ccb_ar[i].MinX;
			    int kaderhoogte = multiplicator * hoogte;
			    int kadery  = ccb_ar[i].MinY - ((kaderhoogte - hoogte) / 2);
			    int kaderbreedte = breedte;
			
			    if ( kaderOmvatLetter( kaderx , kadery , kaderbreedte , kaderhoogte ) ) {
			    	ccb_ar[i].isPotentialLetter = true;
			    	restored++;
			    	continue;
			    }
			    //    i
			    //  Y Y Y
			    //    i
			    kadery       = ccb_ar[i].MinY;
			    kaderbreedte = multiplicator * breedte;
			    kaderx       = ccb_ar[i].MinX - ((kaderbreedte - breedte) / 2);
			    kaderhoogte  = hoogte;
			    if ( kaderOmvatLetter( kaderx , kadery , kaderbreedte , kaderhoogte ) ) {
			    	ccb_ar[i].isPotentialLetter = true;
			    	restored++;
			    	continue;
			    }
			}
			do_log(5, "Overcompensation : restored [" + restored  + "] original letters back to isLetter out of [" + assessed + "]");
	}
	
	//------------------------------------------------------------
	private void reloadBinarizedImageInMemory()
	//------------------------------------------------------------
	{   // read the UNCROPPED binarized image
		boolean isOK=true;
		String FName = xMSet.getBinarizedOutputImageNameUncropped(); 
		if( xMSet.xU.IsBestand(FName) == false )  isOK=false;
		//
		if( isOK ) {
			do_log(5,"Reading uncropped binarized file into memory [" + FName + "]");
			gpLoadImageInBuffer imaload = new gpLoadImageInBuffer(xMSet.xU , logger);
			cPage.cmcImg.pixels = null;
			cPage.cmcImg.pixels = imaload.loadBestandInBuffer( FName );
			
			// verify wether the UNCROPPED image is in buffer 
			int picBreedte = cPage.getUncroppedWidth();
			int picHoogte  = cPage.getUncroppedHeigth();
			int picAantal  = picBreedte * picHoogte;
			if( cPage.cmcImg.pixels.length != picAantal )  {
				do_error("System error : uncropped buffer size does not match the size of biarized image ");
			}
	    	else {
			    do_log(5,"Binarized Image reloaded in memory [" + FName + "]");
			}
			imaload=null;
		}
		// force fallback for debug purposes if needed by setting isOK to false
        //isOK = false;  
	    // fallback - can be removed later	
		if(  isOK == false )  {
			do_error("System error - Binarized image not available - will rebinarize original image");
			transformeerOrigineelNaarZwartWit();
			return;
		}
	}
		
	//------------------------------------------------------------
	private void transformeerOrigineelNaarZwartWit()
	//------------------------------------------------------------
	{
		TRACE("transformeerOrigineelNaarZwartWit");
		// herinlezen van de oorspronkelijke file 
				gpIntArrayFileIO fin = new gpIntArrayFileIO(logger);
				cPage.cmcImg.pixels = null;
				cPage.cmcImg.pixels = fin.readIntArrayFromFile(xMSet.getOriginalImagePixelDumpFileName());
				// GrayScale de picture en daarna onmiddellijk naar ZwartWit
				int picBreedte = cPage.getUncroppedWidth();
				int picHoogte  = cPage.getUncroppedHeigth();
				int picAantal  = picBreedte * picHoogte;
				if( cPage.cmcImg.pixels.length != picAantal )  {
					do_log( 0 , "systeem fout : uncropped buffer grootte ");
				}
		        int optimalThreshold = xMSet.getOptimalThreshold() & 0xff;
				for(int i=0; i<picAantal;i++)
				{
					int p = cPage.cmcImg.pixels[i]; 
		            int r = 0xff & ( p >> 16); 
		            int g = 0xff & ( p >> 8); 
		            int b = 0xff & p; 
		            int lum = (int) Math.round(0.2126*r + 0.7152*g + 0.0722*b);               
		            if( (lum & 0xff) < optimalThreshold) lum = 0; else lum = 0xff;     
		            cPage.cmcImg.pixels[i] = (255 << 24) | (lum << 16) | (lum << 8) | lum;
		        }
	}
	
	//------------------------------------------------------------
	private void dumpComponents()
	//------------------------------------------------------------
	{
		TRACE("dumpComponents");
	    // detailed information on the connected components and text paragraphs for the graphical editor
		// CCB and TCC infomration is casted to cmGrapghPageObject format and then dumped 
		// Connected components
		cmcGraphPageObject ar[] = new cmcGraphPageObject[ccb_ar.length + tcc_ar.length];
		for(int i=0;i<ar.length;i++)
		{
			ar[i] = new cmcGraphPageObject();
		}
		
		// copy and transform CCB (connected components)
		for(int i=0;i<ccb_ar.length;i++)
		{
			 ar[i].tipe    = (ccb_ar[i].isPotentialLetter == true ) ? cmcProcEnums.PageObjectType.LETTER : cmcProcEnums.PageObjectType.NOISE;
			 if( ccb_ar[i].cluster == FRAME ) ar[i].tipe = cmcProcEnums.PageObjectType.FRAME;
			 ar[i].UID     = xMSet.mkNumericalUID();
			 ar[i].removed = false;
			 ar[i].isValid =  ccb_ar[i].isValid;
			 ar[i].MinX    = ccb_ar[i].MinX;
			 ar[i].MinY    = ccb_ar[i].MinY;
			 ar[i].MaxX    = ccb_ar[i].MaxX;
			 ar[i].MaxY    = ccb_ar[i].MaxY;
			 ar[i].ClusterIdx    = ccb_ar[i].cluster;
			 ar[i].BundelIdx     = ccb_ar[i].bundel;
			 ar[i].visi          = cmcProcEnums.VisibilityType.INVISIBLE;	
			 ar[i].isSelected    = false;
			 ar[i].DrawObjectOID = -1;
			 ar[i].hasChanged    = false;
			 ar[i].changetipe    = cmcProcEnums.EditChangeType.NONE;
		}
		// text paragraphs
		for(int i=0;i<tcc_ar.length;i++)
		{
			 int j = i + ccb_ar.length;
			 ar[j].tipe    = (tcc_ar[i].isLetterParagraph == true ) ? cmcProcEnums.PageObjectType.TEXTPARAGRAPH : cmcProcEnums.PageObjectType.PARAGRAPH;
			 ar[j].UID     = tcc_ar[i].UID;
			 ar[j].removed = false;
			 ar[j].isValid = tcc_ar[i].isValid;
			 ar[j].MinX    = tcc_ar[i].MinX;
			 ar[j].MinY    = tcc_ar[i].MinY;
			 ar[j].MaxX    = tcc_ar[i].MaxX;
			 ar[j].MaxY    = tcc_ar[i].MaxY;
			 ar[j].ClusterIdx    = -1;
			 ar[j].BundelIdx     = tcc_ar[i].bundel;
			 ar[j].visi          = cmcProcEnums.VisibilityType.INVISIBLE;	
			 ar[j].isSelected    = false;
			 ar[j].DrawObjectOID = -1;
			 ar[j].hasChanged    = false;
			 ar[j].changetipe    = cmcProcEnums.EditChangeType.NONE;
		}
		cmcStatDAO dao = new cmcStatDAO(xMSet,logger);
		dao.dumpComponents(ar,null);
		
		// Create Empty text file,
		cmcTextDump tdump = new cmcTextDump(xMSet,logger);
		tdump.create(ar,cPage, cmeta.getLanguage() );
	}
	
	public long getLoadTimeLetters()
	{
		return LoadTimeLetters;
	}
	public long getLoadTimeParagraphs()
	{
		return LoadTimeParagraphs;
	}
	public int getNbrOfParagraphs()
	{
		return tcc_ar.length;
	}
	
	// due to shifting the coco around that mihgt or might not be letters it might occr that the FRAME cluster has become empty
	// this function will re-assess and re-establish the FRAME cluster
	//------------------------------------------------------------
	public void reAssessFrames()
	//------------------------------------------------------------
	{
		// scan for the cluster that has the coco with maximum heigth (exclude letter)
		int maxHeigth=-1;
		int potentialFRAME = -1;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false ) continue;
			if( ccb_ar[i].cluster == LETTER ) continue;
			int h = ccb_ar[i].MaxY - ccb_ar[i].MinY - 1;
		    //do_log(1," -> " + ccb_ar[i].cluster + " " + ccb_ar[i].originalCluster + " " + h );
			if( h > maxHeigth ) {
				maxHeigth = h;
				potentialFRAME = ccb_ar[i].cluster;
			}
		}
		if( potentialFRAME < 0 ) {
			do_error("Strange - could not find a potential FRAME cluster");
			return;  // no harm done
		}
		do_log(1,"Frame assessment [Previous=" + FRAME + "] [Reassessed=" + potentialFRAME + "] [MaxHeigth=" + maxHeigth + "]");
       
		//
		if( FRAME !=  potentialFRAME ) {
		  FRAME = potentialFRAME;
	      // finally, zet de tipes
	      fixTipesOnCoCoCuLst();
		}
		
		// overkill - KMeans on height is accurate enough
		
		// look for objects that are in the same size range but not part of FRAME cluster
		// via K-MEANS 3 on surface
		/*
		int aantal = 0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false ) continue;
			if( ccb_ar[i].cluster == LETTER ) continue;
			aantal++;
		}
		int[] oppervlakte = new int[aantal];
		aantal=0;
		for(int i=0;i<ccb_ar.length;i++)
		{
			if( ccb_ar[i].isValid == false ) continue;
			if( ccb_ar[i].cluster == LETTER ) continue;
			int b = ccb_ar[i].MaxX - ccb_ar[i].MinX - 1; if (b < 0) b=0;
			int h = ccb_ar[i].MaxY - ccb_ar[i].MinY - 1; if ( h< 0) h=0;
			oppervlakte[aantal] = b *h;
			aantal++;
		}
	    //	
		cmcProcKMeans km = new cmcProcKMeans();
		km.populateSingleDimensionSet( 3 , oppervlakte );
		km.doit();
		int[] clusters = km.getClusters();
		
		for(int i=0;i<oppervlakte.length;i++)
		{
			do_log(1,"" + oppervlakte[i] + "->" + clusters[i] );
		}
		*/
	}
	
}
