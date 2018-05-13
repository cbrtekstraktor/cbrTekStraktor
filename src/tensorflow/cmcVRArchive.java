package tensorflow;

import java.util.ArrayList;

import tensorflow.cmcVRParagraph;

public class cmcVRArchive {
	
	  private String ShortArchiveFileName=null;
	  private long startt=-1L;
	  private long stopt=-1L;
	  private ArrayList<cmcVRParagraph> plist=null;
	  
	  public cmcVRArchive(String fn)
	  {
		  ShortArchiveFileName=fn;
		  startt=stopt=System.currentTimeMillis();
		  plist = new ArrayList<cmcVRParagraph>();
	  }
	  
	  public void setStopt(long lo)
	  {
		  stopt=lo;
	  }
	  public String getShortArchiveFileName()
	  {
		  return this.ShortArchiveFileName;
	  }
	  public ArrayList<cmcVRParagraph> getplist()
	  {
		  return plist;
	  }

}
