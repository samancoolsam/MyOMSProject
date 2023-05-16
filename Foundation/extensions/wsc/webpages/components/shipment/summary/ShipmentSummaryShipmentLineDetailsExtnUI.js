//  OMNI-71678 - BOPIS: Apply update Staging Locations END
scDefine(["dojo/text!./templates/ShipmentSummaryShipmentLineDetailsExtn.html","scbase/loader!dijit/form/Button","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/ButtonDataBinder","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/DataLabel","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scButtonDataBinder
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scDataLabel
			 ,
			    _scLink
){
return _dojodeclare("extn.components.shipment.summary.ShipmentSummaryShipmentLineDetailsExtnUI",
				[], {
			templateString: templateText
	
	
	
	
	
	
	
					,	
	namespaces : {
		targetBindingNamespaces :
		[
			{
	  scExtensibilityArrayItemId: 'extn_TargetNamespaces_1'
						,
	  value: 'extn_HoldLocation_Add_staging'
						
			}
			
		],
		sourceBindingNamespaces :
		[
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
                eventId: 'afterScreenInit',
                sequence: '32',
                handler: {
                   methodName: "initializeScreen"
                }
			},

{
	  eventId: 'extn_btnEdit_onClick'

,	  sequence: '51'




,handler : {
methodName : "enableEditStagingLoc"

 
}
}
,
{
	  eventId: 'extn_btnUpdate_onClick'

,	  sequence: '51'




,handler : {
methodName : "assignStagingLocation"

 
}
}

]
}
 //  OMNI-71678 - BOPIS: Apply update Staging Locations END
});
});


