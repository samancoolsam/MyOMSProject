


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/cancel/CancelOrderBaseScreenExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnCancelOrderBaseScreenExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.order.cancel.CancelOrderBaseScreenExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.order.cancel.CancelOrderBaseScreenExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'cancelOrder_cancelOrderAPI'
,
//Start:OMNI-63312: WCC Cancellations
		 mashupRefId : 			'cancelOrderMashupRef'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupId : 			'extn_CancelOrder'
,
		 mashupRefId : 			'extn_CancelOrderRef'
,
		 extnType : 			'ADD'

	}
//End:OMNI-63312: WCC Cancellations

	]

}
);
});

