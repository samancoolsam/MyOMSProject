<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<SCRIPT LANGUAGE="JavaScript">

function OKForPrintClicked(){
		yfcChangeDetailView(getCurrentViewId());		
		//window.close();
}
</SCRIPT>
<%
	String sOrgCode = "";		
	String sEnterpriseCode = "";
	String sBuyer = "";
	String sSeller = "";
	String sScac = "";
	String sFlowName = "";
	String sDoc = resolveValue("xml:/Print/@PrintDocumentId");
	String sPrinter = resolveValue("xml:/Print/PrinterPreference/@PrinterId");
	String sLoadKey = resolveValue("xml:/Print/Load/@LoadKey");
	String sShipmentKey = resolveValue("xml:/Print/Shipment/@ShipmentKey");
	String sShipmentContainerKey = resolveValue("xml:/Print/Container/@ShipmentContainerKey");
	String sWaveNo = resolveValue("xml:/Print/Wave/@WaveNo");	
	String sManifestKey = resolveValue("xml:/Print/Manifest/@ManifestKey");	
	String sIsHazmat = resolveValue("xml:/Print/Manifest/@IsHazmat");
	String sBatchNo = resolveValue("xml:/Print/Batch/@BatchNo");

	if((sDoc!=null) && (!isVoid(sDoc))){
		YFCElement rootElement = YFCDocument.createDocument("Print").getDocumentElement();
		if((sLoadKey!=null) && (!isVoid(sLoadKey))){			
				String sOriginNode = resolveValue("xml:/Print/Load/@OriginNode");
				String sLoadType = resolveValue("xml:/Print/Load/@LoadType");
				String sMultipleLoadStop = resolveValue("xml:/Print/Load/@MultipleLoadStop");
				sScac = resolveValue("xml:/Print/Load/@Scac");
				sEnterpriseCode = resolveValue("xml:/Print/Load/@EnterpriseCode");
				sFlowName = "PrintLoadBOL";
			if(isShipNodeUser()) {
				sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
			}else{
				sOrgCode = sOriginNode;
			}
			rootElement.getChildElement("Load", true).setAttribute("LoadKey",sLoadKey);
			rootElement.getChildElement("Load", true).setAttribute("LoadType",sLoadType);
			rootElement.getChildElement("Load", true).setAttribute("MultipleLoadStop",sMultipleLoadStop);
		}else if((sShipmentKey!=null) && (!isVoid(sShipmentKey))){
				String sShipNode = resolveValue("xml:/Print/Shipment/@ShipNode");
				sScac = resolveValue("xml:/Print/Shipment/@SCAC");				
				sEnterpriseCode = resolveValue("xml:/Print/Shipment/@EnterpriseCode");
				sBuyer = resolveValue("xml:/Print/Shipment/@BuyerOrganizationCode");
				sSeller = resolveValue("xml:/Print/Shipment/@SellerOrganizationCode");
				if(isShipNodeUser()) {
					sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
				}else{
					sOrgCode = sShipNode;
				}
				rootElement.getChildElement("Shipment", true).setAttribute("ShipmentKey",sShipmentKey);
				
				if(equals(sDoc,"PACKLIST")){
					sFlowName = "PrintPackList";
				}else if(equals(sDoc,"CONTAINER_LABEL")){
					sFlowName = "PrintShipmentContainerLabels";
				}else if(equals(sDoc,"VICS_BOL")){
					sFlowName = "PrintShipmentBOL";
				}
		}else if((sShipmentContainerKey!=null) && (!isVoid(sShipmentContainerKey))){
				String sShipNode = resolveValue("xml:/Print/Container/@ShipNode");
				sScac = resolveValue("xml:/Print/Container/@SCAC");				
				sEnterpriseCode = resolveValue("xml:/Print/Container/@EnterpriseCode");
				sBuyer = resolveValue("xml:/Print/Container/@BuyerOrganizationCode");
				sSeller = resolveValue("xml:/Print/Container/@SellerOrganizationCode");
				if(isShipNodeUser()) {
					sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
				}else{
					sOrgCode = sShipNode;
				}
				rootElement.getChildElement("Container", true).setAttribute("ShipmentContainerKey",sShipmentContainerKey);
				if(equals(sDoc,"CONTAINER_LABEL")) {
					sFlowName = "PrintShippingLabel";
				}else if(equals(sDoc,"UPS_CARRIER_LABEL")) {
					rootElement.getChildElement("Container", true).setAttribute("SCAC",sScac);
					sFlowName = "PrintUPSCarrierLabel";
				}
		}else if((sWaveNo!=null) && (!isVoid(sWaveNo))){
				String sShipNode = resolveValue("xml:/Print/Wave/@Node");				
				if(isShipNodeUser()) {
					sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
				}else{
					sOrgCode = sShipNode;
				}
				rootElement.getChildElement("Wave", true).setAttribute("WaveNo",sWaveNo);
				rootElement.getChildElement("Wave", true).setAttribute("Node",sOrgCode);
				sFlowName = "PrintWave";
		}else if((sManifestKey!=null) && (!isVoid(sManifestKey))){
				String sShipNode = resolveValue("xml:/Print/Manifest/@ShipNode");
				sScac = resolveValue("xml:/Print/Manifest/@SCAC");				
				if(isShipNodeUser()) {
					sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
				}else{
					sOrgCode = sShipNode;
				}
				rootElement.getChildElement("Manifest", true).setAttribute("ManifestKey",sManifestKey);
				rootElement.getChildElement("Manifest", true).setAttribute("IsHazmat",sIsHazmat);
				rootElement.getChildElement("Manifest", true).setAttribute("SCAC",sScac);
				sFlowName = "PrintManifestSummary";
		}else if((sBatchNo!=null) && (!isVoid(sBatchNo))){
				String sShipNode = resolveValue("xml:/Print/Batch/@Node");				
				if(isShipNodeUser()) {
					sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
				}else{
					sOrgCode = sShipNode;
				}
				rootElement.getChildElement("Batch", true).setAttribute("BatchNo",sBatchNo);
				rootElement.getChildElement("Batch", true).setAttribute("Node",sOrgCode);
				sFlowName = "PrintPickList";
		}

		rootElement.getChildElement("PrinterPreference", true).setAttribute("OrganizationCode",sOrgCode);
		rootElement.getChildElement("PrinterPreference").setAttribute("PrinterId",resolveValue("xml:/Print/PrinterPreference/@PrinterId"));		
		rootElement.getChildElement("LabelPreference", true).setAttribute("Node",sOrgCode);
		rootElement.getChildElement("LabelPreference", true).setAttribute("SCAC",sScac);
		rootElement.getChildElement("LabelPreference", true).setAttribute("EnterpriseCode",sEnterpriseCode);
		rootElement.getChildElement("LabelPreference", true).setAttribute("BuyerOrganizationCode",sBuyer);
		rootElement.getChildElement("LabelPreference", true).setAttribute("SellerOrganizationCode",sSeller);
		rootElement.getChildElement("LabelPreference", true).setAttribute("NoOfCopies",resolveValue("xml:/Print/LabelPreference/@NoOfCopies"));

%>
	<yfc:callAPI serviceName="<%=sFlowName%>" inputElement='<%=rootElement%>' outputNamespace="Print" />
<%
	}
