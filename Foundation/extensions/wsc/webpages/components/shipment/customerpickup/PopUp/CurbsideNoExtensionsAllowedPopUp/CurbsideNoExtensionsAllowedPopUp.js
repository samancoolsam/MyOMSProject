scDefine([
    "dojo/text!./templates/CurbsideNoExtensionsAllowedPopUp.html",
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
//OMNI-84586 Curbside No Further extensions Allowed PopUp Screen UI-START
) {
    return _dojodeclare("extn.components.shipment.customerpickup.PopUp.CurbsideNoExtensionsAllowedPopUp.CurbsideNoExtensionsAllowedPopUp", [_scScreen], {
        templateString: templateText,
        uId: "CurbsideNoExtensionsAllowedPopUp",
        packageName: "extn.components.shipment.customerpickup.PopUp.CurbsideNoExtensionsAllowedPopUp",
        className: "CurbsideNoExtensionsAllowedPopUp",
        title: "Curbside No Further Extensions Allowed on Order PopUp Screen",
        screen_description: "Curbside No Further Extensions Allowed on Order PopUp Screen",
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
                    eventId: 'extn_OK_Button_onClick',
                    sequence: '30',
                    description: 'This method is used to close the popup',
                    handler: {
                        methodName: "onClickOfOK"
                    }
                }
            ]
        },

        initializeScreen: function(event, bEvent, ctrl, args) {

        },
        onClickOfOK: function(event, bEvent, ctrl, args) {
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