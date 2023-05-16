<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<table class="view">
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
  <jsp:param name="ShowDocumentType" value="true"/>
  <jsp:param name="DocumentTypeBinding" value="0006"/>
  <jsp:param name="EnterpriseCodeBinding" value="Academy_Direct"/>
  <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
  <jsp:param name="ScreenType" value="search"/>
 </jsp:include>

    <yfc:callAPI apiID="AP4"/>
    <yfc:callAPI apiID="AP5"/>
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Shipment_Mode</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Shipment/@ShipMode" class="combobox">
                <yfc:loopOptions binding="xml:ShipmentModeList:/CommonCodeList/@CommonCode" name="CodeShortDescription"
                value="CodeValue" selected="xml:/Shipment/@ShipMode" isLocalized="Y"/>
            </select>
        </td>
    <tr>
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Carrier_Service</yfc:i18n>
        </td>
    </tr>
    <tr>
		<td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Shipment/ScacAndService/@ScacAndServiceKey" class="combobox">
				<yfc:loopOptions binding="xml:ScacAndServiceList:/ScacAndServiceList/@ScacAndService" name="ScacAndServiceDesc" value="ScacAndServiceKey" selected="xml:/ScacAndServiceList/ScacAndService/@ScacAndServiceKey" isLocalized="Y"/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>BOL_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@BolNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/@BolNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@BolNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Pro_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@ProNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/@ProNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@ProNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Trailer_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@TrailerNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/@TrailerNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@TrailerNo")%>/>
        </td>
    </tr>
    <yfc:callAPI apiID="AP6"/>
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
			<input type="checkbox" <%=getCheckBoxOptions("xml:/Shipment/@IsRoutingPending", "xml:/Shipment/@IsRoutingPending", "Y")%>><yfc:i18n>Requires_Routing</yfc:i18n></input>	
		</td>
	</tr>
	<%if((equals("omd",resolveValue("xml:/CurrentEntity/@ApplicationCode")))&&(isShipNodeUser())){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ShipNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}else if(isShipNodeUser()){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ReceivingNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}%>
	
</table>