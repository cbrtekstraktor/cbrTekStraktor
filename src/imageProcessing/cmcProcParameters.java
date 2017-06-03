package imageProcessing;

import cbrTekStraktorModel.cmcProcSettings;



public class cmcProcParameters {

	public int MEAN_CHAR_COUNT = 500;                           // rekenkundig gemiddelde van aantal letters per plaat
    public int CHAR_COUNT_TOLERANCE = (MEAN_CHAR_COUNT / 2) ;  // tolerantie op de Char Count
    public int MIN_CHAR_COUNT = (MEAN_CHAR_COUNT / 5);         // ondergrens aantal letters per plaat
    public int MAX_CHAR_COUNT = (MEAN_CHAR_COUNT * 2);         // bovengrens aantal letters per plaat
    public double HorizontalVerticalVarianceThreshold = 5;
    //
    //
    /*
    */
    //
	public int ARBITRAIR_HOOGTE_ONDER = 6;           // letter minimale hoogte
	public int ARBITRAIR_HOOGTE_BOVEN = 100;         // letter maximale hoogte
	
	cmcProcSettings xMSet=null;
	
	
	public cmcProcParameters(cmcProcSettings iM)
	{
		xMSet = iM;
		//
		int ip = xMSet.getMeanCharacterCount();
		if( (ip >= 100) && (ip<1000) ) {
			MEAN_CHAR_COUNT = ip;                           
		    CHAR_COUNT_TOLERANCE = (MEAN_CHAR_COUNT / 2) ;  
		    MIN_CHAR_COUNT = (MEAN_CHAR_COUNT / 5);         
		    MAX_CHAR_COUNT = (MEAN_CHAR_COUNT * 2); 
		}
		//
		HorizontalVerticalVarianceThreshold = (double)xMSet.getHorizontalVerticalVarianceThreshold();
	}
	
}
