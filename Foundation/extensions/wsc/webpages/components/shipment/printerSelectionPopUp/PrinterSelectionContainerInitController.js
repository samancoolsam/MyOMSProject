scDefine(["scbase/loader!dojo/_base/declare",
 "scbase/loader!dojo/_base/kernel", 
"scbase/loader!dojo/text", 
"scbase/loader!sc/plat/dojo/controller/ScreenController",
 "scbase/loader!extn/components/shipment/printerSelectionPopUp/PrinterSelectionContainer"], 
function(
_dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnPrinterSelectionContainer) {
    return _dojodeclare("extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainerInitController", [_scScreenController], {
        screenId: 'extn.components.shipment.printerSelectionPopUp.PrinterSelectionContainer',
       mashupRefs: [
		{
            sourceNamespace: 'getPrinterDevice_output',
            mashupRefId: 'getPrinterDeviceInit',
            mashupId: 'getPrinterDevice'
        },

	]
    });
});