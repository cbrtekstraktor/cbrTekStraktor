package cbrTekStraktorModel;

import java.util.ArrayList;

import dao.cmcArchiveDAO;
import logger.logLiason;
import generalpurpose.gpAppendStream;
import generalpurpose.gpSAX;


public class cmcProcReadCorpus {

	boolean DEBUG = false;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	private gpSAX SAXparser = null;

	private String UID="";
	private int uncroppedwidth=-1;
	private int uncroppedheigth=-1;
    private int payloadwidth=-1;
    private int payloadheigth=-1;
    private int NbrOfElementsInLetterCluster=-1;
    private int NbrOfLettersInParagraph=0;
    private boolean isLetterParagraph=false;
    private int SomLettersInParagraaf=0;
    private int SomParagrafen=0;
    private int SomLetterParagrafen=0;
    
    private boolean monochromedetected=false;
    private int nbrOfPeaks;
    private int nbrOfValidPeaks;
    private double peakCoverage;
    private String monochromestatus;
    private String colourschema;
    
    private int MAXSTACK = 100;
    private String[] stack;
    private int stackptr=0;
    private int linesWritten=0;
    private gpAppendStream sout = null;
    private String CorpusStatDir = null;
    
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

    // ---------------------------------------------------------------------------------
	public cmcProcReadCorpus(cmcProcSettings is, logLiason ilog)
	// ---------------------------------------------------------------------------------
	{
	   xMSet = is;
	   logger = ilog;
       SAXparser = new gpSAX(this);
       stack = new String[MAXSTACK];
       //
       String FNaam = xMSet.getOverallStatFileName();
       if( xMSet.xU.IsBestand(FNaam) ) xMSet.xU.VerwijderBestand(FNaam);
       sout = new gpAppendStream(FNaam,xMSet.getCodePageString());
       //
       CorpusStatDir = xMSet.getCorpusDir() + xMSet.xU.ctSlash + "Stats";
       if( xMSet.xU.IsDir( CorpusStatDir ) == true ) {
         // Clean Stat Dir
         purgeCorpusStatDir();
         // Extract all Stat files
         extractStatFiles();
         //
         scanCorpus(CorpusStatDir);
         //
         sout.CloseAppendFile();
         //
         purgeCorpusStatDir();
       }
    }

	// ---------------------------------------------------------------------------------
	private void purgeCorpusStatDir()
	// ---------------------------------------------------------------------------------
	{
		ArrayList<String> list = xMSet.xU.GetFilesInDir(CorpusStatDir,".xml");
		for(int i=0;i<list.size();i++)
		{
			String ExtrFileName = CorpusStatDir + xMSet.xU.ctSlash + list.get(i);
			if( ExtrFileName.trim().toUpperCase().endsWith("_STAT.XML") == false )  continue;
			if( xMSet.xU.IsBestand( ExtrFileName ) == false ) continue;
		    do_log(9,"Removing [" + ExtrFileName + "]");		
            xMSet.xU.VerwijderBestand( ExtrFileName );		
		}		
	}

	// ---------------------------------------------------------------------------------
	private boolean extractStatFiles()
	// ---------------------------------------------------------------------------------
	{
		xMSet.purgeDirByName( xMSet.getTempDir() , true );
		cmcArchiveDAO xao = new cmcArchiveDAO(xMSet,logger);
		String ArcDir = xMSet.getArchiveDir();
		ArrayList<String> list = xMSet.xU.GetFilesInDir(ArcDir,".zip");
		for(int i=0;i<list.size();i++)
		{
			String ZipFileName = ArcDir + xMSet.xU.ctSlash + list.get(i);
			String StatFileName = ZipFileName; //  <short>_set.zip
			if( StatFileName == null ) continue;
			StatFileName = xMSet.xU.RemplaceerIgnoreCase( ZipFileName , "_set.zip" , "_stat.xml");
			StatFileName = xMSet.xU.getFolderOrFileName( StatFileName );
			String ExtractedFileName = xao.unzip_SingleFile( ZipFileName , StatFileName );
			if( ExtractedFileName == null ) continue;
			// move from Temp to output/stat
			String TargetFileName = CorpusStatDir + xMSet.xU.ctSlash + StatFileName;
			do_log(9,"[Extr=" + ExtractedFileName + "] [Target=" + TargetFileName + "]");
			if( xMSet.xU.IsBestand(TargetFileName) ) {
				xMSet.xU.VerwijderBestand( TargetFileName );
				if( xMSet.xU.IsBestand(TargetFileName) ) continue;
			}
			try {
				xMSet.xU.copyFile( ExtractedFileName , TargetFileName );
				xMSet.xU.VerwijderBestand( ExtractedFileName );
			}
			catch (Exception e ) {
				do_error("Cannot move [" + ExtractedFileName  + "] to [" + TargetFileName + "]");
				continue;
			}
		}		
		xao=null;
		return true;
	}
	
