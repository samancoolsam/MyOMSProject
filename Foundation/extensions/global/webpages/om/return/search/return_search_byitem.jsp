<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>

<table class="view">
    <tr>
        <td>
            <input type="hidden" name="xml:/Order/@DraftOrderFlag" value="N"/>
        </td>
    </tr>

    <jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
        <jsp:param name="RefreshOnDocumentType" value="true"/>
        <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
    </jsp:include>
    <% // Now call the APIs that are dependent on the common fields (Doc Type & Enterprise Code)
       // Unit of Measures, Product Classes, and Order Line Types are refreshed. %>
    <yfc:callAPI apiID="AP2"/>
    <yfc:callAPI apiID="AP3"/>
    <yfc:callAPI apiID="AP4"/>

    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Order_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Order/@OrderNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Order/@OrderNoQryType"/>
            </select>
            <input class="unprotectedinput" type="text" <%=getTextOptions("xml:/Order/@OrderNo")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>X_Order_#</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Order/OrderLine/DerivedFrom/@OrderNoQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Order/OrderLine/DerivedFrom/@OrderNoQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/OrderLine/DerivedFrom/@OrderNo")%>/>
			<img class="lookupicon" onclick="callOrderNoLookup('xml:/Order/OrderLine/DerivedFrom/@OrderNo', null, 'order', 'yfcListViewGroupId=YOML011')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Order")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Item_ID</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Order/OrderLine/Item/@ItemIDQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Order/OrderLine/Item/@ItemIDQryType"/>
            </select>
            <input class="unprotectedinput" type="text" <%=getTextOptions("xml:/Order/OrderLine/Item/@ItemID")%>/>
            <% String extraParams = getExtraParamsForTargetBinding("xml:/Item/@CallingOrganizationCode", getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode")); %>
            <img class="lookupicon" name="search" onclick="callItemLookup('xml:/Order/OrderLine/Item/@ItemID','xml:/Order/OrderLine/Item/@ProductClass','xml:/Order/OrderLine/OrderLineTranQuantity/@TransactionalUOM','item','<%=extraParams%>')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Item") %> />
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Product_Class</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Order/OrderLine/Item/@ProductClass" class="combobox">
                <yfc:loopOptions binding="xml:ProductClassList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Order/OrderLine/Item/@ProductClass" isLocalized="Y"/>
            </select>
        </td>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Unit_Of_Measure</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell" nowrap="true" >
            <select name="xml:/Order/OrderLine/OrderLineTranQuantity/@TransactionalUOM" class="combobox">
                <yfc:loopOptions binding="xml:UnitOfMeasureList:/ItemUOMMasterList/@ItemUOMMaster" name="UnitOfMeasure"
                value="UnitOfMeasure" selected="xml:/Order/OrderLine/OrderLineTranQuantity/@TransactionalUOM"/>
            </select>
        </td>
    </tr>
        <td class="searchlabel" >
            <yfc:i18n>Line_Type</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell">
            <select name="xml:/Order/OrderLine/@LineType" class="combobox">
                <yfc:loopOptions binding="xml:LineTypeCodeList:/OrderLineTypeList/@OrderLineType" name="LineTypeDesc" value="LineType" selected="xml:/Order/OrderLine/@LineType" isLocalized="Y"/>
            </select>
        </td>
    </tr>
    <td class="searchlabel" >
        <yfc:i18n>Ship_Node</yfc:i18n>
    </td>
	</tr>
	<tr>
    <td class="searchcriteriacell">
        <select class="combobox" name="xml:/Order/@ShipNodeQryType">
            <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc" value="QueryType" selected="xml:/Order/@ShipNodeQryType"/>
        </select>
        <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@ShipNode")%> />
        <img class="lookupicon" onclick="callLookup(this,'shipnode')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Ship_Node") %> />
    </td>
	</tr>
</table>
