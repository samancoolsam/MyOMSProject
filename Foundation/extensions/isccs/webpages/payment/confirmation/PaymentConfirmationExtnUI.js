
scDefine(["dojo/text!./templates/PaymentConfirmationExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _scplat
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.payment.confirmation.PaymentConfirmationExtnUI",
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

,	  description: 'extn_disablebuttons'



,handler : {
methodName : "extn_disablebuttons"

 
}
}
,
{
	  eventId: 'saveCurrentPage'

,	  sequence: '19'

,	  description: 'extn_skipProcessOrderPayments'



,handler : {
methodName : "extn_skipProcessOrderPayments"

 
}
}

]
}

});
});


