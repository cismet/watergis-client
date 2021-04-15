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

import de.cismet.cids.custom.watergis.server.search.AllGewBySb;
import de.cismet.cids.custom.watergis.server.search.AllLineObjects;
import de.cismet.cids.custom.watergis.server.search.AllPunktObjects;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.KatasterSbReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GewaesserData;
import de.cismet.watergis.reports.types.SbObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterSbReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterSbReport.class);
    private static final String[] exceptionalNumberFields = {
            "gmdNummer",
            "group",
            "gmdName",
            "code",
            "anzahlGu",
            "gu"
        };
// private final Map<String, CidsLayer> layerMap = new HashMap<String, CidsLayer>();

    static {
        Arrays.sort(exceptionalNumberFields);
    }

    //~ Instance fields --------------------------------------------------------

    GewaesserData gd;

    private List<SbObj> parts;
    private List<String> sheetNames = new ArrayList<String>();

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
        parameters.put("gemeinden", 0);
        parameters.put("wasserschutz", KatasterSbReportDialog.getInstance().isWsg());
        parameters.put("ueber", KatasterSbReportDialog.getInstance().isSchutzgebiete());
        parameters.put("ben", KatasterSbReportDialog.getInstance().isBen());
        parameters.put("aus", KatasterSbReportDialog.getInstance().isAus());
        parameters.put("pegel", KatasterSbReportDialog.getInstance().isPegel());
        parameters.put("gb", KatasterSbReportDialog.getInstance().isGb());
        parameters.put("sb", KatasterSbReportDialog.getInstance().isSb());
        parameters.put("gmd", KatasterSbReportDialog.getInstance().isGmd());
        parameters.put("prof", KatasterSbReportDialog.getInstance().isProf());
        parameters.put("sbef", KatasterSbReportDialog.getInstance().isSbef());
        parameters.put("ubef", KatasterSbReportDialog.getInstance().isUbef());
        parameters.put("bbef", KatasterSbReportDialog.getInstance().isBbef());
        parameters.put("rl", KatasterSbReportDialog.getInstance().isRl());
        parameters.put("d", KatasterSbReportDialog.getInstance().isD());
        parameters.put("due", KatasterSbReportDialog.getInstance().isDue());
        parameters.put("scha", KatasterSbReportDialog.getInstance().isScha());
        parameters.put("wehr", KatasterSbReportDialog.getInstance().isWehr());
        parameters.put("schw", KatasterSbReportDialog.getInstance().isSchw());
        parameters.put("foto", KatasterSbReportDialog.getInstance().isFoto());
        parameters.put("anlp", KatasterSbReportDialog.getInstance().isAnlp());
        parameters.put("anll", KatasterSbReportDialog.getInstance().isAnll());
        parameters.put("kr", KatasterSbReportDialog.getInstance().isKr());
        parameters.put("ea", KatasterSbReportDialog.getInstance().isEa());
        parameters.put("deich", KatasterSbReportDialog.getInstance().isDeich());
        parameters.put("ughz", KatasterSbReportDialog.getInstance().isUghz());
        parameters.put("leis", KatasterSbReportDialog.getInstance().isLeis());
        parameters.put("tech", KatasterSbReportDialog.getInstance().isTech());
        parameters.put("dok", KatasterSbReportDialog.getInstance().isDok());
        parameters.put("proj", KatasterSbReportDialog.getInstance().isProj());
        parameters.put("perGew", KatasterSbReportDialog.getInstance().isPerGew());
        parameters.put("perAbschn", false);
        parameters.put("sumGu", false);
        parameters.put("wdm", false);
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(KatasterSbReport.class.getResourceAsStream(
                    "/de/cismet/watergis/reports/sb_gewaesser.jasper"));

        init(gew);

        dataSources.put("schaubezirke", getGewaesser());

        if (KatasterSbReportDialog.getInstance().isPerGew()) {
            dataSources.put("gewaesser", getGewaesserAbschnitt());
        }

        // create print from report and data
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        // set orientation
// jasperPrint.setOrientation(jasperReport.getOrientationValue());

