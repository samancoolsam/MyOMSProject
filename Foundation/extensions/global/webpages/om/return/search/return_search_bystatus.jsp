 <%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Order Management  (5725-D10)
IBM Sterling Configure Price Quote (5725-D11)
(C) Copyright IBM Corp. 2005, 2014 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<%  
	YFCDate oEndDate = new YFCDate(); 
	oEndDate.setEndOfDay();
%>
<script language="javascript">

	function setOrderStateFlags(value1, value2){
        var oOrderComplete = document.all("xml:/Order/@OrderComplete");
        if (oOrderComplete != null)
		oOrderComplete.value = value1;
	var oReadFromHistory = document.all("xml:/Order/@ReadFromHistory");
	if (oReadFromHistory != null)
		oReadFromHistory.value = value2;
    }

</script>

<%
	// Values for radiobinding
	// open : OrderComplete="N" ReadFromHistory="N" radiobinding="open"
	// recent : OrderComplete=" " ReadFromHistory="N" radiobinding="recent"
	// history : OrderComplete=" " ReadFromHistory="Y" radiobinding="history"
	// all : OrderComplete=" " ReadFromHistory="B" radiobinding="all"

	// default is open : OrderComplete="N" ReadFromHistory="N" radiobinding="open"

    String bReadFromHistory = resolveValue("xml:/Order/@ReadFromHistory");
	String sOrderComplete = resolveValue("xml:/Order/@OrderComplete");

	String radiobinding = "open";
	if(isVoid(sOrderComplete) && equals(bReadFromHistory, "N")){
		radiobinding = "recent";
	}
	else if(isVoid(sOrderComplete) && equals(bReadFromHistory, "Y")){
		radiobinding = "history";
	}
	else if(isVoid(sOrderComplete) && equals(bReadFromHistory, "B")){
		radiobinding = "all";
    }
	else{
		radiobinding = "open";
		YFCElement orderElem = (YFCElement)request.getAttribute("Order");
		if(orderElem == null){
			orderElem = YFCDocument.createDocument("Order").getDocumentElement();
		}
		orderElem.setAttribute("OrderComplete", "N");
		orderElem.setAttribute("ReadFromHistory", "N");
		bReadFromHistory = "N";
		sOrderComplete = "N";
	}
	

%>


