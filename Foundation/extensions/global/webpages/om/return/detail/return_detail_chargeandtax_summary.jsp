<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="java.lang.Double" %>

<%
String displayInEnterpriseCurrency = request.getParameter("DisplayInEnterpriseCurrency");
String total = "";

if (equals(displayInEnterpriseCurrency,"Y")) {
	total = "TotalsInEnterpriseCurrency";
} else {
	total = "Totals";	
}

String chargeType=request.getParameter("chargeType");
if (isVoid(chargeType)) {
    chargeType="Remaining";
}

String ChargeViewID="";
String TaxViewID="";
if(equals(chargeType,"Overall")) {
    ChargeViewID="L01";
    TaxViewID="L04";
}
else if(equals(chargeType,"Remaining")) {
    ChargeViewID="L02";
    TaxViewID="L05";
}
else if(equals(chargeType,"Invoiced")) {
    ChargeViewID="L03";
    TaxViewID="L06";
}

double totalLineExtendedPrice = 0;
double totalLineDiscount = 0;
double totalLineCharges = 0;
double totalLineTax = 0;
double totalLineTotal = 0;
double totalOtherCharges = 0;
%>
<yfc:loopXML name="Order" binding="xml:/Order/OrderLines/@OrderLine" id="orderline"> 
<%
request.setAttribute("orderline", (YFCElement)pageContext.getAttribute("orderline"));
YFCElement oOrderLine = (YFCElement)request.getAttribute("orderline");
String sItemGroupCode = oOrderLine.getAttribute("ItemGroupCode");
if(oOrderLine != null)
{   
    YFCElement oTotals = (YFCElement)oOrderLine.getElementsByTagName("Line" + chargeType + total).item(0);
    if(oTotals != null)
    {
		if(equals(sItemGroupCode, "DS") )
		{
			totalLineExtendedPrice -= oTotals.getDoubleAttribute("ExtendedPrice");
			totalLineExtendedPrice -= oTotals.getDoubleAttribute("OptionPrice");
	        totalLineTotal -= oTotals.getDoubleAttribute("LineTotal");
			totalLineDiscount += oTotals.getDoubleAttribute("Discount");
			totalLineTax -= oTotals.getDoubleAttribute("Tax");
			totalLineCharges -= oTotals.getDoubleAttribute("Charges");
			totalOtherCharges += oTotals.getDoubleAttribute("Discount");
			totalOtherCharges -= oTotals.getDoubleAttribute("Charges");

		}
		else
		{
			totalLineExtendedPrice += oTotals.getDoubleAttribute("ExtendedPrice");
			totalLineExtendedPrice += oTotals.getDoubleAttribute("OptionPrice");
	        totalLineTotal += oTotals.getDoubleAttribute("LineTotal");
			totalLineDiscount -= oTotals.getDoubleAttribute("Discount");
			totalLineTax += oTotals.getDoubleAttribute("Tax");
			totalLineCharges += oTotals.getDoubleAttribute("Charges");
			totalOtherCharges -= oTotals.getDoubleAttribute("Discount");
			totalOtherCharges += oTotals.getDoubleAttribute("Charges");

		}
    }
}
%>
</yfc:loopXML>
<%
	YFCLocale locale = getLocale();
    String sTotalLineExtendedPrice = getLocalizedStringFromComputedDouble(locale,totalLineExtendedPrice);
    String sTotalLineDiscount = getLocalizedStringFromComputedDouble(locale,totalLineDiscount);
    String sTotalLineTax = getLocalizedStringFromComputedDouble(locale,totalLineTax);
    String sTotalLineCharges = getLocalizedStringFromComputedDouble(locale,totalLineCharges);
    String sTotalLineTotal = getLocalizedStringFromComputedDouble(locale,totalLineTotal);
	String stotalOtherCharges = getLocalizedStringFromComputedDouble(locale,totalOtherCharges);
%>
<table class="table" cellpadding="0" cellspacing="0" width="100%"  suppressFooter="true">
<thead>
   <% if (isVoid(request.getParameter("showOrderedOnly"))) { %>
		<tr>
	        <td sortable="no">
	            <select name="chargeType" class="combobox" onchange="yfcChangeDetailView(getCurrentViewId());">
	                <option value="Overall" <%if (equals(chargeType,"Overall")) {%> selected <%}%>><yfc:i18n>Overall</yfc:i18n></option>
	                <option value="Remaining" <%if (equals(chargeType,"Remaining")) {%> selected <%}%>><yfc:i18n>Open</yfc:i18n></option>
	                <% if (isVoid(displayInEnterpriseCurrency)) { %>
	                	<option value="Invoiced" <%if (equals(chargeType,"Invoiced")) {%> selected <%}%>><yfc:i18n>Invoiced</yfc:i18n></option>
	                <%}%>
	            </select>
	        </td>
	        <td colspan="4" sortable="no">
				<table>
					<tr>
				        <% if (equals(displayInEnterpriseCurrency,"Y")) { %> 
				        	<td>&nbsp;</td>
				        	<td class="detaillabel" ><yfc:i18n>Currency_Conversion_Date</yfc:i18n></td>
				        	<td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/PriceInfo/@ReportingConversionDate"/></td>
				        <%}%>
				    </tr>
				</table>
			</td>
		</tr>
   <%}%>
   	<% if (equals(displayInEnterpriseCurrency,"Y") && equals(request.getParameter("showOrderedOnly"),"Y")) { %> 
   		<tr>
   			<td colspan="5">
	   			<table>
	   				<tr>   				
				   		<td class="detaillabel" ><yfc:i18n>Currency_Conversion_Date</yfc:i18n></td>
				        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/PriceInfo/@ReportingConversionDate"/></td>
			        </tr>
			    </table>
		    </td>
        </tr>
   	<%}%>
    <tr>
        <td class="numerictablecolumnheader" style="width:100px" onmouseover="style.cursor='auto'">&nbsp;</td>
        <td class="numerictablecolumnheader" onmouseover="style.cursor='auto'"><yfc:i18n>Price</yfc:i18n></td>
		<td class="numerictablecolumnheader" onmouseover="style.cursor='auto'"><yfc:i18n>Total_Charges</yfc:i18n></td>
        <td class="numerictablecolumnheader" onmouseover="style.cursor='auto'"><yfc:i18n>Taxes</yfc:i18n></td>
        <td class="numerictablecolumnheader" onmouseover="style.cursor='auto'"><yfc:i18n>Totals</yfc:i18n></td>
    </tr>
