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

import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class AdoptLocalConfigFileAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    File file;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdoptLocalConfigFileAction object.
     *
     * @param  file  DOCUMENT ME!
     */
    public AdoptLocalConfigFileAction(final File file) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                LocalConfigAction.class,
                "LocalConfigAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-explorerwindow.png"));
        putValue(SMALL_ICON, icon);
        this.file = file;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        final String filename = file.getAbsolutePath();

        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final ConfigurationManager configurationManager = AppBroker.getConfigManager();
        if (filename.endsWith(".xml")) { // NOI18N
            // activeLayers.removeAllLayers();
            mappingComponent.getRasterServiceLayer().removeAllChildren();
            configurationManager.configure(filename);
        } else {
            // activeLayers.removeAllLayers();
            mappingComponent.getRasterServiceLayer().removeAllChildren();
            configurationManager.configure(filename + ".xml"); // NOI18N
        }
        AppBroker.getInstance().getRecentlyOpenedFilesList().addFile(file);
    }

    /**
     * DOCUMENT ME!
     */
    public void adoptConfigFile() {
        actionPerformed(null);
    }
}
