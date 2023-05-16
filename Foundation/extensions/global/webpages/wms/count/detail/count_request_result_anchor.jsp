<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
int total=0;
int item=0;
	YFCElement countResultList= (YFCElement) request.getAttribute("CountResultList");
	if(countResultList==null){
			countResultList=YFCDocument.parse("<CountResultList><CountRequest/></CountResultList>").getDocumentElement();
	}
	YFCElement countReq=countResultList.getChildElement("CountRequest");
    YFCElement status = countReq.getChildElement("Status");
    String sStatus = status.getAttribute("Status");
	if(countReq==null){
			countReq=countResultList.createChild("CountRequest");
	}
//	countReq.setAttribute("ResolvableVariance", "Y");
	
//	if(equals("1500.02",resolveValue("xml:/CountResultList/CountRequest/Status/@Status"))){
//		countReq.setAttribute("ResolvableVariance", "N");
//	}
    	/*if(!YFCCommon.equals(sStatus,"9000") &&  !YFCCommon.equals(countResultList.getAttribute("NoOfVarianceResults"),"0")) {
        	countResultList.setAttribute("EnableAcceptVariance", "Y");
    	} else {
        	countResultList.setAttribute("EnableAcceptVariance", " ");
		}*/
	
	if(countResultList!=null){
		countReq = countResultList.getChildElement("CountRequest");
		String sCountRequestKey = countReq.getAttribute("CountRequestKey");
		String sCountRequestNo = countReq.getAttribute("CountRequestNo");
		YFCElement countRequestElem=YFCDocument.parse("<CountRequest/>").getDocumentElement();
		countRequestElem.setAttribute("CountRequestKey", sCountRequestKey );
		countRequestElem.setAttribute("CountRequestNo", sCountRequestNo );
		%>
		<yfc:callAPI apiName="getCountRequestDetails" inputElement= "<%=countRequestElem%>"  outputNamespace="countRequestDetail"/>
		<%
			YFCElement countReqDetailsElem = (YFCElement)request.getAttribute("countRequestDetail");

			YFCElement eChangeCountRequest = (YFCElement) request.getAttribute("ChangeCountRequest");

			YFCElement countAllowedTransElem =  countReqDetailsElem.getChildElement("AllowedTransactions");
			YFCNodeList allowedTranList = countAllowedTransElem.getElementsByTagName("Transaction");
			
			boolean bVerifyCountAllowed=false;
			if(allowedTranList!=null){
				for(int i=0; i<allowedTranList.getLength(); i++){
					YFCElement allowedTranElem = (YFCElement)allowedTranList.item(i);
					String sTransactionId = allowedTranElem.getAttribute("Tranid");
					if(equals(sTransactionId, "COUNT_VERIFICATION_COMPLETE")){
						bVerifyCountAllowed = true;
						break;
					}
				 }

				 for(int i=0; i<allowedTranList.getLength(); i++){
					YFCElement allowedTranElem = (YFCElement)allowedTranList.item(i);
					String sTransactionId = allowedTranElem.getAttribute("Tranid");
					if(equals(sTransactionId, "ACCEPT_VARIANCE")){

						if(bVerifyCountAllowed==true){

							if(eChangeCountRequest!=null &&  eChangeCountRequest.hasAttribute("CountVerified") && equals("Y", eChangeCountRequest.getAttribute("CountVerified"))){
								// if verification has to be done, dont enable accept variance until verification is complete
								// CR 72590
								countResultList.setAttribute("EnableAcceptVariance", true);
								break;
							}else{
								countResultList.setAttribute("EnableAcceptVariance", false);
								break;
							}

						}
						else{
							countResultList.setAttribute("EnableAcceptVariance", true);
							break;
						}
					}

				 }
			}
	}
%>
<table class="anchor" cellpadding="7px"  cellSpacing="0" >
	<tr>
		<td colspan="3" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I01"/>
			</jsp:include>
		</td>
	</tr>
<yfc:loopXML binding="xml:/CountResultList/@SummaryResultList" id="SummaryResultList"> 
<%if(!isVoid(resolveValue("xml:/SummaryResultList/CountResult/@ItemID"))){
	item++;
	}
	total++;
%>
</yfc:loopXML> 
<% if(item>0){%>
	<tr>
		<td colspan="3" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I02"/>
			</jsp:include>
		</td>
	</tr>
<%} if((total-item)>0){%>
	<tr>
		<td colspan="3" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I03"/>
			</jsp:include>
		</td>
	</tr>
<%}%>
<% if((total==0)&&(item==0)){%>

	<tr>
		<td colspan="3" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I02"/>
			</jsp:include>
		</td>
	</tr>
	<tr>
		<td colspan="3" valign="top">
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I03"/>
			</jsp:include>
		</td>
	</tr>
<%}%>
</table>  
