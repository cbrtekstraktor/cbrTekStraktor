package drawing;

import generalImagePurpose.cmcBulkImageRoutines;
import generalImagePurpose.cmcImageRoutines;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.PixelGrabber;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import logger.logLiason;
import textProcessing.cmcTextObject;
import textProcessing.kloonTextObject;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcArchiveDAO;
import dao.cmcBookMetaDataDAO;
import dao.cmcGraphPageDAO;
import dao.cmcTextDAO;

public class cmcGraphController {

	
	enum drawType { UNKNOWN , LINE , RECTANGLE , FILLEDRECTANGLE , HANDLEDRECTANGLE , THICKRECTANGLE , TEXT}
	
	
	//
	cmcProcSettings xMSet=null;
	cmcKloonGraphPageObject kloon = null;
	cmcGraphPageDAO dao=null;
	logLiason logger=null;
	//
	private cmcImageRoutines iRout=null;
	private cmcBulkImageRoutines ibulk=null;
	private cmcColor iKol= null;
	
	private String sOrigFileName=null;
	private String sCMXUID=null;
	private String sUID=null;
	private String sOrigFileDir=null;
	private String sSourceImageFile=null;
	private String sWorkImageFile=null;
	private String prevFileName="??";
	private String OriginalLanguage="";
    private int ImageWidth=-1;
    private int ImageHeigth=-1;
    private int PayLoadX=-1;
    private int PayLoadY=-1;
    private int PayLoadWidth=-1;
    private int PayLoadHeigth=-1;
    private int[] pixels=null;
    private int[] workPixels=null;
    private int FrameClusterIdx=-1;
    private int LetterClusterIdx=-1;
    private long lastModifiedDate;
    private String CurrentZipFileName=null;
    private boolean ignoreChanges=false;
    
  
    // settings	
    private cmcProcEnums.BackdropType backdropTipe = cmcProcEnums.BackdropType.BLEACHED;
    private cmcProcEnums.WiskerColor wiskerTipe = cmcProcEnums.WiskerColor.CYAN;
	private boolean shoPayloadBoundaries=true;
	private boolean shoFrames=false;
	private boolean shoTextParagraphs=true;
	private boolean shoParagraphs=true;
	private boolean shoCharacters=false;
	private boolean shoNoise=false;
	private boolean shoValids=true;
	private boolean shoInvalids=false;
	//
	private int oidteller=0;
	private int[] wiskerOID = new int[12];
	private int draggerOID=-1;
	private int draggerLineOID=-1;
	
    
	// maintains a stack of objects to be drawn on the canvas - triggered via repaint
	private ArrayList<cmcDrawingObject> stack = null;
	// page object array 
	private cmcGraphPageObject[] ar_pabo=null;
	// text
	private cmcTextObject[] ar_text=null;
	
	class cartouche
	{
		int topObject=-1;
		int topObjectOID=-1;
        int[] graphElementsOID = null;
        int[] childObjects = null;
        int[] childObjectsOID = null;
        
        cartouche(int idx)
        {
        	topObject = idx;
        	graphElementsOID = new int[100];
        	for (int i=0;i<graphElementsOID.length;i++) graphElementsOID[i]=-1;
        }
	}
	private cartouche popupOID = null;
		
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//---------------------------------------------------------------------------------
	private void do_error(String sIn)
	//---------------------------------------------------------------------------------
	{
			do_log(0,sIn);
	}
	//---------------------------------------------------------------------------------
	private void debug(String sIn)
	//---------------------------------------------------------------------------------
	{
		do_error(sIn);
	}
	
	//---------------------------------------------------------------------------------
	public cmcGraphController(cmcProcSettings xMi,logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = xMi;
		logger = ilog;
		iRout = new cmcImageRoutines(logger);
		ibulk = new cmcBulkImageRoutines(xMSet,logger);
		kloon = new cmcKloonGraphPageObject();
		stack = new ArrayList<cmcDrawingObject>();
		iKol = new cmcColor();
		backdropTipe = xMSet.getBackDropType();
		for(int i=0;i<wiskerOID.length;i++) wiskerOID[i]=-1;
	}
	
	//---------------------------------------------------------------------------------
	public void repaint(Graphics g)
	//---------------------------------------------------------------------------------
	{
		
		for(int i=0;i<stack.size();i++)
		{
			cmcDrawingObject o = stack.get(i);
			g.setColor(o.kleur);
			switch( o.tipe )
			{
			case LINE : {
				g.drawLine( o.x1 , o.y1 , o.x2 , o.y2 );
				break;
			}
			case RECTANGLE : {
				g.drawRect( o.x1 , o.y1 , o.x2 - o.x1 , o.y2 - o.y1 );  // opgepast breede bij een rectanle = -1 ttz men telt 1ste x niet mee
				break;
			}
			case FILLEDRECTANGLE : {
				g.fillRect( o.x1 , o.y1 , o.x2 - o.x1 , o.y2 - o.y1 );  // opgepast breede bij een rectanle = -1 ttz men telt 1ste x niet mee
				break;
			}
			case THICKRECTANGLE : {
				g.drawRect( o.x1 , o.y1 , o.x2 - o.x1 , o.y2 - o.y1 );  
				g.drawRect( o.x1+1 , o.y1+1 , o.x2 - o.x1 - 2 , o.y2 - o.y1 - 2 ); 
				g.drawRect( o.x1+2 , o.y1+2 , o.x2 - o.x1 - 4 , o.y2 - o.y1 - 4 ); 
				break;
			}
			case HANDLEDRECTANGLE : {
				
				int breedte = o.x2 - o.x1;
				int hoogte  = o.y2 - o.y1;
				g.drawRect( o.x1 , o.y1 , breedte , hoogte);
				
				int horz = breedte / 5;
				if( horz > 40 ) horz = 40;
				if( (2*horz) >= breedte ) horz = breedte/2;
				g.fillRect( o.x1 , o.y1 , horz , 3);
				g.fillRect( o.x2-horz , o.y1 , horz , 3);
				g.fillRect( o.x1 , o.y2-3 , horz , 3);
				g.fillRect( o.x2-horz , o.y2-3 , horz , 3);
				
				int vert = hoogte / 5;
				if( vert > 40 ) vert =40;
				if ( (2*vert) >= hoogte ) vert = hoogte/2;
				g.fillRect( o.x1 , o.y1 , 3 , vert );
				g.fillRect( o.x2-3 , o.y1 , 3 , vert  );
				g.fillRect( o.x1 , o.y2-vert , 3 , vert  );
				g.fillRect( o.x2-3 , o.y2-vert , 3 , vert  );
				
				break;
			}
			case TEXT : {
			    g.setFont(xMSet.getPreferredFont());
			    g.drawString( o.value , o.x1 , o.y1 +20);
				break;
			}
			default: {do_error("Unknow graph object tipe " + o.tipe + "]"); break; }
			}
		}
	}
	
	//---------------------------------------------------------------------------------
	private void removeFromStackViaOID(int ioid)
	//---------------------------------------------------------------------------------
	{
		try {
			for(int i=0;i<stack.size();i++)
			{
				if( stack.get(i).OID == ioid ) {
					stack.remove(i);
					break;
				}
			}
		}
		catch(Exception e) {
			do_error("Hey ? vannot remove [" + ioid + "] from stack");
		}
	}
	
	//---------------------------------------------------------------------------------
	public int drawRectangle(int x, int y , int breedte , int hoogte , Color clr)
	//---------------------------------------------------------------------------------
	{
		cmcDrawingObject o = new cmcDrawingObject( oidteller++ , drawType.RECTANGLE , x , y , x + breedte - 1, y + hoogte - 1 , clr);
		stack.add(o);
		return o.OID;
	}

	//---------------------------------------------------------------------------------
	public int fillRectangle(int x, int y , int breedte , int hoogte , Color clr)
	//---------------------------------------------------------------------------------
	{
			cmcDrawingObject o = new cmcDrawingObject( oidteller++ , drawType.FILLEDRECTANGLE , x , y , x + breedte - 1, y + hoogte - 1 , clr);
			stack.add(o);
			return o.OID;
	}
	
