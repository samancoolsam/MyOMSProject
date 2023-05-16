<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@include file="/console/jsp/orderentry.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ include file="/yfsjspcommon/editable_util_header.jspf" %>


<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/modificationreason.js"></script> 
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<%  String sHiddenDraftOrderFlag = getValue("Order", "xml:/Order/@DraftOrderFlag");
	String DocumentType = getValue("Order", "xml:/Order/@DocumentType");
    String driverDate = getValue("Order", "xml:/Order/@DriverDate");
	String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("Order", "xml:/Order/@EnterpriseCode"));
	extraParams += "&" + getExtraParamsForTargetBinding("xml:/Order/@OrderHeaderKey", resolveValue("xml:/Order/@OrderHeaderKey"));
	extraParams += "&" + getExtraParamsForTargetBinding("IsStandaloneService", "Y");
	extraParams += "&" + getExtraParamsForTargetBinding("hiddenDraftOrderFlag", sHiddenDraftOrderFlag);
	extraParams += "&" + getExtraParamsForTargetBinding("DocumentType", DocumentType);
	
%>

<script language="javascript">
	// this method is used by 'Add Service Request' action on order header detail innerpanel
	function callPSItemLookup()	{
		yfcShowSearchPopupWithParams('','itemlookup',900,550,new Object(), 'psItemLookup', '<%=extraParams%>');
	}
</script>
<%
String sRequestDOM = request.getParameter("getRequestDOM");
boolean	modify = "true".equals(request.getParameter("ModifyView"));
String hdl = getValue("Order", "xml:/Order/@HasDerivedLines");
if ("Y".equals(hdl) || "true".equals(hdl))
	modify=false;

//for buyer, seller and currency only
boolean	modifyFlag = "true".equals(request.getParameter("ModifyView"));
String hasDerivedParent = getValue("Order", "xml:/Order/@HasDerivedParent");
if ("Y".equals(hasDerivedParent) || "true".equals(hasDerivedParent))
	modifyFlag=false;


//check if there is any exchange order created for the return.
boolean hasNoExchange = true;
YFCElement elem = getElement("Order");
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

//MNI-17789 : Return Tracking Number in DOM Console
String strFedexUrl = YFCConfigurator.getInstance().getProperty("academy.return.tracking.fedex.url");
%>

