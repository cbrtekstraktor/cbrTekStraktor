package cbrTekStraktorModel;

import java.awt.Color;

public class cmcProcConstants {
	
	public static String Application = "cbrTekStraktor";
	public static String Version = "V0.4";
	public static String Build = "13-May-2018";
	public static long   ExportFormatVersion = 20170601;    // used to detect incompatible export files
	
	public static int   QUICKEDITBORDER = 18;
    public static Color DEFAULT_LIGHT_GRAY = javax.swing.UIManager.getDefaults().getColor("Button.background");
    public static int   MINIMAL_DPI = 70;  // a lot of scans are 72
    public static int   MAXIMAL_DPI = 300; // Tesseract works fine at 300
	
    public static String LINUX_TESSERACTHOME = "/usr/bin";
    public static String MSDOS_TESSERACTHOME = "C:\\temp\\Tesseract-4-64Bit\\Tesseract-OCR";
    
    public static int MAX_TRANSLATED_LINES = 150;

    public static int MIN_TENSORFLOW_FLOW_ARCHIVE_FILES = 30;
	public static String LINUX_PYTHONHOME = "/usr/bin";
	public static String MSDOS_PYTHONHOME = "C:\\Program Files\\Python35";
	
			
    public static int ZWART  = 0xff000000;
    public static int WIT    = 0xffffffff;
	public static int ROOD   = 0xffff0000;
	public static int GROEN  = 0xff00ff00;
	public static int BLAUW  = 0xff0000ff;
	public static int ORANJE =  0xffda70d6;  // 218,112,214
	public static int GRIJS  =  0xff121212;  // 218,112,214
	
    
	public cmcProcConstants()
	{
		
	}

}
