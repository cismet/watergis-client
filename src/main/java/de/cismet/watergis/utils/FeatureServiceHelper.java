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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import de.cismet.cismap.cidslayer.CidsFeatureFactory;
import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.ZoomToLayerWorker;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.SelectionListener;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.tools.FeatureTools;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class FeatureServiceHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(FeatureServiceHelper.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Merges the given features to a new one.
     *
     * @param   primaryFeature              the first feature to merge
     * @param   secondaryFeature            the second feature to merge
     * @param   newLayerProperties          The layer properties for the resulted features
     * @param   secondaryFeatureProperties  only the properties of this list will be used from the secondary feature
     * @param   distanceField               The name of the distance property. If this parameter is an empty string, the
     *                                      distance will not be set
     *
     * @return  a new feature that contains the attributes of the two given features
     */
    public static FeatureServiceFeature mergeFeatures(final FeatureServiceFeature primaryFeature,
            final FeatureServiceFeature secondaryFeature,
            final LayerProperties newLayerProperties,
            final List<String> secondaryFeatureProperties,
            final String distanceField) {
        final DefaultFeatureServiceFeature feature = new DefaultFeatureServiceFeature(primaryFeature);
        final Map<String, FeatureServiceAttribute> newFeatureServiceAttributes = newLayerProperties.getFeatureService()
                    .getFeatureServiceAttributes();
        feature.setLayerProperties(newLayerProperties);

        if (secondaryFeature != null) {
            for (final String key : secondaryFeatureProperties) {
                final FeatureServiceAttribute attr = newFeatureServiceAttributes.get(key);
                feature.addProperty(attr.getName(), secondaryFeature.getProperty(attr.getAlias()));
            }

            if (!distanceField.equals("")) {
                final double distance = primaryFeature.getGeometry().distance(secondaryFeature.getGeometry());
                feature.addProperty(distanceField, distance);
            }
        }

        return feature;
    }

    /**
     * Creates a STR tree of with the given features.
     *
     * @param   features  the features, which should be added to the ree
     *
     * @return  a STR tree of with the given features.
     */
    public static STRtree getFeatureTree(final List<FeatureServiceFeature> features) {
        final STRtree tree = new STRtree(((features.size() > 3) ? features.size() : 3));

        for (final FeatureServiceFeature feature : features) {
            if (feature.getGeometry() != null) {
                tree.insert(feature.getGeometry().getEnvelopeInternal(), feature);
            }
        }
        tree.build();
        return tree;
    }

    /**
     * Creates a FeatureServiceAttribute map with the attribute, that should be used, if features with the given
     * attribute maps should be merged.
     *
     * @param   serviceAttr           the service attributes of the primary service
     * @param   targetServiceAttr     the service attributes of the secondary service
     * @param   targetAttributeOrder  contains the attributes of the targetService in the right order
     * @param   newKeys               all feature attributes of the secondary service will be added to this list
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, FeatureServiceAttribute> getSuitableFeatureServiceAttribute(
            final Map<String, FeatureServiceAttribute> serviceAttr,
            final Map<String, FeatureServiceAttribute> targetServiceAttr,
            final List<String> targetAttributeOrder,
            final List<String> newKeys) {
        final Map<String, FeatureServiceAttribute> newAttrMap = new HashMap<String, FeatureServiceAttribute>(
                serviceAttr);
        final Map<String, FeatureServiceAttribute> remainingTargetMap = new HashMap<String, FeatureServiceAttribute>(
                targetServiceAttr);

        // remove possible id fields
// for (final String possibleIdField : new String[] { "ID", "id", "Id", "iD" }) {
// if (newAttrMap.get(possibleIdField) != null) {
// newAttrMap.remove(possibleIdField);
// }
// }

        for (final String key : targetAttributeOrder) {
            final FeatureServiceAttribute originalFeature = targetServiceAttr.get(key);
            if (originalFeature.isGeometry()) {
                // the geometry field should be ignored
                continue;
            }
            final FeatureServiceAttribute newAttr = new FeatureServiceAttribute(originalFeature.getName(),
                    originalFeature.getType(),
                    originalFeature.isSelected());
            newAttr.setAlias(key);
            remainingTargetMap.remove(key);
            String name = "_" + key.replace('.', '_');
            int attemption = 1;

            if (name.length() > 10) {
                name = name.substring(0, 10);
            }

            while (remainingTargetMap.containsKey(name) || serviceAttr.containsKey(name)
                        || newAttrMap.containsKey(name)) {
                if (name.length() > (10 - (int)Math.ceil(Math.log10(attemption + 1)))) {
                    name = name.substring(0, (10 - (int)Math.ceil(Math.log10(attemption + 1))));
                }
                name += attemption++;
            }

            newAttr.setAlias(newAttr.getName());
            newAttr.setName(name);
            newKeys.add(name);
            newAttrMap.put(name, newAttr);
        }

        return newAttrMap;
    }

    /**
     * Creates a FeatureServiceAttribute map with the attributes, which should be used, if features with the given
     * attribute maps should be merged.
     *
     * @param   serviceAttr        the service attributes of the primary service
     * @param   targetServiceAttr  the service attributes of the secondary service
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, FeatureServiceAttribute> getSuitableMergeFeatureServiceAttribute(
            final Map<String, FeatureServiceAttribute> serviceAttr,
            final Map<String, FeatureServiceAttribute> targetServiceAttr) {
        final Map<String, FeatureServiceAttribute> newAttrMap = new HashMap<String, FeatureServiceAttribute>();

        for (final String key : serviceAttr.keySet()) {
            final FeatureServiceAttribute attr = (FeatureServiceAttribute)serviceAttr.get(key).clone();

            if ((targetServiceAttr.containsKey(key) || targetServiceAttr.containsKey(key))
                        && !attr.isGeometry()) {
                newAttrMap.put(key, attr);
            } else if (attr.isGeometry()) {
                newAttrMap.put(key, attr);
            }
        }

        // remove possible id fields
// for (final String possibleIdField : new String[] { "ID", "id", "Id", "iD" }) {
// if (newAttrMap.get(possibleIdField) != null) {
// newAttrMap.remove(possibleIdField);
// }
// }

        return newAttrMap;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, FeatureServiceAttribute> createGeometryOnlyFeatureServiceAttributes(
            final Map<String, FeatureServiceAttribute> attributes) {
        final Map<String, FeatureServiceAttribute> result = new HashMap<String, FeatureServiceAttribute>();

        for (final String key : attributes.keySet()) {
            final FeatureServiceAttribute attr = attributes.get(key);

            if (attr.isGeometry()) {
                result.put(key, (FeatureServiceAttribute)attr.clone());
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Map<String, FeatureServiceAttribute> cloneFeatureServiceAttributes(
            final Map<String, FeatureServiceAttribute> attributes) {
        final Map<String, FeatureServiceAttribute> result = new HashMap<String, FeatureServiceAttribute>();

        for (final String key : attributes.keySet()) {
            final FeatureServiceAttribute attr = attributes.get(key);

            result.put(key, (FeatureServiceAttribute)attr.clone());
        }

        return result;
    }

    /**
     * determines all field name of the given service with the given type.
     *
     * @param   service  DOCUMENT ME!
     * @param   cl       the allowed type or null for all types
     *
     * @return  DOCUMENT ME!
     */
    public static List<String> getAllFieldNames(final AbstractFeatureService service, final Class<?> cl) {
        Map<String, FeatureServiceAttribute> attributeMap = service.getFeatureServiceAttributes();
        final List<String> resultList = new ArrayList<String>();

        if (attributeMap == null) {
            try {
                service.initAndWait();
            } catch (Exception e) {
                LOG.error("Error while initializing the feature service.", e);
            }
            attributeMap = service.getFeatureServiceAttributes();
        }

        for (final String name : (List<String>)service.getOrderedFeatureServiceAttributes()) {
            final FeatureServiceAttribute attr = attributeMap.get(name);

            if ((attr != null) && ((cl == null) || cl.isAssignableFrom(FeatureTools.getClass(attr)))) {
                resultList.add(name);
            }
        }

        return resultList;
    }

    /**
     * Determines the features of the given service.
     *
     * @param   service       the features of this service will be returned
     * @param   onlySelected  returns only the selected features of the services, iff this parameter is true
     *
     * @return  the features of the given service
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static List<FeatureServiceFeature> getFeatures(final AbstractFeatureService service,
            final boolean onlySelected) throws Exception {
        if (onlySelected) {
            return getSelectedFeatures(service);
        } else {
            if (!service.isInitialized()) {
                service.initAndWait();
            }
            final Geometry g = ZoomToLayerWorker.getServiceBounds(service);
            XBoundingBox bb = null;

            if (LOG.isDebugEnabled()) {
                LOG.debug("retrieve all features from the service");
            }

            if (g != null) {
                bb = new XBoundingBox(g);

                try {
                    final CrsTransformer transformer = new CrsTransformer(CismapBroker.getInstance().getSrs()
                                    .getCode());
                    bb = transformer.transformBoundingBox(bb);
                } catch (Exception e) {
                    LOG.error("Cannot transform CRS.", e);
                }
            }

            if (!service.isInitialized()) {
                service.initAndWait();
            }

            return service.getFeatureFactory().createFeatures(
                    service.getQuery(),
                    bb,
                    null,
                    0,
                    0,
                    null);
        }
    }

    /**
     * Provides all selected features of the given service.
     *
     * @param   featureService  service the service, the selected features should be returned for
     *
     * @return  all selected features of the given service
     */
    public static List<FeatureServiceFeature> getSelectedFeatures(final AbstractFeatureService featureService) {
        final List<FeatureServiceFeature> result = new ArrayList<FeatureServiceFeature>();
        final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures(featureService);

        if (selectedFeatures != null) {
            for (final Feature feature : selectedFeatures) {
                if (feature instanceof FeatureServiceFeature) {
                    result.add((FeatureServiceFeature)feature);
                }
            }
        }

        return result;
    }

    /**
     * Provides all selected features of the given service.
     *
     * @param   metaClass  featureService service the service, the selected features should be returned for
     *
     * @return  all selected features of the given service
     */
    public static List<FeatureServiceFeature> getSelectedCidsLayerFeatures(final String metaClass) {
        final List<FeatureServiceFeature> result = new ArrayList<FeatureServiceFeature>();
        final List<Feature> selectedFeatures = SelectionManager.getInstance().getSelectedFeatures();

        if (selectedFeatures != null) {
            for (final Feature feature : selectedFeatures) {
                if (feature instanceof CidsLayerFeature) {
                    final CidsLayerFeature f = (CidsLayerFeature)feature;
                    final CidsLayer layer = (CidsLayer)f.getLayerProperties().getFeatureService();
                    if (layer.getMetaClass().getName().equalsIgnoreCase(metaClass)) {
                        result.add((FeatureServiceFeature)feature);
                    }
                }
            }
        }

        return result;
    }

    /**
     * creates a list with all services fromm the curent mapping model, that provides the given geometry type.
     *
     * @param   geometryType  the geometry type of the service, or null, if all geometry types are allowed
     *
     * @return  all services fromm the curent mapping model, that provides the given geometry type
     */
    public static List<AbstractFeatureService> getServices(final String[] geometryType) {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        final ActiveLayerModel mappingModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap treeMap = mappingModel.getMapServices();
        final List<Integer> keyList = new ArrayList<Integer>(treeMap.keySet());
        Collections.sort(keyList, Collections.reverseOrder());
        final Iterator it = keyList.iterator();

        while (it.hasNext()) {
            final Object service = treeMap.get(it.next());
            if (service instanceof AbstractFeatureService) {
                final AbstractFeatureService featureService = (AbstractFeatureService)service;

                if (geometryType == null) {
                    serviceList.add(featureService);
                } else {
                    try {
                        if (!featureService.isInitialized()) {
                            ((AbstractFeatureService)service).initAndWait();
                        }

                        if (containsIgnoreCase(geometryType, featureService.getGeometryType())) {
                            serviceList.add(featureService);
                        }
                    } catch (Exception e) {
                        LOG.error("Error while initialising service " + ((AbstractFeatureService)service).getName(), e);
                    }
                }
            }
        }

        return serviceList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<AbstractFeatureService> getCidsLayerServicesFromTree(final String metaClass) {
        final List<AbstractFeatureService> serviceList = new ArrayList<AbstractFeatureService>();
        final ActiveLayerModel mappingModel = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeMap treeMap = mappingModel.getMapServices();
        final List<Integer> keyList = new ArrayList<Integer>(treeMap.keySet());
        Collections.sort(keyList, Collections.reverseOrder());
        final Iterator it = keyList.iterator();

        while (it.hasNext()) {
            final Object service = treeMap.get(it.next());
            if (service instanceof CidsLayer) {
                final CidsLayer featureService = (CidsLayer)service;

                if (metaClass == null) {
                    serviceList.add(featureService);
                } else {
                    if (featureService.getMetaClass().getName().equalsIgnoreCase(metaClass)) {
                        serviceList.add(featureService);
                    }
                }
            }
        }

        return serviceList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  service  DOCUMENT ME!
     */
    public static void addServiceLayerToTheTree(final AbstractFeatureService service) {
        AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(service);
        final Component capComponent = AppBroker.getInstance().getComponent(ComponentName.CAPABILITIES);

        if (capComponent instanceof CapabilityWidget) {
            final CapabilityWidget cap = (CapabilityWidget)capComponent;
            cap.refreshJdbcTrees();
        }
    }

    /**
     * Creates a new service with the given name, that contains the given features.
     *
     * @param   c               the parent is required to justify the probably required message dialog
     * @param   features        the features to add to te service
     * @param   tableName       the name of the new service
     * @param   attributeOrder  the attribute order of the new service, or null, if the order does not care
     *
     * @return  the new service or null, iff the feature list is empty or null
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static H2FeatureService createNewService(final Component c,
            final List<FeatureServiceFeature> features,
            final String tableName,
            final List<String> attributeOrder) throws Exception {
        if ((features == null) || features.isEmpty()) {
            JOptionPane.showMessageDialog(
                c,
                NbBundle.getMessage(
                    FeatureServiceHelper.class,
                    "FeatureServiceHelper.createNewService.noFeatures.message",
                    tableName),
                NbBundle.getMessage(
                    FeatureServiceHelper.class,
                    "FeatureServiceHelper.createNewService.noFeatures.title"),
                JOptionPane.INFORMATION_MESSAGE);

            return null;
        } else {
            final H2FeatureService internalService = new H2FeatureService(
                    tableName,
                    H2FeatureServiceFactory.DB_NAME,
                    tableName,
                    null,
                    null,
                    features,
                    attributeOrder);
            if (LOG.isDebugEnabled()) {
                LOG.debug("create the new data source");
            }
            internalService.initAndWait();

            return internalService;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   array   DOCUMENT ME!
     * @param   string  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static boolean containsIgnoreCase(final String[] array, final String string) {
        for (final String tmp : array) {
            if (tmp.equalsIgnoreCase(string)) {
                return true;
            }
        }

        return false;
    }
}
