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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.util.ArrayList;

import de.cismet.cids.custom.watergis.server.search.QpWwgrByNr;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerPermissionProvider;

import de.cismet.watergis.broker.AppBroker;

import static de.cismet.cids.custom.permissions.dlm25w.WatergisPermissionProvider.log;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Dlm25wQpGafPpPermissionProvider extends WatergisPermissionProvider implements CidsLayerPermissionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(Dlm25wQpGafPpPermissionProvider.class);
//    private static final Map<String, String> qpCache = new HashMap<String, String>();

    //~ Methods ----------------------------------------------------------------

    @Override
    protected CidsBean getWwGrBean() {
        try {
            final String qpNr = String.valueOf(cidsBean.getProperty("qp_nr"));
            final CidsServerSearch search = new QpWwgrByNr(qpNr);
            final User user = SessionManager.getSession().getUser();
            final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
                        .customServerSearch(user, search);

            if ((nrList != null) && (nrList.size() == 1)) {
                return ((MetaObject)nrList.get(0)).getBean();
            }
        } catch (Exception e) {
            LOG.error("Cannot determine upload user for qp.", e);
            return null;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected CidsBean getWwGrfromFeature(final CidsLayerFeature feature) {
        try {
            final String qpNr = String.valueOf(feature.getProperty("qp_nr"));
            final CidsServerSearch search = new QpWwgrByNr(qpNr);
            final User user = SessionManager.getSession().getUser();
            final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
                        .customServerSearch(user, search);

            if ((nrList != null) && (nrList.size() == 1)) {
                return ((MetaObject)nrList.get(0)).getBean();
            }
        } catch (Exception e) {
            LOG.error("Cannot determine upload user for qp.", e);
            return null;
        }

        return null;
    }

    @Override
    protected String getWwGrPropertyName() {
        return "ww_gr";
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
            if ((wwGr != null)) {
                String owner = (String)wwGr.getProperty("owner");
                String groupName = u.getUserGroup().getName();

                if (owner.endsWith("_edit")) {
                    owner = owner.substring(0, owner.lastIndexOf("_edit"));
                }

                if (groupName.endsWith("_edit")) {
                    groupName = groupName.substring(0, groupName.lastIndexOf("_edit"));
                }

                if (owner.equals(groupName)) {
                    return true;
                } else {
                    return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
                }
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
            if (wwGr != null) {
                String owner = (String)wwGr.getProperty("owner");
                String groupName = u.getUserGroup().getName();

                if (owner.endsWith("_edit")) {
                    owner = owner.substring(0, owner.lastIndexOf("_edit"));
                }

                if (groupName.endsWith("_edit")) {
                    groupName = groupName.substring(0, groupName.lastIndexOf("_edit"));
                }

                if (owner.equals(groupName)) {
                    return true;
                } else {
                    return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
                }
            } else {
                return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
            }
        } else {
            return false;
        }
    }

//    @Override
//    public boolean getCustomWritePermissionDecisionforUser(final User u) {
//        if (u.getUserGroup().getName().equals("Administratoren")) {
//            return true;
//        } else {
//            try {
//                final String qpNr = String.valueOf(cidsBean.getProperty("qp_nr"));
//                String uplUser = qpCache.get(qpNr);
//
//                if (uplUser == null) {
//                    final CidsServerSearch search = new QpUplNameByNr(qpNr);
//                    final User user = SessionManager.getSession().getUser();
//                    final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
//                                .customServerSearch(user, search);
//
//                    if ((nrList.size() == 1) && (nrList.get(0).size() == 1)) {
//                        uplUser = (String)nrList.get(0).get(0);
//                    }
//
//                    qpCache.put(qpNr, uplUser);
//                }
//
//                return (uplUser != null) && uplUser.equals(uplUser);
//            } catch (Exception e) {
//                LOG.error("Cannot determine upload user for qp.", e);
//                return false;
//            }
//        }
//    }
//
//    @Override
//    public boolean getCustomReadPermissionDecisionforUser(final User u) {
//        return true;
//    }
//
//    @Override
//    public boolean getCustomCidsLayerWritePermissionDecisionforUser(final User u, final CidsLayerFeature feature) {
//        if (u.getUserGroup().getName().equals("Administratoren")) {
//            return true;
//        } else {
//            try {
//                final String qpNr = String.valueOf(feature.getProperty("qp_nr"));
//                String uplUser = qpCache.get(qpNr);
//
//                if (uplUser == null) {
//                    final CidsServerSearch search = new QpUplNameByNr(qpNr);
//                    final User user = SessionManager.getSession().getUser();
//                    final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
//                                .customServerSearch(user, search);
//
//                    if ((nrList.size() == 1) && (nrList.get(0).size() == 1)) {
//                        uplUser = (String)nrList.get(0).get(0);
//                    }
//                    qpCache.put(qpNr, uplUser);
//                }
//
//                return (uplUser != null) && uplUser.equals(uplUser);
//            } catch (Exception e) {
//                LOG.error("Cannot determine upload user for qp.", e);
//                return false;
//            }
//        }
//    }
}
