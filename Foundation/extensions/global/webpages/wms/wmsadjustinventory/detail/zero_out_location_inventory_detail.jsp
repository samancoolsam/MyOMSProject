<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/wmsim.js"></script>
<script language="javascript">

function proceedClicked2(obj) 
{
  
   if(!(document.all("xml:/ZeroOutLocationInventory/@Node").value))
	{
		alert(YFCMSG077); //Node not Passed
		doNormalWindow();
		
	}
	
	else if(!(document.all("xml:/ZeroOutLocationInventory/Source/@LocationId").value))
	{
		//Location  must be Passed
		alert(YFCMSG138);
		doNormalWindow();
		
	}
	
	else
	{
		
		var tmp = document.all("hidPrepareEntityKey");
	    tmp.value = "Y";
		
		if(validateControlValues()){
			
        yfcChangeDetailView(getCurrentViewId());
		}
	}
}
function doNormalWindow()
{
	
    var sVal = document.all("myEntityKey");
	
    var tmp = '<%=getParameter("hidPrepareEntityKey")%>';
	
    var oError = document.all("isError");
	
    if(tmp == "Y" && oError.value == "N")
    {
        oError.value="Y";
		
        showDetailFor(sVal.value);  
    }
}
</script>

<% 


	boolean locAPIcalled= false;	
	boolean bValidLocation = true;
	
	if(equals(getParameter("hidPrepareEntityKey"),"Y")) { %>
	

		<%	if (!isVoid(resolveValue("xml:/ZeroOutLocationInventory/Source/@LocationId"))) { %>
				<yfc:callAPI apiID='AP1'/>
<%				locAPIcalled = true;
				String locVal = getValue("Location","xml:/Locations/Location/@LocationId");
				 %>
				
				<%if (isVoid(locVal)) {
					bValidLocation = false; %>
					<script>
						alert(YFCMSG082);//Invalid Location
					</script>
<%				}
			}%>
			<% if(bValidLocation && locAPIcalled){%>
			<script>
				window.attachEvent("onload",doNormalWindow);
				</script>
				<% } %>
			
		<%} %>

	
<table width="100%" class="view">

	<yfc:makeXMLInput name="MyEntityKey">
			<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/@Node" value="xml:/ZeroOutLocationInventory/@Node"/>
			<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/@EnterpriseCode" value="xml:/ZeroOutLocationInventory/@EnterpriseCode"/>
			<yfc:makeXMLKey binding="xml:/ZeroOutLocationInventory/Source/@LocationId" value="xml:/ZeroOutLocationInventory/Source/@LocationId"/>			
	</yfc:makeXMLInput>
    <input type="hidden" name="myEntityKey" value='<%=getParameter("MyEntityKey")%>' />
	<input type="hidden" name="xml:/Location/LocationType" value='<%=getParameter("locationType")%>' />
<jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
		<jsp:param name="ScreenType" value="detail"/>
		<jsp:param name="ShowDocumentType" value="false"/>
		<jsp:param name="ShowNode" value="true"/>
		<jsp:param name="EnterpriseCodeBinding" value="xml:/ZeroOutLocationInventory/@EnterpriseCode"/>
		<jsp:param name="NodeBinding" value="xml:/ZeroOutLocationInventory/@Node"/>
        <jsp:param name="RefreshOnNode" value="true"/>
		 <jsp:param name="RefreshOnEnterpriseCode" value="false"/>
        <jsp:param name="EnterpriseListForNodeField" value="true"/>
</jsp:include>


<tr>
	<td class="detaillabel" >
        <yfc:i18n>Location</yfc:i18n>
    </td>
    <td nowrap="true">
		<input type="text" class="unprotectedinput" <%=getTextOptions("xml:/ZeroOutLocationInventory/Source/@LocationId","xml:/ZeroOutLocationInventory/Source/@LocationId")%> />       		 
		  <img class="lookupicon" name="search" onclick="callListLookup(this,'location','&xml:/Location/@Node=<%=resolveValue("xml:/CommonFields/@Node")%>&xml:/Location/@LocationType=<%=getParameter("locationType")%>')" 
		<%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Location") %> />
    </td>
    
</tr>
<tr>
<td>
</td>
</tr>
<tr>
    <td colspan="8" align="center">
        <input type="hidden"  name="hidPrepareEntityKey" value="N"/>
        <input type="hidden"  name="isError" value="N"/>
       <input class="button" type="button" value="<%=getI18N("Proceed")%>" onclick="proceedClicked2(this);" />
    </td>
</tr>
</table>
