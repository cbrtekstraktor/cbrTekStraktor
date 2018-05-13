import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class cmcMenuShared {

	Object parentProcess = null;
	
	 cmcMenuShared(Object pi)
	 {
		 parentProcess = pi;
	 }
	 
	//---------------------------------------------------------------------------------
	public String makeLabel(String sIn)
	//---------------------------------------------------------------------------------
	{
			String sRet = "";
			String sTemp = sIn.trim().toUpperCase();
			char[] SChar = sTemp.toCharArray();
		    for(int i=0;i<SChar.length;i++) 
			{	
			   if( i == 0 ) {
				   sRet += SChar[i];
				   sTemp = sTemp.toLowerCase();
				   SChar = sTemp.toCharArray();
				   continue;
			   }
			   if( SChar[i] == '_') SChar[i]=' ';
			   sRet += SChar[i];
				
			}		
			return sRet;
	}
	
	//---------------------------------------------------------------------------------
	boolean performCallback(String sCallbackFunction )
	//---------------------------------------------------------------------------------
	{
		return performCallback(sCallbackFunction,null);
	}
	
	//---------------------------------------------------------------------------------
	boolean performCallback(String sCallbackFunction , String param)
	//---------------------------------------------------------------------------------
	{
	//System.err.println( sCallbackFunction );
	try {
	  Class<?> c = Class.forName(parentProcess.getClass().getName());
	  Method[] allMethods = c.getDeclaredMethods();
	  for (Method m : allMethods) {
	  	    String mname = m.getName();
			if (mname.compareTo(sCallbackFunction) != 0 ) continue;
			Type[] pType = m.getGenericParameterTypes();
			try {
			    m.setAccessible(true);
			    if( param == null ) { Object o = m.invoke( parentProcess ); }
			    if( param != null ) { Object o = m.invoke( parentProcess , param ); }
			    return true;
			} catch (InvocationTargetException x) {
			    Throwable cause = x.getCause();
			    System.out.println("invocation failed" +   mname + cause.getMessage());  // DO NOT CHANGE TO LOGIT
			    return false;
			}
	  }
	  
	}
	catch( Exception e) {
		  System.out.println("Error while invoking method" + e.getMessage()); // DO NOT CHANGE TO LOGIT
		  return false;
	}
	return false;
	}
}
