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
package de.cismet.watergis.gui.actions.geoprocessing;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.ClipDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AbstractGeoprocessingAction.class)
public class ClipGeoprocessingAction extends AbstractGeoprocessingAction {

    //~ Instance fields --------------------------------------------------------

    private ClipDialog dialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DissolveGeoprocessingAction object.
     */
    public ClipGeoprocessingAction() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        super.actionPerformed(e);

        if (dialog == null) {
            dialog = new ClipDialog(AppBroker.getInstance().getWatergisApp(), false);
            dialog.pack();
        }

        StaticSwingTools.centerWindowOnScreen(dialog);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public final String getName() {
        return org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "ClipGeoprocessingAction.text");
    }

    @Override
    public final String getShortDescription() {
        return org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "ClipGeoprocessingAction.toolTipText");
    }

    @Override
    public final ImageIcon getSmallIcon() {
        return new javax.swing.ImageIcon(DissolveGeoprocessingAction.class.getResource(
                    "/de/cismet/watergis/res/icons16/icon-intersectshape.png"));
    }

    @Override
    public int getSortOrder() {
        return 20;
    }
}
