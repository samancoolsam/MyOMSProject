
scDefine(["dojo/text!./templates/ShipTrackShippingExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.shipment.shipmentTracking.lineStatePanels.ShipTrackShippingExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
		],
		sourceBindingNamespaces :
		[
			{
	  description: "extn_getShipNodeList_ns"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_2'
						,
	  value: 'extn_getShipNodeList_ns'
						
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

,	  sequence: '51'

,	  description: 'extn_disableShipmentNo'



,handler : {
methodName : "extn_disableShipmentNo"

 
}
}
,
{
	  eventId: 'extn_ShipNodeLink_onClick'

,	  sequence: '51'

,	  description: 'extn_DisplayStoreAddress'



,handler : {
methodName : "extn_DisplayStoreAddress"

 
}
}
,
{
	  eventId: 'onExtnMashupCompletion'

,	  sequence: '51'

,	  description: 'extn_onMashupCompletion'



,handler : {
methodName : "extn_onMashupCompletion"

 
}
}

]
}

});
});


