scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/return/create/lines/ReturnOrderLinesExtnUI","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils", 
"scbase/loader!isccs/utils/OrderUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!isccs/utils/UIUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnReturnOrderLinesExtnUI,
				_scModelUtils,
				_scBaseUtils,
				_isccsOrderUtils,
				_scWidgetUtils,
				_scScreenUtils,
				_isccsUIUtils,
_isccsBaseTemplateUtils
){ 
	return _dojodeclare("extn.return.create.lines.ReturnOrderLinesExtn", [_extnReturnOrderLinesExtnUI],{
		
	//  (OMNI-20155) - extnEGCLineCheck method is created for disabling return creation for Orderlines with EGC gift card
	
	extnEGCLineCheck : function(event, bEvent, ctrl, args) {
		var orderline= null;
		var egcErrorMessage = null;
			var selectRowData = _scBaseUtils.getAttributeValue("selectedRow", false, args);
			if (
            _isccsUIUtils.isArray(
            selectRowData)) {
                orderline = selectRowData[0];
            } 
			else {
                orderline = selectRowData;
            }
			if (!(
            _scBaseUtils.isVoid(
            orderline))) {
			var vLineType= _scModelUtils.getStringValueFromPath("LineType", selectRowData);
			var child = _scScreenUtils.getChildScreen(this,"previewPanel");
			egcErrorMessage = _scScreenUtils.getString(
            child, "Return can not be created for EGC Line");
			_scWidgetUtils.hideWidget(child, "extn_EGCLineErrorPanel", true);
		
			if((!_scBaseUtils.isVoid(vLineType)) && (_scBaseUtils.equals(vLineType,"EGC")))
			{
				
				_scWidgetUtils.setValue(child, "lblAvailableQuantity","0.00 EACH");
				_scWidgetUtils.disableWidget(child, "quantity", false); 
				_scWidgetUtils.disableWidget(child, "cmbReturnReason", false); 
				_scWidgetUtils.showWidget(child, "extn_EGCLineErrorPanel", true  ,null);
			     child.setScreenEnabled(false);
				
				}
			
		}
		return egcErrorMessage;
	}
	

	});
});
