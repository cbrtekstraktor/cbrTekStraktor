package imageProcessing;

public class cmcConnectedTextComponentBundel {
	
	public long cretme=0L;
	public int bundel=-1;
	public boolean isValid=true;
	public int MinX=-1;
    public int MaxX=-1;
    public int MinY=-1;
    public int MaxY=-1;
    public int counter=0;
    public int mean=-1;
    public int median=-1;
    public double stdev=-1;
    public double vari=-1;
    public double density=-1;
    public double horizontalVariance=-1;
    public double verticalVariance=-1;
    public boolean isLetterParagraph=false;
    public int letterCount=0;
    public long UID;
    
    public cmcConnectedTextComponentBundel(int i,long iUID)
    {
    	cretme = System.nanoTime();
    	bundel = i;
    	isValid=true;
    	MinX=-1;
        MaxX=-1;
        MinY=-1;
        MaxY=-1;
        counter=0;
        mean=-1;
        median=-1;
        stdev=-1;
        vari=-1;
        density=-1;
        horizontalVariance=-1;
        verticalVariance=-1;
        isLetterParagraph=false;
        letterCount=0;
        UID=iUID;
    }
   
}
