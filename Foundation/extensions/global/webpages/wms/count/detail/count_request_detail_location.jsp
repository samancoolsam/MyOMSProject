<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<table class="view" height="100%" width="100%" cellpadding="7px" cellSpacing="0">
<tr style="display:">
	<td class="detaillabel" >
        <yfc:i18n>Number_Of_Locations</yfc:i18n>
    </td>
    <td nowrap="true">
		<yfc:getXMLValue binding="xml:LocationList:/Locations/@TotalNumberOfRecords" />	
	</td>
</tr>
</table>