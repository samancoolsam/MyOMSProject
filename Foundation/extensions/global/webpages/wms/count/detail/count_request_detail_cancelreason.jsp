<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/cancelreasonpopup.js"></script>
<%if(isShipNodeUser()){%>
<yfc:callAPI apiID="AP1"/>
<%}else if(isVoid(resolveValue("xml:/CountProgram/@EnterpriseCode"))){%>
<yfc:callAPI apiID="AP2"/>
<%}else{%>
<yfc:callAPI apiID="AP3"/>
<%}%>
<table width="100%" class="view">
    <tr>
        <td align="right">
            <yfc:i18n>Cancellation_Reason_Code</yfc:i18n>
        </td>
        <td>
            <select name="xml:/ModificationReason/@ReasonCode" class="combobox">
                <yfc:loopOptions binding="xml:ReasonCodeList:/CommonCodeList/@CommonCode" 
                    name="CodeShortDescription" isLocalized="Y" value="CodeValue"/>
            </select>
        </td>
	</tr>	
	<tr>
        <td align="right">
            <yfc:i18n>Reason_Text</yfc:i18n>
        </td>
        <td>
            <textarea class="unprotectedtextareainput" rows="3" cols="50" <%=getTextAreaOptions("xml:/ModificationReason/@ReasonText")%>></textarea>
        </td>
    </tr>

	<tr>
		<td></td>
		<td align="right">
			<input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setOKClickedAttribute();return true;"/>
		</td>
		
	</tr>
</table>