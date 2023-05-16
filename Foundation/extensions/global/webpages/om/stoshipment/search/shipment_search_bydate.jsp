<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript">
window.attachEvent("onload",showHidePopups);
function showHidePopups()
{
	showHideDates('Shipment');
	showHideDates('Delivery');
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
        <td>
            <input type="hidden" name="xml:/Shipment/@RequestedShipmentDateQryType" value="BETWEEN"/>
            <input type="hidden" name="xml:/Shipment/@RequestedDeliveryDateQryType" value="BETWEEN"/>
            <input type="hidden" name="xml:/Shipment/@ExpectedShipmentDateQryType" value="BETWEEN"/>
            <input type="hidden" name="xml:/Shipment/@ExpectedDeliveryDateQryType" value="BETWEEN"/>
			<input type="hidden" name="xml:/Shipment/@ActualShipmentDateQryType" value="BETWEEN"/>
			<input type="hidden" name="xml:/Shipment/@ActualDeliveryDateQryType" value="BETWEEN"/>
        </td>
    </tr>    
    <yfc:callAPI apiID="AP5"/>
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
	<tr>
		<td class="searchcriteriacell">
			<input id="ShipmentDateCheckBox" <%=getCheckBoxOptions("xml:/Shipment/@ShipmentDateCheckBox","xml:/Shipment/@ShipmentDateCheckBox","Y") %>  type="checkbox"  onclick="showHideDates('Shipment');" ><yfc:i18n>Enter_Shipment_Dates</yfc:i18n></input>	
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
			<input id="DeliveryDateCheckBox" <%=getCheckBoxOptions("xml:/Shipment/@DeliveryDateCheckBox","xml:/Shipment/@DeliveryDateCheckBox","Y") %> type="checkbox" onclick="showHideDates('Delivery');" ><yfc:i18n>Enter_Delivery_Dates</yfc:i18n></input>	
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
</table>