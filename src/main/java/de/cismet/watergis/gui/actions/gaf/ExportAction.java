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

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.log4j.Logger;

import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.FeatureCollection;

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
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;

import de.cismet.security.WebAccessManager;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GafExportDialog;
import de.cismet.watergis.gui.panels.GafProf;

import de.cismet.watergis.utils.FeatureServiceHelper;
import de.cismet.watergis.utils.GafReader;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportAction.class);
    private static final String PRJ_CONTENT =
        "PROJCS[\"ETRS_1989_UTM_Zone_33N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",33500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",15.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
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
                    "/de/cismet/watergis/res/icons16/icon-export.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (ppLayer == null) {
            ppLayer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                        AppBroker.DOMAIN_NAME,
                        "dlm25w.qp_gaf_pp"));
        }
        StaticSwingTools.showDialog(GafExportDialog.getInstance());

        if (!GafExportDialog.getInstance().isCancelled()) {
            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.GAF_PROF_MC_NAME);

            if (!features.isEmpty()) {
                final File zipFile = new File(GafExportDialog.getInstance().getZipFile());
                final File tmpBaseDir = createTmpDirectory(zipFile.getParentFile());

                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        NbBundle.getMessage(ReportAction.class, "ExportAction.actionPerformed.waitingDialog"),
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            wd.setMax(features.size());
                            int i = 0;
                            wd.setText(NbBundle.getMessage(
                                    ReportAction.class,
                                    "ExportAction.actionPerformed.progress",
                                    i,
                                    features.size()));

                            final File tmpShapeDir = new File(tmpBaseDir, "Shapes");
                            final File tmpReportBasisDir = new File(tmpBaseDir, "Steckbriefe");
                            final File tmpReportLawaDir = new File(tmpBaseDir, "Steckbriefe");
                            final File tmpReportWithoutDir = new File(tmpBaseDir, "Steckbriefe");
                            final File tmpBasisDir = new File(tmpBaseDir, "GAF_Basis");
                            final File tmpLawaDir = new File(tmpBaseDir, "GAF_LAWA");
                            final File tmpWithoutDir = new File(tmpBaseDir, "GAF_ohne");
                            final Map<String, Boolean> fileNames = new HashMap<String, Boolean>();

                            if (GafExportDialog.getInstance().isShapeSelected()) {
                                tmpShapeDir.mkdirs();
                            }

                            // prepare filenames map
                            prepareFileNames(fileNames, features, tmpReportBasisDir, tmpReportLawaDir);

                            final Map<String, Map<Double, List<DefaultFeatureServiceFeature>>> gafMap =
                                new HashMap<String, Map<Double, List<DefaultFeatureServiceFeature>>>();

                            for (final FeatureServiceFeature feature : features) {
                                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;

                                if (Thread.interrupted()) {
                                    return false;
                                }

                                // collect gaf profiles
                                if (GafExportDialog.getInstance().isBasisSelected()) {
                                    if (feature.getProperty("ba_cd") != null) {
                                        final String fileName = GafProf.getBasicGafFileName(cidsFeature);
                                        final File basisFile = new File(tmpBasisDir, fileName);
                                        final Integer qpNr = (Integer)feature.getProperty("qp_nr");
                                        final List<DefaultFeatureServiceFeature> features = getAllPPFeature(qpNr);
                                        Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
                                                basisFile.getAbsolutePath());
                                        final Double station = GafProf.getFeatureStation(cidsFeature);

                                        if (profilesMap == null) {
                                            profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
                                            gafMap.put(basisFile.getAbsolutePath(), profilesMap);
                                        }

                                        profilesMap.put(station, features);
                                    }
                                }

                                if (GafExportDialog.getInstance().isLawaSelected()) {
                                    if (feature.getProperty("la_cd") != null) {
                                        final String fileName = GafProf.getLawaGafFileName(cidsFeature);
                                        final File lawaFile = new File(tmpLawaDir, fileName);
                                        final Integer qpNr = (Integer)feature.getProperty("qp_nr");
                                        final List<DefaultFeatureServiceFeature> features = getAllPPFeature(qpNr);
                                        Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
                                                lawaFile.getAbsolutePath());
                                        final Double station = GafProf.getFeatureStation(cidsFeature);

                                        if (profilesMap == null) {
                                            profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
                                            gafMap.put(lawaFile.getAbsolutePath(), profilesMap);
                                        }

                                        profilesMap.put(station, features);
                                    }
                                }

                                if (GafExportDialog.getInstance().isWithoutSelected()) {
                                    if (feature.getProperty("ba_cd") == null) {
                                        final String fileName = "gaf_ohne.gaf";
                                        final File withoutFile = new File(tmpWithoutDir, fileName);
                                        final Integer qpNr = (Integer)feature.getProperty("qp_nr");
                                        final List<DefaultFeatureServiceFeature> features = getAllPPFeature(qpNr);
                                        Map<Double, List<DefaultFeatureServiceFeature>> profilesMap = gafMap.get(
                                                withoutFile.getAbsolutePath());
                                        final Double station = GafProf.getFeatureStation(cidsFeature);

                                        if (profilesMap == null) {
                                            profilesMap = new HashMap<Double, List<DefaultFeatureServiceFeature>>();
                                            gafMap.put(withoutFile.getAbsolutePath(), profilesMap);
                                        }

                                        profilesMap.put(station, features);
                                    }
                                }

                                if (Thread.interrupted()) {
                                    return false;
                                }

                                // create reports
                                if (GafExportDialog.getInstance().isReportSelected()) {
                                    if (GafExportDialog.getInstance().isBasisSelected()) {
                                        if (feature.getProperty("ba_cd") != null) {
                                            String fileName = GafProf.getBasicReportFileName(cidsFeature);
                                            fileName = ReportAction.toValidFileName(fileNames, fileName, feature);
                                            final File basisFile = new File(tmpReportBasisDir, fileName);
                                            tmpReportBasisDir.mkdirs();

                                            ReportAction.createReport(cidsFeature, basisFile);
                                        }
                                    }
                                    if (GafExportDialog.getInstance().isLawaSelected()) {
                                        if (feature.getProperty("la_cd") != null) {
                                            String fileName = GafProf.getLawaReportFileName(cidsFeature);
                                            fileName = ReportAction.toValidFileName(fileNames, fileName, feature);
                                            final File lawaFile = new File(tmpReportLawaDir, fileName);
                                            tmpReportLawaDir.mkdirs();

                                            ReportAction.createReport(cidsFeature, lawaFile);
                                        }
                                    }
                                    if (GafExportDialog.getInstance().isWithoutSelected()) {
                                        if (feature.getProperty("ba_cd") == null) {
                                            final String nr = String.valueOf(feature.getProperty("qp_nr"));
                                            String fileName = "gaf_ohne___" + nr + ".pdf";
                                            fileName = ReportAction.toValidFileName(fileNames, fileName, feature);
                                            final File withoutFile = new File(tmpReportWithoutDir, fileName);
                                            tmpReportWithoutDir.mkdirs();

                                            ReportAction.createReport(cidsFeature, withoutFile);
                                        }
                                    }
                                }
                                wd.setText(NbBundle.getMessage(
                                        ReportAction.class,
                                        "ExportAction.actionPerformed.progress",
                                        ++i,
                                        features.size()));
                                wd.setProgress(wd.getProgress() + 1);
                            }

                            // create gaf profiles

                            for (final String gafFileName : gafMap.keySet()) {
                                final Map<Double, List<DefaultFeatureServiceFeature>> gafProfiles = gafMap.get(
                                        gafFileName);

                                final GafReader reader = new GafReader(gafProfiles);

                                final File destFile = new File(gafFileName);
                                destFile.getParentFile().mkdirs();
                                final BufferedWriter br = new BufferedWriter(new FileWriter(destFile));
                                br.write(reader.createGafFile());
                                br.close();
                            }

                            if (Thread.interrupted()) {
                                return false;
                            }

                            // create shapes
                            if (GafExportDialog.getInstance().isShapeSelected()) {
                                wd.setText(NbBundle.getMessage(
                                        ReportAction.class,
                                        "ExportAction.actionPerformed.createShape",
                                        ++i,
                                        features.size()));
                                // create qp prj and meta document
                                File shapeFile = new File(tmpShapeDir, "qp");
                                createShapeAndMetaDoc(features, shapeFile.getAbsolutePath());

                                // create qp_gaf_p prj and meta document
                                final TreeSet<Integer> qpNrSet = new TreeSet<Integer>();

                                for (final FeatureServiceFeature feature : features) {
                                    qpNrSet.add((Integer)feature.getProperty("qp_nr"));
                                }

                                CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.qp_gaf_p"));
                                layer.initAndWait();
                                List<FeatureServiceFeature> serviceFeatures = new ArrayList<FeatureServiceFeature>();

                                for (final Object qpNr : qpNrSet) {
                                    serviceFeatures.addAll(layer.getFeatureFactory().createFeatures(
                                            "qp_nr = "
                                                    + qpNr.toString(),
                                            null,
                                            null,
                                            0,
                                            0,
                                            null));
                                }
                                shapeFile = new File(tmpShapeDir, "qp_gaf_p");
                                createShapeAndMetaDoc(serviceFeatures, shapeFile.getAbsolutePath());

                                // create qp_gaf_l prj and meta document
                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.qp_gaf_l"));
                                layer.initAndWait();
                                serviceFeatures = new ArrayList<FeatureServiceFeature>();

                                for (final Object qpNr : qpNrSet) {
                                    serviceFeatures.addAll(layer.getFeatureFactory().createFeatures(
                                            "qp_nr = "
                                                    + qpNr.toString(),
                                            null,
                                            null,
                                            0,
                                            0,
                                            null));
                                }

                                shapeFile = new File(tmpShapeDir, "qp_gaf_l");
                                createShapeAndMetaDoc(serviceFeatures, shapeFile.getAbsolutePath());

                                // create k_qp_gaf_kz prj and meta document
                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.k_qp_gaf_kz"));
                                layer.initAndWait();
                                serviceFeatures = layer.getFeatureFactory()
                                            .createFeatures(layer.getQuery(),
                                                    null,
                                                    null,
                                                    0,
                                                    0,
                                                    null);

                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_kz");
                                createShapeAndMetaDoc(serviceFeatures, shapeFile.getAbsolutePath());

                                // create k_qp_gaf_rk prj and meta document
                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.k_qp_gaf_rk"));
                                layer.initAndWait();
                                serviceFeatures = layer.getFeatureFactory()
                                            .createFeatures(layer.getQuery(),
                                                    null,
                                                    null,
                                                    0,
                                                    0,
                                                    null);

                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_rk");
                                createShapeAndMetaDoc(serviceFeatures, shapeFile.getAbsolutePath());

                                // create k_qp_gaf_bk prj and meta document
                                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                            AppBroker.DOMAIN_NAME,
                                            "dlm25w.k_qp_gaf_bk"));
                                layer.initAndWait();
                                serviceFeatures = layer.getFeatureFactory()
                                            .createFeatures(layer.getQuery(),
                                                    null,
                                                    null,
                                                    0,
                                                    0,
                                                    null);

                                shapeFile = new File(tmpShapeDir, "k_qp_gaf_bk");
                                createShapeAndMetaDoc(serviceFeatures, shapeFile.getAbsolutePath());
                            }

                            if (Thread.interrupted()) {
                                return false;
                            }
                            wd.setText(NbBundle.getMessage(
                                    ReportAction.class,
                                    "ExportAction.actionPerformed.createZip",
                                    ++i,
                                    features.size()));
                            final ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
                            zipDirectory(tmpBaseDir, zipStream, "");
                            zipStream.close();

                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (Exception e) {
                                LOG.error("Error while exporting gaf profiles.", e);
                            } finally {
                                if (tmpBaseDir.exists()) {
                                    deleteDirectory(tmpBaseDir);
                                }
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
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void createShapeAndMetaDoc(final List<FeatureServiceFeature> features, final String outputFileStem)
            throws Exception {
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

        // create qp shape
        final FeatureCollection fc = new SimpleFeatureCollection(
                "1",
                features.toArray(new FeatureServiceFeature[features.size()]),
                attribList);

        final ShapeFile shape = new ShapeFile(
                fc,
                outputFileStem);
        final ShapeFileWriter writer = new ShapeFileWriter(shape);
        writer.write();

        // create prj
        final BufferedWriter bw = new BufferedWriter(new FileWriter(
                    outputFileStem
                            + ".prj"));
        bw.write(PRJ_CONTENT);
        bw.close();

        // create meta document
        downloadMetaDocument(service, outputFileStem);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service         DOCUMENT ME!
     * @param  outputFileStem  DOCUMENT ME!
     */
    private void downloadMetaDocument(final CidsLayer service, final String outputFileStem) {
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
                String parent = (dirName.equals("") ? "" : dirName + "/");
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