<table class="view" width="100%">
    <yfc:makeXMLInput name="orderKey">
        <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/@OrderHeaderKey" ></yfc:makeXMLKey>
    </yfc:makeXMLInput>
    <yfc:makeXMLInput name="returnKeyForExchange">
        <yfc:makeXMLKey binding="xml:/ReturnOrder/@ReturnOrderHeaderKeyForExchange" value="xml:/Order/@OrderHeaderKey" ></yfc:makeXMLKey>
    </yfc:makeXMLInput>
    <tr>
        <td>
            <input type="hidden" <%=getTextOptions("xml:/Order/@ModificationReasonCode","")%> />
            <input type="hidden" <%=getTextOptions("xml:/Order/@ModificationReasonText","")%>/>
			<input type="hidden" name="userHasOverridePermissions" value='<%=userHasOverridePermissions()%>'/>	<%-- cr 36191 --%>
            <input type="hidden" name="xml:/Order/@Override" value="N"/>
            <input type="hidden" name="TempHeaderKey" value='<%=getValue("Order", "xml:/Order/@OrderHeaderKey")%>'/>
            <input type="hidden" name="TempBuyer" value='<%=getValue("Order", "xml:/Order/@BuyerOrganizationCode")%>'/>
            <input type="hidden" name="TempSeller" value='<%=getValue("Order", "xml:/Order/@SellerOrganizationCode")%>'/>
            <input type="hidden" name="TempCurrency" value='<%=getValue("Order", "xml:/Order/PriceInfo/@Currency")%>'/>
            <input type="hidden" name="xml:/AllocateOrder/@RuleID" value='<%=getValue("Order", "xml:/Order/@AllocationRuleID")%>'/>
            <input type="hidden" name="xml:/AllocateOrder/@IgnoreAllocateDate" value='N'/>
            <input type="hidden" name="hiddenDraftOrderFlag" value='<%=getValue("Order", "xml:/Order/@DraftOrderFlag")%>'/>
            <input type="hidden" name="chkExchangeEntityKey" value='<%=getParameter("returnKeyForExchange")%>'/>
			<input type="hidden" name="xml:/Order/@EnterpriseCode" value='<%=getValue("Order", "xml:/Order/@EnterpriseCode")%>'/>
            <input type="hidden" name="WorkOrderEntityKey" value='<%=getParameter("orderKey")%>'/>
        </td>
    </tr>

    <tr>
		<td class="detaillabel" ><yfc:i18n>Enterprise</yfc:i18n></td>
        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@EnterpriseCode"/></td>
        <td class="detaillabel" ><yfc:i18n>Buyer</yfc:i18n></td>
		<%if (isModificationAllowed("xml:/Order/@BuyerOrganizationCode","xml:/Order/AllowedModifications") && modifyFlag) { %>
        <td nowrap="true" >
            <input type="text" <%if (equals(sRequestDOM,"Y")) {%> OldValue="<%=resolveValue("xml:OrigAPIOutput:/Order/@BuyerOrganizationCode")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/@BuyerOrganizationCode", "xml:/Order/AllowedModifications")%>/>
            <img class="lookupicon" onclick="callLookupForOrder(this,'BUYER','xml:/Order/@EnterpriseCode','<%=resolveValue("xml:DocumentParamsList:/DocumentParamsList/DocumentParams/@DocumentType")%>')" <%=yfsGetImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization", "xml:/Order/@BuyerOrganizationCode", "xml:/Order/AllowedModifications")%>/>
        </td>
		<%} else {%>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@BuyerOrganizationCode"/></td>
		<%}%>
        <td class="detaillabel" ><yfc:i18n>Seller</yfc:i18n></td>
		<%if (isModificationAllowed("xml:/Order/@SellerOrganizationCode","xml:/Order/AllowedModifications") && modifyFlag) { %>
        <td nowrap="true" >
            <input type="text" <%if (equals(sRequestDOM,"Y")) {%> OldValue="<%=resolveValue("xml:OrigAPIOutput:/Order/@SellerOrganizationCode")%>"  <%}%> <%=yfsGetTextOptions("xml:/Order/@SellerOrganizationCode", "xml:/Order/AllowedModifications")%>/>
            <img class="lookupicon" onclick="callLookupForOrder(this,'SELLER','xml:/Order/@EnterpriseCode','<%=resolveValue("xml:DocumentParamsList:/DocumentParamsList/DocumentParams/@DocumentType")%>')" <%=yfsGetImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization", "xml:/Order/@SellerOrganizationCode", "xml:/Order/AllowedModifications")%>/>
        </td>
		<%} else {%>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@SellerOrganizationCode"/></td>
		<%}%>
    </tr>
        <td class="detaillabel" ><yfc:i18n>Order_#</yfc:i18n></td>
        <td class="protectedtext"><a <%=getDetailHrefOptions("L06",getParameter("orderKey"),"")%> ><yfc:getXMLValue binding="xml:/Order/@OrderNo"/></a>
			<%	if(isTrue("xml:/Order/@ReturnByGiftRecipient") )	{	%>
				<img class="lookupicon" <%=getImageOptions(request.getContextPath() + "/console/icons/gift.gif", "Return_By_Gift_Recipient")%>/>
			<%	}	%>
		</td>
        <td class="detaillabel" ><yfc:i18n>Status</yfc:i18n></td>
        <td class="protectedtext">
		<% if (isVoid(getValue("Order", "xml:/Order/@Status"))) {%>
			[<yfc:i18n>Draft</yfc:i18n>]
			<% } else { %>
				<a <%=getDetailHrefOptions("L01", getParameter("orderKey"), "ShowReleaseNo=Y")%>><%=displayOrderStatus(getValue("Order","xml:/Order/@MultipleStatusesExist"),getValue("Order","xml:/Order/@MaxOrderStatusDesc"),true)%></a>
			<% } %>
            <% if (equals("Y", getValue("Order", "xml:/Order/@HoldFlag"))) { %>

	            <% if (isTrue("xml:/Rules/@RuleSetValue")) {%>
					<img onmouseover="this.style.cursor='default'" class="columnicon" <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "This_order_is_held")%>>
				<%	}	else	{	%>
					<a <%=getDetailHrefOptions("L05", getParameter("orderKey"), "")%>><img class="columnicon" <%=getImageOptions(YFSUIBackendConsts.HELD_ORDER, "This_order_is_held\nclick_to_add/remove_hold")%>></a>
				<%	}	%>

            <% } %>

            <% if (equals("Y", getValue("Order", "xml:/Order/@isHistory"))){ %>
                <img class="icon" onmouseover="this.style.cursor='default'" <%=getImageOptions(YFSUIBackendConsts.HISTORY_ORDER, "This_is_an_archived_order")%>/>
            <% } %>
        </td>
        <td class="detaillabel" ><yfc:i18n>Order_Date</yfc:i18n></td>
		<% if (modify) {%>
        <td nowrap="true">
            <input class="dateinput" type="text" <%if (equals(sRequestDOM,"Y")) {%> OldValue="<%=resolveValue("xml:OrigAPIOutput:/Order/@OrderDate_YFCDATE")%>"  <%}%>  <%=getTextOptions("xml:/Order/@OrderDate_YFCDATE")%>/>
            <img class="lookupicon" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar")%>/>
			<input class="dateinput" type="text" <%if (equals(sRequestDOM,"Y")) {%> OldValue="<%=resolveValue("xml:OrigAPIOutput:/Order/@OrderDate_YFCTIME")%>"  <%}%> <%=getTextOptions("xml:/Order/@OrderDate_YFCTIME")%>/>
			<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>

        </td>
		<%} else {%>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@OrderDate"/></td>
		<%}%>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Order_Type</yfc:i18n></td>
		<% if (modify) {%>
        <td>
            <select name="xml:/Order/@OrderType" class="combobox">
                <yfc:loopOptions binding="xml:OrderTypeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Order/@OrderType" isLocalized="Y"/>
            </select>
        </td>
		<%} else {%>
	        <td class="protectedtext"><%=getComboText("xml:OrderTypeList:/CommonCodeList/@CommonCode" ,"CodeShortDescription" ,"CodeValue" ,"xml:/Order/@OrderType",true)%></td>
	<%}%>
        <td class="detaillabel" ><yfc:i18n>Currency</yfc:i18n></td>
		<% if (modifyFlag) {%>
        <td>
            <select <%=yfsGetComboOptions("xml:/Order/PriceInfo/@Currency", "xml:/Order/AllowedModifications")%>>
                <yfc:loopOptions binding="xml:GetCurrencyList:/CurrencyList/@Currency" name="CurrencyDescription" value="Currency" selected="xml:/Order/PriceInfo/@Currency" isLocalized="Y"/>
            </select>
        </td>
		<%} else {%>
	        <td class="protectedtext">
				<yfc:getXMLValueI18NDB name="ReturnCurrencyList" binding="xml:/CurrencyList/Currency/@CurrencyDescription"/>
			</td>
		<%}%>
		<td class="detaillabel" ><yfc:i18n>Return_Against_Order</yfc:i18n></td>
			<% String s="N"; %>
	        <td class="protectedtext">
			<%if (isTrue("xml:/Order/@HasDerivedParent")) { s="Y"; } %>
				<yfc:i18n><%=displayFlagAttribute(s)%></yfc:i18n>
			</td>
	</tr>
	<tr>

	<% if(equals("true", request.getParameter("ShowDocumentType") ) )	{	%>
		<td class="detaillabel" >
			<yfc:i18n>Document_Type</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValueI18NDB binding="xml:DocumentParamsList:/DocumentParamsList/DocumentParams/@Description"></yfc:getXMLValueI18NDB>
		</td>
	<% } %>
	<% if(!hasNoExchange){	%>
		<yfc:makeXMLInput name="ExchangeOrderForReturn">
				<yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Order/ExchangeOrders/ExchangeOrder/@OrderHeaderKey" />
		</yfc:makeXMLInput>
		<td class="detaillabel" >
			<yfc:i18n>Exchange_Order_Created_For_Return</yfc:i18n>
		</td>
		<td class="protectedtext">
			<a <%=getDetailHrefOptions("L02",getParameter("ExchangeOrderForReturn"),"")%> >
				<yfc:getXMLValue binding="xml:/Order/ExchangeOrders/ExchangeOrder/@OrderNo"/>
			</a> 
		</td>
	<% } %>
	
	<!-- OMNI-17789: Start-->
	<td class="detaillabel" >
		<yfc:i18n>TrackingNo</yfc:i18n>
	</td>
	<td class="protectedtext">		
		<a href="<%=strFedexUrl%><yfc:getXMLValue binding='xml:/Order/Extn/@ExtnTrackingNo'/>" target="_blank">
			<yfc:getXMLValue binding="xml:/Order/Extn/@ExtnTrackingNo"/>
		</a>
	</td>
	<!-- OMNI-17789: End-->
	
	</tr>
</table>
