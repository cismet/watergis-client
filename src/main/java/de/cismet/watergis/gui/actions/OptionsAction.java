/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.watergis.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import de.cismet.lookupoptions.gui.OptionsDialog;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class OptionsAction extends AbstractAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OptionsAction object.
     */
    public OptionsAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(OptionsAction.class, "OptionsAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(OpenProjectAction.class, "OptionsAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(OpenProjectAction.class, "OptionsAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-settingsandroid.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFrame main = (JFrame)AppBroker.getInstance().getComponent(ComponentName.MAIN);
        final OptionsDialog od = new OptionsDialog(main, true);
        od.setLocationRelativeTo(main);
        od.setVisible(true);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
