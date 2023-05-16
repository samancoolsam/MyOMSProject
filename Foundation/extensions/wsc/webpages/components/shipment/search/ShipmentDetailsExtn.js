
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/search/ShipmentDetailsExtnUI","scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/ContextUtils", "scbase/loader!ias/utils/PrintUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/common/utils/CommonUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/search/ShipmentDetailsUI", "scbase/loader!wsc/components/shipment/search/utils/SearchUtils","scbase/loader!sc/plat/dojo/Userprefs","scbase/loader!ias/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!extn/components/shipment/common/utils/ShipmentUtilsExtn"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentDetailsExtnUI
			 ,
			 	_iasBaseTemplateUtils, _iasContextUtils, _iasPrintUtils, _iasUIUtils, _scBaseUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _wscCommonUtils, _wscShipmentUtils, _wscShipmentDetailsUI, _wscSearchUtils,_scUserprefs,_iasScreenUtils,_scEventUtils, _extnShipmentUtilsExtn
){ 
	return _dojodeclare("extn.components.shipment.search.ShipmentDetailsExtn", [_extnShipmentDetailsExtnUI],{
	// custom code here
	// code changes for SLA indicator : Begin
	extn_afterInitializeScreen: function(event, bEvent, ctrl, args) {
		 var shipmentList = null;
		 var deliveryMethod = null;
		 shipmentList = _scScreenUtils.getModel(this, "getShipmentList_output");
		 deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentList);
		 var shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentList);
		 if (_scBaseUtils.equals(deliveryMethod, "SHP") || (!_scBaseUtils.equals(shipmentStatus, "1100.70.06.10") && !_scBaseUtils.equals(shipmentStatus, "1100.70.06.20") )) {
		 	 _scWidgetUtils.hideWidget(this, "lbl_timeRemaining", true);
            _scWidgetUtils.hideWidget(this, "img_TimeRmnClock", true);
            if (_scBaseUtils.equals(shipmentStatus, "1100.70.06.30")) {
            	_scWidgetUtils.hideWidget(this,"lnkPack",false);
        	}
		 }
	},
	      //Start - OMNI-48473 STS2.0 - Overridden initialization mashup to use custom util for displaying next task
        initializeScreen: function(
        event, bEvent, ctrl, args) {
            var shipmentList = null;
            var status = null;
            var deliveryMethod = null;
            var shipNode = null;
            shipmentList = _scScreenUtils.getModel(
            this, "getShipmentList_output");
            var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", shipmentList);
            if(_scBaseUtils.equals(documentType,"0006")){
                _extnShipmentUtilsExtn.showNextTask(
                this, shipmentList, "Shipment.ShipNode", "lnkStartBRP", "lnkContinueBRP", "lnkPrint", "lnkPack", "lnkContinuePack", "lnkStartCustomerPickup");
            } else{
            	_wscShipmentUtils.showNextTask(
                this, shipmentList, "Shipment.ShipNode", "lnkStartBRP", "lnkContinueBRP", "lnkPrint", "lnkPack", "lnkContinuePack", "lnkStartCustomerPickup");  
            }
            _wscShipmentUtils.showHideHoldLocation(
            this, shipmentList, "lblHoldLocation");
            deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", shipmentList);
			
			//OMNI-95718 Complete Assembly start
			var spStatus=_scModelUtils.getStringValueFromPath("Shipment.Status.Status",shipmentList);
            if(_scBaseUtils.equals(documentType,"0001") && _scBaseUtils.equals(deliveryMethod,"PICK")
			  && _scBaseUtils.equals(spStatus,"1100.70.06.10.5")){
             _scWidgetUtils.showWidget(this, "extn_buttonWS_Assembly", true);
			 _scWidgetUtils.hideWidget(this,"lnkContinueBRP", false);
				
			  }
                 
            //OMNI-95718 Complete Assembly END
			
			
			
            if (
            _scBaseUtils.equals(
            deliveryMethod, "SHP")) {
                _scWidgetUtils.hideWidget(
                this, "lblExpectedShipDate", true);
                _scWidgetUtils.showWidget(
                this, "lblCarrier", true, null);
            }
            _wscSearchUtils.scrollCenter();
            _scWidgetUtils.setFocusOnWidgetUsingUid(
            this, "lblOrderNo");
            status = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentList);
            if (!(
            _wscShipmentUtils.showSLA(
            status))) {
                _scWidgetUtils.hideWidget(
                this, "lbl_timeRemaining", true);
                _scWidgetUtils.hideWidget(
                this, "img_TimeRmnClock", true);
            } else {
                var urlString = null;
                urlString = _scModelUtils.getStringValueFromPath("Shipment.ImageUrl", shipmentList);
                if (!(
                _scBaseUtils.isVoid(
                urlString))) {
                    var imageUrlModel = null;
                    imageUrlModel = _scModelUtils.createNewModelObjectWithRootKey("CommonCode");
                    _scModelUtils.setStringValueAtModelPath("CommonCode.CodeLongDescription", urlString, imageUrlModel);
                    _scModelUtils.setStringValueAtModelPath("CommonCode.CodeShortDescription", _scModelUtils.getStringValueFromPath("Shipment.ImageAltText", shipmentList), imageUrlModel);
                    _scScreenUtils.setModel(
                    this, "clockImageBindingValues", imageUrlModel, null);
                }
            }
        },
		
		//OMNI-95718 Complete Assembly start
		ws_CompleteAssembly_click: function(event, bEvent, ctrl, args){
		 _scScreenUtils.showConfirmMessageBox(this, _scScreenUtils.getString(this, "extn_complete_assembly_confirmation"), "handleClickAssembly", null);//OMNI-98466
		},
		// OMNI-95718 Complete Assembly END
		//OMNI-98466
		handleClickAssembly : function(res, args) {
			if (_scBaseUtils.equals(res, "Ok")) {
				_iasContextUtils.addToContext("isRedirectFromListScreen", "Y");
				this.openShipmentDetails();
			} 
			else if (_scBaseUtils.equals(res, "Cancel")) {
				_scWidgetUtils.showWidget(this, "extn_button_Assembly");
			}
		},
		
		
        //End - OMNI-48473 STS2.0 - Overridden initialization mashup to use custom util for displaying next task
	// code changes for SLA indicator : End
	handlePickTicket: function(event, bEvent, ctrl, args) {
        var shipmentModel = null;
        shipmentModel = _scScreenUtils.getModel(this, "getShipmentList_output");
        var printPickTicketInputModel = null;
        printPickTicketInputModel = _scModelUtils.createNewModelObjectWithRootKey("Print");
        var shipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", shipmentModel);
        var shipmentType = _scModelUtils.getStringValueFromPath("Shipment.ShipmentType", shipmentModel);
        var pickTicketPrinted = _scModelUtils.getStringValueFromPath("Shipment.PickTicketPrinted", shipmentModel);
        var shipNode = _scModelUtils.getStringValueFromPath("Shipment.ShipNode", shipmentModel);
        var loginId = _scUserprefs.userId;
        _scModelUtils.setStringValueAtModelPath("Print.ShipmentKey", shipmentKey, printPickTicketInputModel);
        _scModelUtils.setStringValueAtModelPath("Print.ShipmentType", shipmentType, printPickTicketInputModel);
        _scModelUtils.setStringValueAtModelPath("Print.PickTicketPrinted", pickTicketPrinted, printPickTicketInputModel);
        _scModelUtils.setStringValueAtModelPath("Print.ShipNode", shipNode, printPickTicketInputModel);
        _scModelUtils.setStringValueAtModelPath("Print.Login.LoginID", loginId, printPickTicketInputModel);
        // _iasUIUtils.callApi(this, printPickTicketInputModel, "searchPickTicket", null);
        _iasUIUtils.callApi(this, printPickTicketInputModel, "extn_PrintShipmentPickTicketsSearch_ref", null);
        
    },

    getDeliveryMethod: function(dataValue, screen, widget, namespace, modelObj, options) {
        var model = _scScreenUtils.getModel(this, "getShipmentList_output");

        var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", model);
        var ShipmentLines = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", model);
	var documentType = _scModelUtils.getStringValueFromPath("Shipment.DocumentType", model);
        if(!_scBaseUtils.isVoid(ShipmentLines)) {
            var shipmentLine = ShipmentLines[0];
            var fulfillmentType = _scModelUtils.getStringValueFromPath("OrderLine.FulfillmentType", shipmentLine);
        }
        if(_scBaseUtils.equals(deliveryMethod, "PICK")) {
            var dMethod = "Pick-up"
            return dMethod;
        }
        else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(fulfillmentType, "SOF")) {
            var dMethod = "Special Order";
            return dMethod;
        }
	//Start - STS2.0 OMNI-48473
	else if (_scBaseUtils.equals(deliveryMethod, "SHP") && _scBaseUtils.equals(documentType, "0006")) {
		var dMethod="STS";
		return dMethod;
	} //End - STS2.0 OMNI-48473
        else {
            var dMethod = "SFS";
            return dMethod;
        }
    },
