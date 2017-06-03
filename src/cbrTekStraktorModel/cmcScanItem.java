package cbrTekStraktorModel;

public class cmcScanItem {

	// wordt gebruikt door cmcSettings om bestandsinformatie in $HOME/test directory op te slaan
	
	String FName = null;
	boolean processed = false;
	
	cmcScanItem(String sF)
	{
		processed=false;
		FName=sF;
	}
}
