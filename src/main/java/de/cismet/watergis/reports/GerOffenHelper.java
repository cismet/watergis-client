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

import org.openide.util.Exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import de.cismet.cids.custom.watergis.server.search.AllGewWithParts;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.reports.types.GmdPartObjOffen;
import de.cismet.watergis.reports.types.KatasterGewObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerOffenHelper {

    //~ Instance fields --------------------------------------------------------

    private List<KatasterGewObj> objList;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GerOffenHelper object.
     *
     * @param   routeIds         DOCUMENT ME!
     * @param   allowedWdmArray  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public GerOffenHelper(final int[] routeIds, final int[] allowedWdmArray) throws Exception {
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
     * @param   allowedWdmArray  gemId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<KatasterGewObj> getAllRoutes(final int[] routeIds, final int[] allowedWdmArray) throws Exception {
        final CidsServerSearch search = new AllGewWithParts(routeIds, allowedWdmArray);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<KatasterGewObj> objList = new ArrayList<KatasterGewObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new KatasterGewObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(7),
                        (String)f.get(8),
                        (String)f.get(9),
                        (Integer)f.get(10),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4)));
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
        for (final KatasterGewObj tmp : objList) {
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
        for (final KatasterGewObj tmp : objList) {
            if (tmp.getId() == gew) {
                return tmp.getGewName();
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

        for (final KatasterGewObj tmp : objList) {
            ts.add(tmp.getId());
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

        for (final KatasterGewObj tmp : objList) {
            ts.add(tmp.getOwner());
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

        for (final KatasterGewObj tmp : objList) {
            if ((tmp.getOwner() != null) && tmp.getOwner().equals(gu)) {
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

//        for (final KatasterGewObj tmp : objList) {
//            if (tmp.getOwner() != null && tmp.getOwner().equals(gu)) {
//                ts.add(tmp.Sb());
//            }
//        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getGuId(final String owner) {
        for (final KatasterGewObj tmp : objList) {
            if (tmp.getOwner().equals(owner)) {
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
    public Collection<KatasterGewObj> getAbschnitte() {
        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<KatasterGewObj> getAbschnitteOffen() {
        final List<KatasterGewObj> abschnList = new ArrayList<KatasterGewObj>();

        for (final KatasterGewObj tmp : objList) {
            if (tmp.getArt().equals("p")) {
                abschnList.add(tmp);
            }
        }
        return abschnList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profObjects  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<KatasterGewObj> getAbschnitteOffen(final List<GmdPartObjOffen> profObjects) {
        final List<KatasterGewObj> abschnList = new ArrayList<KatasterGewObj>();
        int profIndex = 0;

        // profObjects and objList are ordered by baCd and from respectivly baStVon

        for (KatasterGewObj tmp : objList) {
            if (tmp.getArt().equals("p")) {
                if ((profIndex < profObjects.size()) && (tmp.getId() == profObjects.get(profIndex).getId())
                            && profObjects.get(profIndex).isInGewPart(tmp.getId(), tmp.getFrom(), tmp.getTill())) {
                    do {
                        final GmdPartObjOffen profPart = profObjects.get(profIndex++);

                        if (tmp.getFrom() < profPart.getBaStVon()) {
                            try {
                                final KatasterGewObj newObject = tmp.clone();
                                newObject.setTill(profPart.getBaStVon());
                                abschnList.add(newObject);
                                abschnList.add(profPart.toKatasterGewObj());

                                if (tmp.getTill() > profPart.getBaStBis()) {
                                    tmp = tmp.clone();
                                    tmp.setFrom(profPart.getBaStBis());
                                } else {
                                    continue;
                                }
                            } catch (CloneNotSupportedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        } else {
                            // tmp.getFrom() == profPart.getBaStVon();
                            try {
                                abschnList.add(profPart.toKatasterGewObj());

                                if (tmp.getTill() > profPart.getBaStBis()) {
                                    tmp = tmp.clone();
                                    tmp.setFrom(profPart.getBaStBis());
                                } else {
                                    continue;
                                }
                            } catch (CloneNotSupportedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } while ((profIndex < profObjects.size()) && (tmp.getId() == profObjects.get(profIndex).getId())
                                && profObjects.get(profIndex).isInGewPart(tmp.getId(), tmp.getFrom(), tmp.getTill()));

                    abschnList.add(tmp);
                } else {
                    abschnList.add(tmp);
                }
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

        for (final KatasterGewObj tmp : objList) {
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

        for (final KatasterGewObj tmp : objList) {
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

        for (final KatasterGewObj tmp : objList) {
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

        for (final KatasterGewObj tmp : objList) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountOffeneAbschn(final String owner) {
        int count = 0;

        for (final KatasterGewObj tmp : objList) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String owner) {
        double length = 0;

        for (final KatasterGewObj tmp : objList) {
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
     * @param   owner  DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public double getLengthOffeneAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : objList) {
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
     * @param   gu  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountGewAll(final String gu) {
        int count = 0;

        for (final KatasterGewObj tmp : objList) {
            if (tmp.getOwner().equals(gu)) {
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

        for (final KatasterGewObj tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : objList) {
            if (tmp.getOwner().equals(gu)) {
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

        for (final KatasterGewObj tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }
}
