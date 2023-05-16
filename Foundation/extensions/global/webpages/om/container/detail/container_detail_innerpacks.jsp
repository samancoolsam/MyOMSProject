<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<%
	YFCElement container = (YFCElement) request.getAttribute("Container");
	String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");
	boolean outboundShipment = false;
	if(equals(sAppCode,"omd")){
       outboundShipment = true;
	}
	boolean showPackedQty = false;
	if(outboundShipment){
		if(!isVoid(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"))){
			if(!equals(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"),"0")){
				showPackedQty = true;
			}
		}
	}
%>
<table class="table" width="100%">
<thead>
    <tr> 
        <td class="tablecolumnheader" style="width:30px" sortable="no">
            <yfc:i18n>Tag_Details</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@ItemID")%>" sortable="no">
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@ProductClass")%>" sortable="no">
            <yfc:i18n>PC</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@UOM")%>" sortable="no">
            <yfc:i18n>UOM</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@ItemDesc")%>" sortable="no">
            <yfc:i18n>Description</yfc:i18n>
        </td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@InnerPackQuantity")%>" sortable="no">
            <yfc:i18n>Inner_Pack_Quantity</yfc:i18n>
        </td>
		
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/ContainerInnerPacks/ContainerInnerPack/@NoOfInnerPacks")%>" sortable="no">
            <yfc:i18n>No_Of_Inner_Packs</yfc:i18n>
        </td>		
    </tr>
</thead>
<tbody>
    <%String className="oddrow";%>
    <yfc:loopXML binding="xml:/ContainerInnerPacks/@ContainerInnerPack" id="ContainerInnerPack"  keyName="ContainerInnerPackKey">
   	<tr class='<%=className%>'>
		<%
			if (equals("oddrow",className))
				className="evenrow";
			else
				className="oddrow";													
		%>
	<td>
		<% 
			container.setAttribute("DummyItemID",getValue("ContainerInnerPack","xml:/ContainerInnerPack/@ItemID"));
			container.setAttribute("DummyProductClass",getValue("ContainerInnerPack","xml:/ContainerInnerPack/@ProductClass"));
			container.setAttribute("DummyUnitOfMeasure",getValue("ContainerInnerPack","xml:/ContainerInnerPack/@UnitOfMeasure"));
			container.setAttribute("DummyEnterpriseCode",resolveValue("xml:/Container/Shipment/@EnterpriseCode"));
%>
		<yfc:callAPI apiID="AP2"/>

<%
				//changes not required for RQ-WMS-158 as inner packs donot support serial capture.
				boolean  serialTracked=equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerializedFlag"));
				//boolean  serialTracked=(equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInShipping")) || equals("Y",getValue("ItemDetails","xml:/Item/PrimaryInformation/@SerialCapturedInReceiving")));
				boolean  tagControlled=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag")) ||equals("S",getValue("ItemDetails","xml:/Item/InventoryParameters/@TagControlFlag"));
				boolean  timeSensitive=equals("Y",getValue("ItemDetails","xml:/Item/InventoryParameters/@TimeSensitive"));

				if(tagControlled && !isVoid(resolveValue("xml:/Container/Shipment/@ShipNode"))){
					YFCElement tempItemElement = YFCDocument.parse("<Item ItemID=\"\" ItemKey=\"\" TagCapturedInShipping=\"\"><InventoryParameters TagControlFlag=\"\" IsSerialTracked=\"\" TimeSensitive=\"\"/><InventoryTagAttributes ItemTagKey=\"\" ItemKey=\"\" LotNumber=\"\" LotKeyReference=\"\" ManufacturingDate=\"\" LotExpirationDate=\"\" LotAttribute1=\"\" LotAttribute2=\"\" LotAttribute3=\"\" RevisionNo=\"\" BatchNo=\"\"><Extn/></InventoryTagAttributes> </Item>").getDocumentElement();
					YFCElement oItemDetailsElement = YFCDocument.createDocument("Item").getDocumentElement(); 
					oItemDetailsElement.setAttribute("CallingOrganizationCode",resolveValue("xml:/Container/Shipment/@EnterpriseCode"));
					oItemDetailsElement.setAttribute("ItemID",getValue("ContainerInnerPack","xml:/ContainerInnerPack/@ItemID"));
					oItemDetailsElement.setAttribute("Node",resolveValue("xml:/Container/Shipment/@ShipNode"));
					oItemDetailsElement.setAttribute("BuyerOrganization",resolveValue("xml:/Container/Shipment/@BuyerOrganizationCode"));
					oItemDetailsElement.setAttribute("UnitOfMeasure",getValue("ContainerInnerPack","xml:/ContainerInnerPack/@UnitOfMeasure")); 
%>
					<yfc:callAPI apiName="getNodeItemDetails" inputElement="<%=oItemDetailsElement%>" templateElement="<%=tempItemElement%>" outputNamespace="ItemDetails"/>
<%
					tagControlled = equals("Y",getValue("ItemDetails","xml:/Item/@TagCapturedInShipping")) ||equals("S",getValue("ItemDetails","xml:/Item/@TagCapturedInShipping"));
				}
				if(serialTracked||tagControlled||timeSensitive){					
%>
		  <img onclick="expandCollapseDetails('innerPackSet_<%=ContainerInnerPackCounter%>','<%=getI18N("Click_To_See_Tag_Info")%>','<%=getI18N("Click_To_Hide_Tag_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Tag_Info")%> />
	<%}%>
	</td>	
        
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerInnerPack" binding="xml:/ContainerInnerPack/@ItemID"/>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerInnerPack" binding="xml:/ContainerInnerPack/@ProductClass"/>
        </td>
        <td class="tablecolumn">
            <yfc:getXMLValue name="ContainerInnerPack" binding="xml:/ContainerInnerPack/@UnitOfMeasure"/>
        </td>
		<td class="tablecolumn">
            <yfc:getXMLValue name="ItemDetails" binding="xml:ItemDetails:/Item/PrimaryInformation/@Description"/>
        </td>
        <td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerInnerPacks:/ContainerInnerPack/@InnerPackQuantity")%>">
            <yfc:getXMLValue name="ContainerInnerPack" binding="xml:/ContainerInnerPack/@InnerPackQuantity"/>
        </td>
		<td class="numerictablecolumn" sortValue="<%=getNumericValue("xml:ContainerInnerPacks:/ContainerInnerPack/@NoOfInnerPacks")%>">
			<yfc:getXMLValue name="ContainerInnerPack" binding="xml:/ContainerInnerPack/@NoOfInnerPacks"/>
		</td>

      	<tr id='<%="innerPackSet_"+ContainerInnerPackCounter%>' class='<%=className%>' style="display:none">
			<td colspan="7" >
				<jsp:include page="/om/shipment/detail/shipment_detail_includeinnerpacktag.jsp" flush="true">
					<jsp:param name="optionSetBelongingToLine" value='<%=String.valueOf(ContainerInnerPackCounter)%>'/>
					<jsp:param name="Modifiable" value='false'/>
					<jsp:param name="LabelTDClass" value='detaillabel'/>
					<jsp:param name="TagContainer" value='ContainerInnerPacks'/>
					<jsp:param name="TagElement" value='ShipmentTagSerial'/>
					<jsp:param name="Source" value='InnerPack'/>
				</jsp:include>
			</td>
		</tr>
    </tr>
    </yfc:loopXML> 
</tbody>
</table>
 