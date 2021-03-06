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

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.custom.attributerule.MessageDialog;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.actions.CleanUpAction;
import de.cismet.watergis.gui.panels.StatusBar;

/**
 * DOCUMENT ME!
 *
 * @author   Thorsten Herter
 * @version  $Revision$, $Date$
 */
public class FlipAction extends AbstractAction implements CleanUpAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FlipAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public FlipAction() {
        setEnabled(false);
        final String tooltip = org.openide.util.NbBundle.getMessage(
                FlipAction.class,
                "FlipAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                FlipAction.class,
                "FlipAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                FlipAction.class,
                "FlipAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-fliphorizontal.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        final Collection<Feature> selectedFeature = map.getFeatureCollection().getSelectedFeatures();
        boolean invalidGeometryType = false;
        boolean geometryChanged = false;

        for (final Feature feature : selectedFeature) {
            if (feature.getGeometry().getGeometryType().equals("LineString")) {
                if (feature.isEditable()) {
                    feature.setGeometry(feature.getGeometry().reverse());
                    final PFeature pf = map.getPFeatureHM().get(feature);

                    if (pf != null) {
                        pf.refresh();
                        pf.visualize();
                    }
                    CismapBroker.getInstance()
                            .getMappingComponent()
                            .getMemUndo()
                            .addAction(new FeatureFlipAction(feature, map));
                    geometryChanged = true;
                }
            } else {
                invalidGeometryType = true;
            }
        }

//        if (geometryChanged) {
//            final MessageDialog dialog = new MessageDialog(AppBroker.getInstance().getWatergisApp(),
//                    true,
//                    NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.geometryChanged"),
//                    NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.geometryChanged.title"));
//            dialog.setSize(200, 100);
//            StaticSwingTools.showDialog(dialog);
//        }

        if (!geometryChanged && invalidGeometryType) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.wrongGeometryType.text"),
                NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.wrongGeometryType.title"),
                JOptionPane.ERROR_MESSAGE);
        }

        if (selectedFeature.isEmpty()) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.nothingSelected.text"),
                NbBundle.getMessage(FlipAction.class, "FlipAction.actionPerformed.nothingSelected.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public void cleanUp() {
        ((DefaultFeatureCollection)(AppBroker.getInstance().getMappingComponent().getFeatureCollection()))
                .removeFeaturesByInstance(PureNewFeature.class);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class FeatureFlipAction implements CustomAction {

        //~ Instance fields ----------------------------------------------------

        private final Feature f;
        private final MappingComponent mc;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new FeatureFlipAction object.
         *
         * @param  f   DOCUMENT ME!
         * @param  mc  DOCUMENT ME!
         */
        public FeatureFlipAction(final Feature f, final MappingComponent mc) {
            this.f = f;
            this.mc = mc;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void doAction() {
            final Vector v = new Vector();
            final PFeature pf = mc.getPFeatureHM().get(f);

            v.add(f);
            f.setGeometry(f.getGeometry().reverse());
            pf.visualize();
            ((DefaultFeatureCollection)mc.getFeatureCollection()).fireFeaturesChanged(v);
        }

        @Override
        public String info() {
            return "Geometrie drehen";
        }

        @Override
        public CustomAction getInverse() {
            return new FeatureFlipAction(f, mc);
        }

        @Override
        public boolean featureConcerned(final Feature feature) {
            return (f != null) && f.equals(feature);
        }
    }
}
