
scDefine(["dojo/text!./templates/ShipmentSummaryContainerListExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel"]
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
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
){
return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryContainerListExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [
//OMNI-6583 Mobile Store - STS - TO Detailed Receiving Report Screen Start
{
	  eventId: 'afterScreenInit'

,	  sequence: '51'

,	  description: 'extn_intializescreen'



,handler : {
methodName : "extn_intializescreen"

 
}
}
,
{
	  eventId: 'extn_PendingContainerLink_onClick'

,	  sequence: '51'

,	  description: 'extn_openTOSummaryScreen'



,handler : {
methodName : "extn_openTOSummaryScreen"

 
}
}
//OMNI-6583 Mobile Store - STS - TO Detailed Receiving Report Screen END
]
}

});
});


