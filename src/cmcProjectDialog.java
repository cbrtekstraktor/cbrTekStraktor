import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcEnums.PROJECT_OPTIONS;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorProject.cmcFolderInitialization;
import cbrTekStraktorProject.cmcProjectCore;
import cbrTekStraktorProject.cmcProjectWrapper;
import dao.cmcProjectDAO;
import drawing.cmcGraphController;
import logger.logLiason;

public class cmcProjectDialog {
	 cmcProcSettings    xMSet = null;
	 logLiason          logger=null;
	 cmcProcEnums       cmcenums=null;
	 cmcProcEnums.PROJECT_OPTIONS tipe = cmcProcEnums.PROJECT_OPTIONS.UNKNOWN;
	 
	 String             LEGELIJN  = "                       ";
	 JLabel[]           arLabel   = new JLabel[cmcProcEnums.PROJECT_LABEL.values().length];
	 JTextField[]       arText    = new JTextField[arLabel.length];
	 JLabel[]           arSpacer  = new JLabel[cmcProcEnums.PROJECT_LABEL.values().length];
	 JComboBox[]        arBox     = new JComboBox[cmcProcEnums.PROJECT_DROP.values().length];
	 JLabel[]           arLabel2  = new JLabel[arBox.length];
	 JLabel[]           arSpacer2 = new JLabel[arBox.length];
	 JButton            okButton  = null;
	 JButton            cancelButton = null;
	 Color              kleur = Color.WHITE;
	 //Color              kleur = javax.swing.UIManager.getDefaults().getColor("Button.background");
	 
	 
	 String SuperFolder = null;
	 Long   SuperCreated = 0L;
	 int    adapterCounter=0;
	     
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
    public cmcProjectDialog(JFrame jf , cmcProcSettings iM , logLiason ilog , String itipe)
    //------------------------------------------------------------
    {
      xMSet = iM;
      kleur = cmcProcConstants.DEFAULT_LIGHT_GRAY;
      logger = ilog;
      cmcenums = new cmcProcEnums(xMSet);
   	  if( itipe.startsWith("N") ) tipe = cmcProcEnums.PROJECT_OPTIONS.NEW;
  	  else
  	  if( itipe.startsWith("E") ) tipe = cmcProcEnums.PROJECT_OPTIONS.EDIT;
  	  else
  	  if( itipe.startsWith("O") ) tipe = cmcProcEnums.PROJECT_OPTIONS.OPEN;
  	  else {
  		do_error("Internal error got tipe [" + itipe + "]");
  	  }
	  initialize(jf);
    }

