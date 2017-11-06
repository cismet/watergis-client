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
 * This is the reverse method of the CompletelyContainSourceSpatialMethod.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class CompletelyWithinSourceSpatialMethod implements SpatialSelectionMethodInterface {

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
                    return featureGeometry.within(source.buffer(0.001));
                } else {
                    return featureGeometry.within(source);
                }
            } else {
                return featureGeometry.within(source.buffer(distance));
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
                CompletelyWithinSourceSpatialMethod.class,
                "CompletelyWithinSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 6;
    }

    @Override
    public boolean isUsedForGeoprocessing() {
        return false;
    }
}
