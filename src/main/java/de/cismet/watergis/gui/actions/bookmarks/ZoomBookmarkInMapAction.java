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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.cismap.commons.XBoundingBox;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.ManageBookmarksDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ZoomBookmarkInMapAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ZoomBookmarkInMapAction.class);

    //~ Instance fields --------------------------------------------------------

    private ManageBookmarksDialog manageBookmarksDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowBookmarkInMapAction object.
     */
    public ZoomBookmarkInMapAction() {
        this(null);
    }

    /**
     * Creates a new ShowBookmarkInMapAction object.
     *
     * @param  manageBookmarksDialog  DOCUMENT ME!
     */
    public ZoomBookmarkInMapAction(final ManageBookmarksDialog manageBookmarksDialog) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ZoomBookmarkInMapAction.class,
                "ZoomBookmarkInMapAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                ZoomBookmarkInMapAction.class,
                "ZoomBookmarkInMapAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ZoomBookmarkInMapAction.class,
                "ZoomBookmarkInMapAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        this.manageBookmarksDialog = manageBookmarksDialog;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Bookmark bookmark = manageBookmarksDialog.getSelectedBookmark();

        final Geometry geom = bookmark.getGeometry();
        final XBoundingBox xbox = new XBoundingBox(geom);
        AppBroker.getInstance().getMappingComponent().gotoBoundingBox(xbox, true, true, 500);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
