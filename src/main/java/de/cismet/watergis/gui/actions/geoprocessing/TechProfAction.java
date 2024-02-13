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
package de.cismet.watergis.gui.actions.geoprocessing;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.MergeDialog;
import de.cismet.watergis.gui.dialog.StationDialog;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AbstractGeoprocessingAction.class)
public class TechProfAction extends AbstractGeoprocessingAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final List<String> TECH_ATTR = new ArrayList<>();
    private static final List<String> PROF_ATTR = new ArrayList<>();
    private static final Map<String, String> PROF_NAME_MAPPING = new HashMap<>();
    private static final List<String> ATTRIBUTE_ORDER = new ArrayList<>();

    static {
        TECH_ATTR.add("id");
        TECH_ATTR.add("geom");
        TECH_ATTR.add("ww_gr");
        TECH_ATTR.add("ba_cd");
        TECH_ATTR.add("ba_st_von");
        TECH_ATTR.add("ba_st_bis");
//        TECH_ATTR.add("l_st");
        TECH_ATTR.add("tech");
        TECH_ATTR.add("obj_nr");
        TECH_ATTR.add("obj_nr_gu");
//        TECH_ATTR.add("bemerkung");
        TECH_ATTR.add("na_gu");
        TECH_ATTR.add("mahd_gu");
        TECH_ATTR.add("gu_gu");
        TECH_ATTR.add("laenge");
//        PROF_ATTR.add("id");
//        PROF_ATTR.add("ww_gr");
//        PROF_ATTR.add("l_st");
        PROF_ATTR.add("profil");
//        PROF_ATTR.add("obj_nr");
//        PROF_ATTR.add("obj_nr_gu");
//        PROF_ATTR.add("traeger");
//        PROF_ATTR.add("traeger_gu");
//        PROF_ATTR.add("wbbl");
        PROF_ATTR.add("ausbaujahr");
//        PROF_ATTR.add("zust_kl");
//        PROF_ATTR.add("bemerkung");
        PROF_ATTR.add("br");
//        PROF_ATTR.add("ho_e");
//        PROF_ATTR.add("ho_a");
//        PROF_ATTR.add("gefaelle");
        PROF_ATTR.add("bv_re");
        PROF_ATTR.add("bh_re");
        PROF_ATTR.add("bl_re");
        PROF_ATTR.add("bv_li");
        PROF_ATTR.add("bh_li");
        PROF_ATTR.add("bl_li");
//        PROF_ATTR.add("mw");
//        PROF_ATTR.add("laenge");

//        PROF_NAME_MAPPING.put("id", "id2");
//        PROF_NAME_MAPPING.put("ww_gr", "ww_gr2");
//        PROF_NAME_MAPPING.put("l_st", "l_st2");
//        PROF_NAME_MAPPING.put("obj_nr", "obj_nr2");
//        PROF_NAME_MAPPING.put("obj_nr_gu", "obj_nr_gu2");
//        PROF_NAME_MAPPING.put("bemerkung", "bemerkung2");
//        PROF_NAME_MAPPING.put("laenge", "laenge2");

        ATTRIBUTE_ORDER.add("id");
        ATTRIBUTE_ORDER.add("ww_gr");
        ATTRIBUTE_ORDER.add("geom");
        ATTRIBUTE_ORDER.add("ba_cd");
        ATTRIBUTE_ORDER.add("ba_st_von");
        ATTRIBUTE_ORDER.add("ba_st_bis");
//        ATTRIBUTE_ORDER.add("l_st");
        ATTRIBUTE_ORDER.add("tech");
        ATTRIBUTE_ORDER.add("obj_nr");
        ATTRIBUTE_ORDER.add("obj_nr_gu");
//        ATTRIBUTE_ORDER.add("bemerkung");
        ATTRIBUTE_ORDER.add("na_gu");
        ATTRIBUTE_ORDER.add("mahd_gu");
        ATTRIBUTE_ORDER.add("gu_gu");
        ATTRIBUTE_ORDER.add("laenge");
//        ATTRIBUTE_ORDER.add("id2");
//        ATTRIBUTE_ORDER.add("ww_gr2");
//        ATTRIBUTE_ORDER.add("l_st2");
        ATTRIBUTE_ORDER.add("profil");
//        ATTRIBUTE_ORDER.add("obj_nr2");
//        ATTRIBUTE_ORDER.add("obj_nr_gu2");
//        ATTRIBUTE_ORDER.add("traeger");
//        ATTRIBUTE_ORDER.add("traeger_gu");
//        ATTRIBUTE_ORDER.add("wbbl");
        ATTRIBUTE_ORDER.add("ausbaujahr");
//        ATTRIBUTE_ORDER.add("zust_kl");
//        ATTRIBUTE_ORDER.add("bemerkung2");
        ATTRIBUTE_ORDER.add("br");
//        ATTRIBUTE_ORDER.add("ho_e");
//        ATTRIBUTE_ORDER.add("ho_a");
//        ATTRIBUTE_ORDER.add("gefaelle");
        ATTRIBUTE_ORDER.add("bv_re");
        ATTRIBUTE_ORDER.add("bh_re");
        ATTRIBUTE_ORDER.add("bl_re");
        ATTRIBUTE_ORDER.add("bv_li");
        ATTRIBUTE_ORDER.add("bh_li");
        ATTRIBUTE_ORDER.add("bl_li");
//        ATTRIBUTE_ORDER.add("mw");
//        ATTRIBUTE_ORDER.add("laenge2");
    }

    //~ Instance fields --------------------------------------------------------

    private StationDialog dialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public TechProfAction() {
        putValue(SHORT_DESCRIPTION, getShortDescription());
        putValue(NAME, getName());
        putValue(SMALL_ICON, getSmallIcon());
        setEnabled(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        int result = JOptionPane.showOptionDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    TechProfAction.class,
                    "TechProfAction.actionPerformed.message"),
                NbBundle.getMessage(
                    TechProfAction.class,
                    "TechProfAction.actionPerformed.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[] { "Weiter", "Abbrechen" },
                null);
        if (result == JOptionPane.YES_OPTION) {
            result = JOptionPane.showOptionDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        TechProfAction.class,
                        "TechProfAction.actionPerformed.message2"),
                    NbBundle.getMessage(
                        TechProfAction.class,
                        "TechProfAction.actionPerformed.title2"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new Object[] { "Verschneiden", "Abbrechen" },
                    null);
            if (result == JOptionPane.YES_OPTION) {
                final WaitingDialogThread<H2FeatureService> wdt = new WaitingDialogThread<H2FeatureService>(AppBroker
                                .getInstance().getWatergisApp(),
                        true,
                        "Erstelle Verschnitt                  ",
                        null,
                        100,
                        true) {

                        @Override
                        protected H2FeatureService doInBackground() throws Exception {
                            // retrieve Features
                            int progress = 10;
                            wd.setText(NbBundle.getMessage(
                                    MergeDialog.class,
                                    "MergeDialog.butOkActionPerformed.doInBackground.retrieving"));
                            wd.setMax(100);
                            wd.setProgress(5);
                            if (Thread.interrupted()) {
                                return null;
                            }
                            final CidsLayer tech = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "dlm25w.fg_ba_tech"));
                            final CidsLayer prof = new CidsLayer(ClassCacheMultiple.getMetaClass(
                                        AppBroker.DOMAIN_NAME,
                                        "dlm25w.fg_ba_prof"));
                            final String query = "dlm25wPk_ww_gr1.ww_gr = 3100";
                            // and dlm25w.fg_ba.ba_cd = '10:1LV29a-20'
                            tech.initAndWait();
                            final List<FeatureServiceFeature> featureListTech = tech.getFeatureFactory()
                                        .createFeatures(query, null, null, 0, 0, null);
                            wd.setProgress(10);
                            prof.initAndWait();
                            final List<FeatureServiceFeature> featureListProf = prof.getFeatureFactory()
                                        .createFeatures(query, null, null, 0, 0, null);
                            wd.setProgress(20);
                            if (Thread.interrupted()) {
                                return null;
                            }

                            // initialise variables for the geoperation
                            final List<FeatureServiceFeature> resultedFeatures = new ArrayList<>();
                            final LayerProperties techLayerProperties = featureListTech.get(0).getLayerProperties();
                            final LayerProperties profServiceLayerProperties = featureListProf.get(0)
                                        .getLayerProperties();
                            final Map<String, FeatureServiceAttribute> featureServiceAttributesTech = new HashMap<>(
                                    techLayerProperties.getFeatureService().getFeatureServiceAttributes());
                            final Map<String, FeatureServiceAttribute> featureServiceAttributesProf = new HashMap<>(
                                    profServiceLayerProperties.getFeatureService().getFeatureServiceAttributes());
                            final Map<String, FeatureServiceAttribute> newFeatureServiceAttributes = new HashMap<>();

                            for (final String key : featureServiceAttributesTech.keySet()) {
                                if (TECH_ATTR.contains(key)) {
                                    final FeatureServiceAttribute attr = (FeatureServiceAttribute)
                                        featureServiceAttributesTech.get(key).clone();
                                    newFeatureServiceAttributes.put(key, attr);
                                }
                            }
                            for (final String key : featureServiceAttributesProf.keySet()) {
                                if (PROF_ATTR.contains(key)) {
                                    final FeatureServiceAttribute attr = (FeatureServiceAttribute)
                                        featureServiceAttributesProf.get(key).clone();

                                    if (PROF_NAME_MAPPING.get(key) != null) {
                                        attr.setName(PROF_NAME_MAPPING.get(key));
                                        newFeatureServiceAttributes.put(PROF_NAME_MAPPING.get(key), attr);
                                    } else {
                                        newFeatureServiceAttributes.put(key, attr);
                                    }
                                }
                            }

                            final LayerProperties newLayerProperties = techLayerProperties.clone();
                            int count = 0;
                            final int totalCount = featureListTech.size();

                            newLayerProperties.setFeatureService((AbstractFeatureService)
                                techLayerProperties.getFeatureService().clone());
                            newLayerProperties.getFeatureService()
                                    .setFeatureServiceAttributes(newFeatureServiceAttributes);

                            wd.setText(NbBundle.getMessage(
                                    MergeDialog.class,
                                    "MergeDialog.butOkActionPerformed.doInBackground.createFeatures"));

                            for (final FeatureServiceFeature techFeature : featureListTech) {
                                ++count;
                                final String baCd = (String)techFeature.getProperty("ba_cd");
                                final double von = (Double)techFeature.getProperty("ba_st_von");
                                final double bis = (Double)techFeature.getProperty("ba_st_bis");
                                final List<Part> parts = new ArrayList<>();

                                for (final FeatureServiceFeature profFeature : featureListProf) {
                                    final double vonProf = (Double)profFeature.getProperty("ba_st_von");
                                    final double bisProf = (Double)profFeature.getProperty("ba_st_bis");

                                    if (baCd.equals(profFeature.getProperty("ba_cd")) && (von <= bisProf)
                                                && (bis >= vonProf)) {
                                        if (Math.abs(Math.max(von, vonProf) - Math.min(bis, bisProf)) > 0.01) {
                                            parts.add(new Part(Math.max(von, vonProf), Math.min(bis, bisProf)));
                                            resultedFeatures.add(createNewFeature(
                                                    profFeature,
                                                    techFeature,
                                                    von,
                                                    bis,
                                                    vonProf,
                                                    bisProf,
                                                    newLayerProperties));
                                        }
                                    }
                                }

                                Collections.sort(parts);

                                if (parts.isEmpty()) {
                                    resultedFeatures.add(createNewFeature(
                                            null,
                                            techFeature,
                                            von,
                                            bis,
                                            -1,
                                            -1,
                                            newLayerProperties));
                                } else {
                                    double currentStat = von;

                                    for (final Part p : parts) {
                                        if (currentStat < (p.getFrom() - 0.01)) {
                                            resultedFeatures.add(createNewFeature(
                                                    null,
                                                    techFeature,
                                                    currentStat,
                                                    p.getFrom(),
                                                    -1,
                                                    -1,
                                                    newLayerProperties));
                                        }

                                        currentStat = p.getTo();
                                    }

                                    if (currentStat < (bis - 0.01)) {
                                        resultedFeatures.add(createNewFeature(
                                                null,
                                                techFeature,
                                                currentStat,
                                                bis,
                                                -1,
                                                -1,
                                                newLayerProperties));
                                    }
                                }

                                if (Thread.interrupted()) {
                                    return null;
                                }

                                // refresh the progress bar
                                if (progress < (20 + (count * 70 / totalCount))) {
                                    progress = 20 + (count * 70 / totalCount);
                                    wd.setProgress(progress);
                                }
                            }

                            // create the service
                            wd.setText(NbBundle.getMessage(
                                    MergeDialog.class,
                                    "MergeDialog.butOkActionPerformed.doInBackground.creatingDatasource"));
                            H2FeatureService.removeTableIfExists("TechProf");

                            return FeatureServiceHelper.createNewService(AppBroker.getInstance().getWatergisApp(),
                                    resultedFeatures,
                                    "TechProf",
                                    new ArrayList<>(newFeatureServiceAttributes.values()),
                                    ATTRIBUTE_ORDER);
                        }

                        @Override
                        protected void done() {
                            try {
                                final H2FeatureService service = get();
                                final H2FeatureService newService = new H2FeatureService(service.getName(),
                                        service.getDatabasePath(),
                                        service.getTableName(),
                                        null);

                                if (service != null) {
                                    AppBroker.getInstance()
                                            .getMappingComponent()
                                            .getMappingModel()
                                            .removeLayer(service);
                                    FeatureServiceHelper.addServiceLayerToTheTree(newService);
                                }
                            } catch (Exception ex) {
                                LOG.error("Error while execute the merge operation.", ex);
                            }
                        }

                        private DefaultFeatureServiceFeature createNewFeature(final FeatureServiceFeature profFeature,
                                final FeatureServiceFeature techFeature,
                                final double von,
                                final double bis,
                                double vonProf,
                                double bisProf,
                                final LayerProperties newLayerProperties) {
                            final DefaultFeatureServiceFeature newFeature = new DefaultFeatureServiceFeature();
                            if (profFeature != null) {
                                newFeature.setProperty("id2", profFeature.getProperty("id"));
                                newFeature.setProperty("ww_gr2", profFeature.getProperty("ww_gr"));
                                newFeature.setProperty("l_st2", profFeature.getProperty("l_st"));
                                newFeature.setProperty("profil", profFeature.getProperty("profil"));
                                newFeature.setProperty("obj_nr2", profFeature.getProperty("obj_nr"));
                                newFeature.setProperty("obj_nr_gu2", profFeature.getProperty("obj_nr_gu2"));
                                newFeature.setProperty("traeger", profFeature.getProperty("traeger"));
                                newFeature.setProperty("traeger_gu", profFeature.getProperty("traeger_gu"));
                                newFeature.setProperty("wbbl", profFeature.getProperty("wbbl"));
                                newFeature.setProperty("ausbaujahr", profFeature.getProperty("ausbaujahr"));
                                newFeature.setProperty("zust_kl", profFeature.getProperty("zust_kl"));
                                newFeature.setProperty("bemerkung2", profFeature.getProperty("bemerkung"));
                                newFeature.setProperty("br", profFeature.getProperty("br"));
                                newFeature.setProperty("ho_e", profFeature.getProperty("ho_e"));
                                newFeature.setProperty("ho_a", profFeature.getProperty("ho_a"));
                                newFeature.setProperty("gefaelle", profFeature.getProperty("gefaelle"));
                                newFeature.setProperty("bv_re", profFeature.getProperty("bv_re"));
                                newFeature.setProperty("bh_re", profFeature.getProperty("bh_re"));
                                newFeature.setProperty("bl_re", profFeature.getProperty("bl_re"));
                                newFeature.setProperty("bv_li", profFeature.getProperty("bv_li"));
                                newFeature.setProperty("bh_li", profFeature.getProperty("bh_li"));
                                newFeature.setProperty("bl_li", profFeature.getProperty("bl_li"));
                                newFeature.setProperty("mw", profFeature.getProperty("mw"));
                                newFeature.setProperty("laenge2", profFeature.getProperty("laenge"));
                            } else {
                                vonProf = -1;
                                bisProf = Double.MAX_VALUE;
                            }
                            newFeature.setProperty("id", techFeature.getProperty("id"));
                            newFeature.setProperty("ww_gr", techFeature.getProperty("ww_gr"));
                            newFeature.setProperty("ba_cd", techFeature.getProperty("ba_cd"));
                            newFeature.setProperty("ba_st_von", Math.max(von, vonProf));
                            newFeature.setProperty("ba_st_bis", Math.min(bis, bisProf));
                            newFeature.setProperty("l_st", techFeature.getProperty("l_st"));

                            newFeature.setProperty("tech", techFeature.getProperty("tech"));
                            newFeature.setProperty("obj_nr", techFeature.getProperty("obj_nr"));
                            newFeature.setProperty("obj_nr_gu", techFeature.getProperty("obj_nr_gu"));
                            newFeature.setProperty("bemerkung", techFeature.getProperty("bemerkung"));
                            newFeature.setProperty("na_gu", techFeature.getProperty("na_gu"));
                            newFeature.setProperty("mahd_gu", techFeature.getProperty("mahd_gu"));
                            newFeature.setProperty("gu_gu", techFeature.getProperty("gu_gu"));
                            newFeature.setProperty("laenge", techFeature.getProperty("laenge"));

                            newFeature.setLayerProperties(newLayerProperties);
                            if ((Math.max(von, vonProf) == von) && (Math.min(bis, bisProf) == bis)) {
                                newFeature.setGeometry(techFeature.getGeometry());
                            } else if ((Math.max(von, vonProf) == vonProf)
                                        && (Math.min(bis, bisProf) == bisProf)) {
                                newFeature.setGeometry(profFeature.getGeometry());
                            } else {
                                Geometry g = null;
                                final double realTechVon = (Double)techFeature.getProperty("ba_st_von");

                                if (von != realTechVon) {
                                    g = techFeature.getGeometry();
                                    final double len = bis - von;
                                    final LocationIndexedLine lineLIL = new LocationIndexedLine(g);
                                    final LinearLocation startLoc = lineLIL.indexOf(lineLIL.extractPoint(
                                                lineLIL.getStartIndex(),
                                                von
                                                        - realTechVon));
                                    final LinearLocation endLoc = lineLIL.getEndIndex();
                                    g = lineLIL.extractLine(startLoc, endLoc);
                                } else if (Math.max(von, vonProf) == von) {
                                    g = techFeature.getGeometry();
                                } else if (Math.max(von, vonProf) == vonProf) {
                                    g = profFeature.getGeometry();
                                }

                                final double len = Math.min(bis, bisProf) - Math.max(von, vonProf);
                                final LengthIndexedLine lineLIL = new LengthIndexedLine(g);

                                newFeature.setGeometry(lineLIL.extractLine(0, len));
                            }

                            return newFeature;
                        }

                        class Part implements Comparable<Part> {

                            private double from;
                            private double to;

                            public Part(final double from, final double to) {
                                if (from > to) {
                                    this.to = from;
                                    this.from = to;
                                } else {
                                    this.from = from;
                                    this.to = to;
                                }
                            }

                            /**
                             * DOCUMENT ME!
                             *
                             * @return  the from
                             */
                            public double getFrom() {
                                return from;
                            }

                            /**
                             * DOCUMENT ME!
                             *
                             * @param  from  the from to set
                             */
                            public void setFrom(final double from) {
                                this.from = from;
                            }

                            /**
                             * DOCUMENT ME!
                             *
                             * @return  the to
                             */
                            public double getTo() {
                                return to;
                            }

                            /**
                             * DOCUMENT ME!
                             *
                             * @param  to  the to to set
                             */
                            public void setTo(final double to) {
                                this.to = to;
                            }

                            @Override
                            public int compareTo(final Part o) {
                                return new Double(getFrom()).compareTo(o.getFrom());
                            }
                        }
                    };
                wdt.start();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public final String getName() {
        return "Verschnitt tech/prof";
    }

    @Override
    public final String getShortDescription() {
        return "Verschnitt tech/prof";
    }

    @Override
    public final ImageIcon getSmallIcon() {
        return new javax.swing.ImageIcon(TechProfAction.class.getResource(
                    "/de/cismet/watergis/res/icons16/icon-flickralt.png"));
    }

    @Override
    public int getSortOrder() {
        return 85;
    }
}
