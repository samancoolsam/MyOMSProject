<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/order.jspf" %>

    <% setHistoryFlags((YFCElement) request.getAttribute("Order")); %>
    <yfc:callAPI apiID='AP2'/> <% /* DerivedToOrderLineList */ %>
    <yfc:callAPI apiID='AP3'/> <% /* ChainedToOrderLineList */ %>

    <% prepareRelatedDocuments((YFCElement) request.getAttribute("Order"),(YFCElement) request.getAttribute("DerivedToOrderLineList"),(YFCElement) request.getAttribute("ChainedToOrderLineList")); 
    %>
	<yfc:loopXML binding="xml:/Order/RelatedFromDocuments/RelatedFromDocumentKeys/@Document" id="Document">
		<% request.setAttribute("Document", pageContext.getAttribute("Document")); %>		
		<yfc:callAPI apiID='AP1'/>
		<% insertRelatedFromOrderDetails((YFCElement) request.getAttribute("RelatedFromOrderDetails"),(YFCElement) request.getAttribute("Order"),getValue("Document", "xml:/Document/@Relationship")); %>		
	</yfc:loopXML>
	<% sortRelatedDocuments((YFCElement) request.getAttribute("Order")); %>
	
<table cellSpacing=0 class="anchor" cellpadding="7px">
    <tr>
        <td>
            <jsp:include page="/yfc/innerpanel.jsp" flush="true">
                <jsp:param name="CurrentInnerPanelID" value="I01"/>
            </jsp:include>
        </td>
    </tr>
    <%  
        ArrayList docTypeList = getLoopingElementList("xml:/Order/RelatedDocuments/@DocumentType");
        for (int DocumentTypeCounter = 0; DocumentTypeCounter < docTypeList.size(); DocumentTypeCounter++) {
            
            YFCElement singleDocType = (YFCElement) docTypeList.get(DocumentTypeCounter);
            pageContext.setAttribute("DocumentType", singleDocType);             request.setAttribute("DocumentType", pageContext.getAttribute("DocumentType"));
        
            setAdditionalAttr(singleDocType, (YFCElement) request.getAttribute("DocumentParamsList"));
            String sDocumentType = getDBString(singleDocType.getAttribute("DocumentTypeDescription")) + " " ;
            String sDocumentTypeLiteral = singleDocType.getAttribute("DocumentType") + "_Order" ;
			String sLines = "Lines";
            boolean bIsReturnService = singleDocType.getBooleanAttribute("isReturnDocument");
       %>
        <tr>

            <td>
	            <jsp:include page="/yfc/innerpanel.jsp" flush="true">
	                <jsp:param name="CurrentInnerPanelID" value="I02"/> 
	                <jsp:param name="DocumentTypeCounter" value='<%=String.valueOf(DocumentTypeCounter)%>'/>
	                <jsp:param name="Title" value='<%=sDocumentTypeLiteral%>'/>
					<jsp:param name="TitleQty" value="<%=sLines%>" />
                    <jsp:param name="IsReturnService" value='<%=String.valueOf(bIsReturnService)%>'/>
				</jsp:include>
            </td>
        </tr>
    <% } %>

	
	<%	if(((YFCElement) request.getAttribute("ExchangeOrder")) != null){ %>
			<tr>
				<td>
					<jsp:include page="/yfc/innerpanel.jsp" flush="true">
						<jsp:param name="CurrentInnerPanelID" value="I03"/>
					</jsp:include>
				</td>
			</tr>
	<% } else if(((YFCElement) request.getAttribute("ReturnOrderForExchange")) != null){ 
	%>
			<tr>
				<td>
					<jsp:include page="/yfc/innerpanel.jsp" flush="true">
						<jsp:param name="CurrentInnerPanelID" value="I03"/>
					</jsp:include>
				</td>
			</tr>
	<% } %>
	</table>
