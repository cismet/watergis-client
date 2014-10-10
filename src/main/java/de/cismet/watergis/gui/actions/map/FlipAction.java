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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.DefaultFeatureCollection;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;

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
                    "/images/rotate.png"));
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
                feature.setGeometry(feature.getGeometry().reverse());
                geometryChanged = true;
            } else {
                invalidGeometryType = true;
            }
        }

        if (geometryChanged) {
            final Component c = AppBroker.getInstance().getComponent(ComponentName.STATUSBAR);

            if (c instanceof StatusBar) {
                ((StatusBar)c).showNotification(NbBundle.getMessage(
                        FlipAction.class,
                        "FlipAction.actionPerformed.geometryChanged"));
            }
        }

        if (invalidGeometryType) {
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
                .removeFeaturesByInstance(Feature.class);
    }
}
