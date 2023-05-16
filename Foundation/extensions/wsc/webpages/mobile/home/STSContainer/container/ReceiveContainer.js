scDefine([
	"dojo/text!./templates/ReceiveContainer.html",
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
	"scbase/loader!sc/plat/dojo/utils/ControllerUtils",
	"scbase/loader!ias/utils/ContextUtils",
	"scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils"
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
	_scControllerUtils,
	_iasContextUtils,
	_scPlatformUIFmkUtils

) {
	return _dojodeclare("extn.mobile.home.STSContainer.container.ReceiveContainer", [_scScreen], {
		templateString: templateText,
		uId: "ReceiveContainer",
		packageName: "extn.mobile.home.STSContainer.container",
		className: "ReceiveContainer",
		title: "Title_ReceiveTOContainer",
        screen_description: "Receive TO Container",	
		namespaces: {


			targetBindingNamespaces: [
				{
				description: 'The input to the getContainerList mashup.',
				value: 'getContainerList'
			}

			],		
		
		sourceBindingNamespaces: [
				{
				description: 'To store the BatchNo at the JS level',
				value: 'BatchNoModel'
			},
			{
				description: 'To store the getShipmentContainerList at the JS level',
				value: 'getShipmentContainerList_outptut'
			},


			] 
		},
		subscribers: {
			local: [				
				{
                eventId: 'extn_btnFinish_onClick',
                sequence: '32',
                handler: {
                    methodName: "extn_OnCloseAction"
                }
			},
			{
                eventId: 'extn_scanTOContainer_onClick',
                sequence: '32',
                handler: {
                    methodName: "receiveOnClick"
                }
			},
			{
                eventId: 'extn_txt_ContainerNo_onKeyUp',
                sequence: '32',
                handler: {
                    methodName: "containerActionOnEnter"
                }
			},
			{
                eventId: 'extn_ViewReceivedContainers_onClick',
                sequence: '32',
                handler: {
                    methodName: "openSTSReceivedContainer"
                }
			},
			{
                eventId: 'extn_ViewPendingContainers_onClick',
                sequence: '32',
                handler: {
                    methodName: "openSTSPendingContainer"
                }
			},
			{
                eventId: 'extn_Last_Scanned_ContainerID_onClick',
                sequence: '32',
                handler: {
                    methodName: "openTOSummaryScreen"
                }
			},
		{
                eventId: 'extn_SearchTOShipments_onClick',
                sequence: '32',
                handler: {
                    methodName: "openSTSOrderSearchScreen"
                }
			},
		{
                eventId: 'extn_lastScannedContainer_onClick',
                sequence: '32',
                handler: {
                    methodName: "openToSummaryScreen"
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
                    eventId: 'afterScreenLoad',
                    sequence: '32',
                    handler: {
                        methodName: "extn_afterScreenLoad"
                    }
                },			
			]
		},

		extn_afterScreenLoad: function(event, bEvent, ctrl, args) {
            _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_txt_ContainerNo");
        },
		
		containerActionOnEnter: function(
        event, bEvent, ctrl, args) {
            if (
            _iasEventUtils.isEnterPressed(
            event)) {
                this.receiveOnClick(
                event, bEvent, ctrl, args);
            }
        },

		receiveOnClick: function(event, bEvent, ctrl, args) {
		var sTargetModel = _scScreenUtils.getTargetModel(this, "ContainerNo_Output", null);
		var sContainerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", sTargetModel);
		if (!_scBaseUtils.isVoid(sContainerNo)){
				_scModelUtils.setStringValueAtModelPath("Container.ContainerNo", sContainerNo ,sTargetModel);
				_iasUIUtils.callApi(this, sTargetModel, "getExtnSTSContainerList", null);
				
				
		}else{
			_iasBaseTemplateUtils.showMessage(
                        this, "Enter Container ID", "error", null);
						return 0;	
		}
				
	},
						
		openTOSummaryScreen: function(){								
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", "extn.mobile.editors.ReceiveContainerEditor");
		},

	handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		var sContainerCount = 0;
            if(_scBaseUtils.equals(mashupRefId, "getExtnSTSContainerList")){
					if(!_scBaseUtils.isVoid(modelOutput.AcadSTSTOContainersList.AcadSTSTOContainers)){
						
						sContainerCount = modelOutput.AcadSTSTOContainersList.AcadSTSTOContainers.length;					
						if(sContainerCount != 0){
							_iasBaseTemplateUtils.showMessage(
								this, "Duplicate Container ID scanned. Proceed scanning next Container ID.", "error", null);
								_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
								return 0;	
						}	
											
					}else{
						var sTargetModel = _scScreenUtils.getTargetModel(this, "ContainerNo_Output", null);
						var sContainerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", sTargetModel);
						_scModelUtils.setStringValueAtModelPath("Container.ContainerNo", sContainerNo ,sTargetModel);
						_iasUIUtils.callApi(this, sTargetModel, "getShipmentContainerList", null);	
						
					}
			}else if(_scBaseUtils.equals(mashupRefId, "getShipmentContainerList")){
				
					if(_scBaseUtils.isVoid(modelOutput.Containers.Container)){
					
						_iasBaseTemplateUtils.showMessage(
								this, "No Container ID found, order has been canceled. Customer reservation for these items have been removed, work the product to the sales floor. Scan next Container ID.", "error", null);
								_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
								return 0;	
						
					}else if(!_scBaseUtils.isVoid(modelOutput.Containers.Container[0].ContainerNo)){
						var sContainers = modelOutput.Containers.Container;
						//for(i=0; i < sContainers.length; i++)
            // OMNI-10249  STS - Container ID Belongs To a Different Store 
						for(i=0; i < 1; i++)
						{
						var sShpContKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey", sContainers[i]);
						var sZone = _scModelUtils.getStringValueFromPath("Zone", sContainers[i]);
						var sReceivingStore = _scModelUtils.getStringValueFromPath("Shipment.ReceivingNode", sContainers[i]);
						var sLoginStore = _iasContextUtils.getFromContext("CurrentStore");
						var sShpStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", sContainers[i]);
						var sShpKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", sContainers[i]);
						//OMNI-89251 STS Firearm changes- Start
						var sCode = _scModelUtils.getStringValueFromPath("Shipment.PackListType", sContainers[i]);
						if(!_scBaseUtils.isVoid(sCode) && _scBaseUtils.equals(sCode, "FA")){
							_iasScreenUtils.showErrorMessageBoxWithOk(this, this.getSimpleBundleString('extn_Firearm_Message'));
							_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);								
								return 0;	
									
						}
						//OMNI-89251 STS Firearm changes- END
						
						var sIsConCncld = _scModelUtils.getStringValueFromPath("Extn.ExtnIsSOCancelled", sContainers[i]);
						if(_scBaseUtils.equals(sShpStatus, "1100.70.06.10")){
							_iasScreenUtils.showErrorMessageBoxWithOk(
								this, "The Container ID is not ready to be received. Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.");
							_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);								
								return 0;	
									
						}else if(sReceivingStore != sLoginStore){
							_iasScreenUtils.showErrorMessageBoxWithOk(
								this, "Container ID belongs to a different store. Place container in the exception area and email SFSAdmin@Academy.com. Scan next Container ID.");
								_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
								return 0;
						}else if(sZone == "LOST"){
							_iasScreenUtils.showErrorMessageBoxWithOk(
								this, "This order has been canceled. Customer reservation has been removed, work the product to the sales floor. Scan next Container ID.");	
								_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
								return 0;								
						}
          //OMNI - 60804 & 60806 - Removed mashup for calling changeShipment, ExtnCancellationActionedAt will be updated to 'Receving' while receiving
						if(_scBaseUtils.equals(sIsConCncld,"Y")){							
							_iasBaseTemplateUtils.showMessage(
											this, "This order has been canceled. Customer reservation has been removed, work the product to the sales floor. Scan next Container ID.", "error", null);
						}
						}
					
						var sTargetModel = _scScreenUtils.getTargetModel(this, "ContainerNo_Output", null);
						var sContainerNo = _scModelUtils.getStringValueFromPath("Container.ContainerNo", sTargetModel);
						//fetch the source batchno model from UI
						var sBatchModel = _scScreenUtils.getModel(this, "BatchNoModel");
						var sBatchNo = _scModelUtils.getStringValueFromPath("BatchNoModel.BatchNo", sBatchModel);
						if(!_scBaseUtils.isVoid(sBatchNo)){
								_scModelUtils.setStringValueAtModelPath("Container.BatchNo", sBatchNo ,sTargetModel);
						}else{
								_scModelUtils.setStringValueAtModelPath("Container.BatchNo", "" ,sTargetModel);
						}
						//OMNI-9405 - Start
						var userId = _scPlatformUIFmkUtils.getUserId();
						_scModelUtils.setStringValueAtModelPath("Container.Createuserid", userId, sTargetModel);
						//OMNI-9405 - End
						//input targetmodel to invoke backend  service
						_scModelUtils.setStringValueAtModelPath("Container.ContainerNo", sContainerNo ,sTargetModel);
						//storing getShipmentContainerList to get the orderheaderkey
						_scScreenUtils.setModel(this,"getShipmentContainerList_outptut",modelOutput,"");					
						_iasUIUtils.callApi(this, sTargetModel, "createAndReceiveTOContainer", null);	
						
					}					
			}else if (_scBaseUtils.equals(mashupRefId, "createAndReceiveTOContainer")) {  
					if(!_scBaseUtils.isVoid(modelOutput.AcadSTSTOContainers.ContainerNo)){
					var sContainerNo = _scModelUtils.getStringValueFromPath("AcadSTSTOContainers.ContainerNo", modelOutput);
					var sOutBatchNo = _scModelUtils.getStringValueFromPath("AcadSTSTOContainers.BatchNo", modelOutput);
					_scWidgetUtils.setValue(this, "extn_lastScannedContainer", sContainerNo, null);
					_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
					
					// clearing the session objects
					  window.sessionStorage.removeItem("ContainersModel");
		 			  window.sessionStorage.removeItem("LastScannedContainerModel");
				
					//fetch the UI source batchno model and set the batchno
					var sBatchModel = _scScreenUtils.getModel(this, "BatchNoModel");
					var sBatchNo = _scModelUtils.getStringValueFromPath("BatchNoModel.BatchNo", sBatchModel);
					if(_scBaseUtils.isVoid(sBatchNo)){
					var sBatchNoModel = _scBaseUtils.getNewModelInstance(); 
					_scModelUtils.setStringValueAtModelPath("BatchNoModel.BatchNo", sOutBatchNo,sBatchNoModel);
					_scScreenUtils.setModel(this,"BatchNoModel",sBatchNoModel,null);
					}	
								
				//count of scanned containers for a batch
					var sCountModel = _scScreenUtils.getTargetModel(this, "Count_Input", null);						
					_scModelUtils.setStringValueAtModelPath("Container.BatchNo", sOutBatchNo ,sCountModel);
					_iasUIUtils.callApi(this, sCountModel, "getExtnSTSContainerCountList", null);
					
				}
			}else if(_scBaseUtils.equals(mashupRefId, "getExtnSTSContainerCountList")){
				if(!_scBaseUtils.isVoid(modelOutput.AcadSTSTOContainersList.AcadSTSTOContainers)){
					var	sScannedCount = modelOutput.AcadSTSTOContainersList.AcadSTSTOContainers.length;				
					_scWidgetUtils.setValue(this, "extn_countOfContainers", sScannedCount, null);
				}				
			}
	},
	handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
			
			var mashupRefId =  _scModelUtils.getStringValueFromPath("0.mashupRefId", inputData);
			if (hasError && _scBaseUtils.equals(mashupRefId, "createAndReceiveTOContainer")) {
					
					var eCode = _scModelUtils.getStringValueFromPath("response.Errors.Error.0.ErrorCode", data);
					_iasScreenUtils.showErrorMessageBoxWithOk(this, eCode);
					_scWidgetUtils.setValue(this, "extn_txt_ContainerNo", null, null);
			
			}
		},
		extn_OnCloseAction: function(event, bEvent, ctrl, args)
		{
		 var screenInput= _scBaseUtils.getNewModelInstance();
		 _scModelUtils.setStringValueAtModelPath("IsInvokedFromReceiveContainer","Y",screenInput);
		 var sLastScannedContainer = _scWidgetUtils.getValue(this,"extn_lastScannedContainer");
		 if(_scBaseUtils.isVoid(sLastScannedContainer))
		 {
			_iasBaseTemplateUtils.showMessage(
								this, "No Valid Container ID scanned. Proceed scanning a Container ID.", "error", null);
								return 0;	
			window.sessionStorage.removeItem("ContainersModel");
		 }
		_scControllerUtils.openScreenInEditor("extn.mobile.home.STSContainer.detailedReceivedReport.ReceivedDetailsReportScreen", screenInput, null, {}, {}, "extn.mobile.editors.ReceiveContainerEditor");
		},
		//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - START
		openSTSOrderSearchScreen: function(){	
    // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - START
			var clearSessionObject = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("SearchCriteria", clearSessionObject);	
    // OMNI - 8777 - Session object is retaining in the Ship to store order search screen in the scenario which is mentioned in the steps - END
			_wscMobileHomeUtils.openScreen("extn.mobile.home.STSOrderSearch.OrderSearch.STSOrderSearchScreen", "extn.mobile.editors.ReceiveContainerEditor");
		},
	//OMNI-6624 - Mobile Store UI - New Search screen customization for STS - END
		// opening the ShipmentSummary on click of last scanned container no
		openToSummaryScreen: function(event, bEvent, ctrl, args)
		{
		 	var sContainersModel = _scScreenUtils.getModel(this, "getShipmentContainerList_outptut");
		 	var sOrderHeaderkey = "";
		 	var containersList= _scModelUtils.getStringValueFromPath("Containers.Container", sContainersModel);
		 	for(var i in containersList){
		 	 var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);
		 	 var containerNo= _scModelUtils.getStringValueFromPath("ContainerNo", containerModel);
		 	 var sLastScannedContainer = _scWidgetUtils.getValue(this,"extn_lastScannedContainer");
			 var sCount = _scWidgetUtils.getValue(this,"extn_countOfContainers");
		 	 if(_scBaseUtils.equals(containerNo,sLastScannedContainer))
		 	 {
		 	  sOrderHeaderkey = _scModelUtils.getStringValueFromPath("Shipment.OrderHeaderKey", containerModel);
        // OMNI-10249  STS - Container ID Belongs To a Different Store  
			  break;
		 	 }
		 	}
			if(_scBaseUtils.isVoid(sContainersModel))
			{
				var sessionModel = window.sessionStorage;
	       	    	 	var ContainerModel = JSON.parse(window.sessionStorage.getItem("LastScannedContainerModel"));
				if(!_scBaseUtils.isVoid(ContainerModel))
				{
				  sOrderHeaderkey=_scModelUtils.getStringValueFromPath("OrderHeaderKey", ContainerModel);
				  sLastScannedContainer=_scModelUtils.getStringValueFromPath("ContainerNo", ContainerModel);
				  sCount=_scModelUtils.getStringValueFromPath("ContainerCount", ContainerModel);

				}

			}
			else
			{
			  window.sessionStorage.removeItem("LastScannedContainerModel");
			}

		 	if(!_scBaseUtils.isVoid(sOrderHeaderkey))
		 	{
				var sessionObject = _scBaseUtils.getNewModelInstance();
		 	     	_scModelUtils.setStringValueAtModelPath("ContainerNo",sLastScannedContainer,sessionObject);
		 	     	_scModelUtils.setStringValueAtModelPath("ContainerCount",sCount,sessionObject);
		 	     	_scModelUtils.setStringValueAtModelPath("OrderHeaderKey",sOrderHeaderkey,sessionObject);
					
					//fetch the UI source batchno model and set the batchno
					var sBatchModel = _scScreenUtils.getModel(this, "BatchNoModel");
					var sBatchNo = _scModelUtils.getStringValueFromPath("BatchNoModel.BatchNo", sBatchModel);
					
					_scModelUtils.setStringValueAtModelPath("BatchNo",sBatchNo,sessionObject);
					window.sessionStorage.setItem("LastScannedContainerModel",JSON.stringify(sessionObject));
					
					var screenInput= _scBaseUtils.getNewModelInstance();
	            	_scModelUtils.setStringValueAtModelPath("Shipment.OrderHeaderKey",sOrderHeaderkey,screenInput);
	            	_scModelUtils.setStringValueAtModelPath("InvokedFrom","ReceiveContainer",screenInput);
	            	_scControllerUtils.openScreenInEditor("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary", screenInput, null, {}, {}, "extn.mobile.editors.ReceiveContainerEditor");

		 	}
		},

		initializeScreen:function(event, bEvent, ctrl, args)
		{
		  var sessionModel = window.sessionStorage;
	       	  var ContainerModel = JSON.parse(window.sessionStorage.getItem("LastScannedContainerModel"));
		  if(!_scBaseUtils.isVoid(ContainerModel))
		  {
	            var sCount = _scModelUtils.getStringValueFromPath("ContainerCount", ContainerModel);
		    var sContianerNo= _scModelUtils.getStringValueFromPath("ContainerNo", ContainerModel);
				var sBatchNo= _scModelUtils.getStringValueFromPath("BatchNo", ContainerModel);
		    _scWidgetUtils.setValue(this, "extn_countOfContainers",sCount , null);
		    _scWidgetUtils.setValue(this, "extn_lastScannedContainer",sContianerNo, null);
			
			var sBatchNoModel = _scBaseUtils.getNewModelInstance(); 
			_scModelUtils.setStringValueAtModelPath("BatchNoModel.BatchNo", sBatchNo,sBatchNoModel);
			_scScreenUtils.setModel(this,"BatchNoModel",sBatchNoModel,null);

		  }

		}
		

	});
});
 
