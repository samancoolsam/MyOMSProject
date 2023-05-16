scDefine([
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!dojo/_base/kernel",
	"scbase/loader!dojo/text",
	"scbase/loader!extn/components/shipment/summary/ExtnCancelShipmentOtherStores",
	"scbase/loader!sc/plat/dojo/controller/ServerDataController"
], function(
	_dojodeclare, _dojokernel, _dojotext, _extnExtnCancelShipmentOtherStores, _scServerDataController
) {
return _dojodeclare("extn.components.shipment.summary.ExtnCancelShipmentOtherStoresBehaviorController", [_scServerDataController], {
	screenId: 'extn.components.shipment.summary.ExtnCancelShipmentOtherStores',
	mashupRefs: [{
            cached: 'PAGE',
            mashupId: 'common_getReasonCodeList',
            mashupRefId: 'extn_getCancellationReasonList'
        },	{

        	mashupId: 'common_getReasonCodeList_new',
            mashupRefId: 'extn_getShortageReasonCode_new'
		}
        ]

});
});
