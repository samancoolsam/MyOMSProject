


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackItemScanExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackItemScanExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackItemScanExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackItemScanExtn'	,

			
			//OMNI-69797 START
			 mashupRefs : 	[
	 		{
				 mashupId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
				 mashupRefId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty',
				 extnType : 			'ADD'
			}
			]
			//OMNI-69797 END		
}
);
});

