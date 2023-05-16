<%@include file="/yfsjspcommon/yfsutil.jspf"%>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%
//code to Node Status value based on the attribute value CanShipToOtherAddresses
//If CanShipToOtherAddresses=Y then set NodeStatus=Active
//If CanShipToOtherAddresses=N then set NodeStatus=InActive
    //Declare the elements being used in the code
   	YFCElement organization = null;
	YFCElement nodeElement = null;
	//From the response xml fetch the root element OrganizationList
	YFCElement organizationList = (YFCElement)request.getAttribute("OrganizationList");
	//Iterate through the child element Organization		
	for (Iterator i = organizationList.getChildren(); i.hasNext();) 
	{
		//Fetch the element Organization
		organization = (YFCElement)i.next();
		//Fetch the element Organization/Node
		nodeElement=organization.getChildElement("Node");
	  	//Check if the attribute value CanShipToOtherAddresses is Y
		if (YFCCommon.equals(nodeElement.getAttribute("CanShipToOtherAddresses"),"Y"))
		{   
			    //Set the attribute Organization/Node/@NodeStaus=Active
				nodeElement.setAttribute("NodeStatus", "Active");
		}
		else
		{
			 //Set the attribute Organization/Node/@NodeStaus=InActive
				nodeElement.setAttribute("NodeStatus", "InActive");
		}
	}
%>

<!-- This jsp performes List Action -->
<table class="table" border="0" cellspacing="0" width="100%">
<thead>
    <tr> 
        <!-- Checkbox setting at the header level -->
        <td class="checkboxheader" sortable="no">
            <input type="checkbox" name="checkbox" value="checkbox" onclick="doCheckAll(this);"/>
        </td>
        <!-- Heading Names Display -->
        <td class="tablecolumnheader"><yfc:i18n>Store_ID</yfc:i18n></td>
        <td class="tablecolumnheader"><yfc:i18n>Store_Name</yfc:i18n></td>
        <td class="tablecolumnheader"><yfc:i18n>Node_Type</yfc:i18n></td>
        <td class="tablecolumnheader"><yfc:i18n>Store_Status</yfc:i18n></td>
   </tr>
</thead>
<!-- List Body content Display -->
<tbody>
    <yfc:loopXML binding="xml:/OrganizationList/@Organization" id="Organization"> 
       
	    <!-- input XML for Displaying the data in list page -->
	    <yfc:makeXMLInput name="organizationKey">
		    <yfc:makeXMLKey binding="xml:/Organization/Node/@ShipNode" value="xml:/Organization/Node/@ShipNode"/>	
		</yfc:makeXMLInput>
	       
	    <tr> 
	      <!-- Checkbox setting at the column level -->
	      <td class="checkboxcolumn">
	            <input type="checkbox" value='<%=getParameter("organizationKey")%>' name="EntityKey"/>
		   </td>
		   <!-- Display the required data in the List page -->
	       <td class="tablecolumn"><yfc:getXMLValue name="Organization" binding="xml:/Organization/Node/@ShipNode"/></td>
	       <td class="tablecolumn"><yfc:getXMLValue name="Organization" binding="xml:/Organization/Node/@Description"/></td>
	       <td class="tablecolumn"><yfc:getXMLValue name="Organization" binding="xml:/Organization/Node/@NodeType"/></td>
	       <td class="tablecolumn"><yfc:getXMLValue name="Organization" binding="xml:/Organization/Node/@NodeStatus"/></td>
	    </tr>
    </yfc:loopXML> 
</tbody>
</table>