	//---------------------------------------------------------------------------------
	public int drawThickRectangle(int x, int y , int breedte , int hoogte , Color clr)
	//---------------------------------------------------------------------------------
	{
			cmcDrawingObject o = new cmcDrawingObject( oidteller++ , drawType.THICKRECTANGLE , x , y , x + breedte - 1, y + hoogte - 1 , clr);
			stack.add(o);
			return o.OID;
	}
	//---------------------------------------------------------------------------------
	public int drawHandledRectangle(int x, int y , int breedte , int hoogte , Color clr)
	//---------------------------------------------------------------------------------
	{
			cmcDrawingObject o = new cmcDrawingObject( oidteller++ , drawType.HANDLEDRECTANGLE , x , y , x + breedte - 1, y + hoogte - 1 , clr);
			stack.add(o);
			return o.OID;
	}
	
	//---------------------------------------------------------------------------------
	public int drawLine( int x1 , int y1 , int x2 , int y2 , Color clr )
	//---------------------------------------------------------------------------------
	{
		cmcDrawingObject o = new cmcDrawingObject( oidteller++, drawType.LINE , x1 , y1 , x2 , y2 , clr);
		stack.add(o);
		return o.OID;
	}
	
	//---------------------------------------------------------------------------------
	public int drawText(int x1 , int y1 , Color clr , String sIn )
	//---------------------------------------------------------------------------------
	{
		cmcDrawingObject o = new cmcDrawingObject( oidteller++, drawType.TEXT , x1 , y1 , clr , sIn);
		stack.add(o);
		return o.OID;
	}
	
	//---------------------------------------------------------------------------------
	public String getSourceImageFile()
	//---------------------------------------------------------------------------------
	{
		return sSourceImageFile;
	}

	//---------------------------------------------------------------------------------
	public String getWorkImageFile()
	//---------------------------------------------------------------------------------
	{
		return sWorkImageFile;
	}

	//---------------------------------------------------------------------------------
	public String getTitle()
	//---------------------------------------------------------------------------------
	{
		if( (sOrigFileName == null) || (sCMXUID == null) ) return "?";
		return this.sOrigFileName + " (" + this.sCMXUID + ")";
	}
	
	// getters and setters
	//---------------------------------------------------------------------------------
	public cmcProcEnums.BackdropType getBackdropType()
	{
		return backdropTipe;
	}
	public cmcProcEnums.WiskerColor getWiskerType()
	{
		return wiskerTipe;
	}
	public boolean getShoPayloadBoundaries()
    {
    	return shoPayloadBoundaries;
    }
	public boolean getShoFrames()
	{
		return shoFrames;
	}
	public boolean getShoParagraphs()
	{
		return shoParagraphs;
	}
	public boolean getShoTextParagraphs()
	{
		return shoTextParagraphs;
	}
	public boolean getShoCharacters()
	{
		return shoCharacters;
	}
	public boolean getShoNoise()
	{
		return shoNoise;
	}
	public boolean getShoValids()
	{
		return shoValids;
	}
	public boolean getShoInvalids()
	{
		return shoInvalids;
	}
	public String getOriginalLanguage()
	{
		return OriginalLanguage;
	}
	
	
	public int getImageWidth()
	{
		return this.ImageWidth;
	}
	public int getImageHeigth()
	{
		return this.ImageHeigth;
	}
	public int getPayLoadX()
	{
		return this.PayLoadX;
	}
	public int getPayLoadY()
	{
		return this.PayLoadY;
	}
	public String getCMXUID()
	{
		return sCMXUID;
	}
	public String getUID()
	{
		return sUID;
	}
	//
	public void changeOriginalLanguage(String sin)
	{
		OriginalLanguage = sin.trim().toUpperCase();
		do_error("TODO - update metadata XML >> " + OriginalLanguage);
	}
	public void setShoPayloadBoundaries(boolean ib)
	{
		shoPayloadBoundaries=ib;
	}
	public void setShoFrames(boolean ib)
	{
		shoFrames=ib;
	}
	public void setShoTextParagraphs(boolean ib)
	{
		shoTextParagraphs=ib;
	}
	public void setShoParagraphs(boolean ib)
	{
		shoParagraphs=ib;
	}
	public void setShoCharacters(boolean ib)
	{
		shoCharacters=ib;
	}
	public void setShoNoise(boolean ib)
	{
		shoNoise=ib;
	}
	public void setShoValids(boolean ib)
	{
		shoValids=ib;
	}
	public void setShoInvalids(boolean ib)
	{
		shoInvalids=ib;
	}
	public void setBackdropTipe(cmcProcEnums.BackdropType t)
	{
		backdropTipe = t;
	}
	public void setWiskerTipe(cmcProcEnums.WiskerColor t)
	{
		wiskerTipe = t;
	}
	
	//---------------------------------------------------------------------------------
	public boolean getIsTextParagraph(int idx)
	//---------------------------------------------------------------------------------
	{
		if( (idx < 0) || (idx >= ar_pabo.length) ) {
			do_error("getIsTextParagraph - idx out of bound");
			return false;
		}
		if( ar_pabo[idx].removed ) return false;
		if( ar_pabo[idx].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) return true;
		if( ar_pabo[idx].tipe != cmcProcEnums.PageObjectType.PARAGRAPH ) {
			do_error("getIsTextParagraph - function should not be called - not a paragraph");
		}
		return false;
	}
	
	//---------------------------------------------------------------------------------
	public void purgeVariables()
	//---------------------------------------------------------------------------------
	{
				sOrigFileName = null;
				sOrigFileDir=null;
				sCMXUID=null;
				sUID=null;
				ImageWidth=-1;
			    ImageHeigth=-1;
			    PayLoadX=-1;
			    PayLoadY=-1;
			    PayLoadWidth=-1;
			    PayLoadHeigth=-1;
			    pixels=null;
			    ar_pabo=null;
			    FrameClusterIdx=-1;
			    LetterClusterIdx=-1;
			    OriginalLanguage="Unknown";
			    ignoreChanges = false;
			    lastModifiedDate = System.currentTimeMillis();
			    backdropTipe = xMSet.getBackDropType();
			    ar_text=null;
	}
	
	//---------------------------------------------------------------------------------
	public void clearStack()
	//---------------------------------------------------------------------------------
	{
		if( stack != null ) {
			 int aantal=stack.size();
			 for(int i=0;i<aantal;i++) stack.remove(0);
			 stack=null;
			}
			stack = new ArrayList<cmcDrawingObject>();
	}
	
	// read the XML file en intialiseer alles
	//---------------------------------------------------------------------------------
	public boolean initializeController(String ZipFileName)
	//---------------------------------------------------------------------------------
	{
		try {
		//
		purgeVariables();
		//
		// stack
		clearStack();
		// Create DAO and unzip
		if( dao != null ) dao =  null;
		dao = new cmcGraphPageDAO( xMSet , ZipFileName , logger);
		// check the dao
		if( dao.IsDaoReady() == false ) {
		    do_error("DAO is not ready [" + ZipFileName + "]");
			return false;
		}
		setZipFileName( ZipFileName );
		
		// read STAT file
		if( readStatXMLFile() == false ) return false;
		
		// read the TEXT files
		if ( readTextXMLFile() == false ) return false;
		
		// read the zMetadatafile
	    readZMetadata();
	    
		// determine visbility
		setObjectVisibility();
		
		// Kopieer nu originele file naar de Cache
		String sF = sOrigFileDir + xMSet.xU.ctSlash + sOrigFileName;
	    if( xMSet.xU.IsBestand( sF ) == false ) {
	    	do_error("Cannot locate original image file [" + sF + "]");
	    	return false;
	    }
	    lastModifiedDate = xMSet.xU.getModificationTime(sF);
	    String sSuf = xMSet.xU.GetSuffix(sF);
	    sSourceImageFile = xMSet.getCacheDir() + xMSet.xU.ctSlash + "SourceImage." + sSuf;
	    if( xMSet.xU.IsBestand( sSourceImageFile ) == true ) {
	    	xMSet.xU.VerwijderBestand(sSourceImageFile);
	    	if( xMSet.xU.IsBestand( sSourceImageFile ) == true ) {
	    		do_error("Cannot remove [" + sSourceImageFile + "]");
	    		return false;
	    	}
	    }
	    try {
	     xMSet.xU.copyFile( sF , sSourceImageFile );
	    }
	    catch( Exception e ) {
	    	do_error("Cannot move [" + sF + "] to ["+ sSourceImageFile + "]");
    		return false;
	    }
	    //
	    sWorkImageFile = xMSet.getCacheDir() + xMSet.xU.ctSlash + "WorkImage." + xMSet.getPreferredImageSuffix();
	    //
		return true;
		}
		catch(Exception e) {
			do_error("Something majorly went wrong " + xMSet.xU.LogStackTrace(e));
			return false;
		}

	}
	
