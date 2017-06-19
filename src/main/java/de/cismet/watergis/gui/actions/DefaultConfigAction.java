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
package de.cismet.watergis.gui.actions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class DefaultConfigAction extends AbstractDefaultConfigAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public DefaultConfigAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                DefaultConfigAction.class,
                "DefaultConfigAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(DefaultConfigAction.class, "DefaultConfigAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                DefaultConfigAction.class,
                "DefaultConfigAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-sharedfile.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    protected List<String> getOpenViewIds() {
        final List<String> openIds = new ArrayList<String>();

        openIds.add("Karte");
        openIds.add("Themenbaum");

        return openIds;
    }
}
