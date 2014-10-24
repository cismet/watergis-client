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

import de.cismet.watergis.gui.components.GeometryOpButton;
import de.cismet.watergis.gui.dialog.DissolveDialog;

import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DissolveGeoprocessingAction extends AbstractGeoprocessingAction {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DissolveGeoprocessingAction object.
     *
     * @param  button  DOCUMENT ME!
     * @param  mode    DOCUMENT ME!
     */
    public DissolveGeoprocessingAction(final GeometryOpButton button, final int mode) {
        super(button, mode);
        setEnabled(false);
        final String tooltip = org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "DissolveGeoprocessingAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                AbstractGeoprocessingAction.class,
                "DissolveGeoprocessingAction.text");
        putValue(NAME, text);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-zoom.png"));
        putValue(SMALL_ICON, icon);
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
}
