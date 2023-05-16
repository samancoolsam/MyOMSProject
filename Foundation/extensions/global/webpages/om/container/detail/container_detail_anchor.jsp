<%@include file="/yfsjspcommon/yfsutil.jspf"%>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>

<%String sAppCode = resolveValue("xml:/CurrentEntity/@ApplicationCode");%>
<% 
	int numRecs=0;
	if(!isVoid(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"))){
		numRecs=Integer.parseInt(resolveValue("xml:/Container/ContainerStatusAudits/@TotalNumberOfRecords"));
	}
%>


<table class="anchor" cellpadding="7px"  cellSpacing="0" >
<tr>
    <td colspan="3" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
        </jsp:include>
    </td>
</tr>
<tr>
<%//Execution details should be shown only for WMS nodes
	if(numRecs > 0){
		YFCElement containerElem = (YFCElement) request.getAttribute("Container");
		if (containerElem != null) { 
			YFCElement shipmentElem = containerElem.getChildElement("Shipment");
			if (shipmentElem != null) { 
				if (equals(sAppCode,"omd")) {	 //in case of sales order, put execution status inner panel %>
					
						<td colspan="3" >
							<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
								<jsp:param name="CurrentInnerPanelID" value="I06"/>
							</jsp:include>
						</td>  
					
<%				}
			}
		}
	}
%>
</tr>
<tr>
    <td colspan="3">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td colspan="3" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I04"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td colspan="3" >
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I07"/>
        </jsp:include>
    </td>
</tr>
<% //Child contaners should be shown only for WMS nodes
	if(numRecs > 0){
		YFCElement containerElem = (YFCElement) request.getAttribute("Container");
		if (containerElem != null) { 
			YFCElement shipmentElem = containerElem.getChildElement("Shipment"); %>
					<tr>
					<td colspan="3" >
						<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
							<jsp:param name="CurrentInnerPanelID" value="I05"/>
						</jsp:include>
					</td>
					</tr>
<%		
		}
	}	
%>
</table>