%>
<yfc:callAPI apiID="AP1"/>
<yfc:callAPI apiID="AP2"/>
<%
	YFCElement printRootElem=(YFCElement)request.getAttribute("PrintDocument");
	HashMap printDocMap=new HashMap();
	for(Iterator oItr=printRootElem.getChildren();oItr.hasNext();) {
		YFCElement printDocElem=(YFCElement)oItr.next();
		String sDocumentId=printDocElem.getAttribute("PrintDocumentId");
		String sDocumentDescription=printDocElem.getAttribute("PrintDocumentDescription");
		if(isVoid(sDocumentDescription)) {
			sDocumentDescription=sDocumentId;
		}
		printDocMap.put(sDocumentId,sDocumentDescription);
	}
%>
<table width="100%" class="view">
<%if((sWaveNo==null) || (isVoid(sWaveNo))){%>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Print_Document</yfc:i18n>
        </td>
        <td>
            <select name="xml:/Print/@PrintDocumentId" class="combobox">
				<%if((sLoadKey!=null) && (!isVoid(sLoadKey))){%>						
						<OPTION value="" selected></OPTION>
						<OPTION value=VICS_BOL><%=(String)printDocMap.get("VICS_BOL")%></OPTION> 
				<%}else if((sShipmentKey!=null) && (!isVoid(sShipmentKey))){%>
						<OPTION value="" selected></OPTION>
						<OPTION value=PACKLIST><%=(String)printDocMap.get("PACKLIST")%></OPTION> 
						<OPTION value=CONTAINER_LABEL><%=(String)printDocMap.get("CONTAINER_LABEL")%></OPTION> 
						<OPTION value=VICS_BOL><%=(String)printDocMap.get("VICS_BOL")%></OPTION>
				<%}else if((sShipmentContainerKey!=null) && (!isVoid(sShipmentContainerKey))){%>
						<OPTION value="" selected></OPTION>
						<OPTION value=CONTAINER_LABEL><%=(String)printDocMap.get("CONTAINER_LABEL")%></OPTION> 	
						<OPTION value=UPS_CARRIER_LABEL><%=(String)printDocMap.get("UPS_CARRIER_LABEL")%></OPTION> 	
				<%}else if((sManifestKey!=null) && (!isVoid(sManifestKey))){%> 
						<OPTION value="" selected></OPTION>
						<OPTION value=UPS_PICKUP_SUMMARY><%=(String)printDocMap.get("UPS_PICKUP_SUMMARY")%></OPTION> 				
				<%}else if((sBatchNo!=null) && (!isVoid(sBatchNo))){%> 
						<OPTION value="" selected></OPTION>
						<OPTION value=PICKLIST><%=(String)printDocMap.get("PICKLIST")%></OPTION> 				
				<%}else{%>
                <yfc:loopOptions binding="xml:PrintDocument:/PrintDocuments/@PrintDocument"                    name="PrintDocumentDescription" value="PrintDocumentId" isLocalized="Y"/>
				<%}%>
            </select>
        </td>
    </tr>
<%}else{%>
	<input type="hidden" name="xml:/Print/@PrintDocumentId" value="Wave"/>
<%}%>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Printer_Name</yfc:i18n>
        </td>
        <td>
            <select name="xml:/Print/PrinterPreference/@PrinterId" class="combobox">
                <yfc:loopOptions binding="xml:Device:/Devices/@Device" 
                    name="DeviceId" value="DeviceId"/>
            </select>
        </td>
    </tr>    
	 <tr>
        <td class="detaillabel">
            <yfc:i18n>No_of_Copies</yfc:i18n>
        </td>
        <td>
			<input type="text" class="numericunprotectedinput" <%=getTextOptions("xml:/Print/LabelPreference/@NoOfCopies","")%> />
        </td>
    </tr>    
	<tr>        
        <td align="center" colspan="2">
            <input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="OKForPrintClicked();"/>
        <td>
    <tr>
</table>