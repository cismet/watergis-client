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

import org.openide.util.NbBundle;

import java.awt.EventQueue;

import java.io.File;

import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.gui.RestrictedFileSystemView;
import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.map.SaveDrawingsAction;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GafExportDialog extends javax.swing.JDialog {

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private String lastPath = WatergisApp.getDIRECTORYPATH_WATERGIS();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butFile;
    private javax.swing.JButton butOk;
    private javax.swing.JCheckBox ckBasis;
    private javax.swing.JCheckBox ckGaf;
    private javax.swing.JCheckBox ckLawa;
    private javax.swing.JCheckBox ckReport;
    private javax.swing.JCheckBox ckShape;
    private javax.swing.JCheckBox ckWithout;
    private javax.swing.JCheckBox ckbGafSelection;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel labAusgabe;
    private javax.swing.JLabel labBezug;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private GafExportDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

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
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static GafExportDialog getInstance() {
        return LazyInitializer.INSTANCE;
    }

    @Override
    public void setVisible(final boolean b) {
        if (b) {
            refreshSelectedFeatureCount(true);
            cancelled = true;
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

        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        txtFile = new javax.swing.JTextField();
        butFile = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        ckbGafSelection = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        labBezug = new javax.swing.JLabel();
        ckBasis = new javax.swing.JCheckBox();
        ckLawa = new javax.swing.JCheckBox();
        ckWithout = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        labAusgabe = new javax.swing.JLabel();
        ckGaf = new javax.swing.JCheckBox();
        ckReport = new javax.swing.JCheckBox();
        ckShape = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.title_1",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.butOk.text_1",
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
                GafExportDialog.class,
                "GafExportDialog.butCancel.text_1",
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
                GafExportDialog.class,
                "GafExportDialog.txtFile.text_1",
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
                GafExportDialog.class,
                "GafExportDialog.butFile.text_1",
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

        ckbGafSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGafSelection,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckbGafSelection.text",
                new Object[] {})); // NOI18N
        ckbGafSelection.setEnabled(false);
        ckbGafSelection.setMinimumSize(new java.awt.Dimension(458, 24));
        ckbGafSelection.setPreferredSize(new java.awt.Dimension(458, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbGafSelection, gridBagConstraints);

        jPanel2.setLayout(new java.awt.GridBagLayout());

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labBezug,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.labBezug.text",
                new Object[] {})); // NOI18N
        labBezug.setPreferredSize(new java.awt.Dimension(75, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel4.add(labBezug, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckBasis,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckBasis.text",
                new Object[] {})); // NOI18N
        ckBasis.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckBasisActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        jPanel4.add(ckBasis, gridBagConstraints);

        ckLawa.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckLawa,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckLawa.text",
                new Object[] {})); // NOI18N
        ckLawa.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckLawaActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel4.add(ckLawa, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckWithout,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckWithout.text",
                new Object[] {})); // NOI18N
        ckWithout.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckWithoutActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel4.add(ckWithout, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labAusgabe,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.labAusgabe.text",
                new Object[] {})); // NOI18N
        labAusgabe.setPreferredSize(new java.awt.Dimension(75, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel5.add(labAusgabe, gridBagConstraints);

        ckGaf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckGaf,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckGaf.text",
                new Object[] {})); // NOI18N
        ckGaf.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckGafActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 0);
        jPanel5.add(ckGaf, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckReport,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckReport.text",
                new Object[] {})); // NOI18N
        ckReport.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckReportActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel5.add(ckReport, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckShape,
            org.openide.util.NbBundle.getMessage(
                GafExportDialog.class,
                "GafExportDialog.ckShape.text",
                new Object[] {})); // NOI18N
        ckShape.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckShapeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 20, 5, 0);
        jPanel5.add(ckShape, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel2.add(jPanel5, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

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
        final File file = StaticSwingTools.chooseFile(
                lastPath,
                true,
                new String[] { "zip" },
                org.openide.util.NbBundle.getMessage(
                    GafExportDialog.class,
                    "PhotoExportReportDialog.butFileActionPerformed().getDescription()"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            lastPath = file.getParent();
            txtFile.setText(file.getAbsolutePath());
        }
    } //GEN-LAST:event_butFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckBasisActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckBasisActionPerformed
        checkContentSelection();
    }                                                                           //GEN-LAST:event_ckBasisActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckLawaActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckLawaActionPerformed
        checkContentSelection();
    }                                                                          //GEN-LAST:event_ckLawaActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckWithoutActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckWithoutActionPerformed
        checkContentSelection();
    }                                                                             //GEN-LAST:event_ckWithoutActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckReportActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckReportActionPerformed
        checkContentSelection();
    }                                                                            //GEN-LAST:event_ckReportActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckShapeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckShapeActionPerformed
        checkContentSelection();
    }                                                                           //GEN-LAST:event_ckShapeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckGafActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckGafActionPerformed
        checkContentSelection();
    }                                                                         //GEN-LAST:event_ckGafActionPerformed

    /**
     * DOCUMENT ME!
     */
    private void checkContentSelection() {
        butOk.setEnabled((ckBasis.isSelected() || ckLawa.isSelected() || ckWithout.isSelected())
                    && (ckReport.isSelected()
                        || ckShape.isSelected() || ckGaf.isSelected()));
    }

    /**
     * refreshes the ckbSelection1 label.
     *
     * @param  forceGuiRefresh  DOCUMENT ME!
     */
    public void refreshSelectedFeatureCount(final boolean forceGuiRefresh) {
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                AppBroker.GAF_PROF_MC_NAME);

        final int count = features.size();

        ckbGafSelection.setText(NbBundle.getMessage(GafExportDialog.class,
                "GafExportDialog.ckbGafSelection.text")
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GafProfReportDialog.refreshSelectedFeatureCount.text",
                        count));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getZipFile() {
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
     * @return  the cancelled
     */
    public boolean isGafSelected() {
        return ckGaf.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isBasisSelected() {
        return ckBasis.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isLawaSelected() {
        return ckLawa.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isWithoutSelected() {
        return ckWithout.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isReportSelected() {
        return ckReport.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the cancelled
     */
    public boolean isShapeSelected() {
        return ckShape.isSelected();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient GafExportDialog INSTANCE = new GafExportDialog(AppBroker.getInstance()
                        .getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
