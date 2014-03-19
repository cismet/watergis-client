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

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    public void executeMethod(final List<PFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final MappingComponent map = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)map.getInputEventListener().get(MappingComponent.SELECT);
        final List<Feature> toBeSelected = new ArrayList<Feature>();

        for (final PFeature feature : features) {
            if (!feature.isSelected()) {
                feature.setSelected(true);
                sl.addSelectedFeature(feature);
                toBeSelected.add(feature.getFeature());
            }
        }

        ((DefaultFeatureCollection)map.getFeatureCollection()).select(toBeSelected);
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
                "AddToSelectedFeaturesSelectionMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 2;
    }
}
