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

import org.apache.commons.io.FilenameUtils;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.io.File;

import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.raster.wms.AbstractWMS;
import de.cismet.cismap.commons.rasterservice.MapService;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

/**
 * An Action, which adopts a local configuration file, with the help of the ConfigurationManager. Notifies the
 * RecentlyOpenedFilesList, that a new file was loaded.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 * @see      RecentlyOpenedFilesList
 * @see      ConfigurationManager
 */
public class AdoptLocalConfigFileAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    File file;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdoptLocalConfigFileAction object.
     *
     * @param  file  the file which will be loaded by the ConfigurationManager
     */
    public AdoptLocalConfigFileAction(final File file) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                LocalConfigAction.class,
                "LocalConfigAction.toolTipText");
//        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-file.png"));
        putValue(SMALL_ICON, icon);
        this.file = file;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        AppBroker.getInstance().getRecentlyOpenedFilesList().saveFilenames();

        final String filename = file.getAbsolutePath();

        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final ConfigurationManager configurationManager = AppBroker.getConfigManager();

        mappingComponent.getRasterServiceLayer().removeAllChildren();
        AppBroker.getInstance().getWatergisApp().setCurrentLayoutFile(filename);
        configurationManager.configure(filename);

        final String layoutFilepath = FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename)
                    + ".layout";
        AppBroker.getInstance().getWatergisApp().loadLayout(layoutFilepath);

        AppBroker.getInstance().switchMapMode(mappingComponent.getInteractionMode());
        AppBroker.getInstance().getRecentlyOpenedFilesList().addFile(file);
        String projectName = file.getName();

        if (projectName.contains(".")) {
            projectName = projectName.substring(0, projectName.indexOf("."));
        }

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

        AppBroker.getInstance().getWatergisApp().setTitle("FIS Gewässer – Projekt: " + projectName);
    }

    /**
     * DOCUMENT ME!
     */
    public void adoptConfigFile() {
        actionPerformed(null);
    }
}
