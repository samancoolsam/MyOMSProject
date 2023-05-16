<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<table class="view" width="100%">
<% String itemId =  resolveValue("xml:/AdjustLocationInventory/@FromMenu");

if(isVoid(itemId)) { %>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Receipt_#</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/SummaryAttributes/Receipt/@ReceiptNo" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/Receipt/@ReceiptNo","xml:/NodeInventory/LocationInventoryList/LocationInventory/SummaryAttributes/Receipt/@ReceiptNo")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>FIFO_#</yfc:i18n>
		</td> 
		<td class="protectedtext">	<%=(int)getNumericValue("xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@FifoNo")%>
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@FifoNo","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@FifoNo")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Country_Of_Origin</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@CountryOfOrigin" />
			<input type="hidden"
			<%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@CountryOfOrigin","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@CountryOfOrigin")%> />
		</td>
	</tr>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Segment_Type</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@SegmentType" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@SegmentType","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@SegmentType")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Segment_#</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@Segment" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@Segment","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@Segment")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Ship_By_Date</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@ShipByDate" />
			<input type="hidden" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@ShipByDate","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/@ShipByDate")%> />
		</td>
	</tr>
<%}else {%>

	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Receipt_#</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" 
			<%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/Receipt/@ReceiptNo","")%> />
			<img class="lookupicon" onclick="callLookup(this,'receiptlookup','&xml:/Receipt/@ShowMultipleDocumentTypes=Y')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Receipt") %> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>FIFO_#</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@FifoNo","")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Country_Of_Origin</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" 
			<%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@CountryOfOrigin","")%> />
			<img class="lookupicon" onclick="callLookup(this,'countryoforigin')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Country_Of_Origin") %> />
		</td>
	</tr>
	<tr>
		<td class="detaillabel" >
			<yfc:i18n>Segment_Type</yfc:i18n>
		</td>
		<td nowrap="true">
			<select class="combobox" <%=getComboOptions("xml:/AdjustLocationInventory/Source/Inventory/@SegmentType")%>>
					<yfc:loopOptions binding="xml:SegmentType:/CommonCodeList/@CommonCode" 
						name="CodeShortDescription" value="CodeShortDescription" isLocalized="Y"  selected="xml:/AdjustLocationInventory/Source/Inventory/@SegmentType"/>
				</select>
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Segment_#</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@Segment","")%> />
		</td>
		<td class="detaillabel" >
			<yfc:i18n>Ship_By_Date</yfc:i18n>
		</td>
		<td nowrap="true">
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/@ShipByDate","")%> />
	        <img class="lookupicon" name="Calendar" onclick="invokeCalendar(this);return false;" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		</td>
	</tr>

	<script>
		var temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/Receipt/@ReceiptNo");
		if (temp!= null) { 
			temp.value = "";
		}
		temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/@FifoNo");
		if (temp!= null) { 
			temp.value = "";
		}
		temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/@CountryOfOrigin");
		if (temp!= null) { 
			temp.value = "";
		}
		temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/@SegmentType");
		if (temp!= null) { 
			temp.value = "";
		}
		temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/@Segment");
		if (temp!= null) { 
			temp.value = "";
		}
		temp = document.all("xml:/AdjustLocationInventory/Source/Inventory/@ShipByDate");
		if (temp!= null) { 
			temp.value = "";
		}
	</script>

<%}%>
</table>


