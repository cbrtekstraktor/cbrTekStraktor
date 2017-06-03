package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphPageObject;
import drawing.cmcKloonGraphPageObject;

public class cmcGraphPageDAORead {
	
	cmcProcSettings xMSet=null;
	cmcKloonGraphPageObject kloon = null;
    logLiason logger=null;
    
	private String sOrigFileName=null;
	private String sSourceImageFile=null;
	private String sCMXUID=null;
	private String UID=null;
	private String sOrigFileDir=null;
	private int ImageWidth=-1;
    private int ImageHeigth=-1;
    private int PayLoadX=-1;
    private int PayLoadY=-1;
    private int PayLoadWidth=-1;
    private int PayLoadHeigth=-1;
    private int FrameClusterIdx=-1;
    private int LetterClusterIdx=-1;
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
		
	//---------------------------------------------------------------------------------
	cmcGraphPageDAORead( cmcProcSettings is , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
		kloon = new cmcKloonGraphPageObject();
		purgeVariables();
	}

	//---------------------------------------------------------------------------------
	public void purgeVariables()
	//---------------------------------------------------------------------------------
	{
					sOrigFileName = null;
					sOrigFileDir=null;
					sCMXUID=null;
					UID=null;
					ImageWidth=-1;
				    ImageHeigth=-1;
				    PayLoadX=-1;
				    PayLoadY=-1;
				    PayLoadWidth=-1;
				    PayLoadHeigth=-1;
				    FrameClusterIdx=-1;
				    LetterClusterIdx=-1;
				    physicalWidthDPI=-1;
				    physicalHeigthDPI=-1;
	}
	//---------------------------------------------------------------------------------
	public String getOrigFileName()
	{
		return sOrigFileName;
	}
	public String getCMXUID()
	{
		return sCMXUID;
	}
	public String getUID()
	{
		return UID;
	}
	public String getOrigFileDir()
	{
		return sOrigFileDir;
	}
	public int getImageWidth()
	{
		return ImageWidth;
	}
	public int getImageHeigth()
	{
		return ImageHeigth;
	}
	public int getPayLoadX()
	{
		return PayLoadX;
	}
	public int getPayLoadY()
	{
		return PayLoadY;
	}
	public int getPayLoadWidth()
	{
		return PayLoadWidth;
	}
	public int getPayLoadHeigth()
	{
		return PayLoadHeigth;
	}
	public int getFrameClusterIdx()
	{
		return FrameClusterIdx;
	}
	public int getLetterClusterIdx()
	{
		return LetterClusterIdx;
	}
	public String getSourceImageFile()
	{
		return sOrigFileDir.trim() + xMSet.xU.ctSlash + sOrigFileName.trim();
	}
	public int getPhysicalWidthDPI()
	{
		return physicalWidthDPI;
	}
	public int getPhysicalHeigthDPI()
	{
		return physicalHeigthDPI;
	}
	//---------------------------------------------------------------------------------
	public cmcGraphPageObject[] readXML()
	//---------------------------------------------------------------------------------
	{
		    purgeVariables();
		    cmcGraphPageObject[] ar_pabo = null; 
			boolean isOk=true;
			String FNaam = xMSet.getGraphXML();
			if( xMSet.xU.IsBestand(FNaam) == false ) {
				do_error("Cannot find [" + FNaam + "]");
				return null;
			}
			
	        //
			int paracount=-1;
			int cococount=-1;
			boolean inParaDump=false;
			boolean inCoCoDump=false;
			try {
				File inFile  = new File(FNaam);  // File to read from.
		       	//BufferedReader reader = new BufferedReader(new FileReader(inFile));
		  	    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), xMSet.getCodePageString()));
		       	//
		       	String sLijn=null;
		       	cmcGraphPageObject[] ar_tmp = null;
		       	int lastClusterIdx=-1;
		       	boolean inco=false;
		       	while ((sLijn=reader.readLine()) != null) {
		        	//
		        	String sVal = "";
					sVal = xMSet.xU.extractXMLValue(sLijn,"FileName"); if ( sVal != null ) { sOrigFileName = sVal; }
					sVal = xMSet.xU.extractXMLValue(sLijn,"FilePath"); if ( sVal != null ) { sOrigFileDir = sVal; }
					sVal = xMSet.xU.extractXMLValue(sLijn,"CMXUID"); if ( sVal != null ) { sCMXUID = sVal; }
					sVal = xMSet.xU.extractXMLValue(sLijn,"UID"); if ( sVal != null ) { UID = sVal; }
					sVal = xMSet.xU.extractXMLValue(sLijn,"UncroppedHeigth"); if ( sVal != null ) { ImageHeigth = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"UncroppedWidth"); if ( sVal != null ) { ImageWidth = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PayLoadX"); if ( sVal != null ) { PayLoadX = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PayLoadY"); if ( sVal != null ) { PayLoadY = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PayLoadWidth"); if ( sVal != null ) { PayLoadWidth = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PayLoadHeigth"); if ( sVal != null ) { PayLoadHeigth = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PhysicalWidthDPI"); if ( sVal != null ) { physicalWidthDPI = xMSet.xU.NaarInt(sVal); }
					sVal = xMSet.xU.extractXMLValue(sLijn,"PhysicalHeigthDPI"); if ( sVal != null ) { physicalHeigthDPI = xMSet.xU.NaarInt(sVal); }
					//
					//
					if( sLijn.trim().indexOf("<FinalConnectedComponentClusters>") >=0  ) inco=true;
					if( sLijn.trim().indexOf("</FinalConnectedComponentClusters>") >=0 ) inco=false;
					if( inco ) {
					 sVal = xMSet.xU.extractXMLValue(sLijn,"ClusterIdx");if ( sVal != null ) { lastClusterIdx = xMSet.xU.NaarInt(sVal); }
					 sVal = xMSet.xU.extractXMLValue(sLijn,"ClusterType"); if( sVal != null ) {
						 if( sVal.trim().compareToIgnoreCase("FRAME") == 0 )  FrameClusterIdx = lastClusterIdx;
						 if( sVal.trim().compareToIgnoreCase("LETTER") == 0 ) LetterClusterIdx = lastClusterIdx;
					 }
					}
		
					
					//
					if( sLijn.trim().toLowerCase().contains("<connectedcomponentdump>") == true) inCoCoDump=true;
					if( sLijn.trim().toLowerCase().contains("</connectedcomponentdump>") == true) inCoCoDump=false;
					sVal = xMSet.xU.extractXMLValue(sLijn,"ConnectedComponentCount"); if ( (sVal != null) && (inCoCoDump) ) { 
						cococount = xMSet.xU.NaarInt(sVal);
						//do_log(9,"[CoCoCount=" + cococount + "]");
						ar_tmp = new cmcGraphPageObject[cococount];
						for(int i=0;i<cococount;i++) {
							ar_tmp[i] = new cmcGraphPageObject();
							ar_tmp[i].MinX = -1;
						}
						cococount=-1;
					}
					//  <!--  Valid,Minx,MinY,MaxX,MaxY,ClusterIdx,BundleIdx,IsLetter,UID 
					if( (inCoCoDump==true) && ((sLijn.startsWith("true")==true)||(sLijn.startsWith("false")==true)) ) {
						cococount++;
						if( cococount >= ar_tmp.length ) {
						    do_error("Connected Component spill over MAX=[" + ar_tmp.length + "] and now reading [" + cococount + "] [" + sLijn + "]");
						    isOk=false;
						    break;
						}
						else {
							if( sLijn.startsWith("true") == true ) ar_tmp[cococount].isValid = true; else ar_tmp[cococount].isValid = false;
							ar_tmp[cococount].MinX =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,2,',')); 
							ar_tmp[cococount].MinY =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,3,',')); 
							ar_tmp[cococount].MaxX =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,4,',')); 
							ar_tmp[cococount].MaxY =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,5,',')); 
							ar_tmp[cococount].ClusterIdx =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,6,',')); 
							ar_tmp[cococount].BundelIdx  =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,7,',')); 
							sVal = xMSet.xU.GetVeld(sLijn,8,',').trim();
							if( sVal.compareToIgnoreCase("true")==0) ar_tmp[cococount].tipe = cmcProcEnums.PageObjectType.LETTER;
							                                    else ar_tmp[cococount].tipe = cmcProcEnums.PageObjectType.NOISE; 
							ar_tmp[cococount].UID        =  xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn,9,','));
							ar_tmp[cococount].removed    = false;
						}	
					}
					
					//
					if( sLijn.trim().toLowerCase().contains("<paragraphdump>") == true) inParaDump=true;
					if( sLijn.trim().toLowerCase().contains("</paragraphdump>") == true) inParaDump=false;
					sVal = xMSet.xU.extractXMLValue(sLijn,"ParagraphCount"); 
					if ( (sVal != null) && (inParaDump) ) { 
						paracount = xMSet.xU.NaarInt(sVal);
						do_log(9,"[cococount=" + (cococount+1) + "] [paraCount=" + paracount + "]");
						ar_pabo = new cmcGraphPageObject[paracount + cococount + 1]; // cocoount = total minus 1
						for(int i=0;i<ar_pabo.length;i++) {
							ar_pabo[i] = new cmcGraphPageObject();;
							ar_pabo[i].MinX = -1;
						}
						//
						if( kloon.copy_GraphPageObject( ar_tmp , ar_pabo ) == false ) {
							do_error("Cannot copy ar_temp onto ar_pabo");
							return null;
						}
						ar_tmp=null;
						paracount = cococount;  // will be increased by 1 immediately
					}
					// - <!--  Valid,Minx,MinY,MaxX,MaxY,BundleIdx,isLetterParagraph,UID --> 
					if( (inParaDump==true) && ((sLijn.startsWith("true")==true)||(sLijn.startsWith("false")==true)) ) {
						paracount++;
						if( paracount >= ar_pabo.length ) {
						    do_error("Text Component spill over");
						    isOk=false;
						    break;
						}
						else {
						 ar_pabo[paracount].tipe = cmcProcEnums.PageObjectType.PARAGRAPH;	
						 if( sLijn.startsWith("true") == true ) ar_pabo[paracount].isValid = true; else ar_pabo[paracount].isValid = false;
						 ar_pabo[paracount].MinX =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,2,','));
						 ar_pabo[paracount].MinY =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,3,','));
						 ar_pabo[paracount].MaxX =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,4,','));
						 ar_pabo[paracount].MaxY =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,5,','));
						 ar_pabo[paracount].BundelIdx =  xMSet.xU.NaarInt(xMSet.xU.GetVeld(sLijn,6,','));
						 sVal = xMSet.xU.GetVeld(sLijn,7,',').trim();
						 if( sVal.compareToIgnoreCase("true")==0) ar_pabo[paracount].tipe = cmcProcEnums.PageObjectType.TEXTPARAGRAPH;
						 ar_pabo[paracount].UID =  xMSet.xU.NaarLong(xMSet.xU.GetVeld(sLijn,8,','));
						 sVal = xMSet.xU.GetVeld(sLijn,9,',').trim();
						 ar_pabo[paracount].removed = (sVal.compareToIgnoreCase("true")==0) ? true : false;
						}
					}
		        }
		        reader.close();
			}
			catch(Exception e) {
				do_error("Error reading [" + FNaam + "] " + xMSet.xU.LogStackTrace(e));
				return null;
			}
			// TODO de tipes plaatsen ??
			
			// post process - valdiation
			if( sCMXUID == null ) {
				do_error("Cannot determine CMXUID");
				isOk=false;
			}
			if( UID == null ) {
				do_error("Cannot determine UID");
				isOk=false;
			}
			if( sOrigFileName == null ) {
				do_error("Cannot determine FileName");
				isOk=false;
			}
			if( sOrigFileDir == null ) {
				do_error("Cannot determine FileDir");
				isOk=false;
			}
			if( (ImageHeigth < 0) || (ImageWidth < 0) || (PayLoadX < 0) || (PayLoadY < 0) || (PayLoadWidth < 0) || (PayLoadHeigth < 0) ) {
				do_error("Cannot determine ImageHeigth, ImageWidth , PayLoadX , PayLoadY , PayLoadWidth , payLoadHeigth");
				isOk=false;
			}
			if( ar_pabo == null ) {
				do_error( "Page Object array not initialized");
				isOk=false;
			}
			else {
				int j=0;
				for(int i=0;i<ar_pabo.length;i++) if( ar_pabo[i].MinX < 0) j++;
				if( j > 0) {
					do_error( "There are [" + j + "] entries in page object paragraph buffer not initialized");
					isOk=false;
				}
			}
			if( FrameClusterIdx < 0 )  {
				do_error("Frame Cluster Idx could not be found");
				isOk=false;
			}
			if( LetterClusterIdx < 0 )  {
				do_error("Letter Cluster Idx could not be found");
				isOk=false;
			}
			
			// zet de FRAME
			if( FrameClusterIdx >= 0) {
				do_log(5,"FRAME=" + FrameClusterIdx );
				for(int i=0;i<ar_pabo.length;i++) 
				{
					if( ar_pabo[i].ClusterIdx != FrameClusterIdx ) continue;
					if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) || (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
					ar_pabo[i].tipe = cmcProcEnums.PageObjectType.FRAME;
				}
			}
		    //	
			if( !isOk ) ar_pabo=null; 
			return ar_pabo;  
		}
		
	
}
