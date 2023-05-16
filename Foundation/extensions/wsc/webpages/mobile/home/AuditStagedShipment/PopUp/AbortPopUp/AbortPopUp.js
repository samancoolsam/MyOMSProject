scDefine([
    "dojo/text!./templates/AbortPopUp.html",
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
//OMNI-68379 - AbortPopUp - START
) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.AbortPopUp.AbortPopUp", [_scScreen], {
        templateString: templateText,
        uId: "AbortPopUp",
        packageName: "extn.mobile.home.AuditStagedShipment.PopUp.AbortPopUp",
        className: "AbortPopUp",
        title: "Abort PopUp Screen",
        screen_description: "Abort PopUp Screen",
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
                    eventId: 'Popup_btnAbort_onClick',
                    sequence: '30',
                    description: 'This method is used to abort the batch and start new batch',
                    handler: {
                        methodName: "onClickOfAbort"
                    }
                },
				{
                    eventId: 'Popup_btnNo_onClick',
                    sequence: '30',
                    description: 'This method is used to close the popup and redirect to home page',
                    handler: {
                        methodName: "onClickOfNo"
                    }
                }
            ]
        },

        initializeScreen: function(event, bEvent, ctrl, args) {
            
        },
		onClickOfAbort: function(event, bEvent, ctrl, args) {
		var clearSessionObject = _scBaseUtils.getNewModelInstance();
		var targetModel = _scBaseUtils.getTargetModel(this, "Start_Output");
 		var currentNode = _iasContextUtils.getFromContext("CurrentStore");
		var loginid = _iasContextUtils.getFromContext("Loginid");
		var batchHeaderKey = _iasContextUtils.getFromContext("AcadScanBatchHeaderKey");
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.Action", "RESET" ,targetModel);
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.StoreNo", currentNode  ,targetModel);
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.UserID", loginid ,targetModel);
		_scModelUtils.setStringValueAtModelPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", batchHeaderKey ,targetModel);
        _iasContextUtils.addToContext("AcadScanBatchHeaderKey ", clearSessionObject);
       _iasUIUtils.callApi(this, targetModel, "startBatchScanProcess", null);
        },
		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
        if(_scBaseUtils.equals(mashupRefId,"startBatchScanProcess")){
		var sAcadScanBatchHeaderKey = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.AcadScanBatchHeaderKey", modelOutput);
		var sStagedShipmentsCount = _scModelUtils.getStringValueFromPath("ACADScanBatchHeader.StagedShipmentsCount", modelOutput);
		_iasContextUtils.addToContext("AcadScanBatchHeaderKey", sAcadScanBatchHeaderKey);
		_iasContextUtils.addToContext("StagedShipmentsCount", sStagedShipmentsCount);
	    _wscMobileHomeUtils.openScreen("extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabel", "extn.mobile.editors.ReceiveContainerEditor");
		_scWidgetUtils.closePopup(this, "NO", false);
        }
	 },

    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
         _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
	 },

        onClickOfNo: function(event, bEvent, ctrl, args) {
            this.isApplyClicked = true;
			_wscMobileHomeUtils.openScreen("wsc.mobile.home.MobileHome","wsc.mobile.editors.MobileEditor");
		
            _scWidgetUtils.closePopup(this, "NO", false);
        
        },
        onPopupClose: function(
            event, bEvent, ctrl, args) {
            this.isApplyClicked = false;
            _scWidgetUtils.closePopup(
                this, "CLOSE", false);
        }
    });
});