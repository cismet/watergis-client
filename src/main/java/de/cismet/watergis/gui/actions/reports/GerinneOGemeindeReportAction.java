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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

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

import de.cismet.cids.custom.watergis.server.search.AllGemeinden;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.dialog.GerinneOGemeindeReportDialog;

import de.cismet.watergis.reports.GerinneOGemeindeReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOGemeindeReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOGemeindeReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public GerinneOGemeindeReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Gemeinden";
        String tooltiptext = "Gemeindenauswertung";
        String mnemonic = "E";

        try {
            text = NbBundle.getMessage(GerinneOGemeindeReportAction.class,
                    "GerinneOGemeindeReportAction.text");
            tooltiptext = NbBundle.getMessage(
                    GerinneOGemeindeReportAction.class,
                    "GerinneOGemeindeReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(GerinneOGemeindeReportAction.class,
                    "GerinneOGemeindeReportAction.mnemonic");
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
            StaticSwingTools.showDialog(GerinneOGemeindeReportDialog.getInstance());

            if (!GerinneOGemeindeReportDialog.getInstance().isCancelled()) {
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
                            final List<Integer> gmdList = new ArrayList<Integer>();

                            if (GerinneOGemeindeReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneOGemeindeReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            if (GerinneOGemeindeReportDialog.getInstance().isSelectionGmd()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneOGemeindeReportDialog.getInstance().getSelectedGmd()) {
                                    gmdList.add((Integer)feature.getProperty("gmd_nr"));
                                }
                            } else {
                                final CidsServerSearch search = new AllGemeinden(baCdList);

                                final User user = SessionManager.getSession().getUser();
                                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                            .getProxy().customServerSearch(user, search);

                                if ((attributes != null) && !attributes.isEmpty()) {
                                    for (final ArrayList f : attributes) {
                                        gmdList.add((Integer)f.get(0));
                                    }
                                }
                            }

                            final GerinneOGemeindeReport gr = new GerinneOGemeindeReport();
                            final int[] gmd = new int[gmdList.size()];
                            int[] gew = new int[baCdList.size()];

                            for (int i = 0; i < gmdList.size(); ++i) {
                                gmd[i] = gmdList.get(i);
                            }

                            if (baCdList.isEmpty()) {
                                gew = null;
                            } else {
                                for (int i = 0; i < baCdList.size(); ++i) {
                                    gew[i] = baCdList.get(i);
                                }
                            }
                            return gr.createGemeindeReport(gmd, gew);
                        }

                        @Override
                        protected void done() {
                            try {
                                DownloadManager.instance().add(new FakeFileDownload(get()));
                            } catch (Exception e) {
                                LOG.error("Error while performing the offene Gerinne report.", e);
                            }
                        }
                    };

                wdt.start();
            }
        } catch (Exception ex) {
            LOG.error("Error while creating gerinne offen gemeinden report", ex);
        }
    }
}
