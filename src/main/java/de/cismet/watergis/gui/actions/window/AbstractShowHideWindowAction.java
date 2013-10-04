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

import net.infonode.docking.View;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public abstract class AbstractShowHideWindowAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    private View view;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  view  DOCUMENT ME!
     */
    public void setView(final View view) {
        this.view = view;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        showOrHideView();
    }

    /**
     * DOCUMENT ME!
     */
    void showOrHideView() {
        ///irgendwas besser als Closable ??
        // Problem wenn floating --> close -> open  (muss zweimal open)
        if (view.isClosable()) {
            view.close();
        } else {
            view.restore();
        }
    }
}
