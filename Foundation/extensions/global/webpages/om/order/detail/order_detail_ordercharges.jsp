<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<!-- Added as part of  OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="view" width="100%">
    <tr>
        <td class="detaillabel" ><yfc:i18n>Line_Sub_Total</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order","xml:/Order/OverallTotals/@LineSubTotal")%>&nbsp;</span><span class="protectedtext"  style="width:8px"><yfc:i18n>Plus</yfc:i18n></span></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Total_Charges</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order","xml:/Order/OverallTotals/@GrandCharges")%>&nbsp;</span><span class="protectedtext"  style="width:8px"><yfc:i18n>Plus</yfc:i18n></span></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Total_Tax</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order","xml:/Order/OverallTotals/@GrandTax")%>&nbsp;</span><span class="protectedtext"  style="width:8px"><yfc:i18n>Plus</yfc:i18n></span></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Total_Discount</yfc:i18n></td>
        <td class="protectednumber" nowrap="true"><span class="protectednumber" ><%=getValue("Order","xml:/Order/OverallTotals/@GrandDiscount")%>&nbsp;</span><span class="protectedtext"  style="width:8px"><yfc:i18n>Minus</yfc:i18n></span></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Grand_Total</yfc:i18n></td>
        <td class="totaltext"><span class="protectednumber" ><% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp; <%=getValue("Order","xml:/Order/OverallTotals/@GrandTotal")%>&nbsp; <%=curr0[1]%></span><span class="protectedtext"  style="width:8px">&nbsp;</span></td>
    </tr>
</table>
