
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/backroompick/BackroomPickProductScanExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/EventUtils", "scbase/loader!ias/utils/RepeatingScreenUtils", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WizardUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/RepeatingPanelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!sc/plat/dojo/utils/WizardUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/backroompick/BackroomPickProductScanUI", "scbase/loader!wsc/components/shipment/backroompick/utils/BackroomPickUpUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnBackroomPickProductScanExtnUI
			,
				_iasBaseTemplateUtils, _iasContextUtils, _iasEventUtils, _iasRepeatingScreenUtils, _iasScreenUtils, _iasUIUtils, _iasWizardUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scRepeatingPanelUtils, _scScreenUtils, _scWidgetUtils, _scWizardUtils, _wscCommonUtils, _wscBackroomPickProductScanUI, _wscBackroomPickUpUtils, _wscShipmentUtils
){ 
	return _dojodeclare("extn.components.shipment.backroompick.BackroomPickProductScanExtn", [_extnBackroomPickProductScanExtnUI],{
	// custom code here
	/*This method is to send Extn attribute to changeShipment API based on certain conditions */
	updateShipmentLineDetails: function(
        event, bEvent, ctrl, args) {
            this.backroomPickSaved = false;
            var shipmentModel = null;
            shipmentModel = _scBaseUtils.getModelValueFromBean("inputData", args);
			var shipmentLineExtn = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", shipmentModel);
			var shortageReasonCode = _scModelUtils.getStringValueFromPath("Shipment.ShortageReasonCode", shipmentModel);

            // changes for passing planogram details to changeShipment -- Beegin

            var plaDetailsOutput = _scScreenUtils.getModel(this, "getShortageLines");

            var sLines = _scModelUtils.getStringValueFromPath("Page.Output.ShipmentLines", plaDetailsOutput);

            // changes for passing planogram details to changeShipment -- End
			for(var shipmentLine in shipmentLineExtn)
			{
				var shortageQty = _scModelUtils.getStringValueFromPath("ShortageQty", shipmentLineExtn[shipmentLine]);
				if(!_scBaseUtils.isVoid(shortageQty) && shortageQty > 0)
				{
					 var extnShipmentLine = _scModelUtils.createModelObjectFromKey("Extn", shipmentLineExtn[shipmentLine]);
					 _scModelUtils.setStringValueAtModelPath("ExtnMsgToSIM", "N", extnShipmentLine);
					 _scModelUtils.setStringValueAtModelPath("ExtnReasonCode", shortageReasonCode, extnShipmentLine);
				}

			}
            // for(var sLine in sLines)
            // {
            //     var storedInDB = _scModelUtils.getStringValueFromPath("IsStoredInDB", sLines[sLine]);

            //     if(_scBaseUtils.equals(storedInDB, "N")) {
            //         if(_scModelUtils.getStringValueFromPath("ShipmentLine.Extn", shipmentModel)) {
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnDepartment", _scModelUtils.getStringValueFromPath("Extn.ExtnDepartment", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnLiveDate", _scModelUtils.getStringValueFromPath("Extn.ExtnLiveDate", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPlanogramStatus", _scModelUtils.getStringValueFromPath("Extn.ExtnPlanogramStatus", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPogId", _scModelUtils.getStringValueFromPath("Extn.ExtnPogId", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPogNumber", _scModelUtils.getStringValueFromPath("Extn.ExtnPogNumber", sLines[sLine]), shipmentModel);
            //         } else {
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn", {}, shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnDepartment", _scModelUtils.getStringValueFromPath("Extn.ExtnDepartment", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnLiveDate", _scModelUtils.getStringValueFromPath("Extn.ExtnLiveDate", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPlanogramStatus", _scModelUtils.getStringValueFromPath("Extn.ExtnPlanogramStatus", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPogId", _scModelUtils.getStringValueFromPath("Extn.ExtnPogId", sLines[sLine]), shipmentModel);
            //             _scModelUtils.setStringValueAtModelPath("ShipmentLine.Extn.ExtnPogNumber", _scModelUtils.getStringValueFromPath("Extn.ExtnPogNumber", sLines[sLine]), shipmentModel);
            //         }
            //     }
            // }
            // OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - START 
			    var CompleteShipmentModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
            var sDeliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",CompleteShipmentModel);
            // OMNI-70080 - STS Resourcing
			var sDocumentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType",CompleteShipmentModel);
            var sNodeType = _scModelUtils.getStringValueFromPath("Shipment.ShipNode.NodeType",CompleteShipmentModel);     
            if(!_scBaseUtils.isVoid(sDeliveryMethod) && (_scBaseUtils.equals(sDeliveryMethod,"PICK") || 
            	(_scBaseUtils.equals(sDeliveryMethod,"SHP") && _scBaseUtils.equals(sDocumentType,"0006") && _scBaseUtils.equals(sNodeType,"Store"))) 
            	&& (!_scBaseUtils.isVoid(shortageReasonCode)))
            {
            	if (_scBaseUtils.equals(sDeliveryMethod,"SHP") && _scBaseUtils.equals(sDocumentType,"0006") && _scBaseUtils.equals(sNodeType,"Store")) {
            		_scModelUtils.setStringValueAtModelPath("Shipment.IsSTS2ShipmentShort","Y",shipmentModel);
            	}
            	_scModelUtils.setStringValueAtModelPath("Shipment.IsShortedfromWebStore","Y",shipmentModel);
            }
			
			// OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - END 
			//OMNI-92366 - Start
			var sBopisFireArm = _iasContextUtils.getFromContext("BopisFireArm");
			var sSerialNo = _iasContextUtils.getFromContext("SerialNoContext");
			if(!_scBaseUtils.isVoid(sBopisFireArm) && (_scBaseUtils.equals(sBopisFireArm,"Y")) && !_scBaseUtils.isVoid(sSerialNo)){
				var vShipmentLine = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.0.ShipmentLine", shipmentModel);
				var vShipmentTagSerials = _scModelUtils.createModelObjectFromKey("ShipmentTagSerials", vShipmentLine);
				var vShipmentTagSerial = _scModelUtils.createModelObjectFromKey("ShipmentTagSerial", vShipmentTagSerials);
				_scModelUtils.setStringValueAtModelPath("Quantity", "1.00", vShipmentTagSerial);
				_scModelUtils.setStringValueAtModelPath("Action", "Delete", vShipmentTagSerial);
				_scModelUtils.setStringValueAtModelPath("SerialNo", sSerialNo, vShipmentTagSerial);
				_scModelUtils.setStringValueAtModelPath("ShipmentLineKey", vShipmentLine.ShipmentLineKey,vShipmentTagSerial);
			}		
			//OMNI-92366 - End	
            _iasUIUtils.callApi(
            this, shipmentModel, "updateShipmentQuantityForPickAllLine", null);
        },
		checkIfHipPrinterAvailableFlag : function(event, bEvent, ctrl, args){
		var sTargetModel = _scScreenUtils.getTargetModel(this, "extn_getPrinterDeviceMashupRefId_output", null);
		_iasUIUtils.callApi(this, sTargetModel, "extn_getPrinterDeviceMashupRefId", null);	          
	},

    gotoNextScreen: function() {
		this.loadNextScreen();
		
	},
	loadNextScreen: function(){
			_scScreenUtils.clearScreen(
            this, "translateBarCode_input");

            inputShipmentModel = _scScreenUtils.getModel(
            this, "backroomPickShipmentDetails_output");

            var dMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", inputShipmentModel);

            if(_scBaseUtils.equals("PICK", dMethod)) {
		        	  var selectPrinterValue={};
		          	var parentScreen = null;
                parentScreen = _iasUIUtils.getParentScreen(
                this, true);
                if (
                _scWizardUtils.isCurrentPageLastEntity(
                parentScreen)) {
                    _iasWizardUtils.setActionPerformedOnWizard(
                    parentScreen, "CONFIRM");
                } else {
                    _iasWizardUtils.setActionPerformedOnWizard(
                    parentScreen, "NEXT");
                }
                _scEventUtils.fireEventToParent(
                this, "onSaveSuccess", null);
            } else {
                _iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", _scScreenUtils.getInitialInputData(this), "wsc.desktop.editors.ShipmentEditor", this, null);
            }
		},
		
		onpopupclose:function(event, bEvent, ctrl, args) {
			 _scScreenUtils.clearScreen(
            this, "translateBarCode_input");

            inputShipmentModel = _scScreenUtils.getModel(
            this, "backroomPickShipmentDetails_output");

            var dMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", inputShipmentModel);

            if(_scBaseUtils.equals("PICK", dMethod)) {
var selectPrinterValue={};
var parentScreen = null;
                parentScreen = _iasUIUtils.getParentScreen(
                this, true);
                if (
                _scWizardUtils.isCurrentPageLastEntity(
                parentScreen)) {
                    _iasWizardUtils.setActionPerformedOnWizard(
                    parentScreen, "CONFIRM");
                } else {
                    _iasWizardUtils.setActionPerformedOnWizard(
                    parentScreen, "NEXT");
                }
                _scEventUtils.fireEventToParent(
                this, "onSaveSuccess", null);
            } else {
                _iasUIUtils.openWizardInEditor("wsc.components.shipment.summary.ShipmentSummaryWizard", _scScreenUtils.getInitialInputData(this), "wsc.desktop.editors.ShipmentEditor", this, null);
            }
        },
		//OMNI-90829,OMNI-90674 START
		scanProductOnEnter: function(
        event, bEvent, ctrl, args) {
            if (
            _iasEventUtils.isEnterPressed(
            event)) {
				this.scanProduct();
				}
        },
		scanProductAndSerialNoOnEnter: function(
        event, bEvent, ctrl, args) {
			if (_iasEventUtils.isEnterPressed(event)) {
				this.scanProductAndSerialNo();
			 }
		},
		
		scanProductAndSerialNo: function(
        event, bEvent, ctrl, args) {
			barCodeModel = _scScreenUtils.getTargetModel(this, "translateBarCode_input", null);
			var serialCodeModel = _scScreenUtils.getTargetModel(this, "extn_SerialNo_input", null);
            var serialNo = _scModelUtils.getStringValueFromPath("Shipment.SerialNo", serialCodeModel);
           //OMNI 94874
			var spacePresent=false;
            for (var i = 0; i < serialNo.length; i++) {
                  var nstr=serialNo[i];
			   if (_scBaseUtils.contains(nstr, " ")){
                       spacePresent=true; 
			   }
			}
            //OMNI 94874
              _scModelUtils.setStringValueAtModelPath("BarCode.ShipmentContextualInfo.SerialNo", serialNo, barCodeModel);
			var sItemID = _iasContextUtils.getFromContext("ItemIDContext");
			_scModelUtils.setStringValueAtModelPath("BarCode.BarCodeData", sItemID, barCodeModel);
				if(_scBaseUtils.isVoid(sItemID)){
					_iasScreenUtils.showErrorMessageBoxWithOk(
                    this, "NoProductScanned");
                 //OMNI-93022 - Start
				_scWidgetUtils.enableWidget(this, "scanProductIdTxt", true);
				_scWidgetUtils.enableWidget(this, "addProductButton", true);				
				_scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
				_scWidgetUtils.disableWidget(this, "extn_serialNoBox", true);
				_scWidgetUtils.disableWidget(this, "extn_serialNoButton", true);
				//OMNI-93022 - End
                }
				    else if (_scBaseUtils.isVoid(serialNo) || _scBaseUtils.isVoid(serialNo.trim()) || (spacePresent)) {
                    _iasScreenUtils.showErrorMessageBoxWithOk(this, "Serial No not scanned");
                } else {
					_iasUIUtils.callApi(this, barCodeModel, "extn_translateBarcode_ref", null);
				}
				var eventDefn = null;
				var eventArgs = null;
				eventArgs = {};
				eventDefn = {};
				_scBaseUtils.setAttributeValue("argumentList", _scBaseUtils.getNewBeanInstance(), eventDefn);
				this.clearItemFilter();                
		},
		
		scanProduct: function() {
            var barCodeModel = null;
            var barCodeData = null;
			 var sBopisFireArm = _iasContextUtils.getFromContext("BopisFireArm");//OMNI-90829
            if (!(
            _scScreenUtils.isValid(
            this, "translateBarCode_input"))) {
                _iasScreenUtils.showErrorMessageBoxWithOk(
                this, "InvalidBarCodeData");
            }else if(!_scBaseUtils.equals(sBopisFireArm, "Y")) {//OMNI-90829
                barCodeModel = _scScreenUtils.getTargetModel(
                this, "translateBarCode_input", null);
                barCodeData = _scModelUtils.getStringValueFromPath("BarCode.BarCodeData", barCodeModel);
                if (
                _scBaseUtils.isVoid(
                barCodeData)) {
                    _iasScreenUtils.showErrorMessageBoxWithOk(
                    this, "NoProductScanned");
                } else {
					_iasUIUtils.callApi(
                    this, barCodeModel, "translateBarCode", null);
					var eventDefn = null;
                    var eventArgs = null;
                    eventArgs = {};
                    eventDefn = {};
                    _scBaseUtils.setAttributeValue("argumentList", _scBaseUtils.getNewBeanInstance(), eventDefn);
                    this.clearItemFilter();
                }
            }
			else if (!_scBaseUtils.isVoid(sBopisFireArm) && _scBaseUtils.equals(sBopisFireArm, "Y") ) {
				 barCodeModel = _scScreenUtils.getTargetModel(
                this, "translateBarCode_input", null);
				 barCodeData = _scModelUtils.getStringValueFromPath("BarCode.BarCodeData", barCodeModel);
				_iasContextUtils.addToContext("ItemIDContext",barCodeData);
				_scWidgetUtils.enableWidget(this, "extn_serialNoBox", true);
				_scWidgetUtils.enableWidget(this, "extn_serialNoButton", true);
				_scWidgetUtils.setFocusOnWidgetUsingUid(this, "extn_serialNoBox");
				_scWidgetUtils.disableWidget(this, "scanProductIdTxt", true);
				_scWidgetUtils.disableWidget(this, "addProductButton", true);
			}
			
        },
		//OMNI-90829,OMNI-90674 END
		customError: function(
        screen, data, code) {
            var errorMessageBundle = null;
            if (
            _scBaseUtils.equals(
            code, "YCD00063")) {
                errorMessageBundle = "InvalidBarCodeData";
            } else if (
            _scBaseUtils.equals(
            code, "YCP0187")) {
                errorMessageBundle = "BarCodeDataRequired";
            } else if (
            _scBaseUtils.equals(
            code, "YCD00064")) {
                errorMessageBundle = "MultipleItemsFound";
            } else if (
            _scBaseUtils.equals(
            code, "YCD00065")) {
                errorMessageBundle = "ItemNotFound";
            } else if (
            _scBaseUtils.equals(
            code, "YCD00066")) {
                errorMessageBundle = "ItemNotInShipment";
            } else if (
            _scBaseUtils.equals(
            code, "YCD00067")) {
                errorMessageBundle = "ItemOverrage_BackroomPick";
            } else {
                return false;
            }
            if (!(
            _scBaseUtils.isVoid(
            errorMessageBundle))) {
				//OMNI-93022 - Start
				var sBopisFireArm = _iasContextUtils.getFromContext("BopisFireArm");
				if (!_scBaseUtils.isVoid(sBopisFireArm) && _scBaseUtils.equals(sBopisFireArm, "Y") ) {	
					_scWidgetUtils.enableWidget(this, "scanProductIdTxt", true);
					_scWidgetUtils.enableWidget(this, "addProductButton", true);				
					_scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
					_scWidgetUtils.disableWidget(this, "extn_serialNoBox", true);
					_scWidgetUtils.disableWidget(this, "extn_serialNoButton", true);			
				}
				//OMNI-93022 - End
                _iasScreenUtils.showErrorMessageBoxWithOk(
                this, errorMessageBundle);
                return true;
            }
        },
    // code changes for SLA indicator : Begin
    extn_afterOOTBafterScreenLoad: function(event, bEvent, ctrl, args) {
        var shipmentModel = null;
        var deliveryMethod = null;
        shipmentModel = _scScreenUtils.getModel(this, "backroomPickShipmentDetails_output");
        deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentModel);
		//OMNI-90829 START
        var vPackListType = _scModelUtils.getStringValueFromPath("Shipment.PackListType", shipmentModel);
        var vShipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentModel);
       if(!_scBaseUtils.isVoid(vPackListType) && _scBaseUtils.equals(vPackListType, "FA") && _scBaseUtils.equals(deliveryMethod, "PICK") && !_scBaseUtils.equals(vShipmentType, "STS")){
			_iasContextUtils.addToContext("BopisFireArm", "Y");
			_scWidgetUtils.disableWidget(this, "extn_serialNoBox", true);
			_scWidgetUtils.disableWidget(this, "extn_serialNoButton", true);
		}else{
			_iasContextUtils.addToContext("BopisFireArm", "");
			_scWidgetUtils.hideWidget(this, "extn_serialNoBox", true);
			_scWidgetUtils.hideWidget(this, "extn_serialNoButton", true);
		}
        //OMNI-90829 END
        if (_scBaseUtils.equals(deliveryMethod, "SHP")) {
            _scWidgetUtils.hideWidget(this, "img_TimeRmnClock", true);
            _scWidgetUtils.hideWidget(this, "dueInTimeTextLabel", true);
            // Hide sla indicator for sfs orders in mobile
            if (_iasContextUtils.isMobileContainer()) {
                var parentScreen = _iasUIUtils.getParentScreen(this, true);
                _scWidgetUtils.hideWidget(parentScreen, "img_TimeRmnClock", false);
                _scWidgetUtils.hideWidget(this, "extn_timeRemaining", false);
            }
        }
    },
    extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
        if (_iasContextUtils.isMobileContainer()) {
            _scWidgetUtils.showWidget(this, "extn_timeRemaining", false);
        }   
    },
    // code changes for SLA indicator : End
	
	/* This OOB method has been overridden in order to set focus on Product ID scannable textbox irrespective of mobile or desktop version */
	afterScreenLoad: function(
        event, bEvent, ctrl, args) 
	{
		/* if (!(
		_iasContextUtils.isMobileContainer())) {
			_scWidgetUtils.setFocusOnWidgetUsingUid(
		   this, "scanProductIdTxt");
		} */
		_scWidgetUtils.setFocusOnWidgetUsingUid(
		   this, "scanProductIdTxt");
		var wizardInstance = false;
		wizardInstance = _iasUIUtils.getParentScreen(
		this, true);
		_iasWizardUtils.hideNavigationalWidget(
		wizardInstance, "prevBttn", false);
	},
    //BOPIS-1496: Can't Edit/Short Pick the Picked Quantity : Begin
    showHideShipmentLineList: function(
        shipmentLineList) {
            if (
            _scBaseUtils.equals("0", _scModelUtils.getStringValueFromPath("ShipmentLines.TotalNumberOfRecords", shipmentLineList))) {
                // _scWidgetUtils.showWidget(
                // this, "allLinesPickedLabel", false, null);
                // _scWidgetUtils.hideWidget(
                // this, "tpProductLines", false);
                // _scWidgetUtils.hideWidget(
                // this, "lastProductScannedDetailsScreenRef", false);
                // _scWidgetUtils.hideWidget(
                // this, "pickAll", false);
                // _scWidgetUtils.hideWidget(
                // this, "productScanForm", false);
                // _scWidgetUtils.hideWidget(
                // this, "viewShortItemsLink", false);
                // if (
                // _iasContextUtils.isMobileContainer()) {
                //     _scRepeatingPanelUtils.hideMobilePaginationPreviousButton(
                //     this, this.activeRepeatingPanelUId, false);
                //     _scRepeatingPanelUtils.hideMobilePaginationNextButton(
                //     this, this.activeRepeatingPanelUId, false);
                // } else {
                //     _scRepeatingPanelUtils.hideDesktopPaginationBar(
                //     this, this.activeRepeatingPanelUId, false);
                // }
            } else {
                this.nextView = "shortItems";
                var eDef = null;
                eDef = {};
                var eArgs = null;
                eArgs = {};
                _scEventUtils.fireEventInsideScreen(
                this, "reloadSelectView", eDef, eArgs);
            }
        },
        //BOPIS-1496: Can't Edit/Short Pick the Picked Quantity : End

// OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations - START 

refreshShipmentLineAfterQuantityUpdate: function(
        modelOutput) {
            var eventArgs = null;
            var eventDefn = null;
            eventDefn = {};
            eventArgs = {};
            if (
            _scBaseUtils.equals("MarkAllLinesShortage", _scModelUtils.getStringValueFromPath("Shipment.Action", modelOutput))) {
                _scScreenUtils.showConfirmMessageBox(
                this, _scScreenUtils.getString(
                this, "Message_PickAllLinesMessage"), "handlePickAllConfirmation", null, null);
            } else if (
			
            _scBaseUtils.equals("ShowCancelPopup", _scModelUtils.getStringValueFromPath("Shipment.Action", modelOutput))) {
//invoke email service

		this.sendCancellEmail(modelOutput); 
                _scScreenUtils.showWarningMessageBoxWithOk(
                this, _scScreenUtils.getString(
                this, "Message_OrderWillBeCancelled"), "handleCancelOrderConfirmation", _iasUIUtils.getTextOkObjectForMessageBox(
                this), null, null);
            } else {
                var pickedQtyModel = null;
                pickedQtyModel = {};
                _scModelUtils.setStringValueAtModelPath("Quantity", _scModelUtils.getStringValueFromPath("ShipmentLine.BackroomPickedQuantity", modelOutput), pickedQtyModel);
                var repPanelUId = null;
                repPanelUId = _scRepeatingPanelUtils.returnUIdOfIndividualRepeatingPanel(
                this, this.activeRepeatingPanelUId, _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", modelOutput));
                var shipmentLineModel = null;
                var repPanelScreen = null;
                repPanelScreen = _iasScreenUtils.getRepeatingPanelScreenWidget(
                this, repPanelUId);
                if (
                _scBaseUtils.isVoid(
                repPanelScreen)) {} else {
                    shipmentLineModel = _scScreenUtils.getModel(
                    repPanelScreen, "ShipmentLine");
                    var shortageQty = 0;
                    shortageQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.ShortageQty", modelOutput);
                    if (
                    shortageQty >= 1) {
                        _scModelUtils.setStringValueAtModelPath("ShipmentLine.Quantity", _scModelUtils.getStringValueFromPath("ShipmentLine.Quantity", modelOutput), shipmentLineModel);
                        _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageQty", _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageQty", modelOutput), shipmentLineModel);
                    }
                    var backroomPickComplete = null;
                    backroomPickComplete = _scModelUtils.getStringValueFromPath("ShipmentLine.BackroomPickComplete", modelOutput);
                    if (
                    _scBaseUtils.isVoid(
                    backroomPickComplete)) {
                        backroomPickComplete = null;
                    }
                    _scModelUtils.setStringValueAtModelPath("ShipmentLine.BackroomPickComplete", backroomPickComplete, shipmentLineModel);
                    _scRepeatingPanelUtils.setModelForIndividualRepeatingPanel(
                    this, this.activeRepeatingPanelUId, _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", modelOutput), "QuantityTextBoxModel_Input", pickedQtyModel, null);
                    _scRepeatingPanelUtils.setModelForIndividualRepeatingPanel(
                            this, this.activeRepeatingPanelUId, _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", modelOutput), "QuantityReadOnlyModel_Input", pickedQtyModel, null);
                    _scScreenUtils.clearScreen(
                    repPanelScreen, "QuantityTextBoxModel_Output");
                    _scModelUtils.setNumberValueAtModelPath("ShipmentLine.BackroomPickedQuantity", _scModelUtils.getNumberValueFromPath("ShipmentLine.BackroomPickedQuantity", modelOutput), shipmentLineModel);
                    var args = null;
                    eventDefn = {};
                    args = {};
                    _scBaseUtils.setAttributeValue("ShipmentLine", shipmentLineModel, args);
                    _scBaseUtils.setAttributeValue("argumentList", args, eventDefn);
                    _scEventUtils.fireEventToChild(
                    this, repPanelUId, "handleQuantityChange", eventDefn);
                    _wscBackroomPickUpUtils.setPickedQuantityInScreen(
                    repPanelScreen, _scModelUtils.getStringValueFromPath("ShipmentLine.BackroomPickedQuantity", modelOutput));
                    _scBaseUtils.setAttributeValue("ShipmentLine", shipmentLineModel, eventArgs);
                    _iasScreenUtils.toggleHighlight(
                    this, repPanelScreen, "LastScannedShipmentLineScreenUId", "errorMsgPnl", "success", "Message_PickedQuantityUpdatedSuccessfully");
                }
                if (
                _scBaseUtils.equals(
                this.lastScannedItemShipmentLineKey, _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", modelOutput))) {
                    _scBaseUtils.setAttributeValue("argumentList", eventArgs, eventDefn);
                    var childScreen = null;
                    childScreen = _scScreenUtils.getChildScreen(
                    this, "lastProductScannedDetailsScreenRef");
                    _scScreenUtils.setModel(
                    this, "lastProductScanned_output", modelOutput, null);
                    if (!(
                    _scBaseUtils.isVoid(
                    childScreen))) {
                        _scScreenUtils.clearScreen(
                        childScreen, "PickedQuantity_Output");
                        if (!(
                        _scWidgetUtils.isWidgetVisible(
                        this, "lastProductScannedDetailsScreenRef"))) {
                            _scWidgetUtils.showWidget(
                            this, "lastProductScannedDetailsScreenRef", false, null);
                        }
                        _scEventUtils.fireEventInsideScreen(
                        childScreen, "updateLastProductScanned", eventDefn, eventArgs);
                    }
                }
            }
        },


sendCancellEmail:function(modelOutput)
{
   var ShipmentLineModel = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0", modelOutput);
   var qty = _scModelUtils.getStringValueFromPath("Quantity",ShipmentLineModel);
   var sShipmentLineKey=_scModelUtils.getStringValueFromPath("ShipmentLineKey",ShipmentLineModel);
 
  var completeShipmentModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
  var sDeliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",completeShipmentModel);
  //OMNI-30146 - START
  var sShipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",completeShipmentModel);
  //OMNI-30146 - END
  var cancelMailInput  = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
  var orderNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Order.0.OrderNo",completeShipmentModel);
  var emailID = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Order.0.CustomerEMailID",completeShipmentModel);
  _scModelUtils.setStringValueAtModelPath("Shipment.OrderNo",orderNo,cancelMailInput);
  _scModelUtils.setStringValueAtModelPath("Shipment.CustomerEMailID",emailID,cancelMailInput);
  _scModelUtils.setStringValueAtModelPath("Shipment.DeliveryMethod",sDeliveryMethod,cancelMailInput);
  //OMNI-30146 - START
  _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo",sShipmentNo,cancelMailInput);
  //OMNI-30146 - END
  var completShipmentLineList = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine",completeShipmentModel);
   for(var i in completShipmentLineList)
  {
  	var cShipmentLinekey= _scModelUtils.getStringValueFromPath("ShipmentLineKey", completShipmentLineList[i]);
  	_scModelUtils.setStringValueAtModelPath("OrderLine.OrderedQty", qty,completShipmentLineList[i]);
   }
 
  _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine",completShipmentLineList,cancelMailInput);

 _iasUIUtils.callApi(
            this, cancelMailInput, "extn_SendBOPISCancelMailMashup", null);

},
// OMNI - 4017, 5888, 5885 BOPIS: Cancel Email consolidation at Order level for cancellations- END 

//OMNI-3675::Last scanned Product hide for BOPIS & SFS - Start
handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "getNotPickedShipmentLineListCount")) {
                this.showHideShipmentLineList(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "validateChangeShipmentStatusOnNext")) {
                this.canProceedToNextScreen(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "saveShipmentStatusForPickUpOrder")) {
                this.gotoNextScreen();
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "saveShipmentStatusForShipOrder")) {
                this.gotoNextScreen();
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getNotPickedShipmentLineListOnNext")) {
                this.isPickComplete(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getAllShipmentLineList")) {
                this.updateShipmentLineListPanel(
                modelOutput, "ALLLINES");
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "getNotPickedShipmentLineList")) {
                this.updateShipmentLineListPanel(
                modelOutput, "SHORTLINES");
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "updateShipmentQuantityForPickAllLine")) {
             //OMNI-92366/OMNI-92921 - Start
               var shipmentDetailModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
               var vPackListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetailModel);
               var vShipmentType=_scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetailModel); 
			   if(_scBaseUtils.equals(vPackListType, "FA")&&  !_scBaseUtils.equals(vShipmentType, "STS")) {  
                  //var sSerialNo = _iasContextUtils.getFromContext("SerialNoContext");
                  var barcodeModel = _scScreenUtils.getModel(this, "extn_translateBarcode_ref_output");
                  if (!(_scBaseUtils.isVoid(barcodeModel))) {
                    var serialNoModel = _scModelUtils.getStringValueFromPath("BarCode.Shipment.ShipmentLine.ShipmentTagSerials", barcodeModel);
                    //OMNI-95070 Start
                    var shipmentTagSerials = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentTagSerials", modelOutput);
                    //OMNI-95070 End
                    var tempShipmentTagSerial = _scModelUtils.getStringValueFromPath("ShipmentTagSerial", shipmentTagSerials);
                   _scModelUtils.setStringValueAtModelPath("ShipmentTagSerial", tempShipmentTagSerial, barcodeModel.BarCode.Shipment.ShipmentLine.ShipmentTagSerials);
                 }else {
                      var modelForsOpNs = _scBaseUtils.getNewBeanInstance();
                       //OMNI-95070 Start
                       var shipmentTagSerials = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentTagSerials", modelOutput);
                       //OMNI-95070 End
                      var tempShipmentTagSerial = _scModelUtils.getStringValueFromPath("ShipmentTagSerial", shipmentTagSerials);
                      _scModelUtils.addModelToModelPath("BarCode.Shipment.ShipmentLine.ShipmentTagSerials.ShipmentTagSerial",tempShipmentTagSerial,modelForsOpNs);              
                     _scScreenUtils.setModel(this, "extn_translateBarcode_ref_output", modelForsOpNs, null);
                  }
                }
             //OMNI-92366/OMNI-92921 - End
            this.refreshShipmentLineAfterQuantityUpdate(modelOutput);
			//OMNI-3675: Start
			var completeShipmentModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
			var sDeliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",completeShipmentModel); 
               if((_scBaseUtils.equals(sDeliveryMethod,"SHP")) || (_scBaseUtils.equals(sDeliveryMethod,"PICK")))
                {
                	_scWidgetUtils.hideWidget(
                            this, "lastProductScannedDetailsScreenRef", null);
                }
			//OMNI-3675: End
			_iasContextUtils.addToContext("SerialNoContext","");//OMNI-92366
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "updateShipmentQuantityForPickAll")) {
                this.showConfirmMsgBoxAfterPickAll(
                modelOutput);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "translateBarCode")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "translateBarCode_output", modelOutput, null);
                }
                this.updateProductQuantity(
                modelOutput);
			//OMNI-3675: Start
			var completeShipmentModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
			var sDeliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",completeShipmentModel);
                if((_scBaseUtils.equals(sDeliveryMethod,"SHP")) || (_scBaseUtils.equals(sDeliveryMethod,"PICK")))
                {
                	_scWidgetUtils.hideWidget(
                            this, "lastProductScannedDetailsScreenRef", null);
                }
			//OMNI-3675: End
            }
			//OMNI-3675::Last scanned Product hide for BOPIS & SFS - End

			if (_scBaseUtils.equals(mashupRefId, "extn_getPrinterDeviceMashupRefId")) {
				var model=modelOutput;
				_scScreenUtils.setModel(
                this, "extn_getPrinterDeviceMashupRefId_output", modelOutput, null);
				var sPrinterCount = modelOutput.Devices;
				if(!_scBaseUtils.isVoid(sPrinterCount)){
					_iasContextUtils.addToContext("IsHipPrinterEnabled", "Y");
				}
				else {
					_iasContextUtils.addToContext("IsHipPrinterEnabled", "N");
				}				
			}
			
			//OMNI-90674 - Start
			if (
            _scBaseUtils.equals(
            mashupRefId, "extn_translateBarcode_ref")) {
                if (!(
                _scBaseUtils.equals(
                false, applySetModel))) {
                    _scScreenUtils.setModel(
                    this, "extn_translateBarcode_ref_output", modelOutput, null);					
                }
				var outputModel = _scScreenUtils.getModel(this,"extn_translateBarcode_ref_output");
				var sDuplicateScan = _scModelUtils.getStringValueFromPath("Error.ErrorMessage",outputModel);
				var sInvalidOutput = _scModelUtils.getStringValueFromPath("Error.ErrorDescription",outputModel);//OMNI-93022
				
				if (!_scBaseUtils.isVoid(
                sDuplicateScan)) {
					
                    _iasScreenUtils.showErrorMessageBoxWithOk(
                    this, sDuplicateScan);
                } //OMNI-93022 - Start 
				else if (!_scBaseUtils.isVoid(sInvalidOutput)){
				 
				_scWidgetUtils.enableWidget(this, "scanProductIdTxt", true);
				_scWidgetUtils.enableWidget(this, "addProductButton", true);				
				_scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
				_scWidgetUtils.disableWidget(this, "extn_serialNoBox", true);
				_scWidgetUtils.disableWidget(this, "extn_serialNoButton", true);
				_iasScreenUtils.showErrorMessageBoxWithOk(
                    this, sInvalidOutput);
				}//OMNI-93022 - End
				else{
				_iasContextUtils.addToContext("ItemIDContext","");
                this.updateProductQuantity(
                modelOutput);
			//OMNI-3675: Start
			var completeShipmentModel = _scScreenUtils.getModel(this,"backroomPickShipmentDetails_output");
			var sDeliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",completeShipmentModel);
                if((_scBaseUtils.equals(sDeliveryMethod,"SHP")) || (_scBaseUtils.equals(sDeliveryMethod,"PICK")))
                {
                	_scWidgetUtils.hideWidget(
                            this, "lastProductScannedDetailsScreenRef", null);
                }
			//OMNI-3675: End
			//OMNI-90829 START
				_scWidgetUtils.enableWidget(this, "scanProductIdTxt", true);
				_scWidgetUtils.enableWidget(this, "addProductButton", true);				
				_scWidgetUtils.setFocusOnWidgetUsingUid(this, "scanProductIdTxt");
				_scWidgetUtils.disableWidget(this, "extn_serialNoBox", true);
				_scWidgetUtils.disableWidget(this, "extn_serialNoButton", true);
				
			//OMNI-90829 End
				}
			}
			//OMNI-90674 - End
}
});
});

