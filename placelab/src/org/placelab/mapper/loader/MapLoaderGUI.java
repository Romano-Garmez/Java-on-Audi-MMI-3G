/*
 * Created on Aug 20, 2004
 *
 */
package org.placelab.mapper.loader;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.placelab.collections.Iterator;
import org.placelab.collections.LinkedList;
import org.placelab.collections.List;
import org.placelab.core.PlacelabProperties;
import org.placelab.core.TwoDCoordinate;
import org.placelab.eventsystem.SWTEventSystem;
import org.placelab.mapper.CompoundMapper;
import org.placelab.mapper.Mapper;
import org.placelab.util.Cmdline;

/**
 * A GUI application to load all worthwhile mappers from 
 * various sources of beacon data on the web.
 * 
 */
public class MapLoaderGUI implements SelectionListener {
	protected SWTEventSystem evs;
	protected Display display;
	protected Shell shell;
	protected TabFolder folder;
	protected Combo stateCombo, regionCombo;
	protected Text latitude1, latitude2;
	protected Text longitude1, longitude2;
	protected Composite wigleInfo;
	protected Label wigleNameLabel, wiglePassLabel;
	protected Text wigleName, wiglePass;
	protected Button deleteButton, loadButton;
	protected Label statusLabel, countLabel;
	protected Button[] buttons;
	
	protected MapSource[] sources;
	
	protected Mapper mapper;
	protected MapSourceLoader loader;
	
	public static void main(String args[]) {
		Cmdline.parse(args);
		try {
			List list = new LinkedList();
			String[] files = Cmdline.getStrayArgs();
			for (int i = 0; i < files.length; i++) {
				try {
					list.add(new FileMapSource(files[i]));
				} catch (FileNotFoundException e) {}
			}
			
			// XXX: change mapper business
			MapLoaderGUI loader = new MapLoaderGUI(list);
			loader.run();
		} catch (Exception e) {
			System.err.println("MapLoaderGUI puked: " + e);
			System.exit(-1);
		}
	}
	
	public MapLoaderGUI(List extraSources) throws IOException {
		List defaultSources = MapSourceLoader.getDefaultSources();
		sources = new MapSource[defaultSources.size() + extraSources.size()];
		int n = 0;
		for (int i = 0; i < defaultSources.size(); i++)
			sources[n++] = (MapSource)defaultSources.get(i);
		for (int i = 0; i < extraSources.size(); i++)
			sources[n++] = (MapSource)extraSources.get(i);
		
		display = Display.getDefault();
		
		shell = new Shell(display);
		shell.setText("Placelab MapLoader");
		shell.setLayout(new GridLayout(2, false));

		constructShell();
	
		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		shell.open();
		evs = new SWTEventSystem(display, shell);
		
		mapper = null;
		loader = null;
		
		// XXX: change defaults?
		latitude1.setText("47.0");
		latitude2.setText("48.0");
		longitude1.setText("-121.0");
		longitude2.setText("-123.0");
		
		// insert the states in sorted order
		Iterator iter = StateRegions.states();
		while (iter.hasNext()) {
			String state = (String)iter.next();
			boolean placed = false;
			for (int i = 0; i < stateCombo.getItemCount(); i++) {
				if (stateCombo.getItem(i).compareTo(state) > 0) {
					stateCombo.add(state, i);
					placed = true;
					break;
				}
			}
			if (!placed) stateCombo.add(state);
		}
		
		stateCombo.select(0);
		
		// insert the regions
		iter = EuropeRegions.regions();
		while (iter.hasNext()) {
			String region = (String)iter.next();
			regionCombo.add(region);
		}
		
		regionCombo.select(0);
	}
	
