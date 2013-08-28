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
public class SaveProjectAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SaveProjectAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SaveProjectAction object.
     */
    public SaveProjectAction() {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_S,
                ActionEvent.CTRL_MASK));
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SaveProjectAction.class,
                "SaveProjectAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(SaveProjectAction.class, "SaveProjectAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SaveProjectAction.class,
                "SaveProjectAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-save-floppy.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Save Project");
        }
        save();
    }
    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    /**
     * DOCUMENT ME!
     */
    private void save() {
        JFileChooser fc;

        try {
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS());
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS(), new RestrictedFileSystemView());
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
                            "CismapPlugin.mniSaveConfigActionPerformed.FileFilter.getDescription.return"); // NOI18N
                }
            });

        final int state = fc.showSaveDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));
        if (LOG.isDebugEnabled()) {
            LOG.debug("state:" + state); // NOI18N
        }

        final ConfigurationManager configurationManager = AppBroker.getConfigManager();
        if (state == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            final String name = file.getAbsolutePath();

            if (name.endsWith(".xml")) {                                // NOI18N
                configurationManager.writeConfiguration(name);
            } else {
                configurationManager.writeConfiguration(name + ".xml"); // NOI18N
            }
            AppBroker.getInstance().getRecentlyOpenedFilesList().addFile(file);
        }
    }
}
