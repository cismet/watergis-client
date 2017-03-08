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

/**
 * Unlike CompletelyContainSourceSpatialMethod, this method does not distinguish between points in the boundary and in
 * the interior of geometries.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class ContainSourceSpatialMethod implements SpatialSelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean featureGeometryFulfilsRequirements(final Geometry source,
            final Geometry featureGeometry,
            final double distance) {
        if ((featureGeometry != null) && (source != null)) {
            if (distance == 0.0) {
                // The difference between covers and contains - covers is a more inclusive relation. In particular,
                // unlike contains it does not distinguish between points in the boundary and in the interior of
                // geometries.
                return featureGeometry.covers(source);
            } else {
                return featureGeometry.covers(source.buffer(distance));
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
                ContainSourceSpatialMethod.class,
                "ContainSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 3;
    }

    @Override
    public boolean isUsedForGeoprocessing() {
        return false;
    }
}
