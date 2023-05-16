<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>

<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<%
	YFCElement containerElem = (YFCElement) request.getAttribute("Container");
	String containerKey = containerElem.getAttribute("ShipmentContainerKey");
	YFCElement shipContainerTemplate = YFCDocument.parse("<Container ><Shipment ShipNode=\"\" /></Container>").getDocumentElement();
%>
<yfc:callAPI apiName="getShipmentContainerDetails" inputElement="<%=containerElem%>" templateElement="<%=shipContainerTemplate%>" outputNamespace="Test"/>
<%
	String shipNode = resolveValue("xml:Test:/Container/Shipment/@ShipNode");
	YFCElement shipNodeElem = YFCDocument.parse("<ShipNode ShipNode=\"" + resolveValue("xml:Test:/Container/Shipment/@ShipNode")  + "\"/>").getDocumentElement();
	YFCElement shipNodeTemplate = YFCDocument.parse("<ShipNodeList TotalNumberOfRecords=\"\" ><ShipNode DcmIntegrationRealTime=\"\"/></ShipNodeList>").getDocumentElement();
%>
<yfc:callAPI apiName="getShipNodeList" inputElement="<%=shipNodeElem%>" templateElement="<%=shipNodeTemplate%>" outputNamespace="ShipNodeOutPut"/>
<% if(isVoid(resolveValue("xml:Test:/Container/Shipment/@ShipNode")) || 
	equals("N", resolveValue("xml:ShipNodeOutPut:/ShipNodeList/ShipNode/@DcmIntegrationRealTime") ) ) {
%>
<script>
		alert('<%=getI18N("Cannot_Pack/Unpack_a_Container_for_a_Application_Consoles_Node")%>');
		window.close();
</script>
<%}else{%>
<script>
	var sVal = document.all("EntityKey");
	showDetailForOnAdvancedList('container', 'YWME200',sVal.value , "") ;
</script>
<%}%>

</table>