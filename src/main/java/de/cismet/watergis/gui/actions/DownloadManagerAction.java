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

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.downloadmanager.DownloadManagerDialog;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;
import javax.swing.KeyStroke;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class DownloadManagerAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(de.cismet.tools.gui.downloadmanager.DownloadManagerAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DownloadManagerAction object.
     */
    public DownloadManagerAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-download-alt.png");
        String text = "Download-Manager";
        String tooltiptext = "Anzeigen der Downloads";
        String mnemonic = "D";

        try {
            text = NbBundle.getMessage(
                    DownloadManagerAction.class,
                    "DownloadManagerAction.text");
            tooltiptext = NbBundle.getMessage(
                    DownloadManagerAction.class,
                    "DownloadManagerAction.toolTipText");
            mnemonic = NbBundle.getMessage(
                    DownloadManagerAction.class,
                    "DownloadManagerAction.mnemonic");
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
        final JDialog downloadManager = DownloadManagerDialog.instance(AppBroker.getInstance().getComponent(
                    ComponentName.MAIN));
        downloadManager.pack();
        StaticSwingTools.showDialog(downloadManager);
    }
}
