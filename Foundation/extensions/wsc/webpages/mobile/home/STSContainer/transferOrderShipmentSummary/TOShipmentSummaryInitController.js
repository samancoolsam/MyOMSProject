scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ScreenController", "scbase/loader!extn/mobile/home/STSContainer/transferOrderShipmentSummary/TOShipmentSummary"], function(
_dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnTOShipmentSummary) {
    return _dojodeclare("extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummaryInitController", [_scScreenController], {
        screenId: 'extn.mobile.home.STSContainer.transferOrderShipmentSummary.TOShipmentSummary',
        // OMNI-56184 - Change HIP Printer for STS - START
    	childControllers: [{
            screenId: 'wsc.components.shipment.summary.ShipmentRT',
            controllerId: 'wsc.components.shipment.summary.ShipmentRTInitController'
        }],
        // OMNI-56184 - Change HIP Printer for STS - END
        mashupRefs: [
		
        ]
    });
});