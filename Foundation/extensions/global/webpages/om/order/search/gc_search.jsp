<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@include file="/console/jsp/order.jspf" %>
<%@ include file="/console/jsp/paymentutils.jspf" %>

<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/tools.js"></script>
<script language="javascript" src="<%=request.getContextPath()%>/console/scripts/om.js"></script>
<script language="javascript">

}
</script>
<%

%>

<table class="view">
	<tr>
        <td class="searchlabel" >
            <yfc:i18n>Document Type</yfc:i18n>
        </td>
		
		<td class="searchlabel" >
           <input type="text" class="protectedinput" readOnly="TRUE"  <%=getTextOptions("xml:/Order/@DocumentType","0001")%>/>
        </td>
    </tr>
	<tr>
		<td class="searchlabel" >
            <yfc:i18n>Enterprise</yfc:i18n>
        </td>
		<td class="searchlabel" >
           <input type="text" class="protectedinput" readOnly="TRUE" <%=getTextOptions("xml:/Order/@EnterpriseCode","Academy_Direct")%>/>
        </td>
	</tr>
    <tr>
        <td class="searchlabel" >
            <yfc:i18n>Order #</yfc:i18n>
        </td>
         <td nowrap="true" class="searchcriteriacell">
             <input type="text" class="unprotectedinput" <%=getTextOptions("xml:/Order/@OrderNo")%>/>
        </td>
    </tr>
</table>