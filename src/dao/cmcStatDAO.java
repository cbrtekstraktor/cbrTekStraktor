package dao;

import java.util.ArrayList;

import logger.logLiason;
import imageProcessing.cmcConnectedTextComponentBundel;
import imageProcessing.cmcProcColorHistogram;
import imageProcessing.ConnectedComponentCluster;
import imageProcessing.cmcTimingInfo;
import generalpurpose.gpAppendStream;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;
import drawing.cmcGraphPageObject;

public class cmcStatDAO {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private gpAppendStream aps = null;
	
	//-----------------------------------------------------------------------
	public cmcStatDAO(cmcProcSettings is,logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
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
	
	//-----------------------------------------------------------------------
	private boolean openStream(String FNaam)
	//-----------------------------------------------------------------------
	{
		aps = null;
		aps = new gpAppendStream(FNaam,xMSet.getCodePageString());
		return true;
	}
	
	//-----------------------------------------------------------------------
	public boolean writePageStats(comicPage pag)
	//-----------------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == true ) {
			xMSet.xU.VerwijderBestand(XmlFLong);
		}
		if( xMSet.xU.IsBestand(XmlFLong) == true ) {
			do_error("Cannot remove [" + XmlFLong + "]");
			return false;
		}
		//
		openStream(XmlFLong);
		//
		//aps.AppendIt("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		aps.AppendIt(xMSet.getXMLEncodingHeaderLine());
		aps.AppendIt("<!-- Application : " + xMSet.getApplicDesc() + " -->");
		aps.AppendIt("<!-- Start : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		aps.AppendIt("<ComicPage>");
		// History
		aps.AppendIt("<ProcessHistory>");
		aps.AppendIt("<ProcessTimeStamp>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</ProcessTimeStamp>" );
		aps.AppendIt("</ProcessHistory>");
		//		
		aps.AppendIt("<File>");
		aps.AppendIt("<FileName>" + xMSet.xU.GetFileName(pag.getFName()) + "</FileName>");
		aps.AppendIt("<FilePath>" + xMSet.xU.GetParent(pag.getFName()) + "</FilePath>");
		aps.AppendIt("<FileSize>" + xMSet.xU.getFileSize(pag.getFName())+ "</FileSize>");
		aps.AppendIt("<FileLastModification>" + (xMSet.xU.prntStandardDateTime(xMSet.xU.getModificationTime(xMSet.getOrigImageLongFileName()))).toUpperCase()+ "</FileLastModification>");
		aps.AppendIt("</File>");
		//
		aps.AppendIt("<Image>");
		aps.AppendIt("<CMXUID>" + pag.getCMXUID() + "</CMXUID>");
		aps.AppendIt("<UID>" + pag.getUID() + "</UID>");
		aps.AppendIt("<UncroppedWidth>" + pag.getUncroppedWidth() + "</UncroppedWidth>");
		aps.AppendIt("<UncroppedHeigth>" + pag.getUncroppedHeigth() + "</UncroppedHeigth>");
		aps.AppendIt("<UncroppedSize>" + (pag.getUncroppedHeigth()*pag.getUncroppedWidth()) + "</UncroppedSize>");
		aps.AppendIt("<PayLoadX>" + pag.getPayLoadTopLeft().x + "</PayLoadX>");
		aps.AppendIt("<PayLoadY>" + pag.getPayLoadTopLeft().y + "</PayLoadY>");
		aps.AppendIt("<PayLoadWidth>" + pag.getPayloadWidth() + "</PayLoadWidth>");
		aps.AppendIt("<PayLoadHeigth>" + pag.getPayloadHeigth() + "</PayLoadHeigth>");
		aps.AppendIt("<PayLoadSize>" + (pag.getPayloadWidth()*pag.getPayloadHeigth()) + "</PayLoadSize>");
		//
		aps.AppendIt("<PhysicalWidthDPI>" + pag.getPhysicalWidthDPI() + "</PhysicalWidthDPI>");
		aps.AppendIt("<PhysicalHeigthDPI>" + pag.getPhysicalHeigthDPI() + "</PhysicalHeigthDPI>");
		//
		aps.AppendIt("<ColourSchema>" + (xMSet.getColourSchema()).toString().toLowerCase() + "</ColourSchema>");
		aps.AppendIt("<MonochromeDetected>" + pag.getIsMonoChrome() + "</MonochromeDetected>");
		aps.AppendIt("<MonochromeDetectedStatus>" + xMSet.getMonochromedetectionStatus() + "</MonochromeDetectedStatus>");
		aps.AppendIt("<Grayscale>" + pag.getIsGrayScale() + "</Grayscale>");
		aps.AppendIt("<NbrOfPeaks>" + pag.getNumberOfPeaks() + "</NbrOfPeaks>");
		aps.AppendIt("<NbrOfValidPeaks>" + pag.getNumberOfValidPeaks() + "</NbrOfValidPeaks>");
		aps.AppendIt("<PeakCoverage>" + pag.getPeakCoverage() + "</PeakCoverage>");
		//	
		aps.AppendIt("<BlackBorder>" + pag.getHasBlackBorder() + "</BlackBorder>");
		aps.AppendIt("<GrainyBackGround>" + pag.getHasgrainyBackGround() + "</GrainyBackGround>");
		//
		aps.AppendIt("</Image>");
	    //	
		aps.CloseAppendFile();
        //
		return true;
	}
	
