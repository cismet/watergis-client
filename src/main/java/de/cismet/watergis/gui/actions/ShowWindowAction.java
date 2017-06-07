/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.watergis.broker.AppBroker;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ShowWindowAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    private final String windowId;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowWindowAction object.
     */
    public ShowWindowAction() {
        this("", "");
    }

    /**
     * Creates a new CloseAction object.
     *
     * @param  WindowName  DOCUMENT ME!
     */
    public ShowWindowAction(final String WindowName) {
        this(WindowName, WindowName);
    }

    /**
     * Creates a new CloseAction object.
     *
     * @param  windowId    DOCUMENT ME!
     * @param  WindowName  DOCUMENT ME!
     */
    public ShowWindowAction(final String windowId, final String WindowName) {
        this.windowId = windowId;
//        final String tooltip = org.openide.util.NbBundle.getMessage(CloseAction.class, "ShowWindowAction.toolTipText");
//        putValue(SHORT_DESCRIPTION, tooltip);
//        final String text = org.openide.util.NbBundle.getMessage(CloseAction.class, "ShowWindowAction.text");
        putValue(NAME, WindowName);
//        final String mnemonic = org.openide.util.NbBundle.getMessage(CloseAction.class, "ShowWindowAction.mnemonic");
//        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-webpage.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        AppBroker.getInstance().getWatergisApp().showWindow(windowId);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
