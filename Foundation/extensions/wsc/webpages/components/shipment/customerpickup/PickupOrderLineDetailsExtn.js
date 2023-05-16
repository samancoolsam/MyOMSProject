
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/customerpickup/PickupOrderLineDetailsExtnUI", "scbase/loader!ias/utils/ScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/product/common/utils/ProductUtils", "scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils", "scbase/loader!wsc/components/shipment/customerpickup/PickupOrderLineDetailsUI", "scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!ias/utils/ContextUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnPickupOrderLineDetailsExtnUI
			 ,
			 	_iasScreenUtils, _iasUIUtils, _scBaseUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _wscProductUtils, _wscShipmentUtils, _wscPickupOrderLineDetailsUI,_iasBaseTemplateUtils,_iasContextUtils

){ 
	return _dojodeclare("extn.components.shipment.customerpickup.PickupOrderLineDetailsExtn", [_extnPickupOrderLineDetailsExtnUI],{
	// custom code here
	initializeScreen: function(
        event, bEvent, ctrl, args) {
            var shipmentLineModel = null;
            var varItemWidget = null;
            shipmentLineModel = _scScreenUtils.getModel(
            this, "ShipmentLine");
            var staging = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnStagingLocation", shipmentLineModel)
            _scWidgetUtils.setValue(this, "extn_screenbase_datalabel_staging", staging , false);
			//OMNI-84925 START
			_scWidgetUtils.hideWidget(this, "extn_Line_Shipment_No");
			var isCurbsideOrder = shipmentLineModel.ShipmentLine.Shipment[0].Extn.ExtnIsCurbsidePickupOpted;	
			if (_iasContextUtils.isMobileContainer()){ 			
				 var shipmentNo = shipmentLineModel.ShipmentLine.Shipment[0].ShipmentNo;
				_scWidgetUtils.setValue(this, "extn_Line_Shipment_No", shipmentNo , false);
				_scWidgetUtils.showWidget(this, "extn_Line_Shipment_No");
			}
			//OMNI-84925 END
            varItemWidget = _wscProductUtils.createItemVariationPanel(
            this, "itemVariationPanelUp", shipmentLineModel, "ShipmentLine.OrderLine.ItemDetails.AttributeList.Attribute");
            if (!(
            _scBaseUtils.isVoid(
            varItemWidget))) {
                _scWidgetUtils.placeAt(
                this, "itemVariationPanelHolder", "itemVariationPanelUp", null);
            }

        // Over-ride need to be changed to event. Done for disabling quantity buttons
            var parentScreen = _iasUIUtils.getParentScreen(this, true);
            var pShipmentDetails = _scScreenUtils.getModel(parentScreen, 'ShipmentDetails');
        	var Shipments = _scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine", pShipmentDetails);
        	var shipmentLine = Shipments[0];
        	var paymentInfo = _scModelUtils.getStringValueFromPath("Order.PaymentStatus", shipmentLine);
			
			//OMNI-85831- Start
			var currentShipmentKey = _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", pShipmentDetails);
			var cShipmentKey = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentKey", shipmentLineModel);
			//OMNI-85831- End
			
        if(_scBaseUtils.equals(paymentInfo,"AWAIT_PAY_INFO") ||  _scBaseUtils.equals(paymentInfo,"AWAIT_AUTH")) {
			//OMNI-85831
			if (_scBaseUtils.equals(currentShipmentKey, cShipmentKey)) {
            _scWidgetUtils.disableWidget(this, "addQtyLink");
            _scWidgetUtils.disableWidget(this, "removeQtyLink");
            _scWidgetUtils.disableWidget(this, "txtScannedQuantity");
             // OMNI-3637 - BOPIS: Automate Authorization Failure - START
           this.extn_ShortPick(shipmentLineModel);
	  _scScreenUtils.showErrorMessageBox(
                this, "Payment is not authorized. Please proceed to POS to checkout the customer. The customer's credit card has not been charged.", "CancelOrder", _iasUIUtils.getTextOkObjectForMessageBox(this), null);
			}
                //    _iasBaseTemplateUtils.showMessage(
              // this, "Payment is not authorized, please proceed with record shortage using 'authorization failure' option for each line", "error", null);
               // OMNI-3637 - BOPIS: Automate Authorization Failure - END
        }
        // _iasScreenUtils.showInfoMessageBoxWithOk(this, "Payment is not Autherized, please proceed with record shortage", "infoCallBack", null);
        //Age Verification changes to dispaly Age Restriction value : Begin
        var shipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey", shipmentLineModel);
        if(!_scBaseUtils.isVoid(Shipments)) {
            for (var i=0;i<Shipments.length;i++) {
                var parentShipmentLineKey =  _scModelUtils.getStringValueFromPath("ShipmentLineKey", Shipments[i]);
                if(_scBaseUtils.equals(shipmentLineKey, parentShipmentLineKey)) {
                    var extnAgeRestrictionCode = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnAgeRestrictionCode", Shipments[i]);
                    var restrictedAgeFlag = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnIsAgeRestricted", Shipments[i]);
                    if(_scBaseUtils.equals(restrictedAgeFlag ,"N")){
                        _scWidgetUtils.setValue(this, "extn_ageRestriction", "None");
                    }
                    else {
                        _scWidgetUtils.setValue(this, "extn_ageRestriction", extnAgeRestrictionCode);
                    }
                }
            }
        }
        //Age Verification changes to dispaly Age Restriction value : Begin
    },

    infoCallBack: function() {

    },
 // OMNI-3637 - BOPIS: Automate Authorization Failure - START
 extn_ShortPick:function(shipmentLineModel)
{
    var shipmentLineModelInput = _scBaseUtils.getNewModelInstance();
            var ShipmentLineKey = _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey",shipmentLineModel);
            var quantity = _scModelUtils.getStringValueFromPath("ShipmentLine.Quantity",shipmentLineModel);
            _scModelUtils.setStringValueAtModelPath("MarkAllShortLineWithShortage","N",shipmentLineModelInput);
            _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageResolutionReason","Authorization Failure",shipmentLineModelInput);
            _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShipmentLineKey",ShipmentLineKey,shipmentLineModelInput);
            _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageQty",quantity,shipmentLineModelInput);
            _scModelUtils.setStringValueAtModelPath("ShipmentLine.CancelReason","Authorization Failure",shipmentLineModelInput);
       	   _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageQty",quantity,shipmentLineModelInput);
	   _scModelUtils.setStringValueAtModelPath("ShipmentLine.ShortageQty",quantity,shipmentLineModelInput);
           if(!_scBaseUtils.equals(quantity, "0.00"))
            {
              var eventArgs = null;
                var eventDefn = null;
                eventDefn = {};
                eventArgs = {};
                _scBaseUtils.setAttributeValue("inputData", shipmentLineModelInput, eventArgs);
                _scBaseUtils.setAttributeValue("argumentList", eventArgs, eventDefn);
                _scEventUtils.fireEventToParent(
                this, "updateShortageForShipmentLines", eventDefn);
	    }
},

 CancelOrder: function()
{
              var eventArgs = null;
                var eventDefn = null;
                eventDefn = {};
                eventArgs = {};
              //  _scBaseUtils.setAttributeValue("inputData", shipmentLineModelInput, eventArgs);
                _scBaseUtils.setAttributeValue("argumentList", eventArgs, eventDefn);
                _scEventUtils.fireEventToParent(
                this, "extn_FinishPick_button_onClick", eventDefn);


},
 // OMNI-3637 - BOPIS: Automate Authorization Failure - END
    // Overr OOTB method to disable onclick event on image
    openItemDetails: function(
    event, bEvent, ctrl, args) {
        // var itemDetails = null;
        // itemDetails = _scScreenUtils.getTargetModel(
        // this, "ItemDetails_Output", null);
        // _wscProductUtils.openItemDetails(
        // this, itemDetails);
    },
    //BOPIS-1641- BEGIN
    fireEventToUpdateShipmentLineQuantity: function(
        lineAction, shipmentLineQuantity) {
            var shipmentLineModel = null;
            shipmentLineModel = _scScreenUtils.getTargetModel(
            this, "ShipmentLine_Output", null);
            var shipmentLineSourceModel = _scScreenUtils.getModel(
            this, "ShipmentLine");
            var originalQty = _scModelUtils.getStringValueFromPath("ShipmentLine.OriginalQuantity", shipmentLineSourceModel);
            _scModelUtils.setStringValueAtModelPath("ShipmentLine.OriginalQuantity",originalQty,shipmentLineModel);
            _scModelUtils.setNumberValueAtModelPath("ShipmentLine.CustomerPickedQuantity", shipmentLineQuantity, shipmentLineModel);
            var shipmentModel = null;
            shipmentModel = _scScreenUtils.getTargetModel(
            this, "Shipment_Output", null);
            shipmentModel = _wscShipmentUtils.importShipmentLineToShipment(
            shipmentModel, shipmentLineModel);
            var eventArgs = null;
            var eventDefn = null;
            eventDefn = {};
            eventArgs = {};
            _scBaseUtils.setAttributeValue("inputData", shipmentModel, eventArgs);
            _scBaseUtils.setAttributeValue("argumentList", eventArgs, eventDefn);
            _scEventUtils.fireEventToParent(
            this, "updateShipmentLineDetails", eventDefn);
        },
        //BOPIS-1641- END
		
		 // START -  (OMNI - 1410 )  : BOPIS "Picked Up" image on BOPIS Screen		
		 getShortageQuantity: function(
        unformattedValue, screen, widget, namespace, model, options) {
            var shipmentQuantity = null;
            var customerPickedQuantity = null;
            var shortageQuantity = null;
            var shortageDisplayQuantity = null;
            var shortageResolution = null;
            var zero = 0;
            shipmentQuantity = _scModelUtils.getNumberValueFromPath("ShipmentLine.Quantity", model);
            customerPickedQuantity = _scModelUtils.getNumberValueFromPath("ShipmentLine.CustomerPickedQuantity", model);
            shortageResolution = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageResolutionReason", model);
            _scWidgetUtils.hideWidget(
            this, "productShortedImagePanel", false);
            if (!(
            _iasUIUtils.isValueNumber(
            customerPickedQuantity))) {
                customerPickedQuantity = zero;
            }
            shortageQuantity = _wscShipmentUtils.subtract(
            shipmentQuantity, customerPickedQuantity);
            if (
            _scBaseUtils.equals(
            shipmentQuantity, zero)) {
                _scWidgetUtils.hideWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", true);
				        this, "extn_screenbase_button", true);
                //End - OMNI-3680 BOPIS:Record Shortage Button
                _scWidgetUtils.hideWidget(
                this, "productScanStatusImage", true);
                _scWidgetUtils.hideWidget(
                this, "completelyPickedLabel", true);
                _scWidgetUtils.hideWidget(
                this, widget, true);
                _scWidgetUtils.showWidget(
                this, "productShortedImagePanel", false, null);
                _scScreenUtils.addClass(
                this, "completeShort");
                _scWidgetUtils.disableFields(
                this, null);
            } else if (
            _scBaseUtils.equals(
            shortageQuantity, zero)) {
                _scWidgetUtils.hideWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", true);
				    this, "extn_screenbase_button", true);
            //End - OMNI-3680 BOPIS:Record Shortage Button
				//if (_iasContextUtils.isMobileContainer()) {
                _scWidgetUtils.hideWidget(
                this, "productScanStatusImage", true);
				_scWidgetUtils.setLabel(this,"completelyPickedLabel","Items Added For Pickup Process",null);
				/*}
				else{
				_scWidgetUtils.showWidget(
                this, "productScanStatusImage", false, null);	
				} */
                _scWidgetUtils.showWidget(
                this, "completelyPickedLabel", false, null);
                _scWidgetUtils.hideWidget(
                this, widget, true);
            } else if (!(
            _scBaseUtils.isVoid(
            shortageResolution))) {
                shortageDisplayQuantity = _wscShipmentUtils.getFormattedDisplayQuantity(
                shortageQuantity, screen, widget, namespace, model, options);
                _scWidgetUtils.hideWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", true);
				  this, "extn_screenbase_button", true);
          //End - OMNI-3680 BOPIS:Record Shortage Button
                _scWidgetUtils.hideWidget(
                this, "productScanStatusImage", true);
                _scWidgetUtils.hideWidget(
                this, "completelyPickedLabel", true);
                _scWidgetUtils.showWidget(
                this, widget, false, null);
                if (
                _scBaseUtils.equals(
                customerPickedQuantity, zero)) {
                    _scWidgetUtils.showWidget(
                    this, "productShortedImagePanel", false, null);
                }
            } else {
                _scWidgetUtils.hideWidget(
                this, widget, true);
                _scWidgetUtils.hideWidget(
                this, "productScanStatusImage", true);
                _scWidgetUtils.hideWidget(
                this, "completelyPickedLabel", true);
                _scWidgetUtils.showWidget(
                //Start - OMNI-3680 BOPIS:Record Shortage Button
                //this, "shortageResolutionLink", false, null);
				this, "extn_screenbase_button", true);
        //End - OMNI-3680 BOPIS:Record Shortage Button
            }
            return shortageDisplayQuantity;
        }, 		
        // END -  (OMNI - 1410 )  : BOPIS "Picked Up" image on BOPIS Screen
});
});

