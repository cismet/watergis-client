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

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.ClipboardWaitDialog;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.GeoLinkUrl;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class CreateGeoLinkAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CreateGeoLinkAction.class);

    //~ Instance fields --------------------------------------------------------

    //TODO wrong port
    int httpInterfacePort = 9099;

    private ClipboardWaitDialog clipboarder;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public CreateGeoLinkAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                CreateGeoLinkAction.class,
                "CreateGeoLinkAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(CreateGeoLinkAction.class, "CreateGeoLinkAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                CreateGeoLinkAction.class,
                "CreateGeoLinkAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-link.png"));
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
                    final XBoundingBox bb = (XBoundingBox)AppBroker.getInstance().getMappingComponent()
                                .getCurrentBoundingBoxFromCamera();
                    final String u = "http://localhost:" + httpInterfacePort + "/gotoBoundingBox?x1="
                                + bb.getX1()                                                       // NOI18N
                                + "&y1=" + bb.getY1() + "&x2=" + bb.getX2() + "&y2=" + bb.getY2(); // NOI18N
                    final GeoLinkUrl url = new GeoLinkUrl(u);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(url, null);
                    return null;
                }

                @Override
                protected void done() {
                    clipboarder.dispose();
                }
            }.execute();
        StaticSwingTools.showDialog(clipboarder);
    }

    @Override
    public boolean isEnabled() {
        return false || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
