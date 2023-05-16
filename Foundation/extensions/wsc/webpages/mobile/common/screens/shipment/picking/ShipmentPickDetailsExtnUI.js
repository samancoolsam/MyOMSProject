
scDefine(["dojo/text!./templates/ShipmentPickDetailsExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _idxContentPane
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
return _dojodeclare("extn.mobile.common.screens.shipment.picking.ShipmentPickDetailsExtnUI",
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
	  eventId: 'afterBehaviorMashupCall'

,	  sequence: '51'




,handler : {
methodName : "extn_afterBehaviorMashupCall"

 
}
}
// OMNI - 9236 - Ship to Store Order Search "customer pick up" - START
,
{
	  eventId: 'lnk_RecordCustomerPickupAction_onClick'

,	  sequence: '19'

,	  description: 'extn_startCustomerPickProcessForContainer'



,handler : {
methodName : "extn_startCustomerPickProcessForContainer"

 
}
}
,
{
	  eventId: 'lnk_RecordCustomerPickupAction_onClick'

,	  sequence: '18'




,handler : {
methodName : "extn_RecordStoreUserAction"

 
}
}
,

//OMNI-96066 START
{
	  eventId: 'extn_screenbase_link_Assembly_onClick'

,	  sequence: '51'




,handler : {
methodName : "click_Assembly_btn"

 
}
}
,
{
	  eventId: 'extn_Receive_link_onClick'

,	  sequence: '51'




,handler : {
methodName : "receiveClick"

 
}
}

]
}

});
});


