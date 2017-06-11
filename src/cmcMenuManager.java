import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



public class cmcMenuManager {

	cmcMenuShared memShared = null;
	JMenuItem arMenuItem[] = new JMenuItem[100];

	//---------------------------------------------------------------------------------
	public cmcMenuManager( cmcMenuShared is)
	//---------------------------------------------------------------------------------
	{
	   	memShared = is;
	}

	//---------------------------------------------------------------------------------
	public boolean makeMenus(JMenuBar menuBar , Font xfont , int Breedte , String[] ar)
	//---------------------------------------------------------------------------------
	{
		JMenu fileMenu = new JMenu("File");
		fileMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		fileMenu.setFont(xfont);
		fileMenu.setOpaque(true);
		//
		JMenu importMenu = new JMenu("Import");
		importMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		importMenu.setFont(xfont);
		importMenu.setOpaque(true);
		//
		JMenu exportMenu = new JMenu("Export");
		exportMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		exportMenu.setFont(xfont);
		exportMenu.setOpaque(true);
		//
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		toolsMenu.setFont(xfont);
		toolsMenu.setOpaque(true);
		//
		JMenu editMenu = new JMenu("Edit");
		editMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		editMenu.setFont(xfont);
		editMenu.setOpaque(true);
		//
		JMenu projectMenu = new JMenu("Project");
		projectMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		projectMenu.setFont(xfont);
		projectMenu.setOpaque(true);
		//
		JMenu propertyMenu = new JMenu("Properties");
		propertyMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		propertyMenu.setFont(xfont);
		propertyMenu.setOpaque(true);
		//
		JMenu filterMenu = new JMenu("Image Filters");
		filterMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		filterMenu.setFont(xfont);
		filterMenu.setOpaque(true);
		//
		JMenu moreMenu = new JMenu("More");
		moreMenu.setPreferredSize(new Dimension(Breedte,menuBar.getHeight()));
		moreMenu.setFont(xfont);
		moreMenu.setOpaque(true);
		//
		for(int i=0;i<arMenuItem.length;i++) arMenuItem[i]=null;
		//
		menuBar.add(fileMenu);
		menuBar.add(toolsMenu);
		//
		int k=-1;
		JMenu current = fileMenu;
		for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values().length;i++)
		{
			k++;
			String sText = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values()[i].toString();
			if( sText.startsWith("DIV") ) {
				current.add(new JSeparator());
				continue;
			}
			if( sText.compareToIgnoreCase("IMPORT")==0 ) {
				current.add(importMenu);
				current = importMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("IMPORT_CONT")==0 ) {
				current = fileMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("EXPORT")==0 ) {
				current.add(exportMenu);
				current = exportMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("EXPORT_CONT")==0 ) {
				current = fileMenu;
				continue;
			}
			if( sText.startsWith("TOOLS") ) {
				current = toolsMenu;
				continue;
			}
			if( sText.startsWith("PROJECT_START") ) {
				current.add(projectMenu);
				current = projectMenu;
				continue;
			}
			if( sText.startsWith("PROJECT_STOP") ) {
				current = fileMenu;
				continue;
			}
			if( sText.startsWith("PROPERTIES_START") ) {
				current.add(propertyMenu);
				current = propertyMenu;
				continue;
			}
			if( sText.startsWith("PROPERTIES_STOP") ) {
				current = fileMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("EDIT_ZONE_START")==0 ) {
				current.add(editMenu);
				current = editMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("EDIT_ZONE_STOP")==0 ) {
				current = toolsMenu;
				continue;
			}
			if( sText.startsWith("IMAGE_FILTERS") ) {
				current.add(filterMenu);
				current = filterMenu;
				continue;
			}
			if( sText.startsWith("TOOLS_CONT") ) {
				current = toolsMenu;
				continue;
			}
			if( sText.compareToIgnoreCase("MORE") == 0) {
				current.add(moreMenu);
				current = moreMenu;
				continue;
			}
			if( sText.startsWith("MORE_CONT") ) {
				current = toolsMenu;
				continue;
			}
            //
			arMenuItem[k] = new JMenuItem( memShared.makeLabel(sText) );
			arMenuItem[k].setFont(xfont);
			arMenuItem[k].addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent actionEvent) {
	                performMenuItemAction( actionEvent.getSource() );
	            }
	        });
		    current.add(arMenuItem[k]);
		}
		setRecent(ar);	
		return true;
	}
	
	//---------------------------------------------------------------------------------
	private void performMenuItemAction(Object iO)
	//---------------------------------------------------------------------------------
	{
		cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS pdx = null;
		try {
		  if( iO == null ) return;
		  JMenuItem mi = (JMenuItem)iO;
		  int idx=-1;
		  for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values().length;i++)
		  {
			if( arMenuItem[i] == null ) continue;
			if( mi == arMenuItem[i] ) { idx=i; break; }
		  }
		  if( idx < 0 ) return;
		  pdx = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values()[idx];
		}
		catch( Exception e ) {
			return;
		}
		if( pdx == null ) return;
		//
		switch( pdx )
		{
		case OPEN_PROJECT  : { memShared.performCallback("doProject","O"); break; }
		case EDIT_PROJECT  : { memShared.performCallback("doProject","E"); break; }
		case NEW_PROJECT   : { memShared.performCallback("doProject","N"); break; }
		case OPEN_IMAGE    : { memShared.performCallback("doClickImageButton"); break;}
		case OPEN_REPORT   : { memShared.performCallback("doClickReportButton"); break;}
		case OPEN_ARCHIVE  : { memShared.performCallback("doClickArchiveBrowser"); break;} 
		case SAVE          : { memShared.performCallback("doSave"); break;}
		case REFRESH       : { memShared.performCallback("doClickRefreshButton"); break;}
		case LOAD_IMAGE    : { memShared.performCallback("doClickImageButton"); break;}
		case EXTRACT_TEXT  : { memShared.performCallback("doClickExtractButton"); break;}
		case START_EDIT    : { memShared.performCallback("doClickEditButton"); break;}
		case STOP_EDIT     : { memShared.performCallback("doClickEditButton"); break;}
		case OCR           : { memShared.performCallback("doClickOCRButton"); break;}
		case TRANSLATE     : { memShared.performCallback("doClickTranslateButton"); break;}
		case REINJECT      : { memShared.performCallback("doClickReInjectButton"); break;}
		case REPORT        : { memShared.performCallback("doClickReportButton"); break;}
		case INFO          : { memShared.performCallback("doClickInfoButton"); break;}
		case EDIT_OPTIONS  : { memShared.performCallback("doClickOptionsButton"); break; }
		case QUICK_EDIT    : { memShared.performCallback("doQuickEdit"); break; }
		case DETAILED_EDIT : { memShared.performCallback("doDetailedEdit"); break; }
		case TESSERACT_OPTIONS  : { memShared.performCallback("doTesseractOptionDialog"); break; }
		case IMPORT_TEXT   : { memShared.performCallback("doImportText"); break; }
		case EXPORT_TEXT   : { memShared.performCallback("doExportText"); break; }
		//
		case RECENT1 : { memShared.performCallback("doRecent","0"); break; }
		case RECENT2 : { memShared.performCallback("doRecent","1"); break; }
		case RECENT3 : { memShared.performCallback("doRecent","2"); break; }
		case RECENT4 : { memShared.performCallback("doRecent","3"); break; }
		case RECENT5 : { memShared.performCallback("doRecent","4"); break; }
	    // filters
		case INVERT : 
		case ORIGINAL : 
		case GRAYSCALE : 
		case MONOCHROME_OTSU : 
		case BLUEPRINT : 
		case MONOCHROME_SAUVOLA :
		case BLEACHED :
		case MONOCHROME_NIBLAK :
		case MAINFRAME :
		case CONVOLUTION_EDGE :
		case CONVOLUTION_GAUSSIAN :
		case CONVOLUTION_SHARPEN :
		case CONVOLUTION_GLOW :
		case SOBEL_ON_GRAYSCALE :
		case SOBEL :
		case GRADIENT_WIDE :
		case GRADIENT_NARROW :
		case HISTOGRAM_EQUALISATION : { memShared.performCallback("doClickImageFilterButton",""+pdx); break; }
	    //
		case HOUSEKEEPING    : { memShared.performCallback("doHousekeeping"); break; }
		case STATISTICS      : { memShared.performCallback("doClickStatisticsButton"); break;}
		case MONITOR         : { memShared.performCallback("doClickMonitor"); break;}
		case ARCHIVE_VIEWER  : { memShared.performCallback("doClickArchiveViewer"); break;}
		case ARCHIVE_BROWSER : { memShared.performCallback("doClickArchiveBrowser"); break;}
	    //
		case EXIT : { memShared.performCallback("doClose"); break; }
		default : { memShared.performCallback("popMessage" , "Currently no code foreseen for [" + pdx + "]"); break; }
		}
	}
	
	//---------------------------------------------------------------------------------
	public void setRecent(String[] ar)
	//---------------------------------------------------------------------------------
	{
		int k=-1;
		for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values().length;i++)
		{
			String sText = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values()[i].toString();
			if( sText.startsWith("RECENT") == false ) continue;
			k++;
			String ss = "";
			if( k >= ar.length ) ss = "";
			else {
			if( ar[k] == null ) ss = "";  else ss = ar[k];
			}
			arMenuItem[i].setText( ss );
		}
	}
	
	//---------------------------------------------------------------------------------
	public void setMenuItemStatus(cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS mi , boolean stat)
	//---------------------------------------------------------------------------------
	{
		  int idx=-1;
		  for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values().length;i++)
		  {
			if( mi == cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values()[i] ) {
				idx=i;
				break;
			}
		  }
		  if( idx < 0 ) return;
          arMenuItem[idx].setEnabled( stat );
	}
	//---------------------------------------------------------------------------------
	public void setMenuItemVisible(cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS mi , boolean stat)
	//---------------------------------------------------------------------------------
	{
			  int idx=-1;
			  for(int i=0;i<cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values().length;i++)
			  {
				if( mi == cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.values()[i] ) {
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
		setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.START_EDIT , true );
		setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.STOP_EDIT , false);
		setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.QUICK_EDIT , false);
		setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.DETAILED_EDIT , false);
		//	
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE) ) {
			// disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setEnabled(false);
			}
			// enable the following
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OPEN_PROJECT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EDIT_PROJECT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.NEW_PROJECT , true );
			//
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OPEN_IMAGE , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OPEN_REPORT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OPEN_ARCHIVE , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.INFO , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.GENERAL_PROPERTIES , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.TESSERACT_OPTIONS , true );
			//
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.RECENT1 , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.RECENT2 , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.RECENT3 , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.RECENT4 , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.RECENT5 , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EXIT , true );
			//
		    setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.LOAD_IMAGE , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EXTRACT_TEXT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.START_EDIT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.TRANSLATE , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.REINJECT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OCR , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.REPORT , true );
			//
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.HOUSEKEEPING , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.STATISTICS , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MONITOR , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.ARCHIVE_VIEWER , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.ARCHIVE_BROWSER , true );
		    //
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.IMPORT_TEXT , true );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EXPORT_TEXT , true );

			return;
		}
	    //	in flux modi
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGELOADING) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTING) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDITLOADING) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.OCR_BUSY) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.STATSGATHERING) ) {
		    // disable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
				if( arMenuItem[i] != null ) arMenuItem[i].setEnabled(false);
			}
			// allow monitor
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MONITOR , true );
			//
			return;
		}
		// IMAGE
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE) ) {
			// enable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setEnabled(true);
			}
			return;
		}
		// EDIT
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDIT) ) {
		  // disable all 
		  for(int i=0;i<arMenuItem.length;i++)
		  {
			 if( arMenuItem[i] != null ) arMenuItem[i].setEnabled(false);
		  }
		  //
		  setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.START_EDIT , false );
		  setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.STOP_EDIT , true );
		  setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.QUICK_EDIT , true);
		  setMenuItemVisible( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.DETAILED_EDIT , true);
		  //
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.STOP_EDIT , true );
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.QUICK_EDIT , true );
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.DETAILED_EDIT , true );
		  //
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.SAVE , true );
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EDIT_OPTIONS , true );
		  setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EXIT , true );
			
	    }
		// Extract or Stats
		if( (as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTED) ||
			(as == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTED) ) {
			// enable all 
			for(int i=0;i<arMenuItem.length;i++)
			{
			 if( arMenuItem[i] != null ) arMenuItem[i].setEnabled(true);
			}
			// disable filters
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.ORIGINAL , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.BLUEPRINT , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MAINFRAME , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MONOCHROME_NIBLAK, false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MONOCHROME_SAUVOLA , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.MONOCHROME_OTSU , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.BLEACHED , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.GRAYSCALE , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.ORIGINAL , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.INVERT , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.SOBEL_ON_GRAYSCALE , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.SOBEL , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.HISTOGRAM_EQUALISATION , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.GRADIENT_WIDE , false );
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.GRADIENT_NARROW , false );
			
			setMenuItemStatus( cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.HOUSEKEEPING ,false );
			//
			return;
		}
		
		
	 }

	
}
