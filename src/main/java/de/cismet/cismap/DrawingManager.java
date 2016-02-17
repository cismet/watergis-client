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
package de.cismet.cismap;

import com.vividsolutions.jts.geom.Geometry;

import edu.umd.cs.piccolo.PNode;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import org.h2gis.utilities.wrapper.ConnectionWrapper;
import org.h2gis.utilities.wrapper.StatementWrapper;

import org.openide.util.Exceptions;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import java.io.File;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.AbstractNewFeature;
import de.cismet.cismap.commons.features.DrawingFeature;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.FeatureCollectionEvent;
import de.cismet.cismap.commons.features.FeatureCollectionListener;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.cismap.io.converters.GeomFromWktConverter;

import de.cismet.commons.concurrency.CismetConcurrency;
import de.cismet.commons.concurrency.CismetExecutors;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.VisualizingDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DrawingManager implements FeatureCollectionListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DrawingManager.class);
//    private static final String INIT_DB_SEQUENCE = "CREATE SEQUENCE IF NOT EXISTS \"cids_system.drawing_seq\" START WITH 1";
    public static final String DRAWING_TABLE_NAME = "Zeichnungen";
    public static final String CHECK_TABLE = "select sld from \"" + DRAWING_TABLE_NAME + "\"";
    private static final String INIT_DB_TABLE = "CREATE TABLE IF NOT EXISTS \"" + DRAWING_TABLE_NAME
                + "\" (id identity primary key not null, geom Geometry, type varchar, text varchar, autoscale boolean, background boolean, fontsize integer, sld text)";
    private static final String ADD_FEATURE = "INSERT INTO \"" + DRAWING_TABLE_NAME
                + "\" (geom, type, text, autoscale, background, fontsize, sld) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_FEATURE = "DELETE FROM \"" + DRAWING_TABLE_NAME + "\" WHERE ID = %1$s";
    private static final String DELETE_ALL_FEATURE = "DELETE FROM \"" + DRAWING_TABLE_NAME + "\"";
    private static final String SELECT_ALL_FEATURES =
        "SELECT id, geom, type, text, autoscale, background, fontsize, sld FROM \""
                + DRAWING_TABLE_NAME
                + "\"";
    private static final String CHANGE_FEATURE = "UPDATE \""
                + DRAWING_TABLE_NAME
                + "\" SET geom = ?, type = ?, text = ?, sld = ?  WHERE ID = ?";
    private static final String CREATE_TABLE_FROM_CSV = "CREATE TABLE \"%s\" as select * from CSVREAD('%s');";
    private static final String TEMP_TABLE = "temp_drawing";

    //~ Instance fields --------------------------------------------------------

    private final Executor executor = CismetExecutors.newSingleThreadExecutor();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DrawingManager object.
     */
    public DrawingManager() {
        AppBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatureCollectionListener(this);
        initDB();
        loadFeatures();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    public static void loadFeatures() {
        final List<Feature> featuresToRemove = new ArrayList<Feature>();
        final List<Feature> oldFeatures = AppBroker.getInstance()
                    .getMappingComponent()
                    .getFeatureCollection()
                    .getAllFeatures();

        for (final Feature f : oldFeatures) {
            if (f instanceof DrawingSLDStyledFeature) {
                featuresToRemove.add(f);
            }
        }

        AppBroker.getInstance().getMappingComponent().getFeatureCollection().removeFeatures(featuresToRemove);

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final List<DrawingSLDStyledFeature> features = getAllFeatures();
                    AppBroker.getInstance().getMappingComponent().getFeatureCollection().addFeatures(features);
                }
            });
    }

    /**
     * DOCUMENT ME!
     */
    private static void initDB() {
        ConnectionWrapper conn = null;
        StatementWrapper st = null;
        try {
            conn = H2FeatureServiceFactory.getDBConnection(null);
            H2FeatureServiceFactory.initDatabase(conn);
            st = H2FeatureServiceFactory.createStatement(conn);

            try {
                st.execute(CHECK_TABLE);
            } catch (Exception e) {
                // the sld field does no exists, so delete the table to create a new on ewith the sld field
                try {
                    st.execute("drop table \"" + DRAWING_TABLE_NAME + "\"");
                } catch (Exception ex) {
                    // nothing to do
                }
            }

            st.execute(INIT_DB_TABLE);
        } catch (SQLException e) {
            LOG.error("Error while initialising the internal database", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing a db statement", ex);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing the db connection", ex);
                }
            }
        }
    }

    @Override
    public void featuresAdded(final FeatureCollectionEvent fce) {
        orderFeatures(fce);

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final Collection<Feature> features = new ArrayList<Feature>(fce.getEventFeatures());

                    for (final Feature feature : features) {
                        if (feature instanceof DrawingFeature) {
                            if (((DrawingFeature)feature).getId() == -1) {
                                addFeatureToDb((DrawingFeature)feature);
                                final DrawingSLDStyledFeature styledFeature = new DrawingSLDStyledFeature(
                                        (DrawingFeature)feature);
                                if (AppBroker.getInstance().getDrawingStyleLayer() != null) {
                                    styledFeature.setSLDStyles(
                                        AppBroker.getInstance().getDrawingStyles(
                                            feature.getGeometry().getGeometryType()).get("default"));
                                }
                                AppBroker.getInstance()
                                        .getMappingComponent()
                                        .getFeatureCollection()
                                        .removeFeature(feature);
                                AppBroker.getInstance()
                                        .getMappingComponent()
                                        .getFeatureCollection()
                                        .addFeature(styledFeature);
                            }
                        }
                    }
                }
            });
    }

    @Override
    public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
        executor.execute(new Runnable() {

                @Override
                public void run() {
                    removeAllFeatures();
                }
            });
    }

    @Override
    public void featuresRemoved(final FeatureCollectionEvent fce) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final Collection<Feature> features = fce.getEventFeatures();

                    for (final Feature feature : features) {
                        if (feature instanceof DrawingSLDStyledFeature) {
                            removeFeature((DrawingSLDStyledFeature)feature);
                        }
                    }
                }
            });
    }

    @Override
    public void featuresChanged(final FeatureCollectionEvent fce) {
        executor.execute(new Runnable() {

                @Override
                public void run() {
                    final Collection<Feature> features = fce.getEventFeatures();

                    for (final Feature feature : features) {
                        if (feature instanceof DrawingSLDStyledFeature) {
                            if (((DrawingSLDStyledFeature)feature).getId() != -1) {
                                changeFeature((DrawingSLDStyledFeature)feature);
                            }
                        }
                    }
                }
            });
    }

    @Override
    public void featureSelectionChanged(final FeatureCollectionEvent fce) {
        orderFeatures(fce);
    }

    @Override
    public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
    }

    @Override
    public void featureCollectionChanged() {
    }

    /**
     * Correct the ordering of the features of the given feature collection.
     *
     * @param  fce  DOCUMENT ME!
     */
    private void orderFeatures(final FeatureCollectionEvent fce) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    orderAllFeatures();
                }
            });
    }

