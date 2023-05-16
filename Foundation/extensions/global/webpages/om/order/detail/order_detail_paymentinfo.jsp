<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
 <!-- Added as Part of OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/console/jsp/paymentutils.jspf" %>
<jsp:include page="/om/order/detail/order_detail_paymentinfo_include.jsp" flush="true"/>

<script language="javascript"> 
	window.attachEvent("onload", onLoadAlterDisplay);	
	function onLoadAlterDisplay(){
		document.getElementById('tablePaymentInfo').style.display = 'none';
		document.getElementById('tablePaymentInfo').style.display = 'block';
	}
</script>
<%
	String sRequestDOM = request.getParameter("getRequestDOM");
    preparePaymentStatusList(getValue("Order", "xml:/Order/@PaymentStatus"), (YFCElement) request.getAttribute("PaymentStatusList"));
    computePaymentAmounts((YFCElement) request.getAttribute("Order"));
%>

<table class="view" width="100%" id="tablePaymentInfo">
    <tr>
        <td class="detaillabel" ><yfc:i18n>Status</yfc:i18n></td>
        <td>
            <select <% if (equals(sRequestDOM,"Y")) {%>OldValue="<%=resolveValue("xml:OrigAPIOutput:/Order/@PaymentStatus")%>" <%}%> <%=yfsGetComboOptions("xml:/Order/@PaymentStatus", "xml:/Order/AllowedModifications")%>>
                <yfc:loopOptions binding="xml:/PaymentStatusList/@PaymentStatus" name="DisplayDescription"
                value="CodeType" selected="xml:/Order/@PaymentStatus"/>
            </select>
        </td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Type</yfc:i18n></td>
        <% if (equals(getValue("Order","xml:/Order/@MultiplePaymentMethods"),"Y")) { %>    
        <td class="protectedtext"><yfc:i18n>MULTIPLE</yfc:i18n></td>
        <!-- Start - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
        <%} else if (equals(getValue("Order","xml:/Order/PaymentMethods/PaymentMethod/@PaymentReference5"),"Klarna")) { %>    
        <td class="protectedtext"><%=getValue("Order", "xml:/Order/PaymentMethods/PaymentMethod/@PaymentReference5")%>&nbsp;</td>
        <%} else {%>
        <td class="protectedtext">	<%=getComboText("xml:/PaymentTypeList/@PaymentType","PaymentTypeDescription","PaymentType","xml:/Order/PaymentMethods/PaymentMethod/@PaymentType",true)%>
		</td>
        <%}%>
        <!-- End - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Authorized</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalOpenAuthorizations")%>&nbsp;</span><span class="protectedtext"  style="width:8px"></span></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Collected</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalCredits")%>&nbsp;</span><span class="protectedtext"  style="width:8px"></span></td>
    </tr>

	<% if(equals(getValue("Order", "xml:/Order/@OrderPurpose"), "EXCHANGE")){ %>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Funds_Available_From_Return</yfc:i18n></td>
        <td class="protectednumber"><span class="protectednumber" ><%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@FundsAvailableFromReturn")%>&nbsp;</span><span class="protectedtext"  style="width:8px"></span></td>
    </tr>
	<% } %>
</table>
