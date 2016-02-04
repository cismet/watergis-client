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
package de.cismet.watergis.gui.actions.foto;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

import de.cismet.watergis.gui.panels.Photo;

import de.cismet.watergis.utils.FeatureServiceHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DeleteAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DeleteAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public DeleteAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(DeleteAction.class,
                "DeleteAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(DeleteAction.class,
                "DeleteAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(DeleteAction.class,
                "DeleteAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-circledelete.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final List<FeatureServiceFeature> features = FeatureServiceHelper.getSelectedCidsLayerFeatures(
                AppBroker.FOTO_MC_NAME);

        final int ans = JOptionPane.showConfirmDialog(
                AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    DeleteAction.class,
                    "DeleteAction.actionPerformed().text",
                    features.size()),
                NbBundle.getMessage(DeleteAction.class, "DeleteAction.actionPerformed().title"),
                JOptionPane.YES_NO_OPTION);

        if (ans == JOptionPane.YES_OPTION) {
            if (!features.isEmpty()) {
                final WaitingDialogThread<Boolean> wdt = new WaitingDialogThread<Boolean>(
                        StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                        true,
                        // NbBundle.getMessage(SonstigeCheckAction.class,
                        // "SonstigeCheckAction.actionPerformed().dialog"),
                        NbBundle.getMessage(DeleteAction.class, "DeleteAction.actionPerformed.waitingDialog"),
                        null,
                        100,
                        true) {

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            wd.setMax(features.size());
                            int i = 0;
                            wd.setText(NbBundle.getMessage(
                                    DeleteAction.class,
                                    "DeleteAction.actionPerformed.progress",
                                    i,
                                    features.size()));

                            for (final FeatureServiceFeature feature : features) {
                                if (Thread.interrupted()) {
                                    return false;
                                }
                                Photo.deletePhoto((CidsLayerFeature)feature);
                                wd.setText(NbBundle.getMessage(
                                        DeleteAction.class,
                                        "DeleteAction.actionPerformed.progress",
                                        ++i,
                                        features.size()));
                            }
                            return true;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                SelectionManager.getInstance().removeSelectedFeatures(features);
                                final List<AbstractFeatureService> services = FeatureServiceHelper
                                            .getCidsLayerServicesFromTree(
                                                "foto");

                                for (final AbstractFeatureService featureService : services) {
                                    featureService.retrieve(true);
                                }
                            } catch (Exception e) {
                                LOG.error("Error while deleting photos.", e);
                            }
                        }
                    };

                wdt.start();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
