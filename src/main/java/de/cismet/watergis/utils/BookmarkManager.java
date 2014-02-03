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

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.openide.util.Exceptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.cismet.cids.custom.beans.watergis.Bookmark;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;
import de.cismet.watergis.gui.actions.bookmarks.MenuBookMarkAction;
import de.cismet.watergis.gui.recently_opened_files.RecentlyOpenedFilesList;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class BookmarkManager implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RecentlyOpenedFilesList.class);

    private static final String XML_ENCODING;

    static {
        final String charset = Charset.defaultCharset().toString();
        if ("MacRoman".equals(charset)) { // NOI18N
            XML_ENCODING = "UTF-8";
        } else {
            XML_ENCODING = "ISO-8859-1";
        }
    }

    //~ Instance fields --------------------------------------------------------

    private String bookmarkFilePath = WatergisApp.getDIRECTORYPATH_WATERGIS()
                + System.getProperty("file.separator") + "bookmarks.xml";
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
        menu.add(menuItem, 3);

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
     */
    private void clean() {
        final List<Bookmark> tmpList = new ArrayList<Bookmark>();
        tmpList.addAll(bookmarkList);

        for (final Bookmark bm : tmpList) {
            remove(bm);
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
        loadFromFile(new File(bookmarkFilePath));
    }

    @Override
    public void masterConfigure(final Element parent) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        saveToFile(new File(bookmarkFilePath));
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  bookmarkFile  DOCUMENT ME!
     */
    public void loadFromFile(final File bookmarkFile) {
        try {
            final SAXBuilder builder = new SAXBuilder(false);
            final Document doc = builder.build(bookmarkFile);

            final Element rootObject = doc.getRootElement();
            if (rootObject != null) {
                final Element bookmarkListElement = rootObject.getChild("bookmarks");
                final List<Element> bookmarks = bookmarkListElement.getChildren("bookmark");

                clean();
//                bookmarkList.clear();

                for (int i = 0; i < bookmarks.size(); i++) {
                    add(new Bookmark(bookmarks.get(i)));
                }
            }
        } catch (final Exception e) {
            LOG.warn("Error while reading the bookmars from the file" + bookmarkFile.toString(), e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    public void saveToFile(final File file) {
        OutputStreamWriter writer = null;
        try {
            final Element root = new Element("bookmarkManager");

            final Element bookmarks = new Element("bookmarks");
            for (int i = 0; i < bookmarkList.size(); i++) {
                bookmarks.addContent(bookmarkList.get(i).toElement());
            }
            root.addContent(bookmarks);
            final Document doc = new Document(root);
            final Format format = Format.getPrettyFormat();
            format.setEncoding(XML_ENCODING); // NOI18N

            final XMLOutputter serializer = new XMLOutputter(format);
            writer = new OutputStreamWriter(new FileOutputStream(file), XML_ENCODING);
            serializer.output(doc, writer);
            writer.flush();
        } catch (Exception ex) {
            LOG.error("Error while saving bookmarks", ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
