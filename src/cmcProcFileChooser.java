import java.io.File;

import javax.swing.JFileChooser;



public class cmcProcFileChooser {

	String FNaam = null;

	cmcProcFileChooser(String sDir , boolean FolderOnly)
	{
	 JFileChooser fc = new JFileChooser(sDir);
	 if( FolderOnly ) fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	 int result = fc.showOpenDialog(null);
     if( result == JFileChooser.APPROVE_OPTION ) {
    	 File f = fc.getSelectedFile();
    	 FNaam = f.getAbsolutePath();
     }
	}
	
	public String getAbsoluteFilePath()
	{
		return FNaam;
	}
}
