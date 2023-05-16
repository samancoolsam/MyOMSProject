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
<table class="table" editable="false" width=100%>
<tbody>
    <tr>
		<td width="10%">
			&nbsp;
		</td>
		<td width="90%" style="border:1px solid black">
			<table class="table" editable="true" width="100%" cellspacing="0">
				<tbody>
				<yfc:loopXML binding="xml:/Inbox/InboxReferencesList/@InboxReferences" id="InboxReferences">
				<%
					String sLineNo=getParameter("optionSetBelongingToLine");
					Integer myInteger=new Integer(Integer.parseInt(sLineNo));
					if (equals(myInteger, InboxReferencesCounter)){
						String referenceValue = getValue("InboxReferences","xml:/InboxReferences/@Value");
						YFCElement oLogMsgElem = getLogMessageElement(referenceValue);
						if(!isVoid(oLogMsgElem)) {
							for (Iterator itr = oLogMsgElem.getChildren();itr.hasNext();) {
								YFCElement oLogMsgDtlsElem = (YFCElement)itr.next();
								%>
								<tr>
									<td class="tablecolumn"><yfc:i18n><%=oLogMsgDtlsElem.getAttribute("Key")%></yfc:i18n></td>
									<td class="tablecolumn"><yfc:i18n><%=oLogMsgDtlsElem.getAttribute("Value")%></yfc:i18n></td>
								</tr>
								<%
							}
						}
					}
				%>
				</yfc:loopXML>
				</tbody>
			</table>
		</td>
	</tr>
</tbody>
</table>

<%!
public YFCElement getLogMessageElement(String referenceValue) {
	YFCElement oLogMsgElem = null;
	//System.out.println("\nreferenceValue before decode : "+referenceValue);
	referenceValue = com.yantra.yfc.ui.backend.util.HTMLEncode.htmlUnescape(referenceValue);
	//System.out.println("\nreferenceValue after decode : "+referenceValue);
	try {		
		oLogMsgElem = YFCDocument.parse(referenceValue).getDocumentElement();
	}catch (Exception e){
		e.printStackTrace();
	}	
	return oLogMsgElem;
}
%>
