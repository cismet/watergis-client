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
package de.cismet.watergis.gui.actions.map;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import de.cismet.cismap.DrawingManager;

import de.cismet.cismap.commons.RestrictedFileSystemView;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

import de.cismet.watergis.gui.WatergisApp;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LoadDrawingsAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(SaveDrawingsAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public LoadDrawingsAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                LoadDrawingsAction.class,
                "LoadDrawingsAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-importfile.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        JFileChooser fc;

        try {
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS());
        } catch (Exception bug) {
            // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
            fc = new JFileChooser(WatergisApp.getDIRECTORYPATH_WATERGIS(), new RestrictedFileSystemView());
        }

        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(final File f) {
                    return f.isDirectory()
                                || f.getName().toLowerCase().endsWith(".ze"); // NOI18N
                }

                @Override
                public String getDescription() {
                    return org.openide.util.NbBundle.getMessage(
                            LoadDrawingsAction.class,
                            "LoadDrawingsAction.actionPerformed.FileFilter.getDescription"); // NOI18N
                }
            });

        final int state = fc.showOpenDialog(AppBroker.getInstance().getComponent(ComponentName.MAIN));

        if (state == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            if (file.exists()) {
                try {
//                    DrawingManager.removeAllFeatures();
                    DrawingManager.addFeatures(file);
                    DrawingManager.loadFeatures();
                } catch (Exception ex) {
                    LOG.error("Error while loading drawings.", ex);
                }
            } else {
                LOG.warn("Drawing file, which the user wanted to open, does not exist.");
                final String message = org.openide.util.NbBundle.getMessage(
                        LoadDrawingsAction.class,
                        "LoadDrawingsAction.actionPerformed.fileDoesNotExist.message");
                final String title = org.openide.util.NbBundle.getMessage(
                        LoadDrawingsAction.class,
                        "LoadDrawingsAction.actionPerformed.fileDoesNotExist.title");

                JOptionPane.showMessageDialog(
                    AppBroker.getInstance().getMappingComponent(),
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
