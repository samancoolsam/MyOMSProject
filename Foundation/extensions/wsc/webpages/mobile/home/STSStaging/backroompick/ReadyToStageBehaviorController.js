scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/STSStaging/backroompick/ReadyToStage"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnReadyToStage) {
    return _dojodeclare("extn.mobile.home.STSStaging.backroompick.ReadyToStageBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.STSStaging.backroompick.ReadyToStage',
        mashupRefs: [
		{
			mashupId: 'extn_readyToStage_ValidateScannedContainerID',
            mashupRefId: 'extn_validateScannedContainerID'
        },
		//OMNI-9142: Start
		{
			mashupId: 'extn_readyToStage_getSOOrderStatus',
            mashupRefId: 'extn_getSOOrderStatus'
        },
		//OMNI-9142: End
		{
			mashupId: 'extn_readyToStage_getSOShipmentList',
            mashupRefId: 'extn_getSOShipmentList'
        },
		{
			mashupId: 'extn_readyToStage_updateExtnCancellationActioned',
            mashupRefId: 'extn_updateExtnCancellationActionedOnSOCancellationByCustomer'
        },
		{
			mashupId: 'extn_readyToStage_assignStagingLocationAtContainerLevel',
            mashupRefId: 'extn_assignStagingLocationAtContainerLevel'
        },
		{
			mashupId: 'extn_readyToStage_assignStagingLocationAtShpLineLevel',
            mashupRefId: 'extn_assignStagingLocationAtShpLineLevel'
        },
		{
			mashupId: 'extn_readyToStage_getTOShipmentList',
            mashupRefId: 'extn_getTOShipmentList'
        },
		{
			mashupId: 'extn_readyToStage_changeSOShipmentStatus',
            mashupRefId: 'extn_changeSOShipmentStatus'
        },
		//OMNI-9200: Start
		{
			mashupId: 'extn_readyToStage_UpdatePickUpQty',
            mashupRefId: 'extn_changeShipment_UpdatePickUpQty'
        },
		//OMNI-9200: End		
		//OMNI-34725: Start
		{
			mashupId: 'extn_readyToStage_getCompleteOrderDetails',
			mashupRefId: 'extn_getCompleteOrderDetails'
		},
		//OMNI-63938: Start
		{
			mashupId : 	'getPrinterDevice',
			mashupRefId : 'getPrinterDeviceRef'
		},
   	//OMNI-63938: End
		//OMNI- 62990 START
		{
			mashupId: 'extn_PrintOrderTicket',
			mashupRefId: 'extn_PrintSTSOrderTicket_ref'
		}
		//OMNI- 62990 End
        ]
    });
});
