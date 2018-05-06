import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;

import javax.swing.border.TitledBorder;
import javax.swing.JMenuBar;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerNumberModel;

import java.io.File;
import java.util.ArrayList;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBox;


import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import logger.logLiason;
import logger.nioServerDaemon;
import monitor.cmcMonitorController;
import ocr.cmcOCRController;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcController;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSemaphore;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.cmcProcTimingInfo;
import cbrTekStraktorModel.comicPage;
import dao.cmcArchiveDAO;
import dao.cmcBookMetaDataDAO;
import drawing.cmcGraphController;
import generalImagePurpose.gpMakeScreenShot;
import generalImagePurpose.gpRotateImageFile;
import generalpurpose.gpFileChooser;
import generalpurpose.gpInterrupt;
import generalpurpose.gpUnZipFileList;
import imageProcessing.cmcProcColorHistogram;

import bubbleProcessing.cmcBubbleMaker;



public class comicImageProcessorMainGUI {
	
	private JFrame frame;
    private JPanel imgPanel;
    private JScrollPane scrollPane;
    private JButton btnImage;
    private JButton btnExtract;
    private JButton btnEdit;
    private JButton btnOCR;
    private JButton btnTranslate;
    private JButton btnReport;
    private JButton btnStats;
    private JButton saveButton;
    private JButton redoButton;
    private JButton stopButton;
    private JButton infoButton;
    private JButton optionButton;
    private JButton klok;
    //
    private JSeparator saveSepa;
    private JSeparator redoSepa;
    private JSeparator stopSepa;
    private JSeparator infoSepa;
    private JSeparator optionSepa;
    //  
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel histoPanel;
    private JSpinner spinner;
    private JComboBox imageProcessingComboBox;
    private JProgressBar pbar;
    private JCheckBox scanCheckBox;
    private JTextField statusMessage;
    private JMenuBar menuBar;
    //
    private boolean requestHistogram=false;
    private boolean requestImageProcessing=false;
    private boolean requestCleanUp=false;
    private boolean robotMode=false;  
    private boolean scanMode=false;
    private boolean runCompleted=false;
    private int     tenSeconds=0;
    private int     tenSecondsMAX=5*10;
    private Point   currentPixel = new Point(-1,-1);
    private Point   dragStartPixel = new Point(-1,-1);
    private boolean HistoUncroppedDumped=true;
    private boolean HistoGrayScaleDumped=true;
    //
    private int LEFTPANELWIDTH=150;
    private int HISTOPANELHEIGHT=300;
    private int imgDisplayRatio=100;
    private int prevImgDisplayRatio=imgDisplayRatio;
    private int COMPHOOGTE=22;
    private int BUTTONHOOGTE = COMPHOOGTE + 3;
    private boolean drawmode=false;
    private String imageFilterSelectie=null;
    private int DelayedCheck=100;
    //
    static cmcProcSettings xMSet=null;
    static comicPage cPage=null;
    static cmcProcController ctrl=null;
    static cmcProcSemaphore cSema =null;
    static cmcGraphController gCon=null;
    static cmcMenuManager menuMan=null;
    static cmcPopupMenuManager popMan=null;
    static cmcProcTimingInfo timeStat=null;
    static logLiason logger=null;
    static logLiason logger2nd = null;
    static nioServerDaemon logDaemon=null;
    static cmcMonitorController moniControl=null;
    //
    static gpMakeScreenShot srsh = null;  // screenshot component
    private long mousedowntimer = 0L;
    private long mousefixedtimer = 0L;
    private int popupstatus=-1;
    private boolean trackMouseDownPeriod=false;
    private boolean trackMouseMoved=false;
    private boolean trackMouseDragged=false;
    static private Font xfont = null;
    private cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE appState = cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE; 
    private cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE prevAppState = cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE; // something different

    private String lastImageFileName=null;
    
    static Color CORNFLOWER = new Color(100, 149, 237);
    static Color BUTTONHOVER = new Color(176, 196, 222);  
    static String BulkFileName = null;
           
    // purge item
    class PurgeItem {
    	String Fname=null;
    	long tstmp=0L;
    	PurgeItem(String s)
    	{
    		Fname = s;
    		tstmp = System.currentTimeMillis();
    	}
    }
    ArrayList<PurgeItem> verwijderLijst = new ArrayList<PurgeItem>();
    
