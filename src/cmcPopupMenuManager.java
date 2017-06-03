import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import logger.logLiason;


public class cmcPopupMenuManager {

	cmcMenuShared memShared = null;
	logLiason logger=null;
	
	private JPopupMenu popup=null;
	JMenuItem arMenuItem[] = new JMenuItem[120];
	JMenu filterMenu =null;
		
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
	cmcPopupMenuManager(cmcMenuShared is , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		memShared = is;
		logger = ilog;
	}
	
	//---------------------------------------------------------------------------------
	public boolean makeMenus(JFrame frame , Font xfont)
	//---------------------------------------------------------------------------------
	{
		    popup = new JPopupMenu("");
		    //
		    for(int i=0;i<arMenuItem.length;i++) arMenuItem[i]=null;
		    //
		    boolean inFilter=false;
		    for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values().length;i++)
			{
		     	String sText = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values()[i].toString();
				
		     	if( sText.startsWith("DIV") ) {
		     		JSeparator sepa = new JSeparator();
		     		sepa.setName(sText);
					popup.add(sepa);
					continue;
				}
		     	if( sText.startsWith("START") ) {
		     		if( sText.startsWith("START_EDIT") == false ) continue;
				}
		     	if( sText.compareToIgnoreCase("IMAGE_FILTER_STOP") == 0 ) {
		     		inFilter=false;
		     		continue;
				}
		     	//
		     	arMenuItem[i] = new JMenuItem( memShared.makeLabel(sText));
				arMenuItem[i].setFont(xfont);
				arMenuItem[i].setName(sText);
				arMenuItem[i].addActionListener(new ActionListener() {
			            @Override
			            public void actionPerformed(ActionEvent actionEvent) {
			            	performMenuItemAction( actionEvent.getSource() );
			            }
			    });
				//
				//do_error( sText );
			    //  sub menu acivate
		     	if( sText.compareToIgnoreCase("IMAGE_FILTER") == 0 ) {
		     		inFilter=true;
		     		filterMenu = new JMenu("Image Filters");
		    		filterMenu.setFont(xfont);
		     		popup.add(filterMenu);
		     		continue;
				}
				if( inFilter ) {
					if( filterMenu != null ) filterMenu.add(arMenuItem[i]);
				}
				else {
				  popup.add(arMenuItem[i]);
				}
			}
		    //
		    frame.getContentPane().add(popup);
		    //  
		    return true;
	}
	

	//---------------------------------------------------------------------------------
	public void popItUp(Component c , String appStat , int x , int y)
	//---------------------------------------------------------------------------------
	{
		popup.show(c, x, y);
		//do_error("Status[" + appStat + "] Lok=(" + x + "," + y + ")" );
	}
	
	//---------------------------------------------------------------------------------
	private void performMenuItemAction(Object iO)
	//---------------------------------------------------------------------------------
	{
		    cbrTekStraktorModel.cmcProcEnums.POP_ITEMS pdx = null;
			try {
			  if( iO == null ) return;
			  JMenuItem mi = (JMenuItem)iO;
			  int idx=-1;
			  for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values().length;i++)
			  {
				if( arMenuItem[i] == null ) continue;
				if( mi == arMenuItem[i] ) { idx=i; break; }
			  }
			  if( idx < 0 ) return;
			  pdx = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values()[idx];
			}
			catch( Exception e ) {
				return;
			}
			if( pdx == null ) return;
			switch( pdx )
			{
			case IMAGE_REFRESH : { memShared.performCallback("doClickRefreshButton"); break;}
			case LOAD_IMAGE    : { memShared.performCallback("doClickImageButton"); break;}
			case EXTRACT_TEXT  : { memShared.performCallback("doClickExtractButton"); break;}
			case START_EDIT    : { memShared.performCallback("doClickEditButton"); break;}
			case STOP_EDIT     : { memShared.performCallback("doClickEditButton"); break;}
			case OCR           : { memShared.performCallback("doClickOCRButton"); break;}
			case TRANSLATE     : { memShared.performCallback("doClickTranslateButton"); break;}
			case REPORT        : { memShared.performCallback("doClickReportButton"); break;}
			case REINJECT      : { memShared.performCallback("doClickReInjectButton"); break;}
			case IMAGE_INFO    : { memShared.performCallback("doClickInfoButton"); break;}
			case EXTRACT_INFO  : { memShared.performCallback("doClickInfoButton"); break;}
			case EDIT_INFO     : { memShared.performCallback("doClickInfoButton"); break;}
			case EDIT_OPTIONS  : { memShared.performCallback("doClickOptionsButton"); break; }
			case QUICK_EDIT    : { memShared.performCallback("doQuickEdit"); break; }
			case DETAILED_EDIT : { memShared.performCallback("doDetailedEdit"); break; }
			//
			case SET_TO_TEXT     : { memShared.performCallback("doEditPopUpToText"); break; }
			case SET_TO_NON_TEXT : { memShared.performCallback("doEditPopUpToNoText"); break; }
			case DELETE          : { memShared.performCallback("doEditPopUpToDelete"); break; }
		    // filters
			case INVERT : 
			case ORIGINAL : 
			case GRAYSCALE : 
			case MONOCHROME_OTSU : 
			case BLUEPRINT : 
			case MONOCHROME_SAUVOLA :
			case BLEACHED :
			case MAINFRAME :
			case MONOCHROME_NIBLAK :
			case CONVOLUTION_SHARPEN :
			case CONVOLUTION_GLOW :
			case CONVOLUTION_GAUSSIAN :
			case CONVOLUTION_EDGE :
			case SOBEL :
			case SOBEL_ON_GRAYSCALE :
			case GRADIENT_WIDE :
			case GRADIENT_NARROW :
			case HISTOGRAM_EQUALISATION : { memShared.performCallback("doClickImageFilterButton",""+pdx); break; }
			//
			case STATISTICS      : { memShared.performCallback("doClickStatisticsButton"); break;}
			case MONITOR         : { memShared.performCallback("doClickMonitor"); break;}
			case ARCHIVE_BROWSER : { memShared.performCallback("doClickArchiveBrowser"); break;}
			case ARCHIVE_VIEWER  : { memShared.performCallback("doClickArchiveViewer"); break;}
			case STOP            : { memShared.performCallback("requestInterrupt"); break;}
		    //
		    default : { memShared.performCallback("popMessage" , "Currently no code foreseen for [" + pdx + "]"); break; } 
			}
	}
	
	//---------------------------------------------------------------------------------
	public void setMenuItemVisible(cbrTekStraktorModel.cmcProcEnums.POP_ITEMS mi , boolean stat)
	//---------------------------------------------------------------------------------
	{
		  int idx=-1;
		  for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values().length;i++)
		  {
			if( mi == cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.values()[i] ) {
				idx=i;
				break;
			}
		  }
		  if( idx < 0 ) return;
          arMenuItem[idx].setVisible( stat );
	}
	
	//---------------------------------------------------------------------------------
	public void setMenuAppState(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE as)
	//---------------------------------------------------------------------------------
	{
		filterMenu.setVisible(false);
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
			}
			// enable the following
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.LOAD_IMAGE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_TEXT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.START_EDIT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP_EDIT , false );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.TRANSLATE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.OCR , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REPORT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REINJECT , true );
			//
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.IMAGE_INFO , true );
		    //
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STATISTICS , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_BROWSER , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_VIEWER , true );
			//
			removeDoubleSeparators();
			return;
		}
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
			}
			// enable the following
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.LOAD_IMAGE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_TEXT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.START_EDIT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP_EDIT , false );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.TRANSLATE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.OCR , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REPORT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REINJECT , true );
			//
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.IMAGE_INFO , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.IMAGE_REFRESH , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.IMAGE_SAVE , true );
			//
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ORIGINAL, true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.BLUEPRINT, true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.MAINFRAME, true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.MONOCHROME_NIBLAK, true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.MONOCHROME_SAUVOLA , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.MONOCHROME_OTSU , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.BLEACHED , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.HISTOGRAM_EQUALISATION , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.CONVOLUTION_GLOW , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.CONVOLUTION_GAUSSIAN , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.CONVOLUTION_SHARPEN , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.CONVOLUTION_EDGE , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.SOBEL , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.SOBEL_ON_GRAYSCALE , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.GRADIENT_WIDE , true );
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.GRADIENT_NARROW , true );
		    //
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STATISTICS , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_BROWSER , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_VIEWER , true );
		    //	
			filterMenu.setVisible(true);
            //
			removeDoubleSeparators();
			return;
		}
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTED) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
			}
			// enable the following
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.LOAD_IMAGE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_TEXT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.START_EDIT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP_EDIT , false );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.TRANSLATE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.OCR , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REPORT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REINJECT , true );
			//
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_INFO , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_REFRESH , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_SAVE , true );
			//
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STATISTICS , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_BROWSER , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.ARCHIVE_VIEWER , true );
            //
			removeDoubleSeparators();
			return;
		}
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDIT) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
			}
			// enable the following
			//setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.START_EDIT, false );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP_EDIT , true );
			//
		    setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EDIT_INFO , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EDIT_REFRESH , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EDIT_SAVE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EDIT_OPTIONS , true );
			//
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.DELETE , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.DETAILED_EDIT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.QUICK_EDIT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.SET_TO_NON_TEXT , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.SET_TO_TEXT , true );
			//
			removeDoubleSeparators();
			return;
		}
		// in Flux 
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTING) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGELOADING) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.OCR_BUSY) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
			}
			// enable the following
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.MONITOR , true );
			setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP , true );
            //
			removeDoubleSeparators();
			return;
		}

		// catch all
		// disable all 
		for(int i=0;i<arMenuItem.length;i++)
		{
		 if( arMenuItem[i] != null ) arMenuItem[i].setVisible(false);
		}
		removeDoubleSeparators();
	}
	
	//---------------------------------------------------------------------------------
	private void removeDoubleSeparators()
	//---------------------------------------------------------------------------------
	{
		if( popup == null )  return;
		int prevTipe  = -1;     
		int isepa=10;
		int iitem=20;
		Component[] c = popup.getComponents();
	    for (int i=0; i<c.length; i++)
        {
            if (c[i] instanceof JSeparator) {
            	JSeparator x = (JSeparator)c[i];
            	// if previous is separator then hide
            	if( prevTipe == isepa ) x.setVisible(false);
            	                   else x.setVisible(true);
            	prevTipe = isepa;
            	continue;
            }
            else 
            if (c[i] instanceof JMenuItem) {
              	JMenuItem x = (JMenuItem)c[i];
              	// if not visible then do not up update previous
              	if( x.isVisible() == false ) continue;
               	prevTipe = iitem;
               	continue;
            }
            prevTipe=-1; 	
        }
	}
}
