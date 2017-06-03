package textProcessing;

import cbrTekStraktorModel.cmcProcEnums;

public class cmcTextObject {
      private int MAX_TRANS = 150;
	
	  public int BundelIdx;
	  public long UID;
	  public boolean removed=false;
	  public String TextOCR;
	  public String TextFrom;
	  public String[] TextTranslated;
	  public boolean hasChanged=false;
	  public long changeDate;   // populate it with System.currentTimeMillis() if needed
	  public cmcProcEnums.TextConfidence confidence = cmcProcEnums.TextConfidence.UNKNOWN;
	  //
	  public cmcTextObject()
	  {
		  BundelIdx=-1;
		  UID=-1L;
		  removed=false;
		  TextOCR="";
		  TextFrom="";
		  TextTranslated= new String[MAX_TRANS];
		  changeDate=-1L;
		  hasChanged=false;
		  for(int i=0;i<TextTranslated.length;i++) TextTranslated[i]="";
		  confidence = cmcProcEnums.TextConfidence.UNKNOWN;
	  }
	
}
