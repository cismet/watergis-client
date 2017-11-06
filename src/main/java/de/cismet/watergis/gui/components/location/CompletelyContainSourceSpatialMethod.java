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
package de.cismet.watergis.gui.components.location;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.NbBundle;

import de.cismet.watergis.utils.GeometryUtils;

/**
 * This class checks, if the target geometry completely contains the source geometry.
 *
 * <p>The contains predicate has the following equivalent definitions:</p>
 *
 * <ul>
 *   <li>Every point of the other geometry is a point of this geometry, and the interiors of the two geometries have at
 *     least one point in common.</li>
 *   <li>The DE-9IM Intersection Matrix for the two geometries is T*****FF* g.within(this) (contains is the inverse of
 *     within)</li>
 *   <li>An implication of the definition is that "Polygons do not contain their boundary". In other words, if a
 *     geometry G is a subset of the points in the boundary of a polygon P, P.contains(G) = false</li>
 * </ul>
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class CompletelyContainSourceSpatialMethod implements SpatialSelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean featureGeometryFulfilsRequirements(final Geometry source,
            final Geometry featureGeometry,
            final double distance) {
        final boolean onlyPointsOrLines = GeometryUtils.isLineOrPoint(source)
                    && GeometryUtils.isLineOrPoint(featureGeometry);

        if ((featureGeometry != null) && (source != null)) {
            if (distance == 0.0) {
                if (onlyPointsOrLines) {
                    return featureGeometry.buffer(0.001).contains(source);
                } else {
                    return featureGeometry.contains(source);
                }
            } else {
                return featureGeometry.contains(source.buffer(distance));
            }
        }

        return false;
    }

    @Override
    public boolean isDistanceRequired() {
        return false;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                CompletelyContainSourceSpatialMethod.class,
                "CompletelyContainSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 4;
    }

    @Override
    public boolean isUsedForGeoprocessing() {
        return false;
    }
}
