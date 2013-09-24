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
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.tools.StaticDecimalTools;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class GoToAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GoToAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public GoToAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-map-marker.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Goto to position");
        }
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        final XBoundingBox c = (XBoundingBox)mappingComponent.getCurrentBoundingBoxFromCamera();
        final double x = (c.getX1() + c.getX2()) / 2;
        final double y = (c.getY1() + c.getY2()) / 2;
        final String message = org.openide.util.NbBundle.getMessage(
                GoToAction.class,
                "GoToAction.actionPerformed().dialogMessage");

        final String s = JOptionPane.showInputDialog(
                AppBroker.getInstance().getComponent(ComponentName.MAIN),
                message,
                StaticDecimalTools.round(x)
                        + ","
                        + StaticDecimalTools.round(y));

        try {
            final String[] sa = s.split(",");
            final Double gotoX = new Double(sa[0]);
            final Double gotoY = new Double(sa[1]);
            final String currentCrsCode = CismapBroker.getInstance().getSrs().getCode();
            final XBoundingBox bb = new XBoundingBox(
                    gotoX,
                    gotoY,
                    gotoX,
                    gotoY,
                    currentCrsCode,
                    mappingComponent.isInMetricSRS());
            mappingComponent.gotoBoundingBox(bb, true, false, mappingComponent.getAnimationDuration());
        } catch (Exception skip) {
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