	//---------------------------------------------------------------------------------
	private void readZMetadata()
	//---------------------------------------------------------------------------------
	{
		cmcBookMetaDataDAO meta = new cmcBookMetaDataDAO(xMSet,null,null,logger);
		meta.readMetaDataFile(xMSet.getGraphZMetaXML());
		OriginalLanguage = meta.getLanguage();
	}

	//---------------------------------------------------------------------------------
	private boolean readStatXMLFile()
	//---------------------------------------------------------------------------------
	{
        ar_pabo = dao.readXML();
        if( ar_pabo == null ) return false;
        //
    	sOrigFileName = dao.getOrigFileName();
    	sCMXUID= dao.getCMXUID();
    	sUID= dao.getUID();
    	sOrigFileDir=dao.getOrigFileDir();
    	ImageWidth=dao.getImageWidth();
        ImageHeigth=dao.getImageHeigth();
        PayLoadX=dao.getPayLoadX();
        PayLoadY=dao.getPayLoadY();
        PayLoadWidth=dao.getPayLoadWidth();
        PayLoadHeigth=dao.getPayLoadHeigth();
        FrameClusterIdx=dao.getFrameClusterIdx();
        LetterClusterIdx=dao.getLetterClusterIdx();
    	//
		return true;
	}

	//---------------------------------------------------------------------------------
	private boolean readTextXMLFile()
	//---------------------------------------------------------------------------------
	{
        cmcTextDAO dao = new cmcTextDAO(xMSet,logger);
        ar_text = dao.readTextObjects();
        if( ar_text == null ) return false;
		return true;
	}
	
	//---------------------------------------------------------------------------------
	public String getShortPageInfo()
	//---------------------------------------------------------------------------------
	{
		return "[" + sOrigFileName + " - " + sCMXUID + "] " + OriginalLanguage.toLowerCase() + " [" + this.ImageWidth + "x" + this.ImageHeigth + "]" +
	           " [Created : " + (xMSet.xU.prntStandardDateTime(lastModifiedDate)).toLowerCase() + "]";
	}
	
