scDefine(["scbase/loader!dojo/_base/declare",
        "scbase/loader!dojo/_base/kernel",
        "scbase/loader!dojo/text",
        "scbase/loader!sc/plat/dojo/controller/ScreenController",
        "scbase/loader!extn/components/shipment/scanSerialNumberPopUp/scanSerialNumberPopUp"
    ],
    function(
        _dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnCancelledPopUp) {
        return _dojodeclare("extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberInitController", [_scScreenController], {
            screenId: 'extn.components.shipment.scanSerialNumberPopUp.scanSerialNumberPopUp',
            mashupRefs: [
				
            ]
        });
    });