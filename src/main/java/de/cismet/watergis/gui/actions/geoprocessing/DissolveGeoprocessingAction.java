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

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.DissolveDialog;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = AbstractGeoprocessingAction.class)
public class DissolveGeoprocessingAction extends AbstractGeoprocessingAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public DissolveGeoprocessingAction() {
        super();
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        super.actionPerformed(e);
        final List<AbstractFeatureService> services = getSelectedServices();

        if (services.size() == 1) {
            final AbstractFeatureService service = services.get(0);
            final DissolveDialog dialog = new DissolveDialog(AppBroker.getInstance().getWatergisApp(), true, service);
            dialog.pack();
            StaticSwingTools.showDialog(dialog);
        } else {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    DissolveGeoprocessingAction.class,
                    "DissolveGeoprocessingAction.actionPerformed.moreThanOneService"),
                NbBundle.getMessage(
                    DissolveGeoprocessingAction.class,
                    "DissolveGeoprocessingAction.actionPerformed.moreThanOneService.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }
    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    @Override
    public final String getName() {
        return org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "DissolveGeoprocessingAction.text");
    }

    @Override
    public final String getShortDescription() {
        return org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "DissolveGeoprocessingAction.toolTipText");
    }

    @Override
    public final ImageIcon getSmallIcon() {
        return new javax.swing.ImageIcon(DissolveGeoprocessingAction.class.getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
    }

    @Override
    public int getSortOrder() {
        return 10;
    }
}
