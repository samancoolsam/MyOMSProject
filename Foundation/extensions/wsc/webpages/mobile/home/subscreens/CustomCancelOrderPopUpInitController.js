scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/mobile/home/subscreens/CustomCancelOrderPopUp",
	"scbase/loader!sc/plat/dojo/controller/ScreenController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnCustomCancelOrderPopUp, _scScreenController
) {
return _dojodeclare("extn.mobile.home.subscreens.CustomCancelOrderPopUpInitController", [_scScreenController], {
	screenId: 'extn.mobile.home.subscreens.CustomCancelOrderPopUp',
	mashupRefs: [

		{
            sourceNamespace: 'getReasonCodeList_output',
            callSequence: '',
            mashupRefId: 'getReasonCodeListInit',
            sequence: '',
            sourceBindingOptions: '',
            mashupId: 'common_getReasonCodeList_new'
        },

	]

});
});
