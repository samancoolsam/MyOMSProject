<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript">
	function showIgnoreCancelledLines(obj){
		if (obj=='NotInWave'){
			var checkBox=document.all("IgnoreCancelledLines");
			checkBox.style.display="";	
		}else{
			var checkBox=document.all("IgnoreCancelledLines");
			checkBox.style.display="none";
		}
	}
</script>

<table class="view">
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
  <jsp:param name="ShowDocumentType" value="true"/>
  <jsp:param name="DocumentTypeBinding" value="0006"/>
  <jsp:param name="EnterpriseCodeBinding" value="Academy_Direct"/>
  <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
  <jsp:param name="ScreenType" value="search"/>
 </jsp:include>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Shipment_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/@ShipmentNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Shipment/@ShipmentNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@ShipmentNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Wave_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/ShipmentLines/ShipmentLine/@WaveNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/ShipmentLines/ShipmentLine/@WaveNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/ShipmentLines/ShipmentLine/@WaveNo")%>/>
        </td>
    </tr>
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Load_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Shipment/LoadShipments/LoadShipment/Load/@LoadNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                    value="QueryType" selected="xml:/Shipment/LoadShipments/LoadShipment/Load/@LoadNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/LoadShipments/LoadShipment/Load/@LoadNo")%>/>
        </td>
    </tr>
    <yfc:callAPI apiID="AP3"/>
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
            <yfc:i18n>Buyer</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Shipment/@BuyerOrganizationCode")%>/>
            <% String enterpriseCode = getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode");%>
            <img class="lookupicon" onclick="callLookupForOrder(this,'BUYER','<%=enterpriseCode%>','xml:/Shipment/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
    </tr>
    <yfc:callAPI apiID="AP4"/>
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
	<tr/>
	<tr>
		<td>
			<fieldset>
				<table class="view" height="100%" width="100%" >
				<tr>
				   <yfc:i18n>Selection_Is_Valid_If_No_Wave_Entered</yfc:i18n>
				</tr>
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" onclick="showIgnoreCancelledLines('');" <%=getRadioOptions("xml:/Shipment/@IsIncludedInWave", "xml:/Shipment/@IsIncludedInWave", "Y" )%>><yfc:i18n>Shipment_In_Wave</yfc:i18n></input>
						</td>
					</tr>
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" onclick="showIgnoreCancelledLines('NotInWave');" <%=getRadioOptions("xml:/Shipment/@IsIncludedInWave", "xml:/Shipment/@IsIncludedInWave", "N" )%>><yfc:i18n>Shipment_Not_In_Wave</yfc:i18n></input>
						</td>
						<td id='IgnoreCancelledLines' class="searchcriteriacell" style="display:none">
							<input type="checkbox" yfcCheckedValue="Y" yfcUnCheckedValue="N" <%=getCheckBoxOptions("xml:/Shipment/@IgnoreCancelledShipmentLines", "xml:/Shipment/@IgnoreCancelledShipmentLines", "Y")%>><yfc:i18n>Ignore_Cancelled_Shipment_Lines</yfc:i18n></input>
						</td>
					</tr>
					<tr>
						<td class="searchcriteriacell">
							<input type="radio" onclick="showIgnoreCancelledLines('');" name="xml:/Shipment/@IsIncludedInWave" value=""
							<% 
							if (YFCCommon.isVoid(resolveValue("xml:/Shipment/@IsIncludedInWave"))) {%>
							CHECKED
							<%}%>
							><yfc:i18n>All</yfc:i18n></input>
						</td>
					</tr>
				</table>
			</fieldset>
		</td>
	</tr>
	<tr>
		<td class="searchcriteriacell">
			<input type="checkbox" yfcCheckedValue="Y" yfcUnCheckedValue=" " <%=getCheckBoxOptions("xml:/Shipment/@HasNodeExceptions", "xml:/Shipment/@HasNodeExceptions", "Y")%>><yfc:i18n>Has_Shortage</yfc:i18n></input>
		</td>
	</tr>
	<tr>
		<td class="searchcriteriacell">
			<input id="ShipmentDateCheckBox" type="checkbox" onclick="showHideDates('Shipment');" ><yfc:i18n>Enter_Shipment_Dates</yfc:i18n></input>	
		</td>
	</tr>
	<tr id='ShipmentDates' style="display:none">
		<td>
			<table class="view">
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Requested_Ship_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromRequestedShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromRequestedShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToRequestedShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToRequestedShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Expected_Ship_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromExpectedShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromExpectedShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToExpectedShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToExpectedShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Actual_Ship_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromActualShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromActualShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToActualShipmentDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToActualShipmentDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td class="searchcriteriacell">
			<input id="DeliveryDateCheckBox" type="checkbox" onclick="showHideDates('Delivery');" ><yfc:i18n>Enter_Delivery_Dates</yfc:i18n></input>	
		</td>
	</tr>
	<tr id='DeliveryDates' style="display:none">
		<td>
			<table class="view">
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Requested_Delivery_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromRequestedDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromRequestedDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToRequestedDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToRequestedDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Expected_Delivery_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromExpectedDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromExpectedDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToExpectedDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToExpectedDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
				<tr>
					<td class="searchlabel" >
						<yfc:i18n>Actual_Delivery_Date</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td nowrap="true">
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromActualDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@FromActualDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
						<yfc:i18n>To</yfc:i18n>
					</td>
				</tr>
				<tr>
					<td>
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToActualDeliveryDate_YFCDATE")%>/>
						<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
						<input class="dateinput" type="text" <%=getTextOptions("xml:/Shipment/@ToActualDeliveryDate_YFCTIME")%>/>
						<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" 
						<%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<%if((equals("omd",resolveValue("xml:/CurrentEntity/@ApplicationCode")))&&(isShipNodeUser())){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ShipNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}else if(isShipNodeUser()){%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@ReceivingNode", "xml:CurrentUser:/User/@Node")%>/>
	<%}%>
		<input type="hidden" <%=getTextOptions("xml:/Shipment/@SCAC")%>/>
</table>
<%
if("N".equals(resolveValue("xml:/Shipment/@IsIncludedInWave"))){
%>
	<script language="javascript">
		var checkBox=document.all("IgnoreCancelledLines");
		checkBox.style.display="";	
	</script>
<%
}
%>