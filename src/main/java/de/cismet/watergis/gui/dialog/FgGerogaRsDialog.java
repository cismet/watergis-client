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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cids.custom.watergis.server.search.Buffer;
import de.cismet.cids.custom.watergis.server.search.PreparedRandstreifenGeoms;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;

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
public class FgGerogaRsDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FgGerogaRsDialog.class);

    //~ Instance fields --------------------------------------------------------

    private JTextField[] allTxtBr;

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
    private int selectedGewFeatureCount = -1;
    private String lastValue = "";

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbbAttr1;
    private javax.swing.JComboBox cbbAttr2;
    private javax.swing.JComboBox cbbFlaeche;
    private javax.swing.JCheckBox ckbFgFlaechen;
    private javax.swing.JCheckBox ckbFlSelection;
    private javax.swing.JCheckBox ckbKleinsee;
    private javax.swing.JCheckBox ckbOffeneFg;
    private javax.swing.JCheckBox ckbOffeneFgBreite;
    private javax.swing.JCheckBox ckbOstsee;
    private javax.swing.JCheckBox ckbPerArea;
    private javax.swing.JCheckBox ckbSee;
    private javax.swing.JCheckBox ckbStand;
    private javax.swing.JCheckBox ckbVar1;
    private javax.swing.JCheckBox ckbVar2;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel labM;
    private javax.swing.JLabel labM1;
    private javax.swing.JLabel labM2;
    private javax.swing.JTextField txtBr1;
    private javax.swing.JTextField txtBr10;
    private javax.swing.JTextField txtBr2;
    private javax.swing.JTextField txtBr3;
    private javax.swing.JTextField txtBr4;
    private javax.swing.JTextField txtBr5;
    private javax.swing.JTextField txtBr6;
    private javax.swing.JTextField txtBr7;
    private javax.swing.JTextField txtBr8;
    private javax.swing.JTextField txtBr9;
    private javax.swing.JTextField txtFile;
    private javax.swing.JTextField txtGerBr;
    private javax.swing.JTextField txtVar2Br;
    private javax.swing.JTextField txtVar2St;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private FgGerogaRsDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        final Var1InputVerifier verifier = new Var1InputVerifier();
        final Var2BrInputVerifier verifier2Br = new Var2BrInputVerifier();
        final Var2StInputVerifier verifier2St = new Var2StInputVerifier();

        allTxtBr = new JTextField[] { txtBr1, txtBr2, txtBr3, txtBr4, txtBr5, txtBr6, txtBr7, txtBr8, txtBr9, txtBr10 };

        for (final JTextField field : allTxtBr) {
            field.addFocusListener(new FocusAdapter() {

                    @Override
                    public void focusGained(final FocusEvent e) {
                        lastValue = field.getText();
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        boolean valid = false;

                        String text = field.getText();
                        text = text.replace(',', '.');

                        if (text.equals("")) {
                            parameterValid(false);
                            return;
                        }

                        try {
                            final double d = Double.parseDouble(text);

                            if ((d > 0) && (d < 1000)) {
                                if (!text.contains(".") || ((text.indexOf(".") + 3) >= text.length())) {
                                    valid = true;
                                }
                            }
                        } catch (NumberFormatException ex) {
                            // nothing to do
                        }

                        if (!valid) {
                            JOptionPane.showMessageDialog(
                                FgGerogaRsDialog.this,
                                "Eingabewert ist nicht zulässig !",
                                "Unzulässige Eingabe",
                                JOptionPane.WARNING_MESSAGE);
                            field.setText(lastValue);
                        } else {
                            parameterValid(false);
                        }
                    }
                });
        }

