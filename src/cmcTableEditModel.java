import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.swing.table.*;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;
import drawing.cmcGraphController;

public class cmcTableEditModel extends AbstractTableModel {
	
		
	cmcProcSettings xMSet = null;
	cmcTableEditModelGraphPageObject GraphObject = null;
	cmcGraphController gcon=null;
	logLiason logger=null;
	
	private boolean contentHasBeenEdited=false;
	Object CallingClass=null;

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

	//
	//---------------------------------------------------------------------------------
	cmcTableEditModel(cmcProcSettings iS , Object o , logLiason ilog , cmcGraphController gc)
	//---------------------------------------------------------------------------------
	{
		xMSet = iS;
		CallingClass=o;
		logger=ilog;
		gcon=gc;
		contentHasBeenEdited=false;
	}
		
	// reads the file and shows the table - triggered by Window Open event
	//---------------------------------------------------------------------------------
	public void ReadDataAndShow(String ArchiveName)
	//---------------------------------------------------------------------------------
	{
		if( GraphObject != null ) GraphObject = null;
		GraphObject = new cmcTableEditModelGraphPageObject(xMSet,ArchiveName,logger,gcon);
		fireTableDataChanged();
	}
	// performs the selection of data again without reloading the XML file
	//---------------------------------------------------------------------------------
	public void ReDoSelection()
	//---------------------------------------------------------------------------------
	{
		GraphObject.createSelection();
		fireTableDataChanged();
	}
	//---------------------------------------------------------------------------------
	public int getMaxImageWidth()
	//---------------------------------------------------------------------------------
	{
		return GraphObject.getMaxImageWidth();
	}
	//
	//---------------------------------------------------------------------------------
	public String getColumnName(int column )
	//---------------------------------------------------------------------------------
	{
		if( GraphObject == null) return "?";
		return GraphObject.getColumnName(column);
	}
	//
	//---------------------------------------------------------------------------------
	public int getRowCount() 
	//---------------------------------------------------------------------------------
	{
		if( GraphObject == null) return 0;
		return GraphObject.getRowCount();
	}
	//
	//---------------------------------------------------------------------------------
	public int getColumnCount() 
	//---------------------------------------------------------------------------------
	{
		if( GraphObject == null) return 0;
		return GraphObject.getColumnCount();
	}
	//
	//---------------------------------------------------------------------------------
	public Class getColumnClass(int column) 
	//---------------------------------------------------------------------------------
	{
		if( GraphObject == null ) return String.class;
		return GraphObject.getColumnClass(column);
	}
	//
	//---------------------------------------------------------------------------------
	public Object getValueAt(int row , int column ) 
	//---------------------------------------------------------------------------------
	{
		if( row < 0 ) return "";
		if( row >= this.getRowCount () ) return "";
		if( GraphObject == null ) return "";
		return GraphObject.getItem(row, column);
	}
	//
	//---------------------------------------------------------------------------------
	public void execFireTableDataChanged() // MOST IMPORT FUNCTION OF ALL
	//---------------------------------------------------------------------------------
	{
			fireTableDataChanged();
	}
	//
	//---------------------------------------------------------------------------------
	public boolean toggleColSortOrder(int col)
	//---------------------------------------------------------------------------------
	{
		return true;
	}
	//
	//---------------------------------------------------------------------------------
	public boolean isCellEditable(int row, int col)
	//---------------------------------------------------------------------------------
	{
		// if not in edit mode only allow the edit the text
		if( gcon == null ){
			switch(xMSet.getQuickEditOption())
			{
			case TEXT_AREAS : {
				if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.EXTRACTED_TEXT))	{
					xMSet.setQuickEditRequestedRow(row);
					return true;
				}
				break;
			}
			case POTENTIAL_TEXT_AREAS : {
				if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ISTEXT)) return true;
				break;
			}
			default : break;
			}
			return false;
		}
		//
		switch(xMSet.getQuickEditOption())
		{
		case TEXT_AREAS : {
			if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ISTEXT)) return true;
			if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.EXTRACTED_TEXT))  {
				xMSet.setQuickEditRequestedRow(row);
				return true;
			}
			if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.REMOVED)) return true;
			break;
		}
		case POTENTIAL_TEXT_AREAS : {
			if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ISTEXT)) return true;
			if( col==cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.REMOVED)) return true;
			break;
		}
		default : break;
		}
		return false;
	}
	//
	//---------------------------------------------------------------------------------
	public void setValueAt(Object value, int row , int column )
	//---------------------------------------------------------------------------------
	{
		// Only when text paragraph are displayed
		if( isCellEditable(-1,column) == false ) return;
		//
		if( column == cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.ISTEXT) ) {
			if( GraphObject.toggleIsText(row) == false ) return;
		}
		else 
		if( column == cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.EXTRACTED_TEXT) ) {
			String txt = value.toString();
			if( txt != null ) {
				if( txt.toUpperCase().trim().startsWith("<HTML>") && (txt.toUpperCase().trim().endsWith("</HTML>")) ) {
					do_error("STRIPPEN =>" + txt);
				}
			}
			xMSet.setQuickEditRequestedRow(-1);
		    if( GraphObject.setText(row,txt) == false ) return;  
		}
		else 
		if( column == cmcProcEnums.getQuickColsIndex(cmcProcEnums.QUICKCOLS.REMOVED) ) {
		    if( GraphObject.toggleRemoved(row) == false) return; 
		}
		else {
			do_error("setValue (" + row + "," + column + ")");
			return; 
		}
		contentHasBeenEdited=true;
		PerformCallback("checkButtonStatus");
		//do_error("Content Has been edited [" + contentHasBeenEdited + "]");
	}
	//---------------------------------------------------------------------------------
	public boolean ContentHasBeenEdited()
	//---------------------------------------------------------------------------------
	{
		return contentHasBeenEdited;
	}
	//---------------------------------------------------------------------------------
	public String getCMXUID()
	//---------------------------------------------------------------------------------
	{
		return GraphObject.getCMXUID();
	}
	//---------------------------------------------------------------------------------
	public boolean propagateChanges(cmcGraphController gCon)
	//---------------------------------------------------------------------------------
	{
		return this.GraphObject.propagateChanges(gCon);
	}
	//---------------------------------------------------------------------------------
	public boolean propagateChangesAfterOCR()
	//---------------------------------------------------------------------------------
	{
		return this.GraphObject.propagateChangesAfterOCR();
	}
	//---------------------------------------------------------------------------------
	boolean PerformCallback(String sCallbackFunction  )
	//---------------------------------------------------------------------------------
	{
	try {
		Class<?> c = Class.forName(CallingClass.getClass().getName());
		
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
			    Object o = m.invoke(CallingClass);
			    return true;
			// Handle any exceptions thrown by method to be invoked.
			} catch (InvocationTargetException x) {
			    Throwable cause = x.getCause();
			    System.out.println("invocation failed" +   mname + cause.getMessage());  // DO NOT CHANGE TO LOGIT
			    return false;
			}
	  }
	}
	catch( Exception e) {
		  System.out.println("Error while invoking method" +  e.getMessage()); // DO NOT CHANGE TO LOGIT
		  return false;
	}
	return false;
	}

}