	//-----------------------------------------------------------------------
	public boolean dump_histogram(cmcProcColorHistogram h , boolean isOriginal)
	//-----------------------------------------------------------------------
	{
		
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		//
		if( isOriginal )
		aps.AppendIt("<OriginalHistogram>");
		else
		aps.AppendIt("<PayloadHistogram>");
		//	
		for(int k=0;k<4;k++)
		{
			double[] dh=null;
			int mean=0;
			int med=0;
			double vari=0;
			double stddev=0;
			String sC="";
			switch(k)
			{
			case 0 : { sC="Red"; dh=h.getHistoRed();mean=h.getMeanRed();med=h.getMedianRed();stddev=h.getStdDevRed(); vari=h.getVarianceRed();break;}
			case 1 : { sC="Green"; dh=h.getHistoGreen();mean=h.getMeanGreen();med=h.getMedianGreen();stddev=h.getStdDevGreen();vari=h.getVarianceGreen();break;}
			case 2 : { sC="Blue"; dh=h.getHistoBlue();mean=h.getMeanBlue();med=h.getMedianBlue();stddev=h.getStdDevBlue();vari=h.getVarianceBlue();break;}
			default: { sC="Luminence"; dh=h.getHistoGray();mean=h.getMeanGray();med=h.getMedianGray();stddev=h.getStdDevGray();vari=h.getVarianceGray();break;}
			}
			//
			aps.AppendIt("<Histogram" + sC + ">");
			aps.AppendIt("<Bins>" + 256 + "</Bins>");
			aps.AppendIt("<Mean>" + mean + "</Mean>");
			aps.AppendIt("<Median>" + med + "</Median>");
			aps.AppendIt("<Variance>" + vari + "</Variance>");
			aps.AppendIt("<StdDev>" + stddev + "</StdDev>");
		    //
			String sLijn="";
			double scale = h.getScale();
			for(int j=0;j<256;j++)
			{
				int f = (int)(dh[j] * scale);
				if( j ==0 )  sLijn = "" + f;
				else sLijn = sLijn + "," + f;
			}
			aps.AppendIt("<Frequency>" + sLijn + "</Frequency>");
			aps.AppendIt("</Histogram" + sC + ">");
			
		}
		//
		if( isOriginal )
		aps.AppendIt("</OriginalHistogram>");
		else
		aps.AppendIt("</PayloadHistogram>");
		//
		aps.CloseAppendFile();
        return true;
        
		
	}
	
	//------------------------------------------------------------
	private void Rapporteer(String sIn)
	//------------------------------------------------------------
	{
			boolean isXML = ((sIn.trim().startsWith("<")) && (sIn.trim().endsWith(">")) ? true : false);
			String sLijn = sIn;
			if( !isXML ) sLijn = "<!-- " + sIn + " -->";
			aps.AppendIt(sLijn);
			if( !isXML ) do_log(9,sIn);
	}
	
