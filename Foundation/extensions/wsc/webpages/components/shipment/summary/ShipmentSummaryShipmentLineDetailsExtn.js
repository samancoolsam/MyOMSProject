scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/summary/ShipmentSummaryShipmentLineDetailsExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!wsc/components/shipment/common/utils/ShipmentUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils",
"scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/BaseTemplateUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnShipmentSummaryShipmentLineDetailsExtnUI
			,
				_scScreenUtils
			,
				_scWidgetUtils
			, 
				_scModelUtils
			,
				_wscShipmentUtils
			,
			_scBaseUtils,
			_iasUIUtils,
			_iasBaseTemplateUtils
			
){ 
	return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryShipmentLineDetailsExtn", [_extnShipmentSummaryShipmentLineDetailsExtnUI],{
	// custom code here
	
	/*This method is to show the cancellaion reason selected inn the previous sreens if any while recording Shortage */
	/*This is a new method */

	initializeScreen: function(
    event, bEvent, ctrl, args) {
        this.showItemVariationCP();
        var sDetails = _scScreenUtils.getModel(this, "parentShipmentModel");

        var deliveryMethod = _scModelUtils.getStringValueFromPath("Shipment.DeliveryMethod", sDetails);

        if(_scBaseUtils.equals(deliveryMethod, "SHP")) {
        	_scWidgetUtils.hideWidget(this, "extn_datalabel");
        }
		//<!-- OMNI-71678 - Editable Staging Location - Start -->
		shpLineSrcModel = _scScreenUtils.getModel(
            this, "shipmentLine_Src");


     //***OMNI-105507 START
    var ASFlag=_scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.Extn.ExtnIsASP",shpLineSrcModel);
	var ASShipNode=_scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.Order.0.Extn.ExtnASShipNode",shpLineSrcModel);
	var shipNode=_scModelUtils.getStringValueFromPath("ShipmentLine.Shipment.0.ShipNode",shpLineSrcModel);
	var AddressLine1=_scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.PersonInfoShipTo.AddressLine1",shpLineSrcModel);
	var SOShpLineStatus = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.Status", shpLineSrcModel);
	//var currentStore= _iasContextUtils.getFromContext("CurrentStore");
    if((!_scBaseUtils.isVoid(ASFlag) && !_scBaseUtils.isVoid(ASShipNode)) && (_scBaseUtils.equals(ASFlag, "Y")) && 
    	(_scBaseUtils.equals(SOShpLineStatus, "Ready For Customer Pick Up")) && (!_scBaseUtils.equals(shipNode, ASShipNode)) &&  (!_scBaseUtils.isVoid(AddressLine1))){
              _scWidgetUtils.showWidget(this, "extn_AlternateStore");
		      _scWidgetUtils.setValue(this, "extn_AlternateStoreAddress", AddressLine1, false);
			  _scWidgetUtils.showWidget(this, "extn_AlternateStoreAddress");
              
     }
     //***OMNI-105507 END
        var SOShpStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", sDetails);
		var SOShpLineStatus = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.Status", shpLineSrcModel);
		var SOShpLineFulfillmentType = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.FulfillmentType", shpLineSrcModel);
		_scWidgetUtils.hideWidget(this, "extn_btnEdit", false);
		_scWidgetUtils.hideWidget(this, "extn_btnUpdate", false);
		_scWidgetUtils.hideWidget(this, "extn_txtStagingLoc", false);
		if (_scBaseUtils.equals(deliveryMethod, "PICK") && !_scBaseUtils.equals(SOShpLineStatus, "Cancelled") && _scBaseUtils.equals(SOShpLineFulfillmentType, "BOPIS") && _scBaseUtils.equals(SOShpStatus, "1100.70.06.30.5")) {
			var getCommonCodeInput = {};				
			getCommonCodeInput = _scModelUtils.createModelObjectFromKey("CommonCode", getCommonCodeInput);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeType", "TGL_RCP_WEB_SOM_UI" , getCommonCodeInput);
			_scModelUtils.setStringValueAtModelPath("CommonCode.CodeValue", "EDIT_STAGING_LOC_WEBSOM" , getCommonCodeInput);
			_iasUIUtils.callApi(this,getCommonCodeInput, "extn_getFlagEditStagingLocation", null);	
		}
		//<!-- OMNI-71678 - Editable Staging Location - Start -->
    },

    getQuantity: function(
    dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
        // var shipmentModel = null;
        // shipmentModel = _scScreenUtils.getModel(
        // this, "parentShipmentModel");
        // var shipmentStatus = null;
        // shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
        var retValue = null;
        // if (
        // _scBaseUtils.contains(shipmentStatus, "1400")) {
            retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.Quantity", shipmentLineModel);
            retValue = _wscShipmentUtils.getFormattedDisplayQuantity(
            retValue, this, widget, nameSpace, shipmentLineModel, options);
            return retValue;
        // } else {
        //     retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.OriginalQuantity", shipmentLineModel);
        //     retValue = _wscShipmentUtils.getFormattedDisplayQuantity(
        //     retValue, this, widget, nameSpace, shipmentLineModel, options);
        //     return retValue;
        // }
        },

    getOriginalQuantity: function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
        var retValue = null;
        retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.OriginalQuantity", shipmentLineModel);
        retValue = _wscShipmentUtils.getFormattedDisplayQuantity(
        retValue, this, widget, nameSpace, shipmentLineModel, options);
        return retValue;
    },

	getCancelReason: function(
        dataValue, screen, widget, nameSpace, shipmentLineModel, options)
		{
			var shipmentModel = null;
            shipmentModel = _scScreenUtils.getModel(
            this, "parentShipmentModel");
			var shpLineSrcModel = null;
            shpLineSrcModel = _scScreenUtils.getModel(
            this, "shipmentLine_Src");
			var shipmentLineKeySrc = shpLineSrcModel.ShipmentLine.ShipmentLineKey;
			var shortageQty = shpLineSrcModel.ShipmentLine.ShortageQty;
			var shipmentLines = shipmentModel.Shipment.ShipmentLines.ShipmentLine;
			if(shortageQty>=1)
			{
				for(var shipmentLine in shipmentLines)
				{
					var shipmentLineKey = shipmentLines[shipmentLine].ShipmentLineKey;
					if(shipmentLineKey==shipmentLineKeySrc)
					{
						var notes = shipmentLines[shipmentLine].OrderLine.Notes;
						var numberOfNotes = notes.NumberOfNotes;
						if(!_scBaseUtils.equals(numberOfNotes, "0"))
						{
							var note = notes.Note;
							var notesLength = note.length;
							var noteText = note[notesLength-1].NoteText;
							var noteTextSplit = noteText.split(":");
							if (noteTextSplit.length == 1) {
								noteTextSplit = noteTextSplit[0];
							}
							else {
								noteTextSplit = noteTextSplit[1];
							}
							_scWidgetUtils.showWidget(this, "extn_CancelReason", false, "");
							return noteTextSplit;
						}
					}
				}
			}
			
			
		},
		getShortQtyValue: function(
        dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
            var retValue = null;
            var shipmentModel = null;
            shipmentModel = _scScreenUtils.getModel(
            this, "parentShipmentModel");
            /* BOPIS-188: removing status condition to display the Shorted quantity : start
             var shipmentStatus = null;
             shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
             if (
             _scBaseUtils.contains(shipmentStatus, "1400")) {
                _scWidgetUtils.hideWidget(
                 this, "lbl_ShortedQty", true);
             } else {
                 retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageQty", shipmentLineModel);
                 retValue = _wscShipmentUtils.showFormattedDisplayQuantityIfGreaterThanZero(
                 retValue, this, widget, nameSpace, shipmentLineModel, options);
                 return retValue;
             } */
            retValue = _scModelUtils.getStringValueFromPath("ShipmentLine.ShortageQty", shipmentLineModel);
            if(retValue>=1){
            	_scWidgetUtils.showWidget(this, "lbl_ShortedQty", false, "");
            }
            retValue = _wscShipmentUtils.showFormattedDisplayQuantityIfGreaterThanZero(
            retValue, this, widget, nameSpace, shipmentLineModel, options);
            return retValue;
            // BOPIS-188: removing status condition to display the Shorted quantity: end

        },
		
		/* This OOB method has been overridden to rectify an OOB code error */
		getPickedQtyValue: function(
        dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
            var retValue = null;
            var shipmentModel = null;
            shipmentModel = _scScreenUtils.getModel(
            this, "parentShipmentModel");
            var shipmentStatus = null;
            shipmentStatus = _scModelUtils.getStringValueFromPath("Shipment.Status.Status", shipmentModel);
            var backroomPickInProgressStatus = "1100.70.06.20";
            var readyForPackingStatus = "1100.70.06.50";
			/* The below line of OOB code is commented to declare the correct value for the status - BOPIS 173 	
			 BOPIS 173 has been resolved here 
			var readyForCustomerPick = "1100.70.06.30";
			
			-- End */
            var readyForCustomerPick = "1100.70.06.30.5";
			var assemblyInProgress = "1100.70.06.10.5";//OMNI-95876
            if (
            _scBaseUtils.or(
            _scBaseUtils.contains(
            shipmentStatus, backroomPickInProgressStatus), _scBaseUtils.contains(
            shipmentStatus, readyForCustomerPick)) || _scBaseUtils.contains(
            shipmentStatus, readyForPackingStatus) || _scBaseUtils.contains(
            shipmentStatus, assemblyInProgress)) {
                retValue = _wscShipmentUtils.showFormattedDisplayQuantityIfGreaterThanZero(
                dataValue, this, widget, nameSpace, shipmentLineModel, options);
                return retValue;
            } else {
                _scWidgetUtils.hideWidget(
               this, "lbl_PickedQty", true);
            }
        },
		
		/* This OOB method has been overridden to nullify the on click event of image link in products tab */
		openItemDetails: function(
        event, bEvent, ctrl, args) {
            // do nothing
        },
	       
        /*This method is to check if there are any BOPIS Save The Sales Line present and if yes, to display the label "Save The Sale" */
        isSaveTheSalesLine:function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
          var isSaveTheSale = null;
          isSaveTheSale = _scModelUtils.getStringValueFromPath("ShipmentLine.OrderLine.Extn.ExtnOriginalFulfillmentType", shipmentLineModel);
          if(!_scBaseUtils.isVoid(isSaveTheSale) && _scBaseUtils.equals(isSaveTheSale, "BOPIS")) {
           	    _scWidgetUtils.showWidget(this, "extn_save_the_sale_label");
          }
  },
  
  		//<!-- OMNI-71678 - Editable Staging Location - Start -->
  		enableEditStagingLoc:function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
  			_scWidgetUtils.enableWidget(this, "extn_btnUpdate", false);		
			_scWidgetUtils.enableWidget(this, "extn_txtStagingLoc", false);
  		},
  		
  		assignStagingLocation:function(dataValue, screen, widget, nameSpace, shipmentLineModel, options) {
  			shipmentModel = _scScreenUtils.getModel(
            this, "parentShipmentModel");
			var shpLineSrcModel = null;
            shipmentLineModel = _scScreenUtils.getModel(
            this, "shipmentLine_Src");
			var sShipmentNo=_scModelUtils.getStringValueFromPath("Shipment.ShipmentKey",shipmentModel);
			var existingStagingLoc = _scWidgetUtils.getValue(this,"extn_txtStagingLoc");
			var lKey =  _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey",shipmentLineModel);
			if(/[^a-zA-Z0-9\-\/]/.test(existingStagingLoc)){
				_iasBaseTemplateUtils.showMessage(this, "Staging location value allow only AlphaNumeric values, not special characters pattern=^[A-Za-z0-9[-~_\s.!,/+=()\"@\\:;&\*\[\]\u007F-\uFFFF]] Input="+existingStagingLoc, "error", null);
			}else{
				if(!_scBaseUtils.isVoid(existingStagingLoc)){
			var inputToMashup = _scModelUtils.createNewModelObjectWithRootKey("Shipment");
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentKey", sShipmentNo ,inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine", {}, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.ShipmentLineKey", lKey, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn", {}, inputToMashup);
				_scModelUtils.setStringValueAtModelPath("Shipment.ShipmentLines.ShipmentLine.Extn.ExtnStagingLocation", existingStagingLoc, inputToMashup);
				_iasUIUtils.callApi(this, inputToMashup, "saveHoldLocation_ref", null);
				}else{
					_iasBaseTemplateUtils.showMessage(this, "Please enter valid stage location", "error", null);
				}
				}
  		},
  		
  		handleMashupCompletion:function(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _iasBaseTemplateUtils.handleMashupCompletion(mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
            for (var i in mashupRefList) {
                var mashupRefId = _scModelUtils.getStringValueFromPath("mashupRefId", mashupRefList[i]);
                if(_scBaseUtils.equals(mashupRefId,"extn_getFlagEditStagingLocation")){
					 var commonCodeModel = mashupRefList[i].mashupRefOutput;
					 if (typeof commonCodeModel.CommonCodeList.CommonCode != 'undefined' && !(_scBaseUtils.isVoid(commonCodeModel.CommonCodeList.CommonCode[0]))) {
					 	 var enableEditLoc = commonCodeModel.CommonCodeList.CommonCode[0].CodeShortDescription;
						 if(!(_scBaseUtils.isVoid(enableEditLoc)) && (_scBaseUtils.equals(enableEditLoc,"Y"))){
						 	_scWidgetUtils.hideWidget(this, "extn_datalabel", false);
						 	_scWidgetUtils.showWidget(this, "extn_txtStagingLoc", false);
						 	_scWidgetUtils.showWidget(this, "extn_btnEdit", false);
							_scWidgetUtils.showWidget(this, "extn_btnUpdate", false);
							_scWidgetUtils.disableWidget(this, "extn_btnUpdate", false);		
							_scWidgetUtils.disableWidget(this, "extn_txtStagingLoc", false);
						 }
					}
				} if(_scBaseUtils.equals(mashupRefId, "saveHoldLocation_ref")) {
            		var mashupOutputObject = mashupRefList[i].mashupRefOutput;
                	var output = _scModelUtils.getModelObjectFromPath("Shipment" ,mashupOutputObject);
                	var sLine = _scModelUtils.getStringValueFromPath("ShipmentLines.ShipmentLine", output);
                	var outputLength = sLine.length;
					shipmentLineModel = _scScreenUtils.getModel(this, "shipmentLine_Src");
					var lKey =  _scModelUtils.getStringValueFromPath("ShipmentLine.ShipmentLineKey",shipmentLineModel);
                	for(var i=0; i < outputLength; i++) {
                    	var Shipment = sLine[i];
                    	var sLineKey = _scModelUtils.getStringValueFromPath("ShipmentLineKey", Shipment);
                    	if(_scBaseUtils.equals(lKey, sLineKey)) {
                    		var HoldLocation = _scModelUtils.getStringValueFromPath("Extn.ExtnStagingLocation", Shipment);
                    		_scWidgetUtils.setValue(this, "extn_datalabel", HoldLocation, false);
							_scWidgetUtils.hideWidget(this, "extn_datalabel", false);
						 	_scWidgetUtils.showWidget(this, "extn_txtStagingLoc", false);
						 	_scWidgetUtils.showWidget(this, "extn_btnEdit", false);
							_scWidgetUtils.showWidget(this, "extn_btnUpdate", false);
							_scWidgetUtils.disableWidget(this, "extn_btnUpdate", false);		
							_scWidgetUtils.disableWidget(this, "extn_txtStagingLoc", false);
                    		break;
                    	}
            		}
            		this.ownerScreen.isDirtyCheckRequired = false;
            	  
        		}
					 
            }
       }
       //<!-- OMNI-71678 - Editable Staging Location - END -->
});
});
