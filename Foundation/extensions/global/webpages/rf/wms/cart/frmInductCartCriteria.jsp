<%@ include file="/yfc/rfutil.jspf" %>
<%@ include file="/console/jsp/rftasktypeutils.jspf" %>

<%
	String formName="/frmInductCartCriteria";
	String sMandateBatch = getParameter("MandateBatch");
	String sStartPick = getParameter("StartPick");
	String sShowDepositScreen = getParameter("ShowDepositScreen");

	HashMap attrMp=(HashMap)TaskTypeGroupFormNameMap.get("INDUCT_CART");
	request.setAttribute("Entity", (String)attrMp.get("Entity"));

	YFCDocument ydoc = getForm(formName);
	if(isVoid(sMandateBatch)){
		if(isVoid(getParameter("xml:/InductCart/@MandateBatch"))){
			sMandateBatch = "Y";
		}else{
			sMandateBatch = getParameter("xml:/InductCart/@MandateBatch");
		}
	}
	if(isVoid(sStartPick) && isVoid(getParameter("xml:/InductCart/@StartPick"))){
		if(isVoid(getParameter("xml:/InductCart/@StartPick"))){
			sStartPick = "Y";
		}else{
			sStartPick = getParameter("xml:/InductCart/@StartPick");
		}
	}
	try{
		deleteAllFromTempQ("ShowDepositScreen");
	}catch(Exception ex){}
	if(!isVoid(sShowDepositScreen)){
		try{
			addToTempQ("ShowDepositScreen", sShowDepositScreen, false);
		}catch(Exception ex){}				
	}

	request.setAttribute("xml:/InductCart/@MandateBatch", sMandateBatch);
	request.setAttribute("xml:/InductCart/@StartPick", sStartPick);

	if("N".equals(sMandateBatch)){
		YFCElement lblBatchElem = getField(ydoc,"lblBatchNo");
		if(lblBatchElem!=null){
			lblBatchElem.setAttribute("type","hidden");
			lblBatchElem.setAttribute("subtype","Hidden");
			lblBatchElem.setAttribute("value","");
		}

		YFCElement txtBatchElem = getField(ydoc,"txtBatchNo");
		if(txtBatchElem!=null){
			txtBatchElem.setAttribute("type","hidden");									
			txtBatchElem.setAttribute("subtype","Hidden");
		}
	}	
	out.println(sendForm(ydoc, "txtCartId", true)) ;
%>
			
