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
package de.cismet.watergis.gui.actions.split;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import java.util.List;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface FeatureSplitter {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   masterFeature  DOCUMENT ME!
     * @param   splitLine      childFeature DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Feature[] split(Feature masterFeature, LineString splitLine);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    List<FeatureServiceFeature> getAdditionalFeaturesToSave();

    /**
     * DOCUMENT ME!
     */
    void undo();

    /**
     * DOCUMENT ME!
     */
    void unlockObjects();
}
