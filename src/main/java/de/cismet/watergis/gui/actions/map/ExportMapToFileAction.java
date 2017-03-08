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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.ExportMapToFileDialog;

/**
 * An action which opens a dialog, which allows it to save the map shown in the mapping component to a file.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ExportMapToFileAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportMapToFileAction.class);

    //~ Instance fields --------------------------------------------------------

    private ExportMapToFileDialog exportMapToFileDialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapAction object.
     */
    public ExportMapToFileAction() {
        final String text = org.openide.util.NbBundle.getMessage(
                ExportMapToFileAction.class,
                "ExportMapToFileAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ExportMapToFileAction.class,
                "ExportMapToFileAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ExportMapToFileAction.class,
                "ExportMapToFileAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-export.png"));
        putValue(SMALL_ICON, icon);

        exportMapToFileDialog = new ExportMapToFileDialog(AppBroker.getInstance().getWatergisApp(), true);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        StaticSwingTools.showDialog(exportMapToFileDialog);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
