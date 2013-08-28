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
package de.cismet.watergis.gui.recently_opened_files;

import org.apache.log4j.Logger;

import org.jdom.Element;

import java.io.File;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

/**
 * A List which manages the last opened files. The last used file is on the first position in the list. A maximal amount
 * of files can be set.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class RecentlyOpenedFilesList implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(RecentlyOpenedFilesList.class);

    //~ Instance fields --------------------------------------------------------

    private int maxAmount = 5;
    private LinkedList<File> fileList = new LinkedList<File>();

    //~ Methods ----------------------------------------------------------------

    /**
     * Add a file on the first position of the List. If too many files are in the list (more than maxAmount), then the
     * oldest files will be removed from the list.
     *
     * @param  file  added to the first position of the list
     */
    public void addFile(final File file) {
        if (fileList.contains(file)) {
            fileList.remove(file);
        }
        fileList.push(file);
        while (fileList.size() > maxAmount) {
            fileList.removeLast();
        }
    }

    /**
     * Returns the list with the files, the last opened file is on the first position.
     *
     * @return  list with the files
     */
    public Collection<File> getFileList() {
        if (fileList.size() < maxAmount) {
            return fileList;
        } else {
            return fileList.subList(0, maxAmount);
        }
    }

    @Override
    public void configure(final Element parent) {
        try {
            final Element prefs = parent.getChild("watergisRecentlyOpenedLocalConfigFiles");
            final Element maxAmountElement = prefs.getChild("maxAmount");
            maxAmount = Integer.parseInt(maxAmountElement.getText());

            final Element files = prefs.getChild("files");
            final List<Element> filepaths = files.getChildren("file");

            fileList.clear();
            for (int i = 0; i < filepaths.size(); i++) {
                final File file = new File(filepaths.get(i).getText());
                fileList.addLast(file);
            }
        } catch (final Exception skip) {
            LOG.warn("Error while reading the list with the recently opened files", skip); // NOI18N
        }
    }

    @Override
    public void masterConfigure(final Element parent) {
        // do nothing
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        final Element root = new Element("watergisRecentlyOpenedLocalConfigFiles");

        final Element maxAmountElement = new Element("maxAmount");
        maxAmountElement.addContent(Integer.toString(maxAmount));
        root.addContent(maxAmountElement);

        final Element files = new Element("files");
        for (int i = 0; (i < fileList.size()) && (i < maxAmount); i++) {
            final Element file = new Element("file");
            file.addContent(fileList.get(i).getAbsolutePath());
            files.addContent(file);
        }

        root.addContent(files);

        return root;
    }

    /**
     * Set the maximal amount of files, which are managed by the list.
     *
     * @return  maximal amount of files
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Set the maximal amount of files, which will be managed by the list.
     *
     * @param  maxAmount  maximal amount of files
     */
    public void setMaxAmount(final int maxAmount) {
        this.maxAmount = maxAmount;
    }
}
