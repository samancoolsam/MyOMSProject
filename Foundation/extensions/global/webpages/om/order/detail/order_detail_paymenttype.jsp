<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/console/jsp/chargeutils.jspf" %>
<%@ include file="/console/jsp/order.jspf" %>
<%@ include file="/console/jsp/paymentutils.jspf" %>
<%@page  import="com.yantra.yfc.ui.backend.util.*" %>

<%
    computePaymentAmounts((YFCElement) request.getAttribute("Order"));
%>

<table class="anchor" cellpadding="7px">
<tr>
    <td valign="top">
        <table class="view" width="100%">
            <tr>
                <td class="detaillabel" ><yfc:i18n>Payment_Rule</yfc:i18n></td>
                <td>
                    <select <%=yfsGetComboOptions("xml:/Order/@PaymentRuleId", "xml:/Order/AllowedModifications")%>>
                        <yfc:loopOptions binding="xml:/PaymentRuleList/@PaymentRuleId" name="Description" value="PaymentRuleId" selected="xml:/Order/@PaymentRuleId" isLocalized="Y" />
                    </select>
                </td>
				<td></td>
				<td></td>
				<td></td>
			</tr>
			<tr>
                <td class="detaillabel" ><yfc:i18n>Total_Collected</yfc:i18n></td>
                <td class="protectednumber">
                    <% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%> 
                    <a <%=getDetailHrefOptions("L01", "", "")%>><%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalCredits")%></a>
                    <%=curr0[1]%>
                </td>
                <td class="detaillabel" ><yfc:i18n>Total_Refunded</yfc:i18n></td>
                <td class="protectednumber">
                    <% String[] curr1 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr1[0]%> 
                    <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalRefunds")%>
                    <%=curr1[1]%>
                </td>
            </tr>      
            <tr>
				<td class="detaillabel" ><yfc:i18n>Total_Adjustments</yfc:i18n></td>
				<td class="protectednumber">
					<% String[] curr2 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr2[0]%> <%=getValue("Order", "xml:/Order/@TotalAdjustmentAmount")%> <%=curr2[1]%>
				</td>
				<td class="detaillabel" ><yfc:i18n>Total_Cancelled</yfc:i18n></td>
				<td class="protectednumber">
					<% String[] curr3 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr3[0]%> 
                    <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalCancelled")%>
					<%=curr3[1]%>
				</td>
            </tr>
            <tr>
				<% if(!equals(getParameter("ShowAuthorized"),"N")) {%>
					<td class="detaillabel" ><yfc:i18n>Open_Authorized</yfc:i18n></td>
					<td class="protectednumber">
						<% String[] curr4 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr4[0]%> <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalOpenAuthorizations")%> <%=curr4[1]%>
					</td>
				<%} else {%>
					<td colspan="2">&nbsp;</td>
				<%}%>
                <td colspan="2">&nbsp;</td>
            </tr>
		
			<% if(equals(getValue("Order", "xml:/Order/@OrderPurpose"), "EXCHANGE")){ %>
			<tr>
				<%
				String prefixSymbol = getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol");
				String postfixSymbol = getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol");
				String[] curr5 = getLocalizedCurrencySymbol( prefixSymbol, getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), postfixSymbol);

				String fundsAvailFromReturn = curr5[0] + " " + getValue("Order", "xml:/Order/ChargeTransactionDetails/@FundsAvailableFromReturn") + " " + curr5[1];

				double dPendingTransferIn = getNumericValue("xml:/Order/@PendingTransferIn");

				String pendingTransfer =  curr5[0] + " " + getValue("Order", "xml:/Order/@PendingTransferIn") + " " + curr5[1];
				String fundsAvailString = getFormatedI18N("Awaiting_Credit_For_Product_Receipt", new String[]{ pendingTransfer });
				%>

				<td class="detaillabel" ><yfc:i18n>Funds_Available_From_Return</yfc:i18n></td>
				<td class="protectednumber">
					<%=fundsAvailFromReturn%>
				</td>

				<%	if(!YFCCommon.equals(0, dPendingTransferIn) )	{	%>
					<td class="protectedtext" colspan="2">
						<%=fundsAvailString%>
					</td>
				<% } %>
			</tr>
			<% } %>
        </table>
    </td>
</tr>
<tr>
    <td valign="top">
        <table class="view" width="100%" cellSpacing="10">
        <%  ArrayList paymentMethodsList = getLoopingElementList("xml:/Order/PaymentMethods/@PaymentMethod");
            for (int PaymentMethodCounter = 0; PaymentMethodCounter < paymentMethodsList.size(); PaymentMethodCounter++) {
                
                YFCElement singlePaymentMethod = (YFCElement) paymentMethodsList.get(PaymentMethodCounter);
                pageContext.setAttribute("PaymentMethod", singlePaymentMethod);
                
                /*The line directly below has been added to work around a bug in innerpanel.jsp.
                  Need to assess the impact of the proposed change to innerpanel.jsp before making 
                  the fix and removing the line below.*/
                  
                request.removeAttribute(YFCUIBackendConsts.YFC_CURRENT_INNER_PANEL_ID);
    
                String paymentType = getValue("PaymentMethod", "xml:/PaymentMethod/@PaymentType");
				String paymentTypeGroup = "";
				if(!isVoid(paymentType)){
					YFCElement paymentTypeListElem = (YFCElement)request.getAttribute("PaymentTypeList");
					for (Iterator i = paymentTypeListElem.getChildren(); i.hasNext();) {
						YFCElement childElem = (YFCElement) i.next();
						if(equals(paymentType, childElem.getAttribute("PaymentType"))){
							paymentTypeGroup = childElem.getAttribute("PaymentTypeGroup");
							break;
						}
					}
				}
                request.setAttribute("PaymentMethod", pageContext.getAttribute("PaymentMethod"));
    
                preparePaymentMethodElement((YFCElement) request.getAttribute("PaymentMethod"), paymentTypeGroup);
            %>
            <!-- The IF condition below was added to remove the blank element. -->
            <% if(!isVoid(resolveValue("xml:/PaymentMethod/@PaymentKey"))){%>
                <tr>
                    <td addressip="true">
                        <jsp:include page="/yfc/innerpanel.jsp" flush="true">
                            <jsp:param name="CurrentInnerPanelID" value="I03"/>
                            <jsp:param name="PaymentMethodCounter" value='<%=String.valueOf(PaymentMethodCounter)%>'/>  
                            <jsp:param name="Title" value='<%=getLocalizedPaymentType(paymentTypeGroup)%>'/>
							<jsp:param name="Path" value="xml:/Order/PersonInfoBillTo"/>
							<jsp:param name="AllowedModValue" value='N'/>
							<jsp:param name="DataXML" value="Order"/>
                        </jsp:include>
                    </td>
                </tr>
            <%}
        }%>
        </table>
    </td>
</tr>
</table>


