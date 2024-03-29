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

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.ProxyHandler;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;
import de.cismet.tools.gui.downloadmanager.WebDavDownload;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GafExportDialog;
import de.cismet.watergis.gui.panels.GafProf;
import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.profile.GafReader;
import de.cismet.watergis.profile.QpUplDownload;

import de.cismet.watergis.utils.FeatureServiceHelper;
import de.cismet.watergis.utils.JumpShapeWriter;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportAction.class);
    private static CidsLayer ppLayer = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public ExportAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(ExportAction.class,
                "ExportAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(ExportAction.class,
                "ExportAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(ExportAction.class,
                "ExportAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-exportfile.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        try {
            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.QP_UPL_MC_NAME);

            if ((features == null) || features.isEmpty()) {
                final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                        AppBroker.QP_UPL_MC_NAME);

                if ((services != null) && !services.isEmpty()) {
                    FeatureServiceHelper.getFeatures(services.get(0), false);
                }
            } else {
                LOG.warn("No qp_upl objects found to create qp_upl export");
            }

            if (!features.isEmpty()) {
                if (DownloadManagerDialog.showAskingForUserTitle(AppBroker.getInstance().getRootWindow())) {
                    final String jobname = DownloadManagerDialog.getInstance().getJobName();

                    final WebDavClient webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(),
                            Photo.WEB_DAV_USER,
                            Photo.WEB_DAV_PASSWORD,
                            true);
                    final File f = new File(DownloadManager.instance().getDestinationDirectory(), jobname);

                    DownloadManager.instance()
                            .add(new QpUplDownload(webDavClient, f.getAbsolutePath(), "Download Profil", features));
                }
            }
        } catch (Exception ex) {
            LOG.error("Error while creating qp_upl export", ex);
        }
    }

