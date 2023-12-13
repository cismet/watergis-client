/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.check;

import java.util.EventObject;

import de.cismet.watergis.gui.actions.checks.AbstractCheckAction;
import de.cismet.watergis.gui.actions.checks.AbstractCheckResult;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class BackgroundCheckEvent extends EventObject {

    //~ Instance fields --------------------------------------------------------

    private final int index;
    private AbstractCheckResult result = null;
    private Exception exception = null;
    private final AbstractCheckAction check;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BackgroundCheckEvent object.
     *
     * @param  index   DOCUMENT ME!
     * @param  check   DOCUMENT ME!
     * @param  result  DOCUMENT ME!
     * @param  source  DOCUMENT ME!
     */
    public BackgroundCheckEvent(final int index,
            final AbstractCheckAction check,
            final AbstractCheckResult result,
            final Object source) {
        super(source);
        this.result = result;
        this.index = index;
        this.check = check;
    }

    /**
     * Creates a new BackgroundCheckEvent object.
     *
     * @param  index      DOCUMENT ME!
     * @param  check      DOCUMENT ME!
     * @param  exception  DOCUMENT ME!
     * @param  source     DOCUMENT ME!
     */
    public BackgroundCheckEvent(final int index,
            final AbstractCheckAction check,
            final Exception exception,
            final Object source) {
        super(source);
        this.exception = exception;
        this.index = index;
        this.check = check;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AbstractCheckResult getResult() {
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Exception getException() {
        return exception;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the check
     */
    public AbstractCheckAction getCheck() {
        return check;
    }
}
