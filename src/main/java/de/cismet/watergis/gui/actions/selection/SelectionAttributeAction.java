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
package de.cismet.watergis.gui.actions.selection;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SelectionAttributeAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectionAttributeAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SelectionAttributeAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SelectionAttributeAction.class,
                "SelectionAttributeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SelectionAttributeAction.class,
                "SelectionAttributeAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SelectionAttributeAction.class,
                "SelectionAttributeAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-selectionadd.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        LOG.info("Not supported yet.");
    }

    @Override
    public boolean isEnabled() {
        return false || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
