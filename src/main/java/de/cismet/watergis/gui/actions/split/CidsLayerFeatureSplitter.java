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
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import de.cismet.cids.custom.wrrl_db_mv.util.CidsBeanSupport;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.LineAndStationCreator;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockerFactory;
import de.cismet.cismap.commons.gui.attributetable.FeatureLockingInterface;
import de.cismet.cismap.commons.gui.attributetable.LockAlreadyExistsException;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.FeatureRegistry;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.watergis.broker.AppBroker;

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
    private static int stationId = -1;
    private static Set<Feature> lockedFeatures = new TreeSet<Feature>();

    //~ Instance fields --------------------------------------------------------

    private final List<FeatureServiceFeature> additionalFeaturesToSave = new ArrayList<FeatureServiceFeature>();
    private List<FeatureServiceFeature> originalFeature = new ArrayList<FeatureServiceFeature>();
    private List<FeatureServiceFeature> featuresToRemove = new ArrayList<FeatureServiceFeature>();
    private Map<FeatureLockingInterface, List<Object>> lockMap = new HashMap<FeatureLockingInterface, List<Object>>();

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

            if (splittedGeom.length > 0) {
                for (int i = 0; i < splittedGeom.length; ++i) {
                    if (((splittedGeom[i] instanceof LineString) || (splittedGeom[i] instanceof MultiLineString))
                                && (splittedGeom[i].getLength() < 0.01)) {
                        return null;
                    } else if (((splittedGeom[i] instanceof Polygon) || (splittedGeom[i] instanceof MultiPolygon))
                                && (splittedGeom[i].getArea() < 0.0001)) {
                        return null;
                    }
                }
            }

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
                final CidsLayer layer = (CidsLayer)((CidsLayerFeature)masterFeature).getLayerProperties()
                            .getFeatureService();

                if (layer.getMetaClass().getTableName().equalsIgnoreCase("dlm25w.fg_bak")) {
                    try {
                        splitCat2((CidsLayerFeature)masterFeature, splitLine);
                    } catch (LockAlreadyExistsException ex) {
                        return null;
                    }
                }
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
                                    ((DefaultFeatureServiceFeature)masterFeature).setProperty(
                                        linePropertyName,
                                        origLineBean);
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
     * DOCUMENT ME!
     *
     * @param   fgBak      DOCUMENT ME!
     * @param   splitLine  DOCUMENT ME!
     *
     * @throws  LockAlreadyExistsException  DOCUMENT ME!
     */
    private void splitCat2(final CidsLayerFeature fgBak, final LineString splitLine) throws LockAlreadyExistsException {
        final List<MetaClass> cat2Classes = new ArrayList<MetaClass>();

        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_anll"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_bbef"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_d"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_deich"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_due"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_gbk_delta"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_leis"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_prof"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_rl"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_sb"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_sbef"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_tech"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_ubef"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_ughz"));

        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_ae"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gbk"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gn1"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gn2"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gn3"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_gwk"));
        cat2Classes.add(ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_bak_wk"));

        for (final MetaClass cl : cat2Classes) {
            try {
                final CidsLayer layer = new CidsLayer(cl);

                layer.initAndWait();
                String query;
                if (cl.getTableName().toLowerCase().contains("fg_bak")) {
                    query = "dlm25w.fg_bak.ba_cd = '" + fgBak.getProperty("ba_cd") + "'";
                } else {
                    query = "dlm25w.fg_ba.ba_cd = '" + fgBak.getProperty("ba_cd") + "'";
                }
                final List<Feature> features = layer.getFeatureFactory().createFeatures(query, null, null, 0, 0, null);

                if ((features != null) && !features.isEmpty()) {
                    final FeatureLockingInterface locker = FeatureLockerFactory.getInstance()
                                .getLockerForFeatureService(layer);

                    if (locker != null) {
                        try {
                            List<Object> locks = lockMap.get(locker);
                            final List<Feature> featuresToLock = new ArrayList<Feature>();

                            if (locks == null) {
                                locks = new ArrayList<Object>();
                                lockMap.put(locker, locks);
                            }

                            for (final Feature f : features) {
                                if (!lockedFeatures.contains(f)) {
                                    featuresToLock.add(f);
                                }
                            }
                            locks.add(locker.lock(featuresToLock, false));
                            lockedFeatures.addAll(featuresToLock);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                "Es können aufgesetzte Objekte aus "
                                        + layer.getName()
                                        + " nicht gesperrt werden",
                                "Sperre",
                                JOptionPane.ERROR_MESSAGE);
                            unlockObjects();
                            throw new LockAlreadyExistsException(e.getMessage(), "Object already locked");
                        }
                    }
                }

                for (final Feature f : features) {
                    final FeatureServiceFeature feature = (FeatureServiceFeature)f;
                    if (feature.getGeometry().intersects(splitLine)) {
                        final AttributeTableRuleSet ruleSet = ((DefaultFeatureServiceFeature)feature)
                                    .getLayerProperties().getAttributeTableRuleSet();

                        final FeatureServiceFeature clonedFeature = ruleSet.cloneFeature(feature);
                        clonedFeature.setId(feature.getId());
                        clonedFeature.setProperty("id", feature.getId());
                        ((CidsLayerFeature)clonedFeature).getBean();
                        originalFeature.add(clonedFeature);
                        final Feature[] splittedFeatures = split(feature, splitLine);

                        if ((splittedFeatures != null) && (splittedFeatures.length > 0)) {
                            additionalFeaturesToSave.add(feature);

                            for (final Feature splittedFeature : splittedFeatures) {
                                if ((splittedFeature.getGeometry() != null)
                                            && (splittedFeature.getGeometry() instanceof LineString)
                                            && (((LineString)splittedFeature.getGeometry()).getLength() > 0.01)) {
                                    featuresToRemove.add((FeatureServiceFeature)splittedFeature);

                                    additionalFeaturesToSave.add((FeatureServiceFeature)splittedFeature);
                                }
                            }
                        }
                    }
                }
            } catch (LockAlreadyExistsException e) {
                throw e;
            } catch (Exception e) {
                LOG.error("Error while splitting features");
            }
        }
    }

    @Override
    public List<FeatureServiceFeature> getAdditionalFeaturesToSave() {
        return additionalFeaturesToSave;
    }

    @Override
    public void undo() {
        try {
            for (final FeatureServiceFeature feature : originalFeature) {
                try {
                    final CidsBean bean = ((CidsLayerFeature)feature).getBean();
                    bean.getMetaObject().setStatus(MetaObject.MODIFIED);
                    if (((CidsBean)bean.getProperty("ba_st")) != null) {
                        bean.getMetaObject().setStatus(MetaObject.MODIFIED);
                        bean.getMetaObject().getAttribute("ba_st").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st")).getMetaObject().getAttribute("von").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st")).getMetaObject().getAttribute("bis").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st")).getMetaObject().getAttribute("geom").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.geom")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st.geom")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.von")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st.von")).getMetaObject().getAttribute("wert").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.von")).getMetaObject()
                                .getAttribute("real_point")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.von.real_point")).getMetaObject()
                                .setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st.von.real_point")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.bis")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st.bis")).getMetaObject().getAttribute("wert").setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.bis")).getMetaObject()
                                .getAttribute("real_point")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("ba_st.bis.real_point")).getMetaObject()
                                .setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("ba_st.bis.real_point")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                    }
                    if (((CidsBean)bean.getProperty("bak_st")) != null) {
                        bean.getMetaObject().setStatus(MetaObject.MODIFIED);
                        bean.getMetaObject().getAttribute("bak_st").setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st")).getMetaObject().getAttribute("von").setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st")).getMetaObject().getAttribute("bis").setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st")).getMetaObject().getAttribute("geom").setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.geom")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st.geom")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.von")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st.von")).getMetaObject()
                                .getAttribute("wert")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.von")).getMetaObject()
                                .getAttribute("real_point")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.von.real_point")).getMetaObject()
                                .setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st.von.real_point")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.bis")).getMetaObject().setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st.bis")).getMetaObject()
                                .getAttribute("wert")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.bis")).getMetaObject()
                                .getAttribute("real_point")
                                .setChanged(true);
                        ((CidsBean)bean.getProperty("bak_st.bis.real_point")).getMetaObject()
                                .setStatus(MetaObject.MODIFIED);
                        ((CidsBean)bean.getProperty("bak_st.bis.real_point")).getMetaObject()
                                .getAttribute("GEO_STRING")
                                .setChanged(true);
                    }
                    bean.persist();
                } catch (Exception e) {
                    ((CidsLayerFeature)feature).getBean().getMetaObject().setStatus(2);
                    LOG.error("Cannot undo split change", e);
                }
                ((CidsLayerFeature)feature).getBean().getMetaObject().setStatus(0);
            }
            for (final FeatureServiceFeature feature : featuresToRemove) {
                try {
                    ((CidsLayerFeature)feature).delete();
                } catch (Exception e) {
                    LOG.error("Cannot undo split change", e);
                }
            }
        } finally {
            unlockObjects();
        }
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
            final int newId = --stationId;
            fromStation.getMetaObject().setID(newId);
            fromStation.setProperty("id", newId);
        }
        if (cloneStations && (tillStation != null)) {
            tillStation = CidsBeanSupport.cloneStation(tillStation);
            final int newId = --stationId;
            tillStation.getMetaObject().setID(newId);
            tillStation.setProperty("id", newId);
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

    @Override
    public void unlockObjects() {
        for (final FeatureLockingInterface locker : lockMap.keySet()) {
            final List<Object> lockList = lockMap.get(locker);

            for (final Object lock : lockList) {
                try {
                    locker.unlock(lock);
                } catch (Exception e) {
                    LOG.error("Cannot unlock object", e);
                }
            }
        }

        lockedFeatures.clear();
    }
}
