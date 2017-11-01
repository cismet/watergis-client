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

import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.components.DrawingMode;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    service = DrawingMode.class,
    position = 40
)
public class NewTextModeAction extends AbstractNewGeometryModeAction implements DrawingMode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public NewTextModeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                NewTextModeAction.class,
                "NewTextModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                NewTextModeAction.class,
                "NewTextModeAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/newText.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        super.actionPerformed(e);

        if (!e.getSource().equals(AppBroker.getInstance())) {
            final MappingComponent map = AppBroker.getInstance().getMappingComponent();
            ((CreateNewGeometryListener)map.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                CreateGeometryListenerInterface.TEXT);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
