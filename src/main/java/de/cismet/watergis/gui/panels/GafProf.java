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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.type.OrientationEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRSaveContributor;
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

import java.math.BigDecimal;

import java.net.URLDecoder;

import java.sql.Timestamp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import de.cismet.cids.custom.watergis.server.search.GafPosition;
import de.cismet.cids.custom.watergis.server.search.PhotoGetBaStat;
import de.cismet.cids.custom.watergis.server.search.PhotoGetLaStat;
import de.cismet.cids.custom.watergis.server.search.PhotoGetPhotoNumber;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsFeatureFactory;
import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.tools.CismetThreadPool;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.gaf.CheckAction;
import de.cismet.watergis.gui.dialog.GafImportDialog;
import de.cismet.watergis.gui.dialog.PhotoOptionsDialog;

import de.cismet.watergis.profile.AbstractImportDialog;
import de.cismet.watergis.profile.GafReader;
import de.cismet.watergis.profile.ProfileLine;
import de.cismet.watergis.profile.ProfileReader;
import de.cismet.watergis.profile.ProfileReaderFactory;

import de.cismet.watergis.utils.CidsBeanUtils;
import de.cismet.watergis.utils.ContributorWrapper;
import de.cismet.watergis.utils.ConversionUtils;
import de.cismet.watergis.utils.CustomGafCatalogueReader;
import de.cismet.watergis.utils.CustomJrViewer;
import de.cismet.watergis.utils.FeatureServiceHelper;

