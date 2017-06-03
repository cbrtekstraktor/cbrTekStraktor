package bubbleProcessing;

import generalImagePurpose.cmcImageRoutines;

import java.awt.Point;
import java.util.ArrayList;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcGraphPageDAO;
import drawing.cmcGraphPageObject;

public class cmcBubbleMaker {
	
	enum Orientation { VERTICAL , HORIZONTAL , UNKNOWN }
	enum EdgeType { TOP_EDGE , BOTTOM_EDGE , LEFT_EDGE , RIGHT_EDGE , NOT_AN_EDGE }
	enum OverlapType { ISOLATED , TRUE_OVERLAP , HORIZONTAL_OVERLAP , VERTICAL_OVERLAP , UNKNOWN }
	
	boolean DUMP_MODE = false;
	boolean USE_TEST_DATA = false;
	
	long UIDSeed = 0L;
	int NULLORDER=99999;
	long totUID=100L;
	
	cmcProcSettings xMSet=null;
	logLiason logger=null;

	int TOP_LEFT     = 0;
	int TOP_RIGHT    = 1;
	int BOTTOM_LEFT  = 2;
	int BOTTOM_RIGHT = 3;
	
	int VALUE = 0;
	int INDEX = 1;
	int ORDER = 2;
	
	int imgBreedte = -1;
	int imgHoogte = -1;
	
	class cmcLine
	{
	   long UID=0L;
	   Point from;
	   Point to;
	   Orientation orient = Orientation.UNKNOWN;
	   int orderHorizontal=NULLORDER;
	   int orderVertical=NULLORDER;
	   EdgeType edgetipe = EdgeType.NOT_AN_EDGE;
	   cmcLine(int x1 , int y1 , int x2 , int y2)
	   {
		   UID = totUID++;
		   edgetipe = EdgeType.NOT_AN_EDGE;
		   from = new Point(x1,y1);
		   to = new Point(x2,y2);
		   orderHorizontal=NULLORDER;
		   orderVertical=NULLORDER;
		   // Orientation + ensure that the first point is always the lesser/lower
		   if( x1 == x2 ) {
			   orient = Orientation.VERTICAL;
			   if( y1 > y2 ) {  // switch highest is now first
				   from.y = y2;
				   to.y = y1;
			   }
		   }
		   else
		   if( y1 == y2 ) {
			   orient = Orientation.HORIZONTAL;
			   if( x1 > x2 ) {  // switch
				   from.x = x2;
				   to.x = x1;
			   }
		   }
		   else {
		       orient = Orientation.UNKNOWN;
		   }
	   }
	}
	
	class cmcShape
	{
		long UID=-1L;
		int bubbleIdx=-1;
	    Point[] encoCorners = new Point[4];  // encompassing corners -> virtual rectangle for all lines
	    Point[] extrmCorners = new Point[4];
	    cmcLine[] lines = new cmcLine[1000];
	    int orderVertical = NULLORDER;
	    int orderHorizontal = NULLORDER;
	    boolean consolidatedshape=false;
	    cmcShape()
	    {
	    	orderVertical = NULLORDER;
	  	    orderHorizontal = NULLORDER;
	  	    consolidatedshape=false;
	  	 	for(int i=0;i<encoCorners.length;i++)
	    	{
	    		encoCorners[i] = new Point(-1,-1);
	    		extrmCorners[i] = new Point(-1,-1);
	    	}
	    	// do not initialize the lines => require NULL to detect end of list
	    }
	    
	}
	
	
	class cmcBubble
	{
		long UID=-1L;
		int bubbleIdx=-1;
		int OverlappingShapesConsolidated=0;
	    Point[] corners = new Point[4];
	    cmcShape[] shapes = new cmcShape[1000];
	}
	
	cmcBubble[] ar_bubl = null;
	cmcShape[] ar_shpe = null;
	
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
    private void DUMP(boolean regular, String s)
    //------------------------------------------------------------
    {
    	if( this.DUMP_MODE ) {
    		System.out.println(s);	
    	}
    	else {
    	  if( regular ) do_log(5,s);	
    	}
    }
    
    // ---------------------------------------------------------------------------------
	public cmcBubbleMaker(cmcProcSettings is, logLiason ilog)
	// ---------------------------------------------------------------------------------
	{
	   xMSet = is;
	   logger = ilog;
	}

