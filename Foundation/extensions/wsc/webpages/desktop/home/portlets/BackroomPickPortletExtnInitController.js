


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/desktop/home/portlets/BackroomPickPortletExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBackroomPickPortletExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.desktop.home.portlets.BackroomPickPortletExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.desktop.home.portlets.BackroomPickPortletExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'backroomPickPortlet_getShipmentListPickCount'
,
		 sourceNamespace : 			''
,
		 mashupRefId : 			'getShipmentListReadyForPickInit'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

