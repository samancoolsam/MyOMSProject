
scDefine(["dojo/text!./templates/PickOrdersPortletExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Label","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _scCurrencyDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.desktop.home.portlets.PickOrdersPortletExtnUI",
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
	  description: "Namespace for Label Count"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_2'
						,
	  value: 'extn_labelsCount'
						
			}
			,
			{
	  description: "Namespace for storing the SFS orders count after mashup call"
						,
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_3'
						,
	  value: 'extn_SFSOrdersOutput'
						
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
	  eventId: 'extn_btnViewPickupOrders_onClick'

,	  sequence: '51'

,	  description: 'showBatchList'



,handler : {
methodName : "showBatchList"

 
}
}
,
{
	  eventId: 'extn_lnkRefresh_onClick'

,	  sequence: '51'

,	  description: 'extnRefreshLink'



,handler : {
methodName : "extnRefreshLink"

 
}
}

]
}

});
});


