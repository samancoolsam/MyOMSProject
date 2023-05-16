


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/search/ShipmentSearchExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentSearchExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.search.ShipmentSearchExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.search.ShipmentSearchExtn'

			,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_getUserIDFromUsername'
,
		 mashupRefId : 			'extn_getUserIDFromUsername_ref'

	}

	]
			
			
}
);
});

