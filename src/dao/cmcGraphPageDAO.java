package dao;

import generalpurpose.gpUnZipFileList;

import java.util.ArrayList;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphPageObject;

public class cmcGraphPageDAO {
	
	cmcProcSettings xMSet=null;
    cmcGraphPageDAORead reader = null;
    cmcGraphPageDAOWrite writer = null;
    logLiason logger=null;
    
    //private String StatXMLFileName = null;
    private cmcGraphPageObject[] ar_pabo = null;
    
    private boolean DaoReady=false;
    private String CurrentZipFileName=null;
    
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

	//-----------------------------------------------------------------------
	public cmcGraphPageDAO(cmcProcSettings is, String zipfile , logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet = is;
		logger=ilog;
		reader = new cmcGraphPageDAORead(xMSet,logger);
		writer = new cmcGraphPageDAOWrite(xMSet,logger);
		CurrentZipFileName=null;
		if( zipfile != null ) {
			//StatXMLFileName = unzip_XMLs(zipfile);
			do_log(1,"Extracting archive [" + zipfile + "]");
			cmcArchiveDAO archo = new cmcArchiveDAO(xMSet,logger);
			//String ret = archo.unzip_XMLs(zipfile);
	//System.err.println("Extracted " + StatXMLFileName );
			if( archo.unzip_XMLs(zipfile) != null ) {
			  CurrentZipFileName=zipfile;
			  DaoReady = true;
	//System.err.println("Got ->" + StatXMLFileName + " " + CurrentZipFileName );
			}
			archo = null;
		}
	}
	
	/*
	//-----------------------------------------------------------------------
	public String getStatXMLFileName()
	//-----------------------------------------------------------------------
	{
	   return StatXMLFileName;
	}
	*/
	//---------------------------------------------------------------------------------
	public String getOrigFileName()
	{
		return reader.getOrigFileName();
	}
	public String getCMXUID()
	{
		return reader.getCMXUID();
	}
	public String getUID()
	{
		return reader.getUID();
	}
	public String getOrigFileDir()
	{
		return reader.getOrigFileDir();
	}
	public int getImageWidth()
	{
		return reader.getImageWidth();
	}
	public int getImageHeigth()
	{
		return reader.getImageHeigth();
	}
	public int getPayLoadX()
	{
		return reader.getPayLoadX();
	}
	public int getPayLoadY()
	{
		return reader.getPayLoadY();
	}
	public int getPayLoadWidth()
	{
		return reader.getPayLoadWidth();
	}
	public int getPayLoadHeigth()
	{
		return reader.getPayLoadHeigth();
	}
	public int getFrameClusterIdx()
	{
		return reader.getFrameClusterIdx();
	}
	public int getLetterClusterIdx()
	{
		return reader.getLetterClusterIdx();
	}
	public String getSourceImageFile()
	{
		return reader.getSourceImageFile();
	}
	public int getPhysicalWidthDPI()
	{
		return reader.getPhysicalWidthDPI();
	}
	public int getPhysicalHeigthDPI()
	{
		return reader.getPhysicalHeigthDPI();
	}
	//---------------------------------------------------------------------------------
			
	/*	
	//-----------------------------------------------------------------------
	private String unzip_XMLs(String ZipFileNaam)
	//-----------------------------------------------------------------------
	{
					String sRet=null;
					String fo = ZipFileNaam;
					if( fo == null ) return null;
					if( xMSet.xU.IsBestand( fo ) == false ) {
						System.err.println("Cannot locate Zip file [" + ZipFileNaam + "]");
						return null;
					}
			        gpUnZipFileList uzip = new gpUnZipFileList( fo , xMSet.getTempDir() , null );
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
			                	if( flist.get(i).toUpperCase().indexOf("_VERSION_") >= 0 ) continue;
			                	if( flist.get(i).toUpperCase().indexOf("_STAT.XML") >= 0 ) {
			                	  sRet = xMSet.getGraphXML();
			                	}
			                	else if( flist.get(i).toUpperCase().indexOf("_LANG.XML") >= 0 ) {
				                  sRet = xMSet.getLanguageXML();
				               	}
			                	else {
			                		System.err.println("Unsupported XML in zip [" +  flist.get(i) + "]");
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
				                	System.err.println("Error moving [" + FNaam +"] to [" + FDest +"] " + e.getMessage() );
				                	return null;
				            }    
			        	}
			        	xMSet.xU.VerwijderBestand( FNaam );
			        }
			        uzip=null;
			        DaoReady=true;
			        CurrentZipFileName=ZipFileNaam;
			        return sRet;
	}
*/
	//---------------------------------------------------------------------------------
	public boolean IsDaoReady()
	//---------------------------------------------------------------------------------
	{
		return DaoReady;
	}
	
	/*
	//-----------------------------------------------------------------------
	public boolean readGraphPageObjectByname(String FNaam)
	//-----------------------------------------------------------------------
	{
		if( FNaam != null ) {
			//String ff = unzip_XMLs(FNaam);
			cmcArchiveDAO archo = new cmcArchiveDAO(xMSet);
			String ff = archo.unzip_XMLs(FNaam);
			if( ff == null ) return false;
		}
		ar_pabo = reader.readXML();
	    System.out.println("Read [" + getNumberOfRows() + "]");
		return true;
	}
	*/
	//---------------------------------------------------------------------------------
	public cmcGraphPageObject[] readXML()
	//---------------------------------------------------------------------------------
	{
		return reader.readXML();
	}

	//---------------------------------------------------------------------------------
	public boolean updateArchiveFile(cmcGraphPageObject[] ar_pabo)
	//---------------------------------------------------------------------------------
	{
		return writer.updateArchiveFile(ar_pabo);
	}
	/*
	//---------------------------------------------------------------------------------
	public boolean reZipAllFiles(String ZipFileName)
	//---------------------------------------------------------------------------------
	{
		String FNaam = CurrentZipFileName == null ? "null" : CurrentZipFileName;
		if(  ZipFileName.trim().compareToIgnoreCase(FNaam) != 0 ) {
			System.err.println("Current Archive [" + FNaam + "] and request to update [" + ZipFileName + "]");
			return false;
		}
		cmcArchiveDAO archo = new cmcArchiveDAO(xMSet);
		boolean ib = archo.reZipAllFiles(ZipFileName);
		archo=null;
		return ib;
		//return writer.reZipAllFiles(ZipFileName);
	}
	*/
	//-----------------------------------------------------------------------
	public int getNumberOfRows()
	//-----------------------------------------------------------------------
	{
		return ar_pabo == null ? 0 : ar_pabo.length;
	}

	//---------------------------------------------------------------------------------
	public String getItem(int row , int col)
	//---------------------------------------------------------------------------------
	{
		if( ar_pabo == null ) return "";
		if( (row<0)|| (row>=getNumberOfRows()) ) return "?";
		switch( col )
		{
		case 0 : return ""+ar_pabo[row].UID;
		case 1 : return ""+ar_pabo[row].ClusterIdx;
		case 2 : return ""+ar_pabo[row].tipe;
		case 3 : return ""+(ar_pabo[row].MaxX - ar_pabo[row].MinX + 1);
		case 4 : return ""+(ar_pabo[row].MaxY - ar_pabo[row].MinY + 1);
		default : return ""+ar_pabo[row].changetipe;
		}
	}
	

	
}
