<%@include file="/yfsjspcommon/yfsutil.jspf"%>


<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>            
            <td class="tablecolumnheader"><yfc:i18n>Load_#</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Load_Status</yfc:i18n></td>
         </tr>
    </thead>
    <tbody>
	 <yfc:loopXML binding="xml:/Manifest/Loads/@Load" id="Load">		
			<%
				String sManifestComplete = resolveValue("xml:/Load/@LoadManifestComplete");
				if(YFCCommon.equals(sManifestComplete,"N")){				
			%>
				<tr>
					<yfc:makeXMLInput name="loadKey">
						<yfc:makeXMLKey binding="xml:/Load/@LoadKey" value="xml:/Load/@LoadKey" />
						<yfc:makeXMLKey binding="xml:/Load/@LoadNo" value="xml:/Load/@LoadNo" />
					</yfc:makeXMLInput>
					<td class="tablecolumn">
						<a <%=getDetailHrefOptions("L01",getParameter("loadKey"),"")%> >
							<yfc:getXMLValue binding="xml:/Load/@LoadNo"/>				
						</a>
					</td>
					<td class="tablecolumn">
						<yfc:getXMLValueI18NDB binding="xml:/Load/Status/@Description"/>
					</td>
				</tr>
			<%
				}		
			%>
		</yfc:loopXML>
	
	</tbody>
</table>

