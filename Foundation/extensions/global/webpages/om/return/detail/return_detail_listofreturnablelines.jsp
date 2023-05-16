<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ include file="/console/jsp/order.jspf" %>

<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.YFCDate" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script> 
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<%!
	public static String DEFAULT_BASE="Order";
%>

<%
	String bindingNodeBase = request.getParameter("BindingNode");
	if (isVoid(bindingNodeBase))  {
		bindingNodeBase=DEFAULT_BASE;
	}
	String bindingNode="xml:/"+bindingNodeBase+"/"; 
%>

<script language="javascript">
function myInit() {
	IgnoreChangeNames();

	var myDialogArguments = window.dialogArguments;
	if (myDialogArguments) {
		if (myDialogArguments.orderKey) {
			// Depending on which page (header or release) this page is called from, it may either be passed pass the header key or release key, 
			// Hence the same key is assigned to both fields. Only 1 of them is used by the API
			var thisPageOrderHeaderKey = document.all("xml:/Order/@OrderHeaderKey");
			thisPageOrderHeaderKey.value = myDialogArguments.orderKey;
			var thisPageOrderReleaseKey = document.all("xml:/OrderRelease/@OrderReleaseKey");
			thisPageOrderReleaseKey.value = myDialogArguments.orderKey;
		}
		if (myDialogArguments.buyer || myDialogArguments.seller || myDialogArguments.currency) {
			checkAddlInfo(myDialogArguments);
		}
	}
}

function checkAddlInfo(myDialogArguments) {
	var buyer = document.all("NewBuyer");
	var seller = document.all("NewSeller");
	var currency = document.all("NewCurrency");
	
	if(buyer.value != "" ||  seller.value != "" || currency.value != ""){
		//alert(buyer.value + '=' + myDialogArguments.buyer+' - '+seller.value + '=' + myDialogArguments.seller+' - '+currency.value + '=' + myDialogArguments.currency);

		if (buyer.value != myDialogArguments.buyer || seller.value != myDialogArguments.seller || currency.value != myDialogArguments.currency) {
			alert(YFCMSG029);
			window.close();
		}
	}
}

</script>

<script language="javascript">
    window.attachEvent("onload", myInit);
    document.body.attachEvent("onunload", processSaveRecordsForReturnableLines);
</script>

