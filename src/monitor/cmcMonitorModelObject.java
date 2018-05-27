package monitor;
import java.util.ArrayList;

import logger.logLiason;
import cbrTekStraktorModel.cmcProcEnums;
import cbrTekStraktorModel.cmcProcSettings;


public class cmcMonitorModelObject {

	cmcProcSettings xMSet = null;
	logLiason logger=null;
	cmcProcEnums cenum=null;
	
	Object CallingClass=null;
    ArrayList<cmcMonitorItem> mlist=null;
    
    private long lcounter = System.currentTimeMillis();
    
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
	cmcMonitorModelObject(cmcProcSettings iS , logLiason ilog)
	//---------------------------------------------------------------------------------
	{
    	mlist = new ArrayList<cmcMonitorItem>();
		xMSet = iS;
		logger=ilog;
		cenum = new cmcProcEnums(xMSet);
    }
	//---------------------------------------------------------------------------------
	private int getMonitorItemIdx(long luid)
	//---------------------------------------------------------------------------------
	{
		if( mlist == null ) return -1;
		for(int i=0;i<mlist.size();i++)
		{
			if( mlist.get(i).getUID() == luid ) return i;
		}
		return -1;
	}
	//---------------------------------------------------------------------------------
	public boolean upsertMonitorItemLine(cmcMonitorItem imon )
	//---------------------------------------------------------------------------------
	{
		if( mlist == null )  mlist = new ArrayList<cmcMonitorItem>();
        int idx = getMonitorItemIdx( imon.getUID() );
       //do_error("Got " + imon.getFileName() + " " + imon.getUID() + " " + idx);
        if( idx < 0 ) {
        	cmcMonitorItem x = new cmcMonitorItem( lcounter++ );
        	x.setUID( imon.getUID());
        	x.setFileName( imon.getFileName());
        	x.setStarttime( imon.getStarttime());
        	x.setEndtime( imon.getEndtime());
        	x.setComment( imon.getComment());
        	x.setProcessed( imon.getProcessed());
        	mlist.add(x);
        }
        else {
        	mlist.get(idx).setUID( imon.getUID());
        	mlist.get(idx).setFileName( imon.getFileName());
        	mlist.get(idx).setStarttime( imon.getStarttime());
        	mlist.get(idx).setEndtime( imon.getEndtime());
        	mlist.get(idx).setComment( imon.getComment());
        	mlist.get(idx).setProcessed( imon.getProcessed());
     //do_error("UPdate " + mlist.get(idx).getFileName() + " " + mlist.get(idx).getProcessed() + " " + mlist.get(idx).getComment());
        }
     	return true;
	}
	//---------------------------------------------------------------------------------
	public int getNumberOfCompletedItems()
	//---------------------------------------------------------------------------------
	{
		if( mlist == null ) return -1;
		int  n = 0;
		for(int i=0;i<mlist.size();i++)
		{
			if( (mlist.get(i).getStarttime() >= 0L) && (mlist.get(i).getEndtime() >= 0L) ) n++;
		}
		return n;
	}
	//---------------------------------------------------------------------------------
	public int getNumberOfUnCompletedItems()
	//---------------------------------------------------------------------------------
	{
		if( mlist == null ) return -1;
		return mlist.size() - getNumberOfCompletedItems();
	}
	
	    //---------------------------------------------------------------------------------
		public String getColumnName(int column )
		//---------------------------------------------------------------------------------
		{
	      cmcProcEnums.BULK_MONITOR x = cenum.getBulkMonitorAtIndex(column);
	      return ( x == null ) ? "?" : xMSet.xU.Capitalize(""+x);
		}
		
		//
		//---------------------------------------------------------------------------------
		public int getRowCount() 
		//---------------------------------------------------------------------------------
		{
			return mlist.size();
		}
		//
		//---------------------------------------------------------------------------------
		public int getColumnCount() 
		//---------------------------------------------------------------------------------
		{
		  return cmcProcEnums.BULK_MONITOR.values().length;
		}
		//
		//---------------------------------------------------------------------------------
		public Class getColumnClass(int column) 
		//---------------------------------------------------------------------------------
		{
		   cmcProcEnums.BULK_MONITOR x = cenum.getBulkMonitorAtIndex(column);
		   if( x == null ) return String.class;
		   switch ( x )
		   {
			   case COMPLETED : return Boolean.class;
			   default : return String.class;
		   }	
		   	
		}
	
		//---------------------------------------------------------------------------------
		public boolean isCellEditable(int row, int col)
		//---------------------------------------------------------------------------------
		{
		   return false;
		}
		

		//---------------------------------------------------------------------------------
		public Object getItem(int row, int column)
		//---------------------------------------------------------------------------------
		{
			 cmcProcEnums.BULK_MONITOR x = cenum.getBulkMonitorAtIndex(column);
		     if( x == null ) return null;
		     if( (row<0) || (row>= mlist.size()) ) return null;
		     //
		     
		     cmcMonitorItem p = mlist.get(row);
		     long secs = (p.getEndtime() - p.getStarttime()) / 1000L;
		     long tenthsecs = ((p.getEndtime() - p.getStarttime()) % 1000L) / 100L;
		     boolean ib = ((p.getStarttime() >= 0L) && (p.getEndtime() >= 0L)) ? true : false;
		     String sStart = ( p.getStarttime() < 0L ) ? "" : xMSet.xU.prntDateTime(p.getStarttime(), "HH:mm:ss");
		     String sElaps = (ib == false) ? "" : ""+ secs + "." + tenthsecs + " sec";
		     String FName = xMSet.xU.getFolderOrFileName(p.getFileName());
		     //
		     switch ( x )
			 {
			   case COMPLETED  : return ib;
			   case FILENAME   : return FName;
			   case START_TIME : return sStart;
			   case ELAPSED    : return sElaps;
			   case COMMENT    : return p.getComment();
			   default : return "?error";		   
			 }
			   
		}
		
		//---------------------------------------------------------------------------------
		public boolean propagateChanges()
		//---------------------------------------------------------------------------------
		{
			return true;
		}

	
}
