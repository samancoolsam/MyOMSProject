scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/subscreens/CustomSearchOrders"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCustomSearchOrders) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomSearchOrdersBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.subscreens.CustomSearchOrders',
        mashupRefs: [
        
		{
            mashupId: 'mobileHome_EComOrderSearch_getOrderList',
            mashupRefId: 'getOrderListOnSearchOrder'
        }


        ]
    });
});