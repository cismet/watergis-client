/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.panels;

import edu.umd.cs.piccolox.event.PNotificationCenter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

import org.mortbay.log.Log;

import java.awt.BorderLayout;

import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.AttachFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.FeatureMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.JoinPolygonsListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SimpleMoveListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SplitPolygonListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MapPanel extends javax.swing.JPanel implements FeatureCollectionListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MapPanel.class);

    //~ Instance fields --------------------------------------------------------

    private MappingComponent mappingComponent;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates new form KartenPanel.
     */
    public MapPanel() {
        initComponents();
        initMap();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(
            jLabel1,
            org.openide.util.NbBundle.getMessage(MapPanel.class, "MapPanel.jLabel1.text")); // NOI18N
        add(jLabel1, java.awt.BorderLayout.CENTER);
    }                                                                                       // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     */
    private void initMap() {
        mappingComponent = CismapBroker.getInstance().getMappingComponent();
        mappingComponent.getFeatureCollection().addFeatureCollectionListener(this);
        mappingComponent.setBackgroundEnabled(true);
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "attachFeatureRequested",
                    AttachFeatureListener.ATTACH_FEATURE_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.ATTACH_POLYGON_TO_ALPHADATA));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "selectionChanged",
                    SplitPolygonListener.SELECTION_CHANGED,
                    mappingComponent.getInputListener(MappingComponent.SPLIT_POLYGON));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "splitPolygon",
                    SplitPolygonListener.SPLIT_FINISHED,
                    mappingComponent.getInputListener(MappingComponent.SPLIT_POLYGON));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "featureDeleteRequested",
                    DeleteFeatureListener.FEATURE_DELETE_REQUEST_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.REMOVE_POLYGON));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "joinPolygons",
                    JoinPolygonsListener.FEATURE_JOIN_REQUEST_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.JOIN_POLYGONS));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "selectionChanged",
                    PSelectionEventHandler.SELECTION_CHANGED_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.SELECT));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "selectionChanged",
                    FeatureMoveListener.SELECTION_CHANGED_NOTIFICATION,
                    mappingComponent.getInputListener(MappingComponent.MOVE_POLYGON));
        PNotificationCenter.defaultCenter()
                .addListener(
                    this,
                    "coordinatesChanged",
                    SimpleMoveListener.COORDINATES_CHANGED,
                    mappingComponent.getInputListener(MappingComponent.MOTION));
        this.add(BorderLayout.CENTER, mappingComponent);
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.featuresAdded(): Not supported yet.");
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.allFeaturesRemoved(): Not supported yet.");
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.featuresRemoved(): Not supported yet.");
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.featuresChanged(): Not supported yet.");
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.featureSelectionChanged(): Not supported yet.");
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
        Log.info("MapPanel.featureReconsiderationRequested(): Not supported yet.");
    }

    @Override
    public void featureCollectionChanged() {
        Log.info("MapPanel.featureCollectionChanged(): Not supported yet.");
    }
}
