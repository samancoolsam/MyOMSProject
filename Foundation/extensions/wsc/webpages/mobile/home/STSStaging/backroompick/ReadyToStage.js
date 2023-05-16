scDefine([
	//OMNI-6588: STS 1.0/1.1 Author - Radhakrishna Mediboina (CTS POD Team)
	/*
	1.	Store user scans the container to stage and enters staging location.
	2.	User will not be able to stage if 
			a.	The container number does not exist in the system.
			b.	The container does not belong to the store.
			c.	The container was marked as �Lost�
			d.	The customer order was cancelled.
			e.	The TO was not fully received (SO Shipment was not created).
	3.	If the container was cancelled, then update the ExtnCancellationActionedAt as �Staging� if current staging location is empty else update it as �AfterStaging� since user was informed that order was cancelled by customer.
	4.	Warning will be displayed if the container was already staged.
	5.	If the user wants to change staging location, update the staging location on TO container. Update staging location on SO shipment lines.
	6.	If the container was not staged, then set the staging location on TO container. Set staging location on SO shipment lines.
	7.	When all shipment lines are staged, then move the shipment and order to ready for customer pick status. Send ready for collection email to customer.
	*/
	
	"dojo/text!./templates/ReadyToStage.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!ias/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!ias/utils/BaseTemplateUtils",
	"scbase/loader!wsc/mobile/home/utils/MobileHomeUtils",
	"scbase/loader!ias/utils/EventUtils",
	"scbase/loader!ias/utils/ScreenUtils",
	"scbase/loader!ias/utils/ContextUtils",
	"scbase/loader!sc/plat/dojo/utils/ControllerUtils",
	"scbase/loader!dojo/_base/connect",
	"scbase/loader!dojo/dom-attr"
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
	_iasEventUtils,
	_iasScreenUtils,
	_iasContextUtils,
	_scControllerUtils,
	dConnect,
	dDomAttr
) {
	return _dojodeclare("extn.mobile.home.STSStaging.backroompick.ReadyToStage", [_scScreen], {
		templateString: templateText,
		uId: "ReadyToStage",
		packageName: "extn.mobile.home.STSStaging.backroompick",
		className: "ReadyToStage",
		title: "Title_ReadyToStage",
        screen_description: "STS Stage Container",	
		
		namespaces: {
			targetBindingNamespaces: [
            {
                value: 'extn_getChangedPrinterDeviceID',
                description: "This namespace is used to take printer option selected."
            },
			{
                value: 'extn_selectedPrinterDevice',
                description: "This namespace is used to take printer option selected."
            }
            ],
			sourceBindingNamespaces: [
				{
					description: "The details of the container",
					value: 'scannedContainerDetails'
				},
				{
					description: 'This namespace contains the Printer ID from session',
					value: 'extn_printerFromSession1'
				
				},
				{
					value: 'getPrinterDeviceList_output_ns',
					description: "This namespace contains the Printer device list"
				}
			] 			
		},

		subscribers: {
			local: [	
			{
                eventId: 'extn_ScanContainerNo_onClick',
                sequence: '32',
                handler: {
                    methodName: "validateContainerOnEnter"
                }
			},			
			{
                eventId: 'extn_ScanContainerNo_onKeyDown',
                sequence: '32',
                handler: {
                   methodName: "validateContainerOnEnter"
                }
			},
			{
                eventId: 'extn_scanTOContainerBarCode_onClick',
                sequence: '30',
                description: 'Subscriber for scan barcode',
                listeningControlUId: 'extn_scanTOContainerBarCode',
                handler: {
                    methodName: "extn_onClickBarcode",
                    description: "To handle for scan product"
                }
          	},		
			{
                eventId: 'extn_lastScannedContainerID_onClick',
                sequence: '32',
                handler: {
                    methodName: "OpenSOSummaryScreen"
                }
			},			
			{
                eventId: 'extn_btnClose_onClick',
                sequence: '32',
                handler: {
                    methodName: "OnCloseAction"
                }
			},			
			{
				eventId: 'assignStagingButton_onClick',
				sequence: '32',
				handler: {
					methodName: "assignStagingLocation"
				}
			},			
			{
				eventId: 'extn_assignStagingLoc_onClick',
				sequence: '32',
				handler: {
					methodName: "assignStagigLocOnEnter"
				}
			},			
			{
				eventId: 'extn_assignStagingLoc_onKeyDown',
				sequence: '32',
				handler: {
					methodName: "assignStagigLocOnEnter"
				}
			},
			{
                eventId: 'afterScreenInit',
                sequence: '32',
                handler: {
                   methodName: "initializeScreen"
                }
			},
                {
                eventId: 'extn_ShipToStoreOrderSearch_onClick',
                sequence: '32',
                handler: {
                    methodName: "openSTSOrderSearchScreen"
                }
			},
			{
				eventId: 'afterScreenLoad',
				sequence: '32',
				handler: {
					methodName: "extn_afterScreenLoad"
				}
			},
			{
				eventId: 'extn_filterning_select_print_to_onChange',
				sequence: '32',
				handler: {
					methodName: "storePrinterInSessionSTS"
				}
			}			
			]
		},
	
		setInitialized: function(event, bEvent, ctrl, args) {
            this.isScreeninitialized = true;
        },
		
		extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_ScanContainerNo");
						this.printerIDFromSession();

        },
		
		//OMNI-9176: Start
		extn_afterContainerScan: function(event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_assignStagingLoc");
        },
		//OMNI-9176: End
		_removeReadOnlyState: function() {
				var fs = this.getWidgetByUId("extn_filterning_select_print_to");
				//dDomAttr.remove(fs.text, "readonly");
			},
		_addReadOnlyState: function() {
				var fs = this.getWidgetByUId("extn_filterning_select_print_to");
				//dDomAttr.set(fs.text, "readonly", true);
			},
      //START OMNI-98581	
			globalOOTB_AssemblyReq : false,
      //END  OMNI-98581	
		initializeScreen:function(event, bEvent, ctrl, args){
			//var inputModel = _scScreenUtils.getInitialInputData(this);
            //var sInvokedFrom = _scModelUtils.getStringValueFromPath("InvokedFrom", inputModel);
			//if(!_scBaseUtils.equals(sInvokedFrom,"HomeScreen"))
			//{
			var sessionModel = window.sessionStorage;
			var ContainerModel = JSON.parse(window.sessionStorage.getItem("LastScannedContainerModel"));
			if(!_scBaseUtils.isVoid(ContainerModel))
			{
				var sContianerNo= _scModelUtils.getStringValueFromPath("ContainerNo", ContainerModel);
				_scWidgetUtils.setValue(this, "extn_lastScannedContainerID",sContianerNo, null);
			}
			_scWidgetUtils.hideWidget(this,"containerStagedPanel", false);
			//}
			//OMNI-63938: Start
			var selectedPrinterModel = null;
			selectedPrinterModel = _scScreenUtils.getTargetModel(this, "extn_selectedPrinterDevice",null);
			var storePrinterID=_iasContextUtils.getFromContext("PrinterID");
			var model1 = {};
			if(!_scBaseUtils.isVoid(storePrinterID)) {
				_scModelUtils.setStringValueAtModelPath("Devices.Device.DeviceId",storePrinterID,model1);
				_scScreenUtils.setModel(this, "extn_printerFromSession1", model1);
				_iasContextUtils.addToContext("IsHipPrinterEnabled", "Y");
			}
			else {
				_scScreenUtils.setModel(this, "getPrinterDeviceList_output_ns", model1);
			}
			_iasUIUtils.callApi(this, selectedPrinterModel, "getPrinterDeviceRef", null);
      		//OMNI-63938: End
		},
		
		validateContainerOnEnter: function(event, bEvent, ctrl, args) {
			if(_iasEventUtils.isEnterPressed(event)) {
				this.validateContainerNo();
			}
        },
		
		extn_onClickBarcode:function(event, bEvent, ctrl,args){
			this.validateContainerNo();				
		},
			
		validateContainerNo: function(event, bEvent, ctrl, args) {			
			var sTargetModel = _scScreenUtils.getTargetModel(this, "ContainerIDFromUI", null);
			var sContainerNo = _scModelUtils.getStringValueFromPath("Container.ContainerID", sTargetModel);
			var mashupInput = _scBaseUtils.getNewModelInstance();
			if (!_scBaseUtils.isVoid(sContainerNo)){
				_scModelUtils.setStringValueAtModelPath("Container.ContainerNo", sContainerNo ,mashupInput);	
				_scScreenUtils.setModel(this, "mScannedContainerID", mashupInput, null);					
				_iasUIUtils.callApi(this, mashupInput, "extn_validateScannedContainerID", null);
			}else{
				_iasBaseTemplateUtils.showMessage(
					this, "No containers scanned.", "error", null);
			}
			_scWidgetUtils.hideWidget(this,"containerStagedPanel", false);			
		},
		
		assignStagigLocOnEnter: function(event, bEvent, ctrl, args) {
			if (_iasEventUtils.isEnterPressed(event)) {
					this.assignStagingLocation();
			}
        },
		
		assignStagingLocation: function(event, bEvent, ctrl, args) {
			/*
			var mScannedContainerID = _scScreenUtils.getModel(this,"mScannedContainerID");
			//var sessionModel = window.sessionStorage;
			if(_scBaseUtils.isVoid(mScannedContainerID)){
				mScannedContainerID = JSON.parse(window.sessionStorage.getItem("mScannedContainerID"));
			}
			*/
			var mvalidContainer = _scScreenUtils.getModel(this,"mvalidContainer");
			if(_scBaseUtils.isVoid(mvalidContainer)){
				mvalidContainer = JSON.parse(window.sessionStorage.getItem("mvalidContainer"));
			}		
			/*
			var sContainerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", mScannedContainerID);			
			if(_scBaseUtils.isVoid(sContainerNo))
			{
				sContainerNo = _scModelUtils.getStringValueFromPath("ContainerNo", mvalidContainer);
			}
			*/
			var mLastScannedContainer = _scScreenUtils.getTargetModel(this, "Last_Scanned_Container", null);
			var sLastScannedContainer = _scModelUtils.getStringValueFromPath("LastScannedContainer", mLastScannedContainer);			
			var mScannedStagingLoc = _scScreenUtils.getTargetModel(this, "HoldLocation_Add_staging", null);
			var sScannedStagingLoc = _scModelUtils.getStringValueFromPath("HoldLocation", mScannedStagingLoc);			
			if (!_scBaseUtils.isVoid(sLastScannedContainer)){
				if (!_scBaseUtils.isVoid(sScannedStagingLoc)){
					var mashupInput = _scBaseUtils.getNewModelInstance();					
					var sShipmentContainerKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", mvalidContainer);
					var sShipmentKey = _scModelUtils.getStringValueFromPath("ShipmentKey", mvalidContainer);
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sShipmentKey ,mashupInput);
					_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.ShipmentContainerKey", sShipmentContainerKey ,mashupInput);
					_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.Zone", sScannedStagingLoc ,mashupInput);						
					_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsReceived", "Y" ,mashupInput);	
					_iasUIUtils.callApi(this, mashupInput, "extn_assignStagingLocationAtContainerLevel", null);						
				}else{
					_iasBaseTemplateUtils.showMessage(
							this, "No Staging Location scanned.", "error", null);
				}
			}else{
				_iasBaseTemplateUtils.showMessage(
						this, "No containers scanned.", "error", null);
				_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
				_scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
				_scWidgetUtils.hideWidget(this,"containerStagedPanel", false);
			}			
		},
    	//OMNI-63938: Start - Display Print To drop down only when HIP Printer is enabled 
		afterhandleMashupOutput: function() {
			var isHipPrinterEnabledFlag=_iasContextUtils.getFromContext("IsHipPrinterEnabled");
			if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag)){
				if(isHipPrinterEnabledFlag=="N"){					
					_scWidgetUtils.hideWidget(this, "extn_filterning_select_print_to", false);
				}
			}		
		  //OMNI-63938: End
        
			var fs = this.getWidgetByUId("extn_filterning_select_print_to");
			this._addReadOnlyState();
			dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
			dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			
      //OMNI-56182: Start
			var bindings = null;
			bindings = {};
			var screenConstructorParams = null;
			screenConstructorParams = {};
			var popupParams = null;
			popupParams = {};
			popupParams["binding"] = bindings;
			popupParams["screenConstructorParams"] = screenConstructorParams;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "printerIDFromSession";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			var printerModel=_iasContextUtils.getFromContext("PrinterID");
			var isHipPrinterEnabledFlag=_iasContextUtils.getFromContext("IsHipPrinterEnabled");
			if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag)){
				if(isHipPrinterEnabledFlag=="Y"){
					if(_scBaseUtils.isVoid(printerModel)) {
						_iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "User does not have a printer assigned. Please<br/>select a valid printer from the dropdown.", this, popupParams, dialogParams);
					}
					else{
						var sPrinterID=_iasContextUtils.getFromContext("PrinterID");
						if(_scBaseUtils.isVoid(sPrinterID)){
							_iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "User does not have a printer assigned. Please<br/>select a valid printer from the dropdown.", this, popupParams, dialogParams);
						}	
					}
				}					
			} 
      //OMNI-56182: End				
		},		
		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
	   	    //OMNI-63938: Start
        	if ( _scBaseUtils.equals(mashupRefId, "getPrinterDeviceRef")) {		
				_scScreenUtils.setModel(this, "getPrinterDeviceList_output_ns", modelOutput, null);
				var sPrinterCount = modelOutput.Devices;
				if(!_scBaseUtils.isVoid(sPrinterCount)){
					_iasContextUtils.addToContext("IsHipPrinterEnabled", "Y");
					var deviceList= _scModelUtils.getStringValueFromPath("Devices.Device", modelOutput);
					for(var i in deviceList){
						var deviceModel = _scModelUtils.getStringValueFromPath("",deviceList[i]);  
						var dviceAttributes = _scModelUtils.getStringValueFromPath("DeviceParamsXML.Attributes.Attribute", deviceModel);
						for(var j in dviceAttributes){
							var deviceAttributeModel = _scModelUtils.getStringValueFromPath("", dviceAttributes[j]);
							var deviceDisplayName = _scModelUtils.getStringValueFromPath("DisplayName", deviceAttributeModel);
							if(deviceDisplayName == "IP_ADDRESS"){
								var deviceIPAddress = _scModelUtils.getStringValueFromPath("Value", deviceAttributeModel);
								_scModelUtils.setStringValueAtModelPath("DeviceIP", deviceIPAddress , deviceList[i]);

							}
						}
					}
				    window.sessionStorage.setItem("PrinterModel",JSON.stringify(modelOutput));
				}
				else {
					_iasContextUtils.addToContext("IsHipPrinterEnabled", "N");
				}
				this.afterhandleMashupOutput();
				
			}
      		//OMNI-63938: End
 			if(_scBaseUtils.equals(mashupRefId,"extn_validateScannedContainerID"))
			{
				if(_scModelUtils.hasAttributeInModelPath("Containers.Container", modelOutput))
				{
					_scScreenUtils.setModel(this, "getShipmentContainerList_outptut", modelOutput, null);
					var mScannedContainerID = _scScreenUtils.getModel(this,"mScannedContainerID");					
					var sContainers = modelOutput.Containers.Container;		
					//OMNI-10249  STS - Container ID Belongs To a Different Store 		
					//for(var i in sContainers)
					for(var i=0; i<1; i++)
					{
						var sContainerNo = _scModelUtils.getStringValueFromPath("ContainerNo", sContainers[i]);						
						if(!_scBaseUtils.isVoid(sContainerNo) &&
						_scBaseUtils.equals(sContainerNo,_scModelUtils.getStringValueFromPath("Container.ContainerNo", mScannedContainerID)))
						{							
							var sReceivingNode = _scModelUtils.getStringValueFromPath("Shipment.ReceivingNode", sContainers[i]);	
							if(!_scBaseUtils.isVoid(sReceivingNode) && 
							_scBaseUtils.equals(_iasContextUtils.getFromContext("CurrentStore"),sReceivingNode))
							{
								//OMNI-90509 START
								var sPackListType = _scModelUtils.getStringValueFromPath("Shipment.PackListType", sContainers[i]);
								if(!_scBaseUtils.isVoid(sPackListType) && _scBaseUtils.equals(sPackListType, "FA")){
									_iasScreenUtils.showErrorMessageBoxWithOk(this, this.getSimpleBundleString('extn_StageFireArm_Message'));											
										return 0;										
								}
								//OMNI-90509 END
									var sIsConCncld = _scModelUtils.getStringValueFromPath("Extn.ExtnIsSOCancelled", sContainers[i]);
									var sZone = _scModelUtils.getStringValueFromPath("Zone", sContainers[i]);	
									var sSalesOHKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.ChainedFromOrderHeaderKey", sContainers[i]);
									var sSHPKey = _scModelUtils.getStringValueFromPath("ShipmentKey", sContainers[i]);
									var sSHPConKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", sContainers[i]);
									//OMNI-9142: Start
									//Fetch the TO Shipment Status
									var sTOShpStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", sContainers[i]);
									if(!_scBaseUtils.isVoid(sTOShpStatus) && _scBaseUtils.equals(sTOShpStatus,"1600.001"))
									{
										var mashupInput = _scBaseUtils.getNewModelInstance();
										_scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", sSalesOHKey ,mashupInput);
										_iasUIUtils.callApi(this, mashupInput, "extn_getSOOrderStatus", null);
									}
									//OMNI-9142: End
									if(_scBaseUtils.isVoid(sIsConCncld) || !_scBaseUtils.equals(sIsConCncld,"Y"))
									{
										if(_scBaseUtils.isVoid(sZone)){
											_scScreenUtils.setModel(this, "mvalidContainer", sContainers[i], null);										
											//_scScreenUtils.setModel(this, "mScannedContainerDetails", modelOutput, null);										
											//_scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
											//_scWidgetUtils.setValue(this, "extn_lastScannedStagingLoc", null, null);
											var mashupInput = _scBaseUtils.getNewModelInstance();
											_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", sSalesOHKey ,mashupInput);
											_iasUIUtils.callApi(this, mashupInput, "extn_getSOShipmentList", null);											
										}else if(_scBaseUtils.equals(sZone,"LOST")){
											_iasBaseTemplateUtils.showMessage(
													this, "This order has been canceled. Customer reservation has been removed, work the product to the sales floor. Scan next Container ID.", "error", null);
											_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
											_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);											
										}else if(!_scBaseUtils.isVoid(sZone) && !_scBaseUtils.equals(sZone,"LOST")){
											_iasBaseTemplateUtils.showMessage(
													this, "Container ID has been already Staged, verify staging location. Scan next Container ID.", "error", null);
											_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
											_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
										}
									}else if(_scBaseUtils.equals(sIsConCncld,"Y")){
										//OMNI-OMNI - 60804 & 60806 - Updated the error message to be displayed
										_iasBaseTemplateUtils.showMessage(
												this, "This Order has been cancelled. Customer reservation has been removed, work the product to the same floor. Scan next Container ID.", "error", null);	
										_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
										_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);	
										var mashupInput = _scBaseUtils.getNewModelInstance();
										_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSHPKey ,mashupInput);
										_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.ShipmentContainerKey", sSHPConKey ,mashupInput);
										_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.IsReceived", "Y" ,mashupInput);
										if(_scBaseUtils.isVoid(sZone))
										{																				
											_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.Extn.ExtnCancellationActionedAt", "Staging" ,mashupInput);
											_iasUIUtils.callApi(this, mashupInput, "extn_updateExtnCancellationActionedOnSOCancellationByCustomer", null);
											
										}else if(!_scBaseUtils.isVoid(sZone)){
											_scModelUtils.setStringValueAtModelPath("Shipment.Containers.Container.Extn.ExtnCancellationActionedAt", "AfterStaging" ,mashupInput);
											_iasUIUtils.callApi(this, mashupInput, "extn_updateExtnCancellationActionedOnSOCancellationByCustomer", null);
										}
									}
								/*
								}else{
									_iasBaseTemplateUtils.showMessage(
											this, "Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.", "error", null);
									_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);	
								}
								*/
							}else{
								_iasBaseTemplateUtils.showMessage(
										this, "Container ID belongs to a different store. Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.", "error", null);
								_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
								_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);								
							}
						}
					}
				}else{
					_iasBaseTemplateUtils.showMessage(
								this, "Invalid Container ID. Scan a valid Container ID.", "error", null)	
					_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
					_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
				}			
			}
			//OMNI-9142: Start
			else if(_scBaseUtils.equals(mashupRefId,"extn_getSOOrderStatus")){
				//OMNI-30157: STS 1.1 - Start
				_scScreenUtils.setModel(this, "getSOOrderDetails_outptut", modelOutput, null);
				/*
				var sOrderLines = modelOutput.Order.OrderLines.OrderLine;					
				for(var i in sOrderLines)
				{
					var sFulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType", sOrderLines[i]);
					if(!_scBaseUtils.isVoid(sFulfillmentType) && _scBaseUtils.equals(sFulfillmentType,"STS")){
						var sMaxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", sOrderLines[i]);
						if(!_scBaseUtils.isVoid(sMaxLineStatus) && sMaxLineStatus < '3200'){
							_iasBaseTemplateUtils.showMessage(
									this, "Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.", "error", null);
							_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
							_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
						}
					}
				}
				*/
				//OMNI-30157: STS 1.1 - End
			}
			//OMNI-9142: End
			else if(_scBaseUtils.equals(mashupRefId,"extn_getSOShipmentList")){				
				if(_scModelUtils.hasAttributeInModelPath("Shipments.Shipment", modelOutput))
				{
					var sSOShipments = modelOutput.Shipments.Shipment;					
					for(var i in sSOShipments)
					{				
						var sSOShpLines = sSOShipments[i].ShipmentLines.ShipmentLine;
						for(var j in sSOShpLines)
						{
							var sFulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", sSOShpLines[j]);	
							var sOLKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", sSOShpLines[j]);
							var mvalidContainer = _scScreenUtils.getModel(this,"mvalidContainer");							
							var sTOShpLines = mvalidContainer.Shipment.ShipmentLines.ShipmentLine;
							for(var k in sTOShpLines)
							{
								var sChainedOLKey = _scModelUtils.getStringValueFromPath("ChainedFromOrderLineKey", sTOShpLines[k]);								
								if(_scBaseUtils.equals(sFulfillmentType,"STS") && _scBaseUtils.equals(sOLKey,sChainedOLKey))
								{
									//OMNI-30157: STS 1.1 - Start
									/*
									var sStatus = _scModelUtils.getStringValueFromPath("Status.Status", sSOShipments[i]);
									_scScreenUtils.setModel(this, "mvalidShipment", sSOShipments[i], null);
									if(_scBaseUtils.equals(sStatus,"1100.70.06.10") || _scBaseUtils.equals(sStatus,"1100.70.06.20"))
									{
										var svalidContainerID = _scModelUtils.getStringValueFromPath("ContainerNo", mvalidContainer);
										_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", svalidContainerID, null);
										_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
										this.extn_afterContainerScan();
									}
									*/
									var sSOSHPNo = _scModelUtils.getStringValueFromPath("ShipmentNo", sSOShipments[i]);
									_scScreenUtils.setModel(this, "mvalidShipment", sSOShipments[i], null);
									var sExtnSOShipmentNo = _scModelUtils.getStringValueFromPath("Extn.ExtnSOShipmentNo", mvalidContainer);
									if(!_scBaseUtils.isVoid(sExtnSOShipmentNo) && _scBaseUtils.equals(sExtnSOShipmentNo,sSOSHPNo))
									{
										var svalidContainerID = _scModelUtils.getStringValueFromPath("ContainerNo", mvalidContainer);
										_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", svalidContainerID, null);
										_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
										this.extn_afterContainerScan();
									}
									//OMNI-30157: STS 1.1 - End
									//OMNI-6591: Start
									else {
										//OMNI-9199: Start - Updated Error message
										/*										
										_iasBaseTemplateUtils.showMessage(
												    this, "Containers of the shipment not completely received at Store. Proceed scanning next Container ID.", "error", null);
										*/
										_iasBaseTemplateUtils.showMessage(
												    this, "The Container ID is not ready to be Staged. Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.", "error", null);
										//OMNI-9199: End - Updated Error message
										 _scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
										 _scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
										 _scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
									}
									//OMNI-6591: End
								}
							}
						}
					}        
				}				
				//OMNI-6591: Start
				else {
					//OMNI-9199: Start - Updated Error message
					/*										
					_iasBaseTemplateUtils.showMessage(
								this, "Containers of the shipment not completely received at Store. Proceed scanning next Container ID.", "error", null);
					*/
					_iasBaseTemplateUtils.showMessage(
							    this, "The Container ID is not ready to be Staged. Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.", "error", null);
					//OMNI-9199: End
					_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
					_scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
					_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);					
				}
				//OMNI-6591: End
			}
			else if (_scBaseUtils.equals(mashupRefId, "extn_assignStagingLocationAtContainerLevel")) {
				var sSHPKey = "";
				var sSHPNo = "";
				var sSHPNode = "";
				var mScannedStagingLocFrmUI = _scScreenUtils.getTargetModel(this, "HoldLocation_Add_staging", null);
				var sScannedStagingLoc = _scModelUtils.getStringValueFromPath("HoldLocation", mScannedStagingLocFrmUI);
				var mvalidShipment = _scScreenUtils.getModel(this, "mvalidShipment");
				//var sessionModel = window.sessionStorage;                
				if (_scBaseUtils.isVoid(mvalidShipment)) {
					mvalidShipment = JSON.parse(window.sessionStorage.getItem("mvalidShipment"));
				}
				sSHPKey = _scModelUtils.getStringValueFromPath("ShipmentKey", mvalidShipment);
				//OMNI-62990 -STS Order Ticket                
				var isHipPrinterEnabledFlag =_iasContextUtils.getFromContext("IsHipPrinterEnabled");
				var sPrinterId =_iasContextUtils.getFromContext("PrinterID");
				var sPrinterIP =_iasContextUtils.getFromContext("IPAddress"); 
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSHPKey, inputModel);
				sSHPNo = _scModelUtils.getStringValueFromPath("ShipmentNo", mvalidShipment);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", sSHPNo, inputModel);
				sSHPNode = _scModelUtils.getStringValueFromPath("ShipNode", mvalidShipment);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", sSHPNode, inputModel);
				//OMNI-66709 : START
				if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag) && isHipPrinterEnabledFlag=="Y" && !_scBaseUtils.isVoid(sPrinterId) && !_scBaseUtils.isVoid(sPrinterIP)){
					//Print with HIP Printer
					_scModelUtils.setStringValueAtModelPath("Shipment.PrinterID", sPrinterId, inputModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.PrinterIP", sPrinterIP, inputModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", isHipPrinterEnabledFlag, inputModel);
									
				} else {
					//OMNI-66709 Fallback to print with Loftware 
					_scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", "N", inputModel);
				}
				_scModelUtils.setStringValueAtModelPath("Shipment.FulfillmentType", "STS", inputModel);
				_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "N", inputModel);
				_iasUIUtils.callApi(this, inputModel, "extn_PrintSTSOrderTicket_ref", null);
				//OMNI-66709 : END
			
				//OMNI-62990 -STS Order Ticket- end
				var sSOShpLines = mvalidShipment.ShipmentLines.ShipmentLine;
				for(var i in sSOShpLines)
				{
					var sSOLineKey = _scModelUtils.getStringValueFromPath("OrderLine.OrderLineKey", sSOShpLines[i]);
					var sCurrentLoc = _scModelUtils.getStringValueFromPath("Extn.ExtnStagingLocation", sSOShpLines[i]);					
					var mvalidContainer = _scScreenUtils.getModel(this,"mvalidContainer");
					if(_scBaseUtils.isVoid(mvalidContainer)){
						mvalidContainer = JSON.parse(window.sessionStorage.getItem("mvalidContainer"));
					}					
					var sTOShpLines = mvalidContainer.Shipment.ShipmentLines.ShipmentLine;					
					for(var j in sTOShpLines)
					{
						var sChainedOLKey = _scModelUtils.getStringValueFromPath("ChainedFromOrderLineKey", sTOShpLines[j]);
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
									var sUpdatedStgLoc = sCurrentLoc.concat(',').concat(sScannedStagingLoc);
									_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", sUpdatedStgLoc ,mashupInput);
									_iasUIUtils.callApi(this, mashupInput, "extn_assignStagingLocationAtShpLineLevel", null);
								}
							}
						}					
					}
				}
				//OMNI-34725: Start
				/*
				var sTOOHKey = _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey", modelOutput);
				var mashupInput = _scBaseUtils.getNewModelInstance();
				_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey", sTOOHKey ,mashupInput);
				_iasUIUtils.callApi(this, mashupInput, "extn_getTOShipmentList", null);
				*/
				var sSOOHKey = _scModelUtils.getStringValueFromPath("OrderHeaderKey", mvalidShipment);
				var mashupInput = _scBaseUtils.getNewModelInstance();
				_scModelUtils.setStringValueAtModelPath("Order.EnterpriseCode", "Academy_Direct", mashupInput);
				_scModelUtils.setStringValueAtModelPath("Order.DocumentType", "0001", mashupInput);
				_scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", sSOOHKey, mashupInput);
				_iasUIUtils.callApi(this, mashupInput, "extn_getCompleteOrderDetails", null);
				//OMNI-34725: End
			}
			//OMNI-34725: Start
			//else if(_scBaseUtils.equals(mashupRefId,"extn_getTOShipmentList")){				
			else if(_scBaseUtils.equals(mashupRefId,"extn_getCompleteOrderDetails")){
			//OMNI-34725: End
				var sSOShpKey = "";
				var sSOShpStatus = "";
				var sConCount = 0;
				var sZoneCount = 0;
				var sSOShpNo = "";
				var mvalidShipment = _scScreenUtils.getModel(this,"mvalidShipment");
				sSOShpNo = _scModelUtils.getStringValueFromPath("ShipmentNo", mvalidShipment);
				
				//OMNI-34725: Start
				/*
				var sShipments = modelOutput.Shipments.Shipment;			
				for(var i in sShipments){
					if(!_scBaseUtils.isVoid(sShipments[i].Containers.Container)){
						var sContainers = sShipments[i].Containers.Container;	
						sConCount = sConCount+sShipments[i].Containers.Container.length;
						for(var j in sContainers){
							var sZone = _scModelUtils.getStringValueFromPath("Zone", sContainers[j]);
							if(!_scBaseUtils.isVoid(sZone)){
								sZoneCount += 1;
							}
						}
					}
				}
				*/
				var sContainers = modelOutput.Order.Containers.Container;			
				for(var i in sContainers){
					if(!_scBaseUtils.isVoid(sContainers[i])){
						var sSOShipmentNo = _scModelUtils.getStringValueFromPath("Extn.ExtnSOShipmentNo", sContainers[i]);
						if(!_scBaseUtils.isVoid(sSOShipmentNo) && _scBaseUtils.equals(sSOShipmentNo, sSOShpNo)){
							sConCount += 1;
							var sZone = _scModelUtils.getStringValueFromPath("Zone", sContainers[i]);
							if(!_scBaseUtils.isVoid(sZone)){
								sZoneCount += 1;
							}
						}
					}
				}
				//OMNI-34725: End
				
				//var sessionModel = window.sessionStorage;
				if(_scBaseUtils.isVoid(mvalidShipment)){
					mvalidShipment = JSON.parse(window.sessionStorage.getItem("mvalidShipment"));
				}
				sSOShpKey = _scModelUtils.getStringValueFromPath("ShipmentKey", mvalidShipment);
				sSOShpStatus = _scModelUtils.getStringValueFromPath("Status.Status", mvalidShipment);
				
				//OMNI-9200: Start - Pick Up Quantity - Auto Default
				var sSHPLineArray = "";
				sSHPLineArray = _scBaseUtils.getNewArrayInstance();
				var sShipmentLines = mvalidShipment.ShipmentLines.ShipmentLine;
				for(var i in sShipmentLines){
					var sSHPLineKey = "";
					var sOriginalQty = "";
					var sQty = "";
					var mShpmentLine = _scBaseUtils.getNewModelInstance();
					sSHPLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", sShipmentLines[i]);
					sOriginalQty = _scModelUtils.getStringValueFromPath("OriginalQuantity", sShipmentLines[i]);
					sQty = _scModelUtils.getStringValueFromPath("Quantity", sShipmentLines[i]);
					if(!_scBaseUtils.isVoid(sQty) && !_scBaseUtils.equals(sQty,"0.00")){
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
               //OMNI-98581 START
                var chshipmentkey=this.checkAssemblyItem();
				if(_scBaseUtils.equals(sConCount, sZoneCount)) {
                      if((this.globalOOTB_AssemblyReq) && !_scBaseUtils.isVoid(chshipmentkey)) {
                          _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", chshipmentkey ,mchangeShpStatus);
                          _scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.10.5" ,mchangeShpStatus);
						  _scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "EXTN_STORE_BACKROOM_PICK.0001.ex" ,mchangeShpStatus);
					    }else{
						      _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSOShpKey ,mchangeShpStatus);		
						      _scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30.5" ,mchangeShpStatus);
					          _scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK" ,mchangeShpStatus);;	
						     }
              //OMNI-98581 END
                     _iasUIUtils.callApi(this, mchangeShpStatus, "extn_changeSOShipmentStatus", null);					
					//_iasBaseTemplateUtils.showMessage(this, "Container ID has been staged. Proceed scanning next Container ID.", "success", null);
					_scWidgetUtils.showWidget(this,"containerStagedPanel", false);
					_scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
					_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
					_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
				}else{
					if(!_scBaseUtils.equals(sSOShpStatus,"1100.70.06.20")){
						_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sSOShpKey ,mchangeShpStatus);	
						_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.20" ,mchangeShpStatus);
						_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK_IN_PROGRESS" ,mchangeShpStatus);
						_iasUIUtils.callApi(this, mchangeShpStatus, "extn_changeSOShipmentStatus", null);
					}
					//_iasBaseTemplateUtils.showMessage(this, "Container ID has been staged. Proceed scanning next Container ID.", "success", null);
					_scWidgetUtils.showWidget(this,"containerStagedPanel", false);
					_scWidgetUtils.setValue(this, "extn_assignStagingLoc", null, null);
					_scWidgetUtils.setValue(this, "extn_lastScannedContainerID", null, null);
					_scWidgetUtils.setValue(this, "extn_ScanContainerNo", null, null);
				}
				window.sessionStorage.removeItem("LastScannedContainerModel");
				window.sessionStorage.removeItem("mvalidContainer");
				window.sessionStorage.removeItem("mvalidShipment");
				window.sessionStorage.removeItem("mScannedContainerID");	
				window.sessionStorage.removeItem("ContainerModel");
				//OMNI-9176: Start				
				this.extn_afterScreenLoad();
				//OMNI-9176: End
			}
		},
      //OMNI-98581 START
     checkAssemblyItem: function(event, bEvent, ctrl, args) {
          var splDetailModel = _scScreenUtils.getModel(this, "mvalidShipment");
          var chShipmentKey=null;
		 if(_scModelUtils.hasAttributeInModelPath("ShipmentLines", splDetailModel)) {
                	//var sSOShipments = spDetailModel.Shipments.Shipment;
                       var shipmentLine = splDetailModel.ShipmentLines.ShipmentLine;
                       for(var i=0;i<shipmentLine.length; i++) {
					     var vIsAssembly = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnIsAssemblyRequired", shipmentLine[i]);
					        if (!_scBaseUtils.isVoid(vIsAssembly) && _scBaseUtils.equals(vIsAssembly, "Y")) {
								     this.globalOOTB_AssemblyReq=true;
								     chShipmentKey=splDetailModel.ShipmentKey;
								     break;
                              }
                        }
                     }
				
					return chShipmentKey ;     
			},
			//OMNI-98581 END
          handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {           
			_iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         //OMNI-98581 START
			  var recMashup = _scModelUtils.getStringValueFromPath("0.mashupRefId", inputData);
             if( _scBaseUtils.equals(recMashup,"extn_changeSOShipmentStatus")&& (this.globalOOTB_AssemblyReq)) {
			    _iasBaseTemplateUtils.showMessage(this, "This Container Has Items That Require Assembly", "information", null)
                this.globalOOTB_AssemblyReq=false;
                    } 
                    //OMNI-98581 END
			},
		
						
		OpenSOSummaryScreen: function(event, bEvent, ctrl, args){
			//_wscMobileHomeUtils.openScreen("extn.mobile.home.STSStaging.SOShipmentSummary.SOShipmentSummaryScreen", "extn.mobile.editors.ReceiveContainerEditor");
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", "extn.mobile.editors.ReceiveContainerEditor");
		},
		
		OnCloseAction: function(event, bEvent, ctrl, args){
			window.sessionStorage.removeItem("LastScannedContainerModel");
			window.sessionStorage.removeItem("mvalidContainer");
			window.sessionStorage.removeItem("mvalidShipment");
			window.sessionStorage.removeItem("mScannedContainerID");
			window.sessionStorage.removeItem("ContainerModel");
			_wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");
		},
		
		//Opening the SO Summary on click of Last Scanned ContainerNo
		OpenSOSummaryScreen: function(event, bEvent, ctrl, args){
			var sSOHeaderkey = "";
		 	var sContainersModel = _scScreenUtils.getModel(this, "getShipmentContainerList_outptut");			
		 	var containersList= _scModelUtils.getStringValueFromPath("Containers.Container", sContainersModel);
		 	for(var i in containersList){
				var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);
				var containerNo= _scModelUtils.getStringValueFromPath("ContainerNo", containerModel);
				var sLastScannedContainer = _scWidgetUtils.getValue(this,"extn_lastScannedContainerID");
				if(_scBaseUtils.equals(containerNo,sLastScannedContainer))
				{
					//sSOHeaderkey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.ChainedFromOrderHeaderKey", containerModel);
					sSOHeaderkey = _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey", containerModel);
					//OMNI-10249  STS - Container ID Belongs To a Different Store  
					break;
				}
		 	}
			
			if(_scBaseUtils.isVoid(sContainersModel)){
				//var sessionModel = window.sessionStorage;
				var ContainerModel = JSON.parse(window.sessionStorage.getItem("LastScannedContainerModel"));
				if(!_scBaseUtils.isVoid(ContainerModel))
				{				
					sSOHeaderkey=_scModelUtils.getStringValueFromPath("OrderHeaderKey", ContainerModel);
					sLastScannedContainer=_scModelUtils.getStringValueFromPath("ContainerNo", ContainerModel);		 
				}
			}else{
				window.sessionStorage.removeItem("LastScannedContainerModel");
				window.sessionStorage.removeItem("mvalidContainer");
				window.sessionStorage.removeItem("mvalidShipment");
				window.sessionStorage.removeItem("mScannedContainerID");
			}

		 	if(!_scBaseUtils.isVoid(sSOHeaderkey)){						
				var mvalidContainer = _scScreenUtils.getModel(this,"mvalidContainer");
				if(_scBaseUtils.isVoid(mvalidContainer)){
					mvalidContainer = JSON.parse(window.sessionStorage.getItem("mvalidContainer"));
				}
				var mvalidShipment = _scScreenUtils.getModel(this,"mvalidShipment");
				if(_scBaseUtils.isVoid(mvalidShipment)){
					mvalidShipment = JSON.parse(window.sessionStorage.getItem("mvalidShipment"));
				}
				var mScannedContainerID = _scScreenUtils.getModel(this,"mScannedContainerID");
				if(_scBaseUtils.isVoid(mScannedContainerID)){
					mScannedContainerID = JSON.parse(window.sessionStorage.getItem("mScannedContainerID"));
				}
				
				window.sessionStorage.setItem("mvalidContainer",JSON.stringify(mvalidContainer));
				window.sessionStorage.setItem("mvalidShipment",JSON.stringify(mvalidShipment));
				window.sessionStorage.setItem("mScannedContainerID",JSON.stringify(mScannedContainerID));
				var sessionObject = _scBaseUtils.getNewModelInstance();
				_scModelUtils.setStringValueAtModelPath("ContainerNo",sLastScannedContainer,sessionObject);
				_scModelUtils.setStringValueAtModelPath("OrderHeaderKey",sSOHeaderkey,sessionObject);
				window.sessionStorage.setItem("LastScannedContainerModel",JSON.stringify(sessionObject));
				
				var screenInput= _scBaseUtils.getNewModelInstance();
				_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey",sSOHeaderkey,screenInput);
				_scModelUtils.setStringValueAtModelPath("InvokedFrom","StageContainer",screenInput);
				_scControllerUtils.openScreenInEditor("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", screenInput, null, {}, {}, "extn.mobile.editors.ReceiveContainerEditor");
		 	}
	},
	//OMNI-63938: Start - Storing the printer in session 
	storePrinterInSessionSTS: function(event, bEvent, ctrl, args) {
		var selectedPrinterDevice = event;
        if (!_scBaseUtils.isVoid(selectedPrinterDevice)) {
			var PrinterModel = JSON.parse(window.sessionStorage.getItem("PrinterModel"));
			if(!_scBaseUtils.isVoid(PrinterModel)){
			var deviceList= _scModelUtils.getStringValueFromPath("Devices.Device", PrinterModel);
				for(var i in deviceList){
					var deviceModel = _scModelUtils.getStringValueFromPath("",deviceList[i]);  
					var deviceDisplayName= _scModelUtils.getStringValueFromPath("DeviceId", deviceModel);	
					if(deviceDisplayName == selectedPrinterDevice) {
						var sPrinterIPAddress = _scModelUtils.getStringValueFromPath("DeviceIP", deviceModel);
						_iasContextUtils.addToContext("IPAddress", sPrinterIPAddress);
						_iasContextUtils.addToContext("PrinterID", selectedPrinterDevice);
					}
				}
			}
		}
		else{
			var emptyModel = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("PrinterID", emptyModel);
		}
	},
  	//OMNI-63938: End
	//OMNI-63938: Start - Retrieving the Printer ID from session
	printerIDFromSession:function(event, bEvent, ctrl, args) {
		var sessionModel = window.sessionStorage;
		var storePrinterId =_iasContextUtils.getFromContext("PrinterID");
		var model1 = {};
		if(!_scBaseUtils.isVoid(storePrinterId)) {
			_scModelUtils.setStringValueAtModelPath("Devices.Device.DeviceId",storePrinterId,model1);
			_scScreenUtils.setModel(this, "extn_printerFromSession1", model1);
			var PrinterModel = JSON.parse(window.sessionStorage.getItem("PrinterModel"));
			  if(!_scBaseUtils.isVoid(PrinterModel)){
			    var deviceList= _scModelUtils.getStringValueFromPath("Devices.Device", PrinterModel);
				for(var i in deviceList){
					var deviceModel = _scModelUtils.getStringValueFromPath("",deviceList[i]);  
					var deviceDisplayName= _scModelUtils.getStringValueFromPath("DeviceId", deviceModel);	
					if(deviceDisplayName == storePrinterId) {
						var sPrinterIPAddress = _scModelUtils.getStringValueFromPath("DeviceIP", deviceModel);
						_iasContextUtils.addToContext("IPAddress", sPrinterIPAddress);
				    }
				}
			}
		}
	},
  	//OMNI-63938: End
		
		//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
		openSTSOrderSearchScreen: function(){
			//OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - START
			var clearSessionObject = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);
			//OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - END		
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", "extn.mobile.editors.ReceiveContainerEditor");
		},
    	//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
	});
});
