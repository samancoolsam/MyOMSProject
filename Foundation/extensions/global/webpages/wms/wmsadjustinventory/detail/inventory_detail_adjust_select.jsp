<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ include file="/yfsjspcommon/editable_util_header.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>

<script language="javascript">

function validateInput(sAPIQtyText, sQueryTypeCombo) {
	if (formserialbinding() && updateAPIQty(sAPIQtyText, sQueryTypeCombo))
		return true;
	return false;
}

function formserialbinding() {
	var sReasonCode = document.all["xml:/AdjustLocationInventory/Audit/@ReasonCode"];
	if(sReasonCode.value == "") {
		alert(YFCMSG040);
		return false;
	}
	else {
		yfcMultiSelectToSingleAPIOnAction('chkEntityKey', 'yfcMultiSelectCounter', 'yfcMultiSelectValue1', 'SerialNo', 'xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail', "");
		yfcMultiSelectToSingleAPIOnAction('chkEntityKey', 'yfcMultiSelectCounter', 'yfcMultiSelectValue2', 'Quantity', 'xml:/AdjustLocationInventory/Source/Inventory/SerialList/SerialDetail', ""); 
		return true;
	}
}

    function updateAPIQty(sAPIQtyText, sQueryTypeCombo)
    {
        var sOperation=document.all(sQueryTypeCombo);
        var oAPIQtyText = document.all(sAPIQtyText);
        var tempQty = document.all("xml:/Temp/@NewQuantity");
		
		if (oAPIQtyText != null && tempQty != null && sOperation != null) { 
			if (sOperation.value == "-")
				oAPIQtyText.value= "-" + tempQty.value;
			else if (sOperation.value == "+")   
				oAPIQtyText.value= tempQty.value;
			else { 
				alert(YFCMSG133);
				return false;
			} 
		}
		return true;
    }
	

function getaddtablequantity() {
	var quantity = 0;
    var tblObj = window.document.getElementById("specialChange");    
    var childNodes = tblObj.getElementsByTagName("TR");
	for ( var i = 0 ; i < childNodes.length; i++ ) {
		var child = childNodes.item(i);
		//alert("nodeName" + child.nodeName);
			if (!isBlankElement(child))
				quantity = quantity + 1;
	}
	//alert("quantity ->" + quantity);
	return quantity;
}

function getremovetablequantity() {
    var tblObj = window.document.getElementById("RemoveSerialNo");    
    var childNodes = tblObj.getElementsByTagName("INPUT");
	var quantity = 0;
	for ( var l = 0 ; l < childNodes.length; l++ ) {
		var child = childNodes.item(l);
		if (child.checked) quantity= quantity - 1;
	}
	//alert("quantity ->" + quantity);
	return quantity;
}


function showUnitPricePopup1() {
	var addquantity = getaddtablequantity();
	var removequantity = getremovetablequantity();
	var adjqty = addquantity + removequantity;
	yfcShowDetailPopupWithParams("YWMD015","", 700, 250,"AdjustmentQuantity=" + adjqty ,"wmsadjustinventory",document.all("myEntityKey").value);
}

</script>

<table width="100%" class="view">


<tr>
    <td class="detaillabel" ><yfc:i18n>Node</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/@Node" />
        <input type="hidden"  
          <%=getTextOptions("xml:/AdjustLocationInventory/@Node","xml:/NodeInventory/@Node")%> />
    </td>
<%	String itemid = resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID"); %>
	<td class="detaillabel" >
        <yfc:i18n>Enterprise</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/@EnterpriseCode","xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode")%> />
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/@EnterpriseCode" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/@EnterpriseCode","xml:/AdjustLocationInventory/@EnterpriseCode")%> />
		<% } %>
    </td>
	<td>
	</td>
	<td>
	</td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Location</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/@LocationId" />
			<input type="hidden"            <%=getTextOptions("xml:/AdjustLocationInventory/Source/@LocationId","xml:/NodeInventory/LocationInventoryList/LocationInventory/@LocationId")%> />
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/@LocationId" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@LocationId","xml:/AdjustLocationInventory/Source/@LocationId")%> />
		<% } %>
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Pallet_ID</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
        <%  
		   if(!(isVoid(resolveValue("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID")))) { String sACaseId = resolveValue("xml:/AdjustLocationInventory/Source/@CaseId");
			if (isVoid(sACaseId)) { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/@PalletId" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@PalletId","xml:/AdjustLocationInventory/Source/@PalletId")%> />
		<% }} else if (!(isVoid(itemid))) { %>
			<% 
			if (isVoid(getParameter("caseId"))) { %>
			<%=getParameter("palletId") %>
			<input type="hidden"           <%=getTextOptions("xml:/AdjustLocationInventory/Source/@PalletId",getParameter("palletId"))%> />
		<% }}  %>
			
		
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Case_ID</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
        <% 
           if(!(isVoid(resolveValue("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID")))) { %>
            <yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/@CaseId" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@CaseId","xml:/AdjustLocationInventory/Source/@CaseId")%> />
		<% } else if (!(isVoid(itemid))) { %>
			<%=getParameter("caseId") %>
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/@CaseId",getParameter("caseId"))%> />
		<% } %>
    </td>

</tr>
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID" />
			<input type="hidden"   <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID","xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID")%> />
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID","xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID")%> />
		<% } %>
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ProductClass" />
			<input type="hidden"          <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass","xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ProductClass")%>/>
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass","xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ProductClass")%> />
		<% } %>
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@UnitOfMeasure" />
			<input type="hidden"         <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure","xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@UnitOfMeasure")%>/>
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure","xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure")%> />
		<% } %>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
		<yfc:i18n>Item_Description</yfc:i18n>
	</td>
    <td class="protectedtext" nowrap="true" >
    <% if (!(isVoid(itemid))) { %>
		<yfc:getXMLValue  binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/Item/PrimaryInformation/@Description"/>
    <% } else { %>
    <%=resolveValue("xml:ItemDetails:/Item/PrimaryInformation/@Description")%>
     <% } %>
	</td>
	<td class="detaillabel" >
        <yfc:i18n>Inventory_Status</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<% if (!(isVoid(itemid))) { %>
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/SummaryAttributes/@InventoryStatus" />
	        <input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus","xml:/NodeInventory/LocationInventoryList/LocationInventory/SummaryAttributes/@InventoryStatus")%>/>
		<% } else { %>
			<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus","xml:/AdjustLocationInventory/Source/Inventory/@InventoryStatus")%> />
		<% } %>
    </td>
</tr>
	<% if (!(isVoid(itemid))) { %>
			<yfc:makeXMLInput name="MyEntityKey">
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@UnitOfMeasure"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity"/>
			</yfc:makeXMLInput>
	<% } else { %>
			<yfc:makeXMLInput name="MyEntityKey">
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/AdjustLocationInventory/@EnterpriseCode"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure"/>
					<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity"/>
			</yfc:makeXMLInput>
	<% } %>
    <input type="hidden" name="myEntityKey" value="<%=getParameter("MyEntityKey")%>"/> </tr>
    <input type="hidden"  name="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity"        value="<%=resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity")%>" />
	<input type="hidden"  <%=getTextOptions("xml:ItemDetails:/Item/PrimaryInformation/@NumSecondarySerials")%> />
</table>
