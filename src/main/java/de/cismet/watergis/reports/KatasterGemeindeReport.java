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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.ImageIcon;

import de.cismet.cids.custom.watergis.server.search.AllGewByGem;
import de.cismet.cids.custom.watergis.server.search.AllLineObjects;
import de.cismet.cids.custom.watergis.server.search.AllPunktObjects;
import de.cismet.cids.custom.watergis.server.search.SeeReport;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.tools.gui.WaitDialog;

import de.cismet.watergis.gui.dialog.KatasterGemeindeReportDialog;

import de.cismet.watergis.reports.types.FeatureDataSource;
import de.cismet.watergis.reports.types.GemeindenData;
import de.cismet.watergis.reports.types.GmdPartObj;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class KatasterGemeindeReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(KatasterGemeindeReport.class);
    private static ImageIcon annotationIco = new javax.swing.ImageIcon(KatasterGemeindeReport.class.getResource(
                "/de/cismet/watergis/reports/Station.png")); // NOI18N
    private static final String[] exceptionalFields = {
            "name",
            "nummer",
            "gmdNummer",
//            "group",
            "gmdName",
            "code",
            "anzahlGu",
            "gu"
        };
// private final Map<String, CidsLayer> layerMap = new HashMap<String, CidsLayer>();

    static {
        Arrays.sort(exceptionalFields);
    }

    //~ Instance fields --------------------------------------------------------

    private final Map<Integer, List<GmdPartObj>> gemPartMap = new HashMap<Integer, List<GmdPartObj>>();
    private final Map<Integer, GemeindenData> gemDataMap = new HashMap<Integer, GemeindenData>();
    private final List<String> sheetNames = new ArrayList<String>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   gemId  baCd DOCUMENT ME!
     * @param   gew    DOCUMENT ME!
     * @param   wd     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public File createGemeindeReport(final int[] gemId, final int[] gew, final WaitDialog wd) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.YYYY");

        parameters.put("datum", df.format(new Date()));
        parameters.put("gemeinden", gemId.length);
        parameters.put("wasserschutz", KatasterGemeindeReportDialog.getInstance().isWsg());
        parameters.put("ueber", KatasterGemeindeReportDialog.getInstance().isSchutzgebiete());
        parameters.put("ben", KatasterGemeindeReportDialog.getInstance().isBen());
        parameters.put("aus", KatasterGemeindeReportDialog.getInstance().isAus());
        parameters.put("pegel", KatasterGemeindeReportDialog.getInstance().isPegel());
        parameters.put("gb", KatasterGemeindeReportDialog.getInstance().isGb());
        parameters.put("sb", KatasterGemeindeReportDialog.getInstance().isSb());
        parameters.put("prof", KatasterGemeindeReportDialog.getInstance().isProf());
        parameters.put("sbef", KatasterGemeindeReportDialog.getInstance().isSbef());
        parameters.put("ubef", KatasterGemeindeReportDialog.getInstance().isUbef());
        parameters.put("bbef", KatasterGemeindeReportDialog.getInstance().isBbef());
        parameters.put("rl", KatasterGemeindeReportDialog.getInstance().isRl());
        parameters.put("d", KatasterGemeindeReportDialog.getInstance().isD());
        parameters.put("due", KatasterGemeindeReportDialog.getInstance().isDue());
        parameters.put("scha", KatasterGemeindeReportDialog.getInstance().isScha());
        parameters.put("wehr", KatasterGemeindeReportDialog.getInstance().isWehr());
        parameters.put("schw", KatasterGemeindeReportDialog.getInstance().isSchw());
        parameters.put("foto", KatasterGemeindeReportDialog.getInstance().isFoto());
        parameters.put("anlp", KatasterGemeindeReportDialog.getInstance().isAnlp());
        parameters.put("anll", KatasterGemeindeReportDialog.getInstance().isAnll());
        parameters.put("kr", KatasterGemeindeReportDialog.getInstance().isKr());
        parameters.put("ea", KatasterGemeindeReportDialog.getInstance().isEa());
        parameters.put("deich", KatasterGemeindeReportDialog.getInstance().isDeich());
        parameters.put("ughz", KatasterGemeindeReportDialog.getInstance().isUghz());
        parameters.put("leis", KatasterGemeindeReportDialog.getInstance().isLeis());
        parameters.put("tech", KatasterGemeindeReportDialog.getInstance().isTech());
        parameters.put("dok", KatasterGemeindeReportDialog.getInstance().isDok());
        parameters.put("proj", KatasterGemeindeReportDialog.getInstance().isProj());
        parameters.put("perGew", KatasterGemeindeReportDialog.getInstance().isPerGew());
        parameters.put("perAbschn", KatasterGemeindeReportDialog.getInstance().isPerPart());
        parameters.put("sumGu", KatasterGemeindeReportDialog.getInstance().isSumGu());
        parameters.put("wdm", KatasterGemeindeReportDialog.getInstance().isPerWdm());
        parameters.put("dataSources", dataSources);

        final FeatureDataSource dummyDataSource = new FeatureDataSource(new ArrayList());
        // load report
        final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(KatasterGemeindeReport.class
                        .getResourceAsStream("/de/cismet/watergis/reports/gemeinden.jasper"));

        if (!init(gemId, gew, wd)) {
            return null;
        }

        dataSources.put("gemeinden", getGemeindenAll());

        if (KatasterGemeindeReportDialog.getInstance().isPerGew()
                    && !KatasterGemeindeReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesser", getGewaesser());
        } else if (KatasterGemeindeReportDialog.getInstance().isPerPart()) {
            dataSources.put("gewaesserAbschnitt", getGewaesserAbschnitt());
        }

        if (KatasterGemeindeReportDialog.getInstance().isPerGew()) {
            if (KatasterGemeindeReportDialog.getInstance().isSumGu()) {
                if (KatasterGemeindeReportDialog.getInstance().isPerWdm()) {
                    dataSources.put("gewaesserGuAbschnitt", getGewaesserGuWidmung());
                } else {
                    dataSources.put("gewaesserGu", getGewaesserGu());
                }
            }
        }

        if (KatasterGemeindeReportDialog.getInstance().isSumGu()) {
            if (KatasterGemeindeReportDialog.getInstance().isPerWdm()) {
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

        final File file = new File(
                KatasterGemeindeReportDialog.getInstance().getPath()
                        + "/Kataster_Gemeinden.xlsx");
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
        config.setIgnoreCellBackground(false);
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
        final KatasterGemeindeReport report = new KatasterGemeindeReport();
//        try {
//            report.createGemeindeReport(new int[] { 2 }, new int[] { 2 });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr     DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     * @param   wd        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private boolean init(final int[] gemNr, final int[] routeIds, final WaitDialog wd) throws Exception {
        wd.setMax(gemNr.length + 1);
        wd.setProgress(0);
        for (final int gem : gemNr) {
            final List<GmdPartObj> parts = mergeRoutes(getAllRoutes(gem, routeIds));
            gemPartMap.put(gem, parts);

            final Integer[] idList = getGew(gem).toArray(new Integer[0]);
            int[] routes = new int[idList.length];

            for (int i = 0; i < idList.length; ++i) {
                routes[i] = idList[i];
            }

            if (routes.length == 0) {
                routes = null;
            }
            final GemeindenData gemData = new GemeindenData(gem, routes);
            gemDataMap.put(gem, gemData);

            // cut sea parts
            final List<GmdPartObj> newParts = new ArrayList<GmdPartObj>();

            for (final GmdPartObj tmp : parts) {
                final double length = gemData.getLength(
                        GemeindenData.LineFromPolygonTable.sg_see,
                        tmp.getId(),
                        tmp.getFrom(),
                        tmp.getTill());
                if (!tmp.getArt().equals("g") && (length >= 0.01)) {
                    if (length == Math.abs(tmp.getTill() - tmp.getFrom())) {
                        tmp.setArt("s");
                        newParts.add(tmp);
                    } else {
                        final List<GemeindenData.LineObjectData> seaParts = gemData.getLengthFromTill(
                                GemeindenData.LineFromPolygonTable.sg_see,
                                tmp.getId(),
                                tmp.getFrom(),
                                tmp.getTill());
                        double lastEnd = tmp.getFrom();

                        for (final GemeindenData.LineObjectData line : seaParts) {
                            if (line.getFrom() > lastEnd) {
                                final GmdPartObj obj = new GmdPartObj(tmp);
                                obj.setFrom(lastEnd);
                                obj.setTill(line.getFrom());
                                newParts.add(obj);
                                lastEnd = line.getFrom();
                            }
                            final GmdPartObj obj = new GmdPartObj(tmp);
                            obj.setFrom(lastEnd);
                            obj.setTill(line.getTo());
                            obj.setArt("s");
                            newParts.add(obj);
                            lastEnd = line.getTo();
                        }
                        if (lastEnd < tmp.getTill()) {
                            final GmdPartObj obj = new GmdPartObj(tmp);
                            obj.setFrom(lastEnd);
                            newParts.add(obj);
                        }
                    }
                } else {
                    newParts.add(tmp);
                }
            }
            gemPartMap.put(gem, newParts);
            final int progress = wd.getProgress() + 1;
            if (Thread.interrupted()) {
                return false;
            }
            wd.setText("Erstelle (Gemeinden) " + progress + "/" + gemNr.length);
            wd.setProgress(progress);
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   routes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<GmdPartObj> mergeRoutes(final List<GmdPartObj> routes) {
        final List<GmdPartObj> newRoutes = new ArrayList<GmdPartObj>();
        if ((routes == null) || (routes.size() < 2)) {
            return routes;
        }
        GmdPartObj lastRoute = routes.get(0);

        for (int i = 1; i < routes.size(); ++i) {
            final GmdPartObj tmp = routes.get(i);

            if (lastRoute.getArt().equals(tmp.getArt()) && (lastRoute.getId() == tmp.getId())
                        && (lastRoute.getTill() > (tmp.getFrom() - 0.01))
                        && (lastRoute.getNr_li() == tmp.getNr_li())
                        && (lastRoute.getNr_re() == tmp.getNr_re())) {
                lastRoute.setTill(tmp.getTill());
            } else {
                newRoutes.add(lastRoute);
                lastRoute = tmp;
            }
        }

        newRoutes.add(lastRoute);

        return newRoutes;
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
    private List<GmdPartObj> getAllRoutes(final int gemId, final int[] routeIds) throws Exception {
        final CidsServerSearch search = new AllGewByGem(gemId, routeIds, getAllowedWdms());
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);
        final List<GmdPartObj> objList = new ArrayList<GmdPartObj>();

        if ((attributes != null) && !attributes.isEmpty()) {
            for (final ArrayList f : attributes) {
                objList.add(new GmdPartObj(
                        (Integer)f.get(0),
                        (String)f.get(1),
                        (String)f.get(7),
                        (String)f.get(8),
                        (String)f.get(9),
                        (Integer)f.get(10),
                        (String)f.get(2),
                        (Double)f.get(3),
                        (Double)f.get(4),
                        (Integer)f.get(5),
                        (Integer)f.get(6),
                        (Double)f.get(11),
                        (Double)f.get(12)));
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

        if (KatasterGemeindeReportDialog.getInstance().is1501()) {
            wdmList.add(1501);
        }

        if (KatasterGemeindeReportDialog.getInstance().is1502()) {
            wdmList.add(1502);
        }

        if (KatasterGemeindeReportDialog.getInstance().is1503()) {
            wdmList.add(1503);
        }

        if (KatasterGemeindeReportDialog.getInstance().is1504()) {
            wdmList.add(1504);
        }

        if (KatasterGemeindeReportDialog.getInstance().is1505()) {
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
        final TreeSet<Integer> set = new TreeSet<Integer>(new Comparator<Integer>() {

                    @Override
                    public int compare(final Integer o1, final Integer o2) {
                        return gemDataMap.get(o1).getGmdName().compareTo(gemDataMap.get(o2).getGmdName());
                    }
                });
        set.addAll(gemDataMap.keySet());

        for (final Integer gem : set) {
            final Map<String, Object> feature = new HashMap<String, Object>();
            feature.put("name", gemDataMap.get(gem).getGmdName());
            feature.put("nummer", gem);
            feature.put("gew_a", toNullIfZero(getLengthGewAll(gem) * 100 / getLengthGewAll()));
            feature.put("gew_l", toNullIfZero(getLengthGewAll(gem)));
            final double lengthSee = getLengthSeeAbschn(gem);
            feature.put(
                "offene_a",
                toNullIfZero(
                    (getLengthGewAll(gem) - getLengthGeschlAbschn(gem) - lengthSee)
                            * 100
                            / (getLengthGewAll(gem))));
            feature.put(
                "offene_l",
                toNullIfZero(getLengthGewAll(gem) - getLengthGeschlAbschn(gem) - lengthSee));
            feature.put(
                "see_a",
                toNullIfZero(lengthSee * 100 / (getLengthGewAll(gem))));
            feature.put("see_l", toNullIfZero(lengthSee));
            feature.put(
                "geschl_a",
                toNullIfZero(
                    getLengthGeschlAbschn(gem)
                            * 100
                            / (getLengthGewAll(gem))));
            feature.put("geschl_l", toNullIfZero(getLengthGeschlAbschn(gem)));
            feature.put(
                "wschutz_a",
                toNullIfZero(getCountLineObjectsAll(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem)));
            feature.put(
                "wschutz_l",
                toNullIfZero(getLengthLineObjectsAll(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem)));
            feature.put(
                "ueber_a",
                toNullIfZero(getCountLineObjectsAll(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem)));
            feature.put(
                "ueber_l",
                toNullIfZero(getLengthLineObjectsAll(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem)));
            feature.put("ben_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.wr_wbu_ben, gem)));
            feature.put("aus_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.wr_wbu_aus, gem)));
            feature.put("pegel_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.mn_ow_pegel, gem)));
            feature.put("gb_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_gb, gem)));
            feature.put("gb_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_gb, gem)));
            feature.put("sb_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_sb, gem)));
            feature.put("sb_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_sb, gem)));
            feature.put(
                "prof_a",
                toNullIfZero(
                    percentage(
                        getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_prof, gem),
                        getLengthGewAll(gem)
                                - getLengthGeschlAbschn(gem))));
            feature.put("prof_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_prof, gem)));
            feature.put("sbef_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_sbef, gem)));
            feature.put("sbef_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_sbef, gem)));
            feature.put("ubef_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_ubef, gem)));
            feature.put("ubef_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_ubef, gem)));
            feature.put("bbef_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_bbef, gem)));
            feature.put("bbef_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_bbef, gem)));
            feature.put("rl_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_rl, gem)));
            feature.put("rl_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_rl, gem)));
            feature.put("d_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_d, gem)));
            feature.put("d_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_d, gem)));
            feature.put("due_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_due, gem)));
            feature.put("due_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_due, gem)));
            feature.put("scha_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_scha, gem)));
            feature.put("wehr_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_wehr, gem)));
            feature.put("schw_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_schw, gem)));
            feature.put("foto_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.foto, gem)));
            feature.put("anlp_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_anlp, gem)));
            feature.put("anll_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_anll, gem)));
            feature.put("anll_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_anll, gem)));
            feature.put("kr_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_kr, gem)));
            feature.put("ea_a", toNullIfZero(getCountPointObjectsAll(AllPunktObjects.Table.fg_ba_ea, gem)));
            feature.put("deich_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.deich_ft, gem)));
            feature.put("deich_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.deich_ft, gem)));
            feature.put("ughz_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_ughz, gem)));
            feature.put("ughz_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_ughz, gem)));
            feature.put("leis_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_leis, gem)));
            feature.put("leis_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_leis, gem)));
            feature.put("tech_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_tech, gem)));
            feature.put("tech_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_tech, gem)));
            feature.put("dok_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_doku, gem)));
            feature.put("dok_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_doku, gem)));
            feature.put("proj_a", toNullIfZero(getCountLineObjectsAll(AllLineObjects.Table.fg_ba_proj, gem)));
            feature.put("proj_l", toNullIfZero(getLengthLineObjectsAll(AllLineObjects.Table.fg_ba_proj, gem)));

            features.add(feature);
        }
        features.add(createKumFeature(features, false, true));

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   part   DOCUMENT ME!
     * @param   total  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double percentage(final double part, final double total) {
        if (part == 0.0) {
            return 0.0;
        } else if (total == 0.0) {
            return 0.0;
        }

        return part * 100 / total;
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
        final TreeSet<Integer> set = new TreeSet<Integer>(new Comparator<Integer>() {

                    @Override
                    public int compare(final Integer o1, final Integer o2) {
                        return gemDataMap.get(o1).getGmdName().compareTo(gemDataMap.get(o2).getGmdName());
                    }
                });
        set.addAll(gemDataMap.keySet());

        for (final Integer gem : set) {
            if (getGew(gem).isEmpty()) {
                continue;
            }
            sheetNames.add(gemDataMap.get(gem).getGmdName());
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            for (final int gew : getGew(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                feature.put("gmdNummer", gem);
                feature.put("anzahlGew", getCountGewAll(gem));
                feature.put("group", gem);
                feature.put("code", getBaCd(gem, gew));
                feature.put("gewName", getGewName(gem, gew));
                feature.put("gewLaenge", getLengthGew(gem, gew));
                feature.put("offene_a", getCountOffeneAbschn(gem, gew));
                feature.put("offene_l", getLengthOffeneAbschn(gem, gew));
                feature.put("see_a", getCountLineObjects(GemeindenData.LineFromPolygonTable.sg_see, gem, gew));
                feature.put("see_l", getLengthLineObjects(GemeindenData.LineFromPolygonTable.sg_see, gem, gew));
                feature.put("geschl_a", getCountGeschlAbschn(gem, gew));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, gew));
                feature.put("wschutz_a", getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem, gew));
                feature.put("wschutz_l", getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem, gew));
                feature.put("ueber_a", getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem, gew));
                feature.put("ueber_l", getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem, gew));
                feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, gem, gew));
                feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, gem, gew));
                feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, gem, gew));
                feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gem, gew));
                feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, gem, gew));
                feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gem, gew));
                feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, gem, gew));
                feature.put(
                    "prof_a",
                    percentage(
                        getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, gem, gew),
                        getLengthGew(gem, gew)
                                - getLengthGeschlAbschn(gem, gew)));
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
                feature.put("foto_a", getCountPointObjects(AllPunktObjects.Table.foto, gem, gew));
                feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, gem, gew));
                feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, gem, gew));
                feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, gem, gew));
                feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, gem, gew));
                feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, gem, gew));
                feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.deich_ft, gem, gew));
                feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.deich_ft, gem, gew));
                feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, gew));
                feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, gew));
                feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, gem, gew));
                feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, gem, gew));
                feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, gem, gew));
                feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, gem, gew));
                feature.put("dok_a", getCountLineObjects(AllLineObjects.Table.fg_ba_doku, gem, gew));
                feature.put("dok_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_doku, gem, gew));
                feature.put("proj_a", getCountLineObjects(AllLineObjects.Table.fg_ba_proj, gem, gew));
                feature.put("proj_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_proj, gem, gew));

                features.add(feature);
                featureListKum.add(feature);
            }
            features.add(createKumFeature(featureListKum, false, false));
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

        for (final Integer gem : gemDataMap.keySet()) {
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            final List<Map<String, Object>> featureListGewKum = new ArrayList<Map<String, Object>>();
            String code = null;
            if (gemPartMap.get(gem).isEmpty()) {
                continue;
            }
            sheetNames.add(gemDataMap.get(gem).getGmdName());
            for (final GmdPartObj gew : gemPartMap.get(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                feature.put("gmdNummer", gem);
                feature.put("anzahlGew", getCountGewAll(gem));
                feature.put("anzahlAbschn", gemPartMap.get(gem).size());
                feature.put("group", gem);
                feature.put("code", getBaCd(gem, gew.getId()));
                feature.put("gewName", gew.getGewName());
                feature.put("von", convertStation(gew.getFrom()));
                feature.put("bis", convertStation(gew.getTill()));
                feature.put("laenge", gew.getLength());
                feature.put("gewLaenge", gew.getLength());
                feature.put("offene_a", getCountOffeneAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put("offene_l", getLengthOffeneAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "see_a",
                    getCountLineObjects(
                        GemeindenData.LineFromPolygonTable.sg_see,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "see_l",
                    getLengthLineObjects(
                        GemeindenData.LineFromPolygonTable.sg_see,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put("geschl_a", getCountGeschlAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill()));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(
                        GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(
                        GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ueber_a",
                    getCountLineObjects(
                        GemeindenData.LineFromPolygonTable.wr_sg_uesg,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(
                        GemeindenData.LineFromPolygonTable.wr_sg_uesg,
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
                    percentage(
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_prof,
                            gem,
                            gew.getId(),
                            gew.getFrom(),
                            gew.getTill()),
                        gew.getLength()
                                - getLengthGeschlAbschn(gem, gew.getId(), gew.getFrom(), gew.getTill())));
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
                    "foto_a",
                    getCountPointObjects(AllPunktObjects.Table.foto,
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
                        AllLineObjects.Table.deich_ft,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "deich_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.deich_ft,
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
                feature.put(
                    "dok_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_doku,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "dok_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_doku,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "proj_a",
                    getCountLineObjects(
                        AllLineObjects.Table.fg_ba_proj,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));
                feature.put(
                    "proj_l",
                    getLengthLineObjects(
                        AllLineObjects.Table.fg_ba_proj,
                        gem,
                        gew.getId(),
                        gew.getFrom(),
                        gew.getTill()));

                final String newCode = getBaCd(gem, gew.getId());

                if ((code != null) && !code.equals(newCode)) {
                    final Map<String, Object> kumObj = createKumFeature(featureListGewKum, true, false);
                    kumObj.remove("von");
                    kumObj.remove("bis");
                    kumObj.remove("gewLaenge");
                    features.add(kumObj);
                    featureListGewKum.clear();
                }

                code = newCode;
                features.add(feature);
                featureListKum.add(feature);
                featureListGewKum.add(feature);
            }
            // generate the last sub total (start)
            Map<String, Object> kumObj = createKumFeature(featureListGewKum, true, false);
            kumObj.remove("von");
            kumObj.remove("bis");
            kumObj.remove("gewLaenge");
            features.add(kumObj);
            featureListGewKum.clear();
            // generate the last sub total (end)

            kumObj = createKumFeature(featureListKum, false, false);
            kumObj.remove("code");
            kumObj.remove("gewName");
            kumObj.remove("von");
            kumObj.remove("bis");
            kumObj.remove("gewLaenge");
            features.add(kumObj);

            for (final Map<String, Object> f : features) {
                if (f.get("summe") == null) {
                    f.remove("code");
                    f.remove("gewName");
                    f.remove("gewLaenge");
                }
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
    private FeatureDataSource getGewaesserGu() throws Exception {
        final List<Map<String, Object>> features = new ArrayList<Map<String, Object>>();

        for (final Integer gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getGmdName());
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();
            for (final String guName : getGu(gem)) {
                final Map<String, Object> feature = new HashMap<String, Object>();
                feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                feature.put("gmdNummer", gem);
                feature.put("anzahlGu", getCountGu(gem));
                feature.put("group", gem);
                feature.put("gu", getGuId(gem, guName));
                feature.put("guName", guName);
                feature.put("gewAnzahl", getCountGew(gem, guName));
                feature.put("gewLaenge", getLengthGew(gem, guName));
                feature.put("offene_a", getCountOffeneAbschn(gem, guName));
                feature.put("offene_l", getLengthOffeneAbschn(gem, guName));
                feature.put("see_a", getCountLineObjects(GemeindenData.LineFromPolygonTable.sg_see, gem, guName));
                feature.put("see_l", getLengthLineObjects(GemeindenData.LineFromPolygonTable.sg_see, gem, guName));
                feature.put("geschl_a", getCountGeschlAbschn(gem, guName));
                feature.put("geschl_l", getLengthGeschlAbschn(gem, guName));
                feature.put(
                    "wschutz_a",
                    getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem, guName));
                feature.put(
                    "wschutz_l",
                    getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, gem, guName));
                feature.put("ueber_a", getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem, guName));
                feature.put(
                    "ueber_l",
                    getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, gem, guName));
                feature.put("ben_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, gem, guName));
                feature.put("aus_a", getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, gem, guName));
                feature.put("pegel_a", getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, gem, guName));
                feature.put("gb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_gb, gem, guName));
                feature.put("gb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, gem, guName));
                feature.put("sb_a", getCountLineObjects(AllLineObjects.Table.fg_ba_sb, gem, guName));
                feature.put("sb_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, gem, guName));
                feature.put(
                    "prof_a",
                    percentage(
                        getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, gem, guName),
                        getLengthGew(gem, guName)
                                - getLengthGeschlAbschn(gem, guName)));
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
                feature.put("foto_a", getCountPointObjects(AllPunktObjects.Table.foto, gem, guName));
                feature.put("anlp_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, gem, guName));
                feature.put("anll_a", getCountLineObjects(AllLineObjects.Table.fg_ba_anll, gem, guName));
                feature.put("anll_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, gem, guName));
                feature.put("kr_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, gem, guName));
                feature.put("ea_a", getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, gem, guName));
                feature.put("deich_a", getCountLineObjects(AllLineObjects.Table.deich_ft, gem, guName));
                feature.put("deich_l", getLengthLineObjects(AllLineObjects.Table.deich_ft, gem, guName));
                feature.put("ughz_a", getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, guName));
                feature.put("ughz_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, gem, guName));
                feature.put("leis_a", getCountLineObjects(AllLineObjects.Table.fg_ba_leis, gem, guName));
                feature.put("leis_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, gem, guName));
                feature.put("tech_a", getCountLineObjects(AllLineObjects.Table.fg_ba_tech, gem, guName));
                feature.put("tech_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, gem, guName));
                feature.put("dok_a", getCountLineObjects(AllLineObjects.Table.fg_ba_doku, gem, guName));
                feature.put("dok_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_doku, gem, guName));
                feature.put("proj_a", getCountLineObjects(AllLineObjects.Table.fg_ba_proj, gem, guName));
                feature.put("proj_l", getLengthLineObjects(AllLineObjects.Table.fg_ba_proj, gem, guName));

                features.add(feature);
                featureListKum.add(feature);
            }
            features.add(createKumFeature(featureListKum, false, false));
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

        for (final Integer gem : gemDataMap.keySet()) {
            sheetNames.add("GU " + gemDataMap.get(gem).getGmdName());
            final List<Map<String, Object>> featureListKum = new ArrayList<Map<String, Object>>();

            for (final String guName : getGu(gem)) {
                final List<Map<String, Object>> featureListGuKum = new ArrayList<Map<String, Object>>();

                for (final Integer wdm : getWidmung(gem, guName)) {
                    final Map<String, Object> feature = new HashMap<String, Object>();
                    feature.put("gmdName", gemDataMap.get(gem).getGmdName());
                    feature.put("gmdNummer", gem);
                    feature.put("anzahlGu", getCountGu(gem));
                    feature.put("group", gem);
                    feature.put("gu", getGuId(gem, guName));
                    feature.put("guName", guName);
                    feature.put("widmung", String.valueOf(wdm));
                    feature.put("gew_a", getCountGew(gem, guName, wdm));
                    feature.put("gew_l", getLengthGew(gem, guName, wdm));
                    feature.put("offene_a", getCountOffeneAbschn(gem, guName, wdm));
                    feature.put("offene_l", getLengthOffeneAbschn(gem, guName, wdm));
                    feature.put(
                        "see_a",
                        getCountLineObjects(
                            GemeindenData.LineFromPolygonTable.sg_see,
                            gem,
                            wdm));
                    feature.put(
                        "see_l",
                        getLengthLineObjects(
                            GemeindenData.LineFromPolygonTable.sg_see,
                            gem,
                            wdm));
                    feature.put("geschl_a", getCountGeschlAbschn(gem, guName, wdm));
                    feature.put("geschl_l", getLengthGeschlAbschn(gem, guName, wdm));
                    feature.put(
                        "wschutz_a",
                        getCountLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                            gem,
                            wdm));
                    feature.put(
                        "wschutz_l",
                        getLengthLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                            gem,
                            wdm));
                    feature.put(
                        "ueber_a",
                        getCountLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_uesg,
                            gem,
                            wdm));
                    feature.put(
                        "ueber_l",
                        getLengthLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_uesg,
                            gem,
                            wdm));
                    feature.put(
                        "ben_a",
                        getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben,
                            gem,
                            wdm));
                    feature.put(
                        "aus_a",
                        getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus,
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
                        percentage(
                            getLengthLineObjects(
                                AllLineObjects.Table.fg_ba_prof,
                                gem,
                                wdm),
                            getLengthGew(gem, wdm)
                                    - getLengthGeschlAbschn(gem, wdm)));
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
                        "foto_a",
                        getCountPointObjects(AllPunktObjects.Table.foto,
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
                            AllLineObjects.Table.deich_ft,
                            gem,
                            wdm));
                    feature.put(
                        "deich_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.deich_ft,
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
                    feature.put(
                        "dok_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_doku,
                            gem,
                            wdm));
                    feature.put(
                        "dok_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_doku,
                            gem,
                            wdm));
                    feature.put(
                        "proj_a",
                        getCountLineObjects(
                            AllLineObjects.Table.fg_ba_proj,
                            gem,
                            wdm));
                    feature.put(
                        "proj_l",
                        getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_proj,
                            gem,
                            wdm));

                    features.add(feature);
                    featureListKum.add(feature);
                    featureListGuKum.add(feature);
                }
                features.add(createKumFeature(featureListGuKum, true, false));
            }
            features.add(createKumFeature(featureListKum, false, false));
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
        final List<Map<String, Object>> features = new ArrayList<>();

        sheetNames.add("GU");
        final List<Map<String, Object>> featureListKum = new ArrayList<>();
        for (final String guName : getGu()) {
            final Map<String, Object> feature = new HashMap<>();
            feature.put("anzahlGu", getCountGu());
            feature.put("group", 1);
            feature.put("gu", getGuId(guName));
            feature.put("guName", guName);
            feature.put("gewAnzahl", getCountGew(guName));
            feature.put("gewLaenge", getLengthGew(guName));
            final double lengthSee = getLengthLineObjects(GemeindenData.LineFromPolygonTable.sg_see, guName);

            feature.put(
                "offene_a",
                toNullIfZero(
                    (getLengthGew(guName) - getLengthGeschlAbschn(guName) - lengthSee)
                            * 100
                            / (getLengthGew(guName))));
            feature.put(
                "offene_l",
                toNullIfZero(getLengthGew(guName) - getLengthGeschlAbschn(guName) - lengthSee));
            feature.put(
                "see_a",
                toNullIfZero(lengthSee * 100 / (getLengthGew(guName))));
            feature.put("see_l", toNullIfZero(lengthSee));

            feature.put(
                "geschl_a",
                toNullIfZero(
                    getLengthGeschlAbschn(guName)
                            * 100
                            / (getLengthGew(guName))));
            feature.put("geschl_l", toNullIfZero(getLengthGeschlAbschn(guName)));
            feature.put(
                "wschutz_a",
                toNullIfZero(getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, guName)));
            feature.put(
                "wschutz_l",
                toNullIfZero(getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_wsg, guName)));
            feature.put(
                "ueber_a",
                toNullIfZero(getCountLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, guName)));
            feature.put(
                "ueber_l",
                toNullIfZero(getLengthLineObjects(GemeindenData.LineFromPolygonTable.wr_sg_uesg, guName)));
            feature.put("ben_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben, guName)));
            feature.put("aus_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus, guName)));
            feature.put("pegel_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.mn_ow_pegel, guName)));
            feature.put("gb_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_gb, guName)));
            feature.put("gb_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_gb, guName)));
            feature.put("sb_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_sb, guName)));
            feature.put("sb_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_sb, guName)));
            feature.put(
                "prof_a",
                toNullIfZero(
                    percentage(
                        getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, guName),
                        getLengthGew(guName)
                                - getLengthGeschlAbschn(guName))));
            feature.put("prof_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_prof, guName)));
            feature.put("sbef_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_sbef, guName)));
            feature.put("sbef_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_sbef, guName)));
            feature.put("ubef_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_ubef, guName)));
            feature.put("ubef_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_ubef, guName)));
            feature.put("bbef_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_bbef, guName)));
            feature.put("bbef_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_bbef, guName)));
            feature.put("rl_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_rl, guName)));
            feature.put("rl_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_rl, guName)));
            feature.put("d_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_d, guName)));
            feature.put("d_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_d, guName)));
            feature.put("due_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_due, guName)));
            feature.put("due_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_due, guName)));
            feature.put("scha_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_scha, guName)));
            feature.put("wehr_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_wehr, guName)));
            feature.put("schw_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_schw, guName)));
            feature.put("foto_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.foto, guName)));
            feature.put("anlp_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_anlp, guName)));
            feature.put("anll_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_anll, guName)));
            feature.put("anll_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_anll, guName)));
            feature.put("kr_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_kr, guName)));
            feature.put("ea_a", toNullIfZero(getCountPointObjects(AllPunktObjects.Table.fg_ba_ea, guName)));
            feature.put("deich_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.deich_ft, guName)));
            feature.put("deich_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.deich_ft, guName)));
            feature.put("ughz_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_ughz, guName)));
            feature.put("ughz_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_ughz, guName)));
            feature.put("leis_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_leis, guName)));
            feature.put("leis_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_leis, guName)));
            feature.put("tech_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_tech, guName)));
            feature.put("tech_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_tech, guName)));
            feature.put("dok_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_doku, guName)));
            feature.put("dok_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_doku, guName)));
            feature.put("proj_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_proj, guName)));
            feature.put("proj_l", toNullIfZero(getLengthLineObjects(AllLineObjects.Table.fg_ba_proj, guName)));

            features.add(feature);
            featureListKum.add(feature);
        }
        features.add(createKumFeature(featureListKum, false, false));

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
                feature.put("widmung", String.valueOf(wdm));
                feature.put("gew_a", getCountGew(guName, wdm));
                feature.put("gew_l", getLengthGew(guName, wdm));
                final double lengthSee = getLengthLineObjects(GemeindenData.LineFromPolygonTable.sg_see, guName, wdm);

                feature.put(
                    "offene_a",
                    toNullIfZero(
                        (getLengthGew(guName, wdm) - getLengthGeschlAbschn(guName, wdm) - lengthSee)
                                * 100
                                / (getLengthGew(guName, wdm))));
                feature.put(
                    "offene_l",
                    toNullIfZero(getLengthGew(guName, wdm) - getLengthGeschlAbschn(guName, wdm) - lengthSee));
                feature.put(
                    "see_a",
                    toNullIfZero(lengthSee * 100 / (getLengthGew(guName, wdm))));
                feature.put("see_l", toNullIfZero(lengthSee));

                feature.put(
                    "geschl_a",
                    toNullIfZero(
                        getLengthGeschlAbschn(guName, wdm)
                                * 100
                                / (getLengthGew(guName, wdm))));
                feature.put("geschl_l", toNullIfZero(getLengthGeschlAbschn(guName, wdm)));
                feature.put(
                    "wschutz_a",
                    toNullIfZero(getCountLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                            guName,
                            wdm)));
                feature.put(
                    "wschutz_l",
                    toNullIfZero(getLengthLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_wsg,
                            guName,
                            wdm)));
                feature.put(
                    "ueber_a",
                    toNullIfZero(getCountLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_uesg,
                            guName,
                            wdm)));
                feature.put(
                    "ueber_l",
                    toNullIfZero(getLengthLineObjects(
                            GemeindenData.LineFromPolygonTable.wr_sg_uesg,
                            guName,
                            wdm)));
                feature.put(
                    "ben_a",
                    toNullIfZero(getCountPointObjects(AllPunktObjects.Table.wr_wbu_ben,
                            guName,
                            wdm)));
                feature.put(
                    "aus_a",
                    toNullIfZero(getCountPointObjects(AllPunktObjects.Table.wr_wbu_aus,
                            guName,
                            wdm)));
                feature.put(
                    "pegel_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.mn_ow_pegel,
                            guName,
                            wdm)));
                feature.put(
                    "gb_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_gb,
                            guName,
                            wdm)));
                feature.put(
                    "gb_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_gb,
                            guName,
                            wdm)));
                feature.put(
                    "sb_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_sb,
                            guName,
                            wdm)));
                feature.put(
                    "sb_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_sb,
                            guName,
                            wdm)));
                feature.put(
                    "prof_a",
                    toNullIfZero(
                        percentage(
                            getLengthLineObjects(
                                AllLineObjects.Table.fg_ba_prof,
                                guName,
                                wdm),
                            getLengthGew(guName, wdm)
                                    - getLengthGeschlAbschn(guName, wdm))));
                feature.put(
                    "prof_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_prof,
                            guName,
                            wdm)));
                feature.put(
                    "sbef_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_sbef,
                            guName,
                            wdm)));
                feature.put(
                    "sbef_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_sbef,
                            guName,
                            wdm)));
                feature.put(
                    "ubef_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_ubef,
                            guName,
                            wdm)));
                feature.put(
                    "ubef_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_ubef,
                            guName,
                            wdm)));
                feature.put(
                    "bbef_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_bbef,
                            guName,
                            wdm)));
                feature.put(
                    "bbef_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_bbef,
                            guName,
                            wdm)));
                feature.put(
                    "rl_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_rl,
                            guName,
                            wdm)));
                feature.put(
                    "rl_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_rl,
                            guName,
                            wdm)));
                feature.put(
                    "d_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_d,
                            guName,
                            wdm)));
                feature.put(
                    "d_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_d,
                            guName,
                            wdm)));
                feature.put(
                    "due_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_due,
                            guName,
                            wdm)));
                feature.put(
                    "due_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_due,
                            guName,
                            wdm)));
                feature.put(
                    "scha_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_scha,
                            guName,
                            wdm)));
                feature.put(
                    "wehr_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_wehr,
                            guName,
                            wdm)));
                feature.put(
                    "schw_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_schw,
                            guName,
                            wdm)));
                feature.put(
                    "foto_a",
                    toNullIfZero(getCountPointObjects(AllPunktObjects.Table.foto,
                            guName,
                            wdm)));
                feature.put(
                    "anlp_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_anlp,
                            guName,
                            wdm)));
                feature.put(
                    "anll_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_anll,
                            guName,
                            wdm)));
                feature.put(
                    "anll_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_anll,
                            guName,
                            wdm)));
                feature.put(
                    "kr_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_kr,
                            guName,
                            wdm)));
                feature.put(
                    "ea_a",
                    toNullIfZero(getCountPointObjects(
                            AllPunktObjects.Table.fg_ba_ea,
                            guName,
                            wdm)));
                feature.put(
                    "deich_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.deich_ft,
                            guName,
                            wdm)));
                feature.put(
                    "deich_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.deich_ft,
                            guName,
                            wdm)));
                feature.put(
                    "ughz_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_ughz,
                            guName,
                            wdm)));
                feature.put(
                    "ughz_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_ughz,
                            guName,
                            wdm)));
                feature.put(
                    "leis_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_leis,
                            guName,
                            wdm)));
                feature.put(
                    "leis_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_leis,
                            guName,
                            wdm)));
                feature.put(
                    "tech_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_tech,
                            guName,
                            wdm)));
                feature.put(
                    "tech_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_tech,
                            guName,
                            wdm)));
                feature.put(
                    "dok_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_doku,
                            guName,
                            wdm)));
                feature.put(
                    "dok_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_doku,
                            guName,
                            wdm)));
                feature.put(
                    "proj_a",
                    toNullIfZero(getCountLineObjects(
                            AllLineObjects.Table.fg_ba_proj,
                            guName,
                            wdm)));
                feature.put(
                    "proj_l",
                    toNullIfZero(getLengthLineObjects(
                            AllLineObjects.Table.fg_ba_proj,
                            guName,
                            wdm)));

                features.add(feature);
                featureListKum.add(feature);
                featureListGuKum.add(feature);
            }
            features.add(createKumFeature(featureListGuKum, true, false));
        }
        features.add(createKumFeature(featureListKum, false, false));