//OMNI-8711 - Update Cognos: BOPIS IncompletePickup Shipment Report -start

 extn_RecordStoreUserAction:function(event,bEvent,ctrl,args)
	{
		 var shipmentDetailModel = null;
         shipmentDetailModel = _scScreenUtils.getModel(this, "getShipmentList_output");
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

//OMNI-8711 Update Cognos: BOPIS IncompletePickup Shipment Report- End

     //BOPIS-1612: Begin
    openPackWizard: function() {
        var shipmentModel = null;
        shipmentModel = _scScreenUtils.getTargetModel(
        this, "getShipmentDetails_input", null);
        _iasUIUtils.callApi(this, shipmentModel, "extn_getShipmentList_ref", null);
        // _wscShipmentUtils.openPackingWizard(
        // this, shipmentModel);
    },
    extn_afterBehaviorMashupCall: function(event, bEvent, ctrl, args) {
        var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
        if (!_scBaseUtils.isVoid(mashupRefList)) {
            for (var i = 0; i < mashupRefList.length; i++) {
                var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if (_scBaseUtils.equals(mashupRefid, "extn_getShipmentList_ref")) {
                    var modelOutput = mashupRefList[i].mashupRefOutput;
                    var shipments = _scModelUtils.getStringValueFromPath("Shipments.Shipment", modelOutput);
                    var shipment = shipments[0];
                    var status = _scModelUtils.getStringValueFromPath("Status", shipment);
                    var shipmentModel = null;
                    shipmentModel = _scScreenUtils.getTargetModel(
                    this, "getShipmentDetails_input", null);
                    if (_scBaseUtils.equals(status, "1100.70.06.50")) {
                        _wscShipmentUtils.openPackingWizard(this, shipmentModel);
                        _scWidgetUtils.hideWidget(this, "lnkPack", true);
                    }
                    else {
                        var parentScreen = _iasUIUtils.getParentScreen(this, true);
                        _scEventUtils.fireEventToParent(parentScreen, "SST_SearchButton_onClick", null);
                    }
                }
            }
        }
    }
    //BOPIS-1612: End
});
});

