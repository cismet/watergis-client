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
package de.cismet.watergis.gui.dialog;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import de.cismet.cids.editors.DefaultBindableReferenceCombo;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class BufferDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(BufferDialog.class);

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgBuffer;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbField;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbIndividual;
    private javax.swing.JCheckBox ckbMergeBuffer;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel labTableName;
    private javax.swing.JLabel labTheme;
    private javax.swing.JRadioButton rbFieldBuffer;
    private javax.swing.JRadioButton rbFixBuffer;
    private javax.swing.JTextField txtBuffer;
    private javax.swing.JTextField txtTable;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public BufferDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        cbTheme.setModel(new DefaultComboBoxModel(getServices().toArray(new AbstractFeatureService[0])));
        cbTheme.setSelectedItem(null);
        cbTheme.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    if (value != null) {
                        return super.getListCellRendererComponent(
                                list,
                                ((AbstractFeatureService)value).getName(),
                                index,
                                isSelected,
                                cellHasFocus);
                    } else {
                        return super.getListCellRendererComponent(
                                list,
                                " ",
                                index,
                                isSelected,
                                cellHasFocus);
                    }
                }
            });

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
        ckbSelected = new javax.swing.JCheckBox();
        ckbIndividual = new javax.swing.JCheckBox();
        rbFixBuffer = new javax.swing.JRadioButton();
        rbFieldBuffer = new javax.swing.JRadioButton();
        txtBuffer = new javax.swing.JTextField();
        cbField = new javax.swing.JComboBox();
        ckbMergeBuffer = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(labTheme, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.labTheme.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(labTheme, gridBagConstraints);

        cbTheme.setMinimumSize(new java.awt.Dimension(200, 27));
        cbTheme.setPreferredSize(new java.awt.Dimension(200, 27));
        cbTheme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbThemeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(cbTheme, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(labTableName, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.labTableName.text", new Object[] {})); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(ckbSelected, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.ckbSelected.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbSelected, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ckbIndividual, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.ckbIndividual.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbIndividual, gridBagConstraints);

        bgBuffer.add(rbFixBuffer);
        rbFixBuffer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbFixBuffer, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.rbFixBuffer.text", new Object[] {})); // NOI18N
        rbFixBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFixBufferActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(rbFixBuffer, gridBagConstraints);

        bgBuffer.add(rbFieldBuffer);
        org.openide.awt.Mnemonics.setLocalizedText(rbFieldBuffer, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.rbFieldBuffer.text", new Object[] {})); // NOI18N
        rbFieldBuffer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbFieldBufferActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(rbFieldBuffer, gridBagConstraints);

        txtBuffer.setMinimumSize(new java.awt.Dimension(200, 27));
        txtBuffer.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(txtBuffer, gridBagConstraints);

        cbField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbField.setMinimumSize(new java.awt.Dimension(200, 27));
        cbField.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(cbField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(ckbMergeBuffer, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.ckbMergeBuffer.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbMergeBuffer, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(butOk, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(80, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butOkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(butCancel, org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.butCancel.text", new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butCancelActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<AbstractFeatureService> getServices() {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        serviceList.add(null);
        final ActiveLayerModel mappingModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap treeMap = mappingModel.getMapServices();
        for (final Iterator it = treeMap.keySet().iterator(); it.hasNext();) {
            final Object service = treeMap.get(it.next());
            if (service instanceof AbstractFeatureService) {
                serviceList.add((AbstractFeatureService)service);
            }
        }

        return serviceList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOkActionPerformed
//        final FeatureFactory factory = service.getFeatureFactory();
//        final Object serviceQuery = service.getQuery();
//        final String propertyName = cbTheme.getSelectedItem().toString();
//        final String tableName = txtTable.getText();
//        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
//                        .getInstance().getWatergisApp(),
//                true,
//                "Dissolve",
//                null,
//                100) {
//
//                @Override
//                protected H2FeatureService doInBackground() throws Exception {
//                    final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
//                    XBoundingBox bb = null;
//                    int progress = 0;
//                    wd.setText(NbBundle.getMessage(
//                            BufferDialog.class,
//                            "DissolveDialog.butOkActionPerformed.doInBackground.retrieving"));
//                    wd.setMax(100);
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("retrieve all features from the service");
//                    }
//
//                    if (g != null) {
//                        bb = new XBoundingBox(g);
//
//                        try {
//                            final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs()
//                                            .getCode());
//                            bb = transformer.transformBoundingBox(bb);
//                        } catch (Exception e) {
//                            LOG.error("Cannot transform CRS.", e);
//                        }
//                    }
//
//                    final List<FeatureServiceFeature> featureList = factory.createFeatures(
//                            serviceQuery,
//                            bb,
//                            null,
//                            0,
//                            0,
//                            null);
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("sort the features");
//                    }
//
//                    final Map<Object, List<FeatureServiceFeature>> featureMap =
//                        new HashMap<Object, List<FeatureServiceFeature>>();
//                    int n = 0;
//                    int featureCount = featureList.size();
//                    wd.setText(NbBundle.getMessage(
//                            BufferDialog.class,
//                            "DissolveDialog.butOkActionPerformed.doInBackground.sorting"));
//
//                    for (final FeatureServiceFeature f : featureList) {
//                        final Object propValue = f.getProperty(propertyName);
//
//                        List<FeatureServiceFeature> dissolvedFeature = featureMap.get(propValue);
//
//                        if (dissolvedFeature == null) {
//                            dissolvedFeature = new ArrayList<FeatureServiceFeature>();
//                            dissolvedFeature.add(f);
//                            featureMap.put(propValue, dissolvedFeature);
//                        } else {
//                            dissolvedFeature.add(f);
//                        }
//
//                        ++n;
//
//                        if ((progress) < (n * 20 / featureCount)) {
//                            progress = (n * 20 / featureCount);
//
//                            wd.setProgress(progress);
//                        }
//                    }
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("merge geometries");
//                    }
//                    wd.setText(NbBundle.getMessage(
//                            BufferDialog.class,
//                            "DissolveDialog.butOkActionPerformed.doInBackground.merging"));
//                    final List<FeatureServiceFeature> dissolvedFeatures = new ArrayList<FeatureServiceFeature>();
//                    featureCount = featureMap.keySet().size();
//                    n = 0;
//
//                    for (final Object key : featureMap.keySet()) {
//                        final List<FeatureServiceFeature> features = featureMap.get(key);
//                        final FeatureServiceFeature f = features.get(0);
//                        final List<Geometry> geomList = new ArrayList<Geometry>();
//
//                        for (final FeatureServiceFeature feature : features) {
//                            geomList.add(feature.getGeometry());
//                        }
//
//                        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
//                                geomList.get(0).getSRID());
//                        Geometry geom = factory.buildGeometry(geomList);
//
//                        if (geom instanceof GeometryCollection) {
//                            geom = ((GeometryCollection)geom).union();
//                        }
//
//                        f.setGeometry(geom);
//                        dissolvedFeatures.add(f);
//                        ++n;
//
//                        if ((progress) < (20 + (n * 55 / featureCount))) {
//                            progress = 20 + (n * 55 / featureCount);
//
//                            wd.setProgress(progress);
//                        }
//                    }
//
//                    wd.setText(NbBundle.getMessage(
//                            BufferDialog.class,
//                            "DissolveDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
//                    final H2FeatureService internalService = new H2FeatureService(
//                            tableName,
//                            H2FeatureServiceFactory.DB_NAME,
//                            tableName,
//                            null,
//                            null,
//                            dissolvedFeatures);
//                    if (LOG.isDebugEnabled()) {
//                        LOG.debug("create the new data source");
//                    }
//                    internalService.initAndWait();
//
//                    return internalService;
//                }
//
//                @Override
//                protected void done() {
//                    try {
//                        final H2FeatureService service = get();
//                        AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(service);
//                        final Component capComponent = AppBroker.getInstance().getComponent(ComponentName.CAPABILITIES);
//
//                        if (capComponent instanceof CapabilityWidget) {
//                            final CapabilityWidget cap = (CapabilityWidget)capComponent;
//                            cap.refreshJdbcTrees();
//                        }
//                    } catch (Exception ex) {
//                        LOG.error("Error while dissolving features.", ex);
//                    }
//                }
//            };
//
//        if (H2FeatureService.tableAlreadyExists(tableName)) {
//            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
//                NbBundle.getMessage(
//                    BufferDialog.class,
//                    "DissolveDialog.butOkActionPerformed.tableAlreadyExists",
//                    tableName),
//                NbBundle.getMessage(
//                    BufferDialog.class,
//                    "DissolveDialog.butOkActionPerformed.tableAlreadyExists.title"),
//                JOptionPane.ERROR_MESSAGE);
//        } else {
//            this.setVisible(false);
//            wdt.start();
//        }
    }//GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbThemeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbThemeActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        if (service != null) {
            final List attributes = new ArrayList();
            attributes.add(null);
            attributes.addAll(service.getOrderedFeatureServiceAttributes());
            cbField.setModel(new DefaultComboBoxModel(attributes.toArray(new String[0])));
            cbField.setSelectedItem(null);
//            cbField.setEnabled(rbFieldBuffer.isSelected());
        } else {
            cbField.setModel(new DefaultComboBoxModel());
//            cbField.setEnabled(false);
        }
        enabledOrNot();
    }//GEN-LAST:event_cbThemeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void rbFieldBufferActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFieldBufferActionPerformed
        enabledOrNot();
    }//GEN-LAST:event_rbFieldBufferActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void rbFixBufferActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFixBufferActionPerformed
        enabledOrNot();
    }//GEN-LAST:event_rbFixBufferActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void enabledOrNot() {
        final boolean isServiceSelected = cbTheme.getSelectedItem() != null;
        txtBuffer.setEnabled(isServiceSelected && rbFixBuffer.isSelected());
        cbField.setEnabled(isServiceSelected && rbFieldBuffer.isSelected());
        txtTable.setEnabled(isServiceSelected);
        labTableName.setEnabled(isServiceSelected);
        ckbIndividual.setEnabled(isServiceSelected);
        ckbMergeBuffer.setEnabled(isServiceSelected);
        ckbSelected.setEnabled(isServiceSelected);
        rbFieldBuffer.setEnabled(isServiceSelected);
        rbFixBuffer.setEnabled(isServiceSelected);
        butOk.setEnabled(isServiceSelected);
    }
}
