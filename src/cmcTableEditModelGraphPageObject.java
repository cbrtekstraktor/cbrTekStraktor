import generalImagePurpose.cmcBulkImageRoutines;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import logger.logLiason;
import textProcessing.cmcTextObject;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcGraphPageDAO;
import dao.cmcTextDAO;
import drawing.cmcGraphController;
import drawing.cmcGraphPageObject;


public class cmcTableEditModelGraphPageObject {

	cmcProcSettings xMSet = null;
	cmcGraphPageDAO dao = null;
	cmcTextDAO rao = null;
	cmcBulkImageRoutines bulk = null;
	cmcGraphController gCon=null;
	logLiason logger=null;
	
	private String ArchiveName = null;
	private cmcGraphPageObject[] ar_pabo=null;
	private cmcTextObject[] ar_text=null;
	
	private BufferedImage generalDummyImage=null;
	
    private int[] pixels=null;
    private int ImageWidth = -1;
    private int ImageHeigth = -1;
    private int PayLoadX = -1;
    private int PayLoadY = -1;
    String SourceImageFile = null;
    String CMXUID=null;
    String UID=null;
    private int maxImageWidth = 10;
	private String OriginalLanguage=null;
	
	class DisplayObject
	{
		cmcGraphPageObject obj=null;
		cmcTextObject txtObj=null;
		boolean isText;
		boolean hasBeenRemoved;
		int pabo_idx=-1;
		BufferedImage bufimg;
		int width;
		int height;
		String text;
		boolean keep=false;
		boolean tipeChanged=false;
		boolean textChanged=false;
		boolean removedChanged=false;
	}	
	private DisplayObject[] ar_pool=null;
	private DisplayObject[] ar_selection=null;
	
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
	public cmcTableEditModelGraphPageObject(cmcProcSettings is , String sIn , logLiason ilog , cmcGraphController gc)
	//---------------------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
		ArchiveName = sIn;
		gCon=gc;
		OriginalLanguage=null;
		generalDummyImage = maakDummyImage(50, 20);
		//
		boolean dataInitializedOk=false;
		if( gc==null ) {  // not  in EDIT mode eg. after OCR
			if( dao != null ) dao =  null;
	        do_log(5,"Trying to read zip file [" + ArchiveName + "]");
			dao = new cmcGraphPageDAO( xMSet , ArchiveName , logger);
			if( dao.IsDaoReady() == false ) {
			    do_error("DAO is not ready [" + ArchiveName + "]");
			    dao = null;
			}
			rao = new cmcTextDAO( xMSet , logger);
			if( (dao != null) && (rao !=null) ) {
				ar_pabo = dao.readXML();
		        ar_text = rao.readTextObjects();
		        OriginalLanguage=rao.getOriginalLanguageCode();
		    }
		    if( (ar_pabo != null) && (ar_text!=null) ) dataInitializedOk=true;
	        ImageWidth = dao.getImageWidth();
            ImageHeigth = dao.getImageHeigth();
            PayLoadX = dao.getPayLoadX();
            PayLoadY = dao.getPayLoadY();
            SourceImageFile = dao.getSourceImageFile();
            CMXUID = dao.getCMXUID();
            UID= dao.getUID();
            
    	}
		else {
			ar_pabo = gCon.getPageObjectArray();
			ar_text = gCon.getTextObjectArray();
			ImageWidth = gCon.getImageWidth();
	        ImageHeigth = gCon.getImageHeigth();
	        PayLoadX = gCon.getPayLoadX();
	        PayLoadY = gCon.getPayLoadY();
	        SourceImageFile = gCon.getSourceImageFile();
            CMXUID = gCon.getCMXUID();
            UID= gCon.getUID();
            OriginalLanguage=gCon.getOriginalLanguage();
            //
            if( (ar_pabo != null) && (ar_text!=null) ) dataInitializedOk=true;
   	    }
		//
		if( dataInitializedOk ) {
	           MakeDisplayObjects();
	           if( ar_pool != null ) {
	             createSelection();
	           }
	     }
	   
	}
	
	//---------------------------------------------------------------------------------
	public String getCMXUID()
	//---------------------------------------------------------------------------------
	{
		return CMXUID;
	}
	//---------------------------------------------------------------------------------
	public String getUID()
	//---------------------------------------------------------------------------------
	{
			return UID;
	}	
	//---------------------------------------------------------------------------------
	public int getMaxImageWidth()
	//---------------------------------------------------------------------------------
	{
		return maxImageWidth;
	}
	
	//---------------------------------------------------------------------------------
	private int getTextObjectIdxViaUID(long UID)
	//---------------------------------------------------------------------------------
	{
		if( ar_text == null ) return -1;
		for(int i=0;i<ar_text.length;i++)
		{
			if( ar_text[i].UID == UID ) return i;
		}
		return -1;
	}
	
	//---------------------------------------------------------------------------------
	private void MakeDisplayObjects()
	//---------------------------------------------------------------------------------
	{
		//
		ar_pool = new DisplayObject[ar_pabo.length];
		for(int i=0;i<ar_pabo.length;i++)
		{
			DisplayObject x = new DisplayObject();
			x.obj = ar_pabo[i];
			x.pabo_idx = i;
			x.width = ar_pabo[i].MaxX - ar_pabo[i].MinX + 1;
			x.height = ar_pabo[i].MaxY - ar_pabo[i].MinY + 1;
			x.isText = (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? true : false;
			x.hasBeenRemoved = ar_pabo[i].removed;
			
			x.keep = false;
			x.bufimg = null;
			//
			int tix = getTextObjectIdxViaUID(ar_pabo[i].UID);
			if( tix >= 0 ) {
				x.txtObj = ar_text[tix];
				x.text = ar_text[tix].TextFrom;
			}
			else {
				// 
				if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) || (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH)) {
				  do_error("Could not find text object for text paragraph [" + ar_pabo[i].UID + "] adding it");
				  cmcTextObject y = new cmcTextObject();
				  y.BundelIdx = ar_pabo[i].BundelIdx;
				  y.UID=ar_pabo[i].UID;
				  y.removed=false;
				  //
				  x.txtObj = y;
				  x.text = "";
				}
				else {
				 x.txtObj = null;
				 x.text = "";
				}
			}
			//
			ar_pool[i] = x;
		}
		do_log(5,"Pooled [" + ar_pool.length + "]");
	}
	
	//---------------------------------------------------------------------------------
	public void createSelection()
	//---------------------------------------------------------------------------------
	{
		if( ar_pool == null ) return;
		ar_selection=null;
		//
		cmcProcEnums.QUICKEDITOPTIONS optionSelected= xMSet.getQuickEditOption();
		// perform selection
		int aantal = 0;
		for(int i=0;i<ar_pool.length;i++)
		{
			ar_pool[i].keep = false;
			//
			//if( ar_pool[i].obj.removed ) continue;
			cmcProcEnums.PageObjectType tipe = ar_pabo[i].tipe;
			switch( optionSelected )
			{
			case TEXT_AREAS: {if ( tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) continue; break;}
			case POTENTIAL_TEXT_AREAS: {if ( (tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH)&&((tipe != cmcProcEnums.PageObjectType.PARAGRAPH)) ) continue; break;}
			case FRAMES : {if ( tipe != cmcProcEnums.PageObjectType.FRAME ) continue; break;}
			case TEXT_BUBBLES : continue; // todo
			case LETTER : {if ( tipe != cmcProcEnums.PageObjectType.LETTER ) continue; break;}
			case NOISE : {if ( tipe != cmcProcEnums.PageObjectType.NOISE ) continue; break;}
			default : break;
			}
		    //	
			ar_pool[i].keep = true;
			aantal++;
		}
		
		// duplicate
		ar_selection = new DisplayObject[aantal];
		int teller=0;
		maxImageWidth=-1;
        for(int i=0;i<ar_pool.length;i++)
        {
        	if( ar_pool[i].keep == false ) continue;
    		//
    		DisplayObject x = new DisplayObject();
    		x.obj      = ar_pool[i].obj;
    		x.txtObj   = ar_pool[i].txtObj;
    		x.pabo_idx = ar_pool[i].pabo_idx;
    		x.bufimg   = null;
    		x.width    = ar_pool[i].width;
    		x.height   = ar_pool[i].height;
    		x.isText   = ar_pool[i].isText;
    		x.text           = ar_pool[i].text;
    		x.hasBeenRemoved = ar_pool[i].hasBeenRemoved;
    		x.keep           = ar_pool[i].keep;
    		//
    		ar_selection[teller] = x;
    		teller++;
    		//
    		if( maxImageWidth < x.width ) maxImageWidth = x.width;
        }
      	do_log(5,"Selected [" + optionSelected + " -> " + ar_selection.length + "]");
        //
	    initialiseerPixels();
        extractImages();
    }

	//---------------------------------------------------------------------------------
	public int getRowCount()
	//---------------------------------------------------------------------------------
	{
		if( ar_selection == null ) return 0;
		return ar_selection.length;
	}
	
	//---------------------------------------------------------------------------------
	public String getColumnName(int column)
	//---------------------------------------------------------------------------------
	{
	  //return this.headerLables[column];	
	  return ""+cmcProcEnums.QUICKCOLS.values()[column];
	}
	
	//---------------------------------------------------------------------------------
	public int getColumnCount()
	//---------------------------------------------------------------------------------
	{
		 //return this.headerLables.length;
		 return cmcProcEnums.QUICKCOLS.values().length;
	}
	
	//---------------------------------------------------------------------------------
	public Class getColumnClass(int column) 
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<cmcProcEnums.QUICKCOLS.values().length;i++)
		{
			if( column != i ) continue;
			switch( cmcProcEnums.QUICKCOLS.values()[i])
			{
			case UID : return Integer.class;
			case IMAGE : return ImageIcon.class;
			case WIDTH : return Integer.class;
			case HEIGHT : return Integer.class;
			case ISTEXT : return Boolean.class;
			case EXTRACTED_TEXT : return String.class;
			case REMOVED : return Boolean.class;
			case ELEMENTS : return Integer.class;
			case LETTERS : return Integer.class;
			default : return String.class;
			}
		}
		return String.class;
	}
	
	//---------------------------------------------------------------------------------
	public Object getItem(int row , int column ) 
	//---------------------------------------------------------------------------------
	{
		try {
		if( (row < 0)|| (row >= getRowCount()) ) return "";
		if( (column < 0) ||( column >= cmcProcEnums.QUICKCOLS.values().length) ) return "";
	    switch( cmcProcEnums.QUICKCOLS.values()[column])
	    {
	    case UID : return ar_selection[row].obj.UID;
	    case IMAGE : return new ImageIcon(ar_selection[row].bufimg);
	    case WIDTH : return ar_selection[row].width;
	    case HEIGHT : return ar_selection[row].height;
	    case ISTEXT : return ar_selection[row].isText;
	    case EXTRACTED_TEXT : {
	    	// if in edit mode and this is the current row the do not add HTML
	    	if( xMSet.getQuickEditRequestedRow() == row ) return ar_selection[row].text;
	    	return "<html>" + ar_selection[row].text + "</html>";   // for some reason adding HMTL results in text wrapping
	    }
	    case REMOVED : return ar_selection[row].hasBeenRemoved;
	    case CLASSIFICATION : return ar_selection[row].obj.tipe;
	    case CHANGETYPE : return ar_selection[row].obj.changetipe;
	    case ELEMENTS : return 0;
	    case LETTERS : return 0;
	    default : return "";
	    }
		}
		catch(Exception e) {
			do_error("Got a weird error " + e.getMessage());
			return "";
		}
	}

	//-----------------------------------------------------------------------
	private BufferedImage maakDummyImage(int ibreedte , int ihoogte)
	//-----------------------------------------------------------------------
	{
				int breedte = 30;
				int hoogte = 20;
				//
				boolean dodef = false;
				if( ibreedte <= 0 ) dodef=true;
				if( ihoogte <= 0 ) dodef=true;
				if( ibreedte * ihoogte <= 0 ) dodef=true;
				if( dodef == false ) {
					breedte = ibreedte;
					hoogte = ihoogte;
				}
				//
				int[] zixels = new int[breedte*hoogte];
				for(int i=0;i<zixels.length;i++) zixels[i] = 0xff00ff00;
				BufferedImage dummyImage=null;
			    dummyImage = new BufferedImage(breedte , hoogte, BufferedImage.TYPE_INT_RGB);
			    dummyImage.setRGB(0, 0, breedte, hoogte, zixels, 0, breedte);
			    return dummyImage;
	}
	
	//---------------------------------------------------------------------------------
	public boolean initialiseerPixels()
	//---------------------------------------------------------------------------------
	{
			long startt = System.currentTimeMillis();
		    Image img=null;
		    try {
			    URL url = new URL("file:///" + SourceImageFile);
		        img = ImageIO.read(url);
		    }
		    catch(Exception e )
		    {
			   do_error("Reading imagefile [" + SourceImageFile + "] " + e.getMessage());
			   return false;
		    }
		    //
		    int w = img.getWidth(null);
		    int h = img.getHeight(null);
		    if( (h!=this.ImageHeigth) || (w!=this.ImageWidth)) {
		    	do_error("Image dimension does not match XML content");
		    	return false;
		    }
		    //
		    pixels=null;
		    //workPixels=null;
		    pixels = new int[ w * h];
		    PixelGrabber pg = new PixelGrabber( img , 0 , 0 , w , h , pixels , 0 , w );
		    try {
		    	pg.grabPixels();
		    }
		    catch(Exception e )
		    {
			   do_error("Pixelgrabber [" + SourceImageFile + "] " + e.getMessage());
			   return false;
		    }
		    //
		    img=null;
		    //
		    do_log(5,"Pixels grabbed in " + (System.currentTimeMillis()-startt) + " msec");
		    //
		    return true;
	}
		
	//---------------------------------------------------------------------------------
	public int[] getPixels(int idx)
	//---------------------------------------------------------------------------------
	{
		  if( (idx<0) || (idx>=ar_pabo.length)) return null;
		  //	
		  int breedte = ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
		  int hoogte  = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
		  int[] ret = new int[ breedte * hoogte ];
		  int teller=0;
	      for(int y=0;y<hoogte;y++)
		  {
				for(int x=0;x<breedte;x++)
				{
					int p = (x + PayLoadX + ar_pabo[idx].MinX) + (( y + PayLoadY + ar_pabo[idx].MinY) * ImageWidth );
					if( p >= pixels.length ) {
						do_error("overflow " + x + " " + y + " " + p );
						return null;
					}
					ret[teller] = pixels[p];
					teller++;
				}
		   }
	       // if removed - cross it
	       if ( ar_pabo[idx].removed ) ret = diagonalCross(ret,breedte);
		   return ret;
	}
	
	//---------------------------------------------------------------------------------
	private int[] diagonalCross(int[] in, int breedte)
	//---------------------------------------------------------------------------------
	{
		int KLEUR = 0xffff0000;
		int[] ret = Arrays.copyOf(in, in.length);
		try {
		  int hoogte = in.length / breedte;
		  double slope = (double)hoogte / (double)breedte;
		  for(int i=0;i<breedte;i++)
		  {
			  int j = ((int)((double)i * slope) * breedte) + i;
			  ret[j] = KLEUR;
			  j -= breedte;
			  if( j >= 0 ) ret[j] = KLEUR;
			  j += breedte;
			  if( j < ret.length ) ret[j] = KLEUR;
			  //
			  j = (((hoogte-1) - (int)((double)i * slope)) * breedte) + i;
			  ret[j] = KLEUR;
			  j -= breedte;
			  if( j >= 0 ) ret[j] = KLEUR;
			  j += breedte;
			  if( j < ret.length ) ret[j] = KLEUR;
		  }
		}
		catch(Exception e) {
			do_error("diagonalCross " + e.getMessage());
		}
		return ret;
	}

	//---------------------------------------------------------------------------------
	private int[] surround(int[] in , int oribreedte , int edge)
	//---------------------------------------------------------------------------------
	{
		int KLEUR = 0xffffffff;
		int orihoogte = in.length / oribreedte;
	    int hoogte = orihoogte + (edge*2);
	    int breedte = oribreedte + (edge*2);
	    int[] ret = new int[hoogte * breedte];
	    for(int i=0;i<ret.length;i++) ret[i]= KLEUR;
	    for(int i=0;i<orihoogte;i++)
	    {
	    	int small = i * oribreedte;
	    	int large = (i+edge) * breedte;
	    	for(int j=0;j<oribreedte;j++)
	    	{
	    		ret[j+large+edge] = in[j+small];
	    	}
	    }
		return ret;
	}
	
	//---------------------------------------------------------------------------------
	private void extractImages()
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<ar_selection.length;i++)
		{
			ar_selection[i].bufimg = null;
			int idx = ar_selection[i].pabo_idx;
			try {
			 int breedte = ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
			 int hoogte  = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
			 if( breedte * hoogte <= 0 ) continue;
			 int[] zixels = getPixels(idx);
			 if ( zixels == null ) continue;
			 if( xMSet.getuseMonoChromeInDialogs() ) {
				 if ( bulk == null ) bulk = new cmcBulkImageRoutines(xMSet,logger);
					int[] zoxels = Arrays.copyOf(zixels, zixels.length);
					zixels = bulk.doNiblak(zoxels, breedte , true);
					zoxels=null;
			 }
			 int edge = (cmcProcConstants.QUICKEDITBORDER * 3) / 2;
			 int[] bigger = surround( zixels , breedte, edge);
			 ar_selection[i].bufimg = new BufferedImage(breedte+(2*edge) , hoogte+(2*edge), BufferedImage.TYPE_INT_RGB);
			 ar_selection[i].bufimg.setRGB(0, 0, breedte+(2*edge), hoogte+(2*edge), bigger, 0, breedte+(2*edge));
			}
			catch(Exception e) {
			  do_error("Exception Creating buffered image [idx=" + idx + "]");	
			}
			if( ar_selection[i].bufimg == null ) ar_selection[i].bufimg = generalDummyImage;
		}
	}

	//---------------------------------------------------------------------------------
	public boolean toggleIsText(int row)
	//---------------------------------------------------------------------------------
	{
		if( (row<0)||(row>=ar_selection.length)) return false;
		int idx = ar_selection[row].pabo_idx;
		if( (idx<0)||(idx>=ar_pool.length)) return false;
		ar_pool[idx].isText = !ar_pool[idx].isText;
		ar_pool[idx].tipeChanged=true;
		ar_selection[row].isText = ar_pool[idx].isText;
		do_log(1,"Toggled [" + ar_pool[idx].isText + "]");
		return true;
	}

	//---------------------------------------------------------------------------------
	public boolean toggleRemoved(int row)
	//---------------------------------------------------------------------------------
	{
		if( (row<0)||(row>=ar_selection.length)) return false;
		int idx = ar_selection[row].pabo_idx;
		if( (idx<0)||(idx>=ar_pool.length)) return false;
		ar_pool[idx].hasBeenRemoved = !ar_pool[idx].hasBeenRemoved;
		ar_pool[idx].removedChanged=true;
		ar_selection[row].hasBeenRemoved = ar_pool[idx].hasBeenRemoved;
		return true;
	}

	//---------------------------------------------------------------------------------
	public boolean setText(int row,String sin)
	//---------------------------------------------------------------------------------
	{
		if( (row<0)||(row>=ar_selection.length)) return false;
		int idx = ar_selection[row].pabo_idx;
		if( (idx<0)||(idx>=ar_pool.length)) return false;
		ar_pool[idx].text = sin;
		ar_pool[idx].textChanged=true;
		ar_selection[row].text = ar_pool[idx].text;
		return true;
	}

	//---------------------------------------------------------------------------------
	public boolean propagateChanges(cmcGraphController gCon)
	//---------------------------------------------------------------------------------
	{
		if( gCon == null ) {
			do_error("grapController not initialized");
			return false;
		}
		// text togggle
		for(int i=0;i<ar_pool.length;i++)
		{
			if( !ar_pool[i].tipeChanged ) continue;
			boolean prev = (ar_pool[i].obj.tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ? true : false;
			if( ar_pool[i].isText == prev ) continue;
			int idx = ar_pool[i].pabo_idx;
			do_log(1,"Flush isText change on [idx=" + idx + "]");
			boolean ib = gCon.updateTextIndicator(idx,ar_pool[i].isText);
			if( ib == false ) {
				do_error("Could not udpate isText flag [idx=" + idx + "]");
				return false;
			}
		}
		// changes on the remove checkbox
		for(int i=0;i<ar_pool.length;i++)
		{
			if( !ar_pool[i].removedChanged ) continue;
			boolean prev = ar_pool[i].obj.removed;
			if( ar_pool[i].hasBeenRemoved == prev ) continue;
			int idx = ar_pool[i].pabo_idx;
			do_log(1,"Flush REMOVED change on [idx=" + idx + "]");
			boolean ib=false;
			if( ar_pool[i].hasBeenRemoved ) ib = gCon.removeViaIdx(idx);
			                           else ib = gCon.undoRemoveViaIdx(idx);
			if( ib == false ) {
				do_error("Could not remove idx=" + idx);
				return false;
			}
		}		
		// text
		for(int i=0;i<ar_pool.length;i++)
		{
			if( !ar_pool[i].textChanged ) continue;
			int idx = ar_pool[i].pabo_idx;
			String prev = ar_pool[i].txtObj.TextFrom;
			String curr = ar_pool[i].text;
			if( prev.trim().compareTo(curr.trim()) == 0 ) continue;
			do_log(1,"Flush Text change on [idx=" + idx + "] " + ar_pool[i].text);
	        //		
			gCon.updateOrigTextViaUID( ar_pool[i].txtObj.UID , ar_pool[i].text);
		}
		//
		return true;
	}
	
	// when not in EDIT mode - only the text can be updated
	//---------------------------------------------------------------------------------
	public boolean propagateChangesAfterOCR()
	//---------------------------------------------------------------------------------
	{
		int changes=0;
		boolean isOK = true;
		for(int i=0;i<ar_pool.length;i++)
		{
			if( !ar_pool[i].textChanged ) continue;
			//int idx = ar_pool[i].pabo_idx;
			String prev = ar_pool[i].txtObj.TextFrom;
			String curr = ar_pool[i].text;
			if( prev.trim().compareTo(curr.trim()) == 0 ) continue;
			if( localUpdateOrigTextViaUID( ar_pool[i].txtObj.UID , ar_pool[i].text ) == false ) {
				isOK=false;
				continue;
			}
			changes++;
		}
		if( (isOK==true) && (changes>0 )) {
			isOK = flushTextChanges();
		}
        return isOK;
	}

	//---------------------------------------------------------------------------------
	private boolean localUpdateOrigTextViaUID( long iuid , String sText)
	//---------------------------------------------------------------------------------
	{
		int idx=-1;
		for(int i=0;i<ar_text.length;i++)
		{
			if( ar_text[i].UID != iuid) continue;
			idx=i;
			break;
		}
		if( idx < 0) {
			do_error("Cannot locate text with UID [" + iuid + "] to change to [" + sText + "]");
			return false;
		}
		//
		do_log(1,"Flush Text change on [idx=" + idx + "] [UID=" + iuid + "] [From=" + ar_text[idx].TextFrom + "] [To=" + sText + "]");
		//ar_text[idx].TextOCR=sText;
		ar_text[idx].TextFrom=sText;
		ar_text[idx].changeDate=System.currentTimeMillis();
		ar_text[idx].hasChanged=true;
		//  
		return true;
	}

	//---------------------------------------------------------------------------------
	private boolean flushTextChanges()
	//---------------------------------------------------------------------------------
	{
        do_log(1,"Request to flush text changes");
        //
        if( OriginalLanguage == null ) {
        	do_error("Cannot flush changes to Text DAO - Original Language is NULL");
        	return false;
        }
        //
 		cmcTextDAO rao = new cmcTextDAO(xMSet,logger);
 		if( rao.flushChangesToXML(ar_text , OriginalLanguage) == false ) return false;
 		rao=null;
 		do_log(5,"Changes flushed to text XML in cache");
 		// 
 		cmcArchiveDAO archo = new cmcArchiveDAO(xMSet,logger);
 		return archo.reZipAllFiles(xMSet.getCurrentArchiveFileName());
   }
	
}
