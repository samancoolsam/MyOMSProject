<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<table width="100%" class="view">
<tr>
    <td class="detaillabel" >
        <yfc:i18n>ItemID</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ItemID" />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@ProductClass" />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@UnitOfMeasure" />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
		<yfc:i18n>Location</yfc:i18n>
	</td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/@LocationId" />
    </td>
    <td class="detaillabel" >
		<yfc:i18n>Enterprise</yfc:i18n>
	</td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/InventoryItem/@OrganizationCode" />
    </td>
    <td class="detaillabel" >
		<yfc:i18n>Quantity</yfc:i18n>
	</td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/@Quantity" />
    </td>
</tr>
</table>