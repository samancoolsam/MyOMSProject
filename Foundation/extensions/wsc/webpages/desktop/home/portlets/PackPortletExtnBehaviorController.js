


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/desktop/home/portlets/PackPortletExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnPackPortletExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.desktop.home.portlets.PackPortletExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.desktop.home.portlets.PackPortletExtn'

			
			,

			
			
			 mashupRefs : 	[
	 		{
		 extnType : 			'MODIFY'
,
		 mashupId : 			'portlet_getShipmentList'
,
		 mashupRefId : 			'getShipmentList'

	}

	]
			
			
}
);
});

