


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/customerpickup/CustomerIdentificationExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnCustomerIdentificationExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.customerpickup.CustomerIdentificationExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.customerpickup.CustomerIdentificationExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_getUserListByUserID'
,
		 mashupId : 			'extn_getUserListByUserID'
,
		 extnType : 			'ADD'

	}
,
// START - (OMNI - 1434)  : BOPIS Page Tagging/Reporting
  {
		 mashupRefId : 			'extn_RecordStoreUserActionMashup'
,
		 mashupId : 			'extn_RecordStoreUserActionMashup'
,
		 extnType : 			'ADD'

	}
// END - (OMNI - 1434)  : BOPIS Page Tagging/Reporting
	]

}
);
});

