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
package de.cismet.watergis.reports;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.newuser.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.cismet.cids.custom.watergis.server.search.AllGewByArea;
import de.cismet.cids.custom.watergis.server.search.AllGewByGem;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.reports.types.Flaeche;
import de.cismet.watergis.reports.types.GemeindenDataLightweight;
import de.cismet.watergis.reports.types.GewFlObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerOffenFlHelper {

    //~ Instance fields --------------------------------------------------------

    private final Map<Object, List<GewFlObj>> gemPartMap = new HashMap<Object, List<GewFlObj>>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GerOffenGmdHelper object.
     *
     * @param   flNr             DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GerOffenFlHelper(final Flaeche[] flNr, final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        init(flNr, routeIds, allowedWdmArray);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   flNr             DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final Flaeche[] flNr, final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        for (final Flaeche fl : flNr) {
            gemPartMap.put(fl.getAttr1(), getAllRoutes(fl, routeIds, allowedWdmArray));

            final Integer[] idList = getGew(fl.getAttr1()).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<Object, List<GewFlObj>> getGemPartMap() {
        return gemPartMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fl               gemId DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<GewFlObj> getAllRoutes(final Flaeche fl, final int[] routeIds, final int[] allowedWdmArray)
            throws Exception {
        final CidsServerSearch search = new AllGewByArea(routeIds, allowedWdmArray, fl.getGeom());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GewFlObj> objList = new ArrayList<GewFlObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GewFlObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(5),
                        (String)f.get(6),
                        (String)f.get(7),
                        (Integer)f.get(8),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        fl.getAttr1()));
            }
        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getGew(final Object flNr) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GewFlObj tmp : gemList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final Object flNr) {
        return getCountGewAll(flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final Object flNr) {
        return getLengthGewAll(flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final Object flNr, final int gewId) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                ts.add(tmp.getBaCd());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final Object flNr, final int gewId) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final Object flNr) {
        return getLengthOffeneAbschn(flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final Object flNr, final int gewId) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("p")) {
                    length += tmp.getLength();
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final Object flNr, final int gewId, final double from, final double till) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("p")) {
                    length += tmp.getLengthInGewPart(gewId, from, till);
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountOffeneAbschn(final Object flNr, final String owner) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                if (tmp.getArt().equals("p")) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final Object flNr, final String owner) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                if (tmp.getArt().equals("p")) {
                    length += tmp.getLength();
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final Object flNr, final String owner, final Integer wdm) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                if (tmp.getArt().equals("p")) {
                    length += tmp.getLength();
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final Object flNr, final String gu) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   flNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final Object flNr, final String gu, final Integer wdm) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final Object flNr, final String gu) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final Object flNr, final String gu, final Integer wdm) {
        final List<GewFlObj> gemList = gemPartMap.get(flNr);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }
}
