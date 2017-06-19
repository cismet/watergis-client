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

import de.cismet.cids.custom.watergis.server.search.AllGewByGem;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.reports.types.GemeindenDataLightweight;
import de.cismet.watergis.reports.types.GmdPartObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerOffenGmdHelper {

    //~ Instance fields --------------------------------------------------------

    private final Map<Integer, List<GmdPartObj>> gemPartMap = new HashMap<Integer, List<GmdPartObj>>();
    private final Map<Integer, GemeindenDataLightweight> gemDataMap = new HashMap<Integer, GemeindenDataLightweight>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GerOffenGmdHelper object.
     *
     * @param   gemNr            DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GerOffenGmdHelper(final int[] gemNr, final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        init(gemNr, routeIds, allowedWdmArray);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr            DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final int[] gemNr, final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        for (final int gem : gemNr) {
            gemPartMap.put(gem, getAllRoutes(gem, routeIds, allowedWdmArray));

            final Integer[] idList = getGew(gem).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
            gemDataMap.put(gem, new GemeindenDataLightweight(gem, routes));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Map<Integer, List<GmdPartObj>> getGemPartMap() {
        return gemPartMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemId            DOCUMENT ME!
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<GmdPartObj> getAllRoutes(final int gemId, final int[] routeIds, final int[] allowedWdmArray)
            throws Exception {
        final CidsServerSearch search = new AllGewByGem(gemId, routeIds, allowedWdmArray);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GmdPartObj> objList = new ArrayList<GmdPartObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GmdPartObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(7),
                        (String)f.get(8),
                        (String)f.get(9),
                        (Integer)f.get(10),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        (Integer)f.get(5),
                        (Integer)f.get(6),
                        (Double)f.get(11),
                        (Double)f.get(12)));
            }
        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getGew(final int gemNr) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObj tmp : gemList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final int gemNr) {
        return getCountGewAll(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final int gemNr) {
        return getLengthGewAll(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                ts.add(tmp.getBaCd());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gemNr) {
        return getLengthOffeneAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountOffeneAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gemNr, final String owner, final Integer wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final int gemNr, final String gu) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(gu)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final int gemNr, final String gu, final Integer wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final int gemNr, final String gu) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(gu)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final int gemNr, final String gu, final Integer wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }
}
