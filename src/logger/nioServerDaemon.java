package logger;

import generalpurpose.gpLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class nioServerDaemon extends Thread {
	
	private int BUFFER_SIZE = 2048*4;
	private ServerSocketChannel server = null;
	private Selector selector = null;
	private boolean initOK=false;
	//private int prevTel = -1;
	private boolean requestReset=false;
    private int PortNumber = -1;	
    private String LogFileName=null;
    private String ErrFileName=null;
    private gpLogger logWriter=null;
    private gpLogger errWriter=null;
    

	//---------------------------------------------------------------------------------
	void logit(int level,String sIn)
	//---------------------------------------------------------------------------------
	{
		
		System.out.println( getClass().getName() + " " + sIn);
	}

	//---------------------------------------------------------------------------------
	public nioServerDaemon(int iPort , int loglevel , String FLog , String Ferr)
	//---------------------------------------------------------------------------------
	{
		LogFileName=FLog;
		ErrFileName=Ferr;
		PortNumber=iPort;
		initServer();
		if( isOK() ) {
			logWriter = new gpLogger( loglevel , LogFileName);
			errWriter = new gpLogger( loglevel , ErrFileName);
			logWriter.setVerbose(true);
			errWriter.setVerbose(false);
		}
	}
/*
	//---------------------------------------------------------------------------------
	void MyPauze(int iPeriode)
	//---------------------------------------------------------------------------------
	{
		 	 try 
			 {
		       Thread.sleep(iPeriode);        
		     }
			 catch (InterruptedException ie)
		     {
		        logit(0,ie.getMessage());
		     }
	}
*/
    //
	//---------------------------------------------------------------------------------
	public void run()
	//---------------------------------------------------------------------------------
	{
	   String sRet="";
	   while(true)
       {
    	  sRet = readServer();
    	  if( sRet == null ) continue;
    	  if( sRet.length() <= 0) continue;
    	  opq(sRet);
       }
	}
	//
	//---------------------------------------------------------------------------------
	private void opq(String sLijn)
	//---------------------------------------------------------------------------------
	{
		// Messageformat = <?xml version="1.0"?><msg><msgid>77</msgid><![CDATA[  msessage   ]]></msg>  
		char[] cbuf = sLijn.toCharArray();
		String stag="";
		int lok=-1;
		String ID="";
		for(int i=0;i<cbuf.length;i++) 
		{	
			stag += cbuf[i];
			if( stag.startsWith("<?xml version=\"1.0\"?><msg><msgid>") ) {
				lok=1;
				stag="";
			}
			if( stag.endsWith("</msgid>") ) {
				int jj = stag.length() - 8;
				ID = (jj > 0) ? stag.substring(0,jj) : "-1";
				//System.out.print("[ID=" + ID + "] ");
				lok=2;
				stag="";
			}
			if( stag.startsWith("<![CDATA[") ) {
				lok=3;
				stag="";
			}
			if( stag.endsWith("]]></msg>") ) {
				lok=4;
				int jj = stag.length() - 9;
				String MSG = (jj > 0) ? stag.substring(0,jj) : "-1";
				purgePayload( ID , MSG.trim());
				stag="";
			}
		}		
		
	}
	
	// <lia><logger>1</logger><class>cbrTekStraktorModel.cmcProcSettings</class><level>9</level><payload>sun.cpu.isalist=amd64</payload></lia>
	//---------------------------------------------------------------------------------
	private void purgePayload(String id , String payload)
	//---------------------------------------------------------------------------------
	{
		if( payload.startsWith("<lia><logger>") == false) return;
		if( payload.endsWith("</payload></lia>") == false) return;
		//
		String liasonid = getXMLValue(payload,"logger");
		String slevel = getXMLValue(payload,"level");
		String sclass = getXMLValue(payload,"class");
		String slog = getXMLValue(payload,"payload");
		//
		if( slevel == null ) slevel = "0";
		if( sclass == null ) sclass= "";
		if( slog == null ) slog = "";
		//
		int ilevel = NaarInt(slevel);
		sclass = normalizeClass(sclass);
		slog = slog.trim();
		if( ilevel == 0 ) slevel = "E"; else slevel =" "; 
		//
		String sLine = slevel + " [" + sclass + "] " + slog; 
		if( ilevel == 0 ) errWriter.Logit( 0 , sLine );
		logWriter.Logit(ilevel,sLine);
	}

	//---------------------------------------------------------------------------------
	private String getXMLValue(String sIn , String stag)
	//---------------------------------------------------------------------------------
	{
		int i = sIn.indexOf("<" + stag + ">");
		if( i<0 ) return null;
		int j = sIn.indexOf("</" + stag + ">");
		if( j<0 ) return null;
		try {
		  i += stag.length()+2;
		  String ss = sIn.substring(i,j);
		  return ss;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	//---------------------------------------------------------------------------------
	private String normalizeClass(String s)
	//---------------------------------------------------------------------------------
	{
		try {
		int i = s.lastIndexOf(".");
		if( i <=0 ) return s;
		return s.substring(i+1);
		}
		catch( Exception e ) { return s; }
	}
	
	//
	//---------------------------------------------------------------------------------
	public boolean isOK()
	//
	{
		return initOK;
	}
	//------------------------------------------------------------
	private boolean initServer()
	//------------------------------------------------------------
	{
			try {
			  server = ServerSocketChannel.open();
			  server.configureBlocking(false);
			  server.socket().bind( new java.net.InetSocketAddress("localhost",PortNumber));
			  selector = Selector.open();
			  server.register( selector , SelectionKey.OP_ACCEPT );
			  initOK=true;
			  logit(5,"Selector registered for ACCEPT on port[" + PortNumber + "]");
			}
			catch( Exception e ) { 
				initOK=false;
				logit(0,"initServer() " + e.getMessage());
				logit(0,LogStackTrace(e));
				return false;
			}
			return true;
	}
	//---------------------------------------------------------------------------------
	public String LogStackTrace(Exception e)
	//---------------------------------------------------------------------------------
	{
		      try {
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        e.printStackTrace(pw);
		        return sw.toString();
		      }
		      catch(Exception e2) {
		    	e.printStackTrace();
		        return "";
		      }
	} 
	
	//------------------------------------------------------------
	private String readServer()
	//------------------------------------------------------------
	{
		    String sIn = "";
			if( initOK == false ) return null;
			//  reinit na een fout
			if( requestReset ) {
				requestReset=false;
				initOK=false;
			    try {
			    	selector.close();
					server.close();
				    initServer();
					logit(5,"Selector re-registered for ACCEPT");
				}
				catch(Exception e) {
					logit(0,"re-register - readServer() " + e.getMessage());
					logit(0,LogStackTrace(e));
					return null;
				}
			}
			// kijk op de poort
			try {
			   int ret = selector.select(50);
			   if( ret <=0 ) return null;   // nothing to do
			   //
			   Set<SelectionKey> keys = selector.selectedKeys();
			   Iterator<SelectionKey> i = keys.iterator();
			   while (i.hasNext())
			   {
				   SelectionKey key = i.next();
				   i.remove();
				   //
				   if( key.isAcceptable() ) {
					   logit(5,"accept client");
					   SocketChannel client = server.accept();
					   client.configureBlocking(false);
					   client.register( selector , SelectionKey.OP_READ );  
					   continue;
				   }
				   if( key.isReadable() ) {
					   String sL = readBuffer(key);
					   if( sL == null ) {   // er deed zich een fout voor
						   requestReset=true;
						   continue;
					   }
					   if( sL.length() == 0 ) continue;
					   //
					   sIn = sIn + sL;
					   //logit(0,"DAEMON " + sIn);
					   //
					   continue;  // i.hasnext
				   } // readable
			   }
			}
			catch( Exception e ) {
				logit(0,"readServer() " + e.getMessage());
				logit(0,LogStackTrace(e));
				return null;
			}
			return sIn;
		}
		//------------------------------------------------------------
		private String readBuffer(SelectionKey key)
		//------------------------------------------------------------
		{
			   SocketChannel client = (SocketChannel)key.channel();
			   ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
			   String sL="";
			   try {
				   
				   int attempt=0;
				   while(attempt<1) // niet langer nodig
				   {
					  attempt++;
				      buffer.clear();
				      int len = client.read(buffer);
				      if( len < 0 ) {
					    //logit(0,"Error reading buffer - waiting " + attempt);
					    Thread.sleep(10);  // beetje wachten - met niets terugkeren en wachten opdat selector weer afvuurt
				        return "";
				      }
				      if( len == 0 ) return "";
				      for(int i=0;i<buffer.limit();i++)
				      {
					   sL = sL + (char)(buffer.get(i)&0xff);
				      }
				      return sL;
				   }
				   return sL;
			   }
			   catch( Exception e ) {
					logit(0,"readBuffer() " + e.getMessage());
					logit(0,LogStackTrace(e));
					return null;
				}
		}
		//------------------------------------------------------------
		public void close()
		//------------------------------------------------------------
		{
			if( errWriter != null ) errWriter.CloseLogs();
			if( logWriter != null ) logWriter.CloseLogs();
			if( initOK == false ) return;
			try {
				server.close();
			    logit(5,"close()");
			}
			catch( Exception e ) {
				logit(0,"close() " + e.getMessage());
				logit(0,LogStackTrace(e));
			}
		}
		//---------------------------------------------------------------------------------
		int NaarInt(String sIn)
		//---------------------------------------------------------------------------------
		{
					 int ii=-1;
						
					 try {
						  ii=Integer.parseInt( sIn );
						  return ii;
						 }
						 catch ( NumberFormatException e)
						 {
							 return -1;
						 }
		}
}