//    @Override
//    public void actionPerformed(final ActionEvent e) {
//        if (ppLayer == null) {
//            ppLayer = new CidsLayer(ClassCacheMultiple.getMetaClass(
//                        AppBroker.DOMAIN_NAME,
//                        "dlm25w.qp_gaf_pp"));
//        }
//        StaticSwingTools.showDialog(GafExportDialog.getInstance());
//
//        if (!GafExportDialog.getInstance().isCancelled()) {
//            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
//                    AppBroker.GAF_PROF_MC_NAME);
//
//            if (!features.isEmpty()) {
//                final File zipFile = new File(GafExportDialog.getInstance().getZipFile());
//                final File tmpBaseDir = createTmpDirectory(zipFile.getParentFile());
//
//                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
//                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
//                        true,
//                        NbBundle.getMessage(ReportAction.class, "ExportAction.actionPerformed.waitingDialog"),
//                        null,
//                        100,
//                        true) {
//
//                        @Override
//                        protected Boolean doInBackground() throws Exception {
//                            wd.setMax(features.size());
//                            int i = 0;
//                            wd.setText(NbBundle.getMessage(
//                                    ReportAction.class,
//                                    "ExportAction.actionPerformed.progress",
//                                    i,
//                                    features.size()));
//
//                            final File tmpShapeDir = new File(tmpBaseDir, "Shapes");
//                            final File tmpReportBasisDir = new File(tmpBaseDir, "Steckbriefe_Basis");
//                            final File tmpReportLawaDir = new File(tmpBaseDir, "Steckbriefe_LAWA");
//                            final File tmpReportWithoutDir = new File(tmpBaseDir, "Steckbriefe_ohne");
//                            final File tmpBasisDir = new File(tmpBaseDir, "GAF_Basis");
//                            final File tmpLawaDir = new File(tmpBaseDir, "GAF_LAWA");
//                            final File tmpWithoutDir = new File(tmpBaseDir, "GAF_ohne");
//                            final Map<String, Boolean> fileNames = new HashMap<String, Boolean>();
//
//                            if (GafExportDialog.getInstance().isShapeSelected()) {
//                                tmpShapeDir.mkdirs();
//                            }
//
//                            // prepare filenames map
//                            prepareFileNames(fileNames, features, tmpReportBasisDir, tmpReportLawaDir);
//
//                            final Map<String, Map<Double, List<DefaultFeatureServiceFeature>>> gafMap =
//                                new HashMap<String, Map<Double, List<DefaultFeatureServiceFeature>>>();
//
//                            for (final FeatureServiceFeature feature : features) {
//                                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
//
//                                if (Thread.interrupted()) {
//                                    return false;
//                                }
//
//                                // prepare gaf file export
//                                if (GafExportDialog.getInstance().isGafSelected()) {
//                                    final Integer qpNr = (Integer)feature.getProperty("qp_nr");
//                                    List<DefaultFeatureServiceFeature> ppFeatures = null;
//
//                                    // collect gaf profiles
//                                    if (GafExportDialog.getInstance().isBasisSelected()) {
//                                        if (feature.getProperty("ba_cd") != null) {
//                                            final String fileName = GafProf.getBasicGafFileName(cidsFeature);
//                                            final File basisFile = new File(tmpBasisDir, fileName);
//                                            if (ppFeatures == null) {
//                                                ppFeatures = getAllPPFeature(qpNr);
//                                            }
//                                            Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
//                                                    basisFile.getAbsolutePath());
//                                            final Double station = GafProf.getFeatureStation(cidsFeature);
//
//                                            if (profilesMap == null) {
//                                                profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
//                                                gafMap.put(basisFile.getAbsolutePath(), profilesMap);
//                                            }
//
//                                            profilesMap.put(station, ppFeatures);
//                                        }
//                                    }
//
//                                    if (Thread.interrupted()) {
//                                        return false;
//                                    }
//
//                                    if (GafExportDialog.getInstance().isLawaSelected()) {
//                                        if (feature.getProperty("la_cd") != null) {
//                                            final String fileName = GafProf.getLawaGafFileName(cidsFeature);
//                                            final File lawaFile = new File(tmpLawaDir, fileName);
//                                            if (ppFeatures == null) {
//                                                ppFeatures = getAllPPFeature(qpNr);
//                                            }
//                                            Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
//                                                    lawaFile.getAbsolutePath());
//                                            final Double station = GafProf.getFeatureStation(cidsFeature);
//
//                                            if (profilesMap == null) {
//                                                profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
//                                                gafMap.put(lawaFile.getAbsolutePath(), profilesMap);
//                                            }
//
//                                            profilesMap.put(station, ppFeatures);
//                                        }
//                                    }
//
//                                    if (Thread.interrupted()) {
//                                        return false;
//                                    }
//
//                                    if (GafExportDialog.getInstance().isWithoutSelected()) {
//                                        if (feature.getProperty("ba_cd") == null) {
//                                            final String fileName = "gaf_ohne.gaf";
//                                            final File withoutFile = new File(tmpWithoutDir, fileName);
//                                            if (ppFeatures == null) {
//                                                ppFeatures = getAllPPFeature(qpNr);
//                                            }
//                                            Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
//                                                    withoutFile.getAbsolutePath());
//                                            final Double station = GafProf.getFeatureStation(cidsFeature);
//
//                                            if (profilesMap == null) {
//                                                profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
//                                                gafMap.put(withoutFile.getAbsolutePath(), profilesMap);
//                                            }
//
//                                            profilesMap.put(station, ppFeatures);
//                                        }
//                                    }
//                                }
//
//                                if (Thread.interrupted()) {
//                                    return false;
//                                }
//
//                                // create reports
//                                if (GafExportDialog.getInstance().isReportSelected()) {
//                                    if (GafExportDialog.getInstance().isBasisSelected()) {
//                                        if (feature.getProperty("ba_cd") != null) {
//                                            String fileName = GafProf.getBasicReportFileName(cidsFeature);
//                                            File basisFile = new File(tmpReportBasisDir, fileName);
//                                            fileName = ReportAction.toValidFileName(
//                                                    fileNames,
//                                                    basisFile.getAbsolutePath(),
//                                                    feature);
//                                            basisFile = new File(fileName);
//                                            tmpReportBasisDir.mkdirs();
//
//                                            ReportAction.createReport(cidsFeature, basisFile);
//                                        }
//                                    }
//                                    if (GafExportDialog.getInstance().isLawaSelected()) {
//                                        if (feature.getProperty("la_cd") != null) {
//                                            String fileName = GafProf.getLawaReportFileName(cidsFeature);
//                                            File lawaFile = new File(tmpReportLawaDir, fileName);
//                                            fileName = ReportAction.toValidFileName(
//                                                    fileNames,
//                                                    lawaFile.getAbsolutePath(),
//                                                    feature);
//                                            lawaFile = new File(fileName);
//                                            tmpReportLawaDir.mkdirs();
//
//                                            ReportAction.createReport(cidsFeature, lawaFile);
//                                        }
//                                    }
//                                    if (GafExportDialog.getInstance().isWithoutSelected()) {
//                                        if (feature.getProperty("ba_cd") == null) {
//                                            final String nr = String.valueOf(feature.getProperty("qp_nr"));
//                                            String fileName = "gaf_ohne___" + nr + ".pdf";
//                                            File withoutFile = new File(tmpReportWithoutDir, fileName);
//                                            fileName = ReportAction.toValidFileName(
//                                                    fileNames,
//                                                    withoutFile.getAbsolutePath(),
//                                                    feature);
//                                            withoutFile = new File(fileName);
//                                            tmpReportWithoutDir.mkdirs();
//
//                                            ReportAction.createReport(cidsFeature, withoutFile);
//                                        }
//                                    }
//                                }
//                                wd.setText(NbBundle.getMessage(
//                                        ReportAction.class,
//                                        "ExportAction.actionPerformed.progress",
//                                        ++i,
//                                        features.size()));
//                                wd.setProgress(wd.getProgress() + 1);
//                            }
//
//                            if (GafExportDialog.getInstance().isGafSelected()) {
//                                // create gaf files
//                                for (final String gafFileName : gafMap.keySet()) {
//                                    final Map<Double, List<DefaultFeatureServiceFeature>> gafProfiles = gafMap.get(
//                                            gafFileName);
//
//                                    final GafReader reader = new GafReader(gafProfiles);
//                                    // write gaf file
//                                    File destFile = new File(gafFileName);
//                                    destFile.getParentFile().mkdirs();
//                                    BufferedWriter br = new BufferedWriter(new FileWriter(destFile));
//                                    br.write(reader.createGafFile());
//                                    br.close();
//
//                                    // write bk catalogue files, if required
//                                    final String bkCatalogueContent = reader.createCustomBkCatalogueFile();
//
//                                    if (bkCatalogueContent != null) {
//                                        destFile = new File(gafFileName.substring(0, gafFileName.length() - 4)
//                                                        + "_bk.csv");
//                                        destFile.getParentFile().mkdirs();
//                                        br = new BufferedWriter(new FileWriter(destFile));
//                                        br.write(bkCatalogueContent);
//                                        br.close();
//                                    }
//
//                                    // write rk catalogue file, if required
//                                    final String rkCatalogueContent = reader.createCustomRkCatalogueFile();
//                                    if (rkCatalogueContent != null) {
//                                        destFile = new File(gafFileName.substring(0, gafFileName.length() - 4)
//                                                        + "_rk.csv");
//                                        if (destFile.exists()) {
//                                            System.out.println("gibt es schon");
//                                        }
//                                        destFile.getParentFile().mkdirs();
//                                        br = new BufferedWriter(new FileWriter(destFile));
//                                        br.write(rkCatalogueContent);
//                                        br.close();
//                                    }
//                                }
//                            }
//
//                            if (Thread.interrupted()) {
//                                return false;
//                            }
//
//                            // create shapes
//                            if (GafExportDialog.getInstance().isShapeSelected()) {
//                                wd.setText(NbBundle.getMessage(
//                                        ReportAction.class,
//                                        "ExportAction.actionPerformed.createShape",
//                                        ++i,
//                                        features.size()));
//                                // create qp prj and meta document
//                                File shapeFile = new File(tmpShapeDir, "qp");
//                                createShapeAndMetaDoc(features, shapeFile.getAbsolutePath(), true);
//
//                                // create k_qp_gaf_kz prj and meta document
//                                CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
//                                            AppBroker.DOMAIN_NAME,
//                                            "dlm25w.k_qp_gaf_kz"));
//                                layer.initAndWait();
//                                List<FeatureServiceFeature> serviceFeatures = layer.getFeatureFactory()
//                                            .createFeatures(layer.getQuery(),
//                                                null,
//                                                null,
//                                                0,
//                                                0,
//                                                null);
//
//                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_kz");
//                                createDBF(serviceFeatures, shapeFile.getAbsolutePath());
//
//                                // create k_qp_gaf_rk prj and meta document
//                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
//                                            AppBroker.DOMAIN_NAME,
//                                            "dlm25w.k_qp_gaf_rk"));
//                                layer.initAndWait();
//                                serviceFeatures = layer.getFeatureFactory()
//                                            .createFeatures(layer.getQuery(),
//                                                    null,
//                                                    null,
//                                                    0,
//                                                    0,
//                                                    null);
//
//                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_rk");
//                                createDBF(serviceFeatures, shapeFile.getAbsolutePath());
//
//                                // create k_qp_gaf_bk prj and meta document
//                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
//                                            AppBroker.DOMAIN_NAME,
//                                            "dlm25w.k_qp_gaf_bk"));
//                                layer.initAndWait();
//                                serviceFeatures = layer.getFeatureFactory()
//                                            .createFeatures(layer.getQuery(),
//                                                    null,
//                                                    null,
//                                                    0,
//                                                    0,
//                                                    null);
//
//                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_bk");
//                                createDBF(serviceFeatures, shapeFile.getAbsolutePath());
//                            }
//
//                            if (Thread.interrupted()) {
//                                return false;
//                            }
//                            wd.setText(NbBundle.getMessage(
//                                    ReportAction.class,
//                                    "ExportAction.actionPerformed.createZip",
//                                    ++i,
//                                    features.size()));
//                            final ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
//                            zipDirectory(tmpBaseDir, zipStream, "");
//                            zipStream.close();
//
//                            return true;
//                        }
//
//                        @Override
//                        protected void done() {
//                            try {
//                                get();
//                            } catch (Exception e) {
//                                LOG.error("Error while exporting gaf profiles.", e);
//                            } finally {
//                                if (tmpBaseDir.exists()) {
//                                    deleteDirectory(tmpBaseDir);
//                                }
//                            }
//                        }
//                    };
//
//                wdt.start();
//            }
//        }
//    }

    /**
     * DOCUMENT ME!
     *
     * @param   qpNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<DefaultFeatureServiceFeature> getAllPPFeature(final Integer qpNr) throws Exception {
        ppLayer.initAndWait();
        final FeatureServiceAttribute attr = ppLayer.getFeatureServiceAttributes().get("id");
        return ppLayer.getFeatureFactory()
                    .createFeatures("qp_nr = " + qpNr.toString(),
                        null,
                        null,
                        0,
                        0,
                        new FeatureServiceAttribute[] { attr });
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features        DOCUMENT ME!
     * @param   outputFileStem  DOCUMENT ME!
     * @param   createPrj       DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createShapeAndMetaDoc(final List<FeatureServiceFeature> features,
            final String outputFileStem,
            final boolean createPrj) throws Exception {
        final List<String[]> attribList = new ArrayList<String[]>();
        final CidsLayer service = (CidsLayer)features.get(0).getLayerProperties().getFeatureService();

        for (final String attr
                    : (List<String>)features.get(0).getLayerProperties().getFeatureService()
                    .getOrderedFeatureServiceAttributes()) {
            final String[] attrName = new String[2];
            attrName[0] = attr;
            attrName[1] = attr;
            attribList.add(attrName);
        }

        final JumpShapeWriter shapeWriter = new JumpShapeWriter();
        final String charset = Charset.defaultCharset().name();

        shapeWriter.writeShpFile(features.toArray(new FeatureServiceFeature[features.size()]),
            new File(outputFileStem + ".shp"),
            null,
            charset);

        if (createPrj) {
            // create prj
            final BufferedWriter bw = new BufferedWriter(new FileWriter(
                        outputFileStem
                                + ".prj"));
            bw.write(AppBroker.PRJ_CONTENT);
            bw.close();
        }

        final BufferedWriter bwCpg = new BufferedWriter(new FileWriter(
                    outputFileStem
                            + ".cpg"));
        bwCpg.write(charset);
        bwCpg.close();
        // create meta document
        downloadMetaDocument(service, outputFileStem);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   features        DOCUMENT ME!
     * @param   outputFileStem  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createDBF(final List<FeatureServiceFeature> features, final String outputFileStem) throws Exception {
        createShapeAndMetaDoc(features, outputFileStem, false);
        File file = new File(outputFileStem + ".shp");

        if (file.exists()) {
            file.delete();
        }
        file = new File(outputFileStem + ".shx");

        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service         DOCUMENT ME!
     * @param  outputFileStem  DOCUMENT ME!
     */
    public static void downloadMetaDocument(final CidsLayer service, final String outputFileStem) {
        if (service instanceof CidsLayer) {
            final CidsLayer cl = (CidsLayer)service;
            final String link = cl.getMetaDocumentLink();

            if (link != null) {
                // write pdf
                BufferedInputStream bin = null;
                BufferedOutputStream out = null;

                try {
                    final URL u = new URL(link);
                    final InputStream in = WebAccessManager.getInstance().doRequest(u);
                    bin = new BufferedInputStream(in);
                    out = new BufferedOutputStream(new FileOutputStream(outputFileStem + ".pdf"));
                    final byte[] tmp = new byte[256];
                    int byteCount;

                    while ((byteCount = bin.read(tmp, 0, tmp.length)) != -1) {
                        out.write(tmp, 0, byteCount);
                    }
                } catch (Exception e) {
                    LOG.error("Error while downloading meta document.", e);
                } finally {
                    try {
                        if (bin != null) {
                            bin.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        LOG.error("Error while closing stream", e);
                    }
                }
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
            if (GafExportDialog.getInstance().isBasisSelected()) {
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
            if (GafExportDialog.getInstance().isLawaSelected()) {
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
     * Deletes the given directory and its content.
     *
     * @param  dir  the directory to delete
     */
    private static void deleteDirectory(final File dir) {
        for (final File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }

        dir.delete();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   directorybase  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static File createTmpDirectory(final File directorybase) {
        File tmpSubDirectory = null;
        String subDirName = null;
        final Random rand = new Random(new Date().getTime());

        do {
            subDirName = "tmp" + rand.nextInt(Integer.MAX_VALUE);
            tmpSubDirectory = new File(directorybase, subDirName);
        } while (tmpSubDirectory.exists());

        tmpSubDirectory.mkdirs();

        return tmpSubDirectory;
    }

    /**
     * Creates a zip file that contains the content of the given directory.
     *
     * @param   inputDir  the directory that should be zipped
     * @param   out       file the zip file that should be created
     * @param   dirName   the current sub directory within the zip file
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void zipDirectory(final File inputDir, final ZipOutputStream out, final String dirName)
            throws Exception {
        final int BYTES_ARRAY_LENGTH = 256;
        final byte[] tmp = new byte[BYTES_ARRAY_LENGTH];
        int byteCount;

        for (final File f : inputDir.listFiles()) {
            if (f.isDirectory()) {
                final String parent = (dirName.equals("") ? "" : (dirName + "/"));
                zipDirectory(f, out, parent + f.getName() + "/");
            } else {
                final ZipEntry entry = new ZipEntry(dirName + f.getName());
                out.putNextEntry(entry);
                final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

                try {
                    while ((byteCount = bis.read(tmp, 0, BYTES_ARRAY_LENGTH)) != -1) {
                        out.write(tmp, 0, byteCount);
                    }
                } finally {
                    out.closeEntry();
                    bis.close();
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
