
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/order/details/OrderLineSummaryExtnUI","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnOrderLineSummaryExtnUI,_isccsUIUtils,_scBaseUtils,_scModelUtils
){ 
	return _dojodeclare("extn.order.details.OrderLineSummaryExtn", [_extnOrderLineSummaryExtnUI],{
	// custom code here OMNI 12935
		extn_getPromisedDate: function(
        dataValue, screen, widget, namespace, modelObject, options) {
			var promiseDate=null;
            promiseDate = _scModelUtils.getStringValueFromPath("OrderLine.Extn.ExtnInitialPromiseDate", modelObject);
        	if(_scBaseUtils.isVoid(promiseDate)){
			return null;
			}

			return _scBaseUtils.formatDateToUserFormat(promiseDate);
        }
});
});

