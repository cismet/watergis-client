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
package de.cismet.watergis.reports.types;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

import java.util.ArrayList;

import de.cismet.cids.custom.watergis.server.search.GmdNameByNumber;

import de.cismet.cids.server.search.CidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GemeindenDataLightweight {

    //~ Instance fields --------------------------------------------------------

    private String gmdName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GemeindenData object.
     *
     * @param   fl   gemNr DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GemeindenDataLightweight(final Flaeche fl, final int[] gew) throws Exception {
        gmdName = String.valueOf(fl.getAttr2());
    }
    /**
     * Creates a new GemeindenData object.
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GemeindenDataLightweight(final int gemNr, final int[] gew) throws Exception {
        gmdName = retrieveGmdName(gemNr);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGmdName() {
        return gmdName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gmdNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private String retrieveGmdName(final int gmdNr) throws Exception {
        final CidsServerSearch search = new GmdNameByNumber(gmdNr);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                return (String)f.get(0);
            }
        }

        return null;
    }
}
