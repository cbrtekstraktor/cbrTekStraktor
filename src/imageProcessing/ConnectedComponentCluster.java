package imageProcessing;

import cbrTekStraktorModel.cmcProcEnums;

public class ConnectedComponentCluster {

	public cmcProcEnums.ParagraphType tipe = cmcProcEnums.ParagraphType.UNKNOWN;
	public int clusterIdx=-1;
	public int ElementRang=-1;
	public int aantalElementen=-1;
	public int aantalValid=0;
	public int aantalParagrafen=-1;
	public int aantalTekstParagrafen=-1;
	public int aantalLetters=-1;
	public int distance=-1;
	public int MedianWidth=-1;
	public int MedianHeight=-1;
	public int Quartile1Height=-1;
	public int Quartile3Height=-1;
	public int iqrHeigth=-1;  
	public int Quartile1Width=-1;
	public int Quartile3Width=-1;
	public int iqrWidth=-1;  
	public double tekstparagraafdensiteit=-1;
	public double letterdensiteit=-1;
	public double normalizedMedianHeigth=-1;
	public double normalizedMedianWidth=-1;
	public int meanCharWidth = -1;
	public int meanCharHeigth = -1;
	public int MinWidth=-1;
	public int MinHeigth=-1;
	public int MaxWidth=-1;
	public int MaxHeigth=-1;

	// rapportering
	public StatContainer sco=null;
	public int ElementCount=-1;
	public int NbrOfTextParagraphs=-1;
	public int NbrOfLettersInCluster=-1;
	public int NbrOfOriginals=-1;
	public int NbrOfInvalids=-1;
	//
	public ConnectedComponentCluster(int idx)
	{
		clusterIdx=idx;
	}

}
