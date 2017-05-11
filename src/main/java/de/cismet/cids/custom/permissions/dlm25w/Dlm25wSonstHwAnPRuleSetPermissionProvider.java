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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Dlm25wSonstHwAnPRuleSetPermissionProvider extends WatergisPermissionProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected CidsBean getWwGrBean() {
        return (CidsBean)cidsBean.getProperty("ww_gr");
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

        if ((wwGr != null) && wwGr.getProperty("owner").equals(u.getUserGroup().getName())) {
            return true;
        } else {
            return (wwGr != null) && wwGr.getProperty("ww_gr").equals(4000);
        }
    }
}
