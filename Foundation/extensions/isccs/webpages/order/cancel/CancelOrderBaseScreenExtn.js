
scDefine(["scbase/loader!dojo/_base/declare",
"scbase/loader!extn/order/cancel/CancelOrderBaseScreenExtnUI",
"scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils",
"scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!isccs/utils/WidgetUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils",
"scbase/loader!sc/plat/dojo/utils/EditorUtils","scbase/loader!isccs/utils/UIUtils", "scbase/loader!sc/plat/dojo/utils/WizardUtils",  "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/GridxUtils", "scbase/loader!isccs/utils/BaseTemplateUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnCancelOrderBaseScreenExtnUI, _scScreenUtils,_scWidgetUtils,_scModelUtils,
			    _scBaseUtils,_isccsWidgetUtils,_scControllerUtils,_scEditorUtils,_isccsUIUtils,_scWizardUtils, _scEventUtils, _scGridxUtils,  _isccsBaseTemplateUtils
){ 
	return _dojodeclare("extn.order.cancel.CancelOrderBaseScreenExtn", [_extnCancelOrderBaseScreenExtnUI],{
	
	ExtnReasonCodeChange:function(
        event, bEvent, widgetId, args){

        	var getCompleteOrderDetails_output = null;
        getCompleteOrderDetails_output = _scScreenUtils.getModel(this, "getCompleteOrderDetails_output");
		var firstName = _scModelUtils.getStringValueFromPath("Order.CustomerFirstName", getCompleteOrderDetails_output);
        var lastName = _scModelUtils.getStringValueFromPath("Order.CustomerLastName", getCompleteOrderDetails_output);
		var reasonCode = _scWidgetUtils.getValue(this, "cmbReasoncode");
		var getCancelReasonCodes_output = _scScreenUtils.getModel(this, "getCancelReasonCodes_output", null);
		var cancelreasonCodeList = _scModelUtils.getStringValueFromPath("CommonCodeList.CommonCode",getCancelReasonCodes_output);
		//
		var cancelreasonCode= _scModelUtils.getModelFromListByValue(cancelreasonCodeList,"CodeValue",reasonCode);
        var reasonCode = _scModelUtils.getStringValueFromPath("CodeShortDescription",cancelreasonCode);
		//
		var formtString = "";
		formtString = firstName + " " + lastName;
        formtString = _scBaseUtils.stringConcat(formtString, " requested to cancel because of ");
        formtString = _scBaseUtils.stringConcat(formtString, reasonCode);
		
		var widg = this.getWidgetByUId("extn_CancelNotes");
		_scWidgetUtils.setValue(
					this, "extn_CancelNotes", formtString, false);
		_isccsWidgetUtils.resizeWidget("extn_CancelNotes");
		_scModelUtils.setStringValueAtModelPath("Order.Notes.Note.NoteText", formtString, getCompleteOrderDetails_output);

	},

// Start - OMNI- 12711 - eGift Card: Cancellation of eGC item is not allowed from web call center
		callCancelOrder: function(ctrl, args) {
		var actionOnPage = null;
		actionOnPage = _scBaseUtils.getStringValueFromBean("actionOnPage", args);
		var paginationContext = null;
		paginationContext = _scBaseUtils.getAttributeValue("appPaginatedContext", false, args);
		var cancelPropModel = null;
		cancelPropModel = _scScreenUtils.getTargetModel(this, "getCancelProp_input", null);
		// This is added for cancel Exchange Flow
		 sScreenType = _scWizardUtils.getCurrentPageScreenType(_isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor()));
		if(sScreenType == "CancelExchange"){
			 initialInputData = _scScreenUtils.getInitialInputData(this);
			 var exchangeOrderHeaderKey  =  _scModelUtils.getStringValueFromPath("Order.ExchangeOrders.ExchangeOrder.0.OrderHeaderKey", initialInputData);
			 _scBaseUtils.setAttributeValue("OrderHeaderKey", exchangeOrderHeaderKey, cancelPropModel);
		}
		//End of cancel exchange flow
		var cancelType = null;
		cancelType = _scModelUtils.getStringValueFromPath("Order.CancelType", cancelPropModel);
		
		if (_scBaseUtils.equals(cancelType, "01")) {

			this.cancelAllLines(actionOnPage, paginationContext);

		} else if(_scBaseUtils.equals(cancelType, "02")){

			var cancelOrderListScreenObj = null;
			cancelOrderListScreenObj = _scScreenUtils.getChildScreen(this, "cancelOrderListScreen");
			var cancelOrderItems = null;
			cancelOrderItems = _scGridxUtils.getSelectedTargetRecordsUsingUId(cancelOrderListScreenObj, "OLST_listGrid");
			var orderLinesList = null;
			orderLinesList = _scModelUtils.getModelListFromPath("OrderLineList.OrderLine", cancelOrderItems);
            
      var hasEGCLines = false;
			for(var i in orderLinesList)
			{
				var strLineType = _scModelUtils.getStringValueFromPath("LineType",orderLinesList[i]);
				var strFulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType",orderLinesList[i]);
				if(_scBaseUtils.equals(strLineType,"EGC") || _scBaseUtils.equals(strFulfillmentType,"EGC"))
				{
					hasEGCLines = true;
				}
			}
            var cancelOrder = null;
		    cancelOrder = _scScreenUtils.getTargetModel(this, "getOrderModel", null);
		    var strCancellationReasonCode = _scModelUtils.getStringValueFromPath("Order.ModificationReasonCode",cancelOrder);
            var completeOrderLinesList = null;
            if(_scBaseUtils.equals(strCancellationReasonCode,"Fraud"))
            {
              var completeOrderLinesModel =_scScreenUtils.getModel(cancelOrderListScreenObj, "getCompleteOrderLineList_output");
			  completeOrderLinesModel =  _scModelUtils.getModelListFromPath("Page.Output.OrderLineList.OrderLine", completeOrderLinesModel);
			   for(var i in completeOrderLinesModel){
			     var strLineType = _scModelUtils.getStringValueFromPath("LineType",completeOrderLinesModel[i]);
			     var strFulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType",completeOrderLinesModel[i]);
			        if(_scBaseUtils.equals(strLineType,"EGC") || _scBaseUtils.equals(strFulfillmentType,"EGC"))
			        {
					    	hasEGCLines = true;
			        }
		        }
            }
			
			//var cancelOrder = null;
			//cancelOrder = _scScreenUtils.getTargetModel(this, "getOrderModel", null);
			
            if(hasEGCLines){
				//var strReasonCode = _scModelUtils.getStringValueFromPath("Order.ModificationReasonCode",cancelOrder);
				//if(!_scBaseUtils.equals(strReasonCode,"Fraud")){
					var errormsg = null;
					errormsg = _scScreenUtils.getString(this, "extn_RestrictEGCLinesCnclMsg");
					_scScreenUtils.showErrorMessageBox(this, errormsg, null, null, null);
				/*}else{
					if (_scGridxUtils.isGridDirty(this, "", true) || _scBaseUtils.negateBoolean(_scBaseUtils.isVoid(orderLinesList))) {

						if (_scBaseUtils.greaterThan(this.isAnyLineCancelled, 0) && _scBaseUtils.isVoid(orderLinesList)) {
							_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
						}

						var isCancelQtyValid = false;
						isCancelQtyValid = this.validateCancelingQty(cancelOrderItems);
						if (_scBaseUtils.equals(isCancelQtyValid, true)) {
							this.cancelSelectedLines(cancelOrderItems, actionOnPage, paginationContext);
						}
					} else {
						if (_scBaseUtils.equals(actionOnPage, "NEXT")) {
							_isccsBaseTemplateUtils.hideMessage(this);
							if (_scBaseUtils.greaterThan(this.isAnyLineCancelled, 0) && _scBaseUtils.isVoid(orderLinesList)) {
								_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
							} else if (
							_scBaseUtils.greaterThan(this.isAnyLineCancelledInCancelFlow, 0) && _scBaseUtils.isVoid(orderLinesList)) {
								_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
							} else {
								_isccsBaseTemplateUtils.showMessage(this, "cancelOrder_select_products", "error", null);
							}
						} else {
							_isccsBaseTemplateUtils.showMessage(this, "cancelOrder_select_products", "error", null);
						}
					}
				}*/

			}else {
				if (_scGridxUtils.isGridDirty(this, "", true) || _scBaseUtils.negateBoolean(_scBaseUtils.isVoid(orderLinesList))) {

					if (_scBaseUtils.greaterThan(this.isAnyLineCancelled, 0) && _scBaseUtils.isVoid(orderLinesList)) {
						_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
					}

					var isCancelQtyValid = false;
					isCancelQtyValid = this.validateCancelingQty(cancelOrderItems);
					if (_scBaseUtils.equals(isCancelQtyValid, true)) {
						this.cancelSelectedLines(cancelOrderItems, actionOnPage, paginationContext);
					}
				} else {
					if (_scBaseUtils.equals(actionOnPage, "NEXT")) {
						_isccsBaseTemplateUtils.hideMessage(this);
						if (_scBaseUtils.greaterThan(this.isAnyLineCancelled, 0) && _scBaseUtils.isVoid(orderLinesList)) {
							_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
						} else if (
						_scBaseUtils.greaterThan(this.isAnyLineCancelledInCancelFlow, 0) && _scBaseUtils.isVoid(orderLinesList)) {
							_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
						} else {
							_isccsBaseTemplateUtils.showMessage(this, "cancelOrder_select_products", "error", null);
						}
					} else {
						_isccsBaseTemplateUtils.showMessage(this, "cancelOrder_select_products", "error", null);
					}
				}
			}
		}else {
			_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
		}
	},
		
		
	cancelAllLines: function(actionOnPage, paginatedContext) {      	
		var hasEGCLines = false;
		var cancelOrderListScreenObj = null;
		cancelOrderListScreenObj = _scScreenUtils.getChildScreen(this, "cancelOrderListScreen");
		var orderLineListModel =  _scScreenUtils.getModel(cancelOrderListScreenObj, "getCompleteOrderLineList_output");
		var orderlinesModel =  _scModelUtils.getModelListFromPath("Page.Output.OrderLineList.OrderLine", orderLineListModel);
		
		for(var i in orderlinesModel){
			var strLineType = _scModelUtils.getStringValueFromPath("LineType",orderlinesModel[i]);
			var strFulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType",orderlinesModel[i]);
			//var maxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderlinesModel[i]);
			//var formattedValue = maxLineStatus.substr(0,4);
			if(_scBaseUtils.equals(strLineType,"EGC") || _scBaseUtils.equals(strFulfillmentType,"EGC"))
			{
				//if(maxLineStatus != "9000"){
					hasEGCLines = true;
				//}
			}
		}
        
        var cancelOrder = null;
		cancelOrder = _scScreenUtils.getTargetModel(this, "getOrderModel", null);

		if(hasEGCLines){
			var strReasonCode = _scModelUtils.getStringValueFromPath("Order.ModificationReasonCode",cancelOrder);
			if(!_scBaseUtils.equals(strReasonCode,"Fraud")){
				var errormsg = null;
				errormsg = _scScreenUtils.getString(this, "extn_RestrictEGCOrderCnclMsg");
				_scScreenUtils.showErrorMessageBox(this, errormsg, null, null, null);
			}else{
				_scModelUtils.setStringValueAtModelPath("Order.Action", "CANCEL", cancelOrder);
				_scScreenUtils.setModel(this, "cancelOrderReasonCode_input", cancelOrder, null);

				var mashupContext = null;
				mashupContext = _scControllerUtils.getMashupContext(this);
				_scBaseUtils.setAttributeValue("appPaginatedContext", paginatedContext, mashupContext);
				_scControllerUtils.setMashupContextIdenitier(mashupContext, actionOnPage);

				var confirm_msg = null;
				confirm_msg = _scScreenUtils.getString(this, "ConfirmCalcelOrderMsg");
				_scScreenUtils.showConfirmMessageBox(this, confirm_msg, "handleConfirm", null, mashupContext);
			}
		} else {
			_scModelUtils.setStringValueAtModelPath("Order.Action", "CANCEL", cancelOrder);
			_scScreenUtils.setModel(this, "cancelOrderReasonCode_input", cancelOrder, null);

			var mashupContext = null;
			mashupContext = _scControllerUtils.getMashupContext(this);
			_scBaseUtils.setAttributeValue("appPaginatedContext", paginatedContext, mashupContext);
			_scControllerUtils.setMashupContextIdenitier(mashupContext, actionOnPage);

			var confirm_msg = null;
			confirm_msg = _scScreenUtils.getString(this, "ConfirmCalcelOrderMsg");
			_scScreenUtils.showConfirmMessageBox(this, confirm_msg, "handleConfirm", null, mashupContext);
	    }
	},	
	// End - OMNI- 12711 - eGift Card: Cancellation of eGC item is not allowed from web call center
		handleConfirm: function(
        res, mashupContext) {
            if (
            _scBaseUtils.equals(
            res, "Ok")) {
                var cancelOrder = null;
                cancelOrder = _scScreenUtils.getModel(
                this, "cancelOrderReasonCode_input");
                var cancelPropModel = _scScreenUtils.getTargetModel(
				this, "getCancelProp_input", null);
            var noteString = _scModelUtils.getStringValueFromPath("Order.Notes.Note.NoteText", cancelPropModel);
			_scModelUtils.setStringValueAtModelPath("Order.Notes.Note.NoteText", noteString, cancelOrder);		  
                // This is added for cancel Exchange Flow
                sScreenType = _scWizardUtils.getCurrentPageScreenType(_isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor()));
                if (sScreenType == "CancelExchange") {
                    initialInputData = _scScreenUtils.getInitialInputData(this);
                    var exchangeOrderHeaderKey = _scModelUtils.getStringValueFromPath("Order.ExchangeOrders.ExchangeOrder.0.OrderHeaderKey", initialInputData);
                    _scBaseUtils.setAttributeValue("Order.OrderHeaderKey", exchangeOrderHeaderKey, cancelOrder);
                }
                //End of cancel exchange flow
				//Start:OMNI-63312: WCC Cancellations
           		_isccsUIUtils.callApi(
				//this, cancelOrder, "cancelOrderMashupRef", mashupContext);
                this, cancelOrder, "extn_CancelOrderRef", mashupContext);
				//End:OMNI-63312: WCC Cancellations
            }
        },
// OMNI- 8718 Prevent STS Line Cancellation WEB COM - START
     extn_initializeLayout: function(event, bEvent, ctrl, args)
     {
	       var getCompleteOrderDetails_output = null;
         getCompleteOrderDetails_output = _scScreenUtils.getModel(
            this, "getCompleteOrderDetails_output");
         var hasSTSLines = false;
	       var hasOtherLines = false;
	       var cancelEligibleOtherSTSLines = false;
          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup 
	       var hasBOPISLines = false;
	       var cancelOrderListScreenObj = null;
         cancelOrderListScreenObj = _scScreenUtils.getChildScreen(this, "cancelOrderListScreen");
         var orderLineListModel =  _scScreenUtils.getModel(cancelOrderListScreenObj, "getCompleteOrderLineList_output");
         var orderlinesModel =  _scModelUtils.getModelListFromPath("Page.Output.OrderLineList.OrderLine", orderLineListModel);
         for(var i in orderlinesModel)
	       {
          var deliveryMethod = _scModelUtils.getStringValueFromPath("DeliveryMethod",orderlinesModel[i]);
	        var fulfillmentType = _scModelUtils.getStringValueFromPath("FulfillmentType",orderlinesModel[i]);
	        var maxLineStatus = _scModelUtils.getStringValueFromPath("MaxLineStatus", orderlinesModel[i]);
	        var formattedValue = maxLineStatus.substr(0,4);

          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - START
	         if(_scBaseUtils.equals(deliveryMethod,"PICK"))
	         {
	         if(_scBaseUtils.equals(fulfillmentType, "STS")){
           // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - END
      	     if((formattedValue > 1200) && (maxLineStatus != "9000")) 
       	      {
       		       hasSTSLines=true;
       	      }
	         }
          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - START
	         else if(_scBaseUtils.equals(fulfillmentType, "BOPIS"))
	         {
	         if(formattedValue>=3200 && formattedValue<=3350){
                hasBOPISLines = true;
	         	}
	         }

	         }
          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - END
	        else
	         {
	          if(formattedValue>=3350)
	           {
	      	     hasOtherLines=true;	
	           }
	           else{
                cancelEligibleOtherSTSLines = true;
	          }
	        }
         }

	      if(hasSTSLines && (!(cancelEligibleOtherSTSLines|| hasBOPISLines))){
     		        _scWidgetUtils.showWidget(this, "lblnoCancellation", false, null);
                _scWidgetUtils.hideWidget(this, "cmbReasoncode", false);
                _scWidgetUtils.hideWidget(this, "cancelType", false);
                _scWidgetUtils.setWidgetNonMandatory(this,"cmbReasoncode");
                _scWidgetUtils.disableWidget(this,"extn_CancelNotes",false);
		            var sScreenType = _scWizardUtils.getCurrentPageScreenType(_isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor()));
                if (!(sScreenType == "CancelExchange")) {
		              _scEventUtils.fireEventToParent(this, "disableNextButton", null);
	             	}
	         }
          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - START
	        else if(hasBOPISLines && (!(hasOtherLines || cancelEligibleOtherSTSLines|| hasSTSLines)))
            {
            	_scEventUtils.fireEventToParent(
                this, "enableNextButton", null);
                _scWidgetUtils.hideWidget(
                this, "lblnoCancellation", false);
                _scWidgetUtils.showWidget(
                this, "cmbReasoncode", false, null);
                _scWidgetUtils.showWidget(
                this, "cancelType", false, null);
                _scWidgetUtils.setWidgetMandatory(this,"cmbReasoncode");
                var defaultCancelType = "01";
                var cancelRadioModel = null;
                cancelRadioModel = {};
                sScreenType = _scWizardUtils.getCurrentPageScreenType(_isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor()));
                if (sScreenType == "CancelExchange") {
		              _scModelUtils.setStringValueAtModelPath("Order.CancelType", "00", cancelRadioModel);
		              _scWidgetUtils.setWidgetNonMandatory(this,"cmbReasoncode");
			       	} 
                var setModelOptns = null;
                setModelOptns = {};
                _scBaseUtils.setAttributeValue("clearOldVals", false, setModelOptns);
                _scScreenUtils.setModel(this, "getCancelProp_output", cancelRadioModel, setModelOptns);
            }
          // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - END
       /*	 else if(hasSTSLines && hasOtherLines)
        	{
        	       _scWidgetUtils.showWidget(this, "lblnoCancellation", false, null);
                _scWidgetUtils.hideWidget(this, "cmbReasoncode", false);
                _scWidgetUtils.hideWidget(this, "cancelType", false);
                _scWidgetUtils.setWidgetNonMandatory(this,"cmbReasoncode");
                _scWidgetUtils.disableWidget(this,"extn_CancelNotes",false);
	            	var sScreenType = _scWizardUtils.getCurrentPageScreenType(_isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor()));
                if (!(sScreenType == "CancelExchange")) {
		            _scEventUtils.fireEventToParent(this, "disableNextButton", null);
        	      }     
          }*/
           // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - START
            else if ((cancelEligibleOtherSTSLines && hasSTSLines) || (hasBOPISLines && hasOtherLines) || (hasSTSLines && hasBOPISLines)) 
            {
            	    _scEventUtils.fireEventToParent(
                    this, "enableNextButton", null);
                    _scWidgetUtils.hideWidget(
                    this, "lblnoCancellation", false);
                    _scWidgetUtils.showWidget(
                    this, "cmbReasoncode", false, null);
                    _scWidgetUtils.showWidget(
                    this, "cancelType", false, null);
                    _scWidgetUtils.setWidgetMandatory(this,"cmbReasoncode");
              // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - END
            	    _scWidgetUtils.disableOptionsInListWidget(
                    this, "cancelType", "01");
                    var defaultCancelType = "02";
                    var hiddenCancelQtyColn = false;
                    var cancelRadioModel = null;
                    cancelRadioModel = {};
                    _scModelUtils.setStringValueAtModelPath("Order.CancelType", defaultCancelType, cancelRadioModel);
                    var setModelOptns = null;
                    setModelOptns = {};
                    _scBaseUtils.setAttributeValue("clearOldVals", false, setModelOptns);
                    _scScreenUtils.setModel(
                    this, "getCancelProp_output", cancelRadioModel, setModelOptns);
                    var eventDefn = null;
                    var blankModel = null;
                    eventDefn = {};
                    blankModel = {};
                    eventDefn["argumentList"] = blankModel;
                    _scBaseUtils.setAttributeValue("argumentList.showCancelQtyColumn", true, eventDefn);
                    _scWidgetUtils.disableWidget(this, "update_order", false);
                      _scEventUtils.fireEventToChild(this, "cancelOrderListScreen", "showGridWithHiddenCancelQty", eventDefn);
		                 // _scGridxUtils.enableGrid(cancelOrderListScreenObj, "OLST_listGrid");
		                  _scGridxUtils.refreshGridxHeader(cancelOrderListScreenObj, "OLST_listGrid");
            }
	    },
//Start:OMNI-63312: WCC Cancellations		
 extn_handleMashupOutput: function(event, bEvent, ctrl, args) {
 var model = args;
 var mashupArray=_scModelUtils.getStringValueFromPath("mashupArray",args);
	 var mashupContext=_scModelUtils.getStringValueFromPath("mashupContext",args);
 for(var index in mashupArray)
 {
 	var mashupRefId= _scModelUtils.getStringValueFromPath("mashupRefId", mashupArray[index]);
 	if(_scBaseUtils.equals(mashupRefId,"extn_CancelOrderRef"))
 	{
 		
                           var mashupOutput= _scModelUtils.getStringValueFromPath("mashupRefOutput.Order", mashupArray[index]);
 		_scScreenUtils.setModel(this,"cancelOrder_output",mashupOutput,null);
                this.isAnyLineCancelled = this.isAnyLineCancelled + 1;
                this.isAnyLineCancelledInCancelFlow = this.isAnyLineCancelledInCancelFlow + 1;
                this.handleCancelOrder(
                mashupOutput, mashupContext);
 	}
 } 
}
//End:OMNI-63312: WCC Cancellations
  //OMNI- 8718 Prevent STS Line Cancellation WEB COM - END
    
});
});

