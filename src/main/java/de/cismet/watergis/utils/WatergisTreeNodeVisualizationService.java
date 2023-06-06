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
package de.cismet.watergis.utils;

import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
import Sirius.navigator.types.treenode.ObjectTreeNode;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cids.utils.interfaces.DefaultMetaTreeNodeVisualizationService;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanelEvent;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanelListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoMultiGeomListener;
import de.cismet.cismap.commons.interaction.events.GetFeatureInfoEvent;

import de.cismet.cismap.navigatorplugin.CidsFeature;

import de.cismet.tools.gui.WaitingDialogThread;

import de.cismet.watergis.broker.AppBroker;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(
    supersedes = "de.cismet.cismap.navigatorplugin.DefaultMetaTreeNodeVisualizationServiceForNavigator",
    service = DefaultMetaTreeNodeVisualizationService.class
)
public class WatergisTreeNodeVisualizationService implements DefaultMetaTreeNodeVisualizationService,
    FeatureInfoPanelListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(WatergisTreeNodeVisualizationService.class);

    private static List<Feature> visualisedFeatures = new ArrayList<Feature>();

    //~ Methods ----------------------------------------------------------------

    @Override
    public void removeVisualization(final DefaultMetaTreeNode dmtn) throws Exception {
        if (dmtn instanceof ObjectTreeNode) {
            final ObjectTreeNode oNode = (ObjectTreeNode)dmtn;
            final CidsFeature feature = new CidsFeature(oNode.getMetaObject());
            final List<Feature> featureList = new ArrayList<Feature>();
            featureList.add(feature);
            AppBroker.getInstance().getMappingComponent().removeFeatures(featureList);
        }
    }

    @Override
    public void removeVisualization(final Collection<DefaultMetaTreeNode> c) throws Exception {
        for (final DefaultMetaTreeNode tmp : c) {
            removeVisualization(tmp);
        }
    }

    @Override
    public void addVisualization(final DefaultMetaTreeNode defaultMetaTreeNode) throws Exception {
        if (defaultMetaTreeNode instanceof ObjectTreeNode) {
            final ObjectTreeNode oNode = (ObjectTreeNode)defaultMetaTreeNode;
            final CidsFeature feature = new CidsFeature(oNode.getMetaObject());
            final List<Feature> featureList = new ArrayList<Feature>();
            featureList.add(feature);
            visualisedFeatures.add(feature);
            AppBroker.getInstance().getMappingComponent().addFeaturesToMap(new Feature[] { feature });

            final List<Feature> featureToZoom = new ArrayList<Feature>();
            final DefaultFeatureServiceFeature featureZoom = new DefaultFeatureServiceFeature();
            final XBoundingBox box = new XBoundingBox(feature.getGeometry());

            if (AppBroker.getInstance().getProblemFeatureGeometryIncrease() > 0) {
                box.increase(AppBroker.getInstance().getProblemFeatureGeometryIncrease() * 100);
            }
            featureZoom.setGeometry(box.getGeometry());
            featureToZoom.add(featureZoom);

            AppBroker.getInstance().getMappingComponent().zoomToAFeatureCollection(featureToZoom, false, false);
            AppBroker.getInstance().getInfoWindowAction().showDialog();
            AppBroker.getInstance().getInfoWindowAction().addFeatureInfoPanelListener(this);
            AppBroker.getInstance().getInfoWindowAction().showAllFeature();
            addFeatureToFeatureInfoDialog(oNode);
        }
    }

    /**
     * Adds the feature of the given node to the feature info dialog.
     *
     * @param   oNode  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void addFeatureToFeatureInfoDialog(final ObjectTreeNode oNode) throws Exception {
        final CidsFeature feature = new CidsFeature(oNode.getMetaObject());

        final WaitingDialogThread<List<DefaultFeatureServiceFeature>> wdt =
            new WaitingDialogThread<List<DefaultFeatureServiceFeature>>(AppBroker.getInstance().getWatergisApp(),
                true,
                NbBundle.getMessage(
                    WatergisTreeNodeVisualizationService.class,
                    "WatergisTreeNodeVisualizationService.addFeatureToFeatureInfoDialog.text"),
                null,
                100) {

                @Override
                protected List<DefaultFeatureServiceFeature> doInBackground() throws Exception {
                    final CidsLayer layer = new CidsLayer(oNode.getMetaClass());
                    layer.initAndWait();
                    return layer.getFeatureFactory()
                                .createFeatures(layer.getQuery(),
                                    new XBoundingBox(feature.getGeometry()),
                                    null,
                                    0,
                                    0,
                                    null);
                }

                @Override
                protected void done() {
                    try {
                        final GetFeatureInfoMultiGeomListener l = (GetFeatureInfoMultiGeomListener)AppBroker
                                    .getInstance().getMappingComponent()
                                    .getInputListener(MappingComponent.FEATURE_INFO_MULTI_GEOM);
                        final List<Feature> featureList = new ArrayList<Feature>();
                        final GetFeatureInfoEvent event = new GetFeatureInfoEvent(this, feature.getGeometry());
                        final List<DefaultFeatureServiceFeature> features = get();
                        DefaultFeatureServiceFeature cidsFeature = null;

                        for (final DefaultFeatureServiceFeature f : features) {
                            if (f.getId() == feature.getMetaObject().getId()) {
                                cidsFeature = f;
                                break;
                            }
                        }

                        featureList.add(cidsFeature);
                        event.setFeatures(featureList);

                        l.fireGetFeatureInfoEvent(event);
                    } catch (Exception e) {
                        LOG.error("Error while loading feature with id ", e);
                    }
                }
            };

        wdt.start();
    }

    @Override
    public void addVisualization(final Collection<DefaultMetaTreeNode> c) throws Exception {
        for (final DefaultMetaTreeNode tmp : c) {
            addVisualization(tmp);
        }
    }

    @Override
    public void featureSaved(final FeatureInfoPanelEvent evt) {
        AppBroker.getInstance().getMappingComponent().removeFeatures(visualisedFeatures);
        AppBroker.getInstance().getInfoWindowAction().removeFeatureInfoPanelListener(this);
        visualisedFeatures.clear();
    }

    @Override
    public void dispose(final FeatureInfoPanelEvent evt) {
        AppBroker.getInstance().getMappingComponent().removeFeatures(visualisedFeatures);
        AppBroker.getInstance().getInfoWindowAction().removeFeatureInfoPanelListener(this);
        visualisedFeatures.clear();
    }

    /**
     * DOCUMENT ME!
     */
    public static void removeVisualisedFeatures() {
        AppBroker.getInstance().getMappingComponent().removeFeatures(visualisedFeatures);
        visualisedFeatures.clear();
    }
}