//    /**
//     * Correct the ordering of the give features. This method ensures the feature ordering. This means, text features
//     * are in front of point and points are in front of linestring and so on.
//     *
//     * @param  fce  DOCUMENT ME!
//     */
//    private void orderFeature_internal(final FeatureCollectionEvent fce) {
//        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
//        final FeatureCollection fc = map.getFeatureCollection();
//        final Collection<Feature> eventFeatures = fce.getEventFeatures();
//
//        if ((eventFeatures == null) || (eventFeatures.size() > 1)) {
//            // orderAllFeatures has a better performance as this method with more than one feature
//            orderAllFeatures();
//            return;
//        }
//
//        for (final Feature eventFeature : eventFeatures) {
//            if (eventFeature instanceof DrawingFeature) {
//                final DrawingFeature eFeature = (DrawingFeature)eventFeature;
//                final PFeature eventPFeature = map.getPFeatureHM().get(eventFeature);
//
//                if (eventPFeature != null) {
//                    for (final Feature feature : fc.getAllFeatures()) {
//                        if (feature instanceof DrawingFeature) {
//                            final DrawingFeature dFeature = (DrawingFeature)feature;
//                            final PFeature otherPFeature = map.getPFeatureHM().get(dFeature);
//
//                            if (otherPFeature != null) {
//                                if (eFeature.getTypeOrder() < dFeature.getTypeOrder()) {
//                                    eventPFeature.moveInFrontOf(otherPFeature);
//                                } else {
//                                    if (eFeature.getTypeOrder() != dFeature.getTypeOrder()) {
//                                        otherPFeature.moveInFrontOf(eventPFeature);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * Order all features. This method ensures the feature ordering. This means, text features are in front of point and
     * points are in front of linestring and so on.
     */
    private void orderAllFeatures() {
        final HashMap<Integer, List<PFeature>> orderMap = new HashMap<Integer, List<PFeature>>();
        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        final FeatureCollection fc = map.getFeatureCollection();
        final List<Feature> features = new ArrayList<Feature>(fc.getAllFeatures());
        PNode parentNode = null;

        for (final Feature feature : features) {
            if (feature instanceof DrawingSLDStyledFeature) {
                final DrawingSLDStyledFeature dFeature = (DrawingSLDStyledFeature)feature;
                final Integer order = dFeature.getTypeOrder();

                List<PFeature> featureListForPosition = orderMap.get(order);

                if (featureListForPosition == null) {
                    featureListForPosition = new ArrayList<PFeature>();
                    orderMap.put(order, featureListForPosition);
                }

                final PFeature pfeature = map.getPFeatureHM().get(dFeature);
                if (pfeature != null) {
                    featureListForPosition.add(pfeature);
                }
                if (parentNode == null) {
                    parentNode = pfeature.getParent();
                }
                if ((parentNode != null) && (parentNode.getChildrenCount() > 0)) {
                    parentNode.removeChild(pfeature);
                }
            }
        }

        if (parentNode != null) {
            for (int i = DrawingFeature.getTypeOrderCount() - 1; i >= 0; --i) {
                final List<PFeature> featureListForPosition = orderMap.get(i);

                if (featureListForPosition != null) {
                    for (final PFeature feature : featureListForPosition) {
                        parentNode.addChild(feature);
                    }
                }
            }
        }
    }

    /**
     * Add the given feature to the db.
     *
     * @param  feature  the feature to add
     */
    private static synchronized void addFeatureToDb(final DrawingFeature feature) {
        ConnectionWrapper cw = null;
        PreparedStatement ps = null;

        try {
            cw = H2FeatureServiceFactory.getDBConnection(null);
            feature.setGeometry(CrsTransformer.transformToDefaultCrs(feature.getGeometry()));
            String sld = feature.getSld();

            if (sld == null) {
                sld = VisualizingDialog.exportSLD(VisualizingDialog.getInstance().getStyleLayer(),
                        feature.getGeometry().getGeometryType());
            }

            ps = cw.prepareStatement(ADD_FEATURE);
            ps.setString(1, feature.getGeometry().toText());
            ps.setString(2, feature.getGeometryType().name());
            ps.setString(
                3,
                ((feature.getGeometryType().equals(AbstractNewFeature.geomTypes.TEXT)) ? (feature.getName()) : null));
            ps.setString(4, String.valueOf(feature.isAutoscale()));
            ps.setBoolean(5, feature.getPrimaryAnnotationHalo() != null);
            ps.setObject(
                6,
                ((feature.getPrimaryAnnotationFont() != null) ? feature.getPrimaryAnnotationFont().getSize() : null));
            ps.setString(7, sld);

            ps.execute();
            LOG.error(ADD_FEATURE);
            final ResultSet rs = ps.getGeneratedKeys();

            if (rs.next()) {
                feature.setId(rs.getInt(1));
            } else {
                LOG.error("Error: ID for drawing feature was not generated.");
            }
        } catch (Exception e) {
            LOG.error("Error while inserting new feature into the internal db.", e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
            if (cw != null) {
                try {
                    cw.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing connection", ex);
                }
            }
        }
    }

    /**
     * Remove the given feature.
     *
     * @param  feature  the feature to remove
     */
    private void removeFeature(final DrawingSLDStyledFeature feature) {
        final String delete = String.format(DELETE_FEATURE, feature.getId());
        executeUpdate(delete);
    }

    /**
     * save the given features.
     *
     * @param  feature  the feature to save. It should be already conteined in the database
     */
    private void changeFeature(final DrawingSLDStyledFeature feature) {
        ConnectionWrapper cw = null;
        PreparedStatement ps = null;

        try {
            cw = H2FeatureServiceFactory.getDBConnection(null);
            ps = cw.prepareStatement(CHANGE_FEATURE);
            ps.setString(1, feature.getGeometry().toString());
            ps.setString(2, feature.getGeometryType().name());
            ps.setString(
                3,
                ((feature.getGeometryType().equals(AbstractNewFeature.geomTypes.TEXT)) ? (feature.getText()) : null));
            ps.setString(4, (String)feature.getProperty("sld"));
            ps.setInt(5, feature.getId());

            ps.execute();
        } catch (Exception e) {
            LOG.error("Error while executing the following statement: " + CHANGE_FEATURE, e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
            if (cw != null) {
                try {
                    cw.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing connection", ex);
                }
            }
        }
    }

    /**
     * remove all features from the database.
     */
    public static void removeAllFeatures() {
        executeUpdate(DELETE_ALL_FEATURE);
    }

    /**
     * Add the features from the given file to the db.
     *
     * @param  drawingFile  DOCUMENT ME!
     */
    public static synchronized void addFeatures(final File drawingFile) {
        final String readCvsQuery = String.format(CREATE_TABLE_FROM_CSV, TEMP_TABLE, drawingFile.getAbsolutePath());
        ConnectionWrapper cw = null;
        StatementWrapper st = null;
        final GeomFromWktConverter converter = new GeomFromWktConverter();

        try {
            cw = H2FeatureServiceFactory.getDBConnection(null);
            st = H2FeatureServiceFactory.createStatement(cw);
            st.executeUpdate(String.format("DROP TABLE  IF EXISTS \"%1$s\"", TEMP_TABLE));
            st.executeUpdate(readCvsQuery);
            final ResultSet rs = st.executeQuery(String.format(
                        "SELECT id, geom, type, text, autoscale, background, fontsize, sld FROM \"%1$s\"",
                        TEMP_TABLE));
            final Base64 base64 = new Base64();

            while (rs.next()) {
                final int id = rs.getInt(1);
                final String geomAsTExt = rs.getString(2);
                final String type = rs.getString(3);
                final String text = rs.getString(4);
                final boolean autoscale = Boolean.parseBoolean(rs.getString(5));
                final boolean halo = Boolean.parseBoolean(rs.getString(6));
                final String sld = new String(base64.decode(rs.getString(8)));
                final Geometry geom = converter.convertForward(geomAsTExt, CismapBroker.getInstance().getDefaultCrs());
                geom.setSRID(CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs()));
                final DrawingFeature feature = new DrawingFeature(geom);

//                feature.setId(id);
                if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                    feature.setName(text);
                }
                feature.setGeometryType(AbstractNewFeature.geomTypes.valueOf(type));
                feature.setEditable(true);
                feature.setSld(sld);

                if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                    final int fontsize = Integer.parseInt(rs.getString(7));

                    feature.setAutoScale(autoscale);
                    if (halo) {
                        feature.setPrimaryAnnotationHalo(Color.WHITE);
                    }
                    feature.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, fontsize));
                }

                addFeatureToDb(feature);
            }
        } catch (Exception e) {
            LOG.error("Error while retreiving all features from the database: ", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
            if (cw != null) {
                try {
                    cw.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing connection", ex);
                }
            }
            executeUpdate("drop table \"" + TEMP_TABLE + "\"");
        }
    }

    /**
     * Execute the given statement.
     *
     * @param  statement  the statement to execute
     */
    private static synchronized void executeUpdate(final String statement) {
        ConnectionWrapper cw = null;
        StatementWrapper st = null;

        try {
            cw = H2FeatureServiceFactory.getDBConnection(null);
            st = H2FeatureServiceFactory.createStatement(cw);
            LOG.error(statement, new Exception());
            st.executeUpdate(statement);
        } catch (Exception e) {
            LOG.error("Error while executing the following statement: " + statement, e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
            if (cw != null) {
                try {
                    cw.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing connection", ex);
                }
            }
        }
    }

    /**
     * Retreive all drawing features from the database.
     *
     * @return  All drawing features from the db
     */
    public static synchronized List<DrawingSLDStyledFeature> getAllFeatures() {
        final List<DrawingSLDStyledFeature> features = new ArrayList<DrawingSLDStyledFeature>();

        ConnectionWrapper cw = null;
        StatementWrapper st = null;

        try {
            cw = H2FeatureServiceFactory.getDBConnection(null);
            st = H2FeatureServiceFactory.createStatement(cw);
            final ResultSet rs = st.executeQuery(SELECT_ALL_FEATURES);

            while (rs.next()) {
                final int id = rs.getInt(1);
                final Geometry geom = (Geometry)rs.getObject(2);
                final String type = rs.getString(3);
                final String text = rs.getString(4);
                final boolean autoscale = rs.getBoolean(5);
                final boolean halo = rs.getBoolean(6);
                final int fontsize = rs.getInt(7);
                final String sld = rs.getString(8);
                geom.setSRID(CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs()));
                final DrawingSLDStyledFeature feature = new DrawingSLDStyledFeature();
                feature.setGeometry(geom);

                feature.setId(id);
                if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                    feature.setText(text);
                }
                feature.setGeometryType(AbstractNewFeature.geomTypes.valueOf(type));
                feature.setEditable(true);

                if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                    feature.setAutoScale(autoscale);
                    if (halo) {
                        feature.setPrimaryAnnotationHalo(Color.WHITE);
                    }
                    feature.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, fontsize));
                }
                feature.setSLDStyles(
                    AppBroker.getInstance().getDrawingStylesBySld(sld).get("default"));
                feature.setProperty("sld", sld);

                features.add(feature);
            }
        } catch (Exception e) {
            LOG.error("Error while retreiving all features from the database: ", e);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing statement", ex);
                }
            }
            if (cw != null) {
                try {
                    cw.close();
                } catch (SQLException ex) {
                    LOG.error("Error while closing connection", ex);
                }
            }
        }

        return features;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final Thread[] threads = new Thread[10];

        for (int i = 1; i < 10; ++i) {
            threads[i] = new Thread(new Tester());
            threads[i].setDaemon(false);
        }

        threads[0] = new Thread(new TesterDelete());
        threads[0].setDaemon(false);
        for (int i = 0; i < 10; ++i) {
            threads[i].start();
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class Tester implements Runnable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            ConnectionWrapper cw = null;
            StatementWrapper st = null;
            final GeomFromWktConverter converter = new GeomFromWktConverter();

            try {
                cw = H2FeatureServiceFactory.getDBConnection(null);
                st = H2FeatureServiceFactory.createStatement(cw);

                for (int i = 0; i < 500; ++i) {
                    final String geomAsTExt =
                        "POLYGON((33554.2324 500943.2343, 33584.2324 500943.2343, 33584.2324 500953.2343, 33554.2324 500943.2343))";
                    final String type = "POLYGON";
                    final String text = null;
                    final boolean autoscale = false;
                    final boolean halo = false;
                    final Geometry geom = converter.convertForward(
                            geomAsTExt,
                            CismapBroker.getInstance().getDefaultCrs());
                    geom.setSRID(CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs()));
                    final DrawingFeature feature = new DrawingFeature(geom);

                    if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                        feature.setName(text);
                    }
                    feature.setGeometryType(AbstractNewFeature.geomTypes.valueOf(type));
                    feature.setEditable(true);

                    if (AbstractNewFeature.geomTypes.valueOf(type).equals(AbstractNewFeature.geomTypes.TEXT)) {
                        final int fontsize = 12;

                        feature.setAutoScale(autoscale);
                        if (halo) {
                            feature.setPrimaryAnnotationHalo(Color.WHITE);
                        }
                        feature.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, fontsize));
                    }

                    addFeatureToDb(feature);
                }
            } catch (Exception e) {
                LOG.error("Error while inserting features into the db: ", e);
            } finally {
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while closing statement", ex);
                    }
                }
                if (cw != null) {
                    try {
                        cw.close();
                    } catch (SQLException ex) {
                        LOG.error("Error while closing connection", ex);
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
    private static final class TesterDelete implements Runnable {

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            executeUpdate("delete from \"" + DRAWING_TABLE_NAME + "\"");
        }
    }
}
