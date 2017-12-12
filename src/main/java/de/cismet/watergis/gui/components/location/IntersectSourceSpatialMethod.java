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
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class IntersectSourceSpatialMethod implements SpatialSelectionMethodInterface {

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
                    return source.intersects(featureGeometry.buffer(0.001));
                } else {
                    return source.intersects(featureGeometry);
                }
            } else {
                return featureGeometry.isWithinDistance(source, distance);
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
                IntersectSourceSpatialMethod.class,
                "IntersectSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 1;
    }

    @Override
    public boolean isUsedForGeoprocessing() {
        return true;
    }
}
