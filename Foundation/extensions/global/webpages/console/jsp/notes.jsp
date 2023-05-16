<%
/* 
 * Licensed Materials - Property of IBM
 * IBM Sterling Selling and Fulfillment Suite
 * (C) Copyright IBM Corp. 2001, 2013 All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.ui.backend.util.HTMLEncode" %>

<%
    String allowedBinding = getParameter("allowedBinding");
    String getBinding = getParameter("getBinding");
    String saveBinding = getParameter("saveBinding");

	String isWorkOrderNotesPopup = getParameter("isWorkOrderNotesPopup");
	if (isVoid(isWorkOrderNotesPopup)) {
		isWorkOrderNotesPopup = "N";
	}

	YFCElement curUsr = (YFCElement)session.getAttribute("CurrentUser");
	String loggedInUser = curUsr.getAttribute("Loginid");

	String notesListElemName = "Notes";
	String notesElemName = "Note";

	if(isWorkOrderNotesPopup.equalsIgnoreCase("Y")){
		notesListElemName = "WorkOrderNotesList";
		notesElemName = "WorkOrderNotes";
	}

	String notesElemPath = HTMLEncode.htmlEscape(saveBinding) + "/"+ notesListElemName + "/" + notesElemName +"/";
	String notesListLoopRootElem = getBinding+"/"+ notesListElemName+"/@"+notesElemName; 
	String notesListLoopElem = "xml:/"+notesElemName+"/";
%>

<script language=jscript src="<%=request.getContextPath()%>/console/scripts/om.js">
</script>
<script language=jscript>
	window.document.body.attachEvent("onunload", processSaveRecordsForNotes);
</script>

<table width="100%" cellspacing="0" cellpadding="0" border="0" >
    <tr>
        <td valign="top" >
            <table class="view" width="100%" ID="Notes">
			<tr>
				<td class="detaillabel" >
					<yfc:i18n>Contact_Time</yfc:i18n>
				</td>
				
				<td nowrap="true">
					<input class="dateinput" type="text" <%=yfsGetTextOptions(notesElemPath+"@ContactTime_YFCDATE", getTodayDate(), allowedBinding )%>/>
					<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YCPUIBackendConsts.getUIIconPath()+YCPUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
					<input class="dateinput" type="text" <%=yfsGetTextOptions(notesElemPath+"@ContactTime_YFCTIME", getCurrentTime(), allowedBinding)%>/>
					<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YCPUIBackendConsts.getUIIconPath()+YCPUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
				</td>
				<td class="detaillabel">
					<yfc:i18n>Contact_User</yfc:i18n>
				</td>
				<td>
					<input type="text" class="unprotectedinput" <%=yfsGetTextOptions(notesElemPath+"@ContactUser", loggedInUser, allowedBinding) %> />
				</td>
				<td class="detaillabel" ><yfc:i18n>Reason_Code</yfc:i18n></td>
				<td colspan="1">
					<select <%=yfsGetComboOptions(notesElemPath+"@ReasonCode", "", allowedBinding)%> class="combobox" >
						<yfc:loopOptions binding="xml:NotesReasonCodeList:/CommonCodeList/@CommonCode" 
							name="CodeShortDescription" value="CodeValue" isLocalized="Y"/>
					</select>
				</td>
			</tr>
			<tr>
				<td class="detaillabel" ><yfc:i18n>Contact_Type</yfc:i18n></td>
				<td colspan="1">
				<select <%=yfsGetComboOptions(notesElemPath+"@ContactType", "", allowedBinding)%> class="combobox" >
					<yfc:loopOptions binding="xml:ContactTypeList:/CommonCodeList/@CommonCode" 
						name="CodeShortDescription" value="CodeValue" isLocalized="Y" />
				</select>
				</td>
				<td class="detaillabel" >
					<yfc:i18n>Contact_Reference</yfc:i18n>
				</td>
				<td >
					<input type="text" class="unprotectedinput" <%=yfsGetTextOptions(notesElemPath+"@ContactReference", "", allowedBinding)%> />
				</td>
			</tr>
            <tr>
                <td ><yfc:i18n>Add_Note</yfc:i18n>
            </tr>
            <tr>
                <td rowspan="3" colspan="6" width="100%" >
                        <textarea rows="3"  class="unprotectedtextareainput" style="width:100%" <%=yfsGetTextAreaOptions(notesElemPath+"@NoteText", "", allowedBinding)%>></textarea>
                </td>
            </tr>
            </table>
        </td>
    </tr>
    <tr>
    <td valign="top">
        <table class="table" width="100%" cellspacing="0" >
        <thead>
        <tr>
            <td class="tablecolumnheader" nowrap="true"><yfc:i18n>Date</yfc:i18n></td>
            <td class="tablecolumnheader" nowrap="true"><yfc:i18n>User</yfc:i18n></td>
			<td class="tablecolumnheader" nowrap="true"><yfc:i18n>Reason</yfc:i18n></td>
			<td class="tablecolumnheader" nowrap="true"><yfc:i18n>Contact_Type</yfc:i18n></td>
			<td class="tablecolumnheader" nowrap="true"><yfc:i18n>Contact_Reference</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Notes</yfc:i18n></td>
        </tr>   
        <tr>
            <td>
                <input type="hidden" name="allowedBinding" value='<%=HTMLEncode.htmlEscape(allowedBinding)%>' />
                <input type="hidden" name="getBinding" value='<%=HTMLEncode.htmlEscape(getBinding)%>' />
                <input type="hidden" name="saveBinding" value='<%=HTMLEncode.htmlEscape(saveBinding)%>' /> 
				<input type="hidden" name="isWorkOrderNotesPopup" value='<%=HTMLEncode.htmlEscape(isWorkOrderNotesPopup)%>' />
            </td>
        </tr>
        </thead>
        <tbody>
            <yfc:loopXML binding='<%=notesListLoopRootElem%>' id="Notes">
            <tr>
				<td class="tablecolumn"><yfc:getXMLValue binding='<%=buildBinding("xml:Notes:/"+notesElemName,"/@ContactTime","")%>'/></td>
                <td class="tablecolumn"><yfc:getXMLValue binding='<%=buildBinding("xml:Notes:/"+notesElemName,"/@ContactUser", "")%>' /></td>
                <td class="tablecolumn"><yfc:getXMLValue binding='<%=buildBinding("xml:Notes:/"+notesElemName,"/@ReasonCode", "")%>' /></td>
				<td class="tablecolumn">
					<%=getComboText("xml:ContactTypeList:/CommonCodeList/@CommonCode" ,"CodeShortDescription" ,"CodeValue" ,buildBinding("xml:Notes:/"+notesElemName,"/@ContactType",""),true)%>
				</td>
				<td class="tablecolumn"><yfc:getXMLValue binding='<%=buildBinding("xml:Notes:/"+notesElemName,"/@ContactReference", "")%>' /></td>
                <td class="tablecolumn">
					<textarea class="protectedtextareainput" contenteditable="false" rows="3" style="width:100%" ><yfc:getXMLValue binding='<%=buildBinding("xml:Notes:/"+notesElemName,"/@NoteText", "")%>' /></textarea>
				</td>
            </tr>
            </yfc:loopXML> 
        </tbody>
        </table>
    </td>
    </tr>
</table>