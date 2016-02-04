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
package de.cismet.watergis.utils;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import org.apache.log4j.Logger;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Image;
import java.awt.geom.Point2D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.vecmath.Point3d;

import de.cismet.cids.custom.watergis.server.search.GafCatalogueValues;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GafReader {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GafReader.class);
    private static String[] allKz;
    private static String[] allHYK;
    private static int[] allRK;
    private static int[] allBK;
    private static String[][] allKzHykPerms;
    private static final Map<String, String> kzAbbrevs = new HashMap<String, String>();
    private static final Color UK_COLOR = new Color(180, 0, 0);
    private static final Color OK_COLOR = new Color(255, 80, 80);

    static {
        kzAbbrevs.put("LDOK", "0");
        kzAbbrevs.put("LDUK", "1");
        kzAbbrevs.put("LBOK", "2");
        kzAbbrevs.put("LBUK", "3");
        kzAbbrevs.put("LU", "4");
        kzAbbrevs.put("RU", "5");
        kzAbbrevs.put("RBUK", "6");
        kzAbbrevs.put("RBOK", "7");
        kzAbbrevs.put("RDUK", "8");
        kzAbbrevs.put("RDOK", "9");
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static enum GAF_FIELDS {

        //~ Enum constants -----------------------------------------------------

        STATION, ID, Y, Z, KZ, RK, BK, HW, RW, HYK
    }

    //~ Instance fields --------------------------------------------------------

    private String[] header;
    private final ArrayList<String[]> content = new ArrayList<String[]>();
    private final Map<Double, ArrayList<String[]>> profiles = new HashMap<Double, ArrayList<String[]>>();
    private final int[] gafIndex = new int[10];
    private final List<CidsBean> rk = new ArrayList<CidsBean>();
    private final List<CidsBean> bk = new ArrayList<CidsBean>();
    private final List<CidsBean> kz = new ArrayList<CidsBean>();
    private boolean catalogueInitialised = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GafReader object.
     *
     * @param  gafFile  DOCUMENT ME!
     */
    public GafReader(final File gafFile) {
        init(gafFile);
    }

    /**
     * Creates a new GafReader object.
     *
     * @param  gafFeatures  gafFile DOCUMENT ME!
     */
    public GafReader(final List<DefaultFeatureServiceFeature> gafFeatures) {
        initFromFeatures(1., gafFeatures);
    }

    /**
     * Creates a new GafReader object.
     *
     * @param  gafFeaturesMap  gafFile DOCUMENT ME!
     */
    public GafReader(final Map<Double, List<DefaultFeatureServiceFeature>> gafFeaturesMap) {
        for (final Double profileId : gafFeaturesMap.keySet()) {
            initFromFeatures(profileId, gafFeaturesMap.get(profileId));
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private synchronized void initCatalogues() {
        if (!catalogueInitialised) {
            final MetaClass rkMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_rk");
            final MetaClass kzMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_kz");
            final MetaClass bkMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.k_qp_gaf_bk");

            try {
                final String queryRk = "select " + rkMc.getID() + ", " + rkMc.getPrimaryKey() + " from "
                            + rkMc.getTableName(); // NOI18N
                final String queryKz = "select " + kzMc.getID() + ", " + kzMc.getPrimaryKey() + " from "
                            + kzMc.getTableName(); // NOI18N
                final String queryBk = "select " + bkMc.getID() + ", " + bkMc.getPrimaryKey() + " from "
                            + bkMc.getTableName(); // NOI18N

                final MetaObject[] moRk = SessionManager.getProxy().getMetaObjectByQuery(queryRk, 0);
                final MetaObject[] moBk = SessionManager.getProxy().getMetaObjectByQuery(queryBk, 0);
                final MetaObject[] moKz = SessionManager.getProxy().getMetaObjectByQuery(queryKz, 0);

                if (moRk != null) {
                    for (final MetaObject mo : moRk) {
                        rk.add(mo.getBean());
                    }
                }

                if (moBk != null) {
                    for (final MetaObject mo : moBk) {
                        bk.add(mo.getBean());
                    }
                }

                if (moKz != null) {
                    for (final MetaObject mo : moKz) {
                        kz.add(mo.getBean());
                    }
                }
            } catch (Exception e) {
                LOG.error("Cannot receive gaf catalogue meta objects.", e);
            }

            catalogueInitialised = true;
        }
    }

    /**
     * DOCUMENT ME!
     */
    private static synchronized void initialiseCatalogueValues() {
        if (allKz == null) {
            try {
                final User user = SessionManager.getSession().getUser();
                final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                            .customServerSearch(user, new GafCatalogueValues());

                if ((attributes != null) && !attributes.isEmpty()) {
                    final ArrayList<ArrayList> kz = attributes.get(0);
                    final ArrayList<ArrayList> rk = attributes.get(1);
                    final ArrayList<ArrayList> bk = attributes.get(2);
                    final List<String> hyk = new ArrayList<String>();
                    final List<String[]> kzHykPerms = new ArrayList<String[]>();

                    allKz = new String[kz.size()];
                    allRK = new int[rk.size()];
                    allBK = new int[bk.size()];

                    for (int i = 0; i < kz.size(); ++i) {
                        allKz[i] = (String)kz.get(i).get(0);

                        if (kz.get(i).get(1) instanceof String) {
                            hyk.add((String)kz.get(i).get(1));
                            kzHykPerms.add(new String[] { allKz[i], (String)kz.get(i).get(1) });
                        }
                    }

                    for (int i = 0; i < rk.size(); ++i) {
                        allRK[i] = (Integer)rk.get(i).get(0);
                    }

                    for (int i = 0; i < bk.size(); ++i) {
                        allBK[i] = (Integer)bk.get(i).get(0);
                    }

                    allHYK = hyk.toArray(new String[hyk.size()]);
                    allKzHykPerms = kzHykPerms.toArray(new String[0][0]);
                }
            } catch (Exception e) {
                LOG.error("Error while reading GAF catalogue values.", e);
                allKz = new String[0];
                allHYK = new String[0];
                allRK = new int[0];
                allBK = new int[0];
                allKzHykPerms = new String[0][0];
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  gafFile  DOCUMENT ME!
     */
    private void init(final File gafFile) {
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(gafFile));
            final String headerLine = reader.readLine();
            final List<String> headers = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(headerLine, " \t");
            int index = 0;

            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                headers.add(token);

                try {
                    final GAF_FIELDS field = GAF_FIELDS.valueOf(token.toUpperCase());
                    gafIndex[field.ordinal()] = index;
                } catch (IllegalArgumentException e) {
                    // nothing to do
                }
                ++index;
            }
            header = headers.toArray(new String[headers.size()]);

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.length() == 1) {
                    // end of file. The last line contains a single character
                    break;
                }
                st = new StringTokenizer(line, " \t");
                final List<String> contFields = new ArrayList<String>();

                while (st.hasMoreTokens()) {
                    final String token = st.nextToken();
                    contFields.add(token);
                }
                final String[] contentFields = contFields.toArray(new String[contFields.size()]);

                content.add(contentFields);

                final Double station = getStationNumber(contentFields);

                ArrayList<String[]> profile = profiles.get(station);

                if (profile == null) {
                    profile = new ArrayList<String[]>();
                    profiles.put(station, profile);
                }

                profile.add(contentFields);
            }
        } catch (Exception e) {
            LOG.error("Error while reading GAF file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    LOG.error("Cannot close reader", ex);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  profileId  DOCUMENT ME!
     * @param  features   DOCUMENT ME!
     */
    private void initFromFeatures(final Double profileId, final List<DefaultFeatureServiceFeature> features) {
        try {
            for (final DefaultFeatureServiceFeature feature : features) {
                final String[] contentFields = new String[10];
                contentFields[GAF_FIELDS.Y.ordinal()] = objectToString(feature.getProperty("y"), "-1");
                contentFields[GAF_FIELDS.Z.ordinal()] = objectToString(feature.getProperty("z"), "-1");
                contentFields[GAF_FIELDS.ID.ordinal()] = objectToString(feature.getProperty("id_gaf"), "-1");
                contentFields[GAF_FIELDS.KZ.ordinal()] = objectToString(feature.getProperty("kz"), "x");
                contentFields[GAF_FIELDS.RK.ordinal()] = objectToString(feature.getProperty("rk"), "x");
                contentFields[GAF_FIELDS.BK.ordinal()] = objectToString(feature.getProperty("bk"), "x");
                contentFields[GAF_FIELDS.HW.ordinal()] = objectToString(feature.getProperty("hw"), "-1");
                contentFields[GAF_FIELDS.RW.ordinal()] = objectToString(feature.getProperty("rw"), "-1");
                contentFields[GAF_FIELDS.HYK.ordinal()] = objectToString(feature.getProperty("hyk"), "x");
                contentFields[GAF_FIELDS.STATION.ordinal()] = stationObjectToString(profileId, "1.0");

                content.add(contentFields);

                for (int i = 0; i < 10; ++i) {
                    gafIndex[i] = i;
                }

                final Double station = getStationNumber(contentFields);

                ArrayList<String[]> profile = profiles.get(station);

                if (profile == null) {
                    profile = new ArrayList<String[]>();
                    profiles.put(station, profile);
                }

                profile.add(contentFields);
            }
        } catch (Exception e) {
            LOG.error("Error while reading GAF data from features", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     * @param   width    DOCUMENT ME!
     * @param   height   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Image createImage(final Double profile, final int width, final int height) {
        final ChartCreator chart = new ChartCreator();

        final ArrayList<String[]> gaf = profiles.get(profile);
        final List<ChartCreator.Point> pointList = new ArrayList<ChartCreator.Point>();
        double water = Double.MIN_VALUE;
        final List<ChartCreator.Point> schlamm = new ArrayList<ChartCreator.Point>();
        final List<ChartCreator.Point> baUk = new ArrayList<ChartCreator.Point>();
        final List<ChartCreator.Point> baOk = new ArrayList<ChartCreator.Point>();
        final List<Point2D> eiprof = new ArrayList<Point2D>();
        final List<Point2D> kprof = new ArrayList<Point2D>();
        final List<Point2D> maprof = new ArrayList<Point2D>();
        double xStart = 0;
        double xEnd = 0;
        boolean started = false;

        for (final String[] line : gaf) {
            try {
                final String kzVal = line[gafIndex[GAF_FIELDS.KZ.ordinal()]];

                if (kzVal.equalsIgnoreCase("PA")) {
                    xStart = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    started = true;
                }
                if (started) {
                    final double x = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    final double y = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    final ChartCreator.Point point = new ChartCreator.Point(new Color(234, 156, 39), x, y, true, false);
                    pointList.add(point);

                    final ChartCreator.Point pointWithLine = new ChartCreator.Point(
                            Color.BLACK,
                            x,
                            y,
                            false,
                            true);
                    pointWithLine.setStroke(new BasicStroke(
                            1,
                            BasicStroke.CAP_SQUARE,
                            BasicStroke.JOIN_MITER,
                            2,
                            new float[] { 1f, 3f },
                            0));
                    chart.addPoint(pointWithLine);
                }
                if (kzVal.equalsIgnoreCase("PE")) {
                    xEnd = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    started = false;
                }

                if (kzVal.equalsIgnoreCase("WS")) {
                    water = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                }

                if (kzVal.equalsIgnoreCase("EIUK")) {
                    final double eiz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    final double eiy = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    eiprof.add(new Point2D.Double(eiy, eiz));
                }

                if (kzVal.equalsIgnoreCase("EIFS")) {
                    final double eiz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    eiprof.add(new Point2D.Double(Double.MIN_VALUE, eiz));
                }

                if (kzVal.equalsIgnoreCase("KRUK")) {
                    final double krz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    final double kry = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    kprof.add(new Point2D.Double(kry, krz));
                }

                if (kzVal.equalsIgnoreCase("KRFS")) {
                    final double krz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    kprof.add(new Point2D.Double(Double.MIN_VALUE, krz));
                }

                if (kzVal.equalsIgnoreCase("MAUK")) {
                    final double maz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    final double may = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                    maprof.add(new Point2D.Double(may, maz));
                }

                if (kzVal.equalsIgnoreCase("MAFS")) {
                    final double maz = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                    maprof.add(new Point2D.Double(Double.MIN_VALUE, maz));
                }

                if (kzVal.equalsIgnoreCase("LDOK")
                            || kzVal.equalsIgnoreCase("LDUK")
                            || kzVal.equalsIgnoreCase("LBOK")
                            || kzVal.equalsIgnoreCase("LBUK")
                            || kzVal.equalsIgnoreCase("LU")
                            || kzVal.equalsIgnoreCase("RU")
                            || kzVal.equalsIgnoreCase("RBUK")
                            || kzVal.equalsIgnoreCase("RBOK")
                            || kzVal.equalsIgnoreCase("RDUK")
                            || kzVal.equalsIgnoreCase("RDOK")) {
                    final String text = kzAbbrevs.get(kzVal);
                    final ChartCreator.Point point = new ChartCreator.Point(
                            Color.black,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false,
                            text);
                    chart.addPoint(point);
                }

                if (kzVal.equalsIgnoreCase("SOA")) {
                    schlamm.add(new ChartCreator.Point(
                            Color.CYAN,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("SOP")) {
                    schlamm.add(new ChartCreator.Point(
                            Color.CYAN,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("SOE")) {
                    schlamm.add(new ChartCreator.Point(
                            Color.CYAN,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                    ChartCreator.Point lastPoint = null;

                    for (final ChartCreator.Point point : schlamm) {
                        if (lastPoint != null) {
                            final ChartCreator.HorizontalLine sLine = new ChartCreator.HorizontalLine(
                                    Color.CYAN,
                                    lastPoint,
                                    point,
                                    Color.CYAN);
                            chart.addHorizontalLines(sLine);
                        }
                        lastPoint = point;
                    }
                    schlamm.clear();
                }

                if (kzVal.equalsIgnoreCase("UKAN")) {
                    baUk.add(new ChartCreator.Point(
                            UK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("UKPP")
                            || kzVal.equalsIgnoreCase("UKBA")
                            || kzVal.equalsIgnoreCase("UKWP")
                            || kzVal.equalsIgnoreCase("UKBW")
                            || kzVal.equalsIgnoreCase("UKBP")
                            || kzVal.equalsIgnoreCase("UKBE")) {
                    baUk.add(new ChartCreator.Point(
                            UK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("UKEN")) {
                    baUk.add(new ChartCreator.Point(
                            UK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                    ChartCreator.Point lastPoint = null;

                    for (final ChartCreator.Point point : baUk) {
                        if (lastPoint != null) {
                            final ChartCreator.HorizontalLine sLine = new ChartCreator.HorizontalLine(
                                    UK_COLOR,
                                    lastPoint,
                                    point,
                                    null);
                            chart.addHorizontalLines(sLine);
                        } else {
                            chart.addPoint(new ChartCreator.Point(
                                    UK_COLOR,
                                    point.getX(),
                                    point.getY(),
                                    false,
                                    true));
                        }
                        lastPoint = point;
                    }
                    chart.addPoint(new ChartCreator.Point(
                            UK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            true));
                    baUk.clear();
                }

                if (kzVal.equalsIgnoreCase("OKAN")) {
                    baOk.add(new ChartCreator.Point(
                            OK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("OKPP")
                            || kzVal.equalsIgnoreCase("OKBA")
                            || kzVal.equalsIgnoreCase("OKBW")
                            || kzVal.equalsIgnoreCase("OKBP")
                            || kzVal.equalsIgnoreCase("OKBE")) {
                    baOk.add(new ChartCreator.Point(
                            OK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                }
                if (kzVal.equalsIgnoreCase("OKEN")) {
                    baOk.add(new ChartCreator.Point(
                            OK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            false));
                    ChartCreator.Point lastPoint = null;

                    for (final ChartCreator.Point point : baOk) {
                        if (lastPoint != null) {
                            final ChartCreator.HorizontalLine sLine = new ChartCreator.HorizontalLine(
                                    OK_COLOR,
                                    lastPoint,
                                    point,
                                    null);
                            chart.addHorizontalLines(sLine);
                        } else {
                            chart.addPoint(new ChartCreator.Point(
                                    OK_COLOR,
                                    point.getX(),
                                    point.getY(),
                                    false,
                                    true));
                        }
                        lastPoint = point;
                    }
                    chart.addPoint(new ChartCreator.Point(
                            OK_COLOR,
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]),
                            Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]),
                            false,
                            true));
                    baOk.clear();
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        if (water != Double.MIN_VALUE) {
            chart.addHorizontalLines(new ChartCreator.HorizontalLine(
                    Color.BLUE,
                    new ChartCreator.Point(Color.BLUE, xStart, water, false, false),
                    new ChartCreator.Point(Color.BLUE, xEnd, water, false, false),
                    null));
        }

        // add ei profiles
        for (int i = 0; i < eiprof.size(); ++i) {
            Point2D eiuk = eiprof.get(i);
            ++i;
            Point2D eifs = eiprof.get(i);

            if (eiuk.getX() == Double.MIN_VALUE) {
                final Point2D tmp = eifs;
                eifs = eiuk;
                eiuk = tmp;
            }
            final double cHeight = Math.abs(eiuk.getY() - eifs.getY());
            final double cWidth = cHeight * 2 / 3;

            chart.addCircle(new ChartCreator.Circle(
                    Color.BLACK,
                    eiuk.getX()
                            - (cWidth / 2),
                    eiuk.getY(),
                    cWidth,
                    cHeight));
        }

        // add kr profiles
        for (int i = 0; i < kprof.size(); ++i) {
            Point2D kruk = kprof.get(i);
            ++i;
            Point2D krfs = kprof.get(i);

            if (kruk.getX() == Double.MIN_VALUE) {
                final Point2D tmp = krfs;
                krfs = kruk;
                kruk = tmp;
            }
            final double cHeight = Math.abs(kruk.getY() - krfs.getY());
            final double cWidth = cHeight;

            chart.addCircle(new ChartCreator.Circle(
                    Color.BLACK,
                    kruk.getX()
                            - (cWidth / 2),
                    kruk.getY(),
                    cWidth,
                    cHeight));
        }

        // add ma profiles
        for (int i = 0; i < maprof.size(); ++i) {
            Point2D mauk = maprof.get(i);
            ++i;
            Point2D mafs = maprof.get(i);

            if (mauk.getX() == Double.MIN_VALUE) {
                final Point2D tmp = mafs;
                mafs = mauk;
                mauk = tmp;
            }
            final double cHeight = Math.abs(mauk.getY() - mafs.getY());
            final double cWidth = cHeight * 3 / 2;

            chart.addCircle(new ChartCreator.Circle(
                    Color.BLACK,
                    mauk.getX()
                            - (cWidth / 2),
                    mauk.getY(),
                    cWidth,
                    cHeight));
        }

        chart.addPointLines(new ChartCreator.PointLine(pointList));

        return chart.createImage(width, height);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String createGafFile() {
        final StringBuilder gafContent = new StringBuilder();

        for (final GAF_FIELDS col : GAF_FIELDS.values()) {
            if (col.ordinal() == (GAF_FIELDS.values().length - 1)) {
                gafContent.append(col.name());
            } else {
                gafContent.append(toFixedSizeString(col.name(), 8, ' '));
            }
        }

        gafContent.append("\r\n");
        final List<Double> keyList = new ArrayList<Double>(profiles.keySet());
        Collections.sort(keyList);

        for (final Double profileId : keyList) {
            final List<String[]> lines = profiles.get(profileId);

            for (final String[] line : lines) {
                for (final GAF_FIELDS col : GAF_FIELDS.values()) {
                    gafContent.append(line[gafIndex[col.ordinal()]]);

                    if (col.ordinal() != (GAF_FIELDS.values().length - 1)) {
                        gafContent.append("\t");
                    }
                }
                gafContent.append("\r\n");
            }
        }

        // last gaf line is always 1A 0D 0A
        gafContent.append((char)0x1A).append("\r\n");

        return gafContent.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   s       DOCUMENT ME!
     * @param   size    DOCUMENT ME!
     * @param   filler  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toFixedSizeString(final String s, final int size, final char filler) {
        final StringBuilder fixedSizeString = new StringBuilder(s);
        int i = s.length();

        for (; i < size; ++i) {
            fixedSizeString.append(filler);
        }

        return fixedSizeString.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  the geometry of the normal profil
     */
    public LineString getNpLine(final Double profile) {
        final ArrayList<String[]> profContent = profiles.get(profile);
        final List<Coordinate> coordinateList = new ArrayList<Coordinate>();
        boolean started = false;
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CismapBroker.getInstance().getDefaultCrsAlias());

        for (final String[] line : profContent) {
            try {
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PA")) {
                    started = true;
                }
                if (started) {
                    final double ho = Double.parseDouble(line[gafIndex[GAF_FIELDS.HW.ordinal()]]);
                    final double re = Double.parseDouble(line[gafIndex[GAF_FIELDS.RW.ordinal()]]);
                    final Coordinate coord = new Coordinate(re, ho);
                    coordinateList.add(coord);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PE")) {
                    started = false;
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        final LineString line = factory.createLineString(coordinateList.toArray(new Coordinate[coordinateList.size()]));

        return line;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Point getProfilePoint(final Double profile) {
        final ArrayList<String[]> profContent = profiles.get(profile);
        Double luY = null;
        Double ruY = null;
        Double lbukY = null;
        Double rbukY = null;
        Double lbokY = null;
        Double rbokY = null;
        Double paY = null;
        Double peY = null;

        for (final String[] line : profContent) {
            try {
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("LU")) {
                    luY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("RU")) {
                    ruY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("LBUK")) {
                    lbukY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("RBUK")) {
                    rbukY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("LBOK")) {
                    lbokY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("RBOK")) {
                    rbokY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PA")) {
                    paY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
                if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PE")) {
                    peY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                }
            } catch (NumberFormatException e) {
                LOG.error("Invalid value found. Not a number.", e);
            }
        }

        if (peY == null) {
            return null;
        }

        if ((luY != null) && (ruY != null)) {
            final double stat = (Math.abs(luY - ruY) / 2) + Math.min(luY, ruY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((lbukY != null) && (rbukY != null)) {
            final double stat = (Math.abs(lbukY - rbukY) / 2) + Math.min(lbukY, rbukY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((lbokY != null) && (rbokY != null)) {
            final double stat = (Math.abs(lbokY - rbokY) / 2) + Math.min(lbokY, rbokY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        if ((paY != null) && (peY != null)) {
            final double stat = (Math.abs(paY - peY) / 2) + Math.min(paY, peY) - peY;
            return getCentralpoint(getNpLine(profile), stat);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line      DOCUMENT ME!
     * @param   position  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Point getCentralpoint(final LineString line, final double position) {
        final GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                CismapBroker.getInstance().getDefaultCrsAlias());
        final LengthIndexedLine lil = new LengthIndexedLine(line);
        final Coordinate coordinate = lil.extractPoint(position);

        return factory.createPoint(coordinate);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final GafReader r = new GafReader(new File("/home/therter/tmp/qp_gaf_testdatei.gaf"));
        final Double prof = (Double)r.getProfiles().toArray()[0];

        final Image i = r.createImage(prof, 800, 300);

        final JFrame frame = new JFrame("test");
        final JPanel panel = new JPanel();
        final JLabel label = new JLabel();
        label.setIcon(new ImageIcon(i));
        panel.add(label);
        frame.add(panel);
        frame.setSize(850, 350);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<Double> getProfiles() {
        return profiles.keySet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   profile  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<String[]> getProfileContent(final Double profile) {
        return profiles.get(profile);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field    DOCUMENT ME!
     * @param   gafLine  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getProfileContent(final GAF_FIELDS field, final String[] gafLine) {
        if ((field == GAF_FIELDS.Z) || (field == GAF_FIELDS.Y) || (field == GAF_FIELDS.STATION)
                    || (field == GAF_FIELDS.HW)
                    || (field == GAF_FIELDS.RW)) {
            return toDouble(gafLine[gafIndex[field.ordinal()]]);
        } else if (field == GAF_FIELDS.RK) {
            initCatalogues();
            return toCatalogueElement(rk, gafLine[gafIndex[field.ordinal()]], "rk");
        } else if (field == GAF_FIELDS.BK) {
            initCatalogues();
            return toCatalogueElement(bk, gafLine[gafIndex[field.ordinal()]], "bk");
        } else if (field == GAF_FIELDS.KZ) {
            initCatalogues();
            return toCatalogueElement(kz, gafLine[gafIndex[field.ordinal()]], "kz");
        } else {
            return gafLine[gafIndex[field.ordinal()]];
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double toDouble(final String obj) {
        try {
            return Double.parseDouble(obj);
        } catch (NumberFormatException e) {
            LOG.error("Not a number", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj           DOCUMENT ME!
     * @param   defaultValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String objectToString(final Object obj, final String defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else {
            if (obj instanceof Double) {
                final DecimalFormat format = new DecimalFormat();
                final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                format.setDecimalFormatSymbols(symbols);
                format.setGroupingUsed(false);

                return format.format(obj);
            } else {
                return String.valueOf(obj);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj           DOCUMENT ME!
     * @param   defaultValue  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String stationObjectToString(final Object obj, final String defaultValue) {
        if (obj == null) {
            return defaultValue;
        } else {
            if (obj instanceof Double) {
                final DecimalFormat format = new DecimalFormat("0.000");
                final java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols();
                symbols.setDecimalSeparator('.');
                format.setDecimalFormatSymbols(symbols);
                format.setGroupingUsed(false);

                return format.format(obj);
            } else {
                return String.valueOf(obj);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer toInteger(final String obj) {
        try {
            return Integer.parseInt(obj);
        } catch (NumberFormatException e) {
            LOG.error("Not a number", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   catalogue    DOCUMENT ME!
     * @param   name         DOCUMENT ME!
     * @param   catPropName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean toCatalogueElement(final List<CidsBean> catalogue, final String name, final String catPropName) {
        for (final CidsBean tmpCat : catalogue) {
            if (String.valueOf(tmpCat.getProperty(catPropName)).equalsIgnoreCase(name)) {
                return tmpCat;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] checkFile() {
        final List<GafErrorContainer> errorList = new ArrayList<GafErrorContainer>();
        final String[] errorStrings;

        initialiseCatalogueValues();

        errorList.addAll(Arrays.asList(checkNP()));
        errorList.addAll(Arrays.asList(checkNPPAPE()));
        errorList.addAll(Arrays.asList(checkNPLogik()));
        errorList.addAll(Arrays.asList(checkSOAOE()));
        errorList.addAll(Arrays.asList(checkUKANEN()));
        errorList.addAll(Arrays.asList(checkUKLogik()));
        errorList.addAll(Arrays.asList(checkOKANEN()));
        errorList.addAll(Arrays.asList(checkOKLogik()));
        errorList.addAll(Arrays.asList(checkEI()));
        errorList.addAll(Arrays.asList(checkMA()));
        errorList.addAll(Arrays.asList(checkAR()));
        errorList.addAll(Arrays.asList(checkHA()));
        errorList.addAll(Arrays.asList(checkY()));
        errorList.addAll(Arrays.asList(checkZ()));
        errorList.addAll(Arrays.asList(checkHW()));
        errorList.addAll(Arrays.asList(checkRW()));
        errorList.addAll(Arrays.asList(checkKZ()));
        errorList.addAll(Arrays.asList(checkHyk()));
        errorList.addAll(Arrays.asList(checkKzHyk()));
        errorList.addAll(Arrays.asList(checkRk()));
        errorList.addAll(Arrays.asList(checkBk()));

        errorStrings = new String[errorList.size()];

        for (int i = 0; i < errorList.size(); ++i) {
            final GafErrorContainer c = errorList.get(i);
            errorStrings[i] = c.getStation() + " " + c.getLine() + " " + c.getCode();
        }

        return errorStrings;
    }

    /**
     * Check: Profil enthält kein Normalprofil
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkNP() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean found = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PA")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!found) {
                        errors.add(new GafErrorContainer(station, lineNumber, "NP"));
                    }
                }
                station = getStationNumber(line);
                found = true;
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Normalprofil ohne Profilanfang oder Profilende
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkNPPAPE() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean pa = false;
        boolean pe = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PA")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!pa || !pe) {
                        errors.add(new GafErrorContainer(station, lineNumber, "NP-PA-PE"));
                    }
                    pe = false;
                }
                station = getStationNumber(line);
                pa = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!pa || !pe) {
                        errors.add(new GafErrorContainer(station, lineNumber, "NP-PA-PE"));
                    }
                    pa = false;
                }
                station = getStationNumber(line);
                pe = true;
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Verletzung der logischen Reihenfolge LDOK - LDUK - LBOK - LBUK - LU - FS - RU - RBUK - RBOK - RDUK - RDOK
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkNPLogik() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        final String[] npOrder = { "LDOK", "LDUK", "LBOK", "LBUK", "LU", "FS", "RU", "RBUK", "RBOK", "RDUK", "RDOK" };
        int index = 0;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            final String kz = line[gafIndex[GAF_FIELDS.KZ.ordinal()]].toUpperCase();

            final int i = indexOf(npOrder, kz);
            if ((station != -1) && (station != getStationNumber(line))) {
                index = 0;
            }
            station = getStationNumber(line);

            if (i != -1) {
                if (i < index) {
                    errors.add(new GafErrorContainer(station, lineNumber, "NP-LOGIK"));
                } else {
                    index = i;
                }
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Schlammsohle ohne Profilanfang oder Profilende
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkSOAOE() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean soa = false;
        boolean soe = false;
        boolean so = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("SOA")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (so && !(so && soa && soe)) {
                        errors.add(new GafErrorContainer(station, lineNumber, "S-OA-OE"));
                    }
                    soe = false;
                }
                station = getStationNumber(line);
                soa = true;
                so = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("SOE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (so && !(so && soa && soe)) {
                        errors.add(new GafErrorContainer(station, lineNumber, "S-OA-OE"));
                    }
                    soa = false;
                }
                station = getStationNumber(line);
                soe = true;
                so = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("SOP")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (so && !(so && soa && soe)) {
                        errors.add(new GafErrorContainer(station, lineNumber, "S-OA-OE"));
                    }
                    soa = false;
                    soe = false;
                }
                station = getStationNumber(line);
                so = true;
            }
        }

        if (station != -1) {
            if (so && !(so && soa && soe)) {
                errors.add(new GafErrorContainer(station, lineNumber, "S-OA-OE"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Bauwerksunterkante ohne Profilanfang oder Profilende
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkUKANEN() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean ba = false;
        boolean be = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKAN")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN"));
                    }
                    ba = false;
                }
                station = getStationNumber(line);
                if (ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN")); // Doppelter Anfang ohne Ende
                                                                                        // dazwischen
                }
                ba = true;
                be = false;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKEN")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN"));
                    }
                    ba = false;
                }
                if (!ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN")); // Ende ohne Anfang davor
                }
                ba = false;
                be = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKPP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBA")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKWP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBW")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN"));
                    }
                    ba = false;
                    be = false;
                }
                if (!ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN")); // Bauwerk ohne Anfang
                }
            }
        }

        if (station != -1) {
            if (ba && !be) {
                errors.add(new GafErrorContainer(station, lineNumber, "UK-AN-EN"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Verletzung der logischen Reihenfolge UKBA - UKBP - UKBW oder UKBA - UKBP - UKBE
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkUKLogik() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean ua = false;
        boolean ue = false;
        boolean b = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBA")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ua && !ue) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                    }
                    ua = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (ua) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                }
                ua = true;
                ue = false;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBW")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ua && !ue) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                    }
                    ua = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (!ua) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                }
                ua = false;
                ue = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKBP")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ua && !ue) {
                        errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                    }
                    ua = false;
                    ue = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (!ua) {
                    errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
                }
            }
        }

        if (station != -1) {
            if (ua && !ue) {
                errors.add(new GafErrorContainer(station, lineNumber, "UK-LOGIK"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Bauwerksoberkante ohne Profilanfang oder Profilende
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkOKANEN() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean ba = false;
        boolean be = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKAN")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN"));
                    }
                    ba = false;
                }
                if (ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN")); // Doppelter Anfang ohen Ende
                                                                                        // dazwischen
                }
                station = getStationNumber(line);
                ba = true;
                be = false;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKEN")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN"));
                    }
                    ba = false;
                }
                if (!ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN")); // Ende ohne Anfang davor
                }
                ba = false;
                be = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKPP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBA")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKWP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBW")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBP")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (ba && !be) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN"));
                    }
                    ba = false;
                    be = false;
                }
                if (!ba) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN")); // Bauwerk ohne Anfang
                }
            }
        }

        if (station != -1) {
            if (ba && !be) {
                errors.add(new GafErrorContainer(station, lineNumber, "OK-AN-EN"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Verletzung der logischen Reihenfolge OKBA - OKBP - OKBW oder OKBA - OKBP - OKBE
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkOKLogik() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean oa = false;
        boolean oe = false;
        boolean b = false;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBA")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (oa && !oe) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                    }
                    oa = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (oa) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                }
                oa = true;
                oe = false;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBW")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBE")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (oa && !oe) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                    }
                    oa = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (!oa) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                }
                oa = false;
                oe = true;
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKBP")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (oa && !oe) {
                        errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                    }
                    oa = false;
                    oe = false;
                    b = false;
                }
                station = getStationNumber(line);
                if (!oa) {
                    errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
                }
            }
        }

        if (station != -1) {
            if (oa && !oe) {
                errors.add(new GafErrorContainer(station, lineNumber, "OK-LOGIK"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Ei-Normprofil ohne EIUK oder EIFS bzw.EIUK.Z <= EIFS.Z
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkEI() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean eiuk = false;
        boolean eifs = false;
        Double eiukZ = null;
        Double eifsZ = null;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("EIUK")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((eiuk != eifs) || ((eiukZ != null) && (eifsZ != null) && (eiukZ <= eifsZ))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "EI"));
                    }
                    eifs = false;
                    eifsZ = null;
                }
                eiuk = true;
                station = getStationNumber(line);

                try {
                    eiukZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    eiukZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("EIFS")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((eiuk != eifs) || ((eiukZ != null) && (eifsZ != null) && (eiukZ <= eifsZ))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "EI"));
                    }
                    eiuk = false;
                    eiukZ = null;
                }
                eifs = true;
                station = getStationNumber(line);

                try {
                    eifsZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    eifsZ = 0.0;
                }
            }
        }

        if (station != -1) {
            if ((eiuk != eifs) || ((eiukZ != null) && (eifsZ != null) && (eiukZ <= eifsZ))) {
                errors.add(new GafErrorContainer(station, lineNumber, "EI"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Maul-Normprofil ohne MAUK oder MAFS bzw. MAUK.Z <= MAFS.Z
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkMA() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean mauk = false;
        boolean mafs = false;
        Double maukZ = null;
        Double mafsZ = null;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("MAUK")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((mauk != mafs) || ((maukZ != null) && (mafsZ != null) && (maukZ <= mafsZ))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "MA"));
                    }
                    mafs = false;
                    mafsZ = null;
                }
                mauk = true;
                station = getStationNumber(line);

                try {
                    maukZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    maukZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("MAFS")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((mauk != mafs) || ((maukZ != null) && (mafsZ != null) && (maukZ <= mafsZ))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "MA"));
                    }
                    mauk = false;
                    maukZ = null;
                }
                mafs = true;
                station = getStationNumber(line);

                try {
                    mafsZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    mafsZ = 0.0;
                }
            }
        }

        if (station != -1) {
            if ((mauk != mafs) || ((maukZ != null) && (mafsZ != null) && (maukZ <= mafsZ))) {
                errors.add(new GafErrorContainer(station, lineNumber, "MA"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: ARMCO71-Profil nicht mit allen 4 Punkten ARUK, ARFS, ARLR und ARRR bzw. ARUK.Z <= ARFS.Z bzw. ARLR.Y >=
     * ARRR.Y
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkAR() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean aruk = false;
        boolean arfs = false;
        boolean arlr = false;
        boolean arrr = false;
        Double arukZ = null;
        Double arfsZ = null;
        Double arlrY = null;
        Double arrrY = null;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("ARUK")) {
                aruk = true;
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!((aruk == arfs) && (arfs == arlr) && (arlr == arrr))
                                || ((arukZ != null) && (arfsZ != null) && (arlrY != null) && (arrrY != null)
                                    && ((arukZ <= arfsZ) || (arlrY >= arrrY)))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "AR"));
                    }
                    aruk = false;
                    arfs = false;
                    arlr = false;
                    arrr = false;
                    arfsZ = null;
                    arlrY = null;
                    arrrY = null;
                }
                station = getStationNumber(line);

                try {
                    arukZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    arukZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("ARFS")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!((aruk == arfs) && (arfs == arlr) && (arlr == arrr))
                                || ((arukZ != null) && (arfsZ != null) && (arlrY != null) && (arrrY != null)
                                    && ((arukZ <= arfsZ) || (arlrY >= arrrY)))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "AR"));
                    }
                    aruk = false;
                    arlr = false;
                    arrr = false;
                    arukZ = null;
                    arlrY = null;
                    arrrY = null;
                }
                arfs = true;
                station = getStationNumber(line);

                try {
                    arfsZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    arfsZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("ARLR")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!((aruk == arfs) && (arfs == arlr) && (arlr == arrr))
                                || ((arukZ != null) && (arfsZ != null) && (arlrY != null) && (arrrY != null)
                                    && ((arukZ <= arfsZ) || (arlrY >= arrrY)))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "AR"));
                    }
                    aruk = false;
                    arfs = false;
                    arrr = false;
                    arukZ = null;
                    arfsZ = null;
                    arrrY = null;
                }
                station = getStationNumber(line);
                arlr = true;

                try {
                    arlrY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                } catch (NumberFormatException e) {
                    arlrY = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("ARRR")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if (!((aruk == arfs) && (arfs == arlr) && (arlr == arrr))
                                || ((arukZ != null) && (arfsZ != null) && (arlrY != null) && (arrrY != null)
                                    && ((arukZ <= arfsZ) || (arlrY >= arrrY)))) {
                        errors.add(new GafErrorContainer(station, 0, "AR"));
                    }
                    aruk = false;
                    arfs = false;
                    arlr = false;
                    arukZ = null;
                    arfsZ = null;
                    arlrY = null;
                }
                arrr = true;
                station = getStationNumber(line);

                try {
                    arrrY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                } catch (NumberFormatException e) {
                    arrrY = 0.0;
                }
            }
        }

        if (station != -1) {
            if (!((aruk == arfs) && (arfs == arlr) && (arlr == arrr))
                        || ((arukZ != null) && (arfsZ != null) && (arlrY != null) && (arrrY != null)
                            && ((arukZ <= arfsZ) || (arlrY >= arrrY)))) {
                errors.add(new GafErrorContainer(station, lineNumber, "AR"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: HAMCO84-Profil nicht mit allen 4 Punkten HAUK, HAFS, HALR und HARR bzw. HAUK.Z <= HAFS.Z bzw. HALR.Y >=
     * HARR.Y
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkHA() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;
        boolean hauk = false;
        boolean hafs = false;
        boolean halr = false;
        boolean harr = false;
        Double haukZ = null;
        Double hafsZ = null;
        Double halrY = null;
        Double harrY = null;
        double station = -1;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("HAUK")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((!((hauk == hafs) && (hafs == halr) && (halr == harr))
                                    || ((haukZ != null) && (hafsZ != null) && (halrY != null) && (harrY != null)
                                        && ((haukZ <= hafsZ) || (halrY >= harrY))))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "HA"));
                    }
                    hafs = false;
                    halr = false;
                    harr = false;
                    hafsZ = null;
                    halrY = null;
                    harrY = null;
                }
                hauk = true;
                station = getStationNumber(line);

                try {
                    haukZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    haukZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("HAFS")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((!((hauk == hafs) && (hafs == halr) && (halr == harr))
                                    || ((haukZ != null) && (hafsZ != null) && (halrY != null) && (harrY != null)
                                        && ((haukZ <= hafsZ) || (halrY >= harrY))))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "HA"));
                    }
                    hauk = false;
                    halr = false;
                    harr = false;
                    halrY = null;
                    harrY = null;
                }
                station = getStationNumber(line);
                hafs = true;

                try {
                    hafsZ = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
                } catch (NumberFormatException e) {
                    hafsZ = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("HALR")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((!((hauk == hafs) && (hafs == halr) && (halr == harr))
                                    || ((haukZ != null) && (hafsZ != null) && (halrY != null) && (harrY != null)
                                        && ((haukZ <= hafsZ) || (halrY >= harrY))))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "HA"));
                    }
                    hauk = false;
                    hafs = false;
                    harr = false;
                    hafsZ = null;
                    harrY = null;
                }
                station = getStationNumber(line);
                halr = true;

                try {
                    halrY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                } catch (NumberFormatException e) {
                    halrY = 0.0;
                }
            }
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("HARR")) {
                if ((station != -1) && (station != getStationNumber(line))) {
                    if ((!((hauk == hafs) && (hafs == halr) && (halr == harr))
                                    || ((haukZ != null) && (hafsZ != null) && (halrY != null) && (harrY != null)
                                        && ((haukZ <= hafsZ) || (halrY >= harrY))))) {
                        errors.add(new GafErrorContainer(station, lineNumber, "HA"));
                    }
                    hauk = false;
                    hafs = false;
                    halr = false;
                    hafsZ = null;
                    halrY = null;
                }
                station = getStationNumber(line);
                harr = true;

                try {
                    harrY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                } catch (NumberFormatException e) {
                    harrY = 0.0;
                }
            }
        }

        if (station != -1) {
            if ((!((hauk == hafs) && (hafs == halr) && (halr == harr))
                            || ((haukZ != null) && (hafsZ != null) && (halrY != null) && (harrY != null)
                                && ((haukZ <= hafsZ) || (halrY >= harrY))))) {
                errors.add(new GafErrorContainer(station, lineNumber, "HA"));
            }
        }
        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Y-Wert pro Teilprofil nicht aufsteigend (d.h. absteigend oder identisch)
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkY() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        double y = Double.MIN_VALUE;

        for (final String[] line : content) {
            ++lineNumber;
            if (line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("PE")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("SOE")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("UKEN")
                        || line[gafIndex[GAF_FIELDS.KZ.ordinal()]].equalsIgnoreCase("OKEN")) {
                y = Double.MIN_VALUE;
            } else {
                double newY;

                try {
                    newY = Double.parseDouble(line[gafIndex[GAF_FIELDS.Y.ordinal()]]);
                } catch (NumberFormatException e) {
                    newY = 0.0;
                }

                if ((y != Double.MIN_VALUE) && (newY <= y)) {
                    errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "Y"));
                } else {
                    y = newY;
                }
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Z-Wert unzulässig
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkZ() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final double z;
            ++lineNumber;

            try {
                z = Double.parseDouble(line[gafIndex[GAF_FIELDS.Z.ordinal()]]);
            } catch (NumberFormatException e) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "Z"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Hochwert unzulässig
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkHW() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final double hw;
            ++lineNumber;

            try {
                hw = Double.parseDouble(line[gafIndex[GAF_FIELDS.HW.ordinal()]]);
            } catch (NumberFormatException e) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "HW"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Rechtswert unzulässig
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkRW() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final double rw;
            ++lineNumber;

            try {
                rw = Double.parseDouble(line[gafIndex[GAF_FIELDS.RW.ordinal()]]);
            } catch (NumberFormatException e) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "RW"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Kennziffer nicht in Katalog
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkKZ() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final String kz = line[gafIndex[GAF_FIELDS.KZ.ordinal()]];
            ++lineNumber;

            if (indexOf(allKz, kz) == -1) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "KZ"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: HYK-Kennziffer Hydraulik nicht in Katalog
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkHyk() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final String hyk = line[gafIndex[GAF_FIELDS.HYK.ordinal()]];
            ++lineNumber;

            if (!hyk.equalsIgnoreCase("x") && (indexOf(allHYK, hyk) == -1)) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "HYK"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Kombination Kennziffer-Kennziffer Hydraulik nicht im Katalog
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkKzHyk() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            boolean found = false;
            final String hyk = line[gafIndex[GAF_FIELDS.HYK.ordinal()]];
            final String kz = line[gafIndex[GAF_FIELDS.KZ.ordinal()]];
            ++lineNumber;

            if (!hyk.equalsIgnoreCase("x")) {
                for (final String[] tmp : allKzHykPerms) {
                    if (tmp[0].equalsIgnoreCase(kz) && tmp[1].equalsIgnoreCase(hyk)) {
                        found = true;
                    }
                }
            } else {
                found = true;
            }

            if (!found) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "KZ-HYK"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Rauheitsklasse nicht im Katalog
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkRk() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            final int rk;
            boolean found = false;
            ++lineNumber;

            try {
                rk = Integer.parseInt(line[gafIndex[GAF_FIELDS.RK.ordinal()]]);

                if (rk == -1) {
                    found = true;
                }

                for (final int krk : allRK) {
                    if (krk == rk) {
                        found = true;
                    }
                }

                if (!found) {
                    errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "RK"));
                }
            } catch (NumberFormatException e) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "RK"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * Check: Bewuchsklasse nicht im Katalog
     *
     * @return  true, iff the check was completed successfully
     */
    private GafErrorContainer[] checkBk() {
        final List<GafErrorContainer> errors = new ArrayList<GafErrorContainer>();
        int lineNumber = 1;

        for (final String[] line : content) {
            ++lineNumber;
            final int bk;
            boolean found = false;

            try {
                bk = Integer.parseInt(line[gafIndex[GAF_FIELDS.BK.ordinal()]]);

                if (bk == -1) {
                    found = true;
                }

                for (final int kbk : allBK) {
                    if (kbk == bk) {
                        found = true;
                    }
                }

                if (!found) {
                    errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "BK"));
                }
            } catch (NumberFormatException e) {
                errors.add(new GafErrorContainer(getStationNumber(line), lineNumber, "BK"));
            }
        }

        return errors.toArray(new GafErrorContainer[errors.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   line  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getStationNumber(final String[] line) {
        try {
            return Double.parseDouble(line[gafIndex[GAF_FIELDS.STATION.ordinal()]]);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sArray  DOCUMENT ME!
     * @param   value   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int indexOf(final String[] sArray, final String value) {
        for (int i = 0; i < sArray.length; ++i) {
            if (sArray[i].equals(value)) {
                return i;
            }
        }

        return -1;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class GafErrorContainer {

        //~ Instance fields ----------------------------------------------------

        Double station;
        Integer line;
        String code;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new GafErrorContainer object.
         *
         * @param  station  DOCUMENT ME!
         * @param  line     DOCUMENT ME!
         * @param  code     DOCUMENT ME!
         */
        public GafErrorContainer(final Double station, final Integer line, final String code) {
            this.station = station;
            this.line = line;
            this.code = code;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  code  DOCUMENT ME!
         */
        public void setCode(final String code) {
            this.code = code;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  line  DOCUMENT ME!
         */
        public void setLine(final Integer line) {
            this.line = line;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  station  DOCUMENT ME!
         */
        public void setStation(final Double station) {
            this.station = station;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getCode() {
            return code;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Integer getLine() {
            return line;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public Double getStation() {
            return station;
        }
    }
}
