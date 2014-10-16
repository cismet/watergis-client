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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.KeyStroke;

import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/**
 * DOCUMENT ME!
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class InfoWindowAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(InfoWindowAction.class);

    //~ Instance fields --------------------------------------------------------

    private JDialog dialog = null;
    private FeatureInfoPanel featureInfoPanel;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public InfoWindowAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                InfoWindowAction.class,
                "InfoWindowAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(InfoWindowAction.class, "InfoWindowAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(
                InfoWindowAction.class,
                "InfoWindowAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-info-sign.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        initDialog();

        AppBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.FEATURE_INFO_MULTI_GEOM);
        putValue(SELECTED_KEY, Boolean.TRUE);
        dialog.setVisible(true);
    }

    /**
     * Initializes the info dialog, if it is not already initialized.
     */
    private void initDialog() {
        if (dialog == null) {
            dialog = new JDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(InfoWindowAction.class, "InfoWindowAction.actionPerformed.JDialog"),
                    false);
            featureInfoPanel = new FeatureInfoPanel(
                    AppBroker.getInstance().getMappingComponent(),
                    (ThemeLayerWidget)AppBroker.getInstance().getComponent(ComponentName.TREE));
            dialog.add(featureInfoPanel);
            dialog.setAlwaysOnTop(true);
            dialog.setSize(350, 550);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(final WindowEvent e) {
                        if (featureInfoPanel.dispose()) {
                            dialog.setVisible(false);
                        }
                    }

                    @Override
                    public void windowClosed(final WindowEvent e) {
                        windowClosing(e);
                    }
                });
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }

    /**
     * opens the info dialog.
     */
    public void showDialog() {
        initDialog();
        dialog.setVisible(true);
    }

    /**
     * DOCUMENT ME!
     */
    public void dispose() {
        if (featureInfoPanel != null) {
            featureInfoPanel.dispose();
        }
    }
}
