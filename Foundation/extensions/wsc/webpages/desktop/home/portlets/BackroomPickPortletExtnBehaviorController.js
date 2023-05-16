


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/desktop/home/portlets/BackroomPickPortletExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBackroomPickPortletExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.desktop.home.portlets.BackroomPickPortletExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.desktop.home.portlets.BackroomPickPortletExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'printPickTicket'
,
		 mashupId : 			'backroomPickPortlet_printPickTicket'
,
		 extnType : 			'MODIFY'

	}

	]

}
);
});

