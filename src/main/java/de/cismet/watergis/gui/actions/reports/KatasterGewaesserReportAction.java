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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.watergis.server.search.RouteEnvelopes;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.dialog.KatasterGewaesserReportDialog;

import de.cismet.watergis.reports.KatasterGewaesserReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterGewaesserReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterGewaesserReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public KatasterGewaesserReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Gewässer";
        String tooltiptext = "Gewässer";
        String mnemonic = "E";

        try {
            text = NbBundle.getMessage(KatasterGewaesserReportAction.class,
                    "GewaesserGewReportAction.text");
            tooltiptext = NbBundle.getMessage(
                    KatasterGewaesserReportAction.class,
                    "GewaesserGewReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(KatasterGewaesserReportAction.class,
                    "GewaesserGewReportAction.mnemonic");
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
        final int result = JOptionPane.showOptionDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    KatasterGewaesserReportAction.class,
                    "KatasterGewaesserReportAction.actionPerformed.message"),
                NbBundle.getMessage(
                    KatasterGewaesserReportAction.class,
                    "KatasterGewaesserReportAction.actionPerformed.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Weiter zur Statistik", "Abbrechen" },
                null);

        if (result == JOptionPane.YES_OPTION) {
            try {
                StaticSwingTools.showDialog(KatasterGewaesserReportDialog.getInstance());

                if (!KatasterGewaesserReportDialog.getInstance().isCancelled()) {
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

                                if (KatasterGewaesserReportDialog.getInstance().isSelectionGew()) {
                                    for (final FeatureServiceFeature feature
                                                : KatasterGewaesserReportDialog.getInstance().getSelectedGew()) {
                                        baCdList.add((Integer)feature.getProperty("id"));
                                    }
                                } else {
                                    if (AppBroker.getInstance().isGu()) {
                                        CidsServerSearch search;
                                        final String praefixGroup = ((AppBroker.getInstance().getOwnWwGr() != null)
                                                ? (String)AppBroker.getInstance().getOwnWwGr().getProperty(
                                                    "praefixgroup") : null);

                                        if (praefixGroup != null) {
                                            search = new RouteEnvelopes(" dlm25wPk_ww_gr1.owner = '"
                                                            + AppBroker.getInstance().getOwner()
                                                            + "' or dlm25wPk_ww_gr1.praefixgroup = '" + praefixGroup
                                                            + "'");
                                        } else {
                                            search = new RouteEnvelopes(" dlm25wPk_ww_gr1.owner = '"
                                                            + AppBroker.getInstance().getOwner() + "'");
                                        }

                                        final User user = SessionManager.getSession().getUser();
                                        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                                    .getProxy().customServerSearch(user, search);

                                        if ((attributes != null) && !attributes.isEmpty()) {
                                            for (final ArrayList f : attributes) {
                                                baCdList.add((Integer)f.get(2));
                                            }
                                        }
                                    }
                                }

                                final KatasterGewaesserReport gr = new KatasterGewaesserReport();
                                int[] gew = new int[baCdList.size()];

                                if (baCdList.isEmpty()) {
                                    gew = null;
                                } else {
                                    for (int i = 0; i < baCdList.size(); ++i) {
                                        gew[i] = baCdList.get(i);
                                    }
                                }

                                return gr.createGewaesserReport(gew, wd);
                            }

                            @Override
                            protected void done() {
                                try {
                                    DownloadManager.instance()
                                            .add(new FakeFileDownload(get(), "Statistik: Kataster->Gewässer"));
                                } catch (Exception e) {
                                    LOG.error("Error while performing the Kataster Gewaesser report.", e);
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
}
