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
package de.cismet.watergis.gui.actions.selection;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.listener.SelectionModeChangedEvent;
import de.cismet.watergis.broker.listener.SelectionModeListener;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SelectionRectangleAction extends AbstractAction implements SelectionModeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectionRectangleAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public SelectionRectangleAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SelectionRectangleAction.class,
                "SelectionRectangleAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SelectionRectangleAction.class,
                "SelectionRectangleAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SelectionRectangleAction.class,
                "SelectionRectangleAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-rectangle.png"));
        putValue(SMALL_ICON, icon);
        AppBroker.getInstance().addSelecionModeListener(this);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final SelectionListener sl = (SelectionListener)AppBroker.getInstance().getMappingComponent()
                    .getInputEventListener()
                    .get(MappingComponent.SELECT);
        final String oldMode = sl.getMode();
        sl.setMode(CreateGeometryListenerInterface.RECTANGLE);
        AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
        AppBroker.getInstance().fireSelectionModeChanged(this, oldMode, CreateGeometryListenerInterface.RECTANGLE);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public void selectionModeChanged(final SelectionModeChangedEvent e) {
        if (e.getNewMode().equals(CreateGeometryListenerInterface.RECTANGLE)) {
            putValue(Action.SELECTED_KEY, true);
        } else {
            putValue(Action.SELECTED_KEY, false);
        }
    }
}
