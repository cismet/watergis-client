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
public class Dlm25wFotoPermissionProvider extends AbstractCustomBeanPermissionProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean getCustomWritePermissionDecisionforUser(final User u) {
        final String uplUser = (String)cidsBean.getProperty("upl_name");

        return u.getUserGroup().getName().equals("Administratoren") || u.getName()
                    .equals(uplUser);
    }

    @Override
    public boolean getCustomReadPermissionDecisionforUser(final User u) {
        return true;
    }
}
