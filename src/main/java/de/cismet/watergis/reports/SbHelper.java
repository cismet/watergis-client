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
import java.util.List;
import java.util.TreeSet;

import de.cismet.cids.custom.watergis.server.search.AllGewBySb;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.reports.types.SbObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SbHelper {

    //~ Instance fields --------------------------------------------------------

    private List<SbObj> objList;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SbHelper object.
     *
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public SbHelper(final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        init(routeIds, allowedWdmArray);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        objList = getAllRoutes(routeIds, allowedWdmArray);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<SbObj> getAllRoutes(final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        final CidsServerSearch search = new AllGewBySb(routeIds, allowedWdmArray);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<SbObj> objList = new ArrayList<SbObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new SbObj(
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
                        (String)f.get(6),
                        (Double)f.get(11),
                        (String)f.get(12)));
            }
        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getBaCd(final int gew) {
        for (final SbObj tmp : objList) {
            if (tmp.getId() == gew) {
                return tmp.getBaCd();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGewName(final int gew) {
        for (final SbObj tmp : objList) {
            if (tmp.getId() == gew) {
                return tmp.getGewName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getSbName(final Integer sb) {
        for (final SbObj tmp : objList) {
            if ((tmp.getSb() != null) && tmp.getSb().equals(sb)) {
                return tmp.getSbName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getGew() {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : objList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getGew(final String gu, final Integer wdm, final Integer sb) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm) && (tmp.getSb() != null)
                        && tmp.getSb().equals(sb)) {
                ts.add(tmp.getId());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<String> getGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj tmp : objList) {
            ts.add(tmp.getGuName());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getWidmung(final String gu) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : objList) {
            if ((tmp.getGuName() != null) && tmp.getGuName().equals(gu)) {
                ts.add(tmp.getWidmung());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Integer> getSb(final String gu, final Integer wdm) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : objList) {
            if ((tmp.getGuName() != null) && tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)) {
                ts.add(tmp.getSb());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Integer getSbCount(final String gu) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : objList) {
            if ((tmp.getGuName() != null) && tmp.getGuName().equals(gu)) {
                ts.add(tmp.getSb());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SbObj> getSbPart(final String gu, final Integer wdm, final Integer sb) {
        final List<SbObj> ts = new ArrayList<SbObj>();

        for (final SbObj tmp : objList) {
            if ((tmp.getGuName() != null) && tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)
                        && (tmp.getSb() != null) && tmp.getSb().equals(sb)) {
                ts.add(tmp);
            }
        }

        return ts;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu     gemNr DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SbObj> getSbPart(final String gu, final Integer wdm, final Integer sb, final Integer gewId) {
        final List<SbObj> ts = new ArrayList<SbObj>();

        for (final SbObj tmp : objList) {
            if ((tmp.getId() == gewId) && (tmp.getGuName() != null) && tmp.getGuName().equals(gu)
                        && (tmp.getWidmung() == wdm)
                        && (tmp.getSb() != null) && tmp.getSb().equals(sb)) {
                ts.add(tmp);
            }
        }

        return ts;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGuId(final String owner) {
        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(owner)) {
                return tmp.getGu();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SbObj> getAbschnitte() {
        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<SbObj> getAbschnitteOffen() {
        final List<SbObj> abschnList = new ArrayList<SbObj>();

        for (final SbObj tmp : objList) {
            if (tmp.getArt().equals("p")) {
                abschnList.add(tmp);
            }
        }
        return abschnList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll() {
        return getCountGewAll(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll() {
        return getLengthGewAll(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final int gewId) {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj tmp : objList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                ts.add(tmp.getBaCd());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final int gewId) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn() {
        return getLengthOffeneAbschn(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gewId) {
        double length = 0;

        for (final SbObj tmp : objList) {
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
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final int gewId, final double from, final double till) {
        double length = 0;

        for (final SbObj tmp : objList) {
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
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  gewId DOCUMENT ME!
     * @param   sb   from DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String gu, final int wdm, final int sb) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if ((tmp.getGuName() != null) && tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)
                        && (tmp.getSb() != null) && tmp.getSb().equals(sb)) {
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
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  gewId DOCUMENT ME!
     * @param   sb   from DOCUMENT ME!
     * @param   gew  till DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String gu, final int wdm, final int sb, final Integer gew) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if ((tmp.getId() == gew) && (tmp.getGuName() != null) && tmp.getGuName().equals(gu)
                        && (tmp.getWidmung() == wdm)
                        && (tmp.getSb() != null) && tmp.getSb().equals(sb)) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountOffeneAbschn(final String owner) {
        int count = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(owner)) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String owner) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(owner)) {
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
     * @param   owner  DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
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
     * @param   gu  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final String gu) {
        int count = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final String gu, final Integer wdm) {
        int count = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  gemNr DOCUMENT ME!
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final String gu, final Integer wdm, final Integer sb) {
        int count = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm) && (tmp.getSb() != null)
                        && tmp.getSb().equals(sb)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu     DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final String gu, final Integer wdm, final Integer sb, final Integer gewId) {
        int count = 0;

        for (final SbObj tmp : objList) {
            if ((tmp.getId() == gewId) && tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)
                        && (tmp.getSb() != null) && tmp.getSb().equals(sb)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final String gu) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final String gu, final Integer wdm) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  gemNr DOCUMENT ME!
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final String gu, final Integer wdm, final Integer sb) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)
                        && ((tmp.getSb() != null) && tmp.getSb().equals(sb))) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu     DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthGewAll(final String gu, final Integer wdm, final Integer sb, final Integer gewId) {
        double length = 0;

        for (final SbObj tmp : objList) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)
                        && ((tmp.getSb() != null) && tmp.getSb().equals(sb))) {
                length += tmp.getLength();
            }
        }

        return length;
    }
}
