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

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.ManageBookmarksDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class DeleteBookmarkAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DeleteBookmarkAction.class);

    //~ Instance fields --------------------------------------------------------

    private ManageBookmarksDialog manageBookmarksDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DeleteBookmarkAction object.
     */
    public DeleteBookmarkAction() {
        this(null);
    }

    /**
     * Creates a new DeleteBookmarkAction object.
     *
     * @param  manageBookmarksDialog  DOCUMENT ME!
     */
    public DeleteBookmarkAction(final ManageBookmarksDialog manageBookmarksDialog) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                DeleteBookmarkAction.class,
                "DeleteBookmarkAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                DeleteBookmarkAction.class,
                "DeleteBookmarkAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                DeleteBookmarkAction.class,
                "DeleteBookmarkAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        this.manageBookmarksDialog = manageBookmarksDialog;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Bookmark selectedBookmark = manageBookmarksDialog.getSelectedBookmark();
        manageBookmarksDialog.removeBookmarkFromList(selectedBookmark);
        AppBroker.getInstance().getBookmarkManager().remove(selectedBookmark);
    }
    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
