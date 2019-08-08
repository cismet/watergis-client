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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.dialog.GerinneOGewaesserReportDialog;

import de.cismet.watergis.reports.GerinneOGewaesserReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOGewReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOGewReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public GerinneOGewReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Gewässer";
        String tooltiptext = "Gewässerauswertung";
        String mnemonic = "W";

        try {
            text = NbBundle.getMessage(GerinneOGewReportAction.class,
                    "GerinneOGewReportAction.text");
            tooltiptext = NbBundle.getMessage(GerinneOGewReportAction.class,
                    "GerinneOGewReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(GerinneOGewReportAction.class,
                    "GerinneOGewReportAction.mnemonic");
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
            StaticSwingTools.showDialog(GerinneOGewaesserReportDialog.getInstance());

            if (!GerinneOGewaesserReportDialog.getInstance().isCancelled()) {
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

                            if (GerinneOGewaesserReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneOGewaesserReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            final GerinneOGewaesserReport gr = new GerinneOGewaesserReport();
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
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    "Bei der Erstellung der Auswertung ist ein Fehler aufgetreten.\nEine Fehlerbeschreibung kann dem Logging entnommen werden",
                                    "Fehler",
                                    JOptionPane.ERROR_MESSAGE);
                                LOG.error("Error while performing the offene Gerinne gewaesser report.", e);
                            }
                        }
                    };

                wdt.start();
            }
        } catch (Exception ex) {
            LOG.error("Error while creating gerinne offen gewaesser report", ex);
        }
    }
}
