package com.academy.ecommerce.sterling.paymentInquiry.extn;

import org.eclipse.swt.browser.Browser;
import org.w3c.dom.Element;

import com.yantra.yfc.rcp.IYRCBrowserHandler;
import com.yantra.yfc.rcp.IYRCWebAppHandler;
import com.yantra.yfc.rcp.YRCLoginInfo;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.YRCWebAppLoginInfo;
import com.yantra.yfc.rcp.YRCWebAppUtils;
import com.yantra.yfc.rcp.YRCWebAppStatus;
import com.yantra.yfc.rcp.internal.YRCWebAppManager;

import org.eclipse.swt.browser.Browser;
/**
 * This class will set the user information required for browser 
 * 
 * @author <a href="mailto:Kruthi.KM@cognizant.com">Kruthi K M</a>
 * Copyright © 2006-2009 Sterling Commerce, Inc. All Rights Reserved.
 */
public class AcademyWebAppHandler implements IYRCWebAppHandler {
	private YRCLoginInfo loginInfo = null;

	public AcademyWebAppHandler() {
		// TODO Auto-generated constructor stub
	}

	public void handleBrowser(Browser paramBrowser,
			IYRCBrowserHandler paramIYRCBrowserHandler) {
		// TODO Auto-generated method stub
		YRCWebAppLoginInfo localYRCWebAppLoginInfo = YRCWebAppUtils
				.loginToWebApp("AcadWebAppHandler", this.loginInfo, null);
		if ((YRCPlatformUI.isVoid(localYRCWebAppLoginInfo))
				|| (localYRCWebAppLoginInfo.getLoginStatus()
						.equalsIgnoreCase("yrc_WebApp_Integration_Failure")))
			YRCPlatformUI
					.trace("Web Application Integration for webAppConfig AcademyWebAppHandler failed.");
		else {
			YRCWebAppStatus localYRCWebAppStatus = YRCWebAppUtils
					.addCookiesToBrowserSynchronously("AcadWebAppHandler",
							paramBrowser);

		}
	}

	@Override
	public void init(Element arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUserInfo(YRCLoginInfo paramYRCLoginInfo) {
		// TODO Auto-generated method stub
		this.loginInfo = paramYRCLoginInfo;
	}

}
