/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.custom.attributerule;

import java.util.HashMap;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.DefaultAttributeTableRuleSet;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultCidsLayerAttributeTableRuleSet extends DefaultAttributeTableRuleSet {

    //~ Methods ----------------------------------------------------------------

    @Override
    public FeatureServiceFeature cloneFeature(final FeatureServiceFeature feature) {
        final DefaultFeatureServiceFeature newFeature = (DefaultFeatureServiceFeature)feature
                    .getLayerProperties().getFeatureService().getFeatureFactory().createNewFeature();

        final HashMap<String, Object> properties = feature.getProperties();
        final CidsBean bean = ((CidsLayerFeature)feature).getBean();

        for (final String propertyKey : properties.keySet()) {
            if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(feature.getIdExpression())
                        && !propertyKey.equalsIgnoreCase("obj_nr")) {
                if (bean.getProperty(propertyKey) == null) {
                    newFeature.setProperty(propertyKey, feature.getProperty(propertyKey));
                } else {
                    if ((bean.getProperty(propertyKey) instanceof CidsBean)
                                && bean.getProperty(propertyKey).getClass().getName().equals(
                                    "de.cismet.cids.dynamics.Geom")) {
                        newFeature.setProperty(propertyKey, feature.getGeometry());
                    } else {
                        newFeature.setProperty(propertyKey, bean.getProperty(propertyKey));
                    }
                }
            }
        }

        newFeature.setGeometry(feature.getGeometry());

        return newFeature;
    }
}
