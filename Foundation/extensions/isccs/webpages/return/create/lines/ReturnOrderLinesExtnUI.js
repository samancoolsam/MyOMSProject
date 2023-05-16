
scDefine(["dojo/text!./templates/ReturnOrderLinesExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!gridx/Grid","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/GridxDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _gridxGrid
			 ,
			    _scplat
			 ,
			    _scGridxDataBinder
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.return.create.lines.ReturnOrderLinesExtnUI",
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
	  eventId: 'OLST_listGrid_ScRowSelect'

,	  sequence: '51'




,handler : {
methodName : "extnEGCLineCheck"

 
}
}

]
}

});
});


