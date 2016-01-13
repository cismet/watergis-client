/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.panels;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URLDecoder;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import de.cismet.cids.custom.watergis.server.search.PhotoGetBaStat;
import de.cismet.cids.custom.watergis.server.search.PhotoGetLaStat;
import de.cismet.cids.custom.watergis.server.search.PhotoGetPhotoNumber;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsFeatureFactory;
import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.netutil.Proxy;

import de.cismet.tools.CismetThreadPool;
import de.cismet.tools.PasswordEncrypter;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.PhotoAngleListener;
import de.cismet.watergis.gui.dialog.PhotoOptionsDialog;

import de.cismet.watergis.utils.CidsBeanUtils;
import de.cismet.watergis.utils.ExifReader;
import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Photo extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                AppBroker.DOMAIN_NAME,
                "dlm25w.foto"));
    private static int idCounter = -1;
    private static final String WEB_DAV_USER;
    private static final String WEB_DAV_PASSWORD;
    private static final String WEB_DAV_DIRECTORY;
    private static final String FILE_PROTOCOL_PREFIX = "file://";
    private static final Logger LOG = Logger.getLogger(Photo.class);
    private static final WebDavClient webDavClient;
    public static CidsLayerFeature selectedFeature = null;

    static {
        final ResourceBundle bundle = ResourceBundle.getBundle("WebDav");
        String pass = bundle.getString("password");

        if ((pass != null)) {
            pass = new String(PasswordEncrypter.decrypt(pass.toCharArray(), true));
        }

        WEB_DAV_PASSWORD = pass;
        WEB_DAV_USER = bundle.getString("username");
        WEB_DAV_DIRECTORY = bundle.getString("url");

        webDavClient = new WebDavClient(Proxy.fromPreferences(), WEB_DAV_USER, WEB_DAV_PASSWORD);
    }

    //~ Instance fields --------------------------------------------------------

    private List<CidsLayerFeature> newFeatures = new ArrayList<CidsLayerFeature>();
    private String oldMode;
    private List<FeatureServiceFeature> startLocationChange;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butBack;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butNextPhoto;
    private javax.swing.JButton butPrevPhoto;
    private javax.swing.JButton butPrint;
    private javax.swing.JButton butPrintPreview;
    private javax.swing.JButton butRemoveSelection;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butZoomToPhoto;
    private de.cismet.watergis.gui.panels.PhotoEditor editor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton tbAngle;
    private javax.swing.JToggleButton tbLocate;
    private javax.swing.JToggleButton tbProcessing;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Photo.
     */
    public Photo() {
        initComponents();
        tbProcessing.setVisible(false);
//        butRemoveSelection.setVisible(true);
        SelectionManager.getInstance().addSelectionChangedListener(new SelectionChangedListener() {

                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                            "foto");

                    if (tbLocate.isSelected()) {
                        return;
                    }

                    if ((features != null) && !features.isEmpty()) {
                        Collections.sort(features, PhotoOptionsDialog.getInstance().getSorter());
                        if ((selectedFeature == null) || !features.get(0).equals(selectedFeature)) {
                            setEditorFeature((CidsLayerFeature)features.get(0));
                            butPrevPhoto.setEnabled(false);
                            butNextPhoto.setEnabled(features.size() > 1);
                        }
                    } else {
                        setEditorFeature(null);
                    }
                }
            });

        final DropTargetListener dropListener = new DropTargetAdapter() {

                @Override
                public void dragEnter(final DropTargetDragEvent dtde) {
                }

                @Override
                public void dragExit(final DropTargetEvent dte) {
                }

                @Override
                public void drop(final DropTargetDropEvent e) {
                    try {
                        final Transferable tr = e.getTransferable();
                        final DataFlavor[] flavors = tr.getTransferDataFlavors();
                        boolean isAccepted = false;

                        for (int i = 0; i < flavors.length; i++) {
                            if (flavors[i].isFlavorJavaFileListType()) {
                                // zunaechst annehmen
                                e.acceptDrop(e.getDropAction());
                                final List<File> files = (List<File>)tr.getTransferData(flavors[i]);
                                if ((files != null) && (files.size() > 0)) {
                                    CismetThreadPool.execute(new ImageUploadWorker(files));
                                }
                                e.dropComplete(true);
                                return;
                            } else if (flavors[i].isRepresentationClassInputStream()) {
                                // this is used under linux
                                if (!isAccepted) {
                                    e.acceptDrop(e.getDropAction());
                                    isAccepted = true;
                                }
                                final BufferedReader br = new BufferedReader(new InputStreamReader(
                                            (InputStream)tr.getTransferData(flavors[i])));
                                String tmp = null;
                                final List<File> fileList = new ArrayList<File>();
                                while ((tmp = br.readLine()) != null) {
                                    if (tmp.trim().startsWith(FILE_PROTOCOL_PREFIX)) {
                                        File f = new File(tmp.trim().substring(FILE_PROTOCOL_PREFIX.length()));
                                        if (f.exists()) {
                                            fileList.add(f);
                                        } else {
                                            f = new File(URLDecoder.decode(
                                                        tmp.trim().substring(FILE_PROTOCOL_PREFIX.length()),
                                                        "UTF-8"));

                                            if (f.exists()) {
                                                fileList.add(f);
                                            } else {
                                                LOG.warn("File " + f.toString() + " does not exist.");
                                            }
                                        }
                                    }
                                }
                                br.close();

                                if ((fileList != null) && (fileList.size() > 0)) {
                                    CismetThreadPool.execute(new ImageUploadWorker(fileList));
                                    e.dropComplete(true);
                                    return;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        LOG.warn(ex, ex);
                    }
                    // Problem ist aufgetreten
                    e.rejectDrop();
                }
            };

        new DropTarget(editor, dropListener);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    static synchronized int getNewId() {
        return --idCounter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   string  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String objectToString(final Object string) {
        if (string == null) {
            return "";
        } else {
            return string.toString();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jToolBar1 = new javax.swing.JToolBar();
        butPrintPreview = new javax.swing.JButton();
        butPrint = new javax.swing.JButton();
        butPrevPhoto = new javax.swing.JButton();
        butNextPhoto = new javax.swing.JButton();
        tbLocate = new javax.swing.JToggleButton();
        tbAngle = new javax.swing.JToggleButton();
        tbProcessing = new javax.swing.JToggleButton();
        butZoomToPhoto = new javax.swing.JButton();
        butBack = new javax.swing.JButton();
        butDelete = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        butRemoveSelection = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        editor = new PhotoEditor(webDavClient, WEB_DAV_DIRECTORY);

        setLayout(new java.awt.GridBagLayout());

        jToolBar1.setRollover(true);

        butPrintPreview.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-preview.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrintPreview,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butPrintPreview.text")); // NOI18N
        butPrintPreview.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.butPrintPreview.toolTipText"));                                        // NOI18N
        butPrintPreview.setFocusable(false);
        butPrintPreview.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrintPreview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrintPreview.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrintPreviewActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrintPreview);

        butPrint.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-print.png")));                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrint,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butPrint.text"));                            // NOI18N
        butPrint.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butPrint.toolTipText")); // NOI18N
        butPrint.setFocusable(false);
        butPrint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrint.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrintActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrint);

        butPrevPhoto.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-left.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrevPhoto,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butPrevPhoto.text"));       // NOI18N
        butPrevPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.butPrevPhoto.toolTipText"));                                              // NOI18N
        butPrevPhoto.setFocusable(false);
        butPrevPhoto.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrevPhoto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrevPhoto.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrevPhotoActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrevPhoto);

        butNextPhoto.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-right.png"))); // NOI18N
        butNextPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.butNextPhoto.toolTipText"));                                               // NOI18N
        butNextPhoto.setFocusable(false);
        butNextPhoto.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butNextPhoto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butNextPhoto.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butNextPhotoActionPerformed(evt);
                }
            });
        jToolBar1.add(butNextPhoto);

        tbLocate.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-flagtriangle.png")));                // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            tbLocate,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.tbLocate.text"));                            // NOI18N
        tbLocate.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.tbLocate.toolTipText")); // NOI18N
        tbLocate.setFocusable(false);
        tbLocate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbLocate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbLocate.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    tbLocateActionPerformed(evt);
                }
            });
        jToolBar1.add(tbLocate);

        tbAngle.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-angle.png")));                     // NOI18N
        tbAngle.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.tbAngle.toolTipText")); // NOI18N
        tbAngle.setFocusable(false);
        tbAngle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbAngle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbAngle.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    tbAngleActionPerformed(evt);
                }
            });
        jToolBar1.add(tbAngle);

        tbProcessing.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-edit.png"))); // NOI18N
        tbProcessing.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.tbProcessing.toolTipText"));                                                         // NOI18N
        tbProcessing.setFocusable(false);
        tbProcessing.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbProcessing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbProcessing.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    tbProcessingActionPerformed(evt);
                }
            });
        jToolBar1.add(tbProcessing);

        butZoomToPhoto.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-resize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butZoomToPhoto,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butZoomToPhoto.text")); // NOI18N
        butZoomToPhoto.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.butZoomToPhoto.toolTipText"));                                        // NOI18N
        butZoomToPhoto.setFocusable(false);
        butZoomToPhoto.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butZoomToPhoto.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butZoomToPhoto.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butZoomToPhotoActionPerformed(evt);
                }
            });
        jToolBar1.add(butZoomToPhoto);

        butBack.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-thissideup.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butBack,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butBack.text"));                             // NOI18N
        butBack.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butBack.toolTipText"));   // NOI18N
        butBack.setFocusable(false);
        butBack.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butBack.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butBack.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butBackActionPerformed(evt);
                }
            });
        jToolBar1.add(butBack);

        butDelete.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-remove-sign.png")));                   // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butDelete,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butDelete.text"));                             // NOI18N
        butDelete.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butDelete.toolTipText")); // NOI18N
        butDelete.setFocusable(false);
        butDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butDelete.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butDeleteActionPerformed(evt);
                }
            });
        jToolBar1.add(butDelete);

        butSave.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-save-floppy.png")));               // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butSave,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butSave.text"));                           // NOI18N
        butSave.setToolTipText(org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butSave.toolTipText")); // NOI18N
        butSave.setFocusable(false);
        butSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSave.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butSaveActionPerformed(evt);
                }
            });
        jToolBar1.add(butSave);

        butRemoveSelection.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionremove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butRemoveSelection,
            org.openide.util.NbBundle.getMessage(Photo.class, "Photo.butRemoveSelection.text"));      // NOI18N
        butRemoveSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                Photo.class,
                "Photo.butRemoveSelection.toolTipText"));                                             // NOI18N
        butRemoveSelection.setFocusable(false);
        butRemoveSelection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butRemoveSelection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butRemoveSelection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butRemoveSelectionActionPerformed(evt);
                }
            });
        jToolBar1.add(butRemoveSelection);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jToolBar1, gridBagConstraints);

        editor.setMinimumSize(new java.awt.Dimension(800, 500));
        jScrollPane1.setViewportView(editor);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintPreviewActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrintPreviewActionPerformed
        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(
                    Photo.class,
                    "Photo.butPrintPreviewActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected JasperPrint doInBackground() throws Exception {
                    return fillreport(editor.getCidsLayerFeature());
                }

                @Override
                protected void done() {
                    try {
                        final JasperPrint jasperPrint = get();

                        final JRViewer aViewer = new JRViewer(jasperPrint);
                        final JFrame aFrame = new JFrame(org.openide.util.NbBundle.getMessage(
                                    Photo.class,
                                    "Photo.butPrintPreviewActionPerformed.aFrame.title")); // NOI18N
                        aFrame.getContentPane().add(aViewer);
                        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                        final java.awt.Insets insets = aFrame.getInsets();
                        aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
                            aFrame.getHeight()
                                    + insets.top
                                    + insets.bottom
                                    + 20);
                        aFrame.setLocationRelativeTo(Photo.this);
                        aFrame.setVisible(true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    }//GEN-LAST:event_butPrintPreviewActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrintActionPerformed
        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(
                    Photo.class,
                    "AttributeTable.butPrintActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected JasperPrint doInBackground() throws Exception {
                    return fillreport(editor.getCidsLayerFeature());
                }

                @Override
                protected void done() {
                    try {
                        final JasperPrint jasperPrint = get();

                        JasperPrintManager.printReport(jasperPrint, true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    }//GEN-LAST:event_butPrintActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     * @param   parent   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static InputStream loadFileFromWebDav(final CidsLayerFeature feature, final Component parent)
            throws Exception {
        final String path = (String)feature.getBean().getProperty("dateipfad");
        final String file = WebDavHelper.encodeURL((String)feature.getProperty("foto"));

        return webDavClient.getInputStream(WEB_DAV_DIRECTORY + path + file);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void deletePhoto(final CidsLayerFeature feature) throws Exception {
        final String path = (String)feature.getProperty("dateipfad");
        final String file = (String)feature.getProperty("foto");
        WebDavHelper.deleteFileFromWebDAV(file, webDavClient, WEB_DAV_DIRECTORY + path);
        feature.delete();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static JasperPrint fillreport(final CidsLayerFeature feature) throws Exception {
//        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final JRDataSource ds = new JRDataSource() {

                private boolean first = true;

                @Override
                public boolean next() throws JRException {
                    if (first) {
                        first = false;
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                public Object getFieldValue(final JRField jrf) throws JRException {
                    return feature.getBean().getProperty(jrf.getName());
                }
            };

        final Map<String, Object> map = new HashMap<String, Object>();
        final DecimalFormat format = new DecimalFormat("0.00");
        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');
        format.setDecimalFormatSymbols(symbols);
        final CidsBean basisStat = (CidsBean)feature.getBean().getProperty("ba_st");
        final CidsBean freigabe = (CidsBean)feature.getBean().getProperty("freigabe");

        map.put("punkt", feature.getGeometry());
        map.put("foto", feature.getProperty("foto"));
        map.put("upl_nutzer", feature.getProperty("upl_name"));
        map.put("upl_datum", obj2Time(feature.getProperty("upl_zeit")));
        map.put("bild_id", feature.getProperty("foto_nr"));
        map.put("pos", format.format(feature.getProperty("re")) + ", " + format.format(feature.getProperty("ho")));
        map.put("winkel", feature.getProperty("winkel"));
        map.put("lawa", feature.getProperty("la_cd"));
        if (basisStat != null) {
            map.put("basis", feature.getProperty("route.ba_cd"));
            map.put("basis_stat", basisStat.getProperty("wert"));
        } else {
            map.put("basis", "");
            map.put("basis_stat", 0.0);
        }
        map.put("lawa_stat", feature.getProperty("la_st"));
        map.put("re_li", objectToString(feature.getProperty("l_rl")));
        map.put("status", objectToString(feature.getProperty("l_st")));
        map.put("aufn_nutzer", feature.getProperty("aufn_name"));
        map.put("aufn_datum", obj2Time(feature.getProperty("aufn_zeit")));
        if (freigabe != null) {
            map.put("freigabe", freigabe.toString());
        } else {
            map.put("freigabe", "");
        }
        map.put("titel", feature.getProperty("titel"));
        map.put("beschreibung", feature.getProperty("beschreib"));
        map.put("bemerkung", feature.getProperty("bemerkung"));
        map.put("foto", feature.getProperty("foto"));

        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(Photo.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/foto.jasper"));

        return JasperFillManager.fillReport(jasperReport, map, ds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String obj2Time(final Object o) {
        if (o == null) {
            return "";
        } else {
            try {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                return format.format(o);
            } catch (IllegalArgumentException e) {
                LOG.error("Not a date", e);
                return "";
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static void reloadPhotoServices() {
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                "foto");

        for (final AbstractFeatureService featureService : services) {
            featureService.retrieve(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrevPhotoActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPrevPhotoActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures("foto");

        if ((features != null) && !features.isEmpty()) {
            int index = features.indexOf(feature);
            --index;

            if (index >= 0) {
                setEditorFeature((CidsLayerFeature)features.get(index));
            }
            butNextPhoto.setEnabled(index != (features.size() - 1));
            butPrevPhoto.setEnabled(index != 0);
        }
    }//GEN-LAST:event_butPrevPhotoActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butNextPhotoActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNextPhotoActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures("foto");

        if ((features != null) && !features.isEmpty()) {
            int index = features.indexOf(feature);
            ++index;

            if (index < features.size()) {
                setEditorFeature((CidsLayerFeature)features.get(index));
            }

            butNextPhoto.setEnabled(index != (features.size() - 1));
            butPrevPhoto.setEnabled(index != 0);
        }
    }//GEN-LAST:event_butNextPhotoActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbProcessingActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbProcessingActionPerformed
    }//GEN-LAST:event_tbProcessingActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butBackActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butBackActionPerformed
        AppBroker.getInstance().getMappingComponent().back(true);
    }//GEN-LAST:event_butBackActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butSaveActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butSaveActionPerformed
        try {
            final CidsLayerFeature feature = editor.getCidsLayerFeature();
            final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();

            if (ruleSet != null) {
                ruleSet.beforeSave(feature);
            }
            feature.saveChanges();
            reloadPhotoServices();
        } catch (Exception e) {
            LOG.error("Eror while saving feature", e);
        }
    }//GEN-LAST:event_butSaveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butRemoveSelectionActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butRemoveSelectionActionPerformed
        SelectionManager.getInstance().removeSelectedFeatures(editor.getCidsLayerFeature());

//        final CidsLayerFeature feature = editor.getCidsLayerFeature();
//        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures("foto");
//
//        if ((features != null) && !features.isEmpty()) {
//            int index = features.indexOf(feature);
//            ++index;
//
//            if (index < features.size()) {
//                setEditorFeature((CidsLayerFeature)features.get(index));
//            }
//
//            butNextPhoto.setEnabled(index != (features.size() - 1));
//            butPrevPhoto.setEnabled(index != 0);
//        }
    }//GEN-LAST:event_butRemoveSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butZoomToPhotoActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butZoomToPhotoActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final MappingComponent mappingComponent = CismapBroker.getInstance().getMappingComponent();

        if (mappingComponent != null) {
            final XBoundingBox bbox = new XBoundingBox(feature.getGeometry());
            bbox.increase(10);
            final BoundingBox scaledBBox = mappingComponent.getScaledBoundingBox(500, bbox);
            mappingComponent.gotoBoundingBoxWithHistory(scaledBBox);
        } else {
            LOG.error("MappingComponent is not set");
        }
    }//GEN-LAST:event_butZoomToPhotoActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butDeleteActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butDeleteActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();

        final int ans = JOptionPane.showConfirmDialog(
                Photo.this,
                NbBundle.getMessage(
                    Photo.class,
                    "Photo.butDeleteActionPerformed().text"),
                NbBundle.getMessage(Photo.class, "Photo.butDeleteActionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans != JOptionPane.YES_OPTION) {
            return;
        }

        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                true,
                NbBundle.getMessage(
                    Photo.class,
                    "Photo.butDeleteActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected Void doInBackground() throws Exception {
                    deletePhoto(feature);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        reloadPhotoServices();
                    } catch (Exception e) {
                        LOG.error("Error while deleting objects", e);
                    }
                }
            };

        wdt.start();
    }//GEN-LAST:event_butDeleteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbLocateActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbLocateActionPerformed
        if (tbLocate.isSelected()) {
            startLocationChange = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                    "foto");
            makeFeatureEditable(editor.getCidsLayerFeature());
        } else {
            final CidsLayerFeature layerFeature = editor.getCidsLayerFeature();

            try {
                layerFeature.getBean().setProperty("geom.geo_field", layerFeature.getGeometry());
                // save
                final CidsLayerFeature feature = editor.getCidsLayerFeature();
                final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();

                if (ruleSet != null) {
                    ruleSet.beforeSave(feature);
                }
                feature.saveChanges();
                layerFeature.setEditable(false);
                reloadPhotoServices();
            } catch (Exception e) {
                LOG.error("Error while setting the new geometry", e);
            }
        }
    }//GEN-LAST:event_tbLocateActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbAngleActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbAngleActionPerformed
        if (tbAngle.isSelected()) {
            oldMode = AppBroker.getInstance().getMappingComponent().getInteractionMode();
            AppBroker.getInstance().getMappingComponent().setInteractionMode(PhotoAngleListener.MODE);
        } else if (oldMode != null) {
            AppBroker.getInstance().getMappingComponent().setInteractionMode(oldMode);
        }
    }//GEN-LAST:event_tbAngleActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void setEditorFeature(final CidsLayerFeature feature) {
        if (selectedFeature != null) {
            if (selectedFeature.getBean().getMetaObject().isChanged()) {
                final int ans = JOptionPane.showConfirmDialog(
                        Photo.this,
                        NbBundle.getMessage(
                            Photo.class,
                            "Photo.setEditorFeature().text"),
                        NbBundle.getMessage(Photo.class, "Photo.setEditorFeature().title"),
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (ans == JOptionPane.YES_OPTION) {
                    butSaveActionPerformed(null);
                } else if (ans == JOptionPane.NO_OPTION) {
                    //nothing to do
                } else {
                    return;
                }
            }
        }
        if (tbLocate.isSelected()) {
            tbLocate.setSelected(false);
            tbLocateActionPerformed(null);
        }
        if (tbAngle.isSelected()) {
            tbAngle.setSelected(false);
            tbAngleActionPerformed(null);
        }
        editor.setCidsLayerFeature(feature);
        enableToolbar(feature != null);
        selectedFeature = feature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    private void enableToolbar(final boolean enabled) {
        boolean writable = enabled && editor.isUploader();
        butBack.setEnabled(enabled);
        butNextPhoto.setEnabled(enabled);
        butPrevPhoto.setEnabled(enabled);
        butPrint.setEnabled(enabled);
        butPrintPreview.setEnabled(enabled);
        butRemoveSelection.setEnabled(enabled);
        butZoomToPhoto.setEnabled(enabled);

        tbAngle.setEnabled(writable);
        tbLocate.setEnabled(writable);
        tbProcessing.setEnabled(writable);
        butSave.setEnabled(writable);
        butDelete.setEnabled(writable);
    }

    /**
     * Locks the given feature, if a corresponding locker exists and make the feature editable.
     *
     * @param  feature  the feature to make editable
     */
    private void makeFeatureEditable(final FeatureServiceFeature feature) {
//        if (feature instanceof PermissionProvider) {
//            final PermissionProvider pp = (PermissionProvider)feature;
//
//            if (!pp.hasWritePermissions()) {
//                JOptionPane.showMessageDialog(
//                    this,
//                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.makeFeatureEditable.noPermissions.text"),
//                    NbBundle.getMessage(AttributeTable.class, "AttributeTable.makeFeatureEditable.noPermissions.title"),
//                    JOptionPane.ERROR_MESSAGE);
//
//                return;
//            }
//        }

        if ((feature != null) && !feature.isEditable()) {
            try {
//                if (locker != null) {
//                    lockingObjects.add(locker.lock(feature));
//                }
                feature.setEditable(true);
//                if (!lockedFeatures.contains(feature)) {
//                    lockedFeatures.add(feature);
//                    ((DefaultFeatureServiceFeature)feature).addPropertyChangeListener(model);
//                }
//            } catch (LockAlreadyExistsException ex) {
//                JOptionPane.showMessageDialog(
//                    Photo.this,
//                    NbBundle.getMessage(
//                        AttributeTable.class,
//                        "AttributeTable.ListSelectionListener.valueChanged().lockexists.message",
//                        feature.getId(),
//                        ex.getLockMessage()),
//                    NbBundle.getMessage(
//                        AttributeTable.class,
//                        "AttributeTable.ListSelectionListener.valueChanged().lockexists.title"),
//                    JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                LOG.error("Error while locking feature.", ex);
//                JOptionPane.showMessageDialog(
//                    Photo.this,
//                    NbBundle.getMessage(
//                        AttributeTable.class,
//                        "AttributeTable.ListSelectionListener.valueChanged().exception.message",
//                        ex.getMessage()),
//                    NbBundle.getMessage(
//                        AttributeTable.class,
//                        "AttributeTable.ListSelectionListener.valueChanged().exception.title"),
//                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class ImageUploadWorker extends SwingWorker<List<CidsLayerFeature>, Void> {

        //~ Instance fields ----------------------------------------------------

        private final Collection<File> fotos;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageUploadWorker object.
         *
         * @param  fotos  DOCUMENT ME!
         */
        public ImageUploadWorker(final Collection<File> fotos) {
            this.fotos = fotos;
//            lblPicture.setText("");
//            lblPicture.setToolTipText(null);
//            showWait(true);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected List<CidsLayerFeature> doInBackground() throws Exception {
            final List<CidsLayerFeature> newBeans = new ArrayList<CidsLayerFeature>();

            for (final File imageFile : fotos) {
                final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
                String fileName = format.format(new Date());
                String filePrefix = "foto/";

                if (AppBroker.getInstance().getOwnWwGr() != null) {
                    final Integer wwGr = (Integer)AppBroker.getInstance().getOwnWwGr().getProperty("ww_gr");
                    filePrefix += wwGr + "/";
                    fileName += "-" + wwGr;
                }

                final String userName = SessionManager.getSession().getUser().getName();

                filePrefix += userName + "/";
                fileName += "-" + userName + "-" + imageFile.getName();

//                final String webFileName = WebDavHelper.generateWebDAVFileName(filePrefix, fileName);
                WebDavHelper.uploadFileToWebDAV(
                    fileName,
                    imageFile,
                    WEB_DAV_DIRECTORY
                            + filePrefix,
                    webDavClient,
                    Photo.this);
                final Map<String, Object> props = new HashMap<String, Object>();
                final CidsBean newFotoBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.foto");
                final int id = getNewId();

                for (final String propName : newFotoBean.getPropertyNames()) {
                    props.put(propName, null);
                }
                props.put("id", id);
                layer.initAndWait();
                final CidsLayerFeature feature = new CidsLayerFeature(
                        props,
                        layer.getMetaClass(),
                        ((CidsFeatureFactory)layer.getFeatureFactory()).getLayerInfo(),
                        layer.getLayerProperties(),
                        null);

//                newFotoBean = feature.getBean();
                final Date currentTime = new Date();
                newFotoBean.setProperty("id", id);
                newFotoBean.setProperty("foto", fileName);
                newFotoBean.setProperty("dateipfad", filePrefix);
                newFotoBean.setProperty("upl_datum", new java.sql.Date(currentTime.getTime()));
                newFotoBean.setProperty("upl_zeit", new java.sql.Timestamp(currentTime.getTime()));
                newFotoBean.setProperty("upl_name", userName);
                newFotoBean.setProperty("dateipfad", filePrefix);
                newFotoBean.setProperty("foto_nr", getNextPhotoNumber());
                newFotoBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
                final ExifReader reader = new ExifReader(imageFile);

                try {
                    // the coordinates of the image should be used
                    Point point = reader.getGpsCoords();
                    if (point != null) {
                        point = CrsTransformer.transformToDefaultCrs(point);
                        point.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

                        final CidsBean geom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");

                        geom.setProperty("geo_field", point);
                        newFotoBean.setProperty("geom", geom);
                        feature.setGeometry(point);

                        final CidsBean statusBean = getCatalogueElement("dlm25w.k_l_st", "l_st", "V-GPS3");

                        if (statusBean != null) {
                            newFotoBean.setProperty("l_st", statusBean);
                        }
                    }

                    newFotoBean.setProperty("winkel", reader.getGpsDirection());

                    if (reader.getTime() != null) {
                        newFotoBean.setProperty("aufn_datum", new java.sql.Date(reader.getTime().getTime()));
                        newFotoBean.setProperty("aufn_zeit", new java.sql.Timestamp(reader.getTime().getTime()));
                    }
                } catch (Throwable ex) {
                    LOG.error("Error while reading exif data.", ex);
                }

                if (feature.getGeometry() == null) {
                    final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
                    final BoundingBox bbox = mc.getCurrentBoundingBoxFromCamera();
                    final Geometry geom = bbox.getGeometry(CrsTransformer.extractSridFromCrs(
                                CismapBroker.getInstance().getSrs().getCode()));
                    final Geometry center = geom.getCentroid();
                    final CidsBean cidsGeom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");

                    cidsGeom.setProperty("geo_field", center);
                    newFotoBean.setProperty("geom", cidsGeom);
                    feature.setGeometry(center);

                    final CidsBean statusBean = getCatalogueElement("dlm25w.k_l_st", "l_st", "DOP");
                    if (statusBean != null) {
                        newFotoBean.setProperty("l_st", statusBean);
                    }
                }

                if (PhotoOptionsDialog.getInstance().isAutomatic()) {
                    final Station stat = getNextFgBaStat(feature.getGeometry(),
                            PhotoOptionsDialog.getInstance().getDistance());

                    if (stat != null) {
                        final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                                AppBroker.DOMAIN_NAME,
                                "dlm25w.fg_ba");
                        final CidsBean stationBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.fg_ba_punkt");
                        final CidsBean geom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                        final MetaObject route = SessionManager.getProxy()
                                    .getMetaObject(stat.getId(), routeMc.getID(), AppBroker.DOMAIN_NAME);

                        geom.setProperty("geo_field", stat.getPoint());

                        stationBean.setProperty("wert", stat.getStat());
                        stationBean.setProperty("route", route.getBean());
                        stationBean.setProperty("real_point", geom);

                        newFotoBean.setProperty("ba_st", stationBean);
                        feature.setProperty("ba_cd", stat.getBaCd());

                        final Station laStat = getNextFgLaStat(stat.getPoint());

                        if (laStat != null) {
                            newFotoBean.setProperty("la_cd", stat.getBaCd());
                            newFotoBean.setProperty("la_st", stat.getStat());
                        }
                    }
                }

                final CidsBean freigabeBean = getCatalogueElement("dlm25w.k_freigabe", "name", "alle Nutzer");
                if (freigabeBean != null) {
                    newFotoBean.setProperty("freigabe", freigabeBean);
                }

                feature.setMetaObject(newFotoBean.getMetaObject());

                final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();

                if (ruleSet != null) {
                    ruleSet.beforeSave(feature);
                }
                feature.saveChanges();
                newBeans.add(feature);
            }
            return newBeans;
        }

        @Override
        protected void done() {
            try {
                final List<CidsLayerFeature> newFeatures = get();

                if (!newFeatures.isEmpty()) {
                    newFeatures.addAll(newFeatures);
                    // todo: prüfen, ob noch nicht gespeicherte Fotos existieren
                    setEditorFeature(newFeatures.get(0));
                    reloadPhotoServices();
                }
            } catch (InterruptedException ex) {
                LOG.warn(ex, ex);
            } catch (ExecutionException ex) {
                LOG.error(ex, ex);
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   className      DOCUMENT ME!
         * @param   attributeName  DOCUMENT ME!
         * @param   value          DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private CidsBean getCatalogueElement(final String className,
                final String attributeName,
                final String value) {
            final MetaClass mc = ClassCacheMultiple.getMetaClass(
                    AppBroker.DOMAIN_NAME,
                    className);

            String query = "select " + mc.getID() + ", " + mc.getPrimaryKey() + " from " + mc.getTableName(); // NOI18N
            query += " WHERE " + attributeName + " = '" + value + "'";

            try {
                final MetaObject[] mo = SessionManager.getConnection()
                            .getMetaObjectByQuery(SessionManager.getSession().getUser(), query);

                if ((mo != null) && (mo.length > 0)) {
                    return mo[0].getBean();
                }
            } catch (Exception e) {
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private String getNextPhotoNumber() {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new PhotoGetPhotoNumber());

                if ((attributes != null) && !attributes.isEmpty()) {
                    return String.valueOf(attributes.get(0).get(0));
                }
            } catch (Exception ex) {
                LOG.error("Errro while retrieving next photo number.", ex);
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   geom     DOCUMENT ME!
         * @param   maxDist  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Station getNextFgBaStat(final Geometry geom, final double maxDist) {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new PhotoGetBaStat(geom, maxDist));

                if ((attributes != null) && !attributes.isEmpty()) {
                    final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CismapBroker.getInstance().getDefaultCrsAlias());
                    final WKBReader wkbReader = new WKBReader(geomFactory);
                    final Station stat = new Station();
                    stat.setId((Integer)attributes.get(0).get(0));
                    stat.setBaCd((String)attributes.get(0).get(1));
                    stat.setStat((Double)attributes.get(0).get(2));
                    stat.setPoint(wkbReader.read((byte[])attributes.get(0).get(3)));
                    return stat;
                }
            } catch (Exception ex) {
                LOG.error("Errro while retrieving next fg ba station.", ex);
            }

            return null;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   geom  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Station getNextFgLaStat(final Geometry geom) {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new PhotoGetLaStat(geom));

                if ((attributes != null) && !attributes.isEmpty()) {
                    final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CismapBroker.getInstance().getDefaultCrsAlias());
                    final WKBReader wkbReader = new WKBReader(geomFactory);
                    final Station stat = new Station();
//                    stat.setId((Integer)attributes.get(0).get(0));
                    stat.setBaCd(String.valueOf(attributes.get(0).get(1)));
                    stat.setStat((Double)attributes.get(0).get(2));
//                    stat.setPoint(wkbReader.read((byte[])attributes.get(0).get(3)));
                    return stat;
                }
            } catch (Exception ex) {
                LOG.error("Errro while retrieving next fg ba station.", ex);
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Station {

        //~ Instance fields ----------------------------------------------------

        private int id;
        private String baCd;
        private double stat;
        private Geometry point;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the id
         */
        public int getId() {
            return id;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  id  the id to set
         */
        public void setId(final int id) {
            this.id = id;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the ba_cd
         */
        public String getBaCd() {
            return baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  baCd  the ba_cd to set
         */
        public void setBaCd(final String baCd) {
            this.baCd = baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the stat
         */
        public double getStat() {
            return stat;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  stat  the stat to set
         */
        public void setStat(final double stat) {
            this.stat = stat;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the point
         */
        public Geometry getPoint() {
            return point;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  point  the point to set
         */
        public void setPoint(final Geometry point) {
            this.point = point;
        }
    }
}