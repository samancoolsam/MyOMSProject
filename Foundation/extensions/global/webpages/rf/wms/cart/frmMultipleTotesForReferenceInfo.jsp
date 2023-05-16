<%@ include file="/yfc/rfutil.jspf" %>
<%
	request.setAttribute("xml:/TaskList/@SuggTargetCaseId", getParameter("xml:/Task/@SuggTargetCaseId"));
	request.setAttribute("xml:/Item/@DisplayItemId", getParameter("xml:/TaskList9/Task/Inventory/@ItemId"));
	request.setAttribute("xml:/TaskList/Task/Inventory/@ItemId", getParameter("xml:/TaskList/Task/Inventory/@ItemId"));
	request.setAttribute("xml:/TaskList/Task/Inventory/@SortQty", getParameter("xml:/Task/@SortQty"));
	request.setAttribute("xml:/TaskList/@SuggTotalQuantity", getParameter("xml:/Task/@SuggTotalQuantity"));
	request.setAttribute("xml:/TaskList/@EffectiveTotalQty", getParameter("xml:/Task/@EffectiveTotalQty"));
	request.setAttribute("xml:/Exception1/@ApplyException", getParameter("xml:/Exception/@ApplyException"));
	request.setAttribute("xml:/Exception1/@ExceptionCode", getParameter("xml:/TaskException/@ExceptionCode"));
	if(!isVoid(getParameter("xml:/Task/@UnitOfMeasure"))){
		request.setAttribute("xml:/Task/Inventory/@UnitOfMeasure", getParameter("xml:/Task/@UnitOfMeasure"));
	}else{
		request.setAttribute("xml:/Item/@UnitOfMeasure", getParameter("xml:/Task/Inventory/@UnitOfMeasure"));
	}
	request.setAttribute("xml:/TaskList/Task/@SourceLocationId", getParameter("xml:/Task/@SourceLocationId"));
	request.setAttribute("xml:/Task/@EnterpriseKey", getParameter("xml:/TaskList/Enterprise/@Enterprise"));
	request.setAttribute("xml:/Task/TaskType/@PackWhilePick", getParameter("xml:/Task/TaskType/@PackWhilePick"));
	request.setAttribute("xml:/TaskList/Task/@SourceLocationId", getParameter("xml:/Task/@SourceLocationId"));
	request.setAttribute("xml:/DisplayPurpose/CartPick/Sort/@LastScannedToteId", getParameter("xml:/DisplayPurpose/CartPick/Sort/@LastScannedToteId"));
	String formName = "/frmMultipleTotesForReferenceInfo";
	out.println(sendForm(formName,"Back")) ;
%>
