


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackContainerViewExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackContainerViewExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerViewExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackContainerViewExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_SFSBeforeCreateContainersAndPrint'
,
		 mashupRefId : 			'extn_SFSBeforeCreateContainersAndPrint_ref'

	},
	{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_dropDownValues'
,
		 mashupRefId : 			'extn_dropDownValues_ref'

	},
	{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_ContainerRecommToggle'
,
		 mashupRefId : 			'extn_ContainerRecommToggle_ref'

	}

	]

}
);
});

