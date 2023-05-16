


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/backroompick/UpdateHoldLocationExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnUpdateHoldLocationExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.backroompick.UpdateHoldLocationExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.backroompick.UpdateHoldLocationExtn',

			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_PrintOrderTicket'
,
		 mashupRefId : 			'extn_PrintOrderTicket_ref'

	}
,
		{
		 mashupId : 			'getPrinterDevice'
,
		 mashupRefId : 			'getPrinterDeviceInit1'
,
		 extnType : 			''

	}
,
	{
		 extnType : 			'ADD'
,
		 mashupId : 			'backroomPickUp_changeShipmentStatusToReadyForCustomerPick'
,
		 mashupRefId : 			'saveShipmentStatusForPickUpOrder'

	}
  	// OMNI- 3676 BOPIS: Apply All Staging Locations START
,
	 		{
		 mashupRefId : 			'extn_AssignStagingLocationMashup'
,
		 mashupId : 			'extn_AssignStagingLocationALLMashup'
,
		 extnType : 			'ADD'

	}
	// OMNI- 3676 BOPIS: Apply All Staging Locations END 
	]

			
			
			
}
);
});

