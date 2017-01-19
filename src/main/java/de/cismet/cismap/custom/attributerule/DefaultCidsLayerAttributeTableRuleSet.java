/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.custom.attributerule;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;
import java.util.HashMap;

/**
 *
 * @author therter
 */
public class DefaultCidsLayerAttributeTableRuleSet extends DefaultAttributeTableRuleSet {

    @Override
    public FeatureServiceFeature cloneFeature(FeatureServiceFeature feature) {
        final DefaultFeatureServiceFeature newFeature = (DefaultFeatureServiceFeature)feature
                    .getLayerProperties().getFeatureService().getFeatureFactory().createNewFeature();

        final HashMap<String, Object> properties = feature.getProperties();
        final CidsBean bean = ((CidsLayerFeature)feature).getBean();

        for (final String propertyKey : properties.keySet()) {
            if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(feature.getIdExpression())) {
                newFeature.setProperty(propertyKey, bean.getProperty(propertyKey));
            }
        }

        return newFeature;
    }
}
