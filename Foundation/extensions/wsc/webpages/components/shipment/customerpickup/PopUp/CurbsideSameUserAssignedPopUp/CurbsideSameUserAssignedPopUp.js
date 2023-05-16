scDefine([
    "dojo/text!./templates/CurbsideSameUserAssignedPopUp.html",
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
//OMNI-80183-CurbsideSameUserAssignedPopUp - START
) {
    return _dojodeclare("extn.components.shipment.customerpickup.PopUp.CurbsideSameUserAssignedPopUp.CurbsideSameUserAssignedPopUp", [_scScreen], {
        templateString: templateText,
        uId: "CurbsideSameUserAssignedPopUp",
        packageName: "extn.components.shipment.customerpickup.PopUp.CurbsideSameUserAssignedPopUp",
        className: "CurbsideSameUserAssignedPopUp",
        title: "Curbside Same User Already Assigned PopUp Screen",
        screen_description: "Curbside Same User Already Assigned PopUp Screen",
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
			var curbsideAttendedBy= _scModelUtils.getStringValueFromPath("screen.binding.CurbsideAttendedBy", args);
			_scWidgetUtils.setValue(this, "extn_AlreadyAssignedUserName", curbsideAttendedBy, false);            
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