scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/subscreens/CustomSearchResult"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCustomSearchResult) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchResultBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.subscreens.CustomSearchResult',
        mashupRefs: [

        {
            mashupId: 'eComOrderSearch_getOrderList',
            mashupRefId: 'getOrderListOnNext'
        },


        ]
    });
});