//        final FileOutputStream pfout = new FileOutputStream(new File("/home/therter/tmp/schaubezirke.pdf"));
//        final BufferedOutputStream pout = new BufferedOutputStream(pfout);
//        JasperExportManager.exportReportToPdfStream(jasperPrint, pout);
//        pout.close();

        final File file = new File(
                KatasterSbReportDialog.getInstance().getPath()
                        + "/Kataster_Schaubezirke.xlsx");
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
//        config.setIgnoreCellBorder(true)
        config.setRemoveEmptySpaceBetweenColumns(true);
        config.setRemoveEmptySpaceBetweenRows(true);
        config.setCellHidden(true);
        config.setDetectCellType(true);
        exporter.setConfiguration(config);
        exporter.exportReport();
//        final SimpleXlsxExporterConfiguration config = new SimpleXlsxExporterConfiguration();
//        config.setOverrideHints(Boolean.TRUE);
//        exporter.setConfiguration(config);

//                .exportReportToStream(jasperPrint, out);
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
        final KatasterSbReport report = new KatasterSbReport();
        try {
            report.createGewaesserReport(new int[] { 2 });
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
        parts = getAllRoutes(routeIds);
        int[] routes = routeIds;

        if (routes.length == 0) {
            routes = null;
        }

        gd = new GewaesserData(routes);
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
    private List<SbObj> getAllRoutes(final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewBySb(routeIds, getAllowedWdms());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<SbObj> objList = new ArrayList<SbObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new SbObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(7),
                        (String)f.get(8),
                        (String)f.get(9),
                        (Integer)f.get(10),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        (String)f.get(5),
                        (String)f.get(6),
                        (Double)f.get(11),
                        (String)f.get(12)));
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

        if (KatasterSbReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (KatasterSbReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (KatasterSbReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (KatasterSbReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (KatasterSbReportDialog.getInstance().is1505()) {
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

        sheetNames.add("Schaubezirke");
        for (final String guName : getGu()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            for (final Integer wdm : getWidmung(guName)) {
                final List<Map<String, Object>> featureListKumWdm = new ArrayList<Map<String, Object>>();
                for (final String sb : getSb(guName, wdm)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    feature.put("anzahlSb", getSb(guName, wdm).size());
                    feature.put("gu", getGuId(guName));
                    feature.put("guName", guName);
                    feature.put("group", guName);
                    feature.put("widmung", wdm);
                    feature.put("sb", sb);
                    feature.put("sbName", getSbName(sb));
                    feature.put("gewAnz", getCountGew(guName, wdm, sb));
                    feature.put("gewLaenge", getLengthGew(guName, wdm, sb));
                    feature.put("gew_a", getCountGew(guName, wdm, sb));
                    feature.put("gewLaenge", getLengthGew(guName, wdm, sb));
                    feature.put("offene_a", getCountOffeneAbschn(guName, wdm, sb));
                    feature.put("offene_l", getLengthOffeneAbschn(guName, wdm, sb));
                    feature.put(
                        "see_a",
                        getCountLineObjects(GewaesserData.LineFromPolygonTable.sg_see, guName, wdm, sb));
                    feature.put(
                        "see_l",
                        getLengthLineObjects(GewaesserData.LineFromPolygonTable.sg_see, guName, wdm, sb));
                    feature.put("geschl_a", getCountGeschlAbschn(guName, wdm, sb));
                    feature.put("geschl_l", getLengthGeschlAbschn(guName, wdm, sb));
                    feature.put(
                        "wschutz_a",
                        getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, guName, wdm, sb));
                    feature.put(
                        "wschutz_l",
                        getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, guName, wdm, sb));
                    feature.put(
                        "ueber_a",
                        getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, guName, wdm, sb));
                    feature.put(
                        "ueber_l",
                        getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, guName, wdm, sb));
                    feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, guName, wdm, sb));
                    feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, guName, wdm, sb));
                    feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, guName, wdm, sb));
                    feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, guName, wdm, sb));
                    feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, guName, wdm, sb));
                    feature.put("gmd_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gmd, guName, wdm, sb));
                    feature.put("gmd_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gmd, guName, wdm, sb));
                    feature.put("prof_a", getCountLineObjects(AllLineObjects.Table.fg_ba_prof, guName, wdm, sb));
                    feature.put("prof_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, guName, wdm, sb));
                    feature.put("sbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, guName, wdm, sb));
                    feature.put("sbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, guName, wdm, sb));
                    feature.put("ubef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, guName, wdm, sb));
                    feature.put("ubef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, guName, wdm, sb));
                    feature.put("bbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, guName, wdm, sb));
                    feature.put("bbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, guName, wdm, sb));
                    feature.put("rl_a", getCountLineObjects(AllLineObjects.Table.fg_ba_rl, guName, wdm, sb));
                    feature.put("rl_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, guName, wdm, sb));
                    feature.put("d_a", getCountLineObjects(AllLineObjects.Table.fg_ba_d, guName, wdm, sb));
                    feature.put("d_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_d, guName, wdm, sb));
                    feature.put("due_a", getCountLineObjects(AllLineObjects.Table.fg_ba_due, guName, wdm, sb));
                    feature.put("due_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_due, guName, wdm, sb));
                    feature.put("scha_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, guName, wdm, sb));
                    feature.put("wehr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, guName, wdm, sb));
                    feature.put("schw_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, guName, wdm, sb));
                    feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, guName, wdm, sb));
                    feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, guName, wdm, sb));
                    feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, guName, wdm, sb));
                    feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, guName, wdm, sb));
                    feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, guName, wdm, sb));
                    feature.put("foto_a", getCountPointObjects(AllPunktObjects.Table.foto, guName, wdm, sb));
                    feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.fg_ba_deich, guName, wdm, sb));
                    feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_deich, guName, wdm, sb));
                    feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, guName, wdm, sb));
                    feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, guName, wdm, sb));
                    feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, guName, wdm, sb));
                    feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, guName, wdm, sb));
                    feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, guName, wdm, sb));
                    feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, guName, wdm, sb));
                    feature.put("dok_a", getCountLineObjects(AllLineObjects.Table.fg_ba_doku, guName, wdm, sb));
                    feature.put("dok_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_doku, guName, wdm, sb));
                    feature.put("proj_a", getCountLineObjects(AllLineObjects.Table.fg_ba_proj, guName, wdm, sb));
                    feature.put("proj_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_proj, guName, wdm, sb));

                    features.add(feature);
                    featureListKum.add(feature);
                    featureListKumWdm.add(feature);
                }
                features.add(createKumFeature(featureListKumWdm, true));
            }
            features.add(createKumFeature(featureListKum, false));
        }

        for (final Map<String, Object> f : features) {
            if ((f.get("summe") == null) || (f.get("zwischenSumme") == null) || !((Boolean)f.get("zwischenSumme"))) {
                f.remove("guName");
                f.remove("gu");
                f.remove("widmung");
            }
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
    private FeatureDataSource getGewaesserAbschnitt() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final String sb : getSb()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final String sheetName = String.valueOf(getGuIdBySb(sb)) + "-" + String.valueOf(getWidmungBySb(sb)) + "-"
                        + String.valueOf(sb);
            sheetNames.add(sheetName);
            for (final Integer gew : getGew(sb)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("anzahlGew", getCountGewBySb(sb));
                feature.put("code", getBaCd(gew));
                feature.put("widmung", getWdm(gew));
                feature.put("gu", getGuIdBySb(sb));
                feature.put("group", sheetName);
                feature.put("guName", getOwner(gew));
                feature.put("sb", sb);
                feature.put("sbName", getSbName(sb));
                feature.put("gewName", getGewName(gew));
                feature.put("von", convertStation(startGew(sb, gew)));
                feature.put("bis", convertStation(endGew(sb, gew)));
                final double gewAll = getLengthGew(gew);

                feature.put("teil", ((Math.abs(gewAll - (endGew(sb, gew) - startGew(sb, gew))) < 0.01) ? null : "x"));
                feature.put("gewLaenge", getLengthGew(gew, sb));
                feature.put("offene_a", getCountOffeneAbschnBySb(sb, gew));
                feature.put("offene_l", getLengthOffeneAbschnBySb(sb, gew));
                feature.put(
                    "see_a",
                    getCountLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.sg_see,
                        sb,
                        gew));
                feature.put(
                    "see_l",
                    getLengthLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.sg_see,
                        sb,
                        gew));
                feature.put("geschl_a", getCountGeschlAbschnBySb(sb, gew));
                feature.put("geschl_l", getLengthGeschlAbschnBySb(sb, gew));
                feature.put(
                    "wschutz_a",
                    getCountLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                        sb,
                        gew));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                        sb,
                        gew));
                feature.put(
                    "ueber_a",
                    getCountLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                        sb,
                        gew));
                feature.put(
                    "ueber_l",
                    getLengthLineObjectsBySb(
                        GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                        sb,
                        gew));
                feature.put(
                    "ben_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.wr_wbu_ben,
                        sb,
                        gew));
                feature.put(
                    "aus_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.wr_wbu_aus,
                        sb,
                        gew));
                feature.put(
                    "pegel_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.mn_ow_pegel,
                        sb,
                        gew));
                feature.put(
                    "gb_a",
                    getCountLineObjectsBySb(AllLineObjects.Table.fg_ba_gb, sb, gew));
                feature.put(
                    "gb_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_gb,
                        sb,
                        gew));
                feature.put(
                    "gmd_a",
                    getCountLineObjectsBySb(AllLineObjects.Table.fg_ba_gmd, sb, gew));
                feature.put(
                    "gmd_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_gmd,
                        sb,
                        gew));
                feature.put(
                    "prof_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_prof,
                        sb,
                        gew));
                feature.put(
                    "prof_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_prof,
                        sb,
                        gew));
                feature.put(
                    "sbef_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_sbef,
                        sb,
                        gew));
                feature.put(
                    "sbef_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_sbef,
                        sb,
                        gew));
                feature.put(
                    "ubef_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_ubef,
                        sb,
                        gew));
                feature.put(
                    "ubef_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_ubef,
                        sb,
                        gew));
                feature.put(
                    "bbef_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_bbef,
                        sb,
                        gew));
                feature.put(
                    "bbef_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_bbef,
                        sb,
                        gew));
                feature.put(
                    "rl_a",
                    getCountLineObjectsBySb(AllLineObjects.Table.fg_ba_rl, sb, gew));
                feature.put(
                    "rl_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_rl,
                        sb,
                        gew));
                feature.put(
                    "d_a",
                    getCountLineObjectsBySb(AllLineObjects.Table.fg_ba_d, sb, gew));
                feature.put(
                    "d_l",
                    getLengthLineObjectsBySb(AllLineObjects.Table.fg_ba_d, sb, gew));
                feature.put(
                    "due_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_due,
                        sb,
                        gew));
                feature.put(
                    "due_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_due,
                        sb,
                        gew));
                feature.put(
                    "scha_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_scha,
                        sb,
                        gew));
                feature.put(
                    "wehr_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_wehr,
                        sb,
                        gew));
                feature.put(
                    "schw_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_schw,
                        sb,
                        gew));
                feature.put(
                    "anlp_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_anlp,
                        sb,
                        gew));
                feature.put(
                    "anll_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_anll,
                        sb,
                        gew));
                feature.put(
                    "anll_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_anll,
                        sb,
                        gew));
                feature.put(
                    "kr_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_kr,
                        sb,
                        gew));
                feature.put(
                    "ea_a",
                    getCountPointObjectsBySb(
                        AllPunktObjects.Table.fg_ba_ea,
                        sb,
                        gew));
                feature.put(
                    "deich_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_deich,
                        sb,
                        gew));
                feature.put(
                    "deich_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_deich,
                        sb,
                        gew));
                feature.put(
                    "ughz_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_ughz,
                        sb,
                        gew));
                feature.put(
                    "ughz_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_ughz,
                        sb,
                        gew));
                feature.put(
                    "leis_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_leis,
                        sb,
                        gew));
                feature.put(
                    "leis_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_leis,
                        sb,
                        gew));
                feature.put(
                    "tech_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_tech,
                        sb,
                        gew));
                feature.put(
                    "tech_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_tech,
                        sb,
                        gew));
                feature.put(
                    "dok_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_doku,
                        sb,
                        gew));
                feature.put(
                    "dok_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_doku,
                        sb,
                        gew));
                feature.put(
                    "proj_a",
                    getCountLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_proj,
                        sb,
                        gew));
                feature.put(
                    "proj_l",
                    getLengthLineObjectsBySb(
                        AllLineObjects.Table.fg_ba_proj,
                        sb,
                        gew));

                features.add(feature);
                featureListKum.add(feature);
            }
            final Map<String, Object> l = createKumFeature(featureListKum, false);
            l.put("code", null);
            l.put("gewName", null);
            l.put("von", null);
            l.put("bis", null);
            features.add(l);
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
    private FeatureDataSource getGuTable() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        sheetNames.add("GU");
        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
        for (final String guName : getGu()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            feature.put("anzahlGu", getCountGu());
            feature.put("group", 1);
            feature.put("gu", getGuId(guName));
            feature.put("guName", guName);
            feature.put("gewAnzahl", getCountGew(guName));
            feature.put("gewLaenge", getLengthGew(guName));
            feature.put("offene_a", getCountOffeneAbschn(guName));
            feature.put("offene_l", getLengthOffeneAbschn(guName));
            feature.put("see_a", getCountLineObjects(GewaesserData.LineFromPolygonTable.sg_see, guName));
            feature.put("see_l", getLengthLineObjects(GewaesserData.LineFromPolygonTable.sg_see, guName));
            feature.put("geschl_a", getCountGeschlAbschn(guName));
            feature.put("geschl_l", getLengthGeschlAbschn(guName));
            feature.put(
                "wschutz_a",
                getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, guName));
            feature.put(
                "wschutz_l",
                getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, guName));
            feature.put("ueber_a", getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, guName));
            feature.put(
                "ueber_l",
                getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, guName));
            feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, guName));
            feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, guName));
            feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, guName));
            feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, guName));
            feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, guName));
            feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, guName));
            feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, guName));
            feature.put("prof_a", getCountLineObjects(AllLineObjects.Table.fg_ba_prof, guName));
            feature.put("prof_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, guName));
            feature.put("sbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, guName));
            feature.put("sbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, guName));
            feature.put("ubef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, guName));
            feature.put("ubef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, guName));
            feature.put("bbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, guName));
            feature.put("bbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, guName));
            feature.put("rl_a", getCountLineObjects(AllLineObjects.Table.fg_ba_rl, guName));
            feature.put("rl_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, guName));
            feature.put("d_a", getCountLineObjects(AllLineObjects.Table.fg_ba_d, guName));
            feature.put("d_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_d, guName));
            feature.put("due_a", getCountLineObjects(AllLineObjects.Table.fg_ba_due, guName));
            feature.put("due_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_due, guName));
            feature.put("scha_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, guName));
            feature.put("wehr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, guName));
            feature.put("schw_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, guName));
            feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, guName));
            feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, guName));
            feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, guName));
            feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, guName));
            feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, guName));
            feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.fg_ba_deich, guName));
            feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_deich, guName));
            feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, guName));
            feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, guName));
            feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, guName));
            feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, guName));
            feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, guName));
            feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, guName));

            features.add(feature);
            featureListKum.add(feature);
        }
        features.add(createKumFeature(featureListKum, false));

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private FeatureDataSource getGuWidmung() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        sheetNames.add("GU");
        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

        for (final String guName : getGu()) {
            final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

            for (final Integer wdm : getWidmung(guName)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("anzahlGu", getCountGu());
                feature.put("gu", getGuId(guName));
                feature.put("guName", guName); // abhaengig vom Gewaesser. Nicht vom GU
                feature.put("widmung", wdm);
                feature.put("gew_a", getCountGew(guName, wdm));
                feature.put("gew_l", getLengthGew(guName, wdm));
                feature.put("offene_a", getCountOffeneAbschn(guName, wdm));
                feature.put("offene_l", getLengthOffeneAbschn(guName, wdm));
                feature.put(
                    "see_a",
                    getCountLineObjects(
                        GewaesserData.LineFromPolygonTable.sg_see,
                        guName,
                        wdm));
                feature.put(
                    "see_l",
                    getLengthLineObjects(
                        GewaesserData.LineFromPolygonTable.sg_see,
                        guName,
                        wdm));
                feature.put("geschl_a", getCountGeschlAbschn(guName, wdm));
                feature.put("geschl_l", getLengthGeschlAbschn(guName, wdm));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(
                        GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                        guName,
                        wdm));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(
                        GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                        guName,
                        wdm));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(
                        GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                        guName,
                        wdm));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(
                        GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                        guName,
                        wdm));
                feature.put(
                    "ben_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.wr_wbu_ben,
                        guName,
                        wdm));
                feature.put(
                    "aus_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.wr_wbu_aus,
                        guName,
                        wdm));
                feature.put(
                    "pegel_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.mn_ow_pegel,
                        guName,
                        wdm));
                feature.put(
                    "gb_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_gb,
                        guName,
                        wdm));
                feature.put(
                    "gb_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_gb,
                        guName,
                        wdm));
                feature.put(
                    "sb_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_sb,
                        guName,
                        wdm));
                feature.put(
                    "sb_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_sb,
                        guName,
                        wdm));
                feature.put(
                    "prof_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_prof,
                        guName,
                        wdm));
                feature.put(
                    "prof_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_prof,
                        guName,
                        wdm));
                feature.put(
                    "sbef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_sbef,
                        guName,
                        wdm));
                feature.put(
                    "sbef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_sbef,
                        guName,
                        wdm));
                feature.put(
                    "ubef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_ubef,
                        guName,
                        wdm));
                feature.put(
                    "ubef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_ubef,
                        guName,
                        wdm));
                feature.put(
                    "bbef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_bbef,
                        guName,
                        wdm));
                feature.put(
                    "bbef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_bbef,
                        guName,
                        wdm));
                feature.put(
                    "rl_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_rl,
                        guName,
                        wdm));
                feature.put(
                    "rl_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_rl,
                        guName,
                        wdm));
                feature.put(
                    "d_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_d,
                        guName,
                        wdm));
                feature.put(
                    "d_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_d,
                        guName,
                        wdm));
                feature.put(
                    "due_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_due,
                        guName,
                        wdm));
                feature.put(
                    "due_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_due,
                        guName,
                        wdm));
                feature.put(
                    "scha_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_scha,
                        guName,
                        wdm));
                feature.put(
                    "wehr_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_wehr,
                        guName,
                        wdm));
                feature.put(
                    "schw_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_schw,
                        guName,
                        wdm));
                feature.put(
                    "anlp_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_anlp,
                        guName,
                        wdm));
                feature.put(
                    "anll_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_anll,
                        guName,
                        wdm));
                feature.put(
                    "anll_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_anll,
                        guName,
                        wdm));
                feature.put(
                    "kr_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_kr,
                        guName,
                        wdm));
                feature.put(
                    "ea_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_ea,
                        guName,
                        wdm));
                feature.put(
                    "deich_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_deich,
                        guName,
                        wdm));
                feature.put(
                    "deich_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_deich,
                        guName,
                        wdm));
                feature.put(
                    "ughz_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_ughz,
                        guName,
                        wdm));
                feature.put(
                    "ughz_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_ughz,
                        guName,
                        wdm));
                feature.put(
                    "leis_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_leis,
                        guName,
                        wdm));
                feature.put(
                    "leis_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_leis,
                        guName,
                        wdm));
                feature.put(
                    "tech_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_tech,
                        guName,
                        wdm));
                feature.put(
                    "tech_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_tech,
                        guName,
                        wdm));

                features.add(feature);
                featureListKum.add(feature);
                featureListGuKum.add(feature);
            }
            features.add(createKumFeature(featureListGuKum, true));
        }
        features.add(createKumFeature(featureListKum, false));
