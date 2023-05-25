package org.placelab.demo.jsr0179;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.placelab.util.swt.SwtScrolledComposite;


public class Monitor extends Composite {
	public static Display display;
	public static Shell shell;
	
	
	static {
		display = new Display();
		
		

		if (App.PLATFORM == App.PLATFORM_PPC) {
			shell = new Shell(display, SWT.NO_TRIM | SWT.CLOSE | SWT.RESIZE);
			shell.setLocation(0,26);
			
			// take up the whole screen.
			Rectangle screen = display.getClientArea();
			//shell.setSize(210, screen.height);
		} else {
			shell = new Shell(display, SWT.SHELL_TRIM);
		}
		
		shell.setLayout(new FillLayout());
				
		shell.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		shell.setText("Where Am I?");
		
	}
	
	private Label closestLbl;
	private Table nearbyTbl;
	private Button mapButton;
	private Slider resolutionSlider;
	private Group resolutionLabel;

	private String closestStr;
	private String[][] nearbyData;
	private int resolution;
	private boolean markEnabled = false;
	
	
	
	
	public Monitor (Composite parent) {
		super(parent, SWT.NONE);
		{
			GridLayout gl = new GridLayout();
			gl.marginHeight = 4;
			gl.marginWidth = 4;
			setLayout(gl);
		}
		
		loadMenus();
		
		{
			Group grp = new Group(this, SWT.NONE);
			grp.setText("Closest Landmark");
			
			
			{
				GridLayout gl = new GridLayout();
				gl.marginHeight = 3;
				gl.marginWidth = 3;
				gl.numColumns = 2;
				grp.setLayout(gl);
			}
			
			closestLbl = new Label(grp, SWT.WRAP);		
			closestLbl.setText("");
			
			{
				GridData gd = new GridData();
				gd.verticalAlignment = GridData.FILL;
				gd.grabExcessVerticalSpace = true;
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				closestLbl.setLayoutData(gd);
			}
			
			mapButton = new Button(grp, SWT.PUSH);
			mapButton.setText("Mark");
			mapButton.setEnabled(false);
			mapButton.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					
					Shell s;
					
					if (App.PLATFORM == App.PLATFORM_PPC) {
						s = new Shell(display, SWT.NO_TRIM | SWT.RESIZE | SWT.CLOSE);
						s.setLocation(0,26);
					} else {
						s = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE);
					}
					
					s.setText("Edit Landmark");
					s.setLayout(new FillLayout());
					
					
				
					if (App.PLATFORM == App.PLATFORM_PPC) {
						SwtScrolledComposite sc = new SwtScrolledComposite(s, SWT.V_SCROLL);
						sc.setLayout(new FillLayout());
						Composite c = new EditLandmark(sc, App.store, App.currentLandmark);
						c.setSize(c.computeSize(228, SWT.DEFAULT));
						sc.setContent(c);
					} else {
						new EditLandmark(s, App.store, App.currentLandmark);
						s.setSize(s.computeSize(300, SWT.DEFAULT));
						
					}
					
					s.open();
				}
				
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			{
				GridData gd = new GridData();
				gd.verticalAlignment = GridData.FILL;
				gd.horizontalAlignment = GridData.FILL;
				mapButton.setLayoutData(gd);
			}
			
			
			
			GridData gd = new GridData();
			gd.verticalAlignment = GridData.FILL;
			
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			grp.setLayoutData(gd);
			
			
			
			
		}
		
		{
			Group grp = new Group(this, SWT.NONE);
			grp.setText("Nearby Landmarks");
			resolutionLabel = grp;
			
			{
				GridLayout gl = new GridLayout();
				gl.marginHeight = 3;
				gl.marginWidth = 3;
				grp.setLayout(gl);
			}
			
			
			resolutionSlider = new Slider(grp, SWT.HORIZONTAL);
			resolutionSlider.setMaximum(1000);
			resolutionSlider.setMinimum(0);
			
			{			
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true; 
				resolutionSlider.setLayoutData(gd);
			}
			
			resolutionSlider.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					resolution = ((Slider)e.widget).getSelection();
					resolutionLabel.setText("Nearby Landmarks - " + ((Slider)e.widget).getSelection() + " meters");
				}
				
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			
			setResolution(50);
			
			nearbyTbl = new Table(grp, SWT.MULTI | SWT.BORDER);
			nearbyTbl.setLinesVisible(true);
			nearbyTbl.setHeaderVisible(true);
			

			TableColumn name = new TableColumn(nearbyTbl, SWT.LEFT);
			name.setText("Name");
			name.setWidth(90);
			
			TableColumn desc = new TableColumn(nearbyTbl, SWT.LEFT);
			desc.setText("Distance");
			desc.setWidth(100);
			
			{
				GridData gd = new GridData();
				gd.verticalAlignment = GridData.FILL;
				gd.grabExcessVerticalSpace = true;
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				grp.setLayoutData(gd);
			}
			
			{			
				GridData gd = new GridData();
				gd.verticalAlignment = GridData.FILL;
				gd.grabExcessVerticalSpace = true;
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				gd.heightHint = 60;
				//gd.widthHint = 208;
				nearbyTbl.setLayoutData(gd);
			}
			
		}
	
		this.setSize(this.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	public void setMark (boolean markEnabled) {
		if (this.markEnabled != markEnabled) {
			this.markEnabled = markEnabled;
			
			display.asyncExec(new Runnable(){
				public void run () {
					mapButton.setEnabled(Monitor.this.markEnabled);
				}
			});
		}
	}
	
	public void setClosest (String str) {
		closestStr = str;
		display.syncExec(new Runnable(){
			public void run () {
				closestLbl.setText(closestStr);
			}
		});
	}
	
	public void setNearby (String[][] nearby) {
		nearbyData = nearby;
		
		display.syncExec(new Runnable(){
			public void run () {
				nearbyTbl.removeAll();
				
				for (int i=0;i<nearbyData.length;i++) {
					TableItem item = new TableItem(nearbyTbl, SWT.NONE);
					item.setText(nearbyData[i]);
				}
				
			}
		});
	}
	
	public int getResolution () {
		return resolution;
	}
	
	public void setResolution (int meters) {
		resolution = meters;
	
		display.asyncExec(new Runnable() {
			public void run () {
				resolutionSlider.setSelection(resolution);
				resolutionLabel.setText("Nearby Landmarks - "+resolution+" meters");		
			}
		});
		
	}
	
	
	private void loadMenus () {
		

		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		
		// FILE
		{
			MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
			item.setText("File");
			
			Menu sub = new Menu(shell, SWT.DROP_DOWN);
			item.setMenu(sub);
			
			
			MenuItem quit = new MenuItem(sub, SWT.CHECK);
			quit.setText("Quit");
			quit.addListener(SWT.Selection, new Listener(){
				public void handleEvent(Event e) {
					shell.dispose();
					System.exit(0);
				}
			});
			
			
		}
		
		
	}
	
	
	
}


