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

import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.URL;

import java.util.MissingResourceException;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipOutputStream;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AddThemeAction extends AbstractAction implements ActionListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AddThemeAction.class);

    //~ Instance fields --------------------------------------------------------

    private CapabilityWidget pCapabilities;
    private RootWindow rootWindow;
    private View vCapability;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportOptionAction object.
     */
    public AddThemeAction() {
        super();
        init(null, null, null);
    }

    /**
     * Creates a new ExportOptionAction object.
     *
     * @param  pCapabilities  DOCUMENT ME!
     * @param  rootWindow     DOCUMENT ME!
     * @param  vCapability    DOCUMENT ME!
     */
    public AddThemeAction(final CapabilityWidget pCapabilities, final RootWindow rootWindow, final View vCapability) {
        super();
        init(pCapabilities, rootWindow, vCapability);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  pCapabilities  DOCUMENT ME!
     * @param  rootWindow     DOCUMENT ME!
     * @param  vCapability    DOCUMENT ME!
     */
    public void init(final CapabilityWidget pCapabilities, final RootWindow rootWindow, final View vCapability) {
        this.pCapabilities = pCapabilities;
        this.rootWindow = rootWindow;
        this.vCapability = vCapability;

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-layers.png");
        String text = "Thema hinzufügen";
        String tooltiptext = "Thema hinzufügen";
        String mnemonic = "T";

        try {
            text = NbBundle.getMessage(AddThemeAction.class,
                    "AddThemeAction.text");
            tooltiptext = NbBundle.getMessage(AddThemeAction.class,
                    "AddThemeAction.toolTipText");
            mnemonic = NbBundle.getMessage(AddThemeAction.class,
                    "AddThemeAction.mnemonic");
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

    @Override
    public void actionPerformed(final ActionEvent e) {
        showView();
    }

    /**
     * DOCUMENT ME!
     */
    private void showView() {
        final Dimension prefSize = pCapabilities.getPreferredSize();
        final Dimension size = pCapabilities.getSize();
        Dimension sizeToUse;
        if ((prefSize.height * prefSize.width) >= (size.height * size.width)) {
            sizeToUse = prefSize;
        } else {
            sizeToUse = size;
        }

        final FloatingWindow fw = rootWindow.createFloatingWindow(
                getPointOfCenterOfScreen(sizeToUse),
                sizeToUse,
                vCapability);
        fw.getTopLevelAncestor().setVisible(true);
    }

    /**
     * Centers a Dimension instance on the screen on which the mouse pointer is located.
     *
     * @param   d  w window instance to be centered
     *
     * @return  DOCUMENT ME!
     *
     * @see     StaticSwingTools..centerWindowOnScreen()
     */
    public Point getPointOfCenterOfScreen(final Dimension d) {
        final PointerInfo pInfo = MouseInfo.getPointerInfo();
        final Point pointerLocation = pInfo.getLocation();

        // determine screen boundaries w.r.t. the current mouse position
        final GraphicsConfiguration[] cfgArr = pInfo.getDevice().getConfigurations();

        Rectangle bounds = null;
        for (int i = 0; i < cfgArr.length; i++) {
            bounds = cfgArr[i].getBounds();

            if (pointerLocation.x <= bounds.x) {
                break;
            }
        }

        // determine coordinates in the center of the current mouse location
        final int x = (int)(bounds.x + ((bounds.width - d.getWidth()) / 2));
        final int y = (int)(bounds.y + ((bounds.height - d.getHeight()) / 2));

        return new Point(x, y);
    }
}
