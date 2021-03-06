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

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SelectionMethodInterface.class)
public class AddToSelectedFeaturesSelectionMethod implements SelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void executeMethod(final List<FeatureServiceFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final List<Feature> toBeSelected = new ArrayList<Feature>();

        for (final FeatureServiceFeature feature : features) {
            toBeSelected.add(feature);
        }

        SelectionManager.getInstance().addSelectedFeatures(toBeSelected);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                IntersectSourceSpatialMethod.class,
                "AddToSelectedFeaturesSelectionMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 2;
    }
}
