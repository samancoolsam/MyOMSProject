<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%  
    String modifyView = request.getParameter("ModifyView");
    modifyView = modifyView == null ? "" : modifyView;
%>

<table width="100%" class="view">
<tr>
	<td class="detaillabel" ><yfc:i18n>Organization_Code</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@OrganizationCode" name="Cost"></yfc:getXMLValue></td>
	<td class="detaillabel" ><yfc:i18n>Ship_Node</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@ShipNode" name="Cost"></yfc:getXMLValue></td>
	<td class="detaillabel" ><yfc:i18n>Item_ID</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@ItemID" name="Cost"></yfc:getXMLValue></td>

</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Quantity</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@Quantity" name="Cost"></yfc:getXMLValue></td>
	<td class="detaillabel" ><yfc:i18n>Unit_Of_Measure</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@UnitOfMeasure" name="Cost"></yfc:getXMLValue></td>
	<td class="detaillabel" ><yfc:i18n>Product_Class</yfc:i18n></td>
	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Cost/@ProductClass" name="Cost"></yfc:getXMLValue></td>
</tr>
<tr>
	<td class="detaillabel" ><yfc:i18n>Average_Cost</yfc:i18n></td>
	<td class="protectedtext">
	        <% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp;
			<yfc:getXMLValue binding="xml:/Cost/@AverageCost" name="Cost"></yfc:getXMLValue>
            &nbsp;<%=curr0[1]%>
	</td>
	<td class="detaillabel" ><yfc:i18n>Unit_Cost</yfc:i18n></td>
	<td class="protectedtext">
            <% String[] curr1 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr1[0]%>&nbsp;
			<yfc:getXMLValue binding="xml:/Cost/@UnitCost" name="Cost"></yfc:getXMLValue>
            &nbsp;<%=curr1[1]%>
	</td>
	<td class="detaillabel" ><yfc:i18n>Inventory_Value</yfc:i18n></td>
	<td class="protectedtext">
            <% String[] curr2 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr2[0]%>&nbsp;
			<yfc:getXMLValue binding="xml:/Cost/@InventoryValue" name="Cost"></yfc:getXMLValue>
            &nbsp;<%=curr2[1]%>
	</td>
</tr>
</table>
