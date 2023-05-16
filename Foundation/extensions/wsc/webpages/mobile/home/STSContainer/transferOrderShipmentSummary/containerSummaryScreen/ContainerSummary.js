scDefine([
	"dojo/text!./templates/ContainerSummary.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!ias/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!ias/utils/BaseTemplateUtils",
	"scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
	"scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils",
	"scbase/loader!dojo/dom-attr",
	"scbase/loader!ias/utils/EventUtils",
	"scbase/loader!ias/utils/ContextUtils"
], function (
	templateText,
	_dojodeclare,
	_scScreen,
	_scWidgetUtils,
	_scScreenUtils,
	_scBaseUtils,
	_iasUIUtils,
	_scModelUtils,
	_iasBaseTemplateUtils,
	_wscMobileHomeUtils,
	_wscShipmentUtils,
	dDomAttr,
	_iasEventUtils,
	_iasContextUtils
	
) {
	return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerSummaryScreen.ContainerSummary", [_scScreen], {
		templateString: templateText,
		uId: "ContainerSummary",
		packageName: "extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerSummaryScreen",
		className: "ContainerSummary",
		title: "",
        screen_description: "The repeating container screen used in the Shipment Summary.",	
	
	namespaces: {
		sourceBindingNamespaces: [
			{
				description: 'The output to the getShipmentDetails mashup.',
				value: 'container_Src'
			},
			{
				value: 'getLostContainerList_ns',
				description: "This namespace contains the lostContainer reason list"
			}
			],
			targetBindingNamespaces: [
            {
                value: 'getLostContainerRsn_output_ns',
                description: "This namespace is used to store cancel reason"
            }
			]		
		},

		subscribers: {
			local: [				
			{
                eventId: 'afterScreenInit',
                sequence: '32',
                handler: {
                   methodName: "initializeScreen"
                }
			},
			{
				eventId: 'extn_btnCancel_onClick',
				sequence: '32',
				handler: {
					methodName: "cancelSOOnClick"
				}
			},
			//OMNI-71679 - Start
			{
				eventId: 'btn_editStagingButton_onClick',
				sequence: '32',
				handler: {
					methodName: "enableStagingUpdate"
				}
			},	
			{
				eventId: 'btn_updateStagingButton_onClick',
				sequence: '32',
				handler: {
					methodName: "assignStagingLocation"
				}
			},			
			{
				eventId: 'txt_assignStagingLoc_onClick',
				sequence: '32',
				handler: {
					methodName: "assignStagigLocOnEnter"
				}
			},			
			{
				eventId: 'txt_assignStagingLoc_onKeyDown',
				sequence: '32',
				handler: {
					methodName: "assignStagigLocOnEnter"
				}
			}
			//OMNI-71679 - End		
			]
		},

		initializeScreen: function (event, bEvent, ctrl, args) {
			var pScreen = _iasUIUtils.getParentScreen(this, true);
			var inputModel = _scScreenUtils.getInitialInputData(pScreen);
			_scScreenUtils.setModel(this, "inputModel", inputModel, null);
            var sInvokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);
			
			var containerModel = _scScreenUtils.getModel(this, "container_Src");
			var sContainerid = _scModelUtils.getStringValueFromPath("Container.ContainerNo", containerModel);
			var sZone = _scModelUtils.getStringValueFromPath("Container.Zone", containerModel);
			var sIsCancelled = _scModelUtils.getStringValueFromPath("Container.Extn.ExtnIsSOCancelled", containerModel);
			
			//OMNI-101574 - STS Shipments: Display shipped on date for STS shipments (TC70) - Start
			var stsShipmentUIFetaureFlag = _iasContextUtils.getFromContext("globalSTSShipmentUIFeature");
			if(_scBaseUtils.equals(stsShipmentUIFetaureFlag ,"Y")){
			var ActualShipmentDate = _scModelUtils.getStringValueFromPath("Container.Shipment.ActualShipmentDate",containerModel);
			if(!_scBaseUtils.isVoid(ActualShipmentDate))
			{
				var date = new Date(ActualShipmentDate);
				var yyyy = date.getFullYear().toString();
				var mm = (date.getMonth()+1).toString();
				var dd  = date.getDate().toString();
				
				var ddChars = dd.split('');
			
				ActualShipmentDate = mm + '/' + (ddChars[1]?dd:"0"+ddChars[0]) + '/' + yyyy;	
				_scWidgetUtils.setValue(this, "lbl_ShippedOn", ActualShipmentDate, false);
			} 
			//OMNI-101512---START
			var trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo",containerModel);
			if(!_scBaseUtils.isVoid(trackingNo))
			{
				
				_scWidgetUtils.setValue(this, "lbl_TrackingNo", trackingNo, false);
			}
			//OMNI-101571 - Start
			var trackingStatus = _scModelUtils.getStringValueFromPath("Container.Extn.ExtnTrackingStatus",containerModel);
			if(!_scBaseUtils.isVoid(trackingStatus) && ((_scBaseUtils.contains(trackingStatus,"Delivered")))){
				
				_scWidgetUtils.setValue(this, "lbl_TrackingStatus", trackingStatus, false);
			}
			else if(!_scBaseUtils.isVoid(trackingStatus)){
				
				_scWidgetUtils.setValue(this, "lbl_TrackingStatus", "In Transit", false);
			}

			//OMNI-106224 - STS Shipments: Display "Shipped From" on container summary page - Start
			var shipNode = _scModelUtils.getStringValueFromPath("Container.Shipment.ShipNode.ShipNode",containerModel);
			if(!_scBaseUtils.isVoid(shipNode)){
				shipNode = parseInt(shipNode,10);
				_scWidgetUtils.setValue(this, "lbl_ShippedFrom", shipNode, false);
			}//OMNI-106224 - STS Shipments: Display "Shipped From" on container summary page - End
			}
			else{
				_scWidgetUtils.hideWidget(this,"lbl_TrackingNo", false);
				_scWidgetUtils.hideWidget(this,"lbl_ShippedOn", false);
				_scWidgetUtils.hideWidget(this,"lbl_TrackingStatus", false);
				_scWidgetUtils.hideWidget(this,"lbl_ShippedFrom", false);//OMNI-106224 - Start/End
			}
			//OMNI-101574 - STS Shipments: Display shipped on date for STS shipments (TC70) - End
			
			if(!_scBaseUtils.isVoid(sContainerid) && (_scBaseUtils.equals(sContainerid,"Not Shipped") || (_scBaseUtils.equals(sContainerid,"Cancelled")))){
				_scWidgetUtils.hideWidget(this,"lbl_ContainerNo", false);
				_scWidgetUtils.hideWidget(this,"lbl_isReceived", false);
        //OMNI-32370 To display products that are not picked - START
			  //_scWidgetUtils.hideWidget(this,"extn_Products", false);
        //OMNI-32370 To display products that are not picked - END
				// OMNI -9190  STS Order Search - Container Status - START
				if(_scBaseUtils.equals(sContainerid,"Cancelled")){
					_scWidgetUtils.hideWidget(this,"lbl_isReceived_NC", false);
				}
				else{
					_scWidgetUtils.hideWidget(this,"lbl_isReceived_NC1", false);
				}
				// OMNI -9190  STS Order Search - Container Status - END				 
			}else{ 
				 _scWidgetUtils.hideWidget(this,"lbl_ContainerNo1", false);
				 _scWidgetUtils.hideWidget(this,"lbl_isReceived_NC", false);
				 // OMNI -9190  STS Order Search - Container Status - START
				 _scWidgetUtils.hideWidget(this,"lbl_isReceived_NC1", false);
				 // OMNI -9190  STS Order Search - Container Status - END
			}
			
			if(!_scBaseUtils.equals(sInvokedFrom,"StageContainer") && !_scBaseUtils.equals(sInvokedFrom,"SearchResults")){
				//_scWidgetUtils.hideWidget(this,"lbl_stagingLoc", false);
				_scWidgetUtils.hideWidget(this,"lbl_stagingLocLostCancel", false);
				_scWidgetUtils.hideWidget(this,"lbl_stagingLocLost", false);
				_scWidgetUtils.hideWidget(this,"lbl_ContainerCancelled", false);
			}else {
				if (_scBaseUtils.equals(sZone,"LOST")){
					_scWidgetUtils.disableWidget(this, "extn_btnCancel", true);					
					//Code Changes for OMNI-68038--Start
					_scWidgetUtils.disableWidget(this, "extn_LostReasonCode", false);
					//Code Changes for OMNI-68038--End				
					_scWidgetUtils.hideWidget(this,"lbl_stagingLoc", false);
					_scWidgetUtils.showWidget(this,"lbl_stagingLocLost", false);
					_scWidgetUtils.hideWidget(this,"lbl_stagingLocLostCancel", false);					
					_scWidgetUtils.hideWidget(this,"lbl_ContainerCancelled", false);
					if(_scBaseUtils.equals(sIsCancelled,"Y")){
						_scWidgetUtils.showWidget(this,"lbl_ContainerCancelled", false);
						_scWidgetUtils.showWidget(this,"lbl_stagingLocLostCancel", false);
						_scWidgetUtils.hideWidget(this,"lbl_stagingLocLost", false);
					}
				}else{
					_scWidgetUtils.showWidget(this,"lbl_stagingLoc", false);
					_scWidgetUtils.hideWidget(this,"lbl_stagingLocLostCancel", false);
					_scWidgetUtils.hideWidget(this,"lbl_stagingLocLost", false);
					_scWidgetUtils.hideWidget(this,"lbl_ContainerCancelled", false);
				}	
			}
			
			_scWidgetUtils.hideWidget(this,"lbl_LostReasonCode", false);	
			//OMNI-6360,6361: Start
			this.extn_afterInitializeScreen();
			//OMNI-6360,6361: End
			
      		//Lost Scenarios - Start
      		var lostReasoncode = containerModel.Container.AstraCode;
			if (_scBaseUtils.equals(sZone,"LOST") || _scBaseUtils.equals(sIsCancelled,"Y")){
					_scWidgetUtils.disableWidget(this, "extn_btnCancel", true);
					_scWidgetUtils.showWidget(this,"lbl_ContainerCancelled", false);
						//Code Changes for OMNI-68038--Start
					if(!_scBaseUtils.isVoid(lostReasoncode)){
						_scWidgetUtils.hideWidget(this, "extn_LostReasonCode", false);
						_scWidgetUtils.showWidget(this,"lbl_LostReasonCode", false);	
					}else{
					_scWidgetUtils.disableWidget(this, "extn_LostReasonCode",true);
					}
					//Code Changes for OMNI-68038--End
					if(_scBaseUtils.equals(sZone, "LOST"))
					{
						_scWidgetUtils.setValue(this,"lbl_stagingLoc","",false);
					}
			}      
			// Lost Scenarios - End
			//_iasUIUtils.callApi(this,inputModel, "extn_getTOShipmentDetails", null);		
			
			// Code Changes for OMNI-68038--Start
            this._addReadOnlyState();
            var nodeType = _scModelUtils.getStringValueFromPath("Container.Shipment.ShipNode.NodeType", containerModel);
			var receivedFlag =  _scModelUtils.getStringValueFromPath("Container.IsReceived", containerModel);
			var mashupInput = _scBaseUtils.getNewModelInstance();					
			if (!_scBaseUtils.equals(nodeType, "SharedInventoryDC") && ! _scBaseUtils.equals(receivedFlag, "Y") ) {
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "ASO_LOST_RSN_CODE" ,mashupInput);
				_iasUIUtils.callApi(this,mashupInput, "getLostContainerReasonCode", null);	
			} else {
				_scWidgetUtils.hideWidget(this, "extn_LostReasonCode", false);
			}
			// Code Changes for OMNI-68038--End	
						
			//OMNI-72148 - Start
			//Do not show Edit options when Staging Location is empty or Lost or Order is in Picked up status
			var inputModel = _scScreenUtils.getModel(this,"inputModel");
			var SOShpStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", inputModel);
			_scWidgetUtils.hideWidget(this, "lbl_stagingLocEdit", false);
			_scWidgetUtils.hideWidget(this, "txt_assignStagingLoc", false);
			_scWidgetUtils.hideWidget(this, "btn_editStagingButton", false);
			_scWidgetUtils.hideWidget(this, "btn_updateStagingButton", false);
			if (!_scBaseUtils.isVoid(sZone) && !_scBaseUtils.equals(sZone,"LOST") &&
				!(_scBaseUtils.equals(SOShpStatus, "1400") || _scBaseUtils.equals(SOShpStatus, "1600.002"))) {
				var getCommonCodeInput = {};				
				getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "EDIT_STAGING_LOC_WEBSOM" , getCommonCodeInput);
				_iasUIUtils.callApi(this,getCommonCodeInput, "extn_getFlagEditStagingLocation", null);	
			}
			//OMNI-72148 - End
		},
		
		//OMNI-6360,6361: Start
		extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
			_scWidgetUtils.hideWidget(this, "extn_btnCancel", false);
			//Code Changes for OMNI-68038--Start
			_scWidgetUtils.hideWidget(this, "extn_LostReasonCode", false);
			//Code Changes for OMNI-68038--End
			var inputModel = _scScreenUtils.getModel(this,"inputModel");
			// Lost Scenarios - START
			var SOShpStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", inputModel);
			var pScreen = _iasUIUtils.getParentScreen(this, true);
			var parentShipmentModel = _scScreenUtils.getModel(pScreen,"getShipmentDetails_output");
			var sToShipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", parentShipmentModel);
			SOShpStatus = _scModelUtils.getStringValueFromPath("Shipment.SOShipmentStatus", parentShipmentModel);
			// Lost Scenarios - START
			if((!_scBaseUtils.equals(SOShpStatus, "1100.70.06.30.5")) && (!_scBaseUtils.equals(sToShipmentStatus,"1100.70.06.10")) && (!_scBaseUtils.equals(SOShpStatus, "1400")) && (!_scBaseUtils.equals(SOShpStatus, "1600.002"))){
				var userInformation = JSON.parse(window.sessionStorage.getItem("userInformation"));
				var UserGroupList = userInformation.CurrentUser.User.UserGroupLists.UserGroupList;
				var result = Object.entries(UserGroupList); 
				for(var i = 0; i < result.length; i++){ 
					var data = result[i];
					var UsergroupId = _scModelUtils.getStringValueFromPath("1.UserGroup.UsergroupId", data);
					if(_scBaseUtils.isVoid(UsergroupId)){
						UsergroupId = _scModelUtils.getStringValueFromPath("1.UserGroup.0.UsergroupId", data);
					}
					if(_scBaseUtils.equals(UsergroupId, "ACADEMY_STORE_STS_ADMIN")){
						_scWidgetUtils.showWidget(this, "extn_btnCancel", false);
						//Code Changes for OMNI-68038--Start
						_scWidgetUtils.showWidget(this, "extn_LostReasonCode", false);
						//Code Changes for OMNI-68038--End
						// OMNI -9190  STS Order Search - Container Status - START
						var containerModel = _scScreenUtils.getModel(this, "container_Src");
						var sContainerid = _scModelUtils.getStringValueFromPath("Container.ContainerNo", containerModel);
						if(!_scBaseUtils.isVoid(sContainerid) && (_scBaseUtils.equals(sContainerid,"Not Shipped") || (_scBaseUtils.equals(sContainerid,"Cancelled"))))
			            {
			            	_scWidgetUtils.hideWidget(this, "extn_btnCancel", false);
			            	//Code Changes for OMNI-68038--Start
							_scWidgetUtils.hideWidget(this, "extn_LostReasonCode", false);
							//Code Changes for OMNI-68038--End
			            }
						// OMNI -9190  STS Order Search - Container Status - END
						break;
					}
				}
			}
		},
		
		cancelSOOnClick: function(event, bEvent, ctrl, args) {
			//_iasBaseTemplateUtils.showMessage(this, "........", "error", null);
			//var containerModelList = _scScreenUtils.getModel(this, "containerModelList");
			var containerModel = _scScreenUtils.getModel(this, "container_Src");
			var mashupInput = _scBaseUtils.getNewModelInstance();					
			var sShipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail.0.ShipmentContainerKey", containerModel);
			var sShipmentKey = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail.0.ShipmentKey", containerModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sShipmentKey ,mashupInput);
			_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.ShipmentContainerKey", sShipmentContainerKey ,mashupInput);
			_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.Zone", "LOST" ,mashupInput);
			//Code Changes for OMNI-68038--Start
			var selectedDropDown = _scScreenUtils.getWidgetByUId(this, "extn_LostReasonCode").displayedValue;
			if(!_scBaseUtils.isVoid(selectedDropDown)){
				_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.AstraCode", selectedDropDown ,mashupInput);
			}
			//Code Changes for OMNI-68038--End
			//_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsReceived", "Y" ,mashupInput);						
			_iasUIUtils.callApi(this, mashupInput, "extn_updateZoneAsLOSTAtContainerLevel", null);	
		},
		//OMNI-6360,6361: End
    
		// OMNI - 8091 Lost Scenarios - Start
		handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
            for (var i in mashupRefList) {
                var mashupRefId = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                //Code Changes for OMNI-68038--Start
				if(_scBaseUtils.equals(mashupRefId,"getLostContainerReasonCode"))
				{
					var IsDropDownEnable = _scModelUtils.getStringValueFromPath("CommonCodeList.IsDropDownEnable",mashupRefList[i].mashupRefOutput);
					if (_scBaseUtils.equals(IsDropDownEnable, "Y")) {
						_scScreenUtils.setModel(this,"getLostContainerList_ns",mashupRefList[i].mashupRefOutput,null);	
					}
					else
					{
						_scWidgetUtils.hideWidget(this, "extn_LostReasonCode", false);
					}
				
				}
				//Code Changes for OMNI-68038--end
                else if (_scBaseUtils.equals(mashupRefId, "extn_updateZoneAsLOSTAtContainerLevel")) {
					      _scWidgetUtils.disableWidget(this, "extn_btnCancel", true);
					       //Code Changes for OMNI-68038--Start
							_scWidgetUtils.disableWidget(this, "extn_LostReasonCode", false);
						  //Code Changes for OMNI-68038--End
            // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START 
					      var pScreen = _iasUIUtils.getParentScreen(this, true);
		            var cancelitemList = _scScreenUtils.getModel(pScreen, "canceledItemsList");
		            var cancelshipmentLines = null;
		            if(_scBaseUtils.isVoid(cancelitemList)){
		            cancelshipmentLines = _scBaseUtils.getNewArrayInstance();
		            }
		            else
		            {
		            	 cancelshipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",cancelitemList);
		            }
			       
					var containerModel = _scScreenUtils.getModel(this, "container_Src");
					this.extn_updateContainerCancelledQty(containerModel,cancelshipmentLines);
       // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
       				//Code Changes for OMNI-68038--Start
       				var selectedDropDown = _scScreenUtils.getWidgetByUId(this, "extn_LostReasonCode").displayedValue;
					if(!_scBaseUtils.isVoid(selectedDropDown)){
						_scModelUtils.setStringValueAtModelPath("Container.AstraCode", selectedDropDown ,containerModel);
					}
					//Code Changes for OMNI-68038--End
					_iasUIUtils.callApi(this, containerModel, "extn_markContainerAsLost", null);
					
					//OMNI-9453: Start
					var sConCount = 0;
					var sZoneCount = 0;
					var sLostCount = 0;
					var sZero = 0;
					var mashupRefInput = _scModelUtils.getStringValueFromPath(mashupRefList[i].inputData);
					//var shipment = _scModelUtils.getStringValueFromPath("Shipment", mashupRefList[i].mashupRefOutput);
					//if(!_scBaseUtils.isVoid(sShipments[i].Containers.Container)){
					var sContainers = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container", mashupRefList[i].mashupRefOutput);
					sConCount = sContainers.length;
					if(!_scBaseUtils.equals(sConCount,sZero)){
						for(var j in sContainers){
							var sZone = _scModelUtils.getStringValueFromPath("Zone", sContainers[j]);
							if(!_scBaseUtils.isVoid(sZone)){
								sZoneCount += 1;
								if(_scBaseUtils.equals(sZone,"LOST")){
									sLostCount += 1;
								}
							}
						}
					}

					//var containerModel = _scScreenUtils.getModel(this, "container_Src");		
					if(_scBaseUtils.equals(sConCount,sZoneCount) && !_scBaseUtils.equals(sConCount,sLostCount)){
						var inputModel = _scScreenUtils.getModel(this,"inputModel");
						var sSalesOHKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.ChainedFromOrderHeaderKey", mashupRefList[i].mashupRefOutput);
						var mashupInput = _scBaseUtils.getNewModelInstance();
						_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", sSalesOHKey ,mashupInput);
						_iasUIUtils.callApi(this, mashupInput, "extn_getSOShipmentList", null);					
					}					
					
				}else if(_scBaseUtils.equals(mashupRefId,"extn_getSOShipmentList")){
					var sSOShpKey = "";
					var mashupRefOutput = _scModelUtils.getStringValueFromPath(mashupRefList[i].mashupRefOutput);
					var shipmentList = _scModelUtils.getStringValueFromPath("Shipments.Shipment", mashupRefList[i].mashupRefOutput);
					var containerModel = _scScreenUtils.getModel(this, "container_Src");
					var OLKey = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail.0.ShipmentLine.OrderLine.ChainedFromOrderLine.OrderLineKey", containerModel);
					for(var i in shipmentList){
						var shipmenttype = _scModelUtils.getStringValueFromPath("ShipmentType", shipmentList[i]);
						if(_scBaseUtils.equals(shipmenttype,"STS")){
							sSOShpKey = _scModelUtils.getStringValueFromPath("ShipmentKey", shipmentList[i]);
							//OMNI-9200: Start - Pick Up Quantity - Auto Default
							var sSHPLineArray = "";
							sSHPLineArray = _scBaseUtils.getNewArrayInstance();
							var sShipmentLines = shipmentList[i].ShipmentLines.ShipmentLine;
							for(var j in sShipmentLines){
								var sSHPLineKey = "";
								var sOriginalQty = "";
								var sQty = "";
								var sOLKey = "";
								var mShpmentLine = _scBaseUtils.getNewModelInstance();
								sSHPLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", sShipmentLines[j]);
								sOriginalQty = _scModelUtils.getStringValueFromPath("OriginalQuantity", sShipmentLines[j]);
								sQty = _scModelUtils.getStringValueFromPath("Quantity", sShipmentLines[j]);
								sOLKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", sShipmentLines[j]);
								if(!_scBaseUtils.equals(sQty,"0.00") && !_scBaseUtils.equals(OLKey,sOLKey)){
									_scModelUtils.setStringValueAtModelPath("ShipmentLineKey", sSHPLineKey ,mShpmentLine);
									_scModelUtils.setStringValueAtModelPath("OriginalQuantity", sOriginalQty ,mShpmentLine);
									_scModelUtils.setStringValueAtModelPath("Quantity", sQty ,mShpmentLine);
									_scModelUtils.setStringValueAtModelPath("BackroomPickComplete", 'Y' ,mShpmentLine);
									_scModelUtils.setStringValueAtModelPath("BackroomPickedQuantity", sQty ,mShpmentLine);	
									_scBaseUtils.appendToArray(sSHPLineArray,mShpmentLine);
								}
							}
							var mchangeShipment = _scBaseUtils.getNewModelInstance();	
							_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", sSHPLineArray ,mchangeShipment);	
							_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSOShpKey ,mchangeShipment);
							_iasUIUtils.callApi(this, mchangeShipment, "extn_changeShipment_UpdatePickUpQty", null);						
							//OMNI-9200: End - Pick Up Quantity - Auto Default

							var mchangeShpStatus = _scBaseUtils.getNewModelInstance();
				            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSOShpKey ,mchangeShpStatus);
				            _scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30.5" ,mchangeShpStatus);
					        _scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK" ,mchangeShpStatus);
					        _iasUIUtils.callApi(this, mchangeShpStatus, "extn_changeSOShipmentStatus", null);
						}
					}
				}
				//OMNI-9453: End
				
				//OMNI-72148 - Start
				else if(_scBaseUtils.equals(mashupRefId,"extn_getFlagEditStagingLocation")){
					 var commonCodeModel = mashupRefList[i].mashupRefOutput;
					 if (typeof commonCodeModel.CommonCodeList.CommonCode != 'undefined' && !(_scBaseUtils.isVoid(commonCodeModel.CommonCodeList.CommonCode[0]))) {
						 var enableEditLoc = commonCodeModel.CommonCodeList.CommonCode[0].CodeShortDescription;
						 if(!(_scBaseUtils.isVoid(enableEditLoc)) && (_scBaseUtils.equals(enableEditLoc,"Y"))){
						 	_scWidgetUtils.showWidget(this, "lbl_stagingLocEdit", false);
							_scWidgetUtils.showWidget(this, "txt_assignStagingLoc", false);
							_scWidgetUtils.showWidget(this, "btn_editStagingButton", false);
							_scWidgetUtils.showWidget(this, "btn_updateStagingButton", false);
							_scWidgetUtils.hideWidget(this, "lbl_stagingLoc", false);	
							_scWidgetUtils.disableWidget(this, "txt_assignStagingLoc", false);		
							_scWidgetUtils.disableWidget(this, "btn_updateStagingButton", false);		
						} 
					}
				}
				//OMNI-72148 - End
				//OMNI-71679 - Start
				else if (_scBaseUtils.equals(mashupRefId,"extn_assignStagingLocationAtContainerLevel")) {
					var containerModel = _scScreenUtils.getModel(this, "container_Src");
					var sSalesOHKey = containerModel.Container.ContainerDetails.ContainerDetail[0].ShipmentLine.OrderLine.ChainedFromOrderLine.OrderHeaderKey;
					var mashupInput = _scBaseUtils.getNewModelInstance();
					_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", sSalesOHKey ,mashupInput);
					_iasUIUtils.callApi(this, mashupInput, "extn_getSOShipmentListToUpdateStagingLoc", null);
				} else if (_scBaseUtils.equals(mashupRefId,"extn_getSOShipmentListToUpdateStagingLoc")) {
					var mScannedStagingLoc = _scScreenUtils.getTargetModel(this, "HoldLocation_Add_staging", null);
					var sScannedStagingLoc = _scModelUtils.getStringValueFromPath("HoldLocation", mScannedStagingLoc);		
					var containerModel = _scScreenUtils.getModel(this, "container_Src");
					var existingStagingLoc = _scWidgetUtils.getValue(this,"lbl_stagingLoc");
					
					if (!_scBaseUtils.isVoid(sScannedStagingLoc)){
						var soShipments = mashupRefList[i].mashupRefOutput.Shipments.Shipment[0];
						var sSOShpLines = soShipments.ShipmentLines.ShipmentLine;
						var sSHPKey = _scModelUtils.getStringValueFromPath("ShipmentKey", soShipments);
						for(var i in sSOShpLines)
						{
							var sSOLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", sSOShpLines[i]);
							var sCurrentLoc = _scModelUtils.getStringValueFromPath("Extn.ExtnStagingLocation", sSOShpLines[i]);
							
							var sTOContainer = containerModel.Container.ContainerDetails.ContainerDetail;					
							for(var j in sTOContainer)
							{
								var sChainedOLKey = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderLineKey", sTOContainer[j]);
								
								if (_scBaseUtils.equals(sSOLineKey,sChainedOLKey))
								{
									var sSHPLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", sSOShpLines[i]);
									var mashupInput = _scBaseUtils.getNewModelInstance();
									_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSHPKey ,mashupInput);							
									_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShipmentLineKey", sSHPLineKey ,mashupInput);
									
									if(_scBaseUtils.isVoid(sCurrentLoc)){					
										_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", sScannedStagingLoc ,mashupInput);
										_iasUIUtils.callApi(this, mashupInput, "extn_assignStagingLocationAtShpLineLevel", null);
									}else{
										var sStagingLocArr = sCurrentLoc.split(',');
										if(!sStagingLocArr.includes(sScannedStagingLoc)){
											if(sStagingLocArr.includes(existingStagingLoc)) {
												sStagingLocArr.splice(sStagingLocArr.indexOf(existingStagingLoc), 1);
												sCurrentLoc = sStagingLocArr.toString();
											}
											var sUpdatedStgLoc = sCurrentLoc.concat(',').concat(sScannedStagingLoc);
											_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", sUpdatedStgLoc ,mashupInput);
											_iasUIUtils.callApi(this, mashupInput, "extn_assignStagingLocationAtShpLineLevel", null);
										}
									}
								}
							}
							
						}
					}
				}
				//OMNI-71679 - End
			}
	    },
		// OMNI - 8091 Lost Scenarios - End
  // OMNI -9408 - STS - Order Search Result - Cancellation Product Information - START 
		extn_updateContainerCancelledQty: function(containerModel,cancelshipmentLines){

         var containerDetails = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail", containerModel);
         for (var c in containerDetails)
         {
             var shipmentLine = _scModelUtils.getStringValueFromPath("ShipmentLine",containerDetails[c]);
             var containerQty = _scModelUtils.getNumberValueFromPath("Quantity", containerDetails[c]);
             var orderLineQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.OrderLine.ChainedFromOrderLine.OrderedQty", containerDetails[c]);
             var calculatedQty = orderLineQty - containerQty;
            
             _scModelUtils.setStringValueAtModelPath("ExtnShortPickedReasonCode","Inventory Shortage", shipmentLine);
             _scModelUtils.setStringValueAtModelPath("OrderLine.ItemDetails.DisplayUnitOfMeasure", "EACH", shipmentLine);
             _scModelUtils.setStringValueAtModelPath("OrderLine.ItemDetails.UOMDisplayFormat", "formattedQty", shipmentLine);
             var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLine);
 			 var orderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", shipmentLine);
             if(!_scBaseUtils.isVoid(cancelshipmentLines))
 			 {
 				for(var i in cancelshipmentLines)
 				{
 					var cshipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", cancelshipmentLines[i]);
 					var cOrderLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", cancelshipmentLines[i]);
 					if((cshipmentLineKey == shipmentLineKey) && (cOrderLineKey == orderLineKey))
 					{
						var orderLineQty = _scModelUtils.getNumberValueFromPath("OrderLine.OrderedQty", cancelshipmentLines[i]);
						calculatedQty = orderLineQty - containerQty;
						_scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", calculatedQty.toString(),shipmentLine);
 						_scBaseUtils.removeItemFromArray(cancelshipmentLines, cancelshipmentLines[i]);
 					}
 					
 				}
 			}
	     _scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", calculatedQty.toString(),shipmentLine);
             _scBaseUtils.appendToArray(cancelshipmentLines,shipmentLine);
         }

          if(!_scBaseUtils.isVoid(cancelshipmentLines))
             {
					 var cancelledItemListModel = _scBaseUtils.getNewModelInstance();
                    _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", cancelshipmentLines, cancelledItemListModel);
                    var pScreen = _iasUIUtils.getParentScreen(this, true);
                    _scScreenUtils.setModel(pScreen, "canceledItemsList", cancelledItemListModel, null);
                    _scWidgetUtils.showWidget(pScreen,"extn_tpCancellationDetails",false);

					/*if(! (_scBaseUtils.equals(maxorderStatus,"9000") && _scBaseUtils.equals(minorderStatus,"9000")))
					{
						_scWidgetUtils.showWidget(this,"extn_tpCancellationDetails",false);
					}*/
             }
		},
 //    OMNI -9408 - STS - Order Search Result - Cancellation Product Information - END
 		 //Code Changes for OMNI-68038--Start
 		_addReadOnlyState: function() {
			var fs = this.getWidgetByUId("extn_LostReasonCode");
			dDomAttr.set(fs.textbox, "readonly", true);
		},
		 //Code Changes for OMNI-68038--End
		 
		  //OMNI-71679 - Start
		 enableStagingUpdate: function(event, bEvent, ctrl, args) {
			_scWidgetUtils.enableWidget(this, "txt_assignStagingLoc", false);		
			_scWidgetUtils.enableWidget(this, "btn_updateStagingButton", false);
        },
        
		 assignStagigLocOnEnter: function(event, bEvent, ctrl, args) {
			if (_iasEventUtils.isEnterPressed(event)) {
					this.assignStagingLocation();
			}
        },
		
		assignStagingLocation: function(event, bEvent, ctrl, args) {
			var containerModel = _scScreenUtils.getModel(this, "container_Src");
			var sShipmentContainerKey = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail.0.ShipmentContainerKey", containerModel);
			var sShipmentKey = _scModelUtils.getStringValueFromPath("Container.ContainerDetails.ContainerDetail.0.ShipmentKey", containerModel);
			
			var mScannedStagingLoc = _scScreenUtils.getTargetModel(this, "HoldLocation_Add_staging", null);
			var sScannedStagingLoc = _scModelUtils.getStringValueFromPath("HoldLocation", mScannedStagingLoc);			
			if (!_scBaseUtils.isVoid(sScannedStagingLoc)){
				var mashupInput = _scBaseUtils.getNewModelInstance();					
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sShipmentKey ,mashupInput);
				_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.ShipmentContainerKey", sShipmentContainerKey ,mashupInput);
				_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.Zone", sScannedStagingLoc ,mashupInput);						
				_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsReceived", "Y" ,mashupInput);	//OMNI-74761						
				_iasUIUtils.callApi(this, mashupInput, "extn_assignStagingLocationAtContainerLevel", null);						
			}else{
				_iasBaseTemplateUtils.showMessage(
						this, "No Staging Location scanned.", "error", null);
			}
		}
		//OMNI-71679 - End
	});
});
