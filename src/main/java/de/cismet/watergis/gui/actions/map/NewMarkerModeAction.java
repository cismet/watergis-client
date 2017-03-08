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
    position = 30
)
public class NewMarkerModeAction extends AbstractNewGeometryModeAction implements DrawingMode {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public NewMarkerModeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                NewMarkerModeAction.class,
                "NewMarkerModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        putValue(NAME, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(NewMarkerModeAction.class,
                "NewMarkerModeAction.text");
        putValue(NAME, text);
//        final String mnemonic = org.openide.util.NbBundle.getMessage(
//                NewMarkerModeAction.class,
//                "NewMarkerModeAction.mnemonic");
//        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/newPoint.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        super.actionPerformed(e);

        if (!e.getSource().equals(AppBroker.getInstance())) {
            final MappingComponent map = AppBroker.getInstance().getMappingComponent();
            ((CreateNewGeometryListener)map.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                CreateGeometryListenerInterface.POINT);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
