scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ScreenController", "scbase/loader!extn/mobile/home/subscreens/CustomSearchResult"], function(
_dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnCustomSearchResult) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchResultInitController", [_scScreenController], {
        screenId: 'extn.mobile.home.subscreens.CustomSearchResult',
        mashupRefs: [

		{
            sourceNamespace: 'getOrderList_output',
            callSequence: '',
            mashupRefId: 'getOrderListInit',
            sequence: '',
            sourceBindingOptions: '',
            mashupId: 'eComOrderSearch_getOrderList'
        },



        ]
    });
});