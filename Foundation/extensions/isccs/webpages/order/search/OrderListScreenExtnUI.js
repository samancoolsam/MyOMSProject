
scDefine(["dojo/text!./templates/OrderListScreenExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
return _dojodeclare("extn.order.search.OrderListScreenExtnUI",
				[], {
        //Start - OMNI-3717 Payment Tender Type Search
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

,	  description: 'extn_BeforeBehaviorMashupCall'



,handler : {
methodName : "extn_BeforeBehaviorMashupCall"

 
}
}

]
}
//End - OMNI-3717 Payment Tender Type Search
});
});


