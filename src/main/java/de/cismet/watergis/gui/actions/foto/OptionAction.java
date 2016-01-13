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
package de.cismet.watergis.gui.actions.foto;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.PhotoOptionsDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class OptionAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OptionAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public OptionAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(OptionAction.class,
                "OptionAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(OptionAction.class,
                "OptionAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(OptionAction.class,
                "OptionAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-settingsthree-gears.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(PhotoOptionsDialog.getInstance());
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
