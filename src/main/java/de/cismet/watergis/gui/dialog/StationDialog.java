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
import Sirius.navigator.exception.ConnectionException;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import org.deegree.datatypes.Types;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.JDBCFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.custom.attributerule.MessageDialog;

import de.cismet.connectioncontext.ConnectionContext;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StationDialog.class);
    private static final String SLD =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><sld:StyledLayerDescriptor xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xslutil=\"de.latlon.deejump.plugin.style.XSLUtility\" xmlns:java=\"java\" xmlns:deegreewfs=\"http://www.deegree.org/wfs\" xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns=\"http://www.opengis.net/sld\" version=\"1.0.0\" xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\" xmlns:oj=\"http://cismet.de\" oj:dummy=\"\">\n"
                + "  <sld:NamedLayer>\n"
                + "    <sld:Name>$LAYER_NAME$</sld:Name>\n"
                + "    <sld:UserStyle>\n"
                + "      <sld:Name>$LAYER_NAME$</sld:Name>\n"
                + "      <sld:Title>$LAYER_NAME$</sld:Title>\n"
                + "      <sld:IsDefault>1</sld:IsDefault>\n"
                + "      <sld:FeatureTypeStyle>\n"
                + "        <sld:Name>$LAYER_NAME$</sld:Name>\n"
                + "        <sld:Rule>\n"
                + "          <sld:Name>basicPointStyle</sld:Name>\n"
                + "          <sld:PointSymbolizer>\n"
                + "            <sld:Geometry>\n"
                + "              <ogc:PropertyName>geom</ogc:PropertyName>\n"
                + "            </sld:Geometry>\n"
                + "            <sld:Graphic>\n"
                + "              <sld:Mark>\n"
                + "                <sld:Fill>\n"
                + "                  <sld:CssParameter name=\"fill\">#ff0033</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"fill-opacity\">1.0</sld:CssParameter>\n"
                + "                </sld:Fill>\n"
                + "                <sld:Stroke>\n"
                + "                  <sld:CssParameter name=\"stroke\">#b20023</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"stroke-opacity\">1.0</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"stroke-width\">1</sld:CssParameter>\n"
                + "                </sld:Stroke>\n"
                + "              </sld:Mark>\n"
                + "            </sld:Graphic>\n"
                + "          </sld:PointSymbolizer>\n"
                + "        </sld:Rule>\n"
                + "        <sld:Rule>\n"
                + "          <sld:Name>pointStyle</sld:Name>\n"
                + "          <sld:PointSymbolizer>\n"
                + "            <sld:Geometry>\n"
                + "              <ogc:PropertyName>geom</ogc:PropertyName>\n"
                + "            </sld:Geometry>\n"
                + "            <sld:Graphic>\n"
                + "              <sld:Mark>\n"
                + "                <sld:WellKnownName>square</sld:WellKnownName>\n"
                + "                <sld:Fill>\n"
                + "                  <sld:CssParameter name=\"fill\">#ff0033</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"fill-opacity\">1.0</sld:CssParameter>\n"
                + "                </sld:Fill>\n"
                + "                <sld:Stroke>\n"
                + "                  <sld:CssParameter name=\"stroke\">#b20023</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"stroke-opacity\">1.0</sld:CssParameter>\n"
                + "                  <sld:CssParameter name=\"stroke-width\">1</sld:CssParameter>\n"
                + "                </sld:Stroke>\n"
                + "              </sld:Mark>\n"
                + "              <sld:Size>8</sld:Size>\n"
                + "            </sld:Graphic>\n"
                + "          </sld:PointSymbolizer>\n"
                + "        </sld:Rule>\n"
                + "        <sld:Rule>\n"
                + "          <sld:Name>labelStyle</sld:Name>\n"
                + "          <sld:TextSymbolizer>\n"
                + "            <sld:Geometry>\n"
                + "              <ogc:PropertyName>geom</ogc:PropertyName>\n"
                + "            </sld:Geometry>\n"
                + "            <sld:Label>\n"
                + "              <ogc:PropertyName>stat_c</ogc:PropertyName>\n"
                + "            </sld:Label>\n"
                + "            <sld:Font>\n"
                + "              <sld:CssParameter name=\"font-family\">Dialog</sld:CssParameter>\n"
                + "              <sld:CssParameter name=\"font-style\">normal</sld:CssParameter>\n"
                + "              <sld:CssParameter name=\"font-size\">12.0</sld:CssParameter>\n"
                + "              <sld:CssParameter name=\"font-color\">#ff0000</sld:CssParameter>\n"
                + "            </sld:Font>\n"
                + "            <sld:LabelPlacement>\n"
                + "              <sld:PointPlacement>\n"
                + "                <sld:Displacement>\n"
                + "                  <sld:DisplacementX>0</sld:DisplacementX>\n"
                + "                  <sld:DisplacementY>0</sld:DisplacementY>\n"
                + "                </sld:Displacement>\n"
                + "              </sld:PointPlacement>\n"
                + "            </sld:LabelPlacement>\n"
                + "            <sld:Fill>\n"
                + "              <sld:CssParameter name=\"fill\">#ff0000</sld:CssParameter>\n"
                + "              <sld:CssParameter name=\"fill-opacity\">1</sld:CssParameter>\n"
                + "            </sld:Fill>\n"
                + "            <VendorOption xmlns=\"\" name=\"verticalAlignment\" alignment=\"DEFAULT\"/>\n"
                + "            <VendorOption xmlns=\"\" name=\"horizontalPosition\" alignment=\"CENTER\"/>\n"
                + "          </sld:TextSymbolizer>\n"
                + "        </sld:Rule>\n"
                + "      </sld:FeatureTypeStyle>\n"
                + "    </sld:UserStyle>\n"
                + "  </sld:NamedLayer>\n"
                + "</sld:StyledLayerDescriptor>";

    //~ Instance fields --------------------------------------------------------

    private int selectedThemeFeatureCount = 0;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgBuffer;
    private javax.swing.JButton butCancel;
    private javax.swing.JButton butOk;
    private javax.swing.JComboBox cbDistance;
    private javax.swing.JComboBox cbTheme;
    private javax.swing.JCheckBox ckbSelected;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JLabel labSelected;
    private javax.swing.JLabel labTableAbstand;
    private javax.swing.JLabel labTableAbstand1;
    private javax.swing.JLabel labTableName;
    private javax.swing.JLabel labTheme;
    private javax.swing.JTextField txtAbst;
    private javax.swing.JTextField txtTable;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form DissolveDialog.
     *
     * @param  parent  DOCUMENT ME!
     * @param  modal   DOCUMENT ME!
     */
    public StationDialog(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();

        cbTheme.setModel(new DefaultComboBoxModel(
                FeatureServiceHelper.getServices(null).toArray(
                    new AbstractFeatureService[0])));
        cbTheme.setSelectedItem(null);
        cbTheme.setRenderer(new DefaultListCellRenderer() {

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
        txtTable.setText("Stationen|frei");
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
                                        final AbstractFeatureService service = (AbstractFeatureService)
                                            cbTheme.getSelectedItem();
                                        selectedThemeFeatureCount = refreshSelectedFeatureCount(
                                                false,
                                                ckbSelected,
                                                service,
                                                selectedThemeFeatureCount,
                                                labSelected);
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

        final Thread t = new Thread("loadDistances") {

                @Override
                public void run() {
                    loadDistances();
                }
            };

        t.start();
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
        enabledOrNot();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void loadDistances() {
        final MetaClass metaClass = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_stat_fg");
        final String query = "SELECT %s, %s FROM %s;";
        final String routeQuery = String.format(
                query,
                metaClass.getID(),
                metaClass.getPrimaryKey(),
                metaClass.getTableName());

        try {
            final MetaObject[] mos = SessionManager.getProxy()
                        .getMetaObjectByQuery(
                            routeQuery,
                            0,
                            ConnectionContext.create(ConnectionContext.Category.CATALOGUE, "load distances"));

            if (mos != null) {
                final Integer[] distances = new Integer[mos.length];

                for (int i = 0; i < mos.length; ++i) {
                    distances[i] = (Integer)mos[i].getBean().getProperty("abstand");
                }

                Arrays.sort(distances);

                cbDistance.setModel(new DefaultComboBoxModel(distances));
            }
        } catch (ConnectionException ex) {
            LOG.error("Error while retrieving distances", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgBuffer = new javax.swing.ButtonGroup();
        jDialog1 = new javax.swing.JDialog();
        labTheme = new javax.swing.JLabel();
        cbTheme = new javax.swing.JComboBox();
        labTableName = new javax.swing.JLabel();
        txtTable = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        butOk = new javax.swing.JButton();
        butCancel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        ckbSelected = new javax.swing.JCheckBox();
        labSelected = new javax.swing.JLabel();
        labTableAbstand = new javax.swing.JLabel();
        cbDistance = new javax.swing.JComboBox();
        txtAbst = new javax.swing.JTextField();
        labTableAbstand1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(StationDialog.class, "StationDialog.title", new Object[] {})); // NOI18N
        getContentPane().setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            labTheme,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.labTheme.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(labTheme, gridBagConstraints);

        cbTheme.setMinimumSize(new java.awt.Dimension(200, 27));
        cbTheme.setPreferredSize(new java.awt.Dimension(200, 27));
        cbTheme.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbThemeActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(cbTheme, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableName,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.labTableName.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(labTableName, gridBagConstraints);

        txtTable.setMinimumSize(new java.awt.Dimension(200, 27));
        txtTable.setPreferredSize(new java.awt.Dimension(200, 27));
        txtTable.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtTableActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(txtTable, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            butOk,
            org.openide.util.NbBundle.getMessage(StationDialog.class, "StationDialog.butOk.text", new Object[] {})); // NOI18N
        butOk.setMinimumSize(new java.awt.Dimension(80, 29));
        butOk.setPreferredSize(new java.awt.Dimension(100, 29));
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
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            butCancel,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.butCancel.text",
                new Object[] {})); // NOI18N
        butCancel.setPreferredSize(new java.awt.Dimension(100, 29));
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
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 15, 10);
        jPanel1.add(butCancel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jPanel1, gridBagConstraints);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            ckbSelected,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.ckbSelected.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel4.add(ckbSelected, gridBagConstraints);

        labSelected.setPreferredSize(new java.awt.Dimension(200, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel4.add(labSelected, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
        getContentPane().add(jPanel4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableAbstand,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.labTableAbstand.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(labTableAbstand, gridBagConstraints);

        cbDistance.setMinimumSize(new java.awt.Dimension(100, 27));
        cbDistance.setPreferredSize(new java.awt.Dimension(100, 27));
        cbDistance.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbDistanceActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(cbDistance, gridBagConstraints);

        txtAbst.setToolTipText(org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.txtAbst.toolTipText",
                new Object[] {})); // NOI18N
        txtAbst.setMinimumSize(new java.awt.Dimension(50, 27));
        txtAbst.setPreferredSize(new java.awt.Dimension(50, 27));
        txtAbst.addFocusListener(new java.awt.event.FocusAdapter() {

                @Override
                public void focusLost(final java.awt.event.FocusEvent evt) {
                    txtAbstFocusLost(evt);
                }
            });
        txtAbst.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    txtAbstActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(txtAbst, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            labTableAbstand1,
            org.openide.util.NbBundle.getMessage(
                StationDialog.class,
                "StationDialog.labTableAbstand1.text",
                new Object[] {})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(15, 10, 10, 10);
        getContentPane().add(labTableAbstand1, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butCancelActionPerformed
        setVisible(false);
    }                                                                             //GEN-LAST:event_butCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void butOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_butOkActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        final String tableName = txtTable.getText();

        try {
            if ((txtAbst.getText() != null) && !txtAbst.getText().equals("")) {
                Integer.parseInt(txtAbst.getText());
            }
        } catch (NumberFormatException e) {
            final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(),
                    true,
                    "Bitte nur ganzzahlige Werte verwenden.");
            d.setSize(500, 80);
            StaticSwingTools.showDialog(d);
            txtAbst.setText("");

            return;
        }

        try {
            final List<FeatureServiceFeature> featureList = FeatureServiceHelper.getFeatures(
                    service,
                    ckbSelected.isSelected());

            final List<FeatureServiceFeature> resultedFeatures = new ArrayList<>();
            if ((featureList == null) || (featureList.isEmpty())) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    "Es wurden keine Objekte ausgewählt");
                return;
            } else if (!ckbSelected.isSelected() || FeatureServiceHelper.isAllOrNoneFeaturesSelected(service)) {
                final int answ = JOptionPane.showConfirmDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(
                            StationDialog.class,
                            "StationDialog.butOkActionPerformed().allFeatures.message"),
                        NbBundle.getMessage(
                            StationDialog.class,
                            "StationDialog.butOkActionPerformed().allFeatures.title"),
                        JOptionPane.YES_NO_OPTION);

                if (answ == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                            .getInstance().getWatergisApp(),
                    true,
                    "Erstelle Stationen                                       ",
                    null,
                    100,
                    true) {

                    @Override
                    protected H2FeatureService doInBackground() throws Exception {
                        // retrieve Features
                        final Object distanceObject = cbDistance.getSelectedItem();
                        int distance = 10;
                        int progress = 10;
                        wd.setText(NbBundle.getMessage(
                                StationDialog.class,
                                "BufferDialog.butOkActionPerformed.doInBackground.retrieving"));
                        wd.setMax(100);
                        wd.setProgress(5);
                        if (Thread.interrupted()) {
                            return null;
                        }
                        wd.setProgress(10);
                        if (Thread.interrupted()) {
                            return null;
                        }

                        if (distanceObject instanceof Integer) {
                            distance = (Integer)distanceObject;
                        }

                        try {
                            distance = Integer.parseInt(txtAbst.getText());
                        } catch (NumberFormatException e) {
                            // can never happen
                        }

                        // initialise variables for the geo operation
                        final LayerProperties serviceLayerProperties = featureList.get(0).getLayerProperties();
                        final LayerProperties newLayerProperties = serviceLayerProperties.clone();
                        int count = 0;

                        newLayerProperties.setFeatureService((AbstractFeatureService)
                            serviceLayerProperties.getFeatureService().clone());

                        final List<String> orderedAttributeNames = new ArrayList();
                        final Map<String, FeatureServiceAttribute> attributes = new HashMap<>();
                        FeatureServiceAttribute attr = new FeatureServiceAttribute("id", "integer", false);
                        attributes.put("id", attr);
                        orderedAttributeNames.add("id");
                        attr = new FeatureServiceAttribute("stat", String.valueOf(Types.INTEGER), false);
                        orderedAttributeNames.add("stat");
                        attributes.put("stat", attr);
                        attr = new FeatureServiceAttribute("geom", "Geometry", false);
                        orderedAttributeNames.add("geom");
                        attributes.put("geom", attr);
                        attr = new FeatureServiceAttribute("stat_km", String.valueOf(Types.DOUBLE), false);
                        orderedAttributeNames.add("stat_km");
                        attributes.put("stat_km", attr);
                        attr = new FeatureServiceAttribute("stat_c", String.valueOf(Types.VARCHAR), false);
                        orderedAttributeNames.add("stat_c");
                        attributes.put("stat_c", attr);
                        newLayerProperties.getFeatureService().setFeatureServiceAttributes(attributes);

                        wd.setText(NbBundle.getMessage(
                                StationDialog.class,
                                "BufferDialog.butOkActionPerformed.doInBackground.createFeatures"));
                        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                CismapBroker.getInstance().getDefaultCrsAlias());
                        H2FeatureService service = null;
                        int featuresCreated = 0;

                        // creates stations
                        for (final FeatureServiceFeature f : featureList) {
                            final Geometry geom = f.getGeometry();
                            ++count;

                            if (geom == null) {
                                continue;
                            }

                            final LengthIndexedLine lil = new LengthIndexedLine(geom);

                            for (int geomIndex = 0; geomIndex < geom.getLength(); geomIndex = geomIndex + distance) {
                                final Coordinate coordinate = lil.extractPoint(geomIndex);

                                if (service != null) {
                                    final JDBCFeature newFeature = (JDBCFeature)service.getFeatureFactory()
                                                .createNewFeature();
                                    newFeature.setProperty("stat", geomIndex);
                                    newFeature.setProperty("stat_km", geomIndex / 1000.0);
                                    newFeature.setProperty(
                                        "stat_c",
                                        ((int)geomIndex / 1000)
                                                + "+"
                                                + (geomIndex % 1000));
                                    newFeature.setGeometry(factory.createPoint(coordinate));
                                    newFeature.saveChangesWithoutUpdateEnvelope();
                                } else {
                                    final FeatureServiceFeature newFeature = (FeatureServiceFeature)f.clone();
                                    newFeature.setLayerProperties(newLayerProperties);
                                    newFeature.setGeometry(factory.createPoint(coordinate));
                                    newFeature.setProperty("stat", geomIndex);
                                    newFeature.setProperty("stat_km", geomIndex / 1000.0);
                                    newFeature.setProperty(
                                        "stat_c",
                                        ((int)geomIndex / 1000)
                                                + "+"
                                                + (geomIndex % 1000));
                                    resultedFeatures.add(newFeature);
                                    ++featuresCreated;
                                }
                            }

                            if (Thread.interrupted()) {
                                return null;
                            }
                            if (featuresCreated > 50000) {
                                if (service == null) {
                                    service = FeatureServiceHelper.createNewService(AppBroker.getInstance()
                                                    .getWatergisApp(),
                                            resultedFeatures,
                                            tableName,
                                            orderedAttributeNames);
                                }

                                featuresCreated = 0;
                            }

                            // refresh the progress bar
                            if (progress < (10 + (count * 80 / featureList.size()))) {
                                progress = 10
                                            + (count * 80 / featureList.size());
                                wd.setProgress(progress);
                            }
                        }

                        if (Thread.interrupted()) {
                            return null;
                        }

                        if (service == null) {
                            // create the service
                            wd.setText(NbBundle.getMessage(
                                    StationDialog.class,
                                    "BufferDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
                            return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                                    resultedFeatures,
                                    tableName,
                                    orderedAttributeNames);
                        } else {
                            return service;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            final H2FeatureService service = get();

                            if (service != null) {
                                service.setSLDInputStream(SLD.replace("$LAYER_NAME$", tableName));
                                FeatureServiceHelper.addServiceLayerToTheTree(service);
                            }
                        } catch (Exception ex) {
                            LOG.error("Error while execute the buffer operation.", ex);
                        }
                    }
                };

            if (H2FeatureService.tableAlreadyExists(tableName)) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        StationDialog.class,
                        "BufferDialog.butOkActionPerformed.tableAlreadyExists",
                        tableName),
                    NbBundle.getMessage(
                        StationDialog.class,
                        "BufferDialog.butOkActionPerformed.tableAlreadyExists.title"),
                    JOptionPane.ERROR_MESSAGE);
            } else {
                this.setVisible(false);
                wdt.start();
            }
        } catch (Exception e) {
            LOG.error("Error while execute the buffer operation.", e);
        }
    } //GEN-LAST:event_butOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double toDouble(final Object o) {
        if (o == null) {
            return 0;
        } else {
            final String doubleAsString = o.toString();

            try {
                return Double.parseDouble(doubleAsString);
            } catch (NumberFormatException e) {
                LOG.error(o.toString() + " is not a number.", e);
                return 0;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbThemeActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbThemeActionPerformed
        final AbstractFeatureService service = (AbstractFeatureService)cbTheme.getSelectedItem();
        selectedThemeFeatureCount = refreshSelectedFeatureCount(
                false,
                ckbSelected,
                service,
                selectedThemeFeatureCount,
                labSelected);
        enabledOrNot();
    }                                                                           //GEN-LAST:event_cbThemeActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbDistanceActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbDistanceActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_cbDistanceActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtTableActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtTableActionPerformed
        txtTable.setText("Stationen|frei");
    }                                                                            //GEN-LAST:event_txtTableActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtAbstActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_txtAbstActionPerformed
        // TODO add your handling code here:
    } //GEN-LAST:event_txtAbstActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void txtAbstFocusLost(final java.awt.event.FocusEvent evt) { //GEN-FIRST:event_txtAbstFocusLost
