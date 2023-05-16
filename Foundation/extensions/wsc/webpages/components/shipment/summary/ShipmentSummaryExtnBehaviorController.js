


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/summary/ShipmentSummaryExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentSummaryExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.summary.ShipmentSummaryExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'getShipmentLineList'
,
		 mashupId : 			'shipSummary_getShipmentLineList'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'extn_CancelShipmentOtherStoreMashupSummary'
,
		 mashupId : 			'extn_CancelShipmentOtherStoreMashup'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_getShipmentDetailsAfterCancellation'
,
		 mashupId : 			'shipSummary_getShipmentDetails'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_getUserListByUserID'
,
		 mashupRefId : 			'extn_getUserListByUserID'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_RecordStoreUserActionMashup'
,
		 mashupRefId : 			'extn_RecordStoreUserActionMashup'
,
		 extnType : 			'ADD'

	}
,
<!--OMNI-72013 Begin-->
{
		 mashupId : 			'extn_CompleteOnMyWayOrder'
,
		 mashupRefId : 			'extn_CompleteOnMyWayOrder'
,
		 extnType : 			'ADD'

	}
<!--OMNI-72013 End-->
,
	//OMNI-85083 - Start
		{
		mashupId : 				'extn_RecordStoreUserActionConsldCurbside'
,
		mashupRefId :			'extn_RecordStoreUserActionConsldCurbside'
,
		extnType : 				'ADD'
 
		}
	//OMNI-85083 - End

	,
	//OMNI-96066 START 
		{
		mashupId : 				'extn_CompleteAssemblyChangeShipment'
,
		mashupRefId :			'extn_CompleteAssemblyChangeShipment_Ref'
,
		extnType : 				'ADD'
 
		},
	// OMNI-96066 END 
//OMNI-99079 Start
	{
		mashupId : 			'extn_getCommonCodeListForProductRegistration'
,
		 mashupRefId : 			'extn_getCommonCodeListForProductRegistration_ref'
,
		 extnType : 			'ADD'
     }
	//OMNI-99079 End

	]

}
);
});

