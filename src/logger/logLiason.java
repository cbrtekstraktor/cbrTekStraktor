package logger;

import nioClientServer.nioClient;

public class logLiason {
	private long LogLiasonId = System.nanoTime();
	private nioClient nio=null;
	
	public logLiason(long l)
	{
	 LogLiasonId =l;
	 nio = new nioClient(15342);
	}
	
	// MAY 2018 added exception handler
	public void write( String iclassName , int ilogLevel , String ilogMsg )
	{
		String sMsg = "<lia><logger>" + LogLiasonId + "</logger><class>" + iclassName + "</class><level>" +ilogLevel + "</level><payload>" + ilogMsg + "</payload></lia>";
		try {
		  if( nio.writeClient( sMsg) < 0 ) {
		    if( ilogLevel == 0) System.err.println(iclassName + " " + ilogMsg);
		                   else System.out.println(iclassName + " " + ilogMsg);
		}
		}
		catch(Exception e) {
			System.out.println("nioClient error [" + e.getMessage() + "]");
			System.out.println(iclassName + " " + ilogMsg);
		}
	}

	public void close()
	{
		nio.close(LogLiasonId);
	}
}
