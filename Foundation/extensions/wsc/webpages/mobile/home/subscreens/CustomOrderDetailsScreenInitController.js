scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ScreenController", "scbase/loader!extn/mobile/home/subscreens/CustomOrderDetailsScreen"], function(
_dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnCustomOrderDetailsScreen) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderDetailsScreenInitController", [_scScreenController], {
        screenId: 'extn.mobile.home.subscreens.CustomOrderDetailsScreen',
        mashupRefs: [

		{
            sourceNamespace: 'getOrderDetails_output',
            callSequence: '',
            mashupRefId: 'getOrderDetailsInit',
            sequence: '',
            sourceBindingOptions: '',
            mashupId: 'eComOrderSearch_getOrderDetails'
        },



        ]
    });
});