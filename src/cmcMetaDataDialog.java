import generalpurpose.gpUnZipFileList;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import cbrTekStraktorModel.comicPage;
import dao.cmcBookMetaDataDAO;


public class cmcMetaDataDialog {

	cmcProcSettings xMSet = null;
	cmcBookMetaDataDAO cmeta = null;
	cmcProcEnums  cenums = null;
	logLiason logger = null;
	//
	private JDialog dialog = null;
	private JPanel koepelPane = null;
	private JPanel plaatjesPane = null;
	//
	private final int iCMXUID     = 0;
	private final int iISBN       = 1;
	private final int iSERIES     = 2;
	private final int iSERIESSEQ  = 3;
	private final int iBOOK       = 4;
	private final int iPAGE       = 5;
	private final int iWRITER     = 6;
	private final int iPENCILLER  = 7;
	private final int iCOLOURER   = 8;
	private final int iCOMMENT    = 9;
	private final int iFOLDERNAME = 10;
	private final int iFILENAME   = 11;
	private final int iMETRICS    = 12;
	private final int iUID        = 13;
	// drop downs
	private final int iBLACKWHITE = 0;
	private final int iLANGUAGE   = 1;
	private final int iBINARIZE   = 2;
	private final int iCLUSTER    = 3;
	private final int iPROXIMITY  = 4;
	private final int iCROPPING   = 5;
	private final int iCURATION   = 6;
	//
	final JTextField arField[] = new JTextField[14];
	final JComboBox  arDropField[] = new JComboBox[7];
	final JTextField arLabel[] = new JTextField[arField.length + arDropField.length];
	private boolean removeExtracted=false;
    private boolean requestToCreateEstafetteFile=false;
    private int plaatjesPaneHoogte = -1;
    
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
	public cmcMetaDataDialog(JFrame jf , cmcProcSettings iM , comicPage cpi , boolean isInfo , logLiason ilog , boolean ibMakeEstafetteFile)
	//------------------------------------------------------------
	{
		xMSet = iM;
		logger = ilog;
		xMSet.setDialogCompleteState(false);
		xMSet.setMetadataHasBeenModified(false);
		cenums = new cmcProcEnums(xMSet);
		requestToCreateEstafetteFile=ibMakeEstafetteFile;
		cmeta = new cmcBookMetaDataDAO( xMSet , xMSet.getOrigImageLongFileName() , cpi , logger);
		initialize(jf,isInfo);
	}
	
