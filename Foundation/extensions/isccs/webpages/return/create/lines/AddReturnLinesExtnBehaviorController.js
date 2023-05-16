


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/return/create/lines/AddReturnLinesExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnAddReturnLinesExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.return.create.lines.AddReturnLinesExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.return.create.lines.AddReturnLinesExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'updateReturnOrder'
,
		 mashupId : 			'createReturn_changeReturnOrder'
,
		 extnType : 			'MODIFY'

	}
,
	 		{
		 mashupRefId : 			'processReturnOrder'
,
		 mashupId : 			'createReturn_processReturnOrder'
,
		 extnType : 			'MODIFY'

	}

	]

}
);
});

