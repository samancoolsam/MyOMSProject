<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/dm.js"></script>
<table width="100%" class="view">
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Manifest_#</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@ManifestNo"/>
        </td>		
        <td class="detaillabel">
            <yfc:i18n>Manifest_Date</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@ManifestDate"/>
        </td>		        
    </tr>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Carrier</yfc:i18n> 
        </td>
        <td class="protectedtext">
            <yfc:getXMLValue binding="xml:/Manifest/@Scac"/>
        </td>		
		<td class="detaillabel">
            <yfc:i18n>Manifest_Status</yfc:i18n> 
        </td>
        <td class="protectedtext" >
            <yfc:getXMLValueI18NDB binding="xml:/Manifest/Status/@Description"/>
		</td>
    </tr>
    <tr>
        <td class="detaillabel">
            <yfc:i18n>Trailer_#</yfc:i18n> 
        </td>
        <td class="protectedtext" >
            <yfc:getXMLValue binding="xml:/Manifest/@TrailerNo"/>	    
            <input type="hidden" <%=getTextOptions("xml:/Manifest/@TrailerNo")%>/>
        </td>		
		<td class="detaillabel" nowrap="true" >
			<yfc:i18n>Has_Hazardous_Item(s)</yfc:i18n>
		</td>
		<td class="protectedtext">
			<yfc:getXMLValue binding="xml:/Manifest/@IsHazmat"/>
		</td>
    </tr>    
</table>