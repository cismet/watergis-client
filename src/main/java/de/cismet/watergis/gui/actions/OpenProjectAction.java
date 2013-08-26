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
import java.awt.event.KeyEvent;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.RestrictedFileSystemView;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class OpenProjectAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(OpenProjectAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new OpenProjectAction object.
     */
    public OpenProjectAction() {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_O,
                ActionEvent.CTRL_MASK));
        final String tooltip = org.openide.util.NbBundle.getMessage(
                OpenProjectAction.class,
                "OpenProjectAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(OpenProjectAction.class, "OpenProjectAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                OpenProjectAction.class,
                "OpenProjectAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-folder-open.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Load Project");
        }
        load();
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    /**
     * DOCUMENT ME!
     */
    private void load() {
        JFileChooser fc;

        try {
            fc = new JFileChooser(WatergisApp.DIRECTORYPATH_WATERGIS);
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.DIRECTORYPATH_WATERGIS, new RestrictedFileSystemView());
        }

        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory()
                                || f.getName().toLowerCase().endsWith(".xml"); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            CismapPlugin.class,
                            "CismapPlugin.mniLoadConfigActionPerformed.FileFiltergetDescription.return"); // NOI18N
                }
            });

        final int state = fc.showOpenDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (state == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            final String name = file.getAbsolutePath();

            final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
            final ConfigurationManager configurationManager = AppBroker.getConfigManager();
            if (name.endsWith(".xml")) { // NOI18N
                // activeLayers.removeAllLayers();
                mappingComponent.getRasterServiceLayer().removeAllChildren();
                configurationManager.configure(name);
            } else {
                // activeLayers.removeAllLayers();
                mappingComponent.getRasterServiceLayer().removeAllChildren();
                configurationManager.configure(name + ".xml"); // NOI18N
            }
        }
    }
}
