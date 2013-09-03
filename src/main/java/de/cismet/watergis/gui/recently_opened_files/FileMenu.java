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
import org.apache.log4j.Logger;

import org.jdom.Element;

import java.awt.Component;

import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.cismet.tools.configuration.Configurable;
import de.cismet.tools.configuration.NoWriteError;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.AdoptLocalConfigFileAction;
import de.cismet.watergis.gui.actions.AdoptServerConfigFileAction;

/**
 * Filemenu is the "File"-Menu in the menubar of the WatergisApp. The static menu items can be added to the menu via the
 * GUI builder. The dynamic menu items are added programmatically.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class FileMenu extends JMenu implements Configurable {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FileMenu.class);

    //~ Instance fields --------------------------------------------------------

    final List<JMenuItem> serverProfileItems = new ArrayList<JMenuItem>();
    final List<Component> before = new ArrayList<Component>();
    final List<Component> after = new ArrayList<Component>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FileMenu object.
     */
    public FileMenu() {
        this.addMenuListener(new MenuListener() {

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
     * Has to be called after the GUI builder has added the menu items to the menu. It saves the items of the menu, to
     * use them if the menu has to be rebuild.
     */
    public void saveComponentsAfterInitialisation() {
        final Component[] comps = this.getMenuComponents();
        List<Component> active = before;

        for (final Component comp : comps) {
            if (active != null) {
                active.add(comp);
            }

            if ((active == before) && (comp.getName() != null)
                        && comp.getName().trim().equals("sepCentralFilesStart")) { // erster Separator//NOI18N
                active = null;
            } else if ((active == null) && (comp.getName() != null)
                        && comp.getName().trim().equals("sepLocalFilesEnd")) {     // zweiter Separator//NOI18N
                active = after;
                after.add(comp);
            }
        }
    }

    /**
     * Removes everything from the menu, adds then the first static items, then the dynamic items and afterwards the
     * second static items.
     */
    private void rebuild() {
        this.removeAll();

        for (final Component component : before) {
            this.add(component);
        }

        for (final JMenuItem serverProfile : serverProfileItems) {
            this.add(serverProfile);
        }

        final Collection<File> fileHistory = AppBroker.getInstance().getRecentlyOpenedFilesList().getFileList();
        if (!fileHistory.isEmpty()) {
            this.add(new javax.swing.JPopupMenu.Separator());
        }

        rebuildLocalFiles(fileHistory);

        for (final Component component : after) {
            this.add(component);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileHistory  DOCUMENT ME!
     */
    private void rebuildLocalFiles(final Collection<File> fileHistory) {
        for (final File file : fileHistory) {
            final JMenuItem menuItem = new JMenuItem(file.getName());
            menuItem.setAction(new AdoptLocalConfigFileAction(file));
            final String fileNameWithOutExt = FilenameUtils.removeExtension(file.getName());
            menuItem.setText(fileNameWithOutExt);
            this.add(menuItem);
        }
    }

    @Override
    public void configure(final Element parent) {
        // do nothing
    }

    @Override
    public void masterConfigure(final Element parent) {
        serverProfileItems.clear();

        final Element serverprofiles = parent.getChild("serverProfiles");              // NOI18N
        final Iterator<Element> it = serverprofiles.getChildren("profile").iterator(); // NOI18N

        while (it.hasNext()) {
            final Element next = it.next();
            final String id = next.getAttributeValue("id");                                 // NOI18N
            final String sorter = next.getAttributeValue("sorter");                         // NOI18N
            final String name = next.getAttributeValue("name");                             // NOI18N
            final String path = next.getAttributeValue("path");                             // NOI18N
            final String icon = next.getAttributeValue("icon");                             // NOI18N
            final String descr = next.getAttributeValue("descr");                           // NOI18N
            final String descrWidth = next.getAttributeValue("descrwidth");                 // NOI18N
            final String complexDescriptionText = next.getTextTrim();
            final String complexDescriptionSwitch = next.getAttributeValue("complexdescr"); // NOI18N

            final JMenuItem serverProfileMenuItem = new JMenuItem();
            serverProfileMenuItem.setAction(new AdoptServerConfigFileAction(path));
            serverProfileMenuItem.setText(name);
            serverProfileMenuItem.setName("ServerProfile:" + sorter + ":" + name); // NOI18N

            if ((complexDescriptionSwitch != null) && complexDescriptionSwitch.equalsIgnoreCase("true") // NOI18N
                        && (complexDescriptionText != null)) {
                serverProfileMenuItem.setToolTipText(complexDescriptionText);
            } else if (descrWidth != null) {
                serverProfileMenuItem.setToolTipText("<html><table width=\"" + descrWidth               // NOI18N
                            + "\" border=\"0\"><tr><td>" + descr + "</p></td></tr></table></html>");    // NOI18N
            } else {
                serverProfileMenuItem.setToolTipText(descr);
            }

            try {
                serverProfileMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon)));
            } catch (Exception iconE) {
                LOG.warn("Could not create Icon for ServerProfile.", iconE); // NOI18N
            }

            serverProfileItems.add(serverProfileMenuItem);

            Collections.sort(serverProfileItems, new Comparator<JMenuItem>() {

                    @Override
                    public int compare(final JMenuItem o1, final JMenuItem o2) {
                        if ((o1.getName() != null) && (o2.getName() != null)) {
                            return o1.getName().compareTo(o2.getName());
                        } else {
                            return 0;
                        }
                    }
                });
        }
    }

    @Override
    public Element getConfiguration() throws NoWriteError {
        // do nothing
        return null;
    }
}
