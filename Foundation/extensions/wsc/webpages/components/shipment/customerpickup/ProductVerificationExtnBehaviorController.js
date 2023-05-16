


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/shipment/customerpickup/ProductVerificationExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnProductVerificationExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.shipment.customerpickup.ProductVerificationExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.shipment.customerpickup.ProductVerificationExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupId : 			'Extn_startPaperWork'
,
		 mashupRefId : 			'extn_startPaperWork_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_resetCurbsideOrders'
,
		 mashupRefId : 			'extn_resetCurbsideOrders_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'deleteNotification'
,
		 mashupRefId : 			'deleteNotification_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_AgeVerifiedChangeShipment'
,
		 mashupRefId : 			'extn_AgeVerifiedChangeShipment_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'customerpickup_getBasicShipmentDetails'
,
		 mashupRefId : 			'getShipmentDetails'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_customerpickup_updateShipmentQuantity'
,
		 mashupRefId : 			'extn_customerpickup_updateShipmentQuantity_Ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'customerpickup_recordCustomerPickup'
,
		 mashupRefId : 			'extn_customerpickup_recordCustomerPickup_Ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'UnResrvMsgToSIMOnShrtg'
,
		 mashupRefId : 			'extn_UnResrvMsgToSIMOnShrtg'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_changeShipment'
,
		 mashupRefId : 			'extn_changeShipment_ref'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupId : 			'extn_stampInvoiceNoOnBOPISOrders'
,
		 mashupRefId : 			'extn_stampInvoiceNoOnBOPISOrders_ref'
,
		 extnType : 			'ADD'

	}
	//OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  START
	,
	 		{
		 mashupId : 			'extn_NotifyRelatedOrders'
,
		 mashupRefId : 			'extn_NotifyRelatedOrders'
,
		 extnType : 			'ADD'

	}
	//OMNI-72389 Curbside customer pick up screen UI – Notification of related orders  END
//OMNI-79563-Curbside Estimated Delay Timer Count Changes - START	
,
{
		 mashupId : 			'extn_CurbsideExtensions'
,
		 mashupRefId : 			'extn_CurbsideExtensions_ref'
,
		 extnType : 			'ADD'
	},
//OMNI-79563-Curbside Estimated Delay Timer Count Changes - END	
//OMNI -80092 - Assign Curbside Order -Start
	{
		 mashupId : 			'extn_CurbsideChangeShipmentForAssignee'
,
		 mashupRefId : 			'extn_CurbsideChangeShipmentForAssignee_ref'
,
		 extnType : 			'ADD'

	}
	//OMNI -80092 - Assign Curbside Order -End
,
	//OMNI -85081 - Start
	{
		 mashupId : 			'extn_customerpickup_getPickupShipmentLineList'
,
		 mashupRefId : 			'getShipmentLineListBehv'
,
		 extnType : 			'MODIFY'

	},
	{
		 mashupId : 			'extn_updateShipmentlinesForCurbConsolidation'
,
		 mashupRefId : 			'extn_customerpickup_updateShipmentLinesForCurbConsolidation_ref'
,
		 extnType : 			'ADD'

	},
		//OMNI -85081 - End
		//OMNI -85179 - complete order changes START
	{
		 mashupId : 			'extn_recordCustomerPickForCurbsideConsolidation'
,
		 mashupRefId : 			'extn_recordCustomerPickForCurbsideConsolidation'
,
		 extnType : 			'ADD'

	},
	//OMNI -85179 - complete order changes END
	//OMNI -85085- Start

    {
         mashupId :             'extn_changeShipmentForCurbsideConsoldiation'
,
         mashupRefId :          'extn_changeShipmentForCurbsideConsoldiation'
,
         extnType :             'ADD'

    },
  //OMNI -85085- End
  {
         mashupId :             'extn_resetInstoreOrders'
,
         mashupRefId :          'extn_resetInstoreOrders_ref'
,
         extnType :             'ADD'

    }
	]

}
);
});

