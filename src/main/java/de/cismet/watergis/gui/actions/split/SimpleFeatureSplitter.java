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

import java.util.HashMap;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.watergis.utils.GeometryUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SimpleFeatureSplitter implements FeatureSplitter {

    //~ Methods ----------------------------------------------------------------

    @Override
    public Feature split(final Feature masterFeature, final LineString splitLine) {
        if (masterFeature instanceof DefaultFeatureServiceFeature) {
            final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)masterFeature;
            final boolean isMulti = masterFeature.getGeometry().getGeometryType().toLowerCase().startsWith("multi");

            final Geometry geom = dfsf.getGeometry();

            if (geom.getNumGeometries() == 1) {
                final Geometry[] splittedGeom = GeometryUtils.splitGeom(geom, splitLine);

                if (isMulti) {
                    splittedGeom[0] = StaticGeometryFunctions.toMultiGeometry(splittedGeom[0]);
                    splittedGeom[1] = StaticGeometryFunctions.toMultiGeometry(splittedGeom[1]);
                }

                if (splittedGeom.length == 2) {
                    masterFeature.setGeometry(splittedGeom[0]);
                }

                final FeatureServiceFeature newFeature;

                if ((masterFeature instanceof DefaultFeatureServiceFeature)
                            && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties() != null)
                            && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties()
                                .getAttributeTableRuleSet() != null)) {
                    final AttributeTableRuleSet ruleSet = ((DefaultFeatureServiceFeature)masterFeature)
                                .getLayerProperties().getAttributeTableRuleSet();

                    newFeature = ruleSet.cloneFeature(dfsf);
                    newFeature.setGeometry(splittedGeom[1]);
                } else {
                    newFeature = (DefaultFeatureServiceFeature)dfsf.getLayerProperties().getFeatureService()
                                .getFeatureFactory()
                                .createNewFeature();
                    newFeature.setGeometry(splittedGeom[1]);

                    final HashMap<String, Object> properties = dfsf.getProperties();

                    for (final String propertyKey : properties.keySet()) {
                        if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(dfsf.getIdExpression())) {
                            if (!(properties.get(propertyKey) instanceof Geometry)) {
                                newFeature.setProperty(propertyKey, properties.get(propertyKey));
                            }
                        }
                    }
                }

                return newFeature;
            }
        }

        return null;
    }
}
