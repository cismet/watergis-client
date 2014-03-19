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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.Frame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCollection;
import de.cismet.cismap.commons.gui.layerwidget.ReadOnlyThemeLayerWidget;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

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

    //~ Instance fields --------------------------------------------------------

    Logger LOG = Logger.getLogger(LocationDialog.class);
    int lastSelectedFeatureCount = 0;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butApply;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cboSelectionMethod;
    private javax.swing.JComboBox cboSource;
    private javax.swing.JComboBox cboSpatSelectionMethod;
    private javax.swing.JCheckBox chUseDistance;
    private javax.swing.JCheckBox chUseSelectedFeatures;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labIntro;
    private javax.swing.JLabel labMeasureUnit;
    private javax.swing.JLabel labSelectedFeatures;
    private javax.swing.JLabel labSelectionMethod;
    private javax.swing.JLabel labSource;
    private javax.swing.JLabel labSpatSelectionMethod;
    private javax.swing.JLabel labTarget;
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
                                        refreshSelectedFeatureCount(false);
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
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        final ActiveLayerModel layerModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final List<AbstractFeatureService> sourceLayer = new ArrayList<AbstractFeatureService>();
        final TreeMap<Integer, MapService> serviceMap = layerModel.getMapServices();

        for (final Integer key : serviceMap.keySet()) {
            final MapService service = serviceMap.get(key);

            if (service instanceof AbstractFeatureService) {
                sourceLayer.add((AbstractFeatureService)service);
            }
        }

        cboSource.setModel(new DefaultComboBoxModel(
                sourceLayer.toArray(new AbstractFeatureService[sourceLayer.size()])));
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

        labIntro = new javax.swing.JLabel();
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
        butOk = new javax.swing.JButton();
        butApply = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        chUseDistance = new javax.swing.JCheckBox();
        txtDistance = new javax.swing.JTextField();
        labMeasureUnit = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.title")); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        labIntro.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.labIntro.text")); // NOI18N
        labIntro.setMaximumSize(new java.awt.Dimension(630, 40));
        labIntro.setMinimumSize(new java.awt.Dimension(630, 40));
        labIntro.setPreferredSize(new java.awt.Dimension(630, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(labIntro, gridBagConstraints);

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

        jPanel1.setLayout(new java.awt.FlowLayout(2, 10, 10));

        butOk.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.butOk.text")); // NOI18N
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        jPanel1.add(butOk);

        butApply.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.butApply.text")); // NOI18N
        butApply.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butApplyActionPerformed(evt);
                }
            });
        jPanel1.add(butApply);

        butCancel.setText(org.openide.util.NbBundle.getMessage(LocationDialog.class, "LocationDialog.butCancel.text")); // NOI18N
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        jPanel1.add(butCancel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(jPanel1, gridBagConstraints);

        chUseDistance.setText(org.openide.util.NbBundle.getMessage(
                LocationDialog.class,
                "LocationDialog.chUseDistance.text")); // NOI18N
        chUseDistance.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    chUseDistanceItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        getContentPane().add(chUseDistance, gridBagConstraints);

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
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        dispose();
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    executeMethod();
                }
            });
        dispose();
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void chUseDistanceItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_chUseDistanceItemStateChanged
        txtDistance.setEnabled(chUseDistance.isSelected());
    }                                                                                //GEN-LAST:event_chUseDistanceItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cboSpatSelectionMethodItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cboSpatSelectionMethodItemStateChanged
        final SpatialSelectionMethodInterface spat = (SpatialSelectionMethodInterface)
            cboSpatSelectionMethod.getSelectedItem();
        chUseDistance.setSelected(spat.isDistanceRequired());
    }                                                                                         //GEN-LAST:event_cboSpatSelectionMethodItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butApplyActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butApplyActionPerformed
        executeMethod();
    }                                                                            //GEN-LAST:event_butApplyActionPerformed

    /**
     * refreshes the labSelectedFeatures label.
     *
     * @param  forceGuiRefresh  DOCUMENT ME!
     */
    public void refreshSelectedFeatureCount(final boolean forceGuiRefresh) {
        final AbstractFeatureService featureService = (AbstractFeatureService)cboSource.getSelectedItem();
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
            if (chUseDistance.isSelected() || spat.isDistanceRequired()) {
                String doubleAsString = txtDistance.getText();
                doubleAsString = doubleAsString.replace('.', 'a');
                doubleAsString = doubleAsString.replace(',', '.');
                doubleAsString = doubleAsString.replace('a', ',');
                distance = Double.parseDouble(doubleAsString);
            }
        } catch (NumberFormatException e) {
            LOG.error("Wrong number format.", e);
        }

        for (final Object o : servicesFromTree) {
            if (o instanceof AbstractFeatureService) {
                targetServices.add((AbstractFeatureService)o);
            }
        }

        final SelectionCalculator selectionCalculator = new SelectionCalculator(StaticSwingTools.getParentFrame(this),
                true,
                NbBundle.getMessage(LocationDialog.class, "LocationDialog.executeMethod"),
                null,
                500,
                sourceService,
                targetServices,
                spat,
                meth,
                distance,
                chUseSelectedFeatures.isSelected());
        selectionCalculator.start();
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
        final AbstractFeatureService featureService = (AbstractFeatureService)cboSource.getSelectedItem();
        final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                    .getInputEventListener()
                    .get(MappingComponent.SELECT);
        final Collection<PFeature> selectedPFeatures = sl.getAllSelectedPFeatures();

        for (final PFeature feature : selectedPFeatures) {
            final Object internFeature = feature.getFeature();
            if (internFeature instanceof FeatureServiceFeature) {
                if (((FeatureServiceFeature)internFeature).getLayerProperties()
                            == featureService.getLayerProperties()) {
                    result.add((FeatureServiceFeature)internFeature);
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
    private class SelectionCalculator extends WaitingDialogThread<List<PFeature>> {

        //~ Instance fields ----------------------------------------------------

        final AbstractFeatureService sourceService;
        final List<AbstractFeatureService> targetServices;
        final SpatialSelectionMethodInterface spat;
        final SelectionMethodInterface meth;
        double distance;
        boolean useSelectedFeatures;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SelectionCalculator object.
         *
         * @param  parent               DOCUMENT ME!
         * @param  modal                DOCUMENT ME!
         * @param  text                 DOCUMENT ME!
         * @param  icon                 DOCUMENT ME!
         * @param  delay                DOCUMENT ME!
         * @param  sourceService        DOCUMENT ME!
         * @param  targetServices       DOCUMENT ME!
         * @param  spat                 DOCUMENT ME!
         * @param  meth                 DOCUMENT ME!
         * @param  distance             DOCUMENT ME!
         * @param  useSelectedFeatures  DOCUMENT ME!
         */
        public SelectionCalculator(final Frame parent,
                final boolean modal,
                final String text,
                final Icon icon,
                final int delay,
                final AbstractFeatureService sourceService,
                final List<AbstractFeatureService> targetServices,
                final SpatialSelectionMethodInterface spat,
                final SelectionMethodInterface meth,
                final double distance,
                final boolean useSelectedFeatures) {
            super(parent, modal, text, icon, delay);
            this.sourceService = sourceService;
            this.targetServices = targetServices;
            this.spat = spat;
            this.meth = meth;
            this.distance = distance;
            this.useSelectedFeatures = useSelectedFeatures;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected List<PFeature> doInBackground() throws Exception {
            final List<Geometry> geomList = new ArrayList<Geometry>();
            final List<FeatureServiceFeature> sourceFeatures;
            Geometry geom = null;

            if (useSelectedFeatures) {
                sourceFeatures = getSelectedFeatures(sourceService);
            } else {
                sourceFeatures = sourceService.getFeatureFactory().getLastCreatedFeatures();
            }

            for (final FeatureServiceFeature fsf : sourceFeatures) {
                final Geometry newGeom = fsf.getGeometry();

                if (newGeom != null) {
                    geomList.add(fsf.getGeometry());
                }
            }

            if (geomList.size() > 0) {
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        geomList.get(0).getSRID());
                geom = factory.buildGeometry(geomList);

                if (geom instanceof GeometryCollection) {
                    geom = ((GeometryCollection)geom).union();
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
                final List<PFeature> correspondingFeatures = get();

                meth.executeMethod(correspondingFeatures, sourceService, targetServices);
            } catch (Exception e) {
                LOG.error("Error while calculating new selection", e);
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
        private List<PFeature> calculateCorrespondingFeatures(final Geometry source,
                final List<AbstractFeatureService> targetLayer,
                final double distance,
                final SpatialSelectionMethodInterface spatMethod) {
            final List<PFeature> resultFeatureList = new ArrayList<PFeature>();

            if (source != null) {
                for (final AbstractFeatureService service : targetLayer) {
                    for (final Object featureObject : service.getPNode().getChildrenReference()) {
                        final PFeature feature = (PFeature)featureObject;
                        if (spatMethod.featureGeometryFulfilsRequirements(
                                        source,
                                        feature.getFeature().getGeometry(),
                                        distance)) {
                            resultFeatureList.add(feature);
                        }
                    }
                }
            }

            return resultFeatureList;
        }
    }
}
