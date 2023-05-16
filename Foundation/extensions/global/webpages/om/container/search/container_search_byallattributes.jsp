<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" >
function updateQryType(){
    if(document.all("xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNo").value == 1){
        document.all("xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNoQryType").value ="GE";
    } else{
        document.all("xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNoQryType").value ="";
    }
}
</script>
<table class="view">
	<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ShowDocumentType" value="true"/>
		<jsp:param name="DocumentTypeBinding" value="0006"/>
		<jsp:param name="ShowEnterpriseCode" value="false"/>
		<jsp:param name="ShowNode" value="false"/>
    </jsp:include>
    <tr>
		<td class="searchlabel" >
			<yfc:i18n>Order_#</yfc:i18n>
		</td>
	</tr>
	<tr>
		<td nowrap="true" class="searchcriteriacell">
			 <select name="xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@OrderNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@OrderNoQryType"/>
            </select>
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@OrderNo")%>/>
		</td>
	</tr>
    <tr>
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
                <yfc:loopOptions binding="xml:LPNTypeList:/CommonCodeList/@CommonCode" name="CodeShortDescription"    value="CodeValue" selected="xml:/Container/@ContainerType" isLocalized="Y"/>
            </select>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Tracking_#</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Container/@TrackingNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/@TrackingNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/@TrackingNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
			 <select name="xml:/Container/ContainerDetails/ContainerDetail/@ItemIDQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/ContainerDetails/ContainerDetail/@ItemIDQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/ContainerDetails/ContainerDetail/@ItemID")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Container_SCM</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Container/@ContainerScmQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/@ContainerScmQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/@ContainerScm")%>/>

				<%//If passed populate Load Key.  This is used when linked from Load List View. %>
				<input type="hidden" <%=getTextOptions("xml:yfcSearchCriteria:/Container/@LoadKey")%>/>
				<input type="hidden" <%=getTextOptions("xml:yfcSearchCriteria:/Container/@FromLoadConsole")%>/>
				<input type="hidden" <%=getTextOptions("xml:yfcSearchCriteria:/Container/@OutermostContainersOnly")%>/>
				<input type="hidden" <%=getTextOptions("xml:yfcSearchCriteria:/Container/@GetLoadAndLoadShipmentContainers")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel">
            <yfc:i18n>Tote_Id</yfc:i18n>
        </td>
    </tr>
	<tr>
        <td nowrap="true" class="searchcriteriacell">
			<select name="xml:/Container/Tote/@ToteIdQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Container/Tote/@ToteIdQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Container/Tote/@ToteId")%>/>
        </td>
    </tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" yfcCheckedValue="Y" yfcUnCheckedValue=" " <%=getCheckBoxOptions("xml:/Container/@IsHazmat", "xml:/Container/@IsHazmat", "Y")%>><yfc:i18n>Has_Hazardous_Item(s)</yfc:i18n></input>
		</td>
	</tr>
	<tr>
    <td class="searchcriteriacell" nowrap="true">
        <input type="checkbox" onclick="updateQryType();" <%=getCheckBoxOptions("xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNo", "xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNo", "1")%> yfcCheckedValue='1' yfcUnCheckedValue=''><yfc:i18n>Containers_With_Logical_Kits_Only</yfc:i18n></input>
    </td>

	<input type="hidden" name="xml:/Container/ContainerDetails/ContainerDetail/ShipmentLine/@ShipmentSubLineNoQryType" value=""/>
</tr>
</table>
