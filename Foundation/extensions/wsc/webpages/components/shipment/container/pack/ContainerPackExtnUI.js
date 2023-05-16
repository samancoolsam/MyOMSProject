
scDefine(["dojo/text!./templates/ContainerPackExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel"]
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
return _dojodeclare("extn.components.shipment.container.pack.ContainerPackExtnUI",
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
//BOPIS-1576: Remove manual "Enter" hit after container weight input - begin
/*{
	eventId: 'saveCurrentPage'
,	sequence: '19'

	,handler : {
		methodName : "extn_containerWeightSaveBeforeFinishPack"
	}
}*/
//BOPIS-1576: Remove manual "Enter" hit after container weight input - end
,
{
	  eventId: 'extn_customFinishPack_onClick'

,	  sequence: '51'




,handler : {
methodName : "extnHandleFinishPack"

 
}
}
,
{
	  eventId: 'extn_customFinishPack2_onClick'

,	  sequence: '51'




,handler : {
methodName : "extnHandleFinishPack"

 
}
}

]
}

});
});


