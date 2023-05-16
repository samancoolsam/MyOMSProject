<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsimpopup.js"></script>
<script language="javascript">
    var myObject = new Object();
    myObject = dialogArguments;
    var parentWindow = myObject.currentWindow;

    function setOKClickedAttribute() {
		var StartNoEarlierThan=document.all["xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCDATE"].value+' '+document.all["xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCTIME"].value;
		var FinishNoLaterThan=document.all["xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCDATE"].value+' '+document.all["xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCTIME"].value;
		
		window.dialogArguments["OKClicked"] = "true";
		window.dialogArguments["xml:/NodeInventory/CountRequest/@StartNoEarlierThan"] = StartNoEarlierThan;
		window.dialogArguments["xml:/NodeInventory/CountRequest/@FinishNoLaterThan"] = FinishNoLaterThan;
		window.dialogArguments["xml:/NodeInventory/CountRequest/@CountRequestNo"] = document.all["xml:/NodeInventory/CountRequest/@CountRequestNo"].value;
		window.dialogArguments["xml:/NodeInventory/CountRequest/@Priority"] = document.all["xml:/NodeInventory/CountRequest/@Priority"].value;
		window.dialogArguments["xml:/NodeInventory/CountRequest/@RequestingUserId"] = document.all["xml:/NodeInventory/CountRequest/@RequestingUserId"].value;
        window.close();
		return true;
    }
</script>
<table width="100%" class="view">
	<%if(!equals("Y",resolveValue("xml:/Dummy/@DisableNo"))){%>
		<tr>
			<td class="detaillabel" >
				<yfc:i18n>Count_Request_#</yfc:i18n>
			</td>
			<td nowrap="true">
				<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/NodeInventory/CountRequest/@CountRequestNo")%> />
			</td>
		</tr>
		<%}else{%>
		<input type="hidden" name="xml:/NodeInventory/CountRequest/@CountRequestNo"/>
	<%}%>

<tr>
	<td class="detaillabel" ><yfc:i18n>Priority</yfc:i18n></td>
	<td nowrap="true">
        <select name="xml:/NodeInventory/CountRequest/@Priority" class="combobox">
                <yfc:loopOptions binding="xml:Priority:/CommonCodeList/@CommonCode" 
                    name="CodeShortDescription" isLocalized="Y" value="CodeValue" selected="3"/>
        </select>
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Requesting_User_ID</yfc:i18n>
    </td>
    <td nowrap="true">
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/NodeInventory/CountRequest/@RequestingUserId","xml:CurrentUser:/User/@Loginid")%> />
		<img class="lookupicon" onclick="callLookup(this,'user','xml:/User/@Node=<%=resolveValue("xml:/NodeInventory/@Node")%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_User") %> />
    </td>
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Start_No_Earlier_Than</yfc:i18n>
    </td>
	<td nowrap="true" >
        <input class="dateinput" type="text" <%=getTextOptions("xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCDATE","xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCDATE",getTodayDate())%>/>
        <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCTIME","xml:/NodeInventory/CountRequest/@StartNoEarlierThan_YFCTIME",getCurrentTime() )%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
	</td> 
</tr>
<tr>
	<td class="detaillabel" >
        <yfc:i18n>Finish_No_Later_Than</yfc:i18n>
    </td>
    <td nowrap="true" >
        <input class="dateinput" type="text" <%=getTextOptions("xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCDATE","xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCDATE","")%>/>
        <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
		<input class="dateinput" type="text" <%=getTextOptions("xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCTIME","xml:/NodeInventory/CountRequest/@FinishNoLaterThan_YFCTIME","")%>/>
		<img class="lookupicon" name="search" onclick="invokeTimeLookup(this);return false" <%=getImageOptions(YFSUIBackendConsts.TIME_LOOKUP_ICON, "Time_Lookup") %>/>
	</td>
</tr>	
<tr>
    <td align="right">
		<input type="button" class="button" value='<%=getI18N("__OK__")%>' onclick="setOKClickedAttribute();return true;"/>
	</td>
	<td>
        <input type="button" class="button" value='<%=getI18N("Cancel")%>' onclick="window.close();"/>
   </td>
</tr>
</table>