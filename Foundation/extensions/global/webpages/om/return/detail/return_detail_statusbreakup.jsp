<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
    window.attachEvent("onload", IgnoreChangeNames);
    document.body.attachEvent("onunload", processSaveRecordsForOrder);
</script>
<%
    boolean displayReleaseNo = false;
%>

<table class="table" width="100%" ID="StatusBreakup">
<input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/@OrderHeaderKey", "xml:/OrderLineStatusList/OrderStatus/@OrderHeaderKey")%>/>
<input type="hidden" name="xml:/ChangeSchedule/@ModificationReasonCode" />
<input type="hidden" name="xml:/ChangeSchedule/@ModificationReasonText"/>
<input type="hidden" name="xml:/ChangeSchedule/@Override" value="N"/>
<thead>
    <tr>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/@PrimeLineNo")%>"><yfc:i18n>Line</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ItemID")%>"><yfc:i18n>Item_ID</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ProductClass")%>"><yfc:i18n>PC</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/OrderLineTranQuantity/@TransactionalUOM")%>"><yfc:i18n>UOM</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@ShipNode")%>"><yfc:i18n>Ship_Node</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/Schedule/@TagNumber")%>"><yfc:i18n>Tag_#</yfc:i18n></td>
        <td class=tablecolumnheader sortable="no" style="width:<%= getUITableSize("xml:/OrderStatus/OrderStatusTranQuantity/@StatusQty")%>"><yfc:i18n>Quantity</yfc:i18n></td>
        <% if(displayReleaseNo){ %>
            <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderRelease/@ReleaseNo")%>"><yfc:i18n>Release_#</yfc:i18n></td>
        <% } %>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@StatusDate")%>"><yfc:i18n>Last_Changed_On</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@Status")%>"><yfc:i18n>Status</yfc:i18n></td>
	</tr>
</thead>
<tbody>
    <yfc:loopXML binding="xml:/OrderLineStatusList/@OrderStatus" id="OrderStatus">
        <tr>
            <yfc:makeXMLInput name="orderLineKey">
                <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderStatus/OrderLine/@OrderLineKey"/>
            </yfc:makeXMLInput>
            <yfc:makeXMLInput name="orderReleaseKey">
                <yfc:makeXMLKey binding="xml:/OrderReleaseDetail/@OrderReleaseKey" value="xml:/OrderStatus/@OrderReleaseKey" />
            </yfc:makeXMLInput>
            <td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderStatus:/OrderStatus/OrderLine/@PrimeLineNo")%>">
				<% if(showOrderLineNo("Order","Order")) {%>
	                <a <%=getDetailHrefOptions("L01",getParameter("orderLineKey"),"")%>><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/@PrimeLineNo"/></a>
				<%} else {%>
					<yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/@PrimeLineNo"/>
				<%}%>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@OrderLineKey", "xml:/OrderStatus/@OrderLineKey")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@OrderReleaseKey", "xml:/OrderStatus/@OrderReleaseKey")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@FromExpectedShipDate", "xml:/OrderStatus/Schedule/@ExpectedShipmentDate")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@FromStatus", "xml:/OrderStatus/@Status")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@FromTagNo", "xml:/OrderStatus/Schedule/@TagNumber")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/@OrderLineScheduleKey", "xml:/OrderStatus/Schedule/@OrderLineScheduleKey")%>/>
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/ToSchedules/ToSchedule/@ToTagNo", "xml:/OrderStatus/Schedule/@TagNumber")%> >
                <input type="hidden" <%=getTextOptions("xml:/ChangeSchedule/FromSchedules/FromSchedule_" + OrderStatusCounter + "/ToSchedules/ToSchedule/ToScheduleTranQuantity/@Quantity", "xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@StatusQuantity")%> >
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/Item/@ItemID"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/Item/@ProductClass"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@TransactionalUOM"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/@ShipNode"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/Schedule/@TagNumber"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderStatusTranQuantity/@StatusQty"/>
            </td>
            <% if(displayReleaseNo){ %>
                <td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderStatus:/OrderStatus/OrderRelease/@ReleaseNo")%>">
                    <a <%=getDetailHrefOptions("L02",getParameter("orderReleaseKey"),"")%>>
                    <yfc:getXMLValue binding="xml:/OrderStatus/OrderRelease/@ReleaseNo"/></a>
                </td>
            <% } %>
            <td class="tablecolumn" sortValue="<%=getDateValue("xml:OrderStatus:/OrderStatus/@StatusDate")%>"><yfc:getXMLValue binding="xml:/OrderStatus/@StatusDate" />
            </td>
			<!-- order jsp jas it as above -->
            <td class="tablecolumn"><%=getDBString(getValue("OrderStatus","xml:/OrderStatus/@StatusDescription"))%>
        </tr>
    </yfc:loopXML>
</tbody>
</table>
