


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/order/customerAppeasement/CustomerAppeasementSelectionExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnCustomerAppeasementSelectionExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementSelectionExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.order.customerAppeasement.CustomerAppeasementSelectionExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'customerAppeasement_getReasonCodes'
,
		 sourceNamespace : 			''
,
		 mashupRefId : 			'getCustomerAppeasementReasonCodes'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}
,
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'extn_getAppeasementsOnOrderMashup'
,
		 sourceNamespace : 			'extn_getExistingAppeasementList_output'
,
		 mashupRefId : 			'extn_getAppeasementsOnOrderInitMashup'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''

	}

	]

}
);
});

