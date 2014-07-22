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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

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
}
