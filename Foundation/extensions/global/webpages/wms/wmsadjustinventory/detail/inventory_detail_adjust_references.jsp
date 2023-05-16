<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<table width="100%" class="view">
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Reference_1</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Audit/@Reference1", "")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
        <yfc:i18n>Reference_2</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Audit/@Reference2", "")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
        <yfc:i18n>Reference_3</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Audit/@Reference3", "")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
		<yfc:i18n>Reference_4</yfc:i18n>
	</td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Audit/@Reference4", "")%> />
    </td>
</tr>
<tr>    
    <td class="detaillabel" >
		<yfc:i18n>Reference_5</yfc:i18n>
	</td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/AdjustLocationInventory/Audit/@Reference5", "")%> />
    </td>
</tr>
</table>