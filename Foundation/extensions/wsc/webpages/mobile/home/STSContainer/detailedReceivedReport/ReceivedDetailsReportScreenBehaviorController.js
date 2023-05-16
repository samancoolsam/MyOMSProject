scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/STSContainer/detailedReceivedReport/ReceivedDetailsReportScreen"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnReceivedDetailsReportScreen) {
    return _dojodeclare("extn.mobile.home.STSContainer.detailedReceivedReport.ReceivedDetailsReportScreenBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.STSContainer.detailedReceivedReport.ReceivedDetailsReportScreen',
        mashupRefs: [
		{
            mashupRefId: 'getScannedContainerCount',
            mashupId: 'detailedReceivedReport_getScannedContainerCount'
        },
        ]
    });
});

