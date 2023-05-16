
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/summary/ShipmentRTExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/RelatedTaskUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EditorUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/summary/ShipmentRTUI","scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/ScreenUtils","scbase/loader!ias/utils/ContextUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentRTExtnUI
			 ,
			 _iasBaseTemplateUtils, _iasContextUtils, _iasPrintUtils, _iasRelatedTaskUtils, _iasUIUtils, _scBaseUtils, _scEditorUtils, _scEventUtils, _scModelUtils, _scResourcePermissionUtils, _scScreenUtils, _wscShipmentUtils, _wscShipmentRTUI, _scUserprefs, _iasScreenUtils, iasContextUtils, _scWidgetUtils
){ 
	return _dojodeclare("extn.components.shipment.summary.ShipmentRTExtn", [_extnShipmentRTExtnUI],{
	// custom code here
	// OMNI-56184 - Overridden OOB method to bypass ShipmentKey check - START
	// invoking this method if it is a BOPIS order
	getShipmentDetailsForPick: function() {
        var shipmentSummaryScreen = false;
        shipmentSummaryScreen = _iasUIUtils.getParentScreen(this, true);
        var initialShipmentModel = null;
        initialShipmentModel = _scScreenUtils.getInitialInputData(_scEditorUtils.getCurrentEditor());
		var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", initialShipmentModel);
		var sessionObject = _scBaseUtils.getNewModelInstance();
		_scModelUtils.setStringValueAtModelPath("ShipNode",shipNode,sessionObject);
		window.sessionStorage.setItem("ShipNodeSessionObject",JSON.stringify(sessionObject));
        var newShipmentModel = null;
        newShipmentModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
        var shipmentKey = null;
        shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", initialShipmentModel);
		// STS HIP Printer Changes 
    if(!_scBaseUtils.isVoid(shipmentKey))
		{
			window.sessionStorage.removeItem("ShipmentSessionObject");
			window.sessionStorage.removeItem("ShipNodeSessionObject");
            _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, newShipmentModel);
            _iasUIUtils.callApi(
            this, newShipmentModel, "getShipmentDetails", null);
		}
	},
  // OMNI-56184 - Overridden OOB method to bypass ShipmentKey check - END
  
  // invoking this method if it is an STS order
  //OMNI-62991, OMNI-63947 Hiding ReprintOrderTicket for all other statuses other than RFCP status, whether HIP Printer is Enabled or disabled--Start
	getShipmentDetailsForSTS: function() {
		var shipmentSessionModel = JSON.parse(window.sessionStorage.getItem("ShipmentSessionObject"));
		var status = _scModelUtils.getStringValueFromPath("inputModel.Shipment.Status.Status", shipmentSessionModel);
		//Changes for OMNI-69059-- get  SOShipmentStatus and add a check in the if condition for SOShipmentStatus='1100.70.06.30.5'
		var SOShipmentStatus=_scModelUtils.getStringValueFromPath("inputModel.Shipment.SOShipmentStatus", shipmentSessionModel);
		if(!_scBaseUtils.equals(status, "1100.70.06.30.5")&&!_scBaseUtils.equals(SOShipmentStatus, "1100.70.06.30.5")&&
		!_scBaseUtils.equals(status, "1100.70.06.30.7") && !_scBaseUtils.equals(SOShipmentStatus, "1100.70.06.30.7"))
		{
		_iasRelatedTaskUtils.hideTaskInWebAndMobile(
				this, "extn_printOrderTicektMobile", false);
		}
    },
	// OMNI-63947,OMNI-62991- Hiding ReprintOrderTicket for all other statuses other than RFCP status, whether HIP Printer is Enabled or disabled - END
	extn_handlePrintOrderTicket: function() {
		// OMNI-62991- HIP Printer Changes for STS - START
	    var shipmentSessionModel = JSON.parse(window.sessionStorage.getItem("ShipmentSessionObject"));		
	 //OMNI-62991 - HIP Printer Reprint Changes for STS - END
		//check Hip Printer is present or Not
		var isHipPrinterEnabledFlag = _iasContextUtils.getFromContext("IsHipPrinterEnabled");

		if(!_scBaseUtils.isVoid(isHipPrinterEnabledFlag) && isHipPrinterEnabledFlag=="Y"){
			var sPrinterId =  _iasContextUtils.getFromContext("PrinterID");
	 // OMNI-62991 HIP Printer Reprint Changes for STS - START
			if(!_scBaseUtils.isVoid(shipmentSessionModel) && !_scBaseUtils.isVoid(sPrinterId)){
				this.extnReprintForHipPrinterSTS();
			}
			else if (!_scBaseUtils.isVoid(sPrinterId)){
	 // OMNI-62991 HIP Printer Reprint Changes for STS - END
				this.extnReprintForHipPrinter();
			}
			else{
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
				// OMNI-62991- HIP Printer Reprint Changes for STS - START 
				if(!_scBaseUtils.isVoid(shipmentSessionModel)){
				dialogParams["closeCallBackHandler"] = "extnReprintForHipPrinterSTS";
				}
				// OMNI-62991- HIP Printer Reprint Changes for STS - End 
				else{
				dialogParams["closeCallBackHandler"] = "extnReprintForHipPrinter";
				}
				dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			    _iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "User does not have a printer assigned. Please <br/>select a valid printer from the dropdown.", this, popupParams, dialogParams);
			}
		}
		 else if(_scBaseUtils.isVoid(shipmentSessionModel)){
			var shipmentDetailsModel = null;
			shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
			var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailsModel);
			var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", "N", inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "Y", inputModel);
			var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", shipmentDetailsModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, inputModel);
			var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentDetailsModel);
	   	_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, inputModel);
    	_iasUIUtils.callApi(this, inputModel, "extn_PrintOrderTicketSummary_ref", null);
		}
		// OMNI-62991 - HIP Printer Reprint Changes for STS - START 
		else {
			var shipmentSessionModel = null;
			shipmentSessionModel = JSON.parse(window.sessionStorage.getItem("ShipmentSessionObject"));
			var shipmentNo = _scModelUtils.getStringValueFromPath("inputModel.Shipment.ShipmentNo", shipmentSessionModel);
			var shipmentKey = _scModelUtils.getStringValueFromPath("inputModel.Shipment.ShipmentKey", shipmentSessionModel);
			var shipNodeSessionModel = JSON.parse(window.sessionStorage.getItem("ShipNodeSessionObject"));
			var shipNode = _scModelUtils.getStringValueFromPath("ShipNode", shipNodeSessionModel);
			var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", "N", inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "Y", inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, inputModel);
			_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, inputModel);
			//OMNI-67859 - Reprint with Loftware if HIP Printer is not enabled - START
            _scModelUtils.setStringValueAtModelPath("Shipment.FulfillmentType", "STS" , inputModel);
			//OMNI-67859 - Reprint with Loftware if HIP Printer is not enabled - END
			_iasUIUtils.callApi(this, inputModel, "extn_PrintOrderTicketSummary_ref", null);
		}
		// OMNI-62991 - HIP Printer Reprint Changes for STS - END 

    },
	extnReprintForHipPrinter : function(actionPerformed)	{
	    var shipmentDetailsModel = null;
        shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
        var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailsModel);
		var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
		var isHipPrinterEnabledFlag = _iasContextUtils.getFromContext("IsHipPrinterEnabled");
        _scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", isHipPrinterEnabledFlag, inputModel);	
		var sPrinterId =  _iasContextUtils.getFromContext("PrinterID");
		if (!_scBaseUtils.isVoid(sPrinterId)){
			_scModelUtils.setStringValueAtModelPath("Shipment.PrinterID", sPrinterId, inputModel);
		}
		var labelNumber =  _iasContextUtils.getFromContext("NumberOfLabels");
		if(!_scBaseUtils.isVoid(labelNumber)){
			_scModelUtils.setStringValueAtModelPath("Shipment.NumberOfLabels", labelNumber, inputModel);
		}
		var sPrinterIP =  _iasContextUtils.getFromContext("IPAddress");
		if (!_scBaseUtils.isVoid(sPrinterIP)){
			_scModelUtils.setStringValueAtModelPath("Shipment.PrinterIP", sPrinterIP, inputModel);
		}
		var shipmentNo = _scModelUtils.getStringValueFromPath("Shipment.ShipmentNo", shipmentDetailsModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, inputModel);
		var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentDetailsModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, inputModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "Y", inputModel);
					
		_iasUIUtils.callApi(this, inputModel, "extn_PrintOrderTicketSummary_ref", null);
	},
	// OMNI-62991 HIP Printer Reprint Changes for STS - START 
	extnReprintForHipPrinterSTS : function(actionPerformed)	{
	    var shipNodeSessionModel = JSON.parse(window.sessionStorage.getItem("ShipNodeSessionObject"));
		var shipNode = _scModelUtils.getStringValueFromPath("ShipNode", shipNodeSessionModel);
		var shipmentSessionModel = null;
	    shipmentSessionModel = JSON.parse(window.sessionStorage.getItem("ShipmentSessionObject"));
		var shipmentNo = _scModelUtils.getStringValueFromPath("inputModel.Shipment.ShipmentNo", shipmentSessionModel);
		var shipmentKey = _scModelUtils.getStringValueFromPath("inputModel.Shipment.ShipmentKey", shipmentSessionModel);
		var SOInputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, SOInputModel);
        _scModelUtils.setStringValueAtModelPath("Shipment.IsHipPrinterEnabled", "Y", SOInputModel);	
		var sPrinterId =  _iasContextUtils.getFromContext("PrinterID");
		if (!_scBaseUtils.isVoid(sPrinterId)){
			_scModelUtils.setStringValueAtModelPath("Shipment.PrinterID", sPrinterId, SOInputModel);
		}
		_scModelUtils.setStringValueAtModelPath("Shipment.NumberOfLabels", "1", SOInputModel);
		var sPrinterIP =  _iasContextUtils.getFromContext("IPAddress");
		if (!_scBaseUtils.isVoid(sPrinterIP)){
			_scModelUtils.setStringValueAtModelPath("Shipment.PrinterIP", sPrinterIP, SOInputModel);
		}
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentNo", shipmentNo, SOInputModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.ShipNode", shipNode, SOInputModel);
		_scModelUtils.setStringValueAtModelPath("Shipment.IsReprint", "Y", SOInputModel);
		/* OMNI-67859 - Changes to re-print in HIP Printer if inital print happened with LOFTWARE - START */
		_scModelUtils.setStringValueAtModelPath("Shipment.FulfillmentType", "STS" , SOInputModel);
		/* OMNI-67859 - Changes to re-print in HIP Printer if inital print happened with LOFTWARE - END */
				_iasUIUtils.callApi(this, SOInputModel, "extn_PrintOrderTicketSummary_ref", null);
	},
	// OMNI-62991  - HIP Printer Reprint  Changes for STS - END 
	checkIfHipPrinterAvailable : function(event, bEvent, ctrl, args){
		//method to check if the store is enabled for hip printer
		var sTargetModel = _scScreenUtils.getTargetModel(this, "extn_getPrinterDeviceInitMashup_output", null);
		_iasUIUtils.callApi(this, sTargetModel, "extn_getPrinterDeviceInitMashup", null);	          
	},
	
	extn_changePrinterSelection: function() {
		var bindings = null;
		bindings = {};
		var popupParams = null;
		popupParams = {};
		popupParams["binding"] = bindings;
		var dialogParams = null;
		dialogParams = {};
		dialogParams["closeCallBackHandler"] = "extnOnContainerSelection";
		dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
		_iasUIUtils.openSimplePopup("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer", "Please select a printer from the dropdown.", this, popupParams, dialogParams);
	},
    // overridden this method to invoke custom backend service 'AcademySFSPrintShipmentPickTickets'
    handlePrintPickTicket: function(
        event, bEvent, ctrl, args) {
            var shipmentDetailsModel = null;
            shipmentDetailsModel = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
            if (!(
            this.hasPermissionForPrintPickTicket(
            shipmentDetailsModel))) {
                _iasBaseTemplateUtils.showMessage(
                this, _scScreenUtils.getString(
                this, "Message_UserHasNoPermission"), "error", null);
            } else {
                var shipmentStatus = null;
                shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetailsModel);
                if (
                _scBaseUtils.contains(
                shipmentStatus, "1100.70.06.10") || _scBaseUtils.contains(
                shipmentStatus, "1100.70.06.20") || _scBaseUtils.contains(shipmentStatus, "1100.70.06.50") || _scBaseUtils.contains(shipmentStatus, "1100.70.06.70")) {
                    var printPickTicketInputModel = null;
                    // printPickTicketInputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipments");
                    printPickTicketInputModel = _scModelUtils.createNewModelObjectWithRootKey("Print");
                    var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailsModel);
                    var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentDetailsModel);
                    var pickTicketPrinted = _scModelUtils.getStringValueFromPath("Shipment.PickTicketPrinted", shipmentDetailsModel);
                    var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentDetailsModel);
                    var loginId = _scUserprefs.userId;
                    _scModelUtils.setStringValueAtModelPath("Print.ShipmentKey", shipmentKey, printPickTicketInputModel);
                    _scModelUtils.setStringValueAtModelPath("Print.ShipmentType", shipmentType, printPickTicketInputModel);
                    _scModelUtils.setStringValueAtModelPath("Print.PickTicketPrinted", pickTicketPrinted, printPickTicketInputModel);
                    _scModelUtils.setStringValueAtModelPath("Print.ShipNode", shipNode, printPickTicketInputModel);
                    _scModelUtils.setStringValueAtModelPath("Print.Login.LoginID", loginId, printPickTicketInputModel);
                    // _iasUIUtils.callApi(this, printPickTicketInputModel, "searchPickTicket", null);
                    _iasUIUtils.callApi(this, printPickTicketInputModel, "extn_PrintShipmentPickTickets_ref", null);
                } else {
                    _iasBaseTemplateUtils.showMessage(
                    this, "Message_PickTicketCannotBePrinted", "error", null);
                }
            }
        },
		
        // overridden this method to hide the links printPickTicekt and printOrderTicket on Order Summary of customerPickup screen
        handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel) {
            if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentDetailsForRecordCustomerPick")) {
                this.handle_getShipmentDetailsForRecordCustomerPick(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
			
			if ( _scBaseUtils.equals(mashupRefId, "extn_getPrinterDeviceInitMashup")) {
				_scScreenUtils.setModel(this, "extn_getPrinterDeviceInitMashup_output", modelOutput, null);
				var sPrinterCount = modelOutput.Devices;
        //getting shipment session object
				var shipmentSessionModel = JSON.parse(window.sessionStorage.getItem("ShipmentSessionObject"));
				if(_scBaseUtils.isVoid(sPrinterCount))
				{
				/*disable the link of change printer & reprint order ticket for an STS order
					if the store is not enabled for hip printer */
				// OMNI-63947,OMNI-62991 HIP Printer Reprint Changes for STS - START
					_iasContextUtils.addToContext("IsHipPrinterEnabled","N");
					if(!_scBaseUtils.isVoid(shipmentSessionModel)){
						_iasRelatedTaskUtils.disableTaskInWebAndMobile(this, "extn_ChangePrinterMobile", false);
						_iasRelatedTaskUtils.disableTaskInWebAndMobile(this, "extn_ChangePrinterLink", false);
					}
				// OMNI-63947,OMNI-62991 HIP Printer Reprint Changes for STS - END
				/*hide the link of change printer for a BOPIS order
					if the store is not enabled for hip printer */
					else{
					_iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "extn_ChangePrinterMobile", false);					
					_iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "extn_ChangePrinterLink", false);
					}
     				}
				else{
					_iasContextUtils.addToContext("IsHipPrinterEnabled","Y");
				}
				//OMNI-63947 - calling STS method for hide or show Reprint Order Ticket
				if(!_scBaseUtils.isVoid(shipmentSessionModel)) {
					this.getShipmentDetailsForSTS(); 
				}
			}
			if (
            _scBaseUtils.equals(
            mashupRefId, "getShipmentDetails")) {
                this.handle_getShipmentDetails(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            	var shipmentDetailsModel = null;
	        	var shipmentStatus = null;
	            shipmentDetailsModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
	            shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetailsModel);
	            // adding new conditions to hide pick ticket for ready for customer pick and ready for packing and packing in progress
                if(_scBaseUtils.equals(shipmentStatus, "1400") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.50") || _scBaseUtils.equals(shipmentStatus, "1100.70.06.70")) {
			    	//_iasRelatedTaskUtils.disableTaskInWebAndMobile(this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.disableTaskInWebAndMobile(this, "extn_printOrderTicketLink", false);
			            	_iasRelatedTaskUtils.disableTaskInWebAndMobile(this, "extn_printOrderTicektMobile", true);
			    }
                // Staging location from related task to be disabled for SFS
                var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentDetailsModel);
                if (_scBaseUtils.equals(deliveryMethod, "SHP")) {
              
					_iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "extn_ChangePrinterMobile", true);
				  _iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "extn_ChangePrinterLink", true);
					_iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "extn_printOrderTicektMobile", true);                 
                
					_iasRelatedTaskUtils.hideTaskInWebAndMobile(this, "lnk_RT_AssignToHold", false);
                }
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "searchPickTicket")) {
                this.handle_searchPickTicket(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "printAcknowledgement")) {
                this.handle_printAcknowledgement(
                mashupRefId, modelOutput, mashupInput, mashupContext, applySetModel);
            }
            if (
            _scBaseUtils.equals(
            mashupRefId, "containerPack_StorePackSlip_94") || _scBaseUtils.equals(
            mashupRefId, "extn_PrintShippingLabelSummary_ref") || _scBaseUtils.equals(
            mashupRefId, "extn_PrintReturnLabelSummary_ref") || _scBaseUtils.equals(
            mashupRefId, "extn_PrintORMDLabelSummary_ref")|| _scBaseUtils.equals(
            mashupRefId, "extn_STS_RePrintShippingLabelSummary_ref"))
			{
                var msg = _scScreenUtils.getString(this, "extn_Print_Success");
			_iasScreenUtils.showInfoMessageBoxWithOk(this,msg,"onCloseOfPrintSuccess",{});
            }
        },
		
		/* This method is called after handling the output of mashup with mashuprefid="containerPack_StorePackSlip_94/extn_PrintShippingLabelSummary_ref/extn_PrintReturnLabelSummary_ref/extn_PrintORMDLabelSummary_ref" */
		onCloseOfPrintSuccess : function(result, args)
		{
			if (_scBaseUtils.equals(result, "Ok")) 
			{
				//do nothing
			}
		},
		
		// OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report -start
 extn_RecordStoreUserAction:function(event,bEvent,ctrl,args)
	{
		 var shipmentDetailModel = null;
         shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentDetails_output");
		 var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentNo",shipmentDetailModel);
		 var sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.OrderNo",shipmentDetailModel);
		 var sDeliveryMethod=_scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod",shipmentDetailModel);
		 var sUserID=_scUserprefs.getUserId();
		 //OMNI-79480 - Starts
		 var sExtnIsCurbsidePickupOpted =_scModelUtils.getStringValueFromPath("Shipment.Extn.ExtnIsCurbsidePickupOpted",shipmentDetailModel);
		 //OMNI-79480 - Ends
		 var recordStoreUserActionMashup= _scBaseUtils.getNewModelInstance();
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.ShipmentNo",sShipmentNo,recordStoreUserActionMashup);
		 if(_scBaseUtils.isVoid(sOrderNo)){
			 sOrderNo=_scModelUtils.getStringValueFromPath("Shipment.DisplayOrderNo",shipmentDetailModel);
		 }
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.OrderNo",sOrderNo,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.Delivery_Method",sDeliveryMethod,recordStoreUserActionMashup);
		 _scModelUtils.setStringValueAtModelPath("AcadStoreActionData.UserID",sUserID,recordStoreUserActionMashup);	 
		 //OMNI-79480 - Starts
		 if(_scBaseUtils.equals(sExtnIsCurbsidePickupOpted, "Y")){
			_scModelUtils.setStringValueAtModelPath("AcadStoreActionData.NotifyStore",'N',recordStoreUserActionMashup);
		 }
		 //OMNI-79480 - Ends
		 _iasUIUtils.callApi(
            this, recordStoreUserActionMashup, "extn_RecordStoreUserActionMashup", null);		 
	},	

	//OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report-End

		/*This OOB method has been overriden in order to handle the enabling/disabling/hiding/showing of newly added/OOB links under Related Tasks */ 
		hideOrShowRelatedTasks: function(
        event, bEvent, ctrl, args) 
		{
            var shipmentDetailsModel = null;
            shipmentDetailsModel = _scBaseUtils.getAttributeValue("shipmentModel", false, args);
			var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentDetailsModel);
			var shipmentStore = null;
			shipmentStore = _scModelUtils.getStringValueFromPath("Shipment.ExtnShipNode", shipmentDetailsModel);
			if(_scBaseUtils.isVoid(shipmentStore))
			{
				shipmentStore = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentDetailsModel);
			}
			var deliveryMethod = null;
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentDetailsModel);
            var statusPath = null;
            statusPath = _scBaseUtils.getAttributeValue("statusPath", false, args);
            var status = null;
            status = _scModelUtils.getStringValueFromPath(
            statusPath, shipmentDetailsModel);
			var currentStore = iasContextUtils.getFromContext("CurrentStore");
            if (
            _scBaseUtils.equals(
            deliveryMethod, "SHP")) 
			{
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_PrintPickTicket", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_RecordPickShipment", false);
                _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                this, "lnk_RT_AssignToHold", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_PackShipment", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_UnpackShipment", false);
				
				
                if (
                _scBaseUtils.contains(
                status, "1100.70.06.10")) {
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordPickShipment", false);
                } else if (
                _scBaseUtils.contains(
                status, "1100.70.06.20")) {		  
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordPickShipment", false);
                    _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                    this, "lnk_RT_AssignToHold", false);
                } else if (
                _scBaseUtils.contains(
                status, "1100.70.06.50")) {
					_iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                    this, "lnk_RT_AssignToHold", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PackShipment", false);
                } else if (
                _scBaseUtils.contains(
                status, "1100.70.06.70")) {
					_iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PackShipment", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_UnpackShipment", false);
				} 
				else if (
                _scBaseUtils.contains(
                status, "1300") || _scBaseUtils.contains(
                status, "1100.70.06.30")) {
                    
					/* This below code has been implemented to resolve BOPIS - 1266 JIRA */
					if(_scBaseUtils.contains(status, "1300"))
					{
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "lnk_RT_UnpackShipment", false);
					}
                    
					
					/* Customization starts */
					/* The below links will be shown only if same store orders are opened */
					if(_scBaseUtils.equals(currentStore, shipmentStore))
					{
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/*_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "lnk_RT_PrintPackSlip", false);
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printReturnLabel", false);*/
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printShippingLabel", false);
					
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printORMDLabel", false);
						
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/*_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "lnk_RT_PrintPackSlip", false);	
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printReturnLabel", false);*/
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */						
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printShippingLabel", false);
						if( _scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT"))
						{
							_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printORMDLabel", false);
						}
						
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printShippingLabel", false);
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/*_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printReturnLabel", false); 
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printReturnLabel", false); */
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printORMDLabel", false);
						
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printShippingLabel", false);
						
						if( _scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT"))
						{
							_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printORMDLabel", false);
						}
					}
					
					/* Customization ends */
                } else if (
                _scBaseUtils.contains(
                status, "1400")) 
				{
					/* Customization starts */
					/* The below links will be shown only if same store orders are opened */
					if(_scBaseUtils.equals(currentStore, shipmentStore))
					{
						
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/* _iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "lnk_RT_PrintPackSlip", false);
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printReturnLabel", false); */
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printShippingLabel", false);
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_printORMDLabel", false);
						
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/* _iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "lnk_RT_PrintPackSlip", false); 
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printReturnLabel", false);*/
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */						
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printShippingLabel", false);
						
						if( _scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT"))
						{
							_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_printORMDLabel", false);
						}
						
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printShippingLabel", false);
						/* OMNI - 48152 START Disabling Pack Slip and Return Instructions  */
						/*_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printReturnLabel", false);
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printReturnLabel", false);						*/
						/* OMNI - 48152 END Disabling Pack Slip and Return Instructions  */
						_iasRelatedTaskUtils.showTaskInWebAndMobile(
						this, "extn_m_lnk_printORMDLabel", false);
						
						_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printShippingLabel", false);
						
						if( _scBaseUtils.equals(shipmentType, "AMMO") || _scBaseUtils.equals(shipmentType, "HAZMAT"))
						{
							_iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_lnk_printORMDLabel", false);
						}
					
					}
				}
            } else if (
            _scBaseUtils.equals(
            deliveryMethod, "PICK")) {
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_PrintPickTicket", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_RecordPickShipment", false);
                _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                this, "lnk_RT_AssignToHold", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_RecordCustomerPick", false);
                _iasRelatedTaskUtils.showTaskInWebAndMobile(
                this, "lnk_RT_PrintReceipt", false);
				
                if (
                _scBaseUtils.contains(
                status, "1100.70.06.10")) {
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordPickShipment", false);					
                  	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicektMobile", true);
                   	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicketLink", true);
					
					
                } 

             // OMNI-96068 START
                if(_scBaseUtils.equals(status,"1100.70.06.10.5")){
                  _iasRelatedTaskUtils.disableTaskInWebAndMobile(
                    this, "lnk_RT_RecordPickShipment", false);
				  
			  }

             //OMNI-96068 END



				else if (
                _scBaseUtils.contains(
                status, "1100.70.06.20")) {
                   	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicektMobile", true);
                   	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicketLink", true);		  
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintPickTicket", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordPickShipment", false);
                    _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                    this, "lnk_RT_AssignToHold", false);
					
					
                }

                //BOPIS-1503: resolution changes: commented below else if condition; conflict with next else if block in case of "Ready For Customer Pick Up" status - starts
                /*else if (
                _scBaseUtils.contains(
                status, "1100.70.06.30")) {*/
					/* The below code has been committed as this status number is being used for Ready to Ship and these should not be enabled for this status */
				  /* _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_AssignToHold", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordCustomerPick", false); */
                //} 
                //BOPIS-1503: resolution changes: commented below else if condition; conflict with next else if block in case of "Ready For Customer Pick Up" status - ends

				/* Customization starts */
				else if (
                _scBaseUtils.contains(
                status, "1100.70.06.30.5")) {
					_iasRelatedTaskUtils.showTaskInWebAndMobile(
					this, "extn_printOrderTicketLink", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "extn_printOrderTicketLink", false);
					
					_iasRelatedTaskUtils.showTaskInWebAndMobile(
					this, "extn_printOrderTicektMobile", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "extn_printOrderTicektMobile", false);
					
					 _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                    this, "lnk_RT_AssignToHold", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordCustomerPick", false);

					
				}
				/* Customization ends */
				//BOPIS -2057 Added Shipment Invoice Status check for PickAck Link- Start
			  else if (
                _scBaseUtils.contains(
                status, "1100.70.06.30.7")) {
                        _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_RecordCustomerPick", false);
        }
      
      	else if (
                _scBaseUtils.contains(
                status, "1400") || _scBaseUtils.contains(
                status, "1600.002")) 
				//BOPIS -2057 Added Shipment Invoice Status check for PickAck Link- End
                {
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
                    this, "lnk_RT_PrintReceipt", false);
		                _iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicektMobile", true);
                   	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicketLink", true);
               
                }
				/* Customization starts */
				else if (
                _scBaseUtils.contains(
                status, "9000")) {
                	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicektMobile", true);
                 	_iasRelatedTaskUtils.hideTaskInWebAndMobile
                   	(this, "extn_printOrderTicketLink", true);
        					if((!_scBaseUtils.equals(currentStore, shipmentStore)))
        					{
        						_iasRelatedTaskUtils.disableTaskInWebAndMobile(
        						this, "lnk_RT_RecordPickShipment", false);
        						_iasRelatedTaskUtils.hideTaskInWebAndMobile(
        						this, "lnk_RT_AssignToHold", false);
        						_iasRelatedTaskUtils.disableTaskInWebAndMobile(
        						this, "lnk_RT_RecordCustomerPick", false);
        					}
                }
				/* Customization ends */
            }
            if (!(
            this.hasPermissionForPrintPickTicket(
            shipmentDetailsModel))) {
                _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                this, "lnk_RT_PrintPickTicket", false);
            }
            if (!(
            this.hasPermissionForBackroomPick(
            shipmentDetailsModel))) {
                _iasRelatedTaskUtils.hideTaskInWebAndMobile(
                this, "lnk_RT_RecordPickShipment", false);
            }
			/* OMNI-47066 Reprint STS2.0 Label  -  START */
			var documentType = null;
			documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentDetailsModel);
			if (_scBaseUtils.equals(documentType, "0006")) 
			{
					if (_scBaseUtils.contains(status, "1100.70.06.30"))
                {
                    _iasRelatedTaskUtils.showTaskInWebAndMobile(
                    this, "extn_m_link_Reprint_STS_Shipping_Label", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
						this, "extn_m_link_Reprint_STS_Shipping_Label", false);
					_iasRelatedTaskUtils.showTaskInWebAndMobile(
                   this, "extn_link_reprintSTSShippingLabel", false);
                    _iasRelatedTaskUtils.enableTaskInWebAndMobile(
				    this, "extn_link_reprintSTSShippingLabel", false);
                }
				
			}
			/* OMNI-47066 Reprint STS2.0 Label  -  END */
			
			/* OMNI-47439 Reprint Pack Slip Button Disabled for STS 2.0 & OMNI-52307 Reprint Return Instructions Label Disabled for STS 2.0-  Start */
			var documentType = null;
			documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentDetailsModel);
			/* OMNI-48152 - Start Disabling Pack Slip and Return Instructions */
			/*if (_scBaseUtils.equals(documentType, "0006")) 
			{
				if(_iasContextUtils.isMobileContainer())
				{
					// Reprint Pack Slip Button Disabled In Mobile Screen
					_iasRelatedTaskUtils.disableTaskInWebAndMobile(
							this, "m_lnk_RT_PrintPackSlip", false);
						
					// Reprint Return Instructions Label Disabled In Mobile Screen
					_iasRelatedTaskUtils.disableTaskInWebAndMobile(
							this, "extn_m_lnk_printReturnLabel", false);
				}
				else
				{
					Reprint Pack Slip Button Disabled In Desktop Screen
					_iasRelatedTaskUtils.disableTaskInWebAndMobile(
							this, "lnk_RT_PrintPackSlip", false);
						
					Reprint Return Instructions Label Disabled In Desktop Screen
					_iasRelatedTaskUtils.disableTaskInWebAndMobile(
							this, "extn_printReturnLabel", false);
				}
			} */
			/* OMNI-48152 - End Disabling Pack Slip and Return Instructions */
			/* OMNI-47439 Reprint Pack Slip Button Disabled for STS 2.0 & OMNI-52307 Reprint Return Instructions Label Disabled for STS 2.0 -  End */
			
			/* OMNI-47066 Reprint STS2.0 Label  -  START */
			if (_scBaseUtils.contains(status, "1100.70.06.30") && _scBaseUtils.equals(documentType, "0006"))
            {
                _iasRelatedTaskUtils.showTaskInWebAndMobile(this, "extn_m_link_Reprint_STS_Shipping_Label", false);
                _iasRelatedTaskUtils.enableTaskInWebAndMobile(this, "extn_m_link_Reprint_STS_Shipping_Label", false);    
            }
			/* OMNI-47066 Reprint STS2.0 Label  -  END */
			
        },
		
		/* This method is for handling the onClick event of Reprint Pack Slip Label link */
		handlePrintPackSlip: function(event, bEvent, ctrl, args) 
		{
			this.rePrintPack = "Y";
			this.extnContainerPopup(event, bEvent, ctrl, args);
		},
		
		/* This method is for handling the onClick event of Reprint Shipping Label link */
		extnReprintShippingLabelMethod : function(event, bEvent, ctrl, args) 
		{
			this.rePrintShip = "Y";
			this.extnContainerPopup(event, bEvent, ctrl, args);
        },
		
		/* This method is for handling the onClick event of Reprint STS Shipping Label link */
		/* OMNI-47066 Reprint STS2.0 Label  -  START */
		extnReprintSTSShippingLabelMethod : function(event, bEvent, ctrl, args) 
		{
			this.rePrintSTSShip = "Y";
			this.extnContainerPopup(event, bEvent, ctrl, args);
        },
		/* OMNI-47066 Reprint STS2.0 Label  -  END */
		
		/* This method is for handling the onClick event of Reprint Return Label link */
		extnReprintReturnLabelMethod : function(event, bEvent, ctrl, args) 
		{
			this.rePrintReturn = "Y";
			this.extnContainerPopup(event, bEvent, ctrl, args);
        },
		
		/* This method is for handling the onClick event of Reprint ORM-D Label link */
		extnReprintORMDLabelMethod : function(event, bEvent, ctrl, args) 
		{
			this.rePrintORMD = "Y";
			this.extnContainerPopup(event, bEvent, ctrl, args);
        },
		
		/* This method formulates the input for container popup */
		extnContainerPopup : function(event, bEvent, ctrl, args) 
		{
			var shipmentDetailsModel = null;
            shipmentDetailsModel = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
			var containerDetails = _scModelUtils.getStringValueFromPath("Shipment.Containers.Container",shipmentDetailsModel);
			
			var containerListModel={};
			var containerModel = {};
					containerModel = _scModelUtils.createModelObjectFromKey("Container", containerListModel);
			
			var containerNoModel = [];
			for(var containerIteration in containerDetails)
			{
				var containerNo = _scModelUtils.getStringValueFromPath("ContainerNo",containerDetails[containerIteration]);
				var containerKey = _scModelUtils.getStringValueFromPath("ShipmentContainerKey",containerDetails[containerIteration]);
				containerNoModel.push({
                         'ContainerNo': containerNo,
						 'ShipmentContainerKey' : containerKey
                     });
			}
			_scModelUtils.addModelToModelPath("Container", containerNoModel,containerModel);
			
			var bindings = null;
			bindings = {};
			bindings["ContainerNoData"] = containerModel;
			var popupParams = null;
			popupParams = {};
			popupParams["screenInput"] = containerModel;
			popupParams["outputNamespace"] = "SelectedContainerModel";
			popupParams["binding"] = bindings;
			var dialogParams = null;
			dialogParams = {};
			dialogParams["closeCallBackHandler"] = "extnOnContainerSelection";
			dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
			_iasUIUtils
				.openSimplePopup(
						"extn.components.shipment.summary.SFSContainerPopup",
						"extn_SelectContainerTitle", this,
						popupParams, dialogParams);
		},
		
		/* This method is to handle the output of Reprint Shipping Label popup */
		extnOnContainerSelection : function(actionPerformed,model, popupParams) 
		{
			if (!(_scBaseUtils.equals(actionPerformed, "CLOSE")))
			{
				var checkReprintShip = this.rePrintShip;
				var checkReprintReturn = this.rePrintReturn;
				var checkReprintPack = this.rePrintPack;
				var checkReprintORMD = this.rePrintORMD;
				var checkReprintSTSShip = this.rePrintSTSShip;
				var shortedShipmentLineModel = _scScreenUtils
					.getModel(this, "SelectedContainerModel");
				var shipmentDetailsModel = null;
				shipmentDetailsModel = _scScreenUtils.getModel(
				this, "getShipmentDetails_output");
					
				var shipmentContainerKey = _scModelUtils.getStringValueFromPath("Container", shortedShipmentLineModel);
				var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentDetailsModel);
				var pickTicketPrinted = _scModelUtils.getStringValueFromPath("Shipment.PickTicketPrinted", shipmentDetailsModel);
				
				var reprintInputModel = null;
				reprintInputModel = _scModelUtils.createNewModelObjectWithRootKey("Print");
				_scModelUtils.setStringValueAtModelPath("Print.ShipmentContainerKey", shipmentContainerKey, reprintInputModel);
				_scModelUtils.setStringValueAtModelPath("Print.ShipmentKey", shipmentKey, reprintInputModel);
				
				if(_scBaseUtils.isVoid(pickTicketPrinted))
				{
					_scModelUtils.setStringValueAtModelPath("Print.PickTicketPrinted", "N", reprintInputModel);
				}
				if(!(_scBaseUtils.isVoid(pickTicketPrinted)))
				{
					_scModelUtils.setStringValueAtModelPath("Print.PickTicketPrinted", pickTicketPrinted, reprintInputModel);
				}
				
				if(_scBaseUtils.equals(checkReprintShip, "Y"))
				{
					this.rePrintShip = "N";
					_iasUIUtils.callApi(
                    this, reprintInputModel, "extn_PrintShippingLabelSummary_ref", null);
				}
				else if(_scBaseUtils.equals(checkReprintReturn, "Y"))
				{
					this.rePrintReturn="N";
					_iasUIUtils.callApi(
                    this, reprintInputModel, "extn_PrintReturnLabelSummary_ref", null);
				}
				else if(_scBaseUtils.equals(checkReprintORMD, "Y"))
				{
					this.rePrintORMD = "N";
					_iasUIUtils.callApi(
                    this, reprintInputModel, "extn_PrintORMDLabelSummary_ref", null);
				}
				else if(_scBaseUtils.equals(checkReprintPack, "Y"))
				{
					this.rePrintPack = "N";
					_iasUIUtils.callApi(
                    this, reprintInputModel, "containerPack_StorePackSlip_94", null);
				}
				/* OMNI-47066 Reprint STS2.0 Label  -  START */
				else if(_scBaseUtils.equals(checkReprintSTSShip, "Y"))
				{
					this.rePrintSTSShip = "N";
					_iasUIUtils.callApi(
                    this, reprintInputModel, "extn_STS_RePrintShippingLabelSummary_ref", null);
				}
				/* OMNI-47066 Reprint STS2.0 Label  -  END */
                    
			}
		},
	//Overriden OOTB method to invoke custom print ack service as a part of BOPIS-1186: Begin
		handlePrintReceipt: function(
        event, bEvent, ctrl, args) {
            var shipmentDetailsModel = null;
            shipmentDetailsModel = _scScreenUtils.getModel(
            this, "getShipmentDetails_output");
            var deliveryMethod = null;
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentDetailsModel);
            var shipNode = null;
            shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentDetailsModel);
            if (!(
            _scBaseUtils.equals(
            shipNode, _iasContextUtils.getFromContext("CurrentStore")))) {
                _iasBaseTemplateUtils.showMessage(
                this, _scScreenUtils.getString(
                this, "Message_AckCannotBePrintedDiffentShipNode"), "error", null);
            } else if (!(
            _scBaseUtils.equals(
            deliveryMethod, "PICK"))) {
                _iasBaseTemplateUtils.showMessage(
                this, _scScreenUtils.getString(
                this, "Message_AckCannotBePrinted"), "error", null);
            } else {
                var shipmentStatus = null;
                shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status", shipmentDetailsModel);
                //BOPIS -2057 Added Shipment Invoice Status check for PickAck Link- Start
                if (
                _scBaseUtils.contains(shipmentStatus, "1400")|| _scBaseUtils.contains(shipmentStatus, "1600.002")) {
                	//BOPIS -2057 Added Shipment Invoice Status check for PickAck Link- End
                    var printAckModel = null;
                    printAckModel = _scScreenUtils.getTargetModel(
                    this, "printAcknowledgement_input", null);
                    // _iasUIUtils.callApi(
                    // this, printAckModel, "printAcknowledgement", null);
                    var inputModel = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
	                var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", printAckModel);
	                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", shipmentKey, inputModel);
	                _iasUIUtils.callApi(this, inputModel, "extn_AcademyBOPISPrintAckSlip_ref", null);
                } else {
                    _iasBaseTemplateUtils.showMessage(
                    this, _scScreenUtils.getString(
                    this, "Message_AckCannotBePrintedStatus"), "error", null);
                }
            }
        },
        //Overriden OOTB method to invoke custom print ack service as a part of BOPIS-1186: End
        
        
});
});
