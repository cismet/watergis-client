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
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
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

import de.cismet.cids.custom.watergis.server.search.AllGewOffenByGem;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneOGemeindeReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GemeindenDataLightweight;
import de.cismet.watergis.reports.types.GmdPartObj;
import de.cismet.watergis.reports.types.GmdPartObjOffen;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOGemeindeReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOGemeindeReport.class);
    private static final String[] exceptionalNumberFields = {
            "gmdNummer",
            "group",
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

    private final Map<Integer, List<GmdPartObjOffen>> gemPartMap = new HashMap<Integer, List<GmdPartObjOffen>>();
    private final Map<Integer, GemeindenDataLightweight> gemDataMap = new HashMap<Integer, GemeindenDataLightweight>();
    private final List<String> sheetNames = new ArrayList<String>();
    private GerOffenGmdHelper helper;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gemId  baCd DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public File createGemeindeReport(final int[] gemId, final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("gemeinden", gemId.length);
        parameters.put("perGew", GerinneOGemeindeReportDialog.getInstance().isPerGew());
        parameters.put("perAbschn", GerinneOGemeindeReportDialog.getInstance().isPerPart());
        parameters.put("sumGu", GerinneOGemeindeReportDialog.getInstance().isSumGu());
        parameters.put("wdm", GerinneOGemeindeReportDialog.getInstance().isPerWdm());
        parameters.put("perAbschnProf", GerinneOGemeindeReportDialog.getInstance().isPerPartProf());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneOGemeindeReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/GerinneOffenGem.jasper"));

        init(gemId, gew);
        helper = new GerOffenGmdHelper(gemId, gew, getAllowedWdms());
        dataSources.put("gemeinden", getGemeindenAll());

        if (GerinneOGemeindeReportDialog.getInstance().isPerGew()
                    && !GerinneOGemeindeReportDialog.getInstance().isPerPart()
                    && !GerinneOGemeindeReportDialog.getInstance().isPerPartProf()) {
            dataSources.put("gewaesser", getGewaesser());
        } else if (GerinneOGemeindeReportDialog.getInstance().isPerPartProf()) {
            dataSources.put("gewaesserAbschnittProf", getGewaesserAbschnittProfil());
        } else if (GerinneOGemeindeReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesserAbschnitt", getGewaesserAbschnitt());
        }

        if (GerinneOGemeindeReportDialog.getInstance().isSumGu()) {
            if (GerinneOGemeindeReportDialog.getInstance().isPerWdm()) {
                dataSources.put("gewaesserGuAbschnitt", getGewaesserGuWidmung());
            } else {
                dataSources.put("gewaesserGu", getGewaesserGu());
            }
        }

        // create print from report and data
        final File file = new File(
                GerinneOGemeindeReportDialog.getInstance().getPath()
                        + "/Gerinne_offen_Gemeinden.xls");
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        final FileOutputStream fout = new FileOutputStream(file);
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

        return file;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final GerinneOGemeindeReport report = new GerinneOGemeindeReport();
        try {
            report.createGemeindeReport(new int[] { 2 }, new int[] { 2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr     DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final int[] gemNr, final int[] routeIds) throws Exception {
        for (final int gem : gemNr) {
            gemPartMap.put(gem, getAllRoutes(gem, routeIds));

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
     * @param   gemId     DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<GmdPartObjOffen> getAllRoutes(final int gemId, final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewOffenByGem(gemId, routeIds, getAllowedWdms());
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
                        (Double)f.get(19),
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

        if (GerinneOGemeindeReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneOGemeindeReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneOGemeindeReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneOGemeindeReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneOGemeindeReportDialog.getInstance().is1505()) {
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
    private FeatureDataSource getGemeindenAll() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();
        sheetNames.add("Gemeinden");

        for (final Integer gem : gemDataMap.keySet()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            final double offenLength = helper.getLengthOffeneAbschn(gem);
            final double prof = getLengthGew(gem);
            final double mw = getLengthMw(gem);
            final double gewAll = helper.getLengthGewAll(gem);

            feature.put("name", gemDataMap.get(gem).getGmdName());
            feature.put("nummer", gem);
            feature.put("gew_a", helper.getCountGewAll(gem));
            feature.put("gew_l", helper.getLengthGewAll(gem));
            feature.put("offene_l", offenLength);
            feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
            feature.put("prof_l", prof);
            feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
            feature.put("mw_l", mw);
            feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
            feature.put("profTrap_a", getCountProf("tr", gem));
            feature.put("profTrap_l", getLengthProf("tr", gem));
            feature.put("profRe_a", getCountProf("re", gem));
            feature.put("profRe_l", getLengthProf("re", gem));
            feature.put("brSohleMin", getMinMax("brSo", gem, true));
            feature.put("brSohleMit", getMit("brSo", gem));
            feature.put("brSohleMax", getMinMax("brSo", gem, false));
            feature.put("bvReMin", getMinMax("bvRe", gem, true));
            feature.put("bvReMit", getMit("bvRe", gem));
            feature.put("bvReMax", getMinMax("bvRe", gem, false));
            feature.put("bhReMin", getMinMax("bhRe", gem, true));
            feature.put("bhReMit", getMit("bhRe", gem));
            feature.put("bhReMax", getMinMax("bhRe", gem, false));
            feature.put("blReMin", getMinMax("blRe", gem, true));
            feature.put("blReMit", getMit("blRe", gem));
            feature.put("blReMax", getMinMax("blRe", gem, false));
            feature.put("bvLiMin", getMinMax("bvLi", gem, true));
            feature.put("bvLiMit", getMit("bvLi", gem));
            feature.put("bvLiMax", getMinMax("bvLi", gem, false));
            feature.put("bhLiMin", getMinMax("bhLi", gem, true));
            feature.put("bhLiMit", getMit("bhLi", gem));
            feature.put("bhLiMax", getMinMax("bhLi", gem, false));
            feature.put("blLiMin", getMinMax("blLi", gem, true));
            feature.put("blLiMit", getMit("blLi", gem));
            feature.put("blLiMax", getMinMax("blLi", gem, false));
            feature.put("flSohle", getSum("flSo", gem));
            feature.put("flBoeRe", getSum("flBRe", gem));
            feature.put("flBoeLi", getSum("flBLi", gem));
            feature.put("flBoe", getSum("flB", gem));
            feature.put("flGer", getSum("flGer", gem));
            feature.put("brGewMin", getMinMax("brGew", gem, true));
            feature.put("brGewMit", getMit("brGew", gem));
            feature.put("brGewMax", getMinMax("brGew", gem, false));
            feature.put("flGew", getSum("flGew", gem));
            feature.put("blNassReMin", getMinMax("blNRe", gem, true));
            feature.put("blNassReMit", getMit("blNRe", gem));
            feature.put("blNassReMax", getMinMax("blNRe", gem, false));
            feature.put("blTroReMin", getMinMax("blTRe", gem, true));
            feature.put("blTroReMit", getMit("blTRe", gem));
            feature.put("blTroReMax", getMinMax("blTRe", gem, false));
            feature.put("blNassLiMin", getMinMax("blNLi", gem, true));
            feature.put("blNassLiMit", getMit("blNLi", gem));
            feature.put("blNassLiMax", getMinMax("blNLi", gem, false));
            feature.put("blTroLiMin", getMinMax("blTLi", gem, true));
            feature.put("blTroLiMit", getMit("blTLi", gem));
            feature.put("blTroLiMax", getMinMax("blTLi", gem, false));
            feature.put("flBoeNassRe", getSum("flBnRe", gem));
            feature.put("flBoeTroRe", getSum("flBtRe", gem));
            feature.put("flBoeNassLi", getSum("flBnLi", gem));
            feature.put("flBoeTroLi", getSum("flBtLi", gem));
            feature.put("flBoeNass", getSum("flBn", gem));
            feature.put("flBoeTro", getSum("flBt", gem));
            feature.put("flNass", getSum("flN", gem));
            feature.put("summe", false);

            features.add(feature);
        }
        features.add(createKumFeature(features, false));

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
    private FeatureDataSource getGewaesser() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            if ((getGew(gem) != null) && !getGew(gem).isEmpty()) {
                sheetNames.add(gemDataMap.get(gem).getGmdName());
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
                for (final int gew : getGew(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final double offenLength = helper.getLengthOffeneAbschn(gem, gew);
                    final double prof = getLengthGew(gem, gew);
                    final double mw = getLengthMw(gem, gew);
                    final double gewAll = helper.getLengthGewAll(gem, gew);

                    feature.put("name", getGewName(gem, gew));
                    feature.put("code", getBaCd(gem, gew));
                    // feature.put("gew_a", helper.getCountGewAll(gem, gew));
                    feature.put("gew_l", helper.getLengthGewAll(gem, gew));
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", gem, gew));
                    feature.put("profTrap_l", getLengthProf("tr", gem, gew));
                    feature.put("profRe_a", getCountProf("re", gem, gew));
                    feature.put("profRe_l", getLengthProf("re", gem, gew));
                    feature.put("brSohleMin", getMinMax("brSo", gem, gew, true));
                    feature.put("brSohleMit", getMit("brSo", gem, gew));
                    feature.put("brSohleMax", getMinMax("brSo", gem, gew, false));
                    feature.put("bvReMin", getMinMax("bvRe", gem, gew, true));
                    feature.put("bvReMit", getMit("bvRe", gem, gew));
                    feature.put("bvReMax", getMinMax("bvRe", gem, gew, false));
                    feature.put("bhReMin", getMinMax("bhRe", gem, gew, true));
                    feature.put("bhReMit", getMit("bhRe", gem, gew));
                    feature.put("bhReMax", getMinMax("bhRe", gem, gew, false));
                    feature.put("blReMin", getMinMax("blRe", gem, gew, true));
                    feature.put("blReMit", getMit("blRe", gem, gew));
                    feature.put("blReMax", getMinMax("blRe", gem, gew, false));
                    feature.put("bvLiMin", getMinMax("bvLi", gem, gew, true));
                    feature.put("bvLiMit", getMit("bvLi", gem, gew));
                    feature.put("bvLiMax", getMinMax("bvLi", gem, gew, false));
                    feature.put("bhLiMin", getMinMax("bhLi", gem, gew, true));
                    feature.put("bhLiMit", getMit("bhLi", gem, gew));
                    feature.put("bhLiMax", getMinMax("bhLi", gem, gew, false));
                    feature.put("blLiMin", getMinMax("blLi", gem, gew, true));
                    feature.put("blLiMit", getMit("blLi", gem, gew));
                    feature.put("blLiMax", getMinMax("blLi", gem, gew, false));
                    feature.put("flSohle", getSum("flSo", gem, gew));
                    feature.put("flBoeRe", getSum("flBRe", gem, gew));
                    feature.put("flBoeLi", getSum("flBLi", gem, gew));
                    feature.put("flBoe", getSum("flB", gem, gew));
                    feature.put("flGer", getSum("flGer", gem, gew));
                    feature.put("brGewMin", getMinMax("brGew", gem, gew, true));
                    feature.put("brGewMit", getMit("brGew", gem, gew));
                    feature.put("brGewMax", getMinMax("brGew", gem, gew, false));
                    feature.put("flGew", getSum("flGew", gem, gew));
                    feature.put("blNassReMin", getMinMax("blNRe", gem, gew, true));
                    feature.put("blNassReMit", getMit("blNRe", gem, gew));
                    feature.put("blNassReMax", getMinMax("blNRe", gem, gew, false));
                    feature.put("blTroReMin", getMinMax("blTRe", gem, gew, true));
                    feature.put("blTroReMit", getMit("blTRe", gem, gew));
                    feature.put("blTroReMax", getMinMax("blTRe", gem, gew, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", gem, gew, true));
                    feature.put("blNassLiMit", getMit("blNLi", gem, gew));
                    feature.put("blNassLiMax", getMinMax("blNLi", gem, gew, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", gem, gew, true));
                    feature.put("blTroLiMit", getMit("blTLi", gem, gew));
                    feature.put("blTroLiMax", getMinMax("blTLi", gem, gew, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", gem, gew, true));
                    feature.put("flQsGerMit", getMit("flQsGer", gem, gew));
                    feature.put("flQsGerMax", getMinMax("flQsGer", gem, gew, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", gem, gew, true));
                    feature.put("flQsGewMit", getMit("flQsGew", gem, gew));
                    feature.put("flQsGewMax", getMinMax("flQsGew", gem, gew, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", gem, gew));
                    feature.put("flBoeTroRe", getSum("flBtRe", gem, gew));
                    feature.put("flBoeNassLi", getSum("flBnLi", gem, gew));
                    feature.put("flBoeTroLi", getSum("flBtLi", gem, gew));
                    feature.put("flBoeNass", getSum("flBn", gem, gew));
                    feature.put("flBoeTro", getSum("flBt", gem, gew));
                    feature.put("flNass", getSum("flN", gem, gew));
                    feature.put("summe", false);
                    features.add(feature);
                    featureListKum.add(feature);
                }
                features.add(createKumFeature(featureListKum, false));
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
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesserAbschnitt() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            if ((helper.getGemPartMap().get(gem) != null) && !helper.getGemPartMap().get(gem).isEmpty()) {
                sheetNames.add(gemDataMap.get(gem).getGmdName());
                for (final GmdPartObj gew : helper.getGemPartMap().get(gem)) { // use the complete river part
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final int id = gew.getId();
                    final double from = gew.getFrom();
                    final double till = gew.getTill();
                    final double offenLength = helper.getLengthOffeneAbschn(gem, id, from, till);
                    final double prof = getLengthGew(gem, id, from, till);
                    final double mw = getLengthMw(gem, id, from, till);
                    final double gewAll = helper.getLengthGewAll(gem, id);

                    feature.put("name", getGewName(gem, id));
                    feature.put("code", getBaCd(gem, id));
                    // feature.put("gew_a", helper.getCountGewAll(gem, id, from, till));
                    feature.put("gew_l", helper.getLengthGewAll(gem, id));
                    feature.put("von", convertStation(from));
                    feature.put("bis", convertStation(till));
                    feature.put("laenge", gew.getLength());
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", gem, id, from, till));
                    feature.put("profTrap_l", getLengthProf("tr", gem, id, from, till));
                    feature.put("profRe_a", getCountProf("re", gem, id, from, till));
                    feature.put("profRe_l", getLengthProf("re", gem, id, from, till));
                    feature.put("brSohleMin", getMinMax("brSo", gem, id, from, till, true));
                    feature.put("brSohleMit", getMit("brSo", gem, id, from, till));
                    feature.put("brSohleMax", getMinMax("brSo", gem, id, from, till, false));
                    feature.put("bvReMin", getMinMax("bvRe", gem, id, from, till, true));
                    feature.put("bvReMit", getMit("bvRe", gem, id, from, till));
                    feature.put("bvReMax", getMinMax("bvRe", gem, id, from, till, false));
                    feature.put("bhReMin", getMinMax("bhRe", gem, id, from, till, true));
                    feature.put("bhReMit", getMit("bhRe", gem, id, from, till));
                    feature.put("bhReMax", getMinMax("bhRe", gem, id, from, till, false));
                    feature.put("blReMin", getMinMax("blRe", gem, id, from, till, true));
                    feature.put("blReMit", getMit("blRe", gem, id, from, till));
                    feature.put("blReMax", getMinMax("blRe", gem, id, from, till, false));
                    feature.put("bvLiMin", getMinMax("bvLi", gem, id, from, till, true));
                    feature.put("bvLiMit", getMit("bvLi", gem, id, from, till));
                    feature.put("bvLiMax", getMinMax("bvLi", gem, id, from, till, false));
                    feature.put("bhLiMin", getMinMax("bhLi", gem, id, from, till, true));
                    feature.put("bhLiMit", getMit("bhLi", gem, id, from, till));
                    feature.put("bhLiMax", getMinMax("bhLi", gem, id, from, till, false));
                    feature.put("blLiMin", getMinMax("blLi", gem, id, from, till, true));
                    feature.put("blLiMit", getMit("blLi", gem, id, from, till));
                    feature.put("blLiMax", getMinMax("blLi", gem, id, from, till, false));
                    feature.put("flSohle", getSum("flSo", gem, id, from, till));
                    feature.put("flBoeRe", getSum("flBRe", gem, id, from, till));
                    feature.put("flBoeLi", getSum("flBLi", gem, id, from, till));
                    feature.put("flBoe", getSum("flB", gem, id, from, till));
                    feature.put("flGer", getSum("flGer", gem, id, from, till));
                    feature.put("brGewMin", getMinMax("brGew", gem, id, from, till, true));
                    feature.put("brGewMit", getMit("brGew", gem, id, from, till));
                    feature.put("brGewMax", getMinMax("brGew", gem, id, from, till, false));
                    feature.put("flGew", getSum("flGew", gem, id, from, till));
                    feature.put("blNassReMin", getMinMax("blNRe", gem, id, from, till, true));
                    feature.put("blNassReMit", getMit("blNRe", gem, id, from, till));
                    feature.put("blNassReMax", getMinMax("blNRe", gem, id, from, till, false));
                    feature.put("blTroReMin", getMinMax("blTRe", gem, id, from, till, true));
                    feature.put("blTroReMit", getMit("blTRe", gem, id, from, till));
                    feature.put("blTroReMax", getMinMax("blTRe", gem, id, from, till, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", gem, id, from, till, true));
                    feature.put("blNassLiMit", getMit("blNLi", gem, id, from, till));
                    feature.put("blNassLiMax", getMinMax("blNLi", gem, id, from, till, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", gem, id, from, till, true));
                    feature.put("blTroLiMit", getMit("blTLi", gem, id, from, till));
                    feature.put("blTroLiMax", getMinMax("blTLi", gem, id, from, till, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", gem, id, from, till, true));
                    feature.put("flQsGerMit", getMit("flQsGer", gem, id, from, till));
                    feature.put("flQsGerMax", getMinMax("flQsGer", gem, id, from, till, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", gem, id, from, till, true));
                    feature.put("flQsGewMit", getMit("flQsGew", gem, id, from, till));
                    feature.put("flQsGewMax", getMinMax("flQsGew", gem, id, from, till, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", gem, id, from, till));
                    feature.put("flBoeTroRe", getSum("flBtRe", gem, id, from, till));
                    feature.put("flBoeNassLi", getSum("flBnLi", gem, id, from, till));
                    feature.put("flBoeTroLi", getSum("flBtLi", gem, id, from, till));
                    feature.put("flBoeNass", getSum("flBn", gem, id, from, till));
                    feature.put("flBoeTro", getSum("flBt", gem, id, from, till));
                    feature.put("flNass", getSum("flN", gem, id, from, till));
                    feature.put("summe", false);

                    final String newCode = getBaCd(gem, gew.getId());

                    if ((code != null) && !code.equals(newCode)) {
                        features.add(createKumFeature(featureListGewKum, true));
                        featureListGewKum.clear();
                    }

                    code = newCode;
                    features.add(feature);
                    featureListKum.add(feature);
                    featureListGewKum.add(feature);
                }
                features.add(createKumFeature(featureListKum, false));
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
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesserAbschnittProfil() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            if ((gemPartMap.get(gem) != null) && !gemPartMap.get(gem).isEmpty()) {
                sheetNames.add(gemDataMap.get(gem).getGmdName());
                for (final GmdPartObjOffen gew : gemPartMap.get(gem)) { // use the complete river part
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final int id = gew.getId();
                    final double from = gew.getBaStVon();
                    final double till = gew.getBaStBis();
                    final double offenLength = helper.getLengthOffeneAbschn(gem, id, from, till);
                    final double prof = getLengthGew(gem, id, from, till);
                    final double mw = getLengthMw(gem, id, from, till);
                    final double gewAll = helper.getLengthGewAll(gem, id);

                    feature.put("group", gemDataMap.get(gem).getGmdName());
                    feature.put("name", getGewName(gem, id));
                    feature.put("code", getBaCd(gem, id));
                    // feature.put("gew_a", helper.getCountGewAll(gem, id, from, till));
                    feature.put("gew_l", gewAll);
                    feature.put("von", convertStation(from));
                    feature.put("bis", convertStation(till));
                    feature.put("laenge", gew.getLength());
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", gem, id, from, till));
                    feature.put("profTrap_l", getLengthProf("tr", gem, id, from, till));
                    feature.put("profRe_a", getCountProf("re", gem, id, from, till));
                    feature.put("profRe_l", getLengthProf("re", gem, id, from, till));
                    feature.put("brSohleMin", getMinMax("brSo", gem, id, from, till, true));
                    feature.put("brSohleMit", getMit("brSo", gem, id, from, till));
                    feature.put("brSohleMax", getMinMax("brSo", gem, id, from, till, false));
                    feature.put("bvReMin", getMinMax("bvRe", gem, id, from, till, true));
                    feature.put("bvReMit", getMit("bvRe", gem, id, from, till));
                    feature.put("bvReMax", getMinMax("bvRe", gem, id, from, till, false));
                    feature.put("bhReMin", getMinMax("bhRe", gem, id, from, till, true));
                    feature.put("bhReMit", getMit("bhRe", gem, id, from, till));
                    feature.put("bhReMax", getMinMax("bhRe", gem, id, from, till, false));
                    feature.put("blReMin", getMinMax("blRe", gem, id, from, till, true));
                    feature.put("blReMit", getMit("blRe", gem, id, from, till));
                    feature.put("blReMax", getMinMax("blRe", gem, id, from, till, false));
                    feature.put("bvLiMin", getMinMax("bvLi", gem, id, from, till, true));
                    feature.put("bvLiMit", getMit("bvLi", gem, id, from, till));
                    feature.put("bvLiMax", getMinMax("bvLi", gem, id, from, till, false));
                    feature.put("bhLiMin", getMinMax("bhLi", gem, id, from, till, true));
                    feature.put("bhLiMit", getMit("bhLi", gem, id, from, till));
                    feature.put("bhLiMax", getMinMax("bhLi", gem, id, from, till, false));
                    feature.put("blLiMin", getMinMax("blLi", gem, id, from, till, true));
                    feature.put("blLiMit", getMit("blLi", gem, id, from, till));
                    feature.put("blLiMax", getMinMax("blLi", gem, id, from, till, false));
                    feature.put("flSohle", getSum("flSo", gem, id, from, till));
                    feature.put("flBoeRe", getSum("flBRe", gem, id, from, till));
                    feature.put("flBoeLi", getSum("flBLi", gem, id, from, till));
                    feature.put("flBoe", getSum("flB", gem, id, from, till));
                    feature.put("flGer", getSum("flGer", gem, id, from, till));
                    feature.put("brGewMin", getMinMax("brGew", gem, id, from, till, true));
                    feature.put("brGewMit", getMit("brGew", gem, id, from, till));
                    feature.put("brGewMax", getMinMax("brGew", gem, id, from, till, false));
                    feature.put("flGew", getSum("flGew", gem, id, from, till));
                    feature.put("blNassReMin", getMinMax("blNRe", gem, id, from, till, true));
                    feature.put("blNassReMit", getMit("blNRe", gem, id, from, till));
                    feature.put("blNassReMax", getMinMax("blNRe", gem, id, from, till, false));
                    feature.put("blTroReMin", getMinMax("blTRe", gem, id, from, till, true));
                    feature.put("blTroReMit", getMit("blTRe", gem, id, from, till));
                    feature.put("blTroReMax", getMinMax("blTRe", gem, id, from, till, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", gem, id, from, till, true));
                    feature.put("blNassLiMit", getMit("blNLi", gem, id, from, till));
                    feature.put("blNassLiMax", getMinMax("blNLi", gem, id, from, till, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", gem, id, from, till, true));
                    feature.put("blTroLiMit", getMit("blTLi", gem, id, from, till));
                    feature.put("blTroLiMax", getMinMax("blTLi", gem, id, from, till, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", gem, id, from, till, true));
                    feature.put("flQsGerMit", getMit("flQsGer", gem, id, from, till));
                    feature.put("flQsGerMax", getMinMax("flQsGer", gem, id, from, till, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", gem, id, from, till, true));
                    feature.put("flQsGewMit", getMit("flQsGew", gem, id, from, till));
                    feature.put("flQsGewMax", getMinMax("flQsGew", gem, id, from, till, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", gem, id, from, till));
                    feature.put("flBoeTroRe", getSum("flBtRe", gem, id, from, till));
                    feature.put("flBoeNassLi", getSum("flBnLi", gem, id, from, till));
                    feature.put("flBoeTroLi", getSum("flBtLi", gem, id, from, till));
                    feature.put("flBoeNass", getSum("flBn", gem, id, from, till));
                    feature.put("flBoeTro", getSum("flBt", gem, id, from, till));
                    feature.put("flNass", getSum("flN", gem, id, from, till));
                    feature.put("summe", false);

                    final String newCode = getBaCd(gem, gew.getId());

                    if ((code != null) && !code.equals(newCode)) {
                        features.add(createKumFeature(featureListGewKum, true));
                        featureListGewKum.clear();
                    }

                    code = newCode;
                    features.add(feature);
                    featureListKum.add(feature);
                    featureListGewKum.add(feature);
                }
                features.add(createKumFeature(featureListKum, false, PROFSTAT));
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
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesserGu() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            if ((getGu(gem) != null) && !getGu(gem).isEmpty()) {
                sheetNames.add("GU " + gemDataMap.get(gem).getGmdName());
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
                for (final String guName : getGu(gem)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final double offenLength = helper.getLengthOffeneAbschn(gem, guName);
                    final double prof = getLengthGew(gem, guName);
                    final double mw = getLengthMw(gem, guName);
                    final double gewAll = helper.getLengthGewAll(gem, guName);

                    feature.put("group", String.valueOf(gem));
                    feature.put("name", guName);
                    feature.put("gu", getGuId(gem, guName));
                    feature.put("gew_a", helper.getCountGewAll(gem, guName));
                    feature.put("gew_l", helper.getLengthGewAll(gem, guName));
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", gem, guName));
                    feature.put("profTrap_l", getLengthProf("tr", gem, guName));
                    feature.put("profRe_a", getCountProf("re", gem, guName));
                    feature.put("profRe_l", getLengthProf("re", gem, guName));
                    feature.put("brSohleMin", getMinMax("brSo", gem, guName, true));
                    feature.put("brSohleMit", getMit("brSo", gem, guName));
                    feature.put("brSohleMax", getMinMax("brSo", gem, guName, false));
                    feature.put("bvReMin", getMinMax("bvRe", gem, guName, true));
                    feature.put("bvReMit", getMit("bvRe", gem, guName));
                    feature.put("bvReMax", getMinMax("bvRe", gem, guName, false));
                    feature.put("bhReMin", getMinMax("bhRe", gem, guName, true));
                    feature.put("bhReMit", getMit("bhRe", gem, guName));
                    feature.put("bhReMax", getMinMax("bhRe", gem, guName, false));
                    feature.put("blReMin", getMinMax("blRe", gem, guName, true));
                    feature.put("blReMit", getMit("blRe", gem, guName));
                    feature.put("blReMax", getMinMax("blRe", gem, guName, false));
                    feature.put("bvLiMin", getMinMax("bvLi", gem, guName, true));
                    feature.put("bvLiMit", getMit("bvLi", gem, guName));
                    feature.put("bvLiMax", getMinMax("bvLi", gem, guName, false));
                    feature.put("bhLiMin", getMinMax("bhLi", gem, guName, true));
                    feature.put("bhLiMit", getMit("bhLi", gem, guName));
                    feature.put("bhLiMax", getMinMax("bhLi", gem, guName, false));
                    feature.put("blLiMin", getMinMax("blLi", gem, guName, true));
                    feature.put("blLiMit", getMit("blLi", gem, guName));
                    feature.put("blLiMax", getMinMax("blLi", gem, guName, false));
                    feature.put("flSohle", getSum("flSo", gem, guName));
                    feature.put("flBoeRe", getSum("flBRe", gem, guName));
                    feature.put("flBoeLi", getSum("flBLi", gem, guName));
                    feature.put("flBoe", getSum("flB", gem, guName));
                    feature.put("flGer", getSum("flGer", gem, guName));
                    feature.put("brGewMin", getMinMax("brGew", gem, guName, true));
                    feature.put("brGewMit", getMit("brGew", gem, guName));
                    feature.put("brGewMax", getMinMax("brGew", gem, guName, false));
                    feature.put("flGew", getSum("flGew", gem, guName));
                    feature.put("blNassReMin", getMinMax("blNRe", gem, guName, true));
                    feature.put("blNassReMit", getMit("blNRe", gem, guName));
                    feature.put("blNassReMax", getMinMax("blNRe", gem, guName, false));
                    feature.put("blTroReMin", getMinMax("blTRe", gem, guName, true));
                    feature.put("blTroReMit", getMit("blTRe", gem, guName));
                    feature.put("blTroReMax", getMinMax("blTRe", gem, guName, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", gem, guName, true));
                    feature.put("blNassLiMit", getMit("blNLi", gem, guName));
                    feature.put("blNassLiMax", getMinMax("blNLi", gem, guName, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", gem, guName, true));
                    feature.put("blTroLiMit", getMit("blTLi", gem, guName));
                    feature.put("blTroLiMax", getMinMax("blTLi", gem, guName, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", gem, guName, true));
                    feature.put("flQsGerMit", getMit("flQsGer", gem, guName));
                    feature.put("flQsGerMax", getMinMax("flQsGer", gem, guName, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", gem, guName, true));
                    feature.put("flQsGewMit", getMit("flQsGew", gem, guName));
                    feature.put("flQsGewMax", getMinMax("flQsGew", gem, guName, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", gem, guName));
                    feature.put("flBoeTroRe", getSum("flBtRe", gem, guName));
                    feature.put("flBoeNassLi", getSum("flBnLi", gem, guName));
                    feature.put("flBoeTroLi", getSum("flBtLi", gem, guName));
                    feature.put("flBoeNass", getSum("flBn", gem, guName));
                    feature.put("flBoeTro", getSum("flBt", gem, guName));
                    feature.put("flNass", getSum("flN", gem, guName));
                    feature.put("summe", false);

                    features.add(feature);
                    featureListKum.add(feature);
                }
                features.add(createKumFeature(featureListKum, false));
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
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGewaesserGuWidmung() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            if ((getGu(gem) != null) && !getGu(gem).isEmpty()) {
                sheetNames.add("GU " + gemDataMap.get(gem).getGmdName());
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

                for (final String guName : getGu(gem)) {
                    final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

                    for (final Integer wdm : getWidmung(gem, guName)) {
                        final Map<String, Object> feature = new HashMap<String, Object>();
                        final double offenLength = helper.getLengthOffeneAbschn(gem, guName, wdm);
                        final double prof = getLengthGew(gem, guName, wdm);
                        final double mw = getLengthMw(gem, guName, wdm);
                        final double gewAll = helper.getLengthGewAll(gem, guName, wdm);

                        feature.put("group", String.valueOf(gem));
                        feature.put("name", getGuId(gem, guName));
                        feature.put("gu", guName);
                        feature.put("wdm", wdm);
                        feature.put("gew_a", helper.getCountGewAll(gem, guName, wdm));
                        feature.put("gew_l", helper.getLengthGewAll(gem, guName, wdm));
                        feature.put("offene_l", offenLength);
                        feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                        feature.put("prof_l", prof);
                        feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : offenLength));
                        feature.put("mw_l", mw);
                        feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                        feature.put("profTrap_a", getCountProf("tr", gem, guName, wdm));
                        feature.put("profTrap_l", getLengthProf("tr", gem, guName, wdm));
                        feature.put("profRe_a", getCountProf("re", gem, guName, wdm));
                        feature.put("profRe_l", getLengthProf("re", gem, guName, wdm));
                        feature.put("brSohleMin", getMinMax("brSo", gem, guName, wdm, true));
                        feature.put("brSohleMit", getMit("brSo", gem, guName, wdm));
                        feature.put("brSohleMax", getMinMax("brSo", gem, guName, wdm, false));
                        feature.put("bvReMin", getMinMax("bvRe", gem, guName, wdm, true));
                        feature.put("bvReMit", getMit("bvRe", gem, guName, wdm));
                        feature.put("bvReMax", getMinMax("bvRe", gem, guName, wdm, false));
                        feature.put("bhReMin", getMinMax("bhRe", gem, guName, wdm, true));
                        feature.put("bhReMit", getMit("bhRe", gem, guName, wdm));
                        feature.put("bhReMax", getMinMax("bhRe", gem, guName, wdm, false));
                        feature.put("blReMin", getMinMax("blRe", gem, guName, wdm, true));
                        feature.put("blReMit", getMit("blRe", gem, guName, wdm));
                        feature.put("blReMax", getMinMax("blRe", gem, guName, wdm, false));
                        feature.put("bvLiMin", getMinMax("bvLi", gem, guName, wdm, true));
                        feature.put("bvLiMit", getMit("bvLi", gem, guName, wdm));
                        feature.put("bvLiMax", getMinMax("bvLi", gem, guName, wdm, false));
                        feature.put("bhLiMin", getMinMax("bhLi", gem, guName, wdm, true));
                        feature.put("bhLiMit", getMit("bhLi", gem, guName, wdm));
                        feature.put("bhLiMax", getMinMax("bhLi", gem, guName, wdm, false));
                        feature.put("blLiMin", getMinMax("blLi", gem, guName, wdm, true));
                        feature.put("blLiMit", getMit("blLi", gem, guName, wdm));
                        feature.put("blLiMax", getMinMax("blLi", gem, guName, wdm, false));
                        feature.put("flSohle", getSum("flSo", gem, guName, wdm));
                        feature.put("flBoeRe", getSum("flBRe", gem, guName, wdm));
                        feature.put("flBoeLi", getSum("flBLi", gem, guName, wdm));
                        feature.put("flBoe", getSum("flB", gem, guName, wdm));
                        feature.put("flGer", getSum("flGer", gem, guName, wdm));
                        feature.put("brGewMin", getMinMax("brGew", gem, guName, wdm, true));
                        feature.put("brGewMit", getMit("brGew", gem, guName, wdm));
                        feature.put("brGewMax", getMinMax("brGew", gem, guName, wdm, false));
                        feature.put("flGew", getSum("flGew", gem, guName, wdm));
                        feature.put("blNassReMin", getMinMax("blNRe", gem, guName, wdm, true));
                        feature.put("blNassReMit", getMit("blNRe", gem, guName, wdm));
                        feature.put("blNassReMax", getMinMax("blNRe", gem, guName, wdm, false));
                        feature.put("blTroReMin", getMinMax("blTRe", gem, guName, wdm, true));
                        feature.put("blTroReMit", getMit("blTRe", gem, guName, wdm));
                        feature.put("blTroReMax", getMinMax("blTRe", gem, guName, wdm, false));
                        feature.put("blNassLiMin", getMinMax("blNLi", gem, guName, wdm, true));
                        feature.put("blNassLiMit", getMit("blNLi", gem, guName, wdm));
                        feature.put("blNassLiMax", getMinMax("blNLi", gem, guName, wdm, false));
                        feature.put("blTroLiMin", getMinMax("blTLi", gem, guName, wdm, true));
                        feature.put("blTroLiMit", getMit("blTLi", gem, guName, wdm));
                        feature.put("blTroLiMax", getMinMax("blTLi", gem, guName, wdm, false));
                        feature.put("flQsGerMin", getMinMax("flQsGer", gem, guName, wdm, true));
                        feature.put("flQsGerMit", getMit("flQsGer", gem, guName, wdm));
                        feature.put("flQsGerMax", getMinMax("flQsGer", gem, guName, wdm, false));
                        feature.put("flQsGewMin", getMinMax("flQsGew", gem, guName, wdm, true));
                        feature.put("flQsGewMit", getMit("flQsGew", gem, guName, wdm));
                        feature.put("flQsGewMax", getMinMax("flQsGew", gem, guName, wdm, false));
                        feature.put("flBoeNassRe", getSum("flBnRe", gem, guName, wdm));
                        feature.put("flBoeTroRe", getSum("flBtRe", gem, guName, wdm));
                        feature.put("flBoeNassLi", getSum("flBnLi", gem, guName, wdm));
                        feature.put("flBoeTroLi", getSum("flBtLi", gem, guName, wdm));
                        feature.put("flBoeNass", getSum("flBn", gem, guName, wdm));
                        feature.put("flBoeTro", getSum("flBt", gem, guName, wdm));
                        feature.put("flNass", getSum("flN", gem, guName, wdm));
                        feature.put("summe", false);

                        features.add(feature);
                        featureListKum.add(feature);
                        featureListGuKum.add(feature);
                    }
                    features.add(createKumFeature(featureListGuKum, true));
                }
                features.add(createKumFeature(featureListKum, false));
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

        kumFeature.put("summe", Boolean.TRUE);
        kumFeature.put("zwischenSumme", subtotal);

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
                    final double specificTmp = (Double)f.get("offene_l");
                    final double totalTmp = (Double)f.get("gew_l");

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            } else if (key.endsWith("prof_a") && (value instanceof Double)) {
                double lengthTotal = 0;
                double lengthSpecific = 0;

                for (final Map<String, Object> f : featureListKum) {
                    final double specificTmp = (Double)f.get("prof_l");
                    final double totalTmp = (Double)f.get("offene_l");

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            } else if (key.endsWith("group")) {
                kumFeature.put(key, value);
            } else if (key.endsWith("mw_a") && (value instanceof Double)) {
                double lengthTotal = 0;
                double lengthSpecific = 0;

                for (final Map<String, Object> f : featureListKum) {
                    final double specificTmp = (Double)f.get("mw_l");
                    final double totalTmp = (Double)f.get("prof_l");

                    lengthTotal += totalTmp;
                    lengthSpecific += specificTmp;
                }

                if (lengthTotal == 0) {
                    kumFeature.put(key, 100.0);
                } else {
                    kumFeature.put(key, (lengthSpecific * 100 / lengthTotal));
                }
            } else if (key.endsWith("Min") && (value instanceof Double)) {
                double min = (Double)value;

                for (final Map<String, Object> f : featureListKum) {
                    final double val = (Double)f.get(key);

                    if (stat == PROFSTAT) {
                        f.put("key", null);
                    }

                    if (val < min) {
                        min = val;
                    }
                }

                kumFeature.put(key, min);
            } else if (key.endsWith("Max") && (value instanceof Double)) {
                double max = (Double)value;

                for (final Map<String, Object> f : featureListKum) {
                    final double val = (Double)f.get(key);

                    if (stat == PROFSTAT) {
                        f.put("key", null);
                    }
                    if (val > max) {
                        max = val;
                    }
                }

                kumFeature.put(key, max);
            } else if (key.endsWith("Mit") && (value instanceof Double)) {
                double mit = (Double)value;
                double length = 0.0;

                for (final Map<String, Object> f : featureListKum) {
                    final double val = (Double)f.get(key);
                    final double len = (Double)f.get("prof_l");

                    mit += val * len;
                    length += len;
                }

                kumFeature.put(key, mit / length);
            } else if ((Arrays.binarySearch(exceptionalNumberFields, key) < 0) && (value instanceof Integer)) {
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
            } else if (!(value instanceof String) && !(value instanceof Boolean)
                        && (Arrays.binarySearch(exceptionalNumberFields, key) < 0)) {
                kumFeature.put(key, value);
            }
        }

        return kumFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final int gemNr) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjOffen tmp : gemList) {
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
    private Collection<String> getGu(final int gemNr) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjOffen tmp : gemList) {
            ts.add(tmp.getOwner());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGuId(final int gemNr, final String owner) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObjOffen tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                return tmp.getGu();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr   DOCUMENT ME!
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final int gemNr, final String guName) {
        final List<GmdPartObjOffen> gmdList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjOffen gmdPart : gmdList) {
            if (gmdPart.getOwner().equals(guName)) {
                ts.add(gmdPart.getWdm());
            }
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
    private String getBaCd(final int gemNr, final int gew) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObjOffen tmp : gemList) {
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
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObjOffen tmp : gemList) {
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
    private double getLengthGew(final int gemNr) {
        return getLengthGew(gemNr, -1);
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
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if ((gewId == -1) || (tmp.getId() == gewId)) {
                length += tmp.getLength();
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
    private double getLengthGew(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if ((gewId == -1) || (tmp.getId() == gewId)) {
                length += tmp.getLengthInGewPart(gewId, from, till);
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
    private double getLengthMw(final int gemNr) {
        return getLengthMw(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gemNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getMw() != null) && (tmp.getMw() != 0)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((gewId == -1) || (tmp.getId() == gewId)) && (tmp.getMw() != null) && (tmp.getMw() != 0)) {
                length += tmp.getLengthInGewPart(gewId, from, till);
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gemNr) {
        return getLengthProf(prof, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gemNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final int gemNr, final boolean min) {
        return getMinMax(field, gemNr, -1, min);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final int gemNr, final int gewId, final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((gewId == -1) || (tmp.getId() == gewId))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                    firstValue = false;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final int gemNr, final String gu, final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                    firstValue = false;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field,
            final int gemNr,
            final String gu,
            final int wdm,
            final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((tmp.getOwner() != null) && tmp.getOwner().equals(gu))
                        && ((tmp.getWdm() != null) && tmp.getWdm().equals(wdm))) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                    firstValue = false;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field,
            final int gemNr,
            final int gewId,
            final double from,
            final double till,
            final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                final double value = tmp.get(field);

                if (value == 0.0) {
                    continue;
                }

                if (firstValue || (min && (value < currentVal)) || (!min && (value > currentVal))) {
                    currentVal = value;
                    firstValue = false;
                }
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gemNr) {
        return getMit(field, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gemNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gemNr) {
        return getSum(field, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gemNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                final double value = tmp.get(field);
                currentVal += value;
            }
        }

        return currentVal;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  gewId DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double currentVal = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gemNr) {
        return getCountProf(prof, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gemNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   prof   DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gemNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
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
    private double getLengthGew(final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObjOffen tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWdm() == wdm)) {
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
