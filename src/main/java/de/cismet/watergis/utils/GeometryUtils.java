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
package de.cismet.watergis.utils;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.precision.GeometryPrecisionReducer;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import de.cismet.cids.custom.watergis.server.search.Buffer;
import de.cismet.cids.custom.watergis.server.search.MakeValid;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.commons.concurrency.CismetExecutors;

/**
 * Contains some useful geometry processing operations.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GeometryUtils.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Let the db executes a makeValid on the given geometry.
     *
     * @param   g  the geometry to make valid
     *
     * @return  the (hopefully) valid geometry
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static Geometry makeValid(final Geometry g) throws Exception {
        Geometry validGeometry = null;
        final CidsServerSearch search = new MakeValid(g);

        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);

        if ((attributes != null) && !attributes.isEmpty()) {
            if (!attributes.get(0).isEmpty() && (attributes.get(0).get(0) instanceof byte[])) {
                final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                        CismapBroker.getInstance().getDefaultCrsAlias());
                final WKBReader wkbReader = new WKBReader(geomFactory);
                validGeometry = wkbReader.read((byte[])attributes.get(0).get(0));
            }
        }

        return validGeometry;
    }

    /**
     * Splits the given geometry at the given line.
     *
     * @param   geom       the geometry to split
     * @param   splitLine  the geometry will be splitted at this linestring
     *
     * @return  An array with the two resulted geometries
     */
    public static Geometry[] splitGeom(final Geometry geom, final LineString splitLine) {
        if (geom.getGeometryType().equalsIgnoreCase("LINESTRING")) {
            final Geometry[] result = new Geometry[2];
            final Geometry intersectionPoint = geom.intersection(splitLine);
            final LengthIndexedLine lil = new LengthIndexedLine(geom);
            final double value = lil.indexOf(intersectionPoint.getCoordinate());

            result[0] = lil.extractLine(0, value);
            result[1] = lil.extractLine(value, geom.getLength());

            return result;
        } else if (geom.getGeometryType().equalsIgnoreCase("POLYGON")
                    || geom.getGeometryType().equalsIgnoreCase("MULTIPOLYGON")) {
            return splitPolygon(geom, splitLine);
//            if (geom.getGeometryType().equalsIgnoreCase("MULTIPOLYGON")) {
//                return splitPolygon((Polygon)StaticGeometryFunctions.toSimpleGeometry(geom), splitLine);
//            } else {
//                return splitPolygon((Polygon)geom, splitLine);
//            }
        } else {
            return null;
        }
    }

    /**
     * The algorithm was taken from http://wiki.postgis.org/support/wiki/index.php?SplitPolygonWithLineString.
     *
     * @param   sourceGeom  a polygon
     * @param   splitter    the linestring to split the polygon
     *
     * @return  An array with the two resulted geometries
     */
    public static Geometry[] splitPolygon(final Geometry sourceGeom, final LineString splitter) {
        final Geometry geom = sourceGeom.getBoundary();

        final GeometryFactory geomFactory = sourceGeom.getFactory();
        final List lines = new ArrayList();

        for (int i = 0; i < geom.getNumGeometries(); i++) {
            lines.add(geom.getGeometryN(i));
        }
        lines.add(splitter);

        final Geometry mls = geomFactory.createMultiLineString((LineString[])lines.toArray(new LineString[0]));
        final Point mlsPt = geomFactory.createPoint(mls.getCoordinate());
        final Geometry nodedLines = mls.union(mlsPt);
        final Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(nodedLines);

        final Collection polygons = polygonizer.getPolygons();
        final ArrayList polys = new ArrayList();

        for (final Iterator iter = polygons.iterator(); iter.hasNext();) {
            // checks if given polygon is contained inside source pg, otherwise exclude it
            final Polygon pg = (Polygon)iter.next();
            if (sourceGeom.contains(pg.getInteriorPoint())) {
                polys.add(pg.clone());
            }
        }

        return (Geometry[])polys.toArray(new Geometry[0]);
    }

    /**
     * Unions the geometries of the given features.
     *
     * @param   sourceFeatures  the features the union
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionFeatureEnvelopes(final List<FeatureServiceFeature> sourceFeatures) {
        boolean first = true;
        int srid = -1;
        final List<Geometry> geomList = new ArrayList<Geometry>();

        for (final Feature f : sourceFeatures) {
            Geometry g = f.getGeometry();

            if (g != null) {
                g = g.getEnvelope();

                if (first) {
                    srid = g.getSRID();
                    first = false;
                } else {
                    if (g.getSRID() != srid) {
                        g = CrsTransformer.transformToGivenCrs(g, CrsTransformer.createCrsFromSrid(srid));
                    }
                }

                geomList.add(g);
            }
        }

        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
        Geometry union = factory.buildGeometry(geomList);

        if (union instanceof GeometryCollection) {
            union = ((GeometryCollection)union).union();
        }

        return union;
    }

    /**
     * Unions the geometries of the given features.
     *
     * @param   sourceFeatures  the features the union
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionFeatureGeometries(final List<FeatureServiceFeature> sourceFeatures) {
        final List<Geometry> geomList = new ArrayList<Geometry>();
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FIXED);

        for (final FeatureServiceFeature fsf : sourceFeatures) {
            final Geometry geom = fsf.getGeometry();

            if (geom != null) {
                geomList.add(GeometryPrecisionReducer.reduce(geom, pm));
            }
        }

        if (geomList.size() == 1) {
            return geomList.get(0);
        }
        if (geomList.isEmpty()) {
            return null;
        }

        final Geometry g = unionGeometries2(geomList);
        return g;
    }

    /**
     * An union method that is faster as the iterativ approach with the union() method.
     *
     * @param   geom  the list with the geometries to union
     * @param   from  the index of the first element that should be used for the union operation
     * @param   to    the index of the last element that should be used for the union operation
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionGeometries(final List<Geometry> geom, final int from, final int to) {
        if (to == from) {
            return geom.get(from);
        } else {
            final Geometry g1 = unionGeometries(geom, from, from + ((to - from) / 2));
            final Geometry g2 = unionGeometries(geom, from + ((to - from) / 2) + 1, to);

            System.out.print("valid: " + g1.isValid());
            System.out.print("valid: " + g2.isValid());
            return g1.union(g2);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geomList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionGeometries2(final List<Geometry> geomList) {
        if (geomList.isEmpty()) {
            return null;
        }
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                geomList.get(0).getSRID());
        Geometry geom = factory.buildGeometry(geomList);

        if (geom instanceof GeometryCollection) {
            geom = ((GeometryCollection)geom).union();
        }
        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean isLineOrPoint(final Geometry geom) {
        return ((geom instanceof LineString) || (geom instanceof Point) || (geom instanceof MultiLineString)
                        || (geom instanceof MultiPoint));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geomList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionPolygons(final List<Geometry> geomList) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                geomList.get(0).getSRID());
        final List<Polygon> pList = new ArrayList<Polygon>();

        for (final Geometry g : geomList) {
            if (g instanceof Polygon) {
                if (g.isValid()) {
                    pList.add((Polygon)g);
                } else {
                    LOG.error("is not valid: " + g);
                }
            } else if (g instanceof MultiPolygon) {
                for (int i = 0; i < g.getNumGeometries(); ++i) {
                    if (g.getGeometryN(i).isValid()) {
                        pList.add((Polygon)g.getGeometryN(i));
                    } else {
                        LOG.error("is not valid: " + g.getGeometryN(i));
                    }
                }
            } else {
                LOG.error("Not a Polygon: " + g);
            }
        }

        final GeometryCollection polygonCollection = factory.createGeometryCollection(pList.toArray(
                    new Polygon[pList.size()]));

        return polygonCollection.buffer(0);
    }

    /**
     * Creates a dummy geometry of the given type.
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  a dummy geometry of the given type
     */
    public static Geometry createDummyGeometry(final String geometryType) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), -1);

        if (geometryType.equalsIgnoreCase("Point")) {
            return factory.createPoint(new Coordinate(1, 2));
        } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
            return factory.createMultiPoint(new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
        } else if (geometryType.equalsIgnoreCase("LineString")) {
            return factory.createLineString(new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
        } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
            final LineString ls1 = factory.createLineString(
                    new Coordinate[] { new Coordinate(1, 2), new Coordinate(2, 2) });
            final LineString ls2 = factory.createLineString(
                    new Coordinate[] { new Coordinate(3, 3), new Coordinate(4, 3) });

            return factory.createMultiLineString(new LineString[] { ls1, ls2 });
        } else if (geometryType.equalsIgnoreCase("Polygon")) {
            return factory.createPolygon(
                    new Coordinate[] {
                        new Coordinate(1, 2),
                        new Coordinate(2, 2),
                        new Coordinate(2, 3),
                        new Coordinate(1, 3),
                        new Coordinate(1, 2)
                    });
        } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
            final Polygon p = factory.createPolygon(
                    new Coordinate[] {
                        new Coordinate(1, 2),
                        new Coordinate(2, 2),
                        new Coordinate(2, 3),
                        new Coordinate(1, 3),
                        new Coordinate(1, 2)
                    });

            return factory.createMultiPolygon(new Polygon[] { p });
        }

        return null;
    }

    /**
     * Determines the shape geometry type. In a shape file, every geometry type is described by one byte.
     *
     * @param   geometryType  DOCUMENT ME!
     *
     * @return  the shape geometry type.
     */
    public static byte getShpGeometryType(final String geometryType) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), -1);

        if (geometryType.equalsIgnoreCase("Point")) {
            return 1;
        } else if (geometryType.equalsIgnoreCase("MultiPoint")) {
            return 8;
        } else if (geometryType.equalsIgnoreCase("LineString")) {
            return 3;
        } else if (geometryType.equalsIgnoreCase("MultiLineString")) {
            return 3;
        } else if (geometryType.equalsIgnoreCase("Polygon")) {
            return 5;
        } else if (geometryType.equalsIgnoreCase("MultiPolygon")) {
            return 5;
        }

        return 0;
    }

    /**
     * Removes all content from the given shp or shx file, so that the file contains only the header.
     *
     * @param   fileName    the file to clear
     * @param   shpGeoType  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void clearShpOrShxFile(final String fileName, final int shpGeoType) throws IOException {
        final File origFile = new File(fileName);

        if (origFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            origFile.delete();

            try {
                is = GeometryUtils.class.getResourceAsStream(
                        "/de/cismet/watergis/gui/actions/emptyShapeTemplate.shp");
                os = new FileOutputStream(new File(fileName));
                int b;
                int index = 0;

                while ((b = is.read()) != -1) {
                    if (index == 32) {
                        os.write(shpGeoType);
                    } else {
                        os.write(b);
                    }
                    ++index;
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + origFile.getAbsolutePath(), e);
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + fileName, e);
                }
            }
        }
    }

    /**
     * Removes all content from the given shp or shx file, so that the file contains only the header.
     *
     * @param   fileName  the file to clear
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void clearDbfFile(final String fileName) throws IOException {
        File origFile = new File(fileName);

        if (origFile.exists()) {
            InputStream is = null;
            OutputStream os = null;
            origFile.renameTo(new File(fileName + ".backup"));
            origFile = new File(fileName + ".backup");

            try {
                is = new FileInputStream(origFile);
                os = new FileOutputStream(new File(fileName));
                int content;
                int byteCounter = 0;
                int tmpLength = 0;
                int length = 1000;

                while ((content = is.read()) != -1) {
                    ++byteCounter;
                    if (byteCounter == 5) {
                        // set the object count to 0
                        os.write(0x0);
                        continue;
                    }
                    if (byteCounter == 9) {
                        // byte 9/10 contain the position of first data record
                        tmpLength = content;
                    }
                    if (byteCounter == 10) {
                        tmpLength += content
                                    << 8;
                        length = tmpLength;
                    }
                    os.write(content);
                    if ((byteCounter >= (length - 1)) && (content == 0xd)) { // 0xd is the last byte of
                                                                             // the header
                        break;
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + origFile.getAbsolutePath(), e);
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (Exception e) {
                    LOG.error("Cannot close " + fileName, e);
                }
                origFile.delete();
            }
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class UnionHelper {

        //~ Static fields/initializers -----------------------------------------

        private static final int CORES = Runtime.getRuntime().availableProcessors();
        private static final ExecutorService executor = CismetExecutors.newFixedThreadPool(CORES);

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   geomList  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  InterruptedException  DOCUMENT ME!
         */
        public static Geometry union(final List<Geometry> geomList) throws InterruptedException {
            final BlockingQueue<Geometry> resultQueue = new LinkedBlockingQueue<Geometry>();
//            final List<Geometry> results = Collections.synchronizedList( new ArrayList<Geometry>() );
            int countPerCore = geomList.size() / CORES;
            int i = 0;
            int n = 0;

            if (countPerCore < 1) {
                countPerCore = 1;
            }

            while (i < (geomList.size() - 1)) {
                final List<Geometry> gl = new ArrayList<Geometry>();
                ++n;

                for (; (i < (n * countPerCore)) && (i < (geomList.size())); ++i) {
                    gl.add(geomList.get(i));
                }

                executor.submit(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                final Geometry g = unionGeometries(gl, 0, gl.size() - 1);
                                resultQueue.put(g);
                            } catch (InterruptedException e) {
                            } catch (Exception e) {
                                LOG.error("Error while union geometries", e);
                            }
                        }
                    });
            }

            if (geomList.isEmpty()) {
                return null;
            }
            int resultCount = 0;
            final Geometry lastResult = null;
            Geometry resultGeom = null;
            final List<Geometry> gList = new ArrayList<Geometry>();

            do {
                resultGeom = resultQueue.take();
                gList.add(resultGeom);
                ++resultCount;
//
//                if (lastResult != null) {
//                    final Geometry g1 = lastResult;
//                    final Geometry g2 = resultGeom;
//                    ++n;
//
//                    executor.submit(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            try {
//                                List<Geometry> g = new ArrayList<Geometry>();
//                                g.add(g1);
//                                g.add(g2);
//                                resultQueue.put(unionGeometries(g, 0, g.size() - 1));
//                            } catch (InterruptedException e) {
//
//                            } catch (Exception e) {
//                                LOG.error("Error while union geometries", e);
//                            }
//                        }
//                    });
//
//                    lastResult = null;
//                } else {
//                    lastResult = resultGeom;
//                }
            } while (resultCount != n);

            return unionGeometries(gList, 0, geomList.size() - 1);
        }

        /**
         * public static Geometry unionGeometries(final List<Geometry> geom, final int from, final int to) { if (to ==
         * from) { return geom.get(from); } else { final Geometry g1 = unionGeometries(geom, from, from + ((to - from) /
         * 2)); final Geometry g2 = unionGeometries(geom, from + ((to - from) / 2) + 1, to); return g1.union(g2); } }.
         *
         * @param   geomList  DOCUMENT ME!
         * @param   from      DOCUMENT ME!
         * @param   to        DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public static Geometry unionGeometries(final List<Geometry> geomList, final int from, final int to) {
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    geomList.get(0).getSRID());
            Geometry geom = factory.buildGeometry(geomList);

            if (geom instanceof GeometryCollection) {
                geom = ((GeometryCollection)geom).union();
            }

            return geom;
        }
    }
}
