
scDefine(["dojo/text!./templates/ShipmentLineDetailsExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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
          
          
      //OMNI-45645-SIM Integration Changes -- Start
			 ,
			    _idxContentPane
      //OMNI-45645-SIM Integration Changes -- End

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
			 ,
			    _scLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.common.screens.ShipmentLineDetailsExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	//OMNI-90928 START
	
				,	
	namespaces : {
		targetBindingNamespaces :
		[
		],
		sourceBindingNamespaces :
		[
			{
	  description: "extn_SerialNoValue"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_9'
						,
	  value: 'extn_SerialNoValue'
						
			}
			
		]
	}

	 //OMNI-90928 END
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
methodName : "displayServiceDetails"

 
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
//OMNI-66083 - END
//OMNI-92191 - Start
,
{
	  eventId: 'removeQtyLink_onClick'

,	  sequence: '19'

,	  description: 'launchScanPopup'



,handler : {
methodName : "launchScanPopup"

 
}
}
//OMNI-92191 - End
]
}
});
});


