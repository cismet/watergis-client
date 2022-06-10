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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.cismet.cids.custom.watergis.server.actions.AddProfileAction;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.actions.ServerActionParameter;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.connectioncontext.AbstractConnectionContext;
import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.netutil.ProxyHandler;

import de.cismet.tools.PasswordEncrypter;

import de.cismet.tools.gui.ConfirmationJFileChooser;
import de.cismet.tools.gui.RestrictedFileSystemView;
import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.profile.AbstractImportDialog;
import de.cismet.watergis.profile.ProfileLine;
import de.cismet.watergis.profile.ProfileReader;
import de.cismet.watergis.profile.ProfileReaderFactory;
import de.cismet.watergis.profile.QpCheckResult;

import de.cismet.watergis.utils.CidsBeanUtils;
import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class UploadQpDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(UploadQpDialog.class);
    private static int idCounter = -1;
    private static final WebDavClient webDavClient;
    public static final String WEB_DAV_USER;
    public static final String WEB_DAV_PASSWORD;
    public static final String WEB_DAV_DIRECTORY;

    static {
        final ResourceBundle bundle = ResourceBundle.getBundle("WebDav");
        String pass = bundle.getString("password");

        if ((pass != null)) {
            pass = new String(PasswordEncrypter.decrypt(pass.toCharArray(), true));
        }

        WEB_DAV_PASSWORD = pass;
        WEB_DAV_USER = bundle.getString("username");
        WEB_DAV_DIRECTORY = bundle.getString("url");

        webDavClient = new WebDavClient(ProxyHandler.getInstance().getProxy(), WEB_DAV_USER, WEB_DAV_PASSWORD, true);
    }

    //~ Instance fields --------------------------------------------------------

    private boolean cancelled = false;
    private String lastPath = DownloadManager.instance().getDestinationDirectory().toString();
    private MetaClass LAGESTATUS_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 148);
    private MetaClass LAGE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 335);
    private MetaClass HOEHE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 336);
    private MetaClass FREIGABE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 189);
    private MetaClass QP_PKTE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.qp_pkte");
    private ConnectionContext cc = ConnectionContext.create(
            AbstractConnectionContext.Category.ACTION,
            "UploadQpDialog");

    private ProfileReader reader = null;
    private boolean checkPerformed = false;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butCheck;
    private javax.swing.JButton butColumns;
    private javax.swing.JButton butGafFile;
    private javax.swing.JButton butOk;
    private javax.swing.JButton butRkFile;
    private javax.swing.JComboBox cbFreigabe;
    private javax.swing.JComboBox cbHoehe;
    private javax.swing.JComboBox cbLage;
    private javax.swing.JComboBox cbLageStatus;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private org.jdesktop.swingx.JXDatePicker jxDate;
    private javax.swing.JTextField txtBemerkung;
    private javax.swing.JTextField txtBeschreibung;
    private javax.swing.JTextField txtGafFile;
    private javax.swing.JTextField txtRkFile;
    private javax.swing.JTextField txtTime;
    private javax.swing.JTextField txtTitle;
    private javax.swing.JTextField txtUsr;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    private UploadQpDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        cbFreigabe.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final Component c = super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);

                    if ((c instanceof JLabel) && (value instanceof CidsLayerFeature)) {
                        final String freigabe = String.valueOf(((CidsLayerFeature)value).getProperty("freigabe"));
                        final String name = String.valueOf(((CidsLayerFeature)value).getProperty("name"));
                        ((JLabel)c).setText(freigabe + "-" + name);
                    }

                    return c;
                }
            });

        cbHoehe.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final Component c = super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);

                    if ((c instanceof JLabel) && (value instanceof CidsLayerFeature)) {
                        final String hBez = String.valueOf(((CidsLayerFeature)value).getProperty("h_bezug"));
                        final String name = String.valueOf(((CidsLayerFeature)value).getProperty("name"));
                        ((JLabel)c).setText(hBez + "-" + name);
                    }

                    return c;
                }
            });

        cbLage.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final Component c = super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);

                    if ((c instanceof JLabel) && (value instanceof CidsLayerFeature)) {
                        final String lBez = String.valueOf(((CidsLayerFeature)value).getProperty("l_bezug"));
                        final String name = String.valueOf(((CidsLayerFeature)value).getProperty("name"));
                        ((JLabel)c).setText(lBez + "-" + name);
                    }

                    return c;
                }
            });

        cbLageStatus.setRenderer(new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(final JList list,
                        final Object value,
                        final int index,
                        final boolean isSelected,
                        final boolean cellHasFocus) {
                    final Component c = super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus);

                    if ((c instanceof JLabel) && (value instanceof CidsLayerFeature)) {
                        final String lst = String.valueOf(((CidsLayerFeature)value).getProperty("l_st"));
                        final String name = String.valueOf(((CidsLayerFeature)value).getProperty("name"));
                        ((JLabel)c).setText(lst + "-" + name);
                    }

                    return c;
                }
            });

        setFilter();
        setSize(195, 650);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void setFilter() {
        final CidsLayerFeatureFilter filter = new CidsLayerFeatureFilter() {

                @Override
                public boolean accept(final CidsLayerFeature bean) {
                    if (bean == null) {
                        return true;
                    }

                    return (bean.getProperty("qp") != null)
                                && (Boolean)bean.getProperty("qp");
                }
            };

        ((DefaultCidsLayerBindableReferenceCombo)cbLageStatus).setBeanFilter(filter);
        ((DefaultCidsLayerBindableReferenceCombo)cbFreigabe).setBeanFilter(filter);
    }

    @Override
    public void setVisible(final boolean b) {
        butColumns.setEnabled(false);
        butOk.setEnabled(false);
        butCheck.setEnabled(false);
        txtBemerkung.setText("");
        txtBeschreibung.setText("");
        jxDate.setDate(new Date());
        txtGafFile.setText("");
        txtRkFile.setText("");
        txtTime.setText("");
        txtTitle.setText("");
        txtUsr.setText("");
        cbFreigabe.setSelectedItem(null);
        cbHoehe.setSelectedItem(null);
        cbLage.setSelectedItem(null);
        cbLageStatus.setSelectedItem(null);
        reader = null;
        checkPerformed = false;

        if (b) {
            cancelled = true;
        }
        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static UploadQpDialog getInstance() {
        return LazyInitializer.INSTANCE;
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
        txtGafFile = new javax.swing.JTextField();
        butGafFile = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 0),
                new java.awt.Dimension(200, 32767));
        jPanel4 = new javax.swing.JPanel();
        txtRkFile = new javax.swing.JTextField();
        butRkFile = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        butColumns = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        cbLage = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAGE_MC, true);
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbHoehe = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(HOEHE_MC, true);
        jSeparator1 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        butCheck = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel8 = new javax.swing.JPanel();
        cbLageStatus = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAGESTATUS_MC, true);
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbFreigabe = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(FREIGABE_MC, true);
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtUsr = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtTime = new javax.swing.JTextField();
        jxDate = new org.jdesktop.swingx.JXDatePicker();
        jPanel10 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        txtTitle = new javax.swing.JTextField();
        jPanel11 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        txtBeschreibung = new javax.swing.JTextField();
        jPanel12 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        txtBemerkung = new javax.swing.JTextField();
        jSeparator3 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(UploadQpDialog.class, "UploadQpDialog.title", new Object[] {})); // NOI18N
        setMinimumSize(new java.awt.Dimension(690, 191));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butOk.text_1",
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butCancel.text_1",
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

        txtGafFile.setEditable(false);
        txtGafFile.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtGafFile.text_1",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(txtGafFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butGafFile,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butGafFile.text_1",
                new Object[] {})); // NOI18N
        butGafFile.setPreferredSize(new java.awt.Dimension(146, 29));
        butGafFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butGafFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel3.add(butGafFile, gridBagConstraints);

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

        jPanel4.setLayout(new java.awt.GridBagLayout());

        txtRkFile.setEditable(false);
        txtRkFile.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtRkFile.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(txtRkFile, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butRkFile,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butRkFile.text",
                new Object[] {})); // NOI18N
        butRkFile.setPreferredSize(new java.awt.Dimension(146, 29));
        butRkFile.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butRkFileActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(butRkFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel4, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butColumns,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butColumns.text",
                new Object[] {})); // NOI18N
        butColumns.setPreferredSize(new java.awt.Dimension(146, 29));
        butColumns.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butColumnsActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel5.add(butColumns, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel5, gridBagConstraints);

        jPanel6.setLayout(new java.awt.GridBagLayout());

        cbLage.setMinimumSize(new java.awt.Dimension(200, 27));
        cbLage.setPreferredSize(new java.awt.Dimension(200, 27));
        cbLage.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbLageActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        jPanel6.add(cbLage, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel1.text",
                new Object[] {})); // NOI18N
        jLabel1.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel1.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel6.add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel2.text",
                new Object[] {})); // NOI18N
        jLabel2.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel2.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel6.add(jLabel2, gridBagConstraints);

        cbHoehe.setMinimumSize(new java.awt.Dimension(200, 27));
        cbHoehe.setPreferredSize(new java.awt.Dimension(200, 27));
        cbHoehe.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbHoeheActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        jPanel6.add(cbHoehe, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jSeparator1, gridBagConstraints);

        jPanel7.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butCheck,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butCheck.text",
                new Object[] {})); // NOI18N
        butCheck.setPreferredSize(new java.awt.Dimension(146, 29));
        butCheck.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butCheckActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel7.add(butCheck, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jSeparator2, gridBagConstraints);

        jPanel8.setLayout(new java.awt.GridBagLayout());

        cbLageStatus.setMinimumSize(new java.awt.Dimension(200, 27));
        cbLageStatus.setPreferredSize(new java.awt.Dimension(200, 27));
        cbLageStatus.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbLageStatusActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        jPanel8.add(cbLageStatus, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel3.text",
                new Object[] {})); // NOI18N
        jLabel3.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel3.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jLabel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel4.text",
                new Object[] {})); // NOI18N
        jLabel4.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel4.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel8.add(jLabel4, gridBagConstraints);

        cbFreigabe.setMinimumSize(new java.awt.Dimension(200, 27));
        cbFreigabe.setPreferredSize(new java.awt.Dimension(200, 27));
        cbFreigabe.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbFreigabeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        jPanel8.add(cbFreigabe, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel8, gridBagConstraints);

        jPanel9.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel5,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel5.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(jLabel5, gridBagConstraints);

        txtUsr.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtUsr.text",
                new Object[] {})); // NOI18N
        txtUsr.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtUsrActionPerformed(evt);
                }
            });
        txtUsr.addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyReleased(final java.awt.event.KeyEvent evt) {
                    txtUsrKeyReleased(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel9.add(txtUsr, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel6,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel6.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel7,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel7.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(jLabel7, gridBagConstraints);

        txtTime.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtTime.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel9.add(txtTime, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel9.add(jxDate, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel9, gridBagConstraints);

        jPanel10.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel8,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel8.text",
                new Object[] {})); // NOI18N
        jLabel8.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel8.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel10.add(jLabel8, gridBagConstraints);

        txtTitle.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtTitle.text",
                new Object[] {})); // NOI18N
        txtTitle.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtTitleActionPerformed(evt);
                }
            });
        txtTitle.addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyReleased(final java.awt.event.KeyEvent evt) {
                    txtTitleKeyReleased(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel10.add(txtTitle, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel10, gridBagConstraints);

        jPanel11.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel9,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel9.text",
                new Object[] {})); // NOI18N
        jLabel9.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel9.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel11.add(jLabel9, gridBagConstraints);

        txtBeschreibung.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtBeschreibung.text",
                new Object[] {})); // NOI18N
        txtBeschreibung.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtBeschreibungActionPerformed(evt);
                }
            });
        txtBeschreibung.addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyReleased(final java.awt.event.KeyEvent evt) {
                    txtBeschreibungKeyReleased(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel11.add(txtBeschreibung, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel11, gridBagConstraints);

        jPanel12.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel10,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel10.text",
                new Object[] {})); // NOI18N
        jLabel10.setMinimumSize(new java.awt.Dimension(110, 20));
        jLabel10.setPreferredSize(new java.awt.Dimension(110, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel12.add(jLabel10, gridBagConstraints);

        txtBemerkung.setText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtBemerkung.text",
                new Object[] {})); // NOI18N
        txtBemerkung.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtBemerkungActionPerformed(evt);
                }
            });
        txtBemerkung.addKeyListener(new java.awt.event.KeyAdapter() {

                @Override
                public void keyReleased(final java.awt.event.KeyEvent evt) {
                    txtBemerkungKeyReleased(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel12.add(txtBemerkung, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        getContentPane().add(jSeparator3, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        if (butOk.isEnabled()) {
            final int answer = JOptionPane.showConfirmDialog(
                    this,
                    NbBundle.getMessage(UploadQpDialog.class, "UploadQpDialog.butCancelActionPerformed.message"),
                    NbBundle.getMessage(UploadQpDialog.class, "UploadQpDialog.butCancelActionPerformed.title"),
                    JOptionPane.YES_NO_OPTION);

            if (answer == JOptionPane.NO_OPTION) {
                return;
            }
        }

        cancelled = true;
        setVisible(false);
    } //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        final WaitingDialogThread wdt = new WaitingDialogThread(StaticSwingTools.getParentFrame(this),
                true,
                "Importiere Profile              ",
                null,
                100,
                true) {

                @Override
                protected Object doInBackground() throws Exception {
                    final String userName = SessionManager.getSession().getUser().getName();
                    final CidsBean qpUplBean = createQpUplObject();
                    final Set<Double> profileSet = reader.getProfiles();
                    int profNumber = 0;

                    wd.setMax(profileSet.size());

                    for (final Double profileId : profileSet) {
                        wd.setText("Importiere Profil " + (++profNumber) + "/" + profileSet.size());
                        wd.setProgress(profNumber);
                        final CidsBean qpNplBean = createQpNplObject(reader.getNpLine(profileId),
                                reader.getProfileContent(profileId).get(0),
                                qpUplBean);
                        final List<Object[]> dataList = new ArrayList<Object[]>();

                        for (final ProfileLine line : reader.getProfileContent(profileId)) {
                            final Integer qpId = getNewId();
                            final Object[] pktData = fillQpPkt(qpUplBean, qpNplBean, line);
                            dataList.add(pktData);
                        }

                        final ServerActionParameter pktDataParam = new ServerActionParameter(
                                AddProfileAction.ParameterType.DATA.toString(),
                                dataList.toArray(new Object[dataList.size()][]));
                        final Boolean objectsCreated = (Boolean)SessionManager.getProxy()
                                    .executeTask(
                                            AddProfileAction.TASK_NAME,
                                            AppBroker.getInstance().getDomain(),
                                            (Object)null,
                                            ConnectionContext.createDummy(),
                                            pktDataParam);
                    }

                    if ((txtRkFile.getText() != null) && !txtRkFile.getText().isEmpty()) {
                        final String fileName = "qp-" + String.valueOf(qpUplBean.getProperty("upl_nr")) + ".zip";
                        final String filePrefix = "qp/";
                        final String[] files = txtRkFile.getText().split(":");
                        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        final ZipOutputStream zout = new ZipOutputStream(bout);
                        for (final String f : files) {
                            final ZipEntry entry = new ZipEntry(f.substring(f.lastIndexOf("/") + 1));
                            // entry.set
                            zout.putNextEntry(entry);

                            final FileInputStream fis = new FileInputStream(f);
                            final byte[] tmp = new byte[256];
                            int count;

                            while ((count = fis.read(tmp)) != -1) {
                                zout.write(tmp, 0, count);
                            }
                        }
                        final InputStream is = new ByteArrayInputStream(bout.toByteArray());
                        WebDavHelper.createFolder(WEB_DAV_DIRECTORY + filePrefix, webDavClient);
                        WebDavHelper.uploadFileToWebDAV(
                            fileName,
                            is,
                            WEB_DAV_DIRECTORY
                                    + filePrefix,
                            webDavClient,
                            UploadQpDialog.this);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();

                        List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                                "qp_upl");

                        if ((services == null) || services.isEmpty()) {
                            final CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "dlm25w.qp_upl",
                                        cc));
                            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(layer);
                        }

                        services = FeatureServiceHelper.getCidsLayerServicesFromTree("qp_npl");

                        if ((services == null) || services.isEmpty()) {
                            final CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "dlm25w.qp_npl",
                                        cc));
                            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(layer);
                        }

                        services = FeatureServiceHelper.getCidsLayerServicesFromTree("qp_pkte");

                        if ((services == null) || services.isEmpty()) {
                            final CidsLayer layer = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "dlm25w.qp_pkte",
                                        cc));
                            AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(layer);
                        }

                        reloadService("qp_upl");
                        reloadService("qp_pkte");
                        reloadService("qp_npl");
                    } catch (Exception e) {
                        LOG.error("Error while importing QB Objects", e);
                    }
                }

                /**
                 * DOCUMENT ME!
                 *
                 * @param  name  DOCUMENT ME!
                 */
                private void reloadService(final String name) {
                    final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                            name);

                    for (final AbstractFeatureService featureService : services) {
                        featureService.retrieve(true);
                    }

                    if ((services != null) && !services.isEmpty()) {
                        final AttributeTable tablePf = AppBroker.getInstance()
                                    .getWatergisApp()
                                    .getAttributeTableByFeatureService(services.get(0));

                        if (tablePf != null) {
                            tablePf.reload();
                        }
                    }
                }
            };

        wdt.start();

        cancelled = false;
        setVisible(false);
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   qpUpl  DOCUMENT ME!
     * @param   qNpl   DOCUMENT ME!
     * @param   line   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object[] fillQpPkt(final CidsBean qpUpl, final CidsBean qNpl, final ProfileLine line) {
        final Object[] pktObject = new Object[14];
        final Double stat = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.STATION);

        pktObject[0] = AppBroker.getInstance().getOwnWwGr().getPrimaryKeyValue();
        pktObject[1] = qNpl.getProperty("qp_nr");
        pktObject[2] = qpUpl.getProperty("upl_nr");
        pktObject[3] = qpUpl.getProperty("l_calc");
        pktObject[4] = line.getField(ProfileReader.GAF_FIELDS.ID);
        pktObject[5] = (((stat != null) && (stat >= 0.0)) ? stat : null);
        pktObject[6] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Y);
        pktObject[7] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Z);
        pktObject[8] = line.getField(ProfileReader.GAF_FIELDS.KZ);
        pktObject[9] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RK);
        pktObject[10] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.BK);
        pktObject[11] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.HW);
        pktObject[12] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RW);
        pktObject[13] = line.getField(ProfileReader.GAF_FIELDS.HYK);

        return pktObject;
