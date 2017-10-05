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
package de.cismet.watergis.gui.panels;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.Validator;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.lang.ref.SoftReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import de.cismet.cids.custom.watergis.server.search.CalculateFgLa;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.editors.DefaultCustomObjectEditor;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerFeatureFilter;
import de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.RouteTableCellEditor;
import de.cismet.cismap.linearreferencing.TableStationEditor;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.tools.CismetThreadPool;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.CidsBeanUtils;
import de.cismet.watergis.utils.ConversionUtils;
import de.cismet.watergis.utils.RendererTools;

/**
 * A editor class that can be used to edit/render foto cids layer.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PhotoEditor extends javax.swing.JPanel implements DisposableCidsBeanStore, PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PhotoEditor.class);
    private static final int CACHE_SIZE = 8;
    public static final Map<String, SoftReference<BufferedImage>> IMAGE_CACHE =
        new LinkedHashMap<String, SoftReference<BufferedImage>>(CACHE_SIZE) {

            @Override
            protected boolean removeEldestEntry(final Map.Entry<String, SoftReference<BufferedImage>> eldest) {
                return size() >= CACHE_SIZE;
            }
        };

    private static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

    //~ Instance fields --------------------------------------------------------

    private MetaClass L_ST_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 148);
    private MetaClass L_RL_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 113);
    private MetaClass FREIGABE_MC = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, 189);
    private CidsBean cidsBean;
    private CidsLayerFeature feature;
    private String webDavDirectory;
    private WebDavClient webDavClient;
    private BufferedImage image;
    private Timer timer;
    private ImageResizeWorker currentResizeWorker;
    private Dimension lastDims;
    private RouteTableCellEditor routeCellEditor = new RouteTableCellEditor("dlm25w.fg_ba", "ba_st", false);
    private boolean firstInit = true;
    private PhotoWrapper wrapper = new PhotoWrapper(null);

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cbFreigabe;
    private javax.swing.JComboBox<String> cbReLi;
    private javax.swing.JComboBox<String> cbStatus;
    private de.cismet.cids.editors.DefaultBindableDateChooser dateChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel labAufnDatum;
    private javax.swing.JLabel labAufnName;
    private javax.swing.JLabel labBaCd;
    private javax.swing.JLabel labBaCdVal;
    private javax.swing.JLabel labBemerkung;
    private javax.swing.JLabel labBeschreibung;
    private javax.swing.JLabel labEmpty;
    private javax.swing.JLabel labFoto;
    private javax.swing.JLabel labFotoVal;
    private javax.swing.JLabel labFreigabe;
    private javax.swing.JLabel labId;
    private javax.swing.JLabel labIdVal;
    private javax.swing.JLabel labImage;
    private javax.swing.JLabel labLRl;
    private javax.swing.JLabel labLaCd;
    private javax.swing.JLabel labLaCdVal;
    private javax.swing.JLabel labLst;
    private javax.swing.JLabel labRe;
    private javax.swing.JLabel labStatBa;
    private javax.swing.JLabel labStatLa;
    private javax.swing.JLabel labStatLaVal;
    private javax.swing.JLabel labTitle;
    private javax.swing.JLabel labUplDatum;
    private javax.swing.JLabel labUplDatumVal;
    private javax.swing.JLabel labUplName;
    private javax.swing.JLabel labUplNameVal;
    private javax.swing.JLabel labWinkel;
    private org.jdesktop.swingx.JXBusyLabel lblBusy;
    private org.jdesktop.swingx.JXBusyLabel lblBusyLoad;
    private javax.swing.JPanel panAufn;
    private javax.swing.JPanel panBesch;
    private javax.swing.JPanel panFreigabe;
    private javax.swing.JPanel panGewaesserBezug;
    private javax.swing.JPanel panImage;
    private javax.swing.JPanel panReLi;
    private javax.swing.JPanel panRouteCombo;
    private javax.swing.JPanel panStatEdit;
    private javax.swing.JPanel panStatus;
    private javax.swing.JPanel panUpload;
    private javax.swing.JPanel panVerortung;
    private javax.swing.JTextArea taBemerkung;
    private javax.swing.JTextArea taBeschreibung;
    private javax.swing.JTextArea taTitle;
    private javax.swing.JTextField txtAufn;
    private javax.swing.JTextField txtAufn1;
    private javax.swing.JTextField txtHo;
    private javax.swing.JTextField txtRe;
    private javax.swing.JTextField txtWinklel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PhotoEditor object.
     */
    public PhotoEditor() {
        initComponents();
        setFilter();
        labBaCdVal.setVisible(false);
    }

    /**
     * Creates new form Photo.
     *
     * @param  webDavClient     DOCUMENT ME!
     * @param  webDavDirectory  DOCUMENT ME!
     */
    public PhotoEditor(final WebDavClient webDavClient, final String webDavDirectory) {
        this.webDavDirectory = webDavDirectory;
        this.webDavClient = webDavClient;
        initComponents();
        setFilter();
        labBaCdVal.setVisible(false);

        lblBusy.setBusy(false);

        timer = new javax.swing.Timer(300, new ActionListener() {

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        if (currentResizeWorker != null) {
                            currentResizeWorker.cancel(true);
                        }
                        currentResizeWorker = new ImageResizeWorker();
                        CismetThreadPool.execute(currentResizeWorker);
                    }
                });
        timer.setRepeats(false);

        panImage.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(final ComponentEvent e) {
                    // Bei Windows wird dieses Event manchmal in einer Endlosschleife gefeuert.
                    final double width = e.getComponent().getSize().getWidth();
                    final double height = e.getComponent().getSize().getHeight();
                    if ((lastDims == null)
                                || ((Math.abs(lastDims.getHeight() - height) > 5)
                                    || (Math.abs(lastDims.getWidth() - width) > 5))) {
                        if ((image != null) && !lblBusy.isBusy()) {
                            lastDims = e.getComponent().getSize();
                            showWait(true);
                            timer.restart();
                        }
                    }
                }
            });
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

                    return (bean.getProperty("nicht_qp") != null)
                                && (Boolean)bean.getProperty("nicht_qp");
                }
            };

        final CidsLayerFeatureFilter photoFilter = new CidsLayerFeatureFilter() {

                @Override
                public boolean accept(final CidsLayerFeature bean) {
                    if (bean == null) {
                        return true;
                    }

                    return (bean.getProperty("foto") != null)
                                && (Boolean)bean.getProperty("foto");
                }
            };
        ((DefaultCidsLayerBindableReferenceCombo)cbReLi).setBeanFilter(photoFilter);
        ((DefaultCidsLayerBindableReferenceCombo)cbStatus).setBeanFilter(filter);
        ((DefaultCidsLayerBindableReferenceCombo)cbFreigabe).setBeanFilter(photoFilter);
