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

import javax.swing.AbstractAction;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;

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
public class AdoptLayoutFileAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AdoptLayoutFileAction.class);

    //~ Instance fields --------------------------------------------------------

    String path;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AdoptServerConfigFileAction object.
     *
     * @param  path  the path to the config file, which is in the classpath
     */
    public AdoptLayoutFileAction(final String path) {
        this.path = path;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        AppBroker.getInstance().getWatergisApp().loadLayout(path);
    }

    /**
     * DOCUMENT ME!
     */
    public void adoptLayoutFile() {
        actionPerformed(null);
    }
}
