


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/home/subscreens/PickupInStoreExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnPickupInStoreExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.mobile.home.subscreens.PickupInStoreExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.mobile.home.subscreens.PickupInStoreExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'pickupInStore_getShipmentListInit'
,
		 sourceNamespace : 			''
,
		 mashupRefId : 			'getShipmentList'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

