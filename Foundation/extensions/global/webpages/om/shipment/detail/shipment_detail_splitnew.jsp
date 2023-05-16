<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>

<table width="100%" class="view">
<tr>    
    <td class="detaillabel">
        <yfc:i18n>Shipment_#</yfc:i18n>
    </td>
    <td >
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/SplitShipment/@NewShipmentNo")%>></input>
    </td>
</tr>
<tr></tr>
<tr>
    <td align="right">
		<input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setSplitOKClickedAttribute();return false;"/>
	</td>
	<td>
        <input type="button" class="button" value='<%=getI18N("Cancel")%>' onclick="window.close();"/>
   </td>
</tr>
</table>