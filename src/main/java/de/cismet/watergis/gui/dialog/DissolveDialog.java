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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DissolveDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DissolveDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbMultiPart;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JList fieldList;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labDissolveAttr;
    private javax.swing.JLabel labSelected;
    private javax.swing.JLabel labTableName;
    private javax.swing.JLabel labTheme;
    private javax.swing.JTextField txtTable;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public DissolveDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        txtTable.setText("Dissolve");

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
                                        refreshSelectedFeatureCount(
                                            false,
                                            ckbSelected,
                                            service,
                                            selectedThemeFeatureCount,
                                            labSelected);
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
        refreshFieldModel();
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

        labTableName = new javax.swing.JLabel();
        txtTable = new javax.swing.JTextField();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        labTheme = new javax.swing.JLabel();
        cbTheme = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();
        ckbMultiPart = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        fieldList = new javax.swing.JList();
        labDissolveAttr = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(DissolveDialog.class, "DissolveDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableName,
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.labTableName.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        getContentPane().add(labTableName, gridBagConstraints);

        txtTable.setMinimumSize(new java.awt.Dimension(200, 27));
        txtTable.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        getContentPane().add(txtTable, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(DissolveDialog.class, "DissolveDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(80, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        getContentPane().add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        getContentPane().add(butCancel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.labTheme.text",
                new Object[] {})); // NOI18N
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

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelected,
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.ckbSelected.text",
                new Object[] {})); // NOI18N
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
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.ckbMultiPart.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbMultiPart, gridBagConstraints);

        fieldList.setModel(new javax.swing.AbstractListModel() {

                String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

                @Override
                public int getSize() {
                    return strings.length;
                }
                @Override
                public Object getElementAt(final int i) {
                    return strings[i];
                }
            });
        jScrollPane1.setViewportView(fieldList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labDissolveAttr,
            org.openide.util.NbBundle.getMessage(
                DissolveDialog.class,
                "DissolveDialog.labDissolveAttr.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        getContentPane().add(labDissolveAttr, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

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
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butOkActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        final List selectedFields = fieldList.getSelectedValuesList();
        final String[] propertyNames = (String[])selectedFields.toArray(new String[selectedFields.size()]);
        final String tableName = txtTable.getText();
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                        .getInstance().getWatergisApp(),
                true,
                "Dissolve",
                null,
                100) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
                    XBoundingBox bb = null;
                    int progress = 0;
                    wd.setText(NbBundle.getMessage(
                            DissolveDialog.class,
                            "DissolveDialog.butOkActionPerformed.doInBackground.retrieving"));
                    wd.setMax(100);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("retrieve all features from the service");
                    }

                    if (g != null) {
                        bb = new XBoundingBox(g);

                        try {
                            final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs()
                                            .getCode());
                            bb = transformer.transformBoundingBox(bb);
                        } catch (Exception e) {
                            LOG.error("Cannot transform CRS.", e);
                        }
                    }

                    final List<FeatureServiceFeature> featureList = FeatureServiceHelper.getFeatures(
                            service,
                            ckbSelected.isSelected());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("sort the features");
                    }

                    final Map<List<Object>, List<FeatureServiceFeature>> featureMap =
                        new HashMap<List<Object>, List<FeatureServiceFeature>>();
                    int n = 0;
                    int featureCount = featureList.size();
                    wd.setText(NbBundle.getMessage(
                            DissolveDialog.class,
                            "DissolveDialog.butOkActionPerformed.doInBackground.sorting"));

                    for (final FeatureServiceFeature f : featureList) {
                        final List<Object> propValues = getProperties(f, propertyNames);

                        List<FeatureServiceFeature> dissolvedFeature = featureMap.get(propValues);

                        if (dissolvedFeature == null) {
                            dissolvedFeature = new ArrayList<FeatureServiceFeature>();
                            dissolvedFeature.add(f);
                            featureMap.put(propValues, dissolvedFeature);
                        } else {
                            dissolvedFeature.add(f);
                        }

                        ++n;

                        if ((progress) < (n * 20 / featureCount)) {
                            progress = (n * 20 / featureCount);

                            wd.setProgress(progress);
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("merge geometries");
                    }
                    wd.setText(NbBundle.getMessage(
                            DissolveDialog.class,
                            "DissolveDialog.butOkActionPerformed.doInBackground.merging"));
                    final List<FeatureServiceFeature> dissolvedFeatures = new ArrayList<FeatureServiceFeature>();
                    featureCount = featureMap.keySet().size();
                    n = 0;

                    for (final Object key : featureMap.keySet()) {
                        final List<FeatureServiceFeature> features = featureMap.get(key);
                        final FeatureServiceFeature f = features.get(0);
                        final List<Geometry> geomList = new ArrayList<Geometry>();

                        for (final FeatureServiceFeature feature : features) {
                            geomList.add(feature.getGeometry());
                        }

                        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                geomList.get(0).getSRID());
                        Geometry geom = factory.buildGeometry(geomList);

                        if (geom instanceof GeometryCollection) {
                            geom = ((GeometryCollection)geom).union();
                        }

                        if (ckbMultiPart.isSelected()) {
                            f.setGeometry(geom);
                            dissolvedFeatures.add(f);
                        } else {
                            for (int geomIndex = 0; geomIndex < geom.getNumGeometries(); ++geomIndex) {
                                final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                newFeature.setGeometry(geom.getGeometryN(geomIndex));
                                dissolvedFeatures.add(newFeature);
                            }
                        }
                        ++n;

                        if ((progress) < (20 + (n * 55 / featureCount))) {
                            progress = 20 + (n * 55 / featureCount);

                            wd.setProgress(progress);
                        }
                    }

                    wd.setText(NbBundle.getMessage(
                            DissolveDialog.class,
                            "DissolveDialog.butOkActionPerformed.doInBackground.creatingDatasource"));

                    final List<String> orderedAttributeNames = new ArrayList();
                    orderedAttributeNames.addAll(service.getOrderedFeatureServiceAttributes());

                    return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                            dissolvedFeatures,
                            tableName,
                            orderedAttributeNames);
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        FeatureServiceHelper.addServiceLayerToTheTree(service);
                    } catch (Exception ex) {
                        LOG.error("Error while dissolving features.", ex);
                    }
                }
            };

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    DissolveDialog.class,
                    "DissolveDialog.butOkActionPerformed.tableAlreadyExists",
                    tableName),
                NbBundle.getMessage(
                    DissolveDialog.class,
                    "DissolveDialog.butOkActionPerformed.tableAlreadyExists.title"),
                JOptionPane.ERROR_MESSAGE);
        } else {
            this.setVisible(false);
            wdt.start();
        }
    }//GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbThemeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbThemeActionPerformed
        refreshFieldModel();
    }//GEN-LAST:event_cbThemeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   feature        DOCUMENT ME!
     * @param   propertyNames  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Object> getProperties(final FeatureServiceFeature feature, final String[] propertyNames) {
        final List<Object> propertyList = new ArrayList<Object>();

        for (final String propName : propertyNames) {
            propertyList.add(feature.getProperty(propName));
        }

        return propertyList;
    }

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        cbTheme.setModel(new DefaultComboBoxModel(
                new String[] {
                    NbBundle.getMessage(PointInLineDialog.class, "DissolveDialog.setlayerModel.searchPointServices")
                }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(null).toArray(
                                    new AbstractFeatureService[0])));
                        cbTheme.setSelectedItem(null);
                    }
                });

        t.start();
    }

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
                DissolveDialog.class,
                "Dissolve.refreshSelectedFeatureCount.text",
                count));

        if (forceGuiRefresh || (count != lastSelectedFeatureCount)) {
            box.setSelected(count > 0);
        }
        return count;
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshFieldModel() {
        final Object service = cbTheme.getSelectedItem();

        if ((service != null) && (service instanceof AbstractFeatureService)) {
            final List<String> fields = getAllFieldNames((AbstractFeatureService)service);
            final DefaultListModel model = new DefaultListModel();
            for (final String fieldName : fields) {
                model.add(model.size(), fieldName);
            }
            fieldList.setModel(model);
        } else {
            final DefaultListModel model = new DefaultListModel();
            fieldList.setModel(model);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> getAllFieldNames(final AbstractFeatureService service) {
        Map<String, FeatureServiceAttribute> attributeMap = service.getFeatureServiceAttributes();
        final List<String> resultList = new ArrayList<String>();

        if (attributeMap == null) {
            try {
                service.initAndWait();
            } catch (Exception e) {
                LOG.error("Error while initializing the feature service.", e);
            }
            attributeMap = service.getFeatureServiceAttributes();
        }

        for (final String name : attributeMap.keySet()) {
            resultList.add(name);
        }

        return resultList;
    }
}
