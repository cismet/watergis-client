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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
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
public class GerinneOGewaesserReportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOGewaesserReportDialog.class);

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
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
    private javax.swing.JCheckBox ckbAbschn;
    private javax.swing.JCheckBox ckbAbschnProf;
    private javax.swing.JCheckBox ckbGewSelection;
    private javax.swing.JCheckBox ckbSumGu;
    private javax.swing.JCheckBox ckbWdmSeparated;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private GerinneOGewaesserReportDialog(final java.awt.Frame parent, final boolean modal) {
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
    public static GerinneOGewaesserReportDialog getInstance() {
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

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
        ckb1502 = new javax.swing.JCheckBox();
        ckb1503 = new javax.swing.JCheckBox();
        ckb1505 = new javax.swing.JCheckBox();
        ckbAbschnProf = new javax.swing.JCheckBox();
        ckbSumGu = new javax.swing.JCheckBox();
        ckbWdmSeparated = new javax.swing.JCheckBox();
        ckb1504 = new javax.swing.JCheckBox();
        ckbGewSelection = new javax.swing.JCheckBox();
        ckbAbschn = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        ckb1501.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1501,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckb1501.text",
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
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckb1501, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.butOk.text",
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
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.butCancel.text",
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
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.txtFile.text",
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
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.butFile.text",
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
        getContentPane().add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(filler1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jLabel2, gridBagConstraints);

        ckb1502.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1502,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckb1502.text",
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
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckb1502, gridBagConstraints);

        ckb1503.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1503,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckb1503.text",
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
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckb1503, gridBagConstraints);

        ckb1505.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1505,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckb1505.text",
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
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckb1505, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAbschnProf,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckbAbschnProf.text",
                new Object[] {})); // NOI18N
        ckbAbschnProf.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbAbschnProf.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbAbschnProf.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbAbschnProf.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbAbschnProfActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbAbschnProf, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSumGu,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckbSumGu.text",
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
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbSumGu, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWdmSeparated,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckbWdmSeparated.text",
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
        gridBagConstraints.gridy = 19;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 15, 10);
        getContentPane().add(ckbWdmSeparated, gridBagConstraints);

        ckb1504.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1504,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckb1504.text",
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
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckb1504, gridBagConstraints);

        ckbGewSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGewSelection,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckbGewSelection.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbGewSelection, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAbschn,
            org.openide.util.NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GerinneOGewaesserReportDialog.ckbAbschn.text",
                new Object[] {})); // NOI18N
        ckbAbschn.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbAbschn.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbAbschn.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbAbschn.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbAbschnActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbAbschn, gridBagConstraints);

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
                            GerinneOGewaesserReportDialog.class,
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
    private void ckbAbschnProfActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbAbschnProfActionPerformed
    }                                                                                 //GEN-LAST:event_ckbAbschnProfActionPerformed

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
    private void ckbAbschnActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbAbschnActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_ckbAbschnActionPerformed

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
     * refreshes the ckbSelection1 label.
     *
     * @param   forceGuiRefresh  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int refreshSelectedFeatureCount(final boolean forceGuiRefresh) {
        final TreeSet<FeatureServiceFeature> set = new TreeSet<FeatureServiceFeature>();

        for (final AbstractFeatureService service : getAllActiveFgBaServices()) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures(service));
        }

        final int count = set.size();

        ckbGewSelection.setText(NbBundle.getMessage(
                GerinneOGewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSelection1.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GewaesserReportDialog.refreshSelectedFeatureCount.text",
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
    public boolean isPerPartProf() {
        return ckbAbschnProf.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbAbschn
     */
    public boolean isPerPart() {
        return ckbAbschn.isSelected();
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

        private static final transient GerinneOGewaesserReportDialog INSTANCE = new GerinneOGewaesserReportDialog(
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
