package dao;

import generalpurpose.gpUnZipFileList;
import generalpurpose.gpZipFileList;

import java.util.ArrayList;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcSettings;

public class cmcArchiveDAO {
	
	cmcProcSettings xMSet=null;
    logLiason logger = null;
	
    private String MetaDataSourceFileName = null;
    
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
	public cmcArchiveDAO(cmcProcSettings im,logLiason ilog)
	//------------------------------------------------------------
	{
		logger = ilog;
		xMSet =im;
		MetaDataSourceFileName=null;
	}
	//------------------------------------------------------------
	private boolean purgeTempDir()
	//------------------------------------------------------------
	{
		    return xMSet.purgeDirByName(xMSet.getTempDir(),false);
	}
	
	//-----------------------------------------------------------------------
	public boolean ZipAllFiles(boolean compress)
	//-----------------------------------------------------------------------
	{
		String fo = xMSet.getOrigImageLongFileName();
		if( fo == null ) return false;
		if( fo.length() <= 0 ) return false;
		//
		ArrayList<String> flist = new ArrayList<String>();
		for(int i=0;i<100;i++)
		{
			String sFileNaam=null;
			switch(i)
			{
			case 0 : { sFileNaam = xMSet.getXMLStatFileName(); break; }
			case 1 : { sFileNaam = xMSet.getReportHTMLFileName(); break; }
			case 2 : { sFileNaam = xMSet.getLetterOutputJPGName(); break; }
			case 3 : { sFileNaam = xMSet.getReportHistoColorName(); break; }
			case 4 : { sFileNaam = xMSet.getReportHistoGrayName(); break; }
			case 5 : { sFileNaam = xMSet.getReportBoxDiagramName(); break; }
			case 6 : { sFileNaam = xMSet.getReportClusterDiagramName(); break; }
			case 7 : { 
				sFileNaam=null;
				for(int k=0;k<999;k++)
				{
					String sFn = null;
					sFn = xMSet.getTextOutputJPGName(k);
					if( xMSet.xU.IsBestand( sFn ) == false ) continue; // alles
					flist.add(sFn);
				}
				continue;
			}
			case 8 : { sFileNaam = xMSet.getMetaDataFileName(); break; }
			case 9 : { sFileNaam = xMSet.getReportPeakDiagramName(); break; }
			case 10: { sFileNaam = xMSet.getXMLLangFileName(); break; }
			case 11: { sFileNaam = xMSet.getBinarizedOutputJPGName(); break; }
			default : break;
			}
			if( sFileNaam == null ) continue;
			if( xMSet.xU.IsBestand(sFileNaam)==false ) continue;
			flist.add(sFileNaam);
		}
		//
		// zip
		if( compress ) 
		{
		  if( xMSet.xU.IsBestand(xMSet.getReportZipName()) == true ) {
				xMSet.xU.VerwijderBestand( xMSet.getReportZipName() );
		  }
		  gpZipFileList czip = new gpZipFileList( xMSet.getReportZipName() , flist , logger);
		  if( czip.completedOK() == false ) {
			  do_error("Error whilst compressing");
			  return false;
		  }
		} 
		//
		// remove
		if( (xMSet.xU.IsBestand(xMSet.getReportZipName()) == true)  ) {
		   for(int i=0;i<flist.size();i++)  
	   	   {
			if( i == 0 ) continue;  // hou STAT file
			xMSet.xU.VerwijderBestand( flist.get(i) );
		   }
		}
		flist=null;
		return true;
	}

