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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

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
        final File file = StaticSwingTools.chooseFile(WatergisApp.getDIRECTORYPATH_WATERGIS(),
                false,
                new String[] { "xml" },
                org.openide.util.NbBundle.getMessage(
                    OpenProjectAction.class,
                    "OpenProjectAction.load.FileFilter.getDescription.return"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            if (file.exists()) {
                new AdoptLocalConfigFileAction(file).adoptConfigFile();
            } else {
                LOG.warn("Config file, which the user wanted to open, does not exist.");
                final String message = org.openide.util.NbBundle.getMessage(
                        OpenProjectAction.class,
                        "OpenProjectAction.load.fileDoesNotExist.message");
                final String title = org.openide.util.NbBundle.getMessage(
                        OpenProjectAction.class,
                        "OpenProjectAction.load.fileDoesNotExist.title");

                JOptionPane.showMessageDialog(
                    AppBroker.getInstance().getMappingComponent(),
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
