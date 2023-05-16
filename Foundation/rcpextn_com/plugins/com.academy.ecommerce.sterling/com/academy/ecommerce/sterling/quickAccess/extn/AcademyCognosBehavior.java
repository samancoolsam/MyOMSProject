/**
 * 
 */
package com.academy.ecommerce.sterling.quickAccess.extn;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.academy.ecommerce.sterling.util.AcademyPCAConstants;
import com.academy.ecommerce.sterling.util.XMLUtil;
import com.yantra.yfc.rcp.YRCApiContext;
import com.yantra.yfc.rcp.YRCBehavior;
import com.yantra.yfc.rcp.YRCPlatformUI;
import com.yantra.yfc.rcp.internal.YRCApiCaller;

/**
 * @author sahmed
 * 
 */
public class AcademyCognosBehavior extends YRCBehavior {
	private AcademyCognos page = null;


	public AcademyCognosBehavior(Composite ownerComposite, String form_id,
			Element inElm) {
		super(ownerComposite, form_id, inElm);
		this.page = (AcademyCognos) ownerComposite;
		page.openUrl();
		
	}
}