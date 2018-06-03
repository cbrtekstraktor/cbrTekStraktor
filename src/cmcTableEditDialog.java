import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphController;


public class cmcTableEditDialog {

	cmcProcSettings xMSet = null;
	cmcTableEditModel MyScanModel   = null;
    cmcProcEnums cenums = null;
    cmcGraphController gCon = null;
	logLiason logger=null;
	
	JScrollPane frmComicScrollPane;
	JComboBox filterComboBox;
	JTable frmComicScannerTable;
	JPanel onderPane;
	JPanel omvatOnderPane;
	JButton okButton=null;
	JButton cancelButton=null;
	JCheckBox checker=null;
	
	private int CurrentSelectedRow = -1;
	private int CurrentSelectedCol = -1;
	private int maxImageBreedte=200;
	private boolean isOK=false;
	private boolean okPressed=false;
	private boolean cancelPressed=false;
    private int PreviousSelectedRow = -1;
    private int PreviousSelectedCol = -1;
	
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
	public cmcTableEditDialog(JFrame jf , cmcProcSettings iM , String FNaam , cmcGraphController ic , logLiason ilog )
	//-----------------------------------------------------------------------
	{
		xMSet = iM;
		gCon = ic;
		cenums = new cmcProcEnums(xMSet);
		logger = ilog;
		if( xMSet.xU.IsBestand(FNaam) ) {
		 MyScanModel = new cmcTableEditModel(xMSet,this,logger,gCon);
		 MyScanModel.ReadDataAndShow(FNaam);
		 maxImageBreedte = MyScanModel.getMaxImageWidth();
		 MyScanModel.fireTableDataChanged();
		 initialize(jf);
		}
		else {
		  do_error("Cannot laocate [" + FNaam + "]");
		}
	}

	//-----------------------------------------------------------------------
	public boolean isDialogOk()
	//-----------------------------------------------------------------------
	{
		return isOK;
	}
	
