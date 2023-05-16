scDefine([
	"dojo/text!./templates/ExtnCancelShipmentOtherStores.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ModelUtils",
	"scbase/loader!ias/utils/UIUtils",
	"scbase/loader!sc/plat/dojo/utils/EventUtils",
	"scbase/loader!ias/utils/ScreenUtils",
	"scbase/loader!ias/utils/BaseTemplateUtils"
], function(
	templateText, _dojodeclare, _scScreen
	,
		_scBaseUtils
	,
		_scScreenUtils
	,
		_scWidgetUtils
	,
		_scModelUtils
	,
		_iasUIUtils
	,
		_scEventUtils
	,
		_iasScreenUtils
	,
		_iasBaseTemplateUtils
	
) {
return _dojodeclare("extn.components.shipment.summary.ExtnCancelShipmentOtherStores", [_scScreen], {
	templateString: templateText,
	uId: "ExtnCancelShipmentOtherStores",
	packageName: "extn.components.shipment.summary",
	className: "ExtnCancelShipmentOtherStores",
	shortageReasonPath: 'ShipmentLine.ShortageReasonCode',
	cancelReasonPath: 'ShipmentLine.CancellationReasonCode',
	namespaces: {
		targetBindingNamespaces: [{
			value: 'getShortageReasonOutput',
			description: "Target Model for Shortage Reason of the screen"
		}, {
			value: 'getCancellationReasonCodeOutput',
			description: "Target Model for cancellation reason code"
		}],
		sourceBindingNamespaces: [{
			value: 'extn_getShortageReasonCode_output',
			description: "The options binding namespace data of Shortage Reason."
		}, {
			value: 'CancellationReasonCodeData',
			description: "The source binding namespace  data of Cancellation Reason Code."
		}, {
			value: 'ShipmentLine',
			description: "This namespace holds Shipment Line details."
		}, {
			value: 'popupOutput',
			description: "Stores popup output set from setPopupOutput"
		}]
	},
	hotKeys: [{
            id: "extn_Popup_btnCancel",
            key: "ESCAPE",
            description: "$(_scSimpleBundle:Close)",
            widgetId: "extn_Popup_btnCancel",
            invocationContext: "",
            category: "$(_scSimpleBundle:General)",
            helpContextId: ""
        }],
	subscribers: {
            local: [{
                eventId: 'afterScreenInit',
                sequence: '30',
                description: 'This event is used to perform screen initialization tasks.',
                handler: {
                    methodName: "initializeScreen"
                }
            }, {
                eventId: 'extnRadSelectShortRea_onChange',
                sequence: '30',
                description: 'This event is used to hide or show Cancellation Reason Code dropdown',
                listeningControlUId: 'extnRadSelectShortRea',
                handler: {
                    methodName: "shortageReasonOnChange",
                    description: ""
                }
            }, {
                eventId: 'extn_Popup_btnNext_onClick',
                sequence: '25',
                description: 'Next / Confirm Button Action',
                listeningControlUId: 'extn_Popup_btnNext',
                handler: {
                    methodName: "onPopupConfirm",
                    description: ""
                }
            }, {
                eventId: 'extn_Popup_btnCancel_onClick',
                sequence: '25',
                description: 'Cancel/Close Button Action',
                listeningControlUId: 'extn_Popup_btnCancel',
                handler: {
                    methodName: "onPopupClose",
                    description: ""
                }
            }]
        },
		
		/* This method is called when any new mashup is called and after the mashup completes it function */
		/* If this method doesn't have any errors then handleMashupOutput method will be called */
		handleMashupCompletion: function(
			 mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
			 _iasBaseTemplateUtils.handleMashupCompletion(
			   mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
         },
		
        handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "extn_getCancellationReasonList")) {
                this.showCancellationWidget(
                modelOutput);
            }
            if(_scBaseUtils.equals(
            mashupRefId, "extn_getShortageReasonCode_new")) {
                _scScreenUtils.setModel(
                    this, "extn_getShortageReasonCode_output", modelOutput, null);
            var shortageCodeModel = null;
            var numOfShortageCode = null;
            var zero = 0;
            shortageCodeModel = _scScreenUtils.getModel(
            this, "extn_getShortageReasonCode_output");
            numOfShortageCode = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode", shortageCodeModel);
            numOfShortageCode = numOfShortageCode.length;
            if (!(
            _iasUIUtils.isValueNumber(
            numOfShortageCode))) {
                numOfShortageCode = zero;
            }
            if (
            _scBaseUtils.equals(
            0, numOfShortageCode)) {
                _scWidgetUtils.hideWidget(
                this, "extnInnerContainer", false);
                _scWidgetUtils.hideWidget(
                this, "extn_Popup_btnNext", false);
                _scWidgetUtils.setLabel(
                this, "extn_Popup_btnCancel", _scScreenUtils.getString(
                this, "Ok"));
                _scWidgetUtils.showWidget(
                this, "extnLblNoShortageConfigured", false, null);
            } else {
                this.hideShortageRadio(
                numOfShortageCode);
                
                var eventArgs = null;
                var eventDefn = null;
                eventDefn = {};
                eventArgs = {};
                _scEventUtils.fireEventInsideScreen(
                this, "extnRadSelectShortRea_onChange", eventDefn, eventArgs);
            }
            }
        },
		
		showCancellationWidget: function(
        cancelReasonCodeModel) {
            _scScreenUtils.setModel(
            this, "CancellationReasonCodeData", cancelReasonCodeModel, null);
            _scWidgetUtils.showWidget(
            this, "extnCancellationReasonCode", false, null);
            _scWidgetUtils.setWidgetMandatory(
            this, "extnCancellationReasonCode");
        },
		
		initializeScreen: function(
        event, bEvent, ctrl, args) {
            var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
            _iasUIUtils.callApi(
            this, inputToMashup, "extn_getShortageReasonCode_new", null);
        },
		
        hideShortageRadio: function(
        numOfShortageCode) {
            if (
            _scBaseUtils.equals(
            1, numOfShortageCode)) {
                _scWidgetUtils.addClass(
                this, "extnRadSelectShortRea", "showRadioSetAsLabel");
            }
        },
		
		onPopupClose: function(
        event, bEvent, ctrl, args) {
            _scWidgetUtils.closePopup(
                this, "CLOSE", false);
            
        },
		
		onPopupConfirm: function(
        event, bEvent, ctrl, args) {
            var isValid = true;
            isValid = _scScreenUtils.validate(
            this);
            if (
            _scBaseUtils.equals(
            false, isValid)) {
                var msg = null;
                msg = _scScreenUtils.getString(
                this, "screenHasErrors");
               /* _isccsBaseTemplateUtils.showMessage(
                this, msg, "error", null);*/
            } else {
                this.onApply(
                event, bEvent, ctrl, args);
            }
        },
		
		onApply: function(
        event, bEvent, ctrl, args) {
            if (
            _scBaseUtils.equals(
            this.flowName, "CancelShipmentOtherStores")) {
                var cancelReasonModel = null;
                var shortageReasonModel = null;
                shortageReasonModel = _scBaseUtils.getTargetModel(
                this, "getShortageReasonOutput", null);
                cancelReasonModel = _scBaseUtils.getTargetModel(
                this, "getCancellationReasonCodeOutput", null);
                if (
                _scBaseUtils.equals(
                _scModelUtils.getStringValueFromPath("ShortageReason", shortageReasonModel), "Cancel") && _scBaseUtils.isVoid(
                _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel))) {
                    _iasScreenUtils.showErrorMessageBoxWithOk(
                    this, "Message_CancellationReasonCode");
                } else {
                    _scWidgetUtils.closePopup(
                    this, "APPLY", false);
                }
            } else {
                _scWidgetUtils.closePopup(
                this, "APPLY", false);
            }
        },
		
		getPopupOutput: function(
        event, bEvent, ctrl, args) {
            var shortageReasonTargetModel = null;
            var shipmentLineModel = null;
            var shortageReasonCode = null;
            shortageReasonTargetModel = {};
            shipmentLineModel = _scScreenUtils.getModel(
            this, "ShipmentLine");
            var shortageReasonModel = null;
            shortageReasonModel = _scBaseUtils.getTargetModel(
            this, "getShortageReasonOutput", null);
            var shortedShipmentLineModel = null;
            shortedShipmentLineModel = {};
            shortedShipmentLineModel = _scModelUtils.createModelObjectFromKey(this.entity, shortedShipmentLineModel);
            _scModelUtils.setStringValueAtModelPath(
            this.shortageReasonPath, _scModelUtils.getStringValueFromPath("ShortageReason", shortageReasonModel), shortedShipmentLineModel);
            
            if (
            _scBaseUtils.equals(
            this.flowName, "CancelShipmentOtherStores")) {
                var cancelReasonModel = null;
                cancelReasonModel = _scBaseUtils.getTargetModel(
                this, "getCancellationReasonCodeOutput", null);
                _scModelUtils.setStringValueAtModelPath(
                this.cancelReasonPath, _scModelUtils.getStringValueFromPath("CancelReasonCode", cancelReasonModel), shortedShipmentLineModel);
            }
            return shortedShipmentLineModel;
        },
		
        shortageReasonOnChange: function(
        value, bEvent, ctrl, args) {
            var shortageModel = null;
            shortageModel = _scScreenUtils.getTargetModel(
            this, "getShortageReasonOutput", null);
            if (
            _scBaseUtils.equals(
            _scModelUtils.getStringValueFromPath("ShortageReason", shortageModel), "Cancel") && _scBaseUtils.equals(
            this.flowName, "CancelShipmentOtherStores")) {
                var cancelReasonCodeInput = null;
                cancelReasonCodeInput = {};
                _scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "YCD_CANCEL_REASON", cancelReasonCodeInput);
                var initialInputData = null;
                initialInputData = _scScreenUtils.getInitialInputData(
                this);
                _scModelUtils.setStringValueAtModelPath("CommonCode.CallingOrganizationCode", _scModelUtils.getStringValueFromPath("CommonCode.CallingOrganizationCode", initialInputData), cancelReasonCodeInput);
                _iasUIUtils.callApi(
                this, cancelReasonCodeInput, "extn_getCancellationReasonList", null);
            } else {
                _scWidgetUtils.hideWidget(
                this, "extnCancellationReasonCode", false);
                _scWidgetUtils.setWidgetNonMandatory(
                this, "extnCancellationReasonCode");
            }
        }

});
});
