
scDefine(["dojo/text!./templates/BatchListDetailsScreenExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/layout/AdvancedTableLayout","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _scAdvancedTableLayout
			 ,
			    _scBaseUtils
			 ,
			    _scLink
){
return _dojodeclare("extn.components.batch.batchlist.BatchListDetailsScreenExtnUI",
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
	  eventId: 'extn_BatchReset_onClick'

,	  sequence: '51'

,	  description: 'extnResetBatch'



,handler : {
methodName : "extnResetBatch"

 
}
}
,
{
	  eventId: 'extn_PrintBatch_onClick'

,	  sequence: '51'

,	  description: 'extnPrintBatch'



,handler : {
methodName : "extnPrintBatch"

 
}
}
,
{
   eventId: 'afterScreenInit'

,   sequence: '51'




,handler : {
methodName : "extn_afterInitializeScreen"

 
}
}

]
}

});
});


