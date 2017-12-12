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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    public Feature[] split(final Feature masterFeature, final LineString splitLine) {
        if (masterFeature instanceof DefaultFeatureServiceFeature) {
            final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)masterFeature;
            final boolean isMulti = masterFeature.getGeometry().getGeometryType().toLowerCase().startsWith("multi");

            final Geometry geom = dfsf.getGeometry();

            final Geometry[] splittedGeom = GeometryUtils.splitGeom(geom, splitLine);

            if (isMulti) {
                for (int i = 0; i < splittedGeom.length; ++i) {
                    splittedGeom[i] = StaticGeometryFunctions.toMultiGeometry(splittedGeom[i]);
                    splittedGeom[i].setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                }
            } else {
                if (splittedGeom[0] instanceof LineString) {
                    // the longest line should be the first, because the longest Geometry
                    // should still contain its ba_cd (theme FG_BA).
                    Arrays.sort(splittedGeom, new Comparator<Geometry>() {

                            @Override
                            public int compare(final Geometry o1, final Geometry o2) {
                                return (int)Math.signum(o2.getLength() - o1.getLength());
                            }
                        });
                }
            }
            final List<Feature> newFeatures = new ArrayList<Feature>();

            if (splittedGeom.length > 1) {
                masterFeature.setGeometry(splittedGeom[0]);

                for (int i = 1; i < splittedGeom.length; ++i) {
                    final FeatureServiceFeature newFeature;

                    if ((masterFeature instanceof DefaultFeatureServiceFeature)
                                && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties() != null)
                                && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties()
                                    .getAttributeTableRuleSet() != null)) {
                        final AttributeTableRuleSet ruleSet = ((DefaultFeatureServiceFeature)masterFeature)
                                    .getLayerProperties().getAttributeTableRuleSet();

                        newFeature = ruleSet.cloneFeature(dfsf);
                        newFeatures.add(newFeature);
                        newFeature.setGeometry(splittedGeom[1]);
                    } else {
                        newFeature = (DefaultFeatureServiceFeature)dfsf.getLayerProperties().getFeatureService()
                                    .getFeatureFactory()
                                    .createNewFeature();
                        newFeatures.add(newFeature);
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
                }
                return newFeatures.toArray(new Feature[newFeatures.size()]);
            }
        }

        return null;
    }

    @Override
    public List<FeatureServiceFeature> getAdditionalFeaturesToSave() {
        return new ArrayList<FeatureServiceFeature>();
    }

    @Override
    public void undo() {
    }

    @Override
    public void unlockObjects() {
    }
}
