<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
/*
    Modification Comments:
          1. 5/28/2004
              - Minor bug to remove the saved serial numbers from the popup. No Upgrade Impact.
*/
%>
<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<%
String itemId =  resolveValue("xml:/AdjustLocationInventory/@FromMenu");
String caseId =  resolveValue("xml:/NodeInventory/Inventory/@CaseId");
String palletId =  resolveValue("xml:/NodeInventory/Inventory/@PalletId");
if (isVoid(itemId)){ %>
<yfc:callAPI apiID='AP1'/>
<yfc:callAPI apiID='AP5'/>
<%
if(!(equals("0",resolveValue("xml:/NodeInventory/LocationInventoryList/@TotalNumberOfRecords")))) { %>
    <yfc:callAPI apiID='AP2'/>
<% } %>
<% } else { %>
<yfc:callAPI apiID='AP3'/>
<yfc:callAPI apiID='AP4'/>
<yfc:callAPI apiID='AP6'/>
<% } 
String IsSerialTracked = resolveValue("xml:ItemDetails:/Item/InventoryParameters/@IsSerialTracked");
String IsNodeTracksSerial = resolveValue("xml:Node:/ShipNodeList/ShipNode/@SerialTracking");
String IsSerialCapturedInInventory = resolveValue("xml:ItemDetails:/Item/PrimaryInformation/@SerialCapturedInInventory");
String isTagReqdInInventory = resolveValue("xml:ItemDetails:/Item/@TagCapturedInInventory");
int firscolspan=3;
//changes made in RQ-WMS-158
if (equals("Y",IsSerialCapturedInInventory)) {
	firscolspan=2;
} %>
<tr>
<%
		YFCElement detailAPIRoot = (YFCElement) request.getAttribute("AdjustLocationInventory");
		if (detailAPIRoot != null) {
			YFCElement oSource = null;
			YFCElement oInventory = null;
			oSource = detailAPIRoot.getChildElement("Source");
			if ( oSource == null) {
				oSource = detailAPIRoot.createChild("Source");
			}
			oInventory = oSource.getChildElement("Inventory");
			if ( oInventory == null) {
				oInventory = oSource.createChild("Inventory");
			}
			if (oInventory.getChildElement("SerialList") == null) {
				oInventory.createChild("SerialList");
			}
		} else {			
			YFCDocument oDoc = YFCDocument.createDocument("AdjustLocationInventory");
			YFCElement oAdjust = oDoc.getDocumentElement();
			((oAdjust.createChild("Source")).createChild("Inventory")).createChild("SerialList");
			request.setAttribute("AdjustLocationInventory",oAdjust);
		}
%>
<td colspan="<%=firscolspan%>" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
            <jsp:param name="caseId" value="<%=caseId%>"/>
            <jsp:param name="palletId" value="<%=palletId%>"/>
		    <jsp:param name="getRequestDOM" value="Y"/>
		    <jsp:param name="RootNodeName" value="AdjustLocationInventory"/>
		    <jsp:param name="ChildLoopXMLName" value="SerialDetail"/>
		    <jsp:param name="ChildLoopXMLKeyName" value="SerialNo"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td colspan="<%=firscolspan%>" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
        </jsp:include>
    </td>
</tr>

<% //changes made in RQ-WMS-158
	if (equals("Y",IsSerialCapturedInInventory) ) { %>
<tr>
<% 
		String removeSerialheight="120";
		if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
			removeSerialheight="210";
		}
		
%>		
		<td colspan="1" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I05"/>
                <jsp:param name="height" value="<%=removeSerialheight%>"/>
			</jsp:include>
		</td>
		<% String addSerialheight = "250"; 
			detailAPIRoot = (YFCElement) request.getAttribute("AdjustLocationInventory");
			YFCElement oExistingSerialList = null;
			if (detailAPIRoot != null) {
				
				if (detailAPIRoot.getChildElement("Source") != null)
					if (detailAPIRoot.getChildElement("Source").getChildElement("Inventory") != null)
						oExistingSerialList = detailAPIRoot.getChildElement("Source").getChildElement("Inventory").getChildElement("SerialList");
				if (oExistingSerialList != null) {
				   for (Iterator oIter = oExistingSerialList.getChildren();oIter.hasNext();) {
					   YFCElement oChild = (YFCElement) oIter.next();
					   oExistingSerialList.removeChild(oChild);
				   }
				}
			}

			if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>
	   
				<td colspan="1" height="100%" valign="top">
				<table>
					<tr>
<%  
					addSerialheight = "200"; 	
			}
%>
       
				<td height="100%" valign="top"  width="50%">
					<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
						<jsp:param name="CurrentInnerPanelID" value="I07"/>
						<jsp:param name="height" value="<%=addSerialheight%>"/>
						<jsp:param name="RootNodeName" value="AdjustLocationInventory"/>
						<jsp:param name="ChildLoopXMLName" value="SerialDetail"/>
						<jsp:param name="ChildLoopXMLKeyName" value="SerialKey"/>
						<jsp:param name="ChildLoopParentXMLName" value="SerialList"/>
						<jsp:param name="CheckBoxName" value="Blank"/>
						<jsp:param name="ChildLoopXMLSecondaryKeyName" value="SerialNo"/>
						<jsp:param name="ChildLoopParentNodeDifference" value="Source/Inventory"/>
					</jsp:include>
				</td>
