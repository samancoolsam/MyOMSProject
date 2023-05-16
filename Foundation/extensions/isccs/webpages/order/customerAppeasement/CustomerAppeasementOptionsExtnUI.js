
scDefine(["dojo/text!./templates/CustomerAppeasementOptionsExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementOptionsExtnUI",
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
	  eventId: 'beforeBehaviorMashupCall'

,	  sequence: '51'

,	  description: 'extn_handleBehaviorMashupInput'



,handler : {
methodName : "extn_handleBehaviorMashupInput"

 
}
}

]
}

});
});


