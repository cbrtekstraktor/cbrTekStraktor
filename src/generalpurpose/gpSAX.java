package generalpurpose;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.*;


//This is a light implementation of the SAX XML parser  (c) WSQLC
public class gpSAX {

gpStringUtils xstrutl = null;
Method callbackMethod = null;
Object CallingClass=null;
FileInputStream fromXML = null;
boolean gotExternalLogger=false;

class XMLTuple {
	String Tag="";
	String Content="";
}
ArrayList<XMLTuple> NodeLst = new ArrayList<XMLTuple>();

//
//---------------------------------------------------------------------------------   
public gpSAX(Object iObj)
//---------------------------------------------------------------------------------
{
	xstrutl = new gpStringUtils();
	// reflection info
	// http://docs.oracle.com/javase/tutorial/reflect/member/methodInvocation.html
	try {
		 CallingClass = iObj;	
	}
	catch( Exception e)
	{
		logit(e.getMessage()+ LogStackTrace(e));
	}
}
//
//---------------------------------------------------------------------------------
void logit(String sIn)
//---------------------------------------------------------------------------------
{
	if( gotExternalLogger == true ) gotExternalLogger = PerformCallback( "nodeLogit" , sIn , null , null);
	if( gotExternalLogger == false ) System.out.println(sIn);
}
//
//---------------------------------------------------------------------------------
boolean PerformCallback(String sCallbackFunction , String sTag , String sContent , String sHier )
//---------------------------------------------------------------------------------
{
	boolean DEBUG = true;
	// debug
	//if( DEBUG ) logit(sCallbackFunction + " " + sTag );
	
try {
	Class<?> c = Class.forName(CallingClass.getClass().getName());
  //Object t = c.newInstance();
	
  Method[] allMethods = c.getDeclaredMethods();
  for (Method m : allMethods) {
  	//System.out.println( "->" + m.getName() + "<-");
  	String mname = m.getName();
		if (mname.compareTo(sCallbackFunction) != 0 ) continue;
		Type[] pType = m.getGenericParameterTypes();
		//System.out.println("invoking" + mname);
		try {
			//if( DEBUG ) System.out.println("trying"+mname+sTag+sContent);
		    m.setAccessible(true);
		    Object o = m.invoke(CallingClass, sTag , sContent , sHier);
		    return true;
		    //if( DEBUG ) System.out.println("returned " + mname +(Boolean) o);
		// Handle any exceptions thrown by method to be invoked.
		} catch (InvocationTargetException x) {
		    Throwable cause = x.getCause();
		    System.out.println("invocation failed" +   mname + cause.getMessage());  // DO NOT CHANGE TO LOGIT
		    return false;
		}
  }
  
}
catch( Exception e) {
	  System.out.println("Error while invoking method" +  LogStackTrace(e)); // DO NOT CHANGE TO LOGIT
	  return false;
}
return false;
}

//
//---------------------------------------------------------------------------------
private String LogStackTrace(Exception e)
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
//
//---------------------------------------------------------------------------------
void PushStack(String sTagIn,boolean TrialRun)
//---------------------------------------------------------------------------------
{
	String sTag = sTagIn;
	String sTwee = "";
	// <tag attrib="iets" attrib2="iets"
	if( (sTagIn.indexOf(" ")>0) && (sTagIn.indexOf("=")>sTagIn.indexOf(" ")) )  {
		 sTag  = sTagIn.substring(0,sTagIn.indexOf(" ")).trim();
		 sTwee = sTagIn.substring(sTagIn.indexOf(" ")).trim();
	}
	XMLTuple x = new XMLTuple();
	x.Tag = sTag;
	NodeLst.add(x);
	//
	if ( sTwee.length()==0 ) return;
	sTwee = xstrutl.Remplaceer(sTwee,"\" ","\n")+"\n";   //  "spatie naar \n
	sTwee = xstrutl.Remplaceer(sTwee,"\"", "");          //  " weg
	int aantal = xstrutl.TelDelims(sTwee, '\n');
	for(int i=0;i<aantal;i++)
	{
	  String sPart = xstrutl.GetVeld(sTwee,(i+1),'\n').trim();
	  if( sPart.length()==0) continue;
	  String sLinks = xstrutl.GetVeld(sPart,1,'=');
	  String sRechts = xstrutl.GetVeld(sPart,2,'=');
	  //logit( sTagIn + "(" + sPart+ ") [" + sLinks + "=" + sRechts +"]");
	  
	  //
	  PushStack( sLinks , TrialRun);
	      String sPath = getFullPath();
	      if( !TrialRun ) PerformCallback( "startNode" , sLinks , "" , sPath);
	      String sPopTag = PopTag();  // verwijderd ook de node
	  if( !TrialRun ) PerformCallback( "endNode" , sLinks , sRechts , sPath);
	}
}
//
//---------------------------------------------------------------------------------
void UpdateStack(String sData)
//---------------------------------------------------------------------------------
{
	if( NodeLst.size() <= 0) return;
	NodeLst.get(NodeLst.size()-1).Content = sData;
}
//
//---------------------------------------------------------------------------------
String PopTag()
//---------------------------------------------------------------------------------
{
	if( NodeLst.size() <= 0) return null;
	String sRet = NodeLst.get(NodeLst.size()-1).Tag;
	NodeLst.remove(NodeLst.size()-1);
	return sRet;
}
//
//---------------------------------------------------------------------------------
String PopContent()
//---------------------------------------------------------------------------------
{
	if( NodeLst.size() <= 0) return null;
	String sRet = NodeLst.get(NodeLst.size()-1).Content;
	return sRet;
}
//
//---------------------------------------------------------------------------------
String ReadTag()
//---------------------------------------------------------------------------------
{
	if( NodeLst.size() <= 0) return null;
	String sRet = NodeLst.get(NodeLst.size()-1).Tag;
	return sRet;
}
//
//---------------------------------------------------------------------------------
String getFullPath()
//---------------------------------------------------------------------------------
{
	String sRet = "NOPPES";
	for(int i=0;i<NodeLst.size();i++)
	{
	  if( i==0 )  sRet = NodeLst.get(i).Tag;
	  else sRet = sRet + "." + NodeLst.get(i).Tag;
	}
	return sRet;
}
//
//---------------------------------------------------------------------------------
public boolean ParseXMLFile(String FNaam)
//---------------------------------------------------------------------------------
{
	logit("Parsing [" + FNaam + "]");
	// 2015
	int aantal=NodeLst.size();
	for(int i=0;i<aantal;i++) this.NodeLst.remove(0);
	//
	boolean isOk = DoParseXMLFile(FNaam);
	logit("Parsing [" + FNaam + "] completed with status :" + isOk);
	return isOk;
}
//
//---------------------------------------------------------------------------------
private boolean DoParseXMLFile(String FNaam)
//---------------------------------------------------------------------------------
{
	boolean isOk = OpenXMLFile( FNaam );
	if( isOk == false ) return false;
	isOk = ParseIt( FNaam , true );  // TrialRun
	CloseXMLFile(FNaam);
	if( isOk == false ) return false;
	//
	isOk = OpenXMLFile( FNaam );
	if( isOk == false ) return false;
	isOk = ParseIt( FNaam , false );
	CloseXMLFile(FNaam);
  return isOk;
}
//
//---------------------------------------------------------------------------------
private boolean OpenXMLFile(String FNaam)
//---------------------------------------------------------------------------------
{
	try {
		  fromXML = new FileInputStream(FNaam);
		  return true;
	}
	catch( Exception e)
	{
		logit("Error opening [" + FNaam + "] " + LogStackTrace(e));
		return false;
	}
}
//
//---------------------------------------------------------------------------------
private boolean CloseXMLFile(String FNaam)
//---------------------------------------------------------------------------------
{
	try {
		  fromXML.close();
		  return true;
	}
	catch( Exception e)
	{
		logit("Error closing [" + FNaam + "] " + LogStackTrace(e));
		return false;
	}
}
//
//---------------------------------------------------------------------------------
private boolean ParseIt(String FNaam, boolean TrialRun)
//---------------------------------------------------------------------------------
{
	
	try 
	{
	   
	   byte[] buffer = new byte[100000];
	   int bytes_read=0;
	   byte[] btbuf = new byte[1];
	   byte bt = '?';
	   byte prev = '?';
	   boolean isTag=false;
	   boolean isComment = false;
	   boolean isCdata = false;
	   int teller=0;
	 
	   
	   String sTag="";
	   String sData="";
	   
	   PushStack("root",TrialRun);
	   while( (bytes_read=fromXML.read(btbuf)) != -1 ) {
		   prev = bt;
		   bt = btbuf[0];
		   //
		   teller++;
		   if( (teller==1) && (bt !=(char)'<') ) {
			   logit("Not an XML [" + FNaam + "]");
			   return false;
		   }
		   //
//System.out.print(""+(char)bt );
		   if( bt == 10 ) continue;
		   if( bt == 13 ) continue;
		   
		   if( (bt == (char)'<') && (!isCdata) ) {
			   isTag = true;
			   sTag = "";
			   continue;
		   }
		   if( (bt == (char)'>') && (!isCdata) )  {
			   isTag = false;
			   // begin of eindtag
		       if( sTag.length() < 1 ) {
		    	   logit("File comprises an empty tag");
		    	   return false;
		       }
		       if( sTag.startsWith("![CDATA[") ) {
		    	   try {
		    	    String cdata = sTag.substring(0,sTag.length()-2);
		    	    cdata = cdata.substring("![CDATA[".length());
		    	    UpdateStack(cdata);
		    	    isTag = false;
		            //logit( "->"+cdata+"<-" );
		    	   }
		    	   catch ( Exception e) { logit("Error extracting data from " + sTag); return false; }
		    	   continue;
		       }
		       // <  />
		       if( sTag.endsWith("/") == true) {
		    	   sTag = sTag.substring(0,sTag.length()-1);
		    	   PushStack(sTag,TrialRun);
		    	   String sPath = getFullPath();
		    	   String sPopTag = PopTag();  // verwijderd ook de node - Pop de tag op die manier zijn eventuele attributen verwerkt en verwijderd van de tag
		    	   if( !TrialRun ) PerformCallback( "endNode" , sPopTag , "" , sPath);
		    	   sData = PopContent();
		    	   continue;
		       }
		       // </ >
		       if( sTag.startsWith("/") == true) { // ENDTAG
		    	   String sPath = getFullPath();
		    	   String sPopTag = PopTag();  // verwijderd ook de node
		    	   if( ("/"+sPopTag).compareTo(sTag) != 0 ) {
		    		   logit("non matching tags [" + sTag + "] [" + sPopTag + "]");
			    	   return false;
		    	   }
		    	   // Callback
		    	   //logit("[" + sPopTag + "=" + sData + "]");
		    	   if( !TrialRun ) PerformCallback( "endNode" , sPopTag , sData , sPath);
		    	   // pop de data
		    	   sData = PopContent();
		       }
		       else { // Begin
		    	   // comments ignore
		    	   if( sTag.startsWith("!--")) continue;   //  <!-- 
		    	   if( sTag.startsWith("?")) continue;     //  <?
		    	   // indien data bewaar
		    	   UpdateStack(sData);
		    	   //
		    	   PushStack( sTag , TrialRun );
		    	   String sPath = getFullPath();
		    	   if( !TrialRun ) PerformCallback( "startNode" , sTag , sData , sPath);
		    	   sData  = "";
		       }
		       continue;
		   }
		   
		   if (isTag ) {
			   sTag = sTag + (char)bt;
			   if( sTag.startsWith("![CDATA[") ) isCdata = true;
			   if( (isCdata) && (sTag.endsWith("]]")) ) isCdata = false;
	        //if( isCdata ) logit( sTag) ;
		   }
		   else sData = sData + (char)bt;
	   }
	   //
	  
	   // einde
	   String sPopTag=PopTag();
	   if( (sPopTag.compareTo("root"))!=0) {
		   logit("Non matching tags at end <" + sPopTag + "> </" + sTag + ">");
		   return false;
	   }
	   if( NodeLst.size() != 0) {
		   logit("There are still entries [" + NodeLst.size() + "] on the stack");
		   return false;
	   }
	   //
	}
	catch( Exception e)
	{
		logit("Error reading [" + FNaam + "] " + LogStackTrace(e));
		return false;
	}
	
	return true;
}

}

