


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackShipmentLineListExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackShipmentLineListExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackShipmentLineListExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackShipmentLineListExtn'

			//OMNI:66083 - START
				 ,
			 mashupRefs : 	[
				{
					 mashupId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
					 mashupRefId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
					 extnType : 			'ADD'
				}
			]
			//OMNI:66083 - END
			
			
}
);
});

