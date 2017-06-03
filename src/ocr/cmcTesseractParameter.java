package ocr;

public class cmcTesseractParameter {

	private boolean withold=false;
	private String parameter=null;
	private String value=null;
	private String description=null;
	
	public cmcTesseractParameter()
	{
		withold=false;
		parameter=null;
		value=null;
		description=null;
	}
	public cmcTesseractParameter(String iparam, String ivalue, String idesc)
	{
		withold=false;
		parameter=iparam;
		value=ivalue;
		description=idesc;
	}
	
	public boolean getWithold()
	{
		return withold;
	}
	public String getParameter()
	{
		return parameter;
	}
	public String getDescription()
	{
		return description;
	}
	public String getValue()
	{
		return value;
	}
	//
	public void setWithold(boolean ib)
	{
		withold=ib;
	}
	public void setParameter(String s)
	{
		parameter = s;
	}
	public void setValue(String s)
	{
		value = s;
	}
	public void setDescription(String s)
	{
		description = s;
	}
}
