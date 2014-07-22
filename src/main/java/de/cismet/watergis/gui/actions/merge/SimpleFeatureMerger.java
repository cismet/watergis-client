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
package de.cismet.watergis.gui.actions.merge;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cismap.commons.features.CloneableFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureMerger implements FeatureMerger {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Feature merge(final Feature masterFeature, final Feature childFeature) {
        final Geometry g = masterFeature.getGeometry();
        final Geometry mergedGeom = g.union(childFeature.getGeometry());

        masterFeature.setGeometry(mergedGeom);

        return masterFeature;
    }
}
