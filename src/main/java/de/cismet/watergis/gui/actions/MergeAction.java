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

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.math.geometry.StaticGeometryFunctions;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.merge.FeatureMerger;
import de.cismet.watergis.gui.actions.merge.FeatureMergerFactory;
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

            final FeatureMerger merger = (new FeatureMergerFactory()).getFeatureMergerForFeature(resultedFeature);
            final String geometryType = resultedFeature.getGeometry().getGeometryType();

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
                            }
                        }

                        serviceFeature.saveChanges();

                        for (final Feature f : allValidFeatures) {
                            if (f instanceof ModifiableFeature) {
                                ((ModifiableFeature)f).delete();
                            }
                        }
                    } catch (Exception ex) {
                        LOG.error("Error while saving changes", ex);
                    }

                    if (resultedFeature instanceof FeatureServiceFeature) {
                        ((FeatureServiceFeature)resultedFeature).getLayerProperties()
                                .getFeatureService()
                                .retrieve(true);
                    }
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
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final List<PFeature> features = sl.getAllSelectedPFeatures();
        final List<FeatureServiceFeature> allValidFeatures = new ArrayList<FeatureServiceFeature>();
        FeatureServiceFeature type = null;

        for (final PFeature f : features) {
            if (type == null) {
                if (f.getFeature() instanceof FeatureServiceFeature) {
                    type = (FeatureServiceFeature)f.getFeature();
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
                if (f.getFeature() instanceof FeatureServiceFeature) {
                    final FeatureServiceFeature toCheck = (FeatureServiceFeature)f.getFeature();
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
}
