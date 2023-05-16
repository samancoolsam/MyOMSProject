<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<table class="anchor" cellpadding="7px" cellSpacing="0">
	<tr>
		<td>
			<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
				<jsp:param name="CurrentInnerPanelID" value="I01"/>
			</jsp:include>
		</td>
	</tr>
	<tr>
	   <yfc:hasXMLNode binding="xml:ItemDetails:/Item/InventoryTagAttributes">
	<% YFCElement tagElem =(  YFCElement) request.getAttribute("Item");
	   if(tagElem!=null){
		   request.setAttribute("Item", tagElem);
	   }
	%>
			<td >
					<jsp:include page="/yfc/innerpanel.jsp" flush="true" >
						<jsp:param name="CurrentInnerPanelID" value="I02"/>
						<jsp:param name="Modifiable" value='false'/>
						<jsp:param name="LabelTDClass" value='detaillabel'/>
						<jsp:param name="TagContainer" value='CountResult'/>
						<jsp:param name="TagElement" value='Tag'/>
					</jsp:include>
			</td>
		</yfc:hasXMLNode>
	</tr>
</table>
