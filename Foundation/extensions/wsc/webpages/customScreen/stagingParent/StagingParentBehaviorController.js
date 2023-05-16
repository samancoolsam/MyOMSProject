scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/customScreen/stagingParent/StagingParent",
	"scbase/loader!sc/plat/dojo/controller/ServerDataController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnStagingParent, _scServerDataController
) {
return _dojodeclare("extn.customScreen.stagingParent.StagingParentBehaviorController", [_scServerDataController], {
	screenId: 'extn.customScreen.stagingParent.StagingParent',
	mashupRefs: [{
            mashupId: 'backroomPickUp_saveHoldLocationToShipment',
            mashupRefId: 'saveHoldLocation_ref'
        }]

});
});
