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

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.checks.AbstractCheckAction;
import de.cismet.watergis.gui.actions.checks.AbstractCheckResult;
import de.cismet.watergis.gui.actions.checks.AusbauCheckAction;
import de.cismet.watergis.gui.actions.checks.BasicRoutesCheckAction;
import de.cismet.watergis.gui.actions.checks.BauwerkeCheckAction;
import de.cismet.watergis.gui.actions.checks.GWKConnectionCheckAction;
import de.cismet.watergis.gui.actions.checks.LawaCheckAction;
import de.cismet.watergis.gui.actions.checks.SonstigeCheckAction;
import de.cismet.watergis.gui.actions.checks.VerwaltungCheckAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CheckBackgroundWorker {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CheckBackgroundWorker.class);
    private static final int THREADS = 1;

    //~ Instance fields --------------------------------------------------------

    private final AbstractCheckAction[] CHECKS =
        new AbstractCheckAction[(AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren") ? 7 : 5)];
    private final ThreadPoolExecutor threadExecutor;
    private final List<BackgroundCheckListener> listeners = new ArrayList<>();

    //~ Instance initializers --------------------------------------------------

    {
        CHECKS[0] = new BasicRoutesCheckAction();
        CHECKS[1] = new VerwaltungCheckAction();
        CHECKS[2] = new AusbauCheckAction();
        CHECKS[3] = new BauwerkeCheckAction();
        CHECKS[4] = new SonstigeCheckAction();

        if (AppBroker.getInstance().getOwner().equalsIgnoreCase("Administratoren")) {
            CHECKS[5] = new GWKConnectionCheckAction();
            CHECKS[6] = new LawaCheckAction();
        }
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CheckBackgroundWorker object.
     */
    public CheckBackgroundWorker() {
        threadExecutor = new ThreadPoolExecutor(
                THREADS,
                THREADS,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addBackgroundCheckListener(final BackgroundCheckListener l) {
        listeners.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   l  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeBackgroundCheckListener(final BackgroundCheckListener l) {
        return listeners.remove(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    public void fireBackgroundCheckListener(final BackgroundCheckEvent e) {
        for (final BackgroundCheckListener l : listeners) {
            l.checkComplete(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AbstractCheckAction[] getChecks() {
        return CHECKS;
    }

    /**
     * DOCUMENT ME!
     */
    public void start() {
        for (int index = 0; index < CHECKS.length; ++index) {
            threadExecutor.submit(new ActionExecutor(index, CHECKS[index], this));
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class ActionExecutor implements Runnable {

        //~ Instance fields ----------------------------------------------------

        private final CheckBackgroundWorker parent;
        private final AbstractCheckAction check;
        private final int index;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ActionExecutor object.
         *
         * @param  index   DOCUMENT ME!
         * @param  check   DOCUMENT ME!
         * @param  parent  DOCUMENT ME!
         */
        public ActionExecutor(final int index, final AbstractCheckAction check, final CheckBackgroundWorker parent) {
            this.check = check;
            this.parent = parent;
            this.index = index;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            try {
                final AbstractCheckResult result = check.startBackgroundCheck();
                final BackgroundCheckEvent e = new BackgroundCheckEvent(index, check, result, parent);

                fireBackgroundCheckListener(e);
            } catch (Exception ex) {
                LOG.error("Error during background check.", ex);
                final BackgroundCheckEvent e = new BackgroundCheckEvent(index, check, ex, parent);

                fireBackgroundCheckListener(e);
            }
        }
    }
}
