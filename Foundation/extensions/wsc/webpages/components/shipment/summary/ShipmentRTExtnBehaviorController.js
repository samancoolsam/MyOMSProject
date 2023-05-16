


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/summary/ShipmentRTExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnShipmentRTExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.summary.ShipmentRTExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.summary.ShipmentRTExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'getShipmentDetails'
,
		 mashupId : 			'shipmentRT_getShipmentDetails'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'containerPack_StorePackSlip_94'
,
		 mashupId : 			'containerPack_StorePackSlip_94'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintOrderTicketSummary_ref'
,
		 mashupId : 			'extn_PrintOrderTicket'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintShipmentPickTickets_ref'
,
		 mashupId : 			'extn_PrintShipmentPickTickets'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintShippingLabelSummary_ref'
,
		 mashupId : 			'extn_PrintShippingLabel'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintReturnLabelSummary_ref'
,
		 mashupId : 			'extn_PrintReturnLabel'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintORMDLabelSummary_ref'
,
		 mashupId : 			'extn_PrintORMDLabel'
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
  ,
	 		{
		 mashupId : 			'getPrinterDevice'
,
		 mashupRefId : 			'extn_getPrinterDeviceInitMashup'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_RecordStoreUserActionMashup'
,
		 mashupRefId : 			'extn_RecordStoreUserActionMashup'
,
		 extnType : 			'ADD'

}
,
           {
		 mashupId : 			'extn_STS_RePrintShippingLabel'
,
		 mashupRefId : 			'extn_STS_RePrintShippingLabelSummary_ref'
,
		 extnType : 			'ADD'

}

	]

}
);
});

