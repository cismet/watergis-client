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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.features.ModifiableFeature;
import de.cismet.cismap.commons.features.PermissionProvider;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableRuleSet;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.WithoutGeometryCreator;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.retrieval.RetrievalEvent;
import de.cismet.cismap.commons.retrieval.RetrievalListener;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class NewObjectAction extends AbstractAction {

    //~ Instance fields --------------------------------------------------------

    private AbstractFeatureService service;
    private boolean firstCall = true;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CloseAction object.
     */
    public NewObjectAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(
                NewObjectAction.class,
                "NewObjectAction.cmdNewObject.toolTipText",
                new Object[] { " ", "" });
        putValue(SHORT_DESCRIPTION, tooltip);
//        final String text = org.openide.util.NbBundle.getMessage(NewObjectAction.class, "CloseAction.text");
//        putValue(NAME, text);
//        final String mnemonic = org.openide.util.NbBundle.getMessage(NewObjectAction.class, "CloseAction.mnemonic");
//        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-plus-sign.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        if ((service != null) && !(!firstCall && e.getSource().equals(AppBroker.getInstance()))) {
            final AttributeTableRuleSet ruleSet = service.getLayerProperties().getAttributeTableRuleSet();
            final FeatureCreator creator = ruleSet.getFeatureCreator();
            if (!creator.isCreationAllowed(CismapBroker.getInstance().getMappingComponent())) {
                return;
            }
            final FeatureServiceFeature feature = service.getFeatureFactory().createNewFeature();
            final FeatureCreator activeCreator = AppBroker.getInstance().getActiveFeatureCreator();

            if ((activeCreator != null) && activeCreator.getService().equals(service)) {
                activeCreator.resume();
            } else {
                ruleSet.beforeSave(feature);
                final CustomCreatedListener listener = new CustomCreatedListener(service, feature);
                creator.addFeatureCreatedListener(listener);
                creator.createFeature(CismapBroker.getInstance().getMappingComponent(), feature);
                AppBroker.getInstance().setActiveFeatureCreator(creator);
            }
        }

        firstCall = false;
    }

    @Override
    public boolean isEnabled() {
        return service != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public void setSelectedService(final AbstractFeatureService service) {
        this.service = service;

        if (service == null) {
            final String tooltip = org.openide.util.NbBundle.getMessage(
                    NewObjectAction.class,
                    "NewObjectAction.cmdNewObject.toolTipText",
                    new Object[] { " ", "" });
            putValue(SHORT_DESCRIPTION, tooltip);
        } else {
            final String type = "";
//            final String type = "("
//                        + service.getLayerProperties().getAttributeTableRuleSet().getFeatureCreator().getTypeName()
//                        + ")";

            final String tooltip = org.openide.util.NbBundle.getMessage(
                    NewObjectAction.class,
                    "NewObjectAction.cmdNewObject.toolTipText",
                    new Object[] { ": " + service.getName() + " ", type });
            putValue(SHORT_DESCRIPTION, tooltip);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class CustomCreatedListener implements FeatureCreatedListener {

        //~ Static fields/initializers -----------------------------------------

        private static final Logger LOG = Logger.getLogger(CustomCreatedListener.class);

        //~ Instance fields ----------------------------------------------------

        private List<FeatureServiceFeature> createdFeatures = new ArrayList<FeatureServiceFeature>();
        private final AbstractFeatureService service;
        private final FeatureServiceFeature feature;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CustomCreatedListener object.
         *
         * @param  service  DOCUMENT ME!
         * @param  feature  DOCUMENT ME!
         */
        public CustomCreatedListener(final AbstractFeatureService service, final FeatureServiceFeature feature) {
            this.service = service;
            this.feature = feature;
            this.createdFeatures.add(feature);
        }

        /**
         * Creates a new CustomCreatedListener object.
         *
         * @param  service          DOCUMENT ME!
         * @param  feature          DOCUMENT ME!
         * @param  createdFeatures  DOCUMENT ME!
         */
        public CustomCreatedListener(final AbstractFeatureService service,
                final FeatureServiceFeature feature,
                final List<FeatureServiceFeature> createdFeatures) {
            this.service = service;
            this.feature = feature;
            this.createdFeatures.addAll(createdFeatures);
            this.createdFeatures.add(feature);
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void featureCreated(final FeatureCreatedEvent event) {
            // reload layer
            final LayerProperties props = feature.getLayerProperties();
            if (feature.getProperty("id") != null) {
                feature.setId(((Number)feature.getProperty("id")).intValue());

                if (feature instanceof PermissionProvider) {
                    final PermissionProvider permissionprovider = (PermissionProvider)feature;
                    if (!permissionprovider.hasWritePermissions()) {
                        if (feature instanceof ModifiableFeature) {
                            try {
                                JOptionPane.showMessageDialog(
                                    AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(
                                        NewObjectAction.class,
                                        "NewObjectAction.actionPerformed().featureCreated().message"),
                                    NbBundle.getMessage(
                                        NewObjectAction.class,
                                        "NewObjectAction.actionPerformed().featureCreated().title"),
                                    JOptionPane.ERROR_MESSAGE);
                                ((ModifiableFeature)feature).delete();
                            } catch (Exception e) {
                                LOG.error("Cannot delete feature", e);
                            }
                        }
                        return;
                    }
                }
            }

            if (props != null) {
                final AbstractFeatureService service = props.getFeatureService();

                if (service != null) {
                    service.retrieve(true);
                    service.addRetrievalListener(new RetrievalListener() {

                            @Override
                            public void retrievalStarted(final RetrievalEvent e) {
                            }

                            @Override
                            public void retrievalProgress(final RetrievalEvent e) {
                            }

                            @Override
                            public void retrievalComplete(final RetrievalEvent e) {
                                AppBroker.getInstance().getWatergisApp().addFeatureToAttributeTable(feature);
                                SelectionManager.getInstance().addSelectedFeatures(createdFeatures);
                                service.removeRetrievalListener(this);
                            }

                            @Override
                            public void retrievalAborted(final RetrievalEvent e) {
                            }

                            @Override
                            public void retrievalError(final RetrievalEvent e) {
                            }
                        });

                    final Timer t = new Timer("reload");
                    t.schedule(new TimerTask() {

                            @Override
                            public void run() {
                                final TreeMap<Integer, MapService> services = AppBroker.getInstance()
                                            .getMappingComponent()
                                            .getMappingModel()
                                            .getRasterServices();

                                for (final MapService mapService : services.values()) {
                                    if (!mapService.equals(service) && mapService.isVisible()) {
                                        mapService.retrieve(true);
                                    }
                                }
                            }
                        }, 1000);
                }
            }

            // Preparations for new creation
            final AttributeTableRuleSet ruleSet = service.getLayerProperties().getAttributeTableRuleSet();
            final FeatureCreator creator = ruleSet.getFeatureCreator();
            if (creator instanceof WithoutGeometryCreator) {
                CismapBroker.getInstance().getMappingComponent().setInteractionMode(MappingComponent.SELECT);
                AppBroker.getInstance().setActiveFeatureCreator(null);
                return;
            }
            final FeatureServiceFeature feature = service.getFeatureFactory().createNewFeature();
            ruleSet.beforeSave(feature);
            final CustomCreatedListener listener = new CustomCreatedListener(service, feature, createdFeatures);
            creator.addFeatureCreatedListener(listener);
            creator.createFeature(CismapBroker.getInstance().getMappingComponent(), feature);
            AppBroker.getInstance().setActiveFeatureCreator(creator);
        }
    }
}
