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

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.util.Direction;

import org.apache.log4j.Logger;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.BasicGuiComponentProvider;
import de.cismet.tools.gui.StaticSwingTools;

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
