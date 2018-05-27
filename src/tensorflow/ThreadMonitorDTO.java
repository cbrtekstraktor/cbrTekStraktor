package tensorflow;

import cbrTekStraktorModel.cmcProcEnums;

public class ThreadMonitorDTO {

	   public enum MONITORSTATUS  { QUEUED , STARTED , BUSY , COMPLETED , FAILED }
       public enum INFOTRANSFERSTATUS { COLLECTING , READY , TRANSFERRED }
       
	   private MONITORSTATUS status = MONITORSTATUS.QUEUED;
       private INFOTRANSFERSTATUS txstat = INFOTRANSFERSTATUS.COLLECTING;
       private int fieldIndex=-1;
	   private String ImageFileName=null;
	   private String CommandFileName=null;
	   private String ErrorMsg = null;
	   private cmcProcEnums.PageObjectType TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.UNKNOWN;
	   private double TensorValidPercentage=0;
	   private double TensorInvalidPercentage=0;	
	   private boolean exitStatus=false;
	   private long starttime=-1L;
	   private long endtime=-1L;
	  
	   public ThreadMonitorDTO()
	   {
		   status = MONITORSTATUS.QUEUED;
		   txstat = INFOTRANSFERSTATUS.COLLECTING;
		   fieldIndex=-1;
		   ImageFileName=null;
		   CommandFileName=null;
		   ErrorMsg = null;
		   TipeDeterminedViaTensor =  cmcProcEnums.PageObjectType.UNKNOWN;
		   TensorValidPercentage=0;
		   TensorInvalidPercentage=0;	
		   exitStatus=false;
	   }

	   public void shallowCopy (ThreadMonitorDTO x)
	   {
		   status          = x.status;
		   txstat          = x.txstat;
		   fieldIndex      = x.fieldIndex;
		   ImageFileName   = x.ImageFileName;
		   CommandFileName = x.CommandFileName;
		   ErrorMsg        = x.ErrorMsg;
		   TipeDeterminedViaTensor =  x.TipeDeterminedViaTensor;
		   TensorValidPercentage   = x.TensorValidPercentage;
		   TensorInvalidPercentage = x.TensorInvalidPercentage;
		   exitStatus              = x.exitStatus;
	   }
	   
	   
	   
	public MONITORSTATUS getStatus() {
		return status;
	}

	public void setStatus(MONITORSTATUS status) {
		this.status = status;
	}

	public int getFieldIndex() {
		return fieldIndex;
	}

	public void setFieldIndex(int fieldIndex) {
		this.fieldIndex = fieldIndex;
	}

	public String getImageFileName() {
		return ImageFileName;
	}

	public void setImageFileName(String imageFileName) {
		ImageFileName = imageFileName;
	}

	public String getCommandFileName() {
		return CommandFileName;
	}

	public void setCommandFileName(String commandFileName) {
		CommandFileName = commandFileName;
	}

	public String getErrorMsg() {
		return ErrorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		ErrorMsg = errorMsg;
	}

	public cmcProcEnums.PageObjectType getTipeDeterminedViaTensor() {
		return TipeDeterminedViaTensor;
	}

	public void setTipeDeterminedViaTensor(cmcProcEnums.PageObjectType tipeDeterminedViaTensor) {
		TipeDeterminedViaTensor = tipeDeterminedViaTensor;
	}

	public double getTensorValidPercentage() {
		return TensorValidPercentage;
	}

	public void setTensorValidPercentage(double tensorValidPercentage) {
		TensorValidPercentage = tensorValidPercentage;
	}

	public double getTensorInvalidPercentage() {
		return TensorInvalidPercentage;
	}

	public void setTensorInvalidPercentage(double tensorInvalidPercentage) {
		TensorInvalidPercentage = tensorInvalidPercentage;
	}

	public boolean getExitStatus() {
		return exitStatus;
	}

	public void setExitStatus(boolean exitStatus) {
		this.exitStatus = exitStatus;
	}

	public long getStarttime() {
		return starttime;
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public long getEndtime() {
		return endtime;
	}

	public void setEndtime(long endtime) {
		this.endtime = endtime;
	}

	public INFOTRANSFERSTATUS getTxstat() {
		return txstat;
	}

	public void setTxstat(INFOTRANSFERSTATUS txstat) {
		this.txstat = txstat;
	}
	   
	   
}