<table class="view" >
    <tr>
        <td>
            <input type="hidden" name="xml:/Order/@StatusQryType" value="BETWEEN" OldValue=""/>
            <input type="hidden" name="xml:/Order/@OrderDateQryType" value="BETWEEN" OldValue=""/>
            <input type="hidden" name="xml:/Order/@DraftOrderFlag" value="N" OldValue=""/>
			
			<input type="hidden" name="xml:/Order/OrderHoldType/@Status" value=""/>
			<input type="hidden" name="xml:/Order/OrderHoldType/@StatusQryType" value="" />
        </td>
    </tr>

    <jsp:include page="/yfsjspcommon/common_fields.jsp" flush="true">
        <jsp:param name="RefreshOnDocumentType" value="true"/>
        <jsp:param name="RefreshOnEnterpriseCode" value="true"/>
    </jsp:include>
    <% // Now call the APIs that are dependent on the common fields (Doc Type & Enterprise Code)
       // Statuses and Hold Reason Codes are refreshed. %>
    <yfc:callAPI apiID="AP2"/>
    <yfc:callAPI apiID="AP3"/>

    <%
		if(!isTrue("xml:/Rules/@RuleSetValue") )	{
    %>
			<yfc:callAPI apiID="AP5"/>
	<%
			YFCElement listElement = (YFCElement)request.getAttribute("HoldTypeList");

			YFCDocument document = listElement.getOwnerDocument();
			YFCElement newElement = document.createElement("HoldType");
			newElement.setAttribute("HoldType", " ");
			newElement.setAttribute("HoldTypeDescription", getI18N("All_Held_Orders"));

			YFCElement eFirst = listElement.getFirstChildElement();
			if(eFirst != null)	{
				listElement.insertBefore(newElement, eFirst);
			}	else	{
				listElement.appendChild(newElement);
			}
			request.setAttribute("defaultHoldType", newElement);
		}
	%>

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
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderNo")%>/>
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
			<img class="lookupicon" onclick="callOrderNoLookup('xml:/Order/OrderLine/DerivedFrom/@OrderNo', null,'order', 'yfcListViewGroupId=YOML011')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Order")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Order_Date</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true">
            <input class="dateinput" type="text" <%=getTextOptions("xml:/Order/@FromOrderDate_YFCDATE","xml:/Order/@FromOrderDate_YFCDATE","")%>/>
            <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
            <input class="dateinput" type="hidden" <%=getTextOptions("xml:/Order/@FromOrderDate_YFCTIME","xml:/Order/@FromOrderDate_YFCTIME","")%>/>
			<yfc:i18n>To</yfc:i18n>
            <input class="dateinput" type="text" <%=getTextOptions("xml:/Order/@ToOrderDate_YFCDATE","xml:/Order/@ToOrderDate_YFCDATE","")%>/>
            <img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
			<input class="dateinput" type="hidden" <%=getTextOptions("xml:/Order/@ToOrderDate_YFCTIME","xml:/Order/@ToOrderDate_YFCTIME",oEndDate.getString(getLocale().getTimeFormat()))%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Buyer</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Order/@BuyerOrganizationCodeQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Order/@BuyerOrganizationCodeQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@BuyerOrganizationCode")%>/>
			<% String enterpriseCode = getValue("CommonFields", "xml:/CommonFields/@EnterpriseCode");%>
            <img class="lookupicon" onclick="callLookupForOrder(this,'BUYER','<%=enterpriseCode%>','xml:/Order/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Seller</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Order/@SellerOrganizationCodeQryType" class="combobox">
                <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" name="QueryTypeDesc"
                value="QueryType" selected="xml:/Order/@SellerOrganizationCodeQryType"/>
            </select>
            <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@SellerOrganizationCode")%>/>
            <img class="lookupicon" onclick="callLookupForOrder(this,'SELLER','<%=enterpriseCode%>','xml:/Order/@DocumentType')" <%=getImageOptions(YFSUIBackendConsts.LOOKUP_ICON, "Search_for_Organization")%>/>
        </td>
    </tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Order_Line_Status</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td nowrap="true">
            <select name="xml:/Order/@FromStatus" class="combobox">
                <yfc:loopOptions binding="xml:/StatusList/@Status" name="Description" value="Status" selected="xml:/Order/@FromStatus" isLocalized="Y"/>
            </select>
            <span class="searchlabel" ><yfc:i18n>To</yfc:i18n></span>
        </td>
    </tr>
    <tr>
        <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Order/@ToStatus" class="combobox">
                <yfc:loopOptions binding="xml:/StatusList/@Status" name="Description" value="Status" selected="xml:/Order/@ToStatus" isLocalized="Y"/>
            </select>
        </td>
    </tr>
	<%	if(isTrue("xml:/Rules/@RuleSetValue") )	{	%>
		<tr>
			<td class="searchcriteriacell">
				<input type="checkbox" onclick="manageHoldOpts(this)" <%=getCheckBoxOptions("xml:/Order/@HoldFlag", "xml:/Order/@HoldFlag", "Y")%> yfcCheckedValue='Y' yfcUnCheckedValue='N'><yfc:i18n>Held_Orders</yfc:i18n></input>
			</td>
		</tr>    
		<tr>
			<td class="searchlabel" >
				<yfc:i18n>Hold_Reason_Code</yfc:i18n>
			</td>
		</tr>
		<tr>
			<td class="searchcriteriacell">
				<select name="xml:/Order/@HoldReasonCode" class="combobox">
					<yfc:loopOptions binding="xml:HoldReasonCodeList:/CommonCodeList/@CommonCode" name="CodeShortDescription" value="CodeValue" selected="xml:/Order/@HoldReasonCode" isLocalized="Y"/>
				</select>
			</td>
		</tr>
	<%	}	else	{	%>
		<tr>
			<td class="searchcriteriacell">
				<input type="checkbox" <%=getCheckBoxOptions("xml:/Order/@HoldFlag", "xml:/Order/@HoldFlag", "Y")%> yfcCheckedValue='Y' yfcUnCheckedValue=' ' onclick="manageHoldOpts(this)" ><yfc:i18n>Held_Orders_With_Hold_Type</yfc:i18n></input>
			</td>
		</tr>
		<tr>
			<td>
				<select resetName="<%=getI18N("All_Held_Orders")%>" onchange="resetObjName(this, 'xml:/Order/OrderHoldType/@HoldType')" name="xml:/Order/OrderHoldType/@HoldType" class="combobox" <%if(isTrue("xml:/Order/@HoldFlag") ) {%> ENABLED <%} else {%> disabled="true" <%}%> >
					<yfc:loopOptions binding="xml:/HoldTypeList/@HoldType" name="HoldTypeDescription" value="HoldType" suppressBlank="Y" selected="xml:/Order/OrderHoldType/@HoldType" isLocalized="Y"/>
				</select>
			</td>
		</tr>
	<%	}	%>

	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Order_State</yfc:i18n>
        </td>
    </tr>
    <tr>
        <td class="searchcriteriacell">
			<input type="radio" onclick="setOrderStateFlags('N','N')" <%=getRadioOptions("xml:/Order/@Radiobinding", radiobinding, "open")%>><yfc:i18n>OrderState_Open</yfc:i18n> 
			<input type="radio" onclick="setOrderStateFlags(' ','N')"  <%=getRadioOptions("xml:/Order/@Radiobinding", radiobinding, "recent")%>><yfc:i18n>OrderState_Recent</yfc:i18n><!-- The use of 'NO' is done intentionally, getOrderList API returns history orders only if ReadFromHistory =='Y'  -->
			<input type="radio" onclick="setOrderStateFlags(' ','Y')" <%=getRadioOptions("xml:/Order/@Radiobinding", radiobinding, "history")%>><yfc:i18n>OrderState_History</yfc:i18n>
            <input type="radio" onclick="setOrderStateFlags(' ','B')" <%=getRadioOptions("xml:/Order/@Radiobinding", radiobinding, "all")%>><yfc:i18n>OrderState_All</yfc:i18n>
            <input type="hidden" name="xml:/Order/@OrderComplete" value="<%=sOrderComplete%>"/>
            <input type="hidden" name="xml:/Order/@ReadFromHistory" value="<%=bReadFromHistory%>"/>
        </td>
    </tr>
	<tr>
        <td class="searchlabel">
            <yfc:i18n>Selecting_All_may_be_slow</yfc:i18n>
        </td>
    </tr>
</table>
