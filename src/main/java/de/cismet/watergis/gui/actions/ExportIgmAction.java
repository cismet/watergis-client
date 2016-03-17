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

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import static javax.swing.Action.NAME;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class ExportIgmAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ExportIgmAction.class);

    //~ Instance fields --------------------------------------------------------

    private ExportAction export;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ExportOptionAction object.
     */
    public ExportIgmAction() {
        super();

        final URL icon = getClass().getResource("/de/cismet/watergis/res/icons16/icon-download-alt.png");
        String text = "IGM";
        String tooltiptext = "IGM-Export";
        String mnemonic = "I";

        try {
            text = NbBundle.getMessage(ExportIgmAction.class,
                    "ExportIgmAction.text");
            tooltiptext = NbBundle.getMessage(ExportIgmAction.class,
                    "ExportIgmAction.toolTipText");
            mnemonic = NbBundle.getMessage(ExportIgmAction.class,
                    "ExportIgmAction.mnemonic");
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
        if (export != null) {
            export.actionPerformed(new ActionEvent(this, 1, "igm"));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  export  DOCUMENT ME!
     */
    public void setExport(final ExportAction export) {
        this.export = export;
    }
}
