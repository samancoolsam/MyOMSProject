<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@page import="com.ibm.wsc.core.WSCConstants"%>
<%@page import="com.ibm.wsc.core.utils.WSCSessionUtils"%>
<%@page import="com.sterlingcommerce.security.dv.SCEncoder"%>
<%@page import="com.sterlingcommerce.ui.web.framework.utils.SCUIContextHelper"%>
<%@page import="com.sterlingcommerce.ui.web.framework.context.SCUIContext"%>
<%@page import="com.sterlingcommerce.ui.web.framework.utils.SCUIUtils"%>
<%@page import="com.sterlingcommerce.ui.web.framework.utils.SCUIJSONUtils"%>
<%@page import="com.sterlingcommerce.ui.web.platform.utils.SCUIPlatformUtils"%>
<%@page import="com.sterlingcommerce.baseutil.SCUtil"%>
<%@page import="com.sterlingcommerce.baseutil.SCXmlUtil"%>
<%@page import="com.sterlingcommerce.ui.web.framework.helpers.SCUIMashupHelper"%>
<%@page import="com.sterlingcommerce.ui.web.framework.helpers.SCUILocalizationHelper"%>
<%@page import="com.sterlingcommerce.ui.web.framework.extensibility.SCUIExtensibilityWorkbenchHelper"%>
<%@page import="com.ibm.isccs.core.utils.SCCSMobileUtils" %>
<%@page import="com.yantra.yfc.dom.YFCElement"%>
<%@page import="com.yantra.yfc.log.YFCLogCategory"%>
<%@page import="com.yantra.yfc.ui.backend.util.APIManager"%>
<%@page import="org.w3c.dom.Element"%>
<%@page import="org.w3c.dom.Document"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.InputStream"%>
<%@page import="java.io.InputStreamReader"%>
<%@page import="java.io.IOException"%>
<%@page import="com.yantra.yfc.ui.backend.util.YFCConsoleUtil"%>
<%@ taglib uri="/WEB-INF/scui.tld" prefix="scuitag" %>
<%@ taglib uri="/WEB-INF/scuiimpl.tld" prefix="scuiimpltag" %>
<%@ taglib uri="/WEB-INF/isccs.tld" prefix="isccstag" %>

