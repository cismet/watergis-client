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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SelectionModeChangedEvent {

    //~ Instance fields --------------------------------------------------------

    private Object source;
    private String oldMode;
    private String newMode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionModeChangedEvent object.
     */
    public SelectionModeChangedEvent() {
    }

    /**
     * Creates a new SelectionModeChangedEvent object.
     *
     * @param  source   DOCUMENT ME!
     * @param  oldMode  DOCUMENT ME!
     * @param  newMode  DOCUMENT ME!
     */
    public SelectionModeChangedEvent(final Object source, final String oldMode, final String newMode) {
        this.source = source;
        this.oldMode = oldMode;
        this.newMode = newMode;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the source
     */
    public Object getSource() {
        return source;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  source  the source to set
     */
    public void setSource(final Object source) {
        this.source = source;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the oldMode
     */
    public String getOldMode() {
        return oldMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  oldMode  the oldMode to set
     */
    public void setOldMode(final String oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the newMode
     */
    public String getNewMode() {
        return newMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  newMode  the newMode to set
     */
    public void setNewMode(final String newMode) {
        this.newMode = newMode;
    }
}
