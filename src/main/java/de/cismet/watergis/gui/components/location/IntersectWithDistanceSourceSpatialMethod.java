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
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SpatialSelectionMethodInterface.class)
public class IntersectWithDistanceSourceSpatialMethod extends IntersectSourceSpatialMethod {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isDistanceRequired() {
        return true;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                IntersectWithDistanceSourceSpatialMethod.class,
                "IntersectWithDistanceSourceSpatialMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 2;
    }
}
