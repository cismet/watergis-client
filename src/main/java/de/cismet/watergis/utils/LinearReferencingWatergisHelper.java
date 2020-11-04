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
package de.cismet.watergis.utils;

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;

import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = LinearReferencingHelper.class)
public class LinearReferencingWatergisHelper implements LinearReferencingHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final String PROP_GEOM_GEOFIELD = "geo_field";
    private static final String PROP_STATION_VALUE = "wert";
    private static final String PROP_STATION_GEOM = "real_point";
    private static final String PROP_STATION_ROUTE = "route";
    private static final String PROP_ROUTE_GEOM = "geom";
    private static final String PROP_FG_BAK_NAME = "ba_cd";
    private static final String PROP_FG_BA_NAME = "ba_cd";
    private static final String PROP_FG_LAK_NAME = "la_cd";
    private static final String PROP_FG_LA_NAME = "la_cd";
    private static final String PROP_SG_SU_NAME = "su_cd";
    private static final String PROP_SG_UMRING_NAME = "see_id";
    private static final String PROP_FG_BA_DUV_NAME = "ba_cd";
    private static final String PROP_FG_BA_DUV_GES_NAME = "ba_cd";
    private static final String PROP_STATIONLINIE_FROM = "von";
    private static final String PROP_STATIONLINIE_TO = "bis";
    private static final String PROP_STATIONLINIE_GEOM = "geom";
    private static final String MC_NAME_FG_BAK = "fg_bak";
    private static final String MC_NAME_FG_BA = "fg_ba";
    private static final String MC_NAME_FG_LA = "fg_la";
    private static final String MC_NAME_FG_LAK = "fg_lak";
    private static final String MC_NAME_SG_SU = "sg_su";
    private static final String MC_NAME_SG_UMRING = "sg_umring";
    private static final String MC_NAME_FG_BA_DUV = "fg_ba_duv";
    private static final String MC_NAME_FG_BA_DUV_GES = "fg_ba_duv_ges";
    private static final String CN_GEOM = "GEOM";
    private static final String CN_FG_BAK_STATIONLINE = "dlm25w.FG_BAK_LINIE";
    private static final String CN_FG_BAK_STATION = "dlm25w.FG_BAK_PUNKT";
    private static final String CN_FG_BA_STATIONLINE = "dlm25w.FG_BA_LINIE";
    private static final String CN_FG_BA_STATION = "dlm25w.FG_BA_PUNKT";
    private static final String CN_FG_LAK_STATIONLINE = "dlm25w.FG_LAK_LINIE";
    private static final String CN_FG_LAK_STATION = "dlm25w.FG_LAK_PUNKT";
    private static final String CN_FG_LA_STATIONLINE = "dlm25w.FG_LA_LINIE";
    private static final String CN_FG_LA_STATION = "dlm25w.FG_LA_PUNKT";
    private static final String CN_SG_SU_STATION = "dlm25w.SG_SU_PUNKT";
    private static final String CN_SG_UMRING_STATION = "duv.SG_UMRING_PUNKT";
    private static final String CN_FG_BA_DUV_STATION = "duv.FG_BA_DUV_PUNKT";
    private static final String CN_FG_BA_DUV_GES_STATION = "duv.FG_BA_DUV_GES_PUNKT";
    private static final String CN_SG_SU_STATIONLINE = "dlm25w.SG_SU_LINIE";
    private static final String CN_SG_UMRING_STATIONLINE = "duv.SG_UMRING_LINIE";
    private static final String CN_FG_BA_DUV_STATIONLINE = "duv.FG_BA_DUV_LINIE";
    private static final String CN_FG_BA_DUV_GES_STATIONLINE = "duv.FG_BA_DUV_GES_LINIE";

    private static final MetaClass MC_GEOM = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, CN_GEOM);
    private static final MetaClass MC_FG_BAK_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BAK_STATION);
    private static final MetaClass MC_FG_BA_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_STATION);
    private static final MetaClass MC_FG_LAK_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_LAK_STATION);
    private static final MetaClass MC_FG_LA_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_LA_STATION);
    private static final MetaClass MC_SG_SU_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_SG_SU_STATION);
    private static final MetaClass MC_SG_UMRING_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_SG_UMRING_STATION);
    private static final MetaClass MC_FG_BA_DUV_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_DUV_STATION);
    private static final MetaClass MC_FG_BA_DUV_GES_STATION = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_DUV_GES_STATION);
    private static final MetaClass MC_FG_BAK_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BAK_STATIONLINE);
    private static final MetaClass MC_FG_BA_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_STATIONLINE);
    private static final MetaClass MC_FG_LAK_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_LAK_STATIONLINE);
    private static final MetaClass MC_FG_LA_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_LA_STATIONLINE);
    private static final MetaClass MC_SG_SU_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_SG_SU_STATIONLINE);
    private static final MetaClass MC_SG_UMRING_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_SG_UMRING_STATIONLINE);
    private static final MetaClass MC_FG_BA_DUV_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_DUV_STATIONLINE);
    private static final MetaClass MC_FG_BA_DUV_GES_STATIONLINIE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            CN_FG_BA_DUV_GES_STATIONLINE);

    //~ Instance fields --------------------------------------------------------

    private int NEW_STATION_ID = -1;
    private int NEW_LINE_ID = -1;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LinearReferencingHelper object.
     */
    public LinearReferencingWatergisHelper() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public double distanceOfStationGeomToRouteGeomFromStationBean(final CidsBean cidsBean) {
        final Geometry routeGeometry = getRouteGeometryFromStationBean(cidsBean);
        final Geometry pointGeometry = getPointGeometryFromStationBean(cidsBean);
        if (pointGeometry != null) {
            final double distance = pointGeometry.distance(routeGeometry);
            return distance;
        }
        return 0d;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getRouteGeometryFromStationBean(final CidsBean stationBean) {
        final CidsBean geomBean = getRouteGeomBeanFromStationBean(stationBean);

        if (geomBean != null) {
            return (Geometry)geomBean.getProperty(PROP_GEOM_GEOFIELD);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value        DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void setLinearValueToStationBean(final Double value, final CidsBean stationBean) throws Exception {
        if ((stationBean == null)
                    || ((stationBean.getProperty(PROP_STATION_VALUE) != null)
                        && stationBean.getProperty(PROP_STATION_VALUE).equals(value))) {
            return;
        }
        stationBean.setProperty(PROP_STATION_VALUE, value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Geometry getPointGeometryFromStationBean(final CidsBean stationBean) {
        final CidsBean geomBean = getPointGeomBeanFromStationBean(stationBean);

        if (geomBean != null) {
            return (Geometry)geomBean.getProperty(PROP_GEOM_GEOFIELD);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   point        DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void setPointGeometryToStationBean(final Geometry point, final CidsBean stationBean) throws Exception {
        final Geometry oldGeom = (Geometry)getPointGeomBeanFromStationBean(stationBean).getProperty(PROP_GEOM_GEOFIELD);

        if ((oldGeom == null) && (point == null)) {
            return;
        }

        if (((oldGeom == null) || (point == null)) || !oldGeom.equalsExact(point)) {
            getPointGeomBeanFromStationBean(stationBean).setProperty(PROP_GEOM_GEOFIELD, point);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geometry     DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void setRouteGeometryToStationBean(final Geometry geometry, final CidsBean stationBean) throws Exception {
        getRouteGeomBeanFromStationBean(stationBean).setProperty(PROP_GEOM_GEOFIELD, geometry);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getRouteNameFromStationBean(final CidsBean stationBean) {
        Object result = null;
        final CidsBean routeBean = getRouteBeanFromStationBean(stationBean);

        if (routeBean != null) {
            if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BAK)) {
                result = routeBean.getProperty(PROP_FG_BAK_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA)) {
                result = routeBean.getProperty(PROP_FG_BA_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LAK)) {
                result = routeBean.getProperty(PROP_FG_LAK_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LA)) {
                result = routeBean.getProperty(PROP_FG_LA_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_SU)) {
                result = routeBean.getProperty(PROP_SG_SU_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV)) {
                result = routeBean.getProperty(PROP_FG_BA_DUV_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV_GES)) {
                result = routeBean.getProperty(PROP_FG_BA_DUV_GES_NAME);
            } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_UMRING)) {
                result = routeBean.getProperty(PROP_SG_UMRING_NAME);
            } else {
                LOG.error("Unknown station bean. Cannot extract route name from station.");
            }
        } else {
            LOG.error("Cannot extract route bean from station.");
        }

        return String.valueOf(result);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getPointGeomBeanFromStationBean(final CidsBean stationBean) {
        if (stationBean == null) {
            return null;
        }
        return (CidsBean)stationBean.getProperty(PROP_STATION_GEOM);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean getRouteBeanFromStationBean(final CidsBean stationBean) {
        if (stationBean == null) {
            return null;
        }
        return (CidsBean)stationBean.getProperty(PROP_STATION_ROUTE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getRouteGeomBeanFromStationBean(final CidsBean stationBean) {
        final CidsBean route = (CidsBean)getRouteBeanFromStationBean(stationBean);
        if (route != null) {
            return (CidsBean)route.getProperty(PROP_ROUTE_GEOM);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean createStationBeanFromRouteBean(final CidsBean routeBean) {
        return createStationBeanFromRouteBean(routeBean, 0d);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     * @param   value      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean createStationBeanFromRouteBean(final CidsBean routeBean, final double value) {
        CidsBean stationBean = null;
        final CidsBean geomBean = MC_GEOM.getEmptyInstance().getBean();

        if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BAK)) {
            stationBean = MC_FG_BAK_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA)) {
            stationBean = MC_FG_BA_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LAK)) {
            stationBean = MC_FG_LAK_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LA)) {
            stationBean = MC_FG_LA_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_SU)) {
            stationBean = MC_SG_SU_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV)) {
            stationBean = MC_FG_BA_DUV_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV_GES)) {
            stationBean = MC_FG_BA_DUV_GES_STATION.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_UMRING)) {
            stationBean = MC_SG_UMRING_STATION.getEmptyInstance().getBean();
        } else {
            LOG.error("Unknown route bean. Cannot create the corresponding station bean.");
        }

        try {
            final int newStationId = getNewStationId();
            stationBean.setProperty(PROP_STATION_ROUTE, routeBean);
            stationBean.setProperty(PROP_STATION_VALUE, value);
            stationBean.setProperty(PROP_STATION_GEOM, geomBean);

            try {
                final CidsBean routeGeomBean = (CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM);
                final Geometry geom = (Geometry)routeGeomBean.getProperty(PROP_GEOM_GEOFIELD);
                setPointGeometryToStationBean(LinearReferencedPointFeature.getPointOnLine(value, geom), stationBean);
            } catch (Exception e) {
                LOG.error("Cannot set the geometry of the station", e);
            }
            stationBean.setProperty("id", newStationId);
            stationBean.getMetaObject().setID(newStationId);
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while filling bean", ex);
            }
        }
        return stationBean;
    }

    @Override
    public CidsBean createLineBeanFromStationBean(final CidsBean fromBean, final CidsBean toBean) {
        final CidsBean routeBean = getRouteBeanFromStationBean(fromBean);
        if (routeBean == null) {
            return null;
        }

        CidsBean linieBean = null;

        if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BAK)) {
            linieBean = MC_FG_BAK_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA)) {
            linieBean = MC_FG_BA_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LAK)) {
            linieBean = MC_FG_LAK_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LA)) {
            linieBean = MC_FG_LA_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_SU)) {
            linieBean = MC_SG_SU_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV)) {
            linieBean = MC_FG_BA_DUV_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV_GES)) {
            linieBean = MC_FG_BA_DUV_GES_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_UMRING)) {
            linieBean = MC_SG_UMRING_STATIONLINIE.getEmptyInstance().getBean();
        } else {
            LOG.error("Unknown route bean. Cannot create the corresponding line bean.");
        }

        final CidsBean geomBean = MC_GEOM.getEmptyInstance().getBean();

        try {
//            toBean.setProperty(
//                PROP_STATION_VALUE,
//                ((Geometry)((CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM)).getProperty(PROP_GEOM_GEOFIELD))
//                            .getLength());

            linieBean.setProperty(PROP_STATIONLINIE_FROM, fromBean);
            linieBean.setProperty(PROP_STATIONLINIE_TO, toBean);
            geomBean.setProperty(
                PROP_GEOM_GEOFIELD,
                ((Geometry)((CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM)).getProperty(PROP_GEOM_GEOFIELD)));
            linieBean.setProperty(PROP_STATIONLINIE_GEOM, geomBean);

            linieBean.setProperty("id", NEW_LINE_ID);
            linieBean.getMetaObject().setID(NEW_LINE_ID);

            NEW_LINE_ID--;
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while filling bean", ex);
            }
        }
        return linieBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean createLineBeanFromRouteBean(final CidsBean routeBean) {
        if (routeBean == null) {
            return null;
        }

        CidsBean linieBean = null;

        if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BAK)) {
            linieBean = MC_FG_BAK_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA)) {
            linieBean = MC_FG_BA_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LAK)) {
            linieBean = MC_FG_LAK_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_LA)) {
            linieBean = MC_FG_LA_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_SU)) {
            linieBean = MC_SG_SU_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV)) {
            linieBean = MC_FG_BA_DUV_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_FG_BA_DUV_GES)) {
            linieBean = MC_FG_BA_DUV_GES_STATIONLINIE.getEmptyInstance().getBean();
        } else if (routeBean.getMetaObject().getMetaClass().getName().equals(MC_NAME_SG_UMRING)) {
            linieBean = MC_SG_UMRING_STATIONLINIE.getEmptyInstance().getBean();
        } else {
            LOG.error("Unknown route bean. Cannot create the corresponding line bean.");
        }

        final CidsBean fromBean = createStationBeanFromRouteBean(routeBean);
        final CidsBean toBean = createStationBeanFromRouteBean(routeBean);
        final CidsBean geomBean = MC_GEOM.getEmptyInstance().getBean();

        try {
            toBean.setProperty(
                PROP_STATION_VALUE,
                ((Geometry)((CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM)).getProperty(PROP_GEOM_GEOFIELD))
                            .getLength());

            linieBean.setProperty(PROP_STATIONLINIE_FROM, fromBean);
            linieBean.setProperty(PROP_STATIONLINIE_TO, toBean);
            geomBean.setProperty(
                PROP_GEOM_GEOFIELD,
                ((Geometry)((CidsBean)routeBean.getProperty(PROP_ROUTE_GEOM)).getProperty(PROP_GEOM_GEOFIELD)));
            linieBean.setProperty(PROP_STATIONLINIE_GEOM, geomBean);

            linieBean.setProperty("id", NEW_LINE_ID);
            linieBean.getMetaObject().setID(NEW_LINE_ID);

            NEW_LINE_ID--;
        } catch (Exception ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while filling bean", ex);
            }
        }
        return linieBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public double getLinearValueFromStationBean(final CidsBean stationBean) {
        if ((stationBean == null) || (stationBean.getProperty(PROP_STATION_VALUE) == null)) {
            return 0d;
        }
        return (Double)stationBean.getProperty(PROP_STATION_VALUE);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineBean  DOCUMENT ME!
     * @param   isFrom    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean getStationBeanFromLineBean(final CidsBean lineBean, final boolean isFrom) {
        if (lineBean == null) {
            return null;
        }
        final String stationField = (isFrom) ? PROP_STATIONLINIE_FROM : PROP_STATIONLINIE_TO;
        return (CidsBean)lineBean.getProperty(stationField);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public CidsBean getGeomBeanFromLineBean(final CidsBean lineBean) {
        if (lineBean == null) {
            return null;
        }
        return (CidsBean)lineBean.getProperty(PROP_STATIONLINIE_GEOM);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line      DOCUMENT ME!
     * @param   lineBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public void setGeometryToLineBean(final Geometry line, final CidsBean lineBean) throws Exception {
        CidsBean geomBean = getGeomBeanFromLineBean(lineBean);

        if (geomBean == null) {
            geomBean = MC_GEOM.getEmptyInstance().getBean();
            lineBean.setProperty(PROP_STATIONLINIE_GEOM, geomBean);
        }

        if (geomBean != null) {
            geomBean.setProperty(PROP_GEOM_GEOFIELD, line);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public synchronized int getNewStationId() {
        return --NEW_STATION_ID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public synchronized int getNewLineId() {
        return --NEW_LINE_ID;
    }

    @Override
    public String getValueProperty(final CidsBean station) {
        return PROP_STATION_VALUE;
    }

    @Override
    public String getRouteNamePropertyFromRouteByClassName(final String routeClass) {
        String routeNameProperty = null;

        if (routeClass != null) {
            if (routeClass.endsWith(MC_NAME_FG_BAK)) {
                routeNameProperty = PROP_FG_BAK_NAME;
            } else if (routeClass.endsWith(MC_NAME_FG_BA)) {
                routeNameProperty = PROP_FG_BA_NAME;
            } else if (routeClass.endsWith(MC_NAME_FG_LAK)) {
                routeNameProperty = PROP_FG_LAK_NAME;
            } else if (routeClass.endsWith(MC_NAME_FG_LA)) {
                routeNameProperty = PROP_FG_LA_NAME;
            } else if (routeClass.endsWith(MC_NAME_SG_SU)) {
                routeNameProperty = PROP_SG_SU_NAME;
            } else if (routeClass.endsWith(MC_NAME_FG_BA_DUV)) {
                routeNameProperty = PROP_FG_BA_DUV_NAME;
            } else if (routeClass.endsWith(MC_NAME_FG_BA_DUV_GES)) {
                routeNameProperty = PROP_FG_BA_DUV_GES_NAME;
            } else if (routeClass.endsWith(MC_NAME_SG_UMRING)) {
                routeNameProperty = PROP_SG_UMRING_NAME;
            } else {
                LOG.error("Unknown station bean. Cannot extract route name from station.");
            }
        } else {
            LOG.error("Cannot extract route name from bean.");
        }

        return routeNameProperty;
    }

    @Override
    public void setRouteBeanToStationBean(final CidsBean routeBean, final CidsBean stationBean) throws Exception {
        stationBean.setProperty(PROP_STATION_ROUTE, routeBean);
    }

    @Override
    public String[] getDomainOfRouteTable(final String routeTable) {
        return new String[] { "DLM25W" };
    }

    @Override
    public Geometry getGeometryFromRoute(final CidsBean routeBean) {
        return (Geometry)routeBean.getProperty(PROP_STATIONLINIE_GEOM + "." + PROP_GEOM_GEOFIELD);
    }
}
