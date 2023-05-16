scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/subscreens/CustomOrderDetailsScreen"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCustomOrderDetailsScreen) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderDetailsScreenBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.subscreens.CustomOrderDetailsScreen',
        mashupRefs: [

        {
            mashupId: 'eComOrderSearch_AcademyCancelOrder',
            mashupRefId: 'cancelOrderFromOrderSummaryScreen'
        },
        {
            sourceNamespace: 'getOrderDetails_output',
            callSequence: '',
            mashupRefId: 'getOrderDetailsReload',
            sequence: '',
            sourceBindingOptions: '',
            mashupId: 'eComOrderSearch_getOrderDetails'
        },
        
        ]
    });
});