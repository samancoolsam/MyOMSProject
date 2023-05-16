
scDefine(["dojo/text!./templates/HomeExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _scplat
			 ,
			    _scButtonDataBinder
			 ,
			    _scBaseUtils
){
return _dojodeclare("extn.desktop.home.HomeExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [

	//BOPIS-1574_CR: Single Home Screen refresh button - begin
	{
		  eventId: 'extn_HomeRefresh_link_onClick'

	,	  sequence: '51'


	,handler : {
	methodName : "extn_refreshHomeScreen_method"

	 
	}
	}
	//BOPIS-1574_CR: Single Home Screen refresh button - end


]
}

});
});