	// ---------------------------------------------------------------------------------
	private boolean scanCorpus(String sDir)
	// ---------------------------------------------------------------------------------
	{
		ArrayList<String> list = xMSet.xU.GetFilesInDir(sDir,".xml");
		for(int i=0;i<list.size();i++)
		{
			String FNaam = sDir + xMSet.xU.ctSlash + list.get(i);
			// DEBUG
			if( (i > 10) && (DEBUG==true) ) break;
			//
			resetVars();
			boolean ib = SAXparser.ParseXMLFile(FNaam);
			if( ib == false ) {
				do_error("Error whilst processing corpus file [" + FNaam + "]");
				continue;  // probeer volgende file
			}
			rapporteer();
			ib = checkVars();
			if( ib == false ) {
				do_error("Missing info in XML file [" + FNaam + "]");
				return false;
			}
		}
		return true;
	}
	
	// ---------------------------------------------------------------------------------
	private void resetVars()
	// ---------------------------------------------------------------------------------
	{
		for(int i=0;i<MAXSTACK;i++) stack[i]=null;
		stackptr=0;
		//
		UID="";
		uncroppedwidth=-1;
		uncroppedheigth=-1;
	    payloadwidth=-1;
	    payloadheigth=-1;
	    NbrOfElementsInLetterCluster=-1;
	    NbrOfLettersInParagraph=-1;
	    monochromedetected=false;
	    nbrOfPeaks=-1;
	    nbrOfValidPeaks=-1;
	    peakCoverage=-1;
	    monochromestatus="";
	    colourschema = ""+cmcProcEnums.ColourSchema.UNKNOWN;
	    
	    // cumulatief
	    SomLettersInParagraaf=0;
	    SomLetterParagrafen=0;
	    SomParagrafen=0;
	    
	}
	
	// ---------------------------------------------------------------------------------
	private boolean checkVars()
	// ---------------------------------------------------------------------------------
	{
		if( uncroppedwidth == -1 ) return false;
		if( uncroppedheigth == -1) return false;
	    if( payloadwidth == -1 ) return false;
	    if( payloadheigth == -1 ) return false;
	    if( NbrOfElementsInLetterCluster == -1 ) return false;
	    //
	    
	    return true;
	}
	
	// ---------------------------------------------------------------------------------
	private void rapporteer()
	// ---------------------------------------------------------------------------------
	{
		//prnt( " => Uncropped [Width:" + uncroppedwidth + "] [Heigth:" + uncroppedheigth + "]" );
	    //prnt( " => Payload [Width:" + payloadwidth + "] [Heigth:" + payloadheigth + "]" );
	    //prnt( " => Number of elements in the letter cluster [" + NbrOfElementsInLetterCluster + "]");
	    //prnt( " => Number of paragraphs [" + SomParagrafen + "]");
	    //prnt( " => Number of letter paragraphs [" + SomLetterParagrafen + "]");
	    //prnt( " => Number of letters in paragraphs [" + SomLettersInParagraaf + "]");
	    
		if (monochromestatus.length() == 0 ) monochromestatus="unknown";
		linesWritten++;
		if( linesWritten ==  1 )
		sout.AppendIt("UID,UncroppedWidth,UncroppedHeigth,PayloadWidth,PayloadHeigth,NbrOfElementsInLetterCluster,NbrOfParagraphs,NbrOFLetterParagraphs,NbrOfLetters,Colour,MonochromeDetected,MonochromeDetectionStatus,NbrPeak,NbrValidPreak,%PeakCoverage");
		int ipeakco = (int)Math.round(peakCoverage * 100);
	    sout.AppendIt("" + UID.trim().toLowerCase() + "," +
		                   uncroppedwidth + "," + uncroppedheigth + "," + 
		                   payloadwidth + "," + payloadheigth + "," + 
	    		           NbrOfElementsInLetterCluster + "," + 
		                   SomParagrafen + "," +
	                       SomLetterParagrafen + "," + 
		                   SomLettersInParagraaf + "," +
		                   colourschema.toLowerCase().trim() + ',' +
	                       monochromedetected + "," +
	                       monochromestatus.toLowerCase().trim() + ',' +
                           nbrOfPeaks + "," +
                           nbrOfValidPeaks + "," +
                           ipeakco + "%,"
                            
              );
	}
	