	//---------------------------------------------------------------------------------
	public boolean initialiseerPixels()
	//---------------------------------------------------------------------------------
	{
		// pixels already loaded
		if( pixels != null ) {
		    if( prevFileName.compareToIgnoreCase(this.getSourceImageFile()) == 0) return true;	
		}
		//
		prevFileName = this.getSourceImageFile();
		long startt = System.currentTimeMillis();
	    Image img=null;
	    try {
		    URL url = new URL("file:///" + this.getSourceImageFile() );
	        img = ImageIO.read(url);
	    }
	    catch(Exception e )
	    {
		   do_error("Reading imagefile [" + this.getSourceImageFile() + "] " + e.getMessage());
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
	    workPixels=null;
	    pixels = new int[ w * h];
	    PixelGrabber pg = new PixelGrabber( img , 0 , 0 , w , h , pixels , 0 , w );
	    try {
	    	pg.grabPixels();
	    }
	    catch(Exception e )
	    {
		   do_error("Pixelgrabber [" + this.getSourceImageFile() + "] " + e.getMessage());
		   return false;
	    }
	    //
	    img=null;
	    //
	    do_log(1,"Pixels grabbed in " + (System.currentTimeMillis()-startt) + " msec");
	    //
	    return true;
	}
	
	
	
	//---------------------------------------------------------------------------------
	public void makeOverlay()
	//---------------------------------------------------------------------------------
	{
		//
		clearStack();
		//
		setObjectVisibility();
		//
		switch( backdropTipe )
		{
		case ORIGINAL           : { makeOriginalPicture(); break; }
		case GRAYSCALE          : { workPixels=null; workPixels=ibulk.doGrayscale(pixels); break; }
		case BLEACHED           : { makeBleachedPicture(); break; }
		case BLACKBLEACHED      : { makeBlackBleachedPicture(); break; }
		case GRAYRASTERIZED     : { workPixels=null; workPixels=ibulk.makeGrayRasterizedPicture(pixels,ImageWidth); break; }
		case COLORRASTERIZED    : { workPixels=null; workPixels=ibulk.makeColorRasterizedPicture(pixels,ImageWidth); break; }
		case BLUEPRINT          : { makeBlueprintPicture(); break; }
		case MONOCHROME_NIBLAK  : { makeMonochromePicture(); break; }
		case MONOCHROME_SAUVOLA : { makeMonochromePicture(); break; }
		case MONOCHROME_OTSU    : { makeMonochromePicture(); break; }
		case MAINFRAME          : { makeMainframePicture(); break; }
        default                 : { makeEmptyPicture(); break; }
		}
		// merge de visibles
		mergeVisibleObjects();
		// schrijf
		iRout.writePixelsToFile( workPixels , this.ImageWidth , this.ImageHeigth , this.getWorkImageFile() , cmcImageRoutines.ImageType.RGB);
		workPixels=null;
		// Kaders
		putBordersAroundVisibleObjects();
		if( this.shoPayloadBoundaries ) prepareShoPayloadBoundaries();
		if( shoFrames ) prepareShoFrames();	
	}

	//---------------------------------------------------------------------------------
	public boolean saveWorkFile(String FName)
	//---------------------------------------------------------------------------------
	{
		try {
			xMSet.xU.copyFile(  this.getWorkImageFile() , FName );
			return true;
		}
		catch(Exception e) {
		  do_error("Cannot save image to [" + FName + "] " + e.getMessage() );
		  return false;	
		}
	}
	
	//---------------------------------------------------------------------------------
	private void makeEmptyPicture()
	//---------------------------------------------------------------------------------
	{
		workPixels = null;
		workPixels = new int[pixels.length];
		for(int i=0;i<pixels.length;i++)
		{
			workPixels[i] = 0xffffffff;
		}
	}

	//---------------------------------------------------------------------------------
	private void makeOriginalPicture()
	//---------------------------------------------------------------------------------
	{
		workPixels = null;
		workPixels = new int[pixels.length];
		for(int i=0;i<pixels.length;i++)
		{
			workPixels[i] = pixels[i];
		}
	}
	
	//---------------------------------------------------------------------------------
	private void makeBleachedPicture()
	//---------------------------------------------------------------------------------
	{
		    workPixels = null;
			workPixels = new int[pixels.length];
			for(int i=0;i<pixels.length;i++)
			{
			    workPixels[i] = iRout.bleach(pixels[i]) | 0xff000000;
	     	}
	}
	
	//---------------------------------------------------------------------------------
	private void makeBlackBleachedPicture()
	//---------------------------------------------------------------------------------
	{
	    workPixels = null;
		workPixels = new int[pixels.length];
		for(int i=0;i<pixels.length;i++)
		{
		    workPixels[i] = iRout.blackbleach(pixels[i]) | 0xff000000;
     	}
	}
	//---------------------------------------------------------------------------------
	private void makeMonochromePicture()
	//---------------------------------------------------------------------------------
	{
			 workPixels=null;
			 workPixels=ibulk.doGrayscale(pixels);
			 switch( backdropTipe )
			 {
				case MONOCHROME_NIBLAK  : { workPixels=ibulk.doNiblak(pixels,ImageWidth,true); break; }
				case MONOCHROME_SAUVOLA : { workPixels=ibulk.doNiblak(pixels,ImageWidth,false); break;}
				case MONOCHROME_OTSU    : { 
					 int thres = iRout.otsuTreshold(pixels); 
					 workPixels=ibulk.binarize(pixels,thres);  
					 break; }
			    default                 : { makeEmptyPicture(); break; }
				}
	}
	
	//---------------------------------------------------------------------------------
	private void makeBlueprintPicture()
	//---------------------------------------------------------------------------------
	{
		 workPixels=null; 
		 workPixels=ibulk.doGrayscale(pixels);
		 int thres = iRout.otsuTreshold(pixels); 
		 workPixels=ibulk.binarize(pixels,thres);  
		 //workPixels=ibulk.doNiblak(pixels,ImageWidth,false);
		 workPixels = ibulk.doBlue(workPixels);
	}
	
	//---------------------------------------------------------------------------------
	private void makeMainframePicture()
	//---------------------------------------------------------------------------------
	{
			 workPixels=null; 
			 workPixels=ibulk.doGrayscale(pixels);
			 //int thres = iRout.otsuTreshold(pixels); 
			 //workPixels=ibulk.binarize(pixels,thres);  
			 workPixels=ibulk.doNiblak(pixels,ImageWidth,false);
			 workPixels = ibulk.doMainframe(workPixels);
	}
	
	//---------------------------------------------------------------------------------
	private void prepareShoPayloadBoundaries()
	//---------------------------------------------------------------------------------
	{
				Color k=Color.DARK_GRAY;
		        if( PayLoadX > 0 ) 
		        	this.drawLine( PayLoadX , 0 , PayLoadX , ImageHeigth , k );	
		        if( (PayLoadX + PayLoadWidth) < ImageWidth ) 
		        	this.drawLine( PayLoadX + PayLoadWidth , 0 , PayLoadX + PayLoadWidth, ImageHeigth , k );	
		        if( PayLoadY > 0 ) 
		        	this.drawLine( 0 , PayLoadY , ImageWidth , PayLoadY , k );
		        if( (PayLoadY + PayLoadHeigth) < ImageHeigth ) 
		        	this.drawLine( 0 , PayLoadY + PayLoadHeigth , ImageWidth , PayLoadY + PayLoadHeigth , k );
	}
	
	//---------------------------------------------------------------------------------
	private void prepareShoFrames()
	//---------------------------------------------------------------------------------
	{
		Color k = Color.RED;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].tipe != cmcProcEnums.PageObjectType.FRAME ) continue;
			int breedte = 	ar_pabo[i].MaxX - ar_pabo[i].MinX + 1; 
			int hoogte = 	ar_pabo[i].MaxY - ar_pabo[i].MinY + 1; 
			this.drawHandledRectangle( ar_pabo[i].MinX + PayLoadX ,ar_pabo[i].MinY + PayLoadY , breedte , hoogte , k );
		}
	}
	
	//---------------------------------------------------------------------------------
	private void putBordersAroundVisibleObjects()
	//---------------------------------------------------------------------------------
	{
		Color kleur = Color.cyan;
		for(int i=0;i<ar_pabo.length;i++)
		{
			 ar_pabo[i].DrawObjectOID = -1;
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
			int breedte = 	ar_pabo[i].MaxX - ar_pabo[i].MinX + 1; 
			int hoogte  = 	ar_pabo[i].MaxY - ar_pabo[i].MinY + 1;
			switch( ar_pabo[i].tipe )
			{
			 case FRAME         : { kleur = Color.yellow; break; }
			 case PARAGRAPH     : { drawParagraph(i); continue; }
			 case TEXTPARAGRAPH : { drawParagraph(i); continue; }
			 case LETTER        : { kleur = Color.blue; break; }
			 case NOISE         : { kleur = Color.white; break; }
			 default            : continue;
			}
	        ar_pabo[i].DrawObjectOID = drawRectangle( ar_pabo[i].MinX + PayLoadX ,ar_pabo[i].MinY + PayLoadY , breedte , hoogte , kleur );
		}
	}
	
	//---------------------------------------------------------------------------------
	private void drawParagraph(int i)
	//---------------------------------------------------------------------------------
	{
		Color kleur;
		int breedte = 	ar_pabo[i].MaxX - ar_pabo[i].MinX + 1; 
		int hoogte  = 	ar_pabo[i].MaxY - ar_pabo[i].MinY + 1;
		switch( ar_pabo[i].tipe ) 
		{
			case PARAGRAPH     : { 	kleur = Color.orange; 
									ar_pabo[i].DrawObjectOID = drawThickRectangle( ar_pabo[i].MinX + PayLoadX  ,ar_pabo[i].MinY + PayLoadY  , breedte , hoogte  , kleur );
									return; }
			case TEXTPARAGRAPH : { 	kleur = Color.green;  
									ar_pabo[i].DrawObjectOID  = drawHandledRectangle( ar_pabo[i].MinX + PayLoadX  ,ar_pabo[i].MinY + PayLoadY  , breedte , hoogte  , kleur );
									return; }
		}
	}
	
	//---------------------------------------------------------------------------------
	private void assessObjectVisibility(int idx)
	//---------------------------------------------------------------------------------
	{
		boolean ok=true;
		if( ar_pabo[idx].removed ) { ok = false; }
		else {
			if( ar_pabo[idx].isValid ) {
				if( shoValids == false ) ok = false;
			}
			else {
				if( shoInvalids == false ) ok = false;
			}
		}
		if( ok ) {
		 switch( ar_pabo[idx].tipe )
		 {
		 case FRAME         : { ok=false; break; }  //  keep invisible want omvat alles
		 case PARAGRAPH     : { ok=shoParagraphs; break; }
		 case TEXTPARAGRAPH : { ok=shoTextParagraphs; break; }
		 case LETTER        : { ok=shoCharacters; break; }
		 case NOISE         : { ok=shoNoise; break; }
		 default            : { ok=false; break; }
		 }
		}
		if( ok ) ar_pabo[idx].visi = cmcProcEnums.VisibilityType.VISIBLE;
		    else ar_pabo[idx].visi = cmcProcEnums.VisibilityType.INVISIBLE;
	}
	
	//---------------------------------------------------------------------------------
	public void setObjectVisibility()
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<ar_pabo.length;i++)
		{
			assessObjectVisibility(i);
		}
        shoReport();		
	}
	
	//---------------------------------------------------------------------------------
	private void mergeVisibleObjects()
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
			if( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.FRAME ) continue;   // geen frames mergen
			
			// kopieer uit pixels naar work
			int breedte = ar_pabo[i].MaxX - ar_pabo[i].MinX + 1;
			int hoogte  = ar_pabo[i].MaxY - ar_pabo[i].MinY + 1;
	        //System.out.println("x=" + ar_pabo[i].MinX + " y=" + ar_pabo[i].MinY + " b=" + breedte + " h=" + hoogte + " " + ar_pabo[i].tipe);
			for(int y=0;y<hoogte;y++)
			{
				for(int x=0;x<breedte;x++)
				{
					int p = (x + PayLoadX + ar_pabo[i].MinX) + (( y + PayLoadY + ar_pabo[i].MinY) * ImageWidth );
					if( p >= pixels.length ) {
						System.err.println("overflow " + x + " " + y + " " + p );
						break;
					}
					workPixels[p] = pixels[p];
				}
			}
		}	
	}
	
	//---------------------------------------------------------------------------------
	private void shoReport()
	//---------------------------------------------------------------------------------
	{
	    int frames=0;
	    int para=0;
	    int texts=0;
	    int letters=0;
	    int noise=0;
	    int unk=0;
	    int iframes=0;
	    int ipara=0;
	    int itexts=0;
	    int iletters=0;
	    int inoise=0;
	    int iunk=0;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.VISIBLE ) {
			 switch( ar_pabo[i].tipe )
			 {
			 case FRAME         : { frames++; break; }  
			 case PARAGRAPH     : { para++; break; }
			 case TEXTPARAGRAPH : { texts++; break; }
			 case LETTER        : { letters++; break; }
			 case NOISE         : { noise++; break; }
			 default            : { unk++; break; }
			 }
			}
			if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) {
				 switch( ar_pabo[i].tipe )
				 {
				 case FRAME         : { iframes++; break; }  
				 case PARAGRAPH     : { ipara++; break; }
				 case TEXTPARAGRAPH : { itexts++; break; }
				 case LETTER        : { iletters++; break; }
				 case NOISE         : { inoise++; break; }
				 default            : { iunk++; break; }
				 }
				}

		}
		do_log(9,"    Objects : " + ar_pabo.length);
		do_log(9,"     Frames : " + frames + " " + iframes);
		do_log(9," Paragraphs : " + para + " " + ipara);
		do_log(9,"       Text : " + texts + " " + itexts);
		do_log(9,"    Letters : " + letters + " " + iletters);
		do_log(9,"      Noise : " + noise + " " + inoise);
		do_log(9,"    Unknown : " + unk + " " + iunk);
	}
	
	// called by MAIN following a RIGHT mouse click and by hasParagraphBeenSelected()
	//---------------------------------------------------------------------------------
	public int getSelectedParagraph(int x , int y)
	//---------------------------------------------------------------------------------
	{
		if( ar_pabo == null ) return -1;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
			if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;   // geen frames mergen
			//
			int x1 = ar_pabo[i].MinX + this.PayLoadX;
			if( x < x1 ) continue;
			int x2 = ar_pabo[i].MaxX + this.PayLoadX;
			if( x > x2 ) continue;
			int y1 = ar_pabo[i].MinY + this.PayLoadY;
			if( y < y1 ) continue;
			int y2 = ar_pabo[i].MaxY + this.PayLoadY;
			if( y > y2 ) continue;
			return i;
		}
		return -1;
	}
	
	// mouse pressed down 2 seconds
	//---------------------------------------------------------------------------------
	public boolean hasParagraphBeenSelected(int x , int y)
	//---------------------------------------------------------------------------------
	{
		int idx = getSelectedParagraph(x,y);
		if( idx < 0 ) return false;
		if( ar_pabo[idx].isSelected == false) {
			ar_pabo[idx].isSelected = true;
			int x1 = ar_pabo[idx].MinX + this.PayLoadX;
			int x2 = ar_pabo[idx].MaxX + this.PayLoadX;
			int y1 = ar_pabo[idx].MinY + this.PayLoadY;
			int y2 = ar_pabo[idx].MaxY + this.PayLoadY;
			ar_pabo[idx].DrawObjectOID = drawThickRectangle( x1 , y1 ,  x2-x1+1 , y2-y1+1 , Color.red );
			//
			//activeerComponentenInRange(idx);   no longer used
		}
		else {
			removeFromStackViaOID(ar_pabo[idx].DrawObjectOID);
			ar_pabo[idx].isSelected = false;
			ar_pabo[idx].DrawObjectOID = -1;
		}
		return true;
	}

	/*
	// wordt obsolete - objecten in de buurt geslecteerde
	//---------------------------------------------------------------------------------
	private void activeerComponentenInRange(int idx)
	//---------------------------------------------------------------------------------
	{
		int b = ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
		int h = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
		//       X
		//      XXX
		//       X
		int left_x  = ar_pabo[idx].MinX - (b / 2);
		int left_y  = ar_pabo[idx].MinY - (h / 2);
		int right_x = ar_pabo[idx].MaxX + (b / 2);
		int right_y = ar_pabo[idx].MaxY + (h / 2);
		// in range
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( i == idx ) continue;
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].isValid == false ) continue;
			if( ar_pabo[i].MinX > right_x   ) continue;
			if( ar_pabo[i].MaxX < left_x    ) continue;
			if( ar_pabo[i].MaxY < left_y    ) continue;
			if( ar_pabo[i].MinY > right_y   ) continue;
			//
			int x1 = ar_pabo[i].MinX + this.PayLoadX;
			int x2 = ar_pabo[i].MaxX + this.PayLoadX;
			int y1 = ar_pabo[i].MinY + this.PayLoadY;
			int y2 = ar_pabo[i].MaxY + this.PayLoadY;
			// omzeil de frames
			if( (x2 - x1) > b ) continue;
			if( (y2 - y1) > h ) continue;
			//
			ar_pabo[i].DrawObjectOID = drawRectangle( x1 , y1 ,  x2-x1+1 , y2-y1+1 , Color.yellow );
			//
			//System.err.println("IN range" + x1 + " " + y1 + " " + x2 + " " + y2);
		}
	}
	*/
	
	// called by the ParagrapheditorDialog
	//---------------------------------------------------------------------------------
	public int[] getPixels(int idx)
	//---------------------------------------------------------------------------------
	{
	  if( (idx<0) || (idx>=ar_pabo.length)) return null;
	  if( ar_pabo[idx].removed ) return null;
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
					System.err.println("overflow " + x + " " + y + " " + p );
					return null;
				}
				ret[teller] = pixels[p];
				teller++;
			}
	   }
	   return ret;
	}
	
	//---------------------------------------------------------------------------------
	public int getWidthViaIdx(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return -1;
		 return ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
	}
	
	//---------------------------------------------------------------------------------
	public String getDimensionInfo(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return "";
		 int breedte = ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1;
		 int hoogte  = ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1;
		 return "Object [" + idx + "] : (" + ar_pabo[idx].MinX + "," + ar_pabo[idx].MinY + ") " + breedte + " x " + hoogte;	 
	}
	
	//---------------------------------------------------------------------------------
	public long getUID(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return -1L;
		 return ar_pabo[idx].UID;
	}
	
	//---------------------------------------------------------------------------------
	public int getFirstLetterIdx()
	//---------------------------------------------------------------------------------
	{
			 for(int i=0;i<ar_pabo.length;i++)
			 {
				    if( ar_pabo[i].removed ) continue;
					if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
					if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH)) continue;   // geen frames mergen
					return i;
			 }
			 return -1;
	}
	
	//---------------------------------------------------------------------------------
	public int getNextIdx(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return idx;
		 int first=-1;
		 boolean next=false;
		 for(int i=0;i<ar_pabo.length;i++)
		 {
			    if( ar_pabo[i].removed ) continue;
				if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
				if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;   // geen frames mergen
				if( first < 0 ) first = i;
				if( next == true ) return i;
				if( i == idx ) next = true;
		 }
		 return first;
	}
	
	//---------------------------------------------------------------------------------
	public int getPrevIdx(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return idx;
		 int last=-1;
		 boolean prev=false;
		 for(int i=(ar_pabo.length-1);i>0;i--)
		 {
			    if( ar_pabo[i].removed ) continue;
				if( ar_pabo[i].visi == cmcProcEnums.VisibilityType.INVISIBLE ) continue;
				if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;   // geen frames mergen
				if( last < 0 ) last = i;
				if( prev == true ) return i;
				if( i == idx ) prev = true;
		 }
		 return last;
	}
	
	//---------------------------------------------------------------------------------
	public void showSettings()
	//---------------------------------------------------------------------------------
	{
		String sLijn = 
				        "[shoFrames    =" + shoFrames + "]\n" +
						"[shoTextParagr=" + shoTextParagraphs + "]\n" +
						"[shoParagraps =" + shoParagraphs + "]\n" +
						"[shoCharacters=" + shoCharacters + "]\n" +
						"[shoNoise     =" + shoNoise + "]\n" +
						"[shoValids    =" + shoValids + "]\n" +
						"[shoInvalids  =" + shoInvalids + "]\n" +
						"[Backdrop=" + this.backdropTipe + "]";
	    do_log(5,"==>\n " + sLijn);					 
	}
	
	
	//---------------------------------------------------------------------------------
	private Color getDrawingColor()
	//---------------------------------------------------------------------------------
	{
		switch( wiskerTipe )
		{
		case NONE   : return null;
		case RED    : return Color.RED; 
		case BLUE   : return Color.BLUE; 
		case WHITE  : return Color.WHITE; 
		case BLACK  : return Color.BLACK; 
		case YELLOW : return Color.YELLOW;
		case GREEN  : return Color.GREEN; 
		case GRAY   : return Color.GRAY; 
		case CYAN   : return Color.CYAN; 
		default     : return Color.CYAN;
		}
	}
	//---------------------------------------------------------------------------------
	public boolean wisker(int x,int y) 
	//---------------------------------------------------------------------------------
	{
		Color kolor=getDrawingColor();
		if( wiskerOID[0] != -1 ) {
		 for(int i=0;i<wiskerOID.length;i++) { removeFromStackViaOID(wiskerOID[i]); wiskerOID[i]=-1; }
		}
		
		// middle
		int gap=6;
		kolor = getDrawingColor();
		wiskerOID[0]=drawLine(0,y,x-gap,y,kolor);
		wiskerOID[1]=drawLine(x+gap,y,ImageWidth,y,kolor);
		wiskerOID[2]=drawLine(x,0,x,y-gap,kolor);
		wiskerOID[3]=drawLine(x,y+gap,x,ImageHeigth,kolor);
		
		// left - upper outer
		kolor = Color.white;
		if( y > 0 ) y--;
		if( x > 0 ) x--;
		wiskerOID[4]=drawLine(0,y,x-gap,y,kolor);
		wiskerOID[5]=drawLine(x+gap,y,ImageWidth,y,kolor);
		wiskerOID[6]=drawLine(x,0,x,y-gap,kolor);
		wiskerOID[7]=drawLine(x,y+gap,x,ImageHeigth,kolor);
		
		// right - under
		kolor = Color.gray;
		y += 2;
		x += 2;
		if( x >= ImageWidth ) x = ImageWidth - 1;
		if( y >= ImageHeigth ) y=ImageHeigth - 1;
		wiskerOID[8]=drawLine(0,y,x-gap,y,kolor);
		wiskerOID[9]=drawLine(x+gap,y,ImageWidth,y,kolor);
		wiskerOID[10]=drawLine(x,0,x,y-gap,kolor);
		wiskerOID[11]=drawLine(x,y+gap,x,ImageHeigth,kolor);
		
		return true;
	}
	
	//---------------------------------------------------------------------------------
	public Rectangle makePositiveRectangle(int x1 , int y1 , int x2 , int y2)
	//---------------------------------------------------------------------------------
	{
		int xa = ( x1 < x2 ) ? x1 : x2;
		int ya = ( y1 < y2 ) ? y1 : y2;
		int xb = ( x1 < x2 ) ? x2 : x1;
		int yb = ( y1 < y2 ) ? y2 : y1;
		return new Rectangle( xa , ya , xb-xa , yb-ya);
	}
	
	//---------------------------------------------------------------------------------
	public boolean dragger(int startx , int starty , int currx , int curry ) 
	//---------------------------------------------------------------------------------
	{
		Color pc=getDrawingColor();
		Color kolor = new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 50); // transparent
		removeFromStackViaOID(draggerOID);
		removeFromStackViaOID(draggerLineOID);
		Rectangle rect = makePositiveRectangle( startx , starty , currx , curry );
		draggerOID=fillRectangle( rect.x , rect.y , rect.width , rect.height , kolor);
		draggerLineOID=drawRectangle(rect.x , rect.y , rect.width , rect.height , pc);
		rect=null;
		// do not show wiskers when dragging
		if( wiskerOID[0] != -1 ) {
			 for(int i=0;i<wiskerOID.length;i++) { removeFromStackViaOID(wiskerOID[i]); wiskerOID[i]=-1; }
		}
		return true;
	}
	
	//---------------------------------------------------------------------------------
	public void endDrag()
	//---------------------------------------------------------------------------------
	{
		removeFromStackViaOID(draggerOID);
		removeFromStackViaOID(draggerLineOID);
	}
	
	//---------------------------------------------------------------------------------
	public int zoekOIDViaIdx(int idx)
	//---------------------------------------------------------------------------------
	{
		 if( (idx<0) || (idx>=ar_pabo.length)) return -1;
		 for(int i=0;i<stack.size();i++)
		 {
			 cmcDrawingObject o = stack.get(i);
			 //System.out.println("" + o.x1 + " " + o.y1 + " -- " + ar_pabo[idx].MinX + " " + ar_pabo[idx].MinY + " " + ar_pabo[idx].BundelIdx);
			 if( o.x1 != ar_pabo[idx].MinX + this.PayLoadX ) continue;
			 if( o.y1 != ar_pabo[idx].MinY + this.PayLoadY ) continue;
			 if( o.x2 != ar_pabo[idx].MaxX + this.PayLoadX ) continue;
			 if( o.y2 != ar_pabo[idx].MaxY + this.PayLoadY ) continue;
			 return o.OID;
		 }
		 return -1;
	}
	
	//---------------------------------------------------------------------------------
	public boolean removeViaXY(int x , int y)
	//---------------------------------------------------------------------------------
	{
	    return removeViaIdx(getSelectedParagraph(x,y));
	}
	
	//---------------------------------------------------------------------------------
	public boolean removeViaIdx(int idx)
	//---------------------------------------------------------------------------------
	{
			if( idx < 0 ) return false;
		    int oid = zoekOIDViaIdx(idx);
			if( oid < 0 ) return false;
			//
			ar_pabo[idx].removed = true;
			ar_pabo[idx].hasChanged = true;
			ar_pabo[idx].changetipe = cmcProcEnums.EditChangeType.REMOVE;
			// unlink the objects that refer to this bundle
			int bundix = ar_pabo[idx].BundelIdx;
			for(int i=0;i<ar_pabo.length;i++)
			{
				//if( (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.PARAGRAPH) || (ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH) ) continue;
				if( ar_pabo[i].BundelIdx != bundix ) continue;
				if( i == idx ) continue;
				System.out.println("Object " + i + " is referring to bundle " + bundix + " which is attached to " + idx);
				ar_pabo[i].BundelIdx = -1;
				ar_pabo[i].hasChanged = true;
				ar_pabo[i].changetipe = cmcProcEnums.EditChangeType.UNLINK;
			}
			removeFromStackViaOID(oid);
			do_log(1,"Removed paragraph [" + idx + "] object [" + oid + "] [" + ar_pabo[idx].DrawObjectOID + "]");
			//
			return true;
	}
	
	//---------------------------------------------------------------------------------
	private int getBestFittingObject(int x , int y )
	//---------------------------------------------------------------------------------
	{
		if( ar_pabo == null ) return -1;
		long opp=0L;
		long maxopp=0L;
		int idx=-1;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].removed ) continue;
			//
			int x1 = ar_pabo[i].MinX + this.PayLoadX;
			if( x < x1 ) continue;
			int x2 = ar_pabo[i].MaxX + this.PayLoadX;
			if( x > x2 ) continue;
			int y1 = ar_pabo[i].MinY + this.PayLoadY;
			if( y < y1 ) continue;
			int y2 = ar_pabo[i].MaxY + this.PayLoadY;
			if( y > y2 ) continue;
			int breedte = ar_pabo[i].MaxX - ar_pabo[i].MinX + 1;
			int hoogte  = ar_pabo[i].MaxY - ar_pabo[i].MinY + 1;
			opp = (long)breedte * (long)hoogte;
			if( (opp < maxopp) || (maxopp==0L) ) {
				idx = i;
				maxopp = opp;
			}
