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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

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
                                new GeometryFinishedListener() {

                                    @Override
                                    public void geometryFinished(final Geometry g) {
                                        mc.setInteractionMode(oldInteractionMode);
                                        final FeatureSplitterFactory factory = new FeatureSplitterFactory();

                                        final FeatureSplitter splitter = factory.getFeatureMergerForFeature(
                                                validFeature);

                                        if (splitter != null) {
                                            final Feature[] newFeatures = splitter.split(validFeature, (LineString)g);

                                            if ((newFeatures != null) && (newFeatures.length > 0)) {
                                                AttributeTableRuleSet ruleSet = null;

                                                if (validFeature instanceof ModifiableFeature) {
                                                    if (validFeature instanceof DefaultFeatureServiceFeature) {
                                                        final DefaultFeatureServiceFeature dfsf =
                                                            (DefaultFeatureServiceFeature)validFeature;

                                                        if ((dfsf.getLayerProperties() != null)
                                                                    && (dfsf.getLayerProperties()
                                                                        .getAttributeTableRuleSet() != null)) {
                                                            ruleSet = dfsf.getLayerProperties()
                                                                            .getAttributeTableRuleSet();

                                                            ruleSet.beforeSave(validFeature);
                                                        }
                                                    }

                                                    try {
                                                        ((ModifiableFeature)validFeature).saveChangesWithoutReload();
                                                    } catch (Exception ex) {
                                                        LOG.error("Error while saving changes", ex);
                                                    }
                                                }
                                                for (final Feature newFeature : newFeatures) {
                                                    if ((newFeature instanceof ModifiableFeature)) {
                                                        if ((newFeature instanceof DefaultFeatureServiceFeature)
                                                                    && (ruleSet != null)) {
                                                            ruleSet.beforeSave(
                                                                (DefaultFeatureServiceFeature)newFeature);
                                                        }
                                                        // Save the splitted feature
                                                        try {
                                                            ((ModifiableFeature)newFeature).saveChangesWithoutReload();
                                                            AppBroker.getInstance()
                                                                        .getWatergisApp()
                                                                        .addFeatureToAttributeTable(
                                                                            (FeatureServiceFeature)newFeature);
                                                            if (LOG.isDebugEnabled()) {
                                                                LOG.debug("Splitted features saved");
                                                            }
                                                        } catch (Exception ex) {
                                                            LOG.error("Error while saving changes", ex);
                                                        }

                                                        if (validFeature instanceof FeatureServiceFeature) {
                                                            ((FeatureServiceFeature)validFeature).getLayerProperties()
                                                                        .getFeatureService()
                                                                        .retrieve(true);
                                                        }
                                                    } else {
                                                        LOG.error("Feature is not modifiable");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });

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

        if ((features != null) && (features.size() == 1)) {
            final Feature f = features.get(0);

            if (!f.getGeometry().getGeometryType().equalsIgnoreCase("POINT")) {
                if (features.get(0) instanceof FeatureServiceFeature) {
                    final FeatureServiceFeature feature = (FeatureServiceFeature)features.get(0);
                    final AttributeTable table = AppBroker.getInstance()
                                .getWatergisApp()
                                .getAttributeTableByFeature(feature);

                    if (table != null) {
                        // take the feature from the attribute table, to ensure that changes will not be overwritten
                        final List<FeatureServiceFeature> selectedFeatures = table.getSelectedFeatures();

                        if (selectedFeatures.get(0).equals(feature)) {
                            return selectedFeatures.get(0);
                        }
                    }

                    return feature;
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
}
