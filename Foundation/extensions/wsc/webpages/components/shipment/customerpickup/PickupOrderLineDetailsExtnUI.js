
scDefine(["dojo/text!./templates/PickupOrderLineDetailsExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _scLink
){
return _dojodeclare("extn.components.shipment.customerpickup.PickupOrderLineDetailsExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [
//Start - OMNI-3680 BOPIS:Record Shortage Button
{
	  eventId: 'extn_screenbase_button_onClick'

,	  sequence: '51'

,	  description: 'This method is used to open shortage resolution popup'



,handler : {
methodName : "openShortageResolutionPopup"

 
}
}
//End - OMNI-3680 BOPIS:Record Shortage Button
]
}

});
});


