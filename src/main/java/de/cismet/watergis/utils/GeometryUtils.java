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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GeometryUtils {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry toMultiGeometry(final Geometry g) {
        final GeometryFactory factory = g.getFactory();

        if (g.getGeometryType().equalsIgnoreCase("point")) {
            return factory.createMultiPoint(new Point[] { (Point)g });
        } else if (g.getGeometryType().equalsIgnoreCase("linestring")) {
            return factory.createMultiLineString(new LineString[] { (LineString)g });
        } else if (g.getGeometryType().equalsIgnoreCase("polygon")) {
            return factory.createMultiPolygon(new Polygon[] { (Polygon)g });
        }

        return g;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom       DOCUMENT ME!
     * @param   splitLine  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
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
                return splitPolygon((Polygon)toSimpleGeometry(geom), splitLine);
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
     * @return  DOCUMENT ME!
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
     * DOCUMENT ME!
     *
     * @param   sourceFeatures  DOCUMENT ME!
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

        return unionGeometries(geomList);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geomList  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry unionGeometries(final List<Geometry> geomList) {
        Geometry geom = null;

        if (geomList.size() > 0) {
            final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    geomList.get(0).getSRID());
            geom = factory.buildGeometry(geomList);

            if (geom instanceof GeometryCollection) {
                geom = ((GeometryCollection)geom).union();
            }
        }

        return geom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   g  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry toSimpleGeometry(final Geometry g) {
        if (g.getGeometryType().equalsIgnoreCase("multipoint")) {
            return g.getGeometryN(0);
        } else if (g.getGeometryType().equalsIgnoreCase("multilinestring")) {
            return g.getGeometryN(0);
        } else if (g.getGeometryType().equalsIgnoreCase("multipolygon")) {
            return g.getGeometryN(0);
        }

        return g;
    }
}
