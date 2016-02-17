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
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SelectionMethodInterface.class)
public class SelectFromSelectedFeaturesSelectionMethod implements SelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void executeMethod(final List<FeatureServiceFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final List<Feature> toBeUnSelected = new ArrayList<Feature>();

        for (final Feature feature : SelectionManager.getInstance().getSelectedFeatures()) {
            if (feature instanceof FeatureServiceFeature) {
                final FeatureServiceFeature f = (FeatureServiceFeature)feature;
                if (containedInService(target, f)) {
                    if (!features.contains(f)) {
                        toBeUnSelected.add(f);
                    }
                }
            }
        }

        SelectionManager.getInstance().removeSelectedFeatures(toBeUnSelected);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   target   DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean containedInService(final List<AbstractFeatureService> target, final FeatureServiceFeature feature) {
        for (final AbstractFeatureService service : target) {
            if (feature.getLayerProperties() == service.getLayerProperties()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                IntersectSourceSpatialMethod.class,
                "SelectFromSelectedFeaturesSelectionMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 4;
    }
}
