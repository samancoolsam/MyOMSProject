


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackContainerListExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackContainerListExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerListExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackContainerListExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_getContainerDetail'
,
		 mashupRefId : 			'extn_getContainerDetail_ref'

	},
	{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_getAcadDirectPackLookup'
,
		 mashupRefId : 			'extn_getAcadDirectPackLookup_ref'

	}

	]

}
);
});

