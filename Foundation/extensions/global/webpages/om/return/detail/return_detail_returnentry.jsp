<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/orderentry.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>
<%
    String modifyView = request.getParameter("ModifyView");
    boolean modify = "true".equals(modifyView);
	if (modifyView==null) modify = false;
	String sn = getValue("CurrentUser", "xml:CurrentUser:/User/@Node");
	boolean shipNodeUser = !isVoid(sn);
	String newReturn = resolveValue("xml:NewOrder:/Order/@OrderHeaderKey");

    String enterpriseCode = (String) request.getParameter("xml:/Order/@EnterpriseCode");
    if (isVoid(enterpriseCode)) {
        enterpriseCode = getValue("CurrentOrganization", "xml:CurrentOrganization:/Organization/@PrimaryEnterpriseKey");
        request.setAttribute("xml:/Order/@EnterpriseCode", enterpriseCode);
    }
    
%>
<script language="javascript">
<%    if (!isVoid(newReturn)) {	 
        YFCDocument orderDoc = YFCDocument.createDocument("Order");
        orderDoc.getDocumentElement().setAttribute("OrderHeaderKey",resolveValue("xml:NewOrder:/Order/@OrderHeaderKey"));
        String keyString = orderDoc.getDocumentElement().getString(false);
        keyString = java.net.URLEncoder.encode(keyString);
%>
        function showPODetailPopup() {
            callPopupWithEntity('return', '<%=keyString%>');
        }

        function changeToPODetailView() {
            entityType = "return";
            showDetailFor('<%=orderDoc.getDocumentElement().getString(false)%>');
        }
<% 
        if (equals(request.getParameter(YFCUIBackendConsts.YFC_IN_POPUP), "Y")) { %>
            window.attachEvent("onload", showPODetailPopup);
        <% }
        else { %>
            window.attachEvent("onload", changeToPODetailView);
        <% }
    }
%>
</script>

