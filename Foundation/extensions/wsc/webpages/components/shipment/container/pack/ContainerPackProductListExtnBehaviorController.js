


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackProductListExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackProductListExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackProductListExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackProductListExtn'

			
			
			
			
			
						,

			
			//Start - OMNI-3680 BOPIS:Record Shortage Button
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_recordShortageForPack'
,
		 mashupId : 			'UnResrvMsgToSIMOnShrtg'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_recordShortageForPack_ref'
,
		 mashupId : 			'extn_recordShortageForPack'
,
		 extnType : 			'ADD'

	}
//End - OMNI-3680 BOPIS:Record Shortage Button
	]

}
);
});

