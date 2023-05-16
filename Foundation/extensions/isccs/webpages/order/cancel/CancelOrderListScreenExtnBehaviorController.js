


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/cancel/CancelOrderListScreenExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnCancelOrderListScreenExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.order.cancel.CancelOrderListScreenExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.order.cancel.CancelOrderListScreenExtn'

			
			
			
// OMNI- 8718 Prevent STS Line Cancellation WEB COM - START			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'CancelOrderListScreen_getCompleteOrderLineList'
,
		 mashupRefId : 			'getCompleteOrderLineList'
,
		 extnType : 			'MODIFY'

	}

	]

}
);
});

