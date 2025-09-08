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

import java.awt.EventQueue;

import java.io.File;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
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
public class GerinneGeschlGemeindeReportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGeschlGemeindeReportDialog.class);

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum Art {

        //~ Enum constants -----------------------------------------------------

        rl, d, due
    }

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
    private int selectedGewThemeFeatureCount = -1;
    private String lastPath = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butFile;
    private javax.swing.JButton butOk;
    private javax.swing.JCheckBox ckb1501;
    private javax.swing.JCheckBox ckb1502;
    private javax.swing.JCheckBox ckb1503;
    private javax.swing.JCheckBox ckb1504;
    private javax.swing.JCheckBox ckb1505;
    private javax.swing.JCheckBox ckbArt;
    private javax.swing.JCheckBox ckbArtD;
    private javax.swing.JCheckBox ckbArtDue;
    private javax.swing.JCheckBox ckbArtRl;
    private javax.swing.JCheckBox ckbDim;
    private javax.swing.JCheckBox ckbFach;
    private javax.swing.JCheckBox ckbGemSelection;
    private javax.swing.JCheckBox ckbGewSelection;
    private javax.swing.JCheckBox ckbOhneDimension;
    private javax.swing.JCheckBox ckbOhneKlasse;
    private javax.swing.JCheckBox ckbPerGew;
    private javax.swing.JCheckBox ckbPerObj;
    private javax.swing.JCheckBox ckbSumGu;
    private javax.swing.JCheckBox ckbTf;
    private javax.swing.JCheckBox ckbWdmSeparated;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtDim1;
    private javax.swing.JTextField txtDim10;
    private javax.swing.JTextField txtDim2;
    private javax.swing.JTextField txtDim3;
    private javax.swing.JTextField txtDim4;
    private javax.swing.JTextField txtDim5;
    private javax.swing.JTextField txtDim6;
    private javax.swing.JTextField txtDim7;
    private javax.swing.JTextField txtDim8;
    private javax.swing.JTextField txtDim9;
    private javax.swing.JTextField txtFile;
    private javax.swing.JTextField txtTf1;
    private javax.swing.JTextField txtTf10;
    private javax.swing.JTextField txtTf2;
    private javax.swing.JTextField txtTf3;
    private javax.swing.JTextField txtTf4;
    private javax.swing.JTextField txtTf5;
    private javax.swing.JTextField txtTf6;
    private javax.swing.JTextField txtTf7;
    private javax.swing.JTextField txtTf8;
    private javax.swing.JTextField txtTf9;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private GerinneGeschlGemeindeReportDialog(final java.awt.Frame parent, final boolean modal) {
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
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static GerinneGeschlGemeindeReportDialog getInstance() {
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
        ckbGemSelection = new javax.swing.JCheckBox();
        ckb1502 = new javax.swing.JCheckBox();
        ckb1503 = new javax.swing.JCheckBox();
        ckb1505 = new javax.swing.JCheckBox();
        ckbArt = new javax.swing.JCheckBox();
        ckbTf = new javax.swing.JCheckBox();
        ckbSumGu = new javax.swing.JCheckBox();
        ckbWdmSeparated = new javax.swing.JCheckBox();
        ckb1504 = new javax.swing.JCheckBox();
        ckbGewSelection = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        ckbArtRl = new javax.swing.JCheckBox();
        ckbArtD = new javax.swing.JCheckBox();
        ckbArtDue = new javax.swing.JCheckBox();
        ckbOhneKlasse = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtTf1 = new javax.swing.JTextField();
        txtTf2 = new javax.swing.JTextField();
        txtTf3 = new javax.swing.JTextField();
        txtTf4 = new javax.swing.JTextField();
        txtTf5 = new javax.swing.JTextField();
        txtTf6 = new javax.swing.JTextField();
        txtTf7 = new javax.swing.JTextField();
        txtTf8 = new javax.swing.JTextField();
        txtTf9 = new javax.swing.JTextField();
        txtTf10 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        ckbOhneDimension = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        txtDim1 = new javax.swing.JTextField();
        txtDim2 = new javax.swing.JTextField();
        txtDim3 = new javax.swing.JTextField();
        txtDim4 = new javax.swing.JTextField();
        txtDim5 = new javax.swing.JTextField();
        txtDim6 = new javax.swing.JTextField();
        txtDim7 = new javax.swing.JTextField();
        txtDim8 = new javax.swing.JTextField();
        txtDim9 = new javax.swing.JTextField();
        txtDim10 = new javax.swing.JTextField();
        ckbDim = new javax.swing.JCheckBox();
        ckbPerGew = new javax.swing.JCheckBox();
        ckbPerObj = new javax.swing.JCheckBox();
        ckbFach = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel8.setLayout(new java.awt.GridBagLayout());

        ckb1501.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1501,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckb1501.text",
                new Object[] {})); // NOI18N
        ckb1501.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1501.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1501.setPreferredSize(new java.awt.Dimension(100, 24));
        ckb1501.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckb1501ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1501, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.butOk.text",
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
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.butCancel.text",
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
        jPanel8.add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        txtFile.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtFile.text",
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
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.butFile.text",
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
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(jLabel2, gridBagConstraints);

        ckbGemSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGemSelection,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbGemSelektion.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(ckbGemSelection, gridBagConstraints);

        ckb1502.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1502,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckb1502.text",
                new Object[] {})); // NOI18N
        ckb1502.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1502.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1502.setPreferredSize(new java.awt.Dimension(100, 24));
        ckb1502.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckb1502ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1502, gridBagConstraints);

        ckb1503.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1503,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckb1503.text",
                new Object[] {})); // NOI18N
        ckb1503.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1503.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1503.setPreferredSize(new java.awt.Dimension(100, 24));
        ckb1503.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckb1503ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1503, gridBagConstraints);

        ckb1505.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1505,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckb1505.text",
                new Object[] {})); // NOI18N
        ckb1505.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1505.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1505.setPreferredSize(new java.awt.Dimension(100, 24));
        ckb1505.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckb1505ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1505, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbArt,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbArt.text",
                new Object[] {})); // NOI18N
        ckbArt.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbArt.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbArt.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbArt.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbArtActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        jPanel8.add(ckbArt, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbTf,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbTf.text",
                new Object[] {})); // NOI18N
        ckbTf.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbTf.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbTf.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbTf.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbTfActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
        jPanel8.add(ckbTf, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSumGu,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbSumGu.text",
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
        gridBagConstraints.gridy = 23;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        jPanel8.add(ckbSumGu, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWdmSeparated,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbWdmSeparated.text",
                new Object[] {})); // NOI18N
        ckbWdmSeparated.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbWdmSeparated.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbWdmSeparated.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbWdmSeparated.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbWdmSeparatedActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        jPanel8.add(ckbWdmSeparated, gridBagConstraints);

        ckb1504.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1504,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckb1504.text",
                new Object[] {})); // NOI18N
        ckb1504.setMaximumSize(new java.awt.Dimension(100, 24));
        ckb1504.setMinimumSize(new java.awt.Dimension(100, 24));
        ckb1504.setPreferredSize(new java.awt.Dimension(100, 24));
        ckb1504.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckb1504ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckb1504, gridBagConstraints);

        ckbGewSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGewSelection,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbGewSelection.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(ckbGewSelection, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel3.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel8.add(jLabel3, gridBagConstraints);

        ckbArtRl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbArtRl,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbArtRl.text",
                new Object[] {})); // NOI18N
        ckbArtRl.setMaximumSize(new java.awt.Dimension(100, 24));
        ckbArtRl.setMinimumSize(new java.awt.Dimension(100, 24));
        ckbArtRl.setPreferredSize(new java.awt.Dimension(100, 24));
        ckbArtRl.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbArtRlActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckbArtRl, gridBagConstraints);

        ckbArtD.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbArtD,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbArtD.text",
                new Object[] {})); // NOI18N
        ckbArtD.setMaximumSize(new java.awt.Dimension(100, 24));
        ckbArtD.setMinimumSize(new java.awt.Dimension(100, 24));
        ckbArtD.setPreferredSize(new java.awt.Dimension(100, 24));
        ckbArtD.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbArtDActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckbArtD, gridBagConstraints);

        ckbArtDue.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbArtDue,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbArtDue.text",
                new Object[] {})); // NOI18N
        ckbArtDue.setMaximumSize(new java.awt.Dimension(100, 24));
        ckbArtDue.setMinimumSize(new java.awt.Dimension(100, 24));
        ckbArtDue.setPreferredSize(new java.awt.Dimension(100, 24));
        ckbArtDue.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbArtDueActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel8.add(ckbArtDue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbOhneKlasse,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbOhneKlasse.text",
                new Object[] {})); // NOI18N
        ckbOhneKlasse.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbOhneKlasse.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbOhneKlasse.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbOhneKlasse.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbOhneKlasseActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(ckbOhneKlasse, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel4.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel8.add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel5.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel8.add(jLabel5, gridBagConstraints);

        txtTf1.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf1.text",
                new Object[] {})); // NOI18N
        txtTf1.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf1, gridBagConstraints);

        txtTf2.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf2.text",
                new Object[] {})); // NOI18N
        txtTf2.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf2, gridBagConstraints);

        txtTf3.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf3.text",
                new Object[] {})); // NOI18N
        txtTf3.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf3, gridBagConstraints);

        txtTf4.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf4.text",
                new Object[] {})); // NOI18N
        txtTf4.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf4, gridBagConstraints);

        txtTf5.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf5.text",
                new Object[] {})); // NOI18N
        txtTf5.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf5, gridBagConstraints);

        txtTf6.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf6.text",
                new Object[] {})); // NOI18N
        txtTf6.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf6, gridBagConstraints);

        txtTf7.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf7.text",
                new Object[] {})); // NOI18N
        txtTf7.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf7, gridBagConstraints);

        txtTf8.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf8.text",
                new Object[] {})); // NOI18N
        txtTf8.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf8, gridBagConstraints);

        txtTf9.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf9.text",
                new Object[] {})); // NOI18N
        txtTf9.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf9, gridBagConstraints);

        txtTf10.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtTf10.text",
                new Object[] {})); // NOI18N
        txtTf10.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtTf10, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel6,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel6.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel8.add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbOhneDimension,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbOhneDimension.text",
                new Object[] {})); // NOI18N
        ckbOhneDimension.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbOhneDimension.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbOhneDimension.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbOhneDimension.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbOhneDimensionActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(ckbOhneDimension, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel7,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.jLabel7.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 10, 10);
        jPanel8.add(jLabel7, gridBagConstraints);

        txtDim1.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim1.text",
                new Object[] {})); // NOI18N
        txtDim1.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim1, gridBagConstraints);

        txtDim2.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim2.text",
                new Object[] {})); // NOI18N
        txtDim2.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim2, gridBagConstraints);

        txtDim3.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim3.text",
                new Object[] {})); // NOI18N
        txtDim3.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim3, gridBagConstraints);

        txtDim4.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim4.text",
                new Object[] {})); // NOI18N
        txtDim4.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim4, gridBagConstraints);

        txtDim5.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim5.text",
                new Object[] {})); // NOI18N
        txtDim5.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim5, gridBagConstraints);

        txtDim6.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim6.text",
                new Object[] {})); // NOI18N
        txtDim6.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        jPanel8.add(txtDim6, gridBagConstraints);

        txtDim7.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim7.text",
                new Object[] {})); // NOI18N
        txtDim7.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim7, gridBagConstraints);

        txtDim8.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim8.text",
                new Object[] {})); // NOI18N
        txtDim8.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim8, gridBagConstraints);

        txtDim9.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim9.text",
                new Object[] {})); // NOI18N
        txtDim9.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim9, gridBagConstraints);

        txtDim10.setText(org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.txtDim10.text",
                new Object[] {})); // NOI18N
        txtDim10.setPreferredSize(new java.awt.Dimension(50, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 10, 10);
        jPanel8.add(txtDim10, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDim,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbDim.text",
                new Object[] {})); // NOI18N
        ckbDim.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbDim.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbDim.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbDim.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbDimActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
        jPanel8.add(ckbDim, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerGew,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbPerGew.text",
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
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 15, 10);
        jPanel8.add(ckbPerGew, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPerObj,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbPerObj.text",
                new Object[] {})); // NOI18N
        ckbPerObj.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbPerObj.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbPerObj.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbPerObj.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbPerObjActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        jPanel8.add(ckbPerObj, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFach,
            org.openide.util.NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbFach.text",
                new Object[] {})); // NOI18N
        ckbFach.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbFach.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbFach.setPreferredSize(new java.awt.Dimension(350, 24));
        ckbFach.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbFachActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        jPanel8.add(ckbFach, gridBagConstraints);

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
                            GerinneGeschlGemeindeReportDialog.class,
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
    private void ckbWdmSeparatedActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbWdmSeparatedActionPerformed
        if (ckbWdmSeparated.isSelected()) {
            ckbSumGu.setSelected(true);
        }
    }                                                                                   //GEN-LAST:event_ckbWdmSeparatedActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbTfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbTfActionPerformed
    }                                                                         //GEN-LAST:event_ckbTfActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbArtActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbArtActionPerformed
    }                                                                          //GEN-LAST:event_ckbArtActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbOhneKlasseActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbOhneKlasseActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_ckbOhneKlasseActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbOhneDimensionActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbOhneDimensionActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_ckbOhneDimensionActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbDimActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbDimActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_ckbDimActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbPerObjActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbPerObjActionPerformed
        if (ckbPerObj.isSelected()) {
            ckbPerGew.setSelected(true);
        }
        if (!ckbPerObj.isSelected()) {
            ckbFach.setSelected(false);
        }
    }                                                                             //GEN-LAST:event_ckbPerObjActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbFachActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbFachActionPerformed
        if (ckbFach.isSelected()) {
            ckbPerGew.setSelected(true);
            ckbPerObj.setSelected(true);
        }
    }                                                                           //GEN-LAST:event_ckbFachActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbPerGewActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbPerGewActionPerformed
        if (!ckbPerGew.isSelected()) {
            ckbFach.setSelected(false);
            ckbPerObj.setSelected(false);
        }
    }                                                                             //GEN-LAST:event_ckbPerGewActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbSumGuActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbSumGuActionPerformed
        if (!ckbSumGu.isSelected()) {
            ckbWdmSeparated.setSelected(false);
        }
    }                                                                            //GEN-LAST:event_ckbSumGuActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckb1501ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckb1501ActionPerformed
        deactivateWDM();
    }                                                                           //GEN-LAST:event_ckb1501ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckb1502ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckb1502ActionPerformed
        deactivateWDM();
    }                                                                           //GEN-LAST:event_ckb1502ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckb1503ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckb1503ActionPerformed
        deactivateWDM();
    }                                                                           //GEN-LAST:event_ckb1503ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckb1504ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckb1504ActionPerformed
        deactivateWDM();
    }                                                                           //GEN-LAST:event_ckb1504ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckb1505ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckb1505ActionPerformed
        deactivateWDM();
    }                                                                           //GEN-LAST:event_ckb1505ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbArtRlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbArtRlActionPerformed
        deactivateTyp();
    }                                                                            //GEN-LAST:event_ckbArtRlActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbArtDActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbArtDActionPerformed
        deactivateTyp();
    }                                                                           //GEN-LAST:event_ckbArtDActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbArtDueActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbArtDueActionPerformed
        deactivateTyp();
    }                                                                             //GEN-LAST:event_ckbArtDueActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void deactivateWDM() {
        final JCheckBox[] boxes = { ckb1501, ckb1502, ckb1503, ckb1504, ckb1505 };
        int count = 0;

        for (final JCheckBox box : boxes) {
            if (box.isSelected()) {
                ++count;
            }
        }
        if (count == 1) {
            for (final JCheckBox box : boxes) {
                if (box.isSelected()) {
                    box.setEnabled(false);
                }
            }
        } else {
            for (final JCheckBox box : boxes) {
                if (!box.isEnabled()) {
                    box.setEnabled(true);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void deactivateTyp() {
        final JCheckBox[] boxes = { ckbArtD, ckbArtDue, ckbArtRl };
        int count = 0;

        for (final JCheckBox box : boxes) {
            if (box.isSelected()) {
                ++count;
            }
        }
        if (count == 1) {
            for (final JCheckBox box : boxes) {
                if (box.isSelected()) {
                    box.setEnabled(false);
                }
            }
        } else {
            for (final JCheckBox box : boxes) {
                if (!box.isEnabled()) {
                    box.setEnabled(true);
                }
            }
        }
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

        for (final AbstractFeatureService service : getAllActiveGmdServices()) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
        }

        final int count = set.size();

        ckbGemSelection.setText(NbBundle.getMessage(
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbGemSelektion.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GerinneGeschlGemeindeReportDialog.refreshSelectedFeatureCount.text",
                        count));

        ckbGemSelection.setEnabled(true);

        if (forceGuiRefresh || (count != selectedThemeFeatureCount)) {
            ckbGemSelection.setSelected(count > 0);
        }

        if (count == 0) {
            ckbGemSelection.setSelected(false);
            ckbGemSelection.setEnabled(false);
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
                GerinneGeschlGemeindeReportDialog.class,
                "GerinneGeschlGemeindeReportDialog.ckbGewSelection.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GerinneGeschlGemeindeReportDialog.refreshSelectedGewFeatureCount.text",
                        count));

        ckbGewSelection.setEnabled(true);

        if (forceGuiRefresh || (count != selectedThemeFeatureCount)) {
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
    public FeatureServiceFeature[] getSelectedGmd() {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();

        for (final AbstractFeatureService service : getAllActiveGmdServices()) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
        }

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
    public boolean isPerObject() {
        return ckbPerObj.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isAllDataPerObject() {
        return ckbFach.isSelected();
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
        return ckbWdmSeparated.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbAbschn
     */
    public boolean isPerArt() {
        return ckbArt.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Art> getArt() {
        if (!ckbArt.isSelected()) {
            return null;
        } else {
            final List<Art> artList = new ArrayList<Art>();

            if (ckbArtD.isSelected()) {
                artList.add(Art.d);
            }

            if (ckbArtDue.isSelected()) {
                artList.add(Art.due);
            }

            if (ckbArtRl.isSelected()) {
                artList.add(Art.rl);
            }

            return artList;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Integer> getClasses() {
        if (!ckbTf.isSelected()) {
            return null;
        } else {
            final JTextField[] fields = new JTextField[] {
                    txtTf1,
                    txtTf2,
                    txtTf3,
                    txtTf4,
                    txtTf5,
                    txtTf6,
                    txtTf7,
                    txtTf8,
                    txtTf9,
                    txtTf10
                };
            final List<Integer> classList = new ArrayList<Integer>();

            if (ckbOhneKlasse.isSelected()) {
                classList.add(null);
            }

            for (final JTextField field : fields) {
                final Integer d = getClassInteger(field);

                if (d != null) {
                    classList.add(d);
                }
            }

            return classList;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getClassInteger(final JTextField f) {
        Integer d = null;

        try {
            d = Integer.parseInt(f.getText());
        } catch (NumberFormatException e) {
            // nothing to do
        }

        return d;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<Integer> getDimensions() {
        if (!ckbDim.isSelected()) {
            return null;
        } else {
            final List<Integer> dimList = new ArrayList<Integer>();
            final JTextField[] fields = new JTextField[] {
                    txtDim1,
                    txtDim2,
                    txtDim3,
                    txtDim4,
                    txtDim5,
                    txtDim6,
                    txtDim7,
                    txtDim8,
                    txtDim9,
                    txtDim10
                };

            if (ckbOhneDimension.isSelected()) {
                dimList.add(null);
            }

            for (final JTextField field : fields) {
                final Integer d = getInteger(field);

                if (d != null) {
                    dimList.add(d);
                }
            }

            return dimList;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getInteger(final JTextField f) {
        Integer d = null;

        try {
            d = Integer.parseInt(f.getText());
        } catch (NumberFormatException e) {
            // nothing to do
        }

        return d;
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
        return ckbGemSelection.isSelected();
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

        private static final transient GerinneGeschlGemeindeReportDialog INSTANCE =
            new GerinneGeschlGemeindeReportDialog(AppBroker.getInstance().getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
