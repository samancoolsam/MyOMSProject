<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<table width="100%" class="table">
<thead>
    <tr>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@FromDate")%>"><yfc:i18n>Start_Date</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@EndDate")%>"><yfc:i18n>End_Date</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@Supply")%>"><yfc:i18n>Supply</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@Demand")%>"><yfc:i18n>Demand</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@Available")%>"><yfc:i18n>Available</yfc:i18n></td>
        <td class="tablecolumnheader" style="width:<%= getUITableSize("xml:/InventoryInformation/Item/@Shortage")%>"><yfc:i18n>Shortage</yfc:i18n></td>
    </tr>
</thead>
<tbody>
    <yfc:loopXML name="InventoryInformation" binding="xml:/InventoryInformation/Item/@AvailableToPromiseInventory" id="availability" > 
    <tr>
        <td class="tablecolumn" sortValue="<%=getDateValue("xml:availability:/AvailableToPromiseInventory/@FromDate")%>"><yfc:getXMLValue name="availability" binding="xml:/AvailableToPromiseInventory/@FromDate" /></td>
        <td class="tablecolumn" sortValue="<%=getDateValue("xml:availability:/AvailableToPromiseInventory/@EndDate")%>"><yfc:getXMLValue name="availability" binding="xml:/AvailableToPromiseInventory/@EndDate" /></td>
        <td class="numerictablecolumn" width="20%"  sortValue="<%=getNumericValue("xml:availability:/AvailableToPromiseInventory/@Supply")%>">
            <yfc:getXMLValue name="availability" binding="xml:/AvailableToPromiseInventory/@Supply" />
            <% if ((getDoubleFromLocalizedString(getLocale(),resolveValue("xml:availability:/AvailableToPromiseInventory/@Supply"))) > 0) {%>
                <img onclick="showATPDiv('s'+'<%=availabilityCounter.toString()%>','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
            <%}%>
            <% String sSupplyID = "s"+ availabilityCounter.toString() ;%>
            <div id=<%=sSupplyID%> style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@SupplyType")%>"><yfc:i18n>Supply_Type</yfc:i18n></td>
						<td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@OrganizationCode")%>"><yfc:i18n>Organization_Code</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Supply/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                    <yfc:loopXML name="availability" binding="xml:/AvailableToPromiseInventory/Supplies/@Supply" id="atpsupply" > 
                    <tr>
                        <yfc:makeXMLInput name="atpSupplyTypeKey">
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ItemID" value="xml:/InventoryInformation/Item/@ItemID" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@UnitOfMeasure" value="xml:/InventoryInformation/Item/@UnitOfMeasure" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ProductClass" value="xml:/InventoryInformation/Item/@ProductClass" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@OrganizationCode" value="xml:/InventoryInformation/Item/@OrganizationCode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@SupplyType" value="xml:atpsupply:/Supply/@SupplyType" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ShipNode" value="xml:/InventoryInformation/Item/@ShipNode" />
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@DistributionRuleId" value="xml:/InventoryInformation/Item/@DistributionRuleId"/>
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@ConsiderAllNodes" value="xml:/InventoryInformation/Item/@ConsiderAllNodes"/>
							<yfc:makeXMLKey binding="xml:/InventorySupply/@ConsiderAllSegments" value="xml:/InventoryInformation/Item/@ConsiderAllSegments"/>
							<yfc:makeXMLKey binding="xml:/InventorySupply/@SegmentType" value="xml:/InventoryInformation/Item/@SegmentType"/>
							<yfc:makeXMLKey binding="xml:/InventorySupply/@Segment" value="xml:/InventoryInformation/Item/@Segment"/>
                            <% if ((availabilityCounter.intValue()) > 1) {%>
                                <yfc:makeXMLKey binding="xml:/InventorySupply/@FromDate" value="xml:availability:/AvailableToPromiseInventory/@FromDate" />
                            <%}%>
                            <yfc:makeXMLKey binding="xml:/InventorySupply/@EndDate" value="xml:availability:/AvailableToPromiseInventory/@EndDate" />
                        </yfc:makeXMLInput>
                        <td class="tablecolumn"><%=getComboText("xml:SupplyTypeList:/InventorySupplyTypeList/@InventorySupplyType","Description","SupplyType",getValue("atpsupply","xml:/Supply/@SupplyType"),true)%></td>
						<td class="tablecolumn">
						<%if(getValue("atpsupply","xml:/Supply/@OrganizationCode").equals("")){%>
						<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>
						<%}else%>
						<%=getValue("atpsupply","xml:/Supply/@OrganizationCode")%>
						</td>
                        <td class="numerictablecolumn">
						<%
							if(getValue("atpsupply","xml:/Supply/@OrganizationCode").equals(resolveValue("xml:/InventoryInformation/Item/@InventoryOrganization"))){%>
                            <a <%=getDetailHrefOptions("L01",getParameter("atpSupplyTypeKey"),"")%>>
                                <yfc:getXMLValue name="atpsupply" binding="xml:/Supply/@Quantity" />
                            </a>
							<%}else{%><%=getValue("atpsupply","xml:/Supply/@Quantity")%>
								<%}%>
                        </td>
                    </tr>
                    </yfc:loopXML>
                </tbody>
                </table>
            </div>
        </td>
        <td class="numerictablecolumn" width="20%"  sortValue="<%=getNumericValue("xml:availability:/AvailableToPromiseInventory/@Demand")%>">
            <yfc:getXMLValue  name="availability" binding="xml:/AvailableToPromiseInventory/@Demand" ></yfc:getXMLValue>
            <% if ((getDoubleFromLocalizedString(getLocale(),resolveValue("xml:availability:/AvailableToPromiseInventory/@Demand"))) > 0) {%>
                <img onclick="showATPDiv('d'+'<%=availabilityCounter.toString()%>','<%=getI18N("Click_To_Expand")%>','<%=getI18N("Click_To_Collapse")%>','<%=YFSUIBackendConsts.FOLDER_COLLAPSE%>','<%=YFSUIBackendConsts.FOLDER_EXPAND%>')" <%=getImageOptions(YFSUIBackendConsts.FOLDER,"Click_to_Expand")%> />
            <%}%>
            <% String sDemandID = "d" + availabilityCounter.toString() ;%>
        	<div id=<%=sDemandID%> style="display:none;padding-top:5px">
                <table width="100%"  class="simpletable" cellspacing="0"  cellpadding="0">
                <thead>
                    <tr>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@DemandType")%>"><yfc:i18n>Demand_Type</yfc:i18n></td>
						<td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@OrganizationCode")%>"><yfc:i18n>Organization_Code</yfc:i18n></td>
                        <td class="tablecolumnheadernosort" style="width:<%= getUITableSize("xml:/Demand/@Quantity")%>"><yfc:i18n>Quantity</yfc:i18n></td>
                    </tr>
                </thead>
                <tbody>
                    <yfc:loopXML name="availability" binding="xml:/AvailableToPromiseInventory/Demands/@Demand" id="atpdemand" > 
                        <tr>
                            <td class="tablecolumn"><%=getComboText("xml:DemandTypeList:/InventoryDemandTypeList/@InventoryDemandType","Description","DemandType",getValue("atpdemand","xml:/Demand/@DemandType")	,true)%></td>		
							<td class="tablecolumn">
							<%if(getValue("atpdemand","xml:/Demand/@OrganizationCode").equals("")){%>
								<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>
								<%}else%>
								<%=getValue("atpdemand","xml:/Demand/@OrganizationCode")%>
						</td>
                            <td class="numerictablecolumn">	
							<%
							if(getValue("atpdemand","xml:/Demand/@OrganizationCode").equals(resolveValue("xml:/InventoryInformation/Item/@InventoryOrganization"))){%>
                                <a href="" onclick="showDemandList('<%=resolveValue("xml:/InventoryInformation/Item/@ItemID")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@UnitOfMeasure")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@ProductClass")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@OrganizationCode")%>', '<%=resolveValue("xml:atpdemand:/Demand/@DemandType")%>', '<%=resolveValue("xml:/InventoryInformation/Item/@ShipNode")%>',
								<% if ((availabilityCounter.intValue()) > 1) {%>
									'<%=resolveValue("xml:availability:/AvailableToPromiseInventory/@FromDate")%>',
								<%} else {%>
									'',
								<%}%>
								'<%=resolveValue("xml:availability:/AvailableToPromiseInventory/@EndDate")%>',
								'<%=resolveValue("xml:/InventoryInformation/Item/@DistributionRuleId")%>',
								'<%=resolveValue("xml:/InventoryInformation/Item/@ConsiderAllNodes")%>','BETWEEN'
								);return false;">
                                    <yfc:getXMLValue name="atpdemand" binding="xml:/Demand/@Quantity" />
                                </a><%}else{%><%=getValue("atpdemand","xml:/Demand/@Quantity")%>
								<%}%>
                            </td>
                        </tr>
                    </yfc:loopXML>
                </tbody>
                </table>
            </div>
        </td>
        <% if(equals(resolveValue("xml:/InventoryInformation/Item/@TrackedEverywhere"),"N")) {%>
            <td class="tablecolumn">
                <yfc:i18n>INFINITE</yfc:i18n>
            </td>
        <%} else{%>
            <td class="numerictablecolumn"  sortValue="<%=getNumericValue("xml:availability:/AvailableToPromiseInventory/@Available")%>">
                <yfc:getXMLValue name="availability" binding="xml:/AvailableToPromiseInventory/@Available" />
            </td>        
        <%}%>
        <td <%if ( (getDoubleFromLocalizedString(getLocale(),resolveValue("xml:availability:/AvailableToPromiseInventory/@Shortage"))) > 0) {%> class="numerictablecolumnshortagerow" <%} else {%> class="numerictablecolumn" <%}%>  sortValue="<%=getNumericValue("xml:availability:/AvailableToPromiseInventory/@Shortage")%>">
            <yfc:getXMLValue name="availability" binding="xml:/AvailableToPromiseInventory/@Shortage" />
        </td>
    </tr>
    </yfc:loopXML>
</tbody>
</table>
