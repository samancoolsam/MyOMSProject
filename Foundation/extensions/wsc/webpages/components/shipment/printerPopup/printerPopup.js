scDefine([
	"dojo/text!./templates/printerPopup.html",
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
	"scbase/loader!ias/utils/ContextUtils"
], function (
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
	_iasContextUtils
) {
	return _dojodeclare("extn.components.shipment.printerPopup.printerPopup", [_scScreen], {
		templateString: templateText,
		uId: "printerPopup",
		packageName: "extn.components.shipment.printerPopup",
		className: "printerPopup",
		title: "Title_printerPopup",
        screen_description: "Please choose a print pick labels to proceed",
		
		namespaces: {
             targetBindingNamespaces: [
					{
						value: 'TotalNoPickPrintLabels',
						description: "This namespace is used to take print pick label option selected."
					}
				]
			},
			
		 subscribers: {
            local: [
            {
                eventId: 'Popup_btnOK_onClick',
                sequence: '25',
                description: 'OK Button Action',
                listeningControlUId: 'Popup_btnOK',
                handler: {
                    methodName: "confirmPrinterSelection",
                    description: ""
                }
            }

            ]
		 },
		 confirmPrinterSelection: function(event, bEvent, ctrl, args){
         	var pickPrintLabelModel=null;
         	pickPrintLabelModel = _scScreenUtils.getTargetModel(this, "TotalNoPickPrintLabels",null);
			var selectedLabel = _scModelUtils.getStringValueFromPath("Option", pickPrintLabelModel);
         	this.isApplyClicked = true;
           _scWidgetUtils.closePopup(this, "OK", false);
		_iasContextUtils.addToContext("NumberOfLabels", selectedLabel);

         },
		 onPopupClose: function(
        event, bEvent, ctrl, args) {
            this.isApplyClicked = false;
            _scWidgetUtils.closePopup(
            this, "CLOSE", false);
        }
});
});
