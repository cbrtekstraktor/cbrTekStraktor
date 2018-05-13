package tensorflow;

import cbrTekStraktorModel.cmcProcEnums;

public class cmcVRParagraph {
	
	  private long UID=-1L;
	  private cmcProcEnums.PageObjectType tipe = cmcProcEnums.PageObjectType.UNKNOWN;
	  private cmcProcEnums.PageObjectType new_tipe = cmcProcEnums.PageObjectType.UNKNOWN;
	  private String LongImageFileName=null;
	  private double confidence=0;
	  cmcVRParagraph(long uid)
	  {
		  UID=uid;
	  }
	  public long getUID()
	  {
		  return UID;
	  }
	  public cmcProcEnums.PageObjectType getTipe()
	  {
		  return tipe;
	  }
	  public cmcProcEnums.PageObjectType getNewTipe()
	  {
		  return new_tipe;
	  }
	  public String getLongImageFileName()
	  {
		  return LongImageFileName;
	  }
	  public double getConfidence()
	  {
		  return confidence;
	  }
	  public boolean hasTipeChanged()
	  {
	    if( (tipe != cmcProcEnums.PageObjectType.PARAGRAPH) && (tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) return false;
	    if( (new_tipe != cmcProcEnums.PageObjectType.PARAGRAPH) && (new_tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) return false;
		return (tipe == new_tipe) ? false : true;
	  }
     //
	  public void setTipe(cmcProcEnums.PageObjectType t)
	  {
		  tipe=t;
	  }
	  public void setNewTipe(cmcProcEnums.PageObjectType t)
	  {
		  new_tipe=t;
	  }
	  public void setLongImageFileName(String s)
	  {
		 LongImageFileName=s;
	  }
	  public void setConfidence(double d)
	  {
		  confidence=d;
	  }

}
