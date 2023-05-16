scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ScreenController", "scbase/loader!extn/mobile/home/subscreens/CustomSearchOrders"], function(
_dojodeclare, _dojokernel, _dojotext, _scScreenController, _extnCustomSearchOrders) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchOrdersInitController", [_scScreenController], {
        screenId: 'extn.mobile.home.subscreens.CustomSearchOrders',
        mashupRefs: [
        {
            sourceNamespace: 'eComOrderSearch_getNumberofDays_output',
            mashupRefId: 'getNumberofDaysInit',
            mashupId: 'eComOrderSearch_getNumberofDays'
        },

        ]
    });
});