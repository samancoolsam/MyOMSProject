<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.ui.backend.util.HTMLEncode" %>
<%@ page import="java.net.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<%
	String	keyName = null;
	String	keyValue = null;
	String	linkNo = null;
	//Sorting the InboxReferences on the basis of the Key.
	YFCElement	inboxRefListElement = null;
	YFCElement inboxElement = (YFCElement) request.getAttribute("Inbox");
	if(!isVoid(inboxElement)){
		String[] sortingAttr = {"InboxReferenceKey"};
		//System.out.println("Before Sorting:" + inboxElement);
		inboxRefListElement = inboxElement.getChildElement("InboxReferencesList");
		if(!isVoid(inboxRefListElement)){
			inboxRefListElement.sortChildren(sortingAttr);
			//Putting back the sorted element in the request.
			request.setAttribute("Inbox", inboxElement);
		}
	}
%>
<yfc:loopXML  binding="xml:/Inbox/InboxReferencesList/@InboxReferences" id="InboxReferences"> 
<%
	String referenceType = getValue("InboxReferences","xml:/InboxReferences/@ReferenceType");
	String referenceName = getValue("InboxReferences","xml:/InboxReferences/@Name");
	String referenceValue = getValue("InboxReferences","xml:/InboxReferences/@Value");
%>
<%
    if ("INTEGRATION_ERROR_ID".equals(referenceName)) {
        keyName = referenceName;
        keyValue = referenceValue;
        linkNo = "L08";
%>
        <yfc:makeXMLInput name="linkKey" >
            <yfc:makeXMLKey binding="xml:/IntegrationError/@ErrorTxnId" value="xml:/InboxReferences/@Value"/>
        </yfc:makeXMLInput>
<%
    } else if ("INTEGRATION_ERROR_GROUP_ID".equals(referenceName)) {
        keyName = referenceName;
        keyValue = referenceValue;
        linkNo = "L09";
%>
        <yfc:makeXMLInput name="linkKey" >
            <yfc:makeXMLKey binding="xml:/IntegrationErrorGroup/@ExceptionGroupId" value="xml:/InboxReferences/@Value"/>
        </yfc:makeXMLInput>
<%
	}
%>
</yfc:loopXML> 

