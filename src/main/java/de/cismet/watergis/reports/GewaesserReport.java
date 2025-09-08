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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.linearref.LengthLocationMap;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;
import com.vividsolutions.jts.operation.buffer.BufferOp;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JRRewindableDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.math.RoundingMode;

import java.text.DateFormat;
import java.text.DecimalFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import de.cismet.cids.custom.watergis.server.search.SchuUeberReport;
import de.cismet.cids.custom.watergis.server.search.SchuWasserReport;
import de.cismet.cids.custom.watergis.server.search.VerknReport;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.HeadlessMapProvider;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultXStyledFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.piccolo.FeatureAnnotationSymbol;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.GewaesserReportDialog;
import de.cismet.watergis.gui.panels.GafProf;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GewaesserReport {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GewaesserReport.class);
    private static final MetaClass FG_BA = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba");
    private static final MetaClass FG_LA = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_la");
    private static final MetaClass FG_BAK_GBK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gbk");
    private static final MetaClass K_GWK_LAWA = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.k_gwk_lawa");
    private static final MetaClass FG_BA_STAT = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_stat");
    private static final MetaClass WR_WBU_BEN = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.wr_wbu_ben");
    private static final MetaClass WR_WBU_AUS = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.wr_wbu_aus");
    private static final MetaClass MN_OW_PEGEL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.mn_ow_pegel");
    private static final MetaClass FG_BA_GMD = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_gmd");
    private static final MetaClass FG_BA_GB = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_gb");
    private static final MetaClass FG_BA_SB = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_sb");
    private static final MetaClass FG_BA_UBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_ubef");
    private static final MetaClass FG_BA_BBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_bbef");
    private static final MetaClass FG_BA_SBEF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_sbef");
    private static final MetaClass FG_BA_PROF = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_prof");
    private static final MetaClass FG_BA_RL = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_rl");
    private static final MetaClass FG_BA_D = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_d");
    private static final MetaClass FG_BA_DUE = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_due");
    private static final MetaClass FG_BA_SCHA = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_scha");
    private static final MetaClass FG_BA_WEHR = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_wehr");
    private static final MetaClass FG_BA_SCHW = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_schw");
    private static final MetaClass FG_BA_ANLP = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_anlp");
    private static final MetaClass FG_BA_ANLL = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_anll");
    private static final MetaClass FG_BA_KR = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_kr");
    private static final MetaClass FG_BA_EA = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.fg_ba_ea");
    private static final MetaClass FG_BAK_GN1 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn1");
    private static final MetaClass FG_BAK_GN2 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn2");
    private static final MetaClass FG_BAK_GN3 = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_bak_gn3");
    private static final MetaClass FG_BA_UGHZ = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_ughz");
    private static final MetaClass FG_BA_LEIS = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_leis");
    private static final MetaClass FG_BA_TECH = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_tech");
    private static final MetaClass FG_BA_DOK = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_doku");
    private static final MetaClass FG_BA_PROJ = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.fg_ba_proj");
    private static final MetaClass FG_BA_DEICH = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.deich_ft");
    private static final MetaClass FOTO = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.foto");
    private static final MetaClass K_GU = ClassCacheMultiple.getMetaClass(
            AppBroker.DOMAIN_NAME,
            "dlm25w.k_gu");
    private static ImageIcon annotationIco = new javax.swing.ImageIcon(GewaesserReport.class.getResource(
                "/de/cismet/watergis/reports/Station.png")); // NOI18N
    private static final DecimalFormat formatter = new DecimalFormat();

    static {
        formatter.setMaximumFractionDigits(2);
        formatter.setMinimumFractionDigits(2);
        formatter.setGroupingSize(3);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
    }

    private static final Map<String, CidsLayer> layerMap = new HashMap<String, CidsLayer>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public File createReport(final String baCd) throws Exception {
        final boolean isGu = AppBroker.getInstance().isAdminUser() || AppBroker.getInstance().isGu();
        final boolean isWawi = AppBroker.getInstance().isWawiOrAdminUser();
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        parameters.put("ba_cd", baCd);
        parameters.put("dataSources", dataSources);
        parameters.put("wbblPath", "http://fry.fis-wasser-mv.de/watergis/wr_wbu_wbbl_o/");

        parameters.put("karte", GewaesserReportDialog.getInstance().isKarte());
        parameters.put("isGu", isGu);
        parameters.put("isWawi", isWawi);
        final CidsLayer cl = createCidsLayer(FG_BA);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory().createFeatures(query, null, null, 0, 0, null);

        CidsLayerFeature baFeature = null;
        if ((featureList != null) && (featureList.size() == 1)) {
            baFeature = featureList.get(0);
        }

        if (GewaesserReportDialog.getInstance().isGwk()) {
            dataSources.put("gwk", getLawaGwk(baCd, baFeature));
            parameters.put("gwk", true);
        }
        if (GewaesserReportDialog.getInstance().isGbk()) {
            dataSources.put("gbk", getLawaGbk(baCd, baFeature));
            parameters.put("gbk", true);
        }
        if (GewaesserReportDialog.getInstance().isBen()) {
            dataSources.put("wr_ben", getWasserrechtBenutzung(baCd));
            parameters.put("ben", true);
        }
        if (GewaesserReportDialog.getInstance().isAus()) {
            dataSources.put("wr_aus", getWasserrechtAusbau(baCd));
            parameters.put("ausbau", true);
        }
        if (GewaesserReportDialog.getInstance().isPegel()) {
            dataSources.put("pegel", getPegel(baCd));
            parameters.put("pegel", true);
        }
        if (GewaesserReportDialog.getInstance().isGb()) {
            dataSources.put("gb", getGb(baCd));
            parameters.put("gb", true);
        }
        if (GewaesserReportDialog.getInstance().isGmd()) {
            dataSources.put("gmd", getGmd(baCd));
            parameters.put("gem", true);
        }
        if (GewaesserReportDialog.getInstance().isSb()) {
            dataSources.put("sb", getSb(baCd));
            parameters.put("sb", true);
        }
        if (GewaesserReportDialog.getInstance().isProf()) {
            dataSources.put("prof", getProfile(baCd));
            parameters.put("prof", true);
        }
        if (GewaesserReportDialog.getInstance().isSbef()) {
            dataSources.put("sbef", getSbef(baCd));
            parameters.put("sbef", true);
        }
        if (GewaesserReportDialog.getInstance().isBbef()) {
            dataSources.put("bbef", getBbef(baCd));
            parameters.put("bbef", true);
        }
        if (GewaesserReportDialog.getInstance().isUbef()) {
            dataSources.put("ubef", getUbef(baCd));
            parameters.put("ubef", true);
        }
        if (GewaesserReportDialog.getInstance().isRl()) {
            dataSources.put("rl", getRl(baCd));
            parameters.put("rl", true);
        }
        if (GewaesserReportDialog.getInstance().isD()) {
            dataSources.put("d", getD(baCd));
            parameters.put("d", true);
        }
        if (GewaesserReportDialog.getInstance().isDue()) {
            dataSources.put("due", getDue(baCd));
            parameters.put("due", true);
        }
        if (GewaesserReportDialog.getInstance().isScha()) {
            dataSources.put("scha", getScha(baCd));
            parameters.put("scha", true);
        }
        if (GewaesserReportDialog.getInstance().isWehr()) {
            dataSources.put("wehr", getWehr(baCd));
            parameters.put("wehr", true);
        }
        if (GewaesserReportDialog.getInstance().isSchw()) {
            dataSources.put("schw", getSchw(baCd));
            parameters.put("schoepf", true);
        }
        if (GewaesserReportDialog.getInstance().isAnlp()) {
            dataSources.put("anlp", getAnlp(baCd));
            parameters.put("anlp", true);
        }
        if (GewaesserReportDialog.getInstance().isAnll()) {
            dataSources.put("anll", getAnll(baCd));
            parameters.put("anll", true);
        }
        if (GewaesserReportDialog.getInstance().isKr()) {
            dataSources.put("kr", getKr(baCd));
            parameters.put("kr", true);
        }
        if (GewaesserReportDialog.getInstance().isEa()) {
            dataSources.put("ea", getEa(baCd));
            parameters.put("ea", true);
        }
        if (GewaesserReportDialog.getInstance().isVerkn()) {
            dataSources.put("verkn", getVerkn(baCd));
            parameters.put("verk", true);
        }
        if (GewaesserReportDialog.getInstance().isTopo()) {
            dataSources.put("topo", getTopo(baCd, baFeature));
            parameters.put("topo", true);
        }
        if (GewaesserReportDialog.getInstance().isWsg()) {
            dataSources.put("schutz", getSchutz(baCd));
            parameters.put("wsg", true);
        }
        if (GewaesserReportDialog.getInstance().isSchutzgebiete()) {
            dataSources.put("schutzUeber", getUeber(baCd));
            parameters.put("ueber", true);
        }
        if (GewaesserReportDialog.getInstance().isDeich()) {
            dataSources.put("deich", getDeich(baCd));
            parameters.put("deich", true);
        }
        if (GewaesserReportDialog.getInstance().isUghz()) {
            dataSources.put("ughz", getUghz(baCd));
            parameters.put("ughz", true);
        }
        if (GewaesserReportDialog.getInstance().isFoto()) {
            dataSources.put("foto", getFoto(baCd));
            parameters.put("foto", true);
        }
        if (GewaesserReportDialog.getInstance().isLeis()) {
            dataSources.put("leis", getLeis(baCd));
            parameters.put("leis", true);
        }
        if (GewaesserReportDialog.getInstance().isTech()) {
            dataSources.put("tech", getTech(baCd));
            parameters.put("tech", true);
        }
        if (GewaesserReportDialog.getInstance().isDoku()) {
            dataSources.put("dok", getDok(baCd));
            parameters.put("dok", true);
        }
        if (GewaesserReportDialog.getInstance().isProj()) {
            dataSources.put("proj", getProj(baCd));
            parameters.put("proj", true);
        }

        if ((featureList != null) && (featureList.size() == 1)) {
            final CidsLayerFeature feature = featureList.get(0);
            if (!GewaesserReportDialog.getInstance().isKarte()) {
                parameters.put("map", null);
            } else {
                parameters.put("map", generateMap(feature));
            }
            parameters.put("quelle", convertStation(feature.getGeometry().getLength()));
            parameters.put("muendung", convertStation(0.0));
            parameters.put("senke", 0);
            dataSources.put("dummy", getSteckbrief(feature));
            final JRDataSource dataSource = getSteckbrief(feature);
            // load report
            final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GewaesserReport.class
                            .getResourceAsStream("/de/cismet/watergis/reports/gewaesser.jasper"));

            dataSources.put("steckbrief", dataSource);
            // create print from report and data
            final JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parameters,
                    new FeatureDataSource((FeatureDataSource)dataSource));
            // set orientation
            jasperPrint.setOrientation(jasperReport.getOrientationValue());

            final File file = new File(
                    GewaesserReportDialog.getInstance().getPath()
                            + File.separator
                            + GafProf.removeIllegaleFileNameCharacters((String)feature.getProperty("ba_cd"))
                            + ".pdf");
            final FileOutputStream fout = new FileOutputStream(file);
            final BufferedOutputStream out = new BufferedOutputStream(fout);
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            out.close();

            return file;
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     * @param   path  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void createReport(final String baCd, final String path) throws Exception {
        final HashMap<String, Object> parameters = new HashMap<String, Object>();
        final Map<String, JRDataSource> dataSources = new HashMap<String, JRDataSource>();
        parameters.put("ba_cd", baCd);
        parameters.put("dataSources", dataSources);
        parameters.put("wbblPath", "http://fry.fis-wasser-mv.de/watergis/wr_wbu_wbbl_o/");

        parameters.put("karte", true);
        final CidsLayer cl = createCidsLayer(FG_BA);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory().createFeatures(query, null, null, 0, 0, null);

        CidsLayerFeature baFeature = null;
        if ((featureList != null) && (featureList.size() == 1)) {
            baFeature = featureList.get(0);
        }

        dataSources.put("gwk", getLawaGwk(baCd, baFeature));
        parameters.put("gwk", true);
        dataSources.put("gbk", getLawaGbk(baCd, baFeature));
        parameters.put("gbk", true);
        dataSources.put("wr_ben", getWasserrechtBenutzung(baCd));
        parameters.put("ben", true);
        dataSources.put("wr_aus", getWasserrechtAusbau(baCd));
        parameters.put("ausbau", true);
        dataSources.put("pegel", getPegel(baCd));
        parameters.put("pegel", true);
        dataSources.put("gb", getGb(baCd));
        parameters.put("gb", true);
        dataSources.put("gmd", getGmd(baCd));
        parameters.put("gem", true);
        dataSources.put("sb", getSb(baCd));
        parameters.put("sb", true);
        dataSources.put("prof", getProfile(baCd));
        parameters.put("prof", true);
        dataSources.put("sbef", getSbef(baCd));
        parameters.put("sbef", true);
        dataSources.put("bbef", getBbef(baCd));
        parameters.put("bbef", true);
        dataSources.put("ubef", getUbef(baCd));
        parameters.put("ubef", true);
        dataSources.put("rl", getRl(baCd));
        parameters.put("rl", true);
        dataSources.put("d", getD(baCd));
        parameters.put("d", true);
        dataSources.put("due", getDue(baCd));
        parameters.put("due", true);
        dataSources.put("scha", getScha(baCd));
        parameters.put("scha", true);
        dataSources.put("wehr", getWehr(baCd));
        parameters.put("wehr", true);
        dataSources.put("schw", getSchw(baCd));
        parameters.put("schoepf", true);
        dataSources.put("anlp", getAnlp(baCd));
        parameters.put("anlp", true);
        dataSources.put("anll", getAnll(baCd));
        parameters.put("anll", true);
        dataSources.put("kr", getKr(baCd));
        parameters.put("kr", true);
        dataSources.put("ea", getEa(baCd));
        parameters.put("ea", true);
        dataSources.put("verkn", getVerkn(baCd));
        parameters.put("verk", true);
        dataSources.put("topo", getTopo(baCd, baFeature));
        parameters.put("topo", true);
        dataSources.put("schutz", getSchutz(baCd));
        parameters.put("wsg", true);
        dataSources.put("schutzUeber", getUeber(baCd));
        parameters.put("ueber", true);
        dataSources.put("deich", getDeich(baCd));
        parameters.put("deich", true);
        dataSources.put("ughz", getUghz(baCd));
        parameters.put("ughz", true);
        dataSources.put("foto", getFoto(baCd));
        parameters.put("foto", true);
        dataSources.put("leis", getLeis(baCd));
        parameters.put("leis", true);
        dataSources.put("tech", getTech(baCd));
        parameters.put("tech", true);
        dataSources.put("dok", getDok(baCd));
        parameters.put("dok", true);
        dataSources.put("proj", getProj(baCd));
        parameters.put("proj", true);

        if ((featureList != null) && (featureList.size() == 1)) {
            final CidsLayerFeature feature = featureList.get(0);
            parameters.put("map", generateMap(feature));
            parameters.put("quelle", feature.getGeometry().getLength());
            parameters.put("senke", 0);
            dataSources.put("dummy", getSteckbrief(feature));
            final JRDataSource dataSource = getSteckbrief(feature);
            // load report
            final JasperReport jasperReport = (JasperReport)JRLoader.loadObject(GewaesserReport.class
                            .getResourceAsStream("/de/cismet/watergis/reports/gewaesser.jasper"));

            dataSources.put("steckbrief", dataSource);
            // create print from report and data
            final JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport,
                    parameters,
                    new FeatureDataSource((FeatureDataSource)dataSource));
            // set orientation
            jasperPrint.setOrientation(jasperReport.getOrientationValue());

            final FileOutputStream fout = new FileOutputStream(new File(
                        path
                                + File.separator
                                + GafProf.removeIllegaleFileNameCharacters((String)feature.getProperty("ba_cd"))
                                + ".pdf"));
            final BufferedOutputStream out = new BufferedOutputStream(fout);
            JasperExportManager.exportReportToPdfStream(jasperPrint, out);
            out.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Image generateMap(final CidsLayerFeature feature) {
        try {
            final String urlBackground = "https://sgx.geodatenzentrum.de/wms_topplus_open"
                        + "?REQUEST=GetMap&VERSION=1.1.1&SERVICE=WMS&LAYERS=web_light_grau"
                        + "&BBOX=<cismap:boundingBox>"
                        + "&SRS=EPSG:5650&FORMAT=image/png"
                        + "&WIDTH=<cismap:width>"
                        + "&HEIGHT=<cismap:height>"
                        + "&STYLES=&EXCEPTIONS=application/vnd.ogc.se_inimage";
            final XBoundingBox boundingBox = new XBoundingBox(feature.getGeometry());
            boundingBox.setX1(boundingBox.getX1() - 50);
            boundingBox.setY1(boundingBox.getY1() - 50);
            boundingBox.setX2(boundingBox.getX2() + 50);
            boundingBox.setY2(boundingBox.getY2() + 50);

            final HeadlessMapProvider mapProvider = new HeadlessMapProvider();
            mapProvider.setCenterMapOnResize(true);
            mapProvider.setBoundingBox(boundingBox);
            final SimpleWmsGetMapUrl getMapUrl = new SimpleWmsGetMapUrl(urlBackground);
            final SimpleWMS simpleWms = new SimpleWMS(getMapUrl);
            mapProvider.addLayer(simpleWms);
            // this configures the internal mapping component. So the scale can be determined
            mapProvider.getImageAndWait(72, 130, 762, 400);
            final double scale = mapProvider.getMappingComponent().getScaleDenominator();

            final CidsLayer cl = createCidsLayer(FG_BA);
            final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                        .createFeatures(null, mapProvider.getCurrentBoundingBoxFromMap(), null, 0, 0, null);

            final CidsLayer clStat = createCidsLayer(FG_BA_STAT);
            final String query = "ba_cd = '" + feature.getProperty("ba_cd") + "'";
            final List<CidsLayerFeature> statList = clStat.getFeatureFactory()
                        .createFeatures(query, mapProvider.getCurrentBoundingBoxFromMap(), null, 0, 0, null);

            for (final CidsLayerFeature fgBa : featureList) {
                if (fgBa.getId() == feature.getId()) {
                    fgBa.setLinePaint(Color.RED);
                    fgBa.setLineWidth(3);
                } else {
                    fgBa.setLinePaint(Color.BLUE);
                    fgBa.setLineWidth(2);
                }
                mapProvider.addFeature(fgBa);
            }

            for (final CidsLayerFeature stat : statList) {
                final int statValue = (int)((Double)stat.getProperty("ba_st_km") * 1000);

                if (((scale > 25000) && (scale <= 100000) && ((statValue % 500) != 0))
                            || ((scale > 100000) && ((statValue % 1000) != 0))) {
                    continue;
                }
                final DefaultXStyledFeature feat = new DefaultXStyledFeature(
                        null,
                        "",
                        "",
                        null,
                        null);
                feat.setGeometry(stat.getGeometry());
                feat.setPointAnnotationSymbol(FeatureAnnotationSymbol.newCenteredFeatureAnnotationSymbol(
                        annotationIco.getImage(),
                        annotationIco.getImage()));
                feat.setPrimaryAnnotation(stat.getProperty("ba_st_c").toString());
                feat.setPrimaryAnnotationVisible(true);
                feat.setPrimaryAnnotationPaint(Color.BLACK);
//                feat.setPrimaryAnnotationHalo(Color.WHITE);
                feat.setPrimaryAnnotationJustification(JLabel.CENTER_ALIGNMENT);
                feat.setPrimaryAnnotationFont(new Font("sansserif", Font.PLAIN, 10));
                feat.setAutoScale(true);
                mapProvider.addFeature(feat);
            }

            return mapProvider.getImageAndWait(72, 200, 760, 398);
        } catch (Exception e) {
            LOG.error("Error while retrievin gmap.", e);
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom     DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double calculateFromRelatedToFeatureGeom(final Geometry geom, final CidsLayerFeature feature) {
        return calculateRelatedToFeatureGeom(geom, feature, false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom     DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double calculateTillRelatedToFeatureGeom(final Geometry geom, final CidsLayerFeature feature) {
        return calculateRelatedToFeatureGeom(geom, feature, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   geom     DOCUMENT ME!
     * @param   feature  DOCUMENT ME!
     * @param   till     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Double calculateRelatedToFeatureGeom(final Geometry geom,
            final CidsLayerFeature feature,
            final boolean till) {
        final Geometry baGeom = feature.getGeometry();
        final GeometryFactory f = baGeom.getFactory();
        final Geometry intersection = geom.buffer(0.05, 0, BufferParameters.CAP_FLAT).intersection(baGeom);
        if (intersection.isEmpty()) {
            return null;
        }
        final Coordinate lastCoord = intersection.getCoordinates()[intersection.getCoordinates().length - 1];
        final Geometry lastPoint = f.createPoint(lastCoord);
        final Coordinate firstCoord = intersection.getCoordinates()[0];
        final Geometry firstPoint = f.createPoint(firstCoord);
        double lastPointPosition = 0;
        double firstPointPosition = 0;
        final LocationIndexedLine lineLIL = new LocationIndexedLine(baGeom);
        final LengthLocationMap lineLLM = new LengthLocationMap(baGeom);

        if (lastPoint.distance(baGeom) < 0.01) {
            final LinearLocation pointLL = lineLIL.indexOf(lastCoord);
            lastPointPosition = lineLLM.getLength(pointLL);
        }
        if (firstPoint.distance(baGeom) < 0.01) {
            final LinearLocation pointLL = lineLIL.indexOf(firstCoord);
            firstPointPosition = lineLLM.getLength(pointLL);
        }

        if (till) {
            return ((lastPointPosition > firstPointPosition) ? lastPointPosition : firstPointPosition);
        } else {
            return ((lastPointPosition < firstPointPosition) ? lastPointPosition : firstPointPosition);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd       DOCUMENT ME!
     * @param   baFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getLawaGwk(final String baCd, final CidsLayerFeature baFeature) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_LA);
        final String query = "dlm25wPk_gwk_lawa1.la_cd in (\n"
                    + "select distinct k.la_cd\n"
                    + "from dlm25w.k_gwk_lawa k \n"
                    + "join dlm25w.fg_bak_gwk bg on (k.id = bg.la_cd) \n"
                    + "join dlm25w.fg_bak_linie l on (bg.bak_st = l.id) \n"
                    + "join dlm25w.fg_bak_punkt von on l.von = von.id \n"
                    + "join dlm25w.fg_bak bak on (bak.id = von.route)\n"
                    + "where \n"
                    + "bak.ba_cd = '" + baCd + "'\n"
                    + ")";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("la_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
//            feature.put("von", convertStation((Double)cidsFeature.getProperty("la_st_von")));
//            feature.put("bis", convertStation((Double)cidsFeature.getProperty("la_st_bis")));
            if (baFeature == null) {
                // fg_ba object does not exist
                break;
            }
            final Double from = calculateFromRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);
            final Double till = calculateTillRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);

            if ((from == null) || (till == null)) {
                // the object is not within the fg_ba
                continue;
            }

            feature.put("von", convertStation(from));
            feature.put("bis", convertStation(till));
            feature.put("laenge", toString(Math.abs(till - from)));
            feature.put("gwk", toString(cidsFeature.getProperty("la_cd")));
            feature.put("gwk_kurz", toString(cidsFeature.getProperty("la_cd_k")));
            feature.put("name", toString(cidsFeature.getProperty("la_gn")));
            feature.put("wrrl", toX(cidsFeature.getProperty("la_wrrl")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  baCd DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getSteckbrief(final CidsLayerFeature f) throws Exception {
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();
        String unterhalter = "";

        if (f.getBean().getProperty("ww_gr.praefixgroup") != null) {
            final CidsLayer cl = createCidsLayer(K_GU);
            final String query = "code = '" + f.getBean().getProperty("ww_gr.praefixgroup") + "'";
            final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                        .createFeatures(query, null, null, 0, 0, null);

            if ((featureList != null) && (featureList.size() == 1)) {
                unterhalter = (String)featureList.get(0).getProperty("name");
                if (unterhalter != null) {
                    unterhalter = unterhalter.replace("Wasser- und Schifffahrtsamt", "WSA");
                    unterhalter = unterhalter.replace("Staatliches Amt für Landwirtschaft und Umwelt", "StALU");
                    unterhalter = unterhalter.replace(
                            "Landesamt für Umwelt, Naturschutz und Geologie Mecklenburg-Vorpommern",
                            "LUNG");
                }
            }
        }

        final Map<String, String> feature = new HashMap<String, String>();
        feature.put("gewaessercode", toString(f.getProperty("ba_cd")));
        feature.put("unterhalter", unterhalter);
        feature.put("gewaessername", toString(f.getProperty("ba_gn")));
        feature.put("laenge", toString(f.getProperty("laenge")));
        feature.put("ordnung", ((f.getProperty("wdm") != null) ? String.valueOf(f.getProperty("wdm")) : null));
        feature.put("datum", toString(new Date()));

        features.add(feature);

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd       DOCUMENT ME!
     * @param   baFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getLawaGbk(final String baCd, final CidsLayerFeature baFeature) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BAK_GBK);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("bak_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final CidsBean bean = (CidsBean)cidsFeature.getBean().getProperty("gbk_lawa");
            final Map<String, String> feature = new HashMap<String, String>();
//            feature.put("von", convertStation((Double)cidsFeature.getProperty("bak_st_von")));
//            feature.put("bis", convertStation((Double)cidsFeature.getProperty("bak_st_bis")));
            final Double from = calculateFromRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);
            final Double till = calculateTillRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);

            if ((from == null) || (till == null)) {
                // the object is not within the fg_ba
                continue;
            }

            final String gwkLawa = toString(bean.getProperty("gwk_lawa"));
            final CidsLayer gwkLayer = createCidsLayer(K_GWK_LAWA);
            final String gwkQuery = gwkLayer.decoratePropertyName("la_cd") + " = " + gwkLawa;
            final List<CidsLayerFeature> gwkList = gwkLayer.getFeatureFactory()
                        .createFeatures(gwkQuery, null, null, 0, 0, null);

            if ((gwkList != null) && (gwkList.size() == 1)) {
                feature.put("name", toString(gwkList.get(0).getProperty("la_gn")));
            }

            feature.put("von", convertStation(from));
            feature.put("bis", convertStation(till));
            feature.put("laenge", toString(Math.abs(till - from)));

            feature.put("gbk", toString(bean.getProperty("gbk_lawa")));
            feature.put("gbk_kurz", toString(bean.getProperty("gbk_lawa_k")));
//            feature.put("name", toString(bean.getProperty("la_gn")));
            feature.put("gbk_von", toString(bean.getProperty("gbk_von")));
            feature.put("gbk_bis", toString(bean.getProperty("gbk_bis")));
//            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getSchutz(final String baCd) throws Exception {
        final ArrayList<ArrayList> propList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new SchuWasserReport(baCd));

        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final ArrayList featurePops : propList) {
            final Map<String, String> feature = new HashMap<String, String>();
            final Double laenge = Math.abs((Double)featurePops.get(0) - (Double)featurePops.get(1));
            feature.put("von", convertStation((Double)featurePops.get(0)));
            feature.put("bis", convertStation((Double)featurePops.get(1)));
            feature.put("zone", toString(featurePops.get(3)));
            feature.put("wbbl", toString(featurePops.get(4)));
            feature.put("name", toString(featurePops.get(2)));
            feature.put("text", toString(featurePops.get(5)));
            feature.put("laenge", toString(laenge));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getUeber(final String baCd) throws Exception {
        final ArrayList<ArrayList> propList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new SchuUeberReport(baCd));

        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final ArrayList featurePops : propList) {
            final Map<String, String> feature = new HashMap<String, String>();
            final Double laenge = Math.abs((Double)featurePops.get(0) - (Double)featurePops.get(1));
            feature.put("von", convertStation((Double)featurePops.get(0)));
            feature.put("bis", convertStation((Double)featurePops.get(1)));
            feature.put("wbbl", toString(featurePops.get(3)));
            feature.put("name", toString(featurePops.get(2)));
            feature.put("text", toString(featurePops.get(4)));
            feature.put("laenge", toString(laenge));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getVerkn(final String baCd) throws Exception {
        final ArrayList<ArrayList> propList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new VerknReport(baCd));

        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        if (propList != null) {
            for (final ArrayList featurePops : propList) {
                if ((Boolean)featurePops.get(4)) {
                    continue;
                }
                final Map<String, String> feature = new HashMap<String, String>();
                feature.put("station", convertStation((Double)featurePops.get(0)));
                feature.put("einm", toString(featurePops.get(1)));
                feature.put("entsp", toString(featurePops.get(2)));
                feature.put(
                    "seite",
                    (((featurePops.get(1) != null) && !featurePops.get(1).equals(" "))
                        ? flipSide(toString(featurePops.get(3))) : toString(featurePops.get(3))));

                features.add(feature);
            }
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   side  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String flipSide(final String side) {
        if ((side != null) && side.equals("re")) {
            return "li";
        } else if ((side != null) && side.equals("li")) {
            return "re";
        }

        return side;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd       DOCUMENT ME!
     * @param   baFeature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getTopo(final String baCd, final CidsLayerFeature baFeature) throws Exception {
        final CidsLayer gn1 = createCidsLayer(FG_BAK_GN1);
        final String queryGn1 = gn1.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsaGn1 = gn1.getFeatureServiceAttributes().get("bak_st_von");
        fsaGn1.setAscOrder(true);
        final List<CidsLayerFeature> featureListGn1 = gn1.getFeatureFactory()
                    .createFeatures(queryGn1, null, null, 0, 0, new FeatureServiceAttribute[] { fsaGn1 });

        final CidsLayer gn2 = createCidsLayer(FG_BAK_GN2);
        final String queryGn2 = gn2.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsaGn2 = gn2.getFeatureServiceAttributes().get("bak_st_von");
        fsaGn2.setAscOrder(true);
        final List<CidsLayerFeature> featureListGn2 = gn2.getFeatureFactory()
                    .createFeatures(queryGn2, null, null, 0, 0, new FeatureServiceAttribute[] { fsaGn2 });

        final CidsLayer gn3 = createCidsLayer(FG_BAK_GN3);
        final String queryGn3 = gn3.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsaGn3 = gn3.getFeatureServiceAttributes().get("bak_st_von");
        fsaGn3.setAscOrder(true);
        final List<CidsLayerFeature> featureListGn3 = gn3.getFeatureFactory()
                    .createFeatures(queryGn3, null, null, 0, 0, new FeatureServiceAttribute[] { fsaGn3 });

        Iterator<CidsLayerFeature> featureIt;

        if (featureListGn1 != null) {
            featureIt = featureListGn1.iterator();

            while (featureIt.hasNext()) {
                final CidsLayerFeature cidsFeature = featureIt.next();
                final Double from = calculateFromRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);
                final Double till = calculateTillRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);

                if ((from == null) || (till == null)) {
                    // the object is not within the fg_ba
                    featureIt.remove();
                }

                cidsFeature.setProperty("bak_st_von", from);
                cidsFeature.setProperty("bak_st_bis", till);
                cidsFeature.setProperty("laenge", toString(Math.abs(till - from)));
            }
        }

        if (featureListGn2 != null) {
            featureIt = featureListGn2.iterator();

            while (featureIt.hasNext()) {
                final CidsLayerFeature cidsFeature = featureIt.next();
                final Double from = calculateFromRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);
                final Double till = calculateTillRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);

                if ((from == null) || (till == null)) {
                    // the object is not within the fg_ba
                    featureIt.remove();
                }

                cidsFeature.setProperty("bak_st_von", from);
                cidsFeature.setProperty("bak_st_bis", till);
                cidsFeature.setProperty("laenge", toString(Math.abs(till - from)));
            }
        }

        if (featureListGn3 != null) {
            featureIt = featureListGn3.iterator();

            while (featureIt.hasNext()) {
                final CidsLayerFeature cidsFeature = featureIt.next();
                final Double from = calculateFromRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);
                final Double till = calculateTillRelatedToFeatureGeom(cidsFeature.getGeometry(), baFeature);

                if ((from == null) || (till == null)) {
                    // the object is not within the fg_ba
                    featureIt.remove();
                }

                cidsFeature.setProperty("bak_st_von", from);
                cidsFeature.setProperty("bak_st_bis", till);
                cidsFeature.setProperty("laenge", toString(Math.abs(till - from)));
            }
        }

        final topoFeatureCalc calc = new topoFeatureCalc();
        calc.addFeatures(featureListGn1, "gn1", "topGn1");
        calc.addFeatures(featureListGn2, "gn2", "topGn2");
        calc.addFeatures(featureListGn3, "gn3", "topGn3");

        return new FeatureDataSource(calc.calcResult());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getGmd(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_GMD);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("nummer_re", toString(cidsFeature.getProperty("nr_re"), false));
            feature.put("nummer_li", toString(cidsFeature.getProperty("nr_li"), false));
            feature.put("name_re", toString(cidsFeature.getProperty("name_re")));
            feature.put("name_li", toString(cidsFeature.getProperty("name_li")));
            feature.put("reli", toString(cidsFeature.getProperty("st_rl").equals("2")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getGb(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_GB);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("nummer_re", toString(cidsFeature.getProperty("nr_re"), false));
            feature.put("nummer_li", toString(cidsFeature.getProperty("nr_li"), false));
            feature.put("name_re", toString(cidsFeature.getProperty("name_re")));
            feature.put("name_li", toString(cidsFeature.getProperty("name_li")));
            feature.put("reli", toString(cidsFeature.getProperty("st_rl").equals(2)));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getSb(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_SB);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final CidsBean bean = cidsFeature.getBean();
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("nummer", toString(bean.getProperty("sb.sb"), false));
            feature.put("name", toString(bean.getProperty("sb.name")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getProfile(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_PROF);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
//            CidsBean bean = cidsFeature.getBean();
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("profil", toString(cidsFeature.getProperty("profil")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("ho_e", toString(cidsFeature.getProperty("ho_e")));
            feature.put("ho_a", toString(cidsFeature.getProperty("ho_a")));
            feature.put("gefaelle", toString(cidsFeature.getProperty("gefaelle")));
            feature.put("bv_re", toString(cidsFeature.getProperty("bv_re")));
            feature.put("bh_re", toString(cidsFeature.getProperty("bh_re")));
            feature.put("bl_re", toString(cidsFeature.getProperty("bl_re")));
            feature.put("bv_li", toString(cidsFeature.getProperty("bv_li")));
            feature.put("bh_li", toString(cidsFeature.getProperty("bh_li")));
            feature.put("bl_li", toString(cidsFeature.getProperty("bl_li")));
            feature.put("mw", toString(cidsFeature.getProperty("mw")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getSbef(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_SBEF);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
//            CidsBean bean = cidsFeature.getBean();
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("sbef", toString(cidsFeature.getProperty("sbef")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("ho_e", toString(cidsFeature.getProperty("ho_e")));
            feature.put("ho_a", toString(cidsFeature.getProperty("ho_a")));
            feature.put("gefaelle", toString(cidsFeature.getProperty("gefaelle")));
            feature.put("ho_d_e", toString(cidsFeature.getProperty("ho_d_e")));
            feature.put("ho_d_a", toString(cidsFeature.getProperty("ho_d_a")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getBbef(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_BBEF);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("bbef", toString(cidsFeature.getProperty("bbef")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("ho_d_o", toString(cidsFeature.getProperty("ho_d_o")));
            feature.put("ho_d_u", toString(cidsFeature.getProperty("ho_d_u")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getUbef(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_UBEF);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("ubef", toString(cidsFeature.getProperty("ubef")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("ho_d_o", toString(cidsFeature.getProperty("ho_d_o")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getRl(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_RL);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("profil", toString(cidsFeature.getProperty("profil")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            if (toString(cidsFeature.getProperty("profil")).equals("kr")
                        || toString(cidsFeature.getProperty("profil")).equals("ei")) {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li"), false));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li"), false));
            } else {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li")));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li")));
            }
            feature.put("ho_e", toString(cidsFeature.getProperty("ho_e")));
            feature.put("ho_a", toString(cidsFeature.getProperty("ho_a")));
            feature.put("gefaelle", toString(cidsFeature.getProperty("gefaelle")));
            feature.put("br_tr_o_li", toString(cidsFeature.getProperty("br_tr_o_li")));
            feature.put("ho_d_e", toString(cidsFeature.getProperty("ho_d_e")));
            feature.put("ho_d_a", toString(cidsFeature.getProperty("ho_d_a")));
            feature.put("ho_d_m", toString(cidsFeature.getProperty("ho_d_m")));
            feature.put("mw", toString(cidsFeature.getProperty("mw")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getD(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_D);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("profil", toString(cidsFeature.getProperty("profil")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            if (toString(cidsFeature.getProperty("profil")).equals("kr")
                        || toString(cidsFeature.getProperty("profil")).equals("ei")) {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li"), false));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li"), false));
            } else {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li")));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li")));
            }
            feature.put("ho_e", toString(cidsFeature.getProperty("ho_e")));
            feature.put("ho_a", toString(cidsFeature.getProperty("ho_a")));
            feature.put("gefaelle", toString(cidsFeature.getProperty("gefaelle")));
            feature.put("br_tr_o_li", toString(cidsFeature.getProperty("br_tr_o_li")));
            feature.put("ho_d_e", toString(cidsFeature.getProperty("ho_d_e")));
            feature.put("ho_d_a", toString(cidsFeature.getProperty("ho_d_a")));
            feature.put("ho_d_m", toString(cidsFeature.getProperty("ho_d_m")));
            feature.put("mw", toString(cidsFeature.getProperty("mw")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getDue(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_DUE);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("profil", toString(cidsFeature.getProperty("profil")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            if (toString(cidsFeature.getProperty("profil")).equals("kr")
                        || toString(cidsFeature.getProperty("profil")).equals("ei")) {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li"), false));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li"), false));
            } else {
                feature.put("br_dm_li", toString(cidsFeature.getProperty("br_dm_li")));
                feature.put("ho_li", toString(cidsFeature.getProperty("ho_li")));
            }
            feature.put("ho_e", toString(cidsFeature.getProperty("ho_e")));
            feature.put("ho_a", toString(cidsFeature.getProperty("ho_a")));
            feature.put("gefaelle", toString(cidsFeature.getProperty("gefaelle")));
            feature.put("br_tr_o_li", toString(cidsFeature.getProperty("br_tr_o_li")));
            feature.put("ho_d_e", toString(cidsFeature.getProperty("ho_d_e")));
            feature.put("ho_d_a", toString(cidsFeature.getProperty("ho_d_a")));
            feature.put("ho_d_m", toString(cidsFeature.getProperty("ho_d_m")));
            feature.put("mw", toString(cidsFeature.getProperty("mw")));
            feature.put("ho_d_iab", toString(cidsFeature.getProperty("ho_d_iab")));
            feature.put("ho_d_iauf", toString(cidsFeature.getProperty("ho_d_iauf")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getScha(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_SCHA);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("scha", toString(cidsFeature.getProperty("scha")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("ho_so", toString(cidsFeature.getProperty("ho_so")));
            feature.put("ho_d_so_ok", toString(cidsFeature.getProperty("ho_d_so_ok")));
            feature.put("material", toString(cidsFeature.getProperty("material")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getWehr(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_WEHR);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("wehr", toString(cidsFeature.getProperty("wehr")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("br_li", toString(cidsFeature.getProperty("br_li")));
            feature.put("ho_so", toString(cidsFeature.getProperty("ho_so")));
            feature.put("sz", toString(cidsFeature.getProperty("sz")));
            feature.put("az", toString(cidsFeature.getProperty("az")));
            feature.put("wehr_v", toString(cidsFeature.getProperty("wehr_v")));
            feature.put("wehr_av", toString(cidsFeature.getProperty("wehr_av")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("material_v", toString(cidsFeature.getProperty("material_v")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getSchw(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_SCHW);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("schw", toString(cidsFeature.getProperty("schw")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("ho_so", toString(cidsFeature.getProperty("ho_so")));
            feature.put("sz", toString(cidsFeature.getProperty("sz")));
            feature.put("az", toString(cidsFeature.getProperty("az")));
            feature.put("ezg_fl", toString(cidsFeature.getProperty("ezg_fl")));
            feature.put("v_fl", toString(cidsFeature.getProperty("v_fl")));
            feature.put("pu", toString(cidsFeature.getProperty("pu")));
            feature.put("pu_foel", toString(cidsFeature.getProperty("pu_foel")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getAnlp(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_ANLP);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("anlp", toString(cidsFeature.getProperty("anlp")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getKr(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_KR);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("kr", toString(cidsFeature.getProperty("kr")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("lage", toString(cidsFeature.getProperty("l_oiu")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getEa(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_EA);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("ea", toString(cidsFeature.getProperty("ea")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("lage_og", toString(cidsFeature.getProperty("l_og")));
            feature.put("ho_ea", toString(cidsFeature.getProperty("ho_ea")));
            feature.put("ho_d_ea", toString(cidsFeature.getProperty("ho_d_ea")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getUghz(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_UGHZ);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("ughz", toString(cidsFeature.getProperty("ughz")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("ho_d_o", toString(cidsFeature.getProperty("ho_d_o")));
            feature.put("ho_d_u", toString(cidsFeature.getProperty("ho_d_u")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getLeis(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_LEIS);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("leis", toString(cidsFeature.getProperty("leis")));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getTech(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_TECH);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("tech", toString(cidsFeature.getProperty("tech")));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("na_gu", toString(cidsFeature.getProperty("na_gu"), false));
            feature.put("mahd_gu", toString(cidsFeature.getProperty("mahd_gu"), false));
            feature.put("gu_gu", toString(cidsFeature.getProperty("gu_gu"), false));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getDok(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_DOK);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("name", toString(cidsFeature.getProperty("name")));
            feature.put("objnr", toString(cidsFeature.getProperty("doc_nr"), false));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("doc_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getProj(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_PROJ);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("name", toString(cidsFeature.getProperty("name")));
            feature.put("objnr", toString(cidsFeature.getProperty("proj_nr"), false));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("proj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getDeich(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_DEICH);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("km_von", toString(cidsFeature.getProperty("km_von")));
            feature.put("km_bis", toString(cidsFeature.getProperty("km_bis")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("lage_bd", toString(cidsFeature.getProperty("l_fk")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("deich", toString(cidsFeature.getProperty("deich")));
            feature.put("ord", toString(cidsFeature.getProperty("ord")));
            feature.put("sg", toString(cidsFeature.getProperty("schgr")));
            feature.put("berme_w", toX(cidsFeature.getProperty("berme_w")));
            feature.put("berme_b", toX(cidsFeature.getProperty("berme_b")));
            feature.put("material_f", toString(cidsFeature.getProperty("material_f")));
            feature.put("material_w", toString(cidsFeature.getProperty("material_w")));
            feature.put("material_k", toString(cidsFeature.getProperty("material_k")));
            feature.put("material_i", toString(cidsFeature.getProperty("material_i")));
            feature.put("material_b", toString(cidsFeature.getProperty("material_b")));
            feature.put("name", toString(cidsFeature.getProperty("name")));
            feature.put("nummer", toString(cidsFeature.getProperty("nr")));
            feature.put("br_f", toString(cidsFeature.getProperty("br_f")));
            feature.put("br_k", toString(cidsFeature.getProperty("br_k")));
            feature.put("ho_k_f", toString(cidsFeature.getProperty("ho_k_f")));
            feature.put("ho_k_pn", toString(cidsFeature.getProperty("ho_k_pn")));
            feature.put("ho_bhw_pn", toString(cidsFeature.getProperty("ho_bhw_pn")));
            feature.put("ho_mw_pn", toString(cidsFeature.getProperty("ho_mw_pn")));
            feature.put("bv_w", toString(cidsFeature.getProperty("bv_w")));
            feature.put("bv_b", toString(cidsFeature.getProperty("bv_b")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getFoto(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FOTO);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("titel", toString(cidsFeature.getProperty("titel")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("lage", toString(cidsFeature.getProperty("l_rl")));
            feature.put("datum", toString(cidsFeature.getProperty("aufn_datum")));
            feature.put("winkel", toString(cidsFeature.getProperty("winkel")));
            feature.put("zeit", toString(cidsFeature.getProperty("aufn_zeit")));
            feature.put("obj_nr", toString(cidsFeature.getProperty("foto_nr"), false));
            feature.put("freigabe", toString(cidsFeature.getProperty("freigabe")));
            feature.put("beschreibung", toString(cidsFeature.getProperty("beschreib")));
            feature.put("foto_nr_gu", toString(cidsFeature.getProperty("foto_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getAnll(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(FG_BA_ANLL);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st_von");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("von", convertStation((Double)cidsFeature.getProperty("ba_st_von")));
            feature.put("bis", convertStation((Double)cidsFeature.getProperty("ba_st_bis")));
            feature.put("lagestatus", toString(cidsFeature.getProperty("l_st")));
            feature.put("anll", toString(cidsFeature.getProperty("anll")));
            feature.put("traeger", toString(cidsFeature.getProperty("traeger")));
            feature.put("jahr", toString(cidsFeature.getProperty("ausbaujahr"), false));
            feature.put("zk", toString(cidsFeature.getProperty("zust_kl"), false));
            feature.put("objnr", toString(cidsFeature.getProperty("obj_nr"), false));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));
            feature.put("bemerkungen", toString(cidsFeature.getProperty("bemerkung")));
            feature.put("br", toString(cidsFeature.getProperty("br")));
            feature.put("esw", toX(cidsFeature.getProperty("esw")));
            feature.put("laenge", toString(cidsFeature.getProperty("laenge")));
            feature.put("traeger_gu", toString(cidsFeature.getProperty("traeger_gu")));
            feature.put("objnr_gu", toString(cidsFeature.getProperty("obj_nr_gu"), false));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getWasserrechtAusbau(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(WR_WBU_AUS);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put(
                "kurzbeschreibung",
                toStringWithoutNull(cidsFeature.getProperty("wbausbau1"))
                        + ((cidsFeature.getProperty("wbausbau1") != null) ? " " : "")
                        + toStringWithoutNull(cidsFeature.getProperty("wbausbau2")));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getPegel(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(MN_OW_PEGEL);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put("nummer", toString(cidsFeature.getProperty("ms_nr"), false));
            feature.put("nummer_wsa", toString(cidsFeature.getProperty("ms_nr_wsa"), false));
            feature.put("name", toString(cidsFeature.getProperty("ms_name")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   baCd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private JRDataSource getWasserrechtBenutzung(final String baCd) throws Exception {
        final CidsLayer cl = createCidsLayer(WR_WBU_BEN);
        final String query = cl.decoratePropertyName("ba_cd") + " = '" + baCd + "'";
        final FeatureServiceAttribute fsa = cl.getFeatureServiceAttributes().get("ba_st");
        fsa.setAscOrder(true);
        final List<CidsLayerFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(query, null, null, 0, 0, new FeatureServiceAttribute[] { fsa });
        final List<Map<String, String>> features = new ArrayList<Map<String, String>>();

        for (final CidsLayerFeature cidsFeature : featureList) {
            final Map<String, String> feature = new HashMap<String, String>();
            feature.put("station", convertStation((Double)cidsFeature.getProperty("ba_st")));
            feature.put(
                "kurzbeschreibung",
                toStringWithoutNull(cidsFeature.getProperty("wbbenzw1"))
                        + ((cidsFeature.getProperty("wbbenzw1") != null) ? " " : "")
                        + toStringWithoutNull(cidsFeature.getProperty("wbbenzw2")));
            feature.put("wbbl", toString(cidsFeature.getProperty("wbbl")));

            features.add(feature);
        }

        return new FeatureDataSource(features);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toStringWithoutNull(final Object o) {
        if (o == null) {
            return "";
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o             DOCUMENT ME!
     * @param   formatObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final Object o, final boolean formatObject) {
        if (o == null) {
            return null;
        } else if (!formatObject) {
            return String.valueOf(o);
        } else if ((o instanceof Double) || (o instanceof Integer)) {
            return formatter.format(o);
        } else if (o instanceof Date) {
            return DateFormat.getDateInstance().format((Date)o);
        } else if (o instanceof Boolean) {
            if ((Boolean)o) {
                return "X";
            } else {
                return null;
            }
        } else {
            return String.valueOf(o);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toString(final Object o) {
        return toString(o, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String toX(final Object o) {
        if (o instanceof Number) {
            if (((Number)o).intValue() == 1) {
                return "X";
            }
        }
        return "";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsLayer createCidsLayer(final MetaClass mo) throws Exception {
        CidsLayer layer = layerMap.get(mo.getTableName());

        if (layer == null) {
            layer = new CidsLayer(mo);
            layer.initAndWait();

            layerMap.put(mo.getTableName(), layer);
        }

        return layer;
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanup() {
        layerMap.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   station  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String convertStation(final Double station) {
        if (station == null) {
            return "";
        }
        return toString(station, true);
//        final int km = (int)(station / 1000);
//        final int m = (int)(station % 1000);
//        String mString = String.valueOf(m);
//
//        while (mString.length() < 3) {
//            mString = "0" + mString;
//        }
//
//        return km + "+" + mString;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    class topoFeatureCalc {

        //~ Instance fields ----------------------------------------------------

        Map<String[], List<CidsLayerFeature>> featureMap = new HashMap<String[], List<CidsLayerFeature>>();
        TreeSet<Double> stations = new TreeSet<Double>();

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  features  DOCUMENT ME!
         * @param  fAttr     DOCUMENT ME!
         * @param  mapAttr   DOCUMENT ME!
         */
        public void addFeatures(final List<CidsLayerFeature> features, final String fAttr, final String mapAttr) {
            if (!features.isEmpty()) {
                featureMap.put(new String[] { fAttr, mapAttr }, features);

                for (final CidsLayerFeature f : features) {
                    stations.add((Double)f.getProperty("bak_st_von"));
                    stations.add((Double)f.getProperty("bak_st_bis"));
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<Map<String, String>> calcResult() {
            final List<Map<String, String>> result = new ArrayList<Map<String, String>>();
            Double lastStation = null;

            for (final Double s : stations) {
                if (lastStation != null) {
                    final Map<String, String> entry = createMap(lastStation, s);

                    if (entry != null) {
                        result.add(entry);
                    }
                }
                lastStation = s;
            }

            return result;
        }

        /**
         * DOCUMENT ME!
         *
         * @param   start  DOCUMENT ME!
         * @param   end    DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Map<String, String> createMap(final Double start, final Double end) {
            final Map<String, String> map = new HashMap<String, String>();
            final Double pointInTheMiddle = start + 0.0012;
            boolean attrFound = false;

            map.put("von", GewaesserReport.this.convertStation(start));
            map.put("bis", GewaesserReport.this.convertStation(end));
            map.put("laenge", GewaesserReport.this.toString(Math.abs(end - start)));

            for (final String[] key : featureMap.keySet()) {
                for (final CidsLayerFeature f : featureMap.get(key)) {
                    final Double von = Math.min((Double)f.getProperty("bak_st_von"),
                            (Double)f.getProperty("bak_st_bis"));
                    final Double bis = Math.max((Double)f.getProperty("bak_st_von"),
                            (Double)f.getProperty("bak_st_bis"));

                    if ((von < pointInTheMiddle) && (bis > pointInTheMiddle)) {
                        map.put(key[1], GewaesserReport.this.toString(f.getProperty(key[0])));
                        attrFound = true;
                    }
                }
            }

            if (attrFound) {
                return map;
            } else {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class GewaesserDataSource implements JRDataSource {

        //~ Instance fields ----------------------------------------------------

        private boolean first = true;
        private final String gewaessercode;
        private final String unterhalter;
        private final String gewaessername;
        private final String laenge;
        private final String ordnung;
        private final String datum;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new GewaesserDataSource object.
         *
         * @param  gewaessercode  DOCUMENT ME!
         * @param  unterhalter    DOCUMENT ME!
         * @param  gewaessername  DOCUMENT ME!
         * @param  laenge         DOCUMENT ME!
         * @param  ordnung        DOCUMENT ME!
         * @param  datum          DOCUMENT ME!
         */
        public GewaesserDataSource(final String gewaessercode,
                final String unterhalter,
                final String gewaessername,
                final String laenge,
                final String ordnung,
                final String datum) {
            this.gewaessercode = gewaessercode;
            this.unterhalter = unterhalter;
            this.gewaessername = gewaessername;
            this.laenge = laenge;
            this.ordnung = ordnung;
            this.datum = datum;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean next() throws JRException {
            if (first) {
                first = false;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public Object getFieldValue(final JRField jrf) throws JRException {
            if (jrf.getName().equals("gewaessercode")) {
                return gewaessercode;
            } else if (jrf.getName().equals("unterhalter")) {
                return unterhalter;
            } else if (jrf.getName().equals("gewaessername")) {
                return gewaessername;
            } else if (jrf.getName().equals("laenge")) {
                return laenge;
            } else if (jrf.getName().equals("ordnung")) {
                return ordnung;
            } else if (jrf.getName().equals("datum")) {
                return datum;
            } else {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class FeatureDataSource implements JRRewindableDataSource {

        //~ Instance fields ----------------------------------------------------

        private int index = -1;
        private final List<Map<String, String>> features;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureDataSource object.
         *
         * @param  copy  features DOCUMENT ME!
         */
        public FeatureDataSource(final FeatureDataSource copy) {
            this.features = copy.features;
        }

        /**
         * Creates a new FeatureDataSource object.
         *
         * @param  features  DOCUMENT ME!
         */
        public FeatureDataSource(final List<Map<String, String>> features) {
            this.features = features;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean next() throws JRException {
            ++index;

            if (features.isEmpty() && (index == 0)) {
                return true;
            } else {
                final boolean result = (index) < features.size();

                if (!result) {
                    index = -1;
                }
                return result;
            }
        }

        @Override
        public Object getFieldValue(final JRField jrf) throws JRException {
            if (features.isEmpty()) {
                return null;
            } else {
                return features.get(index).get(jrf.getName());
            }
        }

        @Override
        public void moveFirst() throws JRException {
            index = 0;
        }
    }
}
