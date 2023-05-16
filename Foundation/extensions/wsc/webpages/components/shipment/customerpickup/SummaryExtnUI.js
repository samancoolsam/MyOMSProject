
scDefine(["dojo/text!./templates/SummaryExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
return _dojodeclare("extn.components.shipment.customerpickup.SummaryExtnUI",
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




,handler : {
methodName : "afterScreenInit"

 
}
},

{
	  eventId: 'saveCurrentPage'

,	  sequence: '19'




,handler : {
methodName : "extn_before_save"

 
}
}
,
{
	  eventId: 'afterBehaviorMashupCall'

,	  sequence: '51'




,handler : {
methodName : "extn_afterBehaviorMashupCall"

 
}
}

]
}

});
});


