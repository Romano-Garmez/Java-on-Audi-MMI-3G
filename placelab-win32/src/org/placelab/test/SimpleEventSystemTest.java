package org.placelab.test;

import org.placelab.eventsystem.EventListener;
import org.placelab.eventsystem.EventSystem;
import org.placelab.eventsystem.SimpleEventSystem;

/**
 * 
 * 
 *
 */

public class SimpleEventSystemTest implements Testable {

    boolean call1 = false;
    Thread main;
    Thread bg;
    
    EventSystem es;
    
    public String getName() {
        return "SimpleEventSystemTest";
    }

    public void runTests(final TestResult result) throws Throwable {
        es = new SimpleEventSystem();
        main = new Thread(new Runnable() {
            public void run() {
                es.run();
            }
        });
        main.start();
        bg = new Thread(new Runnable() {
            public void run() {
                //System.out.println("hullo");
                es.notifyTransientEvent(new EventListener() {
                    public void callback(Object eventType, Object data) {
                        callback1((TestResult)data);
                    }
                }, result);
            }
        });
        bg.start();
        if(!call1) {
            // there's no way it should take more than 500ms for this to happen
            synchronized(this) { try { this.wait(500); } catch (InterruptedException ie) { } }
        }
        es.stop();
        result.assertTrue(this, true, call1, "callback check");
    }
    
    private void callback1(TestResult result) {
        //System.out.println("hi");
        result.assertTrue(this, true, main == Thread.currentThread(), "thread check");
        call1 = true;
        synchronized(this) { this.notify(); }
    }
    
    

}
