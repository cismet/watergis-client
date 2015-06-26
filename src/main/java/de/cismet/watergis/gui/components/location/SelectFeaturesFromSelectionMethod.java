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
import de.cismet.cismap.commons.util.SelectionManager;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = SelectionMethodInterface.class)
public class SelectFeaturesFromSelectionMethod implements SelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void executeMethod(final List<FeatureServiceFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final ArrayList list = new ArrayList();
        list.addAll(features);

        SelectionManager.getInstance().setSelectedFeatures(list);
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(
                IntersectSourceSpatialMethod.class,
                "SelectFeaturesFromSelectionMethod.toString");
    }

    @Override
    public Integer getOrderId() {
        return 1;
    }
}
