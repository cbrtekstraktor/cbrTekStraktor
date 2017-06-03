package generalpurpose;

import java.awt.Component;



import java.awt.Dimension;
import java.io.File;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
 

public class gpFileChooser extends JFileChooser {

	      // placeholders for various objects on the dailog
	      JPanel bottomLabelPanel = null;
          JPanel bottomComboBoxPanel = null;
          JComboBox actionBox = null;
          Dimension fillerMinimum=null;
          Dimension fillerMaximum=null;
          Dimension fillerPreferred=null;
          
          
          private String RootFolderName=null;
          private String SelectedFileName = null;
          private String[] optionList = null;

          //-----------------------------------------------------------------------
          private void DEBUG(String s)
          //-----------------------------------------------------------------------
          {
        	  //System.err.println(s);
          }
          
          //-----------------------------------------------------------------------
          //---  FILTERS -----------------------------------------------------------
          
  	      //Accept all directories and all gif, jpg, tiff, or png files.
          //-----------------------------------------------------------------------
          class ImageFilter extends FileFilter 
          //-----------------------------------------------------------------------
          {
        	    public boolean accept(File f) {
        	        if (f.isDirectory()) {
        	            return true;
        	        }
        	        if( f.getAbsolutePath().toUpperCase().endsWith(".PNG")) return true;
        	        if( f.getAbsolutePath().toUpperCase().endsWith(".GIF")) return true;
        	        if( f.getAbsolutePath().toUpperCase().endsWith(".JPG")) return true;
        	        if( f.getAbsolutePath().toUpperCase().endsWith(".JPEG")) return true;
        	        return false;
        	    }
        	    //The description of this filter - needed because displayed
        	    public String getDescription() {
        	        return "Image Files";
        	    }
          }
          //Accept all directories and XML
          //-----------------------------------------------------------------------
          class XMLFilter extends FileFilter 
          //-----------------------------------------------------------------------
          {
        	    public boolean accept(File f) {
        	        if (f.isDirectory()) {
        	            return true;
        	        }
        	        if( f.getAbsolutePath().toUpperCase().endsWith(".XML")) return true;
        	        return false;
        	    }
        	    //The description of this filter - needed because displayed
        	    public String getDescription() {
        	        return "XML Files";
        	    }
          }
          //Accept all directories and _set.ZIP
          //-----------------------------------------------------------------------
          class ArchiveFilter extends FileFilter 
          //-----------------------------------------------------------------------
          {
        	    public boolean accept(File f) {
        	        if (f.isDirectory()) {
        	            return true;
        	        }
        	        if( f.getAbsolutePath().toUpperCase().endsWith("_SET.ZIP")) return true;
        	        return false;
        	    }
        	    //The description of this filter - needed because displayed
        	    public String getDescription() {
        	        return "cbrTekStraktor Archive Files";
        	    }
        	}

          
           // Constructor
           //-----------------------------------------------------------------------
           public gpFileChooser(String startDir)
           //-----------------------------------------------------------------------
           {
        	  // set startdir
        	  super(startDir);
        	  RootFolderName = startDir;
        	  // run through the components of gthis dialog
        	  analyseGUIComponents();
        	  //
        	  SelectedFileName=null;
           }

          // opens/shows the dialog
          //-----------------------------------------------------------------------
      	  public int runDialog()
          //-----------------------------------------------------------------------
      	  {
      		  SelectedFileName = null;
      		  //
      		  if( optionList != null ) enhanceDialog();
      		  //
        	  int result = this.showOpenDialog(null);
              if( result == JFileChooser.APPROVE_OPTION ) {
             	 File f = this.getSelectedFile();
             	 SelectedFileName = f.getAbsolutePath();
              }
              return result;
          }

      	  //-----------------------------------------------------------------------
      	  private void enhanceDialog()
      	  //-----------------------------------------------------------------------
      	  {
      		    // doe we have a handle for the bottom label and combobox panel ?
      		    if( (bottomLabelPanel == null) || (bottomComboBoxPanel==null) ) return;
      		    
      		    // set label : insert filler + add label
      		    if( fillerMinimum == null ) fillerMinimum = new Dimension(1,12);
      		    if( fillerMinimum == null ) fillerMaximum = new Dimension(1,12);
      		    if( fillerPreferred == null ) fillerPreferred = new Dimension(1,12);
    		    //  
      		    Box.Filler fi = new Box.Filler(fillerMinimum,fillerMaximum,fillerPreferred);
                bottomLabelPanel.add(fi);
      		    bottomLabelPanel.add(new JLabel("Action:"));
          
      		    // set the extra combobox : insert filler and add combobox
      		    actionBox = new JComboBox();
                actionBox.setModel(new DefaultComboBoxModel(optionList));
                Box.Filler fi2 = new Box.Filler(fillerMinimum,fillerMaximum,fillerPreferred);
                bottomComboBoxPanel.add(fi2);  	 
                bottomComboBoxPanel.add( actionBox );
                // finally rename the Open button
                setOpenButtonText("OK");
                //
      	  }

