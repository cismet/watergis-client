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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.features.DrawingFeature;
import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DeleteFeatureListener;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.CleanUpAction;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class RemoveDrawingModeAction extends AbstractAction implements CleanUpAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RemoveDrawingModeAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public RemoveDrawingModeAction() {
        setEnabled(false);
        final String tooltip = org.openide.util.NbBundle.getMessage(
                RemoveDrawingModeAction.class,
                "RemoveDrawingModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                RemoveDrawingModeAction.class,
                "RemoveDrawingModeAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                RemoveDrawingModeAction.class,
                "RemoveDrawingModeAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/remove.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch in remove Mode");
        }
        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        map.setInteractionMode(MappingComponent.REMOVE_POLYGON);
        ((DeleteFeatureListener)map.getInputListener(MappingComponent.REMOVE_POLYGON)).setAllowedFeatureClassesToDelete(
            new Class[] { DrawingSLDStyledFeature.class });
        putValue(SELECTED_KEY, Boolean.TRUE);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public void cleanUp() {
        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        ((DeleteFeatureListener)map.getInputListener(MappingComponent.REMOVE_POLYGON)).setAllowedFeatureClassesToDelete(
            null);
    }
}
