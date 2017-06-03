import generalImagePurpose.cmcBulkImageRoutines;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;








import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcConstants;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphController;


public class cmcParagraphEditorDialog {

	
	cmcProcSettings    xMSet = null;
    cmcGraphController gCon=null;
    cmcProcEnums       cenums = null;
    cmcBulkImageRoutines bulk = null;
    logLiason logger = null;

    private Font editFont = new Font("Serif", Font.ITALIC, 14);
    private Color mainColor = Color.WHITE;

    static JLabel plaatjeMidden = null;
    static JCheckBox istext = null;
    static JLabel paraInfo = null;
    static JLabel plaatjeLinks = null;
    static JLabel plaatjeRechts = null;
    static JTextArea textArea = null;
    static JTextArea transArea = null;
    static JComboBox origTaal = null;
    static JComboBox verTaal = null;
    static JButton closeButton = null;
    static JButton saveButton = null;
    static JCheckBox checker =null;
    
    private static int EDITORBREEDTE = 700;
	private static int MAXB = EDITORBREEDTE - 50;
	private static int MAXH = MAXB / 4;
    private BufferedImage bimg=null;
    private BufferedImage dummyImageLeft=null;
    private String LEGELIJN = "            ";
    private int ParagraphIdx = -1;
    private boolean textOrigChanged=false;
    private boolean textTransChanged=false;
    private boolean checkChanged=false;
    
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
	cmcParagraphEditorDialog(JFrame jf , cmcProcSettings iM , cmcGraphController ig , int iPix , logLiason ilog )
	//-----------------------------------------------------------------------
	{
		xMSet = iM;
		gCon = ig;
		ParagraphIdx = iPix;
		logger = ilog;
		mainColor = cmcProcConstants.DEFAULT_LIGHT_GRAY;
		cenums = new cmcProcEnums(xMSet);
		xMSet.setDialogCompleteState(false);
		if( extractTextParagraph(ParagraphIdx) == false ) {
			do_error("Could not extract text paragraph from image");
			return;
		}
		maakDummyImages();
		initialize(jf);
	}
	
