<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@include file="/console/jsp/paymentutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script> 
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script> 

<%
    //this is to switch between the summary and detail collection view
    String viewType=request.getParameter("viewType");
    if (isVoid(viewType)) {
        viewType="Summary";
    }

    computePaymentAmounts((YFCElement) request.getAttribute("Order"));
    boolean hasChargesAndAuthorizations = hasChargesAndAuthorizations((YFCElement) request.getAttribute("Order"));
%>

<table class="view" width="100%">
    <tr>
        <td class="detaillabel" ><yfc:i18n>Amount_Collected_Credits</yfc:i18n></td>
        <td class="protectednumber"><% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%> <%=getValue("Order","xml:/Order/ChargeTransactionDetails/@TotalCredits")%> <%=curr0[1]%></td>
        <td class="detaillabel" ><yfc:i18n>Amount_Invoiced_Debits</yfc:i18n></td>
        <td class="protectednumber"><% String[] curr1 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr1[0]%> <%=getValue("Order","xml:/Order/ChargeTransactionDetails/@TotalDebits")%> <%=curr1[1]%></td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Total_Refunded</yfc:i18n></td>
        <td class="protectednumber"><% String[] curr2 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr2[0]%> <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalRefunds")%>
<%=curr2[1]%></td>
        <td class="detaillabel" ><yfc:i18n>Return_Invoiced_Amount</yfc:i18n></td>
        <td class="protectednumber"><% String[] curr3 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr3[0]%> <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalReturnOrderAmount")%>
<%=curr3[1]%></td>
    </tr>
	<%if (! "0003".equals(getValue("Order","xml:/Order/@DocumentType"))) {%>
		<tr>
			<td class="detaillabel" ><yfc:i18n>Open_Authorized</yfc:i18n></td>
			<td class="protectednumber"><% String[] curr4 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr4[0]%> <%=getValue("Order","xml:/Order/ChargeTransactionDetails/@TotalOpenAuthorizations")%> <%=curr4[1]%></td>
			<td class="detaillabel" ><yfc:i18n>Open_Order_Amount</yfc:i18n></td>
			<td class="protectednumber"><% String[] curr5 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr5[0]%> <%=getValue("Order","xml:/Order/ChargeTransactionDetails/@TotalOpenBookings")%> <%=curr5[1]%></td>
		</tr>
	<%}%>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Total_Cancelled</yfc:i18n></td>
        <td class="protectednumber"><% String[] curr6 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr6[0]%> <%=getValue("Order", "xml:/Order/ChargeTransactionDetails/@TotalCancelled")%>
<%=curr6[1]%></td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
	</tr>
	<% if(equals(getValue("Order", "xml:/Order/@OrderPurpose"), "EXCHANGE")){ %>
	<tr>
		<%
		String prefixSymbol = getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol");
		String postfixSymbol = getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol");
		String[] curr7 = getLocalizedCurrencySymbol( prefixSymbol, getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), postfixSymbol);

		String fundsAvailFromReturn = curr7[0] + " " + getValue("Order", "xml:/Order/ChargeTransactionDetails/@FundsAvailableFromReturn") + " " + curr7[1];

		double dPendingTransferIn = getNumericValue("xml:/Order/@PendingTransferIn");

		String pendingTransfer =  curr7[0] + " " + getValue("Order", "xml:/Order/@PendingTransferIn") + " " + curr7[1];
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
<table class="table" cellpadding="0" cellspacing="0" width="100%"  suppressFooter="true">
<tbody>
    <tr>
    <td>
        <select name="viewType" class="combobox" onchange="yfcChangeDetailView(getCurrentViewId());">
            <option value="Summary" <%if (equals(viewType,"Summary")) {%> selected <%}%>><yfc:i18n>Charge_and_Authorization_Summary</yfc:i18n></option>
            <option value="Detail" <%if (equals(viewType,"Detail")) {%> selected <%}%>><yfc:i18n>Advanced_Collection_Detail</yfc:i18n></option>
        </select>&nbsp;&nbsp;
        <input type="button" class="button" value="<%=getI18N("Expand_All")%>" <% if(!hasChargesAndAuthorizations){ %> disabled="true" <%}%>  onclick="expandAll('TR','yfsPaymentInfo_','<%=replaceI18N("Click_To_Hide_Payment_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>' )" style="cursor:hand" />
        <input type="button" class="button" value="<%=getI18N("Collapse_All")%>"   <% if(!hasChargesAndAuthorizations){ %> disabled="true" <%}%> onclick="collapseAll('TR','yfsPaymentInfo_','<%=replaceI18N("Click_To_See_Payment_Info")%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>' )" style="cursor:hand"/>
    </td>
    </tr>
