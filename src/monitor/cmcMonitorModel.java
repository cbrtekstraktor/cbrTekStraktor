package monitor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.swing.table.AbstractTableModel;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;


public class cmcMonitorModel  extends AbstractTableModel {
	
	cmcProcSettings xMSet = null;
	cmcMonitorModelObject GraphObject = null;
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
	cmcMonitorModel(cmcProcSettings iS , Object o , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
		xMSet = iS;
		CallingClass=o;
		logger=ilog;
    	contentHasBeenEdited=false;
	}
	
        // reads the file and shows the table - triggered by Window Open event
		//---------------------------------------------------------------------------------
		public void ReadDataAndShow()
		//---------------------------------------------------------------------------------
		{
			if( GraphObject != null ) GraphObject = null;
			GraphObject = new cmcMonitorModelObject(xMSet,logger);
			fireTableDataChanged();
		}
		// performs the selection of data again without reloading the XML file
		//---------------------------------------------------------------------------------
		public void ReDoSelection()
		//---------------------------------------------------------------------------------
		{
			//GraphObject.createSelection();
			fireTableDataChanged();
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
			if( GraphObject == null ) return false;
			return GraphObject.isCellEditable(row, col);
		}
		//
		//---------------------------------------------------------------------------------
		public void setValueAt(Object value, int row , int column )
		//---------------------------------------------------------------------------------
		{
			if( isCellEditable(row,column) == false ) return;
			/*
			if( column == cmcProcEnums.getTessOptionIndex(cmcProcEnums.TESS_OPTION_SELECTION.WITHOLD) ) {
				String s = value.toString();
				if( s == null ) return;
				s = s.trim().toUpperCase();
				boolean ib = ( s.startsWith("TRUE") ) ? true : false;
				if( GraphObject.setWithold(row, ib) == false ) return;
			}
			else
			if( column == cmcProcEnums.getTessOptionIndex(cmcProcEnums.TESS_OPTION_SELECTION.VALUE) ) {
				String s = value.toString();
				if( s == null ) return;
				if( GraphObject.setValue(row, s) == false ) return;
			}
			else {
				do_error("setValue (" + row + "," + column + ")");
				return;
			}
			contentHasBeenEdited=true;
            PerformCallback("setConfirm");
            */			
		}
		
		//---------------------------------------------------------------------------------
		public boolean ContentHasBeenEdited()
		//---------------------------------------------------------------------------------
		{
			return contentHasBeenEdited;
		}

		
		public boolean upsertMonitorItemLine(cmcMonitorItem x )
		{
			return this.GraphObject.upsertMonitorItemLine( x );
		}
		
		public boolean propagateChanges()
		{
			return this.GraphObject.propagateChanges();
		}
		
		public int getNumberOfUncompletedItems()
		{
			return this.GraphObject.getNumberOfUnCompletedItems();
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
