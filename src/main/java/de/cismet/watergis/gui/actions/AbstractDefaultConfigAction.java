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

import net.infonode.docking.View;
import net.infonode.docking.util.StringViewMap;

import java.awt.event.ActionEvent;

import java.util.List;

import javax.swing.AbstractAction;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractDefaultConfigAction extends AbstractAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public AbstractDefaultConfigAction() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final StringViewMap map = AppBroker.getInstance().getWatergisApp().getViewMap();
        final List<String> openViewIds = getOpenViewIds();

        for (int i = 0; i < map.getViewCount(); ++i) {
            final View view = map.getViewAtIndex(i);

            if (openViewIds.contains(view.getTitle())) {
                if (!view.isClosable()) {
                    view.restore();
                }
            } else {
                if (view.isClosable()) {
                    view.close();
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract List<String> getOpenViewIds();

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
