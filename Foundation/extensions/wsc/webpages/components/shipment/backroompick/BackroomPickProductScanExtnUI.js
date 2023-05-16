
scDefine(["dojo/text!./templates/BackroomPickProductScanExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scButtonDataBinder
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.backroompick.BackroomPickProductScanExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
		//OMNI-90674 - Start
		{
		 scExtensibilityArrayItemId: '"extn_TargetNamespaces_10'
						,
	    value: 'extn_SerialNo_input'
		}
		//OMNI-90674 - End
		],
		sourceBindingNamespaces :
		[
			{
	  description: "Namespace to check if any hip printers are available for a store"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_7'
						,
	  value: 'extn_getPrinterDeviceMashupRefId_output'
						
			}
			,
			//OMNI-90674 - Start
			{
	  description: "Namespace to store translateBarcode API output"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_8'
						,
	  value: 'extn_translateBarcode_ref_output'
						
			},
			{
	  description: "This namespace contains scanned serial no model"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_9'
						,
	  value: 'extn_SerialNo_output'
						
			}
			//OMNI-90674 - End
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
	  eventId: 'afterScreenLoad'

,	  sequence: '51'




,handler : {
methodName : "extn_afterOOTBafterScreenLoad"

 
}
}
,
{
	  eventId: 'afterScreenInit'

,	  sequence: '51'




,handler : {
methodName : "extn_afterInitializeScreen"

 
}
}
,
{
	  eventId: 'afterScreenInit'

,	  sequence: '51'




,handler : {
methodName : "checkIfHipPrinterAvailableFlag"

 
}
},
//OMNI-90674 - Start
{
	  eventId: 'extn_serialNoButton_onClick'

,	  sequence: '51'




,handler : {
methodName : "scanProductAndSerialNo"

 
}
}
,
{
	  eventId: 'extn_serialNoBox_onKeyDown'

,	  sequence: '51'




,handler : {
methodName : "scanProductAndSerialNoOnEnter"

 
}
}
//OMNI-90674 - End
]
}

});
});


