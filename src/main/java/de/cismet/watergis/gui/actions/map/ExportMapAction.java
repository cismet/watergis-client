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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.gui.ClipboardWaitDialog;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.ImageSelection;

/**
 * An Action which exports the map shown in the mapping component to the system clipboard.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ExportMapAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportMapAction.class);

    //~ Instance fields --------------------------------------------------------

    private ClipboardWaitDialog clipboarder;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportMapAction object.
     */
    public ExportMapAction() {
        final String text = org.openide.util.NbBundle.getMessage(ExportMapAction.class, "ExportMapAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(ExportMapAction.class, "ExportMapAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
//        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
//                KeyEvent.VK_C,
//                ActionEvent.CTRL_MASK));
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ExportMapAction.class,
                "ExportMapAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-copy.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (clipboarder == null) {
            clipboarder = new ClipboardWaitDialog(AppBroker.getInstance().getWatergisApp(), true);
        }

        new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    final ImageSelection imgSel;
                    imgSel = new ImageSelection(AppBroker.getInstance().getMappingComponent().getImage());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
                    return null;
                }

                @Override
                protected void done() {
//                    clipboarder.dispose();
                }
            }.execute();
        // do not show the dialog. See Issue 395
// StaticSwingTools.showDialog(clipboarder);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
