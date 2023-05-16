
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/common/screens/ShipmentLineDetailsExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!ias/utils/UIUtils","scbase/loader!wsc/components/product/common/utils/ProductUtils","scbase/loader!wsc/components/shipment/backroompick/utils/BackroomPickUpUtils","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils","scbase/loader!dojo/dom-attr","scbase/loader!ias/utils/ContextUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentLineDetailsExtnUI
			,
				_scScreenUtils
			,
				_scBaseUtils
			,
				_scModelUtils
			,
				_scEventUtils
			,
				_scWidgetUtils
			,
			    _iasUIUtils
			,
			    _wscProductUtils
			,
			     _wscBackroomPickUpUtils
			,
			    _scResourcePermissionUtils
             ,  dDomAttr
			 ,_iasContextUtils
){ 
	return _dojodeclare("extn.components.shipment.common.screens.ShipmentLineDetailsExtn", [_extnShipmentLineDetailsExtnUI],{
	// custom code here
	      //Start - OMNI-3680 BOPIS:Record Shortage Button
	        initializeScreen: function(
        event, bEvent, ctrl, args) {
             // OMNI-66083 START 
                var getCommonCodeInput = {};				
				getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
				_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "NON_EDITABLE_ADDQTY_SCANNEDQTY" , getCommonCodeInput);
				_iasUIUtils.callApi(this, getCommonCodeInput, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty", null); 
             //  OMNI-66083 END
             //OMNI-90928/OMNI-93071 START
			  var parentScreen=_iasUIUtils.getParentScreen(this, true);
			  var shipmentDetailModel= _scScreenUtils.getModel(parentScreen, "backroomPickShipmentDetails_output");	
			  var vPackListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetailModel);
			  var vShipmentType=_scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetailModel);	
	          if(!_scBaseUtils.equals(vPackListType, "FA")&&  !_scBaseUtils.equals(vShipmentType, "STS")) {
                _scWidgetUtils.hideWidget(this, "extn_datalabelSNO");
			  } if(_scBaseUtils.equals(vPackListType, "FA")&&  !_scBaseUtils.equals(vShipmentType, "STS")){
			    var shipmentLineModel = null;
                shipmentLineModel = _scScreenUtils.getModel( this, "ShipmentLine");
                var modelForsOpNs = _scBaseUtils.getNewBeanInstance();	
			    var shipmentTagSerialsModel=_scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentTagSerials",shipmentLineModel);
                _scModelUtils.addModelToModelPath("BarCode.Shipment.ShipmentLine.ShipmentTagSerials",shipmentTagSerialsModel,modelForsOpNs);
				_scScreenUtils.setModel(this, "extn_SerialNoValue", modelForsOpNs, null);
			}	  
			 //OMNI-90928/OMNI-93071 END
			//OMNI-104474--START 
		 var vExtnAsseblyRequested=_scModelUtils.getStringValueFromPath("Shipment.ShipmentLines.ShipmentLine.0.OrderLine.Extn.ExtnIsAssemblyRequired",shipmentDetailModel);
		if(_scBaseUtils.equals(vExtnAsseblyRequested, "Y")){
			_scWidgetUtils.showWidget(this,"extn_Assebmly_Requested",false,"");
		}else{
				_scWidgetUtils.hideWidget(this,"extn_Assebmly_Requested",false);
			}
			//OMNI-104474--END  
			 
            var shipmentLineModel = null;
            var shipmentLinePickedQuantity = null;
            var varItemWidget = null;
            shipmentLineModel = _scScreenUtils.getModel(
            this, "ShipmentLine");
            var quantityTextBoxModel = null;
            quantityTextBoxModel = {};
            shipmentLinePickedQuantity = _scModelUtils.getStringValueFromPath("ShipmentLine.BackroomPickedQuantity", shipmentLineModel);
            /*OMNI-31379 Changes -- Start*/
			var yantriksEnabled = _scModelUtils.getStringValueFromPath("ShipmentLine.YantriksEnabled", shipmentLineModel);
			if (yantriksEnabled == "N")
			_scWidgetUtils.showWidget(this, "extn_datalabel2");
			/*OMNI-31379 Changes -- End*/
            if(_iasUIUtils.isValueNumber(shipmentLinePickedQuantity)) {
            	this.pickedQuantity = shipmentLinePickedQuantity;
            } else {
            	this.pickedQuantity = _scBaseUtils.formatNumber("0", "ShipmentLine.BackroomPickedQuantity", "Quantity");
            }
            
            _scModelUtils.setStringValueAtModelPath("Quantity", this.pickedQuantity, quantityTextBoxModel);
            _scScreenUtils.setModel(
            this, "QuantityTextBoxModel_Input", quantityTextBoxModel, null);
            _scScreenUtils.setModel(
                    this, "QuantityReadOnlyModel_Input", quantityTextBoxModel, null);
            varItemWidget = _wscProductUtils.createItemVariationPanel(
            this, "itemVariationPanelUp", shipmentLineModel, "ShipmentLine.OrderLine.ItemDetails.AttributeList.Attribute");
            if (!(
            _scBaseUtils.isVoid(
            varItemWidget))) {
                _scWidgetUtils.placeAt(
                this, "itemVariationPanelHolder", "itemVariationPanelUp", null);
            }
            if (
            _scBaseUtils.equals(
            this.flowName, "ContainerPack")) {
                var bundleString = null;
                bundleString = _scScreenUtils.getString(
                this, "Label_Packed");
                _scWidgetUtils.setValue(
                this, "pickedLabel", bundleString, false);
            }
            
        	if(!_scResourcePermissionUtils.hasPermission("WSC000029")) {
        		_scWidgetUtils.showWidget(this, "bpickedQtyLbl", true, null);
        		_scWidgetUtils.hideWidget(this, "uom_lbl", false);
        		var quantityReadOnlyModel = {};
        		_scModelUtils.setStringValueAtModelPath("Quantity", this.pickedQuantity, quantityReadOnlyModel);
        		_scScreenUtils.setModel(this, "QuantityReadOnlyModel_Input", quantityReadOnlyModel, null);
            } 
            
            
            var shortageQty = 0;
            shortageQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.ShortageQty", shipmentLineModel);
            if (
            shortageQty >= 1) {
                this.handleShipmentLineShortage(
                shipmentLineModel);
            } else {
                _wscBackroomPickUpUtils.validatePickedQuantity(
                this, shipmentLineModel, "productScanImagePanel", "completelyPickedLabel", "productScanShortageImagePanel", "updateShortageResolutionImage", "extn_button");
            }
           
           //OMNI-45645: SIM Integration -- Start
			var strAvlStockOnHnd = null;
			var strLastRecDate = null;
			var strItemPrice = null;
			
			strAvlStockOnHnd = _scModelUtils.getStringValueFromPath("ShipmentLine.AvailableStockOnHand", shipmentLineModel);
			strLastRecDate = _scModelUtils.getStringValueFromPath("ShipmentLine.LastReceivedDate", shipmentLineModel);
			strItemPrice = _scModelUtils.getStringValueFromPath("ShipmentLine.ItemPrice", shipmentLineModel);
			
			if(!_scBaseUtils.isVoid(strAvlStockOnHnd) || !_scBaseUtils.isVoid(strLastRecDate) || !_scBaseUtils.isVoid(strItemPrice)){
				strAvlStockOnHnd = "Available stock on hand: " + " " + strAvlStockOnHnd;
				_scWidgetUtils.setValue(this, "extn_AvlStockOnHand", strAvlStockOnHnd, false);
				
				strLastRecDate = "Last received date: " + " " + strLastRecDate;
				_scWidgetUtils.setValue(this, "extn_ItemLRD", strLastRecDate, false);
				
				strItemPrice = "Item price: " + strItemPrice;
				_scWidgetUtils.setValue(this, "extn_ItemPriceLabel", strItemPrice, false);
				
			}else{
				strAvlStockOnHnd = "Available stock on hand:";
				_scWidgetUtils.setValue(this, "extn_AvlStockOnHand", strAvlStockOnHnd, false);
				
				strLastRecDate = "Last received date:";
				_scWidgetUtils.setValue(this, "extn_ItemLRD", strLastRecDate, false);
				
				strItemPrice = "Item price:";
				_scWidgetUtils.setValue(this, "extn_ItemPriceLabel", strItemPrice, false);
			}
			//OMNI-45645: SIM Integration -- End
           
        },
          //OMNI-66083 START
        	extn_AfterBehaviorMashupCall: function(event, bEvent, ctrl, args){
			var mashupRefList = _scModelUtils.getModelObjectFromPath("mashupArray", args);
			if (!_scBaseUtils.isVoid(mashupRefList)) {
				for (var i = 0; i < mashupRefList.length; i++) {
					var mashupRefid = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
						if (_scBaseUtils.equals(mashupRefid, "extn_getFlagToDisableAddQtyAndReadOnlyScannedQty")) {
							var modelOutput = mashupRefList[i].mashupRefOutput;
							var isManualEntry = modelOutput.CommonCodeList.CommonCode[0].CodeShortDescription;
								if(!(_scBaseUtils.isVoid(isManualEntry)) && (_scBaseUtils.equals(isManualEntry,"Y"))){
									_scWidgetUtils.disableWidget(this, "addQtyLink",true);
									var fs = this.getWidgetByUId("txtScannedQuantity");
									dDomAttr.set(fs.textbox, "readonly", true);						 
								 }
						}
				}
			}
			
		},
        //OMNI-66083 END
        handleQuantityChange: function(
        event, bEvent, ctrl, args) {
            var remainingQty = null;
            _scWidgetUtils.hideWidget(
            this, "updateQtyButton", false);
            var shipmentLineModel = null;
            shipmentLineModel = _scBaseUtils.getModelValueFromBean("ShipmentLine", args);
            remainingQty = _wscBackroomPickUpUtils.getFormattedRemainingQuantity(
            this, shipmentLineModel);
             //OMNI-90928/OMNI-93071 START
			 var parentScreen=_iasUIUtils.getParentScreen(this, true);
			 var shipmentDetailModel= _scScreenUtils.getModel(parentScreen, "backroomPickShipmentDetails_output");	
			 var vPackListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentDetailModel);
			 var vShipmentType=_scModelUtils.getStringValueFromPath("Shipment.ShipmentType",shipmentDetailModel);
              if(_scBaseUtils.equals(vPackListType, "FA")&&  !_scBaseUtils.equals(vShipmentType, "STS")){
                 var BackroomPickProductScan = _iasUIUtils.getParentScreen(this, true);
	             var barcodeModel = _scScreenUtils.getModel(BackroomPickProductScan, "extn_translateBarcode_ref_output");
			     _scScreenUtils.setModel(this, "extn_SerialNoValue", barcodeModel, null);
			}//OMNI-90928/OMNI-93071 END
            _scWidgetUtils.setValue(
            this, "remainingQty", remainingQty, false);
            var shortageQty = 0;
            shortageQty = _scModelUtils.getNumberValueFromPath("ShipmentLine.ShortageQty", shipmentLineModel);
            if (
            shortageQty >= 1) {
                this.handleShipmentLineShortage(
                shipmentLineModel);
				//Record Shortage Button
				this.isShipmentLineMarkedPartialShortage(
                shipmentLineModel);
				//Record Shortage Button
            } else {
                _wscBackroomPickUpUtils.validatePickedQuantity(
                this, shipmentLineModel, "productScanImagePanel", "completelyPickedLabel", "productScanShortageImagePanel", "updateShortageResolutionImage", "extn_button");
            }
        },
        //OMNI-90928/OMNI-93071 START
         extn_getSerialNo: function(event, bEvent, ctrl, args) {
         var BackroomPickProductScan = _iasUIUtils.getParentScreen(this, true);
	     //var barcodeModel = _scScreenUtils.getModel(BackroomPickProductScan, "extn_translateBarcode_ref_output");
	     var barcodeModel = _scScreenUtils.getModel(this, "extn_SerialNoValue")
		 var fnlStr=null;	
		    if(!(_scBaseUtils.isVoid(barcodeModel))) {
              var serNoWgt=_scWidgetUtils.getValue(this, "extn_datalabelSNO");
			  var serialNoModel = _scModelUtils.getStringValueFromPath("BarCode.Shipment.ShipmentLine.ShipmentTagSerials.ShipmentTagSerial", barcodeModel);
			if(!_scBaseUtils.isVoid(serialNoModel)) {
			    fnlStr='';
			    for(var i=0;i<serialNoModel.length;i++) {
                       var serialNoValues=_scModelUtils.getStringValueFromPath("SerialNo", serialNoModel[i]);
                       fnlStr=fnlStr+serialNoValues;
                        if(i!=serialNoModel.length-1){
                          fnlStr=fnlStr+', '
						}
			          }   }
		          }
         return fnlStr;
    },
	//OMNI-90928 END	
		//OMNI-92191 - Start
		launchScanPopup: function(event, bEvent, ctrl, args) {
			 var qtyTxt=_scWidgetUtils.getValue(this, "txtScannedQuantity");
			 var shipmentLineDetails = _iasUIUtils.getParentScreen(this, true);
			 var shipmentLineDetailsModel= _scScreenUtils.getModel(shipmentLineDetails, "backroomPickShipmentDetails_output"); 
			 var vPackListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentLineDetailsModel); 
			 if(_scBaseUtils.equals(vPackListType, "FA") && (qtyTxt>'0')){
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
                
                dialogParams["closeCallBackHandler"] = "confirmPopUpCloseOnCloseSelection";
                
                // OMNI-62991- HIP Printer Reprint Changes for STS - End
                              
                dialogParams["class"] = "popupTitleBorder fixedActionBarDialog";
                _iasUIUtils.openSimplePopup("extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberPopUp", "", this, popupParams, dialogParams);
			 }		 
		}, 

		decreaseQuantity: function(event, bEvent, ctrl, args) {
			var qtyTxt=_scWidgetUtils.getValue(this, "txtScannedQuantity");
			var shipmentLineDetails = _iasUIUtils.getParentScreen(this, true);
            var shipmentLineDetailsModel= _scScreenUtils.getModel(shipmentLineDetails, "backroomPickShipmentDetails_output"); 
			var vPackListType=_scModelUtils.getStringValueFromPath("Shipment.PackListType",shipmentLineDetailsModel); 
			if(!_scBaseUtils.equals(vPackListType, "FA")|| (qtyTxt<='0')){
				this.validateAndUpdatePickedQuantity("decrease");
			}	
        },
		confirmPopUpCloseOnCloseSelection: function(event, bEvent, ctrl, args) {             
			 this.isCancelClicked = true;
			_scWidgetUtils.closePopup(this, "Cancel", false);
			 var sSerialNo = _iasContextUtils.getFromContext("SerialNoContext");
			 if(!_scBaseUtils.isVoid(sSerialNo)){
			this.validateAndUpdatePickedQuantity("decrease");
			 }
			
        },	
		//OMNI-92191 - End
		isShipmentLineMarkedPartialShortage: function(shipmentLineModel) {
			var status = false, isPickComplete = false, isLineCompletelyShorted = false;
			if(!_scBaseUtils.isVoid(shipmentLineModel.ShipmentLine.ShortageQty) && (Number(shipmentLineModel.ShipmentLine.ShortageQty) > 0)) {								
				status = true;							
			}
			if(!_scBaseUtils.isVoid(shipmentLineModel.ShipmentLine.BackroomPickComplete) && _scBaseUtils.equals(shipmentLineModel.ShipmentLine.BackroomPickComplete, "Y")) {								
				isPickComplete = true;						
			}
			if(!_scBaseUtils.isVoid(shipmentLineModel.ShipmentLine.Quantity) && _scBaseUtils.numberEquals(Number(shipmentLineModel.ShipmentLine.Quantity), Number("0"))) {								
				isLineCompletelyShorted = true;						
			}
			if(status){
				
				_scWidgetUtils.showWidget(this,"shortagelbl",false,"");
				//scWidgetUtils.showWidget(screen,"shortageResolutionLink",false,"");
				_scWidgetUtils.showWidget(this,"extn_button",false,"");
				
				if(isPickComplete) {
					//scWidgetUtils.hideWidget(screen,"shortageResolutionLink",false);
					_scWidgetUtils.hideWidget(this,"extn_button",false);
				}
			}
        },
		//End - OMNI-3680 BOPIS:Record Shortage Button
	/*This method is to modify the OOB method such that it behaves in the same way and cancels order irrespective of the Reason Code selected while recording shortage */
	 callMultiApiShortageUpdate: function(
        args) {
            var shortedShipmentLineModel = null;
            var shipmentLinePickedModel = null;
            shipmentLinePickedModel = _scScreenUtils.getTargetModel(
            this, "ShipmentLine_Output", null);
            shortedShipmentLineModel = _scBaseUtils.getModelValueFromBean("ShortedShipmentLine", args);
            var updateShortageModel = null;
            updateShortageModel = this.getShortageChangeShipmentModel(shipmentLinePickedModel, args);
            if (
            _scBaseUtils.equals("Y", _scModelUtils.getStringValueFromPath("ShipmentLine.MarkAllShortLineWithShortage", shortedShipmentLineModel))) {
                var shipmentModel = null;
                shipmentModel = {};
                shipmentModel = _scModelUtils.createModelObjectFromKey("Shipment", shipmentModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.Action", "MarkAllLinesShortage", shipmentModel);
                _scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", _scModelUtils.getStringValueFromPath("Shipment.ShipmentKey", updateShortageModel), shipmentModel);
                updateShortageModel = shipmentModel;
            } else {
                _scModelUtils.setStringValueAtModelPath("Shipment.Action", "MarkLineAsShortage", updateShortageModel);
            }
            _scModelUtils.setStringValueAtModelPath("Shipment.ShortageReasonCode", _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageReasonCode", shortedShipmentLineModel), updateShortageModel);
            var eventArgs = null;
            var eventDefn = null;
            eventDefn = {};
            eventArgs = {};
            _scBaseUtils.setAttributeValue("inputData", updateShortageModel, eventArgs);
            _scBaseUtils.setAttributeValue("argumentList", eventArgs, eventDefn);
            _scEventUtils.fireEventToParent(
            this, "updateShipmentLineDetails", eventDefn);
        },
		
		/* This method is also a part where Cancellation happens irrespective of the Reason Code selected while recording shortage */
		getShortageChangeShipmentModel : function(pickedShipmentLineModel,args){
			var pickedModel = {};
			pickedModel.Shipment={};
			pickedModel.Shipment.ShipmentKey=pickedShipmentLineModel.ShipmentLine.ShipmentKey;
			pickedModel.Shipment.ShipmentLines={};
			pickedModel.Shipment.ShipmentLines.ShipmentLine=[];			
							
			var pickedLine = {};
			pickedLine.ShipmentLineKey=pickedShipmentLineModel.ShipmentLine.ShipmentLineKey;
			var pickedQty = !_scBaseUtils.isVoid(pickedShipmentLineModel.ShipmentLine.BackroomPickedQuantity)?Number(pickedShipmentLineModel.ShipmentLine.BackroomPickedQuantity):Number('0');
			var shortageQty = !_scBaseUtils.isVoid(pickedShipmentLineModel.ShipmentLine.ShortageQty)?Number(pickedShipmentLineModel.ShipmentLine.ShortageQty):Number('0');
			pickedLine.BackroomPickedQuantity=pickedQty;	
			if(!_scBaseUtils.isVoid(args) && this.isLineMarkedWithInventoryShortage(args.ShortedShipmentLine)) {
				pickedLine.Quantity=pickedQty;
			} else {
				pickedLine.Quantity=pickedShipmentLineModel.ShipmentLine.Quantity;
			}
			
			pickedLine.ShortageQty=(Number(pickedShipmentLineModel.ShipmentLine.Quantity)-pickedQty) + shortageQty ;
			pickedLine.OldShortageQty= shortageQty ;	
			pickedModel.Shipment.ShipmentLines.ShipmentLine[0]=pickedLine;	

			/*if(args.isLastShortageLineForCancellingShipment=="true" || args.isLastShortageLineForCancellingShipment==true){
				pickedModel.Shipment.Action="Cancel";
			}*/			
			return pickedModel;
		},
		
		/* This method is also a part where Cancellation happens irrespective of the Reason Code selected while recording shortage */
		isLineMarkedWithInventoryShortage : function(shortedShipmentModel){
			
			if(!_scBaseUtils.isVoid(shortedShipmentModel) && !_scBaseUtils.isVoid(shortedShipmentModel.ShipmentLine)) {
				return true;
			}
			
			return false;
			
		},

		displayServiceDetails: function() {
			var sLine = _scScreenUtils.getModel(this, "ShipmentLine");

			var data1 = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ItemDetails.Extn.ExtnStyle", sLine);
			if(_scBaseUtils.isVoid(data1)) {
				data1 = "  ";
			} else {
				data1 += " | "
			}
			var data2 = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ItemDetails.Extn.ExtnSizeCodeDescription", sLine);
			if(_scBaseUtils.isVoid(data2)) {
				data2 = "  ";
			} else {
				data2 += " | "
			}
			var data3 = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ItemDetails.Extn.ExtnVendorColorName", sLine);
			if(_scBaseUtils.isVoid(data3)) {
				data3 = "  ";
			}
			_scWidgetUtils.setValue(this, "extn_datalabel", data1 +  data2 + data3, false);

			data1 = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.ItemDetails.OnhandInventory", sLine);

			_scWidgetUtils.setValue(this, "extn_datalabel2", data1, false);

			data1 = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnLiveDate", sLine);
			if(_scBaseUtils.isVoid(data1)) {
				data1 = "  ";
			} else {
				data1 = data1.substring(0,10);
			}

			_scWidgetUtils.setValue(this, "extn_datalabel3", data1, null);

			data1 = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnPogId", sLine);
			if(_scBaseUtils.isVoid(data1)) {
				data1 = "  ";
			} else {
				data1 += " | "
			}
			data2 = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnDepartment", sLine);
			if(_scBaseUtils.isVoid(data2)) {
				data2 = "  ";
			} else {
				data2 += " | "
			}
			data3 = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnSection", sLine);
			if(_scBaseUtils.isVoid(data3)) {
				data3 = "  ";
			} else {
				data3 += " | "
			}
			var data4 = _scModelUtils.getStringValueFromPath("ShipmentLine.Extn.ExtnPogNumber", sLine);
			if(_scBaseUtils.isVoid(data4)) {
				data4= "  ";
			}
			_scWidgetUtils.setValue(this, "extn_datalabel4",  data1 + data2  + data3 + data4, false);
		},
		// Overr OOTB method to disable onclick event on image
		openItemDetails: function(
        event, bEvent, ctrl, args) {
            // var itemDetailsModel = null;
            // var callingOrgCode = null;
            // itemDetailsModel = _scScreenUtils.getTargetModel(
            // this, "ItemDetails", null);
            // _wscProductUtils.openItemDetails(
            // this, itemDetailsModel);
        },
});
});