import static de.cismet.watergis.gui.panels.Photo.selectedFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GafProf extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final CidsLayer ppLayer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                AppBroker.DOMAIN_NAME,
                "dlm25w.qp_gaf_pp"));
    private static int idCounter = -1;
    private static final String FILE_PROTOCOL_PREFIX = "file://";
    private static final Logger LOG = Logger.getLogger(GafProf.class);
    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    public static CidsLayerFeature selectedFeature = null;

    //~ Instance fields --------------------------------------------------------

    private List<CidsLayerFeature> newFeatures = new ArrayList<CidsLayerFeature>();
    private String oldMode;
    private boolean askForSave = true;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butBack;
    private javax.swing.JButton butDelete;
    private javax.swing.JButton butNextProfile;
    private javax.swing.JButton butPrevProfile;
    private javax.swing.JButton butPrint;
    private javax.swing.JButton butPrintPreview;
    private javax.swing.JButton butRemoveSelection;
    private javax.swing.JButton butSave;
    private javax.swing.JButton butSaveAll;
    private javax.swing.JButton butZoomToProfile;
    private de.cismet.watergis.gui.panels.GafProfEditor editor;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToggleButton tbLocate;
    private javax.swing.JToggleButton tbProcessing;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form Photo.
     */
    public GafProf() {
        initComponents();
        tbLocate.setVisible(false);
        editor.setPpLayer(ppLayer);
        tbProcessing.setVisible(false);
//        butRemoveSelection.setVisible(true);
        SelectionManager.getInstance().addSelectionChangedListener(new SelectionChangedListener() {

                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                            "qp");

                    if (tbLocate.isSelected() || !GafProf.this.isDisplayable()) {
                        return;
                    }
                    if (!isShowing()) {
                        // do not load an object into the editor, if the editor is not showing
                        return;
                    }
                    if ((features != null) && !features.isEmpty()) {
                        Collections.sort(features, PhotoOptionsDialog.getInstance().getSorter());
                        if ((selectedFeature == null) || !features.get(0).equals(selectedFeature)) {
                            setEditorFeature((CidsLayerFeature)features.get(0));
                            butPrevProfile.setEnabled(false);
                            butNextProfile.setEnabled(features.size() > 1);
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
                                    editor.showEditor(true, true);
                                    addGafServicesToTree();
                                    new GafUploadWorker(files).start();
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
                                String tmp;
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

                                if (fileList.size() > 0) {
                                    editor.showEditor(true, true);
                                    addGafServicesToTree();
                                    new GafUploadWorker(fileList).start();
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
        butPrevProfile = new javax.swing.JButton();
        butNextProfile = new javax.swing.JButton();
        tbLocate = new javax.swing.JToggleButton();
        tbProcessing = new javax.swing.JToggleButton();
        butZoomToProfile = new javax.swing.JButton();
        butBack = new javax.swing.JButton();
        butDelete = new javax.swing.JButton();
        butSave = new javax.swing.JButton();
        butRemoveSelection = new javax.swing.JButton();
        butSaveAll = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        editor = new de.cismet.watergis.gui.panels.GafProfEditor();

        setLayout(new java.awt.GridBagLayout());

        jToolBar1.setRollover(true);

        butPrintPreview.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-searchdocument.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrintPreview,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butPrintPreview.text"));    // NOI18N
        butPrintPreview.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butPrintPreview.toolTipText"));                                             // NOI18N
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
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-print.png")));                           // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrint,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butPrint.text"));                            // NOI18N
        butPrint.setToolTipText(org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butPrint.toolTipText")); // NOI18N
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

        butPrevProfile.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-left.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butPrevProfile,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butPrevProfile.text")); // NOI18N
        butPrevProfile.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butPrevProfile.toolTipText"));                                          // NOI18N
        butPrevProfile.setFocusable(false);
        butPrevProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butPrevProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butPrevProfile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butPrevProfileActionPerformed(evt);
                }
            });
        jToolBar1.add(butPrevProfile);

        butNextProfile.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-arrow-right.png"))); // NOI18N
        butNextProfile.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butNextProfile.toolTipText"));                                           // NOI18N
        butNextProfile.setFocusable(false);
        butNextProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butNextProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butNextProfile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butNextProfileActionPerformed(evt);
                }
            });
        jToolBar1.add(butNextProfile);

        tbLocate.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-flagtriangle.png")));                    // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            tbLocate,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.tbLocate.text"));                            // NOI18N
        tbLocate.setToolTipText(org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.tbLocate.toolTipText")); // NOI18N
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

        tbProcessing.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/cismap/commons/gui/attributetable/res/icon-edit.png"))); // NOI18N
        tbProcessing.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.tbProcessing.toolTipText"));                                                       // NOI18N
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

        butZoomToProfile.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-selectionadd.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butZoomToProfile,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butZoomToProfile.text")); // NOI18N
        butZoomToProfile.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butZoomToProfile.toolTipText"));                                          // NOI18N
        butZoomToProfile.setFocusable(false);
        butZoomToProfile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butZoomToProfile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butZoomToProfile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butZoomToProfileActionPerformed(evt);
                }
            });
        jToolBar1.add(butZoomToProfile);

        butBack.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-fullscreen.png")));                    // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butBack,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butBack.text"));                           // NOI18N
        butBack.setToolTipText(org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butBack.toolTipText")); // NOI18N
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
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-circledelete.png")));                      // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butDelete,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butDelete.text"));                             // NOI18N
        butDelete.setToolTipText(org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butDelete.toolTipText")); // NOI18N
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
                getClass().getResource("/de/cismet/watergis/res/icons16/icon-editalt.png")));                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butSave,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butSave.text"));                           // NOI18N
        butSave.setToolTipText(org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butSave.toolTipText")); // NOI18N
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
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butRemoveSelection.text"));  // NOI18N
        butRemoveSelection.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butRemoveSelection.toolTipText"));                                           // NOI18N
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

        butSaveAll.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons16/saveAll.png")));     // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            butSaveAll,
            org.openide.util.NbBundle.getMessage(GafProf.class, "GafProf.butSaveAll.text")); // NOI18N
        butSaveAll.setToolTipText(org.openide.util.NbBundle.getMessage(
                GafProf.class,
                "GafProf.butSaveAll.toolTipText"));                                          // NOI18N
        butSaveAll.setFocusable(false);
        butSaveAll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        butSaveAll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        butSaveAll.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butSaveAllActionPerformed(evt);
                }
            });
        jToolBar1.add(butSaveAll);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jToolBar1, gridBagConstraints);

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
    private void butPrintPreviewActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPrintPreviewActionPerformed
        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(GafProf.class,
                    "GafProf.butPrintPreviewActionPerformed.WaitingDialogThread"),
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

                        final CustomJrViewer aViewer = new CustomJrViewer(jasperPrint);
                        final List<JRSaveContributor> contributors = new ArrayList<JRSaveContributor>();

                        for (final JRSaveContributor contributor : aViewer.getSaveContributors()) {
                            if (contributor.getDescription().toLowerCase().contains("pdf")) {
                                contributors.add(new ContributorWrapper(contributor, "PDF"));
                            } else if (contributor.getDescription().toLowerCase().contains("docx")) {
                                contributors.add(new ContributorWrapper(contributor, "DOCX"));
                            }
                        }

                        Collections.sort(contributors, new Comparator<JRSaveContributor>() {

                                @Override
                                public int compare(final JRSaveContributor o1, final JRSaveContributor o2) {
                                    if ((o1 != null) && (o2 != null)) {
                                        return o1.getDescription().compareTo(o2.getDescription());
                                    } else if ((o1 == null) && (o2 == null)) {
                                        return 0;
                                    } else if (o1 == null) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                }
                            });

                        aViewer.setSaveContributors(contributors.toArray(new JRSaveContributor[contributors.size()]));

                        final JFrame aFrame = new JFrame(org.openide.util.NbBundle.getMessage(
                                    GafProf.class,
                                    "GafProf.butPrintPreviewActionPerformed.aFrame.title")); // NOI18N
                        aFrame.getContentPane().add(aViewer);
                        final java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                        aFrame.setSize(screenSize.width / 2, screenSize.height / 2);
                        final java.awt.Insets insets = aFrame.getInsets();
                        aFrame.setSize(aFrame.getWidth() + insets.left + insets.right,
                            aFrame.getHeight()
                                    + insets.top
                                    + insets.bottom
                                    + 20);
                        aFrame.setLocationRelativeTo(GafProf.this);
                        aFrame.setVisible(true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butPrintPreviewActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrintActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPrintActionPerformed
        final WaitingDialogThread<JasperPrint> wdt = new WaitingDialogThread<JasperPrint>(StaticSwingTools
                        .getParentFrame(this),
                true,
                NbBundle.getMessage(GafProf.class,
                    "GafProf.butPrintActionPerformed.WaitingDialogThread"),
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
                        jasperPrint.setOrientation(OrientationEnum.LANDSCAPE);

                        JasperPrintManager.printReport(jasperPrint, true);
                    } catch (Exception e) {
                        LOG.error("Error while creating report", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butPrintActionPerformed

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
    @Deprecated
    public static InputStream loadFileFromWebDav(final CidsLayerFeature feature, final Component parent)
            throws Exception {
        return null;
    }

    /**
     * Deletes the given feature and all dependend objects.
     *
     * @param   feature  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void deleteProfile(final CidsLayerFeature feature) throws Exception {
        final String nr = String.valueOf(feature.getProperty("qp_nr"));
        feature.delete();
        ppLayer.initAndWait();
        final List<DefaultFeatureServiceFeature> features = ppLayer.getFeatureFactory()
                    .createFeatures("qp_nr = " + nr,
                        null,
                        null,
                        0,
                        0,
                        null);

        if (features != null) {
            for (final DefaultFeatureServiceFeature f : features) {
                if (f instanceof CidsLayerFeature) {
                    ((CidsLayerFeature)f).delete();
                }
            }
        }
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
    public static String getBasicReportFileName(final CidsLayerFeature feature) throws Exception {
        final Object baStObject = feature.getProperty("ba_st");
        String baCd;
        Double stat;

        if (baStObject instanceof CidsBean) {
            baCd = (String)((CidsBean)baStObject).getProperty("route.ba_cd");
            stat = (Double)((CidsBean)baStObject).getProperty("wert");
        } else {
            baCd = (String)feature.getProperty("ba_cd");
            stat = (Double)baStObject;
        }

        final DecimalFormat format = new DecimalFormat("0.00");
        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);

        return removeIllegaleFileNameCharacters(baCd) + "__" + format.format(stat) + ".pdf";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Double getFeatureStation(final CidsLayerFeature feature) {
        final Object baStObject = feature.getProperty("ba_st");
        Double stat;

        if (baStObject instanceof CidsBean) {
            stat = (Double)((CidsBean)baStObject).getProperty("wert");
        } else {
            stat = (Double)baStObject;
        }

        return stat;
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
    public static String getBasicGafFileName(final CidsLayerFeature feature) throws Exception {
        final Object baStObject = feature.getProperty("ba_st");
        String baCd;

        if (baStObject instanceof CidsBean) {
            baCd = (String)((CidsBean)baStObject).getProperty("route.ba_cd");
        } else {
            baCd = (String)feature.getProperty("ba_cd");
        }

        return removeIllegaleFileNameCharacters(baCd) + ".gaf";
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
    public static String getLawaReportFileName(final CidsLayerFeature feature) throws Exception {
        final String baCd = String.valueOf(feature.getProperty("la_cd"));
        final Double stat = (Double)feature.getProperty("la_st");

        final DecimalFormat format = new DecimalFormat("0.00");
        final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
        symbols.setDecimalSeparator(',');
        format.setDecimalFormatSymbols(symbols);
        format.setGroupingUsed(false);

        return removeIllegaleFileNameCharacters(baCd) + "__" + format.format(stat) + ".pdf";
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
    public static String getLawaGafFileName(final CidsLayerFeature feature) throws Exception {
        final String baCd = String.valueOf(feature.getProperty("la_cd"));

        return removeIllegaleFileNameCharacters(baCd) + ".gaf";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String removeIllegaleFileNameCharacters(String name) {
        name = name.replace("\\", "-");
        name = name.replace("/", "_");
        name = name.replace(":", "+");
        name = name.replace("*", "#");
        name = name.replace("?", "!");
        name = name.replace("\"", "'");
        name = name.replace("<", "$");
        name = name.replace(">", "%");
        name = name.replace("|", "~");

        return name;
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
        final CidsBean basisStat = (CidsBean)feature.getBean().getProperty("ba_st");
        final CidsBean freigabe = (CidsBean)feature.getBean().getProperty("freigabe");
        final User user = SessionManager.getSession().getUser();
        Boolean hasPermissionforBemerkung;

        if ((user == null) || user.getUserGroup().getName().equalsIgnoreCase("administratoren")) {
            hasPermissionforBemerkung = true;
        } else {
            hasPermissionforBemerkung = (feature.getProperty("upl_name") != null)
                        && feature.getProperty("upl_name").equals(user.getName());
        }

        map.put("punkt", feature.getGeometry());
        map.put("upl_nutzer", feature.getProperty("upl_name"));
        map.put(
            "upl_datum",
            dateTime2String(feature.getProperty("upl_datum"), (String)feature.getProperty("upl_zeit")));
        map.put("qp_nr", objectToString(feature.getProperty("qp_nr")));
        map.put(
            "pos",
            ConversionUtils.numberToString(feature.getProperty("re"))
                    + " "
                    + ConversionUtils.numberToString(feature.getProperty("ho")));
        map.put("lawa", objectToString(feature.getProperty("la_cd")));
        map.put(
            "lawa_stat",
            ((feature.getProperty("la_st") == null) ? ""
                                                    : ConversionUtils.numberToString(feature.getProperty("la_st"))));
        map.put("dhhn", "ja");
        map.put("status", objectToString(feature.getProperty("l_st")));
        map.put("aufn_nutzer", feature.getProperty("aufn_name"));
        map.put(
            "aufn_datum",
            dateTime2String(feature.getProperty("aufn_datum"), (String)feature.getProperty("aufn_zeit")));
        map.put("titel", feature.getProperty("titel"));
        map.put("beschreibung", feature.getProperty("beschreib"));
        map.put("bemerkung", feature.getProperty("bemerkung"));

        if (basisStat != null) {
            map.put("basis", feature.getProperty("ba_cd"));
            map.put("basis_stat", ConversionUtils.numberToString(basisStat.getProperty("wert")));
        } else {
            map.put("basis", "");
            map.put("basis_stat", "");
        }
        if (freigabe != null) {
            map.put("freigabe", freigabe.toString());
        } else {
            map.put("freigabe", "");
        }
        map.put("isUploader", hasPermissionforBemerkung);

        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GafProf.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/gafProf.jasper"));

        return JasperFillManager.fillReport(jasperReport, map, ds);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   date  o DOCUMENT ME!
     * @param   time  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String dateTime2String(final Object date, final String time) {
        if ((date == null) && (time == null)) {
            return "";
        } else if (date == null) {
            return time;
        } else if (time == null) {
            try {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                return format.format(date);
            } catch (IllegalArgumentException e) {
                LOG.error("Not a date", e);
                return "";
            }
        } else {
            try {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
                return format.format(date) + " " + time;
            } catch (IllegalArgumentException e) {
                LOG.error("Not a date", e);
                return "";
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static void addGafServicesToTree() {
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                "qp");

        if ((services == null) || services.isEmpty()) {
            final CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                        AppBroker.DOMAIN_NAME,
                        "dlm25w.qp"));
            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(layer);
            AttributeTableFactory.getInstance().switchProcessingMode(layer);
        } else {
            if (!SelectionManager.getInstance().getEditableServices().contains(services.get(0))) {
                AttributeTableFactory.getInstance().switchProcessingMode(services.get(0));
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static void closeEditMode() {
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                "qp");

        if (!(services == null) || services.isEmpty()) {
            if (SelectionManager.getInstance().getEditableServices().contains(services.get(0))) {
                AttributeTableFactory.getInstance().switchProcessingMode(services.get(0));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  layerName  DOCUMENT ME!
     */
    private static void addLayerToTree(final String layerName) {
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                layerName);

        if ((services == null) || services.isEmpty()) {
            final CidsLayer l = new CidsLayer(ClassCacheMultiple.getMetaClass(
                        AppBroker.DOMAIN_NAME,
                        "dlm25w."
                                + layerName));
            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(l);
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static void reloadGafProfileServices() {
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                "qp");

        for (final AbstractFeatureService featureService : services) {
            featureService.retrieve(true);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butPrevProfileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butPrevProfileActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures("qp");

        if ((features != null) && !features.isEmpty()) {
            Collections.sort(features, PhotoOptionsDialog.getInstance().getSorter());
            int index = features.indexOf(feature);
            --index;

            if (index >= 0) {
                setEditorFeature((CidsLayerFeature)features.get(index));
            }
            butNextProfile.setEnabled(index < (features.size() - 1));
            butPrevProfile.setEnabled(index > 0);
        } else {
            butNextProfile.setEnabled(false);
            butPrevProfile.setEnabled(false);
        }
    } //GEN-LAST:event_butPrevProfileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butNextProfileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butNextProfileActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures("qp");

        if ((features != null) && !features.isEmpty()) {
            Collections.sort(features, PhotoOptionsDialog.getInstance().getSorter());
            int index = features.indexOf(feature);
            ++index;

            if (index < features.size()) {
                setEditorFeature((CidsLayerFeature)features.get(index));
            }

            butNextProfile.setEnabled(index < (features.size() - 1));
            butPrevProfile.setEnabled(index > 0);
        } else {
            butNextProfile.setEnabled(false);
            butPrevProfile.setEnabled(false);
        }
    } //GEN-LAST:event_butNextProfileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbProcessingActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tbProcessingActionPerformed
    }                                                                                //GEN-LAST:event_tbProcessingActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butBackActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butBackActionPerformed
        AppBroker.getInstance().getMappingComponent().back(true);
    }                                                                           //GEN-LAST:event_butBackActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butSaveActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butSaveActionPerformed
        try {
            final CidsLayerFeature feature = editor.getCidsLayerFeature();
            final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();

            if (ruleSet != null) {
                ruleSet.beforeSave(feature);
            }
            feature.saveChangesWithoutReload();
            reloadGafProfileServices();
        } catch (Exception e) {
            LOG.error("Eror while saving feature", e);
        }
    } //GEN-LAST:event_butSaveActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butRemoveSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butRemoveSelectionActionPerformed
        SelectionManager.getInstance().removeSelectedFeatures(editor.getCidsLayerFeature());
    }                                                                                      //GEN-LAST:event_butRemoveSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butZoomToProfileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butZoomToProfileActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();
        final MappingComponent mappingComponent = CismapBroker.getInstance().getMappingComponent();

        if (mappingComponent != null) {
            final XBoundingBox bbox = new XBoundingBox(feature.getGeometry());
            bbox.increase(10);
            final BoundingBox scaledBBox = mappingComponent.getScaledBoundingBox(1000, bbox);
            mappingComponent.gotoBoundingBoxWithHistory(scaledBBox);
        } else {
            LOG.error("MappingComponent is not set");
        }
    } //GEN-LAST:event_butZoomToProfileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butDeleteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butDeleteActionPerformed
        final CidsLayerFeature feature = editor.getCidsLayerFeature();

        if (!feature.hasWritePermissions()) {
            return;
        }

        final int ans = JOptionPane.showConfirmDialog(
                GafProf.this,
                NbBundle.getMessage(GafProf.class,
                    "GafProf.butDeleteActionPerformed().text"),
                NbBundle.getMessage(GafProf.class, "GafProf.butDeleteActionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans != JOptionPane.YES_OPTION) {
            return;
        }

        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(StaticSwingTools.getParentFrame(this),
                true,
                NbBundle.getMessage(GafProf.class,
                    "GafProf.butDeleteActionPerformed.WaitingDialogThread"),
                null,
                500) {

                @Override
                protected Void doInBackground() throws Exception {
                    deleteProfile(feature);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        reloadGafProfileServices();
                        askForSave = false;
                        butRemoveSelectionActionPerformed(evt);
                    } catch (Exception e) {
                        LOG.error("Error while deleting objects", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butDeleteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void tbLocateActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_tbLocateActionPerformed
        if (tbLocate.isSelected()) {
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
                feature.saveChangesWithoutReload();
                layerFeature.setEditable(false);
                reloadGafProfileServices();
            } catch (Exception e) {
                LOG.error("Error while setting the new geometry", e);
            }
        }
    } //GEN-LAST:event_tbLocateActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butSaveAllActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butSaveAllActionPerformed
        final WaitingDialogThread<Void> wdt = new WaitingDialogThread<Void>(AppBroker.getInstance().getWatergisApp(),
                true,
                NbBundle.getMessage(GafProf.class, "GafProf.butSaveAllActionPerformed"),
                null,
                100) {

                @Override
                protected Void doInBackground() throws Exception {
                    final CidsLayerFeature feature = editor.getCidsLayerFeature();
                    final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();
                    final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                            "qp");

                    if ((features != null) && !features.isEmpty()) {
                        for (final FeatureServiceFeature selectedF : features) {
                            if (selectedF.getId() != feature.getId()) {
                                selectedF.setProperty("aufn_name", feature.getProperty("aufn_name"));
                                selectedF.setProperty("aufn_datum", feature.getProperty("aufn_datum"));
                                selectedF.setProperty("aufn_zeit", feature.getProperty("aufn_zeit"));
                                selectedF.setProperty("titel", feature.getProperty("titel"));
                                selectedF.setProperty("beschreib", feature.getProperty("beschreib"));
                                selectedF.setProperty("bemerkung", feature.getProperty("bemerkung"));
                                selectedF.setProperty("l_st", feature.getBean().getProperty("l_st"));
                                selectedF.setProperty("freigabe", feature.getBean().getProperty("freigabe"));

                                if (selectedF instanceof ModifiableFeature) {
                                    ((ModifiableFeature)selectedF).saveChangesWithoutReload();
                                }
                            }
                        }
                    }

                    if (ruleSet != null) {
                        ruleSet.beforeSave(feature);
                    }
                    feature.saveChangesWithoutReload();

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        reloadGafProfileServices();
                    } catch (Exception e) {
                        LOG.error("Error apply changes for all selected profiles", e);
                    }
                }
            };

        wdt.start();
    } //GEN-LAST:event_butSaveAllActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void setEditorFeature(final CidsLayerFeature feature) {
