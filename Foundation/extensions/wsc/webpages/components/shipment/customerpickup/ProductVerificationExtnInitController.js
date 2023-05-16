


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/customerpickup/ProductVerificationExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnProductVerificationExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.shipment.customerpickup.ProductVerificationExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.shipment.customerpickup.ProductVerificationExtn'

			
			
			
			
			
						,
  //Combining Customer Pickup screen OMNI-8660 -Start
			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'customerpickup_getBasicShipmentDetails'
,
		 sourceNamespace : 			'ShipmentDetails'
,
		 mashupRefId : 			'getShipmentDetails'
,
		 extnType : 			''
,
		 callSequence : 			''

	}
,
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'customerpickup_getCustomerVerficationMethodList'
,
		 sourceNamespace : 			'extn_CustomerVerficationMethodList'
,
		 mashupRefId : 			'extn_CustomerVerifcationMethodList'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''

	}
  //OMNI-79056- Curbside Estimated Time Delay Changes-START
  ,
{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'extn_CurbsideEstimatedTimeDelay'
,
		 sourceNamespace : 			'extn_CurbsideCommomCodeList'
,
		 mashupRefId : 			'extn_CurbsideEstimatedTimeDelay_ref'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''
	}	
  //OMNI-79056- Curbside Estimated Time Delay Changes-END
  //Combining Customer Pickup screen OMNI-8660 -End
,
//OMNI-85179 - Start
{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'extn_getCurbsideGetShipmentLine'
,
		 sourceNamespace : 			'extn_curbside'
,
		 mashupRefId : 			'extn_getCurbsideGetShipmentLine'
,
		 extnType : 			'ADD'
,
		 callSequence : 			''

	}
//OMNI-85179 - End
	]
}
);
});

