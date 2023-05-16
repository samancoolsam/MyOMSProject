<%@ include file="/yfc/rfutil.jspf" %>
<%@ include file="/rf/wms/count/count_include.jspf" %>
<%@ page import="com.yantra.yfc.core.YFCObject" %>

<%  String formName = "/frmCartPick"; 
	String focusField = null;
	String errorDesc = null;
	String errorField = null;
	double opUOMQty=0;
	String opUOM="";
	String sOrgCode = resolveValue("xml:CurrentUser:/User/@Node");
	String sUserId = resolveValue("xml:CurrentUser:/User/@Loginid");
	String sConsiderInProgressTasks = getParameter("xml:/Task/@ConsiderInProgressTasks");
	String sPrevScannedLocation = getParameter("xml:/PrevScannedLocation/@PrevScannedLocation");
	String sEquipmentId="";
	String sZoneId="";
	String sForceLocation="";
	String entitybase= getParameter("entitybase") ;
	String forwardPage = "";
	String sBatchNo="";
	String sIsConsolidated="";
	String sSourceLocationId="";
    String ItemforgetNodeItemDetails="";
	String sItemId="";
	String sQuantity="";
	String sUOM = "";
	String sAltUomQty = "";
	String sDeleteTempQ = getParameter("xml:/Delete/@DeleteTempQ");
	double totalRecords = 0;
	
	YFCElement EquipmentContextualInfoElem= (YFCElement)((getTempQ()).getElementsByTagName("EquipmentContextualInfo")).item(0);
	if(!isVoid(EquipmentContextualInfoElem)){
		sEquipmentId = EquipmentContextualInfoElem.getAttribute("EquipmentId");
	}else{
		sEquipmentId = getParameter("xml:/Task/@EquipmentId");
	}

	//getBatchContextualInfo from tempQ()
	YFCElement BatchContextualInfoElem= (YFCElement)((getTempQ()).getElementsByTagName("BatchContextualInfo")).item(0);
		if(BatchContextualInfoElem!=null){
		   sBatchNo = BatchContextualInfoElem.getAttribute("BatchNo");
		  }else{
		   sBatchNo = getParameter("xml:/Task/@BatchNo");
		  }

  if(isVoid(sBatchNo)){
   errorDesc = "Mobile_Cart_Not_Inducted_to_a_Batch";
  }
  if(equals("Y",sDeleteTempQ)){
	deleteAllFromTempQ("Task");
	deleteAllFromTempQ("Inventory");
  }
	if(isVoid(errorDesc)){
		YFCElement inElem = YFCDocument.createDocument("Task").getDocumentElement();
		inElem.setAttribute("UserId", sUserId);
		inElem.setAttribute("OrganizationCode", sOrgCode);
		YFCElement taskRefElem = inElem.createChild("TaskReferences");
		taskRefElem.setAttribute("BatchNo", sBatchNo);
		YFCElement oComplexQuery = inElem.createChild("ComplexQuery");
		YFCElement oAndElem = oComplexQuery.createChild("And");
		List valueList = new ArrayList();
		valueList.add("9000");
		valueList.add("2000");
		for( Iterator valueListItr = valueList.iterator();valueListItr.hasNext();) {
			String sValue = (String)valueListItr.next();	
			YFCElement oExpElem = oAndElem.createChild("Exp");
			oExpElem.setAttribute("Name","TaskStatus");
			oExpElem.setAttribute("QryType","NE");
			oExpElem.setAttribute("Value",sValue);
		}

		YFCElement getTaskListTemplate = YFCDocument.parse("<TaskList><Task TaskId=\"\" AssignedToUserId=\"\" TaskKey=\"\" TaskStatus=\"\" HoldReasonCode=\"\" ><Inventory/></Task></TaskList>").getDocumentElement();
		
		%><yfc:callAPI apiName="getTaskList" inputElement='<%=inElem%>' templateElement="<%=getTaskListTemplate%>" outputNamespace="InProgressTaskList"/><%	
		errorDesc=checkForError() ;	
		if(isVoid(errorDesc)){
			//If tasks are found then, fwd it to frmCartPickDeposit
			YFCElement oInProgressTaskList = (YFCElement)request.getAttribute("InProgressTaskList");
				if(oInProgressTaskList != null){
					YFCNodeList taskList = oInProgressTaskList.getElementsByTagName("Task");
					if(taskList != null){
						totalRecords = taskList.getLength();
					}
				}
			if(totalRecords > 0){
				YFCElement taskListElem = (YFCElement)request.getAttribute("InProgressTaskList");
				YFCNodeList taskList = taskListElem.getElementsByTagName("Task");
				boolean bHasOpenTasks = false;
				for(int i=0;i<taskList.getLength() && isVoid(errorDesc);i++){
					YFCElement taskElem =(YFCElement) taskList.item(i);
					if(equals("1300",taskElem.getAttribute("TaskStatus"))){
						String sAssignedUserId = taskElem.getAttribute("AssignedToUserId");
						if(!equals(sUserId,sAssignedUserId)){
							errorDesc = getI18N("Mobile_Cart_picking_already_started_by_user_")+sAssignedUserId;
						}
					}else if(!bHasOpenTasks && ("1100".equals(taskElem.getAttribute("TaskStatus")) || "1200".equals(taskElem.getAttribute("TaskStatus")))){
						bHasOpenTasks = true;
					}
				}
				if(isVoid(errorDesc) && !bHasOpenTasks){
					request.setAttribute("xml:/Task/@EquipmentId",sEquipmentId);
					forwardPage = entitybase+"/frmCartPickDeposit.jsp";
					forwardPage=checkExtension(forwardPage);
					%>
					<jsp:forward page='<%=forwardPage%>' >
						<jsp:param name="xml:/Task/@BatchNo" value='<%=sBatchNo%>'/>
					</jsp:forward>
					<%	
				}
			}
		}
	}
  //call getNextTask
  if(isVoid(errorDesc)){
	  //calling markTaskAvailability for the user so that getNextTask can look for the next task
	  //This is called because if getNextTask returns no task then it sets TaskAvailable for the user as 'N' in the YFS_USER_ACTIVITY. getNextTask looks into task availability before it  actually to get next valid task.
		YFCDocument taskAvailDoc = YFCDocument.createDocument ("TaskAvailability"); 
		YFCElement taskAvailDocElem = taskAvailDoc.getDocumentElement(); 
		taskAvailDocElem.setAttribute( "UserId" , resolveValue("xml:CurrentUser:/User/@Loginid"));
		taskAvailDocElem.setAttribute( "Availability" , true);
		%>
		<yfc:callAPI apiName="markTaskAvailability" inputElement="<%=taskAvailDocElem%>" />
		<%
		
		//calling getNextTask
     YFCDocument inDocTogetNextTask = YFCDocument.createDocument("GetNextTask");
		YFCElement inXMLTogetNextTask = inDocTogetNextTask.getDocumentElement();
		inXMLTogetNextTask.setAttribute("UserId", sUserId);
		inXMLTogetNextTask.setAttribute("OrganizationCode", sOrgCode);
		inXMLTogetNextTask.setAttribute("ConsolidateAcrossBatchReference", "Y");
		inXMLTogetNextTask.setAttribute("EquipmentId", sEquipmentId);
		inXMLTogetNextTask.setAttribute("AutoAccept","Y");
                inXMLTogetNextTask.setAttribute("ConsolidateSerials", "Y");
		YFCElement taskElement = inXMLTogetNextTask.createChild("Task");
		YFCElement taskReferencesElement = taskElement.createChild("TaskReferences");
		taskReferencesElement.setAttribute("BatchNo", sBatchNo);
		
		YFCElement orderByElement = inXMLTogetNextTask.createChild("OrderBy");
		YFCElement attributeElement = orderByElement.createChild("Attribute");
		attributeElement.setAttribute("Desc", "SourceSortSequence");
		attributeElement.setAttribute("Name", "SourceSortSequence");
		
		YFCElement attributeElement1 = orderByElement.createChild("Attribute");
		attributeElement1.setAttribute("Desc", "TaskPriority");
		attributeElement1.setAttribute("Name", "TaskPriority");
		
		YFCElement attributeElement2 = orderByElement.createChild("Attribute");
		attributeElement2.setAttribute("Desc", "SourceLocationId");
		attributeElement2.setAttribute("Name", "SourceLocationId");
		
		YFCElement attributeElement3 = orderByElement.createChild("Attribute");
		attributeElement3.setAttribute("Desc", "ReferenceSortSequence");
		attributeElement3.setAttribute("Name", "ReferenceSortSequence");
		
		YFCElement attributeElement4 = orderByElement.createChild("Attribute");
		attributeElement4.setAttribute("Desc", "FinishNoLaterThan");
		attributeElement4.setAttribute("Name", "FinishNoLaterThan");
		
		YFCElement attributeElement5 = orderByElement.createChild("Attribute");
		attributeElement5.setAttribute("Desc", "ItemId");
		attributeElement5.setAttribute("Name", "ItemId");
		
		YFCElement attributeElement6 = orderByElement.createChild("Attribute");
		attributeElement6.setAttribute("Desc", "TaskId");
		attributeElement6.setAttribute("Name", "TaskId");
		
	
		YFCElement getNextTaskTemplate = YFCDocument.parse("<TaskList UserId=\"\"><Task EnterpriseKey=\"\" SourceZoneId=\"\" SourceLocationId=\"\" OrganizationCode=\"\" IsConsolidatedTask=\"\" TaskId=\"\" TaskKey=\"\"><Inventory ItemId=\"\"  Quantity=\"\" UnitOfMeasure=\"\" ProductClass=\"\" SerialNo=\"\" TargetCaseId=\"\" CaptureTagInExecution=\"\" CaptureTagAttributesInExecution=\"\"><Item DisplayItemId=\"\"><PrimaryInformation DisplayItemDescription=\"\" SerializedFlag=\"\" NumSecondarySerials=\"\"/> <AlternateUOMList/> <InventoryParameters  TagControlFlag=\"\" IsSerialTracked=\"\" TimeSensitive=\"\"/> </Item> </Inventory> <TaskReferences/> <TaskType/> </Task> </TaskList>").getDocumentElement();
		
		//call getNextTask API
		%><yfc:callAPI apiName="getNextTask" inputElement='<%=inXMLTogetNextTask%>' templateElement="<%=getNextTaskTemplate%>" outputNamespace="TaskList"/><%	
		errorDesc=checkForError() ;	

        YFCDocument ydoc = getForm(formName) ;
		YFCElement oNextTask = (YFCElement)request.getAttribute("TaskList");
		 YFCElement oTask = oNextTask.getChildElement("Task");
		   if(oTask!=null){
			   //Adding the getNextTask output to the tempQ. This task element is used for rejecting tasks when user presses 'F10' key. 
					try{					
						deleteFromTempQ("Task", "GetNextTask");
					}catch(Exception e){}
					try{
						addToTempQ("Task","GetNextTask", oTask.getAttributes(), false);
					}catch(Exception e){}
		             request.setAttribute("xml:/Task/@TaskId","");
					 sIsConsolidated = oTask.getAttribute("IsConsolidatedTask");
					 sSourceLocationId = oTask.getAttribute("SourceLocationId");
					 sZoneId = oTask.getAttribute("SourceZoneId");
					 YFCElement oInventory = oTask.getChildElement("Inventory");
					 if(oInventory!=null){
						  ItemforgetNodeItemDetails = oInventory.getAttribute("ItemId");
						  YFCElement item = oInventory.getChildElement("Item");					 
					 if(item!=null){
			         sItemId = item.getAttribute("DisplayItemId");
					 }else{
					  sItemId = oInventory.getAttribute("ItemId");
					 }
					sQuantity = oInventory.getAttribute("Quantity");
					double suggestedQty=Double.parseDouble(sQuantity);
					sUOM = oInventory.getAttribute("UnitOfMeasure");
					
					// code for alternate UOM starts here
					if(item!=null){ 
				YFCElement alternateUOMList=item.getChildElement("AlternateUOMList");
				if(alternateUOMList!=null){ 
					YFCNodeList uomList=alternateUOMList.getElementsByTagName("AlternateUOM");
					if((uomList!=null)&&(uomList.getLength()>0)){ 
						int i=0;
						while( i < uomList.getLength()){
							YFCElement altUOM=(YFCElement)uomList.item(i);
							if(!(sUOM.equals(altUOM.getAttribute("UnitOfMeasure")))){
								//sAltUomQty = altUOM.getAttribute("Quantity");
								double altUOMQty=Double.parseDouble(altUOM.getAttribute("Quantity"));
								if((altUOMQty <= suggestedQty)&&(altUOMQty > opUOMQty)){
										opUOMQty=altUOMQty;
										opUOM=altUOM.getAttribute("UnitOfMeasure");
										sAltUomQty = altUOMQty+"";
								}
							}
							i++;
						}

						int numOfOpUOMs=0;
						double numOfInvUOMs=0;
						if(opUOMQty!=0){ 
								numOfOpUOMs=(int)(suggestedQty/opUOMQty) ;
								numOfInvUOMs=suggestedQty%opUOMQty;
								
								request.setAttribute("xml:/Task1/Inventory/@OpQty",(new Integer(numOfOpUOMs)).toString());
								
								request.setAttribute("xml:/Task/Inventory/@InvQuantity",(getFormattedDouble(numOfInvUOMs))); 

								
								YFCElement Elem1 = getField(ydoc, "lblOUom");

								if(Elem1!=null){
											Elem1.setAttribute("value",opUOM);
											Elem1.setAttribute("subtype", "ProtectedText") ;
											Elem1.setAttribute("type", "text") ;
								}
								YFCElement Elem2 =getField(ydoc, "txtOUom");

								if(Elem2!=null){
											Elem2.setAttribute("subtype", "Text") ;
											Elem2.setAttribute("type", "text") ;
											Elem2.setAttribute("validate", true) ;
								}
								
								YFCElement Elem3 =getField(ydoc, "lblPlus");

								if(Elem3!=null){
											Elem3.setAttribute("subtype", "Label") ;
											Elem3.setAttribute("type", "text") ;
								}

								YFCElement Elem4 =getField(ydoc, "lblEqualTo");

								if(Elem4!=null){
											Elem4.setAttribute("subtype", "Label") ;
											Elem4.setAttribute("type", "text") ;
								}
								
								request.setAttribute("xml:/Task1/Inventory/@OpUomQty", getFormattedDouble(opUOMQty));
								
								request.setAttribute("xml:/Task1/Inventory/@OpUom",opUOM);
						} else{
						    request.setAttribute("xml:/Task/Inventory/@InvQuantity", getFormattedDouble(suggestedQty)); 
						}
						
					} else { 
						request.setAttribute("xml:/Task/Inventory/@InvQuantity",getFormattedDouble(suggestedQty)); 
					}

				} 

			}// ends here

			}
			//Get display ItemId and Description
			YFCDocument inputDoc = YFCDocument.parse("<Item ItemID=\"" + ItemforgetNodeItemDetails +"\" OrganizationCode=\"" + resolveValue("xml:/TaskList/Task/@EnterpriseKey") +"\"   Node=\""+ getValue("CurrentUser","xml:CurrentUser:/User/@Node")+"\" UnitOfMeasure= \"" + sUOM+"\" /> ");

			YFCDocument templateDoc = YFCDocument.parse("<Item><PrimaryInformation ShortDescription=\"\" DisplayItemDescription=\"\"/><InventoryTagAttributes /><AlternateUOMList><AlternateUOM/></AlternateUOMList></Item>",true);
			try {
			%>
				<yfc:callAPI apiName="getNodeItemDetails" inputElement='<%=inputDoc.getDocumentElement()%>'  templateElement='<%=templateDoc.getDocumentElement()%>' outputNamespace='Item' />
			<%		
				YFCElement item=(YFCElement)request.getAttribute("Item");
				Map attrMap= item.getChildElement("InventoryTagAttributes").getAttributes();
				Map extnAttrMap = null;
				YFCElement extnElem = item.getChildElement("InventoryTagAttributes").getChildElement("Extn");
				if(!isVoid(extnElem)){
					extnAttrMap = extnElem.getAttributes();
				}				
				try{
					//put inventory tag attrs of item into tempq
					addToTempQ("InventoryTagAttributes", "1" , attrMap , false ) ;
					if(!isVoid(extnAttrMap)){
						//put inventory tag attrs of item into tempq
						addToTempQ("InventoryTagExtnAttributes", "1" , extnAttrMap, false ) ;						
					}
				}catch(Exception e){}
			} catch (Exception e) {
				//Discard it.
			}

               //call getZoneDetails
			if (errorDesc == null) {
            YFCDocument tempDocForZone = YFCDocument.parse("<Zone/>");
            YFCDocument zoneDoc = YFCDocument.createDocument("Zone");
            YFCElement zoneInXML = zoneDoc.getDocumentElement();
            zoneInXML.setAttribute("ZoneId",sZoneId);
            zoneInXML.setAttribute("Node",sOrgCode);

            %><yfc:callAPI apiName="getZoneDetails" inputElement='<%=zoneInXML%>'
            templateElement='<%=tempDocForZone.getDocumentElement()%>' /><%
            errorDesc=checkForError() ;

            if(errorDesc == null) {
                YFCElement zoneOutElem = (YFCElement)request.getAttribute("Zone");
                if(zoneOutElem != null) {
                    sForceLocation  = zoneOutElem.getAttribute("ForceLocnScanOnVisit");
                }
            }
        }
			   //
			   request.setAttribute("xml:/TaskList/Task/@SourceLocationId",sSourceLocationId);
			   if(equals(sPrevScannedLocation,sSourceLocationId)){
			    request.setAttribute("xml:/TaskList1/Task/@SourceLocationId",sSourceLocationId);
			   }
			   if(equals("N",sForceLocation)){
				YFCElement locationScanElem=getField(ydoc,"txtLocationId");
			    if(locationScanElem!=null){
                    locationScanElem.setAttribute("validate",false);
                    locationScanElem.setAttribute("type","text");
                    locationScanElem.setAttribute("subtype","ProtectedText");
					request.setAttribute("xml:/TaskList1/Task/@SourceLocationId",sSourceLocationId);
					}
			   }
			   request.setAttribute("xml:/TaskList9/Task/Inventory/@ItemId",sItemId);
			   request.setAttribute("xml:/Task/@ConsolidationFlag",sIsConsolidated);
			   request.setAttribute("xml:/Task1/@TaskId",oTask.getAttribute("TaskId"));
			   request.setAttribute("xml:/TaskList/@SuggQty",sQuantity);
			   request.setAttribute("xml:/TaskList/Task/Inventory/@UnitOfMeasure",sUOM);
			   request.setAttribute("xml:/Batch1/@BatchNo",sBatchNo);
			   if (!isVoid(sAltUomQty)){
					request.setAttribute("xml:/Task1/Inventory/@OpUomQty",getFormattedDouble(Double.parseDouble(sAltUomQty)));
			   }
			   request.setAttribute("xml:/Task/@ItemId","");
			   request.setAttribute("xml:/Task/@SourceLocationId","");
			   request.setAttribute("xml:/Task1/@ForceLocationScan",sForceLocation);
			   request.setAttribute("xml:/Task1/@EquipmentId",sEquipmentId);


			   out.println(sendForm(ydoc,"txtLocationId",true)) ;
		   } else {
			   formName = "/frmNoOpenTasks";
			   out.println(sendForm(formName,"txtInfo")) ;
		   }
		}
	    if (!isVoid(errorDesc)) {		
	    errorField = "txtCartId" ;
		String errorXML = getErrorXML(errorDesc, errorField);
		%><%=errorXML%><%
	}
		   
		%>

	
