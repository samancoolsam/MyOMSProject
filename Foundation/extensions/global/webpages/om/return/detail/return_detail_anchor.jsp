<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/console/jsp/order.jspf" %>
<%@ page import="com.yantra.yfc.dom.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<%
    YFCElement oOrderElem = getElement("Order");

    if (oOrderElem != null) {
		oOrderElem.setAttribute("ReceiptEnableFlag",false);
		if (getNumericValue("xml:SingleShipmentReceiptList:/ReceiptList/@TotalNumberOfRecords") == 0) {
			if (getNumericValue("xml:MultipleShipmentReceiptList:/ReceiptList/@TotalNumberOfRecords") != 0) {
				oOrderElem.setAttribute("ReceiptEnableFlag",true);
				oOrderElem.setAttribute("TotalNumberOfReceipts",Integer.parseInt(resolveValue("xml:MultipleShipmentReceiptList:/ReceiptList/@TotalNumberOfRecords")));
			}
		} else {
				oOrderElem.setAttribute("ReceiptEnableFlag",true);
				oOrderElem.setAttribute("TotalNumberOfReceipts",Integer.parseInt(resolveValue("xml:SingleShipmentReceiptList:/ReceiptList/@TotalNumberOfRecords")));
		}
    }
	if (oOrderElem != null) {
		String sShipNode = oOrderElem.getAttribute("ShipNode");
		if (!equals(oOrderElem.getAttribute("DraftOrderFlag"),"Y"))
			if (isVoid(sShipNode) && oOrderElem.getChildElement("OrderLines") != null) {
				YFCElement oOrderLines = oOrderElem.getChildElement("OrderLines");
				for (Iterator oIter = oOrderLines.getChildren();oIter.hasNext();) {
					YFCElement oOrderLine = (YFCElement) oIter.next();
					if (!isVoid(oOrderLine.getAttribute("ShipNode"))) {
						oOrderElem.setAttribute("ReceiveFlag","Y");
						oOrderElem.setAttribute("ShipNodeReceiveFlag",oOrderLine.getAttribute("ShipNode"));
						break;
					}
				}
			} else {
				oOrderElem.setAttribute("ReceiveFlag","Y");
				oOrderElem.setAttribute("ShipNodeReceiveFlag",oOrderElem.getAttribute("ShipNode"));
			}
	}

	setHoldFuncFlags((YFCElement) request.getAttribute("Order"), isTrue("xml:/Rules/@RuleSetValue"));
%>


<%
    //prepareOrderElement((YFCElement) request.getAttribute("Order"));
    YFCElement elem = getElement("Order");

    if (elem != null) {
        //Flip the draft order flag into confirmed flag.
        elem.setAttribute("ConfirmedFlag", !isTrue("xml:/Order/@DraftOrderFlag"));
    }
%>
<%
    prepareOrderElement((YFCElement) request.getAttribute("Order"),(YFCElement) request.getAttribute("DerivedOrderLineList"),(YFCElement) request.getAttribute("ChainedOrderLineList"));

	setActiveAlerts((YFCElement) request.getAttribute("InboxList"));
%>
<%
    //check whether an exchange order exists
	boolean hasNoExchange = true;
    if (elem != null) {
		YFCElement exchangeOrders = elem.getChildElement("ExchangeOrders");
	    if (exchangeOrders != null) {
			YFCElement exchangeOrder = exchangeOrders.getChildElement("ExchangeOrder");
		    if (exchangeOrder != null) {		
				//set HasNoExchange flag to check for exchange order.
				hasNoExchange = false;
			}
		}
    }
	elem.setAttribute("HasNoExchange", hasNoExchange);

%>

<table cellSpacing=0 class="anchor" cellpadding="7px">
<tr>
    <td>
        <yfc:makeXMLInput name="orderHeaderKey">
            <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
        </yfc:makeXMLInput>
        <input type="hidden" value='<%=getParameter("orderHeaderKey")%>' name="OrderEntityKey"/>
        <input type="hidden" <%=getTextOptions("xml:/Order/@OrderHeaderKey")%>/>
        <yfc:makeXMLInput name="receiveKey">
            <yfc:makeXMLKey binding="xml:/Receipt/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey"/>
            <yfc:makeXMLKey binding="xml:/Receipt/@DocumentType" value="xml:/Order/@DocumentType"/>
            <yfc:makeXMLKey binding="xml:/Receipt/@ReceivingNode" value="xml:/Order/@ShipNodeReceiveFlag"/>
        </yfc:makeXMLInput>
        <input type="hidden" value='<%=getParameter("receiveKey")%>' name="ReceiveKey"/>
    </td>
</tr>
<tr>
    <td colspan="4">
	    <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
            <jsp:param name="ShowDocumentType" value="true"/>
            <jsp:param name="getRequestDOM" value="Y"/>
            <jsp:param name="ModifyView" value="true"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td height="100%" width="33%" id="ReturnFromAddress">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
            <jsp:param name="Path" value="xml:/Order/PersonInfoShipTo"/>
            <jsp:param name="DataXML" value="Order"/>
            <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValue("ShipToAddress", "xml:/Order/AllowedModifications")%>'/>
            <jsp:param name="ShowAnswerSetOptions" value='<%=resolveValue("xml:/QuestionType/@AnswerSetFound")%>'/>
            <jsp:param name="AnswerSetOptionsBinding" value="xml:/Order/@RetainAnswerSets"/>
        </jsp:include>
    </td>
    <td height="100%" width="33%" id="RefundToAddress">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I05"/>
            <jsp:param name="Path" value="xml:/Order/PersonInfoBillTo"/>
            <jsp:param name="DataXML" value="Order"/>
            <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValue("BillToAddress", "xml:/Order/AllowedModifications")%>'/>
        </jsp:include>
    </td>
    <td height="100%" width="34%">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
</tr>

<tr>
    <td colspan="4">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I06"/>
        </jsp:include>
    </td>
</tr>

<% if (equals(getValue("Order","xml:/Order/@HasDeliveryLines"),"Y")) //add following inner panel only if there are any Delivery service lines in the order
	{%>
<tr>
    <td colspan="4">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I07"/>
        </jsp:include>
    </td>
</tr>
<%	}%>
<% if (equals(getValue("Order","xml:/Order/@HasServiceLines"),"Y")) //add following inner panel only if there are any provided service lines in the order
	{%>
<tr>
    <td colspan="4">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I08"/>
        </jsp:include>
    </td>
</tr>
<%	}%>


</table>
