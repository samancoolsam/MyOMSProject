<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript">
	yfcDoNotPromptForChanges(true);
</script>
<script language="javascript">
	function onEnterpriseChange(sAccountNo){
		document.all("xml:SCACAccountList:/NodeSCACAccounts/NodeSCACAccount/@ShipperAccountNo").value =document.all(sAccountNo).value;
	}
</script>
<%  	
	YFCDate highDate = new YFCDate().HIGH_DATE;
	YFCDate nowDate = new YFCDate();	

%>
 <table class="view" width="100%">	
	<tr><td class="detaillabel"/><td class="detaillabel"/><td class="detaillabel"/><td/></tr>
	<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="ShowEnterpriseCode" value="false"/>
		<jsp:param name="ShowNode" value="true"/> 		
		<jsp:param name="NodeBinding" value="xml:/Manifest/@ShipNode"/>
        <jsp:param name="RefreshOnNode" value="true"/>        
	</jsp:include>


<%
	YFCElement tempElem = null;
	YFCElement elemNodeScac = YFCDocument.createDocument("NodeSCACAccount").getDocumentElement();
	elemNodeScac.setAttribute("ShipNode", resolveValue("xml:CommonFields:/CommonFields/@Node"));
	elemNodeScac.setAttribute("SCAC", resolveValue("xml:/Manifest/@Scac"));
	if(!isVoid(resolveValue("xml:CommonFields:/CommonFields/@Node")))
	{
%>
		<yfc:callAPI apiName="getNodeSCACAccountList" inputElement="<%=elemNodeScac%>" 
		templateElement="<%=tempElem%>" outputNamespace="SCACAccountList"/>
<%
	}	
%>

	<tr>		
		<input type="hidden" name="xml:/Manifest/@Scac" value='<%=resolveValue("xml:/Manifest/@Scac")%>'/>

		<td class="detaillabel" ><yfc:i18n>Manifest_#</yfc:i18n></td>		
		<td>
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Manifest/@ManifestNo") %> />			
		</td>			
		<td class="detaillabel" ><yfc:i18n>Manifest_Date</yfc:i18n></td>
		<td>
			<input class="dateinput" type="text" <%=getTextOptions("xml:/Manifest/@ManifestDate_YFCDATE","xml:/Manifest/@ManifestDate_YFCDATE",nowDate.getString(getLocale().getDateFormat()))%>/>
			<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
        </td>
	</tr>
	<tr>		
		<td class="detaillabel" ><yfc:i18n>Trailer_#</yfc:i18n></td>		
		<td>
			<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Manifest/@TrailerNo") %> />			
		</td>			
		<td></td><td></td>
	 </tr>
	 <tr></tr>
	 <tr>
		<td class="detaillabel" > <yfc:i18n>Enterprise</yfc:i18n> </td>		
		<td>			
			<select name="xml:/Manifest/@ShipperAccountNo" class="combobox" 
			onchange="onEnterpriseChange('xml:/Manifest/@ShipperAccountNo')" >
		        <yfc:loopOptions binding="xml:SCACAccountList:/NodeSCACAccounts/@NodeSCACAccount" name="Enterprise" value="ShipperAccountNo" suppressBlank="Y" selected="xml:/Manifest/@ShipperAccountNo"/>
	        </select>
		</td>
		
		<td class="detaillabel" ><yfc:i18n>Shipper_Account_#_For_The_Selected_Enterprise</yfc:i18n></td>		
		<td class="protectedtext">
			<input class="protectedtext" <%=getTextOptions("xml:SCACAccountList:/NodeSCACAccounts/NodeSCACAccount/@ShipperAccountNo")%> />
		</td>
	 </tr>
</table>