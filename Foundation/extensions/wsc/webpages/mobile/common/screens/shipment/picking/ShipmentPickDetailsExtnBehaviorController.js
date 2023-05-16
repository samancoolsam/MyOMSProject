


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/common/screens/shipment/picking/ShipmentPickDetailsExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentPickDetailsExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.mobile.common.screens.shipment.picking.ShipmentPickDetailsExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.mobile.common.screens.shipment.picking.ShipmentPickDetailsExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_getShipmentList'
,
		 mashupRefId : 			'extn_getShipmentList_mobile'

	}
,
// OMNI - 9236 - Ship to Store Order Search "customer pick up" - START
	 		{
		 mashupId : 			'extn_RFCP_getSOShipmentList'
,
		 mashupRefId : 			'extn_getSOShipmentList'
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

	},
	//OMNI-85083 - Start
		{
		mashupId : 				'extn_RecordStoreUserActionConsldCurbside'
,
		mashupRefId :			'extn_RecordStoreUserActionConsldCurbside'
,
		extnType : 				'ADD'
 
		}
	//OMNI-85083 - End
	]

}
);
});

