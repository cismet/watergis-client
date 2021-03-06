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

import org.deegree.datatypes.Types;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.components.location.SpatialSelectionMethodInterface;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PointInLineDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PointInLineDialog.class);

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;
    private int selectedTargetThemeFeatureCount = 0;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgBuffer;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbGeoMethod;
    private javax.swing.JComboBox cbTargetTheme;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JCheckBox ckbSelectedTarget;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labDistance;
    private javax.swing.JLabel labDistanceField;
    private javax.swing.JLabel labGeoMethod;
    private javax.swing.JLabel labSelected;
    private javax.swing.JLabel labSelectedTarget;
    private javax.swing.JLabel labTableName;
    private javax.swing.JLabel labTargetTheme;
    private javax.swing.JLabel labTheme;
    private javax.swing.JTextField txtBuffer;
    private javax.swing.JTextField txtDistanceField;
    private javax.swing.JTextField txtTable;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form PointInLineDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public PointInLineDialog(final java.awt.Frame parent, final boolean modal) {
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

        final Collection<? extends SpatialSelectionMethodInterface> spatialSelectionMethod = Lookup.getDefault()
                    .lookupAll(SpatialSelectionMethodInterface.class);
        final List<SpatialSelectionMethodInterface> ssmList = new ArrayList<SpatialSelectionMethodInterface>();

        for (final SpatialSelectionMethodInterface method : spatialSelectionMethod) {
            if (method.isUsedForGeoprocessing()) {
                ssmList.add(method);
            }
        }

        Collections.sort(ssmList, new Comparator<SpatialSelectionMethodInterface>() {

                @Override
                public int compare(final SpatialSelectionMethodInterface o1, final SpatialSelectionMethodInterface o2) {
                    return o1.getOrderId().compareTo(o2.getOrderId());
                }
            });

        cbGeoMethod.setModel(new DefaultComboBoxModel(
                ssmList.toArray(new SpatialSelectionMethodInterface[ssmList.size()])));

        txtTable.setText("PunktAufLinie");
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
        cbGeoMethodItemStateChanged(null);
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
        txtBuffer = new javax.swing.JTextField();
        cbGeoMethod = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        labTargetTheme = new javax.swing.JLabel();
        cbTargetTheme = new javax.swing.JComboBox();
        labGeoMethod = new javax.swing.JLabel();
        labDistance = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        ckbSelectedTarget = new javax.swing.JCheckBox();
        labSelectedTarget = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();
        labDistanceField = new javax.swing.JLabel();
        txtDistanceField = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.labTheme.text",
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

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableName,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.labTableName.text",
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

        txtBuffer.setMinimumSize(new java.awt.Dimension(200, 27));
        txtBuffer.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(txtBuffer, gridBagConstraints);

        cbGeoMethod.setModel(new javax.swing.DefaultComboBoxModel(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbGeoMethod.setMinimumSize(new java.awt.Dimension(200, 27));
        cbGeoMethod.setPreferredSize(new java.awt.Dimension(200, 27));
        cbGeoMethod.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbGeoMethodItemStateChanged(evt);
                }
            });
        cbGeoMethod.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

                @Override
                public void propertyChange(final java.beans.PropertyChangeEvent evt) {
                    cbGeoMethodPropertyChange(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(cbGeoMethod, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.butOk.text",
                new Object[] {})); // NOI18N
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
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.butCancel.text",
                new Object[] {})); // NOI18N
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
                PointInLineDialog.class,
                "PointInLineDialog.labTargetTheme.text",
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

        org.openide.awt.Mnemonics.setLocalizedText(
            labGeoMethod,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.labGeoMethod.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labGeoMethod, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labDistance,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.labDistance.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labDistance, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelectedTarget,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.ckbSelectedTarget.text",
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
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.ckbSelected.text",
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
            labDistanceField,
            org.openide.util.NbBundle.getMessage(
                PointInLineDialog.class,
                "PointInLineDialog.labDistanceField.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labDistanceField, gridBagConstraints);

        txtDistanceField.setMinimumSize(new java.awt.Dimension(200, 27));
        txtDistanceField.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(txtDistanceField, gridBagConstraints);

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
                new String[] {
                    NbBundle.getMessage(PointInLineDialog.class, "PointInLineDialog.setlayerModel.searchPointServices")
                }));
        cbTargetTheme.setModel(new DefaultComboBoxModel(
                new String[] {
                    NbBundle.getMessage(PointInLineDialog.class, "PointInLineDialog.setlayerModel.searchLineServices")
                }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "Point" }).toArray(
                                    new AbstractFeatureService[0])));
                        cbTargetTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "LineString", "MultiLineString" })
                                            .toArray(new AbstractFeatureService[0])));

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
                "PointInLine                                            ",
                null,
                100,
                true) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    // retrieve Features
                    int progress = 10;
                    wd.setText(NbBundle.getMessage(
                            PointInLineDialog.class,
                            "PointInLineDialog.butOkActionPerformed.doInBackground.retrieving"));
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
                    double buffer = 0.001;
                    final List<FeatureServiceFeature> resultedFeatures = new ArrayList<FeatureServiceFeature>();
                    final SpatialSelectionMethodInterface spatialOperation = (SpatialSelectionMethodInterface)
                        cbGeoMethod.getSelectedItem();
                    final LayerProperties serviceLayerProperties = featureList.get(0).getLayerProperties();
                    final LayerProperties targetServiceLayerProperties = targetFeatureList.get(0).getLayerProperties();
                    final Map<String, FeatureServiceAttribute> featureServiceAttributes =
                        new HashMap<String, FeatureServiceAttribute>(serviceLayerProperties.getFeatureService()
                                    .getFeatureServiceAttributes());
                    final Map<String, FeatureServiceAttribute> targetFeatureServiceAttributes =
                        new HashMap<String, FeatureServiceAttribute>(targetServiceLayerProperties.getFeatureService()
                                    .getFeatureServiceAttributes());
                    final List<String> secondaryFeatureProperties = new ArrayList<String>();
                    final Map<String, FeatureServiceAttribute> newFeatureServiceAttributes = FeatureServiceHelper
                                .getSuitableFeatureServiceAttribute(
                                    featureServiceAttributes,
                                    targetFeatureServiceAttributes,
                                    targetService.getOrderedFeatureServiceAttributes(),
                                    secondaryFeatureProperties);
                    final LayerProperties newLayerProperties = serviceLayerProperties.clone();
                    int count = 0;
                    String distanceField = "";

                    newLayerProperties.setFeatureService((AbstractFeatureService)
                        serviceLayerProperties.getFeatureService().clone());
                    newLayerProperties.getFeatureService().setFeatureServiceAttributes(newFeatureServiceAttributes);

                    if (txtBuffer.isEnabled() && !txtBuffer.getText().equals("")) {
                        try {
                            buffer = Double.parseDouble(txtBuffer.getText());
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                NbBundle.getMessage(
                                    PointInLineDialog.class,
                                    "PointInLineDialog.butOkActionPerformed.doInBackground.noBuffer.message"),
                                NbBundle.getMessage(
                                    PointInLineDialog.class,
                                    "PointInLineDialog.butOkActionPerformed.doInBackground.noBuffer.title"),
                                JOptionPane.ERROR_MESSAGE);
                            LOG.error("Invalid buffer entered. No buffer is used", e);
                        }
                    }

                    if (!txtDistanceField.getText().equals("") && (buffer != 0.001)) {
                        final FeatureServiceAttribute fsa = new FeatureServiceAttribute(txtDistanceField.getText(),
                                String.valueOf(Types.DOUBLE),
                                false);
                        newFeatureServiceAttributes.put(txtDistanceField.getText(), fsa);
                        distanceField = txtDistanceField.getText();
                    }

                    wd.setText(NbBundle.getMessage(
                            PointInLineDialog.class,
                            "PointInLineDialog.butOkActionPerformed.doInBackground.createFeatures"));

                    for (final FeatureServiceFeature f : featureList) {
                        Geometry searchGeom = f.getGeometry();
                        ++count;

                        if (buffer < 0.01) {
                            buffer = 0.01;
                        }

                        if ((buffer != 0) && (searchGeom != null)) {
                            searchGeom = searchGeom.buffer(buffer);
                        }

                        if (searchGeom != null) {
                            final List<FeatureServiceFeature> intersectingFeatures = featureTree.query(
                                    searchGeom.getEnvelopeInternal());
                            double maxDistanceToOrigin = Double.MAX_VALUE;
                            FeatureServiceFeature match = null;

                            for (final FeatureServiceFeature targetFeature : intersectingFeatures) {
                                if (spatialOperation.featureGeometryFulfilsRequirements(
                                                f.getGeometry(),
                                                targetFeature.getGeometry(),
                                                buffer)) {
                                    final double distanceToOrigin = f.getGeometry()
                                                .distance(targetFeature.getGeometry());
                                    if (distanceToOrigin < maxDistanceToOrigin) {
                                        match = targetFeature;
                                        maxDistanceToOrigin = distanceToOrigin;
                                    }
                                }
                            }

                            if (match != null) {
                                resultedFeatures.add(FeatureServiceHelper.mergeFeatures(
                                        f,
                                        match,
                                        newLayerProperties,
                                        secondaryFeatureProperties,
                                        distanceField));
                            }
                        }

                        // Issue 401: 15. do not add points without intersection
// if (!suitableFeatureFound) {
// resultedFeatures.add(FeatureServiceHelper.mergeFeatures(
// f,
// null,
// newLayerProperties,
// secondaryFeatureProperties,
// distanceField));
// }

                        if (Thread.interrupted()) {
                            return null;
                        }
                        // refresh the progress bar
                        if (progress < (10 + (count * 80 / featureList.size()))) {
                            progress = 10 + (count * 80 / featureList.size());
                            wd.setProgress(progress);
                        }
                    }

                    final List<String> orderedAttributeNames = new ArrayList();
                    orderedAttributeNames.addAll(service.getOrderedFeatureServiceAttributes());
                    orderedAttributeNames.addAll(secondaryFeatureProperties);

                    if (!distanceField.equals("")) {
                        orderedAttributeNames.add(distanceField);
                    }

                    // create the service
                    wd.setText(NbBundle.getMessage(
                            PointInLineDialog.class,
                            "PointInLineDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
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
                        LOG.error("Error while execute the point in line operation.", ex);
                    }
                }
            };

        if (H2FeatureService.tableAlreadyExists(tableName)) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    PointInLineDialog.class,
                    "PointInLineDialog.butOkActionPerformed.tableAlreadyExists",
                    tableName),
                NbBundle.getMessage(
                    PointInLineDialog.class,
                    "PointInLineDialog.butOkActionPerformed.tableAlreadyExists.title"),
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
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbGeoMethodPropertyChange(final java.beans.PropertyChangeEvent evt) { //GEN-FIRST:event_cbGeoMethodPropertyChange
    }                                                                                  //GEN-LAST:event_cbGeoMethodPropertyChange

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbGeoMethodItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbGeoMethodItemStateChanged
        final Object method = cbGeoMethod.getSelectedItem();
        if (method instanceof SpatialSelectionMethodInterface) {
            final boolean distanceRequired = ((SpatialSelectionMethodInterface)method).isDistanceRequired();
            txtBuffer.setEnabled(distanceRequired);
            txtDistanceField.setEnabled(distanceRequired);
        }
    }                                                                              //GEN-LAST:event_cbGeoMethodItemStateChanged

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
                PointInLineDialog.class,
                "PointInLineDialog.refreshSelectedFeatureCount.text",
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
