package textProcessing;

public class kloonTextObject {

	public kloonTextObject()
	{
	}
	
	public void kloon( cmcTextObject x , cmcTextObject y )
	{
		  y.BundelIdx = x.BundelIdx;
		  y.UID = x.UID;
		  y.removed=x.removed;
		  y.TextOCR = x.TextOCR;
		  y.TextFrom = x.TextFrom;
		  y.changeDate = x.changeDate;
		  y.hasChanged = x.hasChanged;
		  for(int i=0;i<y.TextTranslated.length;i++) y.TextTranslated[i] = x.TextTranslated[i];
		  y.confidence = x.confidence;
	}
	
}
