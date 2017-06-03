package cbrTekStraktorModel;

public class runTimeMonitor {
	 long runStartTime;
	  long estimatedImageTime;
	  long estimatedTimeBeforePreprocess;
	  long estimatedTimeBeforeBinarize;
	  long estimatedTimeBeforeCoCo;
	  long actualImageTime;
	  long actualPreprocessTime;
	  long actualBinarizeTime;
	  long actualEndToEndTime;
	  float actualDensity;
	  float normaWidth;
	  float normaRatio;
	  float normaSize;
	  float normaSuffix;
	  float normaColor;
	  float normaImageTime;
	  float normaPreprocessTime;
	  float normaBinarizeTime;
	  float normaCocoTime;
	  float normaDensity;
	  
	  runTimeMonitor(float fw,float fr,float fs,float ss,long eima)
	  {
		  runStartTime=System.currentTimeMillis();
		  estimatedImageTime=eima;
		  estimatedTimeBeforePreprocess=-1L;
		  estimatedTimeBeforeBinarize=-1L;
		  estimatedTimeBeforeCoCo=-1L;
		  //
		  actualImageTime=-1L;
		  actualPreprocessTime=-1L;
		  actualBinarizeTime=-1L;
		  actualEndToEndTime=-1L;
		  actualDensity=-1f;
		  //
		  normaWidth=fw;
		  normaRatio=fr;
		  normaSize=fs;
		  normaSuffix=ss;
		  normaColor=-1f;
		  normaImageTime=-1f;
		  normaPreprocessTime=-1f;
		  normaBinarizeTime=-1f;
		  normaCocoTime=-1f;
		  normaDensity=-1;
	  }

	  
	  public long getEstimatedImageTime()
	  {
		  return this.estimatedImageTime;
	  }
	  public long getEstimatedE2BeforePreprocessTime()
	  {
		  return this.estimatedTimeBeforePreprocess;
	  }
	  public long getEstimatedE2BeforeBinarizeTime()
	  {
		  return this.estimatedTimeBeforeBinarize;
	  }
	  public long getEstimatedE2BeforeCoCoTime()
	  {
		  return this.estimatedTimeBeforeCoCo;
	  }
	  public long getActualImageTime()
	  {
		  return actualImageTime;
	  }
	  public long getActualPreprocessTime()
	  {
		  return actualPreprocessTime;
	  }
	  public long getActualBinarzeTime()
	  {
		  return actualBinarizeTime;
	  }
	  public long getActualEndToEndTime()
	  {
		  return actualEndToEndTime;
	  }
	  
}
