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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingWorker;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.commons.concurrency.CismetConcurrency;

import de.cismet.tools.gui.ScrollableComboBox;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ZoomPanel extends javax.swing.JPanel {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ZoomPanel.class);

    //~ Instance fields --------------------------------------------------------

    protected boolean routeModelInitialised = false;
    protected final MetaClass objectMc;
    protected String featureClass;
    protected final String nameAttr;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JComboBox cbObjects;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form ZoomPanel.
     *
     * @param  objectMc      DOCUMENT ME!
     * @param  featureClass  DOCUMENT ME!
     * @param  nameAttr      type DOCUMENT ME!
     */
    public ZoomPanel(final MetaClass objectMc, final String featureClass, final String nameAttr) {
        initComponents();
        this.objectMc = objectMc;
        this.nameAttr = nameAttr;
        AutoCompleteDecorator.decorate(cbObjects);
        this.featureClass = featureClass;
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public void init() {
        cbObjects.setModel(new DefaultComboBoxModel(new Object[] { "Lade ..." }));
        final SwingWorker sw = new SwingWorker<GeometryElement[], Void>() {

                @Override
                protected GeometryElement[] doInBackground() throws Exception {
                    if (objectMc != null) {
                        final CidsLayer layer = new CidsLayer(objectMc);
                        layer.initAndWait();
                        final Map<String, FeatureServiceAttribute> attributes = layer.getFeatureServiceAttributes();
                        String userGroup = SessionManager.getSession().getUser().getUserGroup().getName();

                        if (userGroup.endsWith("_edit")) {
                            userGroup = userGroup.substring(0, userGroup.indexOf("_edit"));
                        }
                        final FeatureServiceAttribute attr = attributes.get("gmd_" + userGroup);
                        // theme vw_alk_gmd should only contain the object of the current user (if it is a wbv user)
                        final String query = ((attr == null) ? layer.getQuery() : (attr.getName() + "=" + 1));
                        final List features = layer.getFeatureFactory().createFeatures(query, null, null);
                        final List<GeometryElement> beans = new ArrayList<GeometryElement>();

                        if ((features != null) && !features.isEmpty()) {
                            for (final Object f : features) {
                                if (f instanceof CidsLayerFeature) {
                                    final CidsLayerFeature feature = (CidsLayerFeature)f;

                                    beans.add(new GeometryElement(
                                            feature.getId(),
                                            String.valueOf(feature.getProperty(nameAttr)),
                                            feature.getGeometry().getEnvelope(),
                                            feature.getProperty(nameAttr)
                                                    instanceof Integer));
                                }
                            }
                        }

                        Collections.sort(beans);
                        return beans.toArray(new GeometryElement[beans.size()]);
                    } else {
                        // The user has no read permissions for the route meta class
                        return new GeometryElement[0];
                    }
                }

                @Override
                protected void done() {
                    try {
                        final GeometryElement[] tmp = get();
                        cbObjects.setModel(new DefaultComboBoxModel(tmp));
                        routeModelInitialised = true;
                    } catch (Exception e) {
                        LOG.error("Error while initializing the model of the route combobox", e); // NOI18N
                    }
                }
            };

        CismetConcurrency.getInstance("watergis").getDefaultExecutor().execute(sw);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        cbObjects = new ScrollableComboBox();

        setLayout(new java.awt.GridBagLayout());

        cbObjects.setEditable(true);
        cbObjects.setMaximumSize(new java.awt.Dimension(240, 24));
        cbObjects.setMinimumSize(new java.awt.Dimension(240, 24));
        cbObjects.setPreferredSize(new java.awt.Dimension(240, 24));
        cbObjects.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    cbObjectsActionPerformed(evt);
                }
            });
        add(cbObjects, new java.awt.GridBagConstraints());
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void cbObjectsActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_cbObjectsActionPerformed
        final Object selectedObject = cbObjects.getSelectedItem();

        if (routeModelInitialised && (selectedObject instanceof GeometryElement)) {
            final GeometryElement selectedRoute = (GeometryElement)cbObjects.getSelectedItem();
            final XBoundingBox bbox = (XBoundingBox)selectedRoute.getEnvelope().clone();
            bbox.increase(10);
            final int id = selectedRoute.getId();

            CismapBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(bbox);

            if (bbox.getX1() == bbox.getX2()) {
                CismapBroker.getInstance()
                        .getMappingComponent()
                        .gotoBoundingBoxWithHistory(CismapBroker.getInstance().getMappingComponent()
                            .getBoundingBoxFromScale(500));
            }

            final Thread t = new Thread("selectAfterZoomOnRiver") {

                    @Override
                    public void run() {
                        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                                featureClass);

                        for (final AbstractFeatureService service : services) {
                            try {
                                service.initAndWait();
                                final List<FeatureServiceFeature> features = service.getFeatureFactory()
                                            .createFeatures("dlm25w." + featureClass + ".id = " + id,
                                                null,
                                                null,
                                                0,
                                                1,
                                                null);

                                if ((features != null) && !features.isEmpty()) {
                                    SelectionManager.getInstance().setSelectedFeaturesForService(service, features);
                                }
                            } catch (Exception e) {
                                LOG.error("Error while selecting " + featureClass + " object", e);
                            }
                        }
                    }
                };

            t.start();
        } else {
            final Thread t = new Thread("selectAfterZoomOnRiver") {

                    @Override
                    public void run() {
                        final List<AbstractFeatureService> services = FeatureServiceHelper.getCidsLayerServicesFromTree(
                                featureClass);

                        for (final AbstractFeatureService service : services) {
                            try {
                                SelectionManager.getInstance()
                                        .setSelectedFeaturesForService(service, new ArrayList<Feature>());
                            } catch (Exception e) {
                                LOG.error("Error while selecting " + featureClass + " object", e);
                            }
                        }
                    }
                };

            t.start();
        }
    } //GEN-LAST:event_cbObjectsActionPerformed

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class GeometryElement implements Comparable<GeometryElement> {

        //~ Instance fields ----------------------------------------------------

        private XBoundingBox env;
        private final String name;
        private final int id;
        private boolean isInteger;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RouteElement object.
         *
         * @param  id         DOCUMENT ME!
         * @param  name       bean DOCUMENT ME!
         * @param  g          DOCUMENT ME!
         * @param  isInteger  DOCUMENT ME!
         */
        public GeometryElement(final int id, final String name, final Geometry g, final boolean isInteger) {
            this.name = name;
            this.id = id;
            this.isInteger = isInteger;

            if (g != null) {
                env = new XBoundingBox(g);
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public XBoundingBox getEnvelope() {
            return env;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the id
         */
        public int getId() {
            return id;
        }

        @Override
        public int compareTo(final GeometryElement o) {
            if (isInteger) {
                try {
                    final Integer i1 = Integer.parseInt(toString());
                    final Integer i2 = Integer.parseInt(o.toString());

                    return i1.compareTo(i2);
                } catch (NumberFormatException e) {
                    return toString().compareTo(o.toString());
                }
            } else {
                return toString().compareTo(o.toString());
            }
        }
    }
}
