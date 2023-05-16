<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table width="100%" class="view">
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Item" binding="xml:/Item/@ItemID"/>
		<input type="hidden" <%=getTextOptions("xml:/InventoryItem/@ItemID")%> />
		<input type="hidden" <%=getTextOptions("xml:/InventoryItem/@ProductClass")%> />
		<input type="hidden" <%=getTextOptions("xml:/InventoryItem/@UnitOfMeasure")%> />
		<input type="hidden" <%=getTextOptions("xml:/InventoryItem/@OrganizationCode")%> />
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Product_Class</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Item" binding="xml:/Item/@ProductClass"/>
    </td>
    <td class="detaillabel" >
        <yfc:i18n>Unit_Of_Measure</yfc:i18n>
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="Item" binding="xml:/Item/@UnitOfMeasure"/>
    </td>
</tr>
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Description</yfc:i18n> 
    </td>
    <td class="protectedtext">
        <yfc:getXMLValue name="ItemDetail" binding="xml:/Item/PrimaryInformation/@Description"/>
    </td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
</tr>
</table>
