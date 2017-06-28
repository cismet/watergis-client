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
package de.cismet.watergis.gui.actions.reports;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GerinneGeschlSbReportDialog;

import de.cismet.watergis.reports.GerinneGSbReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneGSbReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGSbReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public GerinneGSbReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Schaubezirke";
        String tooltiptext = "Schaubezirke";
        String mnemonic = "S";

        try {
            text = NbBundle.getMessage(GerinneGSbReportAction.class,
                    "GerinneGSbReportAction.text");
            tooltiptext = NbBundle.getMessage(GerinneGSbReportAction.class,
                    "GerinneGSbReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(GerinneGSbReportAction.class,
                    "GerinneGSbReportAction.mnemonic");
        } catch (MissingResourceException e) {
            LOG.error("Couldn't find resources. Using fallback settings.", e);
        }

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

//        putValue(SHORT_DESCRIPTION, tooltiptext);
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(NAME, text);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            StaticSwingTools.showDialog(GerinneGeschlSbReportDialog.getInstance());

            if (!GerinneGeschlSbReportDialog.getInstance().isCancelled()) {
                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        // NbBundle.getMessage(SonstigeCheckAction.class,
                        // "SonstigeCheckAction.actionPerformed().dialog"),
                        "erstelle Auswertung",
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            final List<Integer> baCdList = new ArrayList<Integer>();

                            if (GerinneGeschlSbReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneGeschlSbReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            final GerinneGSbReport gr = new GerinneGSbReport();
                            int[] gew = new int[baCdList.size()];

                            if (baCdList.isEmpty()) {
                                gew = null;
                            } else {
                                for (int i = 0; i < baCdList.size(); ++i) {
                                    gew[i] = baCdList.get(i);
                                }
                            }
                            gr.createGerinneGewaesserReport(gew);
                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (Exception e) {
                                LOG.error("Error while performing the geschlossene Gerinne sb report.", e);
                            }
                        }
                    };

                wdt.start();
            }
        } catch (Exception ex) {
            LOG.error("Error while creating gemeinden report", ex);
        }
    }
}