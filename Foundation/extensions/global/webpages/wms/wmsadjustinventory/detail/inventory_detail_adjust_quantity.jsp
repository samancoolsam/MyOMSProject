<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.core.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript">
function showUnitPricePopup() {
	var quantity = document.all("xml:/AdjustLocationInventory/Source/Inventory/@Quantity");
	var key;
	var eleArray = document.all("myEntityKey");
    for ( var i =0; i < eleArray.length; i++ ) {
        if ( eleArray[i].name == "myEntityKey" ) {
			key = eleArray[i];
        }
    }
	yfcShowDetailPopupWithParams("YWMD015","", 700, 250,"AdjustmentQuantity=" + quantity.value, "wmsadjustinventory",key.value);
}
</script>

<input type="hidden" name="xml:/AdjustLocationInventory/Source/Inventory/@Quantity"/>
<input type="hidden" name="xml:/AdjustLocationInventory/@UserId" value="<%=resolveValue("xml:CurrentUser:/User/@Loginid")%>"/>
<%
	String defaultQty = "";
	int quantity = (int)getNumericValue("xml:/AdjustLocationInventory/Source/Inventory/@Quantity");
	if (quantity < 0) {
		quantity = -quantity;
	}
	defaultQty = getLocalizedStringFromInt(getLocale(), quantity);
%>

<table width="100%" class="view">
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Current_Quantity</yfc:i18n>
    </td>
    <td class="numericprotectedinput" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity" />
        <input type="hidden"  name="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity"        value="<%=resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity")%>" />
    </td>
	<td/>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Adjust_Quantity</yfc:i18n>
    </td>
    <td class="detaillabel" >
        <select ID="ParseSelect" <%=getComboOptions("xml:/AdjustLocationInventory/Source/Inventory/@QtyQryType","-")%> class="combobox" onchange="blankoutTextBoxes('xml:/Temp/@NewQuantity'); " >
            <option name="NotDefined" value="" >
                <yfc:i18n></yfc:i18n>
            </option>
            <option name="IncreaseBy" value="+" >
                <yfc:i18n>Increase_By</yfc:i18n>
            </option>
            <option name="DecreaseBy" value="-" >
                <yfc:i18n>Decrease_By</yfc:i18n>
            </option>
        </select>
    </td>
	<%if (!equals("Y",resolveValue("xml:ItemDetails:/Item/InventoryParameters/@IsSerialTracked")) ||  !equals(resolveValue("xml:Node:/ShipNodeList/ShipNode/@SerialTracking"),"Y")) {%>
    <td nowrap="true">
        <input type="text" class="numericunprotectedinput"  <%=getTextOptions("xml:/Temp/@NewQuantity","")%> />
    </td>
	<% } else { %>
    <td nowrap="true">
		<input type="text" class="protectedtext" <%=getTextOptions("xml:/Temp/@NewQuantity","","")%> /> 
    </td>
	<% } %>
<%	String itemid = resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID"); %>
		<% if (!isVoid(itemid)) { %>
				<yfc:makeXMLInput name="MyEntityKey">
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@UnitOfMeasure"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity"/>
				</yfc:makeXMLInput>
		<% } else { %>
		
		<% String preQuantity =resolveValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity"); 
			if(!isVoid(preQuantity)) { %>
				<yfc:makeXMLInput name="MyEntityKey">
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/AdjustLocationInventory/@EnterpriseCode"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity" value="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity"/>
				</yfc:makeXMLInput>
			<% } else { %>
				 <yfc:makeXMLInput name="MyEntityKey">
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/@EnterpriseCode" value="xml:/AdjustLocationInventory/@EnterpriseCode"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure" value="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@UnitOfMeasure"/>
						<yfc:makeXMLKey binding="xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity" value="0"/>
				</yfc:makeXMLInput>
		 		
		<% }} %>
    <input type="hidden" name="myEntityKey" value="<%=getParameter("MyEntityKey")%>"/> </tr>
</table>