    private String[] arFileStack = new String[5];
    
    
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
    
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		// bestaat de directory, enz
		xMSet = new cmcProcSettings(args);
		if( xMSet.isActive() == false ) {
			System.err.println("Error whilst initializing");
			//System.exit(1);
		}
		else {
		 //
		 logDaemon = new nioServerDaemon(15342 , xMSet.getLogLevel() , xMSet.getLogFileName() , xMSet.getErrFileName());
		 if( logDaemon.isOK() == false ) {
			System.err.println("Could not start logger Daemaon");
			logDaemon=null;
		 }
		 else {
		  logDaemon.start();
		  //
		  logger = new logLiason(1L); // mainlogger
		  logger2nd = new logLiason(2L);  // swingworker and dependent processes logger
	      xMSet.setLogger(logger);
		 }
		}
		//
	   xfont = xMSet.getPreferredFont();
		//
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try
					{
					  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					  UIDefaults defaults = UIManager.getLookAndFeelDefaults();
					  if (defaults.get("Table.alternateRowColor") == null)
					      defaults.put("Table.alternateRowColor", cmcProcConstants.DEFAULT_LIGHT_GRAY);
					}
					catch(Exception e){}
					// controller voor de graphical editor
					gCon = new cmcGraphController(xMSet,logger);
					//
					cSema = new cmcProcSemaphore();
					cPage = new comicPage(xMSet,logger2nd);
					ctrl = new cmcProcController(cPage,cSema,xMSet,gCon,logger2nd);
					timeStat = new cmcProcTimingInfo(xMSet,logger2nd); // could be logger also
					ctrl.execute();  // start de thread
					//
					comicImageProcessorMainGUI window = new comicImageProcessorMainGUI();
					window.frame.setVisible(true);
					//
					moniControl = new cmcMonitorController(xMSet,logger);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public comicImageProcessorMainGUI() {
		for(int i=0;i<arFileStack.length;i++) arFileStack[i] = xMSet.getFileStack(i);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				doe_resize();
			}
			 
		});
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	do_shutdown();
	        	/*
	        	Rectangle r = frame.getBounds();
            	xMSet.writePropertiesFile(r.x,r.y,r.width,r.height, arFileStack );
	            System.exit(0);
	            */
	        }
	    });
		frame.setBounds(xMSet.startX, xMSet.startY, xMSet.startW, xMSet.startH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setFont(xfont);
		//
		//
		menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 800, COMPHOOGTE + 5);
	    //menuBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
	    frame.getContentPane().add(menuBar);
	    //
	    cmcMenuShared menuShared = new cmcMenuShared(this);
	    //
	    menuMan = new cmcMenuManager(menuShared);
	    menuMan.makeMenus(menuBar,xfont,LEFTPANELWIDTH,arFileStack);
	    //
	    popMan = new cmcPopupMenuManager(menuShared,logger);
	    popMan.makeMenus(frame,xfont);
	    //
		menuBar.add(saveSepa=new JSeparator(JSeparator.VERTICAL));
		
		//
		saveButton = new JButton("Save");
		saveButton.setBorderPainted(false);
		saveButton.setFocusPainted(false);
		saveButton.setContentAreaFilled(false);
		saveButton.setOpaque(true);
		saveButton.setFont(xfont);
		saveButton.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		saveButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doSave();
			}
		});
		saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        saveButton.setBackground(BUTTONHOVER);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        saveButton.setBackground(UIManager.getColor("control"));
		    }
		});
		menuBar.add(saveButton);
		
		//
		menuBar.add(stopSepa=new JSeparator(JSeparator.VERTICAL));
		//
		stopButton = new JButton("Stop");
		stopButton.setBorderPainted(false);
		stopButton.setFocusPainted(false);
		stopButton.setContentAreaFilled(false);
		stopButton.setOpaque(true);
		stopButton.setFont(xfont);
		stopButton.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		stopButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				stophasbeenpressed();
			}
		});
		stopButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        stopButton.setBackground(BUTTONHOVER);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        stopButton.setBackground(UIManager.getColor("control"));
		    }
		});
		menuBar.add(stopButton);
		
		//
		menuBar.add(redoSepa=new JSeparator(JSeparator.VERTICAL));
		//
		redoButton = new JButton("Refresh");
		redoButton.setBorderPainted(false);
		redoButton.setFocusPainted(false);
		redoButton.setContentAreaFilled(false);
		redoButton.setOpaque(true);
		redoButton.setFont(xfont);
		redoButton.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		redoButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickRefreshButton();
			}
		});
		redoButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        redoButton.setBackground(BUTTONHOVER);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        redoButton.setBackground(UIManager.getColor("control"));
		    }
		});
		menuBar.add(redoButton);
		
		//
		menuBar.add(infoSepa=new JSeparator(JSeparator.VERTICAL));
		//
		infoButton = new JButton("Info");
		infoButton.setBorderPainted(false);
		infoButton.setFocusPainted(false);
		infoButton.setContentAreaFilled(false);
		infoButton.setOpaque(true);
		infoButton.setFont(xfont);
		infoButton.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		infoButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickInfoButtonParam( arg0.getX() , arg0.getY() );
			}
		});
		infoButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        infoButton.setBackground(BUTTONHOVER);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        infoButton.setBackground(UIManager.getColor("control"));
		    }
		});
		menuBar.add(infoButton);
		
		//
		menuBar.add(optionSepa=new JSeparator(JSeparator.VERTICAL));
		//
		optionButton = new JButton("Option");
		optionButton.setBorderPainted(false);
		optionButton.setFocusPainted(false);
		optionButton.setContentAreaFilled(false);
		optionButton.setOpaque(true);
		optionButton.setFont(xfont);
		optionButton.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		optionButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickOptionsButton();
			}
		});
		optionButton.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        optionButton.setBackground(BUTTONHOVER);
		    }

		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        optionButton.setBackground(UIManager.getColor("control"));
		    }
		});
		menuBar.add(optionButton);
		
		//
		statusMessage = new JTextField(xMSet.getApplicDesc());
		statusMessage.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		statusMessage.setOpaque(true);
		statusMessage.setEnabled(false);
		statusMessage.setFont(xfont);
		menuBar.add(statusMessage);
		
		//menuBar.add(new JSeparator(JSeparator.VERTICAL));
		klok = new JButton("..");
	    klok.setBorderPainted(false);
		klok.setFocusPainted(false);
		klok.setContentAreaFilled(false);
		klok.setOpaque(true);
		klok.setFont(xfont);
		klok.setPreferredSize(new Dimension(LEFTPANELWIDTH/2,menuBar.getHeight()));
		menuBar.add(klok);
		
		//
		leftPanel = new JPanel();
		leftPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		leftPanel.setBounds(0, menuBar.getHeight() + 1, LEFTPANELWIDTH, 576);
		frame.getContentPane().add(leftPanel);
		leftPanel.setLayout(null);
		//
		//
		btnImage = new JButton("Image");
		btnImage.setFont(xfont);
		btnImage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickImageButton();
			}
		});
		btnImage.setBounds(10, 10 , LEFTPANELWIDTH - 20 , BUTTONHOOGTE);
		leftPanel.add(btnImage);
		
		//
		btnExtract = new JButton("Extract Text");
		btnExtract.setFont(xfont);
		btnExtract.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickExtractButton();
			}
		});
		btnExtract.setBounds(10, 10 + (BUTTONHOOGTE + 3)*1, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnExtract);
		//
		//
		btnEdit = new JButton("Edit");
		btnEdit.setFont(xfont);
		btnEdit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				btnEdit.setBackground( btnImage.getBackground() );  // o die wijze steeds herzettenachtergornd
				doClickEditButton();
			}
		});
		btnEdit.setBounds(10, 10 + (BUTTONHOOGTE + 3)*2, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnEdit);
		//
		//
		btnOCR = new JButton("OCR");
		btnOCR.setFont(xfont);
		btnOCR.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickOCRButton();
			}
		});
		btnOCR.setBounds(10, 10 + (BUTTONHOOGTE + 3)*3, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnOCR);
		//
        //
		btnTranslate = new JButton("Translate");
		btnTranslate.setFont(xfont);
		btnTranslate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				doClickTranslateButton();
			}
		});
		btnTranslate.setBounds(10, 10 + (BUTTONHOOGTE + 3)*4, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnTranslate);
		//
		//
		btnReport = new JButton("Report");
		btnReport.setFont(xfont);
		btnReport.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
			   doClickReportButton();
			}
		});
		btnReport.setBounds(10, 10 + (BUTTONHOOGTE + 3)*5, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnReport);
		//
		//
		btnStats = new JButton("Reinject");
		btnStats.setFont(xfont);
		btnStats.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				//doClickStatisticsButton();
				doClickReInjectButton();
			}
		});
		btnStats.setBounds(10, 10 + (BUTTONHOOGTE + 3)*6, LEFTPANELWIDTH-20 , BUTTONHOOGTE);
		leftPanel.add(btnStats);
		//
		//
		histoPanel = new histogramPanel();
		histoPanel.setBounds(5, frame.getHeight() - HISTOPANELHEIGHT , LEFTPANELWIDTH - 10 , HISTOPANELHEIGHT);
		leftPanel.add(histoPanel);
		//
		imageProcessingComboBox = new JComboBox();
		imageProcessingComboBox.setFont(xfont);
		imageProcessingComboBox.setModel(new DefaultComboBoxModel(
				new String[]{"Original", "Info" , "Bleached" , "Grayscale" , 
						     "Monochrome (Otsu)" , "Monochrome (Niblak)" , "Monochrome (Sauvola)" , 
						     "Blueprint" , "Mainframe", "Invert" , "Histogram equalisation" , 
						     "Convolution Glow" , "Convolution Gaussian" , "Convolution Sharpen" , 
						     "Convolution Edge" , "Sobel" , "Sobel Grayscale" ,"Gradient Narrow" , "Gradient Wide"}));
		imageProcessingComboBox.setBounds(histoPanel.getX() , histoPanel.getY() - 21, histoPanel.getWidth(), 20);
		// Triggers always
		imageProcessingComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              	doClickImageFilterPrepareAndRun(); 
            }
        });
		leftPanel.add(imageProcessingComboBox);
		imageProcessingComboBox.setVisible(false); // niet tonen
		//
		pbar = new JProgressBar();
		pbar.setBounds(imageProcessingComboBox.getX(), imageProcessingComboBox.getY()-21, imageProcessingComboBox.getWidth(), 20);
		pbar.setVisible(false);
		leftPanel.add(pbar);
		
        //
		rightPanel = new JPanel();
		rightPanel.setBounds(LEFTPANELWIDTH+1, leftPanel.getY() , 400, 600);
		rightPanel.setLayout(new BorderLayout(0, 0));
		frame.getContentPane().add(rightPanel);
		
		//
		imgPanel = new picturePanel();
		imgPanel.setBounds(rightPanel.getWidth(), rightPanel.getY() , 600,550);
		frame.getContentPane().add(imgPanel);
		
		//
		scrollPane = new JScrollPane();
		scrollPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				 trackMouseDownPeriod=false;
				 int yoffset = scrollPane.getVerticalScrollBar().getValue();
				 int xoffset = scrollPane.getHorizontalScrollBar().getValue();
				 int x = arg0.getPoint().x + xoffset;
				 int y = arg0.getPoint().y + yoffset;
				 //
				 if (arg0.getClickCount() == 2 ) {  // dubbel
					 if( drawmode ) {
						   int idx = gCon.getSelectedParagraph(x, y);
						   if( idx >= 0) doeParagraphDialog(idx);
					 }
					 else {
					   askToStartEdit();
					 }
				 }
				 else { if( SwingUtilities.isRightMouseButton(arg0) ) {   // RECHTS
					do_rightclickX(arg0.getComponent(),arg0.getX(),arg0.getY());
				 }
				  else {
				    doe_pixelinfo( x , y );
				  }
				}
				//
				currentPixel.x = x;
			    currentPixel.y = y;
			}
			public void mousePressed(MouseEvent e) {
			    if (e.getButton() == MouseEvent.BUTTON1) {
			    	 int yoffset = scrollPane.getVerticalScrollBar().getValue();
					 int xoffset = scrollPane.getHorizontalScrollBar().getValue();
					 currentPixel.x = e.getPoint().x + xoffset;
					 currentPixel.y = e.getPoint().y + yoffset;
			    	 mousedowntimer = System.currentTimeMillis();
			    	 trackMouseDownPeriod=true;
			    	 mousefixedtimer=0L;
			    }
			}
			public void mouseReleased(MouseEvent e) {
			    if (e.getButton() == MouseEvent.BUTTON1) {
			    	trackMouseDownPeriod=false;
			    	if( dragStartPixel.x != -1 ) {
			    	  doEndDrag();
			    	  dragStartPixel.x=-1;
			    	  dragStartPixel.y=-1;
			    	  imgPanel.repaint();
			    	}
			    }
			}
		});
		scrollPane.addMouseMotionListener(new MouseMotionListener(){
			
			//  ook nog iets doen mbt. rechtclick draggen ??
		    @Override public void mouseDragged(    MouseEvent e){
		    	 int yoffset = scrollPane.getVerticalScrollBar().getValue();
				 int xoffset = scrollPane.getHorizontalScrollBar().getValue();
				 currentPixel.x = e.getPoint().x + xoffset;
				 currentPixel.y = e.getPoint().y + yoffset;
				 if( dragStartPixel.x == -1 ) {
					 dragStartPixel.x = currentPixel.x;
					 dragStartPixel.y = currentPixel.y;
				 }
				 trackMouseDragged=true;
		    }
		    @Override public void mouseMoved(    MouseEvent e){
		    	 int yoffset = scrollPane.getVerticalScrollBar().getValue();
				 int xoffset = scrollPane.getHorizontalScrollBar().getValue();
				 currentPixel.x = e.getPoint().x + xoffset;
				 currentPixel.y = e.getPoint().y + yoffset;
				 trackMouseMoved=true;
		    }
	    });
		scrollPane.setViewportView(imgPanel);
		rightPanel.add(BorderLayout.CENTER,scrollPane);
		
		//
		spinner = new JSpinner();
		spinner.setFont(xfont);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				imgDisplayRatio = (Integer)spinner.getValue();
				//System.out.println(""+imgDisplayRatio);
			}
		});
		spinner.setModel(new SpinnerNumberModel(imgDisplayRatio, 10, 200, 1));
		//spinner.setBounds(179, 0, 58, menuBar.getHeight());
		//frame.getContentPane().add(spinner);
		spinner.setBounds(btnStats.getX() , btnStats.getY() + btnStats.getHeight() + 5 , btnStats.getWidth(), btnStats.getHeight());
		leftPanel.add(spinner);
	
		
		//
		scanCheckBox = new JCheckBox("Bulk Processor");
		scanCheckBox.setFont(xfont);
		scanCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if( scanCheckBox.isSelected() ) {
					btnExtract.setText("Bulk Extract");
					btnOCR.setText("Bulk OCR"); 
				}
				else {
					btnExtract.setText("Extract Text");
					btnOCR.setText("OCR"); 
				}
			}
		});
		//scanCheckBox.setBounds(250, 0, 150, menuBar.getHeight());
		//frame.getContentPane().add(scanCheckBox);
		//scanCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
		scanCheckBox.setBounds(btnStats.getX() , spinner.getY() + btnStats.getHeight() + 5 , btnStats.getWidth(), btnStats.getHeight());
		leftPanel.add(scanCheckBox);
		
		// Timer
		ActionListener timerListener = new ActionListener(){
			  public void actionPerformed(ActionEvent event){
				 // toon histogram
			     if( requestHistogram ) {
			    	     requestHistogram = false;
			    	  	 histoPanel.repaint();
			    	  	 // Wegschrijven van de histogrammen naar file
						 if( HistoUncroppedDumped == false ) {
						    HistoUncroppedDumped=true;
						    writeHistogramToFile("ORIGINAL");
						    dumpBoxDiagram();
						    writePeaks();
						 }
						 if( HistoGrayScaleDumped == false ) {
							HistoGrayScaleDumped=true;
							writeHistogramToFile("GRAY");
						 }
			     }
			     // mouse down in image for 500 msec
			     if( trackMouseDownPeriod ) {
			    	  long elapsed = System.currentTimeMillis() - mousedowntimer;
					  if( elapsed > 300L ) {
						 trackMouseDownPeriod = false;
					     if( gCon.hasParagraphBeenSelected(currentPixel.x,currentPixel.y) ) {
					    	 imgPanel.repaint();
					    	 boolean ib = yesNoDialog("Do you want to remove this object" , "");
					    	 gCon.hasParagraphBeenSelected(currentPixel.x,currentPixel.y); // rode rand verwijderen	 
					         if( ib ) {
					        	 ib = gCon.removeViaXY(currentPixel.x,currentPixel.y);
					        	 imgPanel.repaint();
					         }
					     }
					  }
			     }
			     // mouse stable in edit mode
			     if( drawmode ) {
			    	  long elapsed = System.currentTimeMillis() - mousefixedtimer;
					  if( (elapsed > 500L) &&  (popupstatus==0) ) {
						  popupstatus = 1;
						  mousefixedtimer = System.currentTimeMillis();
						  doe_edit_popup(true);
					  }
					  else
					  if( (elapsed > 5000L) &&  (popupstatus==1) ) {
						  popupstatus = 2;
						  doe_edit_popup(false);
					  }	  
			     }
			     if( trackMouseMoved ) {
			   	      if( gCon.wisker(currentPixel.x,currentPixel.y) ) imgPanel.repaint();
					  trackMouseMoved = false;
					  mousefixedtimer = 0L;
					  popupstatus=0;
					  doe_edit_popup(false);
			     }
			     if( trackMouseDragged ) {
			   	      if( gCon.dragger(dragStartPixel.x , dragStartPixel.y , currentPixel.x , currentPixel.y ) ) imgPanel.repaint();
					  trackMouseDragged = false;
					  mousefixedtimer = 0L;
			     }
			     //
			     DelayedCheck++;
			     if( DelayedCheck == 6 ) {
			    	if( gCon.getNumberOfChanges() > 0 )	btnEdit.setBackground(Color.GREEN);
			     }
			     
			     // image processing request
			     if( requestImageProcessing ) {
			    	 //
			    	 requestImageProcessing=false;
			    	 //String keuze = imageProcessingComboBox.getSelectedItem().toString();
			    	 String keuze = imageFilterSelectie;
			    	 if (keuze == null ) keuze ="";
			    	 if( keuze.toUpperCase().trim().indexOf("ORIGINAL")>=0) {
			    		 doe_load(xMSet.getOrigImageLongFileName());
			    	 } 	 
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("INFO")>=0) {
			    		 doe_info();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BINARIZE_OTSU")>=0) {
			    		 process_otsu();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("MONOCHROME_OTSU")>=0) {
				    		 process_otsu();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BINARIZE_BLUE_OTSU")>=0) {
			    		 process_otsu();
			    	 }
			    	 else
				   	 if( keuze.toUpperCase().trim().indexOf("MONOCHROME_BLUE_OTSU")>=0) {
				    		 process_otsu();
				   	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BINARIZE_NIBLAK")>=0) {
			    		 process_niblak();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("MONOCHROME_NIBLAK")>=0) {
				    		 process_niblak();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BINARIZE_SAUVOLA")>=0) {
			    		 process_sauvola();
			    	 }
			    	 else
		    		 if( keuze.toUpperCase().trim().indexOf("MONOCHROME_SAUVOLA")>=0) {
				    		 process_sauvola();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("INVERT")>=0) {
			    		 process_invert();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BLEACHED")>=0) {
			    		 process_bleach();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("HISTOGRAM_EQUALISATION")>=0) {
			    		 process_histogramEqualization();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("MAINFRAME")>=0) {
			    		 process_mainframe();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("BLUEPRINT")>=0) {
			    		 process_blueprint();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("CONVOLUTION_EDGE")>=0) {
			    		 process_convolution("EDGE");
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("CONVOLUTION_GLOW")>=0) {
			    		 process_convolution("GLOW");
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("CONVOLUTION_SHARPEN")>=0) {
			    		 process_convolution("SHARPEN");
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("CONVOLUTION_GAUSSIAN")>=0) {
			    		 process_convolution("GAUSSIAN");
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("SOBEL")>=0) {
			    		 if( keuze.toUpperCase().trim().indexOf("GRAYSCALE")>=0) {
				    		 process_sobelgrayscale();
				    	 }	 
			    		 else process_sobelcolor();
			    	 }
			    	 else
			    	 if( keuze.toUpperCase().trim().indexOf("GRADIENT")>=0) {
			    		 if( keuze.toUpperCase().trim().indexOf("WIDE")>=0) {
				    		 process_gradientwide();
				    	 }	 
			    		 else process_gradientnarrow();
			    	 }
			    	 else
				   	 if( keuze.toUpperCase().trim().indexOf("GRAYSCALE")>=0) {
				    		 process_grayscale();
				   	 }
			    	 else {
			    		 do_error("Unknown imagefilter request [" + keuze + "]");
			    	 }
			     }
			     //
			     if( imgDisplayRatio != prevImgDisplayRatio ) {
			    	 prevImgDisplayRatio = imgDisplayRatio;
			    	 requestCleanUp=true;
			     }
			     //  het is voldoende om uitgesteld een resize uit te voeren
			     if( requestCleanUp ) {
			    	    requestCleanUp=false;
			    	    doe_resize();
			     }
			     // kijk of de controller klaar is met iets
			     // scannen van een dir
			     // Do not forget : getsema clears the semaphore
				 cmcProcSemaphore.TaskType sema = cSema.getSemaphore();
				 if( sema == cmcProcSemaphore.TaskType.OVERTHECLIFF) {
					 popMessage("Unrecoverable error. Please close the application");
				 }
			     if( (scanMode==true) && (runCompleted==true)) {
			    	 if( sema == cmcProcSemaphore.TaskType.DO_NOTHING) {
			    		 BulkFileName = xMSet.popScanListItem();  // also set start
			    		 if( BulkFileName != null ) {
			    			 // fire the monitor
			    			 syncMonitor();
				    		 // this could be BULK OCR or BULK Extract
			    			 // distinction is easy - if ZIP then OCR else Extract
			    			 String sfx = xMSet.xU.GetSuffix(BulkFileName);
			    			 if( sfx == null ) sfx = "???";
			    			 if( sfx.compareToIgnoreCase("ZIP") == 0) {
			    			    do_log(5,"Bulk OCR on [" + BulkFileName + "]");
			    			    xMSet.setCurrentArchiveFileName(BulkFileName);
			    			    requestTask( cmcProcSemaphore.TaskType.PREPARE_OCR ); 
			    			    sema = cmcProcSemaphore.TaskType.DO_NOTHING;
			    			    runCompleted=false;  // will ensure that we wiat until the swingworker performs the OCR preprocess
			    			 }
			    			 else { // extract
			    			    do_log(5,"Scanning [" + BulkFileName + "]");
			    			    doe_load(BulkFileName);
			    			    robotMode=true;
			    			 }
			    		 }
			    		 else {
			    			 do_log(5,"No more items on scanlist");
			    			 scanCheckBox.setSelected(false);
			    			 scanMode = false;
			    			 if( moniControl != null ) moniControl.requestDelayedClose();
			    		 }
			    		
			    	 } // else negeer
			    	 //else { do_log(5,"->" + sema ); }
			     }
			     
			     if( sema != cmcProcSemaphore.TaskType.DO_NOTHING) {
			    	 imgPanel.repaint();
			    	 requestHistogram=true;
			     	 pbar.setVisible(false);
			     	 // ROBOT mode
			     	 if( robotMode ) {
			     		 switch (sema )
			     		 {
			     		 case SHOUT         : { do_shout(); break; }
			     		 case DO_LOAD_IMAGE : { resetInterrupt(); if( scanMode == false ) doe_dialoog(); else doe_preprocess(); break; }
			     		 case DO_DIALOG     : { if ( toon_dialoog() == false ) {runCompleted=true; enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE); } else doe_preprocess(); break; }
			     		 case DO_PREPROCESS : { doe_dialoog(); doe_grijs(); break; }
			     		 case DO_GRAYSCALE  : { doe_binarize(); break; }
			     		 case DO_BINARIZE   : { doe_cc(); break; }
			     		 case DO_CONNECTEDCOMPONENT : { runCompleted=true; enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EXTRACTED); lastImageFileName=null; timeStat.setEndToEnd(ctrl.getActualEndToEndTime()); syncMonitorEnd(); break; }
			     		 case PREPARE_OCR   : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.OCR_BUSY); doe_show_ocr_file(); break; }
			     		 case SHOW_OCR_FILE : { doe_tesseract(); break; }
			     		 case RUN_TESSERACT : { process_ocr_result(); runCompleted=true; syncMonitorEnd(); enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE); break; } 
			     		 default : { do_error("Wrong status in robot mode [" + sema + "]"); break; }
			     		 }
			     	 }
			     	 else {
			     		// non robotmode
			     		switch (sema )
			     		 {
			     		 case SHOUT              : { do_shout(); break; }
			     		 case DO_LOAD_IMAGE      : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); toonStatusInfo(cPage.getShortImageInfo()); break; }
			     		 case PROCESS_GRAYSCALE  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_BLACKWHITE : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_NIBLAK     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_SAUVOLA    : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_INVERT     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_STATS      : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_BLEACH     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_EQUAL      : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_MAINFRAME     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_BLUEPRINT     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_CONVOLUTION_EDGE     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_CONVOLUTION_GLOW     : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_CONVOLUTION_SHARPEN  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_CONVOLUTION_GAUSSIAN : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_SOBELGRAYSCALE  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_SOBELCOLOR  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_GRADIENT_NARROW  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
			     		 case PROCESS_GRADIENT_WIDE  : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGE); break; }
					     case DO_EDITOR_IMAGE    : { doe_fastload( gCon.getWorkImageFile()); enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDIT); break; }
			     		 case DO_FAST_LOAD_IMAGE : { toonStatusInfo(gCon.getShortPageInfo()); disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDIT); 
			     		                             //btnEdit.setEnabled(true);
			     		                             setControlStatusW("EDIT",true);
			     		                             break; }
			     		 case DO_FLUSH_CHANGES   : { enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE);
			 			                             cPage.cmcImg.clearImg();
						                             toonStatusInfo("");
					                                 imgPanel.repaint();
					                                 break; }
			     		 //case PREPARE_OCR : break;
			     		 case EXTRACT_ALL_TEXT : { doEndOfTextExport(); break; }
			     		 case IMPORT_ALL_TEXT  : { do_error("einde import"); break; }
			     		 default : { do_error("Wrong status in NON robot mode [" + sema + "]"); break; }
			     		 }
			     	     	 
			     	 }
			     	 requestCleanUp=true;
			     }
			     // forceer de pbar -- helpt niet
			     /*
			     if ( pbar.isVisible() == false ) {
			    	 if( (appState == ApplicState.IMAGELOADING) || (appState == ApplicState.EDITLOADING) ) {
			    		 pbar.setVisible(true);
			    	 }
			     }
			     */
			     // if pbar visible then display progress
			     if( pbar.isVisible() == true ) {
			    	 String sVal = timeStat.getRunTiming();
                     pbar.setValue(maakIval(sVal,pbar.getValue())); // extracts percentage
                     pbar.setStringPainted(true);
                     pbar.setString(sVal);
			     }
			     
			     // tien seconden
			     tenSeconds++;
			     if( tenSeconds > tenSecondsMAX ) {
			    	 tenSeconds=0;
			    	 processVerwijderLijst(false);
			     }
			     if( (tenSeconds % 5)==0) {
			    	 String sPat = ((tenSeconds % 10)==0) ? "HH:mm" : "HH mm";
			    	 klok.setText(xMSet.xU.prntDateTime(System.currentTimeMillis(),sPat));
			     }
			     // knoppen
			     if( appState != prevAppState ) {
			    	 prevAppState = appState;
			    	 setQuickButtons(appState);
			     }
			     
			  }
		};
		Timer displayTimer = new Timer(200, timerListener);
		displayTimer.start();
		//
		loadVerwijderLijst();
		//
		//doe_load("c:\\temp\\cmcProc\\cover.jpg");
		//
		setTitle();
	    toonStatusInfo(xMSet.getSystemInfo());
        //
        if( xMSet.isActive() == false ) {
        	boolean ib = yesNoDialog(xMSet.getInitError() , "Something went wrong during the initialisation. Would you like to fix these anomalies?");
        	disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IDLE);
        	// Fixit
        	if( ib ) ib = xMSet.fixFoldersAndFiles(); 
        	if( ib == false ) {
        		popMessage("Could not initialize folder structure");
        		System.exit(1);
        	}
        	else {
        	  xMSet.flushProjectConfig(true);
        	  popMessage("Folder structure has been initialized.  Project is [" + xMSet.getRootDir() + "]. Please restart application.");
        	}
       	}
        else {  // normal start
        	resetInterrupt();
        	do_log(1,"Open for business");
        }
        
	}
	
	//-----------------------------------------------------------------------
	private int maakIval(String sVal, int curval)
	//-----------------------------------------------------------------------
    {
          int idx = sVal.indexOf("%");
          if (idx >= 0 ) {
        	  String sNum=null;
        	  try { sNum = sVal.substring(0,idx).trim(); }
        	  catch(Exception e) { sNum=null;  }
              int i =sNum == null ? i=-1 : xMSet.xU.NaarInt(sNum);
              if( (i >= 0) && (i<=100) ) return i;
          }
          return (curval + 5) % 1000;
    }
	
	//-----------------------------------------------------------------------
	private Dimension getImageDisplayDimension()
	//-----------------------------------------------------------------------
	{
		try {
		 int iw = cPage.getImageWidth();
		 int ih = cPage.getImageHeigth();
		 double ratio = (double)iw  / (double)ih;
		 double dw =  (double)iw * (double)imgDisplayRatio * 0.01;
		 double dh = dw / ratio;
		 iw = (int)dw;
		 ih = (int)dh;
		 return new Dimension( iw , ih);
		}
		catch(Exception e) {
			do_error("Error bij bepalen dimensies " + e.getMessage());
			return new Dimension( -1 , -1);
		}
	}
	
	//-----------------------------------------------------------------------
	class picturePanel extends JPanel
	//-----------------------------------------------------------------------
	{
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g)
		{
			//
			g.setColor(Color.cyan);
			g.fillRect(0,0,WIDTH,HEIGHT);
			//
			if( cPage.cmcImg.img == null ) return;
			 //
			 try {
			  if( imgDisplayRatio != 100 ) {
				g.drawImage(cPage.cmcImg.img , 0, 0 , getImageDisplayDimension().width, getImageDisplayDimension().height, this);
			  }
			  else g.drawImage(cPage.cmcImg.img,0,0,this);
			 }
			 catch( Exception e) {
				do_error("Error bij tekenen image " + e.getMessage());
				return;
			}
			//
			if( drawmode  ) {
				gCon.repaint(g);
			}
		}
	}
	
	//-----------------------------------------------------------------------
	class histogramPanel extends JPanel
	//-----------------------------------------------------------------------
	{
		private static final long serialVersionUID = 2L;

		public void paint(Graphics g)
		{
			g.setColor(Color.WHITE);
			g.fillRect(0,0,this.getWidth(),this.getHeight());
			colorHistogram2(g , cPage.cmcImg.hstgrm , this.getWidth()  , this.getHeight() );
		}
	
	}
	
		
	//-----------------------------------------------------------------------
	private void doe_resize()
	//-----------------------------------------------------------------------
	{
		if( frame.isVisible() == false )  return;
		
	    //System.out.println("" + frame.getWidth() + " " + frame.getHeight());
		//int statusLengte = frame.getWidth() - scanCheckBox.getX() - scanCheckBox.getWidth() - 50;
		//this.ImageDetailLabel.setBounds( scanCheckBox.getX() + scanCheckBox.getWidth() + 10, 0, statusLengte , ImageDetailLabel.getHeight());
		menuBar.setBounds( menuBar.getX() , menuBar.getY() , frame.getWidth() - 15 , menuBar.getHeight() );
		//System.out.println( "" + statusLengte);
	    //	
		int imageDisplayWidth =  getImageDisplayDimension().width;
		int imageDisplayHeigth =  getImageDisplayDimension().height;
	    // indien geen picture return
		if( imageDisplayWidth < 0 ) {
			rightPanel.setVisible(false);
			imgPanel.setVisible(false);
			histoPanel.setVisible(false);
			return;
		}
		if( imgPanel.isVisible() == false ) { // toggle
			rightPanel.setVisible(true);
			imgPanel.setVisible(true);
			histoPanel.setVisible(true);
		}
		//
		int pw = frame.getWidth() - LEFTPANELWIDTH - 20;
		int ph = frame.getHeight() - COMPHOOGTE - 45;
		if( imageDisplayWidth > 0 ) {  // indien er geen img is ovelaten
		 int scrollRand = 21;
		 if( imageDisplayWidth + scrollRand < pw ) pw = imageDisplayWidth + scrollRand;
		 if( imageDisplayHeigth + scrollRand < ph ) ph = imageDisplayHeigth + scrollRand;
		} 
		rightPanel.setSize( pw , ph );
		imgPanel.setPreferredSize( new Dimension( getImageDisplayDimension().width , getImageDisplayDimension().height ) );
		//		
		leftPanel.setBounds(0, leftPanel.getY() , leftPanel.getWidth() , ph);
		int histoy = ph - histoPanel.getHeight();
		int marge = btnStats.getHeight() + imageProcessingComboBox.getHeight() + pbar.getHeight() + 20;
		if (histoy < this.btnStats.getY() + marge ) histoy = this.btnStats.getY() + marge;  // verhinder dat er overvloeit
		//
		histoPanel.setLocation( histoPanel.getX() , histoy );
		imageProcessingComboBox.setBounds(histoPanel.getX() , histoPanel.getY() - COMPHOOGTE -1, histoPanel.getWidth(), 20);	
		pbar.setBounds(imageProcessingComboBox.getX(), imageProcessingComboBox.getY()-21, imageProcessingComboBox.getWidth(), 20);
		//
	}
		
	//-----------------------------------------------------------------------
	private void doe_load(String FNaam )
	//-----------------------------------------------------------------------
	{
		    xMSet.setColourSchema(cmcProcEnums.ColourSchema.UNKNOWN);
	    	xMSet.setMonochromedetectionStatus("unkown");
			if( FNaam == null ) {
				cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRecentImageDir(),false);
				FNaam = fc.getAbsoluteFilePath();
				if( FNaam == null ) return;
			}
			if( xMSet.xU.IsBestand(FNaam) == false ) {
				popMessage("File [" + FNaam + "] could not be found");
				return;
			}
			if( xMSet.xU.isGrafisch(FNaam) == false ) {
				popMessage("File [" + FNaam + "] is not a supported graphical format");
				return;
			}
			// zet de attributen
			toonStatusInfo(FNaam);
			runCompleted=false;
			HistoUncroppedDumped=false;
			spinnerReset(FNaam);
			// push filenaam op stack
			pushRecentStack(FNaam,'F');
			pushRecentImageDir(FNaam);
		    cPage.prepareLoadImage(FNaam);
			requestTask( cmcProcSemaphore.TaskType.DO_LOAD_IMAGE );
			//
			disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGELOADING);
			//
	}
	
	//-----------------------------------------------------------------------
	private void doe_fastload(String FNaam )
	//-----------------------------------------------------------------------
	{
		    xMSet.setColourSchema(cmcProcEnums.ColourSchema.UNKNOWN);
	    	xMSet.setMonochromedetectionStatus("unkown");
			toonStatusInfo(gCon.getTitle());
			runCompleted=false;
			HistoUncroppedDumped=false;
			spinnerReset(FNaam);
			// push filenaam op stack
			pushRecentStack(FNaam,'F');
		    cPage.prepareLoadImage(FNaam);
			requestTask( cmcProcSemaphore.TaskType.DO_FAST_LOAD_IMAGE );
	}
	
	//-----------------------------------------------------------------------
	private void doe_preprocess()
	//-----------------------------------------------------------------------
	{
		timeStat.estimatePreprocessTime(ctrl.getActualImageLoadTime(),ctrl.getPageColourScheme());
		requestTask( cmcProcSemaphore.TaskType.DO_PREPROCESS );	
	}
	//-----------------------------------------------------------------------
	private void doe_grijs()
	//-----------------------------------------------------------------------
	{
		HistoGrayScaleDumped=false;
		requestTask( cmcProcSemaphore.TaskType.DO_GRAYSCALE );
	}
	//-----------------------------------------------------------------------
	private void doe_binarize()
	//-----------------------------------------------------------------------
	{
		timeStat.estimateBinarizeTime(ctrl.getActualPreprocessTime());
		requestTask( cmcProcSemaphore.TaskType.DO_BINARIZE );	
	}
	//-----------------------------------------------------------------------
	private void doe_cc()
	//-----------------------------------------------------------------------
	{
		timeStat.estimateCoCoTime(ctrl.getActualBinarizeTime(),ctrl.getBWDensity());
		requestTask( cmcProcSemaphore.TaskType.DO_CONNECTEDCOMPONENT );	
	}
	//-----------------------------------------------------------------------
	private void process_grayscale()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_GRAYSCALE );
	}
	//-----------------------------------------------------------------------
	private void process_bleach()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_BLEACH );
	}
	//-----------------------------------------------------------------------
	private void process_otsu()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_BLACKWHITE );
	}
	//-----------------------------------------------------------------------
	private void process_niblak()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_NIBLAK );
	}
	//-----------------------------------------------------------------------
	private void process_sauvola()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_SAUVOLA );
	}
	//-----------------------------------------------------------------------
	private void process_invert()
	//-----------------------------------------------------------------------
	{
		requestTask( cmcProcSemaphore.TaskType.PROCESS_INVERT );
	}
	//-----------------------------------------------------------------------
	private void process_histogramEqualization()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_EQUAL );
	}
	//-----------------------------------------------------------------------
	private void process_mainframe()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_MAINFRAME );
	}
	//-----------------------------------------------------------------------
	private void process_blueprint()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_BLUEPRINT );
	}
	//-----------------------------------------------------------------------
	private void process_sobelgrayscale()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_SOBELGRAYSCALE );
	}
	//-----------------------------------------------------------------------
	private void process_sobelcolor()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_SOBELCOLOR );
	}
	//-----------------------------------------------------------------------
	private void process_gradientnarrow()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_GRADIENT_NARROW );
	}
	//-----------------------------------------------------------------------
	private void process_gradientwide()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_GRADIENT_WIDE );
	}
	//-----------------------------------------------------------------------
	private void process_convolution(String sTipe)
	//-----------------------------------------------------------------------
	{
		if( sTipe.toUpperCase().startsWith("GLOW"))	requestTask( cmcProcSemaphore.TaskType.PROCESS_CONVOLUTION_GLOW );
		else
		if( sTipe.toUpperCase().startsWith("EDGE"))	requestTask( cmcProcSemaphore.TaskType.PROCESS_CONVOLUTION_EDGE );
		else
		if( sTipe.toUpperCase().startsWith("SHARPEN"))	requestTask( cmcProcSemaphore.TaskType.PROCESS_CONVOLUTION_SHARPEN );
		else
		if( sTipe.toUpperCase().startsWith("GAUSS"))	requestTask( cmcProcSemaphore.TaskType.PROCESS_CONVOLUTION_GAUSSIAN );
		else System.err.println("process_convolution - unknown [" + sTipe + "]");
	}
	//-----------------------------------------------------------------------
	private void process_stats()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.PROCESS_STATS );
	}
	//-----------------------------------------------------------------------
	private void requestTask(cmcProcSemaphore.TaskType tt)
	//-----------------------------------------------------------------------
	{
			pbar.setValue(0);
			pbar.setVisible(true);
			ctrl.prepareForTask(tt);
	}
		
	//-----------------------------------------------------------------------
	private void doe_toon_stats(Graphics g , boolean TgtIsFile )
	//-----------------------------------------------------------------------
	{
		cmcProcColorHistogram h = cPage.oriHstgrm;
		if ( h == null ) {
			do_error("original histogram is NULL");
			return;
		}
				
		int dikte = 20;
		int band = dikte + 20;
		int hoogte = 300;
		int breedte = (band *5);
		int xoffset = 20;
		int yoffset = 20;
		// locatie afhankeleijk van pixel
		int curPixX = currentPixel.x;
		int curPixY = currentPixel.y;
		if( curPixX != -1) {
		    if( ((curPixX + breedte ) < (rightPanel.getWidth()-10)) && ((curPixY + hoogte ) < (rightPanel.getHeight()-10) ) ) {
		    	xoffset = curPixX + band;
		    	yoffset = curPixY + band;
		    }
		}
				
		int Y = yoffset - band + hoogte - (hoogte - 256)/2;
		Color pc = Color.WHITE;
		if( TgtIsFile ) g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()) );
		           else g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 200) );
	    g.fillRoundRect(xoffset-band, yoffset-band, breedte, hoogte, 20, 20);	
	    pc = CORNFLOWER;
		g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 200) );
		g.drawLine(xoffset - (band/2) , Y     , xoffset + (7*(band/2)) , Y);
		g.drawLine(xoffset - (band/2) , Y-128 , xoffset + (7*(band/2)) , Y-128);
		g.drawLine(xoffset - (band/2) , Y-256 , xoffset + (7*(band/2)) , Y-256);
		g.drawLine(xoffset - (band/2) , Y-64  , xoffset + (7*(band/2)) , Y-64);
		g.drawLine(xoffset - (band/2) , Y-192 , xoffset + (7*(band/2)) , Y-192);
		
		double histo[] = null;
		int avg=0;
		int med=0;
		//int rad=8;
		int mi=0;
		int mx=0;
		int q1=0;
		int q3=0;
		double stddev=0;
		for(int k=0;k<4;k++)
		{
			switch(k)
			{
			 case 0 : { histo = h.getHistoRed(); g.setColor(Color.RED); avg = h.getMeanRed(); med = h.getMedianRed(); stddev = h.getStdDevRed(); mi=h.getMinRed(); mx=h.getMaxRed(); q1 = h.getQuart1Red(); q3 = h.getQuart3Red(); break; } 
			 case 1 : { histo = h.getHistoGreen(); g.setColor(Color.GREEN); avg = h.getMeanGreen(); med = h.getMedianGreen(); stddev = h.getStdDevGreen(); mi=h.getMinGreen(); mx=h.getMaxGreen(); q1 = h.getQuart1Green(); q3 = h.getQuart3Green(); break; } 
			 case 2 : { histo = h.getHistoBlue(); g.setColor(Color.BLUE); avg = h.getMeanBlue(); med = h.getMedianBlue(); stddev = h.getStdDevBlue(); mi=h.getMinBlue(); mx=h.getMaxBlue(); q1 = h.getQuart1Blue(); q3 = h.getQuart3Blue(); break; } 
			 case 3 : { histo = h.getHistoGray(); g.setColor(Color.BLACK); avg = h.getMeanGray(); med = h.getMedianGray(); stddev = h.getStdDevGray(); mi=h.getMinGray(); mx=h.getMaxGray(); q1 = h.getQuart1Gray(); q3 = h.getQuart3Gray(); break; } 
			}
			//			
			int midx = (xoffset-band) + (band * (k+1) );
			int bot = Y - q1;
			int top = Y - q3;
			//
			pc = g.getColor();
			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
		    //
			g.fillRect( midx - (dikte/2) , top , dikte , q3-q1); 
			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()).brighter() ); // kader helder
			g.drawRect( midx - (dikte/2) , top , dikte , q3-q1); 
			g.drawRect( midx - (dikte/2)+1 , top+1 , dikte-2 , q3-q1-2); 
			// vertikale lijnen tot de MIn en MAx
			g.drawLine( midx , Y - mi , midx , bot);
			g.drawLine( midx , top , midx , Y - mx);
			// dikte
			g.drawLine( midx+1 , Y - mi , midx+1 , bot);
			g.drawLine( midx+1 , top , midx+1 , Y - mx);
			// Horizontale eindjes
			g.drawLine( midx - 10, Y - mi , midx + 10 , Y - mi);
			g.drawLine( midx - 10, Y - mx , midx + 10 , Y - mx);
			// dikte
			g.drawLine( midx - 10, Y - mi - 1, midx + 10 , Y - mi - 1);
			g.drawLine( midx - 10, Y - mx - 1, midx + 10 , Y - mx - 1);
			//median
			g.drawLine( midx - (dikte/2) , Y - med , midx + (dikte/2) , Y - med); 
			g.drawLine( midx - (dikte/2) , Y - med -1 , midx + (dikte/2) , Y - med -1); 
			// mean
			pc = Color.WHITE;
			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
			g.drawLine( midx - (dikte/2) , Y - avg , midx + (dikte/2) , Y - avg); 
			g.drawLine( midx - (dikte/2) , Y - avg -1 , midx + (dikte/2) , Y - avg -1); 
			// stdev
			pc = Color.WHITE;
			g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 100) );
			//g.drawLine( midx - (dikte/2) , Y - avg - ((int)stddev/2) , midx + (dikte/2) , Y - avg - ((int)stddev/2) );
			//g.drawLine( midx - (dikte/2) , Y - avg + ((int)stddev/2) , midx + (dikte/2) , Y - avg + ((int)stddev/2) );
			g.drawOval(midx - 5, Y - avg - ((int)stddev/2) - 5 , 10, 10);
			g.drawOval(midx - 5, Y - avg + ((int)stddev/2) - 5 , 10, 10);
			g.drawLine(midx , Y - avg - ((int)stddev/2) , midx , Y - avg + ((int)stddev/2) );
		}	
				
		// RGB waarden van de geselecteerde pixel
		curPixX = currentPixel.x;
		curPixY = currentPixel.y;
		currentPixel.x = -1;
		currentPixel.y = -1;  // reset

		if( curPixX < 0 ) return;
		int curPixRed   = cPage.cmcImg.getPixelRedValueAtXY(curPixX,curPixY);
		int curPixGreen = cPage.cmcImg.getPixelGreenValueAtXY(curPixX,curPixY);
		int curPixBlue  = cPage.cmcImg.getPixelBlueValueAtXY(curPixX,curPixY);
		int curPixGray  = cPage.cmcImg.getPixelGrayValueAtXY(curPixX,curPixY);
		//System.out.println("-->" + curPixRed + " " + curPixGreen + " " + curPixBlue);
		
		int midx = xoffset;
		pc = Color.RED;
		g.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 200) );
		g.drawLine( midx , Y - curPixRed , midx + band , Y - curPixGreen );
		g.drawLine( midx + band , Y - curPixGreen , midx + (2*band) , Y - curPixBlue );
		g.drawLine( midx + (2*band), Y - curPixBlue , midx + (3*band) , Y - curPixGray );
	}
		
	//-----------------------------------------------------------------------
	private void dumpBoxDiagram()
	//-----------------------------------------------------------------------
	{
		try {
		  BufferedImage bi = new BufferedImage( 170 , 270 , BufferedImage.TYPE_INT_RGB);
		  Graphics2D ig = bi.createGraphics();
		  doe_toon_stats(ig,true);
		  ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getBoxDiagramName()));
          //System.err.println("DEBUG - boxdiagram dumped " + xMSet.getPreferredImageSuffix());
		}
		catch(Exception e) {
			do_error("Creating box diagram " + e.getMessage() + " " + xMSet.xU.LogStackTrace(e) );
		}
	}
	
	//-----------------------------------------------------------------------
	private void writeHistogramToFile(String sTipe)
	//-----------------------------------------------------------------------
	{
	   int breedte = histoPanel.getWidth();
	   int hoogte = histoPanel.getHeight();
	   try {
			  BufferedImage bi = new BufferedImage( breedte , hoogte , BufferedImage.TYPE_INT_RGB);
			  Graphics2D ig = bi.createGraphics();
			  // witte achtergond
			  Color pc = Color.WHITE;
			  ig.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()));
			  ig.fillRect(0,0,breedte,hoogte);
			  //
			  if( sTipe.toUpperCase().trim().indexOf("ORIGINAL")>=0) {
			    colorHistogram2(ig,cPage.oriHstgrm,breedte,hoogte);
			    ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getHistoScreenShotDumpNameUncropped()));
			  }
			  if( sTipe.toUpperCase().trim().indexOf("GRAY")>=0) {
				    colorHistogram2(ig,cPage.cmcImg.hstgrm,breedte,hoogte);
				    ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getHistoScreenShotDumpNameGrayScale()));
			  }
			}
			catch(Exception e) {
				do_error("Writing histogram " + e.getMessage() + " " + xMSet.xU.LogStackTrace(e));
			}
	}
	
	//-----------------------------------------------------------------------
	private void colorHistogram2(Graphics g , cmcProcColorHistogram h , int inWi , int inHe)
	//-----------------------------------------------------------------------
	{
		if( h == null ) return;   // bvb. editmode
		//
		int yoffset = (inHe - 256) / 2;
		int breedte = inWi - 30;
		int xoffset =  inWi - (inWi - breedte) / 2;
		//	
		double maxallow= (double)1.5;
		double histo[] = null;
		int avg=0;
		int med=0;
		int rad=8;
		double stddev=0;
		for(int k=0;k<4;k++)
		{
			 switch(k)
			 {
			 case 0 : { histo = h.getHistoRed(); g.setColor(Color.RED); avg = h.getMeanRed(); med = h.getMedianRed(); stddev = h.getStdDevRed(); break; } 
			 case 1 : { histo = h.getHistoGreen(); g.setColor(Color.GREEN); avg = h.getMeanGreen(); med = h.getMedianGreen(); stddev = h.getStdDevGreen(); break; } 
			 case 2 : { histo = h.getHistoBlue(); g.setColor(Color.BLUE); avg = h.getMeanBlue(); med = h.getMedianBlue(); stddev = h.getStdDevBlue(); break; } 
			 case 3 : { histo = h.getHistoGray(); g.setColor(Color.DARK_GRAY); avg = h.getMeanGray(); med = h.getMedianGray(); stddev = h.getStdDevGray(); break; } 
			 }
			
		    double max = 0;
		    for(int i=0;i<255;i++) // MAX maar exclusief de WITTE component
			{
		    	if( histo[i] > max ) max = histo[i];
			}
		    // cap de witte component - in strips is er veel wit / boorden enz.
		    if( histo[255] > (maxallow*max) ) histo[255]= maxallow * max;
		    if( histo[0] > (maxallow*max) ) histo[0]= maxallow * max;
		    //
		    int xp=0;
			int x=0;
			int xavg=0;
			int xmed=0;
			for(int i=0;i<256;i++)
			{
				x = (int)(histo[i] * (double)breedte / max);    // de histo bevat niet echt de frequentie maar de FREQ / TOTAAL pixels; doch aangeziendeeltal constant
				g.drawLine( xoffset - xp  , 256 - i + yoffset + 1 , xoffset - x , 256 - i + yoffset);
				xp = x;
				if( i == avg ) xavg = x;
				if( i == med ) xmed = x;
			}
			// vullen
			Color pc = g.getColor();
		    Color trans = new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 80 );
		    g.setColor(trans);
			for(int i=0;i<256;i++)
			{
				x = (int)(histo[i] * (double)breedte / max); 
				g.drawLine( xoffset , 256 - i + yoffset , xoffset - x - 1 , 256 - i + yoffset);
			}
			g.setColor(pc);
			// mean
			g.fillOval(xoffset - (rad/2), 256 - avg + yoffset - (rad/2),rad,rad);
			// mediaan
			g.fillOval(xoffset - xmed - (rad/2), 256 - med + yoffset - (rad/2),rad,rad);
		}
		
		// as
		g.setColor(Color.DARK_GRAY);
		g.fillRect( xoffset, yoffset , 1 , 256 );
		
		// gradient
		for(int i=0;i<256;i++)
		{
			g.setColor(new Color(i,i,i));
			g.fillRect( xoffset + 5, 256 - i + yoffset , 10 , 1 );
		}
		
	}
	
	//-----------------------------------------------------------------------
	private void doe_extract()
	//-----------------------------------------------------------------------
	{
		// Has Bulk extractor been selected
		if( this.scanCheckBox.isSelected() ) {
			//
			spinnerReset(null);
			cPage.prepareLoadImage(null);
		    // Keuze van een Folder
			cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRootDir(),true);
			String sDir = fc.getAbsoluteFilePath();
			if( sDir ==  null ) sDir = "No folder specified";
			if( xMSet.xU.IsDir(sDir) == false ) {
				popMessage("[" + sDir + "] is not a valid folder");
				return;
			}
			// scan list and monitor
			xMSet.createScanList(sDir);
			int aantal = xMSet.getScanListSize();
			if( aantal <= 0 ) {
				this.scanCheckBox.setSelected(false);
				popMessage("There are no valid graphical files present in folder [" + sDir + "]");
			}
			else {
			  // METADATA input screen for a series of images  -> request to create estafettefile (bare essential metadata)
			  if( xMSet.xU.IsBestand(xMSet.getEstafetteFileName()) == true ) {
				  xMSet.xU.VerwijderBestand(xMSet.getEstafetteFileName());
				  if( xMSet.xU.IsBestand(xMSet.getEstafetteFileName()) == true ) {
					  do_error("Could not delete [" + xMSet.getEstafetteFileName() + "]");
					  return;
				  }		  
			  }
			  cmcMetaDataDialog cmet = new cmcMetaDataDialog(frame , xMSet , null , false , logger , true);
			  boolean ib =  xMSet.getDialogCompleteStatus();
			  if( ib == false ) return;
			  // check whether the estafette file is there
			  if( xMSet.xU.IsBestand(xMSet.getEstafetteFileName()) == false ) {
				  do_error("Could not locate estafette file [" + xMSet.getEstafetteFileName() + "]" );
				  return;
			  }
			  //
			  scanMode=true;
			  runCompleted=true; // triggert de start
			  //
			  startMonitor(sDir);
			}
		}
		// Piecemeal extractor
		else {
		  scanMode = false;
		  doe_load(null);
		}
	}
	
	//-----------------------------------------------------------------------
	private void popMessage(String sMsg)
	//-----------------------------------------------------------------------
	{
		do_error(sMsg);
		JOptionPane.showMessageDialog(frame,sMsg,xMSet.getApplicDesc(),JOptionPane.WARNING_MESSAGE);
	}

	//-----------------------------------------------------------------------
	private void setControlStatusW(String control , boolean stat)
	//-----------------------------------------------------------------------
	{
		cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS mi = null;
		cbrTekStraktorModel.cmcProcEnums.POP_ITEMS pi = null;
		//
		if( control.toUpperCase().startsWith("IMAGE") ) {
			btnImage.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.LOAD_IMAGE;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.LOAD_IMAGE;
		}
		else
		if( control.toUpperCase().startsWith("EXTRACT") ) {
			btnExtract.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.EXTRACT_TEXT;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.EXTRACT_TEXT;
		}
		else
		if( control.toUpperCase().startsWith("EDIT") ) {
			btnEdit.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.START_EDIT;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STOP_EDIT;
		}
		else
		if( control.toUpperCase().startsWith("OCR") ) {
			btnOCR.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.OCR;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.OCR;
		}
		else
		if( control.toUpperCase().startsWith("TRANS") ) {
			btnTranslate.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.TRANSLATE;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.TRANSLATE;
		}
		else
		if( control.toUpperCase().startsWith("REPORT") ) {
			btnReport.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.REPORT;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.REPORT;
		}
		else
		if( control.toUpperCase().startsWith("STAT") ) {
			btnStats.setEnabled(stat);
			mi = cbrTekStraktorModel.cmcProcEnums.MENU_ITEMS.STATISTICS;
			pi = cbrTekStraktorModel.cmcProcEnums.POP_ITEMS.STATISTICS;
		}
		else
		if( control.toUpperCase().startsWith("SPIN") ) {
				spinner.setEnabled(stat);
		}
		// menu items
        if( mi != null ) this.menuMan.setMenuItemStatus(mi, stat);		
        // pop menu
        if( pi != null ) this.popMan.setMenuItemVisible(pi, stat);		
        
	}
	
	//-----------------------------------------------------------------------
	private void setControlStatus(boolean stat)
	//-----------------------------------------------------------------------
	{
		setControlStatusW("IMAGE",stat);
		setControlStatusW("EXTRACT",stat);
		setControlStatusW("EDIT",stat);
		setControlStatusW("OCR",stat);
		setControlStatusW("TRANS",stat);
		setControlStatusW("REPORT",stat);
		setControlStatusW("STATS",stat);
		setControlStatusW("SPIN",stat);
	}
	
	//-----------------------------------------------------------------------
	private void spinnerReset(String longFileName)
	//-----------------------------------------------------------------------
	{
	   imgDisplayRatio=100;
	   prevImgDisplayRatio=-1;  // triggers a resize to 100%
	   timeStat.estimateImageLoadTimeNanoSeconds(longFileName);
	   spinner.setValue(imgDisplayRatio);
	}
	
	//-----------------------------------------------------------------------
	private void disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE as)
	//-----------------------------------------------------------------------
	{
		setControlStatus(false);
		appState=as;
	}
	
	//-----------------------------------------------------------------------
	private void enable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE as)
	//-----------------------------------------------------------------------
	{
		setControlStatus(true);
		appState=as;
	}
	
	//-----------------------------------------------------------------------
	private void switchToImageMode(boolean stat)
	//-----------------------------------------------------------------------
	{
		imageProcessingComboBox.setVisible(stat);
	}
	
	//-----------------------------------------------------------------------
	private void doe_info()
	//-----------------------------------------------------------------------
	{
		// grab the Histogram from main screen and rotate
		gpRotateImageFile xRot = new gpRotateImageFile( xMSet.xU , logger);
		boolean ib = xRot.rotate90DegreesClockWize( xMSet.getHistoScreenShotDumpNameUncropped() , xMSet.getTempJPGName() );
		if( ib == false ) {
			//  If there is a ZIP file then no worries it will be used to extract the rotated histogram
			if( xMSet.xU.IsBestand(xMSet.getReportZipName()) == false ) {
			  popMessage("Cannot rotate " + xMSet.getHistoScreenShotDumpNameUncropped() + " and not ZIP file [" + xMSet.getReportZipName() + "]");
			  return;
			}
		}
		// Dialog
		cmcMetaDataDialog cmet = new cmcMetaDataDialog(frame , xMSet , cPage , true , logger , false); 
		cmet=null;
		// if the metadata changed - and there is a ZIP, then changes will need to be stored on disk
		if( xMSet.hasMetadataBeenModified() ) {
			cmcBookMetaDataDAO mdao = new cmcBookMetaDataDAO(xMSet , xMSet.getOrigImageLongFileName() , cPage , logger );
			if( mdao.flushChangesToArchive() == false ) {
				popMessage("Something went wrong when loading the changes in the archive");
			}
			mdao=null;
		}
	}


	//-----------------------------------------------------------------------
	private String unzip_report(String ZipFileNaam)
	//-----------------------------------------------------------------------
	{
			String sRet=null;
			String fo = ZipFileNaam;
			if( fo == null ) return null;
			if( xMSet.xU.IsBestand( fo ) == false ) return null;
	        gpUnZipFileList uzip = new gpUnZipFileList( fo , xMSet.getTempDir() , null , logger);
	        ArrayList<String> flist = xMSet.xU.GetFilesInDir( xMSet.getTempDir() , null );
	        boolean isImage=false;
	        boolean isHTML=false;
	        for(int i=0;i<flist.size();i++)
	        {
	        	String FNaam = xMSet.getTempDir() + xMSet.xU.ctSlash + flist.get(i);
	        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
	        	isImage = xMSet.xU.isGrafisch(FNaam);
	        	isHTML  = FNaam.toLowerCase().endsWith(".html");
	        	if( isImage || isHTML ) {
	                String FDest = null;
	                if( isHTML ) {
	                	FDest = xMSet.getReportHTMLDir() + xMSet.xU.ctSlash + flist.get(i);
	                	sRet = FDest;
	                }
	                else {
	                	FDest = xMSet.getReportImageDir() + xMSet.xU.ctSlash + flist.get(i);
	                	// zet op de purge lijst
	                	PurgeItem x = new PurgeItem( FDest );
	                	verwijderLijst.add(x);
	                }
	                
	                try {
	                 if( xMSet.xU.IsBestand(FDest) == true ) xMSet.xU.VerwijderBestand(FDest);
	                 xMSet.xU.copyFile( FNaam , FDest );
	                }
	                catch( Exception e ) {
	                	System.err.println("Error moving [" + FNaam +"] to [" + FDest +"] " + e.getMessage() );
	                	return null;
	                }
	        	}
	        	xMSet.xU.VerwijderBestand( FNaam );
	        }
	        uzip=null;
	        return sRet;
		}

	
	//-----------------------------------------------------------------------
	private void loadVerwijderLijst()
	//-----------------------------------------------------------------------
	{
		 loadVerwijderLijstDetail( xMSet.getReportImageDir() , true , false);
		 loadVerwijderLijstDetail( xMSet.getCacheDir() , true , true );
		 do_log(1,"Initial purge list stocked by [" + verwijderLijst.size() + "] elements");
		 processVerwijderLijst(true);
	}
	
	//-----------------------------------------------------------------------
	private void loadVerwijderLijstDetail(String sDir , boolean graphical , boolean xml)
	//-----------------------------------------------------------------------
	{
		 if( sDir == null ) return;
		 if( xMSet.xU.IsDir(sDir) == false ) return;
		 ArrayList<String> flist = xMSet.xU.GetFilesInDir( sDir , null );
		 for(int i=0;i<flist.size();i++)
	     {
	        	String FNaam = sDir + xMSet.xU.ctSlash + flist.get(i);
	        	if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
	        	String ssuf = xMSet.xU.GetSuffix(FNaam).trim().toUpperCase();
	        	boolean remove = false;
	        	if( (xMSet.xU.isGrafisch(FNaam ) == true) && (graphical) ) remove=true;
	        	if( (ssuf.compareToIgnoreCase("XML")==0) && (xml) ) remove=true;
	        	if( !remove ) continue;
	            PurgeItem x = new PurgeItem( FNaam );
	            verwijderLijst.add(x);
	     }
		
	}
	
	//-----------------------------------------------------------------------
	private void processVerwijderLijst(boolean forced)
	//-----------------------------------------------------------------------
	{
		if ( verwijderLijst == null ) return;
		for(int i=0;i<verwijderLijst.size();i++)
		{
			long delta = System.currentTimeMillis() - verwijderLijst.get(i).tstmp;
			if( (delta < 30000) && (forced==false) ) continue;
			String FNaam = verwijderLijst.get(i).Fname;
			if( FNaam == null ) continue;
			if( xMSet.xU.IsBestand( FNaam ) == false ) continue;
			verwijderLijst.get(i).Fname = null;  // markeer
			xMSet.xU.VerwijderBestand(FNaam);
			do_log(5,"Purging -> " + FNaam );
		}
		int aantal = verwijderLijst.size();
		for(int i=0;i<aantal;i++)
		{
			boolean found = false;
			for(int j=0;j<verwijderLijst.size();j++)
			{
				if( verwijderLijst.get(j).Fname != null ) continue;
				verwijderLijst.remove(j);
				found = true;
				break;
			}
			if( found == false ) break;
		}
		int removed = aantal - verwijderLijst.size();
		if( removed > 0) do_log(5,"Purged [" + removed + "] files out of [" + aantal + "]");
	}
	
	//-----------------------------------------------------------------------
	private String selecteerArchief()
	//-----------------------------------------------------------------------
	{
		// kies welke report
				cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRecentArchiveDir(),false);
				String FNaam = fc.getAbsoluteFilePath();
				if( FNaam == null )  return null;
				if( xMSet.xU.IsBestand(FNaam) == false ) return null;
				// moet een zip zijn
				if( FNaam.toUpperCase().trim().endsWith(".ZIP") == false ) {
					popMessage(" File [" + FNaam + "] is not a ZIP file");
					return null;
				}
				//
				String reportURL = unzip_report( FNaam );
				if( reportURL == null ) {
					popMessage("Cannot create URL for [NULL]");
					return null;
				}
				if( xMSet.xU.IsBestand(reportURL) == false ) {
					popMessage("Cannot create URL for [" + xMSet.getReportHTMLFileName() + "]");
					return null;
				}
				pushRecentArchiefDir(FNaam);
				return reportURL;
	}
	
	//-----------------------------------------------------------------------
	private void openFileInBrowser(String sFile)
	//-----------------------------------------------------------------------
	{
		String reportURL = sFile;
		if( reportURL == null ) return;
		reportURL = "file:///" + reportURL;
		String err = null;
		Runtime rt = Runtime.getRuntime();
	    try{
	    	Process clientProcess = null;
	    	// OS
	    	if( xMSet.getMyOS() == cmcProcEnums.OS_TYPE.MSDOS ) {
	    		 switch (xMSet.getBrowser() )
	    		 {
	    		 case MOZILLA : {
	    		  clientProcess = rt.exec(new String[] {"C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe","-new-window", reportURL });
	    	      break;
	    		  }
	    		 case EXPLORER : {
	    		  clientProcess = rt.exec(new String[] {"C:\\Program Files\\Internet Explorer\\iexplore.exe",reportURL});
	    		  break; 
	    		  }
	    		 case CHROME : {
	    		    clientProcess = rt.exec(new String[] { System.getProperty("user.home") + "\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",reportURL});
	    		  break;    
	    		  }
	    		 default : {
	    			 err = "Browser [" + xMSet.getBrowser() + "] is not supported for [" + xMSet.getMyOS() + "]";
	    			 break;
	    		  }
	    		 }
	    	}
	    	else 
	    	if( xMSet.getMyOS() == cmcProcEnums.OS_TYPE.LINUX ){
	    		 switch (xMSet.getBrowser() )
	    		 {
	    		 case MOZILLA : {
	    		  clientProcess = rt.exec(new String[] {"/usr/bin/firefox","-new-window", reportURL });
	    	      break;
	    		  }
	    		 case EXPLORER : {
	    		  //clientProcess = rt.exec(new String[] {"C:\\Program Files\\Internet Explorer\\iexplore.exe",reportURL});
	    		  //break; 
	    		  }
	    		 case CHROME : {
	    		  //clientProcess = rt.exec(new String[] {"/Program Files (x86)/Google/Chrome/Application/chrome.exe",reportURL});
	    		  //break;    
	    		  }
	    		 default : {
	    			 err = "Browser [" + xMSet.getBrowser() + "] is not supported for [" + xMSet.getMyOS() + "]";
	    			 break;
	    		  }
	    		 }
	    	}
	    	else {
	    	 err = "Operating system [" + xMSet.getMyOS() + "] is currently not supported";	
	 		}
	    	if( err == null ) {
	    	 int exitVal = clientProcess.waitFor();
	         do_log(5,"viewReport exitValue: " + exitVal);
	    	}
	    } catch (Exception e){
	    	err = e.getMessage();
	    	e.printStackTrace();
	    }
	    if( err != null ) popMessage("Could not open brower " + err);
	}
	
	
	//-----------------------------------------------------------------------
	private void writePeaks()
	//-----------------------------------------------------------------------
	{
		   if( cPage.monoHstgrm == null )  return;   // is het geval bij editmode
		   int hoogte = histoPanel.getWidth();
		   int breedte = histoPanel.getHeight();
		   try {
				  BufferedImage bi = new BufferedImage( breedte , hoogte , BufferedImage.TYPE_INT_RGB);
				  Graphics2D ig = bi.createGraphics();
				  // witte achtergond
				  Color pc = Color.WHITE;
				  ig.setColor( new Color(pc.getRed(),pc.getGreen(),pc.getBlue()));
				  ig.fillRect(0,0,breedte,hoogte);
				  //
				  peakHistogram(ig,cPage.monoHstgrm.getSmoothHisto(),breedte,hoogte);
				  ImageIO.write(bi, xMSet.getPreferredImageSuffix() , new File(xMSet.getReportPeakDiagramName()) );
		   }  
		   catch(Exception e) {
			 do_error("writePeaks " + e.getMessage() + xMSet.xU.LogStackTrace(e) );
		   }
	}
	
	//-----------------------------------------------------------------------
	private void peakHistogram(Graphics g , double[] ar , int canvasWidth , int canvasHeigth )
	//-----------------------------------------------------------------------
	{
		//
		int CAP = 2;
		//
		int vertMarge = 15;
		int picHoogte = canvasHeigth - ( 2 * vertMarge);
		int picBreedte = ar.length;
		//
		double maxFreq=0;
		for(int i=0;i<ar.length;i++)
		{
			double h = ar[i];
			if( h <= 1 ) continue;  // geen peak
			if( h >= 2 ) h -= 2;
			if( h >= 1 ) h -= 1;
		    if( maxFreq < h ) maxFreq = h;
		}
		// 2de grootste
		double secondMaxFreq=0;
		int secondIdx=-1;
		for(int i=0;i<ar.length;i++)
		{
			double h = ar[i];
			if( h <= 1 ) continue;  // geen peak
			if( h >= 2 ) h -= 2;
			if( h >= 1 ) h -= 1;
		    if( h == maxFreq ) continue;
			if( secondMaxFreq < h ) {secondMaxFreq = h; secondIdx=i; }
		}
		//System.err.println(">>" + maxFreq + " " + secondMaxFreq );
		// CAPPEN
		if ( maxFreq > (CAP * secondMaxFreq) ) maxFreq = CAP * secondMaxFreq;
	    //	
		int xOffset = (canvasWidth - picBreedte) / 2;
		int yOffset = canvasHeigth - vertMarge;
		//
		Color pc = Color.BLUE;
		Color trans = new Color(pc.getRed(),pc.getGreen(),pc.getBlue(), 80 );
		// abscis
		g.drawLine( xOffset , yOffset , xOffset + picBreedte , yOffset);
		//
		int py = yOffset;
		for(int i=0;i<ar.length;i++)
		{
			double h = ar[i];
			if( h >= 2 ) h -= 2;
			if( h >= 1 ) h -= 1;
			double yd = (h / (double)maxFreq ) * (double)picHoogte;
			if( yd > picHoogte ) yd = picHoogte; // cappen
			// licht
			g.setColor(trans);
			g.drawLine( xOffset + i, yOffset , xOffset + i , yOffset - (int)yd );
			// hard
			g.setColor(pc);
			g.drawLine( xOffset + i - 1, py , xOffset + i , yOffset - (int)yd  );
			py = yOffset - (int)yd;
		}
	    // Peaks
		Color pc1 = Color.BLUE;
		Color pc2 = Color.RED;
		for(int i=0;i<ar.length;i++)
		{
			double h = ar[i];
			boolean valid=false;
			if( h <= 1 ) continue;  // geen peak
			if( h >= 2 ) { h -= 2;  valid=true; }
			if( h >= 1 ) { h -= 1;  }
			double yd = (h / (double)maxFreq ) * (double)picHoogte;
			if( yd > picHoogte ) yd = picHoogte; // cappen
			g.setColor(pc1);
			g.drawLine( xOffset + i  , yOffset , xOffset + i , yOffset - (int)yd   );
			// circle
			if( (valid==true) || (i == secondIdx)) {
				if( valid ) g.setColor(pc2); else g.setColor(pc1);
				g.fillOval( xOffset + i - 2 , yOffset - (int)yd - 6 , 4 , 4 );
		        //System.err.println(">>>" + i + " " + valid + secondIdx );
			}
			
			// marker
			g.setColor(pc1);
			g.drawLine( xOffset + i  , yOffset + 4 , xOffset + i , yOffset + 2   );
		}
		// gradient
		for(int i=0;i<ar.length;i++)
		{
			double grad = ((double)i / (double)ar.length ) * (double)256;
			int k = (int)Math.round(grad);
			g.setColor(new Color(k,k,k));
			g.fillRect( xOffset + i , yOffset + 6 , 1 , 10 );
		}
	}
	
	//-----------------------------------------------------------------------
	private void doe_dialoog()
	//-----------------------------------------------------------------------
	{
			requestTask( cmcProcSemaphore.TaskType.DO_DIALOG );
	}
	//-----------------------------------------------------------------------
	private boolean toon_dialoog()
	//-----------------------------------------------------------------------
	{
		 // make sure to have a Histo bitmap stored
		 if( xMSet.xU.IsBestand(xMSet.getTempJPGName()) == false ) {
		  gpRotateImageFile xRot = new gpRotateImageFile( xMSet.xU , logger);
		  boolean ibx = xRot.rotate90DegreesClockWize( xMSet.getHistoScreenShotDumpNameUncropped() , xMSet.getTempJPGName() );
		 }
         // Modal Dialog
		 cmcMetaDataDialog cmet = new cmcMetaDataDialog(frame , xMSet , cPage , false , logger , false);
		 boolean ib =  xMSet.getDialogCompleteStatus();
		 return ib; 
	}
	
	//-----------------------------------------------------------------------
	private String selecteerZipFile(String preferredFile)
	//-----------------------------------------------------------------------
	{
		            String FNaam = preferredFile;
		        	gCon.setZipFileName( null );
				    if( FNaam == null ) {
					 cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getArchiveDir(),false);
		             FNaam = fc.getAbsoluteFilePath();
					 if( FNaam == null )  return null;
		            }
					if( xMSet.xU.IsBestand(FNaam) == false ) return null;
					// moet een zip zijn
					if( FNaam.toUpperCase().trim().endsWith(".ZIP") == false ) {
						popMessage(" File [" + FNaam + "] is not a ZIP file");
						return null;
					}
					pushRecentArchiefDir(FNaam);
					return FNaam;
	}

	//-----------------------------------------------------------------------
	private boolean startEdit(String ZipFileName)
	//-----------------------------------------------------------------------
	{
		if( ZipFileName == null ) return false;
		if( xMSet.xU.IsBestand( ZipFileName ) == false ) return false;
		boolean ib = gCon.initializeController(ZipFileName);
		if( ib == false ) {
			popMessage("Could not activiate EDIT mode");
			return false;
		}
		else {
			btnEdit.setText("Stop Edit");
			requestTask( cmcProcSemaphore.TaskType.DO_EDITOR_IMAGE );
		}
		return true;
	}
	

	//-----------------------------------------------------------------------
	private void doe_pixelinfo(int x, int y)
	//-----------------------------------------------------------------------
	{
		   if( cPage.cmcImg.img == null ) return;
	       //do_log(9,"--> (" + x + "," + y + ")" ); 
	}
	
	//-----------------------------------------------------------------------
	private void startEditMode()
	//-----------------------------------------------------------------------
	{
		disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDITLOADING);
		pbar.setVisible(true);
		//btnEdit.setEnabled(true);
		setControlStatusW("EDIT",true);
	}
	
	//-----------------------------------------------------------------------
	private void endEditMode()
	//-----------------------------------------------------------------------
	{
		int changes = gCon.getNumberOfChanges();
		if ( changes == 0 ) gCon.setIgnoreChanges();
		else {
			if( yesNoDialog("Do you want save your changes?" , "There are " + changes + " modifications") == false ) gCon.setIgnoreChanges();
		}
		requestTask( cmcProcSemaphore.TaskType.DO_FLUSH_CHANGES );  // process everything in asynchronous manner
		btnEdit.setText("Edit");
	}
	
	//-----------------------------------------------------------------------
	private void doeParagraphDialog(int idx)
	//-----------------------------------------------------------------------
	{
		if( idx < 0) return;
		cmcParagraphEditorDialog pe = new cmcParagraphEditorDialog(frame,xMSet,gCon,idx,logger);
        toonEditStatus();		
	}
	
	//-----------------------------------------------------------------------
	private void toonStatusInfo(String s)
	//-----------------------------------------------------------------------
	{
		//ImageDetailLabel.setText(s);
		//do_error( s);
		statusMessage.setText(s);
	}
	
	//-----------------------------------------------------------------------
	private boolean yesNoDialog(String sMsg , String sTitle)
	//-----------------------------------------------------------------------
	{
		int reply = JOptionPane.showConfirmDialog(null, sMsg, sTitle, JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) return true;
        return false;
	}
	
	//-----------------------------------------------------------------------
	private void askToStartEdit()
	//-----------------------------------------------------------------------
	{
	   if( cPage.cmcImg.img == null ) return;
	   // einde van een extractie
	   if( (robotMode == true) && (runCompleted == true) ) {
	     if( yesNoDialog("Do you want to start editing" , xMSet.getOrigImageLongFileName()) == true) {
	    	 String sPref = xMSet.getReportZipName();
	    	 if( xMSet.xU.IsBestand( sPref ) )  clickOnEdit(sPref);	
	     }
	   }
	}
	
	//-----------------------------------------------------------------------
	private void clickOnEdit(String preferredFile)
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("Edit");
		switchToImageMode(false);
		robotMode=false;
		drawmode = !drawmode;
		if( drawmode ) {
			boolean ib = startEdit(selecteerZipFile(preferredFile));
			if( ib == false ) {drawmode = false; return; }
			toonStatusInfo("Loading [" + gCon.getTitle() + "]");
			startEditMode();
		}
		else {
			endEditMode();
		}
	}
	
	//-----------------------------------------------------------------------
	private void doEndDrag()
	//-----------------------------------------------------------------------
	{
		if( this.drawmode == false ) return;
		Rectangle r = gCon.makePositiveRectangle( currentPixel.x , currentPixel.y , dragStartPixel.x , dragStartPixel.y );
		//do_log( 1, "END DRAG " + r.x + " " + r.y + " " + r.width + " " + r.height );
		boolean ib = yesNoDialog("Do you want to create a text object" , "");
		if( ib ) {
			gCon.makeTextParagraph(r);
		}
		gCon.endDrag();
	    imgPanel.repaint();
	}
	
	//-----------------------------------------------------------------------
	private void doe_edit_popup(boolean stat )
	//-----------------------------------------------------------------------
	{
		gCon.doImagePopup( stat , currentPixel.x , currentPixel.y );
		imgPanel.repaint();
	}
	
	//-----------------------------------------------------------------------
	private void do_rightclickX(Component c , int x , int y)
	//-----------------------------------------------------------------------
	{
	 popMan.popItUp(c, ""+appState , x, y);
	}
	
	//-----------------------------------------------------------------------
	private void do_infodialogs(int x , int y)
	//-----------------------------------------------------------------------
	{
		  if( drawmode ) { 
		    cmcEditOptionDialog eo = new cmcEditOptionDialog( frame , xMSet , gCon , logger);
		    if( xMSet.getDialogCompleteStatus() ) {
			 requestTask( cmcProcSemaphore.TaskType.DO_EDITOR_IMAGE ); // OK has been pressed so refresh
		    }
		  }
		  else {
			doe_info(); // dialog
		  }
	}
	
	//-----------------------------------------------------------------------
	private void doSave()
	//-----------------------------------------------------------------------
	{
		switch( appState )
		{
		case IMAGE           : { break; }
		case EXTRACTED       : { break; }
		case EDIT            : { break; }
		default : return;
		}
		cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRecentSaveDir(),false);
		String FName = fc.getAbsoluteFilePath();
		if( FName  == null ) return;
		if( xMSet.xU.isGrafisch( FName ) == false ) {
			FName = FName + "." + xMSet.getPreferredImageSuffix();
		}
		if( xMSet.xU.IsBestand(FName) ) {
			if( yesNoDialog("The file you defined already exists. Overwrite?", FName) == false ) return;
		}
		// iRout.write
		boolean ib=true;
		switch( appState )
		{
		case IMAGE           : { cPage.cmcImg.writeToFile(FName); break; }
		case EXTRACTED       : { cPage.cmcImg.writeToFile(FName); break; }
		case EDIT            : { ib=gCon.saveWorkFile(FName); break; }
		default : return;
		}
		if( ib == false ) popMessage("Could not save image to [" + FName + "]");
		else {
			popMessage("Image saved to [" + FName + "]");
			pushRecentSaveDir(FName);
		}
	}
	
	//-----------------------------------------------------------------------
	private void setQuickButtons(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE ia)
	//-----------------------------------------------------------------------
	{
	//do_error("STATUS -> " + ia );
		// Save Stop Redo Info Option
		String ss="NNNNN";
		switch( ia )
		{
		case IDLE            : { ss = "NNNYN"; break; }
		case IMAGELOADING    : { ss = "NYNNN"; break; }
		case IMAGE           : { ss = "YNYYN"; break; }
		case EXTRACTING      : { ss = "NYNNN"; break; }
		case EXTRACTED       : { ss = "YNYYN"; break; }
		case EDITLOADING     : { ss = "NYNNN"; break; }
		case EDIT            : { ss = "YYYYY"; break; }
		case STATSGATHERING  : { ss = "NYNNN"; break; }
		case STATISTICS      : { ss = "NNNNN"; break; }
		case OCR_BUSY        : { ss = "NYNNN"; break; }
		default : { ss = "NNNNN"; do_error("Unsupported status " + ia); break; }
		}
		saveButton.setVisible( ss.substring(0,1).compareToIgnoreCase("Y")==0 ? true : false );
		stopButton.setVisible( ss.substring(1,2).compareToIgnoreCase("Y")==0 ? true : false );
		redoButton.setVisible( ss.substring(2,3).compareToIgnoreCase("Y")==0 ? true : false );
		infoButton.setVisible( ss.substring(3,4).compareToIgnoreCase("Y")==0 ? true : false );
		optionButton.setVisible( ss.substring(4,5).compareToIgnoreCase("Y")==0 ? true : false );
		//
		saveSepa.setVisible(saveButton.isVisible());
		stopSepa.setVisible(stopButton.isVisible());
		redoSepa.setVisible(redoButton.isVisible());
		infoSepa.setVisible(infoButton.isVisible());
		optionSepa.setVisible(optionButton.isVisible());
		//
		menuMan.setMenuAppState( appState );
		popMan.setMenuAppState( appState );
		//
		if( ia == cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.EDIT ) {
			stopButton.setText("Quick");
			infoButton.setText("Ingest");
		}
		else {
			stopButton.setText("Stop");
			infoButton.setText("Info");
		}
	}
	
	//-----------------------------------------------------------------------
	public void doClickImageButton()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("Image");
		robotMode=false;
		switchToImageMode(true);
		doe_load(null);
	}
	//-----------------------------------------------------------------------
	public void doClickImageButtonParam(String FNaam)
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("Image");
		robotMode=false;
		switchToImageMode(true);
		doe_load(FNaam);
	}
	//-----------------------------------------------------------------------
	public void doClickExtractButton()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("Extract");
		robotMode=true;     // This is what kick off the extraction
		switchToImageMode(false);
		scanMode = false;
		if( this.scanCheckBox.isSelected() ) {
			doe_extract();
		}
		else {
		 String FNaam=null;
		 if( lastImageFileName != null ) {
			  boolean ib = yesNoDialog("Do you want to extract text from current Image" , lastImageFileName );
			  if( ib ) FNaam = lastImageFileName;
		 }
		 //
		 doe_load(FNaam);
		}
	}
	
	//-----------------------------------------------------------------------
	public void doClickEditButton()
	//-----------------------------------------------------------------------
	{
		clickOnEdit(null);
	}
	//-----------------------------------------------------------------------
	public void doClickRefreshButton()
	//-----------------------------------------------------------------------
	{
		switch( appState )
		{
		case EDIT            : { requestTask( cmcProcSemaphore.TaskType.DO_EDITOR_IMAGE ); break; }
		default :  break; 
		}
	}
	//-----------------------------------------------------------------------
	public void doClickInfoButtonParam(int x,int y)
	//-----------------------------------------------------------------------
	{
		switch( appState )
		{
		case IDLE            : { popMessage( "Application : "+xMSet.getApplicDesc() + "\nProject : " + xMSet.getProjectName() + "\nDescription : " + xMSet.getProjectDescription()); break; }
		case IMAGE           : { do_infodialogs( x , y); break; }
		case EXTRACTED       : { do_infodialogs( x , y ); break; }
		case EDIT            : { doDetailedEdit(); break; }
		default :  break; 
		}
	}
	//-----------------------------------------------------------------------
	public void doClickInfoButton()
	//-----------------------------------------------------------------------
	{
	   doClickInfoButtonParam(0,0);	
	}
	//-----------------------------------------------------------------------
	public void doClickOCRButton()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("OCR");
		// Stop file
		this.resetInterrupt();
		//
		if( this.scanCheckBox.isSelected() ) {
			do_log(1,"Bulk OCR");
			cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRootDir(),true);
			String sDir = fc.getAbsoluteFilePath();
			if( sDir ==  null ) sDir = "No folder specified";
			if( xMSet.xU.IsDir(sDir) == false ) {
				popMessage("[" + sDir + "] is not a valid folder");
				return;
			}
			xMSet.createOCRScanList(sDir);
			int aantal = xMSet.getScanListSize();
			if( aantal <= 0 ) {
				this.scanCheckBox.setSelected(false);
				popMessage("There are no valid archive files present in folder [" + sDir + "]");
			}
			else {
				 robotMode=true;
				 switchToImageMode(false);
				 scanMode=true;
				 runCompleted=true; // triggert de start
				 //
				 this.startMonitor(sDir);
			}
			return;
		}
		//
		// Single file
		robotMode=true;
		scanMode=false;
		switchToImageMode(false);
		String FNaam=null;
		// Select archive - verify presence and check whether ZIP
	    if( FNaam == null ) {
	    	cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getArchiveDir(),false);
			FNaam = fc.getAbsoluteFilePath();
			if( FNaam == null ) return;
		}
		if( xMSet.xU.IsBestand(FNaam) == false ) {
			popMessage("File [" + FNaam + "] could not be found");
			return;
		}
	    String sfx = xMSet.xU.GetSuffix(FNaam);
	    if( sfx.trim().compareToIgnoreCase("ZIP") != 0 ) {
	    	popMessage("File [" + FNaam + "] is not an archive");
			return;
	    }
	    xMSet.setCurrentArchiveFileName(FNaam);
		requestTask( cmcProcSemaphore.TaskType.PREPARE_OCR ); 
	}
	//-----------------------------------------------------------------------
	public void doClickTranslateButton()
	//-----------------------------------------------------------------------
	{
		switchToImageMode(false);
		popMessage("TRANSLATE is under construction");
	}
	//-----------------------------------------------------------------------
	public void doClickReportButton()
	//-----------------------------------------------------------------------
	{
	   switchToImageMode(false);
	   String sFile = selecteerArchief();
	   if( sFile == null ) return;
	   if( xMSet.xU.IsBestand(sFile) == false ) return;
	   openFileInBrowser(sFile);
	}
	//-----------------------------------------------------------------------
	public void doClickStatisticsButton()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo("Statistics");
		switchToImageMode(false);
		disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.STATSGATHERING);
		process_stats();
	}
	//-----------------------------------------------------------------------
	public void doClickReInjectButton()
	//-----------------------------------------------------------------------
	{
		switchToImageMode(false);
		//popMessage("Re-Inject is under construction");
	    prepareBubble();
	}
	
	//-----------------------------------------------------------------------
	public void doTesseractOptionDialog()
	//-----------------------------------------------------------------------
	{
		switchToImageMode(false);
		cmcTesseractOptionDialog to = new cmcTesseractOptionDialog( frame , xMSet , logger);
	}
	//-----------------------------------------------------------------------
	public void doClickImageFilterPrepareAndRun()
	//-----------------------------------------------------------------------
	{
	 String drop = imageProcessingComboBox.getSelectedItem().toString();
   	 if (drop == null ) return;
   	 String keuze=null;
   	 if( drop.toUpperCase().trim().indexOf("ORIGINAL")>=0) keuze ="ORIGINAL";
   	 if( drop.toUpperCase().trim().indexOf("INFO")>=0) keuze ="INFO";
   	 if( drop.toUpperCase().trim().indexOf("GRAYSCALE")>=0) keuze="GRAYSCALE";
   	 if( drop.toUpperCase().trim().indexOf("MONOCHROME (OTSU)")>=0) keuze="BINARIZE_OTSU";
   	 if( drop.toUpperCase().trim().indexOf("BLUEPRINT")>=0) keuze="BLUEPRINT";
   	 if( drop.toUpperCase().trim().indexOf("MAINFRAME")>=0) keuze="MAINFRAME";
  	 if( drop.toUpperCase().trim().indexOf("MONOCHROME (NIBLAK)")>=0) keuze="BINARIZE_NIBLAK";
   	 if( drop.toUpperCase().trim().indexOf("MONOCHROME (SAUVOLA)")>=0) keuze="BINARIZE_SAUVOLA";
   	 if( drop.toUpperCase().trim().indexOf("INVERT")>=0) keuze="INVERT";
   	 if( drop.toUpperCase().trim().indexOf("BLEACHED")>=0) keuze="BLEACHED";
   	 if( drop.toUpperCase().trim().indexOf("HISTOGRAM")>=0) keuze="HISTOGRAM_EQUALISATION";
   	 //
   	 if( drop.toUpperCase().trim().indexOf("CONVOLUTION EDGE")>=0) keuze="CONVOLUTION_EDGE";
   	 if( drop.toUpperCase().trim().indexOf("CONVOLUTION GAUSSIAN")>=0) keuze="CONVOLUTION_GAUSSIAN";
   	 if( drop.toUpperCase().trim().indexOf("CONVOLUTION SHARPEN")>=0) keuze="CONVOLUTION_SHARPEN";
   	 if( drop.toUpperCase().trim().indexOf("CONVOLUTION GLOW")>=0) keuze="CONVOLUTION_GLOW";
	 if( drop.toUpperCase().trim().indexOf("SOBEL")>=0) {
		 if( drop.toUpperCase().trim().indexOf("GRAY")>=0) keuze = "SOBEL_GRAYSCALE";
		 else keuze="SOBEL";
	 }
	 if( drop.toUpperCase().trim().indexOf("GRADIENT")>=0) {
		 if( drop.toUpperCase().trim().indexOf("NARROW")>=0) keuze = "GRADIENT_NARROW";
		 else keuze="GRADIENT_WIDE";
	 }
	
	 //
   	 if( keuze == null ) {
   		 do_error("Could not determine image filter [" + drop + "]" );
   		 return;
   	 }
   	 doClickImageFilterButton(keuze);
    }
	//-----------------------------------------------------------------------
	public void doClickImageFilterButton(String sIn)
	//-----------------------------------------------------------------------
	{
		imageFilterSelectie=sIn;
		disable_controls(cbrTekStraktorModel.cmcProcEnums.APPLICATION_STATE.IMAGELOADING);
    	requestHistogram = true;
   	    requestImageProcessing=true;
	}
	//-----------------------------------------------------------------------
	public void doClickOptionsButton()
	//-----------------------------------------------------------------------
	{
	    cmcEditOptionDialog eo = new cmcEditOptionDialog( frame , xMSet , gCon , logger);
	    if( (xMSet.getDialogCompleteStatus()) && (drawmode) ) {
			 requestTask( cmcProcSemaphore.TaskType.DO_EDITOR_IMAGE ); // OK has been pressed so refresh
	    }
	}
	//-----------------------------------------------------------------------
	private void pushRecentStack(String FNaam , char tipe)
	//-----------------------------------------------------------------------
	{
		if( tipe == 'F' ) lastImageFileName = FNaam;
		// if on stack ignore
		for(int i=0;i<arFileStack.length;i++)
		{
			if( arFileStack[i] == null ) continue;
			if( arFileStack[i].compareToIgnoreCase(FNaam) == 0) return;
		}
		for(int i=arFileStack.length-1;i>=1;i--)
		{
			arFileStack[i] = arFileStack[i-1];
		}
		arFileStack[0] = FNaam;
        if( menuMan == null ) return;	
        menuMan.setRecent( arFileStack );
	}
	//-----------------------------------------------------------------------
	public void doRecent(String sNum )
	//-----------------------------------------------------------------------
	{
	   int idx = xMSet.xU.NaarInt(sNum);
	   if( (idx < 0) || (idx >= arFileStack.length)) return;
	   String FNaam = arFileStack[idx];
	   if( FNaam == null ) return;
	   if( xMSet.xU.IsBestand(FNaam) == false ) return;
	   doClickImageButtonParam(FNaam);
	}
	//-----------------------------------------------------------------------
	public void doClose()
	//-----------------------------------------------------------------------
	{
		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}
	//-----------------------------------------------------------------------
	public void doHousekeeping()
	//-----------------------------------------------------------------------
	{
		processVerwijderLijst(true);
	}
	
	private void pushRecentImageDir(String FNaam)
	{
		 pushRecentDir(FNaam,'I');
	}
	private void pushRecentArchiefDir(String FNaam)
	{
		pushRecentDir(FNaam,'Z');
	}
	private void pushRecentSaveDir(String FNaam)
	{
		pushRecentDir(FNaam,'S');
	}
	private void pushRecentDir(String FNaam , char tipe)
	{
		String sDir = xMSet.xU.GetParent(FNaam);
		if( xMSet.xU.IsDir( sDir ) == false ) return;
		switch( tipe )
		{
		case 'Z' : { xMSet.setRecentArchiveDir(sDir); return; }
		case 'I' : { xMSet.setRecentImageDir(sDir); return; }
		case 'S' : { xMSet.setRecentSaveDir(sDir); return; }
		default : { System.err.println("pushRecentDir unsupported option [" + tipe + "]"); break; }
		}
	}

	//-----------------------------------------------------------------------
	private void stophasbeenpressed()
	//-----------------------------------------------------------------------
	{
		//System.out.println("STP" + this.appState);
		// quick edit button on menubar
		if( appState == cmcProcEnums.APPLICATION_STATE.EDIT ) {
			doQuickEdit();
			return;
		}
		requestInterrupt();
	}
	
	
	//-----------------------------------------------------------------------
	public void doQuickEdit()
	//-----------------------------------------------------------------------
	{
		if( appState != cmcProcEnums.APPLICATION_STATE.EDIT ) {
			System.err.println("Not in edit mode");
			return;
		}
		/*
		// check whether there are uncommitted changes
		if( gCon.getNumberOfChanges() > 0 ) {
			boolean ib = yesNoDialog("There are uncommitted changes. These will not be visible until you save the changes" ,  "");
		}
		*/
		String FNaam = gCon.getZipFileName();
		cmcTableEditDialog cmet = new cmcTableEditDialog(frame , xMSet , FNaam , gCon , logger);
		if( cmet.isDialogOk() == false ) {
			popMessage("Could not correctly initialize the Quick Edit Dialog");
		}
		else {
			toonEditStatus();
		}
		cmet=null;
	}
	
	//-----------------------------------------------------------------------
	public void doDetailedEdit()
	//-----------------------------------------------------------------------
	{
		if( appState != cmcProcEnums.APPLICATION_STATE.EDIT ) {
			do_error("doDetailedEdit - Not in edit mode");
			return;
		}
		int idx =gCon.getSelectedParagraph(this.currentPixel.x,currentPixel.y);
		if( idx < 0 ) idx=gCon.getFirstLetterIdx();
		if( idx < 0 ) {
			do_error("doDetailedEdit - Cannot find a paragraph");
			return;
		}
		doeParagraphDialog(idx);
	}

	//-----------------------------------------------------------------------
	private void do_shutdown()
	//-----------------------------------------------------------------------
	{
    	Rectangle r = frame.getBounds();
    	xMSet.writePropertiesFile(r.x,r.y,r.width,r.height, arFileStack );
    	// purge
    	xMSet.purgeDirByName(xMSet.getTempDir(),true);
    	// loggers
    	//do_log(0,"REQUEST GRACEFULL SHUTDOWN");
    	
    	if( logger != null ) logger.close();
    	if( logger2nd != null ) logger2nd.close();
    	if( logDaemon != null ) logDaemon.close();
    	//
        System.exit(0);
	}
	
	//-----------------------------------------------------------------------
	private void toonEditStatus()
	//-----------------------------------------------------------------------
	{
		DelayedCheck=0;
		/*
	    do_log(9,"Dialog end ==>Number of changes = "  + gCon.getNumberOfChanges());
		if( gCon.getNumberOfChanges() <= 0 ) return;
		btnEdit.setBackground(Color.GREEN);
		*/
	}
	
	//-----------------------------------------------------------------------
	private void doe_show_ocr_file()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo( xMSet.getOCRResultFile());
		doe_fastload(xMSet.getOCROutputImageFileName()); // merely runs the Image.preload
		robotMode=true;  // just to force the correct state in the timer process
		requestTask( cmcProcSemaphore.TaskType.SHOW_OCR_FILE );	// performs actual displaying of OCR image
	}
	
	//-----------------------------------------------------------------------
	private void doe_tesseract()
	//-----------------------------------------------------------------------
	{
			robotMode=true;  // just to force the correct state in the timer process
			requestTask( cmcProcSemaphore.TaskType.RUN_TESSERACT );	
	}
	
	//-----------------------------------------------------------------------
	private void process_ocr_result()
	//-----------------------------------------------------------------------
	{
		toonStatusInfo( xMSet.getOCRSummaryResult());
		if( scanMode == true ) return; // do not display when in Bulk mode
		cmcTableEditDialog ted = new cmcTableEditDialog( frame , xMSet , xMSet.getCurrentArchiveFileName()  , null , logger );
		ted=null;
	}
	
	//-----------------------------------------------------------------------
	private void do_shout()
	//-----------------------------------------------------------------------
	{
		String msg = gotInterrupt() ? "Stopped on user request" :"Something went wrong. Please consult the Error Log File [" + xMSet.getErrFileName() + "]";
		popMessage(msg);
	}

	//-----------------------------------------------------------------------
	private void setTitle()
	//-----------------------------------------------------------------------
	{
	frame.setTitle(xMSet.getApplicDesc() + " [Project : " + xMSet.xU.getFolderOrFileName(xMSet.getRootDir()) + "]");
	}
	
	//-----------------------------------------------------------------------
	private void doProject(String tipe)
	//-----------------------------------------------------------------------
	{
		cmcProjectDialog pp = new cmcProjectDialog( frame , xMSet , logger , tipe);
		setTitle();
	}

	//-----------------------------------------------------------------------
	public boolean requestInterrupt()
	//-----------------------------------------------------------------------
	{
		gpInterrupt irq = new gpInterrupt( xMSet.getInterruptFileName() );
		boolean ib = irq.requestInterrupt();
        irq = null;
        if( ib == false ) do_error("Could not set interrupt"); else do_log(1,"Stop requested");
        return ib;
	}
	//-----------------------------------------------------------------------
	private boolean resetInterrupt()
	//-----------------------------------------------------------------------
	{
			gpInterrupt irq = new gpInterrupt( xMSet.getInterruptFileName() );
			boolean ib = irq.resetInterrupt();
	        irq = null;
	        if( ib == false ) do_error("Could not reset interrupt");
	        return ib;
	}
	//-----------------------------------------------------------------------
	private boolean gotInterrupt()
	//-----------------------------------------------------------------------
	{
				gpInterrupt irq = new gpInterrupt( xMSet.getInterruptFileName() );
				boolean ib = irq.gotInterrupt();
		        irq = null;
		        return ib;
	}
	
	//-----------------------------------------------------------------------
	private void doEditPopUpToText()
	//-----------------------------------------------------------------------
	{
		doEditPopUpTextChanges(true);
	}
	//-----------------------------------------------------------------------
	private void doEditPopUpToNoText()
	//-----------------------------------------------------------------------
	{
		doEditPopUpTextChanges(false);
	}
	//-----------------------------------------------------------------------
	private void doEditPopUpTextChanges(boolean ToText)
	//-----------------------------------------------------------------------
	{
		if( this.drawmode == false ) return;
		boolean ib = gCon.updateTextIndicatorViaXY( currentPixel.x , currentPixel.y , ToText);
		if( ib == false ) popMessage("Could not set the textindicator on this object");
		imgPanel.repaint();
		toonEditStatus();
	}
	//-----------------------------------------------------------------------
	private void doEditPopUpToDelete()
	//-----------------------------------------------------------------------
	{
		if( this.drawmode == false ) return;
		boolean ib = gCon.removeViaXY(currentPixel.x , currentPixel.y);
		if( ib == false ) popMessage("Could not remove object");
		imgPanel.repaint();
		toonEditStatus();
	}

	//-----------------------------------------------------------------------
	private void startMonitor(String FolderName)
	//-----------------------------------------------------------------------
	{
		if( moniControl !=null ) moniControl.startMonitor(FolderName);
	}
	//-----------------------------------------------------------------------
	private void syncMonitorEnd()
	//-----------------------------------------------------------------------
	{
		if( moniControl !=null ) moniControl.syncMonitorEnd(BulkFileName);
	}
	//-----------------------------------------------------------------------
	private void syncMonitor()
	//-----------------------------------------------------------------------
	{
		if( moniControl !=null ) moniControl.syncMonitor();
	}
	//-----------------------------------------------------------------------
    public void doClickMonitor()
	//-----------------------------------------------------------------------
    {
       startMonitor(null);
    }
	//-----------------------------------------------------------------------
    private void doClickArchiveDialog(boolean enableZap)
	//-----------------------------------------------------------------------
    {
    	gpFileChooser jc = new gpFileChooser(xMSet.getArchiveDir());
		jc.setFilter("ARCHIVE");
		if( enableZap ) jc.setOptions(new String[] { "Explore the archive files", "Examine the objects extracted" , "Examine the text extracted" , "Zap an archive" });
        jc.runDialog();
        String FileName = jc.getAbsoluteFilePath();
        String ActionTipe = jc.getAction();
        if(  (FileName==null) || (ActionTipe==null) ) return;
        //
        if( ActionTipe.toUpperCase().startsWith("ZAP") == true) {
        	boolean ib = yesNoDialog("Do you want to delete [" + FileName + "]" , "Zap archive");
        	if( ib ) {
        		xMSet.xU.VerwijderBestand(FileName);
        		if( xMSet.xU.IsBestand(FileName) ) {
        			popMessage("Could not remove [" + FileName + "]");
        		}
        	}
        	return;
        }
        else
        if( ActionTipe.toUpperCase().startsWith("EXPLORE") == true ) {
        	extractAndBrowse(FileName);
        	return;
        }	
        if( ActionTipe.toUpperCase().startsWith("EXAMINE") == true ) {
        	if( ActionTipe.toUpperCase().indexOf("OBJECTS") >= 0 ) {
        		extractFileFromArchiveAndDisplay(FileName , "_stat.xml");
        	}
        	else {
        		extractFileFromArchiveAndDisplay(FileName ,"_lang.xml");	
        	}
        }
        else {
        	popMessage("System error - unsupported option choosen [" + ActionTipe + "]");
        }
    }
	//-----------------------------------------------------------------------
    private void extractAndBrowse(String ZipFileName)
	//-----------------------------------------------------------------------
    {
    	// extract to temp
    	cmcArchiveDAO xao = new cmcArchiveDAO(xMSet,logger);
    	if( xao.unzipFullArchive(ZipFileName) == false ) {
    		popMessage("Could not unzip [" + ZipFileName + "]");
    		return;
    	}
    	xao=null;
    	// open dialog and lock 
    	gpFileChooser jc = new gpFileChooser(xMSet.getTempDir());
    	jc.setOpenButtonText("View");
        jc.setLockToFolder();  // restrict to current folder
	    jc.runDialog();
        String FileName = jc.getAbsoluteFilePath();
        if(  FileName==null ) {
        	xMSet.purgeDirByName( xMSet.getTempDir() , false );
        	return;
        }
        // remove temp except the choosen one
        xMSet.purgeDirByNameButKeep( xMSet.getTempDir() , FileName , false );
        viewFileViaBrowser(FileName);    
    }
	//-----------------------------------------------------------------------
	private void extractFileFromArchiveAndDisplay(String FileName ,String reqSfx)
	//-----------------------------------------------------------------------
	{
		String requestFileName = xMSet.xU.getFolderOrFileName( FileName );
		requestFileName = xMSet.xU.RemplaceerIgnoreCase( requestFileName , "_set.zip" , reqSfx );
	 	// extract to temp
    	cmcArchiveDAO xao = new cmcArchiveDAO(xMSet,logger);
    	String ret = xao.unzip_SingleFile( FileName, requestFileName);
    	xao = null;
    	if(  ret == null ) {
    		popMessage("Could not extract [" + requestFileName + "] from [" + FileName + "]");
    		return;
    	}
    	String sTemp = xMSet.getTempDir() + xMSet.xU.ctSlash + requestFileName;
    	if( xMSet.xU.IsBestand( sTemp ) == false ) {
    		popMessage("Could not find [" + sTemp + "] extracted from [" + FileName + "]");
    		return;
    	}
    	do_log(1,"Showing [" + sTemp + "] from [" + FileName + "]");
        viewFileViaBrowser(sTemp);    
    }
    //-----------------------------------------------------------------------
    public void doClickArchiveBrowser()
	//-----------------------------------------------------------------------
    {
    	doClickArchiveDialog(true);
    }
	//-----------------------------------------------------------------------
    public void doClickArchiveViewer()
	//-----------------------------------------------------------------------
    {
    	doClickArchiveDialog(false);
    }
	//-----------------------------------------------------------------------
    private void viewFileViaBrowser(String FileName)
	//-----------------------------------------------------------------------
    {
    	openFileInBrowser(FileName);
    }
	//-----------------------------------------------------------------------
    private void doExportText()
	//-----------------------------------------------------------------------
    {
    	// Stop file
   		this.resetInterrupt();
   		//
   		cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRootDir(),true);
    	String sDir = fc.getAbsoluteFilePath();
    	if( sDir ==  null ) sDir = "No folder specified";
    	if( xMSet.xU.IsDir(sDir) == false ) {
    		popMessage("[" + sDir + "] is not a valid folder");
    		return;
    	}
    	xMSet.createOCRScanList(sDir);
    	int aantal = xMSet.getScanListSize();
    	if( aantal <= 0 ) {
    		this.scanCheckBox.setSelected(false);
    		popMessage("There are no valid archive files present in folder [" + sDir + "]");
    	}
    	else {
    	 // remove target file
    	 if( xMSet.xU.IsBestand(xMSet.getTextReportName()) ) {
    		 xMSet.xU.VerwijderBestand(xMSet.getTextReportName());
    	 }
		 requestTask( cmcProcSemaphore.TaskType.EXTRACT_ALL_TEXT );
		 robotMode=false;
		 runCompleted=true; // triggert de start
		}
		return;
    }
    
    private void doEndOfTextExport()
    {
   	    if( xMSet.xU.IsBestand(xMSet.getTextReportName())==false ) {
   	    	do_error("Report export file not found [" + xMSet.getTextReportName() + "]");
   	    	return;
   	    }
   	    try {
    	  cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRecentSaveDir(),false);
		  String FName = fc.getAbsoluteFilePath();
		  if( FName == null ) return;
		  // if no suffix add txt
		  if ( FName.indexOf(".") < 0 ) {
			  FName = FName + ".txt";
		  }
		  if( xMSet.xU.IsBestand(FName) ) {
			if( yesNoDialog("The file you defined already exists. Overwrite?", FName) == false ) return;
		  }
		  // kopieer
		  do_log(5,"Moving to [" + FName + "]");
		  xMSet.xU.copyFile( xMSet.getTextReportName() , FName );
		  if( xMSet.xU.IsBestand( FName ) == false ) {
			  do_error("Could not create file [" + FName + "]");
		  }
   	    }
   	    catch( Exception e ) {
   	    	do_error("doEndOfTextExport " + e.getMessage());
   	    	return;
   	    }
   	    finally {
   	      do_log(5,"Removing [" + xMSet.getTextReportName() + "]");
   		  xMSet.xU.VerwijderBestand(xMSet.getTextReportName());
	    }
		
    }
    
    private void doImportText()
    {
     	// Stop file
   		this.resetInterrupt();
   		//
   		cmcProcFileChooser fc = new cmcProcFileChooser(xMSet.getRootDir(),false);
    	String sFile = fc.getAbsoluteFilePath();
    	if( sFile ==  null ) sFile = "No file specified";
    	if( xMSet.xU.IsBestand(sFile) == false ) {
    		popMessage("[" + sFile + "] is not a valid file");
    		return;
    	}
        do_error( sFile ) ;
        //
        xMSet.createImportScanList(sFile);
    	int aantal = xMSet.getScanListSize();
    	if( aantal <= 0 ) {
    		this.scanCheckBox.setSelected(false);
    		popMessage("There are no valid archive files present in file [" + sFile + "]");
    	}
    	else {
    	   	 requestTask( cmcProcSemaphore.TaskType.IMPORT_ALL_TEXT );
    		 robotMode=false;
    		 runCompleted=true; // triggert de start
    	}
    }
    
    // experimental
    private void prepareBubble()
    {
    	this.popMessage("Re-insert is currenlty not supported (05 June)");
    	/*
     	gpFileChooser jc = new gpFileChooser(xMSet.getArchiveDir());
    	jc.setFilter("ARCHIVE");
    	jc.runDialog();
        String FileName = jc.getAbsoluteFilePath();
        if( FileName==null ) return;
        //
        cmcBubbleMaker bubmak = new cmcBubbleMaker(xMSet,logger);
        bubmak.initialize( FileName );
        */
    }
    
    //-----------------------------------------------------------------------
  	public void doTesseractVersionDialog()
  	//-----------------------------------------------------------------------
  	{
  		cmcOCRController ocro = new cmcOCRController( xMSet , logger);
  		String sversion = ocro.getTesseractVersion();
  		ocro = null;
  		this.popMessage( sversion);
  	}
    
}
