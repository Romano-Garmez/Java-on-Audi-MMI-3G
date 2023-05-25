package org.placelab.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.placelab.util.swt.AffineTransform;
import org.placelab.util.swt.GlyphComposite;
import org.placelab.util.swt.GlyphHolder;
import org.placelab.util.swt.GlyphLabel;
import org.placelab.util.swt.GlyphRectangle;

public class GlyphDrawTest {
    
    Display display;
    Shell shell;
    GlyphHolder holder;
    GlyphComposite composite;
    GlyphRectangle rect;
    GlyphLabel label;
    

    public static void main(String args[]) {
        GlyphDrawTest g = new GlyphDrawTest();
        g.test();

    }
    
    private void test() {
        buildScene();
        //AffineTransform t= AffineTransform.getRotateInstance(Math.PI / 4.0, -60, -60);
        //AffineTransform t = AffineTransform.getRotateInstance(Math.PI); //AffineTransform.getTranslateInstance(5, 5);  //getScaleInstance(2, 1);
        //AffineTransform t = AffineTransform.getScaleInstance(2, 2);
        //t.concatenate(AffineTransform.getShearInstance(.5, .5));
        //t.preConcatenate(AffineTransform.getTranslateInstance(-30, 0));
        
        //t.preConcatenate(AffineTransform.getScaleInstance(2, 2));
        
        //System.out.println(t.getScaleX());
        //System.out.println(t.getScaleY());
        
        //label.setTransform(t);
        
        AffineTransform t = AffineTransform.getScaleInstance(2, 2);
        
        rect.setTransform(t);
        label.setTransform(t);
        
        System.out.println(rect.getBounds());
        //System.out.println(label.getBounds());
        
        spin();
    }
    
    
    private void buildScene() {
        display = new Display();
        shell = new Shell(display);
        shell.setSize(200, 200);
        holder = new GlyphHolder(shell, 0);
        holder.setSize(200, 200);
        holder.setBackground(new Color(display, 0, 0, 0));
        
        composite = holder.getChild();
        shell.open();
        rect = new GlyphRectangle(holder, 0);
        rect.set(10, 10, 50, 50);
        rect.setForeground(display.getSystemColor(SWT.COLOR_RED));
        rect.setBackground(display.getSystemColor(SWT.COLOR_RED));
        label = new GlyphLabel(holder, 0);
        label.setLocation(10, 10);
        label.setBackground(null);
        label.setText("Hello World");
    }
    
    private void spin() {
        //shell.dispose();
		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}
    }
    

}
