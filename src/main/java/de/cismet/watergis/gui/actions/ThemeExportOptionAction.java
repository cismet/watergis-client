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
package de.cismet.watergis.gui.actions;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.MissingResourceException;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.gui.dialog.ExportDialog;
import de.cismet.watergis.gui.dialog.ThemeExportDialog;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ThemeExportOptionAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ThemeExportOptionAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportOptionAction object.
     */
    public ThemeExportOptionAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-settingsthree-gears.png");
        String text = "Exportoptionen";
        String tooltiptext = "Exportoptionen";
        String mnemonic = "E";

        try {
            text = NbBundle.getMessage(ThemeExportOptionAction.class,
                    "ThemeExportOptionAction.text");
            tooltiptext = NbBundle.getMessage(ThemeExportOptionAction.class,
                    "ThemeExportOptionAction.toolTipText");
            mnemonic = NbBundle.getMessage(ThemeExportOptionAction.class,
                    "ThemeExportOptionAction.mnemonic");
        } catch (MissingResourceException e) {
            LOG.error("Couldn't find resources. Using fallback settings.", e);
        }

        if (icon != null) {
            putValue(SMALL_ICON, new javax.swing.ImageIcon(icon));
        }

        putValue(SHORT_DESCRIPTION, tooltiptext);
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        putValue(NAME, text);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(ThemeExportDialog.getInstance());
    }
}
