package generalpurpose;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;

public class gpAppendStream {

	boolean AppendFileIsOpen=false;
	String AppendFileName=null;
	//private PrintStream AppendFile=null;
	private Writer AppendFile=null;
	private String CodePage=null;
	private String CRLF="\n";
	
	//
	//---------------------------------------------------------------------------------
	public gpAppendStream(String FNm , String encoding)
	//---------------------------------------------------------------------------------
	{
		AppendFileName = FNm;
		CodePage = encoding;
		CRLF = System.getProperty("line.separator");
		OpenAppendFile();
	}
	//
	//---------------------------------------------------------------------------------
	private void OpenAppendFile()
	//---------------------------------------------------------------------------------
	{
	   //System.err.println("Codepage" + CodePage );
		try{
			//AppendFile=new PrintStream(new FileOutputStream(AppendFileName,true),"UTF8");
			AppendFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(AppendFileName,true), CodePage));
			AppendFileIsOpen=true;
		}catch(Exception e){
			e.printStackTrace(System.err);
		}
	}
	//
	//---------------------------------------------------------------------------------
	public void AppendIt(String sIn)
	//---------------------------------------------------------------------------------
	{
			if( AppendFileIsOpen ) {
			 try {
				AppendFile.append(sIn + CRLF);
				//System.out.println("ap" + sIn);
			 }
			 catch(Exception e ) {
				e.printStackTrace(System.err);
			 }
			}
			
	}
	//
	//---------------------------------------------------------------------------------
	public void CloseAppendFile()
	//---------------------------------------------------------------------------------
	{
		if( AppendFileIsOpen == false ) return;
		if( AppendFile != null ) {
			try {
			   AppendFile.close();
			   //System.out.println("Closed appendfiles [" + AppendFileName + "]" );
			}
			catch(Exception e ) {
				e.printStackTrace(System.err);
			}
		}
	}
	
}
