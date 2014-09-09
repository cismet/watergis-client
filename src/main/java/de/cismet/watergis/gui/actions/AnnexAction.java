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

import Sirius.navigator.connection.SessionManager;
import Sirius.navigator.tools.CacheException;
import Sirius.navigator.tools.MetaObjectCache;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

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
    private static List<CidsBean> WW_GR;
    private static boolean initialized = false;

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

        if (loadPermissions && (WW_GR == null)) {
            WW_GR = new ArrayList<CidsBean>();
            final MetaClass katWwGrMc = ClassCacheMultiple.getMetaClass(AppBroker.DOMAIN_NAME, "dlm25w.kat_ww_gr");

            final String queryTemplate = "SELECT %s, %s FROM %s where owner = '%s';";
            final String routeQuery = String.format(
                    queryTemplate,
                    katWwGrMc.getID(),
                    katWwGrMc.getPrimaryKey(),
                    katWwGrMc.getTableName(),
                    SessionManager.getSession().getUser().getUserGroup().getName());

            try {
                final MetaObject[] mos = MetaObjectCache.getInstance().getMetaObjectsByQuery(routeQuery, false);

                if ((mos != null) && (mos.length > 0)) {
                    for (final MetaObject mo : mos) {
                        WW_GR.add(mo.getBean());
                    }
                }

                initialized = true;
            } catch (CacheException e) {
                LOG.error("Cannot retrieve the permissions list", e);
            }
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final CidsLayerFeature[] features = getRelevantFeatures(sl.getAllSelectedPFeatures());

        while (!initialized) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // nothing to do
            }
        }

        if ((WW_GR == null) || WW_GR.isEmpty()) {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    AnnexAction.class,
                    "AnnexAction.actionPerformed.noGroup.message",
                    SessionManager.getSession().getUser().getUserGroup().getName()),
                NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.noGroup.title"),
                JOptionPane.ERROR_MESSAGE);

            return;
        }

        if ((features != null) && (features.length > 0)) {
            final Object newWwGr;

            if (WW_GR.size() > 1) {
                final Object answer = JOptionPane.showInputDialog(AppBroker.getInstance().getWatergisApp(),
                        NbBundle.getMessage(AnnexAction.class, "AnnexAction.actionPerformed.possibleOwner.message"),
                        "AnnexAction.actionPerformed.possibleOwner.title",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        WW_GR.toArray(),
                        WW_GR.get(0).toString());
                newWwGr = answer;
            } else {
                newWwGr = WW_GR.get(0);
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
                        boolean annexAll = true;

                        for (final CidsLayerFeature cidsFeature : features) {
                            final CidsBean cidsBean = cidsFeature.getBean();
                            final CidsBean wwGrBean = (CidsBean)cidsBean.getProperty("ww_gr");

                            if (wwGrBean == null) {
                                try {
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("annex object with id " + cidsFeature.getId());
                                    }
                                    if ((cidsFeature.getLayerProperties() != null)
                                                && (cidsFeature.getLayerProperties().getFeatureService() != null)) {
                                        services.add(cidsFeature.getLayerProperties().getFeatureService());
                                    }
                                    cidsBean.setProperty("ww_gr", newWwGr);
                                    cidsBean.persist();
                                } catch (Exception ex) {
                                    LOG.error("Cannot annex feature", ex);
                                }
                            } else {
                                annexAll = false;
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
                            refreshServiceAttributeTables(get());
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
