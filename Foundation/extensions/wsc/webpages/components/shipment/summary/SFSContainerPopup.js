scDefine([
	"dojo/text!./templates/SFSContainerPopup.html",
	"scbase/loader!dojo/_base/declare",
	"scbase/loader!sc/plat/dojo/widgets/Screen",
	"scbase/loader!sc/plat/dojo/utils/WidgetUtils",
	"scbase/loader!sc/plat/dojo/utils/ScreenUtils",
	"scbase/loader!sc/plat/dojo/utils/BaseUtils",
    "scbase/loader!dojo/_base/connect",
    "scbase/loader!dojo/dom-attr"
], function(
	templateText, _dojodeclare, _scScreen, _scWidgetUtils, _scScreenUtils, _scBaseUtils, dConnect, dDomAttr
) {
return _dojodeclare("extn.components.shipment.summary.SFSContainerPopup", [_scScreen], {
	templateString: templateText,
	uId: "SFSContainerPopup",
	packageName: "extn.components.shipment.summary",
	className: "SFSContainerPopup",
	namespaces: {
            targetBindingNamespaces: [{
                value: 'ContainerNoDataOutput',
                description: "Target Model for Container Number selected"
            }],
            sourceBindingNamespaces: [{
                value: 'ContainerNoData',
                description: "The options binding namespace  data of Container numbers"
            }]
        },
		hotKeys: [{
            id: "Popup_btnCancelContainer",
            key: "ESCAPE",
            description: "$(_scSimpleBundle:Close)",
            widgetId: "Popup_btnCancelContainer",
            invocationContext: "",
            category: "$(_scSimpleBundle:General)",
            helpContextId: ""
        }],
		events: [{
            name: 'saveCurrentPage'
        }, {
            name: 'reloadScreen'
        }],
		subscribers: {
            global: [{
                eventId: 'beforeContainerDialogClosed',
                sequence: '25',
                description: 'Subscriber for before the container dialog is closed',
                handler: {
                    methodName: "onPopupClose"
                }
            }],
            local: [{
                eventId: 'saveCurrentPage',
                sequence: '25',
                description: 'Subscriber for save current page event for wizard',
                handler: {
                    methodName: "save"
                }
            }, {
                eventId: 'Popup_btnNextContainer_onClick',
                sequence: '25',
                description: 'Next / Confirm Button Action',
                listeningControlUId: 'Popup_btnNextContainer',
                handler: {
                    methodName: "onPopupConfirm",
                    description: ""
                }
            }, {
                eventId: 'Popup_btnCancelContainer_onClick',
                sequence: '25',
                description: '',
                listeningControlUId: 'Popup_btnCancelContainer',
                handler: {
                    methodName: "onPopupClose",
                    description: ""
                }
            }
            ,
            {
                eventId: 'afterScreenInit'
                ,    
                sequence: '51'

                ,handler : {
                methodName : "extn_afterScreenInit"

                }
            }
            ]
        },
		
        handleMashupCompletion: function(
        mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
           /* _isccsBaseTemplateUtils.handleMashupCompletion(
            mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this); */
        },
		
        onPopupClose: function(
        event, bEvent, ctrl, args) {
            _scWidgetUtils.closePopup(
                this, "CLOSE", false);
            
        },
		
		onPopupConfirm: function(
        event, bEvent, ctrl, args) {
            var isValid = true;
            isValid = _scScreenUtils.validate(
            this);
            if (
            _scBaseUtils.equals(
            false, isValid)) {
                var msg = null;
                msg = _scScreenUtils.getString(
                this, "screenHasErrors");
               /* _isccsBaseTemplateUtils.showMessage(
                this, msg, "error", null);*/
            } else {
                this.onApply(
                event, bEvent, ctrl, args);
            }
        },
		
		onApply: function(
        event, bEvent, ctrl, args) {
           _scWidgetUtils.closePopup(
                this, "APPLY", false);
        },
		
        getPopupOutput: function(
        event, bEvent, ctrl, args) {
            var options = null;
            options = {};
            options["allowEmpty"] = true;
            var getSelectedContainer = null;
            getSelectedContainer = _scBaseUtils.getTargetModel(
            this, "ContainerNoDataOutput", options);
            return getSelectedContainer;
        },
		
		save: function(
        event, bEvent, ctrl, args) {
            var eventDefinition = null;
           /* eventDefinition = {};
            _scBaseUtils.setAttributeValue("argumentList", args, eventDefinition);
            _scEventUtils.fireEventToParent(
            this, "onSaveSuccess", eventDefinition); */
        },
		
		/* BOPIS 1571 */
        extn_afterScreenInit: function(event, bEvent, ctrl, args) {
            var containerNoFilter = this.getWidgetByUId("extn_ContainerNoFilter");
            this._addReadOnlyState();
            dConnect.connect(containerNoFilter, "openDropDown", this, "_addReadOnlyState");
            dConnect.connect(containerNoFilter, "closeDropDown", this, "_addReadOnlyState");

        },
		
        _removeReadOnlyState: function() {
            var containerNoFilter = this.getWidgetByUId("extn_ContainerNoFilter");
            dDomAttr.remove(containerNoFilter.textbox, "readonly");
            dDomAttr.remove(containerNoFilter.textbox, "readonly");
        },
		
        _addReadOnlyState: function() {
            var containerNoFilter = this.getWidgetByUId("extn_ContainerNoFilter");
            dDomAttr.set(containerNoFilter.textbox, "readonly", true);
            dDomAttr.set(containerNoFilter.textbox, "readonly", true);
        }
		/*BOPIS -1571 END */

});
});
