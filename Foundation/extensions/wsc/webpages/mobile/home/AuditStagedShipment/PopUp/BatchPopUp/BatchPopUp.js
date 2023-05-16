scDefine([
    "dojo/text!./templates/BatchPopUp.html",
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
    "scbase/loader!dojo/_base/connect",
    "scbase/loader!dojo/dom-attr"
], function(
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
    dConnect,
    dDomAttr
) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.BatchPopUp.BatchPopUp", [_scScreen], {
        templateString: templateText,
        uId: "BatchPopUp",
        packageName: "extn.mobile.home.AuditStagedShipment.PopUp.BatchPopUp",
        className: "BatchPopUp",
        title: "Batch PopUp",
        screen_description: "Batch PopUp for Start New Batch or Continue Batch",
        namespaces: {
        },
        subscribers: {
            local: [{
                    eventId: 'afterScreenInit',
                    sequence: '30',
                    description: 'This method is used to perform screen initialization tasks.',
                    handler: {
                        methodName: "initializeScreen"
                    }
                },
				{
					eventId: 'abortButton_onClick',
					sequence: '30',
					description: 'This method will redirect to home page',
					handler: {
						methodName: "abortAndStart"
					}
				},
        /*OMNI-69646 Continue Changes - START*/
                {
                    eventId: 'continueButton_onClick',
                    sequence: '30',
                    description: 'This method will redirect to scan page',
                    handler: {
                        methodName: "onClick_continue"
                    }
      	        }
         /*OMNI-69646 Continue Changes - END*/    
            ]
        },

        initializeScreen: function(event, bEvent, ctrl, args) {
        /*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - START*/   
	    	var clearSessionObject = _scBaseUtils.getNewModelInstance();
	    	_iasContextUtils.addToContext("In_Progress_Session", clearSessionObject);
        /*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - END*/   
        },
		
		abortAndStart: function(){
			var targetModel = _scBaseUtils.getTargetModel(this, "Start_Output");
			var currentNode = _iasContextUtils.getFromContext("CurrentStore");
			var loginid = _iasContextUtils.getFromContext("Loginid");
			var batchHeaderKey = _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "RESET" ,targetModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,targetModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", loginid ,targetModel);
			if(!_scBaseUtils.isVoid(batchHeaderKey)){
				_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", batchHeaderKey ,targetModel);
			}
			_iasUIUtils.callApi(this, targetModel, "startBatchScanProcess", null);	
		},
    /*OMNI-69646 Continue Changes - START*/   
     onClick_continue: function() {
      var targetModel = _scBaseUtils.getTargetModel(this, "Start_Output");
      var currentNode = _iasContextUtils.getFromContext("CurrentStore");
      var batchHeaderKey = _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
      var loginid = _iasContextUtils.getFromContext("Loginid");
      _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "CONTINUE", targetModel);
      _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode, targetModel);
      _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", loginid, targetModel);
      _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", batchHeaderKey, targetModel);
      _iasUIUtils.callApi(this, targetModel, "continueScanProcess", null);
      },
    /*OMNI-69646 Continue Changes - END*/   
		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
			if(_scBaseUtils.equals(mashupRefId,"startBatchScanProcess")){
				var sAcadScanBatchHeaderKey = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", modelOutput);
				_iasContextUtils.addToContext("AcadScanBatchHeaderKey", sAcadScanBatchHeaderKey );
                var sStagedShipmentsCount = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.StagedShipmentsCount", modelOutput);
                _iasContextUtils.addToContext("StagedShipmentsCount", sStagedShipmentsCount);
            }
      /*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - START*/ 
            if (_scBaseUtils.equals(mashupRefId, "continueScanProcess")) {
                var sAcadScanBatchHeaderKey = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", modelOutput);
                var sBatchScanDuration = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.BatchScanDuration", modelOutput);
                var sShipmentsCancelled = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ShipmentsCancelled", modelOutput);
                var sShipmentsScanned = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.ShipmentsScanned", modelOutput);
                _iasContextUtils.addToContext("AcadScanBatchHeaderKey", sAcadScanBatchHeaderKey);
                _iasContextUtils.addToContext("BatchScanDuration", sBatchScanDuration);
                _iasContextUtils.addToContext("ShipmentsCancelled", sShipmentsCancelled);
                _iasContextUtils.addToContext("ShipmentsScanned", sShipmentsScanned);
                var sStagedShipmentsCount = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.StagedShipmentsCount", modelOutput);
                _iasContextUtils.addToContext("StagedShipmentsCount", sStagedShipmentsCount);
		    	_iasContextUtils.addToContext("In_Progress_Session", "Y");
            }
				this.confirmPopUpCloseSelection();
				_wscMobileHomeUtils.openScreen("extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabel", "extn.mobile.editors.ReceiveContainerEditor");
			/*OMNI-69643 & OMNI-69646 Shipment Count & Continue Changes - END*/ 
		},

		handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
			_iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
		},
		
        confirmPopUpCloseSelection: function(event, bEvent, ctrl, args) {
            this.isApplyClicked = true;
            _scWidgetUtils.closePopup(this, "OK", false);
        }
    });
});