//        for (final JTextField field : allTxtBr) {
//            field.setInputVerifier(verifier);
//        }
//
//        txtVar2Br.setInputVerifier(verifier2Br);
//        txtVar2St.setInputVerifier(verifier2St);

        if (!modal) {
            // is not required, if the dialog is modal
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
                                            selectedThemeFeatureCount = refreshSelectedFeatureCount(
                                                    false);
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

        cbbFlaeche.setRenderer(new DefaultListCellRenderer() {

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
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   disableButton  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean parameterValid(final boolean disableButton) {
        Double lastValue = null;
        Double currentValue = null;
        boolean firstField = true;

        for (final JTextField text : allTxtBr) {
            if (firstField) {
                firstField = false;
                currentValue = getDouble(text);
                lastValue = getDouble(text);

                if (lastValue == null) {
                    if (disableButton) {
                        butOk.setEnabled(false);
                    }
                    return false;
                }
            } else {
                currentValue = getDouble(text);

                if (currentValue != null) {
                    if ((lastValue == null) || (currentValue <= lastValue)) {
                        if (disableButton) {
                            butOk.setEnabled(false);
                        }
                        return false;
                    }
                }
                lastValue = currentValue;
            }
        }
        butOk.setEnabled(true);

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double getDouble(final JTextField field) {
        String text = field.getText();
        text = text.replace(',', '.');

        if (text.equals("")) {
            return null;
        }

        try {
            final double d = Double.parseDouble(text);

            return d;
        } catch (NumberFormatException ex) {
            // nothing to do
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FgGerogaRsDialog getInstance() {
        return LazyInitializer.INSTANCE;
    }

    @Override
    public void setVisible(final boolean b) {
        if (b) {
            cancelled = true;
            selectedThemeFeatureCount = refreshSelectedFeatureCount(true);
        }
        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        final Object selectedObject = cbbFlaeche.getSelectedItem();
        cbbFlaeche.setModel(new DefaultComboBoxModel(
                new String[] {
                    NbBundle.getMessage(
                        PointInPolygonDialog.class,
                        "PointInPolygonDialog.setlayerModel.searchPolygonServices")
                }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final DefaultComboBoxModel model = new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "Polygon", "MultiPolygon" }).toArray(
                                    new AbstractFeatureService[0]));
                        model.insertElementAt(null, 0);
                        cbbFlaeche.setModel(model);
                        cbbFlaeche.setSelectedItem(selectedObject);
                    }
                });

        t.start();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtFile = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        ckbOffeneFg = new javax.swing.JCheckBox();
        ckbOffeneFgBreite = new javax.swing.JCheckBox();
        txtGerBr = new javax.swing.JTextField();
        labM = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel13 = new javax.swing.JLabel();
        ckbFgFlaechen = new javax.swing.JCheckBox();
        ckbStand = new javax.swing.JCheckBox();
        ckbSee = new javax.swing.JCheckBox();
        ckbKleinsee = new javax.swing.JCheckBox();
        ckbOstsee = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel14 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        ckbVar1 = new javax.swing.JCheckBox();
        txtBr1 = new javax.swing.JTextField();
        txtBr2 = new javax.swing.JTextField();
        txtBr3 = new javax.swing.JTextField();
        txtBr4 = new javax.swing.JTextField();
        txtBr5 = new javax.swing.JTextField();
        txtBr6 = new javax.swing.JTextField();
        txtBr7 = new javax.swing.JTextField();
        txtBr8 = new javax.swing.JTextField();
        txtBr9 = new javax.swing.JTextField();
        txtBr10 = new javax.swing.JTextField();
        ckbVar2 = new javax.swing.JCheckBox();
        txtVar2Br = new javax.swing.JTextField();
        labM1 = new javax.swing.JLabel();
        txtVar2St = new javax.swing.JTextField();
        labM2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabel15 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cbbFlaeche = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        cbbAttr1 = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        cbbAttr2 = new javax.swing.JComboBox();
        ckbFlSelection = new javax.swing.JCheckBox();
        ckbPerArea = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.butOk.text",
                new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(120, 29));
        butOk.setPreferredSize(new java.awt.Dimension(150, 29));
        butOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.setMinimumSize(new java.awt.Dimension(120, 29));
        butCancel.setPreferredSize(new java.awt.Dimension(150, 29));
        butCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butCancel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        txtFile.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtFile.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(txtFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel16,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel16.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 0, 0);
        jPanel3.add(jLabel16, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(filler1, gridBagConstraints);

        ckbOffeneFg.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbOffeneFg,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbOffeneFg.text",
                new Object[] {})); // NOI18N
        ckbOffeneFg.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbOffeneFgActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(ckbOffeneFg, gridBagConstraints);

        ckbOffeneFgBreite.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbOffeneFgBreite,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbOffeneFgBreite.text",
                new Object[] {})); // NOI18N
        ckbOffeneFgBreite.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbOffeneFgBreiteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(ckbOffeneFgBreite, gridBagConstraints);

        txtGerBr.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtGerBr.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtGerBr.text",
                new Object[] {})); // NOI18N
        txtGerBr.setPreferredSize(new java.awt.Dimension(50, 27));
        txtGerBr.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtGerBrFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(txtGerBr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labM,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.labM.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 10);
        getContentPane().add(labM, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel4.add(jSeparator1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel13,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel13.text",
                new Object[] {})); // NOI18N
        jLabel13.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel4.add(jLabel13, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel4, gridBagConstraints);

        ckbFgFlaechen.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFgFlaechen,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbFgFlaechen.text",
                new Object[] {})); // NOI18N
        ckbFgFlaechen.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbFgFlaechenActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(ckbFgFlaechen, gridBagConstraints);

        ckbStand.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbStand,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbStand.text",
                new Object[] {})); // NOI18N
        ckbStand.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbStandActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(ckbStand, gridBagConstraints);

        ckbSee.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSee,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbSee.text",
                new Object[] {})); // NOI18N
        ckbSee.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbSeeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 10, 10);
        getContentPane().add(ckbSee, gridBagConstraints);

        ckbKleinsee.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbKleinsee,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbKleinsee.text",
                new Object[] {})); // NOI18N
        ckbKleinsee.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbKleinseeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 25, 10, 10);
        getContentPane().add(ckbKleinsee, gridBagConstraints);

        ckbOstsee.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbOstsee,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbOstsee.text",
                new Object[] {})); // NOI18N
        ckbOstsee.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbOstseeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        getContentPane().add(ckbOstsee, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel5.add(jSeparator2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel14,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel14.text",
                new Object[] {})); // NOI18N
        jLabel14.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel5.add(jLabel14, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        ckbVar1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbVar1,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbVar1.text",
                new Object[] {})); // NOI18N
        ckbVar1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbVar1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        jPanel2.add(ckbVar1, gridBagConstraints);

        txtBr1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr1.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr1.text",
                new Object[] {})); // NOI18N
        txtBr1.setPreferredSize(new java.awt.Dimension(50, 27));
        txtBr1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtBr1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 5);
        jPanel2.add(txtBr1, gridBagConstraints);

        txtBr2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr2.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr2.text",
                new Object[] {})); // NOI18N
        txtBr2.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr2, gridBagConstraints);

        txtBr3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr3.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr3.text",
                new Object[] {})); // NOI18N
        txtBr3.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr3, gridBagConstraints);

        txtBr4.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr4.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr4.text",
                new Object[] {})); // NOI18N
        txtBr4.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr4, gridBagConstraints);

        txtBr5.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr5.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr5.text",
                new Object[] {})); // NOI18N
        txtBr5.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr5, gridBagConstraints);

        txtBr6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr6.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr6.text",
                new Object[] {})); // NOI18N
        txtBr6.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr6, gridBagConstraints);

        txtBr7.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr7.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr7.text",
                new Object[] {})); // NOI18N
        txtBr7.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr7, gridBagConstraints);

        txtBr8.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr8.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr8.text",
                new Object[] {})); // NOI18N
        txtBr8.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr8, gridBagConstraints);

        txtBr9.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr9.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr9.text",
                new Object[] {})); // NOI18N
        txtBr9.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr9, gridBagConstraints);

        txtBr10.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtBr10.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtBr10.text",
                new Object[] {})); // NOI18N
        txtBr10.setPreferredSize(new java.awt.Dimension(50, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtBr10, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbVar2,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbVar2.text",
                new Object[] {})); // NOI18N
        ckbVar2.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbVar2ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        jPanel2.add(ckbVar2, gridBagConstraints);

        txtVar2Br.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtVar2Br.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtVar2Br.text",
                new Object[] {})); // NOI18N
        txtVar2Br.setPreferredSize(new java.awt.Dimension(50, 27));
        txtVar2Br.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtVar2BrFocusLost(evt);
                }
            });
        txtVar2Br.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtVar2BrActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 5);
        jPanel2.add(txtVar2Br, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labM1,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.labM1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel2.add(labM1, gridBagConstraints);

        txtVar2St.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtVar2St.setText(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.txtVar2St.text",
                new Object[] {})); // NOI18N
        txtVar2St.setPreferredSize(new java.awt.Dimension(50, 27));
        txtVar2St.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtVar2StFocusLost(evt);
                }
            });
        txtVar2St.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtVar2StActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
        jPanel2.add(txtVar2St, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labM2,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.labM2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 10, 0);
        jPanel2.add(labM2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel5.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        jPanel6.add(jSeparator3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel15,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel15.text",
                new Object[] {})); // NOI18N
        jLabel15.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        jPanel6.add(jLabel15, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel3.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        jPanel7.add(jLabel3, gridBagConstraints);

        cbbFlaeche.setPreferredSize(new java.awt.Dimension(160, 27));
        cbbFlaeche.addItemListener(new java.awt.event.ItemListener() {

                @Override
                public void itemStateChanged(final java.awt.event.ItemEvent evt) {
                    cbbFlaecheItemStateChanged(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(cbbFlaeche, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel11,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel11.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(jLabel11, gridBagConstraints);

        cbbAttr1.setPreferredSize(new java.awt.Dimension(160, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(cbbAttr1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel12,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.jLabel12.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(jLabel12, gridBagConstraints);

        cbbAttr2.setPreferredSize(new java.awt.Dimension(160, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(cbbAttr2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFlSelection,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbFlSelection.text",
                new Object[] {})); // NOI18N
        ckbFlSelection.setEnabled(false);
        ckbFlSelection.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbFlSelectionActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        jPanel7.add(ckbFlSelection, gridBagConstraints);

        ckbPerArea.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerArea,
            org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbPerArea.text",
                new Object[] {})); // NOI18N
        ckbPerArea.setActionCommand(org.openide.util.NbBundle.getMessage(
                FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbPerArea.actionCommand",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 10);
        jPanel7.add(ckbPerArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel6.add(jPanel7, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel6, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        cancelled = true;
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        if (ckbVar1.isSelected() && !parameterValid(true)) {
            JOptionPane.showMessageDialog(
                this,
                "Die Breiten bei Variante 1 müssen aufsteigend sein.",
                "Fehlerhafte Werte",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!txtFile.getText().equals("") && checkValues()) {
            cancelled = false;
            setVisible(false);
            start();
        } else {
//            butFileActionPerformed(null);
        }
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbOffeneFgActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbOffeneFgActionPerformed
        CheckBoxCheck();
    }                                                                               //GEN-LAST:event_ckbOffeneFgActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void CheckBoxCheck() {
        int count = 0;

        if (ckbFgFlaechen.isSelected()) {
            ++count;
        }
        if (ckbOffeneFg.isSelected()) {
            ++count;
        }
        if (ckbOffeneFgBreite.isSelected()) {
            ++count;
        }
        if (ckbSee.isSelected()) {
            ++count;
        }
        if (ckbKleinsee.isSelected()) {
            ++count;
        }
        if (ckbOstsee.isSelected()) {
            ++count;
        }

        if (count > 1) {
            ckbFgFlaechen.setEnabled(true);
            ckbOffeneFg.setEnabled(true);
            ckbOffeneFgBreite.setEnabled(true);
            ckbSee.setEnabled(true);
            ckbKleinsee.setEnabled(true);
            ckbOstsee.setEnabled(true);
        } else if (count == 1) {
            if (ckbFgFlaechen.isSelected()) {
                ckbFgFlaechen.setEnabled(false);
            }
            if (ckbOffeneFg.isSelected()) {
                ckbOffeneFg.setEnabled(false);
            }
            if (ckbOffeneFgBreite.isSelected()) {
                ckbOffeneFgBreite.setEnabled(false);
            }
            if (ckbSee.isSelected()) {
                ckbSee.setEnabled(false);
            }
            if (ckbKleinsee.isSelected()) {
                ckbKleinsee.setEnabled(false);
            }
            if (ckbOstsee.isSelected()) {
                ckbOstsee.setEnabled(false);
            }
        } else if (count == 0) {
            ckbKleinsee.setSelected(true);
            ckbKleinsee.setEnabled(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbStandActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbStandActionPerformed
        ckbSee.setSelected(ckbStand.isSelected());
        ckbKleinsee.setSelected(ckbStand.isSelected());
        CheckBoxCheck();
    }                                                                            //GEN-LAST:event_ckbStandActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbOstseeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbOstseeActionPerformed
        CheckBoxCheck();
    }                                                                             //GEN-LAST:event_ckbOstseeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtBr1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtBr1ActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_txtBr1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtVar2BrActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtVar2BrActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_txtVar2BrActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbVar2ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbVar2ActionPerformed
        ckbVar1.setSelected(!ckbVar2.isSelected());
        enableVarTextFields();
    }                                                                           //GEN-LAST:event_ckbVar2ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtVar2StActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtVar2StActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_txtVar2StActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbbFlaecheItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbbFlaecheItemStateChanged
        refreshFieldModel();
        selectedThemeFeatureCount = refreshSelectedFeatureCount(false);
    }                                                                             //GEN-LAST:event_cbbFlaecheItemStateChanged

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbFlSelectionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbFlSelectionActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_ckbFlSelectionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbVar1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbVar1ActionPerformed
        ckbVar2.setSelected(!ckbVar1.isSelected());
        enableVarTextFields();
    }                                                                           //GEN-LAST:event_ckbVar1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbSeeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbSeeActionPerformed
        ckbStand.setSelected(ckbSee.isSelected() && ckbKleinsee.isSelected());
        CheckBoxCheck();
    }                                                                          //GEN-LAST:event_ckbSeeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbKleinseeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbKleinseeActionPerformed
        ckbStand.setSelected(ckbSee.isSelected() && ckbKleinsee.isSelected());
        CheckBoxCheck();
    }                                                                               //GEN-LAST:event_ckbKleinseeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtGerBrFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtGerBrFocusLost
        double br = 0.0;
        try {
            br = Double.parseDouble(txtGerBr.getText());

            if ((br > 1000) || (br < 0)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Eingabewert ist nicht zulässig !",
                    "Unzulässige Eingabe",
                    JOptionPane.WARNING_MESSAGE);
                txtGerBr.setText("6");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Eingabewert ist nicht zulässig !",
                "Unzulässige Eingabe",
                JOptionPane.WARNING_MESSAGE);
            txtGerBr.setText("6");
        }
    } //GEN-LAST:event_txtGerBrFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbOffeneFgBreiteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbOffeneFgBreiteActionPerformed
        CheckBoxCheck();
    }                                                                                     //GEN-LAST:event_ckbOffeneFgBreiteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbFgFlaechenActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbFgFlaechenActionPerformed
        CheckBoxCheck();
    }                                                                                 //GEN-LAST:event_ckbFgFlaechenActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtVar2BrFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtVar2BrFocusLost
        double br = 0.0;
        try {
            br = Double.parseDouble(txtVar2Br.getText());

            if ((br > 1000) || (br <= 0)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Eingabewert ist nicht zulässig !",
                    "Unzulässige Eingabe",
                    JOptionPane.WARNING_MESSAGE);
                txtVar2Br.setText("20");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Eingabewert ist nicht zulässig !",
                "Unzulässige Eingabe",
                JOptionPane.WARNING_MESSAGE);
            txtVar2Br.setText("20");
        }
    } //GEN-LAST:event_txtVar2BrFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtVar2StFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtVar2StFocusLost
        int br = 0;
        try {
            br = Integer.parseInt(txtVar2St.getText());

            if ((br > 1000) || (br <= 0)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Eingabewert ist nicht zulässig !",
                    "Unzulässige Eingabe",
                    JOptionPane.WARNING_MESSAGE);
                txtVar2St.setText("4");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Eingabewert ist nicht zulässig !",
                "Unzulässige Eingabe",
                JOptionPane.WARNING_MESSAGE);
            txtVar2St.setText("4");
        }
    } //GEN-LAST:event_txtVar2StFocusLost

    /**
     * DOCUMENT ME!
     */
    private void enableVarTextFields() {
        txtBr1.setEnabled(ckbVar1.isSelected());
        txtBr2.setEnabled(ckbVar1.isSelected());
        txtBr3.setEnabled(ckbVar1.isSelected());
        txtBr4.setEnabled(ckbVar1.isSelected());
        txtBr5.setEnabled(ckbVar1.isSelected());
        txtBr6.setEnabled(ckbVar1.isSelected());
        txtBr7.setEnabled(ckbVar1.isSelected());
        txtBr8.setEnabled(ckbVar1.isSelected());
        txtBr9.setEnabled(ckbVar1.isSelected());
        txtBr10.setEnabled(ckbVar1.isSelected());
        txtVar2Br.setEnabled(ckbVar2.isSelected());
        txtVar2St.setEnabled(ckbVar2.isSelected());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean checkValues() {
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    private void start() {
        final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                        .getInstance().getWatergisApp(),
                true,
                NbBundle.getMessage(FgGerogaRsDialog.class, "FgGerogaRsDialog.start().WaitingDialogThread"),
                null,
                100,
                true) {

                @Override
                protected H2FeatureService doInBackground() throws Exception {
                    Geometry totalGeom = null;
                    XBoundingBox bbox = null;
                    final boolean useAreas = cbbFlaeche.getSelectedIndex() != -1;
                    AbstractFeatureService service = null;
                    List<FeatureServiceFeature> areaFeatures = null;

                    if (useAreas) {
                        service = (AbstractFeatureService)cbbFlaeche.getSelectedItem();

                        if (ckbFlSelection.isSelected()) {
                            areaFeatures = Arrays.asList(getSelectedFl());
                        } else {
                            areaFeatures = service.getFeatureFactory()
                                        .createFeatures(service.getQuery(), null, null, 0, 0, null);
                        }
                        if (canceled) {
                            return null;
                        }
                        Geometry g = GeometryUtils.unionFeatureEnvelopes(areaFeatures);
                        if (canceled) {
                            return null;
                        }
                        g = g.buffer(getBiggestStreifen() + 1);
                        bbox = new XBoundingBox(g);
                    }

//                if (ckbOffeneFg.isSelected()) {
//                    MetaClass fgGerogMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_gerog");
//                    CidsLayer fgGerogLayer = new CidsLayer(fgGerogMc);
//                    fgGerogLayer.initAndWait();
//                    List<FeatureServiceFeature> fgGerogFeatures = fgGerogLayer.getFeatureFactory().createFeatures("typ = 'so'", bbox, null, 0, 0, null);
//                    totalGeom = GeometryUtils.unionFeatureGeometries(fgGerogFeatures);
//                }
//
//                if (ckbOffeneFgBreite.isSelected()) {
//                    MetaClass fgMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
//                    CidsLayer fgLayer = new CidsLayer(fgMc);
//                    fgLayer.initAndWait();
//                    List<FeatureServiceFeature> fgFeatures = fgLayer.getFeatureFactory().createFeatures(fgLayer.getQuery(), bbox, null, 0, 0, null);
//                    double br = 3;
//
//                    try {
//                        br = Double.parseDouble(txtGerBr.getText()) / 2;
//                    } catch (NumberFormatException e) {
//                        //nothing to do
//                    }
//
//                    for (FeatureServiceFeature feature : fgFeatures) {
//                        feature.setGeometry(feature.getGeometry().buffer(br));
//                    }
//
//                    if (!fgFeatures.isEmpty()) {
//                        if (totalGeom == null) {
//                            totalGeom = GeometryUtils.unionFeatureGeometries(fgFeatures);
//                        } else {
//                            totalGeom = totalGeom.union( GeometryUtils.unionFeatureGeometries(fgFeatures) );
//                        }
//                    }
//                }
//
//                if (ckbFgFlaechen.isSelected()) {
//                    MetaClass fgFlMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_fl");
//                    CidsLayer fgFlLayer = new CidsLayer(fgFlMc);
//                    fgFlLayer.initAndWait();
//                    List<FeatureServiceFeature> fgFlFeatures = fgFlLayer.getFeatureFactory().createFeatures(fgFlLayer.getQuery(), bbox, null, 0, 0, null);
//
//                    if (!fgFlFeatures.isEmpty()) {
//                        if (totalGeom == null) {
//                            totalGeom = GeometryUtils.unionFeatureGeometries(fgFlFeatures);
//                        } else {
//                            totalGeom = totalGeom.union( GeometryUtils.unionFeatureGeometries(fgFlFeatures) );
//                        }
//                    }
//                }
//
//                if (ckbSee.isSelected()) {
//                    MetaClass seeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.sg_see");
//                    CidsLayer seeLayer = new CidsLayer(seeMc);
//                    seeLayer.initAndWait();
//                    List<FeatureServiceFeature> seeFeatures = seeLayer.getFeatureFactory().createFeatures(seeLayer.getQuery(), bbox, null, 0, 0, null);
//
//                    if (!seeFeatures.isEmpty()) {
//                        if (totalGeom == null) {
//                            totalGeom = GeometryUtils.unionFeatureGeometries(seeFeatures);
//                        } else {
//                            totalGeom = totalGeom.union( GeometryUtils.unionFeatureGeometries(seeFeatures) );
//                        }
//                    }
//                }
//
//                if (ckbKleinsee.isSelected()) {
//                    MetaClass seeKlMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.sg_see_kl");
//                    CidsLayer seeLayer = new CidsLayer(seeKlMc);
//                    seeLayer.initAndWait();
//                    List<FeatureServiceFeature> seeFeatures = seeLayer.getFeatureFactory().createFeatures(seeLayer.getQuery(), bbox, null, 0, 0, null);
//
//                    if (!seeFeatures.isEmpty()) {
//                        if (totalGeom == null) {
//                            totalGeom = GeometryUtils.unionFeatureGeometries(seeFeatures);
//                        } else {
//                            totalGeom = totalGeom.union( GeometryUtils.unionFeatureGeometries(seeFeatures) );
//                        }
//                    }
//                }
//
//                if (ckbOstsee.isSelected()) {
//                    MetaClass ostseeMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.ezg_mv_ostsee");
//                    CidsLayer seeLayer = new CidsLayer(ostseeMc);
//                    seeLayer.initAndWait();
//                    List<FeatureServiceFeature> seeFeatures = seeLayer.getFeatureFactory().createFeatures(seeLayer.getQuery(), bbox, null, 0, 0, null);
//
//                    for (FeatureServiceFeature f: seeFeatures) {
//                        if (f.getGeometry() instanceof MultiPolygon) {
//                            f.setGeometry(removeHolesFromMultiPolygon((MultiPolygon)f.getGeometry()));
//                        }
//                    }
//
//                    if (!seeFeatures.isEmpty()) {
//                        if (totalGeom == null) {
//                            totalGeom = GeometryUtils.unionFeatureGeometries(seeFeatures);
//                        } else {
//                            totalGeom = totalGeom.union( GeometryUtils.unionFeatureGeometries(seeFeatures) );
//                        }
//                    }
//                }
                    double br = 0.0;
                    try {
                        br = Double.parseDouble(txtGerBr.getText()) / 2;
                    } catch (NumberFormatException e) {
                        // nothing to do
                    }
                    final Geometry bboxGeometry = ((bbox != null) ? bbox.getGeometry() : null);
                    final CidsServerSearch search = new PreparedRandstreifenGeoms(
                            bboxGeometry,
                            ckbOffeneFg.isSelected(),
                            ckbOffeneFgBreite.isSelected(),
                            ckbFgFlaechen.isSelected(),
                            ckbSee.isSelected(),
                            ckbKleinsee.isSelected(),
                            ckbOstsee.isSelected(),
                            br);

                    final User user = SessionManager.getSession().getUser();
                    final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(user, search);

                    if (canceled) {
                        return null;
                    }

                    if ((attributes != null) && !attributes.isEmpty()) {
                        if (!attributes.get(0).isEmpty() && (attributes.get(0).get(0) instanceof byte[])) {
                            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(
                                        PrecisionModel.FLOATING),
                                    CismapBroker.getInstance().getDefaultCrsAlias());
                            final WKBReader wkbReader = new WKBReader(geomFactory);
                            totalGeom = wkbReader.read((byte[])attributes.get(0).get(0));
                        }
                    }
                    if (canceled) {
                        return null;
                    }

                    if (bbox != null) {
                        return createStreifen(totalGeom.intersection(bbox.getGeometry()),
                                useAreas,
                                areaFeatures,
                                service);
                    } else {
                        return createStreifen(totalGeom, useAreas, areaFeatures, service);
                    }
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param   totalGeom    DOCUMENT ME!
                 * @param   useAreas     DOCUMENT ME!
                 * @param   areaFeature  DOCUMENT ME!
                 * @param   areaService  DOCUMENT ME!
                 *
                 * @return  DOCUMENT ME!
                 *
                 * @throws  Exception  DOCUMENT ME!
                 */
                private H2FeatureService createStreifen(final Geometry totalGeom,
                        final boolean useAreas,
                        final List<FeatureServiceFeature> areaFeature,
                        final AbstractFeatureService areaService) throws Exception {
                    Geometry oldRs = totalGeom;
                    H2FeatureService targetlayer = null;
                    final Timestamp time = new Timestamp(new Date().getTime());
                    final String user = SessionManager.getSession().getUser().getName();
                    String attr1String = null;
                    String attr2String = null;
                    final Double[] streifen = getStreifen();
                    int index = 0;

                    if (useAreas && ckbPerArea.isSelected()) {
                        wd.setMax(streifen.length);
                        for (final Double rs : streifen) {
                            wd.setText("Erstelle (Randstreifen) " + (++index) + " / " + streifen.length);
                            wd.setProgress(index - 1);
                            final Geometry currentRs = buffer(totalGeom, rs);
                            if (canceled) {
                                return null;
                            }
                            final Geometry rsWithOutMiddle = currentRs.difference(oldRs);
                            oldRs = currentRs;

                            for (final FeatureServiceFeature area : areaFeature) {
                                if (canceled) {
                                    return null;
                                }
                                final Geometry rsForArea = rsWithOutMiddle.intersection(area.getGeometry());
                                rsForArea.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

                                for (int geomNumber = 0; geomNumber < rsForArea.getNumGeometries(); ++geomNumber) {
                                    if (canceled) {
                                        return null;
                                    }
                                    final Geometry g = rsForArea.getGeometryN(geomNumber);
                                    if (!(g instanceof Polygon)) {
                                        continue;
                                    }

                                    if (targetlayer == null) {
                                        // create new layer
                                        attr1String = (String)cbbAttr1.getSelectedItem();
                                        attr2String = (String)cbbAttr2.getSelectedItem();
                                        final Map<String, FeatureServiceAttribute> attrMap =
                                            areaService.getLayerProperties()
                                                    .getFeatureService()
                                                    .getFeatureServiceAttributes();
                                        final FeatureServiceAttribute attr1 = attrMap.get(attr1String);
                                        final FeatureServiceAttribute attr2 = attrMap.get(attr2String);
                                        final List<FeatureServiceAttribute> serviceAttributes =
                                            new ArrayList<FeatureServiceAttribute>();
                                        FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                                                "id",
                                                String.valueOf(Types.INTEGER),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(
                                                "geom",
                                                String.valueOf(Types.GEOMETRY),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(attr1.getName(),
                                                attr1.getType(),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(attr2.getName(),
                                                attr2.getType(),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(
                                                "br_rs",
                                                String.valueOf(Types.DOUBLE),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(
                                                "fl_rs",
                                                String.valueOf(Types.DOUBLE),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(
                                                "fis_g_date",
                                                String.valueOf(Types.TIMESTAMP),
                                                true);
                                        serviceAttributes.add(serviceAttribute);
                                        serviceAttribute = new FeatureServiceAttribute(
                                                "fis_g_user",
                                                String.valueOf(Types.VARCHAR),
                                                true);
                                        serviceAttributes.add(serviceAttribute);

                                        final ArrayList<ArrayList> properties = new ArrayList<ArrayList>();
                                        final ArrayList propList = new ArrayList();
                                        propList.add(1);
                                        propList.add(g);
                                        propList.add(area.getProperty(attr1String));
                                        propList.add(area.getProperty(attr2String));
                                        propList.add(rs);
                                        propList.add(round(g.getArea()));
                                        propList.add(time);
                                        propList.add(user);
                                        properties.add(propList);

                                        targetlayer = FeatureServiceHelper.createNewService(
                                                properties,
                                                txtFile.getText(),
                                                serviceAttributes);
                                    } else {
                                        final JDBCFeature f = (JDBCFeature)targetlayer.getFeatureFactory()
                                                    .createNewFeature();
                                        f.setProperty("br_rs", rs);
                                        f.setProperty("geom", g);
                                        f.setProperty("fis_g_date", time);
                                        f.setProperty("fis_g_user", user);
                                        f.setGeometry(g);
                                        f.setProperty(attr1String, area.getProperty(attr1String));
                                        f.setProperty(attr2String, area.getProperty(attr2String));
                                        f.setProperty("fl_rs", round(g.getArea()));
                                        f.saveChangesWithoutReload();
                                    }
                                }
                            }
                        }
                    } else {
                        // keine Flächen oder Randstreifen pro Fläche deaktiviert
                        Geometry areaPolygon = null;
                        if (useAreas) {
                            areaPolygon = GeometryUtils.unionFeatureGeometries(areaFeature);
                        }
                        if (canceled) {
                            return null;
                        }

                        for (final Double rs : getStreifen()) {
                            if (canceled) {
                                return null;
                            }
                            final Geometry currentRs = buffer(totalGeom, rs);
                            Geometry rsWithOutMiddle = currentRs.difference(oldRs);
                            oldRs = currentRs;
                            if (canceled) {
                                return null;
                            }
                            if (useAreas) {
                                rsWithOutMiddle = rsWithOutMiddle.intersection(areaPolygon);
                            }
                            rsWithOutMiddle.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

                            for (int geomNumber = 0; geomNumber < rsWithOutMiddle.getNumGeometries(); ++geomNumber) {
                                if (canceled) {
                                    return null;
                                }
                                final Geometry g = rsWithOutMiddle.getGeometryN(geomNumber);
                                if (!(g instanceof Polygon)) {
                                    continue;
                                }

                                if (targetlayer == null) {
                                    // create new layer
                                    final List<FeatureServiceAttribute> serviceAttributes =
                                        new ArrayList<FeatureServiceAttribute>();
                                    FeatureServiceAttribute serviceAttribute = new FeatureServiceAttribute(
                                            "id",
                                            String.valueOf(Types.INTEGER),
                                            true);
                                    serviceAttributes.add(serviceAttribute);
                                    serviceAttribute = new FeatureServiceAttribute(
                                            "geom",
                                            String.valueOf(Types.GEOMETRY),
                                            true);
                                    serviceAttributes.add(serviceAttribute);
                                    serviceAttribute = new FeatureServiceAttribute(
                                            "br_rs",
                                            String.valueOf(Types.DOUBLE),
                                            true);
                                    serviceAttributes.add(serviceAttribute);
                                    serviceAttribute = new FeatureServiceAttribute(
                                            "fis_g_date",
                                            String.valueOf(Types.TIMESTAMP),
                                            true);
                                    serviceAttributes.add(serviceAttribute);
                                    serviceAttribute = new FeatureServiceAttribute(
                                            "fis_g_user",
                                            String.valueOf(Types.VARCHAR),
                                            true);
                                    serviceAttributes.add(serviceAttribute);

                                    final ArrayList<ArrayList> properties = new ArrayList<ArrayList>();
                                    final ArrayList propList = new ArrayList();
                                    propList.add(1);
                                    propList.add(g);
                                    propList.add(rs);
                                    propList.add(time);
                                    propList.add(user);
                                    properties.add(propList);

                                    targetlayer = FeatureServiceHelper.createNewService(
                                            properties,
                                            txtFile.getText(),
                                            serviceAttributes);
                                } else {
                                    final JDBCFeature f = (JDBCFeature)targetlayer.getFeatureFactory()
                                                .createNewFeature();
                                    f.setProperty("br_rs", rs);
                                    f.setProperty("geom", g);
                                    f.setProperty("fis_g_date", time);
                                    f.setProperty("fis_g_user", user);
                                    f.setGeometry(g);
                                    f.saveChangesWithoutReload();
                                }
                            }
                        }
                    }

                    return targetlayer;
                }

                @Override
                protected void done() {
                    try {
                        final H2FeatureService service = get();

                        if (service != null) {
                            CismapBroker.getInstance().getMappingComponent().getMappingModel().addLayer(service);
                        }
                    } catch (Exception e) {
                        LOG.error("Error during the theme creation", e);
                    }
                }
            };

        wdt.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mp  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry removeHolesFromMultiPolygon(final MultiPolygon mp) {
        final List<Polygon> polygons = new ArrayList<Polygon>();

        for (int i = 0; i < mp.getNumGeometries(); ++i) {
            final Polygon p = (Polygon)mp.getGeometryN(i);
            polygons.add(p.getFactory().createPolygon(p.getExteriorRing().getCoordinates()));
        }

        return mp.getFactory().createMultiPolygon(polygons.toArray(new Polygon[polygons.size()]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double round(final Double value) {
        if (value == null) {
            return null;
        } else {
            return Math.round(value * 100) / 100.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double[] getStreifen() {
        final List<Double> streifenList = new ArrayList<Double>();

        if (ckbVar1.isSelected()) {
            for (final JTextField field : allTxtBr) {
                try {
                    final double val = Double.parseDouble(field.getText());
                    streifenList.add(val);
                } catch (NumberFormatException e) {
                }
            }
        } else {
            int count = 0;
            double br = 0;

            try {
                count = Integer.parseInt(txtVar2St.getText());
            } catch (NumberFormatException e) {
            }
            try {
                br = Double.parseDouble(txtVar2Br.getText());
            } catch (NumberFormatException e) {
            }

            for (int i = 1; i <= count; ++i) {
                streifenList.add(i * (br / count));
            }
        }

        return streifenList.toArray(new Double[streifenList.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getBiggestStreifen() {
        double max = 0;

        for (final Double d : getStreifen()) {
            if (d > max) {
                max = d;
            }
        }

        return max;
    }

    /**
     * refreshes the ckbSelection1 label.
     *
     * @param   forceGuiRefresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int refreshSelectedFeatureCount(final boolean forceGuiRefresh) {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();

        if (cbbFlaeche.getSelectedItem() instanceof AbstractFeatureService) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures((AbstractFeatureService)cbbFlaeche.getSelectedItem()));
        }

        final int count = set.size();

        ckbFlSelection.setText(NbBundle.getMessage(FgGerogaRsDialog.class,
                "FgGerogaRsDialog.ckbFlSelection.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "FgGerogaRsDialog.refreshSelectedFeatureCount.text",
                        count));

        ckbFlSelection.setEnabled(true);

        if (forceGuiRefresh || (count != selectedThemeFeatureCount)) {
            ckbFlSelection.setSelected(count > 0);
        }

        if (count == 0) {
            ckbFlSelection.setSelected(false);
            ckbFlSelection.setEnabled(false);
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   g     DOCUMENT ME!
     * @param   dist  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Geometry buffer(final Geometry g, final double dist) throws Exception {
        Geometry bufferedGeometry = null;
        final CidsServerSearch search = new Buffer(g, dist);

        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);

        if ((attributes != null) && !attributes.isEmpty()) {
            if (!attributes.get(0).isEmpty() && (attributes.get(0).get(0) instanceof byte[])) {
                final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        CismapBroker.getInstance().getDefaultCrsAlias());
                final WKBReader wkbReader = new WKBReader(geomFactory);
                bufferedGeometry = wkbReader.read((byte[])attributes.get(0).get(0));
            }
        }

        return bufferedGeometry;
    }

    /**
     * DOCUMENT ME!
     */
    private void refreshFieldModel() {
        final AbstractFeatureService service = (AbstractFeatureService)cbbFlaeche.getSelectedItem();

        if (service != null) {
            final List<String> fields = FeatureServiceHelper.getAllFieldNames(service, null);
            final Object oldValue = cbbAttr1.getSelectedItem();
            final Object oldValue2 = cbbAttr2.getSelectedItem();
            cbbAttr1.setModel(new DefaultComboBoxModel(fields.toArray(new String[fields.size()])));
            cbbAttr2.setModel(new DefaultComboBoxModel(fields.toArray(new String[fields.size()])));
            cbbAttr1.setSelectedItem(oldValue);
            cbbAttr2.setSelectedItem(oldValue2);
        } else {
            cbbAttr1.setModel(new DefaultComboBoxModel());
            cbbAttr2.setModel(new DefaultComboBoxModel());
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature[] getSelectedFl() {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();
        final AbstractFeatureService service = (AbstractFeatureService)cbbFlaeche.getSelectedItem();

        set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
//
        return set.toArray(new FeatureServiceFeature[set.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature[] getSelectedGew() {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();

        for (final AbstractFeatureService service : getAllActiveFgBaServices()) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
        }

        return set.toArray(new FeatureServiceFeature[set.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<AbstractFeatureService> getAllActiveFgBaServices() {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        final ActiveLayerModel mappingModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap treeMap = mappingModel.getMapServices();
        final List<Integer> keyList = new ArrayList<Integer>(treeMap.keySet());
        final Iterator it = keyList.iterator();

        while (it.hasNext()) {
            final Object service = treeMap.get(it.next());

            if (service instanceof CidsLayer) {
                final CidsLayer featureService = (CidsLayer)service;

                if (featureService.getMetaClass().getTableName().equalsIgnoreCase("dlm25w.fg_ba")) {
                    try {
                        if (!featureService.isInitialized()) {
                            ((AbstractFeatureService)service).initAndWait();
                        }
                        serviceList.add(featureService);
                    } catch (Exception e) {
                        LOG.error("Error while initialising service", e);
                    }
                }
            }
        }

        return serviceList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<AbstractFeatureService> getAllActiveGmdServices() {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        final ActiveLayerModel mappingModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap treeMap = mappingModel.getMapServices();
        final List<Integer> keyList = new ArrayList<Integer>(treeMap.keySet());
        final Iterator it = keyList.iterator();

        while (it.hasNext()) {
            final Object service = treeMap.get(it.next());

            if (service instanceof CidsLayer) {
                final CidsLayer featureService = (CidsLayer)service;

                if (featureService.getMetaClass().getTableName().equalsIgnoreCase("dlm25w.vw_alk_gmd")) {
                    try {
                        if (!featureService.isInitialized()) {
                            ((AbstractFeatureService)service).initAndWait();
                        }
                        serviceList.add(featureService);
                    } catch (Exception e) {
                        LOG.error("Error while initialising service", e);
                    }
                }
            }
        }

        return serviceList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getPath() {
        return txtFile.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isCancelled() {
        return cancelled;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient FgGerogaRsDialog INSTANCE = new FgGerogaRsDialog(
                AppBroker.getInstance().getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Var1InputVerifier extends InputVerifier {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean verify(final JComponent input) {
            boolean valid = false;
            final JTextField field = (JTextField)input;

            String text = field.getText();
            text = text.replace(',', '.');

            try {
                final double d = Double.parseDouble(text);

                if ((d > 0) && (d < 1000)) {
                    if (!text.contains(".") || ((text.indexOf(".") + 3) >= text.length())) {
                        valid = true;
                    }
                }
            } catch (NumberFormatException e) {
                // nothing to do
            }

            return valid;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Var2BrInputVerifier extends InputVerifier {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean verify(final JComponent input) {
            boolean valid = false;
            final JTextField field = (JTextField)input;

            String text = field.getText();
            text = text.replace(',', '.');

            try {
                final double d = Double.parseDouble(text);

                if ((d > 0) && (d < 1000)) {
                    if (!text.contains(".") || ((text.indexOf(".") + 3) >= text.length())) {
                        valid = true;
                    }
                }
            } catch (NumberFormatException e) {
                // nothing to do
            }

            return valid;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class Var2StInputVerifier extends InputVerifier {

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean verify(final JComponent input) {
            boolean valid = false;
            final JTextField field = (JTextField)input;

            String text = field.getText();
            text = text.replace(',', '.');

            try {
                final int i = Integer.parseInt(text);

                if ((i > 0) && (i <= 100)) {
                    valid = true;
                }
            } catch (NumberFormatException e) {
                // nothing to do
            }

            return valid;
        }
    }
}
