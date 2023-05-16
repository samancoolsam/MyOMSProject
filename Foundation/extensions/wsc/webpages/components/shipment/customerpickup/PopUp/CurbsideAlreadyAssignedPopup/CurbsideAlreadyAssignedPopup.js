scDefine([
    "dojo/text!./templates/CurbsideAlreadyAssignedPopup.html",
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
//OMNI-79565 - CurbsideDelayConfirmPopUpPopUp - START
) {
    return _dojodeclare("extn.components.shipment.customerpickup.PopUp.CurbsideAlreadyAssignedPopup.CurbsideAlreadyAssignedPopup", [_scScreen], {
        templateString: templateText,
        uId: "CurbsideAlreadyAssignedPopup",
        packageName: "extn.components.shipment.customerpickup.PopUp.CurbsideAlreadyAssignedPopup",
        className: "CurbsideAlreadyAssignedPopup",
        title: "Curbside Delay Confirm PopUp Screen",
        screen_description: "Curbside Delay Confirm PopUp Screen",
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
                    eventId: 'extn_Yes_btn_onClick',
                    sequence: '30',
                    description: 'This method is used to Confirm the Curbside Delay',
                    handler: {
                        methodName: "onClickOfYes"
                    }
                },
				{
                    eventId: 'extn_No_btn_onClick',
                    sequence: '30',
                    description: 'This method is used to close the popup',
                    handler: {
                        methodName: "onClickOfNo"
                    }
                }
            ]
        },

        initializeScreen: function(event, bEvent, ctrl, args) {
			var delayInMins= _scModelUtils.getStringValueFromPath("screen.binding.UserName", args);
			_scWidgetUtils.setValue(this, "extn_UserName", delayInMins, false);
            
        },
		onClickOfYes: function(event, bEvent, ctrl, args) {
			 _iasContextUtils.addToContext("ConfirmAssignment", 'Y' );
            _scWidgetUtils.closePopup(this, "OK", false);
        
	 },

    
        onClickOfNo: function(event, bEvent, ctrl, args) {
            
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