	//------------------------------------------------------------
	public boolean initialize(String ArchiveFileName )
	//------------------------------------------------------------
	{
		cmcGraphPageDAO dao = new cmcGraphPageDAO( xMSet , ArchiveFileName , logger );
		if( dao.IsDaoReady() == false ) {
			do_error("Could not read object information from [" + ArchiveFileName + "]");
			return false;
		}
		cmcGraphPageObject[] ar_pabo = dao.readXML();
		// sizes
		imgBreedte = dao.getImageWidth();
		imgHoogte  = dao.getImageHeigth();
		
		// use the isselected flag to create a list of bundels to be processed
		int aant=0;
		for(int i=0;i<ar_pabo.length;i++)
		{
			ar_pabo[i].isSelected = false;
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].tipe != cmcProcEnums.PageObjectType.TEXTPARAGRAPH ) continue;
			//
			ar_pabo[i].isSelected = true;
			aant++;
		}
		DUMP(true,"Found [" + aant + "] bubbles to create");
	    //		
		ar_bubl = new cmcBubble[ aant ];
		int k=-1;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].isSelected == false ) continue;
			k++;
			ar_bubl[k] = new cmcBubble();
			ar_bubl[k].UID = ar_pabo[i].UID;
			ar_bubl[k].bubbleIdx = ar_pabo[i].BundelIdx;
			ar_bubl[k].corners[TOP_LEFT] = new Point( ar_pabo[i].MinX , ar_pabo[i].MinY );
			ar_bubl[k].corners[TOP_RIGHT] = new Point( ar_pabo[i].MaxX , ar_pabo[i].MinY );
			ar_bubl[k].corners[BOTTOM_LEFT] = new Point( ar_pabo[i].MinX , ar_pabo[i].MaxY );
			ar_bubl[k].corners[BOTTOM_RIGHT] = new Point( ar_pabo[i].MaxX , ar_pabo[i].MaxY );
			
		}
		// check
		int err=0;
		for(int i=0;i<aant;i++)
		{
			if( ar_bubl[i].UID < 0L ) { do_error("Bubble [" + i + "] not instantiated"); err++; }
			if( ar_bubl[i].bubbleIdx < 0 ) {do_error("Bubble [" + i + "] not instantiated"); err++; }
		}
		if( err > 0 ) return false;
		DUMP(true,"Created [" + ar_bubl.length + "] bubbles");
		
		// now scan through the list again to attach the letters to the bubbles
		aant=0;
		for(int i=0;i<ar_pabo.length;i++)
		{
			ar_pabo[i].isSelected = false;
			if( ar_pabo[i].removed ) continue;
			if( ar_pabo[i].tipe != cmcProcEnums.PageObjectType.LETTER ) continue;
		    // does it belong to a bubble
			int idx=-1;
			for(int j=0;j<ar_bubl.length;j++)
			{
				if( ar_bubl[j].bubbleIdx == ar_pabo[i].BundelIdx ) { idx = ar_bubl[j].bubbleIdx; break; }
			}
			if( idx < 0 ) continue;
			//
			ar_pabo[i].isSelected = true;
			aant++;
		}
		DUMP(true,"Found [" + aant + "] letters to attach");
        //
		ar_shpe = new cmcShape[ aant ];
		k=-1;
		for(int i=0;i<ar_pabo.length;i++)
		{
			if( ar_pabo[i].isSelected == false ) continue;
			k++;
			ar_shpe[k] = new cmcShape();
			ar_shpe[k].UID = ar_pabo[i].UID;
			ar_shpe[k].bubbleIdx = ar_pabo[i].BundelIdx;
			ar_shpe[k].encoCorners[TOP_LEFT] = new Point( ar_pabo[i].MinX , ar_pabo[i].MinY );
			ar_shpe[k].encoCorners[TOP_RIGHT] = new Point( ar_pabo[i].MaxX , ar_pabo[i].MinY );
			ar_shpe[k].encoCorners[BOTTOM_LEFT] = new Point( ar_pabo[i].MinX , ar_pabo[i].MaxY );
			ar_shpe[k].encoCorners[BOTTOM_RIGHT] = new Point( ar_pabo[i].MaxX , ar_pabo[i].MaxY );
		}
		// check
		err=0;
		for(int i=0;i<aant;i++)
		{
			if( ar_shpe[i].UID < 0L ) { do_error("Letter [" + i + "] not instantiated"); err++; }
			if( ar_shpe[i].bubbleIdx < 0 ) {do_error("Letter [" + i + "] not instantiated"); err++; }
		}
		if( err > 0 ) return false;
		DUMP(true,"Created [" + ar_shpe.length + "] letters");
	    
		// attach the ar_shpe to the bubbles
		for(int i=0;i<ar_bubl.length;i++)
		{
		  int odx=0;
		  for(int j=0;j<ar_shpe.length;j++)
		  {
			  if( ar_shpe[j].bubbleIdx != ar_bubl[i].bubbleIdx ) continue;
			  ar_bubl[i].shapes[odx] = ar_shpe[j];
			  odx++;
		  }
		  DUMP(true,"Attached [" + odx + "] shapes to bubble [Idx=" + ar_bubl[i].bubbleIdx + "]");
		}
		
	    // debug
		if( USE_TEST_DATA )  loadtestdata();
	    
		
		// remove any of the overlaps on the rectangular shape - can be due to italic characters
		findAndRemoveOverlap(true);
		
		// Set the lines once the corners have been read and overlap removed	
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
		   cmcShape x = ar_bubl[i].shapes[j];
		   if( x == null ) continue;
		   x.lines[0] = new cmcLine( x.encoCorners[TOP_LEFT].x ,  x.encoCorners[TOP_LEFT].y ,  x.encoCorners[TOP_RIGHT].x ,  x.encoCorners[TOP_RIGHT].y );
		   x.lines[1] = new cmcLine( x.encoCorners[TOP_RIGHT].x ,  x.encoCorners[TOP_RIGHT].y ,  x.encoCorners[BOTTOM_RIGHT].x ,  x.encoCorners[BOTTOM_RIGHT].y );
		   x.lines[2] = new cmcLine( x.encoCorners[BOTTOM_RIGHT].x ,  x.encoCorners[BOTTOM_RIGHT].y ,  x.encoCorners[BOTTOM_LEFT].x ,  x.encoCorners[BOTTOM_LEFT].y );
		   x.lines[3] = new cmcLine( x.encoCorners[BOTTOM_LEFT].x ,  x.encoCorners[BOTTOM_LEFT].y ,  x.encoCorners[TOP_LEFT].x ,  x.encoCorners[TOP_LEFT].y );
		  }
		}
		
		//
		recalculateCharacteristics();
		//

		// start of finding best fitting contour
		findBestFittingContourInAllBubbles();
		
		// report
		dumpreport();
		return true;
	}
	
	//------------------------------------------------------------
	private void recalculateCharacteristics()
	//------------------------------------------------------------
	{
		// encompassing rectangle
		setEncompassingCorners();
		// Line orientation
		setLineOrientation();
		// order lines
		setLineOrder();
		// set the edgetype
		setEdgeTypes();
		// find the extremities
		setExtremities();
		// order the shapes
		setShapeOrder();
		//
		boolean hasOverlap = findAndRemoveOverlap(false);
		if( hasOverlap ) {
			do_error("Found an overlapping rectangular shape ==> will stop");
		}
		//
		runReport();
	}
	
	//------------------------------------------------------------
	private void setLineOrientation()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
			  cmcShape x = ar_bubl[i].shapes[j];
			  if( x == null ) continue;
	          for(int z=0;z<x.lines.length;z++)
	          {
	        	 cmcLine y = x.lines[z];
	        	 if( y == null ) continue;
	 	         if( y.from.x == y.to.x )  y.orient = Orientation.VERTICAL;
	 	         else 
	 	         if( y.from.y == y.to.y )  y.orient = Orientation.HORIZONTAL;
	 	         else {
	 	        	 do_error("Unsupported orientation [" + y.from + " " + y.from + "]");
	 	        	y.orient = Orientation.UNKNOWN;
	 	         }
	 	         // ensure that from is lower the to
	 	         if( y.orient == Orientation.HORIZONTAL ) {
	 	        	 if( y.from.x > y.to.x ) {
	 	        		 int old = y.from.x;
	 	        		 y.from.x = y.to.x;
	 	        		 y.to.x = old;
	 	        		 do_error("Bizar -> switched X");
	 	        	 }
	 	         }
	 	        if( y.orient == Orientation.VERTICAL ) {
	 	        	 if( y.from.y > y.to.y ) {
	 	        		 int old = y.to.y;
	 	        		 y.from.y = y.to.y;
	 	        		 y.to.y = old;
	 	        		 do_error("Bizar -> switched Y");
	 	        	 }
	 	         }
	 	         
	          }
		  }
		}
	}

	//------------------------------------------------------------
	private void setLineOrder()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
			  cmcShape x = ar_bubl[i].shapes[j];
			  if( x == null ) continue;
			  setHorizontaLineOrder(x.lines);
			  setVerticalLineOrder(x.lines);
	      }
		}
	}

	//------------------------------------------------------------
	private int[][] bubbleSort(int[][] list)
	//------------------------------------------------------------
	{
		int[][] sorted = new int[list.length][3];
		for(int i=0;i<list.length;i++)
		{
			sorted[i][VALUE] = list[i][VALUE];
			sorted[i][INDEX] = list[i][INDEX];
			sorted[i][ORDER] = NULLORDER;
		}
		for(int i=0;i<list.length;i++)
		{
			boolean swap=false;
			for(int j=0;j<(list.length-1);j++)
			{
				if( sorted[j][VALUE] > sorted[j+1][VALUE]) {
					int z0 = sorted[j][VALUE];
					int z1 = sorted[j][INDEX]; 
					sorted[j][VALUE] = sorted[j+1][VALUE];
					sorted[j][INDEX] = sorted[j+1][INDEX];
					sorted[j+1][VALUE] = z0;
					sorted[j+1][INDEX] = z1;
					swap=true;
				}
			}
			if ( swap == false ) break;
		}
		// set the order on the 3rd field
		// when the x or y coordinate is the sam , order will set to be the same
	    for(int i=0;i<sorted.length;i++)  sorted[i][ORDER] = i;
	    int prevval = -1;
	    int curorder = 0;
	    for(int i=0;i<sorted.length;i++)
	    {
	    	if( i == 0 ) { prevval = sorted[i][VALUE]; sorted[i][ORDER] = curorder; continue; }
	    	if( prevval == sorted[i][VALUE] ) {
	    		sorted[i][ORDER] = curorder;
	    	}
	    	else {
	    		curorder++;
	    		sorted[i][ORDER]=curorder;
	    		prevval = sorted[i][VALUE];
	    	}
	    }
		return sorted;
	}

	//------------------------------------------------------------
	private void setLineOrder(cmcLine[] lines , Orientation tipe)
	//------------------------------------------------------------
	{
	   int[][] olist = new int[lines.length][3];
	   for(int i=0;i<lines.length;i++) 
	   { 
		      olist[i][VALUE] = NULLORDER;
		      olist[i][INDEX] = i;
		      olist[i][ORDER] = NULLORDER;
		      if( lines[i] == null ) continue;
		      if( (lines[i].orient == Orientation.HORIZONTAL) && (tipe == Orientation.HORIZONTAL)  ) {
		    	  olist[i][VALUE] = lines[i].from.x;
		      }
		      else
		      if( (lines[i].orient == Orientation.VERTICAL) && (tipe == Orientation.VERTICAL) ) {
		    	  olist[i][VALUE] = lines[i].from.y;
		      }
	   }
	   // reset
	   for(int i=0;i<lines.length;i++) 
	   {
      	if( lines[i] == null ) continue;
      	if( tipe == Orientation.VERTICAL ) lines[i].orderVertical = NULLORDER;
      	if( tipe == Orientation.HORIZONTAL )lines[i].orderHorizontal = NULLORDER;
       }
       // bubbelsort
       int[][] sorted = bubbleSort(olist);
       if( tipe == Orientation.HORIZONTAL ) {
//for(int i=0;i<sorted.length;i++) if( sorted[i][0] != NULLORDER ) do_log(1,"HORIZONTAL " + i + " " + sorted[i][0] + " " + sorted[i][1] + " " + sorted[i][2]);
         for(int i=0;i<sorted.length;i++) 
	     {
//if(  sorted[i][0] != NULLORDER ) do_log(1,"[" + i + "][0=" + sorted[i][0] + "] [1=" + sorted[i][1] + "] [2=" + sorted[i][2] + "]");      	 
        	if( sorted[i][VALUE] == NULLORDER ) continue; 
        	int idx = sorted[i][INDEX];
        	if( idx == NULLORDER ) continue; // is an error shoudl be reported
        	if( lines[idx] == null ) continue;
        	lines[idx].orderHorizontal = sorted[i][ORDER];
	     }
//for(int i=0;i<sorted.length;i++) if( sorted[i][0] != NULLORDER ) do_log(1,"HORIZONTAL " + i + " " + sorted[i][0] + " " + sorted[i][1] + " " + sorted[i][2]);
         
       }
       else
   	   if( tipe == Orientation.VERTICAL ) {
 //for(int i=0;i<sorted.length;i++) if( lines[i] != null ) do_log(1,"->" + i + " V=" + lines[i].orderVertical + " H=" + lines[i].orderHorizontal );
         for(int i=0;i<sorted.length;i++) 
  	     {
        	if( sorted[i][VALUE] == NULLORDER ) continue;
            int idx = sorted[i][INDEX];
        	if( idx == NULLORDER ) continue;
          	if( lines[idx] == null ) continue;
          	lines[idx].orderVertical = sorted[i][ORDER];
  	     }
       }
   	   else {
   		   do_error("Unsupported orientation for sort");
   	   }
       
	}

	//------------------------------------------------------------
	private void setHorizontaLineOrder(cmcLine[] lines)
	//------------------------------------------------------------
	{
	   setLineOrder( lines , Orientation.HORIZONTAL );
	}

	//------------------------------------------------------------
	private void setVerticalLineOrder( cmcLine[] lines)
	//------------------------------------------------------------
	{
		setLineOrder( lines , Orientation.VERTICAL );
	}
	
	//------------------------------------------------------------
	private void dumpreport()
	//------------------------------------------------------------
	{
		int breedte = imgBreedte;
		int hoogte = imgHoogte;;
		int[]  fig = new int[breedte*hoogte];
		
		for(int i=0;i<fig.length;i++) fig[i] = 0xffffffff;
		
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  //do_log(1,"bubble "+i);
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
			  cmcShape x = ar_bubl[i].shapes[j];
			  if( x == null ) continue;
			  //do_log(1,"shape"+j);
			  drawlines( x.lines , fig , breedte);
		  }
		}
			
		// public void writePixelsToFile(int[] dump , int iwidth , int iheigth , String FNaam , cmcImageRoutines.ImageType Tipe)
		cmcImageRoutines ir = new cmcImageRoutines(logger);
		ir.writePixelsToFile( fig , breedte , hoogte , xMSet.getRootDir() + xMSet.xU.ctSlash + "junk.png" , cmcImageRoutines.ImageType.RGB );
		ir = null;
	}
	
	//------------------------------------------------------------
	private void drawlines( cmcLine[] lines , int[] fig , int breedte)
	//------------------------------------------------------------
	{
		for(int i=0;i<lines.length;i++)
		{
		 cmcLine x = lines[i];
		 if( x == null ) continue;
		 //do_log(1,"" + x.from + " " + x.to );
		 // kleur
		 int kleur = 0xff000000;
		 if( x.orient == Orientation.HORIZONTAL ) kleur = 0xffff0000;
		 if( x.orient == Orientation.VERTICAL ) kleur = 0xff00ff00;
		 // horizontaal
		 if( x.from.y == x.to.y ) 
		 {
			 int z =  Math.abs(x.from.x - x.to.x) + 1;
			 int a = Math.min( x.from.x , x.to.x );
			 for(int k=0;k<z;k++) fig[ (x.from.y * breedte) + k + a] = kleur;
		 }
		 else if( x.from.x == x.to.x ) {
			 int z =  Math.abs(x.from.y - x.to.y) + 1;
			 int a = Math.min( x.from.y , x.to.y );
			 for(int k=0;k<z;k++) fig[ ((k +a)* breedte) + x.from.x ] = kleur;
		 }
		 else {
			 do_error("unsupported");
		 }
			 
		}
	}

	//------------------------------------------------------------
	private void setEncompassingCorners()
	//------------------------------------------------------------
	{		
		for(int i=0;i<ar_bubl.length;i++)
	    {
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
		   cmcShape x = ar_bubl[i].shapes[j];
		   if( x == null ) continue;
		   setEncompassingCornersOnShape( x );
	      }
		}
	}

	//------------------------------------------------------------
	private void setEncompassingCornersOnShape(cmcShape shp)
	//------------------------------------------------------------
	{	
		for(int i=0;i<shp.encoCorners.length;i++) {
			  shp.encoCorners[i].x = -1;
			  shp.encoCorners[i].y = -1;
		}
		int minX=-1;
		int minY=-1;
		int maxX=-1;
		int maxY=-1;
		for(int i=0;i<shp.lines.length;i++)
		{
			cmcLine x = shp.lines[i];
			if( x == null ) continue;
			if( maxY == -1 ) {
				minX = x.from.x;
				minY = x.from.y;
				maxX = x.to.x;
				maxY = x.to.y;
			}
			//
			if( x.from.x > maxX) maxX = x.from.x;
			if( x.from.y > maxY) maxY = x.from.y;
			if( x.from.x < minX) minX = x.from.x;
			if( x.from.y < minY) minY = x.from.y;
			//
			if( x.to.x > maxX) maxX = x.to.x;
			if( x.to.y > maxY) maxY = x.to.y;
			if( x.to.x < minX) minX = x.to.x;
			if( x.to.y < minY) minY = x.to.y;
		}
		shp.encoCorners[TOP_LEFT]     = new Point( minX , minY );
	    shp.encoCorners[TOP_RIGHT]    = new Point( maxX , minY );
		shp.encoCorners[BOTTOM_RIGHT] = new Point( maxX , maxY );
		shp.encoCorners[BOTTOM_LEFT]  = new Point( minX , maxY );
		
	}
	
	//------------------------------------------------------------
	private void setEdgeTypes()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
		   cmcShape x = ar_bubl[i].shapes[j];
		   if( x == null ) continue;
		   setEdgeTypeOnShape( x );
	      }
		}
	}

	//------------------------------------------------------------
	private void setEdgeTypeOnShape( cmcShape shp)
	//------------------------------------------------------------
	{
		for(int i=0;i<shp.lines.length; i++) {
			if( shp.lines[i] == null ) continue;
			shp.lines[i].edgetipe = EdgeType.NOT_AN_EDGE;
		}
	
		// sweep down
		// overlaps vertical line
		// there migh tbe more RIGHT/LEFT edges dependng on the y coord
		for(int y=shp.encoCorners[TOP_LEFT].y ; y<=shp.encoCorners[BOTTOM_LEFT].y ; y++)
		{
			int minX = 999999;
			int maxX = -1;
			int minIdx=-1;
			int maxIdx=-1;
			for(int i=0;i<shp.lines.length;i++)
			{
				cmcLine z = shp.lines[i];
				if( z == null ) continue;
				if( z.orient != Orientation.VERTICAL ) continue;
				if( (y < z.from.y) || (y > z.to.y) ) continue;  // y is within range of vertical line
				if( (minX > z.from.x ) ) {   // left minimum
					minX = z.from.x;
					minIdx =i;
				}
				if( (maxX < z.from.x ) ) {  // right maximum
					maxX = z.from.x;
					maxIdx =i;
				}
			}
			if( minIdx >= 0 ) shp.lines[minIdx].edgetipe = EdgeType.LEFT_EDGE;
			if( maxIdx >= 0 ) shp.lines[maxIdx].edgetipe = EdgeType.RIGHT_EDGE;
		}
		// sweep to the right
		for(int x=shp.encoCorners[TOP_LEFT].x ; x<=shp.encoCorners[TOP_RIGHT].x ; x++)
		{
			int minY = 999999;
			int maxY = -1;
			int minIdx=-1;
			int maxIdx=-1;
			for(int i=0;i<shp.lines.length;i++)
			{
				cmcLine z = shp.lines[i];
				if( z == null ) continue;
          		if( z.orient != Orientation.HORIZONTAL ) continue;
          		if( (x < z.from.x) || (x > z.to.x) ) continue;  // x is within range of HORZ line
				if( (minY > z.from.y ) ) {   // top minimum
					minY = z.from.y;
					minIdx =i;
				}
				if( (maxY < z.from.y ) ) {  // right maximum
					maxY = z.from.y;
					maxIdx =i;
				}
    		}
			if( minIdx >= 0 ) shp.lines[minIdx].edgetipe = EdgeType.TOP_EDGE;
			if( maxIdx >= 0 ) shp.lines[maxIdx].edgetipe = EdgeType.BOTTOM_EDGE;
		}
        // check
		int tpedge=0;
		int btedge=0;
		int lfedge=0;
		int riedge=0;
		for(int i=0;i<shp.lines.length;i++)
		{
			cmcLine z = shp.lines[i];
			if( z == null ) continue;
      	    if( z.edgetipe == EdgeType.LEFT_EDGE ) lfedge++;
      	    if( z.edgetipe == EdgeType.RIGHT_EDGE ) riedge++;
      	    if( z.edgetipe == EdgeType.TOP_EDGE ) tpedge++;
      	    if( z.edgetipe == EdgeType.BOTTOM_EDGE ) btedge++;
    	}
		if( lfedge == 0) do_error("Could not find left edge [" + shp.UID + "]");
		if( riedge == 0) do_error("Could not find right edge [" + shp.UID + "]");
		if( tpedge == 0) do_error("Could not find top edge [" + shp.UID + "]");
		if( btedge == 0) do_error("Could not find bottom edge [" + shp.UID + "]");
		
	}
	
	//------------------------------------------------------------
	private void setExtremities()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_bubl.length;i++)
		{
		  if( ar_bubl[i] == null ) continue;
		  for(int j=0;j<ar_bubl[i].shapes.length;j++)
		  {
			  cmcShape x = ar_bubl[i].shapes[j];
			  if( x == null ) continue;
			  setExtremitiesOnShape( x );
	      }
		}
	}
	
	private void setExtremitiesOnShape(cmcShape shp)
	{
	  // reset
	  for(int i=0;i<shp.extrmCorners.length;i++) {
		  shp.extrmCorners[i].x = -1;
		  shp.extrmCorners[i].y = -1;
	  }
	  
      // TOPLEFT =  from all vertical most to the left take the one with lowest y coordinate on the from
	  int miny=9999;
	  int minx=9999;
	  int idx=-1;
	  for(int i=0;i<shp.lines.length;i++)
	  {
		  cmcLine x = shp.lines[i];
		  if( x == null ) continue;
		  if( x.edgetipe != EdgeType.LEFT_EDGE ) continue;
		  if( x.from.x < minx ) {  // leftmost
			  minx = x.from.x;
			  miny = 9999;
			  idx=i;
		  }
		  if( x.from.y < miny ) { // smallest y
			  miny = x.from.y;
			  idx=i;
		  }
	  }
	  if( idx >= 0 ) { 
		  shp.extrmCorners[TOP_LEFT].x = shp.lines[idx].from.x; 
		  shp.extrmCorners[TOP_LEFT].y = shp.lines[idx].from.y; 
	  }
	  else do_error("could not find extreme left top corner on " + shp.UID );
	  
	  // TOP right - RIGHT edge which has the highest x and lowest y
	  miny=9999;
	  int maxx=-9999;
	  idx=-1;
	  for(int i=0;i<shp.lines.length;i++)
	  {
		  cmcLine x = shp.lines[i];
		  if( x == null ) continue;
		  if( x.edgetipe != EdgeType.RIGHT_EDGE ) continue;
		  if( x.from.x > maxx ) {  // leftmost
			  maxx = x.from.x;
			  miny = -9999;
			  idx=i;
		  }
		  if( x.from.y < miny ) { // smallest y
			  miny = x.from.y;
			  idx=i;
		  }
	  }
	  if( idx >= 0 ) { 
		  shp.extrmCorners[TOP_RIGHT].x = shp.lines[idx].from.x; 
		  shp.extrmCorners[TOP_RIGHT].y = shp.lines[idx].from.y; 
	  }
	  else do_error("could not find extreme right top corner on " + shp.UID );
	  
	  // BOTTOM left - LEFT EDGE with lowest x and highest y on the to
	  int maxy=-9999;
	  minx=9999;
	  idx=-1;
	  for(int i=0;i<shp.lines.length;i++)
	  {
		  cmcLine x = shp.lines[i];
		  if( x == null ) continue;
		  if( x.edgetipe != EdgeType.LEFT_EDGE ) continue;
		  if( x.from.x < minx ) {  // leftmost
			  minx = x.from.x;
			  maxy = -9999;
			  idx=i;
		  }
		  if( x.to.y > maxy ) { // smallest y
			  maxy = x.to.y;
			  idx=i;
		  }
	  }
	  if( idx >= 0 ) { 
		  shp.extrmCorners[BOTTOM_LEFT].x = shp.lines[idx].from.x; 
		  shp.extrmCorners[BOTTOM_LEFT].y = shp.lines[idx].to.y; 
	  }
	  else do_error("could not find extreme bottom left corner on " + shp.UID );
	  
	  // BOTTOM right - right EDGE with lowest x and highest y on the to
	  maxy=-9999;
	  maxx=-9999;
	  idx=-1;
	  for(int i=0;i<shp.lines.length;i++)
	  {
		  cmcLine x = shp.lines[i];
		  if( x == null ) continue;
		  if( x.edgetipe != EdgeType.RIGHT_EDGE ) continue;
		  if( x.from.x > maxx ) {  // right most
			  maxx = x.from.x;
			  maxy = -9999;
			  idx=i;
		  }
		  if( x.to.y > maxy ) { // smallest y on the to
			  maxy = x.to.y;
			  idx=i;
		  }
	  }
	  if( idx >= 0 ) { 
		  shp.extrmCorners[BOTTOM_RIGHT].x = shp.lines[idx].from.x; 
		  shp.extrmCorners[BOTTOM_RIGHT].y = shp.lines[idx].to.y; 
	  }
	  else do_error("could not find extreme bottom right corner on " + shp.UID );
	  
	  
	}

	private void setShapeOrder()
	{
		  for(int i=0;i<ar_bubl.length;i++)
		  {
			if( ar_bubl[i] == null ) continue;
			setShapeOrderInBubble( ar_bubl[i].shapes , Orientation.HORIZONTAL);
			setShapeOrderInBubble( ar_bubl[i].shapes , Orientation.VERTICAL);
		  }
	}
	
	private void setShapeOrderInBubble(cmcShape[] shapes , Orientation tipe)
	{
	   int[][] olist = new int[shapes.length][3];
	   for(int i=0;i<shapes.length;i++) 
	   { 
			      olist[i][VALUE] = NULLORDER;
			      olist[i][INDEX] = i;
			      olist[i][ORDER] = NULLORDER;
			      if( shapes[i] == null ) continue;
			      if( tipe == Orientation.HORIZONTAL  ) {
			    	  olist[i][VALUE] = shapes[i].extrmCorners[TOP_LEFT].x;
			      }
			      else
			      if( tipe == Orientation.VERTICAL ) {
			    	  olist[i][VALUE] = shapes[i].extrmCorners[TOP_LEFT].y;
			      }
	   }
	   // reset
	   for(int i=0;i<shapes.length;i++) 
	   {
	      	if( shapes[i] == null ) continue;
	      	if( tipe == Orientation.VERTICAL ) shapes[i].orderVertical = NULLORDER;
	      	if( tipe == Orientation.HORIZONTAL ) shapes[i].orderHorizontal = NULLORDER;
       }
       // bubbelsort
       int[][] sorted = bubbleSort(olist);
       //
       if( tipe == Orientation.HORIZONTAL ) {
         for(int i=0;i<sorted.length;i++) 
	     {
        	if( sorted[i][VALUE] == NULLORDER ) continue; 
        	int idx = sorted[i][INDEX];
        	if( idx == NULLORDER ) continue; // is an error shoudl be reported
        	if( shapes[idx] == null ) continue;
        	shapes[idx].orderHorizontal = sorted[i][ORDER];
	     }
         
       }
       else
   	   if( tipe == Orientation.VERTICAL ) {
         for(int i=0;i<sorted.length;i++) 
  	     {
        	if( sorted[i][VALUE] == NULLORDER ) continue;
            int idx = sorted[i][INDEX];
        	if( idx == NULLORDER ) continue;
          	if( shapes[idx] == null ) continue;
          	shapes[idx].orderVertical = sorted[i][ORDER];
  	     }
       }
   	   else {
   		   do_error("Unsupported orientation for sort on shape");
   	   }
	}

	//------------------------------------------------------------
	private boolean findAndRemoveOverlap(boolean fixit)
	//------------------------------------------------------------
	{		
		boolean isOK=true;
		for(int i=0;i<ar_bubl.length;i++)
	    {
		  if( ar_bubl[i] == null ) continue;
		  ArrayList<cmcShape> overlaplist = bubbleHasOverlappingShapes(ar_bubl[i] , !fixit);
		  if( overlaplist != null ) {
			  //
			  for(int k=0;k<overlaplist.size()/2;k++) {
				  DUMP(true,"Overlap [Bubble" + ar_bubl[i].bubbleIdx + "] " +  overlaplist.get(k*2).UID + " " + cornerToString(overlaplist.get(k*2).encoCorners) + " " +  overlaplist.get((k*2)+1).UID + " " + cornerToString(overlaplist.get((k*2)+1).encoCorners)) ;
			  }
			  //	
			  if( fixit ) removeOverlappingShapes(ar_bubl[i]);
			  else {
				  isOK=false;
				  do_error("Overlap on bubble [" + ar_bubl[i].bubbleIdx + "]"); 
			  }
		  }
		}
		return !isOK;
	}
	
	//------------------------------------------------------------
	private ArrayList<cmcShape> bubbleHasOverlappingShapes(cmcBubble bub,boolean testconflict)
	//------------------------------------------------------------
	{
		ArrayList<cmcShape> list = new ArrayList<cmcShape>();
		for(int i=0;i<bub.shapes.length;i++)
		{
			cmcShape one = bub.shapes[i];
			if ( one == null ) continue;
			for(int j=0;j<bub.shapes.length;j++)
			{
				cmcShape two = bub.shapes[j];
				if( (two == null) || (j==i) ) continue;
				boolean ib1 = doTheseTwoShapesOverlapEncompassingRectangle(one,two);
				boolean ib2 = doTheseTwoShapesOverlapViaLines(one , two);
				if( (ib1 != ib2) && (testconflict == true) ) {
					do_error("Overlap logic has conflicting result " + ib1 + " " + ib2 + " on " + one.UID + " " + two.UID + " " + i + " " + j);
				}
				if( ib1 == true ) {
					list.add( one );
					list.add( two );
				}
			}
		}
		if( list.size() == 0 ) list = null;
		return list;
	}

	//------------------------------------------------------------
	private boolean doTheseTwoShapesOverlapLegOne(cmcShape one , cmcShape two)
	//------------------------------------------------------------
	{
		for(int i=0;i<4;i++) // run through each corners of the 2nd shape
		{
			Point x = null;
			switch( i )
			{
			case 0 : { x = two.encoCorners[TOP_LEFT]; break; }
			case 1 : { x = two.encoCorners[TOP_RIGHT]; break; }
			case 2 : { x = two.encoCorners[BOTTOM_LEFT]; break; }
			case 3 : { x = two.encoCorners[BOTTOM_RIGHT]; break; }
			default : { do_error("overlap - system error"); break; }
			}
			//
			if( (x.x >= one.encoCorners[TOP_LEFT].x) && 
				(x.x <= one.encoCorners[TOP_RIGHT].x ) &&
				(x.y >= one.encoCorners[TOP_LEFT].y) &&
				(x.y <= one.encoCorners[BOTTOM_RIGHT].y)) return true;
		}
		return false;
	}

	//------------------------------------------------------------
	private boolean doTheseTwoShapesOverlapEncompassingRectangle(cmcShape one , cmcShape two)
	//------------------------------------------------------------
	{
		boolean ib = doTheseTwoShapesOverlapLegOne( one , two);
		if( ib ) return true;
		return doTheseTwoShapesOverlapLegOne( two , one );
	}

	//------------------------------------------------------------
	private void removeOverlappingShapes(cmcBubble bub)
	//------------------------------------------------------------
	{
		//
		bub.OverlappingShapesConsolidated=0;
		// calculate max numbers of fixes
		int aant=0;
		for(int i=0;i<bub.shapes.length;i++)
		{
			cmcShape one = bub.shapes[i];
			if( one != null ) aant++;
		}	
		for(int atmp=0;atmp<aant; atmp++)
		{
			ArrayList<cmcShape> overlaplist = bubbleHasOverlappingShapes( bub , false );
			if( overlaplist == null ) break;
			
			// overlap
			cmcShape one = overlaplist.get(0);
			cmcShape two = overlaplist.get(1);
			long lfirst = one.UID;
			long lsecond = two.UID;
			DUMP(true,"Fixing " +  cornerToString(one.encoCorners) + " " + cornerToString(two.encoCorners)) ;
			//
			int minx = ( one.encoCorners[TOP_LEFT].x < two.encoCorners[TOP_LEFT].x ) ? one.encoCorners[TOP_LEFT].x  : two.encoCorners[TOP_LEFT].x;
			int miny = ( one.encoCorners[TOP_LEFT].y < two.encoCorners[TOP_LEFT].y ) ? one.encoCorners[TOP_LEFT].y  : two.encoCorners[TOP_LEFT].y;
			int maxx = ( one.encoCorners[BOTTOM_RIGHT].x > two.encoCorners[BOTTOM_RIGHT].x ) ? one.encoCorners[BOTTOM_RIGHT].x  : two.encoCorners[BOTTOM_RIGHT].x;
			int maxy = ( one.encoCorners[BOTTOM_RIGHT].y > two.encoCorners[BOTTOM_RIGHT].y ) ? one.encoCorners[BOTTOM_RIGHT].y  : two.encoCorners[BOTTOM_RIGHT].y;
			//
			for(int i=0;i<bub.shapes.length;i++)
			{
				if( bub.shapes[i] == null ) continue;
				if( bub.shapes[i].UID == lfirst ) {
					bub.shapes[i].encoCorners[ TOP_LEFT ] = new Point( minx , miny);
					bub.shapes[i].encoCorners[ TOP_RIGHT ] = new Point( maxx , miny);
					bub.shapes[i].encoCorners[ BOTTOM_RIGHT ] = new Point( maxx , maxy);
					bub.shapes[i].encoCorners[ BOTTOM_LEFT ] = new Point( minx , maxy);
					DUMP( true , "Merged [" + lfirst + "] and [" + lsecond + "]");
					bub.shapes[i].consolidatedshape=true;
					bub.OverlappingShapesConsolidated++;
				}
				if( bub.shapes[i].UID == lsecond ) {
					bub.shapes[i] = null; // remove
					DUMP( true , "Removed shape [" + lsecond + "]");
				}
			}
		}
	}
	
	
	//------------------------------------------------------------
	//------------------------------------------------------------
	//------------------------------------------------------------
	private void runReport()
	
	{
		  for(int i=0;i<ar_bubl.length;i++)
		  {
			if( ar_bubl[i] == null ) continue;
			runBubbleReport (ar_bubl[i] );
		  }
	}

	private void runBubbleReport(cmcBubble bub)
	{
		  DUMP(false,"==BUBBLE================");
		  DUMP(false , "Bubble [Idx=" + bub.bubbleIdx + "] [UID=" + bub.bubbleIdx + "] [OverlappedFixed=" + bub.OverlappingShapesConsolidated + "]");
		  for(int i=0;i<bub.shapes.length;i++)
		  {
			  if( bub.shapes[i] == null ) continue;
			  runShapeReport( bub.shapes[i] );
		  }
		  DUMP(false,"==================");
	}
	
	private String ptString(Point p)
	{
		if( p == null ) return "null";
		return "[" + (int)p.getX() + "," + (int)p.getY() + "]";
	}
	
	private String cornerToString( Point[] corner)
	{
		return 
		" TL" + ptString(corner[TOP_LEFT]) + 
		" TR" + ptString(corner[TOP_RIGHT]) + 
		" BL" + ptString(corner[BOTTOM_LEFT]) + 
		" BR" + ptString(corner[BOTTOM_RIGHT]);
	}
	
	private void runShapeReport(cmcShape shp)
	{
		
		  DUMP(false, "__Shape [UID=" + shp.UID +"] [H=" + shp.orderHorizontal + "] [V=" + shp.orderVertical + "] [ConsolidatedShape=" + shp.consolidatedshape + "]");
		  DUMP(false, "____Encomp " + cornerToString( shp.encoCorners ) );
		  DUMP(false, "____Extrem " + cornerToString( shp.extrmCorners) );
		  for(int i=0;i<shp.lines.length;i++)
		  {
			  if( shp.lines[i] == null ) continue;
			  runLineReport( shp.lines[i] );
		  }
	}

	private String lineToString(cmcLine x)
	{
		return "____Line [" + x.UID + "] [" + x.orient + "] " + ptString(x.from) + " " + ptString(x.to) + "  [H=" + x.orderHorizontal + "] [V=" + x.orderVertical + "]";
	}
	private void runLineReport(cmcLine x)
	{
	   DUMP(false, lineToString(x));
	}
	
	//------------------------------------------------------------
	private void loadtestdata()
	//------------------------------------------------------------
	{
		long UID = 100L;
		ar_bubl = null;
		ar_shpe = null;
		//
		ar_bubl = new cmcBubble[1];
		ar_bubl[0] = new cmcBubble();
		ar_bubl[0].UID = 10L;
		ar_bubl[0].bubbleIdx = 100;
		ar_bubl[0].corners[TOP_LEFT] = new Point( 0 , 0 );
		ar_bubl[0].corners[TOP_RIGHT] = new Point( 100 , 0 );
		ar_bubl[0].corners[BOTTOM_RIGHT] = new Point( 100 , 100 );
		ar_bubl[0].corners[BOTTOM_LEFT] = new Point( 0 , 100 );
		//
		ar_bubl[0].shapes[0] = new cmcShape();
		ar_bubl[0].shapes[0].UID = UID++;
		ar_bubl[0].shapes[0].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[0].encoCorners[TOP_LEFT]     = new Point( 10 , 10 );
		ar_bubl[0].shapes[0].encoCorners[TOP_RIGHT]    = new Point( 30 , 10 );
		ar_bubl[0].shapes[0].encoCorners[BOTTOM_RIGHT]  = new Point( 30 , 20);
		ar_bubl[0].shapes[0].encoCorners[BOTTOM_LEFT] = new Point( 10 , 20 );
		//
		ar_bubl[0].shapes[1] = new cmcShape();
		ar_bubl[0].shapes[1].UID = UID++;
		ar_bubl[0].shapes[1].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[1].encoCorners[TOP_LEFT]     = new Point( 40 , 0 );
		ar_bubl[0].shapes[1].encoCorners[TOP_RIGHT]    = new Point( 50 , 0 );
		ar_bubl[0].shapes[1].encoCorners[BOTTOM_RIGHT]  = new Point( 50 , 20);
		ar_bubl[0].shapes[1].encoCorners[BOTTOM_LEFT] = new Point( 40 , 20 );
		//
		ar_bubl[0].shapes[2] = new cmcShape();
		ar_bubl[0].shapes[2].UID = UID++;
		ar_bubl[0].shapes[2].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[2].encoCorners[TOP_LEFT]     = new Point( 20 , 30 );
		ar_bubl[0].shapes[2].encoCorners[TOP_RIGHT]    = new Point( 40 , 30 );
		ar_bubl[0].shapes[2].encoCorners[BOTTOM_RIGHT]  = new Point( 40 , 60);
		ar_bubl[0].shapes[2].encoCorners[BOTTOM_LEFT] = new Point( 20 , 60 );
		//
		ar_bubl[0].shapes[3] = new cmcShape();
		ar_bubl[0].shapes[3].UID = UID++;
		ar_bubl[0].shapes[3].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[3].encoCorners[TOP_LEFT]     = new Point( 60 , 10 );
		ar_bubl[0].shapes[3].encoCorners[TOP_RIGHT]    = new Point( 70 , 10 );
		ar_bubl[0].shapes[3].encoCorners[BOTTOM_RIGHT]  = new Point( 70 , 40);
		ar_bubl[0].shapes[3].encoCorners[BOTTOM_LEFT] = new Point( 60 , 40 );
		//
		ar_bubl[0].shapes[4] = new cmcShape();
		ar_bubl[0].shapes[4].UID = UID++;
		ar_bubl[0].shapes[4].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[4].encoCorners[TOP_LEFT]     = new Point( 50 , 50 );
		ar_bubl[0].shapes[4].encoCorners[TOP_RIGHT]    = new Point( 80 , 50 );
		ar_bubl[0].shapes[4].encoCorners[BOTTOM_RIGHT]  = new Point( 80 , 70);
		ar_bubl[0].shapes[4].encoCorners[BOTTOM_LEFT] = new Point( 50 , 70 );
		//
		ar_bubl[0].shapes[5] = new cmcShape();
		ar_bubl[0].shapes[5].UID = UID++;
		ar_bubl[0].shapes[5].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[5].encoCorners[TOP_LEFT]     = new Point( 20 , 70 );
		ar_bubl[0].shapes[5].encoCorners[TOP_RIGHT]    = new Point( 40 , 70 );
		ar_bubl[0].shapes[5].encoCorners[BOTTOM_RIGHT]  = new Point( 40 , 90);
		ar_bubl[0].shapes[5].encoCorners[BOTTOM_LEFT] = new Point( 20 , 90 );
		//
		//
		ar_bubl[0].shapes[6] = new cmcShape();
		ar_bubl[0].shapes[6].UID = UID++;
		ar_bubl[0].shapes[6].bubbleIdx = ar_bubl[0].bubbleIdx;
		ar_bubl[0].shapes[6].encoCorners[TOP_LEFT]     = new Point( 10 , 70 );
		ar_bubl[0].shapes[6].encoCorners[TOP_RIGHT]    = new Point( 20 , 70 );
		ar_bubl[0].shapes[6].encoCorners[BOTTOM_RIGHT]  = new Point( 20 , 80);
		ar_bubl[0].shapes[6].encoCorners[BOTTOM_LEFT] = new Point( 10 , 80 );
		
		
		
		// enlarge
		for(int i=0;i<ar_bubl.length;i++)
		{
			if( ar_bubl[i] == null ) continue;
			for(int k=0;k<ar_bubl[i].shapes.length;k++){
				cmcShape s = ar_bubl[i].shapes[k];
				if ( s == null ) continue;
				for(int p=0;p<s.encoCorners.length;p++)
				{
					s.encoCorners[p].x = s.encoCorners[p].x * 10;
					s.encoCorners[p].y = s.encoCorners[p].y * 10;
				}
			}
		}
	}

	//------------------------------------------------------------
	//------------------------------------------------------------
	//------------------------------------------------------------


	//------------------------------------------------------------
	private void findBestFittingContourInAllBubbles()
	//------------------------------------------------------------
	{
		for(int i=0;i<ar_bubl.length;i++)
	    {
		  if( ar_bubl[i] == null ) continue;
		  findBestFittingContourPerBubble(ar_bubl[i]);
	    }
		
		do_error("ensure that in a binome there is at least one rectangle");
	}
	
	//------------------------------------------------------------
	private void findBestFittingContourPerBubble(cmcBubble bub)
	//------------------------------------------------------------
	{
		if( bub == null ) return;
		int nshapes=0;
		for(int i=0;i<bub.shapes.length;i++) if( bub.shapes[i] != null ) nshapes++;
		// max iterations is nshapes
		for(int iter=0;iter<=nshapes;iter++)
		{
			DUMP(true,"[Bubble=" + bub.UID + "] [Iteration=" + iter + "]" );
			// find 2 shapes to combine
			findNextShapesToCombine( bub.shapes );
		}
	}

	
	// scan from left to right; try the first one with any of the other; if no success try second etc
	//------------------------------------------------------------
	private void findNextShapesToCombine(cmcShape[] shapelist)
	//------------------------------------------------------------
	{
	    int leftidx=-1;		
		for(int i=0;i<shapelist.length;i++ )
		{
	      if( leftidx == -1 ) leftidx = findLeftMostShape( shapelist );
	                     else leftidx = findNextShapeHorizontal( shapelist , leftidx );
	      if( leftidx < 0 ) {
	    	  DUMP(true,"Could not find next shape");
	    	  break;
	      }
	      
	      // now iterate through the list from the left idx onwards and find horz/vert overlap
	      // continue until you find one - found return the combination
	      int rightidx = findNextShapeHorizontal( shapelist , leftidx );
	      if( rightidx < 0 ) break;
	      DUMP( false , " -> [leftIdx=" + leftidx + "] [x=" + shapelist[leftidx].extrmCorners[TOP_LEFT].x + "]" +
	    		      " [RightIdx=" + rightidx + "] [x=" + shapelist[rightidx].extrmCorners[TOP_LEFT].x + "]"  );
	 
	      // determine overlap between left and right
	      cmcShape leftShape = shapelist[leftidx];
	      cmcShape rightShape = shapelist[rightidx];
	      OverlapType olap = determineOverlapType( leftShape , rightShape );
          switch( olap )
          {
          case UNKNOWN  : break;
          case ISOLATED : continue;
          case TRUE_OVERLAP : { 
        	  do_error( "Impossible : Found a true overlap [left=" + leftShape.UID + "] [right=" + rightShape.UID + "]"); 
        	  continue; }
          case HORIZONTAL_OVERLAP : {
        	  do_error("Impossible : Cannot occur at this moment [left=" + leftShape.UID + "] [right=" + rightShape.UID + "]"); 
        	  continue; }
          case VERTICAL_OVERLAP : {
        	  do_error("Impossible : Cannot occur at this moment [left=" + leftShape.UID + "] [right=" + rightShape.UID + "]"); 
        	  continue; }
          default : {
        	  do_error("Impossible : Cannot occur at this moment [left=" + leftShape.UID + "] [right=" + rightShape.UID + "]"); 
        	  continue; }          
          }
          // either HORIZONT or VERTICAL or bad overlap
          olap = joinShapes( leftShape , rightShape );
          
		}
		// return nothing found to combine anymore
	}

	//------------------------------------------------------------
	private int findLeftMostShape(cmcShape[] shapelist)
	//------------------------------------------------------------
	{
		int idx = -1;
		for(int i=0;i<shapelist.length;i++ )
		{
			cmcShape shp = shapelist[i];
			if( shp == null ) continue;
			if( idx < 0 ) { idx = i; continue; }
			if( shp.orderHorizontal < shapelist[idx].orderHorizontal ) idx = i;
		}
		return idx;
	}

	//------------------------------------------------------------
	private int findNextShapeHorizontal(cmcShape[] shapelist , int startidx)
	//------------------------------------------------------------
	{
		if( (startidx <0) || (startidx > shapelist.length) ) return -1;
		if ( shapelist[startidx] == null ) return -1;
		//
		int currentOrderHorizontal = shapelist[startidx].orderHorizontal;
		// get the next idx where the horizontal order is the same, ie. same orderhorizontal
		int idx = -1;
		for(int i=0;i<shapelist.length;i++ )
		{
			cmcShape shp = shapelist[i];
			if( shp == null ) continue;
			// more to the left than current
			if( shp.orderHorizontal < currentOrderHorizontal ) continue;
			// more to the right than current
			if( shp.orderHorizontal > currentOrderHorizontal ) continue;
			// equal left distance (same orderhorizontal)
			if ( i <= startidx ) continue;
			idx = i;
			break;
		}
		// there is an equal - so return
		if( idx >= 0 ) return idx;
		// get the first idx which is is not on the same left distance
		// start by finding the next orderhorizontal
		int nextOrderHorizontal=-1;
		for(int i=0;i<shapelist.length;i++ )
		{
			cmcShape shp = shapelist[i];
			if( shp == null ) continue;
			if( shp.orderHorizontal <= currentOrderHorizontal ) continue; // equal or more to the left
			if( nextOrderHorizontal < 0 ) {
				nextOrderHorizontal = shp.orderHorizontal;
				continue;
			}
			if( shp.orderHorizontal < nextOrderHorizontal ) {
				nextOrderHorizontal = shp.orderHorizontal;
				continue;
			}
		}
		if( nextOrderHorizontal < 0 ) return -1;
		// now find the first index with the nextOrderhorizontal
		for(int i=0;i<shapelist.length;i++ )
		{
			cmcShape shp = shapelist[i];
			if( shp == null ) continue;
			if( shp.orderHorizontal == nextOrderHorizontal ) return i;
		}
		return -1;
	}

	//  uses the corners of the ecopassing and look whethet those are in the northeast, southeast,southwest,nothwest segments
	//------------------------------------------------------------
	private boolean areShapesNotOverlappedAtAll(cmcShape left , cmcShape right )
	//------------------------------------------------------------
	{
		//  bottom_right is outside (north-west) top_left
		if( (left.encoCorners[BOTTOM_RIGHT].x < right.encoCorners[TOP_LEFT].x) && 
			(left.encoCorners[BOTTOM_RIGHT].y < right.encoCorners[TOP_LEFT].y) ) return true;
		//  top_right is outside bottom_left (south west)
		if( (left.encoCorners[TOP_RIGHT].x < right.encoCorners[BOTTOM_LEFT].x) && 
			(left.encoCorners[TOP_RIGHT].y > right.encoCorners[BOTTOM_LEFT].y) ) return true;
		//  bottom_left is outside top_right (north east)
		if( (left.encoCorners[BOTTOM_LEFT].x > right.encoCorners[TOP_RIGHT].x) && 
			(left.encoCorners[BOTTOM_LEFT].y < right.encoCorners[TOP_RIGHT].y) ) return true;
	    //  top_left is outside bottom_right (south east)
		if( (left.encoCorners[TOP_LEFT].x > right.encoCorners[BOTTOM_RIGHT].x) && 
			(left.encoCorners[TOP_LEFT].y > right.encoCorners[BOTTOM_RIGHT].y) ) return true;
		//	
		return false;
	}
	
	//------------------------------------------------------------
	private OverlapType determineOverlapType( cmcShape left , cmcShape right)
	//------------------------------------------------------------
	{
		// prelim check no overlap at all 
		if( areShapesNotOverlappedAtAll(left,right) ) return OverlapType.ISOLATED;
		if( areShapesNotOverlappedAtAll(right,left) ) return OverlapType.ISOLATED;
		
		// true overlap
		boolean ib = doTheseTwoShapesOverlapViaLines(left , right);
        if( ib ) {  
            do_error( "Need to check for full encompassed shapes");
            return OverlapType.TRUE_OVERLAP;
        }
        
		// Horizontal or Vertical
        return OverlapType.UNKNOWN;
	}
	
	// the idea is to loop through eery line and see if it crosses one of the lines of the other shape
	// the flaw is : what is one of the shape is completely envelopped in the other 
	// solution : ensure that one of the shapes is a pure rectangle => then work via extremiteis
	//------------------------------------------------------------
	private boolean doTheseTwoShapesOverlapViaLines(cmcShape one , cmcShape two)
	//------------------------------------------------------------
	{
		for(int i=0;i<one.lines.length;i++)
		{
			cmcLine line1 = one.lines[i];
			if( line1 == null ) continue;
			for(int j=0;j<two.lines.length;j++)
			{
				cmcLine line2 = two.lines[j];
				if( line2 == null ) continue;
				Point p = crossPointLines( line1 , line2 );
				
				if( p == null ) {
					// to do check completely envelopped - currently ok, because we check overlap at the beginning
					continue;
				}
				
				// debug info
				//DUMP(false,"--STA- overlap shape[" + one.UID + " " + two.UID + "]");
				//runLineReport(line1);
				//runLineReport(line2);
				//DUMP(false,"Crossed [" + ptString(p) + "]" ); 
				//DUMP(false,"--STO-");
				
				DUMP(true,"Lines intersect/overlap shape [" + one.UID + " " + two.UID + "] " + ptString(p));

				return true;
			}
		}
		
		return false;
	}

	// Cross point of 2 lines which have a from and to end
	//------------------------------------------------------------
	private Point crossPointLines( cmcLine l1 , cmcLine l2)
	//------------------------------------------------------------
	{
		
		// both are horizontal 
		if( (l1.orient == Orientation.HORIZONTAL) && (l2.orient == Orientation.HORIZONTAL) ) {
			
		   // if the y coordinate is different no cross
		   if( l1.from.y != l2.from.y )  return null;
		   // l1 envelopped in l2
	       if( (l1.from.x >= l2.from.x) && (l1.to.x <= l2.to.x) ) return l1.from;
	       // l2 envelopped in l1
	       if( (l2.from.x >= l1.from.x) && (l2.to.x <= l1.to.x) ) return l2.from;
		   // which one is the first
		   if( l1.from.x < l2.from.x )  {  // first one has the smallest x
			   if( l1.to.x < l2.from.x ) return null;  // the end of first line must precede start of 2nd
			   // overlap
			   return l1.to;
		   }
		   if( l2.from.x < l1.from.x )  {  // second one has the smallest x
			   if( l2.to.x < l1.from.x ) return null;
			   // overlap
			   return l2.to;
		   }
		   do_error("CrossPointLines - HOR - This point should never be reached");
		   do_error("L1 "+ lineToString(l1));
		   do_error("L2 "+ lineToString(l2));
		   //
           return null;			   
		}
		
		// both are vertical
		else
		if( (l1.orient == Orientation.VERTICAL) && (l2.orient == Orientation.VERTICAL) ) {
			// if the x is different no cross
			if( l1.from.x != l2.from.x )  return null;
			// first envelopped in 2nd
			if( (l1.from.y >= l2.from.y) && (l1.to.y <= l2.to.y) ) return l1.from;
			// second envelopped in first
			if( (l2.from.y >= l1.from.y) && (l2.to.y <= l1.to.y)) return l2.from;
			// which one is first
			if( l1.from.y < l2.from.y ) {
				if( l1.to.y < l2.from.y ) return null;
				return l1.to;
			}
			if( l2.from.y < l1.from.y ) {
				if( l2.to.y < l1.from.y ) return null;
				return l2.to;
			}
		   do_error("CrossPointLines - VER - This point should never be reached");
		   do_error("L1 "+ lineToString(l1));
		   do_error("L2 "+ lineToString(l2));
		   //
           return null;			   
		}
		
		// cross
		else 
		if ( l1.orient != l2.orient ) {  
			Point xc = new Point( 0,0);
			// crosspoint =  X of the Vertical line and Y of the horizontal 
			if( (l1.orient == Orientation.HORIZONTAL) && (l2.orient == Orientation.VERTICAL) ) {
				xc.x = l2.from.x;
				xc.y = l1.from.y;
			}
			else
			if( (l1.orient == Orientation.VERTICAL) && (l2.orient == Orientation.HORIZONTAL) ) {
				xc.x = l1.from.x;
				xc.y = l2.from.y;
			}
			else {
				do_error("CrossPointLine : impossible (first) [l1=" + l1.orient + "] [l2=" + l2.orient + "]");
				return null;
			}
			// now verify whether the cross point is included on both lines
			if( (pointIsOnLine( xc , l1)) && (pointIsOnLine( xc , l2)) ) return xc;
			//
			return null;
		}
		
		// ano
		do_error("CrossPointLine : impossible (2nd) [l1=" + l1.orient + "] [l2=" + l2.orient + "]");
	    
		return null;
	}
	
	//------------------------------------------------------------
	private boolean pointIsOnLine( Point p , cmcLine l)
	//------------------------------------------------------------
	{
		int outTipe=0;
		if( l.orient == Orientation.HORIZONTAL ) {
			if( l.from.y != p.y ) outTipe=1;
			else {
				if( (p.x < l.from.x) || ( p.x > l.to.x) ) outTipe=2;
			}
		}
		else
		if( l.orient == Orientation.VERTICAL ) {
			if( l.from.x != p.x ) outTipe=3;
			else {
				if( (p.y < l.from.y) || ( p.y > l.to.y) ) outTipe=4;
			}
		}
		else {
			do_error("Unsupported orientation");
		}
		
		if( outTipe == 0 ) {
			//DUMP(false,"Cross P=" + ptString(p) + " [" + l.orient + "] From[" + ptString(l.from) + "] To[" + ptString(l.to)  + "]");
			return true; 
		}
		return false;
	}
	
	
	private OverlapType joinShapes( cmcShape left , cmcShape right)
	{
		// 
		
		return OverlapType.UNKNOWN;
	}
	
}
