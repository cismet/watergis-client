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

import org.apache.commons.io.FilenameUtils;


import java.io.File;

import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.AdoptLocalConfigFileAction;
import de.cismet.watergis.gui.actions.LocalConfigAction;

/**
 * A menu which shows the recently opened files
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class RecentlyOpenedFileMenu extends JMenu {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new RecentlyOpenedFileMenu object.
     */
    public RecentlyOpenedFileMenu() {
        super("Open Recent");
        setAction(new LocalConfigAction());

        addMenuListener(new MenuListener() {

                @Override
                public void menuSelected(final MenuEvent e) {
                    rebuild();
                }

                @Override
                public void menuDeselected(final MenuEvent e) {
                }

                @Override
                public void menuCanceled(final MenuEvent e) {
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void rebuild() {
        removeAll();
        final Collection<File> fileHistory = AppBroker.getInstance().getRecentlyOpenedFilesList().getFileList();
        for (final File file : fileHistory) {
            final JMenuItem menuItem = new JMenuItem(file.getName());
            menuItem.setAction(new AdoptLocalConfigFileAction(file));
            final String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
            menuItem.setText(fileNameWithOutExt);
            add(menuItem);
        }
    }
}