	//-----------------------------------------------------------------------
	public String unzip_XMLs(String ZipFileNaam)
	//-----------------------------------------------------------------------
	{
				String sRet=null;
				String fo = ZipFileNaam;
				if( fo == null ) return null;
				if( xMSet.xU.IsBestand( fo ) == false ) {
					do_error("Cannot locate Zip file [" + ZipFileNaam + "]");
					return null;
				}
		        gpUnZipFileList uzip = new gpUnZipFileList( fo , xMSet.getTempDir() , null , logger); // do not remove = unzip routine
		        ArrayList<String> flist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
		        boolean isXML=false;
		        for(int i=0;i<flist.size();i++)
		        {
		        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + flist.get(i);
		        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
		        	isXML  = FNaam.toLowerCase().endsWith(".xml");
		        	if( isXML ) {
		                String FDest = null;
		                //  <file>_stat.xml <file>_lang.xml en  sMetaData.xml
		                if( flist.get(i).toUpperCase().startsWith("ZMETADATA") == false )  {
		                	// skip if there is a _VERSION_ string
		                	if( flist.get(i).toUpperCase().indexOf("_STAT_VER_") >= 0 ) continue;
		                	if( flist.get(i).toUpperCase().indexOf("_LANG_VER_") >= 0 ) continue;
		                	if( flist.get(i).toUpperCase().indexOf("_STAT.XML") >= 0 ) {
		                	  sRet = xMSet.getGraphXML();
		                	}
		                	else if( flist.get(i).toUpperCase().indexOf("_LANG.XML") >= 0 ) {
			                  sRet = xMSet.getLanguageXML();
			               	}
		                	else {
		                	 do_error("Unsupported XML in zip [" +  flist.get(i) + "]");
		                	 continue;
		                	}
		                	FDest = sRet;
		                }
		                else {
		                	FDest = xMSet.getGraphZMetaXML();
		                }
		             	try {
			                 if( xMSet.xU.IsBestand(FDest) == true ) xMSet.xU.VerwijderBestand(FDest);
			                 xMSet.xU.copyFile( FNaam , FDest );
			                }
			                catch( Exception e ) {
			                	do_error("Error moving [" + FNaam +"] to [" + FDest +"] " + e.getMessage() );
			                	return null;
			            }    
		        	}
		        	xMSet.xU.VerwijderBestand( FNaam );
		        }
		        uzip=null;
		        //DaoReady=true;
		        //CurrentZipFileName=ZipFileNaam;
		        return sRet;
	}
	
	//------------------------------------------------------------
	public void setMetaDataSourceFileName(String s)
	//------------------------------------------------------------
	{
		MetaDataSourceFileName = s;
	}
	