	//------------------------------------------------------------
	private void initialize(JFrame jf,boolean isInfo)
	//------------------------------------------------------------
	{
		String sTitel = "Image Metadata Info " + suggestTitle();
        //
		try
        {
            dialog = new JDialog(jf,"",Dialog.ModalityType.DOCUMENT_MODAL);  // final voor de dispose   
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setTitle( sTitel );
            dialog.setLocationRelativeTo( jf );
            dialog.setLocation(10,10);
            //
            Font font = xMSet.getPreferredFont();  
            //
            koepelPane = new JPanel();
            koepelPane.setBackground(Color.WHITE);
            koepelPane.setLayout(new BoxLayout(koepelPane, BoxLayout.Y_AXIS));
            // indien info komen bovenaan plaatjes
            URL boxURL = null;
            URL histoURL = null;
            URL peakURL = null;
            removeExtracted=false;
            if( cmeta.isEmpty() ) {
            	do_log(1,"Metadata DAO is empty. Using the current images");
            	boxURL  = xMSet.xU.maakFileURL( xMSet.getBoxDiagramName() );
           	    histoURL= xMSet.xU.maakFileURL( xMSet.getTempJPGName() );
           	    peakURL = xMSet.xU.maakFileURL( xMSet.getReportPeakDiagramName() );
            }
            else {
             String ZipFNaam = xMSet.getReportZipName();
         	 if( (isInfo==true) && (xMSet.xU.IsBestand(ZipFNaam)==false) ) {  // only if there is no ZIP
             	 do_log(1,"informational and No zip found");
                 boxURL  = xMSet.xU.maakFileURL( xMSet.getBoxDiagramName() );
            	 histoURL= xMSet.xU.maakFileURL( xMSet.getTempJPGName() );
            	 peakURL = xMSet.xU.maakFileURL( xMSet.getReportPeakDiagramName() );
             }
             else { // extraheer dan de plaatjes uit de zip indien die bestaat
            	   if( xMSet.xU.IsBestand(ZipFNaam)) {
            		do_log(1,"Fetching from ZIP");
            		//String TargetDir = xMSet.getTempDir();
            		String TargetDir = xMSet.getAlternativeTempImageDir();
            	    gpUnZipFileList uzip1 = new gpUnZipFileList( ZipFNaam , TargetDir , "zBoxDiagr_" , logger); uzip1 = null;
            	    gpUnZipFileList uzip2 = new gpUnZipFileList( ZipFNaam , TargetDir , "zColrHist_" , logger); uzip2 = null;
            	    gpUnZipFileList uzip3 = new gpUnZipFileList( ZipFNaam , TargetDir , "zPeakDiag_" , logger); uzip3 = null;
                    ArrayList<String> flist = xMSet.xU.GetFilesInDir( TargetDir , null );
                    for(int i=0;i<flist.size();i++)
                    {
                     if( flist.get(i).startsWith("zBoxDiagr_") )  boxURL  = xMSet.xU.maakFileURL( TargetDir + xMSet.xU.ctSlash + flist.get(i));
                     if( flist.get(i).startsWith("zColrHist_") )  histoURL= xMSet.xU.maakFileURL( TargetDir + xMSet.xU.ctSlash + flist.get(i));
                     if( flist.get(i).startsWith("zPeakDiag_") )  peakURL = xMSet.xU.maakFileURL( TargetDir + xMSet.xU.ctSlash + flist.get(i));
                     //do_error( "-->" + flist.get(i) );
                     removeExtracted=true;
                    }
                   }
             }
            }
            // is an estafette file is requested - make sure images will not be displayed
            if( requestToCreateEstafetteFile == true ) { histoURL = boxURL = peakURL = null;  }
                                                  else { do_log(5,"Pictures [" + histoURL + "," + boxURL + "," + peakURL + "]"); }
		    if( (boxURL != null)  || ( histoURL != null) || (peakURL != null) ) {
            	 plaatjesPane = new JPanel();
                 plaatjesPane.setBackground(Color.WHITE);
     		     plaatjesPane.setLayout(new BoxLayout(plaatjesPane, BoxLayout.X_AXIS));
     		     //
     			 if( histoURL != null) plaatjesPane.add(new JLabel(new ImageIcon(ImageIO.read(histoURL))));
     			 if( boxURL != null) plaatjesPane.add(new JLabel(new ImageIcon(ImageIO.read(boxURL))));
     			 if( peakURL != null) plaatjesPane.add(new JLabel(new ImageIcon(ImageIO.read(peakURL))));
     		     koepelPane.add(plaatjesPane);
     		     if( removeExtracted == true ) removeExtractedFiles(histoURL,boxURL,peakURL);
            }
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
		    
		    // Hide toggles - only is histos have been shown
		    if( (boxURL != null)  || ( histoURL != null) || (peakURL != null) ) 
		    {
		     JTextField hideLabel = new JTextField("Hide histograms");
	    	 hideLabel.setEditable(false);
	    	 hideLabel.setHorizontalAlignment(JTextField.RIGHT);
	    	 hideLabel.setFont(font);
	    	 leftPane.add(hideLabel);
	    	 //
	    	 JComboBox hideDrop = new JComboBox();
	    	 hideDrop.setFont(font);
	    	 String hidelijst[] = { "Yes" , "No" };
    		 for(int j=0;j<hidelijst.length;j++) hideDrop.addItem( hidelijst[j] );
    		 hideDrop.setSelectedIndex(1);
    		 //
    		 hideDrop.addActionListener(new ActionListener(){
		            public void actionPerformed(ActionEvent e){
		              	//doDropDown();
		            	JComboBox x = (JComboBox)e.getSource();
		            	String yesno = x.getSelectedItem().toString();
		            	boolean ib = (yesno.compareToIgnoreCase("YES")==0) ? true : false;
		            	hideHistograms(ib);
		            }
		        });
    		 //
	    	 rightPane.add(hideDrop);
		    }
		    
		    //
		    for(int i=0;i<arField.length;i++)
		    {
		    	arLabel[i] = new JTextField();
		    	arLabel[i].setEditable(false);
		    	arLabel[i].setHorizontalAlignment(JTextField.RIGHT);
		    	arLabel[i].setFont(font);
		    	leftPane.add(arLabel[i]);
		    	//
		    	arField[i] = new JTextField(40);
		    	arField[i].setFont(font);
		    	rightPane.add(arField[i]);
		    	//
		    	switch(i)
		    	{
		    	case iCMXUID     : { arLabel[i].setText("CMXUID");
		    	                     if( requestToCreateEstafetteFile ) arField[i].setText(suggestCMXUID());
		    	                                                   else arField[i].setText(cmeta.getCMXUID()); break; }
		    	case iUID        : { arLabel[i].setText("UID");  arField[i].setText(cmeta.getUID()); arField[i].setEditable(false);break; }
		    	case iISBN       : { arLabel[i].setText("ISBN"); arField[i].setText(cmeta.getISBN()); break; }
		    	case iSERIES     : { arLabel[i].setText("Series"); arField[i].setText(cmeta.getSeriesName()); break; }
		    	case iSERIESSEQ  : { arLabel[i].setText("Series sequence"); arField[i].setText(""+cmeta.getSeriesSequence()); break; }
		    	case iBOOK       : { arLabel[i].setText("Book title"); arField[i].setText(cmeta.getBookName()); break; }
		    	case iPAGE       : { arLabel[i].setText("Page"); arField[i].setText(""+cmeta.getPageNumber()); break; }
		    	case iWRITER     : { arLabel[i].setText("Writer"); arField[i].setText(cmeta.getWriterName()); break; }
		    	case iPENCILLER  : { arLabel[i].setText("Penciller"); arField[i].setText(cmeta.getPencillerName()); break; }
		    	case iCOLOURER   : { arLabel[i].setText("Colourer"); arField[i].setText(cmeta.getColourerName()); break; }
		    	case iCOMMENT    : { arLabel[i].setText("Comment"); arField[i].setText(cmeta.getComment()); break; }
		    	case iFOLDERNAME : { arLabel[i].setText("Folder"); arField[i].setText(cmeta.getFolderName()); arField[i].setEditable(false); break; }
		    	case iFILENAME   : { arLabel[i].setText("File"); arField[i].setText(cmeta.getFileName()); arField[i].setEditable(false); break; }
		     	case iMETRICS    : { arLabel[i].setText("Size"); arField[i].setText(cmeta.getMetrics()); arField[i].setEditable(false); 
		     	                     if( cmeta.gotValidDPI()==false ) arField[i].setForeground(Color.RED); break; }
		     	
		    	}
		    }
		    // DropDowns
		    for(int i=0;i<this.arDropField.length;i++)
		    {
		    	int idx = i + arField.length;
		    	arLabel[idx] = new JTextField();
		    	arLabel[idx].setEditable(false);
		    	arLabel[idx].setFont(font);
		    	arLabel[idx].setHorizontalAlignment(JTextField.RIGHT);
		    	switch(i)
		    	{
		     	case iBLACKWHITE : { arLabel[idx].setText("Colour Schema"); break; }
		      	case iLANGUAGE   : { arLabel[idx].setText("Language"); break; }
				case iBINARIZE   : { arLabel[idx].setText("Binarize Classification Method"); break; }
		    	case iCLUSTER    : { arLabel[idx].setText("Cluster Classification Method"); break; }
		    	case iPROXIMITY  : { arLabel[idx].setText("Proximity Tolerance"); break; }
		    	case iCROPPING   : { arLabel[idx].setText("Crop Image"); break; }
			 	case iCURATION   : { arLabel[idx].setText("Tesseract Curation"); break; }
				}
		    	leftPane.add(arLabel[idx]);
		    	//
		    	arDropField[i] = new JComboBox();
		    	arDropField[i].setFont(font);
		    	rightPane.add(arDropField[i]);
		    	
		    	switch(i)
		    	{
		    	case iBLACKWHITE : {
		    		String lijst[] = cenums.getColourSchemaList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , cmeta.getColourSchema() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iLANGUAGE : {
		     		String lijst[] = xMSet.getLanguageList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , cmeta.getLanguage() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iBINARIZE : {
		    		String lijst[] = cenums.getBinarizeMethodList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , cmeta.getBinarizeClassificationTypeString() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iCLUSTER : {
		    		String lijst[] = cenums.getClusterMethodList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , cmeta.getClusterClassificationTypeString());
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iPROXIMITY : {
		    		String lijst[] = cenums.getProximityToleranceList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , cmeta.getProximityTypeString() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iCURATION : {
		    		String lijst[] = cenums.getOCRCurationList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , ""+cmeta.getOCRCuration() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	case iCROPPING : {
		    		String lijst[] = cenums.getCroppingTypeList();
		    		for(int j=0;j<lijst.length;j++) arDropField[i].addItem( lijst[j] );
		    		int k = xMSet.xU.getIdxFromList( lijst , ""+cmeta.getCroppingType() );
		    		if( k >= 0 ) arDropField[i].setSelectedIndex(k);
		    		break;
		    	 }
		    	}
		    	//
		    }
		    //
		    bovenPane.add(leftPane);
		    bovenPane.add(rightPane);
		    koepelPane.add(bovenPane);
		    //
		    JPanel onderPane = new JPanel();
            onderPane.setLayout(new BoxLayout(onderPane, BoxLayout.X_AXIS));
            //
            JButton okButton = new JButton("OK"); 
            okButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    				if( cmeta.isEmpty() ) {
    					String sUID = arField[iCMXUID].getText();
    					if (sUID == null ) sUID = "";
    					sUID = sUID.trim();
    					if( sUID.length() <= 0) { verzoekOmUID(); return; }
    		    	}
    				//
    				cmeta.setCMXUID( arField[iCMXUID].getText() );
    				cmeta.setISBN( arField[iISBN].getText() );
    				cmeta.setSeriesName( arField[iSERIES].getText() );
    				cmeta.setSeriesSequence( arField[iSERIESSEQ].getText() );
    				cmeta.setBookName( arField[iBOOK].getText() );
    				cmeta.setPageNumber( arField[iPAGE].getText() );
    				cmeta.setWriterName( arField[iWRITER].getText() );
    				cmeta.setPencillerName( arField[iPENCILLER].getText() );
    				cmeta.setColourerName( arField[iCOLOURER].getText() );
    				cmeta.setComment( arField[iCOMMENT].getText() );
    				//
    				cmeta.setColourSchema( arDropField[iBLACKWHITE].getSelectedItem().toString() );
    				cmeta.setLanguage( arDropField[iLANGUAGE].getSelectedItem().toString() );
    				cmeta.setBinarizeClassificationType(arDropField[iBINARIZE].getSelectedItem().toString() );
    				cmeta.setClusterClassificationType(arDropField[iCLUSTER].getSelectedItem().toString() );
    				cmeta.setProximityType(arDropField[iPROXIMITY].getSelectedItem().toString() );
    				cmeta.setOCRCuration(arDropField[iCURATION].getSelectedItem().toString() );
    				cmeta.setCroppingType(arDropField[iCROPPING].getSelectedItem().toString() );
    				// assess whether a request was made to create estafette file
    				if( requestToCreateEstafetteFile == true ) {
    					cmeta.setMetadataFileName(xMSet.getEstafetteFileName());
    				}
    				//
    				cmeta.writeMetaData();
    				//
    				xMSet.setDialogCompleteState(true);
    				xMSet.setMetadataHasBeenModified(cmeta.getHasBeenModified());
    				//
     			    dialog.dispose();
    			}
    		});
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent arg0) {
    			  xMSet.setMetadataHasBeenModified(false);
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
		    //
		    //
            dialog.add(koepelPane);
            dialog.pack();
            dialog.setLocationByPlatform(true);
            dialog.setVisible(true);
            //
            
        } 
        catch (Exception e) 
        {
            //e.printStackTrace();
            do_error( "Metdata Dialog" + e.getMessage() );
        }
	}

	//------------------------------------------------------------
	private void removeExtractedFiles(URL one, URL two, URL three)
	//------------------------------------------------------------
	{
	 for(int i=0;i<3;i++)
	 {
		 String FName = null;
		 switch( i )
		 {
		 case 0 : { FName = one.toString(); break; }
		 case 1 : { FName = two.toString(); break; }
		 case 2 : { FName = three.toString(); break; }
		 default : break;
		 }
		 if( FName == null ) continue;
         //do_error( "=====" + FName );
         FName = FName.trim();
         if( FName.toUpperCase().startsWith("FILE:/") == false ) {
        	 do_error("Cannot extract filename from [" + FName );
        	 return;
         }
         FName = FName.substring("FILE:/".length());
         if( xMSet.xU.IsBestand(FName) == false ) continue;
		 do_log(5,"Removing [" + FName + "]") ;
		 xMSet.xU.VerwijderBestand(FName);
	 }
	}
	
	//------------------------------------------------------------
	private void verzoekOmUID()
	//------------------------------------------------------------
	{
		JOptionPane.showMessageDialog(null,"Please provide a Comic UID",xMSet.getApplicDesc(),JOptionPane.WARNING_MESSAGE);
	}

	//------------------------------------------------------------
	private void hideHistograms(boolean hide)
	//------------------------------------------------------------
	{
		if( dialog.isVisible() == false ) return;
		if( plaatjesPane == null ) return;
	    // first time	
		if( plaatjesPaneHoogte < 0 ) {
			plaatjesPaneHoogte =plaatjesPane.getHeight();
		}
		if( hide ) {
		 plaatjesPane.setVisible(false);
		 dialog.setSize( dialog.getWidth() , dialog.getHeight() - plaatjesPaneHoogte );
		}
		else {
		 plaatjesPane.setVisible(true);
		 dialog.setSize( dialog.getWidth() , dialog.getHeight() + plaatjesPaneHoogte );
		}
	}

	//------------------------------------------------------------
	private String suggestCMXUID()
	//------------------------------------------------------------
	{
		String sDir = xMSet.getScanFolder();
		if( sDir == null ) return "";
		sDir =  xMSet.xU.getFolderOrFileName(sDir);
		if (sDir == null ) return "";
		sDir = sDir.trim().toLowerCase();
		sDir = xMSet.xU.keepLettersAndNumbers(sDir);
		return sDir;
	}
	
	//------------------------------------------------------------
	private String suggestTitle()
	//------------------------------------------------------------
	{
		String FileDirName=null;
		if( requestToCreateEstafetteFile ) {
			FileDirName = xMSet.getScanFolder();
			if( xMSet.xU.IsDir(FileDirName) == false ) return "";
			return "[" + FileDirName + "]";
		}
		else {
		    FileDirName = xMSet.getOrigImageLongFileName();
            if( xMSet.xU.IsBestand(FileDirName) ) FileDirName = xMSet.getMetaDataFileName();
            return "[" + FileDirName + "]";
		}
    }
}
