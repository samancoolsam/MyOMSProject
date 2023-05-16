
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/order/cancel/CancelOrderListScreenExtnUI","scbase/loader!sc/plat/dojo/utils/GridxUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!isccs/utils/OrderUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnCancelOrderListScreenExtnUI
                         ,
                            _scGridxUtils
			 ,
			    _scModelUtils
			 ,
			    _scScreenUtils
			 ,
			    _isccsOrderUtils
			 ,
			    _scBaseUtils
){ 
	return _dojodeclare("extn.order.cancel.CancelOrderListScreenExtn", [_extnCancelOrderListScreenExtnUI],{
	// custom code here

  extn_cellEdit: function(event, bEvent, ctrl, args)
{  
 var model=args;
 var warningString = "Partial Cancellation of the line is not allowed.";
 var textObj = null;
 textObj = {};
 var textOK = null;
 textOK = _scScreenUtils.getString(this, "OK");
 textObj["OK"] = textOK;
 _scScreenUtils.showErrorMessageBox(this, warningString, "waringCallback", textObj, null);

},
 //OMNI- 8718 Prevent STS Line Cancellation WEB COM - START
isGridRowDisabled: function(rowData, screen) {
  var availableCancelQty = 0;
  var maxLineStatus = rowData.MaxLineStatus;
  availableCancelQty = _isccsOrderUtils.calculateOrder_CancelQuantityWOFormat(
  this.txQtyRuleSetValue, rowData, false);
  var fulfillmentType  = _scModelUtils.getStringValueFromPath("FulfillmentType", rowData);
  var deliveryMethod  = _scModelUtils.getStringValueFromPath("DeliveryMethod", rowData);
  var formattedValue = maxLineStatus.substr(0,4);
  
  // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - START
  if(_scBaseUtils.equals(fulfillmentType, "STS") && (formattedValue > 1200 && formattedValue<3350)){
    return true;
  }
  else if(_scBaseUtils.equals(fulfillmentType, "BOPIS") && (formattedValue >=3350)){
  	 if (availableCancelQty <= 0 || maxLineStatus.indexOf("3700")>=0) {
      return true;
    } 
    else
    {
    	return false;
    }
  }
  // OMNI-12933 Cancel Items at Line Item Level in COM - Pickup - END
   if (availableCancelQty <= 0 || maxLineStatus.indexOf("3350")>=0) {
      return true;
    } else {
        return false;
    }
  }
  //OMNI- 8718 Prevent STS Line Cancellation WEB COM - END
});
});

