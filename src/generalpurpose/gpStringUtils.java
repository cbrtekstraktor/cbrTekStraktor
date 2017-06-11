package generalpurpose;

public class gpStringUtils {
	
	gpStringUtils()
	{
		
	}
	//
	//---------------------------------------------------------------------------------
	int TelDelims ( String sIn , char ctKar )
	//---------------------------------------------------------------------------------
	{   
	    char[] SChar = sIn.toCharArray();
	    int aantal=0; 		
		for(int ii=0;ii<SChar.length;ii++) 
		{	
			if ( (SChar[ii] == ctKar) ) aantal++;
		}		
		return aantal;
	}
	//
	//---------------------------------------------------------------------------------
	String GetVeld( String sIn , int idx , char delim )
	//---------------------------------------------------------------------------------
	{ String sTemp="";
	  char[] sChar;
	  int ii;
	  int hit = 0;
	  
	  sTemp = delim + sIn.trim() + delim;
	  sChar = sTemp.toCharArray();
	  sTemp = "";
	  for(ii=0;ii<sChar.length;ii++)
	  {
		  if ( sChar[ii] == delim ) { hit++; continue; }
		  if ( hit == idx ) {
			  sTemp = sTemp + sChar[ii];
		  }
	  }
	  return sTemp;
	}
	//
	//---------------------------------------------------------------------------------
	String RemplaceerOLD( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
		        int e = 0;
		        int teller=0;
		        String sOut = sIn;
		        while ((e = sOut.indexOf(sPattern)) >= 0) {
		         String sPre="";
		         String sPost="";
		         String sTemp="";
		         e = sIn.indexOf(sPattern);
		         if( e >= 0) {
		        	sPre = sIn.substring(0,e);
		            sTemp = sPre + sReplace;
		            e += sPattern.length();
		            if( e < sIn.length() ) {
		                sPost = sIn.substring(e);
		                sTemp = sTemp + sPost;
		            }
		         }
		         sOut = sTemp;
		         teller++; if( teller>10) break;
		        } 
		        //System.out.println(sIn + "->" + sOut);
		        return sOut;
		
	}
	//
	//---------------------------------------------------------------------------------
	String RemplaceerIgnoreCase( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{
		        int e = 0;
		        int teller=0;
		        String sOut = sIn;
		        String sUpper = sIn.toUpperCase();
		        while ((e = sUpper.indexOf(sPattern.toUpperCase())) >= 0) {
		         String sPre="";
		         String sPost="";
		         String sTemp="";
		         e = sUpper.indexOf(sPattern.toUpperCase());
		         if( e >= 0) {
		        	sPre = sOut.substring(0,e);
		            sTemp = sPre + sReplace;
		            e += sPattern.length();
		            if( e < sOut.length() ) {
		                sPost = sOut.substring(e);
		                sTemp = sTemp + sPost;
		            }
		         }
		         sOut = sTemp;
		         sUpper = sTemp.toUpperCase();
		         teller++; if( teller>10) break;
		        } 
		        //System.out.println(sIn + "->" + sOut);
		        return sOut;
		
	}
	//
	//---------------------------------------------------------------------------------
	String Remplaceer( String sIn , String sPattern , String sReplace )
	// ---------------------------------------------------------------------------------
	{   // Remplaceer vervangt slechts eerste occurence
		String sOut = sIn;
		for(int i=0;i<100;i++)
		{
		  if( sOut.indexOf(sPattern)<0) break;
		  sOut = this.RemplaceerOLD(sOut, sPattern, sReplace);
		}
		return sOut;
	}
	//
	//---------------------------------------------------------------------------------
	public String keepLettersAndNumbers ( String sIn )
	//---------------------------------------------------------------------------------
	{   
		    char[] sBuf = sIn.toCharArray();
		    String sRet="";		
			for(int i=0;i<sBuf.length;i++) 
			{	
				if( ((sBuf[i]>='a')&&(sBuf[i]<='z')) || ((sBuf[i]>='A')&&(sBuf[i]<='Z')) || ((sBuf[i]>='0')&&(sBuf[i]<='9')) ) sRet += sBuf[i]; 
			}		
			return sRet;
	}
	//
	//---------------------------------------------------------------------------------
	public String keepNumbers ( String sIn )
	//---------------------------------------------------------------------------------
	{   
		    char[] sBuf = sIn.toCharArray();
		    String sRet="";		
			for(int i=0;i<sBuf.length;i++) 
			{	
				if( ((sBuf[i]>='0')&&(sBuf[i]<='9')) ) sRet += sBuf[i]; 
			}		
			return sRet;
	}
	//
	//---------------------------------------------------------------------------------
	public String Capitalize ( String sIn )
	//---------------------------------------------------------------------------------
	{   
		    char[] sBuf = sIn.toLowerCase().toCharArray();
		    char[] sCap = sIn.toUpperCase().toCharArray();
		    String sRet = "";		
			for(int i=0;i<sBuf.length;i++) 
			{	
				 if( i == 0 ) sRet += sCap[i]; else sRet += sBuf[i];
			}		
			return sRet;
	}

	//---------------------------------------------------------------------------------
	public String dotter ( long ilo )
	//---------------------------------------------------------------------------------
	{   
		try {
			return String.format("%,d",ilo);
		}
		catch( Exception e ) {
			return "??"+ilo;
		}
	}
}
