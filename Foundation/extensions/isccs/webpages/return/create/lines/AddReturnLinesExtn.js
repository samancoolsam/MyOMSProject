
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/return/create/lines/AddReturnLinesExtnUI","scbase/loader!sc/plat/dojo/utils/BaseUtils","scbase/loader!sc/plat/dojo/utils/ScreenUtils","scbase/loader!sc/plat/dojo/utils/EventUtils","scbase/loader!isccs/utils/BaseTemplateUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils","scbase/loader!isccs/utils/ReturnUtils","scbase/loader!isccs/utils/UIUtils","scbase/loader!sc/plat/dojo/utils/ControllerUtils"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnAddReturnLinesExtnUI
			 ,
			    _scBaseUtils
			 ,
			    _scScreenUtils
			 ,
			    _scEventUtils
			 ,	
			    _isccsBaseTemplateUtils
			 , 
			     _scModelUtils
			 ,
		           _scWidgetUtils
			 ,
			    _isccsReturnUtils
			 ,
			   _isccsUIUtils
			 ,
				
			  _scControllerUtils
			 
){ 
	return _dojodeclare("extn.return.create.lines.AddReturnLinesExtn", [_extnAddReturnLinesExtnUI],{
	// custom code here

    extn_HideAddMoreOrders: function(event, bEvent, ctrl, args)
    {

      var screenMode = this.salesOrderMode;
      if(screenMode)
     {
      _scWidgetUtils.hideWidget(this,"btnFindLinesForReturn", false, null);
     }
    }

});
});

