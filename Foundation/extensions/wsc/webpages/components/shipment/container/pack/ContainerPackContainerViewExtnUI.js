
scDefine(["dojo/text!./templates/ContainerPackContainerViewExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel"]
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
			    _scDataLabel
){
return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerViewExtnUI",
				[], {
			templateString: templateText,


				namespaces : {
		targetBindingNamespaces :
		[
		],
		sourceBindingNamespaces :
		[
			{
	  value: 'extn_dropDownValues'
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_4'
						
			}
			
		]
	}
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [
	{
	  eventId: 'afterScreenInit'

,	  sequence: '19'




,handler : {
methodName : "beforeScreenInit"

 
}
},

{
	  eventId: 'afterScreenInit'

,	  sequence: '51'




,handler : {
methodName : "extn_afterInitializeScreen"

 
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


