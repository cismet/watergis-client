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
package de.cismet.watergis.gui.actions.bookmarks;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.dialog.ManageBookmarksDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class LoadBookmarksAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(LoadBookmarksAction.class);

    //~ Instance fields --------------------------------------------------------

    private ManageBookmarksDialog manageBookmarksDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowBookmarkInMapAction object.
     */
    public LoadBookmarksAction() {
        this(null);
    }

    /**
     * Creates a new ShowBookmarkInMapAction object.
     *
     * @param  manageBookmarksDialog  DOCUMENT ME!
     */
    public LoadBookmarksAction(final ManageBookmarksDialog manageBookmarksDialog) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                LoadBookmarksAction.class,
                "LoadBookmarksAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                LoadBookmarksAction.class,
                "LoadBookmarksAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                LoadBookmarksAction.class,
                "LoadBookmarksAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        this.manageBookmarksDialog = manageBookmarksDialog;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final File file = StaticSwingTools.chooseFile(WatergisApp.getDIRECTORYPATH_WATERGIS(),
                false,
                new String[] { "lz" },
                org.openide.util.NbBundle.getMessage(
                    LoadBookmarksAction.class,
                    "LoadBookmarksAction.load.FileFilter.getDescription.return"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            if (file.exists()) {
                AppBroker.getInstance().getBookmarkManager().loadFromFile(file);
                if (manageBookmarksDialog != null) {
                    manageBookmarksDialog.updateModel();
                }
            } else {
                LOG.warn("Bookmark file, which the user wanted to open, does not exist.");
                final String message = org.openide.util.NbBundle.getMessage(
                        LoadBookmarksAction.class,
                        "LoadBookmarksAction.load.fileDoesNotExist.message");
                final String title = org.openide.util.NbBundle.getMessage(
                        LoadBookmarksAction.class,
                        "LoadBookmarksAction.load.fileDoesNotExist.title");

                JOptionPane.showMessageDialog(
                    AppBroker.getInstance().getMappingComponent(),
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
