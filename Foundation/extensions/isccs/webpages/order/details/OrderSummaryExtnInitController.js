


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/details/OrderSummaryExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnOrderSummaryExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.order.details.OrderSummaryExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.order.details.OrderSummaryExtn'

			
			
			
<!--OMNI-63466 BEGIN-->			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'extn_getCancelReasons'
,
		 sourceNamespace : 			'extn_CnclReason_output'
,
		 mashupRefId : 			'extn_getCancelReasons'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''

	}

	]
<!--OMNI-63466 END-->
}
);
});

