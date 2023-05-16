<%/*******************************************************************************
Licensed Materials - Property of IBM
IBM Sterling Selling And Fulfillment Suite
(C) Copyright IBM Corp. 2005, 2016 All Rights Reserved.
US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 ********************************************************************************/%>
<%@ include file="/yfsjspcommon/yfsutil.jspf" %>
<%@ include file="/console/jsp/inventory.jspf" %>
<%@ include file="/console/jsp/modificationutils.jspf" %>
<%@ page import="com.yantra.yfs.ui.backend.*" %>
<%@ page import="com.yantra.yfc.util.*" %>

<script language="javascript">
	function changeTagBindings(newBinding) {
		
		var newExtnBinding = newBinding + "Extn/";

	    var tagAttributeTable = document.all("TagAttributeTable");
		var inputs = tagAttributeTable.getElementsByTagName("INPUT");

		for ( var i = 0; i < inputs.length ; i++ ) {
		    var input = inputs.item(i);
		    var inputName = input.name;
		    var j = inputName.lastIndexOf("@");
		    var attributeName = inputName.substring(j,inputName.length);
		    var isExtn = input.isExtn;
		    if (isExtn == "false") {
				input.name = newBinding + attributeName; 		    	
				alert(input.name + " = " + input.value);
		    } else {
		    	input.name = newExtnBinding + attributeName;
		    }
		}
	}	       
</script>

<%
	String tagContainer  = request.getParameter("TagContainer");
	if (isVoid(tagContainer)) {
		tagContainer = "TagContainer";
	}
	
	String tagElement  = request.getParameter("TagElement");
	if (isVoid(tagElement)) {
		tagElement = "Tag";
	}  
	
	prepareTagDetails ((YFCElement) request.getAttribute(tagContainer),tagElement,(YFCElement) request.getAttribute("ItemDetails"));
	Map identifierAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("ItemDetails"),"IdentifierAttributes");
	Map descriptorAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("ItemDetails"),"DescriptorAttributes");
	Map extnIdentifierAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("ItemDetails"),"ExtnIdentifierAttributes");
	Map extnDescriptorAttrMap = getTagAttributesMap((YFCElement) request.getAttribute("ItemDetails"),"ExtnDescriptorAttributes");
	
	String bindingPrefix  = request.getParameter("BindingPrefix");
    String targetBindingPrefix = request.getParameter("TargetBindingPrefix");
    String targetBindingAttributePrefix = request.getParameter("TargetBindingAttributePrefix");
	String extnBindingPrefix = bindingPrefix + "/Extn";
    String targetExtnBindingPrefix = targetBindingPrefix + "/Extn";
	String noOfColumns  = request.getParameter("NoOfColumns");
	String modifiable = request.getParameter("Modifiable");
	String labelTDClass = request.getParameter("LabelTDClass");
	String inputTDClass = request.getParameter("InputTDClass");
	String allowModBinding = request.getParameter("AllowModBinding");

	int totalColumns = 0;
	int currentColumn = 1;
	int columnsToFill = 0;
	
	if (!isVoid(noOfColumns)) {
		totalColumns = (new Integer(noOfColumns)).intValue();		
	} else {
		totalColumns = 3;
	}
	
    boolean isModifiable = false;
    boolean createNewTextField = false;
	boolean resetTagAttribute = false;
	if (equals(modifiable, "true")) {
		isModifiable = true;

        // If the tag attributes are editable, you can make the input fields appear
        // as a field next to the displayed value (instead of modifiable in the same
        // text fields as they are displayed). At the moment this is only valid when
        // the fields are displayed in a "list" type screen (see "ScreenType" parameter
        // below).
        String createNewTextFieldParam = request.getParameter("CreateNewTextField");
        if (equals(createNewTextFieldParam, "true")) {
            createNewTextField = true;
			String resetTagAttributeParam = request.getParameter("ResetTagAttribute");
			if (equals(resetTagAttributeParam, "true")) {
				resetTagAttribute = true;
			}
        }
	}
	
	if (isVoid(labelTDClass)) {
		labelTDClass = "detaillabel";
	}
	
	if (isVoid(inputTDClass)) {
		inputTDClass = "";
	}
    
    boolean identifiersOnly = false;
    String identifiersOnlyParam = request.getParameter("IdentifiersOnly");
    if (equals(identifiersOnlyParam, "true")) {
		identifiersOnly = true;
	}
    String screenType = request.getParameter("ScreenType");
    if (isVoid(screenType)) {
		screenType = "detail";
	}
    boolean listHeadersOnly = false; 
    String listHeadersOnlyParam = request.getParameter("ListHeadersOnly");
    if (equals(listHeadersOnlyParam, "true")) {
		listHeadersOnly = true;
	}

