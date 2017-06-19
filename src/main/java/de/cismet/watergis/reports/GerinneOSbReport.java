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
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

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

import de.cismet.cids.custom.watergis.server.search.AllGewGeschlBySb;
import de.cismet.cids.custom.watergis.server.search.AllGewOffenByGem;
import de.cismet.cids.custom.watergis.server.search.AllGewOffenBySb;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneOSbReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GemeindenDataLightweight;
import de.cismet.watergis.reports.types.GmdPartObj;
import de.cismet.watergis.reports.types.GmdPartObjGeschl;
import de.cismet.watergis.reports.types.GmdPartObjOffen;
import de.cismet.watergis.reports.types.KatasterGewObj;
import de.cismet.watergis.reports.types.SbObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOSbReport extends GerinneOGemeindeReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOSbReport.class);
    private static final String[] exceptionalNumberFields = {
            "gmdNummer",
            "gmdName",
            "code",
            "anzahlGu",
            "gu"
        };
    private static final int PROFSTAT = 1;
// private final Map<String, CidsLayer> layerMap = new HashMap<String, CidsLayer>();

    static {
        Arrays.sort(exceptionalNumberFields);
    }

    //~ Instance fields --------------------------------------------------------

    private List<GmdPartObjOffen> objList;
    private final List<String> sheetNames = new ArrayList<String>();
    private SbHelper helper;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createSbReport(final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("perSb", GerinneOSbReportDialog.getInstance().isPerSb());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneOSbReport.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/GerinneOffenSb.jasper"));

        init(gew);
        helper = new SbHelper(gew, getAllowedWdms());

        dataSources.put("gewaesser", getSb());

        if (GerinneOSbReportDialog.getInstance().isPerSb()) {
            dataSources.put("gewaesserAbschnitt", getSbPerSheet());
        }

        // create print from report and data
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        final FileOutputStream fout = new FileOutputStream(new File(
                    GerinneOSbReportDialog.getInstance().getPath()
                            + "/GerinneOffenSchaubezirke.xls"));
        final BufferedOutputStream out = new BufferedOutputStream(fout);
        final JRXlsExporter exporter = new JRXlsExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        final SimpleOutputStreamExporterOutput exportOut = new SimpleOutputStreamExporterOutput(out);
        exporter.setExporterOutput(exportOut);

        final SimpleXlsReportConfiguration config = new SimpleXlsReportConfiguration();
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
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final GerinneOSbReport report = new GerinneOSbReport();
        try {
            report.createGemeindeReport(new int[] { 2 }, new int[] { 2 });
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
        objList = getAllRoutes(routeIds);
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
    private List<GmdPartObjOffen> getAllRoutes(final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewOffenBySb(routeIds, getAllowedWdms());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GmdPartObjOffen> objList = new ArrayList<GmdPartObjOffen>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GmdPartObjOffen(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(2),
                        (Integer)f.get(3),
                        (String)f.get(4),
                        (String)f.get(5),
                        (Double)f.get(6),
                        (Double)f.get(7),
                        (Integer)f.get(8),
                        (Integer)f.get(9),
                        (String)f.get(10),
                        (Double)f.get(11),
                        (Double)f.get(12),
                        (Double)f.get(13),
                        (Double)f.get(14),
                        (Double)f.get(15),
                        (Double)f.get(16),
                        (Double)f.get(17),
                        (Double)f.get(18),
                        (Double)f.get(18),
                        (Double)f.get(20),
                        (Double)f.get(21),
                        (Double)f.get(22),
                        (String)f.get(23),
                        (Double)f.get(24),
                        (Double)f.get(25),
                        (String)f.get(26),
                        (Double)f.get(27),
                        (Double)f.get(28),
                        (Double)f.get(29),
                        (Double)f.get(30),
                        (String)f.get(31),
                        (Double)f.get(32),
                        (Integer)f.get(33),
                        (Double)f.get(34),
                        (Double)f.get(35),
                        (Double)f.get(36),
                        (Double)f.get(37),
                        (Double)f.get(38),
                        (Double)f.get(39),
                        (Double)f.get(40),
                        (Double)f.get(41),
                        (Double)f.get(42),
                        (Double)f.get(43),
                        (Double)f.get(44),
                        (Double)f.get(45),
                        (Double)f.get(46),
                        (Double)f.get(47),
                        (Double)f.get(48),
                        (String)f.get(49)));
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

        if (GerinneOSbReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneOSbReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneOSbReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneOSbReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneOSbReportDialog.getInstance().is1505()) {
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
    private FeatureDataSource getSb() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        sheetNames.add("Schaubezirke");

        for (final String gu : helper.getGu()) {
            final List<Map<String, Object>> featuresGu = new ArrayList<Map<String, Object>>();
            final int anzSb = helper.getSbCount(gu);

            for (final Integer wdm : helper.getWidmung(gu)) {
                final List<Map<String, Object>> featuresWdm = new ArrayList<Map<String, Object>>();

                for (final Integer sb : helper.getSb(gu, wdm)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final Collection<SbObj> sbParts = helper.getSbPart(gu, wdm, sb);
                    final double offenLength = helper.getLengthOffeneAbschn(gu, wdm, sb);
                    final double prof = getLengthGew(sbParts);
                    final double mw = getLengthMw(sbParts);
                    final double gewAll = helper.getLengthGewAll(gu, wdm, sb);
                    final SbObj firstPart = sbParts.toArray(new SbObj[sbParts.size()])[0];

                    feature.put("teil", ((gewAll == firstPart.getBaLen()) ? null : "x"));
                    feature.put("group", gu);
                    feature.put("name", gu);
                    feature.put("gu", helper.getGuId(gu));
                    feature.put("sb", sb);
                    feature.put("anzSb", anzSb);
                    feature.put("sbName", helper.getSbName(sb));
                    feature.put("wdm", wdm);
                    feature.put("gew_a", helper.getCountGewAll(gu, wdm, sb));
                    feature.put("gew_l", gewAll);
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", sbParts));
                    feature.put("profTrap_l", getLengthProf("tr", sbParts));
                    feature.put("profRe_a", getCountProf("re", sbParts));
                    feature.put("profRe_l", getLengthProf("re", sbParts));
                    feature.put("brSohleMin", getMinMax("brSo", sbParts, true));
                    feature.put("brSohleMit", getMit("brSo", sbParts));
                    feature.put("brSohleMax", getMinMax("brSo", sbParts, false));
                    feature.put("bvReMin", getMinMax("bvRe", sbParts, true));
                    feature.put("bvReMit", getMit("bvRe", sbParts));
                    feature.put("bvReMax", getMinMax("bvRe", sbParts, false));
                    feature.put("bhReMin", getMinMax("bhRe", sbParts, true));
                    feature.put("bhReMit", getMit("bhRe", sbParts));
                    feature.put("bhReMax", getMinMax("bhRe", sbParts, false));
                    feature.put("blReMin", getMinMax("blRe", sbParts, true));
                    feature.put("blReMit", getMit("blRe", sbParts));
                    feature.put("blReMax", getMinMax("blRe", sbParts, false));
                    feature.put("bvLiMin", getMinMax("bvLi", sbParts, true));
                    feature.put("bvLiMit", getMit("bvLi", sbParts));
                    feature.put("bvLiMax", getMinMax("bvLi", sbParts, false));
                    feature.put("bhLiMin", getMinMax("bhLi", sbParts, true));
                    feature.put("bhLiMit", getMit("bhLi", sbParts));
                    feature.put("bhLiMax", getMinMax("bhLi", sbParts, false));
                    feature.put("blLiMin", getMinMax("blLi", sbParts, true));
                    feature.put("blLiMit", getMit("blLi", sbParts));
                    feature.put("blLiMax", getMinMax("blLi", sbParts, false));
                    feature.put("flSohle", getSum("flSo", sbParts));
                    feature.put("flBoeRe", getSum("flBRe", sbParts));
                    feature.put("flBoeLi", getSum("flBLi", sbParts));
                    feature.put("flBoe", getSum("flB", sbParts));
                    feature.put("flGer", getSum("flGer", sbParts));
                    feature.put("brGewMin", getMinMax("brGew", sbParts, true));
                    feature.put("brGewMit", getMit("brGew", sbParts));
                    feature.put("brGewMax", getMinMax("brGew", sbParts, false));
                    feature.put("flGew", getSum("flGew", sbParts));
                    feature.put("blNassReMin", getMinMax("blNRe", sbParts, true));
                    feature.put("blNassReMit", getMit("blNRe", sbParts));
                    feature.put("blNassReMax", getMinMax("blNRe", sbParts, false));
                    feature.put("blTroReMin", getMinMax("blTRe", sbParts, true));
                    feature.put("blTroReMit", getMit("blTRe", sbParts));
                    feature.put("blTroReMax", getMinMax("blTRe", sbParts, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", sbParts, true));
                    feature.put("blNassLiMit", getMit("blNLi", sbParts));
                    feature.put("blNassLiMax", getMinMax("blNLi", sbParts, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", sbParts, true));
                    feature.put("blTroLiMit", getMit("blTLi", sbParts));
                    feature.put("blTroLiMax", getMinMax("blTLi", sbParts, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", sbParts));
                    feature.put("flBoeTroRe", getSum("flBtRe", sbParts));
                    feature.put("flBoeNassLi", getSum("flBnLi", sbParts));
                    feature.put("flBoeTroLi", getSum("flBtLi", sbParts));
                    feature.put("flBoeNass", getSum("flBn", sbParts));
                    feature.put("flBoeTro", getSum("flBt", sbParts));
                    feature.put("flNass", getSum("flN", sbParts));
                    feature.put("summe", false);

                    features.add(feature);
                    featuresWdm.add(feature);
                    featuresGu.add(feature);
                }
                features.add(createKumFeature(featuresWdm, true));
            }
            features.add(createKumFeature(featuresGu, false));
        }

        if (features.isEmpty()) {
            return null;
        } else {
            return new FeatureDataSource(features);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getSbPerSheet() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final String gu : helper.getGu()) {
            for (final Integer wdm : helper.getWidmung(gu)) {
                for (final Integer sb : helper.getSb(gu, wdm)) {
                    final List<Map<String, Object>> featuresSb = new ArrayList<Map<String, Object>>();
                    sheetNames.add(helper.getGuId(gu) + "-" + wdm + "-" + sb);

                    for (final Integer gew : helper.getGew(gu, wdm, sb)) {
                        final Map<String, Object> feature = new HashMap<String, Object>();
                        final Collection<SbObj> sbParts = helper.getSbPart(gu, wdm, sb, gew);
                        final double offenLength = helper.getLengthOffeneAbschn(gu, wdm, sb, gew);
                        final double prof = getLengthGew(sbParts);
                        final double mw = getLengthMw(sbParts);
                        final double gewAll = helper.getLengthGewAll(gu, wdm, sb, gew);

                        feature.put("group", gu + "-" + wdm + "-" + sb);
                        feature.put("gu", gu);
                        feature.put("wdm", wdm);
                        feature.put("sb", sb);
                        feature.put("sbName", helper.getSbName(sb));
                        feature.put("anzGew", helper.getGew(gu, wdm, sb).size());
                        feature.put("code", helper.getBaCd(gew));
                        feature.put("name", helper.getGewName(gew));
                        feature.put("von", convertStation(getMinFrom(sbParts)));
                        feature.put("bis", convertStation(getMaxTill(sbParts)));
                        feature.put("laenge", gewAll);
                        feature.put("offene_l", offenLength);
                        feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                        feature.put("prof_l", prof);
                        feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                        feature.put("mw_l", mw);
                        feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                        feature.put("profTrap_a", getCountProf("tr", sbParts));
                        feature.put("profTrap_l", getLengthProf("tr", sbParts));
                        feature.put("profRe_a", getCountProf("re", sbParts));
                        feature.put("profRe_l", getLengthProf("re", sbParts));
                        feature.put("brSohleMin", getMinMax("brSo", sbParts, true));
                        feature.put("brSohleMit", getMit("brSo", sbParts));
                        feature.put("brSohleMax", getMinMax("brSo", sbParts, false));
                        feature.put("bvReMin", getMinMax("bvRe", sbParts, true));
                        feature.put("bvReMit", getMit("bvRe", sbParts));
                        feature.put("bvReMax", getMinMax("bvRe", sbParts, false));
                        feature.put("bhReMin", getMinMax("bhRe", sbParts, true));
                        feature.put("bhReMit", getMit("bhRe", sbParts));
                        feature.put("bhReMax", getMinMax("bhRe", sbParts, false));
                        feature.put("blReMin", getMinMax("blRe", sbParts, true));
                        feature.put("blReMit", getMit("blRe", sbParts));
                        feature.put("blReMax", getMinMax("blRe", sbParts, false));
                        feature.put("bvLiMin", getMinMax("bvLi", sbParts, true));
                        feature.put("bvLiMit", getMit("bvLi", sbParts));
                        feature.put("bvLiMax", getMinMax("bvLi", sbParts, false));
                        feature.put("bhLiMin", getMinMax("bhLi", sbParts, true));
                        feature.put("bhLiMit", getMit("bhLi", sbParts));
                        feature.put("bhLiMax", getMinMax("bhLi", sbParts, false));
                        feature.put("blLiMin", getMinMax("blLi", sbParts, true));
                        feature.put("blLiMit", getMit("blLi", sbParts));
                        feature.put("blLiMax", getMinMax("blLi", sbParts, false));
                        feature.put("flQsGerMin", getMinMax("flQsGer", sbParts, true));
                        feature.put("flQsGerMit", getMit("flQsGer", sbParts));
                        feature.put("flQsGerMax", getMinMax("flQsGer", sbParts, false));
                        feature.put("flQsGewMin", getMinMax("flQsGew", sbParts, true));
                        feature.put("flQsGewMit", getMit("flQsGew", sbParts));
                        feature.put("flQsGewMax", getMinMax("flQsGew", sbParts, false));
                        feature.put("flSohle", getSum("flSo", sbParts));
                        feature.put("flBoeRe", getSum("flBRe", sbParts));
                        feature.put("flBoeLi", getSum("flBLi", sbParts));
                        feature.put("flBoe", getSum("flB", sbParts));
                        feature.put("flGer", getSum("flGer", sbParts));
                        feature.put("brGewMin", getMinMax("brGew", sbParts, true));
                        feature.put("brGewMit", getMit("brGew", sbParts));
                        feature.put("brGewMax", getMinMax("brGew", sbParts, false));
                        feature.put("flGew", getSum("flGew", sbParts));
                        feature.put("blNassReMin", getMinMax("blNRe", sbParts, true));
                        feature.put("blNassReMit", getMit("blNRe", sbParts));
                        feature.put("blNassReMax", getMinMax("blNRe", sbParts, false));
                        feature.put("blTroReMin", getMinMax("blTRe", sbParts, true));
                        feature.put("blTroReMit", getMit("blTRe", sbParts));
                        feature.put("blTroReMax", getMinMax("blTRe", sbParts, false));
                        feature.put("blNassLiMin", getMinMax("blNLi", sbParts, true));
                        feature.put("blNassLiMit", getMit("blNLi", sbParts));
                        feature.put("blNassLiMax", getMinMax("blNLi", sbParts, false));
                        feature.put("blTroLiMin", getMinMax("blTLi", sbParts, true));
                        feature.put("blTroLiMit", getMit("blTLi", sbParts));
                        feature.put("blTroLiMax", getMinMax("blTLi", sbParts, false));
                        feature.put("flBoeNassRe", getSum("flBnRe", sbParts));
                        feature.put("flBoeTroRe", getSum("flBtRe", sbParts));
                        feature.put("flBoeNassLi", getSum("flBnLi", sbParts));
                        feature.put("flBoeTroLi", getSum("flBtLi", sbParts));
                        feature.put("flBoeNass", getSum("flBn", sbParts));
                        feature.put("flBoeTro", getSum("flBt", sbParts));
                        feature.put("flNass", getSum("flN", sbParts));
                        feature.put("summe", false);

                        features.add(feature);
                        featuresSb.add(feature);
                    }
                    features.add(createKumFeature(featuresSb, true));
                }
            }
        }

        if (features.isEmpty()) {
            return null;
        } else {
            return new FeatureDataSource(features);
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
    private Map<String, Object> createKumFeature(final List<Map<String, Object>> featureListKum,
            final boolean subtotal) {
        return createKumFeature(featureListKum, subtotal, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureListKum  DOCUMENT ME!
     * @param   subtotal        DOCUMENT ME!
     * @param   stat            DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Map<String, Object> createKumFeature(final List<Map<String, Object>> featureListKum,
            final boolean subtotal,
            final int stat) {
        final Map<String, Object> kumFeature = new HashMap<String, Object>();

        if (!subtotal) {
            kumFeature.put("summe", Boolean.TRUE);
        }
        kumFeature.put("zwischenSumme", Boolean.valueOf(subtotal));

        if ((featureListKum == null) || featureListKum.isEmpty()) {
            return kumFeature;
        }

        final Map<String, Object> firstElement = featureListKum.get(0);

        for (final String key : new ArrayList<String>(firstElement.keySet())) {
            final Object value = firstElement.get(key);

            if (key.endsWith("offene_a") && (value instanceof Double)) {
                double lengthTotal = 0;
                double lengthSpecific = 0;

                for (final Map<String, Object> f : featureListKum) {
                    final double specificTmp = toDouble(f.get("offene_l"));
                    final double totalTmp = toDouble(f.get("gew_l"));

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            }
            if (key.endsWith("prof_a") && (value instanceof Double)) {
                double lengthTotal = 0;
                double lengthSpecific = 0;

                for (final Map<String, Object> f : featureListKum) {
                    final double specificTmp = toDouble(f.get("prof_l"));
                    final double totalTmp = toDouble(f.get("offene_l"));

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            }
            if (key.endsWith("mw_a") && (value instanceof Double)) {
                double lengthTotal = 0;
                double lengthSpecific = 0;

                for (final Map<String, Object> f : featureListKum) {
                    final double specificTmp = toDouble(f.get("mw_l"));
                    final double totalTmp = toDouble(f.get("prof_l"));

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            } else if (key.endsWith("Min") && (value instanceof Double)) {
                double min = ((value != null) ? (Double)value : Double.MAX_VALUE);

                for (final Map<String, Object> f : featureListKum) {
                    final double val = toDouble(f.get(key));

                    if (stat == PROFSTAT) {
                        f.put("key", null);
                    }

                    if (val < min) {
                        min = val;
                    }
                }

                if (min != Double.MAX_VALUE) {
                    kumFeature.put(key, min);
                }
            } else if (key.endsWith("Max") && (value instanceof Double)) {
                double max = toDouble(value);

                for (final Map<String, Object> f : featureListKum) {
                    final double val = toDouble(f.get(key));

                    if (stat == PROFSTAT) {
                        f.put("key", null);
                    }
                    if (val > max) {
                        max = val;
                    }
                }

                if (max != 0) {
                    kumFeature.put(key, max);
                }
            } else if (key.endsWith("Mit") && (value instanceof Double)) {
                double mit = 0.0;
                double length = 0.0;

                for (final Map<String, Object> f : featureListKum) {
                    final double val = toDouble(f.get(key));
                    final double len = toDouble(f.get("prof_l"));

                    mit += val * len;
                    length += len;
                }

                if (length != 0.0) {
                    kumFeature.put(key, mit / length);
                }
            } else if ((Arrays.binarySearch(exceptionalNumberFields, key) < 0) && (value instanceof Integer)) {
                int sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    sum += (Integer)f.get(key);
                }

                kumFeature.put(key, sum);
            } else if ((Arrays.binarySearch(exceptionalNumberFields, key) < 0) && (value instanceof Double)) {
                double sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    sum += toDouble(f.get(key));
                }

                kumFeature.put(key, sum);
            } else if ((!(value instanceof String) || key.equals("group")) && !(value instanceof Boolean)
                        && (Arrays.binarySearch(exceptionalNumberFields, key) < 0)) {
                kumFeature.put(key, value);
            }
        }

        return kumFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double toDouble(final Object o) {
        if (o == null) {
            return 0.0;
        } else {
            return (Double)o;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sbParts  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinFrom(final Collection<SbObj> sbParts) {
        double min = Double.MAX_VALUE;

        for (final SbObj tmp : sbParts) {
            if (tmp.getFrom() < min) {
                min = tmp.getFrom();
            }
        }

        return min;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sbParts  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMaxTill(final Collection<SbObj> sbParts) {
        double max = 0;

        for (final SbObj tmp : sbParts) {
            if (tmp.getTill() > max) {
                max = tmp.getTill();
            }
        }

        return max;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGewAll() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjOffen tmp : objList) {
            ts.add(tmp.getBaCd());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew() {
        return getLengthGew(-1);
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

        for (final GmdPartObjOffen tmp : objList) {
            if ((gewId == -1) || (tmp.getId() == gewId)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sbParts  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Collection<SbObj> sbParts) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if (tmp.getId() == sb.getId()) {
                    length += tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     * @param   from   gemNr DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gewId, final double from, final double till) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((gewId == -1) || (tmp.getId() == gewId)) {
                length += tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gewId) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getMw() != null) && (tmp.getMw() != 0)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sbParts  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Collection<SbObj> sbParts) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if ((tmp.getId() == sb.getId()) && (tmp.getMw() != null) && (tmp.getMw() != 0)) {
                    length += tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final String gu) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu)) && (tmp.getMw() != null)
                        && (tmp.getMw() != 0)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   gemNr DOCUMENT ME!
     * @param   wdm  gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final String gu, final Integer wdm) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))
                        && (tmp.getMw() != null)
                        && (tmp.getMw() != 0)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     * @param   from   gemNr DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gewId, final double from, final double till) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getMw() != null) && (tmp.getMw() != 0)) {
                length += tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gewId) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getProfil() != null)
                        && tmp.getProfil().equals(prof)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof     gemNr DOCUMENT ME!
     * @param   sbParts  gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final Collection<SbObj> sbParts) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if ((tmp.getId() == sb.getId()) && (tmp.getProfil() != null) && tmp.getProfil().equals(prof)) {
                    length += tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof  gemNr DOCUMENT ME!
     * @param   gu    gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final String gu) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu)) && (tmp.getProfil() != null)
                        && tmp.getProfil().equals(prof)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof  gemNr DOCUMENT ME!
     * @param   gu    gewId DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final String gu, final Integer wdm) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))
                        && (tmp.getProfil() != null) && tmp.getProfil().equals(prof)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gewId, final double from, final double till) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getProfil() != null)
                        && tmp.getProfil().equals(prof)) {
                length += tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field    gemNr DOCUMENT ME!
     * @param   sbParts  gewId DOCUMENT ME!
     * @param   min      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final Collection<SbObj> sbParts, final boolean min) {
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if (tmp.isInGewPart(sb.getId(), sb.getFrom(), sb.getTill())) {
                    final double value = tmp.get(field);

                    if (value == 0.0) {
                        continue;
                    }

                    if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                        currentVal = value;
                    }
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final String gu, final boolean min) {
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final String gu, final Integer wdm, final boolean min) {
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field,
            final int gewId,
            final double from,
            final double till,
            final boolean min) {
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gewId) {
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                currentVal += value * tmp.getLength();
                length += tmp.getLength();
            }
        }

        if (length == 0) {
            return 0;
        } else {
            return currentVal / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field    gemNr DOCUMENT ME!
     * @param   sbParts  gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final Collection<SbObj> sbParts) {
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if (tmp.isInGewPart(sb.getId(), sb.getFrom(), sb.getTill())) {
                    final double value = tmp.get(field);

                    if (value == 0.0) {
                        continue;
                    }

                    currentVal += value * tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                    length += tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                }
            }
        }

        if (length == 0) {
            return 0;
        } else {
            return currentVal / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final String gu) {
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                currentVal += value * tmp.getLength();
                length += tmp.getLength();
            }
        }

        if (length == 0) {
            return 0;
        } else {
            return currentVal / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final String gu, final Integer wdm) {
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                currentVal += value * tmp.getLength();
                length += tmp.getLength();
            }
        }

        if (length == 0) {
            return 0;
        } else {
            return currentVal / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gewId, final double from, final double till) {
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                currentVal += value * tmp.getLengthInGewPart(gewId, from, till);
                length += tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        if (length == 0) {
            return 0;
        } else {
            return currentVal / length;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gewId) {
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId))) {
                final double value = tmp.get(field);
                currentVal += value;
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field    gemNr DOCUMENT ME!
     * @param   sbParts  gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final Collection<SbObj> sbParts) {
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if (tmp.isInGewPart(sb.getId(), sb.getFrom(), sb.getTill())) {
                    // Die Laenge wird anteilsmaessig beruecksichtigt
                    final double value = tmp.get(field);
                    currentVal += (value / tmp.getLength())
                                * tmp.getLengthInGewPart(sb.getId(), sb.getFrom(), sb.getTill());
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gewId, final double from, final double till) {
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                final double value = tmp.get(field);
                currentVal += (value / tmp.getLength()) * tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final String gu) {
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getOwner() != null) && tmp.getOwner().equals(gu)) {
                final double value = tmp.get(field);
                currentVal += value;
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final String gu, final Integer wdm) {
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getOwner() != null) && tmp.getOwner().equals(gu) && (tmp.getWdm() != null)
                        && tmp.getWdm().equals(wdm)) {
                final double value = tmp.get(field);
                currentVal += value;
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getCountProf(final String prof, final int gewId) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getProfil() != null)
                        && tmp.getProfil().equals(prof)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof     gemNr DOCUMENT ME!
     * @param   sbParts  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final Collection<SbObj> sbParts) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            for (final SbObj sb : sbParts) {
                if ((tmp.isInGewPart(sb.getId(), sb.getFrom(), sb.getTill())) && (tmp.getProfil() != null)
                            && tmp.getProfil().equals(prof)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof  gemNr DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final String gu) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu)) && (tmp.getProfil() != null)
                        && tmp.getProfil().equals(prof)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof  gemNr DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final String gu, final Integer wdm) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))
                        && (tmp.getProfil() != null) && tmp.getProfil().equals(prof)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gewId, final double from, final double till) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if ((tmp.getId() == gewId) && (tmp.getProfil() != null) && tmp.getProfil().equals(prof)) {
                if (tmp.isInGewPart(gewId, from, till)) {
                    ++count;
                }
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
    private double getLengthGew(final String gu) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
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

        for (final GmdPartObjOffen tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWdm() == wdm)) {
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
    private int getCountGew(final String gu) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
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

        for (final GmdPartObjOffen tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWdm() == wdm)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final String gu, final int wdm) {
        double length = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWdm() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gu   DOCUMENT ME!
     * @param   wdm  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGew(final String gu, final int wdm) {
        int count = 0;

        for (final GmdPartObjOffen tmp : objList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWdm() == wdm)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   station  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String convertStation(final Double station) {
        final int km = (int)(station / 1000);
        final int m = (int)(station % 1000);
        String mString = String.valueOf(m);

        while (mString.length() < 3) {
            mString = "0" + mString;
        }

        return km + "+" + mString;
    }
}