	//------------------------------------------------------------
	public boolean AppendSingleLine(String sIn)
	//------------------------------------------------------------
	{
		
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			System.err.println("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		//
		Rapporteer(sIn);
		//
		aps.CloseAppendFile();
        return true;
        
	}
	
	//------------------------------------------------------------
	public boolean raporteerOriginalConComClusters(	ArrayList<ConnectedComponentCluster> cococulst, 
			boolean finaal , comicPage pag)
	//------------------------------------------------------------
	{
		
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		//
		openStream(XmlFLong);
		//
		if( finaal ) {
		 Rapporteer("<FinalConnectedComponentClusters>");
	     Rapporteer("<!-- Details of the K-Means clusters after reorganizing -->");
		}
		else {
		 Rapporteer("<ConnectedComponentClusters>");
         Rapporteer("<!-- Details of the K-Means clusters detected -->");
		}
        //
		//
        for(int i=0;i<cococulst.size();i++)
        {
       	   Rapporteer("<Cluster>");
           if( finaal == false ) {
             Rapporteer("<ClusterIdx>" + cococulst.get(i).clusterIdx + "</ClusterIdx>" );
             Rapporteer("<ClusterType>" + cococulst.get(i).tipe + "</ClusterType>");
      	     Rapporteer("<NumberOfClusterElements>" + cococulst.get(i).aantalElementen + "</NumberOfClusterElements>" );
      	     Rapporteer("<NumberOfOriginalClusterElements>" + cococulst.get(i).aantalElementen + "</NumberOfOriginalClusterElements>");
	         Rapporteer("<NumberOfInValidClusterElements>" + cococulst.get(i).NbrOfInvalids + "</NumberOfInValidClusterElements>");
     	     Rapporteer("<NumberOfClusterParagraphs>" + cococulst.get(i).aantalParagrafen + "</NumberOfClusterParagraphs>" );
     	     Rapporteer("<NbrOfTextParagraphs>" + cococulst.get(i).aantalTekstParagrafen + "</NbrOfTextParagraphs>" );
     	     Rapporteer("<NbrOfCharacters>" + cococulst.get(i).aantalLetters + "</NbrOfCharacters>" );
     	     Rapporteer("<CharacterDensity>" + cococulst.get(i).letterdensiteit + "</CharacterDensity>" );
     	     Rapporteer("<TextParagraphDensity>" + cococulst.get(i).tekstparagraafdensiteit + "</TextParagraphDensity>" );
     	     Rapporteer("<MedianWidth>" + cococulst.get(i).MedianWidth + "</MedianWidth>" );
     	     Rapporteer("<MedianHeigth>" + cococulst.get(i).MedianHeight + "</MedianHeigth>" );
     	     Rapporteer("<HeigthQuartile1>" + cococulst.get(i).Quartile1Height + "</HeigthQuartile1>" );
     	     Rapporteer("<HeigthQuartile3>" + cococulst.get(i).Quartile3Height + "</HeigthQuartile3>" );
     	     Rapporteer("<HeigthIQR>" + cococulst.get(i).iqrHeigth + "</HeigthIQR>" );
     	     Rapporteer("<WidthQuartile1>" + cococulst.get(i).Quartile1Width  + "</WidthQuartile1>" );
   	         Rapporteer("<WidthQuartile3>" + cococulst.get(i).Quartile3Width + "</WidthQuartile3>" );
   	         Rapporteer("<WidthIQR>" + cococulst.get(i).iqrWidth + "</WidthIQR>" );
   	         Rapporteer("<MeanHeigth>" + cococulst.get(i).meanCharHeigth + "</MeanHeigth>" );
     	     Rapporteer("<MeanWidth>" + cococulst.get(i).meanCharWidth + "</MeanWidth>" );
     	     Rapporteer("<MinWidth>" + cococulst.get(i).MinWidth + "</MinWidth>" );
     	     Rapporteer("<MaxWidth>" + cococulst.get(i).MaxWidth + "</MaxWidth>" );
     	     Rapporteer("<MinHeigth>" + cococulst.get(i).MinHeigth + "</MinHeigth>" );
     	     Rapporteer("<MaxHeigth>" + cococulst.get(i).MaxHeigth + "</MaxHeigth>" );
     	     Rapporteer("<NormalizedMedianHeigth>" + cococulst.get(i).normalizedMedianHeigth + "</NormalizedMedianHeigth>" );
     	     Rapporteer("<NormalizedMedianWidth>" + cococulst.get(i).normalizedMedianWidth + "</NormalizedMedianWidth>" );
    	   }
           else {
        	 //
        	 int idx = cococulst.get(i).clusterIdx;
        	 //
        	 double nHeigth=(double)pag.getUncroppedHeigth();
        	 double nWidth=(double)pag.getUncroppedWidth();
        	 double letterDens=(double)cococulst.get(i).ElementCount;
        	 double tekstParaDens=cococulst.get(i).aantalParagrafen;
        	 int aantalTekstPara = 0;
        	 if( cococulst.get(i).tipe == cmcProcEnums.ParagraphType.LETTER ) aantalTekstPara = cococulst.get(i).NbrOfTextParagraphs;;
        	 if( nHeigth > 0) nHeigth = (double)cococulst.get(i).sco.medCharHeigth / nHeigth; else nHeigth=-1;
        	 if( nWidth > 0) nWidth = (double)cococulst.get(i).sco.medCharWidth / nWidth; else nWidth=-1;
         	 if( letterDens > 0 ) letterDens = (double)cococulst.get(i).NbrOfLettersInCluster / letterDens; else letterDens=-1;
         	 if( tekstParaDens > 0) tekstParaDens = (double)aantalTekstPara / tekstParaDens; else tekstParaDens=-1;
        	 //
	         Rapporteer("<ClusterIdx>" + idx + "</ClusterIdx>");
	         Rapporteer("<ClusterType>" + cococulst.get(i).tipe + "</ClusterType>");
	         Rapporteer("<NumberOfClusterElements>" + cococulst.get(i).ElementCount + "</NumberOfClusterElements>");
	         Rapporteer("<NumberOfOriginalClusterElements>" + cococulst.get(i).NbrOfOriginals + "</NumberOfOriginalClusterElements>");
	         Rapporteer("<NumberOfInValidClusterElements>" + cococulst.get(i).NbrOfInvalids + "</NumberOfInValidClusterElements>");  
	         Rapporteer("<NumberOfClusterParagraphs>" + cococulst.get(i).aantalParagrafen + "</NumberOfClusterParagraphs>");
	       	 Rapporteer("<NbrOfTextParagraphs>" + aantalTekstPara + "</NbrOfTextParagraphs>" );
		     Rapporteer("<NbrOfCharacters>" + cococulst.get(i).NbrOfLettersInCluster + "</NbrOfCharacters>" );
	         Rapporteer("<CharacterDensity>" + letterDens + "</CharacterDensity>" );
     	     Rapporteer("<TextParagraphDensity>" + tekstParaDens + "</TextParagraphDensity>" );
     	     Rapporteer("<MedianWidth>" + cococulst.get(i).sco.medCharWidth + "</MedianWidth>" );
    	     Rapporteer("<MedianHeigth>" + cococulst.get(i).sco.medCharHeigth + "</MedianHeigth>" );
    	     Rapporteer("<HeigthQuartile1>" + cococulst.get(i).sco.quartile1Heigth + "</HeigthQuartile1>" );
    	     Rapporteer("<HeigthQuartile3>" + cococulst.get(i).sco.quartile3Heigth + "</HeigthQuartile3>" );
    	     Rapporteer("<HeigthIQR>" + cococulst.get(i).sco.iqrHeigth + "</HeigthIQR>" );
    	     Rapporteer("<WidthQuartile1>" + cococulst.get(i).sco.quartile1Width + "</WidthQuartile1>" );
    	     Rapporteer("<WidthQuartile3>" + cococulst.get(i).sco.quartile3Width + "</WidthQuartile3>" );
    	     Rapporteer("<WidthIQR>" + cococulst.get(i).sco.iqrWidth + "</WidthIQR>" );
    	     // 
     	     Rapporteer("<MeanHeigth>" + cococulst.get(i).sco.meanCharHeigth + "</MeanHeigth>" );
    	     Rapporteer("<MeanWidth>" + cococulst.get(i).sco.meanCharWidth + "</MeanWidth>" );
    	     Rapporteer("<MinWidth>" + cococulst.get(i).sco.glbMinw + "</MinWidth>" );
    	     Rapporteer("<MaxWidth>" + cococulst.get(i).sco.glbMaxw + "</MaxWidth>" );
    	     Rapporteer("<MinHeigth>" + cococulst.get(i).sco.glbMinh + "</MinHeigth>" );
    	     Rapporteer("<MaxHeigth>" + cococulst.get(i).sco.glbMaxh + "</MaxHeigth>" );
    	     Rapporteer("<NormalizedMedianHeigth>" + nHeigth + "</NormalizedMedianHeigth>" );
    	     Rapporteer("<NormalizedMedianWidth>" + nWidth + "</NormalizedMedianWidth>" );
   	       }
           Rapporteer("</Cluster>");
        }
        //
        if( finaal ) Rapporteer("</FinalConnectedComponentClusters>");
   	            else Rapporteer("</ConnectedComponentClusters>");
    	//
		aps.CloseAppendFile();
	    return true;
	}	
	
	
	//------------------------------------------------------------
	public boolean paragraphReport(cmcConnectedTextComponentBundel[] tcc_ar)
	//------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		//
		Rapporteer("<paragraphs>");
		Rapporteer("<NumberOfParagraphs>" + tcc_ar.length + "</NumberOfParagraphs>");
		int nbrOfLetters=0;
		for(int i=0;i<tcc_ar.length;i++) nbrOfLetters += tcc_ar[i].counter;
		Rapporteer("<NumberOfElements>" + nbrOfLetters + "</NumberOfElements>");
		//
		for(int i=0;i<tcc_ar.length;i++)
		{
				int breedte = tcc_ar[i].MaxX - tcc_ar[i].MinX +1;
				int hoogte  = tcc_ar[i].MaxY - tcc_ar[i].MinY +1;
				String sLijn =
				   "<paragraphUID>"		+ tcc_ar[i].UID + "</paragraphUID>" + xMSet.xU.ctEOL +
				   "<Bundle>" + tcc_ar[i].bundel + "</Bundle>" + xMSet.xU.ctEOL +
				   "<NumberOfElements>" + tcc_ar[i].counter + "</NumberOfElements>" + xMSet.xU.ctEOL +
				   "<MinX>"        + tcc_ar[i].MinX    + "</MinX>" + xMSet.xU.ctEOL +
				   "<MinY>"        + tcc_ar[i].MinY    + "</MinY>" + xMSet.xU.ctEOL +
				   "<Width>"       + breedte           + "</Width>" + xMSet.xU.ctEOL +
				   "<Heigth>"      + hoogte            + "</Heigth>"+ xMSet.xU.ctEOL +
				   "<GreyMean>"    + tcc_ar[i].mean    + "</GreyMean>" + xMSet.xU.ctEOL +
				   "<GreyMedian>"  + tcc_ar[i].median  + "</GreyMedian>" + xMSet.xU.ctEOL +
				   "<GreyStdDev>"  + tcc_ar[i].stdev   + "</GreyStdDev>" + xMSet.xU.ctEOL +
				   "<GreyMeanVar>" + tcc_ar[i].vari    + "</GreyMeanVar>" + xMSet.xU.ctEOL +
				   "<BWDensity>"   + tcc_ar[i].density + "</BWDensity>" + xMSet.xU.ctEOL +
	               "<HorizontalVariance>" + tcc_ar[i].horizontalVariance    + "</HorizontalVariance>" + xMSet.xU.ctEOL +
	               "<VerticalVariance>" + tcc_ar[i].verticalVariance + "</VerticalVariance>" + xMSet.xU.ctEOL +
	               "<IsLetterParagraph>" + tcc_ar[i].isLetterParagraph    + "</IsLetterParagraph>" + xMSet.xU.ctEOL +
	               "<NumberOfLetters>" + tcc_ar[i].letterCount    + "</NumberOfLetters>" + xMSet.xU.ctEOL +
	               "<paragraphRemoved>" + "false"   + "</paragraphRemoved>" + xMSet.xU.ctEOL +
	               "<paragraphChangeType>" + "none"   + "</paragraphChangeType>" + xMSet.xU.ctEOL +
	               "<paragraphChangeDate>" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyyMMddHHmmss") + "</paragraphChangeDate>"  
	               ;
				Rapporteer("<paragraph>" + xMSet.xU.ctEOL + sLijn + xMSet.xU.ctEOL + "</paragraph>");
		}
		Rapporteer("</paragraphs>");
	    //
		aps.CloseAppendFile();
	    return true;
	}
	
