
scDefine(["dojo/text!./templates/UpdateHoldLocationExtn.html",
"scbase/loader!dojo/_base/declare",
"scbase/loader!extn/components/shipment/backroompick/UpdateHoldLocationExtnUI",
 "scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils",
 "scbase/loader!ias/utils/EventUtils", "scbase/loader!ias/utils/ScreenUtils", 
 "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils",
 "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils",
 "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils",
 "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/backroompick/UpdateHoldLocationUI",
 "scbase/loader!wsc/components/shipment/backroompick/utils/BackroomPickUpUtils", 
 "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils",
 "scbase/loader!sc/plat/dojo/utils/WidgetUtils", 
 "scbase/loader!dojo/_base/connect",
 "scbase/loader!dojo/dom-attr"]
,
function(			 
			    templateText,_dojodeclare
			 ,
			    _extnUpdateHoldLocationExtnUI
			  ,_iasBaseTemplateUtils, _iasContextUtils, _iasEventUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _wscCommonUtils, _wscUpdateHoldLocationUI, _wscBackroomPickUpUtils, _wscShipmentUtils, _scWidgetUtils,  dConnect, dDomAttr
){ 
	return _dojodeclare("extn.components.shipment.backroompick.UpdateHoldLocationExtn", [_extnUpdateHoldLocationExtnUI],{
		    handleMashupCompletion: function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
        _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
    },
	
    handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
			if ( _scBaseUtils.equals(mashupRefId, "getPrinterDeviceInit1")) {		
				_scScreenUtils.setModel(this, "extn_getPrinterDevice_output1", modelOutput, null);
				var sPrinterCount = modelOutput.Devices;
				if(!_scBaseUtils.isVoid(sPrinterCount)){
					_iasContextUtils.addToContext("IsHipPrinterEnabled", "Y");
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
				else {
										_iasContextUtils.addToContext("IsHipPrinterEnabled", "N");
		    		_iasContextUtils.addToContext("IsHipPrinterEnabled", "N");
				}				
				
		  }
  }
,
	// custom code here

	extn_getRepetingScreen: function() {
		var returnValue = null;
		            returnValue = _scBaseUtils.getNewBeanInstance();
		            _scBaseUtils.addStringValueToBean("repeatingscreenID", "extn.customScreen.stagingParent.StagingParent", returnValue);
		            var constructorData = null;
		            constructorData = _scBaseUtils.getNewBeanInstance();
		            _scBaseUtils.addBeanValueToBean("constructorArguments", constructorData, returnValue);
		            return returnValue;
	},

	extn_afterScreenInit: function() {
            //fix for BOPIS-1543: Short picked lines are coming in the page where staging location is entered : Begin
            var shipmentModel = _scScreenUtils.getModel(
            this, "ShipmentModel");
	        var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",shipmentModel);
		    for(var i=0;i<shipmentLineList.length;i++) {
              	var shortageQty = Number(_scModelUtils.getStringValueFromPath("ShortageQty", shipmentLineList[i]));
              	var originalQuantity = Number(_scModelUtils.getStringValueFromPath("OriginalQuantity", shipmentLineList[i]));
              	if(originalQuantity - shortageQty == 0) {
                    shipmentLineList.splice(i, 1); 
              	}
		    }
		    _scScreenUtils.setModel(this, "extn_ShipmentModel", shipmentModel);
  			var isHipPrinterEnabledFlag=_iasContextUtils.getFromContext("IsHipPrinterEnabled");
  	        if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag)){
  				if(isHipPrinterEnabledFlag=="N"){					
  				     _scWidgetUtils.hideWidget(this, "extn_filteringselect", false);
  				}
  			}		
			
		    //BOPIS-1543: End
            // var dMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentModel);

            // if(_scBaseUtils.equals("PICK", dMethod)) {
            // 	_iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", _scScreenUtils.getInitialInputData(this), "wsc.desktop.editors.ShipmentEditor", this, null);
            // }
			var fs = this.getWidgetByUId("extn_filteringselect");
            this._addReadOnlyState();
            dConnect.connect(fs, "openDropDown", this, "_addReadOnlyState");
            dConnect.connect(fs, "closeDropDown", this, "_addReadOnlyState");
			

      var bindings = null;
      bindings = {};
      var screenConstructorParams = null;
      screenConstructorParams = {};
      var popupParams = null;
      popupParams = {};
      popupParams["binding"] = bindings;
      popupParams["screenConstructorParams"] = screenConstructorParams;
      var dialogParams = null;
      dialogParams = {};
      dialogParams["closeCallBackHandler"] = "printerIDFromSession";
      dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			var printerModel=_iasContextUtils.getFromContext("PrinterID");
			var isHipPrinterEnabledFlag=_iasContextUtils.getFromContext("IsHipPrinterEnabled");
			if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag)){
				if(isHipPrinterEnabledFlag=="Y"){
					if(_scBaseUtils.isVoid(printerModel)) {
						_iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "User does not have a printer assigned. Please<br/>select a valid printer from the dropdown.", this, popupParams, dialogParams);
					}
					else{
						var sPrinterID=_iasContextUtils.getFromContext("PrinterID");
						if(_scBaseUtils.isVoid(sPrinterID)){
							_iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "User does not have a printer assigned. Please<br/>select a valid printer from the dropdown.", this, popupParams, dialogParams);
						}
						
					}
			}
			}
	},
	
	_removeReadOnlyState: function() {
            var fs = this.getWidgetByUId("extn_filteringselect");
            dDomAttr.remove(fs.textbox, "readonly");
    },
    _addReadOnlyState: function() {
            var fs = this.getWidgetByUId("extn_filteringselect");
            dDomAttr.set(fs.textbox, "readonly", true);
    },
	// code changes for printOrderTicket design
	extn_beforeSave: function(event, bEvent, ctrl, args) {
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentModel");
		var shipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",shipmentModel);
		var isExtnStagingLocationNotAssigned = false;
		var isBackroomPickNotCompleted = false;
		for(var i=0;i<shipmentLineList.length;i++) {
			var extnStagingLocationAssigned = _scModelUtils.getStringValueFromPath("ExtnStagingLocation", shipmentLineList[i]);
			var backroomPickedQuantity = _scModelUtils.getStringValueFromPath("BackroomPickedQuantity", shipmentLineList[i]);
			if(!_scBaseUtils.isVoid(backroomPickedQuantity) && !_scBaseUtils.equals(backroomPickedQuantity, "0.00")) {
				if(_scBaseUtils.isVoid(extnStagingLocationAssigned)){
					_iasBaseTemplateUtils.showMessage(this, "Staging location is not assigned. Please assign staging location to proceed.", "error", null);
	                isExtnStagingLocationNotAssigned = true;
	                _scEventUtils.stopEvent(bEvent);
	                break;
				}
			}
			var backroomPickComplete = _scModelUtils.getStringValueFromPath("BackroomPickComplete", shipmentLineList[i]);
			if (_scBaseUtils.isVoid(backroomPickComplete) || !_scBaseUtils.equals(backroomPickComplete, "Y")) {
				isBackroomPickNotCompleted = true;
			}
		}
		if (!isExtnStagingLocationNotAssigned && !isBackroomPickNotCompleted) {
			var isHipPrinterEnabledFlag=_iasContextUtils.getFromContext("IsHipPrinterEnabled");
			if(isHipPrinterEnabledFlag=="N")
			{			
				var shipmentModel = _scScreenUtils.getModel(this, "ShipmentModel");
				var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
				var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				//OMNI-95876 - Start
				var vExtnIsAssemblyRequired = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Extn.ExtnIsAssemblyRequired", shipmentModel);
				if (!_scBaseUtils.isVoid(vExtnIsAssemblyRequired) && _scBaseUtils.equals(vExtnIsAssemblyRequired, "Y")){
					_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.10.5", inputModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "EXTN_STORE_BACKROOM_PICK.0001.ex", inputModel);
				}else{
					_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30", inputModel);
					_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK", inputModel);
				}
				//OMNI-95876 - End
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
			// invoking changesShipmentStatus on click of finish pick insteadof on click of Next button in backroompickproductscan screen 
				_iasUIUtils.callApi(this, inputModel, "saveShipmentStatusForPickUpOrder", null);  
			}
			else{
					var bindings = null;
					bindings = {};
					var screenConstructorParams = null;
					screenConstructorParams = {};
					//bindings["ShipmentLine"] = shipmentLineModel;
					var popupParams = null;
					popupParams = {};
					popupParams["binding"] = bindings;
					popupParams["screenConstructorParams"] = screenConstructorParams;
					var dialogParams = null;
					dialogParams = {};
					dialogParams["closeCallBackHandler"] = "extnPrintPickLabelSelection";
					dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
					_iasUIUtils.openSimplePopup("extn.components.shipment.printerPopup.printerPopup", "", this, popupParams, dialogParams);
				}
			_scEventUtils.stopEvent(bEvent);
		}
	},
	extnPrintPickLabelSelection : function(actionPerformed,model, popupParams)
	{
		var selectLabel = null;
		selectedTargetModel = _scBaseUtils.getTargetModel(this, "TotalNoPickPrintLabels", null);
		selectLabel = _scModelUtils.getStringValueFromPath("Option", selectedTargetModel);
		var shipmentModel = _scScreenUtils.getModel(this, "ShipmentModel");
		var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
		var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
		//OMNI-95876 - Start
		var vExtnIsAssemblyRequired = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Extn.ExtnIsAssemblyRequired", shipmentModel);
		if (!_scBaseUtils.isVoid(vExtnIsAssemblyRequired) && _scBaseUtils.equals(vExtnIsAssemblyRequired, "Y")){
			_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.10.5", inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "EXTN_STORE_BACKROOM_PICK.0001.ex", inputModel);
		}else{
			_scModelUtils.setStringValueAtModelPath("Shipment.BaseDropStatus", "1100.70.06.30", inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.TransactionId", "YCD_BACKROOM_PICK", inputModel);
		}
		//OMNI-95876 - End
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
		 
		 //invoking changesShipmentStatus on click of finish pick insteadof on click of Next button in backroompickproductscan screen 
		_iasUIUtils.callApi(this, inputModel, "saveShipmentStatusForPickUpOrder", null);		
	},
	storePrinterInSession:function(){
		var printerModel = null;
        printerModel = _scBaseUtils.getTargetModel(this, "extn_getChangedPrinterID", null);
        if (!_scBaseUtils.isVoid(printerModel)) {
            selectedPrinterDevice = _scModelUtils.getStringValueFromPath("Devices.Device.DeviceId", printerModel);
    		if(!_scBaseUtils.isVoid(selectedPrinterDevice)){
				var PrinterModel = JSON.parse(window.sessionStorage.getItem("PrinterModel"));
				if(!_scBaseUtils.isVoid(PrinterModel)){
				var containersList= _scModelUtils.getStringValueFromPath("Devices.Device", PrinterModel);
					for(var i in containersList){
						var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);  
						var containerNo= _scModelUtils.getStringValueFromPath("DeviceId", containerModel);	
						if(containerNo == selectedPrinterDevice) {
							var sPrinterIPAddress = _scModelUtils.getStringValueFromPath("DeviceIP", containerModel);
							_iasContextUtils.addToContext("IPAddress", sPrinterIPAddress);
							_iasContextUtils.addToContext("PrinterID", selectedPrinterDevice);
						}
					}
				}
			}
        }
		else{
			var emptyModel = _scBaseUtils.getNewModelInstance();
			_iasContextUtils.addToContext("PrinterID", emptyModel);
		}
	},
	extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
		var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
    	if (!_scBaseUtils.isVoid(mashupRefList)) {
        	for (var i = 0; i < mashupRefList.length; i++) {
                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if (_scBaseUtils.equals(mashupRefid, "saveShipmentStatusForPickUpOrder")) {
                	var modelOutput = mashupRefList[i].mashupRefOutput;
                	var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", modelOutput);
					var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
					
					var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", modelOutput);
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, inputModel);
					
					var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", modelOutput);
					_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, inputModel);

					var sPrinterId =_iasContextUtils.getFromContext("PrinterID");
					if(!_scBaseUtils.isVoid(sPrinterId)) {
						_scModelUtils.setStringValueAtModelPath("Shipment.PrinterID", sPrinterId, inputModel);
					}
				    var sPrinterIP =_iasContextUtils.getFromContext("IPAddress");
					if(!_scBaseUtils.isVoid(sPrinterIP)) {
						_scModelUtils.setStringValueAtModelPath("Shipment.PrinterIP", sPrinterIP, inputModel);
					}
					var sPrinterLabel =_iasContextUtils.getFromContext("NumberOfLabels");
					if(!_scBaseUtils.isVoid(sPrinterLabel))	{
							_scModelUtils.setStringValueAtModelPath("Shipment.NumberOfLabels", sPrinterLabel, inputModel);
					}
					var isHipPrinterEnabledFlag =_iasContextUtils.getFromContext("IsHipPrinterEnabled");
					if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag)) {
						_scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", isHipPrinterEnabledFlag, inputModel);
					}
	
					_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "N", inputModel);
					_iasUIUtils.callApi(this, inputModel, "extn_PrintOrderTicket_ref", null);
                	this.save(event, bEvent, ctrl, args);			
                	var deleteLabel= _scBaseUtils.getNewModelInstance();
				        	_iasContextUtils.addToContext("NumberOfLabels", deleteLabel);     
			    }
                // OMNI-3676 BOPIS: Apply All Staging Locations - START 
       else if (_scBaseUtils.equals(mashupRefid, "extn_AssignStagingLocationMashup"))  {
			this.extn_afterScreenInit();
			var mashuprefOutput = _scModelUtils.getStringValueFromPath("mashupRefOutput", mashupRefList[i]);
			var shipmentLinesModel = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",mashuprefOutput);
      var ShipmentModel=_scScreenUtils.getModel(this, "ShipmentModel");
			var ShipmentLinesList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",ShipmentModel);
      for(var i in shipmentLinesModel) {
        var vShipmenLineKey= _scModelUtils.getStringValueFromPath("ShipmentLineKey",shipmentLinesModel[i]);
  			for(var j in ShipmentLinesList) {
  			 	var vShipmentLineKey1=  _scModelUtils.getStringValueFromPath("ShipmentLineKey",ShipmentLinesList[j]);
  			 	if(_scBaseUtils.equals(vShipmenLineKey,vShipmentLineKey1))
  			 	{
  			 		var location = _scModelUtils.getStringValueFromPath("Extn.ExtnStagingLocation",shipmentLinesModel[i]);
  			 		if(!_scBaseUtils.isVoid(location))
  			 		{
  			 			_scModelUtils.setStringValueAtModelPath("ExtnStagingLocation", location, ShipmentLinesList[j]);
  			 		}
  			 	}
  			 }
        }
		_scWidgetUtils.setValue(this, "extn_StagingLoc", "", null);
		}
            }
        }
	},

  extn_AssignStagingLocationtoAll:function(event, bEvent, ctrl, args) {
	 
    var model =    _scBaseUtils.getTargetModel(this,"extn_StagingLocationAll");
    var stagingLocation = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", model);
    if(!_scBaseUtils.isVoid(stagingLocation))
    {
    	 var shipmentModel  = _scScreenUtils.getModel(this,"extn_ShipmentModel");
    	 var changeShipmentInput = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
    	 _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentModel), changeShipmentInput);
         _scModelUtils.setStringValueAtModelPath("Shipment.Action", "MODIFY", changeShipmentInput);
         var shipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",shipmentModel);

         var changeShipmentLinesInput = _scBaseUtils.getNewArrayInstance();
         for(var index in shipmentLines)
         {
              var changeShipmentLineInput = _scBaseUtils.getNewModelInstance();
              var shipmentLineKey= _scModelUtils.getStringValueFromPath("ShipmentLineKey",shipmentLines[index]);
              var sBackroomPickedQuantity= _scModelUtils.getNumberValueFromPath("BackroomPickedQuantity",shipmentLines[index]);
              if(sBackroomPickedQuantity>0)
              {
              _scModelUtils.setStringValueAtModelPath("ShipmentLineKey",shipmentLineKey, changeShipmentLineInput);
              _scModelUtils.setStringValueAtModelPath("Extn.ExtnStagingLocation",stagingLocation, changeShipmentLineInput);
              _scModelUtils.addModelObjectToModelList(changeShipmentLineInput,changeShipmentLinesInput);
              }
         }
         _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine",changeShipmentLinesInput,changeShipmentInput);
	_iasUIUtils.callApi(this, changeShipmentInput, "extn_AssignStagingLocationMashup", null);

    }
  
  },

	extn_AssignStagingLocationtoAllOnEnter:function(event, bEvent, ctrl, args){
	 if (
				_iasEventUtils.isEnterPressed(
				event)) {
					this.extn_AssignStagingLocationtoAll(event, bEvent, ctrl, args);
			}

	},
	extn_setFocusOnStagingLocation:function(event, bEvent, ctrl, args){
	 _scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_StagingLoc");

	},
	// OMNI-3676 BOPIS: Apply All Staging Locations END
	printerIDFromSession:function(event, bEvent, ctrl, args)
	{
		var sessionModel = window.sessionStorage;
		var storePrinterId =_iasContextUtils.getFromContext("PrinterID");
		var model1 = {};
		if(!_scBaseUtils.isVoid(storePrinterId)) {
			var selectedPrinterDeviceModel=null;
			_scModelUtils.setStringValueAtModelPath("Devices.Device.DeviceId",storePrinterId,model1);
			_scScreenUtils.setModel(this, "extn_printerFromSession", model1);

			var PrinterModel = JSON.parse(window.sessionStorage.getItem("PrinterModel"));
			  if(!_scBaseUtils.isVoid(PrinterModel)){
			    var containersList= _scModelUtils.getStringValueFromPath("Devices.Device", PrinterModel);
				for(var i in containersList){
					var containerModel = _scModelUtils.getStringValueFromPath("",containersList[i]);  
					var containerNo= _scModelUtils.getStringValueFromPath("DeviceId", containerModel);	
					if(containerNo == storePrinterId) {
						var sPrinterIPAddress = _scModelUtils.getStringValueFromPath("DeviceIP", containerModel);
						_iasContextUtils.addToContext("IPAddress", sPrinterIPAddress);
				    }
				}
			}
		}
	},
	//OMNI-3676 BOPIS: Apply All Staging Locations - END
  
	//OMNI-21999: Full name of customer on BOPIS staging screen - START
	extn_CustomerName: function(dataValue, screen, widget, nameSpace, shipmentModel) {
		var strCustomerName="";
		strCustomerName = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.CustomerFirstName", shipmentModel)+" "+_scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.Order.CustomerLastName", shipmentModel);	
		return strCustomerName;
	}
  //OMNI-21999: Full name of customer on BOPIS staging screen - END
	
});
});

