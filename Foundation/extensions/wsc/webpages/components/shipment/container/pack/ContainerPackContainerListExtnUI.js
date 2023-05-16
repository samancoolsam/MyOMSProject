
scDefine(["dojo/text!./templates/ContainerPackContainerListExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label"]
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
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scComboDataBinder
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLabel
){
return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerListExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
			{
	  value: 'extn_tNS'
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_4'
						
			}
			,
			{
	  value: 'extn_containerBarCode'
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_5'
						
			}
			,
			{
	  value: 'extn_dropDownTM'
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_6'
						
			}
			
		],
		sourceBindingNamespaces :
		[
			{
	  value: 'extn_containerIDs'
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_2'
						
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

,	  sequence: '21'




,handler : {
methodName : "beforeScreenInit"

 
}
}
,
{
	  eventId: 'extn_textfield_onKeyDown'

,	  sequence: '51'




,handler : {
methodName : "ExtnhandleBarcodeScan"

 
}
}
,
{
	  eventId: 'afterBehaviorMashupCall'

,	  sequence: '51'




,handler : {
methodName : "afterBehaviousMashup"

 
}
},
{
	  eventId: 'extn_filteringselect_onChange'

,	  sequence: '51'




,handler : {
methodName : "onDropDownChange"

 
}
}

//BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
,
{
	  eventId: 'containerWeight_onKeyUp'

,	  sequence: '19'




,handler : {
methodName : "extn_beforeSaveContainerWeightOnEnter"

 
}
}
//BOPIS-1576: Remove manual "Enter" hit after container weight input - end



]
}

});
});


