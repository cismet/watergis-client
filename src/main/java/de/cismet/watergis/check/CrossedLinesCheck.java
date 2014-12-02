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
package de.cismet.watergis.check;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

//import de.cismet.cismap.commons.gui.piccolo.eventlistener.GeometryCheckInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
//@org.openide.util.lookup.ServiceProvider(service = GeometryCheckInterface.class)
public class CrossedLinesCheck {
}
//implements GeometryCheckInterface {
//
//    //~ Methods ----------------------------------------------------------------
//
//    @Override
//    public boolean check(final Geometry g,
//            final Coordinate lastCoordinate,
//            final boolean ignoreLastGeometryCoordinate) {
//        if (g instanceof LineString) {
//            if (!g.isSimple()) {
//                return false;
//            }
//        } else if (g instanceof Polygon) {
//            final Polygon p = (Polygon)g;
//            boolean valid = check(p.getExteriorRing(), lastCoordinate, ignoreLastGeometryCoordinate);
//
//            if (!valid) {
//                return false;
//            }
//
//            for (int i = 0; i < p.getNumInteriorRing(); ++i) {
//                valid = check(p.getInteriorRingN(i), lastCoordinate, ignoreLastGeometryCoordinate);
//
//                if (!valid) {
//                    return false;
//                }
//            }
//        } else if (g.getNumGeometries() > 1) {
//            for (int i = 0; i < g.getNumGeometries(); ++i) {
//                final boolean valid = check(g.getGeometryN(i), lastCoordinate, ignoreLastGeometryCoordinate);
//
//                if (!valid) {
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }
//
//    @Override
//    public String[] getErrorText() {
//        return new String[] { "Linien dürfen sich", "nicht schneiden" };
//    }
//}
