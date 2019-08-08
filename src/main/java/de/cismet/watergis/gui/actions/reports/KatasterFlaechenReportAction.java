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
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.download.FakeFileDownload;

import de.cismet.watergis.gui.dialog.KatasterFlaechenReportDialog;

import de.cismet.watergis.reports.KatasterflaechenReport;
import de.cismet.watergis.reports.types.Flaeche;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterFlaechenReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterFlaechenReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FlaechenReportAction object.
     */
    public KatasterFlaechenReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Flächenthema";
        String tooltiptext = "Flächenauswertung";
        String mnemonic = "F";

        try {
            text = NbBundle.getMessage(KatasterFlaechenReportAction.class,
                    "KatasterFlaechenReportAction.text");
            tooltiptext = NbBundle.getMessage(
                    KatasterFlaechenReportAction.class,
                    "KatasterFlaechenReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(KatasterFlaechenReportAction.class,
                    "KatasterFlaechenReportAction.mnemonic");
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
            StaticSwingTools.showDialog(KatasterFlaechenReportDialog.getInstance());

            if (!KatasterFlaechenReportDialog.getInstance().isCancelled()) {
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
                            final List<Flaeche> flList = new ArrayList<Flaeche>();
                            final List<Integer> baCdList = new ArrayList<Integer>();

                            if (KatasterFlaechenReportDialog.getInstance().isSelectionFl()) {
                                for (final FeatureServiceFeature feature
                                            : KatasterFlaechenReportDialog.getInstance().getSelectedFl()) {
                                    final Flaeche fl = new Flaeche();
                                    fl.setGeom(feature.getGeometry());
                                    fl.setAttr1(feature.getProperty(
                                            KatasterFlaechenReportDialog.getInstance().getAttr1()));
                                    fl.setAttr2(feature.getProperty(
                                            KatasterFlaechenReportDialog.getInstance().getAttr2()));
                                    flList.add(fl);
                                }
                            } else {
                                final AbstractFeatureService service = KatasterFlaechenReportDialog.getInstance()
                                            .getFlaechenService();
                                service.initAndWait();
                                final List<FeatureServiceFeature> features = service.getFeatureFactory()
                                            .createFeatures(service.getQuery(), null, null, 0, 0, null);

                                for (final FeatureServiceFeature feature
                                            : features) {
                                    final Flaeche fl = new Flaeche();
                                    fl.setGeom(feature.getGeometry());
                                    fl.setAttr1(feature.getProperty(
                                            KatasterFlaechenReportDialog.getInstance().getAttr1()));
                                    fl.setAttr2(feature.getProperty(
                                            KatasterFlaechenReportDialog.getInstance().getAttr2()));
                                    flList.add(fl);
                                }
                            }

                            if (KatasterFlaechenReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : KatasterFlaechenReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            final KatasterflaechenReport gr = new KatasterflaechenReport();
                            int[] gew = new int[baCdList.size()];

                            if (baCdList.isEmpty()) {
                                gew = null;
                            } else {
                                for (int i = 0; i < baCdList.size(); ++i) {
                                    gew[i] = baCdList.get(i);
                                }
                            }

                            return gr.createFlaechenReport(flList.toArray(new Flaeche[flList.size()]), gew);
                        }

                        @Override
                        protected void done() {
                            try {
                                DownloadManager.instance().add(new FakeFileDownload(get()));
                            } catch (Exception e) {
                                LOG.error("Error while performing the gewaesser report.", e);
                            }
                        }
                    };

                wdt.start();
            }
        } catch (Exception ex) {
            LOG.error("Error while creating gewaesser report", ex);
        }
    }
}
