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

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.selection.*;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SelectionModeAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SelectionModeAction.class);

    //~ Instance fields --------------------------------------------------------

    private JButton button;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public SelectionModeAction() {
        setEnabled(false);
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SelectionModeAction.class,
                "SelectionModeAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SelectionModeAction.class,
                "SelectionModeAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SelectionModeAction.class,
                "SelectionModeAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/select.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch in Selection Mode");
        }
        AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
        putValue(SELECTED_KEY, Boolean.TRUE);
        AppBroker.getInstance().getMappingComponent().getHandleLayer().moveToFront();
        if (button != null) {
            button.setSelected(true);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  button  DOCUMENT ME!
     */
    public void setButton(final JButton button) {
        this.button = button;
    }
}
