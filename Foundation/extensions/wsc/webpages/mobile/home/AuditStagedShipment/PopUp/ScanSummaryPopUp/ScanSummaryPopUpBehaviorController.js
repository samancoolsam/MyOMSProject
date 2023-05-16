scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel",
        "scbase/loader!dojo/text",
        "scbase/loader!sc/plat/dojo/controller/ServerDataController",
        "scbase/loader!extn/mobile/home/AuditStagedShipment/PopUp/ScanSummaryPopUp/ScanSummaryPopUp"
    ],
    function(
        _dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCancelledPopUp) {
        return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.ScanSummaryPopUp.ScanSummaryPopUpBehaviorController", [_scServerDataController], {
            screenId: 'extn.mobile.home.AuditStagedShipment.PopUp.ScanSummaryPopUp.ScanSummaryPopUp',
            mashupRefs: [
				{
				mashupRefId: 'getScanSummaryDataOnInit',
				mashupId: 'getScanSummaryData'
			},
			//OMNI-69712 Changes
			{
				mashupRefId: 'updateBatchStatusAsCompletedOnFinish',
				mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen'
			},
			//OMNI-69712 Changes
      
      //OMNI-82354 Start
			{
				mashupRefId: 'insertMissedShipmentsInDBOnFinish',
				mashupId: 'insertMissedShipments_AcadMissedScanDetails'
			}
			//OMNI-82354 End
            ]
        });
    });