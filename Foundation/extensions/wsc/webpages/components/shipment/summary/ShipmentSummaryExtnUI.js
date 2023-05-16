
scDefine(["dojo/text!./templates/ShipmentSummaryExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!idx/layout/TitlePane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _idxContentPane
			 ,
			    _idxTitlePane
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
return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
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
,
{
	  eventId: 'extn_CancelShipmentOtherStoresSummary_onClick'

,	  sequence: '51'

,	  description: 'extn_CancelShipmentOtherStoresSummary'



,handler : {
methodName : "extnCancelShipmentForOtherStores"

 
}
}
,
{
	  eventId: 'lnkStartCustomerPickup_onClick'

,	  sequence: '19'




,handler : {
methodName : "extn_RecordStoreUserAction"

 
}
}
<!--OMNI-72013 begin-->
,
{
	  eventId: 'extn_CompleteOnMyWaybutton_onClick'

,	  sequence: '51'




,handler : {
methodName : "extnOnCompleteClick"

 
}
}
,
<!--OMNI-72013 End-->

// OMNI-95718 Start
{
	  eventId: 'extn_button_Assembly_onClick'

,	  sequence: '51'

,	  description: 'Complete Assembly'



,handler : {
methodName : "extnCompleteAssemblyClick"

 
}
}

// OMNI-95718 END
]
}

});
});


