<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<SCRIPT LANGUAGE="JavaScript">
function setHoldOKClickedAttribute() {
	if(validateControlValues()) {
    	var retVal = new Object();
		retVal.retValue= document.all("xml:/Order/@HoldType").value;		
		if(retVal.retValue.length>0){
			window.dialogArguments["OMReturnValue"] = retVal;
	        window.dialogArguments["OKClicked"] = "YES";        
			window.close();
		}else{
			alert("Please select Hold Type.");
			return false;
		}
    }
}

</SCRIPT>

<table width="100%" class="view">
    <tr>
        <td> 
            <yfc:i18n>Resolve Hold Type</yfc:i18n>
        </td>
        <td>
           <!-- <select name="xml:/ModificationReason/@ReasonCode" class="combobox">
                <yfc:loopOptions binding="xml:ReasonCodeList:/CommonCodeList/@CommonCode" 
                    name="CodeShortDescription" value="CodeValue" isLocalized="Y"/>
            </select> -->
			<select name="xml:/Order/@HoldType" class="combobox">
                <yfc:loopOptions binding="xml:HoldTypeList:/HoldTypeList/@HoldType" 
                    name="HoldTypeDescription" value="HoldType" isLocalized="Y"/>
            </select>
        </td>
    </tr>
    <tr>
        <td></td>
        <td align="right">
            <input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setHoldOKClickedAttribute();return false;"/>
            <input type="button" class="button" value='<%=getI18N("Cancel")%>' onclick="window.close();"/>
        <td>
    <tr>
</table>