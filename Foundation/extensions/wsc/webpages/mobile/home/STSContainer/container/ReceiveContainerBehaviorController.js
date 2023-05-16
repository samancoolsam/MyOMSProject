scDefine(["scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/text", "scbase/loader!sc/plat/dojo/controller/ServerDataController", "scbase/loader!extn/mobile/home/STSContainer/container/ReceiveContainer"], function(
_dojodeclare, _dojokernel, _dojotext, _scServerDataController, _extnReceiveContainer) {
    return _dojodeclare("extn.mobile.home.STSContainer.container.ReceiveContainerBehaviorController", [_scServerDataController], {
        screenId: 'extn.mobile.home.STSContainer.container.ReceiveContainer',
        mashupRefs: [
		{
            mashupRefId: 'createAndReceiveTOContainer',
            mashupId: 'receiveContainer_createAndReceiveTOContainer'
        },
		{
            mashupRefId: 'getExtnSTSContainerList',
            mashupId: 'receiveContainer_getExtnSTSContainerList'
        },
				{
            mashupRefId: 'getExtnSTSContainerCountList',
            mashupId: 'receiveContainerCount_getExtnSTSContainerList'
        },
		{
            mashupRefId: 'getShipmentContainerList',
            mashupId: 'receiveContainer_getShipmentContainerList'
        },
		{
			mashupId: 'extn_readyToStage_updateExtnCancellationActioned',
            mashupRefId: 'extn_updateExtnCancellationActionedOnSOCancellationByCustomer'
        }
        ]
    });
});

