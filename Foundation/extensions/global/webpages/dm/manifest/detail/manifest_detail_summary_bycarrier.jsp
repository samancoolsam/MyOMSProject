<%@ page import="java.util.*" %>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfc.util.*" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<table class="table" editable="false" width="100%" cellspacing="0">
    <thead> 
        <tr>            
            <td class="tablecolumnheader"><yfc:i18n>Carrier_Service</yfc:i18n></td>
            <td class="tablecolumnheader"><yfc:i18n>Num_Packages</yfc:i18n></td>
        </tr>
    </thead>
    <tbody>
        <yfc:loopXML binding="xml:/Manifest/CarrierServiceSummaryList/@CarrierServiceSummary" id="CarrierServiceSummary">
		
            <tr>
                <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/CarrierServiceSummary/@CarrierServiceCode"/>
                </td>
                <td class="tablecolumn">
                    <yfc:getXMLValue binding="xml:/CarrierServiceSummary/@NumberOfContainersManifested"/>
                </td>
            </tr>	
        </yfc:loopXML> 
   </tbody>
</table>
