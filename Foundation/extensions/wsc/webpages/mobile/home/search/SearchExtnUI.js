scDefine(["dojo/text!./templates/SearchExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/CheckBox","scbase/loader!idx/form/FilteringSelect","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/TitlePane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CheckBoxDataBinder","scbase/loader!sc/plat/dojo/binding/ComboDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/ControllerWidget","scbase/loader!sc/plat/dojo/widgets/IdentifierControllerWidget","scbase/loader!wsc/mobile/home/search/AdvancedSearchInitController"]
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
			   _idxCheckBox
			 ,
			    _idxFilteringSelect
			 ,
			    _idxTextBox
			 ,
			    _idxTitlePane
			 ,
			    _scplat
			 ,
			   _scCheckBoxDataBinder
			 ,
			    _scComboDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scControllerWidget
			 ,
			    _scIdentifierControllerWidget
			 ,
			    _wscAdvancedSearchInitController
){
return _dojodeclare("extn.mobile.home.search.SearchExtnUI",
				[], {
			templateString: templateText,
	hotKeys: [ 	],
	events : [	],
	subscribers : {
		local : [

{
	  eventId: 'afterScreenLoad',
	  sequence: '51',
	  handler : {
		methodName : "extn_afterScreenLoad"
}
}
,
{
	  eventId: 'extn_ship_or_order_no_onKeyUp'
,	  sequence: '51',
		handler : {
methodName : "searchOrdersOnEnter"

}
}
//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70  START
,
		{
		  eventId: 'extn_relatedOrders_checkbox_onClick',
		  sequence: '51',
		  description: 'On checkbox checked disable fields',
		  	handler : {
				methodName : "disableWidgets"
			}
		},
//OMNI-71126 Pick Up Order Grouping - Pick Up Order Grouping Order Search TC70  END

{
	  eventId: 'extn_ScanProductIDTextBox_onKeyUp'

,	  sequence: '51'

,	  description: 'scan or enter ProductID'



,handler : {
methodName : "scanProductOnEnter"

, description :  "scan or enter ProductID"  
}
}

	]
	}
});
});