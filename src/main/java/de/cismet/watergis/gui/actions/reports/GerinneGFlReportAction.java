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
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GerinneGeschlFlaechenReportDialog;

import de.cismet.watergis.reports.GerinneGFlReport;
import de.cismet.watergis.reports.types.Flaeche;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneGFlReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGFlReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public GerinneGFlReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-analytics-piechart.png");
        String text = "Flächenthema";
        String tooltiptext = "Flächenthemaauswertung";
        String mnemonic = "F";

        try {
            text = NbBundle.getMessage(GerinneGFlReportAction.class,
                    "GerinneGFlReportAction.text");
            tooltiptext = NbBundle.getMessage(GerinneGFlReportAction.class,
                    "GerinneGFlReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(GerinneGFlReportAction.class,
                    "GerinneGFlReportAction.mnemonic");
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
            StaticSwingTools.showDialog(GerinneGeschlFlaechenReportDialog.getInstance());

            if (!GerinneGeschlFlaechenReportDialog.getInstance().isCancelled()) {
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
                            final List<Flaeche> flList = new ArrayList<Flaeche>();

                            if (GerinneGeschlFlaechenReportDialog.getInstance().isSelectionGew()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneGeschlFlaechenReportDialog.getInstance().getSelectedGew()) {
                                    baCdList.add((Integer)feature.getProperty("id"));
                                }
                            }

                            if (GerinneGeschlFlaechenReportDialog.getInstance().isSelectionFl()) {
                                for (final FeatureServiceFeature feature
                                            : GerinneGeschlFlaechenReportDialog.getInstance().getSelectedFl()) {
                                    final Flaeche fl = new Flaeche();
                                    fl.setGeom(feature.getGeometry());
                                    fl.setAttr1(feature.getProperty(
                                            GerinneGeschlFlaechenReportDialog.getInstance().getAttr1()));
                                    fl.setAttr2(feature.getProperty(
                                            GerinneGeschlFlaechenReportDialog.getInstance().getAttr2()));
                                    flList.add(fl);
                                }
                            } else {
                                final AbstractFeatureService service = GerinneGeschlFlaechenReportDialog.getInstance()
                                            .getFlaechenService();
                                service.initAndWait();
                                final List<FeatureServiceFeature> features = service.getFeatureFactory()
                                            .createFeatures(service.getQuery(), null, null, 0, 0, null);

                                for (final FeatureServiceFeature feature
                                            : features) {
                                    final Flaeche fl = new Flaeche();
                                    fl.setGeom(feature.getGeometry());
                                    fl.setAttr1(feature.getProperty(
                                            GerinneGeschlFlaechenReportDialog.getInstance().getAttr1()));
                                    fl.setAttr2(feature.getProperty(
                                            GerinneGeschlFlaechenReportDialog.getInstance().getAttr2()));
                                    flList.add(fl);
                                }
                            }

                            final GerinneGFlReport gr = new GerinneGFlReport();
                            int[] gew = new int[baCdList.size()];

                            if (baCdList.isEmpty()) {
                                gew = null;
                            } else {
                                for (int i = 0; i < baCdList.size(); ++i) {
                                    gew[i] = baCdList.get(i);
                                }
                            }

                            gr.createFlaechenReport(flList.toArray(new Flaeche[flList.size()]), gew);

                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (Exception e) {
                                LOG.error("Error while performing the geschlossene Gerinne report.", e);
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
