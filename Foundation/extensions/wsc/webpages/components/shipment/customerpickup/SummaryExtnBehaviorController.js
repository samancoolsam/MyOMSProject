


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/customerpickup/SummaryExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnSummaryExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.customerpickup.SummaryExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.customerpickup.SummaryExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'UnResrvMsgToSIMOnShrtg'
,
		 mashupRefId : 			'extn_UnResrvMsgToSIMOnShrtg'

	}
,
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_changeShipment'
,
		 mashupRefId : 			'extn_changeShipment_ref'

	}
,
	 		{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_stampInvoiceNoOnBOPISOrders'
,
		 mashupRefId : 			'extn_stampInvoiceNoOnBOPISOrders_ref'

	},
	
	{
		 mashupRefId : 			'extn_getUserListByUserID'
,
		 mashupId : 			'extn_getUserListByUserID'
,
		 extnType : 			'ADD'

	}
	,
	{
		 extnType : 			'ADD'
,
		 mashupId : 			'extn_AcademyBOPISPrintAckSlip'
,
		 mashupRefId : 			'extn_AcademyBOPISPrintAckSlip_ref'

	},
	//OMNI -85085- Start
	{
		 mashupId : 			'extn_recordCustomerPickForCurbsideConsolidation'
,
		 mashupRefId : 			'extn_recordCustomerPickForCurbsideConsolidation'
,
		 extnType : 			'ADD'

	}
	//OMNI -85085- End
	]

}
);
});

