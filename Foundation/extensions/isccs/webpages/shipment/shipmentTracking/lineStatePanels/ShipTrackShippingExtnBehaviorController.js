


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/shipment/shipmentTracking/lineStatePanels/ShipTrackShippingExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipTrackShippingExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.shipment.shipmentTracking.lineStatePanels.ShipTrackShippingExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.shipment.shipmentTracking.lineStatePanels.ShipTrackShippingExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'extn_getShipNodeListMashup'
,
		 mashupRefId : 			'extn_getShipNodeListMashup'
,
		 extnType : 			'ADD'

	}

	]

}
);
});

