scDefine([
    "dojo/text!./templates/ScanSummaryPopUp.html",
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
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.ScanSummaryPopUp.ScanSummaryPopUp", [_scScreen], {
        templateString: templateText,
        uId: "ScanSummaryPopUp",
        packageName: "extn.mobile.home.AuditStagedShipment.PopUp.ScanSummaryPopUp",
        className: "ScanSummaryPopUp",
        title: "Scan Completed",
        screen_description: "Scan Completed",
        namespaces: {
			sourceBindingNamespaces: [{
                value: 'getScanSummaryData_output',
                description: "This namespace is used for binding the scan summary data"
            }]
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
                    eventId: 'Popup_btnYes_onClick',
                    sequence: '30',
                    description: 'This method is used to move to the home screen',
                    handler: {
                        methodName: "confirmPopUpCloseOnYesSelection"
                    }
                },
				{
                    eventId: 'Popup_btnNo_onClick',
                    sequence: '30',
                    description: 'This method is used to continue scanning',
                    handler: {
                        methodName: "confirmPopUpCloseOnNoSelection"
                    }
                }
            ]
        },

       initializeScreen: function(event, bEvent, ctrl, args) {
			var AcadScanBatchHeaderKey= _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
			var currentNode = _iasContextUtils.getFromContext("CurrentStore");
			var inputModel = _scModelUtils.createNewModelObjectWithRootKey("ACADScanBatchHeader");
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", AcadScanBatchHeaderKey, inputModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode, inputModel);
			_iasUIUtils.callApi(this, inputModel, "getScanSummaryDataOnInit", null);

        },

        confirmPopUpCloseOnYesSelection: function(event, bEvent, ctrl, args) {  		
            //OMNI-69712(UI:Finish Pop Click Yes Action) - START		
			var AcadScanBatchHeaderKey= _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
            var currentNode = _iasContextUtils.getFromContext("CurrentStore");
            var Loginid = _iasContextUtils.getFromContext("Loginid");
			/*OMNI-71628 Count Changes - START */
			var missedShipmentCount= _iasContextUtils.getFromContext("missedShipment");
			var cancelledAndNotScannedCount=  _iasContextUtils.getFromContext("cancelledAndNotScanned");
			//var abandonedAndNotScannedCount=  _iasContextUtils.getFromContext("abandonedAndNotScanned");	
			/*OMNI-71628 Count Changes - END */
			var inputModel = _scModelUtils.createNewModelObjectWithRootKey("ACADScanBatchHeader");
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", AcadScanBatchHeaderKey, inputModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.BatchScanStatus", "COMPLETED", inputModel);
            _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "COMPLETED", inputModel);
            _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,inputModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", Loginid ,inputModel);
			/*OMNI-71628 Count Changes - START */
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.MissedScanTotal", missedShipmentCount , inputModel);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.CancelledNotScannedTotal", cancelledAndNotScannedCount, inputModel);
			//_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AbandonedNotScannedTotal", abandonedAndNotScannedCount, inputModel);
			/*OMNI-71628 Count Changes - END */
			/* OMNI-82354 UI Missed Shipments Reconciliation records on Click of YES button present in info popup - START */
			var missedShipmentsToggleFlag = _iasContextUtils.getFromContext("MissedScanFeatureToggle");
			if (_scBaseUtils.equals(missedShipmentsToggleFlag,'Y')){
				 var missedShipmentModel = _scModelUtils.createNewModelObjectWithRootKey("ACADScanBatchHeader");
                 _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", AcadScanBatchHeaderKey, missedShipmentModel);
                 _scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,missedShipmentModel);
                _iasUIUtils.callApi(this, missedShipmentModel, "insertMissedShipmentsInDBOnFinish", null);
			}
			/* OMNI-82354 UI Missed Shipments Reconciliation records on Click of YES button present in info popup - END */
			_iasUIUtils.callApi(this, inputModel, "updateBatchStatusAsCompletedOnFinish", null);
	        var clearSessionObject = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("AcadScanBatchHeaderKey", clearSessionObject);
			_iasContextUtils.addToContext("In_Progress_Session", clearSessionObject);
			_iasContextUtils.addToContext("BatchScanDuration", clearSessionObject);
			/*OMNI-71628 Count Changes - START */
			_iasContextUtils.addToContext("missedShipment", clearSessionObject);
			_iasContextUtils.addToContext("cancelledAndNotScanned", clearSessionObject);
			//_iasContextUtils.addToContext("abandonedAndNotScanned", clearSessionObject);
			/*OMNI-71628 Count Changes - END */
			 this.isApplyClicked = true;
            _scWidgetUtils.closePopup(this, "OK", false);
			_wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome", "wsc.mobile.editors.MobileEditor");
			//OMNI-69712(UI:Finish Pop Click Yes Action) - END
        },
		
		
		confirmPopUpCloseOnNoSelection: function(event, bEvent, ctrl, args) {
            this.isApplyClicked = true;
            _scWidgetUtils.closePopup(this, "OK", false);
        },
		
		 handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
        _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
    },
    
		//OMNI-70025( UI: Finish Scanned activity - Missed & Cancelled & Abandoned NOT scanned counts) -  START
		hideWidgets : function(){
			_scWidgetUtils.hideWidget(this, "extn_missedShipmentCount_parent");
			_scWidgetUtils.hideWidget(this, "extn_missedShipmentCount_colon");
			_scWidgetUtils.hideWidget(this, "extn_missedShipmentCount");
			_scWidgetUtils.hideWidget(this, "extn_cancelledAndNotScannedCount_parent");
			_scWidgetUtils.hideWidget(this, "extn_cancelledAndNotScannedCount_colon");
			_scWidgetUtils.hideWidget(this, "extn_cancelledAndNotScannedCount");
			//_scWidgetUtils.hideWidget(this, "extn_abandonedAndNotScannedCount_parent");
			//_scWidgetUtils.hideWidget(this, "extn_abandonedAndNotScannedCount_colon");
			//_scWidgetUtils.hideWidget(this, "extn_abandonedAndNotScannedCount");
		},
		showWidgets : function(){
			_scWidgetUtils.showWidget(this, "extn_missedShipmentCount_parent");
			_scWidgetUtils.showWidget(this, "extn_missedShipmentCount_colon");
			_scWidgetUtils.showWidget(this, "extn_missedShipmentCount");
			_scWidgetUtils.showWidget(this, "extn_cancelledAndNotScannedCount_parent");
			_scWidgetUtils.showWidget(this, "extn_cancelledAndNotScannedCount_colon");
			_scWidgetUtils.showWidget(this, "extn_cancelledAndNotScannedCount");
			//_scWidgetUtils.showWidget(this, "extn_abandonedAndNotScannedCount_parent");
			//_scWidgetUtils.showWidget(this, "extn_abandonedAndNotScannedCount_colon");
			//_scWidgetUtils.showWidget(this, "extn_abandonedAndNotScannedCount");
		},
		//OMNI-70025( UI: Finish Scanned activity - Missed & Cancelled & Abandoned NOT scanned counts) - END

		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		if ( _scBaseUtils.equals(mashupRefId, "getScanSummaryDataOnInit")) {
            //OMNI-72100 - Start
			var ElapsedTime = _scModelUtils.getStringValueFromPath("ACADScanBatchDetails.BatchScanDuration", modelOutput);
			_scModelUtils.setStringValueAtModelPath("ACADScanBatchDetails.BatchScanDuration",ElapsedTime+" hrs.", modelOutput);
			//OMNI-72100 - End
			_scScreenUtils.setModel(this, "getScanSummaryData_output", modelOutput, null);
			//OMNI-70025( UI: Finish Scanned activity - Missed & Cancelled & Abandoned NOT scanned counts) - START
				var showScanningActivityCount = modelOutput.ACADScanBatchDetails.ShowScanningActivityCount;					
				/*OMNI-71628 Count Changes - START */
				var missedShipmentCount = modelOutput.ACADScanBatchDetails.MissedShipments;
				var cancelledAndNotScannedCount = modelOutput.ACADScanBatchDetails.CancelledAndNotScanned;
				//var abandonedAndNotScannedCount= modelOutput.ACADScanBatchDetails.AbandonedAndNotScanned;
				if(!_scBaseUtils.isVoid(missedShipmentCount) && !_scBaseUtils.isVoid(cancelledAndNotScannedCount) ){
				//&& !_scBaseUtils.isVoid(abandonedAndNotScannedCount)
					_iasContextUtils.addToContext("missedShipment", missedShipmentCount);
					_iasContextUtils.addToContext("cancelledAndNotScanned", cancelledAndNotScannedCount);
					//_iasContextUtils.addToContext("abandonedAndNotScanned", abandonedAndNotScannedCount);
				}
				/*OMNI-71628 Count Changes - END */
				if(!_scBaseUtils.isVoid(showScanningActivityCount) 
				   && _scBaseUtils.equals(showScanningActivityCount, 'Y')){
					this.showWidgets();
				}else{
					this.hideWidgets();
				}
			//OMNI-70025( UI: Finish Scanned activity - Missed & Cancelled & Abandoned NOT scanned counts)
		}
		
		}
    });
});
