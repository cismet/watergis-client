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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.cismap.custom.attributerule.ConfirmDialog;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AnnexAction extends ReleaseAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(AnnexAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AnnexAction object.
     */
    public AnnexAction() {
        this(false);
    }

    /**
     * Creates a new CloseAction object.
     *
     * @param  loadPermissions  DOCUMENT ME!
     */
    public AnnexAction(final boolean loadPermissions) {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                AnnexAction.class,
                "AnnexAction.AnnexAction().toolTipText",
                new Object[] { " " });
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-addfriend.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final CidsLayerFeature[] features = getRelevantFeatures(SelectionManager.getInstance().getSelectedFeatures(),
                false);
        featureCount = 0;

        if ((AppBroker.getInstance().getOwnWwGrList() == null) || AppBroker.getInstance().getOwnWwGrList().isEmpty()) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    AnnexAction.class,
                    "AnnexAction.actionPerformed.noGroup.message",
                    AppBroker.getInstance().getOwner()),
                NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.noGroup.title"),
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((features != null) && (features.length > 0)) {
            final CidsBean newWwGr = AppBroker.getInstance().getOwnWwGr();
            final ConfirmDialog dialog = new ConfirmDialog(AppBroker.getInstance().getWatergisApp(),
                    true,
                    NbBundle.getMessage(ReleaseAction.class, "AnnexAction.done().title"),
                    NbBundle.getMessage(ReleaseAction.class, "AnnexAction.done().message", features.length),
                    NbBundle.getMessage(ReleaseAction.class, "AnnexAction.done().execute"),
                    NbBundle.getMessage(ReleaseAction.class, "AnnexAction.done().cancel"));
            dialog.setSize(350, 120);
            StaticSwingTools.showDialog(dialog);

            if (dialog.getButtonClicked() != 1) {
                return;
            }

            final WaitingDialogThread<TreeSet<AbstractFeatureService>> wdt =
                new WaitingDialogThread<TreeSet<AbstractFeatureService>>(AppBroker.getInstance().getWatergisApp(),
                    true,
                    NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.WaitingDialogThread.message"),
                    null,
                    100) {

                    @Override
                    protected TreeSet<AbstractFeatureService> doInBackground() throws Exception {
                        final TreeSet<AbstractFeatureService> services = new TreeSet<AbstractFeatureService>(
                                new AbstractFeatureServiceComparator());
                        final boolean annexAll = true;

                        for (final CidsLayerFeature cidsFeature : features) {
                            final CidsBean cidsBean = cidsFeature.getBean();

                            try {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("annex object with id " + cidsFeature.getId());
                                }
                                if ((cidsFeature.getLayerProperties() != null)
                                            && (cidsFeature.getLayerProperties().getFeatureService() != null)) {
                                    services.add(cidsFeature.getLayerProperties().getFeatureService());
                                }
                                cidsBean.setProperty("ww_gr", newWwGr);
                                cidsFeature.setProperty("ww_gr", newWwGr.getProperty("ww_gr"));

                                if ((cidsBean.getProperty("ba_cd") == null)
                                            || !((String)cidsBean.getProperty("ba_cd")).startsWith(
                                                (String)AppBroker.getInstance().getOwnWwGr().getProperty(
                                                    "praefix"))) {
                                    final String baCd = AppBroker.getInstance().getOwnWwGr().getProperty("praefix")
                                                + ":"
                                                + cidsBean.hashCode();
                                    cidsFeature.setProperty("ba_cd", baCd);
                                    cidsBean.setProperty("ba_cd", baCd);
                                }
                                cidsFeature.saveChangesWithoutReload();
                                cidsBean.persist();
                                ++featureCount;
                            } catch (Exception ex) {
                                LOG.error("Cannot annex feature", ex);
                            }
                        }

                        if (!annexAll) {
                            EventQueue.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        JOptionPane.showMessageDialog(
                                            AppBroker.getInstance().getWatergisApp(),
                                            NbBundle.getMessage(
                                                AnnexAction.class,
                                                "AnnexAction.actionPerformed.all.message"),
                                            NbBundle.getMessage(
                                                AnnexAction.class,
                                                "AnnexAction.actionPerformed.all.title"),
                                            JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                        }
                        return services;
                    }

                    @Override
                    protected void done() {
                        try {
                            final TreeSet<AbstractFeatureService> services = get();
                            refreshServiceAttributeTables(services);
                            AppBroker.getInstance().getWatergisApp().initRouteCombo();
                            refreshServiceLayer(services);
                        } catch (Exception e) {
                            LOG.error("Error while annexing objects.", e);
                        }
                    }
                };

            wdt.start();
        } else {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.noFeature.message"),
                NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.noFeature.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