      	  //-----------------------------------------------------------------------
      	  public void setOpenButtonText(String s)
      	  //-----------------------------------------------------------------------
      	  {
      		this.setApproveButtonText(s);
      	  }
      	  
      	  //-----------------------------------------------------------------------
      	  //---GETTERS--------------------------------------------------------------------
          
      	  //-----------------------------------------------------------------------
          public String getAbsoluteFilePath()
      	  //-----------------------------------------------------------------------
          {
    		return SelectedFileName;
    	  }
      	  //-----------------------------------------------------------------------
      	  public String getAction()
      	  //-----------------------------------------------------------------------
      	  {
      		  if( actionBox == null )  return null;
      		  try {
      		   return actionBox.getSelectedItem().toString();
      		  }
      		  catch(Exception e ) { return null;}
      	  }
      	  
      	  
      	  //-----------------------------------------------------------------------
      	  //-- SETTERS---------------------------------------------------------------------
          
      	  //-----------------------------------------------------------------------
       	  public void setFolderOnly()
          //-----------------------------------------------------------------------
       	  {
        	  this.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          }
          //-----------------------------------------------------------------------
       	  public void setLockToFolder()
          //-----------------------------------------------------------------------
       	  {
       		final File dirToLock = new File(RootFolderName);
       		this.setFileView(new FileView() {
       		    @Override
       		    public Boolean isTraversable(File f) {
       		         return dirToLock.equals(f);
       		    }
       		});
          }
          //-----------------------------------------------------------------------
       	  public boolean setFilter(String tipe)
          //-----------------------------------------------------------------------
       	  {
        	  if( tipe == null ) return false;
        	  if(  tipe.compareToIgnoreCase("IMAGE") == 0 ) {
        		  this.setFileFilter( new ImageFilter());
        	      return true;  		  
        	  }
        	  if(  tipe.compareToIgnoreCase("ARCHIVE") == 0 ) {
        		  this.setFileFilter( new ArchiveFilter());
        	      return true;  		  
        	  }
        	  if(  tipe.compareToIgnoreCase("XML") == 0 ) {
        		  this.setFileFilter( new XMLFilter());
        	      return true;  		  
        	  }
        	  return true;
          }
       	  //-----------------------------------------------------------------------
       	  public boolean setOptions(String[] iOptionList)
          //-----------------------------------------------------------------------
       	  {
       		  optionList=iOptionList;
       		  return true;
       	  }
          //-----------------------------------------------------------------------
       	  private void analyseGUIComponents()
          //-----------------------------------------------------------------------
       	  {
       	   	    bottomComboBoxPanel=null;
       		    bottomLabelPanel = null;
       	
       		    //
        	    Component[] c = this.getComponents();
                for (int i=0; i<c.length; i++)
                {
                    DEBUG("ROOT : " + c[i].getClass().getName() );
                    if (c[i] instanceof JPanel) {
                          getChildren( (JPanel)c[i] , 0 );
                    }     
                }
          }
         
          //-----------------------------------------------------------------------
       	  private void getChildren(JPanel o,int level)
          //-----------------------------------------------------------------------
       	  {
            String ident = "";
            for(int k=0;k<level;k++) ident += "  ";
            DEBUG("PANEL : " + o.getClass().getName() + " " + o.getName());
            ident += "  ";
            Component[] c = o.getComponents();
            int labelcount=0;
            for (int i=0; i<c.length; i++)
            {
                  DEBUG(  ident + c[i].getClass().getName() );
                  if (c[i] instanceof JPanel) getChildren( (JPanel)c[i] , level+1 );
                  if (c[i] instanceof JButton) {
                        JButton b = (JButton)c[i];
                        DEBUG(  "Button" + ident + b.getText() );
                  }
                  else
                  if (c[i] instanceof JComboBox) {
                        JComboBox b = (JComboBox)c[i];
                        bottomComboBoxPanel = (JPanel)o;
                  }
                  else
                  if (c[i] instanceof JLabel) {
                        labelcount++;
                        if( labelcount == 2) {   // There are 2 labels on the dialog 
                        	  bottomLabelPanel = o;
                        }
                  }
                  else
                  if( (c[i] instanceof Box.Filler) && (labelcount==1) ) {
                	  Box.Filler f = (Box.Filler)c[i];
                	  fillerMinimum = f.getMinimumSize();
                	  fillerMaximum = f.getMaximumSize();
                	  fillerPreferred = f.getPreferredSize();
                  }
                  else {
                	 continue; 
                  }
            }
          }

          /*
           * 
           * container.add(firstComponent);
Dimension minSize = new Dimension(10, 20);
Dimension prefSize = new Dimension(20, 30);
Dimension maxSize = new Dimension(50, 100);
container.add(new Box.Filler(minSize, prefSize, maxSize));
container.add(secondComponent);
           */
}

 
