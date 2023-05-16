<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<%  String adjqty = request.getParameter("AdjustmentQuantity");
	double currentquantity = getNumericValue("xml:/AdjustLocationInventory/Source/Inventory/@CurrentQuantity");
	double adjustquantity = 0.0;
	Double tempDoubleclass1;
	Double tempDoubleclass2;
	if (!isVoid(adjqty)) { 
		try {
			tempDoubleclass1 = new Double(adjqty);
			adjustquantity = tempDoubleclass1.doubleValue();
		} catch (NumberFormatException ne)
		{
		} 
	}
%>
<table width="100%" class="view">

<tr>
    <td class="detaillabel" >
        <yfc:i18n>Item_ID</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/AdjustLocationInventory/Source/Inventory/InventoryItem/@ItemID" />
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Current_Quantity</yfc:i18n>
    </td>
    <td class="numericprotectedinput" >
		<%=getFormattedDouble(currentquantity)%>
	</td>

	<td class="detaillabel" >
        <yfc:i18n>Unit_Cost</yfc:i18n>
    </td>
    <%
		double unitcost = getNumericValue("xml:ItemDetails:/Item/PrimaryInformation/@UnitCost"); 
	%>
    <td class="numericprotectedinput" >
        <%=displayAmount(getFormattedDouble(unitcost) + "", (YFCElement) request.getAttribute("CurrencyList"), getValue("CurrentEnterprise", "xml:/Organization/@RulesetKey"), getValue("ItemDetails", "xml:/Item/PrimaryInformation/@CostCurrency"))%>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Item_Description</yfc:i18n>
	</td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:ItemDetails:/Item/PrimaryInformation/@Description" />
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Adjustment_Quantity</yfc:i18n>
    </td>
    <td class="numericprotectedinput" >
		<%=getFormattedDouble(adjustquantity)%>
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Adjustment_Cost</yfc:i18n>
    </td>
	<%	
		double totalcost = adjustquantity * unitcost;
	%>
    <td class="numericprotectedinput" >
        <%=displayAmount(getFormattedDouble(totalcost) + "", (YFCElement) request.getAttribute("CurrencyList"), getValue("CurrentEnterprise", "xml:/Organization/@RulesetKey"), getValue("ItemDetails", "xml:/Item/PrimaryInformation/@CostCurrency"))%>
    </td>
</tr>
<tr>
	<td/>
	<td/>
	<td class="detaillabel" >
        <yfc:i18n>Quantity_After_Adjustment</yfc:i18n>
    </td>
	<%	
		double quantityafteradjustment = currentquantity + adjustquantity;
	%>
    <td class="numericprotectedinput" >
		<%=getFormattedDouble(quantityafteradjustment)%>
    </td>
	<td/>
</tr>
</table>