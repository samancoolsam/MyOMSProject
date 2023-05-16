
scDefine(["dojo/text!./templates/CancelOrderBaseScreenExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/form/Textarea","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _idxFilteringSelect
			 ,
			    _idxTextarea
			 ,
			    _scplat
			 ,
			    _scComboDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.order.cancel.CancelOrderBaseScreenExtnUI",
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
//OMNI- 8718 Prevent STS Line Cancellation WEB COM - START
	  eventId: 'initializeLayout'

,	  sequence: '51'

,	  description: 'extn_initializeLayout'



,handler : {
methodName : "extn_initializeLayout"

 
}
}
,
// OMNI- 8718 Prevent STS Line Cancellation WEB COM - END
{
	  eventId: 'cmbReasoncode_onChange'

,	  sequence: '51'

,	  description: 'On Reason code change'



,handler : {
methodName : "ExtnReasonCodeChange"

 
}
}
,
//Start: OMNI-63312 : WCC Cancellations
{
	  eventId: 'onExtnMashupCompletion'

,	  sequence: '51'




,handler : {
methodName : "extn_handleMashupOutput"

 
}
}
//End: OMNI-63312 : WCC Cancellations

]
}

});
});


