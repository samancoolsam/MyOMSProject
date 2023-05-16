
scDefine(["dojo/text!./templates/PaymentInquiryExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!extn/utils/customUtils","scbase/loader!gridx/Grid","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/GridxDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Link"]
 , function(			 
			    templateText
			 ,
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojolang
			 ,
			    _dojotext
			 ,
			    _extncustomUtils
			 ,
			    _gridxGrid
			 ,
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scGridxDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.payment.inquiry.PaymentInquiryExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [

{
	  eventId: 'afterScreenInit'

,	  sequence: '51'

,	  description: 'extn_viewauthorizationsLink'



,handler : {
methodName : "extn_viewauthorizationsLink"

 
}
}
,
{
	  eventId: 'afterScreenInit'

,	  sequence: '52'




,handler : {
methodName : "extn_PopulateDeferredInterest"

 
}
}
  		// OMNI-1923 Customer Care: Sterling WCC: Y order Link - Start 
,
{
	  eventId: 'grdChargesAndRefunds_Link_ScHandleLinkClicked'

,	  sequence: '19'

,	  description: 'extn_displayGCPaymentDetails'



,handler : {
methodName : "extn_displayGCPaymentDetails"

 
}
}
,
{
	  eventId: 'extn_ViewAutorizations_onClick'

,	  sequence: '51'

,	  description: 'extn_openPaymentDetails'



,handler : {
methodName : "extn_openPaymentDetails"

 
}
}

]
}

});
});


