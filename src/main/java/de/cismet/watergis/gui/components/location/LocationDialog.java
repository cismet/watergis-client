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
package de.cismet.watergis.gui.components.location;

import Sirius.navigator.search.dynamic.SearchControlPanel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Frame;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCollection;
import de.cismet.cismap.commons.gui.layerwidget.ReadOnlyThemeLayerWidget;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionChangedEvent;
import de.cismet.cismap.commons.util.SelectionChangedListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LocationDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Class[] SUPPORTED_CLASSES = new Class[] {
            AbstractFeatureService.class,
            LayerCollection.class
        };

    private static final Logger LOG = Logger.getLogger(LocationDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int lastSelectedFeatureCount = 0;
    private boolean inProgress = false;
    private ImageIcon iconSearch;
    private ImageIcon iconCancel;
    private SelectionCalculator selectionCalculator;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearchCancel;
    private javax.swing.JComboBox cboSelectionMethod;
    private javax.swing.JComboBox cboSource;
    private javax.swing.JComboBox cboSpatSelectionMethod;
    private javax.swing.JCheckBox chUseSelectedFeatures;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labDist;
    private javax.swing.JLabel labMeasureUnit;
    private javax.swing.JLabel labSelectedFeatures;
    private javax.swing.JLabel labSelectionMethod;
    private javax.swing.JLabel labSource;
    private javax.swing.JLabel labSpatSelectionMethod;
    private javax.swing.JLabel labTarget;
    private org.jdesktop.swingx.JXBusyLabel lblBusyIcon;
    private javax.swing.Box.Filler strGap;
    private javax.swing.JPanel tltTarget;
    private javax.swing.JTextField txtDistance;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form LocationDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public LocationDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        final Collection<? extends SelectionMethodInterface> selectionMethod = Lookup.getDefault()
                    .lookupAll(SelectionMethodInterface.class);
        final Collection<? extends SpatialSelectionMethodInterface> spatialSelectionMethod = Lookup.getDefault()
                    .lookupAll(SpatialSelectionMethodInterface.class);

        final SpatialSelectionMethodInterface[] ssmArray = spatialSelectionMethod.toArray(
                new SpatialSelectionMethodInterface[spatialSelectionMethod.size()]);
        final SelectionMethodInterface[] smArray = selectionMethod.toArray(
                new SelectionMethodInterface[selectionMethod.size()]);
        // sort the selectionMethodInterfaces
        Arrays.sort(ssmArray, new Comparator<SpatialSelectionMethodInterface>() {

                @Override
                public int compare(final SpatialSelectionMethodInterface o1, final SpatialSelectionMethodInterface o2) {
                    return o1.getOrderId().compareTo(o2.getOrderId());
                }
            });
        Arrays.sort(smArray, new Comparator<SelectionMethodInterface>() {

                @Override
                public int compare(final SelectionMethodInterface o1, final SelectionMethodInterface o2) {
                    return o1.getOrderId().compareTo(o2.getOrderId());
                }
            });

        final URL iconSearchUrl = getClass().getResource(
                "/Sirius/navigator/search/dynamic/SearchControlPanel_btnSearchCancel.png");
        if (iconSearchUrl != null) {
            this.iconSearch = new ImageIcon(iconSearchUrl);
        } else {
            this.iconSearch = new ImageIcon();
        }

        final URL iconCancelUrl = getClass().getResource(
                "/Sirius/navigator/search/dynamic/SearchControlPanel_btnSearchCancel_cancel.png");
        if (iconCancelUrl != null) {
            this.iconCancel = new ImageIcon(iconCancelUrl);
        } else {
            this.iconCancel = new ImageIcon();
        }

        cboSpatSelectionMethod.setModel(new DefaultComboBoxModel(ssmArray));
        cboSelectionMethod.setModel(new DefaultComboBoxModel(smArray));
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
        txtDistance.setEnabled(false);
        setLayerModel();

        SelectionManager.getInstance().addSelectionChangedListener(new SelectionChangedListener() {

                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                refreshSelectedFeatureCount(false);
                            }
                        });
                }
            });

