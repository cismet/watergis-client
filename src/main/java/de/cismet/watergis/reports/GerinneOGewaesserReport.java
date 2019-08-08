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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.cismet.cids.custom.watergis.server.search.AllGewOffenByGem;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneOGewaesserReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GmdPartObjOffen;
import de.cismet.watergis.reports.types.KatasterGewObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOGewaesserReport extends GerinneOGemeindeReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOGewaesserReport.class);
    private static final String[] exceptionalNumberFields = {
            "gmdNummer",
            "gmdName",
            "code",
            "anzahlGu",
            "gu"
        };
    private static final int PROFSTAT = 1;

    static {
        Arrays.sort(exceptionalNumberFields);
    }

    //~ Instance fields --------------------------------------------------------

    private List<GmdPartObjOffen> objList;
    private final List<String> sheetNames = new ArrayList<String>();
    private GerOffenHelper helper;

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
    public File createGewaesserReport(final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("sumGu", GerinneOGewaesserReportDialog.getInstance().isSumGu());
        parameters.put("wdm", GerinneOGewaesserReportDialog.getInstance().isPerWdm());
        parameters.put("perAbschn", GerinneOGewaesserReportDialog.getInstance().isPerPart());
        parameters.put("perAbschnProf", GerinneOGewaesserReportDialog.getInstance().isPerPartProf());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneOGewaesserReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/GerinneOffenGew.jasper"));

        init(gew);
        helper = new GerOffenHelper(gew, getAllowedWdms());

        dataSources.put("gewaesser", getGewaesser());

        if (GerinneOGewaesserReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesserAbschnitt", getGewaesserAbschnitt());
        }

        if (GerinneOGewaesserReportDialog.getInstance().isPerPartProf()) {
            dataSources.put("gewaesserAbschnittProf", getGewaesserAbschnittProfil());
        }

        if (GerinneOGewaesserReportDialog.getInstance().isSumGu()) {
            if (GerinneOGewaesserReportDialog.getInstance().isPerWdm()) {
                dataSources.put("gewaesserGuAbschnitt", getGewaesserGuWidmung());
            } else {
                dataSources.put("gewaesserGu", getGewaesserGu());
            }
        }

        parameters.put("gewaesser", helper.getGew().size());
        parameters.put("offeneAbschnitte", helper.getAbschnitteOffen().size());
        parameters.put("profile", objList.size());

        // create print from report and data
        final File file = new File(
                GerinneOGewaesserReportDialog.getInstance().getPath()
                        + "/Gerinne_offen_Gewässer.xls");
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
        final GerinneOGewaesserReport report = new GerinneOGewaesserReport();
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
        final CidsServerSearch search = new AllGewOffenByGem(routeIds, getAllowedWdms());
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

        if (GerinneOGewaesserReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneOGewaesserReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneOGewaesserReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneOGewaesserReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneOGewaesserReportDialog.getInstance().is1505()) {
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

        sheetNames.add("Gewässer");
        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
        for (final int gew : helper.getGew()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            final double offenLength = helper.getLengthOffeneAbschn(gew);
            final double prof = getLengthGew(gew);
            final double mw = getLengthMw(gew);
            final double gewAll = helper.getLengthGewAll(gew);

            feature.put("name", helper.getGewName(gew));
            feature.put("code", helper.getBaCd(gew));
//                feature.put("gew_a", helper.getCountGewAll(gew));
            feature.put("gew_l", helper.getLengthGewAll(gew));
            feature.put("offene_l", offenLength);
            feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
            feature.put("prof_l", prof);
            feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
            feature.put("mw_l", mw);
            feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
            feature.put("profTrap_a", getCountProf("tr", gew));
            feature.put("profTrap_l", getLengthProf("tr", gew));
            feature.put("profRe_a", getCountProf("re", gew));
            feature.put("profRe_l", getLengthProf("re", gew));
            feature.put("brSohleMin", getMinMax("brSo", gew, true));
            feature.put("brSohleMit", getMit("brSo", gew));
            feature.put("brSohleMax", getMinMax("brSo", gew, false));
            feature.put("bvReMin", getMinMax("bvRe", gew, true));
            feature.put("bvReMit", getMit("bvRe", gew));
            feature.put("bvReMax", getMinMax("bvRe", gew, false));
            feature.put("bhReMin", getMinMax("bhRe", gew, true));
            feature.put("bhReMit", getMit("bhRe", gew));
            feature.put("bhReMax", getMinMax("bhRe", gew, false));
            feature.put("blReMin", getMinMax("blRe", gew, true));
            feature.put("blReMit", getMit("blRe", gew));
            feature.put("blReMax", getMinMax("blRe", gew, false));
            feature.put("bvLiMin", getMinMax("bvLi", gew, true));
            feature.put("bvLiMit", getMit("bvLi", gew));
            feature.put("bvLiMax", getMinMax("bvLi", gew, false));
            feature.put("bhLiMin", getMinMax("bhLi", gew, true));
            feature.put("bhLiMit", getMit("bhLi", gew));
            feature.put("bhLiMax", getMinMax("bhLi", gew, false));
            feature.put("blLiMin", getMinMax("blLi", gew, true));
            feature.put("blLiMit", getMit("blLi", gew));
            feature.put("blLiMax", getMinMax("blLi", gew, false));
            feature.put("mwMin", getMinMax("mw", gew, true));
            feature.put("mwMit", getMit("mw", gew));
            feature.put("mwMax", getMinMax("mw", gew, false));
            feature.put("flSohle", getSum("flSo", gew));
            feature.put("flBoeRe", getSum("flBRe", gew));
            feature.put("flBoeLi", getSum("flBLi", gew));
            feature.put("flBoe", getSum("flB", gew));
            feature.put("flGer", getSum("flGer", gew));
            feature.put("brGewMin", getMinMax("brGew", gew, true));
            feature.put("brGewMit", getMit("brGew", gew));
            feature.put("brGewMax", getMinMax("brGew", gew, false));
            feature.put("flGew", getSum("flGew", gew));
            feature.put("blNassReMin", getMinMax("blNRe", gew, true));
            feature.put("blNassReMit", getMit("blNRe", gew));
            feature.put("blNassReMax", getMinMax("blNRe", gew, false));
            feature.put("blTroReMin", getMinMax("blTRe", gew, true));
            feature.put("blTroReMit", getMit("blTRe", gew));
            feature.put("blTroReMax", getMinMax("blTRe", gew, false));
            feature.put("blNassLiMin", getMinMax("blNLi", gew, true));
            feature.put("blNassLiMit", getMit("blNLi", gew));
            feature.put("blNassLiMax", getMinMax("blNLi", gew, false));
            feature.put("blTroLiMin", getMinMax("blTLi", gew, true));
            feature.put("blTroLiMit", getMit("blTLi", gew));
            feature.put("blTroLiMax", getMinMax("blTLi", gew, false));
            feature.put("flQsGerMin", getMinMax("flQsGer", gew, true));
            feature.put("flQsGerMit", getMit("flQsGer", gew));
            feature.put("flQsGerMax", getMinMax("flQsGer", gew, false));
            feature.put("flQsGewMin", getMinMax("flQsGew", gew, true));
            feature.put("flQsGewMit", getMit("flQsGew", gew));
            feature.put("flQsGewMax", getMinMax("flQsGew", gew, false));
            feature.put("flBoeNassRe", getSum("flBnRe", gew));
            feature.put("flBoeTroRe", getSum("flBtRe", gew));
            feature.put("flBoeNassLi", getSum("flBnLi", gew));
            feature.put("flBoeTroLi", getSum("flBtLi", gew));
            feature.put("flBoeNass", getSum("flBn", gew));
            feature.put("flBoeTro", getSum("flBt", gew));
            feature.put("flNass", getSum("flN", gew));
            feature.put("summe", false);
            feature.put("zwischenSumme", false);
            features.add(feature);
            featureListKum.add(feature);
        }
        features.add(createKumFeature(featureListKum, false));

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
        final List<Map<String, Object>> simpleFeatures = new ArrayList<Map<String, Object>>();

        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
        String code = null;
        sheetNames.add("Abschnitte");

        for (final KatasterGewObj gewAbschn : helper.getAbschnitteOffen()) { // use the complete river part
            final Map<String, Object> feature = new HashMap<String, Object>();
            final int id = gewAbschn.getId();
            final double from = gewAbschn.getFrom();
            final double till = gewAbschn.getTill();
            final double prof = getLengthGew(id, from, till);
            final double mw = getLengthMw(id, from, till);

            feature.put("group", "group");
            feature.put("name", helper.getGewName(id));
            feature.put("code", helper.getBaCd(id));
            feature.put("gew_l_ges", helper.getLengthGewAll(id));
            feature.put("von", convertStation(from));
            feature.put("bis", convertStation(till));
            feature.put("laenge", gewAbschn.getLength());
            feature.put("offene_l", gewAbschn.getLength());
            feature.put("offene_a", gewAbschn.getLength() * 100 / helper.getLengthGewAll(id));
            feature.put("prof_l", prof);
            feature.put("prof_a", ((gewAbschn.getLength() != 0) ? (prof * 100 / gewAbschn.getLength()) : 100));
            feature.put("mw_l", mw);
            feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
            feature.put("profTrap_a", getCountProf("tr", id, from, till));
            feature.put("profTrap_l", getLengthProf("tr", id, from, till));
            feature.put("profRe_a", getCountProf("re", id, from, till));
            feature.put("profRe_l", getLengthProf("re", id, from, till));
            feature.put("brSohleMin", getMinMax("brSo", id, from, till, true));
            feature.put("brSohleMit", getMit("brSo", id, from, till));
            feature.put("brSohleMax", getMinMax("brSo", id, from, till, false));
            feature.put("bvReMin", getMinMax("bvRe", id, from, till, true));
            feature.put("bvReMit", getMit("bvRe", id, from, till));
            feature.put("bvReMax", getMinMax("bvRe", id, from, till, false));
            feature.put("bhReMin", getMinMax("bhRe", id, from, till, true));
            feature.put("bhReMit", getMit("bhRe", id, from, till));
            feature.put("bhReMax", getMinMax("bhRe", id, from, till, false));
            feature.put("blReMin", getMinMax("blRe", id, from, till, true));
            feature.put("blReMit", getMit("blRe", id, from, till));
            feature.put("blReMax", getMinMax("blRe", id, from, till, false));
            feature.put("bvLiMin", getMinMax("bvLi", id, from, till, true));
            feature.put("bvLiMit", getMit("bvLi", id, from, till));
            feature.put("bvLiMax", getMinMax("bvLi", id, from, till, false));
            feature.put("bhLiMin", getMinMax("bhLi", id, from, till, true));
            feature.put("bhLiMit", getMit("bhLi", id, from, till));
            feature.put("bhLiMax", getMinMax("bhLi", id, from, till, false));
            feature.put("blLiMin", getMinMax("blLi", id, from, till, true));
            feature.put("blLiMit", getMit("blLi", id, from, till));
            feature.put("blLiMax", getMinMax("blLi", id, from, till, false));
            feature.put("mwMin", getMinMax("mw", id, from, till, true));
            feature.put("mwMit", getMit("mw", id, from, till));
            feature.put("mwMax", getMinMax("mw", id, from, till, false));
            feature.put("flSohle", getSum("flSo", id, from, till));
            feature.put("flBoeRe", getSum("flBRe", id, from, till));
            feature.put("flBoeLi", getSum("flBLi", id, from, till));
            feature.put("flBoe", getSum("flB", id, from, till));
            feature.put("flGer", getSum("flGer", id, from, till));
            feature.put("brGewMin", getMinMax("brGew", id, from, till, true));
            feature.put("brGewMit", getMit("brGew", id, from, till));
            feature.put("brGewMax", getMinMax("brGew", id, from, till, false));
            feature.put("flGew", getSum("flGew", id, from, till));
            feature.put("blNassReMin", getMinMax("blNRe", id, from, till, true));
            feature.put("blNassReMit", getMit("blNRe", id, from, till));
            feature.put("blNassReMax", getMinMax("blNRe", id, from, till, false));
            feature.put("blTroReMin", getMinMax("blTRe", id, from, till, true));
            feature.put("blTroReMit", getMit("blTRe", id, from, till));
            feature.put("blTroReMax", getMinMax("blTRe", id, from, till, false));
            feature.put("blNassLiMin", getMinMax("blNLi", id, from, till, true));
            feature.put("blNassLiMit", getMit("blNLi", id, from, till));
            feature.put("blNassLiMax", getMinMax("blNLi", id, from, till, false));
            feature.put("blTroLiMin", getMinMax("blTLi", id, from, till, true));
            feature.put("blTroLiMit", getMit("blTLi", id, from, till));
            feature.put("blTroLiMax", getMinMax("blTLi", id, from, till, false));
            feature.put("flQsGerMin", getMinMax("flQsGer", id, from, till, true));
            feature.put("flQsGerMit", getMit("flQsGer", id, from, till));
            feature.put("flQsGerMax", getMinMax("flQsGer", id, from, till, false));
            feature.put("flQsGewMin", getMinMax("flQsGew", id, from, till, true));
            feature.put("flQsGewMit", getMit("flQsGew", id, from, till));
            feature.put("flQsGewMax", getMinMax("flQsGew", id, from, till, false));
            feature.put("flBoeNassRe", getSum("flBnRe", id, from, till));
            feature.put("flBoeTroRe", getSum("flBtRe", id, from, till));
            feature.put("flBoeNassLi", getSum("flBnLi", id, from, till));
            feature.put("flBoeTroLi", getSum("flBtLi", id, from, till));
            feature.put("flBoeNass", getSum("flBn", id, from, till));
            feature.put("flBoeTro", getSum("flBt", id, from, till));
            feature.put("flNass", getSum("flN", id, from, till));
            feature.put("summe", false);
            feature.put("zwischenSumme", false);

            final String newCode = helper.getBaCd(gewAbschn.getId());

            if ((code != null) && !code.equals(newCode)) {
                final Map<String, Object> kumFeature = createKumFeature(featureListGewKum, true);
                kumFeature.put("laenge", featureListGewKum.get(0).get("laenge"));
                kumFeature.put("code", featureListGewKum.get(0).get("code"));
                kumFeature.put("name", featureListGewKum.get(0).get("name"));
                features.add(kumFeature);
                featureListGewKum.clear();
            }

            code = newCode;
            features.add(feature);
            simpleFeatures.add(feature);
            featureListKum.add(feature);
            featureListGewKum.add(feature);
        }
        final Map<String, Object> kumFeature = createKumFeature(featureListKum, false, PROFSTAT);

        for (final Map<String, Object> tmp : simpleFeatures) {
            tmp.put("code", null);
            tmp.put("name", null);
            tmp.put("laenge", null);
        }
        double laenge = 0.0;

        for (final Map<String, Object> tmp : features) {
            if (tmp.get("laenge") != null) {
                laenge += (Double)tmp.get("laenge");
            }
        }

        kumFeature.put("laenge", laenge);
        features.add(kumFeature);

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

        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> simpleFeatures = new ArrayList<Map<String, Object>>();
        String code = null;
        sheetNames.add("Profile");

        for (final KatasterGewObj gewAbschn : helper.getAbschnitteOffen(objList)) { // use the complete river part
            final Map<String, Object> feature = new HashMap<String, Object>();
            final int id = gewAbschn.getId();
            final double from = gewAbschn.getFrom();
            final double till = gewAbschn.getTill();
            final double prof = getLengthGew(id, from, till);
            final double mw = getLengthMw(id, from, till);
            String profTyp = null;

            if (getCountProf("tr", id, from, till) > 0) {
                profTyp = "tr";
            } else if (getCountProf("re", id, from, till) > 0) {
                profTyp = "re";
            }

            feature.put("group", "group");
            feature.put("name", helper.getGewName(id));
            feature.put("code", helper.getBaCd(id));
            feature.put("laenge", gewAbschn.getLength());
            feature.put("gew_l", helper.getLengthGewAll(id));
            feature.put("von", convertStation(from));
            feature.put("bis", convertStation(till));
            feature.put("laenge", gewAbschn.getLength());
            feature.put("offene_a", gewAbschn.getLength() * 100 / helper.getLengthGewAll(id));
            feature.put("prof", ((prof > 0) ? "x" : null));
            feature.put("mw", ((mw > 0) ? "x" : null));
            feature.put("profTyp", profTyp);
            feature.put("brSohleMit", getMit("brSo", id, from, till));
            feature.put("bvReMit", getMit("bvRe", id, from, till));
            feature.put("bhReMit", getMit("bhRe", id, from, till));
            feature.put("blReMit", getMit("blRe", id, from, till));
            feature.put("bvLiMit", getMit("bvLi", id, from, till));
            feature.put("bhLiMit", getMit("bhLi", id, from, till));
            feature.put("blLiMit", getMit("blLi", id, from, till));
            feature.put("mwMit", getMit("mw", id, from, till));
            feature.put("flSohle", getSum("flSo", id, from, till));
            feature.put("flBoeRe", getSum("flBRe", id, from, till));
            feature.put("flBoeLi", getSum("flBLi", id, from, till));
            feature.put("flBoe", getSum("flB", id, from, till));
            feature.put("flGer", getSum("flGer", id, from, till));
            feature.put("brGewMit", getMit("brGew", id, from, till));
            feature.put("flGew", getSum("flGew", id, from, till));
            feature.put("blNassReMit", getMit("blNRe", id, from, till));
            feature.put("blTroReMit", getMit("blTRe", id, from, till));
            feature.put("blNassLiMit", getMit("blNLi", id, from, till));
            feature.put("blTroLiMit", getMit("blTLi", id, from, till));
            feature.put("flQsGerMit", getMit("flQsGer", id, from, till));
            feature.put("flQsGewMit", getMit("flQsGew", id, from, till));
            feature.put("flBoeNassRe", getSum("flBnRe", id, from, till));
            feature.put("flBoeTroRe", getSum("flBtRe", id, from, till));
            feature.put("flBoeNassLi", getSum("flBnLi", id, from, till));
            feature.put("flBoeTroLi", getSum("flBtLi", id, from, till));
            feature.put("flBoeNass", getSum("flBn", id, from, till));
            feature.put("flBoeTro", getSum("flBt", id, from, till));
            feature.put("flNass", getSum("flN", id, from, till));
            feature.put("summe", false);
            feature.put("zwischenSumme", false);

            final String newCode = helper.getBaCd(gewAbschn.getId());

            if ((code != null) && !code.equals(newCode)) {
                final Map<String, Object> kumFeature = createKumFeature(featureListGewKum, true);
                kumFeature.put("gew_l", featureListGewKum.get(0).get("gew_l"));
                kumFeature.put("code", featureListGewKum.get(0).get("code"));
                kumFeature.put("name", featureListGewKum.get(0).get("name"));

                features.add(kumFeature);
                featureListGewKum.clear();
            }

            code = newCode;
            features.add(feature);
            simpleFeatures.add(feature);
            featureListKum.add(feature);
            featureListGewKum.add(feature);
        }
        final Map<String, Object> kumFeature = createKumFeature(featureListKum, false, PROFSTAT);

        for (final Map<String, Object> tmp : simpleFeatures) {
            tmp.put("code", null);
            tmp.put("name", null);
            tmp.put("gew_l", null);
        }
        double laenge = 0.0;

        for (final Map<String, Object> tmp : features) {
            if (tmp.get("gew_l") != null) {
                laenge += (Double)tmp.get("gew_l");
            }
        }
        kumFeature.put("gew_l", laenge);
        features.add(kumFeature);

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

        sheetNames.add("GU ");
        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

        for (final String guName : helper.getGu()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            final double offenLength = helper.getLengthOffeneAbschn(guName);
            final double prof = getLengthGew(guName);
            final double mw = getLengthMw(guName);
            final double gewAll = helper.getLengthGewAll(guName);

            feature.put("group", "group");
            feature.put("name", guName);
            feature.put("gu", helper.getGuId(guName));
            feature.put("gew_a", helper.getCountGewAll(guName));
            feature.put("gew_l", helper.getLengthGewAll(guName));
            feature.put("offene_l", offenLength);
            feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
            feature.put("prof_l", prof);
            feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
            feature.put("mw_l", mw);
            feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
            feature.put("profTrap_a", getCountProf("tr", guName));
            feature.put("profTrap_l", getLengthProf("tr", guName));
            feature.put("profRe_a", getCountProf("re", guName));
            feature.put("profRe_l", getLengthProf("re", guName));
            feature.put("brSohleMin", getMinMax("brSo", guName, true));
            feature.put("brSohleMit", getMit("brSo", guName));
            feature.put("brSohleMax", getMinMax("brSo", guName, false));
            feature.put("bvReMin", getMinMax("bvRe", guName, true));
            feature.put("bvReMit", getMit("bvRe", guName));
            feature.put("bvReMax", getMinMax("bvRe", guName, false));
            feature.put("bhReMin", getMinMax("bhRe", guName, true));
            feature.put("bhReMit", getMit("bhRe", guName));
            feature.put("bhReMax", getMinMax("bhRe", guName, false));
            feature.put("blReMin", getMinMax("blRe", guName, true));
            feature.put("blReMit", getMit("blRe", guName));
            feature.put("blReMax", getMinMax("blRe", guName, false));
            feature.put("bvLiMin", getMinMax("bvLi", guName, true));
            feature.put("bvLiMit", getMit("bvLi", guName));
            feature.put("bvLiMax", getMinMax("bvLi", guName, false));
            feature.put("bhLiMin", getMinMax("bhLi", guName, true));
            feature.put("bhLiMit", getMit("bhLi", guName));
            feature.put("bhLiMax", getMinMax("bhLi", guName, false));
            feature.put("blLiMin", getMinMax("blLi", guName, true));
            feature.put("blLiMit", getMit("blLi", guName));
            feature.put("blLiMax", getMinMax("blLi", guName, false));
            feature.put("mwMin", getMinMax("mw", guName, true));
            feature.put("mwMit", getMit("mw", guName));
            feature.put("mwMax", getMinMax("mw", guName, false));
            feature.put("flSohle", getSum("flSo", guName));
            feature.put("flBoeRe", getSum("flBRe", guName));
            feature.put("flBoeLi", getSum("flBLi", guName));
            feature.put("flBoe", getSum("flB", guName));
            feature.put("flGer", getSum("flGer", guName));
            feature.put("brGewMin", getMinMax("brGew", guName, true));
            feature.put("brGewMit", getMit("brGew", guName));
            feature.put("brGewMax", getMinMax("brGew", guName, false));
            feature.put("flGew", getSum("flGew", guName));
            feature.put("blNassReMin", getMinMax("blNRe", guName, true));
            feature.put("blNassReMit", getMit("blNRe", guName));
            feature.put("blNassReMax", getMinMax("blNRe", guName, false));
            feature.put("blTroReMin", getMinMax("blTRe", guName, true));
            feature.put("blTroReMit", getMit("blTRe", guName));
            feature.put("blTroReMax", getMinMax("blTRe", guName, false));
            feature.put("blNassLiMin", getMinMax("blNLi", guName, true));
            feature.put("blNassLiMit", getMit("blNLi", guName));
            feature.put("blNassLiMax", getMinMax("blNLi", guName, false));
            feature.put("blTroLiMin", getMinMax("blTLi", guName, true));
            feature.put("blTroLiMit", getMit("blTLi", guName));
            feature.put("blTroLiMax", getMinMax("blTLi", guName, false));
            feature.put("flQsGerMin", getMinMax("flQsGer", guName, true));
            feature.put("flQsGerMit", getMit("flQsGer", guName));
            feature.put("flQsGerMax", getMinMax("flQsGer", guName, false));
            feature.put("flQsGewMin", getMinMax("flQsGew", guName, true));
            feature.put("flQsGewMit", getMit("flQsGew", guName));
            feature.put("flQsGewMax", getMinMax("flQsGew", guName, false));
            feature.put("flBoeNassRe", getSum("flBnRe", guName));
            feature.put("flBoeTroRe", getSum("flBtRe", guName));
            feature.put("flBoeNassLi", getSum("flBnLi", guName));
            feature.put("flBoeTroLi", getSum("flBtLi", guName));
            feature.put("flBoeNass", getSum("flBn", guName));
            feature.put("flBoeTro", getSum("flBt", guName));
            feature.put("flNass", getSum("flN", guName));
            feature.put("summe", false);
            feature.put("zwischenSumme", false);

            features.add(feature);
            featureListKum.add(feature);
        }
        if (!featureListKum.isEmpty()) {
            features.add(createKumFeature(featureListKum, false));
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
        final List<Map<String, Object>> simpleFeatures = new ArrayList<Map<String, Object>>();

        sheetNames.add("GU");
        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

        for (final String guName : helper.getGu()) {
            final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

            for (final Integer wdm : helper.getWidmung(guName)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                final double offenLength = helper.getLengthOffeneAbschn(guName, wdm);
                final double prof = getLengthGew(guName, wdm);
                final double mw = getLengthMw(guName, wdm);
                final double gewAll = helper.getLengthGewAll(guName, wdm);

                feature.put("group", "group");
                feature.put("name", helper.getGuId(guName));
                feature.put("gu", guName);
                feature.put("wdm", wdm);
                feature.put("gew_a", helper.getCountGewAll(guName, wdm));
                feature.put("gew_l", helper.getLengthGewAll(guName, wdm));
                feature.put("offene_l", offenLength);
                feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                feature.put("prof_l", prof);
                feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : offenLength));
                feature.put("mw_l", mw);
                feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                feature.put("profTrap_a", getCountProf("tr", guName, wdm));
                feature.put("profTrap_l", getLengthProf("tr", guName, wdm));
                feature.put("profRe_a", getCountProf("re", guName, wdm));
                feature.put("profRe_l", getLengthProf("re", guName, wdm));
                feature.put("brSohleMin", getMinMax("brSo", guName, wdm, true));
                feature.put("brSohleMit", getMit("brSo", guName, wdm));
                feature.put("brSohleMax", getMinMax("brSo", guName, wdm, false));
                feature.put("bvReMin", getMinMax("bvRe", guName, wdm, true));
                feature.put("bvReMit", getMit("bvRe", guName, wdm));
                feature.put("bvReMax", getMinMax("bvRe", guName, wdm, false));
                feature.put("bhReMin", getMinMax("bhRe", guName, wdm, true));
                feature.put("bhReMit", getMit("bhRe", guName, wdm));
                feature.put("bhReMax", getMinMax("bhRe", guName, wdm, false));
                feature.put("blReMin", getMinMax("blRe", guName, wdm, true));
                feature.put("blReMit", getMit("blRe", guName, wdm));
                feature.put("blReMax", getMinMax("blRe", guName, wdm, false));
                feature.put("bvLiMin", getMinMax("bvLi", guName, wdm, true));
                feature.put("bvLiMit", getMit("bvLi", guName, wdm));
                feature.put("bvLiMax", getMinMax("bvLi", guName, wdm, false));
                feature.put("bhLiMin", getMinMax("bhLi", guName, wdm, true));
                feature.put("bhLiMit", getMit("bhLi", guName, wdm));
                feature.put("bhLiMax", getMinMax("bhLi", guName, wdm, false));
                feature.put("blLiMin", getMinMax("blLi", guName, wdm, true));
                feature.put("blLiMit", getMit("blLi", guName, wdm));
                feature.put("blLiMax", getMinMax("blLi", guName, wdm, false));
                feature.put("mwMin", getMinMax("mw", guName, wdm, true));
                feature.put("mwMit", getMit("mw", guName, wdm));
                feature.put("mwMax", getMinMax("mw", guName, wdm, false));
                feature.put("flSohle", getSum("flSo", guName, wdm));
                feature.put("flBoeRe", getSum("flBRe", guName, wdm));
                feature.put("flBoeLi", getSum("flBLi", guName, wdm));
                feature.put("flBoe", getSum("flB", guName, wdm));
                feature.put("flGer", getSum("flGer", guName, wdm));
                feature.put("brGewMin", getMinMax("brGew", guName, wdm, true));
                feature.put("brGewMit", getMit("brGew", guName, wdm));
                feature.put("brGewMax", getMinMax("brGew", guName, wdm, false));
                feature.put("flGew", getSum("flGew", guName, wdm));
                feature.put("blNassReMin", getMinMax("blNRe", guName, wdm, true));
                feature.put("blNassReMit", getMit("blNRe", guName, wdm));
                feature.put("blNassReMax", getMinMax("blNRe", guName, wdm, false));
                feature.put("blTroReMin", getMinMax("blTRe", guName, wdm, true));
                feature.put("blTroReMit", getMit("blTRe", guName, wdm));
                feature.put("blTroReMax", getMinMax("blTRe", guName, wdm, false));
                feature.put("blNassLiMin", getMinMax("blNLi", guName, wdm, true));
                feature.put("blNassLiMit", getMit("blNLi", guName, wdm));
                feature.put("blNassLiMax", getMinMax("blNLi", guName, wdm, false));
                feature.put("blTroLiMin", getMinMax("blTLi", guName, wdm, true));
                feature.put("blTroLiMit", getMit("blTLi", guName, wdm));
                feature.put("blTroLiMax", getMinMax("blTLi", guName, wdm, false));
                feature.put("flQsGerMin", getMinMax("flQsGer", guName, wdm, true));
                feature.put("flQsGerMit", getMit("flQsGer", guName, wdm));
                feature.put("flQsGerMax", getMinMax("flQsGer", guName, wdm, false));
                feature.put("flQsGewMin", getMinMax("flQsGew", guName, wdm, true));
                feature.put("flQsGewMit", getMit("flQsGew", guName, wdm));
                feature.put("flQsGewMax", getMinMax("flQsGew", guName, wdm, false));
                feature.put("flBoeNassRe", getSum("flBnRe", guName, wdm));
                feature.put("flBoeTroRe", getSum("flBtRe", guName, wdm));
                feature.put("flBoeNassLi", getSum("flBnLi", guName, wdm));
                feature.put("flBoeTroLi", getSum("flBtLi", guName, wdm));
                feature.put("flBoeNass", getSum("flBn", guName, wdm));
                feature.put("flBoeTro", getSum("flBt", guName, wdm));
                feature.put("flNass", getSum("flN", guName, wdm));
                feature.put("summe", false);
                feature.put("zwischenSumme", false);

                simpleFeatures.add(feature);
                features.add(feature);
                featureListKum.add(feature);
                featureListGuKum.add(feature);
            }
            final Map<String, Object> kumFeature = createKumFeature(featureListGuKum, true);
            kumFeature.put("wdm", null);
            kumFeature.put("name", helper.getGuId(guName));
            kumFeature.put("gu", guName);
            features.add(kumFeature);
        }
        final Map<String, Object> kumFeature = createKumFeature(featureListKum, false);

        for (final Map<String, Object> tmp : simpleFeatures) {
            tmp.put("name", null);
            tmp.put("gu", null);
        }

        kumFeature.put("wdm", null);
        features.add(kumFeature);

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
            } else if (key.endsWith("prof_a") && (value instanceof Double)) {
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
            } else if (key.endsWith("mw_a") && (value instanceof Double)) {
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
                double min = toDouble(value);

                for (final Map<String, Object> f : featureListKum) {
                    final double val = toDouble(f.get(key));

                    if (stat == PROFSTAT) {
                        f.put("key", null);
                    }

                    if ((val != 0.0) && (val < min)) {
                        min = val;
                    }
                }

                if (min != 0.0) {
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
    private double getLengthMw(final String gu, final int wdm) {
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
    private double getLengthProf(final String prof, final String gu, final int wdm) {
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
     * @param   field  gemNr DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final int gewId, final boolean min) {
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
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
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final String gu, final boolean min) {
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
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
     * @param   field  gemNr DOCUMENT ME!
     * @param   gu     gewId DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final String gu, final int wdm, final boolean min) {
        double currentVal = 0;
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
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
        boolean firstValue = true;

        for (final GmdPartObjOffen tmp : objList) {
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
    private double getMit(final String field, final String gu, final int wdm) {
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
    private double getSum(final String field, final String gu, final int wdm) {
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
    public int getCountProf(final String prof, final String gu, final int wdm) {
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
