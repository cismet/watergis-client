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

import java.io.File;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.dialog.KatasterSbReportDialog;

import de.cismet.watergis.reports.KatasterSbReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterSbReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterSbReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public KatasterSbReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Schaubezirke";
        String tooltiptext = "Schaubezirke";
        String mnemonic = "S";

        try {
            text = NbBundle.getMessage(KatasterSbReportAction.class,
                    "SbReportAction.text");
            tooltiptext = NbBundle.getMessage(KatasterSbReportAction.class,
                    "SbReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(KatasterSbReportAction.class,
                    "SbReportAction.mnemonic");
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
            StaticSwingTools.showDialog(KatasterSbReportDialog.getInstance());

            if (!KatasterSbReportDialog.getInstance().isCancelled()) {
                final WaitingDialogThread<File> wdt = new WaitingDialogThread<File>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        // NbBundle.getMessage(SonstigeCheckAction.class,
                        // "SonstigeCheckAction.actionPerformed().dialog"),
                        "erstelle Auswertung",
                        null,
                        100,
                        true) {

                        @Override
                        protected File doInBackground() throws Exception {
                            final List<Integer> baCdList = new ArrayList<Integer>();

                            if (KatasterSbReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : KatasterSbReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            final KatasterSbReport gr = new KatasterSbReport();
                            int[] gew = new int[baCdList.size()];

                            if (baCdList.isEmpty()) {
                                gew = null;
                            } else {
                                for (int i = 0; i < baCdList.size(); ++i) {
                                    gew[i] = baCdList.get(i);
                                }
                            }

                            return gr.createGewaesserReport(gew);
                        }

                        @Override
                        protected void done() {
                            try {
                                DownloadManager.instance().add(new FakeFileDownload(get()));
                            } catch (Exception e) {
                                LOG.error("Error while performing the schaubezirke report.", e);
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
