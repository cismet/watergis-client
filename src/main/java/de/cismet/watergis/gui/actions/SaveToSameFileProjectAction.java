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
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.tools.configuration.ConfigurationManager;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SaveToSameFileProjectAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SaveToSameFileProjectAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SaveProjectAction object.
     */
    public SaveToSameFileProjectAction() {
//        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
//                KeyEvent.VK_S,
//                ActionEvent.CTRL_MASK));
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SaveToSameFileProjectAction.class,
                "SaveToSameFileProjectAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SaveToSameFileProjectAction.class,
                "SaveToSameFileProjectAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SaveToSameFileProjectAction.class,
                "SaveToSameFileProjectAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-savetodrive.png"));
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
        final String currentLayout = AppBroker.getInstance().getWatergisApp().getCurrentLayoutFile();
        File file = null;

        if (currentLayout != null) {
            file = new File(currentLayout);
        }
//        else {
//            file = StaticSwingTools.chooseFile(WatergisApp.getDIRECTORYPATH_WATERGIS(),
//                    true,
//                    new String[] { "xml" },
//                    org.openide.util.NbBundle.getMessage(
//                        SaveToSameFileProjectAction.class,
//                        "SaveProjectAction.save.FileFilter.getDescription.return"),
//                    AppBroker.getInstance().getComponent(ComponentName.MAIN));
//        }

        if (file != null) {
            final ConfigurationManager configurationManager = AppBroker.getConfigManager();
            final String name = file.getAbsolutePath();

            configurationManager.writeConfiguration(name);
            AppBroker.getInstance().getRecentlyOpenedFilesList().addFile(file);

//            final String layoutPath = FilenameUtils.getFullPath(name) + FilenameUtils.getBaseName(name) + ".layout";
//            AppBroker.getInstance().getWatergisApp().saveLayout(layoutPath);
        } else {
            final ConfigurationManager configurationManager = AppBroker.getConfigManager();
            configurationManager.writeConfiguration();
        }
    }
}
