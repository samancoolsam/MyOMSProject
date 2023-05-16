<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<table class="anchor" cellpadding="7px"  cellSpacing="0" height="500" width="100%">
<yfc:callAPI apiID='AP1'/>
<div style="overflow:auto">
 <tr height="70%">
    <td>
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I01"/>


        </jsp:include>
    </td>
</tr>
</div>
<yfc:callAPI apiID='AP2'/>
<div style="overflow:auto">
<tr height="30%">
    <td>
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>


        </jsp:include>
    </td>
</tr>
</div>
</table>