	//-----------------------------------------------------------------------
	private void initialize(JFrame jf)
	//-----------------------------------------------------------------------
	{
        PreviousSelectedRow = PreviousSelectedCol = -1;
        //
		try {
			   final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
	            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	            dialog.setTitle(MyScanModel.getCMXUID());
	            dialog.setLocationRelativeTo( jf );
	            dialog.setLocation(10,10);
	            //
	            dialog.addComponentListener(new ComponentListener() {
                 public void componentHidden(ComponentEvent e)
                 {
                   //System.out.println("dialog hidden");
                 }
                 public void componentMoved(ComponentEvent e)
                 {
                    //
                 }
                 public void componentResized(ComponentEvent e)
                 {
                	 perform_resize( dialog );
                 }
                 public void componentShown(ComponentEvent e)
                 {
                   zetKolomHoogte();
                 }
                });
	            dialog.addWindowListener(new WindowAdapter() {
	                @Override
	                public void windowClosed(WindowEvent e) {
	                    do_close(dialog);
	                }
	            });
	            //
	            Font font = xMSet.getPreferredFont();  
	            //
	            JPanel koepelPane = new JPanel();
	            //koepelPane.setBackground(Color.WHITE);
	            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
	            //
	            JPanel upperPanel = new JPanel();
	            upperPanel.setBackground(Color.WHITE);
	            upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
	            //
	            checker = new JCheckBox("Monochrome picture");
	            checker.setSelected(xMSet.getuseMonoChromeInDialogs());
	            checker.setBackground(Color.WHITE);
	            checker.addItemListener(new ItemListener() {
		            public void itemStateChanged(ItemEvent e) {
		              swapColorMonochrome();
		            }
		         });
	            upperPanel.add(checker);
	            
		            
	            //
	            JPanel bovenPanel = new JPanel();
	            //bovenPanel.setBackground(Color.WHITE);
	            bovenPanel.setLayout(new BoxLayout(bovenPanel, BoxLayout.X_AXIS));
	            //
	            frmComicScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    		//frmComicScrollPane.setBounds(55, 76, 1151, 398);
	    		bovenPanel.add(frmComicScrollPane);
	    		
	    		frmComicScannerTable = new JTable();
	    		/*
	    		frmComicScannerTable.setDefaultRenderer(Object.class, new TableCellRenderer(){
	                private DefaultTableCellRenderer DEFAULT_RENDERER =  new DefaultTableCellRenderer();
	                @Override
	                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	                    Component c = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	                    if (row%2 == 0){
	                        c.setBackground(Color.WHITE);
	                    }
	                    else {
	                        c.setBackground(Color.LIGHT_GRAY);
	                    }                        
	                    return c;
	                }

	            });
	    		*/
	    		frmComicScannerTable.addFocusListener(new FocusAdapter() {
	    			@Override
	    			public void focusLost(FocusEvent arg0) { // nodig om bij te houden wat de huidige selectie is
	    				CurrentSelectedRow = -1;
	    				CurrentSelectedCol = -1;
	    				checkButtonStatus();
	    			}
	    		});
	    		//
	    		frmComicScannerTable.setRowHeight(30);
	    		frmComicScannerTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);   // zorgt voor de horizontale scrollbar
	    		//
	    		frmComicScannerTable.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    				//
	    				JTable target = (JTable)arg0.getSource();
	    				CurrentSelectedRow = target.getSelectedRow();
	    		        CurrentSelectedCol = target.getSelectedColumn();
                        if( (CurrentSelectedRow != PreviousSelectedRow) || (CurrentSelectedCol != PreviousSelectedCol) ) {
	                        xMSet.setQuickEditRequestedRow(-1);  		        	
	    		        }
	    		        PreviousSelectedRow = CurrentSelectedRow;
	    		        PreviousSelectedCol = CurrentSelectedCol;
	    		  }
	    		});
	    		/*
	    		frmComicScannerTable.addKeyListener(new KeyAdapter() {         
	                public void keyPressed(KeyEvent e) {
	                    System.out.println("pressed");
	                    char key = e.getKeyChar();
	                }
	            });
	            */
	    		//
	    		frmComicScannerTable.setModel(MyScanModel);
	    		//
	    		TableColumnModel tcm = frmComicScannerTable.getColumnModel();
	    		// Renderers zetten
	    		for(int i=0;i<tcm.getColumnCount();i++)
	    		{
	    			TableColumn tc = tcm.getColumn(i);	
	    			int BREEDTE = 100;
	    		    // image
	    			if( i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.IMAGE) ) {
	    				 tc.setMaxWidth((maxImageBreedte+cmcProcConstants.QUICKEDITBORDER)*2);  tc.setMinWidth(maxImageBreedte+cmcProcConstants.QUICKEDITBORDER);
	    				 continue;
	    			}
	    			// toggles
	    			if( (i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ISTEXT)) || 
	    				(i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.REMOVED))) {
	    				 tc.setMaxWidth(BREEDTE/2);  tc.setMinWidth(BREEDTE/2);
	    				 continue;
	    			}
	    			if( (i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.WIDTH)) || 
    					(i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.HEIGHT)) || 
    					(i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ELEMENTS)) || 
		    			(i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.LETTERS))) {
		    			 tc.setMaxWidth(BREEDTE);  tc.setMinWidth(BREEDTE);
		    			 continue;
		    			}
	    			// text
	    			if( i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.EXTRACTED_TEXT) ) {
	    				 tc.setMaxWidth(2*BREEDTE);  tc.setMinWidth(2*BREEDTE);
	    				 continue;
	    			}
	    			// Size
	    			tc.setMaxWidth(BREEDTE);  
	    			tc.setMinWidth(BREEDTE); 
	    		}
                //	            
	    		frmComicScrollPane.setViewportView(frmComicScannerTable);
	    	    //
	    		// Clicken on header of table
	    		frmComicScannerTable.getTableHeader().addMouseListener(new MouseAdapter() {
	    		    @Override
	    		    public void mouseClicked(MouseEvent e) {
	    		        int col = frmComicScannerTable.columnAtPoint(e.getPoint());
	    		        String name = frmComicScannerTable.getColumnName(col);
	    		        do_log( 9 , "Click on Header column index selected " + col + " " + name);
	    		        //xCtrl.doeSorteer(col,MyScanModel.toggleColSortOrder(col));
	    		        //MyScanModel.fireTableDataChanged();
	    		    }
	    		});
	    	    //
	    		koepelPane.add(upperPanel);
	            koepelPane.add(bovenPanel);
	            //
	            omvatOnderPane = new JPanel();
	            omvatOnderPane.setLayout(new BoxLayout(omvatOnderPane, BoxLayout.X_AXIS));
	            //
	            JPanel onderPane = new JPanel();
	            //
			    filterComboBox = new JComboBox();
				filterComboBox.setFont(font);
				//filterComboBox.setModel(new DefaultComboBoxModel(
				//		new String[]{"Identified Text Areas", "Potential Text Areas" , "Frames" , "Speech Bubbles" , "Noise" , "All" }));
				//
				String lijst[] = cenums.getQuickEditOptionsList();
		    	for(int j=0;j<lijst.length;j++) filterComboBox.addItem(xMSet.xU.Remplaceer(lijst[j],"_"," "));
		    	int k = xMSet.xU.getIdxFromList(lijst,""+xMSet.getQuickEditOption());
	    		if( k >= 0 ) filterComboBox.setSelectedIndex(k);
				filterComboBox.addActionListener(new ActionListener(){
		            public void actionPerformed(ActionEvent e){
		              	doDropDown();
		            }
		        });
			
			    //
			    okButton = new JButton("Confirm");
			    okButton.setEnabled(false);
	            okButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  okPressed=true;
	    			  dialog.dispose();
	    			}
	    		});
	            okButton.setFont(font);
	            //
			    cancelButton = new JButton("Stop");
	            cancelButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  cancelPressed=true;
	    			  dialog.dispose();
	    			}
	    		});
	            cancelButton.setFont(font);
	            //
	            GroupLayout layout = new GroupLayout(onderPane);
	            onderPane.setLayout(layout);
	            layout.setAutoCreateGaps(true);
	            layout.setAutoCreateContainerGaps(true);
	            layout.setHorizontalGroup(
	            		   layout.createSequentialGroup()
	            		      .addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
	            		      .addComponent(okButton)
	            		      .addComponent(cancelButton)
	           	);
	           	layout.setVerticalGroup(
	            		   layout.createSequentialGroup()
	            		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	            		    	   .addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
	            		           .addComponent(okButton)
	            		           .addComponent(cancelButton))
	            );
	            //
	           	filterComboBox.setBackground(Color.WHITE);
	            koepelPane.setBackground(Color.WHITE);
	            omvatOnderPane.setBackground(Color.WHITE);
	            onderPane.setBackground(Color.WHITE);
	            //
	            omvatOnderPane.add(onderPane);
	            koepelPane.add(omvatOnderPane);
			    //
	            dialog.add(koepelPane);
	            dialog.pack();
	            dialog.setLocationByPlatform(true);
	            //dialog.setSize(  (int)xMSet.quickframe.getWidth() , (int)xMSet.quickframe.getWidth() );
	            dialog.setBounds( xMSet.quickframe );
	            dialog.setVisible(true);
	            isOK=true;
	            //
		}
		catch(Exception e) {
			isOK=false;
			do_error("Error opening Quick Edit Dialog" + xMSet.xU.LogStackTrace(e));
		}
	}
	

	//-----------------------------------------------------------------------
	private void perform_resize( JDialog diag)
	//-----------------------------------------------------------------------
	{
		//do_log( 9 , "===>" + diag.getWidth() + " " + diag.getHeight() );
	}
	
	//-----------------------------------------------------------------------
	private void doDropDown()
	//-----------------------------------------------------------------------
	{
	  	cmcProcEnums.QUICKEDITOPTIONS opt = cenums.getQuickEditOption(filterComboBox.getSelectedItem().toString());
	  	if( opt == null ) return;
	  	xMSet.setQuickEditOption(opt);
	  	// todo - refresh
	  	MyScanModel.ReDoSelection();
	  	zetKolomHoogte();
    }

	//-----------------------------------------------------------------------
	private void zetKolomHoogte()
	//-----------------------------------------------------------------------
	{
	  int currentMaxImageWidth = 1;
	  int maxHeigth = 1;
	  try {
		
	    for (int row=0; row<frmComicScannerTable.getRowCount(); row++) {
	        int rowHeight = frmComicScannerTable.getRowHeight();
            
	        for (int column=0; column<frmComicScannerTable.getColumnCount(); column++) {
	            Component comp = frmComicScannerTable.prepareRenderer(frmComicScannerTable.getCellRenderer(row, column), row, column);
	            rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
	            if( column ==  cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.IMAGE)) {  // image
	              currentMaxImageWidth = Math.max( currentMaxImageWidth , comp.getPreferredSize().width);
	            }
	        }
	        rowHeight += 10; // increase a bit
	        frmComicScannerTable.setRowHeight(row, rowHeight);
	        maxHeigth = Math.max( maxHeigth , rowHeight);
	    }
        
	  } catch(ClassCastException e) { }
	  System.out.println("Table  Cell: [MaxWidth=" + currentMaxImageWidth + "] [MaxHeigth=" + maxHeigth + "] [Required=" + maxImageBreedte + "]");

	  // assess wether the imagewidth exceeds the columnwidth
	  /*
	  if( Math.abs((double)(currentMaxImageWidth - maxImageBreedte)) < 20 ) return;
// the following code does not seem to work 
	  TableColumnModel tcm = frmComicScannerTable.getColumnModel();
	  for(int i=0;i<tcm.getColumnCount();i++)
	  {
	    if( i==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.IMAGE) ) {
			 TableColumn tc = tcm.getColumn(i);	
			 tc.setMaxWidth(maxImageBreedte); 
			 tc.setMinWidth(maxImageBreedte);
			 tc.setPreferredWidth(maxImageBreedte);
			 break;
		}
	  }
	  */
	}

	//-----------------------------------------------------------------------
	private void do_close(JDialog diag)
	//-----------------------------------------------------------------------
	{
		if( MyScanModel == null ) return;
		xMSet.setQuickFrameBounds( diag.getX() , diag.getY() , diag.getWidth() , diag.getHeight() );
		if( MyScanModel.ContentHasBeenEdited() == false ) return;
		if( cancelPressed ) {
		  int reply = JOptionPane.showConfirmDialog(null, "There are changes. Do you want to save those", MyScanModel.getCMXUID() , JOptionPane.YES_NO_OPTION);
          if (reply != JOptionPane.YES_OPTION) return;
		}
		// EDIT
		boolean isOK = false;
        if ( gCon != null ) {   // when in EDIT mode		
	     isOK = MyScanModel.propagateChanges(gCon);
        }
        else {  // Post OCR
         isOK = MyScanModel.propagateChangesAfterOCR();	
        }	
	    if( isOK == false ){
	    	JOptionPane.showMessageDialog(null,"Changes could not be stored in archive","Project [" + xMSet.getCurrentArchiveFileName() + "]",JOptionPane.ERROR_MESSAGE);
        }
		do_log(1,"changes have been propagated to [Status=" + isOK + "]");
		
    }

	//-----------------------------------------------------------------------
	private void checkButtonStatus()
	//-----------------------------------------------------------------------
	{
      if( okButton.isEnabled() ) return;
	   if( MyScanModel.ContentHasBeenEdited() == false ) return;
	   okButton.setEnabled(true);
	   cancelButton.setText("Cancel");
	}
	
	//-----------------------------------------------------------------------
	private void swapColorMonochrome()
	//-----------------------------------------------------------------------
	{
		xMSet.setUseMonoChromeInDialogs(checker.isSelected());
		doDropDown();
	}
}
