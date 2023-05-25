
package org.placelab.demo.jsr0179;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * DebugWindow provides a PrintStream interface for debugging output. Output
 * will be shown in an SWT window. It may be integrated within existing SWT apps
 * or run standalone, in which case it takes care of running the SWT event loop.
 */

public class DebugWindow extends PrintStream {
	
	public static Display display; 
	
	private static boolean standalone = false;
	
	static {
		if (Display.getCurrent() == null) {
			standalone = true;
			display = Display.getDefault();
		} else {
			display = Display.getCurrent();
		}
	}
	
	public Shell shell;
	private PipedInputStream pin = null;
	private Text text;
	private InputScanner scanner = null;
	
	public DebugWindow (String title, boolean autoFlush) {
		super(new PipedOutputStream(), autoFlush);
		
		try {
			pin = new PipedInputStream((PipedOutputStream) out);
		} catch (IOException e) {
			System.err.println("Error setting up streams: " + e);
			e.printStackTrace();
			return;
		}
		
		shell = new Shell(Display.getCurrent(), SWT.DIALOG_TRIM | SWT.RESIZE);
		shell.setText(title);
		
		GridLayout gl = new GridLayout();
		gl.marginHeight = 4;
		gl.marginWidth = 4;
		gl.numColumns = 1;
		shell.setLayout(gl);
		
		text = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		
		{
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.grabExcessVerticalSpace = true;
			gd.horizontalAlignment = GridData.FILL;
			gd.verticalAlignment = GridData.FILL;
			text.setLayoutData(gd);
		}
		
		Button close = new Button(shell, SWT.PUSH);
		close.setText("Hide");
		close.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				DebugWindow.this.hide();
			}
			
			public void widgetDefaultSelected (SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		{
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.END;
			gd.verticalAlignment = GridData.BEGINNING;
			close.setLayoutData(gd);
		}
	
	}
	
	public DebugWindow (String title) {
		this(title, true);
	}
	
	public DebugWindow () {
		this("Debug Window", true);
	}
	
	public void runEventLoop () {
		
		while (!shell.isDisposed()) {
			
				if (!display.readAndDispatch()) {	
					display.sleep();
				}	
				
		
				
		}
		
		try {
			display.dispose();
			display.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void show () {
		shell.open();
		
		scanner = new InputScanner();
		scanner.start();
		
		if (standalone)
			runEventLoop();
	}
	
	public void hide () {
		
		scanner.cancel();
		scanner = null;
		
		if (shell != null)
			shell.close();
		
		if (standalone) {
			shell.dispose();
			System.exit(0);
		}
		
	}
	
	class InputScanner extends Thread {
		
		private static final int POLL_INTERVAL = 100; // milliseconds
		
		private volatile boolean scan = true;

		private byte[] buffer;
		
		public void run () {
			while (scan) {
				
				for (;;) {
					
					int avail;
					
					try {
						avail = DebugWindow.this.pin.available();
					} catch (IOException e) {
						avail = 0;
					}
					
					if (avail == 0) {
						try {
							Thread.sleep(POLL_INTERVAL);
						} catch (InterruptedException e) { }
						continue;
					}
				
					buffer = new byte[avail];
					
					try {
						DebugWindow.this.pin.read(buffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					DebugWindow.display.syncExec(new Runnable() { 
						public void run () {
							DebugWindow.this.text.append(new String(buffer));
						}
					});
				}
				
			}
			
		}
		
		public void cancel () {
			scan = false;
		}
	}
	
	public static DebugWindow debug;
	
	public static void main (String[] args) {
		
		debug = new DebugWindow("blah");
		
		
		debug.show();
		
		Thread another = new Thread(new Runnable(){ 
			public void run () {
				for (int i = 0; i<1000; i++) { 
					debug.println("i = " + i+"\r");
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) { }
				}
				
			}
		});
		another.start();
		
		
		while (!debug.shell.isDisposed()) {
			
			
			if (!DebugWindow.display.readAndDispatch()) {	
				DebugWindow.display.sleep();
			}
			
		}
		
		DebugWindow.display.dispose();
		
		
	}
}
