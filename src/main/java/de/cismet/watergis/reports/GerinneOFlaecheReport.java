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

import de.cismet.cids.custom.watergis.server.search.AllGewOffenByGeom;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.GerinneOFlaechenReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.Flaeche;
import de.cismet.watergis.reports.types.GewFlObj;
import de.cismet.watergis.reports.types.GmdPartObjOffen;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerinneOFlaecheReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneOFlaecheReport.class);
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

    private final Map<Object, List<GmdPartObjOffen>> gemPartMap = new HashMap<Object, List<GmdPartObjOffen>>();
    private final Map<Object, Flaeche> gemDataMap = new HashMap<Object, Flaeche>();
    private final List<String> sheetNames = new ArrayList<String>();
    private GerOffenFlHelper helper;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   fl   gemId baCd DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createFlaechenReport(final Flaeche[] fl, final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("klasse", GerinneOFlaechenReportDialog.getInstance().getFlaechenService().getName());
        parameters.put("attr1", GerinneOFlaechenReportDialog.getInstance().getAttr1());
        parameters.put("attr2", GerinneOFlaechenReportDialog.getInstance().getAttr2());
        parameters.put("datum", df.format(new Date()));
        parameters.put("perGew", GerinneOFlaechenReportDialog.getInstance().isPerGew());
        parameters.put("perAbschn", GerinneOFlaechenReportDialog.getInstance().isPerPart());
        parameters.put("sumGu", GerinneOFlaechenReportDialog.getInstance().isSumGu());
        parameters.put("wdm", GerinneOFlaechenReportDialog.getInstance().isPerWdm());
        parameters.put("perAbschnProf", GerinneOFlaechenReportDialog.getInstance().isPerPartProf());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GerinneOFlaecheReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/GerinneOffenGem.jasper"));

        init(fl, gew);
        helper = new GerOffenFlHelper(fl, gew, getAllowedWdms());
        dataSources.put("gemeinden", getGemeindenAll());

        if (GerinneOFlaechenReportDialog.getInstance().isPerGew()
                    && !GerinneOFlaechenReportDialog.getInstance().isPerPart()
                    && !GerinneOFlaechenReportDialog.getInstance().isPerPartProf()) {
            dataSources.put("gewaesser", getGewaesser());
        } else if (GerinneOFlaechenReportDialog.getInstance().isPerPartProf()) {
            dataSources.put("gewaesserAbschnittProf", getGewaesserAbschnittProfil());
        } else if (GerinneOFlaechenReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesserAbschnitt", getGewaesserAbschnitt());
        }

        if (GerinneOFlaechenReportDialog.getInstance().isSumGu()) {
            if (GerinneOFlaechenReportDialog.getInstance().isPerWdm()) {
                dataSources.put("gewaesserGuAbschnitt", getGewaesserGuWidmung());
            } else {
                dataSources.put("gewaesserGu", getGewaesserGu());
            }
        }

        // create print from report and data
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        final FileOutputStream fout = new FileOutputStream(new File(
                    GerinneOFlaechenReportDialog.getInstance().getPath()
                            + "/GerinneOffenFläche.xls"));
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
        final GerinneOFlaecheReport report = new GerinneOFlaecheReport();
        try {
//            report.createGemeindeReport(new int[] { 2 }, new int[] { 2 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fls       flNr DOCUMENT ME!
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
            gemDataMap.put(fl.getAttr1(), fl);
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
    private List<GmdPartObjOffen> getAllRoutes(final Flaeche fl, final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewOffenByGeom(routeIds, getAllowedWdms(), fl.getGeom());
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

        if (GerinneOFlaechenReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (GerinneOFlaechenReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (GerinneOFlaechenReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (GerinneOFlaechenReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (GerinneOFlaechenReportDialog.getInstance().is1505()) {
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

        for (final Object flAttr : gemDataMap.keySet()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            final double offenLength = helper.getLengthOffeneAbschn(flAttr);
            final double prof = getLengthGew(flAttr);
            final double mw = getLengthMw(flAttr);
            final double gewAll = helper.getLengthGewAll(flAttr);

            feature.put("name", gemDataMap.get(flAttr).getAttr2());
            feature.put("nummer", flAttr);
            feature.put("gew_a", helper.getCountGewAll(flAttr));
            feature.put("gew_l", helper.getLengthGewAll(flAttr));
            feature.put("offene_l", offenLength);
            feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
            feature.put("prof_l", prof);
            feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
            feature.put("mw_l", mw);
            feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
            feature.put("profTrap_a", getCountProf("tr", flAttr));
            feature.put("profTrap_l", getLengthProf("tr", flAttr));
            feature.put("profRe_a", getCountProf("re", flAttr));
            feature.put("profRe_l", getLengthProf("re", flAttr));
            feature.put("brSohleMin", getMinMax("brSo", flAttr, true));
            feature.put("brSohleMit", getMit("brSo", flAttr));
            feature.put("brSohleMax", getMinMax("brSo", flAttr, false));
            feature.put("bvReMin", getMinMax("bvRe", flAttr, true));
            feature.put("bvReMit", getMit("bvRe", flAttr));
            feature.put("bvReMax", getMinMax("bvRe", flAttr, false));
            feature.put("bhReMin", getMinMax("bhRe", flAttr, true));
            feature.put("bhReMit", getMit("bhRe", flAttr));
            feature.put("bhReMax", getMinMax("bhRe", flAttr, false));
            feature.put("blReMin", getMinMax("blRe", flAttr, true));
            feature.put("blReMit", getMit("blRe", flAttr));
            feature.put("blReMax", getMinMax("blRe", flAttr, false));
            feature.put("bvLiMin", getMinMax("bvLi", flAttr, true));
            feature.put("bvLiMit", getMit("bvLi", flAttr));
            feature.put("bvLiMax", getMinMax("bvLi", flAttr, false));
            feature.put("bhLiMin", getMinMax("bhLi", flAttr, true));
            feature.put("bhLiMit", getMit("bhLi", flAttr));
            feature.put("bhLiMax", getMinMax("bhLi", flAttr, false));
            feature.put("blLiMin", getMinMax("blLi", flAttr, true));
            feature.put("blLiMit", getMit("blLi", flAttr));
            feature.put("blLiMax", getMinMax("blLi", flAttr, false));
            feature.put("flSohle", getSum("flSo", flAttr));
            feature.put("flBoeRe", getSum("flBRe", flAttr));
            feature.put("flBoeLi", getSum("flBLi", flAttr));
            feature.put("flBoe", getSum("flB", flAttr));
            feature.put("flGer", getSum("flGer", flAttr));
            feature.put("brGewMin", getMinMax("brGew", flAttr, true));
            feature.put("brGewMit", getMit("brGew", flAttr));
            feature.put("brGewMax", getMinMax("brGew", flAttr, false));
            feature.put("flGew", getSum("flGew", flAttr));
            feature.put("blNassReMin", getMinMax("blNRe", flAttr, true));
            feature.put("blNassReMit", getMit("blNRe", flAttr));
            feature.put("blNassReMax", getMinMax("blNRe", flAttr, false));
            feature.put("blTroReMin", getMinMax("blTRe", flAttr, true));
            feature.put("blTroReMit", getMit("blTRe", flAttr));
            feature.put("blTroReMax", getMinMax("blTRe", flAttr, false));
            feature.put("blNassLiMin", getMinMax("blNLi", flAttr, true));
            feature.put("blNassLiMit", getMit("blNLi", flAttr));
            feature.put("blNassLiMax", getMinMax("blNLi", flAttr, false));
            feature.put("blTroLiMin", getMinMax("blTLi", flAttr, true));
            feature.put("blTroLiMit", getMit("blTLi", flAttr));
            feature.put("blTroLiMax", getMinMax("blTLi", flAttr, false));
            feature.put("flBoeNassRe", getSum("flBnRe", flAttr));
            feature.put("flBoeTroRe", getSum("flBtRe", flAttr));
            feature.put("flBoeNassLi", getSum("flBnLi", flAttr));
            feature.put("flBoeTroLi", getSum("flBtLi", flAttr));
            feature.put("flBoeNass", getSum("flBn", flAttr));
            feature.put("flBoeTro", getSum("flBt", flAttr));
            feature.put("flNass", getSum("flN", flAttr));
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

        for (final Object flAttr : gemDataMap.keySet()) {
            if ((getGew(flAttr) != null) && !getGew(flAttr).isEmpty()) {
                sheetNames.add(String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
                for (final int gew : getGew(flAttr)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final double offenLength = helper.getLengthOffeneAbschn(flAttr, gew);
                    final double prof = getLengthGew(flAttr, gew);
                    final double mw = getLengthMw(flAttr, gew);
                    final double gewAll = helper.getLengthGewAll(flAttr, gew);

                    feature.put("name", getGewName(flAttr, gew));
                    feature.put("code", getBaCd(flAttr, gew));
                    // feature.put("gew_a", helper.getCountGewAll(gem, gew));
                    feature.put("gew_l", helper.getLengthGewAll(flAttr, gew));
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", flAttr, gew));
                    feature.put("profTrap_l", getLengthProf("tr", flAttr, gew));
                    feature.put("profRe_a", getCountProf("re", flAttr, gew));
                    feature.put("profRe_l", getLengthProf("re", flAttr, gew));
                    feature.put("brSohleMin", getMinMax("brSo", flAttr, gew, true));
                    feature.put("brSohleMit", getMit("brSo", flAttr, gew));
                    feature.put("brSohleMax", getMinMax("brSo", flAttr, gew, false));
                    feature.put("bvReMin", getMinMax("bvRe", flAttr, gew, true));
                    feature.put("bvReMit", getMit("bvRe", flAttr, gew));
                    feature.put("bvReMax", getMinMax("bvRe", flAttr, gew, false));
                    feature.put("bhReMin", getMinMax("bhRe", flAttr, gew, true));
                    feature.put("bhReMit", getMit("bhRe", flAttr, gew));
                    feature.put("bhReMax", getMinMax("bhRe", flAttr, gew, false));
                    feature.put("blReMin", getMinMax("blRe", flAttr, gew, true));
                    feature.put("blReMit", getMit("blRe", flAttr, gew));
                    feature.put("blReMax", getMinMax("blRe", flAttr, gew, false));
                    feature.put("bvLiMin", getMinMax("bvLi", flAttr, gew, true));
                    feature.put("bvLiMit", getMit("bvLi", flAttr, gew));
                    feature.put("bvLiMax", getMinMax("bvLi", flAttr, gew, false));
                    feature.put("bhLiMin", getMinMax("bhLi", flAttr, gew, true));
                    feature.put("bhLiMit", getMit("bhLi", flAttr, gew));
                    feature.put("bhLiMax", getMinMax("bhLi", flAttr, gew, false));
                    feature.put("blLiMin", getMinMax("blLi", flAttr, gew, true));
                    feature.put("blLiMit", getMit("blLi", flAttr, gew));
                    feature.put("blLiMax", getMinMax("blLi", flAttr, gew, false));
                    feature.put("flSohle", getSum("flSo", flAttr, gew));
                    feature.put("flBoeRe", getSum("flBRe", flAttr, gew));
                    feature.put("flBoeLi", getSum("flBLi", flAttr, gew));
                    feature.put("flBoe", getSum("flB", flAttr, gew));
                    feature.put("flGer", getSum("flGer", flAttr, gew));
                    feature.put("brGewMin", getMinMax("brGew", flAttr, gew, true));
                    feature.put("brGewMit", getMit("brGew", flAttr, gew));
                    feature.put("brGewMax", getMinMax("brGew", flAttr, gew, false));
                    feature.put("flGew", getSum("flGew", flAttr, gew));
                    feature.put("blNassReMin", getMinMax("blNRe", flAttr, gew, true));
                    feature.put("blNassReMit", getMit("blNRe", flAttr, gew));
                    feature.put("blNassReMax", getMinMax("blNRe", flAttr, gew, false));
                    feature.put("blTroReMin", getMinMax("blTRe", flAttr, gew, true));
                    feature.put("blTroReMit", getMit("blTRe", flAttr, gew));
                    feature.put("blTroReMax", getMinMax("blTRe", flAttr, gew, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", flAttr, gew, true));
                    feature.put("blNassLiMit", getMit("blNLi", flAttr, gew));
                    feature.put("blNassLiMax", getMinMax("blNLi", flAttr, gew, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", flAttr, gew, true));
                    feature.put("blTroLiMit", getMit("blTLi", flAttr, gew));
                    feature.put("blTroLiMax", getMinMax("blTLi", flAttr, gew, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", flAttr, gew, true));
                    feature.put("flQsGerMit", getMit("flQsGer", flAttr, gew));
                    feature.put("flQsGerMax", getMinMax("flQsGer", flAttr, gew, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", flAttr, gew, true));
                    feature.put("flQsGewMit", getMit("flQsGew", flAttr, gew));
                    feature.put("flQsGewMax", getMinMax("flQsGew", flAttr, gew, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", flAttr, gew));
                    feature.put("flBoeTroRe", getSum("flBtRe", flAttr, gew));
                    feature.put("flBoeNassLi", getSum("flBnLi", flAttr, gew));
                    feature.put("flBoeTroLi", getSum("flBtLi", flAttr, gew));
                    feature.put("flBoeNass", getSum("flBn", flAttr, gew));
                    feature.put("flBoeTro", getSum("flBt", flAttr, gew));
                    feature.put("flNass", getSum("flN", flAttr, gew));
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

        for (final Object flAttr : gemDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            if ((helper.getGemPartMap().get(flAttr) != null) && !helper.getGemPartMap().get(flAttr).isEmpty()) {
                sheetNames.add(String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                for (final GewFlObj gew : helper.getGemPartMap().get(flAttr)) { // use the complete river part
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final int id = gew.getId();
                    final double from = gew.getFrom();
                    final double till = gew.getTill();
                    final double offenLength = helper.getLengthOffeneAbschn(flAttr, id, from, till);
                    final double prof = getLengthGew(flAttr, id, from, till);
                    final double mw = getLengthMw(flAttr, id, from, till);
                    final double gewAll = helper.getLengthGewAll(flAttr, id);

                    feature.put("name", getGewName(flAttr, id));
                    feature.put("code", getBaCd(flAttr, id));
                    // feature.put("gew_a", helper.getCountGewAll(gem, id, from, till));
                    feature.put("gew_l", helper.getLengthGewAll(flAttr, id));
                    feature.put("von", convertStation(from));
                    feature.put("bis", convertStation(till));
                    feature.put("laenge", gew.getLength());
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", flAttr, id, from, till));
                    feature.put("profTrap_l", getLengthProf("tr", flAttr, id, from, till));
                    feature.put("profRe_a", getCountProf("re", flAttr, id, from, till));
                    feature.put("profRe_l", getLengthProf("re", flAttr, id, from, till));
                    feature.put("brSohleMin", getMinMax("brSo", flAttr, id, from, till, true));
                    feature.put("brSohleMit", getMit("brSo", flAttr, id, from, till));
                    feature.put("brSohleMax", getMinMax("brSo", flAttr, id, from, till, false));
                    feature.put("bvReMin", getMinMax("bvRe", flAttr, id, from, till, true));
                    feature.put("bvReMit", getMit("bvRe", flAttr, id, from, till));
                    feature.put("bvReMax", getMinMax("bvRe", flAttr, id, from, till, false));
                    feature.put("bhReMin", getMinMax("bhRe", flAttr, id, from, till, true));
                    feature.put("bhReMit", getMit("bhRe", flAttr, id, from, till));
                    feature.put("bhReMax", getMinMax("bhRe", flAttr, id, from, till, false));
                    feature.put("blReMin", getMinMax("blRe", flAttr, id, from, till, true));
                    feature.put("blReMit", getMit("blRe", flAttr, id, from, till));
                    feature.put("blReMax", getMinMax("blRe", flAttr, id, from, till, false));
                    feature.put("bvLiMin", getMinMax("bvLi", flAttr, id, from, till, true));
                    feature.put("bvLiMit", getMit("bvLi", flAttr, id, from, till));
                    feature.put("bvLiMax", getMinMax("bvLi", flAttr, id, from, till, false));
                    feature.put("bhLiMin", getMinMax("bhLi", flAttr, id, from, till, true));
                    feature.put("bhLiMit", getMit("bhLi", flAttr, id, from, till));
                    feature.put("bhLiMax", getMinMax("bhLi", flAttr, id, from, till, false));
                    feature.put("blLiMin", getMinMax("blLi", flAttr, id, from, till, true));
                    feature.put("blLiMit", getMit("blLi", flAttr, id, from, till));
                    feature.put("blLiMax", getMinMax("blLi", flAttr, id, from, till, false));
                    feature.put("flSohle", getSum("flSo", flAttr, id, from, till));
                    feature.put("flBoeRe", getSum("flBRe", flAttr, id, from, till));
                    feature.put("flBoeLi", getSum("flBLi", flAttr, id, from, till));
                    feature.put("flBoe", getSum("flB", flAttr, id, from, till));
                    feature.put("flGer", getSum("flGer", flAttr, id, from, till));
                    feature.put("brGewMin", getMinMax("brGew", flAttr, id, from, till, true));
                    feature.put("brGewMit", getMit("brGew", flAttr, id, from, till));
                    feature.put("brGewMax", getMinMax("brGew", flAttr, id, from, till, false));
                    feature.put("flGew", getSum("flGew", flAttr, id, from, till));
                    feature.put("blNassReMin", getMinMax("blNRe", flAttr, id, from, till, true));
                    feature.put("blNassReMit", getMit("blNRe", flAttr, id, from, till));
                    feature.put("blNassReMax", getMinMax("blNRe", flAttr, id, from, till, false));
                    feature.put("blTroReMin", getMinMax("blTRe", flAttr, id, from, till, true));
                    feature.put("blTroReMit", getMit("blTRe", flAttr, id, from, till));
                    feature.put("blTroReMax", getMinMax("blTRe", flAttr, id, from, till, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", flAttr, id, from, till, true));
                    feature.put("blNassLiMit", getMit("blNLi", flAttr, id, from, till));
                    feature.put("blNassLiMax", getMinMax("blNLi", flAttr, id, from, till, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", flAttr, id, from, till, true));
                    feature.put("blTroLiMit", getMit("blTLi", flAttr, id, from, till));
                    feature.put("blTroLiMax", getMinMax("blTLi", flAttr, id, from, till, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", flAttr, id, from, till, true));
                    feature.put("flQsGerMit", getMit("flQsGer", flAttr, id, from, till));
                    feature.put("flQsGerMax", getMinMax("flQsGer", flAttr, id, from, till, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", flAttr, id, from, till, true));
                    feature.put("flQsGewMit", getMit("flQsGew", flAttr, id, from, till));
                    feature.put("flQsGewMax", getMinMax("flQsGew", flAttr, id, from, till, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", flAttr, id, from, till));
                    feature.put("flBoeTroRe", getSum("flBtRe", flAttr, id, from, till));
                    feature.put("flBoeNassLi", getSum("flBnLi", flAttr, id, from, till));
                    feature.put("flBoeTroLi", getSum("flBtLi", flAttr, id, from, till));
                    feature.put("flBoeNass", getSum("flBn", flAttr, id, from, till));
                    feature.put("flBoeTro", getSum("flBt", flAttr, id, from, till));
                    feature.put("flNass", getSum("flN", flAttr, id, from, till));
                    feature.put("summe", false);

                    final String newCode = getBaCd(flAttr, gew.getId());

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

        for (final Object flAttr : gemDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            if ((gemPartMap.get(flAttr) != null) && !gemPartMap.get(flAttr).isEmpty()) {
                sheetNames.add(String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                for (final GmdPartObjOffen gew : gemPartMap.get(flAttr)) { // use the complete river part
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final int id = gew.getId();
                    final double from = gew.getBaStVon();
                    final double till = gew.getBaStBis();
                    final double offenLength = helper.getLengthOffeneAbschn(flAttr, id, from, till);
                    final double prof = getLengthGew(flAttr, id, from, till);
                    final double mw = getLengthMw(flAttr, id, from, till);
                    final double gewAll = helper.getLengthGewAll(flAttr, id);

                    feature.put("group", String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                    feature.put("name", getGewName(flAttr, id));
                    feature.put("code", getBaCd(flAttr, id));
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
                    feature.put("profTrap_a", getCountProf("tr", flAttr, id, from, till));
                    feature.put("profTrap_l", getLengthProf("tr", flAttr, id, from, till));
                    feature.put("profRe_a", getCountProf("re", flAttr, id, from, till));
                    feature.put("profRe_l", getLengthProf("re", flAttr, id, from, till));
                    feature.put("brSohleMin", getMinMax("brSo", flAttr, id, from, till, true));
                    feature.put("brSohleMit", getMit("brSo", flAttr, id, from, till));
                    feature.put("brSohleMax", getMinMax("brSo", flAttr, id, from, till, false));
                    feature.put("bvReMin", getMinMax("bvRe", flAttr, id, from, till, true));
                    feature.put("bvReMit", getMit("bvRe", flAttr, id, from, till));
                    feature.put("bvReMax", getMinMax("bvRe", flAttr, id, from, till, false));
                    feature.put("bhReMin", getMinMax("bhRe", flAttr, id, from, till, true));
                    feature.put("bhReMit", getMit("bhRe", flAttr, id, from, till));
                    feature.put("bhReMax", getMinMax("bhRe", flAttr, id, from, till, false));
                    feature.put("blReMin", getMinMax("blRe", flAttr, id, from, till, true));
                    feature.put("blReMit", getMit("blRe", flAttr, id, from, till));
                    feature.put("blReMax", getMinMax("blRe", flAttr, id, from, till, false));
                    feature.put("bvLiMin", getMinMax("bvLi", flAttr, id, from, till, true));
                    feature.put("bvLiMit", getMit("bvLi", flAttr, id, from, till));
                    feature.put("bvLiMax", getMinMax("bvLi", flAttr, id, from, till, false));
                    feature.put("bhLiMin", getMinMax("bhLi", flAttr, id, from, till, true));
                    feature.put("bhLiMit", getMit("bhLi", flAttr, id, from, till));
                    feature.put("bhLiMax", getMinMax("bhLi", flAttr, id, from, till, false));
                    feature.put("blLiMin", getMinMax("blLi", flAttr, id, from, till, true));
                    feature.put("blLiMit", getMit("blLi", flAttr, id, from, till));
                    feature.put("blLiMax", getMinMax("blLi", flAttr, id, from, till, false));
                    feature.put("flSohle", getSum("flSo", flAttr, id, from, till));
                    feature.put("flBoeRe", getSum("flBRe", flAttr, id, from, till));
                    feature.put("flBoeLi", getSum("flBLi", flAttr, id, from, till));
                    feature.put("flBoe", getSum("flB", flAttr, id, from, till));
                    feature.put("flGer", getSum("flGer", flAttr, id, from, till));
                    feature.put("brGewMin", getMinMax("brGew", flAttr, id, from, till, true));
                    feature.put("brGewMit", getMit("brGew", flAttr, id, from, till));
                    feature.put("brGewMax", getMinMax("brGew", flAttr, id, from, till, false));
                    feature.put("flGew", getSum("flGew", flAttr, id, from, till));
                    feature.put("blNassReMin", getMinMax("blNRe", flAttr, id, from, till, true));
                    feature.put("blNassReMit", getMit("blNRe", flAttr, id, from, till));
                    feature.put("blNassReMax", getMinMax("blNRe", flAttr, id, from, till, false));
                    feature.put("blTroReMin", getMinMax("blTRe", flAttr, id, from, till, true));
                    feature.put("blTroReMit", getMit("blTRe", flAttr, id, from, till));
                    feature.put("blTroReMax", getMinMax("blTRe", flAttr, id, from, till, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", flAttr, id, from, till, true));
                    feature.put("blNassLiMit", getMit("blNLi", flAttr, id, from, till));
                    feature.put("blNassLiMax", getMinMax("blNLi", flAttr, id, from, till, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", flAttr, id, from, till, true));
                    feature.put("blTroLiMit", getMit("blTLi", flAttr, id, from, till));
                    feature.put("blTroLiMax", getMinMax("blTLi", flAttr, id, from, till, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", flAttr, id, from, till, true));
                    feature.put("flQsGerMit", getMit("flQsGer", flAttr, id, from, till));
                    feature.put("flQsGerMax", getMinMax("flQsGer", flAttr, id, from, till, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", flAttr, id, from, till, true));
                    feature.put("flQsGewMit", getMit("flQsGew", flAttr, id, from, till));
                    feature.put("flQsGewMax", getMinMax("flQsGew", flAttr, id, from, till, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", flAttr, id, from, till));
                    feature.put("flBoeTroRe", getSum("flBtRe", flAttr, id, from, till));
                    feature.put("flBoeNassLi", getSum("flBnLi", flAttr, id, from, till));
                    feature.put("flBoeTroLi", getSum("flBtLi", flAttr, id, from, till));
                    feature.put("flBoeNass", getSum("flBn", flAttr, id, from, till));
                    feature.put("flBoeTro", getSum("flBt", flAttr, id, from, till));
                    feature.put("flNass", getSum("flN", flAttr, id, from, till));
                    feature.put("summe", false);

                    final String newCode = getBaCd(flAttr, gew.getId());

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

        for (final Object flAttr : gemDataMap.keySet()) {
            if ((getGu(flAttr) != null) && !getGu(flAttr).isEmpty()) {
                sheetNames.add("GU " + String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
                for (final String guName : getGu(flAttr)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    final double offenLength = helper.getLengthOffeneAbschn(flAttr, guName);
                    final double prof = getLengthGew(flAttr, guName);
                    final double mw = getLengthMw(flAttr, guName);
                    final double gewAll = helper.getLengthGewAll(flAttr, guName);

                    feature.put("group", String.valueOf(flAttr));
                    feature.put("name", guName);
                    feature.put("gu", getGuId(flAttr, guName));
                    feature.put("gew_a", helper.getCountGewAll(flAttr, guName));
                    feature.put("gew_l", helper.getLengthGewAll(flAttr, guName));
                    feature.put("offene_l", offenLength);
                    feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                    feature.put("prof_l", prof);
                    feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : 100));
                    feature.put("mw_l", mw);
                    feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                    feature.put("profTrap_a", getCountProf("tr", flAttr, guName));
                    feature.put("profTrap_l", getLengthProf("tr", flAttr, guName));
                    feature.put("profRe_a", getCountProf("re", flAttr, guName));
                    feature.put("profRe_l", getLengthProf("re", flAttr, guName));
                    feature.put("brSohleMin", getMinMax("brSo", flAttr, guName, true));
                    feature.put("brSohleMit", getMit("brSo", flAttr, guName));
                    feature.put("brSohleMax", getMinMax("brSo", flAttr, guName, false));
                    feature.put("bvReMin", getMinMax("bvRe", flAttr, guName, true));
                    feature.put("bvReMit", getMit("bvRe", flAttr, guName));
                    feature.put("bvReMax", getMinMax("bvRe", flAttr, guName, false));
                    feature.put("bhReMin", getMinMax("bhRe", flAttr, guName, true));
                    feature.put("bhReMit", getMit("bhRe", flAttr, guName));
                    feature.put("bhReMax", getMinMax("bhRe", flAttr, guName, false));
                    feature.put("blReMin", getMinMax("blRe", flAttr, guName, true));
                    feature.put("blReMit", getMit("blRe", flAttr, guName));
                    feature.put("blReMax", getMinMax("blRe", flAttr, guName, false));
                    feature.put("bvLiMin", getMinMax("bvLi", flAttr, guName, true));
                    feature.put("bvLiMit", getMit("bvLi", flAttr, guName));
                    feature.put("bvLiMax", getMinMax("bvLi", flAttr, guName, false));
                    feature.put("bhLiMin", getMinMax("bhLi", flAttr, guName, true));
                    feature.put("bhLiMit", getMit("bhLi", flAttr, guName));
                    feature.put("bhLiMax", getMinMax("bhLi", flAttr, guName, false));
                    feature.put("blLiMin", getMinMax("blLi", flAttr, guName, true));
                    feature.put("blLiMit", getMit("blLi", flAttr, guName));
                    feature.put("blLiMax", getMinMax("blLi", flAttr, guName, false));
                    feature.put("flSohle", getSum("flSo", flAttr, guName));
                    feature.put("flBoeRe", getSum("flBRe", flAttr, guName));
                    feature.put("flBoeLi", getSum("flBLi", flAttr, guName));
                    feature.put("flBoe", getSum("flB", flAttr, guName));
                    feature.put("flGer", getSum("flGer", flAttr, guName));
                    feature.put("brGewMin", getMinMax("brGew", flAttr, guName, true));
                    feature.put("brGewMit", getMit("brGew", flAttr, guName));
                    feature.put("brGewMax", getMinMax("brGew", flAttr, guName, false));
                    feature.put("flGew", getSum("flGew", flAttr, guName));
                    feature.put("blNassReMin", getMinMax("blNRe", flAttr, guName, true));
                    feature.put("blNassReMit", getMit("blNRe", flAttr, guName));
                    feature.put("blNassReMax", getMinMax("blNRe", flAttr, guName, false));
                    feature.put("blTroReMin", getMinMax("blTRe", flAttr, guName, true));
                    feature.put("blTroReMit", getMit("blTRe", flAttr, guName));
                    feature.put("blTroReMax", getMinMax("blTRe", flAttr, guName, false));
                    feature.put("blNassLiMin", getMinMax("blNLi", flAttr, guName, true));
                    feature.put("blNassLiMit", getMit("blNLi", flAttr, guName));
                    feature.put("blNassLiMax", getMinMax("blNLi", flAttr, guName, false));
                    feature.put("blTroLiMin", getMinMax("blTLi", flAttr, guName, true));
                    feature.put("blTroLiMit", getMit("blTLi", flAttr, guName));
                    feature.put("blTroLiMax", getMinMax("blTLi", flAttr, guName, false));
                    feature.put("flQsGerMin", getMinMax("flQsGer", flAttr, guName, true));
                    feature.put("flQsGerMit", getMit("flQsGer", flAttr, guName));
                    feature.put("flQsGerMax", getMinMax("flQsGer", flAttr, guName, false));
                    feature.put("flQsGewMin", getMinMax("flQsGew", flAttr, guName, true));
                    feature.put("flQsGewMit", getMit("flQsGew", flAttr, guName));
                    feature.put("flQsGewMax", getMinMax("flQsGew", flAttr, guName, false));
                    feature.put("flBoeNassRe", getSum("flBnRe", flAttr, guName));
                    feature.put("flBoeTroRe", getSum("flBtRe", flAttr, guName));
                    feature.put("flBoeNassLi", getSum("flBnLi", flAttr, guName));
                    feature.put("flBoeTroLi", getSum("flBtLi", flAttr, guName));
                    feature.put("flBoeNass", getSum("flBn", flAttr, guName));
                    feature.put("flBoeTro", getSum("flBt", flAttr, guName));
                    feature.put("flNass", getSum("flN", flAttr, guName));
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

        for (final Object flAttr : gemDataMap.keySet()) {
            if ((getGu(flAttr) != null) && !getGu(flAttr).isEmpty()) {
                sheetNames.add("GU " + String.valueOf(gemDataMap.get(flAttr).getAttr2()));
                final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

                for (final String guName : getGu(flAttr)) {
                    final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

                    for (final Integer wdm : getWidmung(flAttr, guName)) {
                        final Map<String, Object> feature = new HashMap<String, Object>();
                        final double offenLength = helper.getLengthOffeneAbschn(flAttr, guName, wdm);
                        final double prof = getLengthGew(flAttr, guName, wdm);
                        final double mw = getLengthMw(flAttr, guName, wdm);
                        final double gewAll = helper.getLengthGewAll(flAttr, guName, wdm);

                        feature.put("group", String.valueOf(flAttr));
                        feature.put("name", getGuId(flAttr, guName));
                        feature.put("gu", guName);
                        feature.put("wdm", wdm);
                        feature.put("gew_a", helper.getCountGewAll(flAttr, guName, wdm));
                        feature.put("gew_l", helper.getLengthGewAll(flAttr, guName, wdm));
                        feature.put("offene_l", offenLength);
                        feature.put("offene_a", ((gewAll != 0) ? (offenLength * 100 / gewAll) : 100));
                        feature.put("prof_l", prof);
                        feature.put("prof_a", ((offenLength != 0) ? (prof * 100 / offenLength) : offenLength));
                        feature.put("mw_l", mw);
                        feature.put("mw_a", ((prof != 0) ? (mw * 100 / prof) : 100));
                        feature.put("profTrap_a", getCountProf("tr", flAttr, guName, wdm));
                        feature.put("profTrap_l", getLengthProf("tr", flAttr, guName, wdm));
                        feature.put("profRe_a", getCountProf("re", flAttr, guName, wdm));
                        feature.put("profRe_l", getLengthProf("re", flAttr, guName, wdm));
                        feature.put("brSohleMin", getMinMax("brSo", flAttr, guName, wdm, true));
                        feature.put("brSohleMit", getMit("brSo", flAttr, guName, wdm));
                        feature.put("brSohleMax", getMinMax("brSo", flAttr, guName, wdm, false));
                        feature.put("bvReMin", getMinMax("bvRe", flAttr, guName, wdm, true));
                        feature.put("bvReMit", getMit("bvRe", flAttr, guName, wdm));
                        feature.put("bvReMax", getMinMax("bvRe", flAttr, guName, wdm, false));
                        feature.put("bhReMin", getMinMax("bhRe", flAttr, guName, wdm, true));
                        feature.put("bhReMit", getMit("bhRe", flAttr, guName, wdm));
                        feature.put("bhReMax", getMinMax("bhRe", flAttr, guName, wdm, false));
                        feature.put("blReMin", getMinMax("blRe", flAttr, guName, wdm, true));
                        feature.put("blReMit", getMit("blRe", flAttr, guName, wdm));
                        feature.put("blReMax", getMinMax("blRe", flAttr, guName, wdm, false));
                        feature.put("bvLiMin", getMinMax("bvLi", flAttr, guName, wdm, true));
                        feature.put("bvLiMit", getMit("bvLi", flAttr, guName, wdm));
                        feature.put("bvLiMax", getMinMax("bvLi", flAttr, guName, wdm, false));
                        feature.put("bhLiMin", getMinMax("bhLi", flAttr, guName, wdm, true));
                        feature.put("bhLiMit", getMit("bhLi", flAttr, guName, wdm));
                        feature.put("bhLiMax", getMinMax("bhLi", flAttr, guName, wdm, false));
                        feature.put("blLiMin", getMinMax("blLi", flAttr, guName, wdm, true));
                        feature.put("blLiMit", getMit("blLi", flAttr, guName, wdm));
                        feature.put("blLiMax", getMinMax("blLi", flAttr, guName, wdm, false));
                        feature.put("flSohle", getSum("flSo", flAttr, guName, wdm));
                        feature.put("flBoeRe", getSum("flBRe", flAttr, guName, wdm));
                        feature.put("flBoeLi", getSum("flBLi", flAttr, guName, wdm));
                        feature.put("flBoe", getSum("flB", flAttr, guName, wdm));
                        feature.put("flGer", getSum("flGer", flAttr, guName, wdm));
                        feature.put("brGewMin", getMinMax("brGew", flAttr, guName, wdm, true));
                        feature.put("brGewMit", getMit("brGew", flAttr, guName, wdm));
                        feature.put("brGewMax", getMinMax("brGew", flAttr, guName, wdm, false));
                        feature.put("flGew", getSum("flGew", flAttr, guName, wdm));
                        feature.put("blNassReMin", getMinMax("blNRe", flAttr, guName, wdm, true));
                        feature.put("blNassReMit", getMit("blNRe", flAttr, guName, wdm));
                        feature.put("blNassReMax", getMinMax("blNRe", flAttr, guName, wdm, false));
                        feature.put("blTroReMin", getMinMax("blTRe", flAttr, guName, wdm, true));
                        feature.put("blTroReMit", getMit("blTRe", flAttr, guName, wdm));
                        feature.put("blTroReMax", getMinMax("blTRe", flAttr, guName, wdm, false));
                        feature.put("blNassLiMin", getMinMax("blNLi", flAttr, guName, wdm, true));
                        feature.put("blNassLiMit", getMit("blNLi", flAttr, guName, wdm));
                        feature.put("blNassLiMax", getMinMax("blNLi", flAttr, guName, wdm, false));
                        feature.put("blTroLiMin", getMinMax("blTLi", flAttr, guName, wdm, true));
                        feature.put("blTroLiMit", getMit("blTLi", flAttr, guName, wdm));
                        feature.put("blTroLiMax", getMinMax("blTLi", flAttr, guName, wdm, false));
                        feature.put("flQsGerMin", getMinMax("flQsGer", flAttr, guName, wdm, true));
                        feature.put("flQsGerMit", getMit("flQsGer", flAttr, guName, wdm));
                        feature.put("flQsGerMax", getMinMax("flQsGer", flAttr, guName, wdm, false));
                        feature.put("flQsGewMin", getMinMax("flQsGew", flAttr, guName, wdm, true));
                        feature.put("flQsGewMit", getMit("flQsGew", flAttr, guName, wdm));
                        feature.put("flQsGewMax", getMinMax("flQsGew", flAttr, guName, wdm, false));
                        feature.put("flBoeNassRe", getSum("flBnRe", flAttr, guName, wdm));
                        feature.put("flBoeTroRe", getSum("flBtRe", flAttr, guName, wdm));
                        feature.put("flBoeNassLi", getSum("flBnLi", flAttr, guName, wdm));
                        feature.put("flBoeTroLi", getSum("flBtLi", flAttr, guName, wdm));
                        feature.put("flBoeNass", getSum("flBn", flAttr, guName, wdm));
                        feature.put("flBoeTro", getSum("flBt", flAttr, guName, wdm));
                        feature.put("flNass", getSum("flN", flAttr, guName, wdm));
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
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final Object flNr) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObjOffen tmp : gemList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getGu(final Object flNr) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObjOffen tmp : gemList) {
            ts.add(tmp.getOwner());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGuId(final Object flNr, final String owner) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);

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
     * @param   flNr    DOCUMENT ME!
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final Object flNr, final String guName) {
        final List<GmdPartObjOffen> gmdList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     * @param   gew   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final Object flNr, final int gew) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);

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
     * @param   flNr  DOCUMENT ME!
     * @param   gew   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGewName(final Object flNr, final int gew) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);

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
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object flNr) {
        return getLengthGew(flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object flNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Object flNr) {
        return getLengthMw(flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     * @param   gu    gewId DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     * @param   gu    gewId DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Object flNr, final String gu, final Integer wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthMw(final Object flNr, final int gewId, final double from, final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final Object flNr) {
        return getLengthProf(prof, flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  gewId DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  gewId DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof, final Object flNr, final String gu, final Integer wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthProf(final String prof,
            final Object flNr,
            final int gewId,
            final double from,
            final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final Object flNr, final boolean min) {
        return getMinMax(field, flNr, -1, min);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final Object flNr, final int gewId, final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
            if (((gewId == -1) || (tmp.getId() == gewId))) {
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
     * @param   field  gewId DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field, final Object flNr, final String gu, final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  gewId DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field,
            final Object flNr,
            final String gu,
            final Integer wdm,
            final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     * @param   min    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMinMax(final String field,
            final Object flNr,
            final int gewId,
            final double from,
            final double till,
            final boolean min) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
        double currentVal = 0;
        final boolean firstValue = true;

        for (final GmdPartObjOffen tmp : gemList) {
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
     * @param   field  DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final Object flNr) {
        return getMit(field, flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field, final Object flNr, final String gu, final Integer wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getMit(final String field,
            final Object flNr,
            final int gewId,
            final double from,
            final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final Object flNr) {
        return getSum(field, flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   field  DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field,
            final Object flNr,
            final int gewId,
            final double from,
            final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getSum(final String field, final Object flNr, final String gu, final Integer wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final Object flNr) {
        return getCountProf(prof, flNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   prof   DOCUMENT ME!
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final Object flNr, final int gewId) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   prof  DOCUMENT ME!
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof, final Object flNr, final String gu, final Integer wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr   DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCountProf(final String prof,
            final Object flNr,
            final int gewId,
            final double from,
            final double till) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object flNr, final String gu) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
     * @param   flNr  DOCUMENT ME!
     * @param   gu    DOCUMENT ME!
     * @param   wdm   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object flNr, final String gu, final int wdm) {
        final List<GmdPartObjOffen> gemList = gemPartMap.get(flNr);
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
