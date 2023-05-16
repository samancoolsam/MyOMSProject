package com.academy.ecommerce.sterling.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.yantra.yfc.rcp.YRCConstants;
import com.yantra.yfc.rcp.YRCLinkBindingData;
import com.yantra.yfc.rcp.YRCPlatformUI;

public class AcademyScreenPanelUtils {
	
	

	public AcademyScreenPanelUtils() {
		super();
	}
	
	public static void addPanelHeader(Composite composite2, String panelHeader) {
		addPanelHeader(composite2, panelHeader, null, 1, false);
	}

	
	public static void addPanelHeader(final Composite composite2, String sPanelHeader, String sPanelImageTheme,
			int hSpan, boolean bPanelOptions,int titleStyle) {
		GridData gridData182 = new org.eclipse.swt.layout.GridData();
		//gridData182.horizontalIndent = 3;
		gridData182.heightHint = 17;
		gridData182.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData182.horizontalSpan = hSpan;
		gridData182.grabExcessHorizontalSpace = true;
		final Label label82 = new Label(composite2,titleStyle);
		label82.setData(YRCConstants.YRC_CONTROL_NAME,"PanelHeaderLabel");
		label82.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
		label82.setText(sPanelHeader);
		label82.setLayoutData(gridData182);

		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();
				
				// This is to draw the border with background blue color
				gc.setForeground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
				gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				Rectangle r1 = label82.getBounds();
				gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

				//r = composite2.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
				gc.setBackground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
				gc.fillRectangle(r.x,r.y,r.width,r1.height+1);

				// This is to draw the sides for raised effect
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				
				
				gc.dispose();
			}
		});

	}
	

	public static void addPanelHeaderWithAction(final Composite composite2, String sPanelHeader, String sPanelImageTheme, 
			int hSpan, boolean bPanelOptions, int titleStyle,  String sActionLink ){
		
		GridData gridData182 = new org.eclipse.swt.layout.GridData();
		gridData182.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData182.grabExcessHorizontalSpace = true;
		if(hSpan > 1 && sActionLink != null){
			gridData182.horizontalSpan = hSpan - 1;
		}else{
			gridData182.horizontalSpan = hSpan;
		}
		final Label label82 = new Label(composite2,titleStyle);
		label82.setData(YRCConstants.YRC_CONTROL_NAME,"PanelHeaderLabel");
		label82.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
		label82.setText(YRCPlatformUI.getString(sPanelHeader));
		label82.setLayoutData(gridData182);
		label82.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
		label82.setBackgroundImage(YRCPlatformUI.getImage("PanelHeaderImage"));
		label82.setFont(YRCPlatformUI.getFont("PanelHeader"));
		label82.setSize(YRCPlatformUI.getWidth("PanelHeader"),YRCPlatformUI.getHeight("PanelHeader"));
		
		if(sActionLink != null){
			GridData gridData2 = new org.eclipse.swt.layout.GridData();
			gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
			gridData2.grabExcessHorizontalSpace = true;
			gridData2.horizontalSpan = 1;
		
			final Link link1 = new Link(composite2,titleStyle);
			link1.setData(YRCConstants.YRC_CONTROL_NAME,"addressCompositeHeaderLink");
			link1.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
			link1.setText(YRCPlatformUI.getString(sActionLink));
			link1.setLayoutData(gridData2);
			link1.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
			link1.setBackgroundImage(YRCPlatformUI.getImage("PanelHeaderImage"));
			link1.setFont(YRCPlatformUI.getFont("PanelHeader"));
			link1.setSize(YRCPlatformUI.getWidth("PanelHeader"),YRCPlatformUI.getHeight("PanelHeader"));
			link1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					if(composite2.getData("Data") != null){
						//action.headerPanelAction(composite2.getData("Data"));
					}else{
						//action.headerPanelAction(null);
					}
					
				}
			});
		}
		
		
		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();
				
				// This is to draw the border with background blue color
				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				Rectangle r1 = label82.getBounds();
				gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

				//r = composite2.getClientArea();
