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

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.RestrictedFileSystemView;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.WatergisApp;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOFlaechenReportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOFlaechenReportDialog.class);

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
    private int selectedGewThemeFeatureCount = -1;
    private String lastPath = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butFile;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbbAttr1;
    private javax.swing.JComboBox cbbAttr2;
    private javax.swing.JComboBox cbbFlaeche;
    private javax.swing.JCheckBox ckb1501;
    private javax.swing.JCheckBox ckb1502;
    private javax.swing.JCheckBox ckb1503;
    private javax.swing.JCheckBox ckb1504;
    private javax.swing.JCheckBox ckb1505;
    private javax.swing.JCheckBox ckbFlSelection;
    private javax.swing.JCheckBox ckbGewSelection;
    private javax.swing.JCheckBox ckbPerGew;
    private javax.swing.JCheckBox ckbPerPart;
    private javax.swing.JCheckBox ckbPerPartProf;
    private javax.swing.JCheckBox ckbSumGu;
    private javax.swing.JCheckBox ckbWdm;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private GerinneOFlaechenReportDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        txtFile.setText(DownloadManager.instance().getDestinationDirectory().getPath());

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
                                            selectedGewThemeFeatureCount = refreshSelectedGewFeatureCount(
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
     * @return  DOCUMENT ME!
     */
    public static GerinneOFlaechenReportDialog getInstance() {
        return LazyInitializer.INSTANCE;
    }

    @Override
    public void setVisible(final boolean b) {
        if (b) {
            cancelled = true;
            selectedThemeFeatureCount = refreshSelectedFeatureCount(true);
            selectedGewThemeFeatureCount = refreshSelectedGewFeatureCount(true);
        }
        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        cbbFlaeche.setModel(new DefaultComboBoxModel(
                new String[] {
                    NbBundle.getMessage(
                        PointInPolygonDialog.class,
                        "PointInPolygonDialog.setlayerModel.searchPolygonServices")
                }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbbFlaeche.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "Polygon", "MultiPolygon" }).toArray(
                                    new AbstractFeatureService[0])));
                        cbbFlaeche.setSelectedItem(null);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel8 = new javax.swing.JPanel();
        ckb1501 = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtFile = new javax.swing.JTextField();
        butFile = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        jLabel2 = new javax.swing.JLabel();
        ckbFlSelection = new javax.swing.JCheckBox();
        ckb1502 = new javax.swing.JCheckBox();
        ckb1503 = new javax.swing.JCheckBox();
        ckb1505 = new javax.swing.JCheckBox();
        ckbSumGu = new javax.swing.JCheckBox();
        ckbWdm = new javax.swing.JCheckBox();
        ckb1504 = new javax.swing.JCheckBox();
        ckbGewSelection = new javax.swing.JCheckBox();
        ckbPerGew = new javax.swing.JCheckBox();
        ckbPerPart = new javax.swing.JCheckBox();
        ckbPerPartProf = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        cbbFlaeche = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        cbbAttr1 = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        cbbAttr2 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        ckb1501.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1501,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckb1501.text",
                new Object[] {})); // NOI18N
        ckb1501.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1501.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1501.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1501, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.butOk.text",
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
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.butCancel.text",
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
        gridBagConstraints.gridy = 27;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel8.add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        txtFile.setText(org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.txtFile.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(txtFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butFile,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.butFile.text",
                new Object[] {})); // NOI18N
        butFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(butFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        jPanel8.add(filler1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(jLabel2, gridBagConstraints);

        ckbFlSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFlSelection,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GemeindeReportDialog.ckbGemSelektion.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(ckbFlSelection, gridBagConstraints);

        ckb1502.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1502,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckb1502.text",
                new Object[] {})); // NOI18N
        ckb1502.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1502.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1502.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1502, gridBagConstraints);

        ckb1503.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1503,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckb1503.text",
                new Object[] {})); // NOI18N
        ckb1503.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1503.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1503.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1503, gridBagConstraints);

        ckb1505.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1505,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckb1505.text",
                new Object[] {})); // NOI18N
        ckb1505.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1505.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1505.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1505, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSumGu,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbSumGu.text",
                new Object[] {})); // NOI18N
        ckbSumGu.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSumGu.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSumGu.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbSumGu.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbSumGuActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckbSumGu, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWdm,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbWdm.text",
                new Object[] {})); // NOI18N
        ckbWdm.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbWdm.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbWdm.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbWdm.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbWdmActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 26;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 15, 10);
        jPanel8.add(ckbWdm, gridBagConstraints);

        ckb1504.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1504,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckb1504.text",
                new Object[] {})); // NOI18N
        ckb1504.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1504.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1504.setPreferredSize(new java.awt.Dimension(100, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1504, gridBagConstraints);

        ckbGewSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGewSelection,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbGewSelection.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(ckbGewSelection, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerGew,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbPerGew.text",
                new Object[] {})); // NOI18N
        ckbPerGew.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbPerGew.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbPerGew.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbPerGew.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbPerGewActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckbPerGew, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerPart,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbPerPart.text",
                new Object[] {})); // NOI18N
        ckbPerPart.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbPerPart.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbPerPart.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbPerPart.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbPerPartActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 30, 5, 10);
        jPanel8.add(ckbPerPart, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerPartProf,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.ckbPerPartProf.text",
                new Object[] {})); // NOI18N
        ckbPerPartProf.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbPerPartProf.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbPerPartProf.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbPerPartProf.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbPerPartProfActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 40, 15, 10);
        jPanel8.add(ckbPerPartProf, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel8,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.jLabel8.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jLabel8, gridBagConstraints);

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
        jPanel4.add(cbbFlaeche, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel11,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.jLabel11.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jLabel11, gridBagConstraints);

        cbbAttr1.setPreferredSize(new java.awt.Dimension(160, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(cbbAttr1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel12,
            org.openide.util.NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneOFlaechenReportDialog.jLabel12.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jLabel12, gridBagConstraints);

        cbbAttr2.setPreferredSize(new java.awt.Dimension(160, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(cbbAttr2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel8.add(jPanel4, gridBagConstraints);

        jScrollPane1.setViewportView(jPanel8);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jScrollPane1, gridBagConstraints);

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
        if (!txtFile.getText().equals("")) {
            cancelled = false;
            setVisible(false);
        } else {
            butFileActionPerformed(null);
        }
    }                                                                         //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butFileActionPerformed
        JFileChooser fc;

        try {
            fc = new JFileChooser((lastPath == null) ? DownloadManager.instance().getDestinationDirectory().toString()
                                                     : lastPath);
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS(), new RestrictedFileSystemView());
        }

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return NbBundle.getMessage(
                            GerinneOFlaechenReportDialog.class,
                            "GewaesserReportDialog.butFileActionPerformed().getDescription()");
                }
            });

        final int ans = fc.showSaveDialog(this);

        if (ans == JFileChooser.APPROVE_OPTION) {
            txtFile.setText(fc.getSelectedFile().getAbsolutePath());
            lastPath = fc.getSelectedFile().getAbsolutePath();
        }
    } //GEN-LAST:event_butFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbWdmActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbWdmActionPerformed
        if (ckbWdm.isSelected()) {
            ckbSumGu.setSelected(true);
        }
    }                                                                          //GEN-LAST:event_ckbWdmActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbPerPartActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbPerPartActionPerformed
        if (ckbPerPart.isSelected()) {
            ckbPerGew.setSelected(true);
        }
        if (!ckbPerPart.isSelected()) {
            ckbPerPartProf.setSelected(false);
        }
    }                                                                              //GEN-LAST:event_ckbPerPartActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbPerPartProfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbPerPartProfActionPerformed
        if (ckbPerPartProf.isSelected()) {
            ckbPerGew.setSelected(true);
            ckbPerPart.setSelected(true);
        }
    }                                                                                  //GEN-LAST:event_ckbPerPartProfActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbPerGewActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbPerGewActionPerformed
        if (!ckbPerGew.isSelected()) {
            ckbPerPartProf.setSelected(false);
            ckbPerPart.setSelected(false);
        }
    }                                                                             //GEN-LAST:event_ckbPerGewActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbSumGuActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbSumGuActionPerformed
        if (!ckbSumGu.isSelected()) {
            ckbWdm.setSelected(false);
        }
    }                                                                            //GEN-LAST:event_ckbSumGuActionPerformed

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
     */
    private void refreshFieldModel() {
        final AbstractFeatureService service = (AbstractFeatureService)cbbFlaeche.getSelectedItem();

        if (service != null) {
            final List<String> fields = FeatureServiceHelper.getAllFieldNames(service, null);
            cbbAttr1.setModel(new DefaultComboBoxModel(fields.toArray(new String[fields.size()])));
            cbbAttr2.setModel(new DefaultComboBoxModel(fields.toArray(new String[fields.size()])));
        } else {
            cbbAttr1.setModel(new DefaultComboBoxModel());
            cbbAttr2.setModel(new DefaultComboBoxModel());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAttr1() {
        return String.valueOf(cbbAttr1.getSelectedItem());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAttr2() {
        return String.valueOf(cbbAttr2.getSelectedItem());
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
        } else {
            return -1;
        }

        final int count = set.size();

        ckbFlSelection.setText(NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "GemeindeReportDialog.ckbGemSelektion.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "FlaechenReportDialog.refreshSelectedFeatureCount.text",
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
     * refreshes the ckbSelection1 label.
     *
     * @param   forceGuiRefresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int refreshSelectedGewFeatureCount(final boolean forceGuiRefresh) {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();

        for (final AbstractFeatureService service : getAllActiveFgBaServices()) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
        }

        final int count = set.size();

        ckbGewSelection.setText(NbBundle.getMessage(
                GerinneOFlaechenReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbGewSelection.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GewaesserReportDialog.refreshSelectedFeatureCount.text",
                        count));

        ckbGewSelection.setEnabled(true);

        if (forceGuiRefresh || (count != selectedGewThemeFeatureCount)) {
            ckbGewSelection.setSelected(count > 0);
        }

        if (count == 0) {
            ckbGewSelection.setSelected(false);
            ckbGewSelection.setEnabled(false);
        }

        return count;
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

        return set.toArray(new FeatureServiceFeature[set.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public AbstractFeatureService getFlaechenService() {
        return (AbstractFeatureService)cbbFlaeche.getSelectedItem();
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
     * @return  the ckbSelection1
     */
    public boolean isSelectionFl() {
        return ckbFlSelection.isSelected();
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

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isPerGew() {
        return ckbPerGew.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isPerPart() {
        return ckbPerPart.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isPerPartProf() {
        return ckbPerPartProf.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isSumGu() {
        return ckbSumGu.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isPerWdm() {
        return ckbWdm.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckb1501
     */
    public boolean is1501() {
        return ckb1501.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean is1502() {
        return ckb1502.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean is1503() {
        return ckb1503.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean is1504() {
        return ckb1504.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean is1505() {
        return ckb1505.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSelection1
     */
    public boolean isSelectionGmd() {
        return ckbFlSelection.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSelection1
     */
    public boolean isSelectionGew() {
        return ckbGewSelection.isSelected();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient GerinneOFlaechenReportDialog INSTANCE = new GerinneOFlaechenReportDialog(
                AppBroker.getInstance().getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
