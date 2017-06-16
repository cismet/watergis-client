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
import Sirius.server.middleware.types.MetaObject;

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

import de.cismet.cids.custom.reports.WkFgReport;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.*;
import de.cismet.watergis.gui.dialog.WkFgReportDialog;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class WkFgReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WkFgReportAction.class);
    private static final MetaClass FG_BA = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");

    //~ Instance fields --------------------------------------------------------

    private ExportAction export;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GewaesserReportAction object.
     */
    public WkFgReportAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-contact-businesscard.png");
        String text = "FG";
        String tooltiptext = "Wasserkörperauswerung";
        String mnemonic = "F";

        try {
            text = NbBundle.getMessage(WkFgReportAction.class,
                    "WkFgReportAction.text");
            tooltiptext = NbBundle.getMessage(WkFgReportAction.class,
                    "WkFgReportAction.toolTipText");
            mnemonic = NbBundle.getMessage(WkFgReportAction.class,
                    "WkFgReportAction.mnemonic");
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
            StaticSwingTools.showDialog(WkFgReportDialog.getInstance());

            if (!WkFgReportDialog.getInstance().isCancelled()) {
                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        "erstelle Auswertung",
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            final List<String> wkNrList = new ArrayList<String>();

                            if (WkFgReportDialog.getInstance().isSelection()) {
                                for (final FeatureServiceFeature feature
                                            : WkFgReportDialog.getInstance().getSelectedFeatures()) {
                                    wkNrList.add((String)feature.getProperty("wk_nr"));
                                }
                            } else {
                                final MetaClass fgLaWkMc = ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "fg_la_wk");
                                final CidsLayer cl = new CidsLayer(fgLaWkMc);
                                cl.initAndWait();
                                final List<FeatureServiceFeature> features = cl.getFeatureFactory()
                                            .createFeatures(cl.getQuery(), null, null, 0, 0, null);

                                if ((features != null) && !features.isEmpty()) {
                                    for (final FeatureServiceFeature f : features) {
                                        final String wkNr = (String)f.getProperty("wk_nr");

                                        if (wkNr != null) {
                                            wkNrList.add(wkNr);
                                        }
                                    }
                                }
                            }

                            int index = 0;
                            final int listSize = wkNrList.size();

                            wd.setMax(listSize);

                            for (final String wkk : wkNrList) {
                                wd.setProgress(index);
                                wd.setText("Erstelle " + (index++) + " / " + listSize);

                                // create report
                                final MetaClass wkFgMc = ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME_WRRL,
                                        "wk_fg");

                                if (wkFgMc == null) {
                                    LOG.error("Error while creating report. Cannot retrieve wk_fg meta class");
//                                    error(new Exception(NbBundle.getMessage(WkFgDownload.class, "WkFgDownload.run.noMc")));
                                    return false;
                                }
                                final String query = "select " + wkFgMc.getID() + ", " + wkFgMc.getTableName() + "."
                                            + wkFgMc.getPrimaryKey() + " from "
                                            + wkFgMc.getTableName() + " WHERE wk_k = '" + wkk + "'"; // NOI18N
                                try {
                                    final MetaObject[] mos = SessionManager.getProxy()
                                                .getMetaObjectByQuery(SessionManager.getSession().getUser(),
                                                    query,
                                                    AppBroker.DOMAIN_NAME_WRRL);

                                    if ((mos != null) && (mos.length > 0)) {
                                        final String path = WkFgReportDialog.getInstance().getPath();
                                        final File fileToSaveTo = new File(path, wkk + ".pdf");

                                        if (fileToSaveTo.exists()) {
                                            final int ans = JOptionPane.showConfirmDialog(
                                                    AppBroker.getInstance().getWatergisApp(),
                                                    NbBundle.getMessage(
                                                        WkFgReportAction.class,
                                                        "WkFgReportAction.actionPerformed().fileExists.text",
                                                        fileToSaveTo.getAbsolutePath()),
                                                    NbBundle.getMessage(
                                                        WkFgReportAction.class,
                                                        "WkFgReportAction.actionPerformed().fileExists.title"),
                                                    JOptionPane.YES_NO_OPTION);

                                            if (ans != JOptionPane.YES_OPTION) {
                                                continue;
                                            }
                                        }

                                        WkFgReport.createReport(fileToSaveTo.getAbsolutePath(), mos[0].getBean());
                                    } else {
                                        LOG.error("Cannot find wk_fg object with id " + wkk);
//                                        error(new Exception(NbBundle.getMessage(WkFgDownload.class, "WkFgDownload.run.objectNotFound")));
                                    }
                                } catch (Exception ex) {
                                    LOG.error("Error while creating report", ex);
//                                    error(ex);
                                }

                                if (Thread.interrupted() || canceled) {
                                    return false;
                                }
                            }

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
