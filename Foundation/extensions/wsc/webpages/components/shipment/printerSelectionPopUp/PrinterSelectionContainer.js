scDefine([
	"dojo/text!./templates/PrinterSelectionContainer.html",
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
	_iasContextUtils,
	dConnect,
    dDomAttr

) {
	return _dojodeclare("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", [_scScreen], {
		templateString: templateText,
		uId: "PrinterSelectionContainer",
		packageName: "extn.components.shipment.printerSelectionPopUp",
		className: "PrinterSelectionContainer",
		title: "Please choose a printer",
        screen_description: "Please choose a printer",
		 
	namespaces: {
             targetBindingNamespaces: [
            {
                value: 'selectedPrinterDevice',
                description: "This namespace is used to take printer option selected."
            }
            ],
             sourceBindingNamespaces: [
            {
                value: 'getPrinterDevice_output',
                description: "This namespace contains the Printer device list"
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
                eventId: 'Popup_btnOK_onClick',
                sequence: '30',
                description: 'This method is used to cancel the order',
                handler: {
                    methodName: "confirmPrinterSelection"
                }
            }


            ]
        },
		
        
        initializeScreen: function(event, bEvent, ctrl, args) {
			var selectedPrinterModel = null;
			selectedPrinterModel = _scScreenUtils.getTargetModel(this, "selectedPrinterModel",null);
			var storePrinterID=_iasContextUtils.getFromContext("PrinterID");
			var model1 = {};
			if(!_scBaseUtils.isVoid(storePrinterID)) {
				_scModelUtils.setStringValueAtModelPath("Devices.Device.DeviceId",storePrinterID,model1);
				_scScreenUtils.setModel(this, "extn_printerFromSession", model1);
			}
			else {
				_scScreenUtils.setModel(this, "getPrinterDevice_output", model1);
			}
			
			var fs = this.getWidgetByUId("PrinterSelectionContainerUID");
            this._addReadOnlyState();
            dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
            dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			
			_iasUIUtils.callApi(this, selectedPrinterModel, "getPrinterDeviceInit", null);
		
    },
    
	_removeReadOnlyState: function() {
            var fs = this.getWidgetByUId("PrinterSelectionContainerUID");
            dDomAttr.remove(fs.textbox, "readonly");
    },
    _addReadOnlyState: function() {
            var fs = this.getWidgetByUId("PrinterSelectionContainerUID");
            dDomAttr.set(fs.textbox, "readonly", true);
    },
	
	confirmPrinterSelection: function(event, bEvent, ctrl, args){
       	var selectedPrinterDeviceModel=null;
       	selectedPrinterDeviceModel = _scScreenUtils.getTargetModel(this, "selectedPrinterDevice",null);
	
		if(_scBaseUtils.isVoid(selectedPrinterDeviceModel)) { 
			_iasScreenUtils.showErrorMessageBoxWithOk(this, "Printer is not selected. Please select a Printer to proceed.");
		}
		else {
			var sPrinterId = _scModelUtils.getStringValueFromPath("Devices.Device.DeviceId", selectedPrinterDeviceModel);
              
			var PrinterModel = JSON.parse(window.sessionStorage.getItem("PrinterModel"));
		    if(!_scBaseUtils.isVoid(PrinterModel)){
				var containersList= _scModelUtils.getStringValueFromPath("Devices.Device", PrinterModel);
				for(var i in containersList){
					var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);  
					var containerNo= _scModelUtils.getStringValueFromPath("DeviceId", containerModel);	
					if(containerNo == sPrinterId) {
						var sPrinterIPAddress = _scModelUtils.getStringValueFromPath("DeviceIP", containerModel);
						   _iasContextUtils.addToContext("IPAddress", sPrinterIPAddress);
	    				   _iasContextUtils.addToContext("PrinterID", sPrinterId);    				
					}
				}
			}
			this.isApplyClicked = true;
			_scWidgetUtils.closePopup(this, "OK", false);
		}

    },
    onPopupClose: function(
        event, bEvent, ctrl, args) {
            this.isApplyClicked = false;
            _scWidgetUtils.closePopup(
            this, "CLOSE", false);
        },
		
		getPopupOutput: function(
        event, bEvent, ctrl, args) {
            var getPrinterCode = null;
            getPrinterCode = _scBaseUtils.getTargetModel(this, "selectedPrinterDevice", null);
            return getPrinterCode;
        },	

    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
        _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
    },
	
    handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		if ( _scBaseUtils.equals(mashupRefId, "getPrinterDeviceInit")) {
			_scScreenUtils.setModel(this, "getPrinterDevice_output", modelOutput, null);
			var containersList= _scModelUtils.getStringValueFromPath("Devices.Device", modelOutput);
		 	for(var i in containersList){
				var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);  
				var containerNo= _scModelUtils.getStringValueFromPath("DeviceParamsXML.Attributes.Attribute", containerModel);
				for(var j in containerNo){
					var abcModel= _scModelUtils.getStringValueFromPath("", containerNo[j]);
					var abc = _scModelUtils.getStringValueFromPath("DisplayName", abcModel);
					if(abc=="IP_ADDRESS"){
						var abcef = _scModelUtils.getStringValueFromPath("Value", abcModel);
						_scModelUtils.setStringValueAtModelPath("DeviceIP",abcef, containersList[i]);
	
					}
				}
		
			}

			window.sessionStorage.setItem("PrinterModel",JSON.stringify(modelOutput));
			
	

		}
	}
});
});
