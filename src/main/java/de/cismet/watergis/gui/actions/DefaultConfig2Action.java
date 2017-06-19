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
public class DefaultConfig2Action extends AbstractDefaultConfigAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public DefaultConfig2Action() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                DefaultConfig2Action.class,
                "DefaultConfig2Action.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                DefaultConfig2Action.class,
                "DefaultConfig2Action.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                DefaultConfig2Action.class,
                "DefaultConfig2Action.mnemonic");
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
        openIds.add("Übersicht");
        openIds.add("Datenquellen");

        return openIds;
    }
}
