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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.reports.WkFgReport;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.*;
import de.cismet.watergis.gui.dialog.WkFgReportDialog;

import static javax.swing.Action.NAME;

import static de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet.addExtension;

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
                        "lade Steckbriefe",
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            final TreeSet<String> wkNrList = new TreeSet<String>();

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
                                wd.setText("Lade " + (index++) + " / " + listSize);

                                try {
                                    // create report
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

                                    downloadDocumentFromWebDav(
                                        WatergisDefaultRuleSet.WK_FG_WEBDAV_PATH,
                                        WatergisDefaultRuleSet.addExtension(wkk.toUpperCase(), "pdf"),
                                        fileToSaveTo);
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
     * @param   path        DOCUMENT ME!
     * @param   file        DOCUMENT ME!
     * @param   fileToSave  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void downloadDocumentFromWebDav(String path, String file, final File fileToSave) throws Exception {
        // remove slashs from the file
        while (file.startsWith("/")) {
            file = file.substring(1);
        }

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        final WebDavClient webDavClient = WatergisDefaultRuleSet.createWebDavClient();
        final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(fileToSave));
        final InputStream is = webDavClient.getInputStream(path + WebDavHelper.encodeURL(file));
        final byte[] buffer = new byte[256];
        int size;

        while ((size = is.read(buffer)) != -1) {
            os.write(buffer, 0, size);
        }

        is.close();
        os.close();
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
