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

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class BookmarkManager implements Configurable {

    //~ Instance fields --------------------------------------------------------

    private ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public void add(final Bookmark bookmark) {
        bookmarkList.add(bookmark);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bookmark  DOCUMENT ME!
     */
    public void remove(final Bookmark bookmark) {
        bookmarkList.remove(bookmark);
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
