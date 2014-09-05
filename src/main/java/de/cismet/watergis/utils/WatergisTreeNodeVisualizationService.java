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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.cids.utils.interfaces.DefaultMetaTreeNodeVisualizationService;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.BoundingBox;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.GetFeatureInfoMultiGeomListener;
import de.cismet.cismap.commons.interaction.events.GetFeatureInfoEvent;

import de.cismet.cismap.navigatorplugin.CidsFeature;

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
public class WatergisTreeNodeVisualizationService implements DefaultMetaTreeNodeVisualizationService {

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
            AppBroker.getInstance().getMappingComponent().addFeaturesToMap(new Feature[] { feature });
            AppBroker.getInstance().getMappingComponent().zoomToAFeatureCollection(featureList, false, false);
            AppBroker.getInstance().getInfoWindowAction().showDialog();
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
        final GetFeatureInfoMultiGeomListener l = (GetFeatureInfoMultiGeomListener)AppBroker
                    .getInstance().getMappingComponent().getInputListener(MappingComponent.FEATURE_INFO_MULTI_GEOM);
        final GetFeatureInfoEvent event = new GetFeatureInfoEvent(this, feature.getGeometry());
        final CidsLayer layer = new CidsLayer(oNode.getMetaClass());
        final List<DefaultFeatureServiceFeature> features = layer.retrieveFeatures(new XBoundingBox(
                    feature.getGeometry()),
                0,
                0,
                null);
        DefaultFeatureServiceFeature cidsFeature = null;
        for (final DefaultFeatureServiceFeature f : features) {
            if (f.getId() == feature.getMetaObject().getId()) {
                cidsFeature = f;
                break;
            }
        }
        final List<Feature> featureList = new ArrayList<Feature>();
        featureList.add(cidsFeature);
        event.setFeatures(featureList);
        l.fireGetFeatureInfoEvent(event);
    }

    @Override
    public void addVisualization(final Collection<DefaultMetaTreeNode> c) throws Exception {
        for (final DefaultMetaTreeNode tmp : c) {
            addVisualization(tmp);
        }
    }
}
