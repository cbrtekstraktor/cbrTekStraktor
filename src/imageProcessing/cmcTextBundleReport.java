package imageProcessing;

import logger.logLiason;
import generalImagePurpose.gpRotateImageFile;
import generalpurpose.gpAppendStream;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;

 
public class cmcTextBundleReport {
	
	cmcProcSettings xMSet=null;
	cmcProcParameters cParam=null;
	comicPage cPage=null;
	logLiason logger=null;
	
	gpAppendStream app = null;

	int Links=10;
	int TotBreedte=1100;
	int CurY = 0;
	int HorzMarge=4;
	int VertMarge=HorzMarge;
	int TITELHOOGTE=35;
	int GENERALPANEHOOGTE=320;
	int LinksBlokHoogte=-1;
	int plaatHoogte;
	int plaatBreedte;
	boolean plaatLiggend=false;
	
	
	cmcConnectedTextComponentBundel[] tcc_ar=null;
    gpRotateImageFile xRot=null;

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
	public cmcTextBundleReport( cmcProcSettings xi , cmcConnectedTextComponentBundel[] ta , comicPage cp , logLiason ilog , String CodePage)
	//-----------------------------------------------------------------------
	{
		xMSet=xi;
		tcc_ar=ta;
		cPage=cp;
		logger=ilog;
		// vermijder HTML en maak nieuw
		String FNaam = xMSet.getReportHTMLFileName();
		if( xMSet.xU.IsBestand(FNaam ) == true) {
			xMSet.xU.VerwijderBestand(FNaam);
			if(xMSet.xU.IsBestand(FNaam ) == true) {
				do_error("Cannot remove file [" + FNaam + "]");
				return;
			}
		}
		app = new gpAppendStream( FNaam , CodePage);
		maakReport();
		app.CloseAppendFile();
	}
	
	//-----------------------------------------------------------------------
	private void prnt (String sIn)
	//-----------------------------------------------------------------------
	{
		app.AppendIt(sIn);
	}
	
	//-----------------------------------------------------------------------
	private void maakHeader()
	//-----------------------------------------------------------------------
	{
		  
	      prnt("<head>");
	      prnt("<link rel=\"stylesheet\" href=\".\\cbrTekStraktorCSS.txt\">");
	      prnt("</head>");
	}

	//-----------------------------------------------------------------------
	private void maakTitel()
	//-----------------------------------------------------------------------
	{
		int hoogte = TITELHOOGTE;
		prnt("<!-- Title zone -->");
		prnt("<div style=\"background-color:black;color:white;position:absolute;top:0px;left:"+Links+"px;width:" + TotBreedte + "px;text-align:center;height:" + hoogte + "px;font-size:22px;font-family:helvetica;\">");
		prnt( xMSet.xU.GetFileName(xMSet.getOrigImageLongFileName()) );
		prnt("</div>");
		//
		CurY=hoogte;
	}
		
	//-----------------------------------------------------------------------
	private void maakOverzicht()
	//-----------------------------------------------------------------------
	{
		int kaderhoogte=GENERALPANEHOOGTE;
		int kaderbreedte = (TotBreedte - VertMarge) / 2;
		int y = Links + VertMarge + kaderbreedte;
		CurY = TITELHOOGTE + HorzMarge;
		//
		prnt("<!-- General info -->");
		prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + CurY + "px;left:" + y + "px;width:" + kaderbreedte + "px;border:solid;border-width:1px;height:" + kaderhoogte + "px;font-size:14px;font-family:helvetica;\">");
		
