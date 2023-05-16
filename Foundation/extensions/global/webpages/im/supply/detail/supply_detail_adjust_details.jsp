<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfc.core.*" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/im.js"></script>

<script language="javascript">
	window.attachEvent("onload", checkForError);

	function checkForError()	{
					
		var oError = document.all('YFCDetailError');
		var oHiddenError = document.all('hiddenError');
		if(oError.HasError == "Y") {
			oHiddenError.Value = "Y"
		}
	}

	function getInventorySupply()	{
	
		var containerForm= document.all("containerform");
		var hiddenCallApi = document.createElement("<INPUT type='hidden' name='callAPI' value='Y'/>");
		containerForm.insertBefore(hiddenCallApi);
		yfcChangeDetailView(getCurrentViewId());
			
	}

	function setCallAPIFlag()	{
		
		var containerForm= document.all("containerform");
		var hiddenCallApi = document.createElement("<INPUT type='hidden' name='callAPI' value='Y'/>");
		containerForm.insertBefore(hiddenCallApi);
		return true;
	}
	
	function changeInput()	{
		
		var response= document.all(
					"xml:/Item/@ItemID").value;
					alert(response);
	}
</script>

<% 
	if(equals(getParameter("callAPI"),"Y") && equals(getParameter("hiddenError"),"N")) { %>
		<yfc:callAPI apiID='AP1'/>
<% }
    String sDefaultedValue="TRACK"; 
    String sAvailabilityType=resolveValue("xml:CalculatedInventorySupply:/Item/Supplies/InventorySupply/@AvailabilityType");
    if (YFCObject.equals(sAvailabilityType, "INFINITE")) 
        sDefaultedValue= "INFINITE";
%> 

<table class="view" width="100%" >
<tr>
	
    <td class="detaillabel" >
        <input type="hidden" class="unprotectedinput" <%=getTextOptions("xml:/Items/Item/@Quantity","xml:/Item/Supplies/InventorySupply/@QtyToBeAdjusted")%> <%if(YFCObject.equals(sDefaultedValue, "INFINITE")){%> value=""<%}%>/>
		<input type="hidden" class="unprotectedinput" <%=getTextOptions("xml:/InventorySupply/@CallingOrganizationCode","xml:CurrentOrganization:/Organization/@OrganizationCode")%> />
		<input type="hidden" class="unprotectedinput" <%=getTextOptions("xml:/Items/Item/@User","xml:CurrentUser:/User/@Loginid")%> />
        <yfc:i18n>Availability</yfc:i18n>
    </td>
    <td nowrap="true" colspan="2">
        <input type="Radio" class="radiobutton" onclick="enableFields('xml:/Item/Supplies/InventorySupply/@QtyQryType','xml:/Temp/@NewQuantity');"  
            <%=getRadioOptions("xml:/Items/Item/@Availability", sDefaultedValue ,"TRACK" )%> />
        <yfc:i18n>Track</yfc:i18n>
	</td>
	
</tr>
<tr>
    <td class="detaillabel">
    	<input type="button" class="button" value="<%=getI18N("Quantity")%>"  onclick="getInventorySupply()"/>
    </td>
    <td class="protectedtext">
        <%	
			if(!YFCObject.equals(sDefaultedValue, "INFINITE")){
				double quantity = 0; 
				quantity = getNumericValue("xml:CalculatedInventorySupply:/Item/Supplies/InventorySupply/@Quantity");
		%>
			<%=getLocalizedStringFromDouble(getLocale(), quantity)%>
		<%}%> 
    </td>
    <td>    	
		<input type="hidden" name="hiddenError" value="N" />
     </td>    
</tr>
<tr>
    <td class="detaillabel" >
        <select <%=getComboOptions("xml:/Item/Supplies/InventorySupply/@QtyQryType","+")%> class="combobox" onchange="blankoutTextBoxes('xml:/Temp/@NewQuantity','xml:/Items/Item/@Quantity');" 
            <%if(YFCObject.equals(sDefaultedValue, "INFINITE")){%> disabled="true" <%}%> >
            <option name="IncreaseBy" value="+" selected="selected">
                <yfc:i18n>Increase_By</yfc:i18n>
            </option>
            <option name="DecreaseBy" value="-" >
                <yfc:i18n>Decrease_By</yfc:i18n>
            </option>
        </select>
    </td>
    <td nowrap="true">
        <input type="text" class="numericunprotectedinput" 
        	onblur="updateAPIQty(this, 'xml:/Items/Item/@Quantity', 'xml:/Item/Supplies/InventorySupply/@QtyQryType');" 
        	<%=getTextOptions("xml:/Temp/@NewQuantity", "xml:/Item/Supplies/InventorySupply/@NewQuantity")%> 
            <%if(YFCObject.equals(sDefaultedValue, "INFINITE")){%> disabled="true" value=""<%}%> />
    </td>
    <td>&nbsp;</td>
</tr>
</table>
