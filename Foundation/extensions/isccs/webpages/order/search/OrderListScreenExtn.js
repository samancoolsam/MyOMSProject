
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/order/search/OrderListScreenExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!isccs/utils/ModelUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnOrderListScreenExtnUI
			 ,
			    _scBaseUtils
			 ,
			    _scModelUtils
		         ,
			    _isccsModelUtils
){ 
	return _dojodeclare("extn.order.search.OrderListScreenExtn", [_extnOrderListScreenExtnUI],{
	// custom code here
  //Start - OMNI-3717 Payment Tender Type Search
     extn_BeforeBehaviorMashupCall: function (event, bEvent, ctrl, args)
     {
	
	var model = args;
	var mashupRefs = _scModelUtils.getStringValueFromPath("mashupRefs",args);
	for(var index in mashupRefs)
	{
		var sMashupRefId = _scModelUtils.getStringValueFromPath("mashupRefId",mashupRefs[index]);
		if(_scBaseUtils.equals(sMashupRefId ,"getOrderList"))
		{
			var vMashupInputModel =  _scModelUtils.getStringValueFromPath("mashupInputObject",mashupRefs[index]);
			var sPaymentType= _scModelUtils.getStringValueFromPath("Order.PaymentMethod.PaymentType", vMashupInputModel);
			var sCardValue = _scModelUtils.getStringValueFromPath("Order.PaymentMethod.DisplayCreditCardNo", vMashupInputModel);
			if((!_scBaseUtils.isVoid(sPaymentType)) && (!_scBaseUtils.isVoid(sCardValue)))
			{
				if(_scBaseUtils.equals(sPaymentType,"DisplayCreditCardNo"))
				{
					_scModelUtils.setStringValueAtModelPath("Order.PaymentMethod.DisplayCreditCardNo", sCardValue,vMashupInputModel);
					
				}
				else if (_scBaseUtils.equals(sPaymentType,"PaymentReference2"))
				{
					_scModelUtils.setStringValueAtModelPath("Order.PaymentMethod.PaymentReference2", sCardValue,vMashupInputModel);
					_isccsModelUtils.removeAttributeFromModel("Order.PaymentMethod.DisplayCreditCardNo",vMashupInputModel);
				}
				else if (_scBaseUtils.equals(sPaymentType,"SvcNo"))
				{
					_scModelUtils.setStringValueAtModelPath("Order.PaymentMethod.SvcNo", sCardValue,vMashupInputModel);
					_isccsModelUtils.removeAttributeFromModel("Order.PaymentMethod.DisplayCreditCardNo",vMashupInputModel);
				}
			}
			_isccsModelUtils.removeAttributeFromModel("Order.PaymentMethod.PaymentType",vMashupInputModel);
		}
	}
 }
//End - OMNI-3717 Payment Tender Type Search
});
});

