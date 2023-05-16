
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/payment/confirmation/PaymentMethodExtnUI",
"scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/EditorUtils",
"scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!isccs/utils/ModelUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils",
 "scbase/loader!isccs/utils/OrderUtils"]
,
function(			 
			    _dojodeclare,
			    _extnPaymentMethodExtnUI,
				_scWidgetUtils,
				_isccsUIUtils,
				_scEditorUtils,
				_scEventUtils,
				_scScreenUtils,
				_scModelUtils,
				_scBaseUtils,
				_isccsOrderUtils
){ 
	return _dojodeclare("extn.payment.confirmation.PaymentMethodExtn", [_extnPaymentMethodExtnUI],{
	// custom code here

	  getPaymentTypeDisplay: function(
        dataValue, screen, widget, namespace, modelObject, options) {
            var paymentMethodModel = null;
            paymentMethodModel = _scScreenUtils.getModel(
            this, "PaymentMethod");
            var returnValue = null;
            returnValue = _isccsOrderUtils.getPaymentDescription(
            paymentMethodModel, this);
		
			//START - OMNI - 65600
			var paymentRef5Obj = null;			
			paymentRef5Obj = _scModelUtils.getModelObjectFromPath("PaymentReference5", paymentMethodModel);		
			if (!(_scBaseUtils.isVoid(paymentRef5Obj)) && (_scBaseUtils.equals(paymentRef5Obj,"Klarna"))){
				returnValue = paymentRef5Obj;
				}				
			//END - OMNI -65600
			
            var argArray = null;
            argArray = [];
            argArray.push(
            returnValue);
            var ariaLabelValueDel = null;
            ariaLabelValueDel = _scScreenUtils.getFormattedString(
            this, "arialabel_Del_paymentType_Link", argArray);
            var ariaLabelValueEdit = null;
            ariaLabelValueEdit = _scScreenUtils.getFormattedString(
            this, "arialabel_Edit_paymentType_Link", argArray);
            _scWidgetUtils.setLinkAriaLabel(
            this, "lnkDelete", ariaLabelValueDel);
            _scWidgetUtils.setLinkAriaLabel(
            this, "lnkEdit", ariaLabelValueEdit);
            return returnValue;
        }
});
});
