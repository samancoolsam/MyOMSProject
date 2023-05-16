scDefine([
    "dojo/text!./templates/CurbsideDelayConfirmPopUp.html",
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
    return _dojodeclare("extn.components.shipment.customerpickup.PopUp.CurbsideDelayConfirmPopUp.CurbsideDelayConfirmPopUp", [_scScreen], {
        templateString: templateText,
        uId: "CurbsideDelayConfirmPopUp",
        packageName: "extn.components.shipment.customerpickup.PopUp.CurbsideDelayConfirmPopUp",
        className: "CurbsideDelayConfirmPopUp",
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
			var delayInMins= _scModelUtils.getStringValueFromPath("screen.binding.CurbsideDelay", args);
          var maxDelayCount = _scModelUtils.getStringValueFromPath("screen.binding.MaxDelayCounter", args); //OMNI-80059
          var curbsideDelayCount = _scModelUtils.getStringValueFromPath("screen.binding.DelayCount", args); //OMNI-80059
			_scWidgetUtils.setValue(this, "extn_CurbsideTimeDelay", delayInMins, false);
            
          //OMNI-80059 Changes start
          if (_scBaseUtils.equals(parseInt(curbsideDelayCount),(parseInt(maxDelayCount)-1))){
            //OMNI-85987 Verbiage Changes - Start
			    	_scWidgetUtils.showWidget(this,"extn_Note");
                    //**Start OMNI-105780 Updating the message
				    _scWidgetUtils.setValue(this, "extn_Note", "Additional Delays Cannot Be Added To This Order", false);
                      //**END OMNI-105780 Updating the message
				    //_scWidgetUtils.setValue(this, "extn_NoteMsg", "Its Final Time Extension Allowed On Order", false);
            //OMNI-85987 Verbiage Changes - End
			    }
          //OMNI-80059 changes end
        },
		onClickOfYes: function(event, bEvent, ctrl, args) {
			 _iasContextUtils.addToContext("ConfirmDelay", 'Y' );
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