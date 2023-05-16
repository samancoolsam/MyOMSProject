<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript">
function enterActionCancellationReason(modReasonViewID, modReasonCodeBinding, modReasonTextBinding,screenType,key) {
 
    var myObject = new Object();
    myObject.currentWindow = window;
    myObject.reasonCodeInput = document.all(modReasonCodeBinding);
    myObject.reasonTextInput = document.all(modReasonTextBinding);       
	if(screenType=='LIST'){
		<%if(isShipNodeUser()){%>
		yfcShowDetailPopupWithKeys(modReasonViewID,"","550","255",myObject,key,"count", "") ;
		<%}else{%>
			if(isMultipleRecordsSelected(key)){
				alert('<%=getI18N("Node_User_Can_Only_Cancel_Multiple_Requests_Select_Only_one_Record")%>');
				return false;
			}else{
				yfcShowDetailPopupWithKeys(modReasonViewID,"","550","255",myObject,key,"count", "") ;
			}
		<%}%>
	}else{
		yfcShowDetailPopupWithKeys(modReasonViewID, "", "550", "255", myObject, key,"count", "");
	}

	var retVal = myObject["EMReturnValue"];
    var returnValue = myObject["OKClicked"];
    if ( "YES" == returnValue ) {
        return retVal;
    } else {
        return false;
    }
}
</script>
<%
String sKey=resolveValue("xml:/CountRequest/@CountRequestKey");
if(sKey!="")
{
%>

<table class="anchor" cellpadding="7px"  cellSpacing="0">
<yfc:callAPI apiID="AP1"/>

<%
	String sStatus=resolveValue("xml:/CountRequest/@Status");
    YFCElement eCount = (YFCElement) request.getAttribute("CountRequest");
	YFCElement eChangeCountRequest = (YFCElement) request.getAttribute("ChangeCountRequest");
    if(eCount != null){
        eCount.setAttribute("ModifyFlag", true);
        eCount.setAttribute("RecordFlag", false);
		eCount.setAttribute("TasksCreated", true);
		eCount.setAttribute("TasksCompleted", true);
		eCount.setAttribute("CountRequestNotCompletedOrCancelled", true);
		eCount.setAttribute("VarianceAcceptableForCountRequest", true);
		eCount.setAttribute("VerifyCountAllowed", false);
		
		 // call API getExceptionList to enable or disable "EnableAlertAction"

		YFCElement exceptionInput = YFCDocument.parse("<CountResult/>").getDocumentElement();
		exceptionInput.setAttribute("CountRequestKey",eCount.getAttribute("CountRequestKey"));

		 %>		
		
		<yfc:callAPI apiName="getExceptionList" inputElement="<%=exceptionInput%>"  outputNamespace="ExceptionResultList"/>

		<%
 
		YFCElement eExceptionResultList = (YFCElement) request.getAttribute("ExceptionResultList");
		YFCElement eInbox = (YFCElement) eExceptionResultList.getChildElement("Inbox");
		
		if(!isVoid(eInbox)){
			    eCount.setAttribute("EnableAlertAction", "Y");
			}else {
			    eCount.setAttribute("EnableAlertAction", "");
			}


		sStatus = eCount.getAttribute("Status");
		if(!isVoid(sStatus)) {
			if(sStatus.startsWith("1200")){
				eCount.setAttribute("RecordFlag", true);
			} 
			if(sStatus.startsWith("1100")){
				eCount.setAttribute("TasksCreated", false);
				eCount.setAttribute("TasksCompleted", false);				
			}
			if(sStatus.startsWith("2000") || sStatus.startsWith("9000")){
				eCount.setAttribute("CountRequestNotCompletedOrCancelled", false);
			}			
	     }

		 YFCElement allowedTransElem = eCount.getChildElement("AllowedTransactions");
		 if(allowedTransElem!=null){

			 YFCNodeList allowedTranList = allowedTransElem.getElementsByTagName("Transaction");
			 if(allowedTranList!=null){
				 for(int i=0; i<allowedTranList.getLength(); i++){
					YFCElement allowedTranElem = (YFCElement)allowedTranList.item(i);
					String sTransactionId = allowedTranElem.getAttribute("Tranid");
					if(equals(sTransactionId, "COUNT_VERIFICATION_COMPLETE")){
						eCount.setAttribute("VerifyCountAllowed", true); 
						break;
					}
				 }
				for(int i=0; i<allowedTranList.getLength(); i++){
					YFCElement allowedTranElem = (YFCElement)allowedTranList.item(i);
					String sTransactionId = allowedTranElem.getAttribute("Tranid");
					if(equals(sTransactionId, "ACCEPT_VARIANCE")){

						if(eCount.hasAttribute("VerifyCountAllowed") && equals("Y", eCount.getAttribute("VerifyCountAllowed"))){

							if(eChangeCountRequest!=null && eChangeCountRequest.hasAttribute("CountVerified") && equals("Y", eChangeCountRequest.getAttribute("CountVerified"))){
								// if verification has to be done, dont enable accept variance until verification is complete
								// CR 72590
								eCount.setAttribute("EnableAcceptVariance", true);
								break;
							}else{
								eCount.setAttribute("EnableAcceptVariance", false);
								break;
							}

						}
						else{
							eCount.setAttribute("EnableAcceptVariance", true);
							break;
						}
					}
				 }
			 }
		 }
    }
%>
 <tr>
	<td colspan="3" valign="top">
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I01"/>
		</jsp:include>
	</td>
</tr>
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
<tr>
     <td colspan="3" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I07"/>
        </jsp:include>
    </td>
</tr>
</table>
<%
    }else{
%>
<table class="anchor" cellpadding="7px"  cellSpacing="0">
<tr>
    <td colspan="3" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I05"/>
        </jsp:include>
    </td>
</tr>
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
</table>
<%
    YFCElement eCount = (YFCElement) request.getAttribute("CountRequest");
}%>
