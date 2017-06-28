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

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.util.Blackboard;
import com.vividsolutions.jump.util.java2xml.Java2XML;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;

import de.latlon.deejump.plugin.style.LayerStyle2SLDPlugIn;

import org.apache.batik.ext.swing.GridBagConstants;
import org.apache.log4j.Logger;

import org.deegree.style.persistence.sld.SLDParser;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;

import de.cismet.cismap.commons.styling.CustomSLDParser;

import de.cismet.jump.sld.editor.CidsRenderingStylePanel;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class VisualizingDialog extends javax.swing.JDialog {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(VisualizingDialog.class);

    //~ Instance fields --------------------------------------------------------

    private final CidsRenderingStylePanel stylePanel;
    private final Blackboard blackboard;
    private final Blackboard persistenceBlackboard;
    private final Layer layer;
    private boolean canceled = true;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbCancel;
    private javax.swing.JButton jbOk;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form VisualizingDialog.
     */
    private VisualizingDialog() {
        super(AppBroker.getInstance().getWatergisApp(), true);
        initComponents();

        final LayerManager layerManager = new LayerManager();
        final FeatureSchema featureSchema = new FeatureSchema();
        final FeatureCollection features = new FeatureDataset(featureSchema);

        layerManager.setFiringEvents(false);
        if (AppBroker.getInstance().getDrawingStyleLayer() != null) {
            layer = AppBroker.getInstance().getDrawingStyleLayer();
        } else {
            layer = new Layer("default", Color.RED, features, layerManager);
        }
        blackboard = new Blackboard();
        persistenceBlackboard = new Blackboard();
        stylePanel = new CidsRenderingStylePanel(blackboard, layer, persistenceBlackboard);
        final GridBagConstraints constraint = new java.awt.GridBagConstraints(
                0,
                0,
                1,
                1,
                1.0,
                1.0,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0),
                0,
                0);
        jPanel1.add(stylePanel, constraint);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static VisualizingDialog getInstance() {
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
        jbOk = new javax.swing.JButton();
        jbCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jbOk,
            org.openide.util.NbBundle.getMessage(
                VisualizingDialog.class,
                "VisualizingDialog.jbOk.text",
                new Object[] {})); // NOI18N
        jbOk.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jbOkActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 5);
        getContentPane().add(jbOk, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jbCancel,
            org.openide.util.NbBundle.getMessage(
                VisualizingDialog.class,
                "VisualizingDialog.jbCancel.text",
                new Object[] {})); // NOI18N
        jbCancel.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jbCancelActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 10);
        getContentPane().add(jbCancel, gridBagConstraints);

        pack();
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jbOkActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jbOkActionPerformed
        canceled = false;
        stylePanel.updateStyles();
        setVisible(false);
    }                                                                        //GEN-LAST:event_jbOkActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jbCancelActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jbCancelActionPerformed
        canceled = true;
        setVisible(false);
    }                                                                            //GEN-LAST:event_jbCancelActionPerformed

    /**
     * DOCUMENT ME!
     *
     * @param   layer         DOCUMENT ME!
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String exportSLD(final Layer layer, final String geometryType) {
        String sld = null;
        try {
            final Java2XML java2Xml = new Java2XML();
            final StringWriter xmlWriter = new StringWriter();
//            final Geometry geom = firstFeature.getGeometry();
            final String name = "default";

            java2Xml.write(layer, "layer", xmlWriter);
            final HashMap<String, String> params = new HashMap<String, String>();
            params.put("wmsLayerName", name);
            params.put("featureTypeStyle", name);
            params.put("styleName", name);
            params.put("styleTitle", name);
            params.put("Namespace", "http://cismet.de");
            params.put("NamespacePrefix", "");
            params.put("geoType", geometryType);
            params.put("geomProperty", "geom");
            if (layer.getMinScale() != null) {
                params.put("maxScale", "" + layer.getMinScale());
            }
            if (layer.getMaxScale() != null) {
                params.put("minScale", "" + layer.getMaxScale());
            }

            sld = LayerStyle2SLDPlugIn.transformContext(new StringReader(xmlWriter.toString()), params);
        } catch (Exception e) {
            LOG.info("could not save sld definition", e);
        }
        return sld;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   layer         DOCUMENT ME!
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> getStyles(final Layer layer,
            final String geometryType) {
        final Reader input = new StringReader(exportSLD(layer, geometryType));
        Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> styles = null;
        final XMLInputFactory factory = XMLInputFactory.newInstance();

        try {
            styles = CustomSLDParser.getStyles(factory.createXMLStreamReader(input));
        } catch (Exception ex) {
            LOG.error("Fehler in der SLD", ex);
        }
        if (styles == null) {
            LOG.info("SLD Parser funtkioniert nicht");
        }
        return styles;
    }

//    /**
//     * DOCUMENT ME!
//     *
//     * @return  the layer
//     */
//    public BasicStyle getBasicStyle() {
//        return layer.getBasicStyle();
//    }
//
//    /**
//     * DOCUMENT ME!
//     *
//     * @return  the layer
//     */
//    public VertexStyle getVertexStyle() {
//        return layer.getVertexStyle();
//    }

    /**
     * DOCUMENT ME!
     *
     * @return  the layer
     */
    public Layer getStyleLayer() {
        return layer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the canceled
     */
    public boolean isCanceled() {
        return canceled;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final transient VisualizingDialog INSTANCE = new VisualizingDialog();

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LazyInitializer object.
         */
        private LazyInitializer() {
        }
    }
}