//        CismapBroker.getInstance()
//                .getMappingComponent()
//                .getFeatureCollection()
//                .addFeatureCollectionListener(new FeatureCollectionListener() {
//
//                        @Override
//                        public void featuresAdded(final FeatureCollectionEvent fce) {
//                        }
//
//                        @Override
//                        public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
//                        }
//
//                        @Override
//                        public void featuresRemoved(final FeatureCollectionEvent fce) {
//                        }
//
//                        @Override
//                        public void featuresChanged(final FeatureCollectionEvent fce) {
//                        }
//
//                        @Override
//                        public void featureSelectionChanged(final FeatureCollectionEvent fce) {
//                            EventQueue.invokeLater(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//                                        refreshSelectedFeatureCount(false);
//                                    }
//                                });
//                        }
//
//                        @Override
//                        public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
//                        }
//
//                        @Override
//                        public void featureCollectionChanged() {
//                        }
//                    });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        final List<AbstractFeatureService> sourceLayer = FeatureServiceHelper.getServices(null);
        final Object selectedItem = cboSource.getSelectedItem();
        cboSource.setModel(new DefaultComboBoxModel(
                sourceLayer.toArray(new AbstractFeatureService[sourceLayer.size()])));
        cboSource.setSelectedItem(selectedItem);
        refreshSelectedFeatureCount(false);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        labSelectionMethod = new javax.swing.JLabel();
        cboSelectionMethod = new javax.swing.JComboBox();
        labSource = new javax.swing.JLabel();
        cboSource = new javax.swing.JComboBox();
        labTarget = new javax.swing.JLabel();
        tltTarget = new ReadOnlyThemeLayerWidget();
        ((ReadOnlyThemeLayerWidget)tltTarget).setMappingModel((ActiveLayerModel)AppBroker.getInstance()
                    .getMappingComponent().getMappingModel(),
            SUPPORTED_CLASSES);
        chUseSelectedFeatures = new javax.swing.JCheckBox();
        labSpatSelectionMethod = new javax.swing.JLabel();
        cboSpatSelectionMethod = new javax.swing.JComboBox();
        labSelectedFeatures = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        lblBusyIcon = new org.jdesktop.swingx.JXBusyLabel(new java.awt.Dimension(20, 20));
        strGap = new javax.swing.Box.Filler(new java.awt.Dimension(5, 0),
                new java.awt.Dimension(5, 25),
                new java.awt.Dimension(5, 32767));
        btnSearchCancel = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        txtDistance = new javax.swing.JTextField();
        labMeasureUnit = new javax.swing.JLabel();
        labDist = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        labSelectionMethod.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.labSelectionMethod.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labSelectionMethod, gridBagConstraints);

        cboSelectionMethod.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(cboSelectionMethod, gridBagConstraints);

        labSource.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.labSource.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(labSource, gridBagConstraints);

        cboSource.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboSource.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cboSourceItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(cboSource, gridBagConstraints);

        labTarget.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.labTarget.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(labTarget, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(tltTarget, gridBagConstraints);

        chUseSelectedFeatures.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.chUseSelectedFeatures.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(chUseSelectedFeatures, gridBagConstraints);

        labSpatSelectionMethod.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.labSpatSelectionMethod.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(labSpatSelectionMethod, gridBagConstraints);

        cboSpatSelectionMethod.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cboSpatSelectionMethod.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cboSpatSelectionMethodItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(cboSpatSelectionMethod, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labSelectedFeatures, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jSeparator1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 10));

        jPanel2.setMinimumSize(new java.awt.Dimension(125, 25));
        jPanel2.setPreferredSize(new java.awt.Dimension(185, 25));
        jPanel2.setLayout(new java.awt.GridBagLayout());

        lblBusyIcon.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanel2.add(lblBusyIcon, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel2.add(strGap, gridBagConstraints);

        btnSearchCancel.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.btnSearchCancel.text"));        // NOI18N
        btnSearchCancel.setToolTipText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.btnSearchCancel.toolTipText")); // NOI18N
        btnSearchCancel.setMaximumSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.setMinimumSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.setPreferredSize(new java.awt.Dimension(100, 25));
        btnSearchCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    btnSearchCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel2.add(btnSearchCancel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanel6, gridBagConstraints);

        jPanel1.add(jPanel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

        txtDistance.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txtDistance, gridBagConstraints);

        labMeasureUnit.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.labMeasureUnit.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labMeasureUnit, gridBagConstraints);

        labDist.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.labDist.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labDist, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboSourceItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cboSourceItemStateChanged
        refreshSelectedFeatureCount(true);
    }                                                                            //GEN-LAST:event_cboSourceItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboSpatSelectionMethodItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cboSpatSelectionMethodItemStateChanged
        final SpatialSelectionMethodInterface spat = (SpatialSelectionMethodInterface)
            cboSpatSelectionMethod.getSelectedItem();
        txtDistance.setEnabled(spat.isDistanceRequired());

        if (txtDistance.isEnabled() && txtDistance.getText().equals("")) {
            txtDistance.setText("0");
        }
    } //GEN-LAST:event_cboSpatSelectionMethodItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void btnSearchCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_btnSearchCancelActionPerformed
        if (!inProgress) {
            executeMethod();
        } else {
            if ((selectionCalculator != null) && !selectionCalculator.isDone()) {
                selectionCalculator.cancel(true);
            }
        }
    }                                                                                   //GEN-LAST:event_btnSearchCancelActionPerformed

    /**
     * refreshes the labSelectedFeatures label.
     *
     * @param  forceGuiRefresh  DOCUMENT ME!
     */
    public void refreshSelectedFeatureCount(final boolean forceGuiRefresh) {
        final AbstractFeatureService featureService = (AbstractFeatureService)cboSource.getSelectedItem();
        if (featureService != null) {
            final int count = getSelectedFeatures(featureService).size();

            labSelectedFeatures.setText(NbBundle.getMessage(
                    LocationDialog.class,
                    "LocationDialog.labSelectedFeatures.text",
                    count));

            if (forceGuiRefresh || (count != lastSelectedFeatureCount)) {
                chUseSelectedFeatures.setSelected(count > 0);
            }
            lastSelectedFeatureCount = count;
        }
    }

    /**
     * executes the selected operation.
     */
    private void executeMethod() {
        final AbstractFeatureService sourceService = (AbstractFeatureService)cboSource.getSelectedItem();
        final List<AbstractFeatureService> targetServices = new ArrayList<AbstractFeatureService>();
        final SpatialSelectionMethodInterface spat = (SpatialSelectionMethodInterface)
            cboSpatSelectionMethod.getSelectedItem();
        final SelectionMethodInterface meth = (SelectionMethodInterface)cboSelectionMethod.getSelectedItem();
        final List<Object> servicesFromTree = ((ReadOnlyThemeLayerWidget)tltTarget).getSelectedServices();
        double distance = 0.0;

        try {
            if (spat.isDistanceRequired()) {
                String doubleAsString = txtDistance.getText();
                doubleAsString = doubleAsString.replace('.', 'a');
                doubleAsString = doubleAsString.replace(',', '.');
                doubleAsString = doubleAsString.replace('a', ',');
                distance = Double.parseDouble(doubleAsString);
            }
        } catch (NumberFormatException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Wrong number format.", e);
            }
            txtDistance.setText("0");
        }

        for (final Object o : servicesFromTree) {
            if (o instanceof AbstractFeatureService) {
                targetServices.add((AbstractFeatureService)o);
            }
        }

        selectionCalculator = new SelectionCalculator(
                sourceService,
                targetServices,
                spat,
                meth,
                distance,
                chUseSelectedFeatures.isSelected());

        inProgress = true;
        setControlsAccordingToState(inProgress);

        CismetExecutors.newSingleThreadExecutor().execute(selectionCalculator);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  inProgress  DOCUMENT ME!
     */
    public void setControlsAccordingToState(final boolean inProgress) {
        if (inProgress) {
            btnSearchCancel.setText(org.openide.util.NbBundle.getMessage(
                    SearchControlPanel.class,
                    "SearchControlPanel.btnSearchCancel_cancel.text"));        // NOI18N
            btnSearchCancel.setToolTipText(org.openide.util.NbBundle.getMessage(
                    SearchControlPanel.class,
                    "SearchControlPanel.btnSearchCancel_cancel.toolTipText")); // NOI18N
            btnSearchCancel.setIcon(iconCancel);
            lblBusyIcon.setEnabled(true);
            lblBusyIcon.setBusy(true);
        } else {
            btnSearchCancel.setText(org.openide.util.NbBundle.getMessage(
                    LocationDialog.class,
                    "LocationDialog.btnSearchCancel_select.text"));            // NOI18N
            btnSearchCancel.setToolTipText(org.openide.util.NbBundle.getMessage(
                    SearchControlPanel.class,
                    "SearchControlPanel.btnSearchCancel.toolTipText"));        // NOI18N
            btnSearchCancel.setIcon(iconSearch);
            lblBusyIcon.setEnabled(false);
            lblBusyIcon.setBusy(false);
        }
    }

    /**
     * Provides all selected features of the given service.
     *
     * @param   service  the service, the selected features should be returned for
     *
     * @return  all selected features of the given service
     */
    private List<FeatureServiceFeature> getSelectedFeatures(final AbstractFeatureService service) {
        final List<FeatureServiceFeature> result = new ArrayList<FeatureServiceFeature>();
        final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures(service);

        if (selectedFeatures != null) {
            final List<Feature> selectedFeaturesCopy = new ArrayList<Feature>(selectedFeatures);

            for (final Feature feature : selectedFeaturesCopy) {
                if (feature instanceof FeatureServiceFeature) {
                    result.add((FeatureServiceFeature)feature);
                }
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  the command line arguments
     */
    public static void main(final String[] args) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LocationDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LocationDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LocationDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LocationDialog.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final LocationDialog dialog = new LocationDialog(new javax.swing.JFrame(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                            @Override
                            public void windowClosing(final java.awt.event.WindowEvent e) {
                                System.exit(0);
                            }
                        });
                    dialog.setVisible(true);
                }
            });
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * Does the selection.
     *
     * @version  $Revision$, $Date$
     */
    private class SelectionCalculator extends SwingWorker<List<FeatureServiceFeature>, Void> {

        //~ Instance fields ----------------------------------------------------

        final AbstractFeatureService sourceService;
        final List<AbstractFeatureService> targetServices;
        final SpatialSelectionMethodInterface spat;
        final SelectionMethodInterface meth;
        double distance;
        boolean useSelectedFeatures;
        boolean abort = false;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SelectionCalculator object.
         *
         * @param  sourceService        DOCUMENT ME!
         * @param  targetServices       DOCUMENT ME!
         * @param  spat                 DOCUMENT ME!
         * @param  meth                 DOCUMENT ME!
         * @param  distance             DOCUMENT ME!
         * @param  useSelectedFeatures  DOCUMENT ME!
         */
        public SelectionCalculator(
                final AbstractFeatureService sourceService,
                final List<AbstractFeatureService> targetServices,
                final SpatialSelectionMethodInterface spat,
                final SelectionMethodInterface meth,
                final double distance,
                final boolean useSelectedFeatures) {
            this.sourceService = sourceService;
            this.targetServices = targetServices;
            this.spat = spat;
            this.meth = meth;
            this.distance = distance;
            this.useSelectedFeatures = useSelectedFeatures;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected List<FeatureServiceFeature> doInBackground() throws Exception {
            final List<Geometry> geomList = new ArrayList<Geometry>();
            final List<FeatureServiceFeature> sourceFeatures;
            Geometry geom = null;

            if (useSelectedFeatures) {
                sourceFeatures = getSelectedFeatures(sourceService);
            } else {
                final Geometry g = ZoomToLayerWorker.getServiceBounds(sourceService);
                XBoundingBox bounds = null;

                if (g != null) {
                    bounds = new XBoundingBox(g);
                    final String crs;
                    crs = CismapBroker.getInstance().getSrs().getCode();

                    final CrsTransformer trans = new CrsTransformer(crs);
                    bounds = trans.transformBoundingBox(bounds);
                }
                sourceFeatures = sourceService.getFeatureFactory()
                            .createFeatures(sourceService.getQuery(), bounds, null, 0, 0, null);
            }

            for (final FeatureServiceFeature fsf : sourceFeatures) {
                if (Thread.interrupted()) {
                    abort = true;
                    return null;
                }
                final Geometry newGeom = fsf.getGeometry();

                if ((newGeom != null) && fsf.getGeometry().isValid()) {
                    geomList.add(fsf.getGeometry());
                }
            }

            if (geomList.size() > 0) {
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        geomList.get(0).getSRID());
                if (Thread.interrupted()) {
                    abort = true;
                    return null;
                }
                geom = factory.buildGeometry(geomList);

                if (geom instanceof GeometryCollection) {
                    try {
                        geom = ((GeometryCollection)geom).union();
                    } catch (TopologyException e) {
                        LOG.error("Topology exception occured during an union operation", e);
                        geom = null;

                        for (final Geometry g : geomList) {
                            if ((geom == null) && g.isValid()) {
                                geom = g;
                            } else {
                                if ((g != null) && g.isValid()) {
                                    geom = geom.union(g);
                                }
                            }
                        }
                    }
                }
            }

            return calculateCorrespondingFeatures(
                    geom,
                    targetServices,
                    distance,
                    spat);
        }

        @Override
        protected void done() {
            try {
                final List<FeatureServiceFeature> correspondingFeatures = get();

                if (!abort) {
                    meth.executeMethod(correspondingFeatures, sourceService, targetServices);
                }
            } catch (Exception e) {
                LOG.error("Error while calculating new selection", e);
            } finally {
                inProgress = false;
                setControlsAccordingToState(inProgress);
            }
        }

        /**
         * Get all features, which fulfil the requirements of the given spatial selection method.
         *
         * @param   source       DOCUMENT ME!
         * @param   targetLayer  DOCUMENT ME!
         * @param   distance     DOCUMENT ME!
         * @param   spatMethod   DOCUMENT ME!
         *
         * @return  all features, which fulfil the requirements of the given spatial selection method
         */
        private List<FeatureServiceFeature> calculateCorrespondingFeatures(final Geometry source,
                final List<AbstractFeatureService> targetLayer,
                final double distance,
                final SpatialSelectionMethodInterface spatMethod) {
            final List<FeatureServiceFeature> resultFeatureList = new ArrayList<FeatureServiceFeature>();

            if (source != null) {
                Geometry bufferedGeometry = source.getEnvelope();

                if (distance > 0) {
                    bufferedGeometry = bufferedGeometry.buffer(distance);
                }

                for (final AbstractFeatureService service : targetLayer) {
                    final List<FeatureServiceFeature> featureList = getAllFeatures(service, bufferedGeometry);

                    if (featureList == null) {
                        continue;
                    }

                    for (final Object featureObject : featureList) {
                        if (Thread.interrupted()) {
                            abort = true;
                            return null;
                        }
                        final FeatureServiceFeature feature = (FeatureServiceFeature)featureObject;
                        if (spatMethod.featureGeometryFulfilsRequirements(
                                        source,
                                        feature.getGeometry(),
                                        distance)) {
                            resultFeatureList.add(feature);
                        }
                    }
                }
            }

            return resultFeatureList;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   service  DOCUMENT ME!
         * @param   geom     DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private List<FeatureServiceFeature> getAllFeatures(final AbstractFeatureService service, final Geometry geom) {
            try {
                Geometry g = geom;
                service.initAndWait();

                if (geom == null) {
                    g = ZoomToLayerWorker.getServiceBounds(service);
                }
                XBoundingBox bounds = null;

                if (g != null) {
                    bounds = new XBoundingBox(g);
                    final String crs;
                    crs = CismapBroker.getInstance().getSrs().getCode();

                    final CrsTransformer trans = new CrsTransformer(crs);
                    bounds = trans.transformBoundingBox(bounds);
                }

                return service.getFeatureFactory().createFeatures(service.getQuery(), bounds, null, 0, 0, null);
            } catch (Exception e) {
                LOG.error("Error while retrieving features.", e);

                return null;
            }
        }
    }
}
