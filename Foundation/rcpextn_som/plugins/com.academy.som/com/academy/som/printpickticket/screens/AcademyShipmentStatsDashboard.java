	
/*
 * Created on Jun 29,2012
 *
 */
package com.academy.som.printpickticket.screens;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.yantra.yfc.rcp.IYRCComposite;
import com.yantra.yfc.rcp.IYRCPanelHolder;
import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCExtentionBehavior;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.GridData;

/**
 * @author 267146
 *
 * Generated by Sterling RCP Tools
 * Copyright � 2005, 2006 Sterling Commerce, Inc. All Rights Reserved.
 */

public class AcademyShipmentStatsDashboard extends Composite implements IYRCComposite {

	private Composite pnlRoot = null;
    public static final String FORM_ID = "com.academy.som.printpickticket.screens.AcademyShipmentStatsDashboard";
	private Group shipmentStatsGrp = null;

	public AcademyShipmentStatsDashboard(Composite parent, int style, YRCExtentionBehavior
			behavior) {
		super(parent, style);
		initialize();
        setBindingForComponents();
	}
	
	private void initialize() {
		createRootPanel();
		this.setLayout(new FillLayout());
		setSize(new org.eclipse.swt.graphics.Point(300,200));
	}
	
    private void setBindingForComponents() {
        //TODO: set all bindings here
    }
    
    public String getFormId() {
        return FORM_ID;
    }
    
    public Composite getRootPanel() {
        return pnlRoot;
    }

	private void createRootPanel() {
		pnlRoot = new Composite(this, SWT.NONE);
        pnlRoot.setLayout(new GridLayout());
        createShipmentStatsGrp();
	}
	
	public IYRCPanelHolder getPanelHolder() {
        // TODO Complete getPanelHolder
        return null;
    }
    
    public String getHelpId() {
		// TODO Complete getHelpId
		return null;
	}

	/**
	 * This method initializes shipmentStatsGrp	
	 *
	 */
	private void createShipmentStatsGrp() {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		shipmentStatsGrp = new Group(getRootPanel(), SWT.NONE);
		shipmentStatsGrp.setText("Today");
		shipmentStatsGrp.setData(YRCConstants.YRC_CONTROL_NAME, "Label");
		shipmentStatsGrp.setLayoutData(gridData);
		shipmentStatsGrp.setLayout(gridLayout);
	}
  
}
