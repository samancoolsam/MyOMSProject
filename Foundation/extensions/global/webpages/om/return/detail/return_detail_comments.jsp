<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="../console/scripts/om.js"></script>

<%
String bindingNode = request.getParameter("BindingNode");
if (bindingNode==null || "".equals(bindingNode)) 
	bindingNode="xml:/Order";
String ins = bindingNode+"/Instructions/@Instruction";
%>

<input type="hidden" <%=getTextOptions(bindingNode+"/@OrderHeaderKey", bindingNode+"/@OrderHeaderKey")%>/>

<table class="table" width="100%" cellspacing="0" <%if (isModificationAllowed("xml:/@AddInstruction",bindingNode+"/AllowedModifications")) {%> initialRows="3" <%}%>>
<thead>
    <tr>
        <td class="checkboxheader" sortable="no">&nbsp;</td>
        <td class="tablecolumnheader" sortable="no"><yfc:i18n>Date</yfc:i18n></td>
        <td class="tablecolumnheader" sortable="no"><yfc:i18n>User</yfc:i18n></td>
        <td class="tablecolumnheader" sortable="no"><yfc:i18n>Tran_Name</yfc:i18n></td>
        <td class="tablecolumnheader" sortable="no"><yfc:i18n>Text</yfc:i18n></td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML binding="<%=ins%>" id="Instruction">
        <tr>
	        <td class="checkboxcolumn">&nbsp;</td>
			<td><input type="text" class="protectedinput" <%=getTextOptions("xml:/Instruction/@CreateTS")%>/></td>
			<td><input type="text" class="protectedinput" <%=getTextOptions("xml:/Instruction/@User")%>/></td>
			<td><input type="text" class="protectedinput" <%=getTextOptions("xml:/Instruction/@TranName")%>/></td>
            <td class="tablecolumn">
                <table class="view" cellspacing="0" cellpadding="0">
                    <tr>
                        <td>
                            <textarea class="protectedtextareainput" rows="2" cols="80" <%=yfsGetTextAreaOptions(bindingNode+"/Instructions/Instruction_" + InstructionCounter + "/@InstructionText","xml:/Instruction/@InstructionText", bindingNode+"/AllowedModifications")%>><yfc:getXMLValue binding="xml:/Instruction/@InstructionText"/></textarea>
                        </td>
                    </tr>
                    <tr>
                        <td>
							<img align="absmiddle" <%=getImageOptions(YFSUIBackendConsts.INSTRUCTION_URL, "Instruction_URL")%>/>
                            <input type="text" <%=yfsGetTextOptions(bindingNode+"/Instructions/Instruction_" + InstructionCounter + "/@InstructionURL", "xml:/Instruction/@InstructionURL",bindingNode+"/AllowedModifications")%>/>
                            <input type="button" class="button" value="GO" onclick="javascript:goToURL('<%=bindingNode%>/Instructions/Instruction_<%=InstructionCounter%>/@InstructionURL');"/>
                        </td>
                        <td>
                            <input type="hidden" <%=getTextOptions(bindingNode+"/Instructions/Instruction_" + InstructionCounter + "/@InstructionDetailKey", "xml:/Instruction/@InstructionDetailKey")%>/>
                        </td>
                    </tr>
                     <tr>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                    </tr>
                </table>
            </td>
        </tr>
    </yfc:loopXML> 
</tbody>
<tfoot>
    <tr style='display:none' TemplateRow="true">
        <td class="checkboxcolumn">&nbsp;</td>
		<td><input type="text" class="protectedinput" <%=getTextOptions(bindingNode+"/Instructions/Instruction/@Date")%>/></td>
		<td><input type="text" class="protectedinput" <%=getTextOptions(bindingNode+"/Instructions/Instruction/User")%>/></td>
		<td><input type="text" class="protectedinput" <%=getTextOptions(bindingNode+"/Instructions/Instruction/@TranName")%>/></td>
        <td class="tablecolumn">
            <table class="view" cellspacing="0" cellpadding="0">
                <td>
                    <textarea rows="3" cols="80" <%=yfsGetTemplateRowOptions(bindingNode+"/Instructions/Instruction_/@InstructionText", bindingNode+"/AllowedModifications", "ADD_INSTRUCTION", "textarea")%>></textarea>
                </td>
                    <tr>
                        <td>
							<img align="absmiddle" <%=getImageOptions(YFSUIBackendConsts.INSTRUCTION_URL, "Instruction_URL")%>/>
                            <input type="text" <%=yfsGetTemplateRowOptions(bindingNode+"/Instructions/Instruction_/@InstructionURL", bindingNode+"/AllowedModifications", "ADD_INSTRUCTION", "text")%>/>
                        </td>

						<!--
                        <td>
                            <input type="hidden" <%=getTextOptions(bindingNode+"/Instructions/Instruction_/@InstructionDetailKey", "xml:/Instruction/@InstructionDetailKey")%>/>
                        </td>
						-->
                    </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
    <%if (isModificationAllowed("xml:/@AddInstruction",bindingNode+"/AllowedModifications")) {%> 
    <tr>
    	<td nowrap="true" colspan="5">
    		<jsp:include page="/common/editabletbl.jsp" flush="true"/>
    	</td>
    </tr>
    <%}%>
</tfoot>
</table>
