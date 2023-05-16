scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/subscreens/CustomOrderLineDetails"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnCustomOrderDetailsScreen) {
    return _dojodeclare("extn.mobile.home.subscreens.CustomOrderLineDetailsBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.subscreens.CustomOrderLineDetails',
        mashupRefs: [

        {
            mashupId: 'eComOrder_getShipmentListForOrder',
            mashupRefId: 'eComOrder_getShipmentListForOrder_Ref'
        }


        
        ]
    });
});