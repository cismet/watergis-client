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
package de.cismet.watergis.gui.actions.checks;

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import org.apache.log4j.Logger;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.RetrievalServiceLayer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCollection;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.broker.ComponentName;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCheckAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(AbstractCheckAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractGeoprocessingAction object.
     */
    public AbstractCheckAction() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract boolean startCheck(boolean isExport);

    @Override
    public void actionPerformed(final ActionEvent e) {
        startCheck(false);
    }

    /**
     * Uses the given query to execute an analyse.
     *
     * @param   query      the query of the analyse
     * @param   tableName  the name of te table with the analyse result
     *
     * @return  the H2FeatureService as result of the analyse or null, if the analyse has no result
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected H2FeatureService analyseByQuery(final String query, final String tableName) throws Exception {
        final User user = SessionManager.getSession().getUser();
        final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(user, query);

        return createLocalTopicByMetaObjects(mos, tableName);
    }

    /**
     * Uses the given query to execute an analyse.
     *
     * @param   search                    the custom search of the analyse
     * @param   tableName                 the name of te table with the analyse result
     * @param   featureServiceAttributes  DOCUMENT ME!
     *
     * @return  the H2FeatureService as result of the analyse or null, if the analyse has no result
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected H2FeatureService analyseByCustomSearch(final CidsServerSearch search,
            final String tableName,
            final List<FeatureServiceAttribute> featureServiceAttributes) throws Exception {
        final User user = SessionManager.getSession().getUser();
        final ArrayList<ArrayList> attributes = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(user, search);

        return createLocalTopicByAttributeList(attributes, tableName, featureServiceAttributes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mos        DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected H2FeatureService createLocalTopicByMetaObjects(final MetaObject[] mos, final String tableName)
            throws Exception {
        if (mos.length > 0) {
            final List<FeatureServiceFeature> featureList = new ArrayList<FeatureServiceFeature>();
            final MetaClass mc = mos[0].getMetaClass();
            final CidsLayer cl = new CidsLayer(mc);
            cl.initAndWait();
            final CidsLayerInfo layerInfo = CidsLayerUtil.getCidsLayerInfo(mc, SessionManager.getSession().getUser());
//            final Map<String, FeatureServiceAttribute> featureServiceAttributes = cl.getFeatureServiceAttributes();

            for (final MetaObject mo : mos) {
                final HashMap<String, Object> properties = new HashMap<String, Object>(
                        layerInfo.getColumnNames().length);
                final String[] colNames = layerInfo.getColumnNames();
                final CidsBean bean = mo.getBean();

                for (int i = 0; i < colNames.length; ++i) {
                    final String key = layerInfo.getColumnNames()[i];
                    final Object value = bean.getProperty(layerInfo.getColumnPropertyNames()[i]);
                    properties.put(key, value);
                }
                final DefaultFeatureServiceFeature feature = new DefaultFeatureServiceFeature();
                feature.setLayerProperties(cl.getLayerProperties());
                feature.setProperties(properties);
                featureList.add(feature);
            }

            H2FeatureService.removeTableIfExists(tableName + "_1");
            final H2FeatureService internalService = new H2FeatureService(
                    tableName,
                    H2FeatureServiceFactory.DB_NAME,
                    tableName
                            + "_1",
                    null,
                    null,
                    featureList);
            if (LOG.isDebugEnabled()) {
                LOG.debug("create the new data source");
            }
            internalService.initAndWait();

            return internalService;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributes                DOCUMENT ME!
     * @param   tableName                 DOCUMENT ME!
     * @param   featureServiceAttributes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected H2FeatureService createLocalTopicByAttributeList(final ArrayList<ArrayList> attributes,
            final String tableName,
            final List<FeatureServiceAttribute> featureServiceAttributes) throws Exception {
        if (attributes.size() > 0) {
            final List<FeatureServiceFeature> featureList = new ArrayList<FeatureServiceFeature>();
            final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                    CrsTransformer.extractSridFromCrs(CismapBroker.getInstance().getDefaultCrs()));
            final WKBReader wkbReader = new WKBReader(geomFactory);
            int id = 0;
            final LayerProperties layerProperties = new DefaultLayerProperties();
            boolean hasIdField = false;
            // add a dummy service, that contains the feature service attributes
            layerProperties.setFeatureService(new H2FeatureService("dummy", "dummy", null, featureServiceAttributes));

            for (int i = 0; i < attributes.size(); i++) {
                Geometry g = null;
                final HashMap<String, Object> properties = new HashMap<String, Object>(featureServiceAttributes.size());

                for (int j = attributes.get(i).size() - 1; j >= 0; j--) {
                    if (featureServiceAttributes.get(j).getName().equalsIgnoreCase("id")) {
                        hasIdField = true;
                    }
                    if (attributes.get(i).get(j) instanceof byte[]) {
                        try {
                            g = wkbReader.read((byte[])attributes.get(i).get(j));
                            properties.put(featureServiceAttributes.get(j).getName(), g);
                        } catch (final Exception ex) {
                            properties.put(featureServiceAttributes.get(j).getName(), attributes.get(i).get(j));
                        }
                    } else {
                        properties.put(featureServiceAttributes.get(j).getName(), attributes.get(i).get(j));
                    }
                }

                if (!hasIdField) {
                    properties.put("id", ++id);
                }

                final DefaultFeatureServiceFeature lastFeature = new DefaultFeatureServiceFeature(
                        id,
                        g,
                        layerProperties);
                lastFeature.setProperties(properties);
                featureList.add(lastFeature);
            }

            H2FeatureService.removeTableIfExists(tableName + "_1");
            final H2FeatureService internalService = new H2FeatureService(
                    removeFolderNameFromTableName(tableName),
                    H2FeatureServiceFactory.DB_NAME,
                    tableName
                            + "_1",
                    featureServiceAttributes,
                    featureList);
            if (LOG.isDebugEnabled()) {
                LOG.debug("create the new data source");
            }
            internalService.initAndWait();

            return internalService;
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String removeFolderNameFromTableName(final String tableName) {
        if (tableName.contains("->")) {
            return tableName.substring(tableName.lastIndexOf("->") + 2);
        } else {
            return tableName;
        }
    }

    /**
     * Shows the service in the ThemeLayerWidget and refreshs the Capabilities tab of the internal db.
     *
     * @param  service  DOCUMENT ME!
     * @param  folder   DOCUMENT ME!
     */
    protected void showService(final H2FeatureService service, final String folder) {
        final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        LayerCollection layerCollection = null;

        if (folder != null) {
            final StringTokenizer st = new StringTokenizer(folder, "->");

            while (st.hasMoreTokens()) {
                final String subFolder = st.nextToken();

                if (layerCollection != null) {
                    boolean found = false;

                    for (int i = 0; i < layerCollection.size(); ++i) {
                        final Object tmp = layerCollection.get(i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        final LayerCollection newLayerCollection = new LayerCollection();
                        newLayerCollection.setName(subFolder);
                        layerCollection.add(newLayerCollection);
                        layerCollection = newLayerCollection;
                    }
                } else {
                    boolean found = false;

                    for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
                        final Object tmp = model.getChild(model.getRoot(), i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        layerCollection = new LayerCollection();
                        layerCollection.setName(subFolder);
                        model.addEmptyLayerCollection(layerCollection);
                    }
                }
            }
        }

        if (layerCollection != null) {
            model.registerRetrievalServiceLayer(service);
            layerCollection.add(service);
        } else {
            model.addLayer(service);
        }
        final Component capComponent = AppBroker.getInstance().getComponent(ComponentName.CAPABILITIES);

        if (capComponent instanceof CapabilityWidget) {
            final CapabilityWidget cap = (CapabilityWidget)capComponent;
            cap.refreshJdbcTrees();
        }
    }
}
