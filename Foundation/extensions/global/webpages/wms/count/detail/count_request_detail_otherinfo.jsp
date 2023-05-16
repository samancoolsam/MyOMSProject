<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/taskmanagement.js"></script>
<%  	
	YFCDate highDate = new YFCDate().HIGH_DATE;
	YFCDate nowDate = new YFCDate();		
%>

<table width="100%" class="view">
<%if(!isVoid(resolveValue("xml:/CountRequest/@CountRequestKey"))){%>
<tr>
	<td class="detaillabel" ><yfc:i18n>Priority</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/Priority/@Description" />
        <input type="hidden"  
          <%=getTextOptions("xml:/CountRequest/Priority/@Description","xml:/CountRequest/Priority/@Description")%> />
    </td>
	
	<td class="detaillabel" ><yfc:i18n>Requesting_User</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/RequestingUser/@UserName" />
        <input type="hidden" <%=getTextOptions("xml:/CountRequest/RequestingUser/@UserName","xml:/CountRequest/RequestingUser/@UserName")%> />
    </td>
</tr>
	
<tr>
	<td class="detaillabel" ><yfc:i18n>Start_No_Earlier_Than</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@StartNoEarlierThan" />
        <input type="hidden"  
          <%=getTextOptions("xml:/CountRequest/@StartNoEarlierThan",
			 "xml:/CountRequest/@StartNoEarlierThan")%> />
    </td>

	<td class="detaillabel" ><yfc:i18n>Finish_No_Later_Than</yfc:i18n></td>
    <td class="protectedtext" nowrap="true">
		<yfc:getXMLValue binding="xml:/CountRequest/@FinishNoLaterThan" />
        <input type="hidden"  
          <%=getTextOptions("xml:/CountRequest/@FinishNoLaterThan","xml:/CountRequest/@FinishNoLaterThan")%> />
    </td>
</tr>
<%}else{%>
<yfc:callAPI apiID="AP1"/>
<tr>
	 <td class="detaillabel" ><yfc:i18n>Priority</yfc:i18n></td>
	<td nowrap="true">
        <select name="xml:/CountRequest/@Priority" class="combobox">
                <yfc:loopOptions binding="xml:Priority:/CommonCodeList/@CommonCode" 
                    name="CodeShortDescription" isLocalized="Y" value="CodeValue" selected="3"/>
        </select>
    </td>
	<td class="detaillabel" >
        <yfc:i18n>Requesting_User_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/CountRequest/@RequestingUserId","xml:CurrentUser:/User/@Loginid")%> />
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Start_No_Earlier_Than</yfc:i18n>
    </td>
	<td nowrap="true" >
		<input class="dateinput" type="text"
		<%=getTextOptions("xml:/CountRequest/@StartNoEarlierThan_YFCDATE","xml:/CountRequest/@StartNoEarlierThan_YFCDATE",nowDate.getString(getLocale(), false))%>/>
		<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@StartNoEarlierThan_YFCTIME", "xml:/CountRequest/@StartNoEarlierThan_YFCTIME",nowDate.getString(getLocale().getTimeFormat()))%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %> />
		</td>	
	<td class="detaillabel" >
        <yfc:i18n>Finish_No_Later_Than</yfc:i18n>
    </td>
    <td nowrap="true" >
        <input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@FinishNoLaterThan_YFCDATE","xml:/CountRequest/@FinishNoLaterThan_YFCDATE")%>/>
        <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/CountRequest/@FinishNoLaterThan_YFCTIME","xml:/CountRequest/@FinishNoLaterThan_YFCTIME")%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
	</td>
</tr>
<%}%>
</table>
 