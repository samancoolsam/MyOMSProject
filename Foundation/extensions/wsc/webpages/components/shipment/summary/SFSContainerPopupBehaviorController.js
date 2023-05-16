scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/components/shipment/summary/SFSContainerPopup",
	"scbase/loader!sc/plat/dojo/controller/ServerDataController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnSFSContainerPopup, _scServerDataController
) {
return _dojodeclare("extn.components.shipment.summary.SFSContainerPopupBehaviorController", [_scServerDataController], {
	screenId: 'extn.components.shipment.summary.SFSContainerPopup',
	mashupRefs: []

});
});
