/*
 * Created on 15-Sep-2004
 *
 */
package org.placelab.midp;


/**
 *
 */
public interface UIComponent {

    /** used by one component to activate another component */
    public void showUI(UIComponent from);
}
