<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>

<%
    // Change the number of columns in this screen with this parameter
    int totalColumns = 3;
    int currentColumn = 1;
    String widthValue = "33%";

    String outputPath = "";
    String outputAddressTypePath = "";
    int addressCounter = 1;
%>

<table cellSpacing="0" cellPadding="0" width="100%" class="view">
<tr>
    <td colspan="<%=totalColumns%>" style="padding-top:5;padding-left:5">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I03"/>
        </jsp:include>
    </td>
</tr>
<tr>
    <td height="100%" width="<%=widthValue%>" style="padding-top:5;padding-left:5" addressip="true">
        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
            <jsp:param name="CurrentInnerPanelID" value="I02"/>
            <jsp:param name="Path" value="xml:/Order/PersonInfoMarkFor"/>
            <jsp:param name="DataXML" value="Order"/>
            <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValueWithPermission("MarkForAddress", "xml:/Order/AllowedModifications")%>'/>
        </jsp:include>
    </td>

<%  ArrayList addressTypeList = getLoopingElementList("xml:AddressTypeList:/CommonCodeList/@CommonCode");
    for (int CommonCodeCounter = 0; CommonCodeCounter < addressTypeList.size(); CommonCodeCounter++) {
       
        YFCElement singleAddressType = (YFCElement) addressTypeList.get(CommonCodeCounter);
        pageContext.setAttribute("CommonCode", singleAddressType); %>
       
     <% request.setAttribute("CommonCode", pageContext.getAttribute("CommonCode")); %>

     <% ArrayList additionalAddressList = getLoopingElementList("xml:/Order/AdditionalAddresses/@AdditionalAddress");
        for (int AdditionalAddressCounter = 0; AdditionalAddressCounter < additionalAddressList.size(); AdditionalAddressCounter++) {
           
           YFCElement singleAdditionalAddress = (YFCElement) additionalAddressList.get(AdditionalAddressCounter);
           pageContext.setAttribute("AdditionalAddress", singleAdditionalAddress); %>
       
        <% request.setAttribute("AdditionalAddress", pageContext.getAttribute("AdditionalAddress"));
           if (equals(getValue("CommonCode", "xml:/CommonCode/@CodeValue"), getValue("AdditionalAddress", "xml:/AdditionalAddress/@AddressType"))) {
               YFCElement addressElement = (YFCElement) request.getAttribute("AdditionalAddress");
               addressElement.setAttribute("IsDisplayed", "true");
               YFCElement commonCodeElement = (YFCElement) request.getAttribute("CommonCode");
               commonCodeElement.setAttribute("IsDisplayed", "true");
        %>

                <% if ((currentColumn % totalColumns) == 0) {
                       currentColumn = 1; %>
                       </tr><tr>
                <% } else {
                         currentColumn++;
                   }
                %>
                <td height="100%" width="<%=widthValue%>" style="padding-top:5;padding-left:5" addressip="true">

                    <%outputPath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter + "/PersonInfo";
                      outputAddressTypePath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter;
                      addressCounter++;
                    %>

			        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
			            <jsp:param name="CurrentInnerPanelID" value="I01"/>
                        <jsp:param name="DataXML" value="AdditionalAddress"/>
                        <jsp:param name="Path" value="xml:/AdditionalAddress/PersonInfo"/>
                        <jsp:param name="OutputPath" value='<%=outputPath%>'/>
                        <jsp:param name="AddressTypePath" value="xml:/AdditionalAddress"/>
                        <jsp:param name="OutputAddressTypePath" value='<%=outputAddressTypePath%>'/>
                        <jsp:param name="Title" value='<%=getValue("CommonCode", "xml:/CommonCode/@CodeShortDescription")%>'/>
                        <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValueWithPermission("AdditionalAddress", "xml:/Order/AllowedModifications")%>'/>
                    </jsp:include>
                </td>
        <% } %>
    <% } %>
    
    <% YFCElement finalCommonCodeElement = (YFCElement) request.getAttribute("CommonCode");
       if (!equals(finalCommonCodeElement.getAttribute("IsDisplayed"), "true")) {
    %>
            <% if ((currentColumn % totalColumns) == 0) {
                   currentColumn = 1; %>
                   </tr><tr>
            <% } else {
                     currentColumn++;
               }
               outputPath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter + "/PersonInfo";
               outputAddressTypePath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter;
               addressCounter++;
               finalCommonCodeElement.setAttribute("AddressType", finalCommonCodeElement.getAttribute("CodeValue"));
            %>
            <td height="100%" width="<%=widthValue%>" style="padding-top:5;padding-left:5" addressip="true">
		        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
		            <jsp:param name="CurrentInnerPanelID" value="I01"/>
                    <jsp:param name="Path" value="xml:/CommonCode"/>
                    <jsp:param name="DataXML" value="CommonCode"/>
                    <jsp:param name="OutputPath" value='<%=outputPath%>'/>
                    <jsp:param name="AddressTypePath" value="xml:/CommonCode"/>
                    <jsp:param name="OutputAddressTypePath" value='<%=outputAddressTypePath%>'/>
                    <jsp:param name="Title" value='<%=getValue("CommonCode", "xml:/CommonCode/@CodeShortDescription")%>'/>
                    <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValueWithPermission("AdditionalAddress", "xml:/Order/AllowedModifications")%>'/>
                </jsp:include>
            </td>
    <% } %>
<% } %>
<%  ArrayList addAddressList = getLoopingElementList("xml:/Order/AdditionalAddresses/@AdditionalAddress");
    for (int AddAddressCounter = 0; AddAddressCounter < addAddressList.size(); AddAddressCounter++) {
       
       YFCElement singleAddAddress = (YFCElement) addAddressList.get(AddAddressCounter);
       pageContext.setAttribute("AdditionalAddress", singleAddAddress); %>
       
    <% request.setAttribute("AdditionalAddress", pageContext.getAttribute("AdditionalAddress"));
       YFCElement finalAddressElement = (YFCElement) request.getAttribute("AdditionalAddress");
       if (!equals(finalAddressElement.getAttribute("IsDisplayed"), "true")) {
    %>
            <% if ((currentColumn % totalColumns) == 0) {
                   currentColumn = 1; %>
                   </tr><tr>
            <% } else {
                     currentColumn++;
               }
               outputPath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter + "/PersonInfo";
               outputAddressTypePath = "xml:/Order/AdditionalAddresses/AdditionalAddress_" + addressCounter;
               addressCounter++;
            %>
            <td height="100%" width="<%=widthValue%>" style="padding-top:5;padding-left:5" addressip="true">
		        <jsp:include page="/yfc/innerpanel.jsp" flush="true" >
		            <jsp:param name="CurrentInnerPanelID" value="I01"/>
                    <jsp:param name="Path" value="xml:/AdditionalAddress/PersonInfo"/>
                    <jsp:param name="DataXML" value="AdditionalAddress"/>
                    <jsp:param name="OutputPath" value='<%=outputPath%>'/>
                    <jsp:param name="AddressTypePath" value="xml:/AdditionalAddress"/>
                    <jsp:param name="OutputAddressTypePath" value='<%=outputAddressTypePath%>'/>
                    <jsp:param name="Title" value='<%=getValue("AdditionalAddress", "xml:/AdditionalAddress/@AddressType")%>'/>
                    <jsp:param name="AllowedModValue" value='<%=getModificationAllowedValueWithPermission("AdditionalAddress", "xml:/Order/AllowedModifications")%>'/>
                </jsp:include>
            </td>
    <% } %>
<% } %>
</tr>
</table>
