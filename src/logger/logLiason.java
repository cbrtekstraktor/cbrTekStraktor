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
	
	public void write( String iclassName , int ilogLevel , String ilogMsg )
	{
		String sMsg = "<lia><logger>" + LogLiasonId + "</logger><class>" + iclassName + "</class><level>" +ilogLevel + "</level><payload>" + ilogMsg + "</payload></lia>";
		if( nio.writeClient( sMsg) < 0 ) {
		  if( ilogLevel == 0) System.err.println(iclassName + " " + ilogMsg);
		  else System.out.println(iclassName + " " + ilogMsg);
		}
	}

	public void close()
	{
		nio.close(LogLiasonId);
	}
}