</thead>
<tbody>
	<%	String sHdrChargesMinusHdrDiscounts = "0";
		double dblHdrDiscount = getDoubleFromLocalizedString(locale,resolveValue(buildBinding("xml:/Order/",chargeType,total,"/@HdrDiscount")));
		double dblHdrCharges = getDoubleFromLocalizedString(locale,resolveValue(buildBinding("xml:/Order/",chargeType,total,"/@HdrCharges")));
		double dblHdrChargesMinusHdrDiscounts = dblHdrCharges - dblHdrDiscount;
		sHdrChargesMinusHdrDiscounts = getLocalizedStringFromComputedDouble(locale,dblHdrChargesMinusHdrDiscounts);
		
		String sTotalHeaderAndLineCharges = "0";
		double dblTotalHeaderAndLineCharges = dblHdrChargesMinusHdrDiscounts + totalOtherCharges;
		sTotalHeaderAndLineCharges = getLocalizedStringFromComputedDouble(locale,dblTotalHeaderAndLineCharges);
	%>
    <tr>
        <td class="numerictablecolumnheader" style="width:100px" onmouseover="style.cursor='auto'"><yfc:i18n>Header</yfc:i18n></td>
        <td class="numerictablecolumn">-</td>
        <% if (equals(displayInEnterpriseCurrency,"Y")) { %>
			<td class="numerictablecolumn"><%=sHdrChargesMinusHdrDiscounts%></td>
	        <td class="numerictablecolumn"><yfc:getXMLValue name="Order" binding='<%=buildBinding("xml:/Order/",chargeType,total,"/@HdrTax")%>'/></td>
        <%} else {%>
			<td class="numerictablecolumn"><a <%=getDetailHrefOptions(ChargeViewID, "", "")%>><%=sHdrChargesMinusHdrDiscounts%></a></td>
	        <td class="numerictablecolumn"><a <%=getDetailHrefOptions(TaxViewID, "", "")%>><yfc:getXMLValue name="Order" binding='<%=buildBinding("xml:/Order/",chargeType,"Totals/@HdrTax")%>'/></a></td>
	    <%}%>
        <td class="totaltext"><% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp;<yfc:getXMLValue name="Order" binding='<%=buildBinding("xml:/Order/",chargeType,total,"/@HdrTotal")%>'/>&nbsp;<%=curr0[1]%></td>
    </tr>
    <tr>
        <td class="numerictablecolumnheader" style="width:100px" onmouseover="style.cursor='auto'"><yfc:i18n>Line</yfc:i18n></td>
        <td class="numerictablecolumn"><%=sTotalLineExtendedPrice%></td>
        <td class="numerictablecolumn"><%=stotalOtherCharges%></td>
        <td class="numerictablecolumn"><%=sTotalLineTax%></td>
        <td class="totaltext"><% String[] curr1 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr1[0]%>&nbsp;<%=sTotalLineTotal%>&nbsp;<%=curr1[1]%></td>
    </tr>
    <tr>
        <td class="numerictablecolumnheader" style="width:100px" onmouseover="style.cursor='auto'"><yfc:i18n>Totals</yfc:i18n></td>
        <td class="totaltext"><% String[] curr2 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr2[0]%>&nbsp;<%=sTotalLineExtendedPrice%>&nbsp;<%=curr2[1]%></td>
        <td class="totaltext"><% String[] curr3 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr3[0]%>&nbsp;<%=sTotalHeaderAndLineCharges%>&nbsp;<%=curr3[1]%></td> 
        <td class="totaltext"><% String[] curr4 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr4[0]%>&nbsp;<yfc:getXMLValue name="Order" binding='<%=buildBinding("xml:/Order/",chargeType,total,"/@GrandTax")%>'/>&nbsp;<%=curr4[1]%></td>
        <td class="totaltext"><% String[] curr5 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr5[0]%>&nbsp;<yfc:getXMLValue name="Order" binding='<%=buildBinding("xml:/Order/",chargeType,total,"/@GrandTotal")%>'/>&nbsp;<%=curr5[1]%></td>
    </tr>
</tbody>
</table>
