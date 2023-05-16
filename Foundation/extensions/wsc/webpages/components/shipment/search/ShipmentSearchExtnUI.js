
scDefine(["dojo/text!./templates/ShipmentSearchExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/CheckBox","scbase/loader!idx/form/TextBox","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CheckBoxDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _idxCheckBox
			 ,
			    _idxTextBox
			 ,
			    _scplat
			 ,
			    _scCheckBoxDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.components.shipment.search.ShipmentSearchExtnUI",
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
	  eventId: 'afterScreenLoad'

,	  sequence: '51'




,handler : {
methodName : "afterScreenLoad"

 
}
},

{
	  eventId: 'afterBehaviorMashupCall'

,	  sequence: '51'




,handler : {
methodName : "afterBehaviourMashupCall"

 
}
}

]
}

});
});


