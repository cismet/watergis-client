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
package de.cismet.watergis.gui.actions.map;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.components.DrawingMode;
import de.cismet.watergis.gui.dialog.VisualizingDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    service = DrawingMode.class,
    position = 60
)
public class OptionModeAction extends AbstractNewGeometryModeAction implements DrawingMode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public OptionModeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                OptionModeAction.class,
                "OptionModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                OptionModeAction.class,
                "OptionModeAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-settingsandroid.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final VisualizingDialog dialog = VisualizingDialog.getInstance();
//        dialog.setSize(490, 420);
        dialog.pack();
        dialog.setResizable(false);
        StaticSwingTools.showDialog(dialog);
        if (!dialog.isCanceled()) {
            AppBroker.getInstance().setDrawingStyleLayer(dialog.getStyleLayer());

            final MappingComponent mc = AppBroker.getInstance().getMappingComponent();
            final FeatureCollection fc = mc.getFeatureCollection();
            final List<Feature> changedFeatures = new ArrayList<Feature>();

            for (final Object f : fc.getSelectedFeatures()) {
                if (f instanceof DrawingSLDStyledFeature) {
                    final DrawingSLDStyledFeature feature = (DrawingSLDStyledFeature)f;
                    setStyle((DrawingSLDStyledFeature)feature,
                        AppBroker.getInstance().getDrawingStyles(feature.getGeometry().getGeometryType()));
                    feature.setProperty(
                        "sld",
                        VisualizingDialog.exportSLD(
                            VisualizingDialog.getInstance().getStyleLayer(),
                            feature.getGeometry().getGeometryType()));
                    changedFeatures.add(feature);
                }
            }

            if (!changedFeatures.isEmpty() && (fc instanceof DefaultFeatureCollection)) {
                ((DefaultFeatureCollection)fc).fireFeaturesChanged(changedFeatures);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature   DOCUMENT ME!
     * @param  sldStyle  DOCUMENT ME!
     */
    public void setStyle(final DrawingSLDStyledFeature feature,
            final Map<String, LinkedList<org.deegree.style.se.unevaluated.Style>> sldStyle) {
        feature.setSLDStyles(sldStyle.get("default"));
        final PFeature pfeature = AppBroker.getInstance().getMappingComponent().getPFeatureHM().get(feature);

        if (pfeature != null) {
            pfeature.refreshDesign();
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
