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
public class SelectFeaturesFromSelectionMethod implements SelectionMethodInterface {

    //~ Methods ----------------------------------------------------------------

    @Override
    public void executeMethod(final List<FeatureServiceFeature> features,
            final AbstractFeatureService source,
            final List<AbstractFeatureService> target) {
        final ArrayList list = new ArrayList();
        list.addAll(features);

        for (final AbstractFeatureService targetService : target) {
            SelectionManager.getInstance()
                    .removeSelectedFeatures(SelectionManager.getInstance().getSelectedFeatures(targetService));
        }

        SelectionManager.getInstance().addSelectedFeatures(list);
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
