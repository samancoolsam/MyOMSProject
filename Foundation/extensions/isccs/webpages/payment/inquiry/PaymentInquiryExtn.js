
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/payment/inquiry/PaymentInquiryExtnUI","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/ModelUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!sc/plat/dojo/info/ApplicationInfo", "scbase/loader!sc/plat/dojo/utils/PlatformUIFmkImplUtils","scbase/loader!sc/plat/dojo/utils/GridxUtils","scbase/loader!sc/plat/dojo/utils/EventUtils"]
,
function(_dojodeclare,_extnPaymentInquiryExtnUI,_scScreenUtils,_scModelUtils,_isccsUIUtils,_scBaseUtils,
			    _scWidgetUtils, dApplicationInfo,_scPlatformUIFmkImplUtils, _scGridxUtils,_scEventUtils
){ 
	return _dojodeclare("extn.payment.inquiry.PaymentInquiryExtn", [_extnPaymentInquiryExtnUI],{
	// custom code here

  extn_openPaymentDetails : function(event, bEvent, ctrl, args)
   {
     var paymentModel = _scScreenUtils.getModel(this,"paymentInquiry-getPaymentInquiryDetails_Output");
     var strOrderHeaderKey= _scModelUtils.getStringValueFromPath("Order.OrderHeaderKey", paymentModel);
     var strWindowSpecs = "location=yes,height=700,width=750,scrollbars=yes,status=yes,left=100 top=100";
     var hostName=window.location.hostname;
     var port=window.location.port;
     //var paymentUrl ="http://" +hostName+":"+port+"/smcfs/console/order.detail?EntityKey=%3COrder%2BOrderHeaderKey%3D%22" +strOrderHeaderKey +"%22%2F%3E&CurrentDetailViewID=YOMD150";
     //window.open(paymentUrl, "_blank", strWindowSpecs);
	 	 
	 var xhttp1 = new XMLHttpRequest();
	 var token = dApplicationInfo.getCSRFToken();
	 var loginId = _scPlatformUIFmkImplUtils.getUserId();
	 
	 xhttp1.open("POST", "http://" + hostName + ":" + port + "/smcfs/NoUILoginServlet", true);
	  	 	 
	 xhttp1.onreadystatechange = function() 
	 {
		if (this.readyState == 4 && this.status == 200) 
		{
			if(xhttp1.getResponseHeader('sc_csrf_token_value') != null)
			{
				token = xhttp1.getResponseHeader('sc_csrf_token_value');
			}
				
			 var entityKey = '<Order OrderHeaderKey="' + strOrderHeaderKey + '" />';
			 //Start - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM)
			 var screen = 'AYOMD150';
			 //End - OMNI-65895 - Klarna VCN - Sterling to display "Klarna" memo (DOM)
			 var body = "<HTML><HEAD><script>function submitForm(){document.forms['loginForm'].submit();}</script></HEAD><BODY onLoad='submitForm()'><form id='loginForm' method='post' action='";
			 body = body + "http://" + hostName + ":" + port + "/smcfs/console/order.detail'>" ;
			 body = body + "<input type='hidden' name='EntityKey' value='" + entityKey + "'/>";
			 body = body + "<input type='hidden' name='CurrentDetailViewID' value='" + screen + "'/>";
			 body = body + "<input type='hidden' name='securetoken' value='" + token + "'/>";
			 body = body + "<input type='hidden' name='Popup' value='Y'/>";
			 body = body + "</form></BODY></HTML>";
			 					 
			 var popup = window.open("", "WebComPopup", strWindowSpecs);
			 popup.document.write(body);
			 popup.document.close();	
		}
	}
	
	xhttp1.send("LoginID=" + loginId + "&TokenId=" + token + "&ApplicationName=YFSSYS00004");
	 
   },
  extn_viewauthorizationsLink: function(event, bEvent, ctrl, args)
   {
   var paymentModel = _scScreenUtils.getModel(this,"paymentInquiry-getPaymentInquiryDetails_Output");
   var sDocumentType= _scModelUtils.getStringValueFromPath("Order.DocumentType", paymentModel);
   if(_scBaseUtils.equals(sDocumentType,"0001"))
    {
	_scWidgetUtils.showWidget(this,"extn_ViewAutorizations",false,null);	
    }

   },

// OMNI-1923 Customer Care: Sterling WCC: Y order Link - Start
extn_displayGCPaymentDetails: function(event, bEvent, ctrl, args)
 {
         var selectedRowIndex = null;
     selectedRowIndex = _scBaseUtils.getNumberValueFromBean("rowIndex", args);
     var gridId = "";
     gridId = _scBaseUtils.getStringValueFromBean("uId", args);
     var colField= _scModelUtils.getStringValueFromPath("cellJson.colField",args);
     var selectedRowObj = null;
     if(_scBaseUtils.equals(colField,"extn_Payment_Ref"))
     {
     selectedRowObj = _scGridxUtils.getRowJsonFromRowIndex(
     this, gridId, selectedRowIndex);
     var sOrderHeaderKey = "";
     sOrderHeaderKey = _scModelUtils.getStringValueFromPath("TransferToOrder.OrderHeaderKey", selectedRowObj);
     sOrderNo = _scModelUtils.getStringValueFromPath("TransferToOrder.OrderNo", selectedRowObj);
     if(!_scBaseUtils.isVoid(sOrderNo))
     {
     var newOrderModel = null;
     newOrderModel = _scModelUtils.createNewModelObjectWithRootKey("Order");
     _scModelUtils.setStringValueAtModelPath("Order.OrderHeaderKey", sOrderHeaderKey, newOrderModel);
     _scModelUtils.setStringValueAtModelPath("Order.OrderNo", sOrderNo, newOrderModel);
     var sReturnDocumentType = "";
     _isccsUIUtils.openWizardInEditor("isccs.payment.wizards.paymentInquiry.PaymentInquiryWizard", newOrderModel , "isccs.editors.OrderEditor", this);
     _scEventUtils.stopEvent(bEvent);
     }
     }       
}
// OMNI-1923 Customer Care: Sterling WCC: Y order Link - End
// OMNI-20858 Customer Care: Sterling WCC: Display Deffered Interest Promo Start
,
		extn_PopulateDeferredInterest: function() 
		{
		var orderDetailsModel = null;
		var promoApplied=null;
		var paymentType=null;
		orderDetailsModel = _scScreenUtils.getModel(this, "paymentInquiry-getPaymentInquiryDetails_Output");
		var paymentMethod= _scModelUtils.getStringValueFromPath("Order.PaymentMethods.PaymentMethod", orderDetailsModel);
		for(var i in paymentMethod){
			var paymentReference5= _scModelUtils.getStringValueFromPath("PaymentReference5", paymentMethod[i]);
			var paymentType= _scModelUtils.getStringValueFromPath("PaymentType", paymentMethod[i]);
			if(!_scBaseUtils.isVoid(paymentReference5) && !_scBaseUtils.isVoid(paymentType) && _scBaseUtils.equals(paymentType,"PLCC"))
			{
			if(_scBaseUtils.equals(paymentReference5,"00406"))
			{
				promoApplied="6 Months";
				_scWidgetUtils.showWidget(this,"extn_deffered Interest",false,null);
			}
			if(_scBaseUtils.equals(paymentReference5,"00412"))
			{
				promoApplied="12 Months";
				_scWidgetUtils.showWidget(this,"extn_deffered Interest",false,null);
			}	
			_scModelUtils.setStringValueAtModelPath("Order.PaymentReference5", promoApplied, orderDetailsModel);
			_scScreenUtils.setModel(this, "paymentInquiry-getPaymentInquiryDetails_Output", orderDetailsModel, null);
			}
		}
	}
	//OMNI-20858 Customer Care: Sterling WCC: Display Deffered Interest Promo End
});
});
