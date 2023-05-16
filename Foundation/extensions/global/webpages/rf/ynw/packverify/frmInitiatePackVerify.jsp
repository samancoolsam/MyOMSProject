<%@ include file="/yfc/rfutil.jspf" %>

<%	
	String formName = "/frmLocationEntry" ;
	String focusField = "txtLocationId" ;
	String sConfirmCntrPresence = getParameter("ConfirmContainerPresence");
	String sVerifyTotalQty = getParameter("VerifyTotalQty");
	String sVerifyItemCount = getParameter("VerifyItemCount");
	String sDetailVerification = getParameter("DetailVerification");
	String sStopOnFirstMismatch = getParameter("StopOnFirstMismatch");
	String sValidateCntrPickUpStatus = getParameter("ValidateContainerPickUpStatus");

	clearTempQ();

	YFCElement criteriaElem = YFCDocument.createDocument("VerifcationCriteria").getDocumentElement();
	criteriaElem.setAttribute("ConfirmContainerPresence", sConfirmCntrPresence);
	criteriaElem.setAttribute("VerifyTotalQty", sVerifyTotalQty);
	criteriaElem.setAttribute("VerifyItemCount", sVerifyItemCount);
	criteriaElem.setAttribute("DetailVerification", sDetailVerification);
	criteriaElem.setAttribute("StopOnFirstMismatch", sStopOnFirstMismatch);
	criteriaElem.setAttribute("ValidateContainerPickUpStatus", sValidateCntrPickUpStatus);

	addToTempQ("VerifcationCriteria", "VerifcationCriteria", criteriaElem,false);

	if(!"Y".equals(sConfirmCntrPresence)){
		formName = "/frmContainerEntry" ;
		focusField = "txtContainerSCM" ;
	}

	out.println(sendForm(formName, focusField)) ;
%>
