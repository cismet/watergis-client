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
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.commons.RestrictedFileSystemView;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.OpenProjectAction;
import de.cismet.watergis.gui.actions.SaveProjectAction;
import de.cismet.watergis.gui.dialog.ManageBookmarksDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class SaveBookmarksAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SaveBookmarksAction.class);

    //~ Instance fields --------------------------------------------------------

    private ManageBookmarksDialog manageBookmarksDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowBookmarkInMapAction object.
     */
    public SaveBookmarksAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                SaveBookmarksAction.class,
                "SaveBookmarksAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                SaveBookmarksAction.class,
                "SaveBookmarksAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                SaveBookmarksAction.class,
                "SaveBookmarksAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final File file = StaticSwingTools.chooseFile(WatergisApp.getDIRECTORYPATH_WATERGIS(),
                true,
                new String[] { "lz" },
                org.openide.util.NbBundle.getMessage(
                    SaveBookmarksAction.class,
                    "SaveBookmarksAction.save.FileFilter.getDescription.return"),
                AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (file != null) {
            AppBroker.getInstance().getBookmarkManager().saveToFile(file);
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
