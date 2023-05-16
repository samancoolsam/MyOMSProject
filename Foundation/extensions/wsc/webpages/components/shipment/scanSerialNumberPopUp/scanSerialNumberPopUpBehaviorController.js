scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel",
        "scbase/loader!dojo/text",
        "scbase/loader!sc/plat/dojo/controller/ServerDataController",
        "scbase/loader!extn/components/shipment/scanSerialNumberPopUp/scanSerialNumberPopUp"
    ],
    function(
        _dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCancelledPopUp) {
        return _dojodeclare("extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberPopUpBehaviorController", [_scServerDataController], {
            screenId: 'extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberPopUp',
            mashupRefs: [
				{
				mashupRefId: 'getScanSummaryDataOnInit',
				mashupId: 'getScanSummaryData'
			}
			
            ]
        });
    });