<table class="view" width="100%">
    <tr>
        <td>
            <input type="hidden" name="xml:/Order/@EnteredBy" value="<%=resolveValue("xml:CurrentUser:/User/@Loginid")%>"/>
            <input type="hidden" name="xml:/Order/@DraftOrderFlag" value="Y"/>
            <input type="hidden" name="xml:/Order/@CreatedByNode" value="<%=sn%>"/>
			<input type="hidden" name="xml:/Order/@CreatedAtNode" value="<%= (shipNodeUser ? "Y" : "N")%>"/>
			<input type="hidden" name="xml:/Order/@BillToKey" value="<%=getValue("Order", "xml:/Order/@BillToKey")%>"/>
			<input type="hidden" name="xml:/Order/@ShipToKey" value="<%=getValue("Order", "xml:/Order/@ShipToKey")%>"/>
			<input type="hidden" name="xml:/Order/PriceInfo/@ReportingConversionDate" value="<%=resolveValue("xml:/Order/PriceInfo/@ReportingConversionDate")%>"/>
        </td>			
    </tr>

	<% 	//cr 35413
		String isResetTrue = getParameter("ResetDetailPageDocumentType");
		if(isVoid(isResetTrue) )
			isResetTrue = "N";
	%>

    <% if (modify) { %>
        <jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
            <jsp:param name="ScreenType" value="detail"/>
            <jsp:param name="RefreshOnDocumentType" value="true"/>
            <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
            <jsp:param name="ApplicationCode" value="omr"/>
            <jsp:param name="ResetDocumentType" value='<%=isResetTrue%>'/>
        </jsp:include>
        <% // Now call the APIs that are dependent on the common fields (Doc Type & Enterprise Code)
           // Order Type is refreshed. %>
        <yfc:callAPI apiID="AP1"/>

    <% } else { %>
        <jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
            <jsp:param name="ScreenType" value="detail"/>
            <jsp:param name="RefreshOnDocumentType" value="true"/>
            <jsp:param name="ShowEnterpriseCode" value="false"/>
            <jsp:param name="ApplicationCode" value="omr"/>
            <jsp:param name="ResetDocumentType" value='<%=isResetTrue%>'/>
        </jsp:include>
        <% // Now call the APIs that are dependent on the common fields (Doc Type)
           // Order Type is refreshed. 
		%>
        <yfc:callAPI apiID="AP2"/>
    <% } %>

    <tr>
        <% if (!modify) { %>
            <td class="detaillabel"><yfc:i18n>Enterprise</yfc:i18n></td>
            <td class="protectedtext">
                <yfc:getXMLValue binding="xml:/Order/@EnterpriseCode" />
                <input type="hidden" <%=getTextOptions("xml:/Order/@EnterpriseCode")%> />
            </td>
		<% } %>

        <td class="detaillabel" ><yfc:i18n>Buyer</yfc:i18n></td>
		<% String enterpriseCodeForCommon = getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode");%>
		<% if (modify) { %>
        <td nowrap="true" >
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@BuyerOrganizationCode")%>/>			
            <img class="lookupicon" onclick="callLookupForOrder(this,'BUYER','<%=enterpriseCodeForCommon%>','xml:/Order/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
		<% } else { %>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@BuyerOrganizationCode"/></td>
            <input type="hidden" name="xml:/Order/@BuyerOrganizationCode" value="<%=getValue("Order", "xml:/Order/@BuyerOrganizationCode")%>"/>
		<% } %>

        <td class="detaillabel" ><yfc:i18n>Seller</yfc:i18n></td>
		<% if (modify) { %>
        <td nowrap="true" >
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@SellerOrganizationCode")%>/>
            <img class="lookupicon" onclick="callLookupForOrder(this,'SELLER','<%=enterpriseCodeForCommon%>','xml:/Order/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
		<% } else { %>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@SellerOrganizationCode"/></td>
            <input type="hidden" name="xml:/Order/@SellerOrganizationCode" value="<%=getValue("Order", "xml:/Order/@SellerOrganizationCode")%>"/>
		<% } %>

        <% if (modify) { %>
            <td/><td/>
        <% } %>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Order_#</yfc:i18n></td>
        <td><input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderNo","")%>/></td>

        <td class="detaillabel" ><yfc:i18n>Order_Date</yfc:i18n></td>
        <td nowrap="true">
			<input class="dateinput" type="text" <%=getTextOptions("xml:/Order/@OrderDate_YFCDATE", getTodayDate())%>/>
			<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
			<input class="dateinput" type="text" <%=getTextOptions("xml:/Order/@OrderDate_YFCTIME", getCurrentTime())%>/>
			<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
        </td>

        <td class="detaillabel" ><yfc:i18n>Order_Type</yfc:i18n></td>
		<td>
            <select class="combobox" <%=getComboOptions("xml:/Order/@OrderType")%>>
                <yfc:loopOptions binding="xml:OrderTypeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" isLocalized="Y"/>
            </select>
        </td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Currency</yfc:i18n></td>
		<% if (modify) { %>
        <td>
            <select class="combobox" <%=getComboOptions("xml:/Order/PriceInfo/@Currency")%>>
                <yfc:loopOptions binding="xml:/CurrencyList/@Currency" name="CurrencyDescription" value="Currency" selected="xml:/Order/PriceInfo/@Currency" isLocalized="Y"/>
            </select>
        </td>
		<% } else { %>
	        <td class="protectedtext">
				<%=getComboText("xml:CurrencyList:/CurrencyList/@Currency" ,"CurrencyDescription" ,"Currency" ,"xml:/Order/PriceInfo/@Currency",true)%>
			</td>
            <input type="hidden" name="xml:/Order/PriceInfo/@Currency" value="<%=getValue("Order", "xml:/Order/PriceInfo/@Currency")%>"/>
		<% } %>

		<td class="detaillabel" ><yfc:i18n>Return_Against_Order</yfc:i18n></td>
		<% if (modify) { %>
	        <td class="protectedtext"><yfc:i18n><%=displayFlagAttribute("N")%></yfc:i18n></td>
		<% } else { %>
	        <td class="protectedtext"><yfc:i18n><%=displayFlagAttribute("Y")%></yfc:i18n></td>
		<% } %>

		<td class="detaillabel" ><yfc:i18n>Ship_Node</yfc:i18n></td>
		<% if (modify) {%>
            <td nowrap="true" >
                <%if (shipNodeUser) {%>
                    <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@ShipNode", "xml:/Order/@ShipNode", "xml:CurrentUser:/User/@ShipNode")%> />
                <%} else {%>
                    <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@ShipNode", "xml:/Order/@ShipNode")%> />
                <%}%>
                <img class="lookupicon" onclick="callLookup(this,'shipnode')" name="search" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node") %> />&nbsp;
            </td>
		<%} else {%>
	        <td class="protectedtext"><yfc:getXMLValue binding="xml:/Order/@ShipNode"/></td>
		<%}%>
    </tr>
    <tr>
		<td class="detaillabel" ><yfc:i18n>Return_By_Gift_Recipient</yfc:i18n></td>
			<td>
				<input class="checkbox" type="checkbox" <%=getCheckBoxOptions("xml:/Order/@ReturnByGiftRecipient", " xml:/Order/@ReturnByGiftRecipient","Y")%> onClick="setReturnAddressKeys();"/>
		</td>
        <td colspan="4">&nbsp;</td>
    </tr>
</table>
