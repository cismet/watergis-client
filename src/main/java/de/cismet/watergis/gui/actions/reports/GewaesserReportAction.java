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

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.watergis.server.search.AllRoutes;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.*;
import de.cismet.watergis.gui.dialog.GewaesserReportDialog;

import de.cismet.watergis.reports.GewaesserReport;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GewaesserReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GewaesserReportAction.class);
    private static final MetaClass FG_BA = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

    //~ Instance fields --------------------------------------------------------

    private ExportAction export;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public GewaesserReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-contact-businesscard.png");
        String text = "GU";
        String tooltiptext = "Gewässerauswertung";
        String mnemonic = "G";

        try {
            text = NbBundle.getMessage(GewaesserReportAction.class,
                    "GewaesserReportAction.text");
            tooltiptext = NbBundle.getMessage(GewaesserReportAction.class,
                    "GewaesserReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(GewaesserReportAction.class,
                    "GewaesserReportAction.mnemonic");
        } catch (MissingResourceException e) {
            LOG.error("Couldn't find resources. Using fallback settings.", e);
        }

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

        putValue(SHORT_DESCRIPTION, tooltiptext);
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(NAME, text);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            StaticSwingTools.showDialog(GewaesserReportDialog.getInstance());

            if (!GewaesserReportDialog.getInstance().isCancelled()) {
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
                            final List<String> baCdList = new ArrayList<String>();

                            if (GewaesserReportDialog.getInstance().isSelection()) {
                                for (final FeatureServiceFeature feature
                                            : GewaesserReportDialog.getInstance().getSelectedFeatures()) {
                                    baCdList.add((String)feature.getProperty("ba_cd"));
                                }
                            } else {
                                SessionManager.getSession().getUser();
                                final String owner = AppBroker.getInstance().getOwner();

                                if (!owner.equalsIgnoreCase("Administratoren")) {
                                    final MetaClass FG_BA = ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.fg_ba");
                                    final CidsLayer cl = new CidsLayer(FG_BA);
                                    cl.initAndWait();
                                    final String query = "dlm25wPk_ww_gr1.owner  = '" + owner + "'";
                                    final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                                                .createFeatures(query, null, null, 0, 0, null);

                                    for (final CidsLayerFeature f : featureList) {
                                        baCdList.add((String)f.getProperty("ba_cd"));
                                    }
                                } else {
                                    final CidsServerSearch search = new AllRoutes();

                                    final User user = SessionManager.getSession().getUser();
                                    final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                                .getProxy().customServerSearch(user, search);

                                    if ((attributes != null) && !attributes.isEmpty()) {
                                        for (final ArrayList f : attributes) {
                                            baCdList.add((String)f.get(0));
                                        }
                                    }
                                }
                            }

                            final GewaesserReport gr = new GewaesserReport();
                            int index = 0;
                            final int listSize = baCdList.size();

                            wd.setMax(listSize);

                            for (final String baCd : baCdList) {
                                wd.setProgress(index);
                                wd.setText("Erstelle " + (index++) + " / " + listSize);
                                gr.createReport(baCd);

                                if (Thread.interrupted() || canceled) {
                                    return false;
                                }
//                                gr.createReport("2:0:226");
                            }

                            gr.cleanup();
                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
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

    /**
     * DOCUMENT ME!
     *
     * @param  export  DOCUMENT ME!
     */
    public void setExport(final ExportAction export) {
        this.export = export;
    }
}
