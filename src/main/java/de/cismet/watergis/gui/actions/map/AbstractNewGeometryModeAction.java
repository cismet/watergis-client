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
package de.cismet.watergis.gui.actions.map;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.cismet.cismap.commons.features.DrawingFeature;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.CleanUpAction;

import static javax.swing.Action.SELECTED_KEY;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AbstractNewGeometryModeAction extends AbstractAction implements CleanUpAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(NewLinestringModeAction.class);

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch in the new drawing object mode");
        }

        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        map.setInteractionMode(MappingComponent.NEW_POLYGON);
        ((CreateNewGeometryListener)map.getInputListener(MappingComponent.NEW_POLYGON)).setGeometryFeatureClass(
            DrawingFeature.class);
        putValue(SELECTED_KEY, Boolean.TRUE);
    }

    @Override
    public void cleanUp() {
        final MappingComponent map = AppBroker.getInstance().getMappingComponent();
        ((CreateNewGeometryListener)map.getInputListener(MappingComponent.NEW_POLYGON)).setGeometryFeatureClass(
            PureNewFeature.class);
    }
}
