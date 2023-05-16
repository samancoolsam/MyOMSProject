scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel",
 "scbase/loader!dojo/text",
 "scbase/loader!sc/plat/dojo/controller/ServerDataController",
 "scbase/loader!extn/components/shipment/printerSelectionPopUp/PrinterSelectionContainer"],
 function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnPrinterSelectionContainer) {
    return _dojodeclare("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainerBehaviorController", [_scServerDataController], {
        screenId: 'extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer',
	mashupRefs: [
		{
			mashupRefId: 'getPrinterDeviceInit',
            mashupId: 'getPrinterDevice'
        },
		]
		
});
});
