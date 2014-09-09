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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class WatergisPermissionProvider extends AbstractCustomBeanPermissionProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(
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

        if ((wwGr != null) && wwGr.getProperty("owner").equals(u.getUserGroup().getName())) {
            return true;
        } else {
            return (wwGr == null) || wwGr.getProperty("ww_gr").equals(4000);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract CidsBean getWwGrBean();
}