//				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
//				gc.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
//				gc.fillRectangle(r.x,r.y,r.width,r1.height+1);

				// This is to draw the sides for raised effect
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				
				
				gc.dispose();
			}
		});

	}
	
	//XXX todo add javadoc
	public static void addPanelHeader(final Composite composite2, String sPanelHeader, String sPanelImageTheme,
				int hSpan, boolean bPanelOptions) {
		addPanelHeader(composite2,sPanelHeader,sPanelImageTheme,hSpan,bPanelOptions,SWT.NONE);
	}

	public static void addPanelBorder(final Composite composite2) {
		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				gc.dispose();
			}
		});
	}

	public static void addPanelBorder(final Text text) {
		text.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(text);
				Rectangle r = text.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("TaskComposite"));
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				gc.dispose();
			}
		});
	}

	public static Composite getPanelOptions(Composite composite4) {
		if (composite4.getData("YRC_PANEL_OPTION") != null)	{
			Composite c = (Composite)composite4.getData("YRC_PANEL_OPTION");
			return c;
		}	else	{
			return null;
		}

	}
	public static void addPanelNormalBorder(final Composite composite5, final Color bg, final Color fg) {
		composite5.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite5);
				Rectangle r = composite5.getClientArea();
				
				gc.setForeground(fg);		
				gc.setBackground(bg);		
				gc.drawRectangle(r.x+5,r.y+5,r.width-10,r.height-10);
	
				gc.dispose();
			}
		});
		
	}

	public static void paintControl(PaintEvent e) {
		Composite caller = (Composite)e.widget;
		caller.setBackgroundImage(YRCPlatformUI.getImage("PanelHeaderImage"));
		
		Composite child = getFirstChildComposite(caller);
		if(child ==null)return;
		child.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
		//child.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
		child.setBackgroundMode(SWT.INHERIT_FORCE);
		child.setBackgroundImage(YRCPlatformUI.getImage("PanelHeaderImage"));
		child.setFont(YRCPlatformUI.getFont("PanelHeader"));
		child.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC((Composite)e.widget);
				Rectangle r = ((Composite)e.widget).getBounds();
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
//				gc.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
//				gc.fillRectangle(r.x,r.y,r.width,r.height+1);
				gc.dispose();
			}
		});

		for(int i = 0; i < child.getChildren().length; i++){
			Control c = child.getChildren()[i];
			if(c instanceof Link){
				Link link = (Link)c;
				link.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
				link.setForeground(YRCPlatformUI.getForeGroundColor("Link"));
//				link.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				link.setFont(YRCPlatformUI.getFont("PanelHeader"));
				caller.layout(true,true);
			} else{
				c.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
				c.setForeground(YRCPlatformUI.getForeGroundColor("PanelHeader"));
//				c.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				c.setBackgroundImage(YRCPlatformUI.getImage("PanelHeaderImage"));
				c.setFont(YRCPlatformUI.getFont("PanelHeader"));
			}
		}
		
		
	
		GC gc = new GC(caller);
		Rectangle r = caller.getClientArea();
		
		// This is to draw the border with background blue color
		gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
		gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
		
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
		Rectangle r1 = getFirstChildComposite(caller).getBounds();
		gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

		//r = composite2.getClientArea();
		gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
		gc.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
		gc.fillGradientRectangle(r.x,r.y,r.width,r1.height+1, true);

		// This is to draw the sides for raised effect
		gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
		gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
		gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
		//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
		gc.drawLine(r.x,r.y,r.x+r.width,r.y);
		gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
		
		
		gc.dispose();

	}
	
	
	private static Composite getFirstChildComposite(Composite caller){
		for(int i = 0; i < caller.getChildren().length; i++){
			if(caller.getChildren()[i] instanceof Composite){
				return (Composite) caller.getChildren()[i];
			}
		}
		return null;
	}
	
	public static void addGradientPanelHeader(final Composite composite2, String sPanelHeader) {
		addGradientPanelHeader(composite2, sPanelHeader, true);
		
	}
	
	public static void addGradientPanelHeader(final Composite composite2, String sPanelHeader, boolean getFromBundle) {
		GridData gridData182 = new org.eclipse.swt.layout.GridData();
		gridData182.heightHint = 17;
		gridData182.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData182.horizontalSpan = 3;
		gridData182.grabExcessHorizontalSpace = true;
		final CLabel cLabel = new CLabel(composite2, SWT.NONE);
		if (getFromBundle)
		{
			sPanelHeader = YRCPlatformUI.getString(sPanelHeader);
		}
		cLabel.setText(sPanelHeader);
		cLabel.setFont(YRCPlatformUI.getFont("PanelHeader"));
		cLabel.setForeground(YRCPlatformUI.getForeGroundColor("HeaderPanel.0"));
		cLabel.setBackground(new Color[] {
                YRCPlatformUI.getBackGroundColor("HeaderPanel"),
                YRCPlatformUI.getBackGroundColor("HeaderPanel.3")
                },
                
    new int[] {100},
    true);
		cLabel.setData(YRCConstants.YRC_CONTROL_NAME,"PanelHeaderLabel");
		cLabel.setSize(YRCPlatformUI.getWidth("PanelHeader"),YRCPlatformUI.getHeight("PanelHeader"));
		GridData gridData10 = new GridData();
		gridData10.horizontalAlignment = GridData.FILL;
		gridData10.grabExcessHorizontalSpace = true;
		gridData10.heightHint = 20;
		gridData10.horizontalSpan = 2;
		gridData10.verticalAlignment = GridData.CENTER;
		cLabel.setLayoutData(gridData10);

		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();

				// This is to draw the border with background blue color
				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				Rectangle r1 = cLabel.getBounds();
				gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

				//r = composite2.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.setBackground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.fillRectangle(r.x,r.y,r.width,r1.height+1);

				// This is to draw the sides for raised effect
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				
				
				gc.dispose();
			}
		});

	}

	/**
	 * The method creates a panel header with a gradient background and places a label and link on it. 
	 * The label is left aligned while the link is right aligned.The action passed as argument is added to the link
	 * The control names for the label and link will be set according to the labelCtrlName and linkBindingData 
	 * arguments respectively
	 * @param composite2 - the composite within which the header is to be created
	 * @param sPanelHeader - the bundle key for the label literal
	 * @param labelCtrlName - the control name for the label
	 * @param sActionLink - the bundle key for the link literal
	 * @param IYCDPanelHeaderAction - the IYCDPanelHeaderAction for the link
	 */
	public static void addGradientPanelHeaderWithAction(final Composite composite2, String sPanelHeader, 
			String labelCtrlName,
			String sActionLink){
		
		GridData gridData182 = new org.eclipse.swt.layout.GridData();
		gridData182.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData182.grabExcessHorizontalSpace = true;
		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.marginWidth = 0;
		gridLayout4.numColumns = 2;
		gridLayout4.verticalSpacing = 0;
		gridLayout4.horizontalSpacing = 0;
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		final Composite gradientComposite = new Composite(composite2,SWT.BORDER);
		gradientComposite.setLayoutData(gridData);
		gradientComposite.setLayout(gridLayout4);
		gradientComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
		gradientComposite.setBackgroundMode (SWT.INHERIT_FORCE);


		final Label label82 = new Label(gradientComposite,SWT.NONE);
		if (YRCPlatformUI.isVoid(labelCtrlName))
		{
			labelCtrlName = "PanelHeaderLabel";
		}
		label82.setData(YRCConstants.YRC_CONTROL_NAME,labelCtrlName);
		label82.setText(YRCPlatformUI.getString(sPanelHeader));
		label82.setLayoutData(gridData182);
		label82.setFont(YRCPlatformUI.getFont("PanelHeader"));
		label82.setSize(YRCPlatformUI.getWidth("PanelHeader"),YRCPlatformUI.getHeight("PanelHeader"));
		label82.setBackgroundImage(gradientComposite.getBackgroundImage());

		
		if(sActionLink != null){
			GridData gridData2 = new org.eclipse.swt.layout.GridData();
			gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
			gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
			gridData2.grabExcessHorizontalSpace = true;
			gridData2.grabExcessVerticalSpace = true;
			gridData2.horizontalSpan = 1;
		
			final Link link1 = new Link(gradientComposite,SWT.NONE);
			link1.setData(YRCConstants.YRC_CONTROL_NAME,"addressCompositeHeaderLink");
			link1.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"PanelHeader");
			link1.setText(YRCPlatformUI.getString(sActionLink));
			link1.setLayoutData(gridData2);
			link1.setFont(YRCPlatformUI.getFont("PanelHeader"));
			link1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					if(composite2.getData("Data") != null){
						//action.headerPanelAction(composite2.getData("Data"));
					}else{
						//action.headerPanelAction(null);
					}
					
				}
			});
			link1.setBackgroundImage(gradientComposite.getBackgroundImage());
		}
		
		
		gradientComposite.addListener (SWT.Resize, new Listener () {
			public void handleEvent (Event event) {
				Rectangle rect = gradientComposite.getClientArea ();
				Image newImage = new Image (gradientComposite.getDisplay(), Math.max (1, rect.width), rect.height);	
				GC gc = new GC (newImage);
				gc.setForeground (YRCPlatformUI.getBackGroundColor("HeaderPanel"));
				gc.setBackground (YRCPlatformUI.getBackGroundColor("HeaderPanel.3"));
				gc.fillGradientRectangle (rect.x, rect.y, rect.x+rect.width, rect.y+rect.height, true);
				gc.dispose ();
				gradientComposite.setBackgroundImage (newImage);
			}
		});
		
		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();
				
				// This is to draw the border with background blue color
				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				Rectangle r1 = label82.getBounds();
				gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

				//r = composite2.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("HeaderPanel.0"));
				gc.setBackground(YRCPlatformUI.getBackGroundColor("HeaderPanel.3"));
				gc.fillRectangle(r.x,r.y,r.width,r1.height+1);

				// This is to draw the sides for raised effect
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				
				
				gc.dispose();
			}
		});
		composite2.layout(true);


	}


	/**
	 * The method creates a panel header with a label and link. The label is left aligned while the link is right aligned.
	 * The control names for the label and link will be set according to the labelCtrlName and linkBindingData 
	 * arguments respectively
	 * @param composite2 - the composite within which the header is to be created
	 * @param sPanelHeader - the bundle key for the label literal
	 * @param labelCtrlName - the control name for the label
	 * @param sActionLink - the bundle key for the link literal
	 * @param linkBindingData - the YRCLinkBindingData for the link
	 */
	public static void addGradientPanelHeaderWithLink(final Composite composite2, String sPanelHeader,  String labelCtrlName,
			 String sActionLink, YRCLinkBindingData linkBindingData ){
		
		GridData gridData182 = new org.eclipse.swt.layout.GridData();
		gridData182.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData182.grabExcessHorizontalSpace = true;

		GridData gridData183 = new org.eclipse.swt.layout.GridData();
		gridData183.heightHint = 17;

		GridLayout gridLayout4 = new GridLayout();
		gridLayout4.numColumns = 2;
		gridLayout4.verticalSpacing = 0;
		gridLayout4.horizontalSpacing = 0;
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		final Composite gradientComposite = new Composite(composite2,SWT.BORDER);
		gradientComposite.setLayoutData(gridData);
		gradientComposite.setLayout(gridLayout4);
		gradientComposite.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"NoTheme");
		gradientComposite.setBackgroundMode (SWT.INHERIT_DEFAULT);

		
		final Label label82 = new Label(gradientComposite,SWT.NONE);
		if (YRCPlatformUI.isVoid(labelCtrlName))
		{
			labelCtrlName = "labelHeader";
		}
		label82.setData(YRCConstants.YRC_CONTROL_NAME,labelCtrlName);
		label82.setText(YRCPlatformUI.getString(sPanelHeader));
		label82.setLayoutData(gridData182);
		label82.setFont(YRCPlatformUI.getFont("PanelHeader"));
		label82.setForeground(YRCPlatformUI.getForeGroundColor("PanelHeader"));
		label82.setBackgroundImage(gradientComposite.getBackgroundImage());
		label82.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"NoTheme");

		GridData gridData2 = new org.eclipse.swt.layout.GridData();
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END;
		gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.grabExcessVerticalSpace = true;
		gridData2.horizontalSpan = 1;
		
		final Link link1 = new Link(gradientComposite,SWT.NONE);
		link1.setData(YRCConstants.YRC_CONTROL_NAME,linkBindingData.getName());
		link1.setText(YRCPlatformUI.getString(sActionLink));
		link1.setLayoutData(gridData2);
		link1.setFont(YRCPlatformUI.getFont("PanelHeader"));
		link1.setData(YRCConstants.YRC_LINK_BINDING_DEFINATION, linkBindingData);
		link1.setBackgroundImage(gradientComposite.getBackgroundImage());
		link1.setData(YRCConstants.YRC_CONTROL_CUSTOMTYPE,"NoTheme");

		
		
		gradientComposite.addListener (SWT.Resize, new Listener () {
			public void handleEvent (Event event) {
				Rectangle rect = gradientComposite.getClientArea ();
				Image newImage = new Image (gradientComposite.getDisplay(), Math.max (1, rect.width), rect.height);	
				GC gc = new GC (newImage);
				gc.setForeground (YRCPlatformUI.getBackGroundColor("HeaderPanel"));
				gc.setBackground (YRCPlatformUI.getBackGroundColor("HeaderPanel.3"));
				gc.fillGradientRectangle (rect.x, rect.y, rect.x+rect.width, rect.y+rect.height, true);
				gc.dispose ();
				gradientComposite.setBackgroundImage (newImage);
			}
		});
		
		composite2.addPaintListener(new PaintListener(){
			public void paintControl(PaintEvent e) {
				GC gc = new GC(composite2);
				Rectangle r = composite2.getClientArea();
				
				// This is to draw the border with background color
				gc.setForeground(YRCPlatformUI.getBackGroundColor("PanelHeader"));
				gc.drawRectangle(r.x,r.y,r.width-1,r.height-1);
				
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				Rectangle r1 = label82.getBounds();
				gc.drawLine(r.x,r1.y+r1.height,r.x+r.width-2,r1.y+r1.height);

				//r = composite2.getClientArea();
				gc.setForeground(YRCPlatformUI.getBackGroundColor("HeaderPanel"));
				gc.setBackground(YRCPlatformUI.getBackGroundColor("HeaderPanel.3"));
				gc.fillRectangle(r.x,r.y,r.width,r1.height+1);

				// This is to draw the sides for raised effect
				gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x+r.width-1,r.y,r.x+r.width-1,r.y+r.height-1);
				gc.drawLine(r.x,r.y+r.height-1,r.x+r.width-1,r.y+r.height-1);
				//gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));		
				gc.drawLine(r.x,r.y,r.x+r.width,r.y);
				gc.drawLine(r.x,r.y,r.x,r.y+r.height-1);
				
						
				gc.dispose();
			}
		});
		composite2.layout(true, true);


	}


}
