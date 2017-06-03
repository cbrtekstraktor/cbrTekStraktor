package imageProcessing;


public class cmcConnectedComponentBoundary {
	long cretme=System.nanoTime();
	int connectedcomponentlabel=-1;
    boolean isValid=true;
	int MinX=-1;
    int MaxX=-1;
    int MinY=-1;
    int MaxY=-1;
    double relHoogte=-1;
    double relBreedte=-1;
    int counter=0;
    int cluster;    // bevat de cluster ID na een K-MEANS clustering :  0=NOISE 1=LETTER 2=BUBBLE 3=FRAME (indien K=4, indien 5, O=noise, 4=Frame, daartussen
    int originalCluster;
    int bundel=-1;  // groepeert clusters van het type letter in tekstblokken
    boolean hasBeenScannedForLetter=false;
    public boolean isPotentialLetter=true;  
    double density=-1;
    boolean hoogteConform=false;
    boolean breedteConform=false;
}
