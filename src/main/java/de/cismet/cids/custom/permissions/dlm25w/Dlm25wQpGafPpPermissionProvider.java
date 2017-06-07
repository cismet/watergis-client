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

import Sirius.server.newuser.User;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.custom.watergis.server.search.QpUplNameByNr;

import de.cismet.cids.dynamics.AbstractCustomBeanPermissionProvider;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayerFeature;
import de.cismet.cismap.cidslayer.CidsLayerPermissionProvider;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Dlm25wQpGafPpPermissionProvider extends AbstractCustomBeanPermissionProvider
        implements CidsLayerPermissionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(Dlm25wQpGafPpPermissionProvider.class);
    private static final Map<String, String> qpCache = new HashMap<String, String>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean getCustomWritePermissionDecisionforUser(final User u) {
        if (u.getUserGroup().getName().equals("Administratoren")) {
            return true;
        } else {
            try {
                final String qpNr = String.valueOf(cidsBean.getProperty("qp_nr"));
                String uplUser = qpCache.get(qpNr);

                if (uplUser == null) {
                    final CidsServerSearch search = new QpUplNameByNr(qpNr);
                    final User user = SessionManager.getSession().getUser();
                    final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(user, search);

                    if ((nrList.size() == 1) && (nrList.get(0).size() == 1)) {
                        uplUser = (String)nrList.get(0).get(0);
                    }

                    qpCache.put(qpNr, uplUser);
                }

                return (uplUser != null) && uplUser.equals(uplUser);
            } catch (Exception e) {
                LOG.error("Cannot determine upload user for qp.", e);
                return false;
            }
        }
    }

    @Override
    public boolean getCustomReadPermissionDecisionforUser(final User u) {
        return true;
    }

    @Override
    public boolean getCustomCidsLayerWritePermissionDecisionforUser(final User u, final CidsLayerFeature feature) {
        if (u.getUserGroup().getName().equals("Administratoren")) {
            return true;
        } else {
            try {
                final String qpNr = String.valueOf(feature.getProperty("qp_nr"));
                String uplUser = qpCache.get(qpNr);

                if (uplUser == null) {
                    final CidsServerSearch search = new QpUplNameByNr(qpNr);
                    final User user = SessionManager.getSession().getUser();
                    final ArrayList<ArrayList> nrList = (ArrayList<ArrayList>)SessionManager.getProxy()
                                .customServerSearch(user, search);

                    if ((nrList.size() == 1) && (nrList.get(0).size() == 1)) {
                        uplUser = (String)nrList.get(0).get(0);
                    }
                    qpCache.put(qpNr, uplUser);
                }

                return (uplUser != null) && uplUser.equals(uplUser);
            } catch (Exception e) {
                LOG.error("Cannot determine upload user for qp.", e);
                return false;
            }
        }
    }
}
