


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/payment/inquiry/PaymentInquiryExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnPaymentInquiryExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.payment.inquiry.PaymentInquiryExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.payment.inquiry.PaymentInquiryExtn'

			
			
			// OMNI-1923 Customer Care: Sterling WCC: Y order Link - Start 
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			'5'
,
		 mashupId : 			'paymentInquiry-getPaymentInquiryDetails'
,
		 sourceNamespace : 			'paymentInquiry-getPaymentInquiryDetails_Output'
,
		 mashupRefId : 			'getPaymentInquiryDetails'
,
		 extnType : 			''
,
		 callSequence : 			'1'

	}

	]
		// OMNI-1923 Customer Care: Sterling WCC: Y order Link - End 
}
);
});

