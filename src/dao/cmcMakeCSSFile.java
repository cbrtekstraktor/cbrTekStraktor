package dao;

import cbrTekStraktorModel.cmcProcSettings;
import generalpurpose.gpPrintStream;

public class cmcMakeCSSFile {
	
	cmcProcSettings xMSet = null;
	gpPrintStream cout = null;
	
	public cmcMakeCSSFile(cmcProcSettings iM , String FNaam)
	{
		xMSet = iM;
		maakCSSFile(FNaam);
	}

	private void p( String sLijn )
	{
		cout.println(sLijn);
	}
	
	private void maakCSSFile(String FNaam)
	{
		// just a check to see if the folder can be written into
		String folderName = xMSet.xU.getParentFolderName(FNaam);
		if( xMSet.xU.IsDir(folderName) == false ) {
			System.err.println("CSS file [" + FNaam + "] ->  folder [" + folderName + "] is not accessible. Skipping");
			return;
		}
		cout = new gpPrintStream( FNaam , "ASCII");
		//
		p("<!-- Application      : " + xMSet.getApplicDesc() + " Cascaded Style Sheet -->");
		p("<!-- CSS File Created : " + (xMSet.xU.prntStandardDateTime(System.currentTimeMillis())).toUpperCase() + " -->" );
		p("<style type=\"text/css\">");
		p("<!-- for some reason the CSS needs to start by a dummy line -->");
		p(".dummy {");
		p("font-size: 01px;");
		p("font-weight: bold;");
		p("}");
		p(".tekBold {");
		p("font-weight: bold;");
		p("}");
		p(".title {");
		p("font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;");
		p("font-size: 20px;");
		p("}");
		p(".vet {");
		p("font-weight: bold;");
		p("}");
		p("table {");
		p("width: 100%;");
		p("border: 1px solid #B0B0B0;");
		p("font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;");
		p("}");
		p("tbody {");
		p("margin: 0;");
		p("padding: 0;");
		p("border: 0;");
		p("outline: 0;");
		p("font-size: 100%;");
		p("vertical-align: baseline;");
		p("background: transparent;");
		p("}");
		p("thead {");
		p("text-align: left;");
		p("}");
	    p("thead th {");
		p("background: -moz-linear-gradient(top, #F0F0F0 0, #DBDBDB 100%);");
		p("background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #F0F0F0), color-stop(100%, #DBDBDB));");
		p("filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#F0F0F0', endColorstr='#DBDBDB', GradientType=0);");
		p("border: 1px solid #B0B0B0;");
		p("color: #444;");
		p("font-size: 14px;");
		p("font-weight: bold;");
		p("padding: 3px 10px;");
		p("}");
		p("thead {");
		p("text-align: left;");
		p("}");
		p("thead th {");
		p("background: -moz-linear-gradient(top, #F0F0F0 0, #DBDBDB 100%);");
		p("background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #F0F0F0), color-stop(100%, #DBDBDB));");
		p("filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#F0F0F0', endColorstr='#DBDBDB', GradientType=0);");
		p("border: 1px solid #B0B0B0;");
		p("color: #444;");
		p("font-size: 14px;");
		p("font-weight: bold;");
		p("padding: 3px 10px;");
		p("}");
		p("td {");
		p("padding: 3px 10px;");
		p("font-size: 12px;");
		p("text-align: left;");
		p("vertical-align: middle;");
		p("}");
		p("tr:nth-child(even) {");
		p("background: #F2F2F2;");
		p("}");
		p("</style>");
		//
		cout.close();
	}
}



/*
 * 
 * 


<!-- Cascaded Style Sheet section -->
<style type="text/css">

<!-- geen idee maar er moet een dummy entry bovenaan-->
.dummy {
font-size: 01px;
font-weight: bold;
}

.tekBold {
font-weight: bold;
}


.title {
font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
font-size: 20px;
}
.vet {
font-weight: bold;
}

table {
width: 100%
border: 1px solid #B0B0B0;
font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;

}

tbody {
margin: 0;
padding: 0;
border: 0;
outline: 0;
font-size: 100%;
vertical-align: baseline;
background: transparent;
}

thead {
text-align: left;
}

thead th {
background: -moz-linear-gradient(top, #F0F0F0 0, #DBDBDB 100%);
background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #F0F0F0), color-stop(100%, #DBDBDB));
filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#F0F0F0', endColorstr='#DBDBDB', GradientType=0);
border: 1px solid #B0B0B0;
color: #444;
font-size: 14px;
font-weight: bold;
padding: 3px 10px;
}

td {
padding: 3px 10px;
font-size: 12px;
text-align: left;
vertical-align: middle;

}
tr:nth-child(even) {
background: #F2F2F2;
}

</style>





 */