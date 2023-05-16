


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/components/batch/batchlist/BatchPickListExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnBatchPickListExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.components.batch.batchlist.BatchPickListExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.components.batch.batchlist.BatchPickListExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'batchPick_getBatchListForStore'
,
		 sourceNamespace : 			''
,
		 mashupRefId : 			'batchPick_getBatchListForStore'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