	protected void constructShell() {
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		Label label = new Label(shell, SWT.LEFT);
		label.setText("Data Sources:");
		label.setLayoutData(data);
		
		buttons = new Button[sources.length];
		
		boolean enableWigleInfo = false;
		
		for (int i = 0; i < sources.length; i++) {
			buttons[i] = new Button(shell, SWT.CHECK);
			buttons[i].setText(sources[i].getName());
			if(sources[i].getName().equals("Wigle.net")) {
			    enableWigleInfo = sources[i].isDefault();
			    final Button button = buttons[i];
			    buttons[i].addSelectionListener(new SelectionListener() {

                    public void widgetSelected(SelectionEvent arg0) {
                        boolean on = button.getSelection();
                        wigleInfo.setVisible(on);
                        int changeY;
                        if(on) {
                            ((GridData)wigleInfo.getLayoutData()).heightHint = 50;
                            changeY = 50;
                        } else {
                            ((GridData)wigleInfo.getLayoutData()).heightHint = 0;
                            changeY = -50;
                        }
                        shell.setSize(shell.getSize().x, shell.getSize().y + changeY);
                        shell.layout();
                    }

                    public void widgetDefaultSelected(SelectionEvent arg0) {
                        widgetSelected(arg0);
                    }
			        
			    });
			}
			buttons[i].setSelection(sources[i].isDefault());
		}
		
		if (sources.length % 2 == 1)
			new Label(shell, SWT.NONE);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		wigleInfo = new Composite(shell, SWT.NONE);
		wigleInfo.setLayoutData(data);
		wigleInfo.setLayout(new GridLayout(2, false));
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		
		wigleNameLabel = new Label(wigleInfo, SWT.HORIZONTAL | SWT.LEFT);
		wigleNameLabel.setText("Wigle username: ");
		wigleNameLabel.setLayoutData(data);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		
		wiglePassLabel = new Label(wigleInfo, SWT.HORIZONTAL | SWT.LEFT);
		wiglePassLabel.setText("Wigle password: ");
		wiglePassLabel.setLayoutData(data);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		
		wigleName = new Text(wigleInfo, SWT.SINGLE | SWT.BORDER);
		wigleName.setText(PlacelabProperties.get("placelab.wigle_username"));
		wigleName.setLayoutData(data);
		wigleName.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent arg0) {
                PlacelabProperties.set("placelab.wigle_username", 
                        wigleName.getText().trim());
            }
		    
		});
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 1;
		
		wiglePass = new Text(wigleInfo, SWT.SINGLE | SWT.BORDER);
		wiglePass.setEchoChar('*');
		wiglePass.setText(PlacelabProperties.get("placelab.wigle_password"));
		wiglePass.setLayoutData(data);
		wiglePass.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent arg0) {
                PlacelabProperties.set("placelab.wigle_password", 
                        wiglePass.getText().trim());
            }
		    
		});
		
		wigleInfo.setVisible(enableWigleInfo);
		if(!enableWigleInfo) {
		    ((GridData)wigleInfo.getLayoutData()).heightHint = 0;
		}
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		folder = new TabFolder(shell, SWT.NONE);
		folder.setLayoutData(data);
		
		Composite composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		
		TabItem item = new TabItem(folder, SWT.NONE);
		item.setText("Latitude/Longitude");
		item.setControl(composite);
		
		label = new Label(composite, SWT.RIGHT);
		label.setText("Latitude Range:");
		
		latitude1 = new Text(composite, SWT.SINGLE | SWT.BORDER);
		latitude1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		latitude2 = new Text(composite, SWT.SINGLE | SWT.BORDER);
		latitude2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		label = new Label(composite, SWT.RIGHT);
		label.setText("Longitude Range:");
		
		longitude1 = new Text(composite, SWT.SINGLE | SWT.BORDER);
		longitude1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		longitude2 = new Text(composite, SWT.SINGLE | SWT.BORDER);
		longitude2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		item = new TabItem(folder, SWT.NONE);
		item.setText("US State");
		item.setControl(composite);

		label = new Label(composite, SWT.RIGHT);
		label.setText("State:");
		
		stateCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		stateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		composite = new Composite(folder, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		item = new TabItem(folder, SWT.NONE);
		item.setText("Europe");
		item.setControl(composite);
		
		label = new Label(composite, SWT.RIGHT);
		label.setText("Region:");
		
		regionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		regionCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		
		
		deleteButton = new Button(shell, SWT.CHECK);
		deleteButton.setText("Clear Map First");
		deleteButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		deleteButton.setSelection(true);
		
		loadButton = new Button(shell, SWT.PUSH);
		loadButton.setText("Start Loading");
		loadButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		loadButton.addSelectionListener(this);

		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;

		label = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(data);
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		statusLabel = new Label(shell, SWT.NONE);
		statusLabel.setLayoutData(data);
		setStatus("Idle.");
		
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		
		countLabel = new Label(shell, SWT.NONE);
		countLabel.setLayoutData(data);
		setCount(0);
		
		shell.layout();
	}
	
	public void run() {
		evs.run();
		if (loader != null) loader.die();
	}
	
	protected boolean isValidLatitude(double lat) {
		return lat >= -90.0 && lat <= 90.0;
	}
	
	protected boolean isValidLongitude(double lon) {
		return lon >= -180.0 && lon <= 180.0;
	}
	
	protected boolean isValidInput() {
		switch (folder.getSelectionIndex()) {
		case 0:
			try {
				if (!isValidLatitude(Double.parseDouble(latitude1.getText()))) return false;
				if (!isValidLatitude(Double.parseDouble(latitude2.getText()))) return false;
				if (!isValidLongitude(Double.parseDouble(longitude1.getText()))) return false;
				if (!isValidLongitude(Double.parseDouble(longitude2.getText()))) return false;
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
	
		case 1:
		case 2:
			return true;
			
		default:
			return false;
		}
	}
	
	protected void setStatus(String status) {
		statusLabel.setText("Status: " + status);
	}
	
	protected void setCount(int count) {
		countLabel.setText("Beacons Loaded: " + count);	
	}
	
	public void loadMap() {
		if (!isValidInput()) {
			MessageBox message = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			message.setMessage("The area specification does not define a valid region.");
			message.setText("Input Error");
			message.open();
			return;
		}
		
		TwoDCoordinate one;
		TwoDCoordinate two;
		
		switch (folder.getSelectionIndex()) {
		case 0:
			one = new TwoDCoordinate(latitude1.getText(), longitude1.getText());
			two = new TwoDCoordinate(latitude2.getText(), longitude2.getText());
			break;
			
		case 1:
			one = StateRegions.northEast(stateCombo.getText());
			two = StateRegions.southWest(stateCombo.getText());
			break;
			
		case 2:
			one = EuropeRegions.northEast(regionCombo.getText());
			two = EuropeRegions.southWest(regionCombo.getText());
			break;
			
		default:
			System.err.println("Unknown tab");
			return;
		}
		
		mapper = CompoundMapper.createDefaultMapper(true, false);
		
		loader = new MapSourceLoader(mapper, deleteButton.getSelection());
		loader.setArea(one, two);
		
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].getSelection())
				loader.addSource(sources[i]);
		}
		
		setEnabled(false);
		setStatus("Loading sources...");
		
		new Thread(loader).start();
		display.timerExec(0, new LoaderPoker());
	}
	
	public void finishLoad() {
		setEnabled(true);
		String error = loader.getError();
		if (error != null) {
			MessageBox errorDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			errorDialog.setText("Loading Error");
			errorDialog.setMessage("An error occurred while downloading:\n" + error);
			errorDialog.open();
		}
		mapper.close();
		mapper = null;
		loader = null;
	}
	
	public void setEnabled(boolean enabled) {
		folder.setEnabled(enabled);
		stateCombo.setEnabled(enabled);
		latitude1.setEnabled(enabled);
		latitude2.setEnabled(enabled);
		longitude1.setEnabled(enabled);
		longitude2.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
		loadButton.setEnabled(enabled);
		
		for (int i = 0; i < buttons.length; i++)
			buttons[i].setEnabled(enabled);
	}
	
	public void widgetSelected(SelectionEvent event) {
		loadMap();
	}

	public void widgetDefaultSelected(SelectionEvent event) {}
	
	private class LoaderPoker implements Runnable {
		public void run() {
			setStatus(loader.getCurrentStatus());
			setCount(loader.getBeaconCount());
			
			if (loader.isDone()) {
				finishLoad();
			} else {
				display.timerExec(500, this);
			}
		}		
	}
}
