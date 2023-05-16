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
    document.body.attachEvent("onunload", processSaveRecordsForOrder);
</script>
<%
    boolean displayReleaseNo = true;
    String showReleaseNo = request.getParameter("ShowReleaseNo");
    if(showReleaseNo != null && showReleaseNo.equals("N")){
        displayReleaseNo = false;
    }
%>

<% 
	boolean hdrMode = true; 
%>

<%
String sBindingNode=request.getParameter("BindingNode");
if (isVoid(sBindingNode))
	sBindingNode="xml:/OrderLineStatusList";
%>

	<%if (hdrMode) {%>
	<table class="table" width="20%" ID="StatusBreakup" yfcMandatoryMessage="<yfc:i18n>Drop_status_must_be_specified</yfc:i18n>">
	<tr>
    <td class="detaillabel">
        <yfc:i18n>Authorize_Drop_Status</yfc:i18n>
    </td>
    <td>
        <select <%=getComboOptions("xml:/OrderStatusChange/@BaseDropStatus")%>  class="combobox" >
            <yfc:loopOptions binding="xml:OrderLineStatusList:/OrderLineStatusList/DropStatuses/@DropStatus" 
                name="Description" value="Status" selected="xml:/OrderStatusChange/@BaseDropStatus" isLocalized="Y"/>
        </select>
		<input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/@ChangeForAllAvailableQty","Y")%> />
    </td>
	<%for (int i=0;i<90;i++) {%>
		<td>&nbsp;</td>
	<%	}%>
	</tr>
	</table>
	<%}%>
<table class="table" width="100%" ID="StatusBreakup">
<input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/@OrderHeaderKey", "xml:/Order/@OrderHeaderKey")%>/>
<input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/@Override","N")%>/>
<thead>
    <tr>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/@PrimeLineNo")%>"><yfc:i18n>Line</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ItemID")%>"><yfc:i18n>Item_ID</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ProductClass")%>"><yfc:i18n>PC</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderStatusTranQuantity/@TransactionalUOM")%>"><yfc:i18n>UOM</yfc:i18n></td>
        <td class=tablecolumnheader style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@ShipNode")%>"><yfc:i18n>Ship_Node</yfc:i18n></td>
        <td class=tablecolumnheader sortable="no" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderStatusTranQuantity/@StatusQty")%>"><yfc:i18n>Quantity</yfc:i18n></td>
		<% if (! hdrMode) {%>
        <td class=tablecolumnheader sortable="no" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/OrderLineTranQuantity/@StatusQuantity")%>"><yfc:i18n>Quantity_To_Authorize</yfc:i18n></td>
        <td class=tablecolumnheader sortable="no" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@Status")%>"><yfc:i18n>Authorize_Drop_Status</yfc:i18n></td>
        <%} %>
    </tr>
</thead>

<tbody>
    <yfc:loopXML name="OrderLineStatusList" binding="xml:/OrderLineStatusList/@OrderStatus" id="OrderStatus">
        <tr>
            <yfc:makeXMLInput name="orderLineKey">
                <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderStatus/@OrderLineKey"/>
                <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineScheduleKey" value="xml:/OrderStatus/@OrderLineScheduleKey"/>
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
                <input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/@TransactionId", "xml:/OrderLineStatusList/DropStatuses/@TransactionId")%>/>
                <input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/OrderLines/OrderLine_" + OrderStatusCounter + "/@OrderLineKey", "xml:/OrderStatus/@OrderLineKey")%>/>
                <input type="hidden" <%=getTextOptions("xml:/OrderStatusChange/OrderLines/OrderLine_" + OrderStatusCounter + "/@OrderReleaseKey", "xml:/OrderStatus/@OrderReleaseKey")%>/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue name="OrderStatus" binding="xml:/OrderStatus/OrderLine/Item/@ItemID"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue name="OrderStatus" binding="xml:/OrderStatus/OrderLine/Item/@ProductClass"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue name="OrderStatus" binding="xml:/OrderStatus/OrderStatusTranQuantity/@TransactionalUOM"/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue name="OrderStatus" binding="xml:/OrderStatus/OrderLine/@ShipNode"/>
            </td>
            <td nowrap="true" class="numerictablecolumn">
                <yfc:getXMLValue binding="xml:/OrderStatus/OrderStatusTranQuantity/@StatusQty"/>&nbsp;<br>
                <!--<input type="text" class="numericprotectedinput" <%=getTextOptions("xml:/OrderStatusChange/OrderLines/OrderLine_" + OrderStatusCounter + "/OrderLineTranQuantity/@Quantity", "xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@StatusQuantity")%>/>-->
            </td>
		<% if (! hdrMode) {%>
            <td nowrap="true" class="numericunprotectedinput">
                <yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@StatusQuantity"/>&nbsp;<br>
                <input type="text" class="numericprotectedinput" <%=getTextOptions("xml:/OrderStatusChange/OrderLines/OrderLine_" + OrderStatusCounter + "/OrderLineTranQuantity/@Quantity", "xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@StatusQuantity")%>/>
            </td>
			<td>
				<select <%=getComboOptions("xml:/OrderStatusChange/OrderLines/OrderLine_" + OrderStatusCounter + "/@BaseDropStatus")%>  class="combobox" >
					<yfc:loopOptions binding="xml:OrderLineStatusList:/OrderLineStatusList/DropStatuses/@DropStatus" 
						name="Description" value="Status" isLocalized="Y"/>
				</select>
			</td>
		<%}%>
        </tr>
    </yfc:loopXML>
</tbody>
</table>
