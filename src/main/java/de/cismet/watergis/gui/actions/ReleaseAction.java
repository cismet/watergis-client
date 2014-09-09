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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
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
public class ReleaseAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(ReleaseAction.class);
    private static final String FG_BA_CLASS_NAME = "fg_bak";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public ReleaseAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                ReleaseAction.class,
                "ReleaseAction.ReleaseAction().toolTipText",
                new Object[] { " " });
        putValue(SHORT_DESCRIPTION, tooltip);
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-removefriend.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        final MappingComponent mc = CismapBroker.getInstance().getMappingComponent();
        final SelectionListener sl = (SelectionListener)mc.getInputEventListener().get(MappingComponent.SELECT);
        final CidsLayerFeature[] features = getRelevantFeatures(sl.getAllSelectedPFeatures());

        if ((features != null) && (features.length > 0)) {
            final WaitingDialogThread<TreeSet<AbstractFeatureService>> wdt =
                new WaitingDialogThread<TreeSet<AbstractFeatureService>>(AppBroker.getInstance().getWatergisApp(),
                    true,
                    NbBundle.getMessage(
                        ReleaseAction.class,
                        "ReleaseAction.actionPerformed.WaitingDialogThread.message"),
                    null,
                    100) {

                    @Override
                    protected TreeSet<AbstractFeatureService> doInBackground() throws Exception {
                        final TreeSet<AbstractFeatureService> services = new TreeSet<AbstractFeatureService>(
                                new AbstractFeatureServiceComparator());

                        for (final CidsLayerFeature cidsFeature : features) {
                            try {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("release object with id " + cidsFeature.getId());
                                }
                                if ((cidsFeature.getLayerProperties() != null)
                                            && (cidsFeature.getLayerProperties().getFeatureService() != null)) {
                                    services.add(cidsFeature.getLayerProperties().getFeatureService());
                                }
                                final CidsBean cidsBean = cidsFeature.getBean();
                                cidsBean.setProperty("ww_gr", null);
                                cidsBean.persist();
                            } catch (Exception ex) {
                                LOG.error("Cannot release feature", ex);
                            }
                        }

                        return services;
                    }

                    @Override
                    protected void done() {
                        try {
                            refreshServiceAttributeTables(get());
                        } catch (Exception e) {
                            LOG.error("Error while releasing objects", e);
                        }
                    }
                };

            wdt.start();
        } else {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(ReleaseAction.class, "ReleaseAction.actionPerformed.noFeature.message"),
                NbBundle.getMessage(ReleaseAction.class, "ReleaseAction.actionPerformed.noFeature.title"),
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  services  DOCUMENT ME!
     */
    protected void refreshServiceAttributeTables(final TreeSet<AbstractFeatureService> services) {
        for (final AbstractFeatureService f : services) {
            AppBroker.getInstance().getWatergisApp().refreshAttributeTable(f);
        }
    }

    /**
     * Determines all selected features, that can be released.
     *
     * @param   features  all selected features
     *
     * @return  an array with all selected features, that can be released
     */
    protected CidsLayerFeature[] getRelevantFeatures(final List<PFeature> features) {
        final List<CidsLayerFeature> featureList = new ArrayList<CidsLayerFeature>();

        for (final PFeature pf : features) {
            final Feature f = pf.getFeature();

            if (f instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)f;
                if (cidsFeature.getBean().getMetaObject().getMetaClass().getName().toLowerCase().equals(
                                FG_BA_CLASS_NAME)) {
                    featureList.add(cidsFeature);
                }
            }
        }

        return featureList.toArray(new CidsLayerFeature[featureList.size()]);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * A Simple Comparator implementation for AbstractFeatureServices.
     *
     * @version  $Revision$, $Date$
     */
    protected class AbstractFeatureServiceComparator implements Comparator<AbstractFeatureService> {

        //~ Methods ------------------------------------------------------------

        @Override
        public int compare(final AbstractFeatureService o1, final AbstractFeatureService o2) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
        }
    }
}
