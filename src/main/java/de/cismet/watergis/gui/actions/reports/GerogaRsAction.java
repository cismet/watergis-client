/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.actions.reports;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.net.URL;

import java.util.MissingResourceException;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.dialog.FgGerogaRsDialog;

import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static javax.swing.Action.SMALL_ICON;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class GerogaRsAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(GerinneGGewaesserReportAction.class);

    //~ Instance fields --------------------------------------------------------

    private FgGerogaRsDialog dialog;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DissolveGeoprocessingAction object.
     */
    public GerogaRsAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-spiderweb.png");
        String text = "Randstreifen";
        String tooltiptext = "Randstreifen";
        String mnemonic = "R";

        try {
            text = NbBundle.getMessage(GerogaRsAction.class,
                    "GerogaRsAction.text");
            tooltiptext = NbBundle.getMessage(
                    GerogaRsAction.class,
                    "GerogaRsAction.toolTipText");
            mnemonic = NbBundle.getMessage(
                    GerogaRsAction.class,
                    "GerogaRsAction.mnemonic");
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
        if (dialog == null) {
            dialog = FgGerogaRsDialog.getInstance();
            dialog.setSize(980, 690);
        }

        StaticSwingTools.centerWindowOnScreen(dialog);
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
