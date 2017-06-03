package drawing;

public class cmcKloonGraphPageObject {

	//---------------------------------------------------------------------------------
	public cmcKloonGraphPageObject()
	//---------------------------------------------------------------------------------
	{
		
	}
	
	//---------------------------------------------------------------------------------
	public boolean copy_GraphPageObject(cmcGraphPageObject[] src ,cmcGraphPageObject[] tgt)
	//---------------------------------------------------------------------------------
	{
				if( src == null ) return false;
				if( tgt == null ) return false;
				if( src.length > tgt.length ) return false;
				for(int i=0;i<src.length;i++)
				{
					tgt[i].tipe          = src[i].tipe;
					tgt[i].UID           = src[i].UID;
					tgt[i].removed       = src[i].removed;
					tgt[i].isValid       = src[i].isValid;
					tgt[i].MinX          = src[i].MinX;
					tgt[i].MinY          = src[i].MinY;
					tgt[i].MaxX          = src[i].MaxX;
					tgt[i].MaxY          = src[i].MaxY;
					tgt[i].ClusterIdx    = src[i].ClusterIdx;
					tgt[i].BundelIdx     = src[i].BundelIdx;
					tgt[i].visi          = src[i].visi;
					tgt[i].isSelected    = src[i].isSelected;
					tgt[i].DrawObjectOID = src[i].DrawObjectOID;
					tgt[i].hasChanged    = src[i].hasChanged;
					tgt[i].changetipe    = src[i].changetipe;
				}
				return true;
		}
}
