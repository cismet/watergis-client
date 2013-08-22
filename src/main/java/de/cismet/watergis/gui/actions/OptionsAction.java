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
import javax.swing.JFrame;

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

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final JFrame main = (JFrame)AppBroker.getInstance().getComponent(ComponentName.MAIN);
        final OptionsDialog od = new OptionsDialog(main, true);
        od.setLocationRelativeTo(main);
        od.setVisible(true);
    }
}
