scDefine([
    "dojo/text!./templates/CancelledPopUp.html",
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
//OMNI-67005- CancelledPopUp - START
) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.CancelledPopUp.CancelledPopUp", [_scScreen], {
        templateString: templateText,
        uId: "CancelledPopUp",
        packageName: "extn.mobile.home.AuditStagedShipment.PopUp.CancelledPopUp",
        className: "CancelledPopUp",
        title: "Shipment is cancelled",
        screen_description: "Shipment is cancelled",
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
                    eventId: 'Popup_btnOK_onClick',
                    sequence: '30',
                    description: 'This method is used to cancel the order',
                    handler: {
                        methodName: "confirmPopUpCloseSelection"
                    }
                }
            ]
        },

        initializeScreen: function(event, bEvent, ctrl, args) {
            
        },

        confirmPopUpCloseSelection: function(event, bEvent, ctrl, args) {
            this.isApplyClicked = true;
            _scWidgetUtils.closePopup(this, "OK", false);
        },
        onPopupClose: function(
            event, bEvent, ctrl, args) {
            this.isApplyClicked = false;
            _scWidgetUtils.closePopup(
                this, "CLOSE", false);
        }
    });
});