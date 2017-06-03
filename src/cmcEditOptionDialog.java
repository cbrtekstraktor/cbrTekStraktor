import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphController;


public class cmcEditOptionDialog {

	cmcProcSettings    xMSet = null;
    cmcGraphController gCon=null;
    cmcProcEnums       cenums = null;
    logLiason          logger=null;
    
    JLabel[]           arLabel = new JLabel[8];
    JCheckBox[]        arCheck = new JCheckBox[arLabel.length];
    JComboBox[]        arDropField = new JComboBox[2];
    String             LEGELIJN = "                       ";

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
	public cmcEditOptionDialog(JFrame jf , cmcProcSettings iM , cmcGraphController ig , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = iM;
		logger = ilog;
		gCon = ig;
		cenums = new cmcProcEnums(xMSet);
		xMSet.setDialogCompleteState(false);
		initialize(jf);
	}

	//---------------------------------------------------------------------------------
	private void initialize(JFrame jf)
	//---------------------------------------------------------------------------------
	{
	try
        {
            final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setTitle( gCon.getTitle());
            dialog.setLocationRelativeTo( jf );
            dialog.setLocation(10,10);
            //
            Font font =  xMSet.getPreferredFont();
            //
            JPanel koepelPane = new JPanel();
            koepelPane.setBackground(Color.WHITE);
            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
            //
            //
            JPanel sepaPane1 = new JPanel();
            sepaPane1.setBackground(Color.WHITE);
            JLabel d0 = new JLabel();
            d0.setText(LEGELIJN);
            d0.setBackground(Color.WHITE);
            sepaPane1.add(d0);
            //
            JPanel bovenPane = new JPanel();
            bovenPane.setBackground(Color.WHITE);
            bovenPane.setLayout(new BoxLayout(bovenPane, BoxLayout.X_AXIS));
            //
            JPanel leftPane = new JPanel();
            leftPane.setBackground(Color.WHITE);
            leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
            //
            JPanel rightPane = new JPanel();
            rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.Y_AXIS));
            rightPane.setBackground(Color.WHITE);
            //
            JPanel fillerPane = new JPanel();
            fillerPane.setLayout(new BoxLayout(fillerPane, BoxLayout.Y_AXIS));
            fillerPane.setBackground(Color.WHITE);
            //  Content
            for(int i=0;i<arLabel.length;i++)
		    {
    	    	JPanel x = new JPanel();
    	    	x.setLayout(new BoxLayout(x, BoxLayout.X_AXIS));
                x.setBackground(Color.WHITE);
                x.setAlignmentX(Component.LEFT_ALIGNMENT);
                //
		    	arCheck[i] = new JCheckBox();
		    	arCheck[i].setEnabled(true);
		    	arCheck[i].setBackground(Color.WHITE);
    	    	x.add(arCheck[i]);
    	    	
    	    	switch(i)
    	    	{
    	    	case 0 : { arCheck[i].setSelected(gCon.getShoPayloadBoundaries()); break; }
    	    	case 1 : { arCheck[i].setSelected(gCon.getShoFrames()); break; }
    	    	case 2 : { arCheck[i].setSelected(gCon.getShoParagraphs()); break; }
    	    	case 3 : { arCheck[i].setSelected(gCon.getShoTextParagraphs()); break; }
    	    	case 4 : { arCheck[i].setSelected(gCon.getShoCharacters()); break; }
    	    	case 5 : { arCheck[i].setSelected(gCon.getShoNoise()); break; }
    	    	case 6 : { arCheck[i].setSelected(gCon.getShoValids()); break; }
    	    	case 7 : { arCheck[i].setSelected(gCon.getShoInvalids()); break; }
    	    	}
    	    	//
		    	arLabel[i] = new JLabel();
		    	arLabel[i].setHorizontalAlignment(JLabel.LEFT);
		    	arLabel[i].setFont(font);
		    	x.add(arLabel[i]);
		    	//
		    	switch(i)
		    	{
		    	case 0 : { arLabel[i].setText("Show payload boundaries"); break; }
		    	case 1 : { arLabel[i].setText("Show frames"); break; }
				case 2 : { arLabel[i].setText("Show paragraphs"); break; }
				case 3 : { arLabel[i].setText("Show text paragraphs"); break; }
				case 4 : { arLabel[i].setText("Show characters"); break; }
				case 5 : { arLabel[i].setText("Show noise"); break; }
				case 6 : { arLabel[i].setText("Show valid components"); break; }
		    	case 7 : { arLabel[i].setText("Show invalid components"); break; }
		    	}
		    	rightPane.add(x);
		    }
            JLabel d1 = new JLabel();
            d1.setText(LEGELIJN + "Backdrop   ");
            d1.setBackground(Color.WHITE);
            d1.setForeground(Color.WHITE);
            d1.setFont(font);;
            leftPane.add(d1);
            //
            JPanel midPane = new JPanel();
            midPane.setLayout(new BoxLayout(midPane, BoxLayout.X_AXIS));
            midPane.setBackground(Color.WHITE);
            //
            JLabel d2 = new JLabel();
            d2.setFont(font);
            d2.setText(LEGELIJN + "Backdrop   ");
            midPane.add(d2);
            //
            arDropField[0] = new JComboBox();
	    	arDropField[0].setFont(font);
	    	midPane.add(arDropField[0]);
	    	String lijst[] = cenums.getBackdropTypeList();
	    	for(int j=0;j<lijst.length;j++) arDropField[0].addItem( lijst[j] );
    		int k = xMSet.xU.getIdxFromList( lijst , ""+gCon.getBackdropType() );
    		if( k >= 0 ) arDropField[0].setSelectedIndex(k);
    		//
    		JLabel d9 = new JLabel();
            d9.setFont(font);
            d9.setText("BackdropBackdrop");
            d9.setBackground(Color.WHITE);
            d9.setForeground(Color.WHITE);
            midPane.add(d9);
            // 
    		//
    		JPanel midPane2 = new JPanel();
            midPane2.setLayout(new BoxLayout(midPane2, BoxLayout.X_AXIS));
            midPane2.setBackground(Color.WHITE);
            //
            JLabel d2b = new JLabel();
            d2b.setFont(font);
            d2b.setText(LEGELIJN + "Wisker        ");
            midPane2.add(d2b);
    		//
    		arDropField[1] = new JComboBox();
 	    	arDropField[1].setFont(font);
 	    	midPane2.add(arDropField[1]);
 	    	String wlijst[] = cenums.getWiskerTypeList();
 	    	for(int j=0;j<wlijst.length;j++) arDropField[1].addItem( wlijst[j] );
     		k = xMSet.xU.getIdxFromList( wlijst , ""+gCon.getWiskerType() );
     		if( k >= 0 ) arDropField[1].setSelectedIndex(k);
	    	//
     		JLabel d9b = new JLabel();
            d9b.setFont(font);
            d9b.setText("BackdropBackdrop");
            d9b.setBackground(Color.WHITE);
            d9b.setForeground(Color.WHITE);
            midPane2.add(d9b);
    		//
	    	JLabel d4 = new JLabel();
            d4.setText(LEGELIJN);
            d4.setBackground(Color.WHITE);
            d4.setForeground(Color.WHITE);
            d4.setFont(font);;
            fillerPane.add(d4);
            //
            JPanel sepaPane2 = new JPanel();
            sepaPane2.setBackground(Color.WHITE);
            JLabel d3 = new JLabel();
            d3.setText(LEGELIJN);
            d3.setBackground(Color.WHITE);
            sepaPane2.add(d3);
            //
            JPanel sepaPane3 = new JPanel();
            sepaPane3.setBackground(Color.WHITE);
            JLabel d5 = new JLabel();
            d5.setText(LEGELIJN);
            d5.setBackground(Color.WHITE);
            sepaPane3.add(d5);
            //
            bovenPane.add(leftPane);
            bovenPane.add(rightPane);
            bovenPane.add(fillerPane);
            //
            koepelPane.add(sepaPane1);
            koepelPane.add(bovenPane);
            koepelPane.add(midPane);
            koepelPane.add(midPane2);
            koepelPane.add(sepaPane2);
            //
            JPanel onderPane = new JPanel();
            onderPane.setLayout(new BoxLayout(onderPane, BoxLayout.X_AXIS));
            //
            JButton okButton = new JButton("Ok"); 
            okButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    				//
    				gCon.setShoPayloadBoundaries(arCheck[0].isSelected());
    				gCon.setShoFrames(arCheck[1].isSelected());
    				gCon.setShoParagraphs(arCheck[2].isSelected());
    				gCon.setShoTextParagraphs(arCheck[3].isSelected());
    				gCon.setShoCharacters(arCheck[4].isSelected());
    				gCon.setShoNoise(arCheck[5].isSelected());
    				gCon.setShoValids(arCheck[6].isSelected());
    				gCon.setShoInvalids(arCheck[7].isSelected());
    				gCon.setBackdropTipe( cenums.getBackdropType(""+arDropField[0].getSelectedItem() ) );
    				gCon.setWiskerTipe( cenums.getWiskerType(""+arDropField[1].getSelectedItem() ) );
    				//
    				gCon.showSettings();
    				//
    				xMSet.setDialogCompleteState(true);
    				//
     			    dialog.dispose();
    			}
    		});
            /*
            JButton saveButton = new JButton("Save picture");
            saveButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    			  //dialog.dispose();
    			}
    		});
    		*/
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    			  dialog.dispose();
    			}
    		});
            //
            okButton.setFont(font);
            //saveButton.setFont(font);
            cancelButton.setFont(font);
            //
            onderPane.setBackground(okButton.getBackground());
            onderPane.add(okButton);
            //onderPane.add(saveButton);
            onderPane.add(cancelButton);
            //
            koepelPane.add(onderPane);	
            koepelPane.add(sepaPane3);
            //
            dialog.add(koepelPane);
            dialog.pack();
            dialog.setLocationByPlatform(true);
            dialog.setVisible(true);
            //
        } 
        catch (Exception e) 
        {
            do_error( "(cmcEditOptionDialog) System error" + e.getMessage() + " " + xMSet.xU.LogStackTrace(e)) ;
        }
	}
	
	
}
