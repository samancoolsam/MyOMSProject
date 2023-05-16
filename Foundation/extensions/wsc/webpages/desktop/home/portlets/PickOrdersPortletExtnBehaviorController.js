


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/desktop/home/portlets/PickOrdersPortletExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnPickOrdersPortletExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.desktop.home.portlets.PickOrdersPortletExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.desktop.home.portlets.PickOrdersPortletExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_SFSOrdersCount'
,
		 mashupId : 			'extn_SFSOrdersCount'
,
		 extnType : 			'ADD'

	}

	]

}
);
});

