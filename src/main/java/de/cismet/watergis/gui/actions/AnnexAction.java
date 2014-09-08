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

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.navigator.utils.ClassCacheMultiple;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class AnnexAction extends AbstractAction {

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
                    "/de/cismet/watergis/res/icons16/icon-mergeshapes.png"));
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
        final List<PFeature> features = sl.getAllSelectedPFeatures();

        for (final PFeature pf : features) {
            final Feature f = pf.getFeature();

            if (f instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)f;

                while (!initialized) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                        // nothing to do
                    }
                }

                if ((WW_GR != null) && !WW_GR.isEmpty()) {
                    Object newWwGr = null;

                    if (WW_GR.size() > 1) {
                        final Object answer = JOptionPane.showInputDialog(AppBroker.getInstance().getWatergisApp(),
                                "Recht wählen",
                                "Title",
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                WW_GR.toArray(),
                                WW_GR.get(0).toString());
                        newWwGr = answer;
                    } else {
                        newWwGr = WW_GR.get(0);
                    }

                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("annex object with id " + cidsFeature.getId());
                        }
                        final CidsBean cidsBean = cidsFeature.getBean();
                        cidsBean.setProperty("ww_gr", newWwGr);
                        cidsBean.persist();
                    } catch (Exception ex) {
                        LOG.error("Cannot release feature", ex);
                    }
                } else {
                    // todo Fehlemeldung anzeigen
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
