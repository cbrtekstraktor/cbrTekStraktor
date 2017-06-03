package imageProcessing;

public class cmcTimingInfo {

	private String UID="?";
	private long LoadTimePage = -1L;
	private long LoadTimeImage = -1L;
    private long LoadTimePreprocess=-1L;
    private long LoadTimeBinarize=-1L;
	private long LoadTimeConnectedComponent=-1;
	private long LoadTimeLetters=-1;
	private long LoadTimeParagraphs=-1;
    private long LoadTimeEndToEnd=-1;
    
    private int width=-1;
    private int heigth=-1;
    private long fileSize=-1;
    private int nbrConnectedComponents=-1;
    private int nbrParagraphs=-1;
    private double bwDensity=0;
    private String ImageType="?";
    private String ColourScheme="?";
    private String BinarizeMethod="?";
    public cmcTimingInfo()
    {
    	UID="?";
    	LoadTimePage = -1L;
    	LoadTimeImage = -1L;
        LoadTimePreprocess=-1L;
        LoadTimeBinarize=-1L;
    	LoadTimeConnectedComponent=-1;
    	LoadTimeLetters=-1;
    	LoadTimeParagraphs=-1;
        LoadTimeEndToEnd=-1;
        width=-1;
        heigth=-1;
        fileSize=-1;
        nbrConnectedComponents=-1;
        nbrParagraphs=-1;
        bwDensity=-1;
        ImageType="?";
        ColourScheme="?";
        BinarizeMethod="?";
    }
   
    public void setUID(String s)
    {
    	UID=s;
    }
    public void setColourScheme(String s)
    {
    	ColourScheme=s;
    }
    public void setBinarizeMethod(String s)
    {
    	BinarizeMethod=s;
    }
    public void setPageTime(long il)
    {
    	LoadTimePage=il;
    }
    public void setImageTime(long il)
    {
    	LoadTimeImage=il;
    }
    public void setPreprocessTime(long il)
    {
    	LoadTimePreprocess=il;
    }
    public void setBinarizeTime(long il)
    {
    	LoadTimeBinarize=il;
    }
    public void setConnectedComponentTime(long il)
    {
    	LoadTimeConnectedComponent=il;
    }
    public void setLetterTime(long il)
    {
    	LoadTimeLetters=il;
    }
    public void setParagraphTime(long il)
    {
    	LoadTimeParagraphs=il;
    }
    public void setEndToEndTime(long il)
    {
    	LoadTimeEndToEnd=il;
    }
    public void setImageType(String i)
    {
    	ImageType=i;
    }
    public void setWidth(int i)
    {
    	width=i;
    }
    public void setHeigth(int i)
    {
    	heigth=i;
    }
    public void setFileSize(long i)
    {
    	fileSize=i;
    }
    public void setNbrOfConnectedComponents(int i)
    {
    	nbrConnectedComponents=i;
    }
    public void setNbrOfParagraphs(int i)
    {
    	nbrParagraphs=i;
    }
    public void setBWDensity(double i)
    {
    	bwDensity=i;
    }
    
    // Getters
    public String getUID()
    {
    	return UID;
    }
    public String getColourScheme()
    {
    	return ColourScheme;
    }
    public String getBinarizeMethod()
    {
    	return BinarizeMethod;
    }
    public int getWidth()
    {
    	return width;
    }
    public int getHeigth()
    {
    	return heigth;
    }
    public String getImageType()
    {
    	return ImageType;
    }
    public int getNbrOfConnectedComponents()
    {
    	return nbrConnectedComponents;
    }
    public int getNbrOfParagraphs()
    {
    	return nbrParagraphs;
    }
    public long getFileSize()
    {
    	return fileSize;
    }
    public double getBWDensity()
    {
    	return bwDensity;
    }
    public long getLoadTimePage()
    {
    	return LoadTimePage;
    }
    public long getLoadTimeImage()
    {
    	return LoadTimeImage;
    }
    public long getLoadTimePreprocess()
    {
    	return LoadTimePreprocess;
    }
    public long getLoadTimeBinarize()
    {
    	return LoadTimeBinarize;
    }
    public long getLoadTimeConnectedComponents()
    {
    	return LoadTimeConnectedComponent;
    }
    public long getLoadTimeLetters()
    {
    	return LoadTimeLetters;
    }
    public long getLoadTimeParagraphs()
    {
    	return LoadTimeParagraphs;
    }
    public long getLoadTimeEndToEnd()
    {
    	return LoadTimeEndToEnd;
    }
    public long getLoadTimeOverhead()
    {
    	return  LoadTimeEndToEnd - 
		        LoadTimeImage - LoadTimePage - LoadTimePreprocess - 
			    LoadTimeBinarize - LoadTimeConnectedComponent - LoadTimeLetters - LoadTimeParagraphs;
    }
  
    /*
    public void dumpStat()
    {
    	long rest =  getLoadTimeOverhead();
    	
    	System.out.println("Image      = " + LoadTimeImage / 1000000L);
		System.out.println("Page       = " + LoadTimePage / 1000000L);
		System.out.println("Preprocess = " + LoadTimePreprocess / 1000000L);
		System.out.println("Binarize   = " + LoadTimeBinarize / 1000000L);
		System.out.println("CoCo       = " + LoadTimeConnectedComponent / 1000000L);
		System.out.println("Letters    = " + LoadTimeLetters / 1000000L);
		System.out.println("Paragraphs = " + LoadTimeParagraphs / 1000000L);
		System.out.println("Overhead   = " + rest / 1000000L);
		System.out.println("------------------------------------------");
		System.out.println("E22   = " + LoadTimeEndToEnd / 1000000L);
	
		System.out.println("SIZE " + width + " " + heigth );
		System.out.println("Type "+ this.ImageType );
		System.out.println("" + this.nbrConnectedComponents + " " + this.nbrClusters );
	
    }
    */
    
}
