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

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.recently_opened_files.FileMenu;
import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

/**
 * An Action, which adopts a local configuration file, with the help of the ConfigurationManager. Notifies the
 * RecentlyOpenedFilesList, the a new file was loaded.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 * @see      RecentlyOpenedFilesList
 * @see      ConfigurationManager
 */
public class AdoptServerConfigFileAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AdoptServerConfigFileAction.class);

    //~ Instance fields --------------------------------------------------------

    String path;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdoptServerConfigFileAction object.
     *
     * @param  path  the path to the config file, which is in the classpath
     */
    public AdoptServerConfigFileAction(final String path) {
        this.path = path;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        try {
            final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
            ((ActiveLayerModel)mappingComponent.getMappingModel()).removeAllLayers();
            AppBroker.getConfigManager().configureFromClasspath(path, null);
            AppBroker.getInstance().switchMapMode(mappingComponent.getInteractionMode());
        } catch (Throwable ex) {
            LOG.fatal("No ServerProfile", ex); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void adoptConfigFile() {
        actionPerformed(null);
    }
}
