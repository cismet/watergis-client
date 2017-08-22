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

import de.cismet.cids.dynamics.AbstractCustomBeanPermissionProvider;
import de.cismet.cids.dynamics.CidsBean;

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
public class Dlm25wQpPermissionProvider extends WatergisPermissionProvider implements CidsLayerPermissionProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected CidsBean getWwGrBean() {
        return (CidsBean)cidsBean.getProperty("ww_gr");
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
//        final String uplUser = (String)cidsBean.getProperty("upl_name");
//
//        return u.getUserGroup().getName().equals("Administratoren") || u.getName()
//                    .equals(uplUser);
//    }
//
//    @Override
//    public boolean getCustomCidsLayerWritePermissionDecisionforUser(final User u, final CidsLayerFeature feature) {
//        final String uplUser = (String)feature.getProperty("upl_name");
//
//        return u.getUserGroup().getName().equals("Administratoren") || u.getName()
//                    .equals(uplUser);
//    }
//
//    @Override
//    public boolean getCustomReadPermissionDecisionforUser(final User u) {
//        return true;
//    }
}
