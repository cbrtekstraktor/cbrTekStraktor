package drawing;

import java.awt.Color;

public class cmcDrawingObject {
	 //
	 int OID=-1;
	 cmcGraphController.drawType tipe = cmcGraphController.drawType.UNKNOWN;
	 int x1=-1;
	 int y1=-1;
	 int x2=-1;
	 int y2=-1;
	 Color kleur=Color.BLACK;
	 String value;
	 
	 public cmcDrawingObject(int seq , cmcGraphController.drawType t , int a, int b , int c , int d , Color clr)
	 {
		 OID=seq;
		 tipe = t;
		 x1=a;
		 y1=b;
		 x2=c;
		 y2=d;
		 kleur=clr;
		 value = null;
	 }
	 
	 public cmcDrawingObject(int seq , cmcGraphController.drawType t , int a, int b , Color clr , String sIn)
	 {
		 OID=seq;
		 tipe = t;
		 x1=a;
		 y1=b;
		 x2=-1;
		 y2=-1;
		 kleur=clr;
		 value = sIn;
	 }
}
