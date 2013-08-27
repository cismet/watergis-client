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

import de.cismet.cismap.commons.gui.MappingComponent;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.gui.StaticSwingTools;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.actions.selection.*;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ScaleAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ScaleAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public ScaleAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ScaleAction.class,
                "ScaleAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(
                ScaleAction.class,
                "ScaleAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                ScaleAction.class,
                "ScaleAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-ruler.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mappingComponent = AppBroker.getInstance().getMappingComponent();
        try {
            final String s = JOptionPane.showInputDialog(
                    StaticSwingTools.getParentFrame(mappingComponent),
                    org.openide.util.NbBundle.getMessage(
                        ScaleAction.class,
                        "ScaleAction.actionPerformed().InfoDialog.message"),
                    ((int)mappingComponent.getScaleDenominator())
                            + "");                               // NOI18N
            final Integer i = new Integer(s);
            mappingComponent.gotoBoundingBoxWithHistory(mappingComponent.getBoundingBoxFromScale(i));
        } catch (Exception skip) {
            LOG.error("Error in mniScaleActionPerformed", skip); // NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
