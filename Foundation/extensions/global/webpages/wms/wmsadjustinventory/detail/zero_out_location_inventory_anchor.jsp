<%@include file="/yfsjspcommon/yfsutil.jspf"%>



<table class="anchor" cellpadding="7px"  cellSpacing="0" >

<tr>

<td colspan="3" valign="top">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>        
		    <jsp:param name="getRequestDOM" value="Y"/>
		    <jsp:param name="RootNodeName" value="ZeroOutLocationInventory"/>		   
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