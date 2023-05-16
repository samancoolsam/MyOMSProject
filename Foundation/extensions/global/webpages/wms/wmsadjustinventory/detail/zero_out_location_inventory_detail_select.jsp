<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<%@ page import="com.yantra.yfc.core.YFCObject" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>
<script language="javascript">
function callSaveFn() {
	var sReasonCode = document.all["xml:/ZeroOutLocationInventory/Audit/@ReasonCode"];
	if(sReasonCode.value == "" ) {
		
		alert(YFCMSG040);
		 return false;
	}
	else {		
		var containerForm= document.all("containerform");
		var hiddenCallApi = document.createElement("<INPUT type='hidden' name='xml:/Test/@CallApi' value='Y'/>");
		containerForm.insertBefore(hiddenCallApi);		
		yfcChangeDetailView(getCurrentViewId());
		
		
		
	}
	
}
</script>

<% 
boolean bValidReason = false;
String sLocationId = resolveValue("xml:/ZeroOutLocationInventory/Source/@LocationId");
String sNode = resolveValue("xml:/ZeroOutLocationInventory/@Node");

String sReasonCode = resolveValue("xml:/ZeroOutLocationInventory/Audit/@ReasonCode");
String sReasonText = resolveValue("xml:/ZeroOutLocationInventory/Audit/@ReasonText"); 
String entCode = resolveValue("xml:/LocationInventorySummary/@EnterpriseCode");  %>

<% 
String sEnterpriseCode = resolveValue("xml:/ZeroOutLocationInventory/@EnterpriseCode");

%>

	<%	if(equals(resolveValue("xml:/Test/@CallApi"),"Y")){%>

<% YFCDocument inputDoc = YFCDocument.createDocument("AdjustmentReason");
				YFCElement adjustTemplate = YFCDocument.parse("<AdjustmentReason ReasonCode=\"\"></AdjustmentReason>").getDocumentElement();

				YFCElement adjustReasonElem = inputDoc.getDocumentElement();
			    adjustReasonElem.setAttribute("Node",sNode);
				adjustReasonElem.setAttribute("ReasonCode",sReasonCode);
				YFCElement adjustReasonDetailsElem =  adjustReasonElem.createChild("AdjustmentReasonDetails");
				YFCElement adjustReasonDetailElement =  adjustReasonDetailsElem.createChild("AdjustmentReasonDetail");
				//adjustReasonDetailElement.setAttribute("EnterpriseCode",entCode); 
%>

				 <yfc:callAPI apiName="getAdjustmentReasonList" inputElement="<%=adjustReasonElem%>"  outputNamespace="AdjustReasonCode"/>

				<% 
				
				YFCElement apiOutput = (YFCElement)request.getAttribute("AdjustReasonCode");

				//Loop through the XML
				if(!isVoid(apiOutput)){
					for(Iterator i = apiOutput.getChildren(); i.hasNext();) {
						YFCElement adjustmentReason = (YFCElement)i.next();
						YFCElement reasonDtls = adjustmentReason.getChildElement("AdjustmentReasonDetails");
						if(!isVoid(reasonDtls)){
							bValidReason = true;
							for(Iterator iReasonDtl = reasonDtls.getChildren(); iReasonDtl.hasNext();) {
								bValidReason = false;
								String sEntCode = ((YFCElement)iReasonDtl.next()).getAttribute("EnterpriseCode");
								if(sEntCode.equals(entCode)){						
									bValidReason = true;
									break;
								}
							}

							if(bValidReason){				
								break;
							}
						}
					}
				}
					
			if(! bValidReason){%>
			<script>
				alert(YFCMSG140);
			
			</script>
			<%
			
			}%>

		<%
		if(bValidReason){	
		YFCElement elemRaiseEvent = YFCDocument.parse("<RaiseEvent EventId=\"ZEROOUT_LOC_INV_INITIATED\"               TransactionId=\"ZEROOUT_LOCATION_INV\"/>").getDocumentElement();
	elemRaiseEvent.setAttribute("ShipNode",sNode);

	YFCElement dataType=elemRaiseEvent.createChild("DataType");
	dataType.setNodeValue("1");
	dataType.setAttribute("DataType", "XML_STRING");
		
	YFCElement xmlData=elemRaiseEvent.createChild("XMLData");
		
	YFCElement eventXML=YFCDocument.parse("<ZeroOutLocationInventory Node=\""+getValue("CurrentUser","xml:CurrentUser:/User/@Node" )+"\" EnterpriseCode=\""+ sEnterpriseCode +"\"/>").getDocumentElement();	
    eventXML.createChild("Source").setAttribute("LocationId",sLocationId);
	YFCElement auditElem = eventXML.createChild("Audit");
	auditElem.setAttribute("ReasonCode",sReasonCode);
	auditElem.setAttribute("ReasonText",sReasonText);
	xmlData.setNodeValue(eventXML.toString());

	xmlData.setAttribute("DataType", "XML_STRING");
	 %>


<yfc:callAPI apiName="raiseEvent" inputElement="<%=elemRaiseEvent%>" 
					 outputNamespace=""/> 

<% } %>
<% } %>
 <table width="100%" class="view">


<tr>
    <td class="detaillabel" ><yfc:i18n>Node</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/ZeroOutLocationInventory/@Node" />
        <input type="hidden"  
         <%=getTextOptions("xml:/ZeroOutLocationInventory/@Node","xml:/ZeroOutLocationInventory/@Node")%> />
    </td>

	<td class="detaillabel" >
        <yfc:i18n>Enterprise</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		
			<yfc:getXMLValue binding="xml:/ZeroOutLocationInventory/@EnterpriseCode" />
			<input type="hidden" <%=getTextOptions("xml:/ZeroOutLocationInventory/@EnterpriseCode","xml:/ZeroOutLocationInventory/@EnterpriseCode")%> />
		  
    </td>
	<td>
	</td>
	<td>
	</td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Location</yfc:i18n>
    </td>
    <td class="protectedtext" nowrap="true">
		
			<yfc:getXMLValue binding="xml:/ZeroOutLocationInventory/Source/@LocationId" />
			<input type="hidden" <%=getTextOptions("xml:/ZeroOutLocationInventory/Source/@LocationId","xml:/ZeroOutLocationInventory/Source/@LocationId")%> />
		
    </td>
</tr>
	
	
			
	
			<yfc:makeXMLInput name="MyEntityKey">
					<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/@EnterpriseCode" value="xml:/ZeroOutLocationInventory/@EnterpriseCode"/>
					<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/@Node" value="xml:/ZeroOutLocationInventory/@Node"/>
					<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/Source/@LocationId" value="xml:/ZeroOutLocationInventory/Source/@LocationId"/>
					
			</yfc:makeXMLInput>

    <input type="hidden" name="myEntityKey" value="<%=getParameter("MyEntityKey")%>"/></tr>
    
</table>
