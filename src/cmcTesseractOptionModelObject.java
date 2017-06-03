
import logger.logLiason;
import ocr.cmcTesseractParameter;

import java.util.ArrayList;

import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import dao.cmcTesseractParameterDAO;



public class cmcTesseractOptionModelObject {

	cmcProcSettings xMSet = null;
	logLiason logger=null;
	cmcProcEnums cenum = null;
	cmcTesseractParameterDAO dao=null;
	
	
	ArrayList<cmcTesseractParameter> plist = null;
	
	
	//------------------------------------------------------------
    private void do_log(int logLevel , String sIn)
	//------------------------------------------------------------
    {
       if( logger != null ) logger.write( this.getClass().getName() , logLevel , sIn);
       else 
       if (logLevel == 0 ) System.err.println(sIn);
       else System.out.println(sIn);
    }
	//------------------------------------------------------------
    private void do_error(String sIn)
	//------------------------------------------------------------
    {
    	do_log(0,sIn);
    }
	
	//---------------------------------------------------------------------------------
	public cmcTesseractOptionModelObject(cmcProcSettings is , logLiason ilog )
	//---------------------------------------------------------------------------------
	{
		xMSet = is;
		logger = ilog;
		cenum = new cmcProcEnums(xMSet);
		dao = new cmcTesseractParameterDAO(xMSet,logger);
		initialize();
	}

	//---------------------------------------------------------------------------------
	private void initialize()
	//---------------------------------------------------------------------------------
	{
	    plist = dao.readAllParameters();
	    if( plist == null ) {
	    	plist = new ArrayList<cmcTesseractParameter>();
	    	cmcTesseractParameter x = new cmcTesseractParameter("Error","Error","Error");
	    	plist.add(x);
	    }
	}
	
	//---------------------------------------------------------------------------------
	public String getColumnName(int column )
	//---------------------------------------------------------------------------------
	{
      cmcProcEnums.TESS_OPTION_SELECTION x = cenum.getTessOptionAtIndex(column);
      return ( x == null ) ? "?" : xMSet.xU.Capitalize(""+x);
	}
	
	//
	//---------------------------------------------------------------------------------
	public int getRowCount() 
	//---------------------------------------------------------------------------------
	{
		return plist.size();
	}
	//
	//---------------------------------------------------------------------------------
	public int getColumnCount() 
	//---------------------------------------------------------------------------------
	{
	  return cmcProcEnums.TESS_OPTION_SELECTION.values().length;
	}
	//
	//---------------------------------------------------------------------------------
	public Class getColumnClass(int column) 
	//---------------------------------------------------------------------------------
	{
	   cmcProcEnums.TESS_OPTION_SELECTION  x = cenum.getTessOptionAtIndex( column );
	   if( x == null ) return String.class;
	   switch ( x )
	   {
		   case WITHOLD : return boolean.class;
		   default : return String.class;
	   }		
	}
	
	public boolean isCellEditable(int row, int col)
	{
	   cmcProcEnums.TESS_OPTION_SELECTION  x = cenum.getTessOptionAtIndex( col );
	   if( x == null ) return false;
	   switch ( x )
	   {
	   case WITHOLD : return true; // always
	   case PARAMETER : return false;
	   case DESCRIPTION : return false;
	   case VALUE : {
		   if( (row<0) || (row>=plist.size()) )  return false;
		   return plist.get(row).getWithold();
          }
	   default : return false;
	   }
	}
	
	
	public Object getItem(int row, int column)
	{
		 cmcProcEnums.TESS_OPTION_SELECTION  x = cenum.getTessOptionAtIndex( column );
		 if( x == null ) return "?getitem";
		 //
		 if( (row<0) || (row >= plist.size()) ) return "?error";
		 cmcTesseractParameter param = plist.get(row);
		 //
		 switch ( x )
		   {
		   case WITHOLD : return param.getWithold();
		   case PARAMETER : return param.getParameter();
		   case VALUE : return param.getValue();
		   case DESCRIPTION : return param.getDescription();
           default : return "?error";		   
		   }
	}
	
	public boolean setWithold(int row, boolean ib)
	{
		   if( (row<0) || (row>=plist.size()) )  return false;
           plist.get(row).setWithold(ib);
		   return true;
	}
	public boolean setValue(int row, String s)
	{
		   if( (row<0) || (row>=plist.size()) )  return false;
           plist.get(row).setValue(s);
		   return true;
	}
	
	public boolean propagateChanges()
	{
		if( dao == null ) return false;
		dao.overWriteTesseractOptionFile( plist );
		return true;
	}
}
