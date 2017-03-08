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
package de.cismet.watergis.gui.actions.selection;

import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import de.cismet.cismap.DrawingManager;

import de.cismet.cismap.commons.features.DrawingSLDStyledFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToFeaturesWorker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ZoomAllDrawingsAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ZoomAllDrawingsAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public ZoomAllDrawingsAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ZoomAllDrawingsAction.class,
                "ZoomAllDrawingsAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                ZoomAllDrawingsAction.class,
                "ZoomAllDrawingsAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ZoomAllDrawingsAction.class,
                "ZoomAllDrawingsAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-resize.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final List<DrawingSLDStyledFeature> sel = DrawingManager.getInstance().getAllFeatures();

        final ZoomToFeaturesWorker worker = new ZoomToFeaturesWorker(sel.toArray(new Feature[sel.size()]), 10);
        worker.execute();
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
