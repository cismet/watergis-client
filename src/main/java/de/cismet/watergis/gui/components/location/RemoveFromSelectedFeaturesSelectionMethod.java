/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui.components.location;

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

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SelectionMethodInterface.class)
public class RemoveFromSelectedFeaturesSelectionMethod implements SelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void executeMethod(final List<FeatureServiceFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final List<Feature> staySelected = new ArrayList<Feature>();

        for (final Feature feature : SelectionManager.getInstance().getSelectedFeatures()) {
            if (feature instanceof FeatureServiceFeature) {
                if (containedInService(target, (FeatureServiceFeature)feature)) {
                    if (!features.contains(feature)) {
                        staySelected.add(feature);
                    }
                } else {
                    staySelected.add(feature);
                }
            } else {
                staySelected.add(feature);
            }
        }

        for (final AbstractFeatureService targetService : target) {
            SelectionManager.getInstance()
                    .removeSelectedFeatures(SelectionManager.getInstance().getSelectedFeatures(targetService));
        }

        SelectionManager.getInstance().addSelectedFeatures(staySelected);
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
                "RemoveFromSelectedFeaturesSelectionMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 3;
    }
}