		// labels
		int linkerbreedte = 180;
		prnt("<div style=\"background-color:white;color:black;position:absolute;text-align:right;top:10px;left:0px;width:" + linkerbreedte + "px;font-weight:bold;\">");
		for(int i=0;i<12;i++)
		{
			String sLijn = "";
			switch( i )
			{
			case 0 : { sLijn = "Series"; break; }
			case 1 : { sLijn = "Album"; break; }
			case 2 : { sLijn = "Author(s)"; break; }
			case 3 : { sLijn = "Page"; break; }
			case 4 : { sLijn = "Filename"; break; }
			case 5 : { sLijn = "Location"; break; }
			case 6 : { sLijn = "FileSize"; break; }
			case 7 : { sLijn = "Date"; break; }
			case 8 : { sLijn = "Uncropped dimensions"; break; }
			case 9 : { sLijn = "Payload dimensions"; break; }
			default : { sLijn = ""; continue; }
			}
			prnt("<div>" + sLijn + "</div>");
		}
		prnt("</div>");
		
		// inhoud
		int rechterbreedte = kaderbreedte - linkerbreedte - VertMarge - 1;
		prnt("<div style=\"background-color:white;color:black;position:absolute;text-align:left;top:10px;left:" + (linkerbreedte+VertMarge) + "px;width:" + rechterbreedte + "px;font-weight:normal;\">");
		// inhoud
		for(int i=0;i<10;i++)
		{
			String sLijn = "";
			switch( i )
			{
			case 0 : { sLijn = xMSet.xU.prntStandardDateTime(System.currentTimeMillis()); break; }
			case 1 : { sLijn = "Unknown"; break; }
			case 2 : { sLijn = "Unknown"; break; }
			case 3 : { sLijn = "Unknown"; break; }
			case 4 : { sLijn = xMSet.xU.GetFileName(xMSet.getOrigImageLongFileName()); break; }
			case 5 : { sLijn = xMSet.xU.GetParent(xMSet.getOrigImageLongFileName()); break; }
			case 6 : { sLijn = String.format("%,d",xMSet.xU.getFileSize(xMSet.getOrigImageLongFileName())); break; }
			case 7 : { sLijn = xMSet.xU.prntStandardDateTime(xMSet.xU.getModificationTime(xMSet.getOrigImageLongFileName())); break; }
			case 8 : { sLijn = ""+cPage.getUncroppedWidth()+" x "+cPage.getUncroppedWidth(); break; }
			case 9 : { sLijn = ""+cPage.getPayloadWidth()+" x "+cPage.getPayloadHeigth(); break; }
			default : { sLijn = ""; continue; }
			}
			prnt("<div>" + sLijn + "</div>");
		}
		prnt("</div>");

		//
		prnt("</div>");
		