</tbody>
</table>
    <table class="table" editable="false" cellSpacing="0" cellPadding="0" width="100%" suppressFooter="true">
        <thead>
            <tr>
                <td class="tablecolumnheader">&nbsp;</td>
                <td class="tablecolumnheader"><yfc:i18n>Date</yfc:i18n></td>
                <td class="tablecolumnheader"><yfc:i18n>Transaction_Type</yfc:i18n></td>
                <% if(!isSummaryScreen(viewType)){%>
                    <td class="tablecolumnheader"><yfc:i18n>Open_Order</yfc:i18n></td>
                <%}%>
                <td class="tablecolumnheader"><yfc:i18n>Authorized</yfc:i18n></td>
                <% if(!isSummaryScreen(viewType)){%>
                    <td class="tablecolumnheader"><yfc:i18n>Pre_Settled_Amount</yfc:i18n></td>
                    <td class="tablecolumnheader"><yfc:i18n>Invoiced</yfc:i18n></td>
                <%}%>
                <td class="tablecolumnheader"><yfc:i18n>Collected</yfc:i18n></td>
                <td class="tablecolumnheader"><yfc:i18n>Pending_Execution</yfc:i18n></td>
                <td class="tablecolumnheader"><yfc:i18n>Status</yfc:i18n></td>
            </tr>
        </thead>
        <tbody>
            <%  ArrayList chargeTranList = getLoopingElementList("xml:/Order/ChargeTransactionDetails/@ChargeTransactionDetail");
                for (int ChargeTransactionDetailCounter = 0; ChargeTransactionDetailCounter < chargeTranList.size(); ChargeTransactionDetailCounter++) {

            //check if row needs to be displayed

                    YFCElement singleChargeTran = (YFCElement) chargeTranList.get(ChargeTransactionDetailCounter);
                    pageContext.setAttribute("ChargeTransactionDetail", singleChargeTran); 
                    
                String chargeType = singleChargeTran.getAttribute("ChargeType");
                if(showCollectionRecord(viewType, chargeType)){
                    
                    %>
        
                <% request.setAttribute("ChargeTransactionDetail", (YFCElement)pageContext.getAttribute("ChargeTransactionDetail"));%>
               <% String divToDisplay = "yfsPaymentInfo_" + ChargeTransactionDetailCounter; %>
                <yfc:makeXMLInput name="InvoiceKey">
                    <yfc:makeXMLKey binding="xml:/GetOrderInvoiceDetails/@InvoiceKey" value="xml:/ChargeTransactionDetail/@OrderInvoiceKey" />
                </yfc:makeXMLInput>
                <yfc:makeXMLInput name="chargeTransactionKey">
                    <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey" />                         
                    <yfc:makeXMLKey binding="xml:/Order/SelectedChargeTransactionDetail/@ChargeTransactionKey" value="xml:/ChargeTransactionDetail/@ChargeTransactionKey" />
                </yfc:makeXMLInput>
                <tr >                         
                    <td class="tablecolumn">
                        <% if (equals(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@HoldAgainstBook"),"Y")) {   %>
                            <img <%=getImageOptions(YFSUIBackendConsts.PRE_COLLECTED_CREDIT_COLUMN, "Pre_Collected_Amount")%>/>
                        <%}else{%>
                        &nbsp;
                        <%}%>
                    </td>

                    <td class="tablecolumn" sortValue="<%=getDateValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@Createts")%>">
                        <yfc:getXMLValue binding="xml:/ChargeTransactionDetail/@Createts"/>&nbsp;
                    </td>
					<% String chargeTypeDesc = "ChargeType_" + resolveValue("xml:/ChargeTransactionDetail/@ChargeType"); %>

					<td class="tablecolumn">
                        <% if (equals("TRANSFER_IN",getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@ChargeType"))){
						%>
						  <yfc:makeXMLInput name="TransferFromOhKey">
								<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/ChargeTransactionDetail/@TransferFromOhKey" />
						   </yfc:makeXMLInput>
                            <a <%=getDetailHrefOptions("L04", getValue("ChargeTransactionDetail", 		
								"xml:/ChargeTransactionDetail/TransferFromOrder/@DocumentType"), getParameter("TransferFromOhKey"), "")%>>
	                            <yfc:i18n><%=chargeTypeDesc%></yfc:i18n>
							</a>
						<% } else if( equals("TRANSFER_OUT",getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@ChargeType"))) {
						%>
							<yfc:makeXMLInput name="TransferToOhKey">
								<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/ChargeTransactionDetail/@TransferToOhKey" />
							</yfc:makeXMLInput>
                            <a <%=getDetailHrefOptions("L05", getValue("ChargeTransactionDetail", 		
								"xml:/ChargeTransactionDetail/TransferToOrder/@DocumentType"), getParameter("TransferToOhKey"), "")%>>
                            <yfc:i18n><%=chargeTypeDesc%></yfc:i18n>
							</a>
						<%} else {%>
							<yfc:i18n><%=chargeTypeDesc%></yfc:i18n>
						<%}%>
                        <% boolean bWriteDivTag = false; %>
                        <% if (equals("AUTHORIZATION",getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@ChargeType")) ||    equals("CHARGE",getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@ChargeType"))) {%>
                            <img id="<%=divToDisplay+"_img"%>" onclick="expandCollapseDetails('<%=divToDisplay%>','<%=replaceI18N("Click_To_See_Payment_Info")%>','<%=replaceI18N("Click_To_Hide_Payment_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Payment_Info")%> />
                            <% bWriteDivTag = true; %>
                        <%}%>&nbsp;
                    </td>
                    <% if(!isSummaryScreen(viewType)){%>
                        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@BookAmount")%>">                             <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@BookAmount"))%>
                        </td>
                    <%}%>
                    <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@OpenAuthorizedAmount")%>">
                    <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@OpenAuthorizedAmount"))%>&nbsp;
                    </td>
                    <% if(!isSummaryScreen(viewType)){%>
                        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@SettledAmount")%>">
                        <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@SettledAmount"))%>
                        </td>
                        <% if (!isVoid(getValue("ChargeTransactionDetail", "xml:/ChargeTransactionDetail/@OrderInvoiceKey"))) { %>
                            <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@DebitAmount")%>">
                            <a <%=getDetailHrefOptions("L01",getParameter("InvoiceKey"),"")%>>
                            <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@DebitAmount"))%>
                            </td>
                        <% }
                        else {%>
                            <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@DebitAmount")%>">                             <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@DebitAmount"))%>
                            </td>
                        <%}%>
                    <% }%>                        
                    <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@CreditAmount")%>">
                     <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@CreditAmount"))%>&nbsp;
                    </td>
                    <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ChargeTransactionDetail:/ChargeTransactionDetail/@RequestAmount")%>">
                    <%=getColumnFormattedValue(getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@RequestAmount"))%>&nbsp;
                    </td>
					<% String chargeStatusDesc = "ChargeStatus_" + resolveValue("xml:/ChargeTransactionDetail/@Status"); %>
                    <td class="tablecolumn"><yfc:i18n><%=chargeStatusDesc%></yfc:i18n>&nbsp;</td>
                </tr>
                <% if (bWriteDivTag == true) { %>
                    <tr id='<%=divToDisplay%>' style="display:none;padding-top:5px" BypassRowColoring='true'>
                        <% if(!isSummaryScreen(viewType)){%>
                            <td colspan="10" class="tablecolumn">&nbsp;
                        <%}else{%>
                            <td colspan="7" class="tablecolumn">&nbsp;
                        <%}%>
                        <table class="table" >
                        <tbody>
                        <tr>
                            <td width="10%">&nbsp;</td>
                            <td width="30%" class="tablecolumn">&nbsp;
						<%
							String paymentType = getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/PaymentMethod/@PaymentType");
							String paymentTypeGrp = "";
							if(!isVoid(paymentType)){
								YFCElement paymentTypeListElem = (YFCElement)request.getAttribute("PaymentTypeList");
								for (Iterator i = paymentTypeListElem.getChildren(); i.hasNext();) {
									YFCElement childElem = (YFCElement) i.next();
									if(equals(paymentType, childElem.getAttribute("PaymentType"))){
										paymentTypeGrp = childElem.getAttribute("PaymentTypeGroup");
										break;
									}
								}
							}
						%>
                            <fieldset>
                            <legend><yfc:i18n><%=getPaymentCollectionTitle(paymentTypeGrp)%></yfc:i18n></legend>
                              	<!-- Start - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
                                <jsp:include page="/extn/om/order/detail/order_detail_paymenttype_collections.jsp" flush="true">
                                <!-- End - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM) -->
                                <jsp:param name="PrePathId" value="ChargeTransactionDetail/PaymentMethod"/>
                                <jsp:param name="ShowAdditionalParams" value="Y"/>
                                </jsp:include>
                            </fieldset> 
                            </td>                       
                        
                            <td width="50%" class="tablecolumn">&nbsp;
                            <%
                            YFCElement invoiceElem = (YFCElement)singleChargeTran.getChildElement("InvoiceCollectionDetails");
                            boolean hasInvoiceCollection = false;
                            if(invoiceElem.getChildNodes().getLength() > 0)
                                hasInvoiceCollection = true;
                            if(equals("CHARGE",getValue("ChargeTransactionDetail","xml:/ChargeTransactionDetail/@ChargeType")) && hasInvoiceCollection){ %>
                                <fieldset>
                                <legend><yfc:i18n>Invoice_Collection_Information</yfc:i18n></legend>
                                    <jsp:include page="/om/order/detail/order_detail_invoicecollectiondetails.jsp" flush="true">
                                    <jsp:param name="PrePathId" value="ChargeTransactionDetail/InvoiceCollectionDetails"/>
                                    </jsp:include>
                                </fieldset>
                            <% }else{%>
                            &nbsp;
                            <% } %>
                            </td>                       
                            <td width="10%">&nbsp;</td>
                            </tr>
                        </tbody>
                        </table>
                        </td>
                    </tr>
                <% } %>
                <% }//end of if (show collection record) %>
            <% }//end of for loop %>
        </tbody>
    </table>
