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
 * This is the reverse method of the ContainSourceSpatialMethod.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class WithinSourceSpatialMethod implements SpatialSelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean featureGeometryFulfilsRequirements(final Geometry source,
            final Geometry featureGeometry,
            final double distance) {
        if ((featureGeometry != null) && (source != null)) {
            if (distance == 0.0) {
                return featureGeometry.coveredBy(source);
            } else {
                return featureGeometry.coveredBy(source.buffer(distance));
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
                WithinSourceSpatialMethod.class,
                "WithinSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 5;
    }
}
