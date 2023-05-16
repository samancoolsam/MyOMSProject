


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/container/pack/ContainerPackExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnContainerPackExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.container.pack.ContainerPackExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.container.pack.ContainerPackExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_BeforeChangeShipmentOnFinishPack_ref'
,
		 mashupId : 			'extn_BeforeChangeShipmentOnFinishPack'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_stampInvoiceNoOnBOPISOrders_ref'
,
		 mashupId : 			'extn_stampInvoiceNoOnBOPISOrders'
,
		 extnType : 			'ADD'

	}
,
		//BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
	 	/*	{
		 mashupRefId : 			'extn_FinishPackCustomized'
,
		 mashupId : 			'extn_FinishPackCustomized'
,
		 extnType : 			'ADD'

	}
,
		
	 		{
		 mashupRefId : 			'extn_containerPack_changeShipmentForWeight_Ref'
,
		 mashupId : 			'containerPack_changeShipmentForWeight'
,
		 extnType : 			'ADD'

	}*/
		
		{
		 mashupRefId : 			'extn_AcademyUpdateWeightAndFinishPack_Ref'
	,
		 mashupId : 			'extn_AcademyUpdateWeightAndFinishPack'
	,
		 extnType : 			'ADD'

	}
		
		//BOPIS-1576: Remove manual "Enter" hit after container weight input - end

	]

}
);
});

