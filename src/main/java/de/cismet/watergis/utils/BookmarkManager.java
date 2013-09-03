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
package de.cismet.watergis.utils;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.actions.bookmarks.MenuBookMarkAction;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class BookmarkManager implements Configurable {

    //~ Instance fields --------------------------------------------------------

    private ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
    private HashMap<Bookmark, JMenuItem> bookmarkMenuItemMap = new HashMap<Bookmark, JMenuItem>();
    private JPopupMenu.Separator separator;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public void add(final Bookmark bookmark) {
        bookmarkList.add(bookmark);

        final JMenu menu = (JMenu)AppBroker.getInstance().getComponent(ComponentName.MENU_BOOKMARK);
        if (separator == null) {
            separator = new JPopupMenu.Separator();
            menu.add(separator);
        }

        final MenuBookMarkAction action = new MenuBookMarkAction(bookmark);
        final JMenuItem menuItem = new JMenuItem();
        menuItem.setAction(action);
        menu.add(menuItem);

        bookmarkMenuItemMap.put(bookmark, menuItem);

        Collections.sort(bookmarkList);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public void remove(final Bookmark bookmark) {
        bookmarkList.remove(bookmark);

        final JMenu menu = (JMenu)AppBroker.getInstance().getComponent(ComponentName.MENU_BOOKMARK);
        menu.remove(bookmarkMenuItemMap.get(bookmark));

        bookmarkMenuItemMap.remove(bookmark);

        if (bookmarkList.isEmpty()) {
            menu.remove(separator);
            separator = null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection<Bookmark> getBookmarks() {
        return bookmarkList;
    }

    @Override
    public void configure(final Element parent) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void masterConfigure(final Element parent) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        // throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }
}
