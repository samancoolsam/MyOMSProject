
scDefine(["dojo/text!./templates/CustomerIdentificationExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label"]
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
			    _idxFilteringSelect
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scComboDataBinder
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLabel
){
return _dojodeclare("extn.components.shipment.customerpickup.CustomerIdentificationExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
			{
	  value: 'extn_Test'
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_3'
						
			}
			
		],
		sourceBindingNamespaces :
		[
			{
	  value: 'extn_customerNS'
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_4'
						
			},
			{
	  value: 'extn_DefaultCustomerName'
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_5',
	  description: "Default Customer Name"
						
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
methodName : "extn_afterInitialize"

 
}
}
,
{
	  eventId: 'afterScreenInit'

,	  sequence: '50'




,handler : {
methodName : "extn_afterScreenInit"

 
}
}
,
{
	  eventId: 'afterScreenLoad'

,	  sequence: '51'




,handler : {
methodName : "updateCustomerDropDown"

 
}
}
,
{
	  eventId: 'extn_filteringselect_onChange'

,	  sequence: '51'




,handler : {
methodName : "customerDropDown"

 
}
},

// START - (OMNI - 1434)  : BOPIS Page Tagging/Reporting
{
	  eventId: 'saveCurrentPage'

,	  sequence: '19'

,	  description: 'extn_RecordStoreUserAction'



,handler : {
methodName : "extn_RecordStoreUserAction"

 
}
},

// END - (OMNI - 1434)  : BOPIS Page Tagging/Reporting
{
	  eventId: 'afterScreenLoad'

,	  sequence: '52'




,handler : {
methodName : "customerDropDown"

 
}
}

]
}

});
});


