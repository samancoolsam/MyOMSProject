scDefine([
    "dojo/text!./templates/scanSerialNumberPopUp.html",
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
    "scbase/loader!dojo/dom-attr",
    "scbase/loader!sc/plat/dojo/utils/EventUtils"
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
    dDomAttr,
    _scEventUtils

) {
    return _dojodeclare("extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberPopUp", [_scScreen], {
        templateString: templateText,
        uId: "scanSerialNumberPopUp",
        packageName: "extn.components.shipment.scanSerialNumberPopUp",
        className: "scanSerialNumberPopUp",
        title: "Scan Serial Number extnSerialNoScan",
        screen_description: "Scan Serial Number",
        namespaces: {
			targetBindingNamespaces :
		[
		{
		 scExtensibilityArrayItemId: '"extn_TargetNamespaces_8',
	    value: 'extn_SerialNo_input'
		}
		],
			sourceBindingNamespaces: [
			{
				description: "This namespace contains scanned serial no model",
				scExtensibilityArrayItemId: 'extn_SourceNamespaces_9',
				value: 'extn_SerialNo_output'
			}
			]
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
                    eventId: 'extn_Popup_btnCancel_onClick',
                    sequence: '30',
                    description: 'This method is used to move to the home screen',
                    handler: {
                        methodName: "confirmPopUpCloseOnCloseSelection"
                    }
                }, {
                eventId: 'scanSerialNoButton_onClick',
                sequence: '30',
                description: 'Subscriber for Scan/Add button',
                listeningControlUId: 'scanSerialNoButton',
                handler: {
                    methodName: "scanSerialNo",
                    description: "Handled for Scan serial no"
                }
            },{
                eventId: 'scanProductIdTxt_onKeyUp',
                sequence: '30',
                description: 'This method is used to handle Key Up event of Scan serial no text field.',
				 listeningControlUId: 'scanProductIdTxt',
                handler: {
                    methodName: "scanSerialNoOnEnter"
                }
            }
				
            ]
        },

       initializeScreen: function(event, bEvent, ctrl, args) {
			

        },

        confirmPopUpCloseOnCloseSelection: function(event, bEvent, ctrl, args) {             
			 this.isCancelClicked = true;
            _scWidgetUtils.closePopup(this, "Cancel", false);
        },
		

		 handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
        _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
    },
		scanSerialNoOnEnter: function(
        event, bEvent, ctrl, args) {
            if (
            _iasEventUtils.isEnterPressed(
            event)) {
				_scWidgetUtils.hideWidget(this, "extn_InvalidSerialNolbl", false);
				this.scanSerialNo();
				}
        },
        
        scanSerialNo: function() {
            var enteredSno=_scWidgetUtils.getValue(this,"scanProductIdTxt"); 
            //OMNI-94874 start
             if (_scBaseUtils.isVoid(enteredSno) || _scBaseUtils.isVoid(enteredSno.trim())){
                  _scWidgetUtils.hideWidget(this, "extn_InvalidSerialNolbl", false);
                  _scWidgetUtils.setValue(this, "scanProductIdTxt", "");
                  _iasContextUtils.addToContext("SerialNoContext","");
                  return false;
          }
          //OMNI-94874 end


          _scWidgetUtils.hideWidget(this, "extn_InvalidSerialNolbl", false);
            var barCodeModel = null;
            var barCodeData = null;
			var serialNoOut='';
			var serialCodeModel = _scScreenUtils.getTargetModel(this, "extn_SerialNo_input", null);
            var serialNo = _scModelUtils.getStringValueFromPath("Shipment.SerialNo", serialCodeModel);
			var serNoWgt=_scWidgetUtils.getValue(this.ownerScreen, "extn_datalabelSNO");
			/* if (_scBaseUtils.isVoid(serNoWgt)) {
				var quantityTextBoxModel = _scScreenUtils.getTargetModel(
            this.ownerScreen, "ShipmentLineKey_Output", null);
			var shipmentLineKey=quantityTextBoxModel.ShipmentLine.ShipmentLineKey;
			var shipmentLineModel = _scScreenUtils.getModel(this.ownerScreen.ownerScreen, "backroomPickShipmentDetails_output");
			var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentLineModel);
				if (!_scBaseUtils.isVoid(shipmentLineList)) {
					for (var i=0;i<shipmentLineList.length;i++) {
                        var currentShipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", shipmentLineList[i]);
                        if (_scBaseUtils.equals(currentShipmentLineKey,shipmentLineKey)) {
                         var shipmentTagSerialList = _scModelUtils.getStringValueFromPath("ShipmentTagSerials.ShipmentTagSerial", shipmentLineList[i]);
						 var serialNoOut=[];
						 if (!_scBaseUtils.isVoid(shipmentTagSerialList)) {
                        for (var j=0;j<shipmentTagSerialList.length;j++) {
							 var serialNoValues=_scModelUtils.getStringValueFromPath("SerialNo", shipmentTagSerialList[j]);
							serialNoOut.push(serialNoValues);
							}
						}
						}
                        }
                    }
					
                } */
			
			if (!_scBaseUtils.isVoid(serNoWgt)) {
			 serialNoOut = serNoWgt.split(', ');
			}
			if(serialNoOut.includes(serialNo)) {
				_iasContextUtils.addToContext("SerialNoContext",serialNo);
			   _scWidgetUtils.closePopup(this, "OK", false);
		    }else{
				  _scWidgetUtils.showWidget(this, "extn_InvalidSerialNolbl", false);
				  _iasContextUtils.addToContext("SerialNoContext","");
                  _scWidgetUtils.setValue(this, "scanProductIdTxt", "");
			}
        },
		
		handleMashupOutput: function(mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
		
		}
    });
});