<%!	
	YFCLogCategory logger = YFCLogCategory.instance(this.getClass().getName());	
	public String getConfigData(SCUIContext uiContext,String appConfigName){
	
		String path="";
		if("AppConfig".equals(appConfigName)){
			path = "/wsc/container/appConfig.json";
		}
		else if("MobileAppConfig".equals(appConfigName)){
			path = "/wsc/mobile/container/appConfig.json";
		}
		else if("ShipmentStatusConfig".equals(appConfigName)){
			path = "/wsc/components/shipment/search/ShipmentStatusList.json";
		}
		else if("PaginationSizeConfig".equals(appConfigName)){
			path = "/wsc/common/pagination/PaginationSize.json";
		}
		else if("EditorMapConfig".equals(appConfigName)){
			path = "/wsc/common/EditorMap.json";
		}
		else if("BatchLineStatusConfig".equals(appConfigName)){
			path = "/wsc/components/batch/batchpick/common/BatchLineStatusList.json";
		} 
		else if("AlertTypeMap".equals(appConfigName)){
			path = "/wsc/common/alerts/AlertTypeMap.json";
		} 
		else if("OrderSearchConfig".equals(appConfigName)) {
			path = "/wsc/mobile/home/search/OrderSearch.json";
		}
		else{
			path = "";
		}	
	
		if(!SCUIUtils.isVoid(path)){
			String appConfigfilePath = SCUIUtils.getExtendedPath(uiContext,path);
			BufferedReader br = null;
			StringBuffer appConfigData = new StringBuffer();
				try {
					String s = null;
					InputStream is = uiContext.getSession().getServletContext().getResourceAsStream(appConfigfilePath);
					br = new BufferedReader(new InputStreamReader(is));
					
				while((s = br.readLine()) != null){
					appConfigData.append(s);
				}
			} catch (IOException e) {
				logger.error(e);
			} finally{
				try {
					br.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
			return appConfigData.toString();
		}
		else{
			return "{}";
		}
	}
%>

<%
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
	response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
	response.setDateHeader("Expires", 0); // Proxies.

	//Get data from query string
	String shipmentKey = request.getParameter(WSCConstants.SHIPMENT_KEY);
	String orderHeaderKey = request.getParameter(WSCConstants.ORDER_HEADER_KEY);
	String screenID = request.getParameter(WSCConstants.SCREEN_ID);
	
	if(SCUIUtils.isVoid(shipmentKey)){
		shipmentKey="";
	}
	if(SCUIUtils.isVoid(orderHeaderKey)){
		orderHeaderKey="";
	}
	if(SCUIUtils.isVoid(screenID)){
		screenID="";
	}
		
	String developerproductivitymode = System.getProperty("developerproductivitymode");
	Boolean devProdMode = SCUIUtils.isTrue(developerproductivitymode);
	
	SCUIContext uiContext = SCUIContextHelper.getUIContext(request, response);
	uiContext.getSession().setAttribute("isMobile","true");

	String deviceType = SCCSMobileUtils.getDeviceType(request);
	boolean mobileEnv = (deviceType != SCCSMobileUtils.PC);

	WSCSessionUtils.setObjInSession(request.getSession(false), WSCConstants.LOGIN_TYPE, WSCConstants.LOGIN_TYPE_MOBILE);
	
	YFCElement eStore = (YFCElement)WSCSessionUtils.getObjFromSession(request.getSession(false), WSCConstants.SESSION_CURRENT_STORE);
	String currentStore = eStore.getAttribute("ShipNode");
	String enterpriseCode = eStore.getAttribute("EnterpriseCode");
	String currentStoreName = eStore.getAttribute("StoreName");
	SCUIPlatformUtils.changeLocaleForUser(uiContext, eStore.getAttribute("LocaleCode").toString());
	String localeCode = uiContext.getUserPreferences().getLocale().getLocaleCode().toString().replace('_', '-').toLowerCase();
	String localeCharOrientation= uiContext.getUserPreferences().getLocaleCharacterOrientation().toString().replace('_', '-').toLowerCase();

	//Strip the -Ext at the end of the locale.
	//If the locale string contains a - then take the first 5 letters xx-xx
	//Else leave it as is to avoid  
	if (localeCode.length()>5 && localeCode.contains("-"))
		localeCode=localeCode.substring(0,5);
	
		
	YFCElement eUser = (YFCElement)WSCSessionUtils.getObjFromSession(request.getSession(false), WSCConstants.SESSION_CURRENT_USER);
	String sUserName = eUser.getAttribute("Username");
	String loginid = eUser.getAttribute("Loginid");
	
	String reqLocaleCode = (String)session.getAttribute(WSCConstants.LOGIN_STORE_LOCALE_CODE);
	String reqShipNode = (String)session.getAttribute(WSCConstants.LOGIN_STORE_ID);
	String reqEnterpriseCode = (String)session.getAttribute(WSCConstants.LOGIN_ENTERPRISE_CODE);
	
	if(SCUIUtils.isVoid(reqLocaleCode)){
		reqLocaleCode="";
	}
	if(SCUIUtils.isVoid(reqShipNode)){
		reqShipNode="";
	}
	
	Cookie storeCookie = new Cookie (WSCConstants.LOGIN_STORE_ID,reqShipNode);	
	Cookie localeCodeCookie = new Cookie (WSCConstants.LOGIN_STORE_LOCALE_CODE,reqLocaleCode);
	
	storeCookie.setPath(request.getContextPath());
	localeCodeCookie.setPath(request.getContextPath());
	
	response.addCookie(storeCookie);
	response.addCookie(localeCodeCookie);	
	
	Element getRulesDetails = SCXmlUtil.createDocument("Rules").getDocumentElement();
	YFCElement eCurrentOrg = (YFCElement)WSCSessionUtils.getObjFromSession(request.getSession(false), "CurrentOrganization");
	String sOrgCode = eCurrentOrg.getAttribute("OrganizationCode");
	getRulesDetails.setAttribute("CallingOrganizationCode", sOrgCode);
	getRulesDetails.setAttribute("RuleSetFieldName","WSC_SHOW_ITEM_IMAGES");
	Element eGetRulesDetails = (Element) SCUIMashupHelper.invokeMashup("dataprovider_getRuleDetails", getRulesDetails, uiContext);
	String strRuleJson = SCUIJSONUtils.getJSONFromXML(eGetRulesDetails, uiContext);
	
  	Element getRulesDetailsAlertPolling = SCXmlUtil.createDocument("Rules").getDocumentElement();
	getRulesDetailsAlertPolling.setAttribute("CallingOrganizationCode", sOrgCode);
	getRulesDetailsAlertPolling.setAttribute("RuleSetFieldName","WCC_ALERT_POLLING_INTERVAL");
	Element eGetRulesDetailsAlertPolling = (Element) SCUIMashupHelper.invokeMashup("dataprovider_getRuleDetails", getRulesDetailsAlertPolling, uiContext);
	String strRuleJsonPolling = SCUIJSONUtils.getJSONFromXML(eGetRulesDetailsAlertPolling, uiContext);
	
	Element getGotoSummaryOnCompleteRule = SCXmlUtil.createDocument("Rules").getDocumentElement();
	getGotoSummaryOnCompleteRule.setAttribute("CallingOrganizationCode", sOrgCode);
	getGotoSummaryOnCompleteRule.setAttribute("RuleSetFieldName","WSC_GOTO_SUMMARY_AFTERCOMPLETION");
	Element getGotoSummaryOnCompleteRuleOutput = (Element) SCUIMashupHelper.invokeMashup("dataprovider_getRuleDetails", getGotoSummaryOnCompleteRule, uiContext);
	String strRuleJsonGotoSummaryOnComplete = SCUIJSONUtils.getJSONFromXML(getGotoSummaryOnCompleteRuleOutput, uiContext);
		
	boolean isRight2LeftLanguage = false;
	String bidiOrientation = "ltr";
	String isRTL = uiContext.getRequest().getParameter("isRTL");
	String enablePerformanceLogging = uiContext.getRequest().getParameter("performanceLog");
	String performanceThreshold = uiContext.getRequest().getParameter("performanceThreshold");
	if(SCUtil.equals(isRTL, "Y") || SCUtil.equals(localeCharOrientation, "right-to-left")){
		isRight2LeftLanguage = true;
		bidiOrientation = "rtl";
	}
	String documentDomain = APIManager.getInstance().getProperty("yfs.document.domain");
	
	String onlineHelpUrl = YFCConsoleUtil.getAPPKCOnlineHelpPath(request, " ");
%>

<html lang="<%=localeCode%>">
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<%
			if ( deviceType == SCCSMobileUtils.HANDHELD ) {
		%>
			<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, minimal-ui">
		<%
			} else {
		%>
			<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<%
			}
		%>
		
		<title><%= SCUILocalizationHelper.getString(uiContext, "WSC_Product_Name")%></title>
		<script>
			<%
			if(!SCUtil.isVoid(documentDomain)){
			%>
			      document.domain = '<%=documentDomain%>';
			<%}
			%>
		</script>
		<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/wsc/resources/css/icons/images/IBMLogoWindow.ico"/>
		

		<scuitag:includeDojoJS profileID="wsc_mobile"/>
		<%
			if(devProdMode) {
		%>
		<script>
			require(["scbase/loader!<%=request.getContextPath()%>/dojo/dojo/dojoAll.js.uncompressed.js",
					"scbase/loader!<%=request.getContextPath()%>/ibmjs/idx/idxAll.js.uncompressed.js"
					
					],function(d0,d1) {
					//invokeDojoReady();
					}
			);
		</script>
		<%
			}
		%>
		<scuitag:includeDojoCSS isRtl="<%=new Boolean(isRight2LeftLanguage).toString()%>" profileID="wsc_mobile"/>
		<scuitag:inclDojoPlatformDependencies/>
		<scuiimpltag:inclDojoPlatformImplDependencies/>	

		<script>
			appRequire([
		           "scbase/loader!dojo/parser",		           
		           "scbase/loader!dojo/_base/connect",		          
				   "scbase/loader!sc/plat/dojo/ext/mainExtn",
				   "scbase/loader!idx/form/FilteringSelect",
		           "scbase/loader!sc/plat/dojo/utils/Util",
		           "scbase/loader!sc/plat/dojo/utils/BundleUtils",		          
		           "scbase/loader!wsc/mobile/widgets/MobileScreenContainer",
		           "scbase/loader!ias/container/containerUtil",
				   "scbase/loader!wsc/mobile/container/AppHeader",
				   "scbase/loader!ias/utils/ContextUtils",
				   "scbase/loader!ias/utils/UIUtils",
		           "scbase/loader!sc/plat/dojo/utils/LogUtils",
		           "scbase/loader!sc/plat/dojo/utils/ResourcePermissionUtils",
				   "scbase/loader!sc/plat/dojo/info/ApplicationInfo",	
				   "scbase/loader!dijit/DialogUnderlay",
				   "scbase/loader!dojo/aspect",
				   "scbase/loader!dojo/on",
				   "scbase/loader!dojo/ready",
				   "dojo/domReady!"],
		           function(
		        		   dParser,		        		  
		        		   dConnect,		        		  
						   scMainExtn,
						   scFilteringSelect,
		        		   scUtil,
		        		   scBundleUtils,		        		  
		        		   scMobileScreenContainer,
		        		   iasContainerUtil,
						   wscAppHeader,
						   iasContextUtils,
		        		   uiUtils,
		        		   scLogUtils,
		        		   scResourcePermissionUtils,
						   scApplicationInfo,	
						   djDialogUnderlay,
		        		   dAspect,
		        		   dOn,
						   dReady){
						   
								dAspect.after(djDialogUnderlay.prototype, "show", function() {
									dOn(this.domNode, "touchstart", function(event){
										event.stopPropagation();
									});
								});
								
								dAspect.after(djDialogUnderlay.prototype, "show", function() {
									dOn(this.domNode, "touchend", function(event){
										event.stopPropagation();
									});
								});
								
								dAspect.after(djDialogUnderlay.prototype, "show", function() {
									dOn(this.domNode, "MSPointerDown", function(event){
										event.stopPropagation();
									});
									
								});
								
								dAspect.after(djDialogUnderlay.prototype, "show", function() {
									dOn(this.domNode, "MSPointerUp", function(event){
										event.stopPropagation();
									});
									
								});
								
								dAspect.after(djDialogUnderlay.prototype, "show", function() {
									dOn(this.domNode, "click", function(event){
										event.stopPropagation();
									});
									
								});
			
								dReady(100, function(){
								dParser.parse();

								<%
									if ( mobileEnv ) {
								%>
									scApplicationInfo.setMobileEnv();
								<%
									}
								%>

								var appConfig = <%=getConfigData(uiContext,"MobileAppConfig")%>;								
								
								for (var i =0;i<appConfig.resourceBundles.length;i++){
									scBundleUtils.registerGlobalBundles(appConfig.resourceBundles[i].basePath,appConfig.resourceBundles[i].bundleName);											
								}	
								
								scFilteringSelect.prototype.invalidMessage = scBundleUtils.getString("message_noMatchFound");
								
								<scuitag:includeExtensionBundles/>
								
								scUtil.setReloadConfirmationPopupMode("ALWAYS");
								iasContextUtils.addToContext('isMobile',true);
								iasContextUtils.setFixedHeaderHeight(100);
								iasContextUtils.addToContext('DeviceType','<%=deviceType%>');
								iasContainerUtil.setOnlineHelpUrl('<%=onlineHelpUrl%>');
								iasContextUtils.addToContext('CurrentStore','<%=SCEncoder.getEncoder().encodeForJavaScript(currentStore) %>');
	                        	iasContextUtils.addToContext('EnterpriseCode','<%=SCEncoder.getEncoder().encodeForJavaScript(enterpriseCode) %>');
	                        	iasContextUtils.addToContext('Loginid','<%=SCEncoder.getEncoder().encodeForJavaScript(loginid) %>');
	                            iasContextUtils.addToContext('ImagesRuleValue',<%=strRuleJson%>);
	                            iasContextUtils.addToContext('PollingInterval',<%=strRuleJsonPolling%>);
	                            iasContextUtils.addToContext('GotoSummaryOnComplete',<%=strRuleJsonGotoSummaryOnComplete%>);
	                            
	                            scApplicationInfo.setAccessibiltyMode(scResourcePermissionUtils.hasPermission("WSCFO0TER001"));
	                            
								<%
									if(!SCUtil.isVoid(enablePerformanceLogging) && enablePerformanceLogging.equals("Y")){
								 %>
								 	scLogUtils.enablePerformanceLogging();
								 <%}%>
								
								<%
									if(!SCUtil.isVoid(performanceThreshold)){
								 %>
								 	scLogUtils.setMinimumBenchmarkForLogging(<%=SCEncoder.getEncoder().encodeForJavaScript(performanceThreshold)%>);
									
								 <%}%>
								
								var screenContainerInstance = scApplicationInfo.getSCScreenContainerInstance();
								screenContainerInstance.setEditorMapConfig(<%=getConfigData(uiContext,"EditorMapConfig")%>);
								
								var contextPath = '<%=request.getContextPath()%>';
								appConfig.container.contextPath = contextPath;
								appConfig.header.userName = '<%=SCEncoder.getEncoder().encodeForJavaScript(sUserName)%>';
								appConfig.header.storeName = '<%=SCEncoder.getEncoder().encodeForJavaScript(currentStoreName)%>';																						
								
								var initScreenOverlay = document.getElementById("sc_plat_dojo_widgets_ScreenDialogUnderlay_Initial");
								initScreenOverlay.parentNode.removeChild(initScreenOverlay);
																
								iasContainerUtil.init(appConfig);	
								
								wscAppHeader.init(appConfig);
								uiUtils.setAppHeader(wscAppHeader);		
								iasContextUtils.setPaginationSizeList(<%=getConfigData(uiContext,"PaginationSizeConfig")%>);
								iasContextUtils.seteditorList(<%=getConfigData(uiContext,"EditorMapConfig")%>);
								iasContextUtils.setAlertTypeList(<%=getConfigData(uiContext,"AlertTypeMap")%>);
								iasContextUtils.addToContext('orderSearchConfig',<%=getConfigData(uiContext,"OrderSearchConfig")%>);
								iasContextUtils.addToContext('batchLineStatusConfig',<%=getConfigData(uiContext,"BatchLineStatusConfig")%>);
								
								invokeAppReady();
			});
						<%
							if(devProdMode) {
						%>
						dReady(2000, function(){
							iasContainerUtil.openPreviouslyOpenedScreen();
						});
						
						<%
							}
						%>
		});
		
		
		</script>
		<%if(deviceType == SCCSMobileUtils.PC){ %>
			<isccstag:includeFile filePath="/jsps/dojo/extensibility/workbench.jsp" inclusionReq="<%=String.valueOf(SCUIExtensibilityWorkbenchHelper.isExtensibilityWorkbenchMode())%>"/>
		<%}%>
		<%
			StringBuffer conditionalStyles = new StringBuffer(""); 
			if(SCUIUtils.isDevMode()){
				conditionalStyles = conditionalStyles.append(" devMode");
			}
			if(SCUIExtensibilityWorkbenchHelper.isExtensibilityWorkbenchMode()){
				conditionalStyles = conditionalStyles.append(" extnMode");
			}
			if(deviceType == SCCSMobileUtils.PC) {
				conditionalStyles = conditionalStyles.append(" desktopMode");
			} else {
				conditionalStyles = conditionalStyles.append(" handHeld");
			}
		%>
	</head>

	<body id="bodyId" dir="<%=bidiOrientation %>" class="oneui comapps mobile <%=conditionalStyles%>">
		<div id="mainContentHolder" class="mainContentHolder">
		<script>
			<%
			if(!SCUtil.isVoid(documentDomain)){
			%>
			      document.domain = '<%=documentDomain%>';
			<%}
			%>
		</script>
		<audio id="audioAlert" controls preload="auto">
			<source src="<%=request.getContextPath()%>/wsc/resources/audio/alertpopup.mp3" type="audio/mpeg; codecs='mp3';">
			<source src="<%=request.getContextPath()%>/wsc/resources/audio/alertpopup.ogg" type="audio/ogg; codecs='vorbis';">
			<embed height="50" width="100" src="<%=request.getContextPath()%>/wsc/resources/audio/alertpopup.mp3">
		</audio>
		
		
	<input id="shipmentKey" name="ShipmentKey" type="hidden" value='<%=SCEncoder.getEncoder().encodeForJavaScript(shipmentKey)%>' />
	<input id="orderHeaderKey" name="OrderHeaderKey" type="hidden" value='<%=SCEncoder.getEncoder().encodeForJavaScript(orderHeaderKey)%>' />
	<input id="screenID" name="ScreenID" type="hidden" value='<%=SCEncoder.getEncoder().encodeForJavaScript(screenID)%>' />

	<div id="contentWrapperId" class="contentwrapper">	
	
		<div wairole="banner" role="banner" id="mobileAppFrame" class="fixed_layer mobileAppHeader" style="width:100%">
			<div id="AppHeaderLeft" class="AppHeaderLeft" >
				<a id="applicationLogo" class="inline hideBackIcon">
					<img id="backButton" class="backIcon" src="<%=request.getContextPath()%>/wsc/resources/css/icons/images/back_arrow_white.png">
				</a>
				<span id="appTitle" class="appTitle inline"  style=""></span>						
			</div>

			<div id="AppHeaderRight" class="AppHeaderRight">
				<div id="pickUpLabel" class="inline">
					<div id="pickUpLabelText" class="Extn_pickUpLabeltext">Pick Up</div>
				</div>
				<div id="search" class="inline">
					<div id="appSearch" class="inline"></div>
				</div>			
				<div id="alerts" class="inline">
					<div id="appAlerts" class="inline imageNumberPanel"></div>
				</div>
				<div id="devTools" class="devToolsMenu inline"></div>
				<div id="appMenu" class="appMenu inline"></div>
		
			</div>
		</div>

		<div id="AppBodyPanel" aria-live="assertive" class="comappsCenter AppBodyPanel" >			
			<div class="comappsContent comappsContainer" data-dojo-type="dijit.layout.ContentPane" data-dojo-props="uId: 'comappsContainer'" >
				<div id="borderContainerCenter" class="comappsMDIContainer" data-dojo-type="wsc.mobile.widgets.MobileScreenContainer" data-dojo-props="uId:'CenterContainer', doLayout: false">
				</div>
				<div data-dojo-type="dijit.layout.ContentPane"></div> 
			</div>
		</div>
     </div>
     

	<div id="hamburgerPanelId" class="hamburgerPanel">
	</div>
	<div id="bodyoverlay" class="bodyoverlay"></div>	
	<div id="extensibilityShellHolder" class="scExtensibilityShellHolder">
	</div>
	</div>
	<div id="printHolder" class="printHolder" style="display:none;background-color:white;width:100%;height:100%">
	</div>
	<div class="dijitDialogUnderlayWrapper" role="alert" 
		id="sc_plat_dojo_widgets_ScreenDialogUnderlay_Initial" 
		widgetid="sc_plat_dojo_widgets_ScreenDialogUnderlay_Initial" 
		style="display: block; top: 0px; left: 0px; position: fixed; width: 100%; height: 100%;">
		<div class="dijitDialogUnderlay scMask" dojoattachpoint="node" role="alert" 
			id="dialogId_underlay" style="width: 100%; height: 100%; top: -50px; left: -50px; text-align: center;">
		</div>
		<div style="width: 100%; height: 100%; top: -25px; left: -25px; position:absolute">
			<div class="scLoadingIconAndTextWrapper" dojoattachpoint="imageAndTextHolderNode" role="alert" style="top: 50%; left: 50%;">    
				<div class="scLoadingIcon" dojoattachpoint="loadingIconNode"> </div> 
				<div class="scLoadingText" dojoattachpoint="loadingTextNode" role="alert">&nbsp;<%= SCUILocalizationHelper.getString(uiContext, "Processing")%></div>    </div> 
			<iframe src='javascript:""' class="dijitBackgroundIframe" role="presentation" tabindex="-1" style="opacity: 0.1; width: 100%; height: 100%; border: none;"></iframe>
		</div>
	</div>
	</body>
</html>
