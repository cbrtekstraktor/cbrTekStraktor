package cbrTekStraktorModel;

public class cmcMonitorItem {

	boolean processed=false;
	long UID=-1L;
	String FileName=null;
	long starttime=-1L;
	long endtime=-1L;
	String comment=null;
	
	public cmcMonitorItem(long uid)
	{
		processed=false;
		UID = uid;
		FileName=null;
		starttime=-1L;
		endtime=-1L;
		comment=null;
	}
	//
	public boolean getProcessed() { return processed; }
	public long getUID()          { return UID; }
	public String getFileName()   { return FileName; }
	public long getStarttime()    { return starttime; }
	public long getEndtime()      { return endtime; }
	public String getComment()    { return comment; }
	//
	public void setProcessed(boolean ib) { processed=ib; }
	public void setUID(long li)          { UID = li; }
	public void setFileName(String s)    { FileName = s; }
	public void setStarttime(long li)    { starttime = li; }
	public void setEndtime(long li)      { endtime = li; }
	public void setComment(String s)     { comment = s; }
	//
}
