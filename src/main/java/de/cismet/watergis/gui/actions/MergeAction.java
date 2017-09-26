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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

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

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.merge.FeatureMerger;
import de.cismet.watergis.gui.actions.merge.FeatureMergerFactory;
import de.cismet.watergis.gui.actions.merge.MergeException;
import de.cismet.watergis.gui.components.AttributeTableDialog;

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

            Feature resultedFeature = masterFeature;
            allValidFeatures.remove(masterFeature);
            Collections.sort(allValidFeatures, new DistanceComparator(masterFeature));

            final FeatureMerger merger = (new FeatureMergerFactory()).getFeatureMergerForFeature(resultedFeature);
            final String geometryType = resultedFeature.getGeometry().getGeometryType();

            try {
                for (final Feature f : allValidFeatures) {
                    resultedFeature = merger.merge(resultedFeature, f);

                    if (resultedFeature == null) {
                        JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                            NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.mergeFailed"),
                            NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.mergeFailed.title"),
                            JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
            } catch (MergeException ex) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    ex.getMessage(),
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE);

                if (resultedFeature instanceof ModifiableFeature) {
                    ((ModifiableFeature)resultedFeature).undoAll();
                }

                return;
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
                            final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)serviceFeature;

                            if ((dfsf.getLayerProperties() != null)
                                        && (dfsf.getLayerProperties().getAttributeTableRuleSet() != null)) {
                                final AttributeTableRuleSet ruleSet = dfsf.getLayerProperties()
                                            .getAttributeTableRuleSet();

                                ruleSet.beforeSave(dfsf);
                                final List<FeatureServiceFeature> list = new ArrayList<FeatureServiceFeature>();
                                list.add(dfsf);

                                if (!ruleSet.prepareForSave(list)) {
                                    return;
                                }
                            }
                        }

                        for (final Feature f : allValidFeatures) {
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
                    } catch (Exception ex) {
                        LOG.error("Error while saving changes", ex);
                    }

                    AppBroker.getInstance().getMappingComponent().refresh();

//                    if (resultedFeature instanceof FeatureServiceFeature) {
//                        ((FeatureServiceFeature)resultedFeature).getLayerProperties()
//                                .getFeatureService()
//                                .retrieve(true);
//                    }
                } else if (!geometryType.equals(resultedFeature.getGeometry().getGeometryType())) {
                    // The geometry type has changed during the merge process
                    JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.dataTypeChanged"),
                        NbBundle.getMessage(MergeAction.class, "MergeAction.actionPerformed.dataTypeChanged.title"),
                        JOptionPane.ERROR_MESSAGE);

                    if (resultedFeature instanceof ModifiableFeature) {
                        ((ModifiableFeature)resultedFeature).undoAll();
                    }
                } else {
                    LOG.error("Feature is not modifiable " + resultedFeature.getClass().getName());
                }
            }
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
