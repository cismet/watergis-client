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
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.jdesktop.beansbinding.Converter;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;

import java.lang.ref.SoftReference;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cids.editors.DefaultCustomObjectEditor;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.TableStationEditor;

import de.cismet.commons.security.WebDavClient;
import de.cismet.commons.security.WebDavHelper;

import de.cismet.tools.CismetThreadPool;

import de.cismet.watergis.utils.CidsBeanUtils;
import de.cismet.watergis.utils.RendererTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PhotoEditor extends javax.swing.JPanel implements DisposableCidsBeanStore {

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

    //~ Instance fields --------------------------------------------------------

    private CidsBean cidsBean;
    private CidsLayerFeature feature;
    private String webDavDirectory;
    private WebDavClient webDavClient;
    private BufferedImage image;
    private Timer timer;
    private ImageResizeWorker currentResizeWorker;
    private Dimension lastDims;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cismet.cids.editors.DefaultBindableReferenceCombo cbFreigabe;
    private de.cismet.cids.editors.DefaultBindableReferenceCombo cbLRl;
    private de.cismet.cids.editors.DefaultBindableReferenceCombo cbLst;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
    private javax.swing.JPanel panAufn;
    private javax.swing.JPanel panBesch;
    private javax.swing.JPanel panGewaesserBezug;
    private javax.swing.JPanel panImage;
    private javax.swing.JPanel panStatEdit;
    private javax.swing.JPanel panUpload;
    private javax.swing.JPanel panVerortung;
    private javax.swing.JTextArea taBemerkung;
    private javax.swing.JTextArea taBemerkung1;
    private javax.swing.JTextArea taBeschreibung;
    private de.cismet.cids.editors.DefaultBindableTimestampChooser tcAufnZeit;
    private javax.swing.JTextField txtAufn;
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jPanel2 = new javax.swing.JPanel();
        labEmpty = new javax.swing.JLabel();
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
        cbLst = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
        cbLRl = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
        labStatLa = new javax.swing.JLabel();
        labStatBa = new javax.swing.JLabel();
        labStatLaVal = new javax.swing.JLabel();
        panStatEdit = new TableStationEditor("dlm25w.fg_ba");
        jLabel3 = new javax.swing.JLabel();
        panAufn = new javax.swing.JPanel();
        labAufnName = new javax.swing.JLabel();
        labAufnDatum = new javax.swing.JLabel();
        txtAufn = new javax.swing.JTextField();
        labFreigabe = new javax.swing.JLabel();
        cbFreigabe = new de.cismet.cids.editors.DefaultBindableReferenceCombo();
        tcAufnZeit = new de.cismet.cids.editors.DefaultBindableTimestampChooser();
        jLabel4 = new javax.swing.JLabel();
        panBesch = new javax.swing.JPanel();
        labTitle = new javax.swing.JLabel();
        labBemerkung = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taBemerkung = new javax.swing.JTextArea();
        labBeschreibung = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taBeschreibung = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        taBemerkung1 = new javax.swing.JTextArea();
        panImage = new javax.swing.JPanel();
        labImage = new javax.swing.JLabel();
        lblBusy = new org.jdesktop.swingx.JXBusyLabel(new Dimension(75, 75));

        setLayout(new java.awt.CardLayout());

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

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.foto_nr}"),
                labIdVal,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

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

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geom}"),
                txtRe,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setConverter(new CoordinateConverter(true));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        panVerortung.add(txtRe, gridBagConstraints);

        txtHo.setMinimumSize(new java.awt.Dimension(250, 25));
        txtHo.setPreferredSize(new java.awt.Dimension(100, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.geom}"),
                txtHo,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        binding.setConverter(new CoordinateConverter(false));
        bindingGroup.addBinding(binding);

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
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.winkel}"),
                txtWinklel,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

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

        cbLst.setMinimumSize(new java.awt.Dimension(120, 25));
        cbLst.setPreferredSize(new java.awt.Dimension(120, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.l_st}"),
                cbLst,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(cbLst, gridBagConstraints);

        cbLRl.setMinimumSize(new java.awt.Dimension(250, 25));
        cbLRl.setPreferredSize(new java.awt.Dimension(210, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.l_rl}"),
                cbLRl,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panGewaesserBezug.add(cbLRl, gridBagConstraints);

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
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.aufn_name}"),
                txtAufn,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

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

        cbFreigabe.setMinimumSize(new java.awt.Dimension(120, 25));
        cbFreigabe.setPreferredSize(new java.awt.Dimension(120, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.freigabe}"),
                cbFreigabe,
                org.jdesktop.beansbinding.BeanProperty.create("selectedItem"));
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(cbFreigabe, gridBagConstraints);

        tcAufnZeit.setPreferredSize(new java.awt.Dimension(210, 25));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.aufn_zeit}"),
                tcAufnZeit,
                org.jdesktop.beansbinding.BeanProperty.create("timestamp"));
        binding.setConverter(tcAufnZeit.getConverter());
        bindingGroup.addBinding(binding);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        panAufn.add(tcAufnZeit, gridBagConstraints);

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
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.bemerkung}"),
                taBemerkung,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

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
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.beschreib}"),
                taBeschreibung,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

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

        taBemerkung1.setColumns(20);
        taBemerkung1.setRows(4);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(
                org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE,
                this,
                org.jdesktop.beansbinding.ELProperty.create("${cidsBean.titel}"),
                taBemerkung1,
                org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jScrollPane3.setViewportView(taBemerkung1);

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

        bindingGroup.bind();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public void setCidsLayerFeature(final CidsLayerFeature feature) {
        this.feature = feature;

        if (feature == null) {
            setCidsBean(null);
        } else {
            setCidsBean(feature.getBean());
        }
    }
    
    public boolean isUploader() {
        if (feature != null) {
            String currentUser = SessionManager.getSession().getUser().getName();
            
            if (feature.getProperty("upl_name") != null && feature.getProperty("upl_name").equals(currentUser)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsLayerFeature getCidsLayerFeature() {
        this.feature.syncWithBean();
        return this.feature;
    }

    @Override
    public CidsBean getCidsBean() {
        return this.cidsBean;
    }

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        bindingGroup.unbind();
        ((TableStationEditor)panStatEdit).setCidsBean(null);
        this.cidsBean = cidsBean;

        if (cidsBean != null) {
            DefaultCustomObjectEditor.setMetaClassInformationToMetaClassStoreComponentsInBindingGroup(
                bindingGroup,
                this.cidsBean);
            bindingGroup.bind();
            labFotoVal.setText(getPropString("foto"));
            labUplNameVal.setText(getPropString("upl_name"));
            final CidsBean baSt = (CidsBean)cidsBean.getProperty("ba_st");

            if (baSt != null) {
                ((TableStationEditor)panStatEdit).setCidsBean(baSt);
            }
            final Date date = (Date)cidsBean.getProperty("upl_zeit");

            if (date != null) {
                final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

                labUplDatumVal.setText(format.format(date));
            }
            loadFoto();
            showEditor(true);
            setReadOnly(!isUploader());
        } else {
            showEditor(false);
        }
    }
    
    private void setReadOnly(boolean readOnly) {
        if (readOnly) {
            RendererTools.makeReadOnly(txtAufn);
            RendererTools.makeReadOnly(txtHo);
            RendererTools.makeReadOnly(txtRe);
            RendererTools.makeReadOnly(txtWinklel);
            tcAufnZeit.setEnabled(false);
            RendererTools.makeReadOnly(taBemerkung);
            RendererTools.makeReadOnly(taBemerkung1);
            RendererTools.makeReadOnly(taBeschreibung);
            RendererTools.makeReadOnly(cbFreigabe);
            RendererTools.makeReadOnly(cbLRl);
            RendererTools.makeReadOnly(cbLst);
        } else {
            RendererTools.makeWritable(txtAufn);
            RendererTools.makeWritable(txtHo);
            RendererTools.makeWritable(txtRe);
            RendererTools.makeWritable(txtWinklel);
            tcAufnZeit.setEnabled(true);
            RendererTools.makeWritable(taBemerkung);
            RendererTools.makeWritable(taBemerkung1);
            RendererTools.makeWritable(taBeschreibung);
            RendererTools.makeWritable(cbFreigabe);
            RendererTools.makeWritable(cbLRl);
            RendererTools.makeWritable(cbLst);
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
                    Image.SCALE_SMOOTH);
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
     * @param  editor  wait DOCUMENT ME!
     */
    private void showEditor(final boolean editor) {
        if (editor) {
            final CardLayout cardLayout = (CardLayout)getLayout();
            cardLayout.show(this, "editor");
        } else {
            final CardLayout cardLayout = (CardLayout)getLayout();
            cardLayout.show(this, "empty");
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
                        PhotoEditor.this);
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
    class CoordinateConverter extends Converter<CidsBean, String> {

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
        public String convertForward(final CidsBean value) {
            final Object geo = value.getProperty("geo_field");

            if (geo instanceof Point) {
                final Point point = (Point)geo;
                final DecimalFormat format = new DecimalFormat("0.00");
                final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setDecimalSeparator(',');
                symbols.setGroupingSeparator('.');
                format.setDecimalFormatSymbols(symbols);

                if (rw) {
                    return format.format(point.getX());
                } else {
                    return format.format(point.getY());
                }
            } else {
                return "";
            }
        }

        @Override
        public CidsBean convertReverse(final String value) {
            try {
                final double coord = Double.parseDouble(value);
                final CidsBean bean = (CidsBean)cidsBean.getProperty("geom");
                final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        CismapBroker.getInstance().getDefaultCrsAlias());

                if (bean != null) {
                    final Geometry geom = (Geometry)bean.getProperty("geo_field");

                    if (rw) {
                        final double y = geom.getCoordinate().y;

                        try {
                            bean.setProperty("geo_field", factory.createPoint(new Coordinate(coord, y)));
                        } catch (Exception ex) {
                            LOG.warn("Cannot create coordinate");
                        }

                        return bean;
                    } else {
                        final double x = geom.getCoordinate().x;

                        try {
                            bean.setProperty("geo_field", factory.createPoint(new Coordinate(x, coord)));
                        } catch (Exception ex) {
                            LOG.warn("Cannot create coordinate");
                        }

                        return bean;
                    }
                } else {
                    if (rw) {
                        final Double other = getDouble(txtHo.getText());

                        final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");

                        geomBean.setProperty("geo_field", factory.createPoint(new Coordinate(coord, other)));

                        return geomBean;
                    } else {
                        final Double other = getDouble(txtRe.getText());

                        final CidsBean geomBean = CidsBeanUtils.createNewCidsBeanFromTableName("geom");

                        geomBean.setProperty("geo_field", factory.createPoint(new Coordinate(other, coord)));

                        return geomBean;
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
