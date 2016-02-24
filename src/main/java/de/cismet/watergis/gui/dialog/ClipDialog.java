/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui.dialog;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;
import de.cismet.watergis.utils.GeometryUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ClipDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ClipDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;
    private int selectedTargetThemeFeatureCount = 0;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgBuffer;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbTargetTheme;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbMultiPart;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JCheckBox ckbSelectedTarget;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labSelected;
    private javax.swing.JLabel labSelectedTarget;
    private javax.swing.JLabel labTableName;
    private javax.swing.JLabel labTargetTheme;
    private javax.swing.JLabel labTheme;
    private javax.swing.JTextField txtTable;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ClipDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public ClipDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        cbTheme.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final String name;

                    if (value instanceof String) {
                        name = (String)value;
                    } else {
                        name = ((value != null) ? ((AbstractFeatureService)value).getName() : " ");
                    }
                    return super.getListCellRendererComponent(
                            list,
                            name,
                            index,
                            isSelected,
                            cellHasFocus);
                }
            });

        cbTargetTheme.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final String name;

                    if (value instanceof String) {
                        name = (String)value;
                    } else {
                        name = ((value != null) ? ((AbstractFeatureService)value).getName() : " ");
                    }
                    return super.getListCellRendererComponent(
                            list,
                            name,
                            index,
                            isSelected,
                            cellHasFocus);
                }
            });

        txtTable.setText("Ausschnitt");
        CismapBroker.getInstance()
                .getMappingComponent()
                .getFeatureCollection()
                .addFeatureCollectionListener(new FeatureCollectionListener() {

                        @Override
                        public void featuresAdded(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featuresRemoved(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featuresChanged(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featureSelectionChanged(final FeatureCollectionEvent fce) {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        final AbstractFeatureService service = (AbstractFeatureService)
                                            cbTheme.getSelectedItem();
                                        selectedThemeFeatureCount = refreshSelectedFeatureCount(
                                                false,
                                                ckbSelected,
                                                service,
                                                selectedThemeFeatureCount,
                                                labSelected);
                                        final AbstractFeatureService targetService = (AbstractFeatureService)
                                            cbTargetTheme.getSelectedItem();
                                        selectedTargetThemeFeatureCount = refreshSelectedFeatureCount(
                                                false,
                                                ckbSelectedTarget,
                                                targetService,
                                                selectedTargetThemeFeatureCount,
                                                labSelectedTarget);
                                    }
                                });
                        }

                        @Override
                        public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
                        }

                        @Override
                        public void featureCollectionChanged() {
                        }
                    });

        final ActiveLayerModel layerModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        layerModel.addTreeModelWithoutProgressListener(new TreeModelListener() {

                @Override
                public void treeNodesChanged(final TreeModelEvent e) {
                    setLayerModel();
                }

                @Override
                public void treeNodesInserted(final TreeModelEvent e) {
                    setLayerModel();
                }

                @Override
                public void treeNodesRemoved(final TreeModelEvent e) {
                    setLayerModel();
                }

                @Override
                public void treeStructureChanged(final TreeModelEvent e) {
                    setLayerModel();
                }
            });

        setLayerModel();
        enabledOrNot();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgBuffer = new javax.swing.ButtonGroup();
        jDialog1 = new javax.swing.JDialog();
        labTheme = new javax.swing.JLabel();
        cbTheme = new javax.swing.JComboBox();
        labTableName = new javax.swing.JLabel();
        txtTable = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        labTargetTheme = new javax.swing.JLabel();
        cbTargetTheme = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        ckbSelectedTarget = new javax.swing.JCheckBox();
        labSelectedTarget = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();
        ckbMultiPart = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.labTheme.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
        getContentPane().add(labTheme, gridBagConstraints);

        cbTheme.setMinimumSize(new java.awt.Dimension(200, 27));
        cbTheme.setPreferredSize(new java.awt.Dimension(200, 27));
        cbTheme.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbThemeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 5, 10);
        getContentPane().add(cbTheme, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableName,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.labTableName.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labTableName, gridBagConstraints);

        txtTable.setMinimumSize(new java.awt.Dimension(200, 27));
        txtTable.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(txtTable, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(89, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.butCancel.text", new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butCancel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTargetTheme,
            org.openide.util.NbBundle.getMessage(
                ClipDialog.class,
                "ClipDialog.labTargetTheme.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labTargetTheme, gridBagConstraints);

        cbTargetTheme.setMinimumSize(new java.awt.Dimension(200, 27));
        cbTargetTheme.setPreferredSize(new java.awt.Dimension(200, 27));
        cbTargetTheme.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbTargetThemeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(cbTargetTheme, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelectedTarget,
            org.openide.util.NbBundle.getMessage(
                ClipDialog.class,
                "ClipDialog.ckbSelectedTarget.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(ckbSelectedTarget, gridBagConstraints);

        labSelectedTarget.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel3.add(labSelectedTarget, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jPanel3, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelected,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.ckbSelected.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(ckbSelected, gridBagConstraints);

        labSelected.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(labSelected, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jPanel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbMultiPart,
            org.openide.util.NbBundle.getMessage(ClipDialog.class, "ClipDialog.ckbMultiPart.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbMultiPart, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        cbTheme.setModel(new DefaultComboBoxModel(
                new String[] { NbBundle.getMessage(ClipDialog.class,
                        "ClipDialog.setlayerModel.searchServices") }));
        cbTargetTheme.setModel(new DefaultComboBoxModel(
                new String[] {
                    NbBundle.getMessage(ClipDialog.class,
                        "ClipDialog.setlayerModel.searchPolygonServices")
                }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(null).toArray(
                                    new AbstractFeatureService[0])));
                        cbTargetTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "Polygon", "MultiPolygon" }).toArray(
                                    new AbstractFeatureService[0])));

                        if (cbTheme.getModel().getSize() > 0) {
                            cbTheme.setSelectedIndex(0);
                        } else {
                            cbTheme.setSelectedItem(null);
                        }

                        if (cbTargetTheme.getModel().getSize() > 0) {
                            cbTargetTheme.setSelectedIndex(0);
                        } else {
                            cbTargetTheme.setSelectedItem(null);
                        }
                    }
                });

        t.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        final AbstractFeatureService targetService = (AbstractFeatureService)cbTargetTheme.getSelectedItem();
        final String tableName = txtTable.getText();
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                        .getInstance().getWatergisApp(),
                true,
                "Ausschneiden                                            ",
                null,
                100,
                true) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    // retrieve Features
                    int progress = 10;
                    wd.setText(NbBundle.getMessage(
                            ClipDialog.class,
                            "ClipDialog.butOkActionPerformed.doInBackground.retrieving"));
                    wd.setMax(100);
                    wd.setProgress(5);
                    if (Thread.interrupted()) {
                        return null;
                    }
                    final List<FeatureServiceFeature> featureList = FeatureServiceHelper.getFeatures(
                            service,
                            ckbSelected.isSelected());
                    wd.setProgress(10);
                    if (Thread.interrupted()) {
                        return null;
                    }
                    final List<FeatureServiceFeature> targetFeatureList = FeatureServiceHelper.getFeatures(
                            targetService,
                            ckbSelectedTarget.isSelected());

                    // initialise variables for the geoperation
                    final STRtree featureTree = FeatureServiceHelper.getFeatureTree(targetFeatureList);
                    final List<FeatureServiceFeature> resultedFeatures = new ArrayList<FeatureServiceFeature>();
                    int count = 0;
                    int featureCount = 0;

                    wd.setText(NbBundle.getMessage(
                            ClipDialog.class,
                            "ClipDialog.butOkActionPerformed.doInBackground.createFeatures"));

                    for (final FeatureServiceFeature f : featureList) {
                        Geometry clipGeometry = null;
                        ++count;

                        final Geometry searchGeom = f.getGeometry();

                        final List<FeatureServiceFeature> intersectingFeatures = featureTree.query(
                                searchGeom.getEnvelopeInternal());

                        if ((intersectingFeatures != null) && !intersectingFeatures.isEmpty()) {
                            clipGeometry = GeometryUtils.unionFeatureGeometries(intersectingFeatures);
                        }

                        if (clipGeometry != null) {
                            if (f.getGeometry() != null) {
                                final Geometry newGeom = f.getGeometry().intersection(clipGeometry);

                                if ((newGeom != null) && !newGeom.isEmpty()) {
                                    if (ckbMultiPart.isSelected()) {
                                        f.setGeometry(newGeom);
                                        resultedFeatures.add(f);
                                    } else {
                                        for (int geomIndex = 0; geomIndex < newGeom.getNumGeometries(); ++geomIndex) {
                                            final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                            newFeature.setGeometry(newGeom.getGeometryN(geomIndex));
                                            newFeature.setProperty("id", ++featureCount);
                                            resultedFeatures.add(newFeature);
                                        }
                                    }
                                }
                            }
                        }

                        if (Thread.interrupted()) {
                            return null;
                        }
                        if (progress < (10 + (count * 80 / featureList.size()))) {
                            progress = 10 + (count * 80 / featureList.size());
                            wd.setProgress(progress);
                        }
                    }

                    // create the service
                    wd.setText(NbBundle.getMessage(
                            ClipDialog.class,
                            "ClipDialog.butOkActionPerformed.doInBackground.creatingDatasource"));

                    final List<String> orderedAttributeNames = new ArrayList();
                    orderedAttributeNames.addAll(service.getOrderedFeatureServiceAttributes());

                    return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                            resultedFeatures,
                            tableName,
                            orderedAttributeNames);
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        if (service != null) {
                            FeatureServiceHelper.addServiceLayerToTheTree(service);
                        }
                    } catch (Exception ex) {
                        LOG.error("Error while execute the Clip operation.", ex);
                    }
                }
            };

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    ClipDialog.class,
                    "ClipDialog.butOkActionPerformed.tableAlreadyExists",
                    tableName),
                NbBundle.getMessage(
                    ClipDialog.class,
                    "ClipDialog.butOkActionPerformed.tableAlreadyExists.title"),
                JOptionPane.ERROR_MESSAGE);
        } else {
            this.setVisible(false);
            wdt.start();
        }
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbThemeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbThemeActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        selectedThemeFeatureCount = refreshSelectedFeatureCount(
                false,
                ckbSelected,
                service,
                selectedThemeFeatureCount,
                labSelected);
        enabledOrNot();
    }                                                                           //GEN-LAST:event_cbThemeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbTargetThemeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbTargetThemeActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTargetTheme.getSelectedItem();
        selectedTargetThemeFeatureCount = refreshSelectedFeatureCount(
                false,
                ckbSelectedTarget,
                service,
                selectedTargetThemeFeatureCount,
                labSelectedTarget);
        enabledOrNot();
    }                                                                                 //GEN-LAST:event_cbTargetThemeActionPerformed

    /**
     * refreshes the labSelectedFeatures label.
     *
     * @param   forceGuiRefresh           DOCUMENT ME!
     * @param   box                       DOCUMENT ME!
     * @param   featureService            DOCUMENT ME!
     * @param   lastSelectedFeatureCount  DOCUMENT ME!
     * @param   selectionCountlab         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int refreshSelectedFeatureCount(final boolean forceGuiRefresh,
            final JCheckBox box,
            final AbstractFeatureService featureService,
            final int lastSelectedFeatureCount,
            final JLabel selectionCountlab) {
        final int count = ((featureService == null) ? 0
                                                    : FeatureServiceHelper.getSelectedFeatures(featureService).size());

        selectionCountlab.setText(NbBundle.getMessage(
                ClipDialog.class,
                "ClipDialog.refreshSelectedFeatureCount.text",
                count));

        if (forceGuiRefresh || (count != lastSelectedFeatureCount)) {
            box.setSelected(count > 0);
        }
        return count;
    }

    /**
     * DOCUMENT ME!
     */
    private void enabledOrNot() {
        final boolean isServiceSelected = (cbTheme.getSelectedItem() instanceof AbstractFeatureService)
                    && (cbTargetTheme.getSelectedItem() instanceof AbstractFeatureService);

        butOk.setEnabled(isServiceSelected);
    }
}
