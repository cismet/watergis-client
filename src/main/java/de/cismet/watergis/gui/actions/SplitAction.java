/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions;

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.exception.ConnectionException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

import org.apache.log4j.Logger;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.watergis.server.search.MoveFgBaAfterSplit;
import de.cismet.cids.custom.watergis.server.search.MoveFgBakAfterSplit;
import de.cismet.cids.custom.watergis.server.search.RecoverFgBaAfterSplit;
import de.cismet.cids.custom.watergis.server.search.RecoverFgBakAfterSplit;
import de.cismet.cids.custom.watergis.server.search.RouteProblemsCountAndClasses;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureWithId;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.checks.AbstractCheckAction;
import de.cismet.watergis.gui.actions.split.FeatureSplitter;
import de.cismet.watergis.gui.actions.split.FeatureSplitterFactory;

import de.cismet.watergis.utils.SplitGeometryListener;

import de.cismet.watergisserver.trigger.FgObjectTrigger;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SplitAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SplitAction.class);
    private static final Map<AbstractFeatureService, List<FeatureSplitter>> undoMap =
        new HashMap<AbstractFeatureService, List<FeatureSplitter>>();
    private static final Map<AbstractFeatureService, List<FgBaAndStations>> undoStationMap =
        new HashMap<AbstractFeatureService, List<FgBaAndStations>>();
    private static final Map<AbstractFeatureService, List<FgBaAndStations>> undoBakStationMap =
        new HashMap<AbstractFeatureService, List<FgBaAndStations>>();

    //~ Instance fields --------------------------------------------------------

    private SplitGeometryListener splitListener;
    private FeatureServiceFeature lastFeature;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SplitAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SplitAction.class,
                "SplitAction.cmdSplitAction.toolTipText",
                new Object[] { " " });
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-divide.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final FeatureServiceFeature validFeature = determineValidFeature();
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();

        if (validFeature != null) {
            EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        final String oldInteractionMode = mc.getInteractionMode();

                        SplitGeometryListener listener = null;

                        if ((SplitGeometryListener.LISTENER_KEY != oldInteractionMode) && (lastFeature != null)
                                    && (splitListener != null) && validFeature.equals(lastFeature)) {
                            listener = splitListener;
                        } else {
                            listener = new SplitGeometryListener(
                                    mc,
                                    new SplitFinishedListener(mc, oldInteractionMode, validFeature));
                        }

                        splitListener = listener;
                        lastFeature = validFeature;

                        mc.addInputListener(SplitGeometryListener.LISTENER_KEY, listener);
                        mc.putCursor(SplitGeometryListener.LISTENER_KEY, new Cursor(Cursor.CROSSHAIR_CURSOR));
                        mc.setInteractionMode(SplitGeometryListener.LISTENER_KEY);
                    }
                });
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public static void undo(final AbstractFeatureService service) {
        final WaitingDialogThread wdt = new WaitingDialogThread(AppBroker.getInstance().getWatergisApp(),
                true,
                "Mache Teilen Rückgängig",
                null,
                100) {

                @Override
                protected Object doInBackground() throws Exception {
                    final List<FeatureSplitter> splitterList = undoMap.get(service);

                    if ((splitterList != null) && !splitterList.isEmpty()) {
                        for (int i = splitterList.size() - 1; i >= 0; --i) {
                            splitterList.get(i).undo();
                        }
                    }

                    undoMap.remove(service);

                    final List<FgBaAndStations> stationList = undoStationMap.get(service);

                    if ((stationList != null) && !stationList.isEmpty()) {
                        for (int i = stationList.size() - 1; i >= 0; --i) {
                            final FgBaAndStations stations = stationList.get(i);
                            final int[] stationArray = new int[stations.getStations().size()];

                            for (int index = 0; index < stations.getStations().size(); ++index) {
                                stationArray[index] = stations.getStations().get(index);
                            }
                            try {
                                SessionManager.getProxy()
                                        .customServerSearch(SessionManager.getSession().getUser(),
                                            new RecoverFgBaAfterSplit(stations.getFgBaId(), stationArray));
                            } catch (ConnectionException ex) {
                                LOG.error("Error during undo process", ex);
                            }
                        }
                        undoStationMap.remove(service);
                    }

                    final List<FgBaAndStations> bakStationList = undoBakStationMap.get(service);

                    if ((bakStationList != null) && !bakStationList.isEmpty()) {
                        for (int i = bakStationList.size() - 1; i >= 0; --i) {
                            final FgBaAndStations stations = bakStationList.get(i);
                            final int[] stationArray = new int[stations.getStations().size()];

                            for (int index = 0; index < stations.getStations().size(); ++index) {
                                stationArray[index] = stations.getStations().get(index);
                            }
                            try {
                                SessionManager.getProxy()
                                        .customServerSearch(SessionManager.getSession().getUser(),
                                            new RecoverFgBakAfterSplit(stations.getFgBaId(), stationArray));
                            } catch (ConnectionException ex) {
                                LOG.error("Error during undo process", ex);
                            }
                        }
                        undoBakStationMap.remove(service);
                    }

                    return null;
                }
            };

        wdt.start();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public static void commit(final AbstractFeatureService service) {
        final List<FeatureSplitter> splitterList = undoMap.get(service);

        if ((splitterList != null) && !splitterList.isEmpty()) {
            for (final FeatureSplitter splitter : splitterList) {
                splitter.unlockObjects();
            }
            undoMap.remove(service);
        }

        final List<FgBaAndStations> stationList = undoStationMap.get(service);

        if ((stationList != null) && !stationList.isEmpty()) {
            undoStationMap.remove(service);
        }

        final List<FgBaAndStations> bakStationList = undoBakStationMap.get(service);

        if ((bakStationList != null) && !bakStationList.isEmpty()) {
            undoBakStationMap.remove(service);
        }
    }

    /**
     * determines all valid features to merge.
     *
     * @return  DOCUMENT ME!
     */
    private FeatureServiceFeature determineValidFeature() {
        final List<Feature> features = SelectionManager.getInstance().getSelectedFeatures();

        if ((features != null) && (features.size() > 0)) {
            for (final Feature f : features) {
//            final Feature f = features.get(0);

                if (!f.getGeometry().getGeometryType().equalsIgnoreCase("POINT")) {
                    if (f instanceof FeatureServiceFeature) {
                        final FeatureServiceFeature feature = (FeatureServiceFeature)f;
                        final AttributeTable table = AppBroker.getInstance()
                                    .getWatergisApp()
                                    .getAttributeTableByFeature(feature);

                        if ((table != null) && table.isProcessingModeActive()) {
                            // take the feature from the attribute table, to ensure that changes will not be overwritten
                            final List<FeatureServiceFeature> selectedFeatures = table.getSelectedFeatures();

                            for (final FeatureServiceFeature sf : selectedFeatures) {
                                if (sf.equals(feature) && sf.isEditable()) {
                                    return sf;
                                }
                            }
                        } else {
                            continue;
                        }

                        return feature;
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(SplitAction.class, "SplitAction.determineValidFeature().message"),
                NbBundle.getMessage(SplitAction.class, "SplitAction.determineValidFeature().title"),
                JOptionPane.ERROR_MESSAGE);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   f  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature getAttributeTableFeature(final FeatureServiceFeature f) {
        final AttributeTable table = AppBroker.getInstance().getWatergisApp().getAttributeTableByFeature(f);

        if (table != null) {
            // take the feature from the attribute table, to ensure that changes will not be overwritten
            final List<FeatureServiceFeature> selectedFeatures = table.getSelectedFeatures();

            if (selectedFeatures.get(0).equals(f)) {
                return selectedFeatures.get(0);
            }
        }

        return f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class SplitFinishedListener implements GeometryFinishedListener {

        //~ Instance fields ----------------------------------------------------

        private final MappingComponent mc;
        private final String oldInteractionMode;
        private final FeatureServiceFeature validFeature;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SplitFinishedListener object.
         *
         * @param  mc                  DOCUMENT ME!
         * @param  oldInteractionMode  DOCUMENT ME!
         * @param  validFeature        DOCUMENT ME!
         */
        public SplitFinishedListener(final MappingComponent mc,
                final String oldInteractionMode,
                final FeatureServiceFeature validFeature) {
            this.mc = mc;
            this.oldInteractionMode = oldInteractionMode;
            this.validFeature = validFeature;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void geometryFinished(final Geometry g) {
            final WaitingDialogThread<Feature[]> wdt = new WaitingDialogThread<Feature[]>(AppBroker.getInstance()
                            .getWatergisApp(),
                    true,
                    "Teile",
                    null,
                    500) {

                    @Override
                    protected Feature[] doInBackground() throws Exception {
                        mc.setInteractionMode(oldInteractionMode);
                        final FeatureSplitterFactory factory = new FeatureSplitterFactory();

                        final FeatureSplitter splitter = factory.getFeatureMergerForFeature(
                                validFeature);

                        if (splitter != null) {
                            final Feature[] newFeatures = splitter.split(validFeature, (LineString)g);

                            if ((newFeatures != null) && (newFeatures.length > 0)) {
                                final AttributeTableRuleSet ruleSet = getRuleSet(validFeature);
                                int progress = 0;
                                wd.setMax(newFeatures.length + 1 + splitter.getAdditionalFeaturesToSave().size());

                                if ((splitter.getAdditionalFeaturesToSave().size() > 0)
                                            && (validFeature.getLayerProperties() != null)
                                            && (validFeature.getLayerProperties().getFeatureService() != null)) {
                                    List<FeatureSplitter> list = undoMap.get(validFeature.getLayerProperties()
                                                    .getFeatureService());

                                    if (list == null) {
                                        list = new ArrayList<FeatureSplitter>();
                                        undoMap.put(validFeature.getLayerProperties().getFeatureService(), list);
                                    }

                                    list.add(splitter);
                                }

                                for (final Feature newFeature : newFeatures) {
                                    if ((newFeature instanceof ModifiableFeature)) {
                                        if ((newFeature instanceof DefaultFeatureServiceFeature)
                                                    && (ruleSet != null)) {
                                            ruleSet.beforeSave((DefaultFeatureServiceFeature)newFeature);
                                        }
                                        // Save the splitted feature
                                        try {
                                            ((ModifiableFeature)newFeature).saveChangesWithoutReload();
                                            AppBroker.getInstance()
                                                    .getWatergisApp()
                                                    .addFeatureToAttributeTable((FeatureServiceFeature)newFeature);
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug("Splitted features saved");
                                            }
                                            wd.setProgress(++progress);
                                        } catch (Exception ex) {
                                            LOG.error("Error while saving changes", ex);
                                            wd.setProgress(++progress);
                                        }
                                    } else {
                                        LOG.error("Feature is not modifiable");
                                    }
                                }

                                for (final FeatureServiceFeature newFeature : splitter.getAdditionalFeaturesToSave()) {
                                    if ((newFeature instanceof ModifiableFeature)) {
                                        final AttributeTableRuleSet featureRuleSet = getRuleSet(newFeature);
                                        if ((newFeature instanceof DefaultFeatureServiceFeature)
                                                    && (featureRuleSet != null)) {
//                                            DefaultFeatureServiceFeature f = (DefaultFeatureServiceFeature)newFeature;
//                                            f.setProperties("", "");

                                            featureRuleSet.beforeSave((DefaultFeatureServiceFeature)newFeature);
                                        }
                                        // Save the splitted feature
                                        try {
                                            if (newFeature instanceof CidsLayerFeature) {
                                                ((CidsLayerFeature)newFeature).setId(FgObjectTrigger.ID_TO_AVOID_CHECK);
                                            }
                                            ((ModifiableFeature)newFeature).saveChangesWithoutReload();
                                            if (LOG.isDebugEnabled()) {
                                                LOG.debug("Splitted features saved");
                                            }
                                            wd.setProgress(++progress);
                                        } catch (Exception ex) {
                                            LOG.error("Error while saving changes", ex);
                                            wd.setProgress(++progress);
                                        }
                                    } else {
                                        LOG.error("Feature is not modifiable");
                                    }
                                }

                                if (validFeature instanceof CidsLayerFeature) {
                                    if (((CidsLayerFeature)validFeature).getBean().getMetaObject().getMetaClass()
                                                .getTableName().equalsIgnoreCase("dlm25w.fg_bak")) {
                                        for (final Feature newFeature : newFeatures) {
                                            // moved ba stations
                                            final ArrayList<ArrayList> stationList = (ArrayList<ArrayList>)
                                                SessionManager.getProxy()
                                                        .customServerSearch(SessionManager.getSession().getUser(),
                                                                new MoveFgBaAfterSplit(
                                                                    validFeature.getId(),
                                                                    ((FeatureWithId)newFeature).getId()));

                                            final List<Integer> classes = toIntegerList(stationList);

                                            if (!classes.isEmpty()) {
                                                if ((validFeature.getLayerProperties() != null)
                                                            && (validFeature.getLayerProperties().getFeatureService()
                                                                != null)) {
                                                    List<FgBaAndStations> list = undoStationMap.get(
                                                            validFeature.getLayerProperties().getFeatureService());

                                                    if (list == null) {
                                                        list = new ArrayList<FgBaAndStations>();
                                                        undoStationMap.put(validFeature.getLayerProperties()
                                                                    .getFeatureService(),
                                                            list);
                                                    }

                                                    list.add(new FgBaAndStations(classes, validFeature.getId()));
                                                }
                                            }

                                            // moved bak stations
                                            final ArrayList<ArrayList> bakStationList = (ArrayList<ArrayList>)
                                                SessionManager.getProxy()
                                                        .customServerSearch(SessionManager.getSession().getUser(),
                                                                new MoveFgBakAfterSplit(
                                                                    validFeature.getId(),
                                                                    ((FeatureWithId)newFeature).getId()));
                                            final List<Integer> bakClasses = toIntegerList(bakStationList);

                                            if (!bakClasses.isEmpty()) {
                                                if ((validFeature.getLayerProperties() != null)
                                                            && (validFeature.getLayerProperties().getFeatureService()
                                                                != null)) {
                                                    List<FgBaAndStations> list = undoBakStationMap.get(
                                                            validFeature.getLayerProperties().getFeatureService());

                                                    if (list == null) {
                                                        list = new ArrayList<FgBaAndStations>();
                                                        undoBakStationMap.put(validFeature.getLayerProperties()
                                                                    .getFeatureService(),
                                                            list);
                                                    }

                                                    list.add(new FgBaAndStations(bakClasses, validFeature.getId()));
                                                }
                                            }
                                        }
                                    }
                                }
                                // merge serverseitig ausführen

                                if (validFeature instanceof ModifiableFeature) {
                                    try {
                                        if (ruleSet != null) {
                                            ruleSet.beforeSave(validFeature);
                                        }
                                        if (validFeature instanceof CidsLayerFeature) {
                                            ((CidsLayerFeature)validFeature).setDoNotChangeBackup(true);
                                        }
                                        ((ModifiableFeature)validFeature).saveChangesWithoutReload();
                                        validFeature.setEditable(false);
                                        validFeature.setEditable(true);
                                        if (validFeature instanceof CidsLayerFeature) {
                                            ((CidsLayerFeature)validFeature).setDoNotChangeBackup(
                                                false);
                                        }
                                        wd.setProgress(++progress);
                                    } catch (Exception ex) {
                                        LOG.error("Error while saving changes", ex);
                                    }
                                }

                                AppBroker.getInstance().getMappingComponent().refresh();

                                return newFeatures;
                            }
                        }

                        return null;
                    }

                    private List<Integer> toIntegerList(final ArrayList<ArrayList> stationList) {
                        final List<Integer> classes = new ArrayList<Integer>();

                        if ((stationList != null) && !stationList.isEmpty()) {
                            ArrayList innerList;

                            for (int i = 0; i < stationList.size(); ++i) {
                                innerList = stationList.get(i);

                                if ((innerList != null) && !innerList.isEmpty()) {
                                    classes.add((Integer)innerList.get(0));
                                }
                            }
                        }

                        return classes;
                    }

                    private AttributeTableRuleSet getRuleSet(final Feature f) {
                        if (f instanceof DefaultFeatureServiceFeature) {
                            final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)f;

                            if ((dfsf.getLayerProperties() != null)
                                        && (dfsf.getLayerProperties().getAttributeTableRuleSet() != null)) {
                                return dfsf.getLayerProperties().getAttributeTableRuleSet();
                            }
                        }

                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            final Feature[] newFeatures = get();

                            if (newFeatures != null) {
                                if (validFeature instanceof FeatureServiceFeature) {
                                    ((FeatureServiceFeature)validFeature).getLayerProperties()
                                            .getFeatureService()
                                            .retrieve(true);
                                    SelectionManager.getInstance().addSelectedFeatures(
                                        Arrays.asList(newFeatures));
                                    SelectionManager.getInstance()
                                            .addSelectedFeatures(
                                                Collections.nCopies(1, validFeature));
                                }
                            }
                        } catch (Exception e) {
                            LOG.error("Error during split operation", e);
                        }
                    }
                };

            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class FgBaAndStations {

        //~ Instance fields ----------------------------------------------------

        private List<Integer> stations;
        private int fgBaId;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FgBaAndStations object.
         *
         * @param  stations  DOCUMENT ME!
         * @param  fgBaId    DOCUMENT ME!
         */
        public FgBaAndStations(final List<Integer> stations, final int fgBaId) {
            this.stations = stations;
            this.fgBaId = fgBaId;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public List<Integer> getStations() {
            return stations;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public int getFgBaId() {
            return fgBaId;
        }
    }
}