//        qpPkt.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
//        qpPkt.setProperty("qp_nr", qpUpl.getProperty("id"));
//        qpPkt.setProperty("upl_nr", qpUpl.getProperty("upl_nr"));
//        qpPkt.setProperty("l_calc", qpUpl.getProperty("l_calc"));
//        qpPkt.setProperty("gaf_id", line.getField(ProfileReader.GAF_FIELDS.ID));
//        qpPkt.setProperty(
//            "stat",
//            ((line.getFieldAsDouble(ProfileReader.GAF_FIELDS.STATION) >= 0.0)
//                ? line.getFieldAsDouble(ProfileReader.GAF_FIELDS.STATION) : null));
//        qpPkt.setProperty("y", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Y));
//        qpPkt.setProperty("z", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Z));
//        qpPkt.setProperty("kz", line.getField(ProfileReader.GAF_FIELDS.KZ));
//        qpPkt.setProperty("rk", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RK));
//        qpPkt.setProperty("bk", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.BK));
//        qpPkt.setProperty("hw", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.HW));
//        qpPkt.setProperty("rw", line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RW));
//        qpPkt.setProperty("hyk", line.getField(ProfileReader.GAF_FIELDS.HYK));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean createQpUplObject() throws Exception {
        final CidsBean newQpUplBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.qp_upl");
        final GregorianCalendar now = new GregorianCalendar();

        newQpUplBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
        newQpUplBean.setProperty("l_st", ((CidsLayerFeature)cbLageStatus.getSelectedItem()).getBean());
        newQpUplBean.setProperty("l_bezug", ((CidsLayerFeature)cbLage.getSelectedItem()).getBean());
        newQpUplBean.setProperty("h_bezug", ((CidsLayerFeature)cbHoehe.getSelectedItem()).getBean());
        newQpUplBean.setProperty("l_calc", (reader.isCalc() ? "x" : "-"));
        newQpUplBean.setProperty("upl_name", SessionManager.getSession().getUser().getName());
        newQpUplBean.setProperty("aufn_name", txtUsr.getText());
        newQpUplBean.setProperty("aufn_datum", jxDate.getDate());
        newQpUplBean.setProperty("aufn_zeit", txtTime.getText());
        newQpUplBean.setProperty("freigabe", ((CidsLayerFeature)cbFreigabe.getSelectedItem()).getBean());
        newQpUplBean.setProperty("titel", txtTitle.getText());
        newQpUplBean.setProperty("beschreib", txtBeschreibung.getText());
        newQpUplBean.setProperty("bemerkung", txtBemerkung.getText());
        newQpUplBean.setProperty("fis_g_user", SessionManager.getSession().getUser().getName());

        return newQpUplBean.persist(cc);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   npl   DOCUMENT ME!
     * @param   line  DOCUMENT ME!
     * @param   upl   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean createQpNplObject(final Geometry npl, final ProfileLine line, final CidsBean upl)
            throws Exception {
        final CidsBean newQpNplBean = CidsBeanUtils.createNewCidsBeanFromTableName("dlm25w.qp_npl");
        final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
        geomBean.setProperty("geo_field", npl);
        final GregorianCalendar now = new GregorianCalendar();
        final Double stat = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.STATION);

        newQpNplBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
        newQpNplBean.setProperty("geom", geomBean);
        newQpNplBean.setProperty("stat", (((stat != null) && (stat >= 0.0)) ? stat : null));
        newQpNplBean.setProperty("l_st", ((CidsLayerFeature)cbLageStatus.getSelectedItem()).getBean());
        newQpNplBean.setProperty("l_bezug", ((CidsLayerFeature)cbLage.getSelectedItem()).getBean());
        newQpNplBean.setProperty("h_bezug", ((CidsLayerFeature)cbHoehe.getSelectedItem()).getBean());
        newQpNplBean.setProperty("l_calc", (reader.isCalc() ? "x" : "-"));
        newQpNplBean.setProperty("upl_nr", upl.getProperty("upl_nr"));
        newQpNplBean.setProperty("upl_name", SessionManager.getSession().getUser().getName());
        newQpNplBean.setProperty("aufn_name", txtUsr.getText());
        newQpNplBean.setProperty("aufn_datum", new java.sql.Date(jxDate.getDate().getTime()));
        newQpNplBean.setProperty("aufn_zeit", txtTime.getText());
        newQpNplBean.setProperty("freigabe", ((CidsLayerFeature)cbFreigabe.getSelectedItem()).getBean());
        newQpNplBean.setProperty("titel", txtTitle.getText());
        newQpNplBean.setProperty("beschreib", txtBeschreibung.getText());
        newQpNplBean.setProperty("bemerkung", txtBemerkung.getText());
        newQpNplBean.setProperty("fis_g_user", SessionManager.getSession().getUser().getName());

        return newQpNplBean.persist(cc);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butGafFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butGafFileActionPerformed
        final File file = StaticSwingTools.chooseFile(
                lastPath,
                false,
                null,
                "Alle Dateien",
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            lastPath = file.getParent();
            txtGafFile.setText(file.getAbsolutePath());
            if ((txtGafFile.getText() != null) && !txtGafFile.getText().isEmpty()) {
                butColumns.setEnabled(true);
            }
        }
    } //GEN-LAST:event_butGafFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butRkFileActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butRkFileActionPerformed
        JFileChooser fc;

        try {
            fc = new ConfirmationJFileChooser(lastPath);
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(lastPath, new RestrictedFileSystemView());
        }

        final FileFilter fileFilter = new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return true;
                }

                @Override
                public String getDescription() {
                    return "Alle Dateien";
                }
            };
        fc.setAcceptAllFileFilterUsed(false);

        fc.setFileFilter(fileFilter);
        fc.setMultiSelectionEnabled(true);

        final int state = fc.showOpenDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (state == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();

            lastPath = file.getParent();
            txtRkFile.setText(file.getAbsolutePath());
        } else {
            txtRkFile.setText("");
        }
    } //GEN-LAST:event_butRkFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butColumnsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butColumnsActionPerformed
        reader = ProfileReaderFactory.getReader(new File(txtGafFile.getText()));
        final AbstractImportDialog dialog = reader.getImportDialog(StaticSwingTools.getParentFrame(this));

        if (dialog != null) {
            dialog.setAlwaysOnTop(true);
            StaticSwingTools.showDialog(dialog);

            if (dialog.isCancelled()) {
                reader = null;
                return;
            } else {
                butCheck.setEnabled((cbHoehe.getSelectedItem() != null) && (cbLage.getSelectedItem() != null)
                            && (reader != null));
            }
        }
    } //GEN-LAST:event_butColumnsActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbLageActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbLageActionPerformed
        butCheck.setEnabled((cbHoehe.getSelectedItem() != null) && (cbLage.getSelectedItem() != null)
                    && (reader != null));
        checkUploadButton();
    }                                                                          //GEN-LAST:event_cbLageActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbHoeheActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbHoeheActionPerformed
        butCheck.setEnabled((cbHoehe.getSelectedItem() != null) && (cbLage.getSelectedItem() != null)
                    && (reader != null));
        checkUploadButton();
    }                                                                           //GEN-LAST:event_cbHoeheActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCheckActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCheckActionPerformed
        reader.setLageBezug((CidsLayerFeature)cbLage.getSelectedItem());
        reader.setHoeheBezug((CidsLayerFeature)cbHoehe.getSelectedItem());
        final QpCheckResult result = reader.checkFileForHints();

        if (result != null) {
            final Object[] res = new Object[3];
            res[0] = result.getCorrect();
            res[1] = result.getIncorrect();
            res[2] = null;

            if ((result.getErrors() != null) && !result.getErrors().isEmpty()) {
                final StringBuilder sb = new StringBuilder();

                for (final QpCheckResult.ErrorResult er : result.getErrors()) {
                    sb.append("Zeile").append(er.getLine()).append(": ").append(er.getErrorText());
                }
            }
            res[2] = listToString(result.getErrors());
            JOptionPane.showMessageDialog(
                this,
                NbBundle.getMessage(UploadQpDialog.class, "UploadQpDialog.butCheckActionPerformed.message", res),
                NbBundle.getMessage(UploadQpDialog.class, "UploadQpDialog.butCheckActionPerformed.title"),
                JOptionPane.INFORMATION_MESSAGE);
        }

        checkPerformed = true;
    } //GEN-LAST:event_butCheckActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbLageStatusActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbLageStatusActionPerformed
        checkUploadButton();
    }                                                                                //GEN-LAST:event_cbLageStatusActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbFreigabeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbFreigabeActionPerformed
        checkUploadButton();
    }                                                                              //GEN-LAST:event_cbFreigabeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtTitleActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtTitleActionPerformed
        checkUploadButton();
    }                                                                            //GEN-LAST:event_txtTitleActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtBeschreibungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtBeschreibungActionPerformed
        checkUploadButton();
    }                                                                                   //GEN-LAST:event_txtBeschreibungActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtBemerkungActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtBemerkungActionPerformed
        checkUploadButton();
    }                                                                                //GEN-LAST:event_txtBemerkungActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtUsrActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtUsrActionPerformed
        checkUploadButton();
    }                                                                          //GEN-LAST:event_txtUsrActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtTitleKeyReleased(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_txtTitleKeyReleased
        checkUploadButton();
    }                                                                     //GEN-LAST:event_txtTitleKeyReleased

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtBeschreibungKeyReleased(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_txtBeschreibungKeyReleased
        checkUploadButton();
    }                                                                            //GEN-LAST:event_txtBeschreibungKeyReleased

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtBemerkungKeyReleased(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_txtBemerkungKeyReleased
        checkUploadButton();
    }                                                                         //GEN-LAST:event_txtBemerkungKeyReleased

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtUsrKeyReleased(final java.awt.event.KeyEvent evt) { //GEN-FIRST:event_txtUsrKeyReleased
        checkUploadButton();
    }                                                                   //GEN-LAST:event_txtUsrKeyReleased

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    static synchronized int getNewId() {
        return --idCounter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   list  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String listToString(final List<QpCheckResult.ErrorResult> list) {
        if ((list == null) || list.isEmpty()) {
            return "";
        } else {
            final StringBuilder sb = new StringBuilder();

            for (final QpCheckResult.ErrorResult error : list) {
                sb.append("Zeile ").append(error.getLine()).append(": ").append(error.getErrorText()).append("\n");
            }

            return sb.toString();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void checkUploadButton() {
        final boolean active = (cbFreigabe.getSelectedItem() != null) && (cbHoehe.getSelectedItem() != null)
                    && (cbLage.getSelectedItem() != null) && (cbLageStatus.getSelectedItem() != null)
                    && !txtBemerkung.getText().isEmpty() && !txtBeschreibung.getText().isEmpty()
                    && !txtTitle.getText().isEmpty() && !txtUsr.getText().isEmpty() && (reader != null)
                    && checkPerformed;

        butOk.setEnabled(active);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGafFile() {
        return txtGafFile.getText();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getRkFile() {
        if (!txtRkFile.getText().equals("")) {
            final File catFile = new File(txtRkFile.getText());

            if (catFile.exists()) {
                return txtRkFile.getText();
            }
        }

        return null;
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

        private static final transient UploadQpDialog INSTANCE = new UploadQpDialog(AppBroker.getInstance()
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
