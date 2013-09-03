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
import com.vividsolutions.jts.geom.Point;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.CreateBookmarkDialog;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class CreateBookmarkAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateBookmarkAction.class);

    //~ Instance fields --------------------------------------------------------

    private CreateBookmarkDialog bookmarkDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CreateBookmarkAction object.
     *
     * @param  bookmarkDialog  DOCUMENT ME!
     */
    public CreateBookmarkAction(final CreateBookmarkDialog bookmarkDialog) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                CreateBookmarkAction.class,
                "CreateBookmarkAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                CreateBookmarkAction.class,
                "CreateBookmarkAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                CreateBookmarkAction.class,
                "CreateBookmarkAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());

        this.bookmarkDialog = bookmarkDialog;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Geometry geom = createGeometrieInMiddleOfMap();
        final Bookmark bookmark = new Bookmark();
        bookmark.setGeometry(geom);
        bookmark.setName(bookmarkDialog.getBookmarkName());
        bookmark.setDescription(bookmarkDialog.getBookmarkDescription());
        AppBroker.getInstance().getBookmarkManager().add(bookmark);
        bookmarkDialog.dispose();
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Geometry createGeometrieInMiddleOfMap() {
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final XBoundingBox boundingBox = (XBoundingBox)mappingComponent.getCurrentBoundingBoxFromCamera();
        final Point middleOfScreen = boundingBox.getGeometry().getCentroid();
        final DefaultStyledFeature feature = new DefaultStyledFeature();
        feature.setGeometry(middleOfScreen);
        mappingComponent.addFeaturesToMap(new Feature[] { feature });

        return middleOfScreen;
    }
}
