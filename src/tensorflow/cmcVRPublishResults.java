package tensorflow;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcGraphPageDAO;
import drawing.cmcGraphPageObject;

public class cmcVRPublishResults {
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	private String ErrorMsg = "";
	private cmcGraphPageObject[] ar_pabo = null;
	
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
    	ErrorMsg = sIn + "\n";
    	do_log(0,sIn);
    }
    
    //------------------------------------------------------------
    public String getErrorMessage()
    //------------------------------------------------------------
    {
      return ErrorMsg;	
    }

    // ---------------------------------------------------------------------------------
  	public cmcVRPublishResults(cmcProcSettings iM, logLiason ilog)
    // ---------------------------------------------------------------------------------
  	{
  		xMSet = iM;
 		logger = ilog;
  	}
    
    // ---------------------------------------------------------------------------------
  	private int getNumberOfChanges()
    // ---------------------------------------------------------------------------------
  	{
  		int cnt=0;
		int iter= xMSet.getScanListSize();
		for(int i=0;i<iter;i++)
		{
		  cmcVRParagraph obj = (cmcVRParagraph)xMSet.getObjectFromScanList(i);
		  if( obj.hasTipeChanged() ) cnt++;
		}
  		return cnt;
  	}
  	
  	private boolean purgeCacheDir()
  	{
		 return xMSet.purgeDirByName(xMSet.getCacheDir(),false);
  	}
  	
    // ---------------------------------------------------------------------------------
  	public boolean process_results()
    // ---------------------------------------------------------------------------------
  	{
  		// purge cache
  		if( purgeCacheDir() == false ) {
  			do_error("Cannot purge cache dir");
  			return false;
  		}
  		//
        if( this.getNumberOfChanges() <= 0 ) {
	    	do_log( 1 , "There are no changes to be processed");
	    	return true;
	    }
		//  read the archive details
		String LongFileName = xMSet.getCurrentArchiveFileName();
		cmcGraphPageDAO dao = new cmcGraphPageDAO( xMSet , LongFileName , logger );
		ar_pabo = dao.readXML();
        if( ar_pabo == null ) {
        	do_error("Could not load archive file [" + LongFileName + "]");
        	return false;
        }
        // merge changes
        boolean ok=true;
    	int iter= xMSet.getScanListSize();
		for(int i=0;i<iter;i++)
		{
		  cmcVRParagraph obj = (cmcVRParagraph)xMSet.getObjectFromScanList(i);
          //do_log(1,"" + obj.getUID() + " " + obj.getTipe() + " " + obj.getNewTipe() + " " + obj.getConfidence() + " " + obj.hasTipeChanged() );
		  if( obj.hasTipeChanged() == false ) continue;
		  if( updateParagraph( obj ) == false ) return false;
		}
  	    // store the changes to the archive
		ok=dao.updateArchiveFile(ar_pabo);
		if( ok ) {
			 cmcArchiveDAO archo = new cmcArchiveDAO(xMSet,logger);
			 ok=archo.reZipAllFiles(xMSet.getCurrentArchiveFileName());
			 archo=null;
		}
		do_log(9,"Archive Update and rezip [" + ok + "]");
		//
        ar_pabo=null;
		return ok;
  	}
  	
    // ---------------------------------------------------------------------------------
    private cmcGraphPageObject getParagraphViaUID(long uid)
    // ---------------------------------------------------------------------------------
    {
  		for(int i=0;i<ar_pabo.length;i++)
  		{
   		  if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) && (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
   		  if( ar_pabo[i].UID == uid ) return ar_pabo[i];
  		}
  		return null;
  	}
  	
 
    // ---------------------------------------------------------------------------------
    private boolean updateParagraph(cmcVRParagraph obj)
    // ---------------------------------------------------------------------------------
  	{
  	    cmcGraphPageObject pabo = getParagraphViaUID( obj.getUID() );
  	    if( pabo == null ) {
  	    	do_error("Could not locate paragraph [" + obj.getUID() + "]");
  	    	return false;
  	    }
  	    //do_log( 1 , "Updating [" + pabo.UID + " " + pabo.tipe + " -> " + obj.getNewTipe() );
  	    // update paragaph object  	    
  	    pabo.hasChanged = true;
  	    pabo.tipe = obj.getNewTipe();
  	    pabo.changetipe = (obj.getNewTipe() == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? cmcProcEnums.EditChangeType.TO_TEXT : cmcProcEnums.EditChangeType.TO_NO_TEXT;
  	    //
  		return true;
  	}
  	
  	
}