		CurY += kaderhoogte;
	}
	
	//-----------------------------------------------------------------------
	private void maakHistogrammen()
	//-----------------------------------------------------------------------
	{
		
		// roteer de histogrammen
		xRot = new gpRotateImageFile( xMSet.xU , logger);
		xRot.rotate90DegreesClockWize( xMSet.getHistoScreenShotDumpNameUncropped() , xMSet.getReportHistoColorName() );
		xRot.rotate90DegreesClockWize( xMSet.getHistoScreenShotDumpNameGrayScale() , xMSet.getReportHistoGrayName() );
		int imgHoogte = xRot.getBreedte();  // de hoogte is de brredte voor het roteren.
		int imgBreedte = xRot.getHoogte(); 
		//
		int kaderhoogte= imgHoogte + (2*HorzMarge);
		int kaderbreedte = (TotBreedte - VertMarge) / 2;
		int y = Links + VertMarge + kaderbreedte;
		kaderbreedte =  imgBreedte + (2*VertMarge);
		CurY += HorzMarge;
		//
		int Yeen= CurY;
		int xx = (kaderbreedte - imgBreedte) / 2;
        int yy = (kaderhoogte - imgHoogte) / 2;
		prnt("<!-- Color Histogram -->");
		prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + CurY + "px;left:" + y + "px;width:" + kaderbreedte + "px;text-align:center;border:solid;border-width:1px;height:" + kaderhoogte + "px;\">");
		prnt("<div style=\"position:absolute;top:" + yy + "px;left:" + xx + "px;\">");
		prnt("<img src=\"file:///" + xMSet.getReportHistoColorName() + "\" alt=\"0\" height=\"" + imgHoogte + "\">");	
        prnt("</div></div>");
		//
        //
        CurY += (HorzMarge*3) + imgHoogte;
		prnt("<!-- BW Histogram -->");
		prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + CurY + "px;left:" + y + "px;width:" + kaderbreedte + "px;text-align:center;border:solid;border-width:1px;height:" + kaderhoogte + "px;\">");
		prnt("<div style=\"position:absolute;top:" + yy + "px;left:" + xx + "px;\">");
		prnt("<img src=\"file:///" + xMSet.getReportHistoGrayName() + "\" alt=\"0\" height=\"" + imgHoogte + "\">");	
        prnt("</div></div>");
		//
        //
        CurY += (HorzMarge*3) + imgHoogte;
        LinksBlokHoogte = CurY - TITELHOOGTE - (2*HorzMarge);
        //
        
        //  boxdiagram
        // kopieer de box
        int boxbreedte=0;
        int boxhoogte=0;
     	try {
     		  xMSet.xU.copyFile(xMSet.getBoxDiagramName(), xMSet.getReportBoxDiagramName());
     		  // doe een dummy roteer om de hoogte en breedte te weten
     		  xRot.rotate90DegreesClockWize( xMSet.getReportBoxDiagramName() , xMSet.getBoxDiagramName());
     		  boxhoogte  = xRot.getHoogte();
     		  boxbreedte = xRot.getBreedte();
     		}
     		catch( Exception e) {
     			System.err.println("Copy [" + xMSet.getBoxDiagramName() + "] to [" + xMSet.getReportBoxDiagramName() + "]");
     	}
        y += kaderbreedte + VertMarge;
        kaderbreedte = TotBreedte - y + (2*VertMarge);
        int hh = (5*HorzMarge) + (2*imgHoogte);
        yy = (hh - boxhoogte) / 2;	
	    xx = (kaderbreedte - boxbreedte) /2;
	    //System.err.println("B=" + boxbreedte + " H=" + boxhoogte + " kaderb=" + kaderbreedte + " kaderhoogte=" + hh);
		//
        prnt("<!-- Box diagram -->");
		prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + Yeen + "px;left:" + y + "px;width:" + kaderbreedte + "px;text-align:center;border:solid;border-width:1px;height:" + hh + "px;\">");
		prnt("<div style=\"position:absolute;top:" + yy + "px;left:" + xx + "px;\">");
		prnt("<img src=\"file:///" + xMSet.getReportBoxDiagramName() + "\" alt=\"0\">");
		prnt("</div></div>");
	     
	}
	
	//-----------------------------------------------------------------------
	private void maakPlaatZone()
	//-----------------------------------------------------------------------
	{
		int kaderhoogte=LinksBlokHoogte;
		int kaderbreedte = (TotBreedte - VertMarge) / 2;
		CurY = TITELHOOGTE + HorzMarge;
		//
		prnt("<!-- Original Image Analyzed -->");
		prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + CurY + "px;left:" + Links + "px;width:" + kaderbreedte + "px;text-align:center;border:solid;border-width:1px;height:" + kaderhoogte + "px;\">");
		// breedte en hoogte bepalen (resizen)
		bepaalBreedteEnHoogte( cPage.getUncroppedWidth() , cPage.getUncroppedHeigth() ,	kaderbreedte - (VertMarge * 2) , kaderhoogte - (HorzMarge * 2) );
		//
		int xx = (kaderbreedte - plaatBreedte) / 2;
		int yy = (kaderhoogte - plaatHoogte) / 2;
		prnt("<div style=\"position:absolute;top:" + yy + "px;left:" + xx + "px;\">");
		if( plaatLiggend ) {
			prnt("<img src=\"file:///" + xMSet.getOrigImageLongFileName() + "\" alt=\"0\" width=\"" + plaatBreedte + "\">");	
	    }
	    else {
			prnt("<img src=\"file:///" + xMSet.getOrigImageLongFileName() + "\" alt=\"0\" heigth=\"" + plaatHoogte + "\">");	
	    }
		prnt("</div></div>");
		//
		CurY += kaderhoogte;
	}

	//-----------------------------------------------------------------------
	private void bepaalBreedteEnHoogte(int ib , int ih , int maxb , int maxh)
	//-----------------------------------------------------------------------
	{
		double ratio = (double)ib / (double)ih;
		int imgbreedte;
		int imghoogte;
		// Liggend
		if( ratio > 0 ) {  // liggend 
			plaatLiggend=true;
			if( ib > maxb ) imgbreedte = maxb;
			  	       else imgbreedte = ib;
			// kijken of nu de hoogte niet overschrden is
			double dho = (double)imgbreedte / ratio;
			imghoogte = (int)dho;
			if( imghoogte > maxh ) {
				double dbr = (double)maxh * ratio;
				imgbreedte = (int)dbr;
				imghoogte = maxh;
			}
			plaatHoogte  = imghoogte;
			plaatBreedte = imgbreedte;
		}
		else { // staand
			plaatLiggend=false;
			if( ih > maxh ) imghoogte = maxh;
					   else imghoogte = ih;
			// niet te breed
			double dbr = (double)imghoogte * ratio;
			imgbreedte = (int)dbr;
			if( imgbreedte > maxb ) {
				double dho = (double)maxb / ratio;
			    imghoogte = (int)dho;
			    imgbreedte = maxb;
			}
			plaatHoogte  = imghoogte;
			plaatBreedte = imgbreedte;
		}
		//System.err.println("mwxb=" + maxb + "maxh=" + maxh + "b=" + ib + " h=" + ih + " imgb=" + plaatBreedte + " imgh=" + plaatHoogte + " liggend=" + plaatLiggend);
	}
	
	//-----------------------------------------------------------------------
	private void maakTabel()
	//-----------------------------------------------------------------------
	{
		  //
		  CurY += (VertMarge * 3);
		  prnt("<!-- Pargraph information -->");
		  prnt("<div style=\"background-color:white;color:slategray;position:absolute;top:" + CurY + "px;left:" + Links + "px;\">");
		
	      // header van de tabel
		  int idBreedte  =  10;
		  int karBreedte = 200;
		  int imgBreedte = 400;
		  int txtBreedte = TotBreedte - idBreedte - karBreedte - imgBreedte - Links; 
	      prnt("<table id=\"textBundle\" >");
	      prnt("<thead>");
	      prnt("<tr>");
	      prnt("<th style=\"width:" + idBreedte  + "px;\">Id</th>");
	      prnt("<th style=\"width:" + karBreedte + "px;\">Characteristics</th>");
	      prnt("<th style=\"width:" + imgBreedte + "px;\">Extracted image</th>");
	      prnt("<th style=\"width:" + txtBreedte + "px;\">Extracted text</th>");
	      prnt("</tr>");
	      prnt("</thead>");
	      //
	      for(int i=0;i<tcc_ar.length;i++)
	      {
	    	  prnt("<tr>");
	    	  if( tcc_ar[i].isLetterParagraph == false ) {
	    		prnt("<td style=\"background-color:DarkOrange;\">" + String.format("%03d", i) + "</td>");
	    	  }
	    	  else {
	             prnt("<td>" + String.format("%03d", i) + "</td>");
	    	  }
	    	  //
	          // karakteristiek
	          prnt("<td>");
	         
	          for(int k=0;k<9;k++)
	          {
	          prnt("<div>");
	          //
	          prnt("<div style=\"display:inline;width:80px;float:left;text-align:right;font-weight:bold;\">");
	          switch(k)
        	  {
        	  case 0 : { prnt("#Elements"); break; }
        	  case 1 : { prnt("Width"); break; }
        	  case 2 : { prnt("Height"); break; }
        	  case 3 : { prnt("Density"); break; }
        	  case 4 : { prnt("Var"); break; }
        	  case 5:  { prnt("StdDev"); break; }
        	  case 6:  { prnt("HVar/VVar"); break; }
        	  //case 7:  { prnt("VertVar"); break; }
        	  case 7:  { prnt("#Chars"); break; }
        	  default : {prnt("-"); break; }
        	  }
	          prnt("</div>");
	          //
	          prnt("<div style=\"display:inline;width:110px;float:right;\">");
	          switch(k)
        	  {
        	  case 0 : { prnt(""+tcc_ar[i].counter); break; }
        	  case 1 : { prnt(""+(tcc_ar[i].MaxX - tcc_ar[i].MinX +1)); break; }
        	  case 2 : { prnt(""+(tcc_ar[i].MaxY - tcc_ar[i].MinY +1)); break; }
        	  case 3 : { prnt(prntDbl(tcc_ar[i].density)); break; }
        	  case 4 : { prnt(prntDbl(tcc_ar[i].vari)); break; }
        	  case 5 : { prnt(prntDbl(tcc_ar[i].stdev)); break; }
        	  case 6 : {  double dd = -1;
        	              if( tcc_ar[i].verticalVariance > 0.01 ) dd =  tcc_ar[i].horizontalVariance / tcc_ar[i].verticalVariance;
        		          prnt(prntDbl(dd)); break; }
        	  //case 7 : { prnt(prntDbl(tcc_ar[i].verticalVariance)); break; }
        	  case 7 : { prnt(""+tcc_ar[i].letterCount); break; }
        	  default : {
        		  prnt("<select name=\"txt-" + i + "\">");
        		  if( tcc_ar[i].isLetterParagraph ) {
    	           prnt("<option value=\"0\" selected>Text</option>");
    	           prnt("<option value=\"1\">Graph</option>");
        		  }
        		  else {
        		   prnt("<option value=\"0\">Text</option>");
       	           prnt("<option value=\"1\" selected>Graph</option>");
           		  }
    	          prnt("</select>");  
        	      }
        	  }
	          prnt("</div>");
	          //
	          prnt("</div>");
	          }
	          //
	          prnt("</td>");
	          
	          
	          // IMAGE
	          // cap breedte
	          int bb = tcc_ar[i].MaxX - tcc_ar[i].MinX + 1;
	          if( bb > (imgBreedte - (2*VertMarge)) ) bb = imgBreedte - (2*VertMarge);
	          prnt("<td style=\"text-align:center;\">");
	          prnt("<img src=\"file:///" + xMSet.getTextOutputJPGName(i) + "\" alt=\"" + i + "\" width=\"" + bb + "\">");
	          prnt("</td>");
	          
	          //
	          prnt("<td>" + i + "</td>");
	          prnt("</tr>");
	            
	      }
	      //
	      prnt("</table></div>");
	}
	
	
	//-----------------------------------------------------------------------
	private void maakBody()
	//-----------------------------------------------------------------------
	{
		  prnt("<body>");
	      //
          maakTitel();
          maakOverzicht();
		  maakHistogrammen();
	      maakPlaatZone();
	      maakTabel();
	      //
	      prnt("</body>");
	}
	
	//-----------------------------------------------------------------------
	private void maakReport()
	//-----------------------------------------------------------------------
	{
		  prnt("<!DOCTYPE html>");
	      prnt("<!-- Generated by : "  + xMSet.getApplicDesc() +" -->");
	      prnt("<!-- Generated at : "  +  xMSet.xU.prntStandardDateTime(System.currentTimeMillis()) +" -->");
	      prnt("<html>");
	      maakHeader();
	      maakBody();
	      prnt("</html>");
    }
	
	private String prntDbl(double dd)
	{
		return String.format("%-10.5f",dd);
	}
	
}
