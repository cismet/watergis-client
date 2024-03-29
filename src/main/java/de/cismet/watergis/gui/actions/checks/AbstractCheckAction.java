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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.event.TableModelEvent;
import javax.swing.tree.TreePath;

import de.cismet.cids.custom.watergis.server.search.RouteProblemsCount;
import de.cismet.cids.custom.watergis.server.search.RouteProblemsCountAndClasses;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;
import de.cismet.cids.server.search.CidsServerSearch;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.cidslayer.CidsLayer;
import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.MappingModel;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.DefaultLayerProperties;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.attributetable.AttributeTableFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.gui.layerwidget.LayerCollection;
import de.cismet.cismap.commons.gui.layerwidget.ThemeLayerWidget;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.rasterservice.MapService;
import de.cismet.cismap.commons.util.SelectionManager;

import de.cismet.tools.gui.WaitDialog;

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

    //~ Instance fields --------------------------------------------------------

    protected boolean isBackgroundCheck = false;

    private boolean exportCheck = false;
    private Map<String, CidsLayer> initialisedService = new HashMap<String, CidsLayer>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractGeoprocessingAction object.
     */
    public AbstractCheckAction() {
    }

    /**
     * Creates a new AbstractGeoprocessingAction object.
     *
     * @param  isBackgroundCheck  DOCUMENT ME!
     */
    public AbstractCheckAction(final boolean isBackgroundCheck) {
        this.isBackgroundCheck = isBackgroundCheck;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public synchronized AbstractCheckResult startBackgroundCheck() throws Exception {
        final boolean originState = isBackgroundCheck;

        isBackgroundCheck = true;

        final AbstractCheckResult result = check(false, null);
        cleanup();

        isBackgroundCheck = originState;

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     * @param   wd        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected abstract AbstractCheckResult check(final boolean isExport, final WaitDialog wd) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     * @param   wd        DOCUMENT ME!
     * @param   result    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected abstract boolean startCheckInternal(boolean isExport, WaitDialog wd, List<H2FeatureService> result);

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     * @param   wd        DOCUMENT ME!
     * @param   result    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean startCheck(final boolean isExport, final WaitDialog wd, final List<H2FeatureService> result) {
        this.exportCheck = isExport;
        final boolean checkOk = startCheckInternal(isExport, wd, result);

        cleanup();

        return checkOk;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        startCheck(false, null, null);

        cleanup();
    }

    /**
     * DOCUMENT ME!
     */
    private void cleanup() {
        initialisedService.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract int getProgressSteps();

    /**
     * DOCUMENT ME!
     *
     * @param  wd     DOCUMENT ME!
     * @param  steps  n DOCUMENT ME!
     */
    protected void increaseProgress(final WaitDialog wd, final int steps) {
        if (wd != null) {
            wd.increaseProgress(1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  result   DOCUMENT ME!
     * @param  service  DOCUMENT ME!
     */
    protected void addService(final List<H2FeatureService> result, final H2FeatureService service) {
        if (service != null) {
            result.add(service);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   isExport  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected int[] getSelectedIds(final boolean isExport) {
        String user = AppBroker.getInstance().getOwner();
        int[] selectedIds = null;

        if (isBackgroundCheck) {
            return null;
        }

        if (user.equalsIgnoreCase("Administratoren")) {
            user = null;
        }

        if (isExport) {
            if (user == null) {
                selectedIds = getIdsOfSelectedObjects("fg_ba");
            }
        } else {
            selectedIds = getIdsOfSelectedObjects("fg_ba");
        }

        return selectedIds;
    }

    /**
     * Determines the ids of all selected features of the given class.
     *
     * @param   cidsClassName  the cids class name of the selected features
     *
     * @return  an array with all ids of the selected features of the given class.
     */
    protected int[] getIdsOfSelectedObjects(final String cidsClassName) {
        final ActiveLayerModel model = (ActiveLayerModel)AppBroker.getInstance().getMappingComponent()
                    .getMappingModel();
        final TreeSet<Integer> ids = new TreeSet<Integer>();
        final int[] resultArray;
        int index = 0;

        for (final MapService service : model.getMapServices().values()) {
            if (service instanceof CidsLayer) {
                final CidsLayer cidsLayer = (CidsLayer)service;

                if (cidsLayer.getMetaClass().getName().equals(cidsClassName)) {
                    final List<Feature> selectedFeatureList = SelectionManager.getInstance()
                                .getSelectedFeatures(cidsLayer);

                    if (selectedFeatureList != null) {
                        for (final Feature f : selectedFeatureList) {
                            final CidsLayerFeature feature = (CidsLayerFeature)f;
                            ids.add(feature.getId());
                        }
                    }
                }
            }
        }

        resultArray = new int[ids.size()];

        for (final Integer i : ids) {
            resultArray[index++] = i;
        }

        if (resultArray.length == 0) {
            return null;
        } else {
            return resultArray;
        }
    }

    /**
     * Uses the given query to execute an analyse.
     *
     * @param       query      the query of the analyse
     * @param       tableName  the name of te table with the analyse result
     *
     * @return      the H2FeatureService as result of the analyse or null, if the analyse has no result
     *
     * @throws      Exception  DOCUMENT ME!
     *
     * @deprecated  use analyseByQuery(final MetaClass mc, final String query, final String tableName)
     */
    protected H2FeatureService analyseByQuery(final String query, final String tableName) throws Exception {
        final User user = SessionManager.getSession().getUser();
        final MetaObject[] mos = SessionManager.getProxy().getMetaObjectByQuery(user, query);

        return createLocalTopicByMetaObjects(mos, tableName);
    }

    /**
     * Uses the given query to execute an analyse.
     *
     * @param   mc         The meta class of the resulting objects
     * @param   query      the query of the analyse
     * @param   tableName  the name of te table with the analyse result
     *
     * @return  the H2FeatureService as result of the analyse or null, if the analyse has no result
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected H2FeatureService analyseByQuery(final MetaClass mc, final String query, final String tableName)
            throws Exception {
        // init layer
        CidsLayer cl = initialisedService.get(mc.getTableName());

        if (cl == null) {
            cl = new CidsLayer(mc);
            cl.initAndWait();
            initialisedService.put(mc.getTableName(), cl);
        }

        FeatureServiceAttribute idAttr = cl.getFeatureServiceAttributes().get("id");

        if (idAttr == null) {
            idAttr = cl.getFeatureServiceAttributes().get("ID");
        }

        if (idAttr == null) {
            LOG.error("Cannot perform check. ID attribute not found");
            return null;
        }

        // retrieve features
        String clQuery = cl.decoratePropertyName(idAttr.getName()) + " in (select id from (";

        if (query.endsWith(";")) {
            clQuery += query.substring(0, query.length() - 1) + ") as a )";
        } else {
            clQuery += query + ") as a )";
        }

        final List<FeatureServiceFeature> featureList = cl.getFeatureFactory()
                    .createFeatures(clQuery, null, null, 0, 0, null);

        // create db service
        final String layerName = tableName + (isBackgroundCheck ? "TMP" : "");

        if ((featureList != null) && !featureList.isEmpty()) {
            H2FeatureService.removeTableIfExists(layerName + "_1");
            final H2FeatureService internalService = new H2FeatureService(
                    removeFolderNameFromTableName(layerName),
                    H2FeatureServiceFactory.DB_NAME,
                    layerName
                            + "_1",
                    null,
                    null,
                    featureList,
                    cl.getOrderedFeatureServiceAttributes());
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
        final String layerName = tableName + (isBackgroundCheck ? "TMP" : "");

        return createLocalTopicByAttributeList(attributes, layerName, featureServiceAttributes);
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
            CidsLayer cl = initialisedService.get(mc.getTableName());

            if (cl == null) {
                cl = new CidsLayer(mc);
                cl.initAndWait();
                initialisedService.put(mc.getTableName(), cl);
            }

            final CidsLayerInfo layerInfo = CidsLayerUtil.getCidsLayerInfo(mc, SessionManager.getSession().getUser());

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
                    removeFolderNameFromTableName(tableName),
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
     * @param   owner     DOCUMENT ME!
     * @param   routeIds  DOCUMENT ME!
     * @param   classIds  DOCUMENT ME!
     * @param   export    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected ProblemCountAndClasses getErrorObjectsFromTree(final String owner,
            final int[] routeIds,
            final int[] classIds,
            final boolean export) throws Exception {
        final ArrayList<ArrayList> problemCountList = (ArrayList<ArrayList>)SessionManager.getProxy()
                    .customServerSearch(SessionManager.getSession().getUser(),
                            new RouteProblemsCountAndClasses(owner, routeIds, classIds, false, export));
        String count = null;
        final List<String> classes = new ArrayList<String>();

        if ((problemCountList != null) && !problemCountList.isEmpty()) {
            ArrayList innerList;

            for (int i = 0; i < (problemCountList.size() - 1); ++i) {
                innerList = problemCountList.get(i);

                if ((innerList != null) && !innerList.isEmpty()) {
                    classes.add((String)innerList.get(0));
                }
            }
            innerList = problemCountList.get(problemCountList.size() - 1);

            if ((innerList != null) && !innerList.isEmpty()) {
                count = (String)innerList.get(0);
            }
        }

        if (count != null) {
            final ProblemCountAndClasses problems = new ProblemCountAndClasses(
                    count,
                    classes.toArray(new String[classes.size()]));

            return problems;
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
     * DOCUMENT ME!
     *
     * @param  tableNames  DOCUMENT ME!
     */
    protected void removeServicesFromDb(final String[] tableNames) {
        for (final String table : tableNames) {
            if (isBackgroundCheck) {
                H2FeatureService.removeTableIfExists(table + "TMP_1");
            } else {
                H2FeatureService.removeTableIfExists(table + "_1");
            }
        }

        final Component capComponent = AppBroker.getInstance().getComponent(ComponentName.CAPABILITIES);

        if (capComponent instanceof CapabilityWidget) {
            final CapabilityWidget cap = (CapabilityWidget)capComponent;
            cap.refreshJdbcTrees();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  tableNames  DOCUMENT ME!
     */
    protected void removeServicesFromLayerModel(final String[] tableNames) {
        for (final String table : tableNames) {
            removeTableFromActiveLayerModel(table + "_1");
        }
    }

    /**
     * Removes the given entry from the MappingModel.
     *
     * @param  tableNames  entry the entry to remove
     */
    private void removeTableFromActiveLayerModel(final String tableNames) {
        final MappingModel model = CismapBroker.getInstance().getMappingComponent().getMappingModel();
        final TreeMap<Integer, MapService> map = model.getRasterServices();

        if (map != null) {
            final List<LayerCollection> collectionsToRemove = new ArrayList<LayerCollection>();
            for (final MapService service : map.values()) {
                if (service instanceof H2FeatureService) {
                    final H2FeatureService h2Service = (H2FeatureService)service;
                    if (h2Service.getTableName().equals(tableNames)) {
                        final TreeMap<Integer, Object> serviceMap = ((ActiveLayerModel)model)
                                    .getMapServicesAndCollections();

                        for (final Integer key : serviceMap.keySet()) {
                            final Object l = serviceMap.get(key);
                            if (l instanceof LayerCollection) {
                                final LayerCollection coll = getCollectionWithService((LayerCollection)l, h2Service);

                                if (coll != null) {
                                    collectionsToRemove.add(coll);
                                }
                            }
                        }
                        model.removeLayer(h2Service);
                        AttributeTableFactory.getInstance().closeAttributeTable(h2Service);
                    }
                }
            }

            for (int i = 0; i < collectionsToRemove.size(); ++i) {
                final LayerCollection col = collectionsToRemove.get(i);

                final TreeMap<Integer, Object> serviceMap = ((ActiveLayerModel)model).getMapServicesAndCollections();

                for (final Integer key : serviceMap.keySet()) {
                    final Object l = serviceMap.get(key);
                    if (l instanceof LayerCollection) {
                        final LayerCollection coll = getCollectionWithService((LayerCollection)l, col);

                        if (coll != null) {
                            collectionsToRemove.add(coll);
                        }
                    }
                }

                ((ActiveLayerModel)model).removeLayerCollection(col);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   collection  DOCUMENT ME!
     * @param   h2Service   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private LayerCollection getCollectionWithService(final LayerCollection collection, final Object h2Service) {
        if (collection.contains(h2Service) && (collection.size() == 1)) {
            return collection;
        } else {
            for (final Object subLayer : collection) {
                if (subLayer instanceof LayerCollection) {
                    return getCollectionWithService((LayerCollection)subLayer, h2Service);
                }
            }
        }

        return null;
    }

    /**
     * refreshs the LayertreeWidget.
     */
    protected void refreshTree() {
        final ThemeLayerWidget tree = (ThemeLayerWidget)AppBroker.getInstance().getComponent(ComponentName.TREE);
        tree.updateTree();
    }

    /**
     * DOCUMENT ME!
     */
    protected void refreshMap() {
        if (AppBroker.getInstance().getMappingComponent() != null) {
            AppBroker.getInstance().getMappingComponent().refresh();
        }
    }

    /**
     * Shows the service in the ThemeLayerWidget and refreshs the Capabilities tab of the internal db.
     *
     * @param  service  DOCUMENT ME!
     */
    protected void showService(final H2FeatureService service) {
        String folder = service.getTableName();

        if (folder.contains("->")) {
            folder = service.getTableName().substring(0, service.getTableName().lastIndexOf("->"));
        }
        showService(service, folder);
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
        final List<Object> pathObjects = new ArrayList<Object>();

        if (folder != null) {
            final String[] tokens = folder.split("->");

            for (final String subFolder : tokens) {
                if (layerCollection != null) {
                    boolean found = false;

                    for (int i = 0; i < layerCollection.size(); ++i) {
                        final Object tmp = layerCollection.get(i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            pathObjects.add(layerCollection);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        final LayerCollection newLayerCollection = new LayerCollection();
                        newLayerCollection.setName(subFolder);
                        layerCollection = newLayerCollection;
                        model.addEmptyLayerCollection(new TreePath(pathObjects.toArray()), layerCollection);
                        pathObjects.add(newLayerCollection);
                    }
                } else {
                    boolean found = false;

                    for (int i = 0; i < model.getChildCount(model.getRoot()); ++i) {
                        final Object tmp = model.getChild(model.getRoot(), i);

                        if ((tmp instanceof LayerCollection) && ((LayerCollection)tmp).getName().equals(subFolder)) {
                            layerCollection = (LayerCollection)tmp;
                            pathObjects.add(layerCollection);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        layerCollection = new LayerCollection();
                        layerCollection.setName(subFolder);
                        model.addEmptyLayerCollection(layerCollection);
                        pathObjects.add(layerCollection);
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

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    protected static class ProblemCountAndClasses {

        //~ Instance fields ----------------------------------------------------

        private int count;
        private String classes;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new ProblemCountAndClasses object.
         *
         * @param  count    DOCUMENT ME!
         * @param  classes  DOCUMENT ME!
         */
        public ProblemCountAndClasses(final String count, final String[] classes) {
            try {
                this.count = Integer.parseInt(count);
            } catch (NumberFormatException ex) {
                LOG.error("Cannot parse problem count", ex);
            }

            if ((classes == null) || (classes.length == 0)) {
                this.classes = "";
            } else {
                for (final String cl : classes) {
                    if (this.classes == null) {
                        this.classes = cl;
                    } else {
                        this.classes += ", " + cl;
                    }
                }
            }
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the count
         */
        public int getCount() {
            return count;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the classes
         */
        public String getClasses() {
            return classes;
        }
    }
}