//        feature.getCatalogueCombo("l_st").setBeanFilter(filter);
//        feature.getCatalogueCombo("l_rl").setBeanFilter(photoFilter);
//        feature.getCatalogueCombo("freigabe").setBeanFilter(photoFilter);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the wrapper
     */
    public PhotoWrapper getWrapper() {
        return wrapper;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wrapper  the wrapper to set
     */
    public void setWrapper(final PhotoWrapper wrapper) {
        this.wrapper = wrapper;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jPanel1 = new javax.swing.JPanel();
        labFoto = new javax.swing.JLabel();
        labFotoVal = new javax.swing.JLabel();
        panUpload = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        labUplName = new javax.swing.JLabel();
        labUplNameVal = new javax.swing.JLabel();
        labUplDatum = new javax.swing.JLabel();
        labUplDatumVal = new javax.swing.JLabel();
        labId = new javax.swing.JLabel();
        labIdVal = new javax.swing.JLabel();
        panVerortung = new javax.swing.JPanel();
        labRe = new javax.swing.JLabel();
        txtRe = new javax.swing.JTextField();
        txtHo = new javax.swing.JTextField();
        labWinkel = new javax.swing.JLabel();
        txtWinklel = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        panGewaesserBezug = new javax.swing.JPanel();
        labBaCd = new javax.swing.JLabel();
        labLaCd = new javax.swing.JLabel();
        labBaCdVal = new javax.swing.JLabel();
        labLaCdVal = new javax.swing.JLabel();
        labLst = new javax.swing.JLabel();
        labLRl = new javax.swing.JLabel();
        labStatLa = new javax.swing.JLabel();
        labStatBa = new javax.swing.JLabel();
        labStatLaVal = new javax.swing.JLabel();
        panStatEdit = new javax.swing.JPanel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        panRouteCombo = new javax.swing.JPanel();
        panReLi = new javax.swing.JPanel();
        cbReLi = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(L_RL_MC, true);
        panStatus = new javax.swing.JPanel();
        cbStatus = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(L_ST_MC, true);
        panAufn = new javax.swing.JPanel();
        labAufnName = new javax.swing.JLabel();
        labAufnDatum = new javax.swing.JLabel();
        txtAufn = new javax.swing.JTextField();
        labFreigabe = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtAufn1 = new javax.swing.JTextField();
        dateChooser = new de.cismet.cids.editors.DefaultBindableDateChooser();
        panFreigabe = new javax.swing.JPanel();
        cbFreigabe = new de.cismet.cismap.cidslayer.DefaultCidsLayerBindableReferenceCombo(FREIGABE_MC, true);
        panBesch = new javax.swing.JPanel();
        labTitle = new javax.swing.JLabel();
        labBemerkung = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taBemerkung = new javax.swing.JTextArea();
        labBeschreibung = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taBeschreibung = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        taTitle = new javax.swing.JTextArea();
        panImage = new javax.swing.JPanel();
        labImage = new javax.swing.JLabel();
        lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
        jPanel3 = new javax.swing.JPanel();
        lblBusyLoad = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));
        jPanel2 = new javax.swing.JPanel();
        labEmpty = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        labFoto.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labFoto,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labFoto.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 15, 10, 0);
        jPanel1.add(labFoto, gridBagConstraints);

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.foto}"),
                labFotoVal,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 0);
        jPanel1.add(labFotoVal, gridBagConstraints);

        panUpload.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.jLabel1.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        panUpload.add(jLabel1, gridBagConstraints);

        labUplName.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labUplName,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labUplName.text", new Object[] {})); // NOI18N
        labUplName.setPreferredSize(new java.awt.Dimension(103, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panUpload.add(labUplName, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.upl_name}"),
                labUplNameVal,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panUpload.add(labUplNameVal, gridBagConstraints);

        labUplDatum.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labUplDatum,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labUplDatum.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panUpload.add(labUplDatum, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panUpload.add(labUplDatumVal, gridBagConstraints);

        labId.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labId,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labId.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panUpload.add(labId, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panUpload.add(labIdVal, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel1.add(panUpload, gridBagConstraints);

        panVerortung.setLayout(new java.awt.GridBagLayout());

        labRe.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labRe,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labRe.text", new Object[] {})); // NOI18N
        labRe.setPreferredSize(new java.awt.Dimension(103, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panVerortung.add(labRe, gridBagConstraints);

        txtRe.setMinimumSize(new java.awt.Dimension(250, 25));
        txtRe.setPreferredSize(new java.awt.Dimension(100, 25));
        txtRe.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtReFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panVerortung.add(txtRe, gridBagConstraints);

        txtHo.setMinimumSize(new java.awt.Dimension(250, 25));
        txtHo.setPreferredSize(new java.awt.Dimension(100, 25));
        txtHo.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtHoFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 0, 10, 10);
        panVerortung.add(txtHo, gridBagConstraints);

        labWinkel.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labWinkel,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labWinkel.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 15, 10, 10);
        panVerortung.add(labWinkel, gridBagConstraints);

        txtWinklel.setMinimumSize(new java.awt.Dimension(120, 25));
        txtWinklel.setPreferredSize(new java.awt.Dimension(120, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.winkel}"),
                txtWinklel,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue("");
        binding.setSourceUnreadableValue("");
        binding.setConverter(new DoubleConverter());
        bindingGroup.addBinding(binding);

        txtWinklel.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusGained(final java.awt.event.FocusEvent evt) {
                    txtWinklelFocusGained(evt);
                }
                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtWinklelFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panVerortung.add(txtWinklel, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel2,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.jLabel2.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 0, 0);
        panVerortung.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel1.add(panVerortung, gridBagConstraints);

        panGewaesserBezug.setLayout(new java.awt.GridBagLayout());

        labBaCd.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labBaCd,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labBaCd.text", new Object[] {})); // NOI18N
        labBaCd.setPreferredSize(new java.awt.Dimension(103, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labBaCd, gridBagConstraints);

        labLaCd.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labLaCd,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labLaCd.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labLaCd, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.ba_st.route.ba_cd}"),
                labBaCdVal,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setSourceNullValue("");
        binding.setSourceUnreadableValue("null");
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labBaCdVal, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labLaCdVal, gridBagConstraints);

        labLst.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labLst,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labLst.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labLst, gridBagConstraints);

        labLRl.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labLRl,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labLRl.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labLRl, gridBagConstraints);

        labStatLa.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labStatLa,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labStatLa.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labStatLa, gridBagConstraints);

        labStatBa.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labStatBa,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labStatBa.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labStatBa, gridBagConstraints);

        labStatLaVal.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(labStatLaVal, gridBagConstraints);

        panStatEdit.setPreferredSize(new java.awt.Dimension(120, 25));
        panStatEdit.setLayout(new java.awt.GridLayout(1, 0));

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.ba_st.wert}"),
                jSpinner1,
                org.jdesktop.beansbinding.BeanProperty.create("value"));
        binding.setSourceNullValue(0);
        binding.setSourceUnreadableValue(0);
        bindingGroup.addBinding(binding);

        panStatEdit.add(jSpinner1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        panGewaesserBezug.add(panStatEdit, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel3,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.jLabel3.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 10, 0);
        panGewaesserBezug.add(jLabel3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(panRouteCombo, gridBagConstraints);

        panReLi.setMinimumSize(new java.awt.Dimension(250, 25));
        panReLi.setPreferredSize(new java.awt.Dimension(210, 25));
        panReLi.setLayout(new java.awt.GridLayout(1, 0));

        cbReLi.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.lRl}"),
                cbReLi,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        panReLi.add(cbReLi);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(panReLi, gridBagConstraints);

        panStatus.setMinimumSize(new java.awt.Dimension(120, 25));
        panStatus.setPreferredSize(new java.awt.Dimension(120, 25));
        panStatus.setLayout(new java.awt.GridLayout(1, 0));

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.lSt}"),
                cbStatus,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        panStatus.add(cbStatus);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(panStatus, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel1.add(panGewaesserBezug, gridBagConstraints);

        panAufn.setLayout(new java.awt.GridBagLayout());

        labAufnName.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labAufnName,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labAufnName.text", new Object[] {})); // NOI18N
        labAufnName.setPreferredSize(new java.awt.Dimension(103, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(labAufnName, gridBagConstraints);

        labAufnDatum.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labAufnDatum,
            org.openide.util.NbBundle.getMessage(
                PhotoEditor.class,
                "PhotoEditor.labAufnDatum.text",
                new Object[] {}));                                // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(labAufnDatum, gridBagConstraints);

        txtAufn.setMinimumSize(new java.awt.Dimension(250, 25));
        txtAufn.setPreferredSize(new java.awt.Dimension(210, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.aufnahmename}"),
                txtAufn,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        txtAufn.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusGained(final java.awt.event.FocusEvent evt) {
                    txtAufnFocusGained(evt);
                }
                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtAufnFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(txtAufn, gridBagConstraints);

        labFreigabe.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labFreigabe,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labFreigabe.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(labFreigabe, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel4,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.jLabel4.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 7, 10, 0);
        panAufn.add(jLabel4, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        txtAufn1.setMinimumSize(new java.awt.Dimension(85, 25));
        txtAufn1.setPreferredSize(new java.awt.Dimension(85, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.aufnahmezeit}"),
                txtAufn1,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        txtAufn1.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtAufn1FocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanel4.add(txtAufn1, gridBagConstraints);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.aufndatum}"),
                dateChooser,
                org.jdesktop.beansbinding.BeanProperty.create("date"));
        binding.setConverter(dateChooser.getConverter());
        bindingGroup.addBinding(binding);

        dateChooser.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    dateChooserFocusLost(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(dateChooser, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(jPanel4, gridBagConstraints);

        panFreigabe.setMinimumSize(new java.awt.Dimension(120, 25));
        panFreigabe.setPreferredSize(new java.awt.Dimension(120, 25));
        panFreigabe.setLayout(new java.awt.GridLayout(1, 0));

        cbFreigabe.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.freigabe}"),
                cbFreigabe,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        panFreigabe.add(cbFreigabe);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(panFreigabe, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel1.add(panAufn, gridBagConstraints);

        panBesch.setLayout(new java.awt.GridBagLayout());

        labTitle.setFont(new java.awt.Font("Ubuntu", 1, 15));                                                       // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labTitle,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labTitle.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(labTitle, gridBagConstraints);

        labBemerkung.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labBemerkung,
            org.openide.util.NbBundle.getMessage(
                PhotoEditor.class,
                "PhotoEditor.labBemerkung.text",
                new Object[] {}));                                // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(labBemerkung, gridBagConstraints);

        taBemerkung.setColumns(20);
        taBemerkung.setRows(4);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.bemerkung}"),
                taBemerkung,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        taBemerkung.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    taBemerkungFocusLost(evt);
                }
            });
        jScrollPane1.setViewportView(taBemerkung);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(jScrollPane1, gridBagConstraints);

        labBeschreibung.setFont(new java.awt.Font("Ubuntu", 1, 15)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(
            labBeschreibung,
            org.openide.util.NbBundle.getMessage(
                PhotoEditor.class,
                "PhotoEditor.labBeschreibung.text",
                new Object[] {}));                                   // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(labBeschreibung, gridBagConstraints);

        taBeschreibung.setColumns(20);
        taBeschreibung.setRows(4);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.beschreibung}"),
                taBeschreibung,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        taBeschreibung.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    taBeschreibungFocusLost(evt);
                }
            });
        jScrollPane2.setViewportView(taBeschreibung);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(jScrollPane2, gridBagConstraints);

        taTitle.setColumns(20);
        taTitle.setRows(4);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${wrapper.titel}"),
                taTitle,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        taTitle.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    taTitleFocusLost(evt);
                }
            });
        jScrollPane3.setViewportView(taTitle);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panBesch.add(jScrollPane3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        jPanel1.add(panBesch, gridBagConstraints);

        panImage.setMinimumSize(new java.awt.Dimension(300, 300));
        panImage.setPreferredSize(new java.awt.Dimension(300, 300));
        panImage.setLayout(new java.awt.CardLayout());

        labImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        panImage.add(labImage, "image");

        lblBusy.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBusy.setMaximumSize(new java.awt.Dimension(140, 40));
        lblBusy.setMinimumSize(new java.awt.Dimension(140, 40));
        lblBusy.setPreferredSize(new java.awt.Dimension(140, 40));
        panImage.add(lblBusy, "busy");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        jPanel1.add(panImage, gridBagConstraints);

        add(jPanel1, "editor");

        jPanel3.setLayout(new java.awt.GridBagLayout());

        lblBusyLoad.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblBusyLoad.setMaximumSize(new java.awt.Dimension(140, 40));
        lblBusyLoad.setMinimumSize(new java.awt.Dimension(140, 40));
        lblBusyLoad.setPreferredSize(new java.awt.Dimension(140, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel3.add(lblBusyLoad, gridBagConstraints);

        add(jPanel3, "load");

        jPanel2.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labEmpty,
            org.openide.util.NbBundle.getMessage(PhotoEditor.class, "PhotoEditor.labEmpty.text", new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(labEmpty, gridBagConstraints);

        add(jPanel2, "empty");

        bindingGroup.bind();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtReFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtReFocusLost
        try {
            final double coord = Double.parseDouble(txtRe.getText().replace(',', '.'));
            final CidsBean bean = (CidsBean)cidsBean.getProperty("geom");
            final Geometry geom = feature.getGeometry();
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CismapBroker.getInstance().getDefaultCrsAlias());

            if ((bean != null) && (geom != null)) {
                final double y = geom.getCoordinate().y;
                final Geometry point = factory.createPoint(new Coordinate(coord, y));

                try {
                    bean.setProperty("geo_field", point);
                    feature.setGeometry(point);
                    relocateFeature();
                } catch (Exception ex) {
                    LOG.warn("Cannot create coordinate");
                }
            } else {
                final Double other = getDouble(txtHo.getText());

                final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                final Geometry point = factory.createPoint(new Coordinate(coord, other));

                geomBean.setProperty("geo_field", point);
                cidsBean.setProperty("geom", geomBean);
                feature.setGeometry(point);
                relocateFeature();
            }
        } catch (NumberFormatException e) {
            LOG.warn("NumberFormatException", e);
        } catch (Exception e) {
            LOG.error("Error in rh/ho converter", e);
        }
    } //GEN-LAST:event_txtReFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtHoFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtHoFocusLost
        try {
            final double coord = Double.parseDouble(txtHo.getText().replace(',', '.'));
            final CidsBean bean = (CidsBean)cidsBean.getProperty("geom");
            final Geometry geom = feature.getGeometry();
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CismapBroker.getInstance().getDefaultCrsAlias());

            if ((bean != null) && (geom != null)) {
                final double x = geom.getCoordinate().x;
                final Geometry point = factory.createPoint(new Coordinate(x, coord));

                try {
                    bean.setProperty("geo_field", point);
                    feature.setGeometry(point);
                    relocateFeature();
                } catch (Exception ex) {
                    LOG.warn("Cannot create coordinate");
                }
            } else {
                final Double other = getDouble(txtRe.getText());

                final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                final Geometry point = factory.createPoint(new Coordinate(other, coord));

                geomBean.setProperty("geo_field", point);
                cidsBean.setProperty("geom", geomBean);
                feature.setGeometry(point);
                relocateFeature();
            }
        } catch (NumberFormatException e) {
            LOG.warn("NumberFormatException", e);
        } catch (Exception e) {
            LOG.error("Error in rh/ho converter", e);
        }
    } //GEN-LAST:event_txtHoFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void taTitleFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_taTitleFocusLost
    }                                                                    //GEN-LAST:event_taTitleFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void taBeschreibungFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_taBeschreibungFocusLost
    }                                                                           //GEN-LAST:event_taBeschreibungFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void taBemerkungFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_taBemerkungFocusLost
    }                                                                        //GEN-LAST:event_taBemerkungFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtAufnFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtAufnFocusLost
    }                                                                    //GEN-LAST:event_txtAufnFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtAufn1FocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtAufn1FocusLost
    }                                                                     //GEN-LAST:event_txtAufn1FocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtWinklelFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtWinklelFocusLost
