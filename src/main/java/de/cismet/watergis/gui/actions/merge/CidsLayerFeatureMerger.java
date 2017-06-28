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
package de.cismet.watergis.gui.actions.merge;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import org.apache.log4j.Logger;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.FeatureRegistry;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

import de.cismet.math.geometry.StaticGeometryFunctions;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerFeatureMerger implements FeatureMerger {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerFeatureMerger.class);
    private static final String[] POSSIBLE_LINE_PROP_NAMES = { "ba_st", "bak_st", "la_st", "lak_st" };

    //~ Instance fields --------------------------------------------------------

    private LinearReferencingHelper linearReferencingHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();

    //~ Methods ----------------------------------------------------------------

    @Override
    public Feature merge(final Feature masterFeature, final Feature childFeature) throws MergeException {
        final Geometry g = masterFeature.getGeometry();
        Geometry mergedGeom = g.union(childFeature.getGeometry());
        final String originGeomType = masterFeature.getGeometry().getGeometryType();
        final boolean isMulti = masterFeature.getGeometry().getGeometryType().toLowerCase().startsWith("multi");

        // The geometry type cannot be point, because the merge operation is not allowed for point geometries.
        // Otherwise, the geoemtry type would be changed to multi point. The only exception would be features with the
        // same point geometry, but a merge with with features makes no sense.

        if (g.getGeometryType().toUpperCase().contains("LINE")) {
            // try to merge the lines
            final LineMerger lineMerger = new LineMerger();
            lineMerger.add(mergedGeom);

            if (lineMerger.getMergedLineStrings().size() == 1) {
                mergedGeom = (Geometry)lineMerger.getMergedLineStrings().toArray(new Geometry[1])[0];
            }
        }

        if (isMulti) {
            mergedGeom = StaticGeometryFunctions.toMultiGeometry(mergedGeom);
        }
        mergedGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        if (!((((DefaultFeatureServiceFeature)masterFeature).getLayerProperties() != null)
                        && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties()
                            .getAttributeTableRuleSet() != null))) {
            // this should never happen
            LOG.error("Cids layer has no layer properties");
            // todo Ausgabe
            return null;
        }

        try {
            final AttributeTableRuleSet ruleSet = ((DefaultFeatureServiceFeature)masterFeature).getLayerProperties()
                        .getAttributeTableRuleSet();
            Object origLineBean = null;
            String linePropertyName = null;

            for (final String possibleName : POSSIBLE_LINE_PROP_NAMES) {
                origLineBean = ((CidsLayerFeature)masterFeature).getBean().getProperty(possibleName);

                if (origLineBean instanceof CidsBean) {
                    linePropertyName = possibleName;
                    break;
                }
            }

            if (linePropertyName != null) {
                if (ruleSet.isCatThree()) {
                    ((CidsLayerFeature)masterFeature).removeStations();
                    ((CidsLayerFeature)masterFeature).setProperty(linePropertyName, null);
                    ((CidsLayerFeature)masterFeature).getBean().setProperty(linePropertyName, null);
                } else {
                    final CidsBean otherLineBean = (CidsBean)((CidsLayerFeature)childFeature).getBean()
                                .getProperty(linePropertyName);
                    CidsBean masterFrom = linearReferencingHelper.getStationBeanFromLineBean((CidsBean)origLineBean,
                            true);
                    CidsBean masterTill = linearReferencingHelper.getStationBeanFromLineBean((CidsBean)origLineBean,
                            false);
                    CidsBean otherFrom = linearReferencingHelper.getStationBeanFromLineBean(otherLineBean, true);
                    CidsBean otherTill = linearReferencingHelper.getStationBeanFromLineBean(otherLineBean, false);

                    double masterFromVal = linearReferencingHelper.getLinearValueFromStationBean(masterFrom);
                    double masterTillVal = linearReferencingHelper.getLinearValueFromStationBean(masterTill);
                    double otherFromVal = linearReferencingHelper.getLinearValueFromStationBean(otherFrom);
                    double otherTillVal = linearReferencingHelper.getLinearValueFromStationBean(otherTill);

                    final CidsBean masterRouteBean = linearReferencingHelper.getRouteBeanFromStationBean(masterFrom);
                    final CidsBean otherRouteBean = linearReferencingHelper.getRouteBeanFromStationBean(otherFrom);

                    if (masterRouteBean.getMetaObject().getID() != otherRouteBean.getMetaObject().getID()) {
                        throw new MergeException("Die ausgewählten Objekte liegen nicht auf der gleichen Route");
                    }

                    if (masterFromVal > masterTillVal) {
                        final CidsBean tmp = masterFrom;
                        masterFrom = masterTill;
                        masterTill = tmp;
                        final double tmpVal = masterFromVal;
                        masterFromVal = masterTillVal;
                        masterTillVal = tmpVal;
                    }

                    if (otherFromVal > otherTillVal) {
                        final CidsBean tmp = otherFrom;
                        otherFrom = otherTill;
                        otherTill = tmp;
                        final double tmpVal = otherFromVal;
                        otherFromVal = otherTillVal;
                        otherTillVal = tmpVal;
                    }

                    if (otherFromVal < masterFromVal) {
                        linearReferencingHelper.setLinearValueToStationBean(otherFromVal, masterFrom);
                    }
                    if (otherTillVal > masterTillVal) {
                        linearReferencingHelper.setLinearValueToStationBean(otherTillVal, masterTill);
                    }
                }
            }
        } catch (MergeException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while setting station beans", e);
        }
        masterFeature.setGeometry(mergedGeom);

        return masterFeature;
    }
}
