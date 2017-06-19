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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.cismet.cids.custom.watergis.server.search.AllLineObjects;
import de.cismet.cids.custom.watergis.server.search.AllPunktObjects;
import de.cismet.cids.custom.watergis.server.search.GmdNameByNumber;
import de.cismet.cids.custom.watergis.server.search.SchuUeberReport;
import de.cismet.cids.custom.watergis.server.search.SchuWasserReport;
import de.cismet.cids.custom.watergis.server.search.SeeReport;

import de.cismet.cids.server.search.CidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterFlaechenData {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum LineFromPolygonTable {

        //~ Enum constants -----------------------------------------------------

        sg_see, wr_sg_wsg, wr_sg_uesg
    }

    //~ Instance fields --------------------------------------------------------

    private Map<LineFromPolygonTable, List<LineObjectData>> lineFromPolygonMap =
        new EnumMap<LineFromPolygonTable, List<LineObjectData>>(LineFromPolygonTable.class);
    private Map<AllLineObjects.Table, List<LineObjectData>> lineMap =
        new EnumMap<AllLineObjects.Table, List<LineObjectData>>(AllLineObjects.Table.class);
    private Map<AllPunktObjects.Table, List<PointObjectData>> pointMap =
        new EnumMap<AllPunktObjects.Table, List<PointObjectData>>(AllPunktObjects.Table.class);
    private Object attr2;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GemeindenData object.
     *
     * @param   attr   gemNr DOCUMENT ME!
     * @param   attr2  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public KatasterFlaechenData(final Object attr, final Object attr2, final int[] gew) throws Exception {
        this.attr2 = attr2;
        init(gew);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final int[] gew) throws Exception {
        lineMap.put(AllLineObjects.Table.fg_ba_d, createDataObjects(AllLineObjects.Table.fg_ba_d, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_anll, createDataObjects(AllLineObjects.Table.fg_ba_anll, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_bbef, createDataObjects(AllLineObjects.Table.fg_ba_bbef, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_due, createDataObjects(AllLineObjects.Table.fg_ba_due, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_gb, createDataObjects(AllLineObjects.Table.fg_ba_gb, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_leis, createDataObjects(AllLineObjects.Table.fg_ba_leis, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_prof, createDataObjects(AllLineObjects.Table.fg_ba_prof, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_rl, createDataObjects(AllLineObjects.Table.fg_ba_rl, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_sb, createDataObjects(AllLineObjects.Table.fg_ba_sb, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_sbef, createDataObjects(AllLineObjects.Table.fg_ba_sbef, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_tech, createDataObjects(AllLineObjects.Table.fg_ba_tech, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_ubef, createDataObjects(AllLineObjects.Table.fg_ba_ubef, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_ughz, createDataObjects(AllLineObjects.Table.fg_ba_ughz, gew));
        lineMap.put(AllLineObjects.Table.fg_ba_deich, createDataObjects(AllLineObjects.Table.fg_ba_deich, gew));

        pointMap.put(AllPunktObjects.Table.fg_ba_anlp, createPointDataObjects(AllPunktObjects.Table.fg_ba_anlp, gew));
        pointMap.put(AllPunktObjects.Table.wr_wbu_aus, createPointDataObjects(AllPunktObjects.Table.wr_wbu_aus, gew));
        pointMap.put(AllPunktObjects.Table.wr_wbu_ben, createPointDataObjects(AllPunktObjects.Table.wr_wbu_ben, gew));
        pointMap.put(AllPunktObjects.Table.fg_ba_kr, createPointDataObjects(AllPunktObjects.Table.fg_ba_kr, gew));
        pointMap.put(AllPunktObjects.Table.mn_ow_pegel, createPointDataObjects(AllPunktObjects.Table.mn_ow_pegel, gew));
        pointMap.put(AllPunktObjects.Table.fg_ba_scha, createPointDataObjects(AllPunktObjects.Table.fg_ba_scha, gew));
        pointMap.put(AllPunktObjects.Table.fg_ba_schw, createPointDataObjects(AllPunktObjects.Table.fg_ba_schw, gew));
        pointMap.put(AllPunktObjects.Table.fg_ba_wehr, createPointDataObjects(AllPunktObjects.Table.fg_ba_wehr, gew));
        pointMap.put(AllPunktObjects.Table.fg_ba_ea, createPointDataObjects(AllPunktObjects.Table.fg_ba_ea, gew));

        lineFromPolygonMap.put(LineFromPolygonTable.sg_see, createSee(gew));
        lineFromPolygonMap.put(LineFromPolygonTable.wr_sg_uesg, createSchutzUeber(gew));
        lineFromPolygonMap.put(LineFromPolygonTable.wr_sg_wsg, createSchutzWasser(gew));
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getAttr2() {
        return attr2;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCount(final AllPunktObjects.Table table, final int gew, final double from, final double till) {
        final List<PointObjectData> points = pointMap.get(table);
        int count = 0;

        if (points != null) {
            for (final PointObjectData pointObj : points) {
                if (pointObj.isInGewPart(gew, from, till)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCount(final AllLineObjects.Table table, final int gew, final double from, final double till) {
        final List<LineObjectData> lines = lineMap.get(table);
        int count = 0;

        if (lines != null) {
            for (final LineObjectData lineObj : lines) {
                if (lineObj.isInGewPart(gew, from, till)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getLength(final AllLineObjects.Table table, final int gew, final double from, final double till) {
        final List<LineObjectData> lines = lineMap.get(table);
        int length = 0;

        if (lines != null) {
            for (final LineObjectData lineObj : lines) {
                length += lineObj.getLengthInGewPart(gew, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCount(final LineFromPolygonTable table, final int gew, final double from, final double till) {
        final List<LineObjectData> lines = lineFromPolygonMap.get(table);
        int count = 0;

        if (lines != null) {
            for (final LineObjectData lineObj : lines) {
                if (lineObj.isInGewPart(gew, from, till)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getLength(final LineFromPolygonTable table, final int gew, final double from, final double till) {
        final List<LineObjectData> lines = lineFromPolygonMap.get(table);
        int length = 0;

        if (lines != null) {
            for (final LineObjectData lineObj : lines) {
                length += lineObj.getLengthInGewPart(gew, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<LineObjectData> createSchutzWasser(final int[] gew) throws Exception {
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, new SchuWasserReport(gew));
        final List<LineObjectData> objList = new ArrayList<LineObjectData>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new LineObjectData(
                        (Integer)f.get(3),
                        (String)f.get(2),
                        (Double)f.get(0),
                        (Double)f.get(1),
                        (Double)f.get(1)
                                - (Double)f.get(0)));
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
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<LineObjectData> createSchutzUeber(final int[] gew) throws Exception {
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, new SchuUeberReport(gew));
        final List<LineObjectData> objList = new ArrayList<LineObjectData>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new LineObjectData(
                        (Integer)f.get(3),
                        (String)f.get(2),
                        (Double)f.get(0),
                        (Double)f.get(1),
                        (Double)f.get(1)
                                - (Double)f.get(0)));
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
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<LineObjectData> createSee(final int[] gew) throws Exception {
//        final User user = SessionManager.getSession().getUser();
//        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
//                    .customServerSearch(user, new SeeReport(gew));
        final List<LineObjectData> objList = new ArrayList<LineObjectData>();

//        if ((attributes != null) && !attributes.isEmpty()) {
//            for (final ArrayList f : attributes) {
//                objList.add(new LineObjectData(
//                        (Integer)f.get(3),
//                        (String)f.get(2),
//                        (Double)f.get(0),
//                        (Double)f.get(1),
//                        (Double)f.get(1)
//                                - (Double)f.get(0)));
//            }
//        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<LineObjectData> createDataObjects(final AllLineObjects.Table table, final int[] gew) throws Exception {
        final CidsServerSearch search = new AllLineObjects(table, gew);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<LineObjectData> objList = new ArrayList<LineObjectData>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new LineObjectData(
                        (Integer)f.get(0),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        (Double)f.get(1)));
            }
        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<PointObjectData> createPointDataObjects(final AllPunktObjects.Table table, final int[] gew)
            throws Exception {
        final CidsServerSearch search = new AllPunktObjects(table, gew);
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<PointObjectData> objList = new ArrayList<PointObjectData>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new PointObjectData((Integer)f.get(0), (String)f.get(1), (Double)f.get(2)));
            }
        }

        return objList;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PointObjectData {

        //~ Instance fields ----------------------------------------------------

        private int gewId;
        private String baCd;
        private double from;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PointObjectData object.
         */
        public PointObjectData() {
        }

        /**
         * Creates a new PointObjectData object.
         *
         * @param  gewId  DOCUMENT ME!
         * @param  baCd   DOCUMENT ME!
         * @param  from   DOCUMENT ME!
         */
        public PointObjectData(final int gewId, final String baCd, final double from) {
            this.gewId = gewId;
            this.baCd = baCd;
            this.from = from;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   gew   DOCUMENT ME!
         * @param   from  DOCUMENT ME!
         * @param   till  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isInGewPart(final int gew, final double from, final double till) {
            return (gew == gewId) && (Math.min(from, till) <= this.from) && (Math.max(from, till) >= from);
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the baCd
         */
        public String getBaCd() {
            return baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  baCd  the baCd to set
         */
        public void setBaCd(final String baCd) {
            this.baCd = baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the from
         */
        public double getFrom() {
            return from;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  from  the from to set
         */
        public void setFrom(final double from) {
            this.from = from;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class LineObjectData {

        //~ Instance fields ----------------------------------------------------

        private int gewId;
        private String baCd;
        private double from;
        private double to;
        private double length;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new LineObjectData object.
         */
        public LineObjectData() {
        }

        /**
         * Creates a new LineObjectData object.
         *
         * @param  gewId   DOCUMENT ME!
         * @param  baCd    DOCUMENT ME!
         * @param  from    DOCUMENT ME!
         * @param  to      DOCUMENT ME!
         * @param  length  DOCUMENT ME!
         */
        public LineObjectData(final int gewId,
                final String baCd,
                final double from,
                final double to,
                final double length) {
            this.gewId = gewId;
            this.baCd = baCd;
            this.from = from;
            this.to = to;
            this.length = length;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   gew   DOCUMENT ME!
         * @param   from  DOCUMENT ME!
         * @param   till  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public boolean isInGewPart(final int gew, final double from, final double till) {
            return (gew == gewId)
                        && ((Math.min(Math.max(this.from, this.to), Math.max(from, till))
                                - Math.max(Math.min(this.from, this.to), Math.min(from, till))) > 0.1);
        }

        /**
         * DOCUMENT ME!
         *
         * @param   gew   DOCUMENT ME!
         * @param   from  DOCUMENT ME!
         * @param   till  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public double getLengthInGewPart(final int gew, final double from, final double till) {
            if (gew == gewId) {
                return Math.max(
                        0.0,
                        Math.min(Math.max(this.from, this.to), Math.max(from, till))
                                - Math.max(Math.min(this.from, this.to), Math.min(from, till)));
            } else {
                return 0;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the baCd
         */
        public String getBaCd() {
            return baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  baCd  the baCd to set
         */
        public void setBaCd(final String baCd) {
            this.baCd = baCd;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the from
         */
        public double getFrom() {
            return from;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  from  the from to set
         */
        public void setFrom(final double from) {
            this.from = from;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the to
         */
        public double getTo() {
            return to;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  to  the to to set
         */
        public void setTo(final double to) {
            this.to = to;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the length
         */
        public double getLength() {
            return length;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  length  the length to set
         */
        public void setLength(final double length) {
            this.length = length;
        }
    }
}
