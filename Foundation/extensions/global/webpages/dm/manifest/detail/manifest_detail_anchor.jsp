<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%
	YFCElement elemManifest = (YFCElement) request.getAttribute("Manifest");
	if(elemManifest!=null){
		elemManifest.setAttribute("ManifestOpen",true);
		if(equals("Y",elemManifest.getAttribute("ManifestClosedFlag")))
			elemManifest.setAttribute("ManifestOpen",false);
	}
%>
<table class="anchor" height="100%" width="100%">
 <tr height="40%">
	<table width="100%" >
	<tr>
    <td width="45%" height="100%" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>
        </jsp:include>
    </td>
	<td width="55%" height="100%" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
        </jsp:include>
    </td>
	</tr>
	</table>
</tr>
<tr height="50%">
	<table width="100%" height="100%" cellspacing=0 cellpadding=3>
	<tr>
	<td width="45%" style="padding-right:1px;">
		<table height="100%" width="100%" cellspacing=0 cellpadding=0>
		<tr style="padding-bottom:3px;">
			<td>
				<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
					<jsp:param name="CurrentInnerPanelID" value="I03"/>
					
				</jsp:include>
			</td>
		</tr>
		<tr >
			<td>
				<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
					<jsp:param name="CurrentInnerPanelID" value="I04"/>
					<jsp:param name="IPHeight" value="150"/>
				</jsp:include>
			</td>
		</tr>
		</table>
	</td>
	<td width="55%" >
		<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			<jsp:param name="CurrentInnerPanelID" value="I05"/>
		</jsp:include>

	</td>
	</tr>
	</table>

</tr>

</table>
