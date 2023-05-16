
scDefine(["dojo/text!./templates/ContainerPackProductListExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.components.shipment.container.pack.ContainerPackProductListExtnUI",
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


