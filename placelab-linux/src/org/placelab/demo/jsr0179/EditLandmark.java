
package org.placelab.demo.jsr0179;

import java.io.IOException;

import javax.microedition.location.AddressInfo;
import javax.microedition.location.Landmark;
import javax.microedition.location.LandmarkStore;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;
import javax.microedition.location.QualifiedCoordinates;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class EditLandmark extends Composite {
	
	private Text lat;
	private Text lon;
	//private Text alt;
	
	// private Combo category;  // we're ignoring categories for now.
	private Text name;
	private Text desc;
	
	private Landmark original = null;
	
	public EditLandmark (Composite parent, LandmarkStore store, Landmark landmark) {
		super(parent, SWT.NONE);
		
		createLayout();
		
		if (landmark != null) {
			
			original = landmark;
			
			name.setText(landmark.getName());
			desc.setText(landmark.getDescription());
			
			lat.setText(Double.toString(landmark.getQualifiedCoordinates().getLatitude()));
			lon.setText(Double.toString(landmark.getQualifiedCoordinates().getLongitude()));
			// alt.setText(Float.toString(landmark.getQualifiedCoordinates().getAltitude()));
				
		} else {
			
			refreshCoordinates();
		}
		
	}
	
	
	
	public EditLandmark (Composite parent, LandmarkStore store) {
		this(parent, store, null);
	}
	
	private void refreshCoordinates () {
		Monitor.display.asyncExec(new Runnable(){
			
			public void run () {
				Location l = LocationProvider.getLastKnownLocation();
				
				if ( (l == null) || (!l.isValid())) {
					System.err.println("Unable to get last known location.");
				} else {
					QualifiedCoordinates qc = l.getQualifiedCoordinates();
					
					lat.setText(Double.toString(qc.getLatitude()));
					lon.setText(Double.toString(qc.getLongitude()));
					// alt.setText(Float.toString(qc.getAltitude()));
				}
				
			}
		});
	}
	
	private void createLayout() {
		{
			GridLayout gl = new GridLayout();
			gl.marginHeight = 5;
			gl.marginWidth = 5;
			gl.numColumns = 1;
			
			setLayout(gl);
			
		}
		
		/*
		 * Coordinates section
		 */
		{
			Group grp = new Group(this, SWT.NONE);
			grp.setText("Coordinates");
			
			{
				GridLayout gl = new GridLayout();
				gl.marginHeight = 3;
				gl.marginWidth = 3;
				gl.numColumns = 1;
				
				grp.setLayout(gl);
				
				
			}
			
			
			
			
			{
				
				
				new Label(grp, SWT.NONE).setText("Latitude");
				lat = new Text(grp, SWT.SINGLE | SWT.BORDER);
				
				new Label(grp, SWT.NONE).setText("Longitude");
				lon = new Text(grp, SWT.SINGLE | SWT.BORDER);
				
				// new Label(this, SWT.NONE).setText("Altitude");
				// alt = new Text(this, SWT.SINGLE | SWT.BORDER);
				
				
				{
					GridData gd = new GridData();
					gd.horizontalAlignment = GridData.FILL;
					gd.grabExcessHorizontalSpace = true;
					lat.setLayoutData(gd);
				}
				
				{
					GridData gd = new GridData();
					gd.horizontalAlignment = GridData.FILL;
					gd.grabExcessHorizontalSpace = true;
					lon.setLayoutData(gd);
				}
				
				/*
				 {
				 GridData gd = new GridData();
				 gd.horizontalAlignment = GridData.FILL;
				 gd.grabExcessHorizontalSpace = true;
				 alt.setLayoutData(gd);
				 }
				 */
			}
			
			
			
					
			Button refresh = new Button(grp, SWT.PUSH);
			refresh.setText("Refresh");
			refresh.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					EditLandmark.this.refreshCoordinates();
				}
				
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
					
			{
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.END;
				gd.grabExcessHorizontalSpace = true;
				refresh.setLayoutData(gd);
			}
					
							
				
			
					
			GridData gd = new GridData();
			//gd.verticalAlignment = GridData.FILL;
			//gd.grabExcessVerticalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			//gd.widthHint = 210;
			grp.setLayoutData(gd);
	
		}
		
		/*
		 * Landmark section
		 */
		{
			Group grp = new Group(this, SWT.NONE);
			grp.setText("Landmark");
			
			
			{
				GridLayout gl = new GridLayout();
				gl.marginHeight = 3;
				gl.marginWidth = 3;
				gl.numColumns = 1;
				grp.setLayout(gl);
			}
			
			/*
			 * Let's not worry about categories for right now. 
			new Label(grp, SWT.NONE).setText("Category");
			category = new Combo(grp, SWT.DROP_DOWN); 
			
			category.removeAll();
			
			for (Enumeration e = App.store.getCategories(); e.hasMoreElements(); ) {
				category.add((String) e.nextElement());
			}
			 */
			new Label(grp, SWT.NONE).setText("Name");
			name = new Text(grp, SWT.SINGLE | SWT.BORDER);
			
			new Label(grp, SWT.NONE).setText("Description");
			desc = new Text(grp, SWT.MULTI | SWT.WRAP | SWT.BORDER);
			
			/*
			{
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				category.setLayoutData(gd);
			}
			*/
			
			{
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				name.setLayoutData(gd);
			}
			
			{
				GridData gd = new GridData();
				gd.horizontalAlignment = GridData.FILL;
				gd.verticalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				gd.grabExcessVerticalSpace = true;
				gd.heightHint = 100;
				desc.setLayoutData(gd);
			}
					
			GridData gd = new GridData();
			gd.verticalAlignment = GridData.FILL;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
			
			grp.setLayoutData(gd);
			
			
			
			
		}
		
		{
			Composite grp = new Composite(this, SWT.NONE);
			
			{
				GridLayout gl = new GridLayout();
				gl.marginWidth = 20;	// makes the buttons appear just the left of the PPC keyboard in the lower right corner
				gl.marginHeight = 2;
				grp.setLayout(gl);
			}
			
			
			Composite buttons = new Composite(grp, SWT.NONE);
			{
				RowLayout rl = new RowLayout(SWT.HORIZONTAL);
				rl.spacing = 4;
				buttons.setLayout(rl);	
			}
			
			Button cancel = new Button(buttons , SWT.PUSH);
			cancel.setText("Cancel");
			cancel.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					EditLandmark.this.getShell().close();
				}
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			Button del = new Button(buttons , SWT.PUSH);
			del.setText("Remove");
			del.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					System.err.println("Delete!");
					
					if (original != null) {
						try {
							App.store.deleteLandmark(original);
						} catch (Exception ex) {
							System.err.println("Error erasing previous landmark from store.");
							return;
						}
					}
					
					EditLandmark.this.getShell().close();
				}
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
			
			
			Button save = new Button(buttons , SWT.PUSH);
			save.setText("Save");
			save.addSelectionListener(new SelectionListener(){
				public void widgetSelected (SelectionEvent e) {
					System.err.println("Save!");
					
					if (original != null) {
						
						// if lat/lon/altitude has been changed, do not delete
						// the original
						
						boolean delete = true;
						
						try {
							double tLat = Double.parseDouble(lat.getText());
							double tLon = Double.parseDouble(lon.getText());
							
							if (tLat != original.getQualifiedCoordinates().getLatitude())
								delete = false;
							if (tLon != original.getQualifiedCoordinates().getLongitude())
								delete = false;
						} catch (NumberFormatException ne) {
							delete = false;
						}
						
						if (delete) {
							try {
								App.store.deleteLandmark(original);
							} catch (Exception ex) {
								System.err.println("Error erasing previous landmark from store.");
								return;
							}
						}
					}
					
					/*
					if (category.getText().equals("")) {
						
						// find default category
						String items[] = category.getItems();
						boolean exists = false;
						for (int i = 0;i<items.length; i++) {
							if (items[i].equals("default")) {
								exists = true;
								category.select(i);
								break;
							}
						}
						
						if (!exists) { 
							category.add("default", category.getItems().length);
							category.select(category.getItems().length-1);
						}
					}*/
					
					Landmark l = getLandmark();
					
					try {
						// App.store.addLandmark(l, category.getText());
						App.store.addLandmark(l, "default");
					} catch (IOException ex) {
						System.err.println("Error adding/updating landmark into the store.");
					}
					
					/*
					try {
						LocationProvider.addProximityListener(App.self, l.getQualifiedCoordinates(), 20F);
					} catch (LocationException le) {
						System.err.println("Error registering listener for new landmark.");
						le.printStackTrace();
					}
					*/
					
					EditLandmark.this.getShell().close();
				}
				public void widgetDefaultSelected (SelectionEvent e) {
					widgetSelected(e);
				}
			});
			

			
			
			GridData gd = new GridData();
			gd.verticalAlignment = GridData.BEGINNING;
			gd.horizontalAlignment = GridData.END;
			gd.grabExcessHorizontalSpace = true;
			grp.setLayoutData(gd);
			
			
			
			
		}
	}
	
	public Landmark getLandmark () {
		
		double latD;
		double lonD;
		float altF;
		
		try {
			latD = Double.parseDouble(lat.getText());
			lonD = Double.parseDouble(lon.getText());
			// altF = Float.parseFloat(alt.getText());
			altF = 0.0F;
		} catch (NumberFormatException e) {
			return null;
		}
	
		QualifiedCoordinates qc = new QualifiedCoordinates(latD, lonD, altF, 0F, 0F);
		
		AddressInfo ai = new AddressInfo();
		
		Landmark l = new Landmark(name.getText(), desc.getText(), qc, ai);
		
		return l;
	}
	
}