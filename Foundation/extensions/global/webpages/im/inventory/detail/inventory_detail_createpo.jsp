<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/orderentry.jspf"%>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
    window.doNotChangeNames=true;
</script>
<script language="Javascript" >
	yfcDoNotPromptForChanges(true);
</script>
<%

    // Default the enterprise code if it is not passed
    String enterpriseCode = (String) request.getParameter("xml:/Order/@EnterpriseCode");
    if (isVoid(enterpriseCode)) {
        enterpriseCode = getValue("CurrentOrganization", "xml:CurrentOrganization:/Organization/@PrimaryEnterpriseKey");
        request.setAttribute("xml:/Order/@EnterpriseCode", enterpriseCode);
    }
    
    prepareMasterDataElements(enterpriseCode, (YFCElement) request.getAttribute("OrganizationList"),
                            (YFCElement) request.getAttribute("EnterpriseParticipationList"),
                            (YFCElement) request.getAttribute("CurrencyList"),
                            (YFCElement) request.getAttribute("OrderTypeList"),
                            getValue("CurrentOrganization", "xml:CurrentOrganization:/Organization/@IsHubOrganization"));

    String orderHeaderKey = resolveValue("xml:/Order/@OrderHeaderKey");
%>

<script language="javascript">
<%
    if (!isVoid(orderHeaderKey)) {
        YFCDocument orderDoc = YFCDocument.createDocument("Order");
        orderDoc.getDocumentElement().setAttribute("OrderHeaderKey", resolveValue("xml:/Order/@OrderHeaderKey"));
        String keyString = orderDoc.getDocumentElement().getString(false);
        keyString = java.net.URLEncoder.encode(keyString);
%>
        function showPODetailPopup() {
            callPopupWithEntity('po', '<%=keyString%>','false');
        }

        function changeToPODetailView() {
            entityType = "po";
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
            <input type="hidden" name="xml:/Order/@DraftOrderFlag" value="Y"/>
            <input type="hidden" name="xml:/Order/@DocumentType" value="0005"/>
            <input type="hidden" name="xml:/Order/@EnteredBy" value="<%=resolveValue("xml:CurrentUser:/User/@Loginid")%>"/>
        </td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Enterprise</yfc:i18n></td>
        <td>
            <select class="combobox" onChange="updateCurrentView()" <%=getComboOptions("xml:/Order/@EnterpriseCode", HTMLEncode.htmlEscape(enterpriseCode))%>>
                <yfc:loopOptions binding="xml:/OrganizationList/@Organization" name="OrganizationCode"
                    value="OrganizationCode" selected="xml:/Order/@EnterpriseCode" targetBinding="xml:/Order/@EnterpriseCode"/>
            </select>
        </td>
        <td class="detaillabel" ><yfc:i18n>Buyer</yfc:i18n></td>
        <td nowrap="true" >
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@BuyerOrganizationCode","xml:/InventoryItem/@OrganizationCode")%>/>
            <img class="lookupicon" onclick="callLookup(this,'organization','xml:/Organization/OrgRoleList/OrgRole/@RoleKey=BUYER')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Buyer_Organization")%>/>
        </td>
    <tr>
    </tr>
        <td class="detaillabel" ><yfc:i18n>Seller</yfc:i18n></td>
        <td nowrap="true" >
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@SellerOrganizationCode")%>/>
            <img class="lookupicon" onclick="callLookup(this,'organization','xml:/Organization/OrgRoleList/OrgRole/@RoleKey=SELLER')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Seller_Organization")%>/>
        </td>
        <td class="detaillabel"><yfc:i18n>Order_#</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="detaillabel" ><yfc:i18n>Order_Type</yfc:i18n></td>
        <td>
            <select class="combobox" <%=getComboOptions("xml:/Order/@OrderType")%>>
                <yfc:loopOptions binding="xml:OrderTypeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Order/@OrderType" isLocalized="Y"/>
            </select>
        </td>
        <td class="detaillabel"><yfc:i18n>Order_Date</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderDate")%>/>
            <img class="lookupicon" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar")%>/>
        </td>
    </tr>
    <tr>
        <td class="detaillabel"><yfc:i18n>Order_Name</yfc:i18n></td>
        <td>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderName")%>/>
        </td>
        <td class="detaillabel" ><yfc:i18n>Currency</yfc:i18n></td>
        <td>
            <select class="combobox" <%=getComboOptions("xml:/Order/PriceInfo/@Currency")%>>
                <yfc:loopOptions binding="xml:/CurrencyList/@Currency" name="CurrencyDescription" value="Currency" selected="xml:/Order/PriceInfo/@Currency" isLocalized="Y"/>
            </select>
        </td>
    </tr>
</table>
