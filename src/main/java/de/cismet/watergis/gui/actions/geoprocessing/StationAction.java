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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.StationDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AbstractGeoprocessingAction.class)
public class StationAction extends AbstractGeoprocessingAction {

    //~ Instance fields --------------------------------------------------------

    private StationDialog dialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public StationAction() {
        putValue(SHORT_DESCRIPTION, getShortDescription());
        putValue(NAME, getName());
        putValue(SMALL_ICON, getSmallIcon());
        setEnabled(false);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (dialog == null) {
            dialog = new StationDialog(AppBroker.getInstance().getWatergisApp(), false);
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
        return "Freie Stationierung";
    }

    @Override
    public final String getShortDescription() {
        return "Freie Stationierung";
    }

    @Override
    public final ImageIcon getSmallIcon() {
        return new javax.swing.ImageIcon(StationAction.class.getResource(
                    "/de/cismet/watergis/res/icons16/icon-flickralt.png"));
    }

    @Override
    public int getSortOrder() {
        return 50;
    }
}