if (!equals(screenType, "list")) { %>
    <table class="view" width="100%" id="TagAttributeTable">
<% } %>
	<%String legendName = "";
	int i = 0;

	//Check if 'HideDescriptorAttributes' param is passed as true. If so, then we only want to go through the following while loop once.
	//i.e. We only want to show the 'Identifier' attributes and not the 'Descriptor' attributes.
	String hideDescriptorAttributes = request.getParameter("HideDescriptorAttributes");
	int x = 2;
	if (equals(hideDescriptorAttributes, "true")) {
		x = 1;
	}
	while (i < x) { 
		int j = 0;
		currentColumn = 1;
		columnsToFill = 0;
		Map normalMap = null;
		Map extnMap = null;
		Map currentMap = null;
		if (i == 0) {
			normalMap = identifierAttrMap;
			extnMap = extnIdentifierAttrMap;
			legendName = "Tag_Identifiers";
		} else {
			normalMap = descriptorAttrMap;
			extnMap = extnDescriptorAttrMap;
			legendName = "Tag_Attributes";
		}		
		if ((normalMap != null) || (extnMap != null)) {
            if (!equals(screenType, "list")) { %>
                <tr>
                    <td>
                        <fieldset>
                        <legend><yfc:i18n><%=legendName%></yfc:i18n></legend> 
                            <table class="view" width="100%">
                                <tr>
            <% } %>
							<%while (j < 2) {
								String currentBindingPrefix = "";
                                String currentTargetBindingPrefix = "";
								boolean isExtn = false;
								if (j == 0) {
									currentMap = normalMap;
									isExtn = false;
									currentBindingPrefix = bindingPrefix;
                                    currentTargetBindingPrefix = targetBindingPrefix;
								} else {
									currentMap = extnMap;
									isExtn = true;
									currentBindingPrefix = extnBindingPrefix;
                                    currentTargetBindingPrefix = targetExtnBindingPrefix;
								}
								//Need to add a dummy hidden attribute so the tag element is always posted.
								if(j == 0) {%>
									<input type="hidden" <%=getTextOptions(currentTargetBindingPrefix + "/@dummy", "dummyVal")%>/>
								<%}
								if (currentMap != null) {
									if (!currentMap.isEmpty()) {
										for (Iterator k = currentMap.keySet().iterator(); k.hasNext();) {
									        String currentAttr = (String) k.next();
									        String currentAttrValue = (String) currentMap.get(currentAttr);

									                                                
											if (equals(screenType, "list")) {
                                                if (listHeadersOnly) { %>
                                                    <td class="tablecolumnheader" sortable="no"><yfc:i18n><%=currentAttr%></yfc:i18n></td>
                                                <% }
                                                else { %>
                                                    <td class="tablecolumn" sortable="no"><%=currentAttrValue%>
                                                    
                                                    <% if (createNewTextField) { %>
                                                        <br/>
														<%
															String computedtargetBinding = "";
															if(isExtn){
																computedtargetBinding = currentTargetBindingPrefix + "/@" + currentAttr;
															} else {
																computedtargetBinding = currentTargetBindingPrefix + "/@" + targetBindingAttributePrefix + currentAttr;
															}
														%>
														<%if(isVoid(allowModBinding)) {%> 
															<input type="text" class="unprotectedinput" isExtn=<%=isExtn%> <%if(resetTagAttribute) { %>
															<%=getTextOptions(computedtargetBinding, "")%> <%} else { %> <%=getTextOptions(computedtargetBinding)%> <%}%>/>
														<%} else {%>
															<input type="text" isExtn=<%=isExtn%> <%if(resetTagAttribute) { %> <%=yfsGetTextOptions(computedtargetBinding, "" ,allowModBinding)%> <%} else { %> <%=yfsGetTextOptions(computedtargetBinding,allowModBinding)%> <%}%>/>
														<%}%>
														<%if(equals(YFCDataTypeRepository.getInstance().getDataTypeForAttribute(currentAttr),"Date")) { %>
															<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
														<%}%>
                                                    <% } %>
                                                    </td>
                                                <% }
                                            }
                                            else { %>
                                                <td class=<%=HTMLEncode.htmlEscape(labelTDClass)%>><yfc:i18n><%=currentAttr%></yfc:i18n></td>
                                                
                                                <%if (isModifiable) { %>
                                                      
                                                      <%if ((!isVoid(targetBindingPrefix)) && (!isVoid(bindingPrefix))) { %>
                                                      
                                                           <td class=<%=HTMLEncode.htmlEscape(inputTDClass)%> nowrap="true">
																<%if(isVoid(allowModBinding)) {%> 
																	<input type="text" class="unprotectedinput" isExtn=<%=isExtn%> <%=getTextOptions(currentTargetBindingPrefix + "/@" +currentAttr ,currentBindingPrefix + "/@" +currentAttr) %> />
																<%} else {%>
																	<input type="text" isExtn=<%=isExtn%> <%=yfsGetTextOptions(currentTargetBindingPrefix + "/@" +currentAttr ,currentBindingPrefix + "/@" +currentAttr ,allowModBinding) %> />
																<%}%>
																<%if(equals(YFCDataTypeRepository.getInstance().getDataTypeForAttribute(currentAttr),"Date")) { %>
																	<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
																<%}%>
														   </td>                                           
																                                             
                                                      <% } else { %>
                                                      
                                                           <td class=<%=HTMLEncode.htmlEscape(inputTDClass)%> nowrap="true">
																<%if(isVoid(allowModBinding)) {%> 
																	<input type="text" class="unprotectedinput" isExtn=<%=isExtn%> <%=getTextOptions(currentBindingPrefix + "/@" +currentAttr) %> />
																<%} else {%>
																	<input type="text" isExtn=<%=isExtn%> <%=yfsGetTextOptions(currentBindingPrefix + "/@" +currentAttr ,allowModBinding) %> />
																<%}%>
																<%if(equals(YFCDataTypeRepository.getInstance().getDataTypeForAttribute(currentAttr),"Date")) { %>
																	<img class="lookupicon" name="search" onclick="invokeCalendar(this);return false" <%=getImageOptions(YFSUIBackendConsts.DATE_LOOKUP_ICON, "Calendar") %> />
																<%}%>
														   </td>
                                                      <% } %>
                                                    
                                                <%} else {%>
                                                
                                                    <%
													if(equals(YFCDataTypeRepository.getInstance().getDataTypeForAttribute(currentAttr),"Date"))  {
												        YFCDate yfcDate = YFCDate.getYFCDate(currentAttrValue);
												        String currentValue = yfcDate.getDateString(getLocale());	%>		
														
														<td class="protectedtext"><%=currentValue%></td>

											         <%       }   
													else {  %>
													   
													    <td class="protectedtext"><%=currentAttrValue%></td>

													<% } %>
												 


                                                    	
                                                    
                                                <%}
                                                columnsToFill = totalColumns - currentColumn;	
                                                if ((currentColumn % totalColumns) == 0) {
                                                       currentColumn = 1; %>
                                                       
                                                       </tr><tr>
                                                       
                                                <%} else {
                                                        currentColumn++;
                                                }
                                            }
								        }
								    }
								}
					        	if ((j == 1) && (columnsToFill != 0)) {
					        		for (int n = 0; n < columnsToFill; n++) {%>  
					        			<td></td><td></td>
					        		<%}
					        	}
					        	j++;
					        }

                            if (!equals(screenType, "list")) { %>
					        </tr>
					   	</table>
					</fieldset>	
				</td>
			</tr>
		    <% }
        }
        // If only the identifier attributes should be displayed, then skip the rest by
        // setting the "i" loop counter to 2 (so the while loop ends).
        if (identifiersOnly) {
            i = 2;
        }
        else {
    		i++;
        }
	}
if (!equals(screenType, "list")) { %>
    </table>
<% } %>