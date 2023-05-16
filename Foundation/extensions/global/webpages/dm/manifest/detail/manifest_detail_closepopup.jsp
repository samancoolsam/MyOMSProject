<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<SCRIPT LANGUAGE="JavaScript">
	function setOKClicked() {
		var myObject = new Object();
		myObject = dialogArguments;		
		var parentWindow = myObject.currentWindow;		
		var parenttrailerNo  = 	myObject.trailerNo;
		if(validateControlValues()) {
			var retVal = new Object();		
			retVal["xml:/Manifest/@TrailerNo"] = document.all["xml:/Manifest/@TrailerNo"].value;
			var trailerNoInput = document.all("xml:/Manifest/@TrailerNo");
			parenttrailerNo.value = trailerNoInput.value;
			window.dialogArguments["EMReturnValue"] = retVal;
			window.dialogArguments["OKClicked"] = "YES";
			window.close();
		}
	}
</SCRIPT>
<table width="100%" class="view">    
	<tr>
        <td class="detaillabel">
            <yfc:i18n>Trailer_#</yfc:i18n> 
        </td>
        <td class="protectedtext">
	        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Manifest/@TrailerNo")	%> />
		</td>		
    </tr>
    <tr>        
        <td colspan="2" align="center">
		   <input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setOKClicked();return false;"/>           
        <td>
    <tr>
</table>