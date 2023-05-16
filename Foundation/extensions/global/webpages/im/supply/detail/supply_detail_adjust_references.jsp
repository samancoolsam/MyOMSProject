<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2013 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<%
String sBindingNode=request.getParameter("BindingNode");
%>

<table width="100%" class="view">
<tr>
    <td class="detaillabel" >
        <yfc:i18n>Reference_1</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(sBindingNode, "/@Reference_1", "") ,"")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
        <yfc:i18n>Reference_2</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(sBindingNode, "/@Reference_2", "") ,"")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" >
        <yfc:i18n>Reference_3</yfc:i18n>
    </td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(sBindingNode, "/@Reference_3", "") ,"")%> />
    </td>
</tr>    
<tr>    
    <td class="detaillabel" ><yfc:i18n>Reference_4</yfc:i18n></td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(sBindingNode, "/@Reference_4", "") ,"")%> />
    </td>
</tr>
<tr>    
    <td class="detaillabel" ><yfc:i18n>Reference_5</yfc:i18n></td>
    <td>
        <input type="text" class="unprotectedinput" <%=getTextOptions(buildBinding(sBindingNode, "/@Reference_5", "") ,"")%> />
    </td>
</tr>
</table>
