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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.cismap.commons.XBoundingBox;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class MenuBookMarkAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    Bookmark bookmark;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MenuBookMarkAction object.
     */
    public MenuBookMarkAction() {
        this(null);
    }

    /**
     * Creates a new MenuBookMarkAction object.
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public MenuBookMarkAction(final Bookmark bookmark) {
        this.bookmark = bookmark;

        putValue(NAME, bookmark.getName());
        putValue(SHORT_DESCRIPTION, bookmark.getDescription());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-bookmarkfour.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Geometry geom = bookmark.getGeometry();
        final XBoundingBox xbox = new XBoundingBox(geom);
        AppBroker.getInstance().getMappingComponent().gotoBoundingBox(xbox, true, true, 500);
    }
}
