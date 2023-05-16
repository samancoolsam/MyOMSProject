


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/search/OrderListScreenExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnOrderListScreenExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.order.search.OrderListScreenExtnBehaviorController", 
				[_scExtnServerDataController], {

			//Start - OMNI-3717 Payment Tender Type Search
			 screenId : 			'extn.order.search.OrderListScreenExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'getOrderList'
,
		 mashupId : 			'orderList_getOrderList'
,
		 extnType : 			''

	}

	]

}
//End - OMNI-3717 Payment Tender Type Search
);
});

