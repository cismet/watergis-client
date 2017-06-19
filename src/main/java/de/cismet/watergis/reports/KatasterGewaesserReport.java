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

import de.cismet.cids.custom.watergis.server.search.AllGewWithParts;
import de.cismet.cids.custom.watergis.server.search.AllLineObjects;
import de.cismet.cids.custom.watergis.server.search.AllPunktObjects;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.KatasterGewaesserReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GewaesserData;
import de.cismet.watergis.reports.types.KatasterGewObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterGewaesserReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterGewaesserReport.class);
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

    private List<KatasterGewObj> parts;
    private List<String> sheetNames = new ArrayList<String>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createGewaesserReport(final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("gemeinden", 0);
        parameters.put("wasserschutz", KatasterGewaesserReportDialog.getInstance().isWsg());
        parameters.put("ueber", KatasterGewaesserReportDialog.getInstance().isSchutzgebiete());
        parameters.put("ben", KatasterGewaesserReportDialog.getInstance().isBen());
        parameters.put("aus", KatasterGewaesserReportDialog.getInstance().isAus());
        parameters.put("pegel", KatasterGewaesserReportDialog.getInstance().isPegel());
        parameters.put("gb", KatasterGewaesserReportDialog.getInstance().isGb());
        parameters.put("sb", KatasterGewaesserReportDialog.getInstance().isSb());
        parameters.put("prof", KatasterGewaesserReportDialog.getInstance().isProf());
        parameters.put("sbef", KatasterGewaesserReportDialog.getInstance().isSbef());
        parameters.put("ubef", KatasterGewaesserReportDialog.getInstance().isUbef());
        parameters.put("bbef", KatasterGewaesserReportDialog.getInstance().isBbef());
        parameters.put("rl", KatasterGewaesserReportDialog.getInstance().isRl());
        parameters.put("d", KatasterGewaesserReportDialog.getInstance().isD());
        parameters.put("due", KatasterGewaesserReportDialog.getInstance().isDue());
        parameters.put("scha", KatasterGewaesserReportDialog.getInstance().isScha());
        parameters.put("wehr", KatasterGewaesserReportDialog.getInstance().isWehr());
        parameters.put("schw", KatasterGewaesserReportDialog.getInstance().isSchw());
        parameters.put("anlp", KatasterGewaesserReportDialog.getInstance().isAnlp());
        parameters.put("anll", KatasterGewaesserReportDialog.getInstance().isAnll());
        parameters.put("kr", KatasterGewaesserReportDialog.getInstance().isKr());
        parameters.put("ea", KatasterGewaesserReportDialog.getInstance().isEa());
        parameters.put("deich", KatasterGewaesserReportDialog.getInstance().isDeich());
        parameters.put("ughz", KatasterGewaesserReportDialog.getInstance().isUghz());
        parameters.put("leis", KatasterGewaesserReportDialog.getInstance().isLeis());
        parameters.put("tech", KatasterGewaesserReportDialog.getInstance().isTech());
        parameters.put("perGew", false);
        parameters.put("perAbschn", KatasterGewaesserReportDialog.getInstance().isPerPart());
        parameters.put("sumGu", KatasterGewaesserReportDialog.getInstance().isSumGu());
        parameters.put("wdm", KatasterGewaesserReportDialog.getInstance().isPerWdm());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(KatasterGewaesserReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/stat_gewaesser.jasper"));

        init(gew);

        if (KatasterGewaesserReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesser", getGewaesser());
        } else {
            dataSources.put("gewaesserAbschn", getGewaesserAbschnitt());
        }

        if (KatasterGewaesserReportDialog.getInstance().isSumGu()) {
            if (KatasterGewaesserReportDialog.getInstance().isPerWdm()) {
                dataSources.put("guWidmung", getGuWidmung());
            } else {
                dataSources.put("gu", getGuTable());
            }
        }

        // create print from report and data
        final JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport,
                parameters,
                dummyDataSource);
        // set orientation
// jasperPrint.setOrientation(jasperReport.getOrientationValue());

//        final FileOutputStream pfout = new FileOutputStream(new File("/home/therter/tmp/gewaesser.pdf"));
//        final BufferedOutputStream pout = new BufferedOutputStream(pfout);
//        JasperExportManager.exportReportToPdfStream(jasperPrint, pout);
//        pout.close();

        final FileOutputStream fout = new FileOutputStream(new File(
                    KatasterGewaesserReportDialog.getInstance().getPath()
                            + "/Gewässer.xlsx"));
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
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        final KatasterGewaesserReport report = new KatasterGewaesserReport();
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
    private List<KatasterGewObj> getAllRoutes(final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewWithParts(routeIds, getAllowedWdms());
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

        if (KatasterGewaesserReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (KatasterGewaesserReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (KatasterGewaesserReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (KatasterGewaesserReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (KatasterGewaesserReportDialog.getInstance().is1505()) {
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
        for (final int gew : getGew()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            feature.put("anzahlGew", getCountGewAll());
            feature.put("code", getBaCd(gew));
            feature.put("gewName", getGewName(gew));
            feature.put("gewLaenge", getLengthGew(gew));
            feature.put("offene_a", getCountOffeneAbschn(gew));
            feature.put("offene_l", getLengthOffeneAbschn(gew));
            feature.put("see_a", getCountLineObjects(GewaesserData.LineFromPolygonTable.sg_see, gew));
            feature.put("see_l", getLengthLineObjects(GewaesserData.LineFromPolygonTable.sg_see, gew));
            feature.put("geschl_a", getCountGeschlAbschn(gew));
            feature.put("geschl_l", getLengthGeschlAbschn(gew));
            feature.put("wschutz_a", getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, gew));
            feature.put("wschutz_l", getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_wsg, gew));
            feature.put("ueber_a", getCountLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, gew));
            feature.put("ueber_l", getLengthLineObjects(GewaesserData.LineFromPolygonTable.wr_sg_uesg, gew));
            feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, gew));
            feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, gew));
            feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, gew));
            feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gew));
            feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, gew));
            feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gew));
            feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, gew));
            feature.put("prof_a", getCountLineObjects(AllLineObjects.Table.fg_ba_prof, gew));
            feature.put("prof_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, gew));
            feature.put("sbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, gew));
            feature.put("sbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, gew));
            feature.put("ubef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, gew));
            feature.put("ubef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, gew));
            feature.put("bbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, gew));
            feature.put("bbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, gew));
            feature.put("rl_a", getCountLineObjects(AllLineObjects.Table.fg_ba_rl, gew));
            feature.put("rl_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, gew));
            feature.put("d_a", getCountLineObjects(AllLineObjects.Table.fg_ba_d, gew));
            feature.put("d_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_d, gew));
            feature.put("due_a", getCountLineObjects(AllLineObjects.Table.fg_ba_due, gew));
            feature.put("due_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_due, gew));
            feature.put("scha_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, gew));
            feature.put("wehr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, gew));
            feature.put("schw_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, gew));
            feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, gew));
            feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, gew));
            feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, gew));
            feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, gew));
            feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, gew));
            feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.fg_ba_deich, gew));
            feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_deich, gew));
            feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, gew));
            feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, gew));
            feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, gew));
            feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, gew));
            feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, gew));
            feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, gew));

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
    private FeatureDataSource getGewaesserAbschnitt() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
        final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
        String code = null;
        sheetNames.add("Gewässer");
        for (final KatasterGewObj gew : parts) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            feature.put("anzahlGew", getCountGewAll());
            feature.put("anzahlAbschn", parts.size());
            feature.put("code", getBaCd(gew.getId()));
            feature.put("gewName", gew.getGewName());
            feature.put("von", convertStation(gew.getFrom()));
            feature.put("bis", convertStation(gew.getTill()));
            feature.put("gewLaenge", gew.getLength());
            feature.put("offene_a", getCountOffeneAbschn(gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put("offene_l", getLengthOffeneAbschn(gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "see_a",
                getCountLineObjects(
                    GewaesserData.LineFromPolygonTable.sg_see,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "see_l",
                getLengthLineObjects(
                    GewaesserData.LineFromPolygonTable.sg_see,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put("geschl_a", getCountGeschlAbschn(gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put("geschl_l", getLengthGeschlAbschn(gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "wschutz_a",
                getCountLineObjects(
                    GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "wschutz_l",
                getLengthLineObjects(
                    GewaesserData.LineFromPolygonTable.wr_sg_wsg,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ueber_a",
                getCountLineObjects(
                    GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ueber_l",
                getLengthLineObjects(
                    GewaesserData.LineFromPolygonTable.wr_sg_uesg,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ben_a",
                getCountPointObjects(
                    AllPunktObjects.Table.wr_wbu_ben,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "aus_a",
                getCountPointObjects(
                    AllPunktObjects.Table.wr_wbu_aus,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "pegel_a",
                getCountPointObjects(
                    AllPunktObjects.Table.mn_ow_pegel,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "gb_a",
                getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "gb_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_gb,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "sb_a",
                getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "sb_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_sb,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "prof_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_prof,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "prof_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_prof,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "sbef_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_sbef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "sbef_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_sbef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ubef_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_ubef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ubef_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_ubef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "bbef_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_bbef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "bbef_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_bbef,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "rl_a",
                getCountLineObjects(AllLineObjects.Table.fg_ba_rl, gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "rl_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_rl,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "d_a",
                getCountLineObjects(AllLineObjects.Table.fg_ba_d, gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "d_l",
                getLengthLineObjects(AllLineObjects.Table.fg_ba_d, gew.getId(), gew.getFrom(), gew.getTill()));
            feature.put(
                "due_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_due,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "due_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_due,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "scha_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_scha,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "wehr_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_wehr,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "schw_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_schw,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "anlp_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_anlp,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "anll_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_anll,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "anll_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_anll,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "kr_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_kr,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ea_a",
                getCountPointObjects(
                    AllPunktObjects.Table.fg_ba_ea,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "deich_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_deich,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "deich_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_deich,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ughz_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_ughz,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "ughz_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_ughz,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "leis_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_leis,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "leis_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_leis,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "tech_a",
                getCountLineObjects(
                    AllLineObjects.Table.fg_ba_tech,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));
            feature.put(
                "tech_l",
                getLengthLineObjects(
                    AllLineObjects.Table.fg_ba_tech,
                    gew.getId(),
                    gew.getFrom(),
                    gew.getTill()));

            final String newCode = getBaCd(gew.getId());

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

        for (final KatasterGewObj tmp : parts) {
            ts.add(tmp.getId());
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

        for (final KatasterGewObj tmp : parts) {
            ts.add(tmp.getOwner());
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
        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
                return tmp.getGu();
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
        for (final KatasterGewObj tmp : parts) {
            if (tmp.getId() == gew) {
                return tmp.getWidmung();
            }
        }

        return null;
    }

    /**
     * /** * DOCUMENT ME! * * @param gemNr DOCUMENT ME! * @param gew owner DOCUMENT ME! * * @return DOCUMENT ME!
     *
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final String guName) {
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final KatasterGewObj gmdPart : parts) {
            if (gmdPart.getOwner().equals(guName)) {
                ts.add(gmdPart.getWidmung());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final KatasterGewObj tmp : parts) {
            ts.add(tmp.getOwner());
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
        for (final KatasterGewObj tmp : parts) {
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
        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
            length += tmp.getLength();
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
    private double getLengthGew(final int gewId) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getId() == gewId) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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
    private int getCountGew(final String gu) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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
    private int getCountOffeneAbschn(final String owner, final Integer wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
    private double getLengthOffeneAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
    private int getCountGeschlAbschn(final String owner, final Integer wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
    private double getLengthGeschlAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
                if (tmp.getArt().equals("p") && (tmp.getWidmung() == wdm)) {
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
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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
            final Integer wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
            final Integer wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
            final Integer wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
            final Integer wdm) {
        double length = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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
            final Integer wdm) {
        int count = 0;

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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

        for (final KatasterGewObj tmp : parts) {
            if (tmp.getOwner().equals(owner)) {
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