	//------------------------------------------------------------
	public boolean reZipAllFiles(String ZipName)
	//------------------------------------------------------------
	{
			do_log(1,"Request to rezip [" + ZipName + "]");
			//
			if( ZipName == null ) return false;
			if( xMSet.xU.IsBestand( ZipName ) == false ) return false;
	        // purge TEMP
			if( purgeTempDir() == false ) return false;
		    //
			gpUnZipFileList uzip = new gpUnZipFileList( ZipName , xMSet.getTempDir() , null , logger);
			if( uzip.UnzippedCorrectly() == false ) return false;
			uzip=null;
			
			// Do we need to change a New_GraphEditor.xml file 
			String FTestFile = xMSet.getCacheDir() + xMSet.xU.ctSlash + "New_GraphEditor.xml";	
			if( xMSet.xU.IsBestand( FTestFile ) == false ) {
			  do_log(1,"Skipping. There is no [" + FTestFile + "]");	
			}
			// rename the old file and insert new one
			else {
			 String TargetXMLName = null;
			 ArrayList<String> flist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
		     for(int i=0;i<flist.size();i++)
		     {
		        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + flist.get(i);
		        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
		        	do_log(1,"Extracted file [" + FNaam + "]");
		        	// look for _stat.xml and remove
		        	if( FNaam.trim().toUpperCase().endsWith("_STAT.XML") == false ) continue;
		        	TargetXMLName = FNaam.trim();
		        	break;
		     }
			 if( TargetXMLName == null ) {
				do_log(0,"Cannot locate main Statistics XML file");
				return false;
			 }
			 xMSet.xU.VerwijderBestand( TargetXMLName );
			 
			 // Copy Cache/grapheditor.xml to Temp/TargetXML_Version_etc
			 String PrevVersion = (TargetXMLName.substring(0 , TargetXMLName.length() - 9)) + "_Stat_Ver_" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyMMddHHmm") + ".xml";
		     String sSource = xMSet.getCacheDir() + xMSet.xU.ctSlash + "GraphEditor.xml";	
			 do_log(1,"Copy [" + sSource + "] -> [" + PrevVersion + "]");
			 try {
			  xMSet.xU.copyFile( sSource , PrevVersion );
			 }
			 catch (Exception e) {
				do_log(0,"Cannot Copy [" + sSource + "] -> [" + PrevVersion + "]" + e.getMessage());
				return false;
			 }
			 // Copy Cache/new_grapheditor.xml to Temp/TargetXML_Version
			 sSource = xMSet.getCacheDir() + xMSet.xU.ctSlash + "New_GraphEditor.xml";	
			 do_log(1,"Copy [" + sSource + "] -> [" + TargetXMLName + "]");
			 try {
				 xMSet.xU.copyFile( sSource , TargetXMLName );
			 }
			 catch (Exception e) {
				do_log(0,"Cannot Copy [" + sSource + "] -> [" + TargetXMLName + "]" + e.getMessage());
				return false;
			 }
			}
			
			// If there is a New_TextEditor.xml then replace old one
			String FNewEditor = xMSet.getLanguageNewXML();
			if( xMSet.xU.IsBestand(FNewEditor) == false ) {
				 do_log(1,"Skipping. There is no new editor file [" + FTestFile + "]");	
			}
			else {
				 String TargetXMLName = null;
				 ArrayList<String> flist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
			     for(int i=0;i<flist.size();i++)
			     {
			        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + flist.get(i);
			        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
			        	do_log(1,"Extracted file [" + FNaam + "]");
			        	// look for _Lang.xml and remove
			        	if( FNaam.trim().toUpperCase().endsWith("_LANG.XML") == false ) continue;
			        	TargetXMLName = FNaam.trim();
			        	break;
			     }
				 if( TargetXMLName == null ) {
					do_log(0,"Cannot locate current Text Editor XML file");
					return false;
				 }
				 xMSet.xU.VerwijderBestand( TargetXMLName );
				 
				 // Copy the Editor.xml from the cache into temp and increase version
				 // Copy Cache/texteditor.xml to Temp/TargetXML_Version_etc
				 String PrevVersion = (TargetXMLName.substring(0 , TargetXMLName.length() - 9)) + "_Lang_Ver_" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyMMddHHmm") + ".xml";
			     String sSource = xMSet.getCacheDir() + xMSet.xU.ctSlash + "TextEditor.xml";	
				 do_log(1,"Copy [" + sSource + "] -> [" + PrevVersion + "]");
				 try {
				   xMSet.xU.copyFile( sSource , PrevVersion );
				 }
				 catch (Exception e) {
					do_log(0,"Cannot Copy [" + sSource + "] -> [" + PrevVersion + "]" + e.getMessage());
					return false;
				 }

				 // Copy Cache/new_texteditor.xml to Temp/TargetXML_Version
				 sSource = FNewEditor;	
				 do_log(1,"Copy [" + sSource + "] -> [" + TargetXMLName + "]");
				 try {
					 xMSet.xU.copyFile( sSource , TargetXMLName );
				 }
				 catch (Exception e) {
					do_log(0,"Cannot Copy [" + sSource + "] -> [" + TargetXMLName + "]" + e.getMessage());
					return false;
				 }
			}
			
			// see if there is new metadata  file in the archive dir
			// this file acts as a flag and is set by the cmcBookMetadataDAO
			if( MetaDataSourceFileName != null ) {
			 if( xMSet.xU.IsBestand(MetaDataSourceFileName) ) {
			    // get the old MetadataFile from temp and rename
				String targetMetadata = xMSet.getTempDir() + xMSet.xU.ctSlash + xMSet.xU.getFolderOrFileName(MetaDataSourceFileName);
                if( xMSet.xU.IsBestand(targetMetadata) == false )  {
                	do_error("Cannot locate previous metadatafile [" + targetMetadata + "]");
                	return false;
                }
                // stick timestamp to old one   (strip .XML)
                String prevVersionMetadata = targetMetadata.substring(0,targetMetadata.length()-4) + "_Meta_Ver_" + xMSet.xU.prntDateTime(System.currentTimeMillis(),"yyMMddHHmm") + ".xml";
				// Create a previous version
				do_log(5,"Copying [" + targetMetadata + "] to [" + prevVersionMetadata + "]");                
				try {
					 xMSet.xU.copyFile( targetMetadata , prevVersionMetadata );
				}
				catch (Exception e) {
					do_log(0,"Cannot Copy [" + targetMetadata + "] -> [" + prevVersionMetadata + "]" + e.getMessage());
					return false;
				}
				// delete the old version
				do_log(5,"deleting [" + targetMetadata + "]");
				xMSet.xU.VerwijderBestand( targetMetadata );
				if( xMSet.xU.IsBestand(targetMetadata) == true )  {
                	do_error("Could not delete metadatafile [" + targetMetadata + "]");
                	return false;
                }
				// copying the new one in
				do_log(5,"Copying [" + MetaDataSourceFileName + "] to [" + targetMetadata + "]");
				try {
					 xMSet.xU.copyFile( MetaDataSourceFileName , targetMetadata );
				}
				catch (Exception e) {
					do_log(0,"Cannot Copy [" + MetaDataSourceFileName + "] -> [" + targetMetadata + "]" + e.getMessage());
					return false;
				}
				// deleting the source
				xMSet.xU.VerwijderBestand( MetaDataSourceFileName );
				if( xMSet.xU.IsBestand(MetaDataSourceFileName) == true )  {
                	do_error("Could not delete source metadatafile [" + MetaDataSourceFileName + "]");
                	return false;
                }
			 }
			} else do_log(5,"Skipping - there is no new metadata file");
			
		    // rezip
		    if( xMSet.xU.IsBestand(ZipName) == true ) {
					xMSet.xU.VerwijderBestand( ZipName );
			}
			ArrayList<String> templist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
			ArrayList<String> ziplist = new ArrayList<String>();
			//for(int i=0;i<flist.size();i++)
			for(int i=0;i<templist.size();i++)
		    {
		        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + templist.get(i);
		        	if( xMSet.xU.IsBestand( FNaam ) == false ) {
		        		do_error("Cannot locate file to be zipped [" + FNaam + "]");
		        		return false;
		        	}
		        	ziplist.add(FNaam);
		      }
			gpZipFileList czip = new gpZipFileList( ZipName , ziplist , logger);
			if( czip.completedOK() == false ) {
				  do_error("Error whilst compressing");
				  return false;
			}
			// purge TEMP
			if( purgeTempDir() == false ) return false;
			//
   		    do_log(5,"succesfully rezipped [" + ZipName + "]");
			return true;
	}


