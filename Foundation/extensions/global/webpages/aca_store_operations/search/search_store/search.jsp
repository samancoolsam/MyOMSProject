<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>

<script type="text/javascript" language="javascript">


	//setting To get all the store list on press of the Enter key.   
	window.attachEvent("onload",getSearchHandle);
	function getSearchHandle()
	{
     	 document.all("Store_Text").focus();
    }	
</script>
<!-- This jsp performs Search Action -->
<table width="100%" class="view">
<!-- Input for Store ID Text Box -->
<tr>
     <td class="searchlabel" ><yfc:i18n>Store_ID</yfc:i18n></td>
	     <td nowrap="true" class="searchcriteriacell">
	      <select name="xml:/Organization/Node/@ShipNodeQryType" class="combobox" >
	        <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
	          name="QueryTypeDesc" value="QueryType" selected="xml:/Organization/Node/@ShipNodeQryType"/>
	      </select>
	     </td>
         <td>
	      <input type="text" size="30" maxLength="40" class="unprotectedinput" id="Store_Text" <%=getTextOptions("xml:/Organization/Node/@ShipNode") %>/>
         </td>
</tr>
<!-- Input for Store Name Text Box -->
<tr>
     <td class="searchlabel" ><yfc:i18n>Store_Name</yfc:i18n></td>
          <td nowrap="true" class="searchcriteriacell">
            <select name="xml:/Organization/Node/@DescriptionQryType" class="combobox">
              <yfc:loopOptions binding="xml:/QueryTypeList/StringQueryTypes/@QueryType" 
                name="QueryTypeDesc" value="QueryType" selected="xml:/Organization/Node/@DescriptionQryType"/>
             </select>
          </td>
          <td>
           <input type="text" class="unprotectedinput" id="StoreName_Text" <%=getTextOptions("xml:/Organization/Node/@Description") %>"/>
          </td>
</tr>
<!-- Input for Store Status Combo box -->      
<tr>                  
     <td nowrap="true"  class="searchcriteriacell"><yfc:i18n>Store_Status</yfc:i18n></td>
         <td>
            <select  name="xml:/Organization/Node/@CanShipToOtherAddresses" class="combobox" id="Store_ID_Combo">
               <yfc:loopOptions binding="xml:CommonCodeList:/CommonCodeList/@CommonCode" 
                name="CodeValue" value="CodeShortDescription" selected="xml:/Organization/Node/@CanShipToOtherAddresses" suppressBlank="Y"/>
            </select>
        </td>            
</tr>
</table>