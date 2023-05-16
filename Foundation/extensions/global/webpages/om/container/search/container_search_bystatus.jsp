<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<SCRIPT LANGUAGE="JavaScript">
function disableOuterMostContainers(){
	changeSearchView(getCurrentSearchViewId());  
}
</SCRIPT>
<table class="view">
    <jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ShowDocumentType" value="true"/>
		<jsp:param name="DocumentTypeBinding" value="0006"/>
		<jsp:param name="ShowEnterpriseCode" value="true"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="NodeBinding" value="xml:yfcSearchCriteria:/Container/Shipment/@ShipNode"/>
	  <jsp:param name="EnterpriseCodeBinding" value="Academy_Direct"/>
	  <jsp:param name="RefreshOnEnterpriseCode" value="true"/>

	</jsp:include>
    <yfc:callAPI apiID="AP4"/>
    <tr>
		<input type="hidden" name="xml:/Container/@StatusQryType" value="BETWEEN"/>
        <td class="searchlabel" >
            <yfc:i18n>Shipment_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
			 <select name="xml:/Container/Shipment/@ShipmentNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/Shipment/@ShipmentNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/Shipment/@ShipmentNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Container_#</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Container/@ContainerNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/@ContainerNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/@ContainerNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Container_Type</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Container/@ContainerType" class="combobox">
                <yfc:loopOptions binding="xml:LPNTypeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Container/@ContainerType" isLocalized="Y"/>
            </select>
        </td>
    </tr>
	<tr>
		<td>
			<fieldset>
			<legend><yfc:i18n>Container_Contains</yfc:i18n></legend> 
				<table class="view" height="100%" width="100%" >
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" <%=getRadioOptions("xml:/Container/@ContainsStdQty", "xml:/Container/@ContainsStdQty", "Y" )%>><yfc:i18n>Standard_Quantity</yfc:i18n></input>
						</td>
					</tr>
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" <%=getRadioOptions("xml:/Container/@ContainsStdQty", "xml:/Container/@ContainsStdQty", "N" )%>><yfc:i18n>Loose_Quantity</yfc:i18n></input>
						</td>
					</tr>
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" <%=getRadioOptions("xml:/Container/@ContainsStdQty", " ", " " )%>><yfc:i18n>Any</yfc:i18n></input>
						</td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Status</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true">
            <select name="xml:/Container/@FromStatus" class="combobox">
                <yfc:loopOptions binding="xml:/StatusList/@Status" name="Description" value="Status" selected="xml:/Container/@FromStatus" isLocalized="Y"/>
            </select>
            <span class="searchlabel" ><yfc:i18n>To</yfc:i18n></span>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Container/@ToStatus" class="combobox">
                <yfc:loopOptions binding="xml:/StatusList/@Status" name="Description" value="Status" selected="xml:/Container/@ToStatus" isLocalized="Y"/>
            </select>
        </td>
    </tr>
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Carrier_Service</yfc:i18n>
        </td>
    </tr>
    <tr>
		<td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Container/Shipment/ScacAndService/@ScacAndServiceKey" class="combobox">
				<yfc:loopOptions binding="xml:ScacAndServiceList:/ScacAndServiceList/@ScacAndService" name="ScacAndServiceDesc" value="ScacAndServiceKey" selected="xml:/Container/Shipment/ScacAndService/@ScacAndServiceKey" isLocalized="Y"/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Container_Group</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Container/@ContainerGroup" class="combobox">
                <yfc:loopOptions binding="xml:ContainerGroupList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Container/@ContainerGroup" isLocalized="Y"/>
            </select>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Wave_#</yfc:i18n>
        </td>
    </tr>
	<tr>
	    <td nowrap="true" class="searchcriteriacell">
			 <select name="xml:/Container/Shipment/ShipmentLines/ShipmentLine/@WaveNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/Shipment/ShipmentLines/ShipmentLine/@WaveNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/Shipment/ShipmentLines/ShipmentLine/@WaveNo")%>/>
        </td>
    </tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" <%=getCheckBoxOptions("xml:/Container/@OutermostContainersOnly", "xml:/Container/@OutermostContainersOnly", "Y")%>><yfc:i18n>Outermost_Containers_only</yfc:i18n></input>	
		</td>
	</tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" onClick="changeSearchView(getCurrentSearchViewId());"  <%=getCheckBoxOptions("xml:/Container/@ManifestKeyQryType","xml:/Container/@ManifestKeyQryType","VOID")%> yfcCheckedValue='VOID' yfcUnCheckedValue='' ><yfc:i18n>Unmanifested_Containers_only</yfc:i18n></input>	
		</td>
	</tr>
</table>
<SCRIPT LANGUAGE="JavaScript">
if(document.all("xml:/Container/@ManifestKeyQryType")){
	if(document.all("xml:/Container/@ManifestKeyQryType").checked){
		document.all("xml:/Container/@OutermostContainersOnly").disabled = true;
		document.all("xml:/Container/@OutermostContainersOnly").checked = true;
	}else{
		document.all("xml:/Container/@OutermostContainersOnly").disabled = false;
	}
}
</SCRIPT>
