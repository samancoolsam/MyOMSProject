


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/search/ShipmentDetailsExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentDetailsExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.search.ShipmentDetailsExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.search.ShipmentDetailsExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'extn_PrintShipmentPickTickets'
,
		 mashupRefId : 			'extn_PrintShipmentPickTicketsSearch_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_getShipmentList'
,
		 mashupRefId : 			'extn_getShipmentList_ref'
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

	]

			
			
			
}
);
});

