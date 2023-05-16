
scDefine(["scbase/loader!dojo/_base/declare","scbase/loader!extn/components/shipment/container/pack/ContainerPackContainerLinesExtnUI"]
,
function(			 
			    _dojodeclare
			 ,
			    _extnContainerPackContainerLinesExtnUI
){ 
	return _dojodeclare("extn.components.shipment.container.pack.ContainerPackContainerLinesExtn", [_extnContainerPackContainerLinesExtnUI],{
	// custom code here
	
	/* This OOB method has been overridden to nullify the on click event of image link in products tab */
	openProductDetails: function(
        event, bEvent, ctrl, args) {
            //do nothing
        }
});
});