<table class="table" width="100%" ID="ReturnableLines">
<thead>
    <tr>

        <% // Call APIs to refresh return reasons and line types based on the selected document type
           // that appears in the header inner panel of the return entry screen.
        %>
        <yfc:callAPI apiID="AP3"/>
        <yfc:callAPI apiID="AP4"/>

		<td class="tablecolumnheader" style="width:20px">&nbsp;</td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/@PrimeLineNo")%>"><yfc:i18n>Line</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ItemID")%>"><yfc:i18n>Item_ID</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/Item/@ProductClass")%>"><yfc:i18n>PC</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/OrderLine/OrderLineTranQuantity/@TransactionalUOM")%>"><yfc:i18n>UOM</yfc:i18n></td>
        <td class="tablecolumnheader" ><yfc:i18n>Description</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/Schedule/@TagNumber")%>"><yfc:i18n>Tag_#</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/Schedule/@ShipByDate")%>"><yfc:i18n>Ship_By_Date</yfc:i18n></td>
		<td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@StatusQty")%>"><yfc:i18n>Available_To_Return</yfc:i18n></td>
        <td class="tablecolumnheader" sortable="no" style="width:<%= getUITableSize("xml:/OrderLineStatusList/OrderStatus/@StatusQty")%>"><yfc:i18n>Return_Qty</yfc:i18n></td>
		<td class="tablecolumnheader" sortable="no"><yfc:i18n>Line_Type</yfc:i18n></td>
		<td class="tablecolumnheader" sortable="no"><yfc:i18n>Reason_Code</yfc:i18n></td>
        <td class="tablecolumnheader" sortable="no"><yfc:i18n>Ship_Node</yfc:i18n></td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML binding="xml:/OrderLineStatusList/@OrderStatus" id="OrderStatus">
		<%if (equals(getValue("OrderStatus","xml:/OrderStatus/OrderLine/@ItemGroupCode"),"PROD"))
			//display line in this inner panel only for product lines
		{%>
        <tr>
            <yfc:makeXMLInput name="orderLineKey">
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/OrderStatus/OrderLine/@OrderLineKey"/>
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@PrimeLineNo" value="xml:/OrderStatus/OrderLine/@PrimeLineNo"/>
                    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@SubLineNo" value="xml:/OrderStatus/OrderLine/@SubLineNo"/>
            </yfc:makeXMLInput>
			<% request.setAttribute("OrderStatus", pageContext.getAttribute("OrderStatus"));
			%>
			<yfc:callAPI apiID='AP1'/>
				<% 
					int retWin = (int) getNumericValue("xml:/Item/PrimaryInformation/@ReturnWindow");
					String dateFrom = getValue("OrderStatus", "xml:/OrderStatus/Schedule/@ExpectedShipmentDate");

					YFCDate statusDate;
					if (isVoid(dateFrom))
						statusDate = new YFCDate();
					else
						statusDate= new YFCDate(dateFrom, getLocale());
					YFCDate retWinDate = statusDate.getNewDate(retWin);
                    String alertMessage = "";

                    if (! isTrue("xml:/Item/PrimaryInformation/@IsReturnable")) {
                        alertMessage = getI18N("Item_is_not_returnable._");
                    }

                    if (isTrue("xml:/Item/PrimaryInformation/@CreditWOReceipt") ) {
                        alertMessage  += getI18N("Credit_allowed_without_receipt._");
                    }

                    if (retWinDate.lt(statusDate, true)) {
                        alertMessage += getI18N("Return_window_expired_on_") +  retWinDate.getString(getLocale(), false) ;
                    }
				%>
					<td class="tablecolumn">
					<% if (equals(getValue("OrderStatus","xml:/OrderStatus/OrderLine/@GiftFlag"),"Y") ){ %>
						<img <%=getImageOptions(request.getContextPath() + "/console/icons/gift.gif", "This_is_a_Gift_Line")%>/>
					<% } %>
                    <% if (!isVoid(alertMessage)){%>
    					<img class="columnicon" onmouseover="this.style.cursor='default'" title='<%=alertMessage%>' <%=getImageOptions(YFSUIBackendConsts.YANTRA_TITLE_ALERT_EXCEPTIONS, "Exceptions")%>/>
				<%} else {%>
                        &nbsp;
				<%}%>
					</td>
            <td class="tablecolumn" sortValue="<%=getNumericValue("xml:OrderStatus:/OrderStatus/OrderLine/@PrimeLineNo")%>">
				<% if(showOrderLineNo("Order","Order")) {%>
	                <a <%=getDetailHrefOptions("L01",getParameter("orderLineKey"),"")%>><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/@PrimeLineNo"/></a>
				<%} else {%>
					<yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/@PrimeLineNo"/>
				<%}%>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/DerivedFrom/@OrderLineKey", "xml:/OrderStatus/OrderLine/@OrderLineKey")%>/>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/DerivedFrom/@PrimeLineNo", "xml:/OrderStatus/OrderLine/@PrimeLineNo")%>/>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/DerivedFrom/@SubLineNo", "xml:/OrderStatus/OrderLine/@SubLineNo")%>/>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/DerivedFrom/@OrderReleaseKey", "xml:/OrderStatus/@OrderReleaseKey")%>/>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/@ShipToKey", "xml:/OrderStatus/OrderLine/@ShipToKey")%>/>
				<input type="hidden" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_"+OrderStatusCounter+"/OrderLineTranQuantity/@TransactionalUOM", "xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@TransactionalUOM")%>/>
            </td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/Item/@ItemID"/></td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/Item/@ProductClass"/></td>
            <td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/OrderLine/OrderLineTranQuantity/@TransactionalUOM"/></td>

			<% long cntr=0; %>
            <yfc:loopXML name="OrderStatus" binding="xml:/OrderStatus/@OrderLine" id="OrderLine">
                <% cntr++; %>
                <% if (cntr==1) { %>
                    <td class="tablecolumn"><%=getLocalizedOrderLineDescription("OrderLine")%></td>
                <% } %>
            </yfc:loopXML>

			<td class="tablecolumn"><yfc:getXMLValue binding="xml:/OrderStatus/Schedule/@TagNumber"/></td>
            <td class="tablecolumn" sortValue="<%=getDateValue("xml:OrderStatus:/OrderStatus/Schedule/@ShipByDate")%>"><yfc:getXMLValue binding="xml:/OrderStatus/Schedule/@ShipByDate"/></td>
            <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:OrderStatus:/OrderStatus/OrderStatusTranQuantity/@StatusQty")%>"><yfc:getXMLValue binding="xml:/OrderStatus/OrderStatusTranQuantity/@StatusQty"/></td>
            <td nowrap="true" class="numerictablecolumn">
                <input type="text" class="numericunprotectedinput" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_" + OrderStatusCounter + "/OrderLineTranQuantity/@OrderedQty", "")%>/>
            </td>
            <td nowrap="true" class="tablecolumn">
				<select class="combobox" <%=getComboOptions(bindingNode+"OrderLines/OrderLine_" + OrderStatusCounter + "/@LineType","")%>>
                    <yfc:loopOptions binding="xml:LineTypeList:/OrderLineTypeList/@OrderLineType" name="LineTypeDesc" value="LineType" selected='<%=bindingNode+"OrderLines/OrderLine/@LineType"%>' isLocalized="Y"/>
				</select>
            </td>
            <td nowrap="true" class="tablecolumn">
				<select class="combobox" <%=getComboOptions(bindingNode+"OrderLines/OrderLine_" + OrderStatusCounter +"/@ReturnReason","")%>>
                    <yfc:loopOptions binding="xml:ReasonCodeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected='<%=bindingNode+"OrderLines/OrderLine/@ReturnReason"%>' isLocalized="Y"/>
				</select>
            </td>
            <td class="tablecolumn" nowrap="true">
                <input type="text" class="unprotectedinput" <%=getTextOptions(bindingNode+"OrderLines/OrderLine_" + OrderStatusCounter + "/@ShipNode", "xml:/OrderStatus/Schedule/@ShipNode")%>/>&nbsp;
				<img class="lookupicon" onclick="callLookup(this,'shipnode')" name="search" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node") %> />&nbsp;
            </td>
        </tr>
		<%}%>
    </yfc:loopXML>
            <input type="hidden" name="userHasOverridePermissions" value='<%=userHasOverridePermissions()%>'/>
            <input type="hidden" name="xml:/Order/@Override" value="<%=(userHasOverridePermissions()?"Y":"N")%>"/>
            <input type="hidden" name="xml:/OrderRelease/@Override" value="<%=(userHasOverridePermissions()?"Y":"N")%>"/>
			<input type="hidden" <%=getTextOptions("xml:/Order/@OrderHeaderKey", "")%> />
			<input type="hidden" <%=getTextOptions("xml:/OrderLineStatusList/OrderStatus/@OrderHeaderKey")%> />
			<input type="hidden" <%=getTextOptions("xml:/OrderRelease/@OrderReleaseKey", "")%> />
			<input type="hidden" name="NewBuyer" value="<%=getValue("OrderLineStatusList", "xml:/OrderLineStatusList/OrderStatus/OrderLine/Order/@BuyerOrganizationCode")%>" />
			<input type="hidden" name="NewSeller" value="<%=getValue("OrderLineStatusList", "xml:/OrderLineStatusList/OrderStatus/OrderLine/Order/@SellerOrganizationCode")%>" />
			<input type="hidden" name="NewCurrency" value="<%=getValue("OrderLineStatusList", "xml:/OrderLineStatusList/OrderStatus/OrderLine/Order/PriceInfo/@Currency")%>" />
</tbody>
</table>
