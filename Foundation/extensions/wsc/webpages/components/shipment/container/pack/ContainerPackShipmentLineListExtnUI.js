
scDefine(["dojo/text!./templates/ContainerPackShipmentLineListExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
 , function(			 
			    templateText
			 ,
			    _dijitButton
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
			    _scButtonDataBinder
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.container.pack.ContainerPackShipmentLineListExtnUI",
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
methodName : "extn_afterInitializeScreen"

 
}
}
,
//Start - OMNI-3680 BOPIS:Record Shortage Button
{
	  eventId: 'extn_button_onClick'

,	  sequence: '51'

,	  description: 'This method is used to open shortage resolution popup'



,handler : {
methodName : "openShortageResolutionPopup"

 
}
}
//Start - OMNI-3680 BOPIS:Record Shortage Button
//OMNI:66083 - START
,
{
	  eventId: 'afterBehaviorMashupCall',	 
	  sequence: '51',
	  handler : {
		methodName : "extn_AfterBehaviorMashupCall" 
}
}
//OMNI:66083 - END
]
}

});
});


