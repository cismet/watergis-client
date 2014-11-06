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
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;

import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

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

    protected Collection<ActionListener> listeners = new ArrayList<ActionListener>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractGeoprocessingAction object.
     */
    public AbstractGeoprocessingAction() {
        putValue(SHORT_DESCRIPTION, getShortDescription());
        putValue(NAME, getName());
        putValue(SMALL_ICON, getSmallIcon());
        setEnabled(false);
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
        fireActionEvent(e);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addActionListener(final ActionListener listener) {
        return this.listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeModeListener(final ActionListener listener) {
        return this.listeners.remove(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    protected void fireActionEvent(final ActionEvent e) {
        for (final ActionListener listener : listeners) {
            listener.actionPerformed(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getShortDescription();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract ImageIcon getSmallIcon();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract int getSortOrder();
}
