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
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.tools.SimpleFeatureCollection;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.PhotoExportDialog;
import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.FeatureServiceHelper;

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
        StaticSwingTools.showDialog(PhotoExportDialog.getInstance());

        if (!PhotoExportDialog.getInstance().isCancelled()) {
            final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    AppBroker.FOTO_MC_NAME);

            if (!features.isEmpty()) {
                final File zipFile = new File(PhotoExportDialog.getInstance().getZipFile());
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

                            final File tmpShapeDir = new File(tmpBaseDir, "Shape");
                            final File tmpPhotoDir = new File(tmpBaseDir, "Fotos");
                            final File tmpReportDir = new File(tmpBaseDir, "Steckbriefe");

                            if (PhotoExportDialog.getInstance().isPhotosSelected()) {
                                tmpPhotoDir.mkdirs();
                            }
                            if (PhotoExportDialog.getInstance().isReportSelected()) {
                                tmpReportDir.mkdirs();
                            }
                            if (PhotoExportDialog.getInstance().isShapeSelected()) {
                                tmpShapeDir.mkdirs();
                            }

                            for (final FeatureServiceFeature feature : features) {
                                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;

                                if (Thread.interrupted()) {
                                    return false;
                                }

                                if (PhotoExportDialog.getInstance().isPhotosSelected()) {
                                    final InputStream is = Photo.loadFileFromWebDav(cidsFeature, null);
                                    final File destFile = new File(tmpPhotoDir, (String)feature.getProperty("foto"));
                                    final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
                                                destFile));
                                    final BufferedInputStream bin = new BufferedInputStream(is);
                                    final byte[] tmp = new byte[256];
                                    int count;

                                    while ((count = bin.read(tmp)) != -1) {
                                        os.write(tmp, 0, count);
                                    }

                                    is.close();
                                    os.close();
                                }

                                if (Thread.interrupted()) {
                                    return false;
                                }
                                if (PhotoExportDialog.getInstance().isReportSelected()) {
                                    final JasperPrint print = Photo.fillreport((CidsLayerFeature)feature);
                                    print.setOrientation(print.getOrientationValue());

                                    String photoFile = (String)feature.getProperty("foto");

                                    photoFile += ".pdf";
                                    final File file = new File(tmpReportDir, photoFile);

                                    final FileOutputStream fout = new FileOutputStream(file);
                                    final BufferedOutputStream out = new BufferedOutputStream(fout);
                                    JasperExportManager.exportReportToPdfStream(print, out);
                                    out.close();
                                }
                                wd.setText(NbBundle.getMessage(
                                        ReportAction.class,
                                        "ExportAction.actionPerformed.progress",
                                        ++i,
                                        features.size()));
                                wd.setProgress(wd.getProgress() + 1);
                            }

                            if (Thread.interrupted()) {
                                return false;
                            }
                            if (PhotoExportDialog.getInstance().isShapeSelected()) {
                                wd.setText(NbBundle.getMessage(
                                        ReportAction.class,
                                        "ExportAction.actionPerformed.createShape",
                                        ++i,
                                        features.size()));
                                final List<String[]> attribList = new ArrayList<String[]>();

                                for (final String attr
                                            : (List<String>)features.get(0).getLayerProperties().getFeatureService()
                                            .getOrderedFeatureServiceAttributes()) {
                                    final String[] attrName = new String[2];
                                    attrName[0] = attr;
                                    attrName[1] = attr;
                                    attribList.add(attrName);
                                }

                                final FeatureCollection fc = new SimpleFeatureCollection(
                                        "1",
                                        features.toArray(new FeatureServiceFeature[features.size()]),
                                        attribList);
                                final File shapeFile = new File(tmpShapeDir, "fotos");

                                final ShapeFile shape = new ShapeFile(
                                        fc,
                                        shapeFile.getAbsolutePath());
                                final ShapeFileWriter writer = new ShapeFileWriter(shape);
                                writer.write();

                                final BufferedWriter bw = new BufferedWriter(new FileWriter(
                                            shapeFile.getAbsolutePath()
                                                    + ".prj"));
                                bw.write(PRJ_CONTENT);
                                bw.close();
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
                                LOG.error("Error while exporting photos.", e);
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