//System.out.println("found [i=" + i + ", opp=" + opp + "] => idx=" + idx);
		}
//System.out.println("===================");
		return idx;
	}
	
	
	//---------------------------------------------------------------------------------
	private void removeCartouche(cartouche ic)
	//---------------------------------------------------------------------------------
	{
		if( ic == null ) return;
		this.removeFromStackViaOID( ic.topObjectOID );
		for(int i=0;i<ic.graphElementsOID.length;i++) {
			if( ic.graphElementsOID[i] < 0 ) continue;
			this.removeFromStackViaOID(ic.graphElementsOID[i]); 
		}
		for(int i=0;i<ic.childObjectsOID.length;i++) {
			if( ic.childObjectsOID[i] < 0 ) continue;
			this.removeFromStackViaOID(ic.childObjectsOID[i]); 
		}
	}
	
	//---------------------------------------------------------------------------------
	public void doImagePopup( boolean up , int xi , int yi )
	//---------------------------------------------------------------------------------
	{
		String sKolor = "red";
		// undo
		if( up == false ) {
			if( popupOID != null ) {
				removeCartouche(popupOID);
				popupOID=null;
			}
			return;
		}
		int idx = getBestFittingObject( xi , yi );
		if( idx < 0 ) return;
		
		// info object
		Color kolor=iKol.getColor("lightyellow");
		//kolor = new Color(kolor.getRed(),kolor.getGreen(),kolor.getBlue(), 10);  // transparane
		popupOID = new cartouche(idx);
		
		popupOID.childObjects = null;
		popupOID.childObjects = do_ObjectsEnveloped( idx );
		int cntr=0;
		for(int i=0;i<popupOID.childObjects.length;i++) {
			if( popupOID.childObjects[i] >=0 ) cntr++;
		}
	    //	
	    int hoogte=xMSet.getPreferredFont().getSize()+3;
	    int breedte=300;
	    int x = xi;
	    int y = yi;
	    popupOID.graphElementsOID[0] = this.fillRectangle( x + 5 , y + 5 , breedte , (hoogte*5) , kolor );        // lightyellow area
		popupOID.graphElementsOID[1] = this.drawRectangle( x + 5 , y + 5 , breedte , (hoogte*5) , Color.BLACK );  // border
		popupOID.graphElementsOID[3] = this.drawText(  x + 10 , y + 3 + (hoogte*0) , Color.BLACK , "Type : "+ar_pabo[idx].tipe );
		popupOID.graphElementsOID[4] = this.drawText(  x + 10 , y + 3 + (hoogte*1) , Color.BLACK , "Size : "+ (ar_pabo[idx].MaxX -  ar_pabo[idx].MinX + 1) + " x " + (ar_pabo[idx].MaxY -  ar_pabo[idx].MinY + 1));
		popupOID.graphElementsOID[5] = this.drawText(  x + 10 , y + 3 + (hoogte*2) , Color.BLACK , "#ChildObjects : " + cntr);
		popupOID.graphElementsOID[10] = this.drawLine(  x + 5 ,  y + 3 + (hoogte*4) , x+breedte+5 , y + 3 + (hoogte*4) , Color.BLACK );
			
		// transparante fill op het volledige object
		kolor = iKol.getColor(sKolor);
		kolor = new Color(kolor.getRed(),kolor.getGreen(),kolor.getBlue(), 50); // transparent
		popupOID.topObjectOID = this.fillRectangle( ar_pabo[idx].MinX + this.PayLoadX , 
				                                 ar_pabo[idx].MinY + + this.PayLoadY , 
				                                 (ar_pabo[idx].MaxX - ar_pabo[idx].MinX + 1 ) ,
				                                 (ar_pabo[idx].MaxY - ar_pabo[idx].MinY + 1 ) ,
				                                 kolor );
		// Border op de child objects
		kolor = iKol.getColor(sKolor);
		popupOID.childObjectsOID = new int[popupOID.childObjects.length];
		for(int i=0;i<popupOID.childObjects.length;i++) {
			popupOID.childObjectsOID[i] = -1;
			if( popupOID.childObjects[i] < 0 ) continue;
			    int rel_idx = popupOID.childObjects[i];
			
			    popupOID.childObjectsOID[i] = 
					this.drawRectangle( ar_pabo[rel_idx].MinX + this.PayLoadX , 
										ar_pabo[rel_idx].MinY + + this.PayLoadY , 
										(ar_pabo[rel_idx].MaxX - ar_pabo[rel_idx].MinX + 1 ) ,
										(ar_pabo[rel_idx].MaxY - ar_pabo[rel_idx].MinY + 1 ) ,
                    kolor );
		}

	}
	
	//---------------------------------------------------------------------------------
	private int[] do_ObjectsEnveloped( int idx )
	//---------------------------------------------------------------------------------
	{
		int[] list = new int[ar_pabo.length];
		for(int i=0;i<list.length;i++) list[i] = -1;
	    int minx = ar_pabo[idx].MinX;
	    int miny = ar_pabo[idx].MinY;
	    int maxx = ar_pabo[idx].MaxX;
	    int maxy = ar_pabo[idx].MaxY;
	    
	    int k=0;
	    for(int i=0;i<ar_pabo.length;i++)
	    {
	    	if( ar_pabo[i].removed ) continue;
	    	if( i == idx ) continue;
			if( (ar_pabo[i].MinX < minx) || (ar_pabo[i].MinX > maxx ) ) continue;
			if( (ar_pabo[i].MinY < miny) || (ar_pabo[i].MinY > maxy ) ) continue;
			
			list[k] = i;
			k++;
			if( k >= (list.length-1) ) {
				do_error("Array overrun");
				break;
			}
	    }
		return list;
	}
	
	//---------------------------------------------------------------------------------
	public void setIgnoreChanges()
	//---------------------------------------------------------------------------------
	{
		 ignoreChanges=true;	
	}
	
	//---------------------------------------------------------------------------------
	public boolean flushChanges()
	//---------------------------------------------------------------------------------
	{ 
		 if( ignoreChanges ) return true;
		 if( getNumberOfChanges() == 0 ) return true;
		 if( getNumberOfGraphChanges() > 0) {
		   if( dao.updateArchiveFile(ar_pabo) == false ) return false;
		   // if text has been removed or tipe has changed to/from text the TexTDAO needs to be updated
		   propagateObjectChangesToText();
		 }
		 if( getNumberOfTextChanges() > 0 ) {
		   cmcTextDAO rao = new cmcTextDAO(xMSet,logger);
		   if( rao.flushChangesToXML(ar_text , OriginalLanguage) == false ) return false;
		   rao=null;
		 }
		 cmcArchiveDAO archo = new cmcArchiveDAO(xMSet,logger);
		 return archo.reZipAllFiles(CurrentZipFileName);
		 //if( dao.reZipAllFiles(CurrentZipFileName) == false ) return false;
		 //return true;
	}

	// APR8 : added TextConfidence to XML -> so we need to keep track of object changes from now onwards too
	//---------------------------------------------------------------------------------
	private void propagateObjectChangesToText()
	//---------------------------------------------------------------------------------
	{
		// read the AR_POOL - if text/paragraph then fetch the AR_TEXT and compare - count changes 
		for(int i=0;i<ar_pabo.length;i++)
		{
		 if( (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH) && (ar_pabo[i].tipe != cmcProcEnums.PageObjectType.PARAGRAPH) ) continue;
		 int idx = getTextIdxViaUID(ar_pabo[i].UID);
		 if( idx < 0) continue;
		 
		 // compare the tipe and confidence
		 cmcProcEnums.TextConfidence targetConfi = ( ar_pabo[i].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) ? cmcProcEnums.TextConfidence.TEXT : cmcProcEnums.TextConfidence.NO_TEXT;
		 // compare remove
		 boolean targetRemove = ar_pabo[i].removed;
		 // do we need to save changes
		 if( (targetConfi == ar_text[idx].confidence) && (targetRemove == ar_text[idx].removed) ) continue;
         do_log(1,"Synchronize TextDAO UID [" + ar_text[idx].UID + "] Tipe [" + ar_pabo[i].tipe + "<=" + ar_text[idx].confidence + "] Remove[" + ar_pabo[i].removed + "<=" + ar_text[idx].removed + "]");
         // update the text object
         ar_text[idx].confidence = targetConfi;
         ar_text[idx].removed = targetRemove;
         ar_text[idx].hasChanged = true;
         ar_text[idx].changeDate = System.currentTimeMillis();
 		}
	}
	
	
	//---------------------------------------------------------------------------------
	private int getMaxBundleIndex()
	//---------------------------------------------------------------------------------
	{
		 int maxi=-1;
		 for(int i=0;i<ar_pabo.length;i++) 
		 {
		   if( maxi < ar_pabo[i].BundelIdx ) maxi = ar_pabo[i].BundelIdx;
		 }
		 return maxi;
	}
	
	//---------------------------------------------------------------------------------
	public boolean makeTextParagraph(Rectangle r)
	//---------------------------------------------------------------------------------
	{
		do_log(1,"Request for creating a new text paragraph " + r);
		
		// check of er overlap is met een andere text of paragraph
        // todo		
		
	    
		// link de onderliggende aan deze cluster
	    // Extend ar_pabo with 1 record : 
	    cmcGraphPageObject[] ar_temp = new cmcGraphPageObject[ar_pabo.length];
	    for(int i=0;i<ar_temp.length;i++) {
	    	cmcGraphPageObject y = new cmcGraphPageObject();
	    	ar_temp[i] = y;
	    }
	    if( kloon.copy_GraphPageObject( ar_pabo , ar_temp ) == false) return false;
	    ar_pabo = null;
	    ar_pabo = new cmcGraphPageObject[ar_temp.length+1];
	    for(int i=0;i<ar_pabo.length;i++) {
	    	cmcGraphPageObject y = new cmcGraphPageObject();
	    	ar_pabo[i] = y;
	    }
	    if( kloon.copy_GraphPageObject( ar_temp , ar_pabo ) == false ) return false;
	    ar_temp = null;
	    // maak een dummy bundle
	 	cmcGraphPageObject x = new cmcGraphPageObject();
	 	x.tipe = cmcProcEnums.PageObjectType.TEXTPARAGRAPH;
	 	x.UID = xMSet.mkNumericalUID();
	 	x.removed = false;
	 	x.isValid = true;
	 	x.MinX = r.x - this.PayLoadX ;
	 	x.MinY = r.y - this.PayLoadY;
	 	x.MaxX = r.x + r.width - this.PayLoadX;
	 	x.MaxY = r.y + r.height - this.PayLoadY;
	 	x.ClusterIdx = -1;
	 	x.BundelIdx = getMaxBundleIndex() + 1;
	    x.visi = cmcProcEnums.VisibilityType.VISIBLE;	
	    x.isSelected = false;
	    x.DrawObjectOID = -1;
	    x.hasChanged = true;
	    x.changetipe = cmcProcEnums.EditChangeType.CREATE;
	    // add
		ar_pabo[ ar_pabo.length - 1 ] = x;
		
		// link the clusters underneath to the bundle
		int[] childObjects = do_ObjectsEnveloped( ar_pabo.length - 1 );
		for(int i=0;i<childObjects.length;i++)
		{
			if( childObjects[i] < 0 ) continue;
			int rel_idx = childObjects[i];
			ar_pabo[rel_idx].BundelIdx = x.BundelIdx;
			ar_pabo[rel_idx].hasChanged = true;
			ar_pabo[rel_idx].changetipe = cmcProcEnums.EditChangeType.LINK;
		}
		
		//------------------------------------------------------------
		// PART TWO
		// create a text object and extent ar_text
		cmcTextObject[] rr_temp = new cmcTextObject[ar_text.length];
		kloonTextObject kloon2 = new kloonTextObject();
		for(int i=0;i<ar_text.length;i++)
		{
			rr_temp[i] = new cmcTextObject();
			kloon2.kloon( ar_text[i] , rr_temp[i] );
		}
		ar_text=null;
		ar_text = new cmcTextObject[ rr_temp.length + 1];
		for(int i=0;i<rr_temp.length;i++)
		{
			ar_text[i] = new cmcTextObject();
			kloon2.kloon( rr_temp[i] , ar_text[i] );
		}
		ar_text[ar_text.length-1] = new cmcTextObject();
		ar_text[ar_text.length-1].BundelIdx = x.BundelIdx;
		ar_text[ar_text.length-1].UID = x.UID;
		ar_text[ar_text.length-1].removed = false;
		ar_text[ar_text.length-1].hasChanged = true;
		ar_text[ar_text.length-1].changeDate = System.currentTimeMillis();
		rr_temp=null;
		kloon2 = null;
		//
		drawParagraph(ar_pabo.length - 1);
		//
		return true;
	}
	
	//---------------------------------------------------------------------------------
	public void setZipFileName(String sin)
	//---------------------------------------------------------------------------------
	{
		this.CurrentZipFileName = sin;
	}

	//---------------------------------------------------------------------------------
	public String getZipFileName()
	//---------------------------------------------------------------------------------
	{
		return CurrentZipFileName;
	}
	
	//---------------------------------------------------------------------------------
	public boolean undoRemoveViaIdx(int idx)
	//---------------------------------------------------------------------------------
	{
			if( (idx < 0) || (idx>=ar_pabo.length) ) return false;
		    //
			ar_pabo[idx].removed = false;
			ar_pabo[idx].hasChanged = true;
			ar_pabo[idx].changetipe = cmcProcEnums.EditChangeType.UNDO_REMOVE;
			// relink the objects that refer to this bundle
			int bundix = ar_pabo[idx].BundelIdx;
			// Relink the clusters underneath to the bundle
			int[] childObjects = do_ObjectsEnveloped( idx );
			int aantal=0;
			for(int i=0;i<childObjects.length;i++)
			{
				if( childObjects[i] < 0 ) continue;
				int rel_idx = childObjects[i];
				ar_pabo[rel_idx].BundelIdx = bundix;
				ar_pabo[rel_idx].hasChanged = true;
				ar_pabo[rel_idx].changetipe = cmcProcEnums.EditChangeType.RELINK;
				aantal++;
			}	
			do_log(1,"Undo Removed paragraph [" + idx + "] relinked [#=" + aantal + "]");
			//
			return true;
	}

	//---------------------------------------------------------------------------------
	private int getTextIdxViaUID(long UID)
	//---------------------------------------------------------------------------------
	{
		for(int i=0;i<ar_text.length;i++)
		{
			if( ar_text[i].UID == UID) return i;
		}
		return -1;
	}
	
	//---------------------------------------------------------------------------------
	public String getOriginalTextViaUID(long UID)
	//---------------------------------------------------------------------------------
	{
		String Ret="";
		int idx = getTextIdxViaUID(UID);
		if( (idx<0) || (idx>=ar_text.length) ) return Ret;
		Ret = ar_text[idx].TextFrom == null ? "" : ar_text[idx].TextFrom;
		return Ret;
	}
	
	//---------------------------------------------------------------------------------
	public String getTranslatedTextViaUID(long UID , String sLang)
	//---------------------------------------------------------------------------------
	{
		String Ret="";
		int idx = getTextIdxViaUID(UID);
		if( (idx<0) || (idx>=ar_text.length) ) return Ret;
        int tix = xMSet.xU.getIdxFromList( xMSet.getLanguageList() , sLang );
        if( (tix < 0) || ( tix >= ar_text[idx].TextTranslated.length) ) {
        	return sLang + " " + UID;	
        }
		return ar_text[idx].TextTranslated[tix];
	}
	
	//---------------------------------------------------------------------------------
	public boolean updateOrigTextViaUID( long UID , String sText)
	//---------------------------------------------------------------------------------
	{
		int idx = getTextIdxViaUID(UID);
		if( (idx<0) || (idx>=ar_text.length) ) {
			do_error("Could not find UID[" + UID +"] to update text");
			return false;
		}
debug("UID=" + UID + " " + sText);
        //		
		ar_text[idx].TextFrom = sText;
		ar_text[idx].hasChanged = true;
		ar_text[idx].changeDate = System.currentTimeMillis();
		//
		return true;
	}
	//---------------------------------------------------------------------------------
	public boolean updateTransTextViaUID( long UID , String sText, String sLang)
	//---------------------------------------------------------------------------------
	{
			int idx = getTextIdxViaUID(UID);
			if( (idx<0) || (idx>=ar_text.length) ) return false;
			//
			int tix = xMSet.xU.getIdxFromList( xMSet.getLanguageList() , sLang );
		    if( (tix < 0) || ( tix >= ar_text[idx].TextTranslated.length) ) {
		    	    System.err.println("Unknown language on UID=" + UID + " " + sText + " " + sLang);
		    	  return false;	
		     }
debug("UID=" + UID + " " + sText + " " + sLang);
	        //	
			ar_text[idx].TextTranslated[tix] = sText;
			ar_text[idx].hasChanged = true;
			ar_text[idx].changeDate = System.currentTimeMillis();
			//
			return true;
	}

	//---------------------------------------------------------------------------------
	public boolean updateTextIndicator(int idx , boolean ib)
	//---------------------------------------------------------------------------------
	{
		if( (idx<0) || (idx>=ar_pabo.length)) return false;
		//int oid = zoekOIDViaIdx(idx);
		int oid = ar_pabo[idx].DrawObjectOID;
		if( oid < 0 ) return false;
debug("(updateTextIndicator) [idx=" + idx + "] [oid=" + oid + "] [OID=" + ar_pabo[idx].DrawObjectOID + "] ["+ ib + "] [UID=" + ar_pabo[idx].UID + "]" );
		ar_pabo[idx].tipe = (ib == true) ? cmcProcEnums.PageObjectType.TEXTPARAGRAPH : cmcProcEnums.PageObjectType.PARAGRAPH;
		ar_pabo[idx].hasChanged = true;
		if( ar_pabo[idx].changetipe != cmcProcEnums.EditChangeType.CREATE ) {  // ignore to set changetipe if this is a newly added paragraph
		 if( ar_pabo[idx].tipe == cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) ar_pabo[idx].changetipe = cmcProcEnums.EditChangeType.TO_TEXT;
	                                                               	    else ar_pabo[idx].changetipe = cmcProcEnums.EditChangeType.TO_NO_TEXT;
		}
		removeFromStackViaOID(oid);
		drawParagraph(idx);
		return true;
	}

	//---------------------------------------------------------------------------------
	public boolean updateTextIndicatorViaXY(int x , int y , boolean ib)
	//---------------------------------------------------------------------------------
	{
		return updateTextIndicator( getSelectedParagraph( x , y) , ib);
	}
	
	//---------------------------------------------------------------------------------
	public int getNumberOfChanges()
	//---------------------------------------------------------------------------------
	{
		 return getNumberOfGraphChanges() + getNumberOfTextChanges();
	}
	//---------------------------------------------------------------------------------
	private int getNumberOfGraphChanges()
	//---------------------------------------------------------------------------------
	{
		int nbr=0;
		for(int i=0;i<ar_pabo.length;i++)
		{
		    	if( ar_pabo[i].hasChanged ) nbr++;
		}
        do_log(9,"(getNumberOfGraphChanges) " + nbr);
		return nbr;
	}
	//---------------------------------------------------------------------------------
	private int getNumberOfTextChanges()
	//---------------------------------------------------------------------------------
	{
		 int nbr=0;
		 for(int i=0;i<ar_text.length;i++)
		 {
			 if( ar_text[i].hasChanged ) nbr++;
		 }
         do_log(9,"(getNumberOfTextChanges) " + nbr);
	     return nbr;	
	}
	//---------------------------------------------------------------------------------
	public cmcGraphPageObject[] getPageObjectArray()
	//---------------------------------------------------------------------------------
	{
		return ar_pabo;
	}
	//---------------------------------------------------------------------------------
	public cmcTextObject[] getTextObjectArray()
	//---------------------------------------------------------------------------------
	{
		return ar_text;
	}

}
