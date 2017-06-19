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
package de.cismet.cids.custom.permissions.dlm25w;

import Sirius.server.newuser.User;

import java.util.List;

import de.cismet.cids.dynamics.AbstractCustomBeanPermissionProvider;
import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerPermissionProvider;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class WatergisPermissionProvider extends AbstractCustomBeanPermissionProvider
        implements CidsLayerPermissionProvider {

    //~ Static fields/initializers ---------------------------------------------

    protected static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
            WatergisPermissionProvider.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean getCustomReadPermissionDecisionforUser(final User u) {
        return true;
    }

    @Override
    public boolean getCustomWritePermissionDecisionforUser(final User u) {
        if (u.getUserGroup().getName().equalsIgnoreCase("administratoren")
                    || u.getUserGroup().getName().equalsIgnoreCase("admin_edit")) {
            if (log.isDebugEnabled()) {
                log.debug("member of admin group. permission is granted");
            }
            return true;
        }

        final CidsBean wwGr = getWwGrBean();

        if ((AppBroker.getInstance().getOwnWwGr() != null)
                    && (AppBroker.getInstance().getOwnWwGr().getProperty("ww_gr") != null)) {
            if ((wwGr != null) && wwGr.getProperty("owner").equals(u.getUserGroup().getName())) {
                return true;
            } else {
                return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean getCustomCidsLayerWritePermissionDecisionforUser(final User u, final CidsLayerFeature feature) {
        if (u.getUserGroup().getName().equalsIgnoreCase("administratoren")
                    || u.getUserGroup().getName().equalsIgnoreCase("admin_edit")) {
            if (log.isDebugEnabled()) {
                log.debug("member of admin group. permission is granted");
            }
            return true;
        }

        final CidsBean wwGr = getWwGrfromFeature(feature);

        if ((AppBroker.getInstance().getOwnWwGr() != null)
                    && (AppBroker.getInstance().getOwnWwGr().getProperty("ww_gr") != null)) {
            if ((wwGr != null) && wwGr.getProperty("owner").equals(u.getUserGroup().getName())) {
                return true;
            } else {
                return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
            }
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getWwGrfromFeature(final CidsLayerFeature feature) {
        return getWwGrBeanFromProperty(feature, getWwGrPropertyName());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature   DOCUMENT ME!
     * @param   property  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CidsBean getWwGrBeanFromProperty(final CidsLayerFeature feature, final String property) {
        final Object wwGrObject = feature.getProperty(property);
        final List<CidsBean> wwgrBeans = AppBroker.getInstance().getWwGrList();

        if (wwGrObject == null) {
            return null;
        } else if (wwGrObject instanceof CidsBean) {
            return (CidsBean)wwGrObject;
        } else if (wwGrObject instanceof CidsLayerFeature) {
            final CidsLayerFeature f = (CidsLayerFeature)wwGrObject;

            for (final CidsBean bean : wwgrBeans) {
                if (bean.getProperty("id").equals(f.getId())) {
                    return bean;
                }
            }
        } else {
            final String wwGr = wwGrObject.toString();

            for (final CidsBean bean : wwgrBeans) {
                if ((bean.getProperty("ww_gr") != null) && bean.getProperty("ww_gr").toString().equals(wwGr)) {
                    return bean;
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract CidsBean getWwGrBean();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract String getWwGrPropertyName();
}
