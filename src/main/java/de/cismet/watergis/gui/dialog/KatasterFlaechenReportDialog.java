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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
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
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;

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
public class KatasterFlaechenReportDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterFlaechenReportDialog.class);

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private int selectedThemeFeatureCount = -1;
    private int selectedGewFeatureCount = -1;
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
    private javax.swing.JCheckBox ckbAbschn;
    private javax.swing.JCheckBox ckbAbschnOffGeschl;
    private javax.swing.JCheckBox ckbAnll;
    private javax.swing.JCheckBox ckbAnlp;
    private javax.swing.JCheckBox ckbAus;
    private javax.swing.JCheckBox ckbBbef;
    private javax.swing.JCheckBox ckbBen;
    private javax.swing.JCheckBox ckbD;
    private javax.swing.JCheckBox ckbDeich;
    private javax.swing.JCheckBox ckbDok;
    private javax.swing.JCheckBox ckbDue;
    private javax.swing.JCheckBox ckbEa;
    private javax.swing.JCheckBox ckbFlSelection;
    private javax.swing.JCheckBox ckbFoto;
    private javax.swing.JCheckBox ckbGb;
    private javax.swing.JCheckBox ckbGew;
    private javax.swing.JCheckBox ckbGewSelection;
    private javax.swing.JCheckBox ckbGmd;
    private javax.swing.JCheckBox ckbKr;
    private javax.swing.JCheckBox ckbLeis;
    private javax.swing.JCheckBox ckbPegel;
    private javax.swing.JCheckBox ckbProf;
    private javax.swing.JCheckBox ckbProj;
    private javax.swing.JCheckBox ckbRl;
    private javax.swing.JCheckBox ckbSb;
    private javax.swing.JCheckBox ckbSbef;
    private javax.swing.JCheckBox ckbScha;
    private javax.swing.JCheckBox ckbSchutzgebiete;
    private javax.swing.JCheckBox ckbSchw;
    private javax.swing.JCheckBox ckbSumGu;
    private javax.swing.JCheckBox ckbTech;
    private javax.swing.JCheckBox ckbUbef;
    private javax.swing.JCheckBox ckbUghz;
    private javax.swing.JCheckBox ckbWdmSeparated;
    private javax.swing.JCheckBox ckbWehr;
    private javax.swing.JCheckBox ckbWsg;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField txtFile;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private KatasterFlaechenReportDialog(final java.awt.Frame parent, final boolean modal) {
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
                                            selectedGewFeatureCount = refreshSelectedGewFeatureCount(
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
    public static KatasterFlaechenReportDialog getInstance() {
        return LazyInitializer.INSTANCE;
    }

    @Override
    public void setVisible(final boolean b) {
        if (b) {
            cancelled = true;
            selectedThemeFeatureCount = refreshSelectedFeatureCount(true);
            selectedGewFeatureCount = refreshSelectedGewFeatureCount(true);
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

        jPanel2 = new javax.swing.JPanel();
        ckbProf = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        ckbDue = new javax.swing.JCheckBox();
        ckbWsg = new javax.swing.JCheckBox();
        ckbGmd = new javax.swing.JCheckBox();
        ckbAnll = new javax.swing.JCheckBox();
        ckbSb = new javax.swing.JCheckBox();
        ckbBbef = new javax.swing.JCheckBox();
        ckbEa = new javax.swing.JCheckBox();
        ckbWehr = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ckbSbef = new javax.swing.JCheckBox();
        ckbLeis = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        ckbSchutzgebiete = new javax.swing.JCheckBox();
        ckbSchw = new javax.swing.JCheckBox();
        ckbRl = new javax.swing.JCheckBox();
        ckbGb = new javax.swing.JCheckBox();
        ckbUghz = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        ckbAus = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        ckbDeich = new javax.swing.JCheckBox();
        ckbScha = new javax.swing.JCheckBox();
        ckbTech = new javax.swing.JCheckBox();
        ckbPegel = new javax.swing.JCheckBox();
        ckbKr = new javax.swing.JCheckBox();
        ckbAnlp = new javax.swing.JCheckBox();
        ckbD = new javax.swing.JCheckBox();
        ckbUbef = new javax.swing.JCheckBox();
        ckbBen = new javax.swing.JCheckBox();
        jLabel13 = new javax.swing.JLabel();
        ckbDok = new javax.swing.JCheckBox();
        ckbProj = new javax.swing.JCheckBox();
        ckbFoto = new javax.swing.JCheckBox();
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
        ckbGew = new javax.swing.JCheckBox();
        ckbAbschn = new javax.swing.JCheckBox();
        ckbSumGu = new javax.swing.JCheckBox();
        ckbWdmSeparated = new javax.swing.JCheckBox();
        ckb1504 = new javax.swing.JCheckBox();
        ckbGewSelection = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        cbbFlaeche = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        cbbAttr1 = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        cbbAttr2 = new javax.swing.JComboBox();
        ckbAbschnOffGeschl = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.title",
                new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        ckbProf.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbProf,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbProf.text",
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
        jPanel2.add(ckbProf, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel8,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel8.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel8, gridBagConstraints);

        ckbDue.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDue,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbDue.text",
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
        jPanel2.add(ckbDue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWsg,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbWsg.text",
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
        jPanel2.add(ckbWsg, gridBagConstraints);

        ckbGmd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGmd,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbGmd.text",
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
        jPanel2.add(ckbGmd, gridBagConstraints);

        ckbAnll.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAnll,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbAnll.text",
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
        jPanel2.add(ckbAnll, gridBagConstraints);

        ckbSb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSb,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbSb.text",
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
        jPanel2.add(ckbSb, gridBagConstraints);

        ckbBbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbBbef,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbBbef.text",
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
        jPanel2.add(ckbBbef, gridBagConstraints);

        ckbEa.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbEa,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbEa.text",
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
        jPanel2.add(ckbEa, gridBagConstraints);

        ckbWehr.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWehr,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbWehr.text",
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
        jPanel2.add(ckbWehr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel5.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel4.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel6,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel6.text",
                new Object[] {})); // NOI18N
        jLabel6.setToolTipText(org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel6.toolTipText",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel2.add(jLabel6, gridBagConstraints);

        ckbSbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSbef,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbSbef.text",
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
        jPanel2.add(ckbSbef, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbLeis,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbLeis.text",
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
        jPanel2.add(ckbLeis, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel10,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel10.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel10, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel7,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel7.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel7, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSchutzgebiete,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbSchutzgebiete.text",
                new Object[] {})); // NOI18N
        ckbSchutzgebiete.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSchutzgebiete.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSchutzgebiete.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        jPanel2.add(ckbSchutzgebiete, gridBagConstraints);

        ckbSchw.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSchw,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbSchw.text",
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
        jPanel2.add(ckbSchw, gridBagConstraints);

        ckbRl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbRl,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbRl.text",
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
        jPanel2.add(ckbRl, gridBagConstraints);

        ckbGb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGb,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbGb.text",
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
        jPanel2.add(ckbGb, gridBagConstraints);

        ckbUghz.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbUghz,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbUghz.text",
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
        jPanel2.add(ckbUghz, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel9,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel9.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel9, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAus,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbAus.text",
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
        jPanel2.add(ckbAus, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel2.add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDeich,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbDeich.text",
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
        jPanel2.add(ckbDeich, gridBagConstraints);

        ckbScha.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbScha,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbScha.text",
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
        jPanel2.add(ckbScha, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbTech,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbTech.text",
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
        jPanel2.add(ckbTech, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbPegel,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbPegel.text",
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
        jPanel2.add(ckbPegel, gridBagConstraints);

        ckbKr.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbKr,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbKr.text",
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
        jPanel2.add(ckbKr, gridBagConstraints);

        ckbAnlp.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAnlp,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbAnlp.text",
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
        jPanel2.add(ckbAnlp, gridBagConstraints);

        ckbD.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbD,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbD.text",
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
        jPanel2.add(ckbD, gridBagConstraints);

        ckbUbef.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbUbef,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbUbef.text",
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
        jPanel2.add(ckbUbef, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbBen,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbBen.text",
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
        jPanel2.add(ckbBen, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel13,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel13.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 22;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 35, 10, 10);
        jPanel2.add(jLabel13, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbDok,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbDok.text",
                new Object[] {})); // NOI18N
        ckbDok.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbDok.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbDok.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        jPanel2.add(ckbDok, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbProj,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbProj.text",
                new Object[] {})); // NOI18N
        ckbProj.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbProj.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbProj.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 25;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        jPanel2.add(ckbProj, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFoto,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbFoto.text",
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
        jPanel2.add(ckbFoto, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(jPanel2, gridBagConstraints);

        ckb1501.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1501,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckb1501.text",
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
        getContentPane().add(ckb1501, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.butOk.text",
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.butCancel.text",
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.txtFile.text",
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.butFile.text",
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jLabel2, gridBagConstraints);

        ckbFlSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbFlSelection,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbFlSelection.text",
                new Object[] {})); // NOI18N
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
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbFlSelection, gridBagConstraints);

        ckb1502.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1502,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckb1502.text",
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
        getContentPane().add(ckb1502, gridBagConstraints);

        ckb1503.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1503,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckb1503.text",
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
        getContentPane().add(ckb1503, gridBagConstraints);

        ckb1505.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1505,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckb1505.text",
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
        getContentPane().add(ckb1505, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGew,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbGew.text",
                new Object[] {})); // NOI18N
        ckbGew.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbGew.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbGew.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbGew.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbGewActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        getContentPane().add(ckbGew, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAbschn,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbAbschn.text",
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
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 5, 10);
        getContentPane().add(ckbAbschn, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSumGu,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbSumGu.text",
                new Object[] {})); // NOI18N
        ckbSumGu.setMaximumSize(new java.awt.Dimension(260, 24));
        ckbSumGu.setMinimumSize(new java.awt.Dimension(260, 24));
        ckbSumGu.setPreferredSize(new java.awt.Dimension(260, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 5, 10);
        getContentPane().add(ckbSumGu, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbWdmSeparated,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbWdmSeparated.text",
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
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbWdmSeparated, gridBagConstraints);

        ckb1504.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckb1504,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckb1504.text",
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
        getContentPane().add(ckb1504, gridBagConstraints);

        ckbGewSelection.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(
            ckbGewSelection,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbGewSelection.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(ckbGewSelection, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel3.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jLabel3, gridBagConstraints);

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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel11.text",
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.jLabel12.text",
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
        getContentPane().add(jPanel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbAbschnOffGeschl,
            org.openide.util.NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbAbschnOffGeschl.text",
                new Object[] {})); // NOI18N
        ckbAbschnOffGeschl.setMaximumSize(new java.awt.Dimension(350, 24));
        ckbAbschnOffGeschl.setMinimumSize(new java.awt.Dimension(350, 24));
        ckbAbschnOffGeschl.setPreferredSize(new java.awt.Dimension(260, 24));
        ckbAbschnOffGeschl.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    ckbAbschnOffGeschlActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 15, 10);
        getContentPane().add(ckbAbschnOffGeschl, gridBagConstraints);

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
                            KatasterFlaechenReportDialog.class,
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
    private void ckbAbschnActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbAbschnActionPerformed
        if (ckbAbschn.isSelected()) {
            ckbGew.setSelected(true);
        }
    }                                                                             //GEN-LAST:event_ckbAbschnActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void ckbGewActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbGewActionPerformed
        if (!ckbGew.isSelected()) {
            ckbAbschn.setSelected(false);
        }
    }                                                                          //GEN-LAST:event_ckbGewActionPerformed

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
    private void cbbFlaecheItemStateChanged(final java.awt.event.ItemEvent evt) { //GEN-FIRST:event_cbbFlaecheItemStateChanged
        refreshFieldModel();
        selectedThemeFeatureCount = refreshSelectedFeatureCount(false);
    }                                                                             //GEN-LAST:event_cbbFlaecheItemStateChanged

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
    private void ckbAbschnOffGeschlActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_ckbAbschnOffGeschlActionPerformed
        if (ckbAbschnOffGeschl.isSelected()) {
            ckbAbschn.setSelected(true);
            ckbGew.setSelected(true);
        }
    }                                                                                      //GEN-LAST:event_ckbAbschnOffGeschlActionPerformed

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

        if (cbbFlaeche.getSelectedItem() instanceof AbstractFeatureService) {
            set.addAll(FeatureServiceHelper.getSelectedFeatures((AbstractFeatureService)cbbFlaeche.getSelectedItem()));
        } else {
            return -1;
        }

        final int count = set.size();

        ckbFlSelection.setText(NbBundle.getMessage(
                KatasterFlaechenReportDialog.class,
                "FlaechenReportDialog.ckbFlSelection.text") + " "
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
                KatasterFlaechenReportDialog.class,
                "KatasterFlaechenReportDialog.ckbGewSelection.text") + " "
                    + NbBundle.getMessage(
                        BufferDialog.class,
                        "KatasterFlaechenReportDialog.refreshSelectedFeatureCount.text",
                        count));

        ckbGewSelection.setEnabled(true);

        if (forceGuiRefresh || (count != selectedGewFeatureCount)) {
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
     * @return  DOCUMENT ME!
     */
    public String getThema() {
        return ((AbstractFeatureService)cbbFlaeche.getSelectedItem()).getName();
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
     * @return  the ckbGb
     */
    public boolean isGb() {
        return ckbGb.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbGew
     */
    public boolean isPerGew() {
        return ckbGew.isSelected();
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
    public boolean isSelectionFl() {
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
     * @return  the ckbTech
     */
    public boolean isFoto() {
        return ckbFoto.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbTech
     */
    public boolean isDok() {
        return ckbDok.isSelected();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the ckbTech
     */
    public boolean isProj() {
        return ckbProj.isSelected();
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

        private static final transient KatasterFlaechenReportDialog INSTANCE = new KatasterFlaechenReportDialog(
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
