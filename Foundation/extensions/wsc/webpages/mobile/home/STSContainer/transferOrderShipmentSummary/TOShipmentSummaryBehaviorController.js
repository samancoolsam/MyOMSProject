scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/STSContainer/transferOrderShipmentSummary/TOShipmentSummary"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnTOShipmentSummary) {
    return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummaryBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary',
        mashupRefs: [
		{
            mashupRefId: 'extn_getTOShipmentDetails',
            mashupId: 'extn_transferOrderSummary_getTOShipmentDetails'
        },
      //  OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - Start
        {
            mashupRefId: 'extn_AcademySTSPrintAckSlip_ref',
            mashupId: 'extn_AcademyBOPISPrintAckSlip'
        },
     // OMNI-9303 Unable to reprint the pickup acknowledgment slip for STS orders - End
      //OMNI-72012 Start
	 {
		 mashupId : 			'extn_CompleteOnMyWayOrder'
,
		 mashupRefId : 			'extn_CompleteOnMyWayOrder'
,
		 extnType : 			'ADD'

	},
	//OMNI-72012 End
	//OMNI-98331 - Start
	 {
		 mashupId : 			'extn_readyToStage_changeSOShipmentStatus'
,
		 mashupRefId : 			'extn_readyToStage_changeSOShipmentStatus_ref'
,
		 extnType : 			'ADD'

	},
	//OMNI-98331 - End
        ]
    });
});

