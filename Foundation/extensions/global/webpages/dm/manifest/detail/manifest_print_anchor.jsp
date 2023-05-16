<%@ include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/documentprint.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
	window.attachEvent("onload",printDocument);
</script>
<table class="anchor" cellpadding="7px" cellSpacing="0">
<tr>
	<td align="right">
		<jsp:include page="/console/jsp/printDocumentButtons.jsp" flush="true"/>
	</td>
</tr>
<tr>
    <td>
        <jsp:include page="/dm/manifest/detail/manifest_detail_document.jsp" flush="true"/>
    </td>
</tr>
</table>