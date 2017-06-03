package cbrTekStraktorModel;

public class cmcStopWatch {
	
	private String StopWatchName=null;
	private long startTimeStampMilli=-1L;
	private long startTimeStampNano=0L;
	private long durationMilliSec=-1L;
	private long durationNanoSec=-1L;
	
	public cmcStopWatch(String in)
	{
		StopWatchName=in;
		startTimeStampMilli=System.currentTimeMillis();
		startTimeStampNano=System.nanoTime();
	}
	//-----------------------------------------------------------------------
	public void stopChrono()
	//-----------------------------------------------------------------------
	{
		durationMilliSec = System.currentTimeMillis() - startTimeStampMilli;
		durationNanoSec = System.nanoTime() - startTimeStampNano;
	}
	//-----------------------------------------------------------------------
	public String getStopWatchName()
	//-----------------------------------------------------------------------
	{
		return StopWatchName;
	}

	//-----------------------------------------------------------------------
	public long getDurationMilliSec()
	//-----------------------------------------------------------------------
	{
		return durationMilliSec;
	}
	//-----------------------------------------------------------------------
	public long getDurationNanoSec()
	//-----------------------------------------------------------------------
	{
		return durationNanoSec;
	}
}