	//
    // XML Event Handlers
    // ---------------------------------------------------------------------------------
	public void startNode(String qName , String sContent, String sHier) 
	// ---------------------------------------------------------------------------------
	{
		push( qName );
		
		if(qName.equalsIgnoreCase("paragraph"))  {
		   isLetterParagraph=false;
		   NbrOfLettersInParagraph=-1;
		}
	}
	//
    // ---------------------------------------------------------------------------------
	public void endNode(String qName , String sContent, String sHier)
	// ---------------------------------------------------------------------------------
	{
		// bewaar de current stack en pop de stack onmiddellijk
		String currStack = getStack();
	    pop( qName );   // voor de return
	    //
	    if( currStack.equalsIgnoreCase("ComicPage|paragraphs|NumberOfElements") ) { extract( sContent , 500); return; }
	    if( currStack.equalsIgnoreCase("ComicPage|paragraphs|paragraph|NumberOfLetters") ) { extract( sContent , 501); return; }
	    if( currStack.equalsIgnoreCase("ComicPage|paragraphs|paragraph|IsLetterParagraph") ) { extract( sContent , 502); return; }
		//
		if(qName.equalsIgnoreCase("UID"))             { extract( sContent , 99); return; }
		if(qName.equalsIgnoreCase("UncroppedWidth"))  { extract( sContent , 100); return; }
		if(qName.equalsIgnoreCase("UncroppedHeigth")) { extract( sContent , 101); return; }         
		if(qName.equalsIgnoreCase("PayloadWidth"))    { extract( sContent , 102); return; }         
		if(qName.equalsIgnoreCase("PayloadHeigth"))   { extract( sContent , 103); return; }
		//
		if(qName.equalsIgnoreCase("MonochromeDetected")) { extract( sContent , 201); return; }
		if(qName.equalsIgnoreCase("NbrOfPeaks"))         { extract( sContent , 202); return; }
		if(qName.equalsIgnoreCase("NbrOfValidPeaks"))    { extract( sContent , 203); return; }
		if(qName.equalsIgnoreCase("PeakCoverage"))       { extract( sContent , 204); return; }
		if(qName.equalsIgnoreCase("MonochromeDetectedStatus"))  { extract( sContent , 205); return; }
		if(qName.equalsIgnoreCase("ColourSchema"))  { extract( sContent , 206); return; }
		//
		if(qName.equalsIgnoreCase("paragraph"))       { verwerkParagraaf(); return; }         
		
	}
	//
    // ---------------------------------------------------------------------------------
	public void extract( String sContent , int idx)
	// ---------------------------------------------------------------------------------
	{
    	String tempVal = sContent;
    	switch ( idx )
		{
		 case  99 : { UID = tempVal.trim(); break; }
	     //	
    	 case 100 : { uncroppedwidth = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 101 : { uncroppedheigth = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 102 : { payloadwidth = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 103 : { payloadheigth = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 //
    	 case 201 : { monochromedetected = xMSet.xU.ValueInBooleanValuePair("="+tempVal.trim()); break; }
    	 case 202 : { nbrOfPeaks = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 203 : { nbrOfValidPeaks = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 204 : { peakCoverage = xMSet.xU.NaarDouble(tempVal.trim()); break; }
    	 case 205 : { monochromestatus = tempVal.trim(); break; }
    	 case 206 : { colourschema = tempVal.trim(); break; }
    	 //
    	 case 500 : { NbrOfElementsInLetterCluster = xMSet.xU.NaarInt(tempVal.trim()); break; }
    	 case 501 : { NbrOfLettersInParagraph = xMSet.xU.NaarInt(tempVal.trim()); break; } 
    	 case 502 : { isLetterParagraph = xMSet.xU.ValueInBooleanValuePair("="+tempVal.trim()); break; } 
		 default  : return;
		}
	}
	
    // ---------------------------------------------------------------------------------

	private void verwerkParagraaf()
	{
		if( NbrOfLettersInParagraph == -1 ) {
			do_error("NumberOfLetters Tag not found");
			return;
		}
		SomParagrafen++;
		if( isLetterParagraph ) {
			SomLettersInParagraaf += NbrOfLettersInParagraph;
		    SomLetterParagrafen++;
		}
	}

    // ---------------------------------------------------------------------------------
	private boolean push(String sTag)
    // ---------------------------------------------------------------------------------
	{
		stack[stackptr]=sTag;
		stackptr++;
		if( stackptr >= MAXSTACK ) {
			System.err.println("MAXSTACK exceeded");
			stackptr--;
			return false;
		}
		return true;
	}

    // ---------------------------------------------------------------------------------
	private boolean pop(String sTag)
    // ---------------------------------------------------------------------------------
	{
		stack[stackptr]=null;
		stackptr--;
		if( stackptr < 0 ) {
			do_error("MAXSTACK under 0");
			stackptr=0;
			return false;
		}
		return true;
	}

    // ---------------------------------------------------------------------------------
	private String getStack()
    // ---------------------------------------------------------------------------------
	{
		String sTemp = "";
		for(int i=0;i<stackptr;i++)
		{
			if( i == 0 ) sTemp = stack[i]; else sTemp = sTemp + "|" + stack[i];
		}
		return sTemp;
	}
}
