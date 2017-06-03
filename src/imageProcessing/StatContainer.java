package imageProcessing;

// statistiek resultaat container
public class StatContainer {
	public int meanCharWidth = -1;
	public int meanCharHeigth = -1;
	public int medCharWidth = -1;    // medianbreedte van een char
	public int medCharHeigth = -1;
	public int quartile1Heigth=-1;
	public int quartile3Heigth=-1;
	public int iqrHeigth=-1;  // Inter Quaretile Range hoogte
	public int quartile1Width=-1;
	public int quartile3Width=-1;
	public int iqrWidth=-1;  // Inter Quaretile Range hoogte
	public int glbMinw=-1;
	public int glbMinh=-1;
	public int glbMaxw=-1;
	public int glbMaxh=-1;
}
