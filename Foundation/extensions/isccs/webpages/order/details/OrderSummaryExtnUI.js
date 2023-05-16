
scDefine(["dojo/text!./templates/OrderSummaryExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!gridx/Grid","scbase/loader!gridx/modules/ColumnResizer","scbase/loader!gridx/modules/ColumnWidth","scbase/loader!gridx/modules/HLayout","scbase/loader!gridx/modules/select/Row","scbase/loader!idx/layout/ContentPane","scbase/loader!isccs/common/address/display/AddressDisplayInitController","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/GridxDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/ControllerWidget","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/IdentifierControllerWidget","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _gridxColumnResizer
			 ,
			    _gridxColumnWidth
			 ,
			    _gridxHLayout
			 ,
			    _gridxRow
			 ,
			    _idxContentPane
			 ,
			    _isccsAddressDisplayInitController
			 ,
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scGridxDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scControllerWidget
			 ,
			    _scDataLabel
			 ,
			    _scIdentifierControllerWidget
			 ,
			    _scLink
){
return _dojodeclare("extn.order.details.OrderSummaryExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	<!--OMNI-63466 Begin-->
					,	
	namespaces : {
		targetBindingNamespaces :
		[
		],
		sourceBindingNamespaces :
		[
			{
	  scExtensibilityArrayItemId: 'extn_SourceNamespaces_3'
						,
	  value: 'extn_CnclReason_output'
						
			}
			
		]
	}
<!--OMNI-63466 End-->
	
	,
	hotKeys: [ 
	]

,events : [
	]

,subscribers : {

local : [

{






 
	  eventId: 'imgOnHold_imageClick'

,	  sequence: '51'

,	  description: 'On Click of Hold'



,handler : {
methodName : "extn_ResolveHoldWizard_onClickHandler"

 
}
}

]
}

});
});


