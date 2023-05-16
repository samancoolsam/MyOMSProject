
scDefine(["dojo/text!./templates/ShipmentRTExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!ias/utils/BaseTemplateUtils","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _iasBaseTemplateUtils
			 ,
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.summary.ShipmentRTExtnUI",
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
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_1'
						,
	  value: 'extn_getPrinterDeviceInitMashup_output'
						
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
methodName : "checkIfHipPrinterAvailable"

 
}
}
,
{
	  eventId: 'lnk_RT_RecordCustomerPick_onClick'

,	  sequence: '19'




,handler : {
methodName : "extn_RecordStoreUserAction"

 
}
}
,
{
	  eventId: 'm_lnk_RT_RecordCustomerPick_onClick'

,	  sequence: '19'




,handler : {
methodName : "extn_RecordStoreUserAction"

 
}
}
,
{
	  eventId: 'extn_printOrderTicketLink_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_handlePrintOrderTicket"

 
}
}
,
{
	  eventId: 'extn_printOrderTicektMobile_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_handlePrintOrderTicket"

 
}
}
,
{
	  eventId: 'extn_printShippingLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintShippingLabelMethod'



,handler : {
methodName : "extnReprintShippingLabelMethod"

 
}
}
,
{
	  eventId: 'extn_m_lnk_printShippingLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintShippingLabelMethod'



,handler : {
methodName : "extnReprintShippingLabelMethod"

 
}
}
,
{
	  eventId: 'extn_printReturnLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintReturnLabelMethod'



,handler : {
methodName : "extnReprintReturnLabelMethod"

 
}
}
,
{
	  eventId: 'extn_printORMDLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintORMDLabelMethod'



,handler : {
methodName : "extnReprintORMDLabelMethod"

 
}
}
,
{
	  eventId: 'extn_m_lnk_printReturnLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintReturnLabelMethod'



,handler : {
methodName : "extnReprintReturnLabelMethod"

 
}
}
,
{
	  eventId: 'extn_m_lnk_printORMDLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintORMDLabelMethod'



,handler : {
methodName : "extnReprintORMDLabelMethod"

 
}
}
,
{
	  eventId: 'extn_m_lnk_printShippingLabel_onClick'

,	  sequence: '52'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}
,
{
	  eventId: 'extn_m_lnk_printReturnLabel_onClick'

,	  sequence: '52'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}
,
{
	  eventId: 'extn_m_lnk_printORMDLabel_onClick'

,	  sequence: '52'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}

,
{
	  eventId: 'extn_ChangePrinterMobile_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_changePrinterSelection"

 
}
}
,
{
	  eventId: 'extn_ChangePrinterMobile_onClick'

,	  sequence: '52'




,handler : {
methodName : "getPopupOutput"

 
}
}
,
{
	  eventId: 'extn_printOrderTicektMobile_onClick'

,	  sequence: '52'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}
,
{
	  eventId: 'extn_ChangePrinterMobile_onClick'

,	  sequence: '53'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}
,
{
	  eventId: 'extn_ChangePrinterLink_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_changePrinterSelection"

 
}
}
,
{
	  eventId: 'extn_ChangePrinterLink_onClick'

,	  sequence: '52'




,handler : {
methodName : "getPopupOutput"

 
}
}
,
{
	  eventId: 'extn_m_link_Reprint_STS_Shipping_Label_onClick'

,	  sequence: '51'

,	  description: 'extnReprintSTSShippingLabelMethod'



,handler : {
methodName : "extnReprintSTSShippingLabelMethod"

 
}
},
{
	  eventId: 'extn_link_reprintSTSShippingLabel_onClick'

,	  sequence: '51'

,	  description: 'extnReprintSTSShippingLabelMethod'



,handler : {
methodName : "extnReprintSTSShippingLabelMethod"

 
}
}
,
{
	  eventId: 'extn_m_link_Reprint_STS_Shipping_Label_onClick'

,	  sequence: '52'




,handler : {
methodName : "toggleMobileRelatedTask"
, className :  "BaseTemplateUtils" 
, packageName :  "ias.utils" 
 
}
}

]
}

});
});


