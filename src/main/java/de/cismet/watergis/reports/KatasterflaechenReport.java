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

import javax.swing.ImageIcon;

import de.cismet.cids.custom.watergis.server.search.AllGewByArea;
import de.cismet.cids.custom.watergis.server.search.AllLineObjects;
import de.cismet.cids.custom.watergis.server.search.AllPunktObjects;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.watergis.gui.dialog.KatasterFlaechenReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.Flaeche;
import de.cismet.watergis.reports.types.GewFlObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterflaechenReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterflaechenReport.class);
    private static ImageIcon annotationIco = new javax.swing.ImageIcon(KatasterflaechenReport.class.getResource(
                "/de/cismet/watergis/reports/Station.png")); // NOI18N
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

    private final Map<Object, List<GewFlObj>> flPartMap = new HashMap<Object, List<GewFlObj>>();
    private final Map<Object, KatasterFlaechenData> flDataMap = new HashMap<Object, KatasterFlaechenData>();
    private final List<String> sheetNames = new ArrayList<String>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   fl   baCd DOCUMENT ME!
     * @param   gew  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createFlaechenReport(final Flaeche[] fl, final int[] gew) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("gemeinden", fl.length);
        parameters.put("wasserschutz", KatasterFlaechenReportDialog.getInstance().isWsg());
        parameters.put("ueber", KatasterFlaechenReportDialog.getInstance().isSchutzgebiete());
        parameters.put("ben", KatasterFlaechenReportDialog.getInstance().isBen());
        parameters.put("aus", KatasterFlaechenReportDialog.getInstance().isAus());
        parameters.put("pegel", KatasterFlaechenReportDialog.getInstance().isPegel());
        parameters.put("gb", KatasterFlaechenReportDialog.getInstance().isGb());
        parameters.put("sb", KatasterFlaechenReportDialog.getInstance().isSb());
        parameters.put("prof", KatasterFlaechenReportDialog.getInstance().isProf());
        parameters.put("sbef", KatasterFlaechenReportDialog.getInstance().isSbef());
        parameters.put("ubef", KatasterFlaechenReportDialog.getInstance().isUbef());
        parameters.put("bbef", KatasterFlaechenReportDialog.getInstance().isBbef());
        parameters.put("rl", KatasterFlaechenReportDialog.getInstance().isRl());
        parameters.put("d", KatasterFlaechenReportDialog.getInstance().isD());
        parameters.put("due", KatasterFlaechenReportDialog.getInstance().isDue());
        parameters.put("scha", KatasterFlaechenReportDialog.getInstance().isScha());
        parameters.put("wehr", KatasterFlaechenReportDialog.getInstance().isWehr());
        parameters.put("schw", KatasterFlaechenReportDialog.getInstance().isSchw());
        parameters.put("anlp", KatasterFlaechenReportDialog.getInstance().isAnlp());
        parameters.put("anll", KatasterFlaechenReportDialog.getInstance().isAnll());
        parameters.put("kr", KatasterFlaechenReportDialog.getInstance().isKr());
        parameters.put("ea", KatasterFlaechenReportDialog.getInstance().isEa());
        parameters.put("deich", KatasterFlaechenReportDialog.getInstance().isDeich());
        parameters.put("ughz", KatasterFlaechenReportDialog.getInstance().isUghz());
        parameters.put("leis", KatasterFlaechenReportDialog.getInstance().isLeis());
        parameters.put("tech", KatasterFlaechenReportDialog.getInstance().isTech());
        parameters.put("perGew", KatasterFlaechenReportDialog.getInstance().isPerGew());
        parameters.put("perAbschn", KatasterFlaechenReportDialog.getInstance().isPerPart());
        parameters.put("sumGu", KatasterFlaechenReportDialog.getInstance().isSumGu());
        parameters.put("wdm", KatasterFlaechenReportDialog.getInstance().isPerWdm());
        parameters.put("thema", KatasterFlaechenReportDialog.getInstance().getFlaechenService().getName()); // todo setzen
        parameters.put("attrName1", KatasterFlaechenReportDialog.getInstance().getAttr1());                 // todo setzen
        parameters.put("attrName2", KatasterFlaechenReportDialog.getInstance().getAttr2());                 // todo setzen
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(KatasterflaechenReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/flaechen.jasper"));

        init(fl, gew);

        dataSources.put("gemeinden", getGemeindenAll());

        if (KatasterFlaechenReportDialog.getInstance().isSumGu()) {
            if (KatasterFlaechenReportDialog.getInstance().isPerWdm()) {
                dataSources.put("guWidmung", getGuWidmung());
            } else {
                dataSources.put("gu", getGuTable());
            }
        }

        if (KatasterFlaechenReportDialog.getInstance().isPerGew()
                    && !KatasterFlaechenReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesser", getGewaesser());
        } else if (KatasterFlaechenReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesserAbschnitt", getGewaesserAbschnitt());
        }

        if (KatasterFlaechenReportDialog.getInstance().isSumGu()) {
            if (KatasterFlaechenReportDialog.getInstance().isPerWdm()) {
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
        // set orientation
// jasperPrint.setOrientation(jasperReport.getOrientationValue());

//        final FileOutputStream pfout = new FileOutputStream(new File("/home/therter/tmp/gemeinden.pdf"));
//        final BufferedOutputStream pout = new BufferedOutputStream(pfout);
//        JasperExportManager.exportReportToPdfStream(jasperPrint, pout);
//        pout.close();

        final FileOutputStream fout = new FileOutputStream(new File(
                    KatasterFlaechenReportDialog.getInstance().getPath()
                            + "/Flächen.xlsx"));
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
//        final GewaesserFlaechenReport report = new GewaesserFlaechenReport();
//        try {
//            report.createGemeindeReport(new int[] { 2 }, new int[] { 2 });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fl        attr1 DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void init(final Flaeche[] fl, final int[] routeIds) throws Exception {
        for (final Flaeche f : fl) {
            flPartMap.put(f.getAttr1(), getAllRoutes(f, routeIds));

            final Integer[] idList = getGew(f.getAttr1()).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
            flDataMap.put(f.getAttr1(), new KatasterFlaechenData(f.getAttr1(), f.getAttr2(), routes));
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
    private List<GewFlObj> getAllRoutes(final Flaeche fl, final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewByArea(routeIds, getAllowedWdms(), fl.getGeom());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GewFlObj> objList = new ArrayList<GewFlObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GewFlObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(5),
                        (String)f.get(6),
                        (String)f.get(7),
                        (Integer)f.get(8),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        fl.getAttr1()));
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

        if (KatasterFlaechenReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (KatasterFlaechenReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (KatasterFlaechenReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (KatasterFlaechenReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (KatasterFlaechenReportDialog.getInstance().is1505()) {
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
        sheetNames.add(KatasterFlaechenReportDialog.getInstance().getFlaechenService().getName());

        for (final Object gem : flDataMap.keySet()) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            feature.put("name", String.valueOf(flDataMap.get(gem).getAttr2()));
            feature.put("nummer", gem);
            feature.put("gew_a", getCountGewAll(gem));
            feature.put("gew_l", getLengthGewAll(gem));
            feature.put("offene_a", getCountOffeneAbschn(gem));
            feature.put("offene_l", getLengthOffeneAbschn(gem));
            feature.put("see_a", getCountLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("see_l", getLengthLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("geschl_a", getCountGeschlAbschn(gem));
            feature.put("geschl_l", getLengthGeschlAbschn(gem));
            feature.put("wschutz_a", getCountLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("wschutz_l", getLengthLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("ueber_a", getCountLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("ueber_l", getLengthLineObjectsAll(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem));
            feature.put("ben_a", getCountPointObjectsAll(AllPunktObjects.Table.wr_wbu_ben, gem));
            feature.put("aus_a", getCountPointObjectsAll(AllPunktObjects.Table.wr_wbu_aus, gem));
            feature.put("pegel_a", getCountPointObjectsAll(AllPunktObjects.Table.mn_ow_pegel, gem));
            feature.put("gb_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_gb, gem));
            feature.put("gb_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_gb, gem));
            feature.put("sb_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_sb, gem));
            feature.put("sb_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_sb, gem));
            feature.put("prof_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_prof, gem));
            feature.put("prof_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_prof, gem));
            feature.put("sbef_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_sbef, gem));
            feature.put("sbef_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_sbef, gem));
            feature.put("ubef_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_ubef, gem));
            feature.put("ubef_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_ubef, gem));
            feature.put("bbef_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_bbef, gem));
            feature.put("bbef_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_bbef, gem));
            feature.put("rl_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_rl, gem));
            feature.put("rl_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_rl, gem));
            feature.put("d_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_d, gem));
            feature.put("d_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_d, gem));
            feature.put("due_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_due, gem));
            feature.put("due_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_due, gem));
            feature.put("scha_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_scha, gem));
            feature.put("wehr_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_wehr, gem));
            feature.put("schw_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_schw, gem));
            feature.put("anlp_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_anlp, gem));
            feature.put("anll_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_anll, gem));
            feature.put("anll_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_anll, gem));
            feature.put("kr_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_kr, gem));
            feature.put("ea_a", getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_ea, gem));
            feature.put("deich_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_deich, gem));
            feature.put("deich_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_deich, gem));
            feature.put("ughz_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_ughz, gem));
            feature.put("ughz_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_ughz, gem));
            feature.put("leis_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_leis, gem));
            feature.put("leis_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_leis, gem));
            feature.put("tech_a", getCountLineObjectsAll(AllLineObjects.Table.fg_ba_tech, gem));
            feature.put("tech_l", getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_tech, gem));

            features.add(feature);
        }
        features.add(createKumFeature(features, false));

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

        for (final Object gem : flDataMap.keySet()) {
            sheetNames.add(String.valueOf(flDataMap.get(gem).getAttr2()));
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            for (final int gew : getGew(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", String.valueOf(flDataMap.get(gem).getAttr2()));
                feature.put("gmdNummer", String.valueOf(gem));
                feature.put("anzahlGew", getCountGewAll(gem));
                feature.put("group", String.valueOf(gem));
                feature.put("code", getBaCd(gem, gew));
                feature.put("gewName", getGewName(gem, gew));
                feature.put("gewLaenge", getLengthGew(gem, gew));
                feature.put("offene_a", getCountOffeneAbschn(gem, gew));
                feature.put("offene_l", getLengthOffeneAbschn(gem, gew));
                feature.put("see_a", getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem, gew));
                feature.put("see_l", getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem, gew));
                feature.put("geschl_a", getCountGeschlAbschn(gem, gew));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, gew));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, gem, gew));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, gem, gew));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, gem, gew));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, gem, gew));
                feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, gem, gew));
                feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, gem, gew));
                feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, gem, gew));
                feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gem, gew));
                feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, gem, gew));
                feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gem, gew));
                feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, gem, gew));
                feature.put("prof_a", getCountLineObjects(AllLineObjects.Table.fg_ba_prof, gem, gew));
                feature.put("prof_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, gem, gew));
                feature.put("sbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, gem, gew));
                feature.put("sbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, gem, gew));
                feature.put("ubef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, gem, gew));
                feature.put("ubef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, gem, gew));
                feature.put("bbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, gem, gew));
                feature.put("bbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, gem, gew));
                feature.put("rl_a", getCountLineObjects(AllLineObjects.Table.fg_ba_rl, gem, gew));
                feature.put("rl_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, gem, gew));
                feature.put("d_a", getCountLineObjects(AllLineObjects.Table.fg_ba_d, gem, gew));
                feature.put("d_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_d, gem, gew));
                feature.put("due_a", getCountLineObjects(AllLineObjects.Table.fg_ba_due, gem, gew));
                feature.put("due_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_due, gem, gew));
                feature.put("scha_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, gem, gew));
                feature.put("wehr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, gem, gew));
                feature.put("schw_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, gem, gew));
                feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, gem, gew));
                feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, gem, gew));
                feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, gem, gew));
                feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, gem, gew));
                feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, gem, gew));
                feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.fg_ba_deich, gem, gew));
                feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_deich, gem, gew));
                feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, gew));
                feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, gew));
                feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, gem, gew));
                feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, gem, gew));
                feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, gem, gew));
                feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, gem, gew));

                features.add(feature);
                featureListKum.add(feature);
            }
            features.add(createKumFeature(featureListKum, false));
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

        for (final Object gem : flDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            sheetNames.add(String.valueOf(flDataMap.get(gem).getAttr2()));
            for (final GewFlObj gew : flPartMap.get(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", String.valueOf(flDataMap.get(gem).getAttr2()));
                feature.put("gmdNummer", String.valueOf(gem));
                feature.put("anzahlGew", getCountGewAll(gem));
                feature.put("anzahlAbschn", flPartMap.get(gem).size());
                feature.put("group", String.valueOf(gem));
                feature.put("code", getBaCd(gem, gew.getId()));
                feature.put("gewName", gew.getGewName());
                feature.put("von", convertStation(gew.getFrom()));
                feature.put("bis", convertStation(gew.getTill()));
                feature.put("gewLaenge", gew.getLength());
                feature.put("offene_a", getCountOffeneAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put("offene_l", getLengthOffeneAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "see_a",
                    getCountLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.sg_see,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "see_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.sg_see,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put("geschl_a", getCountGeschlAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ben_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.wr_wbu_ben,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "aus_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.wr_wbu_aus,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "pegel_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.mn_ow_pegel,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "gb_a",
                    getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "gb_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_gb,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "sb_a",
                    getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "sb_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_sb,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "prof_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_prof,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "prof_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_prof,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "sbef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_sbef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "sbef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_sbef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ubef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_ubef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ubef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_ubef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "bbef_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_bbef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "bbef_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_bbef,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "rl_a",
                    getCountLineObjects(AllLineObjects.Table.fg_ba_rl, gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "rl_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_rl,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "d_a",
                    getCountLineObjects(AllLineObjects.Table.fg_ba_d, gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "d_l",
                    getLengthLineObjects(AllLineObjects.Table.fg_ba_d, gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "due_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_due,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "due_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_due,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "scha_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_scha,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "wehr_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_wehr,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "schw_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_schw,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "anlp_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_anlp,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "anll_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_anll,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "anll_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_anll,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "kr_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_kr,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ea_a",
                    getCountPointObjects(
                        AllPunktObjects.Table.fg_ba_ea,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "deich_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_deich,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "deich_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_deich,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ughz_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_ughz,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ughz_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_ughz,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "leis_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_leis,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "leis_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_leis,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "tech_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_tech,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "tech_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_tech,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));

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
        return new FeatureDataSource(features);
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

        for (final Object gem : flDataMap.keySet()) {
            sheetNames.add("GU " + String.valueOf(flDataMap.get(gem).getAttr2()));
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            for (final String guName : getGu(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", String.valueOf(flDataMap.get(gem).getAttr2()));
                feature.put("gmdNummer", String.valueOf(gem));
                feature.put("anzahlGu", getCountGu(gem));
                feature.put("group", String.valueOf(gem));
                feature.put("gu", getGuId(gem, guName));
                feature.put("guName", guName);
                feature.put("gewAnzahl", getCountGew(gem, guName));
                feature.put("gewLaenge", getLengthGew(gem, guName));
                feature.put("offene_a", getCountOffeneAbschn(gem, guName));
                feature.put("offene_l", getLengthOffeneAbschn(gem, guName));
                feature.put(
                    "see_a",
                    getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem, guName));
                feature.put(
                    "see_l",
                    getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, gem, guName));
                feature.put("geschl_a", getCountGeschlAbschn(gem, guName));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, guName));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, gem, guName));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, gem, guName));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, gem, guName));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, gem, guName));
                feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, gem, guName));
                feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, gem, guName));
                feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, gem, guName));
                feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gem, guName));
                feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, gem, guName));
                feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gem, guName));
                feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, gem, guName));
                feature.put("prof_a", getCountLineObjects(AllLineObjects.Table.fg_ba_prof, gem, guName));
                feature.put("prof_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, gem, guName));
                feature.put("sbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, gem, guName));
                feature.put("sbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, gem, guName));
                feature.put("ubef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, gem, guName));
                feature.put("ubef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, gem, guName));
                feature.put("bbef_a", getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, gem, guName));
                feature.put("bbef_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, gem, guName));
                feature.put("rl_a", getCountLineObjects(AllLineObjects.Table.fg_ba_rl, gem, guName));
                feature.put("rl_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, gem, guName));
                feature.put("d_a", getCountLineObjects(AllLineObjects.Table.fg_ba_d, gem, guName));
                feature.put("d_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_d, gem, guName));
                feature.put("due_a", getCountLineObjects(AllLineObjects.Table.fg_ba_due, gem, guName));
                feature.put("due_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_due, gem, guName));
                feature.put("scha_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, gem, guName));
                feature.put("wehr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, gem, guName));
                feature.put("schw_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, gem, guName));
                feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, gem, guName));
                feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, gem, guName));
                feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, gem, guName));
                feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, gem, guName));
                feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, gem, guName));
                feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.fg_ba_deich, gem, guName));
                feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_deich, gem, guName));
                feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, guName));
                feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, guName));
                feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, gem, guName));
                feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, gem, guName));
                feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, gem, guName));
                feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, gem, guName));

                features.add(feature);
                featureListKum.add(feature);
            }
            features.add(createKumFeature(featureListKum, false));
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
    private FeatureDataSource getGewaesserGuWidmung() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Object gem : flDataMap.keySet()) {
            sheetNames.add("GU " + String.valueOf(flDataMap.get(gem).getAttr2()));
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

            for (final String guName : getGu(gem)) {
                final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

                for (final Integer wdm : getWidmung(gem, guName)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    feature.put("gmdName", String.valueOf(flDataMap.get(gem).getAttr2()));
                    feature.put("gmdNummer", String.valueOf(gem));
                    feature.put("anzahlGu", getCountGu(gem));
                    feature.put("group", String.valueOf(gem));
                    feature.put("gu", getGuId(gem, guName));
                    feature.put("guName", guName);
                    feature.put("widmung", wdm);
                    feature.put("gew_a", getCountGew(gem, guName, wdm));
                    feature.put("gew_l", getLengthGew(gem, guName, wdm));
                    feature.put("offene_a", getCountOffeneAbschn(gem, guName, wdm));
                    feature.put("offene_l", getLengthOffeneAbschn(gem, guName, wdm));
                    feature.put(
                        "see_a",
                        getCountLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.sg_see,
                            gem,
                            wdm));
                    feature.put(
                        "see_l",
                        getLengthLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.sg_see,
                            gem,
                            wdm));
                    feature.put("geschl_a", getCountGeschlAbschn(gem, guName, wdm));
                    feature.put("geschl_l", getLengthGeschlAbschn(gem, guName, wdm));
                    feature.put(
                        "wschutz_a",
                        getCountLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                            gem,
                            wdm));
                    feature.put(
                        "wschutz_l",
                        getLengthLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                            gem,
                            wdm));
                    feature.put(
                        "ueber_a",
                        getCountLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
                            gem,
                            wdm));
                    feature.put(
                        "ueber_l",
                        getLengthLineObjects(
                            KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
                            gem,
                            wdm));
                    feature.put(
                        "ben_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.wr_wbu_ben,
                            gem,
                            wdm));
                    feature.put(
                        "aus_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.wr_wbu_aus,
                            gem,
                            wdm));
                    feature.put(
                        "pegel_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.mn_ow_pegel,
                            gem,
                            wdm));
                    feature.put(
                        "gb_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_gb,
                            gem,
                            wdm));
                    feature.put(
                        "gb_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_gb,
                            gem,
                            wdm));
                    feature.put(
                        "sb_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_sb,
                            gem,
                            wdm));
                    feature.put(
                        "sb_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_sb,
                            gem,
                            wdm));
                    feature.put(
                        "prof_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_prof,
                            gem,
                            wdm));
                    feature.put(
                        "prof_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_prof,
                            gem,
                            wdm));
                    feature.put(
                        "sbef_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_sbef,
                            gem,
                            wdm));
                    feature.put(
                        "sbef_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_sbef,
                            gem,
                            wdm));
                    feature.put(
                        "ubef_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_ubef,
                            gem,
                            wdm));
                    feature.put(
                        "ubef_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_ubef,
                            gem,
                            wdm));
                    feature.put(
                        "bbef_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_bbef,
                            gem,
                            wdm));
                    feature.put(
                        "bbef_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_bbef,
                            gem,
                            wdm));
                    feature.put(
                        "rl_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_rl,
                            gem,
                            wdm));
                    feature.put(
                        "rl_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_rl,
                            gem,
                            wdm));
                    feature.put(
                        "d_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_d,
                            gem,
                            wdm));
                    feature.put(
                        "d_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_d,
                            gem,
                            wdm));
                    feature.put(
                        "due_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_due,
                            gem,
                            wdm));
                    feature.put(
                        "due_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_due,
                            gem,
                            wdm));
                    feature.put(
                        "scha_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_scha,
                            gem,
                            wdm));
                    feature.put(
                        "wehr_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_wehr,
                            gem,
                            wdm));
                    feature.put(
                        "schw_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_schw,
                            gem,
                            wdm));
                    feature.put(
                        "anlp_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_anlp,
                            gem,
                            wdm));
                    feature.put(
                        "anll_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_anll,
                            gem,
                            wdm));
                    feature.put(
                        "anll_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_anll,
                            gem,
                            wdm));
                    feature.put(
                        "kr_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_kr,
                            gem,
                            wdm));
                    feature.put(
                        "ea_a",
                        getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_ea,
                            gem,
                            wdm));
                    feature.put(
                        "deich_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_deich,
                            gem,
                            wdm));
                    feature.put(
                        "deich_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_deich,
                            gem,
                            wdm));
                    feature.put(
                        "ughz_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_ughz,
                            gem,
                            wdm));
                    feature.put(
                        "ughz_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_ughz,
                            gem,
                            wdm));
                    feature.put(
                        "leis_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_leis,
                            gem,
                            wdm));
                    feature.put(
                        "leis_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_leis,
                            gem,
                            wdm));
                    feature.put(
                        "tech_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_tech,
                            gem,
                            wdm));
                    feature.put(
                        "tech_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_tech,
                            gem,
                            wdm));

                    features.add(feature);
                    featureListKum.add(feature);
                    featureListGuKum.add(feature);
                }
                features.add(createKumFeature(featureListGuKum, true));
            }
            features.add(createKumFeature(featureListKum, false));
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
            feature.put("see_a", getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, guName));
            feature.put("see_l", getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.sg_see, guName));
            feature.put("geschl_a", getCountGeschlAbschn(guName));
            feature.put("geschl_l", getLengthGeschlAbschn(guName));
            feature.put(
                "wschutz_a",
                getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, guName));
            feature.put(
                "wschutz_l",
                getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg, guName));
            feature.put("ueber_a", getCountLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, guName));
            feature.put(
                "ueber_l",
                getLengthLineObjects(KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg, guName));
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
                        KatasterFlaechenData.LineFromPolygonTable.sg_see,
                        guName,
                        wdm));
                feature.put(
                    "see_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.sg_see,
                        guName,
                        wdm));
                feature.put("geschl_a", getCountGeschlAbschn(guName, wdm));
                feature.put("geschl_l", getLengthGeschlAbschn(guName, wdm));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                        guName,
                        wdm));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_wsg,
                        guName,
                        wdm));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
                        guName,
                        wdm));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(
                        KatasterFlaechenData.LineFromPolygonTable.wr_sg_uesg,
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
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final Object attr1) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GewFlObj tmp : gemList) {
            ts.add(tmp.getId());
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<GewFlObj> getGew(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        final List<GewFlObj> list = new ArrayList<GewFlObj>();

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                list.add(tmp);
            }
        }

        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getGu(final Object attr1) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GewFlObj tmp : gemList) {
            ts.add(tmp.getOwner());
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                ts.add(tmp.getOwner());
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGuId(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                return tmp.getGu();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGuId(final String owner) {
        for (final Object attr1 : flPartMap.keySet()) {
            final String tmp = getGuId(attr1, owner);

            if (tmp != null) {
                return tmp;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getWdm(final Object attr1, final int gew) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);

        for (final GewFlObj tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getWidmung();
            }
        }

        return null;
    }

    /**
     * /** * DOCUMENT ME! * * @param attr1 DOCUMENT ME! * @param gew owner DOCUMENT ME! * * @return DOCUMENT ME!
     *
     * @param   attr1   DOCUMENT ME!
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final Object attr1, final String guName) {
        final List<GewFlObj> gmdList = flPartMap.get(attr1);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GewFlObj gmdPart : gmdList) {
            if (gmdPart.getOwner().equals(guName)) {
                ts.add(gmdPart.getWidmung());
            }
        }

        return ts.descendingSet();
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gmdList = flPartMap.get(attr1);

            for (final GewFlObj gmdPart : gmdList) {
                if (gmdPart.getOwner().equals(guName)) {
                    ts.add(gmdPart.getWidmung());
                }
            }
        }

        return ts.descendingSet();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGu(final Object attr1) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GewFlObj tmp : gemList) {
            ts.add(tmp.getOwner());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGu() {
        final TreeSet<String> ts = new TreeSet<String>();

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                ts.add(tmp.getOwner());
            }
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getBaCd(final Object attr1, final int gew) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);

        for (final GewFlObj tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getBaCd();
            }
        }

        return null;
    }
    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getGewName(final Object attr1, final int gew) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);

        for (final GewFlObj tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getGewName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGewAll(final Object attr1) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GewFlObj tmp : gemList) {
            ts.add(tmp.getBaCd());
        }

        return ts.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGewAll(final Object attr1) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            length += tmp.getLength();
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object attr1, final int gewId) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getId() == gewId) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object attr1, final String gu) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   wdm  attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final String gu, final Integer wdm) {
        double length = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGew(final Object attr1, final String gu) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   wdm  attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGew(final String gu, final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final Object attr1, final String gu, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                length += tmp.getLength();
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gu     DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGew(final Object attr1, final String gu, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
                ++count;
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(gu)) {
                    length += tmp.getLength();
                }
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(gu)) {
                    ++count;
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final Object attr1) {
        return getCountOffeneAbschn(attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final Object attr1) {
        return getLengthOffeneAbschn(attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final Object attr1) {
        return getCountGeschlAbschn(attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final Object attr1) {
        return getLengthGeschlAbschn(attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final Object attr1, final int gewId) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final Object attr1, final int gewId) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final Object attr1, final int gewId) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final Object attr1, final int gewId) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final Object attr1, final int gewId, final double from, final double till) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final Object attr1, final int gewId, final double from, final double till) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final Object attr1, final int gewId, final double from, final double till) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final Object attr1, final int gewId, final double from, final double till) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final Object attr1, final String owner) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final String owner, final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("p")) {
                        ++count;
                    }
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("p")) {
                        length += tmp.getLength();
                    }
                }
            }
        }
        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final String owner, final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("g")) {
                        ++count;
                    }
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final String owner, final Integer wdm) {
        double length = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("g")) {
                        length += tmp.getLength();
                    }
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final Object attr1, final String owner, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final Object attr1, final String owner, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final Object attr1, final String owner, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
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
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final Object attr1, final String owner, final int wdm) {
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
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
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final String owner) {
        int count = 0;

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("p")) {
                        ++count;
                    }
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("p")) {
                        length += tmp.getLength();
                    }
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("g")) {
                        ++count;
                    }
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

        for (final Object attr1 : flPartMap.keySet()) {
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("g")) {
                        length += tmp.getLength();
                    }
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final AllLineObjects.Table table, final Object attr1) {
        return getCountLineObjects(table, attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final AllLineObjects.Table table, final Object attr1) {
        return getLengthLineObjects(table, attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final KatasterFlaechenData.LineFromPolygonTable table, final Object attr1) {
        return getCountLineObjects(table, attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final KatasterFlaechenData.LineFromPolygonTable table, final Object attr1) {
        return getLengthLineObjects(table, attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjectsAll(final AllPunktObjects.Table table, final Object attr1) {
        return getCountPointObjects(table, attr1, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final Object attr1, final int gewId) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final Object attr1, final int gewId) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final int gewId) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final int gewId) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final Object attr1, final int gewId) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final Object attr1,
            final int gewId,
            final double from,
            final double till) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);

        return gemData.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final Object attr1,
            final int gewId,
            final double from,
            final double till) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);

        return gemData.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final int gewId,
            final double from,
            final double till) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);

        return gemData.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final int gewId,
            final double from,
            final double till) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);

        return gemData.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final Object attr1,
            final int gewId,
            final double from,
            final double till) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);

        return gemData.getCount(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final Object attr1, final String owner) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final Object attr1, final String owner) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final String owner) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final String owner) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final Object attr1, final String owner) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final Object attr1,
            final String owner,
            final Integer wdm) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final Object attr1,
            final String owner,
            final Integer wdm) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final String owner,
            final Integer wdm) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final Object attr1,
            final String owner,
            final Integer wdm) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        double length = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   attr1  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final Object attr1,
            final String owner,
            final Integer wdm) {
        final KatasterFlaechenData gemData = flDataMap.get(attr1);
        final List<GewFlObj> gemList = flPartMap.get(attr1);
        int count = 0;

        for (final GewFlObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final String owner, final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final String owner, final Integer wdm) {
        double length = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final String owner,
            final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
            }
        }

        return count;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final String owner,
            final Integer wdm) {
        double length = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
            }
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    attr1 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final String owner, final Integer wdm) {
        int count = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);
            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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
    private int getCountLineObjects(final AllLineObjects.Table table, final String owner) {
        int count = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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
    private int getCountLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final String owner) {
        int count = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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
    private double getLengthLineObjects(final KatasterFlaechenData.LineFromPolygonTable table,
            final String owner) {
        double length = 0;

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    length += gemData.getLength(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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

        for (final Object attr1 : flDataMap.keySet()) {
            final KatasterFlaechenData gemData = flDataMap.get(attr1);
            final List<GewFlObj> gemList = flPartMap.get(attr1);

            for (final GewFlObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    count += gemData.getCount(table, tmp.getId(), tmp.getFrom(), tmp.getTill());
                }
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