    //------------------------------------------------------------
    private void initialize(JFrame jf)
    //------------------------------------------------------------
    {
    	try
        {
            final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setTitle( makeLabel("" + tipe + " Project") );
            dialog.setLocationRelativeTo( jf );
            dialog.setBackground(kleur);
            dialog.setLocation(10,10);
            //
            Font font =  xMSet.getPreferredFont();
            Dimension stdDim = new Dimension( 250 , font.getSize());
            //
            JPanel koepelPane = new JPanel();
            koepelPane.setBackground(kleur);
            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
            //
            //
            JPanel sepaPane1 = new JPanel();
            sepaPane1.setBackground(kleur);
            JLabel d0 = new JLabel();
            d0.setText(LEGELIJN);
            d0.setBackground(kleur);
            sepaPane1.add(d0);
            //
            JPanel bovenPane = new JPanel();
            bovenPane.setBackground(kleur);
            bovenPane.setLayout(new BoxLayout(bovenPane, BoxLayout.X_AXIS));
            //
            JPanel leftPane = new JPanel();
            leftPane.setBackground(kleur);
            leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
            //
            JPanel rightPane = new JPanel();
            rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
            rightPane.setBackground(kleur);
            //
            JPanel fillerPane = new JPanel();
            fillerPane.setLayout(new BoxLayout(fillerPane, BoxLayout.Y_AXIS));
            fillerPane.setBackground(kleur);
            //  ComboBoxes
            for(int i=0;i<arBox.length;i++)
		    {
            	JPanel x = new JPanel();
    	    	x.setLayout(new BoxLayout(x, BoxLayout.X_AXIS));
                x.setBackground(kleur);
                x.setAlignmentX(Component.CENTER_ALIGNMENT);
                //
                arLabel2[i] = new JLabel();
                arLabel2[i].setFont(font);
                arLabel2[i].setHorizontalAlignment(JLabel.RIGHT);
                arLabel2[i].setText(makeLabel(cmcProcEnums.PROJECT_DROP.values()[i].toString()) );
    			arLabel2[i].setPreferredSize(stdDim);
                x.add(arLabel2[i]);
            	// voeg een verticaal blokje tussen
    	    	arSpacer2[i] = new JLabel();
    	    	arSpacer2[i].setHorizontalAlignment(JLabel.CENTER);
    	    	arSpacer2[i].setFont(font);
    	    	arSpacer2[i].setPreferredSize(new Dimension(10,25));
    	    	arSpacer2[i].setText(":");
    	    	x.add(arSpacer2[i]);
    	        //
    	    	arBox[i] = new JComboBox();
    	    	arBox[i].setFont(font);
    	    	switch( cmcProcEnums.PROJECT_DROP.values()[i] )
    	    	{
    	    	 case PROJECT : {
    	    		 String lijst[] = getProjectList();
    		    	 for(int j=0;j<lijst.length;j++) arBox[i].addItem( lijst[j] );
    		    	 int k = xMSet.xU.getIdxFromList( lijst , "TODO-GET PROJECT ID" );
    		    	 if( k >= 0 ) arBox[i].setSelectedIndex(k);
    	    		 break;
    	    	 }
    	    	 case ENCODING : {
    	    		 String lijst[] = cmcenums.getEncodingList();
		    		 for(int j=0;j<lijst.length;j++) arBox[i].addItem( lijst[j].toUpperCase() );
		    		 // selectign the correct item is done in separate routine adapt
		    	 }
    	    	 case LANGUAGE : {
    	    		 String lijst[] = xMSet.getLanguageList();
		    		 for(int j=0;j<lijst.length;j++) arBox[i].addItem( xMSet.xU.Capitalize(lijst[j]) );
		    		 // selectign the correct item is done in separate routine adapt
		    	 }
    	    	 case EDITOR_BACKDROP : {
    	    		 String lijst[] = cmcenums.getBackdropTypeList();
		    		 for(int j=0;j<lijst.length;j++) arBox[i].addItem( xMSet.xU.Capitalize(lijst[j]) );
		    		 // selectign the correct item is done in separate routine adapt
		    	 }
    	    	 case BROWSER : {
    	    		 String lijst[] = cmcenums.getBrowserList();
		    		 for(int j=0;j<lijst.length;j++) arBox[i].addItem( xMSet.xU.Capitalize(lijst[j]) );
		    		 // selectign the correct item is done in separate routine adapt
		    	 }

    	    	 default : break;
    	    	}
    	    	//
    	    	arBox[i].setName("BOX[" + i + "]");
    	    	arBox[i].addActionListener(new ActionListener() {
    	             public void actionPerformed(ActionEvent event) {
    	                 JComboBox comboBox = (JComboBox) event.getSource();
    	                 handleEvent( comboBox.getName() , (String)comboBox.getSelectedItem() ); 
    	             }
    	        });
    	    	x.add(arBox[i]);
    	    	//
    	    	rightPane.add(x);
    	    	// voeg een lege panel toe = horizontale spacer
		    	JPanel y = new JPanel();
    	    	y.setLayout(new BoxLayout(y, BoxLayout.X_AXIS));
                y.setBackground(kleur);
                y.setAlignmentX(Component.CENTER_ALIGNMENT);
                //
		    	JLabel junk = new JLabel();
		    	junk.setPreferredSize(new Dimension(200,3));
		    	junk.setText(" ");
		    	y.add(junk);
		    	//
		    	rightPane.add(y);
		    }
            //  Content
            for(int i=0;i<arLabel.length;i++)
		    {
    	    	JPanel x = new JPanel();
    	    	x.setLayout(new BoxLayout(x, BoxLayout.X_AXIS));
                x.setBackground(kleur);
                x.setAlignmentX(Component.CENTER_ALIGNMENT);
                //
		    	arLabel[i] = new JLabel();
		    	arLabel[i].setHorizontalAlignment(JLabel.RIGHT);
		    	arLabel[i].setFont(font);
		    	arLabel[i].setPreferredSize(stdDim);
		    	arLabel[i].setText(makeLabel(cmcProcEnums.PROJECT_LABEL.values()[i].toString()) );
				x.add(arLabel[i]);
		    	//
		    	// voeg een verticaal blokje tussen
				arSpacer[i] = new JLabel();
				arSpacer[i].setHorizontalAlignment(JLabel.CENTER);
				arSpacer[i].setFont(font);
				arSpacer[i].setPreferredSize(new Dimension(10,25));
				arSpacer[i].setText(":");
		    	x.add(arSpacer[i]);
		    	//
		    	// Text Field
		    	arText[i] = new JTextField();
                arText[i].setText( "->" + i);
		    	arText[i].setPreferredSize(stdDim);
                x.add(arText[i]);
                
		    	// voeg de x pane met de 3 elementen toe
		    	rightPane.add(x);
		    
		    	// voeg een lege panel toe = horizontale spacer
		    	JPanel y = new JPanel();
    	    	y.setLayout(new BoxLayout(y, BoxLayout.X_AXIS));
                y.setBackground(kleur);
                y.setAlignmentX(Component.CENTER_ALIGNMENT);
                //
		    	JLabel junk = new JLabel();
		    	junk.setPreferredSize(new Dimension(200,3));
		    	junk.setText(" ");
		    	y.add(junk);
		    	//
		    	rightPane.add(y);
		    
		    }
    		//
	    	JLabel d4 = new JLabel();
            d4.setText(LEGELIJN);
            d4.setBackground(kleur);
            d4.setForeground(kleur);
            d4.setFont(font);;
            fillerPane.add(d4);
            //
            JPanel sepaPane2 = new JPanel();
            sepaPane2.setBackground(kleur);
            JLabel d3 = new JLabel();
            d3.setText(LEGELIJN);
            d3.setBackground(kleur);
            d3.setPreferredSize(stdDim);
            sepaPane2.add(d3);
            //
            JPanel sepaPane3 = new JPanel();
            sepaPane3.setBackground(kleur);
            JLabel d5 = new JLabel();
            d5.setText(LEGELIJN);
            d5.setBackground(kleur);
            d5.setPreferredSize(stdDim);
            sepaPane3.add(d5);
            //
            //bovenPane.add(leftPane);
            bovenPane.add(rightPane);
            bovenPane.add(fillerPane);
            //
            koepelPane.add(sepaPane1);
            koepelPane.add(bovenPane);
            koepelPane.add(sepaPane2);
            //
            JPanel onderPane = new JPanel();
            onderPane.setLayout(new BoxLayout(onderPane, BoxLayout.X_AXIS));
            //
            okButton = new JButton("OK"); 
            okButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    		      if( doClickOk() == true ) {
    		    	  dialog.dispose();    
    		      }
    			}
    		});
            cancelButton = new JButton("Cancel");
            cancelButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    			  dialog.dispose();
    			}
    		});
            //
            okButton.setFont(font);
            cancelButton.setFont(font);
            //
            onderPane.setBackground(okButton.getBackground());
            onderPane.add(okButton);
            onderPane.add(cancelButton);
            //
            koepelPane.add(onderPane);	
            koepelPane.add(sepaPane3);
            //
            dialog.add(koepelPane);
            dialog.pack();
            dialog.setLocationByPlatform(true);
            //
            adaptContent();
            //
            dialog.setVisible(true);
            //
        } 
        catch (Exception e) 
        {
            do_error( "(cmcEditOptionDialog) System error" + e.getMessage() + " " + xMSet.xU.LogStackTrace(e)) ;
        }

    }

    //---------------------------------------------------------------------------------
  	public String makeLabel(String sIn)
  	//---------------------------------------------------------------------------------
  	{
  			String sRet = "";
  			String sTemp = sIn.trim().toUpperCase();
  			char[] SChar = sTemp.toCharArray();
  		    for(int i=0;i<SChar.length;i++) 
  			{	
  			   if( i == 0 ) {
  				   sRet += SChar[i];
  				   sTemp = sTemp.toLowerCase();
  				   SChar = sTemp.toCharArray();
  				   continue;
  			   }
  			   if( SChar[i] == '_') SChar[i]=' ';
  			   sRet += SChar[i];
  				
  			}		
  			return sRet;
  	}
  	
 	//---------------------------------------------------------------------------------
    private void adaptContent()
    //---------------------------------------------------------------------------------
    {
    	adapterCounter++;
       	for(int i=0;i<arBox.length;i++)
    	{
    		arBox[i].setEnabled(false);
    		arBox[i].setVisible(true);
    		arLabel2[i].setVisible(true);
    	}
    	for(int i=0;i<arText.length;i++)
    	{
    		arText[i].setEnabled(false);
    		arText[i].setVisible(true);
    		arLabel[i].setVisible(true);
    	}
    	//
    	if( tipe == cmcProcEnums.PROJECT_OPTIONS.OPEN ) {
    		okButton.setText("Switch");
    		// drop downs
    		for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_DROP.values()[i] )
   	    		{
   	    		case PROJECT : {arBox[i].setEnabled(true); break; }
   	    	    default : break;
   	    		}
   	    	}
    		//
    		// text
   			for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
   	    		{
   	    		default : break;
   	    		}
   	    	}		
       	    //
    	}
    	else
   		if( tipe == cmcProcEnums.PROJECT_OPTIONS.EDIT ) {
   			okButton.setText("Confirm");
   		    // drop downs
    		for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_DROP.values()[i] )
   	    		{
   	    		case PROJECT : {arBox[i].setEnabled(false); break; }
   	    		case ENCODING : {arBox[i].setEnabled(true); break; }
   	    		case LANGUAGE : {arBox[i].setEnabled(true); break; }
   	    		case EDITOR_BACKDROP : {arBox[i].setEnabled(true); break; }
   	 		    case BROWSER : {arBox[i].setEnabled(true); break; }
   	   	       default : break;
   	    		}
   	    	}
    		// text
   			for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
   	    		{case PROJECT_NAME : ;
   	    		case DESCRIPTION : ;
   	    		case MEAN_CHARACTER_COUNT : ;
   	    		case HORIZONTAL_VERTICAL_VARIANCE_THRESHOLD : ;
   	    		case TESSERACT_FOLDER : ;
   	    		case PYTHON_FOLDER : ;
   	    		case DATEFORMAT : ;
   	    		case LOGGING_LEVEL : ;
   	    		case MAXIMUM_NUMBER_OF_THREADS : ;
   	    		case PREFERRED_FONT_NAME : ;
   	    		case PREFERRED_FONT_SIZE : { arText[i].setEnabled(true); break; }
   	    		//
   	    		case SIZE : ;
   	    		case NUMBER_OF_ARCHIVES : ;
   	    		case FIRST_ACCESSED : ;
   	    		case LAST_ACCESSED : { arText[i].setVisible(true); break; } 
   	    		default : break;
   	    		}
   	    	}
   			//
       	}
   		else
   	   	if( tipe == cmcProcEnums.PROJECT_OPTIONS.NEW ) {
   	   	    okButton.setText("Create");
		    // drop downs
    		for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_DROP.values()[i] )
   	    		{
   	    		case PROJECT  : {arBox[i].setVisible(false); break; }
   	    		case ENCODING : {arBox[i].setEnabled(true); break; }
   	    		case LANGUAGE : {arBox[i].setEnabled(true); break; }
   	    		case EDITOR_BACKDROP : {arBox[i].setEnabled(true); break; }
   	    		case BROWSER : {arBox[i].setEnabled(true); break; }
   	   	        default : break;
   	    		}
   	    	}
    		// Text
   			for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
   	    	{
   	    		switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
   	    		{
   	    		case PROJECT_NAME : ;
   	    		case DESCRIPTION : ;
   	    		case MEAN_CHARACTER_COUNT : ;
   	    		case HORIZONTAL_VERTICAL_VARIANCE_THRESHOLD : ;
   	    		case TESSERACT_FOLDER : ;
   	    		case PYTHON_FOLDER : ;
   	    		case DATEFORMAT : ;
   	    		case LOGGING_LEVEL : ;
   	    		case MAXIMUM_NUMBER_OF_THREADS : ;
   	    		case PREFERRED_FONT_NAME : ;
   	    		case PREFERRED_FONT_SIZE : { arText[i].setEnabled(true); break; }
   	    		//
   	    		case SIZE : ;
   	    		case NUMBER_OF_ARCHIVES : ;
   	    		case FIRST_ACCESSED : ;
   	    		case LAST_ACCESSED : { arText[i].setVisible(false); break; } 
   	    		default : break;
   	    		}
   	    	}		
            //
   	   	}
    	//
    	for(int i=0;i<arBox.length;i++)
    	{
    		arLabel2[i].setVisible(arBox[i].isVisible());
    		if( !arLabel2[i].isVisible() ) arSpacer2[i].setForeground(kleur);
       }
    	for(int i=0;i<arText.length;i++)
    	{
    		arLabel[i].setVisible(arText[i].isVisible());
    		if( !arLabel[i].isVisible() ) arSpacer[i].setForeground(kleur);
      }
       //	
       // prepare to read from the project configuration XML file
       //
       cmcProjectWrapper wrapper = xMSet.getCurrentProjectWrapper();
   	   cmcProjectCore core = null;
       switch( tipe )
       {
         case OPEN : {
        	 // if this is the first time the adapt runs, so when the dialog opens, pickthe current; else te one slected
        	 if( adapterCounter == 1) core = wrapper.getCore();	 
        	                     else core = readDAOViaDropDown(wrapper.getSuperDir());
        	 break; }
         case NEW  : { core = wrapper.getCore(); break; }  // read the current project 
         case EDIT : { core = wrapper.getCore(); break; } 
         default   : { core = wrapper.getCore(); break; }
         }
        //
     	if( core == null )  {
     		do_error("System error");
     		return;
     	}
    	//
    	SuperCreated = core.getCreated();
    	// stats
    	//
    	wrapper.fetchFileStats();
    	//
    	for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
	   	{
	   	 switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
	   	 {
	   	 case ROOT_FOLDER          : { SuperFolder = wrapper.getSuperDir(); arText[i].setText(wrapper.getSuperDir()); break; }
	   	 case PROJECT_NAME         : { arText[i].setText(core.getProjectName()); break; }
	   	 case DESCRIPTION          : { arText[i].setText(""+core.getProjectDescription()); break; } 
	   	 case MEAN_CHARACTER_COUNT : { arText[i].setText(""+core.getMeanCharacterCount()); break; }
	   	 case TESSERACT_FOLDER     : { arText[i].setText(core.getTesseractDir()); break; }
	 	 case PYTHON_FOLDER        : { arText[i].setText(core.getPythonHomeDir()); break; }
	   	 case PREFERRED_FONT_NAME  : { arText[i].setText(core.getPreferredFontName()); break; }
	   	 case PREFERRED_FONT_SIZE  : { arText[i].setText(""+core.getPreferredFontSize()); break; }
	   	 case DATEFORMAT           : { arText[i].setText(core.getDateformat()); break; }
	   	 case LOGGING_LEVEL        : { arText[i].setText(""+core.getLogLevel()); break; }
   	     case MAXIMUM_NUMBER_OF_THREADS : { arText[i].setText(""+core.getMaxThreads()); break; }
	   	 case SIZE                 : { arText[i].setText(""+wrapper.getSize() + " Bytes"); break; }
	   	 case NUMBER_OF_ARCHIVES   : { arText[i].setText(""+wrapper.getNumberOfArchives() + "/" + wrapper.getNumberOfFiles()); break; }
	   	 case FIRST_ACCESSED       : { arText[i].setText(""+xMSet.xU.prntDateTimeISODate(wrapper.getFirstAccessed(),core.getDateformat())); break; }
	   	 case LAST_ACCESSED        : { arText[i].setText(""+xMSet.xU.prntDateTimeISODate(wrapper.getLastAccessed(),core.getDateformat())); break; }
	   	 case HORIZONTAL_VERTICAL_VARIANCE_THRESHOLD : { arText[i].setText(""+core.getHorizontalVerticalVarianceThreshold()); break; }
		 default : break;
	     }
	   	}
    	// project values for drop down
    	for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
	   	{
  		 // read the items op drop down and choose the applicable one
   		switch( cmcProcEnums.PROJECT_DROP.values()[i] )
    	{
   	     case PROJECT : {
   	    	 String lijst[] = new String[arBox[i].getItemCount()];
   	    	 for (int k = 0; k < lijst.length; k++) lijst[k] = (String)arBox[i].getItemAt(k);
   	    	 String shFolderName = xMSet.xU.getFolderOrFileName(core.getProjectFolderName());
   	    	 int k = xMSet.xU.getIdxFromList( lijst , shFolderName );
   	    	 if( k >= 0 ) arBox[i].setSelectedIndex(k);
   	    	 break;
   	     }
   		 case ENCODING : {
    		 String lijst[] = new String[arBox[i].getItemCount()];
    		 for (int k = 0; k < lijst.length; k++) lijst[k] = (String)arBox[i].getItemAt(k);
    		 int k = xMSet.xU.getIdxFromList( lijst , ""+core.getEncoding() );
    		 if( k >= 0 ) arBox[i].setSelectedIndex(k);
    		 break;
    	  }
   		 case LANGUAGE : {
    		 String lijst[] = new String[arBox[i].getItemCount()];
    		 for (int k = 0; k < lijst.length; k++) lijst[k] = (String)arBox[i].getItemAt(k);
    		 int k = xMSet.xU.getIdxFromList( lijst , ""+core.getPreferredLanguageLong() );
    		 if( k >= 0 ) arBox[i].setSelectedIndex(k);
    		 break;
    	  }
   		case EDITOR_BACKDROP : {
   			String lijst[] = new String[arBox[i].getItemCount()];
   			for (int k = 0; k < lijst.length; k++) lijst[k] = (String)arBox[i].getItemAt(k);
   			int k = xMSet.xU.getIdxFromList( lijst , ""+core.getBackDropType() );
   			if( k >= 0 ) arBox[i].setSelectedIndex(k);
   			break;
   	      }
   		case BROWSER : {
   			String lijst[] = new String[arBox[i].getItemCount()];
   			for (int k = 0; k < lijst.length; k++) lijst[k] = (String)arBox[i].getItemAt(k);
   			int k = xMSet.xU.getIdxFromList( lijst , ""+core.getBrowser() );
   			if( k >= 0 ) arBox[i].setSelectedIndex(k);
   			break;
   	      }
   		
 	      default : break;
 	     }
	   	}
    	// if NEW then put an empty value in the name and description fields
    	if( tipe == cmcProcEnums.PROJECT_OPTIONS.NEW ) {
    		for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
    	   	{
    	   	 switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
    	   	 {
    	   	 case PROJECT_NAME         : { arText[i].setText(""); break; }
    	   	 case DESCRIPTION          : { arText[i].setText(""); break; } 
    	   	 default : break;
    	     }
    	   	}
    	}
    	//
    }
    
    //---------------------------------------------------------------------------------
    private String[] getProjectList()
    //---------------------------------------------------------------------------------
    {
        ArrayList<String> list = xMSet.xU.GetDirsInDir(xMSet.getSuperDir());
        int nbr = (list==null) ? 0 : list.size();
        if( nbr > 0) {
    	  nbr=0;
    	  for(int i=0;i<list.size();i++) 
    	  {
    		  String sFolder = list.get(i);
    		  String sTest = xMSet.getSuperDir() + xMSet.xU.ctSlash + sFolder + xMSet.xU.ctSlash + "cbrTekStraktor.xml";
    		  if( !xMSet.xU.IsBestand(sTest) ) { list.set(i,null); continue; } 
    		  nbr++;
    	  }
    	}
   	    String[] ar = null;
    	if( nbr == 0 ) {
    		ar = new String[1];
            ar[0] = "Error Could not find a project";    		
    	}
    	else {
    		ar = new String[nbr];
    		nbr= 0;
    		for(int i=0;i<list.size();i++) 
      	    {
    			if( list.get(i) == null ) continue;
    			ar[nbr] = list.get(i);
    			nbr++;
      	    }
    	}
    	return ar;
    }
   
    //---------------------------------------------------------------------------------
    private cmcProjectCore grabProjectCoreCharacteristics()
    //---------------------------------------------------------------------------------
    {
    	cmcProjectCore proj = new cmcProjectCore(null);
    	// drop downs
    	cmcProcEnums enu = new cmcProcEnums(xMSet);
		for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
	    {
	    		switch( cmcProcEnums.PROJECT_DROP.values()[i] )
	    		{
	      	    case PROJECT : {
	      	    	proj.setProjectName(arBox[i].getSelectedItem().toString());
	      	    	proj.setProjectFolderName(SuperFolder + xMSet.xU.ctSlash + proj.getProjectName());
	      	    	break;
	      	    }
	    		case ENCODING : { 
	    			String enc = arBox[i].getSelectedItem().toString();
	    			cmcProcEnums.ENCODING cp = enu.getEncoding(enc);
	    			if( cp != null ) proj.setEncoding(cp);
	    			break; }
	    		case LANGUAGE : { 
	    			String slang = arBox[i].getSelectedItem().toString();
	    			proj.setPreferredLanguageLong(slang);
	    			break; }
	    		case EDITOR_BACKDROP : { 
	    			String enc = arBox[i].getSelectedItem().toString();
	    			cmcProcEnums.BackdropType bt = enu.getBackdropType(enc);
	    			if( bt != null ) proj.setBackDropType(bt);
	    			break; }
	    		case BROWSER : { 
	    			String enc = arBox[i].getSelectedItem().toString();
	    			cmcProcEnums.BROWSER bt = enu.getBrowser(enc);
	    			if( bt != null ) proj.setBrowser(bt);
	    			break; }
	    		
	    	    default : break;
	    		}
	    }
		enu = null;
		// Text
		for(int i=0;i<cmcProcEnums.PROJECT_LABEL.values().length;i++)
    	{
	    		switch( cmcProcEnums.PROJECT_LABEL.values()[i] )
	    		{
	    		case PROJECT_NAME         : { proj.setProjectName(arText[i].getText()); break; }
	    		case DESCRIPTION          : { proj.setDescription(arText[i].getText()); break; }
	    		case TESSERACT_FOLDER     : { proj.setTesseractDir(arText[i].getText()); break; }
	    		case PYTHON_FOLDER        : { proj.setPythonHomeDir(arText[i].getText()); break; }
	    		case DATEFORMAT           : { proj.setDateformat(arText[i].getText()); break; }
	    		case PREFERRED_FONT_NAME  : { proj.setPreferredFontName(arText[i].getText()); break; }
            	case LOGGING_LEVEL        : { proj.setLogLevel(xMSet.xU.NaarInt(arText[i].getText())); break; }
            	case MAXIMUM_NUMBER_OF_THREADS : { proj.setMaxThreads(xMSet.xU.NaarInt(arText[i].getText())); break; }
		    	case MEAN_CHARACTER_COUNT : { proj.setMeanCharacterCount(xMSet.xU.NaarInt(arText[i].getText())); break; }
	    		case PREFERRED_FONT_SIZE  : { proj.setPreferredFontSize(xMSet.xU.NaarInt(arText[i].getText())); break; }
	    		case HORIZONTAL_VERTICAL_VARIANCE_THRESHOLD : { proj.setHorizontalVerticalVarianceThreshold(xMSet.xU.NaarInt(arText[i].getText())); break; }
		    	//
	    		default : break;
	    		}
    	}
		return proj;
    }
    
    //---------------------------------------------------------------------------------
    private boolean createNewProject()
    //---------------------------------------------------------------------------------
    {
    	boolean isOK=true;
    	//
    	cmcProjectCore proj = grabProjectCoreCharacteristics();
        //	
		String sErr = "";
		String sFolderName=null;
		// Make new folder
		if( proj.getProjectName() != null ) {
		   sFolderName = xMSet.xU.Capitalize(xMSet.xU.keepLettersAndNumbers(proj.getProjectName().trim()));
		   if( sFolderName == null ) sFolderName = "";
		   if( sFolderName.length() < 5 ) sErr = "Foldername [" + sFolderName + "] is too short";
		   else {
			   sFolderName = SuperFolder + xMSet.xU.ctSlash + sFolderName;
			   proj.setProjectFolderName(sFolderName);
		   }
		}
		else proj.setProjectFolderName(null);
		// assess settings
		if( (proj.hasValidProjectCharacteristics() == false) || (sErr.length()>0) ) {
			isOK = false;
			sErr += proj.getErrors();
		    do_error( "Project is invalid " + sErr );
		    JOptionPane.showMessageDialog(null,sErr,"Invalid project characteristics",JOptionPane.ERROR_MESSAGE);
		}
		// bon maak het project dan maar aan
		if( isOK ) {
			// check dirs
			if( xMSet.xU.IsDir(SuperFolder) == false ) {
			  	JOptionPane.showMessageDialog(null,"Cannot access superfolder [" + SuperFolder + "]","Project [" + sFolderName + "]",JOptionPane.ERROR_MESSAGE);
			  	isOK = false;
			}
			else {
			 if( xMSet.xU.IsDir(sFolderName) == true ) {
			  	JOptionPane.showMessageDialog(null,"Projectfolder already exists","Project [" + sFolderName + "]",JOptionPane.ERROR_MESSAGE);
			  	isOK = false;
			 }
			 else {
			  // run the initialize class
			  cmcFolderInitialization ini = new cmcFolderInitialization(xMSet,logger);
			  boolean ib = ini.checkFoldersAndFiles( sFolderName , true , true ); // safest options to create
			  //
		      if( xMSet.xU.IsDir(sFolderName) == false ) {
				  	JOptionPane.showMessageDialog(null,"Cannot create folder","Project [" + sFolderName + "]",JOptionPane.ERROR_MESSAGE);
				  	isOK = false;
		      }
		      else {  // overwrite
			   cmcProjectDAO dao = new cmcProjectDAO(xMSet,logger);
	           isOK = dao.writeConfig(proj);
	           dao=null;
		      }
			 }
			}
	        if( isOK == false )
	        	JOptionPane.showMessageDialog(null,"Could not create project files and folders","Project [" + sFolderName + "]",JOptionPane.ERROR_MESSAGE);
	        else
	      	   JOptionPane.showMessageDialog(null,"project created","Project [" + sFolderName + "]",JOptionPane.WARNING_MESSAGE);
		}
		//
    	return isOK;
    }
    
    //---------------------------------------------------------------------------------
    private boolean doClickOk()
    //---------------------------------------------------------------------------------
    {
        switch( tipe )
        {
        case OPEN : return switchProject(SuperFolder);
        case NEW  : return createNewProject(); 
        case EDIT : return editProject(); 
        default   : break;
        }
    	return false;
    }
    
    //---------------------------------------------------------------------------------
    private boolean editProject()
    //---------------------------------------------------------------------------------
    {
    	boolean isOK=true;
    	String sErr="";
    	cmcProjectCore proj = grabProjectCoreCharacteristics();
    	
    	// overrule values not displayed
    	proj.setCreated(SuperCreated);
    	//
    	if( (proj.hasValidProjectCharacteristics() == false)  ) {
			isOK = false;
			sErr += proj.getErrors();
		    do_error( "Project is invalid " + sErr );
		}
    	if( isOK ) {
    	  if( xMSet.xU.IsDir( proj.getProjectFolderName() ) == true ) {
    		   // overwrite
               cmcProjectDAO dao = new cmcProjectDAO(xMSet,logger);
	           isOK = dao.writeConfig(proj);
	           dao=null;
	           sErr += "Error while writing configuration XML in [" + proj.getProjectFolderName() + "]";
    	  }
    	  else {
    		sErr += "Cannot access folder [" +  proj.getProjectFolderName() + "]";
    		isOK = false;
    	  }
    	}
    	//
    	if( isOK == false )
	    JOptionPane.showMessageDialog(null,proj.getErrors(),"Invalid project characteristics",JOptionPane.ERROR_MESSAGE);
    	else
    	JOptionPane.showMessageDialog(null,"Project [" + proj.getProjectName() + "] has been updated","Project updated",JOptionPane.ERROR_MESSAGE);
    	// reload teh config file
    	boolean ib = xMSet.switchProject(proj.getProjectFolderName());
   //do_error( "test => " + xMSet.getBackDropType() );
     	return isOK;
    }
    
    //---------------------------------------------------------------------------------
    private String getFolderNameViaDropDown(String SuperFolder)
    //---------------------------------------------------------------------------------
    {
    	// get folder of the selected project via the dropdown
        String selProjectFolderShort = null;
        for(int i=0;i<cmcProcEnums.PROJECT_DROP.values().length;i++)
	   	{
   		 switch( cmcProcEnums.PROJECT_DROP.values()[i] )
    	 {
   	      case PROJECT : {
   	    	 selProjectFolderShort = arBox[i].getSelectedItem().toString();
   	    	 break;
   	      }
   	      default : break;
    	 }
	   	}
        if( selProjectFolderShort == null ) {
        	do_error("Cannot read short foldername from dowp down");
        	return null;
        }
        String ProjectFolderName = SuperFolder + xMSet.xU.ctSlash + selProjectFolderShort;
        return ProjectFolderName;
    }
    
    //---------------------------------------------------------------------------------
    private cmcProjectCore readDAOViaDropDown(String SuperFolder)
    //---------------------------------------------------------------------------------
    {
    	String ProjectFolderName = getFolderNameViaDropDown(SuperFolder);
 	    cmcProjectDAO dao = new cmcProjectDAO(xMSet,logger);
        cmcProjectCore proj = dao.readProjectXMLConfig(ProjectFolderName);
        if( proj == null ) {
    	   do_error("Could not read data via DAO for [" + ProjectFolderName + "]");
        }
        return proj;
    }
    
    //---------------------------------------------------------------------------------
    private void handleEvent( String boxname , String selected)
    //---------------------------------------------------------------------------------
    {
    	switch( tipe )
        {
        case OPEN : {
        	if ( boxname.compareToIgnoreCase("BOX[0]") != 0 ) return;
            adaptContent();
        	break;
        }
        case NEW  : break; 
        case EDIT : break; 
        default   : break;
        }
    }

    //---------------------------------------------------------------------------------
    private boolean switchProject(String SuperFolderName)
    //---------------------------------------------------------------------------------
    {
     	String ProjectFolderName = getFolderNameViaDropDown(SuperFolder);
     	boolean ib = xMSet.switchProject(ProjectFolderName);
     	if( ib ) {
     		JOptionPane.showMessageDialog(null,"Project switched","Project [" + xMSet.getCurrentProjectWrapper().getProjectName() + "]",JOptionPane.WARNING_MESSAGE);
     	     xMSet.requestProjectSwap(true);
     	}
     	else
     	if( ib ) JOptionPane.showMessageDialog(null,"Could not switch to [" + ProjectFolderName + "]","Project [" + xMSet.getCurrentProjectWrapper().getProjectName() + "]",JOptionPane.ERROR);
     	return ib;
    }
}
