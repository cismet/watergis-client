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

import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.cismet.cids.custom.watergis.server.search.AllGewGeschlByGeom;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneGeschlFlaechenReportDialog;
import de.cismet.watergis.gui.dialog.GerinneGeschlGemeindeReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.Flaeche;
import de.cismet.watergis.reports.types.GmdPartObjGeschl;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneGFlReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGFlReport.class);
    private static final String[] exceptionalNumberFields = {
            "gmdNummer",
            "group",
            "gmdName",
            "code",
            "anzahlGu",
            "gu",
            "dimension",
            "dim"
        };
// private final Map<String, CidsLayer> layerMap = new HashMap<String, CidsLayer>();

    static {
        Arrays.sort(exceptionalNumberFields);
    }

    //~ Instance fields --------------------------------------------------------

    protected List<String> sheetNames = new ArrayList<String>();
    protected HashMap<String, Object> parameters = new HashMap<String, Object>();

    private final Map<Object, List<GmdPartObjGeschl>> gemPartMap = new HashMap<Object, List<GmdPartObjGeschl>>();
    private final Map<Object, KatasterFlaechenData> gemDataMap = new HashMap<Object, KatasterFlaechenData>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   fl   gemId baCd DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public File createFlaechenReport(final Flaeche[] fl, final int[] gew) throws Exception {
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("klasse", GerinneGeschlFlaechenReportDialog.getInstance().getFlaechenService().getName());
        parameters.put("attr1", GerinneGeschlFlaechenReportDialog.getInstance().getAttr1());
        parameters.put("attr2", GerinneGeschlFlaechenReportDialog.getInstance().getAttr2());
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
        parameters.put("dimension", (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null));
        parameters.put("tiefenklasse", (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null));
        parameters.put("fachdata", GerinneGeschlFlaechenReportDialog.getInstance().isAllDataPerObject());
        parameters.put("perObject", GerinneGeschlFlaechenReportDialog.getInstance().isPerObject());
        parameters.put("sumGu", GerinneGeschlFlaechenReportDialog.getInstance().isSumGu());
        parameters.put("withWdm", GerinneGeschlFlaechenReportDialog.getInstance().isPerWdm());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneGFlReport.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/GerinneGeschlFl.jasper"));

        init(fl, gew);

        if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                    && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                    && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                    && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses().size() > 1)) {
            parameters.put("gemKomp", true);
            dataSources.put("gemeinden", getGemeinden2());
        } else if (((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                    || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null))
                    || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses().size() == 1))) {
            parameters.put("gemKomp", false);
            dataSources.put("gemeinden", getGemeinden1());
        } else {
            parameters.put("gemKomp", false);
            dataSources.put("gemeinden", getGemeinden());
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().isPerGew()) {
            if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                dataSources.put("gewaesser", getGewaesser2());
            } else if (((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses().size() == 1))) {
                dataSources.put("gewaesser", getGewaesser1());
            } else {
                dataSources.put("gewaesser", getGewaesser());
            }
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().isPerObject()) {
            dataSources.put("objects", getObjects());
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().isSumGu()
                    && !GerinneGeschlFlaechenReportDialog.getInstance().isPerWdm()) {
            if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                dataSources.put("gu", getGuDataSource2());
            } else if (((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses().size() == 1))) {
                dataSources.put("gu", getGuDataSource1());
            } else {
                dataSources.put("gu", getGuDataSource());
            }
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().isSumGu()
                    && GerinneGeschlFlaechenReportDialog.getInstance().isPerWdm()) {
            if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                dataSources.put("gu", getGuWdmDataSource2());
            } else if (((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null))
                        || ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                            && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses().size() == 1))) {
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
                GerinneGeschlFlaechenReportDialog.getInstance().getPath()
                        + "/Flächen.xlsx");
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
        final GerinneGFlReport report = new GerinneGFlReport();
        try {
//            report.createRlDDueReport(new int[] { 2 }, new int[] { 2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fls       gemNr DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final Flaeche[] fls, final int[] routeIds) throws Exception {
        for (final Flaeche fl : fls) {
            gemPartMap.put(fl.getAttr1(), getAllRoutes(fl, routeIds));

            final Integer[] idList = getGew(fl.getAttr1()).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
            gemDataMap.put(fl.getAttr1(), new KatasterFlaechenData(fl.getAttr1(), fl.getAttr2(), routes));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fl        gemId DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<GmdPartObjGeschl> getAllRoutes(final Flaeche fl, final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewGeschlByGeom(routeIds, getAllowedWdms(), fl.getGeom());
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
     * @return  DOCUMENT ME!
     */
    private int[] getAllowedWdms() {
        final List<Integer> wdmList = new ArrayList<Integer>();

        if (GerinneGeschlFlaechenReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneGeschlFlaechenReportDialog.getInstance().is1505()) {
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
    private FeatureDataSource getGemeinden() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        for (final Object gem : gemDataMap.keySet()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            double count = 0;
            double length = 0;
            feature.put("anzahlGmd", gemDataMap.size());
            feature.put("group", "gemeinde");
            feature.put("gmdNr", gem);
            feature.put("gmdName", gemDataMap.get(gem).getAttr2());
            feature.put("art", art);

            if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

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
            } else if (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null) {
                final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

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
            } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog.getInstance()
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

                    if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
    private FeatureDataSource getGemeinden1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        for (int di = 0; di < d.size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

            for (final Object gem : gemDataMap.keySet()) {
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
                feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                feature.put("art", art);

                if (!isTiefeArt && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        double colCount;
                        double colLength;

                        if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                                    && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1)) {
                            final GerinneGeschlFlaechenReportDialog.Art arten = GerinneGeschlFlaechenReportDialog
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
                } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
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

                        if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
    private FeatureDataSource getGemeinden2() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");
        boolean first = true;
        final String art = createArtString();

        for (int di = 0; di < GerinneGeschlFlaechenReportDialog.getInstance().getDimensions().size(); ++di) {
            final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
            final List<Integer> d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

            for (final Object gem : gemDataMap.keySet()) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                final Integer dimTill = d.get(di);
                double count = 0;
                double length = 0;

                feature.put("anzahlGmd", gemDataMap.size());
                feature.put("group", null);
                feature.put("gmdNr", gem);
                feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                feature.put("art", art);
                feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                for (final GerinneGeschlFlaechenReportDialog.Art a
                            : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                    double countA = 0;
                    double lengthA = 0;
                    if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

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

                if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
                    double countT = 0;
                    double lengthT = 0;

                    for (int i = 0; i < l.size(); ++i) {
                        for (final GerinneGeschlFlaechenReportDialog.Art a
                                    : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
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
    private FeatureDataSource getGewaesser() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        boolean first = true;
        final String art = createArtString();

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("" + gemDataMap.get(gem).getAttr2());
            for (final Integer gew : getGew(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                double count = 0;
                double length = 0;
                feature.put("anzGew", gemDataMap.size());
                feature.put("group", String.valueOf(gem));
                feature.put("gmdNr", gem);
                feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                feature.put("gewName", getGewName(gem, gew));
                feature.put("code", getBaCd(gem, gew));
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gem, gew));

                if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllTf(gem, gew, from, till);
                        final double colLength = getLengthAllTf(gem, gew, from, till);
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
                } else if (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllDim(gem, gew, from, till);
                        final double colLength = getLengthAllDim(gem, gew, from, till);
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
                } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCount(gem, gew, l.get(i));
                        final double colLength = getLength(gem, gew, l.get(i));

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

                        if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
                            parameters.put("ges", false);
                        }
                        first = false;
                    }
                }

                feature.put("anzGes", count);
                feature.put("laengeGes", length);

                features.add(feature);
            }
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
    private FeatureDataSource getGewaesser1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("" + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < d.size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

                for (final Integer gew : getGew(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;
                    feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                    feature.put("anzGew", getGew(gem).size());
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                    feature.put("gewName", getGewName(gem, gew));
                    feature.put("code", getBaCd(gem, gew));
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gew));

                    if (!isTiefeArt && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + i;
                            final String laenge = "laenge" + i;
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1)) {
                                final GerinneGeschlFlaechenReportDialog.Art arten = GerinneGeschlFlaechenReportDialog
                                            .getInstance().getArt().get(0);
                                colCount = getCountAllTfDim(gem, gew, arten, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gem, gew, arten, from, till, dimFrom, dimTill);
                            } else {
                                colCount = getCountAllTfDim(gem, gew, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gem, gew, from, till, dimFrom, dimTill);
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
                    } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                        final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                    .getInstance().getArt();

                        for (int i = 0; i < l.size(); ++i) {
                            final String anz = "anz" + (i + 1);
                            final String laenge = "laenge" + (i + 1);
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if (isTiefeArt) {
                                colCount = getCountTf(gem, gew, l.get(i), dimFrom, dimTill);
                                colLength = getLengthTf(gem, gew, l.get(i), dimFrom, dimTill);
                            } else {
                                colCount = getCountDim(gem, gew, l.get(i), dimFrom, dimTill);
                                colLength = getLengthDim(gem, gew, l.get(i), dimFrom, dimTill);
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

                            if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
        }

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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("" + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < GerinneGeschlFlaechenReportDialog.getInstance().getDimensions().size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
                final List<Integer> d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                for (final Integer gew : getGew(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;

                    feature.put("anzGew", getGew(gem).size());
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                    feature.put("gewName", getGewName(gem, gew));
                    feature.put("code", getBaCd(gem, gew));
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gew));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                    for (final GerinneGeschlFlaechenReportDialog.Art a
                                : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                        double countA = 0;
                        double lengthA = 0;
                        if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                            final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                            for (int i = 0; i < l.size(); ++i) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final String anz = "anz" + getArtPrefix(a) + (i + 1);
                                final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                                final String lab = "lab" + (i + 1);
                                final double colCount = getCountAllTfDim(gem, gew, a, from, till, dimFrom, dimTill);
                                final double colLength = getLengthAllTfDim(gem, gew, a, from, till, dimFrom, dimTill);
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

                    if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
                        double countT = 0;
                        double lengthT = 0;

                        for (int i = 0; i < l.size(); ++i) {
                            for (final GerinneGeschlFlaechenReportDialog.Art a
                                        : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final double colCount = getCountAllTfDim(gem, gew, a, from, till, dimFrom, dimTill);
                                final double colLength = getLengthAllTfDim(gem, gew, a, from, till, dimFrom, dimTill);
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
        }
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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getAttr2());
            for (final String gu : getGu(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                double count = 0;
                double length = 0;
                feature.put("anzGu", getGu(gem).size());
                feature.put("gu", gu);
                feature.put("guName", gu);
                feature.put("group", String.valueOf(gem));
                feature.put("gmdNr", gem);
                feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                feature.put("arten", art);
                feature.put("laenge", getLengthGew(gem, gu));

                if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + i;
                        final String laenge = "laenge" + i;
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllTf(gem, gu, from, till);
                        final double colLength = getLengthAllTf(gem, gu, from, till);
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
                } else if (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null) {
                    final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                    for (int i = 0; i < l.size(); ++i) {
                        final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                        // a NPE
                        final Integer till = l.get(i);
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCountAllDim(gem, gu, from, till);
                        final double colLength = getLengthAllDim(gem, gu, from, till);
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
                } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                    final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                .getInstance().getArt();

                    for (int i = 0; i < l.size(); ++i) {
                        final String anz = "anz" + (i + 1);
                        final String laenge = "laenge" + (i + 1);
                        final String lab = "lab" + (i + 1);
                        final double colCount = getCount(gem, gu, l.get(i));
                        final double colLength = getLength(gem, gu, l.get(i));

                        count += colCount;
                        length += colLength;

                        feature.put(lab, l.get(i).name().toUpperCase());
                        feature.put(anz, colCount);
                        feature.put(laenge, colLength);
                    }

                    if (first) {
                        for (int i = 0; i < l.size(); ++i) {
                            parameters.put("spalte" + (i + 1), true);
                        }

                        if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
                            parameters.put("ges", false);
                        }
                        first = false;
                    }
                }

                feature.put("anzGes", count);
                feature.put("laengeGes", length);

                features.add(feature);
            }
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

        if ((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("Gu " + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < d.size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

                for (final String gu : getGu(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;
                    feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                    feature.put("anzGu", getGu(gem).size());
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                    feature.put("guName", gu);
                    feature.put("gu", gu);
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gu));

                    if (!isTiefeArt && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + i;
                            final String laenge = "laenge" + i;
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1)) {
                                final GerinneGeschlFlaechenReportDialog.Art arten = GerinneGeschlFlaechenReportDialog
                                            .getInstance().getArt().get(0);
                                colCount = getCountAllTfDim(gem, gu, arten, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gem, gu, arten, from, till, dimFrom, dimTill);
                            } else {
                                colCount = getCountAllTfDim(gem, gu, from, till, dimFrom, dimTill);
                                colLength = getLengthAllTfDim(gem, gu, from, till, dimFrom, dimTill);
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
                    } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                        final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                    .getInstance().getArt();

                        for (int i = 0; i < l.size(); ++i) {
                            final String anz = "anz" + (i + 1);
                            final String laenge = "laenge" + (i + 1);
                            final String lab = "lab" + (i + 1);
                            double colCount;
                            double colLength;

                            if (isTiefeArt) {
                                colCount = getCountTf(gem, gu, l.get(i), dimFrom, dimTill);
                                colLength = getLengthTf(gem, gu, l.get(i), dimFrom, dimTill);
                            } else {
                                colCount = getCountDim(gem, gu, l.get(i), dimFrom, dimTill);
                                colLength = getLengthDim(gem, gu, l.get(i), dimFrom, dimTill);
                            }

                            count += colCount;
                            length += colLength;

                            feature.put(lab, l.get(i).name().toUpperCase());
                            feature.put(anz, colCount);
                            feature.put(laenge, colLength);
                        }

                        if (first) {
                            for (int i = 0; i < l.size(); ++i) {
                                parameters.put("spalte" + (i + 1), true);
                            }

                            if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
        }

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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < GerinneGeschlFlaechenReportDialog.getInstance().getDimensions().size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
                final List<Integer> d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                for (final String gu : getGu(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to prevent
                                                                                         // a NPE
                    final Integer dimTill = d.get(di);
                    double count = 0;
                    double length = 0;

                    feature.put("anzGew", getGew(gem).size());
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                    feature.put("guName", gu);
                    feature.put("gu", gu);
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gu));
                    feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                    for (final GerinneGeschlFlaechenReportDialog.Art a
                                : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                        double countA = 0;
                        double lengthA = 0;
                        if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                            final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                            for (int i = 0; i < l.size(); ++i) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final String anz = "anz" + getArtPrefix(a) + (i + 1);
                                final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                                final String lab = "lab" + (i + 1);
                                final double colCount = getCountAllTfDim(gem, gu, a, from, till, dimFrom, dimTill);
                                final double colLength = getLengthAllTfDim(gem, gu, a, from, till, dimFrom, dimTill);
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

                    if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
                        double countT = 0;
                        double lengthT = 0;

                        for (int i = 0; i < l.size(); ++i) {
                            for (final GerinneGeschlFlaechenReportDialog.Art a
                                        : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final double colCount = getCountAllTfDim(gem, gu, a, from, till, dimFrom, dimTill);
                                final double colLength = getLengthAllTfDim(gem, gu, a, from, till, dimFrom, dimTill);
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
        }
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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getAttr2());
            for (final String gu : getGu(gem)) {
                final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
                for (final Integer wdm : getWdm(gem, gu)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    double count = 0;
                    double length = 0;
                    feature.put("wdm", wdm);
                    feature.put("anzGu", getGu(gem).size());
                    feature.put("gu", gu);
                    feature.put("guName", gu);
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                    feature.put("arten", art);
                    feature.put("laenge", getLengthGew(gem, gu, wdm));

                    if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + i;
                            final String laenge = "laenge" + i;
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCountAllTf(gem, gu, wdm, from, till);
                            final double colLength = getLengthAllTf(gem, gu, wdm, from, till);
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
                    } else if (GerinneGeschlFlaechenReportDialog.getInstance().getDimensions() != null) {
                        final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                        for (int i = 0; i < l.size(); ++i) {
                            final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                            // prevent a NPE
                            final Integer till = l.get(i);
                            final String anz = "anz" + (i + 1);
                            final String laenge = "laenge" + (i + 1);
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCountAllDim(gem, gu, wdm, from, till);
                            final double colLength = getLengthAllDim(gem, gu, wdm, from, till);
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
                    } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                        final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                    .getInstance().getArt();

                        for (int i = 0; i < l.size(); ++i) {
                            final String anz = "anz" + (i + 1);
                            final String laenge = "laenge" + (i + 1);
                            final String lab = "lab" + (i + 1);
                            final double colCount = getCount(gem, gu, wdm, l.get(i));
                            final double colLength = getLength(gem, gu, wdm, l.get(i));

                            count += colCount;
                            length += colLength;

                            feature.put(lab, l.get(i).name().toUpperCase());
                            feature.put(anz, colCount);
                            feature.put(laenge, colLength);
                        }

                        if (first) {
                            for (int i = 0; i < l.size(); ++i) {
                                parameters.put("spalte" + (i + 1), true);
                            }

                            if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
    private FeatureDataSource getGuWdmDataSource1() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featuresKum = new ArrayList<Map<String, Object>>();
//        sheetNames.add("Gemeinden");
        boolean first = true;
        parameters.put("dimension", true);
        boolean isTiefeArt;
        List<Integer> d;
        final String art = createArtString();

        if ((GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)
                    && ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                        && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() > 1))) {
            // Art und Tiefe als Dimension
            d = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
            isTiefeArt = true;
        } else {
            d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
            isTiefeArt = false;
        }

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("Gu " + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < d.size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();

                for (final String gu : getGu(gem)) {
                    final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
                    for (final Integer wdm : getWdm(gem, gu)) {
                        final Map<String, Object> feature = new HashMap<String, Object>();
                        final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to
                                                                                             // prevent a NPE
                        final Integer dimTill = d.get(di);
                        double count = 0;
                        double length = 0;
                        feature.put("wdm", wdm);
                        feature.put("dimLab", (isTiefeArt ? "Tiefe" : "Dimension"));
                        feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));
                        feature.put("anzGu", getGu(gem).size());
                        feature.put("group", String.valueOf(gem));
                        feature.put("gmdNr", gem);
                        feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                        feature.put("guName", gu);
                        feature.put("gu", gu);
                        feature.put("arten", art);
                        feature.put("laenge", getLengthGew(gem, gu, wdm));

                        if (!isTiefeArt && (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null)) {
                            final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                            for (int i = 0; i < l.size(); ++i) {
                                final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to
                                                                                                // prevent a NPE
                                final Integer till = l.get(i);
                                final String anz = "anz" + i;
                                final String laenge = "laenge" + i;
                                final String lab = "lab" + (i + 1);
                                double colCount;
                                double colLength;

                                if ((GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null)
                                            && (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1)) {
                                    final GerinneGeschlFlaechenReportDialog.Art arten =
                                        GerinneGeschlFlaechenReportDialog.getInstance().getArt().get(0);
                                    colCount = getCountAllTfDim(gem, gu, wdm, arten, from, till, dimFrom, dimTill);
                                    colLength = getLengthAllTfDim(gem, gu, wdm, arten, from, till, dimFrom, dimTill);
                                } else {
                                    colCount = getCountAllTfDim(gem, gu, wdm, from, till, dimFrom, dimTill);
                                    colLength = getLengthAllTfDim(gem, gu, wdm, from, till, dimFrom, dimTill);
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
                        } else if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() != null) {
                            final List<GerinneGeschlFlaechenReportDialog.Art> l = GerinneGeschlFlaechenReportDialog
                                        .getInstance().getArt();

                            for (int i = 0; i < l.size(); ++i) {
                                final String anz = "anz" + (i + 1);
                                final String laenge = "laenge" + (i + 1);
                                final String lab = "lab" + (i + 1);
                                double colCount;
                                double colLength;

                                if (isTiefeArt) {
                                    colCount = getCountTf(gem, gu, wdm, l.get(i), dimFrom, dimTill);
                                    colLength = getLengthTf(gem, gu, wdm, l.get(i), dimFrom, dimTill);
                                } else {
                                    colCount = getCountDim(gem, gu, wdm, l.get(i), dimFrom, dimTill);
                                    colLength = getLengthDim(gem, gu, wdm, l.get(i), dimFrom, dimTill);
                                }

                                count += colCount;
                                length += colLength;

                                feature.put(lab, l.get(i).name().toUpperCase());
                                feature.put(anz, colCount);
                                feature.put(laenge, colLength);
                            }

                            if (first) {
                                for (int i = 0; i < l.size(); ++i) {
                                    parameters.put("spalte" + (i + 1), true);
                                }

                                if (GerinneGeschlFlaechenReportDialog.getInstance().getArt().size() == 1) {
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
            features.add(createKumFeature(featuresKum, false));
        }

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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getAttr2());
            for (int di = 0; di < GerinneGeschlFlaechenReportDialog.getInstance().getDimensions().size(); ++di) {
                final List<Map<String, Object>> featuresKumDim = new ArrayList<Map<String, Object>>();
                final List<Integer> d = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();

                for (final String gu : getGu(gem)) {
                    final List<Map<String, Object>> subGroupFeatures = new ArrayList<Map<String, Object>>();
                    for (final Integer wdm : getWdm(gem, gu)) {
                        final Map<String, Object> feature = new HashMap<String, Object>();
                        final Integer dimFrom = ((di > 0) ? d.get(di - 1) : new Integer(0)); // new Integer(0) to
                                                                                             // prevent a NPE
                        final Integer dimTill = d.get(di);
                        double count = 0;
                        double length = 0;

                        feature.put("wdm", wdm);
                        feature.put("anzGew", getGew(gem).size());
                        feature.put("group", String.valueOf(gem));
                        feature.put("gmdNr", gem);
                        feature.put("gmdName", gemDataMap.get(gem).getAttr2());
                        feature.put("guName", gu);
                        feature.put("gu", gu);
                        feature.put("arten", art);
                        feature.put("laenge", getLengthGew(gem, gu, wdm));
                        feature.put("dimension", ((d.get(di) == null) ? "ohne" : ("bis " + d.get(di))));

                        for (final GerinneGeschlFlaechenReportDialog.Art a
                                    : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                            double countA = 0;
                            double lengthA = 0;
                            if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                                final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();

                                for (int i = 0; i < l.size(); ++i) {
                                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                                    final Integer till = l.get(i);
                                    final String anz = "anz" + getArtPrefix(a) + (i + 1);
                                    final String laenge = "laenge" + getArtPrefix(a) + (i + 1);
                                    final String lab = "lab" + (i + 1);
                                    final double colCount = getCountAllTfDim(
                                            gem,
                                            gu,
                                            wdm,
                                            a,
                                            from,
                                            till,
                                            dimFrom,
                                            dimTill);
                                    final double colLength = getLengthAllTfDim(
                                            gem,
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

                        if (GerinneGeschlFlaechenReportDialog.getInstance().getClasses() != null) {
                            final List<Integer> l = GerinneGeschlFlaechenReportDialog.getInstance().getClasses();
                            double countT = 0;
                            double lengthT = 0;

                            for (int i = 0; i < l.size(); ++i) {
                                for (final GerinneGeschlFlaechenReportDialog.Art a
                                            : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                                    final Integer from = ((i > 0) ? l.get(i - 1) : new Integer(0)); // new Integer(0) to prevent a NPE
                                    final Integer till = l.get(i);
                                    final double colCount = getCountAllTfDim(
                                            gem,
                                            gu,
                                            wdm,
                                            a,
                                            from,
                                            till,
                                            dimFrom,
                                            dimTill);
                                    final double colLength = getLengthAllTfDim(
                                            gem,
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
            features.add(createKumFeature(featuresKum, false));
        }
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

        for (final Object gem : gemDataMap.keySet()) {
            sheetNames.add("Objekte " + gemDataMap.get(gem).getAttr2());
            for (final Integer gew : getGew(gem)) {
                for (final GmdPartObjGeschl obj : getObjects(gem, gew)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    feature.put("group", String.valueOf(gem));
                    feature.put("gmdNr", gem);
                    feature.put("gmdName", gemDataMap.get(gem).getAttr2());
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
     * @param   d  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Integer getDim(final Double d) {
        final List<Integer> dims = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
        int lastDim = 0;

        if ((dims == null) || (d == null)) {
            return toInteger(d);
        }

        for (final Integer tmp : dims) {
            if (tmp == null) {
                continue;
            }
            if (d > tmp) {
                return lastDim;
            } else {
                lastDim = tmp;
            }
        }

        return toInteger(d);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tf  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Integer getTf(final Double tf) {
        final List<Integer> tfs = GerinneGeschlFlaechenReportDialog.getInstance().getDimensions();
        int lastTf = 0;

        if ((tfs == null) || (tf == null)) {
            return toInteger(tf);
        }

        for (final int tmp : tfs) {
            if (tf > tmp) {
                return lastTf;
            } else {
                lastTf = tmp;
            }
        }

        return toInteger(tf);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   d  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Integer toInteger(final Double d) {
        if (d == null) {
            return null;
        } else {
            return d.intValue();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureListKum  DOCUMENT ME!
     * @param   subtotal        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Map<String, Object> createKumFeature(final List<Map<String, Object>> featureListKum,
            final boolean subtotal) {
        final Map<String, Object> kumFeature = new HashMap<String, Object>();

        kumFeature.put("summe", Boolean.TRUE);
        kumFeature.put("zwischenSumme", subtotal);

        if ((featureListKum == null) || featureListKum.isEmpty()) {
            return kumFeature;
        }

        final Map<String, Object> firstElement = featureListKum.get(0);

        for (final String key : firstElement.keySet()) {
            final Object value = firstElement.get(key);

            if ((Arrays.binarySearch(exceptionalNumberFields, key) < 0) && (value instanceof Integer)) {
                int sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    sum += (Integer)f.get(key);
                }

                kumFeature.put(key, sum);
            } else if ((Arrays.binarySearch(exceptionalNumberFields, key) < 0) && (value instanceof Double)) {
                double sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    sum += (Double)f.get(key);
                }

                kumFeature.put(key, sum);
            } else {
                kumFeature.put(key, value);
            }
        }

        return kumFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   art  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getArtPrefix(final GerinneGeschlFlaechenReportDialog.Art art) {
        final char firstCharacter = Character.toUpperCase(art.name().charAt(0));
        String prefix;

        if (art.name().length() > 1) {
            prefix = firstCharacter + art.name().substring(1);
        } else {
            prefix = String.valueOf(firstCharacter);
        }

        return prefix;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String createArtString() {
        String art;

        if (GerinneGeschlFlaechenReportDialog.getInstance().getArt() == null) {
            art = "RL / D / Due";
        } else {
            art = null;
            for (final GerinneGeschlFlaechenReportDialog.Art a
                        : GerinneGeschlFlaechenReportDialog.getInstance().getArt()) {
                if (art == null) {
                    art = a.name();
                } else {
                    art += " / " + a.name();
                }
            }
        }

        return art;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final Object gemNr) {
        final List<GmdPartObjGeschl> gemList = gemPartMap.get(gemNr);
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
    private Collection<GmdPartObjGeschl> getObjects(final Object gemNr, final int gew) {
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
    private Collection<String> getGu(final Object gemNr) {
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
    private Collection<Integer> getWdm(final Object gemNr, final String gu) {
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
    private double getCountAllDim(final Object gemNr, final Integer from, final Integer till) {
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
    private double getLengthAllDim(final Object gemNr, final Integer from, final Integer till) {
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
    private double getCountDim(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthDim(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountTf(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthTf(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCount(final Object gemNr, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getLength(final Object gemNr, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getCountAllTf(final Object gemNr, final Integer from, final Integer till) {
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
    private double getLengthAllTf(final Object gemNr, final Integer from, final Integer till) {
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
    private double getCountAllTfDim(final Object gemNr,
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
    private double getLengthAllTfDim(final Object gemNr,
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
    private double getCountAllTfDim(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthAllTfDim(final Object gemNr,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountAllDim(final Object gemNr, final int gew, final Integer from, final Integer till) {
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
    private double getLengthAllDim(final Object gemNr, final int gew, final Integer from, final Integer till) {
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
    private double getCountAllDim(final Object gemNr, final String gu, final Integer from, final Integer till) {
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
    private double getLengthAllDim(final Object gemNr, final String gu, final Integer from, final Integer till) {
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
    private double getCountAllDim(final Object gemNr,
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
    private double getLengthAllDim(final Object gemNr,
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
    private double getCountDim(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthDim(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountTf(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthTf(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountDim(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthDim(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountTf(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthTf(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountDim(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthDim(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountTf(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthTf(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCount(final Object gemNr, final int gew, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getLength(final Object gemNr, final int gew, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getCount(final Object gemNr, final String gu, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getLength(final Object gemNr, final String gu, final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getCount(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getLength(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art) {
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
    private double getCountAllTf(final Object gemNr, final int gew, final Integer from, final Integer till) {
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
    private double getLengthAllTf(final Object gemNr, final int gew, final Integer from, final Integer till) {
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
    private double getCountAllTf(final Object gemNr, final String gu, final Integer from, final Integer till) {
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
    private double getLengthAllTf(final Object gemNr, final String gu, final Integer from, final Integer till) {
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
    private double getCountAllTf(final Object gemNr,
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
    private double getLengthAllTf(final Object gemNr,
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
    private double getCountAllTfDim(final Object gemNr,
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
    private double getLengthAllTfDim(final Object gemNr,
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
    private double getCountAllTfDim(final Object gemNr,
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
    private double getLengthAllTfDim(final Object gemNr,
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
    private double getCountAllTfDim(final Object gemNr,
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
    private double getLengthAllTfDim(final Object gemNr,
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
    private double getCountAllTfDim(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthAllTfDim(final Object gemNr,
            final int gew,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountAllTfDim(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthAllTfDim(final Object gemNr,
            final String gu,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getCountAllTfDim(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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
    private double getLengthAllTfDim(final Object gemNr,
            final String gu,
            final Integer wdm,
            final GerinneGeschlFlaechenReportDialog.Art art,
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

        for (final Object gemNr : gemPartMap.keySet()) {
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
    private String getBaCd(final Object gemNr, final int gew) {
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
    private String getGewName(final Object gemNr, final int gew) {
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
    private int getCountGewAll(final Object gemNr) {
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
    private double getLengthGewAll(final Object gemNr) {
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
    private double getLengthGew(final Object gemNr, final int gewId) {
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
    private double getLengthGew(final Object gemNr, final String gu) {
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
    private double getLengthGew(final String gu, final int wdm) {
        double length = 0;

        for (final Object gemNr : gemPartMap.keySet()) {
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
    private int getCountGew(final Object gemNr, final String gu) {
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
    private int getCountGew(final String gu, final int wdm) {
        int count = 0;

        for (final Object gemNr : gemPartMap.keySet()) {
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
    private double getLengthGew(final Object gemNr, final String gu, final int wdm) {
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
     * @param   station  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String convertStation(final Double station) {
        final int km = (int)(station / 1000);
        final int m = (int)(station % 1000);
        String mString = String.valueOf(m);

        while (mString.length() < 3) {
            mString = "0" + mString;
        }

        return km + "+" + mString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   a  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String toColumnName(final GerinneGeschlFlaechenReportDialog.Art a) {
        if (a.equals(GerinneGeschlFlaechenReportDialog.Art.d)) {
            return "D";
        } else if (a.equals(GerinneGeschlFlaechenReportDialog.Art.due)) {
            return "Due";
        } else if (a.equals(GerinneGeschlFlaechenReportDialog.Art.rl)) {
            return "RL";
        } else {
            return a.name();
        }
    }
}
