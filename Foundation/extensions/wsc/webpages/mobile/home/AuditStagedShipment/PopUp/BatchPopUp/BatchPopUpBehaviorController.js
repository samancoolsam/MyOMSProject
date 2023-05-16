scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel",
        "scbase/loader!dojo/text",
        "scbase/loader!sc/plat/dojo/controller/ServerDataController",
        "scbase/loader!extn/mobile/home/AuditStagedShipment/PopUp/BatchPopUp/BatchPopUp"
    ],
    function(
        _dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnBatchPopUp) {
        return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.BatchPopUp.BatchPopUpBehaviorController", [_scServerDataController], {
            screenId: 'extn.mobile.home.AuditStagedShipment.PopUp.BatchPopUp.BatchPopUp',
            mashupRefs: [
				{
					mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen', 
					mashupRefId: 'startBatchScanProcess'
				},
				        /*OMNI-69646 Continue Changes - START*/
				{
					mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen', 
					mashupRefId: 'continueScanProcess'
				}
						/*OMNI-69646 Continue Changes - END*/
            ]
        });
    });