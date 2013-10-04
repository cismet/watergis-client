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
package de.cismet.watergis.gui.actions.window;

import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.watergis.broker.AppBroker;

/**
 * An Action which shows or hides the overview map.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ShowHideOverviewWindowAction extends AbstractShowHideWindowAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShowHideOverviewWindowAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapAction object.
     */
    public ShowHideOverviewWindowAction() {
        final String text = org.openide.util.NbBundle.getMessage(
                ShowHideOverviewWindowAction.class,
                "ShowHideOverviewWindowAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ShowHideOverviewWindowAction.class,
                "ShowHideOverviewWindowAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ShowHideOverviewWindowAction.class,
                "ShowHideOverviewWindowAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-map.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
