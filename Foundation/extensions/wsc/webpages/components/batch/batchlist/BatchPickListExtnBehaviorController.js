


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/batch/batchlist/BatchPickListExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBatchPickListExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.batch.batchlist.BatchPickListExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.batch.batchlist.BatchPickListExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_manageUserUiState_batchSortMethod'
,
		 mashupId : 			'manageUserUiState_batchSortMethod'
,
		 extnType : 			'ADD'

	}

	]

}
);
});