//        }
        return new FeatureDataSource(features);
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
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew() {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : parts) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final String sb) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj tmp : parts) {
            if (tmp.getSb().equals(sb)) {
                ts.add(tmp.getId());
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

        for (final SbObj tmp : parts) {
            ts.add(tmp.getGuName());
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
    private String getGuId(final String owner) {
        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                return tmp.getGu();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGuIdBySb(final String sb) {
        for (final SbObj tmp : parts) {
            if (tmp.getSb().equals(sb)) {
                return tmp.getGu();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getOwner(final int gew) {
        for (final SbObj tmp : parts) {
            if (tmp.getId() == gew) {
                return tmp.getGuName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getSbName(final String sb) {
        for (final SbObj tmp : parts) {
            if (tmp.getSb().equals(sb)) {
                return tmp.getSbName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getWdm(final int gew) {
        for (final SbObj tmp : parts) {
            if (tmp.getId() == gew) {
                return tmp.getWidmung();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final String guName) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final SbObj gmdPart : parts) {
            if (gmdPart.getGuName().equals(guName)) {
                ts.add(gmdPart.getWidmung());
            }
        }

        return ts;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getWidmungBySb(final String sb) {
        for (final SbObj gmdPart : parts) {
            if (gmdPart.getSb().equals(sb)) {
                return gmdPart.getWidmung();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   guName  DOCUMENT ME!
     * @param   wdm     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getSb(final String guName, final int wdm) {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj gmdPart : parts) {
            if (gmdPart.getGuName().equals(guName) && (gmdPart.getWidmung() == wdm)) {
                ts.add(gmdPart.getSb());
            }
        }

        return ts;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getSb() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj gmdPart : parts) {
            ts.add(gmdPart.getSb());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb   DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double startGew(final String sb, final Integer gew) {
        double min = Double.MAX_VALUE;

        for (final SbObj gmdPart : parts) {
            if ((gmdPart.getId() == gew) && gmdPart.getSb().equals(sb) && (gmdPart.getFrom() < min)) {
                min = gmdPart.getFrom();
            }
        }

        return min;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb   DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double endGew(final String sb, final Integer gew) {
        double max = Double.MIN_VALUE;

        for (final SbObj gmdPart : parts) {
            if ((gmdPart.getId() == gew) && gmdPart.getSb().equals(sb) && (gmdPart.getTill() > max)) {
                max = gmdPart.getTill();
            }
        }

        return max;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj tmp : parts) {
            ts.add(tmp.getGuName());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final int gew) {
        for (final SbObj tmp : parts) {
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
    private String getGewName(final int gew) {
        for (final SbObj tmp : parts) {
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
    private int getCountGewAll() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final SbObj tmp : parts) {
            ts.add(tmp.getBaCd());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGewAll() {
        double length = 0;

        for (final SbObj tmp : parts) {
            length += tmp.getLength();
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  The length of the whole ba_cd
     */
    private double getLengthGew(final int gewId) {
        final double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getId() == gewId) {
                return tmp.getBaLen();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gewId, final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gewId) && (tmp.getSb().equals(sb))) {
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

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu)) {
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

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)) {
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
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final String gu, final int wdm, final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
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

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGewBySb(final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getSb().equals(sb)) {
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

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm)) {
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
     * @param   sb   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGew(final String gu, final int wdm, final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(gu) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                ++count;
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn() {
        return getCountOffeneAbschn(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn() {
        return getLengthOffeneAbschn(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn() {
        return getCountGeschlAbschn(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn() {
        return getLengthGeschlAbschn(-1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gewId) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
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
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gewId) {
        double length = 0;

        for (final SbObj tmp : parts) {
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
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gewId) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("g")) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gewId) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("g")) {
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
    private int getCountOffeneAbschn(final int gewId, final double from, final double till) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
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
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gewId, final double from, final double till) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getId() == gewId) {
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
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gewId, final double from, final double till) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                if (tmp.getArt().equals("g")) {
                    ++count;
                }
            }
        }

        return count;
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
    private double getLengthGeschlAbschn(final int gewId, final double from, final double till) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getId() == gewId) {
                if (tmp.getArt().equals("g")) {
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
    private int getCountOffeneAbschn(final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
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
    private double getLengthOffeneAbschn(final String owner) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
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
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                if (tmp.getArt().equals("g")) {
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
    private double getLengthGeschlAbschn(final String owner) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                if (tmp.getArt().equals("g")) {
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
    private int getCountOffeneAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
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
    private int getCountGeschlAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                if (tmp.getArt().equals("g")) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                if (tmp.getArt().equals("g")) {
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
     * @param   sb     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final String owner, final int wdm, final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final String owner, final int wdm, final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
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
     * @param   sb     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final String owner, final int wdm, final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                if (tmp.getArt().equals("g")) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     * @param   sb     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final String owner, final int wdm, final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                if (tmp.getArt().equals("g")) {
                    length += tmp.getLength();
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb   owner DOCUMENT ME!
     * @param   gew  wdm gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschnBySb(final String sb, final int gew) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
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
     * @param   sb   owner DOCUMENT ME!
     * @param   gew  wdm gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschnBySb(final String sb, final int gew) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
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
     * @param   sb   owner DOCUMENT ME!
     * @param   gew  wdm gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschnBySb(final String sb, final int gew) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                if (tmp.getArt().equals("g")) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb   owner DOCUMENT ME!
     * @param   gew  wdm gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschnBySb(final String sb, final int gew) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                if (tmp.getArt().equals("g")) {
                    length += tmp.getLength();
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final AllLineObjects.Table table) {
        return getCountLineObjects(table, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final AllLineObjects.Table table) {
        return getLengthLineObjects(table, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final GewaesserData.LineFromPolygonTable table) {
        return getCountLineObjects(table, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final GewaesserData.LineFromPolygonTable table) {
        return getLengthLineObjects(table, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjectsAll(final AllPunktObjects.Table table) {
        return getCountPointObjects(table, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final int gewId) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final int gewId) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GewaesserData.LineFromPolygonTable table, final int gewId) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final int gewId) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final int gewId) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final int gewId,
            final double from,
            final double till) {
        return gd.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final int gewId,
            final double from,
            final double till) {
        return gd.getLength(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GewaesserData.LineFromPolygonTable table,
            final int gewId,
            final double from,
            final double till) {
        return gd.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final int gewId,
            final double from,
            final double till) {
        return gd.getLength(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final int gewId,
            final double from,
            final double till) {
        return gd.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final String owner) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final int gemNr,
            final String owner) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final int gemNr, final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final String owner,
            final int wdm) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final String owner,
            final int wdm) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner,
            final int wdm) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner,
            final int wdm) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final String owner,
            final int wdm) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final String owner,
            final int wdm,
            final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final String owner,
            final int wdm,
            final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner,
            final int wdm,
            final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner,
            final int wdm,
            final String sb) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final String owner,
            final int wdm,
            final String sb) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner) && (tmp.getWidmung() == wdm) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsBySb(final AllLineObjects.Table table, final String sb, final int gew) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsBySb(final AllLineObjects.Table table, final String sb, final int gew) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsBySb(final GewaesserData.LineFromPolygonTable table,
            final String sb,
            final int gew) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsBySb(final GewaesserData.LineFromPolygonTable table,
            final String sb,
            final int gew) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   sb     gemNr DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjectsBySb(final AllPunktObjects.Table table, final String sb, final int gew) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if ((tmp.getId() == gew) && tmp.getSb().equals(sb)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GewaesserData.LineFromPolygonTable table,
            final String owner) {
        double length = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                length += gd.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final String owner) {
        int count = 0;

        for (final SbObj tmp : parts) {
            if (tmp.getGuName().equals(owner)) {
                count += gd.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
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
