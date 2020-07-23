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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.cismet.cids.custom.watergis.server.search.AllGewGeschlBySb;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneGeschlGemeindeReportDialog;
import de.cismet.watergis.gui.dialog.GerinneGeschlSbReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GemeindenData;
import de.cismet.watergis.reports.types.GemeindenDataLightweight;
import de.cismet.watergis.reports.types.GmdPartObjGeschl;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneGSbReport extends GerinneGGemeindeReport {

    //~ Instance fields --------------------------------------------------------

    private Map<Integer, List<GmdPartObjGeschl>> gemPartMap = new HashMap<Integer, List<GmdPartObjGeschl>>();
    private Map<Integer, GemeindenDataLightweight> gemDataMap = new HashMap<Integer, GemeindenDataLightweight>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public File createGerinneGewaesserReport(final int[] gew) throws Exception {
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("spalte1", false);
        parameters.put("spalte2", false);
        parameters.put("spalte3", false);
        parameters.put("spalte4", false);
        parameters.put("spalte5", false);
        parameters.put("spalte6", false);
        parameters.put("spalte7", false);
        parameters.put("spalte8", false);
        parameters.put("spalte9", false);
        parameters.put("spalte10", false);
        parameters.put("spalte11", false);
        parameters.put("ges", true);
        parameters.put("dimension", (GerinneGeschlSbReportDialog.getInstance().getDimensions() != null));
        parameters.put("tiefenklasse", (GerinneGeschlSbReportDialog.getInstance().getClasses() != null));
        parameters.put("fachdata", GerinneGeschlSbReportDialog.getInstance().isAllDataPerObject());
        parameters.put("perObject", GerinneGeschlSbReportDialog.getInstance().isPerSb());
        parameters.put("sumGu", false);
        parameters.put("withWdm", false);
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneGSbReport.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/gerinneGeschlSb.jasper"));

        init(gew);

        if ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                    && (GerinneGeschlGemeindeReportDialog.getInstance().getDimensions() != null)
                    && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null)
                    && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses().size() > 1)) {
            parameters.put("gemKomp", true);
            dataSources.put("gemeinden", getSb2());
        } else if (((GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null))
                    || ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses().size() == 1))) {
            parameters.put("gemKomp", false);
            dataSources.put("gemeinden", getSb1());
        } else {
            parameters.put("gemKomp", false);
            dataSources.put("gemeinden", getSb());
        }

        if (GerinneGeschlSbReportDialog.getInstance().isPerSb()) {
            dataSources.put("objects", getObjects());
        }

        // create print from report and data
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        // set orientation jasperPrint.setOrientation(jasperReport.getOrientationValue());
        //
        // final FileOutputStream pfout = new FileOutputStream(new File("/home/therter/tmp/gemeinden.pdf")); final
        // BufferedOutputStream pout = new BufferedOutputStream(pfout);
        // JasperExportManager.exportReportToPdfStream(jasperPrint, pout); pout.close();

        final File file = new File(
                GerinneGeschlSbReportDialog.getInstance().getPath()
                        + "/Schaubezirke.xlsx");
        final FileOutputStream fout = new FileOutputStream(file);
        final BufferedOutputStream out = new BufferedOutputStream(fout);
        final JRXlsxExporter exporter = new JRXlsxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        final SimpleOutputStreamExporterOutput exportOut = new SimpleOutputStreamExporterOutput(out);
        exporter.setExporterOutput(exportOut);

        final SimpleXlsxReportConfiguration config = new SimpleXlsxReportConfiguration();
        config.setOnePagePerSheet(Boolean.TRUE);
        config.setSheetNames(sheetNames.toArray(new String[sheetNames.size()]));
        config.setShowGridLines(true);
        config.setColumnWidthRatio(1.5f);
        config.setRemoveEmptySpaceBetweenColumns(true);
        config.setRemoveEmptySpaceBetweenRows(true);
        config.setCellHidden(true);
        config.setDetectCellType(true);
        exporter.setConfiguration(config);
        exporter.exportReport();

        exportOut.close();
        // without this close, the file will be corrupted
        out.close();

        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final GerinneGSbReport report = new GerinneGSbReport();
        try {
            report.createReport(new int[] { 2 }, new int[] { 2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeIds  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final int[] routeIds) throws Exception {
        final List<GmdPartObjGeschl> objectList = getAllRoutes(routeIds);

        for (final GmdPartObjGeschl obj : objectList) {
            List<GmdPartObjGeschl> sbList = gemPartMap.get(obj.getNr_li());

            if (sbList == null) {
                sbList = new ArrayList<GmdPartObjGeschl>();
                gemPartMap.put(obj.getNr_li(), sbList);
            }

            sbList.add(obj);
        }

        for (final Integer sb : gemPartMap.keySet()) {
            final Integer[] idList = getGew(sb).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
            gemDataMap.put(sb, new GemeindenDataLightweight(sb, routes));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routeIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<GmdPartObjGeschl> getAllRoutes(final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewGeschlBySb(routeIds, getAllowedWdms());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GmdPartObjGeschl> objList = new ArrayList<GmdPartObjGeschl>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GmdPartObjGeschl(
                        (Integer)f.get(0),
                        (String)f.get(7),
                        (String)f.get(1),
                        (String)f.get(4),
                        (String)f.get(2),
                        (Integer)f.get(3),
                        (String)f.get(8),
                        (Double)f.get(9),
                        (Double)f.get(10),
                        (Integer)f.get(11),
                        (Integer)f.get(12),
                        (Double)f.get(5),
                        (Double)f.get(6),
                        (String)f.get(13),
                        (String)f.get(14),
                        (String)f.get(15),
                        (String)f.get(16),
                        (String)f.get(17),
                        (Integer)f.get(18),
                        (String)f.get(19),
                        (String)f.get(20),
                        (String)f.get(21),
                        (Double)f.get(22),
                        (Double)f.get(23),
                        (Double)f.get(24),
                        (Double)f.get(25),
                        (Double)f.get(26),
                        (Double)f.get(27),
                        (Double)f.get(28),
                        (Double)f.get(29),
                        (Double)f.get(30),
                        (Double)f.get(31),
                        (Double)f.get(32),
                        (String)f.get(33),
                        (String)f.get(34)));
            }
        }

        return objList;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int[] getAllowedWdms() {
        final List<Integer> wdmList = new ArrayList<Integer>();

        if (GerinneGeschlSbReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneGeschlSbReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneGeschlSbReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneGeschlSbReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneGeschlSbReportDialog.getInstance().is1505()) {
            wdmList.add(1505);
        }

        final int[] wdms = new int[wdmList.size()];

        for (int i = 0; i < wdmList.size(); ++i) {
            wdms[i] = wdmList.get(i);
        }

        return wdms;
    }

    /**
     * Only one dimension or no dimension.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getSb() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        for (final Integer gem : gemDataMap.keySet()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            double count = 0;
            double length = 0;
            feature.put("anzahlGmd", gemDataMap.size());
            feature.put("group", "gemeinde");
            feature.put("gmdNr", gem);
            feature.put("gmdName", gemDataMap.get(gem).getGmdName());
            feature.put("art", art);

            if (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null) {
                final List<Integer> l = GerinneGeschlGemeindeReportDialog.getInstance().getClasses();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) instead of 0 to
                                                                                    // prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + i;
                    final String laenge = "laenge" + i;
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllTf(gem, from, till);
                    final double colLength = getLengthAllTf(gem, from, till);
                    count += colCount;
                    length += colLength;

                    feature.put(lab, "Tiefe:\n" + ((l.get(i) == null) ? "ohne" : ("bis " + l.get(i))));
                    feature.put(anz, colCount);
                    feature.put(laenge, colLength);
                }

                if (first) {
                    for (int i = 0; i < l.size(); ++i) {
                        parameters.put("spalte" + (i + 1), true);
                    }

                    first = false;
                }
            } else if (GerinneGeschlGemeindeReportDialog.getInstance().getDimensions() != null) {
                final List<Integer> l = GerinneGeschlGemeindeReportDialog.getInstance().getDimensions();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllDim(gem, from, till);
                    final double colLength = getLengthAllDim(gem, from, till);
                    count += colCount;
                    length += colLength;

                    feature.put(lab, "Dimension:\n" + ((l.get(i) == null) ? "ohne" : ("bis " + l.get(i))));
                    feature.put(anz, colCount);
                    feature.put(laenge, colLength);
                }

                if (first) {
                    for (int i = 0; i < l.size(); ++i) {
                        parameters.put("spalte" + (i + 1), true);
                    }

                    first = false;
                }
            } else if (GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null) {
                final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGemeindeReportDialog.getInstance()
                            .getArt();

                for (int i = 0; i < l.size(); ++i) {
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCount(gem, l.get(i));
                    final double colLength = getLength(gem, l.get(i));

                    count += colCount;
                    length += colLength;

                    feature.put(lab, toColumnName(l.get(i)));
                    feature.put(anz, colCount);
                    feature.put(laenge, colLength);
                }

                if (first) {
                    for (int i = 0; i < l.size(); ++i) {
                        parameters.put("spalte" + (i + 1), true);
                    }

                    if (GerinneGeschlGemeindeReportDialog.getInstance().getArt().size() == 1) {
                        parameters.put("ges", false);
                    }
                    first = false;
                }
            }

            feature.put("anzGes", count);
            feature.put("laengeGes", length);

            features.add(feature);
        }
        features.add(createKumFeature(features, false));

        return new FeatureDataSource(features);
    }

    /**
     * tf und dim, art und dim, art und tiefe, art (nur eine) und tf und dim.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getSb1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGemeindeReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlGemeindeReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlGemeindeReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        for (int di = 0; di < d.size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

            for (final Integer gem : gemDataMap.keySet()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) instead of 0
                                                                                     // to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;
                feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                feature.put("anzahlGmd", gemDataMap.size());
                feature.put("group", null);
                feature.put("gmdNr", gem);
                feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                feature.put("art", art);

                if (!isTiefeArt && (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null)) {
                    final List<Integer> l = GerinneGeschlGemeindeReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if ((GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null)
                                    && (GerinneGeschlGemeindeReportDialog.getInstance().getArt().size() == 1)) {
                            final GerinneGeschlGemeindeReportDialog.Art arten = GerinneGeschlGemeindeReportDialog
                                        .getInstance().getArt().get(0);
                            colCount = getCountAllTfDim(gem, arten, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gem, arten, from, till, dimFrom, dimTill);
                        } else {
                            colCount = getCountAllTfDim(gem, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gem, from, till, dimFrom, dimTill);
                        }
                        count += colCount;
                        length += colLength;

                        feature.put(lab, "Tiefe:\n" + ((l.get(i) == null) ? "ohne" : ("bis " + l.get(i))));
                        feature.put(anz, colCount);
                        feature.put(laenge, colLength);
                    }

                    if (first) {
                        for (int i = 0; i < l.size(); ++i) {
                            parameters.put("spalte" + (i + 1), true);
                        }

                        first = false;
                    }
                } else if (GerinneGeschlGemeindeReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGemeindeReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if (isTiefeArt) {
                            colCount = getCountTf(gem, l.get(i), dimFrom, dimTill);
                            colLength = getLengthTf(gem, l.get(i), dimFrom, dimTill);
                        } else {
                            colCount = getCountDim(gem, l.get(i), dimFrom, dimTill);
                            colLength = getLengthDim(gem, l.get(i), dimFrom, dimTill);
                        }

                        count += colCount;
                        length += colLength;

                        feature.put(lab, toColumnName(l.get(i)));
                        feature.put(anz, colCount);
                        feature.put(laenge, colLength);
                    }

                    if (first) {
                        for (int i = 0; i < l.size(); ++i) {
                            parameters.put("spalte" + (i + 1), true);
                        }

                        if (GerinneGeschlGemeindeReportDialog.getInstance().getArt().size() == 1) {
                            parameters.put("ges", false);
                        }
                        first = false;
                    }
                }

                feature.put("anzGes", count);
                feature.put("laengeGes", length);

                features.add(feature);
                featuresKum.add(feature);
                featuresKumDim.add(feature);
            }
            features.add(createKumFeature(featuresKumDim, true));
        }
        features.add(createKumFeature(featuresKum, false));

        return new FeatureDataSource(features);
    }

    /**
     * alle Dimensionen.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getSb2() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        for (int di = 0; di < GerinneGeschlGemeindeReportDialog.getInstance().getDimensions().size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
            final List<Integer> d = GerinneGeschlGemeindeReportDialog.getInstance().getDimensions();

            for (final Integer gem : gemDataMap.keySet()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;

                feature.put("anzahlGmd", gemDataMap.size());
                feature.put("group", null);
                feature.put("gmdNr", gem);
                feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                feature.put("art", art);
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                for (final GerinneGeschlGemeindeReportDialog.Art a
                            : GerinneGeschlGemeindeReportDialog.getInstance().getArt()) {
                    double countA = 0;
                    double lengthA = 0;
                    if (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlGemeindeReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) instead
                                                                                            // of 0 to prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + getArtPrefix(a) + (i + 1);
                            final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCountAllTfDim(gem, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gem, a, from, till, dimFrom, dimTill);
                            count += colCount;
                            length += colLength;
                            countA += colCount;
                            lengthA += colLength;

                            feature.put(lab, "Tiefe:\n" + ((l.get(i) == null) ? "ohne" : ("bis " + l.get(i))));
                            feature.put(anz, colCount);
                            feature.put(laenge, colLength);
                        }

                        if (first) {
                            for (int i = 0; i < l.size(); ++i) {
                                parameters.put("spalte" + (i + 1), true);
                            }

                            first = false;
                        }
                    }

                    feature.put("anz" + getArtPrefix(a) + "Ges", countA);
                    feature.put("laenge" + getArtPrefix(a) + "Ges", lengthA);
                }
                feature.put("anzGes", count);
                feature.put("laengeGes", length);

                if (GerinneGeschlGemeindeReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlGemeindeReportDialog.getInstance().getClasses();
                    double countT = 0;
                    double lengthT = 0;

                    for (int i = 0; i < l.size(); ++i) {
                        for (final GerinneGeschlGemeindeReportDialog.Art a
                                    : GerinneGeschlGemeindeReportDialog.getInstance().getArt()) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final double colCount = getCountAllTfDim(gem, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gem, a, from, till, dimFrom, dimTill);
                            countT += colCount;
                            lengthT += colLength;
                        }

                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        feature.put(anz, countT);
                        feature.put(laenge, lengthT);
                    }
                }

                features.add(feature);
                featuresKum.add(feature);
                featuresKumDim.add(feature);
            }
            features.add(createKumFeature(featuresKumDim, true));
        }
        features.add(createKumFeature(featuresKum, false));

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getObjects() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final boolean first = true;
        final String art = createArtString();

        for (final Integer gem : gemDataMap.keySet()) {
            sheetNames.add("Objekte " + gemDataMap.get(gem).getGmdName());
            for (final Integer gew : getGew(gem)) {
                for (final GmdPartObjGeschl obj : getObjects(gem, gew)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                    feature.put("gewName", getGewName(gem, gew));
                    feature.put("code", getBaCd(gem, gew));
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gew));
                    feature.put("von", convertStation(obj.getFrom()));
                    feature.put("bis", convertStation(obj.getTill()));
                    feature.put("anzahlObj", getObjects(gem, gew).size());
                    feature.put("tf", getTf(obj.getTf()));
                    feature.put("dim", getDim(obj.getDim()));
                    feature.put("ls", obj.getLs());
                    feature.put("prof", obj.getProf());
                    feature.put("ma", obj.getMa());
                    feature.put("objNr", obj.getObjNr());
                    feature.put("tr", obj.getTr());
                    feature.put("ausbaujahr", obj.getAusbaujahr());
                    feature.put("wbbl", obj.getWbbl());
                    feature.put("art", obj.getArt());
                    feature.put("laengeObj", obj.getLength());
                    feature.put("faktor", obj.getLs());
                    feature.put("zustKl", obj.getLs());
                    feature.put("br", obj.getLs());
                    feature.put("brOben", obj.getBrOben());
                    feature.put("hoehe", obj.getHoehe());
                    feature.put("hEin", obj.gethEin());
                    feature.put("hAus", obj.gethAus());
                    feature.put("gefaelle", obj.getGefaelle());
                    feature.put("dhAus", obj.getDhAus());
                    feature.put("dhEin", obj.getDhEin());
                    feature.put("hAb", obj.gethAb());
                    feature.put("hAuf", obj.gethAuf());
                    feature.put("aufstieg", obj.getAufstieg());

                    features.add(feature);
                }
            }
        }
        features.add(createKumFeature(features, false));

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sbNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final int sbNr) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(sbNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjGeschl tmp : gemList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<GmdPartObjGeschl> getObjects(final int gemNr, final int gew) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        final TreeSet<GmdPartObjGeschl> ts = new TreeSet<GmdPartObjGeschl>();

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getId() == gew) {
                ts.add(tmp);
            }
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
    private Collection<String> getGu(final int gemNr) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjGeschl tmp : gemList) {
            ts.add(tmp.getOwner());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWdm(final int gemNr, final String gu) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu)) {
                ts.add(tmp.getWidmung());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   value  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    protected boolean valueBetween(final Double value, final Integer from, final Integer till) {
        if ((value == null) && (till == null)) {
            return true;
        } else if (((value == null) || (till == null))) { // xor caused by the first case
            return false;
        } else {
            final Integer fromConvert = ((from == null) ? 0 : from);

            return (value > fromConvert) && (value <= till);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final int gemNr, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final int gemNr, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final int gemNr, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final int gemNr, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final int gemNr, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final int gemNr, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (!tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final int gemNr, final int gew, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final int gemNr, final int gew, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final int gemNr, final String gu, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final int gemNr, final String gu, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final int gemNr, final int gew, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final int gemNr, final int gew, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final int gemNr, final String gu, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final int gemNr, final String gu, final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getWidmung().equals(wdm) && tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   art    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getWidmung().equals(wdm) && tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final int gemNr, final int gew, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final int gemNr, final int gew, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final int gemNr, final String gu, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final int gemNr, final String gu, final Integer from, final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gew DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gew      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final int gew,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gew      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final int gew,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final String gu,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final String gu,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && !tmp.getArt().equals("p")
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gew      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gew      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr    DOCUMENT ME!
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && tmp.getWidmung().equals(wdm) && tmp.getArt().equals(art.name())
                        && valueBetween(tmp.getTf(), tfFrom, tfTill)
                        && valueBetween(tmp.getDim(), dimFrom, dimTill)) {
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
    private int getCountGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObjGeschl tmp : gemList) {
                ts.add(tmp.getOwner());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final int gemNr, final int gew) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getBaCd();
            }
        }

        return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGewName(final int gemNr, final int gew) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getGewName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGewAll(final int gemNr) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjGeschl tmp : gemList) {
            ts.add(tmp.getBaCd());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGewAll(final int gemNr) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            length += tmp.getLength();
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gemNr, final int gewId) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getId() == gewId) {
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
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gemNr, final String gu) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
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
    private double getLengthGew(final String gu, final Integer wdm) {
        double length = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObjGeschl tmp : gemList) {
                if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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
    private int getCountGew(final int gemNr, final String gu) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
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
    private int getCountGew(final String gu, final Integer wdm) {
        int count = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObjGeschl tmp : gemList) {
                if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjGeschl tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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
    @Override
    protected String createArtString() {
        String art;

        if (GerinneGeschlSbReportDialog.getInstance().getArt() == null) {
            art = "RL-D-Due";
        } else {
            art = null;
            for (final GerinneGeschlGemeindeReportDialog.Art a
                        : GerinneGeschlSbReportDialog.getInstance().getArt()) {
                if (art == null) {
                    art = a.name();
                } else {
                    art += "-" + a.name();
                }
            }
        }

        return art;
    }
}
