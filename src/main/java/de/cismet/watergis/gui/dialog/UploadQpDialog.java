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

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.text.Format;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
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

import de.cismet.cismap.custom.attributerule.MessageDialog;
import de.cismet.cismap.custom.attributerule.WatergisDefaultRuleSet;

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
import de.cismet.watergis.profile.WPROFReader;

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

    private MetaClass LAWA_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");
    private final Format formatter = new WatergisDefaultRuleSet.TimeFormatter();
    private boolean cancelled = false;
    private String lastPath = DownloadManager.instance().getDestinationDirectory().toString();
    private MetaClass LAGESTATUS_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_hyk");
    private MetaClass LAGE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_l_bezug");
    private MetaClass HOEHE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_h_bezug");
    private MetaClass FREIGABE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_freigabe");
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
    private javax.swing.JButton butOk1;
    private javax.swing.JButton butRoute;
    private javax.swing.JButton butUploadFiles;
    private javax.swing.JComboBox cbFreigabe;
    private javax.swing.JComboBox cbHoehe;
    private javax.swing.JComboBox cbLage;
    private javax.swing.JComboBox cbLageStatus;
    private javax.swing.JComboBox cbRoute;
    private javax.swing.JDialog diaRoute;
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
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private org.jdesktop.swingx.JXDatePicker jxDate;
    private javax.swing.JLabel labCheck;
    private javax.swing.JLabel labCol;
    private javax.swing.JLabel labRoute;
    private javax.swing.JLabel lblRoute;
    private javax.swing.JList<String> liFiles;
    private javax.swing.JTextField txtBemerkung;
    private javax.swing.JTextField txtBeschreibung;
    private javax.swing.JTextField txtGafFile;
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
        AutoCompleteDecorator.decorate(cbRoute, new ObjectToStringConverter() {

                @Override
                public String getPreferredStringForItem(final Object item) {
                    if (item instanceof CidsLayerFeature) {
                        return String.valueOf(((CidsLayerFeature)item).getProperty("la_cd"));
                    } else {
                        return String.valueOf(item);
                    }
                }
            });
        cbRoute.setRenderer(new DefaultListCellRenderer() {

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
                        ((JLabel)c).setText(String.valueOf(((CidsLayerFeature)value).getProperty("la_cd")));
                    }

                    return c;
                }
            });
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
        setSize(195, 700);
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
        if (b) {
            butColumns.setEnabled(false);
            butOk.setEnabled(false);
            butCheck.setEnabled(false);
            txtBemerkung.setText("");
            txtBeschreibung.setText("");
//            jxDate.setDate(new Date());
            txtGafFile.setText("");
            liFiles.setModel(new DefaultListModel<String>());
            txtTime.setText("");
            txtTitle.setText("");
            txtUsr.setText("");
            cbFreigabe.setSelectedItem(null);
            cbLageStatus.setSelectedItem(null);

            final InitComboboxThread thread = new InitComboboxThread("InitComboThread");
            thread.start();
            cancelled = true;
            resetDialog();
        }

        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     */
    private void resetDialog() {
        checkPerformed = false;
        reader = null;
        lblRoute.setText("");
        butRoute.setEnabled(false);
        labRoute.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons22/ok.png")));

        labCol.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons22/stop.png")));

        labCheck.setIcon(new javax.swing.ImageIcon(
                getClass().getResource("/de/cismet/watergis/res/icons22/stop.png")));
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

        diaRoute = new javax.swing.JDialog();
        butOk1 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        cbRoute = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAWA_MC, true);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        liFiles = new javax.swing.JList<>();
        butUploadFiles = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        butColumns = new javax.swing.JButton();
        labCol = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        butRoute = new javax.swing.JButton();
        labRoute = new javax.swing.JLabel();
        lblRoute = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        cbLage = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAGE_MC, false);
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cbHoehe = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(HOEHE_MC, false);
        jSeparator1 = new javax.swing.JSeparator();
        jPanel7 = new javax.swing.JPanel();
        butCheck = new javax.swing.JButton();
        labCheck = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jPanel8 = new javax.swing.JPanel();
        cbLageStatus = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(LAGESTATUS_MC, false);
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbFreigabe = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(FREIGABE_MC, false);
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtUsr = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtTime = new JFormattedTextField(formatter);
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

        diaRoute.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        diaRoute.setTitle(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.diaRoute.title",
                new Object[] {})); // NOI18N
        diaRoute.setAlwaysOnTop(true);
        diaRoute.setModal(true);
        diaRoute.getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk1,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butOk1.text",
                new Object[] {})); // NOI18N
        butOk1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butOk1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        diaRoute.getContentPane().add(butOk1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel11,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.jLabel11.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        diaRoute.getContentPane().add(jLabel11, gridBagConstraints);

        cbRoute.setMinimumSize(new java.awt.Dimension(200, 27));
        cbRoute.setPreferredSize(new java.awt.Dimension(200, 27));
        cbRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        diaRoute.getContentPane().add(cbRoute, gridBagConstraints);

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
        butGafFile.setMaximumSize(new java.awt.Dimension(146, 29));
        butGafFile.setMinimumSize(new java.awt.Dimension(146, 29));
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

        jPanel4.setPreferredSize(new java.awt.Dimension(252, 100));
        jPanel4.setLayout(new java.awt.GridBagLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(66, 75));

        liFiles.setModel(new javax.swing.AbstractListModel<String>() {

                String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };

                @Override
                public int getSize() {
                    return strings.length;
                }
                @Override
                public String getElementAt(final int i) {
                    return strings[i];
                }
            });
        jScrollPane1.setViewportView(liFiles);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butUploadFiles,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butUploadFiles.text",
                new Object[] {})); // NOI18N
        butUploadFiles.setMaximumSize(new java.awt.Dimension(146, 29));
        butUploadFiles.setMinimumSize(new java.awt.Dimension(146, 29));
        butUploadFiles.setPreferredSize(new java.awt.Dimension(146, 29));
        butUploadFiles.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butUploadFilesActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel4.add(butUploadFiles, gridBagConstraints);

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
        butColumns.setMinimumSize(new java.awt.Dimension(146, 29));
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
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 140);
        jPanel5.add(butColumns, gridBagConstraints);

        labCol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/watergis/res/icons22/stop.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labCol,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.labCol.text",
                new Object[] {}));                                                                                     // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel5.add(labCol, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel5, gridBagConstraints);

        jPanel13.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butRoute,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.butRoute.text",
                new Object[] {})); // NOI18N
        butRoute.setEnabled(false);
        butRoute.setMinimumSize(new java.awt.Dimension(146, 29));
        butRoute.setPreferredSize(new java.awt.Dimension(146, 29));
        butRoute.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    butRouteActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        jPanel13.add(butRoute, gridBagConstraints);

        labRoute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/watergis/res/icons22/ok.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labRoute,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.labRoute.text",
                new Object[] {}));                                                                                     // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel13.add(labRoute, gridBagConstraints);

        lblRoute.setMaximumSize(new java.awt.Dimension(130, 20));
        lblRoute.setMinimumSize(new java.awt.Dimension(130, 20));
        lblRoute.setPreferredSize(new java.awt.Dimension(130, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        jPanel13.add(lblRoute, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel13, gridBagConstraints);

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
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
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
        butCheck.setMinimumSize(new java.awt.Dimension(146, 29));
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

        labCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/watergis/res/icons22/stop.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labCheck,
            org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.labCheck.text",
                new Object[] {}));                                                                                       // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        jPanel7.add(labCheck, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
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
        gridBagConstraints.gridy = 11;
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
        txtTime.setToolTipText(org.openide.util.NbBundle.getMessage(
                UploadQpDialog.class,
                "UploadQpDialog.txtTime.toolTipText",
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
        gridBagConstraints.gridy = 13;
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
        gridBagConstraints.gridy = 14;
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
        gridBagConstraints.gridy = 15;
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
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        getContentPane().add(jPanel12, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 17;
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

                    final String fileName = "qp-" + String.valueOf(qpUplBean.getProperty("upl_nr")) + ".zip";
                    final String filePrefix = "qp/";
                    final ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    final ZipOutputStream zout = new ZipOutputStream(bout);
                    final List<String> files = new ArrayList<>();

                    files.add(txtGafFile.getText());

                    if ((liFiles.getModel() != null) && (liFiles.getModel().getSize() > 0)) {
                        for (int i = 0; i < liFiles.getModel().getSize(); ++i) {
                            files.add(liFiles.getModel().getElementAt(i));
                        }
                    }

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
                        zout.closeEntry();
                    }
                    zout.close();
                    final InputStream is = new ByteArrayInputStream(bout.toByteArray());
                    WebDavHelper.createFolder(WEB_DAV_DIRECTORY + filePrefix, webDavClient);
                    WebDavHelper.uploadFileToWebDAV(
                        fileName,
                        is,
                        WEB_DAV_DIRECTORY
                                + filePrefix,
                        webDavClient,
                        UploadQpDialog.this);

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

        pktObject[0] = qpUpl.getPrimaryKeyValue();
        pktObject[1] = qNpl.getPrimaryKeyValue();
        pktObject[2] = line.getField(ProfileReader.GAF_FIELDS.ID);
        pktObject[3] = (((stat != null) && (stat >= 0.0)) ? stat : null);
        pktObject[4] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Y);
        pktObject[5] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.Z);
        pktObject[6] = line.getField(ProfileReader.GAF_FIELDS.KZ);
        pktObject[7] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RK);
        pktObject[8] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.BK);
        pktObject[9] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.HW);
        pktObject[10] = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.RW);
        pktObject[11] = line.getField(ProfileReader.GAF_FIELDS.HYK);

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
        final java.sql.Date aufnDate = ((jxDate.getDate() == null) ? null
                                                                   : new java.sql.Date(jxDate.getDate().getTime()));

        newQpUplBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
        newQpUplBean.setProperty("l_st", ((CidsLayerFeature)cbLageStatus.getSelectedItem()).getBean());
        newQpUplBean.setProperty("l_bezug", ((CidsLayerFeature)cbLage.getSelectedItem()).getBean());
        newQpUplBean.setProperty("h_bezug", ((CidsLayerFeature)cbHoehe.getSelectedItem()).getBean());
        newQpUplBean.setProperty("l_calc", (reader.isCalc() ? "calc" : "-"));
        newQpUplBean.setProperty("upl_name", SessionManager.getSession().getUser().getName());
        newQpUplBean.setProperty("aufn_name", txtUsr.getText());
        newQpUplBean.setProperty("aufn_datum", aufnDate);
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
        final Double stat = line.getFieldAsDouble(ProfileReader.GAF_FIELDS.STATION);

        newQpNplBean.setProperty("ww_gr", AppBroker.getInstance().getOwnWwGr());
        newQpNplBean.setProperty("geom", geomBean);
        newQpNplBean.setProperty("stat", (((stat != null) && (stat >= 0.0)) ? stat : null));
        newQpNplBean.setProperty("l_st", ((CidsLayerFeature)cbLageStatus.getSelectedItem()).getBean());
        newQpNplBean.setProperty("qp_upl", upl);
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

            resetDialog();
        }
    } //GEN-LAST:event_butGafFileActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butUploadFilesActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butUploadFilesActionPerformed
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
            final File[] files = fc.getSelectedFiles();
            final DefaultListModel<String> model = new DefaultListModel<>();

            for (final File file : files) {
                lastPath = file.getParent();
                model.addElement(file.getAbsolutePath());
            }
            liFiles.setModel(model);
        } else {
            liFiles.setModel(new DefaultListModel<String>());
        }
    } //GEN-LAST:event_butUploadFilesActionPerformed

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
                labCol.setIcon(new javax.swing.ImageIcon(
                        getClass().getResource("/de/cismet/watergis/res/icons22/stop.png")));
                return;
            } else {
                butCheck.setEnabled((cbHoehe.getSelectedItem() != null) && (cbLage.getSelectedItem() != null)
                            && (reader != null));
                labCol.setIcon(new javax.swing.ImageIcon(
                        getClass().getResource("/de/cismet/watergis/res/icons22/ok.png")));

                if (dialog.isRouteRequired()) {
                    butRoute.setEnabled(true);
                    labRoute.setIcon(new javax.swing.ImageIcon(
                            getClass().getResource("/de/cismet/watergis/res/icons22/stop.png")));
                } else {
                    butRoute.setEnabled(false);
                    labRoute.setIcon(new javax.swing.ImageIcon(
                            getClass().getResource("/de/cismet/watergis/res/icons22/ok.png")));
                }
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

            if (result.getCorrect() == 0) {
                final String title = "Import-Info";
                final String text = "Die Datei enhält keine gültigen QP - ein Upload ist nicht möglich";
                final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(), true, text, title);
                d.setSize(500, 80);
                StaticSwingTools.showDialog(d);
            } else if (result.getIncorrect() > 0) {
                final String title = "Import-Info";
                final String text = "Beim Upload werden nur die gültigen QP berücksichtigt!";
                final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(), true, text, title);
                d.setSize(500, 80);
                StaticSwingTools.showDialog(d);
            }

            if (result.getCorrect() > 0) {
                checkPerformed = true;
                labCheck.setIcon(new javax.swing.ImageIcon(
                        getClass().getResource("/de/cismet/watergis/res/icons22/ok.png")));
            }
        }
        checkUploadButton();
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
     * @param  evt  DOCUMENT ME!
     */
    private void butRouteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butRouteActionPerformed
        diaRoute.setSize(300, 150);
        diaRoute.setAlwaysOnTop(true);
        StaticSwingTools.centerWindowOnScreen(diaRoute);
    }                                                                            //GEN-LAST:event_butRouteActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOk1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOk1ActionPerformed
        if ((cbRoute.getSelectedItem() != null) && (reader instanceof WPROFReader)) {
            final CidsLayerFeature routeFeature = (CidsLayerFeature)cbRoute.getSelectedItem();
            ((WPROFReader)reader).setRoute(routeFeature);
            diaRoute.setVisible(false);
            labRoute.setIcon(new javax.swing.ImageIcon(
                    getClass().getResource("/de/cismet/watergis/res/icons22/ok.png")));
            lblRoute.setText(String.valueOf(routeFeature.getProperty("la_cd")));
        }
    }                                                                          //GEN-LAST:event_butOk1ActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbRouteActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbRouteActionPerformed
    }                                                                           //GEN-LAST:event_cbRouteActionPerformed

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
        List<QpCheckResult.ErrorResult> errors = null;
        final int size = ((list != null) ? list.size() : 0);
        boolean errorCut = false;
        final int MAX_SIZE = 20;

        if (size > MAX_SIZE) {
            errors = new ArrayList<>();

            for (int i = 0; i < MAX_SIZE; ++i) {
                errors.add(list.get(i));
            }

            errorCut = true;
        } else if (list != null) {
            errors = new ArrayList<>(list);
        }

        if ((errors == null) || errors.isEmpty()) {
            return "keine";
        } else {
            final StringBuilder sb = new StringBuilder();

            for (final QpCheckResult.ErrorResult error : errors) {
                sb.append("Zeile ").append(error.getLine()).append(": ").append(error.getErrorText()).append("\n");
            }

            if (errorCut) {
                sb.append("... weitere Fehler in ").append(size - MAX_SIZE).append(" Zeilen\n");
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
                    && (reader != null) && checkPerformed
                    && labRoute.getIcon().toString().endsWith("ok.png");

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
    private final class InitComboboxThread extends Thread {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new InitComboboxThread object.
         *
         * @param  name  DOCUMENT ME!
         */
        public InitComboboxThread(final String name) {
            super(name);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            boolean done = false;

            do {
                if ((cbHoehe.getModel().getSize() > 1) && (cbLage.getModel().getSize() > 1)
                            && (cbLageStatus.getModel().getSize() > 1)) {
                    EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                cbHoehe.setSelectedIndex(0);
                                cbLage.setSelectedIndex(0);
                                cbLageStatus.setSelectedIndex(cbLageStatus.getModel().getSize() - 1);
                            }
                        });
                    done = true;
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    // nothing to do
                }
            } while (!done);
        }
    }

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
