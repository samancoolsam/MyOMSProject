
scDefine(["dojo/text!./templates/BatchLineDetailsExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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

      //OMNI-45645-SIM Integration Changes -- Start
			 ,
			    _idxContentPane
      //OMNI-45645-SIM Integration Changes -- End

			 ,
			    _scplat
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
return _dojodeclare("extn.components.batch.batchpick.scanlist.BatchLineDetailsExtnUI",
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
	  description: "This namespace is to retrieve the store details along with the item details."
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_1'
						,
	  value: 'extn_BatchLine'
						
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




,handler : {
methodName : "extn_afterInitializeScreen"

 
}
}
]
}

});
});


