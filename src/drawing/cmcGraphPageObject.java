package drawing;

import cbrTekStraktorModel.cmcProcEnums;

public class cmcGraphPageObject {
	
	    public cmcProcEnums.PageObjectType tipe = cmcProcEnums.PageObjectType.UNKNOWN;
	    public long UID=0L;
	    public boolean removed=false;
		public boolean isValid=false;
		public int MinX=-1;
		public int MinY=-1;
		public int MaxX=-1;
		public int MaxY=-1;
		public int ClusterIdx=-1;
		public int BundelIdx=-1;
	    public cmcProcEnums.VisibilityType visi = cmcProcEnums.VisibilityType.INVISIBLE;	
	    public boolean isSelected=false;
	    public int DrawObjectOID=-1;
	    public boolean hasChanged=false;
	    public cmcProcEnums.EditChangeType changetipe = cmcProcEnums.EditChangeType.NONE;
}
