scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/components/shipment/summary/SFSContainerPopup",
	"scbase/loader!sc/plat/dojo/controller/ScreenController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnSFSContainerPopup, _scScreenController
) {
return _dojodeclare("extn.components.shipment.summary.SFSContainerPopupInitController", [_scScreenController], {
	screenId: 'extn.components.shipment.summary.SFSContainerPopup',
	mashupRefs: []

});
});