<%
		if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>
			</tr>       
<%
		}
				if (isVoid(itemId)) { 
					if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>
						<tr>
							<td height="100%" valign="top">
								<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
									<jsp:param name="CurrentInnerPanelID" value="I06"/>
									<jsp:param name="Modifiable" value='false'/>
									<jsp:param name="LabelTDClass" value='detaillabel'/>
									<jsp:param name="TagContainer" value='NodeInventory'/>
									<jsp:param name="TagElement" value='TagDetail'/>
								</jsp:include>
							</td>
						</tr>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute1","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute1")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute2","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute2")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute3","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute3")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotExpirationDate","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotExpirationDate")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotKeyReference","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotKeyReference")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotNumber","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotNumber")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@ManufacturingDate","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@ManufacturingDate")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@RevisionNo","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@RevisionNo")%>/>
						<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@TagNumber","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@TagNumber")%>/>	
					
<% 
					}
				} else {
					if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>
						<tr>
							<td height="100%" valign="top" width="50%">
								<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
									<jsp:param name="CurrentInnerPanelID" value="I06"/>
									<jsp:param name="Modifiable" value='true'/>
									<jsp:param name="LabelTDClass" value='detaillabel'/>
									<jsp:param name="InputTDClass" value='searchcriteriacell'/>
									<jsp:param name="BindingPrefix" value='xml:/AdjustLocationInventory/Source/Inventory/TagDetail'/>
								</jsp:include>
							</td>
				<script>
						var reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute1");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute2");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute3");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotExpirationDate");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotKeyReference");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotNumber");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@ManufacturingDate");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@RevisionNo");
						if (reasontext!= null) { 
							reasontext.value = "";
						}reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@TagNumber");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@BatchNo");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
				</script>                  
						</tr>
					
<% 
					}
				} 
		if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>
	   
		</table>
		</td>
<%
		}	
%>
</tr>
<% } else {%>
<tr>
	<%	if (isVoid(itemId)) { 
			if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
	%>	   
			<td colspan="3" valign="top"  width="50%">
				<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
					<jsp:param name="CurrentInnerPanelID" value="I06"/>
					<jsp:param name="Modifiable" value='false'/>
					<jsp:param name="LabelTDClass" value='detaillabel'/>
					<jsp:param name="TagContainer" value='NodeInventory'/>
					<jsp:param name="TagElement" value='TagDetail'/>
				</jsp:include>
			</td>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute1","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute1")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute2","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute2")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute3","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotAttribute3")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotExpirationDate","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotExpirationDate")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotKeyReference","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotKeyReference")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotNumber","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@LotNumber")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@ManufacturingDate","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@ManufacturingDate")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@RevisionNo","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@RevisionNo")%>/>
			<input type="hidden"  <%=getTextOptions("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@TagNumber","xml:/NodeInventory/LocationInventoryList/LocationInventory/ItemInventoryDetailList/ItemInventoryDetail/TagDetail/@TagNumber")%>/>	
		
<% 
			}	
		} else { 
			if("Y".equals(isTagReqdInInventory) || "S".equals(isTagReqdInInventory)){
%>		
			<td colspan="3" valign="top">
				<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
					<jsp:param name="CurrentInnerPanelID" value="I06"/>
					<jsp:param name="Modifiable" value='true'/>
					<jsp:param name="LabelTDClass" value='detaillabel'/>
					<jsp:param name="InputTDClass" value='searchcriteriacell'/>
					<jsp:param name="BindingPrefix" value='xml:/AdjustLocationInventory/Source/Inventory/TagDetail'/>
				</jsp:include>
			</td>
				<script>
						var reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute1");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute2");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotAttribute3");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotExpirationDate");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotKeyReference");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@LotNumber");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@ManufacturingDate");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@RevisionNo");
						if (reasontext!= null) { 
							reasontext.value = "";
						}reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@TagNumber");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
						reasontext = document.all("xml:/AdjustLocationInventory/Source/Inventory/TagDetail/@BatchNo");
						if (reasontext!= null) { 
							reasontext.value = "";
						}
				</script> 		
<%
		}
	} 
%>
</tr>
<% } %>
<tr>
    <td colspan="1" height="100%" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="Binding" value="xml:/AdjustLocationInventory/Audit"/>
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
<%	if (!equals("Y",IsSerialTracked) || !equals(IsNodeTracksSerial,"Y")) { %>
    <td colspan="1" height="100%" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I08"/>
        </jsp:include>
    </td>
<% } %>
	<td colspan="1" height="100%" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I04"/>
        </jsp:include>
    </td>
</tr>
	<script>
		var reasontext = document.all("xml:/AdjustLocationInventory/Audit/@ReasonText");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@ReasonCode");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		var quan = document.all("xml:/AdjustLocationInventory/Source/Inventory/@Quantity")
		if (quan != null) {
			quan.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@Reference1");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@Reference2");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@Reference3");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@Reference4");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
		reasontext = document.all("xml:/AdjustLocationInventory/Audit/@Reference5");
		if (reasontext!= null) { 
			reasontext.value = "";
		}
	</script>
</table>