//        try {
//            if ((txtWinklel.getText() != null) && !txtWinklel.getText().equals("")) {
//                feature.setProperty("winkel", Double.parseDouble(txtWinklel.getText().replace(',', '.')));
//            } else {
//                feature.setProperty("winkel", txtWinklel.getText());
//            }
//            showNewAngle();
//        } catch (NumberFormatException e) {
//        }
        showNewAngle();
    } //GEN-LAST:event_txtWinklelFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void dateChooserFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_dateChooserFocusLost
    }                                                                        //GEN-LAST:event_dateChooserFocusLost

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtWinklelFocusGained(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtWinklelFocusGained
        // TODO add your handling code here:
    } //GEN-LAST:event_txtWinklelFocusGained

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtAufnFocusGained(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtAufnFocusGained
        // TODO add your handling code here:
    } //GEN-LAST:event_txtAufnFocusGained

    /**
     * Set the cids layer feature. This method should be used instead of the setCidsBean method
     *
     * @param  feature  The feature that should be shown in the editor
     */
    public void setCidsLayerFeature(final CidsLayerFeature feature) {
        if (this.feature != null) {
            this.feature.removePropertyChangeListener(this);
        }
        this.feature = feature;
        if (this.feature != null) {
            this.feature.addPropertyChangeListener(this);
        }

        if (feature == null) {
            setCidsBean(null);
        } else {
            setCidsBean(feature.getBean());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     * @param  baCd     DOCUMENT ME!
     * @param  baSt     DOCUMENT ME!
     * @param  laCd     DOCUMENT ME!
     * @param  laSt     DOCUMENT ME!
     */
    protected void refreshLaStation(final FeatureServiceFeature feature,
            final String baCd,
            final Double baSt,
            final String laCd,
            final String laSt) {
        final Thread refreshLa = new Thread("refreshLa") {

                @Override
                public void run() {
                    try {
                        if ((baCd == null) || (baSt == null)) {
                            feature.setProperty(laCd, null);
                            feature.setProperty(laSt, null);
                            labLaCdVal.setText("");
                            labStatLaVal.setText("");
                        } else {
                            final CidsServerSearch search = new CalculateFgLa(baCd, baSt);

                            final User user = SessionManager.getSession().getUser();
                            final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager
                                        .getProxy().customServerSearch(user, search);

                            if ((attributes != null) && (attributes.size() > 0) && (attributes.get(0) != null)
                                        && (attributes.get(0).size() > 1)) {
                                feature.setProperty(laCd, attributes.get(0).get(0));
                                feature.setProperty(laSt, attributes.get(0).get(1));
                                labLaCdVal.setText(String.valueOf(attributes.get(0).get(0)));
                                labStatLaVal.setText(String.valueOf(attributes.get(0).get(1)));
                            } else {
                                feature.setProperty(laCd, null);
                                feature.setProperty(laSt, null);
                                labLaCdVal.setText("");
                                labStatLaVal.setText("");
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Cannot retrieve la_cd, la_st", e);
                    }
                }
            };

        refreshLa.start();
    }

    /**
     * True, if the current user has write permissions on the selected feature.
     *
     * @return  true, if the user has write permissions
     */
    public boolean hasWriteAccess() {
        if (feature != null) {
            return feature.hasWritePermissions();
        }

        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   text  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  NumberFormatException  DOCUMENT ME!
     */
    private Double getDouble(final String text) throws NumberFormatException {
        return Double.parseDouble(text);
    }

    /**
     * Determine the currently shown feature.
     *
     * @return  The feature that is currently shown in the editor
     */
    public CidsLayerFeature getCidsLayerFeature() {
//        this.feature.syncWithBean();
        return this.feature;
    }

    @Override
    public CidsBean getCidsBean() {
        return this.cidsBean;
    }

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        bindingGroup.unbind();
        panRouteCombo.removeAll();
//        panStatEdit.removeAll();
        wrapper.setFeature(null);
        this.cidsBean = cidsBean;

        if (cidsBean != null) {
            wrapper.setFeature(feature);
            bindingGroup.bind();

            final Component routeComp = routeCellEditor.getFeatureComponent(feature, feature.getProperty("ba_cd"));
            panRouteCombo.add(routeComp);
            routeComp.addFocusListener(new FocusListener() {

                    @Override
                    public void focusGained(final FocusEvent e) {
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        feature.setProperty("ba_cd", routeCellEditor.getCellEditorValue());
                    }
                });

            refreshGui();

            final CidsBean baSt = (CidsBean)cidsBean.getProperty("ba_st");

//            final TableStationEditor stationComp = feature.getStationEditor("ba_st");
//            if (stationComp != null) {
//                stationComp.setSize(100, 20);
//                stationComp.addFocusListener(new FocusListener() {
//
//                        @Override
//                        public void focusGained(final FocusEvent e) {
//                        }
//
//                        @Override
//                        public void focusLost(final FocusEvent e) {
//                            feature.setProperty("ba_st", stationComp.getValue());
//                        }
//                    });
//                stationComp.setStationProperty("ba_st");
//                stationComp.setParentFeature(feature);
//                panStatEdit.add(stationComp);
//            }

            final Date uplDate = (Date)cidsBean.getProperty("upl_datum");
            final String uplTime = (String)cidsBean.getProperty("upl_zeit");

            if ((uplTime != null) && (uplDate != null)) {
                labUplDatumVal.setText(dateFormatter.format(uplDate) + " " + uplTime);
            }

            txtRe.setText(getPointValue(true));
            txtHo.setText(getPointValue(false));

            loadFoto();
            showEditor(true, false);
            setReadOnly(!hasWriteAccess());
        } else {
            showEditor(false, false);
//            Photo.selectedFeature = null;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void relocateFeature() {
        try {
            if (feature.getProperty("ba_cd") == null) {
                Photo.tryStationCreation(feature, null);
            }
        } catch (Exception e) {
            LOG.error("Cannot determine new station", e);
        }

        Photo.refreshFeatureDesignOnMap(feature);
    }

    /**
     * DOCUMENT ME!
     */
    public void refreshGui() {
        labFotoVal.setText(getPropString("foto"));
        labUplNameVal.setText(getPropString("upl_name"));
        refreshLaStation(
            feature,
            (String)feature.getProperty("ba_cd"),
            (Double)feature.getProperty("ba_st"),
            "la_cd",
            "la_st");
        labIdVal.setText(getPropString("foto_nr"));
//        txtAufn.setText(object2String(feature.getProperty("aufn_name")));
//        txtAufn1.setText(object2String(feature.getProperty("aufn_zeit")));
//        txtWinklel.setText(object2String(feature.getProperty("winkel")));
//        taBemerkung.setText(object2String(feature.getProperty("bemerkung")));
//        taTitle.setText(object2String(feature.getProperty("titel")));
//        taBeschreibung.setText(object2String(feature.getProperty("beschreib")));
    }

    /**
     * DOCUMENT ME!
     */
    private void showNewAngle() {
        final List<PFeature> pfeatureList = feature.getLayerProperties()
                    .getFeatureService()
                    .getPNode()
                    .getChildrenReference();

        for (final PFeature pf : pfeatureList) {
            final Feature f = pf.getFeature();

            if (f instanceof FeatureServiceFeature) {
                if (((FeatureServiceFeature)f).getId() == feature.getId()) {
                    ((FeatureServiceFeature)f).setProperty("winkel", feature.getProperty("winkel"));
                    pf.visualize();
                    pf.refreshDesign();
                    final PFeature mapFeature = pf.getViewer().getPFeatureHM().get(feature);

                    if (mapFeature != null) {
                        ((FeatureServiceFeature)mapFeature.getFeature()).setProperty(
                            "winkel",
                            feature.getProperty("winkel"));
                        mapFeature.visualize();
                        mapFeature.refreshDesign();
                    }
                    break;
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String object2String(final Object o) {
        if (o == null) {
            return "";
        } else if (o instanceof Double) {
            return String.valueOf(o).replace('.', ',');
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   rw  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getPointValue(final boolean rw) {
        final Object geo = feature.getGeometry();

        if (geo instanceof Point) {
            final Point point = (Point)geo;

            if (rw) {
                return ConversionUtils.numberToString(point.getX());
            } else {
                return ConversionUtils.numberToString(point.getY());
            }
        } else {
            return "";
        }
    }

    /**
     * Set the station bean of the selected feature.
     *
     * @param  baSt  the new station bean
     */
    public void setStatBean(final CidsBean baSt) {
        System.out.println("test");
//        ((TableStationEditor)panStatEdit).setCidsBean(baSt);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  readOnly  DOCUMENT ME!
     */
    private void setReadOnly(final boolean readOnly) {
        if (readOnly) {
            RendererTools.makeReadOnly(txtAufn);
            RendererTools.makeReadOnly(txtHo);
            RendererTools.makeReadOnly(txtRe);
            RendererTools.makeReadOnly(txtWinklel);
            dateChooser.setEnabled(false);
            txtAufn1.setEnabled(false);
            RendererTools.makeReadOnly(taBemerkung);
            RendererTools.makeReadOnly(taTitle);
            RendererTools.makeReadOnly(taBeschreibung);
            if (panReLi.getComponentCount() > 0) {
                RendererTools.makeReadOnly((DefaultCidsLayerBindableReferenceCombo)panReLi.getComponent(0));
            }
            if (panFreigabe.getComponentCount() > 0) {
                RendererTools.makeReadOnly((DefaultCidsLayerBindableReferenceCombo)panFreigabe.getComponent(0));
            }
            if (panStatus.getComponentCount() > 0) {
                RendererTools.makeReadOnly((DefaultCidsLayerBindableReferenceCombo)panStatus.getComponent(0));
            }
        } else {
            RendererTools.makeWritable(txtAufn);
            RendererTools.makeWritable(txtHo);
            RendererTools.makeWritable(txtRe);
            RendererTools.makeWritable(txtWinklel);
            dateChooser.setEnabled(true);
            txtAufn1.setEnabled(true);
            RendererTools.makeWritable(taBemerkung);
            RendererTools.makeWritable(taTitle);
            RendererTools.makeWritable(taBeschreibung);
            if (panReLi.getComponentCount() > 0) {
                RendererTools.makeWritable((DefaultCidsLayerBindableReferenceCombo)panReLi.getComponent(0));
            }
            if (panFreigabe.getComponentCount() > 0) {
                RendererTools.makeWritable((DefaultCidsLayerBindableReferenceCombo)panFreigabe.getComponent(0));
            }
            if (panStatus.getComponentCount() > 0) {
                RendererTools.makeWritable((DefaultCidsLayerBindableReferenceCombo)panStatus.getComponent(0));
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   propName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getPropString(final String propName) {
        final Object value = cidsBean.getProperty(propName);

        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void loadFoto() {
        final String path = (String)cidsBean.getProperty("dateipfad");
        final String filename = (String)cidsBean.getProperty("foto");
        boolean cacheHit = false;

        if ((path != null) && (filename != null)) {
            final String file = path + filename;
            final SoftReference<BufferedImage> cachedImageRef = IMAGE_CACHE.get(file);
            if (cachedImageRef != null) {
                final BufferedImage cachedImage = cachedImageRef.get();
                if (cachedImage != null) {
                    cacheHit = true;
                    image = cachedImage;
                    showWait(true);
                    timer.restart();
                }
            }
            if (!cacheHit) {
                CismetThreadPool.execute(new LoadImageWorker(path, filename));
            }
        }
    }

    @Override
    public void dispose() {
        setCidsLayerFeature(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  error  DOCUMENT ME!
     */
    private void indicateError(final String error) {
        labImage.setToolTipText(error);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   bi         DOCUMENT ME!
     * @param   component  DOCUMENT ME!
     * @param   insetX     DOCUMENT ME!
     * @param   insetY     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Image adjustScale(final BufferedImage bi,
            final JComponent component,
            final int insetX,
            final int insetY) {
        final double scalex = (double)component.getWidth() / bi.getWidth();
        final double scaley = (double)component.getHeight() / bi.getHeight();
        final double scale = Math.min(scalex, scaley);
        if (scale <= 1d) {
            return bi.getScaledInstance((int)(bi.getWidth() * scale) - insetX,
                    (int)(bi.getHeight() * scale)
                            - insetY,
                    Image.SCALE_FAST);
        } else {
            return bi;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  wait  DOCUMENT ME!
     */
    private void showWait(final boolean wait) {
        if (wait) {
            if (!lblBusy.isBusy()) {
                final CardLayout cardLayout = (CardLayout)panImage.getLayout();
                cardLayout.show(panImage, "busy");
                labImage.setIcon(null);
                lblBusy.setBusy(true);
            }
        } else {
            final CardLayout cardLayout = (CardLayout)panImage.getLayout();
            cardLayout.show(panImage, "image");
            lblBusy.setBusy(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  editor      wait DOCUMENT ME!
     * @param  editorWait  DOCUMENT ME!
     */
    public void showEditor(final boolean editor, final boolean editorWait) {
        if (editor) {
            if (editorWait) {
                final CardLayout cardLayout = (CardLayout)getLayout();
                cardLayout.show(this, "load");
                lblBusyLoad.setBusy(true);
            } else {
                final CardLayout cardLayout = (CardLayout)getLayout();
                cardLayout.show(this, "editor");
                lblBusyLoad.setBusy(false);
            }
        } else {
            final CardLayout cardLayout = (CardLayout)getLayout();
            cardLayout.show(this, "empty");
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("wert")) {
            feature.setProperty("ba_st", evt.getNewValue());
        } else if (evt.getPropertyName().equals("geom")) {
            txtRe.setText(getPointValue(true));
            txtHo.setText(getPointValue(false));
            relocateFeature();
        } else if (evt.getPropertyName().equals("ba_cd")) {
            panRouteCombo.removeAll();
            final Component routeComp = routeCellEditor.getFeatureComponent(feature, feature.getProperty("ba_cd"));
            panRouteCombo.add(routeComp);
            routeComp.addFocusListener(new FocusListener() {

                    @Override
                    public void focusGained(final FocusEvent e) {
                    }

                    @Override
                    public void focusLost(final FocusEvent e) {
                        feature.setProperty("ba_cd", routeCellEditor.getCellEditorValue());
                    }
                });

//            panStatEdit.removeAll();
//            final TableStationEditor stationComp = feature.getStationEditor("ba_st");
//            if (stationComp != null) {
//                stationComp.setSize(100, 20);
//                stationComp.addFocusListener(new FocusListener() {
//
//                        @Override
//                        public void focusGained(final FocusEvent e) {
//                        }
//
//                        @Override
//                        public void focusLost(final FocusEvent e) {
//                            feature.setProperty("ba_st", stationComp.getValue());
//                        }
//                    });
//                stationComp.setStationProperty("ba_st");
//                stationComp.setParentFeature(feature);
//                panStatEdit.add(stationComp);
//            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class LoadImageWorker extends SwingWorker<BufferedImage, Void> {

        //~ Instance fields ----------------------------------------------------

        private final String path;
        private final String file;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LoadSelectedImageWorker object.
         *
         * @param  path  toLoad DOCUMENT ME!
         * @param  file  DOCUMENT ME!
         */
        public LoadImageWorker(final String path, final String file) {
            this.path = path;
            this.file = file;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected BufferedImage doInBackground() throws Exception {
            if ((file != null) && (file.length() > 0)) {
                return WebDavHelper.downloadImageFromWebDAV(
                        file,
                        webDavDirectory
                                + path,
                        webDavClient,
                        null);
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                image = get();
                if (image != null) {
                    IMAGE_CACHE.put(path + file, new SoftReference<BufferedImage>(image));
                    timer.restart();
                } else {
                    indicateError("Bild konnte nicht geladen werden: Unbekanntes Bildformat");
                }
            } catch (InterruptedException ex) {
                image = null;
                LOG.warn(ex, ex);
            } catch (ExecutionException ex) {
                image = null;
                LOG.error(ex, ex);
                String causeMessage = "";
                final Throwable cause = ex.getCause();
                if (cause != null) {
                    causeMessage = cause.getMessage();
                }
                indicateError(causeMessage);
            } finally {
                if (image == null) {
                    showWait(false);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    final class ImageResizeWorker extends SwingWorker<ImageIcon, Void> {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ImageResizeWorker object.
         */
        public ImageResizeWorker() {
        }

        //~ Methods ------------------------------------------------------------

        @Override
        protected ImageIcon doInBackground() throws Exception {
            if (image != null) {
                final ImageIcon result = new ImageIcon(adjustScale(image, panImage, 20, 20));
                return result;
            } else {
                return null;
            }
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    final ImageIcon result = get();
                    labImage.setIcon(result);
                    labImage.setText("");
                    labImage.setToolTipText(null);
                } catch (InterruptedException ex) {
                    LOG.warn(ex, ex);
                } catch (ExecutionException ex) {
                    LOG.error(ex, ex);
                    labImage.setText("Fehler beim Skalieren!");
                } finally {
                    showWait(false);
                    if (currentResizeWorker == this) {
                        currentResizeWorker = null;
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class TimeValidator extends Validator<String> {

        //~ Methods ------------------------------------------------------------

        @Override
        public Validator.Result validate(final String t) {
            if ((t == null) || t.isEmpty()) {
                return null;
            }

            try {
                timeFormatter.parse(t);
            } catch (ParseException ex) {
                return new Validator.Result(null, "Dies ist kein gültiges Datum");
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class BeanPropertyChangeListener implements PropertyChangeListener {

        //~ Methods ------------------------------------------------------------

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
//            if (evt.getPropertyName().equals("aufn_datum")) {
//                feature.setProperty("aufn_datum", dateChooser.getDate());
//            } else if (evt.getPropertyName().equals("winkel")) {
//                txtWinklel.setText(
//                    ((evt.getNewValue() != null) ? String.valueOf(evt.getNewValue()) : ""));
//            } else
            if (evt.getPropertyName().equals("ba_st") && (evt.getNewValue() instanceof CidsBean)) {
                final TableStationEditor stationComp = feature.getStationEditor("ba_st");

                if (stationComp != null) {
                    stationComp.setSize(100, 20);
                    stationComp.addFocusListener(new FocusListener() {

                            @Override
                            public void focusGained(final FocusEvent e) {
                            }

                            @Override
                            public void focusLost(final FocusEvent e) {
                                feature.setProperty("ba_st", stationComp.getValue());
                            }
                        });
                    panStatEdit.add(stationComp);
                    refreshLaStation(
                        feature,
                        (String)feature.getProperty("ba_cd"),
                        (Double)feature.getProperty("ba_st"),
                        "la_cd",
                        "la_st");
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class DoubleConverter extends Converter<Double, String> {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   value  an instance of the class de.cismet.cids.dynamics.Geom is expected
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String convertForward(final Double value) {
            if (value != null) {
                return ConversionUtils.numberToString(value);
            } else {
                return "";
            }
        }

        @Override
        public Double convertReverse(final String value) {
            try {
                if ((value == null) || value.equals("")) {
                    return null;
                }
                final double coord = Double.parseDouble(value.replace(',', '.'));

                return coord;
            } catch (NumberFormatException e) {
                return null;
            } catch (Exception e) {
                LOG.error("Error in Double converter", e);
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class CoordinateConverter extends Converter<Geometry, String> {

        //~ Instance fields ----------------------------------------------------

        private boolean rw;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CoordinateConverter object.
         *
         * @param  rw  DOCUMENT ME!
         */
        public CoordinateConverter(final boolean rw) {
            this.rw = rw;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   value  an instance of the class de.cismet.cids.dynamics.Geom is expected
         *
         * @return  DOCUMENT ME!
         */
        @Override
        public String convertForward(final Geometry value) {
//            final Object geo = value.getProperty("geo_field");
            final Object geo = value;

            if (geo instanceof Point) {
                final Point point = (Point)geo;

                if (rw) {
                    return ConversionUtils.numberToString(point.getX());
                } else {
                    return ConversionUtils.numberToString(point.getY());
                }
            } else {
                return "";
            }
        }

        @Override
        public Geometry convertReverse(final String value) {
            try {
                final double coord = Double.parseDouble(value.replace(',', '.'));
                final CidsBean bean = (CidsBean)cidsBean.getProperty("geom");
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        CismapBroker.getInstance().getDefaultCrsAlias());

                if (bean != null) {
                    final Geometry geom = (Geometry)bean.getProperty("geo_field");

                    if (rw) {
                        final double y = geom.getCoordinate().y;
                        final Geometry point = factory.createPoint(new Coordinate(coord, y));

                        try {
                            bean.setProperty("geo_field", point);
                        } catch (Exception ex) {
                            LOG.warn("Cannot create coordinate");
                        }

                        return point;
                    } else {
                        final double x = geom.getCoordinate().x;
                        final Geometry point = factory.createPoint(new Coordinate(x, coord));

                        try {
                            bean.setProperty("geo_field", point);
                        } catch (Exception ex) {
                            LOG.warn("Cannot create coordinate");
                        }

                        return point;
                    }
                } else {
                    if (rw) {
                        final Double other = getDouble(txtHo.getText());

                        final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                        final Geometry point = factory.createPoint(new Coordinate(coord, other));

                        geomBean.setProperty("geo_field", point);

                        return point;
                    } else {
                        final Double other = getDouble(txtRe.getText());

                        final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");
                        final Geometry point = factory.createPoint(new Coordinate(other, coord));

                        geomBean.setProperty("geo_field", point);

                        return point;
                    }
                }
            } catch (NumberFormatException e) {
                return null;
            } catch (Exception e) {
                LOG.error("Error in rh/ho converter", e);
                return null;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   text  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  NumberFormatException  DOCUMENT ME!
         */
        private Double getDouble(final String text) throws NumberFormatException {
            return Double.parseDouble(text);
        }
    }
}
