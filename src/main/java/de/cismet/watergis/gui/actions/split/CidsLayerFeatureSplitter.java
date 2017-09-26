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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.cismet.cids.custom.wrrl_db_mv.util.CidsBeanSupport;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.LineAndStationCreator;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.FeatureRegistry;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.watergis.gui.actions.merge.CidsLayerFeatureMerger;

import de.cismet.watergis.utils.GeometryUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerFeatureSplitter implements FeatureSplitter {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerFeatureSplitter.class);
    private static final String[] POSSIBLE_LINE_PROP_NAMES = { "ba_st", "bak_st", "la_st", "lak_st", "sg_su_stat" };

    //~ Instance fields --------------------------------------------------------

    private LinearReferencingHelper linearReferencingHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();

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

            if (!((((DefaultFeatureServiceFeature)masterFeature).getLayerProperties() != null)
                            && (((DefaultFeatureServiceFeature)masterFeature).getLayerProperties()
                                .getAttributeTableRuleSet() != null))) {
                // this should never happen
                LOG.error("Cids layer has no layer properties");
                // todo Ausgabe
                return null;
            }
            final List<Feature> newFeatures = new ArrayList<Feature>();

            if (splittedGeom.length > 1) {
                masterFeature.setGeometry(splittedGeom[0]);

                for (int i = 1; i < splittedGeom.length; ++i) {
                    final AttributeTableRuleSet ruleSet = ((DefaultFeatureServiceFeature)masterFeature)
                                .getLayerProperties().getAttributeTableRuleSet();

                    final FeatureServiceFeature newFeature = ruleSet.cloneFeature(dfsf);
                    newFeatures.add(newFeature);

                    try {
//                        CidsBean featureClone = CidsBeanSupport.cloneCidsBean(((CidsLayerFeature) masterFeature).getBean(), false);
//                        ((CidsLayerFeature) newFeature).setMetaObject(featureClone.getMetaObject());
                        newFeature.setGeometry(splittedGeom[i]);
                        String linePropertyName = null;
                        Object origLineBean = null;

                        for (final String possibleName : POSSIBLE_LINE_PROP_NAMES) {
                            origLineBean = ((CidsLayerFeature)masterFeature).getBean().getProperty(possibleName);

                            if (origLineBean instanceof CidsBean) {
                                linePropertyName = possibleName;
                                break;
                            }
                        }

                        if (linePropertyName != null) {
                            if (ruleSet.isCatThree()) {
                                newFeature.setProperty(linePropertyName, null);
                                final FeatureCreator creator = ruleSet.getFeatureCreator();
                                final LineAndStationCreator lineCreator = (LineAndStationCreator)creator;
                                final MetaClass routeMc = lineCreator.getRouteClass();
                                final String stationProperty = lineCreator.getStationProperty();

                                if (i == 1) {
                                    // adjust the master stations only one time
                                    ((CidsLayerFeature)masterFeature).removeStations();
                                    ((CidsLayerFeature)masterFeature).setProperty(linePropertyName, null);
                                    ((CidsLayerFeature)masterFeature).getBean().setProperty(linePropertyName, null);

                                    if (creator instanceof LineAndStationCreator) {
                                        CidsLayerFeatureMerger.setCalculatedLine((FeatureServiceFeature)masterFeature,
                                            routeMc,
                                            linearReferencingHelper,
                                            splittedGeom[0],
                                            stationProperty);
                                    }
                                    ((CidsLayerFeature)masterFeature).initStations();
                                }

                                if (creator instanceof LineAndStationCreator) {
                                    CidsLayerFeatureMerger.setCalculatedLine(
                                        newFeature,
                                        routeMc,
                                        linearReferencingHelper,
                                        splittedGeom[i],
                                        stationProperty);
                                    ((CidsLayerFeature)newFeature).initStations();
                                }
                            } else {
                                CidsBean statLine = CidsBeanSupport.cloneStationline((CidsBean)origLineBean);
                                statLine = adjustLineStation(statLine, splittedGeom[i], true);
                                newFeature.setProperty(linePropertyName, statLine);

                                if (i == 1) {
                                    // adjust the master stations only one time
                                    adjustLineStation((CidsBean)origLineBean, splittedGeom[0], false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Error while cloning station line", e);
                    }
                }
            }

            return newFeatures.toArray(new Feature[newFeatures.size()]);
        }

        return null;
    }

    /**
     * Adjusts the given stationLine bean so that it uses the given line geometry.
     *
     * @param   master         station line cidsBean
     * @param   geometry       a line geometry
     * @param   cloneStations  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean adjustLineStation(CidsBean master, final Geometry geometry, final boolean cloneStations)
            throws Exception {
        CidsBean fromStation = linearReferencingHelper.getStationBeanFromLineBean(master, true);
        CidsBean tillStation = linearReferencingHelper.getStationBeanFromLineBean(master, false);

        if (cloneStations && (fromStation != null)) {
            fromStation = CidsBeanSupport.cloneStation(fromStation);
        }
        if (cloneStations && (tillStation != null)) {
            tillStation = CidsBeanSupport.cloneStation(tillStation);
            master = linearReferencingHelper.createLineBeanFromStationBean(fromStation, tillStation);
        }
        final Geometry routGeom = linearReferencingHelper.getRouteGeometryFromStationBean(fromStation);
        final LengthIndexedLine lil = new LengthIndexedLine(routGeom);
        double fromValue = lil.indexOf(geometry.getCoordinates()[0]);
        double tillValue = lil.indexOf(geometry.getCoordinates()[geometry.getCoordinates().length - 1]);

        if (fromValue > tillValue) {
            final double tmp = fromValue;
            fromValue = tillValue;
            tillValue = tmp;
        }

        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CismapBroker.getInstance().getDefaultCrsAlias());
        final Geometry fromPoint = factory.createPoint(lil.extractPoint(fromValue));
        final Geometry tillPoint = factory.createPoint(lil.extractPoint(tillValue));
        final Geometry lineGeometry = lil.extractLine(fromValue, tillValue);
        fromPoint.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
        tillPoint.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
        lineGeometry.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        linearReferencingHelper.setLinearValueToStationBean(fromValue, fromStation);
        linearReferencingHelper.setLinearValueToStationBean(tillValue, tillStation);
        linearReferencingHelper.setPointGeometryToStationBean(fromPoint, fromStation);
        linearReferencingHelper.setPointGeometryToStationBean(tillPoint, tillStation);
        linearReferencingHelper.setGeometryToLineBean(lineGeometry, master);

        return master;
    }
}
