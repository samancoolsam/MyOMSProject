scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel",
        "scbase/loader!dojo/text",
        "scbase/loader!sc/plat/dojo/controller/ServerDataController",
        "scbase/loader!extn/mobile/home/AuditStagedShipment/PopUp/AbortPopUp/AbortPopUp"
    ],
    function(
        _dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnAbortPopUp) {
        return _dojodeclare("extn.mobile.home.AuditStagedShipment.PopUp.AbortPopUp.AbortPopUpBehaviorController", [_scServerDataController], {
            screenId: 'extn.mobile.home.AuditStagedShipment.PopUp.AbortPopUp.AbortPopUp',
           mashupRefs: [
		{
			mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen', 
		       mashupRefId: 'startBatchScanProcess'
        	}
        ]
        });
    });