<table class="table" width="100%" >
	<%String className="oddrow";%>
  <thead> 
  <tr> 
    <td class="tablecolumnheader" height="20" style="width:<%=getUITableSize("xml:/InboxReferences/@Name")%>"><yfc:i18n>ReferenceName</yfc:i18n></td>
    <td class="tablecolumnheader" height="20" style="width:<%=getUITableSize("xml:/InboxReferences/@Value")%>"><yfc:i18n>ReferenceValue</yfc:i18n></td>
  </tr>
  </thead> 
  <tbody> 
  
	  <yfc:loopXML  binding="xml:/Inbox/InboxReferencesList/@InboxReferences" id="InboxReferences"> 
		<%
			String referenceType = getValue("InboxReferences","xml:/InboxReferences/@ReferenceType");
			String tempReferenceName = getValue("InboxReferences","xml:/InboxReferences/@Name");
			String referenceValue = getValue("InboxReferences","xml:/InboxReferences/@Value");
			String referenceName = ("TEXT".equals(referenceType) && tempReferenceName!=null && tempReferenceName.matches("\\d+"))?"":tempReferenceName;
		%>
		<%if ( !"COMMENT".equals(referenceType) ) {%>
	  <tr> 
	  <% if(!(linkNo !=null && keyName != null && keyName.equals(referenceName))){ %>
			<%if ( "URL".equalsIgnoreCase(referenceType) ) {%>
				<td class="tablecolumn"><%=getI18N(referenceName)%></td>
				<td class="tablecolumn"><a href='<%=referenceValue%>' ><%=referenceValue%></a></td>
			<%} else if ("TraceXML".equalsIgnoreCase(referenceType)) {
				if (equals("oddrow",className))
					className="evenrow";
				else
					className="oddrow";
				%> 
				<tr class='<%=className%>'>
					<td>
						<img onclick="expandCollapseDetails('optionSet_<%=InboxReferencesCounter%>','<%=replaceI18N("Click_To_See_Alert_Detail_Info")%>','<%=replaceI18N("Click_To_Hide_Alert_Detail_Info")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')"  style="cursor:hand" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_To_See_Alert_Detail_Info")%> />
					</td>
					<td class="tablecolumn" ><yfc:i18n><%=getLocalizedValue(referenceName,getReferenceValueHeader(referenceValue))%></yfc:i18n></td>
					<tr id='optionSet_<%=InboxReferencesCounter%>' class='<%=className%>' style="display:none">						
						<td colspan="8">
							<jsp:include page="/em/alerts/detail/alert_detail_xmltrace_references.jsp" flush="true" >
								<jsp:param name="optionSetBelongingToLine" value='<%=String.valueOf(InboxReferencesCounter)%>'/>
							</jsp:include>						
						</td>
					</tr>			
				</tr>			
			<%} else {%>
				<td class="tablecolumn"><%=getI18N(referenceName)%></td>
				<% if (YFCCommon.equals("WaveKey",referenceName)) {%>
					<yfc:makeXMLInput name="waveKey" >
						<yfc:makeXMLKey binding="xml:/Wave/@WaveKey" value="xml:/InboxReferences/@Value" />
					</yfc:makeXMLInput>	  
					<td class="tablecolumn">
						<a <%=getDetailHrefOptions("L06", " ", getParameter("waveKey"), "")%>>	   
							<yfc:getXMLValue binding="xml:/InboxReferences/@Value"/>
						</a>
					</td>
				<%	} else if (YFCCommon.equals("WorkOrderKey",referenceName)) {%>
					<yfc:makeXMLInput name="WorkOrderKey" >
						<yfc:makeXMLKey binding="xml:/Wave/@WorkOrderKey" value="xml:/InboxReferences/@Value" />
					</yfc:makeXMLInput>	  
					<td class="tablecolumn">
						<a <%=getDetailHrefOptions("L07", " ", getParameter("WorkOrderKey"), "")%>>	   
							<yfc:getXMLValue binding="xml:/InboxReferences/@Value"/>
						</a>
					</td>
				<%	} else { %>
					<td class="tablecolumn"><yfc:i18n><%=getLocalizedValue(referenceName,referenceValue)%></yfc:i18n></td>
				<%	}  %>
			<%}%>
			<% } %>
		 </tr>
		<%}%>
	   </yfc:loopXML> 
	  <%
		String orderHeaderKey = getValue("Inbox","xml:/Inbox/@OrderHeaderKey");
		if ( !isVoid(orderHeaderKey) ) {
			String documentType = getValue("Inbox","xml:/Inbox/@OrderDocumentType");
	  %>
	  <tr> 
		<% if ( !isVoid(documentType) ) {%>
			<td class="tablecolumn"><yfc:i18n><%=documentType%>_Order</yfc:i18n></td>
		<% } else { %>
			<td class="tablecolumn"><yfc:i18n>Order</yfc:i18n></td>
		<%}%>
		<td class="tablecolumn">
			<yfc:makeXMLInput name="OrderHeaderKey" >
			    <yfc:makeXMLKey binding="xml:/Order/@OrderHeaderKey" value="xml:/Inbox/@OrderHeaderKey" />
	        </yfc:makeXMLInput>	  
            <a <%=getDetailHrefOptions("L01", getValue("Inbox", "xml:/Inbox/@OrderDocumentType"), getParameter("OrderHeaderKey"), "")%>>	   
				<yfc:getXMLValue binding="xml:/Inbox/@OrderNo"/>
            </a>
		</td>
	  </tr>
	  <%}%>
	  <%
		String orderLineKey = getValue("Inbox","xml:/Inbox/@OrderLineKey");
		if ( !isVoid(orderLineKey) ) {
			String documentType = getValue("Inbox","xml:/Inbox/@OrderDocumentType");
	  %>
	  <tr> 
		<% if ( !isVoid(documentType) ) {%>
			<td class="tablecolumn"><yfc:i18n><%=documentType%>_Order_Line</yfc:i18n></td>
		<% } else {%>
			<td class="tablecolumn"><yfc:i18n>Order_Line</yfc:i18n></td>
		<%}%>
		<td class="tablecolumn">
			<yfc:makeXMLInput name="OrderLineKey" >
			    <yfc:makeXMLKey binding="xml:/OrderLineDetail/@OrderLineKey" value="xml:/Inbox/@OrderLineKey" />
	        </yfc:makeXMLInput>	  
            <a <%=getDetailHrefOptions("L02", getValue("Inbox", "xml:/Inbox/@OrderDocumentType"), getParameter("OrderLineKey"), "")%>>	   
				<%=orderLineKey %>
            </a>
		</td>
	  </tr>
	  <%}%>
	  <%
		String shipmentNo = getValue("Inbox","xml:/Inbox/@ShipmentNo");
		if ( !isVoid(shipmentNo) ) {
			String documentType = getValue("Inbox","xml:/Inbox/@ShipmentDocumentType");
	  %>
	  <tr> 
		<% if ( !isVoid(documentType) ) {%>
			<td class="tablecolumn"><yfc:i18n><%=documentType%>_Shipment</yfc:i18n></td>
		<% } else { %>
			<td class="tablecolumn"><yfc:i18n>Shipment</yfc:i18n></td>
		<%}%>
		<td class="tablecolumn">
			<yfc:makeXMLInput name="ShipmentKey" >
			    <yfc:makeXMLKey binding="xml:/Shipment/@ShipmentKey" value="xml:/Inbox/@ShipmentKey" />
	        </yfc:makeXMLInput>	  
            <a <%=getDetailHrefOptions("L03", getValue("Inbox", "xml:/Inbox/@ShipmentDocumentType"), getParameter("ShipmentKey"), "")%>>	   
				<yfc:getXMLValue binding="xml:/Inbox/@ShipmentNo"/>
            </a>
		</td>
	  </tr>
	  <%}%>
	  <%
		String loadNo = getValue("Inbox","xml:/Inbox/@LoadNo");
		if ( !isVoid(loadNo) ) {
			String documentType = getValue("Inbox","xml:/Inbox/@LoadDocumentType");
	  %>
	  <tr> 
		<% if ( !isVoid(documentType) ) {%>
			<td class="tablecolumn"><yfc:i18n><%=documentType%>_Load</yfc:i18n></td>
		<% } else {%>
			<td class="tablecolumn"><yfc:i18n>Load</yfc:i18n></td>
		<%}%>
		<td class="tablecolumn">
			<yfc:makeXMLInput name="LoadKey" >
			    <yfc:makeXMLKey binding="xml:/Load/@LoadNo" value="xml:/Inbox/@LoadNo" />
	        </yfc:makeXMLInput>	  
            <a <%=getDetailHrefOptions("L04", getValue("Inbox", "xml:/Inbox/@LoadDocumentType"), getParameter("LoadKey"), "")%>>	   
				<yfc:getXMLValue binding="xml:/Inbox/@LoadNo"/>
            </a>
		</td>
	  </tr>
	  <%}%>
	  <%
		String moveRequestKey = getValue("Inbox","xml:/Inbox/@MoveRequestKey");
		if ( !isVoid(moveRequestKey) ) {
	  %>
	  <tr> 
		<td class="tablecolumn"><yfc:i18n>Move_Request</yfc:i18n></td>
		<td class="tablecolumn">
			<yfc:makeXMLInput name="MoveRequestKey" >
			    <yfc:makeXMLKey binding="xml:/MoveRequest/@MoveRequestKey" value="xml:/Inbox/@MoveRequestKey" />
	        </yfc:makeXMLInput>	  
            <a <%=getDetailHrefOptions("L05", " ", getParameter("MoveRequestKey"), "")%>>	   
				<yfc:getXMLValue binding="xml:/Inbox/@MoveRequestKey"/>
            </a>
		</td>
	  </tr>
	  <%}
	if ( linkNo != null ) {%>
		<td class="tablecolumn"><%=keyName%></td>
		<td class="tablecolumn">
            <a <%=getDetailHrefOptions(linkNo, " ", getParameter("linkKey"), "")%>><%=keyValue %></a>
        </td>
  <%}%>
  </tbody> 
</table>

<%!
public String getReferenceValueHeader(String referenceValue) {
	String sHeader = "";
	YFCElement oLogElem = null;
	//System.out.println("\nreferenceValue before decode : "+referenceValue);
	referenceValue = com.yantra.yfc.ui.backend.util.HTMLEncode.htmlUnescape(referenceValue);
	//System.out.println("\nreferenceValue after decode : "+referenceValue);
	try {		
		oLogElem = YFCDocument.parse(referenceValue).getDocumentElement();
	}catch (Exception e){
		e.printStackTrace();
	}
	if(!isVoid(oLogElem)) {
		sHeader = oLogElem.getAttribute("Message");
	}
	return sHeader;
}
%>
