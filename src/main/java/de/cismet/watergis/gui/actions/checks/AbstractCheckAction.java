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

import org.apache.log4j.Logger;

import java.awt.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.cidslayer.CidsLayerInfo;

import de.cismet.cids.tools.CidsLayerUtil;

import de.cismet.cismap.cidslayer.CidsLayer;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.featureservice.H2FeatureService;
import de.cismet.cismap.commons.featureservice.factory.H2FeatureServiceFactory;
import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;

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
            final CidsLayerInfo layerInfo = CidsLayerUtil.getCidsLayerInfo(mc);
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

            final H2FeatureService internalService = new H2FeatureService(
                    tableName,
                    H2FeatureServiceFactory.DB_NAME,
                    tableName,
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
     * Shows the service in the ThemeLayerWidget and refreshs the Capabilities tab of the internal db.
     *
     * @param  service  DOCUMENT ME!
     */
    protected void showService(final H2FeatureService service) {
        AppBroker.getInstance().getMappingComponent().getMappingModel().addLayer(service);
        final Component capComponent = AppBroker.getInstance().getComponent(ComponentName.CAPABILITIES);

        if (capComponent instanceof CapabilityWidget) {
            final CapabilityWidget cap = (CapabilityWidget)capComponent;
            cap.refreshJdbcTrees();
        }
    }
}
