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

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.selection.*;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class MoveModeAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(MoveModeAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public MoveModeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(MoveModeAction.class,
                "MoveModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(MoveModeAction.class,
                "MoveModeAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(MoveModeAction.class,
                "MoveModeAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-move.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch in Move Mode");
        }
        if (AppBroker.getInstance().getLastActionMode() != AppBroker.getInstance().getSelectionMode()) {
            if (AppBroker.getInstance().getSelectionMode() != null) {
                AppBroker.getInstance().getSelectionMode().actionPerformed(null);
            }
        }
        CismapBroker.getInstance().getMappingComponent().setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
//        AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
        AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.MOVE_POLYGON);
        putValue(SELECTED_KEY, Boolean.TRUE);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