	//------------------------------------------------------------
	public boolean finalWarning(int[] ClassificationGuess , int LETTER)
	//------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		for(int i=0;i<(ClassificationGuess.length-1);i++)
		{
			if( ClassificationGuess[i] == ClassificationGuess[i+1] ) continue;
			if( (ClassificationGuess[i] < 0) || (ClassificationGuess[i+1] < 0) ) continue;
			String sL = "Text cluster used [" + LETTER + "], however classification is not uniformly determined: [" + ClassificationGuess[i] + "] != [" + ClassificationGuess[i+1] + "]";
			do_error(sL);
			Rapporteer(sL);
		}
		if( ClassificationGuess[0] == LETTER ) return true;
		if( ClassificationGuess[1] == LETTER ) return true;
		String sL = "Text cluster has manually been overruled to [" + LETTER +"]";
		do_error(sL);
		Rapporteer(sL);
		aps.CloseAppendFile();
		return true;
	}


	//------------------------------------------------------------
	public boolean dumpClusterClassification(int[] ClassificationGuess , int LETTER , int FRAME)
	//------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		//
		Rapporteer("<ClusterClassification>");
		for(int i=0;i<ClassificationGuess.length;i++)
		{
		 Rapporteer("<Method>");
		 Rapporteer("<MethodID>" + (i+1) + "</MethodID>");
		 Rapporteer("<TextCluster>" + ClassificationGuess[i] + "</TextCluster>");
		 Rapporteer("</Method>");
		}
		Rapporteer("<FrameCluster>" + FRAME + "</FrameCluster>");
		//
		if(  (ClassificationGuess[0] != LETTER) && (ClassificationGuess[1] != LETTER) ) {
			Rapporteer("<ClassificationConflict>Overruled</ClassificationConflict>");	
			 Rapporteer("<TextCluster>" + LETTER + "</TextCluster>");
		}
		else {
			boolean ib = ClassificationGuess[0] != ClassificationGuess[1];
			Rapporteer("<ClassificationConflict>" + ib + "</ClassificationConflict>");		
		}
		Rapporteer("</ClusterClassification>");
		//
		aps.CloseAppendFile();
		return true;
	}
	
	//------------------------------------------------------------
	public boolean dumpComponents( cmcGraphPageObject[] ar , gpAppendStream gp)
	//------------------------------------------------------------
	{
		if( gp == null ) {
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		}
		else {  // the calling routoine provides the printstream
			aps=gp;
		}
		//
			int cococount = 0;
			for(int i=0;i<ar.length;i++)
			{
				if( (ar[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) || (ar[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
				cococount++;
			}
			int paracount = ar.length - cococount;
			//
			String sLijn="";
			aps.AppendIt("<GraphicalEditorArea>");
			aps.AppendIt("<ConnectedComponentDump>");
			aps.AppendIt("<ConnectedComponentCount>" + cococount + "</ConnectedComponentCount>");
			aps.AppendIt("<!-- Valid,Minx,MinY,MaxX,MaxY,ClusterIdx,BundleIdx,IsLetter,UID -->");
			aps.AppendIt("<![CDATA[");
			for(int i=0;i<ar.length;i++)
			{
			   if( (ar[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) || (ar[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
			   boolean isLetter = (ar[i].tipe == cmcProcEnums.PageObjectType.LETTER) ? true : false;
			   sLijn = "" +
			           ar[i].isValid + "," +
			           ar[i].MinX + "," + ar[i].MinY + "," + ar[i].MaxX + "," + ar[i].MaxY + "," +
			           ar[i].ClusterIdx + "," + ar[i].BundelIdx + "," + isLetter + "," + 
			           ar[i].UID + "," +
			           ar[i].changetipe;
			   aps.AppendIt(sLijn.trim().toLowerCase());
			}
			aps.AppendIt("]]>");
			aps.AppendIt("</ConnectedComponentDump>");
			//
			
			// text paragraphs
			aps.AppendIt("<ParagraphDump>");
			aps.AppendIt("<ParagraphCount>" + paracount + "</ParagraphCount>");
			aps.AppendIt("<!-- Valid,Minx,MinY,MaxX,MaxY,BundleIdx,isLetterParagraph,UID,removed -->");
			aps.AppendIt("<![CDATA[");
			for(int i=0;i<ar.length;i++)
	        {
			    if( (ar[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
			    boolean isLetter = (ar[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? true : false;	
	        	sLijn= "" +
	                   ar[i].isValid + "," +
	                   ar[i].MinX + "," + ar[i].MinY + "," + ar[i].MaxX + "," + ar[i].MaxY + "," +
	                   ar[i].BundelIdx + "," + isLetter + "," + 
	                   ar[i].UID + "," +
	                   ar[i].removed + "," +
	                   ar[i].changetipe;
	        	aps.AppendIt(sLijn.trim().toLowerCase());
	        }
			aps.AppendIt("]]>");
			aps.AppendIt("</ParagraphDump>");
			aps.AppendIt("</GraphicalEditorArea>");
			
			
		System.out.println("Component details dumped");
		if( gp == null ) aps.CloseAppendFile();
		return true;
	}

	//------------------------------------------------------------
	public boolean dumpTimeInfo(cmcTimingInfo f)
	//------------------------------------------------------------
	{
		String XmlFLong = xMSet.getXMLStatFileName();
		if( xMSet.xU.IsBestand(XmlFLong) == false ) {
			do_error("Cannot locate [" + XmlFLong + "] for append");
			return false;
		}
		openStream(XmlFLong);
		//
		Rapporteer("<TimingInfoNanoSec>");
        Rapporteer("<LoadTimeImage>" + f.getLoadTimeImage() + "</LoadTimeImage>");
        Rapporteer("<LoadTimePage>" + f.getLoadTimeImage() + "</LoadTimePage>");
        Rapporteer("<LoadTimeBinarize>" + f.getLoadTimeBinarize() + "</LoadTimeBinarize>");
        Rapporteer("<LoadTimeConnectedComponents>" + f.getLoadTimeConnectedComponents() + "</LoadTimeConnectedComponents>");
        Rapporteer("<LoadTimeParagraphs>" + f.getLoadTimeParagraphs() + "</LoadTimeParagraphs>");
        Rapporteer("<LoadTimeOverhead>" + f.getLoadTimeOverhead() + "</LoadTimeOverhead>");
        Rapporteer("<LoadTimeEndToEnd>" + f.getLoadTimeEndToEnd() + "</LoadTimeEndToEnd>");
        //
		Rapporteer("</TimingInfoNanoSec>");
		//
		aps.CloseAppendFile();
		return true;
	}
	
	//------------------------------------------------------------
	public boolean putStopGap()
	//------------------------------------------------------------
	{
			String XmlFLong = xMSet.getXMLStatFileName();
			if( xMSet.xU.IsBestand(XmlFLong) == false ) {
				do_error("Cannot locate [" + XmlFLong + "] for append");
				return false;
			}
			openStream(XmlFLong);
			Rapporteer("<!-- Stop : " + 	(xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
			Rapporteer("</ComicPage>");
			aps.CloseAppendFile();
			return true;
	}
		
}
