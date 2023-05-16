scDefine(["scbase/loader!dojo/_base/declare", 
 "scbase/loader!dojo/_base/kernel", 
 "scbase/loader!dojo/text", 
 "scbase/loader!sc/plat/dojo/controller/ServerDataController", 
 "scbase/loader!extn/mobile/home/AuditStagedShipment/AuditScanScreen/ScanShipmentLabel"], 
 function(
_dojodeclare, 
 _dojokernel, 
 _dojotext, 
 _scServerDataController, 
 _extnReadyToStage) {
    return _dojodeclare("extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabelBehaviorController", 
 [_scServerDataController], 
 {
        screenId: 'extn.mobile.home.AuditStagedShipment.AuditScanScreen.ScanShipmentLabel', 

        mashupRefs: [
		{
			mashupId : 'getScannedLabelDetails_ScanShipmentLabel', 
		       mashupRefId : 'getScannedLabelDetails'
        	}
        ]
    });
});