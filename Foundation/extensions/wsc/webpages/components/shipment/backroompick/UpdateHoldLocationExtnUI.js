
scDefine(["dojo/text!./templates/UpdateHoldLocationExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel"]
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
			    _idxFilteringSelect
			 ,
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scButtonDataBinder
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
){
return _dojodeclare("extn.components.shipment.backroompick.UpdateHoldLocationExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
     // OMNI- 3676 BOPIS: Apply All Staging Locations START 
	namespaces : {
		targetBindingNamespaces :
		[
			{
	  description: "extn_StagingLocationAll"
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_2'
						,
	  value: 'extn_StagingLocationAll'
						
			}
			,
			{
	  description: "This namesapce is the placeholder for storing printer id"
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_3'
						,
	  value: 'extn_getChangedPrinterID'
						
			}
			
		],
		sourceBindingNamespaces :
		[
			{
	  description: "printer id from session object"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_4'
						,
	  value: 'extn_printerFromSession'
						
			}
			,
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_5'
						,
	  value: 'extn_getPrinterDevice_output1'
						
			}
			
		]
	}

    //  OMNI- 3676 BOPIS: Apply All Staging Locations END
	
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
methodName : "extn_afterScreenInit"

 
}
}
,
// OMNI- 3676 BOPIS: Apply All Staging Locations START 
{
	  eventId: 'afterScreenLoad'

,	  sequence: '51'

,	  description: 'extn_setFocusOnStagingLocation'



,handler : {
methodName : "extn_setFocusOnStagingLocation"

 
}
}
,
{
	  eventId: 'afterScreenLoad'

,	  sequence: '52'




,handler : {
methodName : "printerIDFromSession"

 
}
}
,
{
	  eventId: 'saveCurrentPage'

,	  sequence: '19'




,handler : {
methodName : "extn_beforeSave"

 
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

// OMNI- 3676 BOPIS: Apply All Staging Locations START 
,
{
	  eventId: 'extn_StagingLoc_onKeyDown'

,	  sequence: '51'

,	  description: 'extn_AssignStagingLocationtoAllOnEnter'



,handler : {
methodName : "extn_AssignStagingLocationtoAllOnEnter"

 
}
}
,
{
	  eventId: 'extn_assignALLLoc_onClick'

,	  sequence: '51'

,	  description: 'extn_AssignStagingLocationtoAll'



,handler : {
methodName : "extn_AssignStagingLocationtoAll"

 
}
}
,
{
	  eventId: 'extn_filteringselect_onChange'

,	  sequence: '51'




,handler : {
methodName : "storePrinterInSession"

 
}
}

]
}

});
});


