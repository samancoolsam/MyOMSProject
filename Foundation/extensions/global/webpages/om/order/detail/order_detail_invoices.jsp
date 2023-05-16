<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<table class="table" editable="false" width="100%" cellpadding="0" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
                <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
            </td>
            <td class="tablecolumnheader" nowrap="true"><yfc:i18n>Invoice_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Invoice_Type</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Total_Amount</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Amount_Collected</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/OrderInvoiceList/@OrderInvoice" id="OrderInvoice">
            <tr>
                <yfc:makeXMLInput name="invoiceKey">
                    <yfc:makeXMLKey binding="xml:/GetOrderInvoiceDetails/@InvoiceKey" value="xml:/OrderInvoice/@OrderInvoiceKey" />
                </yfc:makeXMLInput>
                <td>
                    <input type="checkbox" value='<%=getParameter("invoiceKey")%>' name="chkEntityKey"/>
                </td>
                <td class="tablecolumn"><a <%=getDetailHrefOptions("L01",getParameter("invoiceKey"),"")%>>
                    <yfc:getXMLValue binding="xml:/OrderInvoice/@InvoiceNo"/></a>
                </td>
                <td class="tablecolumn"><%=getI18N(getValue("OrderInvoice", "xml:/OrderInvoice/@InvoiceType"))%></td>
                <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:OrderInvoice:/OrderInvoice/@TotalAmount")%>">
                    <% String[] curr0 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr0[0]%>&nbsp;<yfc:getXMLValue binding="xml:/OrderInvoice/@TotalAmount"/>&nbsp;<%=curr0[1]%>
                </td>
                <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:OrderInvoice:/OrderInvoice/@AmountCollected")%>">
                    <% String[] curr1 = getLocalizedCurrencySymbol( getValue("CurrencyList", "xml:/CurrencyList/Currency/@PrefixSymbol"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@Currency"), getValue("CurrencyList", "xml:/CurrencyList/Currency/@PostfixSymbol"));%><%=curr1[0]%>&nbsp;<yfc:getXMLValue binding="xml:/OrderInvoice/@AmountCollected"/>&nbsp;<%=curr1[1]%>
                </td>
            </tr>
        </yfc:loopXML>
   </tbody>
</table>
