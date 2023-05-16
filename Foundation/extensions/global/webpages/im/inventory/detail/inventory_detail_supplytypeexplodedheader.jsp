<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<table class="view" width="100%">
<tr>
    <td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/@ItemID" name="Item" /></td>
    <td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/@ProductClass" name="Item" /></td>
    <td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/@UnitOfMeasure" name="Item" /></td>
</tr>
<tr>
    <td class="detaillabel" ><yfc:i18n>Description</yfc:i18n></td>
    <td class="protectedtext"><yfc:getXMLValue binding="xml:/Item/PrimaryInformation/@Description" name="Item" /></td>
    <td class="detaillabel" ><yfc:i18n>Supply_Type</yfc:i18n></td>
    <td class="protectedtext"><%=getComboText("xml:SupplyTypeList:/InventorySupplyTypeList/@InventorySupplyType","Description","SupplyType",resolveValue("xml:/Item/Supplies/InventorySupply/@SupplyType"),true)%></td>
</tr>
</table>
