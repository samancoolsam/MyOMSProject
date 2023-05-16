


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/common/screens/ShipmentLineDetailsExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentLineDetailsExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.common.screens.ShipmentLineDetailsExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.common.screens.ShipmentLineDetailsExtn'
			 //OMNI:66083 - START
			 	,
			 mashupRefs : 	[
				 {
					 mashupId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty' ,
					 mashupRefId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty' ,
					 extnType : 			'ADD'
				 }
			]
			//OMNI:66083 - END
			
			
}
);
});

