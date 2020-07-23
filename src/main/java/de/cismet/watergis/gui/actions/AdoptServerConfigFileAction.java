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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.TreeMap;

import javax.swing.AbstractAction;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;

/**
 * An Action, which adopts a server configuration file, with the help of the ConfigurationManager.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 * @see      ConfigurationManager
 */
public class AdoptServerConfigFileAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AdoptServerConfigFileAction.class);

    //~ Instance fields --------------------------------------------------------

    String path;
    String name;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdoptServerConfigFileAction object.
     *
     * @param  path  the path to the config file, which is in the classpath
     */
    public AdoptServerConfigFileAction(final String path) {
        this(path, "");
    }

    /**
     * Creates a new AdoptServerConfigFileAction object.
     *
     * @param  path  the path to the config file, which is in the classpath
     * @param  name  DOCUMENT ME!
     */
    public AdoptServerConfigFileAction(final String path, final String name) {
        this.path = path;
        this.name = name;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        try {
            final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
            ((ActiveLayerModel)mappingComponent.getMappingModel()).removeAllLayers();
            AppBroker.getConfigManager().configureFromClasspath(path, null);
            AppBroker.getInstance().switchMapMode(mappingComponent.getInteractionMode());
            AppBroker.getInstance().getWatergisApp().setTitle("FIS Gewässer – Projekt: " + name);
            final String fileName = path.substring(path.lastIndexOf("/") + 1);
            AppBroker.getInstance()
                    .getWatergisApp()
                    .setCurrentLayoutFile(AppBroker.getInstance().getWatergisApp().getDIRECTORYPATH_WATERGIS()
                        + System.getProperty("file.separator") + fileName);
            EventQueue.invokeLater(new Thread("Initialise layers") {

                    @Override
                    public void run() {
                        final TreeMap<Integer, MapService> map = ((ActiveLayerModel)mappingComponent.getMappingModel())
                                    .getMapServices();

                        if ((map != null) && (map.keySet() != null)) {
                            for (final Integer key : map.keySet()) {
                                final MapService service = map.get(key.intValue());

                                if ((service instanceof AbstractWMS) && !((AbstractWMS)service).isVisible()) {
                                    ((AbstractWMS)service).setSize(
                                        mappingComponent.getHeight(),
                                        mappingComponent.getWidth());
                                }
                            }
                        }
                    }
                });
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
