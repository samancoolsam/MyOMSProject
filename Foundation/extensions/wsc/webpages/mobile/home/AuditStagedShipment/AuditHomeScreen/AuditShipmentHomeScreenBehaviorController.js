scDefine(["scbase/loader!dojo/_base/declare", 
 "scbase/loader!dojo/_base/kernel", 
 "scbase/loader!dojo/text", 
 "scbase/loader!sc/plat/dojo/controller/ServerDataController", 
 "scbase/loader!extn/mobile/home/AuditStagedShipment/AuditHomeScreen/AuditShipmentHomeScreen"], 
 function(
_dojodeclare, 
 _dojokernel, 
 _dojotext, 
 _scServerDataController, 
 _extnScanStaggedShipments) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.AuditHomeScreen.AuditShipmentHomeScreenBehaviorController", 
 [_scServerDataController], 
 {
        screenId: 'extn.mobile.home.AuditStagedShipment.AuditHomeScreen.AuditShipmentHomeScreen', 
        mashupRefs: [
		{
			mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen', 
		       mashupRefId: 'startBatchScanProcess'
        	},
		{
			mashupId: 'startBatchScanProcess_AuditShipmentHomeScreen', 
		       mashupRefId: 'abortBatchScanProcess'
        	}
        ]
    });
});
