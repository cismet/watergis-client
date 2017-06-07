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
package de.cismet.watergis.gui.actions.foto;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.PhotoReportDialog;
import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ReportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ReportAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public ReportAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(ReportAction.class,
                "ReportAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(ReportAction.class,
                "ReportAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(ReportAction.class,
                "ReportAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-contact-businesscard.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(PhotoReportDialog.getInstance());

        if (!PhotoReportDialog.getInstance().isCancelled()) {
            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.FOTO_MC_NAME);

            if (!features.isEmpty()) {
                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        // NbBundle.getMessage(SonstigeCheckAction.class,
                        // "SonstigeCheckAction.actionPerformed().dialog"),
                        NbBundle.getMessage(ReportAction.class, "ReportAction.actionPerformed.waitingDialog"),
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            wd.setMax(features.size());
                            int i = 0;
                            wd.setText(NbBundle.getMessage(
                                    ReportAction.class,
                                    "ReportAction.actionPerformed.progress",
                                    i,
                                    features.size()));

                            for (final FeatureServiceFeature feature : features) {
                                try {
                                    if (Thread.interrupted()) {
                                        return false;
                                    }
                                    final JasperPrint print = Photo.fillreport((CidsLayerFeature)feature);
                                    print.setOrientation(print.getOrientationValue());

                                    String photoFile = (String)feature.getProperty("foto");

                                    if (photoFile.contains(".")) {
                                        photoFile = photoFile.substring(0, photoFile.lastIndexOf("."));
                                    }

                                    photoFile += ".pdf";
                                    final File file = new File(PhotoReportDialog.getInstance().getPath()
                                                    + File.separator + photoFile);

                                    if (Thread.interrupted()) {
                                        return false;
                                    }
                                    if (file.exists()) {
                                        // todo: frage, ob die Datei überschriben werden soll
                                    }

                                    final FileOutputStream fout = new FileOutputStream(file);
                                    final BufferedOutputStream out = new BufferedOutputStream(fout);
                                    JasperExportManager.exportReportToPdfStream(print, out);
                                    out.close();
                                } catch (Exception ex) {
                                    LOG.error("Error while creating photo report", ex);
                                } finally {
                                    wd.setText(NbBundle.getMessage(
                                            ReportAction.class,
                                            "ReportAction.actionPerformed.progress",
                                            ++i,
                                            features.size()));
                                    wd.setProgress(wd.getProgress() + 1);
                                }
                            }
                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (Exception e) {
                                LOG.error("Error while create photo reports.", e);
                            }
                        }
                    };

                wdt.start();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
