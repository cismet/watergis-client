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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import de.cismet.cids.custom.watergis.server.search.DetermineClosestRoute;
import de.cismet.cids.custom.watergis.server.search.DetermineSourceSink;
import de.cismet.cids.custom.watergis.server.search.RouteProblemsCount;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.LineAndStationCreator;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
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
    private static final String[] POSSIBLE_LINE_PROP_NAMES = { "ba_st", "bak_st", "la_st", "lak_st", "sg_su_stat" };

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

                if ((mergedGeom.getCoordinates()[0] != g.getCoordinates()[0])
                            && (mergedGeom.getCoordinates()[mergedGeom.getCoordinates().length - 1]
                                != g.getCoordinates()[g.getCoordinates().length - 1])) {
                    mergedGeom = mergedGeom.reverse();
                }
            } else {
                final Geometry mergedGeomReverseOrder = masterFeature.getGeometry()
                            .union(childFeature.getGeometry().reverse());

                final LineMerger reverseOrderLineMerger = new LineMerger();
                reverseOrderLineMerger.add(mergedGeomReverseOrder);

                if (reverseOrderLineMerger.getMergedLineStrings().size() == 1) {
                    mergedGeom = (Geometry)reverseOrderLineMerger.getMergedLineStrings().toArray(new Geometry[1])[0];
                }
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
                    final FeatureCreator creator = ruleSet.getFeatureCreator();

                    if (creator instanceof LineAndStationCreator) {
                        final LineAndStationCreator lineCreator = (LineAndStationCreator)creator;
                        final MetaClass routeMc = lineCreator.getRouteClass();
                        final String stationProperty = lineCreator.getStationProperty();

                        setCalculatedLine((FeatureServiceFeature)masterFeature,
                            routeMc,
                            linearReferencingHelper,
                            mergedGeom,
                            stationProperty);
                        ((CidsLayerFeature)masterFeature).initStations();
                    }
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
//least(greatest(v.wert, b.wert), greatest(von.wert, bis.wert)) - greatest(least(v.wert, b.wert), least(von.wert, bis.wert)) > 0.1
                    final double d =
                        Math.min(Math.max(masterFromVal, masterTillVal), Math.max(otherFromVal, otherTillVal))
                                - Math.max(Math.min(masterFromVal, masterTillVal), Math.min(otherFromVal, otherTillVal));

                    if (!(d > -0.1)) {
                        throw new MergeException("Die ausgewählten Objekte berühren oder überlappen sich nicht");
                    }

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
                    final CidsBean geomBean = linearReferencingHelper.getGeomBeanFromLineBean((CidsBean)origLineBean);
                    masterFeature.setGeometry((Geometry)geomBean.getProperty("geo_field"));

                    return masterFeature;
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

    /**
     * DOCUMENT ME!
     *
     * @param   feature          DOCUMENT ME!
     * @param   routeClass       DOCUMENT ME!
     * @param   helper           DOCUMENT ME!
     * @param   g                DOCUMENT ME!
     * @param   stationProperty  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void setCalculatedLine(final FeatureServiceFeature feature,
            final MetaClass routeClass,
            final LinearReferencingHelper helper,
            final Geometry g,
            final String stationProperty) throws Exception {
        final Geometry firstPoint = createPointFromCoords(
                g.getCoordinates()[0],
                g.getFactory());
        final Geometry lastPoint = createPointFromCoords(
                g.getCoordinates()[g.getNumPoints() - 1],
                g.getFactory());

        final ArrayList routeMetaObject = (ArrayList)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new DetermineClosestRoute(
                                routeClass.getID(),
                                routeClass.getPrimaryKey(),
                                routeClass.getTableName(),
                                firstPoint.toText()));

        CidsBean routeBean = null;

        if ((routeMetaObject != null) && !routeMetaObject.isEmpty()) {
            routeBean = ((MetaObject)routeMetaObject.get(0)).getBean();
        }

        if (routeBean != null) {
            final CidsBean firstStation = createStationFromRoute(
                    routeBean,
                    firstPoint,
                    helper);
            final CidsBean lastStation = createStationFromRoute(
                    routeBean,
                    lastPoint,
                    helper);
            final CidsBean lineBean = helper.createLineBeanFromRouteBean(routeBean);
            final Double firstVal = (Double)firstStation.getProperty(
                    helper.getValueProperty(firstStation));
            final Double lastVal = (Double)lastStation.getProperty(
                    helper.getValueProperty(lastStation));

            helper.setLinearValueToStationBean(
                firstVal,
                helper.getStationBeanFromLineBean(lineBean, true));
            helper.setLinearValueToStationBean(
                lastVal,
                helper.getStationBeanFromLineBean(lineBean, false));
            feature.setProperty(stationProperty, lineBean);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   coord    DOCUMENT ME!
     * @param   factory  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Geometry createPointFromCoords(final Coordinate coord,
            final GeometryFactory factory) {
        return factory.createPoint(coord);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     * @param   point      DOCUMENT ME!
     * @param   helper     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static CidsBean createStationFromRoute(final CidsBean routeBean,
            final Geometry point,
            final LinearReferencingHelper helper) {
        final Coordinate[] firstCoords = DistanceOp.nearestPoints(
                helper.getGeometryFromRoute(routeBean),
                point);
        final double firstPosition = LinearReferencedPointFeature.getPositionOnLine(
                firstCoords[0],
                helper.getGeometryFromRoute(routeBean));

        final CidsBean station = helper.createStationBeanFromRouteBean(
                routeBean,
                firstPosition);

        return station;
    }
}
