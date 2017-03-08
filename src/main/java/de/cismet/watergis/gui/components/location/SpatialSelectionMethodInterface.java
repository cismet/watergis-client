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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface SpatialSelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   source           DOCUMENT ME!
     * @param   featureGeometry  DOCUMENT ME!
     * @param   distance         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean featureGeometryFulfilsRequirements(Geometry source, Geometry featureGeometry, double distance);
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isDistanceRequired();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Integer getOrderId();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    boolean isUsedForGeoprocessing();
}
