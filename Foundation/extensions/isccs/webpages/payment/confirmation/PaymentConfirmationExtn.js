
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/payment/confirmation/PaymentConfirmationExtnUI","scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/EditorUtils","scbase/loader!sc/plat/dojo/utils/EventUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnPaymentConfirmationExtnUI
			 ,
      			    _scWidgetUtils
			 ,
			    _isccsUIUtils
			 ,
			    _scEditorUtils
			 ,
			    _scEventUtils
){ 
	return _dojodeclare("extn.payment.confirmation.PaymentConfirmationExtn", [_extnPaymentConfirmationExtnUI],{
	// custom code here

  extn_disablebuttons: function(event,bEvent,ctrl,args)
   {
     var model = args;
     _scWidgetUtils.hideWidget(
                        this, "btnCreateExchange", false, null);     
     _scWidgetUtils.disableWidget(
                    this, "btnAddPaymentMethod", false);     

   },

extn_skipProcessOrderPayments:function(event,bEvent,ctrl,args)
{
  var wizardInstance = null;
  wizardInstance = _isccsUIUtils.getCurrentWizardInstance(_scEditorUtils.getCurrentEditor());
  if (wizardInstance.wizardUI.wizardId){
    if (wizardInstance.wizardUI.wizardId == "isccs.order.wizards.cancelOrder.CancelOrderWizard"){
		_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
		_scEventUtils.stopEvent(bEvent);
	 }
	 else if (wizardInstance.wizardUI.wizardId == "isccs.return.wizards.createReturn.CreateReturnWizard"){
		_scEventUtils.fireEventToParent(this, "onSaveSuccess", null);
		_scEventUtils.stopEvent(bEvent);
	 }
	}
   
}
});
});

