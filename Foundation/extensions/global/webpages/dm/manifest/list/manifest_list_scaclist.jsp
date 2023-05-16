<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@include file="/console/jsp/currencyutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<div style="height:175px;overflow:auto">
<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>
            <td sortable="no" class="checkboxheader">
				<input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>			
            </td>
			<td class="tablecolumnheader"><yfc:i18n>SCAC</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Description</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Date</yfc:i18n></td>
			<td class="tablecolumnheader"><yfc:i18n>Num_of_Open_Manifests</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Total_#_of_Packages_to_be_Manifested</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/ScacList/@Scac" id="Scac">
            <tr>
                <yfc:makeXMLInput name="ScacKey">
                    <yfc:makeXMLKey binding="xml:/Scac/@ScacKey" value="xml:/Scac/@ScacKey" />
					<yfc:makeXMLKey binding="xml:/Scac/@Scac" value="xml:/Scac/@Scac" />
                    <yfc:makeXMLKey binding="xml:/Scac/@OpenManifests" value="xml:/Scac/@OpenManifests" />
                    <yfc:makeXMLKey binding="xml:/Scac/@Date" value="xml:/Scac/@Date" />
					<yfc:makeXMLKey binding="xml:/Scac/@EligibleContainers" value="xml:/Scac/@EligibleContainers" />
                </yfc:makeXMLInput>
                <td class="checkboxcolumn"> 
                    <input type="checkbox" value='<%=getParameter("ScacKey")%>' name="scacEntityKey" yfcMultiSelectValue1='<%=getValue("Scac", "xml:/Scac/@Scac")%>' />
			    </td>
                <td class="tablecolumn">
						<yfc:getXMLValue binding="xml:/Scac/@Scac"/>					
                </td>
                <td class="tablecolumn">
					<yfc:getXMLValueI18NDB binding="xml:/Scac/@ScacDesc"/>
                </td>
				<td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Scac/@Date"/>
                </td>
                <td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Scac/@OpenManifests"/>
				</td>
                <td class="tablecolumn">
					<yfc:getXMLValue binding="xml:/Scac/@EligibleContainers"/>
				</td>
            </tr>
        </yfc:loopXML> 
			<input type="hidden" name="xml:/Scac/Manifest/@ManifestNo" />
	        <input type="hidden" name="xml:/Scac/Manifest/@ManifestDate" />
			<input type="hidden" name="xml:/Scac/Manifest/@TrailerNo" />
	        <input type="hidden" name="xml:/Scac/Manifest/@ShipNode" />
			<input type="hidden" name="xml:/Scac/Manifest/@ShipperAccountNo" />
			<input type="hidden" name="xml:/Scac/@Scac" />
   </tbody>
</table>
</div>