	    //---------------------------------------------------------------------------------
		private void initialize(JFrame jf)
		//---------------------------------------------------------------------------------
		{
		Color kleur = mainColor;
		try
	        {
	            final JDialog dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
	            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	            dialog.setTitle( gCon.getTitle());
	            dialog.setLocationRelativeTo( jf );
	            dialog.setLocation(10,10);
	            //
	            Font font = xMSet.getPreferredFont();
	            //
	            JPanel koepelPane = new JPanel();
	            koepelPane.setBackground(kleur);
	            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
	            //
	            // Lege pane bovenaan
	            JPanel sepaPaneTop = new JPanel();
	            sepaPaneTop.setBackground(kleur);
	            paraInfo = new JLabel();
	            paraInfo.setText(gCon.getDimensionInfo(ParagraphIdx));
	            paraInfo.setBackground(kleur);
	            paraInfo.setFont(font);
	            sepaPaneTop.add(paraInfo);
	            koepelPane.add(sepaPaneTop);
	            
	           
	            // Plaatjes Pane
	            JPanel imaPane = new JPanel();
                imaPane.setBackground(kleur);
                imaPane.setLayout(new BoxLayout(imaPane, BoxLayout.X_AXIS));
    		    
                // Dummy plaatje om de hoogte van plaatjes te locken
    		    plaatjeLinks = new JLabel();
    		    plaatjeLinks.setBackground(kleur);
    		    plaatjeLinks.setIcon(new ImageIcon(dummyImageLeft));
    		    plaatjeRechts = new JLabel();
    		    plaatjeRechts.setBackground(kleur);
     		    plaatjeRechts.setIcon(new ImageIcon(dummyImageLeft));
	            //
	            JPanel plaatjesPane = new JPanel();
                plaatjesPane.setBackground(kleur);
                //plaatjesPane.setBorder(BorderFactory.createLineBorder(Color.black));
    		    plaatjesPane.setLayout(new BoxLayout(plaatjesPane, BoxLayout.X_AXIS));
    		    // Images
    		    plaatjeMidden = new JLabel();
    		    plaatjeMidden.setIcon(new ImageIcon(bimg));
    		 	plaatjesPane.add(plaatjeMidden);
    		    //
    		 	imaPane.add(plaatjeLinks);
    			imaPane.add(plaatjesPane);
    			imaPane.add(plaatjeRechts);
    			koepelPane.add(imaPane);
    			
    			
    			// Lege pane juist onder de image
	            JPanel sepaPaneIma = new JPanel();
	            sepaPaneIma.setBackground(kleur);
	            JLabel d1 = new JLabel();
	            d1.setText(LEGELIJN);
	            d1.setBackground(kleur);
	            sepaPaneTop.add(d1);
	            koepelPane.add(sepaPaneIma);
	            
    			// Panes met info
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
	          
	            //  Links Content
	            istext = new JCheckBox();
	            istext.setEnabled(true);
	            istext.setSelected( gCon.getIsTextParagraph(ParagraphIdx) );
	            istext.setBackground(kleur);
	            istext.addItemListener(new ItemListener() {
		            public void itemStateChanged(ItemEvent e) {
		              swapIsText();
		            }
		         });
	            leftPane.add(istext);
	           
	            // Rechts Content
	            JLabel rtekst = new JLabel("Is a text paragraph");
	            rtekst.setVisible(true);
	            rtekst.setFont(font);
	            rtekst.setBackground(kleur);
	            rtekst.setHorizontalAlignment(SwingConstants.LEFT);
		        rightPane.add(rtekst);
	            
		        // 
		        JPanel dropBovenPane = new JPanel();
	            dropBovenPane.setBackground(kleur);
	            dropBovenPane.setLayout(new BoxLayout(dropBovenPane, BoxLayout.X_AXIS));
	            //
	            origTaal = new JComboBox();
	            origTaal.setFont(font);
	            String lijst[] = xMSet.getLanguageList();
			    for(int j=0;j<lijst.length;j++) origTaal.addItem( lijst[j].trim() );
	    		int k = xMSet.xU.getIdxFromList( lijst , gCon.getOriginalLanguage() );
	    		if( k >= 0 ) origTaal.setSelectedIndex(k);
	    		origTaal.addActionListener(new ActionListener(){
		            public void actionPerformed(ActionEvent e){
		              	showTexts(true);
		            }
		        });
		    	dropBovenPane.add(origTaal);
		    	//
		    	JLabel dropLeeg = new JLabel(LEGELIJN);
	            dropLeeg.setVisible(true);
	            dropLeeg.setFont(font);
	            dropLeeg.setBackground(kleur);
	            dropLeeg.setPreferredSize(new Dimension((EDITORBREEDTE * 3) / 4,25));
		        dropBovenPane.add(dropLeeg);
	            
		        // pane boven de drop
		        JPanel sepaPanetussen = new JPanel();
	            sepaPanetussen.setBackground(kleur);
	            JLabel d12 = new JLabel();
	            d12.setText(LEGELIJN);
	            d12.setBackground(kleur);
	            sepaPanetussen.add(d12);
	            
		        // 
		        JPanel dropOnderPane = new JPanel();
	            dropOnderPane.setBackground(kleur);
	            dropOnderPane.setLayout(new BoxLayout(dropOnderPane, BoxLayout.X_AXIS));
	            //
	            verTaal = new JComboBox();
	            verTaal.setFont(font);
	            for(int j=0;j<lijst.length;j++) verTaal.addItem( lijst[j].trim() );
	    		//k = xMSet.xU.getIdxFromList( lijst , gCon.getOriginalLanguage() );
	            k=0;
	    		if( k >= 0 ) verTaal.setSelectedIndex(k);
	    		verTaal.addActionListener(new ActionListener(){
		            public void actionPerformed(ActionEvent e){
		              	showTexts(false);
		            }
		        });
		    	
	        	dropOnderPane.add(verTaal);
		    	//
		    	JLabel dropLeeg2 = new JLabel(LEGELIJN);
	            dropLeeg2.setVisible(true);
	            dropLeeg2.setFont(dropLeeg.getFont());
	            dropLeeg2.setBackground(dropLeeg.getBackground());
	            dropLeeg2.setPreferredSize(dropLeeg.getPreferredSize());
		        dropOnderPane.add(dropLeeg2);
	            
		        // checkbox
		        checker = new JCheckBox("Monochrome picture");
		        checker.setSelected(xMSet.getuseMonoChromeInDialogs());
		        checker.setBackground(kleur);
		        checker.addItemListener(new ItemListener() {
		            public void itemStateChanged(ItemEvent e) {
		              swapColorMonochrome();
		            }
		          });
		        
	            // text
	            textArea = new JTextArea(gCon.getOriginalTextViaUID(gCon.getUID(ParagraphIdx)));
	            textArea.setFont(editFont);
	            textArea.setLineWrap(true);
	            textArea.setWrapStyleWord(true);
	            textArea.getDocument().addDocumentListener(new DocumentListener() {
	                @Override
	                public void removeUpdate(DocumentEvent e) { textOrigChanged=true; }
	                @Override
	                public void insertUpdate(DocumentEvent e) { textOrigChanged=true; }
	                @Override
	                public void changedUpdate(DocumentEvent arg0) { textOrigChanged=true; }
	            });
	            textArea.addFocusListener(new FocusListener() {
	                public void focusGained(FocusEvent e) {
	                  //
	                }
	                public void focusLost(FocusEvent e) {
	                  enableButtons();
	                }
	            });
	            
	            //
	            JScrollPane areaScrollPane = new JScrollPane(textArea);
	            areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	            areaScrollPane.setPreferredSize(new Dimension(EDITORBREEDTE,100));
	            /*
	            areaScrollPane.setBorder(
	                BorderFactory.createCompoundBorder(
	                    BorderFactory.createCompoundBorder(
	                                    BorderFactory.createTitledBorder("Paragraph text"),
	                                    BorderFactory.createEmptyBorder(5,5,5,5)),
	                    areaScrollPane.getBorder()));
	            */
	            areaScrollPane.setBorder(
		                BorderFactory.createCompoundBorder(
		                    BorderFactory.createCompoundBorder(
		                                    BorderFactory.createTitledBorder(""),
		                                    BorderFactory.createEmptyBorder(2,2,2,2)),
		                    areaScrollPane.getBorder()));
		            
	            //
	            // vertaling
	            transArea = new JTextArea( gCon.getTranslatedTextViaUID(gCon.getUID(ParagraphIdx) , xMSet.getLanguageCode(verTaal.getSelectedIndex(),true)));
	            transArea.setFont(editFont);
	            transArea.setLineWrap(true);
	            transArea.setWrapStyleWord(true);
	            transArea.getDocument().addDocumentListener(new DocumentListener() {
	                @Override
	                public void removeUpdate(DocumentEvent e) { textTransChanged=true; }
	                @Override
	                public void insertUpdate(DocumentEvent e) { textTransChanged=true; }
	                @Override
	                public void changedUpdate(DocumentEvent arg0) { textTransChanged=true; }
	            });
	            transArea.addFocusListener(new FocusListener() {
	                public void focusGained(FocusEvent e) {
	                  //
	                }
	                public void focusLost(FocusEvent e) {
	                  enableButtons();
	                }
	            });

	            //   
	            JScrollPane transScrollPane = new JScrollPane(transArea);
	            transScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
	            transScrollPane.setPreferredSize(new Dimension(EDITORBREEDTE,100));
	            transScrollPane.setBorder(
		                BorderFactory.createCompoundBorder(
		                    BorderFactory.createCompoundBorder(
		                                    BorderFactory.createTitledBorder(""),
		                                    BorderFactory.createEmptyBorder(2,2,2,2)),
		                    transScrollPane.getBorder()));
		        
	            /*
	            transScrollPane.setBorder(
	                BorderFactory.createCompoundBorder(
	                    BorderFactory.createCompoundBorder(
	                                    BorderFactory.createTitledBorder("Translated text"),
	                                    BorderFactory.createEmptyBorder(5,5,5,5)),
	                    transScrollPane.getBorder()));
	            */        
	            //
	            bovenPane.add(leftPane);
	            bovenPane.add(rightPane);
	            koepelPane.add(bovenPane);
	            koepelPane.add(dropBovenPane);
	            koepelPane.add(areaScrollPane );
	            koepelPane.add(sepaPanetussen);
		      	koepelPane.add(dropOnderPane );
	          	koepelPane.add(transScrollPane);
	         	
	            
	            // Lege pane juist bovenknop
	            JPanel sepaPaneknopboven = new JPanel();
	            sepaPaneknopboven.setBackground(kleur);
	            JLabel d8 = new JLabel();
	            d8.setText(LEGELIJN);
	            d8.setBackground(kleur);
	            sepaPaneknopboven.add(d8);
	            sepaPaneknopboven.add(checker);
	            //sepaPaneknopboven.add(d8);
	            koepelPane.add(sepaPaneknopboven);
	            
	            //
	            JPanel onderPane = new JPanel();
	            onderPane.setLayout(new BoxLayout(onderPane, BoxLayout.X_AXIS));
	            //
	            closeButton = new JButton("Close"); 
	            closeButton.setEnabled(false);
	            closeButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    				processTextChanges();
	    				xMSet.setDialogCompleteState(true);
	    			    dialog.dispose();
	    			}
	    		});
	            /*
	            JButton ocrButton = new JButton("OCR");
	            ocrButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  //dialog.dispose();
	    			}
	    		});
	            ocrButton.setEnabled(false);
	            */
	            JButton cancelButton = new JButton("Cancel");
	            cancelButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  dialog.dispose();
	    			}
	    		});
	            JButton prevButton = new JButton("Previous");
	            prevButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  processTextChanges();
	    			  doePrev();
	    			}
	    		});
	            JButton nextButton = new JButton("Next");
	            nextButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  processTextChanges();
	    			  doeNext();
	    			}
	    		});
	            saveButton = new JButton("Save");
	            saveButton.setEnabled(false);
	            saveButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			 processTextChanges();
	    			}
	    		});
	            JButton deleteButton = new JButton("Delete");
	            deleteButton.addMouseListener(new MouseAdapter() {
	    			@Override
	    			public void mouseClicked(MouseEvent arg0) {
	    			  gCon.removeViaIdx(ParagraphIdx);
	    			}
	    		});
	            //
	            prevButton.setFont(font);
	            closeButton.setFont(font);
	            saveButton.setFont(font);
		        //ocrButton.setFont(font);
		        deleteButton.setFont(font);
	            cancelButton.setFont(font);
	            nextButton.setFont(font);
	            //
	            onderPane.setBackground(closeButton.getBackground());
	            //
	          
	            onderPane.add(prevButton);
	            onderPane.add(closeButton);
	            //onderPane.add(saveButton);  // functionality of save was confusing
	            //onderPane.add(ocrButton);
	            onderPane.add(deleteButton);
		        onderPane.add(cancelButton);
	            onderPane.add(nextButton);
	            //
	            koepelPane.add(onderPane);
	            // Lege pane onder knop
	            JPanel sepaPaneknoponder = new JPanel();
	            sepaPaneknoponder.setBackground(kleur);
	            JLabel d9 = new JLabel();
	            d9.setText(LEGELIJN);
	            d9.setBackground(kleur);
	            sepaPaneknoponder.add(d9);
	            koepelPane.add(sepaPaneknoponder);
	    
	            // nu nog links en rechts een pane
	            JPanel linkerMarge = new JPanel();
	            linkerMarge.setBackground(kleur);
	            JLabel d10 = new JLabel();
	            d10.setText("iets");
	            d10.setForeground(kleur);
	            linkerMarge.add(d10);
	            
	            JPanel rechterMarge = new JPanel();
	            rechterMarge.setBackground(kleur);
	            JLabel d11 = new JLabel();
	            d11.setText("iets");
	            d11.setForeground(kleur);
	            rechterMarge.add(d11);
	          
	            JPanel superPane = new JPanel();
	            superPane.setBackground(kleur);
	            superPane.setLayout(new BoxLayout(superPane, BoxLayout.X_AXIS));
	    	    //   
	            superPane.add(linkerMarge);
	            superPane.add(koepelPane);
	            superPane.add(rechterMarge);
	            dialog.add(superPane);
	            
	            //
	            dialog.pack();
	            dialog.setLocationByPlatform(true);
	            dialog.setVisible(true);
	            //
	            plaatjeMidden.repaint();
	        } 
	        catch (Exception e) 
	        {
	            do_error( "System error" + e.getMessage() + " " + xMSet.xU.LogStackTrace(e)) ;
	        }
		}
		
		/*
		//-----------------------------------------------------------------------
		class CheckboxAction extends AbstractAction
		//-----------------------------------------------------------------------
		{
		    public CheckboxAction(String text) {
		        super(text);
		    }
		 
		    @Override
		    public void actionPerformed(ActionEvent e) {
		        JCheckBox cb = (JCheckBox) e.getSource();
		        gCon.updateTextIndicator(ParagraphIdx,cb.isSelected());
		    }
		}
		*/
		
		//-----------------------------------------------------------------------
		private void maakDummyImages()
		//-----------------------------------------------------------------------
		{
			int breedte = 10;
			int hoogte = MAXH + 10;
			int[] zixels = new int[breedte*hoogte];
			// background
			for(int i=0;i<zixels.length;i++) zixels[i] = mainColor.getRGB(); //0xffffffff;
			//
			if( bimg != null ) {
				int lijnKleur = ( gCon.getIsTextParagraph(ParagraphIdx) == true ) ? Color.GREEN.getRGB() : Color.RED.getRGB();
				int lijnHoogte = bimg.getHeight() + 10;
			    int j = (hoogte - lijnHoogte) / 2;
			    int k = j + lijnHoogte; 
			    if( j < 0 ) j =0;
			    if( k > hoogte ) k = hoogte;
			    for(int z=-1;z<2;z++) {
			     int midX = (breedte / 2) + z; 	
			     for(int i=j;i<k;i++) {
			    	zixels[ midX + (i*breedte)] = lijnKleur;
			     }
			    }
			}
			dummyImageLeft=null;
		    dummyImageLeft = new BufferedImage(breedte , hoogte, BufferedImage.TYPE_INT_RGB);
		    dummyImageLeft.setRGB(0, 0, breedte, hoogte, zixels, 0, breedte);
		}
		
		//-----------------------------------------------------------------------
		private boolean extractTextParagraph(int idx)
		//-----------------------------------------------------------------------
		{
			int[] pixels = gCon.getPixels(idx);
			if( pixels == null ) return false;
			//
			int width = gCon.getWidthViaIdx(idx);
			int heigth = pixels.length / width;
			if( (width<0) || (heigth<0) ) {
				System.err.println("Cannot determine width");
				return false;
			}
			// Black and white
			if( xMSet.getuseMonoChromeInDialogs() ) {
				if ( bulk == null ) bulk = new cmcBulkImageRoutines(xMSet,logger);
				int[] poxels = Arrays.copyOf(pixels, pixels.length);
				pixels = bulk.doNiblak(poxels, width, true);
				poxels=null;
			}
			bimg=null;
		    bimg = new BufferedImage(width, heigth, BufferedImage.TYPE_INT_RGB);
		    bimg.setRGB(0, 0, width, heigth, pixels, 0, width);
		    //
		    if( ( width > MAXB) || (heigth > MAXH) ) {
				double ratio = (double)width / (double)heigth;
				if( width > MAXB ) {
					double ho = (double)MAXB / ratio;
					bimg = doeResize( bimg , MAXB ,  (int)ho );
					width = bimg.getWidth();
					heigth = bimg.getHeight();
				}
				if( heigth > MAXH ) {
					double br = (double)MAXH * ratio;
					bimg = doeResize( bimg , (int)br ,  MAXH );
					width = bimg.getWidth();
					heigth = bimg.getHeight();
				}
			}
		    //
			return true;
		}
		
		//-----------------------------------------------------------------------
		public static BufferedImage doeResize(BufferedImage image, int width, int heigth)
		//-----------------------------------------------------------------------
		{
		    //System.err.println("resize " + width +" " + heigth + " MXH=" + MAXH);
		    BufferedImage bi = new BufferedImage(width, heigth, BufferedImage.TRANSLUCENT);
		    Graphics2D g2d = (Graphics2D) bi.createGraphics();
		    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
		    g2d.drawImage(image, 0, 0, width, heigth, null);
		    g2d.dispose();
		    return bi;
		}
		
		//-----------------------------------------------------------------------
		private void doePrev()
		//-----------------------------------------------------------------------
		{
			ParagraphIdx = gCon.getPrevIdx(ParagraphIdx);
			redoContent();
		}
		
		//-----------------------------------------------------------------------
		private void doeNext()
		//-----------------------------------------------------------------------
		{
			ParagraphIdx = gCon.getNextIdx(ParagraphIdx);
			redoContent();
  	    }
		
		//-----------------------------------------------------------------------
		private void redoContent()
		//-----------------------------------------------------------------------
		{
			if( extractTextParagraph(ParagraphIdx) == false ) return;
			plaatjeMidden.setIcon(new ImageIcon(bimg));
			istext.setSelected( gCon.getIsTextParagraph(ParagraphIdx) );
			paraInfo.setText(gCon.getDimensionInfo(ParagraphIdx) + " " + gCon.getUID(ParagraphIdx) );
	        plaatjeMidden.repaint();
	        maakDummyImages();
	        plaatjeLinks.setIcon(new ImageIcon(dummyImageLeft));
	        plaatjeRechts.setIcon(new ImageIcon(dummyImageLeft));
	        //
	        showTexts(false);
	        checkChanged=false;
	 	}

		//-----------------------------------------------------------------------
		private void showTexts(boolean originalLanguageChanged)
		//-----------------------------------------------------------------------
		{
			 if( originalLanguageChanged ) {
				 String sLang = xMSet.getLanguageCode(origTaal.getSelectedIndex(),true);
				 if( sLang == null ) sLang = "???";
				 int idx = xMSet.xU.getIdxFromList( xMSet.getLanguageList() , sLang );
				 if( idx >= 0 ) gCon.changeOriginalLanguage(sLang);
			 }
			 textArea.setText(gCon.getOriginalTextViaUID(gCon.getUID(ParagraphIdx)));
		     transArea.setText(gCon.getTranslatedTextViaUID(gCon.getUID(ParagraphIdx) , xMSet.getLanguageCode(verTaal.getSelectedIndex(),true)) );
		     textOrigChanged=false;
		     textTransChanged=false;
		}
	
		//-----------------------------------------------------------------------
		private void enableButtons()
		//-----------------------------------------------------------------------
		{
			if( (textOrigChanged == false) && (textTransChanged==false) && (checkChanged==false)) return;
			if( closeButton.isEnabled() ) return;
			closeButton.setEnabled(true);
			saveButton.setEnabled(true);
		}
		
		//-----------------------------------------------------------------------
		private void processTextChanges()
		//-----------------------------------------------------------------------
		{
			if( textOrigChanged ) {
			  textOrigChanged=false;
			  gCon.updateOrigTextViaUID(gCon.getUID(ParagraphIdx),textArea.getText());
			  enableButtons();
			}
			//
			if( textTransChanged ) {
			  textTransChanged=false;
			  enableButtons();
			  do_log(9,"Orig" + transArea.getText() );
			  gCon.updateTransTextViaUID(gCon.getUID(ParagraphIdx),transArea.getText(),xMSet.getLanguageCode(verTaal.getSelectedIndex(),true));

			}
		}
		
		//-----------------------------------------------------------------------
		private void swapIsText()
		//-----------------------------------------------------------------------
		{
			boolean ib = gCon.getIsTextParagraph(ParagraphIdx);
			if( istext.isSelected() == ib ) return; // no change in gCon
			//do_log(1,"Toggle [now=" + istext.isSelected() + "][prev=" + ib + "]");
			ib = gCon.updateTextIndicator(ParagraphIdx,istext.isSelected());
			if( ib == false ) {
				do_error("Could not update text indicator [" + ParagraphIdx + "]");
				return;
			}
			checkChanged=true;
			enableButtons();
		}
		
		//-----------------------------------------------------------------------
		private void swapColorMonochrome()
		//-----------------------------------------------------------------------
		{
			xMSet.setUseMonoChromeInDialogs(checker.isSelected());
			redoContent();
		}
}
