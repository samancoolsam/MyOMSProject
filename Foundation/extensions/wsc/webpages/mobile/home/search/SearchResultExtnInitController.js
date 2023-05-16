


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/mobile/home/search/SearchResultExtn","scbase/loader!sc/plat/dojo/controller/ExtnScreenController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnSearchResultExtn
			 ,
			    _scExtnScreenController
){

return _dojodeclare("extn.mobile.home.search.SearchResultExtnInitController", 
				[_scExtnScreenController], {

			
			 screenId : 			'extn.mobile.home.search.SearchResultExtn'

			
			
			
			
			
						,

			
			
			 mashupRefs : 	[
	 		{
		 sourceBindingOptions : 			''
,
		 sequence : 			''
,
		 mashupId : 			'mobileHomeSearch_getShipmentList'
,
		 sourceNamespace : 			''
,
		 mashupRefId : 			'getShipmentListInit'
,
		 extnType : 			'MODIFY'
,
		 callSequence : 			''

	}

	]

}
);
});

