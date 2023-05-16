


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/batch/batchlist/BatchListDetailsScreenExtn","scbase/loader!sc/plat/dojo/controller/ExtnServerDataController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBatchListDetailsScreenExtn
			 ,
			    _scExtnServerDataController
){

return _dojodeclare("extn.components.batch.batchlist.BatchListDetailsScreenExtnBehaviorController", 
				[_scExtnServerDataController], {

			
			 screenId : 			'extn.components.batch.batchlist.BatchListDetailsScreenExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 mashupRefId : 			'extn_ResetBatch'
,
		 mashupId : 			'extn_ResetBatch'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintBatch'
,
		 mashupId : 			'extn_PrintBatch'
,
		 extnType : 			'ADD'

	}
,
	 		{
		 mashupRefId : 			'extn_PrintBatchNew'
,
		 mashupId : 			'extn_PrintBatchNew'
,
		 extnType : 			'ADD'

	}

	]

}
);
});

