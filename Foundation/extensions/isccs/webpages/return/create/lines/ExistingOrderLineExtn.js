scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/return/create/lines/ExistingOrderLineExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/EventUtils",
"scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnExistingOrderLineExtnUI,_scScreenUtils,_scEventUtils,_scBaseUtils,_scWidgetUtils,_scResourcePermissionUtils
){ 
	return _dojodeclare("extn.return.create.lines.ExistingOrderLineExtn", [_extnExistingOrderLineExtnUI],{
	// custom code here - to handle the OOB null value for return reason drop down, quantity

            		
	setInitialized: function(
        event, bEvent, ctrl, args) {
            this.isScreeninitialized = true;
            if(_scBaseUtils.isVoid(_scScreenUtils.getModel(this, "getReturnReasonList_output"))){
				_scScreenUtils.setModel(this, "getReturnReasonList_output", _scScreenUtils.getModel(this.getOwnerScreen(), "getReturnReasonList_output"), null);
			}
			if(_scBaseUtils.isVoid(_scScreenUtils.getModel(this, "selectedOrderLine"))){
			_scScreenUtils.setModel(this, "selectedOrderLine", _scScreenUtils.getModel(this.getOwnerScreen(), "selectedOrderLine"), null);
			}
        }
});
});

