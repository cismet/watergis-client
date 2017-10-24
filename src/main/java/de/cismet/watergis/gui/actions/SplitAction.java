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
import com.vividsolutions.jts.geom.LineString;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedLineFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.split.FeatureSplitter;
import de.cismet.watergis.gui.actions.split.FeatureSplitterFactory;

import de.cismet.watergis.utils.SplitGeometryListener;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class SplitAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SplitAction.class);

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

                        final SplitGeometryListener listener = new SplitGeometryListener(
                                mc,
                                new SplitFinishedListener(mc, oldInteractionMode, validFeature));

                        mc.addInputListener(SplitGeometryListener.LISTENER_KEY, listener);
                        mc.putCursor(SplitGeometryListener.LISTENER_KEY, new Cursor(Cursor.CROSSHAIR_CURSOR));
                        mc.setInteractionMode(SplitGeometryListener.LISTENER_KEY);
                    }
                });
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
                                AttributeTableRuleSet ruleSet = null;
                                int progress = 0;
                                wd.setMax(newFeatures.length + 1);

                                if (validFeature instanceof ModifiableFeature) {
                                    if (validFeature instanceof DefaultFeatureServiceFeature) {
                                        final DefaultFeatureServiceFeature dfsf = (DefaultFeatureServiceFeature)
                                            validFeature;

                                        if ((dfsf.getLayerProperties() != null)
                                                    && (dfsf.getLayerProperties().getAttributeTableRuleSet() != null)) {
                                            ruleSet = dfsf.getLayerProperties().getAttributeTableRuleSet();

                                            ruleSet.beforeSave(validFeature);
                                        }
                                    }

                                    try {
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

                                        return newFeatures;
                                    } else {
                                        LOG.error("Feature is not modifiable");
                                    }
                                }
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
}
