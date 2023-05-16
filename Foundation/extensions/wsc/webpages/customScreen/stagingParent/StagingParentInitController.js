scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/customScreen/stagingParent/StagingParent",
	"scbase/loader!sc/plat/dojo/controller/ScreenController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnStagingParent, _scScreenController
) {
return _dojodeclare("extn.customScreen.stagingParent.StagingParentInitController", [_scScreenController], {
	screenId: 'extn.customScreen.stagingParent.StagingParent',
	mashupRefs: []

});
});
