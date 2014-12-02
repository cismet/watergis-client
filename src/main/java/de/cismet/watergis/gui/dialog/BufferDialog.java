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
import com.vividsolutions.jts.index.strtree.STRtree;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

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
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class BufferDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(BufferDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;

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
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labSelected;
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
        txtTable.setText("Puffer");
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
        cbField.setEnabled(rbFieldBuffer.isSelected());
        txtBuffer.setEnabled(rbFixBuffer.isSelected());
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
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.labTheme.text", new Object[] {})); // NOI18N
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

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
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

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableName,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.labTableName.text",
                new Object[] {})); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbIndividual,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.ckbIndividual.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbIndividual, gridBagConstraints);

        bgBuffer.add(rbFixBuffer);
        rbFixBuffer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            rbFixBuffer,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.rbFixBuffer.text",
                new Object[] {})); // NOI18N
        rbFixBuffer.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
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
        org.openide.awt.Mnemonics.setLocalizedText(
            rbFieldBuffer,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.rbFieldBuffer.text",
                new Object[] {})); // NOI18N
        rbFieldBuffer.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
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

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbMergeBuffer,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.ckbMergeBuffer.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbMergeBuffer, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(80, 29));
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
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(BufferDialog.class, "BufferDialog.butCancel.text", new Object[] {})); // NOI18N
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

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelected,
            org.openide.util.NbBundle.getMessage(
                BufferDialog.class,
                "BufferDialog.ckbSelected.text",
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
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jPanel4, gridBagConstraints);

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
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        final String tableName = txtTable.getText();
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                        .getInstance().getWatergisApp(),
                true,
                "Puffer",
                null,
                100) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    // retrieve Features
                    int progress = 10;
                    wd.setText(NbBundle.getMessage(
                            BufferDialog.class,
                            "BufferDialog.butOkActionPerformed.doInBackground.retrieving"));
                    wd.setMax(100);
                    wd.setProgress(5);
                    final List<FeatureServiceFeature> featureList = FeatureServiceHelper.getFeatures(
                            service,
                            ckbSelected.isSelected());
                    wd.setProgress(10);

                    // initialise variables for the geoperation
                    double buffer = 0;
                    final List<FeatureServiceFeature> resultedFeatures = new ArrayList<FeatureServiceFeature>();
                    final LayerProperties serviceLayerProperties = featureList.get(0).getLayerProperties();
                    final LayerProperties newLayerProperties = serviceLayerProperties.clone();
                    int count = 0;
                    final int percentageToBuffer = (ckbMergeBuffer.isSelected() ? 80 : 40);
                    String distanceAttribute = "";

                    newLayerProperties.setFeatureService((AbstractFeatureService)
                        serviceLayerProperties.getFeatureService().clone());
                    newLayerProperties.getFeatureService()
                            .setFeatureServiceAttributes(FeatureServiceHelper
                                .createGeometryOnlyFeatureServiceAttributes(
                                    serviceLayerProperties.getFeatureService().getFeatureServiceAttributes()));

                    if (rbFixBuffer.isSelected()) {
                        if (txtBuffer.isEnabled() && !txtBuffer.getText().equals("")) {
                            try {
                                buffer = Double.parseDouble(txtBuffer.getText());
                            } catch (NumberFormatException e) {
                                // todo Ausgabe für den Nutzer
                                LOG.error("Invalid buffer entered. No buffer is used", e);
                            }
                        }
                    } else {
                        distanceAttribute = cbField.getSelectedItem().toString();
                    }

                    wd.setText(NbBundle.getMessage(
                            BufferDialog.class,
                            "BufferDialog.butOkActionPerformed.doInBackground.createFeatures"));

                    // buffer geometries
                    for (final FeatureServiceFeature f : featureList) {
                        Geometry geom = f.getGeometry();
                        ++count;

                        if (geom != null) {
                            if (rbFixBuffer.isSelected()) {
                                geom = geom.buffer(buffer);
                            } else {
                                final double bufferValue = toDouble(f.getProperty(distanceAttribute));
                                geom = geom.buffer(bufferValue);
                            }
                        }

                        if (ckbIndividual.isSelected() && (geom.getNumGeometries() > 1)) {
                            for (int geomIndex = 0; geomIndex < geom.getNumGeometries(); ++geomIndex) {
                                final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                newFeature.setLayerProperties(newLayerProperties);
                                newFeature.setGeometry(geom.getGeometryN(geomIndex));
                                resultedFeatures.add(newFeature);
                            }
                        } else {
                            if (!geom.isEmpty()) {
                                final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                newFeature.setGeometry(geom);
                                resultedFeatures.add(newFeature);
                            }
                        }

                        // refresh the progress bar
                        if (progress < (10 + (count * percentageToBuffer / featureList.size()))) {
                            progress = 10 + (count * percentageToBuffer / featureList.size());
                            wd.setProgress(progress);
                        }
                    }

                    // merge geometries of required
                    if (!ckbMergeBuffer.isSelected()) {
                        final List<FeatureServiceFeature> bufferedFeatures = new ArrayList<FeatureServiceFeature>(
                                resultedFeatures);
                        resultedFeatures.clear();
                        final TreeSet<FeatureServiceFeature> usedFeatureMap = new TreeSet<FeatureServiceFeature>();
                        count = 0;

                        final STRtree tree = FeatureServiceHelper.getFeatureTree(bufferedFeatures);

                        for (final FeatureServiceFeature f : bufferedFeatures) {
                            ++count;

                            // refresh the progress bar
                            if (progress
                                        < (10 + percentageToBuffer
                                            + (count * (80 - percentageToBuffer) / featureList.size()))) {
                                progress = 10 + percentageToBuffer
                                            + (count * (80 - percentageToBuffer) / featureList.size());
                                wd.setProgress(progress);
                            }

                            if (usedFeatureMap.contains(f)) {
                                continue;
                            } else {
                                usedFeatureMap.add(f);
                            }

                            if (f.getGeometry() != null) {
                                boolean geometryIncreased;
                                do {
                                    geometryIncreased = false;
                                    final List<FeatureServiceFeature> suitableFeatures = tree.query(f.getGeometry()
                                                    .getEnvelopeInternal());

                                    for (final FeatureServiceFeature intersectionCandidate : suitableFeatures) {
                                        if (!f.equals(intersectionCandidate)
                                                    && f.getGeometry().intersects(intersectionCandidate.getGeometry())
                                                    && !usedFeatureMap.contains(intersectionCandidate)) {
                                            f.setGeometry(f.getGeometry().union(intersectionCandidate.getGeometry()));
                                            usedFeatureMap.add(intersectionCandidate);
                                            geometryIncreased = true;
                                            ++count;

                                            // refresh the progress bar
                                            if (progress
                                                        < (10 + percentageToBuffer
                                                            + (count * (80 - percentageToBuffer) / featureList.size()))) {
                                                progress = 10 + percentageToBuffer
                                                            + (count * (80 - percentageToBuffer) / featureList.size());
                                                wd.setProgress(progress);
                                            }
                                        }
                                    }
                                } while (geometryIncreased);
                            }

                            f.setLayerProperties(newLayerProperties);
                            resultedFeatures.add(f);
                        }
                    }

                    final List<String> orderedAttributeNames = new ArrayList();
                    orderedAttributeNames.addAll(service.getOrderedFeatureServiceAttributes());

                    // create the service
                    wd.setText(NbBundle.getMessage(
                            BufferDialog.class,
                            "BufferDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
                    return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                            resultedFeatures,
                            tableName,
                            orderedAttributeNames);
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        FeatureServiceHelper.addServiceLayerToTheTree(service);
                    } catch (Exception ex) {
                        LOG.error("Error while execute the buffer operation.", ex);
                    }
                }
            };

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    BufferDialog.class,
                    "BufferDialog.butOkActionPerformed.tableAlreadyExists",
                    tableName),
                NbBundle.getMessage(
                    BufferDialog.class,
                    "BufferDialog.butOkActionPerformed.tableAlreadyExists.title"),
                JOptionPane.ERROR_MESSAGE);
        } else {
            this.setVisible(false);
            wdt.start();
        }
    }//GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double toDouble(final Object o) {
        if (o == null) {
            return 0;
        } else {
            final String doubleAsString = o.toString();

            try {
                return Double.parseDouble(doubleAsString);
            } catch (NumberFormatException e) {
                LOG.error(o.toString() + " is not a number.", e);
                return 0;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbThemeActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbThemeActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        selectedThemeFeatureCount = refreshSelectedFeatureCount(
                false,
                ckbSelected,
                service,
                selectedThemeFeatureCount,
                labSelected);
        refreshFieldModel();
        enabledOrNot();
    }//GEN-LAST:event_cbThemeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void rbFieldBufferActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFieldBufferActionPerformed
        cbField.setEnabled(rbFieldBuffer.isSelected());
        txtBuffer.setEnabled(rbFixBuffer.isSelected());
    }//GEN-LAST:event_rbFieldBufferActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void rbFixBufferActionPerformed(final java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbFixBufferActionPerformed
        cbField.setEnabled(rbFieldBuffer.isSelected());
        txtBuffer.setEnabled(rbFixBuffer.isSelected());
    }//GEN-LAST:event_rbFixBufferActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void refreshFieldModel() {
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();

        if (service != null) {
            final List<String> fields = getAllFieldNames(service, Number.class);
            cbField.setModel(new DefaultComboBoxModel(fields.toArray(new String[fields.size()])));
        } else {
            cbField.setModel(new DefaultComboBoxModel());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   service  DOCUMENT ME!
     * @param   cl       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> getAllFieldNames(final AbstractFeatureService service, final Class<?> cl) {
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
            final FeatureServiceAttribute attr = attributeMap.get(name);

            if (cl.isAssignableFrom(FeatureTools.getClass(attr))) {
                resultList.add(name);
            }
        }

        return resultList;
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
                BufferDialog.class,
                "BufferDialog.refreshSelectedFeatureCount.text",
                count));

        if (forceGuiRefresh || (count != lastSelectedFeatureCount)) {
            box.setSelected(count > 0);
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        cbTheme.setModel(new DefaultComboBoxModel(
                new String[] { NbBundle.getMessage(BufferDialog.class,
                        "BufferDialog.setlayerModel.searchServices") }));

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
     * DOCUMENT ME!
     */
    private void enabledOrNot() {
        final boolean isServiceSelected = (cbTheme.getSelectedItem() instanceof AbstractFeatureService);

        butOk.setEnabled(isServiceSelected);
    }
}
