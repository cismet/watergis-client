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
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;

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
                    splittedGeom[0] = GeometryUtils.toMultiGeometry(splittedGeom[0]);
                    splittedGeom[1] = GeometryUtils.toMultiGeometry(splittedGeom[1]);
                }

                if (splittedGeom.length == 2) {
                    masterFeature.setGeometry(splittedGeom[0]);
                }

                final DefaultFeatureServiceFeature newFeature = (DefaultFeatureServiceFeature)dfsf
                            .getLayerProperties().getFeatureService().getFeatureFactory().createNewFeature();
                newFeature.setGeometry(splittedGeom[1]);

                final HashMap<String, Object> properties = dfsf.getProperties();

                for (final String propertyKey : properties.keySet()) {
                    if (!propertyKey.equalsIgnoreCase("id") && !propertyKey.equals(dfsf.getIdExpression())) {
                        if (!(properties.get(propertyKey) instanceof Geometry)) {
                            newFeature.setProperty(propertyKey, properties.get(propertyKey));
                        }
                    }
                }

                return newFeature;
            }
        }

        return null;
    }
}
