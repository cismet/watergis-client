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

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.components.SnappingMode;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    service = SnappingMode.class,
    position = 10
)
public class NoSnappingModeAction extends AbstractNewGeometryModeAction implements SnappingMode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public NoSnappingModeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                NoSnappingModeAction.class,
                "NoSnappingModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                NoSnappingModeAction.class,
                "NoSnappingModeAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/no-snap.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        super.actionPerformed(e);

        if (!e.getSource().equals(AppBroker.getInstance())) {
            final MappingComponent map = AppBroker.getInstance().getMappingComponent();
            map.setSnappingEnabled(false);
            map.setSnappingOnLineEnabled(false);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
