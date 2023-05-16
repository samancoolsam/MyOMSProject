scDefine([
	"dojo/text!./templates/CustomCancelOrderPopUp.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
    "scbase/loader!sc/plat/dojo/utils/BaseUtils"
], function(
	templateText, 
	_dojodeclare, 
	_scScreen,
	_scWidgetUtils,
	_scScreenUtils,
    _scBaseUtils

) {
return _dojodeclare("extn.mobile.home.subscreens.CustomCancelOrderPopUp", [_scScreen], {
	templateString: templateText,
	uId: "CustomCancelOrderPopUp",
	packageName: "extn.mobile.home.subscreens",
	className: "CustomCancelOrderPopUp",



	namespaces: {


             targetBindingNamespaces: [


            {
                value: 'selectedReasonCode',
                description: "This namespace is used to take cancellation reason selected."
            }

            ],

             sourceBindingNamespaces: [

            {
                value: 'getReasonCodeList_output',
                description: "This namespace contains the reason code list"
            },

            ]


        },


      subscribers: {
            local: [

            {
                eventId: 'afterScreenInit',
                sequence: '30',
                description: 'This method is used to perform screen initialization tasks.',
                handler: {
                    methodName: "initializeScreen"
                }
            },


            {
                eventId: 'Popup_btnApply_onClick',
                sequence: '30',
                description: 'This method is used to cancel the order',
                handler: {
                    methodName: "confirmCancellation"
                }
            },

            {
                eventId: 'Popup_btnCancel_onClick',
                sequence: '25',
                description: 'Cancel/Close Button Action',
                listeningControlUId: 'Popup_btnCancel',
                handler: {
                    methodName: "onPopupClose",
                    description: ""
                }
            },


            ]
        },


         initializeScreen: function(event, bEvent, ctrl, args) {
         	

         },

         confirmCancellation: function(event, bEvent, ctrl, args){
         	var selectedReasonCodeModel=null;
         	selectedReasonCodeModel = _scScreenUtils.getTargetModel(this, "selectedReasonCode",null);

         	 this.isApplyClicked = true;
            _scWidgetUtils.closePopup(
            this, "APPLY", false);

         },

        onPopupClose: function(
        event, bEvent, ctrl, args) {
            this.isApplyClicked = false;
            _scWidgetUtils.closePopup(
            this, "CLOSE", false);
        },

        getPopupOutput: function(
        event, bEvent, ctrl, args) {
            var getSelectedReason = null;
            getSelectedReason = _scBaseUtils.getTargetModel(this, "selectedReasonCode", null);
            return getSelectedReason;
        },

        

});
});