//        if ((selectedFeature != null) && askForSave && editor.hasWriteAccess()) {
//            if (selectedFeature.getBean().getMetaObject().isChanged()
//                        && (selectedFeature.getBean().getMetaObject().getStatus()
//                            != Sirius.server.localserver.object.Object.TO_DELETE)) {
//                final int ans = JOptionPane.showConfirmDialog(
//                        GafProf.this,
//                        NbBundle.getMessage(GafProf.class,
//                            "GafProf.setEditorFeature().text"),
//                        NbBundle.getMessage(GafProf.class, "GafProf.setEditorFeature().title"),
//                        JOptionPane.YES_NO_CANCEL_OPTION);
//
//                if (ans == JOptionPane.YES_OPTION) {
//                    butSaveActionPerformed(null);
//                } else if (ans == JOptionPane.NO_OPTION) {
//                    // nothing to do
//                } else {
//                    return;
//                }
//            }
//        }

        askForSave = true;

        if (tbLocate.isSelected()) {
            tbLocate.setSelected(false);
            tbLocateActionPerformed(null);
        }

        if ((feature != null) && !feature.isEditable()) {
            makeFeatureEditable(feature);
        }

        editor.setCidsLayerFeature(feature);
        enableToolbar(feature != null);
        final CidsLayerFeature formerSelectedFeature = selectedFeature;
        selectedFeature = feature;

        refreshFeatureVisualisation(formerSelectedFeature);
        refreshFeatureVisualisation(selectedFeature);

        if (feature != null) {
            butDelete.setEnabled(editor.hasWriteAccess());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    private void refreshFeatureVisualisation(final CidsLayerFeature feature) {
        if (feature != null) {
            final AbstractFeatureService service = feature.getLayerProperties().getFeatureService();
            if (service != null) {
                final List<PFeature> pfeatureList = service.getPNode().getChildrenReference();

                for (final PFeature pf : pfeatureList) {
                    final Feature f = pf.getFeature();

                    if (f instanceof FeatureServiceFeature) {
                        if (((FeatureServiceFeature)f).getId() == feature.getId()) {
                            pf.visualize();
                            pf.refreshDesign();
                        }
                        final PFeature mapFeature = pf.getViewer().getPFeatureHM().get(feature);

                        if (mapFeature != null) {
                            mapFeature.visualize();
                            mapFeature.refreshDesign();
                        }
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  enabled  DOCUMENT ME!
     */
    private void enableToolbar(final boolean enabled) {
        final boolean writable = enabled && editor.hasWriteAccess();
        butBack.setEnabled(enabled);
        butNextProfile.setEnabled(enabled);
        butPrevProfile.setEnabled(enabled);
        butPrint.setEnabled(enabled);
        butPrintPreview.setEnabled(enabled);
        butRemoveSelection.setEnabled(enabled);
        butZoomToProfile.setEnabled(enabled);

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

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        selectedFeature = null;
        editor.dispose();
        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                "qp");

        if ((services != null) && !services.isEmpty()) {
            if (SelectionManager.getInstance().getEditableServices().contains(services.get(0))) {
                AttributeTableFactory.getInstance().switchProcessingMode(services.get(0));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature     DOCUMENT ME!
     * @param   newGafBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void tryStationCreation(final CidsLayerFeature feature, final CidsBean newGafBean) throws Exception {
        final Station stat = getNextFgBaStat(feature.getGeometry(), 1);

        if (stat != null) {
            final MetaClass routeMc = ClassCacheMultiple.getMetaClass(
                    AppBroker.DOMAIN_NAME,
                    "dlm25w.fg_ba");
            final CidsBean stationBean = CidsBeanUtils.createNewCidsBeanFromTableName(
                    "dlm25w.fg_ba_punkt");
            final CidsBean stationGeom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
            final MetaObject route = SessionManager.getProxy()
                        .getMetaObject(stat.getId(), routeMc.getID(), AppBroker.DOMAIN_NAME);

            stationGeom.setProperty("geo_field", stat.getPoint());

            stationBean.setProperty("wert", stat.getStat());
            stationBean.setProperty("route", route.getBean());
            stationBean.setProperty("real_point", stationGeom);

            newGafBean.setProperty("ba_st", stationBean);
            feature.setProperty("ba_cd", stat.getBaCd());

            final Station laStat = getNextFgLaStat(stat.getPoint());

            if (laStat != null) {
                feature.setProperty("la_cd", laStat.getLaCd());
                feature.setProperty("la_st", laStat.getStat());
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom     DOCUMENT ME!
     * @param   maxDist  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Station getNextFgBaStat(final Geometry geom, final double maxDist) {
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
    private static Station getNextFgLaStat(final Geometry geom) {
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
                stat.setLaCd((BigDecimal)attributes.get(0).get(1));
                stat.setStat((Double)attributes.get(0).get(2));
//                    stat.setPoint(wkbReader.read((byte[])attributes.get(0).get(3)));
                return stat;
            }
        } catch (Exception ex) {
            LOG.error("Errro while retrieving next fg ba station.", ex);
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class GafUploadWorker extends WaitingDialogThread<List<CidsLayerFeature>> {

        //~ Instance fields ----------------------------------------------------

        private final Collection<File> profs;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageUploadWorker object.
         *
         * @param  profs  fotos DOCUMENT ME!
         */
        public GafUploadWorker(final Collection<File> profs) {
            super(StaticSwingTools.getParentFrame(GafProf.this), true, "Importiere Profil", null, 50);
            this.profs = profs;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected List<CidsLayerFeature> doInBackground() throws Exception {
            final List<CidsLayerFeature> newBeans = new ArrayList<CidsLayerFeature>();
            boolean dhhn92Check = false;
            boolean dhhn92 = false;
            final Iterator<File> it = profs.iterator();
            final List<File> possibleCatalogueFiles = new ArrayList<File>();
            final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                    "qp");
            CidsLayer layer = null;
            final Date currentTime = new Date();

            if ((services != null) && !services.isEmpty()) {
                layer = (CidsLayer)services.get(0);
            } else {
                layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                            AppBroker.DOMAIN_NAME,
                            "dlm25w.qp"));
            }

            while (it.hasNext()) {
                final File f = it.next();

                if (!f.getName().toLowerCase().endsWith(".gaf") && !f.getName().toLowerCase().endsWith(".csv")
                            && !f.getName().toLowerCase().endsWith(".txt")) {
                    possibleCatalogueFiles.add(f);
                    it.remove();
                }
            }

            for (final File gafFile : profs) {
                final String userName = SessionManager.getSession().getUser().getName();
                final ProfileReader reader = ProfileReaderFactory.getReader(gafFile);
                boolean hasBkcat = false;
                boolean hasRkcat = false;

                final AbstractImportDialog dialog = reader.getImportDialog(StaticSwingTools.getParentFrame(
                            GafProf.this));

                if (dialog != null) {
                    dialog.setAlwaysOnTop(true);
                    StaticSwingTools.showDialog(dialog);

                    if (dialog.isCancelled()) {
                        continue;
                    }
                }

                // add custom catalogues
                for (final File catalogue : possibleCatalogueFiles) {
                    try {
                        final CustomGafCatalogueReader.FILE_TYPE catType = reader.addCustomCatalogue(catalogue);

                        if (catType.equals(CustomGafCatalogueReader.FILE_TYPE.BK)) {
                            if (hasBkcat) {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        GafUploadWorker.class,
                                        "GafProf.ImageUploadWorker.doInBackground().multiBkCat.message"),
                                    NbBundle.getMessage(
                                        GafUploadWorker.class,
                                        "GafProf.ImageUploadWorker.doInBackground().multiBkCat.title"),
                                    JOptionPane.ERROR_MESSAGE);

                                return null;
                            } else {
                                hasBkcat = true;
                            }
                        } else if (catType.equals(CustomGafCatalogueReader.FILE_TYPE.RK)) {
                            if (hasRkcat) {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        GafUploadWorker.class,
                                        "GafProf.ImageUploadWorker.doInBackground().multiRkCat.message"),
                                    NbBundle.getMessage(
                                        GafUploadWorker.class,
                                        "GafProf.ImageUploadWorker.doInBackground().multiRkCat.title"),
                                    JOptionPane.ERROR_MESSAGE);

                                return null;
                            } else {
                                hasRkcat = true;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(
                                GafUploadWorker.class,
                                "GafProf.ImageUploadWorker.doInBackground().unknownCat.message",
                                e.getMessage()),
                            NbBundle.getMessage(
                                GafUploadWorker.class,
                                "GafProf.ImageUploadWorker.doInBackground().unknownCat.title"),
                            JOptionPane.ERROR_MESSAGE);

                        return null;
                    }
                }

                // check the gaf file
                final String[] errors = reader.checkFile();

                if ((errors != null) && (errors.length > 0)) {
                    CheckAction.handleErrors(errors, gafFile);
                    return null;
                }

//                final String[] hints = reader.checkFileForHints();
//
//                if ((hints != null) && (hints.length > 0)) {
//                    CheckAction.handleHints(hints, gafFile);
//                }

                if (!dhhn92Check) {
                    StaticSwingTools.showDialog(GafImportDialog.getInstance());
                    GafImportDialog.getInstance().setAlwaysOnTop(true);
                    dhhn92Check = true;

                    dhhn92 = !GafImportDialog.getInstance().isCancelled();
                }

                if (!dhhn92) {
                    continue;
                }

                for (final Double profile : reader.getProfiles()) {
                    final Map<String, Object> props = new HashMap<String, Object>();
                    final CidsBean newGafBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.qp");
                    final int id = getNewId();
                    for (final String propName : newGafBean.getPropertyNames()) {
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

                    newGafBean.setProperty("id", id);
                    newGafBean.setProperty("upl_datum", new java.sql.Date(currentTime.getTime()));
                    newGafBean.setProperty("upl_zeit", timeFormatter.format(currentTime.getTime()));
                    newGafBean.setProperty("upl_name", userName);
                    newGafBean.setProperty("qp_nr", getNextProfileNumber());
                    newGafBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());

                    Geometry intersectionPoint = getIntersectionPoint(reader.getNpLine(profile));

                    if (intersectionPoint == null) {
                        intersectionPoint = reader.getProfilePoint(profile);
                    }

                    if (intersectionPoint != null) {
                        final CidsBean geom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");

                        geom.setProperty("geo_field", intersectionPoint);
                        newGafBean.setProperty("geom", geom);
                        feature.setGeometry(intersectionPoint);

                        final CidsBean statusBean = getCatalogueElement("dlm25w.k_l_st", "l_st", "V-Bau");

                        if (statusBean != null) {
                            newGafBean.setProperty("l_st", statusBean);
                        }

                        tryStationCreation(feature, newGafBean);
                    } else {
                    }

                    final CidsBean freigabeBean = getCatalogueElement("dlm25w.k_freigabe", "name", "alle Nutzer");
                    if (freigabeBean != null) {
                        newGafBean.setProperty("freigabe", freigabeBean);
                    }

                    feature.setMetaObject(newGafBean.getMetaObject());

                    final AttributeTableRuleSet ruleSet = feature.getLayerProperties().getAttributeTableRuleSet();

                    if (ruleSet != null) {
                        ruleSet.beforeSave(feature);
                    }

                    for (final ProfileLine line : reader.getProfileContent(profile)) {
                        final CidsBean ppBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.qp_gaf_pp");
                        final CidsBean geom = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                        final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                CismapBroker.getInstance().getDefaultCrsAlias());
                        final Double rw = (Double)reader.getProfileContent(GafReader.GAF_FIELDS.RW, line);
                        final Double hw = (Double)reader.getProfileContent(GafReader.GAF_FIELDS.HW, line);

                        geom.setProperty("geo_field", gf.createPoint(new Coordinate(rw, hw)));

                        ppBean.setProperty("geom", geom);
                        ppBean.setProperty("id_gaf", reader.getProfileContent(GafReader.GAF_FIELDS.ID, line));
                        ppBean.setProperty("y", reader.getProfileContent(GafReader.GAF_FIELDS.Y, line));
                        ppBean.setProperty("z", reader.getProfileContent(GafReader.GAF_FIELDS.Z, line));
                        ppBean.setProperty("kz", reader.getProfileContent(GafReader.GAF_FIELDS.KZ, line));
                        ppBean.setProperty("hyk", reader.getProfileContent(GafReader.GAF_FIELDS.HYK, line));
                        ppBean.setProperty("hw", hw);
                        ppBean.setProperty("rw", rw);
                        ppBean.setProperty("fis_g_date", new Timestamp(System.currentTimeMillis()));
                        ppBean.setProperty("fis_g_user", SessionManager.getSession().getUser().getName());
                        ppBean.setProperty("qp_nr", newGafBean.getProperty("qp_nr"));
                        ppBean.setProperty("p_nr", getNextProfileNumber());

                        Object obj = reader.getProfileContent(GafReader.GAF_FIELDS.RK, line);

                        if (obj instanceof CustomGafCatalogueReader.RkObject) {
                            final CustomGafCatalogueReader.RkObject rkObj = (CustomGafCatalogueReader.RkObject)obj;
                            ppBean.setProperty("rk", null);
                            ppBean.setProperty("rk_name", rkObj.getName());
                            ppBean.setProperty("rk_k", rkObj.getK());
                            ppBean.setProperty("rk_kst", rkObj.getKst());
                        } else {
                            ppBean.setProperty("rk", obj);
                        }

                        obj = reader.getProfileContent(GafReader.GAF_FIELDS.BK, line);

                        if (obj instanceof CustomGafCatalogueReader.BkObject) {
                            final CustomGafCatalogueReader.BkObject bkObj = (CustomGafCatalogueReader.BkObject)obj;
                            ppBean.setProperty("bk", null);
                            ppBean.setProperty("bk_name", bkObj.getName());
                            ppBean.setProperty("bk_ax", bkObj.getAx());
                            ppBean.setProperty("bk_ay", bkObj.getAy());
                            ppBean.setProperty("bk_dp", bkObj.getDp());
                        } else {
                            ppBean.setProperty("bk", obj);
                        }

                        ppBean.persist();
                    }

                    final CidsLayerFeature newFeature = (CidsLayerFeature)feature.saveChanges();
                    newBeans.add(newFeature);
                }
            }
            addFeatures(newBeans);
            return newBeans;
        }

        @Override
        protected void done() {
            try {
                final List<CidsLayerFeature> newFeatures = get();

                if ((newFeatures != null) && !newFeatures.isEmpty()) {
                    final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                            "qp");
                    SelectionManager.getInstance().removeSelectedFeatures(features);
                    SelectionManager.getInstance().addSelectedFeatures(newFeatures);
                    GafProf.this.newFeatures.addAll(newFeatures);

                    reloadGafProfileServices();
                }
            } catch (Exception ex) {
                LOG.warn(ex, ex);
            } finally {
                if (editor.getCidsLayerFeature() == null) {
                    editor.showEditor(false, false);
                } else {
                    editor.showEditor(true, false);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param  features  DOCUMENT ME!
         */
        private void addFeatures(final List<? extends FeatureServiceFeature> features) {
            final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                    "qp");

            if ((services != null) && !services.isEmpty()) {
                final AttributeTable tablePf = AppBroker.getInstance()
                            .getWatergisApp()
                            .getAttributeTableByFeatureService(services.get(0));

                if (tablePf != null) {
                    for (final FeatureServiceFeature f : features) {
                        tablePf.addFeature(f);
                    }
                }
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
        private Integer getNextProfileNumber() {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new PhotoGetPhotoNumber());

                if ((attributes != null) && !attributes.isEmpty()) {
                    return ((Long)attributes.get(0).get(0)).intValue();
                }
            } catch (Exception ex) {
                LOG.error("Errro while retrieving next profile number.", ex);
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
        private Geometry getIntersectionPoint(final Geometry geom) {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new GafPosition(geom, 1));

                if ((attributes != null) && !attributes.isEmpty()) {
                    final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CismapBroker.getInstance().getDefaultCrsAlias());
                    final WKBReader wkbReader = new WKBReader(geomFactory);

                    return wkbReader.read((byte[])attributes.get(0).get(0));
                }
            } catch (Exception ex) {
                LOG.error("Errro while retrieving gaf profile position.", ex);
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class Station {

        //~ Instance fields ----------------------------------------------------

        private int id;
        private String baCd;
        private BigDecimal laCd;
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

        /**
         * DOCUMENT ME!
         *
         * @return  the laCd
         */
        public BigDecimal getLaCd() {
            return laCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  laCd  the laCd to set
         */
        public void setLaCd(final BigDecimal laCd) {
            this.laCd = laCd;
        }
    }
}
