package monitor;
import generalpurpose.gpInterrupt;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;


public class cmcMonitorDialog {
	
	cmcProcSettings    xMSet = null;
    cmcProcEnums       cenums = null;
    logLiason          logger=null;
    cmcMonitorModel    MyScanModel   = null;
    
    JScrollPane frmComicScrollPane;
	JTable frmComicScannerTable;
	JPanel onderPane;
	JPanel omvatOnderPane;
	JButton stopButton=null;
	JButton cancelButton=null;
	JCheckBox checker=null;

	private int CurrentSelectedRow = -1;
	private int CurrentSelectedCol = -1;
	private boolean hasBeenClosed=false;
	private boolean stophasbeenpressed=false;
	private String blkFolderName=null;
	private long countDownToClose=-1L;
	private long keepMonitorVisibleAfterCompletion = 10000L;
	
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
	public cmcMonitorDialog(JFrame jf , cmcProcSettings iM , logLiason ilog , String iDir)
	//---------------------------------------------------------------------------------
	{
		xMSet = iM;
		logger = ilog;
		cenums = new cmcProcEnums(xMSet);
		blkFolderName = (iDir != null ) ? "[" + xMSet.xU.getFolderOrFileName(iDir) + "]" : "";
		// initialize and populate model
		MyScanModel = new cmcMonitorModel(xMSet,this,logger);
		MyScanModel.ReadDataAndShow();
		MyScanModel.fireTableDataChanged();
		//
		initialize(jf);
	}
	

