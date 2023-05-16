
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/order/details/OrderSummaryExtnUI", "scbase/loader!isccs/utils/RelatedTaskUtils", "scbase/loader!isccs/utils/UIUtils", 	"scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnOrderSummaryExtnUI,_isccsRelatedTaskUtils, _isccsUIUtils, 	_scWidgetUtils, _scScreenUtils, _scModelUtils,_scBaseUtils
){ 
	return _dojodeclare("extn.order.details.OrderSummaryExtn", [_extnOrderSummaryExtnUI],{
	// custom code here
	extn_ResolveHoldWizard_onClickHandler: function(
        event, bEvent, ctrl, args) {
            var taskInput = null;
            var taskConfig = null;
            taskInput = _isccsRelatedTaskUtils.getRelatedTaskInput(
            this);
            _isccsUIUtils.openWizardInEditor("isccs.order.wizards.resolveHold.ResolveHoldWizard", taskInput, "isccs.editors.OrderEditor", this);
        },
	
  
  	//Start OMNI-12939 update the date and time format
	extn_getOrderDate : function(dataValue, screen, widget, namespace, modelObj, options){
		var orderDateModel = null;
		orderDateModel = _scScreenUtils.getModel(this, "getCompleteOrderDetails_output");
		var orderDate= _scModelUtils.getStringValueFromPath("Order.OrderDate", orderDateModel);
		if(!_scBaseUtils.isVoid(orderDate))
		{
		 var tmp = dojo.date.stamp.fromISOString(orderDate,{selector: 'date'});
		 orderDate = dojo.date.locale.format(tmp, {selector:'date',fullYear:'true'});
		
       		 var dateTimeStamp = dataValue.split("T");
       		 var timestamp = dateTimeStamp[1];
           var onlyTime = timestamp.split("-");
           timestamp  = onlyTime[0];
        	 orderDate = orderDate +" " +timestamp;
		}
		return orderDate;
		//End OMNI-12939 update the date and time format
	}
	
});
});

