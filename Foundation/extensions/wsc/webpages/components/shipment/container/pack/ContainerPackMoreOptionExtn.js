
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackMoreOptionExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!wsc/components/shipment/container/pack/ContainerPackMoreOptionUI"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackMoreOptionExtnUI
			 ,
			 	_scBaseUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _wscContainerPackMoreOptionUI
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackMoreOptionExtn", [_extnContainerPackMoreOptionExtnUI],{
	// custom code here
	// code changes to hide 'add more products' : Begin
	extn_afterInitializeScreen:  function(event, bEvent, ctrl, args) {
		var container_Src = _scScreenUtils.getModel(this, "container_Src");
		var trackingNo = _scModelUtils.getStringValueFromPath("Container.TrackingNo", container_Src);
        if (!_scBaseUtils.isVoid(trackingNo)) {
                _scWidgetUtils.disableWidget(this, "addProductsLink", false);
        }
	}
	// code changes to hide 'add more products' : End
});
});

