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

import de.cismet.cids.custom.watergis.server.search.AllGewGeschlByGem;
import de.cismet.cids.custom.watergis.server.search.AllGewWithParts;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneGeschlGemeindeReportDialog;
import de.cismet.watergis.gui.dialog.GerinneGeschlGewaesserReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GmdPartObjGeschl;
import de.cismet.watergis.reports.types.KatasterGewObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneGGewaesserReport extends GerinneGGemeindeReport {

    //~ Instance fields --------------------------------------------------------

    private List<GmdPartObjGeschl> objList;
    private GerOffenHelper gerOffenHelper;

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
        parameters.put("dimension", (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null));
        parameters.put("tiefenklasse", (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null));
        parameters.put("fachdata", GerinneGeschlGewaesserReportDialog.getInstance().isAllDataPerObject());
        parameters.put("perObject", GerinneGeschlGewaesserReportDialog.getInstance().isPerObject());
        parameters.put("sumGu", GerinneGeschlGewaesserReportDialog.getInstance().isSumGu());
        parameters.put("withWdm", GerinneGeschlGewaesserReportDialog.getInstance().isPerWdm());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneGGewaesserReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/gerinneGeschlGew.jasper"));

        init(gew);

        if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                    && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                    && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
            parameters.put("gemKomp", true);
            dataSources.put("gewaesser", getGewaesser2());
        } else if (((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null))
                    || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses().size() == 1))) {
            parameters.put("gemKomp", false);
            dataSources.put("gewaesser", getGewaesser1());
        } else {
            parameters.put("gemKomp", false);
            dataSources.put("gewaesser", getGewaesser());
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().isPerObject()) {
            dataSources.put("objects", getObjects());
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().isSumGu()
                    && !GerinneGeschlGewaesserReportDialog.getInstance().isPerWdm()) {
            if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
                dataSources.put("gu", getGuDataSource2());
            } else if (((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses().size() == 1))) {
                dataSources.put("gu", getGuDataSource1());
            } else {
                dataSources.put("gu", getGuDataSource());
            }
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().isSumGu()
                    && GerinneGeschlGewaesserReportDialog.getInstance().isPerWdm()) {
            if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
                dataSources.put("gu", getGuWdmDataSource2());
            } else if (((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null))
                        || ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses().size() == 1))) {
                dataSources.put("gu", getGuWdmDataSource1());
            } else {
                dataSources.put("gu", getGuWdmDataSource());
            }
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
                GerinneGeschlGewaesserReportDialog.getInstance().getPath()
                        + "/Gerinne_geschlossen_Gewässer.xlsx");
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
        final GerinneGGewaesserReport report = new GerinneGGewaesserReport();
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
        objList = getAllRlDDue(routeIds);
        gerOffenHelper = new GerOffenHelper(routeIds, getAllowedWdms());
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
    private List<GmdPartObjGeschl> getAllRlDDue(final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewGeschlByGem(routeIds, getAllowedWdms());
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
                        (String)f.get(5),
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
     * @return  DOCUMENT ME!
     */
    private int[] getAllowedWdms() {
        final List<Integer> wdmList = new ArrayList<Integer>();

        if (GerinneGeschlGewaesserReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneGeschlGewaesserReportDialog.getInstance().is1505()) {
            wdmList.add(1505);
        }

        final int[] wdms = new int[wdmList.size()];

        for (int i = 0; i < wdmList.size(); ++i) {
            wdms[i] = wdmList.get(i);
        }

        return wdms;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesser() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        boolean first = true;
        final String art = createArtString();
        sheetNames.add("Gewässer");

        for (final Integer gew : gerOffenHelper.getGew()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            double count = 0;
            double length = 0;
            feature.put("anzGew", objList.size());
            feature.put("group", null);
            feature.put("gewName", getGewName(gew));
            feature.put("code", getBaCd(gew));
            feature.put("arten", art);
            feature.put("laenge", gerOffenHelper.getLengthGewAll(gew));

            if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + i;
                    final String laenge = "laenge" + i;
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllTf(gew, from, till);
                    final double colLength = getLengthAllTf(gew, from, till);
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
            } else if (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null) {
                final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllDim(gew, from, till);
                    final double colLength = getLengthAllDim(gew, from, till);
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
            } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog.getInstance()
                            .getArt();

                for (int i = 0; i < l.size(); ++i) {
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCount(gew, l.get(i));
                    final double colLength = getLength(gew, l.get(i));

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

                    if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
                        parameters.put("ges", false);
                    }
                    first = false;
                }
            }

            feature.put("anzGes", count);
            feature.put("laengeGes", length);

            features.add(feature);
        }
        final Map<String, Object> total = createKumFeature(features, false);
        total.put("gewName", null);
        total.put("code", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * tf und dim art und dim art und tiefe art (nur eine) und tf und dim.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesser1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        sheetNames.add("Gewässer");
        for (int di = 0; di < d.size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

            for (final Integer gew : gerOffenHelper.getGew()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;
                feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                feature.put("anzGew", getGew().size());
                feature.put("group", null);
                feature.put("gewName", getGewName(gew));
                feature.put("code", getBaCd(gew));
                feature.put("arten", art);
                feature.put("laenge", gerOffenHelper.getLengthGewAll(gew));

                if (!isTiefeArt && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                                    && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1)) {
                            final GerinneGeschlGemeindeReportDialog.Art arten = GerinneGeschlGewaesserReportDialog
                                        .getInstance().getArt().get(0);
                            colCount = getCountAllTfDim(gew, arten, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gew, arten, from, till, dimFrom, dimTill);
                        } else {
                            colCount = getCountAllTfDim(gew, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gew, from, till, dimFrom, dimTill);
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
                } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if (isTiefeArt) {
                            colCount = getCountTf(gew, l.get(i), dimFrom, dimTill);
                            colLength = getLengthTf(gew, l.get(i), dimFrom, dimTill);
                        } else {
                            colCount = getCountDim(gew, l.get(i), dimFrom, dimTill);
                            colLength = getLengthDim(gew, l.get(i), dimFrom, dimTill);
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

                        if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
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
        final Map<String, Object> total = createKumFeature(featuresKum, false);
        total.put("gewName", null);
        total.put("code", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * alle Dimensionen.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesser2() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        sheetNames.add("Gewässer");
        for (int di = 0; di < GerinneGeschlGewaesserReportDialog.getInstance().getDimensions().size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
            final List<Integer> d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

            for (final Integer gew : gerOffenHelper.getGew()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;

                feature.put("anzGew", getGew().size());
                feature.put("group", null);
                feature.put("gewName", getGewName(gew));
                feature.put("code", getBaCd(gew));
                feature.put("arten", art);
                feature.put("laenge", gerOffenHelper.getLengthGewAll(gew));
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                for (final GerinneGeschlGemeindeReportDialog.Art a
                            : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                    double countA = 0;
                    double lengthA = 0;
                    if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + getArtPrefix(a) + (i + 1);
                            final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCountAllTfDim(gew, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gew, a, from, till, dimFrom, dimTill);
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

                if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
                    double countT = 0;
                    double lengthT = 0;

                    for (int i = 0; i < l.size(); ++i) {
                        for (final GerinneGeschlGemeindeReportDialog.Art a
                                    : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final double colCount = getCountAllTfDim(gew, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gew, a, from, till, dimFrom, dimTill);
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
        final Map<String, Object> total = createKumFeature(featuresKum, false);
        total.put("gewName", null);
        total.put("code", null);
        features.add(total);

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

        sheetNames.add("Objekte");
        for (final Integer gew : getGew()) {
            for (final GmdPartObjGeschl obj : getObjects(gew)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("group", null);
                feature.put("gewName", getGewName(gew));
                feature.put("code", getBaCd(gew));
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gew));
                feature.put("von", convertStation(obj.getFrom()));
                feature.put("bis", convertStation(obj.getTill()));
                feature.put("anzahlObj", objList.size());
                feature.put("tf", getTf(obj.getTf()));
                feature.put("dim", getDim(obj.getDim()));
                feature.put("ls", obj.getLs());
                feature.put("prof", obj.getProf());
                feature.put("ma", obj.getMa());
                feature.put("objNr", obj.getObjNr());
                feature.put("objNr_gu", obj.getObjNrGu());
                feature.put("tr", obj.getTr());
                feature.put("tr_gu", obj.getTrGu());
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
        final Map<String, Object> kumFeature = createKumFeature(features, false);
        kumFeature.put("code", null);
        kumFeature.put("gewName", null);
        kumFeature.put("von", null);
        kumFeature.put("bis", null);
//        features.add(kumFeature);

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuDataSource() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        boolean first = true;
        final String art = createArtString();

        sheetNames.add("GU");
        for (final String gu : getGu()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            double count = 0;
            double length = 0;
            feature.put("anzGu", getGu().size());
            feature.put("gu", gu);
            feature.put("guName", gu);
            feature.put("group", null);
            feature.put("arten", art);
            feature.put("laenge", getLengthGew(gu));

            if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + i;
                    final String laenge = "laenge" + i;
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllTf(gu, from, till);
                    final double colLength = getLengthAllTf(gu, from, till);
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
            } else if (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null) {
                final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

                for (int i = 0; i < l.size(); ++i) {
                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                    final Integer till = l.get(i);
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCountAllDim(gu, from, till);
                    final double colLength = getLengthAllDim(gu, from, till);
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
            } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog.getInstance()
                            .getArt();

                for (int i = 0; i < l.size(); ++i) {
                    final String anz = "anz" + (i + 1);
                    final String laenge = "laenge" + (i + 1);
                    final String lab = "lab" + (i + 1);
                    final double colCount = getCount(gu, l.get(i));
                    final double colLength = getLength(gu, l.get(i));

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

                    if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
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
     * tf und dim art und dim art und tiefe art (nur eine) und tf und dim.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuDataSource1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        sheetNames.add("Gu");
        for (int di = 0; di < d.size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

            for (final String gu : getGu()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;
                feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                feature.put("anzGu", getGu().size());
                feature.put("group", null);
                feature.put("guName", gu);
                feature.put("gu", gu);
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gu));

                if (!isTiefeArt && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                                    && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1)) {
                            final GerinneGeschlGemeindeReportDialog.Art arten = GerinneGeschlGewaesserReportDialog
                                        .getInstance().getArt().get(0);
                            colCount = getCountAllTfDim(gu, arten, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gu, arten, from, till, dimFrom, dimTill);
                        } else {
                            colCount = getCountAllTfDim(gu, from, till, dimFrom, dimTill);
                            colLength = getLengthAllTfDim(gu, from, till, dimFrom, dimTill);
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
                } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if (isTiefeArt) {
                            colCount = getCountTf(gu, l.get(i), dimFrom, dimTill);
                            colLength = getLengthTf(gu, l.get(i), dimFrom, dimTill);
                        } else {
                            colCount = getCountDim(gu, l.get(i), dimFrom, dimTill);
                            colLength = getLengthDim(gu, l.get(i), dimFrom, dimTill);
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

                        if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
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
    private FeatureDataSource getGuDataSource2() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        sheetNames.add("GU");
        for (int di = 0; di < GerinneGeschlGewaesserReportDialog.getInstance().getDimensions().size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
            final List<Integer> d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

            for (final String gu : getGu()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;

                feature.put("anzGew", getGew().size());
                feature.put("group", null);
                feature.put("guName", gu);
                feature.put("gu", gu);
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gu));
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                for (final GerinneGeschlGemeindeReportDialog.Art a
                            : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                    double countA = 0;
                    double lengthA = 0;
                    if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + getArtPrefix(a) + (i + 1);
                            final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCountAllTfDim(gu, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gu, a, from, till, dimFrom, dimTill);
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

                if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
                    double countT = 0;
                    double lengthT = 0;

                    for (int i = 0; i < l.size(); ++i) {
                        for (final GerinneGeschlGemeindeReportDialog.Art a
                                    : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final double colCount = getCountAllTfDim(gu, a, from, till, dimFrom, dimTill);
                            final double colLength = getLengthAllTfDim(gu, a, from, till, dimFrom, dimTill);
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
        final Map<String, Object> total = createKumFeature(featuresKum, false);
        total.put("gu", null);
        total.put("guName", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuWdmDataSource() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        boolean first = true;
        final String art = createArtString();

        sheetNames.add("GU");
        for (final String gu : getGu()) {
            final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
            for (final Integer wdm : getWdm(gu)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                double count = 0;
                double length = 0;
                feature.put("wdm", wdm);
                feature.put("anzGu", getGu().size());
                feature.put("gu", gu);
                feature.put("guName", gu);
                feature.put("group", null);
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gu, wdm));

                if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllTf(gu, wdm, from, till);
                        final double colLength = getLengthAllTf(gu, wdm, from, till);
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
                } else if (GerinneGeschlGewaesserReportDialog.getInstance().getDimensions() != null) {
                    final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllDim(gu, wdm, from, till);
                        final double colLength = getLengthAllDim(gu, wdm, from, till);
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
                } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCount(gu, wdm, l.get(i));
                        final double colLength = getLength(gu, wdm, l.get(i));

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

                        if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
                            parameters.put("ges", false);
                        }
                        first = false;
                    }
                }

                feature.put("anzGes", count);
                feature.put("laengeGes", length);

                subGroupFeatures.add(feature);
                features.add(feature);
            }
            features.add(createKumFeature(subGroupFeatures, true));
        }
        final Map<String, Object> total = createKumFeature(features, false);
        total.put("gu", null);
        total.put("guName", null);
        total.put("wdm", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * tf und dim art und dim art und tiefe art (nur eine) und tf und dim.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuWdmDataSource1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        sheetNames.add("Gu");
        for (int di = 0; di < d.size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

            for (final String gu : getGu()) {
                final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
                for (final Integer wdm : getWdm(gu)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;
                    feature.put("wdm", wdm);
                    feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                    feature.put("anzGu", getGu().size());
                    feature.put("group", null);
                    feature.put("guName", gu);
                    feature.put("gu", gu);
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gu, wdm));

                    if (!isTiefeArt && (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null)) {
                        final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + i;
                            final String laenge = "laenge" + i;
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if ((GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null)
                                        && (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1)) {
                                final GerinneGeschlGemeindeReportDialog.Art arten = GerinneGeschlGewaesserReportDialog
                                            .getInstance().getArt().get(0);
                                colCount = getCountAllTfDim(gu, wdm, arten, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gu, wdm, arten, from, till, dimFrom, dimTill);
                            } else {
                                colCount = getCountAllTfDim(gu, wdm, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gu, wdm, from, till, dimFrom, dimTill);
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
                    } else if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() != null) {
                        final List<GerinneGeschlGemeindeReportDialog.Art> l = GerinneGeschlGewaesserReportDialog
                                    .getInstance().getArt();

                        for (int i = 0; i < l.size(); ++i) {
                            final String anz = "anz" + (i + 1);
                            final String laenge = "laenge" + (i + 1);
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if (isTiefeArt) {
                                colCount = getCountTf(gu, wdm, l.get(i), dimFrom, dimTill);
                                colLength = getLengthTf(gu, wdm, l.get(i), dimFrom, dimTill);
                            } else {
                                colCount = getCountDim(gu, wdm, l.get(i), dimFrom, dimTill);
                                colLength = getLengthDim(gu, wdm, l.get(i), dimFrom, dimTill);
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

                            if (GerinneGeschlGewaesserReportDialog.getInstance().getArt().size() == 1) {
                                parameters.put("ges", false);
                            }
                            first = false;
                        }
                    }

                    feature.put("anzGes", count);
                    feature.put("laengeGes", length);

                    features.add(feature);
                    subGroupFeatures.add(feature);
                    featuresKum.add(feature);
                    featuresKumDim.add(feature);
                }
                features.add(createKumFeature(subGroupFeatures, true));
            }
            features.add(createKumFeature(featuresKumDim, true));
        }
        final Map<String, Object> total = createKumFeature(featuresKum, false);
        total.put("gu", null);
        total.put("guName", null);
        total.put("wdm", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * alle Dimensionen.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuWdmDataSource2() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        sheetNames.add("GU");
        for (int di = 0; di < GerinneGeschlGewaesserReportDialog.getInstance().getDimensions().size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
            final List<Integer> d = GerinneGeschlGewaesserReportDialog.getInstance().getDimensions();

            for (final String gu : getGu()) {
                final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
                for (final Integer wdm : getWdm(gu)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;

                    feature.put("wdm", wdm);
                    feature.put("anzGew", getGew().size());
                    feature.put("group", null);
                    feature.put("guName", gu);
                    feature.put("gu", gu);
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gu, wdm));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                    for (final GerinneGeschlGemeindeReportDialog.Art a
                                : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                        double countA = 0;
                        double lengthA = 0;
                        if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                            final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();

                            for (int i = 0; i < l.size(); ++i) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final String anz = "anz" + getArtPrefix(a) + (i + 1);
                                final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                                final String lab = "lab" + (i + 1);
                                final double colCount = getCountAllTfDim(
                                        gu,
                                        wdm,
                                        a,
                                        from,
                                        till,
                                        dimFrom,
                                        dimTill);
                                final double colLength = getLengthAllTfDim(
                                        gu,
                                        wdm,
                                        a,
                                        from,
                                        till,
                                        dimFrom,
                                        dimTill);
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

                    if (GerinneGeschlGewaesserReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlGewaesserReportDialog.getInstance().getClasses();
                        double countT = 0;
                        double lengthT = 0;

                        for (int i = 0; i < l.size(); ++i) {
                            for (final GerinneGeschlGemeindeReportDialog.Art a
                                        : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final double colCount = getCountAllTfDim(
                                        gu,
                                        wdm,
                                        a,
                                        from,
                                        till,
                                        dimFrom,
                                        dimTill);
                                final double colLength = getLengthAllTfDim(
                                        gu,
                                        wdm,
                                        a,
                                        from,
                                        till,
                                        dimFrom,
                                        dimTill);
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
                    subGroupFeatures.add(feature);
                    featuresKum.add(feature);
                    featuresKumDim.add(feature);
                }
                features.add(createKumFeature(subGroupFeatures, true));
            }
            features.add(createKumFeature(featuresKumDim, true));
        }
        final Map<String, Object> total = createKumFeature(featuresKum, false);
        total.put("gu", null);
        total.put("guName", null);
        total.put("wdm", null);
        features.add(total);

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew() {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjGeschl tmp : objList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<GmdPartObjGeschl> getGew(final String owner) {
        final List<GmdPartObjGeschl> list = new ArrayList<GmdPartObjGeschl>();

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(owner)) {
                list.add(tmp);
            }
        }

        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGewName(final int gew) {
        return gerOffenHelper.getGewName(gew);
//        for (final GmdPartObjGeschl tmp : objList) {
//            if (tmp.getId() == gew) {
//                return tmp.getGewName();
//            }
//        }
//
//        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final int gew) {
        return gerOffenHelper.getBaCd(gew);
//        for (final GmdPartObjGeschl tmp : objList) {
//            if (tmp.getId() == gew) {
//                return tmp.getBaCd();
//            }
//        }
//
//        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gewId) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getId() == gewId) {
                length += tmp.getLength();
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
     */
    private double getCount(final int gew) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew)) {
                ++count;
            }
        }

        return count;
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
    private double getCountAllDim(final int gew, final Integer from, final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final int gew, final GerinneGeschlGemeindeReportDialog.Art art) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final int gew, final GerinneGeschlGemeindeReportDialog.Art art) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
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
    private double getLengthAllDim(final int gew, final Integer from, final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
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
    private double getCountAllTf(final int gew, final Integer from, final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
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
    private double getLengthAllTf(final int gew, final Integer from, final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gew      DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gew      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final int gew,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gew      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final int gew,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gew   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final int gew,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if ((tmp.getId() == gew) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
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
     */
    private Collection<GmdPartObjGeschl> getObjects(final int gew) {
        final TreeSet<GmdPartObjGeschl> ts = new TreeSet<GmdPartObjGeschl>();

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getId() == gew) {
                ts.add(tmp);
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjGeschl tmp : objList) {
            ts.add(tmp.getOwner());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final String gu, final Integer from, final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final String gu, final Integer from, final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final String gu, final Integer from, final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final String gu, final Integer from, final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && !tmp.getArt().equals("p") && valueBetween(tmp.getDim(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   gu   gew DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final String gu, final GerinneGeschlGemeindeReportDialog.Art art) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gew DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final String gu, final GerinneGeschlGemeindeReportDialog.Art art) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu       gew DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu       gew DOCUMENT ME!
     * @param   art      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu       gew DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final String gu,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu       gew DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final String gu,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTfDim(final String gu,
            final Integer wdm,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu       gew DOCUMENT ME!
     * @param   wdm      DOCUMENT ME!
     * @param   tfFrom   DOCUMENT ME!
     * @param   tfTill   DOCUMENT ME!
     * @param   dimFrom  DOCUMENT ME!
     * @param   dimTill  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTfDim(final String gu,
            final Integer wdm,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name()) && valueBetween(tmp.getTf(), from, till)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final String gu,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu   DOCUMENT ME!
     * @param   wdm  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final String gu, final Integer wdm) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
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
    private Collection<Integer> getWdm(final String gu) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu)) {
                ts.add(tmp.getWidmung());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllTf(final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllTf(final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountAllDim(final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthAllDim(final String gu,
            final Integer wdm,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu   gew DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCount(final String gu, final Integer wdm, final GerinneGeschlGemeindeReportDialog.Art art) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getWidmung().equals(wdm) && tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gew DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLength(final String gu, final Integer wdm, final GerinneGeschlGemeindeReportDialog.Art art) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getWidmung().equals(wdm) && tmp.getOwner().equals(gu) && tmp.getArt().equals(art.name())) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
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
    private double getCountAllTfDim(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
    private double getLengthAllTfDim(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer tfFrom,
            final Integer tfTill,
            final Integer dimFrom,
            final Integer dimTill) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountTf(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthTf(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getCountDim(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        int count = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu    gew DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     * @param   art   DOCUMENT ME!
     * @param   from  DOCUMENT ME!
     * @param   till  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthDim(final String gu,
            final Integer wdm,
            final GerinneGeschlGemeindeReportDialog.Art art,
            final Integer from,
            final Integer till) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
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
     * @param   gu  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final String gu) {
        double length = 0;

        for (final GmdPartObjGeschl tmp : objList) {
            if (tmp.getOwner().equals(gu)) {
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

        if (GerinneGeschlGewaesserReportDialog.getInstance().getArt() == null) {
            art = "RL-D-Due";
        } else {
            art = null;
            for (final GerinneGeschlGemeindeReportDialog.Art a
                        : GerinneGeschlGewaesserReportDialog.getInstance().getArt()) {
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
