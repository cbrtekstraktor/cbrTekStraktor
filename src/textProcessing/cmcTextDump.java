package textProcessing;

import logger.logLiason;
import generalpurpose.gpAppendStream;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;
import dao.cmcTextDAO;
import drawing.cmcGraphPageObject;



public class cmcTextDump {

	cmcProcSettings xMSet=null;
	logLiason logger=null;
	
	//-----------------------------------------------------------------------
	public cmcTextDump( cmcProcSettings xi , logLiason ilog)
	//-----------------------------------------------------------------------
	{
		xMSet=xi;
		logger=ilog;
	}

	//-----------------------------------------------------------------------
	public boolean create(cmcGraphPageObject[] ar,comicPage cpi , String Language)
	//-----------------------------------------------------------------------
	{
		if( ar == null ) return false;
		//
		int aantal =0;
		for(int i=0;i<ar.length;i++)
		{   // need to dump text and non text paragraph - because tipes can manually be changed
			if( (ar[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
			aantal++;
		}
		cmcTextObject[] ar_text = new cmcTextObject[aantal];
		int teller=0;
		for(int i=0;i<ar.length;i++)
		{
			if( (ar[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
		    //	
			cmcTextObject x = new cmcTextObject();
			x.UID = ar[i].UID;
			x.BundelIdx = ar[i].BundelIdx;
			x.removed = ar[i].removed;
			x.confidence = (ar[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? cmcProcEnums.TextConfidence.TEXT : cmcProcEnums.TextConfidence.NO_TEXT;
			//
			ar_text[teller] = x;
			teller++;
  		}
	    //	
		cmcTextDAO dao = new cmcTextDAO(xMSet,logger);
		return dao.createEmptyXMLFile(ar_text, cpi, Language);
	}
	
}
