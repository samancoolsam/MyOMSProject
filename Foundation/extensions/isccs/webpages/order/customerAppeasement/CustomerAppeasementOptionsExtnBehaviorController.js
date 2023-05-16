


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/customerAppeasement/CustomerAppeasementOptionsExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnCustomerAppeasementOptionsExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementOptionsExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.order.customerAppeasement.CustomerAppeasementOptionsExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'getAppeasementOffersUE'
,
		 mashupId : 			'customerAppeasement_getAppeasementOffersUE'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'recordInvoiceCreation'
,
		 mashupId : 			'customerAppeasement_recordInvoiceCreation'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'sendFutureOrderCustomerAppeasementUE'
,
		 mashupId : 			'customerAppeasement_sendFutureOrderCustomerAppeasementUE'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'changeOrder'
,
		 mashupId : 			'customerAppeasement_changeOrder'
,
		 extnType : 			'MODIFY'

	}

	]

}
);
});

