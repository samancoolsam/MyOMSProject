
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/return/details/ReturnSummaryExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnReturnSummaryExtnUI
			 ,
				_scScreenUtils
			 ,
				_scModelUtils
			 , 
				isccsUIUtils
			 ,
				_scBaseUtils
){ 
	return _dojodeclare("extn.return.details.ReturnSummaryExtn", [_extnReturnSummaryExtnUI],{
	// custom code here
	
	extn_TrackReturnOrder: function (event, bEvent, ctrl, args) {
		
		var orderDetailsModel = null;
		orderDetailsModel = _scScreenUtils.getModel(this, "getCompleteOrderDetails_output");
		var trackingNo= _scModelUtils.getStringValueFromPath("Order.Extn.ExtnTrackingNo", orderDetailsModel);
		
		if(!_scBaseUtils.isVoid(trackingNo)){
			
			var fedxURL = "https://www.fedex.com/apps/fedextrack/?action=track&tracknumbers=";	
			var options = {"destination":"window"};
			isccsUIUtils.openURL(fedxURL+trackingNo, options);	
				
		}
	}
	
		
});
});

