<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<table class="view">
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
  <jsp:param name="ShowDocumentType" value="true"/>
  <jsp:param name="DocumentTypeBinding" value="0006"/>
  <jsp:param name="EnterpriseCodeBinding" value="Academy_Direct"/>
  <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
  <jsp:param name="ScreenType" value="search"/>
 </jsp:include>
    <yfc:callAPI apiID="AP3"/>
    <yfc:callAPI apiID="AP4"/>

    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/ShipmentLines/ShipmentLine/@ItemIDQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/ShipmentLines/ShipmentLine/@ItemIDQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/ShipmentLines/ShipmentLine/@ItemID")%>/>
			 <% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
            <img class="lookupicon" onclick="callItemLookup('xml:/Shipment/ShipmentLines/ShipmentLine/@ItemID','xml:/Shipment/ShipmentLines/ShipmentLine/@ProductClass','xml:/Shipment/ShipmentLines/ShipmentLine/@UnitOfMeasure','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item")%>/>
        </td>
    </tr>
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Product_Class</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Shipment/ShipmentLines/ShipmentLine/@ProductClass" class="combobox">
                <yfc:loopOptions binding="xml:ProductClassList:/CommonCodeList/@CommonCode" name="CodeValue"
                value="CodeValue" selected="xml:/Shipment/ShipmentLines/ShipmentLine/@ProductClass"/>
            </select>
        </td>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Unit_Of_Measure</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Shipment/ShipmentLines/ShipmentLine/@UnitOfMeasure" class="combobox">
                <yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure"
                value="UnitOfMeasure" selected="xml:/Shipment/ShipmentLines/ShipmentLine/@UnitOfMeasure"/>
            </select>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Buyer</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@BuyerOrganizationCodeQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/@BuyerOrganizationCodeQryType"/>
            </select>
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@BuyerOrganizationCode")%>/>
            <% String enterpriseCode = getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode");%>
            <img class="lookupicon" onclick="callLookupForOrder(this,'BUYER','<%=enterpriseCode%>','xml:/Shipment/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Seller</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Shipment/@SellerOrganizationCodeQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/@SellerOrganizationCodeQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@SellerOrganizationCode")%>/>
            <img class="lookupicon" onclick="callLookupForOrder(this,'SELLER','<%=enterpriseCode%>','xml:/Shipment/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
    </tr>
    <yfc:callAPI apiID="AP5"/>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Status</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true">
            <select name="xml:/Shipment/@Status" class="combobox">
                <yfc:loopOptions binding="xml:/StatusList/@Status" name="Description" value="Status" selected="xml:/Shipment/@Status" isLocalized="Y"/>
            </select>
        </td>
    </tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" yfcCheckedValue=" " yfcUnCheckedValue="N" <%=getCheckBoxOptions("xml:/Shipment/@ShipmentClosedFlag", "xml:/Shipment/@ShipmentClosedFlag", " ")%>><yfc:i18n>Include_Closed_Shipments</yfc:i18n></input>
		</td>
	</tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" yfcCheckedValue="Y" yfcUnCheckedValue=" " <%=getCheckBoxOptions("xml:/Shipment/@HazardousMaterialFlag", "xml:/Shipment/@HazardousMaterialFlag", "Y")%>><yfc:i18n>Has_Hazardous_Item(s)</yfc:i18n></input>
		</td>
	</tr>
	<%if((equals("omd",resolveValue("xml:/CurrentEntity/@ApplicationCode")))&&(isShipNodeUser())){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ShipNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}else if(isShipNodeUser()){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ReceivingNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}%>
</table>