	  //-----------------------------------------------------------------------
	  	private void initialize(JFrame jf)
	  	//-----------------------------------------------------------------------
	  	{
	  	
	  		try {
	  			    final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.MODELESS);  // final voor de dispose   
	  	            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	  	            dialog.setTitle("Bulk Processor Monitor " + blkFolderName );
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
	                     //System.out.println("dialog moved");
	                   }
	                   public void componentResized(ComponentEvent e)
	                   {
	                     //System.out.println("dialog resized");
	                   }
	                   public void componentShown(ComponentEvent e)
	                   {
	                     //zetKolomHoogte();
	                   }
	                  });
	  	            dialog.addWindowListener(new WindowAdapter() {
	  	                @Override
	  	                public void windowClosed(WindowEvent e) {
	  	                    do_close();
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
	  	            JPanel bovenPanel = new JPanel();
	  	            //bovenPanel.setBackground(Color.WHITE);
	  	            bovenPanel.setLayout(new BoxLayout(bovenPanel, BoxLayout.X_AXIS));
		            //
		            frmComicScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		    		//frmComicScrollPane.setBounds(55, 76, 1151, 398);
		    		bovenPanel.add(frmComicScrollPane);
	                //	    		
		    		frmComicScannerTable = new JTable();
	  	            frmComicScannerTable = new JTable();
	  	            frmComicScannerTable.addFocusListener(new FocusAdapter() {
	  	    			@Override
	  	    			public void focusLost(FocusEvent arg0) { // nodig om bij te houden wat de huidige selectie is
	  	    				CurrentSelectedRow = -1;
	  	    				CurrentSelectedCol = -1;
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
	  	    		  }
	  	    		});
	  	    		
	  	    		
	  	    		//
	  	    		frmComicScannerTable.setModel(MyScanModel);
	  	    		//
	  	    		TableColumnModel tcm = frmComicScannerTable.getColumnModel();
	  	    		
	  	    		// Renderers zetten
	  	    		for(int i=0;i<tcm.getColumnCount();i++)
	  	    		{
	  	    			TableColumn tc = tcm.getColumn(i);
	  	    			int BREEDTE = 100;
	  	    			cmcProcEnums.BULK_MONITOR x = cenums.getBulkMonitorAtIndex(i);
	  	    			if( x != null ) {
	  	    			 switch( x )
	  	    			 {
	  	    			 case COMPLETED  : { BREEDTE = 30; break; }
	  	    			 case FILENAME   : { BREEDTE = 300; break; }
	  	    			 case START_TIME : { BREEDTE = 100; break; }
	  	    			 case ELAPSED    : { BREEDTE = 80; break; }
	  	    			 case COMMENT    : { BREEDTE = 400; break; }
	  	    			 default : { BREEDTE = 100; break; }
	  	    			 }
	  	    			}
	  	    			// Size
	  	    			tc.setMaxWidth(BREEDTE*10);  
	  	    			tc.setMinWidth(BREEDTE);
	  	    			tc.setWidth(BREEDTE);
	  	    		}
	  	    	
	                //	            
	  	    		frmComicScrollPane.setViewportView(frmComicScannerTable);
	  	    	    //
	  	    		// Clicken op tabel header
	  	    		frmComicScannerTable.getTableHeader().addMouseListener(new MouseAdapter() {
	  	    		    @Override
	  	    		    public void mouseClicked(MouseEvent e) {
	  	    		        int col = frmComicScannerTable.columnAtPoint(e.getPoint());
	  	    		        String name = frmComicScannerTable.getColumnName(col);
	  	    		        do_log(1,"Column index selected " + col + " " + name);
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
	  			    stopButton = new JButton("Stop");
	  			    stopButton.setEnabled(false);
	  	            stopButton.addMouseListener(new MouseAdapter() {
	  	    			@Override
	  	    			public void mouseClicked(MouseEvent arg0) {
	  	    			  //MyScanModel.propagateChanges();
	  	    			  //dialog.dispose();  -- niet closen
	  	    			  if( requestInterrupt() ) {
	  	    				  stophasbeenpressed=true;
	  	    				  stopButton.setEnabled(false);
	  	    			  }
	  	    			}
	  	    		});
	  	            stopButton.setFont(font);
	  	            //
	  			    cancelButton = new JButton("Ok");
	  			    /*   ?? changed to add action
	  	            cancelButton.addMouseListener(new MouseAdapter() {
	  	    			@Override
	  	    			public void mouseClicked(MouseEvent arg0) {
	  	    			  do_error("Clicked");
	  	    			  dialog.dispose();
	  	    			}
	  	    		});
	  	    		*/
	  	            cancelButton.addActionListener(new ActionListener()
	  	            {
	  	              public void actionPerformed(ActionEvent e)
	  	              {
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
	  	            		      //.addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
	  	            		      .addComponent(stopButton)
	  	            		      .addComponent(cancelButton)
	  	           	);
	  	           	layout.setVerticalGroup(
	  	            		   layout.createSequentialGroup()
	  	            		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
	  	            		    	   //.addComponent(filterComboBox,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
	  	            		           .addComponent(stopButton)
	  	            		           .addComponent(cancelButton))
	  	            );
	  	            
	  	            //
	  	           	//filterComboBox.setBackground(Color.WHITE);
	  	            koepelPane.setBackground(Color.WHITE);
	  	            omvatOnderPane.setBackground(Color.WHITE);
	  	            onderPane.setBackground(Color.WHITE);
	  	            //
	  	            omvatOnderPane.add(onderPane);
	  	            koepelPane.add(omvatOnderPane);
	  			    //
	  	            dialog.add(koepelPane);
	  	            dialog.pack();
	  	            
	  	            // timer
	  	  		    ActionListener timerListener = new ActionListener(){
	  			      public void actionPerformed(ActionEvent event) {
	  			    	  
	  			    	  // timer
	  			    	  if( countDownToClose > 0 ) {
	  			    	    if ( (System.currentTimeMillis() - countDownToClose) > keepMonitorVisibleAfterCompletion ) {
	  			    	    	countDownToClose = -1L;
	  			    	    	forceClose();
	  			    	    }
	  			    	  }
	  			    	  
				      }
	  			    };
	  			    Timer displayTimer = new Timer(200, timerListener);
	  			    displayTimer.start();

	  			    //
	  	            dialog.setLocationByPlatform(true);
	  	            dialog.setSize( 800 , 400 );
	   	            dialog.setVisible(true);
	  	            //
	  		}
	  		catch(Exception e) {
	  			do_error("Error openining Bulk Monitor Dialog" + xMSet.xU.LogStackTrace(e));
	  		}
	  	}

		//---------------------------------------------------------------------------------
	  	private void do_close()
		//---------------------------------------------------------------------------------
	  	{
	  	    hasBeenClosed=true;	
	  	}
	    //-----------------------------------------------------------------------
		private boolean requestInterrupt()
		//-----------------------------------------------------------------------
		{
			gpInterrupt irq = new gpInterrupt( xMSet.getInterruptFileName() );
			boolean ib = irq.requestInterrupt();
	        irq = null;
	        if( ib == false ) do_error("Could not set interrupt");
	        return ib;
		}
	  	// these are functions which are called by the Main GUI
		//---------------------------------------------------------------------------------
	  	public boolean upsertMonitorItemLine(cmcMonitorItem x )
		//---------------------------------------------------------------------------------
		{
			return this.MyScanModel.upsertMonitorItemLine( x );
		}
	    //---------------------------------------------------------------------------------
	  	public boolean execFireTableDataChanged()
		//---------------------------------------------------------------------------------
	  	{
	  		boolean retFinal = false;
	  		MyScanModel.execFireTableDataChanged();
	  		boolean ib = ( MyScanModel.getNumberOfUncompletedItems() == 0 ) ? false : true;
	  		retFinal = !ib;
	  		if( stophasbeenpressed ) ib = false;
			stopButton.setEnabled(ib);
			return retFinal;
	  	}
		//---------------------------------------------------------------------------------
	  	public boolean endTransmissionAndGetFinal()
		//---------------------------------------------------------------------------------
	  	{
	  		return execFireTableDataChanged();
	  	}
	    //---------------------------------------------------------------------------------
	  	public boolean hasBeenClosed()
		//---------------------------------------------------------------------------------
		{
			return hasBeenClosed;
		}
	  	//---------------------------------------------------------------------------------
	  	public void forceClose()
	  	//---------------------------------------------------------------------------------
	  	{
	  	    cancelButton.doClick();
      	}
	  	//---------------------------------------------------------------------------------
	  	public void requestDelayedClose()
	  	//---------------------------------------------------------------------------------
	  	{
          countDownToClose=System.currentTimeMillis();	  		
	  	}
}
