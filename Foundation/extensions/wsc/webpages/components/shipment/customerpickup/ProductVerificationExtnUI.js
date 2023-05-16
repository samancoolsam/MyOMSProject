
scDefine(["dojo/text!./templates/ProductVerificationExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/ImageDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Image","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _scImageDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scImage
			 ,
			    _scLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.customerpickup.ProductVerificationExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
			{
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_3'
						,
	  value: 'extn_Test'
						
			}
			,
			{
	  description: "This namespace contains customer verification method notes."
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_4'
						,
	  value: 'extn_CustomerVerificationNotes_Input'
						
			}
			,
			{
	  description: "This namespace contains customer verification method selected."
						,
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_5'
						,
	  value: 'extn_CustomerVerificationMethodSelected'
						
			}
			
		],
		sourceBindingNamespaces :
		[
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_4'
						,
	  value: 'extn_customerNS'
						
			}
			,
			{
	  description: "Default Customer Name"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_5'
						,
	  value: 'extn_DefaultCustomerName'
						
			}
			,
			{
	  description: "extn_CustomerVerficationMethodList"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_5'
						,
	  value: 'extn_CustomerVerficationMethodList'
						
			}
			,
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_6'
						,
	  value: 'extn_resetCurbsideOrders_output'
						
			},
			//OMNI-105674 - Start
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_9'
						,
	  value: 'extn_resetInstoreOrders_output'
						
			}
			//OMNI-105674 - End
			
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
methodName : "updateCustomerDropDown"

 
}
}
,
{
	  eventId: 'afterScreenLoad'

,	  sequence: '51'




,handler : {
methodName : "extn_afterScreenLoad"

 
}
}
,
{
	  eventId: 'afterScreenInit'

,	  sequence: '51'




,handler : {
methodName : "extn_afterScreenInit"

 
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
	  eventId: 'extn_customer_piickup_button_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_customer_piickup_button_onClick"

 
}
}
,
{
	  eventId: 'afterBehaviorMashupCall'

,	  sequence: '51'




,handler : {
methodName : "afterBehaviorMashupCall"


 
}
}
,
{
	  eventId: 'extn_FinishPick_button_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}
,
{
	  eventId: 'extn_FinishPick_button2new_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}
,
{
	  eventId: 'extn_FinishPickMobile_link_onClick'

,	  sequence: '51'


// START - (OMNI - 1409)  : BOPIS: Finish Pickup Button for BOPIS orders
,handler : {
methodName : "extn_onFinishPickButtonClick"
}
}
// END - (OMNI - 1409)  : BOPIS: Finish Pickup Button for BOPIS orders
,
{
	  eventId: 'extn_Previous_button_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onPreviousButtonClick"

 
}
}
,
{
	  eventId: 'extn_Previous_button2new_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onPreviousButtonClick"

 
}
}
,
{
	  eventId: 'extn_Close_button_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onCloseButtonClick"

 
}
}
,
{
	  eventId: 'extn_Close_button2new_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onCloseButtonClick"

 
}
}
,
{
	  eventId: 'extn_filteringselect_onChange'

,	  sequence: '51'




,handler : {
methodName : "customerDropDown"

 
}
}
,
{
	  eventId: 'extn_FinishPickuButton_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}
,
{
	  eventId: 'extn_FinishPickupButton1_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}
,
{
	  eventId: 'extn_resetCurbsideOrder1_onClick'


,	  sequence: '51'




,handler : {
methodName : "resetCurbsideOrder"

 
}
}
,
{
	  eventId: 'extn_resetCurbsideButton_onClick'

,	  sequence: '51'




,handler : {
methodName : "resetCurbsideOrder"

 
}
}
,
{
	  eventId: 'extn_FinishPickupButton_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}
,
{
	  eventId: 'extn_FinishPickUpButton1_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onFinishPickButtonClick"

 
}
}

//OMNI-79565 Delay Confirmation PopUp - START 
,
{
	  eventId: 'extn_EstimatedDelay1_onClick'
,	  sequence: '51'
,handler : {
methodName : "extn_EstimatedDelay1_onClick"
}
}
,
{
	  eventId: 'extn_EstimatedDelay2_onClick'
,	  sequence: '51'
,handler : {
methodName : "extn_EstimatedDelay2_onClick"
}
}
,
{
	  eventId: 'extn_EstimatedDelay3_onClick'
,	  sequence: '51'
,handler : {
methodName : "extn_EstimatedDelay3_onClick"
}
}
,
{
	  eventId: 'extn_EstimatedDelay4_onClick'
,	  sequence: '51'
,handler : {
methodName : "extn_EstimatedDelay4_onClick"
}
}
//OMNI-79565 Delay Confirmation PopUp - END 
//OMNI -80092 - Assign Curbside Order - Start
,
{
	  eventId: 'extn_assigncurbsideorder_button_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_onassigncurbsideorder_button_onClick"

 
}
}

//OMNI -80092 - Assign Curbside Order - End
,
//OMNI-105500 - Start
{
	  eventId: 'extn_resetInstoreOrder_onClick'

,	  sequence: '51'




,handler : {
methodName : "resetInstoreOrder"

 
}
}
//OMNI-105500 - End
]
}

});
});


