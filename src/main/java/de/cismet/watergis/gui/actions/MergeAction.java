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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.custom.watergis.server.search.MergeBakAe;

import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.merge.FeatureMerger;
import de.cismet.watergis.gui.actions.merge.FeatureMergerFactory;
import de.cismet.watergis.gui.actions.merge.MergeException;
import de.cismet.watergis.gui.components.AttributeTableDialog;

import de.cismet.watergis.utils.GeometryUtils;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MergeAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MergeAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public MergeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                MergeAction.class,
                "MergeAction.MergeAction().toolTipText",
                new Object[] { " " });
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-mergeshapes.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final List<FeatureServiceFeature> allValidFeatures = determineAllValidFeature();

        // ask for the master feature
        if (allValidFeatures != null) {
            final AttributeTableDialog dialog = new AttributeTableDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.dialog.title"),
                    true);
            dialog.setData(allValidFeatures.get(0).getLayerProperties().getFeatureService(), allValidFeatures);
            dialog.setCustomText(NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.dialog.message"));
            dialog.setSize(650, 300);
            StaticSwingTools.showDialog(dialog);
            final FeatureServiceFeature masterFeature = dialog.getReturnValue();

            if (masterFeature == null) {
                // canceled by the user
                return;
            }

            final WaitingDialogThread wdt = new WaitingDialogThread(AppBroker.getInstance().getWatergisApp(),
                    true,
                    "Zusammenfügen",
                    null,
                    500) {

                    @Override
                    protected Object doInBackground() throws Exception {
                        Feature resultedFeature = masterFeature;
                        final List<FeatureServiceFeature> sortedFeaturesWithoutMaster = sortAndAdjustFeaturesForMerge(
                                masterFeature,
                                allValidFeatures,
                                0.01);

                        if ((sortedFeaturesWithoutMaster != null)
                                    && (sortedFeaturesWithoutMaster.size() < (allValidFeatures.size() - 1))) {
                            JOptionPane.showMessageDialog(
                                wd,
                                NbBundle.getMessage(
                                    MergeAction.class,
                                    "MergeAction.actionPerformed.dataTypeChanged"),
                                NbBundle.getMessage(
                                    MergeAction.class,
                                    "MergeAction.actionPerformed.dataTypeChanged.title"),
                                JOptionPane.ERROR_MESSAGE);
                            return null;
                        }
                        final FeatureMerger merger = (new FeatureMergerFactory()).getFeatureMergerForFeature(
                                resultedFeature);
                        final String geometryType = resultedFeature.getGeometry().getGeometryType();

                        try {
                            for (final Feature f : sortedFeaturesWithoutMaster) {
                                resultedFeature = merger.merge(resultedFeature, f);

                                if (resultedFeature == null) {
                                    JOptionPane.showMessageDialog(
                                        wd,
                                        NbBundle.getMessage(
                                            MergeAction.class,
                                            "MergeAction.actionPerformed.mergeFailed"),
                                        NbBundle.getMessage(
                                            MergeAction.class,
                                            "MergeAction.actionPerformed.mergeFailed.title"),
                                        JOptionPane.ERROR_MESSAGE);
                                    break;
                                }
                            }
                        } catch (MergeException ex) {
                            JOptionPane.showMessageDialog(wd,
                                ex.getMessage(),
                                "Fehler",
                                JOptionPane.ERROR_MESSAGE);

                            if (resultedFeature instanceof ModifiableFeature) {
                                ((ModifiableFeature)resultedFeature).undoAll();
                            }

                            return null;
                        }

                        if (resultedFeature != null) {
                            if (geometryType.toLowerCase().startsWith("multi")
                                        && !geometryType.equals(resultedFeature.getGeometry().getGeometryType())) {
                                final Geometry g = resultedFeature.getGeometry();
                                resultedFeature.setGeometry(StaticGeometryFunctions.toMultiGeometry(g));
                            }

                            if (geometryType.equals(resultedFeature.getGeometry().getGeometryType())
                                        && (resultedFeature instanceof ModifiableFeature)) {
                                final ModifiableFeature serviceFeature = (ModifiableFeature)resultedFeature;

                                // Save the merged feature
                                try {
                                    if (serviceFeature instanceof DefaultFeatureServiceFeature) {
                                        final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)
                                            serviceFeature;

                                        if ((dfsf.getLayerProperties() != null)
                                                    && (dfsf.getLayerProperties().getAttributeTableRuleSet() != null)) {
                                            final AttributeTableRuleSet ruleSet = dfsf.getLayerProperties()
                                                        .getAttributeTableRuleSet();

                                            ruleSet.beforeSave(dfsf);
                                            final List<FeatureServiceFeature> list =
                                                new ArrayList<FeatureServiceFeature>();
                                            list.add(dfsf);

                                            if (!ruleSet.prepareForSave(list)) {
                                                return null;
                                            }
                                        }
                                    }

                                    for (final Feature f : sortedFeaturesWithoutMaster) {
                                        if (f instanceof ModifiableFeature) {
                                            final AttributeTable table = AppBroker.getInstance()
                                                        .getWatergisApp()
                                                        .getAttributeTableByFeature((FeatureServiceFeature)f);

                                            if (table != null) {
                                                table.removeFeature((FeatureServiceFeature)f);
                                            } else {
                                                ((ModifiableFeature)f).delete();
                                            }

                                            SelectionManager.getInstance().removeSelectedFeatures(f);
                                        }
                                    }

                                    serviceFeature.saveChangesWithoutReload();
                                    serviceFeature.setEditable(false);
                                    serviceFeature.setEditable(true);

                                    if (serviceFeature instanceof CidsLayerFeature) {
                                        final CidsLayerFeature clf = (CidsLayerFeature)serviceFeature;
                                        if (clf.getBean().getMetaObject().getMetaClass().getTableName().endsWith(
                                                        "fg_bak")) {
                                            final CidsServerSearch search = new MergeBakAe(clf.getId());
                                            SessionManager.getProxy()
                                                    .customServerSearch(SessionManager.getSession().getUser(), search);
                                        }
                                    }
                                } catch (Exception ex) {
                                    LOG.error("Error while saving changes", ex);
                                }

                                AppBroker.getInstance().getMappingComponent().refresh();
                            } else if (!geometryType.equals(resultedFeature.getGeometry().getGeometryType())) {
                                // The geometry type has changed during the merge process
                                JOptionPane.showMessageDialog(
                                    wd,
                                    NbBundle.getMessage(
                                        MergeAction.class,
                                        "MergeAction.actionPerformed.dataTypeChanged"),
                                    NbBundle.getMessage(
                                        MergeAction.class,
                                        "MergeAction.actionPerformed.dataTypeChanged.title"),
                                    JOptionPane.ERROR_MESSAGE);

                                if (resultedFeature instanceof ModifiableFeature) {
                                    ((ModifiableFeature)resultedFeature).undoAll();
                                }
                            } else {
                                LOG.error("Feature is not modifiable " + resultedFeature.getClass().getName());
                            }
                        }

                        return null;
                    }
                };
            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   masterFeature     DOCUMENT ME!
     * @param   allValidFeatures  DOCUMENT ME!
     * @param   tolerance         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<FeatureServiceFeature> sortAndAdjustFeaturesForMerge(final FeatureServiceFeature masterFeature,
            final List<FeatureServiceFeature> allValidFeatures,
            final double tolerance) {
        final List<FeatureServiceFeature> featuresWithoutMaster = new ArrayList(allValidFeatures);
        featuresWithoutMaster.remove(masterFeature);

        if (masterFeature.getGeometry().getGeometryType().toUpperCase().contains("LINE")) {
            final List<Coordinate> endpoints = new ArrayList();
            final List<Coordinate> startpoints = new ArrayList();
            final Map<FeatureServiceFeature, FeatureServiceFeature> successors = new HashMap<>();
            final Map<FeatureServiceFeature, FeatureServiceFeature> predecessors = new HashMap<>();

            for (int i = -1; i < featuresWithoutMaster.size(); ++i) {
                final FeatureServiceFeature feature = ((i == -1) ? masterFeature : featuresWithoutMaster.get(i));
                final Geometry currentLine = feature.getGeometry();
                final Coordinate startpoint = currentLine.getCoordinates()[0];
                Coordinate endpoint = currentLine.getCoordinates()[currentLine.getNumPoints() - 1];

                if (endpoints.size() > 0) {
                    for (int n = 0; n < endpoints.size(); ++n) {
                        final double dist = endpoints.get(n).distance(startpoint);

                        if (dist < tolerance) {
                            // save successor and predecessor
                            final FeatureServiceFeature successor = feature;
                            final FeatureServiceFeature predecessor = ((n == 0) ? masterFeature
                                                                                : featuresWithoutMaster.get(n - 1));
                            successors.put(predecessor, successor);
                            predecessors.put(successor, predecessor);

                            if (dist != 0.0) {
                                endpoints.set(n, startpoint);

                                predecessor.setGeometry(GeometryUtils.setEndpointOfLine(
                                        predecessor.getGeometry(),
                                        startpoint));
                                break;
                            }
                        }
                    }
                    for (int n = 0; n < startpoints.size(); ++n) {
                        final double dist = startpoints.get(n).distance(endpoint);

                        if (dist < tolerance) {
                            // save successor and predecessor
                            final FeatureServiceFeature successor = ((n == 0) ? masterFeature
                                                                              : featuresWithoutMaster.get(n - 1));
                            final FeatureServiceFeature predecessor = feature;
                            successors.put(predecessor, successor);
                            predecessors.put(successor, predecessor);

                            if (dist != 0.0) {
                                endpoint = startpoints.get(n);

                                predecessor.setGeometry(GeometryUtils.setEndpointOfLine(
                                        predecessor.getGeometry(),
                                        startpoints.get(n)));
                                break;
                            }
                        }
                    }
                }

                startpoints.add(startpoint);
                endpoints.add(endpoint);
            }
            final List<FeatureServiceFeature> result = new ArrayList();
            FeatureServiceFeature next = successors.get(masterFeature);
            int n = 0;

            while ((next != null) && (n <= allValidFeatures.size())) {
                // use n to avoid a possible endless loop, if the result is a circle.
                if (!result.contains(next)) {
                    result.add(next);
                }
                next = successors.get(next);
                ++n;
            }

            FeatureServiceFeature prev = predecessors.get(masterFeature);

            n = 0;
            while ((prev != null) && (n <= allValidFeatures.size())) {
                if (!result.contains(prev)) {
                    result.add(prev);
                }
                prev = predecessors.get(prev);
            }

            return result;
        } else if (masterFeature.getGeometry().getGeometryType().toUpperCase().contains("POLYGON")) {
            Collections.sort(featuresWithoutMaster, new DistanceComparator(masterFeature));

            return featuresWithoutMaster;
        } else {
            return featuresWithoutMaster;
        }
    }

    /**
     * determines all valid features to merge.
     *
     * @return  DOCUMENT ME!
     */
    private List<FeatureServiceFeature> determineAllValidFeature() {
        final List<Feature> allSelectedFeatures = SelectionManager.getInstance().getSelectedFeatures();
        final List<Feature> features = new ArrayList<>();
        final List<FeatureServiceFeature> allValidFeatures = new ArrayList<FeatureServiceFeature>();
        FeatureServiceFeature type = null;

        // use only the selected features from the editable services
        for (final Feature f : allSelectedFeatures) {
            if (f instanceof FeatureServiceFeature) {
                final FeatureServiceFeature serviceFeature = (FeatureServiceFeature)f;
                final AbstractFeatureService service = serviceFeature.getLayerProperties().getFeatureService();

                if ((service != null) && SelectionManager.getInstance().getEditableServices().contains(service)) {
                    features.add(f);
                }
            }
        }

        for (final Feature f : features) {
            if (type == null) {
                if (f instanceof FeatureServiceFeature) {
                    type = (FeatureServiceFeature)f;
                    allValidFeatures.add(type);
                } else {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(
                            MergeAction.class,
                            "MergeAction.determineAllValidFeature.noFeatureServiceFeature"),
                        NbBundle.getMessage(
                            MergeAction.class,
                            "MergeAction.determineAllValidFeature.noFeatureServiceFeature.title"),
                        JOptionPane.ERROR_MESSAGE);
                    break;
                }
            } else {
                if (f instanceof FeatureServiceFeature) {
                    final FeatureServiceFeature toCheck = (FeatureServiceFeature)f;
                    if (type.getLayerProperties().getFeatureService().equals(
                                    toCheck.getLayerProperties().getFeatureService())) {
                        allValidFeatures.add(toCheck);
                    } else {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(
                                MergeAction.class,
                                "MergeAction.determineAllValidFeature.differentTypes"),
                            NbBundle.getMessage(
                                MergeAction.class,
                                "MergeAction.determineAllValidFeature.differentTypes.title"),
                            JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                } else {
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(
                            MergeAction.class,
                            "MergeAction.determineAllValidFeature.noFeatureServiceFeature"),
                        NbBundle.getMessage(
                            MergeAction.class,
                            "MergeAction.determineAllValidFeature.noFeatureServiceFeature.title"),
                        JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        }

        if ((allValidFeatures.size() == features.size()) && (allValidFeatures.size() > 0)) {
            final AttributeTable table = AppBroker.getInstance()
                        .getWatergisApp()
                        .getAttributeTableByFeature(allValidFeatures.get(0));

            if (allValidFeatures.get(0).getGeometry() instanceof Point) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        MergeAction.class,
                        "MergeAction.determineAllValidFeature.pointFeaturesFound.message"),
                    NbBundle.getMessage(
                        MergeAction.class,
                        "MergeAction.determineAllValidFeature.pointFeaturesFound.title"),
                    JOptionPane.ERROR_MESSAGE);

                return null;
            }

            if (table != null) {
                final List<FeatureServiceFeature> selectedFeatures = table.getSelectedFeatures();

                if (selectedFeatures.equals(allValidFeatures)) {
                    return selectedFeatures;
                }
            }
            return allValidFeatures;
        } else {
            if (features.isEmpty()) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(MergeAction.class, "MergeAction.determineAllValidFeature.noFeatureFound"),
                    NbBundle.getMessage(MergeAction.class, "MergeAction.determineAllValidFeature.noFeatureFound.title"),
                    JOptionPane.ERROR_MESSAGE);
            }

            return null;
        }
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
    private static class DistanceComparator implements Comparator<FeatureServiceFeature> {

        //~ Instance fields ----------------------------------------------------

        private final FeatureServiceFeature masterFeature;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DistanceComparator object.
         *
         * @param  masterFeature  DOCUMENT ME!
         */
        public DistanceComparator(final FeatureServiceFeature masterFeature) {
            this.masterFeature = masterFeature;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final FeatureServiceFeature o1, final FeatureServiceFeature o2) {
            final Double distanceO1 = masterFeature.getGeometry().distance(o1.getGeometry());
            final Double distanceO2 = masterFeature.getGeometry().distance(o2.getGeometry());

            return distanceO1.compareTo(distanceO2);
        }
    }
}
