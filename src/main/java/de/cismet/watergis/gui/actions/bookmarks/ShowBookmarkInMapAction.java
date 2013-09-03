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

import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.ManageBookmarksDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ShowBookmarkInMapAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ShowBookmarkInMapAction.class);

    //~ Instance fields --------------------------------------------------------

    private ManageBookmarksDialog manageBookmarksDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ShowBookmarkInMapAction object.
     */
    public ShowBookmarkInMapAction() {
        this(null);
    }

    /**
     * Creates a new ShowBookmarkInMapAction object.
     *
     * @param  manageBookmarksDialog  DOCUMENT ME!
     */
    public ShowBookmarkInMapAction(final ManageBookmarksDialog manageBookmarksDialog) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ShowBookmarkInMapAction.class,
                "ShowBookmarkInMapAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                ShowBookmarkInMapAction.class,
                "ShowBookmarkInMapAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ShowBookmarkInMapAction.class,
                "ShowBookmarkInMapAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        this.manageBookmarksDialog = manageBookmarksDialog;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Bookmark bookmark = manageBookmarksDialog.getSelectedBookmark();
        final DefaultStyledFeature feature = new DefaultStyledFeature();
        feature.setGeometry(bookmark.getGeometry().buffer(20));
        final XBoundingBox box = new XBoundingBox(feature.getGeometry());
//        AppBroker.getInstance().getMappingComponent().getFeatureCollection().removeAllFeatures();
        AppBroker.getInstance().getMappingComponent().getFeatureCollection().addFeature(feature);
        // AppBroker.getInstance().getMappingComponent().zoomToFeatureCollection();
        final ArrayList featureCollection = new ArrayList();
        featureCollection.add(feature);
        AppBroker.getInstance().getMappingComponent().zoomToFeatureCollection();
        // ppBroker.getInstance().getMappingComponent().gotoBoundingBoxWithHistory(box);
    }

    @Override
    public boolean isEnabled() {
        return false || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
