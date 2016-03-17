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

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class Dlm25wFgBakAePermissionProvider extends WatergisPermissionProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    protected CidsBean getWwGrBean() {
        return (CidsBean)cidsBean.getProperty("bak_st.von.route.ww_gr");
    }
}
