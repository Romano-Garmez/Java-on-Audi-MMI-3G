package org.placelab.test;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;
import org.placelab.eventsystem.SWTEventSystem;

public class SWTEventSystemTest implements Testable {

    Display display;
    Shell shell;
    EventSystem es;
    Thread main;
    Thread bg;
    boolean call1 = false;
    
    public String getName() {
        return "SWTEventSystemTest";
    }

    public void runTests(final TestResult result) throws Throwable {
        display = Display.getCurrent();
        // this is necessary since some other tests (that i probably wrote, actually)
        // don't dispose of their displays properly
        if(display == null) display = new Display();
        shell = new Shell(display, 0);
        es = new SWTEventSystem(display, shell);
        main = Thread.currentThread();
        bg = new Thread(new Runnable() {
            public void run() {
                es.notifyTransientEvent(new EventListener() {
                    public void callback(Object eventType, Object data) {
                        callback1((TestResult)data);
                    }
                }, result);
            }
        });
        bg.start();
        es.run();
        result.assertTrue(this, true, call1, "callback check");
    }
    
    private void callback1(TestResult result) {
        result.assertTrue(this, true, main == Thread.currentThread(), "thread check");
        call1 = true;
        shell.dispose();
    }

}
