package dao;

import generalpurpose.gpPrintStream;

import java.awt.Font;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorProject.cmcProjectCore;

public class cmcProjectDAO {

	cmcProcSettings xMSet=null;
	logLiason logger = null;

	private String ShortConfigName = "cbrTekStraktor.xml";
	
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
	public cmcProjectDAO(cmcProcSettings is, logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
	}
	
	//---------------------------------------------------------------------------------
	public cmcProjectCore readProjectXMLConfig(String ProjectFolderName)
	//---------------------------------------------------------------------------------
	{
		if (ProjectFolderName == null ) {
			do_error("Foldername is null");
			return null;
		}
		if( xMSet.xU.IsDir(ProjectFolderName) == false ) {
			do_error("Cannot acces folder of the configuration file [" + ProjectFolderName + "]");
			return null;
		}
		//
		String FConfigFileName = ProjectFolderName + xMSet.xU.ctSlash + ShortConfigName;
		if( xMSet.xU.IsBestand(FConfigFileName) == false ) {
			do_error("Cannot read configuration file [" + FConfigFileName + "]");
			return null;
		}
		//
		cmcProjectCore core = new cmcProjectCore(ProjectFolderName);
		//		
		String sXML = xMSet.xU.ReadContentFromFile(FConfigFileName,1000,"ASCII");
		int aantal = xMSet.xU.TelDelims(sXML,'\n');
		//
		boolean isOK=true;
		String reqFont=null;
		String reqCodepage=null;
		String reqBackDropType=null;
		int reqFontSize=-1;
		String reqBrowser=null;
		for(int i=0;i<aantal;i++)
		{
			String sLijn = xMSet.xU.GetVeld(sXML,(i+1),'\n');
			if( sLijn == null ) continue;
			//
			String sVal = "";
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"Name"); 
			if ( sVal != null ) { core.setProjectName(sVal); do_log(9,"Name [" + sVal + "]");}
		    //
			sVal = xMSet.xU.extractXMLValue(sLijn,"Description"); 
			if ( sVal != null ) { core.setDescription(sVal); do_log(9,"Description [" + sVal + "]");}
		    //
			sVal = xMSet.xU.extractXMLValue(sLijn,"DateFormat"); 
			if ( sVal != null ) { core.setDateformat(sVal); do_log(9,"DateFormat [" + sVal + "]");}
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"Language"); 
			if ( sVal != null ) { core.setPreferredLanguageLong(sVal); do_log(9,"Language [" + sVal + "]");}
			
			
			sVal = xMSet.xU.extractXMLValue(sLijn,"HorizontalVerticalVarianceThreshold"); 
			if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( k > 0 ) core.setHorizontalVerticalVarianceThreshold(k); do_log(9,"HorzVertVariaThreshold [" + k + "]");	}
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"MeanCharacterCount"); 
			if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( k > 0 ) core.setMeanCharacterCount(k);	do_log(9,"MeanCharCount [" + k + "]"); }
		    //
			sVal = xMSet.xU.extractXMLValue(sLijn,"LoggingLevel"); 
			if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( (k>0) && (k<10) ) core.setLogLevel(k); do_log(9,"Loglevel [" + k + "]");	}
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"Created"); 
			if ( sVal != null ) { long k = xMSet.xU.NaarLong(sVal); if ( k>0L ) core.setCreated(k); do_log(9,"Created [" + k + "]");	}
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"Updated"); 
			if ( sVal != null ) { long k = xMSet.xU.NaarLong(sVal); if ( k>0L ) core.setUpdated(k); do_log(9,"Updated [" + k + "]");	}
			
			
			// Font
			sVal = xMSet.xU.extractXMLValue(sLijn,"PreferredFont"); if ( sVal != null ) {	reqFont=sVal.trim();	}
			sVal = xMSet.xU.extractXMLValue(sLijn,"PreferredFontSize"); if ( sVal != null ) {	int k = xMSet.xU.NaarInt(sVal); if ( k > 0 ) reqFontSize=k;	}
			
			// Codepage
			sVal = xMSet.xU.extractXMLValue(sLijn,"Encoding"); if ( sVal != null ) { reqCodepage=sVal.trim();	}
			// Backdrop
			sVal = xMSet.xU.extractXMLValue(sLijn,"BackDropType"); if ( sVal != null ) { reqBackDropType=sVal.trim();	}
			//
			sVal = xMSet.xU.extractXMLValue(sLijn,"TesseractDir"); if ( sVal != null ) { core.setTesseractDir(sVal.trim());	}
			sVal = xMSet.xU.extractXMLValue(sLijn,"TesseractDirectory"); if ( sVal != null ) {	core.setTesseractDir(sVal.trim());	}
			sVal = xMSet.xU.extractXMLValue(sLijn,"TesseractFolder"); if ( sVal != null ) {	core.setTesseractDir(sVal.trim());	}
            //
			sVal = xMSet.xU.extractXMLValue(sLijn,"Browser"); if ( sVal != null ) { reqBrowser=sVal.trim();	}

		}
		// Font
		if( (reqFont !=null) && (reqFontSize>5) && (reqFontSize<20)) {
			try {
			 Font x = new Font( reqFont , Font.PLAIN, reqFontSize);
			 core.setPreferredFont(x);
			 core.setPreferredFontName(reqFont);
			 core.setPreferredFontSize(reqFontSize);
			 do_log(5,"FontSpecs [" + reqFont + " (" + reqFontSize + ") ]");
			}
			catch(Exception e) {
			  do_error("Could not load font [" + reqFont + " " + reqFontSize + "]");	
			}
		}
		// enums
		cmcProcEnums cenum = new cmcProcEnums(xMSet);
		// Codepage
		if( reqCodepage != null )
		{
			cmcProcEnums.ENCODING cp = cenum.getEncoding(reqCodepage);
			if ( cp != null ) {
				core.setEncoding(cp);
				do_log(5,"Codepage [" + cp + "]");
			}
			else {
				do_error("Coud not determine requested codepage [" + reqCodepage + "]");
				isOK=false;
			}
		}
		if( reqBackDropType != null )
		{
			cmcProcEnums.BackdropType bt = cenum.getBackdropType(reqBackDropType);
			if ( bt != null ) {
				core.setBackDropType(bt);
				do_log(5,"BackdropType [" + bt + "]");
			}
			else {
				do_error("Coud not determine requested backdrop type [" + reqBackDropType + "]");
				isOK=false;
			}
		}
		if( reqBrowser != null )
		{
			cmcProcEnums.BROWSER bt = cenum.getBrowser(reqBrowser);
			if ( bt != null ) {
				core.setBrowser(bt);
				do_log(5,"Browser [" + bt + "]");
			}
			else {
				do_error("Coud not determine requested browser [" + reqBrowser + "]");
				isOK=false;
			}
		}

		//
		cenum=null;
		if( isOK == false ) return null;
		// just a test without consequences
		if ( core.hasValidProjectCharacteristics() == false ) {
			do_error("The DAO read a project with invalid characteristics [" + core.getErrors() + "]");
		}
		else do_log(5, "Project [" + core.getProjectName() + "] has been read and is valid" );
		return core;
	}

	
	//---------------------------------------------------------------------------------
	public boolean writeConfig(cmcProjectCore proj)
	//---------------------------------------------------------------------------------
	{
		if (proj.getProjectFolderName() == null ) {
			do_error("Foldername is null");
			return false;
		}
		if( xMSet.xU.IsDir(proj.getProjectFolderName()) == false ) {
			do_error("Cannot acces folder of the configuration file [" + proj.getProjectFolderName() + "]");
			return false;
		}
		//
		String FConfigFileName = proj.getProjectFolderName() + xMSet.xU.ctSlash + ShortConfigName;
		if( xMSet.xU.IsBestand(FConfigFileName) == true ) {
			do_log(5,"ProjectConfiguration [" + FConfigFileName + "] will be overwritten");
		}
		else {
			do_log(5,"ProjectConfiguration [" + FConfigFileName + "] created");
		}
		//
		String sCreated = (proj.getCreated() < 1L) ? xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyMMddHHmmss") : ""+proj.getCreated();
		String sUpdated = xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyyMMddHHmmss");
		
//do_error("" + proj.getCreated() + " " + sCreated );
		    //
		    gpPrintStream cout = null;
			//
			cout = new gpPrintStream( FConfigFileName  , xMSet.getCodePageString() );
			cout.println (xMSet.getXMLEncodingHeaderLine());
			cout.println ("<!-- Application : " + xMSet.getApplicDesc() + " -->");
			cout.println ("<!-- File Created: " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
	        //
			cout.println ( "<cbrTekStraktor>" );
			cout.println ( "<Project>" );
			cout.println ( "<Created>" + sCreated + "</Created>"); 
			cout.println ( "<Updated>" + sUpdated + "</Updated>"); 
			//
			cout.println ( "<Name>" + proj.getProjectName() + "</Name>"); 
			cout.println ( "<Description>" + proj.getProjectDescription() + "</Description>"); 
			cout.println ( "<Dateformat>" + proj.getDateformat() + "</Dateformat>");
			cout.println ( "<Language>" + proj.getPreferredLanguageLong() + "</Language>");
			cout.println ( "<LoggingLevel>" + xMSet.getLogLevel() + "</LoggingLevel>" );
			cout.println ( "<MeanCharacterCount>" + proj.getMeanCharacterCount() + "</MeanCharacterCount>" );
			cout.println ( "<HorizontalVerticalVarianceThreshold>" + proj.getHorizontalVerticalVarianceThreshold() + "</HorizontalVerticalVarianceThreshold>" );
			cout.println ( "<PreferredFont>" + proj.getPreferredFontName() + "</PreferredFont>");
			cout.println ( "<PreferredFontSize>" + proj.getPreferredFontSize() + "</PreferredFontSize>" );
			cout.println ( "<Encoding>" + proj.getEncoding() + "</Encoding>"); 
			cout.println ( "<BackDropType>" + proj.getBackDropType() + "</BackDropType>"); 
			cout.println ( "<TesseractFolder>" + proj.getTesseractDir() + "</TesseractFolder>" );
			cout.println ( "<Browser>" + proj.getBrowser() + "</Browser>" );
			//
			cout.println ( "</Project>" );
			cout.println ( "</cbrTekStraktor>" );
			cout.close();
			//
			return true;
	}
	
	
}
