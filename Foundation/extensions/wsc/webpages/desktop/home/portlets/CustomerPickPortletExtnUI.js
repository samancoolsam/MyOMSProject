
scDefine(["dojo/text!./templates/CustomerPickPortletExtn.html","scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/_base/lang","scbase/loader!dojo/text","scbase/loader!idx/form/TextBox","scbase/loader!idx/layout/ContentPane","scbase/loader!sc/plat","scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder","scbase/loader!sc/plat/dojo/binding/SimpleDataBinder","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/widgets/Link"]
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
			    _idxTextBox
			 ,
			    _idxContentPane
			 ,
			    _scplat
			 ,
			    _scCurrencyDataBinder
			 ,
			    _scSimpleDataBinder
			 ,
			    _scBaseUtils
			 ,
			    _scLink
){
return _dojodeclare("extn.desktop.home.portlets.CustomerPickPortletExtnUI",
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

,	  sequence: '56'




,handler : {
methodName : "extn_initScreenHandler"

 
}
}
/*,
{
	  eventId: 'txtOrderNo_onKeyDown'

,	  sequence: '51'




,handler : {
methodName : "extn_onOrderNoTxtBoxKeyDown_pickUpOrderSearchActionIfPermitted"

 
}
}*/
,
{
	  eventId: 'extn_scanProduct_CustPickPortlet_onClick'

,	  sequence: '51'




,handler : {
methodName : "extn_BarcodeIconOnClick_pickUpOrderSearchActionIfPermitted"

 
}
}

]
}

});
});


