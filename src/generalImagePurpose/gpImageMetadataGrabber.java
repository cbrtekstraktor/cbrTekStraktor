package generalImagePurpose;

import java.util.ArrayList;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcSettings;

public class gpImageMetadataGrabber {

	cmcProcSettings xMSet=null;
    logLiason logger = null;
    gpFetchIIOMetadata iiom = null;
    gpFetchByteImageData iinf = null;


    private int width=-1;
    private int heigth =-1;
    private int physicalWidthDPI=-1;
    private int physicalHeigthDPI=-1;
    
    
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }
	
    //------------------------------------------------------------
    public gpImageMetadataGrabber(cmcProcSettings im,logLiason ilog)
    //------------------------------------------------------------
    {
    	xMSet = im;
    	logger =ilog;
    	iiom = new gpFetchIIOMetadata(xMSet,logger,false);
    	iinf = new gpFetchByteImageData();
    }
	//
	public int getWidth() { return width; }
	public int getHeigth() { return heigth; }
	public int getPhysicalWidthDPI() { return physicalWidthDPI; }
	public int getPhysicalHeigthDPI() { return physicalHeigthDPI; }
	//
    //------------------------------------------------------------
	public boolean grabImageMetadata(String FileName)
    //------------------------------------------------------------
    {
    	do_log(1,"Fetching metadata [" + FileName + "]");
    	// reset
    	width=-1;
        heigth =-1;
        physicalWidthDPI=-1;
        physicalHeigthDPI=-1;
        // via IIOMetadata
    	boolean ib = iiom.parseMetadataTree( FileName );
    	if (ib ) {
    		width = iiom.getWidth();
    		heigth = iiom.getHeigth();
    		physicalWidthDPI = iiom.getPhysicalWidthDPI();
    	    physicalHeigthDPI = iiom.getPhysicalHeigthDPI();
    	    //
    		if( width < 0 ) ib=false;
    		if( heigth < 0 ) ib=false;
    		if( physicalWidthDPI < 0 ) ib=false;
    		if( physicalHeigthDPI < 0 ) ib=false;
    		//do_log(1,"[W=" + width + "] [H=" + heigth + "] [DPIx" + physicalWidthDPI + "] [DPIy" + physicalHeigthDPI + "]");
    		if( ib )  do_log(9,iiom.getReport() );
        }
    	//
    	// if the IIOMetadata did not provide results, try to use getInfo
   		if( ib==false ) {
    		do_log(1,"==> switching to ImageInfo");
    		iinf.cmcProcGetImageInfo(FileName);
    		do_log(1,iinf.getINFO());
    		//
    		width = iinf.getWidth();
    		heigth = iinf.getHeight();
    		physicalWidthDPI = iinf.getPhysicalWidthDpi();
    	    physicalHeigthDPI = iinf.getPhysicalHeightDpi();
    	    //
     		if( width < 0 ) ib=false;
    		if( heigth < 0 ) ib=false;
    		if( physicalWidthDPI < 0 ) ib=false;
    		if( physicalHeigthDPI < 0 ) ib=false;
    		//do_log(1,"[W=" + width + "] [H=" + heigth + "] [DPIx" + physicalWidthDPI + "] [DPIy" + physicalHeigthDPI + "]");
    	}
   		//
   	   // post process - cappen
    	if( ((physicalWidthDPI < cmcProcConstants.MINIMAL_DPI) && (physicalWidthDPI >= 0)) || 
    		((physicalHeigthDPI< cmcProcConstants.MINIMAL_DPI) && (physicalHeigthDPI>= 0)) ) {
    		do_log(1,"Physical{Width,Heith}DPI [" + physicalWidthDPI + "," + physicalHeigthDPI + "] is out of bound [Max=" + cmcProcConstants.MINIMAL_DPI + "] and has been set to invalid");
    		physicalWidthDPI  =  -1;
			physicalHeigthDPI =  -1;
		}
   		//
  		do_log(1,"[W=" + width + "] [H=" + heigth + "] [DPIx" + physicalWidthDPI + "] [DPIy" + physicalHeigthDPI + "]");
  	    //
    	return ib;
    }
   
    
    // 
    // to be removed
    public void test()
    {
    	
    	// JPG
    	ArrayList<String> list = xMSet.xU.GetFilesInDirRecursive("c:\\temp\\cmcproc",null);
    	for(int i=0;i<list.size();i++)
    	{
    		if( (list.get(i).toUpperCase().trim().endsWith("JPG")) || (list.get(i).toUpperCase().trim().endsWith("PNG")) ) {
    			grabImageMetadata(list.get(i));
    			if( i > 150 )	break;
    		}
    	}
    	
    }
   
    
}
