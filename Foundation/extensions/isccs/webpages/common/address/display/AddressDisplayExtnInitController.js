


scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!dojo/_base/kernel","scbase/loader!dojo/text","scbase/loader!extn/common/address/display/AddressDisplayExtn","scbase/loader!sc/plat/dojo/controller/IdentifierController"]
 , function(			 
			    _dojodeclare
			 ,
			    _dojokernel
			 ,
			    _dojotext
			 ,
			    _extnAddressDisplayExtn
			 ,
			    _scIdentifierController
){

return _dojodeclare("extn.common.address.display.AddressDisplayExtnInitController", 
				[_scIdentifierController], {

			
			 screenId : 			'extn.common.address.display.AddressDisplayExtn'
,
			 identifierTemplatesRootPath : 			'extn.common.address.display.identifiers'
,
			 baseTemplateFolder : 			'extn.common.address.display.templates'
,
			 className : 			'AddressDisplayExtn'

			
			
			
}
);
});

