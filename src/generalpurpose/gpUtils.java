package generalpurpose;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class gpUtils {

	private gpStringUtils strUtil = null;
	public char ctSlash = '\\';
	public String ctEOL = "\r\n";
	
			
	//---------------------------------------------------------------------------------
	public gpUtils()
	//---------------------------------------------------------------------------------
	{
		strUtil = new gpStringUtils();
		ctSlash = System.getProperty("file.separator").toCharArray()[0];
		ctEOL = System.getProperty("line.separator");
	}
	
	//
	//---------------------------------------------------------------------------------
	private void logit(int level , String sIn )
	//---------------------------------------------------------------------------------
	{
		if( level != 0) System.out.println(sIn); else System.err.println(sIn);
	}
	//
	//---------------------------------------------------------------------------------
    String RPad(String sIn, int len)
    //---------------------------------------------------------------------------------
    {
      if( sIn == null ) sIn ="";
      int j=len-sIn.length();
      for(int i=0;i<j;i++) sIn = sIn + " ";
      return sIn.substring(0,len);
    }
    //
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
	//
	//---------------------------------------------------------------------------------
	public String GetFileSpecs(String sF)
	//---------------------------------------------------------------------------------
	{           String sTemp="";
			    File fObj = new File(sF);
			    if ( fObj.exists() == true )
			    {
				 if ( fObj.isFile() == true ) {
			     sTemp =  "NME=" + fObj.getName() +
				         "|MOD=" + fObj.lastModified() +   
				         "|LEN=" + fObj.length() + 
				         "|PAR=" + fObj.getParent();
				 }
			    } 
				return sTemp;
	}
	//
	//---------------------------------------------------------------------------------
	public String GetFileName(String sF)
	//---------------------------------------------------------------------------------
	{           
			    File fObj = new File(sF);
			    if ( fObj.exists() == true )
			    {
				 if ( fObj.isFile() == true ) return fObj.getName();
			    } 
				return null;
	}
	//
	//---------------------------------------------------------------------------------
	public String GetParent(String sF)
	//---------------------------------------------------------------------------------
	{           
			    File fObj = new File(sF);
			    if ( fObj.exists() == true )
			    {
				 if ( fObj.isFile() == true ) return fObj.getParent();
			    } 
				return null;
	}
	//
	//---------------------------------------------------------------------------------
	public long getModificationTime(String sF)
	//---------------------------------------------------------------------------------
	{         
			    File fObj = new File(sF);
			    if ( (fObj.exists() == true) && (fObj.isFile() == true) )
			    {
				 	 return fObj.lastModified();
			    }  
				return -1L;
	}
	//
	//---------------------------------------------------------------------------------
	public long getFileSize(String sF)
	//---------------------------------------------------------------------------------
	{         
			    File fObj = new File(sF);
			    if ( (fObj.exists() == true) && (fObj.isFile() == true) )
			    {
				 	 return fObj.length();
			    }  
				return -1L;
	}
	//
	//---------------------------------------------------------------------------------
	String LPadZero( String sIn , int lengte )
	//---------------------------------------------------------------------------------
	{   
			    	int ii;
			    	String sZero = "";
			    	for(ii=0;ii<lengte;ii++) sZero = sZero + "0";
			    	return sZero.substring(0,lengte-sIn.length()) + sIn;
	}
	//---------------------------------------------------------------------------------
	public String GetSuffix( String FNaam )
	//---------------------------------------------------------------------------------
	{
		try {
			  int idx =	FNaam.lastIndexOf('.');
			  if( idx < 0 ) return "";
		 	  return FNaam.substring(  idx + 1 , FNaam.length() ).toUpperCase();
		}
		catch( Exception e )  { return ""; }
	}
	//
	//---------------------------------------------------------------------------------
	public int TelDelims ( String sIn , char ctKar )
	//---------------------------------------------------------------------------------
	{   
	    return strUtil.TelDelims(sIn, ctKar);
	}
	//
	//---------------------------------------------------------------------------------
	public String GetVeld( String sIn , int idx , char delim )
	//---------------------------------------------------------------------------------
	{ 
		return strUtil.GetVeld(sIn, idx, delim);
	}
	//
	//---------------------------------------------------------------------------------
	String RemplaceerOLD( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
		return strUtil.Remplaceer(sIn, sPattern, sReplace);
	}
	//
	//---------------------------------------------------------------------------------
	public String RemplaceerIgnoreCase( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
        return strUtil.RemplaceerIgnoreCase(sIn, sPattern, sReplace);
	}
	//
	//---------------------------------------------------------------------------------
	public String Remplaceer( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{   
		return strUtil.Remplaceer(sIn,sPattern,sReplace);
	}


	//---------------------------------------------------------------------------------
	public int NaarInt(String sIn)
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
	//
	//---------------------------------------------------------------------------------
	public long NaarLong(String sIn)
	//---------------------------------------------------------------------------------
	{
				 long ll=-1;
					
				 try {
					  ll=Long.parseLong( sIn );
					  return ll;
					 }
					 catch ( NumberFormatException e)
					 {
						 return -1;
					 }
	}
	//
	//---------------------------------------------------------------------------------
	public double NaarDouble(String sIn)
	//---------------------------------------------------------------------------------
	{
				 double ll=-1;
					
				 try {
					  ll=Double.parseDouble( sIn );
					  return ll;
					 }
					 catch ( NumberFormatException e)
					 {
						 return -1;
					 }
	}
	//---------------------------------------------------------------------------------
	public boolean ValueInBooleanValuePair(String sIn)
	//---------------------------------------------------------------------------------
	{
		   String sWaarde = this.GetVeld(sIn, 2 , '=');
		   if( sWaarde.trim().toUpperCase().compareTo("Y")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("YES")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("1")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("J")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("TRUE")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("ON")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("JA")==0 ) return true;
		   if( sWaarde.trim().toUpperCase().compareTo("OUI")==0 ) return true;
		   return false;	
	}
	//
	//---------------------------------------------------------------------------------
	public boolean IsDir( String sDir )
	//---------------------------------------------------------------------------------
	{
				try {
				 File fObj = new File(sDir);
				 if ( fObj.exists() == true )
				 {
					if ( fObj.isDirectory() == true ) return true;
				 }
				 return false;
				} catch ( Exception e ) {
					e.printStackTrace();
					return false;
				}
	}
	//
	//---------------------------------------------------------------------------------
	public boolean IsBestand( String sIn )
	//---------------------------------------------------------------------------------
	{
				if( sIn == null ) return false;
				try {
				 File fObj = new File(sIn);
				 if ( fObj.exists() == true )
				 {
					if ( fObj.isFile() == true ) return true;
				 } 
				 return false;
				} catch ( Exception e ) {
					e.printStackTrace();
					return false;
				}
	}
	//
	//---------------------------------------------------------------------------------
	public ArrayList<String> GetFilesInDir( String sDirName , String sPatroon)
	//---------------------------------------------------------------------------------
			{
				ArrayList<String> sLijst = new ArrayList<String>();
				File  dirObj = new File( sDirName );
				{
					if ((dirObj.exists() == true)  ) {
						if (dirObj.isDirectory() == true) {
							File [] fileList = dirObj.listFiles();
							for (int i = 0; i < fileList.length; i++) {
								if (fileList[i].isDirectory()) continue;
								if (fileList[i].isFile()) {
									if( sPatroon != null ) {
										/*
									  if ( fileList[i].getName().length() >= sPatroon.length() ) {
										if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
										sLijst.add(fileList[i].getName());
									  }
									*/
									 if ( fileList[i].getName().length() >= sPatroon.length() ) {
									 		if ( fileList[i].getName().toUpperCase().indexOf(sPatroon.toUpperCase()) < 0 ) continue;
											sLijst.add(fileList[i].getName()); 	}
									} else {
										sLijst.add(fileList[i].getName());
									}
								}
							}
						}
					}
				}		
				return sLijst;
	}
	//---------------------------------------------------------------------------------
	int countFilesInDir( String sDirName , String sPatroon)
	//---------------------------------------------------------------------------------
	{  int teller=0;
	
					sPatroon = sPatroon.toUpperCase();
					File  dirObj = new File( sDirName );
					{
						if ((dirObj.exists() == true)  ) {
							if (dirObj.isDirectory() == true) {
								File [] fileList = dirObj.listFiles();
								for (int i = 0; i < fileList.length; i++) {
									if (fileList[i].isDirectory()) continue;
									if (fileList[i].isFile()) {
										if( sPatroon != null ) {
										  if ( fileList[i].getName().length() >= sPatroon.length() ) {
											//if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
											  if( fileList[i].getName().toUpperCase().indexOf(sPatroon) < 0 ) continue;
											teller++;
										  }
										} else {
											teller++;
										}
									}
								}
							}
						}
					}		
					return teller;
	}
			//
			//---------------------------------------------------------------------------------
			ArrayList<String> GetDirsInDir( String sDirName , String sPatroon)
			//---------------------------------------------------------------------------------
			{
				ArrayList<String> sLijst = new ArrayList<String>();
				File  dirObj = new File( sDirName );
				{
					if ((dirObj.exists() == true)  ) {
						if (dirObj.isDirectory() == true) {
							File [] fileList = dirObj.listFiles();
							for (int i = 0; i < fileList.length; i++) {
								if (fileList[i].isFile()) continue;
								if (fileList[i].isDirectory()) {
									if( sPatroon != null ) {
									  if ( fileList[i].getName().length() >= sPatroon.length() ) {
										if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
										sLijst.add(fileList[i].getName());
									  }
									} else {
										sLijst.add(fileList[i].getName());
									}
								}
							}
						}
					}
				}		
				return sLijst;
			}
			//
			//---------------------------------------------------------------------------------
			public ArrayList<String> GetDirsInDir( String sDirName )
			//---------------------------------------------------------------------------------
			{
				return GetDirsInDir( sDirName , null);
			}
			//
			//---------------------------------------------------------------------------------
			public ArrayList<String> GetFilesInDirRecursive( String sDirName , String sPatroon)
			//---------------------------------------------------------------------------------
			{
				ArrayList<String> sLijst = new ArrayList<String>();
				File  dirObj = new File( sDirName );
				{
					if ((dirObj.exists() == true)  ) {
						if (dirObj.isDirectory() == true) {
							File [] fileList = dirObj.listFiles();
							for (int i = 0; i < fileList.length; i++) {
								// Afdalen
								if (fileList[i].isDirectory()) {
									ArrayList<String> xL = GetFilesInDirRecursive( fileList[i].getAbsolutePath() , sPatroon);
									for(int k=0;k<xL.size();k++) sLijst.add(xL.get(k));
								}
								if (fileList[i].isFile()) {
									if( sPatroon != null ) {
									  if ( fileList[i].getName().length() >= sPatroon.length() ) {
										if ( fileList[i].getName().substring(0,sPatroon.length()).compareToIgnoreCase(sPatroon)!=0) continue;
										sLijst.add(fileList[i].getAbsolutePath());
									  }
									} else {
										sLijst.add(fileList[i].getAbsolutePath());
									}
								}
							}
						}
					}
				}		
				return sLijst;
			}
			//
			//---------------------------------------------------------------------------------
			ArrayList<String> GetFilesInDir( String sDirName )
			//---------------------------------------------------------------------------------
			{
				return GetFilesInDir( sDirName , null);
			}
			//
	
			//---------------------------------------------------------------------------------
			public boolean CreateDirectory(String sDirNaam)
			//---------------------------------------------------------------------------------
			{
				if( this.IsDir( sDirNaam ) ) return true; // bestaat
				boolean success = (new File(sDirNaam)).mkdir();
				if( success == true ) return this.IsDir( sDirNaam );
				return false;
			}
			//
			//---------------------------------------------------------------------------------
			public void copyFile(String sIn , String sOut) throws IOException 
			//---------------------------------------------------------------------------------
			{
				
				   InputStream in = null;
				   OutputStream out = null; 
				   byte[] buffer = new byte[16384];
				   try {
				      in = new FileInputStream(sIn);
				      out = new FileOutputStream(sOut);
				      while (true) {
				         synchronized (buffer) {
				            int amountRead = in.read(buffer);
				            if (amountRead == -1) {
				               break;
				            }
				            out.write(buffer, 0, amountRead); 
				         }
				      } 
				   } finally {
				      if (in != null) {
				         in.close();
				      }
				      if (out != null) {
				    	 out.flush();
				         out.close();
				      }
				   }
			    
			}
			//
			//---------------------------------------------------------------------------------
			public String ReadContentFromFile(String FNaam, int MaxLines,String CodePage)
			//---------------------------------------------------------------------------------
			{
				String sRet= "";
				int teller=0;
				try {
				  File inFile  = new File(FNaam);  // File to read from.
		       	  //BufferedReader reader = new BufferedReader(new FileReader(inFile));
		       	  BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FNaam), CodePage));
		       	  String sLijn = null;
		          while ((sLijn=reader.readLine()) != null) {
		        	teller++; if( teller > 1) sRet = sRet + "\n";
		        	sRet = sRet + sLijn;
		        	if( teller > MaxLines ) {
		        		sRet = sRet + "\n\n --> Maximum  number [" + MaxLines + "] of display lines has been reached. \n --> Use alternative editor to view file [" + FNaam + "]";
		        		break;
		        	}
		          }
		          reader.close();
		          return ( sRet );
				}
				catch (Exception e) {
					return ("Error reading file [" + FNaam + "]");
			    }
			}
			
		    //
			//---------------------------------------------------------------------------------
		    String getHTMLContent( String sIn )
		    //---------------------------------------------------------------------------------
		    {
		      if( sIn == null ) return null;
		      String sRet = "";
		      char[] buf = sIn.toCharArray();
		      boolean inBetweenTag=false;
		      for(int i=0;i<buf.length;i++)
		      {
		    	  if( buf[i] == '<' ) { inBetweenTag = true; continue; }
		    	  if( buf[i] == '>' ) { inBetweenTag = false; continue; }
		     	  if( inBetweenTag ) continue;
		     	  sRet = sRet + buf[i];
		      }
		      return sRet;
		    }
		    
		    //---------------------------------------------------------------------------------
			public boolean VerwijderBestand( String sIn)
			//---------------------------------------------------------------------------------
			{
		        File FObj = new File(sIn);
		        if ( FObj.isFile() != true ) {
		        	logit( 0 ,"ERROR '" + sIn + ") -> file not found");
		        	return false;
		        }
		        if ( FObj.getAbsolutePath().length() < 10 ) {
		        	logit( 0 , sIn + "-> Length too small. File will not be deleted");
		        	return false;  // blunt safety
		        }
		        FObj.delete();
		        File XObj = new File(sIn);
		        if ( XObj.isFile() == true ) {
		        	logit(0,"ERROR" + sIn+ " -> could not be deleted");	
		        }
		        return true;
			}
			//---------------------------------------------------------------------------------
			String HouLettersEnCijfers(String sIn)
			//---------------------------------------------------------------------------------
			{
				String sTemp = "";
			    char[] SChar = sIn.toCharArray();
			    for(int ii=0;ii<SChar.length;ii++) 
				{	
					if ( ((SChar[ii] >= '0') && (SChar[ii] <= '9')) ||
						 ((SChar[ii] >= 'A') && (SChar[ii] <= 'Z')) ||
						 ((SChar[ii] >= 'a') && (SChar[ii] <= 'z')) 
						) sTemp = sTemp + SChar[ii];
				}		
				return sTemp;
			    	
			}
			//
			//---------------------------------------------------------------------------------
		    public String GetValueFromTagBuffer( String sBuffer , String sTag )
		    //---------------------------------------------------------------------------------
		    {
		    	String sRet = null;
    	    	//
		    	if ( sTag == null ) return null;
		    	// 
		    	if( sBuffer.indexOf(sTag) < 0 ) return null;
		    	// <tag> </tag>
		    	if( sBuffer.indexOf( "<"+sTag+">") >= 0) {
		    		int istart = sBuffer.indexOf( "<"+sTag+">") + sTag.length() + 2;
		    		int istop  = sBuffer.indexOf( "</"+sTag+">");
		    		if( (istart <  istop) && (istop>=0) ) {
		    			try {
		    			 sRet = sBuffer.substring(istart,istop);
		    			}
		    			catch( Exception e) { sRet=null; };
		    		}
		    		return sRet;
		    	}
		    	
		        //  href=""   -> lees alles dat na = komt en tussen quotes
		    	int istart=-1;
		    	if( (istart=sBuffer.indexOf(" " + sTag + "=")) >= 0 ) {
		    		 String sTemp = sBuffer.substring(istart);   //  je hebt nu  tag="iets" , knip dus 2de value met delim "
		    		 sRet = strUtil.GetVeld(sTemp,2,'"');  
		    	}
		    	return sRet;
		    }
		    //
		 	//---------------------------------------------------------------------------------
			String getXMLValueIdx(String FNaam , String sTag  , int idx , String CodePage)
			//---------------------------------------------------------------------------------
			{
				int teller=0;
				if( this.IsBestand(FNaam) == false ) return null;
				String sText = this.ReadContentFromFile(FNaam, 1000 , CodePage);
				int aantal = this.TelDelims(sText,'\n');
		    	for(int i=0;i<=aantal;i++)
				{
					String sLijn = this.GetVeld(sText,(i+1),'\n').trim();
					if( sLijn == null ) continue;
					if (sLijn.length() == 0 ) continue;
					if( sLijn.indexOf("<"+sTag+">") < 0 )  continue;
					teller++;
					if( teller < idx ) continue;
					if( teller > idx ) break;
					sLijn = this.GetValueFromTagBuffer(sLijn,sTag);
					if( sLijn == null ) sLijn = "";
					return sLijn;
				}
				return null;
			}
			//---------------------------------------------------------------------------------
			public String extractXMLValueV2(String sin , String tag)
			//---------------------------------------------------------------------------------
			{
				try {
				 if( sin == null ) return null;
				 if( tag == null ) return null;
				 if( tag.length() < 1) return null;
				 sin = sin.trim();
				 if( sin.length() < 7) return null;
				 int sta = sin.toUpperCase().indexOf( ("<" + tag.trim().toUpperCase() + ">") );
				 if( sta < 0 ) return null;
				 sta += ("<" + tag + ">").length();
				 int sto = sin.toUpperCase().indexOf( ("</" + tag.trim().toUpperCase() + ">") );
				 if( sto <  sta) return null;
				 String cut = sin.substring(sta,sto);
				 return cut;
				}
				catch(Exception e ) {
				  return null;
				}
			}

			 //
		 	//---------------------------------------------------------------------------------
			String getXMLValue(String FNaam , String sTag , String CodePage )
		 	//---------------------------------------------------------------------------------
			{
				return getXMLValueIdx(FNaam,sTag,1,CodePage);
			}
            //
			//---------------------------------------------------------------------------------
			String getDirHash(String sDirName)
			//---------------------------------------------------------------------------------
			{
				String sRet = null;
			    String sNoHash=null;
				File  dirObj = new File( sDirName );
				{
					if ((dirObj.exists() == true)  ) {
						if (dirObj.isDirectory() == true) {
							File [] fileList = dirObj.listFiles();
							sNoHash = "";
							for (int i = 0; i < fileList.length; i++) {
								if (fileList[i].isDirectory()) continue;
								if (fileList[i].isFile()) {
									    String sSpec = this.GetFileSpecs( sDirName + "//" + fileList[i].getName());
									    sSpec = this.Remplaceer(sSpec,("PAR="+sDirName).trim(),"");
										sNoHash = sNoHash + sSpec;
								}
							}
						}
					}
				}		
				if( sNoHash == null ) {
					logit(0,"Cannot calculate MD5 hash for Directory [" + sDirName + "]");
					return null;
				}
				sRet = makeMD5Hex(sNoHash);
				//System.out.println("MD5 ->>"+sRet);
				return sRet;
			}
			//
			//---------------------------------------------------------------------------------
			public String makeMD5Hex(String sIn)
			//---------------------------------------------------------------------------------
			{
				try {
					   String s = sIn;
					   MessageDigest md5 = MessageDigest.getInstance("MD5");
					   md5.update(s.getBytes(),0,s.length());
					   BigInteger xBig = new BigInteger(1,md5.digest());
					   String sRet = String.format("%032x",xBig);
					   return sRet.toUpperCase();

					} catch (Exception e) {
						logit(0,"MD5 checksum [" + sIn + "]");
					    logit(0,this.LogStackTrace(e));
					    return null;
					}
			}
			
			//
			//---------------------------------------------------------------------------------
			String removeXMLEscape(String sIn)
			//---------------------------------------------------------------------------------
			{
				String sRet = sIn;
				if( sRet.indexOf("&") < 0 ) return sRet;
				sRet = this.Remplaceer(sRet,"&amp;","&");
				sRet = this.Remplaceer(sRet,"&quot;", "\"");
				sRet = this.Remplaceer(sRet,"&lt;", "<");
				sRet = this.Remplaceer(sRet,"&gt;", ">");
				sRet = this.Remplaceer(sRet,"&apos;", "'");
				return sRet;
			}
			
			//
			//---------------------------------------------------------------------------------
			public String getMSDosDrive(String sIn)
			//---------------------------------------------------------------------------------
			{
				String sRet = "";
				char[] SChar = sIn.toUpperCase().toCharArray();
			    int len = sIn.length();
			    if( len < 3 ) return null;
			    if( SChar[1] != ':') return null;
			    if( (SChar[0] >= 'A') && (SChar[0] <= 'Z') ) sRet = ""+SChar[0]+":";
			    return sRet;
			}
			//
		    // ---------------------------------------------------------------------------------
			public boolean isValidURL(String sUrl)
			// ---------------------------------------------------------------------------------
			{
				try
			    {
			         URL url = new URL(sUrl);
			         return true;
			    }catch(Exception e)
			    {
			    	 //logit("Not a valid URL [" + sUrl + "]");
			         //logit(LogStackTrace(e));
			         return false;
			    }
			}
			//
		    // ---------------------------------------------------------------------------------
			public boolean isGrafisch(String sF)
		    // ---------------------------------------------------------------------------------
			{
				if( sF.toLowerCase().endsWith(".jpg")) return true;
				if( sF.toLowerCase().endsWith(".jpeg")) return true;
				if( sF.toLowerCase().endsWith(".png")) return true;
				if( sF.toLowerCase().endsWith(".gif")) return true;
				return false;
			}
			//
		    // ---------------------------------------------------------------------------------
			public String getHostNameFromURL(String sUrl)
			// ---------------------------------------------------------------------------------
			{
				if( sUrl.indexOf("http")!=0) sUrl = "http://" + sUrl;
				try
			    {
			         URL url = new URL(sUrl);
			         return url.getHost();
			    }catch(Exception e)
			    {
			    	 //logit("Not a valid URL [" + sUrl + "]");
			         //logit(LogStackTrace(e));
			         return "";
			    }
			}
			
			//
		    // ---------------------------------------------------------------------------------
			public String[] sortStringArray(String[] in)
			// ---------------------------------------------------------------------------------
			{
				int aantal = in.length;
				String[] lst = new String[aantal];
				for(int i=0;i<aantal;i++) lst[i] = in[i];
				for(int i=0;i<aantal;i++)
				{
					boolean swap=false;
					for(int j=0;j<(aantal-1);j++)
					{
						if( lst[j].compareToIgnoreCase(lst[j+1]) > 0 ) {
							String s = lst[j];
							lst[j] = lst[j+1];
							lst[j+1] = s;
							swap=true;
						}
					}
					if( swap == false ) break;
				}
				return lst;
			}
			//
		    // ---------------------------------------------------------------------------------
			public String prntStandardDateTime(long l)
			// ---------------------------------------------------------------------------------
			{
				return prntDateTime(l,"dd-MMM-yyyy HH:mm:ss");
			}
			//
		    // ---------------------------------------------------------------------------------
			public String prntDateTime(long l,String sPattern)
			// ---------------------------------------------------------------------------------
			{
				Date date = new Date(l);
				SimpleDateFormat ft = new SimpleDateFormat (sPattern);
			    return ft.format(date).trim();
			}
			// ---------------------------------------------------------------------------------
			public String prntDateTimeISODate(long lDate,String sPattern)
		    // ---------------------------------------------------------------------------------
			{
				try {
				    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
			        java.util.Date parsed = format.parse((""+lDate).trim());
			        java.sql.Date sql = new java.sql.Date(parsed.getTime());
			        SimpleDateFormat ft = new SimpleDateFormat (sPattern);
				    return ft.format(sql).trim();
			  }
				catch (Exception e) {
					return ""+lDate;
				}
			}
			//
			//---------------------------------------------------------------------------------
			public String extractXMLValue(String sLijn , String sTag)
			//---------------------------------------------------------------------------------
			{
		        if( sLijn.toUpperCase().indexOf("<"+sTag.toUpperCase()+">") < 0) return null;
		        String sRet = sLijn;
		        sRet = this.RemplaceerIgnoreCase(sRet,"<"+sTag+">","");
		        sRet = this.RemplaceerIgnoreCase(sRet,"</"+sTag+">","").trim();
		 		return sRet;
			}
			//---------------------------------------------------------------------------------
			public URL maakFileURL(String FNaam)
			//---------------------------------------------------------------------------------
			{
				if( IsBestand( FNaam ) == false ) return null;
			    URL imgURL = null;
			    String sURL = "file:///" + FNaam;
		        try {
		    		imgURL = new URL( sURL );
		    	}
		    	catch( Exception e ) {
		    	    	imgURL = null;
		    	    	System.err.println( "[" + sURL + "] not a valid url" + e.getMessage() );
		    	}
		        return imgURL;
			}
			//---------------------------------------------------------------------------------
			public int getIdxFromList( String lst[] , String s)
			//---------------------------------------------------------------------------------
			{
				for(int i=0;i<lst.length;i++)
				{
					if( s.compareToIgnoreCase( lst[i] ) == 0 ) return i;
				}
				// it is possible that nothing is found if the underscores have been replaced by spaces
				String sel = this.Remplaceer(s," ","");
				for(int i=0;i<lst.length;i++)
				{
					if( sel.compareToIgnoreCase( lst[i] ) == 0 ) return i;
				}
				return -1;
			}
			//---------------------------------------------------------------------------------
			public String keepLettersAndNumbers ( String sIn )
			//---------------------------------------------------------------------------------
			{
				return strUtil.keepLettersAndNumbers(sIn);
			}
			//---------------------------------------------------------------------------------
			public String keepNumbers ( String sIn )
			//---------------------------------------------------------------------------------
			{
				return strUtil.keepNumbers(sIn);
			}
			//---------------------------------------------------------------------------------
			public String Capitalize ( String sIn )
			//---------------------------------------------------------------------------------
			{
				return strUtil.Capitalize(sIn);
			}
			//---------------------------------------------------------------------------------
			public String getParentFolderName(String sAbs)
			//---------------------------------------------------------------------------------
			{
				try {
					int idx = sAbs.lastIndexOf( this.ctSlash );
					String FolderName = sAbs.substring( 0 , idx );
					return FolderName;
				}
				catch (Exception e ) { return null;	}
			}
			//---------------------------------------------------------------------------------
			public String getFolderOrFileName(String sAbs)
			//---------------------------------------------------------------------------------
			{
				try {
					int idx = sAbs.lastIndexOf( this.ctSlash );
					String FName = sAbs.substring( idx+1 );
					return FName;
				}
				catch (Exception e ) { return null;	}
			}
			//---------------------------------------------------------------------------------
			public String dotter(long ilo)
			//---------------------------------------------------------------------------------
			{
				return strUtil.dotter(ilo);
			}
		
			}
