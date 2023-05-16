/*
 * Licensed Materials - Property of IBM
 * IBM Call Center for Commerce (5725-P82)
 * (C) Copyright IBM Corp. 2013 All Rights Reserved.
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */
scDefine(["dojo/text!./templates/ReceiveContainerEditor.html", "scbase/loader!dojo/_base/declare", "scbase/loader!dojo/_base/kernel", "scbase/loader!dojo/_base/lang", "scbase/loader!dojo/text", "scbase/loader!idx/layout/ContentPane", "scbase/loader!ias/utils/BaseTemplateUtils", "scbase/loader!ias/utils/EditorRelatedTaskUtils", "scbase/loader!ias/utils/EditorScreenUtils", "scbase/loader!ias/utils/UIUtils", "scbase/loader!ias/utils/WidgetUtils", "scbase/loader!sc/plat", "scbase/loader!sc/plat/dojo/binding/CurrencyDataBinder", "scbase/loader!sc/plat/dojo/binding/ImageDataBinder", "scbase/loader!sc/plat/dojo/layout/AdvancedTableLayout", "scbase/loader!sc/plat/dojo/utils/BaseUtils", "scbase/loader!sc/plat/dojo/utils/ControllerUtils", "scbase/loader!sc/plat/dojo/utils/EventUtils", "scbase/loader!sc/plat/dojo/utils/ModelUtils", "scbase/loader!sc/plat/dojo/utils/ScreenUtils", "scbase/loader!sc/plat/dojo/utils/WidgetUtils", "scbase/loader!sc/plat/dojo/widgets/Editor", "scbase/loader!sc/plat/dojo/widgets/Image", "scbase/loader!sc/plat/dojo/widgets/Label", "scbase/loader!sc/plat/dojo/widgets/Link"], function(
templateText, _dojodeclare, _dojokernel, _dojolang, _dojotext, _idxContentPane, _isccsBaseTemplateUtils, _isccsEditorRelatedTaskUtils, _isccsEditorScreenUtils, _isccsUIUtils, _isccsWidgetUtils, _scplat, _scCurrencyDataBinder, _scImageDataBinder, _scAdvancedTableLayout, _scBaseUtils, _scControllerUtils, _scEventUtils, _scModelUtils, _scScreenUtils, _scWidgetUtils, _scEditor, _scImage, _scLabel, _scLink) {
    return _dojodeclare("extn.mobile.editors.ReceiveContainerEditor", [_scEditor], {
        templateString: templateText,
        postMixInProperties: function() {
            if (this.getScreenMode() != "default") {
                var origArgs = arguments;
                var htmlName = "templates/ReceiveContainerEditor_" + this.getScreenMode() + ".html";
                this.templateString = dojo.cache("extn.mobile.editors", htmlName);
                var modeUIJSClassString = "extn.mobile.editors.ReceiveContainerEditor_" + this.getScreenMode();
                var that = this;
                var _scUtil = _dojolang.getObject("dojo.utils.Util", true, _scplat);
                _scUtil.getInstance(modeUIJSClassString, null, null, function(instance) {
                    _scBaseUtils.screenModeMixin(that, instance);
                    that.inherited(origArgs);
                });
            }
        },
        baseTemplate: {
            url: _dojokernel.moduleUrl("extn.editors.templates", "ReceiveContainerEditor.html"),
            shared: true
        },
        uId: "ReceiveContainerEditor",
        packageName: "extn.editors",
        className: "ReceiveContainerEditor",
        extensible: true,
        title: "Receive Container",
        namespaces: {
            targetBindingNamespaces: [],
            sourceBindingNamespaces: [{
                description: 'Holds the initial editor input passed into the editor on open.',
                value: 'InitialEditorInput'
            }, {
                description: 'Holds a list of organizations used to create a new order.',
                value: 'getOrganizationList_output'
            }, 
			// {
                // description: 'Holds a list of enterprises the user can access.',
                // value: 'getEnterpriseList'
            // },
			{
                description: 'Used to display the enterprise context information.',
                value: 'enterpriseContext'
            }]
        },
        isRTscreenLoaded: 'null',
        newScreenData: null,
        isRTscreeninitialized: 'null',
        isWizardinitialized: 'null',
        comparisonAttributes: [],
        hotKeys: [],
        events: [{
            name: 'setSystemMessage'
        }, {
            name: 'resizeEditor'
        }, {
            name: 'setEditorInput'
        }, {
            name: 'setScreenTitle'
        }, {
            name: 'beforeEditorClosed'
        }],
        subscribers: {
            local: [{
                eventId: 'closeSystemMessage_onClick',
                sequence: '25',
                handler: {
                    methodName: "closeMessagePanel",
                    className: "BaseTemplateUtils",
                    packageName: "ias.utils"
                }
            }, {
                eventId: 'setEditorInput',
                sequence: '25',
                handler: {
                    methodName: "setEditorInput"
                }
            }, {
                eventId: 'setScreenTitle',
                sequence: '25',
                handler: {
                    methodName: "setScreenTitle",
                    description: ""
                }
            }, {
                eventId: 'beforeEditorClosed',
                sequence: '25',
                handler: {
                    methodName: "handleEditorClose",
                    className: "UIUtils",
                    packageName: "ias.utils",
                    description: ""
                }
            }, {
                eventId: 'afterScreenInit',
                sequence: '25',
                handler: {
                    methodName: "onScreenInit"
                }
            }, {
                eventId: 'linkclose_onClick',
                sequence: '30',
                description: '',
                listeningControlUId: 'linkclose',
                handler: {
                    methodName: "closeCustomerMessagePanel",
                    className: "BaseTemplateUtils",
                    packageName: "ias.utils"
                }
            }, {
                eventId: 'siteMapLink_onClick',
                sequence: '30',
                description: '',
                listeningControlUId: 'siteMapLink',
                handler: {
                    methodName: "openSiteMap",
                    className: "EditorScreenUtils",
                    packageName: "ias.utils",
                    description: ""
                }
            }],
        },
               
        onScreenInit: function(
        event, bEvent, ctrl, args) {
            var screenInput = null;
            screenInput = _scScreenUtils.getInitialInputData(
            this);
            this.setInitialEditorInput(
            screenInput);
            // _isccsUIUtils.getEnterpriseList(this);
        },
        setInitialEditorInput: function(
        screenInput) {
            if (!(
            _scBaseUtils.isVoid(
            _scModelUtils.getModelObjectFromPath("", screenInput)))) {
                _scWidgetUtils.showWidget(
                this, "pnlCustomerContext", false, null);
            }
            _scScreenUtils.setModel(
            this, "InitialEditorInput", screenInput, null);
        },
        setScreenTitle: function(
        event, bEvent, ctrl, args) {
            _isccsBaseTemplateUtils.updateScreenTitleOnEditor(
            this, bEvent);
        },
        getScreenTitle: function() {
            var returnVal = "";
            returnVal = _scScreenUtils.getString(
            this, "Receive Container");
            return returnVal;
        },
        beforeOpenScreenInEditor: function(
        data) {
            _isccsEditorScreenUtils.beforeOpenSearchScreenInEditor(
            data);
        },
        handleResponseForReplaceScreen: function(
        res, args) {
            if (
            _scBaseUtils.equals(
            res, "Ok")) {
                _scScreenUtils.clearScreen(
                _isccsUIUtils.getCurrentWizardInstance(
                this), null);
                if (
                _scBaseUtils.isVoid(
                args)) {
                    _scControllerUtils.continueOpeningInEditor(
                    this.newScreenData);
                } else {
                    _scControllerUtils.continueOpeningInEditor(
                    args);
                }
            }
        },
        setEditorInput: function(
        event, bEvent, ctrl, args) {
            var model = null;
            model = _scBaseUtils.getAttributeValue("model", false, args);
            if (!(
            _scBaseUtils.isVoid(
            model))) {
                var wiz = null;
                _scScreenUtils.setInitialInputData(
                this, model);
                this.setInitialEditorInput(
                model);
                var eventDefn = null;
                eventDefn = _scBaseUtils.getNewBeanInstance();
                _scBaseUtils.setAttributeValue("argumentList", _scBaseUtils.getNewBeanInstance(), eventDefn);
                _isccsEditorScreenUtils.updateEditorTab(
                this);
                wiz = _isccsUIUtils.getCurrentWizardInstance(
                this);
                if (!(
                _scBaseUtils.isVoid(
                wiz))) {
                    _scEventUtils.fireEventInsideScreen(
                    wiz, "setWizardInput", eventDefn, args);
                }
                this.handleEnterpriseContextDisplay();
            }
        },
        handleMashupOutput: function(
        mashupRefId, modelOutput, mashupInput, mashupContext) {
            
        },
        handleMashupCompletion: function(
        mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data) {
            _isccsBaseTemplateUtils.handleMashupCompletion(
            mashupContext, mashupRefObj, mashupRefList, inputData, hasError, data, this);
        }
        
        
       
       /* onReturnEnterpriseSelection: function(
        actionPerformed, model, popupParams) {
            if (!(
            _scBaseUtils.equals(
            actionPerformed, "CLOSE"))) {
                _isccsUIUtils.openWizardInEditor("isccs.return.wizards.createReturn.CreateReturnWizard", model, "isccs.editors.ReturnEditor", this);
            }
        }*/
       
    });
});