	//-----------------------------------------------------------------------
	public String unzip_SingleFile(String ZipFileNaam , String requestFileName)
	//-----------------------------------------------------------------------
	{
		do_log(9,"Extracting [" + requestFileName + "] from [" + requestFileName + "]");
				String sRet=null;
				String fo = ZipFileNaam;
				if( fo == null ) return null;
				if( xMSet.xU.IsBestand( fo ) == false ) {
					do_error("Cannot locate Zip file [" + ZipFileNaam + "]");
					return null;
				}
		        gpUnZipFileList uzip = new gpUnZipFileList( fo , xMSet.getTempDir() , requestFileName , logger); // do not remove = unzip routine
		        ArrayList<String> flist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
		        for(int i=0;i<flist.size();i++)
		        {
		        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + flist.get(i);
		        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
		        	sRet = FNaam;
		        }
		        uzip=null;
		        return sRet;
	}
	
	//-----------------------------------------------------------------------
	public boolean unzipFullArchive(String ZipFileNaam)
	//-----------------------------------------------------------------------
	{
		do_log(9,"Unzipping [" + ZipFileNaam + "]");
		String fo = ZipFileNaam;
		if( fo == null ) return false;
		if( xMSet.xU.IsBestand( fo ) == false ) {
					do_error("Cannot locate Zip file [" + ZipFileNaam + "]");
					return false;
		}
        gpUnZipFileList uzip = new gpUnZipFileList( fo , xMSet.getTempDir() , null , logger); // do not remove = unzip routine
        uzip=null;
        return true;
	}

	// ---------------------------------------------------------------------------------
	public boolean extractAllStatFiles(String TargetDir)
	// ---------------------------------------------------------------------------------
	{
		return extractAllFilesBySuffix(TargetDir,"_stat.xml");
	}
	// ---------------------------------------------------------------------------------
	public boolean extractAllLangFiles(String TargetDir)
	// ---------------------------------------------------------------------------------
	{
		return extractAllFilesBySuffix(TargetDir,"_lang.xml");
	}
	
	// ---------------------------------------------------------------------------------
	public boolean extractAllFilesBySuffix(String TargetDir,String Suffix)
	// ---------------------------------------------------------------------------------
	{
		xMSet.purgeDirByName( xMSet.getTempDir() , true );
		//cmcArchiveDAO xao = new cmcArchiveDAO(xMSet,logger);
		String ArcDir = xMSet.getArchiveDir();
		ArrayList<String> list = xMSet.xU.GetFilesInDir(ArcDir,".zip");
		for(int i=0;i<list.size();i++)
		{
			String ZipFileName = ArcDir + xMSet.xU.ctSlash + list.get(i);
			String StatFileName = ZipFileName; //  <short>_set.zip
			if( StatFileName == null ) continue;
			StatFileName = xMSet.xU.RemplaceerIgnoreCase( ZipFileName , "_set.zip" , Suffix);
			StatFileName = xMSet.xU.getFolderOrFileName( StatFileName );
			String ExtractedFileName = this.unzip_SingleFile( ZipFileName , StatFileName );
			if( ExtractedFileName == null ) continue;
			
			// Is it required to move to another Dir
			if( TargetDir == null ) {
				 do_log(9," => [Extr=" + ExtractedFileName + "] [Target=" + StatFileName + "]");
				continue;
			}
			if( TargetDir.trim().compareToIgnoreCase(xMSet.getTempDir()) != 0 ) 
			{
			  String TargetFileName = TargetDir + xMSet.xU.ctSlash + StatFileName;
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
		}		
		//xao=null;
		return true;
	}

}
