scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/STSContainer/transferOrderShipmentSummary/containerSummaryScreen/ContainerSummary"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnContainerSummaryScreen) {
    return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerSummaryScreen.ContainerSummaryBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.STSContainer.transferOrderShipmentSummary.containerSummaryScreen.ContainerSummary',
        mashupRefs: [
			{
				mashupId: 'extn_readyToStage_assignStagingLocationAtContainerLevel',
				mashupRefId: 'extn_updateZoneAsLOSTAtContainerLevel'
			},			
			// Lost Scenarios - START 
			{
				mashupId: 'extn_containerSummary_markContainerAsLost',
				mashupRefId: 'extn_markContainerAsLost'
			},
			// Lost Scenarios - END 
      //OMNI-9453: Start
			{
				mashupId: 'extn_readyToStage_getSOShipmentList',
				mashupRefId: 'extn_getSOShipmentList'
			},
			{
				mashupId: 'extn_readyToStage_UpdatePickUpQty',
				mashupRefId: 'extn_changeShipment_UpdatePickUpQty'
			},
			{
				mashupId: 'extn_readyToStage_changeSOShipmentStatus',
				mashupRefId: 'extn_changeSOShipmentStatus'
			},
      //OMNI-9453: End
      		//OMNI-68038 - Start
      		{
				mashupId : 	'getLostContainerReasonCode',
				mashupRefId : 'getLostContainerReasonCode'
			},
			//OMNI-68038 - End
			//OMNI-72148 - Start
			{
				mashupId : 			'extn_getFlagToDisableAddQtyAndReadOnlyScannedQty'	,
				mashupRefId : 		'extn_getFlagEditStagingLocation'	,
			},
			//OMNI-72148 - End
			//OMNI-71679 - Start
			{
				mashupId: 'extn_readyToStage_assignStagingLocationAtContainerLevel',
           		mashupRefId: 'extn_assignStagingLocationAtContainerLevel'
        	},
        	{
				mashupId: 'extn_readyToStage_getSOShipmentList',
				mashupRefId: 'extn_getSOShipmentListToUpdateStagingLoc'
			},
			{
				mashupId: 'extn_readyToStage_assignStagingLocationAtShpLineLevel',
	            mashupRefId: 'extn_assignStagingLocationAtShpLineLevel'
	        }
        	//OMNI-71679 - End	
        ]
    });
});

