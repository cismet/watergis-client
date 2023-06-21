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

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.geoprocessing.StationAction;
import de.cismet.watergis.gui.dialog.RefreshDbUserDialog;

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
public class RefreshViewsForUser extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RefreshViewsForUser.class);

    //~ Instance fields --------------------------------------------------------

    private RefreshDbUserDialog dialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateViewsForUser object.
     */
    public RefreshViewsForUser() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                CloseAction.class,
                "RefreshViewsForUser.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(CloseAction.class, "RefreshViewsForUser.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(CloseAction.class, "RefreshViewsForUser.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(StationAction.class.getResource(
                    "/de/cismet/watergis/res/icons16/icon-user.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (dialog == null) {
            dialog = new RefreshDbUserDialog(AppBroker.getInstance().getWatergisApp(), false);
            dialog.pack();
        }

        StaticSwingTools.centerWindowOnScreen(dialog);
    }
}
