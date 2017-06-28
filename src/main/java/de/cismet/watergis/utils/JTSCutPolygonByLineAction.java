/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * JTSCutPolygonAction.java
 *
 * Created on 18 aout 2005, 16:43
 */
package de.cismet.watergis.utils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Class to cut a given polygon or MultiPG (the first one in the geometries) with the given linstring (the second
 * geometry) The geometries Vector MUST contains 1 polygon or multipolygon and 1 linestring. todo: use types collections
 *
 * @author   Nicolas Ribot
 * @version  $Revision$, $Date$
 */
public class JTSCutPolygonByLineAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * The constructor.
     */
    public JTSCutPolygonByLineAction() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * returns the JTS Multipolygon geometry corresponding to the cutting of the first polygon contained into the
     * geometries Vector with the second object that must be a linestring.
     *
     * <p>a result will be returned iff it is composed of polygons and multipolygons</p>
     *
     * @param   sourceGeom  DOCUMENT ME!
     * @param   splitter    DOCUMENT ME!
     *
     * @return  A MultiPolygon resulting from the spatial analysis operation, or null if geometries vector does not
     *          contain 2 polygons, or if cutting operations produced something else than polygons or multipolygons
     */
    public Geometry getResult(final Geometry sourceGeom, final Geometry splitter) {
        final Geometry geom = sourceGeom.getBoundary();
        Geometry result = null;

        // tests if geom and cutter intersect.
        if (!geom.intersects(splitter)) {
            return geom;
        }

        // split-by-line algorithm is taken from PostGIS wiki:
        // http://wiki.postgis.org/support/wiki/index.php?SplitPolygonWithLineString
        // basically, it consists of extracting linear rings, ,unioning them with given splitter line,
        // noding the linestring together and
        // polygonizing the result and removing from the result polygons that are holes.
        // build collection of lines in an heavy way, must be easier:

        final GeometryFactory geomFactory = new GeometryFactory();
        // Vector<Geometry> lines = new Vector();
        final Vector lines = new Vector();
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            lines.add(geom.getGeometryN(i));
        }
        for (int i = 0; i < splitter.getNumGeometries(); i++) {
            lines.add(splitter.getGeometryN(i));
        }
        final Geometry mls = geomFactory.createMultiLineString((LineString[])lines.toArray(new LineString[0]));
        final Point mlsPt = geomFactory.createPoint(mls.getCoordinate());
        final Geometry nodedLines = mls.union(mlsPt);
        final Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(nodedLines);

        final Collection polygons = polygonizer.getPolygons();
        // ArrayList<Polygon> polys = new ArrayList<Polygon>();
        final ArrayList polys = new ArrayList();
        int i = 0;
        for (final Iterator iter = polygons.iterator(); iter.hasNext();) {
            // checks if given polygon is contained inside source pg, otherwise exclude it
            final Polygon pg = (Polygon)iter.next();
            if (sourceGeom.contains(pg.getInteriorPoint())) {
                polys.add(pg);
            } else {
                // logger.info("removing polygon: " + pg.toString());
            }
        }
        result = geomFactory.createMultiPolygon((Polygon[])polys.toArray(new Polygon[0]));

        return result;
    }
}
