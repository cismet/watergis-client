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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.cismet.cismap.commons.features.FeatureServiceFeature;

import de.cismet.math.geometry.StaticGeometryFunctions;

/**
 * Contains some useful geometry processing operations.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryUtils {

    //~ Methods ----------------------------------------------------------------

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
            if (geom.getGeometryType().equalsIgnoreCase("MULTIPOLYGON")) {
                return splitPolygon((Polygon)StaticGeometryFunctions.toSimpleGeometry(geom), splitLine);
            } else {
                return splitPolygon((Polygon)geom, splitLine);
            }
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
    public static Geometry[] splitPolygon(final Polygon sourceGeom, final LineString splitter) {
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
                polys.add(pg);
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
    public static Geometry unionFeatureGeometries(final List<FeatureServiceFeature> sourceFeatures) {
        final List<Geometry> geomList = new ArrayList<Geometry>();

        for (final FeatureServiceFeature fsf : sourceFeatures) {
            final Geometry geom = fsf.getGeometry();

            if (geom != null) {
                geomList.add(geom);
            }
        }

        return unionGeometries(geomList, 0, geomList.size() - 1);
    }

    /**
     * An union method that is faster the the iterativ approach with the union() method.
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

            return g1.union(g2);
        }
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
}
