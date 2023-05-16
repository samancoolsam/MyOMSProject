
scDefine(["dojo/text!./templates/CustomerAppeasementSelectionExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/utils/BaseUtils"]
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
			    _scBaseUtils
){
return _dojodeclare("extn.order.customerAppeasement.CustomerAppeasementSelectionExtnUI",
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
	  description: "extn_getExistingAppeasementList_output"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_6'
						,
	  value: 'extn_getExistingAppeasementList_output'
						
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

,	  description: 'extn_SetAppeasementReasonCodes'



,handler : {
methodName : "extn_SetAppeasementReasonCodes"

 
}
}
,
{
	  eventId: 'saveCurrentPage'

,	  sequence: '19'

,	  description: 'extn_validateAppeasementOnOrder'



,handler : {
methodName : "extn_validateAppeasementOnOrder"

 
}
}

]
}

});
});