//        try {
//            if (!txtAbst.getText().equals("")) {
//                Integer.parseInt(txtAbst.getText());
//            }
//        } catch (NumberFormatException e) {
//            final MessageDialog d = new MessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    true,
//                    "Bitte nur ganzzahlige Werte verwenden.");
//            d.setSize(500, 80);
//            StaticSwingTools.showDialog(d);
//            txtAbst.setText("");
//        }
    } //GEN-LAST:event_txtAbstFocusLost

    /**
     * refreshes the labSelectedFeatures label.
     *
     * @param   forceGuiRefresh           DOCUMENT ME!
     * @param   box                       DOCUMENT ME!
     * @param   featureService            DOCUMENT ME!
     * @param   lastSelectedFeatureCount  DOCUMENT ME!
     * @param   selectionCountlab         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int refreshSelectedFeatureCount(final boolean forceGuiRefresh,
            final JCheckBox box,
            final AbstractFeatureService featureService,
            final int lastSelectedFeatureCount,
            final JLabel selectionCountlab) {
        final int count = ((featureService == null) ? 0
                                                    : FeatureServiceHelper.getSelectedFeatures(featureService).size());

        selectionCountlab.setText(NbBundle.getMessage(
                StationDialog.class,
                "BufferDialog.refreshSelectedFeatureCount.text",
                count));

        if (forceGuiRefresh || (count != lastSelectedFeatureCount)) {
            box.setSelected(count > 0);
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     */
    private void setLayerModel() {
        final Object selectedObject = cbTheme.getSelectedItem();
        cbTheme.setModel(new DefaultComboBoxModel(
                new String[] { NbBundle.getMessage(StationDialog.class,
                        "BufferDialog.setlayerModel.searchServices") }));

        final Thread t = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        cbTheme.setModel(
                            new DefaultComboBoxModel(
                                FeatureServiceHelper.getServices(new String[] { "LineString" }).toArray(
                                    new AbstractFeatureService[0])));

                        if (selectedObject != null) {
                            cbTheme.setSelectedItem(selectedObject);
                        } else {
                            if (cbTheme.getModel().getSize() > 0) {
                                cbTheme.setSelectedIndex(0);
                            } else {
                                cbTheme.setSelectedItem(null);
                            }
                        }
                    }
                });

        t.start();
    }

    /**
     * DOCUMENT ME!
     */
    private void enabledOrNot() {
        final boolean isServiceSelected = (cbTheme.getSelectedItem() instanceof AbstractFeatureService)
                    && (txtTable.getText() != null)
                    && !txtTable.getText().equals("");

        butOk.setEnabled(isServiceSelected);
    }
}
