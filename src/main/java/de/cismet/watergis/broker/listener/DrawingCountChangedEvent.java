/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.broker.listener;

import java.util.EventObject;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DrawingCountChangedEvent extends EventObject {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionModeChangedEvent object.
     *
     * @param  source  DOCUMENT ME!
     */
    public DrawingCountChangedEvent(final Object source) {
        super(source);
    }
}
