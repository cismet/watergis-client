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

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.WatergisApp;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GewaesserReportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GewaesserReportDialog.class);

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
    private String lastPath = WatergisApp.getDIRECTORYPATH_WATERGIS();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butFile;
    private javax.swing.JButton butOk;
    private javax.swing.JCheckBox ckbAnll;
    private javax.swing.JCheckBox ckbAnlp;
    private javax.swing.JCheckBox ckbAus;
    private javax.swing.JCheckBox ckbBbef;
    private javax.swing.JCheckBox ckbBen;
    private javax.swing.JCheckBox ckbD;
    private javax.swing.JCheckBox ckbDeich;
    private javax.swing.JCheckBox ckbDue;
    private javax.swing.JCheckBox ckbEa;
    private javax.swing.JCheckBox ckbFoto;
    private javax.swing.JCheckBox ckbGb;
    private javax.swing.JCheckBox ckbGbk;
    private javax.swing.JCheckBox ckbGmd;
    private javax.swing.JCheckBox ckbGwk;
    private javax.swing.JCheckBox ckbKarte;
    private javax.swing.JCheckBox ckbKr;
    private javax.swing.JCheckBox ckbLeis;
    private javax.swing.JCheckBox ckbPegel;
    private javax.swing.JCheckBox ckbProf;
    private javax.swing.JCheckBox ckbRl;
    private javax.swing.JCheckBox ckbSb;
    private javax.swing.JCheckBox ckbSbef;
    private javax.swing.JCheckBox ckbScha;
    private javax.swing.JCheckBox ckbSchutzgebiete;
    private javax.swing.JCheckBox ckbSchw;
    private javax.swing.JCheckBox ckbSelection1;
    private javax.swing.JCheckBox ckbTech;
    private javax.swing.JCheckBox ckbTopo;
    private javax.swing.JCheckBox ckbUbef;
    private javax.swing.JCheckBox ckbUghz;
    private javax.swing.JCheckBox ckbVerkn;
    private javax.swing.JCheckBox ckbWehr;
    private javax.swing.JCheckBox ckbWsg;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
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
    private GewaesserReportDialog(final java.awt.Frame parent, final boolean modal) {
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
    public static GewaesserReportDialog getInstance() {
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

        ckbKarte = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        txtFile = new javax.swing.JTextField();
        butFile = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ckbSelection1 = new javax.swing.JCheckBox();
        ckbVerkn = new javax.swing.JCheckBox();
        ckbGwk = new javax.swing.JCheckBox();
        ckbGbk = new javax.swing.JCheckBox();
        ckbTopo = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        ckbWsg = new javax.swing.JCheckBox();
        ckbSchutzgebiete = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        ckbDeich = new javax.swing.JCheckBox();
        ckbUghz = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        ckbPegel = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        ckbGmd = new javax.swing.JCheckBox();
        ckbGb = new javax.swing.JCheckBox();
        ckbSb = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        ckbProf = new javax.swing.JCheckBox();
        ckbSbef = new javax.swing.JCheckBox();
        ckbUbef = new javax.swing.JCheckBox();
        ckbBbef = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        ckbRl = new javax.swing.JCheckBox();
        ckbD = new javax.swing.JCheckBox();
        ckbDue = new javax.swing.JCheckBox();
        ckbScha = new javax.swing.JCheckBox();
        ckbWehr = new javax.swing.JCheckBox();
        ckbSchw = new javax.swing.JCheckBox();
        ckbAnlp = new javax.swing.JCheckBox();
        ckbAnll = new javax.swing.JCheckBox();
        ckbKr = new javax.swing.JCheckBox();
        ckbEa = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        ckbFoto = new javax.swing.JCheckBox();
        ckbLeis = new javax.swing.JCheckBox();
        ckbTech = new javax.swing.JCheckBox();
        ckbAus = new javax.swing.JCheckBox();
        ckbBen = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        ckbKarte.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbKarte,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbKarte.text",
                new Object[] {})); // NOI18N
        ckbKarte.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbKarte.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbKarte.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbKarte, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.butOk.text",
                new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(170, 29));
        butOk.setPreferredSize(new java.awt.Dimension(170, 29));
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
                GewaesserReportDialog.class,
                "GewaesserReportDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.setMinimumSize(new java.awt.Dimension(170, 29));
        butCancel.setPreferredSize(new java.awt.Dimension(170, 29));
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
        gridBagConstraints.gridy = 25;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        txtFile.setText(org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.txtFile.text",
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
                GewaesserReportDialog.class,
                "GewaesserReportDialog.butFile.text",
                new Object[] {})); // NOI18N
        butFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(butFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(filler1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel2, gridBagConstraints);

        ckbSelection1.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelection1,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSelection1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbSelection1, gridBagConstraints);

        ckbVerkn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbVerkn,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbVerkn.text",
                new Object[] {})); // NOI18N
        ckbVerkn.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbVerkn.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbVerkn.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbVerkn, gridBagConstraints);

        ckbGwk.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGwk,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbGwk.text",
                new Object[] {})); // NOI18N
        ckbGwk.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbGwk.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbGwk.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbGwk, gridBagConstraints);

        ckbGbk.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGbk,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbGbk.text",
                new Object[] {})); // NOI18N
        ckbGbk.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbGbk.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbGbk.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbGbk, gridBagConstraints);

        ckbTopo.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbTopo,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbTopo.text",
                new Object[] {})); // NOI18N
        ckbTopo.setMaximumSize(new java.awt.Dimension(300, 24));
        ckbTopo.setMinimumSize(new java.awt.Dimension(300, 24));
        ckbTopo.setPreferredSize(new java.awt.Dimension(300, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbTopo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel3.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel3, gridBagConstraints);

        ckbWsg.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWsg,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbWsg.text",
                new Object[] {})); // NOI18N
        ckbWsg.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbWsg.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbWsg.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbWsg, gridBagConstraints);

        ckbSchutzgebiete.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSchutzgebiete,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSchutzgebiete.text",
                new Object[] {})); // NOI18N
        ckbSchutzgebiete.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSchutzgebiete.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSchutzgebiete.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbSchutzgebiete, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel4.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel4, gridBagConstraints);

        ckbDeich.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDeich,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbDeich.text",
                new Object[] {})); // NOI18N
        ckbDeich.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbDeich.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbDeich.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbDeich, gridBagConstraints);

        ckbUghz.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbUghz,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbUghz.text",
                new Object[] {})); // NOI18N
        ckbUghz.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbUghz.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbUghz.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbUghz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel5.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel5, gridBagConstraints);

        ckbPegel.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPegel,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbPegel.text",
                new Object[] {})); // NOI18N
        ckbPegel.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbPegel.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbPegel.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbPegel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel6,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel6.text",
                new Object[] {})); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel6.toolTipText",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel7,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel7.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel7, gridBagConstraints);

        ckbGmd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGmd,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbGmd.text",
                new Object[] {})); // NOI18N
        ckbGmd.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbGmd.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbGmd.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbGmd, gridBagConstraints);

        ckbGb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGb,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbGb.text",
                new Object[] {})); // NOI18N
        ckbGb.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbGb.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbGb.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbGb, gridBagConstraints);

        ckbSb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSb,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSb.text",
                new Object[] {})); // NOI18N
        ckbSb.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSb.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSb.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbSb, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel8,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel8.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel8, gridBagConstraints);

        ckbProf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbProf,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbProf.text",
                new Object[] {})); // NOI18N
        ckbProf.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbProf.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbProf.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbProf, gridBagConstraints);

        ckbSbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSbef,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSbef.text",
                new Object[] {})); // NOI18N
        ckbSbef.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSbef.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSbef.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbSbef, gridBagConstraints);

        ckbUbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbUbef,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbUbef.text",
                new Object[] {})); // NOI18N
        ckbUbef.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbUbef.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbUbef.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbUbef, gridBagConstraints);

        ckbBbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbBbef,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbBbef.text",
                new Object[] {})); // NOI18N
        ckbBbef.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbBbef.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbBbef.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbBbef, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel10,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel10.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel10, gridBagConstraints);

        ckbRl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbRl,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbRl.text",
                new Object[] {})); // NOI18N
        ckbRl.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbRl.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbRl.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbRl, gridBagConstraints);

        ckbD.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbD,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbD.text",
                new Object[] {})); // NOI18N
        ckbD.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbD.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbD.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbD, gridBagConstraints);

        ckbDue.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDue,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbDue.text",
                new Object[] {})); // NOI18N
        ckbDue.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbDue.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbDue.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbDue, gridBagConstraints);

        ckbScha.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbScha,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbScha.text",
                new Object[] {})); // NOI18N
        ckbScha.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbScha.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbScha.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbScha, gridBagConstraints);

        ckbWehr.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWehr,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbWehr.text",
                new Object[] {})); // NOI18N
        ckbWehr.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbWehr.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbWehr.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbWehr, gridBagConstraints);

        ckbSchw.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSchw,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSchw.text",
                new Object[] {})); // NOI18N
        ckbSchw.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSchw.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSchw.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 19;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbSchw, gridBagConstraints);

        ckbAnlp.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAnlp,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbAnlp.text",
                new Object[] {})); // NOI18N
        ckbAnlp.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbAnlp.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbAnlp.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbAnlp, gridBagConstraints);

        ckbAnll.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAnll,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbAnll.text",
                new Object[] {})); // NOI18N
        ckbAnll.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbAnll.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbAnll.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbAnll, gridBagConstraints);

        ckbKr.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbKr,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbKr.text",
                new Object[] {})); // NOI18N
        ckbKr.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbKr.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbKr.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbKr, gridBagConstraints);

        ckbEa.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbEa,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbEa.text",
                new Object[] {})); // NOI18N
        ckbEa.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbEa.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbEa.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 21;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbEa, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel11,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.jLabel11.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        getContentPane().add(jLabel11, gridBagConstraints);

        ckbFoto.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFoto,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbFoto.text",
                new Object[] {})); // NOI18N
        ckbFoto.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbFoto.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbFoto.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbFoto, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbLeis,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbLeis.text",
                new Object[] {})); // NOI18N
        ckbLeis.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbLeis.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbLeis.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbLeis, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbTech,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbTech.text",
                new Object[] {})); // NOI18N
        ckbTech.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbTech.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbTech.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbTech, gridBagConstraints);

        ckbAus.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAus,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbAus.text",
                new Object[] {})); // NOI18N
        ckbAus.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbAus.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbAus.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbAus, gridBagConstraints);

        ckbBen.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbBen,
            org.openide.util.NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbBen.text",
                new Object[] {})); // NOI18N
        ckbBen.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbBen.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbBen.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbBen, gridBagConstraints);

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
            fc = new JFileChooser(lastPath);
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(lastPath, new RestrictedFileSystemView());
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
                            GewaesserReportDialog.class,
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

        ckbSelection1.setText(NbBundle.getMessage(
                GewaesserReportDialog.class,
                "GewaesserReportDialog.ckbSelection1.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "GewaesserReportDialog.refreshSelectedFeatureCount.text",
                        count));

        ckbSelection1.setEnabled(true);

        if (forceGuiRefresh || (count != selectedThemeFeatureCount)) {
            ckbSelection1.setSelected(count > 0);
        }

        if (count == 0) {
            ckbSelection1.setSelected(false);
            ckbSelection1.setEnabled(false);
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FeatureServiceFeature[] getSelectedFeatures() {
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
     * @return  the ckbAnll
     */
    public boolean isAnll() {
        return ckbAnll.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbAnlp
     */
    public boolean isAnlp() {
        return ckbAnlp.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbAus
     */
    public boolean isAus() {
        return ckbAus.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbBbef
     */
    public boolean isBbef() {
        return ckbBbef.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbBen
     */
    public boolean isBen() {
        return ckbBen.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbD
     */
    public boolean isD() {
        return ckbD.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbDeich
     */
    public boolean isDeich() {
        return ckbDeich.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbDue
     */
    public boolean isDue() {
        return ckbDue.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbEa
     */
    public boolean isEa() {
        return ckbEa.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbFoto
     */
    public boolean isFoto() {
        return ckbFoto.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGb
     */
    public boolean isGb() {
        return ckbGb.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGbk
     */
    public boolean isGbk() {
        return ckbGbk.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGmd
     */
    public boolean isGmd() {
        return ckbGmd.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGwk
     */
    public boolean isGwk() {
        return ckbGwk.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbKarte
     */
    public boolean isKarte() {
        return ckbKarte.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbKr
     */
    public boolean isKr() {
        return ckbKr.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbLeis
     */
    public boolean isLeis() {
        return ckbLeis.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbPegel
     */
    public boolean isPegel() {
        return ckbPegel.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbProf
     */
    public boolean isProf() {
        return ckbProf.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbRl
     */
    public boolean isRl() {
        return ckbRl.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSb
     */
    public boolean isSb() {
        return ckbSb.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSbef
     */
    public boolean isSbef() {
        return ckbSbef.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbScha
     */
    public boolean isScha() {
        return ckbScha.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSchutzgebiete
     */
    public boolean isSchutzgebiete() {
        return ckbSchutzgebiete.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSchw
     */
    public boolean isSchw() {
        return ckbSchw.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbSelection1
     */
    public boolean isSelection() {
        return ckbSelection1.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbTech
     */
    public boolean isTech() {
        return ckbTech.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbTopo
     */
    public boolean isTopo() {
        return ckbTopo.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbUbef
     */
    public boolean isUbef() {
        return ckbUbef.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbUghz
     */
    public boolean isUghz() {
        return ckbUghz.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbVerkn
     */
    public boolean isVerkn() {
        return ckbVerkn.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbWehr
     */
    public boolean isWehr() {
        return ckbWehr.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbWsg
     */
    public boolean isWsg() {
        return ckbWsg.isSelected();
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient GewaesserReportDialog INSTANCE = new GewaesserReportDialog(AppBroker
                        .getInstance().getWatergisApp(),
                true);

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
