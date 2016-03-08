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
package de.cismet.watergis.gui.actions.gaf;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;
import de.cismet.cismap.cidslayer.CidsLayer;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GafProfReportDialog;
import de.cismet.watergis.gui.panels.GafProf;

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
                    "/de/cismet/watergis/res/icons16/icon-report.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(GafProfReportDialog.getInstance());

        if (!GafProfReportDialog.getInstance().isCancelled()) {
            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.GAF_PROF_MC_NAME);

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

                            int selectedOptions = 0;

                            if (GafProfReportDialog.getInstance().isBasisSelected()) {
                                ++selectedOptions;
                            }
                            if (GafProfReportDialog.getInstance().isLawaSelected()) {
                                ++selectedOptions;
                            }
                            if (GafProfReportDialog.getInstance().isWithoutSelected()) {
                                ++selectedOptions;
                            }
                            final boolean createFolder = selectedOptions > 1;
                            final File basisPath = new File(GafProfReportDialog.getInstance().getPath(), "Basis");
                            final File lawaPath = new File(GafProfReportDialog.getInstance().getPath(), "Lawa");
                            final File withoutPath = new File(GafProfReportDialog.getInstance().getPath(), "ohne");
                            final Map<String, Boolean> fileNames = new HashMap<String, Boolean>();

                            if (createFolder) {
                                if (GafProfReportDialog.getInstance().isBasisSelected()) {
                                    if (!basisPath.exists()) {
                                        basisPath.mkdirs();
                                    }
                                }
                                if (GafProfReportDialog.getInstance().isLawaSelected()) {
                                    if (!lawaPath.exists()) {
                                        lawaPath.mkdirs();
                                    }
                                }
                                if (GafProfReportDialog.getInstance().isWithoutSelected()) {
                                    if (!withoutPath.exists()) {
                                        withoutPath.mkdirs();
                                    }
                                }
                            }

                            // prepare filenames map
                            prepareFileNames(fileNames, features, basisPath, lawaPath);

                            // create reports
                            for (final FeatureServiceFeature feature : features) {
                                try {
                                    if (Thread.interrupted() || canceled) {
                                        // interrupted does sometimes return false, altough the thread was cancelled.
                                        // Perhaps, the jasper report methods reset the interrupted state
                                        return false;
                                    }

                                    if (GafProfReportDialog.getInstance().isBasisSelected()) {
                                        if (feature.getProperty("ba_cd") != null) {
                                            String fileName = GafProf.getBasicReportFileName((CidsLayerFeature)feature);
                                            File basisFile = new File(basisPath, fileName);
                                            fileName = toValidFileName(fileNames, basisFile.getAbsolutePath(), feature);
                                            basisFile = new File(fileName);
                                            basisPath.mkdirs();

                                            createReport((CidsLayerFeature)feature, basisFile);
                                        }
                                    }
                                    if (GafProfReportDialog.getInstance().isLawaSelected()) {
                                        if (feature.getProperty("la_cd") != null) {
                                            String fileName = GafProf.getLawaReportFileName((CidsLayerFeature)feature);
                                            File lawaFile = new File(lawaPath, fileName);
                                            fileName = toValidFileName(fileNames, lawaFile.getAbsolutePath(), feature);
                                            lawaFile = new File(fileName);
                                            lawaPath.mkdirs();

                                            createReport((CidsLayerFeature)feature, lawaFile);
                                        }
                                    }
                                    if (GafProfReportDialog.getInstance().isWithoutSelected()) {
                                        if (feature.getProperty("ba_cd") == null) {
                                            final String nr = String.valueOf(feature.getProperty("qp_nr"));
                                            String fileName = "gaf_ohne___" + nr + ".pdf";
                                            File withoutFile = new File(withoutPath, fileName);
                                            fileName = toValidFileName(
                                                    fileNames,
                                                    withoutFile.getAbsolutePath(),
                                                    feature);
                                            withoutFile = new File(fileName);
                                            withoutPath.mkdirs();

                                            createReport((CidsLayerFeature)feature, withoutFile);
                                        }
                                    }
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

    /**
     * DOCUMENT ME!
     *
     * @param   fileNames  DOCUMENT ME!
     * @param   features   DOCUMENT ME!
     * @param   basisPath  DOCUMENT ME!
     * @param   lawaPath   DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void prepareFileNames(final Map<String, Boolean> fileNames,
            final List<FeatureServiceFeature> features,
            final File basisPath,
            final File lawaPath) throws Exception {
        for (final FeatureServiceFeature feature : features) {
            if (GafProfReportDialog.getInstance().isBasisSelected()) {
                if (feature.getProperty("ba_cd") != null) {
                    final String fileName = GafProf.getBasicReportFileName((CidsLayerFeature)feature);
                    final File basisFile = new File(basisPath, fileName);
                    final Boolean multiStation = fileNames.get(basisFile.getAbsolutePath());

                    if (multiStation == null) {
                        fileNames.put(basisFile.getAbsolutePath(), Boolean.FALSE);
                    } else if (!multiStation) {
                        fileNames.put(basisFile.getAbsolutePath(), Boolean.TRUE);
                    }
                }
            }
            if (GafProfReportDialog.getInstance().isLawaSelected()) {
                if (feature.getProperty("la_cd") != null) {
                    final String fileName = GafProf.getLawaReportFileName((CidsLayerFeature)feature);
                    final File lawaFile = new File(lawaPath, fileName);
                    final Boolean multiStation = fileNames.get(lawaFile.getAbsolutePath());

                    if (multiStation == null) {
                        fileNames.put(lawaFile.getAbsolutePath(), Boolean.FALSE);
                    } else if (!multiStation) {
                        fileNames.put(lawaFile.getAbsolutePath(), Boolean.TRUE);
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileNames  DOCUMENT ME!
     * @param   fileName   DOCUMENT ME!
     * @param   feature    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toValidFileName(final Map<String, Boolean> fileNames,
            final String fileName,
            final FeatureServiceFeature feature) {
        final Boolean multiStation = fileNames.get(fileName);

        if ((multiStation == null) || !multiStation) {
            return fileName;
        } else {
            final String ending = fileName.substring(fileName.lastIndexOf("."));
            return fileName.substring(0, fileName.lastIndexOf(".")) + "___" + feature.getProperty("qp_nr") + ending;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   file     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void createReport(final CidsLayerFeature feature, final File file) throws Exception {
        createReport(feature, null, file);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   file     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void createReport(final Integer qpId, final File file) throws Exception {
        createReport(null, qpId, file);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   file     DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void createReport(final CidsLayerFeature feature, final Integer qpId, final File file) throws Exception {
        if (file.exists()) {
            final int ans = JOptionPane.showConfirmDialog(
                    AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        DeleteAction.class,
                        "ReportAction.createReport().text",
                        file.getAbsolutePath()),
                    NbBundle.getMessage(DeleteAction.class, "ReportAction.createReport().title"),
                    JOptionPane.YES_NO_OPTION);

            if (ans != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        CidsLayerFeature qpFeature = feature;
        
        if (feature == null) {
            //load the feature from the layer
            final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                    "qp");
            CidsLayer layer;

            if ((services == null) || services.isEmpty()) {
                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.qp"));
            } else {
                layer = (CidsLayer)services.get(0);
            }
            
            layer.initAndWait();
            final List<DefaultFeatureServiceFeature> features = layer.getFeatureFactory()
            .createFeatures("qp_nr = " + qpId.toString(),
                null,
                null,
                0,
                0,
                null);
            
            if (features != null && !features.isEmpty() && features.get(0) instanceof CidsLayerFeature) {
                qpFeature = (CidsLayerFeature)features.get(0);
            }
        }

        final JasperPrint print = GafProf.fillreport(qpFeature);
        print.setOrientation(print.getOrientationValue());
        final FileOutputStream fout = new FileOutputStream(file);
        final BufferedOutputStream out = new BufferedOutputStream(fout);
        JasperExportManager.exportReportToPdfStream(print, out);
        out.close();
    }
    

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
