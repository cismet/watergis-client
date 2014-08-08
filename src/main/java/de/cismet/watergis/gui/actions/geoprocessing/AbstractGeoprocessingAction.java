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

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.components.GeometryOpButton;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractGeoprocessingAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(AbstractGeoprocessingAction.class);

    //~ Instance fields --------------------------------------------------------

    protected GeometryOpButton button;
    protected int mode;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractGeoprocessingAction object.
     *
     * @param  button  DOCUMENT ME!
     * @param  mode    DOCUMENT ME!
     */
    public AbstractGeoprocessingAction(final GeometryOpButton button, final int mode) {
        this.button = button;
        this.mode = mode;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected List<AbstractFeatureService> getSelectedServices() {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        final Component c = AppBroker.getInstance().getComponent(ComponentName.TREE);
        final ThemeLayerWidget widget = (ThemeLayerWidget)c;

        final TreePath[] paths = widget.getSelectionPath();

        if (paths != null) {
            for (final TreePath path : paths) {
                if (path.getLastPathComponent() instanceof AbstractFeatureService) {
                    serviceList.add((AbstractFeatureService)path.getLastPathComponent());
                }
            }
        }

        return serviceList;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        setMode();
    }

    /**
     * DOCUMENT ME!
     */
    protected void setMode() {
        button.setMode(mode);
    }
}
