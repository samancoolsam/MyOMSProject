
scDefine(["dojo/text!./templates/OrderSearchExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/RadioButtonSet","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/RadioSetDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _idxRadioButtonSet
			 ,
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scRadioSetDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.order.search.OrderSearchExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [
//Start - OMNI-3717 Payment Tender Type Search
{
	  eventId: 'extn_PaymentCardValue_onKeyDown'

,	  sequence: '51'

,	  description: 'On Enter'



,handler : {
methodName : "SST_invokeApiOnEnter"

 
}
}
//End - OMNI-3717 Payment Tender Type Search
]
}

});
});


