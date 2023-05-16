


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/home/subscreens/PickupInStoreExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnPickupInStoreExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.mobile.home.subscreens.PickupInStoreExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.mobile.home.subscreens.PickupInStoreExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'pickupInStore_getShipmentList'
,
		 mashupRefId : 			'getShipmentListBehavior'
,
		 extnType : 			'MODIFY'

	}

	]

}
);
});

