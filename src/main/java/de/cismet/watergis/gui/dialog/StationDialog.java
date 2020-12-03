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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StationDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgBuffer;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
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
    public StationDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        cbTheme.setModel(new DefaultComboBoxModel(
                FeatureServiceHelper.getServices(null).toArray(
                    new AbstractFeatureService[0])));
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
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(StationDialog.class, "StationDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.labTheme.text",
                new Object[] {})); // NOI18N
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
                StationDialog.class,
                "StationDialog.labTableName.text",
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

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(StationDialog.class, "StationDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(100, 29));
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
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.setPreferredSize(new java.awt.Dimension(100, 29));
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
                StationDialog.class,
                "StationDialog.ckbSelected.text",
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
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        final String tableName = txtTable.getText();
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                        .getInstance().getWatergisApp(),
                true,
                "Erstelle Stationen                                       ",
                null,
                100,
                true) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    // retrieve Features
                    int progress = 10;
                    wd.setText(NbBundle.getMessage(
                            StationDialog.class,
                            "BufferDialog.butOkActionPerformed.doInBackground.retrieving"));
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

                    // initialise variables for the geo operation
                    final List<FeatureServiceFeature> resultedFeatures = new ArrayList<FeatureServiceFeature>();
                    if ((featureList == null) || (featureList.size() == 0)) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            "Es wurden keine Objekte ausgewählt");
                        return null;
                    }
                    final LayerProperties serviceLayerProperties = featureList.get(0).getLayerProperties();
                    final LayerProperties newLayerProperties = serviceLayerProperties.clone();
                    int count = 0;

                    newLayerProperties.setFeatureService((AbstractFeatureService)
                        serviceLayerProperties.getFeatureService().clone());

                    // todo: set service attributes
                    final List<String> orderedAttributeNames = new ArrayList();
                    final Map<String, FeatureServiceAttribute> attributes =
                        new HashMap<String, FeatureServiceAttribute>();
                    FeatureServiceAttribute attr = new FeatureServiceAttribute("id", "integer", false);
                    attributes.put("id", attr);
                    orderedAttributeNames.add("id");
                    attr = new FeatureServiceAttribute("stat", "integer", false);
                    orderedAttributeNames.add("stat");
                    attributes.put("stat", attr);
                    attr = new FeatureServiceAttribute("geom", "Geometry", false);
                    orderedAttributeNames.add("geom");
                    attributes.put("geom", attr);
                    newLayerProperties.getFeatureService().setFeatureServiceAttributes(attributes);

                    wd.setText(NbBundle.getMessage(
                            StationDialog.class,
                            "BufferDialog.butOkActionPerformed.doInBackground.createFeatures"));
                    final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                            CismapBroker.getInstance().getDefaultCrsAlias());
                    H2FeatureService service = null;
                    int featuresCreated = 0;

                    // creates stations
                    for (final FeatureServiceFeature f : featureList) {
                        final Geometry geom = f.getGeometry();
                        ++count;

                        if (geom == null) {
                            continue;
                        }

                        final LengthIndexedLine lil = new LengthIndexedLine(geom);

                        for (int geomIndex = 0; geomIndex < geom.getLength(); geomIndex = geomIndex + 10) {
                            final Coordinate coordinate = lil.extractPoint(geomIndex);

                            if (service != null) {
                                final JDBCFeature newFeature = (JDBCFeature)service.getFeatureFactory()
                                            .createNewFeature();
//                                newFeature.setLayerProperties(newLayerProperties);
                                newFeature.setProperty("stat", geomIndex);
                                newFeature.setGeometry(factory.createPoint(coordinate));
                                newFeature.saveChangesWithoutUpdateEnvelope();
                            } else {
                                final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                newFeature.setLayerProperties(newLayerProperties);
                                newFeature.setGeometry(factory.createPoint(coordinate));
                                newFeature.setProperty("stat", geomIndex);
                                resultedFeatures.add(newFeature);
                                ++featuresCreated;
                            }
                        }

                        if (Thread.interrupted()) {
                            return null;
                        }
                        if (featuresCreated > 50000) {
                            if (service == null) {
                                service = FeatureServiceHelper.createNewService(AppBroker.getInstance()
                                                .getWatergisApp(),
                                        resultedFeatures,
                                        tableName,
                                        orderedAttributeNames);
                            }

                            featuresCreated = 0;
                        }

                        // refresh the progress bar
                        if (progress < (10 + (count * 80 / featureList.size()))) {
                            progress = 10 + (count * 80 / featureList.size());
                            wd.setProgress(progress);
                        }
                    }

                    if (Thread.interrupted()) {
                        return null;
                    }

                    if (service == null) {
                        // create the service
                        wd.setText(NbBundle.getMessage(
                                StationDialog.class,
                                "BufferDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
                        return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                                resultedFeatures,
                                tableName,
                                orderedAttributeNames);
                    } else {
                        return service;
                    }
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        if (service != null) {
                            FeatureServiceHelper.addServiceLayerToTheTree(service);
                        }
                    } catch (Exception ex) {
                        LOG.error("Error while execute the buffer operation.", ex);
                    }
                }
            };

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    StationDialog.class,
                    "BufferDialog.butOkActionPerformed.tableAlreadyExists",
                    tableName),
                NbBundle.getMessage(StationDialog.class,
                    "BufferDialog.butOkActionPerformed.tableAlreadyExists.title"),
                JOptionPane.ERROR_MESSAGE);
        } else {
            this.setVisible(false);
            wdt.start();
        }
    } //GEN-LAST:event_butOkActionPerformed

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
                StationDialog.class,
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
        final Object selectedObject = cbTheme.getSelectedItem();
        cbTheme.setModel(new DefaultComboBoxModel(
                new String[] { NbBundle.getMessage(StationDialog.class,
                        "BufferDialog.setlayerModel.searchServices") }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "LineString" }).toArray(
                                    new AbstractFeatureService[0])));

                        if (selectedObject != null) {
                            cbTheme.setSelectedItem(selectedObject);
                        } else {
                            if (cbTheme.getModel().getSize() > 0) {
                                cbTheme.setSelectedIndex(0);
                            } else {
                                cbTheme.setSelectedItem(null);
                            }
                        }
                    }
                });

        t.start();
    }

    /**
     * DOCUMENT ME!
     */
    private void enabledOrNot() {
        final boolean isServiceSelected = (cbTheme.getSelectedItem() instanceof AbstractFeatureService)
                    && (txtTable.getText() != null) && !txtTable.getText().equals("");

        butOk.setEnabled(isServiceSelected);
    }
}