//        }
        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   featureListKum  DOCUMENT ME!
     * @param   subtotal        DOCUMENT ME!
     * @param   isGeschDouble   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Map<String, Object> createKumFeature(final List<Map<String, Object>> featureListKum,
            final boolean subtotal,
            final boolean isGeschDouble) {
        final Map<String, Object> kumFeature = new HashMap<String, Object>();

        kumFeature.put("summe", Boolean.TRUE);
        kumFeature.put("zwischenSumme", subtotal);

        if ((featureListKum == null) || featureListKum.isEmpty()) {
            return kumFeature;
        }

        final Map<String, Object> firstElement = featureListKum.get(0);

        for (final String key : firstElement.keySet()) {
            final Object value = firstElement.get(key);
            Object firstNonNullvalue = firstElement.get(key);

            if (firstNonNullvalue == null) {
                for (final Map<String, Object> f : featureListKum) {
                    if (f.get(key) != null) {
                        firstNonNullvalue = f.get(key);
                        break;
                    }
                }
            }

            if ((value == null) && (firstNonNullvalue == null)) {
                continue;
            }

            if ((key != null) && key.equalsIgnoreCase("group")) {
                kumFeature.put(key, value);
            } else if ((Arrays.binarySearch(exceptionalFields, key) < 0) && (firstNonNullvalue instanceof Integer)) {
                int sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    if (f.get(key) instanceof Number) {
                        sum += (Integer)f.get(key);
                    }
                }

                kumFeature.put(key, sum);
            } else if ((Arrays.binarySearch(exceptionalFields, key) < 0) && (firstNonNullvalue instanceof Double)) {
                double sum = 0;

                for (final Map<String, Object> f : featureListKum) {
                    if (f.get(key) instanceof Number) {
                        sum += (Double)f.get(key);
                    }
                }

                kumFeature.put(key, sum);
            } else if (Arrays.binarySearch(exceptionalFields, key) < 0) {
                kumFeature.put(key, value);
            }
        }

        if (isGeschDouble) {
            Double offene = (Double)kumFeature.get("offene_l");
            Double geschl = (Double)kumFeature.get("geschl_l");
            Double see = (Double)kumFeature.get("see_l");
            Double prof = (Double)kumFeature.get("prof_l");

            if (offene == null) {
                offene = 0.0;
            }
            if (see == null) {
                see = 0.0;
            }
            if (geschl == null) {
                geschl = 0.0;
            }
            if (prof == null) {
                prof = 0.0;
            }

            kumFeature.put("offene_a", toNullIfZero(offene * 100.0 / (offene + geschl + see)));
            kumFeature.put("see_a", toNullIfZero(see * 100.0 / (offene + geschl + see)));
            kumFeature.put("geschl_a", toNullIfZero(geschl * 100.0 / (offene + geschl + see)));
            if (offene == 0.0) {
                kumFeature.put("prof_a", 0.0);
            } else {
                kumFeature.put("prof_a", toNullIfZero(prof * 100.0 / (offene + see)));
            }
        }

        kumFeature.put("wschutz_a", null);
        kumFeature.put("ueber_a", null);
        kumFeature.put("sb_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_sb, null)));
        kumFeature.put("gb_a", toNullIfZero(getCountLineObjects(AllLineObjects.Table.fg_ba_gb, null)));
        kumFeature.put("sbef_a", null);
        kumFeature.put("ubef_a", null);
        kumFeature.put("bbef_a", null);
        kumFeature.put("rl_a", null);
        kumFeature.put("d_a", null);
        kumFeature.put("due_a", null);
        kumFeature.put("anll_a", null);
        kumFeature.put("deich_a", null);
        kumFeature.put("ughz_a", null);
        kumFeature.put("leis_a", null);
        kumFeature.put("tech_a", null);
        kumFeature.put("dok_a", null);
        kumFeature.put("proj_a", null);

        return kumFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object toNullIfZero(final Object o) {
        if (o instanceof Number) {
            if (((Number)o).doubleValue() == 0.0) {
                return null;
            }
        }

        return o;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getGew(final int gemNr) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObj tmp : gemList) {
            ts.add(tmp.getId());
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
    private List<GmdPartObj> getGew(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final List<GmdPartObj> list = new ArrayList<GmdPartObj>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                list.add(tmp);
            }
        }

        return list;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<String> getGu(final int gemNr) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObj tmp : gemList) {
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

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                ts.add(tmp.getOwner());
            }
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObj tmp : gemList) {
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
        for (final int gemNr : gemPartMap.keySet()) {
            final String tmp = getGuId(gemNr, owner);

            if (tmp != null) {
                return tmp;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gew    owner DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getWdm(final int gemNr, final int gew) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getId() == gew) {
                return tmp.getWidmung();
            }
        }

        return null;
    }

    /**
     * /** * DOCUMENT ME! * * @param gemNr DOCUMENT ME! * @param gew owner DOCUMENT ME! * * @return DOCUMENT ME!
     *
     * @param   gemNr   DOCUMENT ME!
     * @param   guName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<Integer> getWidmung(final int gemNr, final String guName) {
        final List<GmdPartObj> gmdList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ts = new TreeSet<Integer>();

        for (final GmdPartObj gmdPart : gmdList) {
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

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gmdList = gemPartMap.get(gemNr);

            for (final GmdPartObj gmdPart : gmdList) {
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
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGu(final int gemNr) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObj tmp : gemList) {
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

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

        for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<String> ts = new TreeSet<String>();

        for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            length += tmp.getLength();
        }

        return length;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGewAll() {
        double length = 0;

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGew(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(gu) && (tmp.getWidmung() == wdm)) {
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
    private int getCountGew(final int gemNr, final String gu, final int wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gemNr) {
        return getCountOffeneAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gemNr) {
        return getLengthOffeneAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthSeeAbschn(final int gemNr) {
        return getLengthSeeAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountSeeAbschn(final int gemNr) {
        return getCountSeeAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gemNr) {
        return getCountGeschlAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gemNr) {
        return getLengthGeschlAbschn(gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountSeeAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("s")) {
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
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthSeeAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (tmp.getId() == gewId)) {
                if (tmp.getArt().equals("s")) {
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
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gemNr, final int gewId) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((tmp.getId() == gewId) && tmp.isInGewPart(gewId, from, till)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getId() == gewId) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
                    length += tmp.getLengthInGewPart(gewId, from, till);
                }
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
    private int getCountGeschlAbschn(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gemNr, final int gewId, final double from, final double till) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gemNr, final String owner) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
    private int getCountOffeneAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final String owner, final int wdm) {
        int count = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final String owner, final int wdm) {
        double length = 0;

        for (final Integer gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountOffeneAbschn(final int gemNr, final String owner, final int wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthOffeneAbschn(final int gemNr, final String owner, final int wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                if ((tmp.getArt().equals("p") || tmp.getArt().equals("o")) && (tmp.getWidmung() == wdm)) {
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
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountGeschlAbschn(final int gemNr, final String owner, final int wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        int count = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthGeschlAbschn(final int gemNr, final String owner, final int wdm) {
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    if (tmp.getArt().equals("p") || tmp.getArt().equals("o")) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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

        for (final int gemNr : gemPartMap.keySet()) {
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final AllLineObjects.Table table, final int gemNr) {
        return getCountLineObjects(table, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final AllLineObjects.Table table, final int gemNr) {
        return getLengthLineObjects(table, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjectsAll(final GemeindenData.LineFromPolygonTable table, final int gemNr) {
        return getCountLineObjects(table, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjectsAll(final GemeindenData.LineFromPolygonTable table, final int gemNr) {
        return getLengthLineObjects(table, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjectsAll(final AllPunktObjects.Table table, final int gemNr) {
        return getCountPointObjects(table, gemNr, -1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final int gemNr, final int gewId) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> set = new TreeSet<Integer>();
        final int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final int gemNr, final int gewId) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table, final int gemNr, final int gewId) {
        if (gewId < 0) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            final TreeSet<Integer> set = new TreeSet<Integer>();

            for (final GmdPartObj tmp : gemList) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }

            return set.size();
        } else {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            final TreeSet<Integer> set = new TreeSet<Integer>();
            final int count = 0;

            for (final GmdPartObj tmp : gemList) {
                if ((gewId < 0) || (gewId == tmp.getId())) {
                    set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }

            return set.size();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final int gewId) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final int gemNr, final int gewId) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ids = new TreeSet<Integer>();
        final int count = 0;

        for (final GmdPartObj tmp : gemList) {
            if ((gewId < 0) || (gewId == tmp.getId())) {
                ids.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return ids.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ids  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String print(final TreeSet<Integer> ids) {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Integer> it = ids.descendingIterator();

        while (it.hasNext()) {
            sb.append(it.next() + "\n");
        }

        return sb.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final GemeindenData gemData = gemDataMap.get(gemNr);

        return gemData.getIds(table, gewId, from, till).size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final GemeindenData gemData = gemDataMap.get(gemNr);

        return gemData.getLength(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final GemeindenData gemData = gemDataMap.get(gemNr);

        return gemData.getIds(table, gewId, from, till).size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final GemeindenData gemData = gemDataMap.get(gemNr);

        return gemData.getLength(table, gewId, from, till);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   gewId  DOCUMENT ME!
     * @param   from   DOCUMENT ME!
     * @param   till   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final int gemNr,
            final int gewId,
            final double from,
            final double till) {
        final GemeindenData gemData = gemDataMap.get(gemNr);

        return gemData.getIds(table, gewId, from, till).size();
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
    private int getCountLineObjects(final AllLineObjects.Table table, final int gemNr, final String owner) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> set = new TreeSet<>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return set.size();
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
    private double getLengthLineObjects(final AllLineObjects.Table table, final int gemNr, final String owner) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final String owner) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return set.size();
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
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final String owner) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final int gemNr, final String owner) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ids = new TreeSet<>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner)) {
                ids.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return ids.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table,
            final int gemNr,
            final String owner,
            final int wdm) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table,
            final int gemNr,
            final String owner,
            final int wdm) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final String owner,
            final int wdm) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final int gemNr,
            final String owner,
            final int wdm) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        double length = 0;

        for (final GmdPartObj tmp : gemList) {
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
     * @param   gemNr  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table,
            final int gemNr,
            final String owner,
            final int wdm) {
        final GemeindenData gemData = gemDataMap.get(gemNr);
        final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
        final TreeSet<Integer> ids = new TreeSet<>();

        for (final GmdPartObj tmp : gemList) {
            if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                ids.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
            }
        }

        return ids.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final AllLineObjects.Table table, final String owner, final int wdm) {
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final Integer gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final AllLineObjects.Table table, final String owner, final int wdm) {
        double length = 0;

        for (final Integer gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table,
            final String owner,
            final int wdm) {
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final Integer gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final String owner,
            final int wdm) {
        double length = 0;

        for (final Integer gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
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
     * @param   wdm    gemNr DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getCountPointObjects(final AllPunktObjects.Table table, final String owner, final int wdm) {
        final TreeSet<Integer> ids = new TreeSet<>();

        for (final Integer gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);
            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner) && (tmp.getWidmung() == wdm)) {
                    ids.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }

        return ids.size();
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
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final int gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
                if ((owner == null) || tmp.getOwner().equals(owner)) {
                    set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }
        return set.size();
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

        for (final int gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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
    private int getCountLineObjects(final GemeindenData.LineFromPolygonTable table,
            final String owner) {
        final TreeSet<Integer> set = new TreeSet<Integer>();

        for (final int gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    set.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }

        return set.size();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   table  DOCUMENT ME!
     * @param   owner  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private double getLengthLineObjects(final GemeindenData.LineFromPolygonTable table,
            final String owner) {
        double length = 0;

        for (final int gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
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
        final TreeSet<Integer> ids = new TreeSet<>();

        for (final int gemNr : gemDataMap.keySet()) {
            final GemeindenData gemData = gemDataMap.get(gemNr);
            final List<GmdPartObj> gemList = gemPartMap.get(gemNr);

            for (final GmdPartObj tmp : gemList) {
                if (tmp.getOwner().equals(owner)) {
                    ids.addAll(gemData.getIds(table, tmp.getId(), tmp.getFrom(), tmp.getTill()));
                }
            }
        }

        return ids.size();
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
